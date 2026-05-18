package com.liteflow.modules.reservation.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Building: Toà nhà trong khu resort.
 */
@Entity
@Table(name = "Buildings")
public class Building implements Serializable {

    @Id
    @Column(name = "BuildingID", columnDefinition = "uniqueidentifier")
    private UUID buildingID;

    @Column(name = "Code", length = 20, nullable = false, unique = true)
    private String code;

    @Column(name = "Name", length = 200, nullable = false)
    private String name;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "TotalFloors")
    private Integer totalFloors = 1;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Floor> floors = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (buildingID == null) buildingID = UUID.randomUUID();
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters & Setters
    public UUID getBuildingID() { return buildingID; }
    public void setBuildingID(UUID v) { buildingID = v; }
    public String getCode() { return code; }
    public void setCode(String v) { code = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public Integer getTotalFloors() { return totalFloors; }
    public void setTotalFloors(Integer v) { totalFloors = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { isActive = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<Floor> getFloors() { return floors; }
    public void setFloors(List<Floor> v) { floors = v; }

    @Override public String toString() {
        return "Building{code='" + code + "', name='" + name + "'}";
    }
}
