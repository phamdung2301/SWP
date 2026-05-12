package com.liteflow.controller.auth;

import com.liteflow.model.auth.User;
import com.liteflow.service.auth.AuditService;
import com.liteflow.service.auth.AuthService;
import com.liteflow.service.auth.OtpService;
import com.liteflow.service.auth.UserService;
import com.liteflow.security.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.util.logging.Logger;

import java.io.IOException;
import java.util.*;

/**
 * LoginServlet: - Nhận email & password từ form - Xác thực user qua AuthService
 * - Nếu cần OTP => phát OTP (admin: 000000) - Nếu không cần OTP => đăng nhập
 * luôn
 */
@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();
    private final OtpService otpService = new OtpService();
    private final AuditService audit = new AuditService();

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());

    private static final long REFRESH_TTL = 7 * 24 * 3600; // 7 ngày

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("UserLogin") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // Đảm bảo session được tạo trước khi tạo CSRF token
        // Điều này đảm bảo session cookie được gửi về browser ngay từ đầu
        session = req.getSession(true);
        generateCsrf(req);
        req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ✅ CSRF Protection
        String csrfForm = req.getParameter("csrfToken");
        
        // Lấy session hiện có - tạo mới nếu chưa có (để đảm bảo session cookie được gửi về browser)
        // Nếu session không tồn tại, có thể do cookie chưa được gửi từ browser
        HttpSession session = req.getSession(false);
        String csrfSess = null;
        boolean sessionJustCreated = false;
        
        if (session != null) {
            csrfSess = (String) session.getAttribute("csrfToken");
        } else {
            // Session không tồn tại - có thể do cookie chưa được gửi từ browser
            // Tạo session mới để đảm bảo cookie được gửi về browser
            session = req.getSession(true);
            sessionJustCreated = true;
            LOG.info("[LoginServlet] Session not found in POST request, created new session");
        }
        
        // Kiểm tra CSRF token: phải có cả token từ form và từ session, và chúng phải khớp
        if (csrfForm == null || csrfForm.isBlank()) {
            LOG.warning("[LoginServlet] CSRF token missing from form");
            generateCsrf(req);
            req.setAttribute("error", "Invalid request. Please refresh and try again.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        }
        
        // Nếu session vừa được tạo hoặc không có CSRF token trong session
        if (csrfSess == null || csrfSess.isBlank()) {
            if (sessionJustCreated) {
                // Session vừa được tạo - đây có thể là lần submit đầu tiên khi cookie chưa được gửi
                // Tạo CSRF token mới và lưu vào session, sau đó yêu cầu user refresh
                LOG.info("[LoginServlet] New session created, generating CSRF token and requesting refresh");
                generateCsrf(req);
                req.setAttribute("error", "Please refresh the page and try again.");
                req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
                return;
            } else {
                // Session đã tồn tại nhưng không có CSRF token - tạo token mới
                LOG.warning("[LoginServlet] Session exists but CSRF token missing");
                generateCsrf(req);
                req.setAttribute("error", "Session expired. Please refresh and try again.");
                req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
                return;
            }
        }
        
        // Kiểm tra CSRF token có khớp không
        if (!csrfSess.equals(csrfForm)) {
            LOG.warning("[LoginServlet] CSRF token mismatch");
            generateCsrf(req);
            req.setAttribute("error", "Invalid request. Please refresh and try again.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        }
        
        // CSRF token hợp lệ, tiếp tục xử lý login

        String email = req.getParameter("id"); // field name=id trong form
        String password = req.getParameter("password");
        // Normalize email early: trim + lowercase to ensure consistent lookup
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        String ip = req.getRemoteAddr();

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            generateCsrf(req);
            req.setAttribute("error", "Email và mật khẩu không được để trống.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        }

        User user = null;
        try {
            user = userService.getUserByEmail(email);
            if (user == null) {
                LOG.warning("[LoginServlet] Login failed — user not found for email=" + email);
                generateCsrf(req);
                req.setAttribute("error", "Email không tồn tại hoặc chưa được đăng ký.");
                req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
                return;
            }

            if (!authService.checkPassword(user, password)) {
                LOG.warning("[LoginServlet] Login failed — password check failed for user=" + user.getEmail());
                generateCsrf(req);
                req.setAttribute("error", "Mật khẩu không đúng.");
                req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
                return;
            }

        } catch (org.hibernate.LazyInitializationException lie) {
            LOG.severe("[LoginServlet] LazyInitializationException while loading user roles: " + lie.getMessage());
            generateCsrf(req);
            req.setAttribute("error", "Lỗi hệ thống nội bộ (auth roles). Vui lòng thử lại sau.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        } catch (Exception ex) {
            LOG.severe("[LoginServlet] Unexpected error during authentication: " + ex.getMessage());
            generateCsrf(req);
            req.setAttribute("error", "Lỗi hệ thống. Vui lòng thử lại sau.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        }

        // ✅ Tạo access token qua AuthService (đồng thời tạo session DB)
        Optional<String> accessTokenOpt;
        try {
            accessTokenOpt = authService.login(user, password, req.getHeader("User-Agent"), ip);
        } catch (org.hibernate.LazyInitializationException lie) {
            LOG.severe("[LoginServlet] LazyInitializationException in AuthService.login: " + lie.getMessage());
            generateCsrf(req);
            req.setAttribute("error", "Lỗi hệ thống nội bộ (auth roles). Vui lòng thử lại sau.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        } catch (Exception ex) {
            LOG.severe("[LoginServlet] Error in AuthService.login: " + ex.getMessage());
            generateCsrf(req);
            req.setAttribute("error", "Lỗi hệ thống. Vui lòng thử lại sau.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        }
        if (accessTokenOpt.isEmpty()) {
            generateCsrf(req);
            req.setAttribute("error", "Đăng nhập thất bại. Vui lòng kiểm tra email/mật khẩu.");
            req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
            return;
        }
        String accessToken = accessTokenOpt.get();

    // Check remember-me from form
    boolean remember = "on".equalsIgnoreCase(req.getParameter("remember"));

        // ✅ Kiểm tra OTP (admin dùng mã cố định 000000)
        // Try to find an active UserSession (by user) to determine if 2FA can be skipped
        com.liteflow.model.auth.UserSession activeSession = null;
        try {
            var sessions = userService.getUserSessions(user.getUserID());
            if (sessions != null && !sessions.isEmpty()) {
                // pick the most recent non-revoked session
                activeSession = sessions.stream()
                        .filter(s -> s != null && !s.isRevoked())
                        .max((a, b) -> a.getExpiresAt().compareTo(b.getExpiresAt()))
                        .orElse(null);
            }
        } catch (Exception ignore) {
        }

        boolean needOtp = authService.is2faRequired(user, activeSession);

        // Bỏ qua OTP cho TẤT CẢ TÀI KHOẢN để tiện test ở môi trường dev (như yêu cầu của bạn)
        needOtp = false;

        // Xử lý OTP theo từng loại user (đã bị vô hiệu hóa vì needOtp = false ở trên)
        if ("admin@liteflow.com".equalsIgnoreCase(user.getEmail())) {
            // Vẫn cho phép admin nhận luồng OTP để test form giao diện OTP nếu muốn,
            // hoặc bỏ qua bằng cách ẩn dòng needOtp = true; dưới đây.
            // Để bỏ qua hoàn toàn, mình comment lại:
            // needOtp = true; 
            // otpService.issueFixedOtp(user, "000000", ip);
            // LOG.info("Admin OTP issued: 000000");
        } else if (needOtp) {
            // User thường: Sinh OTP ngẫu nhiên và gửi qua email
            String otp = otpService.issueOtp(user, ip);
            // Attempt to email the OTP to the user (best-effort)
            try {
                com.liteflow.util.MailUtil.sendOtpMail(user.getEmail(), otp);
                LOG.info("OTP email sent to: " + user.getEmail());
            } catch (Exception e) {
                LOG.warning("Failed to send OTP email to " + user.getEmail() + ": " + e.getMessage());
            }
        }

        if (needOtp) {
            // Sử dụng lại session đã được khai báo ở đầu method, đảm bảo session tồn tại
            if (session == null) {
                session = req.getSession(true);
            }
            session.setAttribute("pendingUser", user.getUserID());
            session.setAttribute("pendingAccessToken", accessToken);
            // Mark OTP context so VerifyOtpServlet knows this is a login OTP
            session.setAttribute("otpContext", "login");
            // Save email for resend in verify-login.jsp
            session.setAttribute("otpEmail", user.getEmail());

            audit.logOtpIssued(user, ip);
            resp.sendRedirect(req.getContextPath() + "/auth/verify");
            return;
        }

        // ✅ Không cần OTP → hoàn tất đăng nhập
        completeLogin(req, resp, user, accessToken, ip, remember);
    }

    private void completeLogin(HttpServletRequest req, HttpServletResponse resp, User user,
            String accessToken, String ip, boolean remember) throws IOException {

        HttpSession session = req.getSession(true);
        session.setAttribute("UserLogin", user.getUserID().toString());
        if (remember) {
            session.setAttribute("rememberedAt", java.time.Instant.now().toString());
        }

        // Refresh token (JWT dạng refresh)
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("uid", user.getUserID().toString());

        List<String> roles = userService.getRoleNames(user.getUserID());
        session.setAttribute("UserRoles", roles);
        session.setAttribute("UserDisplayName", user.getDisplayName());
        String refreshToken = JwtUtil.issue(
                UUID.randomUUID().toString(),
                claims,
                roles,
                REFRESH_TTL
        );

        // Lưu refresh token vào DB (UserSession)
        userService.createSession(
                user,
                refreshToken,
                req.getHeader("User-Agent"),
                ip,
                java.time.LocalDateTime.now().plusSeconds(REFRESH_TTL)
        );

        // Cookie HttpOnly
        setHttpOnlyCookie(resp, "LITEFLOW_REFRESH", refreshToken,
                (int) REFRESH_TTL,
                req.getContextPath().isBlank() ? "/" : req.getContextPath(),
                Boolean.parseBoolean(System.getenv().getOrDefault("LITEFLOW_COOKIE_SECURE", "false"))
        );

    // Remember-me cookie: chỉ lưu email (dùng để autofill trên client), KHÔNG tự động restore session trên server
    if (remember) {
        String rememberValue = user.getEmail();
        int rememberSeconds = 24 * 3600;
        String cookie = "LITEFLOW_REMEMBER=" + java.net.URLEncoder.encode(rememberValue, java.nio.charset.StandardCharsets.UTF_8)
            + "; Max-Age=" + rememberSeconds
            + "; Path=" + (req.getContextPath().isBlank() ? "/" : req.getContextPath())
            // Không dùng HttpOnly để JavaScript có thể đọc được cookie này
            + (Boolean.parseBoolean(System.getenv().getOrDefault("LITEFLOW_COOKIE_SECURE", "false")) ? "; Secure" : "")
            + "; SameSite=Strict";
        resp.addHeader("Set-Cookie", cookie);
    } else {
        // Xóa cookie remember me nếu user không bật remember me
        clearCookie(resp, "LITEFLOW_REMEMBER", req.getContextPath());
    }

        // Access token trả qua header
        resp.setHeader("X-Access-Token", accessToken);

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
     * Sinh CSRF token mới
     */
    private void generateCsrf(HttpServletRequest req) {
        String csrfToken = UUID.randomUUID().toString();
        req.getSession(true).setAttribute("csrfToken", csrfToken);
        req.setAttribute("csrfToken", csrfToken);
    }

    /**
     * Set cookie HttpOnly + SameSite
     */
    private void setHttpOnlyCookie(HttpServletResponse resp,
            String name,
            String value,
            int maxAge,
            String path,
            boolean secure) {
        String cookie = name + "=" + value
                + "; Max-Age=" + maxAge
                + "; Path=" + (path == null ? "/" : path)
                + "; HttpOnly"
                + (secure ? "; Secure" : "")
                + "; SameSite=Strict";
        resp.addHeader("Set-Cookie", cookie);
    }

    /**
     * Clear cookie
     */
    private void clearCookie(HttpServletResponse resp, String name, String contextPath) {
        Cookie c = new Cookie(name, "");
        c.setMaxAge(0);
        c.setPath(contextPath == null || contextPath.isEmpty() ? "/" : contextPath);
        c.setHttpOnly(true);
        c.setSecure(Boolean.parseBoolean(System.getenv().getOrDefault("LITEFLOW_COOKIE_SECURE", "false")));
        resp.addCookie(c);
    }
}
