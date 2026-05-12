package com.liteflow.dao.inventory;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.inventory.StockAlertNotification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.UUID;
import java.util.List;
import java.util.Collections;

/**
 * DAO for StockAlertNotification entities
 */
public class StockAlertNotificationDAO extends GenericDAO<StockAlertNotification, UUID> {

    public StockAlertNotificationDAO() {
        super(StockAlertNotification.class, UUID.class);
    }

    /**
     * Check if notification has been sent for specific variant, user, and threshold
     * @param userId User ID
     * @param variantId Product Variant ID
     * @param threshold Alert threshold (10 or 20)
     * @return true if notification has been sent, false otherwise
     */
    public boolean hasNotificationBeenSent(UUID userId, UUID variantId, int threshold) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = "SELECT COUNT(n) FROM StockAlertNotification n " +
                         "WHERE n.userId = :userId " +
                         "AND n.productVariant.productVariantId = :variantId " +
                         "AND n.alertThreshold = :threshold";
            
            Query query = em.createQuery(jpql);
            query.setParameter("userId", userId);
            query.setParameter("variantId", variantId);
            query.setParameter("threshold", threshold);
            
            Long count = (Long) query.getSingleResult();
            return count != null && count > 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error checking notification status: " + e.getMessage());
            e.printStackTrace();
            return false; // If error, assume not sent to allow retry
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Reset notification state when stock increases above threshold
     * Delete notifications for variants that now have stock > threshold
     * @param variantId Product Variant ID
     * @param threshold Alert threshold
     */
    public void resetNotificationState(UUID variantId, int threshold) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            
            String jpql = "DELETE FROM StockAlertNotification n " +
                         "WHERE n.productVariant.productVariantId = :variantId " +
                         "AND n.alertThreshold = :threshold";
            
            Query query = em.createQuery(jpql);
            query.setParameter("variantId", variantId);
            query.setParameter("threshold", threshold);
            
            int deleted = query.executeUpdate();
            em.getTransaction().commit();
            
            if (deleted > 0) {
                System.out.println("✅ Reset notification state for variant " + variantId + 
                                 " threshold " + threshold + " (deleted " + deleted + " records)");
            }
            
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Error resetting notification state: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Get all notifications for a specific user
     */
    public List<StockAlertNotification> getNotificationsByUser(UUID userId) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = "SELECT n FROM StockAlertNotification n " +
                         "WHERE n.userId = :userId " +
                         "ORDER BY n.sentAt DESC";
            
            Query query = em.createQuery(jpql);
            query.setParameter("userId", userId);
            
           
            List<StockAlertNotification> results = query.getResultList();
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting notifications by user: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}

