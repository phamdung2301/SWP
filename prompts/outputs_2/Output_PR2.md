# MA TRẬN TEST CASE TÍCH HỢP - LITEFLOW SYSTEM

**Mục tiêu:** Integration Testing Coverage ≥70%  
**Phạm vi:** Backend (Servlet ↔ Service ↔ DAO) + Frontend (JSP ↔ API) + Cross-module Integration  
**Tổng số:** 99 test cases (50 Happy Path + 25 Edge Cases + 24 Error Scenarios)

---

## 📊 TEST CASE MATRIX - INTEGRATION TESTS

---

## ✅ HAPPY PATH SCENARIOS (50 test cases)

### **Module 1: Authentication & RBAC (6 tests)**

**TC-HP-001: Login thành công email/password**
- **Input:** `{email: "admin@liteflow.com", password: "Admin@123"}`
- **Expected:** HTTP 200, session created, JWT token, redirect dashboard theo role, DB: UserSessions=ACTIVE
- **Mock:** None (real DB)

**TC-HP-002: Login Google OAuth2**
- **Input:** `{googleToken: "valid_jwt", email: "user@gmail.com"}`
- **Expected:** HTTP 200, user created/updated, session created, JWT returned
- **Mock:** Google OAuth2 API verification

**TC-HP-003: 2FA TOTP verification**
- **Input:** `{userId: "uuid-001", totpCode: "123456", sessionToken: "temp_token"}`
- **Expected:** HTTP 200, session upgraded to FULLY_AUTHENTICATED, last2FAVerifiedAt updated
- **Mock:** TOTP generator với time-based code

**TC-HP-004: Password recovery - Request OTP**
- **Input:** `{email: "user@liteflow.com"}`
- **Expected:** HTTP 200, OTP sent to email, OtpToken record in DB với expiry
- **Mock:** EmailService (MailUtil)

**TC-HP-005: Password recovery - Reset password**
- **Input:** `{email: "user@liteflow.com", otp: "123456", newPassword: "NewPass@123"}`
- **Expected:** HTTP 200, password updated (BCrypt hashed), OTP invalidated, confirmation email sent
- **Mock:** EmailService

**TC-HP-006: RBAC - Admin tạo user mới**
- **Input:** `{email: "newuser@liteflow.com", roles: ["CASHIER"], createdBy: "admin-uuid"}`
- **Expected:** HTTP 201, User + UserRoles created, welcome email sent
- **Mock:** EmailService

---

### **Module 2: Cashier/POS Order Management (10 tests)**

**TC-HP-007: Tạo order mới**
- **Input:** `{tableId: "table-001", items: [{productId: "prod-001", quantity: 2}]}`
- **Expected:** HTTP 201, Order + OrderItems created, stock deducted (ProductStock -2), Kitchen notification sent, table status=OCCUPIED
- **Mock:** None

**TC-HP-008: Thêm item vào order hiện có**
- **Input:** `{orderId: "order-001", productId: "prod-002", quantity: 1}`
- **Expected:** HTTP 200, OrderItem added, stock deducted, order total recalculated
- **Mock:** None

**TC-HP-009: Xóa item khỏi order**
- **Input:** `{orderId: "order-001", orderItemId: "item-001"}`
- **Expected:** HTTP 200, OrderItem removed, stock restored, total recalculated
- **Mock:** None

**TC-HP-010: Apply discount coupon**
- **Input:** `{orderId: "order-001", discountCode: "SAVE10"}`
- **Expected:** HTTP 200, discount 10% applied, total giảm, discount info saved
- **Mock:** None

**TC-HP-011: Payment Cash**
- **Input:** `{orderId: "order-001", paymentMethod: "CASH", amount: 200000}`
- **Expected:** HTTP 200, order status=COMPLETED, SalesInvoice created, receipt HTML generated, table status=AVAILABLE
- **Mock:** None

**TC-HP-012: Payment Credit Card**
- **Input:** `{orderId: "order-001", paymentMethod: "CARD", cardToken: "tok_visa_123"}`
- **Expected:** HTTP 200, PaymentGateway called, transaction ID saved, order completed
- **Mock:** PaymentGateway (return success transaction)

**TC-HP-013: Split bill**
- **Input:** `{orderId: "order-001", split: [{items: ["item-1", "item-2"]}, {items: ["item-3"]}]}`
- **Expected:** HTTP 200, 2 new orders created, items divided correctly, original order archived
- **Mock:** None

