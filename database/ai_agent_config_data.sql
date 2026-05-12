-- ============================================================
-- AI AGENT CONFIGURATIONS - DEFAULT DATA
-- Insert default configuration values
-- ============================================================

USE LiteFlowDBO;
GO

-- Get default admin user ID (assuming first admin user)
DECLARE @AdminUserID UNIQUEIDENTIFIER;
SELECT TOP 1 @AdminUserID = u.UserID 
FROM Users u
INNER JOIN UserRoles ur ON u.UserID = ur.UserID
INNER JOIN Roles r ON ur.RoleID = r.RoleID
WHERE r.Name = 'ADMIN' AND ur.IsActive = 1
ORDER BY u.CreatedAt;

IF @AdminUserID IS NULL
BEGIN
    -- Fallback: Get first user with any admin-related role (Owner, Admin, Manager)
    SELECT TOP 1 @AdminUserID = u.UserID 
    FROM Users u
    INNER JOIN UserRoles ur ON u.UserID = ur.UserID
    INNER JOIN Roles r ON ur.RoleID = r.RoleID
    WHERE r.Name IN ('ADMIN', 'Owner', 'MANAGER') AND ur.IsActive = 1
    ORDER BY u.CreatedAt;
END

IF @AdminUserID IS NULL
BEGIN
    -- Final fallback: Get first user
    SELECT TOP 1 @AdminUserID = UserID FROM Users ORDER BY CreatedAt;
END

-- ============================================================
-- STOCK ALERT SETTINGS
-- ============================================================

INSERT INTO AIAgentConfigurations (ConfigKey, ConfigValue, ConfigType, Category, DisplayName, Description, MinValue, MaxValue, DefaultValue, IsActive, UpdatedBy)
VALUES
    ('stock.warning_threshold', '20', 'INTEGER', 'STOCK_ALERT', N'Ngưỡng cảnh báo tồn kho', N'Số lượng tồn kho tối thiểu để cảnh báo (WARNING)', '1', '1000', '20', 1, @AdminUserID),
    ('stock.critical_threshold', '10', 'INTEGER', 'STOCK_ALERT', N'Ngưỡng nguy hiểm tồn kho', N'Số lượng tồn kho tối thiểu để cảnh báo nguy hiểm (CRITICAL)', '1', '1000', '10', 1, @AdminUserID),
    ('stock.alert_check_interval', '3600', 'INTEGER', 'STOCK_ALERT', N'Khoảng thời gian kiểm tra (giây)', N'Khoảng thời gian giữa các lần kiểm tra tồn kho (tính bằng giây)', '60', '86400', '3600', 1, @AdminUserID),
    ('stock.enable_alerts', 'true', 'BOOLEAN', 'STOCK_ALERT', N'Bật cảnh báo tồn kho', N'Bật/tắt tính năng cảnh báo tồn kho tự động', NULL, NULL, 'true', 1, @AdminUserID);

-- ============================================================
-- DEMAND FORECAST SETTINGS
-- ============================================================

INSERT INTO AIAgentConfigurations (ConfigKey, ConfigValue, ConfigType, Category, DisplayName, Description, MinValue, MaxValue, DefaultValue, IsActive, UpdatedBy)
VALUES
    ('forecast.low_stock_threshold', '20', 'INTEGER', 'DEMAND_FORECAST', N'Ngưỡng tồn kho thấp', N'Ngưỡng tồn kho thấp cho dự báo nhu cầu', '1', '1000', '20', 1, @AdminUserID),
    ('forecast.critical_stock_threshold', '5', 'INTEGER', 'DEMAND_FORECAST', N'Ngưỡng tồn kho nguy hiểm', N'Ngưỡng tồn kho nguy hiểm cho dự báo nhu cầu', '1', '1000', '5', 1, @AdminUserID),
    ('forecast.days_ahead', '7', 'INTEGER', 'DEMAND_FORECAST', N'Số ngày dự báo trước', N'Số ngày dự báo nhu cầu trước', '1', '90', '7', 1, @AdminUserID),
    ('forecast.enable_forecast', 'true', 'BOOLEAN', 'DEMAND_FORECAST', N'Bật dự báo nhu cầu', N'Bật/tắt tính năng dự báo nhu cầu tự động', NULL, NULL, 'true', 1, @AdminUserID);

-- ============================================================
-- PO AUTO CREATION SETTINGS
-- ============================================================

INSERT INTO AIAgentConfigurations (ConfigKey, ConfigValue, ConfigType, Category, DisplayName, Description, MinValue, MaxValue, DefaultValue, IsActive, UpdatedBy)
VALUES
    ('po.recent_check_days', '1', 'INTEGER', 'PO_AUTO', N'Số ngày kiểm tra PO gần đây', N'Số ngày để kiểm tra xem đã có PO gần đây cho sản phẩm chưa', '0', '30', '1', 1, @AdminUserID),
    ('po.default_lead_time_days', '7', 'INTEGER', 'PO_AUTO', N'Thời gian giao hàng mặc định (ngày)', N'Số ngày mặc định từ khi đặt hàng đến khi nhận hàng', '1', '90', '7', 1, @AdminUserID),
    ('po.default_reorder_quantity', '20', 'INTEGER', 'PO_AUTO', N'Số lượng đặt hàng mặc định', N'Số lượng mặc định để đưa tồn kho về mức an toàn', '1', '10000', '20', 1, @AdminUserID),
    ('po.auto_create_enabled', 'true', 'BOOLEAN', 'PO_AUTO', N'Bật tự động tạo PO', N'Bật/tắt tính năng tự động tạo đơn đặt hàng', NULL, NULL, 'true', 1, @AdminUserID),
    ('po.auto_approve_below_amount', '5000000', 'INTEGER', 'PO_AUTO', N'Tự động duyệt PO dưới số tiền (VNĐ)', N'Tự động duyệt đơn đặt hàng nếu tổng tiền dưới số tiền này (VNĐ)', '0', '100000000', '5000000', 1, @AdminUserID);

