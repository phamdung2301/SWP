package com.liteflow.dao.sales;

import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * SalesInvoiceDAO - Quản lý hoá đơn bán hàng (Orders)
 * Orders với PaymentStatus = 'Paid' = Hoá đơn bán hàng
 */
public class SalesInvoiceDAO {
    
    protected static EntityManager getEntityManager() {
        return BaseDAO.emf.createEntityManager();
    }
    
    /**
     * Get all sales invoices (paid orders) with pagination
     * @param limit Number of results
     * @param offset Starting position
     * @return List of Orders
     */
    public List<Map<String, Object>> getAllSalesInvoices(int limit, int offset) {
        EntityManager em = getEntityManager();
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            System.out.println("📊 Getting sales invoices (limit: " + limit + ", offset: " + offset + ")");
            
            String jpql = "SELECT o FROM Order o " +
                         "LEFT JOIN FETCH o.session s " +
                         "LEFT JOIN FETCH s.table t " +
                         "LEFT JOIN FETCH t.room r " +
                         "LEFT JOIN FETCH o.createdBy u " +
                         "WHERE o.paymentStatus = 'Paid' " +
                         "ORDER BY o.orderDate DESC";
            
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            
            List<Order> orders = query.getResultList();
            
            for (Order order : orders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", order.getOrderId());
                map.put("orderNumber", order.getOrderNumber());
                map.put("orderDate", order.getOrderDate());
                map.put("totalAmount", order.getTotalAmount());
                // ✅ Lấy paymentMethod từ Order, nếu không có thì lấy từ Session
                String paymentMethod = order.getPaymentMethod();
                if (paymentMethod == null || paymentMethod.isEmpty()) {
                    if (order.getSession() != null) {
                        paymentMethod = order.getSession().getPaymentMethod();
                    }
                }
                map.put("paymentMethod", paymentMethod);
                map.put("paymentStatus", order.getPaymentStatus());
                map.put("status", order.getStatus());
                
                // Customer info
                if (order.getSession() != null) {
                    map.put("customerName", order.getSession().getCustomerName());
                    map.put("customerPhone", order.getSession().getCustomerPhone());
                    
                    if (order.getSession().getTable() != null) {
                        map.put("tableName", order.getSession().getTable().getTableName());
                        if (order.getSession().getTable().getRoom() != null) {
                            map.put("roomName", order.getSession().getTable().getRoom().getName());
                        }
                    }
                }
                
                // Created by
                if (order.getCreatedBy() != null) {
                    map.put("createdByName", order.getCreatedBy().getDisplayName());
                }
                
                map.put("notes", order.getNotes());
                
                result.add(map);
            }
            
            System.out.println("✅ Found " + result.size() + " sales invoices");
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting sales invoices: " + e.getMessage());
            e.printStackTrace();
            return result;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get sales invoices by date range
     */
    public List<Map<String, Object>> getSalesInvoicesByDateRange(LocalDate startDate, LocalDate endDate, int limit, int offset) {
        EntityManager em = getEntityManager();
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            System.out.println("📊 Getting sales invoices from " + startDate + " to " + endDate);
            
            String jpql = "SELECT o FROM Order o " +
                         "LEFT JOIN FETCH o.session s " +
                         "LEFT JOIN FETCH s.table t " +
                         "LEFT JOIN FETCH t.room r " +
                         "LEFT JOIN FETCH o.createdBy u " +
                         "WHERE o.paymentStatus = 'Paid' " +
                         "AND o.orderDate BETWEEN :startDate AND :endDate " +
                         "ORDER BY o.orderDate DESC";
            
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            
            List<Order> orders = query.getResultList();
            
            for (Order order : orders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", order.getOrderId());
                map.put("orderNumber", order.getOrderNumber());
                map.put("orderDate", order.getOrderDate());
                map.put("totalAmount", order.getTotalAmount());
                // ✅ Lấy paymentMethod từ Order, nếu không có thì lấy từ Session
                String paymentMethod = order.getPaymentMethod();
                if (paymentMethod == null || paymentMethod.isEmpty()) {
                    if (order.getSession() != null) {
                        paymentMethod = order.getSession().getPaymentMethod();
                    }
                }
                map.put("paymentMethod", paymentMethod);
                map.put("paymentStatus", order.getPaymentStatus());
                map.put("status", order.getStatus());
                map.put("subTotal", order.getSubTotal());
                map.put("vat", order.getVat());
                map.put("discount", order.getDiscount());
                
                if (order.getSession() != null) {
                    map.put("customerName", order.getSession().getCustomerName());
                    map.put("customerPhone", order.getSession().getCustomerPhone());
                    
                    if (order.getSession().getTable() != null) {
                        map.put("tableName", order.getSession().getTable().getTableName());
                        if (order.getSession().getTable().getRoom() != null) {
                            map.put("roomName", order.getSession().getTable().getRoom().getName());
                        }
                    }
                }
                
                if (order.getCreatedBy() != null) {
                    map.put("createdByName", order.getCreatedBy().getDisplayName());
                }
                
                map.put("notes", order.getNotes());
                
                result.add(map);
            }
            
            System.out.println("✅ Found " + result.size() + " sales invoices in date range");
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting sales invoices by date: " + e.getMessage());
            e.printStackTrace();
            return result;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get sales invoice details with order items
     */
    public Map<String, Object> getSalesInvoiceDetails(UUID orderId) {
        EntityManager em = getEntityManager();
        
        try {
            System.out.println("📋 Getting sales invoice details for: " + orderId);
            
            String jpql = "SELECT o FROM Order o " +
                         "LEFT JOIN FETCH o.session s " +
                         "LEFT JOIN FETCH s.table t " +
                         "LEFT JOIN FETCH t.room r " +
                         "LEFT JOIN FETCH o.createdBy u " +
                         "LEFT JOIN FETCH o.orderDetails od " +
                         "LEFT JOIN FETCH od.productVariant pv " +
                         "LEFT JOIN FETCH pv.product p " +
                         "WHERE o.orderId = :orderId";
            
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("orderId", orderId);
            
            Order order = query.getSingleResult();
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getOrderId());
            result.put("orderNumber", order.getOrderNumber());
            result.put("orderDate", order.getOrderDate());
            result.put("totalAmount", order.getTotalAmount());
            result.put("subTotal", order.getSubTotal());
            result.put("vat", order.getVat());
            result.put("discount", order.getDiscount());
            // ✅ Lấy paymentMethod từ Order, nếu không có thì lấy từ Session
            String paymentMethod = order.getPaymentMethod();
            if (paymentMethod == null || paymentMethod.isEmpty()) {
                if (order.getSession() != null) {
                    paymentMethod = order.getSession().getPaymentMethod();
                }
            }
            result.put("paymentMethod", paymentMethod);
            result.put("paymentStatus", order.getPaymentStatus());
            result.put("status", order.getStatus());
            result.put("notes", order.getNotes());
            
            if (order.getSession() != null) {
                result.put("customerName", order.getSession().getCustomerName());
                result.put("customerPhone", order.getSession().getCustomerPhone());
                
                if (order.getSession().getTable() != null) {
                    result.put("tableName", order.getSession().getTable().getTableName());
                    if (order.getSession().getTable().getRoom() != null) {
                        result.put("roomName", order.getSession().getTable().getRoom().getName());
                    }
                }
            }
            
            if (order.getCreatedBy() != null) {
                result.put("createdByName", order.getCreatedBy().getDisplayName());
            }
            
            // Order items
            List<Map<String, Object>> items = new ArrayList<>();
            if (order.getOrderDetails() != null) {
                order.getOrderDetails().forEach(detail -> {
                    Map<String, Object> item = new HashMap<>();
                    if (detail.getProductVariant() != null && detail.getProductVariant().getProduct() != null) {
                        item.put("productName", detail.getProductVariant().getProduct().getName());
                        item.put("size", detail.getProductVariant().getSize());
                    }
                    item.put("quantity", detail.getQuantity());
                    item.put("unitPrice", detail.getUnitPrice());
                    item.put("totalPrice", detail.getTotalPrice());
                    items.add(item);
                });
            }
            result.put("items", items);
            
            System.out.println("✅ Sales invoice details loaded: " + items.size() + " items");
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting sales invoice details: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total count of sales invoices
     */
    public long getTotalCount() {
        EntityManager em = getEntityManager();
        
        try {
            String jpql = "SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = 'Paid'";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            System.err.println("❌ Error getting sales invoice count: " + e.getMessage());
            return 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Search sales invoices by customer name or phone
     */
    public List<Map<String, Object>> searchSalesInvoices(String keyword, int limit, int offset) {
        EntityManager em = getEntityManager();
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            System.out.println("🔍 Searching sales invoices with keyword: " + keyword);
            
            String jpql = "SELECT o FROM Order o " +
                         "LEFT JOIN FETCH o.session s " +
                         "LEFT JOIN FETCH s.table t " +
                         "LEFT JOIN FETCH t.room r " +
                         "LEFT JOIN FETCH o.createdBy u " +
                         "WHERE o.paymentStatus = 'Paid' " +
                         "AND (LOWER(s.customerName) LIKE LOWER(:keyword) " +
                         "OR s.customerPhone LIKE :keyword " +
                         "OR o.orderNumber LIKE :keyword) " +
                         "ORDER BY o.orderDate DESC";
            
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("keyword", "%" + keyword + "%");
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            
            List<Order> orders = query.getResultList();
            
            for (Order order : orders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", order.getOrderId());
                map.put("orderNumber", order.getOrderNumber());
                map.put("orderDate", order.getOrderDate());
                map.put("totalAmount", order.getTotalAmount());
                // ✅ Lấy paymentMethod từ Order, nếu không có thì lấy từ Session
                String paymentMethod = order.getPaymentMethod();
                if (paymentMethod == null || paymentMethod.isEmpty()) {
                    if (order.getSession() != null) {
                        paymentMethod = order.getSession().getPaymentMethod();
                    }
                }
                map.put("paymentMethod", paymentMethod);
                
                if (order.getSession() != null) {
                    map.put("customerName", order.getSession().getCustomerName());
                    map.put("customerPhone", order.getSession().getCustomerPhone());
                    
                    if (order.getSession().getTable() != null) {
                        map.put("tableName", order.getSession().getTable().getTableName());
                    }
                }
                
                result.add(map);
            }
            
            System.out.println("✅ Found " + result.size() + " matching sales invoices");
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error searching sales invoices: " + e.getMessage());
            e.printStackTrace();
            return result;
        } finally {
            em.close();
        }
    }
}

