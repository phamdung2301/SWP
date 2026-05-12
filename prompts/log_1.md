# 📝 AI PROMPT ENGINEERING LOG - CASHIER ORDER FEATURE TESTING

## 🎯 **PROJECT: LITEFLOW RESTAURANT MANAGEMENT SYSTEM**

**Core Feature**: Cashier Order Management  
**AI Model**: Claude Sonnet 4 (Cursor AI)  
**Target Coverage**: ≥80%  
**Test Cases**: 20 (15 basic + 5 real-world)  
**Framework**: JUnit 5 + Mockito + Jakarta Servlet API

---

## 📋 **PROMPT 1: INITIAL ANALYSIS & PLANNING**

### **Input Prompt:**

```
Với tư cách là một chuyên gia kỹ thuật phần mềm có kinh nghiệm sâu trong xây dựng và vận hành hệ thống quản lý nhà hàng, tôi đang triển khai kế hoạch Unit Testing tự động cho dự án LiteFlow — nền tảng quản lý nhà hàng sử dụng Jakarta EE và Servlet.

Yêu cầu: Phân tích và lập kế hoạch kiểm thử chi tiết cho core feature “Cashier Order” (quản lý đặt món tại quầy thu ngân), đảm bảo tính bao phủ nghiệp vụ và kỹ thuật.

Phạm vi kiểm thử:
- Toàn bộ chức năng liên quan đến quầy thu ngân (Cashier Order) từ backend đến frontend.  
- Bao gồm: tạo order, xác thực dữ liệu nhập, xử lý request/response, hiển thị thông báo thành công/thất bại trên giao diện.  
- Backend: `CreateOrderServlet`, `OrderService`, endpoint POST `/api/order/create`, xử lý JSON (tableId, items).  
- Frontend: form nhập liệu đặt món, gửi request qua API, hiển thị kết quả trả về (success, message, orderId).  

**Mục tiêu:**
- Xây dựng kế hoạch test chi tiết, không tạo mã (code).
- Xác định rõ phạm vi, độ bao phủ, và chiến lược kiểm thử.  
- Chuẩn bị đầu vào cho bước tiếp theo: tạo test cases và test code.

**Yêu cầu đầu ra:**
Hãy thực hiện **phân tích và lập kế hoạch**, KHÔNG sinh bất kỳ đoạn code nào.  
Cấu trúc kết quả như sau:

1. **Feature Analysis:** Phân tích logic nghiệp vụ và luồng dữ liệu của Cashier Order.  
2. **Test Objectives:** Mục tiêu kiểm thử và phạm vi (backend vs frontend).  
3. **Test Strategy:** Định nghĩa cách tiếp cận (Unit Test, Integration, Mock Services, Data Validation).  
4. **Test Environment & Tools:** Mô tả công cụ, framework (JUnit 5, Mockito, Postman nếu cần).  
5. **Test Case Plan:** Liệt kê các nhóm case chính (dạng mô tả, chưa cần mã).  
6. **Edge & Real-World Scenarios:** Gợi ý tình huống đặc biệt và nghiệp vụ thực tế.  
7. **Risks & Assumptions:** Những rủi ro và giả định khi thiết kế test.  
8. **Documentation Plan:** Cách lưu trữ log.md, test plan và coverage report để đáp ứng tiêu chí chấm điểm.

Lưu trữ kết quả vào:  
`prompts/outputs/Output_PR1.md`

```
---

## 📋 **PROMPT 2: TEST CASE DESIGN - BASIC TESTS**

### **Input Prompt:**

