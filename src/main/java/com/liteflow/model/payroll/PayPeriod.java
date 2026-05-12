package com.liteflow.model.payroll;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "PayPeriods")
public class PayPeriod implements Serializable {

    @Id
    @Column(name = "PayPeriodID", columnDefinition = "uniqueidentifier")
    private UUID payPeriodId;

    @Column(name = "Name", length = 100, nullable = false)
    private String name;

    @Column(name = "PeriodType", length = 20, nullable = false)
    private String periodType;

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "Status", length = 20, nullable = false)
    private String status;

    @Column(name = "LockedAt")
    private LocalDateTime lockedAt;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "payPeriod", fetch = FetchType.LAZY)
    private List<PayrollRun> payrollRuns = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (payPeriodId == null) {
            payPeriodId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "Open";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getPayPeriodId() {
        return payPeriodId;
    }

    public void setPayPeriodId(UUID payPeriodId) {
        this.payPeriodId = payPeriodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<PayrollRun> getPayrollRuns() {
        return payrollRuns;
    }

    public void setPayrollRuns(List<PayrollRun> payrollRuns) {
        this.payrollRuns = payrollRuns;
    }
}


