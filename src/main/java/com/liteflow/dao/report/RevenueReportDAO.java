package com.liteflow.dao.report;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * DAO for Revenue Report queries
 * Queries Orders, OrderDetails, Products for revenue analytics
 */
public class RevenueReportDAO {
    
    private static EntityManagerFactory emf;
    
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
     * Get total revenue for date range
     */
    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            String jpql = "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid'";
            
            TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            BigDecimal result = query.getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting total revenue: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total order count for date range
     */
    public long getTotalOrders(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            String jpql = "SELECT COUNT(o) FROM Order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid'";
            
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            return query.getSingleResult();
            
        } catch (Exception e) {
            System.err.println("❌ Error getting total orders: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get daily revenue trend
     * Returns: List of [date, revenue, orderCount]
     */
    public List<Object[]> getDailyRevenueTrend(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            String jpql = "SELECT CAST(o.orderDate AS LocalDate), " +
                         "SUM(o.totalAmount), COUNT(o) " +
                         "FROM Order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid' " +
                         "GROUP BY CAST(o.orderDate AS LocalDate) " +
                         "ORDER BY CAST(o.orderDate AS LocalDate)";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            return query.getResultList();
            
        } catch (Exception e) {
            System.err.println("❌ Error getting daily trend: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get hourly revenue distribution
     * Returns: List of [hour, revenue]
     */
    public List<Object[]> getHourlyRevenue(LocalDate date) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = date.atTime(LocalTime.MAX);
            
            // Use native query for HOUR function
            String sql = "SELECT DATEPART(HOUR, o.OrderDate) as Hour, " +
                        "SUM(o.TotalAmount) as Revenue " +
                        "FROM Orders o " +
                        "WHERE o.OrderDate BETWEEN :startDate AND :endDate " +
                        "AND o.PaymentStatus = 'Paid' " +
                        "GROUP BY DATEPART(HOUR, o.OrderDate) " +
                        "ORDER BY DATEPART(HOUR, o.OrderDate)";
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = em.createNativeQuery(sql)
                .setParameter("startDate", startDateTime)
                .setParameter("endDate", endDateTime)
                .getResultList();
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting hourly revenue: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get top selling products
     * Returns: List of [productId, productName, quantity, revenue]
     */
    public List<Object[]> getTopProducts(LocalDate startDate, LocalDate endDate, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            // FIX: Use p.productId (camelCase) and p.name (entity field names)
            String jpql = "SELECT p.productId, p.name, " +
                         "SUM(od.quantity), SUM(od.totalPrice) " +
                         "FROM OrderDetail od " +
                         "JOIN od.productVariant pv " +
                         "JOIN pv.product p " +
                         "JOIN od.order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid' " +
                         "GROUP BY p.productId, p.name " +
                         "ORDER BY SUM(od.totalPrice) DESC";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            query.setMaxResults(limit);
            
            List<Object[]> results = query.getResultList();
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting top products: " + e.getMessage());
            System.err.println("   Exception class: " + e.getClass().getName());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get revenue by product category
     * Returns: List of [categoryName, revenue]
     */
    public List<Object[]> getRevenueByCategory(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            // FIX: Use pc.category.name instead of pc.categoryName
            // ProductCategory entity has 'category' relationship, not direct 'categoryName' field
            String jpql = "SELECT pc.category.name, SUM(od.totalPrice) " +
                         "FROM OrderDetail od " +
                         "JOIN od.productVariant pv " +
                         "JOIN pv.product p " +
                         "JOIN p.productCategories pc " +
                         "JOIN od.order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid' " +
                         "GROUP BY pc.category.name " +
                         "ORDER BY SUM(od.totalPrice) DESC";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            return query.getResultList();
            
        } catch (Exception e) {
            System.err.println("❌ Error getting revenue by category: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get new customers count (first order in period)
     */
    public long getNewCustomers(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            // Count distinct sessions (tables) whose first order is in this period
            String jpql = "SELECT COUNT(DISTINCT o.session) FROM Order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid'";
            
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            return query.getSingleResult();
            
        } catch (Exception e) {
            System.err.println("❌ Error getting new customers: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get returning customers count (for restaurant context, returns 0 as concept doesn't apply)
     */
    public long getReturningCustomers(LocalDate startDate, LocalDate endDate) {
        // Restaurant context: No user tracking, so returning customers concept doesn't apply
        // Return 0 to avoid errors
        return 0;
    }
    
    /**
     * Get peak hour (hour with highest revenue)
     */
    public Integer getPeakHour(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            String sql = "SELECT TOP 1 DATEPART(HOUR, o.OrderDate) as Hour " +
                        "FROM Orders o " +
                        "WHERE o.OrderDate BETWEEN :startDate AND :endDate " +
                        "AND o.PaymentStatus = 'Paid' " +
                        "GROUP BY DATEPART(HOUR, o.OrderDate) " +
                        "ORDER BY SUM(o.TotalAmount) DESC";
            
            @SuppressWarnings("unchecked")
            List<Integer> results = em.createNativeQuery(sql)
                .setParameter("startDate", startDateTime)
                .setParameter("endDate", endDateTime)
                .getResultList();
            
            return results.isEmpty() ? null : results.get(0);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting peak hour: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get revenue for previous period (for comparison)
     */
    public BigDecimal getPreviousPeriodRevenue(LocalDate startDate, LocalDate endDate) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate prevStartDate = startDate.minusDays(days + 1);
        LocalDate prevEndDate = startDate.minusDays(1);
        
        return getTotalRevenue(prevStartDate, prevEndDate);
    }
    
    /**
     * Get total cost of goods sold (COGS) for date range
     * Calculates: SUM(OrderDetail.quantity * ProductVariant.originalPrice)
     */
    public BigDecimal getTotalCostOfGoodsSold(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            // Use JPQL to get all order details and calculate COGS in Java
            // JPQL doesn't always handle Integer * BigDecimal multiplication well
            String jpql = "SELECT od.quantity, pv.originalPrice " +
                         "FROM OrderDetail od " +
                         "JOIN od.order o " +
                         "JOIN od.productVariant pv " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid'";
            
            @SuppressWarnings("unchecked")
            List<Object[]> orderDetails = em.createQuery(jpql, Object[].class)
                .setParameter("startDate", startDateTime)
                .setParameter("endDate", endDateTime)
                .getResultList();
            
            BigDecimal totalCOGS = BigDecimal.ZERO;
            for (Object[] row : orderDetails) {
                Integer quantity = (Integer) row[0];
                BigDecimal originalPrice = (BigDecimal) row[1];
                
                if (quantity != null && originalPrice != null) {
                    BigDecimal itemCOGS = originalPrice.multiply(BigDecimal.valueOf(quantity));
                    totalCOGS = totalCOGS.add(itemCOGS);
                }
            }
            
            return totalCOGS;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting total COGS: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get previous period COGS for comparison
     */
    public BigDecimal getPreviousPeriodCOGS(LocalDate startDate, LocalDate endDate) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate prevStartDate = startDate.minusDays(days + 1);
        LocalDate prevEndDate = startDate.minusDays(1);
        
        return getTotalCostOfGoodsSold(prevStartDate, prevEndDate);
    }
    
    /**
     * Get revenue by weekday (1=Monday, 7=Sunday)
     * Returns: List of [weekday (1-7), revenue, orderCount]
     */
    public List<Object[]> getRevenueByWeekday(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            // Use native query for DATEPART(WEEKDAY, ...)
            // In SQL Server, WEEKDAY returns 1=Sunday, 2=Monday, ..., 7=Saturday
            // We adjust to 1=Monday, 7=Sunday by: ((DATEPART(WEEKDAY, date) + 5) % 7) + 1
            // This formula: Sunday(1)->7, Monday(2)->1, Tuesday(3)->2, ..., Saturday(7)->6
            String sql = "SELECT " +
                        "((DATEPART(WEEKDAY, o.OrderDate) + 5) % 7) + 1 as Weekday, " +
                        "SUM(o.TotalAmount) as Revenue, " +
                        "COUNT(o.OrderID) as OrderCount " +
                        "FROM Orders o " +
                        "WHERE o.OrderDate BETWEEN :startDate AND :endDate " +
                        "AND o.PaymentStatus = 'Paid' " +
                        "GROUP BY ((DATEPART(WEEKDAY, o.OrderDate) + 5) % 7) + 1 " +
                        "ORDER BY Weekday";
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = em.createNativeQuery(sql)
                .setParameter("startDate", startDateTime)
                .setParameter("endDate", endDateTime)
                .getResultList();
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting revenue by weekday: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * DEBUG: Get all orders for today (regardless of payment status)
     * Used to debug why revenue might be showing as 0
     */
    public Map<String, Object> getDebugOrdersToday(LocalDate date) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = date.atTime(LocalTime.MAX);
            
            // Total orders count
            String countJpql = "SELECT COUNT(o) FROM Order o " +
                              "WHERE o.orderDate BETWEEN :startDate AND :endDate";
            Long totalOrders = em.createQuery(countJpql, Long.class)
                .setParameter("startDate", startDateTime)
                .setParameter("endDate", endDateTime)
                .getSingleResult();
            
            // Total revenue (all statuses)
            String revenueJpql = "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
                                "WHERE o.orderDate BETWEEN :startDate AND :endDate";
            BigDecimal totalRevenue = em.createQuery(revenueJpql, BigDecimal.class)
                .setParameter("startDate", startDateTime)
                .setParameter("endDate", endDateTime)
                .getSingleResult();
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("totalOrders", totalOrders);
            debug.put("totalRevenue", totalRevenue);
            debug.put("startDateTime", startDateTime);
            debug.put("endDateTime", endDateTime);
            
            return debug;
            
        } catch (Exception e) {
            System.err.println("❌ Error in debug query: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * 🆕 Get monthly revenue for last N months
     * Returns: List of [year, month, totalRevenue]
     */
    public List<Object[]> getMonthlyRevenue(int numberOfMonths) {
        EntityManager em = emf.createEntityManager();
        try {
            // Calculate start date (N months ago from now)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(numberOfMonths - 1).withDayOfMonth(1);
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            // JPQL: Group by YEAR and MONTH
            String jpql = "SELECT YEAR(o.orderDate), MONTH(o.orderDate), " +
                         "COALESCE(SUM(o.totalAmount), 0) " +
                         "FROM Order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid' " +
                         "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
                         "ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            List<Object[]> results = query.getResultList();
            
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting monthly revenue: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    /**
     * 🆕 Get comprehensive today's metrics for dashboard
     * Includes revenue, orders, customers, peak hour, hourly trend
     */
    public Map<String, Object> getTodayMetrics() {
        EntityManager em = emf.createEntityManager();
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
            LocalDateTime yesterdayStart = yesterday.atStartOfDay();
            LocalDateTime yesterdayEnd = yesterday.atTime(LocalTime.MAX);
            
            // 1. Today's revenue
            String revenueJpql = "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
                                "WHERE o.orderDate BETWEEN :start AND :end AND o.paymentStatus = 'Paid'";
            BigDecimal todayRevenue = em.createQuery(revenueJpql, BigDecimal.class)
                .setParameter("start", todayStart)
                .setParameter("end", todayEnd)
                .getSingleResult();
            metrics.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
            
            // 2. Yesterday's revenue for comparison
            BigDecimal yesterdayRevenue = em.createQuery(revenueJpql, BigDecimal.class)
                .setParameter("start", yesterdayStart)
                .setParameter("end", yesterdayEnd)
                .getSingleResult();
            metrics.put("yesterdayRevenue", yesterdayRevenue != null ? yesterdayRevenue : BigDecimal.ZERO);
            
            // 3. Today's order count
            String orderCountJpql = "SELECT COUNT(o) FROM Order o " +
                                   "WHERE o.orderDate BETWEEN :start AND :end AND o.paymentStatus = 'Paid'";
            Long todayOrders = em.createQuery(orderCountJpql, Long.class)
                .setParameter("start", todayStart)
                .setParameter("end", todayEnd)
                .getSingleResult();
            metrics.put("todayOrders", todayOrders != null ? todayOrders : 0L);
            
            // 4. Yesterday's order count
            Long yesterdayOrders = em.createQuery(orderCountJpql, Long.class)
                .setParameter("start", yesterdayStart)
                .setParameter("end", yesterdayEnd)
                .getSingleResult();
            metrics.put("yesterdayOrders", yesterdayOrders != null ? yesterdayOrders : 0L);
            
            // 5. Today's average order value
            long orderCount = todayOrders != null ? todayOrders : 0L;
            BigDecimal safeTodayRevenue = todayRevenue != null ? todayRevenue : BigDecimal.ZERO;
            BigDecimal avgOrderValue = orderCount > 0 ? 
                safeTodayRevenue.divide(BigDecimal.valueOf(orderCount), 0, java.math.RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            metrics.put("avgOrderValue", avgOrderValue);
            
            // 6. Peak hour (hour with highest revenue)
            String peakHourJpql = "SELECT HOUR(o.orderDate), SUM(o.totalAmount) FROM Order o " +
                                 "WHERE o.orderDate BETWEEN :start AND :end AND o.paymentStatus = 'Paid' " +
                                 "GROUP BY HOUR(o.orderDate) " +
                                 "ORDER BY SUM(o.totalAmount) DESC";
            List<Object[]> peakHourResults = em.createQuery(peakHourJpql, Object[].class)
                .setParameter("start", todayStart)
                .setParameter("end", todayEnd)
                .setMaxResults(1)
                .getResultList();
            
            if (!peakHourResults.isEmpty()) {
                Integer peakHour = (Integer) peakHourResults.get(0)[0];
                metrics.put("peakHour", String.format("%02d:00", peakHour));
            } else {
                metrics.put("peakHour", "--:--");
            }
            
            // 7. Hourly revenue trend for today (mini chart data)
            String hourlyJpql = "SELECT HOUR(o.orderDate), COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
                               "WHERE o.orderDate BETWEEN :start AND :end AND o.paymentStatus = 'Paid' " +
                               "GROUP BY HOUR(o.orderDate) " +
                               "ORDER BY HOUR(o.orderDate)";
            List<Object[]> hourlyResults = em.createQuery(hourlyJpql, Object[].class)
                .setParameter("start", todayStart)
                .setParameter("end", todayEnd)
                .getResultList();
            
            // Create hourly map (0-23)
            Map<Integer, BigDecimal> hourlyMap = new HashMap<>();
            for (Object[] row : hourlyResults) {
                Integer hour = (Integer) row[0];
                BigDecimal revenue = (BigDecimal) row[1];
                hourlyMap.put(hour, revenue);
            }
            
            // Fill missing hours with 0
            List<String> hourLabels = new ArrayList<>();
            List<BigDecimal> hourRevenues = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                hourLabels.add(String.format("%02d:00", i));
                hourRevenues.add(hourlyMap.getOrDefault(i, BigDecimal.ZERO));
            }
            metrics.put("hourlyLabels", hourLabels);
            metrics.put("hourlyRevenues", hourRevenues);
            
            System.out.println("✅ Today's metrics: Revenue=" + todayRevenue + 
                             ", Orders=" + todayOrders + 
                             ", Peak=" + metrics.get("peakHour"));
            
        } catch (Exception e) {
            System.err.println("❌ Error getting today's metrics: " + e.getMessage());
            e.printStackTrace();
            // Return empty/default values
            metrics.put("todayRevenue", BigDecimal.ZERO);
            metrics.put("yesterdayRevenue", BigDecimal.ZERO);
            metrics.put("todayOrders", 0L);
            metrics.put("yesterdayOrders", 0L);
            metrics.put("avgOrderValue", BigDecimal.ZERO);
            metrics.put("peakHour", "--:--");
            metrics.put("hourlyLabels", new ArrayList<>());
            metrics.put("hourlyRevenues", new ArrayList<>());
        } finally {
            em.close();
        }
        
        return metrics;
    }
}

