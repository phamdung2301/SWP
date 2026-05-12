package com.liteflow.dao.timesheet;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.timesheet.ForgotClockRequest;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ForgotClockRequestDAO extends GenericDAO<ForgotClockRequest, UUID> {

    public ForgotClockRequestDAO() {
        super(ForgotClockRequest.class, UUID.class);
    }

    /**
     * Lấy tất cả yêu cầu quên chấm công của một employee
     */
    public List<ForgotClockRequest> findByEmployeeId(UUID employeeId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT fcr FROM ForgotClockRequest fcr WHERE fcr.employee.employeeID = :employeeId " +
                                    "ORDER BY fcr.createdAt DESC", ForgotClockRequest.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy yêu cầu quên chấm công theo status
     */
    public List<ForgotClockRequest> findByEmployeeAndStatus(UUID employeeId, String status) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT fcr FROM ForgotClockRequest fcr WHERE fcr.employee.employeeID = :employeeId " +
                                    "AND fcr.status = :status " +
                                    "ORDER BY fcr.createdAt DESC", ForgotClockRequest.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả yêu cầu đang chờ duyệt
     */
    public List<ForgotClockRequest> findPendingRequests() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT fcr FROM ForgotClockRequest fcr WHERE fcr.status = :status " +
                                    "ORDER BY fcr.createdAt ASC", ForgotClockRequest.class)
                    .setParameter("status", "Chờ duyệt")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy yêu cầu trong khoảng thời gian
     */
    public List<ForgotClockRequest> findByDateRange(LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT fcr FROM ForgotClockRequest fcr " +
                                    "WHERE fcr.forgotDate >= :startDate AND fcr.forgotDate <= :endDate " +
                                    "ORDER BY fcr.forgotDate DESC", ForgotClockRequest.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy yêu cầu của employee trong khoảng thời gian
     */
    public List<ForgotClockRequest> findByEmployeeAndDateRange(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT fcr FROM ForgotClockRequest fcr " +
                                    "WHERE fcr.employee.employeeID = :employeeId " +
                                    "AND fcr.forgotDate >= :startDate AND fcr.forgotDate <= :endDate " +
                                    "ORDER BY fcr.forgotDate DESC", ForgotClockRequest.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra xem đã có yêu cầu cho ngày này chưa
     */
    public boolean hasRequestForDate(UUID employeeId, LocalDate forgotDate, UUID excludeRequestId) {
        EntityManager em = emf.createEntityManager();
        try {
            String query = "SELECT COUNT(fcr) FROM ForgotClockRequest fcr " +
                    "WHERE fcr.employee.employeeID = :employeeId " +
                    "AND fcr.forgotDate = :forgotDate " +
                    "AND fcr.status IN ('Chờ duyệt', 'Đã duyệt')";

            if (excludeRequestId != null) {
                query += " AND fcr.forgotClockRequestId != :excludeRequestId";
            }

            var q = em.createQuery(query, Long.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("forgotDate", forgotDate);

            if (excludeRequestId != null) {
                q.setParameter("excludeRequestId", excludeRequestId);
            }

            return q.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Đếm số yêu cầu đang chờ duyệt của employee
     */
    public long countPendingByEmployee(UUID employeeId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(fcr) FROM ForgotClockRequest fcr " +
                                    "WHERE fcr.employee.employeeID = :employeeId " +
                                    "AND fcr.status = 'Chờ duyệt'", Long.class)
                    .setParameter("employeeId", employeeId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra xem employee có phải là owner của request không
     */
    public boolean isOwner(UUID requestId, UUID employeeId) {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(fcr) FROM ForgotClockRequest fcr " +
                                    "WHERE fcr.forgotClockRequestId = :requestId " +
                                    "AND fcr.employee.employeeID = :employeeId", Long.class)
                    .setParameter("requestId", requestId)
                    .setParameter("employeeId", employeeId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}