**TC-HP-014: Load order history**
- **Input:** `{tableId: "table-001", fromDate: "2025-10-01"}`
- **Expected:** HTTP 200, list of orders sorted DESC by date
- **Mock:** None

**TC-HP-015: Print invoice**
- **Input:** `{orderId: "order-001"}`
- **Expected:** HTTP 200, HTML invoice với items, tax, total, QR code payment
- **Mock:** None

**TC-HP-016: Close order session**
- **Input:** `{orderId: "order-001"}`
- **Expected:** HTTP 200, order session closed, table released, revenue recorded
- **Mock:** None

---

### **Module 3: Inventory Management (7 tests)**

**TC-HP-017: Tạo product với image upload**
- **Input:** `{name: "Phở Bò", price: 50000, category: "MAIN", image: file(2MB)}`
- **Expected:** HTTP 201, Product created, image saved to `/uploads/products/`, stock initialized=0
- **Mock:** None

**TC-HP-018: Update product info**
- **Input:** `{productId: "prod-001", name: "Phở Bò Đặc Biệt", price: 60000}`
- **Expected:** HTTP 200, Product updated, audit log recorded
- **Mock:** None

**TC-HP-019: Soft delete product**
- **Input:** `{productId: "prod-001"}`
- **Expected:** HTTP 200, Product.isActive=false, không hiển thị trong menu
- **Mock:** None

**TC-HP-020: Set variant price**
- **Input:** `{productId: "prod-001", variantId: "var-L", price: 70000}`
- **Expected:** HTTP 200, ProductVariant price updated
- **Mock:** None

**TC-HP-021: Create room**
- **Input:** `{name: "VIP Room", capacity: 10}`
- **Expected:** HTTP 201, Room created
- **Mock:** None

**TC-HP-022: Create table trong room**
- **Input:** `{roomId: "room-001", tableNumber: 5, seats: 4}`
- **Expected:** HTTP 201, Table created, linked to room
- **Mock:** None

**TC-HP-023: Low stock alert**
- **Input:** (ProductStock quantity < threshold trigger)
- **Expected:** Alert generated, notification sent to admin, alert log saved
- **Mock:** EmailService / NotificationService

---

### **Module 4: Employee & HR Management (6 tests)**

**TC-HP-024: Tạo employee mới**
- **Input:** `{name: "Nguyen Van A", email: "nva@liteflow.com", role: "CASHIER"}`
- **Expected:** HTTP 201, Employee + User created, credentials generated, welcome email sent
- **Mock:** EmailService

**TC-HP-025: Clock-in attendance**
- **Input:** `{employeeId: "emp-001", timestamp: "2025-10-31T08:00:00"}`
- **Expected:** HTTP 200, EmployeeAttendance record created với clockInTime
- **Mock:** None

**TC-HP-026: Clock-out attendance**
- **Input:** `{employeeId: "emp-001", timestamp: "2025-10-31T17:00:00"}`
- **Expected:** HTTP 200, EmployeeAttendance updated với clockOutTime, hoursWorked calculated (9h)
- **Mock:** None

**TC-HP-027: Tạo shift template**
- **Input:** `{name: "Morning Shift", startTime: "08:00", endTime: "12:00"}`
- **Expected:** HTTP 201, ShiftTemplate created
- **Mock:** None

**TC-HP-028: Assign employee to shift**
- **Input:** `{employeeId: "emp-001", shiftId: "shift-001", date: "2025-11-01"}`
- **Expected:** HTTP 201, EmployeeShift created, no conflict with existing shifts
- **Mock:** None

**TC-HP-029: Calculate payroll**
- **Input:** `{employeeId: "emp-001", period: "2025-10"}`
- **Expected:** HTTP 200, payroll calculated (baseSalary + overtime + bonus), Paysheet generated
- **Mock:** None

---

### **Module 5: Kitchen Management (2 tests)**

**TC-HP-030: Display order queue**
- **Input:** GET `/kitchen/orders?status=PENDING`
- **Expected:** HTTP 200, list of pending orders sorted by timestamp ASC
- **Mock:** None

**TC-HP-031: Update order status**
- **Input:** `{orderId: "order-001", status: "PREPARING"}`
- **Expected:** HTTP 200, order status updated, notification sent to cashier dashboard
- **Mock:** NotificationService (WebSocket/AJAX)

