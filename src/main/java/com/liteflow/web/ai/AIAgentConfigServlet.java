package com.liteflow.web.ai;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to display AI Agent Configuration page
 */
@WebServlet("/ai-agent-config")
public class AIAgentConfigServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login.jsp");
            return;
        }
        
        // Check if user has permission (Admin, Manager, or Owner)
        @SuppressWarnings("unchecked")
        java.util.List<String> userRoles = (java.util.List<String>) session.getAttribute("UserRoles");
        
        boolean hasPermission = false;
        if (userRoles != null) {
            for (String role : userRoles) {
                if ("ADMIN".equalsIgnoreCase(role) || 
                    "MANAGER".equalsIgnoreCase(role) || 
                    "Owner".equalsIgnoreCase(role)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        
        if (!hasPermission) {
            request.getRequestDispatcher("/accessDenied.jsp").forward(request, response);
            return;
        }
        
        // Forward to JSP page
        request.getRequestDispatcher("/ai-agent-config.jsp").forward(request, response);
    }
}

