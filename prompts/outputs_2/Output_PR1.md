# KẾ HOẠCH KIỂM THỬ TÍCH HỢP - HỆ THỐNG LITEFLOW

**Ngày tạo:** 31/10/2025  
**Phiên bản:** 2.0  
**Mục tiêu Coverage:** ≥70% trên toàn hệ thống (Backend + Frontend Integration)

---

## 1. FEATURE ANALYSIS - PHÂN TÍCH LOGIC NGHIỆP VỤ

### 1.1. Authentication & Authorization Module
**Business Logic:**
- Login Flow: User credential → AuthenticationFilter → AuthService → UserDAO → JWT token + Session → Redirect theo role
- 2FA: TOTP verification → Upgrade session → Allow access
- OAuth2: Google callback → Verify token → Create/update user → Grant access
- Password Recovery: Request OTP → Email verification → Reset password → Hash BCrypt
- RBAC: AuthorizationFilter validate role + permission → Allow/Deny resource access

**Integration Points:** Filter ↔ Session, AuthService ↔ EmailService, AuthService ↔ OAuth2 API, Role ↔ Permission matrix

### 1.2. Cashier/POS Order Module
**Business Logic:**
- Create Order: Select table → Add items → Validate stock → Calculate total (tax, discount) → Submit → Deduct inventory → Notify kitchen
- Payment: Apply discount → Payment method → Process → Print invoice → Close session → Update revenue
- Order Modification: Add/remove items → Recalculate → Validate stock
- Split Bill: Divide items → Calculate separately → Multi-payment processing

**Integration Points:** AJAX ↔ CashierAPIServlet, OrderService ↔ InventoryService (stock), OrderService ↔ KitchenService, PaymentService ↔ Gateway (mock), Transaction rollback on failure

### 1.3. Kitchen Management Module
**Business Logic:**
- Order Queue: Receive from Cashier → Display pending orders → Sort by priority
- Status Update: Pending → Preparing → Ready → Served → Notify Cashier
- Multi-station: Route orders by dish type (appetizer, main, dessert)

**Integration Points:** Real-time sync Cashier ↔ Kitchen, Order status workflow, Notification system

### 1.4. Inventory Management Module
**Business Logic:**
- Product CRUD: Create/edit/delete → Upload image → Set variants → ProductDAO persist
- Stock Tracking: Auto-deduct on order → Check low-stock threshold → Generate alert
- Price Management: Dynamic pricing → ProductVariantDAO update
- Room/Table: Manage rooms/tables → Assign products → Track availability

**Integration Points:** ProductService ↔ FileUpload, InventoryDAO ↔ OrderService, Alert system ↔ Dashboard

### 1.5. Employee & HR Management Module
**Business Logic:**
- Employee CRUD: Create employee → Assign roles → Generate credentials
- Attendance: Clock-in/out → Calculate hours → Flag late/early leave
- Schedule: Create shifts → Assign employees → Validate conflicts
- Payroll: Calculate (base + overtime + bonus) → Generate paysheet
- Leave Request: Submit → Manager approve → Update attendance

**Integration Points:** EmployeeService ↔ AuthService, TimesheetService ↔ ScheduleService, CompensationService ↔ AttendanceDAO

### 1.6. Table Reservation Module
**Business Logic:**
- Create: Customer info + date/time → Validate availability → Create reservation → Send confirmation
- Pre-order: Attach menu items → Calculate estimated total
- Arrival: Confirm → Assign table → Link to POS order
- Overdue: Job check >15 min → Auto-cancel → Release table
- Cancel: Update status → Release table → Notify

**Integration Points:** ReservationService ↔ TableService, ReservationService ↔ OrderService, Notification system

### 1.7. Procurement Module
**Business Logic:**
- Supplier: CRUD suppliers → Track SLA
- PO: Create → Select supplier + items → Send
- Goods Receipt: Record → Validate with PO → Update inventory
- Invoice: Match invoice vs PO + GR → Validate → Approve payment

**Integration Points:** ProcurementService ↔ InventoryService, GoodsReceipt ↔ InventoryDAO, Alert system

### 1.8. Reporting Module
**Business Logic:**
- Revenue Report: Aggregate sales → Group by period → Calculate metrics
- Top Products: Analyze orders → Rank by quantity/revenue
- Dashboard: Real-time sales, inventory, attendance metrics

