package com.liteflow.dao.procurement;

import com.liteflow.model.procurement.InvoiceItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

public class InvoiceItemDAO {
    
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");
    
    /**
     * Lấy tất cả items của một Invoice
     */
    public List<InvoiceItem> findByInvoiceID(UUID invoiceID) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<InvoiceItem> query = em.createQuery(
                "SELECT i FROM InvoiceItem i WHERE i.invoiceID = :invoiceID ORDER BY i.itemID",
                InvoiceItem.class
            );
            query.setParameter("invoiceID", invoiceID);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy items chưa được match
     */
    public List<InvoiceItem> findUnmatched(UUID invoiceID) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<InvoiceItem> query = em.createQuery(
                "SELECT i FROM InvoiceItem i WHERE i.invoiceID = :invoiceID AND i.matched = false ORDER BY i.itemID",
                InvoiceItem.class
            );
            query.setParameter("invoiceID", invoiceID);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy items có chênh lệch
     */
    public List<InvoiceItem> findWithDiscrepancy(UUID invoiceID) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<InvoiceItem> query = em.createQuery(
                "SELECT i FROM InvoiceItem i WHERE i.invoiceID = :invoiceID AND " +
                "(ABS(i.discrepancyAmount) > 0.01 OR i.discrepancyQuantity <> 0) ORDER BY i.itemID",
                InvoiceItem.class
            );
            query.setParameter("invoiceID", invoiceID);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy item theo POItemID
     */
    public List<InvoiceItem> findByPOItemID(Integer poItemID) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<InvoiceItem> query = em.createQuery(
                "SELECT i FROM InvoiceItem i WHERE i.poItemID = :poItemID",
                InvoiceItem.class
            );
            query.setParameter("poItemID", poItemID);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Xóa tất cả items của một invoice
     */
    public boolean deleteByInvoiceID(UUID invoiceID) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            int deleted = em.createQuery(
                "DELETE FROM InvoiceItem i WHERE i.invoiceID = :invoiceID"
            ).setParameter("invoiceID", invoiceID)
             .executeUpdate();
            em.getTransaction().commit();
            return deleted > 0;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    public boolean insert(InvoiceItem item) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(item);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    public boolean update(InvoiceItem item) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(item);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
