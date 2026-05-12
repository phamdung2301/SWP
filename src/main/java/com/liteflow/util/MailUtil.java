package com.liteflow.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;

import java.util.Properties;
import java.util.logging.Logger;

public class MailUtil {

    private static final Logger LOG = Logger.getLogger(MailUtil.class.getName());

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USER = "iloveaifu@gmail.com";
    private static final String SMTP_PASS = "mbtiraewyuhnpijt";  // App Password

    public static void sendOtpMail(String to, String otp) throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER, "LiteFlow Security"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("🔐 LiteFlow Email Verification - Your OTP Code");

        // HTML template - Fixed for Java 11 compatibility
        String html = "<div style=\"font-family: Arial, sans-serif; padding: 20px; background: #f5f7fa;\">" +
            "<h2 style=\"color: #333;\">Welcome to <span style=\"color:#0066ff;\">LiteFlow</span> 🎉</h2>" +
            "<p>Thank you for signing up. To complete your registration, please use the following One-Time Password (OTP):</p>" +
            "<div style=\"margin: 20px 0; text-align: center;\">" +
                "<div style=\"display:inline-block; padding: 20px 40px; background:#0066ff; color:#fff; font-size:28px; font-weight:bold; letter-spacing:8px; border-radius:8px;\">" +
                    otp +
                "</div>" +
            "</div>" +
            "<p>This code is valid for <b>5 minutes</b>. Please do not share it with anyone.</p>" +
            "<p>If you did not request this, please ignore this email.</p>" +
            "<hr style=\"margin:30px 0;\"/>" +
            "<p style=\"font-size:12px; color:#555;\">" +
                "LiteFlow Security Team<br/>" +
                "This is an automated email, please do not reply." +
            "</p>" +
        "</div>";

        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);
        LOG.info("✅ OTP mail sent to " + to);
    }

    /**
     * Send reservation confirmation email to customer
     * @param to Customer email address
     * @param customerName Customer name
     * @param reservationCode Reservation code (e.g., 30102025-001)
     * @param arrivalTime Arrival date and time
     * @param numberOfGuests Number of guests
     * @param tableName Table name (can be null)
     * @param depositAmount Deposit amount (deprecated, ignored)
     * @param preOrderedItems List of pre-ordered items (can be null)
     */
    public static void sendReservationConfirmationMail(
            String to, 
            String customerName, 
            String reservationCode,
            String arrivalTime,
            int numberOfGuests,
            String tableName,
            String depositAmount,
            String preOrderedItems) throws MessagingException, UnsupportedEncodingException {
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER, "LiteFlow Restaurant"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("✅ Xác Nhận Đặt Bàn - LiteFlow Restaurant");

        // Professional HTML email template
        String html = 
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<style>" +
                    "body { margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }" +
                    ".container { max-width: 600px; margin: 0 auto; background: #ffffff; }" +
                    ".header { background: linear-gradient(135deg, #0080FF 0%, #00c6ff 50%, #7d2ae8 100%); padding: 30px 20px; text-align: center; }" +
                    ".header h1 { color: #ffffff; margin: 0; font-size: 28px; font-weight: bold; }" +
                    ".header p { color: #ffffff; margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; }" +
                    ".content { padding: 30px 20px; }" +
                    ".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }" +
                    ".success-badge { background: #d4edda; border: 2px solid #28a745; border-radius: 8px; padding: 15px; text-align: center; margin: 20px 0; }" +
                    ".success-badge h2 { color: #28a745; margin: 0 0 10px 0; font-size: 24px; }" +
                    ".success-badge .code { font-size: 32px; font-weight: bold; color: #0080FF; letter-spacing: 2px; }" +
                    ".details-box { background: #f8f9fa; border-left: 4px solid #0080FF; padding: 20px; margin: 20px 0; border-radius: 4px; }" +
                    ".detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e9ecef; }" +
                    ".detail-row:last-child { border-bottom: none; }" +
                    ".detail-label { font-weight: 600; color: #555; }" +
                    ".detail-value { color: #333; text-align: right; }" +
                    ".items-section { margin: 20px 0; }" +
                    ".items-title { font-weight: 600; color: #333; margin-bottom: 10px; }" +
                    ".item { background: #fff; border: 1px solid #e9ecef; padding: 10px; margin: 5px 0; border-radius: 4px; }" +
                    ".important-note { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
                    ".important-note strong { color: #856404; }" +
                    ".footer { background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #e9ecef; }" +
                    ".footer p { color: #6c757d; font-size: 12px; margin: 5px 0; }" +
                    ".contact-info { margin: 15px 0; }" +
                    ".contact-info a { color: #0080FF; text-decoration: none; }" +
                "</style>" +
            "</head>" +
            "<body>" +
                "<div class=\"container\">" +
                    "<!-- Header -->" +
                    "<div class=\"header\">" +
                        "<h1>🍽️ LiteFlow Restaurant</h1>" +
                        "<p>Where Every Meal Tells a Story</p>" +
                    "</div>" +
                    
                    "<!-- Content -->" +
                    "<div class=\"content\">" +
                        "<p class=\"greeting\">Xin chào <strong>" + customerName + "</strong>,</p>" +
                        
                        "<p>Cảm ơn quý khách đã chọn <strong>LiteFlow Restaurant</strong>! Chúng tôi rất vui được phục vụ quý khách.</p>" +
                        
                        "<!-- Success Badge -->" +
                        "<div class=\"success-badge\">" +
                            "<h2>✅ Đặt Bàn Thành Công</h2>" +
                            "<p>Mã đặt bàn của quý khách:</p>" +
                            "<div class=\"code\">" + reservationCode + "</div>" +
                        "</div>" +
                        
                        "<!-- Reservation Details -->" +
                        "<div class=\"details-box\">" +
                            "<h3 style=\"margin-top: 0; color: #0080FF;\">📋 Thông Tin Đặt Bàn</h3>" +
                            "<div class=\"detail-row\">" +
                                "<span class=\"detail-label\">👤 Tên khách hàng:</span>" +
                                "<span class=\"detail-value\">" + customerName + "</span>" +
                            "</div>" +
                            "<div class=\"detail-row\">" +
                                "<span class=\"detail-label\">📅 Thời gian đến:</span>" +
                                "<span class=\"detail-value\">" + arrivalTime + "</span>" +
                            "</div>" +
                            "<div class=\"detail-row\">" +
                                "<span class=\"detail-label\">👥 Số lượng khách:</span>" +
                                "<span class=\"detail-value\">" + numberOfGuests + " người</span>" +
                            "</div>" +
                            (tableName != null && !tableName.isEmpty() ? 
                                "<div class=\"detail-row\">" +
                                    "<span class=\"detail-label\">🪑 Bàn:</span>" +
                                    "<span class=\"detail-value\">" + tableName + "</span>" +
                                "</div>" : "") +
                            "" +
                        "</div>" +
                        
                        "<!-- Pre-ordered Items -->" +
                        (preOrderedItems != null && !preOrderedItems.isEmpty() ?
                            "<div class=\"items-section\">" +
                                "<div class=\"items-title\">🍴 Món Đặt Trước:</div>" +
                                preOrderedItems +
                            "</div>" : "") +
                        
                        "<!-- Important Note -->" +
                        "<div class=\"important-note\">" +
                            "<strong>⚠️ Lưu Ý Quan Trọng:</strong>" +
                            "<ul style=\"margin: 10px 0; padding-left: 20px;\">" +
                                "<li>Vui lòng đến <strong>trước giờ đặt 5-10 phút</strong> để check-in.</li>" +
                                "<li>Nếu quý khách <strong>đến trễ quá 30 phút</strong>, đặt bàn có thể bị hủy tự động.</li>" +
                                "<li>Nếu có thay đổi, vui lòng liên hệ chúng tôi trước <strong>ít nhất 2 giờ</strong>.</li>" +
                                "<li>Mang theo <strong>mã đặt bàn</strong> khi đến nhà hàng.</li>" +
                            "</ul>" +
                        "</div>" +
                        
                        "<p>Nếu quý khách cần hỗ trợ hoặc thay đổi thông tin đặt bàn, vui lòng liên hệ chúng tôi qua:</p>" +
                        "<div class=\"contact-info\">" +
                            "📞 Hotline: <a href=\"tel:1900-1234\">1900-1234</a><br>" +
                            "📧 Email: <a href=\"mailto:reservation@liteflow.com\">reservation@liteflow.com</a>" +
                        "</div>" +
                        
                        "<p style=\"margin-top: 20px;\">Chúng tôi rất mong được đón tiếp quý khách!</p>" +
                        "<p><strong>Trân trọng,</strong><br>Đội ngũ LiteFlow Restaurant 🍽️</p>" +
                    "</div>" +
                    
                    "<!-- Footer -->" +
                    "<div class=\"footer\">" +
                        "<p><strong>LiteFlow Restaurant</strong></p>" +
                        "<p>123 Nguyễn Huệ, Quận 1, TP.HCM</p>" +
                        "<p>Website: <a href=\"https://liteflow.com\">www.liteflow.com</a></p>" +
                        "<hr style=\"margin: 15px 0; border: none; border-top: 1px solid #dee2e6;\">" +
                        "<p style=\"font-size: 11px; color: #999;\">Đây là email tự động, vui lòng không trả lời email này.</p>" +
                        "<p style=\"font-size: 11px; color: #999;\">© 2025 LiteFlow Restaurant. All rights reserved.</p>" +
                    "</div>" +
                "</div>" +
            "</body>" +
            "</html>";

        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);
        LOG.info("✅ Reservation confirmation email sent to " + to + " - Code: " + reservationCode);
    }

    /**
     * Send purchase order email to supplier
     * @param to Supplier email address
     * @param supplierName Supplier name
     * @param poId Purchase Order ID (UUID as string)
     * @param createDate Order creation date (formatted string)
     * @param expectedDelivery Expected delivery date (formatted string)
     * @param totalAmount Total amount (double)
     * @param notes Notes (can be null)
     * @param items List of purchase order items
     */
    public static void sendPurchaseOrderEmail(
            String to,
            String supplierName,
            String poId,
            String createDate,
            String expectedDelivery,
            double totalAmount,
            String notes,
            java.util.List<com.liteflow.model.procurement.PurchaseOrderItem> items) 
            throws MessagingException, UnsupportedEncodingException {
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER, "LiteFlow Procurement"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Đơn đặt hàng từ LiteFlow");

        // Format total amount with VND currency
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        String formattedAmount = df.format(totalAmount) + " VNĐ";

        // Build items table HTML
        StringBuilder itemsTableHtml = new StringBuilder();
        if (items != null && !items.isEmpty()) {
            itemsTableHtml.append("<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0; background: #fff;\">");
            itemsTableHtml.append("<thead>");
            itemsTableHtml.append("<tr style=\"background: #0080FF; color: #fff;\">");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: left; border: 1px solid #ddd;\">STT</th>");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: left; border: 1px solid #ddd;\">Tên sản phẩm</th>");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: right; border: 1px solid #ddd;\">Số lượng</th>");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: right; border: 1px solid #ddd;\">Đơn giá</th>");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: right; border: 1px solid #ddd;\">Thành tiền</th>");
            itemsTableHtml.append("</tr>");
            itemsTableHtml.append("</thead>");
            itemsTableHtml.append("<tbody>");
            
            int stt = 1;
            for (com.liteflow.model.procurement.PurchaseOrderItem item : items) {
                double itemTotal = item.getQuantity() * item.getUnitPrice();
                String formattedUnitPrice = df.format(item.getUnitPrice()) + " VNĐ";
                String formattedItemTotal = df.format(itemTotal) + " VNĐ";
                
                itemsTableHtml.append("<tr style=\"border-bottom: 1px solid #e9ecef;\">");
                itemsTableHtml.append("<td style=\"padding: 10px; border: 1px solid #ddd;\">").append(stt++).append("</td>");
                itemsTableHtml.append("<td style=\"padding: 10px; border: 1px solid #ddd;\">").append(escapeHtml(item.getItemName())).append("</td>");
                itemsTableHtml.append("<td style=\"padding: 10px; text-align: right; border: 1px solid #ddd;\">").append(item.getQuantity()).append("</td>");
                itemsTableHtml.append("<td style=\"padding: 10px; text-align: right; border: 1px solid #ddd;\">").append(formattedUnitPrice).append("</td>");
                itemsTableHtml.append("<td style=\"padding: 10px; text-align: right; border: 1px solid #ddd; font-weight: bold;\">").append(formattedItemTotal).append("</td>");
                itemsTableHtml.append("</tr>");
            }
            
            itemsTableHtml.append("</tbody>");
            itemsTableHtml.append("</table>");
        } else {
            itemsTableHtml.append("<p style=\"color: #999; font-style: italic; padding: 20px;\">Không có sản phẩm nào trong đơn hàng.</p>");
        }

        // Professional HTML email template
        String html = 
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<style>" +
                    "body { margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }" +
                    ".container { max-width: 700px; margin: 0 auto; background: #ffffff; }" +
                    ".header { background: linear-gradient(135deg, #0080FF 0%, #00c6ff 50%, #7d2ae8 100%); padding: 30px 20px; text-align: center; }" +
                    ".header h1 { color: #ffffff; margin: 0; font-size: 28px; font-weight: bold; }" +
                    ".header p { color: #ffffff; margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; }" +
                    ".content { padding: 30px 20px; }" +
                    ".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }" +
                    ".details-box { background: #f8f9fa; border-left: 4px solid #0080FF; padding: 20px; margin: 20px 0; border-radius: 4px; }" +
                    ".detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e9ecef; }" +
                    ".detail-row:last-child { border-bottom: none; }" +
                    ".detail-label { font-weight: 600; color: #555; }" +
                    ".detail-value { color: #333; text-align: right; }" +
                    ".total-amount { background: #e7f3ff; border: 2px solid #0080FF; border-radius: 8px; padding: 15px; text-align: center; margin: 20px 0; }" +
                    ".total-amount .label { font-size: 14px; color: #555; margin-bottom: 5px; }" +
                    ".total-amount .value { font-size: 24px; font-weight: bold; color: #0080FF; }" +
                    ".notes-section { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
                    ".notes-section strong { color: #856404; }" +
                    ".footer { background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #e9ecef; }" +
                    ".footer p { color: #6c757d; font-size: 12px; margin: 5px 0; }" +
                    ".contact-info { margin: 15px 0; }" +
                    ".contact-info a { color: #0080FF; text-decoration: none; }" +
                "</style>" +
            "</head>" +
            "<body>" +
                "<div class=\"container\">" +
                    "<!-- Header -->" +
                    "<div class=\"header\">" +
                        "<h1>📦 LiteFlow</h1>" +
                        "<p>Hệ thống quản lý nhà hàng</p>" +
                    "</div>" +
                    
                    "<!-- Content -->" +
                    "<div class=\"content\">" +
                        "<p class=\"greeting\">Kính gửi <strong>" + escapeHtml(supplierName) + "</strong>,</p>" +
                        
                        "<p>Chúng tôi xin gửi đơn đặt hàng với thông tin như sau:</p>" +
                        
                        "<!-- Order Details -->" +
                        "<div class=\"details-box\">" +
                            "<h3 style=\"margin-top: 0; color: #0080FF;\">📋 Thông Tin Đơn Hàng</h3>" +
                            "<div class=\"detail-row\">" +
                                "<span class=\"detail-label\">Mã đơn hàng:</span>" +
                                "<span class=\"detail-value\"><strong>" + escapeHtml(poId) + "</strong></span>" +
                            "</div>" +
                            "<div class=\"detail-row\">" +
                                "<span class=\"detail-label\">Ngày gửi:</span>" +
                                "<span class=\"detail-value\">" + escapeHtml(createDate) + "</span>" +
                            "</div>" +
                            "<div class=\"detail-row\">" +
                                "<span class=\"detail-label\">Ngày giao dự kiến:</span>" +
                                "<span class=\"detail-value\">" + escapeHtml(expectedDelivery) + "</span>" +
                            "</div>" +
                        "</div>" +
                        
                        "<!-- Items Table -->" +
                        "<div style=\"margin: 20px 0;\">" +
                            "<h3 style=\"color: #0080FF; margin-bottom: 10px;\">📦 Danh Sách Sản Phẩm</h3>" +
                            itemsTableHtml.toString() +
                        "</div>" +
                        
                        "<!-- Total Amount -->" +
                        "<div class=\"total-amount\">" +
                            "<div class=\"label\">Tổng tiền đơn hàng</div>" +
                            "<div class=\"value\">" + formattedAmount + "</div>" +
                        "</div>" +
                        
                        (notes != null && !notes.trim().isEmpty() ?
                            "<!-- Notes -->" +
                            "<div class=\"notes-section\">" +
                                "<strong>📝 Ghi chú:</strong><br>" +
                                "<p style=\"margin: 10px 0 0 0; color: #856404;\">" + escapeHtml(notes) + "</p>" +
                            "</div>" : "") +
                        
                        "<p style=\"margin-top: 20px;\">Vui lòng xác nhận và chuẩn bị hàng hóa theo đơn hàng trên. Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.</p>" +
                        
                        "<p><strong>Trân trọng,</strong><br>Đội ngũ LiteFlow 📦</p>" +
                    "</div>" +
                    
                    "<!-- Footer -->" +
                    "<div class=\"footer\">" +
                        "<p><strong>LiteFlow</strong></p>" +
                        "<p>Hệ thống quản lý nhà hàng</p>" +
                        "<div class=\"contact-info\">" +
                            "📧 Email: <a href=\"mailto:procurement@liteflow.com\">procurement@liteflow.com</a><br>" +
                            "📞 Hotline: 1900-1234" +
                        "</div>" +
                        "<hr style=\"margin: 15px 0; border: none; border-top: 1px solid #dee2e6;\">" +
                        "<p style=\"font-size: 11px; color: #999;\">Đây là email tự động, vui lòng không trả lời email này.</p>" +
                        "<p style=\"font-size: 11px; color: #999;\">© 2025 LiteFlow. All rights reserved.</p>" +
                    "</div>" +
                "</div>" +
            "</body>" +
            "</html>";

        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);
        LOG.info("✅ Purchase order email sent to " + to + " - PO ID: " + poId);
    }

    /**
     * Send missing goods notification email to supplier
     * @param to Supplier email address
     * @param supplierName Supplier name
     * @param poId Purchase Order ID (UUID as string)
     * @param createDate Order creation date (formatted string)
     * @param expectedDelivery Expected delivery date (formatted string)
     * @param missingItems List of missing items (List<Map> with keys: itemName, orderedQty, receivedQty, missingQty)
     */
    public static void sendMissingGoodsEmail(
            String to,
            String supplierName,
            String poId,
            String createDate,
            String expectedDelivery,
            java.util.List<java.util.Map<String, Object>> missingItems) 
            throws MessagingException, UnsupportedEncodingException {
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER, "LiteFlow Procurement"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Thông báo thiếu hàng - Đơn đặt hàng " + poId);

        // Build missing items table HTML
        StringBuilder itemsTableHtml = new StringBuilder();
        if (missingItems != null && !missingItems.isEmpty()) {
            itemsTableHtml.append("<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0; background: #fff;\">");
            itemsTableHtml.append("<thead>");
            itemsTableHtml.append("<tr style=\"background: #ff9800; color: #fff;\">");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: left; border: 1px solid #ddd;\">STT</th>");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: left; border: 1px solid #ddd;\">Tên sản phẩm</th>");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: right; border: 1px solid #ddd;\">Số lượng đặt</th>");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: right; border: 1px solid #ddd;\">Số lượng đã nhận</th>");
            itemsTableHtml.append("<th style=\"padding: 12px; text-align: right; border: 1px solid #ddd;\">Số lượng thiếu</th>");
            itemsTableHtml.append("</tr>");
            itemsTableHtml.append("</thead>");
            itemsTableHtml.append("<tbody>");
            
            int stt = 1;
            for (java.util.Map<String, Object> item : missingItems) {
                String itemName = (String) item.get("itemName");
                Integer orderedQty = ((Number) item.get("orderedQty")).intValue();
                Integer receivedQty = ((Number) item.get("receivedQty")).intValue();
                Integer missingQty = ((Number) item.get("missingQty")).intValue();
                
                itemsTableHtml.append("<tr style=\"border-bottom: 1px solid #e9ecef; background: #fff3cd;\">");
                itemsTableHtml.append("<td style=\"padding: 10px; border: 1px solid #ddd;\">").append(stt++).append("</td>");
                itemsTableHtml.append("<td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">").append(escapeHtml(itemName)).append("</td>");
                itemsTableHtml.append("<td style=\"padding: 10px; text-align: right; border: 1px solid #ddd;\">").append(orderedQty).append("</td>");
                itemsTableHtml.append("<td style=\"padding: 10px; text-align: right; border: 1px solid #ddd;\">").append(receivedQty).append("</td>");
                itemsTableHtml.append("<td style=\"padding: 10px; text-align: right; border: 1px solid #ddd; font-weight: bold; color: #dc3545;\">").append(missingQty).append("</td>");
                itemsTableHtml.append("</tr>");
            }
            
            itemsTableHtml.append("</tbody>");
            itemsTableHtml.append("</table>");
        } else {
            itemsTableHtml.append("<p style=\"color: #999; font-style: italic; padding: 20px;\">Không có thông tin sản phẩm thiếu.</p>");
        }

        // Professional HTML email template
        String html = 
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<style>" +
                    "body { margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }" +
                    ".container { max-width: 700px; margin: 0 auto; background: #ffffff; }" +
                    ".header { background: linear-gradient(135deg, #ff9800 0%, #ff6b35 50%, #f7931e 100%); padding: 30px 20px; text-align: center; }" +
                    ".header h1 { color: #ffffff; margin: 0; font-size: 28px; font-weight: bold; }" +
                    ".header p { color: #ffffff; margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; }" +
                    ".content { padding: 30px 20px; }" +
                    ".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }" +
                    ".warning-badge { background: #fff3cd; border: 2px solid #ff9800; border-radius: 8px; padding: 15px; text-align: center; margin: 20px 0; }" +
                    ".warning-badge h2 { color: #856404; margin: 0 0 10px 0; font-size: 24px; }" +
                    ".details-box { background: #f8f9fa; border-left: 4px solid #ff9800; padding: 20px; margin: 20px 0; border-radius: 4px; }" +
                    ".detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e9ecef; }" +
                    ".detail-row:last-child { border-bottom: none; }" +
                    ".detail-label { font-weight: 600; color: #555; }" +
                    ".detail-value { color: #333; text-align: right; }" +
                    ".request-section { background: #e7f3ff; border-left: 4px solid #0080FF; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
                    ".request-section strong { color: #004085; }" +
                    ".footer { background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #e9ecef; }" +
                    ".footer p { color: #6c757d; font-size: 12px; margin: 5px 0; }" +
                    ".contact-info { margin: 15px 0; }" +
                    ".contact-info a { color: #0080FF; text-decoration: none; }" +
                "</style>" +
            "</head>" +
            "<body>" +
                "<div class=\"container\">" +
                    "<!-- Header -->" +
                    "<div class=\"header\">" +
                        "<h1>⚠️ LiteFlow</h1>" +
                        "<p>Thông báo thiếu hàng</p>" +
                    "</div>" +
                    
                    "<!-- Content -->" +
                    "<div class=\"content\">" +
                        "<p class=\"greeting\">Kính gửi <strong>" + escapeHtml(supplierName) + "</strong>,</p>" +
                        
                        "<p>Chúng tôi đã nhận hàng nhưng chưa đủ số lượng theo đơn đặt hàng sau:</p>" +
                        
                        "<!-- Warning Badge -->" +
                        "<div class=\"warning-badge\">" +
                            "<h2>⚠️ Chưa nhận đủ hàng</h2>" +
                            "<p>Vui lòng kiểm tra và giao bổ sung số lượng hàng còn thiếu</p>" +
                        "</div>" +
                        
                        "<!-- Order Details -->" +
                        "<div class=\"details-box\">" +
                            "<h3 style=\"margin-top: 0; color: #ff9800;\">📋 Thông Tin Đơn Hàng</h3>" +
                            "<div class=\"detail-row\">" +
                                "<span class=\"detail-label\">Mã đơn hàng:</span>" +
                                "<span class=\"detail-value\"><strong>" + escapeHtml(poId) + "</strong></span>" +
                            "</div>" +
                            "<div class=\"detail-row\">" +
                                "<span class=\"detail-label\">Ngày tạo:</span>" +
                                "<span class=\"detail-value\">" + escapeHtml(createDate) + "</span>" +
                            "</div>" +
                            "<div class=\"detail-row\">" +
                                "<span class=\"detail-label\">Ngày giao dự kiến:</span>" +
                                "<span class=\"detail-value\">" + escapeHtml(expectedDelivery) + "</span>" +
                            "</div>" +
                        "</div>" +
                        
                        "<!-- Missing Items Table -->" +
                        "<div style=\"margin: 20px 0;\">" +
                            "<h3 style=\"color: #ff9800; margin-bottom: 10px;\">📦 Danh Sách Sản Phẩm Thiếu</h3>" +
                            itemsTableHtml.toString() +
                        "</div>" +
                        
                        "<!-- Request Section -->" +
                        "<div class=\"request-section\">" +
                            "<strong>📝 Yêu cầu:</strong><br>" +
                            "<p style=\"margin: 10px 0 0 0; color: #004085;\">Vui lòng giao bổ sung số lượng hàng còn thiếu theo thông tin trên. Chúng tôi rất mong nhận được hàng hóa đầy đủ trong thời gian sớm nhất.</p>" +
                        "</div>" +
                        
                        "<p style=\"margin-top: 20px;\">Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.</p>" +
                        
                        "<p><strong>Trân trọng,</strong><br>Đội ngũ LiteFlow 📦</p>" +
                    "</div>" +
                    
                    "<!-- Footer -->" +
                    "<div class=\"footer\">" +
                        "<p><strong>LiteFlow</strong></p>" +
                        "<p>Hệ thống quản lý nhà hàng</p>" +
                        "<div class=\"contact-info\">" +
                            "📧 Email: <a href=\"mailto:procurement@liteflow.com\">procurement@liteflow.com</a><br>" +
                            "📞 Hotline: 1900-1234" +
                        "</div>" +
                        "<hr style=\"margin: 15px 0; border: none; border-top: 1px solid #dee2e6;\">" +
                        "<p style=\"font-size: 11px; color: #999;\">Đây là email tự động, vui lòng không trả lời email này.</p>" +
                        "<p style=\"font-size: 11px; color: #999;\">© 2025 LiteFlow. All rights reserved.</p>" +
                    "</div>" +
                "</div>" +
            "</body>" +
            "</html>";

        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);
        LOG.info("✅ Missing goods email sent to " + to + " - PO ID: " + poId);
    }

    /**
     * Escape HTML special characters to prevent XSS
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
