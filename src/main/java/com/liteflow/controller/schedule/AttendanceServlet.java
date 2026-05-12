package com.liteflow.controller.schedule;

import com.liteflow.model.auth.EmployeeShift;
import com.liteflow.model.timesheet.EmployeeShiftTimesheet;
import com.liteflow.model.timesheet.EmployeeAttendance;
import com.liteflow.service.employee.EmployeeService;
import com.liteflow.service.timesheet.ScheduleService;
import com.liteflow.service.timesheet.TimesheetService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet(name = "AttendanceServlet", urlPatterns = {"/attendance"})
public class AttendanceServlet extends HttpServlet {

    private final ScheduleService scheduleService = new ScheduleService();
    private final TimesheetService timesheetService = new TimesheetService();
    private final EmployeeService employeeService = new EmployeeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String weekStartParam = req.getParameter("weekStart");
        LocalDate now = LocalDate.now();
        LocalDate weekStart;
        if (weekStartParam != null && !weekStartParam.isBlank()) {
            weekStart = LocalDate.parse(weekStartParam);
        } else {
            DayOfWeek dow = now.getDayOfWeek();
            int shift = (dow.getValue() + 7 - DayOfWeek.MONDAY.getValue()) % 7;
            weekStart = now.minusDays(shift);
        }

        var templates = scheduleService.getActiveTemplates();
        List<EmployeeShift> shifts = scheduleService.getShiftsForWeek(weekStart);
        List<EmployeeShiftTimesheet> timesheets = timesheetService.getTimesheetsForWeek(weekStart);
        var attendanceList = timesheetService.getAttendanceForWeek(weekStart);

        // Optional filters
        String employeeCodeFilter = req.getParameter("employeeCode");
        if (employeeCodeFilter != null && !employeeCodeFilter.isBlank()) {
            String ec = employeeCodeFilter.trim();
            shifts.removeIf(s -> s.getEmployee() == null || s.getEmployee().getEmployeeCode() == null || !s.getEmployee().getEmployeeCode().equals(ec));
            timesheets.removeIf(t -> t.getEmployee() == null || t.getEmployee().getEmployeeCode() == null || !t.getEmployee().getEmployeeCode().equals(ec));
        }

        // Build map: day -> templateName -> list of attendance entries (combine shift and timesheet info)
        DateTimeFormatter dmy = DateTimeFormatter.ofPattern("dd/MM", java.util.Locale.forLanguageTag("vi"));
        DateTimeFormatter dmyFull = DateTimeFormatter.ofPattern("dd/MM/yyyy", java.util.Locale.forLanguageTag("vi"));

        // Index timesheets by date and time window for quick lookup
        Map<LocalDate, List<EmployeeShiftTimesheet>> tsByDate = new HashMap<>();
        for (var ts : timesheets) {
            tsByDate.computeIfAbsent(ts.getWorkDate(), k -> new ArrayList<>()).add(ts);
        }

        // Index attendance by date and employee for badge/status
        Map<LocalDate, Map<java.util.UUID, String>> attByDateEmp = new HashMap<>();
        Map<LocalDate, Map<java.util.UUID, java.time.LocalTime[]>> attTimes = new HashMap<>();
        Map<LocalDate, Map<java.util.UUID, EmployeeAttendance>> attFullData = new HashMap<>();
        for (var att : attendanceList) {
            if (att.getEmployee() == null) continue;
            LocalDate d = att.getWorkDate();
            attByDateEmp.computeIfAbsent(d, k -> new HashMap<>()).put(att.getEmployee().getEmployeeID(), att.getStatus());
            var arr = new java.time.LocalTime[]{att.getCheckInTime(), att.getCheckOutTime()};
            attTimes.computeIfAbsent(d, k -> new HashMap<>()).put(att.getEmployee().getEmployeeID(), arr);
            attFullData.computeIfAbsent(d, k -> new HashMap<>()).put(att.getEmployee().getEmployeeID(), att);
        }

