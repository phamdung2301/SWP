package com.liteflow.model.procurement;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Suppliers")
public class Supplier implements Serializable {

    @Id
    @Column(name = "SupplierID", columnDefinition = "uniqueidentifier")
    private UUID supplierID;

    @PrePersist
    public void prePersist() {
        if (supplierID == null) supplierID = UUID.randomUUID();
    }

    @Column(name = "Name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "Contact")
    private String contact;
    
    @Column(name = "Email")
    private String email;
    
    @Column(name = "Phone")
    private String phone;
    
    @Column(name = "Address")
    private String address;

    @Column(name = "TaxCode", length = 50)
    private String taxCode;

    @Column(name = "Rating")
    private Double rating = 0.0;      // 0–5 stars
    
    @Column(name = "OnTimeRate")
    private Double onTimeRate = 0.0;  // %
    
    @Column(name = "DefectRate")
    private Double defectRate = 0.0;  // %

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CreatedBy", columnDefinition = "uniqueidentifier")
    private UUID createdBy;

    // Getters & Setters
    public UUID getSupplierID() { return supplierID; }
    public void setSupplierID(UUID supplierID) { this.supplierID = supplierID; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Double getOnTimeRate() { return onTimeRate; }
    public void setOnTimeRate(Double onTimeRate) { this.onTimeRate = onTimeRate; }
    public Double getDefectRate() { return defectRate; }
    public void setDefectRate(Double defectRate) { this.defectRate = defectRate; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}
