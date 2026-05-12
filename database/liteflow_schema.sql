USE master;
GO

IF DB_ID('LiteFlowDBO') IS NOT NULL
BEGIN
    ALTER DATABASE LiteFlowDBO SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE LiteFlowDBO;
END
GO
CREATE DATABASE LiteFlowDBO;
GO
USE LiteFlowDBO;
GO

-- =======================================================
-- 1. AUTHENTICATION & AUTHORIZATION
-- =======================================================

-- USERS
CREATE TABLE Users (
    UserID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Email NVARCHAR(320) NOT NULL UNIQUE,
    Phone NVARCHAR(32) NULL,
    GoogleID NVARCHAR(200) NULL,
    PasswordHash NVARCHAR(MAX) NOT NULL,
    TwoFactorSecret NVARCHAR(200) NULL,
    DisplayName NVARCHAR(200),
    IsActive BIT DEFAULT 1,
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),
    Meta NVARCHAR(MAX),
    Last2FAVerifiedAt DATETIME2 NULL
);
GO
CREATE INDEX IX_Users_Email ON Users(Email);
CREATE UNIQUE INDEX UX_Users_Phone_NotNull ON Users(Phone) WHERE Phone IS NOT NULL;
GO

-- ROLES
CREATE TABLE Roles (
    RoleID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Name NVARCHAR(100) UNIQUE,
    Description NVARCHAR(500)
);
GO

-- USER ROLES
CREATE TABLE UserRoles (
    UserID UNIQUEIDENTIFIER NOT NULL,
    RoleID UNIQUEIDENTIFIER NOT NULL,
    AssignedAt DATETIME2 DEFAULT SYSDATETIME(),
    AssignedBy UNIQUEIDENTIFIER NULL,
    IsActive BIT DEFAULT 1,
    PRIMARY KEY(UserID, RoleID),
    CONSTRAINT FK_UserRoles_User FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    CONSTRAINT FK_UserRoles_Role FOREIGN KEY (RoleID) REFERENCES Roles(RoleID) ON DELETE CASCADE
);
GO

-- USER SESSIONS
CREATE TABLE UserSessions (
    SessionID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserID UNIQUEIDENTIFIER NOT NULL,
    JWT NVARCHAR(MAX),
    DeviceInfo NVARCHAR(500),
    IPAddress NVARCHAR(50),
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    ExpiresAt DATETIME2,
    Revoked BIT DEFAULT 0,
    Last2faVerifiedAt DATETIME2 NULL,
    Action NVARCHAR(200),
    ObjectType NVARCHAR(100),
    ObjectID UNIQUEIDENTIFIER NULL,
    Details NVARCHAR(MAX),
    IPAddressAction NVARCHAR(50),
    CreatedAtAction DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_UserSessions_User FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);
GO
CREATE INDEX IX_UserSessions_UserID ON UserSessions(UserID);
GO

-- OTP TOKENS
CREATE TABLE OtpTokens (
    OtpID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserID UNIQUEIDENTIFIER NOT NULL,
    Code NVARCHAR(6) NOT NULL,
    ExpiresAt DATETIME2 NOT NULL,
    Used BIT DEFAULT 0,
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    IPAddress NVARCHAR(50),
	TargetEmail VARCHAR(320) NULL,
    CONSTRAINT FK_OtpTokens_User FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);
GO
CREATE INDEX IX_OtpTokens_UserID ON OtpTokens(UserID);
CREATE INDEX IX_OtpTokens_Expiry ON OtpTokens(ExpiresAt, Used);
GO

-- AUDIT LOGS
CREATE TABLE AuditLogs (
    AuditID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserID UNIQUEIDENTIFIER NULL,
    Action NVARCHAR(200) NOT NULL,
    ObjectType NVARCHAR(100) NOT NULL,
    ObjectID NVARCHAR(36) NULL,
    Details NVARCHAR(MAX) NULL,
    IPAddress NVARCHAR(50) NULL,
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_AuditLogs_User FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL
);
GO
CREATE INDEX IX_AuditLogs_UserID ON AuditLogs(UserID);
CREATE INDEX IX_AuditLogs_Action ON AuditLogs(Action);
CREATE INDEX IX_AuditLogs_ObjectType ON AuditLogs(ObjectType);
CREATE INDEX IX_AuditLogs_CreatedAt ON AuditLogs(CreatedAt);
GO

-- TRIGGER cleanup OTP
CREATE OR ALTER TRIGGER TRG_Cleanup_OtpTokens
ON OtpTokens
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM OtpTokens
    WHERE ExpiresAt < SYSDATETIME() OR Used = 1;
END;
GO

-- =======================================================
-- 2. PRODUCT & CATEGORY
-- =======================================================
CREATE TABLE Categories (
    CategoryID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(MAX)
);

CREATE TABLE Products (
    ProductID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(MAX),
    ImageURL NVARCHAR(MAX),
    ImportDate DATETIME2 DEFAULT SYSDATETIME(),
    IsDeleted BIT NOT NULL DEFAULT 0,
    ProductType NVARCHAR(50) NULL,
    Status NVARCHAR(50) DEFAULT N'Đang bán',
    Unit NVARCHAR(50) NULL
);

CREATE TABLE ProductVariant (
    ProductVariantID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ProductID UNIQUEIDENTIFIER NOT NULL,
    Size NVARCHAR(50) NOT NULL,
    OriginalPrice DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    Price DECIMAL(10,2) NOT NULL,
    DiscountPrice DECIMAL(10,2) DEFAULT NULL,
    DiscountExpiry DATETIME2,
    IsDeleted BIT DEFAULT 0,
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE CASCADE
);

CREATE TABLE ProductsCategories (
    ProductCategoryID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ProductID UNIQUEIDENTIFIER NOT NULL,
    CategoryID UNIQUEIDENTIFIER NOT NULL,
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE CASCADE,
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID) ON DELETE CASCADE
);

-- =======================================================
-- 3. INVENTORY
-- =======================================================
CREATE TABLE Inventory (
    InventoryID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    StoreLocation NVARCHAR(100) DEFAULT 'Main Warehouse'
);

CREATE TABLE ProductStock (
    ProductStockID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    InventoryID UNIQUEIDENTIFIER NOT NULL,
    Amount INT NOT NULL DEFAULT 0,
    FOREIGN KEY (ProductVariantID) REFERENCES ProductVariant(ProductVariantID) ON DELETE CASCADE,
    FOREIGN KEY (InventoryID) REFERENCES Inventory(InventoryID) ON DELETE CASCADE
);

