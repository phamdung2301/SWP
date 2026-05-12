


-- ============================================================
-- 1️⃣ CATEGORIES
-- ============================================================
INSERT INTO Categories (Name, Description) VALUES
(N'Cà phê', N'Các loại cà phê truyền thống và hiện đại'),
(N'Trà', N'Trà hoa quả, trà sữa, trà thảo mộc'),
(N'Sinh tố & Nước ép', N'Nước ép trái cây tươi, sinh tố dinh dưỡng'),
(N'Bánh ngọt', N'Bánh ngọt dùng kèm cà phê, trà'),
(N'Snack', N'Đồ ăn nhẹ kèm đồ uống');
GO


-- ============================================================
-- 2️⃣ PRODUCTS
-- ============================================================
    INSERT INTO Products (Name, Description, ImageURL, ProductType, Status, Unit) VALUES
-- Nhóm Dịch vụ (cà phê, trà - phục vụ trực tiếp tại quán)
(N'Cà phê đen', N'Cà phê phin truyền thống Việt Nam, đậm vị', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Dịch vụ', N'Đang bán', N'Ly'),
(N'Cà phê sữa đá', N'Cà phê phin pha sữa đặc, vị ngọt béo', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Dịch vụ', N'Đang bán', N'Ly'),
(N'Latte', N'Cà phê espresso với sữa nóng và lớp foam', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Dịch vụ', N'Đang bán', N'Ly'),
(N'Trà đào cam sả', N'Trà đào kết hợp cam và sả, vị thanh mát', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Dịch vụ', N'Đang bán', N'Ly'),
(N'Trà sữa trân châu', N'Trà sữa ngọt dịu kèm trân châu đen', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Dịch vụ', N'Đang bán', N'Ly'),

-- Nhóm Chế biến (thức ăn cần chế biến/pha chế)
(N'Sinh tố xoài', N'Sinh tố xoài tươi, ngọt mát', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Chế biến', N'Đang bán', N'Ly'),
(N'Nước ép cam', N'Nước cam vắt tươi nguyên chất', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Chế biến', N'Đang bán', N'Ly'),
(N'Khoai tây chiên', N'Khoai tây chiên giòn vàng', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Chế biến', N'Đang bán', N'Phần'),

-- Nhóm Hàng hóa thường (sản phẩm có sẵn, không cần chế biến)
(N'Bánh tiramisu', N'Bánh tiramisu Ý, mềm xốp, vị cà phê', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Hàng hóa thường', N'Đang bán', N'Miếng'),
(N'Croissant bơ', N'Bánh croissant Pháp, giòn thơm vị bơ', 'https://www.eatright.org/-/media/images/eatright-articles/eatright-article-feature-images/benefitsofcoffee_600x450.jpg?h=450&w=600&rev=6c8a9cd4a94d4cac8af8543054fd7b07&hash=F64F1F79DE48F33E3FB6A4FD5979C51F', N'Hàng hóa thường', N'Đang bán', N'Cái');
GO


-- ============================================================
-- 3️⃣ PRODUCT VARIANTS
-- ============================================================
-- Cà phê đen
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'S', 10000, 20000 FROM Products WHERE Name = N'Cà phê đen';
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'M', 15000, 25000 FROM Products WHERE Name = N'Cà phê đen';

-- Cà phê sữa đá
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'S', 13000, 25000 FROM Products WHERE Name = N'Cà phê sữa đá';
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'M', 18000, 30000 FROM Products WHERE Name = N'Cà phê sữa đá';

-- Latte
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'M', 25000, 40000 FROM Products WHERE Name = N'Latte';
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'L', 30000, 45000 FROM Products WHERE Name = N'Latte';

-- Trà đào cam sả
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'M', 20000, 35000 FROM Products WHERE Name = N'Trà đào cam sả';
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'L', 25000, 40000 FROM Products WHERE Name = N'Trà đào cam sả';

-- Trà sữa trân châu
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'M', 22000, 35000 FROM Products WHERE Name = N'Trà sữa trân châu';
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'L', 28000, 40000 FROM Products WHERE Name = N'Trà sữa trân châu';

-- Sinh tố xoài
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'M', 28000, 40000 FROM Products WHERE Name = N'Sinh tố xoài';

-- Nước ép cam
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, 'M', 18000, 30000 FROM Products WHERE Name = N'Nước ép cam';

-- Bánh tiramisu
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, N'1 miếng', 32000, 45000 FROM Products WHERE Name = N'Bánh tiramisu';

-- Croissant bơ
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, N'1 cái', 15000, 25000 FROM Products WHERE Name = N'Croissant bơ';

-- Khoai tây chiên
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, N'Phần nhỏ', 12000, 25000 FROM Products WHERE Name = N'Khoai tây chiên';
INSERT INTO ProductVariant (ProductID, Size, OriginalPrice, Price)
SELECT ProductID, N'Phần lớn', 20000, 35000 FROM Products WHERE Name = N'Khoai tây chiên';
GO


-- ============================================================
-- 4️⃣ PRODUCT - CATEGORY MAPPING
-- ============================================================
INSERT INTO ProductsCategories (ProductID, CategoryID)
SELECT p.ProductID, c.CategoryID FROM Products p JOIN Categories c ON c.Name = N'Cà phê'
WHERE p.Name IN (N'Cà phê đen', N'Cà phê sữa đá', N'Latte');

INSERT INTO ProductsCategories (ProductID, CategoryID)
SELECT p.ProductID, c.CategoryID FROM Products p JOIN Categories c ON c.Name = N'Trà'
WHERE p.Name IN (N'Trà đào cam sả', N'Trà sữa trân châu');

INSERT INTO ProductsCategories (ProductID, CategoryID)
SELECT p.ProductID, c.CategoryID FROM Products p JOIN Categories c ON c.Name = N'Sinh tố & Nước ép'
WHERE p.Name IN (N'Sinh tố xoài', N'Nước ép cam');

INSERT INTO ProductsCategories (ProductID, CategoryID)
SELECT p.ProductID, c.CategoryID FROM Products p JOIN Categories c ON c.Name = N'Bánh ngọt'
WHERE p.Name IN (N'Bánh tiramisu', N'Croissant bơ');

INSERT INTO ProductsCategories (ProductID, CategoryID)
SELECT p.ProductID, c.CategoryID FROM Products p JOIN Categories c ON c.Name = N'Snack'
WHERE p.Name IN (N'Khoai tây chiên');
GO


-- ============================================================
-- 5️⃣ INVENTORY & PRODUCT STOCK
-- ============================================================
INSERT INTO Inventory (StoreLocation) VALUES (N'Kho chính');

INSERT INTO ProductStock (ProductVariantID, InventoryID, Amount)
SELECT pv.ProductVariantID, i.InventoryID, 100
FROM ProductVariant pv CROSS JOIN Inventory i;
GO


-- ============================================================
-- 6️⃣ USERS, ROLES, USERROLES, ADMIN
-- ============================================================
-- USERS
INSERT INTO Users (Email, Phone, GoogleID, PasswordHash, TwoFactorSecret, DisplayName, IsActive, Meta)
SELECT 'owner@liteflow.vn', '0901000001', 'google-oauth2|1234567890', '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', '2FA1', N'Nguyễn Văn A - Owner', 1, N'{"role":"Owner"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'owner@liteflow.vn');

INSERT INTO Users (Email, Phone, GoogleID, PasswordHash, TwoFactorSecret, DisplayName, IsActive, Meta)
SELECT 'cashier1@liteflow.vn', '0901000002', NULL, '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', NULL, N'Trần Thị B - Cashier', 1, N'{"role":"Cashier"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'cashier1@liteflow.vn');

INSERT INTO Users (Email, Phone, GoogleID, PasswordHash, TwoFactorSecret, DisplayName, IsActive, Meta)
SELECT 'inventory@liteflow.vn', '0901000003', NULL, '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', '2FA3', N'Lê Văn C - Inventory', 1, N'{"role":"Inventory Manager"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'inventory@liteflow.vn');

INSERT INTO Users (Email, Phone, GoogleID, PasswordHash, TwoFactorSecret, DisplayName, IsActive, Meta)
SELECT 'procurement@liteflow.vn', '0901000004', NULL, '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', NULL, N'Phạm Thị D - Procurement', 1, N'{"role":"Procurement Officer"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'procurement@liteflow.vn');

INSERT INTO Users (Email, Phone, GoogleID, PasswordHash, TwoFactorSecret, DisplayName, IsActive, Meta)
SELECT 'hr@liteflow.vn', '0901000005', 'google-oauth2|987654321', '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', '2FA5', N'Hoàng Văn E - HR', 1, N'{"role":"HR Officer"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'hr@liteflow.vn');

INSERT INTO Users (Email, Phone, GoogleID, PasswordHash, TwoFactorSecret, DisplayName, IsActive, Meta)
SELECT 'giangducx2312@gmail.com', '0901000006', NULL, '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', NULL, N'Đỗ Thị F - Staff', 1, N'{"role":"Employee"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'giangducx2312@gmail.com');
GO

-- ROLES
INSERT INTO Roles (Name, Description) 
SELECT 'Owner', 'System owner/manager'
WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE Name = 'Owner');

INSERT INTO Roles (Name, Description) 
SELECT 'Cashier', 'Point of Sale operator'
WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE Name = 'Cashier');

INSERT INTO Roles (Name, Description) 
SELECT 'Inventory Manager', 'Manage stock and products'
WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE Name = 'Inventory Manager');

INSERT INTO Roles (Name, Description) 
SELECT 'Procurement Officer', 'Manage purchase orders and suppliers'
WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE Name = 'Procurement Officer');

INSERT INTO Roles (Name, Description) 
SELECT 'HR Officer', 'Handle HR and payroll'
WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE Name = 'HR Officer');

INSERT INTO Roles (Name, Description) 
SELECT 'Employee', 'General staff'
WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE Name = 'Employee');

INSERT INTO Roles (Name, Description) 
SELECT 'Admin', 'Administrator with full access'
WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE Name = 'Admin');
GO

-- USERROLES
INSERT INTO UserRoles (UserID, RoleID)
SELECT u.UserID, r.RoleID FROM Users u JOIN Roles r ON u.Meta LIKE '%Owner%' AND r.Name = 'Owner'
WHERE NOT EXISTS (SELECT 1 FROM UserRoles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID)
UNION ALL
SELECT u.UserID, r.RoleID FROM Users u JOIN Roles r ON u.Meta LIKE '%Cashier%' AND r.Name = 'Cashier'
WHERE NOT EXISTS (SELECT 1 FROM UserRoles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID)
UNION ALL
SELECT u.UserID, r.RoleID FROM Users u JOIN Roles r ON u.Meta LIKE '%Inventory%' AND r.Name = 'Inventory Manager'
WHERE NOT EXISTS (SELECT 1 FROM UserRoles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID)
UNION ALL
SELECT u.UserID, r.RoleID FROM Users u JOIN Roles r ON u.Meta LIKE '%Procurement%' AND r.Name = 'Procurement Officer'
WHERE NOT EXISTS (SELECT 1 FROM UserRoles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID)
UNION ALL
SELECT u.UserID, r.RoleID FROM Users u JOIN Roles r ON u.Meta LIKE '%HR%' AND r.Name = 'HR Officer'
WHERE NOT EXISTS (SELECT 1 FROM UserRoles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID)
UNION ALL
SELECT u.UserID, r.RoleID FROM Users u JOIN Roles r ON u.Meta LIKE '%Employee%' AND r.Name = 'Employee'
WHERE NOT EXISTS (SELECT 1 FROM UserRoles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID);
GO

-- ADMIN DEV ACCOUNT
DECLARE @AdminID UNIQUEIDENTIFIER = NEWID();
DECLARE @AdminEmail NVARCHAR(320) = N'admin@liteflow.com';
DECLARE @AdminHash NVARCHAR(MAX) = N'$2a$12$bSyne//LHFXF0lzzK4jBbu./rIRSHdTcH7VuMMQLk7U9hPMtK5BgC'; -- password = "1"

IF NOT EXISTS (SELECT 1 FROM Users WHERE LOWER(Email) = LOWER(@AdminEmail))
BEGIN
    INSERT INTO Users (UserID, Email, Phone, PasswordHash, DisplayName, IsActive, Meta)
    VALUES (@AdminID, @AdminEmail, N'0901000000', @AdminHash, N'LiteFlow Admin', 1, N'{}');
END
ELSE
BEGIN
    SELECT @AdminID = UserID FROM Users WHERE LOWER(Email) = LOWER(@AdminEmail);
    UPDATE Users
    SET PasswordHash = @AdminHash,
        IsActive = 1,
        DisplayName = N'LiteFlow Admin'
    WHERE UserID = @AdminID;
END

INSERT INTO UserRoles (UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM Users u
JOIN Roles r ON r.Name = 'Admin'
WHERE u.Email = 'admin@liteflow.com'
AND NOT EXISTS (SELECT 1 FROM UserRoles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID);
GO


-- ============================================================
-- 7️⃣ ROOMS & TABLES - QUÁN ĂN THỰC TẾ
-- ============================================================
INSERT INTO Rooms (Name, Description, TableCount, TotalCapacity) VALUES
(N'Khu Vực Lễ Tân', N'Khu vực gần lễ tân, tiện lợi cho khách hàng vãng lai', 8, 32),
(N'Phòng Gia Đình', N'Phòng dành cho gia đình, không gian ấm cúng, phù hợp cho trẻ em', 6, 24),
(N'Phòng Họp', N'Phòng họp riêng tư cho khách VIP, trang bị đầy đủ tiện nghi', 4, 20),
(N'Phòng Ngoài Trời', N'Khu vực ngoài trời, thoáng mát, phù hợp cho khách hàng thích không gian tự nhiên', 5, 20),
(N'Phòng Làm Việc', N'Phòng yên tĩnh cho khách hàng làm việc, có wifi tốt', 8, 32),
(N'Khu Vực Bar', N'Khu vực bar với không gian mở, phù hợp cho nhóm bạn', 6, 24),
(N'Phòng VIP', N'Phòng VIP cao cấp, không gian sang trọng, phục vụ khách hàng đặc biệt', 3, 12),
(N'Tầng 2', N'Tầng 2 với view đẹp, không gian yên tĩnh và riêng tư', 7, 28);
GO

INSERT INTO Tables (RoomID, TableNumber, TableName, Capacity, Status) VALUES
-- Khu Vực Lễ Tân (8 bàn)
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Lễ Tân'), 'LT-01', N'Bàn Lễ Tân 1', 2, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Lễ Tân'), 'LT-02', N'Bàn Lễ Tân 2', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Lễ Tân'), 'LT-03', N'Bàn Lễ Tân 3', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Lễ Tân'), 'LT-04', N'Bàn Lễ Tân 4', 6, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Lễ Tân'), 'LT-05', N'Bàn Lễ Tân 5', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Lễ Tân'), 'LT-06', N'Bàn Lễ Tân 6', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Lễ Tân'), 'LT-07', N'Bàn Lễ Tân 7', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Lễ Tân'), 'LT-08', N'Bàn Lễ Tân 8', 4, 'Available'),

