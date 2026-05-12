package com.liteflow.controller.payroll;

import com.liteflow.service.payroll.PayrollService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Servlet for Payroll management API
 */
@WebServlet(name = "PayrollServlet", urlPatterns = {"/api/payroll/*"})
public class PayrollServlet extends HttpServlet {

    private final PayrollService payrollService;

    public PayrollServlet() {
        this.payrollService = new PayrollService();
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
            resp.getWriter().flush();
            return;
        }

        try {
            if (pathInfo.equals("/list") || pathInfo.equals("/")) {
                // GET /api/payroll/list?month=X&year=Y
                handleGetPayrollList(req, resp);
            } else if (pathInfo.startsWith("/employee/")) {
                // GET /api/payroll/employee/{employeeId}?month=X&year=Y
                String employeeIdStr = pathInfo.substring(10); // Remove "/employee/"
                handleGetEmployeePayroll(employeeIdStr, req, resp);
            } else if (pathInfo.equals("/generate")) {
                // GET /api/payroll/generate?month=X&year=Y
                handleGeneratePayroll(req, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print("{\"error\":\"Endpoint not found\"}");
                resp.getWriter().flush();
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            resp.getWriter().flush();
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        String requestURI = req.getRequestURI();
        String servletPath = req.getServletPath();
        
        // Normalize pathInfo - remove leading/trailing slashes and query strings
        if (pathInfo != null) {
            pathInfo = pathInfo.trim();
            // Remove query string if present
            int queryIndex = pathInfo.indexOf('?');
            if (queryIndex > 0) {
                pathInfo = pathInfo.substring(0, queryIndex);
            }
        }
        
        // Debug logging
        System.out.println("PayrollServlet POST - pathInfo: " + pathInfo);
        System.out.println("PayrollServlet POST - requestURI: " + requestURI);
        System.out.println("PayrollServlet POST - servletPath: " + servletPath);
        
        // If pathInfo is null or empty, try to extract from requestURI
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            String extractedPath = null;
            if (requestURI != null) {
                // Extract path after /api/payroll/
                int payrollIndex = requestURI.indexOf("/api/payroll/");
                if (payrollIndex >= 0) {
                    String afterPayroll = requestURI.substring(payrollIndex + "/api/payroll/".length());
                    int queryIndex = afterPayroll.indexOf('?');
                    if (queryIndex > 0) {
                        afterPayroll = afterPayroll.substring(0, queryIndex);
                    }
                    if (!afterPayroll.isEmpty()) {
                        extractedPath = "/" + afterPayroll;
                    }
                }
            }
            
            if (extractedPath != null && !extractedPath.isEmpty()) {
                pathInfo = extractedPath;
                System.out.println("PayrollServlet POST - Extracted pathInfo from URI: " + pathInfo);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JSONObject errorJson = new JSONObject();
                errorJson.put("error", "Invalid endpoint - pathInfo is null or empty");
                errorJson.put("pathInfo", pathInfo != null ? pathInfo : "null");
                errorJson.put("requestURI", requestURI);
                errorJson.put("servletPath", servletPath);
                resp.getWriter().print(errorJson.toString());
                resp.getWriter().flush();
                return;
            }
        }

        try {
            if (pathInfo.equals("/mark-paid") || pathInfo.contains("mark-paid")) {
                // POST /api/payroll/mark-paid
                handleMarkAsPaid(req, resp);
            } else if (pathInfo.equals("/generate") || pathInfo.contains("generate")) {
                // POST /api/payroll/generate?month=X&year=Y
                handleGeneratePayroll(req, resp);
            } else if (pathInfo.equals("/recalculate") || pathInfo.contains("recalculate")) {
                // POST /api/payroll/recalculate?month=X&year=Y&employeeId=UUID (optional)
                handleRecalculatePayroll(req, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JSONObject errorJson = new JSONObject();
                errorJson.put("error", "Invalid endpoint");
                errorJson.put("pathInfo", pathInfo);
                errorJson.put("requestURI", requestURI);
                resp.getWriter().print(errorJson.toString());
                resp.getWriter().flush();
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", e.getMessage());
            errorJson.put("pathInfo", pathInfo);
            resp.getWriter().print(errorJson.toString());
            resp.getWriter().flush();
            e.printStackTrace();
        }
    }

    private void handleGetPayrollList(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
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

        List<PayrollService.PayrollEntryDTO> payrollList = payrollService.getPayrollForMonth(month, year);

        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("month", month);
        json.put("year", year);
        
        JSONArray entriesArray = new JSONArray();
        BigDecimal totalSalary = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO;
        int paidCount = 0;
        int unpaidCount = 0;

        for (PayrollService.PayrollEntryDTO dto : payrollList) {
            JSONObject entryJson = convertDTOToJSON(dto);
            entriesArray.put(entryJson);
            
            totalSalary = totalSalary.add(dto.getTotalSalary());
            totalPaid = totalPaid.add(dto.getTotalPaid());
            totalRemaining = totalRemaining.add(dto.getTotalRemaining());
            
            if (dto.getIsPaid() != null && dto.getIsPaid()) {
                paidCount++;
            } else {
                unpaidCount++;
            }
        }

        json.put("entries", entriesArray);
        json.put("totalSalary", totalSalary.toPlainString());
        json.put("totalPaid", totalPaid.toPlainString());
        json.put("totalRemaining", totalRemaining.toPlainString());
        json.put("paidCount", paidCount);
        json.put("unpaidCount", unpaidCount);

        resp.getWriter().print(json.toString());
        resp.getWriter().flush();
    }

    private void handleGetEmployeePayroll(String employeeIdStr, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            UUID employeeId = UUID.fromString(employeeIdStr);
            
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

            PayrollService.PayrollEntryDTO dto = payrollService.getPayrollForEmployee(employeeId, month, year);

            JSONObject json = new JSONObject();
            if (dto != null) {
                json.put("success", true);
                json.put("data", convertDTOToJSON(dto));
            } else {
                json.put("success", false);
                json.put("error", "Payroll entry not found");
            }
            resp.getWriter().print(json.toString());
            resp.getWriter().flush();
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Invalid employee ID format\"}");
            resp.getWriter().flush();
        }
    }

    private void handleMarkAsPaid(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String payrollEntryIdStr = req.getParameter("payrollEntryId");
        
        if (payrollEntryIdStr == null || payrollEntryIdStr.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Missing payrollEntryId\"}");
            resp.getWriter().flush();
            return;
        }

        try {
            UUID payrollEntryId = UUID.fromString(payrollEntryIdStr);
            boolean success = payrollService.markAsPaid(payrollEntryId);

            JSONObject json = new JSONObject();
            if (success) {
                json.put("success", true);
                json.put("message", "Marked as paid successfully");
            } else {
                json.put("success", false);
                json.put("error", "Failed to mark as paid");
            }
            resp.getWriter().print(json.toString());
            resp.getWriter().flush();
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Invalid payroll entry ID format\"}");
            resp.getWriter().flush();
        }
    }

    private void handleGeneratePayroll(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
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

        try {
            int createdCount = payrollService.generatePayrollForMonth(month, year);
            
            JSONObject json = new JSONObject();
            
            if (createdCount > 0) {
                json.put("success", true);
                json.put("message", "Đã tạo bảng lương cho " + createdCount + " nhân viên");
            } else {
                json.put("success", true);
                json.put("message", "Không có nhân viên nào cần tạo bảng lương mới. Có thể tất cả nhân viên đã có bảng lương cho tháng này.");
                json.put("warning", true);
            }
            
            json.put("createdCount", createdCount);
            json.put("month", month);
            json.put("year", year);
            
            resp.getWriter().print(json.toString());
            resp.getWriter().flush();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", "Lỗi khi tạo bảng lương: " + e.getMessage());
            resp.getWriter().print(json.toString());
            resp.getWriter().flush();
            e.printStackTrace();
        }
    }

    private void handleRecalculatePayroll(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String monthStr = req.getParameter("month");
        String yearStr = req.getParameter("year");
        String employeeIdStr = req.getParameter("employeeId");

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

        try {
            JSONObject json = new JSONObject();
            
            if (employeeIdStr != null && !employeeIdStr.trim().isEmpty()) {
                // Recalculate for specific employee
                try {
                    UUID employeeId = UUID.fromString(employeeIdStr.trim());
                    boolean success = payrollService.recalculatePayrollEntry(employeeId, month, year);
                    
                    if (success) {
                        json.put("success", true);
                        json.put("message", "Đã cập nhật lại bảng lương cho nhân viên");
                    } else {
                        json.put("success", false);
                        json.put("error", "Không thể cập nhật bảng lương");
                    }
                } catch (IllegalArgumentException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.put("success", false);
                    json.put("error", "Invalid employee ID format");
                }
            } else {
                // Recalculate for all employees in the month
                int recalculatedCount = payrollService.recalculatePayrollForMonth(month, year);
                
                json.put("success", true);
                json.put("message", "Đã cập nhật lại bảng lương cho " + recalculatedCount + " nhân viên");
                json.put("recalculatedCount", recalculatedCount);
            }
            
            json.put("month", month);
            json.put("year", year);
            
            resp.getWriter().print(json.toString());
            resp.getWriter().flush();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", "Lỗi khi cập nhật bảng lương: " + e.getMessage());
            resp.getWriter().print(json.toString());
            resp.getWriter().flush();
            e.printStackTrace();
        }
    }

    private JSONObject convertDTOToJSON(PayrollService.PayrollEntryDTO dto) {
        JSONObject json = new JSONObject();
        json.put("payrollEntryId", dto.getPayrollEntryId().toString());
        json.put("employeeId", dto.getEmployeeId().toString());
        json.put("employeeCode", dto.getEmployeeCode());
        json.put("employeeName", dto.getEmployeeName());
        json.put("compensationType", dto.getCompensationType());
        json.put("totalSalary", dto.getTotalSalary().toPlainString());
        json.put("allowances", dto.getAllowances().toPlainString());
        json.put("bonuses", dto.getBonuses().toPlainString());
        json.put("deductions", dto.getDeductions().toPlainString());
        json.put("totalPaid", dto.getTotalPaid().toPlainString());
        json.put("totalRemaining", dto.getTotalRemaining().toPlainString());
        json.put("isPaid", dto.getIsPaid());
        return json;
    }
}

