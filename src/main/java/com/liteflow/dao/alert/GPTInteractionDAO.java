package com.liteflow.dao.alert;

import com.liteflow.model.alert.GPTInteraction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DAO for GPT Interaction operations
 */
public class GPTInteractionDAO {
    
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");
    
    /**
     * Get all GPT interactions
     */
    public List<GPTInteraction> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<GPTInteraction> query = em.createQuery(
                "SELECT gi FROM GPTInteraction gi ORDER BY gi.createdAt DESC",
                GPTInteraction.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get GPT interaction by ID
     */
    public GPTInteraction getById(UUID interactionID) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(GPTInteraction.class, interactionID);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get interactions by purpose
     */
    public List<GPTInteraction> getByPurpose(String purpose, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<GPTInteraction> query = em.createQuery(
                "SELECT gi FROM GPTInteraction gi " +
                "WHERE gi.purpose = :purpose " +
                "ORDER BY gi.createdAt DESC",
                GPTInteraction.class
            );
            query.setParameter("purpose", purpose);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get successful interactions
     */
    public List<GPTInteraction> getSuccessfulInteractions(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<GPTInteraction> query = em.createQuery(
                "SELECT gi FROM GPTInteraction gi " +
                "WHERE gi.status = 'SUCCESS' " +
                "ORDER BY gi.createdAt DESC",
                GPTInteraction.class
            );
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get failed interactions
     */
    public List<GPTInteraction> getFailedInteractions(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<GPTInteraction> query = em.createQuery(
                "SELECT gi FROM GPTInteraction gi " +
                "WHERE gi.status IN ('FAILED', 'TIMEOUT', 'RATE_LIMITED') " +
                "ORDER BY gi.createdAt DESC",
                GPTInteraction.class
            );
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get interactions by date range
     */
    public List<GPTInteraction> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<GPTInteraction> query = em.createQuery(
                "SELECT gi FROM GPTInteraction gi " +
                "WHERE gi.createdAt BETWEEN :startDate AND :endDate " +
                "ORDER BY gi.createdAt DESC",
                GPTInteraction.class
            );
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Insert new GPT interaction
     */
    public boolean insert(GPTInteraction interaction) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(interaction);
            em.getTransaction().commit();
            System.out.println("✅ GPTInteraction inserted: " + interaction.getInteractionID() + 
                             " - Cost: " + interaction.getFormattedCostVND());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to insert GPTInteraction: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Update GPT interaction
     */
    public boolean update(GPTInteraction interaction) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(interaction);
            em.getTransaction().commit();
            System.out.println("✅ GPTInteraction updated: " + interaction.getInteractionID());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to update GPTInteraction: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Record feedback
     */
    public boolean recordFeedback(UUID interactionID, boolean wasHelpful, String notes) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            GPTInteraction interaction = em.find(GPTInteraction.class, interactionID);
            if (interaction != null) {
                interaction.setWasHelpful(wasHelpful);
                interaction.setFeedbackNotes(notes);
                em.merge(interaction);
            }
            em.getTransaction().commit();
            System.out.println("✅ Feedback recorded for GPTInteraction: " + interactionID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to record feedback: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total cost (USD)
     */
    public BigDecimal getTotalCostUSD() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(gi.estimatedCostUSD), 0) FROM GPTInteraction gi " +
                "WHERE gi.status = 'SUCCESS'",
                BigDecimal.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total cost (VND)
     */
    public BigDecimal getTotalCostVND() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(gi.estimatedCostVND), 0) FROM GPTInteraction gi " +
                "WHERE gi.status = 'SUCCESS'",
                BigDecimal.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total tokens used
     */
    public Long getTotalTokens() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COALESCE(SUM(gi.totalTokens), 0) FROM GPTInteraction gi " +
                "WHERE gi.status = 'SUCCESS'",
                Long.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get average response time
     */
    public Double getAverageResponseTime() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                "SELECT AVG(CAST(gi.responseTimeMs AS double)) FROM GPTInteraction gi " +
                "WHERE gi.status = 'SUCCESS' AND gi.responseTimeMs IS NOT NULL",
                Double.class
            );
            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get success rate
     */
    public double getSuccessRate() {
        EntityManager em = emf.createEntityManager();
        try {
            Long total = em.createQuery("SELECT COUNT(gi) FROM GPTInteraction gi", Long.class)
                          .getSingleResult();
            if (total == 0) return 0.0;
            
            Long successful = em.createQuery(
                "SELECT COUNT(gi) FROM GPTInteraction gi WHERE gi.status = 'SUCCESS'",
                Long.class
            ).getSingleResult();
            
            return (successful * 100.0) / total;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get statistics by model
     */
    public List<Object[]> getStatsByModel() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Object[]> query = em.createQuery(
                "SELECT gi.model, COUNT(gi), SUM(gi.totalTokens), SUM(gi.estimatedCostVND) " +
                "FROM GPTInteraction gi " +
                "WHERE gi.status = 'SUCCESS' " +
                "GROUP BY gi.model",
                Object[].class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get statistics by purpose
     */
    public List<Object[]> getStatsByPurpose() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Object[]> query = em.createQuery(
                "SELECT gi.purpose, COUNT(gi), SUM(gi.totalTokens), SUM(gi.estimatedCostVND) " +
                "FROM GPTInteraction gi " +
                "WHERE gi.status = 'SUCCESS' " +
                "GROUP BY gi.purpose " +
                "ORDER BY COUNT(gi) DESC",
                Object[].class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Delete old interactions (cleanup)
     */
    public int deleteOldInteractions(int daysOld) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            int count = em.createQuery(
                "DELETE FROM GPTInteraction gi " +
                "WHERE gi.createdAt < :cutoffDate"
            )
            .setParameter("cutoffDate", LocalDateTime.now().minusDays(daysOld))
            .executeUpdate();
            em.getTransaction().commit();
            System.out.println("✅ Deleted " + count + " old GPT interactions");
            return count;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to delete old interactions: " + e.getMessage());
            return 0;
        } finally {
            em.close();
        }
    }
}


