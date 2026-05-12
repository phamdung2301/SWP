package com.liteflow.listener;

import com.liteflow.service.alert.AlertSchedulerService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;

/**
 * Listener to start/stop AlertSchedulerService on server startup/shutdown
 */
@WebListener
public class AlertSchedulerListener implements ServletContextListener {
    
    private AlertSchedulerService schedulerService;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("🚀 Initializing Alert Scheduler...");
        
        try {
            schedulerService = new AlertSchedulerService();
            schedulerService.start();
            
            // Store in servlet context for potential access
            sce.getServletContext().setAttribute("alertScheduler", schedulerService);
            
            System.out.println("✅ Alert Scheduler initialized successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Alert Scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("🛑 Shutting down Alert Scheduler...");
        
        if (schedulerService != null) {
            schedulerService.stop();
        }
        
        System.out.println("✅ Alert Scheduler shut down successfully");
    }
}


