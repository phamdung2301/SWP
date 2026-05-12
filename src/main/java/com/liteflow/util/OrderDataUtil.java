package com.liteflow.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Utility class for order data processing
 * Used by both production code and test code
 */
public class OrderDataUtil {
    
    private static final Gson gson = new Gson();
    
    /**
     * Read JSON from request body
     */
    public static String readRequestBody(BufferedReader reader) throws IOException {
        if (reader == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    
    /**
     * Parse JSON request data
     * Returns null if requestBody is null or invalid JSON
     */
    
    public static Map<String, Object> parseRequestData(String requestBody) {
        // ✅ Kiểm tra null trước khi parse
        if (requestBody == null || requestBody.trim().isEmpty()) {
            return null;
        }
        
        try {
            // ✅ Bọc trong try-catch để handle JsonSyntaxException
            return gson.fromJson(requestBody, Map.class);
        } catch (JsonSyntaxException e) {
            System.err.println("❌ JSON sai định dạng: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate table ID
     * Cho phép cả UUID và special table IDs (takeaway, delivery)
     */
    public static String validateTableId(Map<String, Object> requestData) {
        // ✅ Kiểm tra null cho requestData
        if (requestData == null) {
            return "Request data không hợp lệ";
        }
        
        String tableIdStr = (String) requestData.get("tableId");
        if (tableIdStr == null || tableIdStr.isEmpty()) {
            return "Table ID không được rỗng";
        }
        
        // ✅ Cho phép bàn đặc biệt: takeaway, delivery
        if ("takeaway".equals(tableIdStr) || "delivery".equals(tableIdStr)) {
            return null; // Valid special table
        }
        
        return null; // Valid
    }
    
    /**
     * Validate items array
     */
    
    public static String validateItems(Map<String, Object> requestData) {
        // ✅ Kiểm tra null cho requestData
        if (requestData == null) {
            return "Request data không hợp lệ";
        }
        
        List<Map<String, Object>> items = (List<Map<String, Object>>) requestData.get("items");
        if (items == null || items.isEmpty()) {
            return "Danh sách món không được rỗng";
        }
        return null; // Valid
    }
    
    /**
     * Check if tableId is a special table (takeaway, delivery)
     */
    public static boolean isSpecialTable(String tableIdStr) {
        return "takeaway".equals(tableIdStr) || "delivery".equals(tableIdStr);
    }
    
    /**
     * Convert string to UUID with validation
     * Returns null for special tables (takeaway, delivery)
     */
    public static UUID parseTableId(String tableIdStr) throws IllegalArgumentException {
        if (isSpecialTable(tableIdStr)) {
            return null; // Special tables don't have UUID
        }
        return UUID.fromString(tableIdStr);
    }
    
    /**
     * Extract table ID from request data
     */
    
    public static String extractTableId(Map<String, Object> requestData) {
        // ✅ Kiểm tra null trước khi gọi get()
        if (requestData == null) {
            return null;
        }
        return (String) requestData.get("tableId");
    }
    
    /**
     * Extract items from request data
     */
    public static List<Map<String, Object>> extractItems(Map<String, Object> requestData) {
        // ✅ Kiểm tra null trước khi gọi get()
        if (requestData == null) {
            return null;
        }
        
        
        List<Map<String, Object>> items = (List<Map<String, Object>>) requestData.get("items");
        return items;
    }
    
    /**
     * Check if UUID string is valid
     */
    public static boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Generate test UUID for testing purposes
     */
    public static UUID generateTestUUID() {
        return UUID.randomUUID();
    }
    
    /**
     * Generate deterministic UUID for testing
     */
    public static UUID generateTestUUID(int seed) {
        String uuidString = String.format("%08d-0000-0000-0000-000000000000", seed);
        return UUID.fromString(uuidString);
    }
}
