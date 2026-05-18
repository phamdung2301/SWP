package com.liteflow.modules.inventory.service;

import com.liteflow.modules.inventory.dao.OrderDAO;
import java.util.*;

public class OrderService {
    
    private final OrderDAO orderDAO;
    
    public OrderService() {
        this.orderDAO = new OrderDAO();
    }
    
    /**
     * Tạo order mới và gửi thông báo đến bếp
     * @param tableId ID của bàn (có thể null với bàn đặc biệt: Mang về, Giao hàng)
     * @param items Danh sách món
     * @param userId ID của user tạo order
     * @param invoiceName Tên hóa đơn
     * @param orderNote Ghi chú đơn hàng
     * @return Map chứa orderId và orderNumber
     */
    public Map<String, Object> createOrderAndNotifyKitchen(UUID tableId, List<Map<String, Object>> items, UUID userId, String invoiceName, String orderNote) {
        // ✅ Cho phép tableId = null với bàn đặc biệt (Mang về, Giao hàng)
        // Validate input
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách món không được rỗng");
        }
        
        // Tạo order
        Map<String, Object> orderInfo = orderDAO.createOrder(tableId, items, userId, invoiceName, orderNote);
        
        // ✅ Kiểm tra null trước khi get
        if (orderInfo == null) {
            throw new RuntimeException("Không thể tạo order");
        }
        
        String orderNumber = (String) orderInfo.get("orderNumber");
        String tableInfo = (tableId != null) ? "Table ID: " + tableId : "Bàn đặc biệt (Mang về/Giao hàng)";
        
      
        // Ví dụ: WebSocket, Server-Sent Events, hoặc Polling
        System.out.println("📢 Thông báo đến bếp: Order mới đã được tạo - " + tableInfo + ", OrderNumber: " + orderNumber + ", Invoice: " + invoiceName + (orderNote != null ? ", Note: " + orderNote : ""));
        
        return orderInfo;
    }
    
    /**
     * Lấy orders của bàn (cho cashier)
     */
    public List<Map<String, Object>> getOrdersByTable(UUID tableId) {
        if (tableId == null) {
            throw new IllegalArgumentException("Table ID không được null");
        }
        
        List<Map<String, Object>> result = orderDAO.getOrdersByTable(tableId);
        // ✅ Return empty list instead of null
        return result != null ? result : new ArrayList<>();
    }
    
    /**
     * Lấy danh sách orders đang chờ làm
     */
    public List<Map<String, Object>> getPendingOrders() {
        List<Map<String, Object>> result = orderDAO.getPendingOrders();
        // ✅ Return empty list instead of null
        return result != null ? result : new ArrayList<>();
    }
    
    /**
     * Cập nhật trạng thái order
     */
    public boolean updateOrderStatus(UUID orderId, String status) {
        // ✅ Validate orderId
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID không được null");
        }
        
        // ✅ Validate status
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không được rỗng");
        }
        
        List<String> validStatuses = Arrays.asList("Pending", "Preparing", "Ready", "Served", "Cancelled");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }
        
        return orderDAO.updateOrderStatus(orderId, status);
    }
    
    /**
     * Đánh dấu order là đang chuẩn bị
     */
    public boolean markOrderAsPreparing(UUID orderId) {
        return updateOrderStatus(orderId, "Preparing");
    }
    
    /**
     * Đánh dấu order là đã sẵn sàng
     */
    public boolean markOrderAsReady(UUID orderId) {
        return updateOrderStatus(orderId, "Ready");
    }
    
    /**
     * Đánh dấu order là đã phục vụ
     */
    public boolean markOrderAsServed(UUID orderId) {
        return updateOrderStatus(orderId, "Served");
    }
    
    /**
     * Hủy order
     */
    public boolean cancelOrder(UUID orderId) {
        return updateOrderStatus(orderId, "Cancelled");
    }
}

