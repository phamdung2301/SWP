-- ============================================================
-- SMART ALERT SYSTEM - DATABASE SCHEMA
-- AI-Enhanced Alert & Notification System with GPT Integration
-- ============================================================

USE LiteFlowDBO;
GO

-- ============================================================
-- 1️⃣ ALERT CONFIGURATIONS
-- Cấu hình các loại cảnh báo trong hệ thống
-- ============================================================

IF OBJECT_ID('AlertConfigurations', 'U') IS NOT NULL DROP TABLE AlertConfigurations;
GO

CREATE TABLE AlertConfigurations (
    AlertID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    
    -- Basic Info
    AlertType NVARCHAR(50) NOT NULL, -- PO_PENDING, LOW_INVENTORY, REVENUE_DROP, DAILY_SUMMARY
    Name NVARCHAR(200) NOT NULL,
    Description NVARCHAR(500),
    IsEnabled BIT DEFAULT 1,
    
    -- Trigger Conditions (JSON format)
    TriggerConditions NVARCHAR(MAX), 
    -- Example: {"threshold": 5, "timeWindow": "24h", "comparison": "less_than"}
    
    -- Notification Channels
    NotifySlack BIT DEFAULT 0,
    NotifyTelegram BIT DEFAULT 0,
    NotifyEmail BIT DEFAULT 0,
    NotifyInApp BIT DEFAULT 1, -- In-app notification bell
    
    -- Recipients (JSON array of UserIDs or external identifiers)
    Recipients NVARCHAR(MAX),
    -- Example: ["user-uuid-1", "user-uuid-2"] or ["@owner", "@procurement"]
    
    -- AI Enhancement
    UseGPTSummary BIT DEFAULT 0, -- Sử dụng GPT để tóm tắt thông minh
    GPTPromptTemplate NVARCHAR(MAX), -- Custom prompt cho GPT
    
    -- Schedule (Cron expression for periodic alerts)
    ScheduleCron NVARCHAR(100), -- "0 18 * * *" = 6PM daily
    LastTriggered DATETIME2 NULL,
    NextScheduledRun DATETIME2 NULL,
    
    -- Priority
    Priority NVARCHAR(20) DEFAULT 'MEDIUM' CHECK (Priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    
    -- Metadata
    CreatedBy UNIQUEIDENTIFIER REFERENCES Users(UserID) ON DELETE NO ACTION,
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),
    
    -- Audit
    TotalTriggered INT DEFAULT 0,
    LastTriggeredBy UNIQUEIDENTIFIER NULL REFERENCES Users(UserID) ON DELETE NO ACTION
);
GO

CREATE INDEX IX_AlertConfigs_Type ON AlertConfigurations(AlertType);
CREATE INDEX IX_AlertConfigs_Enabled ON AlertConfigurations(IsEnabled);
CREATE INDEX IX_AlertConfigs_Priority ON AlertConfigurations(Priority);
CREATE INDEX IX_AlertConfigs_Schedule ON AlertConfigurations(NextScheduledRun) WHERE NextScheduledRun IS NOT NULL;
GO

-- ============================================================
-- 2️⃣ NOTIFICATION CHANNELS
-- Cấu hình kết nối với Slack, Telegram, Email
-- ============================================================

IF OBJECT_ID('NotificationChannels', 'U') IS NOT NULL DROP TABLE NotificationChannels;
GO

CREATE TABLE NotificationChannels (
    ChannelID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    
    -- Channel Type
    ChannelType NVARCHAR(20) NOT NULL CHECK (ChannelType IN ('SLACK', 'TELEGRAM', 'EMAIL', 'SMS')),
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(500),
    
    -- Slack Configuration
    SlackWebhookURL NVARCHAR(500) NULL,
    SlackChannel NVARCHAR(100) NULL, -- #general, #alerts, etc.
    
    -- Telegram Configuration
    TelegramBotToken NVARCHAR(200) NULL,
    TelegramChatID NVARCHAR(100) NULL, -- Chat/Group ID
    
    -- Email Configuration
    EmailRecipients NVARCHAR(MAX) NULL, -- JSON array of emails
    EmailFrom NVARCHAR(200) NULL,
    
    -- Status
    IsActive BIT DEFAULT 1,
    LastUsed DATETIME2 NULL,
    LastError NVARCHAR(MAX) NULL,
    
    -- Rate Limiting
    MaxRequestsPerHour INT DEFAULT 100,
    CurrentHourRequests INT DEFAULT 0,
    HourResetAt DATETIME2 DEFAULT DATEADD(HOUR, 1, SYSDATETIME()),
    
    -- Metadata
    CreatedBy UNIQUEIDENTIFIER REFERENCES Users(UserID) ON DELETE NO ACTION,
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME()
);
GO

