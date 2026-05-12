package com.liteflow.service.payroll;

import com.liteflow.dao.employee.EmployeeDAO;
import com.liteflow.dao.payroll.PayrollEntryDAO;
import com.liteflow.dao.payroll.PayPeriodDAO;
import com.liteflow.dao.payroll.PayrollRunDAO;
import com.liteflow.model.auth.Employee;
import com.liteflow.model.payroll.PayPeriod;
import com.liteflow.model.payroll.PayrollEntry;
import com.liteflow.model.payroll.PayrollRun;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing payroll entries and payments
 */
public class PayrollService {

    private final PayrollEntryDAO payrollEntryDAO;
    private final PayrollCalculationService calculationService;
    private final EmployeeDAO employeeDAO;
    private final PayPeriodDAO payPeriodDAO;
    private final PayrollRunDAO payrollRunDAO;

    public PayrollService() {
        this.payrollEntryDAO = new PayrollEntryDAO();
        this.calculationService = new PayrollCalculationService();
        this.employeeDAO = new EmployeeDAO();
        this.payPeriodDAO = new PayPeriodDAO();
        this.payrollRunDAO = new PayrollRunDAO();
    }

    /**
     * Get payroll list for all employees in a specific month
     */
    public List<PayrollEntryDTO> getPayrollForMonth(int month, int year) {
        List<PayrollEntryDTO> result = new ArrayList<>();
        
        // Get all active employees
        List<Employee> employees = employeeDAO.getActiveEmployees();
        
        for (Employee employee : employees) {
            PayrollEntryDTO dto = getPayrollForEmployee(employee.getEmployeeID(), month, year);
            if (dto != null) {
                result.add(dto);
            }
        }
        
        return result;
    }

    /**
     * Get payroll for a specific employee in a month
     */
    public PayrollEntryDTO getPayrollForEmployee(UUID employeeId, int month, int year) {
        Employee employee = employeeDAO.findById(employeeId);
        if (employee == null) {
            return null;
        }

        // Check if payroll entry exists
        PayrollEntry existingEntry = payrollEntryDAO.findByEmployeeAndMonthYear(employeeId, month, year);
        
        if (existingEntry == null) {
            // Create payroll entry if it doesn't exist
            existingEntry = createPayrollEntry(employeeId, month, year);
        }

        if (existingEntry == null) {
            return null;
        }

        // Calculate current salary
        PayrollCalculationService.MonthlySalaryResult salaryResult = 
            calculationService.calculateMonthlySalary(employeeId, month, year);
        
        BigDecimal totalPaid = payrollEntryDAO.getTotalPaidForMonth(employeeId, month, year);
        BigDecimal totalRemaining = salaryResult.getTotalSalary()
            .subtract(salaryResult.getDeductions())
            .subtract(totalPaid);

        PayrollEntryDTO dto = new PayrollEntryDTO();
        dto.setPayrollEntryId(existingEntry.getPayrollEntryId());
        dto.setEmployeeId(employeeId);
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setEmployeeName(employee.getFullName());
        dto.setCompensationType(existingEntry.getCompensationType());
        dto.setTotalSalary(salaryResult.getTotalSalary());
        dto.setAllowances(salaryResult.getAllowances());
        dto.setBonuses(salaryResult.getBonuses());
        dto.setDeductions(salaryResult.getDeductions());
        dto.setTotalPaid(totalPaid);
        dto.setTotalRemaining(totalRemaining);
        dto.setIsPaid(existingEntry.getIsPaid());
        
        return dto;
    }

