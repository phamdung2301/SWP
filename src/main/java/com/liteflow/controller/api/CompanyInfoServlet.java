package com.liteflow.controller.api;

import com.liteflow.service.CompanyInfoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * API Servlet for Company Info management
 * Endpoint: /api/company-info
 * GET: Get company info (including TaxCode from .env)
 * POST: Update company info (TaxCode is read-only, not updatable)
 */
@WebServlet("/api/company-info")
public class CompanyInfoServlet extends HttpServlet {
    
    private CompanyInfoService companyInfoService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.companyInfoService = new CompanyInfoService();
        System.out.println("✅ CompanyInfoServlet initialized");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Enable CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        try {
            Map<String, Object> companyInfo = companyInfoService.getCompanyInfoWithTaxCode();
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("data", new JSONObject(companyInfo));
            
            response.getWriter().write(result.toString(2));
            System.out.println("✅ CompanyInfo API - GET response sent");
            
        } catch (Exception e) {
            System.err.println("❌ CompanyInfo API Error (GET): " + e.getMessage());
            e.printStackTrace();
            
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("message", "Error retrieving company info");
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(error.toString(2));
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Enable CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
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
            
            // Extract data (exclude taxCode as it's read-only)
            java.util.Map<String, String> data = new java.util.HashMap<>();
            if (requestJson.has("name")) {
                data.put("name", requestJson.getString("name"));
            }
            if (requestJson.has("address")) {
                data.put("address", requestJson.getString("address"));
            }
            if (requestJson.has("phone")) {
                data.put("phone", requestJson.getString("phone"));
            }
            if (requestJson.has("email")) {
                data.put("email", requestJson.getString("email"));
            }
            
            // Update company info
            boolean success = companyInfoService.updateCompanyInfo(data);
            
            JSONObject result = new JSONObject();
            if (success) {
                result.put("success", true);
                result.put("message", "Company info updated successfully");
                
                // Return updated data
                Map<String, Object> updatedInfo = companyInfoService.getCompanyInfoWithTaxCode();
                result.put("data", new JSONObject(updatedInfo));
            } else {
                result.put("success", false);
                result.put("message", "Failed to update company info");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
            response.getWriter().write(result.toString(2));
            System.out.println("✅ CompanyInfo API - POST response sent");
            
        } catch (Exception e) {
            System.err.println("❌ CompanyInfo API Error (POST): " + e.getMessage());
            e.printStackTrace();
            
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("message", "Error updating company info");
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(error.toString(2));
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

