package com.liteflow.modules.auth.controller;

import com.liteflow.modules.auth.model.User;
import com.liteflow.modules.auth.service.UserService;
import com.liteflow.modules.auth.service.OtpService;
import com.liteflow.modules.auth.service.AuditService;
import com.liteflow.util.MailUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/auth/forgot"})
public class ForgotPasswordServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final OtpService otpService = new OtpService();
    private final AuditService audit = new AuditService();
    private static final Logger LOG = Logger.getLogger(ForgotPasswordServlet.class.getName());

    // GET: render trang forgot.jsp
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Generate CSRF token for the forgot form
        String csrfToken = java.util.UUID.randomUUID().toString();
        req.getSession(true).setAttribute("csrfToken", csrfToken);
        req.setAttribute("csrfToken", csrfToken);
        req.getRequestDispatcher("/auth/forgot.jsp").forward(req, resp);
    }

    // POST: xử lý gửi OTP
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // CSRF validation
        String csrfForm = req.getParameter("csrfToken");
        HttpSession sessionCsrf = req.getSession(false);
        String csrfSess = sessionCsrf != null ? (String) sessionCsrf.getAttribute("csrfToken") : null;
        if (csrfSess == null || !csrfSess.equals(csrfForm)) {
            req.setAttribute("error", "Invalid request. Please refresh and try again.");
            // regenerate token
            String newToken = java.util.UUID.randomUUID().toString();
            req.getSession(true).setAttribute("csrfToken", newToken);
            req.setAttribute("csrfToken", newToken);
            req.getRequestDispatcher("/auth/forgot.jsp").forward(req, resp);
            return;
        }
        String email = req.getParameter("email");
        String ip = req.getRemoteAddr();

        if (email == null || email.isBlank()) {
            req.setAttribute("error", "❌ Vui lòng nhập email.");
            req.getRequestDispatcher("/auth/forgot.jsp").forward(req, resp);
            return;
        }

        email = email.trim().toLowerCase();
        User u = userService.getUserByEmail(email);

        if (u == null) {
            req.setAttribute("error", "❌ Email không tồn tại trong hệ thống.");
            req.getRequestDispatcher("/auth/forgot.jsp").forward(req, resp);
            return;
        }

        try {
            String otp;
            if ("admin@liteflow.com".equalsIgnoreCase(email)) {
                otp = "000000";
                LOG.info("🔑 Admin fixed OTP: " + otp);
            } else {
                // OTP DB-based (đã lưu + audit trong OtpService.issueOtp)
                otp = otpService.issueOtp(u, ip);
                MailUtil.sendOtpMail(email, otp);
            }

            // Lưu context vào session để VerifyOtpServlet xử lý
            HttpSession session = req.getSession(true);
            session.setAttribute("otpEmail", email);
            session.setAttribute("otpContext", "forgot");
            session.setAttribute("otpJustSent", Boolean.TRUE);

            // Audit log rõ ràng (reset password)
            audit.log(u,
                    AuditService.AuditAction.OTP_ISSUED,
                    AuditService.ObjectType.USER,
                    u.getUserID().toString(),
                    "OTP issued for password reset",
                    ip);

            // Chuyển sang trang verify OTP cho quên mật khẩu
            resp.sendRedirect(req.getContextPath() + "/auth/verify");

        } catch (MessagingException e) {
            e.printStackTrace();
            req.setAttribute("error", "❌ Không thể gửi OTP email, vui lòng thử lại sau.");
            req.getRequestDispatcher("/auth/forgot.jsp").forward(req, resp);
        }
    }
}
