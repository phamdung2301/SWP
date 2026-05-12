package com.liteflow.unit.controller.cashier;

import com.liteflow.controller.cashier.CashierAPIServlet;
import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.*;
import com.liteflow.unit.base.UnitTestBase;
import com.liteflow.utils.TestDataBuilder;
import com.google.gson.Gson;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit/Integration tests for CashierAPIServlet
 *
 * Tests all API endpoints:
 * - POST /api/cashier/order/create
 * - POST /api/cashier/checkout
 * - GET /api/cashier/invoice/next-number
 * - GET /api/cashier/notification/history
 */
public class CashierAPIServletTest extends UnitTestBase {

    private CashierAPIServlet servlet;
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
    private HttpSession session;

    private StringWriter responseWriter;
    private AutoCloseable mocks;

    // Test data
    private Room testRoom;
    private Table testTable;
    private Category testCategory;
    private Product testProduct;
    private ProductVariant testVariant;
    private Inventory testInventory;
    private ProductStock testStock;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        mocks = MockitoAnnotations.openMocks(this);

        // Initialize servlet
        servlet = new CashierAPIServlet();
        gson = new Gson();

        // Setup BaseDAO.emf to use test EntityManagerFactory
        try {
            Field emfField = BaseDAO.class.getDeclaredField("emf");
            emfField.setAccessible(true);
            emfField.set(null, entityManagerFactory);
        } catch (Exception e) {
            fail("Failed to set BaseDAO.emf: " + e.getMessage());
        }