CREATE INDEX IX_NotificationChannels_Type ON NotificationChannels(ChannelType);
CREATE INDEX IX_NotificationChannels_Active ON NotificationChannels(IsActive);
GO

-- ============================================================
-- 3️⃣ ALERT HISTORY
-- Lịch sử các cảnh báo đã gửi
-- ============================================================

IF OBJECT_ID('AlertHistory', 'U') IS NOT NULL DROP TABLE AlertHistory;
GO

CREATE TABLE AlertHistory (
    HistoryID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    AlertID UNIQUEIDENTIFIER NULL REFERENCES AlertConfigurations(AlertID) ON DELETE NO ACTION,
    
    -- Alert Info
    AlertType NVARCHAR(50) NOT NULL,
    Title NVARCHAR(200) NOT NULL,
    Message NVARCHAR(MAX) NOT NULL,
    MessageHTML NVARCHAR(MAX) NULL, -- Formatted HTML version
    
    -- Context Data (JSON) - Data used to generate alert
    ContextData NVARCHAR(MAX),
    -- Example: {"poId": "xxx", "amount": 1000000, "supplierName": "Trung Nguyen"}
    
    -- AI-Generated Content
    GPTSummary NVARCHAR(MAX) NULL, -- GPT-generated summary
    GPTInteractionID UNIQUEIDENTIFIER NULL, -- Link to GPTInteractions table
    
    -- Delivery Status
    SentToSlack BIT DEFAULT 0,
    SentToTelegram BIT DEFAULT 0,
    SentToEmail BIT DEFAULT 0,
    SentInApp BIT DEFAULT 0,
    
    DeliveryStatus NVARCHAR(20) DEFAULT 'PENDING' CHECK (DeliveryStatus IN ('PENDING', 'SENT', 'FAILED', 'PARTIAL')),
    ErrorMessage NVARCHAR(500) NULL,
    
    -- User Interaction
    IsRead BIT DEFAULT 0,
    ReadAt DATETIME2 NULL,
    ReadBy UNIQUEIDENTIFIER NULL REFERENCES Users(UserID) ON DELETE NO ACTION,
    
    IsDismissed BIT DEFAULT 0,
    DismissedAt DATETIME2 NULL,
    DismissedBy UNIQUEIDENTIFIER NULL REFERENCES Users(UserID) ON DELETE NO ACTION,
    
    -- Action Taken
    ActionTaken NVARCHAR(100) NULL, -- APPROVED, REJECTED, VIEWED, etc.
    ActionTakenAt DATETIME2 NULL,
    ActionTakenBy UNIQUEIDENTIFIER NULL REFERENCES Users(UserID) ON DELETE NO ACTION,
    
    -- Priority
    Priority NVARCHAR(20) DEFAULT 'MEDIUM' CHECK (Priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    
    -- Timestamps
    TriggeredAt DATETIME2 DEFAULT SYSDATETIME(),
    SentAt DATETIME2 NULL,
    
    -- Expiry (auto-dismiss old alerts)
    ExpiresAt DATETIME2 NULL
);
GO

CREATE INDEX IX_AlertHistory_Type ON AlertHistory(AlertType);
CREATE INDEX IX_AlertHistory_Status ON AlertHistory(DeliveryStatus);
CREATE INDEX IX_AlertHistory_Triggered ON AlertHistory(TriggeredAt);
CREATE INDEX IX_AlertHistory_Read ON AlertHistory(IsRead, IsDismissed);
CREATE INDEX IX_AlertHistory_Priority ON AlertHistory(Priority);
CREATE INDEX IX_AlertHistory_Expiry ON AlertHistory(ExpiresAt) WHERE ExpiresAt IS NOT NULL;
GO

-- ============================================================
-- 4️⃣ GPT INTERACTIONS
-- Theo dõi sử dụng GPT API, chi phí, và hiệu quả
-- ============================================================

IF OBJECT_ID('GPTInteractions', 'U') IS NOT NULL DROP TABLE GPTInteractions;
GO

CREATE TABLE GPTInteractions (
    InteractionID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    
    -- Request Info
    Model NVARCHAR(50) NOT NULL, -- gpt-4o-mini, gpt-4o, etc.
    Purpose NVARCHAR(100) NOT NULL, -- DAILY_SUMMARY, INVENTORY_ALERT, etc.
    
    -- Prompt & Response
    SystemPrompt NVARCHAR(MAX) NOT NULL,
    UserPrompt NVARCHAR(MAX) NOT NULL,
    AssistantResponse NVARCHAR(MAX) NULL,
    
    -- Token Usage
    PromptTokens INT DEFAULT 0,
    CompletionTokens INT DEFAULT 0,
    TotalTokens INT DEFAULT 0,
    
    -- Cost Calculation (based on model pricing)
    EstimatedCostUSD DECIMAL(10,6) DEFAULT 0.000000,
    EstimatedCostVND DECIMAL(12,2) DEFAULT 0.00,
    
    -- Performance
    ResponseTimeMs INT NULL, -- Response time in milliseconds
    
    -- Status
    Status NVARCHAR(20) DEFAULT 'SUCCESS' CHECK (Status IN ('SUCCESS', 'FAILED', 'TIMEOUT', 'RATE_LIMITED')),
    ErrorMessage NVARCHAR(500) NULL,
    
    -- Quality Feedback
    WasHelpful BIT NULL, -- User feedback
    FeedbackNotes NVARCHAR(500) NULL,
    
    -- Context
    RelatedAlertID UNIQUEIDENTIFIER NULL,
    RelatedObjectType NVARCHAR(50) NULL, -- PO, Order, Inventory, etc.
    RelatedObjectID NVARCHAR(100) NULL,
    
    -- Metadata
    CreatedBy UNIQUEIDENTIFIER NULL REFERENCES Users(UserID) ON DELETE NO ACTION,
    CreatedAt DATETIME2 DEFAULT SYSDATETIME()
);
GO

CREATE INDEX IX_GPTInteractions_Model ON GPTInteractions(Model);
CREATE INDEX IX_GPTInteractions_Purpose ON GPTInteractions(Purpose);
CREATE INDEX IX_GPTInteractions_Created ON GPTInteractions(CreatedAt);
CREATE INDEX IX_GPTInteractions_Cost ON GPTInteractions(EstimatedCostUSD);
CREATE INDEX IX_GPTInteractions_Status ON GPTInteractions(Status);
GO

-- ============================================================
-- 5️⃣ USER ALERT PREFERENCES
-- Tùy chọn cá nhân của từng user về alerts
-- ============================================================

IF OBJECT_ID('UserAlertPreferences', 'U') IS NOT NULL DROP TABLE UserAlertPreferences;
GO

CREATE TABLE UserAlertPreferences (
    PreferenceID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserID UNIQUEIDENTIFIER NOT NULL REFERENCES Users(UserID) ON DELETE CASCADE,
    
    -- Global Settings
    EnableNotifications BIT DEFAULT 1,
    EnableSlack BIT DEFAULT 1,
    EnableTelegram BIT DEFAULT 0,
    EnableEmail BIT DEFAULT 0,
    EnableInApp BIT DEFAULT 1,
    
    -- Per-Type Settings (JSON)
    AlertTypeSettings NVARCHAR(MAX),
    -- Example: {"PO_PENDING": true, "LOW_INVENTORY": true, "DAILY_SUMMARY": false}
    
    -- Quiet Hours
    QuietHoursEnabled BIT DEFAULT 0,
    QuietHoursStart TIME NULL, -- 22:00
    QuietHoursEnd TIME NULL,   -- 08:00
    
    -- Telegram/Slack User IDs
    TelegramUserID NVARCHAR(100) NULL,
    SlackUserID NVARCHAR(100) NULL,
    
    -- Metadata
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),
    
    CONSTRAINT UQ_UserAlertPreferences_User UNIQUE (UserID)
);
GO

