package com.liteflow.service.timesheet;

import com.liteflow.dao.timesheet.PersonalScheduleDAO;
import com.liteflow.model.timesheet.PersonalSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PersonalScheduleService {

    private final PersonalScheduleDAO personalScheduleDAO = new PersonalScheduleDAO();

    /**
     * Lấy tất cả lịch cá nhân của employee
     */
    public List<PersonalSchedule> getSchedulesByEmployeeId(UUID employeeId) {
        return personalScheduleDAO.findByEmployeeId(employeeId);
    }

    /**
     * Lấy lịch cá nhân trong khoảng thời gian
     */
    public List<PersonalSchedule> getSchedulesByDateRange(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        return personalScheduleDAO.findByEmployeeAndDateRange(employeeId, startDate, endDate);
    }

    /**
     * Lấy lịch cá nhân theo ngày
     */
    public List<PersonalSchedule> getSchedulesByDate(UUID employeeId, LocalDate date) {
        return personalScheduleDAO.findByEmployeeAndDate(employeeId, date);
    }

    /**
     * Lấy lịch cá nhân theo priority
     */
    public List<PersonalSchedule> getSchedulesByPriority(UUID employeeId, String priority) {
        return personalScheduleDAO.findByEmployeeAndPriority(employeeId, priority);
    }

    /**
     * Lấy lịch cá nhân theo status
     */
    public List<PersonalSchedule> getSchedulesByStatus(UUID employeeId, String status) {
        return personalScheduleDAO.findByEmployeeAndStatus(employeeId, status);
    }

    /**
     * Tạo mới lịch cá nhân
     */
    public boolean createSchedule(PersonalSchedule schedule) {
        if (schedule == null || schedule.getEmployee() == null) {
            return false;
        }
        return personalScheduleDAO.insert(schedule);
    }

    /**
     * Cập nhật lịch cá nhân
     */
    public boolean updateSchedule(PersonalSchedule schedule) {
        if (schedule == null || schedule.getScheduleId() == null) {
            return false;
        }
        return personalScheduleDAO.update(schedule);
    }

    /**
     * Xóa lịch cá nhân
     */
    public boolean deleteSchedule(UUID scheduleId) {
        return personalScheduleDAO.delete(scheduleId);
    }

    /**
     * Lấy lịch cá nhân theo ID
     */
    public Optional<PersonalSchedule> getScheduleById(UUID scheduleId) {
        PersonalSchedule schedule = personalScheduleDAO.findById(scheduleId);
        return Optional.ofNullable(schedule);
    }

    /**
     * Kiểm tra quyền sở hữu
     */
    public boolean isOwner(UUID scheduleId, UUID employeeId) {
        Optional<PersonalSchedule> schedule = getScheduleById(scheduleId);
        if (schedule.isEmpty()) {
            return false;
        }
        return schedule.get().getEmployee().getEmployeeID().equals(employeeId);
    }
}


