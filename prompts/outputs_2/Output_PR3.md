## PR3 — CẤU TRÚC THƯ MỤC & MAPPING TEST CASES

### TỔNG QUAN
**Mục tiêu:** Thiết kế cấu trúc thư mục test Integration Testing tuân thủ Maven Standard để đạt coverage >70%  
**Phạm vi:** Mapping toàn bộ modules trong dự án (không chỉ 85 TCs ban đầu)  
**Chiến lược dữ liệu:** 
- **Ưu tiên:** Mocks & Helpers cho fast tests
- **Hỗ trợ:** H2 in-memory database khi cần test persistence logic
- **Nguyên tắc:** Không lỗi khi sử dụng H2, đảm bảo tests chạy ổn định

---

## 📂 PHẦN 1: SOURCE CODE & MODULE ANALYSIS

### 1.1. Module Nghiệp Vụ Chính (Toàn bộ dự án)

| Module | Main Components | Test Priority | Status |
|--------|----------------|---------------|--------|
| **Authentication & RBAC** | `web/auth/*`, `filter/*`, `AuthService` | Critical | ⏳ Pending |
| **Cashier/POS Order** | `CashierServlet`, `OrderService`, `PaymentService` | Critical | ⏳ Pending |
| **Inventory** | `ProductServlet`, `InventoryService` | High | ⏳ Pending |
| **Employee** | `EmployeeServlet`, `AttendanceServlet`, `TimesheetService` | Medium | ⏳ Pending |
| **Reservation** | `ReceptionServlet`, `ReservationService`, `TableService` | Medium | ⏳ Pending |
| **Procurement** | `web/procurement/*`, `ProcurementService` | Medium | ⏳ Pending |
| **Sales** | `SalesInvoiceServlet`, `SalesService` | Medium | ⏳ Pending |
| **Alert & Notification** | `AlertServlet`, `NotificationChannel` | Low | ⏳ Pending |
| **Report & Analytics** | `RevenueReportServlet`, `DashboardServlet` | Low | ⏳ Pending |
| **Schedule** | `ScheduleServlet`, `PersonalScheduleService` | Low | ⏳ Pending |
| **Compensation & Payroll** | `CompensationServlet`, `PayrollService` | Low | ⏳ Pending |
| **AI & API** | `ChatBotServlet`, `DemandForecastServlet` | Low | ⏳ Pending |

---

## 📂 PHẦN 2: CẤU TRÚC THƯ MỤC TEST

### 2.1. Tổng Quan Structure

```
src/test/java/com/liteflow/
├── controller/          # Servlet Integration Tests
│   ├── auth/           ⏳ Multiple files (Login, Signup, Logout, OAuth2, OTP, Forgot, Refresh, LoginGoogle)
│   ├── cashier/        ⏳ Multiple files (CashierServlet, CashierAPIServlet)
│   ├── inventory/      ⏳ Multiple files (ProductServlet)
│   ├── employee/       ⏳ Multiple files (EmployeeServlet, Attendance, Timesheet)
│   ├── reservation/    ⏳ 2 files (ReceptionServlet, RoomTableServlet)
│   ├── procurement/    ⏳ 3 files (PurchaseOrder, GoodsReceipt, Invoice)
│   ├── sales/          ⏳ 2 files (SalesInvoiceServlet, SalesInvoicePage)
│   └── report/         ⏳ 2 files (RevenueReportServlet, DashboardServlet)
│
├── service/            # Service Layer Integration Tests
│   ├── auth/          ⏳ Multiple files (AuthService, UserService, OtpService, RoleService)
│   ├── order/         ⏳ Multiple files (OrderService)
│   ├── inventory/     ⏳ Multiple files (ProductService)
│   ├── employee/      ⏳ Multiple files (EmployeeService, TimesheetService)
│   ├── procurement/   ⏳ 2 files (ProcurementService, SupplierService)
│   └── compensation/  ⏳ 1 file (CompensationService)
│
├── filter/            ⏳ 3 files (Authentication, Authorization, Session)
│
└── helpers/           # Test Utilities ✅ IMPLEMENTED
    ├── builders/      ✅ TestDataBuilder.java
    └── mocks/         ✅ ServletTestHelper.java

src/test/resources/
└── mock-responses/    (JSON mock data - optional)
```

