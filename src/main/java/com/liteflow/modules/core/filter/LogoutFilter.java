package com.liteflow.modules.core.filter;

import com.liteflow.filter.BaseFilter;
import com.liteflow.modules.auth.model.User;
import com.liteflow.modules.auth.service.AuditService;
import com.liteflow.modules.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

public class LogoutFilter extends BaseFilter {

    private final AuditService audit = new AuditService();
    private final UserService userService = new UserService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = asHttp(request);
        HttpServletResponse res = asHttp(response);
        HttpSession session = getSession(req, false);

        User u = null;
        if (session != null) {
            Object s = session.getAttribute("UserLogin");
            if (s instanceof User) {
                u = (User) s;
            } else if (s instanceof UUID) {
                u = userService.getUserById((UUID) s).orElse(null);
            } else if (s instanceof String) {
                String sval = (String) s;
                try {
                    u = userService.getUserById(UUID.fromString(sval)).orElse(null);
                } catch (IllegalArgumentException ex) {
                    u = userService.findByEmail(sval);
                }
            }
        }

        Cookie expired = new Cookie("LITEFLOW_TOKEN", "");
        expired.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        expired.setHttpOnly(true);
        expired.setSecure(false);
        expired.setMaxAge(0);
        res.addCookie(expired);

        if (session != null) {
            session.invalidate();
        }

        if (u != null) {
            audit.logLogout(u, req.getRemoteAddr());
        }

        res.sendRedirect(req.getContextPath() + "/login");
    }
}
