-- ============================================================
-- PO ALERT NOTIFICATIONS - DATABASE SCHEMA
-- Tracking notifications đã gửi cho PO mới để tránh spam
-- ============================================================

USE LiteFlowDBO;
GO

-- ============================================================
-- PO ALERT NOTIFICATIONS TABLE
-- ============================================================

IF OBJECT_ID('POAlertNotifications', 'U') IS NOT NULL DROP TABLE POAlertNotifications;
GO

CREATE TABLE POAlertNotifications (
    NotificationID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    
    -- Reference to PO and user
    POID UNIQUEIDENTIFIER NOT NULL,
    UserID UNIQUEIDENTIFIER NOT NULL,
    
    -- Notification details
    SentAt DATETIME2 DEFAULT SYSDATETIME(),
    MessageSent NVARCHAR(MAX),  -- Nội dung message đã gửi
    
    -- Status
    IsAcknowledged BIT DEFAULT 0,  -- User đã xem chưa
    
    -- Foreign keys
    FOREIGN KEY (POID) REFERENCES PurchaseOrders(POID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);
GO

-- Indexes for quick lookup
CREATE UNIQUE INDEX IX_POAlertNotifications_POUser 
ON POAlertNotifications(POID, UserID);

CREATE INDEX IX_POAlertNotifications_POID 
ON POAlertNotifications(POID, SentAt);

CREATE INDEX IX_POAlertNotifications_SentAt 
ON POAlertNotifications(SentAt DESC);
GO

