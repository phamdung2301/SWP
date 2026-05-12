### 1. Feature Analysis

- **Business goal**: Thu ngân chọn bàn, chọn món, gửi món mới sang bếp (notify kitchen) và nhận phản hồi tạo đơn thành công kèm `orderId` để tiếp tục phục vụ/thu tiền.
- **Actors & touchpoints**:
  - **Frontend (cashier.jsp)**: UI chọn bàn, thực đơn, giỏ món, tính tiền. Hàm `notifyKitchen()` gửi POST tới `/api/order/create` với JSON gồm `tableId` (UUID) và `items` (danh sách món mới). Hiển thị thông báo thành công/thất bại, cập nhật trạng thái các món đã gửi.
  - **Backend**:
    - `CreateOrderServlet` (POST `/api/order/create`): Đọc JSON, validate input, chuyển đổi `tableId` sang UUID, gọi `OrderService.createOrderAndNotifyKitchen(...)`. Trả về JSON `{ success, message, orderId }`, HTTP 201 khi thành công; 400 cho lỗi nhập liệu; 500 cho lỗi máy chủ.
    - `OrderService`: Xác thực đầu vào cơ bản; ủy quyền tạo đơn cho `OrderDAO.createOrder(...)`; in log “thông báo bếp” (placeholder cho real-time sau này). Có thêm các API liên quan đến trạng thái đơn: `getOrdersByTable`, `getPendingOrders`, `updateOrderStatus`, `markOrderAsPreparing/Ready/Served`, `cancelOrder`.
- **Data flow (core path - notify kitchen)**:
  1) Thu ngân chọn bàn (UI lưu `selectedTable`). 2) Thêm món vào `orderItems` (có `quantity`, `price`, `note`). 3) `notifyKitchen()` tính phần “món mới” bằng `quantity - notifiedQuantity`. 4) Gửi POST `/api/order/create` với `{ tableId, items: [{ variantId, quantity, unitPrice, note }] }`. 5) Servlet parse JSON → validate → chuyển UUID → gọi Service → DAO tạo đơn → trả `orderId`. 6) Frontend nhận `{ success, orderId, message }`, hiển thị alert, cập nhật `notifiedQuantity = quantity` cho các món đã gửi, render lại UI.
- **Validation points**:
  - Frontend: bắt buộc chọn bàn; phải có ít nhất 1 món mới; tính tiền, VAT 10% (làm tròn), trạng thái enable/disable nút.
  - Backend: `tableId` không rỗng, hợp lệ định dạng UUID; `items` không rỗng; propagate lỗi phù hợp (400/500); trả Content-Type `application/json; charset=UTF-8`.

### 2. Test Objectives

- **Backend**:
  - Xác thực đầu vào và mã lỗi/HTTP status đúng trên `CreateOrderServlet` (JSON invalid/missing/UUID sai/items rỗng).
  - Đảm bảo `OrderService.createOrderAndNotifyKitchen` validate và gọi `OrderDAO.createOrder` đúng tham số; trả về `orderId` đúng; lỗi được ném đúng loại.
  - Bảo đảm phương thức trạng thái (`updateOrderStatus`, `mark*`, `cancelOrder`) chỉ nhận giá trị hợp lệ; ném `IllegalArgumentException` khi invalid.
  - Đảm bảo CORS OPTIONS đáp ứng header/200 khi cần.
- **Frontend**:
  - Hàm `addToCart`, `removeFromCart`, `updateQuantity`, `updateNote`, `updateBill`, `renderOrderItems` hoạt động đúng logic, đặc biệt cập nhật `notifiedQuantity` và định danh món.
  - `notifyKitchen()` chỉ gửi phần “mới” (delta), đóng gói payload đúng, gọi fetch đúng URL/headers; xử lý response success/failure đúng, cập nhật state và hiển thị thông báo.
  - `loadTableOrders()` chuyển đổi dữ liệu từ API sang `orderItems` đúng, hợp nhất item theo `variantId`, gắn `notified`/`notifiedQuantity` chuẩn.

### 3. Test Strategy

- **Unit tests (ưu tiên chính)**:
  - Backend: JUnit 5 + Mockito cho `OrderService` và `CreateOrderServlet` (mock `OrderDAO` và `OrderService`).
  - Frontend: Jest + jsdom để test logic JS thuần trong `cashier.jsp` (tách nhẹ hoặc sử dụng DOM fixture để gắn hàm vào `window`). Mock `fetch` và `alert`.
- **Integration-light (optional, smoke)**:
  - Kiểm tra mapping `/api/order/create` hoạt động qua `MockHttpServletRequest/Response` (không cần container thật), hoặc RestAssured nếu có embedded server trong pipeline.
