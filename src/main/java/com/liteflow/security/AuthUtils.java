package com.liteflow.security;

import com.liteflow.model.auth.Role;
import com.liteflow.model.auth.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AuthUtils: - Hash / verify password (BCrypt, chuẩn hoá $2y/$2b -> $2a cho
 * jBCrypt) - Sinh JWT qua JwtUtil - Tiện ích cookie JWT, device info, email
 * normalize
 */
public final class AuthUtils {

    private static final int BCRYPT_ROUNDS = 10;
    private static final int ACCESS_TTL_SECONDS = Integer.parseInt(
            System.getProperty("LITEFLOW_JWT_TTL_SECONDS",
                    System.getenv().getOrDefault("LITEFLOW_JWT_TTL_SECONDS", "900")) // mặc định 15 phút
    );

    private AuthUtils() {
    }

    // === Password ===
    public static String hashPassword(String raw) {
        if (raw == null) {
            return null;
        }
        return BCrypt.hashpw(raw, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public static boolean verifyPassword(String raw, String hashed) {
        if (raw == null || hashed == null) {
            return false;
        }
        return BCrypt.checkpw(raw, normalizeBcryptHash(hashed));
    }

//    /**
//     * Chuẩn hoá prefix để jBCrypt đọc được mọi biến thể.
//     */
    public static String normalizeBcryptHash(String hash) {
        if (hash == null) {
            return null;
        }
        if (hash.startsWith("$2y$")) {
            return "$2a$" + hash.substring(4);
        }
        if (hash.startsWith("$2b$")) {
            return "$2a$" + hash.substring(4);
        }
        return hash;
    }

    // === JWT ===
    public static String generateJwt(User user) {
        if (user == null) {
            return null;
        }

        List<String> roles = user.getActiveRoles().stream()
                .map(Role::getName)
                .toList();

        Map<String, Object> claims = Map.of(
                "email", user.getEmail(),
                "displayName", user.getDisplayName()
        );

        return JwtUtil.generateToken(
                user.getUserID().toString(),
                roles,
                ACCESS_TTL_SECONDS,
                claims
        );
    }

    // === Cookie / Device ===
    public static void setJwtCookie(HttpServletResponse resp, String jwt, int maxAgeSec, boolean secure) {
        // Cookie API không có SameSite, thêm thêm header để chắc chắn
        Cookie ck = new Cookie("LITEFLOW_TOKEN", jwt);
        ck.setHttpOnly(true);
        ck.setSecure(secure);
        ck.setPath("/");
        ck.setMaxAge(maxAgeSec);
        resp.addCookie(ck);

        // Best effort SameSite=Lax
        resp.addHeader("Set-Cookie",
                "LITEFLOW_TOKEN=" + jwt
                + "; Path=/; HttpOnly"
                + (secure ? "; Secure" : "")
                + "; SameSite=Lax; Max-Age=" + maxAgeSec);
    }

    public static String extractDeviceInfo(HttpServletRequest req) {
        String ua = req.getHeader("User-Agent");
        String ip = extractClientIp(req);
        return String.format("UA=%s | IP=%s", ua, ip);
    }

    public static String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xrip = req.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) {
            return xrip.trim();
        }
        return req.getRemoteAddr();
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