---

## 2. TEST OBJECTIVES - MỤC TIÊU KIỂM THỬ

### 2.1. Primary Objectives
1. **Integration Coverage ≥70%**: Tất cả luồng Servlet ↔ Service ↔ DAO ↔ DB được test
2. **Business Logic Validation**: Xác minh quy trình nghiệp vụ đúng requirement
3. **Data Integrity**: Đảm bảo transaction, constraint, cascade hoạt động đúng
4. **Error Handling**: Kiểm tra xử lý lỗi, exception, rollback
5. **Performance Baseline**: Đo thời gian response (order <500ms, login <300ms)

### 2.2. Backend Testing Scope
**Targets:**
- 24 Servlets trong `com.liteflow.controller` và `com.liteflow.web`
- Tất cả Services trong `com.liteflow.service`
- Core DAOs: UserDAO, OrderDAO, ProductDAO, EmployeeDAO, ReservationDAO, PurchaseOrderDAO
- Filters: Authentication, Authorization, Common
- Transaction Management: Rollback scenarios
- External Integrations: Mock Email, Payment, OAuth2

### 2.3. Frontend Testing Scope
**Targets:**
- UI Flow: Login → Dashboard → Order → Payment → Receipt
- Form Validation: Input validation, error messages
- AJAX Integration: Cart update, kitchen refresh
- Navigation: Menu routing, redirect logic

**Tools:** Selenium WebDriver (Chrome only)

### 2.4. Integration Points to Test
✅ Frontend ↔ Backend (JSP → Servlet)  
✅ Servlet ↔ Service (Request → Business logic)  
✅ Service ↔ DAO (Transaction → Query)  
✅ DAO ↔ Database (CRUD → Constraints)  
✅ Cross-module (Order → Inventory → Kitchen → Payment)  
✅ External Services (Email, Payment mock)  
✅ Scheduled Jobs (Alert, overdue check)

---

## 3. TEST STRATEGY - CHIẾN LƯỢC KIỂM THỬ

### 3.1. Test Levels

**Level 1: Unit Testing** (Baseline - Already Done)
- Scope: Utility classes, helper methods
- Coverage: ~80% trên package `util`

**Level 2: Integration Testing** (Main Focus)
- Servlet Integration: Mock Request/Response, real Service + DAO + DB
- Service Layer: Real DAO + DB, mock external services
- DAO Layer: Real database, validate CRUD + transactions + constraints

**Test Distribution:**
- 40% Happy Path: Luồng chuẩn, data hợp lệ
- 30% Edge Cases: Boundary values, empty data, limits
- 30% Error Scenarios: Invalid input, violations, rollback

**Level 3: End-to-End Testing**
- Complete Order Flow: Login → Order → Payment → Kitchen → Inventory
- Employee Lifecycle: Create → Schedule → Attendance → Payroll
- Procurement Flow: PO → Goods Receipt → Invoice → Inventory

**Level 4: System Integration**
- Order → Inventory alert → Auto-generate PO
- Payment → Revenue report → Dashboard update
- Attendance → Payroll → Dashboard metrics

### 3.2. Mock Strategy

**Mock External Services Only:**
- ✅ EmailService: OTP, confirmation emails
- ✅ PaymentGateway: Payment processing (success, fail, timeout)
- ✅ OAuth2 API: Token verification
- ✅ SMS Service: Notifications

**Use Real Components:**
- ✅ Database: SQL Server test instance (clone production schema)
- ✅ Services & DAOs: Real classes
- ✅ Business Logic: No mocking

### 3.3. Data Preparation

**Test Data Management:**
- Seed Script: `TestDataBuilder.java` creates test data
- Database Reset: Rollback after each test
- Transactional Tests: `@Transactional` for auto-rollback

**Sample Data:**
- Users: Admin, Manager, Cashier, Chef (with roles)
- Products: 20 items with varying stock levels
- Tables: 10 tables (available/occupied)
- Orders: 5 sample orders (pending, completed, cancelled)

### 3.4. Test Execution Strategy

**Phase 1: Critical Path (Week 1)**
- Authentication & RBAC (6 tests)
- Cashier Order (10 tests)
- Target: 70% coverage on Auth + Order

**Phase 2: Core Business (Week 2)**
- Inventory (7 tests)
- Employee (6 tests)
- Kitchen (4 tests)
- Target: 70% coverage on Inventory + Employee