CREATE TABLE InventoryLogs (
    LogID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    ActionType NVARCHAR(10) NOT NULL,
    QuantityChanged INT NOT NULL,
    ActionDate DATETIME2 DEFAULT SYSDATETIME(),
    StoreLocation NVARCHAR(100) DEFAULT 'Main Warehouse',
    FOREIGN KEY (ProductVariantID) REFERENCES ProductVariant(ProductVariantID)
);

-- =======================================================
-- 4. ROOMS & TABLES (Must be created before TableSessions)
-- =======================================================
CREATE TABLE Rooms (
    RoomID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(500),
    TableCount INT DEFAULT 0,           -- Số lượng bàn
    TotalCapacity INT DEFAULT 0,        -- Tổng sức chứa
    CreatedAt DATETIME2 DEFAULT SYSDATETIME()
);
CREATE TABLE Tables (
    TableID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    RoomID UNIQUEIDENTIFIER NULL,
    TableNumber NVARCHAR(50) NOT NULL,
    TableName NVARCHAR(100) NOT NULL,  -- Tên hiển thị (vd: "Bàn 1", "Bàn VIP")
    Capacity INT NOT NULL DEFAULT 4,   -- Sức chứa
    Status NVARCHAR(50) DEFAULT 'Available' CHECK (Status IN ('Available', 'Occupied', 'Reserved', 'Maintenance')),
    IsActive BIT DEFAULT 1,            -- Bàn có hoạt động không
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),
    
    CONSTRAINT FK_Tables_Room FOREIGN KEY (RoomID) REFERENCES Rooms(RoomID) ON DELETE SET NULL
);

-- =======================================================
-- 4.1 RESERVATIONS (Đặt bàn trước)
-- =======================================================

-- RESERVATIONS - Quản lý đặt bàn trước của khách hàng
CREATE TABLE Reservations (
    ReservationID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ReservationCode NVARCHAR(20) NOT NULL UNIQUE,
    CustomerName NVARCHAR(100) NOT NULL,
    CustomerPhone NVARCHAR(20) NOT NULL,
    CustomerEmail NVARCHAR(100) NULL,
    ArrivalTime DATETIME2 NOT NULL,
    NumberOfGuests INT NOT NULL CHECK (NumberOfGuests > 0),
    TableID UNIQUEIDENTIFIER NULL,
    RoomID UNIQUEIDENTIFIER NULL,
    
    Status NVARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (Status IN ('PENDING', 'CONFIRMED', 'SEATED', 'CANCELLED', 'NO_SHOW', 'CLOSED')),
    Notes NVARCHAR(MAX),
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),
    
    CONSTRAINT FK_Reservations_Tables FOREIGN KEY (TableID) REFERENCES Tables(TableID) ON DELETE SET NULL,
    CONSTRAINT FK_Reservations_Rooms FOREIGN KEY (RoomID) REFERENCES Rooms(RoomID) ON DELETE SET NULL
);

-- RESERVATION ITEMS - Món đặt trước cho đặt bàn
CREATE TABLE ReservationItems (
    ReservationItemID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ReservationID UNIQUEIDENTIFIER NOT NULL,
    ProductID UNIQUEIDENTIFIER NOT NULL,
    Quantity INT NOT NULL DEFAULT 1 CHECK (Quantity > 0),
    Note NVARCHAR(255),
    
    CONSTRAINT FK_ReservationItems_Reservations FOREIGN KEY (ReservationID) 
        REFERENCES Reservations(ReservationID) ON DELETE CASCADE,
    CONSTRAINT FK_ReservationItems_Products FOREIGN KEY (ProductID) 
        REFERENCES Products(ProductID) ON DELETE CASCADE
);

-- Indexes for Reservations
CREATE INDEX IX_Reservations_ArrivalTime ON Reservations(ArrivalTime);
CREATE INDEX IX_Reservations_Status ON Reservations(Status);
CREATE INDEX IX_Reservations_ReservationCode ON Reservations(ReservationCode);
CREATE INDEX IX_Reservations_CustomerPhone ON Reservations(CustomerPhone);
CREATE INDEX IX_Reservations_CustomerEmail ON Reservations(CustomerEmail);
CREATE INDEX IX_Reservations_TableID ON Reservations(TableID);
CREATE INDEX IX_Reservations_RoomID ON Reservations(RoomID);
CREATE INDEX IX_Reservations_CreatedAt ON Reservations(CreatedAt);

CREATE INDEX IX_ReservationItems_ReservationID ON ReservationItems(ReservationID);
CREATE INDEX IX_ReservationItems_ProductID ON ReservationItems(ProductID);
GO

-- Trigger: Auto-update UpdatedAt timestamp for Reservations
CREATE OR ALTER TRIGGER TRG_Reservations_UpdatedAt
ON Reservations
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    UPDATE Reservations
    SET UpdatedAt = SYSDATETIME()
    FROM Reservations r
    INNER JOIN inserted i ON r.ReservationID = i.ReservationID;
END;
GO

-- =======================================================
-- 5. CAFE MANAGEMENT SYSTEM
-- =======================================================

-- TABLE SESSIONS - Quản lý phiên làm việc của từng bàn
CREATE TABLE TableSessions (
    SessionID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TableID UNIQUEIDENTIFIER NULL,  -- ✅ Cho phép NULL với bàn đặc biệt (Mang về, Giao hàng)
    CustomerName NVARCHAR(200) NULL,  -- Tên khách hàng (tùy chọn)
    CustomerPhone NVARCHAR(20) NULL,  -- SĐT khách hàng (tùy chọn)
    CheckInTime DATETIME2 DEFAULT SYSDATETIME(),  -- Thời gian vào
    CheckOutTime DATETIME2 NULL,      -- Thời gian ra
    Status NVARCHAR(50) DEFAULT 'Active' CHECK (Status IN ('Active', 'Completed', 'Cancelled')),
    TotalAmount DECIMAL(10,2) DEFAULT 0.00,  -- Tổng tiền của phiên
    PaymentMethod NVARCHAR(50) NULL,  -- Phương thức thanh toán
    PaymentStatus NVARCHAR(50) DEFAULT 'Unpaid' CHECK (PaymentStatus IN ('Unpaid', 'Paid', 'Partial')),
    InvoiceName NVARCHAR(100) NULL,   -- Tên hóa đơn (vd: "Bàn 1 - HD 1")
    Notes NVARCHAR(MAX) NULL,         -- Ghi chú
    CreatedBy UNIQUEIDENTIFIER NULL,  -- Nhân viên tạo phiên
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),
    
    CONSTRAINT FK_TableSessions_Table FOREIGN KEY (TableID) REFERENCES Tables(TableID) ON DELETE CASCADE,
    CONSTRAINT FK_TableSessions_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(UserID) ON DELETE SET NULL
);