### 2.2. Chi Tiết Files Theo Module (Actual Implementation)

**⏳ Module 1: Authentication & RBAC**
- `service/auth/AuthServiceIntegrationTest.java`
- `service/auth/UserServiceIntegrationTest.java`
- `service/auth/OtpServiceIntegrationTest.java`
- `service/auth/RoleServiceIntegrationTest.java`
- `controller/auth/LoginServletIntegrationTest.java`
- `controller/auth/SignupServletIntegrationTest.java`
- `controller/auth/LogoutServletIntegrationTest.java`
- `controller/auth/OAuth2CallbackServletIntegrationTest.java`
- `controller/auth/OtpServletIntegrationTest.java`
- `controller/auth/ForgotPasswordServletIntegrationTest.java`
- `controller/auth/RefreshServletIntegrationTest.java`
- `controller/auth/LoginGoogleServletIntegrationTest.java`

**⏳ Module 2: Cashier/POS Order**
- `service/order/OrderServiceIntegrationTest.java`
- `controller/cashier/CashierAPIServletIntegrationTest.java`
- `controller/cashier/CashierServletIntegrationTest.java`

**⏳ Module 3: Inventory**
- `service/inventory/ProductServiceIntegrationTest.java`
- `controller/inventory/ProductServletIntegrationTest.java`

**⏳ Module 4: Employee**
- `service/employee/EmployeeServiceIntegrationTest.java`
- `service/employee/TimesheetServiceIntegrationTest.java`
- `controller/employee/EmployeeServletIntegrationTest.java`
- `controller/employee/AttendanceServletIntegrationTest.java`
- `controller/employee/TimesheetServletIntegrationTest.java`

**⏳ Module 5: Reservation - Pending**
- `controller/reservation/ReceptionServletIntegrationTest.java`
- `controller/reservation/RoomTableServletIntegrationTest.java`

**⏳ Module 6: Procurement - Pending**
- `controller/procurement/PurchaseOrderServletIntegrationTest.java`
- `controller/procurement/GoodsReceiptServletIntegrationTest.java`
- `controller/procurement/InvoiceServletIntegrationTest.java`

**⏳ Additional Modules - Pending**
- `controller/sales/SalesInvoiceServletIntegrationTest.java`
- `controller/report/RevenueReportServletIntegrationTest.java`
- `controller/schedule/ScheduleServletIntegrationTest.java`
- `controller/compensation/CompensationServletIntegrationTest.java`
- `filter/AuthenticationFilterIntegrationTest.java`
- `filter/AuthorizationFilterIntegrationTest.java`

---

## 🗂️ PHẦN 3: TEST CASE MAPPING & NAMING

### 3.1. Quy Tắc Đặt Tên

| Loại Test | Pattern | Ví dụ |
|-----------|---------|-------|
| **Integration Test** | `<Class>IntegrationTest.java` | `OrderServiceIntegrationTest.java` |
| **E2E Test** | `<Feature>E2ETest.java` | `OrderFlowE2ETest.java` |
| **Special Tests** | `<Feature>ConcurrencyTest.java` | `StockConcurrencyTest.java` |

### 3.2. Tổng Hợp Mapping (Actual Progress)

| Module | Status | Test Files | Total Tests | Coverage Est. |
|--------|--------|-----------|-------------|---------------|
| **Auth & RBAC** | ⏳ Pending | Multiple files | Target: ≥70% coverage |
| **Cashier/POS** | ⏳ Pending | Multiple files | Target: ≥70% coverage |
| **Inventory** | ⏳ Pending | Multiple files | Target: ≥70% coverage |
| **Employee** | ⏳ Pending | Multiple files | Target: ≥70% coverage |
| **Reservation** | ⏳ Pending | Multiple files | Target: ≥70% coverage |
| **Procurement** | ⏳ Pending | Multiple files | Target: ≥70% coverage |
| **Sales** | ⏳ Pending | Multiple files | Target: ≥70% coverage |
| **Report** | ⏳ Pending | Multiple files | Target: ≥70% coverage |
| **Filter** | ⏳ Pending | Multiple files | Target: ≥70% coverage |
| **TOTAL** | **All modules** | **All test files** | **Target: ≥70% overall** |

