package com.liteflow.model.alert;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Alert Configuration Entity
 * Cấu hình các loại cảnh báo trong hệ thống
 */
@Entity
@Table(name = "AlertConfigurations")
public class AlertConfiguration {
    
    @Id
    @Column(name = "AlertID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID alertID;
    
    @Column(name = "AlertType", nullable = false, length = 50)
    private String alertType;
    
    @Column(name = "Name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "Description", length = 500)
    private String description;
    
    @Column(name = "IsEnabled")
    private Boolean isEnabled = true;
    
    @Column(name = "TriggerConditions", columnDefinition = "NVARCHAR(MAX)")
    private String triggerConditions; // JSON string
    
    @Column(name = "NotifySlack")
    private Boolean notifySlack = false;
    
    @Column(name = "NotifyTelegram")
    private Boolean notifyTelegram = false;
    
    @Column(name = "NotifyEmail")
    private Boolean notifyEmail = false;
    
    @Column(name = "NotifyInApp")
    private Boolean notifyInApp = true;
    
    @Column(name = "Recipients", columnDefinition = "NVARCHAR(MAX)")
    private String recipients; // JSON array of UserIDs
    
    @Column(name = "UseGPTSummary")
    private Boolean useGPTSummary = false;
    
    @Column(name = "GPTPromptTemplate", columnDefinition = "NVARCHAR(MAX)")
    private String gptPromptTemplate;
    
    @Column(name = "ScheduleCron", length = 100)
    private String scheduleCron;
    
    @Column(name = "LastTriggered")
    private LocalDateTime lastTriggered;
    
    @Column(name = "NextScheduledRun")
    private LocalDateTime nextScheduledRun;
    
    @Column(name = "Priority", length = 20)
    private String priority = "MEDIUM";
    
    @Column(name = "CreatedBy", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID createdBy;
    
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    
    @Column(name = "TotalTriggered")
    private Integer totalTriggered = 0;
    
    @Column(name = "LastTriggeredBy", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID lastTriggeredBy;
    
    // Constructors
    public AlertConfiguration() {
        this.alertID = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public AlertConfiguration(String alertType, String name) {
        this();
        this.alertType = alertType;
        this.name = name;
    }
    
    // Getters and Setters
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Boolean enabled) {
        isEnabled = enabled;
    }
    
    public String getTriggerConditions() {
        return triggerConditions;
    }
    
    public void setTriggerConditions(String triggerConditions) {
        this.triggerConditions = triggerConditions;
    }
    
    public Boolean getNotifySlack() {
        return notifySlack;
    }
    
    public void setNotifySlack(Boolean notifySlack) {
        this.notifySlack = notifySlack;
    }
    
    public Boolean getNotifyTelegram() {
        return notifyTelegram;
    }
    
    public void setNotifyTelegram(Boolean notifyTelegram) {
        this.notifyTelegram = notifyTelegram;
    }
    
    public Boolean getNotifyEmail() {
        return notifyEmail;
    }
    
    public void setNotifyEmail(Boolean notifyEmail) {
        this.notifyEmail = notifyEmail;
    }
    
    public Boolean getNotifyInApp() {
        return notifyInApp;
    }
    
    public void setNotifyInApp(Boolean notifyInApp) {
        this.notifyInApp = notifyInApp;
    }
    
    public String getRecipients() {
        return recipients;
    }
    
    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }
    
    public Boolean getUseGPTSummary() {
        return useGPTSummary;
    }
    
    public void setUseGPTSummary(Boolean useGPTSummary) {
        this.useGPTSummary = useGPTSummary;
    }
    
    public String getGptPromptTemplate() {
        return gptPromptTemplate;
    }
    
    public void setGptPromptTemplate(String gptPromptTemplate) {
        this.gptPromptTemplate = gptPromptTemplate;
    }
    
    public String getScheduleCron() {
        return scheduleCron;
    }
    
    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }
    
    public LocalDateTime getLastTriggered() {
        return lastTriggered;
    }
    
    public void setLastTriggered(LocalDateTime lastTriggered) {
        this.lastTriggered = lastTriggered;
    }
    
    public LocalDateTime getNextScheduledRun() {
        return nextScheduledRun;
    }
    
    public void setNextScheduledRun(LocalDateTime nextScheduledRun) {
        this.nextScheduledRun = nextScheduledRun;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public UUID getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getTotalTriggered() {
        return totalTriggered;
    }
    
    public void setTotalTriggered(Integer totalTriggered) {
        this.totalTriggered = totalTriggered;
    }
    
    public UUID getLastTriggeredBy() {
        return lastTriggeredBy;
    }
    
    public void setLastTriggeredBy(UUID lastTriggeredBy) {
        this.lastTriggeredBy = lastTriggeredBy;
    }
    
    // Utility Methods
    @PrePersist
    protected void onCreate() {
        if (alertID == null) {
            alertID = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if alert should run now based on schedule
     */
    public boolean shouldRunNow() {
        if (nextScheduledRun == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(nextScheduledRun);
    }
    
    /**
     * Check if any notification channel is enabled
     */
    public boolean hasNotificationChannels() {
        return Boolean.TRUE.equals(notifySlack) || 
               Boolean.TRUE.equals(notifyTelegram) || 
               Boolean.TRUE.equals(notifyEmail) || 
               Boolean.TRUE.equals(notifyInApp);
    }
    
    @Override
    public String toString() {
        return "AlertConfiguration{" +
                "alertID=" + alertID +
                ", alertType='" + alertType + '\'' +
                ", name='" + name + '\'' +
                ", priority='" + priority + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }
}


