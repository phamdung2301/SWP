package com.liteflow.modules.reservation.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HotelReservation: Đặt phòng khách sạn.
 * Khác với Reservation (đặt bàn nhà hàng).
 */
@Entity
@Table(name = "HotelReservations")
public class HotelReservation implements Serializable {

    public enum Status {
        PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW
    }

    @Id
    @Column(name = "ReservationID", columnDefinition = "uniqueidentifier")
    private UUID reservationID;

    @Column(name = "ReservationCode", length = 20, nullable = false, unique = true)
    private String reservationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GuestID", nullable = false)
    private GuestProfile guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HotelRoomID", nullable = false)
    private HotelRoom hotelRoom;

    @Column(name = "CheckInDate", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "CheckOutDate", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "Adults", nullable = false)
    private Integer adults = 1;

    @Column(name = "Children", nullable = false)
    private Integer children = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20, nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "Source", length = 50)
    private String source;  // WALK_IN, ONLINE, OTA

    @Column(name = "TotalAmount", precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "PaidAmount", precision = 14, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "SpecialRequests", columnDefinition = "NVARCHAR(MAX)")
    private String specialRequests;

    @Column(name = "CreatedBy", columnDefinition = "uniqueidentifier")
    private UUID createdBy;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (reservationID == null) reservationID = UUID.randomUUID();
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    /** Tính số đêm lưu trú */
    public long getNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /** Số tiền còn lại cần thanh toán */
    public BigDecimal getBalanceDue() {
        if (totalAmount == null) return BigDecimal.ZERO;
        BigDecimal paid = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        return totalAmount.subtract(paid);
    }

    public boolean isConfirmed() { return status == Status.CONFIRMED; }
    public boolean isCheckedIn()  { return status == Status.CHECKED_IN; }
    public boolean isCheckedOut() { return status == Status.CHECKED_OUT; }
    public boolean isCancelled()  { return status == Status.CANCELLED; }

    // Getters & Setters
    public UUID getReservationID() { return reservationID; }
    public void setReservationID(UUID v) { reservationID = v; }
    public String getReservationCode() { return reservationCode; }
    public void setReservationCode(String v) { reservationCode = v; }
    public GuestProfile getGuest() { return guest; }
    public void setGuest(GuestProfile v) { guest = v; }
    public HotelRoom getHotelRoom() { return hotelRoom; }
    public void setHotelRoom(HotelRoom v) { hotelRoom = v; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate v) { checkInDate = v; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate v) { checkOutDate = v; }
    public Integer getAdults() { return adults; }
    public void setAdults(Integer v) { adults = v; }
    public Integer getChildren() { return children; }
    public void setChildren(Integer v) { children = v; }
    public Status getStatus() { return status; }
    public void setStatus(Status v) { status = v; }
    public String getSource() { return source; }
    public void setSource(String v) { source = v; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal v) { totalAmount = v; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal v) { paidAmount = v; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String v) { specialRequests = v; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID v) { createdBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "HotelReservation{code='" + reservationCode + "', status=" + status
                + ", nights=" + getNights() + "}";
    }
}
