package com.liteflow.service.report;

import com.liteflow.dao.report.RevenueReportDAO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Revenue Report Service
 * Business logic for revenue analytics
 */
public class RevenueReportService {
    
    private final RevenueReportDAO reportDAO;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    
    public RevenueReportService() {
        this.reportDAO = new RevenueReportDAO();
    }
    
    /**
     * Generate complete revenue report
     */
    public JSONObject generateReport(LocalDate startDate, LocalDate endDate) {
        JSONObject report = new JSONObject();
        
        try {
            System.out.println("📊 Generating revenue report from " + startDate + " to " + endDate);
            
            // Get basic statistics
            BigDecimal totalRevenue = reportDAO.getTotalRevenue(startDate, endDate);
            long totalOrders = reportDAO.getTotalOrders(startDate, endDate);
            
            // Calculate average order value
            BigDecimal avgOrderValue = totalOrders > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            // Get previous period for comparison
            BigDecimal prevRevenue = reportDAO.getPreviousPeriodRevenue(startDate, endDate);
            double growthRate = calculateGrowthRate(totalRevenue, prevRevenue);
            
            // Get customer statistics
            long newCustomers = reportDAO.getNewCustomers(startDate, endDate);
            long returningCustomers = reportDAO.getReturningCustomers(startDate, endDate);
            
            // Get total cost of goods sold (COGS) - with error handling
            BigDecimal totalCOGS = BigDecimal.ZERO;
            BigDecimal totalProfit = totalRevenue; // Default to revenue if COGS fails
            double profitGrowthRate = 0.0;
            
            try {
                totalCOGS = reportDAO.getTotalCostOfGoodsSold(startDate, endDate);
                
                // Calculate total profit = Revenue - COGS
                totalProfit = totalRevenue.subtract(totalCOGS);
                
                // Get previous period COGS for profit comparison
                BigDecimal prevCOGS = reportDAO.getPreviousPeriodCOGS(startDate, endDate);
                BigDecimal prevProfit = prevRevenue.subtract(prevCOGS);
                profitGrowthRate = calculateGrowthRate(totalProfit, prevProfit);
            } catch (Exception e) {
                System.err.println("❌ Error calculating COGS/Profit: " + e.getMessage());
                e.printStackTrace();
                // Use default values already set above
            }
            
            // Get peak hour
            Integer peakHour = reportDAO.getPeakHour(startDate, endDate);
            
            // Build summary
            report.put("totalRevenue", totalRevenue.doubleValue());
            report.put("totalOrders", totalOrders);
            report.put("avgOrderValue", avgOrderValue.doubleValue());
            report.put("growth", growthRate);
            report.put("comparedToPrevious", prevRevenue.doubleValue());
            report.put("newCustomers", newCustomers);
            report.put("returningCustomers", returningCustomers);
            
            // Always set COGS and Profit values (even if 0)
            report.put("totalCOGS", totalCOGS != null ? totalCOGS.doubleValue() : 0.0);
            report.put("totalProfit", totalProfit != null ? totalProfit.doubleValue() : totalRevenue.doubleValue());
            report.put("profitGrowth", profitGrowthRate);
            
            report.put("peakHour", peakHour != null ? peakHour + ":00" : "N/A");
            
            // Get trend data
            report.put("trendData", generateTrendData(startDate, endDate));
            
            // Get hourly data (use today or last day of range)
            report.put("hourlyData", generateHourlyData(endDate));
            
            // Get weekday data
            report.put("weekdayData", generateWeekdayData(startDate, endDate));
            
            // Get category data
            report.put("productData", generateCategoryData(startDate, endDate));
            
            // Get top products
            report.put("topProducts", generateTopProducts(startDate, endDate, 10));
            
            // 🆕 Get monthly revenue (last 12 months)
            report.put("monthlyData", generateMonthlyRevenue());
            
            System.out.println("✅ Report generated successfully");
            System.out.println("   Total Revenue: " + formatCurrency(totalRevenue.doubleValue()));
            System.out.println("   Total Orders: " + totalOrders);
            System.out.println("   Growth: " + String.format("%.1f%%", growthRate));
            
        } catch (Exception e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
            e.printStackTrace();
            
            // If report already has some data, preserve COGS/Profit if they exist
            if (!report.has("totalCOGS")) {
                report.put("totalCOGS", 0);
            }
            if (!report.has("totalProfit")) {
                report.put("totalProfit", report.has("totalRevenue") ? report.getDouble("totalRevenue") : 0);
            }
            if (!report.has("profitGrowth")) {
                report.put("profitGrowth", 0);
            }
            
            // Set other defaults only if not already set
            if (!report.has("totalRevenue")) {
                report.put("totalRevenue", 0);
            }
            if (!report.has("totalOrders")) {
                report.put("totalOrders", 0);
            }
            if (!report.has("avgOrderValue")) {
                report.put("avgOrderValue", 0);
            }
            if (!report.has("growth")) {
                report.put("growth", 0);
            }
            
            report.put("error", e.getMessage());
        }
        
        // Final check: ensure COGS/Profit are always set
        if (!report.has("totalCOGS")) {
            report.put("totalCOGS", 0);
        }
        if (!report.has("totalProfit")) {
            report.put("totalProfit", report.has("totalRevenue") ? report.getDouble("totalRevenue") : 0);
        }
        if (!report.has("profitGrowth")) {
            report.put("profitGrowth", 0);
        }
        
        return report;
    }
    
