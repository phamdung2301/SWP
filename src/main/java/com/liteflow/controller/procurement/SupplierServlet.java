package com.liteflow.controller.procurement;

import com.liteflow.service.procurement.ProcurementService;
import com.liteflow.model.procurement.Supplier;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

// @WebServlet(urlPatterns = {"/procurement/supplier"}) // Disabled - using web.xml mapping
public class SupplierServlet extends HttpServlet {
    private final ProcurementService service = new ProcurementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, jakarta.servlet.ServletException {
        try {
            // Check if this is a JSON API request (for fetching single supplier)
            String supplierIdParam = req.getParameter("id");
            String format = req.getParameter("format");
            
            if (supplierIdParam != null && "json".equals(format)) {
                // Return JSON for single supplier
                handleGetSupplierJson(supplierIdParam, resp);
                return;
            }
            
            System.out.println("=== SUPPLIER SERVLET DEBUG ===");
            
            // Set content type and encoding FIRST
            resp.setContentType("text/html; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            
            // Disable chunked encoding to prevent ERR_INCOMPLETE_CHUNKED_ENCODING
            resp.setHeader("Transfer-Encoding", "identity");
            
            List<Supplier> suppliers = service.getAllSuppliers();
            System.out.println("Suppliers loaded: " + suppliers.size());
            
            // Set in request scope for JSP
            req.setAttribute("suppliers", suppliers);
            
            System.out.println("Forwarding to supplier-list.jsp");
            // Try simple version first
            req.getRequestDispatcher("/procurement/supplier-list-simple.jsp").forward(req, resp);
            
        } catch (Exception e) {
            System.err.println("ERROR in SupplierServlet doGet: " + e.getMessage());
            e.printStackTrace();
            
            // Send error response with proper encoding
            resp.setContentType("text/html; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("<html><head><meta charset='UTF-8'></head><body><h1>Lỗi hệ thống</h1><p>" + e.getMessage() + "</p><a href='/LiteFlow/procurement/supplier-simple'>Thử phiên bản đơn giản</a></body></html>");
            resp.getWriter().flush();
        }
    }
    
    /**
     * Handle GET request for single supplier JSON
     * Endpoint: /procurement/supplier?id={uuid}&format=json
     */
    private void handleGetSupplierJson(String supplierIdParam, HttpServletResponse resp) throws IOException {
        try {
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            
            UUID supplierId = UUID.fromString(supplierIdParam);
            Supplier supplier = service.getSupplierById(supplierId);
            
            if (supplier == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"success\": false, \"message\": \"Supplier not found\"}");
                return;
            }
            
            // Build JSON response
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"success\": true,");
            json.append("\"data\": {");
            json.append("\"id\": \"").append(supplier.getSupplierID()).append("\",");
            json.append("\"name\": \"").append(escapeJson(supplier.getName())).append("\",");
            json.append("\"contact\": \"").append(escapeJson(supplier.getContact())).append("\",");
            json.append("\"email\": \"").append(escapeJson(supplier.getEmail())).append("\",");
            json.append("\"phone\": \"").append(escapeJson(supplier.getPhone())).append("\",");
            json.append("\"address\": \"").append(escapeJson(supplier.getAddress())).append("\",");
            json.append("\"taxCode\": \"").append(escapeJson(supplier.getTaxCode())).append("\",");
            json.append("\"rating\": ").append(supplier.getRating() != null ? supplier.getRating() : 0).append(",");
            json.append("\"onTimeRate\": ").append(supplier.getOnTimeRate() != null ? supplier.getOnTimeRate() : 0).append(",");
            json.append("\"isActive\": ").append(supplier.getIsActive() != null && supplier.getIsActive());
            json.append("}");
            json.append("}");
            
            resp.getWriter().write(json.toString());
            resp.getWriter().flush();
            
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\": false, \"message\": \"Invalid supplier ID format\"}");
        } catch (Exception e) {
            System.err.println("ERROR in handleGetSupplierJson: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"message\": \"Error fetching supplier: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Escape JSON string
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, jakarta.servlet.ServletException {
        try {
            // Set proper encoding
            req.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            
            // Check if it's a JSON request
            String contentType = req.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                handleJsonRequest(req, resp);
            } else {
                // Handle form request
                String action = req.getParameter("action");
                if ("update".equals(action)) {
                    handleUpdateSupplier(req, resp);
                } else {
                    handleCreateSupplier(req, resp);
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR in doPost: " + e.getMessage());
            e.printStackTrace();
            
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private void handleJsonRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Read JSON from request body
        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (java.io.BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }
        
        String jsonString = jsonBuffer.toString();
        System.out.println("Received JSON: " + jsonString);
        
        // Parse JSON (simple parsing for now)
        String action = extractJsonValue(jsonString, "action");
        
        if ("update".equals(action)) {
            handleJsonUpdateSupplier(jsonString, resp);
        } else if ("create".equals(action)) {
            handleJsonCreateSupplier(jsonString, req, resp);
        } else {
            resp.getWriter().write("{\"success\": false, \"message\": \"Unknown action\"}");
        }
    }
    
    private void handleJsonUpdateSupplier(String jsonString, HttpServletResponse resp) throws IOException {
        try {
            // Clean and validate JSON string
            jsonString = jsonString.trim();
            System.out.println("Processing JSON: " + jsonString);
            
            // Parse JSON using safer approach
            String supplierId = extractJsonValueSafe(jsonString, "supplierId");
            String name = extractJsonValueSafe(jsonString, "name");
            String contact = extractJsonValueSafe(jsonString, "contact");
            String email = extractJsonValueSafe(jsonString, "email");
            String phone = extractJsonValueSafe(jsonString, "phone");
            String address = extractJsonValueSafe(jsonString, "address");
            String taxCode = extractJsonValueSafe(jsonString, "taxCode");
            String ratingStr = extractJsonValueSafe(jsonString, "rating");
            String onTimeRateStr = extractJsonValueSafe(jsonString, "onTimeRate");
            String isActiveStr = extractJsonValueSafe(jsonString, "isActive");
            
            System.out.println("Parsed values:");
            System.out.println("SupplierId: " + supplierId);
            System.out.println("Name: " + name);
            System.out.println("Contact: " + contact);
            System.out.println("Email: " + email);
            System.out.println("Phone: " + phone);
            System.out.println("Address: " + address);
            System.out.println("TaxCode: " + taxCode);
            System.out.println("Rating: " + ratingStr);
            System.out.println("OnTimeRate: " + onTimeRateStr);
            System.out.println("IsActive: " + isActiveStr);
            
            // Validate required fields
            if (supplierId == null || supplierId.trim().isEmpty()) {
                resp.getWriter().write("{\"success\": false, \"message\": \"Supplier ID is required\"}");
                return;
            }
            
            if (name == null || name.trim().isEmpty()) {
                resp.getWriter().write("{\"success\": false, \"message\": \"Supplier name is required\"}");
                return;
            }
            
            // Parse numeric values safely
            Double rating = 0.0;
            Double onTimeRate = 0.0;
            Boolean isActive = false;
            
            try {
                if (ratingStr != null && !ratingStr.trim().isEmpty()) {
                    rating = Double.parseDouble(ratingStr);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid rating value: " + ratingStr);
            }
            
            try {
                if (onTimeRateStr != null && !onTimeRateStr.trim().isEmpty()) {
                    onTimeRate = Double.parseDouble(onTimeRateStr);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid onTimeRate value: " + onTimeRateStr);
            }
            
            isActive = "true".equals(isActiveStr) || Boolean.parseBoolean(isActiveStr);
            
            // Get existing supplier first
            Supplier existingSupplier = service.getSupplierById(UUID.fromString(supplierId));
            if (existingSupplier == null) {
                resp.getWriter().write("{\"success\": false, \"message\": \"Supplier not found\"}");
                return;
            }
            
            // Update supplier fields
            existingSupplier.setName(name);
            existingSupplier.setContact(contact);
            existingSupplier.setEmail(email);
            existingSupplier.setPhone(phone);
            existingSupplier.setAddress(address);
            existingSupplier.setTaxCode(taxCode != null && !taxCode.trim().isEmpty() ? taxCode.trim() : null);
            existingSupplier.setRating(rating);
            existingSupplier.setOnTimeRate(onTimeRate);
            existingSupplier.setIsActive(isActive);
            
            // Update supplier
            boolean success = service.updateSupplier(existingSupplier);
            
            if (success) {
                resp.getWriter().write("{\"success\": true, \"message\": \"Supplier updated successfully\"}");
            } else {
                resp.getWriter().write("{\"success\": false, \"message\": \"Failed to update supplier\"}");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR updating supplier: " + e.getMessage());
            e.printStackTrace();
            resp.getWriter().write("{\"success\": false, \"message\": \"Update failed: " + e.getMessage() + "\"}");
        }
    }
    
    private void handleJsonCreateSupplier(String jsonString, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Clean and validate JSON string
            jsonString = jsonString.trim();
            System.out.println("Processing create JSON: " + jsonString);
            
            // Parse JSON using safer approach
            String name = extractJsonValueSafe(jsonString, "name");
            String contact = extractJsonValueSafe(jsonString, "contact");
            String email = extractJsonValueSafe(jsonString, "email");
            String phone = extractJsonValueSafe(jsonString, "phone");
            String address = extractJsonValueSafe(jsonString, "address");
            String taxCode = extractJsonValueSafe(jsonString, "taxCode");
            String ratingStr = extractJsonValueSafe(jsonString, "rating");
            String onTimeRateStr = extractJsonValueSafe(jsonString, "onTimeRate");
            String isActiveStr = extractJsonValueSafe(jsonString, "isActive");
            
            System.out.println("Parsed create values:");
            System.out.println("Name: " + name);
            System.out.println("Contact: " + contact);
            System.out.println("Email: " + email);
            System.out.println("Phone: " + phone);
            System.out.println("Address: " + address);
            System.out.println("TaxCode: " + taxCode);
            System.out.println("Rating: " + ratingStr);
            System.out.println("OnTimeRate: " + onTimeRateStr);
            System.out.println("IsActive: " + isActiveStr);
            
            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                resp.getWriter().write("{\"success\": false, \"message\": \"Tên nhà cung cấp không được để trống\"}");
                return;
            }
            
            if (email == null || email.trim().isEmpty()) {
                resp.getWriter().write("{\"success\": false, \"message\": \"Email không được để trống\"}");
                return;
            }
            
            // Email format validation
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                resp.getWriter().write("{\"success\": false, \"message\": \"Email không đúng định dạng\"}");
                return;
            }
            
            // Parse numeric values safely
            Double rating = 0.0;
            Double onTimeRate = 0.0;
            Boolean isActive = true;
            
            try {
                if (ratingStr != null && !ratingStr.trim().isEmpty()) {
                    rating = Double.parseDouble(ratingStr);
                    if (rating < 0 || rating > 5) {
                        resp.getWriter().write("{\"success\": false, \"message\": \"Đánh giá phải từ 0 đến 5\"}");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid rating value: " + ratingStr);
            }
            
            try {
                if (onTimeRateStr != null && !onTimeRateStr.trim().isEmpty()) {
                    onTimeRate = Double.parseDouble(onTimeRateStr);
                    if (onTimeRate < 0 || onTimeRate > 100) {
                        resp.getWriter().write("{\"success\": false, \"message\": \"Tỷ lệ đúng hẹn phải từ 0 đến 100%\"}");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid onTimeRate value: " + onTimeRateStr);
            }
            
            isActive = "true".equals(isActiveStr) || Boolean.parseBoolean(isActiveStr);
            
            // Get current user ID for createdBy
            String userLogin = (String) req.getSession().getAttribute("UserLogin");
            UUID createdBy = userLogin != null ? UUID.fromString(userLogin) : null;
            
            // Create supplier (returns UUID)
            UUID newSupplierId = service.createSupplier(name, createdBy, email);
            
            if (newSupplierId != null) {
                // Get the created supplier to update additional fields
                Supplier newSupplier = service.getSupplierById(newSupplierId);
                
                if (newSupplier != null) {
                    // Update additional fields
                    newSupplier.setContact(contact);
                    newSupplier.setPhone(phone);
                    newSupplier.setAddress(address);
                    newSupplier.setTaxCode(taxCode != null && !taxCode.trim().isEmpty() ? taxCode.trim() : null);
                    newSupplier.setRating(rating);
                    newSupplier.setOnTimeRate(onTimeRate);
                    newSupplier.setIsActive(isActive);
                    
                    // Save updated supplier
                    boolean success = service.updateSupplier(newSupplier);
                    
                    if (success) {
                        resp.getWriter().write("{\"success\": true, \"message\": \"Nhà cung cấp '" + name + "' đã được thêm thành công\"}");
                    } else {
                        resp.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi lưu thông tin bổ sung\"}");
                    }
                } else {
                    resp.getWriter().write("{\"success\": false, \"message\": \"Không thể tìm thấy nhà cung cấp vừa tạo\"}");
                }
            } else {
                resp.getWriter().write("{\"success\": false, \"message\": \"Không thể tạo nhà cung cấp mới\"}");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR creating supplier: " + e.getMessage());
            e.printStackTrace();
            resp.getWriter().write("{\"success\": false, \"message\": \"Tạo nhà cung cấp thất bại: " + e.getMessage() + "\"}");
        }
    }
    
    private String extractJsonValueSafe(String json, String key) {
        try {
            // Escape special characters in key
            String escapedKey = key.replaceAll("[\\[\\]{}()*+?.\\\\^$|]", "\\\\$0");
            
            // First try to extract string values
            String stringPattern = "\"" + escapedKey + "\"\\s*:\\s*\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(stringPattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                String value = m.group(1);
                // Decode common escape sequences
                value = value.replace("\\\"", "\"")
                           .replace("\\\\", "\\")
                           .replace("\\n", "\n")
                           .replace("\\r", "\r")
                           .replace("\\t", "\t");
                return value;
            }
            
            // Try to extract non-string values (numbers, booleans, null)
            String valuePattern = "\"" + escapedKey + "\"\\s*:\\s*([^,}]+)";
            p = java.util.regex.Pattern.compile(valuePattern);
            m = p.matcher(json);
            if (m.find()) {
                String value = m.group(1).trim();
                
                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                    // Decode escape sequences
                    value = value.replace("\\\"", "\"")
                               .replace("\\\\", "\\")
                               .replace("\\n", "\n")
                               .replace("\\r", "\r")
                               .replace("\\t", "\t");
                }
                
                return value;
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting JSON value for key: " + key + ", Error: " + e.getMessage());
        }
        return null;
    }
    
    private String extractJsonValue(String json, String key) {
        return extractJsonValueSafe(json, key);
    }
    
    private void handleCreateSupplier(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String userLogin = (String) req.getSession().getAttribute("UserLogin");
        UUID createdBy = userLogin != null ? UUID.fromString(userLogin) : null;
        service.createSupplier(name, createdBy, email);
        resp.sendRedirect(req.getContextPath() + "/procurement/supplier");
    }
    
    private void handleUpdateSupplier(HttpServletRequest req, HttpServletResponse resp) throws IOException, jakarta.servlet.ServletException {
        try {
            System.out.println("=== UPDATE SUPPLIER REQUEST ===");
            
            // Get parameters
            String supplierId = req.getParameter("supplierId");
            String name = req.getParameter("name");
            String contact = req.getParameter("contact");
            String email = req.getParameter("email");
            String phone = req.getParameter("phone");
            String address = req.getParameter("address");
            String ratingStr = req.getParameter("rating");
            String onTimeRateStr = req.getParameter("onTimeRate");
            String isActiveStr = req.getParameter("isActive");
            
            System.out.println("Parameters: " + supplierId + ", " + name + ", " + email);
            
            // Business validation
            if (supplierId == null || supplierId.trim().isEmpty()) {
                throw new IllegalArgumentException("Supplier ID is required");
            }
            
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên nhà cung cấp không được để trống");
            }
            
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email không được để trống");
            }
            
            // Email format validation
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Email không đúng định dạng");
            }
            
            UUID supplierUUID = UUID.fromString(supplierId);
            
            // Get existing supplier
            Supplier existingSupplier = service.getSupplierById(supplierUUID);
            if (existingSupplier == null) {
                throw new IllegalArgumentException("Không tìm thấy nhà cung cấp với ID: " + supplierId);
            }
            
            System.out.println("Found supplier: " + existingSupplier.getName());
            
            // Update fields with business logic
            existingSupplier.setName(name.trim());
            existingSupplier.setContact(contact != null ? contact.trim() : null);
            existingSupplier.setEmail(email.trim().toLowerCase());
            existingSupplier.setPhone(phone != null ? phone.trim() : null);
            existingSupplier.setAddress(address != null ? address.trim() : null);
            
            // Rating validation (0-5)
            if (ratingStr != null && !ratingStr.trim().isEmpty()) {
                double rating = Double.parseDouble(ratingStr);
                if (rating < 0 || rating > 5) {
                    throw new IllegalArgumentException("Đánh giá phải từ 0 đến 5");
                }
                existingSupplier.setRating(rating);
            }
            
            // On-time rate validation (0-100)
            if (onTimeRateStr != null && !onTimeRateStr.trim().isEmpty()) {
                double onTimeRate = Double.parseDouble(onTimeRateStr);
                if (onTimeRate < 0 || onTimeRate > 100) {
                    throw new IllegalArgumentException("Tỷ lệ đúng hạn phải từ 0 đến 100%");
                }
                existingSupplier.setOnTimeRate(onTimeRate);
            }
            
            // Status update
            if (isActiveStr != null) {
                existingSupplier.setIsActive(Boolean.parseBoolean(isActiveStr));
            }
            
            // Save updated supplier
            boolean success = service.updateSupplier(existingSupplier);
            
            if (success) {
                System.out.println("Supplier updated successfully: " + existingSupplier.getName());
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"success\": true, \"message\": \"Nhà cung cấp '" + existingSupplier.getName() + "' đã được cập nhật thành công\"}");
            } else {
                throw new RuntimeException("Lỗi khi lưu dữ liệu vào database");
            }
            
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("Error updating supplier: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"success\": false, \"message\": \"Lỗi hệ thống: " + e.getMessage() + "\"}");
        }
    }
}
