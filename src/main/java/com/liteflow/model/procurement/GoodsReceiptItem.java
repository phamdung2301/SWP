package com.liteflow.model.procurement;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * GoodsReceiptItem - Chi tiết phiếu nhận hàng
 * CRITICAL: Tracking số lượng thực tế nhận được để đối chiếu 3-way matching
 */
@Entity
@Table(name = "GoodsReceiptItems")
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ItemID")
    private Integer itemID;

    @Column(name = "ReceiptID", columnDefinition = "uniqueidentifier")
    private UUID receiptID;

    @Column(name = "POItemID")
    private Integer poItemID;

    @Column(name = "ProductName")
    private String productName;

    @Column(name = "OrderedQuantity")
    private Integer orderedQuantity;        // Số lượng đặt (từ PO)

    @Column(name = "ReceivedQuantity")
    private Integer receivedQuantity;       // Số lượng thực nhận ⭐ CRITICAL

    @Column(name = "UnitPrice")
    private Double unitPrice;

    // Discrepancy được tính tự động từ database (computed column)
    @Column(name = "Discrepancy", insertable = false, updatable = false)
    private Integer discrepancy;            // Chênh lệch

    @Column(name = "DiscrepancyPercent", insertable = false, updatable = false)
    private Double discrepancyPercent;      // % chênh lệch

    @Column(name = "DiscrepancyReason")
    private String discrepancyReason;       // Lý do chênh lệch

    @Column(name = "QualityStatus")
    private String qualityStatus = "OK";    // OK | DEFECTIVE | DAMAGED | EXPIRED

    @Column(name = "DefectiveQuantity")
    private Integer defectiveQuantity = 0;  // Số lượng lỗi

    @Column(name = "Notes")
    private String notes;

    // Getters & Setters
    public Integer getItemID() { return itemID; }
    public void setItemID(Integer itemID) { this.itemID = itemID; }

    public UUID getReceiptID() { return receiptID; }
    public void setReceiptID(UUID receiptID) { this.receiptID = receiptID; }

    public Integer getPoItemID() { return poItemID; }
    public void setPoItemID(Integer poItemID) { this.poItemID = poItemID; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getOrderedQuantity() { return orderedQuantity; }
    public void setOrderedQuantity(Integer orderedQuantity) { this.orderedQuantity = orderedQuantity; }

    public Integer getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Integer getDiscrepancy() { return discrepancy; }

    public Double getDiscrepancyPercent() { return discrepancyPercent; }

    public String getDiscrepancyReason() { return discrepancyReason; }
    public void setDiscrepancyReason(String discrepancyReason) { this.discrepancyReason = discrepancyReason; }

    public String getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(String qualityStatus) { this.qualityStatus = qualityStatus; }

    public Integer getDefectiveQuantity() { return defectiveQuantity; }
    public void setDefectiveQuantity(Integer defectiveQuantity) { this.defectiveQuantity = defectiveQuantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Helper methods
    public boolean hasDiscrepancy() {
        return discrepancy != null && discrepancy != 0;
    }

    public boolean hasQualityIssue() {
        return !"OK".equals(qualityStatus);
    }

    public Double getTotalReceived() {
        if (receivedQuantity != null && unitPrice != null) {
            return receivedQuantity * unitPrice;
        }
        return 0.0;
    }

    public Double getTotalOrdered() {
        if (orderedQuantity != null && unitPrice != null) {
            return orderedQuantity * unitPrice;
        }
        return 0.0;
    }
}




