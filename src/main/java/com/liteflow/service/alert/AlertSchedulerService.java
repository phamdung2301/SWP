package com.liteflow.service.alert;

import com.liteflow.dao.alert.AlertConfigurationDAO;
import com.liteflow.dao.procurement.PurchaseOrderDAO;
import com.liteflow.model.alert.AlertConfiguration;
import com.liteflow.model.procurement.PurchaseOrder;
import com.liteflow.service.report.RevenueReportService;
import com.liteflow.service.inventory.ReservationService;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Alert Scheduler Service
 * Handles periodic alert checking and triggering
 * 
 * This service runs in the background and checks:
 * - Scheduled alerts (e.g., daily summary at 6 PM)
 * - Condition-based alerts (e.g., PO pending > 2 days)
 * - Threshold alerts (e.g., low inventory)
 */
public class AlertSchedulerService {
    
    private final AlertConfigurationDAO alertConfigDAO;
    private final AlertService alertService;
    private final PurchaseOrderDAO poDAO;
    private final RevenueReportService revenueService;
    private final ReservationService reservationService;
    private final ScheduledExecutorService scheduler;
    private boolean isRunning = false;
    
    public AlertSchedulerService() {
        this.alertConfigDAO = new AlertConfigurationDAO();
        this.alertService = new AlertService();
        this.poDAO = new PurchaseOrderDAO();
        this.revenueService = new RevenueReportService();
        this.reservationService = new ReservationService();
        this.scheduler = Executors.newScheduledThreadPool(3);
    }
    
    /**
     * Start the scheduler
     */
    public void start() {
        if (isRunning) {
            System.out.println("⚠️ Scheduler is already running");
            return;
        }
        
        System.out.println("🚀 Starting Alert Scheduler...");
        
        // Schedule periodic checks every 5 minutes
        scheduler.scheduleAtFixedRate(
            this::checkScheduledAlerts,
            0,
            5,
            TimeUnit.MINUTES
        );
        
        // Schedule condition checks every 1 minute for real-time updates
        // 🔥 FIX: Change from 60 minutes to 1 minute for responsive notifications
        scheduler.scheduleAtFixedRate(
            this::checkConditionBasedAlerts,
            0,  // Start immediately
            1,  // Run every 1 minute
            TimeUnit.MINUTES
        );
        
        // Schedule reservation overdue checks every 5 minutes
        scheduler.scheduleAtFixedRate(
            this::checkReservationOverdue,
            0,  // Start immediately
            5,  // Run every 5 minutes
            TimeUnit.MINUTES
        );
        
        isRunning = true;
        System.out.println("✅ Alert Scheduler started");
    }
    
