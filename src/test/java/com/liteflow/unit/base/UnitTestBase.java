package com.liteflow.unit.base;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for unit tests with H2 in-memory database setup
 *
 * This class provides:
 * - H2 database initialization and cleanup
 * - EntityManager setup for JPA operations
 * - Transaction management helpers
 * - Test data seeding capabilities
 */
public abstract class UnitTestBase {

    protected static EntityManagerFactory entityManagerFactory;
    protected EntityManager entityManager;

    /**
     * Setup H2 database and EntityManagerFactory before all tests
     */
    @BeforeAll
    public static void setUpClass() {
        Map<String, String> properties = new HashMap<>();

        // H2 in-memory database configuration
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MSSQLServer");
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");

        // Hibernate configuration for H2
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");

        try {
            entityManagerFactory = Persistence.createEntityManagerFactory("LiteFlowPU", properties);
        } catch (Exception e) {
            System.err.println("Failed to create EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Create EntityManager before each test
     */
    @BeforeEach
    public void setUp() {
        entityManager = entityManagerFactory.createEntityManager();
        seedTestData();
    }

    /**
     * Close EntityManager and cleanup after each test
     */
    @AfterEach
    public void tearDown() {
        // Cleanup database before closing EntityManager
        if (entityManager != null && entityManager.isOpen()) {
            cleanupDatabase();
        }
        
        if (entityManager != null) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    /**
     * Close EntityManagerFactory after all tests
     */
    @AfterAll
    public static void tearDownClass() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    /**
     * Helper method to begin transaction
     */
    protected void beginTransaction() {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
    }

    /**
     * Helper method to commit transaction
     */
    protected void commitTransaction() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().commit();
        }
    }

    /**
     * Helper method to rollback transaction
     */
    protected void rollbackTransaction() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }

    /**
     * Helper method to persist entity
     */
    protected <T> T persist(T entity) {
        beginTransaction();
        entityManager.persist(entity);
        commitTransaction();
        return entity;
    }

    /**
     * Helper method to find entity by ID
     */
    protected <T> T find(Class<T> entityClass, Object id) {
        return entityManager.find(entityClass, id);
    }

    /**
     * Helper method to execute query
     */
    protected <T> T executeInTransaction(TransactionCallback<T> callback) {
        try {
            beginTransaction();
            T result = callback.execute(entityManager);
            commitTransaction();
            return result;
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    /**
     * Seed test data - override in subclasses if needed
     */
    protected void seedTestData() {
        // Override in subclasses to seed specific test data
    }

    /**
     * Cleanup database - override in subclasses if needed
     */
    protected void cleanupDatabase() {
        // Override in subclasses to cleanup specific test data
    }

    /**
     * Functional interface for transaction callbacks
     */
    @FunctionalInterface
    protected interface TransactionCallback<T> {
        T execute(EntityManager em);
    }
}
