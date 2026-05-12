package com.liteflow.service.inventory;

import com.liteflow.dao.inventory.*;
import com.liteflow.model.inventory.*;
import com.liteflow.dao.reservation.ReservationDTO;
import com.liteflow.dao.reservation.PreOrderItemDTO;
import com.liteflow.dao.reservation.ValidationResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final ReservationItemDAO reservationItemDAO;
    private final TableDAO tableDAO;
    private final ProductDAO productDAO;

    // Phone validation pattern (Vietnamese phone numbers)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+84|0)[1-9][0-9]{8,9}$");

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.reservationItemDAO = new ReservationItemDAO();
        this.tableDAO = new TableDAO();
        this.productDAO = new ProductDAO();
    }

    /**
     * Generate reservation code in format RS-XXXXXXXX
     * Example: RS-a1b2c3d4
     * Uses UUID to guarantee uniqueness (no retry needed)
     */
    public String generateReservationCode(LocalDate date) {
        // Generate short UUID: RS-XXXXXXXX (8 chars from UUID)
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String code = "RS-" + uuid.substring(0, 8).toUpperCase();
        return code;
    }

    /**
     * Validate phone number format
     */
    public boolean validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validate availability of tables/rooms for the requested time and guest count
     */
    public boolean validateAvailability(LocalDateTime arrivalTime, int numberOfGuests) {
        try {
            // Check if there are tables with sufficient capacity
            List<Table> allTables = tableDAO.findAll();
            
            // Count available tables at that time
            int availableCapacity = 0;
            for (Table table : allTables) {
                if (table.isAvailable() || table.getStatus().equals("Available")) {
                    availableCapacity += table.getCapacity();
                }
            }
            
            // If total available capacity is sufficient, return true
            return availableCapacity >= numberOfGuests;
        } catch (Exception e) {
            throw new RuntimeException("Error validating availability: " + e.getMessage(), e);
        }
    }

    /**
     * Validate pre-ordered items (check stock availability)
     */
    public ValidationResult validatePreOrderedItems(List<UUID> productIds) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        try {
            for (UUID productId : productIds) {
                Product product = productDAO.findById(productId);
                if (product == null) {
                    result.setValid(false);
                    result.addError("Sản phẩm ID " + productId + " không tồn tại");
                    continue;
                }

                // Note: Stock check is skipped for reservations as they are advance bookings
                // Customers can pre-order items even if temporarily out of stock
            }
        } catch (Exception e) {
            result.setValid(false);
            result.addError("Lỗi khi kiểm tra tồn kho: " + e.getMessage());
        }

        return result;
    }

    /**
     * Create a new reservation
     */
    public Reservation createReservation(ReservationDTO dto) {
        try {
            // Validate input
            if (dto.getCustomerName() == null || dto.getCustomerName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên khách hàng không được để trống");
            }
            if (dto.getCustomerPhone() == null || dto.getCustomerPhone().trim().isEmpty()) {
                throw new IllegalArgumentException("Số điện thoại không được để trống");
            }
            if (!validatePhoneNumber(dto.getCustomerPhone())) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ");
            }
            if (dto.getArrivalTime() == null) {
                throw new IllegalArgumentException("Giờ đến không được để trống");
            }
            if (dto.getArrivalTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Giờ đến phải sau thời gian hiện tại");
            }
            if (dto.getNumberOfGuests() == null || dto.getNumberOfGuests() <= 0) {
                throw new IllegalArgumentException("Số lượng khách phải lớn hơn 0");
            }

            // Enforce: one booking per day per identifier (name/phone/email)
            LocalDate bookingDate = dto.getArrivalTime().toLocalDate();
            boolean existsSameDay = reservationDAO.existsSameDayByIdentifier(
                    bookingDate,
                    dto.getCustomerName(),
                    dto.getCustomerPhone(),
                    dto.getCustomerEmail()
            );
            if (existsSameDay) {
                throw new IllegalArgumentException("Khách đã có đặt bàn trong ngày này (theo tên/SĐT/email). Vui lòng chọn ngày khác hoặc cập nhật đặt bàn cũ.");
            }

            // Generate reservation code using UUID (guaranteed unique, no retry needed)
            String code = generateReservationCode(bookingDate);

            // Create reservation entity
            Reservation reservation = new Reservation();
            reservation.setReservationCode(code);
            reservation.setCustomerName(dto.getCustomerName().trim());
            reservation.setCustomerPhone(dto.getCustomerPhone().trim());
            reservation.setCustomerEmail(dto.getCustomerEmail() != null ? dto.getCustomerEmail().trim() : null);
            reservation.setArrivalTime(dto.getArrivalTime());
            reservation.setNumberOfGuests(dto.getNumberOfGuests());
            // Deposit removed
            reservation.setNotes(dto.getNotes());
            reservation.setStatus("PENDING");

            // Set room if provided
            if (dto.getRoomId() != null) {
                RoomDAO roomDAO = new RoomDAO();
                Room room = roomDAO.findById(dto.getRoomId());
                if (room != null) {
                    reservation.setRoom(room);
                }
            }

            // Set table if provided (and sync room if missing)
            if (dto.getTableId() != null) {
                com.liteflow.model.inventory.Table table = tableDAO.findById(dto.getTableId());
                if (table != null) {
                    reservation.setTable(table);
                    if (reservation.getRoom() == null && table.getRoom() != null) {
                        reservation.setRoom(table.getRoom());
                    }
                }
            }

            // Save reservation
            reservation = reservationDAO.create(reservation);

            // Add pre-ordered items
            if (dto.getPreOrderedItems() != null && !dto.getPreOrderedItems().isEmpty()) {
                for (PreOrderItemDTO itemDto : dto.getPreOrderedItems()) {
                    try {
                        Product product = productDAO.findById(itemDto.getProductId());
                        if (product == null) {
                            System.err.println("⚠️ Product not found: " + itemDto.getProductId());
                            continue;
                        }
                        ReservationItem item = new ReservationItem();
                        item.setReservation(reservation);
                        item.setProduct(product);
                        item.setQuantity(itemDto.getQuantity());
                        item.setNote(itemDto.getNote());
                        reservationItemDAO.create(item);
                    } catch (Exception e) {
                        System.err.println("❌ Error adding pre-order item for product " + itemDto.getProductId() + ": " + e.getMessage());
                        e.printStackTrace();
                        // Continue with other items, but log the error
                    }
                }
            }
            
            // Reload reservation from DB to get all related data (items, room, table) in managed state
            reservation = reservationDAO.findById(reservation.getReservationId());
            if (reservation == null) {
                throw new RuntimeException("Không thể tải lại thông tin đặt bàn sau khi tạo.");
            }

            // Send confirmation email if email is provided
            if (reservation.getCustomerEmail() != null && !reservation.getCustomerEmail().trim().isEmpty()) {
                sendConfirmationEmailAsync(reservation);
            }

            return reservation;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Update reservation
     */
    public Reservation updateReservation(UUID reservationId, ReservationDTO dto) {
        try {
            Reservation reservation = reservationDAO.findById(reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Không tìm thấy đặt bàn");
            }

            // Update fields if provided
            if (dto.getCustomerName() != null && !dto.getCustomerName().trim().isEmpty()) {
                reservation.setCustomerName(dto.getCustomerName().trim());
            }
            if (dto.getCustomerPhone() != null && !dto.getCustomerPhone().trim().isEmpty()) {
                if (!validatePhoneNumber(dto.getCustomerPhone())) {
                    throw new IllegalArgumentException("Số điện thoại không hợp lệ");
                }
                reservation.setCustomerPhone(dto.getCustomerPhone().trim());
            }
            if (dto.getCustomerEmail() != null) {
                reservation.setCustomerEmail(dto.getCustomerEmail().trim().isEmpty() ? null : dto.getCustomerEmail().trim());
            }
            if (dto.getArrivalTime() != null) {
                // Enforce rule when date or identifiers change
                LocalDate newDate = dto.getArrivalTime().toLocalDate();
                String newName = dto.getCustomerName() != null && !dto.getCustomerName().trim().isEmpty()
                        ? dto.getCustomerName() : reservation.getCustomerName();
                String newPhone = dto.getCustomerPhone() != null && !dto.getCustomerPhone().trim().isEmpty()
                        ? dto.getCustomerPhone() : reservation.getCustomerPhone();
                String newEmail = dto.getCustomerEmail() != null && !dto.getCustomerEmail().trim().isEmpty()
                        ? dto.getCustomerEmail() : reservation.getCustomerEmail();

                boolean existsSameDay = reservationDAO.existsSameDayByIdentifierExcluding(
                        newDate,
                        newName,
                        newPhone,
                        newEmail,
                        reservationId
                );
                if (existsSameDay) {
                    throw new IllegalArgumentException("Khách đã có đặt bàn trong ngày này (theo tên/SĐT/email). Vui lòng chọn ngày khác hoặc chỉnh sửa đặt bàn kia.");
                }

                reservation.setArrivalTime(dto.getArrivalTime());
            }
            if (dto.getNumberOfGuests() != null && dto.getNumberOfGuests() > 0) {
                reservation.setNumberOfGuests(dto.getNumberOfGuests());
            }
            // Deposit removed
            if (dto.getNotes() != null) {
                reservation.setNotes(dto.getNotes());
            }

            // Update room if provided
            if (dto.getRoomId() != null) {
                RoomDAO roomDAO = new RoomDAO();
                Room room = roomDAO.findById(dto.getRoomId());
                if (room != null) {
                    reservation.setRoom(room);
                }
            }

            // Update table if provided
            if (dto.getTableId() != null) {
                com.liteflow.model.inventory.Table table = tableDAO.findById(dto.getTableId());
                if (table != null) {
                    reservation.setTable(table);
                    if (table.getRoom() != null) {
                        reservation.setRoom(table.getRoom());
                    }
                }
            }

            // Update reservation
            reservation = reservationDAO.updateReservation(reservation);

            // Update pre-ordered items if provided
            if (dto.getPreOrderedItems() != null) {
                // Delete existing items
                reservationItemDAO.deleteByReservationId(reservationId);
                
                // Add new items
                for (PreOrderItemDTO itemDto : dto.getPreOrderedItems()) {
                    Product product = productDAO.findById(itemDto.getProductId());
                    if (product != null) {
                        ReservationItem item = new ReservationItem();
                        item.setReservation(reservation);
                        item.setProduct(product);
                        item.setQuantity(itemDto.getQuantity());
                        item.setNote(itemDto.getNote());
                        reservationItemDAO.create(item);
                    }
                }
            }

            return reservationDAO.findById(reservationId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Assign table to reservation
     */
    public boolean assignTable(UUID reservationId, UUID tableId) {
        try {
            Reservation reservation = reservationDAO.findById(reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Không tìm thấy đặt bàn");
            }

            Table table = tableDAO.findById(tableId);
            if (table == null) {
                throw new IllegalArgumentException("Không tìm thấy bàn");
            }

            // Check if table is available
            if (!table.isAvailable() && !"Available".equals(table.getStatus())) {
                throw new IllegalArgumentException("Bàn không khả dụng");
            }

            // Check capacity
            if (table.getCapacity() < reservation.getNumberOfGuests()) {
                throw new IllegalArgumentException("Sức chứa bàn không đủ cho số lượng khách");
            }

            return reservationDAO.assignTable(reservationId, tableId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error assigning table: " + e.getMessage(), e);
        }
    }

    /**
     * Confirm customer arrival (status: SEATED)
     * This will trigger cashier invoice creation
     */
    public Reservation confirmArrival(UUID reservationId) {
        try {
            Reservation reservation = reservationDAO.findById(reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Không tìm thấy đặt bàn");
            }

            if (reservation.isConfirmed()) {
                throw new IllegalArgumentException("Khách đã được xác nhận đến");
            }

            if (reservation.isCancelled() || reservation.isNoShow()) {
                throw new IllegalArgumentException("Không thể xác nhận cho đặt bàn đã hủy hoặc không đến");
            }

            // Update status to CONFIRMED (not SEATED)
            reservation.setStatus("CONFIRMED");
            
            // Update table status to Occupied if table is assigned
            if (reservation.getTable() != null) {
                Table table = reservation.getTable();
                table.setStatus("Occupied");
                tableDAO.update(table);
            }

            // Create order and notify kitchen if there are pre-ordered items
            // OrderDAO will automatically create TableSession for the table
            if (reservation.getReservationItems() != null && !reservation.getReservationItems().isEmpty()) {
                notifyKitchenForReservation(reservation);
            }

            return reservationDAO.updateReservation(reservation);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error confirming arrival: " + e.getMessage(), e);
        }
    }
    
    /**
     * Notify kitchen with pre-ordered items
     */
    private void notifyKitchenForReservation(Reservation reservation) {
        try {
            List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
            ProductVariantDAO variantDAO = new ProductVariantDAO();
            
            for (ReservationItem item : reservation.getReservationItems()) {
                try {
                    Product product = item.getProduct();
                    UUID productId = product.getProductId();
                    
                    // Load variants from database to avoid lazy loading issue
                    List<ProductVariant> variants = variantDAO.findByProductId(productId);
                    
                    if (variants == null || variants.isEmpty()) {
                        System.err.println("⚠️ Sản phẩm " + product.getName() + " (ID: " + productId + ") không có variant, bỏ qua");
                        continue;
                    }
                    
                    // Use first variant as default
                    ProductVariant variant = variants.get(0);
                    
                    java.util.Map<String, Object> orderItem = new java.util.HashMap<>();
                    orderItem.put("variantId", variant.getProductVariantId().toString());
                    orderItem.put("quantity", item.getQuantity());
                    orderItem.put("unitPrice", variant.getPrice());
                    orderItem.put("note", item.getNote());
                    orderItem.put("status", "Preparing"); // Món đặt trước đã thông báo bếp
                    items.add(orderItem);
                } catch (Exception itemEx) {
                    System.err.println("⚠️ Lỗi khi xử lý món: " + itemEx.getMessage());
                    continue; // Skip this item and continue with others
                }
            }
            
            if (!items.isEmpty()) {
                OrderService orderService = new OrderService();
                UUID tableId = reservation.getTable() != null ? reservation.getTable().getTableId() : null;
                UUID userId = null; // Can be set from session if available
                String invoiceName = reservation.getCustomerName();
                String orderNote = "Đặt trước - Mã: " + reservation.getReservationCode();
                
                orderService.createOrderAndNotifyKitchen(tableId, items, userId, invoiceName, orderNote);
                System.out.println("✅ Đã gửi thông báo món đặt trước tới bếp cho: " + reservation.getReservationCode());
            } else {
                System.out.println("ℹ️ Không có món hợp lệ để gửi tới bếp cho: " + reservation.getReservationCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi thông báo tới bếp: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - let the confirmation continue even if kitchen notification fails
        }
    }

    /**
     * Cancel reservation
     */
    public boolean cancelReservation(UUID reservationId, String reason) {
        try {
            Reservation reservation = reservationDAO.findById(reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Không tìm thấy đặt bàn");
            }

            if (reservation.isCancelled()) {
                throw new IllegalArgumentException("Đặt bàn đã bị hủy");
            }

            if (reservation.isSeated()) {
                throw new IllegalArgumentException("Không thể hủy đặt bàn đã nhận bàn");
            }

            // Update status
            reservation.setStatus("CANCELLED");
            if (reason != null && !reason.trim().isEmpty()) {
                String currentNotes = reservation.getNotes() != null ? reservation.getNotes() : "";
                reservation.setNotes(currentNotes + "\nLý do hủy: " + reason);
            }

            // Free up table if assigned
            if (reservation.getTable() != null) {
                Table table = reservation.getTable();
                table.setStatus("Available");
                tableDAO.update(table);
                reservation.setTable(null);
            }

            reservationDAO.updateReservation(reservation);
            return true;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error cancelling reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Mark reservation as NO_SHOW
     */
    public boolean markNoShow(UUID reservationId) {
        try {
            Reservation reservation = reservationDAO.findById(reservationId);
            if (reservation == null) {
                return false;
            }

            reservation.setStatus("NO_SHOW");

            // Free up table if assigned
            if (reservation.getTable() != null) {
                Table table = reservation.getTable();
                table.setStatus("Available");
                tableDAO.update(table);
                reservation.setTable(null);
            }

            reservationDAO.updateReservation(reservation);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error marking no show: " + e.getMessage(), e);
        }
    }

    /**
     * Close reservation (paid). Status becomes CLOSED and frees table
     */
    public Reservation closeReservation(UUID reservationId) {
        try {
            Reservation reservation = reservationDAO.findById(reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Không tìm thấy đặt bàn");
            }

            if (reservation.isCancelled()) {
                throw new IllegalArgumentException("Không thể đóng đơn đã hủy");
            }

            // Update status to CLOSED
            reservation.setStatus("CLOSED");

            // Free up table if assigned
            if (reservation.getTable() != null) {
                Table table = reservation.getTable();
                table.setStatus("Available");
                tableDAO.update(table);
                reservation.setTable(null);
            }

            return reservationDAO.updateReservation(reservation);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error closing reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Auto-check and mark overdue reservations (background job)
     */
    public int autoCheckOverdue() {
        try {
            List<Reservation> overdueReservations = reservationDAO.checkOverdue(30);
            int count = 0;

            for (Reservation reservation : overdueReservations) {
                markNoShow(reservation.getReservationId());
                count++;
            }

            return count;
        } catch (Exception e) {
            throw new RuntimeException("Error auto-checking overdue: " + e.getMessage(), e);
        }
    }

    /**
     * Get reservations by various filters
     */
    public List<Reservation> getReservations(LocalDate date, String status, String view) {
        try {
            if (date != null && status != null) {
                // Filter by both date and status
                List<Reservation> dateReservations = reservationDAO.findByDate(date);
                return dateReservations.stream()
                    .filter(r -> r.getStatus().equals(status))
                    .toList();
            } else if (date != null) {
                return reservationDAO.findByDate(date);
            } else if (status != null) {
                return reservationDAO.findByStatus(status);
            } else {
                return reservationDAO.findAll();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Search reservations
     */
    public List<Reservation> searchReservations(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return reservationDAO.findAll();
            }
            return reservationDAO.search(keyword.trim());
        } catch (Exception e) {
            throw new RuntimeException("Error searching reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Send confirmation email asynchronously
     * This method runs in a separate thread to avoid blocking the main request
     */
    private void sendConfirmationEmailAsync(Reservation reservation) {
        new Thread(() -> {
            try {
                // Format arrival time
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String arrivalTimeStr = reservation.getArrivalTime().format(formatter);

                // Deposit removed -> pass null to hide block in email template
                String depositAmountStr = null;

                // Get table name
                String tableName = null;
                if (reservation.getTable() != null) {
                    tableName = reservation.getTable().getTableName();
                    if (reservation.getRoom() != null) {
                        tableName = reservation.getRoom().getName() + " - " + tableName;
                    }
                }

                // Build pre-ordered items HTML
                StringBuilder preOrderedItemsHtml = new StringBuilder();
                if (reservation.getReservationItems() != null && !reservation.getReservationItems().isEmpty()) {
                    for (ReservationItem item : reservation.getReservationItems()) {
                        preOrderedItemsHtml.append("<div class=\"item\">")
                                .append("<strong>").append(item.getProduct().getName()).append("</strong>")
                                .append(" - Số lượng: <strong>").append(item.getQuantity()).append("</strong>");
                        if (item.getNote() != null && !item.getNote().trim().isEmpty()) {
                            preOrderedItemsHtml.append("<br><em>Ghi chú: ").append(item.getNote()).append("</em>");
                        }
                        preOrderedItemsHtml.append("</div>");
                    }
                }

                // Send email
                com.liteflow.util.MailUtil.sendReservationConfirmationMail(
                        reservation.getCustomerEmail(),
                        reservation.getCustomerName(),
                        reservation.getReservationCode(),
                        arrivalTimeStr,
                        reservation.getNumberOfGuests(),
                        tableName,
                        depositAmountStr,
                        preOrderedItemsHtml.toString()
                );

                System.out.println("✅ Confirmation email sent successfully to " + reservation.getCustomerEmail() + 
                                 " for reservation " + reservation.getReservationCode());

            } catch (Exception e) {
                System.err.println("❌ Failed to send confirmation email for reservation " + 
                                 reservation.getReservationCode() + ": " + e.getMessage());
                e.printStackTrace();
                // Don't throw exception - email failure shouldn't fail the reservation
            }
        }).start();
    }

    /**
     * Close reservation after payment (from cashier)
     * Updates reservation status to CLOSED for the table
     */
    public void closeReservationByTable(UUID tableId) {
        try {
            // Tìm reservation CONFIRMED của bàn này (chưa đóng)
            Reservation reservation = reservationDAO.findActiveReservationByTable(tableId);
            
            if (reservation != null) {
                // Cập nhật trạng thái sang CLOSED
                reservation.setStatus("CLOSED");
                reservationDAO.update(reservation);
                
                System.out.println("✅ Đã đóng reservation " + reservation.getReservationCode() + 
                                 " cho bàn " + tableId);
            } else {
                System.out.println("ℹ️ Không tìm thấy reservation active cho bàn " + tableId);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đóng reservation: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - payment shouldn't fail if reservation update fails
        }
    }

}