CREATE INDEX IX_UserAlertPrefs_EnableNotif ON UserAlertPreferences(EnableNotifications);
GO

-- ============================================================
-- 6️⃣ VIEWS FOR REPORTING
-- ============================================================

-- Active Alerts Summary
IF OBJECT_ID('vw_ActiveAlerts', 'V') IS NOT NULL DROP VIEW vw_ActiveAlerts;
GO

CREATE VIEW vw_ActiveAlerts AS
SELECT 
    h.HistoryID,
    h.AlertType,
    h.Title,
    h.Message,
    h.Priority,
    h.TriggeredAt,
    h.IsRead,
    h.IsDismissed,
    DATEDIFF(MINUTE, h.TriggeredAt, SYSDATETIME()) as MinutesAgo,
    ac.Name as AlertConfigName,
    u.DisplayName as TriggeredByName
FROM AlertHistory h
LEFT JOIN AlertConfigurations ac ON h.AlertID = ac.AlertID
LEFT JOIN Users u ON h.ReadBy = u.UserID
WHERE h.IsDismissed = 0 
  AND (h.ExpiresAt IS NULL OR h.ExpiresAt > SYSDATETIME())
  AND h.DeliveryStatus IN ('SENT', 'PARTIAL');
GO

-- GPT Cost Summary
IF OBJECT_ID('vw_GPTCostSummary', 'V') IS NOT NULL DROP VIEW vw_GPTCostSummary;
GO

