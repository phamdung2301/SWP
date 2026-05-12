package com.liteflow.model.alert;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Alert History Entity
 * Lịch sử các cảnh báo đã gửi
 */
@Entity
@Table(name = "AlertHistory")
public class AlertHistory {
    
    @Id
    @Column(name = "HistoryID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID historyID;
    
    @Column(name = "AlertID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID alertID;
    
    @Column(name = "AlertType", nullable = false, length = 50)
    private String alertType;
    
    @Column(name = "Title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "Message", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String message;
    
    @Column(name = "MessageHTML", columnDefinition = "NVARCHAR(MAX)")
    private String messageHTML;
    
    @Column(name = "ContextData", columnDefinition = "NVARCHAR(MAX)")
    private String contextData; // JSON
    
    // AI-Generated Content
    @Column(name = "GPTSummary", columnDefinition = "NVARCHAR(MAX)")
    private String gptSummary;
    
    @Column(name = "GPTInteractionID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID gptInteractionID;
    
    // Delivery Status
    @Column(name = "SentToSlack")
    private Boolean sentToSlack = false;
    
    @Column(name = "SentToTelegram")
    private Boolean sentToTelegram = false;
    
    @Column(name = "SentToEmail")
    private Boolean sentToEmail = false;
    
    @Column(name = "SentInApp")
    private Boolean sentInApp = false;
    
    @Column(name = "DeliveryStatus", length = 20)
    private String deliveryStatus = "PENDING";
    
    @Column(name = "ErrorMessage", length = 500)
    private String errorMessage;
    
    // User Interaction
    @Column(name = "IsRead")
    private Boolean isRead = false;
    
    @Column(name = "ReadAt")
    private LocalDateTime readAt;
    
    @Column(name = "ReadBy", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID readBy;
    
    @Column(name = "IsDismissed")
    private Boolean isDismissed = false;
    
    @Column(name = "DismissedAt")
    private LocalDateTime dismissedAt;
    
    @Column(name = "DismissedBy", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID dismissedBy;
    
    // Action Taken
    @Column(name = "ActionTaken", length = 100)
    private String actionTaken;
    
    @Column(name = "ActionTakenAt")
    private LocalDateTime actionTakenAt;
    
    @Column(name = "ActionTakenBy", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID actionTakenBy;
    
    // Priority
    @Column(name = "Priority", length = 20)
    private String priority = "MEDIUM";
    
    // Timestamps
    @Column(name = "TriggeredAt")
    private LocalDateTime triggeredAt;
    
    @Column(name = "SentAt")
    private LocalDateTime sentAt;
    
    @Column(name = "ExpiresAt")
    private LocalDateTime expiresAt;
    
    // Constructors
    public AlertHistory() {
        this.historyID = UUID.randomUUID();
        this.triggeredAt = LocalDateTime.now();
    }
    
    public AlertHistory(String alertType, String title, String message) {
        this();
        this.alertType = alertType;
        this.title = title;
        this.message = message;
    }
    
    // Getters and Setters
    public UUID getHistoryID() {
        return historyID;
    }
    
    public void setHistoryID(UUID historyID) {
        this.historyID = historyID;
    }
    
    public UUID getAlertID() {
        return alertID;
    }
    
    public void setAlertID(UUID alertID) {
        this.alertID = alertID;
    }
    
    public String getAlertType() {
        return alertType;
    }
    
    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMessageHTML() {
        return messageHTML;
    }
    
    public void setMessageHTML(String messageHTML) {
        this.messageHTML = messageHTML;
    }
    
    public String getContextData() {
        return contextData;
    }
    
    public void setContextData(String contextData) {
        this.contextData = contextData;
    }
    
    public String getGptSummary() {
        return gptSummary;
    }
    
    public void setGptSummary(String gptSummary) {
        this.gptSummary = gptSummary;
    }
    
    public UUID getGptInteractionID() {
        return gptInteractionID;
    }
    
    public void setGptInteractionID(UUID gptInteractionID) {
        this.gptInteractionID = gptInteractionID;
    }
    
    public Boolean getSentToSlack() {
        return sentToSlack;
    }
    
    public void setSentToSlack(Boolean sentToSlack) {
        this.sentToSlack = sentToSlack;
    }
    
    public Boolean getSentToTelegram() {
        return sentToTelegram;
    }
    
    public void setSentToTelegram(Boolean sentToTelegram) {
        this.sentToTelegram = sentToTelegram;
    }
    
    public Boolean getSentToEmail() {
        return sentToEmail;
    }
    
    public void setSentToEmail(Boolean sentToEmail) {
        this.sentToEmail = sentToEmail;
    }
    
    public Boolean getSentInApp() {
        return sentInApp;
    }
    
    public void setSentInApp(Boolean sentInApp) {
        this.sentInApp = sentInApp;
    }
    
    public String getDeliveryStatus() {
        return deliveryStatus;
    }
    
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean read) {
        isRead = read;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public UUID getReadBy() {
        return readBy;
    }
    
    public void setReadBy(UUID readBy) {
        this.readBy = readBy;
    }
    
    public Boolean getIsDismissed() {
        return isDismissed;
    }
    
    public void setIsDismissed(Boolean dismissed) {
        isDismissed = dismissed;
    }
    
    public LocalDateTime getDismissedAt() {
        return dismissedAt;
    }
    
    public void setDismissedAt(LocalDateTime dismissedAt) {
        this.dismissedAt = dismissedAt;
    }
    
    public UUID getDismissedBy() {
        return dismissedBy;
    }
    
    public void setDismissedBy(UUID dismissedBy) {
        this.dismissedBy = dismissedBy;
    }
    
    public String getActionTaken() {
        return actionTaken;
    }
    
    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }
    
    public LocalDateTime getActionTakenAt() {
        return actionTakenAt;
    }
    
    public void setActionTakenAt(LocalDateTime actionTakenAt) {
        this.actionTakenAt = actionTakenAt;
    }
    
    public UUID getActionTakenBy() {
        return actionTakenBy;
    }
    
    public void setActionTakenBy(UUID actionTakenBy) {
        this.actionTakenBy = actionTakenBy;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }
    
    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    // Utility Methods
    @PrePersist
    protected void onCreate() {
        if (historyID == null) {
            historyID = UUID.randomUUID();
        }
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
    }
    
    /**
     * Mark alert as read
     */
    public void markAsRead(UUID userId) {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        this.readBy = userId;
    }
    
    /**
     * Dismiss alert
     */
    public void dismiss(UUID userId) {
        this.isDismissed = true;
        this.dismissedAt = LocalDateTime.now();
        this.dismissedBy = userId;
    }
    
    /**
     * Record action taken
     */
    public void recordAction(String action, UUID userId) {
        this.actionTaken = action;
        this.actionTakenAt = LocalDateTime.now();
        this.actionTakenBy = userId;
    }
    
    /**
     * Check if alert is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if alert is active (not read, not dismissed, not expired)
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(isRead) && 
               !Boolean.TRUE.equals(isDismissed) && 
               !isExpired();
    }
    
    /**
     * Get time ago in minutes
     */
    public long getMinutesAgo() {
        return java.time.Duration.between(triggeredAt, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * Mark as sent to channel
     */
    public void markSentToChannel(String channelType) {
        switch (channelType.toUpperCase()) {
            case "SLACK":
                this.sentToSlack = true;
                break;
            case "TELEGRAM":
                this.sentToTelegram = true;
                break;
            case "EMAIL":
                this.sentToEmail = true;
                break;
            case "INAPP":
                this.sentInApp = true;
                break;
        }
        
        // Update delivery status
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
        
        // Check if any channel was sent successfully
        if (Boolean.TRUE.equals(sentToSlack) || 
            Boolean.TRUE.equals(sentToTelegram) || 
            Boolean.TRUE.equals(sentToEmail) || 
            Boolean.TRUE.equals(sentInApp)) {
            deliveryStatus = "SENT";
        }
    }
    
    @Override
    public String toString() {
        return "AlertHistory{" +
                "historyID=" + historyID +
                ", alertType='" + alertType + '\'' +
                ", title='" + title + '\'' +
                ", priority='" + priority + '\'' +
                ", deliveryStatus='" + deliveryStatus + '\'' +
                ", triggeredAt=" + triggeredAt +
                '}';
    }
}

