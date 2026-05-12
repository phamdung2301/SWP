📊 REAL-WORLD TEST CASE MATRIX (5 critical cases)

Critical Production Scenarios:

TC-REAL-001: Security - Negative Price Attack

Scenario: Hệ thống nhận dữ liệu món có `unitPrice` âm từ frontend (do bị chỉnh sửa payload trên DevTools/Proxy), ví dụ món Trà chanh có `unitPrice = -50000` nhằm giảm tổng tiền thanh toán.

Priority: CRITICAL

Test: 
- Description: Gửi POST `/api/order/create` với body `{ tableId: <UUID>, items: [{ variantId: "v-1001", quantity: 2, unitPrice: -50000, note: "Không đá" }] }`.
- Test Objective: Đảm bảo backend phát hiện giá âm và từ chối tạo đơn; UI không cập nhật `notifiedQuantity`.
- Expected Behavior: HTTP 400 với message validation rõ ràng (hiện tại chưa có validation giá ở backend; test này sẽ làm lộ gap). Ở frontend, hiển thị thông báo lỗi thân thiện: "Giá món không hợp lệ". Không gọi DAO tạo đơn.
- Impact: Âm doanh thu, chênh lệch kho và sổ sách, rủi ro gian lận.
- Comment "REAL SCENARIO": Do nhân viên hoặc khách hàng tinh ranh có thể sửa payload (Burp Suite) để lách logic UI.

TC-REAL-002: Security - SQL Injection via Note Field

Scenario: Người dùng thêm ghi chú có chuỗi nguy hiểm như `"'); DROP TABLE orders; --"` hoặc Unicode trông vô hại nhưng mục đích phá hoại, gửi qua trường `note`.

Priority: CRITICAL

Test:
- Description: Gửi POST `/api/order/create` với item có `note` chứa chuỗi SQL injection, và thêm emoji để kiểm tra encode: `"Không hành; ") DROP TABLE orders; -- 😊"`.
- Test Objective: Xác nhận lớp DAO dùng PreparedStatement/parameterized queries (không nối chuỗi), dữ liệu được lưu/từ chối an toàn; không lỗi 500.
- Expected Behavior: HTTP 201 (nếu business cho phép mọi note) hoặc 400 (nếu có policy lọc); tuyệt đối không thực thi câu lệnh độc hại, không lộ stacktrace; log an toàn.
- Impact: Mất dữ liệu, rò rỉ dữ liệu, ngừng dịch vụ.
- Comment "REAL SCENARIO": Ghi chú bếp thường bị lợi dụng vì ít được kiểm soát; các chuỗi đặc biệt có thể phá vỡ câu lệnh SQL nếu không bind parameter.

TC-REAL-003: Unicode/Emoji Handling - Ghi chú tiếng Việt + emoji dài

Scenario: Thu ngân nhập ghi chú dài với tiếng Việt có dấu và emoji: "Không hành, ít đường 😊😊😊 – làm nhanh giúp bàn VIP Tầng 2" (200–500 kí tự).

Priority: HIGH

Test:
- Description: Gửi POST hợp lệ với note dài và nhiều emoji.
- Test Objective: Kiểm tra end-to-end UTF-8: servlet set `application/json; charset=UTF-8`, Gson parse/serialize đúng, DB lưu/đọc không lỗi, UI hiển thị chính xác.
- Expected Behavior: HTTP 201; message thành công; UI hiển thị ghi chú không bị lỗi font/ký tự lạ, không cắt xén bất ngờ.
- Impact: Trải nghiệm người dùng kém, bếp hiểu sai yêu cầu nếu ghi chú bị hỏng.
- Comment "REAL SCENARIO": Nhà hàng Việt thường có yêu cầu tùy biến (ít đường/ít đá, thêm topping), emoji được dùng nhiều trong trao đổi nội bộ.

TC-REAL-004: Data Type Mismatch - quantity là chuỗi "2"

Scenario: Frontend (do bug) gửi `quantity` dưới dạng string thay vì number, ví dụ `"2"` hoặc `"02"`.

Priority: HIGH

Test:
- Description: Gửi POST với body `{ tableId:<UUID>, items:[{ variantId:"v-2001", quantity:"2", unitPrice:45000 }] }`.
- Test Objective: Xác minh backend chịu được sai kiểu (Gson parse -> Double) hay trả về 400; đảm bảo không tạo đơn với quantity lỗi.
- Expected Behavior: Lý tưởng: 400 với thông điệp kiểu dữ liệu không hợp lệ hoặc convert an toàn sang số nguyên 2 (nếu policy cho phép) và vẫn 201. Cần cố định hành vi mong muốn cho dev.
- Impact: Lệch số lượng, sai tiền; khó truy vết lỗi do parse ngầm.
- Comment "REAL SCENARIO": Dữ liệu từ nhiều nguồn UI (thiết bị cũ, auto-fill, copy/paste) dễ sinh sai kiểu.

TC-REAL-005: User Behavior - Double-click gửi trùng đơn

Scenario: Thu ngân bấm nút "Thông báo bếp" hai lần liên tiếp (double-click/delay UI) khiến gửi 2 request gần như đồng thời, có nguy cơ tạo 2 đơn trùng.

Priority: CRITICAL

Test:
- Description: Mô phỏng hai POST giống hệt nhau gửi gần như đồng thời cho cùng `tableId` và cùng `items` delta.
- Test Objective: Đảm bảo chỉ xử lý một lần (idempotency) hoặc client chặn double-submit; ít nhất không nhân đôi số lượng ở bếp.
- Expected Behavior: Một request 201, request kia bị chặn bởi UI hoặc backend trả 409/422 theo policy; UI chỉ cập nhật `notifiedQuantity` một lần.
- Impact: Chuẩn bị thừa, lãng phí nguyên liệu, tăng tải bếp, khách phàn nàn.
- Comment "REAL SCENARIO": Môi trường nhà hàng bận rộn, thu ngân thường click nhanh; mạng chậm làm người dùng tưởng chưa gửi và bấm lại.
