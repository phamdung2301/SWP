package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Product: Sản phẩm trong hệ thống
 */
@Entity
@jakarta.persistence.Table(name = "Products")
public class Product implements Serializable {

    @Id
    @Column(name = "ProductID", columnDefinition = "uniqueidentifier")
    private UUID productId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "ImageURL", columnDefinition = "NVARCHAR(MAX)")
    private String imageUrl;

    @Column(name = "ImportDate")
    private LocalDateTime importDate;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "ProductType", length = 50)
    private String productType;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "Unit", length = 50)
    private String unit;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductVariant> productVariants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserInteraction> userInteractions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (productId == null) {
            productId = UUID.randomUUID();
        }
        if (importDate == null) {
            importDate = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    // Helper methods
    public void addProductVariant(ProductVariant variant) {
        if (productVariants == null) {
            productVariants = new ArrayList<>();
        }
        productVariants.add(variant);
        variant.setProduct(this);
    }

    public void removeProductVariant(ProductVariant variant) {
        if (productVariants != null) {
            productVariants.remove(variant);
            variant.setProduct(null);
        }
    }

    public void addProductCategory(ProductCategory productCategory) {
        if (productCategories == null) {
            productCategories = new ArrayList<>();
        }
        productCategories.add(productCategory);
        productCategory.setProduct(this);
    }

    public void removeProductCategory(ProductCategory productCategory) {
        if (productCategories != null) {
            productCategories.remove(productCategory);
            productCategory.setProduct(null);
        }
    }

    public void addUserInteraction(UserInteraction interaction) {
        if (userInteractions == null) {
            userInteractions = new ArrayList<>();
        }
        userInteractions.add(interaction);
        interaction.setProduct(this);
    }

    public void removeUserInteraction(UserInteraction interaction) {
        if (userInteractions != null) {
            userInteractions.remove(interaction);
            interaction.setProduct(null);
        }
    }

    public boolean isActive() {
        return !Boolean.TRUE.equals(isDeleted);
    }

    // Getters & Setters
    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDateTime importDate) {
        this.importDate = importDate;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<ProductVariant> getProductVariants() {
        return productVariants;
    }

    public void setProductVariants(List<ProductVariant> productVariants) {
        this.productVariants = productVariants;
    }

    public List<ProductCategory> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(List<ProductCategory> productCategories) {
        this.productCategories = productCategories;
    }

    public List<UserInteraction> getUserInteractions() {
        return userInteractions;
    }

    public void setUserInteractions(List<UserInteraction> userInteractions) {
        this.userInteractions = userInteractions;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", productType='" + productType + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