---

### **Module 6: Table Reservation (4 tests)**

**TC-HP-032: Create reservation**
- **Input:** `{customerName: "Tran Thi B", phone: "0901234567", date: "2025-11-01", time: "19:00", guests: 4}`
- **Expected:** HTTP 201, Reservation created, table availability checked, confirmation SMS sent
- **Mock:** SMSService

**TC-HP-033: Add pre-order items**
- **Input:** `{reservationId: "res-001", items: [{productId: "prod-001", quantity: 2}]}`
- **Expected:** HTTP 200, ReservationItems created, estimated total calculated
- **Mock:** None

**TC-HP-034: Confirm customer arrival**
- **Input:** `{reservationId: "res-001"}`
- **Expected:** HTTP 200, reservation status=ARRIVED, table assigned, Order session created
- **Mock:** None

**TC-HP-035: Cancel reservation**
- **Input:** `{reservationId: "res-001"}`
- **Expected:** HTTP 200, status=CANCELLED, table released, notification sent
- **Mock:** SMSService

---

### **Module 7: Procurement (3 tests)**

**TC-HP-036: Create Purchase Order**
- **Input:** `{supplierId: "sup-001", items: [{ingredientId: "ing-001", quantity: 100, unitPrice: 5000}]}`
- **Expected:** HTTP 201, PurchaseOrder + POItems created, status=PENDING
- **Mock:** None

**TC-HP-037: Record Goods Receipt**
- **Input:** `{poId: "po-001", receivedItems: [{poItemId: "poi-001", receivedQty: 100}]}`
- **Expected:** HTTP 201, GoodsReceipt created, inventory stock updated (+100), PO status=RECEIVED
- **Mock:** None

**TC-HP-038: Invoice matching**
- **Input:** `{invoiceNumber: "INV-001", poId: "po-001", amount: 500000}`
- **Expected:** HTTP 200, Invoice matched with PO + GR, status=APPROVED if amounts match
- **Mock:** None

---

### **Module 8: Reporting & Analytics (2 tests)**

**TC-HP-039: Generate revenue report**
- **Input:** `{startDate: "2025-10-01", endDate: "2025-10-31"}`
- **Expected:** HTTP 200, report data with totalRevenue, orderCount, avgOrderValue
- **Mock:** None

**TC-HP-040: Get top-selling products**
- **Input:** `{period: "2025-10", limit: 10}`
- **Expected:** HTTP 200, list of products sorted by revenue DESC
- **Mock:** None

---

### **Module 9: Frontend E2E Integration (10 tests)**

**TC-FE-001: Login flow E2E**
- **Steps:** Open `/auth/login.jsp` → Fill email/password → Submit → Verify redirect to `/dashboard.jsp`
- **Expected:** Dashboard hiển thị, session active, user info trong header
- **Mock:** None

**TC-FE-002: Cashier order flow E2E**
- **Steps:** Login cashier → Navigate `/cart/cashier.jsp` → Select table → Add items → Payment → Print receipt
- **Expected:** Order created in DB, invoice PDF displayed, table released
- **Mock:** None

**TC-FE-003: Employee clock-in E2E**
- **Steps:** Login employee → Navigate `/attendance.jsp` → Click "Clock In" → Verify timestamp
- **Expected:** Attendance record created, timestamp hiển thị trên UI
- **Mock:** None

**TC-FE-004: Product creation E2E**
- **Steps:** Login admin → Navigate `/inventory/productlist.jsp` → Click "Add Product" → Fill form + upload image → Submit
- **Expected:** Product appears in list, image displayed
- **Mock:** None

**TC-FE-005: Reservation creation E2E**
- **Steps:** Open `/reception/reception.jsp` → Fill reservation form → Submit → Verify confirmation message
- **Expected:** Reservation created, confirmation message displayed
- **Mock:** SMSService

**TC-FE-006: Form validation - Empty fields**
- **Steps:** Submit any form with empty required fields
- **Expected:** Error messages displayed next to fields, form not submitted
- **Mock:** None

**TC-FE-007: AJAX cart update**
- **Steps:** In cashier page, add item to cart → Verify total updates without page reload
- **Expected:** AJAX call to `/cashier/api/cart`, DOM updated, no page refresh
- **Mock:** None