---

## 🗂️ PHẦN 4: CHIẾN LƯỢC TẠO DỮ LIỆU TEST

### 4.1. Chiến Lược Kết Hợp (Hybrid Approach)

**Ưu tiên:**
1. **Mocks & Helpers** (Primary) - Fast, isolated tests
   - ✅ Mockito mocks cho DAOs
   - ✅ TestDataBuilder tạo test entities  
   - ✅ ServletTestHelper mock HTTP objects
   - ✅ Không cần database, tests chạy nhanh

2. **H2 In-Memory** (Supporting) - Khi cần test persistence
   - ✅ H2 database cho integration tests phức tạp
   - ✅ test-persistence.xml cấu hình entity scanning
   - ✅ @Transactional để rollback sau mỗi test
   - ✅ SQL seed files cho test data thực tế

### 4.2. Test Resources

```
src/test/resources/
├── META-INF/
│   └── persistence.xml                 # H2 configuration (optional)
├── mock-responses/                     # JSON mock data (optional)
│   ├── payment-success.json
│   └── oauth-response.json
└── test-data.sql                       # Seed data (optional)
```

### 4.3. Test Properties

```properties
# JaCoCo Coverage
jacoco.version=0.8.10
jacoco.output=target/jacoco-integration

# Maven Surefire
surefire.version=3.2.5

# H2 Database (nếu dùng)
jdbc.driver=h2
jdbc.url=jdbc:h2:mem:testdb
jdbc.user=sa
jdbc.password=
```

---

## 📐 PHẦN 5: TEST DATA BUILDERS & HELPERS

### 5.1. IntegrationTestBase (Optional for H2 tests)

**Chọn approach dựa trên nhu cầu:**

**Option A: Mock-Only Tests (Recommended for fast tests)**
```java
@DisplayName("Service Integration Tests")
@Tag("integration")
public class MyServiceIntegrationTest {
    
    private MyService service;
    private MyDAO mockDao;
    
    @BeforeEach
    public void setUp() {
        mockDao = mock(MyDAO.class);
        service = new MyService(mockDao);  // Inject mock
    }
    
    @Test
    public void testSomething() {
        // Arrange: setup mocks
        when(mockDao.findById(any())).thenReturn(testEntity);
        
        // Act: call service
        Result result = service.doSomething();
        
        // Assert: verify behavior
        assertNotNull(result);
        verify(mockDao, times(1)).findById(any());
    }
}
```

**Option B: H2 Database Tests (For persistence logic)**
```java
@DisplayName("Service Integration Tests with H2")
@Tag("integration")
public class MyServiceH2IntegrationTest {
    
    private EntityManagerFactory emf;
    private EntityManager em;
    private MyService service;
    
    @BeforeEach
    public void setUp() {
        emf = Persistence.createEntityManagerFactory("test-persistence");
        em = emf.createEntityManager();
        service = new MyService(new MyDAO(em));
        em.getTransaction().begin();
    }
    
    @AfterEach
    public void tearDown() {
        em.getTransaction().rollback();
        em.close();
        emf.close();
    }
    
    @Test
    public void testPersistence() {
        // Arrange: persist test data
        MyEntity entity = TestDataBuilder.buildMyEntity();
        em.persist(entity);
        em.flush();
        
        // Act: call service
        Result result = service.findById(entity.getId());
        
        // Assert: verify database state
        assertNotNull(result);
    }
}
```

### 5.2. TestDataBuilder.java - Builders cho mọi Entity

