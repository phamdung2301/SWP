package com.liteflow.dao.inventory;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.inventory.Product;
import com.liteflow.model.inventory.Category;
import com.liteflow.model.inventory.ProductCategory;
import java.util.UUID;
import java.util.List;

public class ProductDAO extends GenericDAO<Product, UUID> {

    public ProductDAO() {
        super(Product.class, UUID.class);
    }
    
    /**
     * Get all distinct units from products
     */
    public List<String> getAllUnits() {
        var em = emf.createEntityManager();
        try {
            String jpql = "SELECT DISTINCT p.unit FROM Product p WHERE p.unit IS NOT NULL AND p.unit != '' ORDER BY p.unit";
            return em.createQuery(jpql, String.class).getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Find category by name (returns first match or null)
     */
    public Category findCategoryByName(String name) {
        if (name == null) {
            return null;
        }
        var em = emf.createEntityManager();
        try {
            var query = em.createQuery("SELECT c FROM Category c WHERE c.name = :name", Category.class);
            query.setParameter("name", name);
            var results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    /**
     * Check if category exists by name
     */
    public boolean categoryExists(String name) {
        return findCategoryByName(name) != null;
    }
    
    /**
     * Add new category or return existing one
     */
    public Category addCategoryIfNotExists(String categoryName) {
        Category existing = findCategoryByName(categoryName.trim());
        if (existing != null) {
            return existing;
        }
        
        // Create new category
        Category category = new Category();
        category.setName(categoryName.trim());
        category.setDescription(null);
        
        // Use EntityManager directly to insert Category
        var em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(category);
            tx.commit();
            System.out.println("✅ New category created: " + category.getCategoryId());
            return findCategoryByName(categoryName.trim()); // Fetch the newly created category
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("❌ Failed to create category: " + categoryName + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
    
    /**
     * Insert ProductCategory into database
     */
    public boolean addProductCategory(ProductCategory productCategory) {
        if (productCategory == null) {
            return false;
        }
        var em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(productCategory);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("❌ Error inserting ProductCategory: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Check if category is being used by any product
     */
    public boolean isCategoryInUse(UUID categoryId) {
        var em = emf.createEntityManager();
        try {
            var query = em.createQuery("SELECT COUNT(pc) FROM ProductCategory pc WHERE pc.category.categoryId = :categoryId", Long.class);
            query.setParameter("categoryId", categoryId);
            Long count = query.getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Delete category from database
     */
    public boolean deleteCategory(UUID categoryId) {
        var em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            Category category = em.find(Category.class, categoryId);
            if (category != null) {
                em.remove(category);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("❌ Error deleting category: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
