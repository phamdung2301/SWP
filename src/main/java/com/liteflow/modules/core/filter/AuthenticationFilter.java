package com.liteflow.modules.core.filter;

import com.liteflow.filter.BaseFilter;
import com.liteflow.modules.auth.model.User;
import com.liteflow.modules.auth.service.AuditService;
import com.liteflow.modules.auth.service.UserService;
import com.liteflow.modules.core.security.AccessPolicy;
import com.liteflow.modules.hr.service.EmployeeService;
import com.liteflow.security.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Xác thực + phân quyền theo role và URL. Bật bằng {@code LITEFLOW_AUTH_ENABLED=true} (env hoặc system property).
 */
public class AuthenticationFilter extends BaseFilter {

    private static final Logger LOG = Logger.getLogger(AuthenticationFilter.class.getName());

    private static volatile boolean AUTH_ENABLED = false;

    private final AuditService auditService = new AuditService();
    private final UserService userService = new UserService();
    private final EmployeeService employeeService = new EmployeeService();

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) {
        try {
            String env = System.getenv("LITEFLOW_AUTH_ENABLED");
            if (env == null) {
                env = System.getProperty("LITEFLOW_AUTH_ENABLED");
            }
            if (env != null) {
                AUTH_ENABLED = Boolean.parseBoolean(env);
            }
        } catch (Exception ignore) {
        }
        LOG.info("[AuthenticationFilter] AUTH_ENABLED = " + AUTH_ENABLED + " | roles configured: " + AccessPolicy.allConfiguredRoles());
    }

    private boolean isStaticResource(String path) {
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || path.startsWith("/img/")
                || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")
                || path.endsWith(".gif") || path.endsWith(".svg") || path.endsWith(".ico")
                || path.endsWith(".woff") || path.endsWith(".woff2") || path.endsWith(".ttf") || path.endsWith(".map");
    }

    private boolean isPublicPage(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return true;
        }
        if (path.equals("/login") || path.equals("/register") || path.equals("/logout")
                || path.equals("/auth/google") || path.equals("/oauth2callback")
                || path.equals("/auth/forgot") || path.equals("/auth/reset")
                || path.equals("/verify-otp") || path.equals("/auth/verify") || path.equals("/auth/verify-otp")
                || path.equals("/send-otp") || path.equals("/auth/refresh")) {
            return true;
        }
        if (path.endsWith(".jsp") && (path.startsWith("/auth/") || path.equals("/login.jsp") || path.equals("/register.jsp"))) {
            return true;
        }
        return path.equals("/health") || path.equals("/accessDenied.jsp")
                || path.startsWith("/public/") || path.startsWith("/api/public/");
    }

    /**
     * Chỉ Owner/Admin: cấu hình AI hàng loạt, gửi broadcast — tránh lộ/ lạm dụng dù có prefix rộng ở role khác.
     */
    private boolean isSuperUserOnlyPath(String pathLower) {
        return pathLower.startsWith("/api/ai-agent-config")
                || pathLower.startsWith("/ai-agent-config")
                || pathLower.startsWith("/api/send-notification");
    }

    private boolean hasSuperRole(List<String> roles) {
        if (roles == null) {
            return false;
        }
        for (String r : roles) {
            if (AccessPolicy.isSuperRole(r)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!AUTH_ENABLED) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = asHttp(request);
        HttpServletResponse res = asHttp(response);
        String path = getPath(req);

        if (isStaticResource(path) || isPublicPage(path)) {
            chain.doFilter(req, res);
            return;
        }

        User user = null;
        List<String> roles = Collections.emptyList();

        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                JwtUtil.UserContext ctx = JwtUtil.parseToUserContext(authHeader.substring(7));
                user = userService.getUserById(UUID.fromString(ctx.userId())).orElse(null);
                if (user != null) {
                    roles = (ctx.roles() == null || ctx.roles().isEmpty())
                            ? userService.getRoleNames(user.getUserID())
                            : ctx.roles();
                    HttpSession session = getSession(req, true);
                    session.setAttribute("UserLogin", user.getUserID().toString());
                    session.setAttribute("UserRoles", roles);
                    session.setAttribute("UserDisplayName", user.getDisplayName());
                    employeeService.getEmployeeByUserID(user.getUserID()).ifPresent(emp ->
                            session.setAttribute("UserEmployeeCode", emp.getEmployeeCode()));
                }
            } catch (JwtException e) {
                auditService.logLoginFail("Invalid JWT", req.getRemoteAddr());
            }
        }

        if (user == null) {
            HttpSession session = getSession(req, false);
            if (session != null) {
                Object sUser = session.getAttribute("UserLogin");
                if (sUser instanceof User) {
                    user = (User) sUser;
                } else if (sUser instanceof UUID) {
                    user = userService.getUserById((UUID) sUser).orElse(null);
                } else if (sUser instanceof String) {
                    String sval = (String) sUser;
                    try {
                        user = userService.getUserById(UUID.fromString(sval)).orElse(null);
                    } catch (IllegalArgumentException ex) {
                        user = userService.findByEmail(sval);
                    }
                }

                if (user != null) {
                    @SuppressWarnings("unchecked")
                    List<String> sRoles = (List<String>) session.getAttribute("UserRoles");
                    roles = (sRoles != null) ? sRoles : userService.getRoleNames(user.getUserID());
                    if (session.getAttribute("UserDisplayName") == null) {
                        session.setAttribute("UserDisplayName", user.getDisplayName());
                    }
                    if (session.getAttribute("UserEmployeeCode") == null) {
                        employeeService.getEmployeeByUserID(user.getUserID()).ifPresent(emp ->
                                session.setAttribute("UserEmployeeCode", emp.getEmployeeCode()));
                    }
                }
            }
        }

        if (user == null) {
            LOG.warning("No user found for path: " + path + " - redirecting to login");
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String pathLower = path.toLowerCase();
        if (isSuperUserOnlyPath(pathLower) && !hasSuperRole(roles)) {
            LOG.warning("Super-only path denied: " + path + " roles=" + roles);
            auditService.logDenied(user, path, req.getRemoteAddr());
            res.sendRedirect(req.getContextPath() + "/accessDenied.jsp");
            return;
        }

        if (AccessPolicy.isAuthorized(roles, path)) {
            chain.doFilter(req, res);
        } else {
            LOG.warning("Access denied for " + path + " roles=" + roles);
            auditService.logDenied(user, path, req.getRemoteAddr());
            res.sendRedirect(req.getContextPath() + "/accessDenied.jsp");
        }
    }
}
