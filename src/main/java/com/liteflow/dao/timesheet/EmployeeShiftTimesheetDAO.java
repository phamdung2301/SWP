package com.liteflow.dao.timesheet;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.timesheet.EmployeeShiftTimesheet;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

public class EmployeeShiftTimesheetDAO extends GenericDAO<EmployeeShiftTimesheet, java.util.UUID> {

    public EmployeeShiftTimesheetDAO() {
        super(EmployeeShiftTimesheet.class, java.util.UUID.class);
    }

    public List<EmployeeShiftTimesheet> findByWorkDateRange(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT t FROM EmployeeShiftTimesheet t " +
                    "JOIN FETCH t.employee e " +
                    "LEFT JOIN FETCH t.shift s " +
                    "WHERE t.workDate >= :start AND t.workDate <= :end " +
                    "ORDER BY t.workDate ASC, e.employeeCode ASC", EmployeeShiftTimesheet.class)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm timesheet theo employee, shift và ngày
     */
    public EmployeeShiftTimesheet findByEmployeeShiftAndDate(java.util.UUID employeeId, java.util.UUID shiftId, LocalDate workDate) {
        if (employeeId == null || workDate == null) {
            return null;
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            String query = "SELECT t FROM EmployeeShiftTimesheet t " +
                    "JOIN FETCH t.employee e " +
                    "LEFT JOIN FETCH t.shift s " +
                    "WHERE e.employeeID = :employeeId " +
                    "AND t.workDate = :workDate ";
            
            if (shiftId != null) {
                query += "AND s.shiftID = :shiftId";
            } else {
                query += "AND t.shift IS NULL";
            }
            
            var q = em.createQuery(query, EmployeeShiftTimesheet.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("workDate", workDate);
            
            if (shiftId != null) {
                q.setParameter("shiftId", shiftId);
            }
            
            var results = q.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
}


