package com.liteflow.modules.core.controller;

import com.liteflow.modules.hr.dao.EmployeeShiftDAO;
import com.liteflow.modules.hr.dao.EmployeeAttendanceDAO;
import com.liteflow.modules.auth.model.EmployeeShift;
import com.liteflow.modules.hr.model.EmployeeAttendance;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Servlet admin để recalculate tất cả attendance flags dựa trên shift times
 * URL: /admin/recalculate-attendance-flags
 */
@WebServlet(name = "RecalculateAttendanceFlagsServlet", urlPatterns = {"/admin/recalculate-attendance-flags"})
public class RecalculateAttendanceFlagsServlet extends HttpServlet {

    private final EmployeeAttendanceDAO attendanceDAO = new EmployeeAttendanceDAO();
    private final EmployeeShiftDAO shiftDAO = new EmployeeShiftDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Recalculate Attendance Flags</title></head>");
        out.println("<body style='font-family: system-ui; padding: 40px;'>");
        out.println("<h1>🔄 Recalculate Attendance Flags</h1>");
        out.println("<p>Công cụ này sẽ tính toán lại tất cả flags (isLate, isEarlyLeave, isOvertime) cho dữ liệu EmployeeAttendance dựa trên EmployeeShifts.</p>");
        out.println("<form method='post' style='margin-top: 20px;'>");
        out.println("<label>Số ngày gần đây cần recalculate: <input type='number' name='days' value='30' min='1' max='365' style='width: 80px; padding: 5px;' /></label><br><br>");
        out.println("<button type='submit' style='background: #3b82f6; color: white; padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer; font-size: 16px;'>🚀 Bắt đầu Recalculate</button>");
        out.println("</form>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        // Parse số ngày cần recalculate
        int days = 30;
        try {
            String daysParam = req.getParameter("days");
            if (daysParam != null && !daysParam.isBlank()) {
                days = Integer.parseInt(daysParam);
            }
        } catch (NumberFormatException e) {
            days = 30;
        }
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Recalculate Results</title></head>");
        out.println("<body style='font-family: system-ui; padding: 40px;'>");
        out.println("<h1>🔄 Recalculate Results</h1>");
        out.println("<p>Đang xử lý dữ liệu từ " + startDate + " đến " + endDate + "...</p>");
        out.println("<hr>");
        
        // Lấy tất cả attendance records trong khoảng thời gian
        List<EmployeeAttendance> attendanceList = attendanceDAO.findByWorkDateRange(startDate, endDate);
        
        int totalRecords = attendanceList.size();
        int updatedRecords = 0;
        
        out.println("<p>Tìm thấy <strong>" + totalRecords + "</strong> attendance records.</p>");
        out.println("<ul style='list-style: none; padding: 0; max-height: 400px; overflow-y: auto; background: #f9fafb; padding: 16px; border-radius: 8px;'>");
        
        for (EmployeeAttendance attendance : attendanceList) {
            if (attendance.getEmployee() == null) continue;
            
            LocalDate workDate = attendance.getWorkDate();
            LocalTime checkInTime = attendance.getCheckInTime();
            LocalTime checkOutTime = attendance.getCheckOutTime();
            
            // Tìm shift của nhân viên trong ngày
            List<EmployeeShift> shifts = shiftDAO.findByEmployeeAndDate(
                attendance.getEmployee().getEmployeeID(), workDate
            );
            
            if (shifts.isEmpty()) {
                // Không có shift, bỏ qua
                continue;
            }
            
            // Reset flags
            boolean hasLate = false;
            boolean hasEarlyLeave = false;
            boolean hasOvertime = false;
            
            // Tính toán flags dựa trên shift đầu tiên (giả sử mỗi ngày chỉ có 1 shift chính)
            for (EmployeeShift shift : shifts) {
                if (shift.getStartAt() == null || shift.getEndAt() == null) continue;
                
                LocalTime shiftStartTime = shift.getStartAt().toLocalTime();
                LocalTime shiftEndTime = shift.getEndAt().toLocalTime();
                
                // 1. Kiểm tra đi muộn
                if (checkInTime != null && checkInTime.isAfter(shiftStartTime)) {
                    hasLate = true;
                }
                
                // 2. Kiểm tra về sớm
                if (checkOutTime != null && checkOutTime.isBefore(shiftEndTime)) {
                    hasEarlyLeave = true;
                }
                
                // 3. Kiểm tra tăng ca (chỉ khi không có vi phạm)
                if (!hasLate && !hasEarlyLeave) {
                    boolean inEarly = checkInTime != null && checkInTime.isBefore(shiftStartTime);
                    boolean outLate = checkOutTime != null && checkOutTime.isAfter(shiftEndTime);
                    if (inEarly || outLate) {
                        hasOvertime = true;
                    }
                }
            }
            
            // Cập nhật flags
            boolean changed = false;
            if (attendance.getIsLate() == null || attendance.getIsLate() != hasLate) {
                attendance.setIsLate(hasLate);
                changed = true;
            }
            if (attendance.getIsEarlyLeave() == null || attendance.getIsEarlyLeave() != hasEarlyLeave) {
                attendance.setIsEarlyLeave(hasEarlyLeave);
                changed = true;
            }
            if (attendance.getIsOvertime() == null || attendance.getIsOvertime() != hasOvertime) {
                attendance.setIsOvertime(hasOvertime);
                changed = true;
            }
            
            if (changed) {
                attendanceDAO.update(attendance);
                updatedRecords++;
                
                String statusColor = hasLate || hasEarlyLeave ? "#8b5cf6" : hasOvertime ? "#ef4444" : "#10b981";
                String statusText = hasLate || hasEarlyLeave ? "Vi phạm" : hasOvertime ? "Tăng ca" : "Đúng giờ";
                
                out.println("<li style='padding: 8px; margin-bottom: 4px; background: white; border-left: 4px solid " + statusColor + "; border-radius: 4px;'>");
                out.println("<strong>" + attendance.getEmployee().getFullName() + "</strong> - " + workDate + " ");
                out.println("<span style='color: " + statusColor + "; font-weight: 600;'>[" + statusText + "]</span>");
                out.println("</li>");
            }
        }
        
        out.println("</ul>");
        out.println("<hr>");
        out.println("<h2 style='color: #10b981;'>✅ Hoàn thành!</h2>");
        out.println("<p>Đã cập nhật <strong>" + updatedRecords + "</strong> / " + totalRecords + " records.</p>");
        out.println("<a href='" + req.getContextPath() + "/admin/recalculate-attendance-flags' style='display: inline-block; margin-top: 20px; padding: 10px 20px; background: #e5e7eb; color: #111827; text-decoration: none; border-radius: 8px;'>← Quay lại</a>");
        out.println("</body></html>");
    }
}

