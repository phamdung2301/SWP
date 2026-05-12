package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Category: Danh mục sản phẩm
 */
@Entity
@jakarta.persistence.Table(name = "Categories")
public class Category implements Serializable {

    @Id
    @Column(name = "CategoryID", columnDefinition = "uniqueidentifier")
    private UUID categoryId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (categoryId == null) {
            categoryId = UUID.randomUUID();
        }
    }

    // Helper methods
    public void addProductCategory(ProductCategory productCategory) {
        if (productCategories == null) {
            productCategories = new ArrayList<>();
        }
        productCategories.add(productCategory);
        productCategory.setCategory(this);
    }

    public void removeProductCategory(ProductCategory productCategory) {
        if (productCategories != null) {
            productCategories.remove(productCategory);
            productCategory.setCategory(null);
        }
    }

    // Getters & Setters
    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
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

    public List<ProductCategory> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(List<ProductCategory> productCategories) {
        this.productCategories = productCategories;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                '}';
    }
}