CREATE VIEW vw_GPTCostSummary AS
SELECT 
    Model,
    Purpose,
    COUNT(*) as TotalRequests,
    SUM(TotalTokens) as TotalTokens,
    SUM(EstimatedCostUSD) as TotalCostUSD,
    SUM(EstimatedCostVND) as TotalCostVND,
    AVG(ResponseTimeMs) as AvgResponseTimeMs,
    SUM(CASE WHEN Status = 'SUCCESS' THEN 1 ELSE 0 END) as SuccessCount,
    SUM(CASE WHEN Status = 'FAILED' THEN 1 ELSE 0 END) as FailedCount
FROM GPTInteractions
GROUP BY Model, Purpose;
GO

-- ============================================================
-- 7️⃣ STORED PROCEDURES
-- ============================================================

-- Get Unread Alerts for User
IF OBJECT_ID('sp_GetUnreadAlerts', 'P') IS NOT NULL DROP PROCEDURE sp_GetUnreadAlerts;
GO

CREATE PROCEDURE sp_GetUnreadAlerts
    @UserID UNIQUEIDENTIFIER,
    @Limit INT = 10
AS
BEGIN
    SELECT TOP (@Limit)
        h.HistoryID,
        h.AlertType,
        h.Title,
        h.Message,
        h.Priority,
        h.TriggeredAt,
        DATEDIFF(MINUTE, h.TriggeredAt, SYSDATETIME()) as MinutesAgo
    FROM AlertHistory h
    WHERE h.IsRead = 0 
      AND h.IsDismissed = 0
      AND (h.ExpiresAt IS NULL OR h.ExpiresAt > SYSDATETIME())
      AND h.DeliveryStatus IN ('SENT', 'PARTIAL')
      -- TODO: Add role-based filtering based on @UserID
    ORDER BY h.Priority DESC, h.TriggeredAt DESC;
END;
GO

-- Mark Alert as Read
IF OBJECT_ID('sp_MarkAlertRead', 'P') IS NOT NULL DROP PROCEDURE sp_MarkAlertRead;
GO

CREATE PROCEDURE sp_MarkAlertRead
    @HistoryID UNIQUEIDENTIFIER,
    @UserID UNIQUEIDENTIFIER
AS
BEGIN
    UPDATE AlertHistory
    SET IsRead = 1,
        ReadAt = SYSDATETIME(),
        ReadBy = @UserID
    WHERE HistoryID = @HistoryID;
END;
GO

-- Track GPT Usage
IF OBJECT_ID('sp_TrackGPTInteraction', 'P') IS NOT NULL DROP PROCEDURE sp_TrackGPTInteraction;
GO

CREATE PROCEDURE sp_TrackGPTInteraction
    @Model NVARCHAR(50),
    @Purpose NVARCHAR(100),
    @SystemPrompt NVARCHAR(MAX),
    @UserPrompt NVARCHAR(MAX),
    @AssistantResponse NVARCHAR(MAX),
    @PromptTokens INT,
    @CompletionTokens INT,
    @ResponseTimeMs INT,
    @Status NVARCHAR(20),
    @InteractionID UNIQUEIDENTIFIER OUTPUT
