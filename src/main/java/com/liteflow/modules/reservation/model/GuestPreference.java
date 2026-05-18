package com.liteflow.modules.reservation.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "GuestPreferences")
public class GuestPreference implements Serializable {

    @Id
    @Column(name = "PreferenceID", columnDefinition = "uniqueidentifier")
    private UUID preferenceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GuestID", nullable = false)
    private GuestProfile guest;

    @Column(name = "Category", length = 50)
    private String category; // ROOM, FOOD, SERVICE, OTHER

    @Column(name = "PreferenceValue", columnDefinition = "NVARCHAR(500)")
    private String preferenceValue;

    @Column(name = "Notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (preferenceID == null) preferenceID = UUID.randomUUID();
    }

    public GuestPreference() {}

    // Getters & Setters
    public UUID getPreferenceID() { return preferenceID; }
    public void setPreferenceID(UUID preferenceID) { this.preferenceID = preferenceID; }
    public GuestProfile getGuest() { return guest; }
    public void setGuest(GuestProfile guest) { this.guest = guest; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPreferenceValue() { return preferenceValue; }
    public void setPreferenceValue(String preferenceValue) { this.preferenceValue = preferenceValue; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