**TC-FE-008: Unauthorized access redirect**
- **Steps:** Login as CASHIER → Navigate to `/employees` (admin only)
- **Expected:** Redirect to `/accessDenied.jsp` with 403 message
- **Mock:** None

**TC-FE-009: Kitchen order refresh**
- **Steps:** Login as CHEF → Open `/kitchen/kitchen.jsp` → Verify order list auto-refreshes every 10s
- **Expected:** AJAX polling active, new orders appear automatically
- **Mock:** None

**TC-FE-010: Revenue report chart rendering**
- **Steps:** Login admin → Navigate `/report/revenue.jsp` → Select date range → View chart
- **Expected:** Chart.js renders line chart with revenue data
- **Mock:** None

---

## ⚠️ EDGE CASES (25 test cases)

### **Module 1: Authentication (3 tests)**

**TC-EDGE-001: Password case sensitive**
- **Input:** `{email: "admin@liteflow.com", password: "admin@123"}` (wrong case)
- **Expected:** HTTP 401, error "Invalid credentials"
- **Mock:** None

**TC-EDGE-002: TOTP code expired (>30s)**
- **Input:** `{totpCode: "123456"}` (generated 35s ago)
- **Expected:** HTTP 400, error "TOTP code expired"
- **Mock:** TOTP time mock

**TC-EDGE-003: RBAC - Cashier cố access admin route**
- **Input:** Cashier session, GET `/employees`
- **Expected:** HTTP 403 Forbidden, redirect to access denied
- **Mock:** None

---

### **Module 2: Cashier Order (6 tests)**

**TC-EDGE-004: Order với quantity = 0**
- **Input:** `{productId: "prod-001", quantity: 0}`
- **Expected:** HTTP 400, validation error "Quantity must be > 0"
- **Mock:** None

**TC-EDGE-005: Order sản phẩm out of stock**
- **Input:** `{productId: "prod-001"}` (stock = 0)
- **Expected:** HTTP 400, error "Product out of stock"
- **Mock:** None

**TC-EDGE-006: Apply invalid discount code**
- **Input:** `{discountCode: "INVALID"}`
- **Expected:** HTTP 400, error "Invalid discount code"
- **Mock:** None

**TC-EDGE-007: Order với total = 0 (all free)**
- **Input:** `{items: [{productId: "prod-free", price: 0, quantity: 2}]}`
- **Expected:** HTTP 201, order created, payment skipped, status=COMPLETED
- **Mock:** None

**TC-EDGE-008: Concurrent order on same table**
- **Input:** 2 simultaneous requests tạo order cho table-001
- **Expected:** 1 success, 1 fail với "Table already has active order"
- **Mock:** None (test race condition)

**TC-EDGE-009: Split bill với total không khớp**
- **Input:** `{split: [{items: ["item-1"]}, {items: ["item-2"]}]}` nhưng tổng ≠ original total
- **Expected:** HTTP 400, error "Split amounts do not match original total"
- **Mock:** None

---

### **Module 3: Inventory (5 tests)**

**TC-EDGE-010: Upload image >5MB**
- **Input:** Image file 6MB
- **Expected:** HTTP 400, error "File size exceeds 5MB limit"
- **Mock:** None

**TC-EDGE-011: Tạo product với tên trùng**
- **Input:** `{name: "Phở Bò"}` (already exists)
- **Expected:** HTTP 409 Conflict hoặc warning "Duplicate product name"
- **Mock:** None

**TC-EDGE-012: Set negative price**
- **Input:** `{price: -100}`
- **Expected:** HTTP 400, validation error "Price must be positive"
- **Mock:** None

**TC-EDGE-013: Delete product đang có order active**
- **Input:** `{productId: "prod-001"}` (có trong pending order)
- **Expected:** HTTP 409, error "Cannot delete product with active orders"
- **Mock:** None

**TC-EDGE-014: Stock deduction xuống âm**
- **Input:** Deduct 10 units khi stock = 5
- **Expected:** HTTP 400, error "Insufficient stock"
- **Mock:** None

---

### **Module 4: Employee (4 tests)**

**TC-EDGE-015: Clock-in khi chưa clock-out lần trước**
- **Input:** Employee có attendance record chưa clock-out
- **Expected:** HTTP 400, warning "Previous shift not closed, auto clock-out applied"
- **Mock:** None

