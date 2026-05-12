package com.liteflow.service.alert;

import com.liteflow.dao.alert.*;
import com.liteflow.model.alert.*;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Core Alert Service
 * Handles alert triggering, processing, and delivery
 */
public class AlertService {
    
    private final AlertConfigurationDAO alertConfigDAO;
    private final AlertHistoryDAO alertHistoryDAO;
    @SuppressWarnings("unused")
    private final UserAlertPreferenceDAO userPrefDAO;
    private final NotificationService notificationService;
    private final GPTService gptService;
    
    public AlertService() {
        this.alertConfigDAO = new AlertConfigurationDAO();
        this.alertHistoryDAO = new AlertHistoryDAO();
        this.userPrefDAO = new UserAlertPreferenceDAO();
        this.notificationService = new NotificationService();
        this.gptService = new GPTService();
    }
    
    /**
     * Trigger an alert
     */
    public UUID triggerAlert(String alertType, String title, String message, 
                            JSONObject contextData, String priority) {
        
        System.out.println("🔔 Triggering alert: " + alertType + " - " + title);
        
        // Get alert configuration
        List<AlertConfiguration> configs = alertConfigDAO.getByType(alertType);
        if (configs.isEmpty()) {
            System.err.println("⚠️ No configuration found for alert type: " + alertType);
            return null;
        }
        
        AlertConfiguration config = configs.get(0);
        
        // Create alert history
        AlertHistory alert = new AlertHistory(alertType, title, message);
        alert.setAlertID(config.getAlertID());
        alert.setContextData(contextData != null ? contextData.toString() : null);
        alert.setPriority(priority != null ? priority : config.getPriority());
        alert.setDeliveryStatus("PENDING");
        
        // Generate GPT summary if enabled
        if (Boolean.TRUE.equals(config.getUseGPTSummary()) && gptService.isAvailable() && contextData != null) {
            try {
                String gptSummary = gptService.generateAlertSummary(alertType, contextData);
                alert.setGptSummary(gptSummary);
                
                // Use GPT summary as message if available
                if (gptSummary != null && !gptSummary.isEmpty() && !gptSummary.contains("không khả dụng")) {
                    alert.setMessage(message + "\n\n📊 AI Analysis:\n" + gptSummary);
                }
            } catch (Exception e) {
                System.err.println("⚠️ GPT summary generation failed: " + e.getMessage());
            }
        }
        
        // Save to database
        boolean saved = alertHistoryDAO.insert(alert);
        if (!saved) {
            System.err.println("❌ Failed to save alert to database");
            return null;
        }
        
        // Send notifications
        deliverAlert(alert, config);
        
        // 🔥 FIX: Update deliveryStatus to database after delivery
        boolean updated = alertHistoryDAO.update(alert);
        if (!updated) {
            System.err.println("⚠️ Failed to update alert delivery status (non-critical)");
        }
        
        // Update last triggered
        alertConfigDAO.updateLastTriggered(config.getAlertID(), null);
        
        return alert.getHistoryID();
    }
    
    /**
     * Deliver alert to appropriate channels
     */
    private void deliverAlert(AlertHistory alert, AlertConfiguration config) {
        String title = alert.getTitle();
        String message = alert.getMessage();
        String priority = alert.getPriority();
        
        boolean anySuccess = false;
        
        // Send to Slack
        if (Boolean.TRUE.equals(config.getNotifySlack())) {
            boolean sent = notificationService.sendToDefaultSlack(title, message, priority);
            if (sent) {
                alert.setSentToSlack(true);
                anySuccess = true;
            }
        }
        
        // Send to Telegram
        if (Boolean.TRUE.equals(config.getNotifyTelegram())) {
            boolean sent = notificationService.sendToDefaultTelegram(title, message, priority);
            if (sent) {
                alert.setSentToTelegram(true);
                anySuccess = true;
            }
        }
        
        // Send to Email
        if (Boolean.TRUE.equals(config.getNotifyEmail())) {
        
            System.out.println("📧 Email notification queued for: " + title);
            alert.setSentToEmail(true);
            anySuccess = true;
        }
        
      
        // 🔥 FIX: Force in-app notification to always be sent
        alert.setSentInApp(true);
        anySuccess = true;
        
        // Update delivery status
        if (anySuccess) {
            alert.setDeliveryStatus("SENT");
            alert.setSentAt(LocalDateTime.now());
        } else {
            // This should never happen now since in-app is always sent
            alert.setDeliveryStatus("FAILED");
            alert.setErrorMessage("No channels delivered successfully");
        }
        
        alertHistoryDAO.update(alert);
    }
    
