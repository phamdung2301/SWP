-- =====================================================
-- RESORT ERP EXPANSION - PART 5: TRANSPORT, CRM, EVENT, HR-ORG
-- =====================================================
USE LiteFlowDBO;
GO

-- ======================
-- A. TRANSPORT MODULE
-- ======================
CREATE TABLE Vehicles (
    VehicleID    UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    PlateNumber  NVARCHAR(20)  NOT NULL UNIQUE,
    Type         NVARCHAR(30)  NOT NULL  -- CAR, VAN, BUGGY, SHUTTLE, BUS
        CHECK (Type IN ('CAR','VAN','BUGGY','SHUTTLE','BUS','BOAT','OTHER')),
    Brand        NVARCHAR(100) NULL,
    Model        NVARCHAR(100) NULL,
    Capacity     INT NOT NULL DEFAULT 4,
    Status       NVARCHAR(20) DEFAULT 'AVAILABLE'
        CHECK (Status IN ('AVAILABLE','IN_USE','MAINTENANCE','RETIRED')),
    CurrentFuelL DECIMAL(8,2) NULL,
    IsActive     BIT DEFAULT 1,
    Notes        NVARCHAR(MAX) NULL,
    CreatedAt    DATETIME2 DEFAULT SYSDATETIME()
);
GO

CREATE TABLE Drivers (
    DriverID   UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID UNIQUEIDENTIFIER NOT NULL UNIQUE,
    LicenseNo  NVARCHAR(30)  NOT NULL UNIQUE,
    LicenseExpiry DATE NULL,
    Status     NVARCHAR(20) DEFAULT 'AVAILABLE'
        CHECK (Status IN ('AVAILABLE','ON_TRIP','OFF_DUTY','INACTIVE')),
    CONSTRAINT FK_Driver_Employee FOREIGN KEY (EmployeeID)
        REFERENCES Employees(EmployeeID) ON DELETE CASCADE
);
GO

CREATE TABLE Trips (
    TripID     UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TripCode   NVARCHAR(20) NOT NULL UNIQUE,
    VehicleID  UNIQUEIDENTIFIER NOT NULL,
    DriverID   UNIQUEIDENTIFIER NOT NULL,
    TripType   NVARCHAR(30) DEFAULT 'SHUTTLE'
        CHECK (TripType IN ('AIRPORT_PICKUP','AIRPORT_DROP','SHUTTLE',
                            'BUGGY','EXCURSION','OTHER')),
    GuestID    UNIQUEIDENTIFIER NULL,
    FromLocation NVARCHAR(200) NULL,
    ToLocation   NVARCHAR(200) NULL,
    ScheduledAt DATETIME2 NOT NULL,
    ActualStart DATETIME2 NULL,
    ActualEnd   DATETIME2 NULL,
    Status      NVARCHAR(20) DEFAULT 'SCHEDULED'
        CHECK (Status IN ('SCHEDULED','IN_PROGRESS','COMPLETED','CANCELLED')),
    Notes       NVARCHAR(MAX) NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Trip_Vehicle FOREIGN KEY (VehicleID)
        REFERENCES Vehicles(VehicleID),
    CONSTRAINT FK_Trip_Driver FOREIGN KEY (DriverID)
        REFERENCES Drivers(DriverID),
    CONSTRAINT FK_Trip_Guest FOREIGN KEY (GuestID)
        REFERENCES GuestProfiles(GuestID) ON DELETE SET NULL
);
GO

