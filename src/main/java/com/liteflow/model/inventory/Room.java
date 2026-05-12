package com.liteflow.model.inventory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Room: Phòng trong quán cafe
 */
@Entity
@jakarta.persistence.Table(name = "Rooms")
public class Room implements Serializable {

    @Id
    @Column(name = "RoomID", columnDefinition = "uniqueidentifier")
    private UUID roomId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "TableCount")
    private Integer tableCount = 0;

    @Column(name = "TotalCapacity")
    private Integer totalCapacity = 0;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Table> tables = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (roomId == null) {
            roomId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Room() {
        this.tables = new ArrayList<>();
    }

    // Helper methods
    public void addTable(Table table) {
        if (tables == null) {
            tables = new ArrayList<>();
        }
        tables.add(table);
        table.setRoom(this);
    }

    public void removeTable(Table table) {
        if (tables != null) {
            tables.remove(table);
            table.setRoom(null);
        }
    }

    public int getActualTableCount() {
        return tables != null ? tables.size() : 0;
    }

    public int getAvailableTableCount() {
        if (tables == null) return 0;
        return (int) tables.stream()
                .filter(Table::isAvailable)
                .count();
    }

    public int getOccupiedTableCount() {
        if (tables == null) return 0;
        return (int) tables.stream()
                .filter(Table::isOccupied)
                .count();
    }

    // Getters & Setters
    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getTableCount() {
        return tableCount != null ? tableCount : 0;
    }

    public void setTableCount(Integer tableCount) {
        this.tableCount = tableCount;
    }

    public Integer getTotalCapacity() {
        return totalCapacity != null ? totalCapacity : 0;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", name='" + name + '\'' +
                ", tableCount=" + getTableCount() +
                '}';
    }
}
