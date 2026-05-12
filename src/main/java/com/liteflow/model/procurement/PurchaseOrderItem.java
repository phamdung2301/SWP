package com.liteflow.model.procurement;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "PurchaseOrderItems")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int itemID;

    @Column(name = "POID", columnDefinition = "uniqueidentifier")
    private UUID poid;

    @Column(name = "ItemName")
    private String itemName;
    
    @Column(name = "Quantity")
    private int quantity;
    
    @Column(name = "UnitPrice")
    private double unitPrice;

    // Getters & Setters
    public int getItemID() { return itemID; }
    public void setItemID(int itemID) { this.itemID = itemID; }
    public UUID getPoid() { return poid; }
    public void setPoid(UUID poid) { this.poid = poid; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}
