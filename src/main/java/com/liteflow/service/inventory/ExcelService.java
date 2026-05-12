package com.liteflow.service.inventory;

import com.liteflow.model.inventory.Room;
import com.liteflow.model.inventory.Table;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for handling Excel import/export operations
 */
public class ExcelService {
    
    private RoomTableService roomTableService;
    
    public ExcelService() {
        this.roomTableService = new RoomTableService();
    }
    
    /**
     * Check Excel file for validation without importing
     */
    public Map<String, Object> checkExcelFile(InputStream inputStream, String fileName, 
                                              boolean skipDuplicates, boolean validateData, 
                                              boolean createMissingRooms) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int totalRooms = 0;
        int totalTables = 0;
        
        try {
            Workbook workbook;
            if (fileName.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            
            // Check which sheets exist
            boolean hasRoomsSheet = workbook.getSheet("Rooms") != null;
            boolean hasTablesSheet = workbook.getSheet("Tables") != null;
            
            if (!hasRoomsSheet && !hasTablesSheet) {
                workbook.close();
                result.put("success", false);
                result.put("message", "File Excel không chứa sheet 'Rooms' hoặc 'Tables'");
                result.put("totalRooms", 0);
                result.put("totalTables", 0);
                result.put("errors", Arrays.asList("File Excel phải chứa ít nhất một sheet 'Rooms' hoặc 'Tables'"));
                return result;
            }
            
            // Check Rooms sheet if exists
            if (hasRoomsSheet) {
                Sheet roomsSheet = workbook.getSheet("Rooms");
                for (int i = 1; i <= roomsSheet.getLastRowNum(); i++) {
                    Row row = roomsSheet.getRow(i);
                    if (row == null) continue;
                    
                    try {
                        Room room = parseRoomFromRow(row, validateData);
                        if (room != null) {
                            // Check for duplicates
                            if (!skipDuplicates && roomTableService.getRoomByName(room.getName()) != null) {
                                errors.add("Dòng " + (i + 1) + " (Rooms): Phòng '" + room.getName() + "' đã tồn tại");
                            } else {
                                totalRooms++;
                            }
                        }
                    } catch (Exception e) {
                        errors.add("Dòng " + (i + 1) + " (Rooms): " + e.getMessage());
                    }
                }
            }
            
            // Check Tables sheet if exists
            if (hasTablesSheet) {
                Sheet tablesSheet = workbook.getSheet("Tables");
                for (int i = 1; i <= tablesSheet.getLastRowNum(); i++) {
                    Row row = tablesSheet.getRow(i);
                    if (row == null) continue;
                    
                    try {
                        Table table = parseTableFromRow(row, validateData, createMissingRooms);
                        if (table != null) {
                            // Check for duplicates
                            if (!skipDuplicates && roomTableService.getTableByNumber(table.getTableNumber()) != null) {
                                errors.add("Dòng " + (i + 1) + " (Tables): Bàn '" + table.getTableNumber() + "' đã tồn tại");
                            } else {
                                totalTables++;
                            }
                        }
                    } catch (Exception e) {
                        errors.add("Dòng " + (i + 1) + " (Tables): " + e.getMessage());
                    }
                }
            }
            
            workbook.close();
            
            result.put("success", true);
            result.put("totalRooms", totalRooms);
            result.put("totalTables", totalTables);
            result.put("errors", errors);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi khi đọc file Excel: " + e.getMessage());
            result.put("errors", errors);
        }
        
        return result;
    }
    
