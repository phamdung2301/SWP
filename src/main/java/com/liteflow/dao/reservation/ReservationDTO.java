package com.liteflow.dao.reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Reservation
 * Used to transfer data between Controller, Service, and View layers
 */
public class ReservationDTO {
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDateTime arrivalTime;
    private Integer numberOfGuests;
    private UUID roomId;
    private UUID tableId;
    private BigDecimal depositAmount;
    private String notes;
    private List<PreOrderItemDTO> preOrderedItems;

    // Constructors
    public ReservationDTO() {
    }

    // Getters and Setters
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

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public UUID getTableId() {
        return tableId;
    }

    public void setTableId(UUID tableId) {
        this.tableId = tableId;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PreOrderItemDTO> getPreOrderedItems() {
        return preOrderedItems;
    }

    public void setPreOrderedItems(List<PreOrderItemDTO> preOrderedItems) {
        this.preOrderedItems = preOrderedItems;
    }

    @Override
    public String toString() {
        return "ReservationDTO{" +
                "customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", numberOfGuests=" + numberOfGuests +
                '}';
    }
}

