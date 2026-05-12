package com.liteflow.security;

import com.liteflow.model.auth.User;
import com.liteflow.model.auth.UserSession;
import com.liteflow.service.auth.AuditService;
import com.liteflow.service.auth.OtpService;
import com.liteflow.service.auth.UserService;
import jakarta.servlet.http.Cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AuthControllerHelper: - Login: kiểm mật khẩu → xem có cần OTP không → nếu cần
 * thì phát OTP và báo NEED_OTP - Verify OTP ở servlet riêng (VerifyOtpServlet)
 * → thành công thì tạo session + JWT - Signup: tạo user, audit - Refresh:
 * revoke session cũ, tạo session mới
 *
 * KHÔNG sử dụng session fallback trong helper này (đã do AuthenticationFilter
 * quản).
 */
public class AuthControllerHelper {

    public enum LoginResult {
        SUCCESS, NEED_OTP, INVALID_CREDENTIALS, LOCKED
    }

    private final UserService userService = new UserService();
    private final OtpService otpService = new OtpService();
    private final AuditService audit = new AuditService();

//    /**
//     * Bước 1: Password login. - Nếu sai mật khẩu → INVALID_CREDENTIALS - Nếu
//     * cần OTP (first time hoặc >24h) → phát OTP và trả NEED_OTP - Nếu không cần
//     * OTP → tạo session và SUCCESS
//     */
    public LoginResult handlePasswordLogin(User user,
            String rawPassword,
            HttpServletRequest req,
            HttpServletResponse resp) {
        String ip = AuthUtils.extractClientIp(req);

        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            audit.logLoginFail(user != null ? user.getEmail() : "unknown", ip);
            return LoginResult.INVALID_CREDENTIALS;
        }

        if (!AuthUtils.verifyPassword(rawPassword, user.getPasswordHash())) {
            audit.logLoginFail(user.getEmail(), ip);
            return LoginResult.INVALID_CREDENTIALS;
        }

        boolean needOtp = isOtpRequired(user);
        if (needOtp) {
            otpService.issueOtp(user, ip); // Admin dev => "000000", user thường => random email
            // Controller sẽ chuyển người dùng sang /verify-otp
            return LoginResult.NEED_OTP;
        }

        // Không cần OTP → tạo session & cookie
        createSessionAndCookie(user, req, resp, ip, /*mark2fa*/ true);
        return LoginResult.SUCCESS;
    }

//    /**
//     * Sau khi VerifyOtpServlet xác thực thành công sẽ gọi helper này để hoàn
//     * tất login.
//     */
    public void completeOtpLogin(User user, HttpServletRequest req, HttpServletResponse resp) {
        String ip = AuthUtils.extractClientIp(req);
        createSessionAndCookie(user, req, resp, ip, /*mark2fa*/ true);
    }
//
//    /**
//     * Signup user mới (đã verify OTP ở servlet).
//     */

    public boolean handleSignup(User user, String rawPassword, String ip) {
        user.setPasswordHash(AuthUtils.hashPassword(rawPassword));
        boolean ok = userService.createUser(user);
        if (ok) {
            audit.log(user, AuditService.AuditAction.CREATE,
                    AuditService.ObjectType.USER,
                    user.getUserID().toString(),
                    "User signup", ip);
        }
        return ok;
    }

//    /**
//     * Refresh token: thu hồi token cũ, cấp token mới.
//     */
    public String handleRefresh(User user, UserSession oldSession,
            HttpServletResponse resp, String ip) {
        if (user == null || oldSession == null || oldSession.isRevoked()) {
            audit.logDenied(user, "token-refresh", ip);
            return null;
        }
        String newJwt = AuthUtils.generateJwt(user);

        // revoke cũ + tạo mới
        userService.revokeSession(oldSession.getSessionId());
        userService.createSession(
                user, newJwt,
                oldSession.getDeviceInfo(),
                ip,
                LocalDateTime.now().plusMinutes(15));

        AuthUtils.setJwtCookie(resp, newJwt, 15 * 60, false);

        audit.log(user, AuditService.AuditAction.TOKEN_REFRESH,
                AuditService.ObjectType.USER,
                user.getUserID().toString(),
                "Token refreshed", ip);

        return newJwt;
    }

    // ===== Internals =====
    /**
     * OTP yêu cầu lần đầu hoặc khi lần xác thực OTP gần nhất > 24 giờ.
     */
    private boolean isOtpRequired(User user) {
        if (user.getLast2faVerifiedAt() == null) {
            return true;
        }
        return user.getLast2faVerifiedAt().isBefore(LocalDateTime.now().minusHours(24));
    }

    private void createSessionAndCookie(User user,
            HttpServletRequest req,
            HttpServletResponse resp,
            String ip,
            boolean mark2fa) {
        String jwt = AuthUtils.generateJwt(user);
        String device = AuthUtils.extractDeviceInfo(req);

        UserSession s = userService.createSession(
                user, jwt, device, ip,
                LocalDateTime.now().plusMinutes(15));

        if (mark2fa) {
            s.setLast2faVerifiedAt(LocalDateTime.now());
            user.setLast2faVerifiedAt(LocalDateTime.now());
            userService.updateUser(user);
        }

        AuthUtils.setJwtCookie(resp, jwt, 15 * 60, false);
        audit.logLoginSuccess(user, ip);
    }

    public static void completeLogin(HttpServletRequest req, HttpServletResponse resp,
            User user, List<String> roles, String jwt, String ip)
            throws IOException {

        HttpSession session = req.getSession(true);
        session.setAttribute("UserLogin", user);
        session.setAttribute("UserRoles", roles);

        // ✅ set JWT cookie
        Cookie cookie = new Cookie("LITEFLOW_TOKEN", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ⚠ Prod => true
        cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        cookie.setMaxAge(900); // 15 min
        resp.addCookie(cookie);

        // ✅ audit
        new AuditService().logLoginSuccess(user, ip);
    }

}
