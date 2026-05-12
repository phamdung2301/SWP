package com.liteflow.controller.payment;

import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.*;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Servlet to get payment transaction details and order information
 * GET /api/payment/result?transactionId=xxx
 */
@WebServlet("/api/payment/result")
public class PaymentResultServlet extends HttpServlet {
    
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String transactionIdStr = request.getParameter("transactionId");
        
        if (transactionIdStr == null || transactionIdStr.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("success", false, "message", "Transaction ID is required")));
            return;
        }
        
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            UUID transactionId = UUID.fromString(transactionIdStr);
            
            // Get payment transaction with session and orders
            String jpql = "SELECT pt FROM PaymentTransaction pt " +
                         "LEFT JOIN FETCH pt.session s " +
                         "LEFT JOIN FETCH s.table t " +
                         "LEFT JOIN FETCH t.room r " +
                         "WHERE pt.transactionId = :transactionId";
            
            Query query = em.createQuery(jpql, PaymentTransaction.class);
            query.setParameter("transactionId", transactionId);
            
            PaymentTransaction transaction = null;
            try {
                @SuppressWarnings("unchecked")
                List<PaymentTransaction> results = query.getResultList();
                if (!results.isEmpty()) {
                    transaction = results.get(0);
                }
            } catch (Exception e) {
                System.err.println("Error getting transaction: " + e.getMessage());
                e.printStackTrace();
            }
            
            if (transaction == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(Map.of("success", false, "message", "Transaction not found")));
                return;
            }
            
            // Get session orders
            TableSession session = transaction.getSession();
            UUID sessionId = session.getSessionId();
            
            String ordersJpql = "SELECT o FROM Order o " +
                               "LEFT JOIN FETCH o.orderDetails od " +
                               "LEFT JOIN FETCH od.productVariant pv " +
                               "LEFT JOIN FETCH pv.product p " +
                               "WHERE o.session.sessionId = :sessionId " +
                               "ORDER BY o.orderDate";
            
            Query ordersQuery = em.createQuery(ordersJpql, Order.class);
            ordersQuery.setParameter("sessionId", sessionId);
            
            @SuppressWarnings("unchecked")
            List<Order> orders = ordersQuery.getResultList();
            
            // Build response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            
            // Transaction info
            Map<String, Object> transactionInfo = new HashMap<>();
            transactionInfo.put("transactionId", transaction.getTransactionId().toString());
            transactionInfo.put("amount", transaction.getAmount());
            transactionInfo.put("paymentMethod", transaction.getPaymentMethod());
            transactionInfo.put("paymentStatus", transaction.getPaymentStatus());
            transactionInfo.put("processedAt", transaction.getProcessedAt() != null ? 
                transaction.getProcessedAt().toString() : null);
            transactionInfo.put("vnpayTransactionNo", transaction.getVnpayTransactionNo());
            transactionInfo.put("vnpayResponseCode", transaction.getVnpayResponseCode());
            result.put("transaction", transactionInfo);
            
            // Session info
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("sessionId", session.getSessionId().toString());
            sessionInfo.put("invoiceName", session.getInvoiceName());
            sessionInfo.put("totalAmount", session.getTotalAmount());
            sessionInfo.put("checkInTime", session.getCheckInTime() != null ? 
                session.getCheckInTime().toString() : null);
            sessionInfo.put("checkOutTime", session.getCheckOutTime() != null ? 
                session.getCheckOutTime().toString() : null);
            
            // Table info
            if (session.getTable() != null) {
                Map<String, Object> tableInfo = new HashMap<>();
                tableInfo.put("tableId", session.getTable().getTableId().toString());
                tableInfo.put("tableName", session.getTable().getTableName());
                if (session.getTable().getRoom() != null) {
                    tableInfo.put("roomName", session.getTable().getRoom().getName());
                }
                sessionInfo.put("table", tableInfo);
            } else {
                // Special table (takeaway/delivery)
                sessionInfo.put("table", null);
            }
            
            result.put("session", sessionInfo);
            
            // Orders and items
            List<Map<String, Object>> orderList = new ArrayList<>();
            List<Map<String, Object>> allItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (Order order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("orderId", order.getOrderId().toString());
                orderMap.put("orderNumber", order.getOrderNumber());
                orderMap.put("orderDate", order.getOrderDate() != null ? 
                    order.getOrderDate().toString() : null);
                orderMap.put("totalAmount", order.getTotalAmount());
                
                totalAmount = totalAmount.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
                
                // Order details
                List<Map<String, Object>> items = new ArrayList<>();
                if (order.getOrderDetails() != null) {
                    for (OrderDetail detail : order.getOrderDetails()) {
                        Map<String, Object> item = new HashMap<>();
                        if (detail.getProductVariant() != null) {
                            if (detail.getProductVariant().getProduct() != null) {
                                item.put("productName", detail.getProductVariant().getProduct().getName());
                            }
                            item.put("size", detail.getProductVariant().getSize());
                        }
                        item.put("quantity", detail.getQuantity());
                        item.put("unitPrice", detail.getUnitPrice());
                        item.put("totalPrice", detail.getTotalPrice());
                        item.put("note", detail.getSpecialInstructions());
                        items.add(item);
                        allItems.add(item);
                    }
                }
                orderMap.put("items", items);
                orderList.add(orderMap);
            }
            
            result.put("orders", orderList);
            result.put("items", allItems);
            result.put("totalAmount", totalAmount);
            
            response.getWriter().write(gson.toJson(result));
            
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("success", false, "message", "Invalid transaction ID format")));
        } catch (Exception e) {
            System.err.println("❌ Error getting payment result: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("success", false, "message", "Error getting payment result")));
        } finally {
            em.close();
        }
    }
}

