package com.liteflow.dao.inventory;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.inventory.ProductStock;
import com.liteflow.model.inventory.LowStockItem;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ProductStockDAO extends GenericDAO<ProductStock, UUID> {

    public ProductStockDAO() {
        super(ProductStock.class, UUID.class);
    }
    
    /**
     * Find products with low stock levels
     * @param threshold Stock threshold (alert when stock < threshold)
     * @return List of low stock items
     */
    public List<LowStockItem> findLowStock(int threshold) {
        if (threshold <= 0) {
            System.err.println("❌ Invalid threshold: " + threshold);
            return Collections.emptyList();
        }
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                "SELECT NEW com.liteflow.model.inventory.LowStockItem(" +
                "  p.productId, " +
                "  pv.productVariantId, " +
                "  p.name, " +
                "  pv.size, " +
                "  ps.amount" +
                ") " +
                "FROM ProductStock ps " +
                "JOIN ps.productVariant pv " +
                "JOIN pv.product p " +
                "WHERE p.isDeleted = false " +
                "  AND pv.isDeleted = false " +
                "  AND ps.amount > 0 " +
                "  AND ps.amount < :threshold " +
                "ORDER BY ps.amount ASC";
            
            List<LowStockItem> results = em.createQuery(jpql, LowStockItem.class)
                     .setParameter("threshold", threshold)
                     .getResultList();
            
            // Set threshold for each item
            for (LowStockItem item : results) {
                item.setThreshold(threshold);
            }
            
            System.out.println("✅ Found " + results.size() + " low stock items (threshold=" + threshold + ")");
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error finding low stock: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Find products that are out of stock (amount = 0)
     * @return List of out-of-stock items
     */
    public List<LowStockItem> findOutOfStock() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                "SELECT NEW com.liteflow.model.inventory.LowStockItem(" +
                "  p.productId, " +
                "  pv.productVariantId, " +
                "  p.name, " +
                "  pv.size, " +
                "  ps.amount" +
                ") " +
                "FROM ProductStock ps " +
                "JOIN ps.productVariant pv " +
                "JOIN pv.product p " +
                "WHERE p.isDeleted = false " +
                "  AND pv.isDeleted = false " +
                "  AND ps.amount = 0 " +
                "ORDER BY p.name ASC";
            
            List<LowStockItem> results = em.createQuery(jpql, LowStockItem.class)
                     .getResultList();
            
            System.out.println("✅ Found " + results.size() + " out-of-stock items");
            return results;
            
        } catch (Exception e) {
            System.err.println("❌ Error finding out-of-stock items: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Get total stock for a product (all variants combined)
     * @param productId Product ID
     * @return Total stock amount
     */
    public int getTotalStockByProduct(UUID productId) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                "SELECT SUM(ps.amount) " +
                "FROM ProductStock ps " +
                "JOIN ps.productVariant pv " +
                "WHERE pv.product.productId = :productId " +
                "  AND pv.isDeleted = false";
            
            Long total = em.createQuery(jpql, Long.class)
                          .setParameter("productId", productId)
                          .getSingleResult();
            
            return total != null ? total.intValue() : 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting total stock: " + e.getMessage());
            return 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Count total out-of-stock items
     * @return Number of out-of-stock items
     */
    public int countOutOfStock() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                "SELECT COUNT(ps) " +
                "FROM ProductStock ps " +
                "JOIN ps.productVariant pv " +
                "JOIN pv.product p " +
                "WHERE p.isDeleted = false " +
                "  AND pv.isDeleted = false " +
                "  AND ps.amount = 0";
            
            Long count = em.createQuery(jpql, Long.class).getSingleResult();
            return count != null ? count.intValue() : 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error counting out-of-stock: " + e.getMessage());
            return 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Count low stock items
     * @param threshold Stock threshold
     * @return Number of low stock items
     */
    public int countLowStock(int threshold) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                "SELECT COUNT(ps) " +
                "FROM ProductStock ps " +
                "JOIN ps.productVariant pv " +
                "JOIN pv.product p " +
                "WHERE p.isDeleted = false " +
                "  AND pv.isDeleted = false " +
                "  AND ps.amount > 0 " +
                "  AND ps.amount < :threshold";
            
            Long count = em.createQuery(jpql, Long.class)
                          .setParameter("threshold", threshold)
                          .getSingleResult();
            return count != null ? count.intValue() : 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error counting low stock: " + e.getMessage());
            return 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Check if a specific product variant is out of stock
     * @param productVariantID Product variant ID
     * @return true if out of stock (amount = 0)
     */
    public boolean isOutOfStock(UUID productVariantID) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                "SELECT ps.amount " +
                "FROM ProductStock ps " +
                "WHERE ps.productVariant.productVariantId = :variantId";
            
            Integer amount = em.createQuery(jpql, Integer.class)
                               .setParameter("variantId", productVariantID)
                               .setMaxResults(1)
                               .getSingleResult();
            
            return amount != null && amount == 0;
            
        } catch (Exception e) {
            // If no stock record found, consider as out of stock
            return true;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Get stock level for a specific product variant
     * @param productVariantID Product variant ID
     * @return Current stock amount (0 if not found)
     */
    public int getStockLevel(UUID productVariantID) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            String jpql = 
                "SELECT ps.amount " +
                "FROM ProductStock ps " +
                "WHERE ps.productVariant.productVariantId = :variantId";
            
            Integer amount = em.createQuery(jpql, Integer.class)
                               .setParameter("variantId", productVariantID)
                               .setMaxResults(1)
                               .getSingleResult();
            
            return amount != null ? amount : 0;
            
        } catch (Exception e) {
            return 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
