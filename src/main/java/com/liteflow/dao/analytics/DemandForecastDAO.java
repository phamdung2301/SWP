package com.liteflow.dao.analytics;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * DemandForecastDAO - Truy vấn dữ liệu cho Demand Forecasting
 * Phân tích lịch sử bán hàng, tồn kho, xu hướng
 */
public class DemandForecastDAO {
    
    protected static EntityManagerFactory emf;
    
    static {
        try {
            emf = Persistence.createEntityManagerFactory("LiteFlowPU");
        } catch (Throwable e) {
            System.err.println("❌ Failed to create EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            emf = null;
        }
    }

    /**
     * Get sales history for products
     * @param days Number of days to look back
     * @return List of [ProductVariantID, ProductName, Size, TotalSold, TotalRevenue, DaysWithSales]
     */
    public List<Object[]> getSalesHistory(int days) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();
            LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);
            
            System.out.println("📊 Getting sales history for last " + days + " days...");
            
            String jpql = "SELECT pv.productVariantId, p.name, pv.size, " +
                         "SUM(od.quantity), SUM(od.totalPrice), " +
                         "COUNT(DISTINCT CAST(o.orderDate AS date)) " +
                         "FROM OrderDetail od " +
                         "JOIN od.productVariant pv " +
                         "JOIN pv.product p " +
                         "JOIN od.order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid' " +
                         "GROUP BY pv.productVariantId, p.name, pv.size " +
                         "ORDER BY SUM(od.totalPrice) DESC";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            List<Object[]> results = query.getResultList();
            System.out.println("✅ Found sales data for " + results.size() + " product variants");
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting sales history: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get current stock levels
     * @return List of [ProductVariantID, ProductName, Size, CurrentStock]
     */
    public List<Object[]> getCurrentStockLevels() {
        EntityManager em = emf.createEntityManager();
        try {
            System.out.println("📦 Getting current stock levels...");
            
            String jpql = "SELECT pv.productVariantId, p.name, pv.size, " +
                         "COALESCE(SUM(ps.amount), 0) " +
                         "FROM ProductVariant pv " +
                         "JOIN pv.product p " +
                         "LEFT JOIN pv.productStocks ps " +
                         "WHERE pv.isDeleted = false AND p.isDeleted = false " +
                         "GROUP BY pv.productVariantId, p.name, pv.size " +
                         "ORDER BY p.name, pv.size";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            List<Object[]> results = query.getResultList();
            
            System.out.println("✅ Found stock data for " + results.size() + " product variants");
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting stock levels: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get recent purchase orders to calculate lead time
     * @return List of [POID, CreateDate, ExpectedDelivery, Status]
     */
    public List<Object[]> getRecentPurchaseOrders(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            System.out.println("🚚 Getting recent purchase orders...");
            
            String jpql = "SELECT po.poid, po.createDate, po.expectedDelivery, po.status " +
                         "FROM PurchaseOrder po " +
                         "WHERE po.status IN ('COMPLETED', 'RECEIVING') " +
                         "AND po.expectedDelivery IS NOT NULL " +
                         "ORDER BY po.createDate DESC";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setMaxResults(limit);
            
            List<Object[]> results = query.getResultList();
            System.out.println("✅ Found " + results.size() + " recent purchase orders");
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting purchase orders: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get top revenue products (for prioritization)
     * @param days Number of days to analyze
     * @param limit Top N products
     * @return List of [ProductVariantID, ProductName, Size, Revenue, QuantitySold]
     */
    public List<Object[]> getTopRevenueProducts(int days, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();
            LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);
            
            System.out.println("💰 Getting top " + limit + " revenue products...");
            
            String jpql = "SELECT pv.productVariantId, p.name, pv.size, " +
                         "SUM(od.totalPrice), SUM(od.quantity) " +
                         "FROM OrderDetail od " +
                         "JOIN od.productVariant pv " +
                         "JOIN pv.product p " +
                         "JOIN od.order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid' " +
                         "GROUP BY pv.productVariantId, p.name, pv.size " +
                         "ORDER BY SUM(od.totalPrice) DESC";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            query.setMaxResults(limit);
            
            List<Object[]> results = query.getResultList();
            System.out.println("✅ Found top " + results.size() + " revenue products");
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting top revenue products: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get weekly sales trend (for seasonality analysis)
     * @param weeks Number of weeks to analyze
     * @param productVariantId Specific product variant (null for all)
     * @return List of [WeekNumber, Year, TotalSold]
     */
    public List<Object[]> getWeeklySalesTrend(int weeks, UUID productVariantId) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDate = LocalDate.now().minusWeeks(weeks).atStartOfDay();
            LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);
            
            System.out.println("📈 Getting weekly sales trend...");
            
            String jpql;
            if (productVariantId != null) {
                jpql = "SELECT FUNCTION('DATEPART', 'week', o.orderDate), " +
                       "FUNCTION('YEAR', o.orderDate), SUM(od.quantity) " +
                       "FROM OrderDetail od " +
                       "JOIN od.order o " +
                       "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                       "AND o.paymentStatus = 'Paid' " +
                       "AND od.productVariant.productVariantId = :variantId " +
                       "GROUP BY FUNCTION('DATEPART', 'week', o.orderDate), FUNCTION('YEAR', o.orderDate) " +
                       "ORDER BY FUNCTION('YEAR', o.orderDate), FUNCTION('DATEPART', 'week', o.orderDate)";
            } else {
                jpql = "SELECT FUNCTION('DATEPART', 'week', o.orderDate), " +
                       "FUNCTION('YEAR', o.orderDate), SUM(od.quantity) " +
                       "FROM OrderDetail od " +
                       "JOIN od.order o " +
                       "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                       "AND o.paymentStatus = 'Paid' " +
                       "GROUP BY FUNCTION('DATEPART', 'week', o.orderDate), FUNCTION('YEAR', o.orderDate) " +
                       "ORDER BY FUNCTION('YEAR', o.orderDate), FUNCTION('DATEPART', 'week', o.orderDate)";
            }
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            if (productVariantId != null) {
                query.setParameter("variantId", productVariantId);
            }
            
            List<Object[]> results = query.getResultList();
            System.out.println("✅ Found trend data for " + results.size() + " weeks");
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting weekly trend: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get products with low stock (below threshold)
     * @param threshold Stock threshold
     * @return List of [ProductVariantID, ProductName, Size, CurrentStock, Price]
     */
    public List<Object[]> getLowStockProducts(int threshold) {
        EntityManager em = emf.createEntityManager();
        try {
            System.out.println("⚠️ Getting low stock products (threshold: " + threshold + ")...");
            
            String jpql = "SELECT pv.productVariantId, p.name, pv.size, " +
                         "COALESCE(SUM(ps.amount), 0), pv.price " +
                         "FROM ProductVariant pv " +
                         "JOIN pv.product p " +
                         "LEFT JOIN pv.productStocks ps " +
                         "WHERE pv.isDeleted = false AND p.isDeleted = false " +
                         "GROUP BY pv.productVariantId, p.name, pv.size, pv.price " +
                         "HAVING COALESCE(SUM(ps.amount), 0) <= :threshold " +
                         "ORDER BY COALESCE(SUM(ps.amount), 0) ASC";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("threshold", (long) threshold);
            
            List<Object[]> results = query.getResultList();
            System.out.println("✅ Found " + results.size() + " products with low stock");
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting low stock products: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
}

