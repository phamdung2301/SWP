package com.liteflow.dao.inventory;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.inventory.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReservationDAO extends GenericDAO<Reservation, UUID> {

    public ReservationDAO() {
        super(Reservation.class, UUID.class);
    }

    /**
     * Create a new reservation
     */
    public Reservation create(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            // Merge related entities to ensure they're attached to this EntityManager
            if (reservation.getRoom() != null && reservation.getRoom().getRoomId() != null) {
                reservation.setRoom(em.merge(reservation.getRoom()));
            }
            if (reservation.getTable() != null && reservation.getTable().getTableId() != null) {
                reservation.setTable(em.merge(reservation.getTable()));
            }
            em.persist(reservation);
            // Flush to ensure ID is generated and trigger @PrePersist callbacks
            em.flush();
            tx.commit();
            // Note: Entity becomes detached after em.close(), so we return it as-is
            // The caller should reload it if needed using findById()
            return reservation;
        } catch (Exception e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("❌ Error during rollback: " + rollbackEx.getMessage());
                }
            }
            String errorMsg = "Error creating reservation: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        } finally {
            em.close();
        }
    }

    /**
     * Find all reservations
     */
    public List<Reservation> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r ORDER BY r.arrivalTime DESC", 
                Reservation.class
            );
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all reservations: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find reservation by ID (overrides GenericDAO method for eager loading)
     */
    @Override
    public Reservation findById(UUID reservationId) {
        EntityManager em = emf.createEntityManager();
        try {
            // Use JOIN FETCH to load all related data in one query
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT DISTINCT r FROM Reservation r " +
                "LEFT JOIN FETCH r.reservationItems ri " +
                "LEFT JOIN FETCH ri.product " +
                "LEFT JOIN FETCH r.table t " +
                "LEFT JOIN FETCH t.room " +
                "LEFT JOIN FETCH r.room " +
                "WHERE r.reservationId = :id", 
                Reservation.class
            );
            query.setParameter("id", reservationId);
            List<Reservation> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("❌ Error finding reservation by ID: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error finding reservation by ID: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find reservation by code
     */
    public Reservation findByCode(String reservationCode) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.reservationCode = :code", 
                Reservation.class
            );
            query.setParameter("code", reservationCode);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error finding reservation by code: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find reservations by date (all reservations on a specific date)
     */
    public List<Reservation> findByDate(LocalDate date) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.arrivalTime >= :start AND r.arrivalTime < :end ORDER BY r.arrivalTime", 
                Reservation.class
            );
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding reservations by date: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Check if there is any reservation on the same date for the same
     * customer name OR phone OR email. Used to enforce at-most-one booking
     * per day per identifier.
     */
    public boolean existsSameDayByIdentifier(LocalDate date, String customerName, String customerPhone, String customerEmail) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            StringBuilder jpql = new StringBuilder("SELECT COUNT(r) FROM Reservation r WHERE r.arrivalTime >= :start AND r.arrivalTime < :end");

            // Build OR conditions only for provided identifiers
            boolean hasCondition = false;
            if (customerName != null && !customerName.trim().isEmpty()) {
                jpql.append(" AND (LOWER(r.customerName) = :name");
                hasCondition = true;
            }
            if (customerPhone != null && !customerPhone.trim().isEmpty()) {
                jpql.append(hasCondition ? " OR r.customerPhone = :phone" : " AND (r.customerPhone = :phone");
                hasCondition = true;
            }
            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                jpql.append(hasCondition ? " OR LOWER(r.customerEmail) = :email" : " AND (LOWER(r.customerEmail) = :email");
                hasCondition = true;
            }
            if (hasCondition) {
                jpql.append(")");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            if (customerName != null && !customerName.trim().isEmpty()) {
                query.setParameter("name", customerName.trim().toLowerCase());
            }
            if (customerPhone != null && !customerPhone.trim().isEmpty()) {
                query.setParameter("phone", customerPhone.trim());
            }
            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                query.setParameter("email", customerEmail.trim().toLowerCase());
            }

            Long count = query.getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error checking same-day reservation by identifier: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Same as existsSameDayByIdentifier but excluding a specific reservation ID
     * (useful when updating an existing reservation).
     */
    public boolean existsSameDayByIdentifierExcluding(LocalDate date, String customerName, String customerPhone, String customerEmail, UUID excludeId) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            StringBuilder jpql = new StringBuilder("SELECT COUNT(r) FROM Reservation r WHERE r.arrivalTime >= :start AND r.arrivalTime < :end AND r.reservationId <> :excludeId");

            boolean hasCondition = false;
            if (customerName != null && !customerName.trim().isEmpty()) {
                jpql.append(" AND (LOWER(r.customerName) = :name");
                hasCondition = true;
            }
            if (customerPhone != null && !customerPhone.trim().isEmpty()) {
                jpql.append(hasCondition ? " OR r.customerPhone = :phone" : " AND (r.customerPhone = :phone");
                hasCondition = true;
            }
            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                jpql.append(hasCondition ? " OR LOWER(r.customerEmail) = :email" : " AND (LOWER(r.customerEmail) = :email");
                hasCondition = true;
            }
            if (hasCondition) {
                jpql.append(")");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            query.setParameter("excludeId", excludeId);
            if (customerName != null && !customerName.trim().isEmpty()) {
                query.setParameter("name", customerName.trim().toLowerCase());
            }
            if (customerPhone != null && !customerPhone.trim().isEmpty()) {
                query.setParameter("phone", customerPhone.trim());
            }
            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                query.setParameter("email", customerEmail.trim().toLowerCase());
            }

            Long count = query.getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error checking same-day reservation (excluding) by identifier: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find reservations by status
     */
    public List<Reservation> findByStatus(String status) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.status = :status ORDER BY r.arrivalTime", 
                Reservation.class
            );
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding reservations by status: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find reservations by date range (for calendar view)
     */
    public List<Reservation> findByDateRange(LocalDateTime start, LocalDateTime end) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.arrivalTime >= :start AND r.arrivalTime < :end ORDER BY r.arrivalTime", 
                Reservation.class
            );
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding reservations by date range: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Update reservation and return the updated entity
     */
    public Reservation updateReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            Reservation updated = em.merge(reservation);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating reservation: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Update reservation status
     */
    public boolean updateStatus(UUID reservationId, String newStatus) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation != null) {
                reservation.setStatus(newStatus);
                reservation.setUpdatedAt(LocalDateTime.now());
                em.merge(reservation);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating reservation status: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Assign table to reservation
     */
    public boolean assignTable(UUID reservationId, UUID tableId) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation != null) {
                // Fetch table
                com.liteflow.model.inventory.Table table = em.find(com.liteflow.model.inventory.Table.class, tableId);
                if (table != null) {
                    reservation.setTable(table);
                    if (table.getRoom() != null) {
                        reservation.setRoom(table.getRoom());
                    }
                    reservation.setStatus("CONFIRMED");
                    reservation.setUpdatedAt(LocalDateTime.now());
                    em.merge(reservation);
                    tx.commit();
                    return true;
                }
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error assigning table to reservation: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Check and find overdue reservations (more than X minutes past arrival time)
     */
    public List<Reservation> checkOverdue(int minutesThreshold) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesThreshold);
            
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE (r.status = 'PENDING' OR r.status = 'CONFIRMED') AND r.arrivalTime < :threshold", 
                Reservation.class
            );
            query.setParameter("threshold", threshold);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error checking overdue reservations: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Get the next sequence number for a given date
     */
    public int getNextSequenceNumber(LocalDate date) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            // Find the highest sequence number for this date
            TypedQuery<String> query = em.createQuery(
                "SELECT r.reservationCode FROM Reservation r WHERE r.arrivalTime >= :start AND r.arrivalTime < :end ORDER BY r.reservationCode DESC", 
                String.class
            );
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            query.setMaxResults(1);
            
            List<String> results = query.getResultList();
            if (results.isEmpty()) {
                return 1;
            }
            
           
            String lastCode = results.get(0);
            String[] parts = lastCode.split("-");
            if (parts.length == 2) {
                try {
                    int lastSequence = Integer.parseInt(parts[1]);
                    return lastSequence + 1;
                } catch (NumberFormatException e) {
                    return 1;
                }
            }
            return 1;
        } catch (Exception e) {
            throw new RuntimeException("Error getting next sequence number: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Delete reservation (overrides GenericDAO method)
     */
    @Override
    public boolean delete(UUID reservationId) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation != null) {
                em.remove(reservation);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error deleting reservation: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find reservations by phone number
     */
    public List<Reservation> findByPhone(String phone) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.customerPhone LIKE :phone ORDER BY r.arrivalTime DESC", 
                Reservation.class
            );
            query.setParameter("phone", "%" + phone + "%");
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding reservations by phone: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find reservations by customer name
     */
    public List<Reservation> findByCustomerName(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE LOWER(r.customerName) LIKE LOWER(:name) ORDER BY r.arrivalTime DESC", 
                Reservation.class
            );
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding reservations by customer name: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Search reservations (by code, name, or phone)
     */
    public List<Reservation> search(String keyword) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE " +
                "r.reservationCode LIKE :keyword OR " +
                "LOWER(r.customerName) LIKE LOWER(:keyword) OR " +
                "r.customerPhone LIKE :keyword " +
                "ORDER BY r.arrivalTime DESC", 
                Reservation.class
            );
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error searching reservations: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Count reservations by status for a specific date
     */
    public long countByDateAndStatus(LocalDate date, String status) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(r) FROM Reservation r WHERE r.arrivalTime >= :start AND r.arrivalTime < :end AND r.status = :status", 
                Long.class
            );
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            query.setParameter("status", status);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting reservations: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Find active reservation by table (CONFIRMED status, chưa đóng)
     */
    public Reservation findActiveReservationByTable(UUID tableId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.table.tableId = :tableId AND r.status = 'CONFIRMED' ORDER BY r.arrivalTime DESC",
                Reservation.class
            );
            query.setParameter("tableId", tableId);
            query.setMaxResults(1);
            List<Reservation> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("❌ Error finding active reservation by table: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
}


