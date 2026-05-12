package com.liteflow.service.procurement;

import com.liteflow.dao.procurement.PurchaseOrderItemDAO;
import com.liteflow.model.procurement.PurchaseOrderItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service để tự động tạo PO từ low stock items
 */
public class POAutoCreationService {
    
    /**
     * Class để chứa kết quả tạo PO
     */
    public static class POCreationResult {
        private Map<UUID, UUID> createdPOs;
        private List<String> skippedItems; // itemName của các items đã skip
        
        public POCreationResult() {
            this.createdPOs = new HashMap<>();
            this.skippedItems = new ArrayList<>();
        }
        
        public Map<UUID, UUID> getCreatedPOs() {
            return createdPOs;
        }
        
        public List<String> getSkippedItems() {
            return skippedItems;
        }
        
        public void addCreatedPO(UUID supplierId, UUID poId) {
            this.createdPOs.put(supplierId, poId);
        }
        
        public void addSkippedItem(String itemName) {
            this.skippedItems.add(itemName);
        }
    }
    
    private final ProcurementService procurementService;
    private final SupplierMappingService supplierMappingService;
    private final PurchaseOrderItemDAO purchaseOrderItemDAO;
    private final com.liteflow.service.ai.AIAgentConfigService configService;
    
    // Default values (fallback if config not found)
    private static final int DEFAULT_LEAD_TIME_DAYS = 7;
    private static final int DEFAULT_REORDER_QUANTITY = 20;
    private static final int DEFAULT_RECENT_PO_CHECK_DAYS = 1;
    
    public POAutoCreationService() {
        this.procurementService = new ProcurementService();
        this.supplierMappingService = new SupplierMappingService();
        this.purchaseOrderItemDAO = new PurchaseOrderItemDAO();
        this.configService = new com.liteflow.service.ai.AIAgentConfigService();
    }
    
    /**
     * Get recent PO check days from config or default
     */
    private int getRecentPOCheckDays() {
        return configService.getIntConfig("po.recent_check_days", DEFAULT_RECENT_PO_CHECK_DAYS);
    }
    
    /**
     * Get default lead time days from config or default
     */
    private int getDefaultLeadTimeDays() {
        return configService.getIntConfig("po.default_lead_time_days", DEFAULT_LEAD_TIME_DAYS);
    }
    
    /**
     * Get default reorder quantity from config or default
     */
    private int getDefaultReorderQuantity() {
        return configService.getIntConfig("po.default_reorder_quantity", DEFAULT_REORDER_QUANTITY);
    }
    
    /**
     * Check if auto create PO is enabled
     */
    private boolean isAutoCreatePOEnabled() {
        return configService.getBooleanConfig("po.auto_create_enabled", true);
    }
    
