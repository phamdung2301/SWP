<!-- d9268a00-53c3-4a9c-a92b-5c3c9e294434 0b047177-820a-44c7-b017-6239389d0e15 -->
# Kế hoạch Kiểm thử Unit Testing và System Testing (Selenium)

## 1. Tổng quan

**Mục tiêu:**

- Unit testing cho 3 trang: Cashier, Reception, RoomTable
- System testing (Selenium) với POM cho 2 trang: Cashier, RoomTable
- Coverage mục tiêu: ≥70% cho các module được test

**Framework & Tools:**

- Unit Testing: JUnit 5, Mockito, H2 Database (in-memory)
- System Testing: Selenium WebDriver 4.x, TestNG/JUnit 5, Page Object Model
- Build Tool: Maven
- Coverage: JaCoCo

---

## 2. Cấu trúc Thư mục Test

```
src/test/java/com/liteflow/
├── unit/
│   ├── controller/
│   │   ├── cashier/
│   │   │   ├── CashierServletTest.java
│   │   │   └── CashierAPIServletTest.java
│   │   ├── inventory/
│   │   │   ├── ReceptionServletTest.java
│   │   │   └── RoomTableServletTest.java
│   │   └── ...
│   └── service/
│       ├── inventory/
│       │   ├── OrderServiceTest.java
│       │   ├── ReservationServiceTest.java
│       │   └── RoomTableServiceTest.java
│       └── ...
├── selenium/
│   ├── base/
│   │   ├── BaseTest.java
│   │   └── TestDataHelper.java
│   ├── pages/
│   │   ├── cashier/
│   │   │   ├── CashierPage.java (POM)
│   │   │   ├── TableSection.java
│   │   │   ├── MenuSection.java
│   │   │   └── OrderSection.java
│   │   └── roomtable/
│   │       ├── RoomTablePage.java (POM)
│   │       ├── RoomSection.java
│   │       └── TableSection.java
│   └── tests/
│       ├── CashierSystemTest.java
│       └── RoomTableSystemTest.java
└── utils/
    ├── TestDataBuilder.java
    └── DatabaseTestHelper.java
```

---

## 3. Unit Testing Plan

### 3.1. Cashier Module

#### 3.1.1. CashierServletTest

**File:** `src/test/java/com/liteflow/unit/controller/cashier/CashierServletTest.java`

**Test Cases:**

1. `testDoGet_LoadPageSuccess` - Load cashier page thành công
2. `testDoGet_LoadTablesData` - Load tables data qua AJAX
3. `testDoGet_LoadMenuItems` - Load menu items với stock info
4. `testDoGet_LoadRooms` - Load rooms data
5. `testDoGet_LoadCategories` - Load categories
6. `testDoGet_LoadTodayReservations` - Load reservations hôm nay
7. `testDoGet_EmptyData` - Handle empty data gracefully
8. `testDoGet_ExceptionHandling` - Xử lý exception

#### 3.1.2. CashierAPIServletTest

**File:** `src/test/java/com/liteflow/unit/controller/cashier/CashierAPIServletTest.java`

**Test Cases - Order Creation:**

1. `testCreateOrder_Success` - Tạo order thành công
2. `testCreateOrder_InvalidTableId` - Table ID không hợp lệ
3. `testCreateOrder_EmptyItems` - Items rỗng
4. `testCreateOrder_InvalidVariantId` - Variant ID không hợp lệ
5. `testCreateOrder_ZeroQuantity` - Số lượng = 0
6. `testCreateOrder_NegativeQuantity` - Số lượng âm
7. `testCreateOrder_WithOrderNote` - Tạo order có ghi chú
8. `testCreateOrder_InvalidJson` - JSON không hợp lệ

**Test Cases - Checkout:**

1. `testCheckout_Success` - Checkout thành công
2. `testCheckout_SpecialTable` - Checkout bàn đặc biệt (mang về)
3. `testCheckout_InvalidTableId` - Table ID không hợp lệ
4. `testCheckout_NoActiveSession` - Không có session active
5. `testCheckout_WithDiscount` - Checkout có giảm giá
6. `testCheckout_PaymentMethods` - Test các phương thức thanh toán (cash, card)
7. `testCheckout_StockDeduction` - Trừ stock sau checkout
8. `testCheckout_UpdateTableStatus` - Cập nhật trạng thái bàn

