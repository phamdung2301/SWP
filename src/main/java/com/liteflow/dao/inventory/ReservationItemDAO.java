package com.liteflow.dao.inventory;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.inventory.ReservationItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

public class ReservationItemDAO extends GenericDAO<ReservationItem, UUID> {

    public ReservationItemDAO() {
        super(ReservationItem.class, UUID.class);
    }

    /**
     * Create a new reservation item
     */
    public ReservationItem create(ReservationItem item) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            // Merge Reservation and Product entities to attach them to current EntityManager context
            // This prevents "detached entity" errors when Reservation/Product were created in a different EntityManager
            if (item.getReservation() != null && item.getReservation().getReservationId() != null) {
                item.setReservation(em.merge(item.getReservation()));
            }
            if (item.getProduct() != null && item.getProduct().getProductId() != null) {
                item.setProduct(em.merge(item.getProduct()));
            }
            em.persist(item);
            tx.commit();
            return item;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            String errorMsg = "Error creating reservation item: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        } finally {
            em.close();
        }
    }

    /**
     * Find reservation item by ID (overrides GenericDAO)
     */
    @Override
    public ReservationItem findById(UUID itemId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(ReservationItem.class, itemId);
        } catch (Exception e) {
            throw new RuntimeException("Error finding reservation item by ID: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find all items for a reservation
     */
    public List<ReservationItem> findByReservationId(UUID reservationId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ReservationItem> query = em.createQuery(
                "SELECT ri FROM ReservationItem ri WHERE ri.reservation.reservationId = :reservationId", 
                ReservationItem.class
            );
            query.setParameter("reservationId", reservationId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding reservation items: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Update reservation item and return the updated entity
     */
    public ReservationItem updateItem(ReservationItem item) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            ReservationItem updated = em.merge(item);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating reservation item: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Delete reservation item (overrides GenericDAO)
     */
    @Override
    public boolean delete(UUID itemId) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            ReservationItem item = em.find(ReservationItem.class, itemId);
            if (item != null) {
                em.remove(item);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error deleting reservation item: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Delete all items for a reservation
     */
    public int deleteByReservationId(UUID reservationId) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            int deletedCount = em.createQuery(
                "DELETE FROM ReservationItem ri WHERE ri.reservation.reservationId = :reservationId"
            )
            .setParameter("reservationId", reservationId)
            .executeUpdate();
            tx.commit();
            return deletedCount;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error deleting reservation items: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find all reservation items
     */
    public List<ReservationItem> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ReservationItem> query = em.createQuery(
                "SELECT ri FROM ReservationItem ri", 
                ReservationItem.class
            );
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all reservation items: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}