    /**
     * Generate trend data (daily revenue)
     */
    private JSONObject generateTrendData(LocalDate startDate, LocalDate endDate) {
        JSONObject data = new JSONObject();
        JSONArray dates = new JSONArray();
        JSONArray revenues = new JSONArray();
        JSONArray orders = new JSONArray();
        
        try {
            List<Object[]> trendData = reportDAO.getDailyRevenueTrend(startDate, endDate);
            
            // Create map for easy lookup
            Map<LocalDate, Object[]> dataMap = new HashMap<>();
            for (Object[] row : trendData) {
                LocalDate date = (LocalDate) row[0];
                dataMap.put(date, row);
            }
            
            // Fill in all dates in range (including days with no orders)
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                dates.put(current.format(DATE_FORMATTER));
                
                if (dataMap.containsKey(current)) {
                    Object[] row = dataMap.get(current);
                    BigDecimal revenue = (BigDecimal) row[1];
                    Long orderCount = (Long) row[2];
                    
                    revenues.put(revenue.doubleValue());
                    orders.put(orderCount);
                } else {
                    // No orders on this day
                    revenues.put(0);
                    orders.put(0);
                }
                
                current = current.plusDays(1);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generating trend data: " + e.getMessage());
            e.printStackTrace();
        }
        
        data.put("dates", dates);
        data.put("revenues", revenues);
        data.put("orders", orders);
        
        return data;
    }
    