**Test Cases - Invoice Number:**

1. `testGetNextInvoiceNumber_Success` - Lấy số hóa đơn tiếp theo
2. `testGetNextInvoiceNumber_InvalidTableId` - Table ID không hợp lệ

**Test Cases - Notification History:**

1. `testGetNotificationHistory_Success` - Lấy lịch sử thông báo
2. `testGetNotificationHistory_WithDays` - Lấy với số ngày cụ thể
3. `testGetNotificationHistory_Empty` - Không có thông báo

#### 3.1.3. OrderServiceTest

**File:** `src/test/java/com/liteflow/unit/service/inventory/OrderServiceTest.java`

**Test Cases:**

1. `testCreateOrderAndNotifyKitchen_Success` - Tạo order và notify kitchen
2. `testCreateOrder_CreateSessionIfNotExists` - Tạo session nếu chưa có
3. `testCreateOrder_UseExistingSession` - Sử dụng session hiện có
4. `testCreateOrder_CalculateTotal` - Tính tổng tiền
5. `testCreateOrder_UpdateTableStatus` - Cập nhật trạng thái bàn

### 3.2. Reception Module

#### 3.2.1. ReceptionServletTest

**File:** `src/test/java/com/liteflow/unit/controller/inventory/ReceptionServletTest.java`

**Test Cases - Page Load:**

1. `testHandleReceptionPage_Success` - Load reception page thành công
2. `testHandleReceptionPage_LoadReservations` - Load reservations
3. `testHandleReceptionPage_LoadRoomsAndTables` - Load rooms và tables

**Test Cases - Create Reservation:**

1. `testCreateReservation_Success` - Tạo reservation thành công
2. `testCreateReservation_InvalidPhone` - Số điện thoại không hợp lệ
3. `testCreateReservation_InvalidDate` - Ngày không hợp lệ
4. `testCreateReservation_InvalidGuestCount` - Số khách không hợp lệ
5. `testCreateReservation_WithPreOrderItems` - Tạo reservation có pre-order items

**Test Cases - Update Reservation:**

1. `testUpdateReservation_Success` - Cập nhật reservation thành công
2. `testUpdateReservation_NotFound` - Reservation không tồn tại
3. `testUpdateReservation_InvalidStatus` - Trạng thái không hợp lệ

**Test Cases - Confirm Arrival:**

1. `testConfirmArrival_Success` - Xác nhận khách đến
2. `testConfirmArrival_AlreadySeated` - Đã được nhận bàn
3. `testConfirmArrival_CancelledReservation` - Reservation đã hủy

**Test Cases - Cancel Reservation:**

1. `testCancelReservation_Success` - Hủy reservation thành công
2. `testCancelReservation_NotFound` - Reservation không tồn tại

**Test Cases - Export:**

1. `testExportReservations_Success` - Export Excel thành công
2. `testExportReservations_WithDateRange` - Export với khoảng thời gian

#### 3.2.2. ReservationServiceTest

**File:** `src/test/java/com/liteflow/unit/service/inventory/ReservationServiceTest.java`

**Test Cases:**

1. `testGenerateReservationCode_Unique` - Generate mã reservation unique
2. `testValidatePhoneNumber_Valid` - Validate số điện thoại hợp lệ
3. `testValidatePhoneNumber_Invalid` - Validate số điện thoại không hợp lệ
4. `testValidateAvailability_Available` - Kiểm tra bàn available
5. `testValidateAvailability_NotAvailable` - Bàn không available
6. `testAssignTable_Success` - Gán bàn thành công
7. `testAssignTable_InsufficientCapacity` - Sức chứa không đủ

### 3.3. RoomTable Module

#### 3.3.1. RoomTableServletTest

**File:** `src/test/java/com/liteflow/unit/controller/inventory/RoomTableServletTest.java`

**Test Cases - Page Load:**

1. `testDoGet_LoadRoomsAndTables` - Load rooms và tables

**Test Cases - Room Operations:**

