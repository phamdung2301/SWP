package com.liteflow.controller.employee;

import com.liteflow.dao.payroll.EmployeeCompensationDAO;
import com.liteflow.dao.payroll.PayrollEntryDAO;
import com.liteflow.model.auth.Employee;
import com.liteflow.service.employee.EmployeeService;
import com.liteflow.service.payroll.PayrollCalculationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@WebServlet(name = "EmployeeAPIServlet", urlPatterns = {"/api/employee/*"})
public class EmployeeAPIServlet extends HttpServlet {

    private final EmployeeService employeeService = new EmployeeService();
    private final EmployeeCompensationDAO compensationDAO = new EmployeeCompensationDAO();
    private final PayrollCalculationService payrollCalculationService = new PayrollCalculationService();
    private final PayrollEntryDAO payrollEntryDAO = new PayrollEntryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            UUID userId = getUserIdFromSession(req);
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("{\"error\":\"Not authenticated\"}");
                return;
            }

            // GET /api/employee/salary-summary - Lấy tóm tắt lương theo tháng
            if ("/salary-summary".equals(pathInfo)) {
                handleSalarySummary(userId, req, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print("{\"error\":\"Endpoint not found\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private void handleSalarySummary(UUID userId, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // Get month and year from query parameters
        String monthStr = req.getParameter("month");
        String yearStr = req.getParameter("year");

        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        
        if (monthStr != null && !monthStr.trim().isEmpty()) {
            try {
                month = Integer.parseInt(monthStr.trim());
                if (month < 1 || month > 12) {
                    month = now.getMonthValue();
                }
            } catch (NumberFormatException e) {
                month = now.getMonthValue();
            }
        }
        
        if (yearStr != null && !yearStr.trim().isEmpty()) {
            try {
                year = Integer.parseInt(yearStr.trim());
                if (year < 2020 || year > 2030) {
                    year = now.getYear();
                }
            } catch (NumberFormatException e) {
                year = now.getYear();
            }
        }

        // Get employee from user ID
        Employee employee = employeeService.getEmployeeByUserID(userId).orElse(null);
        if (employee == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\":\"Employee not found\"}");
            return;
        }

        // Calculate salary using PayrollCalculationService
        PayrollCalculationService.MonthlySalaryResult salaryResult = 
            payrollCalculationService.calculateMonthlySalary(employee.getEmployeeID(), month, year);
        
        BigDecimal totalSalary = salaryResult.getTotalSalary();
        BigDecimal totalAdvance = BigDecimal.ZERO; // TODO: Implement advance tracking if needed
        BigDecimal totalDeduction = salaryResult.getDeductions();
        BigDecimal totalPaid = payrollEntryDAO.getTotalPaidForMonth(employee.getEmployeeID(), month, year);
        BigDecimal totalRemaining = totalSalary.subtract(totalAdvance).subtract(totalDeduction).subtract(totalPaid);

        // Build JSON response
        String json = String.format(
            "{\"totalSalary\":%s,\"totalAdvance\":%s,\"totalDeduction\":%s,\"totalPaid\":%s,\"totalRemaining\":%s}",
            totalSalary,
            totalAdvance,
            totalDeduction,
            totalPaid,
            totalRemaining
        );

        resp.getWriter().print(json);
    }

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

