package com.liteflow.controller.timesheet;

import com.liteflow.model.auth.Employee;
import com.liteflow.model.timesheet.EmployeeAttendance;
import com.liteflow.service.employee.EmployeeService;
import com.liteflow.service.timesheet.TimesheetService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@WebServlet(name = "TimesheetServlet", urlPatterns = {"/api/timesheet/*"})
public class TimesheetServlet extends HttpServlet {

    private final TimesheetService timesheetService = new TimesheetService();
    private final EmployeeService employeeService = new EmployeeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        
        try {
            UUID employeeId = getEmployeeIdFromSession(req);
            if (employeeId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("{\"error\":\"Not authenticated\"}");
                return;
            }

            // GET /api/timesheet/status - Lấy trạng thái chấm công hôm nay
            if ("/status".equals(pathInfo)) {
                handleGetStatus(employeeId, resp);
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            UUID employeeId = getEmployeeIdFromSession(req);
            if (employeeId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("{\"error\":\"Not authenticated\"}");
                return;
            }

            // POST /api/timesheet/clock-in - Chấm công vào
            if ("/clock-in".equals(pathInfo)) {
                handleClockIn(employeeId, resp);
            }
            // POST /api/timesheet/clock-out - Chấm công ra
            else if ("/clock-out".equals(pathInfo)) {
                handleClockOut(employeeId, resp);
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

    private void handleGetStatus(UUID employeeId, HttpServletResponse resp) throws IOException {
        EmployeeAttendance attendance = timesheetService.getTodayAttendance(employeeId);
        
        if (attendance == null) {
            // Chưa chấm công
            resp.getWriter().print("{\"hasClockedIn\":false,\"hasClockedOut\":false}");
        } else {
            boolean hasClockedIn = attendance.getCheckInTime() != null;
            boolean hasClockedOut = attendance.getCheckOutTime() != null;
            
            String checkInTime = hasClockedIn ? 
                attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null;
            String checkOutTime = hasClockedOut ? 
                attendance.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null;
            
            String json = String.format(
                "{\"hasClockedIn\":%b,\"hasClockedOut\":%b,\"checkInTime\":\"%s\",\"checkOutTime\":\"%s\"}",
                hasClockedIn, hasClockedOut, 
                checkInTime != null ? checkInTime : "", 
                checkOutTime != null ? checkOutTime : ""
            );
            
            resp.getWriter().print(json);
        }
    }

    private void handleClockIn(UUID employeeId, HttpServletResponse resp) throws IOException {
        EmployeeAttendance attendance = timesheetService.clockIn(employeeId);
        
        if (attendance == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"success\":false,\"error\":\"Không thể chấm công vào\"}");
            return;
        }
        
        String checkInTime = attendance.getCheckInTime() != null ?
            attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";
        
        String json = String.format(
            "{\"success\":true,\"message\":\"Chấm công vào thành công\",\"checkInTime\":\"%s\"}",
            checkInTime
        );
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().print(json);
    }

    private void handleClockOut(UUID employeeId, HttpServletResponse resp) throws IOException {
        EmployeeAttendance attendance = timesheetService.clockOut(employeeId);
        
        if (attendance == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"success\":false,\"error\":\"Không thể chấm công ra\"}");
            return;
        }
        
        String checkOutTime = attendance.getCheckOutTime() != null ?
            attendance.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";
        
        String json = String.format(
            "{\"success\":true,\"message\":\"Chấm công ra thành công\",\"checkOutTime\":\"%s\"}",
            checkOutTime
        );
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().print(json);
    }

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
}