AS
BEGIN
    SET @InteractionID = NEWID();
    
    DECLARE @TotalTokens INT = @PromptTokens + @CompletionTokens;
    DECLARE @CostUSD DECIMAL(10,6);
    
    -- Calculate cost based on gpt-4o-mini pricing
    -- Input: $0.150 per 1M tokens, Output: $0.600 per 1M tokens
    SET @CostUSD = (@PromptTokens * 0.150 / 1000000) + (@CompletionTokens * 0.600 / 1000000);
    
    INSERT INTO GPTInteractions (
        InteractionID, Model, Purpose, SystemPrompt, UserPrompt, 
        AssistantResponse, PromptTokens, CompletionTokens, TotalTokens,
        EstimatedCostUSD, EstimatedCostVND, ResponseTimeMs, Status
    ) VALUES (
        @InteractionID, @Model, @Purpose, @SystemPrompt, @UserPrompt,
        @AssistantResponse, @PromptTokens, @CompletionTokens, @TotalTokens,
        @CostUSD, @CostUSD * 24800, @ResponseTimeMs, @Status
    );
END;
GO

-- ============================================================
-- 8️⃣ TRIGGERS
-- ============================================================

-- Auto-update AlertConfigurations.TotalTriggered
IF OBJECT_ID('tr_AlertHistory_UpdateCounter', 'TR') IS NOT NULL DROP TRIGGER tr_AlertHistory_UpdateCounter;
GO

CREATE TRIGGER tr_AlertHistory_UpdateCounter
ON AlertHistory
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    UPDATE ac
    SET TotalTriggered = TotalTriggered + 1,
        LastTriggered = i.TriggeredAt
    FROM AlertConfigurations ac
    INNER JOIN inserted i ON ac.AlertID = i.AlertID
    WHERE i.AlertID IS NOT NULL;
END;
GO

-- Auto-expire old alerts
IF OBJECT_ID('tr_AlertHistory_AutoExpire', 'TR') IS NOT NULL DROP TRIGGER tr_AlertHistory_AutoExpire;
GO

CREATE TRIGGER tr_AlertHistory_AutoExpire
ON AlertHistory
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Auto-dismiss alerts older than 7 days if not already dismissed
    UPDATE AlertHistory
    SET IsDismissed = 1,
        DismissedAt = SYSDATETIME()
    WHERE IsDismissed = 0
      AND TriggeredAt < DATEADD(DAY, -7, SYSDATETIME())
      AND Priority IN ('LOW', 'MEDIUM');
END;
GO


-- ============================================================
-- NOTICES TABLE
-- Bảng lưu trữ thông báo gửi từ Admin đến Employee
-- ============================================================

IF OBJECT_ID('Notices', 'U') IS NOT NULL DROP TABLE Notices;
GO

CREATE TABLE Notices (
    NoticeID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),

    -- Notice Content
    Title NVARCHAR(200) NOT NULL,
    Content NVARCHAR(MAX) NOT NULL,

    -- Notice Type/Priority
    NoticeType NVARCHAR(20) DEFAULT 'general' CHECK (NoticeType IN ('important', 'general', 'info', 'urgent')),

    -- Status
    IsActive BIT DEFAULT 1,
    IsPinned BIT DEFAULT 0, -- Ghim thông báo lên đầu

    -- Display Period
    PublishedAt DATETIME2 DEFAULT SYSDATETIME(),
    ExpiresAt DATETIME2 NULL, -- Null = không hết hạn

    -- Target Audience (NULL = all employees)
    TargetRoles NVARCHAR(MAX) NULL, -- JSON array: ["Employee", "Manager"]
    TargetUserIDs NVARCHAR(MAX) NULL, -- JSON array of specific user IDs

    -- Creator Info
    CreatedBy UNIQUEIDENTIFIER NOT NULL REFERENCES Users(UserID) ON DELETE NO ACTION,
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),

    -- Metadata
    ViewCount INT DEFAULT 0, -- Số lượt xem

    CONSTRAINT FK_Notices_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(UserID) ON DELETE NO ACTION
);
GO

CREATE INDEX IX_Notices_Type ON Notices(NoticeType);
CREATE INDEX IX_Notices_Active ON Notices(IsActive);
CREATE INDEX IX_Notices_Published ON Notices(PublishedAt);
CREATE INDEX IX_Notices_Pinned ON Notices(IsPinned) WHERE IsPinned = 1;
CREATE INDEX IX_Notices_Expiry ON Notices(ExpiresAt) WHERE ExpiresAt IS NOT NULL;
GO

