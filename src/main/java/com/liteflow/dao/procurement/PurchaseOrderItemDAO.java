package com.liteflow.dao.procurement;

import com.liteflow.model.procurement.PurchaseOrderItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PurchaseOrderItemDAO extends GenericDAO<PurchaseOrderItem, Integer> {
    public PurchaseOrderItemDAO() { 
        super(PurchaseOrderItem.class); 
    }
    
    /**
     * Lấy tất cả items của một Purchase Order
     */
    public List<PurchaseOrderItem> findByPOID(UUID poid) {
        System.out.println("PurchaseOrderItemDAO.findByPOID() called with POID: " + poid);
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            System.out.println("EntityManager created, executing query...");
            
            TypedQuery<PurchaseOrderItem> query = em.createQuery(
                "SELECT p FROM PurchaseOrderItem p WHERE p.poid = :poid ORDER BY p.itemID",
                PurchaseOrderItem.class
            );
            query.setParameter("poid", poid);
            
            List<PurchaseOrderItem> results = query.getResultList();
            System.out.println("Query executed, found " + results.size() + " items");
            
            return results;
        } catch (Exception e) {
            System.err.println("ERROR in PurchaseOrderItemDAO.findByPOID(): " + e.getMessage());
            e.printStackTrace();
            return List.of();
        } finally {
            if (em != null) {
                em.close();
                System.out.println("EntityManager closed");
            }
        }
    }
    
    /**
     * Kiểm tra xem có PO item nào gần đây cho productName và size cụ thể không
     * @param productName Tên sản phẩm
     * @param size Size của sản phẩm (có thể là "N/A" hoặc empty)
     * @param days Số ngày gần đây để check (ví dụ: 1 ngày)
     * @return true nếu có PO item trong khoảng thời gian gần đây với status hợp lệ
     */
    public boolean hasRecentItemByProductNameAndSize(String productName, String size, int days) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            // Tạo itemName format giống với format trong createPOForSupplier
            String itemName;
            if (size != null && !size.trim().isEmpty() && !size.equals("N/A")) {
                itemName = productName + " (Size: " + size + ")";
            } else {
                itemName = productName;
            }
            
            // Tính ngày bắt đầu (days ngày trước)
            LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
            
            System.out.println("🔍 Checking recent PO for item: " + itemName + " (since: " + sinceDate + ")");
            
            // Query: Lấy list PO gần đây với status hợp lệ trước
            String poJpql = "SELECT po.poid FROM PurchaseOrder po " +
                           "WHERE po.status IN ('PENDING', 'APPROVED', 'RECEIVING', 'COMPLETED') " +
                           "AND po.createDate >= :sinceDate";
            
            TypedQuery<UUID> poQuery = em.createQuery(poJpql, UUID.class);
            poQuery.setParameter("sinceDate", sinceDate);
            List<UUID> recentPOIds = poQuery.getResultList();
            
            System.out.println("📋 Found " + recentPOIds.size() + " recent PO(s) with valid status");
            
            if (recentPOIds.isEmpty()) {
                return false;
            }
            
            // Query: Check xem có PurchaseOrderItem nào với itemName này trong các PO gần đây không
            String itemJpql = "SELECT COUNT(poi) FROM PurchaseOrderItem poi " +
                             "WHERE poi.itemName = :itemName " +
                             "AND poi.poid IN :poIds";
            
            TypedQuery<Long> itemQuery = em.createQuery(itemJpql, Long.class);
            itemQuery.setParameter("itemName", itemName);
            itemQuery.setParameter("poIds", recentPOIds);
            
            Long count = itemQuery.getSingleResult();
            
            boolean hasRecent = count != null && count > 0;
            
            if (hasRecent) {
                System.out.println("✅ Found recent PO item for: " + itemName + " (created within " + days + " day(s))");
            } else {
                System.out.println("ℹ️ No recent PO item found for: " + itemName);
            }
            
            return hasRecent;
            
        } catch (Exception e) {
            System.err.println("❌ Error checking recent PO item for " + productName + " (Size: " + size + "): " + e.getMessage());
            e.printStackTrace();
            // Return false on error để không block việc tạo PO
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    /**
     * Lấy danh sách sản phẩm từ lịch sử PO của một nhà cung cấp
     * @param supplierID ID của nhà cung cấp
     * @param limit Số lượng sản phẩm tối đa (top 20-30)
     * @return List of Object[] với format: [ItemName, LatestPrice, OrderCount, LastOrderDate]
     */
    public List<Object[]> getProductsBySupplier(UUID supplierID, int limit) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            // Query lấy sản phẩm từ lịch sử PO của supplier
            // Group by ItemName, lấy giá gần nhất và số lần đặt
            String jpql = 
                "SELECT poi.itemName, " +
                "       MAX(poi.unitPrice) as latestPrice, " +
                "       COUNT(DISTINCT po.poid) as orderCount, " +
                "       MAX(po.createDate) as lastOrderDate " +
                "FROM PurchaseOrderItem poi " +
                "INNER JOIN PurchaseOrder po ON poi.poid = po.poid " +
                "WHERE po.supplierID = :supplierID " +
                "  AND po.status IN ('APPROVED', 'COMPLETED', 'RECEIVING') " +
                "GROUP BY poi.itemName " +
                "ORDER BY lastOrderDate DESC, orderCount DESC";
            
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("supplierID", supplierID);
            query.setMaxResults(limit);
            
            List<Object[]> results = query.getResultList();
            System.out.println("Found " + results.size() + " products for supplier: " + supplierID);
            
            return results;
        } catch (Exception e) {
            System.err.println("ERROR in PurchaseOrderItemDAO.getProductsBySupplier(): " + e.getMessage());
            e.printStackTrace();
            return List.of();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