-- ORDERS - Đơn hàng trong phiên (có thể có nhiều đơn trong 1 phiên)
CREATE TABLE Orders (
    OrderID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    SessionID UNIQUEIDENTIFIER NOT NULL,  -- Liên kết với phiên bàn
    OrderNumber NVARCHAR(50) NOT NULL,    -- Số đơn hàng (vd: ORD001, ORD002)
    OrderDate DATETIME2 DEFAULT SYSDATETIME(),
    SubTotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    VAT DECIMAL(10,2) DEFAULT 0.00,       -- Thuế VAT
    Discount DECIMAL(10,2) DEFAULT 0.00,  -- Giảm giá
    TotalAmount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    Status NVARCHAR(50) DEFAULT 'Pending' CHECK (Status IN ('Pending', 'Preparing', 'Ready', 'Served', 'Cancelled')),
    PaymentMethod NVARCHAR(50) NULL,
    PaymentStatus NVARCHAR(50) DEFAULT 'Unpaid' CHECK (PaymentStatus IN ('Unpaid', 'Paid')),
    Notes NVARCHAR(MAX) NULL,
    CreatedBy UNIQUEIDENTIFIER NULL,
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),
    
    CONSTRAINT FK_Orders_Session FOREIGN KEY (SessionID) REFERENCES TableSessions(SessionID) ON DELETE CASCADE,
    CONSTRAINT FK_Orders_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(UserID) ON DELETE SET NULL
);

-- ORDER DETAILS - Chi tiết món trong đơn hàng
CREATE TABLE OrderDetails (
    OrderDetailID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    OrderID UNIQUEIDENTIFIER NOT NULL,
    ProductVariantID UNIQUEIDENTIFIER NOT NULL,
    Quantity INT NOT NULL DEFAULT 1,
    UnitPrice DECIMAL(10,2) NOT NULL,     -- Giá đơn vị
    TotalPrice DECIMAL(10,2) NOT NULL,    -- Tổng tiền = UnitPrice * Quantity
    SpecialInstructions NVARCHAR(500) NULL, -- Yêu cầu đặc biệt (ít đường, không đá, etc.)
    Status NVARCHAR(50) DEFAULT 'Pending' CHECK (Status IN ('Pending', 'Preparing', 'Ready', 'Served')),
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),
    
    CONSTRAINT FK_OrderDetails_Order FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE,
    CONSTRAINT FK_OrderDetails_ProductVariant FOREIGN KEY (ProductVariantID) REFERENCES ProductVariant(ProductVariantID)
);

-- ORDER STATUS HISTORY - Lịch sử thay đổi trạng thái đơn hàng (dùng cho thông báo kitchen)
CREATE TABLE OrderStatusHistory (
    HistoryID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    OrderID UNIQUEIDENTIFIER NOT NULL,
    OldStatus NVARCHAR(50) NOT NULL,
    NewStatus NVARCHAR(50) NOT NULL,
    ChangedAt DATETIME2 DEFAULT SYSDATETIME(),
    ChangedBy UNIQUEIDENTIFIER NULL,      -- Nhân viên thay đổi trạng thái
    Notes NVARCHAR(MAX) NULL,             -- Ghi chú khi thay đổi
    OrderDetailsSnapshot NVARCHAR(MAX) NULL,  -- Snapshot của order details (JSON) tại thời điểm thay đổi
    
    CONSTRAINT FK_OrderStatusHistory_Order FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE,
    CONSTRAINT FK_OrderStatusHistory_ChangedBy FOREIGN KEY (ChangedBy) REFERENCES Users(UserID) ON DELETE SET NULL
);

-- =======================================================
-- 5. USER INTERACTIONS
-- =======================================================
CREATE TABLE UserInteractions (
    InteractionID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserID UNIQUEIDENTIFIER NOT NULL,
    ProductID UNIQUEIDENTIFIER NOT NULL,
    InteractionType NVARCHAR(50) NOT NULL,
    InteractionTime DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);

-- =======================================================
-- 6. PAYMENT TRANSACTIONS
-- =======================================================
CREATE TABLE PaymentTransactions (
    TransactionID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    SessionID UNIQUEIDENTIFIER NOT NULL,
    OrderID UNIQUEIDENTIFIER NULL,  -- NULL nếu thanh toán toàn bộ phiên
    Amount DECIMAL(10,2) NOT NULL,
    PaymentMethod NVARCHAR(50) NOT NULL CHECK (PaymentMethod IN ('Cash', 'Card', 'Transfer', 'Wallet', 'VNPay')),
    PaymentStatus NVARCHAR(50) DEFAULT 'Completed' CHECK (PaymentStatus IN ('Pending', 'Processing', 'Completed', 'Failed', 'Refunded', 'Cancelled')),
    TransactionReference NVARCHAR(200) NULL,  -- Mã giao dịch từ hệ thống thanh toán
    VNPayTransactionNo NVARCHAR(50) NULL,  -- Mã giao dịch từ VNPay
    VNPayResponseCode NVARCHAR(10) NULL,  -- Mã phản hồi từ VNPay
    Notes NVARCHAR(500) NULL,
    ProcessedBy UNIQUEIDENTIFIER NULL,  -- Nhân viên xử lý thanh toán
    ProcessedAt DATETIME2 DEFAULT SYSDATETIME(),
    
    CONSTRAINT FK_PaymentTransactions_Session FOREIGN KEY (SessionID) REFERENCES TableSessions(SessionID) ON DELETE NO ACTION,
    CONSTRAINT FK_PaymentTransactions_Order FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE NO ACTION,
    CONSTRAINT FK_PaymentTransactions_ProcessedBy FOREIGN KEY (ProcessedBy) REFERENCES Users(UserID) ON DELETE SET NULL
);

