package com.liteflow.controller.employee;

import com.liteflow.dao.timesheet.EmployeeAttendanceDAO;
import com.liteflow.model.auth.Employee;
import com.liteflow.model.timesheet.EmployeeAttendance;
import com.liteflow.service.employee.EmployeeService;
import com.liteflow.service.auth.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "DashboardEmployeeServlet", urlPatterns = {"/dashboard-employee"})
public class DashboardEmployeeServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final EmployeeService employeeService = new EmployeeService();
    private final EmployeeAttendanceDAO attendanceDAO = new EmployeeAttendanceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy userId từ session
        UUID userId = getUserIdFromSession(req);
        
        if (userId == null) {
            // Không có user đăng nhập, redirect về login
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        
        // Load UserRoles từ session hoặc database
       
        List<String> userRoles = (List<String>) req.getSession().getAttribute("UserRoles");
        
        if (userRoles == null || userRoles.isEmpty()) {
            userRoles = userService.getRoleNames(userId);
            req.getSession().setAttribute("UserRoles", userRoles);
        }
        
        // Kiểm tra role Employee
        boolean isEmployee = false;
        if (userRoles != null) {
            for (String role : userRoles) {
                if ("Employee".equalsIgnoreCase(role)) {
                    isEmployee = true;
                    break;
                }
            }
        }
        
        // Nếu không phải Employee, redirect về dashboard thường
        if (!isEmployee) {
            resp.sendRedirect(req.getContextPath() + "/dashboard.jsp");
            return;
        }
        
        // Load attendance data cho NHÂN VIÊN HIỆN TẠI ĐANG ĐĂNG NHẬP
        Employee currentEmployee = employeeService.getEmployeeByUserID(userId).orElse(null);
        
        if (currentEmployee == null) {
            // User không có employee record, redirect
            resp.sendRedirect(req.getContextPath() + "/dashboard.jsp");
            return;
        }
        
        // Lấy thông tin nhân viên hiện tại và load attendance data
        // Get month and year from request parameters, default to current month/year
        String monthParam = req.getParameter("month");
        String yearParam = req.getParameter("year");
        
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        if (monthParam != null && !monthParam.trim().isEmpty()) {
            try {
                month = Integer.parseInt(monthParam.trim());
                if (month < 1 || month > 12) {
                    month = now.getMonthValue();
                }
            } catch (NumberFormatException e) {
                month = now.getMonthValue();
            }
        }
        
        if (yearParam != null && !yearParam.trim().isEmpty()) {
            try {
                year = Integer.parseInt(yearParam.trim());
                if (year < 2020 || year > 2030) {
                    year = now.getYear();
                }
            } catch (NumberFormatException e) {
                year = now.getYear();
            }
        }
        
        // CHỈ LẤY ATTENDANCE CỦA EMPLOYEE HIỆN TẠI
        List<EmployeeAttendance> attendanceList = attendanceDAO.findByEmployeeAndMonth(
            currentEmployee.getEmployeeID(), year, month
        );
        
        // Tạo map để dễ truy cập theo ngày
        Map<Integer, EmployeeAttendance> attendanceMap = new HashMap<>();
        for (EmployeeAttendance attendance : attendanceList) {
            // Đảm bảo chỉ lấy attendance của employee này
            if (attendance.getEmployee() != null && 
                attendance.getEmployee().getEmployeeID().equals(currentEmployee.getEmployeeID())) {
                attendanceMap.put(attendance.getWorkDate().getDayOfMonth(), attendance);
            }
        }
        
        // Lưu thông tin vào session
        req.getSession().setAttribute("UserEmployeeCode", currentEmployee.getEmployeeCode());
        
        // Set attributes cho JSP
        req.setAttribute("currentYear", year);
        req.setAttribute("currentMonth", month);
        req.setAttribute("attendanceMap", attendanceMap);
        req.setAttribute("currentEmployee", currentEmployee);
        
        req.getRequestDispatcher("/dashboard-employee.jsp").forward(req, resp);
    }
    
    /**
     * Helper method để lấy userId từ session
     */
    private UUID getUserIdFromSession(HttpServletRequest req) {
        Object userLogin = req.getSession().getAttribute("UserLogin");
        if (userLogin == null) {
            return null;
        }
        
        if (userLogin instanceof UUID) {
            return (UUID) userLogin;
        } else if (userLogin instanceof String) {
            try {
                return UUID.fromString((String) userLogin);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}

