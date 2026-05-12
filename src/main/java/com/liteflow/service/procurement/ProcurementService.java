package com.liteflow.service.procurement;

import com.liteflow.dao.procurement.*;
import com.liteflow.model.procurement.*;
import com.liteflow.util.MailUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Dịch vụ nghiệp vụ Procurement:
 * - Quản lý nhà cung cấp & SLA
 * - Lập / duyệt / nhận hàng / đối chiếu hóa đơn
 */
public class ProcurementService {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final SupplierSLADAO slaDAO = new SupplierSLADAO();
    private final PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
    private final PurchaseOrderItemDAO itemDAO = new PurchaseOrderItemDAO();
    private final GoodsReceiptDAO grDAO = new GoodsReceiptDAO();
    @SuppressWarnings("unused")
    private final GoodsReceiptItemDAO grItemDAO = new GoodsReceiptItemDAO();
    private final InvoiceDAO invDAO = new InvoiceDAO();
    private final InvoiceItemDAO invItemDAO = new InvoiceItemDAO();
    private final InvoiceMatchingService matchingService = new InvoiceMatchingService();

    /* ============================================================
       1. QUẢN LÝ NHÀ CUNG CẤP & SLA
    ============================================================ */
    public UUID createSupplier(String name, UUID createdBy, String email) {
        Supplier s = new Supplier();
        s.setName(name);
        s.setCreatedBy(createdBy);
        s.setEmail(email);
        supplierDAO.insert(s);

        SupplierSLA sla = new SupplierSLA();
        sla.setSupplierID(s.getSupplierID());
        slaDAO.insert(sla);

        return s.getSupplierID();
    }

    public List<Supplier> getAllSuppliers() { return supplierDAO.getAll(); }
    
    public Supplier getSupplierById(UUID supplierID) {
        return supplierDAO.findById(supplierID);
    }
    
    public boolean updateSupplier(Supplier supplier) {
        return supplierDAO.update(supplier);
    }

    public boolean updateSupplierRating(UUID supplierID, double rating) {
        Supplier s = supplierDAO.findById(supplierID);
        if (s == null) return false;
        s.setRating(rating);
        return supplierDAO.update(s);
    }

    /* ============================================================
       2. LẬP ĐƠN ĐẶT HÀNG (PO)
    ============================================================ */
    public UUID createPurchaseOrder(UUID supplierID, UUID createdBy, LocalDateTime expectedDate, String notes, List<PurchaseOrderItem> items) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("=== ProcurementService.createPurchaseOrder START ===");
        System.out.println("  SupplierID: " + supplierID);
        System.out.println("  CreatedBy: " + createdBy);
        System.out.println("  ExpectedDate: " + expectedDate);
        System.out.println("  Notes: " + (notes != null && !notes.isEmpty() ? notes.substring(0, Math.min(50, notes.length())) : "null"));
        System.out.println("  Items count: " + (items != null ? items.size() : 0));
        
        // ========== VALIDATION ==========
        System.out.println("=== Starting validation ===");
        
        // Validate supplier exists and is active
        System.out.println("  [Validation] Checking supplier: " + supplierID);
        Supplier supplier = supplierDAO.findById(supplierID);
        if (supplier == null) {
            System.err.println("  [Validation] ❌ Supplier not found: " + supplierID);
            throw new IllegalArgumentException("Nhà cung cấp không tồn tại trong hệ thống");
        }
        System.out.println("  [Validation] ✅ Supplier found: " + supplier.getName());
        
        if (supplier.getIsActive() == null || !supplier.getIsActive()) {
            System.err.println("  [Validation] ❌ Supplier is inactive: " + supplier.getName());
            throw new IllegalArgumentException("Nhà cung cấp đã bị vô hiệu hóa. Vui lòng chọn nhà cung cấp khác");
        }
        System.out.println("  [Validation] ✅ Supplier is active");
        
        // Validate expected delivery date
        System.out.println("  [Validation] Checking expected delivery date: " + expectedDate);
        LocalDateTime now = LocalDateTime.now();
        System.out.println("  [Validation] Current time: " + now);
        
        if (expectedDate == null) {
            System.err.println("  [Validation] ❌ Expected date is null");
            throw new IllegalArgumentException("Ngày giao dự kiến không được để trống");
        }
        if (expectedDate.isBefore(now)) {
            System.err.println("  [Validation] ❌ Expected date is in the past");
            throw new IllegalArgumentException("Ngày giao dự kiến phải sau thời điểm hiện tại");
        }
        if (expectedDate.isBefore(now.plusHours(1))) {
            System.err.println("  [Validation] ❌ Expected date is less than 1 hour from now");
            throw new IllegalArgumentException("Ngày giao dự kiến phải cách thời điểm hiện tại ít nhất 1 giờ");
        }
        System.out.println("  [Validation] ✅ Expected date is valid");
        
        // Validate items
        System.out.println("  [Validation] Checking items...");
        if (items == null || items.isEmpty()) {
            System.err.println("  [Validation] ❌ Items list is null or empty");
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất 1 sản phẩm");
        }
        
        for (int i = 0; i < items.size(); i++) {
            PurchaseOrderItem item = items.get(i);
            System.out.println("  [Validation] Item " + (i + 1) + ": " + item.getItemName());
            
            // Validate item name
            if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
                System.err.println("    ❌ Item name is empty");
                throw new IllegalArgumentException("Tên sản phẩm thứ " + (i + 1) + " không được để trống");
            }
            
            // Validate quantity
            System.out.println("    Quantity: " + item.getQuantity());
            if (item.getQuantity() <= 0) {
                System.err.println("    ❌ Quantity is <= 0");
                throw new IllegalArgumentException("Số lượng sản phẩm \"" + item.getItemName() + "\" phải lớn hơn 0");
            }
            if (item.getQuantity() > 100000) {
                System.err.println("    ❌ Quantity exceeds limit: " + item.getQuantity());
                throw new IllegalArgumentException("Số lượng sản phẩm \"" + item.getItemName() + "\" không được vượt quá 100,000");
            }
            
            // Validate unit price
            System.out.println("    Unit Price: " + item.getUnitPrice());
            if (Double.isNaN(item.getUnitPrice()) || Double.isInfinite(item.getUnitPrice())) {
                System.err.println("    ❌ Unit price is NaN or Infinity");
                throw new IllegalArgumentException("Đơn giá sản phẩm \"" + item.getItemName() + "\" không hợp lệ (NaN hoặc Infinity)");
            }
            if (item.getUnitPrice() <= 0) {
                System.err.println("    ❌ Unit price is <= 0");
                throw new IllegalArgumentException("Đơn giá sản phẩm \"" + item.getItemName() + "\" phải lớn hơn 0");
            }
            if (item.getUnitPrice() > 1000000000) {
                System.err.println("    ❌ Unit price exceeds limit: " + item.getUnitPrice());
                throw new IllegalArgumentException("Đơn giá sản phẩm \"" + item.getItemName() + "\" không được vượt quá 1,000,000,000 VNĐ");
            }
            
