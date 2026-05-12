package com.liteflow.unit.controller.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liteflow.controller.inventory.RoomTableServlet;
import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.*;
import com.liteflow.unit.base.UnitTestBase;
import com.liteflow.utils.TestDataBuilder;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoomTableServlet
 * Tests CRUD operations for rooms and tables, validation logic
 */
public class RoomTableServletTest extends UnitTestBase {

    private RoomTableServlet servlet;
    private Gson gson;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletContext servletContext;

    @Mock
    private ServletConfig servletConfig;

    @Mock
    private RequestDispatcher requestDispatcher;

    private StringWriter responseWriter;
    private AutoCloseable mocks;

    // Test data
    private Room testRoom;
    private Table testTable;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        mocks = MockitoAnnotations.openMocks(this);

        try {
            // Setup BaseDAO.emf via reflection
            Field emfField = BaseDAO.class.getDeclaredField("emf");
            emfField.setAccessible(true);
            emfField.set(null, entityManagerFactory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup BaseDAO.emf", e);
        }

        servlet = new RoomTableServlet();
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

        // Setup mocks
        when(servletConfig.getServletContext()).thenReturn(servletContext);

        // Setup response writer
        responseWriter = new StringWriter();
        try {
            when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup response writer", e);
        }

        // Initialize servlet
        try {
            servlet.init(servletConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize servlet", e);
        }

        seedTestData();
    }

    @AfterEach
    @Override
    public void tearDown() {
        try {
            if (mocks != null) {
                mocks.close();
            }
        } catch (Exception e) {
            // Ignore
        }
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
            entityManager.createQuery("DELETE FROM ReservationItem").executeUpdate();
            entityManager.createQuery("DELETE FROM Reservation").executeUpdate();
            entityManager.createQuery("DELETE FROM OrderDetail").executeUpdate();
            entityManager.createQuery("DELETE FROM Order").executeUpdate();
            entityManager.createQuery("DELETE FROM TableSession").executeUpdate();
            entityManager.createQuery("DELETE FROM Table").executeUpdate();
            entityManager.createQuery("DELETE FROM Room").executeUpdate();
            entityManager.createQuery("DELETE FROM ProductStock").executeUpdate();
            entityManager.createQuery("DELETE FROM ProductCategory").executeUpdate();
            entityManager.createQuery("DELETE FROM ProductVariant").executeUpdate();
            entityManager.createQuery("DELETE FROM Product").executeUpdate();
            entityManager.createQuery("DELETE FROM Category").executeUpdate();
            entityManager.createQuery("DELETE FROM Inventory").executeUpdate();

            if (!transactionActive) {
                entityManager.getTransaction().commit();
            }
            entityManager.clear();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            System.err.println("Warning: Error cleaning up database: " + e.getMessage());
            // Don't throw - continue with test setup
        }
    }

    @Override
    protected void seedTestData() {
        beginTransaction();
        try {
            // Create Room
            testRoom = TestDataBuilder.createTestRoom()
                    .withName("Test Room")
                    .withTableCount(10)
                    .withTotalCapacity(40)
                    .build();
            entityManager.persist(testRoom);
            entityManager.flush();

            // Create Table
            testTable = TestDataBuilder.createTestTable()
                    .withTableNumber("001")
                    .withTableName("Table 1")
                    .withStatus("Available")
                    .withCapacity(4)
                    .build();
            testTable.setTableId(null);
            testTable.setRoom(testRoom);
            entityManager.persist(testTable);

            commitTransaction();
            entityManager.clear();
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to seed test data", e);
        }
    }

    // ==================== PAGE LOAD TEST ====================

    /**
     * Test 1: Load RoomTable page successfully
     */
    @Test
    public void testDoGet_LoadRoomsAndTables() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestDispatcher("/inventory/roomtable.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert
        verify(request).setAttribute(eq("rooms"), anyList());
        verify(request).setAttribute(eq("tables"), anyList());
        verify(requestDispatcher).forward(request, response);
    }