CREATE TABLE FuelLogs (
    FuelLogID  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    VehicleID  UNIQUEIDENTIFIER NOT NULL,
    FuelDate   DATE NOT NULL,
    LitersFilled DECIMAL(8,2) NOT NULL,
    CostPerLiter DECIMAL(8,2) NULL,
    TotalCost  DECIMAL(10,2) NULL,
    OdometerKm DECIMAL(10,2) NULL,
    FilledBy   UNIQUEIDENTIFIER NULL,
    Notes      NVARCHAR(200) NULL,
    CreatedAt  DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_FL_Vehicle FOREIGN KEY (VehicleID)
        REFERENCES Vehicles(VehicleID) ON DELETE CASCADE,
    CONSTRAINT FK_FL_User FOREIGN KEY (FilledBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- ======================
-- B. CRM & LOYALTY
-- ======================
CREATE TABLE LoyaltyPoints (
    TxnID      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    GuestID    UNIQUEIDENTIFIER NOT NULL,
    Points     INT NOT NULL,  -- positive=earn, negative=redeem
    TxnType    NVARCHAR(20) NOT NULL
        CHECK (TxnType IN ('EARN','REDEEM','EXPIRE','ADJUST','BONUS')),
    ReferenceID NVARCHAR(50) NULL,  -- reservation code, etc.
    Description NVARCHAR(200) NULL,
    TxnDate    DATETIME2 DEFAULT SYSDATETIME(),
    CreatedBy  UNIQUEIDENTIFIER NULL,
    CONSTRAINT FK_LP_Guest FOREIGN KEY (GuestID)
        REFERENCES GuestProfiles(GuestID) ON DELETE CASCADE,
    CONSTRAINT FK_LP_User FOREIGN KEY (CreatedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

CREATE TABLE VisitHistory (
    VisitID       UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    GuestID       UNIQUEIDENTIFIER NOT NULL,
    ReservationID UNIQUEIDENTIFIER NULL,
    CheckInDate   DATE NOT NULL,
    CheckOutDate  DATE NULL,
    TotalSpent    DECIMAL(14,2) DEFAULT 0,
    RoomNumber    NVARCHAR(20)  NULL,
    Notes         NVARCHAR(500) NULL,
    CONSTRAINT FK_VH_Guest FOREIGN KEY (GuestID)
        REFERENCES GuestProfiles(GuestID) ON DELETE CASCADE,
    CONSTRAINT FK_VH_Res FOREIGN KEY (ReservationID)
        REFERENCES HotelReservations(ReservationID) ON DELETE SET NULL
);
GO

CREATE TABLE CustomerFeedback (
    FeedbackID   UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    GuestID      UNIQUEIDENTIFIER NULL,
    ReservationID UNIQUEIDENTIFIER NULL,
    Channel      NVARCHAR(30) DEFAULT 'IN_APP'
        CHECK (Channel IN ('IN_APP','EMAIL','VERBAL','TRIPADVISOR','GOOGLE','OTHER')),
    OverallRating TINYINT NOT NULL CHECK (OverallRating BETWEEN 1 AND 5),
    CategoryRatings NVARCHAR(MAX) NULL,  -- JSON: {cleanliness: 5, service: 4, ...}
    Comment      NVARCHAR(MAX) NULL,
    IsPublic     BIT DEFAULT 0,
    ResponseBy   UNIQUEIDENTIFIER NULL,
    ResponseText NVARCHAR(MAX) NULL,
    CreatedAt    DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_CF_Guest FOREIGN KEY (GuestID)
        REFERENCES GuestProfiles(GuestID) ON DELETE SET NULL,
    CONSTRAINT FK_CF_Res FOREIGN KEY (ReservationID)
        REFERENCES HotelReservations(ReservationID) ON DELETE SET NULL,
    CONSTRAINT FK_CF_Responder FOREIGN KEY (ResponseBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- ======================
-- C. EVENT MANAGEMENT
-- ======================
CREATE TABLE Venues (
    VenueID     UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Code        NVARCHAR(20)  NOT NULL UNIQUE,
    Name        NVARCHAR(200) NOT NULL,
    Type        NVARCHAR(50)  NOT NULL  -- BALLROOM, MEETING_ROOM, GARDEN, POOLSIDE
        CHECK (Type IN ('BALLROOM','MEETING_ROOM','GARDEN','POOLSIDE','RESTAURANT','OTHER')),
    BuildingID  UNIQUEIDENTIFIER NULL,
    Capacity    INT NOT NULL DEFAULT 50,
    SizeM2      DECIMAL(8,2)  NULL,
    HourlyRate  DECIMAL(12,2) NULL,
    DailyRate   DECIMAL(12,2) NULL,
    Amenities   NVARCHAR(MAX) NULL,
    IsActive    BIT DEFAULT 1,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Venue_Building FOREIGN KEY (BuildingID)
        REFERENCES Buildings(BuildingID) ON DELETE SET NULL
);
GO

CREATE TABLE Events (
    EventID     UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EventCode   NVARCHAR(20)  NOT NULL UNIQUE,
    Title       NVARCHAR(300) NOT NULL,
    EventType   NVARCHAR(50)  NOT NULL  -- WEDDING, CONFERENCE, BIRTHDAY, GALA
        CHECK (EventType IN ('WEDDING','CONFERENCE','BIRTHDAY','GALA',
                             'CORPORATE','EXHIBITION','OTHER')),
    GuestID     UNIQUEIDENTIFIER NULL,  -- organizer/client
    Status      NVARCHAR(20) DEFAULT 'INQUIRY'
        CHECK (Status IN ('INQUIRY','QUOTED','CONFIRMED',
                          'IN_PROGRESS','COMPLETED','CANCELLED')),
    StartAt     DATETIME2 NOT NULL,
    EndAt       DATETIME2 NOT NULL,
    GuestCount  INT NOT NULL DEFAULT 1,
    TotalBudget DECIMAL(14,2) NULL,
    Notes       NVARCHAR(MAX) NULL,
    CreatedBy   UNIQUEIDENTIFIER NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Event_Guest FOREIGN KEY (GuestID)
        REFERENCES GuestProfiles(GuestID) ON DELETE SET NULL,
    CONSTRAINT FK_Event_User FOREIGN KEY (CreatedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL,
    CONSTRAINT CK_Event_Dates CHECK (EndAt > StartAt)
);
GO

CREATE TABLE VenueBookings (
    BookingID  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EventID    UNIQUEIDENTIFIER NOT NULL,
    VenueID    UNIQUEIDENTIFIER NOT NULL,
    StartAt    DATETIME2 NOT NULL,
    EndAt      DATETIME2 NOT NULL,
    SetupType  NVARCHAR(50) NULL,  -- THEATER, CLASSROOM, BANQUET, COCKTAIL
    Price      DECIMAL(12,2) NULL,
    Notes      NVARCHAR(MAX) NULL,
    CreatedAt  DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_VB_Event FOREIGN KEY (EventID)
        REFERENCES Events(EventID) ON DELETE CASCADE,
    CONSTRAINT FK_VB_Venue FOREIGN KEY (VenueID)
        REFERENCES Venues(VenueID),
    CONSTRAINT CK_VB_Dates CHECK (EndAt > StartAt)
);
GO

CREATE TABLE CateringOrders (
    CateringID  UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EventID     UNIQUEIDENTIFIER NOT NULL,
    MenuType    NVARCHAR(50) NULL,
    PerPersonCost DECIMAL(10,2) NULL,
    GuestCount  INT NOT NULL DEFAULT 1,
    TotalCost   DECIMAL(12,2) NULL,
    SpecialNeeds NVARCHAR(MAX) NULL,  -- dietary, halal, vegan...
    Status      NVARCHAR(20) DEFAULT 'PLANNED'
        CHECK (Status IN ('PLANNED','CONFIRMED','PREPARED','SERVED','CANCELLED')),
    Notes       NVARCHAR(MAX) NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_CO_Event FOREIGN KEY (EventID)
        REFERENCES Events(EventID) ON DELETE CASCADE
);
GO

-- ======================
-- D. HR ORGANIZATION EXPANSION
-- ======================
CREATE TABLE Departments (
    DepartmentID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Code         NVARCHAR(20)  NOT NULL UNIQUE,
    Name         NVARCHAR(100) NOT NULL,
    Description  NVARCHAR(500) NULL,
    ManagerID    UNIQUEIDENTIFIER NULL,
    ParentID     UNIQUEIDENTIFIER NULL,
    IsActive     BIT DEFAULT 1,
    CreatedAt    DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Dept_Manager FOREIGN KEY (ManagerID)
        REFERENCES Employees(EmployeeID) ON DELETE SET NULL,
    CONSTRAINT FK_Dept_Parent FOREIGN KEY (ParentID)
        REFERENCES Departments(DepartmentID)
);
GO

CREATE TABLE OrgPositions (
    PositionID   UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    DepartmentID UNIQUEIDENTIFIER NOT NULL,
    Code         NVARCHAR(20)  NOT NULL UNIQUE,
    Title        NVARCHAR(100) NOT NULL,
    Level        INT DEFAULT 1,  -- 1=entry, 5=executive
    BaseSalaryMin DECIMAL(12,2) NULL,
    BaseSalaryMax DECIMAL(12,2) NULL,
    IsActive     BIT DEFAULT 1,
    CONSTRAINT FK_OP_Dept FOREIGN KEY (DepartmentID)
        REFERENCES Departments(DepartmentID) ON DELETE CASCADE
);
GO

CREATE TABLE EmployeeDepartments (
    AssignID     UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EmployeeID   UNIQUEIDENTIFIER NOT NULL,
    DepartmentID UNIQUEIDENTIFIER NOT NULL,
    PositionID   UNIQUEIDENTIFIER NULL,
    IsPrimary    BIT DEFAULT 1,
    StartDate    DATE NOT NULL,
    EndDate      DATE NULL,
    CONSTRAINT FK_ED_Employee FOREIGN KEY (EmployeeID)
        REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
    CONSTRAINT FK_ED_Dept FOREIGN KEY (DepartmentID)
        REFERENCES Departments(DepartmentID),
    CONSTRAINT FK_ED_Position FOREIGN KEY (PositionID)
        REFERENCES OrgPositions(PositionID) ON DELETE SET NULL
);
GO

-- Indexes
CREATE INDEX IX_Trips_Status    ON Trips(Status);
CREATE INDEX IX_Trips_ScheduleAt ON Trips(ScheduledAt);
CREATE INDEX IX_VH_Guest        ON VisitHistory(GuestID);
CREATE INDEX IX_Events_Status   ON Events(Status);
CREATE INDEX IX_Events_Dates    ON Events(StartAt, EndAt);
CREATE INDEX IX_VenueBook_Dates ON VenueBookings(StartAt, EndAt);
GO

PRINT 'Resort ERP Schema Part 5 - Complete';
GO