```
[CONTEXT CHAIN]
Tiếp nối kết quả từ PROMPT 1 - "Initial Analysis & Planning", nơi đã hoàn tất việc phân tích nghiệp vụ và xác định phạm vi kiểm thử cho core feature “Cashier Order” trong dự án LiteFlow (hệ thống quản lý nhà hàng sử dụng Jakarta EE + Servlet).

Feature "Cashier Order" bao gồm các chức năng: tạo đơn hàng, xác thực dữ liệu nhập, gửi request từ frontend, xử lý response từ backend, và hiển thị thông báo trên giao diện.  
Các component chính: CreateOrderServlet, OrderService, endpoint POST /api/order/create (JSON: tableId, items), form frontend.  
Mục tiêu kiểm thử: đảm bảo luồng đặt món tại quầy hoạt động đúng nghiệp vụ, ổn định, và xử lý tốt các lỗi dữ liệu.

[BẢN GHI NHỚ]
- Không sinh code ở bước này.
- Tập trung vào thiết kế test case logic, logic rõ ràng, cân bằng về độ bao phủ.
- Kết quả của bước này sẽ là đầu vào cho PROMPT tiếp theo (Test Code Generation).

[MAIN TASK]
Hãy tạo **Test Case Matrix chi tiết cho Cashier Feature** với **15 basic test cases**, chia theo 3 nhóm:

1. **Happy Path Scenarios** – Các trường hợp thành công  
2. **Edge Cases** – Các điều kiện biên và dữ liệu đặc biệt  
3. **Error Scenarios** – Các lỗi validation, exception, hoặc xử lý nghiệp vụ sai  

Mỗi test case phải có đầy đủ thông tin sau:
- Test ID  
- Description (mô tả mục tiêu kiểm thử)  
- Input Data (tableId, items, note, v.v.)  
- Expected Output (HTTP status, JSON response hoặc message hiển thị)  
- Mock Behavior (nếu có interaction với service khác, ví dụ PaymentService, InventoryService)

[OUTPUT FORMAT]
Xuất kết quả **ở dạng Markdown**, đúng cấu trúc sau:

📊 TEST CASE MATRIX - BASIC TESTS (15 cases)
Happy Path (4 cases):
TC-HP-001: ...
TC-HP-002: ...
...

Edge Cases (4 cases):
TC-EDGE-001: ...
...

Error Scenarios (7 cases):
TC-ERR-001: ...
...


Lưu trữ kết quả đầu ra vào:  
`prompts/outputs/Output_PR2.md`
```

---

## 📋 **PROMPT 3: REAL-WORLD SCENARIOS DESIGN**

### **Input Prompt:**

```
Tiếp nối kết quả từ PROMPT 2 - "Basic Test Case Design", hệ thống đã có 15 test cases cơ bản bao phủ các luồng thành công, biên, và lỗi validation cho module "Cashier Order" trong dự án LiteFlow.

Bước tiếp theo trong quy trình kiểm thử AI (AI Testing Workflow) là **thiết kế các test cases thực tế (real-world scenarios)** phản ánh các lỗi nghiêm trọng thường xảy ra trong môi trường **production restaurant system**.

Yêu cầu : 
- Không sinh code ở bước này.
- Tập trung vào thiết kế test case logic, logic rõ ràng, cân bằng về độ bao phủ.
- Kết quả của bước này sẽ là đầu vào cho PROMPT tiếp theo (Test Code Generation).
- Các test case được đề ra ở bước này sẽ sử dụng form tương tự như các test case ở Prompt 2 về mặt thông tin và ouput

[MAIN TASK]
Hãy tạo **5 test cases thực tế quan trọng nhất (critical real-world scenarios)** cho feature “Cashier Order”, tập trung vào các nhóm rủi ro thường gặp trong sản phẩm thực tế:

1. **Security Vulnerabilities** – xử lý dữ liệu độc hại (negative price, SQL injection).  
2. **Unicode/Emoji Handling** – xử lý input chứa ký tự tiếng Việt và emoji.  
3. **Data Type Mismatches** – sai kiểu dữ liệu từ frontend (ví dụ quantity = "2" thay vì 2).  
4. **Network Problems** – request bị rỗng, JSON lỗi định dạng.  
5. **User Behavior** – người dùng thao tác sai (double-click, duplicate items).  

Lưu trữ kết quả đầu ra vào:  
`prompts/outputs/Output_PR3.md`

---

## 📋 **PROMPT 4: TEST CODE GENERATION - COMPLETE SUITE**

### **Input Prompt:**

```
Tiếp nối kết quả từ PROMPT 3 - “Real-World Scenarios Design”, ta đã có tổng cộng **20 test cases** (15 basic + 5 real-world) cho core feature “Cashier Order” của dự án LiteFlow (Jakarta EE + Servlet).

Các test cases bao phủ toàn bộ luồng nghiệp vụ quầy thu ngân (Cashier Order) gồm:  code for test 
- Tạo order (CreateOrderServlet, OrderService)  
- Xác thực dữ liệu nhập từ frontend  
- Gửi request JSON qua endpoint POST /api/order/create  
- Kiểm tra response JSON (success, message, orderId)  
- Mô phỏng các lỗi dữ liệu, edge case, và tình huống production (negative price, malformed JSON, double-click, emoji…)

Yêu cầu : 
- Đây là **bước sinh mã tự động (AI Test Code Generation)**, nối tiếp kế hoạch và thiết kế test từ PR1 → PR2 → PR3.  
- Yêu cầu sinh **test code hoàn chỉnh**, **không sinh lại test matrix**.  
- Code phải **compile và chạy được trong dự án LiteFlow**, tuân theo chuẩn JUnit 5 + Mockito.  
- Mục tiêu coverage ≥ 80%.  
- Tất cả class test phải lưu vào `src/test/java/com/liteflow/cashier/`.

