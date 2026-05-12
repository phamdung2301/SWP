package com.liteflow.model.payroll;

import com.liteflow.model.auth.Employee;
import com.liteflow.model.auth.User;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "EmployeeCompensation")
public class EmployeeCompensation implements Serializable {

    @Id
    @Column(name = "CompensationID", columnDefinition = "uniqueidentifier")
    private UUID compensationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeID", nullable = false)
    private Employee employee;

    @Column(name = "CompensationType", length = 20, nullable = false)
    private String compensationType; // Fixed | PerShift | Hybrid

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PolicyID")
    private PayPolicy policy;

    @Column(name = "BaseMonthlySalary", precision = 12, scale = 2)
    private BigDecimal baseMonthlySalary;

    @Column(name = "HourlyRate", precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "PerShiftRate", precision = 12, scale = 2)
    private BigDecimal perShiftRate;

    @Column(name = "Currency", length = 3, nullable = false)
    private String currency = "VND";

    @Column(name = "EffectiveFrom", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "EffectiveTo")
    private LocalDate effectiveTo;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "Notes", length = 500)
    private String notes;

    @Column(name = "OvertimeRate", precision = 12, scale = 2)
    private BigDecimal overtimeRate;

    @Column(name = "BonusAmount", precision = 12, scale = 2)
    private BigDecimal bonusAmount;

    @Column(name = "CommissionRate", precision = 12, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "AllowanceAmount", precision = 12, scale = 2)
    private BigDecimal allowanceAmount;

    @Column(name = "DeductionAmount", precision = 12, scale = 2)
    private BigDecimal deductionAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy")
    private User createdBy;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (compensationId == null) {
            compensationId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getCompensationId() { return compensationId; }
    public void setCompensationId(UUID compensationId) { this.compensationId = compensationId; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getCompensationType() { return compensationType; }
    public void setCompensationType(String compensationType) { this.compensationType = compensationType; }
    public PayPolicy getPolicy() { return policy; }
    public void setPolicy(PayPolicy policy) { this.policy = policy; }
    public BigDecimal getBaseMonthlySalary() { return baseMonthlySalary; }
    public void setBaseMonthlySalary(BigDecimal baseMonthlySalary) { this.baseMonthlySalary = baseMonthlySalary; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public BigDecimal getPerShiftRate() { return perShiftRate; }
    public void setPerShiftRate(BigDecimal perShiftRate) { this.perShiftRate = perShiftRate; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public BigDecimal getOvertimeRate() { return overtimeRate; }
    public void setOvertimeRate(BigDecimal overtimeRate) { this.overtimeRate = overtimeRate; }
    public BigDecimal getBonusAmount() { return bonusAmount; }
    public void setBonusAmount(BigDecimal bonusAmount) { this.bonusAmount = bonusAmount; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public BigDecimal getAllowanceAmount() { return allowanceAmount; }
    public void setAllowanceAmount(BigDecimal allowanceAmount) { this.allowanceAmount = allowanceAmount; }
    public BigDecimal getDeductionAmount() { return deductionAmount; }
    public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; }
}