-- =======================================================
-- 7. INDEXES
-- =======================================================
CREATE INDEX IX_Users_IsActive ON Users(IsActive);

CREATE INDEX IX_Products_Name ON Products(Name);
CREATE INDEX IX_Products_IsDeleted ON Products(IsDeleted);

CREATE INDEX IX_ProductVariant_ProductID ON ProductVariant(ProductID);
CREATE INDEX IX_ProductVariant_Price ON ProductVariant(Price);

-- Cafe Management Indexes
CREATE INDEX IX_TableSessions_TableID ON TableSessions(TableID);
CREATE INDEX IX_TableSessions_Status ON TableSessions(Status);
CREATE INDEX IX_TableSessions_CheckInTime ON TableSessions(CheckInTime);
CREATE INDEX IX_TableSessions_CheckOutTime ON TableSessions(CheckOutTime);

CREATE INDEX IX_Orders_SessionID ON Orders(SessionID);
CREATE INDEX IX_Orders_OrderNumber ON Orders(OrderNumber);
CREATE INDEX IX_Orders_OrderDate ON Orders(OrderDate);
CREATE INDEX IX_Orders_Status ON Orders(Status);
CREATE INDEX IX_Orders_PaymentStatus ON Orders(PaymentStatus);

CREATE INDEX IX_OrderDetails_OrderID ON OrderDetails(OrderID);
CREATE INDEX IX_OrderDetails_ProductVariantID ON OrderDetails(ProductVariantID);
CREATE INDEX IX_OrderDetails_Status ON OrderDetails(Status);

CREATE INDEX IX_OrderStatusHistory_OrderID ON OrderStatusHistory(OrderID);
CREATE INDEX IX_OrderStatusHistory_ChangedAt ON OrderStatusHistory(ChangedAt);
CREATE INDEX IX_OrderStatusHistory_NewStatus ON OrderStatusHistory(NewStatus);

CREATE INDEX IX_PaymentTransactions_SessionID ON PaymentTransactions(SessionID);
CREATE INDEX IX_PaymentTransactions_OrderID ON PaymentTransactions(OrderID);
CREATE INDEX IX_PaymentTransactions_ProcessedAt ON PaymentTransactions(ProcessedAt);

CREATE INDEX IX_Rooms_Name ON Rooms(Name);
CREATE INDEX IX_Tables_RoomID ON Tables(RoomID);
CREATE INDEX IX_Tables_Status ON Tables(Status);
CREATE INDEX IX_Tables_IsActive ON Tables(IsActive);
GO

-- =======================================================
-- TRIGGER: Tự động lưu lịch sử khi trạng thái Order thay đổi
-- =======================================================
CREATE OR ALTER TRIGGER TRG_Orders_StatusChange
ON Orders
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Chỉ insert vào OrderStatusHistory khi Status thay đổi
    INSERT INTO OrderStatusHistory (OrderID, OldStatus, NewStatus, ChangedBy, ChangedAt, OrderDetailsSnapshot)
    SELECT 
        i.OrderID,
        d.Status AS OldStatus,
        i.Status AS NewStatus,
        i.CreatedBy AS ChangedBy,
        SYSDATETIME() AS ChangedAt,
        (
            -- Tạo JSON snapshot của OrderDetails
            SELECT 
                p.Name AS productName,
                pv.Size AS size,
                od.Quantity AS quantity,
                od.UnitPrice AS unitPrice,
                od.SpecialInstructions AS note
            FROM OrderDetails od
            INNER JOIN ProductVariant pv ON od.ProductVariantID = pv.ProductVariantID
            INNER JOIN Products p ON pv.ProductID = p.ProductID
            WHERE od.OrderID = i.OrderID
            FOR JSON PATH
        ) AS OrderDetailsSnapshot
    FROM inserted i
    INNER JOIN deleted d ON i.OrderID = d.OrderID
    WHERE i.Status <> d.Status;  -- Chỉ insert khi Status thay đổi
END;
GO

USE LiteFlowDBO;
GO

-- =======================================================
-- 8. EMPLOYEES (Liên kết với bảng Users)
-- =======================================================

CREATE TABLE Employees (
    EmployeeID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserID UNIQUEIDENTIFIER NOT NULL UNIQUE,  -- Liên kết 1-1 với Users
    EmployeeCode NVARCHAR(50) NOT NULL UNIQUE,  -- Mã số nhân viên (vd: EMP001)
    FullName NVARCHAR(200) NOT NULL,
    Gender NVARCHAR(10) CHECK (Gender IN (N'Nam', N'Nữ', N'Khác')),
    BirthDate DATE NULL,
    NationalID NVARCHAR(20) NULL,  -- Số CCCD/CMND
    Phone NVARCHAR(20) NULL,
    Email NVARCHAR(320) NULL,      -- Phụ, có thể khác Email user
    Address NVARCHAR(500) NULL,
    AvatarURL NVARCHAR(MAX) NULL,
    HireDate DATETIME2 DEFAULT SYSDATETIME(),
    TerminationDate DATETIME2 NULL,
    EmploymentStatus NVARCHAR(50) DEFAULT N'Đang làm' 
        CHECK (EmploymentStatus IN (N'Đang làm', N'Đã nghỉ', N'Tạm nghỉ')),
    Position NVARCHAR(100) NULL,   -- Chức vụ (vd: Thu ngân, Quản lý, Pha chế)
    Salary DECIMAL(12,2) NULL,     -- Lương cơ bản
    BankAccount NVARCHAR(100) NULL,
    BankName NVARCHAR(200) NULL,
    Notes NVARCHAR(MAX) NULL,
    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSDATETIME(),

    CONSTRAINT FK_Employees_User FOREIGN KEY (UserID) 
        REFERENCES Users(UserID) ON DELETE CASCADE
);
GO

-- =======================================================
-- 9. INDEXES CHO EMPLOYEE
-- =======================================================
CREATE INDEX IX_Employees_Status ON Employees(EmploymentStatus);
CREATE INDEX IX_Employees_Position ON Employees(Position);
CREATE INDEX IX_Employees_FullName ON Employees(FullName);
CREATE INDEX IX_Employees_HireDate ON Employees(HireDate);
GO

