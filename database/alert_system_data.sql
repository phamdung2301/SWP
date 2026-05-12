-- ============================================================
-- SMART ALERT SYSTEM - SAMPLE DATA
-- Sample configurations and test data
-- ============================================================

USE LiteFlowDBO;
GO

-- ============================================================
-- 1️⃣ NOTIFICATION CHANNELS - Slack & Telegram Setup
-- ============================================================

-- Slack Channel (Owner notifications)
INSERT INTO NotificationChannels (ChannelType, Name, Description, SlackWebhookURL, SlackChannel, IsActive)
VALUES (
    'SLACK',
    N'LiteFlow Owner Alerts',
    N'Kênh Slack chính cho Owner - nhận tất cả thông báo quan trọng',
    'https://hooks.slack.com/services/YOUR/WEBHOOK/URL', -- Replace with real webhook
    '#liteflow-alerts',
    1
);

-- Telegram Bot (Multi-purpose)
INSERT INTO NotificationChannels (ChannelType, Name, Description, TelegramBotToken, TelegramChatID, IsActive)
VALUES (
    'TELEGRAM',
    N'LiteFlow Telegram Bot',
    N'Bot Telegram cho thông báo nhanh và tương tác',
    'YOUR_BOT_TOKEN_HERE', -- Replace with real token
    'YOUR_CHAT_ID_HERE',   -- Replace with real chat ID
    1
);

-- Email Channel (Backup)
INSERT INTO NotificationChannels (ChannelType, Name, Description, EmailRecipients, EmailFrom, IsActive)
VALUES (
    'EMAIL',
    N'LiteFlow Email Notifications',
    N'Email backup cho các cảnh báo quan trọng',
    '["owner@liteflow.vn", "admin@liteflow.vn"]',
    'alerts@liteflow.vn',
    0 -- Disabled by default
);
GO

-- ============================================================
-- 2️⃣ ALERT CONFIGURATIONS - Standard Alerts
-- ============================================================

DECLARE @OwnerID UNIQUEIDENTIFIER = (SELECT TOP 1 UserID FROM Users WHERE Email = 'owner@liteflow.vn');
DECLARE @ProcurementID UNIQUEIDENTIFIER = (SELECT TOP 1 UserID FROM Users WHERE Email = 'procurement@liteflow.vn');
DECLARE @InventoryID UNIQUEIDENTIFIER = (SELECT TOP 1 UserID FROM Users WHERE Email = 'inventory@liteflow.vn');

-- 1. Daily Revenue Summary (6PM every day)
INSERT INTO AlertConfigurations (
    AlertType, Name, Description, IsEnabled,
    TriggerConditions, 
    NotifySlack, NotifyTelegram, NotifyInApp,
    Recipients,
    UseGPTSummary,
    GPTPromptTemplate,
    ScheduleCron,
    NextScheduledRun,
    Priority,
    CreatedBy
) VALUES (
    'DAILY_SUMMARY',
    N'Báo cáo doanh thu cuối ngày',
    N'Tóm tắt doanh thu, đơn hàng, sản phẩm bán chạy hằng ngày lúc 6PM',
    1,
    '{"sendTime": "18:00", "includeTopProducts": true, "includeComparison": true}',
    1, 1, 1,
    '["' + CAST(@OwnerID AS NVARCHAR(36)) + '"]',
    1, -- Use GPT
    N'Bạn là trợ lý phân tích kinh doanh. Hãy tóm tắt doanh thu hôm nay theo cách ngắn gọn, dễ hiểu. Bao gồm: tổng doanh thu, số đơn hàng, so sánh với hôm qua, top 3 sản phẩm bán chạy. Dùng emoji phù hợp.',
    '0 18 * * *', -- 6PM daily
    DATEADD(HOUR, 18 - DATEPART(HOUR, SYSDATETIME()), CAST(CAST(SYSDATETIME() AS DATE) AS DATETIME2)),
    'HIGH',
    @OwnerID
);

-- 2. Purchase Order Pending Alert
INSERT INTO AlertConfigurations (
    AlertType, Name, Description, IsEnabled,
    TriggerConditions,
    NotifySlack, NotifyTelegram, NotifyInApp,
    Recipients,
    UseGPTSummary,
    ScheduleCron,
    NextScheduledRun,
    Priority,
    CreatedBy
) VALUES (
    'PO_PENDING',
    N'Đơn đặt hàng chờ duyệt',
    N'Cảnh báo khi có đơn đặt hàng chờ duyệt quá 2 ngày',
    1,
    '{"thresholdDays": 2, "checkInterval": "4h"}',
    1, 1, 1,
    '["' + CAST(@ProcurementID AS NVARCHAR(36)) + '", "' + CAST(@OwnerID AS NVARCHAR(36)) + '"]',
    0, -- No GPT for simple alerts
    '0 */4 * * *', -- Every 4 hours
    DATEADD(HOUR, 4, SYSDATETIME()),
    'HIGH',
    @ProcurementID
);