- **Mocking & isolation**:
  - Mock `OrderDAO` khi test `OrderService` (sử dụng Mockito mockConstruction hoặc refactor nhẹ để tiêm dependency nếu cần).
  - Mock `OrderService` khi test `CreateOrderServlet` để tập trung vào parse/validate/HTTP status/JSON response.
  - Frontend: mock `fetch`, `alert`, đồng thời khởi tạo tối thiểu DOM để test render/update.
- **Data validation**:
  - Bộ test bao phủ JSON invalid (syntax), thiếu trường, `tableId` invalid/empty, `items` empty/null, giá trị số âm/0, `note` rất dài/unicode, `variantId` thiếu.
- **Error mapping**:
  - Phân biệt 400 (validation) vs 500 (server). Đảm bảo message thân thiện, không lộ stacktrace ra client.

### 4. Test Environment & Tools

- **Backend**:
  - JUnit 5 (Surefire), Mockito 5/Mockito-inline (mock constructor/final nếu cần), Hamcrest/AssertJ, JSONAssert (so sánh JSON), Jakarta Servlet API (mock request/response), Gson (để parse/verify JSON trong test).
- **Frontend**:
  - Node.js LTS, Jest, jsdom, `whatwg-fetch` hoặc mock fetch do Jest cung cấp.
- **Build & reports**:
  - Maven Surefire + JaCoCo cho coverage (branch + line). Báo cáo tại `target/site/jacoco`.
  - Optional: RestAssured cho smoke endpoint; Postman chỉ dùng manual verification khi cần.

### 5. Test Case Plan (nhóm case chính)

- **A. CreateOrderServlet.doPost**
  - A1. Happy path: body hợp lệ → 201, `success=true`, có `orderId` (UUID), message đúng, Content-Type JSON UTF-8.
  - A2. Body rỗng/null → 400, message “Request body không hợp lệ”.
  - A3. JSON sai cú pháp → 400 (nếu parse ném lỗi) hoặc 400 với message phù hợp.
  - A4. Thiếu `tableId` hoặc `tableId=""` → 400, message “Table ID không được rỗng”.
  - A5. `tableId` không đúng định dạng UUID → 400, message “Table ID không hợp lệ: ...”.
  - A6. Thiếu `items` hoặc `items=[]` → 400, message “Danh sách món không được rỗng”.
  - A7. `OrderService` ném `IllegalArgumentException` → 400, propagate message.
  - A8. `OrderService` ném `RuntimeException` khác → 500, message “Lỗi server: ...”.
  - A9. Đảm bảo đóng `PrintWriter`, `response` set charset UTF-8, status phù hợp.
  - A10. doOptions: trả 200, header CORS đầy đủ (`Allow-Origin`, `Allow-Methods`, `Allow-Headers`).

- **B. OrderService.createOrderAndNotifyKitchen**
  - B1. Happy path: `tableId` != null, `items` hợp lệ → gọi `OrderDAO.createOrder` đúng tham số, nhận `orderId` và return.
  - B2. `tableId = null` → ném `IllegalArgumentException("Table ID không được null")`.
  - B3. `items = null` hoặc rỗng → ném `IllegalArgumentException("Danh sách món không được rỗng")`.
  - B4. Log “thông báo bếp” được in (có thể assert bằng appender test nếu cần, optional).

- **C. OrderService trạng thái đơn**
  - C1. `updateOrderStatus` với `status` hợp lệ: gọi `OrderDAO.updateOrderStatus(orderId, status)` trả true/false.
  - C2. `status` không hợp lệ → ném `IllegalArgumentException("Trạng thái không hợp lệ: ...")`.
  - C3. `markOrderAsPreparing/Ready/Served/Cancelled` ủy quyền đúng `status`.
  - C4. `getOrdersByTable(null)` → `IllegalArgumentException`.

- **D. Frontend JS – giỏ món và tính tiền**
  - D1. `addToCart` khi chưa chọn bàn → hiển thị alert, không thêm item.
  - D2. `addToCart` thêm món mới → `orderItems` có item với `quantity=1`, `price` parse đúng, `notifiedQuantity=0`.
  - D3. `addToCart` món đã tồn tại → tăng `quantity`, giữ nguyên `notifiedQuantity` (tracking delta đúng như logic hiện tại).
  - D4. `removeFromCart` xóa item theo `variantId`.
  - D5. `updateQuantity` với `newQuantity<=0` → loại bỏ món.
  - D6. `updateNote` cập nhật `note` đúng; unicode/emoji không lỗi.
  - D7. `updateBill` tính subtotal, VAT 10% (Math.round), total; enable/disable `notifyKitchenBtn`/`checkoutBtn` dựa trên có bàn + có món.
  - D8. `renderOrderItems` hiển thị badge trạng thái đúng theo delta (`new`, `partial`, `notified`).