    /**
     * Generate hourly revenue data
     */
    private JSONObject generateHourlyData(LocalDate date) {
        JSONObject data = new JSONObject();
        JSONArray hours = new JSONArray();
        JSONArray revenues = new JSONArray();
        
        try {
            List<Object[]> hourlyData = reportDAO.getHourlyRevenue(date);
            
            // Create map for easy lookup
            Map<Integer, Double> revenueMap = new HashMap<>();
            for (Object[] row : hourlyData) {
                Integer hour = (Integer) row[0];
                BigDecimal revenue = (BigDecimal) row[1];
                revenueMap.put(hour, revenue.doubleValue());
            }
            
            // Fill in all hours (6-22)
            for (int hour = 6; hour <= 22; hour++) {
                hours.put(String.format("%02d:00", hour));
                revenues.put(revenueMap.getOrDefault(hour, 0.0));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generating hourly data: " + e.getMessage());
            e.printStackTrace();
        }
        
        data.put("hours", hours);
        data.put("revenues", revenues);
        
        return data;
    }
    
    /**
     * Generate weekday revenue data (1=Monday, 7=Sunday)
     */
    private JSONObject generateWeekdayData(LocalDate startDate, LocalDate endDate) {
        JSONObject data = new JSONObject();
        JSONArray weekdays = new JSONArray();
        JSONArray weekdayNames = new JSONArray();
        JSONArray revenues = new JSONArray();
        JSONArray orders = new JSONArray();
        
        try {
            List<Object[]> weekdayData = reportDAO.getRevenueByWeekday(startDate, endDate);
            
            // Create map for easy lookup
            Map<Integer, Object[]> dataMap = new HashMap<>();
            for (Object[] row : weekdayData) {
                Integer weekday = ((Number) row[0]).intValue();
                dataMap.put(weekday, row);
            }
            
            // Day names in Vietnamese
            String[] dayNames = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
            
            // Fill in all weekdays (1-7)
            for (int weekday = 1; weekday <= 7; weekday++) {
                weekdays.put(weekday);
                weekdayNames.put(dayNames[weekday - 1]);
                
                if (dataMap.containsKey(weekday)) {
                    Object[] row = dataMap.get(weekday);
                    BigDecimal revenue = (BigDecimal) row[1];
                    Long orderCount = ((Number) row[2]).longValue();
                    revenues.put(revenue.doubleValue());
                    orders.put(orderCount);
                } else {
                    revenues.put(0.0);
                    orders.put(0);
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generating weekday data: " + e.getMessage());
            e.printStackTrace();
        }
        
        data.put("weekdays", weekdays);
        data.put("weekdayNames", weekdayNames);
        data.put("revenues", revenues);
        data.put("orders", orders);
        
        return data;
    }
    
    /**
     * Generate product category data
     */
    private JSONObject generateCategoryData(LocalDate startDate, LocalDate endDate) {
        JSONObject data = new JSONObject();
        JSONArray categories = new JSONArray();
        JSONArray revenues = new JSONArray();
        JSONArray colors = new JSONArray();
        
        try {
            List<Object[]> categoryData = reportDAO.getRevenueByCategory(startDate, endDate);
            
            // Predefined colors for categories
            String[] colorPalette = {
                "rgba(102, 126, 234, 0.8)",
                "rgba(118, 75, 162, 0.8)",
                "rgba(255, 152, 0, 0.8)",
                "rgba(76, 175, 80, 0.8)",
                "rgba(33, 150, 243, 0.8)",
                "rgba(255, 193, 7, 0.8)",
                "rgba(158, 158, 158, 0.8)"
            };
            
            int colorIndex = 0;
            for (Object[] row : categoryData) {
                String categoryName = (String) row[0];
                BigDecimal revenue = (BigDecimal) row[1];
                
                categories.put(categoryName);
                revenues.put(revenue.doubleValue());
                colors.put(colorPalette[colorIndex % colorPalette.length]);
                
                colorIndex++;
            }
            
            // If no data, add "Chưa có dữ liệu"
            if (categoryData.isEmpty()) {
                categories.put("Chưa có dữ liệu");
                revenues.put(0);
                colors.put(colorPalette[0]);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generating category data: " + e.getMessage());
            e.printStackTrace();
        }
        
        data.put("categories", categories);
        data.put("revenues", revenues);
        data.put("colors", colors);
        
        return data;
    }
    
    /**
     * Generate top products list
     */
    private JSONArray generateTopProducts(LocalDate startDate, LocalDate endDate, int limit) {
        JSONArray products = new JSONArray();
        
        try {
            System.out.println("🏆 generateTopProducts START - Date range: " + startDate + " to " + endDate);
            
            List<Object[]> topProducts = reportDAO.getTopProducts(startDate, endDate, limit);
            System.out.println("   DAO returned " + topProducts.size() + " products");
            
            if (topProducts.isEmpty()) {
                System.out.println("   ⚠️ WARNING: DAO returned empty list!");
                return products; // Return empty array
            }
            
            // Calculate total revenue for percentage
            BigDecimal totalRevenue = reportDAO.getTotalRevenue(startDate, endDate);
            System.out.println("   Total revenue: " + totalRevenue);
            
            int index = 0;
            for (Object[] row : topProducts) {
                try {
                    System.out.println("   Processing product " + (index + 1) + " - Row data: " + java.util.Arrays.toString(row));
                    
                    // FIX: Safe casting - SUM() can return Long or Integer depending on database
                    // UUID productID = (UUID) row[0];
                    String productName = (String) row[1];
                    long quantity = ((Number) row[2]).longValue();  // Safe cast from Integer/Long
                    BigDecimal revenue = (BigDecimal) row[3];
                    
                    System.out.println("      Name: " + productName + ", Qty: " + quantity + ", Revenue: " + revenue);
                    
                    // Calculate percentage of total revenue
                    double percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
                        revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                              .multiply(BigDecimal.valueOf(100))
                              .doubleValue() : 0;
                    
                    // Calculate average price
                    BigDecimal avgPrice = quantity > 0 ? 
                        revenue.divide(BigDecimal.valueOf(quantity), 0, RoundingMode.HALF_UP) : 
                        BigDecimal.ZERO;
                    
                    JSONObject product = new JSONObject();
                    product.put("name", productName);
                    product.put("quantity", quantity);
                    product.put("price", avgPrice.doubleValue());
                    product.put("revenue", revenue.doubleValue());
                    product.put("share", String.format("%.1f%%", percentage));
                    
                    products.put(product);
                    System.out.println("      ✅ Product added to JSON array");
                    
                    index++;
                } catch (Exception rowError) {
                    System.err.println("   ❌ Error processing row " + index + ": " + rowError.getMessage());
                    rowError.printStackTrace();
                }
            }
            
            System.out.println("🏆 generateTopProducts END - Returning " + products.length() + " products");
            
        } catch (Exception e) {
            System.err.println("❌ Error generating top products: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * 🆕 Generate monthly revenue data (last 12 months)
     */
    private JSONObject generateMonthlyRevenue() {
        JSONObject data = new JSONObject();
        JSONArray months = new JSONArray();
        JSONArray revenues = new JSONArray();
        
        try {
            System.out.println("📊 Generating monthly revenue data...");
            
            // Get last 12 months data from DAO
            List<Object[]> monthlyData = reportDAO.getMonthlyRevenue(12);
            
            // Create a map for easy lookup
            Map<String, BigDecimal> dataMap = new HashMap<>();
            for (Object[] row : monthlyData) {
                Integer year = (Integer) row[0];
                Integer month = (Integer) row[1];
                BigDecimal revenue = (BigDecimal) row[2];
                
                String key = String.format("%d-%02d", year, month);
                dataMap.put(key, revenue);
                System.out.println("   Month: " + key + " -> Revenue: " + revenue);
            }
            
            // Fill in all 12 months (even if no data)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(11).withDayOfMonth(1);
            
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                String monthKey = String.format("%d-%02d", current.getYear(), current.getMonthValue());
                String monthLabel = String.format("Tháng %d/%d", current.getMonthValue(), current.getYear());
                
                months.put(monthLabel);
                
                BigDecimal revenue = dataMap.getOrDefault(monthKey, BigDecimal.ZERO);
                revenues.put(revenue.doubleValue());
                
                current = current.plusMonths(1);
            }
            
            data.put("months", months);
            data.put("revenues", revenues);
            
            System.out.println("✅ Monthly revenue data generated: " + months.length() + " months");
            
        } catch (Exception e) {
            System.err.println("❌ Error generating monthly revenue: " + e.getMessage());
            e.printStackTrace();
            
            // Return empty arrays on error
            data.put("months", new JSONArray());
            data.put("revenues", new JSONArray());
        }
        
        return data;
    }
    
    /**
     * Calculate growth rate
     */
    private double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        
        BigDecimal growth = current.subtract(previous)
                                  .divide(previous, 4, RoundingMode.HALF_UP)
                                  .multiply(BigDecimal.valueOf(100));
        
        return growth.doubleValue();
    }
    
    /**
     * Format currency for logging
     */
    private String formatCurrency(double value) {
        return String.format("%,.0f VNĐ", value);
    }
    
    /**
     * 🆕 Generate Today's Revenue Dashboard Report
     * Real-time metrics for current day performance
     */
    public JSONObject generateTodayReport() {
        JSONObject todayReport = new JSONObject();
        
        try {
            System.out.println("📊 Generating TODAY's revenue dashboard...");
            
            // Get all metrics from DAO
            Map<String, Object> metrics = reportDAO.getTodayMetrics();
            
            // Extract values
            BigDecimal todayRevenue = (BigDecimal) metrics.get("todayRevenue");
            BigDecimal yesterdayRevenue = (BigDecimal) metrics.get("yesterdayRevenue");
            Long todayOrders = (Long) metrics.get("todayOrders");
            Long yesterdayOrders = (Long) metrics.get("yesterdayOrders");
            BigDecimal avgOrderValue = (BigDecimal) metrics.get("avgOrderValue");
            String peakHour = (String) metrics.get("peakHour");
            
            List<String> hourlyLabels = (List<String>) metrics.get("hourlyLabels");
            
            List<BigDecimal> hourlyRevenues = (List<BigDecimal>) metrics.get("hourlyRevenues");
            
            // Calculate growth rates
            double revenueGrowth = calculateGrowthRate(todayRevenue, yesterdayRevenue);
            double orderGrowth = yesterdayOrders > 0 ? 
                ((todayOrders - yesterdayOrders) * 100.0 / yesterdayOrders) : 0;
            
            // Build JSON response
            todayReport.put("success", true);
            todayReport.put("todayRevenue", todayRevenue.doubleValue());
            todayReport.put("yesterdayRevenue", yesterdayRevenue.doubleValue());
            todayReport.put("revenueGrowth", revenueGrowth);
            todayReport.put("todayOrders", todayOrders);
            todayReport.put("yesterdayOrders", yesterdayOrders);
            todayReport.put("orderGrowth", orderGrowth);
            todayReport.put("avgOrderValue", avgOrderValue.doubleValue());
            todayReport.put("peakHour", peakHour);
            
            // Hourly trend data for mini chart
            JSONArray hourLabelsArr = new JSONArray();
            JSONArray hourRevenuesArr = new JSONArray();
            for (int i = 0; i < hourlyLabels.size(); i++) {
                hourLabelsArr.put(hourlyLabels.get(i));
                hourRevenuesArr.put(hourlyRevenues.get(i).doubleValue());
            }
            
            JSONObject hourlyTrend = new JSONObject();
            hourlyTrend.put("hours", hourLabelsArr);
            hourlyTrend.put("revenues", hourRevenuesArr);
            todayReport.put("hourlyTrend", hourlyTrend);
            
            // Status indicator
            String status = "success";
            if (revenueGrowth < -10) status = "danger";
            else if (revenueGrowth < 0) status = "warning";
            else if (revenueGrowth > 10) status = "excellent";
            todayReport.put("status", status);
            
            System.out.println("✅ Today's report: Revenue=" + todayRevenue + 
                             ", Growth=" + String.format("%.1f%%", revenueGrowth) +
                             ", Orders=" + todayOrders);
            
        } catch (Exception e) {
            System.err.println("❌ Error generating today's report: " + e.getMessage());
            e.printStackTrace();
            todayReport.put("success", false);
            todayReport.put("error", e.getMessage());
        }
        
        return todayReport;
    }
    
    /**
     * Get report data for printing
     * Returns a Map with all necessary data for printable report
     */
    public Map<String, Object> getReportDataForPrint(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = new HashMap<>();
        
        try {
            System.out.println("📊 Getting report data for print from " + startDate + " to " + endDate);
            
            // Get basic statistics
            BigDecimal totalRevenue = reportDAO.getTotalRevenue(startDate, endDate);
            long totalOrders = reportDAO.getTotalOrders(startDate, endDate);
            
            // Calculate average order value
            BigDecimal avgOrderValue = totalOrders > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            // Get previous period for comparison
            BigDecimal prevRevenue = reportDAO.getPreviousPeriodRevenue(startDate, endDate);
            double growthRate = calculateGrowthRate(totalRevenue, prevRevenue);
            
            // Get customer statistics
            long newCustomers = reportDAO.getNewCustomers(startDate, endDate);
            long returningCustomers = reportDAO.getReturningCustomers(startDate, endDate);
            
            // Get COGS and profit
            BigDecimal totalCOGS = BigDecimal.ZERO;
            BigDecimal totalProfit = totalRevenue;
            double profitGrowthRate = 0.0;
            
            try {
                totalCOGS = reportDAO.getTotalCostOfGoodsSold(startDate, endDate);
                totalProfit = totalRevenue.subtract(totalCOGS);
                
                BigDecimal prevCOGS = reportDAO.getPreviousPeriodCOGS(startDate, endDate);
                BigDecimal prevProfit = prevRevenue.subtract(prevCOGS);
                profitGrowthRate = calculateGrowthRate(totalProfit, prevProfit);
            } catch (Exception e) {
                System.err.println("❌ Error calculating COGS/Profit: " + e.getMessage());
            }
            
            // Determine if single day or multiple days
            boolean isSingleDay = startDate.equals(endDate);
            
            // Get hourly data (if single day)
            Map<String, Object> hourlyData = null;
            if (isSingleDay) {
                JSONObject hourlyJson = generateHourlyData(startDate);
                hourlyData = new HashMap<>();
                // Convert JSONArray to List
                List<String> hours = new ArrayList<>();
                List<Double> revenues = new ArrayList<>();
                JSONArray hoursArray = hourlyJson.getJSONArray("hours");
                JSONArray revenuesArray = hourlyJson.getJSONArray("revenues");
                for (int i = 0; i < hoursArray.length(); i++) {
                    hours.add(hoursArray.getString(i));
                }
                for (int i = 0; i < revenuesArray.length(); i++) {
                    revenues.add(revenuesArray.getDouble(i));
                }
                hourlyData.put("hours", hours);
                hourlyData.put("revenues", revenues);
            }
            
            // Get daily data (if multiple days)
            Map<String, Object> dailyData = null;
            if (!isSingleDay) {
                JSONObject trendJson = generateTrendData(startDate, endDate);
                dailyData = new HashMap<>();
                // Convert JSONArray to List
                List<String> dates = new ArrayList<>();
                List<Double> revenues = new ArrayList<>();
                List<Long> orders = new ArrayList<>();
                JSONArray datesArray = trendJson.getJSONArray("dates");
                JSONArray revenuesArray = trendJson.getJSONArray("revenues");
                JSONArray ordersArray = trendJson.getJSONArray("orders");
                for (int i = 0; i < datesArray.length(); i++) {
                    dates.add(datesArray.getString(i));
                }
                for (int i = 0; i < revenuesArray.length(); i++) {
                    revenues.add(revenuesArray.getDouble(i));
                }
                for (int i = 0; i < ordersArray.length(); i++) {
                    orders.add(ordersArray.getLong(i));
                }
                dailyData.put("dates", dates);
                dailyData.put("revenues", revenues);
                dailyData.put("orders", orders);
            }
            
            // Get top products
            JSONArray topProductsJson = generateTopProducts(startDate, endDate, 10);
            List<Map<String, Object>> topProducts = new ArrayList<>();
            for (int i = 0; i < topProductsJson.length(); i++) {
                JSONObject product = topProductsJson.getJSONObject(i);
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("name", product.getString("name"));
                productMap.put("quantity", product.getLong("quantity"));
                productMap.put("revenue", product.getDouble("revenue"));
                productMap.put("share", product.getString("share"));
                topProducts.add(productMap);
            }
            
            // Get category revenue
            JSONObject categoryJson = generateCategoryData(startDate, endDate);
            List<Map<String, Object>> categories = new ArrayList<>();
            JSONArray categoryNames = categoryJson.getJSONArray("categories");
            JSONArray categoryRevenues = categoryJson.getJSONArray("revenues");
            BigDecimal totalCategoryRevenue = BigDecimal.ZERO;
            for (int i = 0; i < categoryRevenues.length(); i++) {
                totalCategoryRevenue = totalCategoryRevenue.add(BigDecimal.valueOf(categoryRevenues.getDouble(i)));
            }
            
            for (int i = 0; i < categoryNames.length(); i++) {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("name", categoryNames.getString(i));
                double revenue = categoryRevenues.getDouble(i);
                categoryMap.put("revenue", revenue);
                double percentage = totalCategoryRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    (revenue / totalCategoryRevenue.doubleValue()) * 100 : 0;
                categoryMap.put("percentage", String.format("%.1f%%", percentage));
                categories.add(categoryMap);
            }
            
            // Build report data map
            reportData.put("startDate", startDate);
            reportData.put("endDate", endDate);
            reportData.put("isSingleDay", isSingleDay);
            reportData.put("totalRevenue", totalRevenue);
            reportData.put("totalOrders", totalOrders);
            reportData.put("avgOrderValue", avgOrderValue);
            reportData.put("totalCOGS", totalCOGS);
            reportData.put("totalProfit", totalProfit);
            reportData.put("growthRate", growthRate);
            reportData.put("profitGrowthRate", profitGrowthRate);
            reportData.put("newCustomers", newCustomers);
            reportData.put("returningCustomers", returningCustomers);
            reportData.put("hourlyData", hourlyData);
            reportData.put("dailyData", dailyData);
            reportData.put("topProducts", topProducts);
            reportData.put("categories", categories);
            
            System.out.println("✅ Report data for print generated successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error getting report data for print: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reportData;
    }
}

