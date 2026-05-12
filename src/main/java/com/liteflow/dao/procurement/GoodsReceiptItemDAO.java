package com.liteflow.dao.procurement;

import com.liteflow.model.procurement.GoodsReceiptItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

public class GoodsReceiptItemDAO {
    
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");
    
    /**
     * Lấy tất cả items của một Goods Receipt
     */
    public List<GoodsReceiptItem> findByReceiptID(UUID receiptID) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<GoodsReceiptItem> query = em.createQuery(
                "SELECT g FROM GoodsReceiptItem g WHERE g.receiptID = :receiptID ORDER BY g.itemID",
                GoodsReceiptItem.class
            );
            query.setParameter("receiptID", receiptID);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy items có chênh lệch (discrepancy)
     */
    public List<GoodsReceiptItem> findWithDiscrepancy(UUID receiptID) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<GoodsReceiptItem> query = em.createQuery(
                "SELECT g FROM GoodsReceiptItem g WHERE g.receiptID = :receiptID AND g.discrepancy <> 0 ORDER BY g.itemID",
                GoodsReceiptItem.class
            );
            query.setParameter("receiptID", receiptID);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy items có vấn đề chất lượng
     */
    public List<GoodsReceiptItem> findWithQualityIssues(UUID receiptID) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<GoodsReceiptItem> query = em.createQuery(
                "SELECT g FROM GoodsReceiptItem g WHERE g.receiptID = :receiptID AND g.qualityStatus <> 'OK' ORDER BY g.itemID",
                GoodsReceiptItem.class
            );
            query.setParameter("receiptID", receiptID);
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
    public List<GoodsReceiptItem> findByPOItemID(Integer poItemID) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<GoodsReceiptItem> query = em.createQuery(
                "SELECT g FROM GoodsReceiptItem g WHERE g.poItemID = :poItemID",
                GoodsReceiptItem.class
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
    
    public boolean insert(GoodsReceiptItem item) {
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
    
    public boolean update(GoodsReceiptItem item) {
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