        List<Map<String, Object>> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            Map<String, Object> dayMap = new HashMap<>();
            String label;
            switch (i) {
                case 0: label = "Thứ 2"; break;
                case 1: label = "Thứ 3"; break;
                case 2: label = "Thứ 4"; break;
                case 3: label = "Thứ 5"; break;
                case 4: label = "Thứ 6"; break;
                case 5: label = "Thứ 7"; break;
                default: label = "Chủ nhật";
            }
            dayMap.put("label", label);
            dayMap.put("dateStr", day.format(dmy));

            List<Map<String, Object>> rows = new ArrayList<>();
            templates.forEach(t -> {
                Map<String, Object> row = new HashMap<>();
                row.put("templateName", t.getName());
                row.put("startTime", t.getStartTime().toString().substring(0, 5));
                row.put("endTime", t.getEndTime().toString().substring(0, 5));

                List<Map<String, Object>> items = new ArrayList<>();

                // Shifts on this day matching this template time range
                for (EmployeeShift s : shifts) {
                    if (s.getStartAt() == null || s.getEndAt() == null) continue;
                    if (!s.getStartAt().toLocalDate().equals(day)) continue;
                    String sStart = s.getStartAt().toLocalTime().toString().substring(0, 5);
                    String sEnd = s.getEndAt().toLocalTime().toString().substring(0, 5);
                    if (!sStart.equals(row.get("startTime")) || !sEnd.equals(row.get("endTime"))) continue;

                    Map<String, Object> map = new HashMap<>();
                    map.put("employee", s.getEmployee() != null ? s.getEmployee().getFullName() : "");
                    map.put("employeeCode", s.getEmployee() != null ? s.getEmployee().getEmployeeCode() : "");
                    map.put("shiftId", s.getShiftID());
                    map.put("title", s.getTitle());
                    map.put("location", s.getLocation());

                    // Find a timesheet record for this employee and day
                    String status = "Pending";
                    String attendanceStatus = null;
                    java.time.LocalTime attIn = null, attOut = null;
                    EmployeeAttendance fullAtt = null;
                    if (attByDateEmp.containsKey(day) && s.getEmployee() != null) {
                        attendanceStatus = attByDateEmp.get(day).get(s.getEmployee().getEmployeeID());
                        var arr = attTimes.getOrDefault(day, java.util.Collections.emptyMap()).get(s.getEmployee().getEmployeeID());
                        if (arr != null) { attIn = arr[0]; attOut = arr[1]; }
                        fullAtt = attFullData.getOrDefault(day, java.util.Collections.emptyMap()).get(s.getEmployee().getEmployeeID());
                    }
                    if (tsByDate.containsKey(day)) {
                        for (EmployeeShiftTimesheet ts : tsByDate.get(day)) {
                            if (ts.getEmployee() != null && s.getEmployee() != null
                                && ts.getEmployee().getEmployeeID().equals(s.getEmployee().getEmployeeID())) {
                                status = ts.getStatus();
                                map.put("checkInAt", ts.getCheckInAt());
                                map.put("checkOutAt", ts.getCheckOutAt());
                                map.put("hoursWorked", ts.getHoursWorked());
                                map.put("source", ts.getSource());
                                break;
                            }
                        }
                    }
                    // If no timesheet but attendance exists, surface those times
                    if (!map.containsKey("checkInAt") && attIn != null) {
                        map.put("checkInAt", attIn.toString().substring(0,5));
                    }
                    if (!map.containsKey("checkOutAt") && attOut != null) {
                        map.put("checkOutAt", attOut.toString().substring(0,5));
                    }
                    map.put("status", status);
                    if (attendanceStatus != null) {
                        map.put("attendanceStatus", attendanceStatus);
                    }
                    
                    // Add attendance notes and status flags
                    if (fullAtt != null) {
                        if (fullAtt.getNotes() != null && !fullAtt.getNotes().isEmpty()) {
                            map.put("notes", fullAtt.getNotes());
                        }
                        if (fullAtt.getIsLate() != null) map.put("isLate", fullAtt.getIsLate());
                        if (fullAtt.getIsOvertime() != null) map.put("isOvertime", fullAtt.getIsOvertime());
                        if (fullAtt.getIsEarlyLeave() != null) map.put("isEarlyLeave", fullAtt.getIsEarlyLeave());
                    }

                    items.add(map);
                }

                row.put("items", items);
                rows.add(row);
            });

