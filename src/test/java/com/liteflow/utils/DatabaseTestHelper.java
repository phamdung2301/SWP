package com.liteflow.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Test Helper for H2 in-memory database operations
 *
 * This class provides utility methods for:
 * - Setting up H2 test database
 * - Cleaning up test data
 * - Seeding initial test data
 * - Managing database connections
 */
public class DatabaseTestHelper {

    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MSSQLServer";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    /**
     * Create EntityManagerFactory for H2 database
     *
     * @return EntityManagerFactory configured for H2
     */
    public static EntityManagerFactory createTestEntityManagerFactory() {
        Map<String, String> properties = new HashMap<>();

        // H2 in-memory database configuration
        properties.put("jakarta.persistence.jdbc.driver", H2_DRIVER);
        properties.put("jakarta.persistence.jdbc.url", H2_URL);
        properties.put("jakarta.persistence.jdbc.user", H2_USER);
        properties.put("jakarta.persistence.jdbc.password", H2_PASSWORD);

        // Hibernate configuration for H2
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "false");

        return Persistence.createEntityManagerFactory("LiteFlowPU", properties);
    }

    /**
     * Setup H2 database with initial schema
     *
     * @param entityManager EntityManager to use for setup
     */
    public static void setupTestDatabase(EntityManager entityManager) {
        // Schema is automatically created by Hibernate with hbm2ddl.auto=create-drop
        // This method can be used for additional setup if needed
    }

    /**
     * Clean all data from test database
     *
     * @param entityManager EntityManager to use for cleanup
     */
    public static void cleanupDatabase(EntityManager entityManager) {
        if (entityManager == null) {
            return;
        }

        try {
            entityManager.getTransaction().begin();

            // Delete in correct order to respect foreign key constraints
            // Order matters: delete child records before parent records

            // 1. Delete OrderDetails (child of Orders)
            entityManager.createQuery("DELETE FROM OrderDetail").executeUpdate();

            // 2. Delete Orders (child of TableSession)
            entityManager.createQuery("DELETE FROM Order").executeUpdate();

            // 3. Delete TableSessions (child of Table)
            entityManager.createQuery("DELETE FROM TableSession").executeUpdate();

            // 4. Delete ReservationItems (child of Reservation)
            entityManager.createQuery("DELETE FROM ReservationItem").executeUpdate();

            // 5. Delete Reservations (references Table and Room)
            entityManager.createQuery("DELETE FROM Reservation").executeUpdate();

            // 6. Delete Tables (child of Room)
            entityManager.createQuery("DELETE FROM Table").executeUpdate();

            // 7. Delete Rooms (parent)
            entityManager.createQuery("DELETE FROM Room").executeUpdate();

            // 8. Delete other entities as needed
            // entityManager.createQuery("DELETE FROM Product").executeUpdate();
            // entityManager.createQuery("DELETE FROM User").executeUpdate();

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            System.err.println("Error cleaning up database: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Seed test data into database
     *
     * @param entityManager EntityManager to use for seeding
     */
    public static void seedTestData(EntityManager entityManager) {
        if (entityManager == null) {
            return;
        }

        try {
            entityManager.getTransaction().begin();

            // Create test rooms
            var room1 = TestDataBuilder.createTestRoom()
                    .withName("Main Hall")
                    .withDescription("Main dining hall")
                    .withTableCount(5)
                    .withTotalCapacity(20)
                    .build();
            entityManager.persist(room1);

            var room2 = TestDataBuilder.createTestRoom()
                    .withName("VIP Room")
                    .withDescription("VIP dining room")
                    .withTableCount(3)
                    .withTotalCapacity(12)
                    .build();
            entityManager.persist(room2);

            // Create test tables
            var table1 = TestDataBuilder.createTestTable()
                    .withTableNumber("T001")
                    .withTableName("Table 1")
                    .withRoom(room1)
                    .withCapacity(4)
                    .withStatus("Available")
                    .build();
            entityManager.persist(table1);

            var table2 = TestDataBuilder.createTestTable()
                    .withTableNumber("T002")
                    .withTableName("Table 2")
                    .withRoom(room1)
                    .withCapacity(6)
                    .withStatus("Available")
                    .build();
            entityManager.persist(table2);

            var table3 = TestDataBuilder.createTestTable()
                    .withTableNumber("V001")
                    .withTableName("VIP Table 1")
                    .withRoom(room2)
                    .withCapacity(4)
                    .withStatus("Available")
                    .build();
            entityManager.persist(table3);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            System.err.println("Error seeding test data: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Execute SQL script
     *
     * @param sql SQL script to execute
     */
    public static void executeSql(String sql) {
        try (Connection conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error executing SQL: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Get direct JDBC connection to H2 database
     *
     * @return JDBC Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
    }

    /**
     * Check if database is empty
     *
     * @param entityManager EntityManager to use for checking
     * @return true if database has no data
     */
    public static boolean isDatabaseEmpty(EntityManager entityManager) {
        long roomCount = (long) entityManager.createQuery("SELECT COUNT(r) FROM Room r").getSingleResult();
        long tableCount = (long) entityManager.createQuery("SELECT COUNT(t) FROM Table t").getSingleResult();
        long reservationCount = (long) entityManager.createQuery("SELECT COUNT(r) FROM Reservation r").getSingleResult();

        return roomCount == 0 && tableCount == 0 && reservationCount == 0;
    }

    /**
     * Count entities of a specific type
     *
     * @param entityManager EntityManager to use
     * @param entityClass Entity class to count
     * @return number of entities
     */
    public static <T> long countEntities(EntityManager entityManager, Class<T> entityClass) {
        String query = String.format("SELECT COUNT(e) FROM %s e", entityClass.getSimpleName());
        return (long) entityManager.createQuery(query).getSingleResult();
    }

    /**
     * Clear entity manager cache
     *
     * @param entityManager EntityManager to clear
     */
    public static void clearCache(EntityManager entityManager) {
        if (entityManager != null) {
            entityManager.clear();
        }
    }

    /**
     * Flush and clear entity manager
     *
     * @param entityManager EntityManager to flush and clear
     */
    public static void flushAndClear(EntityManager entityManager) {
        if (entityManager != null) {
            entityManager.flush();
            entityManager.clear();
        }
    }
}
