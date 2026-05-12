package com.liteflow.dao.procurement;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.procurement.POAlertNotification;
import jakarta.persistence.EntityManager;
import java.util.UUID;

/**
 * DAO for POAlertNotification entities
 */
public class POAlertNotificationDAO extends GenericDAO<POAlertNotification, UUID> {

    public POAlertNotificationDAO() {
        super(POAlertNotification.class, UUID.class);
    }

    /**
     * Check if notification has been sent for specific PO and user
     * @param userId User ID
     * @param poid PO ID
     * @return true if notification has been sent, false otherwise
     */
    public boolean hasNotificationBeenSent(UUID userId, UUID poid) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            // Use full class name in JPQL
            String jpql = "SELECT COUNT(n) FROM com.liteflow.model.procurement.POAlertNotification n " +
                         "WHERE n.userId = :userId " +
                         "AND n.poid = :poid";
            
            Long count = (Long) em.createQuery(jpql)
                    .setParameter("userId", userId)
                    .setParameter("poid", poid)
                    .getSingleResult();
            
            return count != null && count > 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error checking PO notification status: " + e.getMessage());
            e.printStackTrace();
            return false; // If error, assume not sent to allow retry
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}