**Phase 3: Supporting (Week 3)**
- Reservation (4 tests)
- Procurement (3 tests)
- Reporting (2 tests)

**Phase 4: Robustness (Week 4)**
- 24 edge cases + 25 error scenarios
- Regression testing

### 3.5. Validation Approach

**Backend:**
- ✅ HTTP Status: 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 500 Error
- ✅ Response JSON: Validate structure, field values
- ✅ Database State: Verify insert/update/delete
- ✅ Session State: Validate session attributes, JWT
- ✅ Audit Logs: Verify log entries

**Frontend:**
- ✅ Page Load: Title, elements visible
- ✅ Form Submit: Fill → Submit → Success message
- ✅ Error Display: Invalid input → Error message
- ✅ Navigation: Click → Verify redirect

---

## 4. TEST ENVIRONMENT & TOOLS

### 4.1. Test Framework

**Core:**
- **JUnit 5.10.0**: Main testing framework
- **Mockito 5.5.0**: Mock external services
- **AssertJ 3.24.2**: Fluent assertions
- **JaCoCo 0.8.10**: Coverage reporting (≥70%)
- **Maven Surefire 3.0.0**: Test execution
- **Selenium WebDriver 4.x**: Frontend E2E (Chrome)

### 4.2. Test Database

**Database: SQL Server Test Instance**
- Clone production schema (`liteflow_schema.sql`)
- Connection: JDBC SQL Server driver
- Seed Data: Load from `TestDataBuilder` before each test suite
- Cleanup: Truncate tables after each test class

**Configuration:**
```
Database: liteflow_test
Server: localhost\SQLEXPRESS
User: test_user (limited permissions)
Schema: Auto-created from schema.sql
```

### 4.3. Mock Services

**Mock Classes:**
- `MockEmailService`: Simulate email sending, track sent emails
- `MockPaymentGateway`: Simulate payment (success, fail, timeout)
- `MockOAuth2Service`: Verify Google token, return user info

**Helper Classes:**
- `ServletTestHelper.java`: Mock HttpServletRequest/Response
- `MockServiceHelper.java`: Manage mock services
- `TestDataBuilder.java`: Generate test entities

### 4.4. Test Execution Environment

**Local Development:**
- IDE: IntelliJ IDEA / Eclipse + JUnit plugin
- JDK: Java 11/16
- Maven: 3.6+
- Database: SQL Server 2019+ (test instance)

**CI/CD Pipeline:**
- GitHub Actions / Jenkins
- Trigger: Push, Pull Request
- Steps: `mvn clean test` → Coverage report → Publish
- Threshold: Build fail if coverage < 70%

### 4.5. Test Reporting

**Coverage Report (JaCoCo):**
- Output: `target/site/jacoco/index.html`
- Metrics: Line ≥70%, Branch ≥60%

**Test Execution (Surefire):**
- Output: `target/surefire-reports/`
- Metrics: Total, Passed, Failed, Skipped, Execution time

---

## 5. TEST CASE PLAN - KẾ HOẠCH TEST CASE

### 5.1. Test Case Summary by Module

| Module | Happy Path | Edge Cases | Error Scenarios | Total | Priority |
|--------|-----------|-----------|----------------|-------|----------|
| **1. Authentication & RBAC** | 6 | 3 | 2 | **11** | P0 Critical |
| **2. Cashier/POS Order** | 10 | 6 | 6 | **22** | P0 Critical |
| **3. Inventory Management** | 7 | 5 | 5 | **17** | P1 High |
| **4. Employee & HR** | 6 | 4 | 4 | **14** | P1 High |
| **5. Kitchen Management** | 2 | 1 | 1 | **4** | P2 Medium |
| **6. Table Reservation** | 4 | 3 | 3 | **10** | P2 Medium |
| **7. Procurement** | 3 | 2 | 2 | **7** | P3 Low |
| **8. Reporting & Analytics** | 2 | 1 | 1 | **4** | P3 Low |
| **9. Frontend E2E** | 10 | - | - | **10** | P1 High |
| **TOTAL** | **50** | **25** | **24** | **99** | - |

### 5.2. Module 1: Authentication & Authorization (11 tests)

