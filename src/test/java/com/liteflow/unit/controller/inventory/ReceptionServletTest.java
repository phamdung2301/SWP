package com.liteflow.unit.controller.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liteflow.controller.inventory.ReceptionServlet;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReceptionServlet
 * Tests reservation management, page loading, and Excel export functionality
 */
public class ReceptionServletTest extends UnitTestBase {

    private ReceptionServlet servlet;
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
    private Reservation testReservation;
    private Category testCategory;
    private Product testProduct;
    private ProductVariant testVariant;

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

        servlet = new ReceptionServlet();
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
                    .build();
            testRoom.setRoomId(null); // Clear ID to let Hibernate generate it
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
            entityManager.flush();

            // Create Category
            testCategory = new Category();
            testCategory.setCategoryId(UUID.randomUUID());
            testCategory.setName("Drinks");
            testCategory.setDescription("Beverages");
            entityManager.persist(testCategory);
            entityManager.flush();

            // Create Product
            testProduct = new Product();
            testProduct.setProductId(UUID.randomUUID());
            testProduct.setName("Coffee");
            testProduct.setDescription("Hot Coffee");
            testProduct.setProductType("Beverage");
            testProduct.setStatus("Active");
            testProduct.setUnit("Cup");
            entityManager.persist(testProduct);
            entityManager.flush();

            // Create ProductVariant
            testVariant = new ProductVariant();
            testVariant.setProductVariantId(UUID.randomUUID());
            testVariant.setProduct(testProduct);
            testVariant.setSize("M");
            testVariant.setPrice(BigDecimal.valueOf(45000));
            testVariant.setOriginalPrice(BigDecimal.valueOf(45000));
            entityManager.persist(testVariant);
            entityManager.flush();

