package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ProductVariant: Biến thể sản phẩm (size, giá)
 */
@Entity
@jakarta.persistence.Table(name = "ProductVariant")
public class ProductVariant implements Serializable {

    @Id
    @Column(name = "ProductVariantID", columnDefinition = "uniqueidentifier")
    private UUID productVariantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @Column(name = "Size", length = 50, nullable = false)
    private String size;

    @Column(name = "OriginalPrice", precision = 10, scale = 2, nullable = false)
    private BigDecimal originalPrice = BigDecimal.ZERO;

    @Column(name = "Price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "DiscountPrice", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "DiscountExpiry")
    private LocalDateTime discountExpiry;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductStock> productStocks = new ArrayList<>();

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryLog> inventoryLogs = new ArrayList<>();

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (productVariantId == null) {
            productVariantId = UUID.randomUUID();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    // Helper methods
    public void addProductStock(ProductStock stock) {
        if (productStocks == null) {
            productStocks = new ArrayList<>();
        }
        productStocks.add(stock);
        stock.setProductVariant(this);
    }

    public void removeProductStock(ProductStock stock) {
        if (productStocks != null) {
            productStocks.remove(stock);
            stock.setProductVariant(null);
        }
    }

    public void addInventoryLog(InventoryLog log) {
        if (inventoryLogs == null) {
            inventoryLogs = new ArrayList<>();
        }
        inventoryLogs.add(log);
        log.setProductVariant(this);
    }

    public void removeInventoryLog(InventoryLog log) {
        if (inventoryLogs != null) {
            inventoryLogs.remove(log);
            log.setProductVariant(null);
        }
    }

    public void addOrderDetail(OrderDetail orderDetail) {
        if (orderDetails == null) {
            orderDetails = new ArrayList<>();
        }
        orderDetails.add(orderDetail);
        orderDetail.setProductVariant(this);
    }

    public void removeOrderDetail(OrderDetail orderDetail) {
        if (orderDetails != null) {
            orderDetails.remove(orderDetail);
            orderDetail.setProductVariant(null);
        }
    }

    public boolean isActive() {
        return !Boolean.TRUE.equals(isDeleted);
    }

    public boolean hasDiscount() {
        return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isDiscountExpired() {
        return discountExpiry != null && LocalDateTime.now().isAfter(discountExpiry);
    }

    public BigDecimal getCurrentPrice() {
        if (hasDiscount() && !isDiscountExpired()) {
            return discountPrice;
        }
        return price;
    }

    // Getters & Setters
    public UUID getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(UUID productVariantId) {
        this.productVariantId = productVariantId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(BigDecimal discountPrice) {
        this.discountPrice = discountPrice;
    }

    public LocalDateTime getDiscountExpiry() {
        return discountExpiry;
    }

    public void setDiscountExpiry(LocalDateTime discountExpiry) {
        this.discountExpiry = discountExpiry;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public List<ProductStock> getProductStocks() {
        return productStocks;
    }

    public void setProductStocks(List<ProductStock> productStocks) {
        this.productStocks = productStocks;
    }

    public List<InventoryLog> getInventoryLogs() {
        return inventoryLogs;
    }

    public void setInventoryLogs(List<InventoryLog> inventoryLogs) {
        this.inventoryLogs = inventoryLogs;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    @Override
    public String toString() {
        return "ProductVariant{" +
                "productVariantId=" + productVariantId +
                ", size='" + size + '\'' +
                ", price=" + price +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
