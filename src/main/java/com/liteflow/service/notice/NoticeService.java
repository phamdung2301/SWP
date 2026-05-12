package com.liteflow.service.notice;

import com.liteflow.dao.notice.NoticeDAO;
import com.liteflow.model.notice.Notice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for Notice operations
 */
public class NoticeService {

    private final NoticeDAO noticeDAO;

    public NoticeService() {
        this.noticeDAO = new NoticeDAO();
    }

    /**
     * Create a new notice
     */
    public UUID createNotice(String title, String content, String noticeType,
                            boolean isPinned, LocalDateTime expiresAt, UUID createdBy) {

        Notice notice = new Notice(title, content, noticeType);
        notice.setIsPinned(isPinned);
        notice.setExpiresAt(expiresAt);
        notice.setCreatedBy(createdBy);

        UUID noticeID = noticeDAO.createNotice(notice);

        if (noticeID != null) {
            System.out.println("✅ Notice created successfully: " + title);
        } else {
            System.err.println("❌ Failed to create notice: " + title);
        }

        return noticeID;
    }

    /**
     * Get active notices for employee dashboard
     */
    public List<Notice> getActiveNoticesForEmployee(UUID userID, int limit) {
        return noticeDAO.getActiveNotices(userID, limit);
    }

    /**
     * Get all active notices (admin view)
     */
    public List<Notice> getAllActiveNotices() {
        return noticeDAO.getAllActiveNotices();
    }

    /**
     * Get notice by ID
     */
    public Notice getNoticeByID(UUID noticeID) {
        return noticeDAO.getNoticeByID(noticeID);
    }

    /**
     * Update notice
     */
    public boolean updateNotice(Notice notice) {
        return noticeDAO.updateNotice(notice);
    }

    /**
     * Delete notice (soft delete)
     */
    public boolean deleteNotice(UUID noticeID) {
        return noticeDAO.deleteNotice(noticeID);
    }

    /**
     * Mark notice as read
     */
    public boolean markNoticeAsRead(UUID noticeID, UUID userID) {
        return noticeDAO.markAsRead(noticeID, userID);
    }

    /**
     * Get unread count for user
     */
    public int getUnreadCount(UUID userID) {
        return noticeDAO.getUnreadCount(userID);
    }

    /**
     * Validate notice input
     */
    public String validateNoticeInput(String title, String content, String noticeType) {
        if (title == null || title.trim().isEmpty()) {
            return "Tiêu đề không được để trống";
        }

        if (title.length() > 200) {
            return "Tiêu đề không được vượt quá 200 ký tự";
        }

        if (content == null || content.trim().isEmpty()) {
            return "Nội dung không được để trống";
        }

        if (content.length() > 5000) {
            return "Nội dung không được vượt quá 5000 ký tự";
        }

        if (noticeType == null || noticeType.trim().isEmpty()) {
            return "Loại thông báo không được để trống";
        }

        String[] validTypes = {"important", "general", "info", "urgent"};
        boolean isValidType = false;
        for (String type : validTypes) {
            if (type.equalsIgnoreCase(noticeType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            return "Loại thông báo không hợp lệ";
        }

        return null; // Valid
    }
}
