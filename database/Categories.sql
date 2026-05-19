-- =====================================================
-- File: insert_categories.sql
-- Description: Thêm dữ liệu cho bảng Categories
-- Note: CategoryID sẽ tự động sinh bởi DEFAULT NEWID()
-- =====================================================

-- Xóa dữ liệu cũ (nếu cần)
-- DELETE FROM Categories;

-- Insert danh sách categories
INSERT INTO Categories (CategoryID, Name, Description) VALUES
(DEFAULT, N'Món khai vị', N'Các món ăn nhẹ, kích thích vị giác trước bữa chính như salad, súp, nem, chả giò...'),
(DEFAULT, N'Món chính', N'Các món ăn chính trong bữa ăn, bao gồm cơm, mì, thịt, cá, hải sản, món xào, món nướng...'),
(DEFAULT, N'Món chay', N'Các món ăn từ thực vật, không chứa thịt, cá, hải sản, phù hợp cho người ăn chay hoặc nhu cầu tâm linh'),
(DEFAULT, N'Món nướng', N'Các món ăn được chế biến bằng phương pháp nướng: thịt nướng, hải sản nướng, rau củ nướng'),
(DEFAULT, N'Món lẩu', N'Các loại lẩu: lẩu hải sản, lẩu thái, lẩu nấm, lẩu riêu cua, lẩu bò...'),
(DEFAULT, N'Món hấp', N'Các món ăn được chế biến bằng hơi nước, giữ được vị ngọt tự nhiên và ít dầu mỡ'),
(DEFAULT, N'Món chiên / rán', N'Các món chiên giòn: gà rán, khoai tây chiên, tôm chiên, chả ram...'),
(DEFAULT, N'Món xào', N'Các món xào nhanh trên lửa lớn: rau xào, thịt xào, đậu xào, nấm xào...'),
(DEFAULT, N'Món canh / súp', N'Các món canh, súp, hầm xương, nấu cùng rau củ, thịt hoặc hải sản'),
(DEFAULT, N'Tráng miệng', N'Các món ngọt sau bữa ăn: chè, kem, bánh flan, trái cây, bánh pudding, sữa chua...'),
(DEFAULT, N'Thức uống có cồn', N'Bia, rượu vang, cocktail, rượu mạnh, champagne...'),
(DEFAULT, N'Thức uống không cồn', N'Nước ngọt, nước ép trái cây, sinh tố, trà, cà phê, sữa, nước suối, soda...'),
(DEFAULT, N'Đồ ăn nhanh', N'Burger, pizza, sandwich, hot dog, gà rán, khoai tây lắc...'),
(DEFAULT, N'Salad & Healthy', N'Các món salad trộn, salad rau củ, hạt, quinoa, ăn kiêng, low-carb, fit...'),
(DEFAULT, N'Món hải sản', N'Tôm, cua, ghẹ, mực, ốc, hàu, cá biển... chuyên các món hải sản tươi sống'),
(DEFAULT, N'Món Á', N'Các món ăn Á: sushi, sashimi, món Hàn, món Thái, món Trung, món Ấn, món Việt cổ truyền...'),
(DEFAULT, N'Món Âu', N'Các món châu Âu: pasta, steak, pizza, salad Caesar, khoai tây nghiền, bít tết...'),
(DEFAULT, N'Gia vị & sốt', N'Các loại sốt, gia vị đi kèm: tương ớt, tương cà, sốt mayonnaise, sốt me, muối tiêu chanh...'),
(DEFAULT, N'Combo / set bữa', N'Các bữa ăn combo, set menu dành cho 1 hoặc nhiều người, bao gồm nhiều món kết hợp'),
(DEFAULT, N'Món trẻ em', N'Các món dành riêng cho trẻ nhỏ, ít gia vị, dễ ăn: cháo, bột, nugget, phô mai, sữa...');

-- Kiểm tra kết quả
SELECT CategoryID, Name, Description FROM Categories ORDER BY Name;