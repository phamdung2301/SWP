package com.liteflow.model.procurement;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PurchaseOrders")
public class PurchaseOrder {

    @Id
    @Column(name = "POID", columnDefinition = "uniqueidentifier")
    private UUID poid;

    @PrePersist
    public void prePersist() {
        if (poid == null) poid = UUID.randomUUID();
    }

    @Column(name = "SupplierID", columnDefinition = "uniqueidentifier")
    private UUID supplierID;

    @Column(name = "CreatedBy", columnDefinition = "uniqueidentifier")
    private UUID createdBy;

    @Column(name = "ApprovedBy", columnDefinition = "uniqueidentifier")
    private UUID approvedBy;

    @Column(name = "CreateDate")
    private LocalDateTime createDate = LocalDateTime.now();
    
    @Column(name = "ExpectedDelivery")
    private LocalDateTime expectedDelivery;
    
    @Column(name = "TotalAmount")
    private Double totalAmount = 0.0;

    @Column(name = "Status", length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, RECEIVING, COMPLETED

    @Column(name = "ApprovalLevel")
    private Integer approvalLevel = 1;
    
    @Column(name = "ApprovedAt")
    private LocalDateTime approvedAt;
    
    @Column(name = "Notes")
    private String notes;

    // Getters & Setters
    public UUID getPoid() { return poid; }
    public void setPoid(UUID poid) { this.poid = poid; }
    public UUID getSupplierID() { return supplierID; }
    public void setSupplierID(UUID supplierID) { this.supplierID = supplierID; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public LocalDateTime getExpectedDelivery() { return expectedDelivery; }
    public void setExpectedDelivery(LocalDateTime expectedDelivery) { this.expectedDelivery = expectedDelivery; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getApprovalLevel() { return approvalLevel; }
    public void setApprovalLevel(Integer approvalLevel) { this.approvalLevel = approvalLevel; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
