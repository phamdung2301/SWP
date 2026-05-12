package com.liteflow.model.timesheet;

import com.liteflow.model.auth.Employee;
import com.liteflow.model.auth.User;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ForgotClockRequests")
public class ForgotClockRequest implements Serializable {

    @Id
    @Column(name = "ForgotClockRequestID", columnDefinition = "uniqueidentifier")
    private UUID forgotClockRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeID", nullable = false)
    private Employee employee;

    @Column(name = "ForgotDate", nullable = false)
    private LocalDate forgotDate;

    @Column(name = "ForgotType", length = 20, nullable = false)
    private String forgotType; // CHECK_IN, CHECK_OUT, BOTH

    @Column(name = "ForgotTime")
    private LocalTime forgotTime;

    @Column(name = "Reason", length = 1000, nullable = false)
    private String reason;

    @Column(name = "Status", length = 20, nullable = false)
    private String status = "Chờ duyệt"; // Chờ duyệt, Đã duyệt, Từ chối, Đã hủy

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReviewedBy")
    private User reviewedBy;

    @Column(name = "ReviewedAt")
    private LocalDateTime reviewedAt;

    @Column(name = "ReviewNotes", length = 500)
    private String reviewNotes;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (forgotClockRequestId == null) {
            forgotClockRequestId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = "Chờ duyệt";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==============================
    // Helper methods
    // ==============================

    public boolean isPending() {
        return "Chờ duyệt".equals(status);
    }

    public boolean isApproved() {
        return "Đã duyệt".equals(status);
    }

    public boolean isRejected() {
        return "Từ chối".equals(status);
    }

    public boolean isCancelled() {
        return "Đã hủy".equals(status);
    }

    public boolean isCheckInForgot() {
        return "CHECK_IN".equals(forgotType);
    }

    public boolean isCheckOutForgot() {
        return "CHECK_OUT".equals(forgotType);
    }

    public boolean isBothForgot() {
        return "BOTH".equals(forgotType);
    }

    // ==============================
    // Getters & Setters
    // ==============================

    public UUID getForgotClockRequestId() {
        return forgotClockRequestId;
    }

    public void setForgotClockRequestId(UUID forgotClockRequestId) {
        this.forgotClockRequestId = forgotClockRequestId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getForgotDate() {
        return forgotDate;
    }

    public void setForgotDate(LocalDate forgotDate) {
        this.forgotDate = forgotDate;
    }

    public String getForgotType() {
        return forgotType;
    }

    public void setForgotType(String forgotType) {
        this.forgotType = forgotType;
    }

    public LocalTime getForgotTime() {
        return forgotTime;
    }

    public void setForgotTime(LocalTime forgotTime) {
        this.forgotTime = forgotTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ==============================
    // equals, hashCode, toString
    // ==============================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForgotClockRequest)) return false;
        ForgotClockRequest that = (ForgotClockRequest) o;
        return Objects.equals(forgotClockRequestId, that.forgotClockRequestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(forgotClockRequestId);
    }

    @Override
    public String toString() {
        return "ForgotClockRequest{" +
                "forgotClockRequestId=" + forgotClockRequestId +
                ", forgotDate=" + forgotDate +
                ", forgotType='" + forgotType + '\'' +
                ", forgotTime=" + forgotTime +
                ", status='" + status + '\'' +
                '}';
    }
}

