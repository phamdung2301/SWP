package com.liteflow.model.auth;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OtpToken: Lưu OTP phát hành cho user, có hạn 5 phút, dùng 1 lần.
 */
@Entity
@Table(name = "OtpTokens")
public class OtpToken implements Serializable {

    @Id
    @Column(name = "OtpID", columnDefinition = "uniqueidentifier")
    private UUID otpId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "UserID", nullable = true)
    private User user;

    @Column(name = "TargetEmail", length = 320)
    private String targetEmail; // cho signup OTP khi user chưa tồn tại

    @Column(name = "Code", length = 6, nullable = false)
    private String code;

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt; // Hết hạn sau 5 phút

    @Column(name = "Used", nullable = false)
    private boolean used;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "IPAddress", length = 50)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (otpId == null) {
            otpId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters & Setters
    public UUID getOtpId() {
        return otpId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTargetEmail() {
        return targetEmail;
    }

    public void setTargetEmail(String targetEmail) {
        this.targetEmail = targetEmail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
