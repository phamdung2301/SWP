📊 TEST CASE MATRIX - BASIC TESTS (15 cases)

Happy Path (4 cases):

TC-HP-001: Tạo order thành công với 1 món
- Description: Gửi 1 món mới từ quầy tới bếp, nhận `orderId` và cập nhật trạng thái món đã gửi.
- Input Data:
  - Backend Request: POST `/api/order/create`
    - Body: `{ tableId: "0a4e5d60-9a55-4a55-a7d5-2f1f7f5b1a11", items: [{ variantId: "v-101", quantity: 2, unitPrice: 45000, note: "Ít đá" }] }`
  - Frontend State: `selectedTable.id` trùng `tableId`; `orderItems` có 1 item với `quantity=2`, `notifiedQuantity=0`.
- Expected Output:
  - HTTP 201; JSON `{ success: true, message: "Đã gửi thông báo đến bếp thành công!", orderId: <UUID> }`.
  - UI alert hiển thị thành công; cập nhật `notifiedQuantity=2` cho item; badges đổi sang “Đã gửi bếp”.
- Mock Behavior: `OrderService.createOrderAndNotifyKitchen` trả UUID cố định (e.g., `11111111-1111-1111-1111-111111111111`); `OrderDAO.createOrder` được gọi 1 lần với đúng tham số.

TC-HP-002: Tạo order thành công với nhiều món
- Description: Gửi 3 món mới, tất cả hợp lệ.
- Input Data:
  - Body: `{ tableId: "0a4e5d60-9a55-4a55-a7d5-2f1f7f5b1a11", items: [{ variantId: "v-201", quantity: 1, unitPrice: 30000 }, { variantId: "v-202", quantity: 3, unitPrice: 55000 }, { variantId: "v-203", quantity: 2, unitPrice: 40000, note: "ít cay" }] }`
- Expected Output: HTTP 201; JSON success với `orderId`; UI cập nhật `notifiedQuantity` từng món bằng `quantity` hiện tại; render lại danh sách món và tổng tiền.
- Mock Behavior: Service trả UUID; DAO được verify đã nhận danh sách 3 items.

TC-HP-003: Chỉ gửi phần delta (món đã gửi trước đó + món mới)
- Description: Một món A đã gửi (notifiedQuantity=2, quantity=2), một món B mới thêm (quantity=1). Chỉ B được gửi.
- Input Data:
  - Frontend: `orderItems = [ { variantId: "v-301", quantity: 2, notifiedQuantity: 2 }, { variantId: "v-302", quantity: 1, notifiedQuantity: 0 } ]`
  - Body (kỳ vọng khi gọi): `{ tableId: <UUID>, items: [{ variantId: "v-302", quantity: 1, unitPrice: <price> }] }`
- Expected Output: HTTP 201; JSON success; UI cập nhật `notifiedQuantity` cho B thành 1; A giữ nguyên.
- Mock Behavior: Service nhận đúng 1 item (v-302); DAO được gọi 1 lần.

TC-HP-004: Bàn đang có khách (load orders) rồi gửi thêm món mới
- Description: Sau khi `loadTableOrders()` load các món hiện có (đã notified), thêm món mới C rồi notify.
- Input Data:
  - Frontend: `loadTableOrders` trả `{ success: true, orders: [ { variantId:"v-401", quantity:2, price:50000 }, { variantId:"v-402", quantity:1, price:45000 } ] }` → map thành `notifiedQuantity=quantity`.
  - Thêm mới `v-403` (`quantity=1`).
- Expected Output: Chỉ `v-403` được gửi; HTTP 201; UI cập nhật badge chính xác cho các món.
- Mock Behavior: Mock fetch GET trả danh sách orders; POST service chỉ nhận item `v-403`.

Edge Cases (4 cases):

TC-EDGE-001: Ghi chú rất dài và có emoji/unicode
- Description: `note` 500+ ký tự, chứa emoji và ký tự có dấu.
- Input Data: `items: [{ variantId:"v-501", quantity:1, unitPrice: 60000, note:"🔥🥤 rất lạnh, không đường... (x500)" }]`.
- Expected Output: HTTP 201; JSON success; UI hiển thị bình thường (UTF-8), không lỗi encoding.
- Mock Behavior: Service/DAO xử lý chuỗi dài, không cắt xén; verify gọi thành công.

