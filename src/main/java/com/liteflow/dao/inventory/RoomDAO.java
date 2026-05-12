package com.liteflow.dao.inventory;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.inventory.Room;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

public class RoomDAO extends GenericDAO<Room, UUID> {
    
    public RoomDAO() {
        super(Room.class, UUID.class);
    }
    
    public List<Room> findAll() {
        return super.findAll();
    }
    
    public Room findById(UUID id) {
        return super.findById(id);
    }
    
    public boolean insert(Room room) {
        return super.insert(room);
    }
    
    public boolean update(Room room) {
        return super.update(room);
    }
    
    public boolean delete(UUID id) {
        return super.delete(id);
    }
    
    public List<Room> findByName(String name) {
        return super.findByName(name);
    }
    
    public Room findSingleByName(String name) {
        return super.findSingleByFieldIgnoreCase("name", name);
    }
    
    public boolean checkRoomHasRelatedData(UUID roomId) {
        EntityManager em = emf.createEntityManager();
        try {
            System.out.println("=== DEBUG: Checking related data for room: " + roomId);
            
            // Check tables
            Long tableCount = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM Tables WHERE RoomID = ?"
            ).setParameter(1, roomId).getSingleResult();
            System.out.println("Tables in room: " + tableCount);
            
            // Check table sessions
            Long sessionCount = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM TableSessions ts " +
                "INNER JOIN Tables t ON ts.TableID = t.TableID " +
                "WHERE t.RoomID = ?"
            ).setParameter(1, roomId).getSingleResult();
            System.out.println("Table sessions in room: " + sessionCount);
            
            // Check orders
            Long orderCount = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM Orders o " +
                "INNER JOIN TableSessions ts ON o.SessionID = ts.SessionID " +
                "INNER JOIN Tables t ON ts.TableID = t.TableID " +
                "WHERE t.RoomID = ?"
            ).setParameter(1, roomId).getSingleResult();
            System.out.println("Orders in room: " + orderCount);
            
            // Check payment transactions
            Long paymentCount = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM PaymentTransactions pt " +
                "INNER JOIN TableSessions ts ON pt.SessionID = ts.SessionID " +
                "INNER JOIN Tables t ON ts.TableID = t.TableID " +
                "WHERE t.RoomID = ?"
            ).setParameter(1, roomId).getSingleResult();
            System.out.println("Payment transactions in room: " + paymentCount);
            
            boolean hasData = (tableCount > 0 || sessionCount > 0 || orderCount > 0 || paymentCount > 0);
            System.out.println("Room has related data: " + hasData);
            
            return hasData;
        } catch (Exception e) {
            System.err.println("❌ Error checking related data: " + e.getMessage());
            e.printStackTrace();
            return true; // Assume has data if error
        } finally {
            em.close();
        }
    }
    
    public boolean deleteWithNativeSQL(UUID roomId) {
        EntityManager em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            System.out.println("=== DEBUG: RoomDAO.deleteWithNativeSQL ===");
            System.out.println("Room ID to delete: " + roomId);
            
            tx.begin();
            
            // Step 1: Delete all TableSessions for tables in this room
            try {
                System.out.println("Step 1: Deleting TableSessions...");
                int deletedSessions = em.createNativeQuery(
                    "DELETE FROM TableSessions WHERE TableID IN (SELECT TableID FROM Tables WHERE RoomID = ?)", 
                    Integer.class
                ).setParameter(1, roomId).executeUpdate();
                System.out.println("Deleted " + deletedSessions + " table sessions");
            } catch (Exception e) {
                System.out.println("Step 1 failed (continuing): " + e.getMessage());
            }
            
            // Step 2: Delete all OrderDetails for tables in this room
            try {
                System.out.println("Step 2: Deleting OrderDetails...");
                int deletedOrders = em.createNativeQuery(
                    "DELETE FROM OrderDetails WHERE OrderID IN (" +
                    "SELECT o.OrderID FROM Orders o " +
                    "INNER JOIN TableSessions ts ON o.SessionID = ts.SessionID " +
                    "INNER JOIN Tables t ON ts.TableID = t.TableID " +
                    "WHERE t.RoomID = ?)", 
                    Integer.class
                ).setParameter(1, roomId).executeUpdate();
                System.out.println("Deleted " + deletedOrders + " order details");
            } catch (Exception e) {
                System.out.println("Step 2 failed (continuing): " + e.getMessage());
            }
            
            // Step 3: Delete all Orders for tables in this room
            try {
                System.out.println("Step 3: Deleting Orders...");
                int deletedOrdersMain = em.createNativeQuery(
                    "DELETE FROM Orders WHERE SessionID IN (" +
                    "SELECT ts.SessionID FROM TableSessions ts " +
                    "INNER JOIN Tables t ON ts.TableID = t.TableID " +
                    "WHERE t.RoomID = ?)", 
                    Integer.class
                ).setParameter(1, roomId).executeUpdate();
                System.out.println("Deleted " + deletedOrdersMain + " orders");
            } catch (Exception e) {
                System.out.println("Step 3 failed (continuing): " + e.getMessage());
            }
            
            // Step 4: Delete all PaymentTransactions for tables in this room
            try {
                System.out.println("Step 4: Deleting PaymentTransactions...");
                int deletedPayments = em.createNativeQuery(
                    "DELETE FROM PaymentTransactions WHERE SessionID IN (" +
                    "SELECT ts.SessionID FROM TableSessions ts " +
                    "INNER JOIN Tables t ON ts.TableID = t.TableID " +
                    "WHERE t.RoomID = ?)", 
                    Integer.class
                ).setParameter(1, roomId).executeUpdate();
                System.out.println("Deleted " + deletedPayments + " payment transactions");
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
            
            // Step 6: Finally delete the room
            try {
                System.out.println("Step 6: Deleting Room...");
                int deletedRooms = em.createNativeQuery(
                    "DELETE FROM Rooms WHERE RoomID = ?", 
                    Integer.class
                ).setParameter(1, roomId).executeUpdate();
                System.out.println("Deleted " + deletedRooms + " rooms");
                
                tx.commit();
                
                boolean success = deletedRooms > 0;
                System.out.println("Native SQL delete success: " + success);
                return success;
            } catch (Exception e) {
                System.err.println("Step 6 failed (critical): " + e.getMessage());
                e.printStackTrace();
                tx.rollback();
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception in RoomDAO.deleteWithNativeSQL: " + e.getMessage());
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }
}