-- Phòng Gia Đình (6 bàn)
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Gia Đình'), 'GD-01', N'Bàn Gia Đình 1', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Gia Đình'), 'GD-02', N'Bàn Gia Đình 2', 6, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Gia Đình'), 'GD-03', N'Bàn Gia Đình 3', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Gia Đình'), 'GD-04', N'Bàn Gia Đình 4', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Gia Đình'), 'GD-05', N'Bàn Gia Đình 5', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Gia Đình'), 'GD-06', N'Bàn Gia Đình 6', 2, 'Available'),

-- Phòng Họp (4 bàn)
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Họp'), 'H-01', N'Bàn Họp 1', 6, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Họp'), 'H-02', N'Bàn Họp 2', 6, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Họp'), 'H-03', N'Bàn Họp 3', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Họp'), 'H-04', N'Bàn Họp 4', 4, 'Available'),

-- Phòng Ngoài Trời (5 bàn)
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Ngoài Trời'), 'NT-01', N'Bàn Ngoài Trời 1', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Ngoài Trời'), 'NT-02', N'Bàn Ngoài Trời 2', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Ngoài Trời'), 'NT-03', N'Bàn Ngoài Trời 3', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Ngoài Trời'), 'NT-04', N'Bàn Ngoài Trời 4', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Ngoài Trời'), 'NT-05', N'Bàn Ngoài Trời 5', 4, 'Available'),

-- Phòng Làm Việc (8 bàn)
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Làm Việc'), 'LV-01', N'Bàn Làm Việc 1', 2, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Làm Việc'), 'LV-02', N'Bàn Làm Việc 2', 2, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Làm Việc'), 'LV-03', N'Bàn Làm Việc 3', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Làm Việc'), 'LV-04', N'Bàn Làm Việc 4', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Làm Việc'), 'LV-05', N'Bàn Làm Việc 5', 6, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Làm Việc'), 'LV-06', N'Bàn Làm Việc 6', 6, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Làm Việc'), 'LV-07', N'Bàn Làm Việc 7', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng Làm Việc'), 'LV-08', N'Bàn Làm Việc 8', 4, 'Available'),

-- Khu Vực Bar (6 bàn)
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Bar'), 'B-01', N'Bàn Bar 1', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Bar'), 'B-02', N'Bàn Bar 2', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Bar'), 'B-03', N'Bàn Bar 3', 6, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Bar'), 'B-04', N'Bàn Bar 4', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Bar'), 'B-05', N'Bàn Bar 5', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Khu Vực Bar'), 'B-06', N'Bàn Bar 6', 2, 'Available'),

-- Phòng VIP (3 bàn)
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng VIP'), 'VIP-01', N'Bàn VIP 1', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng VIP'), 'VIP-02', N'Bàn VIP 2', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Phòng VIP'), 'VIP-03', N'Bàn VIP 3', 4, 'Available'),

-- Tầng 2 (7 bàn)
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Tầng 2'), 'T2-01', N'Bàn Tầng 2 - 1', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Tầng 2'), 'T2-02', N'Bàn Tầng 2 - 2', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Tầng 2'), 'T2-03', N'Bàn Tầng 2 - 3', 6, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Tầng 2'), 'T2-04', N'Bàn Tầng 2 - 4', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Tầng 2'), 'T2-05', N'Bàn Tầng 2 - 5', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Tầng 2'), 'T2-06', N'Bàn Tầng 2 - 6', 4, 'Available'),
((SELECT TOP 1 RoomID FROM Rooms WHERE Name = N'Tầng 2'), 'T2-07', N'Bàn Tầng 2 - 7', 2, 'Available');
GO
-- ============================================================
-- 8️⃣ EMPLOYEES (Liên kết 1-1 với bảng Users)
-- ============================================================

