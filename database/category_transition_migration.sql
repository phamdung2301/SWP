-- ==========================================
-- LITEFLOW SYSTEM MIGRATION SCRIPT
-- TASK: Category Transition & Database Cleanup
-- ==========================================

USE LiteFlowDBO;
GO

PRINT '==================================================';
PRINT 'Starting Category Transition & Schema Integrity...';
PRINT '==================================================';

-- 1. Xóa các liên kết danh mục trùng lặp cho cùng một sản phẩm (giữ lại liên kết mới nhất)
BEGIN TRY
    BEGIN TRANSACTION;
        WITH DuplicatePC AS (
            SELECT 
                ProductCategoryID,
                ROW_NUMBER() OVER (
                    PARTITION BY ProductID 
                    ORDER BY ProductCategoryID DESC
                ) as RowNum
            FROM ProductsCategories
        )
        DELETE FROM ProductsCategories
        WHERE ProductCategoryID IN (
            SELECT ProductCategoryID 
            FROM DuplicatePC 
            WHERE RowNum > 1
        );
        
        PRINT '✓ Cleaned up duplicate product-category associations.';
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT 'Error in block 1: ' + ERROR_MESSAGE();
END CATCH;
GO

-- 2. Tự động chuẩn hóa tên danh mục (xóa khoảng trắng thừa)
BEGIN TRY
    BEGIN TRANSACTION;
        UPDATE Categories
        SET Name = LTRIM(RTRIM(Name));
        
        PRINT '✓ Normalized all category names.';
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT 'Error in block 2: ' + ERROR_MESSAGE();
END CATCH;
GO

-- 3. Tạo Danh mục mặc định nếu chưa tồn tại và gán cho sản phẩm chưa có danh mục
BEGIN TRY
    BEGIN TRANSACTION;
        DECLARE @DefaultCategoryID UNIQUEIDENTIFIER;
        
        -- Tạo danh mục 'Chưa phân loại' nếu chưa có
        IF NOT EXISTS (SELECT 1 FROM Categories WHERE Name = N'Chưa phân loại')
        BEGIN
            SET @DefaultCategoryID = NEWID();
            INSERT INTO Categories (CategoryID, Name, Description)
            VALUES (@DefaultCategoryID, N'Chưa phân loại', N'Danh mục mặc định dành cho sản phẩm chưa được phân loại');
            PRINT '✓ Created default category "Chưa phân loại".';
        END
        ELSE
        BEGIN
            SELECT @DefaultCategoryID = CategoryID FROM Categories WHERE Name = N'Chưa phân loại';
        END

        -- Gán danh mục mặc định cho tất cả sản phẩm chưa có liên kết danh mục nào
        INSERT INTO ProductsCategories (ProductCategoryID, ProductID, CategoryID)
        SELECT 
            NEWID(), 
            p.ProductID, 
            @DefaultCategoryID
        FROM Products p 
        WHERE p.IsDeleted = 0 
          AND p.ProductID NOT IN (SELECT ProductID FROM ProductsCategories);
          
        PRINT '✓ Linked all uncategorized products to "Chưa phân loại".';
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT 'Error in block 3: ' + ERROR_MESSAGE();
END CATCH;
GO

PRINT '==================================================';
PRINT '✓ Category Transition Completed Successfully!';
PRINT '==================================================';
