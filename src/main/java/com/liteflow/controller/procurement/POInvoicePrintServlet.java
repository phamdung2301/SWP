package com.liteflow.controller.procurement;

import com.liteflow.model.procurement.PurchaseOrder;
import com.liteflow.model.procurement.Supplier;
import com.liteflow.service.procurement.ProcurementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet for printing invoice from Purchase Order
 * Endpoint: /procurement/invoice/print?poid={uuid}
 */
@WebServlet("/procurement/invoice/print")
public class POInvoicePrintServlet extends HttpServlet {
    
    private final ProcurementService service = new ProcurementService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String poidParam = request.getParameter("poid");
        
        if (poidParam == null || poidParam.trim().isEmpty()) {
            sendErrorPage(response, "Thiếu thông tin đơn hàng", "Vui lòng cung cấp mã đơn hàng để in hóa đơn.");
            return;
        }
        
        try {
            UUID poid = UUID.fromString(poidParam);
            
            // Generate invoice data
            Map<String, Object> invoiceData = service.generateInvoiceFromPO(poid);
            
            // Generate HTML invoice
            String html = generateInvoiceHTML(invoiceData);
            
            // Set response
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println(html);
            out.flush();
            
        } catch (IllegalArgumentException e) {
            sendErrorPage(response, "Mã đơn hàng không hợp lệ", "Mã đơn hàng không đúng định dạng.");
        } catch (RuntimeException e) {
            sendErrorPage(response, "Lỗi", e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error generating invoice: " + e.getMessage());
            e.printStackTrace();
            sendErrorPage(response, "Lỗi hệ thống", "Đã xảy ra lỗi khi tạo hóa đơn. Vui lòng thử lại sau.");
        }
    }
    
    /**
     * Generate HTML invoice from invoice data
     */
    private String generateInvoiceHTML(Map<String, Object> invoiceData) {
        PurchaseOrder po = (PurchaseOrder) invoiceData.get("po");
        Supplier supplier = (Supplier) invoiceData.get("supplier");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) invoiceData.get("items");
        Double totalAmount = (Double) invoiceData.get("totalAmount"); // Tổng tiền sau thuế
        Double subTotal = (Double) invoiceData.get("subTotal"); // Tổng tiền trước thuế
        Double taxAmount = (Double) invoiceData.get("taxAmount"); // Tiền thuế
        Double vatRate = (Double) invoiceData.get("vatRate"); // Thuế suất VAT
        String companyTaxCode = (String) invoiceData.get("companyTaxCode");
        String supplierTaxCode = (String) invoiceData.get("supplierTaxCode");
        LocalDateTime deliveryDate = (LocalDateTime) invoiceData.get("deliveryDate");
        LocalDateTime printDate = (LocalDateTime) invoiceData.get("printDate");
        
