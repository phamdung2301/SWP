package com.liteflow.service.procurement;

import com.liteflow.dao.procurement.*;
import com.liteflow.model.procurement.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * InvoiceMatchingService - 3-Way Matching Logic
 * CRITICAL: Đối chiếu PO ↔ GR ↔ Invoice
 */
public class InvoiceMatchingService {

    @SuppressWarnings("unused")
    private final PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
    private final PurchaseOrderItemDAO poItemDAO = new PurchaseOrderItemDAO();
    private final GoodsReceiptDAO grDAO = new GoodsReceiptDAO();
    private final GoodsReceiptItemDAO grItemDAO = new GoodsReceiptItemDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final InvoiceItemDAO invoiceItemDAO = new InvoiceItemDAO();

    /**
     * Tolerance configuration
     */
    private static final double DEFAULT_PRICE_TOLERANCE_PERCENT = 2.0;     // 2%
    private static final double DEFAULT_QUANTITY_TOLERANCE_PERCENT = 3.0;  // 3%
    private static final double DEFAULT_AMOUNT_TOLERANCE = 50000;          // 50,000đ

    /**
     * 3-Way Matching Result
     */
    public static class MatchingResult {
        public boolean matched;
        public String status;           // MATCHED | MISMATCHED | PARTIAL_MATCH | TOLERANCE_MATCH
        public String message;
        public double totalDiscrepancy;
        public List<ItemDiscrepancy> itemDiscrepancies = new ArrayList<>();
        public boolean requiresApproval;
        public int approvalLevel;

        public static class ItemDiscrepancy {
            public Integer itemID;
            public String productName;
            public String discrepancyType;  // PRICE | QUANTITY | MISSING | EXTRA
            public double expected;
            public double actual;
            public double variance;
            public String note;
        }
    }

    /**
     * MAIN: Perform 3-Way Matching
     * So sánh PO Items ↔ GR Items ↔ Invoice Items
     */
    public MatchingResult performThreeWayMatch(UUID invoiceID) {
        return performThreeWayMatch(invoiceID, DEFAULT_PRICE_TOLERANCE_PERCENT, DEFAULT_QUANTITY_TOLERANCE_PERCENT);
    }

