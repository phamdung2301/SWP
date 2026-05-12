package com.liteflow.filter;

import com.liteflow.model.auth.User;
import com.liteflow.security.JwtUtil;
import com.liteflow.service.auth.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

// @WebFilter annotation removed - using web.xml mapping
public class LoginFilter extends BaseFilter {

    private static volatile boolean LOGIN_FILTER_ENABLED = true;
    private final UserService userService = new UserService();

    @Override
    public void init(FilterConfig filterConfig) {
        try {
            String v = System.getenv("LITEFLOW_LOGINFILTER_ENABLED");
            if (v == null) {
                v = System.getProperty("LITEFLOW_LOGINFILTER_ENABLED");
            }
            if (v != null) {
                LOGIN_FILTER_ENABLED = Boolean.parseBoolean(v);
            }
        } catch (Exception ignore) {
        }
        java.util.logging.Logger.getLogger(LoginFilter.class.getName()).info("[LoginFilter] LOGIN_FILTER_ENABLED = " + LOGIN_FILTER_ENABLED);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!LOGIN_FILTER_ENABLED) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = asHttp(request);
        HttpServletResponse res = asHttp(response);
        HttpSession session = getSession(req, false);

        User u = null;
        if (session != null) {
            Object s = session.getAttribute("UserLogin");
            if (s instanceof User) {
                u = (User) s;
            } else if (s instanceof java.util.UUID) {
                u = userService.getUserById((java.util.UUID) s).orElse(null);
            } else if (s instanceof String) {
                // could be UUID string or email
                String sval = (String) s;
                try {
                    u = userService.getUserById(java.util.UUID.fromString(sval)).orElse(null);
                } catch (IllegalArgumentException e) {
                    u = userService.findByEmail(sval);
                }
            }
        }

        if (u == null) {
            String jwt = null;
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("LITEFLOW_TOKEN".equals(c.getName())) {
                        jwt = c.getValue();
                        break;
                    }
                }
            }
            if (jwt != null && !jwt.isBlank()) {
                try {
                    Jws<Claims> jws = JwtUtil.parse(jwt);
                    String sub = jws.getBody().getSubject();
                    try {
                        Optional<User> opt = userService.getUserById(UUID.fromString(sub));
                        u = opt.orElse(null);
                    } catch (IllegalArgumentException e) {
                        u = userService.findByEmail(sub);
                    }
                } catch (JwtException e) {
                    java.util.logging.Logger.getLogger(LoginFilter.class.getName()).warning("[LoginFilter] Invalid JWT: " + e.getMessage());
                }
            }
        }

        if (u != null) {
            res.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        chain.doFilter(request, response);
    }
}