```java
public class TestDataBuilder {
    
    // === AUTH MODULE ===
    public static User buildUser(String email, String role) {
        return User.builder()
            .userId(UUID.randomUUID())
            .email(email)
            .passwordHash("$2a$10$test.hash")
            .displayName("Test " + role)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    public static Role buildRole(String name) {
        return Role.builder()
            .roleId(UUID.randomUUID())
            .name(name)
            .description("Test role " + name)
            .build();
    }
    
    public static UserSession buildSession(User user) {
        return UserSession.builder()
            .sessionId(UUID.randomUUID())
            .user(user)
            .token("test_jwt_token_" + UUID.randomUUID())
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(8))
            .build();
    }
    
    // === ORDER MODULE ===
    public static Order buildOrder(String tableId, String status) {
        return Order.builder()
            .orderId(UUID.randomUUID())
            .tableId(tableId)
            .orderType("DINE_IN")
            .status(status)
            .totalAmount(0.0)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    public static OrderItem buildOrderItem(Order order, Product product, int qty) {
        return OrderItem.builder()
            .order(order)
            .product(product)
            .quantity(qty)
            .unitPrice(product.getUnitPrice())
            .subtotal(product.getUnitPrice() * qty)
            .build();
    }
    
    public static Payment buildPayment(Order order, String method) {
        return Payment.builder()
            .paymentId(UUID.randomUUID())
            .order(order)
            .paymentMethod(method)
            .amount(order.getTotalAmount())
            .status("COMPLETED")
            .paidAt(LocalDateTime.now())
            .build();
    }
    
    // === INVENTORY MODULE ===
    public static Product buildProduct(String name, double price, int stock) {
        return Product.builder()
            .productId(UUID.randomUUID())
            .name(name)
            .sku("SKU-" + UUID.randomUUID().toString().substring(0, 8))
            .unitPrice(price)
            .costPrice(price * 0.6)
            .stockQuantity(stock)
            .minStockLevel(20)
            .isActive(true)
            .build();
    }
    
    // === EMPLOYEE MODULE ===
    public static Employee buildEmployee(User user, String department) {
        return Employee.builder()
            .user(user)
            .employeeCode("EMP-" + UUID.randomUUID().toString().substring(0, 6))
            .department(department)
            .salary(8000000.0)
            .hireDate(LocalDate.now())
            .status("ACTIVE")
            .build();
    }
    
    public static Attendance buildAttendance(Employee emp, LocalDateTime checkIn) {
        return Attendance.builder()
            .attendanceId(UUID.randomUUID())
            .employee(emp)
            .checkInTime(checkIn)
            .checkOutTime(checkIn.plusHours(8))
            .workHours(8.0)
            .build();
    }
    
    // === RESERVATION MODULE ===
    public static Reservation buildReservation(Table table, LocalDateTime time) {
        return Reservation.builder()
            .reservationId(UUID.randomUUID())
            .table(table)
            .reservationTime(time)
            .numberOfGuests(4)
            .customerPhone("+84901234567")
            .status("CONFIRMED")
            .build();
    }
    
    public static Table buildTable(int number, int capacity) {
        return Table.builder()
            .tableId(UUID.randomUUID())
            .tableNumber(number)
            .capacity(capacity)
            .status("AVAILABLE")
            .build();
    }
    
    // === PROCUREMENT MODULE ===
    public static PurchaseOrder buildPurchaseOrder(Supplier supplier) {
        return PurchaseOrder.builder()
            .poId(UUID.randomUUID())
            .poNumber("PO-2025-" + (int)(Math.random()*1000))
            .supplier(supplier)
            .status("PENDING")
            .totalAmount(0.0)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    public static Supplier buildSupplier(String name) {
        return Supplier.builder()
            .supplierId(UUID.randomUUID())
            .name(name)
            .contactPerson("Contact " + name)
            .phone("+84912345678")
            .isActive(true)
            .build();
    }
}
```

### 5.3. MockServiceHelper.java - External Service Mocks

