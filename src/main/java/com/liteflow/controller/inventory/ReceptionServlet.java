package com.liteflow.controller.inventory;

import com.liteflow.dao.BaseDAO;
import com.liteflow.dao.inventory.*;
import com.liteflow.model.inventory.*;
import com.liteflow.service.inventory.ReservationService;
import com.liteflow.dao.reservation.ReservationDTO;
import com.liteflow.dao.reservation.PreOrderItemDTO;
import com.google.gson.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reception Servlet - Handles all reservation-related endpoints
 */
@WebServlet(urlPatterns = {
    "/reception",
    "/reception/api/reservations",
    "/reception/create",
    "/reception/update/*",
    "/reception/update",
    "/reception/arrive",
    "/reception/cancel",
    "/reception/close",
    "/reception/export",
    "/api/reservation/create",
    "/api/reservation/update",
    "/api/reservation/list",
    "/api/reservation/calendar",
    "/api/reservation/assign-table",
    "/api/reservation/confirm-arrival",
    "/api/reservation/cancel",
    "/api/reservation/export",
    "/api/reservation/check-overdue",
    "/api/reservation/search",
    "/api/reservation/statistics"
})
@MultipartConfig
public class ReceptionServlet extends HttpServlet {

    private ReservationService reservationService;
    private ReservationDAO reservationDAO;
    private RoomDAO roomDAO;
    private TableDAO tableDAO;
   
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.reservationService = new ReservationService();
        this.reservationDAO = new ReservationDAO();
        this.roomDAO = new RoomDAO();
        this.tableDAO = new TableDAO();
    
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        try {
            switch (path) {
                case "/reception":
                    handleReceptionPage(request, response);
                    break;
                case "/reception/api/reservations":
                    handleGetReservationsByDate(request, response);
                    break;
                case "/api/reservation/list":
                    handleGetReservations(request, response);
                    break;
                case "/api/reservation/calendar":
                    handleGetCalendarReservations(request, response);
                    break;
                case "/reception/export":
                    handleExportReservations(request, response);
                    break;
                case "/api/reservation/export":
                    handleExportReservations(request, response);
                    break;
                case "/api/reservation/check-overdue":
                    handleCheckOverdue(request, response);
                    break;
                case "/api/reservation/search":
                    handleSearchReservations(request, response);
                    break;
                case "/api/reservation/statistics":
                    handleGetStatistics(request, response);
                    break;
                default:
                    sendJsonError(response, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            System.err.println("❌ Error in ReceptionServlet GET: " + e.getMessage());
            e.printStackTrace();
            
            // If loading page (not API), throw ServletException to show error page
            if (path.equals("/reception")) {
                throw new ServletException("Error loading reception page: " + e.getMessage(), e);
            }
            
            // For API calls, send JSON error
            sendJsonError(response, 500, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        try {
            // Handle /reception/* endpoints (used by reception page)
            if (path.equals("/reception/create")) {
                handleCreateReservation(request, response);
            } else if (path.equals("/reception/update") || path.startsWith("/reception/update/")) {
                handleUpdateReservation(request, response);
            } else if (path.equals("/reception/arrive")) {
                handleConfirmArrival(request, response);
            } else if (path.equals("/reception/cancel")) {
                handleCancelReservation(request, response);
            } else if (path.equals("/reception/close")) {
                handleCloseReservation(request, response);
            }
            // Handle /api/reservation/* endpoints (for API compatibility)
            else {
                switch (path) {
                    case "/api/reservation/create":
                        handleCreateReservation(request, response);
                        break;
                    case "/api/reservation/update":
                        handleUpdateReservation(request, response);
                        break;
                    case "/api/reservation/assign-table":
                        handleAssignTable(request, response);
                        break;
                    case "/api/reservation/confirm-arrival":
                        handleConfirmArrival(request, response);
                        break;
                    case "/api/reservation/cancel":
                        handleCancelReservation(request, response);
                        break;
                    case "/api/reservation/close":
                        handleCloseReservation(request, response);
                        break;
                    default:
                        sendJsonError(response, 404, "Endpoint not found: " + path);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in ReceptionServlet POST: " + e.getMessage());
            e.printStackTrace();
            sendJsonError(response, 500, "Server error: " + e.getMessage());
        }
    }

    // ========== PAGE HANDLERS ==========

    /**
     * Load reception page with initial data
     */
    private void handleReceptionPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Load today's reservations
            LocalDate today = LocalDate.now();
            List<Reservation> todayReservations = reservationDAO.findByDate(today);
            
            // Load rooms
            List<Room> rooms = roomDAO.findAll();
            
            // Load tables
            List<com.liteflow.model.inventory.Table> tables = tableDAO.findAll();
            
            // Load menu items (products with variants and stock) for pre-orders
            List<Map<String, Object>> menuItems = getMenuItems();
            
            // Convert to JSON for JavaScript (manually to avoid lazy loading)
            String reservationsJson = gson.toJson(convertToReservationDTOs(todayReservations));
            String roomsJson = buildRoomsJson(rooms);
            String tablesJson = buildTablesJson(tables);
            String productsJson = gson.toJson(menuItems);
            
            // Set attributes
            request.setAttribute("reservationsJson", reservationsJson);
            request.setAttribute("roomsJson", roomsJson);
            request.setAttribute("tablesJson", tablesJson);
            request.setAttribute("productsJson", productsJson);
            
            // Forward to JSP
            request.getRequestDispatcher("/reception/reception.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("❌ Error loading reception page: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Error loading reception page", e);
        }
    }

    // ========== API HANDLERS ==========

    /**
     * GET /api/reservation/list
     * Get reservations with optional filters
     */
    private void handleGetReservations(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String dateStr = request.getParameter("date");
            String status = request.getParameter("status");
            
            LocalDate date = null;
            if (dateStr != null && !dateStr.isEmpty()) {
                date = LocalDate.parse(dateStr);
            }
            
            List<Reservation> reservations = reservationService.getReservations(date, status, null);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("count", reservations.size());
            responseMap.put("reservations", convertToReservationDTOs(reservations));
            
            sendJsonResponse(response, responseMap);
            
        } catch (DateTimeParseException e) {
            sendJsonError(response, 400, "Invalid date format");
        } catch (Exception e) {
            sendJsonError(response, 500, "Error fetching reservations: " + e.getMessage());
        }
    }

    /**
     * GET /reception/api/reservations
     * Get reservations by date for frontend
     */
    private void handleGetReservationsByDate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String dateStr = request.getParameter("date");
            
            LocalDate date = LocalDate.now();
            if (dateStr != null && !dateStr.isEmpty()) {
                date = LocalDate.parse(dateStr);
            }
            
            List<Reservation> reservations = reservationDAO.findByDate(date);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("date", date.toString());
            responseMap.put("count", reservations.size());
            responseMap.put("reservations", convertToReservationDTOs(reservations));
            
            sendJsonResponse(response, responseMap);
            
        } catch (DateTimeParseException e) {
            sendJsonError(response, 400, "Invalid date format. Use: YYYY-MM-DD");
        } catch (Exception e) {
            System.err.println("❌ Error in handleGetReservationsByDate: " + e.getMessage());
            e.printStackTrace();
            sendJsonError(response, 500, "Error fetching reservations: " + e.getMessage());
        }
    }

    /**
     * GET /api/reservation/calendar
     * Get reservations for calendar view (date range)
     */
    private void handleGetCalendarReservations(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            
            LocalDateTime startDate = LocalDateTime.parse(startDateStr + "T00:00:00");
            LocalDateTime endDate = LocalDateTime.parse(endDateStr + "T23:59:59");
            
            List<Reservation> reservations = reservationDAO.findByDateRange(startDate, endDate);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("count", reservations.size());
            responseMap.put("reservations", convertToReservationDTOs(reservations));
            
            sendJsonResponse(response, responseMap);
            
        } catch (Exception e) {
            sendJsonError(response, 500, "Error fetching calendar reservations: " + e.getMessage());
        }
    }

