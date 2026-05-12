-- ============================================================
-- SETUP TELEGRAM CHAT ID FOR USER
-- Chạy script này để link Telegram Chat ID với User
-- ============================================================

USE LiteFlowDBO;
GO

-- Example: Link Telegram Chat ID 6969473762 to a user
-- Thay đổi UserID và TelegramUserID theo nhu cầu

-- Option 1: Update existing UserAlertPreference
UPDATE UserAlertPreferences
SET TelegramUserID = '6969473762',
    EnableTelegram = 1,
    EnableNotifications = 1,
    UpdatedAt = SYSDATETIME()
WHERE UserID = (SELECT TOP 1 UserID FROM Users WHERE Email = 'owner@liteflow.vn'); -- Thay đổi email theo nhu cầu
GO

-- Option 2: Create new UserAlertPreference if not exists
-- Lấy UserID từ Users table và tạo preference mới
DECLARE @UserID UNIQUEIDENTIFIER = (SELECT TOP 1 UserID FROM Users WHERE Email = 'owner@liteflow.vn');

IF NOT EXISTS (SELECT 1 FROM UserAlertPreferences WHERE UserID = @UserID)
BEGIN
    INSERT INTO UserAlertPreferences (
        UserID,
        EnableNotifications,
        EnableTelegram,
        TelegramUserID,
        CreatedAt,
        UpdatedAt
    )
    VALUES (
        @UserID,
        1,  -- EnableNotifications
        1,  -- EnableTelegram
        '6969473762',  -- Telegram Chat ID
        SYSDATETIME(),
        SYSDATETIME()
    );
    
    PRINT '✅ Created UserAlertPreference for User: ' + CAST(@UserID AS NVARCHAR(36));
END
ELSE
BEGIN
    UPDATE UserAlertPreferences
    SET TelegramUserID = '6969473762',
        EnableTelegram = 1,
        EnableNotifications = 1,
        UpdatedAt = SYSDATETIME()
    WHERE UserID = @UserID;
    
    PRINT '✅ Updated UserAlertPreference for User: ' + CAST(@UserID AS NVARCHAR(36));
END
GO

-- Verify setup
SELECT 
    u.UserID,
    u.Email,
    uap.TelegramUserID,
    uap.EnableTelegram,
    uap.EnableNotifications
FROM Users u
LEFT JOIN UserAlertPreferences uap ON u.UserID = uap.UserID
WHERE uap.TelegramUserID IS NOT NULL;
GO

