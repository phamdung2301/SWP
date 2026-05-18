package com.liteflow.modules.reservation.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * RoomType: Loại phòng (Standard, Deluxe, Suite, Villa...).
 */
@Entity
@Table(name = "RoomTypes")
public class RoomType implements Serializable {

    @Id
    @Column(name = "RoomTypeID", columnDefinition = "uniqueidentifier")
    private UUID roomTypeID;

    @Column(name = "Code", length = 20, nullable = false, unique = true)
    private String code;

    @Column(name = "Name", length = 100, nullable = false)
    private String name;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "BasePrice", precision = 14, scale = 2, nullable = false)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "MaxOccupancy", nullable = false)
    private Integer maxOccupancy = 2;

    @Column(name = "SizeM2", precision = 8, scale = 2)
    private BigDecimal sizeM2;

    @Column(name = "Amenities", columnDefinition = "NVARCHAR(MAX)")
    private String amenities;

    @Column(name = "ImageURL", columnDefinition = "NVARCHAR(MAX)")
    private String imageURL;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "roomType", fetch = FetchType.LAZY)
    private List<HotelRoom> rooms = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (roomTypeID == null) roomTypeID = UUID.randomUUID();
    }

    // Getters & Setters
    public UUID getRoomTypeID() { return roomTypeID; }
    public void setRoomTypeID(UUID v) { roomTypeID = v; }
    public String getCode() { return code; }
    public void setCode(String v) { code = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal v) { basePrice = v; }
    public Integer getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(Integer v) { maxOccupancy = v; }
    public BigDecimal getSizeM2() { return sizeM2; }
    public void setSizeM2(BigDecimal v) { sizeM2 = v; }
    public String getAmenities() { return amenities; }
    public void setAmenities(String v) { amenities = v; }
    public String getImageURL() { return imageURL; }
    public void setImageURL(String v) { imageURL = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { isActive = v; }
    public List<HotelRoom> getRooms() { return rooms; }
    public void setRooms(List<HotelRoom> v) { rooms = v; }
}
