package com.liteflow.controller.employee;

import com.liteflow.model.auth.Employee;
import com.liteflow.model.payroll.EmployeeCompensation;
import com.liteflow.service.employee.CompensationService;
import com.liteflow.service.employee.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "CompensationServlet", urlPatterns = {"/compensation"})
public class CompensationServlet extends HttpServlet {

    private CompensationService compensationService;
    private EmployeeService employeeService;

    @Override
    public void init() throws ServletException {
        compensationService = new CompensationService();
        employeeService = new EmployeeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        System.out.println("CompensationServlet doGet - action: " + action);

        if ("get".equals(action)) {
            handleGetCompensation(request, response);
        } else if ("getAllWithEmployees".equals(action)) {
            handleGetAllWithEmployees(request, response);
        } else {
            // Default: Return all active compensations
            handleGetAllCompensations(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        try {
            String action = request.getParameter("action");

            if ("save".equals(action)) {
                handleSaveCompensation(request, response);
            } else if ("update".equals(action)) {
                handleUpdateCompensation(request, response);
            } else if ("delete".equals(action)) {
                handleDeleteCompensation(request, response);
            } else {
                response.getWriter().write("{\"success\": false, \"error\": \"Invalid action\"}");
            }
        } catch (Exception e) {
            System.err.println("Error in CompensationServlet POST: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleGetCompensation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String employeeCode = request.getParameter("employeeCode");

        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"error\": \"Missing employee code\"}");
            return;
        }

        EmployeeCompensation compensation = compensationService.getActiveCompensation(employeeCode);

        response.setContentType("application/json");
        if (compensation != null) {
            String json = buildCompensationJson(compensation, null);
            response.getWriter().write("{\"success\": true, \"data\": " + json + "}");
        } else {
            response.getWriter().write("{\"success\": false, \"error\": \"No active compensation found\"}");
        }
    }

    private void handleGetAllCompensations(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<EmployeeCompensation> compensations = compensationService.getAllActiveCompensations();

        response.setContentType("application/json");
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < compensations.size(); i++) {
            if (i > 0) json.append(",");
                json.append(buildCompensationJson(compensations.get(i), null));
        }
        json.append("]");

        response.getWriter().write(json.toString());
    }

    private void handleSaveCompensation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String employeeCode = request.getParameter("employeeCode");
        String compensationType = request.getParameter("compensationType");

        BigDecimal baseMonthlySalary = parseBigDecimal(request.getParameter("baseMonthlySalary"));
        BigDecimal hourlyRate = parseBigDecimal(request.getParameter("hourlyRate"));
        BigDecimal perShiftRate = parseBigDecimal(request.getParameter("perShiftRate"));
        BigDecimal overtimeRate = parseBigDecimal(request.getParameter("overtimeRate"));
        BigDecimal bonusAmount = parseBigDecimal(request.getParameter("bonusAmount"));
        BigDecimal commissionRate = parseBigDecimal(request.getParameter("commissionRate"));
        BigDecimal allowanceAmount = parseBigDecimal(request.getParameter("allowanceAmount"));
        BigDecimal deductionAmount = parseBigDecimal(request.getParameter("deductionAmount"));

        boolean success = compensationService.saveCompensation(
            employeeCode, compensationType,
            baseMonthlySalary, hourlyRate, perShiftRate,
            overtimeRate, bonusAmount, commissionRate,
            allowanceAmount, deductionAmount
        );

        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"Lưu cấu hình lương thành công\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"error\": \"Lưu cấu hình lương thất bại\"}");
        }
    }

    private void handleUpdateCompensation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String compensationIdStr = request.getParameter("compensationId");
        String compensationType = request.getParameter("compensationType");

        UUID compensationId = UUID.fromString(compensationIdStr);
        BigDecimal baseMonthlySalary = parseBigDecimal(request.getParameter("baseMonthlySalary"));
        BigDecimal hourlyRate = parseBigDecimal(request.getParameter("hourlyRate"));
        BigDecimal perShiftRate = parseBigDecimal(request.getParameter("perShiftRate"));
        BigDecimal overtimeRate = parseBigDecimal(request.getParameter("overtimeRate"));
        BigDecimal bonusAmount = parseBigDecimal(request.getParameter("bonusAmount"));
        BigDecimal commissionRate = parseBigDecimal(request.getParameter("commissionRate"));
        BigDecimal allowanceAmount = parseBigDecimal(request.getParameter("allowanceAmount"));
        BigDecimal deductionAmount = parseBigDecimal(request.getParameter("deductionAmount"));

        boolean success = compensationService.updateCompensation(
            compensationId, compensationType,
            baseMonthlySalary, hourlyRate, perShiftRate,
            overtimeRate, bonusAmount, commissionRate,
            allowanceAmount, deductionAmount
        );

        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"Cập nhật cấu hình lương thành công\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"error\": \"Cập nhật cấu hình lương thất bại\"}");
        }
    }

    private void handleDeleteCompensation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String compensationIdStr = request.getParameter("compensationId");
        UUID compensationId = UUID.fromString(compensationIdStr);

        boolean success = compensationService.deleteCompensation(compensationId);

        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"Xóa cấu hình lương thành công\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"error\": \"Xóa cấu hình lương thất bại\"}");
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildCompensationJson(EmployeeCompensation comp, Map<UUID, Employee> employeeMap) {
        try {
            String employeeCode = "";
            String employeeName = "";
            
            // Try to get employee from compensation (should be eager fetched now)
            Employee emp = null;
            try {
                emp = comp.getEmployee();
            } catch (Exception e) {
                // If still lazy loading fails, use map
                System.err.println("Warning: Could not get employee from compensation, using map: " + e.getMessage());
            }
            
            // If employee is null or not loaded, try to get from map
            if (emp == null && employeeMap != null) {
                // Try to get employeeId from compensation's employee reference
                try {
                    UUID employeeId = comp.getEmployee() != null ? comp.getEmployee().getEmployeeID() : null;
                    if (employeeId != null) {
                        emp = employeeMap.get(employeeId);
                    }
                } catch (Exception e) {
                    // If we can't get employeeId, we'll leave emp as null
                }
            }
            
            // Extract employeeCode and employeeName
            if (emp != null) {
                employeeCode = emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "";
                employeeName = emp.getFullName() != null ? emp.getFullName() : "";
            }
            
            return String.format(
                "{\"compensationId\":\"%s\",\"employeeCode\":\"%s\",\"employeeName\":\"%s\"," +
                "\"compensationType\":\"%s\",\"baseMonthlySalary\":%s,\"hourlyRate\":%s," +
                "\"perShiftRate\":%s,\"overtimeRate\":%s,\"bonusAmount\":%s," +
                "\"commissionRate\":%s,\"allowanceAmount\":%s,\"deductionAmount\":%s}",
                comp.getCompensationId() != null ? escapeJson(comp.getCompensationId().toString()) : "",
                escapeJson(employeeCode),
                escapeJson(employeeName),
                comp.getCompensationType() != null ? escapeJson(comp.getCompensationType()) : "",
                formatNumber(comp.getBaseMonthlySalary()),
                formatNumber(comp.getHourlyRate()),
                formatNumber(comp.getPerShiftRate()),
                formatNumber(comp.getOvertimeRate()),
                formatNumber(comp.getBonusAmount()),
                formatNumber(comp.getCommissionRate()),
                formatNumber(comp.getAllowanceAmount()),
                formatNumber(comp.getDeductionAmount())
            );
        } catch (Exception e) {
            System.err.println("Error building compensation JSON: " + e.getMessage());
            e.printStackTrace();
            return "{}";
        }
    }

    private String formatNumber(BigDecimal value) {
        return value != null ? value.toString() : "null";
    }

    private void handleGetAllWithEmployees(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        try {
            System.out.println("=== handleGetAllWithEmployees called ===");

            // Get all employees
            List<Employee> employees = employeeService.getAllEmployees();
            System.out.println("Fetched " + employees.size() + " employees");

            // Create employee map for quick lookup
            Map<UUID, Employee> employeeMap = new HashMap<>();
            for (Employee emp : employees) {
                employeeMap.put(emp.getEmployeeID(), emp);
            }

            // Get all active compensations (with eager fetch)
            List<EmployeeCompensation> compensations = compensationService.getAllActiveCompensations();
            System.out.println("Fetched " + compensations.size() + " compensations");

            // Build JSON response
            StringBuilder json = new StringBuilder();
            json.append("{");

            // Employees array
            json.append("\"employees\":[");
            for (int i = 0; i < employees.size(); i++) {
                if (i > 0) json.append(",");
                Employee emp = employees.get(i);
                json.append(String.format(
                    "{\"employeeCode\":\"%s\",\"fullName\":\"%s\"}",
                    escapeJson(emp.getEmployeeCode()),
                    escapeJson(emp.getFullName())
                ));
            }
            json.append("],");

            // Compensations array
            json.append("\"compensations\":[");
            for (int i = 0; i < compensations.size(); i++) {
                if (i > 0) json.append(",");
                json.append(buildCompensationJson(compensations.get(i), employeeMap));
            }
            json.append("]");

            json.append("}");

            response.getWriter().write(json.toString());
            response.getWriter().flush();
        } catch (Exception e) {
            System.err.println("Error in handleGetAllWithEmployees: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"error\": \"" + escapeJson(e.getMessage()) + "\"}");
            response.getWriter().flush();
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