**TC-EDGE-016: Assign overlapping shifts**
- **Input:** Shift 8-12h và shift 11-15h cùng ngày
- **Expected:** HTTP 409, error "Shift time conflict"
- **Mock:** None

**TC-EDGE-017: Calculate payroll với 0 hours**
- **Input:** Employee không có attendance records trong period
- **Expected:** HTTP 200, payroll = baseSalary only (no overtime)
- **Mock:** None

**TC-EDGE-018: Leave request vượt quota**
- **Input:** `{leaveType: "ANNUAL", days: 10}` (remaining quota = 5)
- **Expected:** HTTP 400, error "Insufficient leave balance"
- **Mock:** None

---

### **Module 5: Kitchen (1 test)**

**TC-EDGE-019: Update order status đã SERVED**
- **Input:** `{orderId: "order-001", status: "PREPARING"}` (current=SERVED)
- **Expected:** HTTP 400, warning "Cannot change status of completed order"
- **Mock:** None

---

### **Module 6: Reservation (3 tests)**

**TC-EDGE-020: Reservation khi no tables available**
- **Input:** `{date: "2025-11-01", time: "19:00"}` (all tables occupied)
- **Expected:** HTTP 400, error "No tables available at this time"
- **Mock:** None

**TC-EDGE-021: Pre-order item out of stock**
- **Input:** `{productId: "prod-001"}` (stock = 0)
- **Expected:** HTTP 200 with warning "Product may not be available upon arrival"
- **Mock:** None

**TC-EDGE-022: Reservation overdue >15 min**
- **Input:** Reservation time=18:00, current time=18:20, status=PENDING
- **Expected:** Job auto-cancel, status=NO_SHOW, table released, notification sent
- **Mock:** Scheduled job trigger

---

### **Module 7: Procurement (2 tests)**

**TC-EDGE-023: Goods receipt quantity < PO quantity**
- **Input:** `{receivedQty: 80}` (ordered 100)
- **Expected:** HTTP 200, partial receipt, PO status=PARTIALLY_RECEIVED
- **Mock:** None

**TC-EDGE-024: Invoice amount ≠ PO amount**
- **Input:** `{invoiceAmount: 520000}` (PO amount = 500000)
- **Expected:** HTTP 200 with warning "Amount mismatch", status=PENDING_REVIEW
- **Mock:** None

---

### **Module 8: Reporting (1 test)**

**TC-EDGE-025: Report với empty date range**
- **Input:** `{startDate: "2025-12-01", endDate: "2025-12-31"}` (future, no data)
- **Expected:** HTTP 200, empty report with zero values
- **Mock:** None

---

## ❌ ERROR SCENARIOS (24 test cases)

### **Module 1: Authentication (2 tests)**

**TC-ERR-001: Login với email không tồn tại**
- **Input:** `{email: "notexist@test.com", password: "any"}`
- **Expected:** HTTP 401, error "Invalid credentials"
- **Mock:** None

**TC-ERR-002: Password recovery với invalid email**
- **Input:** `{email: "notexist@test.com"}`
- **Expected:** HTTP 200 (security: không leak email exists), no OTP sent
- **Mock:** EmailService (track no call made)

---

### **Module 2: Cashier Order (6 tests)**

**TC-ERR-003: Tạo order với invalid table ID**
- **Input:** `{tableId: "invalid-id"}`
- **Expected:** HTTP 404, error "Table not found"
- **Mock:** None

**TC-ERR-004: Tạo order với invalid product ID**
- **Input:** `{productId: "invalid-id"}`
- **Expected:** HTTP 404, error "Product not found"
- **Mock:** None

**TC-ERR-005: Payment fail - Gateway timeout**
- **Input:** Payment request
- **Expected:** HTTP 500, transaction rollback, order status=PENDING, error message "Payment processing failed"
- **Mock:** PaymentGateway throw TimeoutException

**TC-ERR-006: Xóa item từ completed order**
- **Input:** `{orderId: "order-completed"}` (status=COMPLETED)
- **Expected:** HTTP 400, error "Cannot modify completed order"
- **Mock:** None

**TC-ERR-007: Database connection lost khi tạo order**
- **Input:** Any order creation request
- **Expected:** HTTP 500, transaction rollback, no partial data in DB
- **Mock:** DAO throw SQLException

