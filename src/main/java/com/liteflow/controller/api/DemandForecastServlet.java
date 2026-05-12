package com.liteflow.controller.api;

import com.liteflow.service.analytics.DemandForecastService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;

/**
 * DemandForecastServlet - API for Demand Forecasting & Replenishment
 * 
 * Endpoints:
 * - GET /api/demand-forecast?action=suggestions    - Get replenishment suggestions
 * - GET /api/demand-forecast?action=alerts         - Get stock alerts
 * - GET /api/demand-forecast                       - Get full analysis
 */
@WebServlet("/api/demand-forecast")
public class DemandForecastServlet extends HttpServlet {
    
    private DemandForecastService demandService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.demandService = new DemandForecastService();
        System.out.println("✅ DemandForecastServlet initialized");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Enable CORS for testing
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        String action = request.getParameter("action");
        
        System.out.println("📊 Demand Forecast API Request: action=" + action);
        
        try {
            JSONObject result;
            
            if ("alerts".equals(action)) {
                // Get stock alerts only
                result = demandService.getStockAlerts();
            } else if ("suggestions".equals(action)) {
                // Get replenishment suggestions
                result = demandService.generateReplenishmentSuggestions();
            } else {
                // Default: Get full analysis
                JSONObject suggestions = demandService.generateReplenishmentSuggestions();
                JSONObject alerts = demandService.getStockAlerts();
                
                result = new JSONObject();
                result.put("success", true);
                result.put("timestamp", java.time.LocalDateTime.now().toString());
                result.put("replenishmentSuggestions", suggestions);
                result.put("stockAlerts", alerts);
            }
            
            response.getWriter().write(result.toString(2)); // Pretty print with indent=2
            System.out.println("✅ Demand Forecast API Response sent");
            
        } catch (Exception e) {
            System.err.println("❌ Demand Forecast API Error: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("message", "Error generating demand forecast");
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(error.toString(2));
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Handle CORS preflight
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

