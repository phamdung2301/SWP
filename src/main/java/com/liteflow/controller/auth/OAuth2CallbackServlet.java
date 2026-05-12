package com.liteflow.controller.auth;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import com.liteflow.model.auth.Role;
import com.liteflow.model.auth.User;
import com.liteflow.service.auth.UserService;
import com.liteflow.service.auth.RoleService;
import com.liteflow.service.auth.AuditService;
import com.liteflow.security.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@WebServlet("/oauth2callback")
public class OAuth2CallbackServlet extends HttpServlet {

    private String clientId;
    private String clientSecret;
    private String redirectUri;

    private final UserService userService = new UserService();
    private final RoleService roleService = new RoleService();
    private final AuditService audit = new AuditService();

    private static final long ACCESS_TTL = 30 * 60;        // 30 phút
    private static final long REFRESH_TTL = 7 * 24 * 3600; // 7 ngày

    @Override
    public void init() throws ServletException {
        clientId = getServletContext().getInitParameter("google.clientId");
        clientSecret = getServletContext().getInitParameter("google.clientSecret");
        redirectUri = getServletContext().getInitParameter("google.redirectUri");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String code = req.getParameter("code");
        String state = req.getParameter("state");

        // Check CSRF state
        String stateSess = (String) req.getSession().getAttribute("oauth2_state");
        if (stateSess == null || !stateSess.equals(state)) {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp?error=oauth_state");
            return;
        }

        if (code == null || code.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp?error=oauth2_code");
            return;
        }

        try {
            TokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    clientSecret,
                    code,
                    redirectUri
            ).execute();

            Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setClientAuthentication(
                            new com.google.api.client.auth.oauth2.ClientParametersAuthentication(
                                    clientId, clientSecret
                            ))
                    .setTokenServerEncodedUrl("https://oauth2.googleapis.com/token")
                    .build()
                    .setFromTokenResponse(tokenResponse);

            Oauth2 oauth2 = new Oauth2.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("LiteFlow").build();

            Userinfo userInfo = oauth2.userinfo().get().execute();
            if (!Boolean.TRUE.equals(userInfo.getVerifiedEmail())) {
                resp.sendRedirect(req.getContextPath() + "/auth/login.jsp?error=email_unverified");
                return;
            }

            String email = userInfo.getEmail();
            String name = userInfo.getName();
            String googleId = userInfo.getId();

            // Tìm hoặc tạo user

                        User u = userService.findByEmail(email);
                        boolean isNew = false;
                                        if (u == null) {
                                                u = new User();
                                                u.setUserID(UUID.randomUUID());
                                                u.setEmail(email);
                                                u.setGoogleID(googleId);
                                                u.setDisplayName(name);
                                                u.setIsActive(true);
                                                // Google SSO không dùng password local, nhưng DB bắt buộc phải có
                                                // Sinh chuỗi random 60 ký tự làm passwordHash
                                                String randomHash = java.util.UUID.randomUUID().toString().replaceAll("-","")
                                                                + java.util.UUID.randomUUID().toString().replaceAll("-","");
                                                u.setPasswordHash(randomHash.substring(0, 60));
                                                boolean created = userService.createUser(u);
                                                if (!created) {
                                                        // Không tạo được user, không gán role, báo lỗi rõ ràng
                                                        resp.sendRedirect(req.getContextPath() + "/auth/login.jsp?error=google_signup_failed");
                                                        return;
                                                }
                                                isNew = true;

                                                UUID employeeRoleId = roleService.getRoleIdByName("Employee");
                                                if (employeeRoleId != null) {
                                                        roleService.assignRole(u.getUserID(), employeeRoleId, null);
                                                }

                                                audit.log(u, AuditService.AuditAction.CREATE,
                                                                AuditService.ObjectType.USER,
                                                                u.getUserID().toString(),
                                                                "User created via Google SSO",
                                                                req.getRemoteAddr());
                                        }

            List<String> roles = roleService.getUserRoles(u.getUserID())
                    .stream().map(Role::getName).toList();

            // Access token
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", u.getEmail());
            claims.put("displayName", u.getDisplayName());
            claims.put("roles", roles);
            claims.put("sso", true);

            String accessToken = JwtUtil.issue(
                    u.getUserID().toString(),
                    claims,
                    roles,
                    ACCESS_TTL
            );

            // Refresh token
            Map<String, Object> refreshClaims = new HashMap<>();
            refreshClaims.put("type", "refresh");
            refreshClaims.put("uid", u.getUserID().toString());

            String refreshToken = JwtUtil.issue(
                    UUID.randomUUID().toString(),
                    refreshClaims,
                    roles,
                    REFRESH_TTL
            );

            userService.createSession(
                    u,
                    refreshToken,
                    req.getHeader("User-Agent"),
                    req.getRemoteAddr(),
                    java.time.LocalDateTime.now().plusSeconds(REFRESH_TTL)
            );

            boolean secureFlag = Boolean.parseBoolean(
                    System.getenv().getOrDefault("LITEFLOW_COOKIE_SECURE", "false")
            );
            String ctxPath = req.getContextPath();
            setHttpOnlyCookie(resp, "LITEFLOW_REFRESH", refreshToken,
                    (int) REFRESH_TTL,
                    (ctxPath == null || ctxPath.isBlank()) ? "/" : ctxPath,
                    secureFlag);

            // Save userID vào session
            req.getSession(true).setAttribute("UserLogin", u.getUserID().toString());

            audit.log(u, AuditService.AuditAction.LOGIN_SUCCESS,
                    AuditService.ObjectType.USER,
                    u.getUserID().toString(),
                    isNew ? "Google SSO signup+login" : "Google SSO login",
                    req.getRemoteAddr());

            // Access token trả về header
            resp.setHeader("X-Access-Token", accessToken);

            // Determine redirect URL based on user role
            String redirectUrl = getRedirectUrlByRole(roles);
            resp.sendRedirect(req.getContextPath() + redirectUrl);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp?error=oauth2_general");
        }
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

    private void setHttpOnlyCookie(HttpServletResponse resp, String name, String value,
            int maxAge, String path, boolean secure) {
        String cookie = name + "=" + value
                + "; Max-Age=" + maxAge
                + "; Path=" + (path == null ? "/" : path)
                + "; HttpOnly"
                + (secure ? "; Secure" : "")
                + "; SameSite=Strict";
        resp.addHeader("Set-Cookie", cookie);
    }
}
