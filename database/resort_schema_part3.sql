-- =====================================================
-- RESORT ERP EXPANSION - PART 3: MAINTENANCE & ASSETS
-- =====================================================
USE LiteFlowDBO;
GO

-- Asset Categories
CREATE TABLE AssetCategories (
    CategoryID  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Code        NVARCHAR(20)  NOT NULL UNIQUE,
    Name        NVARCHAR(100) NOT NULL,
    Description NVARCHAR(500) NULL,
    ParentID    UNIQUEIDENTIFIER NULL,
    CONSTRAINT FK_AC_Parent FOREIGN KEY (ParentID)
        REFERENCES AssetCategories(CategoryID)
);
GO

-- Assets
CREATE TABLE Assets (
    AssetID      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    AssetCode    NVARCHAR(30)  NOT NULL UNIQUE,
    Name         NVARCHAR(200) NOT NULL,
    CategoryID   UNIQUEIDENTIFIER NULL,
    BuildingID   UNIQUEIDENTIFIER NULL,
    HotelRoomID  UNIQUEIDENTIFIER NULL,
    SerialNumber NVARCHAR(100) NULL,
    Brand        NVARCHAR(100) NULL,
    Model        NVARCHAR(100) NULL,
    PurchaseDate DATE NULL,
    PurchaseCost DECIMAL(14,2) NULL,
    WarrantyExpiry DATE NULL,
    Status       NVARCHAR(20) DEFAULT 'ACTIVE'
        CHECK (Status IN ('ACTIVE','UNDER_MAINTENANCE','RETIRED','LOST')),
    Location     NVARCHAR(200) NULL,
    Notes        NVARCHAR(MAX) NULL,
    IsDeleted    BIT DEFAULT 0,
    CreatedAt    DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt    DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Asset_Category FOREIGN KEY (CategoryID)
        REFERENCES AssetCategories(CategoryID) ON DELETE SET NULL,
    CONSTRAINT FK_Asset_Building FOREIGN KEY (BuildingID)
        REFERENCES Buildings(BuildingID) ON DELETE SET NULL,
    CONSTRAINT FK_Asset_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID) ON DELETE SET NULL
);
GO

-- Maintenance Tickets
CREATE TABLE MaintenanceTickets (
    TicketID    UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TicketCode  NVARCHAR(20)  NOT NULL UNIQUE,
    Title       NVARCHAR(300) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    AssetID     UNIQUEIDENTIFIER NULL,
    HotelRoomID UNIQUEIDENTIFIER NULL,
    BuildingID  UNIQUEIDENTIFIER NULL,
    Priority    NVARCHAR(10) DEFAULT 'NORMAL'
        CHECK (Priority IN ('LOW','NORMAL','HIGH','CRITICAL')),
    Status      NVARCHAR(20) DEFAULT 'OPEN'
        CHECK (Status IN ('OPEN','IN_PROGRESS','WAITING_PART',
                          'DONE','CANCELLED')),
    ReportedBy  UNIQUEIDENTIFIER NULL,
    AssignedTo  UNIQUEIDENTIFIER NULL,  -- Technician/EmployeeID
    EstimatedCost DECIMAL(12,2) NULL,
    ActualCost    DECIMAL(12,2) NULL,
    ReportedAt  DATETIME2 DEFAULT SYSDATETIME(),
    StartedAt   DATETIME2 NULL,
    ResolvedAt  DATETIME2 NULL,
    Notes       NVARCHAR(MAX) NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_MT_Asset FOREIGN KEY (AssetID)
        REFERENCES Assets(AssetID) ON DELETE SET NULL,
    CONSTRAINT FK_MT_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID) ON DELETE SET NULL,
    CONSTRAINT FK_MT_Building FOREIGN KEY (BuildingID)
        REFERENCES Buildings(BuildingID) ON DELETE SET NULL,
    CONSTRAINT FK_MT_Reporter FOREIGN KEY (ReportedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL,
    CONSTRAINT FK_MT_Assignee FOREIGN KEY (AssignedTo)
        REFERENCES Employees(EmployeeID) ON DELETE SET NULL
);
GO

