package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * ProductCategory: Liên kết sản phẩm với danh mục
 */
@Entity
@jakarta.persistence.Table(name = "ProductsCategories")
public class ProductCategory implements Serializable {

    @Id
    @Column(name = "ProductCategoryID", columnDefinition = "uniqueidentifier")
    private UUID productCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    @PrePersist
    protected void onCreate() {
        if (productCategoryId == null) {
            productCategoryId = UUID.randomUUID();
        }
    }

    // Getters & Setters
    public UUID getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(UUID productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "ProductCategory{" +
                "productCategoryId=" + productCategoryId +
                ", product=" + (product != null ? product.getName() : "null") +
                ", category=" + (category != null ? category.getName() : "null") +
                '}';
    }
}
