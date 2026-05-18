-- =====================================================
-- RESORT ERP EXPANSION - PART 1: HOTEL CORE
-- =====================================================
USE LiteFlowDBO;
GO

-- Buildings
CREATE TABLE Buildings (
    BuildingID   UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Code         NVARCHAR(20)  NOT NULL UNIQUE,
    Name         NVARCHAR(200) NOT NULL,
    Description  NVARCHAR(MAX) NULL,
    TotalFloors  INT DEFAULT 1,
    IsActive     BIT DEFAULT 1,
    CreatedAt    DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt    DATETIME2 DEFAULT SYSDATETIME()
);
GO

-- Floors
CREATE TABLE Floors (
    FloorID    UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    BuildingID UNIQUEIDENTIFIER NOT NULL,
    FloorNumber INT NOT NULL,
    Name       NVARCHAR(100) NULL,
    CONSTRAINT FK_Floors_Building FOREIGN KEY (BuildingID)
        REFERENCES Buildings(BuildingID) ON DELETE CASCADE
);
GO

-- Room Types
CREATE TABLE RoomTypes (
    RoomTypeID   UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Code         NVARCHAR(20)  NOT NULL UNIQUE,
    Name         NVARCHAR(100) NOT NULL,
    Description  NVARCHAR(MAX) NULL,
    BasePrice    DECIMAL(14,2) NOT NULL DEFAULT 0,
    MaxOccupancy INT NOT NULL DEFAULT 2,
    SizeM2       DECIMAL(8,2)  NULL,
    Amenities    NVARCHAR(MAX) NULL,
    ImageURL     NVARCHAR(MAX) NULL,
    IsActive     BIT DEFAULT 1
);
GO

-- Hotel Rooms
CREATE TABLE HotelRooms (
    HotelRoomID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    RoomNumber  NVARCHAR(20)  NOT NULL UNIQUE,
    FloorID     UNIQUEIDENTIFIER NOT NULL,
    RoomTypeID  UNIQUEIDENTIFIER NOT NULL,
    Status      NVARCHAR(30) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (Status IN ('AVAILABLE','OCCUPIED','DIRTY','CLEANING',
                          'MAINTENANCE','OUT_OF_ORDER','RESERVED','VIP')),
    IsActive    BIT DEFAULT 1,
    Notes       NVARCHAR(MAX) NULL,
    CreatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt   DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_HotelRooms_Floor FOREIGN KEY (FloorID)
        REFERENCES Floors(FloorID),
    CONSTRAINT FK_HotelRooms_RoomType FOREIGN KEY (RoomTypeID)
        REFERENCES RoomTypes(RoomTypeID)
);
GO