-- 3. Low Inventory Alert
INSERT INTO AlertConfigurations (
    AlertType, Name, Description, IsEnabled,
    TriggerConditions,
    NotifySlack, NotifyTelegram, NotifyInApp,
    Recipients,
    UseGPTSummary,
    GPTPromptTemplate,
    ScheduleCron,
    NextScheduledRun,
    Priority,
    CreatedBy
) VALUES (
    'LOW_INVENTORY',
    N'Tồn kho thấp',
    N'Cảnh báo khi số lượng tồn kho dưới 10 đơn vị',
    1,
    '{"threshold": 10, "checkInterval": "1h"}',
    1, 1, 1,
    '["' + CAST(@InventoryID AS NVARCHAR(36)) + '", "' + CAST(@OwnerID AS NVARCHAR(36)) + '"]',
    1, -- Use GPT for smart suggestions
    N'Phân tích sản phẩm sắp hết hàng và đề xuất số lượng nên đặt dựa trên tốc độ tiêu thụ trung bình. Trả lời ngắn gọn, có số liệu cụ thể.',
    '0 */1 * * *', -- Every hour
    DATEADD(HOUR, 1, SYSDATETIME()),
    'MEDIUM',
    @InventoryID
);

-- 4. Out of Stock Alert (Critical)
INSERT INTO AlertConfigurations (
    AlertType, Name, Description, IsEnabled,
    TriggerConditions,
    NotifySlack, NotifyTelegram, NotifyInApp,
    Recipients,
    UseGPTSummary,
    Priority,
    CreatedBy
) VALUES (
    'OUT_OF_STOCK',
    N'Hết hàng',
    N'Cảnh báo ngay lập tức khi sản phẩm hết hàng hoàn toàn',
    1,
    '{"threshold": 0, "immediate": true}',
    1, 1, 1,
    '["' + CAST(@InventoryID AS NVARCHAR(36)) + '", "' + CAST(@OwnerID AS NVARCHAR(36)) + '"]',
    0,
    'CRITICAL',
    @InventoryID
);

-- 5. Revenue Anomaly Detection
INSERT INTO AlertConfigurations (
    AlertType, Name, Description, IsEnabled,
    TriggerConditions,
    NotifySlack, NotifyTelegram, NotifyInApp,
    Recipients,
    UseGPTSummary,
    GPTPromptTemplate,
    ScheduleCron,
    NextScheduledRun,
    Priority,
    CreatedBy
) VALUES (
    'REVENUE_ANOMALY',
    N'Doanh thu bất thường',
    N'Phát hiện doanh thu tăng/giảm đột ngột so với trung bình 7 ngày',
    1,
    '{"threshold": 30, "comparisonDays": 7, "checkInterval": "1h"}',
    1, 1, 1,
    '["' + CAST(@OwnerID AS NVARCHAR(36)) + '"]',
    1,
    N'Phân tích nguyên nhân có thể gây ra sự thay đổi doanh thu bất thường. Xem xét: giờ cao điểm, ngày trong tuần, sản phẩm, số lượng đơn. Đưa ra nhận định ngắn gọn.',
    '0 */1 * * *', -- Every hour
    DATEADD(HOUR, 1, SYSDATETIME()),
    'HIGH',
    @OwnerID
);

-- 6. PO Overdue Delivery
INSERT INTO AlertConfigurations (
    AlertType, Name, Description, IsEnabled,
    TriggerConditions,
    NotifySlack, NotifyInApp,
    Recipients,
    UseGPTSummary,
    ScheduleCron,
    NextScheduledRun,
    Priority,
    CreatedBy
) VALUES (
    'PO_OVERDUE',
    N'Đơn hàng quá hạn giao',
    N'Cảnh báo khi đơn hàng đã duyệt nhưng quá ngày giao dự kiến',
    1,
    '{"checkInterval": "6h"}',
    1, 1,
    '["' + CAST(@ProcurementID AS NVARCHAR(36)) + '", "' + CAST(@OwnerID AS NVARCHAR(36)) + '"]',
    0,
    '0 */6 * * *', -- Every 6 hours
    DATEADD(HOUR, 6, SYSDATETIME()),
    'MEDIUM',
    @ProcurementID
);

