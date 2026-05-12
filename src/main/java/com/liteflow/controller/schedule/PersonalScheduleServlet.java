package com.liteflow.controller.schedule;

import com.liteflow.model.auth.Employee;
import com.liteflow.model.timesheet.PersonalSchedule;
import com.liteflow.service.employee.EmployeeService;
import com.liteflow.service.timesheet.PersonalScheduleService;
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

@WebServlet(name = "PersonalScheduleServlet", urlPatterns = {"/api/personal-schedule/*"})
public class PersonalScheduleServlet extends HttpServlet {

    private final PersonalScheduleService personalScheduleService = new PersonalScheduleService();
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
                // GET /api/personal-schedule/
                String dateParam = req.getParameter("date");
                if (dateParam != null && !dateParam.isEmpty()) {
                    // Lấy lịch theo ngày
                    LocalDate date = LocalDate.parse(dateParam);
                    var schedules = personalScheduleService.getSchedulesByDate(employeeId, date);
                    out.print(schedulesToJson(schedules));
                } else {
                    // Lấy tất cả lịch
                    var schedules = personalScheduleService.getSchedulesByEmployeeId(employeeId);
                    out.print(schedulesToJson(schedules));
                }
            } else if (pathInfo.matches("/[a-f0-9\\-]+")) {
                // GET /api/personal-schedule/{id}
                String scheduleIdStr = pathInfo.substring(1);
                UUID scheduleId = UUID.fromString(scheduleIdStr);
                
                if (!personalScheduleService.isOwner(scheduleId, employeeId)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Access denied\"}");
                    return;
                }

                var schedule = personalScheduleService.getScheduleById(scheduleId);
                if (schedule.isPresent()) {
                    out.print(scheduleToJson(schedule.get()));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Schedule not found\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid request\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
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

            // employeeId is already EmployeeID from getEmployeeIdFromSession
            Employee employee = employeeService.getEmployeeById(employeeId).orElse(null);
            if (employee == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Employee not found\"}");
                return;
            }

            // Parse request body
            String title = req.getParameter("title");
            String description = req.getParameter("description");
            String startDateStr = req.getParameter("startDate");
            String startTimeStr = req.getParameter("startTime");
            String endTimeStr = req.getParameter("endTime");
            String priority = req.getParameter("priority");
            
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Title is required\"}");
                return;
            }
            if (startDateStr == null || startDateStr.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\":\"Start date is required\"}");
                return;
            }

            PersonalSchedule schedule = new PersonalSchedule();
            schedule.setEmployee(employee);
            schedule.setTitle(title.trim());
            schedule.setDescription(description != null ? description.trim() : null);
            schedule.setStartDate(LocalDate.parse(startDateStr));
            
            if (startTimeStr != null && !startTimeStr.trim().isEmpty()) {
                schedule.setStartTime(LocalTime.parse(startTimeStr));
            }
            if (endTimeStr != null && !endTimeStr.trim().isEmpty()) {
                schedule.setEndTime(LocalTime.parse(endTimeStr));
            }
            schedule.setPriority(priority != null && !priority.trim().isEmpty() ? priority : "Medium");
            schedule.setStatus("Pending");

            if (personalScheduleService.createSchedule(schedule)) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().print(scheduleToJson(schedule));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\":\"Failed to create schedule\"}");
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Invalid date/time format: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
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

            String scheduleIdStr = pathInfo.substring(1);
            UUID scheduleId = UUID.fromString(scheduleIdStr);

            if (!personalScheduleService.isOwner(scheduleId, employeeId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print("{\"error\":\"Access denied\"}");
                return;
            }

            var scheduleOpt = personalScheduleService.getScheduleById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print("{\"error\":\"Schedule not found\"}");
                return;
            }

            PersonalSchedule schedule = scheduleOpt.get();
            
            // Update fields
            String title = req.getParameter("title");
            String description = req.getParameter("description");
            String startDateStr = req.getParameter("startDate");
            String startTimeStr = req.getParameter("startTime");
            String endTimeStr = req.getParameter("endTime");
            String priority = req.getParameter("priority");
            String status = req.getParameter("status");

            if (title != null) schedule.setTitle(title);
            if (description != null) schedule.setDescription(description);
            if (startDateStr != null) schedule.setStartDate(LocalDate.parse(startDateStr));
            if (startTimeStr != null && !startTimeStr.isEmpty()) {
                schedule.setStartTime(LocalTime.parse(startTimeStr));
            }
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                schedule.setEndTime(LocalTime.parse(endTimeStr));
            }
            if (priority != null) schedule.setPriority(priority);
            if (status != null) schedule.setStatus(status);

            if (personalScheduleService.updateSchedule(schedule)) {
                resp.getWriter().print(scheduleToJson(schedule));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\":\"Failed to update schedule\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
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

            String scheduleIdStr = pathInfo.substring(1);
            UUID scheduleId = UUID.fromString(scheduleIdStr);

            if (!personalScheduleService.isOwner(scheduleId, employeeId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print("{\"error\":\"Access denied\"}");
                return;
            }

            if (personalScheduleService.deleteSchedule(scheduleId)) {
                resp.getWriter().print("{\"success\":true}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\":\"Failed to delete schedule\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
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

    private String scheduleToJson(PersonalSchedule schedule) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"scheduleId\":\"").append(schedule.getScheduleId()).append("\",");
        json.append("\"title\":\"").append(escapeJson(schedule.getTitle())).append("\",");
        json.append("\"description\":\"").append(escapeJson(schedule.getDescription())).append("\",");
        json.append("\"startDate\":\"").append(schedule.getStartDate()).append("\",");
        json.append("\"startTime\":").append(schedule.getStartTime() != null ? "\"" + schedule.getStartTime() + "\"" : "null").append(",");
        json.append("\"endTime\":").append(schedule.getEndTime() != null ? "\"" + schedule.getEndTime() + "\"" : "null").append(",");
        json.append("\"priority\":\"").append(schedule.getPriority()).append("\",");
        json.append("\"status\":\"").append(schedule.getStatus()).append("\"");
        json.append("}");
        return json.toString();
    }

    private String schedulesToJson(java.util.List<PersonalSchedule> schedules) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < schedules.size(); i++) {
            json.append(scheduleToJson(schedules.get(i)));
            if (i < schedules.size() - 1) {
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

