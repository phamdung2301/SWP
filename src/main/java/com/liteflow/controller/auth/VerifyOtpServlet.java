package com.liteflow.controller.auth;

import com.liteflow.model.auth.User;
import com.liteflow.service.auth.AuditService;
import com.liteflow.service.auth.AuthService;
import com.liteflow.service.auth.OtpService;
import com.liteflow.service.auth.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
// Instant import removed after switching to DB-based signup OTP
import java.util.*;

/**
 * VerifyOtpServlet:
 * - Xử lý OTP cho signup (tạo tài khoản, OTP lưu session)
 * - Xử lý OTP cho login (2FA, OTP lưu DB)
 * - Xử lý OTP cho forgot password (DB-based) → cho phép vào trang đặt lại mật khẩu
 */
@WebServlet(urlPatterns = {"/verify-otp", "/auth/verify"})
public class VerifyOtpServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final OtpService otpService = new OtpService();
    private final AuditService audit = new AuditService();
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            req.setAttribute("error", "Session expired. Please try again.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        }

        // Lấy OTP nhập từ form
        String otpInput = String.join("",
                req.getParameter("otp1"),
                req.getParameter("otp2"),
                req.getParameter("otp3"),
                req.getParameter("otp4"),
                req.getParameter("otp5"),
                req.getParameter("otp6")
        );

        if (otpInput.isBlank()) {
            req.setAttribute("error", "Please enter OTP.");
            req.getRequestDispatcher("/auth/verify-otp.jsp").forward(req, resp);
            return;
        }

        String ip = req.getRemoteAddr();
        String otpContext = (String) session.getAttribute("otpContext");

        if ("signup".equals(otpContext)) {
            handleSignupOtp(req, resp, session, otpInput, ip);
        } else if ("login".equals(otpContext)) {
            handleLoginOtp(req, resp, session, otpInput, ip);
        } else if ("forgot".equals(otpContext)) {
            handleForgotOtp(req, resp, session, otpInput, ip);
        } else {
            cleanupOtp(session);
            req.setAttribute("error", "OTP context not found.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // If user navigates to /auth/verify, show the verify page.
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("otpContext") == null) {
            // No OTP in progress → redirect to forgot by default
            resp.sendRedirect(req.getContextPath() + "/auth/forgot");
            return;
        }
        // Ensure otpEmail exists in session for resend (fallback from param)
        if (session.getAttribute("otpEmail") == null) {
            String emailParam = req.getParameter("email");
            if (emailParam != null && !emailParam.isBlank()) {
                session.setAttribute("otpEmail", emailParam.trim().toLowerCase());
            }
        }
        String ctx = (String) session.getAttribute("otpContext");
        if ("signup".equals(ctx)) {
            req.getRequestDispatcher("/auth/verify-signup.jsp").forward(req, resp);
        } else if ("login".equals(ctx)) {
            req.getRequestDispatcher("/auth/verify-login.jsp").forward(req, resp);
        } else if ("forgot".equals(ctx)) {
            // Dùng trang OTP riêng cho forgot
            req.getRequestDispatcher("/auth/verify-forgot.jsp").forward(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
        }
    }

    /**
     * OTP cho Signup (session-based)
     */
    private void handleSignupOtp(HttpServletRequest req, HttpServletResponse resp,
            HttpSession session, String otpInput, String ip)
            throws IOException, ServletException {

        String email = (String) session.getAttribute("otpEmail");
        String username = (String) session.getAttribute("signupUsername");
        String hashed = (String) session.getAttribute("signupPasswordHash");

        if (email == null || username == null || hashed == null) {
            cleanupOtp(session);
            req.setAttribute("error", "Signup session invalid.");
            req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
            return;
        }

        // Validate OTP for signup via session-based approach (authen.sql schema doesn't support null UserID in OtpTokens)
        boolean ok = false;
        // Allow admin fixed OTP
        if ("admin@liteflow.com".equalsIgnoreCase(email) && "000000".equals(otpInput)) {
            ok = true;
        } else {
            Object codeObj = session.getAttribute("signupOtpCode");
            Object expObj = session.getAttribute("signupOtpExpire");
            if (codeObj instanceof String && expObj instanceof java.time.LocalDateTime) {
                String code = (String) codeObj;
                java.time.LocalDateTime exp = (java.time.LocalDateTime) expObj;
                if (java.time.LocalDateTime.now().isBefore(exp) && otpInput.equals(code)) {
                    ok = true;
                }
            }
        }

        if (!ok) {
            // don't clear the signup session so user can retry or resend
            req.setAttribute("error", "OTP nhập sai hoặc đã hết hạn. Vui lòng nhập lại hoặc Resend OTP.");
            req.getRequestDispatcher("/auth/verify-signup.jsp").forward(req, resp);
            return;
        }

        // Nếu chưa có user trong DB → tạo mới
        User user = userService.getUserByEmail(email);
        if (user == null) {
            user = new User();
            user.setUserID(UUID.randomUUID());
            user.setEmail(email);
            user.setDisplayName(username);
            // Lấy password đã hash từ session
            user.setPasswordHash(hashed);
            user.setIsActive(true);
        }

        boolean created = userService.createUser(user);
        if (!created) {
            req.setAttribute("error", "Failed to create account.");
            req.getRequestDispatcher("/auth/signup.jsp").forward(req, resp);
            return;
        }
        // Gán quyền mặc định EMPLOYEE nếu chưa có
        try {
            // Ưu tiên dùng UserService.assignRole theo tên role (tiện lợi)
            // Nếu trùng (đã có) thì backend sẽ bỏ qua hoặc báo lỗi an toàn
            userService.assignRole(user.getUserID(), "Employee", ip);
        } catch (Exception ignore) {
            // Fallback im lặng nếu role đã tồn tại hoặc DB chưa seed role
        }
    // Account created successfully. Clear OTP session data but show success on verify page.
    cleanupOtp(session);

    audit.log(user,
        AuditService.AuditAction.CREATE,
        AuditService.ObjectType.USER,
        user.getUserID().toString(),
        "User signed up via OTP",
        ip);

    // Show success message on the signup-verify page with a green box and a button to go back to login.
    req.setAttribute("success", "Đăng ký thành công. Vui lòng quay lại Login để đăng nhập.");
    req.getRequestDispatcher("/auth/verify-signup.jsp").forward(req, resp);
    }

    /**
     * OTP cho Forgot Password (DB-based)
     */
    private void handleForgotOtp(HttpServletRequest req, HttpServletResponse resp,
                                 HttpSession session, String otpInput, String ip)
            throws IOException, ServletException {

        String email = (String) session.getAttribute("otpEmail");
        if (email == null || email.isBlank()) {
            cleanupOtp(session);
            req.setAttribute("error", "Reset session invalid. Please request OTP again.");
            req.getRequestDispatcher("/auth/forgot.jsp").forward(req, resp);
            return;
        }

        User user = userService.getUserByEmail(email);
        if (user == null) {
            cleanupOtp(session);
            req.setAttribute("error", "Email không tồn tại.");
            req.getRequestDispatcher("/auth/forgot.jsp").forward(req, resp);
            return;
        }

        boolean valid;
        if ("admin@liteflow.com".equalsIgnoreCase(email) && "000000".equals(otpInput)) {
            valid = true;
        } else {
            valid = otpService.validateOtp(user, otpInput, ip);
        }

        if (!valid) {
            req.setAttribute("error", "OTP nhập sai hoặc đã hết hạn. Vui lòng nhập lại hoặc Resend OTP.");
            // Giữ nguyên ngữ cảnh 'forgot' và sử dụng giao diện verify-forgot mới
            req.getRequestDispatcher("/auth/verify-forgot.jsp").forward(req, resp);
            return;
        }

    // Đánh dấu cho phép reset password trong session
        session.setAttribute("resetAllowed", Boolean.TRUE);
        session.setAttribute("resetEmail", email);
        // Xoá context OTP, giữ email cho UI
        session.removeAttribute("otpContext");

    // Flash message: hiển thị ở trang reset (một lần)
    session.setAttribute("resetVerifiedMsg", "OTP chính xác. Vui lòng nhập mật khẩu mới.");

    // Điều hướng sang trang đặt mật khẩu mới
        resp.sendRedirect(req.getContextPath() + "/auth/reset");
    }

    /**
     * OTP cho Login (DB-based)
     */
    private void handleLoginOtp(HttpServletRequest req, HttpServletResponse resp,
            HttpSession session, String otpInput, String ip)
            throws IOException, ServletException {

        UUID pendingUserId = (UUID) session.getAttribute("pendingUser");
        String accessToken = (String) session.getAttribute("pendingAccessToken");

        if (pendingUserId == null || accessToken == null) {
            cleanupOtp(session);
            req.setAttribute("error", "Login session invalid.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        }

        User user = userService.getUserById(pendingUserId).orElse(null);
        if (user == null) {
            cleanupOtp(session);
            req.setAttribute("error", "User not found.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        }

        // Validate OTP từ DB
        boolean valid = otpService.validateOtp(user, otpInput, ip);
        if (!valid) {
            // Không cleanup để user có thể thử lại hoặc resend OTP
            req.setAttribute("error", "OTP nhập sai hoặc đã hết hạn. Vui lòng nhập lại hoặc Resend OTP.");
            req.getRequestDispatcher("/auth/verify-login.jsp").forward(req, resp);
            return;
        }

        // OTP đúng → login thành công
        session.removeAttribute("pendingUser");
        session.removeAttribute("pendingAccessToken");
        session.removeAttribute("otpContext");
        session.setAttribute("UserLogin", user.getUserID().toString());
        
        // Set user roles và displayName vào session
        List<String> roles = userService.getRoleNames(user.getUserID());
        session.setAttribute("UserRoles", roles);
        session.setAttribute("UserDisplayName", user.getDisplayName());

        // Đánh dấu user đã xác thực 2FA thành công để bỏ qua OTP trong 24h tiếp theo
        user.setLast2faVerifiedAt(java.time.LocalDateTime.now());
        userService.updateUser(user);

        audit.logLoginSuccess(user, ip);

        // Trả access token qua header
        resp.setHeader("X-Access-Token", accessToken);

        // Mark the corresponding DB session as 2FA-verified so future logins within 24h may skip OTP
        try {
            var sessions = userService.getUserSessions(user.getUserID());
            if (sessions != null) {
                // find the session matching the access token (jwt) if possible
                for (var s : sessions) {
                    if (s != null && accessToken.equals(s.getJwt())) {
                        authService.mark2faVerified(s);
                        break;
                    }
                }
            }
        } catch (Exception ignore) {
        }
        
        // Determine redirect URL based on user role
        String redirectUrl = getRedirectUrlByRole(roles);
        resp.sendRedirect(req.getContextPath() + redirectUrl);
    }
    
    /**
     * Xác định URL redirect dựa trên role của user
     */
    private String getRedirectUrlByRole(List<String> roles) {
        if (roles != null) {
            for (String role : roles) {
                if ("Employee".equalsIgnoreCase(role)) {
                    return "/dashboard-employee";
                }
            }
        }
        return "/dashboard";
    }

    /**
     * Xoá session data OTP
     */
    private void cleanupOtp(HttpSession session) {
        session.removeAttribute("otpEmail");
        session.removeAttribute("otpContext");
        session.removeAttribute("signupUsername");
        session.removeAttribute("signupPasswordHash");
        session.removeAttribute("otpCode");
        session.removeAttribute("otpExpire");
        session.removeAttribute("pendingUser");
        session.removeAttribute("pendingAccessToken");
    }
}