**Happy Path (6):**
- TC-HP-001: Login email/password → Session + JWT + Redirect
- TC-HP-002: Login Google OAuth2 → User created + Session
- TC-HP-003: 2FA TOTP verification → Session upgraded
- TC-HP-004: Password recovery - Request OTP → Email sent
- TC-HP-005: Password recovery - Reset password → Updated
- TC-HP-006: Admin create user → User created + Roles assigned

**Edge Cases (3):**
- TC-EDGE-001: Password case sensitive → Fail
- TC-EDGE-002: TOTP expired (>30s) → Fail
- TC-EDGE-003: Cashier access admin route → 403

**Error Scenarios (2):**
- TC-ERR-001: Invalid email → 401
- TC-ERR-002: Password recovery non-existent email → 200 (no leak)

### 5.3. Module 2: Cashier/POS Order (22 tests)

**Happy Path (10):**
- TC-HP-007: Create order → Order + Items + Stock deducted + Kitchen notified
- TC-HP-008: Add item to order → Item added + Stock deducted
- TC-HP-009: Remove item → Item removed + Stock restored
- TC-HP-010: Apply discount → Total reduced
- TC-HP-011: Payment Cash → Order completed + Invoice
- TC-HP-012: Payment Card → Gateway called + Transaction ID
- TC-HP-013: Split bill → 2 orders + Items divided
- TC-HP-014: Order history → List sorted DESC
- TC-HP-015: Print invoice → HTML generated
- TC-HP-016: Close session → Table available

**Edge Cases (6):**
- TC-EDGE-004: Quantity = 0 → Validation error
- TC-EDGE-005: Out of stock → Error
- TC-EDGE-006: Invalid discount code → Error
- TC-EDGE-007: Total = 0 (free items) → Order created
- TC-EDGE-008: Concurrent order same table → 1 success, 1 fail
- TC-EDGE-009: Split bill mismatch → Error

**Error Scenarios (6):**
- TC-ERR-003: Invalid table ID → 400
- TC-ERR-004: Invalid product ID → 400
- TC-ERR-005: Payment timeout → Rollback
- TC-ERR-006: Modify completed order → Error
- TC-ERR-007: DB failure → 500 + Rollback
- TC-ERR-008: Stock race condition → 1 fail

### 5.4. Module 3: Inventory (17 tests)

**Happy Path (7):**
- TC-HP-017: Create product + image → Product created
- TC-HP-018: Update product → Updated + Audit
- TC-HP-019: Delete product (soft) → isActive = false
- TC-HP-020: Set variant price → Price updated
- TC-HP-021: Create room → Room created
- TC-HP-022: Create table → Table + Room linked
- TC-HP-023: Low stock alert → Alert generated

**Edge Cases (5):**
- TC-EDGE-010: Image >5MB → Error
- TC-EDGE-011: Duplicate product name → Warning
- TC-EDGE-012: Negative price → Error
- TC-EDGE-013: Delete product in active order → Error
- TC-EDGE-014: Stock deduction negative → Error

**Error Scenarios (5):**
- TC-ERR-009: Invalid image format → Error
- TC-ERR-010: Missing required fields → 400
- TC-ERR-011: Update non-existent product → 404
- TC-ERR-012: Duplicate table number → Error
- TC-ERR-013: Alert email fail → Logged + Continue

### 5.5. Module 4: Employee & HR (14 tests)

**Happy Path (6):**
- TC-HP-024: Create employee → Employee + Credentials + Email
- TC-HP-025: Clock-in → Attendance record
- TC-HP-026: Clock-out → Hours calculated
- TC-HP-027: Create shift template → ShiftTemplate created
- TC-HP-028: Assign employee to shift → EmployeeShift created
- TC-HP-029: Calculate payroll → Paysheet generated

**Edge Cases (4):**
- TC-EDGE-015: Clock-in without previous clock-out → Warning
- TC-EDGE-016: Overlapping shifts → Error
- TC-EDGE-017: Zero hours worked → Base salary only
- TC-EDGE-018: Leave exceeds quota → Error

**Error Scenarios (4):**
- TC-ERR-014: Duplicate employee email → 409
- TC-ERR-015: Invalid employee ID → 404
- TC-ERR-016: Assign past shift → Error
- TC-ERR-017: Payroll DB error → 500

### 5.6. Module 5: Kitchen (4 tests)

