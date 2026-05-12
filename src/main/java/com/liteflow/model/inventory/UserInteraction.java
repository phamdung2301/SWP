package com.liteflow.model.inventory;

import com.liteflow.model.auth.User;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserInteraction: Lưu lại tương tác của user với sản phẩm
 */
@Entity
@jakarta.persistence.Table(name = "UserInteractions")
public class UserInteraction implements Serializable {

    @Id
    @Column(name = "InteractionID", columnDefinition = "uniqueidentifier")
    private UUID interactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @Column(name = "InteractionType", length = 50, nullable = false)
    private String interactionType;

    @Column(name = "InteractionTime")
    private LocalDateTime interactionTime;

    @PrePersist
    protected void onCreate() {
        if (interactionId == null) {
            interactionId = UUID.randomUUID();
        }
        if (interactionTime == null) {
            interactionTime = LocalDateTime.now();
        }
    }

    // Helper methods
    public boolean isView() {
        return "VIEW".equalsIgnoreCase(interactionType);
    }

    public boolean isLike() {
        return "LIKE".equalsIgnoreCase(interactionType);
    }

    public boolean isAddToCart() {
        return "ADD_TO_CART".equalsIgnoreCase(interactionType);
    }

    public boolean isPurchase() {
        return "PURCHASE".equalsIgnoreCase(interactionType);
    }

    public boolean isReview() {
        return "REVIEW".equalsIgnoreCase(interactionType);
    }

    // Getters & Setters
    public UUID getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(UUID interactionId) {
        this.interactionId = interactionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public LocalDateTime getInteractionTime() {
        return interactionTime;
    }

    public void setInteractionTime(LocalDateTime interactionTime) {
        this.interactionTime = interactionTime;
    }

    @Override
    public String toString() {
        return "UserInteraction{" +
                "interactionId=" + interactionId +
                ", interactionType='" + interactionType + '\'' +
                ", interactionTime=" + interactionTime +
                '}';
    }
}
