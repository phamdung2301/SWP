package com.liteflow.controller.alert;

import com.liteflow.service.notice.NoticeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servlet for sending notices to employee dashboard
 * Only accessible by Admin users
 */
@WebServlet("/api/send-notification")
public class SendNotificationServlet extends HttpServlet {

    private final NoticeService noticeService;

    public SendNotificationServlet() {
        this.noticeService = new NoticeService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JSONObject errorJson = new JSONObject();
            errorJson.put("success", false);
            errorJson.put("error", "Not authenticated - no session");
            response.getWriter().write(errorJson.toString());
            return;
        }

        // Get userId from session (AuthenticationFilter sets "UserLogin")
        Object userLoginObj = session.getAttribute("UserLogin");
        UUID userId = null;

        if (userLoginObj instanceof UUID) {
            userId = (UUID) userLoginObj;
        } else if (userLoginObj instanceof String) {
            try {
                userId = UUID.fromString((String) userLoginObj);
            } catch (IllegalArgumentException e) {
                // userLoginObj is email, not UUID
            }
        }

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JSONObject errorJson = new JSONObject();
            errorJson.put("success", false);
            errorJson.put("error", "Not authenticated - no user ID");
            response.getWriter().write(errorJson.toString());
            return;
        }

        // Check if user is admin
        Object rolesObj = session.getAttribute("UserRoles");
        boolean isAdmin = false;

        if (rolesObj instanceof java.util.List) {
            
            java.util.List<String> roles = (java.util.List<String>) rolesObj;
            isAdmin = roles.contains("Admin") || roles.contains("Manager");
        }

        if (!isAdmin) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JSONObject errorJson = new JSONObject();
            errorJson.put("success", false);
            errorJson.put("error", "Only admins can send notifications");
            response.getWriter().write(errorJson.toString());
            return;
        }

        try {
            // Read request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject requestBody = new JSONObject(sb.toString());

            // Extract parameters
            String title = requestBody.optString("title", "");
            String content = requestBody.optString("message", "");
            String noticeType = mapPriorityToNoticeType(requestBody.optString("priority", "MEDIUM"));
            boolean isPinned = requestBody.optBoolean("isPinned", false);

            // Optional expiry date (in hours from now)
            int expiryHours = requestBody.optInt("expiryHours", 0);
            LocalDateTime expiresAt = null;
            if (expiryHours > 0) {
                expiresAt = LocalDateTime.now().plusHours(expiryHours);
            }

            // Validate input
            String validationError = noticeService.validateNoticeInput(title, content, noticeType);
            if (validationError != null) {
                JSONObject errorJson = new JSONObject();
                errorJson.put("success", false);
                errorJson.put("error", validationError);
                response.getWriter().write(errorJson.toString());
                return;
            }

            // Create notice for all employees
            UUID noticeID = noticeService.createNotice(
                title,
                content,
                noticeType,
                isPinned,
                expiresAt,
                userId
            );

            if (noticeID != null) {
                JSONObject successJson = new JSONObject();
                successJson.put("success", true);
                successJson.put("message", "Thông báo đã được gửi thành công");
                successJson.put("noticeID", noticeID.toString());
                response.getWriter().write(successJson.toString());

                System.out.println("✅ Notice sent successfully: " + title);
            } else {
                JSONObject errorJson = new JSONObject();
                errorJson.put("success", false);
                errorJson.put("error", "Không thể tạo thông báo");
                response.getWriter().write(errorJson.toString());

                System.err.println("❌ Failed to send notice: " + title);
            }

        } catch (Exception e) {
            System.err.println("❌ Error sending notification: " + e.getMessage());
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject errorJson = new JSONObject();
            errorJson.put("success", false);
            errorJson.put("error", "Internal server error: " + e.getMessage());
            response.getWriter().write(errorJson.toString());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject errorJson = new JSONObject();
        errorJson.put("success", false);
        errorJson.put("error", "GET method not supported. Use POST.");
        response.getWriter().write(errorJson.toString());
    }

    /**
     * Map priority (from alert system) to noticeType (for notice board)
     */
    private String mapPriorityToNoticeType(String priority) {
        if (priority == null) return "general";

        switch (priority.toUpperCase()) {
            case "CRITICAL":
            case "URGENT":
                return "urgent";
            case "HIGH":
                return "important";
            case "MEDIUM":
                return "general";
            case "LOW":
                return "info";
            default:
                return "general";
        }
    }
}