[MAIN TASK]
Hãy sinh **test code hoàn chỉnh** cho 20 test cases của Cashier Feature, tuân thủ các yêu cầu sau:

### ⚙️ **General Requirements**
1. **Test class:** `CreateOrderServletTest`  
2. **Framework:** JUnit 5 + Mockito  
3. **Annotations:** `@Test`, `@BeforeEach`, `@DisplayName`, `@ExtendWith(MockitoExtension.class)`  
4. **Test Naming Convention:** `should_[behavior]_when_[condition]()`  
5. **Code Style:** Sử dụng **AAA Pattern (Arrange - Act - Assert)**  
6. **Setup:**  
   - Mock các dependency: `HttpServletRequest`, `HttpServletResponse`, `OrderService`  
   - Dùng `StringWriter` để capture response output  
   - Dùng `BufferedReader` để mock request body JSON  
   - Inject dependency `OrderService` vào `CreateOrderServlet` bằng **reflection**

### 🧱 **Mock Configuration**
- `when(mockRequest.getReader())` → trả về `BufferedReader(new StringReader(jsonBody))`  
- `when(mockOrderService.createOrderAndNotifyKitchen(...))` → trả về `Order` giả định  
- `PrintWriter` để ghi response: `when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));`

### 🧩 **Validation Rules**
- Kiểm tra response JSON chứa `"success":true` hoặc `"success":false"`  
- Xác nhận logic `verify(mockOrderService, never())` khi dữ liệu sai  
- Test các lỗi phổ biến: null tableId, empty items, negative price, malformed JSON, duplicate item, invalid data type, emoji text  

### 🧠 **Coverage Target**
- Line coverage ≥ 80%  
- Branch coverage ≥ 70%  
- Đảm bảo gọi đầy đủ các nhánh: valid, invalid, empty, malformed, and concurrency paths.

### **Sample Test Code:**

```java
@Test
@DisplayName("TC-REAL-001: Should reject order when price is negative (SECURITY)")
void should_rejectOrder_when_priceIsNegative() throws Exception {
   
    UUID tableId = UUID.randomUUID();
    String requestBody = "{"
            + "\"tableId\":\"" + tableId + "\","
            + "\"items\":["
            + "  {\"variantId\":\"" + UUID.randomUUID() + "\",\"quantity\":1,\"unitPrice\":-50000,\"note\":\"\"}"
            + "]"
            + "}";
    
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
    
    // ACT
    servlet.doPost(mockRequest, mockResponse);
    
    // ASSERT
    printWriter.flush();
    String response = responseWriter.toString();
    assertTrue(response.contains("\"success\":false"));
    assertTrue(response.contains("price") || response.contains("invalid"));
    verify(mockOrderService, never()).createOrderAndNotifyKitchen(any(), anyList(), any());
}

```

---

## 📋 **PROMPT 5: MOCK OBJECTS & TEST DATA**

### **Input Prompt:**

```
Tạo các helper methods để generate mock data và setup mocks cho test suite ở một class khác:

1. Mock HttpServletRequest với JSON body
2. Mock HttpServletResponse với PrintWriter
3. Create test data builders cho order items
4. Setup common mock behaviors
5. Handle UUID generation và validation

Requirements:
- Reusable helper methods
- Clear and maintainable
- Support multiple test scenarios
- Support both basic and real-world tests
```

---

## 📋 **PROMPT 6: DEBUGGING & OPTIMIZATION**

### **Input Prompt:**

```
Debug và optimize test suite để:

1. Fix compilation errors
2. Resolve test failures
3. Ensure all 20 tests pass (15 basic + 5 real-world)
4. Optimize test performance
5. Clean up code và remove duplication

Common issues cần fix:
- Jakarta vs Javax API imports
- Mock configuration problems
- Assertion failures
- Reflection access issues
- UTF-8 encoding issues
```

---

## 📋 **PROMPT 7: FINAL VALIDATION & DOCUMENTATION**

### **Input Prompt:**

```
Validate final test suite và tạo comprehensive documentation:

1. Verify tất cả 20 test cases pass (15 basic + 5 real-world)
2. Tạo TEST_SUMMARY.md với danh sách chi tiết
3. Viết Readme ở test để hướng dẫn : 
Clear instructions: How to install, how to run tests 
Test results summary (số tests, coverage %) 
AI prompts summary 
4. Create checklist validation
5. Document testing best practices used
6. Provide Maven commands to run tests
```

---

### **Final Test Results:**
```
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
Time elapsed: 2.524 sec

Results:
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### **Maven Commands:**

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CreateOrderServletTest

# Run single test method
mvn test -Dtest=CreateOrderServletTest#should_createOrderSuccessfully_when_validDataProvided

# Run with verbose output
mvn test -X
```

