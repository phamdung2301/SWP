package com.liteflow.unit.service.inventory;

import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.Room;
import com.liteflow.model.inventory.Table;
import com.liteflow.service.inventory.RoomTableService;
import com.liteflow.unit.base.UnitTestBase;
import com.liteflow.utils.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoomTableService
 * Tests the business logic for room and table operations, status updates, and capacity limits
 */
public class RoomTableServiceTest extends UnitTestBase {

    private RoomTableService roomTableService;

    // Test data
    private Room testRoom;
    private Table testTable1;
    private Table testTable2;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        try {
            // Setup BaseDAO.emf via reflection to use our test EntityManagerFactory
            Field emfField = BaseDAO.class.getDeclaredField("emf");
            emfField.setAccessible(true);
            emfField.set(null, entityManagerFactory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup BaseDAO.emf", e);
        }

        roomTableService = new RoomTableService();
        seedTestData();
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Override
    protected void cleanupDatabase() {
        // Cleanup database before seeding new data to avoid data accumulation
        if (entityManager == null || !entityManager.isOpen()) {
            return;
        }

        try {
            // Check if transaction is active, if not begin one
            boolean transactionActive = entityManager.getTransaction().isActive();
            if (!transactionActive) {
                entityManager.getTransaction().begin();
            }

            // Delete in correct order to respect foreign key constraints
            entityManager.createQuery("DELETE FROM OrderDetail").executeUpdate();
            entityManager.createQuery("DELETE FROM Order").executeUpdate();
            entityManager.createQuery("DELETE FROM TableSession").executeUpdate();
            entityManager.createQuery("DELETE FROM Table").executeUpdate();
            entityManager.createQuery("DELETE FROM Room").executeUpdate();

            if (!transactionActive) {
                entityManager.getTransaction().commit();
            }
            entityManager.clear();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            System.err.println("Error cleaning up database: " + e.getMessage());
        }
    }

    @Override
    protected void seedTestData() {
        // Cleanup first to ensure no leftover data from previous tests
        cleanupDatabase();
        
        beginTransaction();
        try {
            // Create test Room
            testRoom = TestDataBuilder.createTestRoom()
                    .withName("VIP Room")
                    .withDescription("VIP room for special guests")
                    .withTableCount(10)
                    .withTotalCapacity(40)
                    .build();
            entityManager.persist(testRoom);
            entityManager.flush();

            // Create test Table 1
            testTable1 = TestDataBuilder.createTestTable()
                    .withTableNumber("V001")
                    .withTableName("VIP Table 1")
                    .withRoom(testRoom)
                    .withCapacity(4)
                    .withStatus("Available")
                    .build();
            testTable1.setTableId(null);
            entityManager.persist(testTable1);
            entityManager.flush();

            // Create test Table 2
            testTable2 = TestDataBuilder.createTestTable()
                    .withTableNumber("V002")
                    .withTableName("VIP Table 2")
                    .withRoom(testRoom)
                    .withCapacity(6)
                    .withStatus("Occupied")
                    .build();
            testTable2.setTableId(null);
            entityManager.persist(testTable2);
            entityManager.flush();

            commitTransaction();
            entityManager.clear();
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to seed test data", e);
        }
    }

    // ==================== ROOM OPERATIONS TESTS ====================

    /**
     * Test 1: Get all rooms successfully
     */
    @Test
    public void testGetAllRooms_Success() {
        // Act
        List<Room> rooms = roomTableService.getAllRooms();

        // Assert
        assertNotNull(rooms);
        assertTrue(rooms.size() >= 1); // At least our test room

        // Verify our test room is in the list
        boolean foundTestRoom = rooms.stream()
                .anyMatch(r -> r.getName().equals("VIP Room"));
        assertTrue(foundTestRoom);
    }

    /**
     * Test 2: Get room by ID successfully
     */
    @Test
    public void testGetRoomById_Success() {
        // Act
        Room room = roomTableService.getRoomById(testRoom.getRoomId());

        // Assert
        assertNotNull(room);
        assertEquals("VIP Room", room.getName());
        assertEquals("VIP room for special guests", room.getDescription());
        assertEquals(10, room.getTableCount());
        assertEquals(40, room.getTotalCapacity());
    }

    /**
     * Test 3: Get room by ID - room not found
     */
    @Test
    public void testGetRoomById_NotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act
        Room room = roomTableService.getRoomById(nonExistentId);

        // Assert
        assertNull(room);
    }

