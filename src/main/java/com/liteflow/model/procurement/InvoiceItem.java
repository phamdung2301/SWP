package com.liteflow.model.procurement;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * InvoiceItem - Chi tiết hóa đơn
 * CRITICAL: Tracking từng sản phẩm trong hóa đơn để đối chiếu item-level
 */
@Entity
@Table(name = "InvoiceItems")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ItemID")
    private Integer itemID;

    @Column(name = "InvoiceID", columnDefinition = "uniqueidentifier")
    private UUID invoiceID;

    @Column(name = "POItemID")
    private Integer poItemID;               // Link với PO Item (NULL nếu manual invoice)

    @Column(name = "ProductName")
    private String productName;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "UnitPrice")
    private Double unitPrice;

    // Total được tính tự động từ database (computed column)
    @Column(name = "Total", insertable = false, updatable = false)
    private Double total;

    @Column(name = "Matched")
    private Boolean matched = false;        // Item level matching

    @Column(name = "DiscrepancyAmount")
    private Double discrepancyAmount = 0.0; // Chênh lệch số tiền với PO

    @Column(name = "DiscrepancyQuantity")
    private Integer discrepancyQuantity = 0; // Chênh lệch số lượng với GR

    @Column(name = "MatchNote")
    private String matchNote;

    // Getters & Setters
    public Integer getItemID() { return itemID; }
    public void setItemID(Integer itemID) { this.itemID = itemID; }

    public UUID getInvoiceID() { return invoiceID; }
    public void setInvoiceID(UUID invoiceID) { this.invoiceID = invoiceID; }

    public Integer getPoItemID() { return poItemID; }
    public void setPoItemID(Integer poItemID) { this.poItemID = poItemID; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Double getTotal() { return total; }

    public Boolean getMatched() { return matched; }
    public void setMatched(Boolean matched) { this.matched = matched; }

    public Double getDiscrepancyAmount() { return discrepancyAmount; }
    public void setDiscrepancyAmount(Double discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; }

    public Integer getDiscrepancyQuantity() { return discrepancyQuantity; }
    public void setDiscrepancyQuantity(Integer discrepancyQuantity) { this.discrepancyQuantity = discrepancyQuantity; }

    public String getMatchNote() { return matchNote; }
    public void setMatchNote(String matchNote) { this.matchNote = matchNote; }

    // Helper methods
    public boolean hasDiscrepancy() {
        return (discrepancyAmount != null && Math.abs(discrepancyAmount) > 0.01) ||
               (discrepancyQuantity != null && discrepancyQuantity != 0);
    }

    public boolean isLinkedToPO() {
        return poItemID != null;
    }

    public Double calculateTotal() {
        if (quantity != null && unitPrice != null) {
            return quantity * unitPrice;
        }
        return 0.0;
    }
}




