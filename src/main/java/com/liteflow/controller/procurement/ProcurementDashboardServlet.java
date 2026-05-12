package com.liteflow.controller.procurement;

import com.liteflow.service.procurement.ProcurementService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.List;

@WebServlet(urlPatterns = {"/procurement/dashboard"})
public class ProcurementDashboardServlet extends HttpServlet {
    private final ProcurementService service = new ProcurementService();
   
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Load dashboard statistics
            Map<String, Object> stats = service.getDashboardStatistics();
            req.setAttribute("stats", stats);
            
            // Load recent activities
            List<ProcurementService.ActivityDTO> recentActivities = service.getRecentActivities(5);
            req.setAttribute("recentActivities", recentActivities);
            
            // Calculate alert counts
            int nearDeadlineCount = (Integer) stats.getOrDefault("nearDeadlinePOs", 0);
            int overdueCount = (Integer) stats.getOrDefault("overduePOs", 0);
            int unmatchedInvoices = (Integer) stats.getOrDefault("unmatchedInvoices", 0);
            
            req.setAttribute("nearDeadlineCount", nearDeadlineCount);
            req.setAttribute("overdueCount", overdueCount);
            req.setAttribute("unmatchedInvoices", unmatchedInvoices);
            
            // Show warning alert if there are POs near deadline or overdue
            req.setAttribute("showWarningAlert", (nearDeadlineCount > 0 || overdueCount > 0));
            
            // Show info alert if there are unmatched invoices
            req.setAttribute("showInfoAlert", (unmatchedInvoices > 0));
            
        } catch (Exception e) {
            System.err.println("Error loading procurement dashboard data: " + e.getMessage());
            e.printStackTrace();
            // Continue with empty data - JSP will handle gracefully
        }
        
        req.getRequestDispatcher("/procurement/dashboard.jsp").forward(req, resp);
    }
}
