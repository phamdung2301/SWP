package com.liteflow.dao.timesheet;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.timesheet.LeaveRequest;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LeaveRequestDAO extends GenericDAO<LeaveRequest, UUID> {

    public LeaveRequestDAO() {
        super(LeaveRequest.class, UUID.class);
    }

    /**
     * Lấy tất cả đơn xin nghỉ của một employee
     */
    public List<LeaveRequest> findByEmployeeId(UUID employeeId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT lr FROM LeaveRequest lr WHERE lr.employee.employeeID = :employeeId " +
                            "ORDER BY lr.createdAt DESC", LeaveRequest.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy đơn xin nghỉ trong khoảng thời gian
     */
    public List<LeaveRequest> findByEmployeeAndDateRange(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT lr FROM LeaveRequest lr WHERE lr.employee.employeeID = :employeeId " +
                            "AND lr.startDate <= :endDate AND lr.endDate >= :startDate " +
                            "ORDER BY lr.startDate ASC", LeaveRequest.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy đơn xin nghỉ theo status
     */
    public List<LeaveRequest> findByEmployeeAndStatus(UUID employeeId, String status) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT lr FROM LeaveRequest lr WHERE lr.employee.employeeID = :employeeId " +
                            "AND lr.status = :status " +
                            "ORDER BY lr.createdAt DESC", LeaveRequest.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả đơn xin nghỉ đang chờ duyệt
     */
    public List<LeaveRequest> findPendingRequests() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT lr FROM LeaveRequest lr WHERE lr.status = :status " +
                            "ORDER BY lr.createdAt ASC", LeaveRequest.class)
                    .setParameter("status", "Chờ duyệt")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra xem có đơn xin nghỉ nào trùng lặp thời gian không
     */
    public boolean hasOverlappingLeave(UUID employeeId, LocalDate startDate, LocalDate endDate, UUID excludeRequestId) {
        EntityManager em = emf.createEntityManager();
        try {
            String query = "SELECT COUNT(lr) FROM LeaveRequest lr " +
                    "WHERE lr.employee.employeeID = :employeeId " +
                    "AND lr.status IN ('Chờ duyệt', 'Đã duyệt') " +
                    "AND lr.startDate <= :endDate AND lr.endDate >= :startDate";

            if (excludeRequestId != null) {
                query += " AND lr.leaveRequestId != :excludeRequestId";
            }

            var q = em.createQuery(query, Long.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate);

            if (excludeRequestId != null) {
                q.setParameter("excludeRequestId", excludeRequestId);
            }

            return q.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Đếm số ngày nghỉ đã được duyệt trong một khoảng thời gian
     */
    public long countApprovedLeaveDays(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            List<LeaveRequest> approvedLeaves = em.createQuery(
                    "SELECT lr FROM LeaveRequest lr WHERE lr.employee.employeeID = :employeeId " +
                            "AND lr.status = 'Đã duyệt' " +
                            "AND lr.startDate <= :endDate AND lr.endDate >= :startDate",
                    LeaveRequest.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();

            return approvedLeaves.stream()
                    .mapToLong(lr -> lr.getTotalDays().longValue())
                    .sum();
        } finally {
            em.close();
        }
    }
}
