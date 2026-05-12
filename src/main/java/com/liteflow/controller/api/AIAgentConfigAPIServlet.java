package com.liteflow.controller.api;

import com.liteflow.service.ai.AIAgentConfigService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;

/**
 * API Servlet for AI Agent Configuration
 * 
 * Endpoints:
 * - GET /api/ai-agent-config - Get all configurations
 * - GET /api/ai-agent-config?category=STOCK_ALERT - Get configurations by category
 * - POST /api/ai-agent-config - Update configurations
 * - POST /api/ai-agent-config/reset - Reset configuration(s) to default
 */
@WebServlet("/api/ai-agent-config")
public class AIAgentConfigAPIServlet extends HttpServlet {
    
    private AIAgentConfigService configService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.configService = new AIAgentConfigService();
        System.out.println("✅ AIAgentConfigAPIServlet initialized");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Check authentication
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) {
            sendError(response, "Unauthorized", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Check if user has permission (Admin, Manager, or Owner)
        @SuppressWarnings("unchecked")
        java.util.List<String> userRoles = (java.util.List<String>) session.getAttribute("UserRoles");
        
        boolean hasPermission = false;
        if (userRoles != null) {
            for (String role : userRoles) {
                if ("ADMIN".equalsIgnoreCase(role) || 
                    "MANAGER".equalsIgnoreCase(role) || 
                    "Owner".equalsIgnoreCase(role)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        
        if (!hasPermission) {
            sendError(response, "Forbidden: Admin, Manager, or Owner role required", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            String categoryParam = request.getParameter("category");
            String resource = request.getParameter("resource"); // "suppliers", "categories", or "products"
            
            // Handle resource requests (suppliers, categories, products)
            if (resource != null && !resource.isEmpty()) {
                if ("suppliers".equals(resource)) {
                    handleGetSuppliers(response);
                    return;
                } else if ("categories".equals(resource)) {
                    handleGetCategories(response);
                    return;
                } else if ("products".equals(resource)) {
                    handleGetProducts(response, categoryParam);
                    return;
                }
            }
            
            JSONObject result;
            
            if (categoryParam != null && !categoryParam.isEmpty()) {
                // Get configs by category
                result = configService.getConfigsByCategoryAsJSON(categoryParam);
                result.put("success", true);
                result.put("category", categoryParam);
            } else {
                // Get all configs
                result = configService.getAllConfigsAsJSON();
                result.put("success", true);
            }
            
            result.put("timestamp", System.currentTimeMillis());
            response.getWriter().write(result.toString(2));
            System.out.println("✅ AI Agent Config API Response sent");
            
        } catch (Exception e) {
            System.err.println("❌ AI Agent Config API Error: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Error retrieving configurations: " + e.getMessage(), 
                     HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Check authentication
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) {
            sendError(response, "Unauthorized", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Check if user has permission (Admin, Manager, or Owner)
        @SuppressWarnings("unchecked")
        java.util.List<String> userRoles = (java.util.List<String>) session.getAttribute("UserRoles");
        
        boolean hasPermission = false;
        if (userRoles != null) {
            for (String role : userRoles) {
                if ("ADMIN".equalsIgnoreCase(role) || 
                    "MANAGER".equalsIgnoreCase(role) || 
                    "Owner".equalsIgnoreCase(role)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        
        if (!hasPermission) {
            sendError(response, "Forbidden: Admin, Manager, or Owner role required", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        // Get user ID
        UUID userId = null;
        try {
            String userLogin = (String) session.getAttribute("UserLogin");
            if (userLogin != null && !userLogin.isEmpty()) {
                userId = UUID.fromString(userLogin);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not get user ID from session: " + e.getMessage());
        }
        
        if (userId == null) {
            sendError(response, "User ID not found in session", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            // Read request body
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }
            
            JSONObject requestJson = new JSONObject(requestBody.toString());
            String action = requestJson.optString("action", "");
            
            if ("reset".equals(action)) {
                // Reset to default
                handleReset(requestJson, userId, response);
            } else {
                // Update configurations
                handleUpdate(requestJson, userId, response);
            }
            
        } catch (Exception e) {
            System.err.println("❌ AI Agent Config API Error: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Error processing request: " + e.getMessage(), 
                     HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleUpdate(JSONObject requestJson, UUID userId, HttpServletResponse response) 
            throws IOException {
        
        JSONObject configs = requestJson.optJSONObject("configs");
        if (configs == null) {
            sendError(response, "Missing 'configs' object in request body", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        java.util.Map<String, String> updates = new java.util.HashMap<>();
        for (String key : configs.keySet()) {
            updates.put(key, configs.getString(key));
        }
        
        java.util.Map<String, Boolean> results = configService.updateConfigs(updates, userId);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "Configurations updated");
        result.put("results", new JSONObject(results));
        result.put("timestamp", System.currentTimeMillis());
        
        response.getWriter().write(result.toString(2));
        System.out.println("✅ Updated " + updates.size() + " AI Agent configurations");
    }
    
    private void handleReset(JSONObject requestJson, UUID userId, HttpServletResponse response) 
            throws IOException {
        
        String key = requestJson.optString("key", null);
        String category = requestJson.optString("category", null);
        
        JSONObject result = new JSONObject();
        
        if (key != null && !key.isEmpty()) {
            // Reset single config
            boolean success = configService.resetToDefault(key);
            result.put("success", success);
            result.put("message", success ? "Configuration reset to default" : "Failed to reset configuration");
            result.put("key", key);
        } else if (category != null && !category.isEmpty()) {
            // Reset category
            boolean success = configService.resetCategoryToDefault(category);
            result.put("success", success);
            result.put("message", success ? "Category reset to default" : "Failed to reset category");
            result.put("category", category);
        } else {
            sendError(response, "Missing 'key' or 'category' in request body", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        result.put("timestamp", System.currentTimeMillis());
        response.getWriter().write(result.toString(2));
        System.out.println("✅ Reset AI Agent configuration: " + (key != null ? key : category));
    }
    
    private void handleGetSuppliers(HttpServletResponse response) throws IOException {
        try {
            com.liteflow.service.procurement.ProcurementService procurementService = 
                new com.liteflow.service.procurement.ProcurementService();
            java.util.List<com.liteflow.model.procurement.Supplier> suppliers = 
                procurementService.getAllSuppliers();
            
            org.json.JSONArray suppliersArray = new org.json.JSONArray();
            for (com.liteflow.model.procurement.Supplier supplier : suppliers) {
                if (supplier.getIsActive() != null && supplier.getIsActive()) {
                    org.json.JSONObject supplierObj = new org.json.JSONObject();
                    supplierObj.put("supplierID", supplier.getSupplierID().toString());
                    supplierObj.put("name", supplier.getName());
                    supplierObj.put("email", supplier.getEmail() != null ? supplier.getEmail() : "");
                    supplierObj.put("phone", supplier.getPhone() != null ? supplier.getPhone() : "");
                    suppliersArray.put(supplierObj);
                }
            }
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("suppliers", suppliersArray);
            result.put("count", suppliersArray.length());
            result.put("timestamp", System.currentTimeMillis());
            
            response.getWriter().write(result.toString(2));
            System.out.println("✅ Returned " + suppliersArray.length() + " active suppliers");
            
        } catch (Exception e) {
            System.err.println("❌ Error getting suppliers: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Error retrieving suppliers: " + e.getMessage(), 
                     HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleGetCategories(HttpServletResponse response) throws IOException {
        try {
            com.liteflow.service.inventory.ProductService productService = 
                new com.liteflow.service.inventory.ProductService();
            java.util.List<String> categories = productService.getDistinctCategoriesFromProducts();
            
            org.json.JSONArray categoriesArray = new org.json.JSONArray();
            for (String category : categories) {
                if (category != null && !category.trim().isEmpty()) {
                    org.json.JSONObject categoryObj = new org.json.JSONObject();
                    categoryObj.put("name", category);
                    categoriesArray.put(categoryObj);
                }
            }
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("categories", categoriesArray);
            result.put("count", categoriesArray.length());
            result.put("timestamp", System.currentTimeMillis());
            
            response.getWriter().write(result.toString(2));
            System.out.println("✅ Returned " + categoriesArray.length() + " categories");
            
        } catch (Exception e) {
            System.err.println("❌ Error getting categories: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Error retrieving categories: " + e.getMessage(), 
                     HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleGetProducts(HttpServletResponse response, String categoryName) throws IOException {
        try {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                sendError(response, "Category parameter is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            com.liteflow.service.inventory.ProductInventoryService productInventoryService = 
                new com.liteflow.service.inventory.ProductInventoryService();
            org.json.JSONObject result = productInventoryService.getProductsByCategory(categoryName.trim());
            
            if (!result.optBoolean("success", false)) {
                sendError(response, result.optString("error", "Error retrieving products"), 
                         HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            
            // Format response: chỉ trả về productName và productId (đơn giản hóa)
            org.json.JSONArray products = result.optJSONArray("products");
            org.json.JSONArray simplifiedProducts = new org.json.JSONArray();
            
            if (products != null) {
                for (int i = 0; i < products.length(); i++) {
                    org.json.JSONObject product = products.getJSONObject(i);
                    org.json.JSONObject simplified = new org.json.JSONObject();
                    simplified.put("productName", product.optString("productName", ""));
                    simplified.put("productId", product.optString("productId", ""));
                    simplified.put("categoryName", product.optString("categoryName", categoryName));
                    simplifiedProducts.put(simplified);
                }
            }
            
            JSONObject responseObj = new JSONObject();
            responseObj.put("success", true);
            responseObj.put("products", simplifiedProducts);
            responseObj.put("category", categoryName);
            responseObj.put("count", simplifiedProducts.length());
            responseObj.put("timestamp", System.currentTimeMillis());
            
            response.getWriter().write(responseObj.toString(2));
            System.out.println("✅ Returned " + simplifiedProducts.length() + " products for category: " + categoryName);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting products: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Error retrieving products: " + e.getMessage(), 
                     HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        JSONObject error = new JSONObject();
        error.put("success", false);
        error.put("error", message);
        response.setStatus(statusCode);
        response.getWriter().write(error.toString(2));
    }
}