INSERT INTO Employees 
(UserID, EmployeeCode, FullName, Gender, BirthDate, NationalID, Phone, Email, Address, AvatarURL, 
 HireDate, EmploymentStatus, Position, Salary, BankAccount, BankName, Notes)
SELECT 
    u.UserID,
    'EMP001',
    N'Nguyễn Văn A',
    N'Nam',
    '1990-03-15',
    '012345678901',
    u.Phone,
    u.Email,
    N'123 Nguyễn Trãi, Q.1, TP.HCM',
    N'images/employee_owner.jpg',
    '2020-01-10',
    N'Đang làm',
    N'Chủ quán / Owner',
    30000000,
    '0011223344',
    N'Vietcombank - CN TP.HCM',
    N'Người sáng lập và quản lý chính'
FROM Users u WHERE u.Email = 'owner@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP001');


INSERT INTO Employees 
(UserID, EmployeeCode, FullName, Gender, BirthDate, NationalID, Phone, Email, Address, AvatarURL, 
 HireDate, EmploymentStatus, Position, Salary, BankAccount, BankName, Notes)
SELECT 
    u.UserID,
    'EMP002',
    N'Trần Thị B',
    N'Nữ',
    '1995-07-20',
    '045678901234',
    u.Phone,
    u.Email,
    N'56 Lê Lợi, Q.1, TP.HCM',
    N'images/employee_cashier.jpg',
    '2022-05-01',
    N'Đang làm',
    N'Thu ngân / Cashier',
    12000000,
    '1234567890',
    N'Techcombank - CN Sài Gòn',
    N'Phụ trách thu ngân và POS'
FROM Users u WHERE u.Email = 'cashier1@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP002');

INSERT INTO Employees 
(UserID, EmployeeCode, FullName, Gender, BirthDate, NationalID, Phone, Email, Address, AvatarURL, 
 HireDate, EmploymentStatus, Position, Salary, BankAccount, BankName, Notes)
SELECT 
    u.UserID,
    'EMP003',
    N'Lê Văn C',
    N'Nam',
    '1992-11-02',
    '098765432109',
    u.Phone,
    u.Email,
    N'12 Nguyễn Văn Linh, Q.7, TP.HCM',
    N'images/employee_inventory.jpg',
    '2021-09-15',
    N'Đang làm',
    N'Quản lý kho / Inventory Manager',
    15000000,
    '222333444',
    N'ACB - CN Nam Sài Gòn',
    N'Kiểm soát nhập, xuất và tồn kho'
FROM Users u WHERE u.Email = 'inventory@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP003');

INSERT INTO Employees 
(UserID, EmployeeCode, FullName, Gender, BirthDate, NationalID, Phone, Email, Address, AvatarURL, 
 HireDate, EmploymentStatus, Position, Salary, BankAccount, BankName, Notes)
SELECT 
    u.UserID,
    'EMP004',
    N'Phạm Thị D',
    N'Nữ',
    '1994-06-05',
    '067890123456',
    u.Phone,
    u.Email,
    N'78 Võ Văn Kiệt, Q.5, TP.HCM',
    N'images/employee_procurement.jpg',
    '2021-03-01',
    N'Tạm nghỉ',
    N'Nhân viên mua hàng / Procurement Officer',
    13000000,
    '5566778899',
    N'Sacombank - CN Quận 5',
    N'Tạm nghỉ phép 2 tuần do lý do cá nhân'
FROM Users u WHERE u.Email = 'procurement@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP004');

INSERT INTO Employees 
(UserID, EmployeeCode, FullName, Gender, BirthDate, NationalID, Phone, Email, Address, AvatarURL, 
 HireDate, EmploymentStatus, Position, Salary, BankAccount, BankName, Notes)
SELECT 
    u.UserID,
    'EMP005',
    N'Hoàng Văn E',
    N'Nam',
    '1991-01-25',
    '054321098765',
    u.Phone,
    u.Email,
    N'21 Bạch Đằng, Bình Thạnh, TP.HCM',
    N'images/employee_hr.jpg',
    '2020-07-01',
    N'Đang làm',
    N'Nhân sự / HR Officer',
    14000000,
    '4455667788',
    N'Vietinbank - CN Bình Thạnh',
    N'Phụ trách chấm công, lương thưởng và tuyển dụng'
FROM Users u WHERE u.Email = 'hr@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP005');

INSERT INTO Employees 
(UserID, EmployeeCode, FullName, Gender, BirthDate, NationalID, Phone, Email, Address, AvatarURL, 
 HireDate, EmploymentStatus, Position, Salary, BankAccount, BankName, Notes)
SELECT 
    u.UserID,
    'EMP006',
    N'Đỗ Thị F',
    N'Nữ',
    '1999-09-15',
    '098123456789',
    u.Phone,
    u.Email,
    N'19 Hai Bà Trưng, Quận 3, TP.HCM',
    N'images/employee_staff.jpg',
    '2023-02-01',
    N'Đang làm',
    N'Nhân viên pha chế / Barista',
    10000000,
    '6677889900',
    N'MB Bank - CN Quận 3',
    N'Pha chế đồ uống, hỗ trợ khách hàng tại quầy'
FROM Users u WHERE u.Email = 'giangducx2312@gmail.com'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP006');
GO

-- ============================================================
-- 9️⃣.1 SCHEDULING - SHIFT TEMPLATES, ASSIGNMENTS, SHIFTS
-- ============================================================
-- Create shift templates
INSERT INTO ShiftTemplates (Name, Description, StartTime, EndTime, BreakMinutes, IsActive, CreatedBy)
SELECT N'Ca Sáng', N'7:00 - 12:00', '07:00', '12:00', 15, 1, u.UserID
FROM Users u WHERE u.Email = 'hr@liteflow.vn';

INSERT INTO ShiftTemplates (Name, Description, StartTime, EndTime, BreakMinutes, IsActive, CreatedBy)
SELECT N'Ca Chiều', N'12:00 - 17:00', '12:00', '17:00', 15, 1, u.UserID
FROM Users u WHERE u.Email = 'hr@liteflow.vn';

INSERT INTO ShiftTemplates (Name, Description, StartTime, EndTime, BreakMinutes, IsActive, CreatedBy)
SELECT N'Ca Tối', N'17:00 - 22:00', '17:00', '22:00', 15, 1, u.UserID
FROM Users u WHERE u.Email = 'hr@liteflow.vn';
GO

-- Assign templates to employees by weekdays
-- Employee1 (Barista): Ca Sáng từ Thứ 2 - Thứ 6
INSERT INTO EmployeeShiftAssignments (EmployeeID, TemplateID, Weekday, EffectiveFrom, EffectiveTo, IsActive, CreatedBy)
SELECT e.EmployeeID, t.TemplateID, v.Weekday, CAST(SYSDATETIME() AS DATE), NULL, 1, uHR.UserID
FROM Employees e
JOIN Users uEmp ON uEmp.UserID = e.UserID AND uEmp.Email = 'giangducx2312@gmail.com'
JOIN ShiftTemplates t ON t.Name = N'Ca Sáng'
CROSS JOIN (VALUES (1),(2),(3),(4),(5)) v(Weekday)
CROSS JOIN Users uHR
WHERE uHR.Email = 'hr@liteflow.vn';

-- Cashier: Ca Tối vào Thứ 2, 4, 6
INSERT INTO EmployeeShiftAssignments (EmployeeID, TemplateID, Weekday, EffectiveFrom, EffectiveTo, IsActive, CreatedBy)
SELECT e.EmployeeID, t.TemplateID, v.Weekday, CAST(SYSDATETIME() AS DATE), NULL, 1, uHR.UserID
FROM Employees e
JOIN Users uEmp ON uEmp.UserID = e.UserID AND uEmp.Email = 'cashier1@liteflow.vn'
JOIN ShiftTemplates t ON t.Name = N'Ca Tối'
CROSS JOIN (VALUES (1),(3),(5)) v(Weekday)
CROSS JOIN Users uHR
WHERE uHR.Email = 'hr@liteflow.vn';
GO

-- Concrete shifts for current week (sample)
DECLARE @HR UNIQUEIDENTIFIER = (SELECT TOP 1 UserID FROM Users WHERE Email = 'hr@liteflow.vn');
DECLARE @today DATE = CAST(SYSDATETIME() AS DATE);

-- Employee1 today morning shift
INSERT INTO EmployeeShifts (EmployeeID, Title, Notes, StartAt, EndAt, Location, Status, CreatedBy)
SELECT e.EmployeeID, N'Ca Sáng', N'Phân công mẫu',
       DATEADD(HOUR, 7, CAST(@today AS DATETIME2)),
       DATEADD(HOUR, 12, CAST(@today AS DATETIME2)),
       N'Cửa hàng chính', 'Scheduled', @HR
FROM Employees e
JOIN Users u ON u.UserID = e.UserID
WHERE u.Email = 'giangducx2312@gmail.com';

-- Employee1 tomorrow afternoon shift
INSERT INTO EmployeeShifts (EmployeeID, Title, Notes, StartAt, EndAt, Location, Status, CreatedBy)
SELECT e.EmployeeID, N'Ca Chiều', N'Phân công mẫu',
       DATEADD(HOUR, 12, DATEADD(DAY, 1, CAST(@today AS DATETIME2))),
       DATEADD(HOUR, 17, DATEADD(DAY, 1, CAST(@today AS DATETIME2))),
       N'Cửa hàng chính', 'Scheduled', @HR
FROM Employees e
JOIN Users u ON u.UserID = e.UserID
WHERE u.Email = 'giangducx2312@gmail.com';