            dayMap.put("rows", rows);
            weekDays.add(dayMap);
        }

        LocalDate weekEnd = weekStart.plusDays(6);
        String weekLabel = "Tuần " + weekStart.format(dmy) + " - " + weekEnd.format(dmyFull);
        var wf = java.time.temporal.WeekFields.of(DayOfWeek.MONDAY, 1);
        int weekOfMonth = weekStart.get(wf.weekOfMonth());
        String monthShort = String.format("%02d", weekStart.getMonthValue());
        String controlLabel = "Tuần " + weekOfMonth + " - Th. " + monthShort + " " + weekStart.getYear();

        String prevWeekStart = weekStart.minusDays(7).toString();
        String nextWeekStart = weekStart.plusDays(7).toString();

        StringBuilder fq = new StringBuilder();
        if (employeeCodeFilter != null && !employeeCodeFilter.isBlank()) {
            fq.append("&employeeCode=").append(URLEncoder.encode(employeeCodeFilter, StandardCharsets.UTF_8));
        }
        String filterQuery = fq.toString();

        req.setAttribute("weekLabel", weekLabel);
        req.setAttribute("controlLabel", controlLabel);
        req.setAttribute("weekDays", weekDays);
        req.setAttribute("prevWeekStart", prevWeekStart);
        req.setAttribute("nextWeekStart", nextWeekStart);
        req.setAttribute("templates", templates);
        req.setAttribute("currentWeekStart", weekStart.toString());
        req.setAttribute("employees", employeeService.getAllEmployees());
        req.setAttribute("selectedEmployeeCode", employeeCodeFilter);
        req.setAttribute("filterQuery", filterQuery);
        req.getRequestDispatcher("/attendance.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action == null || action.isBlank()) action = "save";

        String weekStartParam = req.getParameter("weekStart");
        if (weekStartParam == null || weekStartParam.isBlank()) {
            weekStartParam = java.time.LocalDate.now().toString();
        }

        if ("save".equals(action)) {
            String employeeCode = req.getParameter("employeeCode");
            String dateStr = req.getParameter("date"); // yyyy-MM-dd
            String status = req.getParameter("status"); // work | leave_paid | leave_unpaid
            String checkIn = req.getParameter("checkIn"); // HH:mm or empty
            String checkOut = req.getParameter("checkOut"); // HH:mm or empty
            String notes = req.getParameter("notes");
            
            // Parse checkbox status flags
            Boolean inLate = parseBoolean(req.getParameter("inLate"));
            Boolean inOver = parseBoolean(req.getParameter("inOver"));
            Boolean outEarly = parseBoolean(req.getParameter("outEarly"));
            Boolean outOver = parseBoolean(req.getParameter("outOver"));

            java.time.LocalDate workDate = null;
            try { if (dateStr != null && !dateStr.isBlank()) workDate = java.time.LocalDate.parse(dateStr); } catch (Exception ignored) {}

            if (employeeCode != null && workDate != null && status != null && !status.isBlank()) {
                timesheetService.upsertAttendance(employeeCode, workDate, status, checkIn, checkOut, notes,
                        inLate, inOver, outEarly, outOver);
            }

            // redirect back to same week (preserve filter if any)
            StringBuilder redirect = new StringBuilder(req.getContextPath()).append("/attendance?weekStart=").append(weekStartParam);
            String ec = req.getParameter("redirectEmployeeCode");
            if (ec == null || ec.isBlank()) ec = employeeCode;
            if (ec != null && !ec.isBlank()) redirect.append("&employeeCode=").append(java.net.URLEncoder.encode(ec, java.nio.charset.StandardCharsets.UTF_8));
            resp.sendRedirect(redirect.toString());
            return;
        }

        // default: go back
        resp.sendRedirect(req.getContextPath() + "/attendance?weekStart=" + weekStartParam);
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) return null;
        return "true".equalsIgnoreCase(value.trim());
    }
}


