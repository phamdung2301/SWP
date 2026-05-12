<!-- 9b4b5e13-ae4a-4ff5-950a-0c78f71c1cad 99efaa35-124c-4d5f-8b47-9551520718a0 -->
# Kế hoạch: Gửi cảnh báo tồn kho qua Messenger sau thanh toán

## Yêu cầu:

- Sau mỗi lần thanh toán xong → Check stock của sản phẩm đã bán
- Nếu stock nằm trong khoảng cảnh báo → Gửi tin nhắn Messenger cho User
- Tránh spam: Mỗi threshold chỉ gửi 1 lần (<20: 1 lần, <10: 1 lần)

## Những gì cần chuẩn bị:

### 1. Messenger API Integration

**File cần tạo/sửa:**

- `src/main/java/com/liteflow/service/messaging/MessengerService.java` (mới)
- Thêm `sendMessageToUser()` method để gửi tin nhắn Messenger
- Tích hợp Facebook Messenger API (Page Access Token)
- Xử lý authentication và webhook nếu cần

**Cấu hình cần:**

- Facebook Page Access Token (lưu trong env hoặc config)
- Page ID hoặc PSID (Page-Scoped ID) của user
- Có thể cần lưu Messenger PSID của User trong database

### 2. Database Schema - Tracking Notifications

**File: `database/messenger_stock_alert_schema.sql` (mới)**

Cần tạo table để track notifications đã gửi:

```sql
CREATE TABLE StockAlertNotifications (
    NotificationID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    UserID UNIQUEIDENTIFIER NOT NULL,  -- User nhận notification
    AlertThreshold INT NOT NULL,  -- 10 hoặc 20
    StockLevel INT NOT NULL,  -- Stock level tại thời điểm gửi
    SentAt DATETIME2 DEFAULT SYSDATETIME(),
    MessageSent NVARCHAR(MAX),
    FOREIGN KEY (ProductVariantID) REFERENCES ProductVariant(ProductVariantID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

CREATE INDEX IX_StockAlertNotifications_UserVariant 
ON StockAlertNotifications(UserID, ProductVariantID, AlertThreshold);
```

### 3. Service Layer - Stock Alert Check

**File cần tạo:**

- `src/main/java/com/liteflow/service/inventory/StockAlertService.java` (mới)
- Method `checkAndSendAlertsAfterPayment()` - Check stock sau payment
- Method `hasNotificationBeenSent()` - Kiểm tra đã gửi notification chưa
- Method `markNotificationAsSent()` - Đánh dấu đã gửi

**Logic:**

- Sau khi thanh toán, lấy danh sách ProductVariant đã bán
- Check stock hiện tại của từng variant
- So sánh với thresholds (10, 20)
- Kiểm tra xem đã gửi notification cho threshold này chưa
- Nếu chưa gửi và stock ≤ threshold → Gửi Messenger notification

### 4. Hook vào Payment Completion

**File cần sửa:**

- `src/main/java/com/liteflow/controller/CashierAPIServlet.java`

**Vị trí:** Sau dòng 693 (`em.getTransaction().commit()`), thêm:

```java
// 6. Check stock alerts và gửi Messenger notifications
checkStockAlertsAndNotify(em, orderItemsFromRequest, sessionId);
```

**Logic:**

- Sau khi commit transaction (đảm bảo stock đã được trừ)
- Lấy danh sách ProductVariant từ orderItems
- Gọi StockAlertService để check và gửi notifications
- Chạy async để không block payment response

### 5. Messenger Message Templates

**File:** Tạo constants hoặc template service

**Templates:**

- Cảnh báo <20: "⚠️ CẢNH BÁO: Sản phẩm [Tên] (Size: [Size]) còn [X] đơn vị trong kho. Nên nhập hàng sớm."
- Cảnh báo <10: "🔴 NGUY HIỂM: Sản phẩm [Tên] (Size: [Size]) chỉ còn [X] đơn vị. Cần nhập hàng ngay!"

### 6. User Messenger Integration

**Cần bổ sung:**

- Lưu Messenger PSID (Page-Scoped ID) của User trong database
- Có thể thêm field `MessengerPSID` vào Users table hoặc tạo bảng riêng
- Hoặc sử dụng Phone number để map với Messenger (nếu đã verify)

**File schema cần sửa:**

- `database/liteflow_schema.sql` - Thêm `MessengerPSID` vào Users table (optional)
- Hoặc tạo `UserMessengerMapping` table

### 7. Configuration & Environment Variables

**Cần:**

- `MESSENGER_PAGE_ACCESS_TOKEN` - Facebook Page Access Token
- `MESSENGER_API_URL` - "https://graph.facebook.com/v18.0/me/messages"
- Có thể thêm config trong `NotificationChannel` để support Messenger channel

## Files cần tạo mới:

1. `src/main/java/com/liteflow/service/messaging/MessengerService.java`
2. `src/main/java/com/liteflow/service/inventory/StockAlertService.java`
3. `src/main/java/com/liteflow/model/messaging/StockAlertNotification.java`
4. `src/main/java/com/liteflow/dao/messaging/StockAlertNotificationDAO.java`
5. `database/messenger_stock_alert_schema.sql`

## Files cần sửa:

1. `src/main/java/com/liteflow/controller/CashierAPIServlet.java` - Thêm hook sau payment
2. `database/liteflow_schema.sql` - Thêm MessengerPSID (optional) hoặc UserMessengerMapping table
3. `src/main/java/com/liteflow/service/alert/NotificationService.java` - Thêm Messenger channel support

## Dependencies cần:

- Facebook Messenger API SDK hoặc HTTP client để gọi Messenger API
- OkHttp (đã có) hoặc HttpURLConnection để gửi requests

## Testing cần:

- Test với sản phẩm có stock = 6 → Nên gửi notification <10
- Test với sản phẩm có stock = 15 → Nên gửi notification <20
- Test spam prevention → Gửi lần 2 với cùng threshold → Không gửi
- Test sau khi nhập hàng → Stock > 20 → Reset notification state

### To-dos

- [ ] Tạo MessengerService với sendMessageToUser() method, tích hợp Facebook Messenger API
- [ ] Tạo StockAlertNotifications table để track notifications đã gửi (tránh spam)
- [ ] Tạo StockAlertNotification model và DAO để query/save notifications
- [ ] Tạo StockAlertService với logic check stock và gửi Messenger notifications, xử lý spam prevention
- [ ] Thêm Messenger PSID mapping vào Users table hoặc tạo UserMessengerMapping table
- [ ] Thêm hook vào CashierAPIServlet sau payment completion để gọi StockAlertService
- [ ] Tạo message templates cho từng loại cảnh báo (<20, <10) với format rõ ràng
- [ ] Thêm configuration cho Messenger API (Page Access Token, API URL) vào env hoặc config