TC-EDGE-002: Không có món delta để gửi (tất cả đã notified)
- Description: Tất cả item `quantity == notifiedQuantity` → không gọi API.
- Input Data: `orderItems = [{ variantId:"v-601", quantity:2, notifiedQuantity:2 }]`.
- Expected Output: UI alert: "Tất cả món đã được thông báo bếp!"; không gọi fetch POST.
- Mock Behavior: Verify không gọi `OrderService`/không gọi `fetch`.

TC-EDGE-003: Đơn lớn (50 món khác nhau)
- Description: Gửi 50 items hợp lệ để kiểm tra giới hạn payload/thời gian.
- Input Data: `items` gồm 50 phần tử, mỗi phần tử `quantity` trong [1..3].
- Expected Output: HTTP 201; JSON success; UI cập nhật nhanh, không treo; tất cả `notifiedQuantity` cập nhật đúng.
- Mock Behavior: Service/DAO được gọi với danh sách 50 items; đảm bảo performance test nhẹ (không timeouts trong unit env, chỉ logic assertion).

TC-EDGE-004: Thiếu trường optional `note`
- Description: Item không có `note`.
- Input Data: `items: [{ variantId:"v-701", quantity:2, unitPrice: 35000 }]`.
- Expected Output: HTTP 201; JSON success; UI render không lỗi khi `note` null/undefined.
- Mock Behavior: Service/DAO không phụ thuộc `note`.

Error Scenarios (7 cases):

TC-ERR-001: Thiếu `tableId`
- Description: `tableId` null/empty.
- Input Data: Body `{ items: [{ variantId:"v-801", quantity:1, unitPrice: 20000 }] }` hoặc `{ tableId: "" , items: [...] }`.
- Expected Output: HTTP 400; JSON `{ success:false, message:"Table ID không được rỗng" }`.
- Mock Behavior: Service không được gọi; verify 0 calls.

TC-ERR-002: `tableId` không đúng định dạng UUID
- Description: `tableId="table1"` (chuỗi tuỳ ý).
- Input Data: `{ tableId:"table1", items:[{ variantId:"v-802", quantity:1, unitPrice: 20000 }] }`.
- Expected Output: HTTP 400; JSON `{ success:false, message:"Table ID không hợp lệ: table1" }`.
- Mock Behavior: Service không được gọi.

TC-ERR-003: Thiếu `items` hoặc `items=[]`
- Description: Không có danh sách món.
- Input Data: `{ tableId: <UUID> }` hoặc `{ tableId: <UUID>, items: [] }`.
- Expected Output: HTTP 400; JSON `{ success:false, message:"Danh sách món không được rỗng" }`.
- Mock Behavior: Service không được gọi.

TC-ERR-004: JSON sai cú pháp (malformed)
- Description: Body không phải JSON hợp lệ (ví dụ: chuỗi bị cắt dở, dấu ngoặc thiếu).
- Input Data: `"{ tableId: \"...\", items: ["` (malformed)
- Expected Output: HTTP 500; JSON `{ success:false, message:"Lỗi server: <JsonSyntaxException...>" }` (theo hành vi hiện tại của servlet khi Gson ném exception).
- Mock Behavior: Service không được gọi.

TC-ERR-005: Service ném `IllegalArgumentException`
- Description: `OrderService.createOrderAndNotifyKitchen` tự ném lỗi validate nội bộ (vd: `items=null`).
- Input Data: `{ tableId:<UUID>, items:null }`.
- Expected Output: HTTP 400; JSON `{ success:false, message:"Danh sách món không được rỗng" }`.
- Mock Behavior: Mock Service ném `IllegalArgumentException("Danh sách món không được rỗng")`.

TC-ERR-006: Service ném `RuntimeException` không mong đợi
- Description: Lỗi hệ thống từ DAO.
- Input Data: Body hợp lệ.
- Expected Output: HTTP 500; JSON `{ success:false, message:"Lỗi server: ..." }`.
- Mock Behavior: Mock Service ném `RuntimeException("DB down")` hoặc mock DAO ném lỗi; verify servlet map 500.

TC-ERR-007: Frontend notify khi chưa chọn bàn
- Description: Người dùng bấm "Thông báo bếp" khi chưa có `selectedTable`.
- Input Data: `orderItems` có ít nhất 1 item; `selectedTable = null`.
- Expected Output: UI alert: "Vui lòng chọn bàn!"; không gọi fetch POST; không thay đổi `notifiedQuantity`.
- Mock Behavior: Mock `alert` được gọi; mock `fetch` không bị gọi.
