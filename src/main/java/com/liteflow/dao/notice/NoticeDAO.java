package com.liteflow.dao.notice;

import com.liteflow.model.notice.Notice;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for Notice operations using JPA
 */
public class NoticeDAO {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");

    /**
     * Get active notices for employee dashboard
     */
    public List<Notice> getActiveNotices(UUID userID, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            // Get active notices with user read status
            String jpql = "SELECT n FROM Notice n " +
                         "WHERE n.isActive = true " +
                         "AND (n.expiresAt IS NULL OR n.expiresAt > :now) " +
                         "ORDER BY n.isPinned DESC, n.publishedAt DESC";

            TypedQuery<Notice> query = em.createQuery(jpql, Notice.class);
            query.setParameter("now", LocalDateTime.now());
            query.setMaxResults(limit);

            List<Notice> notices = query.getResultList();

            // Check read status for each notice if userID provided
            if (userID != null) {
                for (Notice notice : notices) {
                    boolean isRead = checkIfRead(em, notice.getNoticeID(), userID);
                    notice.setIsRead(isRead);
                }
            }

            return notices;

        } catch (Exception e) {
            System.err.println("❌ Error getting active notices: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    /**
     * Check if notice is read by user
     */
    private boolean checkIfRead(EntityManager em, UUID noticeID, UUID userID) {
        try {
            String sql = "SELECT COUNT(*) FROM NoticeReads WHERE NoticeID = :noticeID AND UserID = :userID";
            Query query = em.createNativeQuery(sql);
            query.setParameter("noticeID", noticeID.toString());
            query.setParameter("userID", userID.toString());

            Number count = (Number) query.getSingleResult();
            return count.intValue() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get all active notices (admin view)
     */
    public List<Notice> getAllActiveNotices() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Notice> query = em.createQuery(
                "SELECT n FROM Notice n " +
                "WHERE n.isActive = true " +
                "AND (n.expiresAt IS NULL OR n.expiresAt > :now) " +
                "ORDER BY n.isPinned DESC, n.publishedAt DESC",
                Notice.class
            );
            query.setParameter("now", LocalDateTime.now());
            return query.getResultList();

        } catch (Exception e) {
            System.err.println("❌ Error getting all active notices: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    /**
     * Create new notice
     */
    public UUID createNotice(Notice notice) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            // Ensure UUID is set
            if (notice.getNoticeID() == null) {
                notice.setNoticeID(UUID.randomUUID());
            }

            em.persist(notice);
            transaction.commit();

            System.out.println("✅ Notice created: " + notice.getNoticeID());
            return notice.getNoticeID();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("❌ Error creating notice: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Mark notice as read
     */
    public boolean markAsRead(UUID noticeID, UUID userID) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            // Check if already read
            String checkSql = "SELECT COUNT(*) FROM NoticeReads WHERE NoticeID = :noticeID AND UserID = :userID";
            Query checkQuery = em.createNativeQuery(checkSql);
            checkQuery.setParameter("noticeID", noticeID.toString());
            checkQuery.setParameter("userID", userID.toString());

            Number count = (Number) checkQuery.getSingleResult();
            if (count.intValue() > 0) {
                transaction.commit();
                return true; // Already read
            }

            // Insert read record
            String insertSql = "INSERT INTO NoticeReads (ReadID, NoticeID, UserID, ReadAt) " +
                              "VALUES (:readID, :noticeID, :userID, :readAt)";
            Query insertQuery = em.createNativeQuery(insertSql);
            insertQuery.setParameter("readID", UUID.randomUUID().toString());
            insertQuery.setParameter("noticeID", noticeID.toString());
            insertQuery.setParameter("userID", userID.toString());
            insertQuery.setParameter("readAt", LocalDateTime.now());

            insertQuery.executeUpdate();

            // Update view count
            String updateSql = "UPDATE Notices SET ViewCount = ViewCount + 1 WHERE NoticeID = :noticeID";
            Query updateQuery = em.createNativeQuery(updateSql);
            updateQuery.setParameter("noticeID", noticeID.toString());
            updateQuery.executeUpdate();

            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("❌ Error marking notice as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get notice by ID
     */
    public Notice getNoticeByID(UUID noticeID) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Notice.class, noticeID);
        } catch (Exception e) {
            System.err.println("❌ Error getting notice by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Update notice
     */
    public boolean updateNotice(Notice notice) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            notice.setUpdatedAt(LocalDateTime.now());
            em.merge(notice);

            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("❌ Error updating notice: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete notice (soft delete)
     */
    public boolean deleteNotice(UUID noticeID) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            Notice notice = em.find(Notice.class, noticeID);
            if (notice != null) {
                notice.setIsActive(false);
                notice.setUpdatedAt(LocalDateTime.now());
                em.merge(notice);
                transaction.commit();
                return true;
            }

            transaction.rollback();
            return false;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("❌ Error deleting notice: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get unread notice count for user
     */
    public int getUnreadCount(UUID userID) {
        EntityManager em = emf.createEntityManager();

        try {
            String sql = "SELECT COUNT(*) FROM Notices n " +
                        "WHERE n.IsActive = 1 " +
                        "AND (n.ExpiresAt IS NULL OR n.ExpiresAt > SYSDATETIME()) " +
                        "AND NOT EXISTS (SELECT 1 FROM NoticeReads nr WHERE nr.NoticeID = n.NoticeID AND nr.UserID = :userID)";

            Query query = em.createNativeQuery(sql);
            query.setParameter("userID", userID.toString());

            Number count = (Number) query.getSingleResult();
            return count.intValue();

        } catch (Exception e) {
            System.err.println("❌ Error getting unread count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            em.close();
        }
    }
}
