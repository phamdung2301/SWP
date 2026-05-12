package com.liteflow.controller.auth;

import com.liteflow.model.auth.User;
import com.liteflow.service.auth.UserService;
import com.liteflow.service.auth.AuditService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;

@WebServlet(urlPatterns = {"/auth/reset"})
public class ResetPasswordServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("resetAllowed"))) {
            // Phải xác thực OTP trước mới được vào reset
            resp.sendRedirect(req.getContextPath() + "/auth/forgot");
            return;
        }
        // Tạo CSRF token mới cho form reset
        String csrfToken = java.util.UUID.randomUUID().toString();
        session.setAttribute("csrfToken", csrfToken);
        req.setAttribute("csrfToken", csrfToken);
        req.getRequestDispatcher("/auth/reset.jsp").forward(req, resp);
    }

    private final UserService userService = new UserService();
    private final AuditService audit = new AuditService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // ✅ CSRF bảo vệ
        String csrfForm = req.getParameter("csrfToken");
        HttpSession session = req.getSession(false);
        String csrfSess = session != null ? (String) session.getAttribute("csrfToken") : null;
        if (csrfSess == null || !csrfSess.equals(csrfForm)) {
            req.setAttribute("error", "Invalid request. Please refresh and try again.");
            req.getRequestDispatcher("/auth/reset.jsp").forward(req, resp);
            return;
        }
        // Chỉ cho phép nếu đã verify OTP trước đó (đã set resetAllowed ở VerifyOtpServlet)
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("resetAllowed"))) {
            req.setAttribute("error", "Session expired or invalid. Please request OTP again.");
            req.getRequestDispatcher("/auth/forgot.jsp").forward(req, resp);
            return;
        }

        String email = (String) session.getAttribute("otpEmail");
        String ip = req.getRemoteAddr();

    // Chỉ cho phép nếu đã verify OTP trước đó

        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        if (email == null || email.isBlank()) {
            cleanupOtp(session);
            req.setAttribute("error", "No email found in session. Please request OTP again.");
            req.getRequestDispatcher("/auth/forgot.jsp").forward(req, resp);
            return;
        }

        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            req.setAttribute("error", "Passwords do not match.");
            req.getRequestDispatcher("/auth/reset.jsp").forward(req, resp);
            return;
        }

        User u = userService.getUserByEmail(email);
        if (u == null) {
            cleanupOtp(session);
            req.setAttribute("error", "Email không tồn tại.");
            req.getRequestDispatcher("/auth/reset.jsp").forward(req, resp);
            return;
        }

        // OTP đã được xác thực ở bước verify → không nhập lại tại đây

        // ✅ change password
        // enforce password policy: tối thiểu 8 ký tự, ít nhất 1 chữ hoa và 1 số
        if (!java.util.regex.Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$").matcher(newPassword).matches()) {
            req.setAttribute("error", "Mật khẩu tối thiểu 8 ký tự, có ít nhất 1 chữ hoa và 1 số.");
            req.getRequestDispatcher("/auth/reset.jsp").forward(req, resp);
            return;
        }

        boolean ok = userService.changePassword(u, newPassword, ip);
        if (ok) {
            audit.log(u, AuditService.AuditAction.PASSWORD_CHANGED,
                    AuditService.ObjectType.USER,
                    u.getUserID().toString(),
                    "Password reset via OTP",
                    ip);

            // Cleanup reset flags and otp data
            cleanupOtp(session);
            session.removeAttribute("resetAllowed");
            session.removeAttribute("resetEmail");

            req.setAttribute("msg", "✅ Mật khẩu đã được đổi thành công. Vui lòng đăng nhập.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
        } else {
            req.setAttribute("error", "Không thể đặt lại mật khẩu.");
            req.getRequestDispatcher("/auth/reset.jsp").forward(req, resp);
        }
    }

    private void cleanupOtp(HttpSession session) {
        session.removeAttribute("otpEmail");
        session.removeAttribute("otpContext");
        session.removeAttribute("signupUsername");
        session.removeAttribute("signupPassword");
        session.removeAttribute("pendingUser");
        session.removeAttribute("pendingAccessToken");
    }

    // no extra helpers
}
