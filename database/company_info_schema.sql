-- CompanyInfo Table Schema
-- Description: Stores company information for LiteFlow (singleton pattern - only 1 record)
-- Note: TaxCode is read-only and loaded from .env file, not stored in database

USE LiteFlowDBO;
GO

-- CompanyInfo Table
CREATE TABLE CompanyInfo (
    CompanyID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Name NVARCHAR(200) NOT NULL,
    Address NVARCHAR(500),
    Phone NVARCHAR(50),
    Email NVARCHAR(150),
    CreatedAt DATETIME2 DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSUTCDATETIME()
);
GO

-- Create trigger to update UpdatedAt timestamp
CREATE TRIGGER trg_CompanyInfo_UpdatedAt
ON CompanyInfo
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE CompanyInfo
    SET UpdatedAt = SYSUTCDATETIME()
    WHERE CompanyID IN (SELECT CompanyID FROM inserted);
END;
GO

-- Insert default company info (if not exists)
IF NOT EXISTS (SELECT 1 FROM CompanyInfo)
BEGIN
    INSERT INTO CompanyInfo (Name, Address, Phone, Email)
    VALUES (
        N'LiteFlow Restaurant',
        N'123 Nguyễn Huệ, Quận 1, TP.HCM',
        N'1900-1234',
        N'procurement@liteflow.com'
    );
    PRINT '✅ Default company info inserted';
END
ELSE
BEGIN
    PRINT 'ℹ️ Company info already exists';
END
GO

PRINT '✅ CompanyInfo table created successfully';
GO

