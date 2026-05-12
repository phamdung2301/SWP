package com.liteflow.dao;

import com.liteflow.model.CompanyInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.UUID;

/**
 * CompanyInfoDAO - DAO for CompanyInfo entity
 * Singleton pattern: Only one CompanyInfo record should exist in database
 */
public class CompanyInfoDAO extends BaseDAO<CompanyInfo, UUID> {

    @Override
    public java.util.List<CompanyInfo> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM CompanyInfo c", CompanyInfo.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("❌ Error in CompanyInfoDAO.getAll(): " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        } finally {
            em.close();
        }
    }

    @Override
    public CompanyInfo findById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(CompanyInfo.class, id);
        } catch (Exception e) {
            System.err.println("❌ Error in CompanyInfoDAO.findById(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Get the single CompanyInfo record (singleton pattern)
     * @return CompanyInfo or null if not found
     */
    public CompanyInfo getCompanyInfo() {
        EntityManager em = emf.createEntityManager();
        try {
            java.util.List<CompanyInfo> list = em.createQuery(
                "SELECT c FROM CompanyInfo c ORDER BY c.createdAt ASC", 
                CompanyInfo.class
            ).setMaxResults(1).getResultList();
            
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("❌ Error in CompanyInfoDAO.getCompanyInfo(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean insert(CompanyInfo companyInfo) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            // Check if company info already exists (singleton pattern)
            CompanyInfo existing = getCompanyInfo();
            if (existing != null) {
                System.out.println("⚠️ CompanyInfo already exists. Use update() instead.");
                return false;
            }
            
            tx.begin();
            em.persist(companyInfo);
            tx.commit();
            System.out.println("✅ CompanyInfo inserted successfully");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error in CompanyInfoDAO.insert(): " + e.getMessage());
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean update(CompanyInfo companyInfo) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(companyInfo);
            tx.commit();
            System.out.println("✅ CompanyInfo updated successfully");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error in CompanyInfoDAO.update(): " + e.getMessage());
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(UUID id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CompanyInfo companyInfo = em.find(CompanyInfo.class, id);
            if (companyInfo != null) {
                em.remove(companyInfo);
                tx.commit();
                System.out.println("✅ CompanyInfo deleted successfully");
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error in CompanyInfoDAO.delete(): " + e.getMessage());
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }
}