-- ============================================================
-- SUPPLIER MAPPING SETTINGS
-- ============================================================

INSERT INTO AIAgentConfigurations (ConfigKey, ConfigValue, ConfigType, Category, DisplayName, Description, MinValue, MaxValue, DefaultValue, IsActive, UpdatedBy)
VALUES
    ('po.supplier_mapping', '{}', 'JSON', 'SUPPLIER_MAPPING', N'Ánh xạ nhà cung cấp theo sản phẩm', N'Thiết lập nhà cung cấp cho từng sản phẩm (JSON: {"ProductName": "SupplierID"})', NULL, NULL, '{}', 1, @AdminUserID);

-- ============================================================
-- GPT SERVICE SETTINGS
-- ============================================================

INSERT INTO AIAgentConfigurations (ConfigKey, ConfigValue, ConfigType, Category, DisplayName, Description, MinValue, MaxValue, DefaultValue, IsActive, UpdatedBy)
VALUES
    ('gpt.model', 'gpt-3.5-turbo', 'STRING', 'GPT_SERVICE', N'Mô hình GPT', N'Mô hình GPT sử dụng (gpt-3.5-turbo, gpt-4, etc.)', NULL, NULL, 'gpt-3.5-turbo', 1, @AdminUserID),
    ('gpt.max_tokens', '1000', 'INTEGER', 'GPT_SERVICE', N'Số token tối đa', N'Số token tối đa cho mỗi request GPT', '100', '4000', '1000', 1, @AdminUserID),
    ('gpt.temperature', '0.7', 'DECIMAL', 'GPT_SERVICE', N'Nhiệt độ (Temperature)', N'Độ sáng tạo của GPT (0-2), cao hơn = sáng tạo hơn', '0', '2', '0.7', 1, @AdminUserID),
    ('gpt.connect_timeout', '30', 'INTEGER', 'GPT_SERVICE', N'Timeout kết nối (giây)', N'Thời gian chờ kết nối đến GPT API (giây)', '5', '300', '30', 1, @AdminUserID),
    ('gpt.write_timeout', '30', 'INTEGER', 'GPT_SERVICE', N'Timeout ghi (giây)', N'Thời gian chờ ghi dữ liệu đến GPT API (giây)', '5', '300', '30', 1, @AdminUserID),
    ('gpt.read_timeout', '60', 'INTEGER', 'GPT_SERVICE', N'Timeout đọc (giây)', N'Thời gian chờ đọc phản hồi từ GPT API (giây)', '10', '600', '60', 1, @AdminUserID),
    ('gpt.enable_features', 'true', 'BOOLEAN', 'GPT_SERVICE', N'Bật tính năng GPT', N'Bật/tắt tất cả tính năng GPT trong hệ thống', NULL, NULL, 'true', 1, @AdminUserID);

-- ============================================================
-- NOTIFICATION SETTINGS
-- ============================================================

INSERT INTO AIAgentConfigurations (ConfigKey, ConfigValue, ConfigType, Category, DisplayName, Description, MinValue, MaxValue, DefaultValue, IsActive, UpdatedBy)
VALUES
    ('notification.daily_summary_time', '18:00', 'TIME', 'NOTIFICATION', N'Thời gian báo cáo hàng ngày', N'Thời gian gửi báo cáo tóm tắt hàng ngày (HH:mm)', NULL, NULL, '18:00', 1, @AdminUserID),
    ('notification.po_pending_check_interval', '14400', 'INTEGER', 'NOTIFICATION', N'Khoảng thời gian kiểm tra PO chờ duyệt (giây)', N'Khoảng thời gian giữa các lần kiểm tra đơn đặt hàng chờ duyệt (tính bằng giây)', '3600', '86400', '14400', 1, @AdminUserID),
    ('notification.enable_telegram', 'true', 'BOOLEAN', 'NOTIFICATION', N'Bật thông báo Telegram', N'Bật/tắt thông báo qua Telegram', NULL, NULL, 'true', 1, @AdminUserID),
    ('notification.enable_email', 'false', 'BOOLEAN', 'NOTIFICATION', N'Bật thông báo Email', N'Bật/tắt thông báo qua Email', NULL, NULL, 'false', 1, @AdminUserID),
    ('notification.enable_inapp', 'true', 'BOOLEAN', 'NOTIFICATION', N'Bật thông báo trong ứng dụng', N'Bật/tắt thông báo trong ứng dụng (notification bell)', NULL, NULL, 'true', 1, @AdminUserID),
    ('notification.quiet_hours_enabled', 'false', 'BOOLEAN', 'NOTIFICATION', N'Bật giờ yên lặng', N'Bật/tắt giờ yên lặng (không gửi thông báo trong khoảng thời gian này)', NULL, NULL, 'false', 1, @AdminUserID),
    ('notification.quiet_hours_start', '22:00', 'TIME', 'NOTIFICATION', N'Bắt đầu giờ yên lặng', N'Thời gian bắt đầu giờ yên lặng (HH:mm)', NULL, NULL, '22:00', 1, @AdminUserID),
    ('notification.quiet_hours_end', '08:00', 'TIME', 'NOTIFICATION', N'Kết thúc giờ yên lặng', N'Thời gian kết thúc giờ yên lặng (HH:mm)', NULL, NULL, '08:00', 1, @AdminUserID);

GO

PRINT '✅ AI Agent Configurations default data inserted successfully';
GO