**TC-ERR-008: Stock deduction race condition**
- **Input:** 2 concurrent orders cho product có stock = 1
- **Expected:** 1 success, 1 fail với "Insufficient stock" (optimistic locking)
- **Mock:** None (test concurrency)

---

### **Module 3: Inventory (5 tests)**

**TC-ERR-009: Upload invalid file format (PDF)**
- **Input:** PDF file thay vì image
- **Expected:** HTTP 400, error "Invalid image format. Only JPG, PNG allowed"
- **Mock:** None

**TC-ERR-010: Tạo product với missing required fields**
- **Input:** `{name: null, price: 50000}`
- **Expected:** HTTP 400, validation error "Product name is required"
- **Mock:** None

**TC-ERR-011: Update non-existent product**
- **Input:** `{productId: "non-existent"}`
- **Expected:** HTTP 404, error "Product not found"
- **Mock:** None

**TC-ERR-012: Tạo table với số bàn trùng trong room**
- **Input:** `{roomId: "room-001", tableNumber: 5}` (already exists)
- **Expected:** HTTP 409, error "Table number already exists in this room"
- **Mock:** None

**TC-ERR-013: Low stock alert fail - EmailService unavailable**
- **Input:** Stock threshold trigger
- **Expected:** Alert logged in DB, error logged, system continues (no throw)
- **Mock:** EmailService throw Exception

---

### **Module 4: Employee (4 tests)**

**TC-ERR-014: Tạo employee với duplicate email**
- **Input:** `{email: "existing@liteflow.com"}`
- **Expected:** HTTP 409 Conflict, error "Email already exists"
- **Mock:** None

**TC-ERR-015: Clock-in với invalid employee ID**
- **Input:** `{employeeId: "non-existent"}`
- **Expected:** HTTP 404, error "Employee not found"
- **Mock:** None

**TC-ERR-016: Assign shift với past date**
- **Input:** `{date: "2025-10-01"}` (date < today)
- **Expected:** HTTP 400, error "Cannot assign shift in the past"
- **Mock:** None

**TC-ERR-017: Calculate payroll fail - DB error**
- **Input:** Valid payroll request
- **Expected:** HTTP 500, error logged, no partial payroll data
- **Mock:** DAO throw SQLException

---

### **Module 5: Kitchen (1 test)**

**TC-ERR-018: Update non-existent order**
- **Input:** `{orderId: "non-existent"}`
- **Expected:** HTTP 404, error "Order not found"
- **Mock:** None

---

### **Module 6: Reservation (3 tests)**

**TC-ERR-019: Create reservation với past date**
- **Input:** `{date: "2025-10-01"}` (date < today)
- **Expected:** HTTP 400, error "Cannot create reservation in the past"
- **Mock:** None

**TC-ERR-020: Confirm arrival của cancelled reservation**
- **Input:** `{reservationId: "res-001"}` (status=CANCELLED)
- **Expected:** HTTP 400, error "Cannot confirm cancelled reservation"
- **Mock:** None

**TC-ERR-021: Cancel completed reservation**
- **Input:** `{reservationId: "res-001"}` (status=COMPLETED)
- **Expected:** HTTP 400, error "Cannot cancel completed reservation"
- **Mock:** None

---

### **Module 7: Procurement (2 tests)**

**TC-ERR-022: Create PO với invalid supplier ID**
- **Input:** `{supplierId: "non-existent"}`
- **Expected:** HTTP 404, error "Supplier not found"
- **Mock:** None

**TC-ERR-023: Record GR với invalid PO ID**
- **Input:** `{poId: "non-existent"}`
- **Expected:** HTTP 404, error "Purchase Order not found"
- **Mock:** None

---

### **Module 8: Reporting (1 test)**

**TC-ERR-024: Generate report với invalid date range (start > end)**
- **Input:** `{startDate: "2025-10-31", endDate: "2025-10-01"}`
- **Expected:** HTTP 400, error "Invalid date range: start date must be before end date"
- **Mock:** None

---

## 📋 TEST CASE SUMMARY