-- =======================================================
-- 10. EMPLOYEE SCHEDULING (Lịch làm việc nhân viên)
-- =======================================================
CREATE TABLE EmployeeShifts (
    ShiftID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    Title NVARCHAR(200) NULL,
    Notes NVARCHAR(1000) NULL,
    StartAt DATETIME2 NOT NULL,
    EndAt DATETIME2 NOT NULL,
    Location NVARCHAR(200) NULL,
    Status NVARCHAR(50) NOT NULL DEFAULT 'Scheduled' 
        CHECK (Status IN ('Scheduled', 'Completed', 'Cancelled')),
    IsRecurring BIT NOT NULL DEFAULT 0,
    CreatedBy UNIQUEIDENTIFIER NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_EmployeeShifts_Employee FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
    CONSTRAINT FK_EmployeeShifts_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(UserID),
    CONSTRAINT CK_EmployeeShifts_TimeRange CHECK (EndAt > StartAt)
);
GO

-- =======================================================
-- 11. SHIFT TEMPLATES (Mẫu ca làm việc) & ASSIGNMENTS
-- =======================================================
CREATE TABLE ShiftTemplates (
    TemplateID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(500) NULL,
    -- Time-of-day range for a single day template
    StartTime TIME NOT NULL,
    EndTime TIME NOT NULL,
    BreakMinutes INT NOT NULL DEFAULT 0 CHECK (BreakMinutes >= 0 AND BreakMinutes <= 720),
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedBy UNIQUEIDENTIFIER NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT CK_ShiftTemplates_TimeRange CHECK (EndTime > StartTime),
    CONSTRAINT FK_ShiftTemplates_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

CREATE UNIQUE INDEX UX_ShiftTemplates_Name ON ShiftTemplates(Name);
CREATE INDEX IX_ShiftTemplates_IsActive ON ShiftTemplates(IsActive);
GO

-- Assign repeating templates to employees by weekday, with optional effective date range
CREATE TABLE EmployeeShiftAssignments (
    AssignmentID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    TemplateID UNIQUEIDENTIFIER NOT NULL,
    Weekday TINYINT NOT NULL CHECK (Weekday BETWEEN 1 AND 7), -- 1=Mon ... 7=Sun
    EffectiveFrom DATE NULL,
    EffectiveTo DATE NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedBy UNIQUEIDENTIFIER NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_EmpShiftAssign_Employee FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
    CONSTRAINT FK_EmpShiftAssign_Template FOREIGN KEY (TemplateID) REFERENCES ShiftTemplates(TemplateID) ON DELETE CASCADE,
    CONSTRAINT FK_EmpShiftAssign_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(UserID),
    CONSTRAINT CK_EmpShiftAssign_DateRange CHECK (EffectiveTo IS NULL OR EffectiveFrom IS NULL OR EffectiveTo >= EffectiveFrom)
);
GO

CREATE INDEX IX_EmpShiftAssign_EmployeeWeekday ON EmployeeShiftAssignments(EmployeeID, Weekday);
CREATE INDEX IX_EmpShiftAssign_Effective ON EmployeeShiftAssignments(EffectiveFrom, EffectiveTo);
CREATE INDEX IX_EmpShiftAssign_IsActive ON EmployeeShiftAssignments(IsActive);
GO

-- Trigger to prevent overlapping active assignments for the same employee, weekday, and date range
CREATE OR ALTER TRIGGER TRG_EmpShiftAssignments_NoOverlap
ON EmployeeShiftAssignments
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Only consider active assignments
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN EmployeeShiftAssignments a
          ON a.EmployeeID = i.EmployeeID
         AND a.AssignmentID <> i.AssignmentID
         AND a.IsActive = 1 AND i.IsActive = 1
         AND a.Weekday = i.Weekday
         -- Date range overlap (NULL means open-ended)
         AND (ISNULL(i.EffectiveFrom, a.EffectiveFrom) IS NULL OR ISNULL(a.EffectiveTo, '9999-12-31') >= ISNULL(i.EffectiveFrom, '0001-01-01'))
         AND (ISNULL(i.EffectiveTo, '9999-12-31') >= ISNULL(a.EffectiveFrom, '0001-01-01'))
    )
    BEGIN
        THROW 51001, N'Nhân viên đã có mẫu ca trùng (weekday/date range).', 1;
    END
END;
GO

-- Indexes to optimize lookups by employee and time range
CREATE INDEX IX_EmployeeShifts_EmployeeID ON EmployeeShifts(EmployeeID);
CREATE INDEX IX_EmployeeShifts_StartAt ON EmployeeShifts(StartAt);
CREATE INDEX IX_EmployeeShifts_EndAt ON EmployeeShifts(EndAt);
CREATE INDEX IX_EmployeeShifts_Status ON EmployeeShifts(Status);
GO

-- Prevent overlapping shifts for the same employee (except when existing or new is Cancelled)
CREATE OR ALTER TRIGGER TRG_EmployeeShifts_NoOverlap
ON EmployeeShifts
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN EmployeeShifts s
          ON s.EmployeeID = i.EmployeeID
         AND s.ShiftID <> i.ShiftID
         AND s.Status <> 'Cancelled'
         AND i.Status <> 'Cancelled'
         -- Time overlap condition: [i.StartAt, i.EndAt) intersects [s.StartAt, s.EndAt)
         AND i.StartAt < s.EndAt
         AND i.EndAt > s.StartAt
    )
    BEGIN
        THROW 51000, N'Nhân viên đã có lịch trùng thời gian.', 1;
    END
END;
GO


-- =======================================================
-- 12. PAYROLL & COMPENSATION
-- =======================================================

-- Pay periods define the time windows for payroll
CREATE TABLE PayPeriods (
    PayPeriodID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Name NVARCHAR(100) NOT NULL,
    PeriodType NVARCHAR(20) NOT NULL CHECK (PeriodType IN ('Monthly', 'Biweekly', 'Weekly')),
    StartDate DATE NOT NULL,
    EndDate DATE NOT NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT 'Open' CHECK (Status IN ('Open', 'Processing', 'Closed')),
    LockedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT CK_PayPeriods_DateRange CHECK (EndDate >= StartDate)
);