**Happy Path (2):**
- TC-HP-030: Display order queue → Orders sorted
- TC-HP-031: Update order status → Status changed + Cashier notified

**Edge Cases (1):**
- TC-EDGE-019: Update already served order → Warning

**Error Scenarios (1):**
- TC-ERR-018: Update non-existent order → 404

### 5.7. Module 6: Reservation (10 tests)

**Happy Path (4):**
- TC-HP-032: Create reservation → Reservation + Confirmation
- TC-HP-033: Add pre-order items → Items + Total calculated
- TC-HP-034: Confirm arrival → Table assigned
- TC-HP-035: Cancel reservation → Status cancelled

**Edge Cases (3):**
- TC-EDGE-020: No tables available → Error
- TC-EDGE-021: Pre-order out of stock → Warning
- TC-EDGE-022: Overdue >15 min → Auto-cancel

**Error Scenarios (3):**
- TC-ERR-019: Past date → Error
- TC-ERR-020: Confirm cancelled reservation → Error
- TC-ERR-021: Cancel completed reservation → Error

### 5.8. Module 7: Procurement (7 tests)

**Happy Path (3):**
- TC-HP-036: Create PO → PO created
- TC-HP-037: Goods receipt → Inventory updated
- TC-HP-038: Invoice matching → Approved

**Edge Cases (2):**
- TC-EDGE-023: Partial receipt → Status updated
- TC-EDGE-024: Invoice amount mismatch → Warning

**Error Scenarios (2):**
- TC-ERR-022: Invalid supplier → 404
- TC-ERR-023: Invalid PO ID → 404

### 5.9. Module 8: Reporting (4 tests)

**Happy Path (2):**
- TC-HP-039: Revenue report → Data aggregated
- TC-HP-040: Top products → Ranked list

**Edge Cases (1):**
- TC-EDGE-025: Empty date range → Empty report

**Error Scenarios (1):**
- TC-ERR-024: Invalid date range → Error

### 5.10. Module 9: Frontend E2E (10 tests)

- TC-FE-001: Login flow → Dashboard displayed
- TC-FE-002: Cashier order flow → Order created + Invoice
- TC-FE-003: Attendance clock-in → Timestamp recorded
- TC-FE-004: Product creation → Product in list
- TC-FE-005: Reservation creation → Confirmation message
- TC-FE-006: Empty form validation → Error messages
- TC-FE-007: AJAX cart update → Total updated
- TC-FE-008: Unauthorized access → 403 redirect
- TC-FE-009: Kitchen order refresh → AJAX polling
- TC-FE-010: Report chart rendering → Chart displayed

---

## 6. COVERAGE TARGET & PRIORITY

### 6.1. Coverage Goals

| Component | Target | Priority |
|-----------|--------|----------|
| Servlets | ≥70% | High |
| Services | ≥75% | High |
| DAOs | ≥80% | High |
| Filters | ≥70% | High |
| Utilities | ≥80% | Medium |
| DTOs/Models | ≥50% | Low |
| Frontend | ≥60% | Medium |

**Overall: ≥70% integration coverage**

### 6.2. Execution Priority

| Priority | Module | Tests | Rationale |
|----------|--------|-------|-----------|
| **P0** | Authentication | 11 | Security, blocks all features |
| **P0** | Cashier Order | 22 | Revenue generation |
| **P1** | Inventory | 17 | Stock management |
| **P1** | Employee | 14 | Operations |
| **P1** | Frontend E2E | 10 | UX validation |
| **P2** | Reservation | 10 | Customer service |
| **P2** | Kitchen | 4 | Order fulfillment |
| **P3** | Procurement | 7 | Back-office |
| **P3** | Reporting | 4 | Analytics |

**Order:** P0 → P1 → P2 → P3

---

## 7. SUCCESS CRITERIA & TIMELINE

### 7.1. Success Criteria

✅ **Coverage:** Overall ≥70%, Critical modules ≥75%  
✅ **Tests:** 99 test cases implemented, 100% pass rate  
✅ **Performance:** Order <500ms, Login <300ms, Reports <2s  
✅ **Bugs:** Zero P0/P1 open bugs  
✅ **Documentation:** Test plan + Coverage report published

### 7.2. Exit Criteria

**Pass Requirements:**
1. All P0 + P1 tests pass (74 test cases)
2. Coverage ≥70% overall
3. Zero P0/P1 bugs unresolved
4. Test execution time <30 min
5. Regression suite established

