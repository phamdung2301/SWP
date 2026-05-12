package com.liteflow.dao.procurement;

import com.liteflow.model.procurement.PurchaseOrder;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class PurchaseOrderDAO extends GenericDAO<PurchaseOrder, UUID> {
    public PurchaseOrderDAO() { 
        super(PurchaseOrder.class); 
    }
    
    @Override
    public List<PurchaseOrder> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT po FROM PurchaseOrder po ORDER BY po.createDate DESC", 
                PurchaseOrder.class
            ).getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Find all pending purchase orders
     * @return List of pending POs
     */
    public List<PurchaseOrder> findPending() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = 
                "SELECT po FROM PurchaseOrder po " +
                "WHERE po.status = 'PENDING' " +
                "ORDER BY po.createDate ASC";
            
            return em.createQuery(jpql, PurchaseOrder.class).getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Find pending POs older than specified days
     * @param minDays Minimum days waiting
     * @return List of old pending POs
     */
    public List<PurchaseOrder> findPendingOlderThan(int minDays) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(minDays);
            
            String jpql = 
                "SELECT po FROM PurchaseOrder po " +
                "WHERE po.status = 'PENDING' " +
                "  AND po.createDate <= :cutoffDate " +
                "ORDER BY po.createDate ASC";
            
            return em.createQuery(jpql, PurchaseOrder.class)
                     .setParameter("cutoffDate", cutoffDate)
                     .getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Count total pending purchase orders
     * @return Number of pending POs
     */
    public int countPending() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT COUNT(po) FROM PurchaseOrder po WHERE po.status = 'PENDING'";
            Long count = em.createQuery(jpql, Long.class).getSingleResult();
            return count != null ? count.intValue() : 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Count pending POs by approval level
     * @param approvalLevel Approval level (1, 2, 3)
     * @return Count
     */
    public int countPendingByLevel(int approvalLevel) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = 
                "SELECT COUNT(po) FROM PurchaseOrder po " +
                "WHERE po.status = 'PENDING' AND po.approvalLevel = :level";
            
            Long count = em.createQuery(jpql, Long.class)
                           .setParameter("level", approvalLevel)
                           .getSingleResult();
            return count != null ? count.intValue() : 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get pending duration in days for a PO
     * @param po Purchase Order
     * @return Days waiting
     */
    public long getPendingDuration(PurchaseOrder po) {
        if (po == null || po.getCreateDate() == null) return 0;
        return ChronoUnit.DAYS.between(po.getCreateDate(), LocalDateTime.now());
    }
    
    /**
     * Find overdue purchase orders (expected delivery passed)
     * @return List of overdue POs
     */
    public List<PurchaseOrder> findOverdue() {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime now = LocalDateTime.now();
            
            String jpql = 
                "SELECT po FROM PurchaseOrder po " +
                "WHERE po.status = 'APPROVED' " +
                "  AND po.expectedDelivery < :now " +
                "ORDER BY po.expectedDelivery ASC";
            
            return em.createQuery(jpql, PurchaseOrder.class)
                     .setParameter("now", now)
                     .getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total value of pending POs
     * @return Total amount in VND
     */
    public double getTotalPendingValue() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = 
                "SELECT SUM(po.totalAmount) FROM PurchaseOrder po " +
                "WHERE po.status = 'PENDING'";
            
            Double total = em.createQuery(jpql, Double.class).getSingleResult();
            return total != null ? total : 0.0;
        } finally {
            em.close();
        }
    }
}
