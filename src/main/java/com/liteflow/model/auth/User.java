package com.liteflow.model.auth;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Tài khoản hệ thống (authen).
 */
@Entity
@Table(name = "Users")
public class User implements Serializable {

    @Id
    @Column(name = "UserID", columnDefinition = "uniqueidentifier")
    private UUID userID;

    @Column(name = "Email", length = 320, unique = true, nullable = false)
    private String email;

    @Column(name = "Phone", length = 32, unique = true)
    private String phone;

    @Column(name = "GoogleID", length = 200)
    private String googleID;

    @Column(name = "PasswordHash", nullable = false)
    private String passwordHash;

    @Column(name = "TwoFactorSecret", length = 200)
    private String twoFactorSecret;

    @Column(name = "DisplayName", length = 200)
    private String displayName;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedAt", updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UpdatedAt")
    private Date updatedAt;

    @Lob
    @Column(name = "Meta")
    private String meta; // JSON mở rộng

    @Column(name = "Last2FAVerifiedAt")
    private LocalDateTime last2faVerifiedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserRole> userRoles = new HashSet<>();

    // ==============================
    // Lifecycle hooks
    // ==============================
    @PrePersist
    protected void onCreate() {
        if (userID == null) {
            userID = UUID.randomUUID();
        }
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // ==============================
    // Helpers
    // ==============================
    /**
     * Lấy tất cả Role đang active
     */
    public Set<Role> getActiveRoles() {
        return userRoles.stream()
                .filter(UserRole::getIsActive)
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
    }

    /**
     * Check user có role cụ thể không
     */
    public boolean hasRole(String roleName) {
        return getActiveRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(roleName));
    }

    /**
     * Lấy role chính (role đầu tiên active)
     */
    public String getPrimaryRoleName() {
        return getActiveRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse(null);
    }

    /**
     * Lấy danh sách role names để gắn vào JWT
     */
    public List<String> getRoleNames() {
        if (userRoles == null || userRoles.isEmpty()) {
            return List.of();
        }
        return userRoles.stream()
                .filter(UserRole::getIsActive)
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean isActiveSafe() {
        return Boolean.TRUE.equals(isActive);
    }

    // ==============================
    // Getters & Setters
    // ==============================
    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGoogleID() {
        return googleID;
    }

    public void setGoogleID(String googleID) {
        this.googleID = googleID;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public LocalDateTime getLast2faVerifiedAt() {
        return last2faVerifiedAt;
    }

    public void setLast2faVerifiedAt(LocalDateTime last2faVerifiedAt) {
        this.last2faVerifiedAt = last2faVerifiedAt;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    // ==============================
    // equals, hashCode, toString
    // ==============================
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(userID, user.userID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID);
    }

    @Override
    public String toString() {
        return "User{"
                + "userID=" + userID
                + ", email='" + email + '\''
                + ", displayName='" + displayName + '\''
                + ", active=" + isActive
                + '}';
    }
}
