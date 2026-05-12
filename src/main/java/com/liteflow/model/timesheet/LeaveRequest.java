package com.liteflow.model.timesheet;

import com.liteflow.model.auth.Employee;
import com.liteflow.model.auth.User;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "LeaveRequests")
public class LeaveRequest implements Serializable {

    @Id
    @Column(name = "LeaveRequestID", columnDefinition = "uniqueidentifier")
    private UUID leaveRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeID", nullable = false)
    private Employee employee;

    @Column(name = "LeaveType", length = 20, nullable = false)
    private String leaveType; // Nghỉ phép, Nghỉ bệnh, Nghỉ không lương, Nghỉ khác

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "TotalDays", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalDays;

    @Column(name = "Reason", length = 1000)
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
        if (leaveRequestId == null) {
            leaveRequestId = UUID.randomUUID();
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

    public boolean isPaidLeave() {
        return "Nghỉ phép".equals(leaveType) || "Nghỉ bệnh".equals(leaveType);
    }

    // ==============================
    // Getters & Setters
    // ==============================

    public UUID getLeaveRequestId() {
        return leaveRequestId;
    }

    public void setLeaveRequestId(UUID leaveRequestId) {
        this.leaveRequestId = leaveRequestId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(BigDecimal totalDays) {
        this.totalDays = totalDays;
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
        if (!(o instanceof LeaveRequest)) return false;
        LeaveRequest that = (LeaveRequest) o;
        return Objects.equals(leaveRequestId, that.leaveRequestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaveRequestId);
    }

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "leaveRequestId=" + leaveRequestId +
                ", leaveType='" + leaveType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalDays=" + totalDays +
                ", status='" + status + '\'' +
                '}';
    }
}
