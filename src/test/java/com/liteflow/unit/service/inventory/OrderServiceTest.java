package com.liteflow.unit.service.inventory;

import com.google.gson.Gson;
import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.*;
import com.liteflow.service.inventory.OrderService;
import com.liteflow.unit.base.UnitTestBase;
import com.liteflow.utils.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderService
 * Tests the business logic for order creation and management
 */
public class OrderServiceTest extends UnitTestBase {

    private OrderService orderService;
    private Gson gson;

    // Test data
    private Room testRoom;
    private Table testTable;
    private Category testCategory;
    private Product testProduct;
    private ProductVariant testVariant;
    private ProductCategory testProductCategory;
    private Inventory testInventory;
    private ProductStock testStock;

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

        orderService = new OrderService();
        gson = new Gson();

        seedTestData();
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
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

            // Create ProductCategory junction
            testProductCategory = new ProductCategory();
            testProductCategory.setProductCategoryId(UUID.randomUUID());
            testProductCategory.setProduct(testProduct);
            testProductCategory.setCategory(testCategory);
            entityManager.persist(testProductCategory);
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

            // Create Inventory
            testInventory = new Inventory();
            testInventory.setInventoryId(UUID.randomUUID());
            testInventory.setStoreLocation("Main Store");
            entityManager.persist(testInventory);
            entityManager.flush();

            // Create ProductStock
            testStock = new ProductStock();
            testStock.setProductStockId(UUID.randomUUID());
            testStock.setProductVariant(testVariant);
            testStock.setInventory(testInventory);
            testStock.setAmount(100);
            entityManager.persist(testStock);

