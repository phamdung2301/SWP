package com.liteflow.controller.auth;

import com.liteflow.model.auth.User;
import com.liteflow.security.JwtUtil;
import com.liteflow.service.auth.AuditService;
import com.liteflow.service.auth.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = {"/auth/refresh"})
public class RefreshServlet extends HttpServlet {

    private final AuditService audit = new AuditService();
    private final UserService userService = new UserService();

    private static final long ACCESS_TTL = 30 * 60;        // 30 phút
    private static final long REFRESH_TTL = 7 * 24 * 3600; // 7 ngày

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String ip = req.getRemoteAddr();

        // 1. Lấy refresh token từ header/cookie
        String refreshToken = extractRefreshToken(req);
        if (refreshToken == null || refreshToken.isBlank()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing refresh token");
            return;
        }

        try {
            // 2. Parse refresh token
            Jws<Claims> parsed = JwtUtil.parse(refreshToken);
            Claims body = parsed.getBody();

            if (body.getExpiration() == null || body.getExpiration().before(new Date())) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Refresh token expired");
                return;
            }

            if (!"refresh".equals(body.get("type"))) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token type");
                return;
            }

            String userId = body.get("uid", String.class);
            if (userId == null) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token payload");
                return;
            }

            User user = userService.getUserById(UUID.fromString(userId)).orElse(null);
            if (user == null) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }

            // 3. Lấy roles
            List<String> roles = userService.getRoleNames(user.getUserID());
            if (roles == null) {
                roles = Collections.emptyList();
            }

            // 4. Tạo access token mới
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", user.getEmail());
            claims.put("displayName", user.getDisplayName());
            claims.put("roles", roles);
            claims.put("jti", UUID.randomUUID().toString());

            String newAccessToken = JwtUtil.issue(
                    user.getUserID().toString(),
                    claims,
                    roles,
                    ACCESS_TTL
            );

            // 5. Rotate refresh token
            Map<String, Object> refreshClaims = new HashMap<>();
            refreshClaims.put("type", "refresh");
            refreshClaims.put("uid", user.getUserID().toString());

            String newRefreshToken = JwtUtil.issue(
                    UUID.randomUUID().toString(),
                    refreshClaims,
                    roles,
                    REFRESH_TTL
            );

            // Revoke token cũ + lưu session mới
            userService.revokeByToken(refreshToken);
            userService.createSession(
                    user,
                    newRefreshToken,
                    req.getHeader("User-Agent"),
                    ip,
                    java.time.LocalDateTime.now().plusSeconds(REFRESH_TTL)
            );

            // 6. Set cookie refresh mới
            boolean secure = Boolean.parseBoolean(System.getenv().getOrDefault("LITEFLOW_COOKIE_SECURE", "false"));
            String ctxPath = req.getContextPath();
            setHttpOnlyCookie(resp, "LITEFLOW_REFRESH", newRefreshToken,
                    (int) REFRESH_TTL,
                    (ctxPath == null || ctxPath.isBlank()) ? "/" : ctxPath,
                    secure);

            // 7. Audit
            audit.log(user,
                    AuditService.AuditAction.TOKEN_REFRESH,
                    AuditService.ObjectType.USER,
                    userId,
                    "Refresh rotated (roles=" + roles + ")",
                    ip);

            // 8. Trả JSON: cả access + refresh
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().printf(
                    "{\"accessToken\":\"%s\",\"refreshToken\":\"%s\"}",
                    newAccessToken, newRefreshToken
            );

        } catch (JwtException e) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token");
        }
    }

    private String extractRefreshToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("LITEFLOW_REFRESH".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

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
}