-- A payroll run calculates and approves payments for a given period
CREATE TABLE PayrollRuns (
    PayrollRunID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    PayPeriodID UNIQUEIDENTIFIER NOT NULL,
    RunNumber INT NOT NULL DEFAULT 1,
    Status NVARCHAR(20) NOT NULL DEFAULT 'Draft' CHECK (Status IN ('Draft', 'Calculated', 'Approved', 'Paid', 'Cancelled')),
    CalculatedAt DATETIME2 NULL,
    ApprovedBy UNIQUEIDENTIFIER NULL,
    ApprovedAt DATETIME2 NULL,
    Notes NVARCHAR(1000) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_PayrollRuns_Period FOREIGN KEY (PayPeriodID) REFERENCES PayPeriods(PayPeriodID) ON DELETE CASCADE,
    CONSTRAINT FK_PayrollRuns_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES Users(UserID) ON DELETE SET NULL
);

-- Concrete payroll results per employee for a run
CREATE TABLE PayrollEntries (
    PayrollEntryID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    PayrollRunID UNIQUEIDENTIFIER NOT NULL,
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    CompensationType NVARCHAR(20) NOT NULL CHECK (CompensationType IN ('Fixed', 'PerShift', 'Hybrid')),
    BaseSalary DECIMAL(12,2) NULL,
    HourlyRate DECIMAL(12,2) NULL,
    PerShiftRate DECIMAL(12,2) NULL,
    HoursWorked DECIMAL(9,2) NULL,
    ShiftsWorked INT NULL,
    OvertimeHours DECIMAL(9,2) NULL,
    HolidayHours DECIMAL(9,2) NULL,
    Allowances DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    Bonuses DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    Deductions DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    GrossPay DECIMAL(12,2) NOT NULL,
    NetPay DECIMAL(12,2) NOT NULL,
    Currency NVARCHAR(3) NOT NULL DEFAULT 'VND',
    ExchangeRate DECIMAL(18,6) NULL, -- against VND on calculation date (if applicable)
    PaidInCurrency NVARCHAR(3) NOT NULL DEFAULT 'VND',
    IsPaid BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_PayrollEntries_Run FOREIGN KEY (PayrollRunID) REFERENCES PayrollRuns(PayrollRunID) ON DELETE CASCADE,
    CONSTRAINT FK_PayrollEntries_Employee FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE
);

-- Manual adjustments per employee within a payroll run
CREATE TABLE PayrollAdjustments (
    AdjustmentID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    PayrollRunID UNIQUEIDENTIFIER NOT NULL,
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    AdjustmentType NVARCHAR(20) NOT NULL CHECK (AdjustmentType IN ('Allowance', 'Bonus', 'Deduction', 'Penalty')),
    Amount DECIMAL(12,2) NOT NULL,
    Reason NVARCHAR(500) NULL,
    CreatedBy UNIQUEIDENTIFIER NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ApprovedBy UNIQUEIDENTIFIER NULL,
    ApprovedAt DATETIME2 NULL,

    CONSTRAINT FK_PayrollAdjustments_Run FOREIGN KEY (PayrollRunID) REFERENCES PayrollRuns(PayrollRunID) ON DELETE CASCADE,
    CONSTRAINT FK_PayrollAdjustments_Emp FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
    CONSTRAINT FK_PayrollAdjustments_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(UserID),
    CONSTRAINT FK_PayrollAdjustments_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES Users(UserID)
);

-- Indexes for payroll entities
CREATE INDEX IX_PayPeriods_Status ON PayPeriods(Status);
CREATE INDEX IX_PayrollRuns_Period ON PayrollRuns(PayPeriodID);
CREATE INDEX IX_PayrollEntries_Run ON PayrollEntries(PayrollRunID);
CREATE INDEX IX_PayrollEntries_Employee ON PayrollEntries(EmployeeID);
GO

-- =======================================================
-- 13. COMPENSATION CONFIGURATION & POLICIES
-- =======================================================

-- Global pay policy presets (can be linked to employees)
CREATE TABLE PayPolicies (
    PolicyID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Name NVARCHAR(100) NOT NULL UNIQUE,
    Description NVARCHAR(500) NULL,
    OvertimeMultiplier DECIMAL(5,2) NOT NULL DEFAULT 1.50,
    NightShiftMultiplier DECIMAL(5,2) NOT NULL DEFAULT 1.20,
    WeekendMultiplier DECIMAL(5,2) NOT NULL DEFAULT 1.50,
    HolidayMultiplier DECIMAL(5,2) NOT NULL DEFAULT 2.00,
    MaxDailyHours DECIMAL(5,2) NULL,
    MinBreakMinutes INT NOT NULL DEFAULT 0 CHECK (MinBreakMinutes >= 0 AND MinBreakMinutes <= 720),
    SocialInsuranceRate DECIMAL(5,4) NULL,
    HealthInsuranceRate DECIMAL(5,4) NULL,
    UnemploymentInsuranceRate DECIMAL(5,4) NULL,
    PITFlatRate DECIMAL(5,4) NULL,
    Currency NVARCHAR(3) NOT NULL DEFAULT 'VND',
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedBy UNIQUEIDENTIFIER NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_PayPolicies_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(UserID) ON DELETE SET NULL
);

-- Per-employee compensation settings (fixed salary, per-shift, or hybrid) with effective ranges
CREATE TABLE EmployeeCompensation (
    CompensationID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    CompensationType NVARCHAR(20) NOT NULL CHECK (CompensationType IN ('Fixed', 'PerShift', 'Hybrid')),
    PolicyID UNIQUEIDENTIFIER NULL,
    BaseMonthlySalary DECIMAL(12,2) NULL,
    HourlyRate DECIMAL(12,2) NULL,
    PerShiftRate DECIMAL(12,2) NULL,

    -- Additional compensation fields
    OvertimeRate DECIMAL(12,2) NULL,        -- Mức lương làm thêm giờ (VD: 30000 VND/giờ)
    BonusAmount DECIMAL(12,2) NULL,         -- Tiền thưởng cố định (VD: 1000000 VND)
    CommissionRate DECIMAL(12,2) NULL,      -- Tỷ lệ hoa hồng % (VD: 5.5 nghĩa là 5.5%)
    AllowanceAmount DECIMAL(12,2) NULL,     -- Phụ cấp (xăng xe, ăn uống, điện thoại, etc.)
    DeductionAmount DECIMAL(12,2) NULL,     -- Giảm trừ (bảo hiểm, thuế, phạt, etc.)

    Currency NVARCHAR(3) NOT NULL DEFAULT 'VND',
    EffectiveFrom DATE NOT NULL,
    EffectiveTo DATE NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    Notes NVARCHAR(500) NULL,
    CreatedBy UNIQUEIDENTIFIER NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_EmployeeComp_Employee FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
    CONSTRAINT FK_EmployeeComp_Policy FOREIGN KEY (PolicyID) REFERENCES PayPolicies(PolicyID) ON DELETE SET NULL,
    CONSTRAINT CK_EmployeeComp_DateRange CHECK (EffectiveTo IS NULL OR EffectiveTo >= EffectiveFrom)
);
GO

