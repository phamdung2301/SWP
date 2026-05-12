package com.liteflow.controller.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * LoginGoogleServlet: - Bước 1: Redirect người dùng sang Google OAuth consent
 * screen - Sinh state chống CSRF, lưu session - Chuyển hướng đến Google để user
 * consent → redirect về /oauth2callback
 */
@WebServlet("/auth/google")
public class LoginGoogleServlet extends HttpServlet {

    private String clientId;
    private String redirectUri;

    @Override
    public void init() throws ServletException {
        clientId = getServletContext().getInitParameter("google.clientId");
        redirectUri = getServletContext().getInitParameter("google.redirectUri");

        if (clientId == null || clientId.isBlank()) {
            throw new ServletException("❌ Missing google.clientId in web.xml");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new ServletException("❌ Missing google.redirectUri in web.xml");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // CSRF state
        String state = UUID.randomUUID().toString();
        req.getSession(true).setAttribute("oauth2_state", state);

        String authorizationUrl = new GoogleAuthorizationCodeRequestUrl(
                clientId,
                redirectUri,
                Arrays.asList(
                        "openid",
                        "https://www.googleapis.com/auth/userinfo.email",
                        "https://www.googleapis.com/auth/userinfo.profile"
                )
        )
                .setAccessType("offline") // lấy refresh token nếu cần
                .setApprovalPrompt("force") // library cũ: tương đương prompt=consent
                // .set("prompt","consent")    // (phương án 2) nếu muốn dùng tham số mới
                .setState(state)
                .build();

        resp.sendRedirect(authorizationUrl);
    }

}
