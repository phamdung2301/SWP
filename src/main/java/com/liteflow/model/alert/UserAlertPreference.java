package com.liteflow.model.alert;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * User Alert Preference Entity
 * Tùy chọn cá nhân của từng user về alerts
 */
@Entity
@Table(name = "UserAlertPreferences")
public class UserAlertPreference {
    
    @Id
    @Column(name = "PreferenceID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID preferenceID;
    
    @Column(name = "UserID", nullable = false, unique = true, columnDefinition = "UNIQUEIDENTIFIER")
    private UUID userID;
    
    // Global Settings
    @Column(name = "EnableNotifications")
    private Boolean enableNotifications = true;
    
    @Column(name = "EnableSlack")
    private Boolean enableSlack = true;
    
    @Column(name = "EnableTelegram")
    private Boolean enableTelegram = false;
    
    @Column(name = "EnableEmail")
    private Boolean enableEmail = false;
    
    @Column(name = "EnableInApp")
    private Boolean enableInApp = true;
    
    // Per-Type Settings (JSON)
    @Column(name = "AlertTypeSettings", columnDefinition = "NVARCHAR(MAX)")
    private String alertTypeSettings;
    // Example: {"PO_PENDING": true, "LOW_INVENTORY": true, "DAILY_SUMMARY": false}
    
    // Quiet Hours
    @Column(name = "QuietHoursEnabled")
    private Boolean quietHoursEnabled = false;
    
    @Column(name = "QuietHoursStart")
    private LocalTime quietHoursStart;
    
    @Column(name = "QuietHoursEnd")
    private LocalTime quietHoursEnd;
    
    // External User IDs
    @Column(name = "TelegramUserID", length = 100)
    private String telegramUserID;
    
    @Column(name = "SlackUserID", length = 100)
    private String slackUserID;
    
    // Metadata
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserAlertPreference() {
        this.preferenceID = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserAlertPreference(UUID userID) {
        this();
        this.userID = userID;
    }
    
    // Getters and Setters
    public UUID getPreferenceID() {
        return preferenceID;
    }
    
    public void setPreferenceID(UUID preferenceID) {
        this.preferenceID = preferenceID;
    }
    
    public UUID getUserID() {
        return userID;
    }
    
    public void setUserID(UUID userID) {
        this.userID = userID;
    }
    
    public Boolean getEnableNotifications() {
        return enableNotifications;
    }
    
    public void setEnableNotifications(Boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }
    
    public Boolean getEnableSlack() {
        return enableSlack;
    }
    
    public void setEnableSlack(Boolean enableSlack) {
        this.enableSlack = enableSlack;
    }
    
    public Boolean getEnableTelegram() {
        return enableTelegram;
    }
    
    public void setEnableTelegram(Boolean enableTelegram) {
        this.enableTelegram = enableTelegram;
    }
    
    public Boolean getEnableEmail() {
        return enableEmail;
    }
    
    public void setEnableEmail(Boolean enableEmail) {
        this.enableEmail = enableEmail;
    }
    
    public Boolean getEnableInApp() {
        return enableInApp;
    }
    
    public void setEnableInApp(Boolean enableInApp) {
        this.enableInApp = enableInApp;
    }
    
    public String getAlertTypeSettings() {
        return alertTypeSettings;
    }
    
    public void setAlertTypeSettings(String alertTypeSettings) {
        this.alertTypeSettings = alertTypeSettings;
    }
    
    public Boolean getQuietHoursEnabled() {
        return quietHoursEnabled;
    }
    
    public void setQuietHoursEnabled(Boolean quietHoursEnabled) {
        this.quietHoursEnabled = quietHoursEnabled;
    }
    
    public LocalTime getQuietHoursStart() {
        return quietHoursStart;
    }
    
    public void setQuietHoursStart(LocalTime quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }
    
    public LocalTime getQuietHoursEnd() {
        return quietHoursEnd;
    }
    
    public void setQuietHoursEnd(LocalTime quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
    }
    
    public String getTelegramUserID() {
        return telegramUserID;
    }
    
    public void setTelegramUserID(String telegramUserID) {
        this.telegramUserID = telegramUserID;
    }
    
    public String getSlackUserID() {
        return slackUserID;
    }
    
    public void setSlackUserID(String slackUserID) {
        this.slackUserID = slackUserID;
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
    
    // Utility Methods
    @PrePersist
    protected void onCreate() {
        if (preferenceID == null) {
            preferenceID = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if currently in quiet hours
     */
    public boolean isInQuietHours() {
        if (!Boolean.TRUE.equals(quietHoursEnabled) || quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        
        // Handle case where quiet hours span midnight
        if (quietHoursStart.isAfter(quietHoursEnd)) {
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        } else {
            return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
        }
    }
    
    /**
     * Check if specific alert type is enabled
     * @param alertType The alert type to check
     * @return true if enabled, false otherwise
     */
    public boolean isAlertTypeEnabled(String alertType) {
        if (!Boolean.TRUE.equals(enableNotifications)) {
            return false;
        }
        
        if (alertTypeSettings == null || alertTypeSettings.isEmpty()) {
            return true; // Default to enabled if no specific settings
        }
        
        // Simple JSON parsing (can use Gson/Jackson for production)
        return alertTypeSettings.contains("\"" + alertType + "\": true");
    }
    
    /**
     * Check if any notification channel is enabled
     */
    public boolean hasEnabledChannels() {
        return Boolean.TRUE.equals(enableSlack) || 
               Boolean.TRUE.equals(enableTelegram) || 
               Boolean.TRUE.equals(enableEmail) || 
               Boolean.TRUE.equals(enableInApp);
    }
    
    /**
     * Disable all notifications
     */
    public void disableAllNotifications() {
        this.enableNotifications = false;
        this.enableSlack = false;
        this.enableTelegram = false;
        this.enableEmail = false;
        this.enableInApp = false;
    }
    
    /**
     * Enable default notifications
     */
    public void enableDefaultNotifications() {
        this.enableNotifications = true;
        this.enableSlack = true;
        this.enableTelegram = false;
        this.enableEmail = false;
        this.enableInApp = true;
    }
    
    /**
     * Set quiet hours
     */
    public void setQuietHours(LocalTime start, LocalTime end) {
        this.quietHoursEnabled = true;
        this.quietHoursStart = start;
        this.quietHoursEnd = end;
    }
    
    /**
     * Disable quiet hours
     */
    public void disableQuietHours() {
        this.quietHoursEnabled = false;
        this.quietHoursStart = null;
        this.quietHoursEnd = null;
    }
    
    @Override
    public String toString() {
        return "UserAlertPreference{" +
                "preferenceID=" + preferenceID +
                ", userID=" + userID +
                ", enableNotifications=" + enableNotifications +
                ", enableSlack=" + enableSlack +
                ", enableTelegram=" + enableTelegram +
                ", enableInApp=" + enableInApp +
                '}';
    }
}


