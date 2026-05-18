-- =====================================================
-- RESORT ERP EXPANSION - PART 4: MULTI-WAREHOUSE, RECIPE BOM
-- =====================================================
USE LiteFlowDBO;
GO

-- Warehouses
CREATE TABLE Warehouses (
    WarehouseID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Code        NVARCHAR(20)  NOT NULL UNIQUE,
    Name        NVARCHAR(200) NOT NULL,
    Type        NVARCHAR(50)  NOT NULL DEFAULT 'MAIN'
        CHECK (Type IN ('MAIN','KITCHEN','BAR','HOUSEKEEPING',
                        'ENGINEERING','MINIBAR','GENERAL')),
    BuildingID  UNIQUEIDENTIFIER NULL,
    Location    NVARCHAR(200) NULL,
    ManagerID   UNIQUEIDENTIFIER NULL,
    IsActive    BIT DEFAULT 1,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_WH_Building FOREIGN KEY (BuildingID)
        REFERENCES Buildings(BuildingID) ON DELETE SET NULL,
    CONSTRAINT FK_WH_Manager FOREIGN KEY (ManagerID)
        REFERENCES Employees(EmployeeID) ON DELETE SET NULL
);
GO

-- Warehouse Zones
CREATE TABLE WarehouseZones (
    ZoneID      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    WarehouseID UNIQUEIDENTIFIER NOT NULL,
    Code        NVARCHAR(20)  NOT NULL,
    Name        NVARCHAR(100) NOT NULL,
    ZoneType    NVARCHAR(50)  NULL,  -- COLD, DRY, FROZEN, AMBIENT
    CONSTRAINT FK_WZ_Warehouse FOREIGN KEY (WarehouseID)
        REFERENCES Warehouses(WarehouseID) ON DELETE CASCADE,
    CONSTRAINT UX_WZ_Code UNIQUE (WarehouseID, Code)
);
GO

-- Stock Batches (for FIFO / FEFO)
CREATE TABLE StockBatches (
    BatchID         UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    WarehouseID     UNIQUEIDENTIFIER NOT NULL,
    ZoneID          UNIQUEIDENTIFIER NULL,
    BatchNumber     NVARCHAR(50)  NULL,
    Quantity        INT NOT NULL DEFAULT 0,
    UnitCost        DECIMAL(14,4) NULL,
    ManufactureDate DATE NULL,
    ExpirationDate  DATE NULL,
    ReceivedAt      DATETIME2 DEFAULT SYSDATETIME(),
    IsExhausted     BIT DEFAULT 0,
    CONSTRAINT FK_SB_Variant FOREIGN KEY (ProductVariantID)
        REFERENCES ProductVariant(ProductVariantID),
    CONSTRAINT FK_SB_Warehouse FOREIGN KEY (WarehouseID)
        REFERENCES Warehouses(WarehouseID),
    CONSTRAINT FK_SB_Zone FOREIGN KEY (ZoneID)
        REFERENCES WarehouseZones(ZoneID) ON DELETE SET NULL
);
GO

