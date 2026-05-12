# LiteFlow Test Infrastructure

## Overview

This directory contains the test infrastructure for LiteFlow project, including both **Unit Tests** and **System Tests** (Selenium).

## Directory Structure

```
src/test/java/com/liteflow/
├── unit/                           # Unit tests
│   ├── base/
│   │   └── UnitTestBase.java      # Base class for unit tests with H2 setup
│   ├── controller/
│   │   ├── cashier/               # Cashier servlet tests
│   │   └── inventory/             # Inventory servlet tests (Reception, RoomTable)
│   └── service/
│       └── inventory/             # Service layer tests
├── selenium/                       # Selenium system tests
│   ├── base/
│   │   ├── BaseTest.java          # Base class for Selenium tests
│   │   └── TestDataHelper.java   # Test data generation utilities
│   ├── pages/                     # Page Object Models (POM)
│   │   ├── cashier/               # Cashier page objects
│   │   └── roomtable/             # RoomTable page objects
│   └── tests/                     # Selenium test classes
└── utils/                          # Shared utilities
    ├── TestDataBuilder.java       # Builder pattern for test entities
    └── DatabaseTestHelper.java   # H2 database utilities
```

## Test Frameworks & Tools

### Unit Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **H2 Database** - In-memory database for testing
- **JaCoCo** - Code coverage tool

### System Testing
- **Selenium WebDriver 4.x** - Browser automation
- **WebDriverManager** - Automatic driver management
- **Page Object Model (POM)** - Design pattern for page objects
- **JUnit 5 / TestNG** - Test frameworks

## Getting Started

### Prerequisites

1. Java 16 or higher
2. Maven 3.x
3. Chrome browser (for Selenium tests)

### Running Tests

#### Run all tests
```bash
mvn test
```

#### Run only unit tests
```bash
mvn test -Dtest="com.liteflow.unit.**"
```

#### Run only Selenium tests
```bash
mvn test -Dtest="com.liteflow.selenium.**"
```

#### Run specific test class
```bash
mvn test -Dtest=CashierServletTest
```

#### Generate coverage report
```bash
mvn test jacoco:report
```
Coverage report will be available at: `target/site/jacoco/index.html`

## Writing Unit Tests

### Example Unit Test

```java
package com.liteflow.unit.controller.cashier;

import com.liteflow.unit.base.UnitTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CashierServletTest extends UnitTestBase {

    @Test
    public void testLoadCashierPage() {
        // Arrange
        var room = TestDataBuilder.createTestRoom().build();
        persist(room);

        // Act
        var result = find(Room.class, room.getRoomId());

        // Assert
        assertNotNull(result);
        assertEquals("Test Room", result.getName());
    }
}
```

### UnitTestBase Features

- Automatic H2 database setup/teardown
- EntityManager lifecycle management
- Transaction management helpers
- Test data seeding capabilities

## Writing Selenium Tests

### Example Selenium Test

```java
package com.liteflow.selenium.tests;

import com.liteflow.selenium.base.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CashierSystemTest extends BaseTest {

    @Test
    public void testLoadCashierPage() {
        // Navigate to cashier page
        navigateTo("/cashier");

        // Wait for page to load
        waitForPageLoad();

        // Assert
        assertTrue(getCurrentUrl().contains("/cashier"));
    }
}
```

### BaseTest Features

- WebDriver initialization and cleanup
- Screenshot capture on failure
- Wait utilities (implicit and explicit)
- Navigation helpers
- JavaScript execution

## Test Data Management

### TestDataBuilder

Provides builder pattern for creating test entities:

```java
// Create a room with tables
Room room = TestDataBuilder.createTestRoom()
    .withName("VIP Room")
    .withTableCount(5)
    .withTotalCapacity(20)
    .build();

// Create a table
Table table = TestDataBuilder.createTestTable()
    .withTableNumber("T001")
    .withRoom(room)
    .withCapacity(4)
    .build();
```

### TestDataHelper (Selenium)

Provides utility methods for generating test data:

```java
String customerName = TestDataHelper.generateRandomCustomerName();
String phoneNumber = TestDataHelper.generateRandomPhoneNumber();
String reservationCode = TestDataHelper.generateReservationCode();
```

## Database Testing

### DatabaseTestHelper

Provides utilities for database operations:

```java
// Setup database
DatabaseTestHelper.setupTestDatabase(entityManager);

// Seed test data
DatabaseTestHelper.seedTestData(entityManager);

// Cleanup
DatabaseTestHelper.cleanupDatabase(entityManager);

// Check if empty
boolean isEmpty = DatabaseTestHelper.isDatabaseEmpty(entityManager);
```

## Code Coverage Goals

- **Cashier Module**: ≥75%
- **Reception Module**: ≥70%
- **RoomTable Module**: ≥75%
- **Critical Paths**: 100%
- **Happy Paths**: 100%
- **Error Scenarios**: ≥80%

## Best Practices

### Unit Tests
1. Use `@BeforeEach` and `@AfterEach` for setup/cleanup
2. Follow AAA pattern (Arrange, Act, Assert)
3. Use descriptive test names (e.g., `testCreateOrder_WithValidData_Success`)
4. Mock external dependencies with Mockito
5. Test both happy paths and error scenarios

### Selenium Tests
1. Use Page Object Model (POM) for maintainability
2. Use explicit waits instead of Thread.sleep()
3. Take screenshots on test failures
4. Keep tests independent and idempotent
5. Use descriptive element locators

### General
1. Keep tests fast and focused
2. Don't test framework code, only application logic
3. Maintain test data independently
4. Clean up test data after each test
5. Document complex test scenarios

## Troubleshooting

### H2 Database Issues
- Check persistence.xml configuration
- Verify entity mappings
- Check H2 dialect compatibility

### Selenium Issues
- Ensure Chrome browser is installed
- Check WebDriverManager configuration
- Verify application is running at `http://localhost:8080/LiteFlow`
- Check element locators are up-to-date

### Maven Issues
- Run `mvn clean install` to refresh dependencies
- Check Java version compatibility
- Verify Maven settings

## CI/CD Integration

Tests are designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Run tests
  run: mvn clean test

- name: Generate coverage report
  run: mvn jacoco:report

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
```

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [H2 Database Documentation](https://www.h2database.com/html/main.html)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

## Contributing

When adding new tests:
1. Follow the existing directory structure
2. Extend appropriate base classes
3. Use TestDataBuilder for entity creation
4. Document complex test scenarios
5. Maintain code coverage goals

## License

Copyright © 2025 LiteFlow. All rights reserved.