-- Work Orders (detail steps for a ticket)
CREATE TABLE WorkOrders (
    WorkOrderID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TicketID    UNIQUEIDENTIFIER NOT NULL,
    Description NVARCHAR(MAX) NOT NULL,
    AssignedTo  UNIQUEIDENTIFIER NULL,
    Status      NVARCHAR(20) DEFAULT 'PENDING'
        CHECK (Status IN ('PENDING','IN_PROGRESS','DONE','CANCELLED')),
    PartsUsed   NVARCHAR(MAX) NULL,  -- JSON list of parts
    LaborHours  DECIMAL(6,2) NULL,
    Cost        DECIMAL(12,2) NULL,
    StartedAt   DATETIME2 NULL,
    CompletedAt DATETIME2 NULL,
    Notes       NVARCHAR(MAX) NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_WO_Ticket FOREIGN KEY (TicketID)
        REFERENCES MaintenanceTickets(TicketID) ON DELETE CASCADE,
    CONSTRAINT FK_WO_Employee FOREIGN KEY (AssignedTo)
        REFERENCES Employees(EmployeeID) ON DELETE SET NULL
);
GO

-- Maintenance Schedules (Preventive)
CREATE TABLE MaintenanceSchedules (
    ScheduleID  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    AssetID     UNIQUEIDENTIFIER NULL,
    Title       NVARCHAR(200) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    Frequency   NVARCHAR(20) NOT NULL  -- DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
        CHECK (Frequency IN ('DAILY','WEEKLY','MONTHLY','QUARTERLY','YEARLY')),
    NextDueDate DATE NOT NULL,
    LastDoneAt  DATETIME2 NULL,
    AssignedTo  UNIQUEIDENTIFIER NULL,
    IsActive    BIT DEFAULT 1,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_MS_Asset FOREIGN KEY (AssetID)
        REFERENCES Assets(AssetID) ON DELETE SET NULL,
    CONSTRAINT FK_MS_Employee FOREIGN KEY (AssignedTo)
        REFERENCES Employees(EmployeeID) ON DELETE SET NULL
);
GO

-- Inspection Logs
CREATE TABLE InspectionLogs (
    LogID        UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ScheduleID   UNIQUEIDENTIFIER NULL,
    AssetID      UNIQUEIDENTIFIER NULL,
    InspectedBy  UNIQUEIDENTIFIER NULL,
    InspectedAt  DATETIME2 DEFAULT SYSDATETIME(),
    Result       NVARCHAR(10) DEFAULT 'PASS'
        CHECK (Result IN ('PASS','FAIL','NEEDS_REPAIR')),
    Findings     NVARCHAR(MAX) NULL,
    ActionTaken  NVARCHAR(MAX) NULL,
    CONSTRAINT FK_IL_Schedule FOREIGN KEY (ScheduleID)
        REFERENCES MaintenanceSchedules(ScheduleID) ON DELETE SET NULL,
    CONSTRAINT FK_IL_Asset FOREIGN KEY (AssetID)
        REFERENCES Assets(AssetID) ON DELETE SET NULL,
    CONSTRAINT FK_IL_User FOREIGN KEY (InspectedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- Indexes
CREATE INDEX IX_Assets_Status   ON Assets(Status);
CREATE INDEX IX_Assets_Building ON Assets(BuildingID);
CREATE INDEX IX_MT_Status       ON MaintenanceTickets(Status);
CREATE INDEX IX_MT_Priority     ON MaintenanceTickets(Priority);
CREATE INDEX IX_MT_Asset        ON MaintenanceTickets(AssetID);
CREATE INDEX IX_MS_NextDue      ON MaintenanceSchedules(NextDueDate);
GO
