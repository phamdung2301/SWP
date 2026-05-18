package com.liteflow.modules.reservation.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Floor: Tầng thuộc một toà nhà.
 */
@Entity
@Table(name = "Floors")
public class Floor implements Serializable {

    @Id
    @Column(name = "FloorID", columnDefinition = "uniqueidentifier")
    private UUID floorID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BuildingID", nullable = false)
    private Building building;

    @Column(name = "FloorNumber", nullable = false)
    private Integer floorNumber;

    @Column(name = "Name", length = 100)
    private String name;

    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HotelRoom> rooms = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (floorID == null) floorID = UUID.randomUUID();
    }

    // Getters & Setters
    public UUID getFloorID() { return floorID; }
    public void setFloorID(UUID v) { floorID = v; }
    public Building getBuilding() { return building; }
    public void setBuilding(Building v) { building = v; }
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer v) { floorNumber = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public List<HotelRoom> getRooms() { return rooms; }
    public void setRooms(List<HotelRoom> v) { rooms = v; }
}