-- Cashier day after tomorrow evening shift
INSERT INTO EmployeeShifts (EmployeeID, Title, Notes, StartAt, EndAt, Location, Status, CreatedBy)
SELECT e.EmployeeID, N'Ca Tối', N'Phân công mẫu',
       DATEADD(HOUR, 17, DATEADD(DAY, 2, CAST(@today AS DATETIME2))),
       DATEADD(HOUR, 22, DATEADD(DAY, 2, CAST(@today AS DATETIME2))),
       N'Cửa hàng chính', 'Scheduled', @HR
FROM Employees e
JOIN Users u ON u.UserID = e.UserID
WHERE u.Email = 'cashier1@liteflow.vn';
GO

-- ============================================================
-- 2️⃣ THÊM TABLE SESSIONS MẪU (Lịch sử giao dịch)
-- ============================================================

-- Session đã hoàn thành (Completed)
INSERT INTO TableSessions (TableID, CustomerName, CustomerPhone, CheckInTime, CheckOutTime, Status, TotalAmount, PaymentMethod, PaymentStatus, InvoiceName, CreatedBy)
SELECT 
    t.TableID,
    N'Nguyễn Văn An',
    '0901111111',
    DATEADD(HOUR, -24, SYSDATETIME()), -- 1 ngày trước
    DATEADD(HOUR, -23, SYSDATETIME()), -- 1 tiếng sau
    'Completed',
    150000,
    'Cash',
    'Paid',
    t.TableName + N' - HD 2', -- Hóa đơn thứ 2 của bàn này
    u.UserID
FROM Tables t
CROSS JOIN Users u
WHERE t.TableName = N'Bàn Lễ Tân 1' AND u.Email = 'cashier1@liteflow.vn';

-- Session đã hủy (Cancelled)
INSERT INTO TableSessions (TableID, CustomerName, CustomerPhone, CheckInTime, CheckOutTime, Status, TotalAmount, PaymentMethod, PaymentStatus, InvoiceName, CreatedBy)
SELECT 
    t.TableID,
    N'Trần Thị Bình',
    '0902222222',
    DATEADD(HOUR, -12, SYSDATETIME()), -- 12 tiếng trước
    DATEADD(HOUR, -11, SYSDATETIME()), -- 1 tiếng sau
    'Cancelled',
    75000,
    'Card',
    'Paid',
    t.TableName + N' - HD 2', -- Hóa đơn thứ 2 của bàn này
    u.UserID
FROM Tables t
CROSS JOIN Users u
WHERE t.TableName = N'Bàn Lễ Tân 2' AND u.Email = 'cashier1@liteflow.vn';

-- Session với khách hàng VIP
INSERT INTO TableSessions (TableID, CustomerName, CustomerPhone, CheckInTime, CheckOutTime, Status, TotalAmount, PaymentMethod, PaymentStatus, InvoiceName, CreatedBy)
SELECT 
    t.TableID,
    N'Lê Văn Cường',
    '0903333333',
    DATEADD(DAY, -3, SYSDATETIME()), -- 3 ngày trước
    DATEADD(DAY, -3, DATEADD(HOUR, 2, SYSDATETIME())), -- 2 tiếng sau
    'Completed',
    450000,
    'Transfer',
    'Paid',
    t.TableName + N' - HD 1', -- Hóa đơn đầu tiên của bàn VIP
    u.UserID
FROM Tables t
CROSS JOIN Users u
WHERE t.TableName = N'Bàn VIP 1' AND u.Email = 'cashier1@liteflow.vn';

-- Session với thanh toán một phần
INSERT INTO TableSessions (TableID, CustomerName, CustomerPhone, CheckInTime, CheckOutTime, Status, TotalAmount, PaymentMethod, PaymentStatus, InvoiceName, CreatedBy)
SELECT 
    t.TableID,
    N'Phạm Thị Dung',
    '0904444444',
    DATEADD(DAY, -2, SYSDATETIME()), -- 2 ngày trước
    DATEADD(DAY, -2, DATEADD(HOUR, 3, SYSDATETIME())), -- 3 tiếng sau
    'Completed',
    200000,
    'Cash',
    'Partial',
    t.TableName + N' - HD 1', -- Hóa đơn đầu tiên của bàn này
    u.UserID
FROM Tables t
CROSS JOIN Users u
WHERE t.TableName = N'Bàn Lễ Tân 3' AND u.Email = 'cashier1@liteflow.vn';

-- Session với ghi chú
INSERT INTO TableSessions (TableID, CustomerName, CustomerPhone, CheckInTime, CheckOutTime, Status, TotalAmount, PaymentMethod, PaymentStatus, Notes, InvoiceName, CreatedBy)
SELECT 
    t.TableID,
    N'Hoàng Văn Em',
    '0905555555',
    DATEADD(DAY, -1, SYSDATETIME()), -- 1 ngày trước
    DATEADD(DAY, -1, DATEADD(HOUR, 1, SYSDATETIME())), -- 1 tiếng sau
    'Completed',
    120000,
    'Wallet',
    'Paid',
    N'Khách hàng thân thiết, yêu cầu cà phê ít đường',
    t.TableName + N' - HD 1', -- Hóa đơn đầu tiên của bàn này
    u.UserID
FROM Tables t
CROSS JOIN Users u
WHERE t.TableName = N'Bàn Lễ Tân 4' AND u.Email = 'cashier1@liteflow.vn';

-- Session với khách vãng lai (không có thông tin)
INSERT INTO TableSessions (TableID, CustomerName, CustomerPhone, CheckInTime, CheckOutTime, Status, TotalAmount, PaymentMethod, PaymentStatus, InvoiceName, CreatedBy)
SELECT 
    t.TableID,
    NULL, -- Khách vãng lai
    NULL,
    DATEADD(HOUR, -6, SYSDATETIME()), -- 6 tiếng trước
    DATEADD(HOUR, -5, SYSDATETIME()), -- 1 tiếng sau
    'Completed',
    85000,
    'Cash',
    'Paid',
    t.TableName + N' - HD 1', -- Hóa đơn đầu tiên của bàn này
    u.UserID
FROM Tables t
CROSS JOIN Users u
WHERE t.TableName = N'Bàn Lễ Tân 5' AND u.Email = 'cashier1@liteflow.vn';

GO

-- ============================================================
-- 3️⃣ THÊM ORDERS VÀ ORDER DETAILS CHO CÁC SESSION
-- ============================================================

-- Order cho session đã hoàn thành
INSERT INTO Orders (SessionID, OrderNumber, SubTotal, VAT, TotalAmount, Status, CreatedBy)
SELECT 
    ts.SessionID,
    'ORD-HISTORY-001',
    135000,
    13500,
    148500,
    'Served',
    u.UserID
FROM TableSessions ts
CROSS JOIN Users u
WHERE ts.CustomerName = N'Nguyễn Văn An' AND u.Email = 'cashier1@liteflow.vn';

-- Order cho session VIP
INSERT INTO Orders (SessionID, OrderNumber, SubTotal, VAT, TotalAmount, Status, CreatedBy)
SELECT 
    ts.SessionID,
    'ORD-HISTORY-002',
    405000,
    40500,
    445500,
    'Served',
    u.UserID
FROM TableSessions ts
CROSS JOIN Users u
WHERE ts.CustomerName = N'Lê Văn Cường' AND u.Email = 'cashier1@liteflow.vn';

-- Order Details cho session đã hoàn thành
INSERT INTO OrderDetails (OrderID, ProductVariantID, Quantity, UnitPrice, TotalPrice, Status)
SELECT 
    o.OrderID,
    pv.ProductVariantID,
    3,
    pv.Price,
    pv.Price * 3,
    'Served'
FROM Orders o
CROSS JOIN Products p
CROSS JOIN ProductVariant pv
WHERE o.OrderNumber = 'ORD-HISTORY-001' 
    AND p.Name = N'Cà phê sữa đá' 
    AND pv.ProductID = p.ProductID 
    AND pv.Size = 'M';

INSERT INTO OrderDetails (OrderID, ProductVariantID, Quantity, UnitPrice, TotalPrice, Status)
SELECT 
    o.OrderID,
    pv.ProductVariantID,
    2,
    pv.Price,
    pv.Price * 2,
    'Served'
FROM Orders o
CROSS JOIN Products p
CROSS JOIN ProductVariant pv
WHERE o.OrderNumber = 'ORD-HISTORY-001' 
    AND p.Name = N'Bánh tiramisu' 
    AND pv.ProductID = p.ProductID 
    AND pv.Size = N'1 miếng';

-- Order Details cho session VIP
INSERT INTO OrderDetails (OrderID, ProductVariantID, Quantity, UnitPrice, TotalPrice, Status)
SELECT 
    o.OrderID,
    pv.ProductVariantID,
    5,
    pv.Price,
    pv.Price * 5,
    'Served'
FROM Orders o
CROSS JOIN Products p
CROSS JOIN ProductVariant pv
WHERE o.OrderNumber = 'ORD-HISTORY-002' 
    AND p.Name = N'Latte' 
    AND pv.ProductID = p.ProductID 
    AND pv.Size = 'L';

INSERT INTO OrderDetails (OrderID, ProductVariantID, Quantity, UnitPrice, TotalPrice, Status)
SELECT 
    o.OrderID,
    pv.ProductVariantID,
    3,
    pv.Price,
    pv.Price * 3,
    'Served'
