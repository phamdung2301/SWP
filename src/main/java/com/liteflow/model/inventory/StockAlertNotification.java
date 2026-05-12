package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockAlertNotification: Tracking notifications đã gửi để tránh spam
 */
@Entity
@jakarta.persistence.Table(name = "StockAlertNotifications")
public class StockAlertNotification implements Serializable {

    @Id
    @Column(name = "NotificationID", columnDefinition = "uniqueidentifier")
    private UUID notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductVariantID", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "UserID", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID userId;

    @Column(name = "AlertThreshold", nullable = false)
    private Integer alertThreshold;  // 10 hoặc 20

    @Column(name = "StockLevel", nullable = false)
    private Integer stockLevel;  // Stock level tại thời điểm gửi

    @Column(name = "SentAt")
    private LocalDateTime sentAt;

    @Column(name = "MessageSent", columnDefinition = "NVARCHAR(MAX)")
    private String messageSent;

    @Column(name = "IsAcknowledged")
    private Boolean isAcknowledged = false;

    @PrePersist
    protected void onCreate() {
        if (notificationId == null) {
            notificationId = UUID.randomUUID();
        }
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
        if (isAcknowledged == null) {
            isAcknowledged = false;
        }
    }

    // Getters & Setters
    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(Integer alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public Integer getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getMessageSent() {
        return messageSent;
    }

    public void setMessageSent(String messageSent) {
        this.messageSent = messageSent;
    }

    public Boolean getIsAcknowledged() {
        return isAcknowledged;
    }

    public void setIsAcknowledged(Boolean isAcknowledged) {
        this.isAcknowledged = isAcknowledged;
    }
}

