package com.liteflow.dao.timesheet;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.timesheet.EmployeeAttendance;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class EmployeeAttendanceDAO extends GenericDAO<EmployeeAttendance, UUID> {

    public EmployeeAttendanceDAO() {
        super(EmployeeAttendance.class, UUID.class);
    }

    public EmployeeAttendance findByEmployeeAndDate(UUID employeeId, LocalDate workDate) {
        if (employeeId == null || workDate == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            List<EmployeeAttendance> list = em.createQuery(
                    "SELECT a FROM EmployeeAttendance a JOIN FETCH a.employee e " +
                            "WHERE e.employeeID = :eid AND a.workDate = :d", EmployeeAttendance.class)
                    .setParameter("eid", employeeId)
                    .setParameter("d", workDate)
                    .setMaxResults(1)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }

    public List<EmployeeAttendance> findByWorkDateRange(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM EmployeeAttendance a " +
                            "JOIN FETCH a.employee e " +
                            "WHERE a.workDate >= :start AND a.workDate <= :end " +
                            "ORDER BY a.workDate ASC, e.employeeCode ASC", EmployeeAttendance.class)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<EmployeeAttendance> findByEmployeeAndMonth(UUID employeeId, int year, int month) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = LocalDate.of(year, month, startDate.lengthOfMonth());
            
            return em.createQuery(
                    "SELECT a FROM EmployeeAttendance a " +
                            "WHERE a.employee.employeeID = :employeeId " +
                            "AND a.workDate >= :startDate AND a.workDate <= :endDate " +
                            "ORDER BY a.workDate ASC", EmployeeAttendance.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}