    /**
     * Trigger Daily Summary alert
     */
    public UUID triggerDailySummary(JSONObject revenueData) {
        String title = "📊 Báo cáo doanh thu hôm nay";
        
        // Build summary message
        StringBuilder message = new StringBuilder();
        message.append("**📊 BÁO CÁO DOANH THU HÔM NAY**\n\n");
        
        if (revenueData.has("totalRevenue")) {
            message.append("💰 Tổng doanh thu: ")
                   .append(String.format("%,.0f VND", revenueData.getDouble("totalRevenue")))
                   .append("\n");
        }
        if (revenueData.has("totalOrders")) {
            message.append("🛒 Số đơn hàng: ")
                   .append(revenueData.getInt("totalOrders"))
                   .append("\n");
        }
        if (revenueData.has("avgOrderValue")) {
            message.append("📈 Giá trị TB/đơn: ")
                   .append(String.format("%,.0f VND", revenueData.getDouble("avgOrderValue")))
                   .append("\n");
        }
        
        return triggerAlert("DAILY_SUMMARY", title, message.toString(), revenueData, "MEDIUM");
    }
    
    /**
     * Trigger PO Pending Summary alert (for multiple pending POs)
     * @param totalPendingCount Total number of pending POs
     * @param criticalCount Number of critical priority POs
     * @param highCount Number of high priority POs
     * @param totalValue Total value of all pending POs
     * @return Alert history ID
     */
    public UUID triggerPOPendingSummary(int totalPendingCount, int criticalCount, int highCount, double totalValue) {
        // Determine priority based on counts
        String priority = "MEDIUM";
        String urgencyEmoji = "📋";
        
        if (criticalCount > 0 || totalPendingCount >= 10) {
            priority = "CRITICAL";
            urgencyEmoji = "🚨";
        } else if (highCount > 0 || totalPendingCount >= 5) {
            priority = "HIGH";
            urgencyEmoji = "⚠️";
        }
        
        String title = String.format("%s Có %d đơn hàng đang chờ duyệt", urgencyEmoji, totalPendingCount);
        
        String criticalInfo = criticalCount > 0 
            ? String.format("\n🚨 **Khẩn cấp:** %d đơn yêu cầu xử lý gấp (≥50M hoặc Level 3)", criticalCount)
            : "";
        
        String highInfo = highCount > 0
            ? String.format("\n⚠️ **Ưu tiên cao:** %d đơn cần duyệt sớm (≥10M hoặc Level 2)", highCount)
            : "";
        
        String message = String.format(
            "**THÔNG BÁO: ĐƠN HÀNG CHỜ DUYỆT**\n\n" +
            "📊 **Tổng quan:**\n" +
            "   • Tổng số đơn chờ duyệt: **%d đơn**\n" +
            "   • Tổng giá trị: **%,.0f VND**%s%s\n\n" +
            "🎯 **Hành động cần làm:**\n" +
            "1. ✅ Truy cập trang quản lý đơn hàng: /procurement/po\n" +
            "2. ✅ Xem danh sách đơn hàng chờ duyệt\n" +
            "3. ✅ Ưu tiên xử lý đơn khẩn cấp trước\n" +
            "4. ✅ Phê duyệt hoặc từ chối các đơn hàng\n\n" +
            "%s",
            totalPendingCount,
            totalValue,
            criticalInfo,
            highInfo,
            criticalCount > 0 ? "⚠️ **LƯU Ý:** Có đơn hàng KHẨN CẤP cần xử lý ngay!" :
            highCount > 0 ? "💡 **Lưu ý:** Nên xử lý sớm để tránh chậm trễ." :
            totalPendingCount >= 5 ? "💡 **Lưu ý:** Số lượng đơn chờ duyệt đang tăng cao." :
            "💡 Vui lòng xem xét và phê duyệt khi có thời gian."
        );
        
        JSONObject context = new JSONObject();
        context.put("totalPendingCount", totalPendingCount);
        context.put("criticalCount", criticalCount);
        context.put("highCount", highCount);
        context.put("totalValue", totalValue);
        context.put("checkTime", java.time.LocalDateTime.now().toString());
        
        return triggerAlert("PO_PENDING", title, message, context, priority);
    }
    
