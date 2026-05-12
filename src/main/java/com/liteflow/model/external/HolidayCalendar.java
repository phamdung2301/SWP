package com.liteflow.model.external;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "HolidayCalendar")
public class HolidayCalendar implements Serializable {

    @Id
    @Column(name = "HolidayID", columnDefinition = "uniqueidentifier")
    private UUID holidayId;

    @Column(name = "HolidayDate", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "Name", length = 200, nullable = false)
    private String name;

    @Column(name = "Region", length = 50, nullable = false)
    private String region;

    @Column(name = "DayType", length = 20, nullable = false)
    private String dayType;

    @Column(name = "IsPaidHoliday", nullable = false)
    private Boolean isPaidHoliday;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (holidayId == null) {
            holidayId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        if (region == null) { region = "VN"; }
        if (dayType == null) { dayType = "Public"; }
        if (isPaidHoliday == null) { isPaidHoliday = Boolean.TRUE; }
    }

    public UUID getHolidayId() { return holidayId; }
    public void setHolidayId(UUID holidayId) { this.holidayId = holidayId; }
    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getDayType() { return dayType; }
    public void setDayType(String dayType) { this.dayType = dayType; }
    public Boolean getIsPaidHoliday() { return isPaidHoliday; }
    public void setIsPaidHoliday(Boolean paidHoliday) { isPaidHoliday = paidHoliday; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}


