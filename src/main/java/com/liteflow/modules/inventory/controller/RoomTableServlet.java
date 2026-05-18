package com.liteflow.modules.inventory.controller;

import com.liteflow.modules.inventory.model.Room;
import com.liteflow.modules.inventory.model.Table;
import com.liteflow.modules.inventory.model.TableSession;
import com.liteflow.modules.inventory.service.RoomTableService;
import com.liteflow.modules.inventory.service.ExcelService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.JSONObject;
import org.json.JSONArray;

public class RoomTableServlet extends HttpServlet {

    private RoomTableService roomTableService;
    private ExcelService excelService;
    private String cachedJsonString = null; // Cache JSON string to avoid "Stream closed" error

    @Override
    public void init() throws ServletException {
        roomTableService = new RoomTableService();
        excelService = new ExcelService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Lấy danh sách phòng và bàn
            List<Room> rooms = roomTableService.getAllRooms();
            List<Table> tables = roomTableService.getAllTables();
            
            // Debug logging
            System.out.println("=== DEBUG: RoomTableServlet ===");
            System.out.println("Số lượng phòng: " + (rooms != null ? rooms.size() : "null"));
            System.out.println("Số lượng bàn: " + (tables != null ? tables.size() : "null"));

            // Gửi sang JSP
            request.setAttribute("rooms", rooms);
            request.setAttribute("tables", tables);
            request.getRequestDispatcher("/inventory/roomtable.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong RoomTableServlet: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().println("Lỗi: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("=== DEBUG: RoomTableServlet POST method called ===");
        
        // Set encoding for form data
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String action = null;
            
            // Check if request is JSON
            String contentType = request.getContentType();
            System.out.println("Content-Type: " + contentType);
            
            if (contentType != null && contentType.contains("application/json")) {
                // Parse JSON request
                action = parseJsonRequest(request);
            } else {
                // Parse form parameters
                action = request.getParameter("action");
            }
            
            System.out.println("Action: " + action);
            System.out.println("Action length: " + (action != null ? action.length() : "null"));
            System.out.println("Action equals 'addRoom': " + "addRoom".equals(action));
            System.out.println("Action trim equals 'addRoom': " + "addRoom".equals(action != null ? action.trim() : null));

            if ("addRoom".equals(action)) {
                response.setContentType("application/json");
                addRoom(request, response);
            } else if ("addTable".equals(action)) {
                response.setContentType("application/json");
                addTable(request, response);
            } else if ("updateTableStatus".equals(action)) {
                response.setContentType("application/json");
                updateTableStatus(request, response);
            } else if ("deleteRoom".equals(action)) {
                response.setContentType("application/json");
                deleteRoom(request, response);
            } else if ("deleteTable".equals(action)) {
                response.setContentType("application/json");
                deleteTable(request, response);
            } else if ("editRoom".equals(action)) {
                response.setContentType("application/json");
                editRoom(request, response);
            } else if ("editTable".equals(action)) {
                response.setContentType("application/json");
                editTable(request, response);
            } else if ("deleteTable".equals(action)) {
                response.setContentType("application/json");
                deleteTable(request, response);
            } else if ("getTableDetails".equals(action)) {
                response.setContentType("application/json");
                getTableDetails(request, response);
            } else if ("getTableHistory".equals(action)) {
                response.setContentType("application/json");
                getTableHistory(request, response);
            } else if ("getAllTables".equals(action)) {
                response.setContentType("application/json");
                getAllTables(request, response);
            } else if ("getAllRooms".equals(action)) {
                response.setContentType("application/json");
                getAllRooms(request, response);
            } else if ("importExcel".equals(action)) {
                response.setContentType("application/json");
                importExcel(request, response);
            } else if ("checkExcel".equals(action)) {
                response.setContentType("application/json");
                checkExcel(request, response);
            } else if ("exportExcel".equals(action)) {
                exportExcel(request, response);
            } else if ("downloadTemplate".equals(action)) {
                downloadTemplate(request, response);
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Hành động không hợp lệ: '" + action + "'\"}");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong RoomTableServlet POST: " + e.getMessage());
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Có lỗi xảy ra: " + e.getMessage() + "\"}");
        } finally {
            // Reset cache after each request
            cachedJsonString = null;
        }
    }
    
    private String parseJsonRequest(HttpServletRequest request) throws IOException {
        // Cache JSON string to avoid "Stream closed" error
        if (cachedJsonString == null) {
            cachedJsonString = getJsonString(request);
        }
        System.out.println("JSON Request: " + cachedJsonString);
        
        // Extract action from JSON
        String action = extractJsonValue(cachedJsonString, "action");
        System.out.println("Extracted action: " + action);
        return action;
    }
    
    private String getJsonString(HttpServletRequest request) throws IOException {
        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }
        return jsonBuffer.toString();
    }
    
    private String sanitizeForJson(String input) {
        if (input == null) return "";
        return input.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "") // Remove control characters
                   .replace("\\", "\\\\") // Escape backslashes first
                   .replace("\"", "\\\"") // Escape quotes
                   .replace("\n", "\\n") // Escape newlines
                   .replace("\r", "\\r") // Escape carriage returns
                   .replace("\t", "\\t") // Escape tabs
                   .replace("/", "\\/") // Escape forward slashes
                   .trim();
    }
    
    private void writeJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        String sanitizedMessage = sanitizeForJson(message);
        response.getWriter().write("{\"success\": " + success + ", \"message\": \"" + sanitizedMessage + "\"}");
    }
    
    private void writeJsonResponse(HttpServletResponse response, boolean success, String message, String tableId) throws IOException {
        String sanitizedMessage = sanitizeForJson(message);
        String sanitizedTableId = sanitizeForJson(tableId);
        response.getWriter().write("{\"success\": " + success + ", \"message\": \"" + sanitizedMessage + "\", \"tableId\": \"" + sanitizedTableId + "\"}");
    }
    
    private String extractJsonValue(String jsonString, String key) {
        try {
            // Simple JSON parsing for basic key-value pairs
            String searchKey = "\"" + key + "\"";
            int startIndex = jsonString.indexOf(searchKey);
            if (startIndex == -1) return null;
            
            startIndex = jsonString.indexOf(":", startIndex) + 1;
            while (startIndex < jsonString.length() && Character.isWhitespace(jsonString.charAt(startIndex))) {
                startIndex++;
            }
            
            if (startIndex >= jsonString.length()) return null;
            
            int endIndex;
            if (jsonString.charAt(startIndex) == '"') {
                // String value - find matching closing quote
                startIndex++; // Skip opening quote
                endIndex = startIndex;
                while (endIndex < jsonString.length()) {
                    if (jsonString.charAt(endIndex) == '"' && jsonString.charAt(endIndex - 1) != '\\') {
                        break;
                    }
                    endIndex++;
                }
            } else {
                // Number or other value
                endIndex = startIndex;
                while (endIndex < jsonString.length() && 
                       (Character.isDigit(jsonString.charAt(endIndex)) || 
                        jsonString.charAt(endIndex) == '.' || 
                        jsonString.charAt(endIndex) == '-' ||
                        jsonString.charAt(endIndex) == '+')) {
                    endIndex++;
                }
            }
            
            if (endIndex > startIndex && endIndex <= jsonString.length()) {
                String result = jsonString.substring(startIndex, endIndex);
                // Clean up any control characters
                return result.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "");
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing JSON value for key: " + key + ", Error: " + e.getMessage());
            return null;
        }
    }
    
    private void addRoom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String name, description, tableCountStr, totalCapacityStr;
            
            // Check if request is JSON
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                // Parse JSON request using cached string
                System.out.println("JSON String: " + cachedJsonString);
                
                // Simple JSON parsing
                name = extractJsonValue(cachedJsonString, "roomName");
                description = extractJsonValue(cachedJsonString, "roomDescription");
                tableCountStr = extractJsonValue(cachedJsonString, "roomTableCount");
                totalCapacityStr = extractJsonValue(cachedJsonString, "roomTotalCapacity");
            } else {
                // Parse form parameters
                name = request.getParameter("roomName");
                description = request.getParameter("roomDescription");
                tableCountStr = request.getParameter("roomTableCount");
                totalCapacityStr = request.getParameter("roomTotalCapacity");
            }

            System.out.println("=== DEBUG: Add Room ===");
            System.out.println("Action parameter: " + request.getParameter("action"));
            System.out.println("Tên phòng: " + name);
            System.out.println("Mô tả: " + description);
            System.out.println("Số lượng bàn: " + tableCountStr);
            System.out.println("Tổng sức chứa: " + totalCapacityStr);
            System.out.println("All parameters:");
            request.getParameterMap().forEach((key, values) -> {
                System.out.println("  " + key + ": " + String.join(", ", values));
            });

            // Validation
            if (name == null || name.trim().isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"Tên phòng không được để trống\"}");
                return;
            }

            if (name.trim().length() > 100) {
                response.getWriter().write("{\"success\": false, \"message\": \"Tên phòng không được vượt quá 100 ký tự\"}");
                return;
            }

            if (tableCountStr == null || tableCountStr.trim().isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"Số lượng bàn không được để trống\"}");
                return;
            }

            if (totalCapacityStr == null || totalCapacityStr.trim().isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"Tổng sức chứa không được để trống\"}");
                return;
            }

            int tableCount, totalCapacity;
            try {
                tableCount = Integer.parseInt(tableCountStr.trim());
                if (tableCount < 0 || tableCount > 50) {
                    response.getWriter().write("{\"success\": false, \"message\": \"Số lượng bàn phải từ 0 đến 50\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Số lượng bàn không hợp lệ\"}");
                return;
            }

            try {
                totalCapacity = Integer.parseInt(totalCapacityStr.trim());
                if (totalCapacity < 1 || totalCapacity > 1000) {
                    response.getWriter().write("{\"success\": false, \"message\": \"Tổng sức chứa phải từ 1 đến 1000\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Tổng sức chứa không hợp lệ\"}");
                return;
            }

            // Tạo đối tượng Room mới
            Room newRoom = new Room();
            newRoom.setName(name.trim());
            newRoom.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);
            newRoom.setTableCount(tableCount);
            newRoom.setTotalCapacity(totalCapacity);

            System.out.println("=== DEBUG: Room Object ===");
            System.out.println("Room Name: " + newRoom.getName());
            System.out.println("Room Description: " + newRoom.getDescription());
            System.out.println("Room TableCount: " + newRoom.getTableCount());
            System.out.println("Room TotalCapacity: " + newRoom.getTotalCapacity());

            // Lưu phòng
            boolean success = roomTableService.addRoom(newRoom);
            
            if (success) {
                // Return room ID along with success message
                String responseJson = String.format("{\"success\": true, \"message\": \"Thêm phòng thành công!\", \"roomId\": \"%s\"}", 
                    newRoom.getRoomId().toString());
                response.getWriter().write(responseJson);
                System.out.println("✅ Thêm phòng thành công: " + name + " (ID: " + newRoom.getRoomId() + ")");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"Có lỗi xảy ra khi thêm phòng\"}");
                System.out.println("❌ Lỗi khi thêm phòng: " + name);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong addRoom: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Có lỗi xảy ra khi thêm phòng: " + e.getMessage() + "\"}");
        }
    }
    
    private void addTable(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String roomIdStr, tableNumber, tableName, capacityStr;
            
            // Check if request is JSON
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                // Parse from JSON
                roomIdStr = extractJsonValue(cachedJsonString, "roomId");
                tableNumber = extractJsonValue(cachedJsonString, "tableNumber");
                tableName = extractJsonValue(cachedJsonString, "tableName");
                capacityStr = extractJsonValue(cachedJsonString, "capacity");
            } else {
                // Parse from form parameters
                roomIdStr = request.getParameter("roomId");
                tableNumber = request.getParameter("tableNumber");
                tableName = request.getParameter("tableName");
                capacityStr = request.getParameter("capacity");
            }

            System.out.println("=== DEBUG: Add Table ===");
            System.out.println("Content-Type: " + contentType);
            System.out.println("Room ID: " + roomIdStr);
            System.out.println("Số bàn: " + tableNumber);
            System.out.println("Tên bàn: " + tableName);
            System.out.println("Sức chứa: " + capacityStr);

            // Validation
            if (tableNumber == null || tableNumber.trim().isEmpty()) {
                writeJsonResponse(response, false, "Số bàn không được để trống");
                return;
            }

            if (tableName == null || tableName.trim().isEmpty()) {
                writeJsonResponse(response, false, "Tên bàn không được để trống");
                return;
            }

            if (tableNumber.trim().length() > 50) {
                writeJsonResponse(response, false, "Số bàn không được vượt quá 50 ký tự");
                return;
            }

            if (tableName.trim().length() > 100) {
                writeJsonResponse(response, false, "Tên bàn không được vượt quá 100 ký tự");
                return;
            }

            // Tạo đối tượng Table mới
            Table newTable = new Table();
            newTable.setTableNumber(tableNumber.trim());
            newTable.setTableName(tableName.trim());
            newTable.setStatus("Available"); // Always set to Available by default

            // Parse capacity
            if (capacityStr != null && !capacityStr.trim().isEmpty()) {
                try {
                    int capacity = Integer.parseInt(capacityStr.trim());
                    if (capacity > 0 && capacity <= 20) {
                        newTable.setCapacity(capacity);
                    } else {
                        writeJsonResponse(response, false, "Sức chứa phải từ 1 đến 20 người");
                        return;
                    }
                } catch (NumberFormatException e) {
                    writeJsonResponse(response, false, "Sức chứa không hợp lệ");
                    return;
                }
            } else {
                newTable.setCapacity(4); // Default capacity
            }

            // Set room if provided and validate room limits
            if (roomIdStr != null && !roomIdStr.trim().isEmpty()) {
                try {
                    UUID roomId = UUID.fromString(roomIdStr.trim());
                    Room room = roomTableService.getRoomById(roomId);
                    if (room != null) {
                        // Validate table count limit
                        if (room.getTableCount() != null && room.getTableCount() > 0) {
                            int currentTableCount = roomTableService.getCurrentTableCountForRoom(roomId);
                            if (currentTableCount >= room.getTableCount()) {
                                writeJsonResponse(response, false, "Phòng \"" + room.getName() + "\" đã đạt giới hạn tối đa " + room.getTableCount() + " bàn");
                                return;
                            }
                        }
                        
                        // Validate total capacity limit
                        if (room.getTotalCapacity() != null && room.getTotalCapacity() > 0) {
                            int currentTotalCapacity = roomTableService.getCurrentTotalCapacityForRoom(roomId);
                            int newCapacity = newTable.getCapacity();
                            if (currentTotalCapacity + newCapacity > room.getTotalCapacity()) {
                                writeJsonResponse(response, false, "Thêm bàn này sẽ vượt quá giới hạn tổng sức chứa " + room.getTotalCapacity() + " người của phòng \"" + room.getName() + "\"");
                                return;
                            }
                        }
                        
                        newTable.setRoom(room);
                    } else {
                        writeJsonResponse(response, false, "Không tìm thấy phòng với ID: " + roomIdStr);
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    writeJsonResponse(response, false, "ID phòng không hợp lệ");
                    return;
                }
            }

            // Lưu bàn
            boolean success = roomTableService.addTable(newTable);
            
            if (success) {
                // Get the saved table ID
                String savedTableId = newTable.getTableId().toString();
                writeJsonResponse(response, true, "Thêm bàn thành công!", savedTableId);
                System.out.println("✅ Thêm bàn thành công: " + tableNumber + " (ID: " + savedTableId + ")");
            } else {
                writeJsonResponse(response, false, "Có lỗi xảy ra khi thêm bàn");
                System.out.println("❌ Lỗi khi thêm bàn: " + tableNumber);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong addTable: " + e.getMessage());
            e.printStackTrace();
            writeJsonResponse(response, false, "Có lỗi xảy ra khi thêm bàn: " + e.getMessage());
        }
    }
    
    private void updateTableStatus(HttpServletRequest request, HttpServletResponse response) {
        try {
            String tableIdStr = request.getParameter("tableId");
            String status = request.getParameter("status");

            System.out.println("=== DEBUG: Update Table Status ===");
            System.out.println("Table ID: " + tableIdStr);
            System.out.println("Status: " + status);

            if (tableIdStr == null || tableIdStr.trim().isEmpty()) {
                request.setAttribute("error", "ID bàn không được để trống");
                return;
            }

            if (status == null || status.trim().isEmpty()) {
                request.setAttribute("error", "Trạng thái không được để trống");
                return;
            }

            try {
                UUID tableId = UUID.fromString(tableIdStr.trim());
                boolean success = roomTableService.updateTableStatus(tableId, status.trim());
                
                if (success) {
                    request.setAttribute("success", "Cập nhật trạng thái bàn thành công!");
                    System.out.println("✅ Cập nhật trạng thái bàn thành công: " + tableId);
                } else {
                    request.setAttribute("error", "Có lỗi xảy ra khi cập nhật trạng thái bàn");
                    System.out.println("❌ Lỗi khi cập nhật trạng thái bàn: " + tableId);
                }
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", "ID bàn không hợp lệ");
                return;
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong updateTableStatus: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra khi cập nhật trạng thái bàn: " + e.getMessage());
        }
    }
    
    private void deleteTable(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String tableIdStr;
            
            // Check if request is JSON
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                // Parse JSON request using cached string
                System.out.println("JSON String: " + cachedJsonString);
                
                if (cachedJsonString == null || cachedJsonString.trim().isEmpty()) {
                    writeJsonResponse(response, false, "Dữ liệu JSON không hợp lệ");
                    return;
                }
                
                tableIdStr = extractJsonValue(cachedJsonString, "tableId");
                
                // Validate extracted values
                if (tableIdStr == null) {
                    writeJsonResponse(response, false, "Không tìm thấy tableId trong JSON");
                    return;
                }
            } else {
                // Parse form parameters
                tableIdStr = request.getParameter("tableId");
            }

            System.out.println("=== DEBUG: Delete Table ===");
            System.out.println("Table ID String: " + tableIdStr);
            System.out.println("Content Type: " + contentType);
            System.out.println("Cached JSON: " + cachedJsonString);

            // Validation
            if (tableIdStr == null || tableIdStr.trim().isEmpty()) {
                writeJsonResponse(response, false, "ID bàn không được để trống");
                return;
            }

            try {
                System.out.println("Attempting to parse UUID from: '" + tableIdStr.trim() + "'");
                UUID tableId = UUID.fromString(tableIdStr.trim());
                System.out.println("Successfully parsed UUID: " + tableId);
                
                System.out.println("Looking up table in database...");
                Table table = roomTableService.getTableById(tableId);
                
                if (table == null) {
                    System.out.println("❌ Table not found in database");
                    writeJsonResponse(response, false, "Không tìm thấy bàn với ID: " + tableId);
                    return;
                }
                
                System.out.println("=== DEBUG: Table to Delete ===");
                System.out.println("Table ID: " + table.getTableId());
                System.out.println("Table Number: " + table.getTableNumber());
                System.out.println("Table Name: " + table.getTableName());
                
                // Delete the table
                System.out.println("Calling roomTableService.deleteTable...");
                boolean success = roomTableService.deleteTable(tableId);
                System.out.println("Delete result from service: " + success);
                
                if (success) {
                    writeJsonResponse(response, true, "Xóa bàn thành công!");
                    System.out.println("✅ Xóa bàn thành công: " + tableId);
                } else {
                    writeJsonResponse(response, false, "Có lỗi xảy ra khi xóa bàn");
                    System.out.println("❌ Xóa bàn thất bại: " + tableId);
                }
                
            } catch (IllegalArgumentException e) {
                writeJsonResponse(response, false, "ID bàn không hợp lệ");
                return;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong deleteTable: " + e.getMessage());
            e.printStackTrace();
            
            // Sanitize error message to prevent JSON parsing errors
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                errorMessage = errorMessage.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "")
                                         .replace("\"", "\\\"")
                                         .replace("\n", " ")
                                         .replace("\r", " ")
                                         .replace("\t", " ")
                                         .trim();
            } else {
                errorMessage = "Unknown error";
            }
            
            writeJsonResponse(response, false, "Có lỗi xảy ra khi xóa bàn: " + errorMessage);
        }
    }
    
    private void editRoom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String roomIdStr, name, description, tableCountStr, totalCapacityStr;
            
            // Check if request is JSON
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                // Parse JSON request using cached string
                System.out.println("JSON String: " + cachedJsonString);
                
                if (cachedJsonString == null || cachedJsonString.trim().isEmpty()) {
                    writeJsonResponse(response, false, "Dữ liệu JSON không hợp lệ");
                    return;
                }
                
                roomIdStr = extractJsonValue(cachedJsonString, "roomId");
                name = extractJsonValue(cachedJsonString, "roomName");
                description = extractJsonValue(cachedJsonString, "roomDescription");
                tableCountStr = extractJsonValue(cachedJsonString, "roomTableCount");
                totalCapacityStr = extractJsonValue(cachedJsonString, "roomTotalCapacity");
                
                // Validate extracted values
                if (roomIdStr == null) {
                    writeJsonResponse(response, false, "Không tìm thấy roomId trong JSON");
                    return;
                }
            } else {
                // Parse form parameters
                roomIdStr = request.getParameter("roomId");
                name = request.getParameter("roomName");
                description = request.getParameter("roomDescription");
                tableCountStr = request.getParameter("roomTableCount");
                totalCapacityStr = request.getParameter("roomTotalCapacity");
            }

            System.out.println("=== DEBUG: Edit Room ===");
            System.out.println("Room ID: " + roomIdStr);
            System.out.println("Tên phòng: " + name);
            System.out.println("Mô tả: " + description);
            System.out.println("Số lượng bàn: " + tableCountStr);
            System.out.println("Tổng sức chứa: " + totalCapacityStr);

            // Validation
            if (roomIdStr == null || roomIdStr.trim().isEmpty()) {
                writeJsonResponse(response, false, "ID phòng không được để trống");
                return;
            }

            if (name == null || name.trim().isEmpty()) {
                writeJsonResponse(response, false, "Tên phòng không được để trống");
                return;
            }

            if (name.trim().length() > 100) {
                writeJsonResponse(response, false, "Tên phòng không được vượt quá 100 ký tự");
                return;
            }

            if (tableCountStr == null || tableCountStr.trim().isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"Số lượng bàn không được để trống\"}");
                return;
            }

            if (totalCapacityStr == null || totalCapacityStr.trim().isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"Tổng sức chứa không được để trống\"}");
                return;
            }

            int tableCount, totalCapacity;
            try {
                tableCount = Integer.parseInt(tableCountStr.trim());
                if (tableCount < 0 || tableCount > 50) {
                    response.getWriter().write("{\"success\": false, \"message\": \"Số lượng bàn phải từ 0 đến 50\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Số lượng bàn không hợp lệ\"}");
                return;
            }

            try {
                totalCapacity = Integer.parseInt(totalCapacityStr.trim());
                if (totalCapacity < 1 || totalCapacity > 1000) {
                    response.getWriter().write("{\"success\": false, \"message\": \"Tổng sức chứa phải từ 1 đến 1000\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Tổng sức chứa không hợp lệ\"}");
                return;
            }

            try {
                UUID roomId = UUID.fromString(roomIdStr.trim());
                Room room = roomTableService.getRoomById(roomId);
                
                if (room == null) {
                    response.getWriter().write("{\"success\": false, \"message\": \"Không tìm thấy phòng\"}");
                    return;
                }
                
                // Update room data
                room.setName(name.trim());
                room.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);
                room.setTableCount(tableCount);
                room.setTotalCapacity(totalCapacity);
                
                System.out.println("=== DEBUG: Updated Room Object ===");
                System.out.println("Room ID: " + room.getRoomId());
                System.out.println("Room Name: " + room.getName());
                System.out.println("Room Description: " + room.getDescription());
                System.out.println("Room TableCount: " + room.getTableCount());
                System.out.println("Room TotalCapacity: " + room.getTotalCapacity());
                
                boolean success = roomTableService.updateRoom(room);
                
                if (success) {
                    response.getWriter().write("{\"success\": true, \"message\": \"Cập nhật phòng thành công!\"}");
                    System.out.println("✅ Cập nhật phòng thành công: " + roomId);
                } else {
                    response.getWriter().write("{\"success\": false, \"message\": \"Có lỗi xảy ra khi cập nhật phòng\"}");
                    System.out.println("❌ Cập nhật phòng thất bại: " + roomId);
                }
                
            } catch (IllegalArgumentException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"ID phòng không hợp lệ\"}");
                return;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong editRoom: " + e.getMessage());
            e.printStackTrace();
            
            // Sanitize error message to prevent JSON parsing errors
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                errorMessage = errorMessage.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "")
                                         .replace("\"", "\\\"")
                                         .replace("\n", " ")
                                         .replace("\r", " ")
                                         .replace("\t", " ")
                                         .trim();
            } else {
                errorMessage = "Unknown error";
            }
            
            writeJsonResponse(response, false, "Có lỗi xảy ra khi cập nhật phòng: " + errorMessage);
        }
    }
    
    private void deleteRoom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String roomIdStr;
            
            // Check if request is JSON
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                // Parse JSON request using cached string
                System.out.println("JSON String: " + cachedJsonString);
                
                if (cachedJsonString == null || cachedJsonString.trim().isEmpty()) {
                    writeJsonResponse(response, false, "Dữ liệu JSON không hợp lệ");
                    return;
                }
                
                roomIdStr = extractJsonValue(cachedJsonString, "roomId");
                
                // Validate extracted values
                if (roomIdStr == null) {
                    writeJsonResponse(response, false, "Không tìm thấy roomId trong JSON");
                    return;
                }
            } else {
                // Parse form parameters
                roomIdStr = request.getParameter("roomId");
            }

            System.out.println("=== DEBUG: Delete Room ===");
            System.out.println("Room ID String: " + roomIdStr);
            System.out.println("Content Type: " + contentType);
            System.out.println("Cached JSON: " + cachedJsonString);

            // Validation
            if (roomIdStr == null || roomIdStr.trim().isEmpty()) {
                writeJsonResponse(response, false, "ID phòng không được để trống");
                return;
            }

            try {
                System.out.println("Attempting to parse UUID from: '" + roomIdStr.trim() + "'");
                UUID roomId = UUID.fromString(roomIdStr.trim());
                System.out.println("Successfully parsed UUID: " + roomId);
                
                System.out.println("Looking up room in database...");
                Room room = roomTableService.getRoomById(roomId);
                
                if (room == null) {
                    System.out.println("❌ Room not found in database");
                    System.out.println("🔍 Debug: Listing all rooms in database...");
                    try {
                        List<Room> allRooms = roomTableService.getAllRooms();
                        System.out.println("Total rooms in database: " + allRooms.size());
                        for (Room r : allRooms) {
                            System.out.println("  - Room ID: " + r.getRoomId() + ", Name: " + r.getName());
                        }
                    } catch (Exception e) {
                        System.out.println("Error listing rooms: " + e.getMessage());
                    }
                    writeJsonResponse(response, false, "Không tìm thấy phòng với ID: " + roomId);
                    return;
                }
                
                System.out.println("=== DEBUG: Room to Delete ===");
                System.out.println("Room ID: " + room.getRoomId());
                System.out.println("Room Name: " + room.getName());
                System.out.println("Room Description: " + room.getDescription());
                
                // Delete the room (this will also delete associated tables due to cascade)
                System.out.println("Calling roomTableService.deleteRoom...");
                boolean success = roomTableService.deleteRoom(roomId);
                System.out.println("Delete result from service: " + success);
                
                if (success) {
                    writeJsonResponse(response, true, "Xóa phòng thành công!");
                    System.out.println("✅ Xóa phòng thành công: " + roomId);
                } else {
                    writeJsonResponse(response, false, "Có lỗi xảy ra khi xóa phòng");
                    System.out.println("❌ Xóa phòng thất bại: " + roomId);
                }
                
            } catch (IllegalArgumentException e) {
                writeJsonResponse(response, false, "ID phòng không hợp lệ");
                return;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong deleteRoom: " + e.getMessage());
            e.printStackTrace();
            
            // Sanitize error message to prevent JSON parsing errors
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                errorMessage = errorMessage.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "")
                                         .replace("\"", "\\\"")
                                         .replace("\n", " ")
                                         .replace("\r", " ")
                                         .replace("\t", " ")
                                         .trim();
            } else {
                errorMessage = "Unknown error";
            }
            
            writeJsonResponse(response, false, "Có lỗi xảy ra khi xóa phòng: " + errorMessage);
        }
    }
    
    private void editTable(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String tableIdStr, tableNumber, tableName, capacityStr, roomIdStr, status;
            
            // Check if request is JSON
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                // Parse JSON request using cached string
                System.out.println("JSON String: " + cachedJsonString);
                
                if (cachedJsonString == null || cachedJsonString.trim().isEmpty()) {
                    writeJsonResponse(response, false, "Dữ liệu JSON không hợp lệ");
                    return;
                }
                
                tableIdStr = extractJsonValue(cachedJsonString, "tableId");
                tableNumber = extractJsonValue(cachedJsonString, "tableNumber");
                tableName = extractJsonValue(cachedJsonString, "tableName");
                capacityStr = extractJsonValue(cachedJsonString, "capacity");
                roomIdStr = extractJsonValue(cachedJsonString, "roomId");
                status = extractJsonValue(cachedJsonString, "status");
                
                // Validate extracted values
                if (tableIdStr == null) {
                    writeJsonResponse(response, false, "Không tìm thấy tableId trong JSON");
                    return;
                }
            } else {
                // Parse form parameters
                tableIdStr = request.getParameter("tableId");
                tableNumber = request.getParameter("tableNumber");
                tableName = request.getParameter("tableName");
                capacityStr = request.getParameter("capacity");
                roomIdStr = request.getParameter("roomId");
                status = request.getParameter("status");
            }

            System.out.println("=== DEBUG: Edit Table ===");
            System.out.println("Table ID: " + tableIdStr);
            System.out.println("Table Number: " + tableNumber);
            System.out.println("Table Name: " + tableName);
            System.out.println("Capacity: " + capacityStr);
            System.out.println("Room ID: " + roomIdStr);
            System.out.println("Status: " + status);

            // Validation
            if (tableIdStr == null || tableIdStr.trim().isEmpty()) {
                writeJsonResponse(response, false, "ID bàn không được để trống");
                return;
            }

            if (tableNumber == null || tableNumber.trim().isEmpty()) {
                writeJsonResponse(response, false, "Số bàn không được để trống");
                return;
            }

            if (tableName == null || tableName.trim().isEmpty()) {
                writeJsonResponse(response, false, "Tên bàn không được để trống");
                return;
            }

            if (tableNumber.trim().length() > 50) {
                writeJsonResponse(response, false, "Số bàn không được vượt quá 50 ký tự");
                return;
            }

            if (tableName.trim().length() > 100) {
                writeJsonResponse(response, false, "Tên bàn không được vượt quá 100 ký tự");
                return;
            }

            int capacity;
            if (capacityStr != null && !capacityStr.trim().isEmpty()) {
                try {
                    capacity = Integer.parseInt(capacityStr.trim());
                    if (capacity < 1 || capacity > 20) {
                        writeJsonResponse(response, false, "Sức chứa phải từ 1 đến 20 người");
                        return;
                    }
                } catch (NumberFormatException e) {
                    writeJsonResponse(response, false, "Sức chứa không hợp lệ");
                    return;
                }
            } else {
                capacity = 4; // Default capacity
            }

            try {
                UUID tableId = UUID.fromString(tableIdStr.trim());
                Table table = roomTableService.getTableById(tableId);
                
                if (table == null) {
                    writeJsonResponse(response, false, "Không tìm thấy bàn");
                    return;
                }
                
                // Update table data
                table.setTableNumber(tableNumber.trim());
                table.setTableName(tableName.trim());
                table.setCapacity(capacity);
                
                // Set room if provided
                if (roomIdStr != null && !roomIdStr.trim().isEmpty()) {
                    try {
                        UUID roomId = UUID.fromString(roomIdStr.trim());
                        Room room = roomTableService.getRoomById(roomId);
                        table.setRoom(room);
                    } catch (IllegalArgumentException e) {
                        writeJsonResponse(response, false, "ID phòng không hợp lệ");
                        return;
                    }
                } else {
                    table.setRoom(null);
                }
                
                // Set status if provided
                if (status != null && !status.trim().isEmpty()) {
                    table.setStatus(status.trim());
                }
                
                System.out.println("=== DEBUG: Updated Table Object ===");
                System.out.println("Table ID: " + table.getTableId());
                System.out.println("Table Number: " + table.getTableNumber());
                System.out.println("Table Name: " + table.getTableName());
                System.out.println("Table Capacity: " + table.getCapacity());
                System.out.println("Table Status: " + table.getStatus());
                System.out.println("Table Room: " + (table.getRoom() != null ? table.getRoom().getName() : "null"));
                
                boolean success = roomTableService.updateTable(table);
                
                if (success) {
                    writeJsonResponse(response, true, "Cập nhật bàn thành công!");
                    System.out.println("✅ Cập nhật bàn thành công: " + tableId);
                } else {
                    writeJsonResponse(response, false, "Có lỗi xảy ra khi cập nhật bàn");
                    System.out.println("❌ Cập nhật bàn thất bại: " + tableId);
                }
                
            } catch (IllegalArgumentException e) {
                writeJsonResponse(response, false, "ID bàn không hợp lệ");
                return;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong editTable: " + e.getMessage());
            e.printStackTrace();
            
            // Sanitize error message to prevent JSON parsing errors
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                errorMessage = errorMessage.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "")
                                         .replace("\"", "\\\"")
                                         .replace("\n", " ")
                                         .replace("\r", " ")
                                         .replace("\t", " ")
                                         .trim();
            } else {
                errorMessage = "Unknown error";
            }
            
            writeJsonResponse(response, false, "Có lỗi xảy ra khi cập nhật bàn: " + errorMessage);
        }
    }
    
    private void getTableDetails(HttpServletRequest request, HttpServletResponse response) {
        try {
            String tableIdStr = request.getParameter("tableId");
            
            if (tableIdStr == null || tableIdStr.trim().isEmpty()) {
                response.getWriter().write("{\"error\": \"ID bàn không được để trống\"}");
                return;
            }

            try {
                UUID tableId = UUID.fromString(tableIdStr.trim());
                Table table = roomTableService.getTableById(tableId);
                
                if (table != null) {
                    // Get active session for this table
                    com.liteflow.modules.inventory.model.TableSession activeSession = 
                        roomTableService.getActiveSessionByTableId(tableId);
                    
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    StringBuilder json = new StringBuilder();
                    json.append("{");
                    json.append("\"tableId\":\"").append(table.getTableId()).append("\",");
                    json.append("\"tableNumber\":\"").append(table.getTableNumber()).append("\",");
                    json.append("\"tableName\":\"").append(table.getTableName()).append("\",");
                    json.append("\"capacity\":").append(table.getCapacity()).append(",");
                    json.append("\"status\":\"").append(table.getStatus()).append("\",");
                    json.append("\"isActive\":").append(table.getIsActive()).append(",");
                    json.append("\"room\":");
                    if (table.getRoom() != null) {
                        json.append("{");
                        json.append("\"roomId\":\"").append(table.getRoom().getRoomId()).append("\",");
                        json.append("\"name\":\"").append(table.getRoom().getName()).append("\"");
                        json.append("}");
                    } else {
                        json.append("null");
                    }
                    json.append(",");
                    json.append("\"activeSession\":");
                    if (activeSession != null) {
                        json.append("{");
                        json.append("\"sessionId\":\"").append(activeSession.getSessionId()).append("\",");
                        json.append("\"customerName\":\"").append(activeSession.getCustomerName() != null ? activeSession.getCustomerName() : "").append("\",");
                        json.append("\"customerPhone\":\"").append(activeSession.getCustomerPhone() != null ? activeSession.getCustomerPhone() : "").append("\",");
                        json.append("\"checkInTime\":\"").append(activeSession.getCheckInTime()).append("\",");
                        json.append("\"totalAmount\":").append(activeSession.getTotalAmount()).append(",");
                        json.append("\"paymentStatus\":\"").append(activeSession.getPaymentStatus()).append("\"");
                        json.append("}");
                    } else {
                        json.append("null");
                    }
                    json.append("}");
                    
                    response.getWriter().write(json.toString());
                } else {
                    response.getWriter().write("{\"error\": \"Không tìm thấy bàn\"}");
                }
            } catch (IllegalArgumentException e) {
                response.getWriter().write("{\"error\": \"ID bàn không hợp lệ\"}");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong getTableDetails: " + e.getMessage());
            e.printStackTrace();
            try {
                response.getWriter().write("{\"error\": \"Có lỗi xảy ra: " + e.getMessage() + "\"}");
            } catch (Exception ex) {
                // Ignore
            }
        }
    }
    
    private void getTableHistory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Use cached JSON string to avoid "Stream closed" error
            if (cachedJsonString == null) {
                cachedJsonString = getJsonString(request);
            }
            
            JSONObject jsonRequest = new JSONObject(cachedJsonString);
            String tableIdStr = jsonRequest.optString("tableId", "");
            
            if (tableIdStr == null || tableIdStr.trim().isEmpty()) {
                writeJsonResponse(response, false, "ID bàn không hợp lệ");
                return;
            }
            
            UUID tableId;
            try {
                tableId = UUID.fromString(tableIdStr);
            } catch (IllegalArgumentException e) {
                writeJsonResponse(response, false, "ID bàn không hợp lệ");
                return;
            }
            
            // Get table info
            Table table = roomTableService.getTableById(tableId);
            if (table == null) {
                writeJsonResponse(response, false, "Không tìm thấy bàn");
                return;
            }
            
            // Get completed table sessions (like cashier notification history)
            List<TableSession> sessions = roomTableService.getCompletedTableSessions(tableId);
            System.out.println("=== DEBUG: Total completed sessions found: " + sessions.size());
            
            // Build response
            JSONObject responseData = new JSONObject();
            responseData.put("success", true);
            
            // Table info
            JSONObject tableInfo = new JSONObject();
            tableInfo.put("tableId", table.getTableId().toString());
            tableInfo.put("tableNumber", table.getTableNumber());
            tableInfo.put("tableName", table.getTableName());
            tableInfo.put("capacity", table.getCapacity());
            tableInfo.put("status", table.getStatus());
            if (table.getRoom() != null) {
                tableInfo.put("roomName", table.getRoom().getName());
            }
            responseData.put("tableInfo", tableInfo);
            
            // Build invoices array from completed sessions
            JSONArray invoicesArray = new JSONArray();
            int invoiceCount = 0;
            for (TableSession session : sessions) {
                System.out.println("Processing session: " + session.getSessionId() + ", status: " + session.getStatus() + ", paymentStatus: " + session.getPaymentStatus());
                invoiceCount++;
                
                JSONObject invoiceObj = new JSONObject();
                invoiceObj.put("transactionId", session.getSessionId().toString()); // Use sessionId as transactionId
                
                // Invoice name
                String invoiceName = session.getInvoiceName();
                if (invoiceName == null || invoiceName.trim().isEmpty()) {
                    invoiceName = table.getTableName();
                }
                invoiceObj.put("invoiceName", invoiceName);
                invoiceObj.put("tableName", table.getTableName());
                
                // Amount details
                java.math.BigDecimal totalAmount = session.getTotalAmount();
                java.math.BigDecimal finalAmount = totalAmount;
                java.math.BigDecimal discount = java.math.BigDecimal.ZERO; // No discount info from session
                
                invoiceObj.put("amount", totalAmount != null ? totalAmount.doubleValue() : 0.0);
                invoiceObj.put("finalAmount", finalAmount != null ? finalAmount.doubleValue() : 0.0);
                invoiceObj.put("discount", discount.doubleValue());
                invoiceObj.put("hasVoucher", false);
                
                // Payment details
                invoiceObj.put("paymentMethod", session.getPaymentMethod() != null ? session.getPaymentMethod() : "cash");
                
                // Format checkOutTime (payment time) for frontend
                java.time.LocalDateTime checkOutTime = session.getCheckOutTime();
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                invoiceObj.put("processedAt", checkOutTime != null ? checkOutTime.format(formatter) : "");
                
                // Staff info from session.createdBy
                if (session.getCreatedBy() != null) {
                    invoiceObj.put("processedByName", session.getCreatedBy().getDisplayName());
                } else {
                    invoiceObj.put("processedByName", "N/A");
                }
                
                // Get order items for this session
                List<com.liteflow.modules.inventory.model.OrderDetail> orderDetails = roomTableService.getOrderDetailsForSession(session.getSessionId());
                invoiceObj.put("itemCount", orderDetails.size());
                
                // Build items array
                JSONArray itemsArray = new JSONArray();
                for (com.liteflow.modules.inventory.model.OrderDetail detail : orderDetails) {
                    JSONObject itemObj = new JSONObject();
                    if (detail.getProductVariant() != null) {
                        itemObj.put("name", detail.getProductVariant().getProduct().getName());
                        itemObj.put("size", detail.getProductVariant().getSize());
                    } else {
                        itemObj.put("name", "N/A");
                        itemObj.put("size", "");
                    }
                    itemObj.put("quantity", detail.getQuantity());
                    itemObj.put("price", detail.getUnitPrice().doubleValue());
                    itemsArray.put(itemObj);
                }
                invoiceObj.put("items", itemsArray);
                
                // Notes from session
                invoiceObj.put("notes", session.getNotes() != null ? session.getNotes() : "");
                
                invoicesArray.put(invoiceObj);
                System.out.println("  -> Added invoice #" + invoiceCount + " to array");
            }
            System.out.println("=== DEBUG: Total invoices processed: " + invoiceCount);
            System.out.println("=== DEBUG: Total invoices in array: " + invoicesArray.length());
            responseData.put("invoices", invoicesArray);
            
            response.getWriter().write(responseData.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Error getting table history: " + e.getMessage());
            e.printStackTrace();
            writeJsonResponse(response, false, "Lỗi khi lấy lịch sử bàn: " + e.getMessage());
        }
    }

    private void getAllTables(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Table> tables = roomTableService.getAllTables();
            
            // Convert to JSON
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < tables.size(); i++) {
                Table table = tables.get(i);
                json.append("{");
                json.append("\"tableId\":\"").append(table.getTableId()).append("\",");
                json.append("\"tableNumber\":\"").append(table.getTableNumber()).append("\",");
                json.append("\"tableName\":\"").append(table.getTableName()).append("\",");
                json.append("\"capacity\":").append(table.getCapacity()).append(",");
                json.append("\"status\":\"").append(table.getStatus()).append("\",");
                json.append("\"createdAt\":\"").append(table.getCreatedAt() != null ? table.getCreatedAt().toString() : "").append("\",");
                if (table.getRoom() != null) {
                    json.append("\"room\":{");
                    json.append("\"roomId\":\"").append(table.getRoom().getRoomId()).append("\",");
                    json.append("\"name\":\"").append(table.getRoom().getName()).append("\"");
                    json.append("}");
                } else {
                    json.append("\"room\":null");
                }
                json.append("}");
                if (i < tables.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            
            response.getWriter().write(json.toString());
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong getAllTables: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("[]");
        }
    }
    
    private void getAllRooms(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Room> rooms = roomTableService.getAllRooms();
            
            // Convert to JSON
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < rooms.size(); i++) {
                Room room = rooms.get(i);
                json.append("{");
                json.append("\"roomId\":\"").append(room.getRoomId()).append("\",");
                json.append("\"name\":\"").append(room.getName()).append("\",");
                json.append("\"description\":\"").append(room.getDescription()).append("\",");
                json.append("\"tableCount\":").append(room.getTableCount()).append(",");
                json.append("\"totalCapacity\":").append(room.getTotalCapacity()).append(",");
                json.append("\"createdAt\":\"").append(room.getCreatedAt() != null ? room.getCreatedAt().toString() : "").append("\"");
                json.append("}");
                if (i < rooms.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            
            response.getWriter().write(json.toString());
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong getAllRooms: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("[]");
        }
    }
    
    /**
     * Import Excel file
     */
    private void importExcel(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            System.out.println("=== DEBUG: Import Excel ===");
            
            // Get multipart data
            Part filePart = request.getPart("file");
            if (filePart == null) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không tìm thấy file\"}");
                return;
            }
            
            String fileName = filePart.getSubmittedFileName();
            System.out.println("File name: " + fileName);
            
            // Get options
            boolean skipDuplicates = "true".equals(request.getParameter("skipDuplicates"));
            boolean validateData = "true".equals(request.getParameter("validateData"));
            boolean createMissingRooms = "true".equals(request.getParameter("createMissingRooms"));
            
            System.out.println("Options - skipDuplicates: " + skipDuplicates + 
                             ", validateData: " + validateData + 
                             ", createMissingRooms: " + createMissingRooms);
            
            // Process file
            try (InputStream inputStream = filePart.getInputStream()) {
                Map<String, Object> result = excelService.importFromExcel(
                    inputStream, fileName, skipDuplicates, validateData, createMissingRooms);
                
                // Convert result to JSON
                JSONObject jsonResult = new JSONObject(result);
                response.getWriter().write(jsonResult.toString());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi import Excel: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi xử lý file: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Export to Excel
     */
    private void exportExcel(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            System.out.println("=== DEBUG: Export Excel ===");
            
            // Generate Excel file
            byte[] excelData = excelService.exportToExcel();
            
            if (excelData.length == 0) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không có dữ liệu để xuất\"}");
                return;
            }
            
            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"roomtable_data.xlsx\"");
            response.setContentLength(excelData.length);
            
            // Write Excel data to response
            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(excelData);
                outputStream.flush();
            }
            
            System.out.println("✅ Excel export completed successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi export Excel: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi xuất file: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Check Excel File
     */
    private void checkExcel(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            System.out.println("=== DEBUG: Check Excel ===");
            
            // Get uploaded file
            Part filePart = request.getPart("excelFile");
            if (filePart == null || filePart.getSize() == 0) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không có file được upload\"}");
                return;
            }
            
            // Get import options
            boolean skipDuplicates = "true".equals(request.getParameter("skipDuplicates"));
            boolean validateData = "true".equals(request.getParameter("validateData"));
            boolean createMissingRooms = "true".equals(request.getParameter("createMissingRooms"));
            
            System.out.println("Skip duplicates: " + skipDuplicates);
            System.out.println("Validate data: " + validateData);
            System.out.println("Create missing rooms: " + createMissingRooms);
            
            // Check Excel file
            Map<String, Object> checkResult = excelService.checkExcelFile(
                filePart.getInputStream(), 
                filePart.getSubmittedFileName(),
                skipDuplicates, 
                validateData, 
                createMissingRooms
            );
            
            // Convert result to JSON
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");
            jsonResponse.append("\"success\": ").append(checkResult.get("success"));
            jsonResponse.append(", \"totalRooms\": ").append(checkResult.get("totalRooms"));
            jsonResponse.append(", \"totalTables\": ").append(checkResult.get("totalTables"));
            jsonResponse.append(", \"errors\": [");
            
        
            List<String> errors = (List<String>) checkResult.get("errors");
            if (errors != null && !errors.isEmpty()) {
                for (int i = 0; i < errors.size(); i++) {
                    if (i > 0) jsonResponse.append(", ");
                    jsonResponse.append("\"").append(errors.get(i).replace("\"", "\\\"")).append("\"");
                }
            }
            
            jsonResponse.append("]");
            jsonResponse.append("}");
            
            response.getWriter().write(jsonResponse.toString());
            System.out.println("✅ Check Excel completed successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi check Excel: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi kiểm tra file: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Download Template
     */
    private void downloadTemplate(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            System.out.println("=== DEBUG: Download Template ===");
            
            String templateType = request.getParameter("templateType");
            System.out.println("Template type: " + templateType);
            
            // Generate template Excel file
            byte[] templateData = excelService.generateTemplate(templateType);
            
            if (templateData.length == 0) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không thể tạo template\"}");
                return;
            }
            
            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "rooms".equals(templateType) ? "mau_phong.xlsx" : "mau_ban.xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLength(templateData.length);
            
            // Write template data to response
            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(templateData);
                outputStream.flush();
            }
            
            System.out.println("✅ Template download completed successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi download template: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi tải template: " + e.getMessage() + "\"}");
        }
    }
    
}
