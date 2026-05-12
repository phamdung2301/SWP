package com.liteflow.dao.timesheet;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.timesheet.PersonalSchedule;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PersonalScheduleDAO extends GenericDAO<PersonalSchedule, UUID> {

    public PersonalScheduleDAO() {
        super(PersonalSchedule.class, UUID.class);
    }

    /**
     * Lấy tất cả lịch cá nhân của một employee
     */
    public List<PersonalSchedule> findByEmployeeId(UUID employeeId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM PersonalSchedule p WHERE p.employee.employeeID = :employeeId " +
                            "ORDER BY p.startDate ASC, p.startTime ASC", PersonalSchedule.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy lịch cá nhân trong khoảng thời gian
     */
    public List<PersonalSchedule> findByEmployeeAndDateRange(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM PersonalSchedule p WHERE p.employee.employeeID = :employeeId " +
                            "AND p.startDate >= :startDate AND p.startDate <= :endDate " +
                            "ORDER BY p.startDate ASC, p.startTime ASC", PersonalSchedule.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy lịch cá nhân theo ngày cụ thể
     */
    public List<PersonalSchedule> findByEmployeeAndDate(UUID employeeId, LocalDate date) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM PersonalSchedule p WHERE p.employee.employeeID = :employeeId " +
                            "AND p.startDate = :date " +
                            "ORDER BY p.startTime ASC", PersonalSchedule.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("date", date)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy lịch cá nhân theo priority
     */
    public List<PersonalSchedule> findByEmployeeAndPriority(UUID employeeId, String priority) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM PersonalSchedule p WHERE p.employee.employeeID = :employeeId " +
                            "AND p.priority = :priority " +
                            "ORDER BY p.startDate ASC, p.startTime ASC", PersonalSchedule.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("priority", priority)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy lịch cá nhân theo status
     */
    public List<PersonalSchedule> findByEmployeeAndStatus(UUID employeeId, String status) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM PersonalSchedule p WHERE p.employee.employeeID = :employeeId " +
                            "AND p.status = :status " +
                            "ORDER BY p.startDate ASC, p.startTime ASC", PersonalSchedule.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}