-- 7. High-Value PO Approval Required
INSERT INTO AlertConfigurations (
    AlertType, Name, Description, IsEnabled,
    TriggerConditions,
    NotifySlack, NotifyTelegram, NotifyInApp,
    Recipients,
    UseGPTSummary,
    Priority,
    CreatedBy
) VALUES (
    'PO_HIGH_VALUE',
    N'Đơn hàng giá trị cao cần duyệt',
    N'Thông báo Owner khi có đơn hàng trên 5 triệu VND',
    1,
    '{"threshold": 5000000, "immediate": true}',
    1, 1, 1,
    '["' + CAST(@OwnerID AS NVARCHAR(36)) + '"]',
    0,
    'HIGH',
    @ProcurementID
);
GO

-- ============================================================
-- 3️⃣ USER ALERT PREFERENCES - Default Settings
-- ============================================================

-- Owner preferences
INSERT INTO UserAlertPreferences (UserID, EnableNotifications, EnableSlack, EnableTelegram, EnableInApp, AlertTypeSettings)
SELECT 
    UserID,
    1, 1, 1, 1,
    '{"DAILY_SUMMARY": true, "PO_PENDING": true, "LOW_INVENTORY": true, "REVENUE_ANOMALY": true, "PO_HIGH_VALUE": true}'
FROM Users WHERE Email = 'owner@liteflow.vn';

-- Procurement preferences
INSERT INTO UserAlertPreferences (UserID, EnableNotifications, EnableSlack, EnableTelegram, EnableInApp, AlertTypeSettings)
SELECT 
    UserID,
    1, 1, 1, 1,
    '{"PO_PENDING": true, "PO_OVERDUE": true, "PO_HIGH_VALUE": true}'
FROM Users WHERE Email = 'procurement@liteflow.vn';

-- Inventory preferences
INSERT INTO UserAlertPreferences (UserID, EnableNotifications, EnableSlack, EnableInApp, AlertTypeSettings)
SELECT 
    UserID,
    1, 1, 1,
    '{"LOW_INVENTORY": true, "OUT_OF_STOCK": true}'
FROM Users WHERE Email = 'inventory@liteflow.vn';

-- Cashier preferences (minimal alerts)
INSERT INTO UserAlertPreferences (UserID, EnableNotifications, EnableInApp, AlertTypeSettings)
SELECT 
    UserID,
    1, 1,
    '{"SHIFT_REMINDER": true}'
FROM Users WHERE Email = 'cashier1@liteflow.vn';
GO

-- ============================================================
-- 4️⃣ SAMPLE ALERT HISTORY (For UI Testing)
-- ============================================================

DECLARE @TestAlertID UNIQUEIDENTIFIER = (SELECT TOP 1 AlertID FROM AlertConfigurations WHERE AlertType = 'LOW_INVENTORY');
DECLARE @TestOwnerID UNIQUEIDENTIFIER = (SELECT TOP 1 UserID FROM Users WHERE Email = 'owner@liteflow.vn');

-- Sample: Low Stock Alert
INSERT INTO AlertHistory (
    AlertID, AlertType, Title, Message, 
    ContextData,
    SentToSlack, SentInApp, DeliveryStatus,
    Priority, TriggeredAt
) VALUES (
    @TestAlertID,
    'LOW_INVENTORY',
    N'⚠️ Tồn kho thấp: Cà phê đen',
    N'Sản phẩm "Cà phê đen" chỉ còn 5 đơn vị trong kho. Đề nghị nhập hàng sớm.',
    '{"productName": "Cà phê đen", "currentStock": 5, "threshold": 10, "avgConsumption": "15 units/day"}',
    1, 1, 'SENT',
    'MEDIUM',
    DATEADD(HOUR, -2, SYSDATETIME())
);

-- Sample: PO Pending Alert


-- Sample: Daily Summary (with GPT)
DECLARE @GPTInteractionID UNIQUEIDENTIFIER = NEWID();

INSERT INTO GPTInteractions (
    InteractionID, Model, Purpose,
    SystemPrompt, UserPrompt, AssistantResponse,
    PromptTokens, CompletionTokens, TotalTokens,
    EstimatedCostUSD, EstimatedCostVND,
    ResponseTimeMs, Status
) VALUES (
    @GPTInteractionID,
    'gpt-4o-mini',
    'DAILY_SUMMARY',
    N'Bạn là trợ lý phân tích kinh doanh cho quán cà phê.',
    N'Tóm tắt doanh thu hôm nay: Tổng 15.2M VND, 142 đơn, trung bình 107K/đơn. Top 3: Cà phê sữa đá (1.35M), Latte (1.44M), Trà đào (1.12M). So với hôm qua tăng 12%.',
    N'📊 **Báo cáo doanh thu 29/10/2025**

💰 **Tổng doanh thu:** 15.2M VND (+12% so hôm qua) 📈
🛒 **Tổng đơn hàng:** 142 đơn
💵 **Giá trị trung bình:** 107K/đơn

🔥 **Top sản phẩm bán chạy:**
1. ☕ Latte - 1.44M VND
2. ☕ Cà phê sữa đá - 1.35M VND  
3. 🍑 Trà đào - 1.12M VND

✅ Ngày hôm nay kinh doanh khả quan với mức tăng trưởng tốt!',
    450, 180, 630,
    0.000162, 4.02,
    850, 'SUCCESS'
);