FROM Orders o
CROSS JOIN Products p
CROSS JOIN ProductVariant pv
WHERE o.OrderNumber = 'ORD-HISTORY-002' 
    AND p.Name = N'Bánh tiramisu' 
    AND pv.ProductID = p.ProductID 
    AND pv.Size = N'1 miếng';

GO

-- ============================================================
-- 4️⃣ THÊM PAYMENT TRANSACTIONS
-- ============================================================

-- Payment cho session đã hoàn thành
INSERT INTO PaymentTransactions (SessionID, OrderID, Amount, PaymentMethod, PaymentStatus, ProcessedBy)
SELECT 
    ts.SessionID,
    o.OrderID,
    o.TotalAmount,
    'Cash',
    'Completed',
    u.UserID
FROM TableSessions ts
CROSS JOIN Orders o
CROSS JOIN Users u
WHERE ts.CustomerName = N'Nguyễn Văn An' 
    AND o.OrderNumber = 'ORD-HISTORY-001'
    AND u.Email = 'cashier1@liteflow.vn';

-- Payment cho session VIP
INSERT INTO PaymentTransactions (SessionID, OrderID, Amount, PaymentMethod, PaymentStatus, ProcessedBy)
SELECT 
    ts.SessionID,
    o.OrderID,
    o.TotalAmount,
    'Transfer',
    'Completed',
    u.UserID
FROM TableSessions ts
CROSS JOIN Orders o
CROSS JOIN Users u
WHERE ts.CustomerName = N'Lê Văn Cường' 
    AND o.OrderNumber = 'ORD-HISTORY-002'
    AND u.Email = 'cashier1@liteflow.vn';

GO


-- ============================================================
-- 5️⃣ CẬP NHẬT TRẠNG THÁI BÀN
-- ============================================================

-- Cập nhật trạng thái bàn dựa trên sessions
UPDATE Tables 
SET Status = 'Occupied'
WHERE TableID IN (
    SELECT DISTINCT ts.TableID 
    FROM TableSessions ts 
    WHERE ts.Status = 'Active'
);

-- Cập nhật một số bàn thành Reserved để test
UPDATE Tables 
SET Status = 'Reserved'
WHERE TableName IN ('Bàn Ngoài Trời 3', 'Bàn Họp 2');

-- Cập nhật một số bàn thành Maintenance để test
UPDATE Tables 
SET Status = 'Maintenance'
WHERE TableName IN ('Bàn Ngoài Trời 4');

-- Cập nhật một số bàn thành Available để test
UPDATE Tables 
SET Status = 'Available'
WHERE TableName IN ('Bàn Ngoài Trời 1', 'Bàn Họp 1', 'Bàn Họp 3', 'Bàn Lễ Tân 1', 'Bàn Lễ Tân 3');

GO

-- ============================================================
-- 5️⃣.1 RESERVATIONS - DỮ LIỆU MẪU ĐẶT BÀN
-- ============================================================

-- Đặt bàn cho hôm nay (đã xác nhận, có gán bàn)
INSERT INTO Reservations (ReservationCode, CustomerName, CustomerPhone, CustomerEmail, ArrivalTime, NumberOfGuests, TableID, RoomID, Status, Notes)
SELECT 
    FORMAT(CAST(SYSDATETIME() AS DATE), 'ddMMyyyy') + '-001',
    N'Nguyễn Văn Hưng',
    '0901234567',
    'hung.nguyen@email.com',
    DATEADD(HOUR, 3, SYSDATETIME()), -- 3 giờ sau
    4,
    t.TableID,
    r.RoomID,
    
    'PENDING',
    N'Đặt bàn gia đình, cần ghế em bé'
FROM Tables t
JOIN Rooms r ON t.RoomID = r.RoomID
WHERE t.TableName = N'Bàn Gia Đình 2';

-- Lấy ReservationID vừa tạo để thêm món đặt trước
DECLARE @Res1ID UNIQUEIDENTIFIER = (SELECT TOP 1 ReservationID FROM Reservations WHERE CustomerPhone = '0901234567');

-- Thêm món đặt trước
INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res1ID, p.ProductID, 2, N'Ít đường'
FROM Products p WHERE p.Name = N'Cà phê sữa đá';

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res1ID, p.ProductID, 4, NULL
FROM Products p WHERE p.Name = N'Trà sữa trân châu';

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res1ID, p.ProductID, 2, N'Không cay'
FROM Products p WHERE p.Name = N'Khoai tây chiên';

-- Đặt bàn cho ngày mai (chưa gán bàn)
INSERT INTO Reservations (ReservationCode, CustomerName, CustomerPhone, CustomerEmail, ArrivalTime, NumberOfGuests, Status, Notes)
VALUES (
    FORMAT(DATEADD(DAY, 1, CAST(SYSDATETIME() AS DATE)), 'ddMMyyyy') + '-001',
    N'Trần Thị Mai',
    '0912345678',
    'mai.tran@email.com',
    DATEADD(HOUR, 19, CAST(DATEADD(DAY, 1, SYSDATETIME()) AS DATETIME2)), -- Ngày mai 7PM
    6,
    
    'PENDING',
    N'Sinh nhật, cần không gian riêng tư'
);

-- Thêm món đặt trước cho đặt bàn ngày mai
DECLARE @Res2ID UNIQUEIDENTIFIER = (SELECT TOP 1 ReservationID FROM Reservations WHERE CustomerPhone = '0912345678');

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res2ID, p.ProductID, 6, NULL
FROM Products p WHERE p.Name = N'Latte';

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res2ID, p.ProductID, 3, NULL
FROM Products p WHERE p.Name = N'Bánh tiramisu';

-- Đặt bàn VIP (hôm nay, buổi tối, đã gán bàn VIP)
INSERT INTO Reservations (ReservationCode, CustomerName, CustomerPhone, CustomerEmail, ArrivalTime, NumberOfGuests, TableID, RoomID, Status, Notes)
SELECT 
    FORMAT(CAST(SYSDATETIME() AS DATE), 'ddMMyyyy') + '-002',
    N'Lê Văn Thành',
    '0923456789',
    'thanh.le@company.com',
    DATEADD(HOUR, 19, CAST(SYSDATETIME() AS DATETIME2)), -- Hôm nay 7PM
    4,
    t.TableID,
    r.RoomID,
    
    'PENDING',
    N'Khách VIP, yêu cầu phục vụ đặc biệt'
FROM Tables t
JOIN Rooms r ON t.RoomID = r.RoomID
WHERE t.TableName = N'Bàn VIP 1';

DECLARE @Res3ID UNIQUEIDENTIFIER = (SELECT TOP 1 ReservationID FROM Reservations WHERE CustomerPhone = '0923456789');

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res3ID, p.ProductID, 4, N'Nóng'
FROM Products p WHERE p.Name = N'Latte';

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res3ID, p.ProductID, 4, NULL
FROM Products p WHERE p.Name = N'Bánh tiramisu';

-- Đặt bàn đã hoàn thành (hôm qua)
INSERT INTO Reservations (ReservationCode, CustomerName, CustomerPhone, CustomerEmail, ArrivalTime, NumberOfGuests, TableID, RoomID, Status, Notes)
SELECT 
    FORMAT(DATEADD(DAY, -1, CAST(SYSDATETIME() AS DATE)), 'ddMMyyyy') + '-001',
    N'Phạm Minh Tuấn',
    '0934567890',
    NULL, -- Không có email
    DATEADD(HOUR, 12, CAST(DATEADD(DAY, -1, SYSDATETIME()) AS DATETIME2)), -- Hôm qua 12PM
    2,
    t.TableID,
    r.RoomID,
    
    'SEATED',
    N'Đã nhận bàn và phục vụ'
FROM Tables t
JOIN Rooms r ON t.RoomID = r.RoomID
WHERE t.TableName = N'Bàn Lễ Tân 2';

DECLARE @Res4ID UNIQUEIDENTIFIER = (SELECT TOP 1 ReservationID FROM Reservations WHERE CustomerPhone = '0934567890');

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res4ID, p.ProductID, 2, NULL
FROM Products p WHERE p.Name = N'Cà phê đen';

-- Đặt bàn bị hủy
INSERT INTO Reservations (ReservationCode, CustomerName, CustomerPhone, ArrivalTime, NumberOfGuests, Status, Notes)
VALUES (
    FORMAT(DATEADD(DAY, -2, CAST(SYSDATETIME() AS DATE)), 'ddMMyyyy') + '-001',
    N'Hoàng Thị Lan',
    '0945678901',
    DATEADD(HOUR, 18, CAST(DATEADD(DAY, -2, SYSDATETIME()) AS DATETIME2)), -- 2 ngày trước 6PM
    3,
    
    'CANCELLED',
    N'Khách hủy do lý do cá nhân'
);

-- Đặt bàn không đến (NO_SHOW)
INSERT INTO Reservations (ReservationCode, CustomerName, CustomerPhone, ArrivalTime, NumberOfGuests, TableID, RoomID, Status, Notes)
SELECT 
    FORMAT(DATEADD(DAY, -1, CAST(SYSDATETIME() AS DATE)), 'ddMMyyyy') + '-002',
    N'Đỗ Văn Khoa',
    '0956789012',
    DATEADD(HOUR, 19, CAST(DATEADD(DAY, -1, SYSDATETIME()) AS DATETIME2)), -- Hôm qua 7PM
    5,
    t.TableID,
    r.RoomID,
    
    'NO_SHOW',
    N'Khách không đến, quá 30 phút tự động hủy'
FROM Tables t
JOIN Rooms r ON t.RoomID = r.RoomID
WHERE t.TableName = N'Bàn Họp 1';

