package com.liteflow.dao.alert;

import com.liteflow.model.alert.AlertConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DAO for Alert Configuration operations
 */
public class AlertConfigurationDAO {
    
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");
    
    /**
     * Get all alert configurations
     */
    public List<AlertConfiguration> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AlertConfiguration> query = em.createQuery(
                "SELECT ac FROM AlertConfiguration ac ORDER BY ac.priority DESC, ac.createdAt DESC",
                AlertConfiguration.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get alert configuration by ID
     */
    public AlertConfiguration getById(UUID alertID) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(AlertConfiguration.class, alertID);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get all enabled alert configurations
     */
    public List<AlertConfiguration> getAllEnabled() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AlertConfiguration> query = em.createQuery(
                "SELECT ac FROM AlertConfiguration ac WHERE ac.isEnabled = true " +
                "ORDER BY ac.priority DESC, ac.alertType",
                AlertConfiguration.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get alert configurations by type
     */
    public List<AlertConfiguration> getByType(String alertType) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AlertConfiguration> query = em.createQuery(
                "SELECT ac FROM AlertConfiguration ac WHERE ac.alertType = :alertType " +
                "AND ac.isEnabled = true",
                AlertConfiguration.class
            );
            query.setParameter("alertType", alertType);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get scheduled alerts that should run now
     */
    public List<AlertConfiguration> getScheduledAlertsToRun() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AlertConfiguration> query = em.createQuery(
                "SELECT ac FROM AlertConfiguration ac " +
                "WHERE ac.isEnabled = true " +
                "AND ac.scheduleCron IS NOT NULL " +
                "AND ac.nextScheduledRun IS NOT NULL " +
                "AND ac.nextScheduledRun <= :now",
                AlertConfiguration.class
            );
            query.setParameter("now", LocalDateTime.now());
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get alerts by priority
     */
    public List<AlertConfiguration> getByPriority(String priority) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AlertConfiguration> query = em.createQuery(
                "SELECT ac FROM AlertConfiguration ac " +
                "WHERE ac.priority = :priority AND ac.isEnabled = true " +
                "ORDER BY ac.createdAt DESC",
                AlertConfiguration.class
            );
            query.setParameter("priority", priority);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get alerts that use GPT
     */
    public List<AlertConfiguration> getGPTEnabledAlerts() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AlertConfiguration> query = em.createQuery(
                "SELECT ac FROM AlertConfiguration ac " +
                "WHERE ac.useGPTSummary = true AND ac.isEnabled = true",
                AlertConfiguration.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Insert new alert configuration
     */
    public boolean insert(AlertConfiguration alertConfig) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(alertConfig);
            em.getTransaction().commit();
            System.out.println("✅ AlertConfiguration inserted: " + alertConfig.getAlertID());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to insert AlertConfiguration: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Update alert configuration
     */
    public boolean update(AlertConfiguration alertConfig) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            alertConfig.setUpdatedAt(LocalDateTime.now());
            em.merge(alertConfig);
            em.getTransaction().commit();
            System.out.println("✅ AlertConfiguration updated: " + alertConfig.getAlertID());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to update AlertConfiguration: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Update last triggered timestamp
     */
    public boolean updateLastTriggered(UUID alertID, UUID triggeredBy) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            AlertConfiguration config = em.find(AlertConfiguration.class, alertID);
            if (config != null) {
                config.setLastTriggered(LocalDateTime.now());
                config.setLastTriggeredBy(triggeredBy);
                config.setTotalTriggered(config.getTotalTriggered() + 1);
                em.merge(config);
            }
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to update last triggered: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Update next scheduled run time
     */
    public boolean updateNextScheduledRun(UUID alertID, LocalDateTime nextRun) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            AlertConfiguration config = em.find(AlertConfiguration.class, alertID);
            if (config != null) {
                config.setNextScheduledRun(nextRun);
                em.merge(config);
            }
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to update next scheduled run: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Enable/disable alert configuration
     */
    public boolean setEnabled(UUID alertID, boolean enabled) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            AlertConfiguration config = em.find(AlertConfiguration.class, alertID);
            if (config != null) {
                config.setIsEnabled(enabled);
                config.setUpdatedAt(LocalDateTime.now());
                em.merge(config);
            }
            em.getTransaction().commit();
            System.out.println("✅ AlertConfiguration " + (enabled ? "enabled" : "disabled") + ": " + alertID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to set enabled: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Delete alert configuration
     */
    public boolean delete(UUID alertID) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            AlertConfiguration config = em.find(AlertConfiguration.class, alertID);
            if (config != null) {
                em.remove(config);
            }
            em.getTransaction().commit();
            System.out.println("✅ AlertConfiguration deleted: " + alertID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to delete AlertConfiguration: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get statistics
     */
    public long getCount() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(ac) FROM AlertConfiguration ac",
                Long.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get enabled count
     */
    public long getEnabledCount() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(ac) FROM AlertConfiguration ac WHERE ac.isEnabled = true",
                Long.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}


