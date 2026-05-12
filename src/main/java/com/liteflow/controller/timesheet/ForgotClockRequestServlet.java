package com.liteflow.controller.timesheet;

import com.liteflow.model.auth.Employee;
import com.liteflow.model.timesheet.ForgotClockRequest;
import com.liteflow.service.employee.EmployeeService;
import com.liteflow.service.timesheet.ForgotClockRequestService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@WebServlet(name = "ForgotClockRequestServlet", urlPatterns = {"/api/forgot-clock/*"})
public class ForgotClockRequestServlet extends HttpServlet {

    private final ForgotClockRequestService forgotClockRequestService = new ForgotClockRequestService();
    private final EmployeeService employeeService = new EmployeeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();

        try {
            UUID employeeId = getEmployeeIdFromSession(req);
            if (employeeId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\":\"Not authenticated\"}");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/forgot-clock/
                String status = req.getParameter("status");
                if (status != null && !status.isEmpty()) {
                    // Lấy yêu cầu theo status
                    var requests = forgotClockRequestService.getForgotClockRequestsByStatus(employeeId, status);
                    out.print(forgotClockRequestsToJson(requests));
                } else {
                    // Lấy tất cả yêu cầu
                    var requests = forgotClockRequestService.getForgotClockRequestsByEmployeeId(employeeId);
                    out.print(forgotClockRequestsToJson(requests));
                }
            } else if (pathInfo.matches("/[a-f0-9\\-]+")) {
                // GET /api/forgot-clock/{id}
                String requestIdStr = pathInfo.substring(1);
                UUID requestId = UUID.fromString(requestIdStr);

                if (!forgotClockRequestService.isOwner(requestId, employeeId)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Access denied\"}");
                    return;
                }

                var request = forgotClockRequestService.getForgotClockRequestById(requestId);
                if (request.isPresent()) {
                    out.print(forgotClockRequestToJson(request.get()));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Forgot clock request not found\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid request\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            UUID employeeId = getEmployeeIdFromSession(req);
            if (employeeId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("{\"error\":\"Not authenticated\"}");
                return;
            }

            Employee employee = employeeService.getEmployeeById(employeeId).orElse(null);
            if (employee == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Employee not found\"}");
                return;
            }

            // Parse request body
            String forgotDateStr = req.getParameter("forgotDate");
            String forgotType = req.getParameter("forgotType");
            String forgotTimeStr = req.getParameter("forgotTime");
            String reason = req.getParameter("reason");

