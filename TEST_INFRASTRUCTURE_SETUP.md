# Test Infrastructure Setup Summary

mvn test "-Dtest=com.liteflow.unit.**"

mvn test "-Dtest=com.liteflow.selenium.**"

## ✅ Completed Tasks

### 1. Test Directory Structure Created

```
src/test/java/com/liteflow/
├── unit/
│   ├── base/
│   │   └── UnitTestBase.java
│   ├── controller/
│   │   ├── cashier/
│   │   └── inventory/
│   └── service/
│       └── inventory/
├── selenium/
│   ├── base/
│   │   ├── BaseTest.java
│   │   └── TestDataHelper.java
│   ├── pages/
│   │   ├── cashier/
│   │   └── roomtable/
│   └── tests/
└── utils/
    ├── TestDataBuilder.java
    └── DatabaseTestHelper.java
```

### 2. Test Dependencies Added to pom.xml

#### Unit Testing Dependencies
- ✅ JUnit 5 (5.10.0)
- ✅ Mockito Core (5.5.0)
- ✅ Mockito JUnit Jupiter (5.5.0)
- ✅ H2 Database (2.2.224)

#### System Testing Dependencies
- ✅ Selenium Java (4.15.0)
- ✅ Selenium Chrome Driver (4.15.0)
- ✅ WebDriverManager (5.6.2)
- ✅ TestNG (7.8.0)

#### Build Plugins
- ✅ Maven Surefire Plugin (3.1.2) - for running tests
- ✅ JaCoCo Maven Plugin (0.8.10) - for code coverage

### 3. Base Test Classes Created

#### UnitTestBase.java
**Location:** `src/test/java/com/liteflow/unit/base/UnitTestBase.java`

**Features:**
- H2 in-memory database setup/teardown
- EntityManager lifecycle management
- Transaction management helpers (`beginTransaction()`, `commitTransaction()`, `rollbackTransaction()`)
- Helper methods for persistence operations (`persist()`, `find()`, `executeInTransaction()`)
- Test data seeding capabilities

**Usage:**
```java
public class MyTest extends UnitTestBase {
    @Test
    public void testSomething() {
        var entity = persist(new MyEntity());
        assertNotNull(entity.getId());
    }
}
```

#### BaseTest.java (Selenium)
**Location:** `src/test/java/com/liteflow/selenium/base/BaseTest.java`

**Features:**
- WebDriver initialization and cleanup
- Chrome browser configuration with recommended options
- Implicit wait (10 seconds) and explicit wait (15 seconds)
- Base URL configuration (`http://localhost:8080/LiteFlow`)
- Screenshot capture on test failure
- Navigation helpers
- JavaScript execution utilities
- Page load wait utilities

**Usage:**
```java
public class MySeleniumTest extends BaseTest {
    @Test
    public void testPage() {
        navigateTo("/cashier");
        waitForPageLoad();
        assertTrue(getCurrentUrl().contains("/cashier"));
    }
}
```

### 4. Helper Classes Created

#### TestDataBuilder.java
**Location:** `src/test/java/com/liteflow/utils/TestDataBuilder.java`

**Features:**
- Builder pattern for creating test entities
- Sensible defaults for all entity properties
- Fluent API for customization
- Factory methods for common scenarios

**Available Builders:**
- `RoomBuilder` - for creating Room entities
- `TableBuilder` - for creating Table entities
- `ReservationBuilder` - for creating Reservation entities
- `OrderBuilder` - for creating Order entities

**Example Usage:**
```java
// Create a room with custom properties
Room room = TestDataBuilder.createTestRoom()
    .withName("VIP Room")
    .withTableCount(5)
    .withTotalCapacity(20)
    .build();

// Create a table linked to the room
Table table = TestDataBuilder.createTestTable()
    .withTableNumber("T001")
    .withRoom(room)
    .withCapacity(4)
    .withStatus("Available")
    .build();

// Helper methods for common scenarios
Room roomWithTables = TestDataBuilder.createRoomWithTables("Main Hall", 5);
Table availableTable = TestDataBuilder.createAvailableTable();
Reservation pendingReservation = TestDataBuilder.createPendingReservation();
```

#### DatabaseTestHelper.java
**Location:** `src/test/java/com/liteflow/utils/DatabaseTestHelper.java`

**Features:**
- H2 database configuration and setup
- EntityManagerFactory creation
- Database cleanup utilities
- Test data seeding
- SQL execution utilities
- Database state checking

**Example Usage:**
```java
// Create EntityManagerFactory
EntityManagerFactory emf = DatabaseTestHelper.createTestEntityManagerFactory();

// Seed test data
DatabaseTestHelper.seedTestData(entityManager);

// Cleanup after test
DatabaseTestHelper.cleanupDatabase(entityManager);

// Check if database is empty
boolean isEmpty = DatabaseTestHelper.isDatabaseEmpty(entityManager);

// Count entities
long count = DatabaseTestHelper.countEntities(entityManager, Room.class);
```

#### TestDataHelper.java (Selenium)
**Location:** `src/test/java/com/liteflow/selenium/base/TestDataHelper.java`

**Features:**
- Random data generation for Selenium tests
- Date/time formatting utilities
- Code generation (reservation codes, order numbers, invoices)
- Test data generation (names, phones, emails)