-- Prevent overlapping active compensation windows for the same employee
CREATE OR ALTER TRIGGER TRG_EmployeeComp_NoOverlap
ON EmployeeCompensation
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN EmployeeCompensation c
          ON c.EmployeeID = i.EmployeeID
         AND c.CompensationID <> i.CompensationID
         AND c.IsActive = 1 AND i.IsActive = 1
         AND (ISNULL(i.EffectiveTo, '9999-12-31') >= ISNULL(c.EffectiveFrom, '0001-01-01'))
         AND (ISNULL(c.EffectiveTo, '9999-12-31') >= ISNULL(i.EffectiveFrom, '0001-01-01'))
    )
    BEGIN
        THROW 51002, N'Nhân viên đã có cấu hình lương chồng lấp thời gian.', 1;
    END
END;
GO

CREATE INDEX IX_EmployeeComp_Employee ON EmployeeCompensation(EmployeeID);
CREATE INDEX IX_EmployeeComp_Active ON EmployeeCompensation(IsActive);
CREATE INDEX IX_EmployeeComp_Effective ON EmployeeCompensation(EffectiveFrom, EffectiveTo);
GO

-- =======================================================
-- 14. SHIFT PAY RULES (Per template/position, weekday/weekend/holiday)
-- =======================================================

CREATE TABLE ShiftPayRules (
    RuleID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TemplateID UNIQUEIDENTIFIER NULL,
    Position NVARCHAR(100) NULL,
    DayType NVARCHAR(20) NOT NULL CHECK (DayType IN ('Any', 'Weekday', 'Weekend', 'Holiday')),
    RateType NVARCHAR(20) NOT NULL CHECK (RateType IN ('Hourly', 'PerShift')),
    Rate DECIMAL(12,2) NOT NULL,
    Currency NVARCHAR(3) NOT NULL DEFAULT 'VND',
    EffectiveFrom DATE NOT NULL,
    EffectiveTo DATE NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    Notes NVARCHAR(500) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_ShiftPayRules_Template FOREIGN KEY (TemplateID) REFERENCES ShiftTemplates(TemplateID) ON DELETE SET NULL,
    CONSTRAINT CK_ShiftPayRules_DateRange CHECK (EffectiveTo IS NULL OR EffectiveTo >= EffectiveFrom)
);

CREATE INDEX IX_ShiftPayRules_Template ON ShiftPayRules(TemplateID);
CREATE INDEX IX_ShiftPayRules_Effective ON ShiftPayRules(EffectiveFrom, EffectiveTo);
CREATE INDEX IX_ShiftPayRules_IsActive ON ShiftPayRules(IsActive);
GO

-- =======================================================
-- 15. TIMESHEETS (Actual working time capture)
-- =======================================================

CREATE TABLE EmployeeShiftTimesheets (
    TimesheetID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    ShiftID UNIQUEIDENTIFIER NULL,
    WorkDate DATE NOT NULL,
    CheckInAt DATETIME2 NOT NULL,
    CheckOutAt DATETIME2 NOT NULL,
    BreakMinutes INT NOT NULL DEFAULT 0 CHECK (BreakMinutes >= 0 AND BreakMinutes <= 720),
    Status NVARCHAR(20) NOT NULL DEFAULT 'Pending' CHECK (Status IN ('Pending', 'Approved', 'Rejected')),
    Source NVARCHAR(20) NOT NULL DEFAULT 'Manual' CHECK (Source IN ('Manual', 'Auto', 'Import')),
    HoursWorked DECIMAL(9,2) NULL,
    ApprovedBy UNIQUEIDENTIFIER NULL,
    ApprovedAt DATETIME2 NULL,
    Notes NVARCHAR(500) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_Timesheets_Employee FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID),
    CONSTRAINT FK_Timesheets_Shift FOREIGN KEY (ShiftID) REFERENCES EmployeeShifts(ShiftID) ON DELETE SET NULL,
    CONSTRAINT FK_Timesheets_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES Users(UserID),
    CONSTRAINT CK_Timesheets_TimeRange CHECK (CheckOutAt > CheckInAt)
);

CREATE INDEX IX_Timesheets_Employee ON EmployeeShiftTimesheets(EmployeeID);
CREATE INDEX IX_Timesheets_Date ON EmployeeShiftTimesheets(WorkDate);
CREATE INDEX IX_Timesheets_Status ON EmployeeShiftTimesheets(Status);
GO

-- =======================================================
-- 16. EXTERNAL INFO (Holidays, Exchange Rates)
-- =======================================================

CREATE TABLE HolidayCalendar (
    HolidayID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    HolidayDate DATE NOT NULL,
    Name NVARCHAR(200) NOT NULL,
    Region NVARCHAR(50) NOT NULL DEFAULT 'VN',
    DayType NVARCHAR(20) NOT NULL DEFAULT 'Public' CHECK (DayType IN ('Public', 'Company', 'Special')),
    IsPaidHoliday BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT UX_HolidayCalendar UNIQUE (HolidayDate, Region)
);

CREATE TABLE ExchangeRates (
    RateID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Currency NVARCHAR(3) NOT NULL,
    RateToVND DECIMAL(18,6) NOT NULL,
    RateDate DATE NOT NULL,
    Source NVARCHAR(100) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT CK_ExchangeRates_Positive CHECK (RateToVND > 0),
    CONSTRAINT UX_ExchangeRates UNIQUE (Currency, RateDate)
);
GO

