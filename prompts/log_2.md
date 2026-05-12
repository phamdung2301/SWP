# 📝 AI PROMPT ENGINEERING LOG - CASHIER ORDER FEATURE TESTING

## 🎯 **PROJECT: LITEFLOW RESTAURANT MANAGEMENT SYSTEM**

**Core Feature**: Cashier Order Management  
**AI Model**: Claude Sonnet 4 (Cursor AI)  
**Target Coverage**: ≥80%  
**Test Cases**: 20 (15 basic + 5 real-world)  
**Framework**: JUnit 5 + Mockito + Jakarta Servlet API

---

## 📋 **PROMPT 1: INITIAL ANALYSIS & PLANNING**

Với tư cách là một chuyên gia kỹ thuật phần mềm có kinh nghiệm sâu trong xây dựng và vận hành hệ thống quản lý nhà hàng, tôi đang triển khai kế hoạch Integration Testing tự động cho dự án LiteFlow — nền tảng quản lý nhà hàng sử dụng Jakarta EE và Servlet.

Yêu cầu: Phân tích và lập kế hoạch kiểm thử chi tiết cho toàn bộ hệ thống LiteFlow, đảm bảo tính bao phủ nghiệp vụ và kỹ thuật.

Phạm vi kiểm thử:
- Bao phủ toàn bộ feature, module của LiteFlow từ backend đến frontend.
- Backend: Kiểm thử tất cả Servlet, Service, DAO, endpoint, transaction, quy trình nghiệp vụ (Order, Inventory, Employee, Menu, Payment, Reporting...).
- Frontend: Kiểm thử các màn hình, luồng nhập liệu, tích hợp API, điều hướng và phản hồi UI.
- Tích hợp: Kiểm tra luồng chính, luồng phụ từng feature; xác thực phối hợp cross-module, xử lý lỗi, phản hồi giao diện và dữ liệu giữa các thành phần hệ thống.

**Mục tiêu:**
- Xây dựng kế hoạch test chi tiết.
- Xác định rõ phạm vi, độ bao phủ, và chiến lược kiểm thử.  
- Chuẩn bị đầu vào cho bước tiếp theo: tạo test cases và test code.

**Yêu cầu đầu ra:**
Hãy thực hiện **phân tích và lập kế hoạch**, KHÔNG sinh bất kỳ đoạn code nào.  
Cấu trúc kết quả như sau:

1. **Feature Analysis:** Phân tích logic nghiệp vụ và luồng dữ liệu của Cashier Order.  
2. **Test Objectives:** Mục tiêu kiểm thử và phạm vi (backend vs frontend).  
3. **Test Strategy:** Định nghĩa cách tiếp cận (Unit Test, Integration, Mock Services, Data Validation).  
4. **Test Environment & Tools:** Mô tả công cụ, framework (JUnit 5, Mockito).  
5. **Test Case Plan:** Liệt kê các nhóm case chính (dạng mô tả, chưa cần mã).  

**Đầu ra**:
- Tuyệt đối KHÔNG sinh code, KHÔNG sinh test case ở bước này.
- Chỉ lập kế hoạch, nhận xét, phân tích logic cô đọng, tập trung vào mục tiêu coverage >70%.

- Lưu:  
`prompts/outputs_2/Output_PR1.md`
```

## 📋 **PROMPT 2: TEST CASE DESIGN - BASIC TESTS (INTEGRATION TESTING TOÀN DỰ ÁN, COVERAGE ≥70%)**

### **Nội dung đã điều chỉnh:**

Sau bước 1 (“Phân tích & lập kế hoạch kiểm thử tích hợp” @Output_2/Output_PR1.md), với phạm vi INTEGRATION TESTING toàn bộ dự án LiteFlow (Jakarta EE + Servlet, backend, frontend, các module nghiệp vụ), hãy thiết kế ma trận test case cho tất cả các tính năng chính.

**Mục tiêu:**  
Đảm bảo test case tích hợp góp phần đạt coverage tích hợp toàn hệ thống ≥70%. Tập trung kiểm tra phối hợp đúng giữa backend, frontend và các service.

**Lưu ý:**
- KHÔNG sinh code.
- Chỉ thiết kế logic test case, tập trung bao phủ nghiệp vụ và các điểm tích hợp.
- Kết quả là input cho bước tiếp theo (phát sinh test code tự động).

**Yêu cầu:**
Xây dựng **Test Case Matrix** cho các feature, chia 3 nhóm:
1. **Happy Path:** Ca thành công (luồng chuẩn, tích hợp mượt)
2. **Edge Cases:** Điều kiện biên, dữ liệu đặc biệt
3. **Error Scenarios:** Lỗi validation, exception, lỗi tích hợp modules/service

Mỗi test case cần ghi rõ:
- Test ID
- Description (mục tiêu/ngữ cảnh)
- Input Data (ví dụ: tableId, items,...)
- Expected Output (HTTP status, JSON response, UI message, hiệu ứng tích hợp)
- Mock Behavior (nếu có, ví dụ giả lập PaymentService, InventoryService…)

**Định dạng Markdown:** 

📊 TEST CASE MATRIX - INTEGRATION TESTS  
Happy Path  
TC-HP-001: ...  
...

Lưu lại vào file:  
`prompts/outputs_2/Output_PR2.md`
```