**Example Usage:**
```java
String customerName = TestDataHelper.generateRandomCustomerName();
String phoneNumber = TestDataHelper.generateRandomPhoneNumber();
String email = TestDataHelper.generateRandomEmail();
String reservationCode = TestDataHelper.generateReservationCode();
String futureDate = TestDataHelper.getFutureDate(7); // 7 days from now
int guestCount = TestDataHelper.generateRandomGuestCount();
```

### 5. Documentation Created

- ✅ **README.md** in `src/test/java/com/liteflow/`
  - Comprehensive test infrastructure documentation
  - Usage examples
  - Best practices
  - Troubleshooting guide

- ✅ **TEST_INFRASTRUCTURE_SETUP.md** (this file)
  - Setup summary
  - Quick reference guide

## JaCoCo Coverage Configuration

**Coverage Goals:**
- Minimum line coverage: 70%
- Package-level enforcement

**Commands:**
```bash
# Run tests with coverage
mvn test

# Generate coverage report
mvn jacoco:report

# View report
open target/site/jacoco/index.html
```

## Verification

### Build Status
✅ Maven compilation successful
✅ Test compilation successful
✅ All dependencies resolved

### Files Created
- ✅ 5 base/helper class files
- ✅ 2 documentation files
- ✅ Complete directory structure

### Maven Commands Tested
```bash
# All passed successfully
mvn clean compile          # ✅ BUILD SUCCESS
mvn test-compile           # ✅ BUILD SUCCESS (5 test files compiled)
```

## Next Steps

According to the test plan, the next phases are:

### Phase 2: Unit Testing - Cashier (Week 1-2)
- [ ] CashierServletTest.java
- [ ] CashierAPIServletTest.java
- [ ] OrderServiceTest.java

### Phase 3: Unit Testing - Reception (Week 2)
- [ ] ReceptionServletTest.java
- [ ] ReservationServiceTest.java

### Phase 4: Unit Testing - RoomTable (Week 2-3)
- [ ] RoomTableServletTest.java
- [ ] RoomTableServiceTest.java

### Phase 5: System Testing - Cashier (Week 3)
- [ ] CashierPage.java (POM)
- [ ] CashierSystemTest.java

### Phase 6: System Testing - RoomTable (Week 3-4)
- [ ] RoomTablePage.java (POM)
- [ ] RoomTableSystemTest.java

### Phase 7: Integration & Reporting (Week 4)
- [ ] CI/CD integration
- [ ] Coverage report generation
- [ ] Documentation updates

## Quick Start Guide

### Running a Simple Test

1. Create a test class extending `UnitTestBase`:
```java
package com.liteflow.unit.controller.cashier;

import com.liteflow.unit.base.UnitTestBase;
import com.liteflow.utils.TestDataBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyFirstTest extends UnitTestBase {

    @Test
    public void testCreateRoom() {
        // Arrange
        var room = TestDataBuilder.createTestRoom()
            .withName("Test Room")
            .build();

        // Act
        persist(room);
        var result = find(Room.class, room.getRoomId());

        // Assert
        assertNotNull(result);
        assertEquals("Test Room", result.getName());
    }
}
```

2. Run the test:
```bash
mvn test -Dtest=MyFirstTest
```

### Running a Selenium Test

1. Ensure the application is running at `http://localhost:8080/LiteFlow`

2. Create a test class extending `BaseTest`:
```java
package com.liteflow.selenium.tests;

import com.liteflow.selenium.base.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyFirstSeleniumTest extends BaseTest {

    @Test
    public void testLoadPage() {
        navigateTo("/cashier");
        waitForPageLoad();
        assertTrue(getCurrentUrl().contains("/cashier"));
    }
}
```

3. Run the test:
```bash
mvn test -Dtest=MyFirstSeleniumTest
```

## Troubleshooting

### Issue: H2 Database connection failed
**Solution:** Check that `persistence.xml` exists and has correct configuration

### Issue: Selenium can't find Chrome driver
**Solution:** WebDriverManager will automatically download it. Ensure Chrome browser is installed.

### Issue: Tests compile but don't run
**Solution:** Check test naming convention - classes should end with `Test` or use `@Test` annotation

### Issue: Coverage report not generated
**Solution:** Run `mvn test jacoco:report` (two commands, or combined)

## Resources

- Test Plan: `.cursor/plans/unit-v-system-testing-plan-4cd2a7-d9268a00.plan.md`
- Test README: `src/test/java/com/liteflow/README.md`
- JUnit 5: https://junit.org/junit5/docs/current/user-guide/
- Mockito: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- Selenium: https://www.selenium.dev/documentation/
- JaCoCo: https://www.jacoco.org/jacoco/trunk/doc/

## Summary

✅ **Todo 1 from test plan has been COMPLETED successfully!**

All test infrastructure has been set up including:
- Test directory structure
- Test dependencies (JUnit 5, Mockito, Selenium, H2, JaCoCo)
- Base test classes (UnitTestBase, BaseTest)
- Helper utilities (TestDataBuilder, DatabaseTestHelper, TestDataHelper)
- Documentation (README, setup guide)

The project is now ready for implementing actual test cases according to the test plan phases.