| Module | Happy Path | Edge Cases | Error Scenarios | **Total** | Coverage Target |
|--------|-----------|-----------|----------------|----------|----------------|
| **1. Authentication & RBAC** | 6 | 3 | 2 | **11** | 75% |
| **2. Cashier/POS Order** | 10 | 6 | 6 | **22** | 80% |
| **3. Inventory Management** | 7 | 5 | 5 | **17** | 75% |
| **4. Employee & HR** | 6 | 4 | 4 | **14** | 70% |
| **5. Kitchen Management** | 2 | 1 | 1 | **4** | 65% |
| **6. Table Reservation** | 4 | 3 | 3 | **10** | 70% |
| **7. Procurement** | 3 | 2 | 2 | **7** | 65% |
| **8. Reporting & Analytics** | 2 | 1 | 1 | **4** | 60% |
| **9. Frontend E2E** | 10 | 0 | 0 | **10** | N/A |
| **TỔNG CỘNG** | **50** | **25** | **24** | **99** | **≥70%** |

---

## 🎯 INTEGRATION POINTS COVERAGE

**Các điểm tích hợp được test:**

✅ **Frontend ↔ Backend:** JSP form submit → Servlet → Response (TC-FE-001 đến TC-FE-010)  
✅ **Servlet ↔ Service:** Request handling → Business logic execution (All TC-HP, TC-EDGE, TC-ERR)  
✅ **Service ↔ DAO:** Transaction management, query execution (All backend tests)  
✅ **DAO ↔ Database:** CRUD, constraints, cascade delete (TC-ERR-007, TC-ERR-017)  
✅ **Cross-module Integration:**
  - Order → Inventory (stock deduction): TC-HP-007, TC-EDGE-005, TC-ERR-008
  - Order → Kitchen (notification): TC-HP-007, TC-HP-031
  - Order → Payment (transaction): TC-HP-011, TC-HP-012, TC-ERR-005
  - Reservation → Order (arrival): TC-HP-034
  - Procurement → Inventory (goods receipt): TC-HP-037
  - Employee → Payroll (attendance): TC-HP-029
  
✅ **External Services Mock:**
  - EmailService: TC-HP-004, TC-HP-005, TC-HP-006, TC-HP-024
  - PaymentGateway: TC-HP-012, TC-ERR-005
  - OAuth2 API: TC-HP-002
  - SMSService: TC-HP-032, TC-HP-035
  
✅ **Concurrency & Race Conditions:** TC-EDGE-008, TC-ERR-008  
✅ **Transaction Rollback:** TC-ERR-005, TC-ERR-007, TC-ERR-017  
✅ **Scheduled Jobs:** TC-EDGE-022 (Overdue reservation check)

---

## 🚀 EXECUTION PRIORITY

**Phase 1 (Critical - Week 1):**
- Module 1 (Auth): 11 tests
- Module 2 (Order): 22 tests
- **Target:** 33 tests, P0 coverage

**Phase 2 (High - Week 2):**
- Module 3 (Inventory): 17 tests
- Module 4 (Employee): 14 tests
- **Target:** 31 tests, P1 coverage

**Phase 3 (Medium - Week 3):**
- Module 5 (Kitchen): 4 tests
- Module 6 (Reservation): 10 tests
- Module 9 (Frontend E2E): 10 tests
- **Target:** 24 tests

**Phase 4 (Low - Week 4):**
- Module 7 (Procurement): 7 tests
- Module 8 (Reporting): 4 tests
- **Target:** 11 tests

**Total:** 99 tests over 4 weeks

---

## 📝 NOTES

**Mock Strategy:**
- Chỉ mock external services (Email, Payment, SMS, OAuth2)
- Tất cả business logic, DAO, DB integration dùng real components
- SQL Server test instance (clone production schema)

**Data Preparation:**
- Seed data: 10 users (các roles), 20 products, 10 tables, 5 sample orders
- Database reset sau mỗi test class (transaction rollback)
- `TestDataBuilder.java` quản lý test data generation

**Test Helpers:**
- `ServletTestHelper.java`: Mock HttpServletRequest/Response
- `MockServiceHelper.java`: Quản lý external service mocks
- `TestDataBuilder.java`: Generate entities

**Coverage Report:**
- JaCoCo plugin: Line ≥70%, Branch ≥60%
- Surefire report: Test execution time, pass/fail rate
- Export: HTML + XML cho CI/CD

---

**Tiếp theo:** PR3 - Test Environment Setup & Seed Data → PR4 - Test Implementation

**Lưu:** `prompts/outputs_2/Output_PR2.md` ✅