    /**
     * Test 4: Add room with validation - success
     */
    @Test
    public void testAddRoom_Validation() {
        // Arrange
        Room newRoom = TestDataBuilder.createTestRoom()
                .withName("Main Hall")
                .withDescription("Main dining hall")
                .withTableCount(20)
                .withTotalCapacity(80)
                .build();

        // Act
        boolean result = roomTableService.addRoom(newRoom);

        // Assert
        assertTrue(result);

        // Verify room was added to database
        entityManager.clear();
        beginTransaction();
        Room savedRoom = entityManager.find(Room.class, newRoom.getRoomId());
        assertNotNull(savedRoom);
        assertEquals("Main Hall", savedRoom.getName());
        assertEquals("Main dining hall", savedRoom.getDescription());
        assertEquals(20, savedRoom.getTableCount());
        assertEquals(80, savedRoom.getTotalCapacity());
        commitTransaction();
    }

    /**
     * Test 5: Update room successfully
     */
    @Test
    public void testUpdateRoom_Success() {
        // Arrange
        beginTransaction();
        testRoom.setName("Updated VIP Room");
        testRoom.setDescription("Updated description");
        testRoom.setTableCount(15);
        testRoom.setTotalCapacity(60);
        commitTransaction();

        // Act
        boolean result = roomTableService.updateRoom(testRoom);

        // Assert
        assertTrue(result);

        // Verify room was updated in database
        entityManager.clear();
        beginTransaction();
        Room updatedRoom = entityManager.find(Room.class, testRoom.getRoomId());
        assertNotNull(updatedRoom);
        entityManager.refresh(updatedRoom);
        assertEquals("Updated VIP Room", updatedRoom.getName());
        assertEquals("Updated description", updatedRoom.getDescription());
        assertEquals(15, updatedRoom.getTableCount());
        assertEquals(60, updatedRoom.getTotalCapacity());
        commitTransaction();
    }

    /**
     * Test 6: Delete room successfully (without tables)
     */
    @Test
    public void testDeleteRoom_Success() {
        // Arrange - Create a new room without tables
        beginTransaction();
        Room roomToDelete = TestDataBuilder.createTestRoom()
                .withName("Empty Room")
                .withDescription("Room to be deleted")
                .withTableCount(0)
                .withTotalCapacity(0)
                .build();
        entityManager.persist(roomToDelete);
        UUID roomId = roomToDelete.getRoomId();
        commitTransaction();
        entityManager.clear();

        // Act
        boolean result = roomTableService.deleteRoom(roomId);

        // Assert
        assertTrue(result);

        // Verify room was deleted from database
        beginTransaction();
        Room deletedRoom = entityManager.find(Room.class, roomId);
        assertNull(deletedRoom);
        commitTransaction();
    }

    // ==================== TABLE OPERATIONS TESTS ====================

    /**
     * Test 7: Get all tables successfully
     */
    @Test
    public void testGetAllTables_Success() {
        // Act
        List<Table> tables = roomTableService.getAllTables();

        // Assert
        assertNotNull(tables);
        assertTrue(tables.size() >= 2); // At least our 2 test tables

        // Verify our test tables are in the list
        boolean foundTable1 = tables.stream()
                .anyMatch(t -> t.getTableNumber().equals("V001"));
        boolean foundTable2 = tables.stream()
                .anyMatch(t -> t.getTableNumber().equals("V002"));
        assertTrue(foundTable1);
        assertTrue(foundTable2);
    }

    /**
     * Test 8: Get tables by room ID successfully
     */
    @Test
    public void testGetTablesByRoomId_Success() {
        // Act
        List<Table> tables = roomTableService.getTablesByRoomId(testRoom.getRoomId());

        // Assert
        assertNotNull(tables);
        assertEquals(2, tables.size());

        // Verify all tables belong to the test room
        for (Table table : tables) {
            assertEquals(testRoom.getRoomId(), table.getRoom().getRoomId());
        }
    }

