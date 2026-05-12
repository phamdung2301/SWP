package com.liteflow.model.alert;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notification Channel Entity
 * Cấu hình kết nối với Slack, Telegram, Email
 */
@Entity
@Table(name = "NotificationChannels")
public class NotificationChannel {
    
    @Id
    @Column(name = "ChannelID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID channelID;
    
    @Column(name = "ChannelType", nullable = false, length = 20)
    private String channelType; // SLACK, TELEGRAM, EMAIL, SMS
    
    @Column(name = "Name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "Description", length = 500)
    private String description;
    
    // Slack Configuration
    @Column(name = "SlackWebhookURL", length = 500)
    private String slackWebhookURL;
    
    @Column(name = "SlackChannel", length = 100)
    private String slackChannel;
    
    // Telegram Configuration
    @Column(name = "TelegramBotToken", length = 200)
    private String telegramBotToken;
    
    @Column(name = "TelegramChatID", length = 100)
    private String telegramChatID;
    
    // Email Configuration
    @Column(name = "EmailRecipients", columnDefinition = "NVARCHAR(MAX)")
    private String emailRecipients; // JSON array
    
    @Column(name = "EmailFrom", length = 200)
    private String emailFrom;
    
    // Status
    @Column(name = "IsActive")
    private Boolean isActive = true;
    
    @Column(name = "LastUsed")
    private LocalDateTime lastUsed;
    
    @Column(name = "LastError", columnDefinition = "NVARCHAR(MAX)")
    private String lastError;
    
    // Rate Limiting
    @Column(name = "MaxRequestsPerHour")
    private Integer maxRequestsPerHour = 100;
    
    @Column(name = "CurrentHourRequests")
    private Integer currentHourRequests = 0;
    
    @Column(name = "HourResetAt")
    private LocalDateTime hourResetAt;
    
    // Metadata
    @Column(name = "CreatedBy", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID createdBy;
    
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    
    // Constructors
    public NotificationChannel() {
        this.channelID = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.hourResetAt = LocalDateTime.now().plusHours(1);
    }
    
    public NotificationChannel(String channelType, String name) {
        this();
        this.channelType = channelType;
        this.name = name;
    }
    
    // Getters and Setters
    public UUID getChannelID() {
        return channelID;
    }
    
    public void setChannelID(UUID channelID) {
        this.channelID = channelID;
    }
    
    public String getChannelType() {
        return channelType;
    }
    
    public void setChannelType(String channelType) {
        this.channelType = channelType;
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
    
    public String getSlackWebhookURL() {
        return slackWebhookURL;
    }
    
    public void setSlackWebhookURL(String slackWebhookURL) {
        this.slackWebhookURL = slackWebhookURL;
    }
    
    public String getSlackChannel() {
        return slackChannel;
    }
    
    public void setSlackChannel(String slackChannel) {
        this.slackChannel = slackChannel;
    }
    
    public String getTelegramBotToken() {
        return telegramBotToken;
    }
    
    public void setTelegramBotToken(String telegramBotToken) {
        this.telegramBotToken = telegramBotToken;
    }
    
    public String getTelegramChatID() {
        return telegramChatID;
    }
    
    public void setTelegramChatID(String telegramChatID) {
        this.telegramChatID = telegramChatID;
    }
    
    public String getEmailRecipients() {
        return emailRecipients;
    }
    
    public void setEmailRecipients(String emailRecipients) {
        this.emailRecipients = emailRecipients;
    }
    
    public String getEmailFrom() {
        return emailFrom;
    }
    
    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getLastUsed() {
        return lastUsed;
    }
    
    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    public String getLastError() {
        return lastError;
    }
    
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
    
    public Integer getMaxRequestsPerHour() {
        return maxRequestsPerHour;
    }
    
    public void setMaxRequestsPerHour(Integer maxRequestsPerHour) {
        this.maxRequestsPerHour = maxRequestsPerHour;
    }
    
    public Integer getCurrentHourRequests() {
        return currentHourRequests;
    }
    
    public void setCurrentHourRequests(Integer currentHourRequests) {
        this.currentHourRequests = currentHourRequests;
    }
    
    public LocalDateTime getHourResetAt() {
        return hourResetAt;
    }
    
    public void setHourResetAt(LocalDateTime hourResetAt) {
        this.hourResetAt = hourResetAt;
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
    
    // Utility Methods
    @PrePersist
    protected void onCreate() {
        if (channelID == null) {
            channelID = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (hourResetAt == null) {
            hourResetAt = LocalDateTime.now().plusHours(1);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if rate limit is exceeded
     */
    public boolean isRateLimitExceeded() {
        resetRateLimitIfNeeded();
        return currentHourRequests >= maxRequestsPerHour;
    }
    
    /**
     * Increment request counter
     */
    public void incrementRequestCount() {
        resetRateLimitIfNeeded();
        currentHourRequests++;
    }
    
    /**
     * Reset rate limit counter if hour has passed
     */
    private void resetRateLimitIfNeeded() {
        if (LocalDateTime.now().isAfter(hourResetAt)) {
            currentHourRequests = 0;
            hourResetAt = LocalDateTime.now().plusHours(1);
        }
    }
    
    /**
     * Check if channel is properly configured
     */
    public boolean isConfigured() {
        switch (channelType.toUpperCase()) {
            case "SLACK":
                return slackWebhookURL != null && !slackWebhookURL.isEmpty();
            case "TELEGRAM":
                return telegramBotToken != null && !telegramBotToken.isEmpty() &&
                       telegramChatID != null && !telegramChatID.isEmpty();
            case "EMAIL":
                return emailRecipients != null && !emailRecipients.isEmpty();
            default:
                return false;
        }
    }
    
    /**
     * Record successful usage
     */
    public void recordSuccess() {
        this.lastUsed = LocalDateTime.now();
        this.lastError = null;
        incrementRequestCount();
    }
    
    /**
     * Record error
     */
    public void recordError(String errorMessage) {
        this.lastError = errorMessage;
        this.lastUsed = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "NotificationChannel{" +
                "channelID=" + channelID +
                ", channelType='" + channelType + '\'' +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}


