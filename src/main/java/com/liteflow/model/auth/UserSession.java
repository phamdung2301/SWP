package com.liteflow.model.auth;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserSession: Lưu session (JWT, device, IP, thời gian hết hạn).
 */
@Entity
@Table(name = "UserSessions") // ✅ sửa tên đúng với DB
public class UserSession implements Serializable {

    @Id
    @Column(name = "SessionID", columnDefinition = "uniqueidentifier")
    private UUID sessionId;

    @Column(name = "UserID", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID userId;

    @Column(name = "JWT", length = 2000)
    private String jwt;

    @Column(name = "DeviceInfo", length = 500)
    private String deviceInfo;

    @Column(name = "IPAddress", length = 100)
    private String ipAddress;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ExpiresAt")
    private LocalDateTime expiresAt;

    @Column(name = "Revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "Last2faVerifiedAt")
    private LocalDateTime last2faVerifiedAt;

    @Column(name = "Action", length = 200)
    private String action;

    @Column(name = "ObjectType", length = 100)
    private String objectType;

    @Column(name = "ObjectID", columnDefinition = "uniqueidentifier")
    private UUID objectID;

    @Column(name = "Details", columnDefinition = "NVARCHAR(MAX)")
    private String details;

    @Column(name = "IPAddressAction", length = 50)
    private String ipAddressAction;

    @Column(name = "CreatedAtAction")
    private LocalDateTime createdAtAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", insertable = false, updatable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        if (sessionId == null) {
            sessionId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters & Setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public LocalDateTime getLast2faVerifiedAt() {
        return last2faVerifiedAt;
    }

    public void setLast2faVerifiedAt(LocalDateTime last2faVerifiedAt) {
        this.last2faVerifiedAt = last2faVerifiedAt;
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

    public UUID getObjectID() {
        return objectID;
    }

    public void setObjectID(UUID objectID) {
        this.objectID = objectID;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddressAction() {
        return ipAddressAction;
    }

    public void setIpAddressAction(String ipAddressAction) {
        this.ipAddressAction = ipAddressAction;
    }

    public LocalDateTime getCreatedAtAction() {
        return createdAtAction;
    }

    public void setCreatedAtAction(LocalDateTime createdAtAction) {
        this.createdAtAction = createdAtAction;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