-- Đặt bàn cho tuần sau (nhiều khách, chưa gán bàn)
INSERT INTO Reservations (ReservationCode, CustomerName, CustomerPhone, CustomerEmail, ArrivalTime, NumberOfGuests, Status, Notes)
VALUES (
    FORMAT(DATEADD(DAY, 7, CAST(SYSDATETIME() AS DATE)), 'ddMMyyyy') + '-001',
    N'Vũ Thị Hồng',
    '0967890123',
    'hong.vu@email.com',
    DATEADD(HOUR, 12, CAST(DATEADD(DAY, 7, SYSDATETIME()) AS DATETIME2)), -- Tuần sau 12PM
    10,
    
    'PENDING',
    N'Tiệc công ty, cần không gian lớn'
);

DECLARE @Res7ID UNIQUEIDENTIFIER = (SELECT TOP 1 ReservationID FROM Reservations WHERE CustomerPhone = '0967890123');

-- Đặt nhiều món
INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res7ID, p.ProductID, 10, NULL
FROM Products p WHERE p.Name = N'Cà phê sữa đá';

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res7ID, p.ProductID, 10, NULL
FROM Products p WHERE p.Name = N'Trà đào cam sả';

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res7ID, p.ProductID, 5, NULL
FROM Products p WHERE p.Name = N'Croissant bơ';

INSERT INTO ReservationItems (ReservationID, ProductID, Quantity, Note)
SELECT @Res7ID, p.ProductID, 3, N'Phần lớn'
FROM Products p WHERE p.Name = N'Khoai tây chiên';

GO

-- ============================================================
-- 6️⃣ THÊM DỮ LIỆU TEST CHO CÁC TRƯỜNG HỢP ĐẶC BIỆT
-- ============================================================

-- Session với null values để test error handling
INSERT INTO TableSessions (TableID, CustomerName, CustomerPhone, CheckInTime, CheckOutTime, Status, TotalAmount, PaymentMethod, PaymentStatus, InvoiceName, CreatedBy)
SELECT 
    t.TableID,
    N'Test Null Values',
    NULL,
    DATEADD(HOUR, -1, SYSDATETIME()),
    NULL, -- CheckOutTime null để test
    'Active',
    NULL, -- TotalAmount null để test
    NULL, -- PaymentMethod null để test
    'Unpaid',
    t.TableName + N' - HD 1', -- Test invoice name
    u.UserID
FROM Tables t
CROSS JOIN Users u
WHERE t.TableName = N'Bàn Lễ Tân 6' AND u.Email = 'cashier1@liteflow.vn';

-- Session với số tiền 0
INSERT INTO TableSessions (TableID, CustomerName, CustomerPhone, CheckInTime, CheckOutTime, Status, TotalAmount, PaymentMethod, PaymentStatus, InvoiceName, CreatedBy)
SELECT 
    t.TableID,
    N'Khách Hàng Miễn Phí',
    '0900000000',
    DATEADD(HOUR, -3, SYSDATETIME()),
    DATEADD(HOUR, -2, SYSDATETIME()),
    'Completed',
    0, -- Số tiền 0
    'Cash',
    'Paid',
    t.TableName + N' - HD 1', -- Test invoice name
    u.UserID
FROM Tables t
CROSS JOIN Users u
WHERE t.TableName = N'Bàn Lễ Tân 7' AND u.Email = 'cashier1@liteflow.vn';

GO

-- ============================================================
-- 7️⃣ THÊM DỮ LIỆU TEST CHO PERFORMANCE
-- ============================================================

-- Thêm nhiều sessions cũ để test pagination và performance
DECLARE @counter INT = 1;
DECLARE @maxCounter INT = 50;

WHILE @counter <= @maxCounter
BEGIN
    INSERT INTO TableSessions (TableID, CustomerName, CustomerPhone, CheckInTime, CheckOutTime, Status, TotalAmount, PaymentMethod, PaymentStatus, InvoiceName, CreatedBy)
    SELECT 
        t.TableID,
        N'Khách Hàng Test ' + CAST(@counter AS NVARCHAR(10)),
        '090' + RIGHT('0000000' + CAST(@counter AS NVARCHAR(10)), 7),
        DATEADD(DAY, -@counter, SYSDATETIME()),
        DATEADD(DAY, -@counter, DATEADD(HOUR, 2, SYSDATETIME())),
        'Completed',
        50000 + (@counter * 1000),
        CASE (@counter % 4)
            WHEN 0 THEN 'Cash'
            WHEN 1 THEN 'Card'
            WHEN 2 THEN 'Transfer'
            ELSE 'Wallet'
        END,
        'Paid',
        t.TableName + N' - HD ' + CAST((@counter + 2) AS NVARCHAR(10)), -- HD 3, HD 4, HD 5, ...
        u.UserID
    FROM Tables t
    CROSS JOIN Users u
    WHERE t.TableName = N'Bàn Lễ Tân 1' AND u.Email = 'cashier1@liteflow.vn';
    
    SET @counter = @counter + 1;
END

GO

-- ============================================================
-- 8️⃣ ATTENDANCE DATA - KHÔNG CÓ DỮ LIỆU MẪU
-- ============================================================
-- ⚠️ QUAN TRỌNG: Không seed dữ liệu EmployeeAttendance
-- Lý do: 
-- 1. Dữ liệu attendance sẽ được tạo TỰ ĐỘNG khi nhân viên clock-in/out từ dashboard
-- 2. Flags (isLate, isEarlyLeave, isOvertime) được tính toán TỰ ĐỘNG dựa trên shift times
-- 3. Việc seed dữ liệu cũ với flags sai sẽ gây lỗi màu sắc hiển thị không đúng
-- 
-- Cách tạo dữ liệu attendance:
-- - Nhân viên đăng nhập vào dashboard-employee
-- - Sử dụng widget "CHẤM CÔNG HÔM NAY" để clock-in/clock-out
-- - Hệ thống sẽ tự động tính toán và lưu flags chính xác

-- ============================================================
-- 9️⃣ SEED BONUS/PENALTY EVENTS FOR CURRENT WEEK
-- ============================================================
USE LiteFlowDBO;
GO

-- ============================================================
-- 🔟 TIMESHEET DATA - KHÔNG CÓ DỮ LIỆU MẪU
-- ============================================================
-- ⚠️ QUAN TRỌNG: Không seed dữ liệu EmployeeShiftTimesheets
-- Lý do:
-- 1. Timesheet được tạo TỰ ĐỘNG khi nhân viên clock-in/out
-- 2. Được tích hợp với EmployeeShifts để tính toán vi phạm và tăng ca
-- 3. HoursWorked được tính tự động từ check-in đến check-out
-- 
-- Timesheet sẽ xuất hiện trên trang /attendance sau khi nhân viên chấm công

DECLARE @HRUser UNIQUEIDENTIFIER = (SELECT TOP 1 UserID FROM Users WHERE Email = 'hr@liteflow.vn');

-- Recompute week anchors and employee IDs in this batch
DECLARE @Mon DATE;
DECLARE @today2 DATE = CAST(SYSDATETIME() AS DATE);
SET @Mon = DATEADD(DAY, -((DATEPART(WEEKDAY, @today2) + 5) % 7), @today2);

DECLARE @EmpBarista UNIQUEIDENTIFIER = (
  SELECT e.EmployeeID FROM Employees e JOIN Users u ON u.UserID = e.UserID AND u.Email = 'giangducx2312@gmail.com'
);

DECLARE @EmpCashier UNIQUEIDENTIFIER = (
  SELECT e.EmployeeID FROM Employees e JOIN Users u ON u.UserID = e.UserID AND u.Email = 'cashier1@liteflow.vn'
);

IF @EmpBarista IS NOT NULL
BEGIN
  INSERT INTO EmployeeCompEvents (EmployeeID, WorkDate, EventType, Amount, Reason, CreatedBy)
  VALUES (@EmpBarista, DATEADD(DAY, 1, @Mon), 'Bonus', 50000, N'Thưởng hiệu suất ca sáng', @HRUser);

  INSERT INTO EmployeeCompEvents (EmployeeID, WorkDate, EventType, Amount, Reason, CreatedBy)
  VALUES (@EmpBarista, DATEADD(DAY, 4, @Mon), 'Penalty', 20000, N'Đi trễ 10 phút', @HRUser);
END

IF @EmpCashier IS NOT NULL
BEGIN
  INSERT INTO EmployeeCompEvents (EmployeeID, WorkDate, EventType, Amount, Reason, CreatedBy)
  VALUES (@EmpCashier, DATEADD(DAY, 3, @Mon), 'Bonus', 30000, N'Hỗ trợ đóng ca tối', @HRUser);
END
GO
-- ============================================================
-- 1️⃣0 PAYROLL & COMPENSATION - SAMPLE DATA
-- ============================================================

-- Create sample Pay Policies
INSERT INTO PayPolicies (Name, Description, OvertimeMultiplier, NightShiftMultiplier, WeekendMultiplier, HolidayMultiplier, MinBreakMinutes, Currency, IsActive, CreatedBy)
SELECT N'Chính sách chuẩn VN', N'Mặc định cho nhân viên toàn thời gian', 1.5, 1.2, 1.5, 2.0, 15, 'VND', 1, u.UserID
FROM Users u WHERE u.Email = 'hr@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM PayPolicies WHERE Name = N'Chính sách chuẩn VN');