    /**
     * POST /api/reservation/create
     * Create a new reservation
     */
    private void handleCreateReservation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            // Read request body
            String body = request.getReader().lines().collect(Collectors.joining());
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            
            // Parse DTO
            ReservationDTO dto = new ReservationDTO();
            dto.setCustomerName(json.get("customerName").getAsString());
            dto.setCustomerPhone(json.get("customerPhone").getAsString());
            if (json.has("customerEmail") && !json.get("customerEmail").isJsonNull()) {
                dto.setCustomerEmail(json.get("customerEmail").getAsString());
            }
            // Parse arrival time - handle both ISO format and datetime-local format
            String arrivalTimeStr = json.get("arrivalTime").getAsString();
            LocalDateTime arrivalTime;
            try {
                // Try ISO format first (e.g., "2025-10-31T20:00:00" or "2025-10-31T20:00:00.000")
                arrivalTime = LocalDateTime.parse(arrivalTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e1) {
                try {
                    // Try datetime-local format (e.g., "2025-10-31T20:00")
                    arrivalTime = LocalDateTime.parse(arrivalTimeStr);
                } catch (Exception e2) {
                    throw new IllegalArgumentException("Định dạng thời gian không hợp lệ: " + arrivalTimeStr + ". Vui lòng chọn lại thời gian.");
                }
            }
            dto.setArrivalTime(arrivalTime);
            dto.setNumberOfGuests(json.get("numberOfGuests").getAsInt());
            
            if (json.has("roomId") && !json.get("roomId").isJsonNull()) {
                dto.setRoomId(UUID.fromString(json.get("roomId").getAsString()));
            }
            if (json.has("tableId") && !json.get("tableId").isJsonNull()) {
                dto.setTableId(UUID.fromString(json.get("tableId").getAsString()));
            }
            
            // deposit removed
            
            if (json.has("notes") && !json.get("notes").isJsonNull()) {
                dto.setNotes(json.get("notes").getAsString());
            }
            
            // Parse pre-ordered items
            if (json.has("preOrderedItems") && json.get("preOrderedItems").isJsonArray()) {
                JsonArray itemsArray = json.getAsJsonArray("preOrderedItems");
                List<PreOrderItemDTO> items = new ArrayList<>();
                for (JsonElement element : itemsArray) {
                    JsonObject itemObj = element.getAsJsonObject();
                    PreOrderItemDTO item = new PreOrderItemDTO();
                    item.setProductId(UUID.fromString(itemObj.get("productId").getAsString()));
                    item.setQuantity(itemObj.get("quantity").getAsInt());
                    if (itemObj.has("note") && !itemObj.get("note").isJsonNull()) {
                        item.setNote(itemObj.get("note").getAsString());
                    }
                    items.add(item);
                }
                dto.setPreOrderedItems(items);
            }
            
            // Create reservation
            Reservation reservation = reservationService.createReservation(dto);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Đặt bàn thành công");
            responseMap.put("reservationCode", reservation.getReservationCode());
            responseMap.put("reservation", convertToReservationDTO(reservation));
            
            sendJsonResponse(response, responseMap);
            
        } catch (IllegalArgumentException e) {
            sendJsonError(response, 400, e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error in handleCreateReservation:");
            e.printStackTrace();
            
            // Extract root cause for better error message
            Throwable rootCause = e;
            int depth = 0;
            while (rootCause.getCause() != null && depth < 5) {
                rootCause = rootCause.getCause();
                depth++;
            }
            
            String errorMessage = "Lỗi khi tạo đặt bàn";
            if (rootCause.getMessage() != null && !rootCause.getMessage().isEmpty()) {
                String causeMsg = rootCause.getMessage();
                // Translate common database errors to Vietnamese
                if (causeMsg.contains("UNIQUE constraint") || causeMsg.contains("duplicate key")) {
                    errorMessage = "Mã đặt bàn đã tồn tại. Vui lòng thử lại.";
                } else if (causeMsg.contains("foreign key") || causeMsg.contains("FK_")) {
                    errorMessage = "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin bàn/phòng/sản phẩm.";
                } else if (causeMsg.contains("detached entity")) {
                    errorMessage = "Lỗi kỹ thuật khi lưu dữ liệu. Vui lòng thử lại.";
                } else if (causeMsg.contains("transaction") || causeMsg.contains("commit")) {
                    errorMessage = "Lỗi khi lưu dữ liệu vào cơ sở dữ liệu. Vui lòng thử lại hoặc liên hệ quản trị viên.";
                } else {
                    errorMessage = "Lỗi: " + causeMsg;
                }
            }
            
            sendJsonError(response, 500, errorMessage);
        }
    }

