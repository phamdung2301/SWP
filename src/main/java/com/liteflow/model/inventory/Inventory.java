package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Inventory: Kho hàng
 */
@Entity
@jakarta.persistence.Table(name = "Inventory")
public class Inventory implements Serializable {

    @Id
    @Column(name = "InventoryID", columnDefinition = "uniqueidentifier")
    private UUID inventoryId;

    @Column(name = "StoreLocation", length = 100)
    private String storeLocation = "Main Warehouse";

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductStock> productStocks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (inventoryId == null) {
            inventoryId = UUID.randomUUID();
        }
        if (storeLocation == null) {
            storeLocation = "Main Warehouse";
        }
    }

    // Helper methods
    public void addProductStock(ProductStock stock) {
        if (productStocks == null) {
            productStocks = new ArrayList<>();
        }
        productStocks.add(stock);
        stock.setInventory(this);
    }

    public void removeProductStock(ProductStock stock) {
        if (productStocks != null) {
            productStocks.remove(stock);
            stock.setInventory(null);
        }
    }

    // Getters & Setters
    public UUID getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(UUID inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    public List<ProductStock> getProductStocks() {
        return productStocks;
    }

    public void setProductStocks(List<ProductStock> productStocks) {
        this.productStocks = productStocks;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId=" + inventoryId +
                ", storeLocation='" + storeLocation + '\'' +
                '}';
    }
}
