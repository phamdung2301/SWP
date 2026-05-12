package com.liteflow.dao.alert;

import com.liteflow.model.alert.NotificationChannel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DAO for Notification Channel operations
 */
public class NotificationChannelDAO {
    
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");
    
    /**
     * Get all notification channels
     */
    public List<NotificationChannel> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<NotificationChannel> query = em.createQuery(
                "SELECT nc FROM NotificationChannel nc ORDER BY nc.channelType, nc.name",
                NotificationChannel.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get notification channel by ID
     */
    public NotificationChannel getById(UUID channelID) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(NotificationChannel.class, channelID);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get all active notification channels
     */
    public List<NotificationChannel> getAllActive() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<NotificationChannel> query = em.createQuery(
                "SELECT nc FROM NotificationChannel nc WHERE nc.isActive = true " +
                "ORDER BY nc.channelType, nc.name",
                NotificationChannel.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get channels by type
     */
    public List<NotificationChannel> getByType(String channelType) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<NotificationChannel> query = em.createQuery(
                "SELECT nc FROM NotificationChannel nc " +
                "WHERE nc.channelType = :channelType AND nc.isActive = true",
                NotificationChannel.class
            );
            query.setParameter("channelType", channelType);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get default Slack channel
     */
    public NotificationChannel getDefaultSlackChannel() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<NotificationChannel> query = em.createQuery(
                "SELECT nc FROM NotificationChannel nc " +
                "WHERE nc.channelType = 'SLACK' AND nc.isActive = true " +
                "ORDER BY nc.createdAt ASC",
                NotificationChannel.class
            );
            query.setMaxResults(1);
            List<NotificationChannel> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get default Telegram channel
     */
    public NotificationChannel getDefaultTelegramChannel() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<NotificationChannel> query = em.createQuery(
                "SELECT nc FROM NotificationChannel nc " +
                "WHERE nc.channelType = 'TELEGRAM' AND nc.isActive = true " +
                "ORDER BY nc.createdAt ASC",
                NotificationChannel.class
            );
            query.setMaxResults(1);
            List<NotificationChannel> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    /**
     * Insert new notification channel
     */
    public boolean insert(NotificationChannel channel) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(channel);
            em.getTransaction().commit();
            System.out.println("✅ NotificationChannel inserted: " + channel.getChannelID());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to insert NotificationChannel: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Update notification channel
     */
    public boolean update(NotificationChannel channel) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            channel.setUpdatedAt(LocalDateTime.now());
            em.merge(channel);
            em.getTransaction().commit();
            System.out.println("✅ NotificationChannel updated: " + channel.getChannelID());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to update NotificationChannel: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Record channel usage
     */
    public boolean recordUsage(UUID channelID, boolean success, String errorMessage) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            NotificationChannel channel = em.find(NotificationChannel.class, channelID);
            if (channel != null) {
                if (success) {
                    channel.recordSuccess();
                } else {
                    channel.recordError(errorMessage);
                }
                em.merge(channel);
            }
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to record usage: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Increment request count
     */
    public boolean incrementRequestCount(UUID channelID) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            NotificationChannel channel = em.find(NotificationChannel.class, channelID);
            if (channel != null) {
                channel.incrementRequestCount();
                em.merge(channel);
            }
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to increment request count: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Enable/disable notification channel
     */
    public boolean setActive(UUID channelID, boolean active) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            NotificationChannel channel = em.find(NotificationChannel.class, channelID);
            if (channel != null) {
                channel.setIsActive(active);
                channel.setUpdatedAt(LocalDateTime.now());
                em.merge(channel);
            }
            em.getTransaction().commit();
            System.out.println("✅ NotificationChannel " + (active ? "activated" : "deactivated") + ": " + channelID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to set active: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Delete notification channel
     */
    public boolean delete(UUID channelID) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            NotificationChannel channel = em.find(NotificationChannel.class, channelID);
            if (channel != null) {
                em.remove(channel);
            }
            em.getTransaction().commit();
            System.out.println("✅ NotificationChannel deleted: " + channelID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to delete NotificationChannel: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get count by type
     */
    public long getCountByType(String channelType) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(nc) FROM NotificationChannel nc " +
                "WHERE nc.channelType = :channelType AND nc.isActive = true",
                Long.class
            );
            query.setParameter("channelType", channelType);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}