    /**
     * Trigger PO Pending alert (legacy - backward compatibility)
     */
    public UUID triggerPOPending(String poId, String supplierName, double amount, int daysWaiting) {
        return triggerPOPending(null, poId, supplierName, amount, daysWaiting, 1);
    }
    
    /**
     * Trigger PO Pending alert with full details
     * @param poid Purchase Order UUID (for spam prevention)
     * @param poId PO ID string for display
     * @param supplierName Supplier name
     * @param amount Total amount in VND
     * @param daysWaiting Days since created
     * @param approvalLevel Required approval level (1-3)
     * @return Alert history ID
     */
    public UUID triggerPOPending(UUID poid, String poId, String supplierName, double amount, int daysWaiting, int approvalLevel) {
        // Determine priority based on amount, approval level, and days waiting
        String priority = "MEDIUM";
        String urgencyEmoji = "📋";
        
        // CRITICAL: Very high value OR very long wait OR requires Board approval
        if (amount >= 50_000_000 || daysWaiting >= 5 || approvalLevel >= 3) {
            priority = "CRITICAL";
            urgencyEmoji = "🚨";
        } 
        // HIGH: High value OR long wait OR requires Director approval
        else if (amount >= 10_000_000 || daysWaiting >= 3 || approvalLevel >= 2) {
            priority = "HIGH";
            urgencyEmoji = "⚠️";
        }
        // MEDIUM: Default for manager approval and lower amounts
        else {
            priority = "MEDIUM";
            urgencyEmoji = "⏳";
        }
        
        // Approval level text
        String approvalText = switch (approvalLevel) {
            case 3 -> "Board Approval (Level 3)";
            case 2 -> "Director Approval (Level 2)";
            default -> "Manager Approval (Level 1)";
        };
        
        // Title varies based on days waiting
        String title;
        if (daysWaiting == 0) {
            title = String.format("%s Đơn hàng mới cần duyệt", urgencyEmoji);
        } else {
            title = String.format("%s Đơn hàng cần duyệt - %d ngày", urgencyEmoji, daysWaiting);
        }
        
        // Status text based on days waiting
        String statusText;
        if (daysWaiting == 0) {
            statusText = "Mới tạo - Cần duyệt ngay";
        } else if (daysWaiting == 1) {
            statusText = "1 ngày";
        } else {
            statusText = daysWaiting + " ngày";
        }
        
        String message = String.format(
            "**ĐƠN ĐẶT HÀNG CHỜ PHÊ DUYỆT**\n\n" +
            "📋 **Mã đơn:** %s\n" +
            "🏢 **Nhà cung cấp:** %s\n" +
            "💵 **Giá trị:** %,.0f VND\n" +
            "📅 **Thời gian chờ:** %s\n" +
            "👔 **Cấp duyệt:** %s\n" +
            "⏰ **Ngày tạo:** " + java.time.LocalDateTime.now().minusDays(daysWaiting).format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            ) + "\n\n" +
            "**🎯 HÀNH ĐỘNG CẦN LÀM:**\n" +
            "1. ✅ Xem chi tiết đơn hàng tại /procurement/po\n" +
            "2. ✅ Kiểm tra thông tin nhà cung cấp và giá\n" +
            "3. ✅ Xác nhận ngân sách và nhu cầu\n" +
            "4. ✅ Phê duyệt hoặc từ chối đơn hàng\n\n" +
            "%s",
            poId, 
            supplierName, 
            amount, 
            statusText,
            approvalText,
            daysWaiting >= 5 ? "⚠️ **CẢNH BÁO:** Đơn hàng chờ quá lâu, có thể ảnh hưởng đến kế hoạch mua hàng!" : 
            daysWaiting >= 3 ? "💡 **Lưu ý:** Nên xử lý sớm để tránh chậm trễ." : 
            daysWaiting == 0 ? "⚡ **Đơn hàng mới:** Vui lòng xem xét và phê duyệt." :
            "💡 Vui lòng xem xét và phê duyệt khi có thời gian."
        );
        
        JSONObject context = new JSONObject();
        if (poid != null) {
            context.put("poid", poid.toString());
        }
        context.put("poId", poId);
        context.put("supplierName", supplierName);
        context.put("amount", amount);
        context.put("daysWaiting", daysWaiting);
        context.put("approvalLevel", approvalLevel);
        context.put("priority", priority);
        context.put("detectedAt", java.time.LocalDateTime.now().toString());
        