-- Stock Transfers between warehouses
CREATE TABLE StockTransfers (
    TransferID      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TransferCode    NVARCHAR(20) NOT NULL UNIQUE,
    FromWarehouseID UNIQUEIDENTIFIER NOT NULL,
    ToWarehouseID   UNIQUEIDENTIFIER NOT NULL,
    Status          NVARCHAR(20) DEFAULT 'DRAFT'
        CHECK (Status IN ('DRAFT','PENDING','IN_TRANSIT','COMPLETED','CANCELLED')),
    RequestedBy     UNIQUEIDENTIFIER NULL,
    ApprovedBy      UNIQUEIDENTIFIER NULL,
    TransferDate    DATETIME2 NULL,
    Notes           NVARCHAR(MAX) NULL,
    CreatedAt       DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_ST_From FOREIGN KEY (FromWarehouseID)
        REFERENCES Warehouses(WarehouseID),
    CONSTRAINT FK_ST_To FOREIGN KEY (ToWarehouseID)
        REFERENCES Warehouses(WarehouseID),
    CONSTRAINT FK_ST_Requester FOREIGN KEY (RequestedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL,
    CONSTRAINT FK_ST_Approver FOREIGN KEY (ApprovedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL,
    CONSTRAINT CK_ST_DiffWH CHECK (FromWarehouseID <> ToWarehouseID)
);
GO

-- Stock Transfer Items
CREATE TABLE StockTransferItems (
    ItemID           UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TransferID       UNIQUEIDENTIFIER NOT NULL,
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    BatchID          UNIQUEIDENTIFIER NULL,
    QuantityRequested INT NOT NULL DEFAULT 1,
    QuantityActual    INT NULL,
    CONSTRAINT FK_STI_Transfer FOREIGN KEY (TransferID)
        REFERENCES StockTransfers(TransferID) ON DELETE CASCADE,
    CONSTRAINT FK_STI_Variant FOREIGN KEY (ProductVariantID)
        REFERENCES ProductVariant(ProductVariantID),
    CONSTRAINT FK_STI_Batch FOREIGN KEY (BatchID)
        REFERENCES StockBatches(BatchID) ON DELETE SET NULL
);
GO

-- Waste / Spoilage Records
CREATE TABLE WasteRecords (
    WasteID         UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    WarehouseID     UNIQUEIDENTIFIER NOT NULL,
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    BatchID         UNIQUEIDENTIFIER NULL,
    Quantity        INT NOT NULL DEFAULT 1,
    Reason          NVARCHAR(50) NOT NULL  -- EXPIRED, DAMAGED, SPOILED, THEFT
        CHECK (Reason IN ('EXPIRED','DAMAGED','SPOILED','THEFT','OTHER')),
    WasteDate       DATE NOT NULL,
    EstimatedLoss   DECIMAL(12,2) NULL,
    RecordedBy      UNIQUEIDENTIFIER NULL,
    Notes           NVARCHAR(500) NULL,
    CreatedAt       DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_WR_Warehouse FOREIGN KEY (WarehouseID)
        REFERENCES Warehouses(WarehouseID),
    CONSTRAINT FK_WR_Variant FOREIGN KEY (ProductVariantID)
        REFERENCES ProductVariant(ProductVariantID),
    CONSTRAINT FK_WR_Batch FOREIGN KEY (BatchID)
        REFERENCES StockBatches(BatchID) ON DELETE SET NULL,
    CONSTRAINT FK_WR_User FOREIGN KEY (RecordedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- Recipes (Bill of Materials)
CREATE TABLE Recipes (
    RecipeID    UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ProductID   UNIQUEIDENTIFIER NOT NULL UNIQUE,
    Name        NVARCHAR(200) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    Yield       DECIMAL(10,4) NOT NULL DEFAULT 1,  -- how many portions
    YieldUnit   NVARCHAR(30)  NOT NULL DEFAULT 'portion',
    IsActive    BIT DEFAULT 1,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Recipe_Product FOREIGN KEY (ProductID)
        REFERENCES Products(ProductID) ON DELETE CASCADE
);
GO

-- Recipe Ingredients (BOM Lines)
CREATE TABLE RecipeIngredients (
    IngredientID    UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    RecipeID        UNIQUEIDENTIFIER NOT NULL,
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    Quantity        DECIMAL(12,4) NOT NULL,
    Unit            NVARCHAR(30)  NOT NULL,
    IsOptional      BIT DEFAULT 0,
    Notes           NVARCHAR(200) NULL,
    CONSTRAINT FK_RI_Recipe FOREIGN KEY (RecipeID)
        REFERENCES Recipes(RecipeID) ON DELETE CASCADE,
    CONSTRAINT FK_RI_Variant FOREIGN KEY (ProductVariantID)
        REFERENCES ProductVariant(ProductVariantID)
);
GO

-- Inventory Audit
CREATE TABLE InventoryAudits (
    AuditID     UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    WarehouseID UNIQUEIDENTIFIER NOT NULL,
    AuditDate   DATE NOT NULL,
    Status      NVARCHAR(20) DEFAULT 'DRAFT'
        CHECK (Status IN ('DRAFT','IN_PROGRESS','COMPLETED','CANCELLED')),
    ConductedBy UNIQUEIDENTIFIER NULL,
    Notes       NVARCHAR(MAX) NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_IA_Warehouse FOREIGN KEY (WarehouseID)
        REFERENCES Warehouses(WarehouseID),
    CONSTRAINT FK_IA_User FOREIGN KEY (ConductedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

CREATE TABLE InventoryAuditItems (
    AuditItemID      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    AuditID          UNIQUEIDENTIFIER NOT NULL,
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    SystemQty        INT NOT NULL DEFAULT 0,
    CountedQty       INT NOT NULL DEFAULT 0,
    Variance         AS (CountedQty - SystemQty) PERSISTED,
    Notes            NVARCHAR(200) NULL,
    CONSTRAINT FK_IAI_Audit FOREIGN KEY (AuditID)
        REFERENCES InventoryAudits(AuditID) ON DELETE CASCADE,
    CONSTRAINT FK_IAI_Variant FOREIGN KEY (ProductVariantID)
        REFERENCES ProductVariant(ProductVariantID)
);
GO

-- Indexes
CREATE INDEX IX_StockBatch_Expiry    ON StockBatches(ExpirationDate);
CREATE INDEX IX_StockBatch_Warehouse ON StockBatches(WarehouseID);
CREATE INDEX IX_StockBatch_Variant   ON StockBatches(ProductVariantID);
CREATE INDEX IX_WasteRecords_Date    ON WasteRecords(WasteDate);
CREATE INDEX IX_StockTransfer_Status ON StockTransfers(Status);
GO