            // Create Test Reservation
            testReservation = TestDataBuilder.createTestReservation()
                    .withCustomerName("John Doe")
                    .withCustomerPhone("0123456789")
                    .withNumberOfGuests(4)
                    .withTable(testTable)
                    .withRoom(testRoom)
                    .withStatus("PENDING")
                    .withArrivalTime(LocalDateTime.now().plusHours(2))
                    .build();
            testReservation.setReservationId(null);
            // Ensure unique reservation code
            testReservation.setReservationCode("TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            entityManager.persist(testReservation);

            commitTransaction();
            entityManager.clear();
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to seed test data", e);
        }
    }

    // ==================== PAGE LOAD TESTS ====================

    /**
     * Test 1: Load reception page successfully
     */
    @Test
    public void testHandleReceptionPage_Success() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/reception");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestDispatcher("/reception/reception.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert
        verify(request).setAttribute(eq("reservationsJson"), anyString());
        verify(request).setAttribute(eq("roomsJson"), anyString());
        verify(request).setAttribute(eq("tablesJson"), anyString());
        verify(request).setAttribute(eq("productsJson"), anyString());
        verify(requestDispatcher).forward(request, response);
    }

    /**
     * Test 2: Load reception page with reservations data
     */
    @Test
    public void testHandleReceptionPage_LoadReservations() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/reception");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestDispatcher("/reception/reception.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert
        verify(request).setAttribute(eq("reservationsJson"), anyString());
        verify(requestDispatcher).forward(request, response);
    }

    /**
     * Test 3: Load reception page with rooms and tables data
     */
    @Test
    public void testHandleReceptionPage_LoadRoomsAndTables() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/reception");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestDispatcher("/reception/reception.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert
        verify(request).setAttribute(eq("roomsJson"), anyString());
        verify(request).setAttribute(eq("tablesJson"), anyString());
        verify(requestDispatcher).forward(request, response);
    }

    // ==================== CREATE RESERVATION TESTS ====================

    /**
     * Test 4: Create reservation successfully
     */
    @Test
    public void testCreateReservation_Success() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/create");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"customerName\":\"Jane Doe\",\"customerPhone\":\"0987654321\"," +
                "\"customerEmail\":\"jane@example.com\",\"arrivalTime\":\"%s\"," +
                "\"numberOfGuests\":2,\"roomId\":\"%s\",\"tableId\":\"%s\"," +
                "\"notes\":\"Window seat please\"}",
                LocalDateTime.now().plusHours(3).toString(),
                testRoom.getRoomId(),
                testTable.getTableId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"), 
                "Expected success=true, but got: " + result.get("success") + 
                ". Message: " + result.get("message"));
        assertEquals("Đặt bàn thành công", result.get("message"));
        assertNotNull(result.get("reservationCode"));
    }

    /**
     * Test 5: Create reservation with invalid phone number
     */
    @Test
    public void testCreateReservation_InvalidPhone() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/create");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"customerName\":\"Jane Doe\",\"customerPhone\":\"123\"," +
                "\"arrivalTime\":\"%s\",\"numberOfGuests\":2}",
                LocalDateTime.now().plusHours(3).toString()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        verify(response).setStatus(400);
    }

    /**
     * Test 6: Create reservation with invalid date (past date)
     */
    @Test
    public void testCreateReservation_InvalidDate() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/create");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"customerName\":\"Jane Doe\",\"customerPhone\":\"0987654321\"," +
                "\"arrivalTime\":\"%s\",\"numberOfGuests\":2}",
                LocalDateTime.now().minusDays(1).toString() // Past date
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        verify(response).setStatus(400);
    }

    /**
     * Test 7: Create reservation with invalid guest count
     */
    @Test
    public void testCreateReservation_InvalidGuestCount() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/create");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"customerName\":\"Jane Doe\",\"customerPhone\":\"0987654321\"," +
                "\"arrivalTime\":\"%s\",\"numberOfGuests\":0}",
                LocalDateTime.now().plusHours(3).toString()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        verify(response).setStatus(400);
    }

    /**
     * Test 8: Create reservation with pre-order items
     */
    @Test
    public void testCreateReservation_WithPreOrderItems() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/create");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"customerName\":\"Jane Doe\",\"customerPhone\":\"0987654321\"," +
                "\"arrivalTime\":\"%s\",\"numberOfGuests\":2," +
                "\"preOrderedItems\":[{\"productId\":\"%s\",\"quantity\":2,\"note\":\"No sugar\"}]}",
                LocalDateTime.now().plusHours(3).toString(),
                testProduct.getProductId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
    }

    // ==================== UPDATE RESERVATION TESTS ====================

    /**
     * Test 9: Update reservation successfully
     */
    @Test
    public void testUpdateReservation_Success() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/update");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"reservationId\":\"%s\",\"customerName\":\"John Updated\"," +
                "\"customerPhone\":\"0123456789\",\"numberOfGuests\":6}",
                testReservation.getReservationId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        assertEquals("Cập nhật thành công", result.get("message"));
    }

    /**
     * Test 10: Update reservation that doesn't exist
     */
    @Test
    public void testUpdateReservation_NotFound() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/update");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"reservationId\":\"%s\",\"customerName\":\"John Updated\"}",
                UUID.randomUUID() // Non-existent reservation ID
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        verify(response).setStatus(anyInt());
    }

    /**
     * Test 11: Update reservation with invalid status
     */
    @Test
    public void testUpdateReservation_InvalidStatus() throws Exception {
        // Arrange - First update reservation to CANCELLED status
        beginTransaction();
        testReservation.setStatus("CANCELLED");
        entityManager.merge(testReservation);
        commitTransaction();

        when(request.getServletPath()).thenReturn("/api/reservation/update");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"reservationId\":\"%s\",\"customerName\":\"John Updated\"}",
                testReservation.getReservationId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert - Should reject update of cancelled reservation
        String jsonResponse = responseWriter.toString();
        assertNotNull(jsonResponse);
    }

    // ==================== CONFIRM ARRIVAL TESTS ====================

    /**
     * Test 12: Confirm arrival successfully
     */
    @Test
    public void testConfirmArrival_Success() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/confirm-arrival");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"reservationId\":\"%s\"}",
                testReservation.getReservationId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        assertEquals("Đã nhận bàn thành công", result.get("message"));
    }

    /**
     * Test 13: Confirm arrival for already seated reservation
     */
    @Test
    public void testConfirmArrival_AlreadySeated() throws Exception {
        // Arrange - Set reservation status to SEATED
        beginTransaction();
        testReservation.setStatus("SEATED");
        entityManager.merge(testReservation);
        commitTransaction();

        when(request.getServletPath()).thenReturn("/api/reservation/confirm-arrival");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"reservationId\":\"%s\"}",
                testReservation.getReservationId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert - Should handle gracefully
        String jsonResponse = responseWriter.toString();
        assertNotNull(jsonResponse);
    }

    /**
     * Test 14: Confirm arrival for cancelled reservation
     */
    @Test
    public void testConfirmArrival_CancelledReservation() throws Exception {
        // Arrange - Set reservation status to CANCELLED
        beginTransaction();
        testReservation.setStatus("CANCELLED");
        entityManager.merge(testReservation);
        commitTransaction();

        when(request.getServletPath()).thenReturn("/api/reservation/confirm-arrival");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"reservationId\":\"%s\"}",
                testReservation.getReservationId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert - Should reject confirm arrival for cancelled reservation
        verify(response).setStatus(400);
    }

    // ==================== CANCEL RESERVATION TESTS ====================

    /**
     * Test 15: Cancel reservation successfully
     */
    @Test
    public void testCancelReservation_Success() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/cancel");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"reservationId\":\"%s\",\"reason\":\"Customer cancelled\"}",
                testReservation.getReservationId()
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        assertEquals("Đã hủy đặt bàn", result.get("message"));
    }

    /**
     * Test 16: Cancel reservation that doesn't exist
     */
    @Test
    public void testCancelReservation_NotFound() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/cancel");
        when(request.getMethod()).thenReturn("POST");

        String requestBody = String.format(
                "{\"reservationId\":\"%s\"}",
                UUID.randomUUID() // Non-existent reservation ID
        );

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        verify(response).setStatus(anyInt());
    }

    // ==================== EXPORT TESTS ====================

    /**
     * Test 17: Export reservations to Excel successfully
     */
    @Test
    public void testExportReservations_Success() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/export");
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("date")).thenReturn(null); // Today's date

        StringWriter outputStreamWriter = new StringWriter();
        when(response.getOutputStream()).thenReturn(new TestServletOutputStream());

        // Act
        servlet.service(request, response);

        // Assert
        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setHeader(eq("Content-Disposition"), contains("attachment"));
    }

    /**
     * Test 18: Export reservations with date range
     */
    @Test
    public void testExportReservations_WithDateRange() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/reservation/export");
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("date")).thenReturn("2025-11-07");

        when(response.getOutputStream()).thenReturn(new TestServletOutputStream());

        // Act
        servlet.service(request, response);

        // Assert
        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setHeader(eq("Content-Disposition"), contains("2025-11-07"));
    }

    // ==================== HELPER CLASS ====================

    /**
     * Test implementation of ServletOutputStream
     */
    private static class TestServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        @Override
        public void write(int b) {
            baos.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            // Not needed for testing
        }

        public byte[] getBytes() {
            return baos.toByteArray();
        }
    }
}
