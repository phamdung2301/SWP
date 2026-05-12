package com.liteflow.controller.procurement;

import com.liteflow.model.procurement.PurchaseOrder;
import com.liteflow.model.procurement.PurchaseOrderItem;
import com.liteflow.service.procurement.ProcurementService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

// @WebServlet(urlPatterns = {"/procurement/po"}) // Disabled - using web.xml mapping
public class PurchaseOrderServlet extends HttpServlet {
    private final ProcurementService service = new ProcurementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, jakarta.servlet.ServletException {
        String action = req.getParameter("action");
        String requestURI = req.getRequestURI();
        String pathInfo = req.getPathInfo();
        
        // Handle API request for supplier products
        // Check both requestURI and pathInfo for flexibility
        if ((requestURI != null && requestURI.contains("/api/products")) || 
            (pathInfo != null && pathInfo.contains("/api/products")) ||
            "products".equals(action)) {
            handleGetSupplierProducts(req, resp);
            return;
        }
        
        // Handle AJAX request for PO details
        if ("details".equals(action)) {
            handleGetDetails(req, resp);
            return;
        }
        
        // CRITICAL: Set response headers to prevent chunked encoding issues
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Transfer-Encoding", "identity");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");
        
        try {
            System.out.println("=== PurchaseOrderServlet.doGet START ===");
            
            List<PurchaseOrder> purchaseOrders = service.getAllPOs();
            List<com.liteflow.model.procurement.Supplier> suppliers = service.getAllSuppliers();
            
            System.out.println("DEBUG: Loaded " + purchaseOrders.size() + " purchase orders");
            System.out.println("DEBUG: Loaded " + suppliers.size() + " suppliers");
            
            req.setAttribute("purchaseOrders", purchaseOrders);
            req.setAttribute("suppliers", suppliers);
            
            System.out.println("DEBUG: Forwarding to po.jsp");
            req.getRequestDispatcher("/procurement/po.jsp").forward(req, resp);
            System.out.println("=== PurchaseOrderServlet.doGet END ===");
            
        } catch (Exception e) {
            System.err.println("ERROR in PurchaseOrderServlet.doGet: " + e.getMessage());
            e.printStackTrace();
            
            // Send error response directly to avoid JSP issues
            resp.setContentType("text/html; charset=UTF-8");
            resp.getWriter().write(
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>PO Error</title></head>\n" +
                "<body>\n" +
                "    <h1>Lỗi tải trang đơn đặt hàng</h1>\n" +
                "    <p><strong>Lỗi:</strong> " + e.getMessage() + "</p>\n" +
                "    <pre>" + e.toString() + "</pre>\n" +
                "    <p><a href=\"/LiteFlow/dashboard\">Quay về Dashboard</a></p>\n" +
                "</body>\n" +
                "</html>"
            );
        }
    }
    
    private void handleGetDetails(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        try {
            String poidStr = req.getParameter("poid");
            System.out.println("handleGetDetails - POID: " + poidStr);
            UUID poid = UUID.fromString(poidStr);
            
            PurchaseOrder po = service.getAllPOs().stream()
                .filter(p -> p.getPoid().equals(poid))
                .findFirst()
                .orElse(null);
            
            if (po == null) {
                System.err.println("PO not found: " + poid);
                resp.getWriter().write("{\"error\": \"PO not found\"}");
                return;
            }
            
            // Get supplier name
            com.liteflow.model.procurement.Supplier supplier = service.getSupplierById(po.getSupplierID());
            String supplierName = supplier != null ? supplier.getName() : "N/A";
            
            List<PurchaseOrderItem> items = service.getPOItems(poid);
            System.out.println("Found " + items.size() + " items for PO");
            
            // Build JSON manually
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"poid\":\"").append(po.getPoid()).append("\",");
            json.append("\"supplierID\":\"").append(po.getSupplierID()).append("\",");
            json.append("\"supplierName\":\"").append(supplierName.replace("\"", "\\\"")).append("\",");
            json.append("\"createDate\":\"").append(po.getCreateDate() != null ? po.getCreateDate() : "").append("\",");
            json.append("\"expectedDelivery\":\"").append(po.getExpectedDelivery() != null ? po.getExpectedDelivery() : "").append("\",");
            json.append("\"totalAmount\":").append(po.getTotalAmount() != null ? po.getTotalAmount() : 0).append(",");
            json.append("\"status\":\"").append(po.getStatus() != null ? po.getStatus() : "PENDING").append("\",");
            json.append("\"notes\":\"").append(po.getNotes() != null ? po.getNotes().replace("\"", "\\\"") : "").append("\",");
            
            // Get total received quantities (for both RECEIVING and APPROVED status)
            // This allows us to show received quantities even if status changes
            Map<String, Integer> receivedQuantities = service.getTotalReceivedQuantities(poid);
            
            json.append("\"receivedQuantities\":{");
            boolean firstReceived = true;
            for (Map.Entry<String, Integer> entry : receivedQuantities.entrySet()) {
                if (!firstReceived) json.append(",");
                json.append("\"").append(entry.getKey().replace("\"", "\\\"")).append("\":").append(entry.getValue());
                firstReceived = false;
            }
            json.append("},");
            
            json.append("\"items\":[");
            
            for (int i = 0; i < items.size(); i++) {
                PurchaseOrderItem item = items.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"itemName\":\"").append(item.getItemName().replace("\"", "\\\"")).append("\",");
                json.append("\"quantity\":").append(item.getQuantity()).append(",");
                json.append("\"unitPrice\":").append(item.getUnitPrice()).append(",");
                json.append("\"total\":").append(item.getQuantity() * item.getUnitPrice());
                json.append("}");
            }
            
            json.append("]}");
            
            System.out.println("Sending JSON response: " + json.toString().substring(0, Math.min(200, json.length())) + "...");
            resp.getWriter().write(json.toString());
            
        } catch (Exception e) {
            System.err.println("ERROR in handleGetDetails: " + e.getMessage());
            e.printStackTrace();
            resp.getWriter().write("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
    
    private void handleGetSupplierProducts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        try {
            String supplierIDStr = req.getParameter("supplierID");
            System.out.println("handleGetSupplierProducts - SupplierID: " + supplierIDStr);
            
            if (supplierIDStr == null || supplierIDStr.trim().isEmpty()) {
                resp.getWriter().write("{\"error\": \"supplierID parameter is required\"}");
                return;
            }
            
            UUID supplierID = UUID.fromString(supplierIDStr);
            List<Map<String, Object>> products = service.getSupplierProducts(supplierID);
            
            // Build JSON response
            StringBuilder json = new StringBuilder();
            json.append("[");
            
            for (int i = 0; i < products.size(); i++) {
                Map<String, Object> product = products.get(i);
                if (i > 0) json.append(",");
                
                json.append("{");
                json.append("\"itemName\":\"").append(escapeJson(product.get("itemName").toString())).append("\",");
                json.append("\"latestPrice\":").append(product.get("latestPrice")).append(",");
                json.append("\"orderCount\":").append(product.get("orderCount")).append(",");
                
                // Format LocalDateTime to ISO string
                Object lastOrderDate = product.get("lastOrderDate");
                if (lastOrderDate != null) {
                    json.append("\"lastOrderDate\":\"").append(lastOrderDate.toString()).append("\"");
                } else {
                    json.append("\"lastOrderDate\":null");
                }
                
                json.append("}");
            }
            
            json.append("]");
            
            System.out.println("Sending " + products.size() + " products for supplier: " + supplierID);
            resp.getWriter().write(json.toString());
            
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format: " + e.getMessage());
            resp.getWriter().write("{\"error\": \"Invalid supplierID format\"}");
        } catch (Exception e) {
            System.err.println("ERROR in handleGetSupplierProducts: " + e.getMessage());
            e.printStackTrace();
            resp.getWriter().write("{\"error\": \"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        String userLogin = (String) req.getSession().getAttribute("UserLogin");
        UUID userID = userLogin != null ? UUID.fromString(userLogin) : null;

        if ("create".equals(action)) {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("PurchaseOrderServlet - CREATE action");
            System.out.println("  User ID: " + userID);
            
            try {
                // Validate user is logged in
                if (userID == null) {
                    System.err.println("❌ CREATE FAILED: User not logged in");
                    throw new IllegalStateException("Bạn chưa đăng nhập. Vui lòng đăng nhập lại.");
                }
                
                // Get parameters from form (match with frontend field names)
                String supplierIDParam = req.getParameter("supplierID");
                String expectedDeliveryParam = req.getParameter("expectedDelivery");
                String notes = req.getParameter("notes");
                
                System.out.println("  [Input] SupplierID param: " + supplierIDParam);
                System.out.println("  [Input] Expected delivery param: " + expectedDeliveryParam);
                System.out.println("  [Input] Notes: " + (notes != null ? notes.substring(0, Math.min(50, notes.length())) : "null"));

                // Validate required fields
                if (supplierIDParam == null || supplierIDParam.trim().isEmpty()) {
                    System.err.println("❌ CREATE FAILED: SupplierID is empty");
                    throw new IllegalArgumentException("Nhà cung cấp không được để trống");
                }
                if (expectedDeliveryParam == null || expectedDeliveryParam.trim().isEmpty()) {
                    System.err.println("❌ CREATE FAILED: Expected delivery is empty");
                    throw new IllegalArgumentException("Ngày giao dự kiến không được để trống");
                }

                // Parse UUID with proper error handling
                UUID supplierID;
                try {
                    supplierID = UUID.fromString(supplierIDParam);
                    System.out.println("  [Parse] SupplierID: " + supplierIDParam + " -> " + supplierID);
                } catch (IllegalArgumentException e) {
                    System.err.println("  [Parse] Invalid SupplierID format: " + supplierIDParam);
                    throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ");
                }

                // Parse date with proper error handling
                // HTML datetime-local format: "YYYY-MM-DDTHH:mm" or "YYYY-MM-DDTHH:mm:ss"
                LocalDateTime expected;
                try {
                    System.out.println("  [Parse] Expected delivery param: " + expectedDeliveryParam);
                    
                    // Try ISO_LOCAL_DATE_TIME format first (YYYY-MM-DDTHH:mm:ss or YYYY-MM-DDTHH:mm)
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    expected = LocalDateTime.parse(expectedDeliveryParam, formatter);
                    
                    System.out.println("  [Parse] Parsed date: " + expected);
                } catch (java.time.format.DateTimeParseException e) {
                    System.err.println("  [Parse] DateTimeParseException: " + e.getMessage());
                    // Try alternative format without seconds
                    try {
                        java.time.format.DateTimeFormatter altFormatter = 
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                        expected = LocalDateTime.parse(expectedDeliveryParam, altFormatter);
                        System.out.println("  [Parse] Parsed with alternative format: " + expected);
                    } catch (Exception e2) {
                        System.err.println("  [Parse] Alternative format also failed: " + e2.getMessage());
                        throw new IllegalArgumentException("Ngày giao dự kiến không đúng định dạng. Vui lòng chọn lại ngày.");
                    }
                } catch (Exception e) {
                    System.err.println("  [Parse] Unexpected exception: " + e.getMessage());
                    e.printStackTrace();
                    throw new IllegalArgumentException("Ngày giao dự kiến không đúng định dạng. Vui lòng chọn lại ngày.");
                }

                // Get item arrays (match with frontend field names)
                String[] names = req.getParameterValues("itemName");
                String[] qtys = req.getParameterValues("quantity");
                String[] prices = req.getParameterValues("unitPrice");
                
                System.out.println("  [Input] Item names count: " + (names != null ? names.length : 0));
                System.out.println("  [Input] Quantities count: " + (qtys != null ? qtys.length : 0));
                System.out.println("  [Input] Prices count: " + (prices != null ? prices.length : 0));

                // Validate items array consistency
                if (names == null || names.length == 0) {
                    System.err.println("❌ CREATE FAILED: No items provided");
                    throw new IllegalArgumentException("Phải có ít nhất 1 sản phẩm");
                }
                if (qtys == null || qtys.length != names.length) {
                    System.err.println("❌ CREATE FAILED: Quantities array length mismatch. Names: " + names.length + ", Quantities: " + (qtys != null ? qtys.length : 0));
                    throw new IllegalArgumentException("Số lượng sản phẩm không khớp với danh sách sản phẩm");
                }
                if (prices == null || prices.length != names.length) {
                    System.err.println("❌ CREATE FAILED: Prices array length mismatch. Names: " + names.length + ", Prices: " + (prices != null ? prices.length : 0));
                    throw new IllegalArgumentException("Đơn giá sản phẩm không khớp với danh sách sản phẩm");
                }
                
                System.out.println("  [Parse] Starting to parse " + names.length + " items...");

                List<PurchaseOrderItem> items = new ArrayList<>();
                for (int i = 0; i < names.length; i++) {
                    if (names[i] != null && !names[i].trim().isEmpty()) {
                        PurchaseOrderItem item = new PurchaseOrderItem();
                        item.setItemName(names[i].trim());
                        
                        // Parse quantity with proper error handling
                        // Format Việt Nam: "1.000" -> "1000" (remove all non-digits)
                        try {
                            String qtyStr = qtys[i] != null ? qtys[i].trim() : "";
                            if (qtyStr.isEmpty()) {
                                throw new IllegalArgumentException("Số lượng sản phẩm \"" + names[i] + "\" không được để trống");
                            }
                            
                            // Remove all non-digit characters (thousand separators, etc.)
                            String qtyClean = qtyStr.replaceAll("[^0-9]", "");
                            if (qtyClean.isEmpty()) {
                                throw new IllegalArgumentException("Số lượng sản phẩm \"" + names[i] + "\" không hợp lệ");
                            }
                            
                            int quantity = Integer.parseInt(qtyClean);
                            if (quantity <= 0) {
                                throw new IllegalArgumentException("Số lượng sản phẩm \"" + names[i] + "\" phải lớn hơn 0");
                            }
                            if (quantity > 100000) {
                                throw new IllegalArgumentException("Số lượng sản phẩm \"" + names[i] + "\" không được vượt quá 100,000");
                            }
                            
                            item.setQuantity(quantity);
                            System.out.println("  [Parse] Item " + (i+1) + " - Quantity: " + qtyStr + " -> " + quantity);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Số lượng sản phẩm \"" + names[i] + "\" không hợp lệ: " + e.getMessage());
                        }
                        
                        // Parse price with proper error handling
                        // Format Việt Nam: "1.000.000" -> "1000000" or "1.000.500,50" -> "1000500.50"
                        // Handle both dot (.) and comma (,) as decimal separator
                        try {
                            String priceStr = prices[i] != null ? prices[i].trim() : "";
                            if (priceStr.isEmpty()) {
                                throw new IllegalArgumentException("Đơn giá sản phẩm \"" + names[i] + "\" không được để trống");
                            }
                            
                            // Remove thousand separators (dots in Vietnamese format)
                            // Keep only digits and one decimal separator (last dot or comma)
                            String priceClean = priceStr.replaceAll("[^0-9.,]", "");
                            
                            // Determine decimal separator: if has both dot and comma, use the last one
                            int lastDot = priceClean.lastIndexOf('.');
                            int lastComma = priceClean.lastIndexOf(',');
                            
                            if (lastDot > lastComma) {
                                // Dot is decimal separator, remove other dots (thousand separators)
                                priceClean = priceClean.substring(0, lastDot).replace(".", "") + "." + priceClean.substring(lastDot + 1).replace(".", "");
                            } else if (lastComma > lastDot) {
                                // Comma is decimal separator, remove dots and other commas
                                priceClean = priceClean.substring(0, lastComma).replaceAll("[.,]", "") + "." + priceClean.substring(lastComma + 1).replaceAll("[.,]", "");
                            } else {
                                // No decimal separator, just remove all non-digits
                                priceClean = priceClean.replaceAll("[^0-9]", "");
                            }
                            
                            if (priceClean.isEmpty()) {
                                throw new IllegalArgumentException("Đơn giá sản phẩm \"" + names[i] + "\" không hợp lệ");
                            }
                            
                            double price = Double.parseDouble(priceClean);
                            
                            // Validate price
                            if (Double.isNaN(price) || Double.isInfinite(price)) {
                                throw new IllegalArgumentException("Đơn giá sản phẩm \"" + names[i] + "\" không hợp lệ (NaN hoặc Infinity)");
                            }
                            if (price <= 0) {
                                throw new IllegalArgumentException("Đơn giá sản phẩm \"" + names[i] + "\" phải lớn hơn 0");
                            }
                            if (price > 1000000000) {
                                throw new IllegalArgumentException("Đơn giá sản phẩm \"" + names[i] + "\" không được vượt quá 1,000,000,000 VNĐ");
                            }
                            
                            item.setUnitPrice(price);
                            System.out.println("  [Parse] Item " + (i+1) + " - Price: " + priceStr + " -> " + price);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Đơn giá sản phẩm \"" + names[i] + "\" không hợp lệ: " + e.getMessage());
                        }
                        
                        items.add(item);
                    }
                }

                if (items.isEmpty()) {
                    System.err.println("❌ CREATE FAILED: No valid items after parsing");
                    throw new IllegalArgumentException("Phải có ít nhất 1 sản phẩm hợp lệ");
                }
                
                System.out.println("  [Parse] Successfully parsed " + items.size() + " items");
                System.out.println("  [Service] Calling createPurchaseOrder()...");
                System.out.println("    - SupplierID: " + supplierID);
                System.out.println("    - CreatedBy: " + userID);
                System.out.println("    - ExpectedDelivery: " + expected);
                System.out.println("    - Notes: " + (notes != null && !notes.isEmpty() ? "Yes" : "No"));
                System.out.println("    - Items count: " + items.size());

                // Call service (service will do additional validation)
                UUID poid;
                try {
                    poid = service.createPurchaseOrder(supplierID, userID, expected, notes, items);
                    System.out.println("  [Service] ✅ PO created successfully: " + poid);
                } catch (Exception e) {
                    System.err.println("  [Service] ❌ createPurchaseOrder() threw exception: " + e.getMessage());
                    e.printStackTrace();
                    throw e; // Re-throw to be caught by outer catch blocks
                }
                
                // Success - redirect with success message
                System.out.println("  [Response] Redirecting to success page with POID: " + poid.toString().substring(0, 8));
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                resp.sendRedirect(req.getContextPath() + "/procurement/po?status=created&poid=" + poid.toString().substring(0, 8));
                
            } catch (IllegalStateException e) {
                // State errors (e.g., not logged in) - must be caught before IllegalArgumentException
                System.err.println("ERROR: State error - " + e.getMessage());
                String errorMsg = java.net.URLEncoder.encode(e.getMessage(), "UTF-8");
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + errorMsg);
            } catch (java.time.format.DateTimeParseException e) {
                // Date parsing errors - must be caught before IllegalArgumentException
                System.err.println("ERROR: Date parsing failed - " + e.getMessage());
                String errorMsg = java.net.URLEncoder.encode("Ngày giao dự kiến không đúng định dạng. Vui lòng chọn lại ngày.", "UTF-8");
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + errorMsg);
            } catch (NumberFormatException e) {
                // Number parsing errors - must be caught before IllegalArgumentException
                System.err.println("ERROR: Number parsing failed - " + e.getMessage());
                String errorMsg = java.net.URLEncoder.encode("Số lượng hoặc đơn giá không hợp lệ. Vui lòng kiểm tra lại.", "UTF-8");
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + errorMsg);
            } catch (IllegalArgumentException e) {
                // Validation errors - user-friendly messages (catch after more specific exceptions)
                System.err.println("ERROR: Validation failed - " + e.getMessage());
                String errorMsg = java.net.URLEncoder.encode(e.getMessage(), "UTF-8");
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + errorMsg);
            } catch (Exception e) {
                // Unexpected errors - log full stack trace but show user-friendly message
                System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.err.println("❌ CREATE FAILED: Unexpected exception");
                System.err.println("  Exception type: " + e.getClass().getName());
                System.err.println("  Exception message: " + e.getMessage());
                System.err.println("  Stack trace:");
                e.printStackTrace();
                System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
                // Show more detailed error in development (you can check for a debug flag)
                String errorMsg = "Lỗi hệ thống khi tạo đơn hàng. Vui lòng thử lại sau hoặc liên hệ quản trị viên.";
                if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                    // Include exception message for debugging
                    errorMsg += " (Chi tiết: " + e.getMessage() + ")";
                }
                
                String encodedError = java.net.URLEncoder.encode(errorMsg, "UTF-8");
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + encodedError);
            }
        }

        if ("approve".equals(action)) {
            try {
                String poidParam = req.getParameter("poid");
                String levelParam = req.getParameter("level");
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("PurchaseOrderServlet - APPROVE action");
                System.out.println("  POID param: " + poidParam);
                System.out.println("  Level param: " + levelParam);
                System.out.println("  User ID: " + userID);
                
                if (poidParam == null || poidParam.trim().isEmpty()) {
                    System.err.println("❌ APPROVE FAILED: Missing POID parameter");
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                        java.net.URLEncoder.encode("Thiếu thông tin đơn hàng", "UTF-8"));
                    return;
                }
                
                if (userID == null) {
                    System.err.println("❌ APPROVE FAILED: User not logged in");
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                        java.net.URLEncoder.encode("Bạn chưa đăng nhập", "UTF-8"));
                    return;
                }
                
                UUID poid = UUID.fromString(poidParam);
                int level = levelParam != null && !levelParam.isEmpty() ? Integer.parseInt(levelParam) : 1;
                
                System.out.println("  Calling approvePO()...");
                boolean success = service.approvePO(poid, userID, level);
                
                if (success) {
                    System.out.println("✅ APPROVE SUCCESS - Redirecting with status=approved");
                    
                    // Refresh PO pending notification immediately
                    try {
                        com.liteflow.service.alert.AlertService alertService = new com.liteflow.service.alert.AlertService();
                        alertService.refreshPOPendingNotification();
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to refresh notification (non-critical): " + e.getMessage());
                    }
                    
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?status=approved");
                } else {
                    System.err.println("❌ APPROVE FAILED - service.approvePO() returned false");
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                        java.net.URLEncoder.encode("Không thể duyệt đơn hàng. Vui lòng kiểm tra console logs.", "UTF-8"));
                }
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
            } catch (IllegalArgumentException e) {
                System.err.println("❌ APPROVE FAILED: Invalid parameters - " + e.getMessage());
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                    java.net.URLEncoder.encode("Thông tin không hợp lệ: " + e.getMessage(), "UTF-8"));
            } catch (Exception e) {
                System.err.println("❌ APPROVE FAILED: Unexpected error - " + e.getMessage());
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                    java.net.URLEncoder.encode("Lỗi hệ thống: " + e.getMessage(), "UTF-8"));
            }
        }

        if ("reject".equals(action)) {
            try {
                String poidParam = req.getParameter("poid");
                String reason = req.getParameter("reason");
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("PurchaseOrderServlet - REJECT action");
                System.out.println("  POID param: " + poidParam);
                System.out.println("  Reason: " + reason);
                System.out.println("  User ID: " + userID);
                
                if (poidParam == null || poidParam.trim().isEmpty()) {
                    System.err.println("❌ REJECT FAILED: Missing POID parameter");
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                        java.net.URLEncoder.encode("Thiếu thông tin đơn hàng", "UTF-8"));
                    return;
                }
                
                if (userID == null) {
                    System.err.println("❌ REJECT FAILED: User not logged in");
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                        java.net.URLEncoder.encode("Bạn chưa đăng nhập", "UTF-8"));
                    return;
                }
                
                UUID poid = UUID.fromString(poidParam);
                
                System.out.println("  Calling rejectPO()...");
                boolean success = service.rejectPO(poid, userID, reason);
                
                if (success) {
                    System.out.println("✅ REJECT SUCCESS - Redirecting with status=rejected");
                    
                    // Refresh PO pending notification immediately
                    try {
                        com.liteflow.service.alert.AlertService alertService = new com.liteflow.service.alert.AlertService();
                        alertService.refreshPOPendingNotification();
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to refresh notification (non-critical): " + e.getMessage());
                    }
                    
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?status=rejected");
                } else {
                    System.err.println("❌ REJECT FAILED - service.rejectPO() returned false");
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                        java.net.URLEncoder.encode("Không thể từ chối đơn hàng. Vui lòng kiểm tra console logs.", "UTF-8"));
                }
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
            } catch (IllegalArgumentException e) {
                System.err.println("❌ REJECT FAILED: Invalid parameters - " + e.getMessage());
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                    java.net.URLEncoder.encode("Thông tin không hợp lệ: " + e.getMessage(), "UTF-8"));
            } catch (Exception e) {
                System.err.println("❌ REJECT FAILED: Unexpected error - " + e.getMessage());
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                    java.net.URLEncoder.encode("Lỗi hệ thống: " + e.getMessage(), "UTF-8"));
            }
        }

        if ("receive".equals(action)) {
            try {
                String poidParam = req.getParameter("poid");
                String itemsJson = req.getParameter("items");
                String notes = req.getParameter("notes");
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("PurchaseOrderServlet - RECEIVE action");
                System.out.println("  POID param: " + poidParam);
                System.out.println("  Items JSON: " + (itemsJson != null ? itemsJson.substring(0, Math.min(100, itemsJson.length())) : "null"));
                
                if (poidParam == null || poidParam.trim().isEmpty()) {
                    System.err.println("❌ RECEIVE FAILED: Missing POID parameter");
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                        java.net.URLEncoder.encode("Thiếu thông tin đơn hàng", "UTF-8"));
                    return;
                }
                
                if (userID == null) {
                    System.err.println("❌ RECEIVE FAILED: User not logged in");
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                        java.net.URLEncoder.encode("Bạn chưa đăng nhập", "UTF-8"));
                    return;
                }
                
                UUID poid = UUID.fromString(poidParam);
                
                // Parse items JSON
                Gson gson = new Gson();
                java.lang.reflect.Type listType = new TypeToken<java.util.List<java.util.Map<String, Object>>>(){}.getType();
                java.util.List<java.util.Map<String, Object>> items = gson.fromJson(itemsJson, listType);
                
                System.out.println("  Calling receiveGoods() with " + items.size() + " items...");
                UUID receiptID = service.receiveGoods(poid, userID, items, notes);
                
                if (receiptID != null) {
                    System.out.println("✅ RECEIVE SUCCESS - Receipt ID: " + receiptID);
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?status=received&receipt=" + receiptID);
                } else {
                    System.err.println("❌ RECEIVE FAILED - service.receiveGoods() returned null");
                    resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                        java.net.URLEncoder.encode("Không thể nhận hàng. Vui lòng kiểm tra console logs.", "UTF-8"));
                }
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
            } catch (IllegalArgumentException e) {
                System.err.println("❌ RECEIVE FAILED: Invalid parameters - " + e.getMessage());
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                    java.net.URLEncoder.encode("Thông tin không hợp lệ: " + e.getMessage(), "UTF-8"));
            } catch (Exception e) {
                System.err.println("❌ RECEIVE FAILED: Unexpected error - " + e.getMessage());
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/procurement/po?error=" + 
                    java.net.URLEncoder.encode("Lỗi hệ thống: " + e.getMessage(), "UTF-8"));
            }
        }
    }
}