    /**
     * POST /api/reservation/update
     * Update an existing reservation
     */
    private void handleUpdateReservation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            
            UUID reservationId = UUID.fromString(json.get("reservationId").getAsString());
            
            ReservationDTO dto = new ReservationDTO();
            if (json.has("customerName") && !json.get("customerName").isJsonNull()) dto.setCustomerName(json.get("customerName").getAsString());
            if (json.has("customerPhone") && !json.get("customerPhone").isJsonNull()) dto.setCustomerPhone(json.get("customerPhone").getAsString());
            if (json.has("customerEmail") && !json.get("customerEmail").isJsonNull()) dto.setCustomerEmail(json.get("customerEmail").getAsString());
            if (json.has("arrivalTime") && !json.get("arrivalTime").isJsonNull()) dto.setArrivalTime(LocalDateTime.parse(json.get("arrivalTime").getAsString()));
            if (json.has("numberOfGuests") && !json.get("numberOfGuests").isJsonNull()) dto.setNumberOfGuests(json.get("numberOfGuests").getAsInt());
            // deposit removed
            if (json.has("notes") && !json.get("notes").isJsonNull()) dto.setNotes(json.get("notes").getAsString());
            if (json.has("roomId") && !json.get("roomId").isJsonNull()) dto.setRoomId(UUID.fromString(json.get("roomId").getAsString()));
            if (json.has("tableId") && !json.get("tableId").isJsonNull()) dto.setTableId(UUID.fromString(json.get("tableId").getAsString()));
            
