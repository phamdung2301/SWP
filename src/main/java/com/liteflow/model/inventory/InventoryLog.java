package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * InventoryLog: Lịch sử thay đổi tồn kho
 */
@Entity
@jakarta.persistence.Table(name = "InventoryLogs")
public class InventoryLog implements Serializable {

    @Id
    @Column(name = "LogID", columnDefinition = "uniqueidentifier")
    private UUID logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductVariantID", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "ActionType", length = 10, nullable = false)
    private String actionType; // IN, OUT, ADJUST

    @Column(name = "QuantityChanged", nullable = false)
    private Integer quantityChanged;

    @Column(name = "ActionDate")
    private LocalDateTime actionDate;

    @Column(name = "StoreLocation", length = 100)
    private String storeLocation = "Main Warehouse";

    @PrePersist
    protected void onCreate() {
        if (logId == null) {
            logId = UUID.randomUUID();
        }
        if (actionDate == null) {
            actionDate = LocalDateTime.now();
        }
        if (storeLocation == null) {
            storeLocation = "Main Warehouse";
        }
    }

    // Helper methods
    public boolean isInbound() {
        return "IN".equalsIgnoreCase(actionType);
    }

    public boolean isOutbound() {
        return "OUT".equalsIgnoreCase(actionType);
    }

    public boolean isAdjustment() {
        return "ADJUST".equalsIgnoreCase(actionType);
    }

    public boolean isIncrease() {
        return quantityChanged != null && quantityChanged > 0;
    }

    public boolean isDecrease() {
        return quantityChanged != null && quantityChanged < 0;
    }

    // Getters & Setters
    public UUID getLogId() {
        return logId;
    }

    public void setLogId(UUID logId) {
        this.logId = logId;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Integer getQuantityChanged() {
        return quantityChanged;
    }

    public void setQuantityChanged(Integer quantityChanged) {
        this.quantityChanged = quantityChanged;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    @Override
    public String toString() {
        return "InventoryLog{" +
                "logId=" + logId +
                ", actionType='" + actionType + '\'' +
                ", quantityChanged=" + quantityChanged +
                ", actionDate=" + actionDate +
                '}';
    }
}
