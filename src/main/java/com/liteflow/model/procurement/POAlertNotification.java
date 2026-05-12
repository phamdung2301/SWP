package com.liteflow.model.procurement;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * POAlertNotification: Tracking notifications đã gửi cho PO mới để tránh spam
 */
@Entity
@jakarta.persistence.Table(name = "POAlertNotifications")
public class POAlertNotification implements Serializable {

    @Id
    @Column(name = "NotificationID", columnDefinition = "uniqueidentifier")
    private UUID notificationId;

    @Column(name = "POID", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID poid;

    @Column(name = "UserID", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID userId;

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

    public UUID getPoid() {
        return poid;
    }

    public void setPoid(UUID poid) {
        this.poid = poid;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

