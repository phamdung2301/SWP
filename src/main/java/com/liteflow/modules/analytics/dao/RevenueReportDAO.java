package com.liteflow.modules.analytics.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import com.liteflow.modules.reservation.model.HotelReservation;

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
            
            // 1. Restaurant Revenue
            String restaurantJpql = "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid'";
            
            TypedQuery<BigDecimal> restaurantQuery = em.createQuery(restaurantJpql, BigDecimal.class);
            restaurantQuery.setParameter("startDate", startDateTime);
            restaurantQuery.setParameter("endDate", endDateTime);
            BigDecimal restaurantRevenue = restaurantQuery.getSingleResult();
            
            // 2. Hotel Revenue
            String hotelJpql = "SELECT COALESCE(SUM(hr.totalAmount), 0) FROM HotelReservation hr " +
                              "WHERE hr.checkOutDate BETWEEN :startDate AND :endDate " +
                              "AND hr.status = com.liteflow.modules.reservation.model.HotelReservation.Status.CHECKED_OUT";
            
            TypedQuery<BigDecimal> hotelQuery = em.createQuery(hotelJpql, BigDecimal.class);
            hotelQuery.setParameter("startDate", startDate);
            hotelQuery.setParameter("endDate", endDate);
            BigDecimal hotelRevenue = hotelQuery.getSingleResult();
            
            return (restaurantRevenue != null ? restaurantRevenue : BigDecimal.ZERO)
                    .add(hotelRevenue != null ? hotelRevenue : BigDecimal.ZERO);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting total revenue: " + e.getMessage());
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
            
            // 1. Restaurant Orders
            String restaurantJpql = "SELECT COUNT(o) FROM Order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid'";
            
            TypedQuery<Long> restaurantQuery = em.createQuery(restaurantJpql, Long.class);
            restaurantQuery.setParameter("startDate", startDateTime);
            restaurantQuery.setParameter("endDate", endDateTime);
            long restaurantOrders = restaurantQuery.getSingleResult();
            
            // 2. Hotel Reservations
            String hotelJpql = "SELECT COUNT(hr) FROM HotelReservation hr " +
                              "WHERE hr.checkOutDate BETWEEN :startDate AND :endDate " +
                              "AND hr.status = com.liteflow.modules.reservation.model.HotelReservation.Status.CHECKED_OUT";
            
            TypedQuery<Long> hotelQuery = em.createQuery(hotelJpql, Long.class);
            hotelQuery.setParameter("startDate", startDate);
            hotelQuery.setParameter("endDate", endDate);
            long hotelOrders = hotelQuery.getSingleResult();
            
            return restaurantOrders + hotelOrders;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting total orders: " + e.getMessage());
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
            
            // 1. Restaurant Trend
            String restaurantJpql = "SELECT CAST(o.orderDate AS LocalDate), " +
                         "SUM(o.totalAmount), COUNT(o) " +
                         "FROM Order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid' " +
                         "GROUP BY CAST(o.orderDate AS LocalDate)";
            
            TypedQuery<Object[]> restaurantQuery = em.createQuery(restaurantJpql, Object[].class);
            restaurantQuery.setParameter("startDate", startDateTime);
            restaurantQuery.setParameter("endDate", endDateTime);
            List<Object[]> restaurantTrend = restaurantQuery.getResultList();
            
            // 2. Hotel Trend
            String hotelJpql = "SELECT hr.checkOutDate, " +
                              "SUM(hr.totalAmount), COUNT(hr) " +
                              "FROM HotelReservation hr " +
                              "WHERE hr.checkOutDate BETWEEN :startDate AND :endDate " +
                              "AND hr.status = com.liteflow.modules.reservation.model.HotelReservation.Status.CHECKED_OUT " +
                              "GROUP BY hr.checkOutDate";
            
            TypedQuery<Object[]> hotelQuery = em.createQuery(hotelJpql, Object[].class);
            hotelQuery.setParameter("startDate", startDate);
            hotelQuery.setParameter("endDate", endDate);
            List<Object[]> hotelTrend = hotelQuery.getResultList();
            
            // Merge trends using TreeMap to keep dates sorted
            Map<LocalDate, Object[]> mergeMap = new TreeMap<>();
            
            for (Object[] row : restaurantTrend) {
                mergeMap.put((LocalDate) row[0], row);
            }
            
            for (Object[] row : hotelTrend) {
                LocalDate date = (LocalDate) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                Long count = (Long) row[2];
                
                if (mergeMap.containsKey(date)) {
                    Object[] existing = mergeMap.get(date);
                    // Update existing row
                    existing[1] = ((BigDecimal) existing[1]).add(amount != null ? amount : BigDecimal.ZERO);
                    existing[2] = ((Long) existing[2]) + (count != null ? count : 0L);
                } else {
                    mergeMap.put(date, row);
                }
            }
            
            return new ArrayList<>(mergeMap.values());
            
        } catch (Exception e) {
            System.err.println("❌ Error getting daily trend: " + e.getMessage());
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
            
            // 1. Restaurant Revenue by Category
            String restaurantJpql = "SELECT pc.category.name, SUM(od.totalPrice) " +
                         "FROM OrderDetail od " +
                         "JOIN od.productVariant pv " +
                         "JOIN pv.product p " +
                         "JOIN p.productCategories pc " +
                         "JOIN od.order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid' " +
                         "GROUP BY pc.category.name";
            
            TypedQuery<Object[]> restaurantQuery = em.createQuery(restaurantJpql, Object[].class);
            restaurantQuery.setParameter("startDate", startDateTime);
            restaurantQuery.setParameter("endDate", endDateTime);
            List<Object[]> restaurantResults = restaurantQuery.getResultList();

            // 2. Hotel Revenue (as a single category "Phòng khách sạn")
            String hotelJpql = "SELECT 'Phòng khách sạn', SUM(hr.totalAmount) " +
                              "FROM HotelReservation hr " +
                              "WHERE hr.checkOutDate BETWEEN :startDate AND :endDate " +
                              "AND hr.status = :status";
            
            TypedQuery<Object[]> hotelQuery = em.createQuery(hotelJpql, Object[].class);
            hotelQuery.setParameter("startDate", startDate);
            hotelQuery.setParameter("endDate", endDate);
            hotelQuery.setParameter("status", com.liteflow.modules.reservation.model.HotelReservation.Status.CHECKED_OUT);
            List<Object[]> hotelResults = hotelQuery.getResultList();

            // Combine
            List<Object[]> combined = new ArrayList<>(restaurantResults);
            if (!hotelResults.isEmpty() && hotelResults.get(0)[1] != null) {
                BigDecimal hotelTotal = (BigDecimal) hotelResults.get(0)[1];
                if (hotelTotal.compareTo(BigDecimal.ZERO) > 0) {
                    combined.add(hotelResults.get(0));
                }
            }
            
            // Sort by revenue desc
            combined.sort((a, b) -> ((BigDecimal) b[1]).compareTo((BigDecimal) a[1]));
            
            return combined;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting revenue by category: " + e.getMessage());
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
            return 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get returning customers count (for restaurant context, returns 0 as concept doesn't apply)
     */
    @SuppressWarnings("unused")
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
            
            // 1. Restaurant Monthly
            String restaurantJpql = "SELECT YEAR(o.orderDate), MONTH(o.orderDate), " +
                         "COALESCE(SUM(o.totalAmount), 0) " +
                         "FROM Order o " +
                         "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                         "AND o.paymentStatus = 'Paid' " +
                         "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate)";
            
            TypedQuery<Object[]> restaurantQuery = em.createQuery(restaurantJpql, Object[].class);
            restaurantQuery.setParameter("startDate", startDateTime);
            restaurantQuery.setParameter("endDate", endDateTime);
            List<Object[]> restaurantResults = restaurantQuery.getResultList();
            
            // 2. Hotel Monthly
            String hotelJpql = "SELECT YEAR(hr.checkOutDate), MONTH(hr.checkOutDate), " +
                              "COALESCE(SUM(hr.totalAmount), 0) " +
                              "FROM HotelReservation hr " +
                              "WHERE hr.checkOutDate BETWEEN :startDate AND :endDate " +
                              "AND hr.status = com.liteflow.modules.reservation.model.HotelReservation.Status.CHECKED_OUT " +
                              "GROUP BY YEAR(hr.checkOutDate), MONTH(hr.checkOutDate)";
            
            TypedQuery<Object[]> hotelQuery = em.createQuery(hotelJpql, Object[].class);
            hotelQuery.setParameter("startDate", startDate);
            hotelQuery.setParameter("endDate", endDate);
            List<Object[]> hotelResults = hotelQuery.getResultList();
            
            // Merge results
            Map<String, Object[]> mergeMap = new HashMap<>();
            
            for (Object[] row : restaurantResults) {
                String key = row[0] + "-" + String.format("%02d", (Integer)row[1]);
                mergeMap.put(key, row);
            }
            
            for (Object[] row : hotelResults) {
                String key = row[0] + "-" + String.format("%02d", (Integer)row[1]);
                BigDecimal amount = (BigDecimal) row[2];
                
                if (mergeMap.containsKey(key)) {
                    Object[] existing = mergeMap.get(key);
                    existing[2] = ((BigDecimal) existing[2]).add(amount != null ? amount : BigDecimal.ZERO);
                } else {
                    mergeMap.put(key, row);
                }
            }
            
            // Sort and return
            List<String> sortedKeys = new ArrayList<>(mergeMap.keySet());
            Collections.sort(sortedKeys);
            
            List<Object[]> result = new ArrayList<>();
            for (String key : sortedKeys) {
                result.add(mergeMap.get(key));
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting monthly revenue: " + e.getMessage());
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
            
            // Use existing methods to get combined metrics
            BigDecimal todayRevenue = getTotalRevenue(today, today);
            BigDecimal yesterdayRevenue = getTotalRevenue(yesterday, yesterday);
            long todayOrders = getTotalOrders(today, today);
            long yesterdayOrders = getTotalOrders(yesterday, yesterday);
            
            metrics.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
            metrics.put("yesterdayRevenue", yesterdayRevenue != null ? yesterdayRevenue : BigDecimal.ZERO);
            metrics.put("todayOrders", todayOrders);
            metrics.put("yesterdayOrders", yesterdayOrders);
            
            // Calculate average order value
            BigDecimal safeTodayRevenue = todayRevenue != null ? todayRevenue : BigDecimal.ZERO;
            BigDecimal avgOrderValue = todayOrders > 0 ? 
                safeTodayRevenue.divide(BigDecimal.valueOf(todayOrders), 0, java.math.RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            metrics.put("avgOrderValue", avgOrderValue);
            
            // Peak hour and hourly trend (currently only from restaurant orders)
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
            
            String peakHourSql = "SELECT TOP 1 DATEPART(HOUR, o.OrderDate) " +
                                 "FROM Orders o " +
                                 "WHERE o.OrderDate BETWEEN :start AND :end AND o.PaymentStatus = 'Paid' " +
                                 "GROUP BY DATEPART(HOUR, o.OrderDate) " +
                                 "ORDER BY SUM(o.TotalAmount) DESC";
            @SuppressWarnings("unchecked")
            List<Object> peakHourResults = em.createNativeQuery(peakHourSql)
                .setParameter("start", todayStart)
                .setParameter("end", todayEnd)
                .getResultList();
            
            if (!peakHourResults.isEmpty()) {
                Object result = peakHourResults.get(0);
                Integer peakHour = null;
                if (result instanceof Object[]) {
                    peakHour = (Integer) ((Object[]) result)[0];
                } else {
                    peakHour = (Integer) result;
                }
                metrics.put("peakHour", String.format("%02d:00", peakHour));
            } else {
                metrics.put("peakHour", "--:--");
            }
            
            // Hourly revenue trend for today
            String hourlySql = "SELECT DATEPART(HOUR, o.OrderDate), SUM(o.TotalAmount) " +
                               "FROM Orders o " +
                               "WHERE o.OrderDate BETWEEN :start AND :end AND o.PaymentStatus = 'Paid' " +
                               "GROUP BY DATEPART(HOUR, o.OrderDate) " +
                               "ORDER BY DATEPART(HOUR, o.OrderDate)";
            @SuppressWarnings("unchecked")
            List<Object[]> hourlyResults = em.createNativeQuery(hourlySql)
                .setParameter("start", todayStart)
                .setParameter("end", todayEnd)
                .getResultList();
            
            Map<Integer, BigDecimal> hourlyMap = new HashMap<>();
            for (Object[] row : hourlyResults) {
                Integer hour = (Integer) row[0];
                BigDecimal revenue = (BigDecimal) row[1];
                hourlyMap.put(hour, revenue);
            }
            
            List<String> hourLabels = new ArrayList<>();
            List<BigDecimal> hourRevenues = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                hourLabels.add(String.format("%02d:00", i));
                hourRevenues.add(hourlyMap.getOrDefault(i, BigDecimal.ZERO));
            }
            metrics.put("hourlyLabels", hourLabels);
            metrics.put("hourlyRevenues", hourRevenues);
            
            System.out.println("✅ Today's combined metrics: Revenue=" + todayRevenue + 
                             ", Orders=" + todayOrders + 
                             ", Peak=" + metrics.get("peakHour"));
            
        } catch (Exception e) {
            System.err.println("❌ Error getting today's metrics: " + e.getMessage());
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

    /**
     * Get Room Occupancy Summary
     */
    public Map<String, Long> getRoomOccupancySummary() {
        EntityManager em = emf.createEntityManager();
        try {
            Map<String, Long> summary = new HashMap<>();
            
            String jpql = "SELECT hr.status, COUNT(hr) FROM HotelRoom hr GROUP BY hr.status";
            List<Object[]> results = em.createQuery(jpql, Object[].class).getResultList();
            
            long total = 0;
            for (Object[] row : results) {
                com.liteflow.modules.reservation.model.HotelRoom.Status status = 
                    (com.liteflow.modules.reservation.model.HotelRoom.Status) row[0];
                long count = (Long) row[1];
                summary.put(status.name(), count);
                total += count;
            }
            summary.put("TOTAL", total);
            return summary;
        } catch (Exception e) {
            System.err.println("❌ Error getting room occupancy: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        } finally {
            em.close();
        }
    }
}