1. `testAddRoom_Success` - Thêm phòng thành công
2. `testAddRoom_DuplicateName` - Tên phòng trùng
3. `testAddRoom_InvalidTableCount` - Số lượng bàn không hợp lệ
4. `testUpdateRoom_Success` - Cập nhật phòng thành công
5. `testUpdateRoom_NotFound` - Phòng không tồn tại
6. `testDeleteRoom_Success` - Xóa phòng thành công
7. `testDeleteRoom_WithTables` - Xóa phòng có bàn (cascade)

**Test Cases - Table Operations:**

1. `testAddTable_Success` - Thêm bàn thành công
2. `testAddTable_RoomTableCountLimit` - Vượt giới hạn số bàn trong phòng
3. `testAddTable_RoomCapacityLimit` - Vượt giới hạn sức chứa phòng
4. `testUpdateTable_Success` - Cập nhật bàn thành công
5. `testUpdateTableStatus_Success` - Cập nhật trạng thái bàn
6. `testUpdateTableStatus_InvalidStatus` - Trạng thái không hợp lệ
7. `testDeleteTable_Success` - Xóa bàn thành công
8. `testDeleteTable_WithActiveSession` - Xóa bàn có session active

**Test Cases - Excel Operations:**

1. `testImportExcel_Success` - Import Excel thành công
2. `testImportExcel_InvalidFile` - File Excel không hợp lệ
3. `testExportExcel_Success` - Export Excel thành công
4. `testCheckExcel_Success` - Check Excel file trước khi import

#### 3.3.2. RoomTableServiceTest

**File:** `src/test/java/com/liteflow/unit/service/inventory/RoomTableServiceTest.java`

**Test Cases:**

1. `testGetAllRooms_Success` - Lấy tất cả phòng
2. `testGetRoomById_Success` - Lấy phòng theo ID
3. `testGetRoomById_NotFound` - Phòng không tồn tại
4. `testAddRoom_Validation` - Validate khi thêm phòng
5. `testGetAllTables_Success` - Lấy tất cả bàn
6. `testGetTablesByRoomId_Success` - Lấy bàn theo phòng
7. `testGetTableById_Success` - Lấy bàn theo ID
8. `testAddTable_Validation` - Validate khi thêm bàn
9. `testUpdateTableStatus_Available` - Cập nhật trạng thái Available
10. `testUpdateTableStatus_Occupied` - Cập nhật trạng thái Occupied

---

## 4. System Testing Plan (Selenium với POM)

### 4.1. Page Object Model Structure

#### 4.1.1. CashierPage (POM)

**File:** `src/test/java/com/liteflow/selenium/pages/cashier/CashierPage.java`

**Elements:**

- Main tabs: Phòng bàn, Thực đơn
- Table grid với filters
- Menu grid với categories
- Order items list
- Bill summary section
- Checkout button

**Methods:**

- `selectTable(String tableName)` - Chọn bàn
- `addMenuItem(String itemName, int quantity)` - Thêm món vào order
- `removeOrderItem(int index)` - Xóa món khỏi order
- `updateOrderItemQuantity(int index, int quantity)` - Cập nhật số lượng
- `applyDiscount(double discount, String type)` - Áp dụng giảm giá
- `clickCheckout()` - Click thanh toán
- `selectPaymentMethod(String method)` - Chọn phương thức thanh toán
- `verifyOrderTotal(double expectedTotal)` - Verify tổng tiền
- `searchMenu(String keyword)` - Tìm kiếm món

#### 4.1.2. RoomTablePage (POM)

**File:** `src/test/java/com/liteflow/selenium/pages/roomtable/RoomTablePage.java`

**Elements:**

- Room section với table list
- Add Room button
- Add Table button
- Edit/Delete buttons
- Import/Export Excel buttons

**Methods:**

- `addRoom(String name, String description, int tableCount, int totalCapacity)` - Thêm phòng
- `editRoom(String roomName, Map<String, Object> updates)` - Sửa phòng
- `deleteRoom(String roomName)` - Xóa phòng
- `addTable(String tableNumber, String roomName, int capacity)` - Thêm bàn
- `editTable(String tableNumber, Map<String, Object> updates)` - Sửa bàn
- `deleteTable(String tableNumber)` - Xóa bàn
- `updateTableStatus(String tableNumber, String status)` - Cập nhật trạng thái bàn
- `importExcel(String filePath)` - Import Excel
- `exportExcel()` - Export Excel
- `verifyRoomExists(String roomName)` - Verify phòng tồn tại
- `verifyTableExists(String tableNumber)` - Verify bàn tồn tại