            // Parse pre-ordered items if provided
            if (json.has("preOrderedItems") && json.get("preOrderedItems").isJsonArray()) {
                JsonArray itemsArray = json.getAsJsonArray("preOrderedItems");
                List<PreOrderItemDTO> items = new ArrayList<>();
                for (JsonElement element : itemsArray) {
                    JsonObject itemObj = element.getAsJsonObject();
                    PreOrderItemDTO item = new PreOrderItemDTO();
                    item.setProductId(UUID.fromString(itemObj.get("productId").getAsString()));
                    item.setQuantity(itemObj.get("quantity").getAsInt());
                    if (itemObj.has("note") && !itemObj.get("note").isJsonNull()) item.setNote(itemObj.get("note").getAsString());
                    items.add(item);
                }
                dto.setPreOrderedItems(items);
            }
            
            Reservation updated = reservationService.updateReservation(reservationId, dto);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Cập nhật thành công");
            responseMap.put("reservation", convertToReservationDTO(updated));
            
            sendJsonResponse(response, responseMap);
            
        } catch (IllegalArgumentException e) {
            sendJsonError(response, 400, e.getMessage());
        } catch (Exception e) {
            sendJsonError(response, 500, "Error updating reservation: " + e.getMessage());
        }
    }

    /**
     * POST /api/reservation/assign-table
     * Assign a table to a reservation
     */
    private void handleAssignTable(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            
            UUID reservationId = UUID.fromString(json.get("reservationId").getAsString());
            UUID tableId = UUID.fromString(json.get("tableId").getAsString());
            
            boolean success = reservationService.assignTable(reservationId, tableId);
            
            if (success) {
                Reservation reservation = reservationDAO.findById(reservationId);
                
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Đã gán bàn thành công");
                responseMap.put("reservation", convertToReservationDTO(reservation));
                
                sendJsonResponse(response, responseMap);
            } else {
                sendJsonError(response, 400, "Không thể gán bàn");
            }
            
        } catch (IllegalArgumentException e) {
            sendJsonError(response, 400, e.getMessage());
        } catch (Exception e) {
            sendJsonError(response, 500, "Error assigning table: " + e.getMessage());
        }
    }

    /**
     * POST /api/reservation/confirm-arrival
     * Confirm customer arrival and create invoice
     */
    private void handleConfirmArrival(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            
            UUID reservationId = UUID.fromString(json.get("reservationId").getAsString());
            
            Reservation reservation = reservationService.confirmArrival(reservationId);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Đã nhận bàn thành công");
            responseMap.put("reservation", convertToReservationDTO(reservation));
            
            sendJsonResponse(response, responseMap);
            
        } catch (IllegalArgumentException e) {
            sendJsonError(response, 400, e.getMessage());
        } catch (Exception e) {
            sendJsonError(response, 500, "Error confirming arrival: " + e.getMessage());
        }
    }

    /**
     * POST /api/reservation/cancel
     * Cancel a reservation
     */
    private void handleCancelReservation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            
            UUID reservationId = UUID.fromString(json.get("reservationId").getAsString());
            String reason = json.has("reason") ? json.get("reason").getAsString() : null;
            
            boolean success = reservationService.cancelReservation(reservationId, reason);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", success);
            responseMap.put("message", success ? "Đã hủy đặt bàn" : "Không thể hủy đặt bàn");
            
            sendJsonResponse(response, responseMap);
            
        } catch (IllegalArgumentException e) {
            sendJsonError(response, 400, e.getMessage());
        } catch (Exception e) {
            sendJsonError(response, 500, "Error cancelling reservation: " + e.getMessage());
        }
    }

    /**
     * POST /reception/close
     * Mark reservation as CLOSED (paid and finished)
     */
    private void handleCloseReservation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            UUID reservationId = UUID.fromString(json.get("reservationId").getAsString());

            Reservation closed = reservationService.closeReservation(reservationId);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Đã đóng đơn");
            responseMap.put("reservation", convertToReservationDTO(closed));
            sendJsonResponse(response, responseMap);
        } catch (IllegalArgumentException e) {
            sendJsonError(response, 400, e.getMessage());
        } catch (Exception e) {
            sendJsonError(response, 500, "Error closing reservation: " + e.getMessage());
        }
    }

    /**
     * GET /api/reservation/search
     * Search reservations by keyword
     */
    private void handleSearchReservations(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String keyword = request.getParameter("keyword");
            List<Reservation> reservations = reservationService.searchReservations(keyword);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("count", reservations.size());
            responseMap.put("reservations", convertToReservationDTOs(reservations));
            
            sendJsonResponse(response, responseMap);
            
        } catch (Exception e) {
            sendJsonError(response, 500, "Error searching reservations: " + e.getMessage());
        }
    }

    /**
     * GET /api/reservation/statistics
     * Get statistics for a specific date
     */
    private void handleGetStatistics(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String dateStr = request.getParameter("date");
            LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", reservationDAO.findByDate(date).size());
            stats.put("pending", reservationDAO.countByDateAndStatus(date, "PENDING"));
            stats.put("confirmed", reservationDAO.countByDateAndStatus(date, "CONFIRMED"));
            stats.put("cancelled", reservationDAO.countByDateAndStatus(date, "CANCELLED"));
            stats.put("noShow", reservationDAO.countByDateAndStatus(date, "NO_SHOW"));
            stats.put("closed", reservationDAO.countByDateAndStatus(date, "CLOSED"));
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("date", date.toString());
            responseMap.put("statistics", stats);
            
            sendJsonResponse(response, responseMap);
            
        } catch (Exception e) {
            sendJsonError(response, 500, "Error fetching statistics: " + e.getMessage());
        }
    }

    /**
     * GET /api/reservation/check-overdue
     * Check and mark overdue reservations
     */
    private void handleCheckOverdue(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            int count = reservationService.autoCheckOverdue();
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Checked overdue reservations");
            responseMap.put("count", count);
            
            sendJsonResponse(response, responseMap);
            
        } catch (Exception e) {
            sendJsonError(response, 500, "Error checking overdue: " + e.getMessage());
        }
    }

    /**
     * GET /api/reservation/export
     * Export reservations to Excel
     */
    private void handleExportReservations(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String dateStr = request.getParameter("date");
            LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
            
            List<Reservation> reservations = reservationDAO.findByDate(date);
            
            Workbook workbook = createExcelWorkbook(reservations);
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=reservations_" + date + ".xlsx");
            
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
            outputStream.flush();
            
        } catch (Exception e) {
            sendJsonError(response, 500, "Error exporting: " + e.getMessage());
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Create Excel workbook from reservations
     */
    private Workbook createExcelWorkbook(List<Reservation> reservations) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reservations");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Mã đặt bàn", "Tên khách", "SĐT", "Giờ đến", "Số khách", "Phòng/Bàn", "Trạng thái", "Ghi chú"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // Fill data
        int rowNum = 1;
        for (Reservation reservation : reservations) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reservation.getReservationCode());
            row.createCell(1).setCellValue(reservation.getCustomerName());
            row.createCell(2).setCellValue(reservation.getCustomerPhone());
            row.createCell(3).setCellValue(reservation.getArrivalTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            row.createCell(4).setCellValue(reservation.getNumberOfGuests());
            row.createCell(5).setCellValue(reservation.getTable() != null ? reservation.getTable().getTableName() : "Chưa gán");
            row.createCell(6).setCellValue(reservation.getStatus());
            row.createCell(7).setCellValue(reservation.getNotes() != null ? reservation.getNotes() : "");
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        return workbook;
    }

    /**
     * Convert Reservation to DTO for JSON response
     */
    private Map<String, Object> convertToReservationDTO(Reservation reservation) {
        if (reservation == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> dto = new HashMap<>();
        dto.put("reservationId", reservation.getReservationId() != null ? reservation.getReservationId().toString() : null);
        dto.put("reservationCode", reservation.getReservationCode());
        dto.put("customerName", reservation.getCustomerName());
        dto.put("customerPhone", reservation.getCustomerPhone());
        dto.put("customerEmail", reservation.getCustomerEmail());
        dto.put("arrivalTime", reservation.getArrivalTime() != null ? reservation.getArrivalTime().toString() : null);
        dto.put("numberOfGuests", reservation.getNumberOfGuests());
        dto.put("status", reservation.getStatus());
        dto.put("notes", reservation.getNotes());
        dto.put("createdAt", reservation.getCreatedAt() != null ? reservation.getCreatedAt().toString() : null);
        dto.put("updatedAt", reservation.getUpdatedAt() != null ? reservation.getUpdatedAt().toString() : null);
        
        if (reservation.getTable() != null) {
            dto.put("tableId", reservation.getTable().getTableId() != null ? reservation.getTable().getTableId().toString() : null);
            dto.put("tableName", reservation.getTable().getTableName());
        }
        
        if (reservation.getRoom() != null) {
            dto.put("roomId", reservation.getRoom().getRoomId() != null ? reservation.getRoom().getRoomId().toString() : null);
            dto.put("roomName", reservation.getRoom().getName());
        }
        
        // Pre-ordered items - handle null safely
        List<Map<String, Object>> items = new ArrayList<>();
        if (reservation.getReservationItems() != null) {
            try {
                for (ReservationItem item : reservation.getReservationItems()) {
                    if (item != null && item.getProduct() != null) {
                        Map<String, Object> itemDto = new HashMap<>();
                        itemDto.put("productId", item.getProduct().getProductId() != null ? item.getProduct().getProductId().toString() : null);
                        itemDto.put("productName", item.getProduct().getName());
                        itemDto.put("quantity", item.getQuantity());
                        itemDto.put("note", item.getNote());
                        items.add(itemDto);
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error converting reservation items to DTO: " + e.getMessage());
                // Continue with empty items list
            }
        }
        dto.put("preOrderedItems", items);
        
        return dto;
    }

    /**
     * Convert list of reservations to DTOs
     */
    private List<Map<String, Object>> convertToReservationDTOs(List<Reservation> reservations) {
        List<Map<String, Object>> dtos = new ArrayList<>();
        for (Reservation reservation : reservations) {
            dtos.add(convertToReservationDTO(reservation));
        }
        return dtos;
    }

    /**
     * Send JSON response
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    /**
     * Send JSON error response
     */
    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(error));
        out.flush();
    }

    /**
     * Manually build Rooms JSON to avoid lazy loading issues
     * (Reference: RoomTableServlet.getAllRooms)
     */
    private String buildRoomsJson(List<Room> rooms) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            json.append("{");
            json.append("\"roomId\":\"").append(room.getRoomId()).append("\",");
            json.append("\"name\":\"").append(escapeJson(room.getName())).append("\",");
            json.append("\"description\":\"").append(escapeJson(room.getDescription())).append("\",");
            json.append("\"tableCount\":").append(room.getTableCount()).append(",");
            json.append("\"totalCapacity\":").append(room.getTotalCapacity());
            json.append("}");
            if (i < rooms.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    /**
     * Manually build Tables JSON to avoid lazy loading issues
     * (Reference: RoomTableServlet pattern)
     */
    private String buildTablesJson(List<com.liteflow.model.inventory.Table> tables) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < tables.size(); i++) {
            com.liteflow.model.inventory.Table table = tables.get(i);
            json.append("{");
            json.append("\"tableId\":\"").append(table.getTableId()).append("\",");
            json.append("\"tableName\":\"").append(escapeJson(table.getTableName())).append("\",");
            json.append("\"capacity\":").append(table.getCapacity()).append(",");
            json.append("\"status\":\"").append(table.getStatus()).append("\",");
            // Get roomId and roomName without triggering lazy load
            if (table.getRoom() != null) {
                json.append("\"roomId\":\"").append(table.getRoom().getRoomId()).append("\",");
                json.append("\"roomName\":\"").append(escapeJson(table.getRoom().getName())).append("\"");
            } else {
                json.append("\"roomId\":null,");
                json.append("\"roomName\":null");
            }
            json.append("}");
            if (i < tables.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    /**
     * Get menu items with variants and stock info (similar to CashierServlet)
     */
    private List<Map<String, Object>> getMenuItems() {
        List<Map<String, Object>> menuItems = new ArrayList<>();
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            // JOIN với ProductStock để lấy số lượng tồn kho
            String jpql = "SELECT p.productId, p.name, p.description, p.imageUrl, " +
                       "pv.productVariantId, pv.size, pv.price, " +
                       "COALESCE(ps.amount, 0) as stockAmount " +
                       "FROM Product p " +
                       "LEFT JOIN ProductVariant pv ON p.productId = pv.product.productId " +
                       "LEFT JOIN ProductStock ps ON pv.productVariantId = ps.productVariant.productVariantId " +
                       "WHERE p.isDeleted = false " +
                       "AND (pv.isDeleted = false OR pv.isDeleted IS NULL) " +
                       "ORDER BY p.name, pv.size";
            
            Query query = em.createQuery(jpql);
           
            List<Object[]> results = query.getResultList();
            
            System.out.println("📦 Loading menu items for reception: " + results.size() + " items");
            
            for (Object[] row : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", row[0].toString());
                item.put("name", row[1]);
                item.put("description", row[2]);
                
                // Xử lý hình ảnh
                String imageUrl = (String) row[3];
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        item.put("imageUrl", imageUrl);
                    } else {
                        item.put("imageUrl", getServletContext().getContextPath() + "/" + imageUrl);
                    }
                } else {
                    item.put("imageUrl", null);
                }
                
                item.put("variantId", row[4] != null ? row[4].toString() : null);
                item.put("size", row[5]);
                item.put("price", row[6]);
                
                // Stock amount
                int stockAmount = row[7] != null ? ((Number) row[7]).intValue() : 0;
                item.put("quantityAvailable", stockAmount);
                
                System.out.println("  📦 " + row[1] + " (" + row[5] + ") | Stock: " + stockAmount);
                
                menuItems.add(item);
            }
            
            System.out.println("✅ Loaded " + menuItems.size() + " menu items for reception");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading menu items: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        
        return menuItems;
    }

    /**
     * Escape special characters for JSON string
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    // ========== GSON ADAPTERS ==========

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime dateTime, java.lang.reflect.Type type, JsonSerializationContext context) {
            return new JsonPrimitive(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate date, java.lang.reflect.Type type, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        @Override
        public LocalDate deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) {
            return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }
}

