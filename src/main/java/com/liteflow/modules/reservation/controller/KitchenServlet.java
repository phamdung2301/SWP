package com.liteflow.modules.reservation.controller;

import com.liteflow.modules.inventory.service.OrderService;
import com.liteflow.modules.core.dao.BaseDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Kitchen Servlet - Unified controller cho tất cả kitchen endpoints
 * - /kitchen - Load trang kitchen với initial data
 * - /api/kitchen/orders - API lấy danh sách orders (JSON)
 * - /api/kitchen/notifications - API lấy/lưu lịch sử thông báo (JSON)
 */
@WebServlet(urlPatterns = {"/kitchen", "/api/kitchen/orders", "/api/kitchen/notifications"})
public class KitchenServlet extends HttpServlet {
    
    private OrderService orderService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        try {
            switch (path) {
                case "/kitchen":
                    handleKitchenPage(request, response);
                    break;
                case "/api/kitchen/orders":
                    handleGetOrders(request, response);
                    break;
                case "/api/kitchen/notifications":
                    handleGetNotifications(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Endpoint không tồn tại");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong KitchenServlet: " + e.getMessage());
            e.printStackTrace();
            
            if (path.startsWith("/api/")) {
                // API endpoint - trả về JSON error
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                sendErrorResponse(response, out, 500, "Lỗi server: " + e.getMessage());
                out.flush();
            } else {
                // Web page - trả về error page
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server");
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        if ("/api/kitchen/notifications".equals(path)) {
            handleSaveNotification(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method không được hỗ trợ");
        }
    }
    
    // ========== HANDLER METHODS ==========
    
    /**
     * Load trang kitchen với initial orders data
     */
    private void handleKitchenPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Lấy danh sách orders đang pending, preparing, và ready
        List<Map<String, Object>> pendingOrders = orderService.getPendingOrders();
        
        // Convert to JSON for JavaScript
        request.setAttribute("ordersJson", gson.toJson(pendingOrders));
        
        // Forward to JSP
        request.getRequestDispatcher("/kitchen/kitchen.jsp").forward(request, response);
        
        System.out.println("✅ Loaded kitchen page with " + pendingOrders.size() + " orders");
    }
    
    /**
     * API: Lấy danh sách orders cho kitchen (JSON)
     */
    private void handleGetOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Lấy danh sách orders đang pending, preparing, và ready
            List<Map<String, Object>> pendingOrders = orderService.getPendingOrders();
            
            // Trả về JSON response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("orders", pendingOrders);
            responseData.put("count", pendingOrders.size());
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(responseData));
            
            System.out.println("✅ API: Đã gửi " + pendingOrders.size() + " orders cho kitchen");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy orders: " + e.getMessage());
            e.printStackTrace();
            
            // Gửi error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi server: " + e.getMessage());
            errorResponse.put("orders", new ArrayList<>());
            errorResponse.put("count", 0);
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(errorResponse));
        } finally {
            out.flush();
        }
    }
    
    /**
     * API: Lấy lịch sử notifications
     */
    private void handleGetNotifications(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Lấy số lượng notifications cần lấy (mặc định 50)
            int limit = 50;
            String limitParam = request.getParameter("limit");
            if (limitParam != null && !limitParam.isEmpty()) {
                try {
                    limit = Integer.parseInt(limitParam);
                    if (limit <= 0 || limit > 100) {
                        limit = 50;
                    }
                } catch (NumberFormatException e) {
                    limit = 50;
                }
            }
            
            // Lấy notifications từ database
            List<Map<String, Object>> notifications = getOrderStatusHistory(limit);
            
            // Trả về response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("notifications", notifications);
            responseData.put("count", notifications.size());
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(responseData));
            
            System.out.println("✅ Trả về " + notifications.size() + " notifications cho kitchen");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy kitchen notifications: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, out, 500, "Lỗi server: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
    
    /**
     * API: Lưu notification mới (optional - cho future use)
     */
    private void handleSaveNotification(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Đọc JSON từ request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            String requestBody = sb.toString();
            System.out.println("📥 Nhận request lưu notification: " + requestBody);
            
            // Parse JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> notificationData = gson.fromJson(requestBody, Map.class);
            
            // Validate data
            if (notificationData == null) {
                sendErrorResponse(response, out, 400, "Request body không hợp lệ");
                return;
            }
            
            // Log notification (có thể lưu vào database nếu cần)
            System.out.println("📝 Kitchen notification: " + notificationData);
            
            // Trả về response thành công
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Đã lưu notification");
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(responseData));
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lưu notification: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, out, 500, "Lỗi server: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Lấy lịch sử cập nhật trạng thái đơn hàng từ database (OrderStatusHistory)
     */
    private List<Map<String, Object>> getOrderStatusHistory(int limit) {
        EntityManager em = BaseDAO.emf.createEntityManager();
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        try {
            // Query từ OrderStatusHistory table - lưu mọi lần thay đổi trạng thái
            String sql = """
                SELECT 
                    osh.HistoryID,
                    osh.OrderID,
                    o.OrderNumber,
                    t.TableName,
                    osh.OldStatus,
                    osh.NewStatus,
                    osh.ChangedAt,
                    osh.OrderDetailsSnapshot,
                    u.DisplayName AS ChangedByName,
                    o.TotalAmount
                FROM OrderStatusHistory osh
                INNER JOIN Orders o ON osh.OrderID = o.OrderID
                INNER JOIN TableSessions ts ON o.SessionID = ts.SessionID
                LEFT JOIN Tables t ON ts.TableID = t.TableID
                LEFT JOIN Users u ON osh.ChangedBy = u.UserID
                ORDER BY osh.ChangedAt DESC
                """;
            
            Query query = em.createNativeQuery(sql);
            query.setMaxResults(limit);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            
            for (Object[] row : results) {
                try {
                    String historyId = row[0] != null ? row[0].toString() : "";
                    String orderId = row[1] != null ? row[1].toString() : "";
                    String orderNumber = row[2] != null ? row[2].toString() : "";
                    String tableName = row[3] != null ? row[3].toString() : "N/A";
                    String oldStatus = row[4] != null ? row[4].toString() : "";
                    String newStatus = row[5] != null ? row[5].toString() : "";
                    Object changedAt = row[6];
                    String orderDetailsJson = row[7] != null ? row[7].toString() : "[]";
                    String changedByName = row[8] != null ? row[8].toString() : "System";
                    Object totalAmount = row[9];
                    
                    // Parse OrderDetailsSnapshot JSON
                    List<Map<String, Object>> items = new ArrayList<>();
                    try {
                        if (orderDetailsJson != null && !orderDetailsJson.isEmpty() && !orderDetailsJson.equals("null")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> parsedItems = gson.fromJson(orderDetailsJson, List.class);
                            if (parsedItems != null) {
                                items = parsedItems;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Không parse được OrderDetailsSnapshot: " + e.getMessage());
                    }
                    
                    // Format timestamp
                    String timestamp = "";
                    if (changedAt != null) {
                        if (changedAt instanceof java.sql.Timestamp) {
                            timestamp = ((java.sql.Timestamp) changedAt).toLocalDateTime().format(formatter);
                        } else if (changedAt instanceof LocalDateTime) {
                            timestamp = ((LocalDateTime) changedAt).format(formatter);
                        } else {
                            timestamp = changedAt.toString();
                        }
                    }
                    
                    // Tạo title dựa trên status change
                    String title = generateNotificationTitle(oldStatus, newStatus);
                    
                    // Tạo notification object
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "status-change");
                    notification.put("title", title);
                    notification.put("historyId", historyId);
                    notification.put("orderId", orderId);
                    notification.put("orderNumber", orderNumber);
                    notification.put("tableName", tableName);
                    notification.put("oldStatus", oldStatus);
                    notification.put("newStatus", newStatus);
                    notification.put("timestamp", timestamp);
                    notification.put("items", items);
                    notification.put("changedByName", changedByName);
                    notification.put("totalAmount", totalAmount);
                    
                    notifications.add(notification);
                    
                } catch (Exception e) {
                    System.err.println("⚠️ Lỗi khi parse row: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("📊 Đã lấy " + notifications.size() + " notifications từ OrderStatusHistory");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi query OrderStatusHistory: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        
        return notifications;
    }
    
    /**
     * Tạo title cho notification dựa trên status change
     */
    private String generateNotificationTitle(String oldStatus, String newStatus) {
        // Map status transitions to Vietnamese titles
        String transition = oldStatus + " -> " + newStatus;
        
        switch (transition) {
            case "Pending -> Preparing":
                return "Đơn hàng bắt đầu chế biến";
            case "Preparing -> Ready":
                return "Đơn hàng đã sẵn sàng";
            case "Ready -> Served":
                return "Đơn hàng đã phục vụ";
            case "Pending -> Cancelled":
            case "Preparing -> Cancelled":
            case "Ready -> Cancelled":
                return "Đơn hàng đã bị hủy";
            default:
                return "Cập nhật trạng thái đơn hàng";
        }
    }
    
    /**
     * Gửi error response
     */
    private void sendErrorResponse(HttpServletResponse response, PrintWriter out, 
                                   int statusCode, String message) {
        response.setStatus(statusCode);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        out.print(gson.toJson(errorResponse));
    }
}
