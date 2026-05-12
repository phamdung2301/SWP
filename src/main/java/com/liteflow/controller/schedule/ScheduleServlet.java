package com.liteflow.controller.schedule;

import com.liteflow.model.auth.EmployeeShift;
import com.liteflow.service.employee.EmployeeService;
import com.liteflow.service.timesheet.ScheduleService;
import com.liteflow.service.auth.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

// @WebServlet annotation removed - using web.xml mapping
public class ScheduleServlet extends HttpServlet {

    private final ScheduleService scheduleService = new ScheduleService();
    private final EmployeeService employeeService = new EmployeeService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String weekStartParam = req.getParameter("weekStart");
        LocalDate now = LocalDate.now();
        LocalDate weekStart;
        if (weekStartParam != null && !weekStartParam.isBlank()) {
            weekStart = LocalDate.parse(weekStartParam);
        } else {
            // Monday as the first day
            DayOfWeek dow = now.getDayOfWeek();
            int shift = (dow.getValue() + 7 - DayOfWeek.MONDAY.getValue()) % 7;
            weekStart = now.minusDays(shift);
        }

        List<EmployeeShift> shifts = scheduleService.getShiftsForWeek(weekStart);
        var templates = scheduleService.getActiveTemplates();
        
        System.out.println("Total shifts before filter: " + shifts.size());

        // Optional filters
        String employeeCodeFilter = req.getParameter("employeeCode");
        String templateNameFilter = req.getParameter("templateName");

        // Load UserRoles và UserEmployeeCode từ session hoặc database
       
        List<String> userRoles = (List<String>) req.getSession().getAttribute("UserRoles");
        
        // Nếu chưa có UserRoles trong session, load từ database
        if (userRoles == null || userRoles.isEmpty()) {
            Object userLogin = req.getSession().getAttribute("UserLogin");
            if (userLogin != null) {
                UUID userId = null;
                if (userLogin instanceof UUID) {
                    userId = (UUID) userLogin;
                } else if (userLogin instanceof String) {
                    try {
                        userId = UUID.fromString((String) userLogin);
                    } catch (IllegalArgumentException e) {
                        // Ignore
                    }
                }
                
                if (userId != null) {
                    userRoles = userService.getRoleNames(userId);
                    req.getSession().setAttribute("UserRoles", userRoles);
                    System.out.println("Loaded UserRoles from database: " + userRoles);
                    
                    // Load UserEmployeeCode
                    employeeService.getEmployeeByUserID(userId).ifPresent(emp -> {
                        req.getSession().setAttribute("UserEmployeeCode", emp.getEmployeeCode());
                        System.out.println("Loaded UserEmployeeCode from database: " + emp.getEmployeeCode());
                    });
                }
            }
        }
        
        boolean isEmployee = false;
        if (userRoles != null) {
            System.out.println("User roles: " + userRoles);
            for (String role : userRoles) {
                if ("Employee".equalsIgnoreCase(role)) {
                    isEmployee = true;
                    break;
                }
            }
        }
        
        if (isEmployee) {
            String userEmployeeCode = (String) req.getSession().getAttribute("UserEmployeeCode");
            System.out.println("Employee user detected. UserEmployeeCode: " + userEmployeeCode);
            if (userEmployeeCode != null && !userEmployeeCode.isBlank()) {
                employeeCodeFilter = userEmployeeCode; // Override với employeeCode của user
                System.out.println("Filtering by employeeCode: " + employeeCodeFilter);
            } else {
                System.out.println("WARNING: Employee user but no UserEmployeeCode in session!");
            }
        }

        if (employeeCodeFilter != null && !employeeCodeFilter.isBlank()) {
            String ec = employeeCodeFilter.trim();
            int beforeCount = shifts.size();
            
            // Log tất cả shifts trước khi filter
            System.out.println("Shifts before filter:");
            for (EmployeeShift s : shifts) {
                if (s.getEmployee() != null) {
                    System.out.println("  Shift: employeeCode=" + s.getEmployee().getEmployeeCode() + ", employee=" + s.getEmployee().getFullName());
                } else {
                    System.out.println("  Shift: employee=NULL");
                }
            }
            
            shifts.removeIf(s -> {
                boolean shouldRemove = s.getEmployee() == null || s.getEmployee().getEmployeeCode() == null || !s.getEmployee().getEmployeeCode().equals(ec);
                if (shouldRemove && s.getEmployee() != null) {
                    System.out.println("Removing shift: " + s.getEmployee().getEmployeeCode() + " != " + ec);
                }
                return shouldRemove;
            });
            
            int afterCount = shifts.size();
            System.out.println("Filtered shifts from " + beforeCount + " to " + afterCount + " by employeeCode: " + ec);
            
            // Log shifts sau khi filter
            System.out.println("Shifts after filter:");
            for (EmployeeShift s : shifts) {
                if (s.getEmployee() != null) {
                    System.out.println("  Shift: employeeCode=" + s.getEmployee().getEmployeeCode() + ", employee=" + s.getEmployee().getFullName());
                }
            }
        }

