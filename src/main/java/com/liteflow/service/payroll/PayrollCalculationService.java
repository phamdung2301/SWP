package com.liteflow.service.payroll;

import com.liteflow.dao.payroll.EmployeeCompensationDAO;
import com.liteflow.dao.timesheet.EmployeeAttendanceDAO;
import com.liteflow.model.payroll.EmployeeCompensation;
import com.liteflow.model.timesheet.EmployeeAttendance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Service for calculating employee payroll based on compensation type and attendance
 */
public class PayrollCalculationService {

    private final EmployeeCompensationDAO compensationDAO;
    private final EmployeeAttendanceDAO attendanceDAO;

    public PayrollCalculationService() {
        this.compensationDAO = new EmployeeCompensationDAO();
        this.attendanceDAO = new EmployeeAttendanceDAO();
    }

    /**
     * Calculate monthly salary for an employee based on their compensation type
     */
    public MonthlySalaryResult calculateMonthlySalary(UUID employeeId, int month, int year) {
        EmployeeCompensation compensation = compensationDAO.getActiveCompensation(employeeId);
        if (compensation == null) {
            return new MonthlySalaryResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        String compensationType = compensation.getCompensationType();
        BigDecimal totalSalary = BigDecimal.ZERO;
        BigDecimal allowances = compensation.getAllowanceAmount() != null ? compensation.getAllowanceAmount() : BigDecimal.ZERO;
        BigDecimal bonuses = compensation.getBonusAmount() != null ? compensation.getBonusAmount() : BigDecimal.ZERO;
        BigDecimal deductions = compensation.getDeductionAmount() != null ? compensation.getDeductionAmount() : BigDecimal.ZERO;

        if ("Fixed".equals(compensationType)) {
            totalSalary = calculateFixedSalary(compensation);
        } else if ("Hybrid".equals(compensationType)) {
            totalSalary = calculateHybridSalary(compensation, employeeId, month, year);
        } else if ("PerShift".equals(compensationType)) {
            totalSalary = calculatePerShiftSalary(compensation, employeeId, month, year);
        }

        // Add allowances and bonuses, subtract deductions
        totalSalary = totalSalary.add(allowances).add(bonuses).subtract(deductions);

        return new MonthlySalaryResult(totalSalary, allowances, bonuses, deductions);
    }

    /**
     * Calculate fixed salary (base monthly salary)
     */
    public BigDecimal calculateFixedSalary(EmployeeCompensation compensation) {
        if (compensation.getBaseMonthlySalary() == null) {
            return BigDecimal.ZERO;
        }
        return compensation.getBaseMonthlySalary();
    }

    /**
     * Calculate hybrid salary (base + hourly rate * hours worked)
     */
    public BigDecimal calculateHybridSalary(EmployeeCompensation compensation, UUID employeeId, int month, int year) {
        BigDecimal baseSalary = compensation.getBaseMonthlySalary() != null 
            ? compensation.getBaseMonthlySalary() 
            : BigDecimal.ZERO;
        
        BigDecimal hourlyRate = compensation.getHourlyRate() != null 
            ? compensation.getHourlyRate() 
            : BigDecimal.ZERO;
        
        BigDecimal totalHours = getTotalHoursWorked(employeeId, month, year);
        BigDecimal hourlyPay = totalHours.multiply(hourlyRate);
        
        return baseSalary.add(hourlyPay);
    }

    /**
     * Calculate per-shift salary (per shift rate * number of shifts)
     */
    public BigDecimal calculatePerShiftSalary(EmployeeCompensation compensation, UUID employeeId, int month, int year) {
        BigDecimal perShiftRate = compensation.getPerShiftRate() != null 
            ? compensation.getPerShiftRate() 
            : BigDecimal.ZERO;
        
        int shiftsWorked = getShiftsWorked(employeeId, month, year);
        
        return perShiftRate.multiply(BigDecimal.valueOf(shiftsWorked));
    }

    /**
     * Get total hours worked in a month from EmployeeAttendance
     * Tính từ CheckInTime và CheckOutTime trong EmployeeAttendance
     */
    public BigDecimal getTotalHoursWorked(UUID employeeId, int month, int year) {
        List<EmployeeAttendance> attendanceList = attendanceDAO.findByEmployeeAndMonth(employeeId, year, month);
        
        BigDecimal totalHours = BigDecimal.ZERO;
        for (EmployeeAttendance attendance : attendanceList) {
            // Chỉ tính cho các ngày có status "Work" và có cả CheckInTime và CheckOutTime
            if ("Work".equals(attendance.getStatus()) && 
                attendance.getCheckInTime() != null && 
                attendance.getCheckOutTime() != null) {
                
                // Tính số giờ làm việc từ CheckInTime đến CheckOutTime
                java.time.Duration duration = java.time.Duration.between(
                    attendance.getCheckInTime(), 
                    attendance.getCheckOutTime()
                );
                
                long totalMinutes = duration.toMinutes();
                // Chuyển đổi từ phút sang giờ (làm tròn 2 chữ số)
                BigDecimal hours = BigDecimal.valueOf(totalMinutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                
                totalHours = totalHours.add(hours);
            }
        }
        
        return totalHours.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get number of shifts worked in a month from EmployeeAttendance
     */
    public int getShiftsWorked(UUID employeeId, int month, int year) {
        List<EmployeeAttendance> attendanceList = attendanceDAO.findByEmployeeAndMonth(employeeId, year, month);
        
        int shiftsCount = 0;
        for (EmployeeAttendance attendance : attendanceList) {
            // Count only "Work" status as a shift
            if ("Work".equals(attendance.getStatus()) && 
                attendance.getCheckInTime() != null && 
                attendance.getCheckOutTime() != null) {
                shiftsCount++;
            }
        }
        
        return shiftsCount;
    }

    /**
     * Result class for monthly salary calculation
     */
    public static class MonthlySalaryResult {
        private final BigDecimal totalSalary;
        private final BigDecimal allowances;
        private final BigDecimal bonuses;
        private final BigDecimal deductions;

        public MonthlySalaryResult(BigDecimal totalSalary, BigDecimal allowances, BigDecimal bonuses, BigDecimal deductions) {
            this.totalSalary = totalSalary;
            this.allowances = allowances;
            this.bonuses = bonuses;
            this.deductions = deductions;
        }

        public BigDecimal getTotalSalary() {
            return totalSalary;
        }

        public BigDecimal getAllowances() {
            return allowances;
        }

        public BigDecimal getBonuses() {
            return bonuses;
        }

        public BigDecimal getDeductions() {
            return deductions;
        }
    }
}

