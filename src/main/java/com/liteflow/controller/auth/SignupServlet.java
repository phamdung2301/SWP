package com.liteflow.controller.auth;

import com.liteflow.model.auth.User;
import com.liteflow.service.auth.AuditService;
import com.liteflow.service.auth.UserService;
// import com.liteflow.util.MailUtil; // bật khi có mail server

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
// ...existing code...
import java.util.regex.Pattern;
import java.util.logging.Logger;
import com.liteflow.service.auth.OtpService;

/**
 * SignupServlet: - Tiếp nhận request đăng ký - Validate input - Sinh OTP, lưu
 * session - Chuyển sang verify-otp.jsp để xác thực
 */
@WebServlet(urlPatterns = {"/register"})
public class SignupServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final AuditService audit = new AuditService();
    private final OtpService otpService = new OtpService();
    private static final Logger LOG = Logger.getLogger(SignupServlet.class.getName());

    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$");
    private static final Pattern PASSWORD_PATTERN
            = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ✅ CSRF bảo vệ
        String csrfForm = req.getParameter("csrfToken");
        String csrfSess = (String) req.getSession().getAttribute("csrfToken");
        if (csrfSess == null || !csrfSess.equals(csrfForm)) {
            req.setAttribute("error", "Invalid request. Please refresh and try again.");
            req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
            return;
        }

        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");
        String ip = req.getRemoteAddr();

        // Validate input rỗng
        if (username == null || email == null || password == null || confirm == null) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
            return;
        }

        username = username.trim();
        email = email.trim().toLowerCase();

        // Validate email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            // Trả lỗi cụ thể cho field email và giữ lại giá trị người dùng đã nhập
            req.setAttribute("emailError", "Email phải hợp lệ và kết thúc bằng .com");
            req.setAttribute("usernameValue", username);
            req.setAttribute("emailValue", email);
            req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
            return;
        }

        // Validate password
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            req.setAttribute("passwordError", "Mật khẩu tối thiểu 8 ký tự, có ít nhất 1 chữ hoa và 1 số.");
            req.setAttribute("usernameValue", username);
            req.setAttribute("emailValue", email);
            req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
            return;
        }

        // Confirm password
        if (!password.equals(confirm)) {
            req.setAttribute("confirmError", "Mật khẩu xác nhận không khớp.");
            req.setAttribute("usernameValue", username);
            req.setAttribute("emailValue", email);
            req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
            return;
        }

        // Check user tồn tại
        User existing = userService.findByEmail(email);
        if (existing != null) {
            req.setAttribute("emailError", "Email đã được đăng ký. Vui lòng đăng nhập hoặc đặt lại mật khẩu.");
            req.setAttribute("usernameValue", username);
            req.setAttribute("emailValue", email);
            req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
            return;
        }

        // ✅ Lưu thông tin signup (hash mật khẩu) vào session, phát OTP lưu vào DB
        HttpSession session = req.getSession(true);
        session.setAttribute("otpContext", "signup");
        session.setAttribute("otpEmail", email);
        session.setAttribute("signupUsername", username);
        // Hash mật khẩu trước khi lưu vào session để tránh giữ plaintext
        String hashed = com.liteflow.util.PasswordUtil.hash(password, 12);
        session.setAttribute("signupPasswordHash", hashed);

        // Issue OTP via DB-backed OtpService (this will also invalidate prior OTPs for the email)
        String otp = otpService.issueOtpForEmail(email, ip);
        try {
            if (!"admin@liteflow.com".equalsIgnoreCase(email)) {
                com.liteflow.util.MailUtil.sendOtpMail(email, otp);
                LOG.info("📧 OTP signup gửi tới " + email + ": " + otp);
            } else {
                LOG.info("🔑 Admin fixed OTP: 000000");
            }
        } catch (Exception e) {
            LOG.warning("❌ Lỗi gửi mail OTP: " + e.getMessage());
        }

        // Fallback OTP lưu session (dự phòng nếu DB OTP không đọc được do schema cũ)
        session.setAttribute("signupOtpCode", otp);
        session.setAttribute("signupOtpExpire", java.time.LocalDateTime.now().plusMinutes(5));

        // Audit log OTP
        audit.log(null,
                AuditService.AuditAction.OTP_ISSUED,
                AuditService.ObjectType.USER,
                email,
                "OTP issued for signup",
                ip);

    // Chuyển tới đường dẫn thân thiện /auth/verify (kèm email để JSP fallback nếu mất session)
    String qEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
    resp.sendRedirect(req.getContextPath() + "/auth/verify?email=" + qEmail + "&sent=1");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Sinh CSRF token giống LoginServlet
        String csrfToken = java.util.UUID.randomUUID().toString();
        req.getSession(true).setAttribute("csrfToken", csrfToken);
        req.setAttribute("csrfToken", csrfToken);
        req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
    }
}
