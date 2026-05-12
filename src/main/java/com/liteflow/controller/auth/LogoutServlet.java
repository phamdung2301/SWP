package com.liteflow.controller.auth;

import com.liteflow.security.JwtUtil;
import com.liteflow.service.auth.AuditService;
import com.liteflow.service.auth.UserService;
import com.liteflow.model.auth.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.UUID;

@WebServlet(urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private final AuditService audit = new AuditService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String ip = req.getRemoteAddr();
        HttpSession session = req.getSession(false);

        // Lấy refresh token từ cookie (ưu tiên workflow refresh)
        String refreshToken = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("LITEFLOW_REFRESH".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                Jws<Claims> parsed = JwtUtil.parse(refreshToken);
                String uid = (String) parsed.getBody().get("uid");
                UUID userId = UUID.fromString(uid);

                User user = userService.getUserById(userId).orElse(null);

                // Đánh dấu thời gian logout để hỗ trợ logic bỏ qua OTP trong 24h
                // (Logic này được xử lý trong LoginServlet thông qua last2faVerifiedAt)

                // Audit log
                audit.logLogout(user, ip);

            } catch (JwtException e) {
                System.err.println("❌ Invalid refresh token on logout: " + e.getMessage());
                audit.log(
                        null,
                        AuditService.AuditAction.ACCESS_DENIED,
                        AuditService.ObjectType.USER,
                        null,
                        "Logout failed: invalid refresh token (" + e.getMessage() + ")",
                        ip
                );
            }
        }

        // Xoá session trong memory
        if (session != null) {
            session.invalidate();
        }

        // Xoá refresh token cookie
        clearCookie(resp, "LITEFLOW_REFRESH", req.getContextPath());

        // Xoá access token cookie
        clearCookie(resp, "LITEFLOW_TOKEN", req.getContextPath());

        // KHÔNG xóa LITEFLOW_REMEMBER cookie để giữ thông tin remember me
        // Cookie này chỉ được xóa khi user uncheck "Remember me" hoặc login với remember me = false

        // Redirect về login
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    private void clearCookie(HttpServletResponse resp, String name, String contextPath) {
        Cookie c = new Cookie(name, "");
        c.setMaxAge(0);
        c.setPath(contextPath == null || contextPath.isEmpty() ? "/" : contextPath);
        c.setHttpOnly(true);
        c.setSecure(false); // ⚠ Prod: set true
        resp.addCookie(c);
    }
}
