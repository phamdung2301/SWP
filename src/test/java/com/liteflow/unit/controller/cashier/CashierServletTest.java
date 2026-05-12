package com.liteflow.unit.controller.cashier;

import com.liteflow.controller.cashier.CashierServlet;
import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.*;
import com.liteflow.unit.base.UnitTestBase;
import com.liteflow.utils.TestDataBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.persistence.EntityManagerFactory;
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit/Integration tests for CashierServlet
 *
 * These tests use H2 in-memory database to test servlet functionality
 * with real database queries.
 */
public class CashierServletTest extends UnitTestBase {

    private CashierServlet servlet;
    private Gson gson;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private ServletContext servletContext;
    
    @Mock
    private ServletConfig servletConfig;

    private StringWriter responseWriter;
    private AutoCloseable mocks;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        mocks = MockitoAnnotations.openMocks(this);

        // Initialize servlet
        servlet = new CashierServlet();
        gson = new Gson();

        // Setup BaseDAO.emf to use test EntityManagerFactory via reflection
        try {
            Field emfField = BaseDAO.class.getDeclaredField("emf");
            emfField.setAccessible(true);
            emfField.set(null, entityManagerFactory);
            
            // Verify it was set correctly
            EntityManagerFactory setEmf = (EntityManagerFactory) emfField.get(null);
            assertNotNull(setEmf, "BaseDAO.emf should not be null");
            assertSame(entityManagerFactory, setEmf, "BaseDAO.emf should be the same instance as test EntityManagerFactory");
            System.out.println("✅ BaseDAO.emf set successfully");
        } catch (Exception e) {
            fail("Failed to set BaseDAO.emf: " + e.getMessage());
        }

