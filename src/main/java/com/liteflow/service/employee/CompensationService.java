package com.liteflow.service.employee;

import com.liteflow.dao.employee.EmployeeDAO;
import com.liteflow.dao.payroll.EmployeeCompensationDAO;
import com.liteflow.model.auth.Employee;
import com.liteflow.model.payroll.EmployeeCompensation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service layer cho salary management
 */
public class CompensationService {

    private final EmployeeCompensationDAO compensationDAO;
    private final EmployeeDAO employeeDAO;

    public CompensationService() {
        this.compensationDAO = new EmployeeCompensationDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Tạo hoặc cập nhật compensation cho nhân viên
     */
    public boolean saveCompensation(String employeeCode, String compensationType,
                                    BigDecimal baseMonthlySalary, BigDecimal hourlyRate,
                                    BigDecimal perShiftRate, BigDecimal overtimeRate,
                                    BigDecimal bonusAmount, BigDecimal commissionRate,
                                    BigDecimal allowanceAmount, BigDecimal deductionAmount) {
        try {
            // Tìm employee
            Employee employee = employeeDAO.findByEmployeeCode(employeeCode);
            if (employee == null) {
                System.err.println("Employee not found: " + employeeCode);
                return false;
            }

            // Vô hiệu hóa các compensation cũ
            compensationDAO.deactivateOldCompensations(employee.getEmployeeID());

            // Tạo compensation mới
            EmployeeCompensation compensation = new EmployeeCompensation();
            compensation.setEmployee(employee);
            compensation.setCompensationType(compensationType);
            compensation.setBaseMonthlySalary(baseMonthlySalary);
            compensation.setHourlyRate(hourlyRate);
            compensation.setPerShiftRate(perShiftRate);
            compensation.setOvertimeRate(overtimeRate);
            compensation.setBonusAmount(bonusAmount);
            compensation.setCommissionRate(commissionRate);
            compensation.setAllowanceAmount(allowanceAmount);
            compensation.setDeductionAmount(deductionAmount);
            compensation.setEffectiveFrom(LocalDate.now());
            compensation.setIsActive(true);

            return compensationDAO.insert(compensation);
        } catch (Exception e) {
            System.err.println("Error saving compensation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy compensation đang active của nhân viên
     */
    public EmployeeCompensation getActiveCompensation(String employeeCode) {
        return compensationDAO.getActiveCompensationByCode(employeeCode);
    }

    /**
     * Lấy compensation đang active của nhân viên theo employeeId
     */
    public EmployeeCompensation getActiveCompensationByEmployeeId(UUID employeeId) {
        return compensationDAO.getActiveCompensation(employeeId);
    }

    /**
     * Lấy tất cả active compensations (để hiển thị bảng)
     */
    public List<EmployeeCompensation> getAllActiveCompensations() {
        return compensationDAO.getAllActiveCompensations();
    }

    /**
     * Lấy compensation history của nhân viên
     */
    public List<EmployeeCompensation> getCompensationHistory(String employeeCode) {
        Employee employee = employeeDAO.findByEmployeeCode(employeeCode);
        if (employee == null) {
            return List.of();
        }
        return compensationDAO.getCompensationHistory(employee.getEmployeeID());
    }

    /**
     * Lấy compensation history của nhân viên theo employeeId
     */
    public List<EmployeeCompensation> getCompensationHistoryByEmployeeId(UUID employeeId) {
        return compensationDAO.getCompensationHistory(employeeId);
    }

    /**
     * Xóa compensation
     */
    public boolean deleteCompensation(UUID compensationId) {
        return compensationDAO.delete(compensationId);
    }

    /**
     * Cập nhật compensation
     */
    public boolean updateCompensation(UUID compensationId, String compensationType,
                                      BigDecimal baseMonthlySalary, BigDecimal hourlyRate,
                                      BigDecimal perShiftRate, BigDecimal overtimeRate,
                                      BigDecimal bonusAmount, BigDecimal commissionRate,
                                      BigDecimal allowanceAmount, BigDecimal deductionAmount) {
        try {
            EmployeeCompensation compensation = compensationDAO.findById(compensationId);
            if (compensation == null) {
                System.err.println("Compensation not found: " + compensationId);
                return false;
            }

            compensation.setCompensationType(compensationType);
            compensation.setBaseMonthlySalary(baseMonthlySalary);
            compensation.setHourlyRate(hourlyRate);
            compensation.setPerShiftRate(perShiftRate);
            compensation.setOvertimeRate(overtimeRate);
            compensation.setBonusAmount(bonusAmount);
            compensation.setCommissionRate(commissionRate);
            compensation.setAllowanceAmount(allowanceAmount);
            compensation.setDeductionAmount(deductionAmount);

            return compensationDAO.update(compensation);
        } catch (Exception e) {
            System.err.println("Error updating compensation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Format tiền tệ để hiển thị
     */
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return String.format("%,.0f", amount);
    }

    /**
     * Format compensation type thành tiếng Việt
     */
    public String formatCompensationType(String type) {
        if (type == null) return "";
        return switch (type) {
            case "Fixed" -> "Lương cứng";
            case "PerShift" -> "Theo ca";
            case "Hybrid" -> "Theo giờ";
            default -> type;
        };
    }
}