### 4.2. System Test Cases

#### 4.2.1. CashierSystemTest

**File:** `src/test/java/com/liteflow/selenium/tests/CashierSystemTest.java`

**Test Cases:**

1. `testLoadCashierPage` - Load trang cashier thành công
2. `testSelectTable` - Chọn bàn và hiển thị thông tin
3. `testAddMenuItemToOrder` - Thêm món vào order
4. `testRemoveOrderItem` - Xóa món khỏi order
5. `testUpdateOrderItemQuantity` - Cập nhật số lượng món
6. `testSearchMenu` - Tìm kiếm món trong menu
7. `testFilterMenuByCategory` - Lọc món theo danh mục
8. `testApplyDiscount` - Áp dụng giảm giá
9. `testCheckout_CashPayment` - Checkout thanh toán tiền mặt
10. `testCheckout_CardPayment` - Checkout thanh toán thẻ
11. `testCheckout_SpecialTable` - Checkout bàn đặc biệt (mang về)
12. `testMultipleInvoices` - Tạo nhiều hóa đơn cùng lúc
13. `testTableStatusUpdate` - Trạng thái bàn cập nhật sau checkout
14. `testOrderNote` - Thêm ghi chú cho order

#### 4.2.2. RoomTableSystemTest

**File:** `src/test/java/com/liteflow/selenium/tests/RoomTableSystemTest.java`

**Test Cases:**

1. `testLoadRoomTablePage` - Load trang roomtable thành công
2. `testAddRoom` - Thêm phòng mới
3. `testEditRoom` - Sửa thông tin phòng
4. `testDeleteRoom` - Xóa phòng (có và không có bàn)
5. `testAddTable` - Thêm bàn vào phòng
6. `testEditTable` - Sửa thông tin bàn
7. `testDeleteTable` - Xóa bàn
8. `testUpdateTableStatus` - Cập nhật trạng thái bàn (Available/Occupied)
9. `testRoomTableCountLimit` - Kiểm tra giới hạn số bàn trong phòng
10. `testRoomCapacityLimit` - Kiểm tra giới hạn sức chứa phòng
11. `testImportExcel` - Import rooms và tables từ Excel
12. `testExportExcel` - Export rooms và tables ra Excel
13. `testValidation_EmptyFields` - Validate trường rỗng
14. `testValidation_DuplicateRoomName` - Validate tên phòng trùng
15. `testTableHistory` - Xem lịch sử sử dụng bàn

---

## 5. Test Data & Setup

### 5.1. Test Data Builder

**File:** `src/test/java/com/liteflow/utils/TestDataBuilder.java`

**Methods:**

- `createTestRoom()` - Tạo test room
- `createTestTable()` - Tạo test table
- `createTestReservation()` - Tạo test reservation
- `createTestOrder()` - Tạo test order
- `createTestMenuItems()` - Tạo test menu items

### 5.2. Database Setup

**File:** `src/test/java/com/liteflow/utils/DatabaseTestHelper.java`

**Methods:**

- `setupTestDatabase()` - Setup H2 database cho unit tests
- `cleanupDatabase()` - Cleanup sau mỗi test
- `seedTestData()` - Seed dữ liệu test

### 5.3. Base Test Classes

#### 5.3.1. UnitTestBase

**File:** `src/test/java/com/liteflow/unit/base/UnitTestBase.java`

**Features:**

- H2 database setup
- EntityManager initialization
- Test data seeding
- Cleanup methods

#### 5.3.2. BaseTest (Selenium)

**File:** `src/test/java/com/liteflow/selenium/base/BaseTest.java`

**Features:**

- WebDriver initialization (ChromeDriver)
- Implicit waits configuration
- Screenshot on failure
- Cleanup methods
- Base URL configuration

---

## 6. Dependencies cần thêm vào pom.xml

```xml
<!-- Test Dependencies -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.15.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-chrome-driver</artifactId>
    <version>4.15.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>5.6.2</version>
    <scope>test</scope>
</dependency>
<!-- JaCoCo for coverage -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
</plugin>
```

---

## 7. Execution Plan

### Phase 1: Setup Infrastructure (Week 1)

