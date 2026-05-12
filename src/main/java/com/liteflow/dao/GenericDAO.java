package com.liteflow.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import com.liteflow.util.Utils;

import java.util.Collections;
import java.util.List;

public class GenericDAO<T, ID> extends BaseDAO<T, ID> {

    private final Class<T> entityClass;

    public GenericDAO(Class<T> entityClass, Class<ID> idClass) {
        this.entityClass = entityClass;
    }

    public T findByField(String fieldName, Object value) {
        if (value == null) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :val";
            List<T> list = em.createQuery(jpql, entityClass)
                    .setParameter("val", value)
                    .setMaxResults(1)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public List<T> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("❌ Error in getAll(): " + e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean insert(T t) {
        if (t == null) {
            System.err.println("❌ Cannot insert null entity");
            return false;
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(t);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("LỖI INSERT: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean update(T t) {
        if (t == null) {
            System.err.println("❌ Cannot update null entity");
            return false;
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(t);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(ID id) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            System.out.println("=== DEBUG: GenericDAO.delete ===");
            System.out.println("Entity class: " + entityClass.getSimpleName());
            System.out.println("ID to delete: " + id);
            
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                System.out.println("Entity found: " + entity);
                em.remove(entity);
                tx.commit();
                System.out.println("✅ Entity deleted successfully");
                return true;
            } else {
                System.out.println("❌ Entity not found with ID: " + id);
                tx.rollback();
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Exception in GenericDAO.delete: " + e.getMessage());
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
    public T findById(ID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(entityClass, id);
        } catch (Exception e) {
            System.err.println("❌ Error finding entity: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public List<T> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("❌ Name parameter is null or empty");
            return Collections.emptyList();
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery(entityClass.getSimpleName() + ".findByName", entityClass)
                    .setParameter("name", "%" + name.trim() + "%")
                    .getResultList();
        } catch (Exception e) {
            System.err.println("❌ Error in findByName: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<T> listWithOffset(int page, int pageSize) {
        if (page <= 0 || pageSize <= 0) {
            System.err.println("❌ Invalid pagination parameters: page=" + page + ", pageSize=" + pageSize);
            return Collections.emptyList();
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery(entityClass.getSimpleName() + ".listWithOffset", entityClass)
                    .setFirstResult((page - 1) * pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("❌ Error in listWithOffset: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<T> findByAttribute(String attributeName, Object value) {
        if (attributeName == null || attributeName.trim().isEmpty()) {
            System.err.println("❌ Attribute name is null or empty");
            return Collections.emptyList();
        }

        if (value == null) {
            System.err.println("❌ Search value is null");
            return Collections.emptyList();
        }

        EntityManager em = emf.createEntityManager();
        List<T> resultList = Collections.emptyList();
        try {
            String queryName = entityClass.getSimpleName() + ".findBy" + Utils.capitalizeFirstLetter(attributeName);
            System.out.println("Executing NamedQuery: " + queryName + " with value: " + value);

            resultList = em.createNamedQuery(queryName, entityClass)
                    .setParameter(attributeName, value)
                    .getResultList();
        } catch (IllegalArgumentException e) {
            System.err.println("❌ ERROR: NamedQuery '" + attributeName + "' không tồn tại hoặc tham số không hợp lệ: " + e.getMessage());
        } catch (PersistenceException e) {
            System.err.println("❌ Database Error khi thực thi query: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected Error trong findByAttribute: " + e.getMessage());
        } finally {
            em.close();
        }
        return resultList;
    }

    public boolean hasNextPage(int page, int pageSize) {
        if (page < 0 || pageSize <= 0) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            List<T> result = em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(1)
                    .getResultList();

            return !result.isEmpty();
        } catch (Exception e) {
            System.err.println("❌ Error in hasNextPage: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                    .getSingleResult();
        } catch (Exception e) {
            System.err.println("❌ Error in count: " + e.getMessage());
            return 0L;
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.createdAt DESC", entityClass)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("❌ Error in findAll: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

//    /**
//     * Tìm 1 bản ghi theo field (so khớp chính xác).
//     */
    public T findSingleByField(String fieldName, Object value) {
        if (fieldName == null || fieldName.isBlank() || value == null) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :val";
            List<T> result = em.createQuery(jpql, entityClass)
                    .setParameter("val", value)
                    .setMaxResults(1)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception e) {
            System.err.println("❌ Error in findSingleByField: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

//    /**
//     * Tìm 1 bản ghi theo field String (so khớp case-insensitive).
//     */
    public T findSingleByFieldIgnoreCase(String fieldName, String value) {
        if (fieldName == null || value == null || value.isBlank()) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName()
                    + " e WHERE LOWER(e." + fieldName + ") = :val";
            List<T> result = em.createQuery(jpql, entityClass)
                    .setParameter("val", value.toLowerCase())
                    .setMaxResults(1)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }
}
