package com.liteflow.filter;

import com.liteflow.model.auth.User;
import com.liteflow.security.JwtUtil;
import com.liteflow.service.employee.EmployeeService;
import com.liteflow.service.auth.AuditService;
import com.liteflow.service.auth.UserService;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

// @WebFilter annotation removed - using web.xml mapping to control filter order
public class AuthenticationFilter extends BaseFilter {

    private static volatile boolean AUTH_ENABLED = false;
//ádasdáds
    private final AuditService auditService = new AuditService();
    private final UserService userService = new UserService();
    private final EmployeeService employeeService = new EmployeeService();

    private static final Map<String, Set<String>> ROLE_FUNCTIONS = new HashMap<>();

    static {
        ROLE_FUNCTIONS.put("Cashier", new HashSet<>(Arrays.asList("/pos", "/sales", "/cart", "/checkout")));
        ROLE_FUNCTIONS.put("Inventory Manager", new HashSet<>(Arrays.asList("/inventory", "/products", "/stock", "/purchaseOrders")));
        ROLE_FUNCTIONS.put("Procurement Officer", new HashSet<>(Arrays.asList("/purchaseOrders", "/suppliers", "/invoices")));
        ROLE_FUNCTIONS.put("HR Officer", new HashSet<>(Arrays.asList("/employees", "/payroll", "/timesheets", "/leaveRequests")));
        // Allow employees to access their own user pages and dashboard
        ROLE_FUNCTIONS.put("Employee", new HashSet<>(Arrays.asList(
                "/dashboard",
                "/schedule",
                "/user/profile",
                "/user/timesheet",
                "/user/payroll",
                "/api/notices"  // Employee có thể xem thông báo
        )));
        ROLE_FUNCTIONS.put("Admin", new HashSet<>(Arrays.asList(
                "/*",
                "/api/send-notification",  // Admin có thể gửi thông báo
                "/api/notices"  // Admin có thể xem thông báo
        ))); // full quyền

        // ============================================================
        // 🆕 PHÂN QUYỀN MODULE PROCUREMENT (THÊM MỚI)
        // ============================================================
        // Cho phép truy cập các đường dẫn trong module Procurement:
        // /procurement/supplier, /procurement/po, /procurement/gr ...
        Set<String> procurementPaths = new HashSet<>(Arrays.asList(
                "/procurement",
                "/procurement/dashboard",
                "/procurement/supplier",
                "/procurement/po",
                "/procurement/gr"
        ));

        List<String> targetRoles = Arrays.asList("Procurement Officer", "Inventory Manager", "Owner", "Admin");

        for (String role : targetRoles) {
            Set<String> funcs = new HashSet<>(ROLE_FUNCTIONS.getOrDefault(role, Collections.emptySet()));
            funcs.addAll(procurementPaths);
            ROLE_FUNCTIONS.put(role, funcs);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
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
        java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).info("[AuthenticationFilter] AUTH_ENABLED = " + AUTH_ENABLED);
    }

    private boolean isStaticResource(String path) {
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || path.startsWith("/img/")
                || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")
                || path.endsWith(".gif") || path.endsWith(".svg") || path.endsWith(".ico")
                || path.endsWith(".woff") || path.endsWith(".woff2") || path.endsWith(".ttf") || path.endsWith(".map");
    }

