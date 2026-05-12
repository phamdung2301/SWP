package com.liteflow.model.inventory;

import java.util.UUID;

/**
 * DTO for Low Stock Items
 * Used for inventory alert system
 */
public class LowStockItem {
    
    private UUID productID;
    private UUID productVariantID;
    private String productName;
    private String size;
    private int currentStock;
    private int threshold;
    
    /**
     * Constructor for JPQL query projection
     */
    public LowStockItem(UUID productID, UUID productVariantID, String productName, 
                        String size, int currentStock) {
        this.productID = productID;
        this.productVariantID = productVariantID;
        this.productName = productName;
        this.size = size;
        this.currentStock = currentStock;
    }
    
    /**
     * Full constructor with threshold
     */
    public LowStockItem(UUID productID, UUID productVariantID, String productName, 
                        String size, int currentStock, int threshold) {
        this.productID = productID;
        this.productVariantID = productVariantID;
        this.productName = productName;
        this.size = size;
        this.currentStock = currentStock;
        this.threshold = threshold;
    }
    
    // Getters and Setters
    
    public UUID getProductID() {
        return productID;
    }
    
    public void setProductID(UUID productID) {
        this.productID = productID;
    }
    
    public UUID getProductVariantID() {
        return productVariantID;
    }
    
    public void setProductVariantID(UUID productVariantID) {
        this.productVariantID = productVariantID;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public int getCurrentStock() {
        return currentStock;
    }
    
    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }
    
    public int getThreshold() {
        return threshold;
    }
    
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
    
    /**
     * Get full display name (Product + Size)
     */
    public String getFullName() {
        return productName + " (" + size + ")";
    }
    
    /**
     * Check if stock is critically low (< 50% of threshold)
     */
    public boolean isCritical() {
        return threshold > 0 && currentStock < (threshold * 0.5);
    }
    
    /**
     * Get stock percentage relative to threshold
     */
    public double getStockPercentage() {
        if (threshold <= 0) return 100.0;
        return (double) currentStock / threshold * 100.0;
    }
    
    @Override
    public String toString() {
        return "LowStockItem{" +
                "productName='" + productName + '\'' +
                ", size='" + size + '\'' +
                ", currentStock=" + currentStock +
                ", threshold=" + threshold +
                '}';
    }
}