-- =======================================================
-- 17. ATTENDANCE STATUS (Daily per-employee)
-- =======================================================
CREATE TABLE EmployeeAttendance (
    AttendanceID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    WorkDate DATE NOT NULL,
    Status NVARCHAR(20) NOT NULL CHECK (Status IN ('Work', 'LeavePaid', 'LeaveUnpaid')),
    CheckInTime TIME NULL,
    CheckOutTime TIME NULL,
    Notes NVARCHAR(500) NULL,
    IsLate BIT NULL,
    IsOvertime BIT NULL,
    IsEarlyLeave BIT NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_EmployeeAttendance_Employee FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
    CONSTRAINT UX_EmployeeAttendance UNIQUE (EmployeeID, WorkDate)
);
GO

CREATE INDEX IX_EmployeeAttendance_Employee ON EmployeeAttendance(EmployeeID);
CREATE INDEX IX_EmployeeAttendance_WorkDate ON EmployeeAttendance(WorkDate);
GO

-- =======================================================
-- 18. COMPENSATION EVENTS (Per-day Bonuses/Penalties)
-- =======================================================
CREATE TABLE EmployeeCompEvents (
    EventID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    WorkDate DATE NULL,
    EventType NVARCHAR(20) NOT NULL CHECK (EventType IN ('Bonus', 'Penalty')),
    Amount DECIMAL(12,2) NOT NULL CHECK (Amount >= 0),
    Reason NVARCHAR(500) NULL,
    CreatedBy UNIQUEIDENTIFIER NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ApprovedBy UNIQUEIDENTIFIER NULL,
    ApprovedAt DATETIME2 NULL,

    CONSTRAINT FK_CompEvents_Employee FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
    CONSTRAINT FK_CompEvents_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(UserID),
    CONSTRAINT FK_CompEvents_ApprovedBy FOREIGN KEY (ApprovedBy) REFERENCES Users(UserID)
);
GO

CREATE INDEX IX_EmployeeCompEvents_Employee ON EmployeeCompEvents(EmployeeID);
CREATE INDEX IX_EmployeeCompEvents_WorkDate ON EmployeeCompEvents(WorkDate);
CREATE INDEX IX_EmployeeCompEvents_Type ON EmployeeCompEvents(EventType);
GO

-- =======================================================
-- 19. PERSONAL SCHEDULE (Personal tasks for employees)
-- =======================================================
CREATE TABLE PersonalSchedules (
    ScheduleID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID UNIQUEIDENTIFIER NOT NULL,
    Title NVARCHAR(200) NOT NULL,
    Description NVARCHAR(1000) NULL,
    StartDate DATE NOT NULL,
    StartTime TIME NULL,
    EndTime TIME NULL,
    Priority NVARCHAR(20) NOT NULL DEFAULT 'Medium' CHECK (Priority IN ('Low', 'Medium', 'High')),
    Status NVARCHAR(20) NOT NULL DEFAULT 'Pending' CHECK (Status IN ('Pending', 'InProgress', 'Completed', 'Cancelled')),
    ReminderDate DATETIME2 NULL,
    ReminderSent BIT NOT NULL DEFAULT 0,
    Notes NVARCHAR(MAX) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_PersonalSchedules_Employee FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE
);
GO

CREATE INDEX IX_PersonalSchedules_Employee ON PersonalSchedules(EmployeeID);
CREATE INDEX IX_PersonalSchedules_StartDate ON PersonalSchedules(StartDate);
CREATE INDEX IX_PersonalSchedules_Priority ON PersonalSchedules(Priority);
CREATE INDEX IX_PersonalSchedules_Status ON PersonalSchedules(Status);
GO


IF OBJECT_ID('ForgotClockRequests', 'U') IS NULL
BEGIN
    CREATE TABLE ForgotClockRequests (
        ForgotClockRequestID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        EmployeeID UNIQUEIDENTIFIER NOT NULL,
        
        -- Forgot Details
        ForgotDate DATE NOT NULL,
        ForgotType NVARCHAR(20) NOT NULL CHECK (ForgotType IN ('CHECK_IN', 'CHECK_OUT', 'BOTH')),
        ForgotTime TIME NULL,
        Reason NVARCHAR(1000) NOT NULL,
        
        -- Status
        Status NVARCHAR(20) NOT NULL DEFAULT N'Chờ duyệt' CHECK (Status IN (N'Chờ duyệt', N'Đã duyệt', N'Từ chối', N'Đã hủy')),
        
        -- Review
        ReviewedBy UNIQUEIDENTIFIER NULL,
        ReviewedAt DATETIME2 NULL,
        ReviewNotes NVARCHAR(500) NULL,
        
        -- Timestamps
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        
        -- Foreign Keys
        CONSTRAINT FK_ForgotClockRequests_Employee FOREIGN KEY (EmployeeID) 
            REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
        CONSTRAINT FK_ForgotClockRequests_ReviewedBy FOREIGN KEY (ReviewedBy) 
            REFERENCES Users(UserID) ON DELETE NO ACTION,
        
        -- Constraints
        -- Allow same day requests (<= instead of <) for employees who forgot to clock in/out today
        CONSTRAINT CK_ForgotClockRequests_ForgotDate CHECK (ForgotDate <= CAST(SYSDATETIME() AS DATE))
    );
    
    -- Indexes
    CREATE INDEX IX_ForgotClockRequests_Employee ON ForgotClockRequests(EmployeeID);
    CREATE INDEX IX_ForgotClockRequests_Status ON ForgotClockRequests(Status);
    CREATE INDEX IX_ForgotClockRequests_ForgotDate ON ForgotClockRequests(ForgotDate);
    CREATE INDEX IX_ForgotClockRequests_CreatedAt ON ForgotClockRequests(CreatedAt);
    
    PRINT '✅ ForgotClockRequests table created successfully';
END
ELSE
BEGIN
    PRINT '⚠️ ForgotClockRequests table already exists';
END
GO

-- ============================================================
-- Trigger to update UpdatedAt timestamp
-- ============================================================

IF OBJECT_ID('TRG_ForgotClockRequests_UpdatedAt', 'TR') IS NOT NULL
    DROP TRIGGER TRG_ForgotClockRequests_UpdatedAt;
GO

CREATE TRIGGER TRG_ForgotClockRequests_UpdatedAt
ON ForgotClockRequests
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    UPDATE ForgotClockRequests
    SET UpdatedAt = SYSDATETIME()
    FROM ForgotClockRequests fcr
    INNER JOIN inserted i ON fcr.ForgotClockRequestID = i.ForgotClockRequestID;
END;
GO

PRINT '✅ ForgotClockRequests trigger created successfully';
GO