```java
public class MockServiceHelper {
    
    public static PaymentGatewayService mockPaymentSuccess() {
        PaymentGatewayService mock = mock(PaymentGatewayService.class);
        when(mock.processPayment(any())).thenReturn(
            PaymentResponse.success("txn_" + UUID.randomUUID(), "Approved")
        );
        return mock;
    }
    
    public static PaymentGatewayService mockPaymentTimeout() {
        PaymentGatewayService mock = mock(PaymentGatewayService.class);
        when(mock.processPayment(any())).thenThrow(
            new TimeoutException("Gateway timeout")
        );
        return mock;
    }
    
    public static EmailService mockEmailService() {
        EmailService mock = mock(EmailService.class);
        doNothing().when(mock).sendEmail(anyString(), anyString(), anyString());
        return mock;
    }
    
    public static OAuth2Service mockOAuthSuccess() {
        OAuth2Service mock = mock(OAuth2Service.class);
        when(mock.verifyToken(anyString())).thenReturn(
            OAuth2User.builder()
                .googleId("1234567890")
                .email("test@gmail.com")
                .displayName("Test User")
                .build()
        );
        return mock;
    }
}
```

### 5.4. ServletTestHelper.java - HTTP Mocking

```java
public class ServletTestHelper {
    
    public static HttpServletRequest mockRequest(String method, String json) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn(method);
        when(req.getReader()).thenReturn(
            new BufferedReader(new StringReader(json))
        );
        return req;
    }
    
    public static HttpServletResponse mockResponse() {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter writer = new StringWriter();
        try {
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resp;
    }
    
    public static String getResponseBody(HttpServletResponse resp) {
        return resp.getWriter().toString();
    }
}
```

### 5.4. TestScenarios.java (Optional Helper)

**Sử dụng khi cần:**
- Reusable test scenarios cho H2 tests
- Complex setup với multiple entities
- Common assertions patterns

**Alternative: Helper methods trong test class**
```java
public class OrderServiceIntegrationTest {
    
    // Helper for common setup
    private Order createTestOrder() {
        return TestDataBuilder.buildOrder("table-001", "PENDING");
    }
    
    private List<Product> createTestProducts() {
        return Arrays.asList(
            TestDataBuilder.buildProduct("Coffee", 45000, 100),
            TestDataBuilder.buildProduct("Tea", 35000, 80)
        );
    }
    
    @Test
    public void testOrderCreation() {
        Order order = createTestOrder();
        List<Product> products = createTestProducts();
        // ... test logic
    }
}
```

**Nếu dùng H2, có thể tạo IntegrationTestBase:**
```java
public abstract class IntegrationTestBase {
    
    protected EntityManager em;
    
    @BeforeEach
    public void setUp() {
        // Setup EntityManager và begin transaction
    }
    
    @AfterEach
    public void tearDown() {
        // Rollback transaction
    }
}
```

---

## 📊 PHẦN 6: LỢI ÍCH & COVERAGE

### 6.1. Lợi Ích Thiết Kế (Hybrid Strategy)

| Khía cạnh | Lợi ích |
|-----------|---------|
| **Builder Pattern** | Tạo test data linh hoạt |
| **Mock Services** | Kiểm soát dependencies, test error cases |
| **Option A: Mock-Only** | Tests chạy nhanh, không cần DB |
| **Option B: H2 Support** | Test persistence logic thực tế |
| **Phân module** | Chạy tests theo module, dễ maintain |
| **Isolation** | Mỗi test độc lập |
| **Flexibility** | Chọn approach phù hợp từng test case |

### 6.2. Coverage Mục Tiêu

| Layer | Target | Target Files |
|-------|--------|--------------|
| **Controller** | ≥75% | Multiple servlet test files |
| **Service** | ≥80% | Multiple service test files |
| **Filter** | ≥70% | Filter test files |
| **TOTAL** | **≥70%** | **All test files** |

**Strategy:**
- Implement tests cho từng module theo priority
- Sử dụng mocks/helpers ưu tiên
- H2 database khi cần test persistence logic
- Đảm bảo không lỗi khi sử dụng H2

---

## 📋 PHẦN 7: CHECKLIST TRIỂN KHAI (UPDATED)

### Setup (Phase 1)
- [ ] Tạo test directory structure
- [ ] Implement `TestDataBuilder.java` (all entities)
- [ ] Implement `ServletTestHelper.java`
- [ ] Optional: `IntegrationTestBase.java` (for H2 tests)
- [ ] Optional: `test-persistence.xml` (for H2 configuration)