-- ============================================================
-- NOTICE READS TABLE
-- Theo dõi nhân viên nào đã đọc thông báo nào
-- ============================================================

IF OBJECT_ID('NoticeReads', 'U') IS NOT NULL DROP TABLE NoticeReads;
GO

CREATE TABLE NoticeReads (
    ReadID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    NoticeID UNIQUEIDENTIFIER NOT NULL,
    UserID UNIQUEIDENTIFIER NOT NULL,
    ReadAt DATETIME2 DEFAULT SYSDATETIME(),

    CONSTRAINT FK_NoticeReads_Notice FOREIGN KEY (NoticeID) REFERENCES Notices(NoticeID) ON DELETE CASCADE,
    CONSTRAINT FK_NoticeReads_User FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    CONSTRAINT UQ_NoticeReads_NoticeUser UNIQUE (NoticeID, UserID)
);
GO

CREATE INDEX IX_NoticeReads_NoticeID ON NoticeReads(NoticeID);
CREATE INDEX IX_NoticeReads_UserID ON NoticeReads(UserID);
GO

-- ============================================================
-- VIEWS
-- ============================================================

-- Active Notices View
IF OBJECT_ID('vw_ActiveNotices', 'V') IS NOT NULL DROP VIEW vw_ActiveNotices;
GO

CREATE VIEW vw_ActiveNotices AS
SELECT
    n.NoticeID,
    n.Title,
    n.Content,
    n.NoticeType,
    n.IsPinned,
    n.PublishedAt,
    n.ExpiresAt,
    n.ViewCount,
    u.DisplayName as CreatedByName,
    n.CreatedBy,
    n.CreatedAt,
    DATEDIFF(DAY, n.PublishedAt, SYSDATETIME()) as DaysAgo
FROM Notices n
INNER JOIN Users u ON n.CreatedBy = u.UserID
WHERE n.IsActive = 1
  AND (n.ExpiresAt IS NULL OR n.ExpiresAt > SYSDATETIME());
GO

-- ============================================================
-- STORED PROCEDURES
-- ============================================================

-- Get Active Notices for Employee Dashboard
IF OBJECT_ID('sp_GetActiveNotices', 'P') IS NOT NULL DROP PROCEDURE sp_GetActiveNotices;
GO

CREATE PROCEDURE sp_GetActiveNotices
    @UserID UNIQUEIDENTIFIER = NULL,
    @Limit INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@Limit)
        n.NoticeID,
        n.Title,
        n.Content,
        n.NoticeType,
        n.IsPinned,
        n.PublishedAt,
        n.ViewCount,
        u.DisplayName as CreatedByName,
        CASE WHEN nr.ReadID IS NOT NULL THEN 1 ELSE 0 END as IsRead
    FROM Notices n
    INNER JOIN Users u ON n.CreatedBy = u.UserID
    LEFT JOIN NoticeReads nr ON n.NoticeID = nr.NoticeID AND nr.UserID = @UserID
    WHERE n.IsActive = 1
      AND (n.ExpiresAt IS NULL OR n.ExpiresAt > SYSDATETIME())
    ORDER BY n.IsPinned DESC, n.PublishedAt DESC;
END;
GO

-- Mark Notice as Read
IF OBJECT_ID('sp_MarkNoticeRead', 'P') IS NOT NULL DROP PROCEDURE sp_MarkNoticeRead;
GO

CREATE PROCEDURE sp_MarkNoticeRead
    @NoticeID UNIQUEIDENTIFIER,
    @UserID UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    -- Insert or ignore if already read
    IF NOT EXISTS (SELECT 1 FROM NoticeReads WHERE NoticeID = @NoticeID AND UserID = @UserID)
    BEGIN
        INSERT INTO NoticeReads (NoticeID, UserID, ReadAt)
        VALUES (@NoticeID, @UserID, SYSDATETIME());

        -- Update view count
        UPDATE Notices
        SET ViewCount = ViewCount + 1
        WHERE NoticeID = @NoticeID;
    END
END;
GO

-- Create New Notice
IF OBJECT_ID('sp_CreateNotice', 'P') IS NOT NULL DROP PROCEDURE sp_CreateNotice;
GO

