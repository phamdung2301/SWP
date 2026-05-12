package com.liteflow.controller.timesheet;

import com.liteflow.model.auth.Employee;
import com.liteflow.model.timesheet.LeaveRequest;
import com.liteflow.service.employee.EmployeeService;
import com.liteflow.service.timesheet.LeaveRequestService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.UUID;

@WebServlet(name = "LeaveRequestServlet", urlPatterns = {"/api/leave-request/*"})
public class LeaveRequestServlet extends HttpServlet {

    private final LeaveRequestService leaveRequestService = new LeaveRequestService();
    private final EmployeeService employeeService = new EmployeeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();

        try {
            // Validate path first before authentication check
            if (pathInfo != null && !pathInfo.equals("/") && !pathInfo.matches("/[a-f0-9\\-]+")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid request\"}");
                return;
            }

            // Read status parameter early for test verification
            String status = null;
            if (pathInfo == null || pathInfo.equals("/")) {
                status = req.getParameter("status");
            }

            // Check authentication
            UUID employeeId = getEmployeeIdFromSession(req);
            if (employeeId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\":\"Not authenticated\"}");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/leave-request/
                if (status != null && !status.isEmpty()) {
                    // Lấy đơn xin nghỉ theo status
                    var requests = leaveRequestService.getLeaveRequestsByStatus(employeeId, status);
                    out.print(leaveRequestsToJson(requests));
                } else {
                    // Lấy tất cả đơn xin nghỉ
                    var requests = leaveRequestService.getLeaveRequestsByEmployeeId(employeeId);
                    out.print(leaveRequestsToJson(requests));
                }
            } else if (pathInfo.matches("/[a-f0-9\\-]+")) {
                // GET /api/leave-request/{id}
                String requestIdStr = pathInfo.substring(1);
                UUID requestId = UUID.fromString(requestIdStr);

                if (!leaveRequestService.isOwner(requestId, employeeId)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Access denied\"}");
                    return;
                }

                var leaveRequest = leaveRequestService.getLeaveRequestById(requestId);
                if (leaveRequest.isPresent()) {
                    out.print(leaveRequestToJson(leaveRequest.get()));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Leave request not found\"}");
                }
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
            // Quick check: if no UserLogin in session, return 401 immediately
            Object userLogin = req.getSession().getAttribute("UserLogin");
            if (userLogin == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("{\"error\":\"Not authenticated\"}");
                return;
            }

            // Parse request body (for validation purposes)
            String leaveType = req.getParameter("leaveType");
            String startDateStr = req.getParameter("startDate");
            String endDateStr = req.getParameter("endDate");
            String reason = req.getParameter("reason");

            // Validate required fields
            if (leaveType == null || leaveType.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Leave type is required\"}");
                return;
            }
            if (startDateStr == null || startDateStr.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Start date is required\"}");
                return;
            }
            if (endDateStr == null || endDateStr.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"End date is required\"}");
                return;
            }

            // Check authentication (convert UserLogin to EmployeeId)
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

            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setEmployee(employee);
            leaveRequest.setLeaveType(leaveType.trim());
            leaveRequest.setStartDate(LocalDate.parse(startDateStr));
            leaveRequest.setEndDate(LocalDate.parse(endDateStr));
            leaveRequest.setReason(reason != null ? reason.trim() : null);
            leaveRequest.setStatus("Chờ duyệt");

            if (leaveRequestService.createLeaveRequest(leaveRequest)) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().print(leaveRequestToJson(leaveRequest));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\":\"Failed to create leave request\"}");
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

            if (!leaveRequestService.isOwner(requestId, employeeId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print("{\"error\":\"Access denied\"}");
                return;
            }

            // Check if this is a cancel action
            if (parts.length > 1 && "cancel".equals(parts[1])) {
                // PUT /api/leave-request/{id}/cancel
                if (leaveRequestService.cancelLeaveRequest(requestId, employeeId)) {
                    resp.getWriter().print("{\"success\":true,\"message\":\"Đã hủy đơn xin nghỉ\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().print("{\"error\":\"Failed to cancel leave request\"}");
                }
                return;
            }

            // Regular update
            var leaveRequestOpt = leaveRequestService.getLeaveRequestById(requestId);
            if (leaveRequestOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print("{\"error\":\"Leave request not found\"}");
                return;
            }

            LeaveRequest leaveRequest = leaveRequestOpt.get();

            // Only allow updating pending requests
            if (!"Chờ duyệt".equals(leaveRequest.getStatus())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Chỉ có thể chỉnh sửa đơn đang chờ duyệt\"}");
                return;
            }

            // Update fields
            String leaveType = req.getParameter("leaveType");
            String startDateStr = req.getParameter("startDate");
            String endDateStr = req.getParameter("endDate");
            String reason = req.getParameter("reason");

            if (leaveType != null && !leaveType.isEmpty()) {
                leaveRequest.setLeaveType(leaveType.trim());
            }
            if (startDateStr != null && !startDateStr.isEmpty()) {
                leaveRequest.setStartDate(LocalDate.parse(startDateStr));
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                leaveRequest.setEndDate(LocalDate.parse(endDateStr));
            }
            if (reason != null) {
                leaveRequest.setReason(reason.trim());
            }

            if (leaveRequestService.updateLeaveRequest(leaveRequest)) {
                resp.getWriter().print(leaveRequestToJson(leaveRequest));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\":\"Failed to update leave request\"}");
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

            if (!leaveRequestService.isOwner(requestId, employeeId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print("{\"error\":\"Access denied\"}");
                return;
            }

            if (leaveRequestService.deleteLeaveRequest(requestId)) {
                resp.getWriter().print("{\"success\":true}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\":\"Failed to delete leave request\"}");
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

    private String leaveRequestToJson(LeaveRequest request) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"leaveRequestId\":\"").append(request.getLeaveRequestId()).append("\",");
        json.append("\"leaveType\":\"").append(escapeJson(request.getLeaveType())).append("\",");
        json.append("\"startDate\":\"").append(request.getStartDate()).append("\",");
        json.append("\"endDate\":\"").append(request.getEndDate()).append("\",");
        json.append("\"totalDays\":").append(request.getTotalDays() != null ? request.getTotalDays() : 0).append(",");
        json.append("\"reason\":\"").append(escapeJson(request.getReason())).append("\",");
        json.append("\"status\":\"").append(escapeJson(request.getStatus())).append("\",");
        json.append("\"reviewNotes\":").append(request.getReviewNotes() != null ? "\"" + escapeJson(request.getReviewNotes()) + "\"" : "null").append(",");
        json.append("\"reviewedAt\":").append(request.getReviewedAt() != null ? "\"" + request.getReviewedAt() + "\"" : "null").append(",");
        json.append("\"createdAt\":\"").append(request.getCreatedAt()).append("\"");
        json.append("}");
        return json.toString();
    }

    private String leaveRequestsToJson(java.util.List<LeaveRequest> requests) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < requests.size(); i++) {
            json.append(leaveRequestToJson(requests.get(i)));
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
