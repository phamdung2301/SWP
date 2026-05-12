package com.liteflow.model.ai;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI Agent Configuration Entity
 * Lưu trữ các cấu hình của AI Agent trong hệ thống
 */
@Entity
@Table(name = "AIAgentConfigurations")
public class AIAgentConfiguration {
    
    @Id
    @Column(name = "ConfigID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID configID;
    
    @Column(name = "ConfigKey", nullable = false, unique = true, length = 100)
    private String configKey;
    
    @Column(name = "ConfigValue", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String configValue;
    
    @Column(name = "ConfigType", nullable = false, length = 50)
    private String configType; // INTEGER, STRING, JSON, BOOLEAN, TIME, CRON, DECIMAL
    
    @Column(name = "Category", nullable = false, length = 50)
    private String category; // STOCK_ALERT, DEMAND_FORECAST, PO_AUTO, SUPPLIER_MAPPING, GPT_SERVICE, NOTIFICATION
    
    @Column(name = "DisplayName", nullable = false, length = 200)
    private String displayName;
    
    @Column(name = "Description", length = 500)
    private String description;
    
    @Column(name = "MinValue", length = 50)
    private String minValue;
    
    @Column(name = "MaxValue", length = 50)
    private String maxValue;
    
    @Column(name = "DefaultValue", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String defaultValue;
    
    @Column(name = "IsActive")
    private Boolean isActive = true;
    
    @Column(name = "UpdatedBy", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID updatedBy;
    
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    
    // Constructors
    public AIAgentConfiguration() {
        this.configID = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public AIAgentConfiguration(String configKey, String configValue, String configType, String category, String displayName) {
        this();
        this.configKey = configKey;
        this.configValue = configValue;
        this.configType = configType;
        this.category = category;
        this.displayName = displayName;
        this.defaultValue = configValue;
    }
    
    // Getters and Setters
    public UUID getConfigID() {
        return configID;
    }
    
    public void setConfigID(UUID configID) {
        this.configID = configID;
    }
    
    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    
    public String getConfigValue() {
        return configValue;
    }
    
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
    
    public String getConfigType() {
        return configType;
    }
    
    public void setConfigType(String configType) {
        this.configType = configType;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getMinValue() {
        return minValue;
    }
    
    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }
    
    public String getMaxValue() {
        return maxValue;
    }
    
    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public UUID getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility Methods
    @PrePersist
    protected void onCreate() {
        if (configID == null) {
            configID = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if config value is at default
     */
    public boolean isDefaultValue() {
        return defaultValue != null && defaultValue.equals(configValue);
    }
    
    /**
     * Reset to default value
     */
    public void resetToDefault() {
        this.configValue = this.defaultValue;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get integer value (for INTEGER type)
     */
    public int getIntValue() {
        try {
            return Integer.parseInt(configValue);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Get boolean value (for BOOLEAN type)
     */
    public boolean getBooleanValue() {
        return "true".equalsIgnoreCase(configValue);
    }
    
    /**
     * Get decimal value (for DECIMAL type)
     */
    public double getDecimalValue() {
        try {
            return Double.parseDouble(configValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    @Override
    public String toString() {
        return "AIAgentConfiguration{" +
                "configID=" + configID +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", category='" + category + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}

