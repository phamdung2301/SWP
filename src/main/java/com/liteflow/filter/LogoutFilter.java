package com.liteflow.filter;

import com.liteflow.model.auth.User;
import com.liteflow.service.auth.AuditService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;

// @WebFilter annotation removed - using web.xml mapping
public class LogoutFilter extends BaseFilter {

    private final AuditService audit = new AuditService();

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
            } else if (s instanceof java.util.UUID) {
                u = new com.liteflow.service.auth.UserService().getUserById((java.util.UUID) s).orElse(null);
            } else if (s instanceof String) {
                String sval = (String) s;
                try {
                    u = new com.liteflow.service.auth.UserService().getUserById(java.util.UUID.fromString(sval)).orElse(null);
                } catch (IllegalArgumentException ex) {
                    u = new com.liteflow.service.auth.UserService().findByEmail(sval);
                }
            }
        }

        Cookie expired = new Cookie("LITEFLOW_TOKEN", "");
        expired.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        expired.setHttpOnly(true);
        expired.setSecure(false); // ⚠ Prod = true
        expired.setMaxAge(0);
        res.addCookie(expired);

        // KHÔNG xóa LITEFLOW_REMEMBER cookie để giữ thông tin remember me
        // Cookie này chỉ được xóa khi user uncheck "Remember me" hoặc login với remember me = false

        if (session != null) {
            session.invalidate();
        }

        if (u != null) {
            audit.log(u,
                    AuditService.AuditAction.LOGOUT,
                    AuditService.ObjectType.USER,
                    u.getUserID().toString(),
                    "User logout via /logout",
                    req.getRemoteAddr());
        }

        res.sendRedirect(req.getContextPath() + "/login");
    }
}
