package com.liteflow.dao.employee;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.auth.EmployeeShift;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EmployeeShiftDAO extends GenericDAO<EmployeeShift, java.util.UUID> {

    public EmployeeShiftDAO() {
        super(EmployeeShift.class, java.util.UUID.class);
    }

    public List<EmployeeShift> findByDateRange(LocalDateTime start, LocalDateTime end) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM EmployeeShift s " +
                                    "JOIN FETCH s.employee e " +
                                    "WHERE s.startAt < :end AND s.endAt > :start " +
                                    "ORDER BY s.startAt ASC", EmployeeShift.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<EmployeeShift> findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59, 999000000);
        return findByDateRange(start, end);
    }

    /**
     * Tìm shift của một employee trong ngày cụ thể
     */
    public List<EmployeeShift> findByEmployeeAndDate(java.util.UUID employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return new java.util.ArrayList<>();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59, 999000000);
            
            return em.createQuery(
                            "SELECT s FROM EmployeeShift s " +
                                    "JOIN FETCH s.employee e " +
                                    "WHERE e.employeeID = :employeeId " +
                                    "AND s.startAt < :end AND s.endAt > :start " +
                                    "ORDER BY s.startAt ASC", EmployeeShift.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}