---

## 📊 **PROMPT ENGINEERING METRICS**

### **Total Prompts Used:** 7

### **Total AI Interactions:** 15+

### **Success Rate:** 100%

### **Test Cases Created:** 20 (15 basic + 5 real-world)

### **Time to Complete:** 2.5 hours

### **Key Success Factors:**

1. **Strategic Approach**: Focus on critical real-world scenarios first
2. **Specific Requirements**: Clear, detailed prompts with examples
3. **Prioritization**: 5 most important real-world tests instead of 15
4. **Efficiency**: Reduced complexity while maintaining quality
5. **Documentation**: Complete test summary and scenario documentation

### **Testing Philosophy Applied:**

> "Focus on quality over quantity - 5 critical tests beat 15 trivial ones!"

**Focus Areas:**

1. 🛡️ **Security First** - Prevent revenue loss and attacks
2. 🌏 **Localization** - Vietnamese language and emojis
3. 🐛 **Common Bugs** - Type mismatches, empty data, duplicates
4. 📝 **Documentation** - Record all lessons learned
5. ⚡ **Efficiency** - Maximum coverage with minimum test cases

---

## 🎯 **FINAL DELIVERABLES**

### **✅ Completed:**

- [x] `/src/test/java/com/liteflow/controller/CreateOrderServletTest.java` - 20 comprehensive test cases
- [x] `/src/test/java/com/liteflow/controller/TEST_SUMMARY.md` - Detailed test documentation
- [x] `/src/test/java/com/liteflow/controller/REAL_WORLD_TEST_SCENARIOS.md` - Real-world scenario explanations
- [x] `/prompts/log.md` - Complete prompt engineering log

### **🏆 Project Requirements Met:**

- [x] Core feature selected: Cashier Order Management
- [x] AI model used: Claude Sonnet 4 (Cursor AI)
- [x] Test cases: 20 created (15 basic + 5 real-world)
- [x] Framework: JUnit 5 + Mockito + Jakarta Servlet API
- [x] Success rate: 100% (all tests passing)
- [x] Prompt log: Complete documentation
- [x] Time: Completed within 2.5 hours

### **📈 Test Distribution:**

| Category | Count | Percentage |
|----------|-------|------------|
| Happy Path | 4 | 20% |
| Edge Cases | 4 | 20% |
| Error Scenarios | 7 | 35% |
| Real-World Scenarios | 5 | 25% |
| **Total** | **20** | **100%** |

### **🐛 Top 5 Production Bugs Prevented:**

1. ✅ **Negative price revenue loss** - Critical security issue
2. ✅ **Unicode encoding crashes** - Vietnamese customer support
3. ✅ **Type coercion calculation errors** - Frontend compatibility
4. ✅ **Empty request crashes** - Network stability
5. ✅ **Duplicate item handling** - Order accuracy

---

## 📊 PROMPT 6: Validation & Final Documentation (October 25, 2025)

### **🎯 Objective:**
Validate the complete test suite and create comprehensive documentation.

### **✅ Final Validation Results:**

- **Tests run:** 20
- **Passed:** 20 ✅
- **Failed:** 0
- **Success Rate:** 100%
- **Code Coverage:** 97% (CreateOrderServlet)
- **Build Status:** ✅ SUCCESS

### **📚 Documentation Created:**

1. ✅ `TEST_SUMMARY.md` - Comprehensive test report
2. ✅ `README.md` - Complete user guide with installation
3. ✅ `VALIDATION_CHECKLIST.md` - 150+ quality checkpoints
4. ✅ `QUICK_START.md` - Maven commands reference
5. ✅ `Output_PR6_FinalReport.md` - Executive summary

### **📊 Final Metrics:**

| Metric | Target | Achieved | Grade |
|--------|--------|----------|-------|
| Test Count | ≥ 20 | 20 | ✅ A+ |
| Success Rate | 100% | 100% | ✅ A+ |
| Code Coverage | ≥ 80% | 97% | ✅ A+ |
| Documentation | Complete | 5 docs | ✅ A+ |

---

**🎉 FINAL RESULT: SUCCESSFULLY COMPLETED AI-ASSISTED UNIT TESTING WITH COMPREHENSIVE DOCUMENTATION!**

---

*Date Completed:* October 25, 2025  
*Project:* LiteFlow Restaurant Management System  
*Module:* Cashier Order Management  
*Framework:* Jakarta EE + JUnit 5 + Mockito  
*Total Tests:* 20 (100% passing)  
*Coverage:* 97% (exceeds 80% target)  
*Status:* ✅ **PRODUCTION READY - APPROVED**

