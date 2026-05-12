package com.liteflow.controller.dashboard;

import com.google.gson.Gson;
import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.TableSession;
import com.liteflow.model.inventory.Order;
import com.liteflow.model.inventory.OrderDetail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Servlet for daily reports
 * GET /api/reports/daily-summary - Get summary data
 * GET /api/reports/daily-export - Export report (PDF/Excel)
 */
@WebServlet("/api/reports/*")
public class DailyReportServlet extends HttpServlet {
    
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // CORS headers
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            sendErrorResponse(response, 400, "Missing path info");
            return;
        }
        
        switch (pathInfo) {
            case "/daily-summary":
                handleDailySummary(request, response);
                break;
            case "/daily-export":
                handleDailyExport(request, response);
                break;
            default:
                sendErrorResponse(response, 404, "Endpoint không tồn tại: " + pathInfo);
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * Get daily summary data
     */
    private void handleDailySummary(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        PrintWriter out = response.getWriter();
        
        try {
            String dateStr = request.getParameter("date");
            String type = request.getParameter("type");
            
            if (dateStr == null || dateStr.isEmpty()) {
                sendErrorResponse(response, 400, "Thiếu tham số ngày");
                return;
            }
            
            LocalDate reportDate = LocalDate.parse(dateStr);
            LocalDateTime startOfDay = reportDate.atStartOfDay();
            LocalDateTime endOfDay = reportDate.atTime(23, 59, 59);
            
            System.out.println("📊 Getting report summary for: " + dateStr + ", type: " + type);
            
            EntityManager em = BaseDAO.emf.createEntityManager();
            
            try {
                // Build query based on type
                String jpql = "SELECT s FROM TableSession s " +
                             "WHERE s.checkOutTime >= :startDate " +
                             "AND s.checkOutTime <= :endDate ";
                
                if ("completed".equals(type)) {
                    jpql += "AND s.status = 'Completed' AND s.paymentStatus = 'Paid' ";
                } else if ("cancelled".equals(type)) {
                    jpql += "AND s.status = 'Cancelled' ";
                }
                
                jpql += "ORDER BY s.checkOutTime DESC";
                
                Query query = em.createQuery(jpql);
                query.setParameter("startDate", startOfDay);
                query.setParameter("endDate", endOfDay);
                
                
                List<TableSession> sessions = query.getResultList();
                
                // Calculate summary
                int totalInvoices = sessions.size();
                BigDecimal totalRevenue = BigDecimal.ZERO;
                int totalItems = 0;
                
                for (TableSession session : sessions) {
                    if (session.getTotalAmount() != null) {
                        totalRevenue = totalRevenue.add(session.getTotalAmount());
                    }
                    
                    // Count items from orders
                    if (session.getOrders() != null) {
                        for (Order order : session.getOrders()) {
                            if (order.getOrderDetails() != null) {
                                totalItems += order.getOrderDetails().size();
                            }
                        }
                    }
                }
                
                Map<String, Object> summary = new HashMap<>();
                summary.put("totalInvoices", totalInvoices);
                summary.put("totalRevenue", totalRevenue.doubleValue());
                summary.put("totalItems", totalItems);
                summary.put("date", dateStr);
                summary.put("type", type);
                
                System.out.println("✅ Summary: " + totalInvoices + " invoices, " + 
                                 totalRevenue + " revenue, " + totalItems + " items");
                
                out.print(gson.toJson(summary));
                
            } finally {
                em.close();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in handleDailySummary: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "Lỗi khi tạo báo cáo: " + e.getMessage());
        }
    }
    
    /**
     * Export daily report (PDF or Excel)
     */
    private void handleDailyExport(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            String dateStr = request.getParameter("date");
            String format = request.getParameter("format");
            String type = request.getParameter("type");
            
            if (dateStr == null || dateStr.isEmpty()) {
                sendErrorResponse(response, 400, "Thiếu tham số ngày");
                return;
            }
            
            if (format == null || format.isEmpty()) {
                format = "pdf";
            }
            
            LocalDate reportDate = LocalDate.parse(dateStr);
            LocalDateTime startOfDay = reportDate.atStartOfDay();
            LocalDateTime endOfDay = reportDate.atTime(23, 59, 59);
            
            System.out.println("📊 Exporting report for: " + dateStr + ", format: " + format + ", type: " + type);
            
            EntityManager em = BaseDAO.emf.createEntityManager();
            
            try {
                // ✅ Fix MultipleBagFetchException: Fetch sessions first without multiple collections
                String jpql = "SELECT DISTINCT s FROM TableSession s " +
                             "WHERE s.checkOutTime >= :startDate " +
                             "AND s.checkOutTime <= :endDate ";
                
                if ("completed".equals(type)) {
                    jpql += "AND s.status = 'Completed' AND s.paymentStatus = 'Paid' ";
                } else if ("cancelled".equals(type)) {
                    jpql += "AND s.status = 'Cancelled' ";
                }
                
                jpql += "ORDER BY s.checkOutTime DESC";
                
                Query query = em.createQuery(jpql);
                query.setParameter("startDate", startOfDay);
                query.setParameter("endDate", endOfDay);
                
                
                List<TableSession> sessions = query.getResultList();
                
                // ✅ Lazy load collections inside transaction
                for (TableSession session : sessions) {
                    // Trigger lazy loading
                    if (session.getOrders() != null) {
                        session.getOrders().size(); // Force load orders
                        for (Order order : session.getOrders()) {
                            if (order.getOrderDetails() != null) {
                                order.getOrderDetails().size(); // Force load order details
                                for (OrderDetail detail : order.getOrderDetails()) {
                                    // Force load product variant and product
                                    if (detail.getProductVariant() != null) {
                                        detail.getProductVariant().getProduct();
                                    }
                                }
                            }
                        }
                    }
                }
                
                if ("pdf".equals(format)) {
                    exportPDF(response, sessions, reportDate, type);
                } else if ("excel".equals(format)) {
                    exportExcel(response, sessions, reportDate, type);
                } else {
                    sendErrorResponse(response, 400, "Định dạng không hỗ trợ: " + format);
                }
                
            } finally {
                em.close();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in handleDailyExport: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "Lỗi khi xuất báo cáo: " + e.getMessage());
        }
    }
    
    /**
     * Export PDF report (HTML print-to-PDF)
     */
    private void exportPDF(HttpServletResponse response, List<TableSession> sessions, 
                          LocalDate reportDate, String type) throws IOException {
        
        // ✅ Set content type as HTML (browser will auto-print to PDF)
        response.setContentType("text/html; charset=UTF-8");
        
        // ✅ No Content-Disposition - let it open in browser for printing
        PrintWriter out = response.getWriter();
        
        // Generate professional HTML report with auto-print
        out.println(generateHTMLReport(sessions, reportDate, type));
        out.flush();
    }
    
    /**
     * Export Excel report
     */
    private void exportExcel(HttpServletResponse response, List<TableSession> sessions,
                            LocalDate reportDate, String type) throws IOException {
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                          "attachment; filename=bao-cao-" + reportDate + ".xlsx");
        
        
        sendErrorResponse(response, 501, "Export Excel chưa được implement");
    }
    
    /**
     * Generate professional HTML report (can be printed as PDF)
     */
    private String generateHTMLReport(List<TableSession> sessions, LocalDate reportDate, String type) {
        StringBuilder html = new StringBuilder();
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        // Calculate totals
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalItems = 0;
        
        for (TableSession session : sessions) {
            if (session.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(session.getTotalAmount());
            }
            if (session.getOrders() != null) {
                for (Order order : session.getOrders()) {
                    if (order.getOrderDetails() != null) {
                        totalItems += order.getOrderDetails().size();
                    }
                }
            }
        }
        
        String typeLabel = "Tất cả";
        if ("completed".equals(type)) typeLabel = "Đã thanh toán";
        else if ("cancelled".equals(type)) typeLabel = "Đã hủy";
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang='vi'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Báo cáo cuối ngày - ").append(reportDate.format(dateFormatter)).append("</title>");
        html.append("<style>");
        html.append("@page { size: A4; margin: 20mm; }");
        html.append("body { font-family: 'Arial', sans-serif; font-size: 12px; line-height: 1.6; color: #333; }");
        html.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 3px solid #17a2b8; padding-bottom: 20px; }");
        html.append(".header h1 { font-size: 28px; color: #17a2b8; margin: 10px 0; }");
        html.append(".header .subtitle { font-size: 16px; color: #666; }");
        html.append(".summary { background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); padding: 20px; border-radius: 8px; margin-bottom: 30px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }");
        html.append(".summary-item { text-align: center; padding: 15px; background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        html.append(".summary-item .label { font-size: 14px; color: #666; margin-bottom: 8px; }");
        html.append(".summary-item .value { font-size: 24px; font-weight: bold; color: #17a2b8; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("thead { background: linear-gradient(135deg, #17a2b8 0%, #138496 100%); color: white; }");
        html.append("th { padding: 12px; text-align: left; font-weight: 600; }");
        html.append("td { padding: 10px; border-bottom: 1px solid #dee2e6; }");
        html.append("tbody tr:hover { background-color: #f8f9fa; }");
        html.append(".total-row { background-color: #e9ecef; font-weight: bold; }");
        html.append(".footer { margin-top: 40px; padding-top: 20px; border-top: 2px solid #dee2e6; text-align: center; color: #666; font-size: 11px; }");
        html.append(".status-paid { color: #28a745; font-weight: 600; }");
        html.append(".status-cancelled { color: #dc3545; font-weight: 600; }");
        html.append("@media print { body { margin: 0; } .no-print { display: none; } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>BÁO CÁO CUỐI NGÀY</h1>");
        html.append("<div class='subtitle'>Ngày: ").append(reportDate.format(dateFormatter)).append(" | Loại: ").append(typeLabel).append("</div>");
        html.append("<div class='subtitle'>Ngày lập: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</div>");
        html.append("</div>");
        
        // Summary
        html.append("<div class='summary'>");
        html.append("<div class='summary-item'>");
        html.append("<div class='label'>Tổng hóa đơn</div>");
        html.append("<div class='value'>").append(sessions.size()).append("</div>");
        html.append("</div>");
        html.append("<div class='summary-item'>");
        html.append("<div class='label'>Tổng doanh thu</div>");
        html.append("<div class='value'>").append(String.format("%,dđ", totalRevenue.longValue())).append("</div>");
        html.append("</div>");
        html.append("<div class='summary-item'>");
        html.append("<div class='label'>Tổng món bán</div>");
        html.append("<div class='value'>").append(totalItems).append("</div>");
        html.append("</div>");
        html.append("</div>");
        
        // Table
        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>STT</th>");
        html.append("<th>Mã HĐ</th>");
        html.append("<th>Phòng/Bàn</th>");
        html.append("<th>Giờ vào</th>");
        html.append("<th>Giờ ra</th>");
        html.append("<th>Số món</th>");
        html.append("<th>Tổng tiền</th>");
        html.append("<th>Trạng thái</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");
        
        int index = 1;
        for (TableSession session : sessions) {
            html.append("<tr>");
            html.append("<td>").append(index++).append("</td>");
            html.append("<td>").append(session.getInvoiceName() != null ? session.getInvoiceName() : "N/A").append("</td>");
            
            String tableName = "Mang về / Giao hàng";
            if (session.getTable() != null) {
                tableName = session.getTable().getTableName();
            }
            html.append("<td>").append(tableName).append("</td>");
            
            html.append("<td>").append(session.getCheckInTime() != null ? session.getCheckInTime().format(timeFormatter) : "").append("</td>");
            html.append("<td>").append(session.getCheckOutTime() != null ? session.getCheckOutTime().format(timeFormatter) : "").append("</td>");
            
            int itemCount = 0;
            if (session.getOrders() != null) {
                for (Order order : session.getOrders()) {
                    if (order.getOrderDetails() != null) {
                        itemCount += order.getOrderDetails().size();
                    }
                }
            }
            html.append("<td>").append(itemCount).append("</td>");
            
            html.append("<td>").append(String.format("%,dđ", session.getTotalAmount() != null ? session.getTotalAmount().longValue() : 0)).append("</td>");
            
            String statusClass = "Completed".equals(session.getStatus()) ? "status-paid" : "status-cancelled";
            String statusLabel = "Completed".equals(session.getStatus()) ? "Đã thanh toán" : "Đã hủy";
            html.append("<td class='").append(statusClass).append("'>").append(statusLabel).append("</td>");
            
            html.append("</tr>");
        }
        
        // Total row
        html.append("<tr class='total-row'>");
        html.append("<td colspan='5'>TỔNG CỘNG</td>");
        html.append("<td>").append(totalItems).append("</td>");
        html.append("<td>").append(String.format("%,dđ", totalRevenue.longValue())).append("</td>");
        html.append("<td></td>");
        html.append("</tr>");
        
        html.append("</tbody>");
        html.append("</table>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("Báo cáo được tạo tự động bởi hệ thống LiteFlow<br>");
        html.append("© 2025 LiteFlow - Hệ thống quản lý nhà hàng");
        html.append("</div>");
        
        html.append("<script>window.print();</script>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Send error response
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) 
            throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        out.print(gson.toJson(error));
    }
}

