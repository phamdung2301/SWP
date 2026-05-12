package com.liteflow.model.timesheet;

import com.liteflow.model.auth.Employee;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "EmployeeAttendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"EmployeeID", "WorkDate"})
})
public class EmployeeAttendance implements Serializable {

    @Id
    @Column(name = "AttendanceID", columnDefinition = "uniqueidentifier")
    private UUID attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeID", nullable = false)
    private Employee employee;

    @Column(name = "WorkDate", nullable = false)
    private LocalDate workDate;

    // Values: Work, LeavePaid, LeaveUnpaid
    @Column(name = "Status", length = 20, nullable = false)
    private String status;

    @Column(name = "CheckInTime")
    private LocalTime checkInTime;

    @Column(name = "CheckOutTime")
    private LocalTime checkOutTime;

    @Column(name = "Notes", length = 500)
    private String notes;

    // Status flags for attendance violations/overtime
    @Column(name = "IsLate")
    private Boolean isLate;

    @Column(name = "IsOvertime")
    private Boolean isOvertime;

    @Column(name = "IsEarlyLeave")
    private Boolean isEarlyLeave;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (attendanceId == null) {
            attendanceId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getAttendanceId() { return attendanceId; }
    public void setAttendanceId(UUID attendanceId) { this.attendanceId = attendanceId; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }
    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Boolean getIsLate() { return isLate; }
    public void setIsLate(Boolean isLate) { this.isLate = isLate; }
    public Boolean getIsOvertime() { return isOvertime; }
    public void setIsOvertime(Boolean isOvertime) { this.isOvertime = isOvertime; }
    public Boolean getIsEarlyLeave() { return isEarlyLeave; }
    public void setIsEarlyLeave(Boolean isEarlyLeave) { this.isEarlyLeave = isEarlyLeave; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}


