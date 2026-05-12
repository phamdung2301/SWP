package com.liteflow.controller.admin;

import com.liteflow.dao.alert.AlertHistoryDAO;
import com.liteflow.model.alert.AlertHistory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notification API Servlet
 * Handles sending notifications to admin/manager when employees submit requests
 */
@WebServlet(name = "NotificationAPIServlet", urlPatterns = {"/api/notification/*"})
public class NotificationAPIServlet extends HttpServlet {

    private final AlertHistoryDAO alertHistoryDAO = new AlertHistoryDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();

        try {
            // Check if user is authenticated
            UUID userId = getUserIdFromSession(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\":false,\"error\":\"Not authenticated\"}");
                return;
            }

            if ("/send-to-admin".equals(pathInfo)) {
                // POST /api/notification/send-to-admin
                handleSendToAdmin(req, resp, userId);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\":false,\"error\":\"Endpoint not found\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        }
    }

    /**
     * Handle sending notification to admin
     */
    private void handleSendToAdmin(HttpServletRequest req, HttpServletResponse resp, UUID userId)
            throws IOException {
        PrintWriter out = resp.getWriter();

        try {
            // Parse request parameters
            String type = req.getParameter("type");
            String title = req.getParameter("title");
            String message = req.getParameter("message");
            String priority = req.getParameter("priority");
            String targetUrl = req.getParameter("targetUrl");

            // Validate required fields
            if (type == null || type.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"error\":\"Type is required\"}");
                return;
            }
            if (title == null || title.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"error\":\"Title is required\"}");
                return;
            }
            if (message == null || message.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"error\":\"Message is required\"}");
                return;
            }

            // Set default priority if not provided
            if (priority == null || priority.trim().isEmpty()) {
                priority = "MEDIUM";
            }

            // Validate priority
            if (!isValidPriority(priority)) {
                priority = "MEDIUM";
            }

            // Create AlertHistory entry
            AlertHistory alert = new AlertHistory();
            alert.setHistoryID(UUID.randomUUID());
            alert.setAlertType(type.trim().toUpperCase());
            alert.setTitle(title.trim());
            alert.setMessage(message.trim());
            alert.setPriority(priority.toUpperCase());
            
            // Set delivery channels
            alert.setSentInApp(true);
            alert.setDeliveryStatus("SENT");
            
            // Set timestamps
            alert.setTriggeredAt(LocalDateTime.now());
            alert.setSentAt(LocalDateTime.now());
            
            // Set context data with target URL
            if (targetUrl != null && !targetUrl.trim().isEmpty()) {
                String contextData = "{\"targetUrl\":\"" + escapeJson(targetUrl) + "\",\"triggeredBy\":\"" + userId.toString() + "\"}";
                alert.setContextData(contextData);
            }
            
            // Mark as unread
            alert.setIsRead(false);
            alert.setIsDismissed(false);

            // Insert into database
            boolean success = alertHistoryDAO.insert(alert);

            if (success) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"success\":true,\"message\":\"Notification sent successfully\",\"historyId\":\"" + alert.getHistoryID() + "\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"error\":\"Failed to create notification\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        }
    }

    /**
     * Get user ID from session
     */
    private UUID getUserIdFromSession(HttpServletRequest req) {
        Object userLogin = req.getSession().getAttribute("UserLogin");
        if (userLogin == null) {
            return null;
        }

        if (userLogin instanceof UUID) {
            return (UUID) userLogin;
        } else if (userLogin instanceof String) {
            try {
                return UUID.fromString((String) userLogin);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Validate priority value
     */
    private boolean isValidPriority(String priority) {
        if (priority == null) return false;
        String upperPriority = priority.toUpperCase();
        return "LOW".equals(upperPriority) || 
               "MEDIUM".equals(upperPriority) || 
               "HIGH".equals(upperPriority) || 
               "CRITICAL".equals(upperPriority);
    }

    /**
     * Escape JSON string
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

