package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@jakarta.persistence.Table(name = "ReservationItems")
public class ReservationItem {

    @Id
    @Column(name = "ReservationItemID", columnDefinition = "uniqueidentifier")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID reservationItemId;

    @ManyToOne
    @JoinColumn(name = "ReservationID", nullable = false)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "Note", length = 255)
    private String note;

    // Constructors
    public ReservationItem() {
        this.quantity = 1;
    }

    public ReservationItem(Reservation reservation, Product product, Integer quantity) {
        this.reservation = reservation;
        this.product = product;
        this.quantity = quantity;
    }

    @PrePersist
    protected void onCreate() {
        if (reservationItemId == null) {
            reservationItemId = UUID.randomUUID();
        }
        if (quantity == null) {
            quantity = 1;
        }
    }

    // Getters and Setters
    public UUID getReservationItemId() {
        return reservationItemId;
    }

    public void setReservationItemId(UUID reservationItemId) {
        this.reservationItemId = reservationItemId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
        return "ReservationItem{" +
                "reservationItemId=" + reservationItemId +
                ", product=" + (product != null ? product.getName() : "null") +
                ", quantity=" + quantity +
                ", note='" + note + '\'' +
                '}';
    }
}

