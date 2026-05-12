package com.liteflow.controller.cashier;

import com.liteflow.dao.BaseDAO;
import com.liteflow.service.inventory.OrderService;
import com.liteflow.util.OrderDataUtil;
import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Unified Cashier API Servlet
 * Handles multiple endpoints:
 * - POST /api/cashier/order/create - Create new order and notify kitchen
 * - POST /api/cashier/checkout - Process payment and close table session
 * - GET  /api/cashier/invoice/next-number - Get next invoice number for table
 * - GET  /api/cashier/notification/history - Get notification history
 */
@WebServlet("/api/cashier/*")
public class CashierAPIServlet extends HttpServlet {
    
    private OrderService orderService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        // ✅ Khởi tạo orderService
        if (orderService == null) {
            orderService = new OrderService();
        }
        if (gson == null) {
            gson = new Gson();
        }
    }
    
    /**
     * ✅ Ensure orderService is initialized (for safety)
     */
    private void ensureInitialized() {
        if (orderService == null) {
            orderService = new OrderService();
        }
        if (gson == null) {
            gson = new Gson();
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        // Route based on path
        if (pathInfo != null) {
            if (pathInfo.equals("/invoice/next-number")) {
                handleGetNextInvoiceNumber(request, response);
            } else if (pathInfo.equals("/notification/history")) {
                handleGetNotificationHistory(request, response);
            } else {
                sendErrorResponse(response, 404, "Endpoint không tồn tại: " + pathInfo);
            }
        } else {
            sendErrorResponse(response, 400, "Thiếu path info");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        // Route based on path
        if (pathInfo != null) {
            if (pathInfo.equals("/order/create")) {
                handleCreateOrder(request, response);
            } else if (pathInfo.equals("/checkout")) {
                handleCheckout(request, response);
            } else {
                sendErrorResponse(response, 404, "Endpoint không tồn tại: " + pathInfo);
            }
        } else {
            sendErrorResponse(response, 400, "Thiếu path info");
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Hỗ trợ CORS nếu cần
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    // =============================================
    // HANDLER: Create Order (POST /api/cashier/order/create)
    // =============================================
    
    private void handleCreateOrder(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        // ✅ Đảm bảo orderService đã được khởi tạo
        ensureInitialized();
        
        PrintWriter out = response.getWriter();
        
        try {
            // Đọc JSON từ request body
            BufferedReader reader = request.getReader();
            String requestBody = OrderDataUtil.readRequestBody(reader);
            System.out.println("📥 Nhận request tạo order: " + requestBody);
            
            // Parse JSON
            Map<String, Object> requestData = OrderDataUtil.parseRequestData(requestBody);
            
            // Validate input
            if (requestData == null) {
                sendErrorResponse(response, 400, "Request body không hợp lệ");
                return;
            }
            
            // Validate table ID
            String tableIdError = OrderDataUtil.validateTableId(requestData);
            if (tableIdError != null) {
                sendErrorResponse(response, 400, tableIdError);
                return;
            }
            
            // Validate items
            String itemsError = OrderDataUtil.validateItems(requestData);
            if (itemsError != null) {
                sendErrorResponse(response, 400, itemsError);
                return;
            }
            
            // Extract and convert data
            String tableIdStr = OrderDataUtil.extractTableId(requestData);
            List<Map<String, Object>> items = OrderDataUtil.extractItems(requestData);
            String invoiceName = (String) requestData.get("invoiceName");
            String orderNote = (String) requestData.get("orderNote"); // ✅ Lấy ghi chú hóa đơn
            
            // Convert tableId to UUID
            UUID tableId;
            try {
                tableId = OrderDataUtil.parseTableId(tableIdStr);
            } catch (IllegalArgumentException e) {
                sendErrorResponse(response, 400, "Table ID không hợp lệ: " + tableIdStr);
                return;
            }
            
          
            UUID userId = null;
            
            System.out.println("📝 Order Note: " + (orderNote != null ? orderNote : "Không có ghi chú"));
            
            // Tạo order
            Map<String, Object> orderInfo = orderService.createOrderAndNotifyKitchen(tableId, items, userId, invoiceName, orderNote);
            String orderNumber = (String) orderInfo.get("orderNumber");
            UUID orderId = (UUID) orderInfo.get("orderId");
            UUID sessionId = (UUID) orderInfo.get("sessionId");
            
            // Trả về response thành công
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Đã gửi thông báo đến bếp thành công!");
            responseData.put("orderId", orderNumber); // ✅ Trả về orderNumber dễ đọc hơn UUID
            responseData.put("orderIdUUID", orderId.toString()); // Giữ UUID cho reference
            if (sessionId != null) {
                responseData.put("sessionId", sessionId.toString()); // ✅ Trả về sessionId
            }
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(responseData));
            
            System.out.println("✅ Đã tạo order thành công: " + orderNumber + " (UUID: " + orderId + ")");
            
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Lỗi validation: " + e.getMessage());
            sendErrorResponse(response, 400, e.getMessage());
        } catch (RuntimeException e) {
            // ✅ Check if RuntimeException is wrapping IllegalArgumentException
            if (e.getCause() instanceof IllegalArgumentException) {
                System.err.println("⚠️ Lỗi validation: " + e.getCause().getMessage());
                sendErrorResponse(response, 400, e.getCause().getMessage());
            } else {
                System.err.println("❌ Lỗi khi tạo order: " + e.getMessage());
                e.printStackTrace();
                sendErrorResponse(response, 500, "Lỗi server: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo order: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "Lỗi server: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
    
    // =============================================
    // HANDLER: Get Next Invoice Number (GET /api/cashier/invoice/next-number)
    // =============================================
    
    private void handleGetNextInvoiceNumber(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        // ✅ Đảm bảo orderService đã được khởi tạo
        ensureInitialized();
        
        PrintWriter out = response.getWriter();
        
        try {
            String tableIdStr = request.getParameter("tableId");
            
            if (tableIdStr == null || tableIdStr.isEmpty()) {
                sendErrorResponse(response, 400, "Table ID không được rỗng");
                return;
            }
            
            // Convert tableId to UUID
            UUID tableId;
            try {
                tableId = UUID.fromString(tableIdStr);
            } catch (IllegalArgumentException e) {
                sendErrorResponse(response, 400, "Table ID không hợp lệ: " + tableIdStr);
                return;
            }
            
            // Lấy số thứ tự hóa đơn tiếp theo từ database
            int nextNumber = getNextInvoiceNumber(tableId);
            
            // Trả về response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("tableId", tableIdStr);
            responseData.put("nextNumber", nextNumber);
            responseData.put("invoiceNumber", nextNumber); // ✅ Thêm field này cho test
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(responseData));
            
            System.out.println("✅ Next invoice number for table " + tableIdStr + ": " + nextNumber);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy số hóa đơn tiếp theo: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "Lỗi server: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
    
    /**
     * Lấy số thứ tự hóa đơn tiếp theo cho bàn
     */
    private int getNextInvoiceNumber(UUID tableId) {
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            String jpql = "SELECT COUNT(s) FROM TableSession s " +
                         "WHERE s.table.tableId = :tableId";
            
            Query query = em.createQuery(jpql);
            query.setParameter("tableId", tableId);
            
            Long count = (Long) query.getSingleResult();
            return count.intValue() + 1;
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đếm sessions: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } finally {
            em.close();
        }
    }
    
    // =============================================
    // HANDLER: Get Notification History (GET /api/cashier/notification/history)
    // =============================================
    
    private void handleGetNotificationHistory(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        // ✅ Đảm bảo orderService đã được khởi tạo
        ensureInitialized();
        
        PrintWriter out = response.getWriter();
        
        try {
            // Lấy số ngày lịch sử (mặc định 7 ngày)
            String daysParam = request.getParameter("days");
            int days = 7;
            if (daysParam != null) {
                try {
                    days = Integer.parseInt(daysParam);
                } catch (NumberFormatException e) {
                    days = 7;
                }
            }
            
            LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
            EntityManager em = BaseDAO.emf.createEntityManager();
            
            try {
                List<Map<String, Object>> notifications = new ArrayList<>();
                
                // 1. Lấy lịch sử THÔNG BÁO BẾP
                String kitchenQuery = "SELECT o FROM Order o " +
                                     "WHERE o.orderDate >= :fromDate " +
                                     "ORDER BY o.orderDate DESC";
                
                Query kitchenQueryObj = em.createQuery(kitchenQuery);
                kitchenQueryObj.setParameter("fromDate", fromDate);
                
                
                List<com.liteflow.model.inventory.Order> orders = kitchenQueryObj.getResultList();
                
                for (com.liteflow.model.inventory.Order order : orders) {
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "kitchen");
                    notification.put("orderId", order.getOrderNumber()); // ✅ Trả về orderNumber thay vì UUID
                    notification.put("orderIdUUID", order.getOrderId().toString()); // Giữ UUID cho reference
                    notification.put("timestamp", order.getOrderDate().toString());
                    notification.put("amount", order.getSubTotal() != null ? order.getSubTotal().doubleValue() : 0.0);
                    
                    // Get table info và invoice name
                    String tableName = "Mang về / Giao hàng"; // Default cho bàn đặc biệt
                    String tableId = "";
                    String invoiceName = "";
                    if (order.getSession() != null) {
                        // ✅ Xử lý table với bàn đặc biệt (table = null)
                        if (order.getSession().getTable() != null) {
                            tableName = order.getSession().getTable().getTableName();
                            tableId = order.getSession().getTable().getTableId().toString();
                        }
                        // ✅ Lấy invoice name từ session
                        if (order.getSession().getInvoiceName() != null) {
                            invoiceName = order.getSession().getInvoiceName();
                        }
                    }
                    notification.put("tableName", tableName);
                    notification.put("tableId", tableId);
                    notification.put("invoiceName", invoiceName); // ✅ Thêm invoice name
                    
                    // Get item details
                    List<Map<String, Object>> items = new ArrayList<>();
                    int itemCount = 0;
                    
                    if (order.getOrderDetails() != null) {
                        itemCount = order.getOrderDetails().size();
                        for (com.liteflow.model.inventory.OrderDetail detail : order.getOrderDetails()) {
                            Map<String, Object> item = new HashMap<>();
                            if (detail.getProductVariant() != null && detail.getProductVariant().getProduct() != null) {
                                item.put("name", detail.getProductVariant().getProduct().getName());
                                item.put("size", detail.getProductVariant().getSize());
                                item.put("quantity", detail.getQuantity());
                                item.put("price", detail.getUnitPrice() != null ? detail.getUnitPrice().doubleValue() : 0.0);
                                items.add(item);
                            }
                        }
                    }
                    
                    notification.put("itemCount", itemCount);
                    notification.put("items", items);
                    
                    // ✅ Thêm ghi chú đơn hàng nếu có
                    String orderNote = order.getNotes();
                    if (orderNote != null && !orderNote.trim().isEmpty()) {
                        notification.put("orderNote", orderNote);
                    }
                    
                    notifications.add(notification);
                }
                
                // 2. Lấy lịch sử THANH TOÁN
                String paymentQuery = "SELECT s FROM TableSession s " +
                                     "WHERE s.status = 'Completed' " +
                                     "AND s.paymentStatus = 'Paid' " +
                                     "AND s.checkOutTime >= :fromDate " +
                                     "ORDER BY s.checkOutTime DESC";
                
                Query paymentQueryObj = em.createQuery(paymentQuery);
                paymentQueryObj.setParameter("fromDate", fromDate);
                

                List<com.liteflow.model.inventory.TableSession> sessions = paymentQueryObj.getResultList();
                
                for (com.liteflow.model.inventory.TableSession session : sessions) {
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "payment");
                    notification.put("sessionId", session.getSessionId().toString());
                    notification.put("timestamp", session.getCheckOutTime().toString());
                    notification.put("amount", session.getTotalAmount() != null ? session.getTotalAmount().doubleValue() : 0.0);
                    notification.put("discount", 0.0);
                    notification.put("paymentMethod", session.getPaymentMethod() != null ? session.getPaymentMethod() : "cash");
                    
                    // Get table info và invoice name
                    String tableName = "Mang về / Giao hàng"; // Default cho bàn đặc biệt
                    String tableId = "";
                    String invoiceName = "";
                    // ✅ Xử lý table với bàn đặc biệt (table = null)
                    if (session.getTable() != null) {
                        tableName = session.getTable().getTableName();
                        tableId = session.getTable().getTableId().toString();
                    }
                    // ✅ Lấy invoice name
                    if (session.getInvoiceName() != null && !session.getInvoiceName().isEmpty()) {
                        invoiceName = session.getInvoiceName();
                    }
                    notification.put("tableName", tableName);
                    notification.put("tableId", tableId);
                    notification.put("invoiceName", invoiceName); // ✅ Thêm invoice name
                    notification.put("finalAmount", session.getTotalAmount() != null ? session.getTotalAmount().doubleValue() : 0.0);
                    notification.put("hasVoucher", false);
                    
                    // Lấy số lượng món
                    int itemCount = 0;
                    if (session.getOrders() != null) {
                        for (com.liteflow.model.inventory.Order order : session.getOrders()) {
                            if (order.getOrderDetails() != null) {
                                itemCount += order.getOrderDetails().size();
                            }
                        }
                    }
                    notification.put("itemCount", itemCount);
                    notifications.add(notification);
                }
                
                // Sắp xếp theo thời gian
                notifications.sort((a, b) -> {
                    String timeA = (String) a.get("timestamp");
                    String timeB = (String) b.get("timestamp");
                    return timeB.compareTo(timeA);
                });
                
                // Trả về response
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("notifications", notifications);
                responseData.put("total", notifications.size());
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(responseData));
                
                System.out.println("✅ Đã lấy " + notifications.size() + " thông báo lịch sử");
                
            } finally {
                em.close();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy notification history: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "Lỗi server: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
    
    // =============================================
    // HANDLER: Checkout (POST /api/cashier/checkout)
    // =============================================
    
    private void handleCheckout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        // ✅ Đảm bảo orderService đã được khởi tạo
        ensureInitialized();
        
        PrintWriter out = response.getWriter();
        
        try {
            // Đọc JSON từ request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            String requestBody = sb.toString();
            System.out.println("📥 Nhận request checkout: " + requestBody);
            
            // Parse JSON
            
            Map<String, Object> requestData = gson.fromJson(requestBody, Map.class);
            
            if (requestData == null) {
                sendErrorResponse(response, 400, "Request body không hợp lệ");
                return;
            }
            
            String tableIdStr = (String) requestData.get("tableId");
            String paymentMethod = (String) requestData.get("paymentMethod");
            
            // Nhận totalAmount từ frontend (optional, sẽ tính từ orders nếu có)
            Double totalAmountFromRequest = null;
            Object totalAmountObj = requestData.get("totalAmount");
            if (totalAmountObj != null) {
                if (totalAmountObj instanceof Number) {
                    totalAmountFromRequest = ((Number) totalAmountObj).doubleValue();
                } else if (totalAmountObj instanceof String) {
                    try {
                        totalAmountFromRequest = Double.parseDouble((String) totalAmountObj);
                    } catch (NumberFormatException e) {
                        // Ignore, sẽ tính từ orders
                    }
                }
            }
            
            if (tableIdStr == null || tableIdStr.isEmpty()) {
                sendErrorResponse(response, 400, "Table ID không được rỗng");
                return;
            }
            
            // ✅ Convert tableId to UUID, cho phép special tables
            UUID tableId = null;
            boolean isSpecialTable = OrderDataUtil.isSpecialTable(tableIdStr);
            
            if (!isSpecialTable) {
                try {
                    tableId = UUID.fromString(tableIdStr);
                } catch (IllegalArgumentException e) {
                    sendErrorResponse(response, 400, "Table ID không hợp lệ: " + tableIdStr);
                    return;
                }
            }
            
            // Get userId from session (used for tracking and notifications)
            UUID userId = (UUID) request.getSession().getAttribute("userId");
            
            // Process checkout
            EntityManager em = BaseDAO.emf.createEntityManager();
            
            try {
                em.getTransaction().begin();
                
                // 1. Tìm hoặc tạo active session
                com.liteflow.model.inventory.TableSession session = null;
                
                if (!isSpecialTable && tableId != null) {
                    // ✅ Với bàn thường, tìm session active theo tableId
                    String sessionQuery = "SELECT s FROM TableSession s WHERE s.table.tableId = :tableId AND s.status = 'Active'";
                    Query query = em.createQuery(sessionQuery);
                    query.setParameter("tableId", tableId);
                    
    
                    List<com.liteflow.model.inventory.TableSession> sessions = query.getResultList();
                    
                    if (!sessions.isEmpty()) {
                        session = sessions.get(0);
                        System.out.println("📋 Sử dụng session có sẵn: " + session.getSessionId());
                    }
                } else if (isSpecialTable) {
                    // ✅ Với special tables, tìm session active gần nhất (có orders chưa checkout)
                    // Tìm session có orders và status = Active, sắp xếp theo thời gian mới nhất
                    String specialSessionQuery = "SELECT DISTINCT s FROM TableSession s " +
                                                 "JOIN s.orders o " +
                                                 "WHERE s.table IS NULL " +
                                                 "AND s.status = 'Active' " +
                                                 "ORDER BY s.checkInTime DESC";
                    Query specialQuery = em.createQuery(specialSessionQuery);
                    specialQuery.setMaxResults(1); // Lấy session mới nhất
                    
    
                    List<com.liteflow.model.inventory.TableSession> specialSessions = specialQuery.getResultList();
                    
                    if (!specialSessions.isEmpty()) {
                        session = specialSessions.get(0);
                        System.out.println("📋 Sử dụng session special table có sẵn: " + session.getSessionId());
                    }
                }
                
                // Tạo session mới nếu chưa có
                if (session == null) {
                    System.out.println("📝 Không tìm thấy session, tạo session mới cho checkout");
                    
                    session = new com.liteflow.model.inventory.TableSession();
                    
                    // ✅ Với bàn thường, tìm table entity
                    if (!isSpecialTable && tableId != null) {
                        com.liteflow.model.inventory.Table table = em.find(com.liteflow.model.inventory.Table.class, tableId);
                        if (table == null) {
                            em.getTransaction().rollback();
                            sendErrorResponse(response, 404, "Không tìm thấy bàn");
                            return;
                        }
                        session.setTable(table);
                    } else {
                        // ✅ Với special tables, không set table entity
                        session.setTable(null);
                        System.out.println("🏷️ Special table checkout: " + tableIdStr);
                    }
                    
                    session.setCheckInTime(LocalDateTime.now());
                    session.setStatus("Active");
                    session.setPaymentStatus("Unpaid");
                    
                    if (userId != null) {
                        com.liteflow.model.auth.User user = em.find(com.liteflow.model.auth.User.class, userId);
                        if (user != null) {
                            session.setCreatedBy(user);
                        }
                    }
                    
                    em.persist(session);
                    em.flush(); // Đảm bảo session được lưu và có ID
                    
                    System.out.println("✅ Đã tạo session mới: " + session.getSessionId());
                }
                
                // 2. Cập nhật session
                session.setStatus("Completed");
                session.setCheckOutTime(LocalDateTime.now());
                session.setPaymentStatus("Paid");
                if (paymentMethod != null && !paymentMethod.isEmpty()) {
                    session.setPaymentMethod(paymentMethod);
                }
                
                // Set totalAmount nếu có từ request hoặc giữ nguyên nếu đã có
                if (totalAmountFromRequest != null && totalAmountFromRequest > 0) {
                    session.setTotalAmount(java.math.BigDecimal.valueOf(totalAmountFromRequest));
                    System.out.println("💰 Set total amount from request: " + totalAmountFromRequest);
                } else if (session.getTotalAmount() == null || session.getTotalAmount().doubleValue() == 0) {
                    // Tính từ orders nếu có
                    String ordersQuery = "SELECT o FROM Order o WHERE o.session.sessionId = :sessionId";
                    Query ordersQueryObj = em.createQuery(ordersQuery);
                    ordersQueryObj.setParameter("sessionId", session.getSessionId());
                    
    
                    List<com.liteflow.model.inventory.Order> ordersForTotal = ordersQueryObj.getResultList();
                    
                    double calculatedTotal = 0;
                    for (com.liteflow.model.inventory.Order order : ordersForTotal) {
                        if (order.getTotalAmount() != null) {
                            calculatedTotal += order.getTotalAmount().doubleValue();
                        }
                    }
                    
                    if (calculatedTotal > 0) {
                        session.setTotalAmount(java.math.BigDecimal.valueOf(calculatedTotal));
                        System.out.println("💰 Calculated total from orders: " + calculatedTotal);
                    }
                }
                
                em.merge(session);
                
                // 3. Cập nhật trạng thái bàn về Available (chỉ với bàn thường)
                if (!isSpecialTable && tableId != null) {
                    com.liteflow.model.inventory.Table table = em.find(com.liteflow.model.inventory.Table.class, tableId);
                    if (table != null) {
                        table.setStatus("Available");
                        em.merge(table);
                        System.out.println("✅ Đã cập nhật trạng thái bàn về Available");
                    }
                    
                    // ✅ Đóng reservation của bàn này (nếu có)
                    try {
                        com.liteflow.service.inventory.ReservationService reservationService = 
                            new com.liteflow.service.inventory.ReservationService();
                        reservationService.closeReservationByTable(tableId);
                    } catch (Exception e) {
                        System.err.println("⚠️ Warning: Failed to close reservation: " + e.getMessage());
                        // Don't fail payment if reservation update fails
                    }
                }
                
                // 4. Cập nhật tất cả orders thành Served và lưu paymentMethod
                String updateOrdersQuery = "UPDATE Order o SET o.status = 'Served', o.paymentStatus = 'Paid'";
                if (paymentMethod != null && !paymentMethod.isEmpty()) {
                    // ✅ Cập nhật paymentMethod cho tất cả orders trong session
                    updateOrdersQuery += ", o.paymentMethod = :paymentMethod";
                }
                updateOrdersQuery += " WHERE o.session.sessionId = :sessionId";
                Query updateQuery = em.createQuery(updateOrdersQuery);
                if (paymentMethod != null && !paymentMethod.isEmpty()) {
                    updateQuery.setParameter("paymentMethod", paymentMethod);
                }
                updateQuery.setParameter("sessionId", session.getSessionId());
                updateQuery.executeUpdate();
                System.out.println("✅ Đã cập nhật paymentMethod '" + paymentMethod + "' cho tất cả orders trong session");
                
                // 5. Trừ số lượng sản phẩm trong kho sau khi thanh toán
                // ✅ Nhận orderItems trực tiếp từ request hoặc lấy từ orders trong session

                List<Map<String, Object>> orderItemsFromRequest = (List<Map<String, Object>>) requestData.get("orderItems");
                
                if (orderItemsFromRequest != null && !orderItemsFromRequest.isEmpty()) {
                    // ✅ Trừ stock trực tiếp từ orderItems (giống cơ chế kitchen)
                    System.out.println("📦 Trừ stock từ orderItems trực tiếp: " + orderItemsFromRequest.size() + " món");
                    deductStockFromItems(em, orderItemsFromRequest);
                } else {
                    // ✅ Fallback: Trừ stock từ orders trong session (nếu có)
                    String ordersQuery = "SELECT o FROM Order o WHERE o.session.sessionId = :sessionId";
                    Query ordersQueryObj = em.createQuery(ordersQuery);
                    ordersQueryObj.setParameter("sessionId", session.getSessionId());
                    
    
                    List<com.liteflow.model.inventory.Order> orders = ordersQueryObj.getResultList();
                    
                    if (orders.isEmpty()) {
                        System.out.println("⚠️ Warning: Không tìm thấy orders nào trong session và không có orderItems từ request!");
                        System.out.println("   Stock sẽ không được cập nhật.");
                    } else {
                        System.out.println("📋 Tìm thấy " + orders.size() + " orders trong session để trừ stock");
                        deductStockFromOrders(em, orders);
                    }
                }
                
                em.getTransaction().commit();
                
                // 6. Check stock alerts và gửi Telegram notifications (async)
                try {
    
                    List<Map<String, Object>> orderItemsForAlert = (List<Map<String, Object>>) requestData.get("orderItems");
                    if (orderItemsForAlert == null || orderItemsForAlert.isEmpty()) {
                        // Fallback: Get from orders in session
                        String ordersQuery = "SELECT o FROM Order o WHERE o.session.sessionId = :sessionId";
                        Query ordersQueryObj = em.createQuery(ordersQuery);
                        ordersQueryObj.setParameter("sessionId", session.getSessionId());
        
                        List<com.liteflow.model.inventory.Order> ordersForAlert = ordersQueryObj.getResultList();
                        
                        if (!ordersForAlert.isEmpty()) {
                            orderItemsForAlert = new java.util.ArrayList<>();
                            for (com.liteflow.model.inventory.Order order : ordersForAlert) {
                                for (com.liteflow.model.inventory.OrderDetail detail : order.getOrderDetails()) {
                                    Map<String, Object> item = new java.util.HashMap<>();
                                    item.put("variantId", detail.getProductVariant().getProductVariantId().toString());
                                    item.put("quantity", detail.getQuantity());
                                    orderItemsForAlert.add(item);
                                }
                            }
                        }
                    }
                    
                    if (orderItemsForAlert != null && !orderItemsForAlert.isEmpty()) {
                        com.liteflow.service.inventory.StockAlertService stockAlertService = 
                            new com.liteflow.service.inventory.StockAlertService();
                        stockAlertService.checkAndSendAlertsAfterPayment(orderItemsForAlert, userId);
                        System.out.println("🔔 Stock alert check initiated for " + orderItemsForAlert.size() + " items");
                    }
                } catch (Exception e) {
                    // Don't fail payment if alert check fails
                    System.err.println("⚠️ Warning: Stock alert check failed (payment still successful): " + e.getMessage());
                    e.printStackTrace();
                }
                
                // Trả về response thành công
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Thanh toán thành công!");
                responseData.put("sessionId", session.getSessionId().toString());
                responseData.put("totalAmount", session.getTotalAmount() != null ? session.getTotalAmount().doubleValue() : 0.0);
                
                // ✅ Thêm invoice number nếu có invoice name
                if (session.getInvoiceName() != null && !session.getInvoiceName().isEmpty()) {
                    responseData.put("invoiceNumber", session.getInvoiceName());
                } else if (tableId != null) {
                    // Fallback: tạo invoice number từ table
                    int invoiceNumber = getNextInvoiceNumber(tableId);
                    responseData.put("invoiceNumber", invoiceNumber);
                } else {
                    // Cho bàn đặc biệt
                    responseData.put("invoiceNumber", "SP-" + session.getSessionId().toString().substring(0, 8));
                }
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(responseData));
                
                System.out.println("✅ Checkout thành công cho bàn " + tableId);
                
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            } finally {
                em.close();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi checkout: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "Lỗi server: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
    
    // =============================================
    // UTILITY METHODS
    // =============================================
    
    /**
     * Trừ stock từ danh sách items (variantId, quantity) - giống cơ chế kitchen
     * @param em EntityManager
     * @param items Danh sách items với variantId và quantity
     */
    private void deductStockFromItems(EntityManager em, List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            try {
                String variantIdStr = (String) item.get("variantId");
                if (variantIdStr == null || variantIdStr.isEmpty()) {
                    continue;
                }
                
                UUID productVariantId = UUID.fromString(variantIdStr);
                Integer quantityToDeduct = null;
                
                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Number) {
                    quantityToDeduct = ((Number) qtyObj).intValue();
                } else if (qtyObj instanceof String) {
                    quantityToDeduct = Integer.parseInt((String) qtyObj);
                }
                
                if (quantityToDeduct == null || quantityToDeduct <= 0) {
                    continue;
                }
                
                // Find ProductStock by ProductVariant
                String stockQuery = "SELECT ps FROM ProductStock ps WHERE ps.productVariant.productVariantId = :variantId";
                Query stockQueryObj = em.createQuery(stockQuery);
                stockQueryObj.setParameter("variantId", productVariantId);
                

                List<com.liteflow.model.inventory.ProductStock> productStocks = stockQueryObj.getResultList();
                
                if (!productStocks.isEmpty()) {
                    // Update the first stock record (should be unique per variant)
                    com.liteflow.model.inventory.ProductStock productStock = productStocks.get(0);
                    int currentAmount = productStock.getAmount() != null ? productStock.getAmount() : 0;
                    int newAmount = Math.max(0, currentAmount - quantityToDeduct);
                    
                    System.out.println("📦 Deducting stock for ProductVariant: " + productVariantId);
                    System.out.println("   Current amount: " + currentAmount);
                    System.out.println("   Quantity to deduct: " + quantityToDeduct);
                    System.out.println("   New amount: " + newAmount);
                    
                    productStock.setAmount(newAmount);
                    em.merge(productStock);
                    
                    // Create inventory log for tracking
                    com.liteflow.model.inventory.ProductVariant productVariant = productStock.getProductVariant();
                    com.liteflow.model.inventory.InventoryLog inventoryLog = new com.liteflow.model.inventory.InventoryLog();
                    inventoryLog.setProductVariant(productVariant);
                    inventoryLog.setActionType("Sale");
                    inventoryLog.setQuantityChanged(-quantityToDeduct); // Negative for sale
                    inventoryLog.setActionDate(LocalDateTime.now());
                    inventoryLog.setStoreLocation(productStock.getInventory().getStoreLocation());
                    
                    em.persist(inventoryLog);
                    
                    System.out.println("✅ Stock updated successfully");
                } else {
                    System.out.println("⚠️ No ProductStock found for ProductVariant: " + productVariantId);
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi trừ stock cho item: " + e.getMessage());
                e.printStackTrace();
                // Continue với item tiếp theo
            }
        }
    }
    
    /**
     * Trừ stock từ danh sách orders (fallback method)
     * @param em EntityManager
     * @param orders Danh sách orders
     */
    private void deductStockFromOrders(EntityManager em, List<com.liteflow.model.inventory.Order> orders) {
        for (com.liteflow.model.inventory.Order order : orders) {
            // Fetch order details to avoid LazyInitializationException
            order.getOrderDetails().size(); // This triggers lazy loading
            
            for (com.liteflow.model.inventory.OrderDetail orderDetail : order.getOrderDetails()) {
                // Fetch product variant to avoid LazyInitializationException
                orderDetail.getProductVariant();
                
                // Get the product variant ID and quantity
                UUID productVariantId = orderDetail.getProductVariant().getProductVariantId();
                Integer quantityToDeduct = orderDetail.getQuantity();
                
                if (quantityToDeduct == null || quantityToDeduct <= 0) {
                    continue;
                }
                
                // Find ProductStock by ProductVariant
                String stockQuery = "SELECT ps FROM ProductStock ps WHERE ps.productVariant.productVariantId = :variantId";
                Query stockQueryObj = em.createQuery(stockQuery);
                stockQueryObj.setParameter("variantId", productVariantId);
                

                List<com.liteflow.model.inventory.ProductStock> productStocks = stockQueryObj.getResultList();
                
                if (!productStocks.isEmpty()) {
                    // Update the first stock record (should be unique per variant)
                    com.liteflow.model.inventory.ProductStock productStock = productStocks.get(0);
                    int currentAmount = productStock.getAmount() != null ? productStock.getAmount() : 0;
                    int newAmount = Math.max(0, currentAmount - quantityToDeduct);
                    
                    System.out.println("📦 Deducting stock for ProductVariant: " + productVariantId);
                    System.out.println("   Current amount: " + currentAmount);
                    System.out.println("   Quantity to deduct: " + quantityToDeduct);
                    System.out.println("   New amount: " + newAmount);
                    
                    productStock.setAmount(newAmount);
                    em.merge(productStock);
                    
                    // Create inventory log for tracking
                    com.liteflow.model.inventory.InventoryLog inventoryLog = new com.liteflow.model.inventory.InventoryLog();
                    inventoryLog.setProductVariant(orderDetail.getProductVariant());
                    inventoryLog.setActionType("Sale");
                    inventoryLog.setQuantityChanged(-quantityToDeduct); // Negative for sale
                    inventoryLog.setActionDate(LocalDateTime.now());
                    inventoryLog.setStoreLocation(productStock.getInventory().getStoreLocation());
                    
                    em.persist(inventoryLog);
                    
                    System.out.println("✅ Stock updated successfully");
                } else {
                    System.out.println("⚠️ No ProductStock found for ProductVariant: " + productVariantId);
                }
            }
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        out.print(gson.toJson(errorResponse));
        out.flush();
    }
}

