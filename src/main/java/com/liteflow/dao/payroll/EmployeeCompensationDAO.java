package com.liteflow.dao.payroll;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.payroll.EmployeeCompensation;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DAO cho EmployeeCompensation entity
 */
public class EmployeeCompensationDAO extends GenericDAO<EmployeeCompensation, UUID> {

    public EmployeeCompensationDAO() {
        super(EmployeeCompensation.class, UUID.class);
    }

    /**
     * Lấy compensation đang active của một nhân viên
     */
    public EmployeeCompensation getActiveCompensation(UUID employeeId) {
        if (employeeId == null) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT ec FROM EmployeeCompensation ec " +
                         "WHERE ec.employee.employeeID = :employeeId " +
                         "AND ec.isActive = true " +
                         "ORDER BY ec.effectiveFrom DESC";
            List<EmployeeCompensation> result = em.createQuery(jpql, EmployeeCompensation.class)
                    .setParameter("employeeId", employeeId)
                    .setMaxResults(1)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception e) {
            System.err.println("Error getting active compensation: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy compensation đang active của nhân viên theo employee code
     */
    public EmployeeCompensation getActiveCompensationByCode(String employeeCode) {
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT ec FROM EmployeeCompensation ec " +
                         "WHERE ec.employee.employeeCode = :code " +
                         "AND ec.isActive = true " +
                         "ORDER BY ec.effectiveFrom DESC";
            List<EmployeeCompensation> result = em.createQuery(jpql, EmployeeCompensation.class)
                    .setParameter("code", employeeCode.trim())
                    .setMaxResults(1)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception e) {
            System.err.println("Error getting active compensation by code: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả compensation history của một nhân viên
     */
    public List<EmployeeCompensation> getCompensationHistory(UUID employeeId) {
        if (employeeId == null) {
            return Collections.emptyList();
        }

        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT ec FROM EmployeeCompensation ec " +
                         "WHERE ec.employee.employeeID = :employeeId " +
                         "ORDER BY ec.effectiveFrom DESC";
            return em.createQuery(jpql, EmployeeCompensation.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error getting compensation history: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    /**
     * Vô hiệu hóa tất cả compensation cũ của nhân viên
     */
    public boolean deactivateOldCompensations(UUID employeeId) {
        if (employeeId == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            String jpql = "UPDATE EmployeeCompensation ec " +
                         "SET ec.isActive = false " +
                         "WHERE ec.employee.employeeID = :employeeId " +
                         "AND ec.isActive = true";
            int updated = em.createQuery(jpql)
                    .setParameter("employeeId", employeeId)
                    .executeUpdate();
            tx.commit();
            System.out.println("Deactivated " + updated + " old compensation records");
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error deactivating old compensations: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả active compensations với Employee được eager fetch
     */
    public List<EmployeeCompensation> getAllActiveCompensations() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT ec FROM EmployeeCompensation ec " +
                         "JOIN FETCH ec.employee e " +
                         "WHERE ec.isActive = true " +
                         "ORDER BY e.fullName";
            return em.createQuery(jpql, EmployeeCompensation.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error getting all active compensations: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách compensations theo loại
     */
    public List<EmployeeCompensation> getCompensationsByType(String compensationType) {
        if (compensationType == null || compensationType.trim().isEmpty()) {
            return Collections.emptyList();
        }

        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT ec FROM EmployeeCompensation ec " +
                         "WHERE ec.compensationType = :type " +
                         "AND ec.isActive = true " +
                         "ORDER BY ec.employee.fullName";
            return em.createQuery(jpql, EmployeeCompensation.class)
                    .setParameter("type", compensationType.trim())
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error getting compensations by type: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
}
