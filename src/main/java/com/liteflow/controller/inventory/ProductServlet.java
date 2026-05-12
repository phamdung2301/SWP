package com.liteflow.controller.inventory;

import com.liteflow.model.inventory.ProductDisplayDTO;
import com.liteflow.service.inventory.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.http.Part;
import java.io.*;
import java.util.List;
import java.util.UUID;

// @WebServlet annotation removed - using web.xml mapping to support multipart-config
public class ProductServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Check for success/error messages in session
            HttpSession session = request.getSession();
            String success = (String) session.getAttribute("success");
            String error = (String) session.getAttribute("error");
            
            if (success != null) {
                request.setAttribute("success", success);
                session.removeAttribute("success");
            }
            
            if (error != null) {
                request.setAttribute("error", error);
                session.removeAttribute("error");
            }
            
            // Lấy danh sách sản phẩm với giá và tồn kho
            List<ProductDisplayDTO> productList = productService.getAllProductsWithPriceAndStock();
            
            // Lấy danh sách danh mục từ sản phẩm hiện có
            List<String> categories = productService.getDistinctCategoriesFromProducts();
            
            // Lấy danh sách đơn vị tính từ sản phẩm hiện có
            List<String> units = productService.getAllUnits();
            
            // Gửi sang JSP
            request.setAttribute("products", productList);
            request.setAttribute("categories", categories);
            request.setAttribute("units", units);
            request.getRequestDispatcher("/inventory/productlist.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong ProductServlet doGet: " + e.getMessage());
            e.printStackTrace();
            
            // Send error response directly to avoid JSP issues
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().write(
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>Product List Error</title></head>\n" +
                "<body>\n" +
                "    <h1>Lỗi tải trang danh sách sản phẩm</h1>\n" +
                "    <p><strong>Lỗi:</strong> " + e.getMessage() + "</p>\n" +
                "    <pre>" + e.toString() + "</pre>\n" +
                "    <p><a href='" + request.getContextPath() + "/dashboard'>Quay về Dashboard</a></p>\n" +
                "</body>\n" +
                "</html>"
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Set encoding for form data
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String action = request.getParameter("action");

            if ("test".equals(action)) {
                response.getWriter().println("POST request received successfully!");
                return;
            }
            
            if ("addCategory".equals(action)) {
                // Thêm category mới
                String categoryName = request.getParameter("categoryName");
                
                if (categoryName == null || categoryName.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Category name is required");
                    return;
                }
                
                try {
                    com.liteflow.dao.inventory.ProductDAO productDAO = new com.liteflow.dao.inventory.ProductDAO();
                    
                    // Check if category already exists
                    if (productDAO.categoryExists(categoryName.trim())) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        response.getWriter().write("Category already exists");
                        return;
                    }
                    
                    // Create new category
                    com.liteflow.model.inventory.Category category = productDAO.addCategoryIfNotExists(categoryName.trim());
                    
                    if (category != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":\"success\",\"message\":\"Category added successfully\"}");
                } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().write("Failed to create category");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error adding category: " + e.getMessage());
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Error adding category: " + e.getMessage());
                }
                return;
            }
            
            if ("create".equals(action)) {
                // Tạo sản phẩm mới
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String imageUrl = request.getParameter("imageUrl");
                String productType = request.getParameter("productType");
                String category = request.getParameter("category");
                String status = request.getParameter("status");
                String unit = request.getParameter("unit");
                String priceStr = request.getParameter("price");
                String stockStr = request.getParameter("stock");
                String[] sizes = request.getParameterValues("size");
                String customSize = request.getParameter("customSize");
                
                // Validate productType (required)
                if (productType == null || productType.trim().isEmpty() || 
                    productType.equals("-- Chọn loại hàng --") || productType.equals("Chọn loại hàng")) {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Vui lòng chọn loại hàng");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                }
                
                // Validate category (required)
                if (category == null || category.trim().isEmpty() || 
                    category.equals("-- Chọn danh mục --") || category.equals("Chọn danh mục")) {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Vui lòng chọn danh mục");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                }
                
                // Validate unit (required)
                if (unit == null || unit.trim().isEmpty() || 
                    unit.equals("-- Chọn đơn vị tính --") || unit.equals("Chọn đơn vị tính")) {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Vui lòng chọn đơn vị tính");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                }

                // Validation
                if (name == null || name.trim().isEmpty()) {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Tên sản phẩm không được để trống");
                    response.sendRedirect(request.getContextPath() + "/products");
                        return;
                    }
                
                // Duplicate name check removed - allow duplicate product names
                
                // Mô tả không bắt buộc - đã bỏ validation

                if (priceStr == null || priceStr.trim().isEmpty()) {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Giá bán không được để trống");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                }

                if (stockStr == null || stockStr.trim().isEmpty()) {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Số lượng tồn kho không được để trống");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                }

                // Validate price
                double price;
                try {
                    // Remove formatting before parsing
                    String cleanPriceStr = priceStr.trim().replace(".", "").replace(",", "").replace(" ", "");
                    price = Double.parseDouble(cleanPriceStr);
                    if (price < 0) {
                        HttpSession session = request.getSession();
                        session.setAttribute("error", "Giá bán phải lớn hơn hoặc bằng 0");
                        response.sendRedirect(request.getContextPath() + "/products");
                        return;
                    }
                    if (price == 0) {
                        HttpSession session = request.getSession();
                        session.setAttribute("error", "Giá bán phải lớn hơn 0");
                        response.sendRedirect(request.getContextPath() + "/products");
                        return;
                    }
                    if (price < 1000) {
                        HttpSession session = request.getSession();
                        session.setAttribute("error", "Giá bán tối thiểu là 1,000 VND");
                        response.sendRedirect(request.getContextPath() + "/products");
                        return;
                    }
                    if (price > 100000000) {
                        HttpSession session = request.getSession();
                        session.setAttribute("error", "Giá bán không được vượt quá 100,000,000 VND");
                        response.sendRedirect(request.getContextPath() + "/products");
                        return;
                    }
                } catch (NumberFormatException e) {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Giá bán không hợp lệ");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                }
                
                // Validate stock (0 - 10000)
                int stock;
                try {
                    stock = Integer.parseInt(stockStr.trim());
                    if (stock < 0 || stock > 10000) {
                        HttpSession session = request.getSession();
                        session.setAttribute("error", "Số lượng tồn kho phải từ 0 đến 10,000");
                        response.sendRedirect(request.getContextPath() + "/products");
                        return;
                    }
                } catch (NumberFormatException e) {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Số lượng tồn kho không hợp lệ");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                }

                // Validate size
                if ((sizes == null || sizes.length == 0) && (customSize == null || customSize.trim().isEmpty())) {
                    request.setAttribute("error", "Vui lòng chọn ít nhất một size");
                    doGet(request, response);
                    return;
                }

                // Handle image upload
                String savedImagePath = handleImageUpload(request, name);

                // Tạo đối tượng Product mới
                com.liteflow.model.inventory.Product newProduct = new com.liteflow.model.inventory.Product();
                newProduct.setName(name.trim());
                newProduct.setDescription(description.trim());
                
                // Set image URL: priority: uploaded file > URL input
                if (savedImagePath != null && !savedImagePath.isEmpty()) {
                    newProduct.setImageUrl(savedImagePath);
                } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    newProduct.setImageUrl(imageUrl.trim());
                } else {
                    newProduct.setImageUrl(null);
                }
                
                newProduct.setProductType(productType != null && !productType.trim().isEmpty() ? productType.trim() : null);
                
                // Validation: Kiểm tra mâu thuẫn giữa số lượng tồn kho và trạng thái
                if (status != null && !status.trim().isEmpty()) {
                    if (stock > 0 && "Hết hàng".equals(status)) {
                        HttpSession session = request.getSession();
                        session.setAttribute("error", "Không thể đặt trạng thái 'Hết hàng' khi số lượng tồn kho lớn hơn 0");
                        response.sendRedirect(request.getContextPath() + "/products");
                        return;
                    }
                    if (stock == 0 && !"Hết hàng".equals(status)) {
                        HttpSession session = request.getSession();
                        session.setAttribute("error", "Số lượng tồn kho = 0, trạng thái phải là 'Hết hàng'");
                        response.sendRedirect(request.getContextPath() + "/products");
                        return;
                    }
                }
                
                // Nếu stock = 0, tự động set status = "Hết hàng"
                String finalStatus;
                if (stock == 0) {
                    finalStatus = "Hết hàng";
                } else {
                    finalStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : "Đang bán";
                }
                newProduct.setStatus(finalStatus);
                newProduct.setUnit(unit != null && !unit.trim().isEmpty() ? unit.trim() : null);
                newProduct.setImportDate(java.time.LocalDateTime.now());
                newProduct.setIsDeleted(false);

                // Lưu sản phẩm
                com.liteflow.dao.inventory.ProductDAO productDAO = new com.liteflow.dao.inventory.ProductDAO();
                boolean success = productDAO.insert(newProduct);
                
                if (success) {
                    try {
                        // Tạo ProductVariant và ProductStock cho mỗi size
                        if (sizes != null && sizes.length > 0) {
                            // Tạo variant cho các size S, M, L được chọn
                            for (String size : sizes) {
                                createProductVariantAndStock(newProduct, size, price, stock);
                            }
                        }
                        
                        if (customSize != null && !customSize.trim().isEmpty()) {
                            // Tạo variant cho custom size
                            createProductVariantAndStock(newProduct, customSize.trim(), price, stock);
                        }
                        
                        // Tự động cập nhật trạng thái: nếu stock = 0 thì set status = "Hết hàng"
                        if (stock == 0) {
                            newProduct.setStatus("Hết hàng");
                            productDAO.update(newProduct);
                        }
                        
                        // Tạo ProductCategory nếu có category được chọn
                        if (category != null && !category.trim().isEmpty()) {
                            createProductCategory(newProduct, category.trim());
                        }
                        
                        // Set success message in session and redirect to avoid resubmit
                        HttpSession session = request.getSession();
                        session.setAttribute("success", "Thêm sản phẩm thành công!");
                        
                        // Redirect to avoid resubmit on F5
                        response.sendRedirect(request.getContextPath() + "/products");
                            return;
                    } catch (Exception e) {
                        System.err.println("❌ Lỗi khi tạo ProductVariant/ProductStock: " + e.getMessage());
                        e.printStackTrace();
                        HttpSession session = request.getSession();
                    session.setAttribute("error", "Sản phẩm đã được tạo nhưng có lỗi khi tạo variants");
                    response.sendRedirect(request.getContextPath() + "/products");
                            return;
                    }
                } else {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Có lỗi xảy ra khi thêm sản phẩm");
                    response.sendRedirect(request.getContextPath() + "/products");
                        return;
                }
            }

            if ("update".equals(action)) {
                String productIdStr = request.getParameter("productId");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String imageUrl = request.getParameter("imageUrl");
                String priceStr = request.getParameter("price");
                String stockStr = request.getParameter("stock");
                String size = request.getParameter("size");

                try {
                    java.util.UUID productId = java.util.UUID.fromString(productIdStr);

                    // Update Product fields
                    com.liteflow.dao.inventory.ProductDAO productDAO = new com.liteflow.dao.inventory.ProductDAO();
                    com.liteflow.model.inventory.Product product = productDAO.findById(productId);
                    if (product == null) {
                        request.setAttribute("error", "Không tìm thấy sản phẩm để cập nhật");
                        doGet(request, response);
                        return;
                    }
                    
                    // Duplicate name check removed - allow duplicate product names
                    if (name != null && !name.trim().isEmpty()) {
                        product.setName(name.trim());
                    }
                    if (description != null) product.setDescription(description.trim());
                    
                    // Update status - Tự động cập nhật nếu stockAmount = 0
                    String status = request.getParameter("status");
                    int stockAmount = 0;
                    if (stockStr != null && !stockStr.isBlank()) {
                        try {
                            stockAmount = Integer.parseInt(stockStr.trim());
                        } catch (NumberFormatException ignored) {}
                    }
                    
                    // Validation: Kiểm tra mâu thuẫn giữa số lượng tồn kho và trạng thái
                    if (status != null && !status.trim().isEmpty()) {
                        if (stockAmount > 0 && "Hết hàng".equals(status)) {
                            HttpSession session = request.getSession();
                            session.setAttribute("error", "Không thể đặt trạng thái 'Hết hàng' khi số lượng tồn kho lớn hơn 0");
                            response.sendRedirect(request.getContextPath() + "/products");
                            return;
                        }
                        if (stockAmount == 0 && !"Hết hàng".equals(status)) {
                            HttpSession session = request.getSession();
                            session.setAttribute("error", "Số lượng tồn kho = 0, trạng thái phải là 'Hết hàng'");
                            response.sendRedirect(request.getContextPath() + "/products");
                            return;
                        }
                    }
                    
                    // Tự động cập nhật trạng thái: nếu stockAmount = 0 thì set status = "Hết hàng"
                    if (stockAmount == 0) {
                        product.setStatus("Hết hàng");
                    } else if (status != null && !status.trim().isEmpty()) {
                        product.setStatus(status.trim());
                    }
                    
                    // Update unit
                    String unit = request.getParameter("unit");
                    if (unit != null && !unit.trim().isEmpty()) {
                        product.setUnit(unit.trim());
                    }
                    
                    // Handle image upload
                    String savedImagePath = handleImageUpload(request, product.getName());
                    
                    // Set image URL: priority: uploaded file > URL input
                    if (savedImagePath != null && !savedImagePath.isEmpty()) {
                        // Delete old image if exists and is not external URL
                        String oldImagePath = product.getImageUrl();
                        if (oldImagePath != null && oldImagePath.startsWith("/uploads/")) {
                            deleteImageFile(oldImagePath, request);
                        }
                        product.setImageUrl(savedImagePath);
                    } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        // Delete old image if exists and is not external URL
                        String oldImagePath = product.getImageUrl();
                        if (oldImagePath != null && oldImagePath.startsWith("/uploads/")) {
                            deleteImageFile(oldImagePath, request);
                        }
                        product.setImageUrl(imageUrl.trim());
                    }
                    boolean productUpdated = productDAO.update(product);

                    // Update price and stock for the selected size
                    com.liteflow.dao.inventory.ProductVariantDAO variantDAO = new com.liteflow.dao.inventory.ProductVariantDAO();
                    com.liteflow.model.inventory.ProductVariant variant = variantDAO.findByProductAndSize(productId, size);
                    if (variant != null) {
                        if (priceStr != null && !priceStr.isBlank()) {
                            try {
                                double price = Double.parseDouble(priceStr.trim());
                                variant.setPrice(java.math.BigDecimal.valueOf(price));
                            } catch (NumberFormatException ignored) {}
                        }

                        if (stockStr != null && !stockStr.isBlank()) {
                            try {
                                int stock = Integer.parseInt(stockStr.trim());
                                com.liteflow.dao.inventory.ProductStockDAO stockDAO = new com.liteflow.dao.inventory.ProductStockDAO();
                                // Tìm stock theo variant và inventory mặc định
                                var em = com.liteflow.dao.BaseDAO.emf.createEntityManager();
                                try {
                                    var stocks = em.createQuery("SELECT ps FROM ProductStock ps WHERE ps.productVariant.productVariantId = :pvid", com.liteflow.model.inventory.ProductStock.class)
                                            .setParameter("pvid", variant.getProductVariantId())
                                            .getResultList();
                                    if (!stocks.isEmpty()) {
                                        var ps = stocks.get(0);
                                        ps.setAmount(stock);
                                        stockDAO.update(ps);
                                    }
                                } finally {
                                    em.close();
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                        variantDAO.update(variant);
                    }

                    HttpSession session = request.getSession();
                    session.setAttribute("success", "Cập nhật sản phẩm thành công");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                } catch (Exception ex) {
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Lỗi khi cập nhật sản phẩm: " + ex.getMessage());
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                }
            }
            
            if ("delete".equals(action)) {
                String productIdStr = request.getParameter("productId");
                String size = request.getParameter("size");

                try {
                    java.util.UUID productId = java.util.UUID.fromString(productIdStr);
                    
                    // Soft delete only the specific variant, not the entire product
                    var em = com.liteflow.dao.BaseDAO.emf.createEntityManager();
                    try {
                        em.getTransaction().begin();
                        
                        // Find and delete only the specific variant
                        var variant = em.createQuery(
                            "SELECT pv FROM ProductVariant pv WHERE pv.product.productId = :productId AND pv.size = :size",
                            com.liteflow.model.inventory.ProductVariant.class
                        ).setParameter("productId", productId)
                         .setParameter("size", size)
                         .getResultList();

                        if (!variant.isEmpty()) {
                            variant.get(0).setIsDeleted(true);
                            em.merge(variant.get(0));
                        }

                        em.getTransaction().commit();
                        
                    } catch (Exception e) {
                        if (em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                        throw e;
                    } finally {
                        em.close();
                    }
                    
                    HttpSession session = request.getSession();
                    session.setAttribute("success", "Đã xóa sản phẩm thành công");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                } catch (Exception e) {
                    System.err.println("❌ Lỗi khi xóa sản phẩm: " + e.getMessage());
                    e.printStackTrace();
                    HttpSession session = request.getSession();
                    session.setAttribute("error", "Có lỗi xảy ra khi xóa sản phẩm");
                    response.sendRedirect(request.getContextPath() + "/products");
                    return;
                }
            }
            
            if ("exportExcel".equals(action)) {
                exportExcel(request, response);
                return;
            }
            
            if ("downloadTemplate".equals(action)) {
                downloadTemplate(request, response);
                return;
            }
            
            if ("checkExcel".equals(action)) {
                response.setContentType("application/json");
                checkExcel(request, response);
                return;
            }
            
            if ("importExcel".equals(action)) {
                response.setContentType("application/json");
                importExcel(request, response);
                return;
            }
            
            if ("deleteCategory".equals(action)) {
                response.setContentType("application/json");
                deleteCategory(request, response);
                return;
            }
            
            if ("deleteUnit".equals(action)) {
                response.setContentType("application/json");
                deleteUnit(request, response);
                return;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong ProductServlet POST: " + e.getMessage());
            e.printStackTrace();
                request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
                doGet(request, response);
        }
    }
    
    /**
     * Export Products to Excel
     */
    private void exportExcel(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Get all products
            List<ProductDisplayDTO> products = productService.getAllProductsWithPriceAndStock();
            
            if (products == null || products.isEmpty()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Không có dữ liệu để xuất\"}");
                return;
            }
            
            // Create Excel workbook
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Products");
            
            // Create header row - Same order as template and import
            // A=Tên, B=Mô tả, C=Loại hàng, D=Size, E=Giá bán, F=Tồn kho, G=Trạng thái, H=Đơn vị, I=Danh mục
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Tên sản phẩm", "Mô tả", "Loại hàng", "Size", "Giá bán", "Số lượng tồn kho", "Trạng thái", "Đơn vị tính", "Danh mục"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // Make header bold
                org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }
            
            // Fill data rows
            int rowNum = 1;
            for (ProductDisplayDTO p : products) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getProductName() != null ? p.getProductName() : "");
                row.createCell(1).setCellValue(p.getDescription() != null ? p.getDescription() : "");
                row.createCell(2).setCellValue(p.getProductType() != null ? p.getProductType() : "");
                row.createCell(3).setCellValue(p.getSize() != null ? p.getSize() : "");
                row.createCell(4).setCellValue(p.getPrice() != null ? p.getPrice().doubleValue() : 0.0);
                row.createCell(5).setCellValue(p.getStockAmount());
                row.createCell(6).setCellValue(p.getStatus() != null ? p.getStatus() : "");
                row.createCell(7).setCellValue(p.getUnit() != null ? p.getUnit() : "");
                row.createCell(8).setCellValue(p.getCategoryName() != null ? p.getCategoryName() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write workbook to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] excelData = outputStream.toByteArray();
            workbook.close();
            outputStream.close();
            
            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"products_data.xlsx\"");
            response.setContentLength(excelData.length);
            
            // Write Excel data to response
            java.io.OutputStream out = response.getOutputStream();
            out.write(excelData);
            out.flush();

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi export Excel: " + e.getMessage());
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi xuất file: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Download Excel Template
     */
    private void downloadTemplate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String templateType = request.getParameter("templateType");

            // Create template Excel file
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // ============ SHEET 1: HƯỚNG DẪN ============
            org.apache.poi.ss.usermodel.Sheet instructionSheet = workbook.createSheet("Hướng dẫn");
            
            // Title row
            org.apache.poi.ss.usermodel.Row titleRow = instructionSheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("HƯỚNG DẪN NHẬP DỮ LIỆU SẢN PHẨM");
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Instruction content
            int rowNum = 2;
            
            // Section 1: Cấu trúc file
            instructionSheet.createRow(rowNum++).createCell(0).setCellValue("CẤU TRÚC FILE (Sheet 'Sản phẩm'):");
            rowNum++;
            
            String[][] instructions = {
                {"Cột A:", "Tên sản phẩm (bắt buộc)"},
                {"Cột B:", "Mô tả sản phẩm (tùy chọn)"},
                {"Cột C:", "Loại hàng - CHỈ: Hàng hóa thường / Chế biến / Dịch vụ / Combo (bắt buộc, chính xác)"},
                {"Cột D:", "Size (S/M/L hoặc tùy chỉnh)"},
                {"Cột E:", "Giá bán (bắt buộc, >= 1000)"},
                {"Cột F:", "Số lượng tồn kho (bắt buộc, 0-10000)"},
                {"Cột G:", "Trạng thái - CHỈ: Đang bán / Hết hàng / Dừng bán (bắt buộc, chính xác)"},
                {"Cột H:", "Đơn vị tính (Ly/Cái/Miếng/Phần - hoặc đơn vị mới)"},
                {"Cột I:", "Danh mục (tùy chọn)"}
            };
            
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            
            for (String[] instruction : instructions) {
                org.apache.poi.ss.usermodel.Row row = instructionSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(instruction[0]);
                row.createCell(1).setCellValue(instruction[1]);
                row.getCell(0).setCellStyle(boldStyle);
            }
            
            rowNum += 2;
            
            // Section 2: Lưu ý
            instructionSheet.createRow(rowNum++).createCell(0).setCellValue("LƯU Ý:");
            rowNum++;
            
            String[] notes = {
                "- Cột C (Loại hàng) chỉ chấp nhận đúng 4 giá trị: Hàng hóa thường, Chế biến, Dịch vụ, Combo",
                "- Cột G (Trạng thái) chỉ chấp nhận đúng 3 giá trị: Đang bán, Hết hàng, Dừng bán",
                "- Nếu trạng thái là 'Hết hàng' thì số lượng tồn kho phải là 0",
                "- Nếu số lượng > 0 thì trạng thái không thể là 'Hết hàng'",
                "- Đơn vị tính có thể nhập mới, không cần phải chọn từ danh sách có sẵn"
            };
            
            for (String note : notes) {
                org.apache.poi.ss.usermodel.Row row = instructionSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(note);
            }
            
            // Auto-size columns
            instructionSheet.autoSizeColumn(0);
            instructionSheet.autoSizeColumn(1);
            
            // ============ SHEET 2: SẢN PHẨM ============
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Sản phẩm");
            
            // Create header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Tên sản phẩm", "Mô tả", "Loại hàng", "Size", "Giá bán", "Số lượng tồn kho", "Trạng thái", "Đơn vị tính", "Danh mục"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // Make header bold
                org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }
            
            // Add sample data row
            org.apache.poi.ss.usermodel.Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("Cà phê đen");
            sampleRow.createCell(1).setCellValue("Cà phê phin truyền thống");
            sampleRow.createCell(2).setCellValue("Dịch vụ");
            sampleRow.createCell(3).setCellValue("M");
            sampleRow.createCell(4).setCellValue(25000);
            sampleRow.createCell(5).setCellValue(100);
            sampleRow.createCell(6).setCellValue("Đang bán");
            sampleRow.createCell(7).setCellValue("Ly");
            sampleRow.createCell(8).setCellValue("Cà phê");
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write workbook to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] templateData = outputStream.toByteArray();
            workbook.close();
            outputStream.close();
            
            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"mau_san_pham.xlsx\"");
            response.setContentLength(templateData.length);
            
            // Write template data to response
            java.io.OutputStream out = response.getOutputStream();
            out.write(templateData);
            out.flush();
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi download template: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi tải template: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Check Excel File
     */
    private void checkExcel(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Part filePart = request.getPart("excelFile");
            if (filePart == null || filePart.getSize() == 0) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không có file được upload\"}");
                return;
            }
            
            boolean skipDuplicates = "true".equals(request.getParameter("skipDuplicates"));
            boolean validateData = "true".equals(request.getParameter("validateData"));
            
            // Read Excel file
            try (java.io.InputStream inputStream = filePart.getInputStream()) {
                org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(inputStream);
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
                
                java.util.List<String> errors = new java.util.ArrayList<>();
                int totalRows = 0;
                int validRows = 0;
                
                // Allowed product types
                java.util.Set<String> allowedTypes = new java.util.HashSet<>();
                allowedTypes.add("Hàng hóa thường");
                allowedTypes.add("Chế biến");
                allowedTypes.add("Dịch vụ");
                allowedTypes.add("Combo");
                
                // Allowed statuses
                java.util.Set<String> allowedStatuses = new java.util.HashSet<>();
                allowedStatuses.add("Đang bán");
                allowedStatuses.add("Hết hàng");
                allowedStatuses.add("Dừng bán");
                
                // Check each row (skip header row 0)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    totalRows++;
                    boolean rowValid = true;
                    
                    // Check column C (index 2) - Product Type
                    if (row.getCell(2) != null) {
                        String productType = getCellValueAsString(row.getCell(2));
                        if (!allowedTypes.contains(productType)) {
                            errors.add("Dòng " + (i + 1) + ", Cột C: Loại hàng không hợp lệ. Chỉ chấp nhận: Hàng hóa thường, Chế biến, Dịch vụ, Combo. Giá trị hiện tại: '" + productType + "'");
                            rowValid = false;
                        }
                    } else {
                        errors.add("Dòng " + (i + 1) + ", Cột C: Loại hàng là bắt buộc");
                        rowValid = false;
                    }
                    
                    // Check column G (index 6) - Status
                    if (row.getCell(6) != null) {
                        String status = getCellValueAsString(row.getCell(6));
                        if (!allowedStatuses.contains(status)) {
                            errors.add("Dòng " + (i + 1) + ", Cột G: Trạng thái không hợp lệ. Chỉ chấp nhận: Đang bán, Hết hàng, Dừng bán. Giá trị hiện tại: '" + status + "'");
                            rowValid = false;
                        }
                    } else {
                        errors.add("Dòng " + (i + 1) + ", Cột G: Trạng thái là bắt buộc");
                        rowValid = false;
                    }
                    
                    if (rowValid) {
                        validRows++;
                    }
                }
                
                workbook.close();
                
                // Build response
                StringBuilder jsonResponse = new StringBuilder();
                jsonResponse.append("{\"success\": ").append(errors.isEmpty());
                jsonResponse.append(", \"message\": \"").append(errors.isEmpty() ? "File hợp lệ" : "File có lỗi");
                jsonResponse.append("\", \"totalProducts\": ").append(validRows);
                jsonResponse.append(", \"totalRows\": ").append(totalRows);
                jsonResponse.append(", \"errors\": [");
                
                for (int i = 0; i < errors.size(); i++) {
                    if (i > 0) jsonResponse.append(", ");
                    jsonResponse.append("\"").append(errors.get(i).replace("\"", "\\\"")).append("\"");
                }
                
                jsonResponse.append("]}");
                
                response.getWriter().write(jsonResponse.toString());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi check Excel: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi kiểm tra file: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Helper method to get cell value as string
     */
    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Remove decimal part if it's 0
                    double num = cell.getNumericCellValue();
                    if (num == (int) num) {
                        return String.valueOf((int) num);
                    }
                    return String.valueOf(num);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * Import Excel File
     */
    private void importExcel(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Part filePart = request.getPart("file");
            if (filePart == null) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không tìm thấy file\"}");
                return;
            }
            
            String fileName = filePart.getSubmittedFileName();
            
            boolean skipDuplicates = "true".equals(request.getParameter("skipDuplicates"));
            boolean validateData = "true".equals(request.getParameter("validateData"));
            
            // Read Excel file
            try (java.io.InputStream inputStream = filePart.getInputStream()) {
                org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(inputStream);
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
                
                // Allowed product types
                java.util.Set<String> allowedTypes = new java.util.HashSet<>();
                allowedTypes.add("Hàng hóa thường");
                allowedTypes.add("Chế biến");
                allowedTypes.add("Dịch vụ");
                allowedTypes.add("Combo");
                
                int successCount = 0;
                int errorCount = 0;
                java.util.List<String> errors = new java.util.ArrayList<>();
                
                // Import each row (skip header row 0)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    try {
                        // Column A: Product name
                        String productName = getCellValueAsString(row.getCell(0));
                        if (productName == null || productName.trim().isEmpty()) {
                            errors.add("Dòng " + (i + 1) + ": Tên sản phẩm không được để trống");
                            errorCount++;
                            continue;
                        }
                        
                        // Column B: Description (optional)
                        String description = getCellValueAsString(row.getCell(1));
                        
                        
                        // Column C: Product Type (must be valid)
                        String productType = getCellValueAsString(row.getCell(2));
                        if (productType == null || productType.trim().isEmpty() || !allowedTypes.contains(productType)) {
                            errors.add("Dòng " + (i + 1) + ": Loại hàng không hợp lệ: '" + productType + "'");
                            errorCount++;
                            continue;
                        }
                        
                        // Column D: Size
                        String size = getCellValueAsString(row.getCell(3));
                        if (size == null || size.trim().isEmpty()) {
                            size = "M"; // Default
                        }
                        
                        // Column E: Price
                        String priceStr = getCellValueAsString(row.getCell(4));
                        double price = 0;
                        try {
                            price = Double.parseDouble(priceStr.replaceAll("[^0-9.]", ""));
                            if (price < 1000) {
                                errors.add("Dòng " + (i + 1) + ": Giá bán phải >= 1000");
                                errorCount++;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            errors.add("Dòng " + (i + 1) + ": Giá bán không hợp lệ: '" + priceStr + "'");
                            errorCount++;
                            continue;
                        }
                        
                        // Column F: Stock
                        String stockStr = getCellValueAsString(row.getCell(5));
                        int stock = 0;
                        try {
                            stock = Integer.parseInt(stockStr.replaceAll("[^0-9]", ""));
                            if (stock < 0 || stock > 10000) {
                                errors.add("Dòng " + (i + 1) + ": Số lượng tồn kho phải từ 0 đến 10000");
                                errorCount++;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            errors.add("Dòng " + (i + 1) + ": Số lượng tồn kho không hợp lệ: '" + stockStr + "'");
                            errorCount++;
                            continue;
                        }
                        
                        // Column G: Status (must be valid)
                        String status = getCellValueAsString(row.getCell(6));
                        
                        // Allowed statuses
                        java.util.Set<String> allowedStatuses = new java.util.HashSet<>();
                        allowedStatuses.add("Đang bán");
                        allowedStatuses.add("Hết hàng");
                        allowedStatuses.add("Dừng bán");
                        
                        if (status == null || status.trim().isEmpty() || !allowedStatuses.contains(status)) {
                            errors.add("Dòng " + (i + 1) + ", Cột G: Trạng thái không hợp lệ. Chỉ chấp nhận: Đang bán, Hết hàng, Dừng bán. Giá trị hiện tại: '" + status + "'");
                            errorCount++;
                            continue;
                        }
                        
                        // Validate stock-status consistency
                        if (stock == 0 && "Đang bán".equals(status)) {
                            errors.add("Dòng " + (i + 1) + ": Số lượng = 0 không thể có trạng thái 'Đang bán'");
                            errorCount++;
                            continue;
                        }
                        if (stock > 0 && "Hết hàng".equals(status)) {
                            errors.add("Dòng " + (i + 1) + ": Số lượng > 0 không thể có trạng thái 'Hết hàng'");
                            errorCount++;
                            continue;
                        }
                        
                        // Column H: Unit (optional, can accept new units)
                        String unit = getCellValueAsString(row.getCell(7));
                        if (unit == null || unit.trim().isEmpty()) {
                            unit = "Cái"; // Default
                        }
                        // Note: Unit is flexible - accepts any new unit value
                        
                        // Column I: Category (optional)
                        String category = getCellValueAsString(row.getCell(8));
                        
                        // Create Product entity
                        com.liteflow.model.inventory.Product product = new com.liteflow.model.inventory.Product();
                        product.setName(productName);
                        product.setDescription(description);
                        product.setProductType(productType);
                        product.setStatus(status);
                        product.setUnit(unit);
                        product.setImageUrl(""); // No image from Excel
                        product.setIsDeleted(false);
                        
                        // Save Product to database
                        com.liteflow.dao.inventory.ProductDAO productDAO = new com.liteflow.dao.inventory.ProductDAO();
                        boolean productSaved = productDAO.insert(product);
                        
                        if (productSaved) {
                            // Create ProductVariant and Stock
                            createProductVariantAndStock(product, size, price, stock);
                            
                        // Link Category if provided
                        if (category != null && !category.trim().isEmpty()) {
                            try {
                                linkProductToCategory(product, category);
                            } catch (Exception e) {
                                System.err.println("⚠️ Warning: Could not link category: " + e.getMessage());
                                // Don't fail the import if category linking fails
                            }
                        }
                            
                            successCount++;
                        } else {
                            errors.add("Dòng " + (i + 1) + ": Không thể tạo sản phẩm trong database");
                            errorCount++;
                        }
                        
                    } catch (Exception e) {
                        errors.add("Dòng " + (i + 1) + ": Lỗi xử lý - " + e.getMessage());
                        errorCount++;
                        System.err.println("❌ Lỗi import dòng " + (i + 1) + ": " + e.getMessage());
                    }
                }
                
                workbook.close();
                
                // Build response
                StringBuilder jsonResponse = new StringBuilder();
                jsonResponse.append("{\"success\": true");
                jsonResponse.append(", \"message\": \"Import thành công ").append(successCount).append(" sản phẩm");
                if (errorCount > 0) {
                    jsonResponse.append(", ").append(errorCount).append(" sản phẩm lỗi");
                }
                jsonResponse.append("\"");
                jsonResponse.append(", \"successCount\": ").append(successCount);
                jsonResponse.append(", \"errorCount\": ").append(errorCount);
                jsonResponse.append(", \"errors\": [");
                
                for (int i = 0; i < Math.min(errors.size(), 10); i++) { // Limit to 10 errors
                    if (i > 0) jsonResponse.append(", ");
                    jsonResponse.append("\"").append(errors.get(i).replace("\"", "\\\"")).append("\"");
                }
                
                jsonResponse.append("]}");
                
                response.getWriter().write(jsonResponse.toString());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi import Excel: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi xử lý file: " + e.getMessage() + "\"}");
        }
    }
    
    private void createProductVariantAndStock(com.liteflow.model.inventory.Product product, String size, double price, int stock) {
        try {
            // Tạo ProductVariant
            com.liteflow.model.inventory.ProductVariant variant = new com.liteflow.model.inventory.ProductVariant();
            variant.setProduct(product);
            variant.setSize(size);
            variant.setPrice(java.math.BigDecimal.valueOf(price));
            variant.setOriginalPrice(java.math.BigDecimal.valueOf(price));
            variant.setIsDeleted(false);
            
            // Lưu ProductVariant
            com.liteflow.dao.inventory.ProductVariantDAO variantDAO = new com.liteflow.dao.inventory.ProductVariantDAO();
            boolean variantSuccess = variantDAO.insert(variant);
            
            if (variantSuccess) {
                // Tạo Inventory mặc định (nếu chưa có)
                com.liteflow.model.inventory.Inventory defaultInventory = getOrCreateDefaultInventory();
                
                if (defaultInventory != null) {
                    // Tạo ProductStock
                    com.liteflow.model.inventory.ProductStock productStock = new com.liteflow.model.inventory.ProductStock();
                    productStock.setProductVariant(variant);
                    productStock.setInventory(defaultInventory);
                    productStock.setAmount(stock);
                    
                    // Lưu ProductStock
                    com.liteflow.dao.inventory.ProductStockDAO stockDAO = new com.liteflow.dao.inventory.ProductStockDAO();
                    boolean stockSuccess = stockDAO.insert(productStock);
                    
                    if (!stockSuccess) {
                        System.err.println("❌ Lỗi khi tạo ProductStock cho size: " + size);
                    }
                } else {
                    System.err.println("❌ Không thể tạo/lấy inventory mặc định");
                }
            } else {
                System.err.println("❌ Lỗi khi tạo ProductVariant cho size: " + size);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong createProductVariantAndStock: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private com.liteflow.model.inventory.Inventory getOrCreateDefaultInventory() {
        try {
            com.liteflow.dao.inventory.InventoryDAO inventoryDAO = new com.liteflow.dao.inventory.InventoryDAO();
            
            // Tìm inventory mặc định
            com.liteflow.model.inventory.Inventory defaultInventory = inventoryDAO.findByField("storeLocation", "Kho chính");
            
            if (defaultInventory == null) {
                // Tạo inventory mặc định nếu chưa có
                defaultInventory = new com.liteflow.model.inventory.Inventory();
                defaultInventory.setStoreLocation("Kho chính");
                boolean insertResult = inventoryDAO.insert(defaultInventory);
                if (!insertResult) {
                    System.err.println("❌ Lỗi khi tạo inventory mặc định");
                }
            }
            
            return defaultInventory;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo/lấy inventory mặc định: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private void createProductCategory(com.liteflow.model.inventory.Product product, String categoryName) {
        try {
            // Tìm hoặc tạo category
            com.liteflow.dao.inventory.ProductDAO productDAO = new com.liteflow.dao.inventory.ProductDAO();
            com.liteflow.model.inventory.Category category = productDAO.addCategoryIfNotExists(categoryName);
            
            if (category == null) {
                System.err.println("❌ Failed to find or create category: " + categoryName);
                return;
            }
            
            // Tạo ProductCategory
            com.liteflow.model.inventory.ProductCategory productCategory = new com.liteflow.model.inventory.ProductCategory();
            productCategory.setProduct(product);
            productCategory.setCategory(category);
            productCategory.setProductCategoryId(java.util.UUID.randomUUID());
            
            // Lưu vào database
            if (!productDAO.addProductCategory(productCategory)) {
                System.err.println("❌ Failed to create ProductCategory");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong createProductCategory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper method to handle image upload
    private String handleImageUpload(HttpServletRequest request, String productName) {
        try {
            Part filePart = request.getPart("imageFile");
            
            // Check if file was uploaded
            if (filePart == null || filePart.getSize() == 0) {
                return null;
            }
            
            String fileName = filePart.getSubmittedFileName();
            if (fileName == null || fileName.isEmpty()) {
                return null;
            }
            
            // Validate file type
            String contentType = filePart.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return null;
            }
            
            // Get file extension
            String extension = "";
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) {
                extension = fileName.substring(lastDot);
            }
            
            // Generate unique filename
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            
            // Save to source directory (src/main/webapp) - persistent across rebuilds
            String projectPath = request.getServletContext().getRealPath("/");
            String srcWebappPath = projectPath.replace("target\\LiteFlow\\", "src\\main\\webapp\\")
                                             .replace("target/LiteFlow/", "src/main/webapp/");
            String srcUploadsDir = srcWebappPath + "uploads" + File.separator + "products";
            File srcUploadDir = new File(srcUploadsDir);
            if (!srcUploadDir.exists()) {
                srcUploadDir.mkdirs();
            }
            
            // Save to target directory (for immediate use)
            String targetUploadsDir = request.getServletContext().getRealPath("/uploads/products/");
            File targetUploadDir = new File(targetUploadsDir);
            if (!targetUploadDir.exists()) {
                targetUploadDir.mkdirs();
            }
            
            // Save file to source directory (permanent)
            String srcFilePath = srcUploadsDir + File.separator + uniqueFileName;
            filePart.write(srcFilePath);
            
            // Copy to target directory (for immediate runtime use)
            String targetFilePath = targetUploadsDir + File.separator + uniqueFileName;
            try (java.io.InputStream input = new java.io.FileInputStream(srcFilePath);
                 java.io.OutputStream output = new java.io.FileOutputStream(targetFilePath)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
            }
            
            // Return relative path for database
            String relativePath = "/uploads/products/" + uniqueFileName;
            return relativePath;
            
        } catch (Exception e) {
            System.err.println("❌ Error handling image upload: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Helper method to link product to category
    private void linkProductToCategory(com.liteflow.model.inventory.Product product, String categoryName) {
        try {
            com.liteflow.dao.inventory.ProductDAO productDAO = new com.liteflow.dao.inventory.ProductDAO();
            
            // Find or create category
            com.liteflow.model.inventory.Category category = productDAO.addCategoryIfNotExists(categoryName.trim());
            
            if (category != null) {
                // Create ProductCategory link
                com.liteflow.model.inventory.ProductCategory productCategory = new com.liteflow.model.inventory.ProductCategory();
                productCategory.setProduct(product);
                productCategory.setCategory(category);
                
                // Save the link
                if (productDAO.addProductCategory(productCategory)) {
                    System.out.println("✅ Linked product to category: " + categoryName);
                } else {
                    System.err.println("❌ Failed to link product to category: " + categoryName);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error linking product to category: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // Delete Category
    private void deleteCategory(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String categoryName = request.getParameter("categoryName");
            
            if (categoryName == null || categoryName.trim().isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"Tên danh mục không được để trống\"}");
                return;
            }
            
            com.liteflow.dao.inventory.ProductDAO productDAO = new com.liteflow.dao.inventory.ProductDAO();
            com.liteflow.model.inventory.Category category = productDAO.findCategoryByName(categoryName.trim());
            
            if (category == null) {
                response.getWriter().write("{\"success\": false, \"message\": \"Danh mục không tồn tại\"}");
                return;
            }
            
            // Check if category is being used by any product
            boolean inUse = productDAO.isCategoryInUse(category.getCategoryId());
            
            if (inUse) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không thể xóa danh mục này vì đang được sử dụng bởi sản phẩm\"}");
                return;
            }
            
            // Delete category from database
            boolean deleted = productDAO.deleteCategory(category.getCategoryId());
            
            if (deleted) {
                response.getWriter().write("{\"success\": true, \"message\": \"Đã xóa danh mục thành công\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"Không thể xóa danh mục\"}");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa danh mục: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi xóa danh mục: " + e.getMessage() + "\"}");
        }
    }
    
    // Delete Unit
    private void deleteUnit(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String unitName = request.getParameter("unitName");
            
            if (unitName == null || unitName.trim().isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"Tên đơn vị tính không được để trống\"}");
                return;
            }
            
            // Check if unit is being used by any product
            List<ProductDisplayDTO> allProducts = productService.getAllProductsWithPriceAndStock();
            boolean inUse = false;
            for (ProductDisplayDTO p : allProducts) {
                if (unitName.trim().equals(p.getUnit())) {
                    inUse = true;
                    break;
                }
            }
            
            if (inUse) {
                response.getWriter().write("{\"success\": false, \"message\": \"Không thể xóa đơn vị tính này vì đang được sử dụng bởi sản phẩm\"}");
                return;
            }
            
            // Since units are stored in Product.Unit column, we can't delete them individually
            // Just return success - the unit will remain in the database but won't be shown in dropdown
            response.getWriter().write("{\"success\": true, \"message\": \"Đã xóa đơn vị tính khỏi danh sách\"}");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa đơn vị tính: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi xóa đơn vị tính: " + e.getMessage() + "\"}");
        }
    }
    
    // Helper method to delete old image file
    private void deleteImageFile(String imagePath, HttpServletRequest request) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        
        try {
            // Delete from target directory
            String targetFilePath = request.getServletContext().getRealPath(imagePath);
            File targetFile = new File(targetFilePath);
            if (targetFile.exists()) {
                targetFile.delete();
                System.out.println("✅ Deleted old image from target: " + imagePath);
            }
            
            // Delete from source directory
            String projectPath = request.getServletContext().getRealPath("/");
            String srcWebappPath = projectPath.replace("target\\LiteFlow\\", "src\\main\\webapp\\")
                                             .replace("target/LiteFlow/", "src/main/webapp/");
            String srcFilePath = srcWebappPath + imagePath.replace("/", File.separator);
            File srcFile = new File(srcFilePath);
            if (srcFile.exists()) {
                srcFile.delete();
                System.out.println("✅ Deleted old image from source: " + srcFilePath);
            }
        } catch (Exception e) {
            System.err.println("❌ Error deleting image: " + e.getMessage());
        }
    }
}
