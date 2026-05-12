package com.liteflow.service.employee;

import com.liteflow.dao.employee.EmployeeDAO;
import com.liteflow.model.auth.Employee;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service cho Employee management
 */
public class EmployeeService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    // ==============================
    // CRUD Operations
    // ==============================

    /**
     * Lấy tất cả employees
     */
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAll();
    }

    /**
     * Tìm employee theo ID
     */
    public Optional<Employee> getEmployeeById(UUID employeeID) {
        return Optional.ofNullable(employeeDAO.findById(employeeID));
    }

    /**
     * Tìm employee theo mã nhân viên
     */
    public Optional<Employee> getEmployeeByCode(String employeeCode) {
        return Optional.ofNullable(employeeDAO.findByEmployeeCode(employeeCode));
    }

    /**
     * Tìm employee theo UserID
     */
    public Optional<Employee> getEmployeeByUserID(UUID userID) {
        return Optional.ofNullable(employeeDAO.findByUserID(userID));
    }

    /**
     * Tạo employee mới
     */
    public boolean createEmployee(Employee employee) {
        if (employee == null) {
            return false;
        }

        // Validate required fields
        if (employee.getEmployeeCode() == null || employee.getEmployeeCode().trim().isEmpty()) {
            return false;
        }

        if (employee.getFullName() == null || employee.getFullName().trim().isEmpty()) {
            return false;
        }

        // Set default values
        if (employee.getEmploymentStatus() == null) {
            employee.setEmploymentStatus("Đang làm");
        }

        return employeeDAO.insert(employee);
    }

    /**
     * Cập nhật employee
     */
    public boolean updateEmployee(Employee employee) {
        if (employee == null || employee.getEmployeeID() == null) {
            return false;
        }

        return employeeDAO.update(employee);
    }

    /**
     * Xóa employee
     */
    public boolean deleteEmployee(UUID employeeID) {
        return employeeDAO.delete(employeeID);
    }

    // ==============================
    // Search & Filter Operations
    // ==============================

    /**
     * Tìm kiếm employees theo từ khóa
     */
    public List<Employee> searchEmployees(String searchTerm) {
        return employeeDAO.searchEmployees(searchTerm);
    }

    /**
     * Lấy employees đang làm việc
     */
    public List<Employee> getActiveEmployees() {
        return employeeDAO.getActiveEmployees();
    }

    // ==============================
    // Statistics
    // ==============================

    /**
     * Lấy tổng số employees
     */
    public long getTotalEmployeeCount() {
        return employeeDAO.getTotalEmployeeCount();
    }

    /**
     * Lấy số employees đang làm việc
     */
    public long getActiveEmployeeCount() {
        return employeeDAO.getActiveEmployeeCount();
    }

    /**
     * Lấy số employees theo vị trí
     */
    public long getEmployeeCountByPosition(String position) {
        return employeeDAO.getEmployeeCountByPosition(position);
    }

    /**
     * Lấy thống kê tổng quan
     */
    public EmployeeStatistics getEmployeeStatistics() {
        EmployeeStatistics stats = new EmployeeStatistics();
        stats.totalEmployees = getTotalEmployeeCount();
        stats.activeEmployees = getActiveEmployeeCount();
        stats.inactiveEmployees = stats.totalEmployees - stats.activeEmployees;
        stats.managerCount = getEmployeeCountByPosition("Quản lý");
        stats.staffCount = getEmployeeCountByPosition("Nhân viên");
        stats.cashierCount = getEmployeeCountByPosition("Thu ngân");
        stats.chefCount = getEmployeeCountByPosition("Đầu bếp");
        
        return stats;
    }

    // ==============================
    // Inner Classes
    // ==============================

    /**
     * Class chứa thống kê employees
     */
    public static class EmployeeStatistics {
        public long totalEmployees;
        public long activeEmployees;
        public long inactiveEmployees;
        public long managerCount;
        public long staffCount;
        public long cashierCount;
        public long chefCount;
        
        // Getters for JSP EL
        public long getTotalEmployees() {
            return totalEmployees;
        }
        
        public long getActiveEmployees() {
            return activeEmployees;
        }
        
        public long getInactiveEmployees() {
            return inactiveEmployees;
        }
        
        public long getManagerCount() {
            return managerCount;
        }
        
        public long getStaffCount() {
            return staffCount;
        }
        
        public long getCashierCount() {
            return cashierCount;
        }
        
        public long getChefCount() {
            return chefCount;
        }
    }
}
