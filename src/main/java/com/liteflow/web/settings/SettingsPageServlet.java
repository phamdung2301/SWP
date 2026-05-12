package com.liteflow.web.settings;

import com.liteflow.model.auth.User;
import com.liteflow.service.auth.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Servlet to display Settings page
 */
@WebServlet("/settings")
public class SettingsPageServlet extends HttpServlet {
    
    private final UserService userService = new UserService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in (all logged-in users can access settings page)
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login.jsp");
            return;
        }
        
        // Check if user has UserLogin attribute (indicates logged in)
        Object userLogin = session.getAttribute("UserLogin");
        if (userLogin == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login.jsp");
            return;
        }
        
        // Load user information for User Info section
        try {
            UUID userId = null;
            if (userLogin instanceof UUID) {
                userId = (UUID) userLogin;
            } else if (userLogin instanceof String) {
                userId = UUID.fromString((String) userLogin);
            }
            
            if (userId != null) {
                User user = userService.getUserById(userId).orElse(null);
                if (user != null) {
                    request.setAttribute("currentUser", user);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error loading user information: " + e.getMessage());
            // Continue without user info - not critical
        }
        
        // Forward to JSP page (role-based filtering will be done in JSP)
        request.getRequestDispatcher("/settings.jsp").forward(request, response);
    }
}

