package com.liteflow.controller.inventory;

import com.liteflow.service.inventory.ProductService;
import com.liteflow.model.inventory.ProductPriceDTO;
import com.liteflow.dao.inventory.ProductVariantDAO;
import com.liteflow.dao.inventory.ProductDAO;
import com.liteflow.model.inventory.ProductVariant;
import com.liteflow.model.inventory.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@MultipartConfig(maxFileSize = 10485760) // 10MB
@WebServlet(name = "SetPriceServlet", urlPatterns = {"/setprice"})
public class SetPriceServlet extends HttpServlet {
    private ProductService productService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productService = new ProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Check for success/error messages in session
            jakarta.servlet.http.HttpSession session = request.getSession();
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
            
            // Lấy danh sách sản phẩm với thông tin giá
            List<ProductPriceDTO> productPriceList = productService.getAllProductsWithPriceInfo();
            // Lấy danh sách danh mục để lọc nhanh
            List<String> categories = productService.getDistinctCategoriesFromProducts();
            
            // Debug logging
            System.out.println("=== DEBUG: SetPriceServlet ===");
            System.out.println("Số lượng sản phẩm lấy được: " + (productPriceList != null ? productPriceList.size() : "null"));
            System.out.println("Số lượng danh mục: " + (categories != null ? categories.size() : "null"));