    /**
     * Import rooms and tables from Excel file
     */
    public Map<String, Object> importFromExcel(InputStream inputStream, String fileName, 
                                             boolean skipDuplicates, boolean validateData, 
                                             boolean createMissingRooms) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        try {
            Workbook workbook = createWorkbook(inputStream, fileName);
            
            // Check which sheets exist
            boolean hasRoomsSheet = workbook.getSheet("Rooms") != null;
            boolean hasTablesSheet = workbook.getSheet("Tables") != null;
            
            if (!hasRoomsSheet && !hasTablesSheet) {
                result.put("success", false);
                result.put("message", "File Excel không chứa sheet 'Rooms' hoặc 'Tables'");
                result.put("errors", Arrays.asList("File Excel phải chứa ít nhất một sheet 'Rooms' hoặc 'Tables'"));
                return result;
            }
            
            int totalSuccess = 0;
            int totalErrors = 0;
            
            // Import rooms if sheet exists
            if (hasRoomsSheet) {
                Map<String, Object> roomsResult = importRooms(workbook, skipDuplicates, validateData);
                result.put("rooms", roomsResult);
                totalSuccess += (Integer) roomsResult.get("success");
                totalErrors += (Integer) roomsResult.get("error");
            } else {
                result.put("rooms", Map.of("success", 0, "error", 0, "message", "Không có sheet 'Rooms'"));
            }
            
            // Import tables if sheet exists
            if (hasTablesSheet) {
                Map<String, Object> tablesResult = importTables(workbook, skipDuplicates, validateData, createMissingRooms);
                result.put("tables", tablesResult);
                totalSuccess += (Integer) tablesResult.get("success");
                totalErrors += (Integer) tablesResult.get("error");
            } else {
                result.put("tables", Map.of("success", 0, "error", 0, "message", "Không có sheet 'Tables'"));
            }
            
            workbook.close();
            
            result.put("success", totalErrors == 0);
            result.put("message", String.format("Nhập thành công %d bản ghi, %d lỗi", totalSuccess, totalErrors));
            result.put("errors", errors);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi import Excel: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Lỗi khi xử lý file Excel: " + e.getMessage());
            result.put("errors", Arrays.asList(e.getMessage()));
        }
        