        // Get company info
        com.liteflow.service.CompanyInfoService companyInfoService = new com.liteflow.service.CompanyInfoService();
        java.util.Map<String, Object> companyInfo = companyInfoService.getCompanyInfoWithTaxCode();
        String companyName = (String) companyInfo.getOrDefault("name", "LiteFlow Restaurant");
        String companyAddress = (String) companyInfo.getOrDefault("address", "123 Nguyễn Huệ, Quận 1, TP.HCM");
        String companyPhone = (String) companyInfo.getOrDefault("phone", "1900-1234");
        String companyEmail = (String) companyInfo.getOrDefault("email", "procurement@liteflow.com");
        String companyTaxCodeFromEnv = (String) companyInfo.get("taxCode");
        if (companyTaxCodeFromEnv != null) {
            companyTaxCode = companyTaxCodeFromEnv;
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("dd 'tháng' MM 'năm' yyyy");
        DecimalFormat numberFormat = new DecimalFormat("#,###");
        
        // Format dates
        String deliveryDateStr = deliveryDate != null ? 
            deliveryDate.format(dateFormatter) : (po.getExpectedDelivery() != null ? 
                po.getExpectedDelivery().format(dateFormatter) : "N/A");
        
        // Format full date for header (use deliveryDate or printDate)
        LocalDateTime dateForHeader = deliveryDate != null ? deliveryDate : 
            (printDate != null ? printDate : LocalDateTime.now());
        String fullDateStr = dateForHeader.format(fullDateFormatter);
        
        // Format PO ID (last 8 characters for invoice number)
        String poIdFull = po.getPoid().toString().replace("-", "");
        String invoiceNumber = poIdFull.length() >= 8 ? 
            poIdFull.substring(poIdFull.length() - 8) : poIdFull;
        
        // Hardcode values for demo
        String serialNo = "2C25TTU";
        String taxAuthorityCode = "007A6C29893BB04F7D9212CC9E5A863F8A";
        String paymentMethod = "Chuyển khoản";
        
        // Format total amount
        String totalAmountStr = numberFormat.format(totalAmount) + " VNĐ";
        String totalAmountWords = convertNumberToWords(totalAmount);
        
        // Build items table
        StringBuilder itemsTable = new StringBuilder();
        int stt = 1;
        for (Map<String, Object> item : items) {
            String itemName = escapeHtml((String) item.get("itemName"));
            Integer quantity = (Integer) item.get("quantity");
            Double unitPrice = (Double) item.get("unitPrice");
            Double total = (Double) item.get("total");
            
            itemsTable.append("<tr>");
            itemsTable.append("<td style=\"text-align: center; padding: 6px; border: 1px solid #000;\">").append(stt++).append("</td>");
            itemsTable.append("<td style=\"padding: 6px; border: 1px solid #000;\">").append(itemName).append("</td>");
            itemsTable.append("<td style=\"text-align: center; padding: 6px; border: 1px solid #000;\">").append("Cái").append("</td>");
            itemsTable.append("<td style=\"text-align: right; padding: 6px; border: 1px solid #000;\">").append(quantity).append("</td>");
            itemsTable.append("<td style=\"text-align: right; padding: 6px; border: 1px solid #000;\">").append(numberFormat.format(unitPrice)).append("</td>");
            itemsTable.append("<td style=\"text-align: right; padding: 6px; border: 1px solid #000; font-weight: bold;\">").append(numberFormat.format(total)).append("</td>");
            itemsTable.append("</tr>");
        }
        
        // Supplier info
        String supplierName = supplier != null && supplier.getName() != null ? escapeHtml(supplier.getName()) : "N/A";
        String supplierAddress = supplier != null && supplier.getAddress() != null ? escapeHtml(supplier.getAddress()) : "";
        String supplierPhone = supplier != null && supplier.getPhone() != null ? escapeHtml(supplier.getPhone()) : "";
        String supplierEmail = supplier != null && supplier.getEmail() != null ? escapeHtml(supplier.getEmail()) : "";
        
        // Build HTML
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='vi'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Hóa đơn mua hàng - ").append(invoiceNumber).append("</title>");
        html.append("<style>");
        html.append("@page { size: A4; margin: 15mm; }");
        html.append("body { font-family: 'Times New Roman', serif; font-size: 13px; line-height: 1.5; color: #000; margin: 0; padding: 0; background: #f5f5f5; }");
        html.append(".invoice-container { max-width: 210mm; margin: 0 auto; padding: 15px; background: #fff; border: 3px solid #E3F2FD; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; margin-bottom: 15px; border-bottom: 2px solid #000; padding-bottom: 10px; background: #f8f9fa; padding: 10px; border-radius: 4px; }");
        html.append(".header h1 { font-size: 20px; font-weight: bold; margin: 5px 0; text-transform: uppercase; letter-spacing: 1px; }");
        html.append(".header-info { font-size: 11px; margin: 2px 0; line-height: 1.4; }");
        html.append(".header-info strong { font-weight: bold; }");
        html.append(".info-section { margin-bottom: 20px; }");
        html.append(".info-box { padding: 10px; border: none; }");
        html.append(".info-box:first-child { border-bottom: 1px solid #ddd; padding-bottom: 10px; margin-bottom: 10px; }");
        html.append(".info-box h3 { font-size: 13px; font-weight: bold; margin: 0 0 8px 0; text-transform: uppercase; border-bottom: 1px solid #ddd; padding-bottom: 4px; }");
        html.append(".info-box p { margin: 3px 0; font-size: 11px; line-height: 1.4; }");
        html.append(".order-info { margin-bottom: 15px; }");
        html.append(".order-info table { width: 100%; border-collapse: collapse; }");
        html.append(".order-info td { padding: 4px 8px; font-size: 11px; }");
        html.append(".order-info td:first-child { font-weight: bold; width: 120px; }");
        html.append(".items-table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append(".items-table th { background: #f0f0f0; padding: 6px; border: 1px solid #000; text-align: center; font-weight: bold; font-size: 11px; }");
        html.append(".items-table td { padding: 6px; border: 1px solid #000; font-size: 10px; }");
        html.append(".total-section { margin-top: 15px; text-align: right; }");
        html.append(".total-section p { margin: 4px 0; font-size: 12px; }");
        html.append(".total-amount { font-size: 15px; font-weight: bold; margin-top: 8px; }");
        html.append(".total-words { font-style: italic; margin-top: 4px; font-size: 11px; }");
        html.append(".notes-section { margin-top: 20px; padding: 8px; border: 1px solid #ddd; }");
        html.append(".notes-section p { margin: 4px 0; font-size: 11px; }");
        html.append(".footer { margin-top: 30px; display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }");
        html.append(".signature-box { text-align: center; }");
        html.append(".signature-box p { margin: 30px 0 5px 0; font-size: 11px; }");
        html.append(".signature-line { border-top: 1px solid #000; width: 200px; margin: 0 auto; padding-top: 5px; }");
        html.append("@media print {");
        html.append("  body { margin: 0; padding: 0; }");
        html.append("  .invoice-container { padding: 0; }");
        html.append("  .no-print { display: none; }");
        html.append("}");
        html.append("</style>");
        html.append("<script>");
        html.append("window.onload = function() { window.print(); }");
        html.append("</script>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='invoice-container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>HÓA ĐƠN MUA HÀNG</h1>");
        html.append("<div class='header-info'><strong>Mẫu số - Ký hiệu:</strong> ").append(serialNo).append("</div>");
        html.append("<div class='header-info'><strong>Số:</strong> ").append(invoiceNumber).append("</div>");
        html.append("<div class='header-info'><strong>Mã của Cơ quan thuế:</strong> ").append(taxAuthorityCode).append("</div>");
        html.append("<div class='header-info'><strong>Ngày</strong> ").append(fullDateStr).append("</div>");
        html.append("</div>");
        
        // Info sections - Vertical layout (Seller on top, Buyer below)
        html.append("<div class='info-section'>");
        
        // Bên bán (Supplier) - Top
        html.append("<div class='info-box'>");
        html.append("<h3>Đơn vị bán</h3>");
        html.append("<p><strong>").append(supplierName).append("</strong></p>");
        if (supplierTaxCode != null && !supplierTaxCode.isEmpty()) {
            html.append("<p>Mã số thuế: ").append(escapeHtml(supplierTaxCode)).append("</p>");
        }
        if (!supplierAddress.isEmpty()) {
            html.append("<p>Địa chỉ: ").append(supplierAddress).append("</p>");
        }
        if (!supplierPhone.isEmpty()) {
            html.append("<p>Điện thoại: ").append(supplierPhone).append("</p>");
        }
        if (!supplierEmail.isEmpty()) {
            html.append("<p>Email: ").append(supplierEmail).append("</p>");
        }
        html.append("<p>HTTT: ").append(paymentMethod).append("</p>");
        html.append("</div>");
        
        // Bên mua (LiteFlow) - Bottom
        html.append("<div class='info-box'>");
        html.append("<h3>Người mua</h3>");
        html.append("<p><strong>").append(escapeHtml(companyName)).append("</strong></p>");
        if (companyTaxCode != null && !companyTaxCode.isEmpty()) {
            html.append("<p>Mã số thuế: ").append(escapeHtml(companyTaxCode)).append("</p>");
        }
        if (companyAddress != null && !companyAddress.isEmpty()) {
            html.append("<p>Địa chỉ: ").append(escapeHtml(companyAddress)).append("</p>");
        }
        if (companyPhone != null && !companyPhone.isEmpty()) {
            html.append("<p>Điện thoại: ").append(escapeHtml(companyPhone)).append("</p>");
        }
        if (companyEmail != null && !companyEmail.isEmpty()) {
            html.append("<p>Email: ").append(escapeHtml(companyEmail)).append("</p>");
        }
        html.append("<p>HTTT: ").append(paymentMethod).append("</p>");
        html.append("</div>");
        
        html.append("</div>");
        
        // Order info (compact - only essential info)
        html.append("<div class='order-info'>");
        html.append("<table>");
        html.append("<tr><td>Mã đơn hàng:</td><td><strong>").append(po.getPoid().toString()).append("</strong></td></tr>");
        html.append("<tr><td>Ngày giao:</td><td>").append(deliveryDateStr).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Items table
        html.append("<table class='items-table'>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>STT</th>");
        html.append("<th>Tên hàng hóa, dịch vụ</th>");
        html.append("<th>ĐVT</th>");
        html.append("<th>SL</th>");
        html.append("<th>Đơn giá</th>");
        html.append("<th>Thành tiền</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");
        html.append(itemsTable.toString());
        html.append("</tbody>");
        html.append("</table>");
        
        // Tax breakdown section
        html.append("<div class='total-section' style='margin-top: 15px;'>");
        html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 8px;'>");
        html.append("<tr>");
        html.append("<td style='text-align: right; padding: 6px; font-size: 11px;'><strong>Tổng tiền trước thuế:</strong></td>");
        html.append("<td style='text-align: right; padding: 6px; font-size: 11px;'><strong>").append(numberFormat.format(subTotal != null ? subTotal : 0)).append(" VNĐ</strong></td>");
        html.append("</tr>");
        if (vatRate != null && vatRate > 0) {
            html.append("<tr>");
            html.append("<td style='text-align: right; padding: 6px; font-size: 11px;'>Thuế suất VAT (").append(numberFormat.format(vatRate)).append("%):</td>");
            html.append("<td style='text-align: right; padding: 6px; font-size: 11px;'>").append(numberFormat.format(taxAmount != null ? taxAmount : 0)).append(" VNĐ</td>");
            html.append("</tr>");
        }
        html.append("<tr style='border-top: 2px solid #000;'>");
        html.append("<td style='text-align: right; padding: 6px; font-size: 13px; font-weight: bold;'>Tổng tiền thanh toán:</td>");
        html.append("<td style='text-align: right; padding: 6px; font-size: 13px; font-weight: bold;'>").append(totalAmountStr).append("</td>");
        html.append("</tr>");
        html.append("</table>");
        html.append("<p class='total-words' style='margin-top: 8px;'>Bằng chữ: ").append(totalAmountWords).append("</p>");
        html.append("</div>");
        
        // Notes
        if (po.getNotes() != null && !po.getNotes().trim().isEmpty()) {
            html.append("<div class='notes-section'>");
            html.append("<p><strong>Ghi chú:</strong></p>");
            html.append("<p>").append(escapeHtml(po.getNotes())).append("</p>");
            html.append("</div>");
        }
        
        // Footer with signatures
        html.append("<div class='footer'>");
        html.append("<div class='signature-box'>");
        html.append("<p><strong>Người mua</strong></p>");
        html.append("<div class='signature-line'></div>");
        html.append("<p style='margin-top: 5px; font-size: 11px;'>(Ký, ghi rõ họ tên)</p>");
        html.append("</div>");
        html.append("<div class='signature-box'>");
        html.append("<p><strong>Người bán</strong></p>");
        html.append("<div class='signature-line'></div>");
        html.append("<p style='margin-top: 5px; font-size: 11px;'>(Ký, ghi rõ họ tên)</p>");
        html.append("</div>");
        html.append("</div>");
        
        // Footer note
        html.append("<div style='margin-top: 20px; text-align: center; font-size: 10px; color: #666; padding-top: 8px;'>");
        html.append("<p style='font-style: italic; margin: 3px 0;'>(Cần kiểm tra đối chiếu khi lập, giao, nhận hóa đơn)</p>");
        html.append("<p style='margin-top: 3px;'>Hóa đơn được tạo tự động từ hệ thống LiteFlow</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Convert number to Vietnamese words (simplified version)
     */
    private String convertNumberToWords(double amount) {
        if (amount == 0) return "Không đồng";
        
        long vnd = Math.round(amount);
        if (vnd == 0) return "Không đồng";
        
        // For large numbers, just format and return
        if (vnd >= 1000000000) {
            return numberFormat.format(vnd) + " đồng";
        }
        
        // Simplified conversion for common cases
        String[] ones = {"", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
        String[] tens = {"", "mười", "hai mươi", "ba mươi", "bốn mươi", "năm mươi", "sáu mươi", "bảy mươi", "tám mươi", "chín mươi"};
        
        StringBuilder result = new StringBuilder();
        
        // Handle millions
        if (vnd >= 1000000) {
            long millions = vnd / 1000000;
            vnd = vnd % 1000000;
            result.append(convertSmallNumber(millions, ones, tens)).append(" triệu ");
        }
        
        // Handle thousands
        if (vnd >= 1000) {
            long thousands = vnd / 1000;
            vnd = vnd % 1000;
            result.append(convertSmallNumber(thousands, ones, tens)).append(" nghìn ");
        }
        
        // Handle remainder
        if (vnd > 0) {
            result.append(convertSmallNumber(vnd, ones, tens));
        }
        
        result.append(" đồng");
        return result.toString().trim();
    }
    
    /**
     * Convert small number (0-999) to Vietnamese words
     */
    private String convertSmallNumber(long num, String[] ones, String[] tens) {
        if (num == 0) return "";
        if (num < 10) return ones[(int)num];
        if (num < 20) {
            if (num == 10) return "mười";
            return "mười " + ones[(int)(num % 10)];
        }
        if (num < 100) {
            int ten = (int)(num / 10);
            int one = (int)(num % 10);
            if (one == 0) return tens[ten];
            if (one == 1) return tens[ten] + " mốt";
            if (one == 5) return tens[ten] + " lăm";
            return tens[ten] + " " + ones[one];
        }
        if (num < 1000) {
            int hundred = (int)(num / 100);
            long remainder = num % 100;
            String hundredStr = ones[hundred] + " trăm";
            if (remainder == 0) return hundredStr;
            if (remainder < 10) return hundredStr + " linh " + ones[(int)remainder];
            return hundredStr + " " + convertSmallNumber(remainder, ones, tens);
        }
        return "";
    }
    
    /**
     * Escape HTML special characters
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Send error page
     */
    private void sendErrorPage(HttpServletResponse response, String title, String message) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='vi'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Lỗi - " + escapeHtml(title) + "</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }");
        out.println(".error-box { max-width: 500px; margin: 0 auto; padding: 30px; border: 2px solid #dc3545; border-radius: 8px; background: #f8d7da; }");
        out.println("h1 { color: #dc3545; }");
        out.println("p { color: #721c24; }");
        out.println("a { color: #0066ff; text-decoration: none; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='error-box'>");
        out.println("<h1>" + escapeHtml(title) + "</h1>");
        out.println("<p>" + escapeHtml(message) + "</p>");
        out.println("<p><a href='javascript:window.close()'>Đóng cửa sổ</a></p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
        out.flush();
    }
    
    private final java.text.DecimalFormat numberFormat = new java.text.DecimalFormat("#,###");
}