            System.out.println("    ✅ Item " + (i + 1) + " is valid");
        }
        System.out.println("  [Validation] ✅ All items are valid");
        // ========== END VALIDATION ==========
        
        System.out.println("=== Creating PurchaseOrder entity ===");
        PurchaseOrder po = new PurchaseOrder();
        po.setSupplierID(supplierID);
        po.setCreatedBy(createdBy);
        po.setExpectedDelivery(expectedDate);
        po.setNotes(notes);
        po.setStatus("PENDING");
        System.out.println("  PO entity created (POID will be generated on persist)");
        
        // Insert PO and check result
        System.out.println("=== Inserting PurchaseOrder into database ===");
        boolean poInserted = poDAO.insert(po);
        if (!poInserted) {
            System.err.println("❌ FAILED to insert PurchaseOrder!");
            throw new RuntimeException("Không thể tạo đơn hàng. Vui lòng kiểm tra dữ liệu và thử lại.");
        }
        System.out.println("✅ PurchaseOrder inserted successfully. POID: " + po.getPoid());

        // Insert items and calculate total
        System.out.println("=== Calculating total amount ===");
        double total = 0.0;
        
        for (int i = 0; i < items.size(); i++) {
            PurchaseOrderItem it = items.get(i);
            it.setPoid(po.getPoid());
            
            // Calculate item total with validation
            int quantity = it.getQuantity();
            double unitPrice = it.getUnitPrice();
            
            System.out.println("  Item " + (i+1) + ": " + it.getItemName());
            System.out.println("    Quantity: " + quantity);
            System.out.println("    Unit Price: " + unitPrice);
            
            // Validate quantity and price before calculation
            if (quantity <= 0) {
                throw new IllegalStateException("Số lượng sản phẩm \"" + it.getItemName() + "\" phải lớn hơn 0");
            }
            if (Double.isNaN(unitPrice) || Double.isInfinite(unitPrice)) {
                throw new IllegalStateException("Đơn giá sản phẩm \"" + it.getItemName() + "\" không hợp lệ (NaN hoặc Infinity)");
            }
            if (unitPrice <= 0) {
                throw new IllegalStateException("Đơn giá sản phẩm \"" + it.getItemName() + "\" phải lớn hơn 0");
            }
            
            // Calculate item total
            double itemTotal = quantity * unitPrice;
            
            // Validate item total
            if (Double.isNaN(itemTotal) || Double.isInfinite(itemTotal)) {
                throw new IllegalStateException("Thành tiền sản phẩm \"" + it.getItemName() + "\" không hợp lệ (NaN hoặc Infinity)");
            }
            if (itemTotal < 0) {
                throw new IllegalStateException("Thành tiền sản phẩm \"" + it.getItemName() + "\" không được âm");
            }
            
            System.out.println("    Item Total: " + itemTotal);
            
            // Add to total (check for overflow)
            double previousTotal = total;
            total += itemTotal;
            
            // Check for overflow
            if (Double.isNaN(total) || Double.isInfinite(total)) {
                throw new IllegalStateException("Tổng tiền đơn hàng vượt quá giới hạn (NaN hoặc Infinity). Vui lòng kiểm tra lại số lượng và đơn giá.");
            }
            if (total < previousTotal) {
                throw new IllegalStateException("Tổng tiền đơn hàng bị overflow. Vui lòng giảm số lượng hoặc đơn giá.");
            }
            
            System.out.println("    Running Total: " + total);
            
            // Insert item
            boolean itemInserted = itemDAO.insert(it);
            if (!itemInserted) {
                System.err.println("❌ FAILED to insert PurchaseOrderItem #" + (i+1));
                throw new RuntimeException("Không thể thêm sản phẩm vào đơn hàng. Vui lòng thử lại.");
            }
            System.out.println("    ✅ Item inserted successfully");
        }
        
        // Final validation of total amount
        System.out.println("=== Final Total Amount: " + total + " ===");
        
        if (Double.isNaN(total) || Double.isInfinite(total)) {
            throw new IllegalStateException("Tổng tiền đơn hàng không hợp lệ (NaN hoặc Infinity). Vui lòng kiểm tra lại.");
        }
        if (total < 0) {
            throw new IllegalStateException("Tổng tiền đơn hàng không được âm.");
        }
        if (total > 1000000000000.0) { // 1 trillion VND
            throw new IllegalStateException("Tổng tiền đơn hàng quá lớn (vượt quá 1,000,000,000,000 VNĐ). Vui lòng kiểm tra lại.");
        }
        
        // Update total amount
        po.setTotalAmount(total);
        System.out.println("=== Updating PO with total amount: " + total + " ===");
        
        boolean poUpdated = poDAO.update(po);
        if (!poUpdated) {
            System.err.println("❌ FAILED to update PurchaseOrder total amount!");
            throw new RuntimeException("Không thể cập nhật tổng tiền. Vui lòng thử lại.");
        }
        
        System.out.println("✅ Total amount updated successfully");
        
        // Send Telegram notification for new PO (async) - notify all users with Telegram enabled
        try {
            POAlertService poAlertService = new POAlertService();
            poAlertService.sendPOCreationNotification(po.getPoid(), null); // null = notify all users with Telegram enabled
        } catch (Exception e) {
            // Don't fail PO creation if notification fails
            System.err.println("⚠️ Warning: PO notification check failed (PO creation still successful): " + e.getMessage());
            e.printStackTrace();
        }
        
        return po.getPoid();
    }

    public List<PurchaseOrder> getAllPOs() { return poDAO.getAll(); }
    
    public List<PurchaseOrderItem> getPOItems(UUID poid) {
        System.out.println("ProcurementService.getPOItems() called with POID: " + poid);
        List<PurchaseOrderItem> items = itemDAO.findByPOID(poid);
        System.out.println("DAO returned " + (items != null ? items.size() : "null") + " items");
        if (items != null && !items.isEmpty()) {
            items.forEach(item -> {
                System.out.println("  - Item: " + item.getItemName() + " (Qty: " + item.getQuantity() + ", Price: " + item.getUnitPrice() + ")");
            });
        }
        return items;
    }
    
    /**
     * Lấy danh sách sản phẩm từ lịch sử PO của một nhà cung cấp
     * @param supplierID ID của nhà cung cấp
     * @return List of Map với keys: itemName, latestPrice, orderCount, lastOrderDate
     */
    public List<Map<String, Object>> getSupplierProducts(UUID supplierID) {
        System.out.println("ProcurementService.getSupplierProducts() called with SupplierID: " + supplierID);
        
        if (supplierID == null) {
            System.err.println("SupplierID is null");
            return List.of();
        }
        
        // Lấy dữ liệu từ DAO (limit 30 sản phẩm)
        List<Object[]> results = itemDAO.getProductsBySupplier(supplierID, 30);
        
        // Format dữ liệu thành List<Map>
        List<Map<String, Object>> products = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> product = new HashMap<>();
            product.put("itemName", row[0]); // String
            product.put("latestPrice", row[1]); // Double
            product.put("orderCount", row[2]); // Long
            product.put("lastOrderDate", row[3]); // LocalDateTime
            
            products.add(product);
        }
        
        System.out.println("Returning " + products.size() + " products for supplier: " + supplierID);
        return products;
    }
    
    /**
     * Tính tổng số lượng đã nhận cho mỗi item trong PO từ tất cả các GoodsReceiptItems
     * @param poid PO ID
     * @return Map với key là itemName, value là tổng số lượng đã nhận (chỉ tính hàng OK)
     */
    public Map<String, Integer> getTotalReceivedQuantities(UUID poid) {
        Map<String, Integer> receivedMap = new HashMap<>();
        jakarta.persistence.EntityManager em = null;
        try {
            em = com.liteflow.dao.BaseDAO.emf.createEntityManager();
            
            // First, check if there are any GoodsReceipts for this PO
            jakarta.persistence.Query receiptsQuery = em.createQuery(
                "SELECT gr.receiptID FROM com.liteflow.model.procurement.GoodsReceipt gr WHERE gr.poid = :poid");
            receiptsQuery.setParameter("poid", poid);
            
            List<UUID> receiptIDs = receiptsQuery.getResultList();
            
            if (receiptIDs.isEmpty()) {
                return receivedMap;
            }
            
            // Query tổng số lượng đã nhận cho mỗi POItemID (chỉ tính hàng OK)
            String jpql = "SELECT gri.poItemID, SUM(gri.receivedQuantity) " +
                         "FROM com.liteflow.model.procurement.GoodsReceiptItem gri " +
                         "WHERE gri.receiptID IN :receiptIDs " +
                         "AND gri.qualityStatus = 'OK' " +
                         "GROUP BY gri.poItemID";
            
            jakarta.persistence.Query query = em.createQuery(jpql);
            query.setParameter("receiptIDs", receiptIDs);
            
            
            List<Object[]> results = query.getResultList();
            
            // Map POItemID -> tổng số lượng đã nhận
            Map<Integer, Integer> poItemReceivedMap = new HashMap<>();
            for (Object[] row : results) {
                Integer poItemID = (Integer) row[0];
                Long sumReceived = ((Number) row[1]).longValue();
                poItemReceivedMap.put(poItemID, sumReceived.intValue());
            }
            
            // Lấy danh sách POItems để map POItemID -> ItemName
            jakarta.persistence.Query poItemsQuery = em.createQuery(
                "SELECT poi FROM com.liteflow.model.procurement.PurchaseOrderItem poi WHERE poi.poid = :poid");
            poItemsQuery.setParameter("poid", poid);
            
            List<PurchaseOrderItem> poItems = poItemsQuery.getResultList();
            
            for (PurchaseOrderItem poi : poItems) {
                Integer totalReceived = poItemReceivedMap.getOrDefault(poi.getItemID(), 0);
                receivedMap.put(poi.getItemName(), totalReceived);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error calculating received quantities: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        
        return receivedMap;
    }
    
    public List<PurchaseOrder> getPOsPendingApproval() {
        return poDAO.getAll().stream()
                .filter(po -> "PENDING".equals(po.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    /* ============================================================
       3. DUYỆT PO (1 cấp hoặc nhiều cấp)
    ============================================================ */
    public boolean approvePO(UUID poid, UUID approver, int level) {
        PurchaseOrder po = poDAO.findById(poid);
        if (po == null || !"PENDING".equals(po.getStatus())) return false;
        po.setApprovalLevel(level);
        po.setApprovedBy(approver);
        po.setApprovedAt(LocalDateTime.now());
        po.setStatus("APPROVED");
        boolean updated = poDAO.update(po);
        
        // Send Telegram notification for status update (async)
        if (updated) {
            try {
                POAlertService poAlertService = new POAlertService();
                poAlertService.sendPOStatusUpdateNotification(poid, "APPROVED", approver);
            } catch (Exception e) {
                // Don't fail approval if notification fails
                System.err.println("⚠️ Warning: PO approval notification failed (approval still successful): " + e.getMessage());
                e.printStackTrace();
            }
            
            // Send email to supplier
            try {
                // Get supplier information
                Supplier supplier = getSupplierById(po.getSupplierID());
                if (supplier == null) {
                    System.err.println("⚠️ Warning: Supplier not found for PO " + poid + " - email not sent");
                } else if (supplier.getEmail() == null || supplier.getEmail().trim().isEmpty()) {
                    System.err.println("⚠️ Warning: Supplier '" + supplier.getName() + "' has no email address - email not sent");
                } else {
                    // Get purchase order items
                    List<PurchaseOrderItem> items = itemDAO.findByPOID(poid);
                    
                    // Format dates
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    String createDateStr = po.getCreateDate() != null ? 
                        po.getCreateDate().format(dateFormatter) : "N/A";
                    String expectedDeliveryStr = po.getExpectedDelivery() != null ? 
                        po.getExpectedDelivery().format(dateFormatter) : "N/A";
                    
                    // Format PO ID (UUID to string)
                    String poIdStr = po.getPoid().toString();
                    
                    // Get total amount
                    double totalAmount = po.getTotalAmount() != null ? po.getTotalAmount() : 0.0;
                    
                    // Get notes
                    String notes = po.getNotes();
                    
                    // Send email
                    MailUtil.sendPurchaseOrderEmail(
                        supplier.getEmail(),
                        supplier.getName(),
                        poIdStr,
                        createDateStr,
                        expectedDeliveryStr,
                        totalAmount,
                        notes,
                        items
                    );
                    
                    System.out.println("✅ Purchase order email sent to supplier: " + supplier.getEmail() + " for PO: " + poIdStr);
                }
            } catch (Exception e) {
                // Don't fail approval if email fails
                System.err.println("⚠️ Warning: Failed to send purchase order email (approval still successful): " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return updated;
    }

    public boolean rejectPO(UUID poid, UUID approver, String reason) {
        PurchaseOrder po = poDAO.findById(poid);
        if (po == null) return false;
        po.setApprovedBy(approver);
        po.setNotes(reason);
        po.setStatus("REJECTED");
        boolean updated = poDAO.update(po);
        
        // Send Telegram notification for status update (async)
        if (updated) {
            try {
                POAlertService poAlertService = new POAlertService();
                poAlertService.sendPOStatusUpdateNotification(poid, "REJECTED", approver);
            } catch (Exception e) {
                // Don't fail rejection if notification fails
                System.err.println("⚠️ Warning: PO rejection notification failed (rejection still successful): " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return updated;
    }

    /* ============================================================
       4. NHẬN HÀNG MỘT PHẦN (Partial Receiving)
    ============================================================ */
    public UUID receivePartial(UUID poid, UUID receivedBy, String note) {
        PurchaseOrder po = poDAO.findById(poid);
        if (po == null) return null;
        GoodsReceipt gr = new GoodsReceipt();
        gr.setPoid(poid);
        gr.setReceivedBy(receivedBy);
        gr.setNotes(note);
        gr.setStatus("PARTIAL");
        grDAO.insert(gr);
        po.setStatus("RECEIVING");
        poDAO.update(po);
        return gr.getReceiptID();
    }

    /**
     * Nhận hàng với chi tiết từng sản phẩm
     * @param poid PO ID
     * @param receivedBy User ID người nhận
     * @param items List of items với receivedQuantity và qualityStatus
     * @param notes Ghi chú
     * @return Receipt ID
     */
    public UUID receiveGoods(UUID poid, UUID receivedBy, List<Map<String, Object>> items, String notes) {
        jakarta.persistence.EntityManager em = null;
        try {
            em = com.liteflow.dao.BaseDAO.emf.createEntityManager();
            em.getTransaction().begin();
            
            // Get PO using the same EntityManager to keep it managed
            PurchaseOrder po = em.find(PurchaseOrder.class, poid);
            if (po == null) {
                throw new RuntimeException("Purchase Order not found: " + poid);
            }
            
            // Get PO items using query with same EM
            jakarta.persistence.Query poItemsQuery = em.createQuery(
                "SELECT poi FROM com.liteflow.model.procurement.PurchaseOrderItem poi WHERE poi.poid = :poid");
            poItemsQuery.setParameter("poid", poid);
            
            List<PurchaseOrderItem> poItems = poItemsQuery.getResultList();
            if (poItems.isEmpty()) {
                throw new RuntimeException("Purchase Order has no items");
            }
            
            // Initialize notes string for collecting over-receipt warnings
            StringBuilder receiptNotes = new StringBuilder();
            if (notes != null && !notes.trim().isEmpty()) {
                receiptNotes.append(notes);
            }
            
            // Create GoodsReceipt
            GoodsReceipt gr = new GoodsReceipt();
            gr.setPoid(poid);
            gr.setReceivedBy(receivedBy);
            gr.setNotes(notes); // Set initial notes, will update later if there are over-receipt warnings
            
            // CRITICAL: Persist GoodsReceipt FIRST to generate ReceiptID via @PrePersist
            em.persist(gr);
            em.flush(); // Force flush to get the generated ReceiptID
            UUID receiptID = gr.getReceiptID();
            
            // Determine status: FULL if all items received fully, otherwise PARTIAL
            boolean allFull = true;
            int totalReceived = 0;
            int totalOrdered = 0;
            
            // List to collect missing items for email notification
            List<Map<String, Object>> missingItems = new ArrayList<>();
            
            // Create GoodsReceiptItems and update inventory
            com.liteflow.dao.inventory.InventoryDAO inventoryDAO = new com.liteflow.dao.inventory.InventoryDAO();
            
            // Get default inventory (usually ID = 1 or first one)
            com.liteflow.model.inventory.Inventory defaultInventory = null;
            try {
                List<com.liteflow.model.inventory.Inventory> inventories = inventoryDAO.getAll();
                if (!inventories.isEmpty()) {
                    defaultInventory = inventories.get(0);
                    System.out.println("Using default inventory: " + defaultInventory.getInventoryId());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not get default inventory: " + e.getMessage());
            }
            
            // Get already received quantities if this is a continuing receive
            Map<String, Integer> alreadyReceivedMap = new HashMap<>();
            if ("RECEIVING".equals(po.getStatus())) {
                // Query total received quantities from database
                String receivedJpql = "SELECT gri.poItemID, SUM(gri.receivedQuantity) " +
                                     "FROM com.liteflow.model.procurement.GoodsReceiptItem gri " +
                                     "WHERE gri.receiptID IN " +
                                     "(SELECT gr.receiptID FROM com.liteflow.model.procurement.GoodsReceipt gr WHERE gr.poid = :poid) " +
                                     "AND gri.qualityStatus = 'OK' " +
                                     "GROUP BY gri.poItemID";
                jakarta.persistence.Query receivedQuery = em.createQuery(receivedJpql);
                receivedQuery.setParameter("poid", poid);
                
                List<Object[]> receivedResults = receivedQuery.getResultList();
                
                Map<Integer, Integer> poItemReceivedMap = new HashMap<>();
                for (Object[] row : receivedResults) {
                    Integer poItemID = (Integer) row[0];
                    Long sumReceived = ((Number) row[1]).longValue();
                    poItemReceivedMap.put(poItemID, sumReceived.intValue());
                }
                
                // Map POItemID -> ItemName
                for (PurchaseOrderItem poi : poItems) {
                    Integer alreadyReceivedQty = poItemReceivedMap.getOrDefault(poi.getItemID(), 0);
                    alreadyReceivedMap.put(poi.getItemName(), alreadyReceivedQty);
                }
            }
            
            for (Map<String, Object> itemData : items) {
                String itemName = (String) itemData.get("itemName");
                Integer orderedQty = ((Number) itemData.get("orderedQuantity")).intValue();
                Integer receivedQty = ((Number) itemData.get("receivedQuantity")).intValue();
                String qualityStatus = (String) itemData.getOrDefault("qualityStatus", "OK");
                
                // Calculate total received (already + new)
                Integer alreadyReceived = alreadyReceivedMap.getOrDefault(itemName, 0);
                Integer totalReceivedForItem = alreadyReceived + receivedQty;
                
                totalOrdered += orderedQty;
                totalReceived += totalReceivedForItem;
                
                if (totalReceivedForItem < orderedQty) {
                    allFull = false;
                    
                    // Add to missing items list for email notification
                    Map<String, Object> missingItem = new HashMap<>();
                    missingItem.put("itemName", itemName);
                    missingItem.put("orderedQty", orderedQty);
                    missingItem.put("receivedQty", totalReceivedForItem);
                    missingItem.put("missingQty", orderedQty - totalReceivedForItem);
                    missingItems.add(missingItem);
                }
                
                // Find matching POItem
                PurchaseOrderItem poItem = null;
                for (PurchaseOrderItem poi : poItems) {
                    if (poi.getItemName().equals(itemName)) {
                        poItem = poi;
                        break;
                    }
                }
                
                if (poItem == null) {
                    System.err.println("⚠️ Warning: POItem not found for: " + itemName);
                    continue;
                }
                
                // Check for over-receipt (received more than ordered)
                Integer overReceipt = receivedQty > orderedQty ? receivedQty - orderedQty : 0;
                if (overReceipt > 0) {
                    double overPercent = (overReceipt.doubleValue() / orderedQty.doubleValue()) * 100.0;
                    System.out.println("⚠️ OVER-RECEIPT WARNING: " + itemName + 
                                     " - Ordered: " + orderedQty + 
                                     ", Received: " + receivedQty + 
                                     ", Over: +" + overReceipt + " (" + String.format("%.1f", overPercent) + "%)");
                    
                    // Log to receipt notes if significant over-receipt (>10%)
                    if (overPercent > 10) {
                        if (receiptNotes.length() > 0) {
                            receiptNotes.append("\n");
                        }
                        receiptNotes.append("[OVER-RECEIPT] ").append(itemName)
                                   .append(": Đặt ").append(orderedQty)
                                   .append(", Nhận ").append(receivedQty)
                                   .append(" (+").append(overReceipt)
                                   .append(", ").append(String.format("%.1f", overPercent)).append("%)");
                    }
                }
                
                // Create GoodsReceiptItem with the generated ReceiptID
                GoodsReceiptItem gri = new GoodsReceiptItem();
                gri.setReceiptID(receiptID); // Use the generated ReceiptID from flush
                gri.setPoItemID(poItem.getItemID());
                gri.setProductName(itemName);
                gri.setOrderedQuantity(orderedQty);
                gri.setReceivedQuantity(receivedQty);
                gri.setUnitPrice(poItem.getUnitPrice());
                gri.setQualityStatus(qualityStatus);
                
                // Set discrepancy reason if over-receipt is significant (>10%)
                if (overReceipt > 0 && (overReceipt.doubleValue() / orderedQty.doubleValue()) > 0.1) {
                    String reason = "Nhận vượt số lượng đặt: +" + overReceipt + " đơn vị (" + 
                                   String.format("%.1f", (overReceipt.doubleValue() / orderedQty.doubleValue()) * 100.0) + "%)";
                    gri.setDiscrepancyReason(reason);
                    System.out.println("📝 Set discrepancy reason for " + itemName + ": " + reason);
                }
                
                // Calculate defective quantity if quality is not OK
                if (!"OK".equals(qualityStatus)) {
                    gri.setDefectiveQuantity(receivedQty);
                }
                
                // Insert GoodsReceiptItem using same EM
                em.persist(gri);
                
                // Update inventory if quality is OK and we have default inventory
                if ("OK".equals(qualityStatus) && defaultInventory != null && receivedQty > 0) {
                    try {
                        // Find product by name using EntityManager query
                        jakarta.persistence.Query productQuery = em.createQuery(
                            "SELECT p FROM com.liteflow.model.inventory.Product p WHERE LOWER(p.name) = LOWER(:name) AND (p.isDeleted = false OR p.isDeleted IS NULL)");
                        productQuery.setParameter("name", itemName);
                        
                        List<com.liteflow.model.inventory.Product> products = productQuery.getResultList();
                        
                        if (!products.isEmpty()) {
                            com.liteflow.model.inventory.Product product = products.get(0);
                            
                            // Get first variant using query
                            jakarta.persistence.Query variantQuery = em.createQuery(
                                "SELECT pv FROM com.liteflow.model.inventory.ProductVariant pv WHERE pv.product.productId = :productId AND (pv.isDeleted = false OR pv.isDeleted IS NULL)");
                            variantQuery.setParameter("productId", product.getProductId());
                            variantQuery.setMaxResults(1);
                            
                            List<com.liteflow.model.inventory.ProductVariant> variants = variantQuery.getResultList();
                            
                            if (!variants.isEmpty()) {
                                com.liteflow.model.inventory.ProductVariant variant = variants.get(0);
                                
                                // Find or create ProductStock using query
                                jakarta.persistence.Query stockQuery = em.createQuery(
                                    "SELECT ps FROM com.liteflow.model.inventory.ProductStock ps WHERE ps.productVariant.productVariantId = :variantId AND ps.inventory.inventoryId = :inventoryId");
                                stockQuery.setParameter("variantId", variant.getProductVariantId());
                                stockQuery.setParameter("inventoryId", defaultInventory.getInventoryId());
                                
                                List<com.liteflow.model.inventory.ProductStock> stocks = stockQuery.getResultList();
                                
                                com.liteflow.model.inventory.ProductStock stock;
                                if (stocks.isEmpty()) {
                                    // Create new ProductStock
                                    stock = new com.liteflow.model.inventory.ProductStock();
                                    stock.setProductVariant(variant);
                                    stock.setInventory(defaultInventory);
                                    stock.setAmount(receivedQty);
                                    em.persist(stock);
                                } else {
                                    // Update existing stock
                                    stock = stocks.get(0);
                                    int currentAmount = stock.getAmount() != null ? stock.getAmount() : 0;
                                    stock.setAmount(currentAmount + receivedQty);
                                    em.merge(stock);
                                }
                                
                                // Create InventoryLog
                                com.liteflow.model.inventory.InventoryLog log = new com.liteflow.model.inventory.InventoryLog();
                                log.setProductVariant(variant);
                                log.setActionType("Purchase Receipt");
                                log.setQuantityChanged(receivedQty);
                                log.setActionDate(java.time.LocalDateTime.now());
                                log.setStoreLocation(defaultInventory.getStoreLocation() != null ? defaultInventory.getStoreLocation() : "Main Warehouse");
                                em.persist(log);
                                
                            } else {
                                System.err.println("⚠️ Warning: No variants found for product: " + itemName);
                            }
                        } else {
                            System.err.println("⚠️ Warning: Product not found in inventory: " + itemName);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Warning: Failed to update inventory for " + itemName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // Set final notes including over-receipt warnings
            gr.setNotes(receiptNotes.length() > 0 ? receiptNotes.toString() : notes);
            
            // Update receipt status (already persisted, just update status)
            gr.setStatus(allFull ? "FULL" : "PARTIAL");
            em.merge(gr);
            
            // Update PO status using same EM (po is already managed)
            if (allFull && totalReceived >= totalOrdered) {
                po.setStatus("COMPLETED");
            } else {
                po.setStatus("RECEIVING");
            }
            em.merge(po);
            em.flush(); // Force flush to database
            
            em.getTransaction().commit();
            
            // Send email notification if there are missing items
            if (!missingItems.isEmpty()) {
                try {
                    // Get supplier information
                    Supplier supplier = getSupplierById(po.getSupplierID());
                    if (supplier == null) {
                        System.err.println("⚠️ Warning: Supplier not found for PO " + poid + " - missing goods email not sent");
                    } else if (supplier.getEmail() == null || supplier.getEmail().trim().isEmpty()) {
                        System.err.println("⚠️ Warning: Supplier '" + supplier.getName() + "' has no email address - missing goods email not sent");
                    } else {
                        // Format dates
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        String createDateStr = po.getCreateDate() != null ? 
                            po.getCreateDate().format(dateFormatter) : "N/A";
                        String expectedDeliveryStr = po.getExpectedDelivery() != null ? 
                            po.getExpectedDelivery().format(dateFormatter) : "N/A";
                        
                        // Format PO ID (UUID to string)
                        String poIdStr = po.getPoid().toString();
                        
                        // Send email
                        MailUtil.sendMissingGoodsEmail(
                            supplier.getEmail(),
                            supplier.getName(),
                            poIdStr,
                            createDateStr,
                            expectedDeliveryStr,
                            missingItems
                        );
                        
                        System.out.println("✅ Missing goods email sent to supplier: " + supplier.getEmail() + " for PO: " + poIdStr + " (" + missingItems.size() + " missing items)");
                    }
                } catch (Exception e) {
                    // Don't fail goods receipt if email fails
                    System.err.println("⚠️ Warning: Failed to send missing goods email (goods receipt still successful): " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return gr.getReceiptID();
            
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ receiveGoods FAILED: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to receive goods: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /* ============================================================
       4. GENERATE HÓA ĐƠN TỪ PO/GR (CHO IN)
    ============================================================ */
    
    /**
     * Generate invoice data from PO/GR for printing
     * @param poid Purchase Order ID
     * @return Map containing all invoice data
     * @throws RuntimeException if PO not found or not COMPLETED
     */
    public Map<String, Object> generateInvoiceFromPO(UUID poid) {
        // 1. Validate PO status = COMPLETED
        PurchaseOrder po = poDAO.findById(poid);
        if (po == null) {
            throw new RuntimeException("Đơn hàng không tồn tại");
        }
        if (!"COMPLETED".equals(po.getStatus())) {
            throw new RuntimeException("Chỉ có thể in hóa đơn cho đơn hàng đã hoàn thành. Đơn hàng hiện tại có trạng thái: " + po.getStatus());
        }
        
        // 2. Get Supplier information
        Supplier supplier = getSupplierById(po.getSupplierID());
        if (supplier == null) {
            System.err.println("⚠️ Warning: Supplier not found for PO " + poid);
        }
        
        // 3. Get POItems
        List<PurchaseOrderItem> poItems = itemDAO.findByPOID(poid);
        if (poItems == null || poItems.isEmpty()) {
            throw new RuntimeException("Đơn hàng không có sản phẩm");
        }
        
        // 4. Get GoodsReceiptItems (sum by POItemID, only OK quality)
        Map<Integer, Integer> receivedQuantities = new HashMap<>();
        jakarta.persistence.EntityManager em = null;
        try {
            em = com.liteflow.dao.BaseDAO.emf.createEntityManager();
            
            // Get all receipt IDs for this PO
            jakarta.persistence.Query receiptsQuery = em.createQuery(
                "SELECT gr.receiptID FROM com.liteflow.model.procurement.GoodsReceipt gr WHERE gr.poid = :poid");
            receiptsQuery.setParameter("poid", poid);
            List<UUID> receiptIDs = receiptsQuery.getResultList();
            
            if (!receiptIDs.isEmpty()) {
                // Sum received quantities by POItemID (only OK quality)
                String jpql = "SELECT gri.poItemID, SUM(gri.receivedQuantity) " +
                             "FROM com.liteflow.model.procurement.GoodsReceiptItem gri " +
                             "WHERE gri.receiptID IN :receiptIDs " +
                             "AND gri.qualityStatus = 'OK' " +
                             "GROUP BY gri.poItemID";
                
                jakarta.persistence.Query query = em.createQuery(jpql);
                query.setParameter("receiptIDs", receiptIDs);
                List<Object[]> results = query.getResultList();
                
                for (Object[] row : results) {
                    Integer poItemID = (Integer) row[0];
                    Long sumReceived = ((Number) row[1]).longValue();
                    receivedQuantities.put(poItemID, sumReceived.intValue());
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Error getting received quantities: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        
        // 5. Merge data: POItems + GRItems
        List<Map<String, Object>> invoiceItems = new ArrayList<>();
        double totalAmount = 0.0;
        
        for (PurchaseOrderItem poItem : poItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("itemName", poItem.getItemName());
            item.put("unitPrice", poItem.getUnitPrice());
            
            // Use received quantity if available, otherwise use ordered quantity
            Integer receivedQty = receivedQuantities.getOrDefault(poItem.getItemID(), poItem.getQuantity());
            item.put("quantity", receivedQty);
            item.put("orderedQuantity", poItem.getQuantity());
            
            // Calculate item total
            double itemTotal = receivedQty * poItem.getUnitPrice();
            item.put("total", itemTotal);
            totalAmount += itemTotal;
            
            invoiceItems.add(item);
        }
        
        // 6. Get latest GoodsReceipt date (for delivery date)
        LocalDateTime deliveryDate = po.getExpectedDelivery();
        try {
            em = com.liteflow.dao.BaseDAO.emf.createEntityManager();
            jakarta.persistence.Query latestReceiptQuery = em.createQuery(
                "SELECT MAX(gr.receiveDate) FROM com.liteflow.model.procurement.GoodsReceipt gr WHERE gr.poid = :poid");
            latestReceiptQuery.setParameter("poid", poid);
            Object latestDate = latestReceiptQuery.getSingleResult();
            if (latestDate != null && latestDate instanceof LocalDateTime) {
                deliveryDate = (LocalDateTime) latestDate;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Error getting latest receipt date: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        
        // 7. Get tax information
        String companyTaxCode = com.liteflow.util.EnvConfigUtil.getMaSoThue();
        String supplierTaxCode = supplier != null ? supplier.getTaxCode() : null;
        double vatRate = com.liteflow.util.EnvConfigUtil.getVATRate();
        
        // 8. Calculate tax amounts
        double subTotal = totalAmount; // Tổng tiền trước thuế
        double taxAmount = subTotal * vatRate / 100.0; // Tiền thuế
        double totalAmountWithTax = subTotal + taxAmount; // Tổng tiền sau thuế
        
        // 9. Build result map
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("po", po);
        invoiceData.put("supplier", supplier);
        invoiceData.put("items", invoiceItems);
        invoiceData.put("subTotal", subTotal);
        invoiceData.put("vatRate", vatRate);
        invoiceData.put("taxAmount", taxAmount);
        invoiceData.put("totalAmount", totalAmountWithTax); // Tổng tiền sau thuế
        invoiceData.put("companyTaxCode", companyTaxCode);
        invoiceData.put("supplierTaxCode", supplierTaxCode);
        invoiceData.put("deliveryDate", deliveryDate);
        invoiceData.put("printDate", LocalDateTime.now());
        
        return invoiceData;
    }

    /* ============================================================
       5. ĐỐI CHIẾU HÓA ĐƠN NHÀ CUNG CẤP (3-WAY MATCHING)
    ============================================================ */
    
    /**
     * DTO for Invoice Items
     */
    public static class InvoiceItemDTO {
        public String productName;
        public int quantity;
        public double unitPrice;
    }
    
    /**
     * Create invoice from PO with actual invoice items and perform 3-way matching
     * @param poid Purchase Order ID
     * @param supplierID Supplier ID
     * @param invoiceNumber Invoice number from supplier
     * @param invoiceDate Invoice date
     * @param invoiceItems Actual items from supplier invoice
     * @return Created invoice ID
     */
    public UUID matchInvoice(UUID poid, UUID supplierID, String invoiceNumber, 
                            LocalDateTime invoiceDate, List<InvoiceItemDTO> invoiceItems) {
        PurchaseOrder po = poDAO.findById(poid);
        if (po == null) {
            throw new RuntimeException("Purchase Order not found");
        }
        
        // Only allow invoice creation for COMPLETED purchase orders
        if (!"COMPLETED".equals(po.getStatus())) {
            throw new RuntimeException("Chỉ có thể tạo hóa đơn cho đơn hàng đã hoàn thành (COMPLETED). Đơn hàng hiện tại có trạng thái: " + po.getStatus());
        }
        
        // Calculate total from actual invoice items
        double totalAmount = 0;
        for (InvoiceItemDTO item : invoiceItems) {
            totalAmount += item.quantity * item.unitPrice;
        }
        
        // Create invoice with actual data
        Invoice inv = new Invoice();
        inv.setPoid(poid);
        inv.setSupplierID(supplierID);
        inv.setTotalAmount(totalAmount);
        inv.setInvoiceDate(invoiceDate != null ? invoiceDate : LocalDateTime.now());
        inv.setMatchStatus("PENDING");
        invDAO.insert(inv);
        
        // Get PO items to find matching poItemID
        List<PurchaseOrderItem> poItems = itemDAO.findByPOID(poid);
        Map<String, Integer> poItemMap = new HashMap<>();
        for (PurchaseOrderItem poItem : poItems) {
            poItemMap.put(poItem.getItemName().toLowerCase().trim(), poItem.getItemID());
        }
        
        // Create invoice items from ACTUAL invoice data (not copied from PO)
        for (InvoiceItemDTO itemDTO : invoiceItems) {
            InvoiceItem invItem = new InvoiceItem();
            invItem.setInvoiceID(inv.getInvoiceID());
            invItem.setProductName(itemDTO.productName);
            invItem.setQuantity(itemDTO.quantity);
            invItem.setUnitPrice(itemDTO.unitPrice);
            
            // Try to match with PO item by name
            String itemKey = itemDTO.productName.toLowerCase().trim();
            if (poItemMap.containsKey(itemKey)) {
                invItem.setPoItemID(poItemMap.get(itemKey));
            }
            
            invItemDAO.insert(invItem);
        }
        
        // Perform 3-way matching (now comparing actual invoice items vs PO items)
        InvoiceMatchingService.MatchingResult result = matchingService.performThreeWayMatch(inv.getInvoiceID());
        
        // Update PO status if matched
        if (result.matched) {
            po.setStatus("COMPLETED");
            poDAO.update(po);
        }
        
        return inv.getInvoiceID();
    }
    
    /**
     * Legacy method - kept for backward compatibility
     * @deprecated Use matchInvoice with items instead
     */
    @Deprecated
    public UUID matchInvoice(UUID poid, UUID supplierID, double invoiceAmount) {
        // Create dummy items from PO for backward compatibility
        List<PurchaseOrderItem> poItems = itemDAO.findByPOID(poid);
        List<InvoiceItemDTO> items = new ArrayList<>();
        for (PurchaseOrderItem poItem : poItems) {
            InvoiceItemDTO dto = new InvoiceItemDTO();
            dto.productName = poItem.getItemName();
            dto.quantity = poItem.getQuantity();
            dto.unitPrice = poItem.getUnitPrice();
            items.add(dto);
        }
        return matchInvoice(poid, supplierID, null, LocalDateTime.now(), items);
    }
    
    /**
     * Perform 3-way matching on existing invoice
     */
    public InvoiceMatchingService.MatchingResult performMatching(UUID invoiceID) {
        return matchingService.performThreeWayMatch(invoiceID);
    }
    
    /**
     * Auto-approve invoice if within tolerance
     */
    public boolean autoApproveInvoice(UUID invoiceID, UUID approvedBy) {
        return matchingService.autoApproveIfEligible(invoiceID, approvedBy);
    }

    /**
     * Create manual invoice (without PO)
     */
    public UUID createManualInvoice(Invoice invoice) {
        if (invoice.getInvoiceID() == null) {
            invoice.setInvoiceID(UUID.randomUUID());
        }
        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDateTime.now());
        }
        invDAO.insert(invoice);
        return invoice.getInvoiceID();
    }

    /* ============================================================
       6. CẬP NHẬT SLA (Đánh giá định kỳ)
    ============================================================ */
    public void evaluateSLA(UUID supplierID, boolean onTime, double delayDays) {
        SupplierSLA sla = slaDAO.getAll()
                .stream().filter(x -> x.getSupplierID().equals(supplierID))
                .findFirst().orElse(null);
        if (sla == null) return;
        sla.setTotalOrders(sla.getTotalOrders() + 1);
        if (onTime) sla.setOnTimeDeliveries(sla.getOnTimeDeliveries() + 1);
        sla.setAvgDelayDays((sla.getAvgDelayDays() + delayDays) / 2);
        sla.setLastEvaluated(LocalDateTime.now());
        slaDAO.update(sla);
    }

    /* ============================================================
       7. BUSINESS RULES & VALIDATION
    ============================================================ */
    
    /**
     * Kiểm tra quyền duyệt PO theo approval level
     */
    public boolean canApprovePO(UUID userID, UUID poid, int requestedLevel) {
        PurchaseOrder po = poDAO.findById(poid);
        if (po == null) return false;
        
        // Business rules:
        // Level 1: Owner/Manager (có thể duyệt tất cả)
        // Level 2: Department Head (có thể duyệt PO < 10M)
        // Level 3: Supervisor (có thể duyệt PO < 5M)
        
        double poAmount = po.getTotalAmount() != null ? po.getTotalAmount() : 0;
        
        switch (requestedLevel) {
            case 1: // Owner/Manager
                return true; // Có thể duyệt tất cả
            case 2: // Department Head
                return poAmount < 10_000_000; // < 10M VND
            case 3: // Supervisor
                return poAmount < 5_000_000; // < 5M VND
            default:
                return false;
        }
    }
    
    /**
     * Tính toán reorder point cho sản phẩm
     */
    public int calculateReorderPoint(String itemName, int avgDailyUsage, int leadTimeDays) {
        // Business rule: Reorder Point = (Average Daily Usage × Lead Time) + Safety Stock
        int safetyStock = (int) (avgDailyUsage * 0.2); // 20% safety stock
        return (avgDailyUsage * leadTimeDays) + safetyStock;
    }
    
    /**
     * Kiểm tra supplier có đủ điều kiện đặt hàng không
     */
    public boolean isSupplierEligible(UUID supplierID) {
        Supplier supplier = supplierDAO.findById(supplierID);
        if (supplier == null || !supplier.getIsActive()) return false;
        
        // Business rule: Rating >= 3.0 và OnTimeRate >= 80%
        return supplier.getRating() >= 3.0 && supplier.getOnTimeRate() >= 80.0;
    }
    
    /**
     * Tự động cập nhật inventory sau khi nhận hàng
     */
    public boolean updateInventoryAfterReceipt(UUID receiptID) {
        GoodsReceipt receipt = grDAO.findById(receiptID);
        if (receipt == null) return false;
        
        // This should update ProductStock table
        return true;
    }
    
    /**
     * Gửi cảnh báo khi PO sắp đến hạn
     */
    public List<PurchaseOrder> getPOsNearDeadline(int daysAhead) {
        LocalDateTime deadline = LocalDateTime.now().plusDays(daysAhead);
        return poDAO.getAll().stream()
                .filter(po -> po.getExpectedDelivery() != null && 
                             po.getExpectedDelivery().isBefore(deadline) &&
                             "APPROVED".equals(po.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Lấy danh sách PO đã trễ hạn
     */
    public List<PurchaseOrder> getOverduePOs() {
        LocalDateTime now = LocalDateTime.now();
        return poDAO.getAll().stream()
                .filter(po -> po.getExpectedDelivery() != null && 
                             po.getExpectedDelivery().isBefore(now) &&
                             ("APPROVED".equals(po.getStatus()) || "RECEIVING".equals(po.getStatus())))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Tính toán tổng giá trị đơn hàng với discount
     */
    public double calculatePOWithDiscount(double baseAmount, double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) return baseAmount;
        return baseAmount * (1 - discountPercent / 100);
    }
    
    /**
     * Kiểm tra budget còn lại cho supplier
     */
    public double getRemainingBudget(UUID supplierID, double monthlyBudget) {
        // This should query actual spending from completed POs
        return monthlyBudget; // Placeholder
    }
    
    /**
     * Tạo báo cáo hiệu suất supplier
     */
    public Map<String, Object> generateSupplierPerformanceReport(UUID supplierID) {
        Supplier supplier = supplierDAO.findById(supplierID);
        SupplierSLA sla = slaDAO.getAll().stream()
                .filter(s -> s.getSupplierID().equals(supplierID))
                .findFirst().orElse(null);
        
        Map<String, Object> report = new HashMap<>();
        if (supplier != null) {
            report.put("supplierName", supplier.getName());
            report.put("rating", supplier.getRating());
            report.put("onTimeRate", supplier.getOnTimeRate());
            report.put("defectRate", supplier.getDefectRate());
        }
        
        if (sla != null) {
            report.put("totalOrders", sla.getTotalOrders());
            report.put("onTimeDeliveries", sla.getOnTimeDeliveries());
            report.put("avgDelayDays", sla.getAvgDelayDays());
            report.put("onTimePercentage", sla.getTotalOrders() > 0 ? 
                (double) sla.getOnTimeDeliveries() / sla.getTotalOrders() * 100 : 0);
        }
        
        return report;
    }
    
    /* ============================================================
       8. INVOICE MANAGEMENT (Quản lý hóa đơn)
    ============================================================ */
    
    /**
     * Lấy tất cả invoices
     */
    public List<Invoice> getAllInvoices() {
        return invDAO.getAll();
    }
    
    /**
     * Lấy invoice theo ID
     */
    public Invoice getInvoiceById(UUID invoiceID) {
        return invDAO.findById(invoiceID);
    }
    
    /**
     * Lấy invoices theo PO
     */
    public List<Invoice> getInvoicesByPO(UUID poid) {
        return invDAO.getAll().stream()
                .filter(inv -> inv.getPoid() != null && inv.getPoid().equals(poid))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Lấy invoices theo supplier
     */
    public List<Invoice> getInvoicesBySupplier(UUID supplierID) {
        return invDAO.getAll().stream()
                .filter(inv -> inv.getSupplierID() != null && inv.getSupplierID().equals(supplierID))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Lấy invoices chưa khớp
     */
    public List<Invoice> getUnmatchedInvoices() {
        return invDAO.getAll().stream()
                .filter(inv -> inv.getMatched() != null && !inv.getMatched())
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Resolve invoice discrepancy
     */
    public boolean resolveInvoiceDiscrepancy(UUID invoiceID, String note) {
        Invoice invoice = invDAO.findById(invoiceID);
        if (invoice == null) return false;
        
        invoice.setMatched(true);
        invoice.setMatchNote(note);
        return invDAO.update(invoice);
    }
    
    /**
     * Update invoice
     */
    public boolean updateInvoice(Invoice invoice) {
        return invDAO.update(invoice);
    }
    
    /**
     * Delete invoice
     */
    public boolean deleteInvoice(UUID invoiceID) {
        return invDAO.delete(invoiceID);
    }
    
    /**
     * Get total invoice amount by supplier
     */
    public double getTotalInvoiceAmountBySupplier(UUID supplierID) {
        return invDAO.getAll().stream()
                .filter(inv -> inv.getSupplierID() != null && inv.getSupplierID().equals(supplierID))
                .mapToDouble(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : 0.0)
                .sum();
    }
    
    /**
     * Get invoices by date range
     */
    public List<Invoice> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return invDAO.getAll().stream()
                .filter(inv -> inv.getInvoiceDate() != null && 
                              !inv.getInvoiceDate().isBefore(startDate) && 
                              !inv.getInvoiceDate().isAfter(endDate))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /* ============================================================
       9. DASHBOARD STATISTICS & ACTIVITIES
    ============================================================ */
    
    /**
     * Activity DTO for dashboard
     */
    public static class ActivityDTO {
        private String type;
        private String description;
        private LocalDateTime timestamp;
        private String entityId;
        private String formattedTime; // Formatted time string for display
        
        public ActivityDTO(String type, String description, LocalDateTime timestamp, String entityId) {
            this.type = type;
            this.description = description;
            this.timestamp = timestamp;
            this.entityId = entityId;
            // Format timestamp for display
            if (timestamp != null) {
                java.time.format.DateTimeFormatter formatter = 
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                this.formattedTime = timestamp.format(formatter);
            } else {
                this.formattedTime = "Không xác định";
            }
        }
        
        public String getType() { return type; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getEntityId() { return entityId; }
        public String getFormattedTime() { return formattedTime; }
    }
    
    /**
     * Get dashboard statistics
     * @return Map with statistics keys
     */
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total suppliers (active only)
        long totalSuppliers = supplierDAO.getAll().stream()
                .filter(s -> s.getIsActive() != null && s.getIsActive())
                .count();
        stats.put("totalSuppliers", (int) totalSuppliers);
        
        // Pending POs
        int pendingPOs = poDAO.countPending();
        stats.put("pendingPOs", pendingPOs);
        
        // In delivery POs (APPROVED or RECEIVING)
        jakarta.persistence.EntityManager em = com.liteflow.dao.BaseDAO.emf.createEntityManager();
        try {
            String jpql = "SELECT COUNT(po) FROM PurchaseOrder po " +
                         "WHERE po.status IN ('APPROVED', 'RECEIVING')";
            Long count = em.createQuery(jpql, Long.class).getSingleResult();
            stats.put("inDeliveryPOs", count != null ? count.intValue() : 0);
        } finally {
            em.close();
        }
        
        // Overdue POs
        List<PurchaseOrder> overduePOs = getOverduePOs();
        stats.put("overduePOs", overduePOs.size());
        
        // Near deadline POs (within 3 days)
        List<PurchaseOrder> nearDeadlinePOs = getPOsNearDeadline(3);
        stats.put("nearDeadlinePOs", nearDeadlinePOs.size());
        
        // Unmatched invoices
        List<Invoice> unmatchedInvoices = getUnmatchedInvoices();
        stats.put("unmatchedInvoices", unmatchedInvoices.size());
        
        return stats;
    }
    
    /**
     * Get recent activities for dashboard
     * @param limit Maximum number of activities to return
     * @return List of ActivityDTO sorted by timestamp (newest first)
     */
    public List<ActivityDTO> getRecentActivities(int limit) {
        List<ActivityDTO> activities = new ArrayList<>();
        
        // Get recent POs
        List<PurchaseOrder> recentPOs = poDAO.getAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreateDate() == null && b.getCreateDate() == null) return 0;
                    if (a.getCreateDate() == null) return 1;
                    if (b.getCreateDate() == null) return -1;
                    return b.getCreateDate().compareTo(a.getCreateDate());
                })
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
        
        for (PurchaseOrder po : recentPOs) {
            String status = po.getStatus();
            String description;
            String type;
            
            if ("PENDING".equals(status)) {
                type = "PO_CREATED";
                description = "Đơn hàng " + formatPOID(po.getPoid()) + " đã được tạo";
            } else if ("APPROVED".equals(status)) {
                type = "PO_APPROVED";
                description = "Đơn hàng " + formatPOID(po.getPoid()) + " đã được duyệt";
            } else if ("REJECTED".equals(status)) {
                type = "PO_REJECTED";
                description = "Đơn hàng " + formatPOID(po.getPoid()) + " đã bị từ chối";
            } else {
                type = "PO_UPDATED";
                description = "Đơn hàng " + formatPOID(po.getPoid()) + " đã được cập nhật";
            }
            
            activities.add(new ActivityDTO(type, description, 
                    po.getCreateDate() != null ? po.getCreateDate() : LocalDateTime.now(),
                    po.getPoid().toString()));
        }
        
        // Get recent Goods Receipts
        List<GoodsReceipt> recentGRs = grDAO.getAll().stream()
                .sorted((a, b) -> {
                    if (a.getReceiveDate() == null && b.getReceiveDate() == null) return 0;
                    if (a.getReceiveDate() == null) return 1;
                    if (b.getReceiveDate() == null) return -1;
                    return b.getReceiveDate().compareTo(a.getReceiveDate());
                })
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
        
        for (GoodsReceipt gr : recentGRs) {
            String status = gr.getStatus();
            String description = "Đã nhận hàng từ đơn hàng " + formatPOID(gr.getPoid()) + 
                               ("FULL".equals(status) ? " (đầy đủ)" : " (một phần)");
            activities.add(new ActivityDTO("GR_RECEIVED", description,
                    gr.getReceiveDate() != null ? gr.getReceiveDate() : LocalDateTime.now(),
                    gr.getReceiptID().toString()));
        }
        
        // Get recent Invoices
        List<Invoice> recentInvoices = invDAO.getAll().stream()
                .sorted((a, b) -> {
                    if (a.getInvoiceDate() == null && b.getInvoiceDate() == null) return 0;
                    if (a.getInvoiceDate() == null) return 1;
                    if (b.getInvoiceDate() == null) return -1;
                    return b.getInvoiceDate().compareTo(a.getInvoiceDate());
                })
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
        
        for (Invoice inv : recentInvoices) {
            String matchStatus = inv.getMatchStatus();
            String description;
            if ("MATCHED".equals(matchStatus)) {
                description = "Hóa đơn " + formatInvoiceID(inv.getInvoiceID()) + " đã được đối chiếu thành công";
            } else if ("MISMATCHED".equals(matchStatus)) {
                description = "Hóa đơn " + formatInvoiceID(inv.getInvoiceID()) + " có sự không khớp";
            } else {
                description = "Hóa đơn " + formatInvoiceID(inv.getInvoiceID()) + " chờ đối chiếu";
            }
            activities.add(new ActivityDTO("INVOICE_MATCHED", description,
                    inv.getInvoiceDate() != null ? inv.getInvoiceDate() : LocalDateTime.now(),
                    inv.getInvoiceID().toString()));
        }
        
        // Get recent Suppliers
        List<Supplier> recentSuppliers = supplierDAO.getAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
        
        for (Supplier supplier : recentSuppliers) {
            activities.add(new ActivityDTO("SUPPLIER_ADDED", 
                    "Thêm nhà cung cấp mới: " + supplier.getName(),
                    supplier.getCreatedAt() != null ? supplier.getCreatedAt() : LocalDateTime.now(),
                    supplier.getSupplierID().toString()));
        }
        
        // Sort all activities by timestamp (newest first) and limit
        return activities.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Format PO ID for display (last 8 characters)
     */
    private String formatPOID(UUID poid) {
        if (poid == null) return "N/A";
        String str = poid.toString().replace("-", "");
        return "PO-" + str.substring(Math.max(0, str.length() - 8));
    }
    
    /**
     * Format Invoice ID for display (last 8 characters)
     */
    private String formatInvoiceID(UUID invoiceID) {
        if (invoiceID == null) return "N/A";
        String str = invoiceID.toString().replace("-", "");
        return "INV-" + str.substring(Math.max(0, str.length() - 8));
    }
}