- Tạo cấu trúc thư mục test
- Thêm dependencies vào pom.xml
- Tạo base test classes
- Setup H2 database cho unit tests
- Setup Selenium WebDriver

### Phase 2: Unit Testing - Cashier (Week 1-2)

- CashierServletTest
- CashierAPIServletTest
- OrderServiceTest

### Phase 3: Unit Testing - Reception (Week 2)

- ReceptionServletTest
- ReservationServiceTest

### Phase 4: Unit Testing - RoomTable (Week 2-3)

- RoomTableServletTest
- RoomTableServiceTest

### Phase 5: System Testing - Cashier (Week 3)

- Tạo CashierPage (POM)
- Tạo CashierSystemTest
- Test các chức năng chính

### Phase 6: System Testing - RoomTable (Week 3-4)

- Tạo RoomTablePage (POM)
- Tạo RoomTableSystemTest
- Test các chức năng chính

### Phase 7: Integration & Reporting (Week 4)

- Tích hợp vào CI/CD
- Generate coverage reports
- Documentation

---

## 8. Coverage Goals

- **Unit Tests:**
  - Cashier Module: ≥75%
  - Reception Module: ≥70%
  - RoomTable Module: ≥75%

- **System Tests:**
  - Critical paths: 100%
  - Happy paths: 100%
  - Error scenarios: ≥80%

---

## 9. Files cần tạo

### Unit Tests (15 files):

1. `CashierServletTest.java`
2. `CashierAPIServletTest.java`
3. `OrderServiceTest.java`
4. `ReceptionServletTest.java`
5. `ReservationServiceTest.java`
6. `RoomTableServletTest.java`
7. `RoomTableServiceTest.java`
8. `UnitTestBase.java`
9. `TestDataBuilder.java`
10. `DatabaseTestHelper.java`
11. Các helper classes khác

### Selenium Tests (8 files):

1. `CashierPage.java` (POM)
2. `TableSection.java`
3. `MenuSection.java`
4. `OrderSection.java`
5. `RoomTablePage.java` (POM)
6. `RoomSection.java`
7. `TableSection.java` (roomtable)
8. `CashierSystemTest.java`
9. `RoomTableSystemTest.java`
10. `BaseTest.java`
11. `TestDataHelper.java`

---

## 10. Notes

- Sử dụng H2 in-memory database cho unit tests
- Selenium tests cần server đang chạy tại `http://localhost:8080/LiteFlow`
- Page Object Model giúp maintain tests dễ dàng
- Mỗi test class nên có setup và teardown methods
- Screenshot tự động khi test fail (Selenium)
- Parallel execution cho system tests nếu có thể

### To-dos

- [x] Setup test infrastructure: tạo cấu trúc thư mục, thêm dependencies vào pom.xml (JUnit 5, Mockito, Selenium, H2), tạo base test classes
- [x] Tạo unit tests cho CashierServlet: test load page, load data (tables, menu, rooms, categories, reservations)
- [x] Tạo unit tests cho CashierAPIServlet: test create order, checkout, get invoice number, get notification history với các edge cases
- [x] Tạo unit tests cho OrderService: test create order, notify kitchen, session management, total calculation
- [x] Tạo unit tests cho ReceptionServlet: test create/update/cancel reservation, confirm arrival, export Excel
- [x] Tạo unit tests cho ReservationService: test validation, code generation, table assignment, availability check
- [x] Tạo unit tests cho RoomTableServlet: test CRUD operations cho rooms và tables
- [x] Tạo unit tests cho RoomTableService: test room/table operations, status updates, capacity limits
- [x] Tạo Page Object Model cho Cashier page: CashierPage, TableSection, MenuSection, OrderSection với các methods tương ứng
- [x] Tạo system tests cho Cashier: test select table, add/remove items, checkout, payment methods, discounts
- [x] Tạo Page Object Model cho RoomTable page: RoomTablePage, RoomSection, TableSection với các methods CRUD
- [x] Tạo system tests cho RoomTable: test CRUD rooms/tables, status updates, Excel import/export, validation
- [x] Tạo TestDataBuilder và DatabaseTestHelper: helper classes để tạo test data và manage database
- [ ] Setup JaCoCo coverage reporting và verify coverage goals (≥70% cho các modules)