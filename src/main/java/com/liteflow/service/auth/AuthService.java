package com.liteflow.service.auth;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.auth.User;
import com.liteflow.model.auth.UserSession;
import com.liteflow.security.JwtUtil;
import com.liteflow.util.PasswordUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

/**
 * AuthService: - Quản lý login, logout, refresh token, 2FA. - Gom logic từ
 * LoginService → chuẩn workflow.
 */
public class AuthService {

    /**
     * TTL mặc định cho JWT (30 phút).
     */
    private static final long DEFAULT_TTL_SECONDS = 1800;

    private final GenericDAO<User, UUID> userDao = new GenericDAO<>(User.class, UUID.class);
    private final GenericDAO<UserSession, UUID> sessionDao = new GenericDAO<>(UserSession.class, UUID.class);
    private final UserService userService = new UserService();
    private final AuditService audit = new AuditService();
    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());
    // Enable dev plaintext admin login when this env var is set to true
    private static final boolean DEV_ADMIN_PLAINTEXT =
        Boolean.parseBoolean(System.getenv().getOrDefault("LITEFLOW_DEV_ADMIN_PLAINTEXT", "false"));

    /**
     * Tìm user theo email (case-insensitive).
     *
     * @param email email cần tìm
     * @return Optional<User>
     */
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
    // Use DAO helper which performs case-insensitive lookup; ensure trimmed lower-case param
    User u = userDao.findSingleByFieldIgnoreCase("email", email.trim());
        return Optional.ofNullable(u);
    }

    /**
     * Kiểm tra mật khẩu BCrypt.
     *
     * @param user user cần check
     * @param rawPassword mật khẩu plaintext
     * @return true nếu khớp, false nếu sai
     */
    public boolean checkPassword(User user, String rawPassword) {
        if (user == null) {
            LOG.warning("[AuthService] Login failed: user not found");
            return false;
        }
        if (!user.isActiveSafe()) {
            LOG.warning(() -> "[AuthService] User is inactive or locked: " + user.getEmail());
            return false;
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            LOG.warning("[AuthService] Password input is empty");
            return false;
        }

        String stored = user.getPasswordHash();
        if (stored == null || stored.isBlank()) {
            LOG.warning(() -> "[AuthService] ❌ User has no stored password hash for: " + user.getEmail());
            return false;
        }

        // Log some details to help debugging malformed hashes / mismatches
    try {
            String prefix = stored.length() >= 4 ? stored.substring(0, 4) : stored;
            LOG.info(() -> "[AuthService] Debug: checking password for " + user.getEmail()
                    + " | storedHashPrefix=" + prefix
                    + " | storedLen=" + stored.length());

            boolean match = PasswordUtil.check(rawPassword, stored);
            LOG.info(() -> "[AuthService] Debug: PasswordUtil.check returned=" + match + " for user=" + user.getEmail());
            if (!match) {
                LOG.warning(() -> "[AuthService] Password mismatch for user: " + user.getEmail());
            }
            return match;
        } catch (IllegalArgumentException e) {
            LOG.warning(() -> "[AuthService] ⚠️ Invalid bcrypt format for user " + user.getEmail() + ": " + e.getMessage());
            // Fallback: cho phép admin đăng nhập nếu mật khẩu trùng rõ ràng (dev mode flag must be enabled)
            if (DEV_ADMIN_PLAINTEXT && "admin@liteflow.com".equalsIgnoreCase(user.getEmail()) && "1".equals(rawPassword)) {
                LOG.info("[AuthService] ✅ DEV MODE: admin logged in with plaintext password");
                return true;
            }
            return false;
        }
    }

    /**
     * Đăng nhập: check user + password → tạo session + JWT.
     *
     * @param user user đã tìm thấy
     * @param rawPassword mật khẩu plaintext
     * @param deviceInfo thông tin thiết bị
     * @param ip địa chỉ IP
     * @return Optional chứa JWT nếu thành công
     */
    public Optional<String> login(User user, String rawPassword, String deviceInfo, String ip) {
    LOG.info(() -> "[AuthService] Attempting login for: " + (user != null ? user.getEmail() : "NULL"));

        if (user == null) {
            LOG.warning("[AuthService] ❌ Login failed: user not found");
            return Optional.empty();
        }

        if (!checkPassword(user, rawPassword)) {
            logLoginAttempt(user, false, ip);
            LOG.warning(() -> "[AuthService] ❌ Login failed for " + user.getEmail() + " — invalid password or/or inactive account");
            return Optional.empty();
        }

        // Sinh JWT
    List<String> roles = userService.getRoleNames(user.getUserID());
    String jwt = JwtUtil.issue(
        user.getUserID().toString(),
        Map.of("email", user.getEmail(), "displayName", user.getDisplayName()),
        new ArrayList<>(roles),
        DEFAULT_TTL_SECONDS
    );

        // Lưu session
        UserSession session = new UserSession();
        session.setUserId(user.getUserID());
        session.setJwt(jwt);
        session.setDeviceInfo(deviceInfo);
        session.setIpAddress(ip);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(DEFAULT_TTL_SECONDS));
        session.setRevoked(false);
        sessionDao.insert(session);

    LOG.info(() -> "[AuthService] ✅ Login successful for " + user.getEmail() + ", JWT issued");
        logLoginAttempt(user, true, ip);
        return Optional.of(jwt);
    }

    /**
     * Logout: revoke session (by JWT).
     *
     * @param jwt token
     * @param user user đang logout
     * @param ip địa chỉ IP
     * @return true nếu thành công
     */
    public boolean logout(String jwt, User user, String ip) {
        if (jwt == null || jwt.isBlank()) {
            return false;
        }

        UserSession s = sessionDao.findSingleByField("jwt", jwt);
        if (s != null) {
            s.setRevoked(true);
            sessionDao.update(s);
            audit.logLogout(user, ip);
            return true;
        }
        return false;
    }

    /**
     * Refresh token nếu session còn hợp lệ.
     *
     * @param oldJwt token cũ
     * @param ip địa chỉ IP
     * @return Optional chứa token mới nếu hợp lệ
     */
    public Optional<String> refreshToken(String oldJwt, String ip) {
        if (oldJwt == null || oldJwt.isBlank()) {
            return Optional.empty();
        }

        UserSession s = sessionDao.findSingleByField("jwt", oldJwt);
        if (s == null || s.isRevoked() || s.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        User user = userDao.findById(s.getUserId());
        if (user == null) {
            return Optional.empty();
        }

    // Sinh JWT mới
    List<String> roles = userService.getRoleNames(user.getUserID());
    String newJwt = JwtUtil.issue(
        user.getUserID().toString(),
        Map.of("email", user.getEmail(), "displayName", user.getDisplayName()),
        new ArrayList<>(roles),
        DEFAULT_TTL_SECONDS
    );

        // Cập nhật session cũ
        s.setJwt(newJwt);
        s.setExpiresAt(LocalDateTime.now().plusSeconds(DEFAULT_TTL_SECONDS));
        sessionDao.update(s);

        audit.log(user,
                AuditService.AuditAction.TOKEN_REFRESH,
                AuditService.ObjectType.USER,
                user.getUserID().toString(),
                "User token refreshed",
                ip);

        return Optional.of(newJwt);
    }

    /**
     * Kiểm tra có cần yêu cầu nhập 2FA không.
     *
     * @param user user
     * @param activeSession session hiện tại
     * @return true nếu cần 2FA
     */
    public boolean is2faRequired(User user, UserSession activeSession) {
        if (user == null) {
            return true;
        }

        // If we have an active session, decide based on its 2FA verification timestamp.
        // Rules:
        // - If session is revoked => require 2FA
        // - If session has a last2faVerifiedAt timestamp and it is within 24 hours => skip 2FA
        // - Otherwise require 2FA
        if (activeSession != null) {
            if (activeSession.isRevoked()) {
                return true;
            }

            if (activeSession.getLast2faVerifiedAt() != null) {
                long hours = ChronoUnit.HOURS.between(
                        activeSession.getLast2faVerifiedAt(), LocalDateTime.now());
                return hours > 24; // require 2FA only if last verified more than 24h ago
            }

            // No record of a successful 2FA on this session -> require 2FA
            return true;
        }

        // No active session found -> require 2FA
        // Nếu không có session, có thể kiểm tra Last2FAVerifiedAt trên user (nếu có)
        if (user.getLast2faVerifiedAt() != null) {
            long hours = ChronoUnit.HOURS.between(user.getLast2faVerifiedAt(), LocalDateTime.now());
            return hours > 24;
        }
        return true;
    }

    /**
     * Đánh dấu user đã xác thực 2FA thành công.
     *
     * @param session session cần update
     */
    public void mark2faVerified(UserSession session) {
        if (session != null) {
            session.setLast2faVerifiedAt(LocalDateTime.now());
            sessionDao.update(session);
        }
    }

    /**
     * Ghi log login attempt.
     *
     * @param user user
     * @param success true nếu login thành công
     * @param ip địa chỉ IP
     */
    public void logLoginAttempt(User user, boolean success, String ip) {
        if (success) {
            audit.logLoginSuccess(user, ip);
        } else {
            audit.logLoginFail(user != null ? user.getEmail() : "UNKNOWN", ip);
        }
    }
}
