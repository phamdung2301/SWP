<!-- b05fa4d4-2258-4322-b4c5-58a75e6f53ed 3644fba3-aa2b-4fbc-86a5-a27a9c2d631f -->
# Kế hoạch dọn dẹp Debug Files và Log Statements

## 1. Xóa các Debug HTML Files

Xóa các file HTML test/debug không cần thiết trong production:

- `src/main/webapp/debug-alerts.html`
- `src/main/webapp/test-add-button.html`
- `src/main/webapp/test-notification-api.html`
- `src/main/webapp/report/test-top-products.html`

## 2. Xóa các Debug/Test Servlets

Xóa các servlet chỉ dùng cho debug/testing:

- `src/main/java/com/liteflow/controller/alert/AlertTestServlet.java`
- `src/main/java/com/liteflow/controller/sales/SalesInvoiceTestServlet.java`
- `src/main/java/com/liteflow/controller/report/TestRevenueAPIServlet.java`
- `src/main/java/com/liteflow/controller/report/TestJPQLServlet.java`
- `src/main/java/com/liteflow/controller/auth/DebugHashServlet.java`
- `src/main/java/com/liteflow/controller/auth/DebugOtpServlet.java`
- `src/main/java/com/liteflow/controller/api/ChatBotDebugServlet.java`

## 3. Xóa Test JSP Files

- `src/main/webapp/alert/test.jsp` (nếu chỉ dùng cho AlertTestServlet)

## 4. Giảm Log Statements trong Java

Loại bỏ hoặc comment các `System.out.println` debug statements trong:

- [ProductService.java](src/main/java/com/liteflow/service/inventory/ProductService.java) - các debug logs trong `getAllProducts()`
- [ProductServlet.java](src/main/java/com/liteflow/controller/inventory/ProductServlet.java) - debug logs trong doGet và doPost
- [PayrollService.java](src/main/java/com/liteflow/service/payroll/PayrollService.java) - nhiều debug logs trong payroll generation
- [PayrollServlet.java](src/main/java/com/liteflow/controller/payroll/PayrollServlet.java) - debug logs
- [GPTService.java](src/main/java/com/liteflow/service/ai/GPTService.java) - debug logs với emoji
- [VNPayUtil.java](src/main/java/com/liteflow/util/VNPayUtil.java) - verbose debug logs
- [DemandForecastDAO.java](src/main/java/com/liteflow/dao/analytics/DemandForecastDAO.java) - debug logs với emoji
- Các file khác có `System.out.println` debug statements

**Lưu ý:** Giữ lại `System.err.println` cho error logging quan trọng, chỉ xóa các debug logs không cần thiết.

## 5. Giảm Console.log trong JavaScript

Loại bỏ hoặc comment các `console.log` debug statements trong:

- [cashier.js](src/main/webapp/js/cashier.js) - nhiều debug logs
- [roomtable-enhanced.js](src/main/webapp/js/roomtable-enhanced.js) - debug logs
- [dashboard-revenue.js](src/main/webapp/js/dashboard-revenue.js) - debug logs
- [dashboard-enhancements.js](src/main/webapp/js/dashboard-enhancements.js) - debug logs
- Các file JS khác có console.log

## 6. Xóa Debug Functions trong JSP/JS

- Xóa function `debugTables()` trong [roomtable.jsp](src/main/webapp/inventory/roomtable.jsp)
- Xóa các test functions như `window.testSort`, `testSorting()` trong roomtable.jsp
- Xóa các debug event listeners không cần thiết
- Xóa các override console.log trong test HTML files (nếu còn)

## 7. Kiểm tra và dọn dẹp

- Kiểm tra các file trong `target/` có copy các debug files không (sẽ tự động xóa khi rebuild)
- Đảm bảo không ảnh hưởng đến functionality chính
- Giữ lại error logging cần thiết cho production debugging

## Thứ tự thực hiện

1. Xóa debug HTML files
2. Xóa debug/test servlets
3. Xóa test JSP files
4. Giảm log statements trong Java (từng file một)
5. Giảm console.log trong JavaScript (từng file một)
6. Xóa debug functions trong JSP/JS
7. Test lại để đảm bảo không có lỗi

### To-dos

- [ ] Xóa các debug HTML files: debug-alerts.html, test-add-button.html, test-notification-api.html, test-top-products.html
- [ ] Xóa các debug/test servlets: AlertTestServlet, SalesInvoiceTestServlet, TestRevenueAPIServlet, TestJPQLServlet, DebugHashServlet, DebugOtpServlet, ChatBotDebugServlet
- [ ] Xóa test.jsp trong alert folder nếu chỉ dùng cho testing
- [ ] Giảm System.out.println debug logs trong Java files (ProductService, ProductServlet, PayrollService, GPTService, VNPayUtil, etc.)
- [ ] Giảm console.log debug statements trong JavaScript files (cashier.js, roomtable-enhanced.js, dashboard files, etc.)
- [ ] Xóa debug functions trong JSP/JS (debugTables, testSort, etc.)