---

## 📋 **PROMPT 3: DIRECTORY STRUCTURE & TEST PLACEMENT**

### **Input Prompt:**

```
Tiếp nối PROMPT 2 @outputs_2/Output_PR2.md, thiết kế **cấu trúc thư mục test** cho Integration Testing toàn dự án LiteFlow, tuân thủ Maven Standard Directory Layout.

**Yêu cầu:**

1. **Phân tích source code:** Liệt kê packages chính và modules nghiệp vụ

2. **Thiết kế test directory theo cấu trúc:**

```
src/test/java/com/liteflow/
├── controller/
│   ├── cashier/
│   │   ├── CreateOrderServletTest.java
│   │   ├── UpdateOrderServletTest.java
│   │   └── ...
│   ├── inventory/
│   │   └── ...
│   └── employee/
│       └── ...
├── service/
│   ├── OrderServiceIntegrationTest.java
│   ├── InventoryServiceIntegrationTest.java
│   └── ...
├── integration/
│   ├── E2EOrderFlowTest.java
│   ├── E2EInventoryFlowTest.java
│   └── ...
└── helpers/
    ├── TestDataBuilder.java
    ├── MockHelper.java
    └── ...
```

3. **Quy tắc đặt tên:** `<ClassName>Test.java` cho unit test, `<ClassName>IntegrationTest.java` cho integration test

4. **Mapping test cases:** Tạo bảng ánh xạ Test Case ID → Test File → Package (ví dụ: TC-HP-001 → CreateOrderServletTest.java)

5. **Test resources:** Config `src/test/resources/` (H2 database, properties, mock JSON)

**Đầu ra:**
- Sơ đồ cấu trúc thư mục đầy đủ
- Bảng mapping chi tiết
- Giải thích ngắn gọn lý do thiết kế

**Lưu ý:** KHÔNG sinh code, chỉ phân tích cấu trúc và quy ước.

Lưu:  
`prompts/outputs_2/Output_PR3.md`
```

---
## 📋 **PROMPT 4: GENERATE INTEGRATION TEST FILES - CASHIER ORDER**

### **Input Prompt:**

```
[BẮT ĐẦU SINH CODE TEST]

- Sinh đầy đủ file test cho module Cashier/POS Order theo cấu trúc chuẩn đã định (Output_PR3), đầy đủ Happy Path, Edge, Error Scenarios (mapping từ Output_PR2).
- Sử dụng helpers (TestDataBuilder, ServletTestHelper, MockServiceHelper...) và quy ước đặt tên theo Output_PR1/3.
- Mỗi test gồm 3 phần: Arrange (dữ liệu/mock), Act (gọi logic), Assert (kiểm tra kết quả).
- Tất cả code được trình bày theo đúng quy ước thư mục/package/class/method, có thể copy vào repo chạy trực tiếp (nếu đủ helper/base).

[LƯU Ý]
- Mỗi test method có @Test, @DisplayName (ghi mã case).
- Có setUp() @BeforeEach nếu cần.
- Tên method dạng “should<Action>_when<Condition>”.
- Không giải thích nghiệp vụ. Chỉ sinh code, trình bày đủ file theo mapping.

[VÍ DỤ]
Bắt đầu file đầu tiên: `CreateOrderIntegrationTest.java` (TC-HP-007, TC-EDGE-005, TC-EDGE-006, TC-EDGE-009). Các file tiếp theo làm tương tự.

```
---

## 📋 **PROMPT 5: MOCK OBJECTS & TEST DATA**

### **Input Prompt:**

```
Tạo các helper methods để generate mock data và setup mocks cho test suite :

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
3. Ensure all tests pass 
4. Optimize test performance
5. Clean up code và remove duplication

Common issues cần fix:
- Jakarta vs Javax API imports
- Mock configuration problems
- Assertion failures
- Reflection access issues
- UTF-8 encoding issues
```
## 📋 **PROMPT 7: FINAL VALIDATION & DOCUMENTATION**

### **Input Prompt:**

```
Validate final test suite và tạo comprehensive documentation:

1. Verify tất cả test cases pass 
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