            commitTransaction();
            entityManager.clear();
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to seed test data", e);
        }
    }

    // ==================== CREATE ORDER AND NOTIFY KITCHEN TESTS ====================

    /**
     * Test 1: Create order and notify kitchen successfully
     */
    @Test
    public void testCreateOrderAndNotifyKitchen_Success() {
        // Arrange
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("variantId", testVariant.getProductVariantId().toString());
        item.put("quantity", 2);
        item.put("note", "No ice");
        items.add(item);

        // Act
        Map<String, Object> result = orderService.createOrderAndNotifyKitchen(
                testTable.getTableId(),
                items,
                null,
                "Table 1 - Invoice 1",
                "Please hurry"
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.get("orderId"));
        assertNotNull(result.get("orderNumber"));

        String orderNumber = (String) result.get("orderNumber");
        assertTrue(orderNumber.startsWith("ORD"));

        // Verify order was created in database
        UUID orderId = (UUID) result.get("orderId");
        beginTransaction();
        Order order = entityManager.find(Order.class, orderId);
        assertNotNull(order);
        assertEquals("Pending", order.getStatus());
        assertEquals("Please hurry", order.getNotes());
        
        // ✅ Verify order details: 1 item with quantity=2 means 1 OrderDetail, not 2
        // Fetch order details to trigger lazy loading
        order.getOrderDetails().size();
        assertEquals(1, order.getOrderDetails().size());
        
        // Verify the detail has quantity 2
        OrderDetail detail = order.getOrderDetails().get(0);
        assertEquals(2, detail.getQuantity());
        assertEquals("No ice", detail.getSpecialInstructions());
        commitTransaction();
    }

    /**
     * Test 2: Create order should create session if not exists
     */
    @Test
    public void testCreateOrder_CreateSessionIfNotExists() {
        // Arrange
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("variantId", testVariant.getProductVariantId().toString());
        item.put("quantity", 1);
        items.add(item);

        // Verify no active session exists for this table
        beginTransaction();
        List<TableSession> sessions = entityManager.createQuery(
                "SELECT s FROM TableSession s WHERE s.table.tableId = :tableId AND s.status = 'Active'",
                TableSession.class)
                .setParameter("tableId", testTable.getTableId())
                .getResultList();
        assertTrue(sessions.isEmpty());
        commitTransaction();

        // Act
        Map<String, Object> result = orderService.createOrderAndNotifyKitchen(
                testTable.getTableId(),
                items,
                null,
                "Table 1 - Invoice 1",
                null
        );

        // Assert
        assertNotNull(result);

        // Verify session was created
        beginTransaction();
        sessions = entityManager.createQuery(
                "SELECT s FROM TableSession s WHERE s.table.tableId = :tableId AND s.status = 'Active'",
                TableSession.class)
                .setParameter("tableId", testTable.getTableId())
                .getResultList();
        assertEquals(1, sessions.size());

        TableSession session = sessions.get(0);
        assertEquals("Active", session.getStatus());
        assertEquals("Table 1 - Invoice 1", session.getInvoiceName());
        commitTransaction();
    }

    /**
     * Test 3: Create order should use existing active session
     */
    @Test
    public void testCreateOrder_UseExistingSession() {
        // Arrange - First create a session manually
        beginTransaction();
        TableSession existingSession = new TableSession();
        existingSession.setSessionId(UUID.randomUUID());
        existingSession.setTable(testTable);
        existingSession.setStatus("Active");
        existingSession.setInvoiceName("Table 1 - Invoice 1");
        existingSession.setTotalAmount(BigDecimal.ZERO);
        entityManager.persist(existingSession);
        UUID sessionId = existingSession.getSessionId();
        commitTransaction();

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("variantId", testVariant.getProductVariantId().toString());
        item.put("quantity", 1);
        items.add(item);

        // Act
        Map<String, Object> result = orderService.createOrderAndNotifyKitchen(
                testTable.getTableId(),
                items,
                null,
                "Table 1 - Invoice 2",
                null
        );

        // Assert
        assertNotNull(result);

        // Verify the same session is used
        beginTransaction();
        List<TableSession> sessions = entityManager.createQuery(
                "SELECT s FROM TableSession s WHERE s.table.tableId = :tableId AND s.status = 'Active'",
                TableSession.class)
                .setParameter("tableId", testTable.getTableId())
                .getResultList();
        assertEquals(1, sessions.size()); // Still only 1 session
        assertEquals(sessionId, sessions.get(0).getSessionId()); // Same session ID
        commitTransaction();
    }

    /**
     * Test 4: Create order should calculate total correctly
     */
    @Test
    public void testCreateOrder_CalculateTotal() {
        // Arrange
        List<Map<String, Object>> items = new ArrayList<>();

        // Add 2 items of testVariant (price: 45000 each)
        Map<String, Object> item1 = new HashMap<>();
        item1.put("variantId", testVariant.getProductVariantId().toString());
        item1.put("quantity", 2);
        items.add(item1);

        // Act
        Map<String, Object> result = orderService.createOrderAndNotifyKitchen(
                testTable.getTableId(),
                items,
                null,
                "Table 1 - Invoice 1",
                null
        );

        // Assert
        assertNotNull(result);
        UUID orderId = (UUID) result.get("orderId");

        // Verify calculations
        beginTransaction();
        Order order = entityManager.find(Order.class, orderId);
        assertNotNull(order);

        // Subtotal = 45000 * 2 = 90000
        BigDecimal expectedSubtotal = BigDecimal.valueOf(90000);
        assertEquals(0, expectedSubtotal.compareTo(order.getSubTotal()));

        // VAT = 90000 * 0.10 = 9000
        BigDecimal expectedVat = BigDecimal.valueOf(9000);
        assertEquals(0, expectedVat.compareTo(order.getVat()));

        // Total = 90000 + 9000 = 99000
        BigDecimal expectedTotal = BigDecimal.valueOf(99000);
        assertEquals(0, expectedTotal.compareTo(order.getTotalAmount()));

        commitTransaction();
    }

    /**
     * Test 5: Create order should update table status to Occupied
     */
    @Test
    public void testCreateOrder_UpdateTableStatus() {
        // Arrange
        beginTransaction();
        testTable.setStatus("Available");
        entityManager.merge(testTable);
        commitTransaction();

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("variantId", testVariant.getProductVariantId().toString());
        item.put("quantity", 1);
        items.add(item);

        // Act
        Map<String, Object> result = orderService.createOrderAndNotifyKitchen(
                testTable.getTableId(),
                items,
                null,
                "Table 1 - Invoice 1",
                null
        );

        // Assert
        assertNotNull(result);

        // Verify table status was updated
        // ✅ Clear entity manager cache to ensure fresh read from database
        entityManager.clear();
        beginTransaction();
        Table updatedTable = entityManager.find(Table.class, testTable.getTableId());
        assertNotNull(updatedTable);
        // Refresh to get latest state from database
        entityManager.refresh(updatedTable);
        assertEquals("Occupied", updatedTable.getStatus());
        commitTransaction();
    }

    /**
     * Test 6: Create order with empty items should throw exception
     */
    @Test
    public void testCreateOrderAndNotifyKitchen_EmptyItems() {
        // Arrange
        List<Map<String, Object>> emptyItems = new ArrayList<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrderAndNotifyKitchen(
                        testTable.getTableId(),
                        emptyItems,
                        null,
                        "Table 1 - Invoice 1",
                        null
                )
        );
        assertEquals("Danh sách món không được rỗng", exception.getMessage());
    }

    /**
     * Test 7: Create order with null items should throw exception
     */
    @Test
    public void testCreateOrderAndNotifyKitchen_NullItems() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrderAndNotifyKitchen(
                        testTable.getTableId(),
                        null,
                        null,
                        "Table 1 - Invoice 1",
                        null
                )
        );
        assertEquals("Danh sách món không được rỗng", exception.getMessage());
    }

    /**
     * Test 8: Create order with invalid variant ID should throw exception
     */
    @Test
    public void testCreateOrderAndNotifyKitchen_InvalidVariantId() {
        // Arrange
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("variantId", UUID.randomUUID().toString()); // Non-existent variant
        item.put("quantity", 1);
        items.add(item);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrderAndNotifyKitchen(
                        testTable.getTableId(),
                        items,
                        null,
                        "Table 1 - Invoice 1",
                        null
                )
        );
        assertTrue(exception.getMessage().contains("Không tìm thấy sản phẩm variant"));
    }

    /**
     * Test 9: Create order for special table (null tableId)
     */
    @Test
    public void testCreateOrderAndNotifyKitchen_SpecialTable() {
        // Arrange
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("variantId", testVariant.getProductVariantId().toString());
        item.put("quantity", 1);
        items.add(item);

        // Act - tableId is null for special tables (Mang về, Giao hàng)
        Map<String, Object> result = orderService.createOrderAndNotifyKitchen(
                null,
                items,
                null,
                "Mang về - Invoice 1",
                "Giao gấp"
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.get("orderId"));
        assertNotNull(result.get("orderNumber"));

        // Verify session was created with null table
        UUID orderId = (UUID) result.get("orderId");
        beginTransaction();
        Order order = entityManager.find(Order.class, orderId);
        assertNotNull(order);
        assertNotNull(order.getSession());
        assertNull(order.getSession().getTable()); // Special table has no table entity
        assertEquals("Mang về - Invoice 1", order.getSession().getInvoiceName());
        commitTransaction();
    }

    /**
     * Test 10: Update order status successfully
     */
    @Test
    public void testUpdateOrderStatus_Success() {
        // Arrange - Create an order first
        beginTransaction();
        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(testTable);
        session.setStatus("Active");
        entityManager.persist(session);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setSession(session);
        order.setOrderNumber("ORD-001");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Pending");
        order.setTotalAmount(BigDecimal.valueOf(50000));
        entityManager.persist(order);
        UUID orderId = order.getOrderId();
        commitTransaction();

        // Act
        boolean result = orderService.updateOrderStatus(orderId, "Preparing");

        // Assert
        assertTrue(result);

        // Verify status was updated
        // ✅ Clear entity manager cache to ensure fresh read from database
        entityManager.clear();
        beginTransaction();
        Order updatedOrder = entityManager.find(Order.class, orderId);
        assertNotNull(updatedOrder);
        // Refresh to get latest state from database
        entityManager.refresh(updatedOrder);
        assertEquals("Preparing", updatedOrder.getStatus());
        commitTransaction();
    }

    /**
     * Test 11: Update order status with invalid status should throw exception
     */
    @Test
    public void testUpdateOrderStatus_InvalidStatus() {
        // Arrange - Create an order first
        beginTransaction();
        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(testTable);
        session.setStatus("Active");
        entityManager.persist(session);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setSession(session);
        order.setOrderNumber("ORD-001");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Pending");
        order.setTotalAmount(BigDecimal.valueOf(50000));
        entityManager.persist(order);
        UUID orderId = order.getOrderId();
        commitTransaction();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(orderId, "InvalidStatus")
        );
        assertTrue(exception.getMessage().contains("Trạng thái không hợp lệ"));
    }
}