            // Gửi sang JSP
            request.setAttribute("productPrices", productPriceList);
            request.setAttribute("categories", categories);
            request.getRequestDispatcher("/inventory/setPrice.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong SetPriceServlet: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra khi tải dữ liệu giá sản phẩm: " + e.getMessage());
            request.getRequestDispatcher("/inventory/setPrice.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("=== DEBUG: SetPriceServlet POST ===");
        
        // Set encoding
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String action = request.getParameter("action");
            System.out.println("Action: " + action);
            
            if (action == null) {
                action = "update"; // Default action
            }
            
            switch (action) {
                case "update":
                    handleUpdatePrice(request, response);
                    break;
                case "exportExcel":
                    exportExcel(request, response);
                    break;
                case "downloadTemplate":
                    downloadTemplate(request, response);
                    break;
                case "checkExcel":
                    checkExcel(request, response);
                    break;
                case "importExcel":
                    importExcel(request, response);
                    break;
                default:
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Action không hợp lệ\"}");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong SetPriceServlet POST: " + e.getMessage());
            e.printStackTrace();
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Có lỗi xảy ra: " + e.getMessage() + "\"}");
        }
    }
    
    private void handleUpdatePrice(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
            String productIdStr = request.getParameter("productId");
            String size = request.getParameter("size");
            String originalPriceStr = request.getParameter("originalPrice");
            String sellingPriceStr = request.getParameter("sellingPrice");

            if (productIdStr == null || productIdStr.isBlank() || size == null || size.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\":false,\"message\":\"Thiếu productId hoặc size\"}");
                return;
            }

            UUID productId = UUID.fromString(productIdStr);
            BigDecimal originalPrice = originalPriceStr != null && !originalPriceStr.isBlank()
                    ? new BigDecimal(originalPriceStr)
                    : null;
            BigDecimal sellingPrice = sellingPriceStr != null && !sellingPriceStr.isBlank()
                    ? new BigDecimal(sellingPriceStr)
                    : null;

            if (originalPrice == null || sellingPrice == null ||
                    originalPrice.compareTo(BigDecimal.ZERO) < 0 || sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\":false,\"message\":\"Giá không hợp lệ\"}");
                return;
            }
        
            // Validation: giá bán phải >= 1000
        if (sellingPrice.compareTo(new BigDecimal("1000")) < 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"success\":false,\"message\":\"Giá bán tối thiểu 1,000 VND\"}");
                return;
            }
        
        // Validation: Max value check (1 billion)
        BigDecimal MAX_VALUE = new BigDecimal("1000000000");
        if (originalPrice.compareTo(MAX_VALUE) > 0 || sellingPrice.compareTo(MAX_VALUE) > 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"success\":false,\"message\":\"Giá không được vượt quá 1.000.000.000 VND\"}");
            return;
        }

            ProductVariantDAO variantDAO = new ProductVariantDAO();
            ProductVariant variant = variantDAO.findByProductAndSize(productId, size);

            if (variant == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"success\":false,\"message\":\"Không tìm thấy biến thể sản phẩm\"}");
                return;
            }

            variant.setOriginalPrice(originalPrice);
            variant.setPrice(sellingPrice);

            boolean ok = variantDAO.update(variant);
        
        // Update Product status if needed
        if (ok) {
            updateProductStatusBasedOnPrice(productId);
        }
        
            if (ok) {
            out.write("{\"success\":true,\"message\":\"Cập nhật giá thành công\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"success\":false,\"message\":\"Cập nhật thất bại\"}");
            }
    }
    
    private void updateProductStatusBasedOnPrice(UUID productId) {
        try {
            ProductDAO productDAO = new ProductDAO();
            Product product = productDAO.findById(productId);
            
            if (product != null) {
                // Get latest variant prices to determine status
                ProductVariantDAO variantDAO = new ProductVariantDAO();
                ProductVariant variant = variantDAO.findByProductAndSize(productId, "M"); // Default size
                
                if (variant == null) {
                    // Try to get any variant
                    var variants = variantDAO.findAll().stream()
                        .filter(v -> v.getProduct().getProductId().equals(productId) && !v.getIsDeleted())
                        .findFirst();
                    variant = variants.orElse(null);
                }
                
                if (variant != null && variant.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                    product.setStatus("Hết hàng");
                    productDAO.update(product);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not update product status: " + e.getMessage());
        }
    }
    
    // ============================================================
    // EXPORT & IMPORT METHODS
    // ============================================================
    
    private void exportExcel(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            System.out.println("=== DEBUG: Export Prices to Excel ===");
            
            // Get all price data
            List<ProductPriceDTO> prices = productService.getAllProductsWithPriceInfo();
            
            if (prices == null || prices.isEmpty()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Không có dữ liệu để xuất\"}");
                return;
            }
            
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Báo cáo giá");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ProductID", "Mã sản phẩm", "Tên sản phẩm", "Size", "Giá vốn", "Giá bán", "Lợi nhuận", "Danh mục"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // Make header bold
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }
            
            // Fill data rows
            int rowNum = 1;
            for (ProductPriceDTO p : prices) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getProductId() != null ? p.getProductId().toString() : "");
                row.createCell(1).setCellValue(p.getProductCode() != null ? p.getProductCode() : "");
                row.createCell(2).setCellValue(p.getProductName() != null ? p.getProductName() : "");
                row.createCell(3).setCellValue(p.getSize() != null ? p.getSize() : "");
                row.createCell(4).setCellValue(p.getOriginalPrice() != null ? p.getOriginalPrice() : 0.0);
                row.createCell(5).setCellValue(p.getSellingPrice() != null ? p.getSellingPrice() : 0.0);
                
                // Calculate profit
                double profit = (p.getSellingPrice() != null ? p.getSellingPrice() : 0.0) 
                              - (p.getOriginalPrice() != null ? p.getOriginalPrice() : 0.0);
                row.createCell(6).setCellValue(profit);
                
                row.createCell(7).setCellValue(p.getCategoryName() != null ? p.getCategoryName() : "");
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
            
            // Generate filename with date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String filename = "bao_cao_gia_" + sdf.format(new Date()) + ".xlsx";
            
            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(excelData.length);
            
            // Write Excel data to response
            java.io.OutputStream out = response.getOutputStream();
            out.write(excelData);
            out.flush();
            
            System.out.println("✅ Excel export completed successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi export Excel: " + e.getMessage());
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi xuất file: " + e.getMessage() + "\"}");
        }
    }
    
    private void downloadTemplate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            System.out.println("=== DEBUG: Download Price Template ===");
            
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            
            // Sheet 1: Instructions
            Sheet instructionSheet = workbook.createSheet("Hướng dẫn");
            Row titleRow = instructionSheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("HƯỚNG DẪN CẬP NHẬT GIÁ SẢN PHẨM");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Instructions
            int rowNum = 2;
            instructionSheet.createRow(rowNum++).createCell(0).setCellValue("CẤU TRÚC FILE (Sheet 'Giá sản phẩm'):");
            rowNum++;
            
            String[][] instructions = {
                {"Cột A:", "ProductID (bắt buộc, UUID từ file xuất)"},
                {"Cột B:", "Mã sản phẩm (hiển thị, không dùng)"},
                {"Cột C:", "Tên sản phẩm (hiển thị, không dùng)"},
                {"Cột D:", "Size (bắt buộc, ví dụ: S, M, L)"},
                {"Cột E:", "Giá vốn (bắt buộc, >= 0)"},
                {"Cột F:", "Giá bán (bắt buộc, >= 1,000)"},
                {"Cột G:", "Lợi nhuận (tự động tính, không sửa)"},
                {"Cột H:", "Danh mục (hiển thị, không dùng)"}
            };
            
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            
            for (String[] instruction : instructions) {
                Row row = instructionSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(instruction[0]);
                row.createCell(1).setCellValue(instruction[1]);
                row.getCell(0).setCellStyle(boldStyle);
            }
            
            rowNum += 2;
            instructionSheet.createRow(rowNum++).createCell(0).setCellValue("LƯU Ý:");
            rowNum++;
            
            String[] notes = {
                "- Tốt nhất: Xuất file Excel từ 'Xuất báo cáo giá', chỉnh sửa giá và nhập lại",
                "- ProductID phải tồn tại trong hệ thống (từ file xuất)",
                "- Size phải khớp với sản phẩm",
                "- Giá bán phải >= 1,000 VND",
                "- Chỉ cập nhật giá, không tạo sản phẩm hoặc variant mới",
                "- Không sửa ProductID, Size trong file xuất ra"
            };
            
            for (String note : notes) {
                instructionSheet.createRow(rowNum++).createCell(0).setCellValue(note);
            }
            
            instructionSheet.autoSizeColumn(0);
            instructionSheet.autoSizeColumn(1);
            
            // Sheet 2: Template
            Sheet sheet = workbook.createSheet("Giá sản phẩm");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ProductID", "Mã sản phẩm", "Tên sản phẩm", "Size", "Giá vốn", "Giá bán", "Lợi nhuận", "Danh mục"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }
            
            // Add sample data
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("00000000-0000-0000-0000-000000000000"); // Sample ProductID
            sampleRow.createCell(1).setCellValue("SP000001");
            sampleRow.createCell(2).setCellValue("Sản phẩm mẫu");
            sampleRow.createCell(3).setCellValue("M");
            sampleRow.createCell(4).setCellValue(15000);
            sampleRow.createCell(5).setCellValue(25000);
            sampleRow.createCell(6).setCellValue(10000); // Lợi nhuận
            sampleRow.createCell(7).setCellValue("Danh mục");
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write workbook
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] templateData = outputStream.toByteArray();
            workbook.close();
            outputStream.close();
            
            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"mau_cap_nhat_gia.xlsx\"");
            response.setContentLength(templateData.length);
            
            // Write template data
            java.io.OutputStream out = response.getOutputStream();
            out.write(templateData);
            out.flush();
            
            System.out.println("✅ Template download completed successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi download template: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi tải template: " + e.getMessage() + "\"}");
        }
    }
    
    private void checkExcel(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            System.out.println("=== DEBUG: Check Excel File ===");
            
            Part filePart = request.getPart("excelFile");
            if (filePart == null || filePart.getSize() == 0) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Không có file được upload\"}");
                return;
            }
            
            // Read Excel file
            try (java.io.InputStream inputStream = filePart.getInputStream()) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                
                List<String> errors = new ArrayList<>();
                int totalRows = 0;
                int validRows = 0;
                
                // Debug: Check header row
                Row headerRow = sheet.getRow(0);
                if (headerRow != null) {
                    System.out.println("Header cells:");
                    for (int j = 0; j < 7; j++) {
                        String cellValue = getCellValueAsString(headerRow.getCell(j));
                        System.out.println("  Column " + j + ": " + cellValue);
                    }
                }
                
                // Check each row (skip header row 0)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    // Debug first data row
                    if (i == 1) {
                        System.out.println("First data row cells:");
                        for (int j = 0; j < 7; j++) {
                            String cellValue = getCellValueAsString(row.getCell(j));
                            System.out.println("  Column " + j + ": " + cellValue);
                        }
                    }
                    
                    totalRows++;
                    boolean rowValid = true;
                    
                    // Validate columns (file xuất báo cáo có 8 cột: A-ProductID, B-Mã, C-Tên, D-Size, E-Giá vốn, F-Giá bán, G-Lợi nhuận, H-Danh mục)
                    String productIdStr = getCellValueAsString(row.getCell(0)); // Cột A: ProductID
                    String size = getCellValueAsString(row.getCell(3)); // Cột D: Size
                    String originalPriceStr = getCellValueAsString(row.getCell(4)); // Cột E: Giá vốn
                    String sellingPriceStr = getCellValueAsString(row.getCell(5)); // Cột F: Giá bán
                    
                    // Check productId exists
                    if (productIdStr == null || productIdStr.trim().isEmpty()) {
                        errors.add("Dòng " + (i + 1) + ": ProductID không được để trống");
                        rowValid = false;
                    }
                    
                    // Check size
                    if (size == null || size.trim().isEmpty()) {
                        errors.add("Dòng " + (i + 1) + ": Size không được để trống");
                        rowValid = false;
                    }
                    
                    // Check prices
                    try {
                        double originalPrice = Double.parseDouble(originalPriceStr);
                        if (originalPrice < 0) {
                            errors.add("Dòng " + (i + 1) + ": Giá vốn phải >= 0");
                            rowValid = false;
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Dòng " + (i + 1) + ": Giá vốn không hợp lệ");
                        rowValid = false;
                    }
                    
                    try {
                        double sellingPrice = Double.parseDouble(sellingPriceStr);
                        if (sellingPrice < 1000) {
                            errors.add("Dòng " + (i + 1) + ": Giá bán phải >= 1,000");
                            rowValid = false;
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Dòng " + (i + 1) + ": Giá bán không hợp lệ");
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
                
                response.setContentType("application/json");
                response.getWriter().write(jsonResponse.toString());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi check Excel: " + e.getMessage());
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi kiểm tra file: " + e.getMessage() + "\"}");
        }
    }
    
    private void importExcel(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            System.out.println("=== DEBUG: Import Excel ===");
            
            Part filePart = request.getPart("file");
            if (filePart == null) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Không tìm thấy file\"}");
                return;
            }
            
            // Read Excel file
            try (java.io.InputStream inputStream = filePart.getInputStream()) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                
                int successCount = 0;
                int errorCount = 0;
                List<String> errors = new ArrayList<>();
                
                // Import each row (skip header row 0)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    try {
                        // Import columns from exported file (A-ProductID, D-Size, E-Giá vốn, F-Giá bán)
                        String productIdStr = getCellValueAsString(row.getCell(0)); // Cột A: ProductID
                        String size = getCellValueAsString(row.getCell(3)); // Cột D: Size
                        String originalPriceStr = getCellValueAsString(row.getCell(4)); // Cột E: Giá vốn
                        String sellingPriceStr = getCellValueAsString(row.getCell(5)); // Cột F: Giá bán
                        
                        // Validate ProductID
                        if (productIdStr == null || productIdStr.trim().isEmpty()) {
                            errors.add("Dòng " + (i + 1) + ": ProductID không được để trống");
                            errorCount++;
                            continue;
                        }
                        
                        // Parse ProductID
                        UUID productId;
                        try {
                            productId = UUID.fromString(productIdStr);
                        } catch (IllegalArgumentException e) {
                            errors.add("Dòng " + (i + 1) + ": ProductID không hợp lệ");
                            errorCount++;
                            continue;
                        }
                        
                        if (size == null || size.trim().isEmpty()) {
                            errors.add("Dòng " + (i + 1) + ": Size không được để trống");
                            errorCount++;
                            continue;
                        }
                        
                        double originalPrice, sellingPrice;
                        try {
                            originalPrice = Double.parseDouble(originalPriceStr);
                            if (originalPrice < 0) {
                                errors.add("Dòng " + (i + 1) + ": Giá vốn phải >= 0");
                                errorCount++;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            errors.add("Dòng " + (i + 1) + ": Giá vốn không hợp lệ");
                            errorCount++;
                            continue;
                        }
                        
                        try {
                            sellingPrice = Double.parseDouble(sellingPriceStr);
                            if (sellingPrice < 1000) {
                                errors.add("Dòng " + (i + 1) + ": Giá bán phải >= 1,000");
                                errorCount++;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            errors.add("Dòng " + (i + 1) + ": Giá bán không hợp lệ");
                            errorCount++;
                            continue;
                        }
                        
                        // Find variant by ProductID and Size directly
                        ProductVariantDAO variantDAO = new ProductVariantDAO();
                        ProductVariant variant = variantDAO.findByProductAndSize(productId, size.trim());
                        
                        if (variant == null) {
                            errors.add("Dòng " + (i + 1) + ": Không tìm thấy variant với ProductID '" + productIdStr + "' và size '" + size + "'");
                            errorCount++;
                            continue;
                        }
                        
                        // Update prices
                        variant.setOriginalPrice(BigDecimal.valueOf(originalPrice));
                        variant.setPrice(BigDecimal.valueOf(sellingPrice));
                        
                        boolean updated = variantDAO.update(variant);
                        
                        if (updated) {
                            successCount++;
                            System.out.println("✅ Updated: " + productIdStr + " - " + size);
                        } else {
                            errors.add("Dòng " + (i + 1) + ": Không thể cập nhật giá");
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
                jsonResponse.append(", \"message\": \"Import thành công ").append(successCount).append(" bản ghi");
                if (errorCount > 0) {
                    jsonResponse.append(", ").append(errorCount).append(" bản ghi lỗi");
                }
                jsonResponse.append("\"");
                jsonResponse.append(", \"successCount\": ").append(successCount);
                jsonResponse.append(", \"errorCount\": ").append(errorCount);
                jsonResponse.append(", \"errors\": [");
                
                for (int i = 0; i < Math.min(errors.size(), 10); i++) {
                    if (i > 0) jsonResponse.append(", ");
                    jsonResponse.append("\"").append(errors.get(i).replace("\"", "\\\"")).append("\"");
                }
                
                jsonResponse.append("]}");
                
                response.setContentType("application/json");
                response.getWriter().write(jsonResponse.toString());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi import Excel: " + e.getMessage());
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Lỗi khi xử lý file: " + e.getMessage() + "\"}");
        }
    }
    
    // Helper methods
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
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
    
}