    public MatchingResult performThreeWayMatch(UUID invoiceID, double priceTolerance, double quantityTolerance) {
        MatchingResult result = new MatchingResult();
        
        try {
            // 1. Load Invoice
            Invoice invoice = invoiceDAO.findById(invoiceID);
            if (invoice == null) {
                result.matched = false;
                result.status = "MISMATCHED";
                result.message = "Invoice not found";
                return result;
            }

            // 2. Load Invoice Items
            List<InvoiceItem> invoiceItems = invoiceItemDAO.findByInvoiceID(invoiceID);
            if (invoiceItems.isEmpty()) {
                result.matched = false;
                result.status = "MISMATCHED";
                result.message = "No invoice items found";
                return result;
            }

            // 3. Load PO Items (if linked to PO)
            Map<Integer, PurchaseOrderItem> poItemsMap = new HashMap<>();
            if (invoice.getPoid() != null) {
                List<PurchaseOrderItem> poItems = poItemDAO.findByPOID(invoice.getPoid());
                for (PurchaseOrderItem item : poItems) {
                    poItemsMap.put(item.getItemID(), item);
                }
            }

            // 4. Load GR Items (if linked to PO)
            Map<Integer, GoodsReceiptItem> grItemsMap = new HashMap<>();
            if (invoice.getPoid() != null) {
                List<GoodsReceipt> receipts = grDAO.getAll().stream()
                    .filter(gr -> invoice.getPoid().equals(gr.getPoid()))
                    .toList();
                
                for (GoodsReceipt receipt : receipts) {
                    List<GoodsReceiptItem> grItems = grItemDAO.findByReceiptID(receipt.getReceiptID());
                    for (GoodsReceiptItem item : grItems) {
                        if (item.getPoItemID() != null) {
                            grItemsMap.put(item.getPoItemID(), item);
                        }
                    }
                }
            }

            // 5. Match each Invoice Item
            double totalDiscrepancyAmount = 0.0;
            int matchedCount = 0;
            int mismatchedCount = 0;

            for (InvoiceItem invItem : invoiceItems) {
                // Get corresponding PO Item
                PurchaseOrderItem poItem = invItem.getPoItemID() != null ? 
                    poItemsMap.get(invItem.getPoItemID()) : null;
                
                // Get corresponding GR Item
                GoodsReceiptItem grItem = invItem.getPoItemID() != null ? 
                    grItemsMap.get(invItem.getPoItemID()) : null;

                // Perform item-level matching
                ItemMatchResult itemResult = matchItem(invItem, poItem, grItem, priceTolerance, quantityTolerance);
                
                // Update invoice item
                invItem.setMatched(itemResult.matched);
                invItem.setDiscrepancyAmount(itemResult.discrepancyAmount);
                invItem.setDiscrepancyQuantity(itemResult.discrepancyQuantity);
                invItem.setMatchNote(itemResult.note);
                invoiceItemDAO.update(invItem);

                // Collect results
                if (itemResult.matched) {
                    matchedCount++;
                } else {
                    mismatchedCount++;
                }
                
                totalDiscrepancyAmount += Math.abs(itemResult.discrepancyAmount);
                result.itemDiscrepancies.addAll(itemResult.discrepancies);
            }

            // 6. Determine overall matching status
            result.totalDiscrepancy = totalDiscrepancyAmount;
            
            if (mismatchedCount == 0) {
                result.matched = true;
                result.status = "MATCHED";
                result.message = "All items matched successfully";
            } else if (matchedCount == 0) {
                result.matched = false;
                result.status = "MISMATCHED";
                result.message = mismatchedCount + " items mismatched";
            } else {
                result.matched = false;
                result.status = "PARTIAL_MATCH";
                result.message = matchedCount + " matched, " + mismatchedCount + " mismatched";
            }

            // 7. Check if tolerance is acceptable
            if (!result.matched && totalDiscrepancyAmount <= DEFAULT_AMOUNT_TOLERANCE) {
                result.matched = true;
                result.status = "TOLERANCE_MATCH";
                result.message = "Matched within tolerance (₫" + String.format("%.0f", totalDiscrepancyAmount) + ")";
                result.requiresApproval = false;
            } else if (!result.matched) {
                result.requiresApproval = true;
                result.approvalLevel = getApprovalLevel(totalDiscrepancyAmount);
            }

            // 8. Update Invoice
            invoice.setMatched(result.matched);
            invoice.setMatchStatus(result.status);
            invoice.setMatchNote(result.message);
            invoice.setMatchedAt(LocalDateTime.now());
            invoiceDAO.update(invoice);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.matched = false;
            result.status = "ERROR";
            result.message = "Error during matching: " + e.getMessage();
            return result;
        }
    }

    /**
     * Item-level matching result
     */
    private static class ItemMatchResult {
        boolean matched;
        double discrepancyAmount;
        int discrepancyQuantity;
        String note;
        List<MatchingResult.ItemDiscrepancy> discrepancies = new ArrayList<>();
    }