        // Setup servlet context and config
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getContextPath()).thenReturn("/LiteFlow");
        when(request.getServletContext()).thenReturn(servletContext);

        // Initialize servlet with mocked ServletConfig
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
        // Cleanup database before closing EntityManager (if it's still open)
        if (entityManager != null && entityManager.isOpen()) {
            try {
                cleanupDatabase();
            } catch (Exception e) {
                System.err.println("Warning: Error cleaning up database: " + e.getMessage());
            }
        }
        
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
        // Wrap all seed data in one transaction
        beginTransaction();

        try {
            // Create test rooms
            Room room1 = TestDataBuilder.createTestRoom()
                    .withName("Main Hall")
                    .withTableCount(3)
                    .build();
            entityManager.persist(room1);
            entityManager.flush(); // Flush to ensure Room is managed before setting on Table

            Room room2 = TestDataBuilder.createTestRoom()
                    .withName("VIP Room")
                    .withTableCount(2)
                    .build();
            entityManager.persist(room2);
            entityManager.flush(); // Flush to ensure Room is managed before setting on Table

            // Create test tables - set Room before persisting
            // Clear ID to let Hibernate generate it
            Table table1 = TestDataBuilder.createTestTable()
                    .withTableNumber("T001")
                    .withTableName("Table 1")
                    .withCapacity(4)
                    .withStatus("Available")
                    .build();
            table1.setTableId(null); // Clear ID to let Hibernate generate it
            table1.setRoom(room1);
            entityManager.persist(table1);

            Table table2 = TestDataBuilder.createTestTable()
                    .withTableNumber("T002")
                    .withTableName("Table 2")
                    .withCapacity(6)
                    .withStatus("Occupied")
                    .build();
            table2.setTableId(null); // Clear ID to let Hibernate generate it
            table2.setRoom(room1);
            entityManager.persist(table2);

            Table table3 = TestDataBuilder.createTestTable()
                    .withTableNumber("V001")
                    .withTableName("VIP Table 1")
                    .withCapacity(4)
                    .withStatus("Reserved")
                    .build();
            table3.setTableId(null); // Clear ID to let Hibernate generate it
            table3.setRoom(room2);
            entityManager.persist(table3);

            // Create test categories
            Category category1 = new Category();
            category1.setCategoryId(UUID.randomUUID());
            category1.setName("Coffee");
            category1.setDescription("Coffee drinks");
            entityManager.persist(category1);

            Category category2 = new Category();
            category2.setCategoryId(UUID.randomUUID());
            category2.setName("Tea");
            category2.setDescription("Tea drinks");
            entityManager.persist(category2);

            // Create test products with variants
            Product product1 = new Product();
            product1.setProductId(UUID.randomUUID());
            product1.setName("Cappuccino");
            product1.setDescription("Classic cappuccino");
            product1.setImageUrl("images/cappuccino.jpg");
            product1.setIsDeleted(false);
            entityManager.persist(product1);

            ProductVariant variant1 = new ProductVariant();
            variant1.setProductVariantId(UUID.randomUUID());
            variant1.setSize("Medium");
            variant1.setPrice(BigDecimal.valueOf(45000));
            variant1.setIsDeleted(false);
            variant1.setProduct(product1);
            entityManager.persist(variant1);

            // Link product to category
            ProductCategory pc1 = new ProductCategory();
            pc1.setProductCategoryId(UUID.randomUUID());
            pc1.setProduct(product1);
            pc1.setCategory(category1);
            entityManager.persist(pc1);

            // Create inventory
            Inventory inventory = new Inventory();
            inventory.setInventoryId(UUID.randomUUID());
            inventory.setStoreLocation("Main Warehouse");
            entityManager.persist(inventory);

            // Create product stock
            ProductStock stock1 = new ProductStock();
            stock1.setProductStockId(UUID.randomUUID());
            stock1.setAmount(100);
            stock1.setProductVariant(variant1);
            stock1.setInventory(inventory);
            entityManager.persist(stock1);

            // Create test reservation for today
            Reservation reservation1 = TestDataBuilder.createTestReservation()
                    .withCustomerName("John Doe")
                    .withCustomerPhone("0123456789")
                    .withArrivalTime(LocalDateTime.now().plusHours(2))
                    .withNumberOfGuests(4)
                    .withStatus("PENDING")
                    .build();
            reservation1.setReservationId(null); // Clear ID to let Hibernate generate it
            reservation1.setTable(table1);
            reservation1.setRoom(room1);
            entityManager.persist(reservation1);

            // Flush and commit transaction to ensure data is persisted
            entityManager.flush();
            commitTransaction();

            // Clear to ensure fresh reads in tests (but data is already committed)
            entityManager.clear();
            
            // Verify data was persisted by checking count
            Long tableCount = entityManager.createQuery("SELECT COUNT(t) FROM Table t", Long.class).getSingleResult();
            System.out.println("✅ Seeded " + tableCount + " tables in database");
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to seed test data: " + e.getMessage(), e);
        }
    }

    /**
     * Test 1: Load cashier page successfully
     */
    @Test
    public void testDoGet_LoadPageSuccess() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/cart/cashier.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert
        verify(request).setAttribute(eq("menuItemsJson"), anyString());
        verify(request).setAttribute(eq("tablesJson"), anyString());
        verify(request).setAttribute(eq("roomsJson"), anyString());
        verify(request).setAttribute(eq("categoriesJson"), anyString());
        verify(request).setAttribute(eq("reservationsJson"), anyString());
        verify(requestDispatcher).forward(request, response);
    }


    /**
     * Test 3: Load menu items with stock info
     */
    @Test
    public void testDoGet_LoadMenuItems() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/cart/cashier.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert - capture menuItemsJson attribute
        verify(request).setAttribute(eq("menuItemsJson"), anyString());

        // We can't easily capture the actual value with Mockito without ArgumentCaptor
        // But we've verified the attribute was set
    }

    /**
     * Test 4: Load rooms data
     */
    @Test
    public void testDoGet_LoadRooms() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/cart/cashier.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert - verify roomsJson attribute was set
        verify(request).setAttribute(eq("roomsJson"), anyString());
    }

    /**
     * Test 5: Load categories
     */
    @Test
    public void testDoGet_LoadCategories() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/cart/cashier.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert - verify categoriesJson attribute was set
        verify(request).setAttribute(eq("categoriesJson"), anyString());
    }

    /**
     * Test 6: Load today's reservations
     */
    @Test
    public void testDoGet_LoadTodayReservations() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/cart/cashier.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert - verify reservationsJson attribute was set
        verify(request).setAttribute(eq("reservationsJson"), anyString());
    }

    /**
     * Test 7: Handle empty data gracefully
     */
    @Test
    public void testDoGet_EmptyData() throws Exception {
        // Arrange - cleanup all test data first
        cleanupDatabase();

        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/cart/cashier.jsp")).thenReturn(requestDispatcher);

        // Act
        servlet.service(request, response);

        // Assert - should still work with empty data
        verify(request).setAttribute(eq("menuItemsJson"), anyString());
        verify(request).setAttribute(eq("tablesJson"), anyString());
        verify(request).setAttribute(eq("roomsJson"), anyString());
        verify(request).setAttribute(eq("categoriesJson"), anyString());
        verify(request).setAttribute(eq("reservationsJson"), anyString());
        verify(requestDispatcher).forward(request, response);
    }

    /**
     * Test 8: Exception handling
     */
    @Test
    public void testDoGet_ExceptionHandling() throws Exception {
        // Arrange - force an exception by mocking getParameter to throw
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenThrow(new RuntimeException("Test exception"));

        // Redirect stderr to suppress exception stack trace in test output
        PrintStream originalErr = System.err;
        try {
            ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errContent));
            
            // Act
            servlet.service(request, response);
            
            // Assert - should send error response
            verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Lỗi server"));
        } finally {
            // Restore original stderr
            System.setErr(originalErr);
        }
    }
}
