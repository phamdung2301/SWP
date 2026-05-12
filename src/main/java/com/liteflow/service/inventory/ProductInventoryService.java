package com.liteflow.service.inventory;

import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.ProductDisplayDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Product & Inventory queries
 * Designed for chatbot integration - returns JSONObject for easy GPT integration
 */
public class ProductInventoryService {
    
    private final ProductService productService;
    
    public ProductInventoryService() {
        this.productService = new ProductService();
    }
    
    /**
     * Get products summary: total count, categories count, etc.
     */
    public JSONObject getProductsSummary() {
        JSONObject summary = new JSONObject();
        
        try {
            List<ProductDisplayDTO> allProducts = productService.getAllProductsWithPriceAndStock();
            
            int totalProducts = (int) allProducts.stream()
                    .map(ProductDisplayDTO::getProductName)
                    .distinct()
                    .count();
            
            int totalVariants = allProducts.size();
            
            Set<String> categories = allProducts.stream()
                    .map(ProductDisplayDTO::getCategoryName)
                    .filter(Objects::nonNull)
                    .filter(cat -> !cat.isEmpty())
                    .collect(Collectors.toSet());
            
            long totalStock = allProducts.stream()
                    .mapToLong(ProductDisplayDTO::getStockAmount)
                    .sum();
            
            int outOfStockCount = (int) allProducts.stream()
                    .filter(p -> p.getStockAmount() == 0)
                    .count();
            
            summary.put("success", true);
            summary.put("totalProducts", totalProducts);
            summary.put("totalVariants", totalVariants);
            summary.put("totalCategories", categories.size());
            summary.put("categories", new JSONArray(categories));
            summary.put("totalStock", totalStock);
            summary.put("outOfStockCount", outOfStockCount);
            
        } catch (Exception e) {
            System.err.println("❌ Error in getProductsSummary: " + e.getMessage());
            e.printStackTrace();
            summary.put("success", false);
            summary.put("error", e.getMessage());
        }
        
        return summary;
    }
    
