package com.liteflow.controller.report;

import com.liteflow.service.report.RevenueReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Revenue Report Servlet
 * Generates comprehensive revenue reports with charts and statistics
 * Now using REAL DATA from database!
 */
@WebServlet("/report/revenue")
public class RevenueReportServlet extends HttpServlet {
    
    private final RevenueReportService reportService;
    
    public RevenueReportServlet() {
        this.reportService = new RevenueReportService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("api".equals(action)) {
            // Return JSON data for charts
            handleAPIRequest(request, response);
        } else if ("today".equals(action)) {
            // 🆕 Return TODAY's dashboard data
            handleTodayRequest(request, response);
        } else {
            // Show report page
            handleReportPage(request, response);
        }
    }
    
    /**
     * Show revenue report page
     */
    private void handleReportPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get date range from parameters or use defaults
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        
        LocalDate startDate = startDateStr != null ? 
            LocalDate.parse(startDateStr) : LocalDate.now().minusDays(30);
        LocalDate endDate = endDateStr != null ? 
            LocalDate.parse(endDateStr) : LocalDate.now();
        
        System.out.println("📊 Loading revenue report page: " + startDate + " to " + endDate);
        
        request.setAttribute("startDate", startDate.toString());
        request.setAttribute("endDate", endDate.toString());
        
        request.getRequestDispatcher("/report/revenue.jsp").forward(request, response);
    }
    
    /**
     * Handle API requests for chart data
     * Now using REAL DATA from RevenueReportService!
     */
    private void handleAPIRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        
        LocalDate startDate = startDateStr != null ? 
            LocalDate.parse(startDateStr) : LocalDate.now().minusDays(30);
        LocalDate endDate = endDateStr != null ? 
            LocalDate.parse(endDateStr) : LocalDate.now();
        
        System.out.println("📊 API Request: " + startDate + " to " + endDate);
        
        JSONObject data = new JSONObject();
        
        try {
            // Generate report using service with REAL database data
            data = reportService.generateReport(startDate, endDate);
            data.put("success", true);
            
            System.out.println("✅ API Response generated successfully");
            
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            
            data.put("success", false);
            data.put("error", e.getMessage());
            data.put("message", "Error loading data from database");
        }
        
        response.getWriter().write(data.toString());
    }
    
    /**
     * 🆕 Handle Today's Dashboard API Request
     * Returns real-time metrics for current day
     */
    private void handleTodayRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        System.out.println("📊 Today's Dashboard API Request");
        
        JSONObject todayData = new JSONObject();
        
        try {
            // Generate today's report using service
            todayData = reportService.generateTodayReport();
            
            System.out.println("✅ Today's API Response generated successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Today's API Error: " + e.getMessage());
            e.printStackTrace();
            
            todayData.put("success", false);
            todayData.put("error", e.getMessage());
            todayData.put("message", "Error loading today's data from database");
        }
        
        response.getWriter().write(todayData.toString());
    }
}
