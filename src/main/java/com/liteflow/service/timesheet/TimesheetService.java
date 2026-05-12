package com.liteflow.service.timesheet;

import com.liteflow.dao.timesheet.EmployeeShiftTimesheetDAO;
import com.liteflow.dao.timesheet.EmployeeAttendanceDAO;
import com.liteflow.model.timesheet.EmployeeShiftTimesheet;
import com.liteflow.model.timesheet.EmployeeAttendance;
import com.liteflow.dao.employee.EmployeeDAO;
import com.liteflow.dao.employee.EmployeeShiftDAO;
import com.liteflow.model.auth.Employee;
import com.liteflow.model.auth.EmployeeShift;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TimesheetService {

    private final EmployeeShiftTimesheetDAO timesheetDAO = new EmployeeShiftTimesheetDAO();
    private final EmployeeAttendanceDAO attendanceDAO = new EmployeeAttendanceDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final EmployeeShiftDAO shiftDAO = new EmployeeShiftDAO();

    public List<EmployeeShiftTimesheet> getTimesheetsForWeek(LocalDate weekStart) {
        if (weekStart == null) return new ArrayList<>();
        LocalDate start = weekStart;
        LocalDate end = weekStart.plusDays(6);
        return new ArrayList<>(timesheetDAO.findByWorkDateRange(start, end));
    }

    public List<EmployeeAttendance> getAttendanceForWeek(LocalDate weekStart) {
        if (weekStart == null) return new ArrayList<>();
        LocalDate start = weekStart;
        LocalDate end = weekStart.plusDays(6);
        return new ArrayList<>(attendanceDAO.findByWorkDateRange(start, end));
    }

    public EmployeeAttendance upsertAttendance(String employeeCode, LocalDate workDate, String status,
                                               String checkIn, String checkOut, String notes,
                                               Boolean inLate, Boolean inOver, Boolean outEarly, Boolean outOver) {
        if (employeeCode == null || employeeCode.isBlank() || workDate == null || status == null || status.isBlank()) {
            return null;
        }
        Employee emp = employeeDAO.findByEmployeeCode(employeeCode.trim());
        if (emp == null) return null;

        EmployeeAttendance att = attendanceDAO.findByEmployeeAndDate(emp.getEmployeeID(), workDate);
        if (att == null) {
            att = new EmployeeAttendance();
            att.setEmployee(emp);
            att.setWorkDate(workDate);
        }
        // Map UI values to DB values
        String st;
        switch (status) {
            case "work": st = "Work"; break;
            case "leave_paid": st = "LeavePaid"; break;
            case "leave_unpaid": st = "LeaveUnpaid"; break;
            default: st = "Work";
        }
        att.setStatus(st);

        LocalTime inT = null;
        LocalTime outT = null;
        try { if (checkIn != null && !checkIn.isBlank()) inT = LocalTime.parse(checkIn); } catch (Exception ignored) {}
        try { if (checkOut != null && !checkOut.isBlank()) outT = LocalTime.parse(checkOut); } catch (Exception ignored) {}
        att.setCheckInTime(inT);
        att.setCheckOutTime(outT);
        if (notes != null) {
            att.setNotes(notes.trim());
        }

        // Set status flags
        att.setIsLate(inLate != null && inLate);
        att.setIsOvertime((inOver != null && inOver) || (outOver != null && outOver));
        att.setIsEarlyLeave(outEarly != null && outEarly);

        if (att.getAttendanceId() == null) {
            attendanceDAO.insert(att);
        } else {
            attendanceDAO.update(att);
        }
        return att;
    }

    // ==============================
    // Clock In/Out Functions
    // ==============================

    /**
     * Chấm công vào - Clock In
     * Tích hợp: Tạo/cập nhật cả EmployeeAttendance và EmployeeShiftTimesheet (nếu có shift)
     * @param employeeId ID của nhân viên
     * @return EmployeeAttendance đã được tạo/cập nhật, hoặc null nếu thất bại
     */
    public EmployeeAttendance clockIn(UUID employeeId) {
        if (employeeId == null) {
            return null;
        }

        Employee employee = employeeDAO.findById(employeeId);
        if (employee == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDateTime nowDateTime = LocalDateTime.now();

        // 1. Tạo/cập nhật EmployeeAttendance
        EmployeeAttendance attendance = attendanceDAO.findByEmployeeAndDate(employeeId, today);
        
        if (attendance == null) {
            attendance = new EmployeeAttendance();
            attendance.setEmployee(employee);
            attendance.setWorkDate(today);
            attendance.setStatus("Work");
            attendance.setCheckInTime(now);
            attendance.setIsLate(false);
            attendance.setIsOvertime(false);
            attendance.setIsEarlyLeave(false);
            attendanceDAO.insert(attendance);
        } else {
            attendance.setCheckInTime(now);
            attendanceDAO.update(attendance);
        }

        // 2. Tìm shift của nhân viên trong ngày (nếu có)
        List<EmployeeShift> shifts = shiftDAO.findByEmployeeAndDate(employeeId, today);
        
        // Reset flags trước khi tính toán
        boolean hasLate = false;
        
        // Nếu có shift, tạo/cập nhật EmployeeShiftTimesheet
        for (EmployeeShift shift : shifts) {
            // Tìm xem đã có timesheet cho shift này chưa
            EmployeeShiftTimesheet timesheet = timesheetDAO.findByEmployeeShiftAndDate(
                employeeId, shift.getShiftID(), today
            );
            
            // Tính toán xem có đi muộn không (so với shift start time)
            LocalDateTime shiftStart = shift.getStartAt();
            if (nowDateTime.isAfter(shiftStart)) {
                hasLate = true;
            }
            
            if (timesheet == null) {
                // Tạo timesheet mới
                timesheet = new EmployeeShiftTimesheet();
                timesheet.setEmployee(employee);
                timesheet.setShift(shift);
                timesheet.setWorkDate(today);
                timesheet.setCheckInAt(nowDateTime);
                timesheet.setCheckOutAt(nowDateTime); // Temporary, will update on clock out
                timesheet.setBreakMinutes(0);
                timesheet.setStatus("Pending");
                timesheet.setSource("Auto");
                timesheetDAO.insert(timesheet);
            } else {
                // Cập nhật check-in time
                timesheet.setCheckInAt(nowDateTime);
                timesheetDAO.update(timesheet);
            }
        }
        
        // Cập nhật flag isLate trong EmployeeAttendance (các flags khác sẽ được cập nhật khi clockOut)
        if (hasLate) {
            attendance.setIsLate(true);
            attendanceDAO.update(attendance);
        }

        return attendance;
    }

    /**
     * Chấm công ra - Clock Out
     * Tích hợp: Cập nhật cả EmployeeAttendance và EmployeeShiftTimesheet (nếu có shift)
     * @param employeeId ID của nhân viên
     * @return EmployeeAttendance đã được cập nhật, hoặc null nếu thất bại
     */
    public EmployeeAttendance clockOut(UUID employeeId) {
        if (employeeId == null) {
            return null;
        }

        Employee employee = employeeDAO.findById(employeeId);
        if (employee == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDateTime nowDateTime = LocalDateTime.now();

        // 1. Cập nhật EmployeeAttendance
        EmployeeAttendance attendance = attendanceDAO.findByEmployeeAndDate(employeeId, today);
        
        if (attendance == null) {
            // Chưa có record (chưa check-in), tạo mới với cả check-in và check-out
            attendance = new EmployeeAttendance();
            attendance.setEmployee(employee);
            attendance.setWorkDate(today);
            attendance.setStatus("Work");
            attendance.setCheckInTime(now);
            attendance.setCheckOutTime(now);
            attendance.setIsLate(false);
            attendance.setIsOvertime(false);
            attendance.setIsEarlyLeave(false);
            attendanceDAO.insert(attendance);
        } else {
            // Đã có record, cập nhật check-out time
            attendance.setCheckOutTime(now);
            attendanceDAO.update(attendance);
        }

        // 2. Tìm shift của nhân viên trong ngày (nếu có)
        List<EmployeeShift> shifts = shiftDAO.findByEmployeeAndDate(employeeId, today);
        
        // Reset flags trước khi tính toán lại
        boolean hasLate = false;
        boolean hasEarlyLeave = false;
        boolean hasOvertime = false;
        
        // Nếu có shift, cập nhật EmployeeShiftTimesheet
        for (EmployeeShift shift : shifts) {
            EmployeeShiftTimesheet timesheet = timesheetDAO.findByEmployeeShiftAndDate(
                employeeId, shift.getShiftID(), today
            );
            
            LocalDateTime shiftStart = shift.getStartAt();
            LocalDateTime shiftEnd = shift.getEndAt();
            
            if (timesheet == null) {
                // Chưa có timesheet (chưa check-in), tạo mới
                timesheet = new EmployeeShiftTimesheet();
                timesheet.setEmployee(employee);
                timesheet.setShift(shift);
                timesheet.setWorkDate(today);
                timesheet.setCheckInAt(nowDateTime);
                timesheet.setCheckOutAt(nowDateTime);
                timesheet.setBreakMinutes(0);
                timesheet.setStatus("Pending");
                timesheet.setSource("Auto");
                
                // Tính số giờ làm việc
                Duration duration = Duration.between(nowDateTime, nowDateTime);
                BigDecimal hours = BigDecimal.valueOf(duration.toMinutes())
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                timesheet.setHoursWorked(hours);
                
                timesheetDAO.insert(timesheet);
            } else {
                // Cập nhật check-out time và tính số giờ làm việc
                timesheet.setCheckOutAt(nowDateTime);
                
                // Tính số giờ làm việc (trừ break minutes)
                LocalDateTime checkIn = timesheet.getCheckInAt();
                if (checkIn != null) {
                    Duration duration = Duration.between(checkIn, nowDateTime);
                    long totalMinutes = duration.toMinutes();
                    long workMinutes = totalMinutes - (timesheet.getBreakMinutes() != null ? timesheet.getBreakMinutes() : 0);
                    BigDecimal hours = BigDecimal.valueOf(workMinutes)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                    timesheet.setHoursWorked(hours);
                    
                    // ===== RECALCULATE TẤT CẢ FLAGS DỰA TRÊN CHECK-IN VÀ CHECK-OUT TIMES =====
                    
                    // 1. Kiểm tra đi muộn (check-in sau shift start)
                    if (checkIn.isAfter(shiftStart)) {
                        hasLate = true;
                    }
                    
                    // 2. Kiểm tra về sớm (check-out trước shift end)
                    if (nowDateTime.isBefore(shiftEnd)) {
                        hasEarlyLeave = true;
                    }
                    
                    // 3. Kiểm tra tăng ca (vào sớm HOẶC về muộn) - CHỈ KHI KHÔNG CÓ VI PHẠM
                    if (!hasLate && !hasEarlyLeave) {
                        if (checkIn.isBefore(shiftStart) || nowDateTime.isAfter(shiftEnd)) {
                            hasOvertime = true;
                        }
                    }
                }
                
                timesheetDAO.update(timesheet);
            }
        }
        
        // Cập nhật TẤT CẢ flags trong EmployeeAttendance
        attendance.setIsLate(hasLate);
        attendance.setIsEarlyLeave(hasEarlyLeave);
        attendance.setIsOvertime(hasOvertime);
        attendanceDAO.update(attendance);

        return attendance;
    }

    /**
     * Lấy trạng thái chấm công của nhân viên trong ngày
     * @param employeeId ID của nhân viên
     * @return EmployeeAttendance của ngày hôm nay, hoặc null nếu chưa chấm công
     */
    public EmployeeAttendance getTodayAttendance(UUID employeeId) {
        if (employeeId == null) {
            return null;
        }
        
        LocalDate today = LocalDate.now();
        return attendanceDAO.findByEmployeeAndDate(employeeId, today);
    }
}