    /**
     * Match a single item: Invoice Item vs PO Item vs GR Item
     */
    private ItemMatchResult matchItem(
        InvoiceItem invItem,
        PurchaseOrderItem poItem,
        GoodsReceiptItem grItem,
        double priceTolerance,
        double quantityTolerance
    ) {
        ItemMatchResult result = new ItemMatchResult();
        result.matched = true;  // Assume matched initially
        result.discrepancyAmount = 0.0;
        result.discrepancyQuantity = 0;
        StringBuilder noteBuilder = new StringBuilder();

        // If no PO item (manual invoice), auto-match
        if (poItem == null) {
            result.note = "Manual invoice - no PO to compare";
            return result;
        }

        // 1. Check QUANTITY (compare with GR if available, else with PO)
        int expectedQuantity = (grItem != null && grItem.getReceivedQuantity() != null) ? 
            grItem.getReceivedQuantity() : 
            (poItem != null ? poItem.getQuantity() : 0);
        
        int actualQuantity = invItem.getQuantity() != null ? invItem.getQuantity() : 0;
        int calcDiscrepancy = actualQuantity - expectedQuantity;
        result.discrepancyQuantity = calcDiscrepancy;

        if (calcDiscrepancy != 0) {
            double quantityVariancePercent = expectedQuantity > 0 ? 
                (Math.abs(result.discrepancyQuantity) * 100.0 / expectedQuantity) : 0;
            
            if (quantityVariancePercent > quantityTolerance) {
                result.matched = false;
                noteBuilder.append(String.format("Quantity mismatch: expected %d, got %d (%.1f%%); ",
                    expectedQuantity, actualQuantity, quantityVariancePercent));
                
                MatchingResult.ItemDiscrepancy disc = new MatchingResult.ItemDiscrepancy();
                disc.productName = invItem.getProductName();
                disc.discrepancyType = "QUANTITY";
                disc.expected = expectedQuantity;
                disc.actual = actualQuantity;
                disc.variance = result.discrepancyQuantity;
                result.discrepancies.add(disc);
            }
        }

        // 2. Check PRICE (compare with PO)
        double expectedPrice = poItem != null ? poItem.getUnitPrice() : 0.0;
        double actualPrice = invItem.getUnitPrice() != null ? invItem.getUnitPrice() : 0.0;
        double calcPriceDisc = actualPrice - expectedPrice;

        if (Math.abs(calcPriceDisc) > 0.01) {
            double priceVariancePercent = expectedPrice > 0 ? 
                (Math.abs(calcPriceDisc) * 100.0 / expectedPrice) : 0;
            
            if (priceVariancePercent > priceTolerance) {
                result.matched = false;
                noteBuilder.append(String.format("Price mismatch: expected ₫%.0f, got ₫%.0f (%.1f%%); ",
                    expectedPrice, actualPrice, priceVariancePercent));
                
                MatchingResult.ItemDiscrepancy disc = new MatchingResult.ItemDiscrepancy();
                disc.productName = invItem.getProductName();
                disc.discrepancyType = "PRICE";
                disc.expected = expectedPrice;
                disc.actual = actualPrice;
                disc.variance = calcPriceDisc;
                result.discrepancies.add(disc);
            }
        }

        // 3. Calculate AMOUNT discrepancy
        double expectedAmount = expectedQuantity * expectedPrice;
        double actualAmount = actualQuantity * actualPrice;
        result.discrepancyAmount = actualAmount - expectedAmount;

        // 4. Set note
        if (result.matched) {
            result.note = "Matched";
        } else {
            result.note = noteBuilder.toString();
        }

        return result;
    }

    /**
     * Get approval level based on discrepancy amount
     */
    private int getApprovalLevel(double discrepancyAmount) {
        if (discrepancyAmount < 100000) {
            return 1;  // Level 1: < 100k
        } else if (discrepancyAmount < 500000) {
            return 2;  // Level 2: 100k-500k
        } else if (discrepancyAmount < 1000000) {
            return 3;  // Level 3: 500k-1M
        } else {
            return 4;  // Level 4: > 1M
        }
    }

    /**
     * 2-Way Matching: PO ↔ Invoice (no GR required)
     * Dùng cho services hoặc các mặt hàng không cần kiểm tra thực nhận
     */
    public MatchingResult performTwoWayMatch(UUID invoiceID) {
        // Similar to 3-way but skip GR comparison
        return performThreeWayMatch(invoiceID); // Same logic, will handle null GR
    }

    /**
     * Auto-approve invoice if within tolerance
     */
    public boolean autoApproveIfEligible(UUID invoiceID, UUID approvedBy) {
        MatchingResult result = performThreeWayMatch(invoiceID);
        
        if (result.matched && !result.requiresApproval) {
            Invoice invoice = invoiceDAO.findById(invoiceID);
            if (invoice != null) {
                invoice.setMatched(true);
                invoice.setMatchStatus("MATCHED");
                invoice.setMatchedBy(approvedBy);
                invoice.setMatchedAt(LocalDateTime.now());
                invoiceDAO.update(invoice);
                return true;
            }
        }
        
        return false;
    }
}