CREATE PROCEDURE sp_CreateNotice
    @Title NVARCHAR(200),
    @Content NVARCHAR(MAX),
    @NoticeType NVARCHAR(20),
    @IsPinned BIT = 0,
    @ExpiresAt DATETIME2 = NULL,
    @CreatedBy UNIQUEIDENTIFIER,
    @NoticeID UNIQUEIDENTIFIER OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    SET @NoticeID = NEWID();

    INSERT INTO Notices (
        NoticeID,
        Title,
        Content,
        NoticeType,
        IsPinned,
        ExpiresAt,
        CreatedBy,
        PublishedAt
    )
    VALUES (
        @NoticeID,
        @Title,
        @Content,
        @NoticeType,
        @IsPinned,
        @ExpiresAt,
        @CreatedBy,
        SYSDATETIME()
    );
END;
GO

-- ============================================================
-- TRIGGERS
-- ============================================================

-- Auto-expire old notices
IF OBJECT_ID('tr_Notices_AutoExpire', 'TR') IS NOT NULL DROP TRIGGER tr_Notices_AutoExpire;
GO

CREATE TRIGGER tr_Notices_AutoExpire
ON Notices
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Deactivate expired notices
    UPDATE Notices
    SET IsActive = 0
    WHERE ExpiresAt IS NOT NULL
      AND ExpiresAt < SYSDATETIME()
      AND IsActive = 1;
END;
GO
CREATE TABLE LeaveRequests (
    LeaveRequestID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    LeaveType NVARCHAR(20) NOT NULL CHECK (LeaveType IN (N'Nghỉ phép', N'Nghỉ bệnh', N'Nghỉ không lương', N'Nghỉ khác')),
    StartDate DATE NOT NULL,
    EndDate DATE NOT NULL,
    TotalDays DECIMAL(5,2) NOT NULL,
    Reason NVARCHAR(1000) NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT N'Chờ duyệt' CHECK (Status IN (N'Chờ duyệt', N'Đã duyệt', N'Từ chối', N'Đã hủy')),
    ReviewedBy UNIQUEIDENTIFIER NULL,
    ReviewedAt DATETIME2 NULL,
    ReviewNotes NVARCHAR(500) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_LeaveRequests_Employee FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
    CONSTRAINT FK_LeaveRequests_ReviewedBy FOREIGN KEY (ReviewedBy) REFERENCES Users(UserID) ON DELETE NO ACTION,
    CONSTRAINT CK_LeaveRequests_DateRange CHECK (EndDate >= StartDate),
    CONSTRAINT CK_LeaveRequests_TotalDays CHECK (TotalDays > 0)
);
GO

-- Indexes for better query performance
CREATE INDEX IX_LeaveRequests_Employee ON LeaveRequests(EmployeeID);
CREATE INDEX IX_LeaveRequests_Status ON LeaveRequests(Status);
CREATE INDEX IX_LeaveRequests_StartDate ON LeaveRequests(StartDate);
CREATE INDEX IX_LeaveRequests_EndDate ON LeaveRequests(EndDate);
CREATE INDEX IX_LeaveRequests_CreatedAt ON LeaveRequests(CreatedAt);
GO

-- Trigger to update UpdatedAt timestamp
CREATE OR ALTER TRIGGER TRG_LeaveRequests_UpdatedAt
ON LeaveRequests
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE LeaveRequests
    SET UpdatedAt = SYSDATETIME()
    FROM LeaveRequests lr
    INNER JOIN inserted i ON lr.LeaveRequestID = i.LeaveRequestID;
END;
GO
-- ============================================================
-- SUCCESS MESSAGE
-- ============================================================

PRINT '========================================';
PRINT '✅ ALERT SYSTEM SCHEMA CREATED!';
PRINT '========================================';
PRINT 'Tables Created:';
PRINT '  1. AlertConfigurations';
PRINT '  2. NotificationChannels';
PRINT '  3. AlertHistory';
PRINT '  4. GPTInteractions';
PRINT '  5. UserAlertPreferences';
PRINT '';
PRINT 'Views Created:';
PRINT '  1. vw_ActiveAlerts';
PRINT '  2. vw_GPTCostSummary';
PRINT '';
PRINT 'Stored Procedures:';
PRINT '  1. sp_GetUnreadAlerts';
PRINT '  2. sp_MarkAlertRead';
PRINT '  3. sp_TrackGPTInteraction';
PRINT '';
PRINT '🤖 GPT Model: gpt-4o-mini';
PRINT '💰 Estimated Cost: ~$0.06/month';
PRINT '========================================';
GO


