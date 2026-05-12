package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * ProductStock: Tồn kho sản phẩm
 */
@Entity
@jakarta.persistence.Table(name = "ProductStock")
public class ProductStock implements Serializable {

    @Id
    @Column(name = "ProductStockID", columnDefinition = "uniqueidentifier")
    private UUID productStockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductVariantID", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InventoryID", nullable = false)
    private Inventory inventory;

    @Column(name = "Amount", nullable = false)
    private Integer amount = 0;

    @PrePersist
    protected void onCreate() {
        if (productStockId == null) {
            productStockId = UUID.randomUUID();
        }
        if (amount == null) {
            amount = 0;
        }
    }

    // Helper methods
    public boolean isInStock() {
        return amount != null && amount > 0;
    }

    public boolean isOutOfStock() {
        return amount == null || amount <= 0;
    }

    public void increaseStock(int quantity) {
        if (amount == null) {
            amount = 0;
        }
        amount += quantity;
    }

    public void decreaseStock(int quantity) {
        if (amount == null) {
            amount = 0;
        }
        amount = Math.max(0, amount - quantity);
    }

    // Getters & Setters
    public UUID getProductStockId() {
        return productStockId;
    }

    public void setProductStockId(UUID productStockId) {
        this.productStockId = productStockId;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "ProductStock{" +
                "productStockId=" + productStockId +
                ", amount=" + amount +
                '}';
    }
}
