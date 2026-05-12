-- ============================================================
-- AI AGENT CONFIGURATIONS - DATABASE SCHEMA
-- Cấu hình các thông số cho AI Agent trong hệ thống
-- ============================================================

USE LiteFlowDBO;
GO

-- ============================================================
-- AI AGENT CONFIGURATIONS TABLE
-- Lưu trữ tất cả các cấu hình của AI Agent
-- ============================================================

-- Drop table if exists
IF OBJECT_ID('AIAgentConfigurations', 'U') IS NOT NULL 
    DROP TABLE AIAgentConfigurations;
GO

CREATE TABLE AIAgentConfigurations (
    ConfigID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    
    -- Config Key (unique identifier)
    ConfigKey NVARCHAR(100) NOT NULL UNIQUE,
    -- Examples: 'stock.warning_threshold', 'gpt.max_tokens', 'po.recent_check_days'
    
    -- Config Value (stored as string, can be JSON)
    ConfigValue NVARCHAR(MAX) NOT NULL,
    
    -- Config Type for validation
    ConfigType NVARCHAR(50) NOT NULL CHECK (ConfigType IN ('INTEGER', 'STRING', 'JSON', 'BOOLEAN', 'TIME', 'CRON', 'DECIMAL')),
    
    -- Category for grouping
    Category NVARCHAR(50) NOT NULL CHECK (Category IN ('STOCK_ALERT', 'DEMAND_FORECAST', 'PO_AUTO', 'SUPPLIER_MAPPING', 'GPT_SERVICE', 'NOTIFICATION')),
    
    -- Display Information
    DisplayName NVARCHAR(200) NOT NULL,
    Description NVARCHAR(500),
    
    -- Validation Constraints
    MinValue NVARCHAR(50) NULL, -- For numeric validation
    MaxValue NVARCHAR(50) NULL,
    DefaultValue NVARCHAR(MAX) NOT NULL,
    
    -- Status
    IsActive BIT DEFAULT 1,
    
    -- Audit Trail
    UpdatedBy UNIQUEIDENTIFIER NULL REFERENCES Users(UserID) ON DELETE NO ACTION,
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),
    CreatedAt DATETIME2 DEFAULT SYSDATETIME()
);
GO

-- Indexes for performance
CREATE INDEX IX_AIAgentConfigs_Category ON AIAgentConfigurations(Category);
CREATE INDEX IX_AIAgentConfigs_Key ON AIAgentConfigurations(ConfigKey);
CREATE INDEX IX_AIAgentConfigs_Active ON AIAgentConfigurations(IsActive);
GO

