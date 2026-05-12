package com.liteflow.controller.report;

import com.liteflow.service.report.RevenueReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servlet for printing revenue report
 * Endpoint: /report/revenue/print?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
 */
@WebServlet("/report/revenue/print")
public class RevenueReportPrintServlet extends HttpServlet {
    
    private final RevenueReportService reportService = new RevenueReportService();
    private final DecimalFormat numberFormat = new DecimalFormat("#,###");
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        
        if (startDateStr == null || endDateStr == null) {
            sendErrorPage(response, "Thiếu thông tin", "Vui lòng cung cấp khoảng thời gian báo cáo.");
            return;
        }
        
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            
            // Get report data
            Map<String, Object> reportData = reportService.getReportDataForPrint(startDate, endDate);
            
            // Generate HTML report
            String html = generateReportHTML(reportData);
            
            // Set response
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println(html);
            out.flush();
            
        } catch (Exception e) {
            System.err.println("❌ Error generating revenue report: " + e.getMessage());
            e.printStackTrace();
            sendErrorPage(response, "Lỗi hệ thống", "Đã xảy ra lỗi khi tạo báo cáo. Vui lòng thử lại sau.");
        }
    }
    
    /**
     * Generate HTML report from report data
     */
    private String generateReportHTML(Map<String, Object> reportData) {
        // Get company info
        com.liteflow.service.CompanyInfoService companyInfoService = new com.liteflow.service.CompanyInfoService();
        java.util.Map<String, Object> companyInfo = companyInfoService.getCompanyInfoWithTaxCode();
        String companyName = (String) companyInfo.getOrDefault("name", "LiteFlow Restaurant");
        String companyAddress = (String) companyInfo.getOrDefault("address", "");
        String companyPhone = (String) companyInfo.getOrDefault("phone", "");
        String companyEmail = (String) companyInfo.getOrDefault("email", "");
        
        // Extract report data
        LocalDate startDate = (LocalDate) reportData.get("startDate");
        LocalDate endDate = (LocalDate) reportData.get("endDate");
        boolean isSingleDay = (Boolean) reportData.getOrDefault("isSingleDay", false);
        
        BigDecimal totalRevenue = (BigDecimal) reportData.get("totalRevenue");
        Long totalOrders = ((Number) reportData.get("totalOrders")).longValue();
        BigDecimal avgOrderValue = (BigDecimal) reportData.get("avgOrderValue");
        BigDecimal totalProfit = (BigDecimal) reportData.getOrDefault("totalProfit", totalRevenue);
        Double growthRate = (Double) reportData.getOrDefault("growthRate", 0.0);
        Double profitGrowthRate = (Double) reportData.getOrDefault("profitGrowthRate", 0.0);
        Long newCustomers = ((Number) reportData.getOrDefault("newCustomers", 0)).longValue();
        Long returningCustomers = ((Number) reportData.getOrDefault("returningCustomers", 0)).longValue();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> hourlyData = (Map<String, Object>) reportData.get("hourlyData");
        @SuppressWarnings("unchecked")
        Map<String, Object> dailyData = (Map<String, Object>) reportData.get("dailyData");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topProducts = (List<Map<String, Object>>) reportData.get("topProducts");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categories = (List<Map<String, Object>>) reportData.get("categories");
        
        // Format dates
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("dd 'tháng' MM 'năm' yyyy");
        String startDateStr = startDate.format(dateFormatter);
        String endDateStr = endDate.format(dateFormatter);
        String dateRangeStr = isSingleDay ? startDateStr : (startDateStr + " - " + endDateStr);
        String printDateStr = LocalDateTime.now().format(fullDateFormatter);
        
        // Build HTML
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='vi'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Báo cáo Doanh thu - ").append(dateRangeStr).append("</title>");
        html.append("<style>");
        html.append("@page { size: A4; margin: 15mm; }");
        html.append("body { font-family: 'Times New Roman', serif; font-size: 12px; line-height: 1.5; color: #000; margin: 0; padding: 0; background: #f5f5f5; }");
        html.append(".report-container { max-width: 210mm; margin: 0 auto; padding: 15px; background: #fff; }");
        html.append(".header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #000; padding-bottom: 15px; }");
        html.append(".header h1 { font-size: 22px; font-weight: bold; margin: 10px 0; text-transform: uppercase; }");
        html.append(".header-info { font-size: 11px; margin: 3px 0; }");
        html.append(".company-info { margin-bottom: 15px; padding: 10px; background: #f9f9f9; border: 1px solid #ddd; }");
        html.append(".company-info p { margin: 3px 0; font-size: 11px; }");
        html.append(".section { margin-bottom: 20px; }");
        html.append(".section-title { font-size: 14px; font-weight: bold; margin-bottom: 10px; padding-bottom: 5px; border-bottom: 1px solid #000; }");
        html.append(".summary-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-bottom: 15px; }");
        html.append(".summary-item { padding: 8px; background: #f9f9f9; border: 1px solid #ddd; }");
        html.append(".summary-label { font-size: 10px; color: #666; margin-bottom: 3px; }");
        html.append(".summary-value { font-size: 13px; font-weight: bold; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 11px; }");
        html.append("th, td { border: 1px solid #000; padding: 6px; text-align: left; }");
        html.append("th { background: #f0f0f0; font-weight: bold; text-align: center; }");
        html.append("td { text-align: right; }");
        html.append("td:first-child { text-align: left; }");
        html.append(".footer { margin-top: 30px; padding-top: 15px; border-top: 1px solid #ddd; text-align: center; font-size: 10px; color: #666; }");
        html.append(".signature-section { margin-top: 30px; display: grid; grid-template-columns: 1fr 1fr; gap: 50px; }");
        html.append(".signature-box { text-align: center; }");
        html.append(".signature-line { border-top: 1px solid #000; width: 200px; margin: 20px auto 5px; }");
        html.append("@media print {");
        html.append("  body { margin: 0; padding: 0; }");
        html.append("  .report-container { padding: 0; }");
        html.append("}");
        html.append("</style>");
        html.append("<script>");
        html.append("window.onload = function() { window.print(); }");
        html.append("</script>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='report-container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>BÁO CÁO DOANH THU</h1>");
        html.append("<div class='header-info'>Khoảng thời gian: <strong>").append(dateRangeStr).append("</strong></div>");
        html.append("<div class='header-info'>Ngày in: ").append(printDateStr).append("</div>");
        html.append("</div>");
        
        // Company Info
        html.append("<div class='company-info'>");
        html.append("<p><strong>").append(escapeHtml(companyName)).append("</strong></p>");
        if (companyAddress != null && !companyAddress.isEmpty()) {
            html.append("<p>Địa chỉ: ").append(escapeHtml(companyAddress)).append("</p>");
        }
        if (companyPhone != null && !companyPhone.isEmpty()) {
            html.append("<p>Điện thoại: ").append(escapeHtml(companyPhone)).append("</p>");
        }
        if (companyEmail != null && !companyEmail.isEmpty()) {
            html.append("<p>Email: ").append(escapeHtml(companyEmail)).append("</p>");
        }
        html.append("</div>");
        
        // Summary Section
        html.append("<div class='section'>");
        html.append("<div class='section-title'>TỔNG QUAN</div>");
        html.append("<div class='summary-grid'>");
        
        addSummaryItem(html, "Tổng doanh thu", formatCurrency(totalRevenue.doubleValue()));
        addSummaryItem(html, "Tổng số đơn hàng", numberFormat.format(totalOrders));
        addSummaryItem(html, "Giá trị đơn hàng TB", formatCurrency(avgOrderValue.doubleValue()));
        addSummaryItem(html, "Tổng lợi nhuận", formatCurrency(totalProfit.doubleValue()));
        addSummaryItem(html, "Tỷ lệ tăng trưởng", String.format("%.1f%%", growthRate));
        addSummaryItem(html, "Tăng trưởng lợi nhuận", String.format("%.1f%%", profitGrowthRate));
        addSummaryItem(html, "Khách hàng mới", numberFormat.format(newCustomers));
        addSummaryItem(html, "Khách hàng quay lại", numberFormat.format(returningCustomers));
        
        html.append("</div>");
        html.append("</div>");
        
        // Details Section - Hourly or Daily
        if (isSingleDay && hourlyData != null) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>DOANH THU THEO GIỜ</div>");
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>Giờ</th>");
            html.append("<th>Doanh thu (VNĐ)</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            @SuppressWarnings("unchecked")
            List<String> hours = (List<String>) hourlyData.get("hours");
            @SuppressWarnings("unchecked")
            List<Double> revenues = (List<Double>) hourlyData.get("revenues");
            
            if (hours != null && revenues != null && !hours.isEmpty()) {
                for (int i = 0; i < hours.size() && i < revenues.size(); i++) {
                    html.append("<tr>");
                    html.append("<td>").append(hours.get(i)).append("</td>");
                    html.append("<td>").append(formatCurrency(revenues.get(i))).append("</td>");
                    html.append("</tr>");
                }
            } else {
                html.append("<tr><td colspan='2' style='text-align: center; padding: 20px;'>Không có dữ liệu</td></tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        } else if (!isSingleDay && dailyData != null) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>DOANH THU THEO NGÀY</div>");
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>Ngày</th>");
            html.append("<th>Doanh thu (VNĐ)</th>");
            html.append("<th>Số đơn</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            @SuppressWarnings("unchecked")
            List<String> dates = (List<String>) dailyData.get("dates");
            @SuppressWarnings("unchecked")
            List<Double> revenues = (List<Double>) dailyData.get("revenues");
            @SuppressWarnings("unchecked")
            List<Long> orders = (List<Long>) dailyData.get("orders");
            
            if (dates != null && revenues != null && orders != null && !dates.isEmpty()) {
                for (int i = 0; i < dates.size() && i < revenues.size() && i < orders.size(); i++) {
                    html.append("<tr>");
                    html.append("<td>").append(dates.get(i)).append("</td>");
                    html.append("<td>").append(formatCurrency(revenues.get(i))).append("</td>");
                    html.append("<td>").append(numberFormat.format(orders.get(i))).append("</td>");
                    html.append("</tr>");
                }
            } else {
                html.append("<tr><td colspan='3' style='text-align: center; padding: 20px;'>Không có dữ liệu</td></tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        
        // Top Products Section
        if (topProducts != null && !topProducts.isEmpty()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>TOP SẢN PHẨM BÁN CHẠY</div>");
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>STT</th>");
            html.append("<th>Tên sản phẩm</th>");
            html.append("<th>Số lượng</th>");
            html.append("<th>Doanh thu (VNĐ)</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            int stt = 1;
            for (Map<String, Object> product : topProducts) {
                html.append("<tr>");
                html.append("<td>").append(stt++).append("</td>");
                html.append("<td>").append(escapeHtml((String) product.get("name"))).append("</td>");
                html.append("<td>").append(numberFormat.format(product.get("quantity"))).append("</td>");
                html.append("<td>").append(formatCurrency(((Number) product.get("revenue")).doubleValue())).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        
        // Category Revenue Section
        if (categories != null && !categories.isEmpty()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>DOANH THU THEO DANH MỤC</div>");
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>Tên danh mục</th>");
            html.append("<th>Doanh thu (VNĐ)</th>");
            html.append("<th>Tỷ lệ %</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (Map<String, Object> category : categories) {
                html.append("<tr>");
                html.append("<td>").append(escapeHtml((String) category.get("name"))).append("</td>");
                html.append("<td>").append(formatCurrency(((Number) category.get("revenue")).doubleValue())).append("</td>");
                html.append("<td>").append(category.get("percentage")).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Báo cáo được tạo tự động từ hệ thống LiteFlow</p>");
        html.append("</div>");
        
        // Signature Section
        html.append("<div class='signature-section'>");
        html.append("<div class='signature-box'>");
        html.append("<p><strong>Người lập</strong></p>");
        html.append("<div class='signature-line'></div>");
        html.append("<p style='margin-top: 5px; font-size: 10px;'>(Ký, ghi rõ họ tên)</p>");
        html.append("</div>");
        html.append("<div class='signature-box'>");
        html.append("<p><strong>Người duyệt</strong></p>");
        html.append("<div class='signature-line'></div>");
        html.append("<p style='margin-top: 5px; font-size: 10px;'>(Ký, ghi rõ họ tên)</p>");
        html.append("</div>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    private void addSummaryItem(StringBuilder html, String label, String value) {
        html.append("<div class='summary-item'>");
        html.append("<div class='summary-label'>").append(label).append("</div>");
        html.append("<div class='summary-value'>").append(value).append("</div>");
        html.append("</div>");
    }
    
    private String formatCurrency(double amount) {
        return numberFormat.format(amount) + " VNĐ";
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
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
}