    /**
     * Get product list with optional filters
     */
    public JSONObject getProductList(String categoryFilter, Boolean inStockOnly) {
        JSONObject result = new JSONObject();
        JSONArray products = new JSONArray();
        
        try {
            List<ProductDisplayDTO> allProducts = productService.getAllProductsWithPriceAndStock();
            
            // Apply filters
            List<ProductDisplayDTO> filtered = allProducts.stream()
                    .filter(p -> categoryFilter == null || categoryFilter.isEmpty() || 
                            (p.getCategoryName() != null && p.getCategoryName().equalsIgnoreCase(categoryFilter)))
                    .filter(p -> inStockOnly == null || !inStockOnly || p.getStockAmount() > 0)
                    .collect(Collectors.toList());
            
            // Group by product name
            Map<String, List<ProductDisplayDTO>> grouped = filtered.stream()
                    .collect(Collectors.groupingBy(ProductDisplayDTO::getProductName));
            
            for (Map.Entry<String, List<ProductDisplayDTO>> entry : grouped.entrySet()) {
                JSONObject productJson = new JSONObject();
                String productName = entry.getKey();
                List<ProductDisplayDTO> variants = entry.getValue();
                
                ProductDisplayDTO first = variants.get(0);
                productJson.put("productId", first.getProductId().toString());
                productJson.put("productName", productName);
                productJson.put("categoryName", first.getCategoryName());
                productJson.put("productType", first.getProductType());
                productJson.put("status", first.getStatus());
                productJson.put("unit", first.getUnit());
                productJson.put("description", first.getDescription());
                
                // Variants array
                JSONArray variantsArray = new JSONArray();
                int totalStock = 0;
                double minPrice = Double.MAX_VALUE;
                double maxPrice = 0;
                
                for (ProductDisplayDTO variant : variants) {
                    JSONObject variantJson = new JSONObject();
                    variantJson.put("size", variant.getSize());
                    variantJson.put("price", variant.getPrice());
                    variantJson.put("stockAmount", variant.getStockAmount());
                    variantsArray.put(variantJson);
                    
                    totalStock += variant.getStockAmount();
                    if (variant.getPrice() != null) {
                        minPrice = Math.min(minPrice, variant.getPrice());
                        maxPrice = Math.max(maxPrice, variant.getPrice());
                    }
                }
                
                productJson.put("variants", variantsArray);
                productJson.put("totalStock", totalStock);
                productJson.put("minPrice", minPrice == Double.MAX_VALUE ? 0 : minPrice);
                productJson.put("maxPrice", maxPrice);
                
                products.put(productJson);
            }
            
            result.put("success", true);
            result.put("products", products);
            result.put("count", products.length());
            
        } catch (Exception e) {
            System.err.println("❌ Error in getProductList: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get product details by name (fuzzy match)
     */
    public JSONObject getProductDetails(String productName) {
        JSONObject result = new JSONObject();
        
        try {
            List<ProductDisplayDTO> allProducts = productService.getAllProductsWithPriceAndStock();
            
            // Fuzzy matching: find products containing the search term
            List<ProductDisplayDTO> matches = allProducts.stream()
                    .filter(p -> p.getProductName() != null && 
                            p.getProductName().toLowerCase().contains(productName.toLowerCase()))
                    .collect(Collectors.toList());
            
            if (matches.isEmpty()) {
                result.put("success", false);
                result.put("error", "Không tìm thấy sản phẩm: " + productName);
                return result;
            }
            
            // Group by product name
            Map<String, List<ProductDisplayDTO>> grouped = matches.stream()
                    .collect(Collectors.groupingBy(ProductDisplayDTO::getProductName));
            
            // If multiple matches, return first one or return list
            String exactMatch = grouped.keySet().stream()
                    .filter(name -> name.equalsIgnoreCase(productName))
                    .findFirst()
                    .orElse(grouped.keySet().iterator().next());
            
            List<ProductDisplayDTO> variants = grouped.get(exactMatch);
            ProductDisplayDTO first = variants.get(0);
            
            JSONObject productJson = new JSONObject();
            productJson.put("productId", first.getProductId().toString());
            productJson.put("productName", first.getProductName());
            productJson.put("categoryName", first.getCategoryName());
            productJson.put("productType", first.getProductType());
            productJson.put("status", first.getStatus());
            productJson.put("unit", first.getUnit());
            productJson.put("description", first.getDescription());
            
            JSONArray variantsArray = new JSONArray();
            int totalStock = 0;
            
            for (ProductDisplayDTO variant : variants) {
                JSONObject variantJson = new JSONObject();
                variantJson.put("size", variant.getSize());
                variantJson.put("price", variant.getPrice());
                variantJson.put("stockAmount", variant.getStockAmount());
                variantsArray.put(variantJson);
                totalStock += variant.getStockAmount();
            }
            
            productJson.put("variants", variantsArray);
            productJson.put("totalStock", totalStock);
            
            result.put("success", true);
            result.put("product", productJson);
            
            // If there are other similar matches, include them
            if (grouped.size() > 1) {
                JSONArray suggestions = new JSONArray();
                for (String name : grouped.keySet()) {
                    if (!name.equals(exactMatch)) {
                        suggestions.put(name);
                    }
                }
                result.put("suggestions", suggestions);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in getProductDetails: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get product stock by product name
     * Query trực tiếp từ bảng ProductStock để lấy amount chính xác
     */
    public JSONObject getProductStock(String productName) {
        JSONObject result = new JSONObject();
        EntityManager em = null;
        
        try {
            em = BaseDAO.emf.createEntityManager();
            
            // Query trực tiếp từ ProductStock table với JOIN Product và ProductVariant
            String jpql = "SELECT p.productId, p.name, pv.productVariantId, pv.size, " +
                         "COALESCE(ps.amount, 0) as stockAmount " +
                         "FROM Product p " +
                         "INNER JOIN ProductVariant pv ON p.productId = pv.product.productId " +
                         "LEFT JOIN ProductStock ps ON pv.productVariantId = ps.productVariant.productVariantId " +
                         "WHERE LOWER(p.name) LIKE LOWER(:productName) " +
                         "AND (p.isDeleted = false OR p.isDeleted IS NULL) " +
                         "AND (pv.isDeleted = false OR pv.isDeleted IS NULL) " +
                         "ORDER BY p.name, pv.size";
            
            Query query = em.createQuery(jpql);
            query.setParameter("productName", "%" + productName + "%");
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            if (results.isEmpty()) {
                result.put("success", false);
                result.put("error", "Không tìm thấy sản phẩm: " + productName);
                return result;
            }
            
            // Group by product name
            Map<String, List<Object[]>> groupedByProduct = new LinkedHashMap<>();
            String exactMatch = null;
            
            for (Object[] row : results) {
                String pName = (String) row[1];
                
                // Find exact match first
                if (exactMatch == null && pName.equalsIgnoreCase(productName)) {
                    exactMatch = pName;
                }
                
                if (!groupedByProduct.containsKey(pName)) {
                    groupedByProduct.put(pName, new ArrayList<>());
                }
                groupedByProduct.get(pName).add(row);
            }
            
            // Use exact match if found, otherwise use first match
            String selectedProduct = exactMatch != null ? exactMatch : groupedByProduct.keySet().iterator().next();
            List<Object[]> variants = groupedByProduct.get(selectedProduct);
            
            JSONArray stockArray = new JSONArray();
            int totalStock = 0;
            
            for (Object[] row : variants) {
                String size = (String) row[3];
                Integer stockAmount = ((Number) row[4]).intValue();
                
                JSONObject stockJson = new JSONObject();
                stockJson.put("productName", selectedProduct);
                stockJson.put("size", size != null ? size : "N/A");
                stockJson.put("stockAmount", stockAmount);
                stockJson.put("isInStock", stockAmount > 0);
                stockArray.put(stockJson);
                
                totalStock += stockAmount;
            }
            
            result.put("success", true);
            result.put("productName", selectedProduct);
            result.put("variants", stockArray);
            result.put("totalStock", totalStock);
            result.put("isInStock", totalStock > 0);
            
            System.out.println("✅ Query ProductStock trực tiếp - Sản phẩm: " + selectedProduct + 
                             ", Tổng tồn kho: " + totalStock);
            
        } catch (Exception e) {
            System.err.println("❌ Error querying ProductStock table: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Lỗi khi truy vấn tồn kho từ database: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        
        return result;
    }
    
    /**
     * Get products by category name
     */
    public JSONObject getProductsByCategory(String categoryName) {
        JSONObject result = new JSONObject();
        
        try {
            List<ProductDisplayDTO> allProducts = productService.getAllProductsWithPriceAndStock();
            
            List<ProductDisplayDTO> filtered = allProducts.stream()
                    .filter(p -> p.getCategoryName() != null && 
                            p.getCategoryName().equalsIgnoreCase(categoryName))
                    .collect(Collectors.toList());
            
            if (filtered.isEmpty()) {
                result.put("success", false);
                result.put("error", "Không tìm thấy sản phẩm trong danh mục: " + categoryName);
                return result;
            }
            
            // Group by product name
            Map<String, List<ProductDisplayDTO>> grouped = filtered.stream()
                    .collect(Collectors.groupingBy(ProductDisplayDTO::getProductName));
            
            JSONArray products = new JSONArray();
            for (Map.Entry<String, List<ProductDisplayDTO>> entry : grouped.entrySet()) {
                JSONObject productJson = new JSONObject();
                String productName = entry.getKey();
                List<ProductDisplayDTO> variants = entry.getValue();
                
                ProductDisplayDTO first = variants.get(0);
                productJson.put("productId", first.getProductId().toString());
                productJson.put("productName", productName);
                productJson.put("categoryName", first.getCategoryName());
                
                JSONArray variantsArray = new JSONArray();
                int totalStock = 0;
                
                for (ProductDisplayDTO variant : variants) {
                    JSONObject variantJson = new JSONObject();
                    variantJson.put("size", variant.getSize());
                    variantJson.put("price", variant.getPrice());
                    variantJson.put("stockAmount", variant.getStockAmount());
                    variantsArray.put(variantJson);
                    totalStock += variant.getStockAmount();
                }
                
                productJson.put("variants", variantsArray);
                productJson.put("totalStock", totalStock);
                products.put(productJson);
            }
            
            result.put("success", true);
            result.put("categoryName", categoryName);
            result.put("products", products);
            result.put("count", products.length());
            
        } catch (Exception e) {
            System.err.println("❌ Error in getProductsByCategory: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get product price by name and size
     */
    public JSONObject getProductPrice(String productName, String size) {
        JSONObject result = new JSONObject();
        
        try {
            List<ProductDisplayDTO> allProducts = productService.getAllProductsWithPriceAndStock();
            
            // Find exact match or fuzzy match
            List<ProductDisplayDTO> matches = allProducts.stream()
                    .filter(p -> p.getProductName() != null && 
                            p.getProductName().toLowerCase().contains(productName.toLowerCase()))
                    .filter(p -> size == null || size.isEmpty() || 
                            (p.getSize() != null && p.getSize().equalsIgnoreCase(size)))
                    .collect(Collectors.toList());
            
            if (matches.isEmpty()) {
                result.put("success", false);
                result.put("error", "Không tìm thấy sản phẩm: " + productName + 
                        (size != null && !size.isEmpty() ? " size " + size : ""));
                return result;
            }
            
            // If size specified, find exact variant
            if (size != null && !size.isEmpty()) {
                Optional<ProductDisplayDTO> exact = matches.stream()
                        .filter(p -> p.getSize() != null && p.getSize().equalsIgnoreCase(size))
                        .findFirst();
                
                if (exact.isPresent()) {
                    ProductDisplayDTO variant = exact.get();
                    result.put("success", true);
                    result.put("productName", variant.getProductName());
                    result.put("size", variant.getSize());
                    result.put("price", variant.getPrice());
                    result.put("stockAmount", variant.getStockAmount());
                } else {
                    // Return all variants if size not found
                    JSONArray prices = new JSONArray();
                    Map<String, List<ProductDisplayDTO>> grouped = matches.stream()
                            .collect(Collectors.groupingBy(ProductDisplayDTO::getProductName));
                    
                    String productNameMatch = grouped.keySet().iterator().next();
                    for (ProductDisplayDTO variant : grouped.get(productNameMatch)) {
                        JSONObject priceJson = new JSONObject();
                        priceJson.put("size", variant.getSize());
                        priceJson.put("price", variant.getPrice());
                        prices.put(priceJson);
                    }
                    
                    result.put("success", true);
                    result.put("productName", productNameMatch);
                    result.put("message", "Không tìm thấy size " + size + ", hiển thị tất cả các size:");
                    result.put("prices", prices);
                }
            } else {
                // No size specified - return all variants
                JSONArray prices = new JSONArray();
                Map<String, List<ProductDisplayDTO>> grouped = matches.stream()
                        .collect(Collectors.groupingBy(ProductDisplayDTO::getProductName));
                
                String productNameMatch = grouped.keySet().stream()
                        .filter(name -> name.equalsIgnoreCase(productName))
                        .findFirst()
                        .orElse(grouped.keySet().iterator().next());
                
                for (ProductDisplayDTO variant : grouped.get(productNameMatch)) {
                    JSONObject priceJson = new JSONObject();
                    priceJson.put("size", variant.getSize());
                    priceJson.put("price", variant.getPrice());
                    priceJson.put("stockAmount", variant.getStockAmount());
                    prices.put(priceJson);
                }
                
                result.put("success", true);
                result.put("productName", productNameMatch);
                result.put("prices", prices);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in getProductPrice: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Search products by keyword
     */
    public JSONObject searchProducts(String keyword) {
        JSONObject result = new JSONObject();
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "Từ khóa tìm kiếm không được để trống");
                return result;
            }
            
            List<ProductDisplayDTO> allProducts = productService.getAllProductsWithPriceAndStock();
            String lowerKeyword = keyword.toLowerCase().trim();
            
            // Search in product name, category, description
            List<ProductDisplayDTO> matches = allProducts.stream()
                    .filter(p -> {
                        boolean matchName = p.getProductName() != null && 
                                p.getProductName().toLowerCase().contains(lowerKeyword);
                        boolean matchCategory = p.getCategoryName() != null && 
                                p.getCategoryName().toLowerCase().contains(lowerKeyword);
                        boolean matchDesc = p.getDescription() != null && 
                                p.getDescription().toLowerCase().contains(lowerKeyword);
                        return matchName || matchCategory || matchDesc;
                    })
                    .collect(Collectors.toList());
            
            if (matches.isEmpty()) {
                result.put("success", false);
                result.put("error", "Không tìm thấy sản phẩm nào với từ khóa: " + keyword);
                result.put("keyword", keyword);
                return result;
            }
            
            // Group by product name
            Map<String, List<ProductDisplayDTO>> grouped = matches.stream()
                    .collect(Collectors.groupingBy(ProductDisplayDTO::getProductName));
            
            JSONArray products = new JSONArray();
            for (Map.Entry<String, List<ProductDisplayDTO>> entry : grouped.entrySet()) {
                JSONObject productJson = new JSONObject();
                String productName = entry.getKey();
                List<ProductDisplayDTO> variants = entry.getValue();
                
                ProductDisplayDTO first = variants.get(0);
                productJson.put("productId", first.getProductId().toString());
                productJson.put("productName", productName);
                productJson.put("categoryName", first.getCategoryName());
                productJson.put("description", first.getDescription());
                
                JSONArray variantsArray = new JSONArray();
                int totalStock = 0;
                double minPrice = Double.MAX_VALUE;
                double maxPrice = 0;
                
                for (ProductDisplayDTO variant : variants) {
                    JSONObject variantJson = new JSONObject();
                    variantJson.put("size", variant.getSize());
                    variantJson.put("price", variant.getPrice());
                    variantJson.put("stockAmount", variant.getStockAmount());
                    variantsArray.put(variantJson);
                    
                    totalStock += variant.getStockAmount();
                    if (variant.getPrice() != null) {
                        minPrice = Math.min(minPrice, variant.getPrice());
                        maxPrice = Math.max(maxPrice, variant.getPrice());
                    }
                }
                
                productJson.put("variants", variantsArray);
                productJson.put("totalStock", totalStock);
                productJson.put("minPrice", minPrice == Double.MAX_VALUE ? 0 : minPrice);
                productJson.put("maxPrice", maxPrice);
                
                products.put(productJson);
            }
            
            result.put("success", true);
            result.put("keyword", keyword);
            result.put("products", products);
            result.put("count", products.length());
            
        } catch (Exception e) {
            System.err.println("❌ Error in searchProducts: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get all products with low stock directly from ProductStock table
     * Query trực tiếp từ ProductStock để lấy amount chính xác
     * @param criticalThreshold Threshold for critical stock (default: 10)
     * @param warningThreshold Threshold for warning stock (default: 20)
     */
    public JSONObject getAllLowStockProducts(int criticalThreshold, int warningThreshold) {
        JSONObject result = new JSONObject();
        EntityManager em = null;
        
        try {
            em = BaseDAO.emf.createEntityManager();
            
            // Query từ ProductVariant với LEFT JOIN ProductStock để lấy tất cả variants, kể cả không có record trong ProductStock
            // Sử dụng SUM để xử lý trường hợp một variant có nhiều record trong ProductStock (multiple inventories)
            String jpql = "SELECT p.productId, p.name, pv.productVariantId, pv.size, " +
                         "COALESCE(SUM(ps.amount), 0) as stockAmount, pv.price, c.name as categoryName " +
                         "FROM ProductVariant pv " +
                         "INNER JOIN pv.product p ON p.productId = pv.product.productId " +
                         "LEFT JOIN pv.productStocks ps ON ps.productVariant.productVariantId = pv.productVariantId " +
                         "LEFT JOIN ProductCategory pc ON p.productId = pc.product.productId " +
                         "LEFT JOIN Category c ON pc.category.categoryId = c.categoryId " +
                         "WHERE (p.isDeleted = false OR p.isDeleted IS NULL) " +
                         "AND (pv.isDeleted = false OR pv.isDeleted IS NULL) " +
                         "GROUP BY p.productId, p.name, pv.productVariantId, pv.size, pv.price, c.name " +
                         "HAVING COALESCE(SUM(ps.amount), 0) <= :warningThreshold " +
                         "ORDER BY COALESCE(SUM(ps.amount), 0) ASC, p.name, pv.size";
            
            Query query = em.createQuery(jpql);
            query.setParameter("warningThreshold", warningThreshold);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            if (results.isEmpty()) {
                result.put("success", true);
                result.put("count", 0);
                result.put("criticalItems", new JSONArray());
                result.put("warningItems", new JSONArray());
                result.put("message", "Không có sản phẩm nào có tồn kho thấp (≤" + warningThreshold + ")");
                return result;
            }
            
            JSONArray criticalItems = new JSONArray();
            JSONArray warningItems = new JSONArray();
            
            // Group by product name
            Map<String, List<Object[]>> groupedByProduct = new LinkedHashMap<>();
            
            for (Object[] row : results) {
                String productName = (String) row[1];
                if (!groupedByProduct.containsKey(productName)) {
                    groupedByProduct.put(productName, new ArrayList<>());
                }
                groupedByProduct.get(productName).add(row);
            }
            
            // Process each product
            for (Map.Entry<String, List<Object[]>> entry : groupedByProduct.entrySet()) {
                String productName = entry.getKey();
                List<Object[]> variants = entry.getValue();
                
                for (Object[] row : variants) {
                    String size = (String) row[3];
                    Integer stockAmount = ((Number) row[4]).intValue();
                    Double price = row[5] != null ? ((java.math.BigDecimal) row[5]).doubleValue() : 0.0;
                    String categoryName = (String) row[6];
                    
                    JSONObject item = new JSONObject();
                    item.put("productName", productName);
                    item.put("size", size != null ? size : "N/A");
                    item.put("stockAmount", stockAmount);
                    item.put("price", price);
                    item.put("categoryName", categoryName != null ? categoryName : "");
                    item.put("isInStock", stockAmount > 0);
                    
                    // Classify by threshold
                    if (stockAmount == 0) {
                        item.put("level", "HẾT HÀNG");
                        item.put("urgency", "URGENT");
                        criticalItems.put(item);
                    } else if (stockAmount <= criticalThreshold) {
                        item.put("level", "NGUY HIỂM");
                        item.put("urgency", "URGENT");
                        criticalItems.put(item);
                    } else if (stockAmount <= warningThreshold) {
                        item.put("level", "CẢNH BÁO");
                        item.put("urgency", "HIGH");
                        warningItems.put(item);
                    }
                }
            }
            
            result.put("success", true);
            result.put("count", criticalItems.length() + warningItems.length());
            result.put("criticalCount", criticalItems.length());
            result.put("warningCount", warningItems.length());
            result.put("criticalItems", criticalItems);
            result.put("warningItems", warningItems);
            result.put("criticalThreshold", criticalThreshold);
            result.put("warningThreshold", warningThreshold);
            
            System.out.println("✅ Query ProductStock trực tiếp - Tìm thấy " + 
                             criticalItems.length() + " sản phẩm NGUY HIỂM, " + 
                             warningItems.length() + " sản phẩm CẢNH BÁO");
            
        } catch (Exception e) {
            System.err.println("❌ Error querying all low stock products from ProductStock: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Lỗi khi truy vấn tồn kho từ database: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        
        return result;
    }
    
}