**Fail Conditions:**
- Any P0 test fails
- Coverage <60%
- >5 P1 bugs open
- Test execution >30 min

### 7.3. Implementation Timeline

**Week 1: Infrastructure**
- Setup SQL Server test instance
- Create test helpers (TestDataBuilder, ServletTestHelper, MockServiceHelper)
- Configure JaCoCo, Surefire
- Implement 5 sample tests

**Week 2-3: Core Testing**
- Auth module (11 tests)
- Order module (22 tests)
- Inventory module (17 tests)
- Run coverage → Adjust

**Week 4: Supporting Modules**
- Employee (14 tests)
- Reservation, Procurement, Reporting (21 tests)
- Frontend E2E (10 tests)

**Week 5: Finalization**
- Run full suite
- Fix failures
- Optimize performance
- Generate coverage report
- Write summary (PR5)

### 7.4. Deliverables

| Deliverable | Timeline | Status |
|-------------|----------|--------|
| PR1: Test Plan | Week 0 | ✅ Completed |
| PR2: Test Case Matrix | Week 0 | ✅ Completed |
| PR3: Environment Setup | Week 1 | Pending |
| PR4: Test Implementation | Week 2-4 | Pending |
| PR5: Coverage Report | Week 5 | Pending |
| PR6: UAT Handoff | Week 6 | Pending |

---

## 8. NEXT STEPS

### 8.1. Immediate Actions

1. **Setup SQL Server test instance:**
   - Clone production schema
   - Create test user with limited permissions
   - Load seed data

2. **Create test infrastructure:**
   - `TestDataBuilder.java` - Generate users, products, tables
   - `ServletTestHelper.java` - Mock Request/Response
   - `MockServiceHelper.java` - Manage external service mocks

3. **Configure build tools:**
   - Update `pom.xml` with JUnit 5, Mockito, JaCoCo
   - Configure Maven Surefire for parallel execution
   - Setup coverage report generation

4. **Implement pilot tests:**
   - 2 Auth tests (login success, RBAC)
   - 2 Order tests (create order, payment)
   - 1 Inventory test (create product)
   - Validate infrastructure works

### 8.2. Resources

**Team:**
- 2 QA Engineers (test design + execution)
- 2 Developers (test implementation)
- 1 DevOps (database + CI/CD setup)

**Effort:** ~160 hours over 5 weeks

---

## APPENDIX

### A. Test Naming Convention

**Format:** `<Module>_<Type>_<Scenario>_Test`

**Examples:**
- `AuthenticationServlet_HappyPath_LoginSuccess_Test`
- `OrderService_EdgeCase_OutOfStock_Test`
- `InventoryDAO_Error_DatabaseFailure_Test`

### B. Test User Accounts

- Admin: `admin@liteflow.com` / `Admin@123`
- Manager: `manager@liteflow.com` / `Manager@123`
- Cashier: `cashier@liteflow.com` / `Cashier@123`
- Chef: `chef@liteflow.com` / `Chef@123`

### C. Reference Documents

- **Output_PR2.md:** Test Case Matrix chi tiết
- **README.md:** System overview
- **liteflow_schema.sql:** Database schema
- **Servlet List:** `src/main/java/com/liteflow/controller/`

---

**Document Version:** 2.0  
**Last Updated:** 31/10/2025  
**Status:** APPROVED ✅

---

## TÓM TẮT

Kế hoạch kiểm thử tích hợp cho hệ thống LiteFlow với **99 test cases** trên **9 modules**, mục tiêu **≥70% coverage**.

**Điểm nổi bật:**
- ✅ Phân tích 8 module nghiệp vụ chính (Auth, Order, Inventory, Employee, Kitchen, Reservation, Procurement, Report)
- ✅ Chiến lược: 50 Happy Path + 25 Edge Cases + 24 Error Scenarios + 10 Frontend E2E
- ✅ Mock chỉ external services (Email, Payment, OAuth2), giữ real business logic
- ✅ SQL Server test instance (không dùng H2)
- ✅ Timeline: 5 weeks, priority P0 (Auth + Order) → P1 (Inventory + Employee) → P2/P3

**Tiếp theo:** PR3 (Environment Setup) → PR4 (Test Implementation) → PR5 (Coverage Report)