    /**
     * Stop the scheduler
     */
    public void stop() {
        if (!isRunning) {
            System.out.println("⚠️ Scheduler is not running");
            return;
        }
        
        System.out.println("🛑 Stopping Alert Scheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        isRunning = false;
        System.out.println("✅ Alert Scheduler stopped");
    }
    
    /**
     * Check scheduled alerts (cron-based)
     */
    private void checkScheduledAlerts() {
        try {
            System.out.println("⏰ Checking scheduled alerts...");
            
            List<AlertConfiguration> configs = alertConfigDAO.getScheduledAlertsToRun();
            
            for (AlertConfiguration config : configs) {
                System.out.println("🔔 Triggering scheduled alert: " + config.getName());
                
                switch (config.getAlertType()) {
                    case "DAILY_SUMMARY":
                        // 🚫 DISABLED: Revenue notification không còn cần thiết
                        // triggerDailySummary();
                        System.out.println("⚠️ DAILY_SUMMARY alert is disabled by user request");
                        break;
                    default:
                        System.out.println("⚠️ Unknown scheduled alert type: " + config.getAlertType());
                }
                
                // Update next run time (add 24 hours for daily)
                LocalDateTime nextRun = LocalDateTime.now().plusDays(1)
                    .with(LocalTime.of(18, 0)); // 6 PM next day
                alertConfigDAO.updateNextScheduledRun(config.getAlertID(), nextRun);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error checking scheduled alerts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check condition-based alerts
     */
    private void checkConditionBasedAlerts() {
        try {
            System.out.println("🔍 Checking condition-based alerts...");
            
            // Check PO pending alerts
            checkPOPendingAlerts();
            
            // Check PO overdue alerts
            checkPOOverdueAlerts();
            
            // Check inventory alerts (if inventory module available)
            // checkInventoryAlerts();
            
            System.out.println("✅ Condition check completed");
            
        } catch (Exception e) {
            System.err.println("❌ Error checking condition-based alerts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Trigger daily summary with REAL data from RevenueReportService
     * 🔥 UPDATED: Use RevenueReportService for 100% consistency with /report/revenue page
     */
    @SuppressWarnings("unused")
    private void triggerDailySummary() {
        System.out.println("📊 Generating daily summary using RevenueReportService...");
        
        try {
            LocalDate today = LocalDate.now();
            System.out.println("   📅 Date: " + today);
            
            // Use RevenueReportService - SAME logic as revenue report page
            JSONObject revenueData = revenueService.generateReport(today, today);
            
            // Log summary
            double totalRevenue = revenueData.optDouble("totalRevenue", 0);
            long totalOrders = revenueData.optLong("totalOrders", 0);
            double avgOrderValue = revenueData.optDouble("avgOrderValue", 0);
            
            System.out.println("   ✅ Paid orders: " + totalOrders + " orders, revenue: " + totalRevenue);
            System.out.println("   📊 Final Revenue: " + String.format("%,.0f", totalRevenue) + " VND");
            System.out.println("   📦 Total Orders: " + totalOrders);
            System.out.println("   💰 Avg/Order: " + String.format("%,.0f", avgOrderValue) + " VND");
            
            if (totalOrders == 0) {
                System.out.println("   ⚠️ No PAID orders found today - alert will show 0 VND");
            }
            
            // Trigger alert (same data as revenue report page)
            alertService.triggerDailySummary(revenueData);
            System.out.println("   ✅ Daily summary alert created with REAL data from RevenueReportService");
            
        } catch (Exception e) {
            System.err.println("❌ Error generating daily summary: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check for pending POs that need approval
     * 🔥 FIX: Use refreshPOPendingNotification() to create single summary alert
     */
    private void checkPOPendingAlerts() {
        try {
            System.out.println("🔍 Checking PO pending alerts (scheduled)...");
            
            // Use the unified refresh method that expires old alerts and creates new summary
            alertService.refreshPOPendingNotification();
            
            System.out.println("✅ PO pending summary refreshed by scheduler");
            
        } catch (Exception e) {
            System.err.println("❌ Error checking PO pending alerts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check for overdue POs
     */
    private void checkPOOverdueAlerts() {
        try {
            List<PurchaseOrder> allPOs = poDAO.getAll();
            
            for (PurchaseOrder po : allPOs) {
                if (!"APPROVED".equals(po.getStatus())) continue;
                if (po.getExpectedDelivery() == null) continue;
                
                // Check if expected delivery date has passed
                if (LocalDateTime.now().isAfter(po.getExpectedDelivery())) {
                    long daysOverdue = ChronoUnit.DAYS.between(po.getExpectedDelivery(), LocalDateTime.now());
                    
                    if (daysOverdue > 0) {
                        // Supplier name would need to be fetched from SupplierDAO
                        String supplierName = "Supplier-" + po.getSupplierID();
                        
                        alertService.triggerPOOverdue(
                            po.getPoid().toString(),
                            supplierName,
                            po.getExpectedDelivery(),
                            (int) daysOverdue
                        );
                        
                        System.out.println("✅ PO overdue alert sent for: " + po.getPoid());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error checking PO overdue alerts: " + e.getMessage());
        }
    }
    
    /**
     * Check inventory levels (placeholder)
     */
    @SuppressWarnings("unused")
    private void checkInventoryAlerts() {
      
        System.out.println("⚠️ Inventory alerts not implemented yet");
    }
    
    /**
     * Manual trigger for daily summary (for testing)
     * 🚫 DISABLED: Revenue notification không còn cần thiết
     */
    public void manualTriggerDailySummary() {
        System.out.println("🚫 Manual trigger DISABLED: Daily Summary alert has been disabled by user request");
        // triggerDailySummary();
    }
    
    /**
     * Manual trigger for PO checks (for testing)
     */
    public void manualTriggerPOChecks() {
        System.out.println("🔔 Manual trigger: PO Checks");
        checkPOPendingAlerts();
        checkPOOverdueAlerts();
    }
    
    /**
     * Check for overdue reservations (more than 30 minutes past arrival time)
     */
    private void checkReservationOverdue() {
        try {
            System.out.println("🏨 Checking overdue reservations...");
            
            int count = reservationService.autoCheckOverdue();
            
            if (count > 0) {
                System.out.println("✅ Marked " + count + " overdue reservations as NO_SHOW");
            } else {
                System.out.println("✅ No overdue reservations found");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error checking overdue reservations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get scheduler status
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Initialize next scheduled runs for all configs
     */
    public void initializeScheduledRuns() {
        System.out.println("🔄 Initializing scheduled runs...");
        
        List<AlertConfiguration> configs = alertConfigDAO.getAllEnabled();
        
        for (AlertConfiguration config : configs) {
            if (config.getScheduleCron() != null && !config.getScheduleCron().isEmpty()) {
                // Parse cron (simplified - only support daily at specific hour)
                // Format: "0 HH * * *" where HH is hour
                String cron = config.getScheduleCron();
                String[] parts = cron.split(" ");
                
                if (parts.length >= 2) {
                    try {
                        int hour = Integer.parseInt(parts[1]);
                        LocalDateTime nextRun = LocalDateTime.now()
                            .with(LocalTime.of(hour, 0));
                        
                        // If time has passed today, schedule for tomorrow
                        if (nextRun.isBefore(LocalDateTime.now())) {
                            nextRun = nextRun.plusDays(1);
                        }
                        
                        alertConfigDAO.updateNextScheduledRun(config.getAlertID(), nextRun);
                        System.out.println("✅ Scheduled: " + config.getName() + " at " + nextRun);
                        
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to parse cron for: " + config.getName());
                    }
                }
            }
        }
        
        System.out.println("✅ Scheduled runs initialized");
    }
}