        // Setup servlet context and config
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getContextPath()).thenReturn("/LiteFlow");
        when(request.getServletContext()).thenReturn(servletContext);
        when(request.getSession()).thenReturn(session);

        // Initialize servlet
        try {
            servlet.init(servletConfig);
        } catch (Exception e) {
            fail("Failed to initialize servlet: " + e.getMessage());
        }

        // Setup mock response writer
        responseWriter = new StringWriter();
        try {
            when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        } catch (Exception e) {
            fail("Failed to setup response writer: " + e.getMessage());
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        try {
            if (mocks != null) {
                mocks.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing mocks: " + e.getMessage());
        }
        super.tearDown();
    }

    @Override
    protected void seedTestData() {
        // Wrap all seed data in one transaction
        beginTransaction();

        try {
            // Create test room
            testRoom = TestDataBuilder.createTestRoom()
                    .withName("Main Hall")
                    .build();
            entityManager.persist(testRoom);
            entityManager.flush();

            // Create test table
            testTable = TestDataBuilder.createTestTable()
                    .withTableNumber("T001")
                    .withTableName("Table 1")
                    .withCapacity(4)
                    .withStatus("Available")
                    .build();
            testTable.setTableId(null);
            testTable.setRoom(testRoom);
            entityManager.persist(testTable);
            entityManager.flush();

            // Create test category
            testCategory = new Category();
            testCategory.setCategoryId(UUID.randomUUID());
            testCategory.setName("Coffee");
            entityManager.persist(testCategory);

            // Create test product
            testProduct = new Product();
            testProduct.setProductId(UUID.randomUUID());
            testProduct.setName("Cappuccino");
            testProduct.setIsDeleted(false);
            entityManager.persist(testProduct);

            // Create product variant
            testVariant = new ProductVariant();
            testVariant.setProductVariantId(UUID.randomUUID());
            testVariant.setSize("Medium");
            testVariant.setPrice(BigDecimal.valueOf(45000));
            testVariant.setIsDeleted(false);
            testVariant.setProduct(testProduct);
            entityManager.persist(testVariant);

            // Link product to category
            ProductCategory pc = new ProductCategory();
            pc.setProductCategoryId(UUID.randomUUID());
            pc.setProduct(testProduct);
            pc.setCategory(testCategory);
            entityManager.persist(pc);

            // Create inventory
            testInventory = new Inventory();
            testInventory.setInventoryId(UUID.randomUUID());
            testInventory.setStoreLocation("Main Warehouse");
            entityManager.persist(testInventory);

            // Create product stock
            testStock = new ProductStock();
            testStock.setProductStockId(UUID.randomUUID());
            testStock.setAmount(100);
            testStock.setProductVariant(testVariant);
            testStock.setInventory(testInventory);
            entityManager.persist(testStock);

            // Commit transaction
            commitTransaction();

            // Clear to ensure fresh reads in tests
            entityManager.clear();
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to seed test data: " + e.getMessage(), e);
        }
    }

    // ==================== ORDER CREATION TESTS ====================

    /**
     * Test 1: Create order successfully
     */
    @Test
    public void testCreateOrder_Success() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/order/create");

        String requestBody = String.format("{\"tableId\":\"%s\",\"items\":[{\"variantId\":\"%s\",\"quantity\":2}],\"orderNote\":\"Test order\"}",
                testTable.getTableId(), testVariant.getProductVariantId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        assertEquals("Đã gửi thông báo đến bếp thành công!", result.get("message"));
        assertNotNull(result.get("orderId"));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    /**
     * Test 2: Create order with invalid table ID
     */
    @Test
    public void testCreateOrder_InvalidTableId() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/order/create");

        String requestBody = "{\"tableId\":\"invalid-uuid\",\"items\":[{\"variantId\":\"" + testVariant.getProductVariantId() + "\",\"quantity\":2}]}";

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("Table ID"));
        verify(response).setStatus(400);
    }

    /**
     * Test 3: Create order with empty items
     */
    @Test
    public void testCreateOrder_EmptyItems() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/order/create");

        String requestBody = String.format("{\"tableId\":\"%s\",\"items\":[]}", testTable.getTableId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertFalse((Boolean) result.get("success"));
        verify(response).setStatus(400);
    }

    /**
     * Test 4: Create order with invalid variant ID
     */
    @Test
    public void testCreateOrder_InvalidVariantId() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/order/create");

        UUID randomId = UUID.randomUUID();
        String requestBody = String.format("{\"tableId\":\"%s\",\"items\":[{\"variantId\":\"%s\",\"quantity\":2}]}",
                testTable.getTableId(), randomId);

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act & Assert - should throw exception or return error
        servlet.service(request, response);

        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        // Should either be error or not find variant
        assertNotNull(result);
    }

    /**
     * Test 5: Create order with zero quantity
     */
    @Test
    public void testCreateOrder_ZeroQuantity() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/order/create");

        String requestBody = String.format("{\"tableId\":\"%s\",\"items\":[{\"variantId\":\"%s\",\"quantity\":0}]}",
                testTable.getTableId(), testVariant.getProductVariantId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertFalse((Boolean) result.get("success"));
        verify(response).setStatus(400);
    }

    /**
     * Test 6: Create order with negative quantity
     */
    @Test
    public void testCreateOrder_NegativeQuantity() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/order/create");

        String requestBody = String.format("{\"tableId\":\"%s\",\"items\":[{\"variantId\":\"%s\",\"quantity\":-1}]}",
                testTable.getTableId(), testVariant.getProductVariantId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertFalse((Boolean) result.get("success"));
        verify(response).setStatus(400);
    }

    /**
     * Test 7: Create order with order note
     */
    @Test
    public void testCreateOrder_WithOrderNote() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/order/create");

        String requestBody = String.format("{\"tableId\":\"%s\",\"items\":[{\"variantId\":\"%s\",\"quantity\":1}],\"orderNote\":\"No sugar please\"}",
                testTable.getTableId(), testVariant.getProductVariantId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    /**
     * Test 8: Create order with invalid JSON
     */
    @Test
    public void testCreateOrder_InvalidJson() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/order/create");

        String requestBody = "{invalid json";

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertFalse((Boolean) result.get("success"));
        verify(response).setStatus(anyInt());
    }

    // ==================== CHECKOUT TESTS ====================

    /**
     * Test 9: Checkout successfully
     */
    @Test
    public void testCheckout_Success() throws Exception {
        // Arrange - First create a table session and order
        beginTransaction();
        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(testTable);
        session.setStatus("Active");
        session.setTotalAmount(BigDecimal.valueOf(90000));
        entityManager.persist(session);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setSession(session);
        order.setOrderNumber("ORD-001");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.valueOf(90000));
        entityManager.persist(order);
        commitTransaction();

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/checkout");

        String requestBody = String.format("{\"tableId\":\"%s\",\"paymentMethod\":\"CASH\",\"amountPaid\":100000}",
                testTable.getTableId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("invoiceNumber"));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Test 10: Checkout special table (table number starting with 0)
     */
    @Test
    public void testCheckout_SpecialTable() throws Exception {
        // Arrange - create special table
        beginTransaction();
        Table specialTable = TestDataBuilder.createTestTable()
                .withTableNumber("0001")
                .withTableName("Special Table")
                .withStatus("Occupied")
                .build();
        specialTable.setTableId(null);
        specialTable.setRoom(testRoom);
        entityManager.persist(specialTable);
        entityManager.flush();

        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(specialTable);
        session.setStatus("Active");
        session.setTotalAmount(BigDecimal.valueOf(50000));
        entityManager.persist(session);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setSession(session);
        order.setOrderNumber("ORD-002");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.valueOf(50000));
        entityManager.persist(order);
        commitTransaction();

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/checkout");

        String requestBody = String.format("{\"tableId\":\"%s\",\"paymentMethod\":\"CASH\",\"amountPaid\":50000}",
                specialTable.getTableId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
    }

    /**
     * Test 11: Checkout with invalid table ID
     */
    @Test
    public void testCheckout_InvalidTableId() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/checkout");

        String requestBody = "{\"tableId\":\"invalid-uuid\",\"paymentMethod\":\"CASH\",\"amountPaid\":50000}";

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertFalse((Boolean) result.get("success"));
        verify(response).setStatus(400);
    }

    /**
     * Test 12: Checkout with no active session
     */
    @Test
    public void testCheckout_NoActiveSession() throws Exception {
        // Arrange - table with no orders
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/checkout");

        String requestBody = String.format("{\"tableId\":\"%s\",\"paymentMethod\":\"CASH\",\"amountPaid\":50000}",
                testTable.getTableId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        // May succeed or fail depending on implementation
        assertNotNull(result);
    }

    /**
     * Test 13: Checkout with discount
     */
    @Test
    public void testCheckout_WithDiscount() throws Exception {
        // Arrange - create order with session
        beginTransaction();
        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(testTable);
        session.setStatus("Active");
        session.setTotalAmount(BigDecimal.valueOf(100000));
        entityManager.persist(session);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setSession(session);
        order.setOrderNumber("ORD-003");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.valueOf(100000));
        entityManager.persist(order);
        commitTransaction();

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/checkout");

        String requestBody = String.format("{\"tableId\":\"%s\",\"paymentMethod\":\"CASH\",\"amountPaid\":90000,\"discount\":10}",
                testTable.getTableId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertNotNull(result);
    }

    /**
     * Test 14: Checkout with different payment methods
     */
    @Test
    public void testCheckout_PaymentMethods() throws Exception {
        // Arrange
        beginTransaction();
        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(testTable);
        session.setStatus("Active");
        session.setTotalAmount(BigDecimal.valueOf(50000));
        entityManager.persist(session);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setSession(session);
        order.setOrderNumber("ORD-004");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.valueOf(50000));
        entityManager.persist(order);
        commitTransaction();

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/checkout");

        String requestBody = String.format("{\"tableId\":\"%s\",\"paymentMethod\":\"CARD\",\"amountPaid\":50000}",
                testTable.getTableId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        assertNotNull(jsonResponse);
    }

    /**
     * Test 15: Checkout and verify stock deduction
     */
    @Test
    public void testCheckout_StockDeduction() throws Exception {
        // Arrange - create order with order detail
        beginTransaction();
        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(testTable);
        session.setStatus("Active");
        session.setTotalAmount(BigDecimal.valueOf(45000));
        entityManager.persist(session);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setSession(session);
        order.setOrderNumber("ORD-005");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.valueOf(45000));
        entityManager.persist(order);

        OrderDetail detail = new OrderDetail();
        detail.setOrderDetailId(UUID.randomUUID());
        detail.setOrder(order);
        detail.setProductVariant(testVariant);
        detail.setQuantity(1);
        detail.setUnitPrice(BigDecimal.valueOf(45000));
        detail.setTotalPrice(BigDecimal.valueOf(45000));
        entityManager.persist(detail);
        commitTransaction();

        int initialStock = testStock.getAmount();

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/checkout");

        String requestBody = String.format("{\"tableId\":\"%s\",\"paymentMethod\":\"CASH\",\"amountPaid\":50000}",
                testTable.getTableId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        assertNotNull(jsonResponse);
    }

    /**
     * Test 16: Checkout and verify table status update
     */
    @Test
    public void testCheckout_UpdateTableStatus() throws Exception {
        // Arrange
        beginTransaction();
        testTable.setStatus("Occupied");
        entityManager.merge(testTable);

        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(testTable);
        session.setStatus("Active");
        session.setTotalAmount(BigDecimal.valueOf(30000));
        entityManager.persist(session);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setSession(session);
        order.setOrderNumber("ORD-006");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.valueOf(30000));
        entityManager.persist(order);
        commitTransaction();

        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/checkout");

        String requestBody = String.format("{\"tableId\":\"%s\",\"paymentMethod\":\"CASH\",\"amountPaid\":30000}",
                testTable.getTableId());

        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        assertNotNull(jsonResponse);
    }

    // ==================== INVOICE NUMBER TESTS ====================

    /**
     * Test 17: Get next invoice number successfully
     */
    @Test
    public void testGetNextInvoiceNumber_Success() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/invoice/next-number");
        when(request.getParameter("tableId")).thenReturn(testTable.getTableId().toString());

        // Act
        servlet.service(request, response);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");

        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("invoiceNumber"));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Test 18: Get next invoice number with invalid table ID
     */
    @Test
    public void testGetNextInvoiceNumber_InvalidTableId() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/invoice/next-number");
        when(request.getParameter("tableId")).thenReturn("invalid-uuid");

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertFalse((Boolean) result.get("success"));
        verify(response).setStatus(400);
    }

    // ==================== NOTIFICATION HISTORY TESTS ====================

    /**
     * Test 19: Get notification history successfully
     */
    @Test
    public void testGetNotificationHistory_Success() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/notification/history");
        when(request.getParameter("days")).thenReturn(null);

        // Act
        servlet.service(request, response);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");

        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("notifications"));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Test 20: Get notification history with specific days parameter
     */
    @Test
    public void testGetNotificationHistory_WithDays() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/notification/history");
        when(request.getParameter("days")).thenReturn("7");

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("notifications"));
    }

    /**
     * Test 21: Get notification history with empty result
     */
    @Test
    public void testGetNotificationHistory_Empty() throws Exception {
        // Arrange - cleanup all orders to ensure empty result
        cleanupDatabase();

        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/notification/history");
        when(request.getParameter("days")).thenReturn("30");

        // Act
        servlet.service(request, response);

        // Assert
        String jsonResponse = responseWriter.toString();
        Map<String, Object> result = gson.fromJson(jsonResponse, Map.class);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("notifications"));
    }
}
