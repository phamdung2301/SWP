-- ============================================================
-- STOCK ALERT NOTIFICATIONS - DATABASE SCHEMA
-- Tracking notifications đã gửi để tránh spam
-- ============================================================

USE LiteFlowDBO;
GO

-- ============================================================
-- STOCK ALERT NOTIFICATIONS TABLE
-- ============================================================

IF OBJECT_ID('StockAlertNotifications', 'U') IS NOT NULL DROP TABLE StockAlertNotifications;
GO

CREATE TABLE StockAlertNotifications (
    NotificationID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    
    -- Reference to product variant and user
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    UserID UNIQUEIDENTIFIER NOT NULL,
    
    -- Alert information
    AlertThreshold INT NOT NULL,  -- 10 hoặc 20
    StockLevel INT NOT NULL,  -- Stock level tại thời điểm gửi
    
    -- Notification details
    SentAt DATETIME2 DEFAULT SYSDATETIME(),
    MessageSent NVARCHAR(MAX),  -- Nội dung message đã gửi
    
    -- Status
    IsAcknowledged BIT DEFAULT 0,  -- User đã xem chưa
    
    -- Foreign keys
    FOREIGN KEY (ProductVariantID) REFERENCES ProductVariant(ProductVariantID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);
GO

-- Indexes for quick lookup
CREATE INDEX IX_StockAlertNotifications_UserVariant 
ON StockAlertNotifications(UserID, ProductVariantID, AlertThreshold);

CREATE INDEX IX_StockAlertNotifications_ProductVariant 
ON StockAlertNotifications(ProductVariantID, AlertThreshold, SentAt);

CREATE INDEX IX_StockAlertNotifications_SentAt 
ON StockAlertNotifications(SentAt DESC);
GO