        return result;
    }
    
    /**
     * Export rooms and tables to Excel file
     */
    public byte[] exportToExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            
            // Create styles
            Map<String, CellStyle> styles = createStyles(workbook);
            
            // Export rooms
            exportRooms(workbook, styles);
            
            // Export tables
            exportTables(workbook, styles);
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi export Excel: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    /**
     * Create workbook from input stream
     */
    private Workbook createWorkbook(InputStream inputStream, String fileName) throws IOException {
        if (fileName.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (fileName.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Only .xlsx and .xls are supported.");
        }
    }
    
    /**
     * Import rooms from Excel
     */
    private Map<String, Object> importRooms(Workbook workbook, boolean skipDuplicates, boolean validateData) {
        Map<String, Object> result = new HashMap<>();
        int success = 0;
        int error = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            Sheet roomsSheet = workbook.getSheet("Rooms");
            if (roomsSheet == null) {
                result.put("success", 0);
                result.put("error", 0);
                result.put("message", "Không tìm thấy sheet 'Rooms'");
                return result;
            }
            
            // Skip header row
            for (int i = 1; i <= roomsSheet.getLastRowNum(); i++) {
                Row row = roomsSheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Room room = parseRoomFromRow(row, validateData);
                    if (room != null) {
                        // Check for duplicates if required
                        if (skipDuplicates && roomExists(room.getName())) {
                            continue;
                        }
                        
                        if (roomTableService.addRoom(room)) {
                            success++;
                        } else {
                            error++;
                            errors.add("Không thể thêm phòng: " + room.getName());
                        }
                    }
                } catch (Exception e) {
                    error++;
                    errors.add("Lỗi dòng " + (i + 1) + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            error++;
            errors.add("Lỗi khi đọc sheet Rooms: " + e.getMessage());
        }
        
        result.put("success", success);
        result.put("error", error);
        result.put("message", String.format("Phòng: %d thành công, %d lỗi", success, error));
        return result;
    }
    
    /**
     * Import tables from Excel
     */
    private Map<String, Object> importTables(Workbook workbook, boolean skipDuplicates, boolean validateData, boolean createMissingRooms) {
        Map<String, Object> result = new HashMap<>();
        int success = 0;
        int error = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            Sheet tablesSheet = workbook.getSheet("Tables");
            if (tablesSheet == null) {
                result.put("success", 0);
                result.put("error", 0);
                result.put("message", "Không tìm thấy sheet 'Tables'");
                return result;
            }
            
            // Skip header row
            for (int i = 1; i <= tablesSheet.getLastRowNum(); i++) {
                Row row = tablesSheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Table table = parseTableFromRow(row, validateData, createMissingRooms);
                    if (table != null) {
                        // Check for duplicates if required
                        if (skipDuplicates && tableExists(table.getTableNumber())) {
                            continue;
                        }
                        
                        if (roomTableService.addTable(table)) {
                            success++;
                        } else {
                            error++;
                            errors.add("Không thể thêm bàn: " + table.getTableNumber());
                        }
                    }
                } catch (Exception e) {
                    error++;
                    errors.add("Lỗi dòng " + (i + 1) + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            error++;
            errors.add("Lỗi khi đọc sheet Tables: " + e.getMessage());
        }
        
        result.put("success", success);
        result.put("error", error);
        result.put("message", String.format("Bàn: %d thành công, %d lỗi", success, error));
        return result;
    }
    
    /**
     * Parse room from Excel row
     */
    private Room parseRoomFromRow(Row row, boolean validateData) {
        Room room = new Room();
        
        // Column A: Room Name (required)
        Cell nameCell = row.getCell(0);
        if (nameCell == null || getCellValueAsString(nameCell).trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng không được để trống");
        }
        room.setName(getCellValueAsString(nameCell).trim());
        
        // Column B: Description (optional)
        Cell descCell = row.getCell(1);
        if (descCell != null) {
            room.setDescription(getCellValueAsString(descCell).trim());
        }
        
        // Column C: Table Count (required)
        Cell tableCountCell = row.getCell(2);
        if (tableCountCell == null) {
            throw new IllegalArgumentException("Số lượng bàn không được để trống");
        }
        int tableCount = getCellValueAsInt(tableCountCell);
        if (validateData && (tableCount < 1 || tableCount > 100)) {
            throw new IllegalArgumentException("Số lượng bàn phải từ 1-100");
        }
        room.setTableCount(tableCount);
        
        // Column D: Total Capacity (required)
        Cell capacityCell = row.getCell(3);
        if (capacityCell == null) {
            throw new IllegalArgumentException("Tổng sức chứa không được để trống");
        }
        int capacity = getCellValueAsInt(capacityCell);
        if (validateData && (capacity < 1 || capacity > 1000)) {
            throw new IllegalArgumentException("Tổng sức chứa phải từ 1-1000");
        }
        room.setTotalCapacity(capacity);
        
        return room;
    }
    
    /**
     * Parse table from Excel row
     */
    private Table parseTableFromRow(Row row, boolean validateData, boolean createMissingRooms) {
        Table table = new Table();
        
        // Column A: Table Number (required)
        Cell numberCell = row.getCell(0);
        if (numberCell == null || getCellValueAsString(numberCell).trim().isEmpty()) {
            throw new IllegalArgumentException("Số bàn không được để trống");
        }
        table.setTableNumber(getCellValueAsString(numberCell).trim());
        
        // Column B: Table Name (required)
        Cell nameCell = row.getCell(1);
        if (nameCell == null || getCellValueAsString(nameCell).trim().isEmpty()) {
            throw new IllegalArgumentException("Tên bàn không được để trống");
        }
        table.setTableName(getCellValueAsString(nameCell).trim());
        
        // Column C: Room Name (optional)
        Cell roomCell = row.getCell(2);
        if (roomCell != null) {
            String roomName = getCellValueAsString(roomCell).trim();
            if (!roomName.isEmpty()) {
                Room room = findRoomByName(roomName);
                if (room == null && createMissingRooms) {
                    // Create missing room
                    room = new Room();
                    room.setName(roomName);
                    room.setTableCount(10); // Default
                    room.setTotalCapacity(40); // Default
                    roomTableService.addRoom(room);
                    room = findRoomByName(roomName); // Get the created room
                }
                table.setRoom(room);
            }
        }
        
        // Column D: Capacity (required)
        Cell capacityCell = row.getCell(3);
        if (capacityCell == null) {
            throw new IllegalArgumentException("Sức chứa không được để trống");
        }
        int capacity = getCellValueAsInt(capacityCell);
        if (validateData && (capacity < 1 || capacity > 20)) {
            throw new IllegalArgumentException("Sức chứa phải từ 1-20");
        }
        table.setCapacity(capacity);
        
        // Trạng thái sẽ được tự động thiết lập là "Available" trong @PrePersist
        
        return table;
    }
    
    /**
     * Export rooms to Excel
     */
    private void exportRooms(Workbook workbook, Map<String, CellStyle> styles) {
        Sheet sheet = workbook.createSheet("Rooms");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Tên phòng", "Mô tả", "Số lượng bàn", "Tổng sức chứa", "Ngày tạo"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Get rooms data
        List<Room> rooms = roomTableService.getAllRooms();
        if (rooms == null) rooms = new ArrayList<>();
        
        // Create data rows
        int rowNum = 1;
        for (Room room : rooms) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(room.getName());
            row.createCell(1).setCellValue(room.getDescription() != null ? room.getDescription() : "");
            row.createCell(2).setCellValue(room.getTableCount() != null ? room.getTableCount() : 0);
            row.createCell(3).setCellValue(room.getTotalCapacity() != null ? room.getTotalCapacity() : 0);
            row.createCell(4).setCellValue(formatDateTime(room.getCreatedAt()));
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    /**
     * Export tables to Excel
     */
    private void exportTables(Workbook workbook, Map<String, CellStyle> styles) {
        Sheet sheet = workbook.createSheet("Tables");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Số bàn", "Tên bàn", "Phòng", "Sức chứa", "Trạng thái", "Ngày tạo"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Get tables data
        List<Table> tables = roomTableService.getAllTables();
        if (tables == null) tables = new ArrayList<>();
        
        // Create data rows
        int rowNum = 1;
        for (Table table : tables) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(table.getTableNumber());
            row.createCell(1).setCellValue(table.getTableName());
            row.createCell(2).setCellValue(table.getRoom() != null ? table.getRoom().getName() : "");
            row.createCell(3).setCellValue(table.getCapacity() != null ? table.getCapacity() : 4);
            row.createCell(4).setCellValue(table.getStatus() != null ? table.getStatus() : "Available");
            row.createCell(5).setCellValue(formatDateTime(table.getCreatedAt()));
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    /**
     * Create styles for Excel
     */
    private Map<String, CellStyle> createStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();
        
        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        styles.put("header", headerStyle);
        
        return styles;
    }
    
    /**
     * Helper methods
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    private int getCellValueAsInt(Cell cell) {
        if (cell == null) return 0;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Giá trị không phải là số: " + cell.getStringCellValue());
                }
            default:
                throw new IllegalArgumentException("Không thể chuyển đổi thành số");
        }
    }
    
    private boolean roomExists(String roomName) {
        List<Room> rooms = roomTableService.getAllRooms();
        if (rooms == null) return false;
        
        return rooms.stream().anyMatch(room -> room.getName().equalsIgnoreCase(roomName));
    }
    
    private boolean tableExists(String tableNumber) {
        List<Table> tables = roomTableService.getAllTables();
        if (tables == null) return false;
        
        return tables.stream().anyMatch(table -> table.getTableNumber().equalsIgnoreCase(tableNumber));
    }
    
    private Room findRoomByName(String roomName) {
        List<Room> rooms = roomTableService.getAllRooms();
        if (rooms == null) return null;
        
        return rooms.stream()
                .filter(room -> room.getName().equalsIgnoreCase(roomName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Format LocalDateTime to dd/MM/yyyy HH:mm format
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Generate template Excel file for rooms or tables
     *
     * @param templateType The type of template ("rooms" or "tables")
     * @return A byte array containing the template Excel file data.
     * @throws IOException If an I/O error occurs.
     */
    public byte[] generateTemplate(String templateType) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             Workbook workbook = new XSSFWorkbook()) {
            
            if ("rooms".equals(templateType)) {
                createRoomsTemplate(workbook);
            } else if ("tables".equals(templateType)) {
                createTablesTemplate(workbook);
            } else {
                throw new IllegalArgumentException("Invalid template type: " + templateType);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new IOException("Error creating template: " + e.getMessage(), e);
        }
    }

    /**
     * Create Rooms template sheet
     */
    private void createRoomsTemplate(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Rooms");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Tên phòng", "Mô tả", "Số lượng bàn tối đa", "Tổng sức chứa"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // Style header cells
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            cell.setCellStyle(headerStyle);
        }
        
        // Add sample data rows
        String[][] sampleData = {
            {"Phòng VIP", "Phòng VIP cao cấp với không gian sang trọng", "5", "20"},
            {"Phòng Thường", "Phòng phục vụ khách hàng thường", "10", "40"},
            {"Khu Vực Bar", "Khu vực bar với không gian mở", "8", "32"}
        };
        
        for (int i = 0; i < sampleData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < sampleData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(sampleData[i][j]);
                
                // Style data cells
                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Add instructions sheet
        Sheet instructionsSheet = workbook.createSheet("Hướng dẫn");
        createInstructionsSheet(instructionsSheet, "rooms");
    }

    /**
     * Create Tables template sheet
     */
    private void createTablesTemplate(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Tables");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Số bàn", "Tên bàn", "Tên phòng", "Sức chứa"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // Style header cells
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            cell.setCellStyle(headerStyle);
        }
        
        // Add sample data rows
        String[][] sampleData = {
            {"T001", "Bàn VIP 1", "Phòng VIP", "4"},
            {"T002", "Bàn VIP 2", "Phòng VIP", "6"},
            {"T101", "Bàn Thường 1", "Phòng Thường", "4"},
            {"T102", "Bàn Thường 2", "Phòng Thường", "4"},
            {"B001", "Bàn Bar 1", "Khu Vực Bar", "2"}
        };
        
        for (int i = 0; i < sampleData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < sampleData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(sampleData[i][j]);
                
                // Style data cells
                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Add instructions sheet
        Sheet instructionsSheet = workbook.createSheet("Hướng dẫn");
        createInstructionsSheet(instructionsSheet, "tables");
    }

    /**
     * Create instructions sheet
     */
    private void createInstructionsSheet(Sheet sheet, String type) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HƯỚNG DẪN NHẬP DỮ LIỆU");
        
        CellStyle titleStyle = sheet.getWorkbook().createCellStyle();
        Font titleFont = sheet.getWorkbook().createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        rowNum++; // Skip a row
        
        if ("rooms".equals(type)) {
            String[] instructions = {
                "Sheet 'Rooms' - Nhập dữ liệu phòng:",
                "",
                "Cột A - Tên phòng (bắt buộc):",
                "  • Không được để trống",
                "  • Tối đa 100 ký tự",
                "  • Không được trùng tên",
                "",
                "Cột B - Mô tả phòng (tùy chọn):",
                "  • Có thể để trống",
                "  • Tối đa 500 ký tự",
                "",
                "Cột C - Số lượng bàn tối đa (bắt buộc):",
                "  • Phải là số nguyên",
                "  • Từ 0 đến 50",
                "",
                "Cột D - Tổng sức chứa (bắt buộc):",
                "  • Phải là số nguyên",
                "  • Từ 1 đến 1000"
            };
            
            for (String instruction : instructions) {
                Row row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                cell.setCellValue(instruction);
            }
        } else {
            String[] instructions = {
                "Sheet 'Tables' - Nhập dữ liệu bàn:",
                "",
                "Cột A - Số bàn (bắt buộc):",
                "  • Không được để trống",
                "  • Tối đa 50 ký tự",
                "  • Không được trùng số",
                "",
                "Cột B - Tên bàn (bắt buộc):",
                "  • Không được để trống",
                "  • Tối đa 100 ký tự",
                "",
                "Cột C - Tên phòng (tùy chọn):",
                "  • Có thể để trống",
                "  • Phòng phải tồn tại trong hệ thống",
                "",
                "Cột D - Sức chứa (bắt buộc):",
                "  • Phải là số nguyên",
                "  • Từ 1 đến 20",
                "  • Default: 4 nếu để trống",
                "",
                "Lưu ý:",
                "  • Trạng thái sẽ tự động được thiết lập là 'Available'",
                "  • Ngày tạo sẽ tự động được thiết lập theo thời gian hệ thống"
            };
            
            for (String instruction : instructions) {
                Row row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                cell.setCellValue(instruction);
            }
        }
        
        // Auto-size column
        sheet.autoSizeColumn(0);
    }
}