INSERT INTO PayPolicies (Name, Description, OvertimeMultiplier, NightShiftMultiplier, WeekendMultiplier, HolidayMultiplier, MinBreakMinutes, Currency, IsActive, CreatedBy)
SELECT N'Chính sách tăng cuối tuần', N'Tăng lương giờ cho cuối tuần', 1.5, 1.2, 1.75, 2.0, 15, 'VND', 1, u.UserID
FROM Users u WHERE u.Email = 'hr@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM PayPolicies WHERE Name = N'Chính sách tăng cuối tuần');
GO

-- Per-employee compensation configurations
INSERT INTO EmployeeCompensation (EmployeeID, CompensationType, PolicyID, BaseMonthlySalary, HourlyRate, PerShiftRate, Currency, EffectiveFrom, EffectiveTo, IsActive, Notes, CreatedBy)
SELECT e.EmployeeID, 'Fixed', p.PolicyID, 12000000, NULL, NULL, 'VND', CAST(SYSDATETIME() AS DATE), NULL, 1, N'Thu nhập cố định', uHR.UserID
FROM Employees e
JOIN Users uEmp ON uEmp.UserID = e.UserID AND uEmp.Email = 'cashier1@liteflow.vn'
JOIN PayPolicies p ON p.Name = N'Chính sách chuẩn VN'
CROSS JOIN Users uHR
WHERE uHR.Email = 'hr@liteflow.vn';

INSERT INTO EmployeeCompensation (EmployeeID, CompensationType, PolicyID, BaseMonthlySalary, HourlyRate, PerShiftRate, Currency, EffectiveFrom, EffectiveTo, IsActive, Notes, CreatedBy)
SELECT e.EmployeeID, 'PerShift', p.PolicyID, NULL, NULL, 100000, 'VND', CAST(SYSDATETIME() AS DATE), NULL, 1, N'Pha chế tính theo ca', uHR.UserID
FROM Employees e
JOIN Users uEmp ON uEmp.UserID = e.UserID AND uEmp.Email = 'giangducx2312@gmail.com'
JOIN PayPolicies p ON p.Name = N'Chính sách chuẩn VN'
CROSS JOIN Users uHR
WHERE uHR.Email = 'hr@liteflow.vn';

INSERT INTO EmployeeCompensation (EmployeeID, CompensationType, PolicyID, BaseMonthlySalary, HourlyRate, PerShiftRate, Currency, EffectiveFrom, EffectiveTo, IsActive, Notes, CreatedBy)
SELECT e.EmployeeID, 'Hybrid', p.PolicyID, 8000000, 20000, NULL, 'VND', CAST(SYSDATETIME() AS DATE), NULL, 1, N'Kho: lương cơ bản + theo giờ', uHR.UserID
FROM Employees e
JOIN Users uEmp ON uEmp.UserID = e.UserID AND uEmp.Email = 'inventory@liteflow.vn'
JOIN PayPolicies p ON p.Name = N'Chính sách chuẩn VN'
CROSS JOIN Users uHR
WHERE uHR.Email = 'hr@liteflow.vn';
GO

-- Shift pay rules (by template/position, with weekend uplift)
INSERT INTO ShiftPayRules (TemplateID, Position, DayType, RateType, Rate, Currency, EffectiveFrom, EffectiveTo, IsActive, Notes)
SELECT t.TemplateID, N'Nhân viên pha chế / Barista', 'Weekday', 'Hourly', 25000, 'VND', CAST(SYSDATETIME() AS DATE), NULL, 1, N'Giờ ngày thường'
FROM ShiftTemplates t WHERE t.Name = N'Ca Sáng';

INSERT INTO ShiftPayRules (TemplateID, Position, DayType, RateType, Rate, Currency, EffectiveFrom, EffectiveTo, IsActive, Notes)
SELECT t.TemplateID, N'Nhân viên pha chế / Barista', 'Weekend', 'Hourly', 30000, 'VND', CAST(SYSDATETIME() AS DATE), NULL, 1, N'Giờ cuối tuần'
FROM ShiftTemplates t WHERE t.Name = N'Ca Sáng';

INSERT INTO ShiftPayRules (TemplateID, Position, DayType, RateType, Rate, Currency, EffectiveFrom, EffectiveTo, IsActive, Notes)
SELECT t.TemplateID, N'Thu ngân / Cashier', 'Any', 'Hourly', 28000, 'VND', CAST(SYSDATETIME() AS DATE), NULL, 1, N'Áp dụng mọi ngày'
FROM ShiftTemplates t WHERE t.Name = N'Ca Tối';
GO

-- Timesheets will be created automatically via clock-in/out feature
GO

-- Holidays and exchange rates
INSERT INTO HolidayCalendar (HolidayDate, Name, Region, DayType, IsPaidHoliday)
SELECT '2025-09-02', N'Quốc khánh Việt Nam', 'VN', 'Public', 1
WHERE NOT EXISTS (SELECT 1 FROM HolidayCalendar WHERE HolidayDate = '2025-09-02' AND Region = 'VN');

INSERT INTO ExchangeRates (Currency, RateToVND, RateDate, Source)
SELECT 'USD', 24800.000000, CAST(SYSDATETIME() AS DATE), N'Static Sample'
WHERE NOT EXISTS (SELECT 1 FROM ExchangeRates WHERE Currency = 'USD' AND RateDate = CAST(SYSDATETIME() AS DATE));

INSERT INTO ExchangeRates (Currency, RateToVND, RateDate, Source)
SELECT 'VND', 1.000000, CAST(SYSDATETIME() AS DATE), N'Parity'
WHERE NOT EXISTS (SELECT 1 FROM ExchangeRates WHERE Currency = 'VND' AND RateDate = CAST(SYSDATETIME() AS DATE));
GO

-- Pay period for current month and a payroll run
DECLARE @StartOfMonth DATE = DATEADD(DAY, 1, EOMONTH(SYSDATETIME(), -1));
DECLARE @EndOfMonth DATE = EOMONTH(SYSDATETIME(), 0);

DECLARE @PeriodID UNIQUEIDENTIFIER = NEWID();
INSERT INTO PayPeriods (PayPeriodID, Name, PeriodType, StartDate, EndDate, Status)
VALUES (@PeriodID, FORMAT(@StartOfMonth, 'yyyy-MM') + N' - Kỳ lương', 'Monthly', @StartOfMonth, @EndOfMonth, 'Open');

DECLARE @RunID UNIQUEIDENTIFIER = NEWID();
INSERT INTO PayrollRuns (PayrollRunID, PayPeriodID, RunNumber, Status, CalculatedAt, Notes)
VALUES (@RunID, @PeriodID, 1, 'Calculated', SYSDATETIME(), N'Chạy lương mẫu cho kỳ hiện tại');

-- Payroll entries for two employees (same batch to keep @RunID)
INSERT INTO PayrollEntries (PayrollRunID, EmployeeID, CompensationType, BaseSalary, HourlyRate, PerShiftRate, HoursWorked, ShiftsWorked, OvertimeHours, HolidayHours, Allowances, Bonuses, Deductions, GrossPay, NetPay)
SELECT @RunID, e.EmployeeID, 'Fixed', 12000000, NULL, NULL, NULL, NULL, 0, 0, 0, 500000, 0, 12500000, 12500000
FROM Employees e JOIN Users u ON u.UserID = e.UserID AND u.Email = 'cashier1@liteflow.vn';

INSERT INTO PayrollEntries (PayrollRunID, EmployeeID, CompensationType, BaseSalary, HourlyRate, PerShiftRate, HoursWorked, ShiftsWorked, OvertimeHours, HolidayHours, Allowances, Bonuses, Deductions, GrossPay, NetPay)
SELECT @RunID, e.EmployeeID, 'PerShift', NULL, NULL, 100000, 4.75, 1, 0, 0, 0, 0, 0, 100000, 100000
FROM Employees e JOIN Users u ON u.UserID = e.UserID AND u.Email = 'giangducx2312@gmail.com';

-- One payroll adjustment (allowance) for barista (same batch)
INSERT INTO PayrollAdjustments (PayrollRunID, EmployeeID, AdjustmentType, Amount, Reason, CreatedBy, CreatedAt)
SELECT @RunID, e.EmployeeID, 'Allowance', 50000, N'Phụ cấp chuyên cần', uHR.UserID, SYSDATETIME()
FROM Employees e
JOIN Users u ON u.UserID = e.UserID AND u.Email = 'giangducx2312@gmail.com'
CROSS JOIN Users uHR
WHERE uHR.Email = 'hr@liteflow.vn';
GO

-- ============================================================
-- SAMPLE EMPLOYEES FOR TESTING (With User accounts)
-- ============================================================

-- Create Users first for EMP007-010
INSERT INTO Users (Email, Phone, PasswordHash, DisplayName, IsActive, Meta)
SELECT 'giang.do@liteflow.vn', '0901234573', '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', N'Đỗ Văn Giang - Đầu bếp', 1, N'{"role":"Employee"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'giang.do@liteflow.vn');

INSERT INTO Users (Email, Phone, PasswordHash, DisplayName, IsActive, Meta)
SELECT 'ha.bui@liteflow.vn', '0901234574', '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', N'Bùi Thị Hà - Phục vụ', 1, N'{"role":"Employee"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'ha.bui@liteflow.vn');

INSERT INTO Users (Email, Phone, PasswordHash, DisplayName, IsActive, Meta)
SELECT 'ich.dinh@liteflow.vn', '0901234575', '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', N'Đinh Văn Ích - Bảo vệ', 1, N'{"role":"Employee"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'ich.dinh@liteflow.vn');

