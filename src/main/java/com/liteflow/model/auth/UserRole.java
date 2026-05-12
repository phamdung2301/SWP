package com.liteflow.model.auth;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserRole: Liên kết User - Role, có thêm metadata (assignedAt, assignedBy).
 */
@Entity
@Table(name = "UserRoles")
@IdClass(UserRoleId.class)
public class UserRole implements Serializable {

    @Id
    @Column(name = "UserID", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "RoleID", nullable = false)
    private UUID roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoleID", insertable = false, updatable = false)
    private Role role;

    @Column(name = "AssignedAt", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "AssignedBy")
    private UUID assignedBy;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    // ==========================
    // Constructors
    // ==========================
    public UserRole() {
    }

    public UserRole(UUID userId, UUID roleId) {
        this.userId = userId;
        this.roleId = roleId;
        this.assignedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // ==========================
    // Getters & Setters
    // ==========================
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public UUID getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(UUID assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