-- Room Status History
CREATE TABLE RoomStatusHistory (
    HistoryID   UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    HotelRoomID UNIQUEIDENTIFIER NOT NULL,
    OldStatus   NVARCHAR(30) NULL,
    NewStatus   NVARCHAR(30) NOT NULL,
    ChangedBy   UNIQUEIDENTIFIER NULL,
    ChangedAt   DATETIME2 DEFAULT SYSDATETIME(),
    Reason      NVARCHAR(500) NULL,
    CONSTRAINT FK_RSH_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID) ON DELETE CASCADE,
    CONSTRAINT FK_RSH_User FOREIGN KEY (ChangedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- Guest Profiles
CREATE TABLE GuestProfiles (
    GuestID       UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    GuestCode     NVARCHAR(20)  NOT NULL UNIQUE,
    FullName      NVARCHAR(200) NOT NULL,
    Email         NVARCHAR(320) NULL,
    Phone         NVARCHAR(30)  NULL,
    NationalID    NVARCHAR(30)  NULL,
    Nationality   NVARCHAR(100) NULL,
    BirthDate     DATE NULL,
    Gender        NVARCHAR(10)  NULL,
    Address       NVARCHAR(500) NULL,
    VIPLevel      NVARCHAR(20) DEFAULT 'STANDARD'
        CHECK (VIPLevel IN ('STANDARD','SILVER','GOLD','PLATINUM','DIAMOND')),
    LoyaltyPoints INT DEFAULT 0,
    TotalStays    INT DEFAULT 0,
    Notes         NVARCHAR(MAX) NULL,
    IsDeleted     BIT DEFAULT 0,
    CreatedAt     DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt     DATETIME2 DEFAULT SYSDATETIME()
);
GO

-- Guest Preferences
CREATE TABLE GuestPreferences (
    PrefID      UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    GuestID     UNIQUEIDENTIFIER NOT NULL,
    PrefKey     NVARCHAR(100) NOT NULL,
    PrefValue   NVARCHAR(500) NOT NULL,
    CONSTRAINT FK_GP_Guest FOREIGN KEY (GuestID)
        REFERENCES GuestProfiles(GuestID) ON DELETE CASCADE
);
GO

-- Hotel Reservations
CREATE TABLE HotelReservations (
    ReservationID   UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ReservationCode NVARCHAR(20) NOT NULL UNIQUE,
    GuestID         UNIQUEIDENTIFIER NOT NULL,
    HotelRoomID     UNIQUEIDENTIFIER NOT NULL,
    CheckInDate     DATE NOT NULL,
    CheckOutDate    DATE NOT NULL,
    Adults          INT NOT NULL DEFAULT 1,
    Children        INT NOT NULL DEFAULT 0,
    Status          NVARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (Status IN ('PENDING','CONFIRMED','CHECKED_IN',
                          'CHECKED_OUT','CANCELLED','NO_SHOW')),
    Source          NVARCHAR(50) NULL,  -- WALK_IN, ONLINE, OTA
    TotalAmount     DECIMAL(14,2) DEFAULT 0,
    PaidAmount      DECIMAL(14,2) DEFAULT 0,
    SpecialRequests NVARCHAR(MAX) NULL,
    CreatedBy       UNIQUEIDENTIFIER NULL,
    CreatedAt       DATETIME2 DEFAULT SYSDATETIME(),
    UpdatedAt       DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_HR_Guest FOREIGN KEY (GuestID)
        REFERENCES GuestProfiles(GuestID),
    CONSTRAINT FK_HR_Room FOREIGN KEY (HotelRoomID)
        REFERENCES HotelRooms(HotelRoomID),
    CONSTRAINT FK_HR_User FOREIGN KEY (CreatedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL,
    CONSTRAINT CK_HR_Dates CHECK (CheckOutDate > CheckInDate)
);
GO

-- Check Ins
CREATE TABLE CheckIns (
    CheckInID     UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ReservationID UNIQUEIDENTIFIER NOT NULL UNIQUE,
    ActualCheckIn DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ProcessedBy   UNIQUEIDENTIFIER NULL,
    Notes         NVARCHAR(MAX) NULL,
    CONSTRAINT FK_CI_Res FOREIGN KEY (ReservationID)
        REFERENCES HotelReservations(ReservationID),
    CONSTRAINT FK_CI_User FOREIGN KEY (ProcessedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- Check Outs
CREATE TABLE CheckOuts (
    CheckOutID     UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ReservationID  UNIQUEIDENTIFIER NOT NULL UNIQUE,
    ActualCheckOut DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    FinalAmount    DECIMAL(14,2) NOT NULL DEFAULT 0,
    ProcessedBy    UNIQUEIDENTIFIER NULL,
    Notes          NVARCHAR(MAX) NULL,
    CONSTRAINT FK_CO_Res FOREIGN KEY (ReservationID)
        REFERENCES HotelReservations(ReservationID),
    CONSTRAINT FK_CO_User FOREIGN KEY (ProcessedBy)
        REFERENCES Users(UserID) ON DELETE SET NULL
);
GO

-- Indexes
CREATE INDEX IX_HotelRooms_Status ON HotelRooms(Status);
CREATE INDEX IX_HotelRooms_Floor  ON HotelRooms(FloorID);
CREATE INDEX IX_HRes_Guest        ON HotelReservations(GuestID);
CREATE INDEX IX_HRes_Room         ON HotelReservations(HotelRoomID);
CREATE INDEX IX_HRes_Status       ON HotelReservations(Status);
CREATE INDEX IX_HRes_Dates        ON HotelReservations(CheckInDate, CheckOutDate);
GO
