package com.liteflow.controller.sales;

import com.liteflow.dao.sales.SalesInvoiceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SalesInvoiceServlet - API for Sales Invoices (Paid Orders)
 * 
 * Endpoints:
 * - GET /sales/invoices?action=list       - List all sales invoices
 * - GET /sales/invoices?action=details&id=UUID - Get invoice details
 * - GET /sales/invoices?action=search&keyword=xxx - Search invoices
 * - GET /sales/invoices?action=filter&startDate=xxx&endDate=xxx - Filter by date
 */
@WebServlet("/sales/invoices")
public class SalesInvoiceServlet extends HttpServlet {
    
    private SalesInvoiceDAO salesDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.salesDAO = new SalesInvoiceDAO();
        System.out.println("✅ SalesInvoiceServlet initialized");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        String requestURL = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        
        System.out.println("════════════════════════════════════════");
        System.out.println("📊 SALES INVOICE API REQUEST");
        System.out.println("URL: " + requestURL + (queryString != null ? "?" + queryString : ""));
        System.out.println("Action: " + action);
        System.out.println("════════════════════════════════════════");
        
        try {
            if ("details".equals(action)) {
                handleGetDetails(request, response);
            } else if ("search".equals(action)) {
                handleSearch(request, response);
            } else if ("filter".equals(action)) {
                handleFilter(request, response);
            } else {
                // Default: list all
                handleList(request, response);
            }
        } catch (Exception e) {
            System.err.println("❌ Sales Invoice API Error: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("message", "Error processing sales invoice request");
            error.put("stackTrace", e.getClass().getName());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(error.toString());
        }
    }
    
    /**
     * List all sales invoices with pagination
     */
    private void handleList(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        int limit = getIntParameter(request, "limit", 50);
        int offset = getIntParameter(request, "offset", 0);
        
        List<Map<String, Object>> invoices = salesDAO.getAllSalesInvoices(limit, offset);
        long totalCount = salesDAO.getTotalCount();
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("totalCount", totalCount);
        result.put("limit", limit);
        result.put("offset", offset);
        result.put("invoices", buildInvoicesArray(invoices));
        
        response.getWriter().write(result.toString());
        System.out.println("✅ Returned " + invoices.size() + " sales invoices");
    }
    
    /**
     * Get sales invoice details
     */
    private void handleGetDetails(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String orderIdStr = request.getParameter("id");
        if (orderIdStr == null || orderIdStr.isEmpty()) {
            throw new IllegalArgumentException("Missing 'id' parameter");
        }
        
        UUID orderId = UUID.fromString(orderIdStr);
        Map<String, Object> invoice = salesDAO.getSalesInvoiceDetails(orderId);
        
        if (invoice == null) {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("message", "Invoice not found");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(error.toString());
            return;
        }
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("invoice", buildInvoiceJSON(invoice));
        
        response.getWriter().write(result.toString());
        System.out.println("✅ Returned invoice details for: " + orderId);
    }
    
    /**
     * Search sales invoices by keyword
     */
    private void handleSearch(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing 'keyword' parameter");
        }
        
        int limit = getIntParameter(request, "limit", 50);
        int offset = getIntParameter(request, "offset", 0);
        
        List<Map<String, Object>> invoices = salesDAO.searchSalesInvoices(keyword.trim(), limit, offset);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("keyword", keyword);
        result.put("invoices", buildInvoicesArray(invoices));
        result.put("count", invoices.size());
        
        response.getWriter().write(result.toString());
        System.out.println("✅ Search returned " + invoices.size() + " results");
    }
    
    /**
     * Filter sales invoices by date range
     */
    private void handleFilter(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        
        LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : LocalDate.now().minusMonths(1);
        LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : LocalDate.now();
        
        int limit = getIntParameter(request, "limit", 50);
        int offset = getIntParameter(request, "offset", 0);
        
        List<Map<String, Object>> invoices = salesDAO.getSalesInvoicesByDateRange(startDate, endDate, limit, offset);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        result.put("invoices", buildInvoicesArray(invoices));
        result.put("count", invoices.size());
        
        response.getWriter().write(result.toString());
        System.out.println("✅ Filter returned " + invoices.size() + " results");
    }
    
    /**
     * Build JSONArray from invoices list
     */
    private JSONArray buildInvoicesArray(List<Map<String, Object>> invoices) {
        JSONArray array = new JSONArray();
        
        for (Map<String, Object> invoice : invoices) {
            array.put(buildInvoiceJSON(invoice));
        }
        
        return array;
    }
    
    /**
     * Build JSON object from invoice map
     */
    private JSONObject buildInvoiceJSON(Map<String, Object> invoice) {
        JSONObject json = new JSONObject();
        
        // Basic info
        json.put("orderId", invoice.get("orderId").toString());
        json.put("orderNumber", invoice.get("orderNumber"));
        
        // Format date
        if (invoice.get("orderDate") != null) {
            LocalDateTime orderDate = (LocalDateTime) invoice.get("orderDate");
            json.put("orderDate", orderDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            json.put("orderDateFormatted", orderDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        
        // Amounts
        if (invoice.get("totalAmount") != null) {
            BigDecimal totalAmount = (BigDecimal) invoice.get("totalAmount");
            json.put("totalAmount", totalAmount.doubleValue());
        }
        
        if (invoice.get("subTotal") != null) {
            BigDecimal subTotal = (BigDecimal) invoice.get("subTotal");
            json.put("subTotal", subTotal.doubleValue());
        }
        
        if (invoice.get("vat") != null) {
            BigDecimal vat = (BigDecimal) invoice.get("vat");
            json.put("vat", vat.doubleValue());
        }
        
        if (invoice.get("discount") != null) {
            BigDecimal discount = (BigDecimal) invoice.get("discount");
            json.put("discount", discount.doubleValue());
        }
        
        // Payment info
        json.put("paymentMethod", invoice.get("paymentMethod"));
        json.put("paymentStatus", invoice.get("paymentStatus"));
        json.put("status", invoice.get("status"));
        
        // Customer info
        json.put("customerName", invoice.get("customerName"));
        json.put("customerPhone", invoice.get("customerPhone"));
        json.put("tableName", invoice.get("tableName"));
        json.put("roomName", invoice.get("roomName"));
        
        // Staff info
        json.put("createdByName", invoice.get("createdByName"));
        
        // Notes
        json.put("notes", invoice.get("notes"));
        
        // Items (if exists)
        if (invoice.containsKey("items")) {
           
            List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("items");
            JSONArray itemsArray = new JSONArray();
            
            for (Map<String, Object> item : items) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("productName", item.get("productName"));
                itemJson.put("size", item.get("size"));
                itemJson.put("quantity", item.get("quantity"));
                
                if (item.get("unitPrice") != null) {
                    BigDecimal unitPrice = (BigDecimal) item.get("unitPrice");
                    itemJson.put("unitPrice", unitPrice.doubleValue());
                }
                
                if (item.get("totalPrice") != null) {
                    BigDecimal totalPrice = (BigDecimal) item.get("totalPrice");
                    itemJson.put("totalPrice", totalPrice.doubleValue());
                }
                
                itemsArray.put(itemJson);
            }
            
            json.put("items", itemsArray);
        }
        
        return json;
    }
    
    /**
     * Get integer parameter with default value
     */
    private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

