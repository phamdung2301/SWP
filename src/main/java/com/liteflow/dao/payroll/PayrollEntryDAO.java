package com.liteflow.dao.payroll;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.payroll.PayrollEntry;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DAO for PayrollEntry operations
 */
public class PayrollEntryDAO extends GenericDAO<PayrollEntry, UUID> {

    public PayrollEntryDAO() {
        super(PayrollEntry.class, UUID.class);
    }

    /**
     * Find payroll entries by employee and month/year
     */
    public List<PayrollEntry> findByEmployeeAndMonth(UUID employeeId, int month, int year) {
        if (employeeId == null) {
            return Collections.emptyList();
        }

        EntityManager em = emf.createEntityManager();
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = LocalDate.of(year, month, startDate.lengthOfMonth());

            String jpql = "SELECT pe FROM PayrollEntry pe " +
                         "JOIN FETCH pe.employee e " +
                         "JOIN FETCH pe.payrollRun pr " +
                         "JOIN FETCH pr.payPeriod pp " +
                         "WHERE e.employeeID = :employeeId " +
                         "AND pp.startDate <= :endDate " +
                         "AND (pp.endDate IS NULL OR pp.endDate >= :startDate) " +
                         "ORDER BY pe.createdAt DESC";

            return em.createQuery(jpql, PayrollEntry.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error finding payroll entries by employee and month: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    /**
     * Get total paid amount for an employee in a specific month
     */
    public BigDecimal getTotalPaidForMonth(UUID employeeId, int month, int year) {
        if (employeeId == null) {
            return BigDecimal.ZERO;
        }

        EntityManager em = emf.createEntityManager();
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = LocalDate.of(year, month, startDate.lengthOfMonth());

            String jpql = "SELECT COALESCE(SUM(pe.netPay), 0) FROM PayrollEntry pe " +
                         "JOIN pe.employee e " +
                         "JOIN pe.payrollRun pr " +
                         "JOIN pr.payPeriod pp " +
                         "WHERE e.employeeID = :employeeId " +
                         "AND pe.isPaid = true " +
                         "AND pp.startDate <= :endDate " +
                         "AND (pp.endDate IS NULL OR pp.endDate >= :startDate)";

            Object result = em.createQuery(jpql)
                    .setParameter("employeeId", employeeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getSingleResult();

            if (result instanceof BigDecimal) {
                return (BigDecimal) result;
            } else if (result instanceof Number) {
                return BigDecimal.valueOf(((Number) result).doubleValue());
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error getting total paid for month: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }

    /**
     * Mark payroll entry as paid
     */
    public boolean markAsPaid(UUID payrollEntryId) {
        if (payrollEntryId == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            PayrollEntry entry = em.find(PayrollEntry.class, payrollEntryId);
            if (entry != null) {
                entry.setIsPaid(true);
                em.merge(entry);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error marking payroll entry as paid: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Find payroll entry by employee, month, and year (for creating if not exists)
     */
    public PayrollEntry findByEmployeeAndMonthYear(UUID employeeId, int month, int year) {
        if (employeeId == null) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = LocalDate.of(year, month, startDate.lengthOfMonth());

            String jpql = "SELECT pe FROM PayrollEntry pe " +
                         "JOIN FETCH pe.employee e " +
                         "JOIN FETCH pe.payrollRun pr " +
                         "JOIN FETCH pr.payPeriod pp " +
                         "WHERE e.employeeID = :employeeId " +
                         "AND pp.startDate <= :endDate " +
                         "AND (pp.endDate IS NULL OR pp.endDate >= :startDate) " +
                         "ORDER BY pe.createdAt DESC";

            List<PayrollEntry> results = em.createQuery(jpql, PayrollEntry.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setMaxResults(1)
                    .getResultList();

            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("Error finding payroll entry: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Get all payroll entries for a specific month/year (for admin paysheet view)
     */
    public List<PayrollEntry> findByMonth(int month, int year) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = LocalDate.of(year, month, startDate.lengthOfMonth());

            String jpql = "SELECT pe FROM PayrollEntry pe " +
                         "JOIN FETCH pe.employee e " +
                         "JOIN FETCH pe.payrollRun pr " +
                         "JOIN FETCH pr.payPeriod pp " +
                         "WHERE pp.startDate <= :endDate " +
                         "AND (pp.endDate IS NULL OR pp.endDate >= :startDate) " +
                         "ORDER BY e.employeeCode ASC";

            return em.createQuery(jpql, PayrollEntry.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error finding payroll entries by month: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
}

