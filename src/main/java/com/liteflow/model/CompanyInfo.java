package com.liteflow.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CompanyInfo - Thông tin công ty LiteFlow
 * Singleton pattern: chỉ có 1 record trong database
 * Note: TaxCode không lưu trong DB, được lấy từ .env file
 */
@Entity
@Table(name = "CompanyInfo")
public class CompanyInfo implements Serializable {

    @Id
    @Column(name = "CompanyID", columnDefinition = "uniqueidentifier")
    private UUID companyID;

    @PrePersist
    public void prePersist() {
        if (companyID == null) {
            companyID = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    @Column(name = "Address", length = 500)
    private String address;

    @Column(name = "Phone", length = 50)
    private String phone;

    @Column(name = "Email", length = 150)
    private String email;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    // Getters & Setters
    public UUID getCompanyID() {
        return companyID;
    }

    public void setCompanyID(UUID companyID) {
        this.companyID = companyID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

