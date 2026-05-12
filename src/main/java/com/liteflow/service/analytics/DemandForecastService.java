package com.liteflow.service.analytics;

import com.liteflow.dao.analytics.DemandForecastDAO;
import com.liteflow.service.ai.AIAgentConfigService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * DemandForecastService - Intelligent Demand Forecasting & Replenishment
 * 
 * Features:
 * - Phân tích lịch sử bán hàng (Sales History Analysis)
 * - Tính toán consumption rate (Tốc độ tiêu thụ)
 * - Dự đoán nhu cầu (Demand Prediction)
 * - Gợi ý nhập hàng (Replenishment Suggestions)
 * - Ưu tiên theo doanh thu (Revenue-based Prioritization)
 * - Tránh out-of-stock (Stock-out Prevention)
 */
public class DemandForecastService {
    
    private final DemandForecastDAO forecastDAO;
    private final AIAgentConfigService configService;
    
    // Default configuration constants (fallback if config not found)
    private static final int DEFAULT_ANALYSIS_DAYS = 30;
    private static final int DEFAULT_LEAD_TIME_DAYS = 3;
    private static final double SAFETY_STOCK_MULTIPLIER = 1.5;
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 20;
    private static final int DEFAULT_CRITICAL_STOCK_THRESHOLD = 5;
    private static final int TOP_PRODUCTS_LIMIT = 20;
    
    public DemandForecastService() {
        this.forecastDAO = new DemandForecastDAO();
        this.configService = new AIAgentConfigService();
    }
    
    /**
     * Get low stock threshold from config or default
     */
    private int getLowStockThreshold() {
        return configService.getIntConfig("forecast.low_stock_threshold", DEFAULT_LOW_STOCK_THRESHOLD);
    }
    
    /**
     * Get critical stock threshold from config or default
     */
    private int getCriticalStockThreshold() {
        return configService.getIntConfig("forecast.critical_stock_threshold", DEFAULT_CRITICAL_STOCK_THRESHOLD);
    }
    
    /**
     * Get forecast days ahead from config or default
     */
    private int getForecastDaysAhead() {
        return configService.getIntConfig("forecast.days_ahead", 7);
    }
    
    /**
     * Check if demand forecast is enabled
     */
    private boolean isDemandForecastEnabled() {
        return configService.getBooleanConfig("forecast.enable_forecast", true);
    }
    
