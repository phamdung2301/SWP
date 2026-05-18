-- =====================================================
-- RESORT ERP EXPANSION - PART 2: HOUSEKEEPING
-- =====================================================
USE LiteFlowDBO;
GO

-- Housekeeping Tasks
CREATE TABLE HousekeepingTasks (
    TaskID      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    HotelRoomID UNIQUEIDENTIFIER NOT NULL,
    TaskType    NVARCHAR(50) NOT NULL DEFAULT 'CHECKOUT_CLEAN'
        CHECK (TaskType IN ('CHECKOUT_CLEAN','STAYOVER_CLEAN',
                            'DEEP_CLEAN','INSPECTION','TURNDOWN')),
    Status      NVARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (Status IN ('PENDING','ASSIGNED','IN_PROGRESS',
                          'INSPECTING','DONE','CANCELLED')),
    AssignedTo  UNIQUEIDENTIFIER NULL,  -- EmployeeID
    Priority    NVARCHAR(10) DEFAULT 'NORMAL'
        CHECK (Priority IN ('LOW','NORMAL','HIGH','URGENT')),
    ScheduledAt DATETIME2 NULL,
    StartedAt   DATETIME2 NULL,
    CompletedAt DATETIME2 NULL,
    Notes       NVARCHAR(MAX) NULL,
    CreatedBy   UNIQUEIDENTIFIER NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_HKT_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID),
    CONSTRAINT FK_HKT_Employee FOREIGN KEY (AssignedTo)
        REFERENCES Employees(EmployeeID) ON DELETE SET NULL,
    CONSTRAINT FK_HKT_Creator FOREIGN KEY (CreatedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- Cleaning Schedules
CREATE TABLE CleaningSchedules (
    ScheduleID  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    HotelRoomID UNIQUEIDENTIFIER NOT NULL,
    EmployeeID  UNIQUEIDENTIFIER NULL,
    ScheduledDate DATE NOT NULL,
    ShiftSlot   NVARCHAR(20) DEFAULT 'MORNING'
        CHECK (ShiftSlot IN ('MORNING','AFTERNOON','EVENING')),
    IsCompleted BIT DEFAULT 0,
    TaskID      UNIQUEIDENTIFIER NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_CS_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID),
    CONSTRAINT FK_CS_Employee FOREIGN KEY (EmployeeID)
        REFERENCES Employees(EmployeeID) ON DELETE SET NULL,
    CONSTRAINT FK_CS_Task FOREIGN KEY (TaskID)
        REFERENCES HousekeepingTasks(TaskID) ON DELETE SET NULL
);
GO

-- Room Inspection Checklist
CREATE TABLE RoomInspections (
    InspectionID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TaskID       UNIQUEIDENTIFIER NOT NULL,
    HotelRoomID  UNIQUEIDENTIFIER NOT NULL,
    InspectedBy  UNIQUEIDENTIFIER NULL,
    InspectedAt  DATETIME2 DEFAULT SYSDATETIME(),
    Result       NVARCHAR(10) DEFAULT 'PASS'
        CHECK (Result IN ('PASS','FAIL','PARTIAL')),
    ChecklistData NVARCHAR(MAX) NULL,  -- JSON checklist items
    Notes        NVARCHAR(MAX) NULL,
    CONSTRAINT FK_RI_Task FOREIGN KEY (TaskID)
        REFERENCES HousekeepingTasks(TaskID) ON DELETE CASCADE,
    CONSTRAINT FK_RI_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID),
    CONSTRAINT FK_RI_User FOREIGN KEY (InspectedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- Linen Inventory
CREATE TABLE LinenInventory (
    LinenID      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ItemName     NVARCHAR(100) NOT NULL,
    Category     NVARCHAR(50) NOT NULL,  -- SHEET, TOWEL, PILLOW_COVER, etc.
    TotalQty     INT NOT NULL DEFAULT 0,
    InUseQty     INT NOT NULL DEFAULT 0,
    InLaundryQty INT NOT NULL DEFAULT 0,
    DamagedQty   INT NOT NULL DEFAULT 0,
    MinStockQty  INT NOT NULL DEFAULT 10,
    UpdatedAt    DATETIME2 DEFAULT SYSDATETIME()
);
GO

-- Laundry Records
CREATE TABLE LaundryRecords (
    LaundryID   UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    HotelRoomID UNIQUEIDENTIFIER NULL,
    LinenID     UNIQUEIDENTIFIER NULL,
    Quantity    INT NOT NULL DEFAULT 1,
    Status      NVARCHAR(20) DEFAULT 'SENT'
        CHECK (Status IN ('SENT','IN_LAUNDRY','DONE','RETURNED')),
    SentAt      DATETIME2 DEFAULT SYSDATETIME(),
    ReturnedAt  DATETIME2 NULL,
    HandledBy   UNIQUEIDENTIFIER NULL,
    Notes       NVARCHAR(500) NULL,
    CONSTRAINT FK_LR_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID) ON DELETE SET NULL,
    CONSTRAINT FK_LR_Linen FOREIGN KEY (LinenID)
        REFERENCES LinenInventory(LinenID) ON DELETE SET NULL,
    CONSTRAINT FK_LR_User FOREIGN KEY (HandledBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- Lost & Found Items
CREATE TABLE LostFoundItems (
    ItemID      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    HotelRoomID UNIQUEIDENTIFIER NULL,
    Description NVARCHAR(500) NOT NULL,
    FoundDate   DATE NOT NULL,
    FoundBy     UNIQUEIDENTIFIER NULL,
    StorageLocation NVARCHAR(200) NULL,
    Status      NVARCHAR(20) DEFAULT 'STORED'
        CHECK (Status IN ('STORED','CLAIMED','DONATED','DISPOSED')),
    GuestID     UNIQUEIDENTIFIER NULL,
    ClaimedAt   DATETIME2 NULL,
    Notes       NVARCHAR(MAX) NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_LF_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID) ON DELETE SET NULL,
    CONSTRAINT FK_LF_Employee FOREIGN KEY (FoundBy)
        REFERENCES Users(UserID) ON DELETE SET NULL,
    CONSTRAINT FK_LF_Guest FOREIGN KEY (GuestID)
        REFERENCES GuestProfiles(GuestID) ON DELETE SET NULL
);
GO

-- Minibar Refills
CREATE TABLE MinibarRefills (
    RefillID    UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    HotelRoomID UNIQUEIDENTIFIER NOT NULL,
    TaskID      UNIQUEIDENTIFIER NULL,
    ItemName    NVARCHAR(100) NOT NULL,
    QtyConsumed INT DEFAULT 0,
    QtyRefilled INT DEFAULT 0,
    RefillDate  DATETIME2 DEFAULT SYSDATETIME(),
    HandledBy   UNIQUEIDENTIFIER NULL,
    ChargeAmount DECIMAL(10,2) DEFAULT 0,
    CONSTRAINT FK_MR_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID),
    CONSTRAINT FK_MR_Task FOREIGN KEY (TaskID)
        REFERENCES HousekeepingTasks(TaskID) ON DELETE SET NULL,
    CONSTRAINT FK_MR_User FOREIGN KEY (HandledBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- Indexes
CREATE INDEX IX_HKT_Room   ON HousekeepingTasks(HotelRoomID);
CREATE INDEX IX_HKT_Status ON HousekeepingTasks(Status);
CREATE INDEX IX_HKT_Assign ON HousekeepingTasks(AssignedTo);
CREATE INDEX IX_CS_Date    ON CleaningSchedules(ScheduledDate);
CREATE INDEX IX_LFI_Status ON LostFoundItems(Status);
GO