INSERT INTO AlertHistory (
    AlertType, Title, Message,
    GPTSummary, GPTInteractionID,
    ContextData,
    SentToSlack, SentToTelegram, SentInApp, DeliveryStatus,
    Priority, TriggeredAt, SentAt
) VALUES (
    'DAILY_SUMMARY',
    N'📊 Báo cáo doanh thu cuối ngày',
    N'Doanh thu hôm nay: 15.2M VND (+12%)',
    N'📊 **Báo cáo doanh thu 29/10/2025**

💰 **Tổng doanh thu:** 15.2M VND (+12% so hôm qua) 📈
🛒 **Tổng đơn hàng:** 142 đơn
💵 **Giá trị trung bình:** 107K/đơn

🔥 **Top sản phẩm bán chạy:**
1. ☕ Latte - 1.44M VND
2. ☕ Cà phê sữa đá - 1.35M VND  
3. 🍑 Trà đào - 1.12M VND

✅ Ngày hôm nay kinh doanh khả quan với mức tăng trưởng tốt!',
    @GPTInteractionID,
    '{"totalRevenue": 15200000, "totalOrders": 142, "avgOrderValue": 107000, "compareYesterday": 12}',
    1, 1, 1, 'SENT',
    'HIGH',
    DATEADD(HOUR, -18, SYSDATETIME()),
    DATEADD(HOUR, -18, DATEADD(SECOND, 2, SYSDATETIME()))
);
GO

-- ============================================================
-- 5️⃣ VERIFICATION & SUMMARY
-- ============================================================

DECLARE @ChannelCount INT = (SELECT COUNT(*) FROM NotificationChannels);
DECLARE @ConfigCount INT = (SELECT COUNT(*) FROM AlertConfigurations);
DECLARE @HistoryCount INT = (SELECT COUNT(*) FROM AlertHistory);
DECLARE @GPTCount INT = (SELECT COUNT(*) FROM GPTInteractions);
DECLARE @PrefCount INT = (SELECT COUNT(*) FROM UserAlertPreferences);

PRINT '========================================';
PRINT '✅ ALERT SYSTEM SAMPLE DATA LOADED!';
PRINT '========================================';
PRINT 'Notification Channels: ' + CAST(@ChannelCount AS NVARCHAR(10));
PRINT 'Alert Configurations: ' + CAST(@ConfigCount AS NVARCHAR(10));
PRINT 'Alert History: ' + CAST(@HistoryCount AS NVARCHAR(10));
PRINT 'GPT Interactions: ' + CAST(@GPTCount AS NVARCHAR(10));
PRINT 'User Preferences: ' + CAST(@PrefCount AS NVARCHAR(10));
PRINT '';
PRINT '📋 Alert Types Configured:';
PRINT '  1. DAILY_SUMMARY - 6PM daily';
PRINT '  2. PO_PENDING - Every 4h';
PRINT '  3. LOW_INVENTORY - Hourly';
PRINT '  4. OUT_OF_STOCK - Immediate';
PRINT '  5. REVENUE_ANOMALY - Hourly';
PRINT '  6. PO_OVERDUE - Every 6h';
PRINT '  7. PO_HIGH_VALUE - Immediate';
PRINT '';
PRINT '🔗 Next Steps:';
PRINT '  1. Update Slack webhook URL';
PRINT '  2. Update Telegram bot token & chat ID';
PRINT '  3. Test notification delivery';
PRINT '  4. Configure GPT API key';
PRINT '========================================';

-- Display active alert configs
SELECT 
    AlertType,
    Name,
    Priority,
    CASE WHEN ScheduleCron IS NOT NULL THEN 'Scheduled' ELSE 'Event-driven' END as TriggerType,
    CASE WHEN UseGPTSummary = 1 THEN 'Yes' ELSE 'No' END as UsesGPT
FROM AlertConfigurations
WHERE IsEnabled = 1
ORDER BY Priority DESC, AlertType;