### Helpers (Phase 2)
- [ ] Implement `TestDataBuilder.java`
- [ ] Implement `ServletTestHelper.java`
- [ ] Optional: `IntegrationTestBase.java` (H2 test support)
- [ ] Optional: `TestScenarios.java` (reusable scenarios)

### Implementation (Phase 3 - Status)
1. **Auth & RBAC** ⏳ PENDING
2. **Cashier/POS** ⏳ PENDING
3. **Inventory** ⏳ PENDING
4. **Employee** ⏳ PENDING
5. **Reservation** ⏳ PENDING
6. **Procurement** ⏳ PENDING
7. **Sales** ⏳ PENDING
8. **Report** ⏳ PENDING
9. **Filter** ⏳ PENDING
10. **Schedule** ⏳ PENDING
11. **Compensation** ⏳ PENDING
12. **Alert & Notification** ⏳ PENDING
13. **AI & API** ⏳ PENDING

### CI/CD (Phase 4)
- [ ] Configure JaCoCo coverage
- [ ] Run coverage report
- [ ] Implement all modules to reach 70%+

---

## 🎯 TÓM TẮT (UPDATED)

### Điểm Nổi Bật
✅ **Hybrid Approach** - Ưu tiên mocks & helpers, hỗ trợ H2 khi cần  
✅ **Flexible Testing** - Mock-only cho speed, H2 cho persistence logic  
✅ **No Database Errors** - Đảm bảo tests chạy ổn định với cả 2 approaches  
✅ **Target Coverage** - Mục tiêu ≥70% cho toàn bộ dự án  
✅ **Comprehensive Testing** - Test tất cả modules trong dự án  

### Modules Cần Test
- Module 1: Auth & RBAC
- Module 2: Cashier/POS
- Module 3: Inventory
- Module 4: Employee

### Các Module Cần Test
- Module 5: Reservation
- Module 6: Procurement
- Module 7: Sales
- Module 8: Report
- Module 9: Filter
- Module 10: Schedule
- Module 11: Compensation
- Module 12: Alert & Notification
- Module 13: AI & API

### Bước Tiếp Theo để đạt 70%+ Coverage

#### Phase 1: Coverage Report (Immediate)
```bash
# Generate coverage report
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

#### Phase 2: Implement Priority Modules

**Priority 1: Critical Modules (Med-High)**
1. **Reservation**
   - `ReceptionServletIntegrationTest.java`
   - `RoomTableServletIntegrationTest.java`
   - `ReservationServiceIntegrationTest.java`

2. **Procurement**
   - `PurchaseOrderServletIntegrationTest.java`
   - `GoodsReceiptServletIntegrationTest.java`
   - `InvoiceServletIntegrationTest.java`
   - `ProcurementServiceIntegrationTest.java`
   - `SupplierServiceIntegrationTest.java`

3. **Sales**
   - `SalesInvoiceServletIntegrationTest.java`
   - `SalesInvoicePageServletIntegrationTest.java`

**Priority 2: Supporting Modules (Low-Medium)**
4. **Filter**
   - `AuthenticationFilterIntegrationTest.java`
   - `AuthorizationFilterIntegrationTest.java`
   - `SessionFilterIntegrationTest.java`

5. **Report**
   - `RevenueReportServletIntegrationTest.java`
   - `DashboardServletIntegrationTest.java`

6. **Schedule**
   - `ScheduleServletIntegrationTest.java`
   - `PersonalScheduleServletIntegrationTest.java`

#### Phase 3: Final Push

7. **Compensation**
8. **Alert & Notification**
9. **AI & API**
10. **Misc Servlets**

#### Estimated Timeline
- **Target:** ≥70% coverage overall
- **Approach:** Implement tests cho từng module theo priority
- **Strategy:** Sử dụng mocks/helpers ưu tiên, H2 khi cần persistence logic

#### Success Criteria
✅ All tests passing  
✅ JaCoCo coverage ≥70% overall  
✅ All critical paths covered  
✅ Stable execution (no database errors)  
✅ Fast execution với mock-only tests