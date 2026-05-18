package com.liteflow.modules.reservation.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * GuestProfile: Hồ sơ khách hàng lưu trú.
 */
@Entity
@Table(name = "GuestProfiles")
public class GuestProfile implements Serializable {

    public enum VIPLevel { STANDARD, SILVER, GOLD, PLATINUM, DIAMOND }

    @Id
    @Column(name = "GuestID", columnDefinition = "uniqueidentifier")
    private UUID guestID;

    @Column(name = "GuestCode", length = 20, nullable = false, unique = true)
    private String guestCode;

    @Column(name = "FullName", length = 200, nullable = false)
    private String fullName;

    @Column(name = "Email", length = 320)
    private String email;

    @Column(name = "Phone", length = 30)
    private String phone;

    @Column(name = "NationalID", length = 30)
    private String nationalID;

    @Column(name = "Nationality", length = 100)
    private String nationality;

    @Column(name = "BirthDate")
    private LocalDate birthDate;

    @Column(name = "Gender", length = 10)
    private String gender;

    @Column(name = "Address", length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "VIPLevel", length = 20)
    private VIPLevel vipLevel = VIPLevel.STANDARD;

    @Column(name = "LoyaltyPoints")
    private Integer loyaltyPoints = 0;

    @Column(name = "TotalStays")
    private Integer totalStays = 0;

    @Column(name = "Notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @Column(name = "IsDeleted")
    private Boolean isDeleted = false;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuestPreference> preferences = new ArrayList<>();

    @OneToMany(mappedBy = "guest", fetch = FetchType.LAZY)
    private List<HotelReservation> reservations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (guestID == null) guestID = UUID.randomUUID();
        createdAt = updatedAt = LocalDateTime.now();
        if (loyaltyPoints == null) loyaltyPoints = 0;
        if (totalStays == null) totalStays = 0;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints = (this.loyaltyPoints == null ? 0 : this.loyaltyPoints) + points;
        recalculateVipLevel();
    }

    private void recalculateVipLevel() {
        int pts = loyaltyPoints == null ? 0 : loyaltyPoints;
        if (pts >= 50000)     vipLevel = VIPLevel.DIAMOND;
        else if (pts >= 20000) vipLevel = VIPLevel.PLATINUM;
        else if (pts >= 8000)  vipLevel = VIPLevel.GOLD;
        else if (pts >= 2000)  vipLevel = VIPLevel.SILVER;
        else                   vipLevel = VIPLevel.STANDARD;
    }

    // Getters & Setters
    public UUID getGuestID() { return guestID; }
    public void setGuestID(UUID v) { guestID = v; }
    public String getGuestCode() { return guestCode; }
    public void setGuestCode(String v) { guestCode = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = v; }
    public String getNationalID() { return nationalID; }
    public void setNationalID(String v) { nationalID = v; }
    public String getNationality() { return nationality; }
    public void setNationality(String v) { nationality = v; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate v) { birthDate = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { gender = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }
    public VIPLevel getVipLevel() { return vipLevel; }
    public void setVipLevel(VIPLevel v) { vipLevel = v; }
    public Integer getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(Integer v) { loyaltyPoints = v; }
    public Integer getTotalStays() { return totalStays; }
    public void setTotalStays(Integer v) { totalStays = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { notes = v; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean v) { isDeleted = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<GuestPreference> getPreferences() { return preferences; }
    public void setPreferences(List<GuestPreference> v) { preferences = v; }
    public List<HotelReservation> getReservations() { return reservations; }
    public void setReservations(List<HotelReservation> v) { reservations = v; }

    @Override
    public String toString() {
        return "GuestProfile{code='" + guestCode + "', name='" + fullName
                + "', vip=" + vipLevel + ", points=" + loyaltyPoints + "}";
    }
}
