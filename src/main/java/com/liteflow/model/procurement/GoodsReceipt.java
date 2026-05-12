package com.liteflow.model.procurement;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "GoodsReceipts")
public class GoodsReceipt {

    @Id
    @Column(name = "ReceiptID", columnDefinition = "uniqueidentifier")
    private UUID receiptID;

    @PrePersist
    public void prePersist() {
        if (receiptID == null) receiptID = UUID.randomUUID();
    }

    @Column(name = "POID", columnDefinition = "uniqueidentifier")
    private UUID poid;

    @Column(name = "ReceivedBy", columnDefinition = "uniqueidentifier")
    private UUID receivedBy;

    @Column(name = "ReceiveDate")
    private LocalDateTime receiveDate = LocalDateTime.now();
    
    @Column(name = "Notes")
    private String notes;
    
    @Column(name = "Status")
    private String status = "PARTIAL"; // PARTIAL | FULL

    // Getters & Setters
    public UUID getReceiptID() { return receiptID; }
    public void setReceiptID(UUID receiptID) { this.receiptID = receiptID; }
    public UUID getPoid() { return poid; }
    public void setPoid(UUID poid) { this.poid = poid; }
    public UUID getReceivedBy() { return receivedBy; }
    public void setReceivedBy(UUID receivedBy) { this.receivedBy = receivedBy; }
    public LocalDateTime getReceiveDate() { return receiveDate; }
    public void setReceiveDate(LocalDateTime receiveDate) { this.receiveDate = receiveDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
