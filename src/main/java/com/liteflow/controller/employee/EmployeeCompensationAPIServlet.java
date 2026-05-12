package com.liteflow.controller.employee;

import com.liteflow.model.payroll.EmployeeCompensation;
import com.liteflow.service.employee.CompensationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * API Servlet for Employee Compensation endpoints
 */
@WebServlet(name = "EmployeeCompensationAPIServlet", urlPatterns = {"/api/employee/compensation/*"})
public class EmployeeCompensationAPIServlet extends HttpServlet {

    private final CompensationService compensationService;

    public EmployeeCompensationAPIServlet() {
        this.compensationService = new CompensationService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Missing path\"}");
            return;
        }

        try {
            if (pathInfo.startsWith("/")) {
                String employeeIdStr = pathInfo.substring(1);
                
                if (pathInfo.equals("/" + employeeIdStr) || pathInfo.matches("/[^/]+$")) {
                    // GET /api/employee/compensation/{employeeId}
                    handleGetCompensation(employeeIdStr, resp);
                } else if (pathInfo.matches("/[^/]+/history")) {
                    // GET /api/employee/compensation/{employeeId}/history
                    employeeIdStr = pathInfo.substring(1, pathInfo.length() - 8); // Remove /history
                    handleGetCompensationHistory(employeeIdStr, resp);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().print("{\"error\":\"Endpoint not found\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Invalid path\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");

        try {
            // POST /api/employee/compensation - Create or update compensation
            handleCreateOrUpdateCompensation(req, resp);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private void handleGetCompensation(String employeeIdStr, HttpServletResponse resp)
            throws IOException {
        try {
            UUID employeeId = UUID.fromString(employeeIdStr);
            EmployeeCompensation compensation = compensationService.getActiveCompensationByEmployeeId(employeeId);

            JSONObject json = new JSONObject();
            if (compensation != null) {
                json.put("success", true);
                json.put("data", convertCompensationToJSON(compensation));
            } else {
                json.put("success", false);
                json.put("error", "No active compensation found");
            }
            resp.getWriter().print(json.toString());
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Invalid employee ID format\"}");
        }
    }

    private void handleGetCompensationHistory(String employeeIdStr, HttpServletResponse resp)
            throws IOException {
        try {
            UUID employeeId = UUID.fromString(employeeIdStr);
            List<EmployeeCompensation> history = compensationService.getCompensationHistoryByEmployeeId(employeeId);

            JSONObject json = new JSONObject();
            json.put("success", true);
            JSONArray historyArray = new JSONArray();
            for (EmployeeCompensation comp : history) {
                historyArray.put(convertCompensationToJSON(comp));
            }
            json.put("data", historyArray);
            resp.getWriter().print(json.toString());
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Invalid employee ID format\"}");
        }
    }

    private void handleCreateOrUpdateCompensation(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String employeeCode = req.getParameter("employeeCode");
        String compensationType = req.getParameter("compensationType");
        String baseMonthlySalaryStr = req.getParameter("baseMonthlySalary");
        String hourlyRateStr = req.getParameter("hourlyRate");
        String perShiftRateStr = req.getParameter("perShiftRate");
        String overtimeRateStr = req.getParameter("overtimeRate");
        String bonusAmountStr = req.getParameter("bonusAmount");
        String commissionRateStr = req.getParameter("commissionRate");
        String allowanceAmountStr = req.getParameter("allowanceAmount");
        String deductionAmountStr = req.getParameter("deductionAmount");

        if (employeeCode == null || compensationType == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Missing required fields\"}");
            return;
        }

        java.math.BigDecimal baseMonthlySalary = parseBigDecimal(baseMonthlySalaryStr);
        java.math.BigDecimal hourlyRate = parseBigDecimal(hourlyRateStr);
        java.math.BigDecimal perShiftRate = parseBigDecimal(perShiftRateStr);
        java.math.BigDecimal overtimeRate = parseBigDecimal(overtimeRateStr);
        java.math.BigDecimal bonusAmount = parseBigDecimal(bonusAmountStr);
        java.math.BigDecimal commissionRate = parseBigDecimal(commissionRateStr);
        java.math.BigDecimal allowanceAmount = parseBigDecimal(allowanceAmountStr);
        java.math.BigDecimal deductionAmount = parseBigDecimal(deductionAmountStr);

        boolean success = compensationService.saveCompensation(
            employeeCode, compensationType,
            baseMonthlySalary, hourlyRate, perShiftRate,
            overtimeRate, bonusAmount, commissionRate,
            allowanceAmount, deductionAmount
        );

        JSONObject json = new JSONObject();
        if (success) {
            json.put("success", true);
            json.put("message", "Compensation saved successfully");
        } else {
            json.put("success", false);
            json.put("error", "Failed to save compensation");
        }
        resp.getWriter().print(json.toString());
    }

    private java.math.BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new java.math.BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private JSONObject convertCompensationToJSON(EmployeeCompensation comp) {
        JSONObject json = new JSONObject();
        json.put("compensationId", comp.getCompensationId().toString());
        json.put("employeeId", comp.getEmployee().getEmployeeID().toString());
        json.put("employeeCode", comp.getEmployee().getEmployeeCode());
        json.put("employeeName", comp.getEmployee().getFullName());
        json.put("compensationType", comp.getCompensationType());
        json.put("baseMonthlySalary", comp.getBaseMonthlySalary());
        json.put("hourlyRate", comp.getHourlyRate());
        json.put("perShiftRate", comp.getPerShiftRate());
        json.put("overtimeRate", comp.getOvertimeRate());
        json.put("bonusAmount", comp.getBonusAmount());
        json.put("commissionRate", comp.getCommissionRate());
        json.put("allowanceAmount", comp.getAllowanceAmount());
        json.put("deductionAmount", comp.getDeductionAmount());
        json.put("effectiveFrom", comp.getEffectiveFrom() != null ? comp.getEffectiveFrom().toString() : null);
        json.put("effectiveTo", comp.getEffectiveTo() != null ? comp.getEffectiveTo().toString() : null);
        json.put("isActive", comp.getIsActive());
        json.put("notes", comp.getNotes());
        return json;
    }
}

