package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@jakarta.persistence.Table(name = "Reservations")
public class Reservation {

    @Id
    @Column(name = "ReservationID", columnDefinition = "uniqueidentifier")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID reservationId;

    @Column(name = "ReservationCode", nullable = false, unique = true, length = 20)
    private String reservationCode;

    @Column(name = "CustomerName", nullable = false, length = 100)
    private String customerName;

    @Column(name = "CustomerPhone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "CustomerEmail", length = 100)
    private String customerEmail;

    @Column(name = "ArrivalTime", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "NumberOfGuests", nullable = false)
    private Integer numberOfGuests;

    @ManyToOne
    @JoinColumn(name = "TableID")
    private Table table;

    @ManyToOne
    @JoinColumn(name = "RoomID")
    private Room room;

    // Deposit removed

    @Column(name = "Status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "Notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ReservationItem> reservationItems = new ArrayList<>();

    // Constructors
    public Reservation() {
        this.status = "PENDING";
        this.reservationItems = new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        if (reservationId == null) {
            reservationId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    // Deposit removed

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ReservationItem> getReservationItems() {
        return reservationItems;
    }

    public void setReservationItems(List<ReservationItem> reservationItems) {
        this.reservationItems = reservationItems;
    }

    // Helper methods
    public void addReservationItem(ReservationItem item) {
        if (reservationItems == null) {
            reservationItems = new ArrayList<>();
        }
        reservationItems.add(item);
        item.setReservation(this);
    }

    public void removeReservationItem(ReservationItem item) {
        if (reservationItems != null) {
            reservationItems.remove(item);
            item.setReservation(null);
        }
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equals(status);
    }

    public boolean isSeated() {
        return "SEATED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isNoShow() {
        return "NO_SHOW".equals(status);
    }

    public boolean hasTable() {
        return table != null;
    }

    public boolean isOverdue(int minutesThreshold) {
        if (arrivalTime == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesThreshold);
        return arrivalTime.isBefore(threshold) && (isPending() || isConfirmed());
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", reservationCode='" + reservationCode + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", numberOfGuests=" + numberOfGuests +
                ", status='" + status + '\'' +
                '}';
    }
}