PRINT '';
PRINT '📊 GPT Cost Summary:';
SELECT * FROM vw_GPTCostSummary;
GO

-- ============================================================
-- 6️⃣ SAMPLE NOTICES FOR EMPLOYEE DASHBOARD
-- Thông báo mẫu hiển thị trên dashboard nhân viên
-- ============================================================

DECLARE @AdminID UNIQUEIDENTIFIER = (SELECT TOP 1 UserID FROM Users WHERE Email = 'owner@liteflow.vn');
DECLARE @Notice1ID UNIQUEIDENTIFIER = NEWID();
DECLARE @Notice2ID UNIQUEIDENTIFIER = NEWID();
DECLARE @Notice3ID UNIQUEIDENTIFIER = NEWID();

-- Notice 1: Important - Holiday Announcement
INSERT INTO Notices (
    NoticeID,
    Title,
    Content,
    NoticeType,
    IsPinned,
    PublishedAt,
    ExpiresAt,
    IsActive,
    CreatedBy,
    ViewCount
) VALUES (
    @Notice1ID,
    N'Thông báo nghỉ lễ Quốc Khánh',
    N'Công ty thông báo lịch nghỉ lễ Quốc Khánh 2/9 từ ngày 31/8 đến 3/9. Toàn thể nhân viên nghỉ theo quy định.',
    'important',
    1, -- Pinned to top
    CAST('2025-10-30' AS DATETIME2),
    CAST('2025-09-03' AS DATETIME2), -- Expires after holiday
    1,
    @AdminID,
    45 -- Sample view count
);

-- Notice 2: General - Attendance Process Update
INSERT INTO Notices (
    NoticeID,
    Title,
    Content,
    NoticeType,
    IsPinned,
    PublishedAt,
    ExpiresAt,
    IsActive,
    CreatedBy,
    ViewCount
) VALUES (
    @Notice2ID,
    N'Cập nhật quy trình chấm công mới',
    N'Từ ngày 1/11, quy trình chấm công sẽ được cập nhật. Vui lòng chấm công đúng giờ và báo cáo khi quên chấm công.',
    'general',
    0,
    CAST('2025-10-29' AS DATETIME2),
    CAST('2025-11-30' AS DATETIME2), -- Expires end of November
    1,
    @AdminID,
    32
);

-- Notice 3: Info - Department Meeting Schedule
INSERT INTO Notices (
    NoticeID,
    Title,
    Content,
    NoticeType,
    IsPinned,
    PublishedAt,
    ExpiresAt,
    IsActive,
    CreatedBy,
    ViewCount
) VALUES (
    @Notice3ID,
    N'Lịch họp phòng ban tháng 11',
    N'Lịch họp phòng ban đã được cập nhật. Vui lòng kiểm tra lịch cá nhân của bạn.',
    'info',
    0,
    CAST('2025-10-28' AS DATETIME2),
    CAST('2025-11-30' AS DATETIME2),
    1,
    @AdminID,
    28
);

-- Sample Notice Reads (some employees already read some notices)
DECLARE @Employee1ID UNIQUEIDENTIFIER = (SELECT TOP 1 EmployeeID FROM Employees);
DECLARE @Employee2ID UNIQUEIDENTIFIER = (SELECT EmployeeID FROM Employees WHERE EmployeeID != @Employee1ID);

-- Employee 1 read Notice 1 and 2
IF @Employee1ID IS NOT NULL
BEGIN
    INSERT INTO NoticeReads (NoticeID, UserID, ReadAt)
    VALUES 
        (@Notice1ID, (SELECT UserID FROM Employees WHERE EmployeeID = @Employee1ID), DATEADD(HOUR, -2, SYSDATETIME())),
        (@Notice2ID, (SELECT UserID FROM Employees WHERE EmployeeID = @Employee1ID), DATEADD(HOUR, -5, SYSDATETIME()));
END

-- Employee 2 read only Notice 1
IF @Employee2ID IS NOT NULL
BEGIN
    INSERT INTO NoticeReads (NoticeID, UserID, ReadAt)
    VALUES 
        (@Notice1ID, (SELECT UserID FROM Employees WHERE EmployeeID = @Employee2ID), DATEADD(HOUR, -1, SYSDATETIME()));
END

PRINT '';
PRINT '========================================';
PRINT '📢 NOTICES DATA LOADED!';
PRINT '========================================';
PRINT 'Total Notices: 3';
PRINT '  1. Holiday Announcement (Important, Pinned)';
PRINT '  2. Attendance Update (General)';
PRINT '  3. Meeting Schedule (Info)';
PRINT '';
PRINT '✅ Sample notice reads created for testing';
PRINT '========================================';
GO