    /**
     * Tạo PO tự động từ low stock items
     * Logic: Group tất cả items cùng category (cùng supplier) vào một PO
     * @param lowStockItems JSONArray chứa các item low stock (từ getAllLowStockProducts)
     * @param createdBy User ID người tạo
     * @return POCreationResult chứa createdPOs và skippedItems
     */
    public POCreationResult createPOsFromLowStockItems(JSONArray lowStockItems, UUID createdBy) {
        System.out.println("=== POAutoCreationService.createPOsFromLowStockItems START ===");
        System.out.println("📦 Low stock items count: " + lowStockItems.length());
        System.out.println("👤 CreatedBy: " + createdBy);
        
        POCreationResult result = new POCreationResult();
        
        if (lowStockItems == null || lowStockItems.length() == 0) {
            System.out.println("⚠️ No low stock items to process");
            return result;
        }
        
        // Group items by category -> supplier
        // Key: Supplier ID, Value: List of items cùng category (cùng supplier)
        Map<UUID, List<JSONObject>> supplierItemsMap = new HashMap<>();
        Map<String, Integer> categoryCountMap = new HashMap<>(); // Track số lượng items theo category
        int skippedCount = 0;
        
        // First pass: Group items by supplier (through category mapping)
        for (int i = 0; i < lowStockItems.length(); i++) {
            try {
                JSONObject item = lowStockItems.getJSONObject(i);
                String productName = item.optString("productName", "");
                String size = item.optString("size", "");
                String categoryName = item.optString("categoryName", "");
                
                // Log item details
                System.out.println("📋 Processing item [" + (i+1) + "/" + lowStockItems.length() + "]: " + 
                                 productName + " (Size: " + size + "), Category: " + categoryName);
                
                // Map product -> supplier (ưu tiên product mapping, fallback về category mapping)
                UUID supplierId = supplierMappingService.getSupplierIdForProduct(productName.trim());
                if (supplierId == null) {
                    // Fallback: try category mapping if product mapping not found
                    if (categoryName != null && !categoryName.trim().isEmpty()) {
                        supplierId = supplierMappingService.getSupplierIdForCategory(categoryName.trim());
                    }
                    
                    if (supplierId == null) {
                        System.out.println("  ⚠️ No supplier mapping for product '" + productName + 
                                         (categoryName != null ? "' (category: " + categoryName + ")" : "") + 
                                         ", skipping item");
                        skippedCount++;
                        continue;
                    }
                }
                
                // Track category count for logging
                categoryCountMap.put(categoryName, categoryCountMap.getOrDefault(categoryName, 0) + 1);
                
                // Group by supplier - Tất cả items cùng supplier sẽ vào cùng một list
                if (!supplierItemsMap.containsKey(supplierId)) {
                    supplierItemsMap.put(supplierId, new ArrayList<>());
                    System.out.println("  ✅ New supplier group created: " + supplierId + " (Category: " + categoryName + ")");
                }
                
                supplierItemsMap.get(supplierId).add(item);
                System.out.println("  ✅ Item added to supplier group: " + supplierId + " (Total items in group: " + supplierItemsMap.get(supplierId).size() + ")");
                
            } catch (Exception e) {
                System.err.println("  ❌ Error processing item at index " + i + ": " + e.getMessage());
                e.printStackTrace();
                skippedCount++;
            }
        }
        
        // Log summary
        System.out.println("\n📊 GROUPING SUMMARY:");
        System.out.println("  - Total items processed: " + lowStockItems.length());
        System.out.println("  - Items skipped: " + skippedCount);
        System.out.println("  - Items grouped: " + (lowStockItems.length() - skippedCount));
        System.out.println("  - Number of supplier groups: " + supplierItemsMap.size());
        
        for (Map.Entry<UUID, List<JSONObject>> entry : supplierItemsMap.entrySet()) {
            UUID supplierId = entry.getKey();
            List<JSONObject> items = entry.getValue();
            System.out.println("  - Supplier " + supplierId + ": " + items.size() + " items");
        }
        
        if (supplierItemsMap.isEmpty()) {
            System.out.println("⚠️ No items could be mapped to suppliers");
            return result;
        }
        
        // Create PO for each supplier (mỗi supplier = 1 PO chứa tất cả items cùng category)
        LocalDateTime expectedDelivery = LocalDateTime.now().plusDays(getDefaultLeadTimeDays());
        
        System.out.println("\n🚀 CREATING PURCHASE ORDERS:");
        for (Map.Entry<UUID, List<JSONObject>> entry : supplierItemsMap.entrySet()) {
            UUID supplierId = entry.getKey();
            List<JSONObject> items = entry.getValue();
            
            System.out.println("\n📋 Processing supplier " + supplierId + " with " + items.size() + " items:");
            for (JSONObject item : items) {
                String productName = item.optString("productName", "");
                String size = item.optString("size", "");
                System.out.println("  - " + productName + " (Size: " + size + ")");
            }
            
            try {
                UUID poId = createPOForSupplier(supplierId, items, createdBy, expectedDelivery, result);
                result.addCreatedPO(supplierId, poId);
                System.out.println("✅ SUCCESS: Created PO " + poId + " for supplier " + supplierId + " with " + items.size() + " items");
            } catch (Exception e) {
                System.err.println("❌ FAILED: Could not create PO for supplier " + supplierId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("\n=== POAutoCreationService.createPOsFromLowStockItems END ===");
        System.out.println("✅ Created " + result.getCreatedPOs().size() + " PO(s) successfully");
        if (!result.getSkippedItems().isEmpty()) {
            System.out.println("⚠️ Skipped " + result.getSkippedItems().size() + " item(s) due to recent PO");
        }
        return result;
    }
    
    /**
     * Tạo PO cho một supplier cụ thể
     * Tất cả items cùng category (cùng supplier) sẽ được thêm vào một PO duy nhất
     */
    private UUID createPOForSupplier(UUID supplierId, List<JSONObject> items, UUID createdBy, LocalDateTime expectedDelivery, POCreationResult result) {
        System.out.println("  📋 Creating PO for supplier: " + supplierId);
        System.out.println("  📦 Total items to add to PO: " + items.size());
        
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot create PO: items list is empty");
        }
        
        // Use Set to track unique items (productName + size) để tránh duplicate
        Set<String> processedItems = new HashSet<>();
        List<PurchaseOrderItem> poItems = new ArrayList<>();
        StringBuilder notes = new StringBuilder("Tự động tạo bởi AI từ low stock items. Bao gồm: ");
        
        int itemIndex = 0;
        for (JSONObject item : items) {
            try {
                itemIndex++;
                String productName = item.optString("productName", "");
                String size = item.optString("size", "");
                int currentStock = item.optInt("stockAmount", 0);
                double unitPrice = item.optDouble("price", 0.0);
                
                // Create unique key để check duplicate
                String uniqueKey = productName + "|" + (size != null ? size : "N/A");
                
                // Check for duplicate (cùng productName + size)
                if (processedItems.contains(uniqueKey)) {
                    System.out.println("  ⚠️ Duplicate item detected: " + productName + " (Size: " + size + ") - skipping");
                    continue;
                }
                processedItems.add(uniqueKey);
                
                // Tạo item name với size (nếu có)
                String itemName = productName;
                if (size != null && !size.trim().isEmpty() && !size.equals("N/A")) {
                    itemName = productName + " (Size: " + size + ")";
                }
                
                // Check xem có PO gần đây cho item này không (trong vòng 1 ngày)
                int recentCheckDays = getRecentPOCheckDays();
                boolean hasRecentPO = purchaseOrderItemDAO.hasRecentItemByProductNameAndSize(productName, size, recentCheckDays);
                if (hasRecentPO) {
                    System.out.println("  ⚠️ Recent PO found for: " + itemName + " (created within " + recentCheckDays + " day(s)) - skipping");
                    result.addSkippedItem(itemName);
                    continue;
                }
                
                // Tính số lượng đặt hàng
                // Logic: Đưa về mức default reorder quantity, tối thiểu 15 đơn vị
                int reorderQuantity = Math.max(getDefaultReorderQuantity() - currentStock, 15);
                
                PurchaseOrderItem poItem = new PurchaseOrderItem();
                poItem.setItemName(itemName);
                poItem.setQuantity(reorderQuantity);
                poItem.setUnitPrice(unitPrice > 0 ? unitPrice : getDefaultPrice(productName)); // Fallback nếu không có giá
                
                poItems.add(poItem);
                
                // Append to notes
                if (itemIndex > 1) {
                    notes.append(", ");
                }
                notes.append(String.format("%s x%d", itemName, reorderQuantity));
                
                System.out.println("    ✅ [" + itemIndex + "/" + items.size() + "] Added: " + itemName + 
                                 " | Stock: " + currentStock + " → Order Qty: " + reorderQuantity + 
                                 " | Price: " + poItem.getUnitPrice());
                
            } catch (Exception e) {
                System.err.println("    ❌ Error processing item [" + itemIndex + "]: " + e.getMessage());
                e.printStackTrace();
                // Continue với item tiếp theo thay vì fail toàn bộ PO
            }
        }
        
        if (poItems.isEmpty()) {
            throw new IllegalStateException("Cannot create PO: No valid items after processing");
        }
        
        notes.append(". Ngày giao dự kiến: ").append(expectedDelivery.toLocalDate());
        
        System.out.println("  ✅ Total PO items created: " + poItems.size() + " (from " + items.size() + " input items)");
        
        // Tạo PO với tất cả items
        UUID poId = procurementService.createPurchaseOrder(
            supplierId,
            createdBy,
            expectedDelivery,
            notes.toString(),
            poItems
        );
        
        System.out.println("  ✅ PO created successfully: " + poId);
        return poId;
    }
    
    /**
     * Lấy giá mặc định nếu không có giá từ item
     * Có thể query từ Product hoặc PO history sau
     */
    private double getDefaultPrice(String productName) {
        // Default price: 50000 VND
        // Có thể cải thiện bằng cách query từ ProductVariant.price hoặc PO history
        return 50000.0;
    }
}