    /**
     * Test 9: Get table by ID successfully
     */
    @Test
    public void testGetTableById_Success() {
        // Act
        Table table = roomTableService.getTableById(testTable1.getTableId());

        // Assert
        assertNotNull(table);
        assertEquals("V001", table.getTableNumber());
        assertEquals("VIP Table 1", table.getTableName());
        assertEquals(4, table.getCapacity());
        assertEquals("Available", table.getStatus());
    }

    /**
     * Test 10: Get table by ID - table not found
     */
    @Test
    public void testGetTableById_NotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act
        Table table = roomTableService.getTableById(nonExistentId);

        // Assert
        assertNull(table);
    }

    /**
     * Test 11: Add table with validation - success
     */
    @Test
    public void testAddTable_Validation() {
        // Arrange
        Table newTable = TestDataBuilder.createTestTable()
                .withTableNumber("V003")
                .withTableName("VIP Table 3")
                .withRoom(testRoom)
                .withCapacity(8)
                .withStatus("Available")
                .build();

        // Act
        boolean result = roomTableService.addTable(newTable);

        // Assert
        assertTrue(result);

        // Verify table was added to database
        entityManager.clear();
        beginTransaction();
        Table savedTable = entityManager.find(Table.class, newTable.getTableId());
        assertNotNull(savedTable);
        assertEquals("V003", savedTable.getTableNumber());
        assertEquals("VIP Table 3", savedTable.getTableName());
        assertEquals(8, savedTable.getCapacity());
        assertEquals("Available", savedTable.getStatus());
        commitTransaction();
    }

    /**
     * Test 12: Update table successfully
     */
    @Test
    public void testUpdateTable_Success() {
        // Arrange
        beginTransaction();
        testTable1.setTableName("Updated VIP Table 1");
        testTable1.setCapacity(6);
        commitTransaction();

        // Act
        boolean result = roomTableService.updateTable(testTable1);

        // Assert
        assertTrue(result);

        // Verify table was updated in database
        entityManager.clear();
        beginTransaction();
        Table updatedTable = entityManager.find(Table.class, testTable1.getTableId());
        assertNotNull(updatedTable);
        entityManager.refresh(updatedTable);
        assertEquals("Updated VIP Table 1", updatedTable.getTableName());
        assertEquals(6, updatedTable.getCapacity());
        commitTransaction();
    }

    /**
     * Test 13: Delete table successfully
     */
    @Test
    public void testDeleteTable_Success() {
        // Arrange - Create a new table to delete
        beginTransaction();
        Table tableToDelete = TestDataBuilder.createTestTable()
                .withTableNumber("V999")
                .withTableName("Table to delete")
                .withRoom(testRoom)
                .withCapacity(2)
                .withStatus("Available")
                .build();
        tableToDelete.setTableId(null);
        entityManager.persist(tableToDelete);
        entityManager.flush();
        UUID tableId = tableToDelete.getTableId();
        commitTransaction();
        entityManager.clear();

        // Act
        boolean result = roomTableService.deleteTable(tableId);

        // Assert
        assertTrue(result);

        // Verify table was deleted from database
        beginTransaction();
        Table deletedTable = entityManager.find(Table.class, tableId);
        assertNull(deletedTable);
        commitTransaction();
    }

    // ==================== TABLE STATUS UPDATE TESTS ====================

    /**
     * Test 14: Update table status to Available successfully
     */
    @Test
    public void testUpdateTableStatus_Available() {
        // Arrange - testTable2 is currently Occupied
        assertEquals("Occupied", testTable2.getStatus());

        // Act
        boolean result = roomTableService.updateTableStatus(
                testTable2.getTableId(),
                "Available"
        );

        // Assert
        assertTrue(result);

        // Verify status was updated in database
        entityManager.clear();
        beginTransaction();
        Table updatedTable = entityManager.find(Table.class, testTable2.getTableId());
        assertNotNull(updatedTable);
        entityManager.refresh(updatedTable);
        assertEquals("Available", updatedTable.getStatus());
        commitTransaction();
    }

    /**
     * Test 15: Update table status to Occupied successfully
     */
    @Test
    public void testUpdateTableStatus_Occupied() {
        // Arrange - testTable1 is currently Available
        assertEquals("Available", testTable1.getStatus());

        // Act
        boolean result = roomTableService.updateTableStatus(
                testTable1.getTableId(),
                "Occupied"
        );

        // Assert
        assertTrue(result);

        // Verify status was updated in database
        entityManager.clear();
        beginTransaction();
        Table updatedTable = entityManager.find(Table.class, testTable1.getTableId());
        assertNotNull(updatedTable);
        entityManager.refresh(updatedTable);
        assertEquals("Occupied", updatedTable.getStatus());
        commitTransaction();
    }

    /**
     * Test 16: Update table status to Reserved successfully
     */
    @Test
    public void testUpdateTableStatus_Reserved() {
        // Arrange - testTable1 is currently Available
        assertEquals("Available", testTable1.getStatus());

        // Act
        boolean result = roomTableService.updateTableStatus(
                testTable1.getTableId(),
                "Reserved"
        );

        // Assert
        assertTrue(result);

        // Verify status was updated in database
        entityManager.clear();
        beginTransaction();
        Table updatedTable = entityManager.find(Table.class, testTable1.getTableId());
        assertNotNull(updatedTable);
        entityManager.refresh(updatedTable);
        assertEquals("Reserved", updatedTable.getStatus());
        commitTransaction();
    }

    /**
     * Test 17: Update table status with non-existent table ID
     */
    @Test
    public void testUpdateTableStatus_TableNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act
        boolean result = roomTableService.updateTableStatus(nonExistentId, "Available");

        // Assert
        assertFalse(result);
    }

    // ==================== STATISTICS TESTS ====================

    /**
     * Test 18: Get total rooms count
     */
    @Test
    public void testGetTotalRooms() {
        // Act
        int totalRooms = roomTableService.getTotalRooms();

        // Assert
        assertTrue(totalRooms >= 1); // At least our test room
    }

    /**
     * Test 19: Get total tables count
     */
    @Test
    public void testGetTotalTables() {
        // Act
        int totalTables = roomTableService.getTotalTables();

        // Assert
        assertTrue(totalTables >= 2); // At least our 2 test tables
    }

    /**
     * Test 20: Get available tables count
     */
    @Test
    public void testGetAvailableTables() {
        // Act
        int availableTables = roomTableService.getAvailableTables();

        // Assert
        assertTrue(availableTables >= 1); // At least testTable1 is Available
    }

    /**
     * Test 21: Get occupied tables count
     */
    @Test
    public void testGetOccupiedTables() {
        // Act
        int occupiedTables = roomTableService.getOccupiedTables();

        // Assert
        assertTrue(occupiedTables >= 1); // At least testTable2 is Occupied
    }

    // ==================== CAPACITY LIMIT TESTS ====================

    /**
     * Test 22: Get current table count for room
     */
    @Test
    public void testGetCurrentTableCountForRoom() {
        // Act
        int currentCount = roomTableService.getCurrentTableCountForRoom(testRoom.getRoomId());

        // Assert
        assertEquals(2, currentCount); // We have 2 active tables in test room
    }

    /**
     * Test 23: Get current total capacity for room
     */
    @Test
    public void testGetCurrentTotalCapacityForRoom() {
        // Act
        int currentCapacity = roomTableService.getCurrentTotalCapacityForRoom(testRoom.getRoomId());

        // Assert
        assertEquals(10, currentCapacity); // testTable1 (4) + testTable2 (6) = 10
    }

    /**
     * Test 24: Get current table count for room with no tables
     */
    @Test
    public void testGetCurrentTableCountForRoom_NoTables() {
        // Arrange - Create a room with no tables
        beginTransaction();
        Room emptyRoom = TestDataBuilder.createTestRoom()
                .withName("Empty Room")
                .withTableCount(0)
                .withTotalCapacity(0)
                .build();
        entityManager.persist(emptyRoom);
        UUID emptyRoomId = emptyRoom.getRoomId();
        commitTransaction();
        entityManager.clear();

        // Act
        int currentCount = roomTableService.getCurrentTableCountForRoom(emptyRoomId);

        // Assert
        assertEquals(0, currentCount);
    }

    /**
     * Test 25: Get current total capacity for room with no tables
     */
    @Test
    public void testGetCurrentTotalCapacityForRoom_NoTables() {
        // Arrange - Create a room with no tables
        beginTransaction();
        Room emptyRoom = TestDataBuilder.createTestRoom()
                .withName("Empty Room 2")
                .withTableCount(0)
                .withTotalCapacity(0)
                .build();
        entityManager.persist(emptyRoom);
        UUID emptyRoomId = emptyRoom.getRoomId();
        commitTransaction();
        entityManager.clear();

        // Act
        int currentCapacity = roomTableService.getCurrentTotalCapacityForRoom(emptyRoomId);

        // Assert
        assertEquals(0, currentCapacity);
    }

    // ==================== HELPER METHOD TESTS ====================

    /**
     * Test 26: Get room by name successfully
     */
    @Test
    public void testGetRoomByName_Success() {
        // Act
        Room room = roomTableService.getRoomByName("VIP Room");

        // Assert
        assertNotNull(room);
        assertEquals("VIP Room", room.getName());
        assertEquals(testRoom.getRoomId(), room.getRoomId());
    }

    /**
     * Test 27: Get room by name - not found
     */
    @Test
    public void testGetRoomByName_NotFound() {
        // Act
        Room room = roomTableService.getRoomByName("Non-existent Room");

        // Assert
        assertNull(room);
    }

    /**
     * Test 28: Get table by table number successfully
     */
    @Test
    public void testGetTableByNumber_Success() {
        // Act
        Table table = roomTableService.getTableByNumber("V001");

        // Assert
        assertNotNull(table);
        assertEquals("V001", table.getTableNumber());
        assertEquals("VIP Table 1", table.getTableName());
        assertEquals(testTable1.getTableId(), table.getTableId());
    }

    /**
     * Test 29: Get table by table number - not found
     */
    @Test
    public void testGetTableByNumber_NotFound() {
        // Act
        Table table = roomTableService.getTableByNumber("NON-EXISTENT");

        // Assert
        assertNull(table);
    }

    /**
     * Test 30: Add multiple tables and verify room capacity tracking
     */
    @Test
    public void testAddMultipleTables_CapacityTracking() {
        // Arrange - Create a new room
        beginTransaction();
        Room newRoom = TestDataBuilder.createTestRoom()
                .withName("Test Capacity Room")
                .withTableCount(5)
                .withTotalCapacity(20)
                .build();
        entityManager.persist(newRoom);
        UUID newRoomId = newRoom.getRoomId();
        commitTransaction();
        entityManager.clear();

        // Act - Add 3 tables with different capacities
        Table table1 = TestDataBuilder.createTestTable()
                .withTableNumber("TC001")
                .withTableName("Capacity Test 1")
                .withRoom(newRoom)
                .withCapacity(4)
                .build();

        Table table2 = TestDataBuilder.createTestTable()
                .withTableNumber("TC002")
                .withTableName("Capacity Test 2")
                .withRoom(newRoom)
                .withCapacity(6)
                .build();

        Table table3 = TestDataBuilder.createTestTable()
                .withTableNumber("TC003")
                .withTableName("Capacity Test 3")
                .withRoom(newRoom)
                .withCapacity(8)
                .build();

        roomTableService.addTable(table1);
        roomTableService.addTable(table2);
        roomTableService.addTable(table3);

        // Assert - Verify table count and total capacity
        int tableCount = roomTableService.getCurrentTableCountForRoom(newRoomId);
        int totalCapacity = roomTableService.getCurrentTotalCapacityForRoom(newRoomId);

        assertEquals(3, tableCount);
        assertEquals(18, totalCapacity); // 4 + 6 + 8 = 18
    }
}
