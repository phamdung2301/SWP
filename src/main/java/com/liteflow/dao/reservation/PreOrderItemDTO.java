package com.liteflow.dao.reservation;

import java.util.UUID;

/**
 * Data Transfer Object for Pre-ordered Items in a Reservation
 */
public class PreOrderItemDTO {
    private UUID productId;
    private Integer quantity;
    private String note;

    // Constructors
    public PreOrderItemDTO() {
    }

    public PreOrderItemDTO(UUID productId, Integer quantity, String note) {
        this.productId = productId;
        this.quantity = quantity;
        this.note = note;
    }

    // Getters and Setters
    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "PreOrderItemDTO{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", note='" + note + '\'' +
                '}';
    }
}

