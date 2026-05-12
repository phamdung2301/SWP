package com.liteflow.controller.alert;

import com.liteflow.model.alert.AlertHistory;
import com.liteflow.service.alert.AlertService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Servlet for Alert System
 * Handles alert viewing, marking as read, dismissing
 */
@WebServlet("/alert/*")
public class AlertServlet extends HttpServlet {
    
    private final AlertService alertService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public AlertServlet() {
        this.alertService = new AlertService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Show alert dashboard
            handleDashboard(request, response);
        } else if (pathInfo.equals("/api/unread-count")) {
            // Get unread count (for notification bell)
            handleUnreadCount(request, response);
        } else if (pathInfo.equals("/api/recent")) {
            // Get recent alerts (for notification dropdown)
            handleRecentAlerts(request, response);
        } else if (pathInfo.equals("/api/active")) {
            // Get active alerts (for dashboard)
            handleActiveAlerts(request, response);
        } else if (pathInfo.startsWith("/api/detail/")) {
            // Get alert detail
            String historyID = pathInfo.substring("/api/detail/".length());
            handleAlertDetail(request, response, historyID);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            System.err.println("❌ AlertServlet doPost: No session found");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Not authenticated - no session\"}");
            return;
        }
        
        // Get userId from session (AuthenticationFilter sets "UserLogin")
        Object userLoginObj = session.getAttribute("UserLogin");
        System.out.println("🔍 AlertServlet doPost: UserLogin object: " + userLoginObj + " (type: " + (userLoginObj != null ? userLoginObj.getClass().getName() : "null") + ")");
        UUID userId = null;
        
        if (userLoginObj instanceof UUID) {
            userId = (UUID) userLoginObj;
        } else if (userLoginObj instanceof String) {
            try {
                userId = UUID.fromString((String) userLoginObj);
            } catch (IllegalArgumentException e) {
                // userLoginObj is email, not UUID
                System.err.println("❌ AlertServlet doPost: Invalid UUID string: " + userLoginObj);
            }
        }
        
        if (userId == null) {
            System.err.println("❌ AlertServlet doPost: No valid userId found");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Not authenticated - no user ID\"}");
            return;
        }
        
        System.out.println("✅ AlertServlet doPost: userId extracted: " + userId);
        
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        if (pathInfo.equals("/api/mark-read")) {
            handleMarkAsRead(request, response, userId);
        } else if (pathInfo.equals("/api/mark-all-read")) {
            handleMarkAllAsRead(request, response, userId);
        } else if (pathInfo.equals("/api/dismiss")) {
            handleDismiss(request, response, userId);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * Show alert dashboard page
     */
    private void handleDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get statistics
        JSONObject stats = alertService.getStatistics();
        request.setAttribute("stats", stats);
        
        // Get active alerts
        List<AlertHistory> activeAlerts = alertService.getActiveAlerts(50);
        request.setAttribute("activeAlerts", activeAlerts);
        
        // Forward to JSP
        request.getRequestDispatcher("/alert/dashboard.jsp").forward(request, response);
    }
    
    /**
     * Get unread count (JSON API)
     */
    private void handleUnreadCount(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            long count = alertService.getUnreadCount();
            
            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("unreadCount", count);
            
            response.getWriter().write(json.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Error getting unread count: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }
    
    /**
     * Get recent alerts (JSON API)
     */
    private void handleRecentAlerts(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            int limit = 10;
            String limitParam = request.getParameter("limit");
            if (limitParam != null) {
                limit = Integer.parseInt(limitParam);
            }
            
            List<AlertHistory> alerts = alertService.getUnreadAlerts(limit);
            
            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("alerts", convertAlertsToJSON(alerts));
            json.put("count", alerts.size());
            
            response.getWriter().write(json.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Error getting recent alerts: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }
    
    /**
     * Get active alerts (JSON API)
     */
    private void handleActiveAlerts(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            int limit = 50;
            String limitParam = request.getParameter("limit");
            if (limitParam != null) {
                limit = Integer.parseInt(limitParam);
            }
            
            List<AlertHistory> alerts = alertService.getActiveAlerts(limit);
            
            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("alerts", convertAlertsToJSON(alerts));
            json.put("count", alerts.size());
            
            response.getWriter().write(json.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Error getting active alerts: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }
    
    /**
     * Get alert detail (JSON API)
     */
    private void handleAlertDetail(HttpServletRequest request, HttpServletResponse response, String historyIDStr) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            UUID historyID = UUID.fromString(historyIDStr);
            AlertHistory alert = alertService.getAlertById(historyID);
            
            if (alert == null) {
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("error", "Alert not found");
                response.getWriter().write(json.toString());
                return;
            }
            
            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("alert", convertAlertToJSON(alert));
            
            response.getWriter().write(json.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Error getting alert detail: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }
    
    /**
     * Mark alert as read (POST)
     */
    private void handleMarkAsRead(HttpServletRequest request, HttpServletResponse response, UUID userId) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String historyIDStr = request.getParameter("historyId");
            if (historyIDStr == null || historyIDStr.isEmpty()) {
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("error", "Missing historyId parameter");
                response.getWriter().write(json.toString());
                return;
            }
            
            UUID historyID = UUID.fromString(historyIDStr);
            boolean success = alertService.markAsRead(historyID, userId);
            
            JSONObject json = new JSONObject();
            json.put("success", success);
            if (!success) {
                json.put("error", "Failed to mark as read");
            }
            
            response.getWriter().write(json.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Error marking as read: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }
    
    /**
     * Mark all alerts as read (POST)
     */
    private void handleMarkAllAsRead(HttpServletRequest request, HttpServletResponse response, UUID userId) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            int count = alertService.markAllAsRead(userId);
            
            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("count", count);
            
            response.getWriter().write(json.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Error marking all as read: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }
    
    /**
     * Dismiss alert (POST)
     */
    private void handleDismiss(HttpServletRequest request, HttpServletResponse response, UUID userId) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String historyIDStr = request.getParameter("historyId");
            if (historyIDStr == null || historyIDStr.isEmpty()) {
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("error", "Missing historyId parameter");
                response.getWriter().write(json.toString());
                return;
            }
            
            UUID historyID = UUID.fromString(historyIDStr);
            boolean success = alertService.dismissAlert(historyID, userId);
            
            JSONObject json = new JSONObject();
            json.put("success", success);
            if (!success) {
                json.put("error", "Failed to dismiss alert");
            }
            
            response.getWriter().write(json.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Error dismissing alert: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }
    
    /**
     * Convert alerts list to JSON array
     */
    private JSONArray convertAlertsToJSON(List<AlertHistory> alerts) {
        JSONArray jsonArray = new JSONArray();
        
        for (AlertHistory alert : alerts) {
            jsonArray.put(convertAlertToJSON(alert));
        }
        
        return jsonArray;
    }
    
    /**
     * Convert single alert to JSON object
     */
    private JSONObject convertAlertToJSON(AlertHistory alert) {
        JSONObject json = new JSONObject();
        
        json.put("historyID", alert.getHistoryID().toString());
        json.put("alertType", alert.getAlertType());
        json.put("title", alert.getTitle());
        json.put("message", alert.getMessage());
        json.put("priority", alert.getPriority());
        json.put("isRead", alert.getIsRead());
        json.put("isDismissed", alert.getIsDismissed());
        json.put("triggeredAt", alert.getTriggeredAt() != null ? 
            alert.getTriggeredAt().format(FORMATTER) : null);
        json.put("minutesAgo", alert.getMinutesAgo());
        
        // Optional fields
        if (alert.getGptSummary() != null) {
            json.put("gptSummary", alert.getGptSummary());
        }
        if (alert.getContextData() != null) {
            try {
                json.put("contextData", new JSONObject(alert.getContextData()));
            } catch (Exception e) {
                json.put("contextData", alert.getContextData());
            }
        }
        
        return json;
    }
}