            // Validate required fields
            if (forgotDateStr == null || forgotDateStr.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Forgot date is required\"}");
                return;
            }
            if (forgotType == null || forgotType.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Forgot type is required\"}");
                return;
            }
            if (reason == null || reason.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Reason is required\"}");
                return;
            }

            ForgotClockRequest request = new ForgotClockRequest();
            request.setEmployee(employee);
            request.setForgotDate(LocalDate.parse(forgotDateStr));
            request.setForgotType(forgotType.trim().toUpperCase());
            
            if (forgotTimeStr != null && !forgotTimeStr.trim().isEmpty()) {
                request.setForgotTime(LocalTime.parse(forgotTimeStr));
            }
            
            request.setReason(reason.trim());
            request.setStatus("Chờ duyệt");

            if (forgotClockRequestService.createForgotClockRequest(request)) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().print(forgotClockRequestToJson(request));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\":\"Failed to create forgot clock request\"}");
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/[a-f0-9\\-]+(/cancel)?")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Invalid request\"}");
            return;
        }

        try {
            UUID employeeId = getEmployeeIdFromSession(req);
            if (employeeId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("{\"error\":\"Not authenticated\"}");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");
            String requestIdStr = parts[0];
            UUID requestId = UUID.fromString(requestIdStr);

            if (!forgotClockRequestService.isOwner(requestId, employeeId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print("{\"error\":\"Access denied\"}");
                return;
            }

            // Check if this is a cancel action
            if (parts.length > 1 && "cancel".equals(parts[1])) {
                // PUT /api/forgot-clock/{id}/cancel
                if (forgotClockRequestService.cancelForgotClockRequest(requestId, employeeId)) {
                    resp.getWriter().print("{\"success\":true,\"message\":\"Đã hủy yêu cầu\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().print("{\"error\":\"Failed to cancel request\"}");
                }
                return;
            }

            // Regular update
            var requestOpt = forgotClockRequestService.getForgotClockRequestById(requestId);
            if (requestOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print("{\"error\":\"Forgot clock request not found\"}");
                return;
            }

            ForgotClockRequest request = requestOpt.get();

            // Only allow updating pending requests
            if (!"Chờ duyệt".equals(request.getStatus())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Chỉ có thể chỉnh sửa yêu cầu đang chờ duyệt\"}");
                return;
            }

            // Update fields
            String forgotDateStr = req.getParameter("forgotDate");
            String forgotType = req.getParameter("forgotType");
            String forgotTimeStr = req.getParameter("forgotTime");
            String reason = req.getParameter("reason");

            if (forgotDateStr != null && !forgotDateStr.isEmpty()) {
                request.setForgotDate(LocalDate.parse(forgotDateStr));
            }
            if (forgotType != null && !forgotType.isEmpty()) {
                request.setForgotType(forgotType.trim().toUpperCase());
            }
            if (forgotTimeStr != null && !forgotTimeStr.isEmpty()) {
                request.setForgotTime(LocalTime.parse(forgotTimeStr));
            }
            if (reason != null) {
                request.setReason(reason.trim());
            }

            if (forgotClockRequestService.updateForgotClockRequest(request)) {
                resp.getWriter().print(forgotClockRequestToJson(request));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\":\"Failed to update request\"}");
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/[a-f0-9\\-]+")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Invalid request\"}");
            return;
        }

        try {
            UUID employeeId = getEmployeeIdFromSession(req);
            if (employeeId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("{\"error\":\"Not authenticated\"}");
                return;
            }

            String requestIdStr = pathInfo.substring(1);
            UUID requestId = UUID.fromString(requestIdStr);

            if (!forgotClockRequestService.isOwner(requestId, employeeId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print("{\"error\":\"Access denied\"}");
                return;
            }

            if (forgotClockRequestService.deleteForgotClockRequest(requestId)) {
                resp.getWriter().print("{\"success\":true}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\":\"Failed to delete request\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            e.printStackTrace();
        }
    }

    // ==============================
    // Helper methods
    // ==============================

    private UUID getEmployeeIdFromSession(HttpServletRequest req) {
        Object userLogin = req.getSession().getAttribute("UserLogin");
        if (userLogin == null) {
            return null;
        }

        UUID userId = null;
        if (userLogin instanceof UUID) {
            userId = (UUID) userLogin;
        } else if (userLogin instanceof String) {
            try {
                userId = UUID.fromString((String) userLogin);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        if (userId == null) {
            return null;
        }

        // Get employee ID from user ID
        return employeeService.getEmployeeByUserID(userId)
                .map(Employee::getEmployeeID)
                .orElse(null);
    }

    private String forgotClockRequestToJson(ForgotClockRequest request) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"forgotClockRequestId\":\"").append(request.getForgotClockRequestId()).append("\",");
        json.append("\"forgotDate\":\"").append(request.getForgotDate()).append("\",");
        json.append("\"forgotType\":\"").append(escapeJson(request.getForgotType())).append("\",");
        json.append("\"forgotTime\":").append(request.getForgotTime() != null ? "\"" + request.getForgotTime() + "\"" : "null").append(",");
        json.append("\"reason\":\"").append(escapeJson(request.getReason())).append("\",");
        json.append("\"status\":\"").append(escapeJson(request.getStatus())).append("\",");
        json.append("\"reviewNotes\":").append(request.getReviewNotes() != null ? "\"" + escapeJson(request.getReviewNotes()) + "\"" : "null").append(",");
        json.append("\"reviewedAt\":").append(request.getReviewedAt() != null ? "\"" + request.getReviewedAt() + "\"" : "null").append(",");
        json.append("\"createdAt\":\"").append(request.getCreatedAt()).append("\"");
        json.append("}");
        return json.toString();
    }

    private String forgotClockRequestsToJson(java.util.List<ForgotClockRequest> requests) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < requests.size(); i++) {
            json.append(forgotClockRequestToJson(requests.get(i)));
            if (i < requests.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
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

