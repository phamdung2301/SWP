package com.liteflow.service.inventory;

import com.liteflow.dao.inventory.RoomDAO;
import com.liteflow.dao.inventory.TableDAO;
import com.liteflow.model.inventory.Room;
import com.liteflow.model.inventory.Table;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class RoomTableService {
    
    private RoomDAO roomDAO;
    private TableDAO tableDAO;
    private EntityManagerFactory emf;
    
    public RoomTableService() {
        this.roomDAO = new RoomDAO();
        this.tableDAO = new TableDAO();
        this.emf = com.liteflow.dao.BaseDAO.emf;
    }
    
    // Room operations
    public List<Room> getAllRooms() {
        try {
            return roomDAO.findAll();
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách phòng: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public Room getRoomById(UUID roomId) {
        try {
            return roomDAO.findById(roomId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy phòng theo ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean addRoom(Room room) {
        try {
            System.out.println("=== DEBUG: RoomTableService.addRoom ===");
            System.out.println("Room: " + room);
            System.out.println("Room Name: " + room.getName());
            System.out.println("Room Description: " + room.getDescription());
            System.out.println("Room TableCount: " + room.getTableCount());
            System.out.println("Room TotalCapacity: " + room.getTotalCapacity());
            
            // Ngày tạo sẽ được tự động thiết lập trong @PrePersist
            // Không cần thiết lập thủ công ở đây
            
            System.out.println("Calling roomDAO.insert...");
            boolean result = roomDAO.insert(room);
            System.out.println("roomDAO.insert result: " + result);
            
            return result;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thêm phòng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateRoom(Room room) {
        try {
            return roomDAO.update(room);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cập nhật phòng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteRoom(UUID roomId) {
        try {
            System.out.println("=== DEBUG: RoomTableService.deleteRoom ===");
            System.out.println("Room ID to delete: " + roomId);
            
            // Check if room exists first
            Room room = roomDAO.findById(roomId);
            if (room == null) {
                System.out.println("❌ Room not found with ID: " + roomId);
                return false;
            }
            
            System.out.println("Room found: " + room.getName());
            
            // Check if room has related data
            boolean hasRelatedData = roomDAO.checkRoomHasRelatedData(roomId);
            System.out.println("Room has related data: " + hasRelatedData);
            
            // First, manually delete all related data using JPA
            System.out.println("Manually deleting related data...");
            boolean relatedDataDeleted = deleteAllRelatedData(roomId);
            System.out.println("Related data deletion result: " + relatedDataDeleted);
            
            // Then try to delete the room
            System.out.println("Trying to delete room...");
            boolean result = roomDAO.delete(roomId);
            System.out.println("Room deletion result: " + result);
            
            if (!result) {
                System.out.println("JPA delete failed, trying Native SQL delete...");
                result = roomDAO.deleteWithNativeSQL(roomId);
                System.out.println("Native SQL delete result: " + result);
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa phòng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean deleteAllRelatedData(UUID roomId) {
        try {
            System.out.println("=== DEBUG: deleteAllRelatedData ===");
            System.out.println("Room ID: " + roomId);
            
            EntityManager em = emf.createEntityManager();
            var tx = em.getTransaction();
            
            try {
                tx.begin();
                
                // Step 1: Delete PaymentTransactions for all tables in this room
                try {
                    System.out.println("Step 1: Deleting PaymentTransactions...");
                    int deletedPayments = em.createNativeQuery(
                        "DELETE FROM PaymentTransactions WHERE SessionID IN (" +
                        "SELECT ts.SessionID FROM TableSessions ts " +
                        "INNER JOIN Tables t ON ts.TableID = t.TableID " +
                        "WHERE t.RoomID = ?)", 
                        Integer.class
                    ).setParameter(1, roomId).executeUpdate();
                    System.out.println("Deleted " + deletedPayments + " payment transactions");
                } catch (Exception e) {
                    System.out.println("Step 1 failed (continuing): " + e.getMessage());
                }
                
                // Step 2: Delete OrderDetails for all tables in this room
                try {
                    System.out.println("Step 2: Deleting OrderDetails...");
                    int deletedOrderDetails = em.createNativeQuery(
                        "DELETE FROM OrderDetails WHERE OrderID IN (" +
                        "SELECT o.OrderID FROM Orders o " +
                        "INNER JOIN TableSessions ts ON o.SessionID = ts.SessionID " +
                        "INNER JOIN Tables t ON ts.TableID = t.TableID " +
                        "WHERE t.RoomID = ?)", 
                        Integer.class
                    ).setParameter(1, roomId).executeUpdate();
                    System.out.println("Deleted " + deletedOrderDetails + " order details");
                } catch (Exception e) {
                    System.out.println("Step 2 failed (continuing): " + e.getMessage());
                }
                
                // Step 3: Delete Orders for all tables in this room
                try {
                    System.out.println("Step 3: Deleting Orders...");
                    int deletedOrders = em.createNativeQuery(
                        "DELETE FROM Orders WHERE SessionID IN (" +
                        "SELECT ts.SessionID FROM TableSessions ts " +
                        "INNER JOIN Tables t ON ts.TableID = t.TableID " +
                        "WHERE t.RoomID = ?)", 
                        Integer.class
                    ).setParameter(1, roomId).executeUpdate();
                    System.out.println("Deleted " + deletedOrders + " orders");
                } catch (Exception e) {
                    System.out.println("Step 3 failed (continuing): " + e.getMessage());
                }
                
                // Step 4: Delete TableSessions for all tables in this room
                try {
                    System.out.println("Step 4: Deleting TableSessions...");
                    int deletedSessions = em.createNativeQuery(
                        "DELETE FROM TableSessions WHERE TableID IN (" +
                        "SELECT TableID FROM Tables WHERE RoomID = ?)", 
                        Integer.class
                    ).setParameter(1, roomId).executeUpdate();
                    System.out.println("Deleted " + deletedSessions + " table sessions");
                } catch (Exception e) {
                    System.out.println("Step 4 failed (continuing): " + e.getMessage());
                }
                
                // Step 5: Delete all tables in this room
                try {
                    System.out.println("Step 5: Deleting Tables...");
                    int deletedTables = em.createNativeQuery(
                        "DELETE FROM Tables WHERE RoomID = ?", 
                        Integer.class
                    ).setParameter(1, roomId).executeUpdate();
                    System.out.println("Deleted " + deletedTables + " tables");
                } catch (Exception e) {
                    System.out.println("Step 5 failed (continuing): " + e.getMessage());
                }
                
                tx.commit();
                System.out.println("All room related data deleted successfully");
                return true;
                
            } catch (Exception e) {
                System.err.println("❌ Error deleting room related data: " + e.getMessage());
                e.printStackTrace();
                if (tx.isActive()) {
                    tx.rollback();
                }
                return false;
            } finally {
                em.close();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting room related data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Table operations
    public List<Table> getAllTables() {
        try {
            return tableDAO.findAll();
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách bàn: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Table> getTablesByRoomId(UUID roomId) {
        try {
            return tableDAO.findByRoomId(roomId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy bàn theo phòng: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public Table getTableById(UUID tableId) {
        try {
            return tableDAO.findById(tableId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy bàn theo ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean addTable(Table table) {
        EntityManager em = this.emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            // Ngày tạo và trạng thái sẽ được tự động thiết lập trong @PrePersist
            // Không cần thiết lập thủ công ở đây
            
            // Ensure the room is attached to the current persistence context
            if (table.getRoom() != null && table.getRoom().getRoomId() != null) {
                Room room = em.find(Room.class, table.getRoom().getRoomId());
                if (room != null) {
                    table.setRoom(room);
                }
            }
            
            // For new tables, set ID to null to let @PrePersist generate it
            // This prevents "detached entity passed to persist" errors
            UUID originalTableId = table.getTableId();
            if (originalTableId != null) {
                // Check if table already exists
                Table existingTable = em.find(Table.class, originalTableId);
                if (existingTable != null) {
                    // Table already exists, merge it
                    tx.begin();
                    em.merge(table);
                    tx.commit();
                    return true;
                }
                // Table ID exists but entity doesn't, set ID to null for new table
                table.setTableId(null);
            }
            
            tx.begin();
            em.persist(table);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("❌ Lỗi khi thêm bàn: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    public boolean updateTable(Table table) {
        try {
            return tableDAO.update(table);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cập nhật bàn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteTable(UUID tableId) {
        try {
            System.out.println("=== DEBUG: RoomTableService.deleteTable ===");
            System.out.println("Table ID to delete: " + tableId);
            
            // Check if table exists first
            Table table = tableDAO.findById(tableId);
            if (table == null) {
                System.out.println("❌ Table not found with ID: " + tableId);
                return false;
            }
            
            System.out.println("Table found: " + table.getTableName());
            
            // First, manually delete all related data using JPA
            System.out.println("Manually deleting related data...");
            boolean relatedDataDeleted = deleteAllTableRelatedData(tableId);
            System.out.println("Related data deletion result: " + relatedDataDeleted);
            
            // Then try to delete the table
            System.out.println("Trying to delete table...");
            boolean result = tableDAO.delete(tableId);
            System.out.println("Table deletion result: " + result);
            
            return result;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa bàn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean deleteAllTableRelatedData(UUID tableId) {
        try {
            System.out.println("=== DEBUG: deleteAllTableRelatedData ===");
            System.out.println("Table ID: " + tableId);
            
            EntityManager em = emf.createEntityManager();
            var tx = em.getTransaction();
            
            try {
                tx.begin();
                
                // Step 1: Delete PaymentTransactions for this table
                try {
                    System.out.println("Step 1: Deleting PaymentTransactions...");
                    int deletedPayments = em.createNativeQuery(
                        "DELETE FROM PaymentTransactions WHERE SessionID IN (" +
                        "SELECT SessionID FROM TableSessions WHERE TableID = ?)", 
                        Integer.class
                    ).setParameter(1, tableId).executeUpdate();
                    System.out.println("Deleted " + deletedPayments + " payment transactions");
                } catch (Exception e) {
                    System.out.println("Step 1 failed (continuing): " + e.getMessage());
                }
                
                // Step 2: Delete OrderDetails for this table
                try {
                    System.out.println("Step 2: Deleting OrderDetails...");
                    int deletedOrderDetails = em.createNativeQuery(
                        "DELETE FROM OrderDetails WHERE OrderID IN (" +
                        "SELECT o.OrderID FROM Orders o " +
                        "INNER JOIN TableSessions ts ON o.SessionID = ts.SessionID " +
                        "WHERE ts.TableID = ?)", 
                        Integer.class
                    ).setParameter(1, tableId).executeUpdate();
                    System.out.println("Deleted " + deletedOrderDetails + " order details");
                } catch (Exception e) {
                    System.out.println("Step 2 failed (continuing): " + e.getMessage());
                }
                
                // Step 3: Delete Orders for this table
                try {
                    System.out.println("Step 3: Deleting Orders...");
                    int deletedOrders = em.createNativeQuery(
                        "DELETE FROM Orders WHERE SessionID IN (" +
                        "SELECT SessionID FROM TableSessions WHERE TableID = ?)", 
                        Integer.class
                    ).setParameter(1, tableId).executeUpdate();
                    System.out.println("Deleted " + deletedOrders + " orders");
                } catch (Exception e) {
                    System.out.println("Step 3 failed (continuing): " + e.getMessage());
                }
                
                // Step 4: Delete TableSessions for this table
                try {
                    System.out.println("Step 4: Deleting TableSessions...");
                    int deletedSessions = em.createNativeQuery(
                        "DELETE FROM TableSessions WHERE TableID = ?", 
                        Integer.class
                    ).setParameter(1, tableId).executeUpdate();
                    System.out.println("Deleted " + deletedSessions + " table sessions");
                } catch (Exception e) {
                    System.out.println("Step 4 failed (continuing): " + e.getMessage());
                }
                
                tx.commit();
                System.out.println("All table related data deleted successfully");
                return true;
                
            } catch (Exception e) {
                System.err.println("❌ Error deleting table related data: " + e.getMessage());
                e.printStackTrace();
                if (tx.isActive()) {
                    tx.rollback();
                }
                return false;
            } finally {
                em.close();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting table related data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateTableStatus(UUID tableId, String status) {
        try {
            Table table = tableDAO.findById(tableId);
            if (table != null) {
                table.setStatus(status);
                return tableDAO.update(table);
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cập nhật trạng thái bàn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Statistics
    public int getTotalRooms() {
        try {
            List<Room> rooms = roomDAO.findAll();
            return rooms != null ? rooms.size() : 0;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đếm số phòng: " + e.getMessage());
            return 0;
        }
    }
    
    public int getTotalTables() {
        try {
            List<Table> tables = tableDAO.findAll();
            return tables != null ? tables.size() : 0;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đếm số bàn: " + e.getMessage());
            return 0;
        }
    }
    
    public int getAvailableTables() {
        try {
            List<Table> availableTables = tableDAO.findByStatus("Available");
            return availableTables != null ? availableTables.size() : 0;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đếm số bàn trống: " + e.getMessage());
            return 0;
        }
    }
    
    public int getOccupiedTables() {
        try {
            List<Table> occupiedTables = tableDAO.findByStatus("Occupied");
            return occupiedTables != null ? occupiedTables.size() : 0;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đếm số bàn đang sử dụng: " + e.getMessage());
            return 0;
        }
    }

    // TableSession operations
    public com.liteflow.model.inventory.TableSession getActiveSessionByTableId(UUID tableId) {
        EntityManager em = this.emf.createEntityManager();
        try {
            jakarta.persistence.Query q = em.createQuery(
                "SELECT ts FROM TableSession ts " +
                "WHERE ts.table.tableId = :tableId " +
                "AND (ts.status = 'Active' OR (ts.status IS NULL AND ts.checkOutTime IS NULL))"
            );
            q.setParameter("tableId", tableId);
            q.setMaxResults(1);
            return (com.liteflow.model.inventory.TableSession) q.getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy phiên hoạt động của bàn: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
    
    // Method to get table sessions (alias for existing method)
    public java.util.List<com.liteflow.model.inventory.TableSession> getTableSessions(UUID tableId) {
        return getTableSessionsByTableId(tableId);
    }
    
    // Method to get completed table sessions (invoices) - similar to cashier notification
    
    public java.util.List<com.liteflow.model.inventory.TableSession> getCompletedTableSessions(UUID tableId) {
        EntityManager em = this.emf.createEntityManager();
        try {
            // Query sessions like cashier does for notification history
            // Use JOIN FETCH to eagerly load table and createdBy to avoid lazy loading issues
            jakarta.persistence.Query q = em.createQuery(
                "SELECT DISTINCT s FROM TableSession s " +
                "LEFT JOIN FETCH s.table " +
                "LEFT JOIN FETCH s.createdBy " +
                "WHERE s.table.tableId = :tableId " +
                "AND s.status = 'Completed' " +
                "AND s.paymentStatus = 'Paid' " +
                "ORDER BY s.checkOutTime DESC"
            );
            q.setParameter("tableId", tableId);
            return q.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting completed table sessions: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    // Method to get table payments with eager loading (DEPRECATED - use getCompletedTableSessions)
    
    public java.util.List<com.liteflow.model.inventory.PaymentTransaction> getTablePayments(UUID tableId) {
        EntityManager em = this.emf.createEntityManager();
        try {
            // Use JOIN FETCH to eagerly load session and processedBy
            jakarta.persistence.Query q = em.createQuery(
                "SELECT DISTINCT pt FROM PaymentTransaction pt " +
                "LEFT JOIN FETCH pt.session ts " +
                "LEFT JOIN FETCH pt.processedBy " +
                "WHERE ts.table.tableId = :tableId " +
                "ORDER BY pt.processedAt DESC"
            );
            q.setParameter("tableId", tableId);
            return q.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting table payments: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    // Method to get order details for a session with eager loading
    
    public java.util.List<com.liteflow.model.inventory.OrderDetail> getOrderDetailsForSession(UUID sessionId) {
        EntityManager em = this.emf.createEntityManager();
        try {
            // Use JOIN FETCH to eagerly load productVariant and product
            jakarta.persistence.Query q = em.createQuery(
                "SELECT DISTINCT od FROM OrderDetail od " +
                "LEFT JOIN FETCH od.productVariant pv " +
                "LEFT JOIN FETCH pv.product p " +
                "JOIN od.order o " +
                "WHERE o.session.sessionId = :sessionId"
            );
            q.setParameter("sessionId", sessionId);
            return q.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting order details for session: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    // Method to get session orders
    
    public java.util.List<com.liteflow.model.inventory.Order> getSessionOrders(UUID sessionId) {
        EntityManager em = this.emf.createEntityManager();
        try {
            jakarta.persistence.Query q = em.createQuery(
                "SELECT o FROM Order o " +
                "WHERE o.session.sessionId = :sessionId " +
                "ORDER BY o.orderDate DESC"
            );
            q.setParameter("sessionId", sessionId);
            return q.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting session orders: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        } finally {
            em.close();
        }
    }
    
    // Method to get order details
    
    public java.util.List<com.liteflow.model.inventory.OrderDetail> getOrderDetails(UUID orderId) {
        EntityManager em = this.emf.createEntityManager();
        try {
            jakarta.persistence.Query q = em.createQuery(
                "SELECT od FROM OrderDetail od " +
                "WHERE od.order.orderId = :orderId " +
                "ORDER BY od.orderDetailId"
            );
            q.setParameter("orderId", orderId);
            return q.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting order details: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        } finally {
            em.close();
        }
    }

    
    public java.util.List<com.liteflow.model.inventory.TableSession> getTableSessionsByTableId(UUID tableId) {
        EntityManager em = this.emf.createEntityManager();
        try {
            jakarta.persistence.Query q = em.createQuery(
                "SELECT ts FROM TableSession ts " +
                "WHERE ts.table.tableId = :tableId " +
                "ORDER BY ts.checkInTime DESC"
            );
            q.setParameter("tableId", tableId);
            return q.getResultList();
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy lịch sử phiên của bàn: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        } finally {
            em.close();
        }
    }

    // Tổng giá trị các đơn đang phục vụ dựa trên tổng OrderDetail.totalPrice của các phiên Active
    public java.math.BigDecimal getTotalActiveSessionsAmount() {
        EntityManager em = this.emf.createEntityManager();
        try {
            jakarta.persistence.Query q = em.createQuery(
                "SELECT COALESCE(SUM(od.totalPrice), 0) " +
                "FROM OrderDetail od " +
                "JOIN od.order o " +
                "JOIN o.session s " +
                "WHERE (s.status = 'Active' OR (s.status IS NULL AND s.checkOutTime IS NULL))"
            );
            Object result = q.getSingleResult();
            if (result instanceof java.math.BigDecimal) {
                return (java.math.BigDecimal) result;
            }
            if (result instanceof Number) {
                return java.math.BigDecimal.valueOf(((Number) result).doubleValue());
            }
            return java.math.BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tính tổng giá trị phiên đang phục vụ: " + e.getMessage());
            return java.math.BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total completed orders (paid) for today
     */
    public long getCompletedOrdersToday() {
        EntityManager em = this.emf.createEntityManager();
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startDateTime = today.atStartOfDay();
            LocalDateTime endDateTime = today.atTime(LocalTime.MAX);
            
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(o) FROM com.liteflow.model.inventory.Order o " +
                "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                "AND o.paymentStatus = 'Paid'",
                Long.class
            );
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            Long result = query.getSingleResult();
            return result != null ? result : 0L;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy số đơn đã xong hôm nay: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total completed orders (paid) for yesterday
     */
    public long getCompletedOrdersYesterday() {
        EntityManager em = this.emf.createEntityManager();
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime startDateTime = yesterday.atStartOfDay();
            LocalDateTime endDateTime = yesterday.atTime(LocalTime.MAX);
            
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(o) FROM com.liteflow.model.inventory.Order o " +
                "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                "AND o.paymentStatus = 'Paid'",
                Long.class
            );
            query.setParameter("startDate", startDateTime);
            query.setParameter("endDate", endDateTime);
            
            Long result = query.getSingleResult();
            return result != null ? result : 0L;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy số đơn đã xong hôm qua: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get current table count for a specific room
     */
    public int getCurrentTableCountForRoom(UUID roomId) {
        EntityManager em = this.emf.createEntityManager();
        try {
            String jpql = "SELECT COUNT(t) FROM Table t WHERE t.room.roomId = :roomId AND (t.isActive = true OR t.isActive IS NULL)";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("roomId", roomId);
            Long count = query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            System.err.println("Error getting current table count for room: " + e.getMessage());
            return 0;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get current total capacity for a specific room
     */
    public int getCurrentTotalCapacityForRoom(UUID roomId) {
        EntityManager em = this.emf.createEntityManager();
        try {
            String jpql = "SELECT COALESCE(SUM(t.capacity), 0) FROM Table t WHERE t.room.roomId = :roomId AND (t.isActive = true OR t.isActive IS NULL)";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("roomId", roomId);
            Long totalCapacity = query.getSingleResult();
            return totalCapacity != null ? totalCapacity.intValue() : 0;
        } catch (Exception e) {
            System.err.println("Error getting current total capacity for room: " + e.getMessage());
            return 0;
        } finally {
            em.close();
        }
    }
    
    // Helper methods for Excel import
    public Room getRoomByName(String name) {
        try {
            return roomDAO.findSingleByName(name);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tìm phòng theo tên: " + e.getMessage());
            return null;
        }
    }
    
    public Table getTableByNumber(String tableNumber) {
        try {
            return tableDAO.findByTableNumber(tableNumber);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tìm bàn theo số: " + e.getMessage());
            return null;
        }
    }
}
