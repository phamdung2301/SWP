package com.liteflow.modules.reservation.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RoomStatusHistory")
public class RoomStatusHistory implements Serializable {

    @Id
    @Column(name = "HistoryID", columnDefinition = "uniqueidentifier")
    private UUID historyID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HotelRoomID", nullable = false)
    private HotelRoom hotelRoom;

    @Column(name = "OldStatus", length = 30)
    private String oldStatus;

    @Column(name = "NewStatus", length = 30, nullable = false)
    private String newStatus;

    @Column(name = "Reason", columnDefinition = "NVARCHAR(500)")
    private String reason;

    @Column(name = "ChangedBy", columnDefinition = "uniqueidentifier")
    private UUID changedBy;

    @Column(name = "ChangedAt")
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        if (historyID == null) historyID = UUID.randomUUID();
        if (changedAt == null) changedAt = LocalDateTime.now();
    }

    public RoomStatusHistory() {}

    // Getters & Setters
    public UUID getHistoryID() { return historyID; }
    public void setHistoryID(UUID historyID) { this.historyID = historyID; }
    public HotelRoom getHotelRoom() { return hotelRoom; }
    public void setHotelRoom(HotelRoom hotelRoom) { this.hotelRoom = hotelRoom; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public UUID getChangedBy() { return changedBy; }
    public void setChangedBy(UUID changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