        if (templateNameFilter != null && !templateNameFilter.isBlank()) {
            var match = templates.stream().filter(t -> templateNameFilter.equals(t.getName())).findFirst();
            if (match.isPresent()) {
                var t = match.get();
                String tStart = t.getStartTime().toString().substring(0, 5);
                String tEnd = t.getEndTime().toString().substring(0, 5);
                shifts.removeIf(s -> {
                    String sStart = s.getStartAt().toLocalTime().toString().substring(0, 5);
                    String sEnd = s.getEndAt().toLocalTime().toString().substring(0, 5);
                    return !sStart.equals(tStart) || !sEnd.equals(tEnd);
                });
            }
        }

        // Build week metadata for JSP rendering
        DateTimeFormatter dmy = DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("vi"));
        DateTimeFormatter dmyFull = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi"));

        LocalDate weekEnd = weekStart.plusDays(6);
        String weekLabel = "Tuần " + weekStart.format(dmy) + " - " + weekEnd.format(dmyFull);

        // Week-of-month label for control chip like: "Tuần 2 - Th. 10 2025"
        var wf = java.time.temporal.WeekFields.of(DayOfWeek.MONDAY, 1);
        int weekOfMonth = weekStart.get(wf.weekOfMonth());
        String monthShort = String.format("%02d", weekStart.getMonthValue());
        String controlLabel = "Tuần " + weekOfMonth + " - Th. " + monthShort + " " + weekStart.getYear();

        // Prepare per-day buckets with 3 base shift rows (templates)
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

            // For each template, collect shifts of that template time range (best-effort match by time)
            List<Map<String, Object>> rows = new ArrayList<>();
            templates.forEach(t -> {
                Map<String, Object> row = new HashMap<>();
                row.put("templateName", t.getName());
                row.put("templateTime", String.format("%s - %s", t.getStartTime().toString().substring(0,5), t.getEndTime().toString().substring(0,5)));
                List<Map<String, String>> cellShifts = new ArrayList<>();
                for (EmployeeShift s : shifts) {
                    if (!s.getStartAt().toLocalDate().equals(day)) continue;
                    String sStart = s.getStartAt().toLocalTime().toString().substring(0,5);
                    String sEnd = s.getEndAt().toLocalTime().toString().substring(0,5);
                    String tStart = t.getStartTime().toString().substring(0,5);
                    String tEnd = t.getEndTime().toString().substring(0,5);
                    if (sStart.equals(tStart) && sEnd.equals(tEnd)) {
                        Map<String, String> vm = new HashMap<>();
                        vm.put("employee", s.getEmployee() != null ? s.getEmployee().getFullName() : "");
                        vm.put("notes", s.getNotes() != null ? s.getNotes() : "");
                        vm.put("location", s.getLocation() != null ? s.getLocation() : "");
                        vm.put("shiftId", s.getShiftID() != null ? s.getShiftID().toString() : "");
                        vm.put("title", s.getTitle() != null ? s.getTitle() : "");
                        vm.put("status", s.getStatus() != null ? s.getStatus() : "");
                        vm.put("startAt", s.getStartAt().toString());
                        vm.put("endAt", s.getEndAt().toString());
                        vm.put("isRecurring", s.getIsRecurring() != null ? s.getIsRecurring().toString() : "false");
                        cellShifts.add(vm);
                    }
                }
                row.put("items", cellShifts);
                rows.add(row);
            });
            dayMap.put("rows", rows);
            weekDays.add(dayMap);
        }

        // Prev/Next links
        String prevWeekStart = weekStart.minusDays(7).toString();
        String nextWeekStart = weekStart.plusDays(7).toString();

        // Preserve filters across navigation
        StringBuilder fq = new StringBuilder();
        if (employeeCodeFilter != null && !employeeCodeFilter.isBlank()) {
            fq.append("&employeeCode=").append(URLEncoder.encode(employeeCodeFilter, StandardCharsets.UTF_8));
        }
        if (templateNameFilter != null && !templateNameFilter.isBlank()) {
            fq.append("&templateName=").append(URLEncoder.encode(templateNameFilter, StandardCharsets.UTF_8));
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
        req.setAttribute("selectedTemplateName", templateNameFilter);
        req.setAttribute("filterQuery", filterQuery);
        req.setAttribute("isEmployee", isEmployee);
        req.getRequestDispatcher("/schedule.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        String weekStartParam = req.getParameter("weekStart");
        if (weekStartParam == null || weekStartParam.isBlank()) {
            weekStartParam = LocalDate.now().toString();
        }

        if (action == null || action.isBlank() || "create".equals(action)) {
            try {
                String[] employeeCodes = req.getParameterValues("employeeCode");
                String dateStr = req.getParameter("date");
                String[] startTimes = req.getParameterValues("startTime");
                String[] endTimes = req.getParameterValues("endTime");
                String title = req.getParameter("title");
                String notes = req.getParameter("notes");
                String location = req.getParameter("location");
                String isRecurringStr = req.getParameter("isRecurring");
                boolean isRecurring = "true".equals(isRecurringStr);

                LocalDate date = LocalDate.parse(dateStr);
                boolean anyCreated = false;
                if (employeeCodes != null && employeeCodes.length > 0 && startTimes != null && endTimes != null && startTimes.length == endTimes.length) {
                    for (String employeeCode : employeeCodes) {
                        if (employeeCode == null || employeeCode.isBlank()) continue;
                        for (int i = 0; i < startTimes.length; i++) {
                            LocalTime st = LocalTime.parse(startTimes[i]);
                            LocalTime et = LocalTime.parse(endTimes[i]);
                            boolean ok = scheduleService.createShift(employeeCode, date, st, et, title, notes, location, isRecurring);
                            anyCreated = anyCreated || ok;
                        }
                    }
                }

                if (anyCreated) {
                    StringBuilder url = new StringBuilder(req.getContextPath())
                            .append("/schedule?weekStart=").append(weekStartParam);
                    // Không thêm employeeCode vào URL để hiển thị toàn bộ lịch làm việc sau khi thêm
                    String templateName = req.getParameter("templateName");
                    if (templateName != null && !templateName.isBlank()) {
                        url.append("&templateName=").append(URLEncoder.encode(templateName, StandardCharsets.UTF_8));
                    }
                    String embed = req.getParameter("embed");
                    if ("1".equals(embed)) {
                        url.append("&embed=1");
                    }
                    resp.sendRedirect(url.toString());
                    return;
                } else {
                    req.setAttribute("error", "Không thể tạo ca làm việc. Vui lòng kiểm tra dữ liệu.");
                }
            } catch (Exception ex) {
                req.setAttribute("error", "Lỗi khi tạo ca làm việc: " + ex.getMessage());
            }
        } else if ("delete".equals(action)) {
            try {
                String shiftIdStr = req.getParameter("shiftId");
                java.util.UUID sid = java.util.UUID.fromString(shiftIdStr);
                boolean ok = scheduleService.deleteShift(sid);
                if (ok) {
                    StringBuilder url = new StringBuilder(req.getContextPath())
                            .append("/schedule?weekStart=")
                            .append(weekStartParam != null ? weekStartParam : LocalDate.now().toString());
                    // Không thêm employeeCode vào URL để hiển thị toàn bộ lịch làm việc sau khi xóa
                    String templateName = req.getParameter("templateName");
                    if (templateName != null && !templateName.isBlank()) {
                        url.append("&templateName=").append(URLEncoder.encode(templateName, StandardCharsets.UTF_8));
                    }
                    String embed = req.getParameter("embed");
                    if ("1".equals(embed)) {
                        url.append("&embed=1");
                    }
                    resp.sendRedirect(url.toString());
                    return;
                } else {
                    req.setAttribute("error", "Không thể xóa ca làm việc");
                }
            } catch (Exception ex) {
                req.setAttribute("error", "Lỗi khi xóa ca làm việc: " + ex.getMessage());
            }
        } else if ("toggleRecurring".equals(action)) {
            try {
                String shiftIdStr = req.getParameter("shiftId");
                String isRecurringStr = req.getParameter("isRecurring");
                java.util.UUID sid = java.util.UUID.fromString(shiftIdStr);
                boolean isRecurring = "true".equals(isRecurringStr);
                
                boolean ok = scheduleService.toggleRecurring(sid, isRecurring);
                if (ok) {
                    StringBuilder url = new StringBuilder(req.getContextPath())
                            .append("/schedule?weekStart=")
                            .append(weekStartParam != null ? weekStartParam : LocalDate.now().toString());
                    // Không thêm employeeCode vào URL để hiển thị toàn bộ lịch làm việc sau khi toggle
                    String templateName = req.getParameter("templateName");
                    if (templateName != null && !templateName.isBlank()) {
                        url.append("&templateName=").append(URLEncoder.encode(templateName, StandardCharsets.UTF_8));
                    }
                    String embed = req.getParameter("embed");
                    if ("1".equals(embed)) {
                        url.append("&embed=1");
                    }
                    resp.sendRedirect(url.toString());
                    return;
                } else {
                    req.setAttribute("error", "Không thể cập nhật trạng thái lặp lại");
                }
            } catch (Exception ex) {
                req.setAttribute("error", "Lỗi khi cập nhật trạng thái lặp lại: " + ex.getMessage());
            }
        }

        // Fallback: render GET with error message
        doGet(req, resp);
    }
}


