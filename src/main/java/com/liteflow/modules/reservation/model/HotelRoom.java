package com.liteflow.modules.reservation.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * HotelRoom: Phòng khách sạn/resort.
 * Trạng thái: AVAILABLE, OCCUPIED, DIRTY, CLEANING,
 *             MAINTENANCE, OUT_OF_ORDER, RESERVED, VIP
 */
@Entity
@Table(name = "HotelRooms")
public class HotelRoom implements Serializable {

    public enum Status {
        AVAILABLE, OCCUPIED, DIRTY, CLEANING,
        MAINTENANCE, OUT_OF_ORDER, RESERVED, VIP
    }

    @Id
    @Column(name = "HotelRoomID", columnDefinition = "uniqueidentifier")
    private UUID hotelRoomID;

    @Column(name = "RoomNumber", length = 20, nullable = false, unique = true)
    private String roomNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FloorID", nullable = false)
    private Floor floor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomTypeID", nullable = false)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 30, nullable = false)
    private Status status = Status.AVAILABLE;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @Column(name = "Notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "hotelRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HotelReservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "hotelRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoomStatusHistory> statusHistories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (hotelRoomID == null) hotelRoomID = UUID.randomUUID();
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    /** Kiểm tra phòng có sẵn sàng đặt không */
    public boolean isAvailableForBooking() {
        return status == Status.AVAILABLE;
    }

    /** Lấy tên toà nhà và tầng */
    public String getLocationLabel() {
        if (floor != null && floor.getBuilding() != null) {
            return floor.getBuilding().getCode() + " - Tầng " + floor.getFloorNumber();
        }
        return "";
    }

    // Getters & Setters
    public UUID getHotelRoomID() { return hotelRoomID; }
    public void setHotelRoomID(UUID v) { hotelRoomID = v; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String v) { roomNumber = v; }
    public Floor getFloor() { return floor; }
    public void setFloor(Floor v) { floor = v; }
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType v) { roomType = v; }
    public Status getStatus() { return status; }
    public void setStatus(Status v) { status = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { isActive = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { notes = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<HotelReservation> getReservations() { return reservations; }
    public void setReservations(List<HotelReservation> v) { reservations = v; }
    public List<RoomStatusHistory> getStatusHistories() { return statusHistories; }
    public void setStatusHistories(List<RoomStatusHistory> v) { statusHistories = v; }

    @Override
    public String toString() {
        return "HotelRoom{roomNumber='" + roomNumber + "', status=" + status + "}";
    }
}