    // ==================== ADD ROOM TESTS ====================

    /**
     * Test 2: Add room successfully
     */
    @Test
    public void testAddRoom_Success() throws Exception {
        // Arrange
        when(request.getContentType()).thenReturn("application/json");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = "{\"action\":\"addRoom\",\"roomName\":\"New Room\"," +
                "\"roomDescription\":\"Test Room\",\"roomTableCount\":\"5\"," +
                "\"roomTotalCapacity\":\"20\"}";

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"),
                "Expected success=true, got: " + result.get("success") +
                ". Message: " + result.get("message"));
        assertEquals("Thêm phòng thành công!", result.get("message"));
        assertNotNull(result.get("roomId"));
    }

    /**
     * Test 3: Add room with invalid data
     */
    @Test
    public void testAddRoom_InvalidData() throws Exception {
        // Arrange - Empty room name
        when(request.getContentType()).thenReturn("application/json");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = "{\"action\":\"addRoom\",\"roomName\":\"\"," +
                "\"roomTableCount\":\"5\",\"roomTotalCapacity\":\"20\"}";

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Tên phòng không được để trống", result.get("message"));
    }

    // ==================== ADD TABLE TESTS ====================

    /**
     * Test 4: Add table successfully
     */
    @Test
    public void testAddTable_Success() throws Exception {
        // Arrange
        when(request.getContentType()).thenReturn("application/json");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"action\":\"addTable\",\"tableNumber\":\"002\"," +
                "\"tableName\":\"Table 2\",\"capacity\":\"4\",\"roomId\":\"%s\"}",
                testRoom.getRoomId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"),
                "Expected success=true. Message: " + result.get("message"));
        assertEquals("Thêm bàn thành công!", result.get("message"));
        assertNotNull(result.get("tableId"));
    }

    // ==================== DELETE ROOM TEST ====================

    /**
     * Test 5: Delete room successfully (cascade delete tables)
     */
    @Test
    public void testDeleteRoom_Success() throws Exception {
        // Arrange - Create a new room to delete
        beginTransaction();
        Room roomToDelete = TestDataBuilder.createTestRoom()
                .withName("Room to Delete")
                .withTableCount(5)
                .build();
        roomToDelete.setRoomId(null);
        entityManager.persist(roomToDelete);
        commitTransaction();
        entityManager.clear();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"action\":\"deleteRoom\",\"roomId\":\"%s\"}",
                roomToDelete.getRoomId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"),
                "Expected success=true. Message: " + result.get("message"));
        assertEquals("Xóa phòng thành công!", result.get("message"));

        // Verify room was deleted
        entityManager.clear();
        beginTransaction();
        Room deletedRoom = entityManager.find(Room.class, roomToDelete.getRoomId());
        assertNull(deletedRoom, "Room should be deleted from database");
        commitTransaction();
    }

    // ==================== DELETE TABLE TEST ====================

    /**
     * Test 6: Delete table successfully
     */
    @Test
    public void testDeleteTable_Success() throws Exception {
        // Arrange - Create a new table to delete
        beginTransaction();
        Table tableToDelete = TestDataBuilder.createTestTable()
                .withTableNumber("999")
                .withTableName("Table to Delete")
                .withCapacity(4)
                .build();
        tableToDelete.setTableId(null);
        tableToDelete.setRoom(testRoom);
        entityManager.persist(tableToDelete);
        commitTransaction();
        entityManager.clear();

        when(request.getContentType()).thenReturn("application/json");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"action\":\"deleteTable\",\"tableId\":\"%s\"}",
                tableToDelete.getTableId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"),
                "Expected success=true. Message: " + result.get("message"));
        assertEquals("Xóa bàn thành công!", result.get("message"));

        // Verify table was deleted
        entityManager.clear();
        beginTransaction();
        Table deletedTable = entityManager.find(Table.class, tableToDelete.getTableId());
        assertNull(deletedTable, "Table should be deleted from database");
        commitTransaction();
    }
}