    private boolean isPublicPage(String path) {
        // Allow root path and welcome file
        if (path.equals("/") || path.equals("")) {
            return true;
        }
        return path.equals("/login") || path.equals("/register") || path.equals("/logout")
                || path.equals("/auth/google") || path.equals("/oauth2callback")
                || path.equals("/auth/forgot") || path.equals("/auth/reset") || path.equals("/verify-otp")
                || path.equals("/auth/verify") || path.equals("/auth/verify-otp")
                || path.equals("/send-otp")
                || (path.endsWith(".jsp") && (path.startsWith("/auth/") || path.equals("/login.jsp") || path.equals("/register.jsp")))
                || path.equals("/health") || path.equals("/accessDenied.jsp")
                || path.startsWith("/public/") || path.startsWith("/api/public/");
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

        // JWT
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
                    // Lưu employeeCode nếu user có employee record
                    employeeService.getEmployeeByUserID(user.getUserID()).ifPresent(emp -> {
                        session.setAttribute("UserEmployeeCode", emp.getEmployeeCode());
                        java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).info("Set UserEmployeeCode: " + emp.getEmployeeCode());
                    });
                }
            } catch (JwtException e) {
                auditService.logLoginFail("Invalid JWT", req.getRemoteAddr());
            }
        }

        // Session fallback: accept User object, UUID, or String (uuid or email)
        if (user == null) {
            HttpSession session = getSession(req, false);
            if (session != null) {
                Object sUser = session.getAttribute("UserLogin");
                java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).info("Session UserLogin attribute: " + sUser + " (type: " + (sUser != null ? sUser.getClass().getSimpleName() : "null") + ")");
                if (sUser instanceof User) {
                    User u = (User) sUser;
                    user = u;
                } else if (sUser instanceof java.util.UUID) {
                    user = userService.getUserById((java.util.UUID) sUser).orElse(null);
                } else if (sUser instanceof String) {
                    String sval = (String) sUser;
                    try {
                        user = userService.getUserById(java.util.UUID.fromString(sval)).orElse(null);
                    } catch (IllegalArgumentException ex) {
                        user = userService.findByEmail(sval);
                    }
                }

                if (user != null) {
           
                    List<String> sRoles = (List<String>) session.getAttribute("UserRoles");
                    roles = (sRoles != null) ? sRoles : userService.getRoleNames(user.getUserID());
                    // Ensure displayName is set in session
                    if (session.getAttribute("UserDisplayName") == null) {
                        session.setAttribute("UserDisplayName", user.getDisplayName());
                    }
                    // Ensure employeeCode is set in session
                    if (session.getAttribute("UserEmployeeCode") == null) {
                        employeeService.getEmployeeByUserID(user.getUserID()).ifPresent(emp -> {
                            session.setAttribute("UserEmployeeCode", emp.getEmployeeCode());
                            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).info("Set UserEmployeeCode (fallback): " + emp.getEmployeeCode());
                        });
                    }
                    java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).info("User found: " + user.getUserID() + ", Roles: " + roles);
                }
            }
        }

        if (user == null) {
            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).warning("No user found for path: " + path + " - redirecting to login");
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (isAuthorized(roles, path)) {
            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).info("Access granted for " + path + " - proceeding to servlet");
            chain.doFilter(req, res);
        } else {
            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).warning("Access denied for " + path + " - redirecting to access denied");
            auditService.logDenied(user, path, req.getRemoteAddr());
            res.sendRedirect(req.getContextPath() + "/accessDenied.jsp");
        }
    }

    private boolean isAuthorized(List<String> roles, String path) {
        if (roles == null || roles.isEmpty()) {
            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).warning("No roles found for path: " + path);
            return false;
        }
        if (roles.stream().anyMatch(r -> r.equalsIgnoreCase("owner") || r.equalsIgnoreCase("admin"))) {
            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).info("Access granted for " + path + " - Owner/Admin role");
            return true;
        }
        String lowerPath = path.toLowerCase();
        for (String role : roles) {
            Set<String> funcs = ROLE_FUNCTIONS.getOrDefault(role, Collections.emptySet());
            if (funcs.stream().anyMatch(f -> lowerPath.startsWith(f.toLowerCase()))) {
                java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).info("Access granted for " + path + " - Role: " + role);
                return true;
            }
        }
        java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).warning("Access denied for " + path + " - Roles: " + roles + ", Available functions: " + ROLE_FUNCTIONS);
        return false;
    }
}
