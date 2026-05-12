package com.liteflow.model.payroll;

import com.liteflow.model.auth.Employee;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PayrollEntries")
public class PayrollEntry implements Serializable {

    @Id
    @Column(name = "PayrollEntryID", columnDefinition = "uniqueidentifier")
    private UUID payrollEntryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PayrollRunID", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeID", nullable = false)
    private Employee employee;

    @Column(name = "CompensationType", length = 20, nullable = false)
    private String compensationType;

    @Column(name = "BaseSalary", precision = 12, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "HourlyRate", precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "PerShiftRate", precision = 12, scale = 2)
    private BigDecimal perShiftRate;

    @Column(name = "HoursWorked", precision = 9, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "ShiftsWorked")
    private Integer shiftsWorked;

    @Column(name = "OvertimeHours", precision = 9, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "HolidayHours", precision = 9, scale = 2)
    private BigDecimal holidayHours;

    @Column(name = "Allowances", precision = 12, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    @Column(name = "Bonuses", precision = 12, scale = 2)
    private BigDecimal bonuses = BigDecimal.ZERO;

    @Column(name = "Deductions", precision = 12, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "GrossPay", precision = 12, scale = 2, nullable = false)
    private BigDecimal grossPay;

    @Column(name = "NetPay", precision = 12, scale = 2, nullable = false)
    private BigDecimal netPay;

    @Column(name = "Currency", length = 3, nullable = false)
    private String currency = "VND";

    @Column(name = "ExchangeRate", precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "PaidInCurrency", length = 3, nullable = false)
    private String paidInCurrency = "VND";

    @Column(name = "IsPaid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (payrollEntryId == null) {
            payrollEntryId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }

    public UUID getPayrollEntryId() {
        return payrollEntryId;
    }

    public void setPayrollEntryId(UUID payrollEntryId) {
        this.payrollEntryId = payrollEntryId;
    }

    public PayrollRun getPayrollRun() {
        return payrollRun;
    }

    public void setPayrollRun(PayrollRun payrollRun) {
        this.payrollRun = payrollRun;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getCompensationType() {
        return compensationType;
    }

    public void setCompensationType(String compensationType) {
        this.compensationType = compensationType;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getPerShiftRate() {
        return perShiftRate;
    }

    public void setPerShiftRate(BigDecimal perShiftRate) {
        this.perShiftRate = perShiftRate;
    }

    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public Integer getShiftsWorked() {
        return shiftsWorked;
    }

    public void setShiftsWorked(Integer shiftsWorked) {
        this.shiftsWorked = shiftsWorked;
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public BigDecimal getHolidayHours() {
        return holidayHours;
    }

    public void setHolidayHours(BigDecimal holidayHours) {
        this.holidayHours = holidayHours;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public BigDecimal getBonuses() {
        return bonuses;
    }

    public void setBonuses(BigDecimal bonuses) {
        this.bonuses = bonuses;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public BigDecimal getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(BigDecimal grossPay) {
        this.grossPay = grossPay;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getPaidInCurrency() {
        return paidInCurrency;
    }

    public void setPaidInCurrency(String paidInCurrency) {
        this.paidInCurrency = paidInCurrency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }
}