- **E. Frontend JS – notifyKitchen()**
  - E1. Không có món → alert yêu cầu chọn món.
  - E2. Chưa chọn bàn → alert yêu cầu chọn bàn.
  - E3. Tính đúng `itemsToNotify` = `quantity - notifiedQuantity` (chỉ > 0 mới gửi).
  - E4. Gọi `fetch` đúng URL `${contextPath}/api/order/create`, header `Content-Type: application/json`.
  - E5. Payload chứa đúng `tableId`, và mỗi item có `variantId`, `quantity` (delta), `unitPrice`, `note`.
  - E6. Response `success=true` → alert thành công, cộng dồn `notifiedQuantity = quantity` hiện tại cho các món đã gửi, render/update lại.
  - E7. Response `success=false` → alert lỗi với message.
  - E8. `fetch` ném lỗi (network/HTTP non-2xx) → alert lỗi generic.

- **F. Frontend JS – loadTableOrders() & chọn bàn**
  - F1. Khi chọn bàn `occupied` → gọi API `/api/order/table/{tableId}`, nhận dữ liệu, convert sang `orderItems` với `notified=true`, `notifiedQuantity=quantity`.
  - F2. Hợp nhất items theo `variantId` (cộng quantity, notifiedQuantity, merge note hợp lý).
  - F3. Khi bàn trống → xóa `orderItems`, render rỗng.
  - F4. Cập nhật `selectedTableInfo` hiển thị tên + phòng đúng.

### 6. Edge & Real-World Scenarios

- **Delta phức tạp**: Tăng/giảm số lượng nhiều lần trước khi notify; verify delta chính xác và không gửi 0 hoặc âm.
- **UUID thực tế**: `tableId` không phải chuỗi dạng UI tạm (vd `table1`) mà phải là UUID thật; test mapping và thông báo lỗi rõ ràng khi sai.
- **Giá trị bất thường**: `quantity=0/-1`, `unitPrice` âm hoặc NaN (frontend nên chặn; backend có thể kiểm bổ sung sau – thêm testcase kỳ vọng 400 nếu bổ sung validation).
- **Ký tự & kích thước dữ liệu**: `note` rất dài (giới hạn hợp lý?), ký tự đặc biệt/emoji; đảm bảo không lỗi encoding UTF-8 end-to-end.
- **Trạng thái đơn**: Cập nhật trạng thái không hợp lệ phải bị chặn; đường đi hợp lệ: Pending → Preparing → Ready → Served/Cancelled.
- **Network/UI**: Mạng chậm/timeout; bấm `notifyKitchen` nhiều lần liên tiếp; đảm bảo UI không gửi trùng delta hoặc hiển thị sai badge.
- **Khả năng mở rộng**: Tương lai push real-time (WebSocket/SSE) – giữ test service độc lập với cơ chế push.

### 7. Risks & Assumptions

- **Mockability của `OrderService`/`OrderDAO`**: `OrderService` khởi tạo `OrderDAO` nội bộ (final field), cần Mockito-inline hoặc mockConstruction; nếu khó, cân nhắc refactor để tiêm dependency. Giả định cho plan: dùng Mockito mockConstruction.
- **Contract `items`**: Backend nhận `List<Map<String,Object>>` – không có schema mạnh; rủi ro nhầm khóa (`variantId` vs `productId`, `unitPrice` vs `price`). Test sẽ cố định payload theo UI hiện tại; đề xuất bổ sung validation/schema rõ ràng về sau.
- **JSP JS tập trung trong một file**: Một số hàm nằm trong `cashier.jsp`; test bằng Jest cần DOM fixture. Nếu cần, cân nhắc tách JS sang file riêng để tăng khả năng test.
- **Localization**: Message tiếng Việt được assert theo text hiện tại; đổi message có thể làm test brittle.
- **CORS/ContextPath**: URL phụ thuộc `${pageContext.request.contextPath}`; trong test frontend dùng placeholder/giá trị giả lập.

### 8. Documentation Plan

- **Lưu trữ kế hoạch**: File này tại `prompts/outputs/Output_PR1.md` (plan chính thức cho PR1).
- **Nhật ký thực thi**: Ghi tiến trình, quyết định, và kết quả chạy test vào `prompts/log.md` (theo thời gian), bao gồm: số test, pass/fail, lỗi chính, hành động khắc phục.
- **Báo cáo coverage**: Bật JaCoCo; commit thư mục `target/site/jacoco/` không bắt buộc, nhưng trích xuất số liệu và ảnh chụp màn hình coverage vào `prompts/screenshots/` nếu cần minh họa; ghi liên kết đường dẫn trong `prompts/log.md`.
- **Báo cáo Surefire**: Lưu ý vị trí `target/surefire-reports/`; khi cần, đính kèm tóm tắt pass/fail vào `prompts/log.md`.
- **Theo dõi phạm vi**: Map test cases theo nhóm A–F ở trên, đánh dấu độ bao phủ Backend/Frontend, đường buồn-happy path và edge cases, đảm bảo không bỏ sót validation & error mapping.