    /**
     * Create a new payroll entry for an employee in a month
     */
    private PayrollEntry createPayrollEntry(UUID employeeId, int month, int year) {
        try {
            // Get or create pay period for the month
            PayPeriod payPeriod = getOrCreatePayPeriod(month, year);
            
            // Get or create payroll run
            PayrollRun payrollRun = getOrCreatePayrollRun(payPeriod);
            
            // Calculate salary
            PayrollCalculationService.MonthlySalaryResult salaryResult = 
                calculationService.calculateMonthlySalary(employeeId, month, year);
            
            Employee employee = employeeDAO.findById(employeeId);
            if (employee == null) {
                return null;
            }

            // Create payroll entry
            PayrollEntry entry = new PayrollEntry();
            entry.setPayrollRun(payrollRun);
            entry.setEmployee(employee);
            
            // Get compensation type from active compensation
            com.liteflow.dao.payroll.EmployeeCompensationDAO compDAO = 
                new com.liteflow.dao.payroll.EmployeeCompensationDAO();
            com.liteflow.model.payroll.EmployeeCompensation compensation = 
                compDAO.getActiveCompensation(employeeId);
            
            if (compensation != null) {
                entry.setCompensationType(compensation.getCompensationType());
                entry.setBaseSalary(compensation.getBaseMonthlySalary());
                entry.setHourlyRate(compensation.getHourlyRate());
                entry.setPerShiftRate(compensation.getPerShiftRate());
                entry.setAllowances(compensation.getAllowanceAmount() != null ? 
                    compensation.getAllowanceAmount() : BigDecimal.ZERO);
                entry.setBonuses(compensation.getBonusAmount() != null ? 
                    compensation.getBonusAmount() : BigDecimal.ZERO);
                entry.setDeductions(compensation.getDeductionAmount() != null ? 
                    compensation.getDeductionAmount() : BigDecimal.ZERO);
            } else {
                entry.setCompensationType("Fixed");
                entry.setAllowances(BigDecimal.ZERO);
                entry.setBonuses(BigDecimal.ZERO);
                entry.setDeductions(BigDecimal.ZERO);
            }
            
            // Set hours/shifts worked
            if ("Hybrid".equals(entry.getCompensationType())) {
                BigDecimal hours = calculationService.getTotalHoursWorked(employeeId, month, year);
                entry.setHoursWorked(hours);
            } else if ("PerShift".equals(entry.getCompensationType())) {
                int shifts = calculationService.getShiftsWorked(employeeId, month, year);
                entry.setShiftsWorked(shifts);
            }
            
            entry.setGrossPay(salaryResult.getTotalSalary());
            entry.setNetPay(salaryResult.getTotalSalary().subtract(salaryResult.getDeductions()));
            entry.setIsPaid(false);
            
            payrollEntryDAO.insert(entry);
            return entry;
        } catch (Exception e) {
            System.err.println("Error creating payroll entry: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get or create pay period for a month
     */
    private PayPeriod getOrCreatePayPeriod(int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, startDate.lengthOfMonth());
        
        // Try to find existing pay period
        List<PayPeriod> periods = payPeriodDAO.getAll();
        for (PayPeriod period : periods) {
            if (period.getStartDate().equals(startDate) && 
                period.getEndDate().equals(endDate)) {
                return period;
            }
        }
        
        // Create new pay period
        PayPeriod period = new PayPeriod();
        period.setName("Tháng " + month + "/" + year);
        period.setPeriodType("Monthly");
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        period.setStatus("Open");
        payPeriodDAO.insert(period);
        return period;
    }

    /**
     * Get or create payroll run for a pay period
     */
    private PayrollRun getOrCreatePayrollRun(PayPeriod payPeriod) {
        // Try to find existing run
        List<PayrollRun> runs = payrollRunDAO.getAll();
        for (PayrollRun run : runs) {
            if (run.getPayPeriod().getPayPeriodId().equals(payPeriod.getPayPeriodId())) {
                return run;
            }
        }
        
        // Create new run
        PayrollRun run = new PayrollRun();
        run.setPayPeriod(payPeriod);
        run.setRunNumber(1);
        run.setStatus("Draft");
        payrollRunDAO.insert(run);
        return run;
    }

    /**
     * Mark payroll entry as paid
     */
    public boolean markAsPaid(UUID payrollEntryId) {
        return payrollEntryDAO.markAsPaid(payrollEntryId);
    }

    /**
     * Generate payroll entries for all active employees in a specific month
     * Returns the number of entries created
     */
    public int generatePayrollForMonth(int month, int year) {
        int createdCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        
        System.out.println("=== Starting payroll generation for month " + month + "/" + year + " ===");
        
        // Get all active employees
        List<Employee> employees = employeeDAO.getActiveEmployees();
        System.out.println("Found " + employees.size() + " active employees");
        
        // If no active employees found, try getting all employees and filter manually
        if (employees.isEmpty()) {
            System.out.println("No active employees found with getActiveEmployees(). Trying to get all employees...");
            List<Employee> allEmployees = employeeDAO.getAll();
            System.out.println("Total employees in database: " + allEmployees.size());
            
            // Filter for active employees manually (in case of encoding issues)
            employees = new ArrayList<>();
            for (Employee emp : allEmployees) {
                String status = emp.getEmploymentStatus();
                System.out.println("  Employee: " + emp.getEmployeeCode() + " - Status: " + status);
                if (status != null && (status.contains("làm") || status.contains("lam") || 
                    status.equalsIgnoreCase("Đang làm") || status.equalsIgnoreCase("Dang lam") ||
                    status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("ACTIVE"))) {
                    employees.add(emp);
                    System.out.println("    -> Added as active");
                }
            }
            System.out.println("After manual filtering: " + employees.size() + " active employees");
        }
        
        if (employees.isEmpty()) {
            System.out.println("No active employees found. Nothing to generate.");
            return 0;
        }
        
        // Get or create pay period
        PayPeriod payPeriod = getOrCreatePayPeriod(month, year);
        System.out.println("PayPeriod: " + payPeriod.getPayPeriodId() + " - " + payPeriod.getName());
        
        // Get or create payroll run
        PayrollRun payrollRun = getOrCreatePayrollRun(payPeriod);
        System.out.println("PayrollRun: " + payrollRun.getPayrollRunId());
        
        for (Employee employee : employees) {
            try {
                System.out.println("Processing employee: " + employee.getEmployeeCode() + " - " + employee.getFullName());
                
                // Check if payroll entry already exists
                PayrollEntry existingEntry = payrollEntryDAO.findByEmployeeAndMonthYear(
                    employee.getEmployeeID(), month, year);
                
                if (existingEntry != null) {
                    System.out.println("  -> Payroll entry already exists, skipping");
                    skippedCount++;
                    continue;
                }
                
                // Create new payroll entry
                System.out.println("  -> Creating new payroll entry...");
                PayrollEntry entry = createPayrollEntryForRun(
                    employee.getEmployeeID(), month, year, payrollRun);
                if (entry != null) {
                    System.out.println("  -> Successfully created payroll entry: " + entry.getPayrollEntryId());
                    createdCount++;
                } else {
                    System.out.println("  -> Failed to create payroll entry (returned null)");
                    errorCount++;
                }
            } catch (Exception e) {
                System.err.println("Error generating payroll for employee " + 
                    employee.getEmployeeCode() + ": " + e.getMessage());
                e.printStackTrace();
                errorCount++;
            }
        }
        
        System.out.println("=== Payroll generation completed ===");
        System.out.println("Total employees: " + employees.size());
        System.out.println("Created: " + createdCount + ", Skipped: " + skippedCount + ", Errors: " + errorCount);
        
        // If no entries were created but there are employees, log warning
        if (createdCount == 0 && employees.size() > 0) {
            System.out.println("WARNING: No entries created. Possible reasons:");
            System.out.println("  - All employees already have payroll entries for this month");
            System.out.println("  - Errors occurred during creation (check logs above)");
        }
        
        return createdCount;
    }

    /**
     * Create payroll entry for a specific employee and payroll run
     */
    private PayrollEntry createPayrollEntryForRun(UUID employeeId, int month, int year, PayrollRun payrollRun) {
        try {
            System.out.println("    -> Calculating salary for employee: " + employeeId);
            
            // Calculate salary
            PayrollCalculationService.MonthlySalaryResult salaryResult = 
                calculationService.calculateMonthlySalary(employeeId, month, year);
            
            System.out.println("    -> Salary calculated: Total=" + salaryResult.getTotalSalary() + 
                ", Allowances=" + salaryResult.getAllowances() + 
                ", Bonuses=" + salaryResult.getBonuses() + 
                ", Deductions=" + salaryResult.getDeductions());
            
            Employee employee = employeeDAO.findById(employeeId);
            if (employee == null) {
                System.err.println("    -> Employee not found: " + employeeId);
                return null;
            }

            // Create payroll entry
            PayrollEntry entry = new PayrollEntry();
            entry.setPayrollRun(payrollRun);
            entry.setEmployee(employee);
            
            // Get compensation type from active compensation
            com.liteflow.dao.payroll.EmployeeCompensationDAO compDAO = 
                new com.liteflow.dao.payroll.EmployeeCompensationDAO();
            com.liteflow.model.payroll.EmployeeCompensation compensation = 
                compDAO.getActiveCompensation(employeeId);
            
            if (compensation != null) {
                System.out.println("    -> Found compensation: Type=" + compensation.getCompensationType());
                entry.setCompensationType(compensation.getCompensationType());
                entry.setBaseSalary(compensation.getBaseMonthlySalary());
                entry.setHourlyRate(compensation.getHourlyRate());
                entry.setPerShiftRate(compensation.getPerShiftRate());
                entry.setAllowances(compensation.getAllowanceAmount() != null ? 
                    compensation.getAllowanceAmount() : BigDecimal.ZERO);
                entry.setBonuses(compensation.getBonusAmount() != null ? 
                    compensation.getBonusAmount() : BigDecimal.ZERO);
                entry.setDeductions(compensation.getDeductionAmount() != null ? 
                    compensation.getDeductionAmount() : BigDecimal.ZERO);
            } else {
                System.out.println("    -> WARNING: No compensation found for employee. Creating entry with zero salary.");
                entry.setCompensationType("Fixed");
                entry.setBaseSalary(BigDecimal.ZERO);
                entry.setHourlyRate(null);
                entry.setPerShiftRate(null);
                entry.setAllowances(BigDecimal.ZERO);
                entry.setBonuses(BigDecimal.ZERO);
                entry.setDeductions(BigDecimal.ZERO);
            }
            
            // Set hours/shifts worked
            if ("Hybrid".equals(entry.getCompensationType())) {
                BigDecimal hours = calculationService.getTotalHoursWorked(employeeId, month, year);
                entry.setHoursWorked(hours);
                System.out.println("    -> Hours worked: " + hours);
            } else if ("PerShift".equals(entry.getCompensationType())) {
                int shifts = calculationService.getShiftsWorked(employeeId, month, year);
                entry.setShiftsWorked(shifts);
                System.out.println("    -> Shifts worked: " + shifts);
            }
            
            // Set overtime and holiday hours to zero if null
            if (entry.getOvertimeHours() == null) {
                entry.setOvertimeHours(BigDecimal.ZERO);
            }
            if (entry.getHolidayHours() == null) {
                entry.setHolidayHours(BigDecimal.ZERO);
            }
            
            entry.setGrossPay(salaryResult.getTotalSalary());
            entry.setNetPay(salaryResult.getTotalSalary().subtract(salaryResult.getDeductions()));
            entry.setIsPaid(false);
            
            // Set currency fields (required)
            if (entry.getCurrency() == null) {
                entry.setCurrency("VND");
            }
            if (entry.getPaidInCurrency() == null) {
                entry.setPaidInCurrency("VND");
            }
            
            System.out.println("    -> Inserting payroll entry...");
            System.out.println("    -> Entry details: GrossPay=" + entry.getGrossPay() + 
                ", NetPay=" + entry.getNetPay() + ", CompensationType=" + entry.getCompensationType());
            payrollEntryDAO.insert(entry);
            System.out.println("    -> Payroll entry inserted successfully: " + entry.getPayrollEntryId());
            
            return entry;
        } catch (Exception e) {
            System.err.println("Error creating payroll entry for employee " + employeeId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recalculate payroll entry for an employee in a specific month/year
     * This should be called when attendance data changes
     */
    public boolean recalculatePayrollEntry(UUID employeeId, int month, int year) {
        try {
            System.out.println("=== Recalculating payroll for employee: " + employeeId + ", month: " + month + "/" + year + " ===");
            
            // Find existing payroll entry
            PayrollEntry entry = payrollEntryDAO.findByEmployeeAndMonthYear(employeeId, month, year);
            
            if (entry == null) {
                System.out.println("  -> No existing payroll entry found. Creating new one...");
                // Create new entry if doesn't exist
                PayPeriod payPeriod = getOrCreatePayPeriod(month, year);
                PayrollRun payrollRun = getOrCreatePayrollRun(payPeriod);
                entry = createPayrollEntryForRun(employeeId, month, year, payrollRun);
                return entry != null;
            }
            
            System.out.println("  -> Found existing payroll entry: " + entry.getPayrollEntryId());
            
            // Recalculate salary
            PayrollCalculationService.MonthlySalaryResult salaryResult = 
                calculationService.calculateMonthlySalary(employeeId, month, year);
            
            System.out.println("  -> Recalculated salary: Total=" + salaryResult.getTotalSalary() + 
                ", Allowances=" + salaryResult.getAllowances() + 
                ", Bonuses=" + salaryResult.getBonuses() + 
                ", Deductions=" + salaryResult.getDeductions());
            
            // Update hours/shifts worked
            if ("Hybrid".equals(entry.getCompensationType())) {
                BigDecimal hours = calculationService.getTotalHoursWorked(employeeId, month, year);
                entry.setHoursWorked(hours);
                System.out.println("  -> Updated hours worked: " + hours);
            } else if ("PerShift".equals(entry.getCompensationType())) {
                int shifts = calculationService.getShiftsWorked(employeeId, month, year);
                entry.setShiftsWorked(shifts);
                System.out.println("  -> Updated shifts worked: " + shifts);
            }
            
            // Update gross pay and net pay
            entry.setGrossPay(salaryResult.getTotalSalary());
            entry.setNetPay(salaryResult.getTotalSalary().subtract(salaryResult.getDeductions()));
            
            System.out.println("  -> Updated GrossPay=" + entry.getGrossPay() + ", NetPay=" + entry.getNetPay());
            
            // Save changes
            payrollEntryDAO.update(entry);
            System.out.println("  -> Payroll entry updated successfully");
            
            return true;
        } catch (Exception e) {
            System.err.println("Error recalculating payroll entry for employee " + employeeId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Recalculate all payroll entries for a specific month/year
     * Useful when bulk attendance updates occur
     */
    public int recalculatePayrollForMonth(int month, int year) {
        System.out.println("=== Recalculating all payroll entries for month " + month + "/" + year + " ===");
        
        List<PayrollEntry> entries = payrollEntryDAO.findByMonth(month, year);
        System.out.println("Found " + entries.size() + " payroll entries to recalculate");
        
        int successCount = 0;
        int errorCount = 0;
        
        for (PayrollEntry entry : entries) {
            try {
                boolean success = recalculatePayrollEntry(
                    entry.getEmployee().getEmployeeID(), 
                    month, 
                    year
                );
                if (success) {
                    successCount++;
                } else {
                    errorCount++;
                }
            } catch (Exception e) {
                System.err.println("Error recalculating payroll entry " + entry.getPayrollEntryId() + ": " + e.getMessage());
                errorCount++;
            }
        }
        
        System.out.println("=== Recalculation completed ===");
        System.out.println("Success: " + successCount + ", Errors: " + errorCount);
        
        return successCount;
    }

    /**
     * DTO for payroll entry display
     */
    public static class PayrollEntryDTO {
        private UUID payrollEntryId;
        private UUID employeeId;
        private String employeeCode;
        private String employeeName;
        private String compensationType;
        private BigDecimal totalSalary;
        private BigDecimal allowances;
        private BigDecimal bonuses;
        private BigDecimal deductions;
        private BigDecimal totalPaid;
        private BigDecimal totalRemaining;
        private Boolean isPaid;

        // Getters and setters
        public UUID getPayrollEntryId() { return payrollEntryId; }
        public void setPayrollEntryId(UUID payrollEntryId) { this.payrollEntryId = payrollEntryId; }
        public UUID getEmployeeId() { return employeeId; }
        public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public String getCompensationType() { return compensationType; }
        public void setCompensationType(String compensationType) { this.compensationType = compensationType; }
        public BigDecimal getTotalSalary() { return totalSalary; }
        public void setTotalSalary(BigDecimal totalSalary) { this.totalSalary = totalSalary; }
        public BigDecimal getAllowances() { return allowances; }
        public void setAllowances(BigDecimal allowances) { this.allowances = allowances; }
        public BigDecimal getBonuses() { return bonuses; }
        public void setBonuses(BigDecimal bonuses) { this.bonuses = bonuses; }
        public BigDecimal getDeductions() { return deductions; }
        public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
        public BigDecimal getTotalPaid() { return totalPaid; }
        public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
        public BigDecimal getTotalRemaining() { return totalRemaining; }
        public void setTotalRemaining(BigDecimal totalRemaining) { this.totalRemaining = totalRemaining; }
        public Boolean getIsPaid() { return isPaid; }
        public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
    }
}

