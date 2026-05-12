package com.liteflow.model.procurement;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SupplierSLA")
public class SupplierSLA {

    @Id
    @Column(name = "SLAID", columnDefinition = "uniqueidentifier")
    private UUID slaID;

    @PrePersist
    public void prePersist() {
        if (slaID == null) slaID = UUID.randomUUID();
    }

    @Column(name = "SupplierID", columnDefinition = "uniqueidentifier")
    private UUID supplierID;

    @Column(name = "TotalOrders")
    private int totalOrders = 0;
    
    @Column(name = "OnTimeDeliveries")
    private int onTimeDeliveries = 0;
    
    @Column(name = "AvgDelayDays")
    private double avgDelayDays = 0;
    
    @Column(name = "LastEvaluated")
    private LocalDateTime lastEvaluated = LocalDateTime.now();

    // Getters & Setters
    public UUID getSlaID() { return slaID; }
    public void setSlaID(UUID slaID) { this.slaID = slaID; }
    public UUID getSupplierID() { return supplierID; }
    public void setSupplierID(UUID supplierID) { this.supplierID = supplierID; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public int getOnTimeDeliveries() { return onTimeDeliveries; }
    public void setOnTimeDeliveries(int onTimeDeliveries) { this.onTimeDeliveries = onTimeDeliveries; }
    public double getAvgDelayDays() { return avgDelayDays; }
    public void setAvgDelayDays(double avgDelayDays) { this.avgDelayDays = avgDelayDays; }
    public LocalDateTime getLastEvaluated() { return lastEvaluated; }
    public void setLastEvaluated(LocalDateTime lastEvaluated) { this.lastEvaluated = lastEvaluated; }
}