INSERT INTO Users (Email, Phone, PasswordHash, DisplayName, IsActive, Meta)
SELECT 'kim.ngo@liteflow.vn', '0901234576', '$2a$12$CrcHqEZraWVdxVOSE2w28uT2NVJjrxDekdHKsXygHbGpMiUCXhmUW', N'Ngô Thị Kim - Kế toán', 1, N'{"role":"Employee"}'
WHERE NOT EXISTS (SELECT 1 FROM Users WHERE Email = 'kim.ngo@liteflow.vn');
GO

-- Assign Employee role to these users
INSERT INTO UserRoles (UserID, RoleID)
SELECT u.UserID, r.RoleID 
FROM Users u 
JOIN Roles r ON r.Name = 'Employee'
WHERE u.Email IN ('giang.do@liteflow.vn', 'ha.bui@liteflow.vn', 'ich.dinh@liteflow.vn', 'kim.ngo@liteflow.vn')
AND NOT EXISTS (SELECT 1 FROM UserRoles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID);
GO

-- EMP007 - Đầu bếp
INSERT INTO Employees (UserID, EmployeeCode, FullName, Phone, Position, Gender, EmploymentStatus, HireDate)
SELECT u.UserID, 'EMP007', N'Đỗ Văn Giang', '0901234573', N'Đầu bếp', N'Nam', N'Đang làm', CAST(SYSDATETIME() AS DATE)
FROM Users u
WHERE u.Email = 'giang.do@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP007');

-- EMP008 - Phục vụ
INSERT INTO Employees (UserID, EmployeeCode, FullName, Phone, Position, Gender, EmploymentStatus, HireDate)
SELECT u.UserID, 'EMP008', N'Bùi Thị Hà', '0901234574', N'Phục vụ', N'Nữ', N'Đang làm', CAST(SYSDATETIME() AS DATE)
FROM Users u
WHERE u.Email = 'ha.bui@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP008');

-- EMP009 - Bảo vệ
INSERT INTO Employees (UserID, EmployeeCode, FullName, Phone, Position, Gender, EmploymentStatus, HireDate)
SELECT u.UserID, 'EMP009', N'Đinh Văn Ích', '0901234575', N'Bảo vệ', N'Nam', N'Đang làm', CAST(SYSDATETIME() AS DATE)
FROM Users u
WHERE u.Email = 'ich.dinh@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP009');

-- EMP010 - Kế toán
INSERT INTO Employees (UserID, EmployeeCode, FullName, Phone, Email, Position, Gender, EmploymentStatus, HireDate)
SELECT u.UserID, 'EMP010', N'Ngô Thị Kim', '0901234576', 'kim.ngo@liteflow.vn', N'Kế toán', N'Nữ', N'Đang làm', CAST(SYSDATETIME() AS DATE)
FROM Users u
WHERE u.Email = 'kim.ngo@liteflow.vn'
AND NOT EXISTS (SELECT 1 FROM Employees WHERE EmployeeCode = 'EMP010');

GO

-- ============================================================
-- SAMPLE EMPLOYEE COMPENSATIONS FOR TESTING
-- ============================================================

-- Compensation for EMP001 (Owner) - Lương cứng
DECLARE @Emp1ID UNIQUEIDENTIFIER;
SELECT @Emp1ID = EmployeeID FROM Employees WHERE EmployeeCode = 'EMP001';

IF @Emp1ID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM EmployeeCompensation WHERE EmployeeID = @Emp1ID AND IsActive = 1)
BEGIN
    INSERT INTO EmployeeCompensation (
        EmployeeID, CompensationType, BaseMonthlySalary,
        OvertimeRate, BonusAmount, AllowanceAmount, DeductionAmount,
        EffectiveFrom, IsActive
    )
    VALUES (
        @Emp1ID, 'Fixed', 30000000,      -- 30M/tháng
        50000, 2000000, 1000000, 500000, -- Overtime, Bonus, Allowance, Deduction
        CAST(SYSDATETIME() AS DATE), 1
    );
END

-- Compensation for EMP002 (Cashier) - Theo giờ
DECLARE @Emp2ID UNIQUEIDENTIFIER;
SELECT @Emp2ID = EmployeeID FROM Employees WHERE EmployeeCode = 'EMP002';

IF @Emp2ID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM EmployeeCompensation WHERE EmployeeID = @Emp2ID AND IsActive = 1)
BEGIN
    INSERT INTO EmployeeCompensation (
        EmployeeID, CompensationType, HourlyRate,
        OvertimeRate, CommissionRate, AllowanceAmount,
        EffectiveFrom, IsActive
    )
    VALUES (
        @Emp2ID, 'Hybrid', 25000,        -- 25k/giờ
        35000, 2.5, 300000,              -- Overtime, Commission, Allowance
        CAST(SYSDATETIME() AS DATE), 1
    );
END

-- Compensation for EMP003 (Inventory) - Theo ca
DECLARE @Emp3ID UNIQUEIDENTIFIER;
SELECT @Emp3ID = EmployeeID FROM Employees WHERE EmployeeCode = 'EMP003';

IF @Emp3ID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM EmployeeCompensation WHERE EmployeeID = @Emp3ID AND IsActive = 1)
BEGIN
    INSERT INTO EmployeeCompensation (
        EmployeeID, CompensationType, PerShiftRate,
        OvertimeRate, BonusAmount, AllowanceAmount, DeductionAmount,
        EffectiveFrom, IsActive
    )
    VALUES (
        @Emp3ID, 'PerShift', 150000,     -- 150k/ca
        40000, 500000, 200000, 100000,   -- Overtime, Bonus, Allowance, Deduction
        CAST(SYSDATETIME() AS DATE), 1
    );
END

GO

-- ============================================================
-- PERSONAL SCHEDULES - Lịch cá nhân cho nhân viên
-- ============================================================
-- Seed data mẫu: 3 lịch cá nhân cho Đỗ Thị F (EMP006)

-- Lấy EmployeeID của Đỗ Thị F
DECLARE @EmpDTF UNIQUEIDENTIFIER;
SELECT @EmpDTF = EmployeeID FROM Employees WHERE EmployeeCode = 'EMP006';

IF @EmpDTF IS NOT NULL
BEGIN
    PRINT N'Đang tạo lịch cá nhân cho Đỗ Thị F (EMP006)...';

    -- Schedule 1: Học kỹ năng pha chế nâng cao (Priority: High, Status: Pending)
    INSERT INTO PersonalSchedules (
        EmployeeID, 
        Title, 
        Description, 
        StartDate, 
        StartTime, 
        EndTime, 
        Priority, 
        Status, 
        ReminderDate,
        Notes
    )
    VALUES (
        @EmpDTF,
        N'Học kỹ năng pha chế nâng cao',
        N'Tham gia khóa học pha chế cà phê chuyên nghiệp tại trung tâm đào tạo Barista. Học các kỹ thuật: Latte Art, Pour Over, Cold Brew, Espresso Extraction.',
        DATEADD(DAY, 3, CAST(SYSDATETIME() AS DATE)),  -- 3 ngày sau
        '09:00',
        '12:00',
        'High',
        'Pending',
        DATEADD(HOUR, -12, DATEADD(DAY, 3, CAST(SYSDATETIME() AS DATETIME2))),  -- Nhắc trước 12 giờ
        N'Cần chuẩn bị: sổ tay, dụng cụ cá nhân. Địa chỉ: 123 Nguyễn Huệ, Q.1. Liên hệ: 0901234567'
    );

    -- Schedule 2: Kiểm tra và bổ sung kho nguyên liệu (Priority: Medium, Status: InProgress)
    INSERT INTO PersonalSchedules (
        EmployeeID, 
        Title, 
        Description, 
        StartDate, 
        StartTime, 
        EndTime, 
        Priority, 
        Status, 
        Notes
    )
    VALUES (
        @EmpDTF,
        N'Kiểm tra và bổ sung kho nguyên liệu',
        N'Rà soát lại toàn bộ nguyên liệu pha chế: cà phê, sữa, đường, syrup, trà. Lập danh sách cần đặt hàng tuần này.',
        CAST(SYSDATETIME() AS DATE),  -- Hôm nay
        '14:00',
        '16:00',
        'Medium',
        'InProgress',
        N'Đã kiểm tra: cà phê ✓, sữa ✓. Cần kiểm tra: syrup, trà, topping'
    );

    -- Schedule 3: Họp team pha chế tháng này (Priority: Low, Status: Completed)
    INSERT INTO PersonalSchedules (
        EmployeeID, 
        Title, 
        Description, 
        StartDate, 
        StartTime, 
        EndTime, 
        Priority, 
        Status, 
        Notes
    )
    VALUES (
        @EmpDTF,
        N'Họp team pha chế tháng này',
        N'Tổng kết công việc tháng trước, chia sẻ kinh nghiệm và đề xuất cải tiến quy trình. Thảo luận về menu đồ uống mới cho mùa hè.',
        DATEADD(DAY, -5, CAST(SYSDATETIME() AS DATE)),  -- 5 ngày trước (đã hoàn thành)
        '17:00',
        '18:30',
        'Low',
        'Completed',
        N'✓ Đã hoàn thành. Kết quả: Thống nhất thêm 3 món mới, cải tiến quy trình đóng ca. Người tham gia: 8/10 nhân viên.'
    );

    PRINT N'✓ Đã tạo 3 lịch cá nhân cho Đỗ Thị F';
END
ELSE
BEGIN
    PRINT N'⚠️ Không tìm thấy nhân viên Đỗ Thị F (EMP006) trong database';
END

GO