        return triggerAlert("PO_PENDING", title, message, context, priority);
    }
    
    /**
     * Trigger Low Inventory alert
     * @param productName Full product name (with size)
     * @param currentStock Current stock level
     * @param threshold Alert threshold
     * @return Alert history ID
     */
    public UUID triggerLowInventory(String productName, int currentStock, int threshold) {
        return triggerLowInventory(null, productName, currentStock, threshold);
    }
    
    /**
     * Trigger Low Inventory alert with product variant ID
     * @param productVariantID Product variant UUID (for spam prevention)
     * @param productName Full product name (with size)
     * @param currentStock Current stock level
     * @param threshold Alert threshold
     * @return Alert history ID
     */
    public UUID triggerLowInventory(UUID productVariantID, String productName, int currentStock, int threshold) {
        String title = "📦 Cảnh báo tồn kho thấp";
        
        String message = String.format(
            "Sản phẩm **%s** sắp hết hàng!\n\n" +
            "📊 Tồn kho hiện tại: %d\n" +
            "⚠️ Ngưỡng cảnh báo: %d\n\n" +
            "Cần đặt hàng bổ sung.",
            productName, currentStock, threshold
        );
        
        JSONObject context = new JSONObject();
        if (productVariantID != null) {
            context.put("productVariantID", productVariantID.toString());
        }
        context.put("productName", productName);
        context.put("currentStock", currentStock);
        context.put("threshold", threshold);
        
        return triggerAlert("LOW_INVENTORY", title, message, context, "MEDIUM");
    }
    
    /**
     * Trigger Out of Stock alert
     * @param productName Full product name (with size)
     * @return Alert history ID
     */
    public UUID triggerOutOfStock(String productName) {
        return triggerOutOfStock(null, productName);
    }
    
    /**
     * Trigger Out of Stock alert with product variant ID
     * @param productVariantID Product variant UUID (for spam prevention)
     * @param productName Full product name (with size)
     * @return Alert history ID
     */
    public UUID triggerOutOfStock(UUID productVariantID, String productName) {
        String title = "🚨 SẢN PHẨM HẾT HÀNG - CẦN XỬ LÝ GẤP";
        
        String message = String.format(
            "**CẢNH BÁO NGHIÊM TRỌNG:** Sản phẩm **%s** đã HẾT HÀNG!\n\n" +
            "📊 **Tồn kho hiện tại:** 0 (KHÔNG CÓ HÀNG)\n" +
            "🚨 **Mức độ:** CRITICAL - Cần xử lý ngay\n" +
            "⏰ **Thời gian:** " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            ) + "\n\n" +
            "**🎯 HÀNH ĐỘNG CẦN LÀM:**\n" +
            "1. ✅ Kiểm tra đơn đặt hàng đang pending\n" +
            "2. ✅ Liên hệ nhà cung cấp NGAY để đặt hàng gấp\n" +
            "3. ✅ Cập nhật trạng thái sản phẩm trên menu\n" +
            "4. ✅ Thông báo cho nhân viên bán hàng\n\n" +
            "⚠️ **Lưu ý:** Sản phẩm hết hàng có thể làm mất khách hàng và doanh thu!",
            productName
        );
        
        JSONObject context = new JSONObject();
        if (productVariantID != null) {
            context.put("productVariantID", productVariantID.toString());
        }
        context.put("productName", productName);
        context.put("stockLevel", 0);
        context.put("severity", "CRITICAL");
        context.put("detectedAt", java.time.LocalDateTime.now().toString());
        
        return triggerAlert("OUT_OF_STOCK", title, message, context, "CRITICAL");
    }
    
    /**
     * Trigger Revenue Anomaly alert
     */
    public UUID triggerRevenueAnomaly(double currentRevenue, double averageRevenue, double deviationPercent) {
        String title = "📈 Phát hiện bất thường doanh thu";
        
        String direction = currentRevenue > averageRevenue ? "cao hơn" : "thấp hơn";
        String emoji = currentRevenue > averageRevenue ? "📈" : "📉";
        
        String message = String.format(
            "%s Doanh thu hiện tại **%s %.1f%%** so với trung bình!\n\n" +
            "💵 Doanh thu hiện tại: %,.0f VND\n" +
            "📊 Trung bình: %,.0f VND\n" +
            "📈 Chênh lệch: %.1f%%",
            emoji, direction, Math.abs(deviationPercent),
            currentRevenue, averageRevenue, Math.abs(deviationPercent)
        );
        
        JSONObject context = new JSONObject();
        context.put("currentRevenue", currentRevenue);
        context.put("averageRevenue", averageRevenue);
        context.put("deviationPercent", deviationPercent);
        
        return triggerAlert("REVENUE_ANOMALY", title, message, context, "HIGH");
    }
    
    /**
     * Trigger PO Overdue alert
     */
    public UUID triggerPOOverdue(String poId, String supplierName, LocalDateTime expectedDelivery, int daysOverdue) {
        String title = "⏰ Đơn đặt hàng quá hạn giao";
        
        String message = String.format(
            "Đơn đặt hàng **%s** từ nhà cung cấp **%s** đã quá hạn giao %d ngày!\n\n" +
            "📅 Ngày giao dự kiến: %s\n" +
            "⏰ Quá hạn: %d ngày\n\n" +
            "Cần liên hệ nhà cung cấp.",
            poId, supplierName, daysOverdue,
            expectedDelivery.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            daysOverdue
        );
        
        JSONObject context = new JSONObject();
        context.put("poId", poId);
        context.put("supplierName", supplierName);
        context.put("expectedDelivery", expectedDelivery.toString());
        context.put("daysOverdue", daysOverdue);
        
        return triggerAlert("PO_OVERDUE", title, message, context, "HIGH");
    }
    
    /**
     * Trigger High Value PO alert
     */
    public UUID triggerHighValuePO(String poId, String supplierName, double amount, double threshold) {
        String title = "💰 Đơn đặt hàng giá trị cao";
        
        String message = String.format(
            "Đơn đặt hàng **%s** từ nhà cung cấp **%s** có giá trị cao, cần phê duyệt.\n\n" +
            "💵 Giá trị: %,.0f VND\n" +
            "⚠️ Ngưỡng: %,.0f VND\n\n" +
            "Vui lòng xem xét kỹ trước khi phê duyệt.",
            poId, supplierName, amount, threshold
        );
        
        JSONObject context = new JSONObject();
        context.put("poId", poId);
        context.put("supplierName", supplierName);
        context.put("amount", amount);
        context.put("threshold", threshold);
        
        return triggerAlert("PO_HIGH_VALUE", title, message, context, "HIGH");
    }
    
    /**
     * Get unread alerts count
     */
    public long getUnreadCount() {
        return alertHistoryDAO.getUnreadCount();
    }
    
    /**
     * Get unread alerts
     */
    public List<AlertHistory> getUnreadAlerts(int limit) {
        return alertHistoryDAO.getUnreadAlerts(limit);
    }
    
    /**
     * Get active alerts
     */
    public List<AlertHistory> getActiveAlerts(int limit) {
        return alertHistoryDAO.getActiveAlerts(limit);
    }
    
    /**
     * Get recent alerts
     */
    public List<AlertHistory> getRecentAlerts(int hours, int limit) {
        return alertHistoryDAO.getRecentAlerts(hours, limit);
    }
    
    /**
     * Mark alert as read
     */
    public boolean markAsRead(UUID historyID, UUID userId) {
        return alertHistoryDAO.markAsRead(historyID, userId);
    }
    
    /**
     * Mark all as read
     */
    public int markAllAsRead(UUID userId) {
        return alertHistoryDAO.markAllAsRead(userId);
    }
    
    /**
     * Dismiss alert
     */
    public boolean dismissAlert(UUID historyID, UUID userId) {
        return alertHistoryDAO.dismiss(historyID, userId);
    }
    
    /**
     * Get alert by ID
     */
    public AlertHistory getAlertById(UUID historyID) {
        return alertHistoryDAO.getById(historyID);
    }
    
    /**
     * Get alert statistics
     */
    public JSONObject getStatistics() {
        JSONObject stats = new JSONObject();
        
        try {
            stats.put("totalAlerts", alertHistoryDAO.getTotalCount());
            stats.put("unreadCount", alertHistoryDAO.getUnreadCount());
            stats.put("sentCount", alertHistoryDAO.getCountByStatus("SENT"));
            stats.put("failedCount", alertHistoryDAO.getCountByStatus("FAILED"));
            
            // Recent alerts by priority
            JSONObject byPriority = new JSONObject();
            byPriority.put("critical", alertHistoryDAO.getByPriority("CRITICAL", 10).size());
            byPriority.put("high", alertHistoryDAO.getByPriority("HIGH", 10).size());
            byPriority.put("medium", alertHistoryDAO.getByPriority("MEDIUM", 10).size());
            byPriority.put("low", alertHistoryDAO.getByPriority("LOW", 10).size());
            stats.put("byPriority", byPriority);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to get alert statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Create manual alert (sent by admin to employees)
     */
    public UUID createManualAlert(String title, String message, String priority, UUID createdByUserId) {
        System.out.println("📨 Creating manual alert from admin: " + title);

        // Create alert history directly without configuration check
        AlertHistory alert = new AlertHistory("MANUAL_NOTIFICATION", title, message);
        alert.setPriority(priority != null ? priority : "MEDIUM");
        alert.setDeliveryStatus("PENDING");

        // Set context data to include creator info
        JSONObject contextData = new JSONObject();
        contextData.put("createdBy", createdByUserId.toString());
        contextData.put("createdAt", LocalDateTime.now().toString());
        contextData.put("type", "admin_notification");
        alert.setContextData(contextData.toString());

        // Save to database
        boolean saved = alertHistoryDAO.insert(alert);
        if (!saved) {
            System.err.println("❌ Failed to save manual alert to database");
            return null;
        }

        // For manual alerts, only send in-app notification
        alert.setSentInApp(true);
        alert.setDeliveryStatus("SENT");
        alert.setSentAt(LocalDateTime.now());

        // Update the alert with delivery status
        boolean updated = alertHistoryDAO.update(alert);
        if (!updated) {
            System.err.println("⚠️ Failed to update manual alert delivery status (non-critical)");
        }

        System.out.println("✅ Manual alert created successfully: " + alert.getHistoryID());
        return alert.getHistoryID();
    }

    /**
     * Cleanup old alerts
     */
    public int cleanupOldAlerts(int daysOld) {
        return alertHistoryDAO.deleteOldAlerts(daysOld);
    }
    
    /**
     * Get all alert configurations
     */
    public List<AlertConfiguration> getAllConfigurations() {
        return alertConfigDAO.getAll();
    }
    
    /**
     * Get enabled configurations
     */
    public List<AlertConfiguration> getEnabledConfigurations() {
        return alertConfigDAO.getAllEnabled();
    }
    
    /**
     * Update alert configuration
     */
    public boolean updateConfiguration(AlertConfiguration config) {
        return alertConfigDAO.update(config);
    }
    
    /**
     * Enable/disable alert configuration
     */
    public boolean setConfigurationEnabled(UUID alertID, boolean enabled) {
        return alertConfigDAO.setEnabled(alertID, enabled);
    }
    
    /**
     * Refresh PO pending notification immediately after approve/reject
     * This method is called from Servlet after PO status changes
     */
    public void refreshPOPendingNotification() {
        try {
            System.out.println("⚡ Refreshing PO pending notification...");
            
            // Import DAOs here to avoid circular dependency
            com.liteflow.dao.procurement.PurchaseOrderDAO poDAO = new com.liteflow.dao.procurement.PurchaseOrderDAO();
            
            // Count current pending POs
            List<com.liteflow.model.procurement.PurchaseOrder> pendingPOs = poDAO.findPending();
            int totalPending = pendingPOs.size();
            
            System.out.println("   Current pending POs: " + totalPending);
            
            // Expire old PO_PENDING alerts
            int expiredCount = alertHistoryDAO.expireOldAlertsByType("PO_PENDING");
            System.out.println("   Expired old alerts: " + expiredCount);
            
            // If no pending POs, don't create alert
            if (totalPending == 0) {
                System.out.println("   No pending POs - no alert needed");
                return;
            }
            
            // Calculate priority breakdown
            int criticalCount = 0;
            int highCount = 0;
            double totalValue = 0;
            
            for (com.liteflow.model.procurement.PurchaseOrder po : pendingPOs) {
                double amount = (po.getTotalAmount() != null) ? po.getTotalAmount() : 0;
                totalValue += amount;
                
                if (amount >= 50_000_000) {
                    criticalCount++;
                } else if (amount >= 10_000_000) {
                    highCount++;
                }
            }
            
            // Trigger new summary alert
            triggerPOPendingSummary(totalPending, criticalCount, highCount, totalValue);
            
            System.out.println("✅ PO pending notification refreshed successfully");
            
        } catch (Exception e) {
            System.err.println("⚠️ Failed to refresh PO notification: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - this is optional enhancement
        }
    }
}

