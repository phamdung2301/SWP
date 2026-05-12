package com.liteflow.controller.cashier;

import com.liteflow.dao.BaseDAO;
import com.liteflow.dao.inventory.ReservationDAO;
import com.liteflow.model.inventory.Reservation;
import com.liteflow.model.inventory.ReservationItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@WebServlet({"/cashier", "/cart/cashier"})
public class CashierServlet extends HttpServlet {
    
    private ReservationDAO reservationDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        this.reservationDAO = new ReservationDAO();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            System.out.println("=== CashierServlet.doGet START ===");
            
            // ✅ Check if this is an AJAX request for tables data
            String action = request.getParameter("action");
            if ("getTables".equals(action)) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                
                List<Map<String, Object>> tables = getTables();
                com.google.gson.Gson gson = new com.google.gson.Gson();
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("tables", tables);
                
                response.getWriter().write(gson.toJson(result));
                System.out.println("✅ Returned tables data via AJAX: " + tables.size() + " tables");
                return;
            }
            
            // Lấy danh sách sản phẩm cho menu
            List<Map<String, Object>> menuItems = getMenuItems();
            System.out.println("✅ Menu items loaded: " + menuItems.size());
            
            // Lấy danh sách bàn
            List<Map<String, Object>> tables = getTables();
            System.out.println("✅ Tables loaded: " + tables.size());
            
            // Lấy danh sách phòng
            List<Map<String, Object>> rooms = getRooms();
            System.out.println("✅ Rooms loaded: " + rooms.size());
            
            // Lấy danh sách danh mục
            List<Map<String, Object>> categories = getCategories();
            System.out.println("✅ Categories loaded: " + categories.size());
            
            // Lấy danh sách đặt bàn hôm nay
            List<Map<String, Object>> reservations = getTodayReservations();
            System.out.println("✅ Reservations loaded: " + reservations.size());
            
            // Convert to JSON strings for JavaScript
            String menuItemsJson = gson.toJson(menuItems);
            String tablesJson = gson.toJson(tables);
            String roomsJson = gson.toJson(rooms);
            String categoriesJson = gson.toJson(categories);
            String reservationsJson = gson.toJson(reservations);
            
            System.out.println("Menu Items JSON length: " + menuItemsJson.length());
            System.out.println("Tables JSON length: " + tablesJson.length());
            System.out.println("Reservations JSON length: " + reservationsJson.length());
            
            request.setAttribute("menuItemsJson", menuItemsJson);
            request.setAttribute("tablesJson", tablesJson);
            request.setAttribute("roomsJson", roomsJson);
            request.setAttribute("categoriesJson", categoriesJson);
            request.setAttribute("reservationsJson", reservationsJson);
            
            System.out.println("=== Forwarding to cashier.jsp ===");
            request.getRequestDispatcher("/cart/cashier.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong CashierServlet: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage());
        }
    }
    
    private List<Map<String, Object>> getMenuItems() {
        List<Map<String, Object>> menuItems = new ArrayList<>();
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            // ✅ JOIN với ProductStock để lấy số lượng tồn kho
            String jpql = "SELECT p.productId, p.name, p.description, p.imageUrl, " +
                       "pv.productVariantId, pv.size, pv.price, " +
                       "c.categoryId, c.name as categoryName, " +
                       "COALESCE(ps.amount, 0) as stockAmount " +
                       "FROM Product p " +
                       "LEFT JOIN ProductVariant pv ON p.productId = pv.product.productId " +
                       "LEFT JOIN ProductStock ps ON pv.productVariantId = ps.productVariant.productVariantId " +
                       "LEFT JOIN ProductCategory pc ON p.productId = pc.product.productId " +
                       "LEFT JOIN Category c ON pc.category.categoryId = c.categoryId " +
                       "WHERE p.isDeleted = false " +
                       "AND (pv.isDeleted = false OR pv.isDeleted IS NULL) " +
                       "ORDER BY c.name, p.name, pv.size";
            
            Query query = em.createQuery(jpql);
            
            List<Object[]> results = query.getResultList();
            
            for (Object[] row : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row[0]);
                item.put("name", row[1]);
                item.put("description", row[2]);
                item.put("imageUrl", row[3]);
                item.put("variantId", row[4]);
                item.put("size", row[5]);
                item.put("price", row[6]);
                item.put("categoryId", row[7]); // ✅ Thêm categoryId
                item.put("category", row[8]);   // Category name
                // ✅ Thêm stock amount (row[9])
                int stockAmount = ((Number) row[9]).intValue();
                item.put("stock", stockAmount);
                
                // Debug log cho stock
                System.out.println("📦 Product: " + row[1] + " (" + row[5] + ") | Stock: " + stockAmount);
                
                // Xử lý đường dẫn hình ảnh
                String imageUrl = (String) row[3];
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Nếu là đường link đầy đủ (bắt đầu bằng http/https), sử dụng trực tiếp
                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        item.put("imageUrl", imageUrl);
                    } else {
                        // Nếu là đường dẫn tương đối, thêm context path
                        item.put("imageUrl", getServletContext().getContextPath() + "/" + imageUrl);
                    }
                } else {
                    // Hình ảnh mặc định nếu không có
                    item.put("imageUrl", getServletContext().getContextPath() + "/images/default-product.jpg");
                }
                
                menuItems.add(item);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy menu items: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        
        return menuItems;
    }
    
    private List<Map<String, Object>> getTables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            String jpql = "SELECT t.tableId, t.tableNumber, t.status, r.name as roomName, t.capacity " +
                       "FROM Table t " +
                       "LEFT JOIN Room r ON t.room.roomId = r.roomId " +
                       "ORDER BY r.name, t.tableNumber";
            
            Query query = em.createQuery(jpql);
            
            List<Object[]> results = query.getResultList();
            
            for (Object[] row : results) {
                Map<String, Object> table = new HashMap<>();
                table.put("id", row[0]);
                table.put("name", row[1]);
                table.put("status", row[2]);
                table.put("room", row[3]);
                // Lấy capacity từ database, nếu null thì mặc định 4
                Integer capacity = (row[4] != null) ? (Integer) row[4] : 4;
                table.put("capacity", capacity);
                
                // Debug log
                System.out.println("📊 Table: " + row[1] + " | Capacity: " + capacity + " | Type: " + (row[4] != null ? row[4].getClass().getName() : "null"));
                
                tables.add(table);
            }
            
            // ✅ Không thêm bàn đặc biệt ở đây nữa - sẽ thêm trong JavaScript
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách bàn: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        
        return tables;
    }
    
    private List<Map<String, Object>> getRooms() {
        List<Map<String, Object>> rooms = new ArrayList<>();
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            String jpql = "SELECT r.roomId, r.name, r.description FROM Room r ORDER BY r.name";
            Query query = em.createQuery(jpql);
            
            List<Object[]> results = query.getResultList();
            
            for (Object[] row : results) {
                Map<String, Object> room = new HashMap<>();
                room.put("id", row[0]);
                room.put("name", row[1]);
                room.put("description", row[2]);
                rooms.add(room);
            }
            
            // ✅ Không cần phòng đặc biệt nữa - các ô đặc biệt được tạo trong JavaScript
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách phòng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        
        return rooms;
    }
    
    private List<Map<String, Object>> getCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            String jpql = "SELECT c.categoryId, c.name, c.description FROM Category c ORDER BY c.name";
            Query query = em.createQuery(jpql);
            
            List<Object[]> results = query.getResultList();
            
            for (Object[] row : results) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", row[0]);
                category.put("name", row[1]);
                category.put("description", row[2]);
                categories.add(category);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh mục: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        
        return categories;
    }
    
    /**
     * Get today's reservations for cashier page
     */
    private List<Map<String, Object>> getTodayReservations() {
        List<Map<String, Object>> reservations = new ArrayList<>();
        
        try {
            LocalDate today = LocalDate.now();
            List<Reservation> todayReservations = reservationDAO.findByDate(today);
            
            for (Reservation reservation : todayReservations) {
                reservations.add(convertToReservationDTO(reservation));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách đặt bàn: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reservations;
    }
    
    /**
     * Convert Reservation entity to DTO map for JSON serialization
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
}