    /**
     * Generate comprehensive replenishment suggestions
     * @return JSON với gợi ý nhập hàng chi tiết
     */
    public JSONObject generateReplenishmentSuggestions() {
        System.out.println("🤖 AI: Starting Demand Forecasting Analysis...");
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("analysisParameters", getAnalysisParameters());
        
        try {
            // 1. Get sales history
            List<Object[]> salesHistory = forecastDAO.getSalesHistory(DEFAULT_ANALYSIS_DAYS);
            
            // 2. Get current stock
            List<Object[]> stockLevels = forecastDAO.getCurrentStockLevels();
            
            // 3. Build stock map for quick lookup
            Map<UUID, StockInfo> stockMap = buildStockMap(stockLevels);
            
            // 4. Calculate lead time
            int averageLeadTime = calculateAverageLeadTime();
            
            // 5. Analyze each product and generate suggestions
            List<ReplenishmentSuggestion> suggestions = new ArrayList<>();
            
            for (Object[] sale : salesHistory) {
                UUID variantId = (UUID) sale[0];
                String productName = (String) sale[1];
                String size = (String) sale[2];
                long totalSold = ((Number) sale[3]).longValue();
                BigDecimal totalRevenue = (BigDecimal) sale[4];
                int daysWithSales = ((Number) sale[5]).intValue();
                
                StockInfo stock = stockMap.get(variantId);
                int currentStock = (stock != null) ? stock.amount : 0;
                
                // Calculate metrics
                double dailySalesRate = (double) totalSold / DEFAULT_ANALYSIS_DAYS;
                int forecastedDemand = (int) Math.ceil(dailySalesRate * (averageLeadTime + 7)); // Lead time + 1 tuần
                int safetyStock = (int) Math.ceil(dailySalesRate * averageLeadTime * SAFETY_STOCK_MULTIPLIER);
                int reorderPoint = (int) Math.ceil((dailySalesRate * averageLeadTime) + safetyStock);
                int suggestedOrderQty = Math.max(0, forecastedDemand - currentStock);
                
                // Calculate stockout risk
                String stockoutRisk = calculateStockoutRisk(currentStock, dailySalesRate, averageLeadTime);
                
                // Calculate priority score (based on revenue and stockout risk)
                double priorityScore = calculatePriorityScore(totalRevenue, stockoutRisk, dailySalesRate);
                
                // Determine urgency
                String urgency = determineUrgency(currentStock, reorderPoint, stockoutRisk);
                
                // Only suggest if needed
                if (shouldSuggestReplenishment(currentStock, reorderPoint, dailySalesRate)) {
                    ReplenishmentSuggestion suggestion = new ReplenishmentSuggestion();
                    suggestion.variantId = variantId;
                    suggestion.productName = productName;
                    suggestion.size = size;
                    suggestion.currentStock = currentStock;
                    suggestion.dailySalesRate = dailySalesRate;
                    suggestion.forecastedDemand = forecastedDemand;
                    suggestion.reorderPoint = reorderPoint;
                    suggestion.safetyStock = safetyStock;
                    suggestion.suggestedOrderQty = suggestedOrderQty;
                    suggestion.totalRevenue = totalRevenue.doubleValue();
                    suggestion.daysWithSales = daysWithSales;
                    suggestion.stockoutRisk = stockoutRisk;
                    suggestion.urgency = urgency;
                    suggestion.priorityScore = priorityScore;
                    suggestion.estimatedDaysUntilStockout = calculateDaysUntilStockout(currentStock, dailySalesRate);
                    
                    suggestions.add(suggestion);
                }
            }
            
            // Sort by priority score (descending)
            suggestions.sort((a, b) -> Double.compare(b.priorityScore, a.priorityScore));
            
            // 6. Build JSON result
            result.put("summary", buildSummary(suggestions, stockMap));
            result.put("urgentItems", buildUrgentItems(suggestions));
            result.put("topRevenueSuggestions", buildTopRevenueSuggestions(suggestions));
            result.put("allSuggestions", buildAllSuggestions(suggestions));
            result.put("insights", generateInsights(suggestions, salesHistory.size()));
            
            System.out.println("✅ AI: Generated " + suggestions.size() + " replenishment suggestions");
            
        } catch (Exception e) {
            System.err.println("❌ AI: Error generating suggestions: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get quick stock alerts (low/critical stock items)
     */
    public JSONObject getStockAlerts() {
        System.out.println("⚠️ AI: Checking stock alerts...");
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        
        try {
            List<Object[]> lowStock = forecastDAO.getLowStockProducts(getLowStockThreshold());
            
            JSONArray critical = new JSONArray();
            JSONArray warning = new JSONArray();
            
            for (Object[] item : lowStock) {
                UUID variantId = (UUID) item[0];
                String productName = (String) item[1];
                String size = (String) item[2];
                long stock = ((Number) item[3]).longValue();
                BigDecimal price = (BigDecimal) item[4];
                
                JSONObject alert = new JSONObject();
                alert.put("variantId", variantId.toString());
                alert.put("productName", productName);
                alert.put("size", size);
                alert.put("currentStock", stock);
                alert.put("price", price.doubleValue());
                
                if (stock <= getCriticalStockThreshold()) {
                    alert.put("level", "CRITICAL");
                    critical.put(alert);
                } else {
                    alert.put("level", "WARNING");
                    warning.put(alert);
                }
            }
            
            result.put("criticalStock", critical);
            result.put("lowStock", warning);
            result.put("totalAlerts", critical.length() + warning.length());
            
            System.out.println("✅ AI: Found " + critical.length() + " critical, " + warning.length() + " low stock items");
            
        } catch (Exception e) {
            System.err.println("❌ AI: Error getting stock alerts: " + e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    // ==================== HELPER METHODS ====================
    
    private JSONObject getAnalysisParameters() {
        JSONObject params = new JSONObject();
        params.put("analysisDays", DEFAULT_ANALYSIS_DAYS);
        params.put("leadTimeDays", DEFAULT_LEAD_TIME_DAYS);
        params.put("safetyStockMultiplier", SAFETY_STOCK_MULTIPLIER);
        params.put("lowStockThreshold", getLowStockThreshold());
        params.put("criticalStockThreshold", getCriticalStockThreshold());
        return params;
    }
    
    private Map<UUID, StockInfo> buildStockMap(List<Object[]> stockLevels) {
        Map<UUID, StockInfo> map = new HashMap<>();
        for (Object[] row : stockLevels) {
            UUID variantId = (UUID) row[0];
            String productName = (String) row[1];
            String size = (String) row[2];
            int amount = ((Number) row[3]).intValue();
            
            StockInfo info = new StockInfo();
            info.variantId = variantId;
            info.productName = productName;
            info.size = size;
            info.amount = amount;
            
            map.put(variantId, info);
        }
        return map;
    }
    
    private int calculateAverageLeadTime() {
        try {
            List<Object[]> recentPOs = forecastDAO.getRecentPurchaseOrders(20);
            
            if (recentPOs.isEmpty()) {
                return DEFAULT_LEAD_TIME_DAYS;
            }
            
            long totalDays = 0;
            int count = 0;
            
            for (Object[] po : recentPOs) {
                LocalDateTime createDate = (LocalDateTime) po[1];
                LocalDateTime expectedDelivery = (LocalDateTime) po[2];
                
                if (createDate != null && expectedDelivery != null) {
                    long days = ChronoUnit.DAYS.between(createDate, expectedDelivery);
                    if (days > 0 && days < 30) { // Reasonable range
                        totalDays += days;
                        count++;
                    }
                }
            }
            
            int avgLeadTime = count > 0 ? (int) Math.ceil((double) totalDays / count) : DEFAULT_LEAD_TIME_DAYS;
            System.out.println("📊 AI: Average lead time: " + avgLeadTime + " days (based on " + count + " orders)");
            
            return avgLeadTime;
            
        } catch (Exception e) {
            System.err.println("⚠️ AI: Error calculating lead time, using default: " + e.getMessage());
            return DEFAULT_LEAD_TIME_DAYS;
        }
    }
    
    private String calculateStockoutRisk(int currentStock, double dailySalesRate, int leadTime) {
        if (dailySalesRate == 0) return "LOW";
        
        double daysUntilStockout = currentStock / dailySalesRate;
        
        if (daysUntilStockout <= leadTime) {
            return "HIGH";
        } else if (daysUntilStockout <= leadTime * 2) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    private double calculatePriorityScore(BigDecimal revenue, String riskLevel, double salesRate) {
        double score = revenue.doubleValue();
        
        // Boost score based on risk
        switch (riskLevel) {
            case "HIGH":
                score *= 3.0;
                break;
            case "MEDIUM":
                score *= 1.5;
                break;
        }
        
        // Boost score based on sales rate
        score += salesRate * 10000;
        
        return score;
    }
    
    private String determineUrgency(int currentStock, int reorderPoint, String risk) {
        if (currentStock <= getCriticalStockThreshold() || risk.equals("HIGH")) {
            return "URGENT";
        } else if (currentStock <= reorderPoint || risk.equals("MEDIUM")) {
            return "HIGH";
        } else if (currentStock <= reorderPoint * 1.5) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    private boolean shouldSuggestReplenishment(int currentStock, int reorderPoint, double dailySalesRate) {
        // Suggest if stock is below reorder point OR if sales rate > 0 and stock is low
        return currentStock <= reorderPoint || (dailySalesRate > 0 && currentStock <= getLowStockThreshold());
    }
    
    private int calculateDaysUntilStockout(int currentStock, double dailySalesRate) {
        if (dailySalesRate == 0) return 999;
        return (int) Math.ceil(currentStock / dailySalesRate);
    }
    
    private JSONObject buildSummary(List<ReplenishmentSuggestion> suggestions, Map<UUID, StockInfo> stockMap) {
        JSONObject summary = new JSONObject();
        
        long urgentCount = suggestions.stream().filter(s -> s.urgency.equals("URGENT")).count();
        long highCount = suggestions.stream().filter(s -> s.urgency.equals("HIGH")).count();
        
        double totalSuggestedValue = suggestions.stream()
            .mapToDouble(s -> s.suggestedOrderQty * (s.totalRevenue / Math.max(1, s.daysWithSales)))
            .sum();
        
        summary.put("totalSuggestions", suggestions.size());
        summary.put("urgentItems", urgentCount);
        summary.put("highPriorityItems", highCount);
        summary.put("estimatedOrderValue", Math.round(totalSuggestedValue));
        summary.put("totalProductsInStock", stockMap.size());
        
        return summary;
    }
    
    private JSONArray buildUrgentItems(List<ReplenishmentSuggestion> suggestions) {
        JSONArray urgent = new JSONArray();
        
        suggestions.stream()
            .filter(s -> s.urgency.equals("URGENT") || s.urgency.equals("HIGH"))
            .limit(10)
            .forEach(s -> urgent.put(s.toJSON()));
        
        return urgent;
    }
    
    private JSONArray buildTopRevenueSuggestions(List<ReplenishmentSuggestion> suggestions) {
        JSONArray top = new JSONArray();
        
        suggestions.stream()
            .sorted((a, b) -> Double.compare(b.totalRevenue, a.totalRevenue))
            .limit(15)
            .forEach(s -> top.put(s.toJSON()));
        
        return top;
    }
    
    private JSONArray buildAllSuggestions(List<ReplenishmentSuggestion> suggestions) {
        JSONArray all = new JSONArray();
        suggestions.forEach(s -> all.put(s.toJSON()));
        return all;
    }
    
    private JSONObject generateInsights(List<ReplenishmentSuggestion> suggestions, int totalProducts) {
        JSONObject insights = new JSONObject();
        
        // Average daily sales rate
        double avgSalesRate = suggestions.stream()
            .mapToDouble(s -> s.dailySalesRate)
            .average()
            .orElse(0);
        
        // Most critical category
        long criticalCount = suggestions.stream()
            .filter(s -> s.stockoutRisk.equals("HIGH"))
            .count();
        
        insights.put("averageDailySalesRate", Math.round(avgSalesRate * 100) / 100.0);
        insights.put("productsNeedingReplenishment", suggestions.size());
        insights.put("replenishmentRate", Math.round((double) suggestions.size() / totalProducts * 100));
        insights.put("highRiskProducts", criticalCount);
        
        // Recommendations
        JSONArray recommendations = new JSONArray();
        if (criticalCount > 5) {
            recommendations.put("⚠️ Có " + criticalCount + " sản phẩm có nguy cơ hết hàng cao - cần đặt hàng ngay!");
        }
        if (suggestions.size() > totalProducts * 0.3) {
            recommendations.put("📊 Hơn 30% sản phẩm cần nhập hàng - xem xét tối ưu quy trình đặt hàng");
        }
        
        insights.put("recommendations", recommendations);
        
        return insights;
    }
    
    // ==================== INNER CLASSES ====================
    
    private static class StockInfo {
        @SuppressWarnings("unused")
        UUID variantId;
        @SuppressWarnings("unused")
        String productName;
        @SuppressWarnings("unused")
        String size;
        int amount;
    }
    
    private static class ReplenishmentSuggestion {
        UUID variantId;
        String productName;
        String size;
        int currentStock;
        double dailySalesRate;
        int forecastedDemand;
        int reorderPoint;
        int safetyStock;
        int suggestedOrderQty;
        double totalRevenue;
        int daysWithSales;
        String stockoutRisk;
        String urgency;
        double priorityScore;
        int estimatedDaysUntilStockout;
        
        JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("variantId", variantId.toString());
            json.put("productName", productName);
            json.put("size", size);
            json.put("currentStock", currentStock);
            json.put("dailySalesRate", Math.round(dailySalesRate * 100) / 100.0);
            json.put("forecastedDemand", forecastedDemand);
            json.put("reorderPoint", reorderPoint);
            json.put("safetyStock", safetyStock);
            json.put("suggestedOrderQty", suggestedOrderQty);
            json.put("totalRevenue", Math.round(totalRevenue));
            json.put("stockoutRisk", stockoutRisk);
            json.put("urgency", urgency);
            json.put("daysUntilStockout", estimatedDaysUntilStockout);
            return json;
        }
    }
}

