package com.liteflow.service.auth;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.auth.OtpToken;
import com.liteflow.model.auth.User;
import com.liteflow.security.TotpUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * OtpService: - Sinh OTP (TOTP-based hoặc random fallback). - Lưu OTP vào DB để
 * quản lý (TTL 5 phút, dùng 1 lần). - Xác thực OTP khi người dùng nhập. -
 * Cleanup OTP hết hạn. - Tích hợp AuditService để log.
 */
public class OtpService {

    private final GenericDAO<OtpToken, UUID> otpDao = new GenericDAO<>(OtpToken.class, UUID.class);
    private final AuditService audit = new AuditService();

    /**
     * Sinh OTP 6 số cho user, lưu vào DB với TTL 5 phút.
     *
     * @param user User cần sinh OTP
     * @param ip IP client
     * @return mã OTP dạng String
     */
    public String issueOtp(User user, String ip) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when issuing OTP");
        }

        // Invalidate any previous active (unused & not expired) OTPs for this user
        invalidateActiveOtpsForUser(user);

        // Ưu tiên TOTP dựa trên secret, fallback random nếu chưa có secret
        String otp = TotpUtil.generate(user.getTwoFactorSecret());
        if (otp == null) {
            otp = String.valueOf((int) (100000 + Math.random() * 900000));
        }

        OtpToken token = new OtpToken();
        token.setUser(user);
        token.setCode(otp);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);
        token.setIpAddress(ip);

        otpDao.insert(token);

        audit.logOtpIssued(user, ip);
        return otp;
    }

    /**
     * Sinh OTP 6 số cho một email (dùng cho signup khi user chưa tồn tại).
     * Lưu vào DB trong targetEmail (không cần user persisted).
     */
    public String issueOtpForEmail(String email, String ip) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null when issuing OTP for email");
        }

        String otp = String.format("%06d", (int) (100000 + Math.random() * 900000));

        // With authen.sql, OtpTokens.UserID is NOT NULL → we do NOT persist signup OTP to DB.
        // Signup OTP will be stored in session by the servlet; here we only return the code and log.
        audit.log(null,
                AuditService.AuditAction.OTP_ISSUED,
                AuditService.ObjectType.USER,
                email.trim().toLowerCase(),
                "OTP issued for signup (session-based)",
                ip);
        return otp;
    }

    /**
     * Xác thực OTP.
     *
     * @param user User cần xác thực
     * @param otp mã OTP do user nhập
     * @param ip IP client
     * @return true nếu xác thực thành công
     */
    public boolean validateOtp(User user, String otp, String ip) {
        if (user == null || otp == null || otp.isBlank()) {
            return false;
        }

        // Đặc biệt xử lý cho admin@liteflow.com với OTP cố định "000000"
        if ("admin@liteflow.com".equalsIgnoreCase(user.getEmail()) && "000000".equals(otp)) {
            audit.logOtpUsed(user, ip);
            return true;
        }

        var allTokens = otpDao.getAll();
        OtpToken token = allTokens.stream()
                .filter(t -> !t.isUsed())
                .filter(t -> t.getCode().equals(otp))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(t -> {
                    if (t.getUser() != null && user != null) {
                        return t.getUser().getUserID().equals(user.getUserID());
                    }
                    // allow matching by targetEmail when user object not present in token
                    if (t.getTargetEmail() != null && user.getEmail() != null) {
                        return t.getTargetEmail().equalsIgnoreCase(user.getEmail().trim());
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);

        if (token == null) {
            return false;
        }

        token.setUsed(true);
        otpDao.update(token);

        audit.logOtpUsed(user, ip);
        return true;
    }

    /**
     * Xoá tất cả OTP hết hạn (cleanup job).
     *
     * @return số OTP bị xoá
     */
    public int cleanupExpired() {
        int deleted = 0;
        var allTokens = otpDao.getAll();
        LocalDateTime now = LocalDateTime.now();
        for (OtpToken token : allTokens) {
            if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now)) {
                otpDao.delete(token.getOtpId());
                deleted++;
            }
        }
        return deleted;
    }

    /**
     * Lấy OTP mới nhất còn hiệu lực của user.
     *
     * @param userId UUID user
     * @return Optional<OtpToken>
     */
    public Optional<OtpToken> getLatestOtp(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }

        var tokens = otpDao.getAll();
        return tokens.stream()
                .filter(t -> t.getUser() != null && userId.equals(t.getUser().getUserID()))
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .max((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    }

    /**
     * Validate OTP that was issued for an email (signup flow).
     */
    public boolean validateOtpForEmail(String email, String otp, String ip) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return false;
        }

        var allTokens = otpDao.getAll();
        OtpToken token = allTokens.stream()
                .filter(t -> !t.isUsed())
                .filter(t -> t.getCode().equals(otp))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(t -> t.getTargetEmail() != null && t.getTargetEmail().equalsIgnoreCase(email.trim()))
                .findFirst()
                .orElse(null);

        if (token == null) return false;

        token.setUsed(true);
        otpDao.update(token);
        audit.logOtpUsed(null, ip);
        return true;
    }

    // Trong OtpService
    public String issueFixedOtp(User user, String fixedCode, String ip) {
        String code = (fixedCode != null && fixedCode.matches("\\d{6}")) ? fixedCode : "000000";
        // Invalidate any previous active OTPs for this user before issuing the fixed one
        if (user != null) {
            invalidateActiveOtpsForUser(user);
        }
        // tái sử dụng luồng persist như issueOtp(...)
        OtpToken token = new OtpToken();
        token.setUser(user);
        token.setCode(code);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);
        token.setIpAddress(ip);
        otpDao.insert(token);
        audit.logOtpIssued(user, ip);
        return code;
    }

    /**
     * Đánh dấu vô hiệu hoá (used=true) tất cả OTP còn hiệu lực cho user này.
     * Được gọi trước khi phát hành OTP mới để đảm bảo OTP cũ không còn dùng được.
     */
    private void invalidateActiveOtpsForUser(User user) {
        if (user == null || user.getUserID() == null) return;
        var allTokens = otpDao.getAll();
        LocalDateTime now = LocalDateTime.now();
        for (OtpToken t : allTokens) {
            if (t == null) continue;
            if (t.getUser() != null
                    && user.getUserID().equals(t.getUser().getUserID())
                    && !t.isUsed()
                    && t.getExpiresAt() != null
                    && t.getExpiresAt().isAfter(now)) {
                t.setUsed(true);
                otpDao.update(t);
            }
        }
    }

    
}
