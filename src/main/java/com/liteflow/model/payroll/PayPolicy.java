package com.liteflow.model.payroll;

import com.liteflow.model.auth.User;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PayPolicies")
public class PayPolicy implements Serializable {

    @Id
    @Column(name = "PolicyID", columnDefinition = "uniqueidentifier")
    private UUID policyId;

    @Column(name = "Name", length = 100, nullable = false)
    private String name;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "OvertimeMultiplier", precision = 5, scale = 2)
    private BigDecimal overtimeMultiplier;

    @Column(name = "NightShiftMultiplier", precision = 5, scale = 2)
    private BigDecimal nightShiftMultiplier;

    @Column(name = "WeekendMultiplier", precision = 5, scale = 2)
    private BigDecimal weekendMultiplier;

    @Column(name = "HolidayMultiplier", precision = 5, scale = 2)
    private BigDecimal holidayMultiplier;

    @Column(name = "MaxDailyHours", precision = 5, scale = 2)
    private BigDecimal maxDailyHours;

    @Column(name = "MinBreakMinutes")
    private Integer minBreakMinutes;

    @Column(name = "SocialInsuranceRate", precision = 5, scale = 4)
    private BigDecimal socialInsuranceRate;

    @Column(name = "HealthInsuranceRate", precision = 5, scale = 4)
    private BigDecimal healthInsuranceRate;

    @Column(name = "UnemploymentInsuranceRate", precision = 5, scale = 4)
    private BigDecimal unemploymentInsuranceRate;

    @Column(name = "PITFlatRate", precision = 5, scale = 4)
    private BigDecimal pitFlatRate;

    @Column(name = "Currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "IsActive")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy")
    private User createdBy;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (policyId == null) {
            policyId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currency == null) {
            currency = "VND";
        }
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getPolicyId() { return policyId; }
    public void setPolicyId(UUID policyId) { this.policyId = policyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getOvertimeMultiplier() { return overtimeMultiplier; }
    public void setOvertimeMultiplier(BigDecimal v) { this.overtimeMultiplier = v; }
    public BigDecimal getNightShiftMultiplier() { return nightShiftMultiplier; }
    public void setNightShiftMultiplier(BigDecimal v) { this.nightShiftMultiplier = v; }
    public BigDecimal getWeekendMultiplier() { return weekendMultiplier; }
    public void setWeekendMultiplier(BigDecimal v) { this.weekendMultiplier = v; }
    public BigDecimal getHolidayMultiplier() { return holidayMultiplier; }
    public void setHolidayMultiplier(BigDecimal v) { this.holidayMultiplier = v; }
    public BigDecimal getMaxDailyHours() { return maxDailyHours; }
    public void setMaxDailyHours(BigDecimal v) { this.maxDailyHours = v; }
    public Integer getMinBreakMinutes() { return minBreakMinutes; }
    public void setMinBreakMinutes(Integer v) { this.minBreakMinutes = v; }
    public BigDecimal getSocialInsuranceRate() { return socialInsuranceRate; }
    public void setSocialInsuranceRate(BigDecimal v) { this.socialInsuranceRate = v; }
    public BigDecimal getHealthInsuranceRate() { return healthInsuranceRate; }
    public void setHealthInsuranceRate(BigDecimal v) { this.healthInsuranceRate = v; }
    public BigDecimal getUnemploymentInsuranceRate() { return unemploymentInsuranceRate; }
    public void setUnemploymentInsuranceRate(BigDecimal v) { this.unemploymentInsuranceRate = v; }
    public BigDecimal getPitFlatRate() { return pitFlatRate; }
    public void setPitFlatRate(BigDecimal v) { this.pitFlatRate = v; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}


