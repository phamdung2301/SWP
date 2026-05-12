package com.liteflow.model.notice;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Notice Entity
 * Represents a notice/announcement on the employee dashboard
 */
@Entity
@Table(name = "Notices")
public class Notice {

    @Id
    @Column(name = "NoticeID", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID noticeID;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "NoticeType", length = 20)
    private String noticeType; // important, general, info, urgent

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "IsPinned")
    private Boolean isPinned;

    @Column(name = "PublishedAt")
    private LocalDateTime publishedAt;

    @Column(name = "ExpiresAt")
    private LocalDateTime expiresAt;

    @Column(name = "TargetRoles", columnDefinition = "NVARCHAR(MAX)")
    private String targetRoles;

    @Column(name = "TargetUserIDs", columnDefinition = "NVARCHAR(MAX)")
    private String targetUserIDs;

    @Column(name = "CreatedBy", columnDefinition = "UNIQUEIDENTIFIER", nullable = false)
    private UUID createdBy;

    @Transient
    private String createdByName; // Join from Users table

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "ViewCount")
    private Integer viewCount;

    @Transient
    private Boolean isRead; // For specific user

    public Notice() {
        this.noticeID = UUID.randomUUID();
        this.isActive = true;
        this.isPinned = false;
        this.publishedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.viewCount = 0;
        this.isRead = false;
    }

    public Notice(String title, String content, String noticeType) {
        this();
        this.title = title;
        this.content = content;
        this.noticeType = noticeType;
    }

    // Getters and Setters
    public UUID getNoticeID() {
        return noticeID;
    }

    public void setNoticeID(UUID noticeID) {
        this.noticeID = noticeID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getTargetRoles() {
        return targetRoles;
    }

    public void setTargetRoles(String targetRoles) {
        this.targetRoles = targetRoles;
    }

    public String getTargetUserIDs() {
        return targetUserIDs;
    }

    public void setTargetUserIDs(String targetUserIDs) {
        this.targetUserIDs = targetUserIDs;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
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

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    // Utility Methods
    public String getFormattedPublishedDate() {
        if (publishedAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return publishedAt.format(formatter);
    }

    public String getFormattedPublishedDateTime() {
        if (publishedAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return publishedAt.format(formatter);
    }

    public boolean isExpired() {
        if (expiresAt == null) return false;
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public String getNoticeTypeBadgeClass() {
        if (noticeType == null) return "general";
        return noticeType.toLowerCase();
    }

    public String getNoticeTypeLabel() {
        if (noticeType == null) return "Chung";

        switch (noticeType.toLowerCase()) {
            case "important":
                return "Quan trọng";
            case "urgent":
                return "Khẩn cấp";
            case "info":
                return "Thông tin";
            case "general":
            default:
                return "Chung";
        }
    }

    @Override
    public String toString() {
        return "Notice{" +
                "noticeID=" + noticeID +
                ", title='" + title + '\'' +
                ", noticeType='" + noticeType + '\'' +
                ", isPinned=" + isPinned +
                ", publishedAt=" + publishedAt +
                '}';
    }
}
