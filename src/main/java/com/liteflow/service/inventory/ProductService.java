package com.liteflow.service.inventory;

import com.liteflow.dao.inventory.ProductDAO;
import com.liteflow.model.inventory.Product;
import com.liteflow.model.inventory.ProductDisplayDTO;
import com.liteflow.model.inventory.ProductPriceDTO;
import com.liteflow.dao.BaseDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductService {
    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public List<Product> getAllProducts() {
        try {
            List<Product> products = productDAO.findAll();
            return products;
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong ProductService.getAllProducts(): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<ProductDisplayDTO> getAllProductsWithPriceAndStock() {
        try {
            EntityManager em = BaseDAO.emf.createEntityManager();
            List<ProductDisplayDTO> result = new ArrayList<>();
            
            try {
                // Query để lấy thông tin sản phẩm với giá, tồn kho và category
                String jpql = "SELECT p.productId, p.name, pv.size, pv.price, " +
                           "COALESCE(ps.amount, 0) as stockAmount, " +
                           "p.isDeleted, p.imageUrl, c.name as categoryName, " +
                           "p.productType, p.description, p.status, p.unit, p.importDate " +
                           "FROM Product p " +
                           "LEFT JOIN ProductVariant pv ON p.productId = pv.product.productId " +
                           "LEFT JOIN ProductStock ps ON pv.productVariantId = ps.productVariant.productVariantId " +
                           "LEFT JOIN ProductCategory pc ON p.productId = pc.product.productId " +
                           "LEFT JOIN Category c ON pc.category.categoryId = c.categoryId " +
                           "WHERE (p.isDeleted = false OR p.isDeleted IS NULL) " +
                           "AND (pv.isDeleted = false OR pv.isDeleted IS NULL) " +
                           "ORDER BY p.importDate DESC, p.name, pv.size";
                
                Query query = em.createQuery(jpql);
                
                List<Object[]> results = query.getResultList();
                
                for (Object[] row : results) {
                    ProductDisplayDTO dto = new ProductDisplayDTO();
                    dto.setProductId((UUID) row[0]);
                    dto.setProductName((String) row[1]);
                    dto.setSize((String) row[2]);
                    // Convert BigDecimal to Double
                    if (row[3] != null) {
                        dto.setPrice(((java.math.BigDecimal) row[3]).doubleValue());
                    } else {
                        dto.setPrice(0.0);
                    }
                    dto.setStockAmount(((Number) row[4]).intValue());
                    dto.setIsDeleted((Boolean) row[5]);
                    dto.setImageUrl((String) row[6]);
                    dto.setCategoryName((String) row[7]);
                    dto.setProductType((String) row[8]);
                    dto.setDescription((String) row[9]);
                    dto.setStatus((String) row[10]);
                    dto.setUnit((String) row[11]);
                    
                    // Tạo mã sản phẩm từ ID
                    dto.setProductCode("SP" + String.format("%06d", Math.abs(dto.getProductId().hashCode()) % 1000000));
                    
                    result.add(dto);
                }
                
                return result;
                
            } finally {
                em.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong ProductService.getAllProductsWithPriceAndStock(): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Product getById(UUID id) {
        return productDAO.findById(id);
    }
    
    public boolean isProductNameExists(String productName) {
        try {
            EntityManager em = BaseDAO.emf.createEntityManager();
            try {
                String jpql = "SELECT COUNT(p) FROM Product p WHERE p.name = :name AND p.isDeleted = false";
                Query query = em.createQuery(jpql);
                query.setParameter("name", productName.trim());
                Long count = (Long) query.getSingleResult();
                boolean exists = count > 0;
                return exists;
            } finally {
                em.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking product name existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isProductNameExistsExcludingId(String productName, UUID excludeId) {
        try {
            EntityManager em = BaseDAO.emf.createEntityManager();
            try {
                String jpql = "SELECT COUNT(p) FROM Product p WHERE p.name = :name AND p.isDeleted = false AND p.productId != :excludeId";
                Query query = em.createQuery(jpql);
                query.setParameter("name", productName.trim());
                query.setParameter("excludeId", excludeId);
                Long count = (Long) query.getSingleResult();
                boolean exists = count > 0;
                return exists;
            } finally {
                em.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking product name existence (excluding ID): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addProduct(Product p) {
        return productDAO.insert(p);
    }

    public boolean updateProduct(Product p) {
        return productDAO.update(p);
    }

    public boolean deleteProduct(UUID id) {
        return productDAO.delete(id);
    }
    
    public List<ProductPriceDTO> getAllProductsWithPriceInfo() {
        try {
            EntityManager em = BaseDAO.emf.createEntityManager();
            List<ProductPriceDTO> result = new ArrayList<>();
            
            try {
                // Query đơn giản để lấy thông tin sản phẩm với giá
                String jpql = "SELECT p.productId, p.name, pv.size, pv.originalPrice, pv.price, " +
                           "p.isDeleted, c.name as categoryName " +
                           "FROM Product p " +
                           "LEFT JOIN ProductVariant pv ON p.productId = pv.product.productId " +
                           "LEFT JOIN ProductCategory pc ON p.productId = pc.product.productId " +
                           "LEFT JOIN Category c ON pc.category.categoryId = c.categoryId " +
                           "WHERE (pv.isDeleted = false OR pv.isDeleted IS NULL) " +
                           "AND (p.isDeleted = false OR p.isDeleted IS NULL) " +
                           "ORDER BY p.name, pv.size";
                
                Query query = em.createQuery(jpql);
                
                List<Object[]> results = query.getResultList();
                
                for (Object[] row : results) {
                    ProductPriceDTO dto = new ProductPriceDTO();
                    dto.setProductId((UUID) row[0]);
                    dto.setProductName((String) row[1]);
                    dto.setSize((String) row[2]);
                    // Convert BigDecimal to Double
                    if (row[3] != null) {
                        dto.setOriginalPrice(((java.math.BigDecimal) row[3]).doubleValue());
                    } else {
                        dto.setOriginalPrice(0.0);
                    }
                    if (row[4] != null) {
                        dto.setSellingPrice(((java.math.BigDecimal) row[4]).doubleValue());
                    } else {
                        dto.setSellingPrice(0.0);
                    }
                    dto.setIsDeleted((Boolean) row[5]);
                    dto.setCategoryName((String) row[6]);
                    
                    // Tạo mã sản phẩm từ ID
                    dto.setProductCode("SP" + String.format("%06d", Math.abs(dto.getProductId().hashCode()) % 1000000));
                    
                    result.add(dto);
                }
                
                return result;
                
            } finally {
                em.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong ProductService.getAllProductsWithPriceInfo(): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<String> getDistinctCategoriesFromProducts() {
        try {
            EntityManager em = BaseDAO.emf.createEntityManager();
            
            try {
                // Query để lấy các danh mục khác nhau từ bảng Category
                String jpql = "SELECT DISTINCT c.name " +
                           "FROM Category c " +
                           "WHERE c.name IS NOT NULL " +
                           "ORDER BY c.name";
                
                Query query = em.createQuery(jpql);
                
                List<String> result = query.getResultList();
                
                return result;
                
            } finally {
                em.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong ProductService.getDistinctCategoriesFromProducts(): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<String> getAllUnits() {
        try {
            List<String> result = productDAO.getAllUnits();
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong ProductService.getAllUnits(): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
