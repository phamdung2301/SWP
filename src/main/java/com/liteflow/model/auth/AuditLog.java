package com.liteflow.model.auth;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * AuditLog: Lưu lại các hành động (login, logout, OTP, role...) phục vụ
 * tracking.
 */
@Entity
@Table(name = "AuditLogs")
public class AuditLog implements Serializable {

    @Id
    @Column(name = "AuditID", columnDefinition = "uniqueidentifier")
    private UUID auditID;

    @Column(name = "UserID", columnDefinition = "uniqueidentifier")
    private UUID userID; // Người thực hiện hành động

    @Column(name = "Action", length = 200, nullable = false)
    private String action; // Loại hành động (LOGIN_SUCCESS, OTP_USED...)

    @Column(name = "ObjectType", length = 100)
    private String objectType; // Loại đối tượng (USER, OTP...)

    @Column(name = "ObjectID", length = 36)
    private String objectID; // ID đối tượng liên quan

    @Column(name = "Details")
    private String details; // Mô tả chi tiết

    @Column(name = "IPAddress", length = 50)
    private String ipAddress; // IP thực hiện

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedAt", nullable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        if (auditID == null) {
            auditID = UUID.randomUUID();
        }
        createdAt = new Date();
    }

    // Getters & Setters
    public UUID getAuditID() {
        return auditID;
    }

    public void setAuditID(UUID auditID) {
        this.auditID = auditID;
    }

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
