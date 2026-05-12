package com.liteflow.dao.inventory;

import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.*;
import com.liteflow.model.auth.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class OrderDAO {
    
    /**
     * Tạo order mới với danh sách items
     * @param tableId ID của bàn
     * @param items Danh sách món (productVariantId, quantity, unitPrice)
     * @param userId ID của user tạo order
     * @param invoiceName Tên hóa đơn (Bàn X - HD Y)
     * @param orderNote Ghi chú đơn hàng
     * @return UUID của order được tạo
     */
    public Map<String, Object> createOrder(UUID tableId, List<Map<String, Object>> items, UUID userId, String invoiceName, String orderNote) {
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            em.getTransaction().begin();
            
            // 1. Kiểm tra xem bàn có session đang active không
            TableSession session = findOrCreateActiveSession(em, tableId, userId, invoiceName);
            
            // 2. Tạo order number
            String orderNumber = generateOrderNumber(em);
            
            // 3. Tạo Order entity
            Order order = new Order();
            order.setOrderId(UUID.randomUUID());
            order.setSession(session);
            order.setOrderNumber(orderNumber);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("Pending"); // Trạng thái chờ làm
            order.setPaymentStatus("Unpaid");
            
            // Tính toán tổng tiền
            BigDecimal subtotal = BigDecimal.ZERO;
            
            // 4. Tạo OrderDetails
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (Map<String, Object> item : items) {
                String variantIdStr = (String) item.get("variantId");
                if (variantIdStr == null || variantIdStr.isEmpty()) {
                    continue;
                }
                
                UUID variantId = UUID.fromString(variantIdStr);
                Integer quantity = ((Number) item.get("quantity")).intValue();
                
                // ✅ Validate quantity
                if (quantity == null || quantity <= 0) {
                    throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
                }
                
                String note = (String) item.get("note"); // Ghi chú từ cashier
                
                // Lấy ProductVariant
                ProductVariant variant = em.find(ProductVariant.class, variantId);
                if (variant == null) {
                    throw new RuntimeException("Không tìm thấy sản phẩm variant: " + variantId);
                }
                
                // ✅ Lấy unitPrice từ variant hoặc từ request (nếu có)
                BigDecimal unitPrice;
                if (item.get("unitPrice") != null) {
                    // Nếu có unitPrice trong request, dùng nó
                    unitPrice = new BigDecimal(item.get("unitPrice").toString());
                } else {
                    // Nếu không có, lấy từ ProductVariant
                    unitPrice = variant.getPrice();
                    if (unitPrice == null) {
                        throw new RuntimeException("Sản phẩm variant không có giá: " + variantId);
                    }
                }
                
                OrderDetail detail = new OrderDetail();
                detail.setOrderDetailId(UUID.randomUUID());
                detail.setOrder(order);
                detail.setProductVariant(variant);
                detail.setQuantity(quantity);
                detail.setUnitPrice(unitPrice);
                detail.calculateTotalPrice();
                
                // ✅ Set status từ item hoặc mặc định "Pending"
                String itemStatus = (String) item.get("status");
                if (itemStatus != null && !itemStatus.trim().isEmpty()) {
                    detail.setStatus(itemStatus);
                } else {
                    detail.setStatus("Pending"); // Món đang chờ làm
                }
                
                // Set ghi chú nếu có
                if (note != null && !note.trim().isEmpty()) {
                    detail.setSpecialInstructions(note.trim());
                }
                
                orderDetails.add(detail);
                subtotal = subtotal.add(detail.getTotalPrice());
            }
            
            // 5. Tính VAT và tổng tiền
            BigDecimal vat = subtotal.multiply(new BigDecimal("0.10")); // 10% VAT
            BigDecimal total = subtotal.add(vat);
            
            order.setSubTotal(subtotal);
            order.setVat(vat);
            order.setTotalAmount(total);
            order.setOrderDetails(orderDetails);
            
            // ✅ Set ghi chú đơn hàng nếu có
            if (orderNote != null && !orderNote.trim().isEmpty()) {
                order.setNotes(orderNote.trim());
                System.out.println("📝 Lưu ghi chú đơn hàng: " + orderNote.trim());
            }
            
            // Set created by
            if (userId != null) {
                User user = em.find(User.class, userId);
                order.setCreatedBy(user);
            }
            
            // 6. Persist order (cascade sẽ persist orderDetails)
            em.persist(order);
            
            // 7. Update session total
            updateSessionTotal(em, session);
            
            em.getTransaction().commit();
            
            System.out.println("✅ Đã tạo order: " + orderNumber + " với " + orderDetails.size() + " món");
            
            // ✅ Trả về cả orderId, orderNumber và sessionId
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getOrderId());
            result.put("orderNumber", orderNumber);
            result.put("sessionId", session.getSessionId());
            return result;
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Lỗi khi tạo order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo order: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Tìm hoặc tạo session active cho bàn
     */
    private TableSession findOrCreateActiveSession(EntityManager em, UUID tableId, UUID userId, String invoiceName) {
        // ✅ Với bàn đặc biệt (tableId = null), tạo session mới mỗi lần
        if (tableId == null) {
            TableSession session = new TableSession();
            session.setSessionId(UUID.randomUUID());
            session.setTable(null); // Special table không cần table entity
            session.setCheckInTime(LocalDateTime.now());
            session.setStatus("Active");
            session.setPaymentStatus("Unpaid");
            
            // ✅ Set invoice name
            if (invoiceName != null && !invoiceName.isEmpty()) {
                session.setInvoiceName(invoiceName);
            }
            
            if (userId != null) {
                User user = em.find(User.class, userId);
                session.setCreatedBy(user);
            }
            
            em.persist(session);
            return session;
        }
        
        // Tìm session đang active cho bàn thường
        Query query = em.createQuery(
            "SELECT s FROM TableSession s WHERE s.table.tableId = :tableId AND s.status = 'Active'"
        );
        query.setParameter("tableId", tableId);
        
        @SuppressWarnings("unchecked")
        List<TableSession> sessions = query.getResultList();
        
        if (!sessions.isEmpty()) {
            // ✅ Update invoice name nếu đã có session (case: gọi thêm món)
            TableSession existingSession = sessions.get(0);
            if (invoiceName != null && !invoiceName.isEmpty()) {
                existingSession.setInvoiceName(invoiceName);
                em.merge(existingSession);
            }
            
            // ✅ Đảm bảo table status là "Occupied" nếu có session active
            Table table = existingSession.getTable();
            if (table != null && !"Occupied".equals(table.getStatus())) {
                table.setStatus("Occupied");
                em.merge(table);
                em.flush();
                System.out.println("✅ Updated table status to Occupied for existing session");
            }
            
            return existingSession;
        }
        
        // Tạo session mới cho bàn thường
        Table table = em.find(Table.class, tableId);
        if (table == null) {
            throw new RuntimeException("Không tìm thấy bàn: " + tableId);
        }
        
        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(table);
        session.setCheckInTime(LocalDateTime.now());
        session.setStatus("Active");
        session.setPaymentStatus("Unpaid");
        
        // ✅ Set invoice name
        if (invoiceName != null && !invoiceName.isEmpty()) {
            session.setInvoiceName(invoiceName);
        }
        
        if (userId != null) {
            User user = em.find(User.class, userId);
            session.setCreatedBy(user);
        }
        
        em.persist(session);
        
        // ✅ Update table status to Occupied
        table.setStatus("Occupied");
        em.merge(table);
        em.flush(); // ✅ Flush để đảm bảo table status được persist
        
        return session;
    }
    
    /**
     * Tạo order number tự động
     */
    private String generateOrderNumber(EntityManager em) {
        // Lấy order count trong ngày (SQL Server compatible)
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        
        Query query = em.createQuery(
            "SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startOfDay AND o.orderDate <= :endOfDay"
        );
        query.setParameter("startOfDay", startOfDay);
        query.setParameter("endOfDay", endOfDay);
        
        Long count = (Long) query.getSingleResult();
        
        String date = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        return "ORD" + date + String.format("%03d", count + 1);
    }
    
    /**
     * Cập nhật tổng tiền của session
     */
    private void updateSessionTotal(EntityManager em, TableSession session) {
        Query query = em.createQuery(
            "SELECT SUM(o.totalAmount) FROM Order o WHERE o.session.sessionId = :sessionId"
        );
        query.setParameter("sessionId", session.getSessionId());
        BigDecimal total = (BigDecimal) query.getSingleResult();
        
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        
        session.setTotalAmount(total);
        em.merge(session);
    }
    
    /**
     * Lấy orders của bàn/session hiện tại (cho cashier)
     */
    public List<Map<String, Object>> getOrdersByTable(UUID tableId) {
        EntityManager em = BaseDAO.emf.createEntityManager();
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // Tìm session active của bàn
            String sessionQuery = "SELECT s FROM TableSession s WHERE s.table.tableId = :tableId AND s.status = 'Active'";
            Query query = em.createQuery(sessionQuery);
            query.setParameter("tableId", tableId);
            
            @SuppressWarnings("unchecked")
            List<TableSession> sessions = query.getResultList();
            
            if (sessions.isEmpty()) {
                return result; // Không có session active
            }
            
            TableSession session = sessions.get(0);
            
            // Lấy tất cả orders của session (trừ Cancelled)
            // GIỮ LẠI cả Served để cashier vẫn hiển thị
            String jpql = "SELECT o FROM Order o " +
                         "LEFT JOIN FETCH o.orderDetails od " +
                         "LEFT JOIN FETCH od.productVariant pv " +
                         "LEFT JOIN FETCH pv.product p " +
                         "WHERE o.session.sessionId = :sessionId " +
                         "AND o.status != 'Cancelled' " +
                         "ORDER BY o.orderDate ASC";
            
            Query orderQuery = em.createQuery(jpql);
            orderQuery.setParameter("sessionId", session.getSessionId());
            
            @SuppressWarnings("unchecked")
            List<Order> orders = orderQuery.getResultList();
            
            for (Order order : orders) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("orderId", order.getOrderId().toString());
                    itemMap.put("orderDetailId", detail.getOrderDetailId().toString());
                    itemMap.put("variantId", detail.getProductVariant().getProductVariantId().toString());
                    itemMap.put("productId", detail.getProductVariant().getProduct().getProductId().toString());
                    itemMap.put("name", detail.getProductVariant().getProduct().getName());
                    itemMap.put("size", detail.getProductVariant().getSize());
                    itemMap.put("price", detail.getUnitPrice().doubleValue());
                    itemMap.put("quantity", detail.getQuantity());
                    itemMap.put("status", detail.getStatus());
                    itemMap.put("note", detail.getSpecialInstructions()); // Ghi chú
                    result.add(itemMap);
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy orders của bàn: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        
        return result;
    }
    
    /**
     * Lấy danh sách orders đang pending (cho màn hình bếp)
     */
    public List<Map<String, Object>> getPendingOrders() {
        EntityManager em = BaseDAO.emf.createEntityManager();
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            String jpql = "SELECT o FROM Order o " +
                         "LEFT JOIN FETCH o.orderDetails od " +
                         "LEFT JOIN FETCH od.productVariant pv " +
                         "LEFT JOIN FETCH pv.product p " +
                         "WHERE o.status IN ('Pending', 'Preparing', 'Ready') " +
                         "ORDER BY o.orderDate ASC";
            
            Query query = em.createQuery(jpql);
            @SuppressWarnings("unchecked")
            List<Order> orders = query.getResultList();
            
            for (Order order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("orderId", order.getOrderId().toString());
                orderMap.put("orderNumber", order.getOrderNumber());
                orderMap.put("orderDate", order.getOrderDate().toString());
                orderMap.put("status", order.getStatus());
                
                // ✅ Xử lý tableName với bàn đặc biệt (table = null)
                String tableName = "Mang về / Giao hàng"; // Default cho bàn đặc biệt
                Table table = order.getSession().getTable();
                if (table != null) {
                    tableName = table.getTableNumber();
                }
                orderMap.put("tableName", tableName);
                
                List<Map<String, Object>> items = new ArrayList<>();
                for (OrderDetail detail : order.getOrderDetails()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    String productName = detail.getProductVariant().getProduct().getName();
                    String size = detail.getProductVariant().getSize();
                    
                    // Thêm size vào tên nếu có
                    if (size != null && !size.isEmpty()) {
                        productName = productName + " (" + size + ")";
                    }
                    
                    itemMap.put("productName", productName);
                    itemMap.put("quantity", detail.getQuantity());
                    itemMap.put("status", detail.getStatus());
                    itemMap.put("note", detail.getSpecialInstructions()); // Ghi chú
                    items.add(itemMap);
                }
                orderMap.put("items", items);
                
                result.add(orderMap);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy pending orders: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        
        return result;
    }
    
    /**
     * Cập nhật trạng thái order
     */
    public boolean updateOrderStatus(UUID orderId, String status) {
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            em.getTransaction().begin();
            
            Order order = em.find(Order.class, orderId);
            if (order == null) {
                return false;
            }
            
            order.setStatus(status);
            
            // Cập nhật trạng thái của tất cả order details
            // ✅ Fetch order details để tránh LazyInitializationException
            order.getOrderDetails().size(); // Trigger lazy loading
            for (OrderDetail detail : order.getOrderDetails()) {
                detail.setStatus(status);
            }
            
            em.merge(order);
            em.flush(); // ✅ Flush để đảm bảo changes được persist
            em.getTransaction().commit();
            
            System.out.println("✅ Đã cập nhật trạng thái order " + order.getOrderNumber() + " thành " + status);
            return true;
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Lỗi khi cập nhật trạng thái order: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}

