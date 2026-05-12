<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kết quả thanh toán - LiteFlow</title>
    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.0.7/css/boxicons.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .payment-result-container {
            max-width: 1200px;
            margin: 0 auto;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: calc(100vh - 40px);
        }
        
        .payment-result-wrapper {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
            width: 100%;
            max-width: 1200px;
        }
        
        @media (max-width: 968px) {
            .payment-result-wrapper {
                grid-template-columns: 1fr;
            }
        }
        
        .payment-result-card {
            background: white;
            border-radius: 20px;
            padding: 40px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            text-align: center;
        }
        
        .payment-result-card.full-width {
            grid-column: 1 / -1;
        }
        
        .payment-result-icon {
            font-size: 100px;
            margin-bottom: 20px;
            animation: scaleIn 0.5s ease-out;
        }
        
        @keyframes scaleIn {
            from {
                transform: scale(0);
                opacity: 0;
            }
            to {
                transform: scale(1);
                opacity: 1;
            }
        }
        
        .payment-result-icon.success {
            color: #4caf50;
        }
        
        .payment-result-icon.failed {
            color: #f44336;
        }
        
        .payment-result-icon.pending {
            color: #ff9800;
        }
        
        .payment-result-title {
            font-size: 32px;
            font-weight: bold;
            margin-bottom: 15px;
            color: #333;
        }
        
        .payment-result-message {
            font-size: 18px;
            color: #666;
            margin-bottom: 30px;
            line-height: 1.6;
        }
        
        .payment-details {
            background: #f8f9fa;
            border-radius: 15px;
            padding: 25px;
            margin-bottom: 30px;
            text-align: left;
        }
        
        .payment-details-title {
            font-size: 20px;
            font-weight: bold;
            color: #333;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #e0e0e0;
        }
        
        .payment-detail-row {
            display: flex;
            justify-content: space-between;
            padding: 12px 0;
            border-bottom: 1px solid #e0e0e0;
            align-items: center;
        }
        
        .payment-detail-row:last-child {
            border-bottom: none;
        }
        
        .payment-detail-label {
            font-weight: 600;
            color: #666;
            font-size: 15px;
        }
        
        .payment-detail-value {
            color: #333;
            font-size: 15px;
            font-weight: 500;
        }
        
        .order-info-section {
            background: white;
            border-radius: 20px;
            padding: 40px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
        }
        
        .order-info-title {
            font-size: 24px;
            font-weight: bold;
            color: #333;
            margin-bottom: 25px;
            padding-bottom: 15px;
            border-bottom: 3px solid #667eea;
        }
        
        .order-info-item {
            margin-bottom: 20px;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 10px;
        }
        
        .order-info-label {
            font-weight: 600;
            color: #666;
            font-size: 14px;
            margin-bottom: 5px;
        }
        
        .order-info-value {
            color: #333;
            font-size: 16px;
            font-weight: 500;
        }
        
        .order-items-list {
            margin-top: 20px;
        }
        
        .order-item-row {
            display: flex;
            justify-content: space-between;
            padding: 12px 0;
            border-bottom: 1px solid #e0e0e0;
            align-items: center;
        }
        
        .order-item-row:last-child {
            border-bottom: none;
        }
        
        .order-item-name {
            flex: 1;
            color: #333;
            font-weight: 500;
        }
        
        .order-item-quantity {
            color: #666;
            margin: 0 15px;
            min-width: 60px;
            text-align: center;
        }
        
        .order-item-price {
            color: #333;
            font-weight: 600;
            min-width: 120px;
            text-align: right;
        }
        
        .order-summary {
            margin-top: 25px;
            padding-top: 20px;
            border-top: 2px solid #e0e0e0;
        }
        
        .order-summary-row {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            font-size: 16px;
        }
        
        .order-summary-row.total {
            font-size: 20px;
            font-weight: bold;
            color: #667eea;
            padding-top: 15px;
            border-top: 2px solid #e0e0e0;
            margin-top: 10px;
        }
        
        .order-summary-label {
            color: #666;
        }
        
        .order-summary-value {
            color: #333;
            font-weight: 600;
        }
        
        .payment-actions {
            display: flex;
            gap: 15px;
            justify-content: center;
            flex-wrap: wrap;
        }
        
        .btn {
            padding: 14px 35px;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        
        .btn i {
            font-size: 20px;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
        }
        
        .btn-secondary {
            background: #e0e0e0;
            color: #333;
        }
        
        .btn-secondary:hover {
            background: #d0d0d0;
            transform: translateY(-2px);
        }
        
        .countdown {
            margin-top: 25px;
            font-size: 16px;
            color: #666;
            padding: 15px;
            background: #f0f7ff;
            border-radius: 10px;
        }
        
        .countdown span {
            font-weight: bold;
            color: #667eea;
            font-size: 18px;
        }
        
        .loading {
            text-align: center;
            padding: 40px;
            color: #666;
        }
        
        .loading i {
            font-size: 48px;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }
        
        .empty-state {
            text-align: center;
            padding: 40px;
            color: #999;
        }
        
        .empty-state i {
            font-size: 64px;
            margin-bottom: 15px;
            opacity: 0.5;
        }
    </style>
</head>
<body>
    <div class="payment-result-container">
        <div class="payment-result-wrapper">
            <div class="payment-result-card">
                <%
                    String status = request.getParameter("status");
                    String transactionId = request.getParameter("transactionId");
                    String responseCode = request.getParameter("responseCode");
                    String successParam = request.getParameter("success");
                    String message = request.getParameter("message");
                    
                    // Xử lý trường hợp status = "Unknown" - coi như failed nếu success = false
                    if ("Unknown".equals(status) && "false".equals(successParam)) {
                        status = "Failed";
                    }
                    
                    boolean isSuccess = "true".equals(successParam) || "Completed".equals(status);
                    boolean isFailed = "false".equals(successParam) || "Failed".equals(status) || "Unknown".equals(status);
                    boolean isPending = "Pending".equals(status) || "Processing".equals(status);
                    
                    // Nếu không có message và failed, hiển thị message mặc định
                    if (isFailed && (message == null || message.trim().isEmpty())) {
                        message = "Giao dịch của bạn không thể được xử lý. Vui lòng thử lại hoặc liên hệ hỗ trợ.";
                    }
                %>
                
                <c:choose>
                    <c:when test="<%= isSuccess %>">
                        <i class='bx bx-check-circle payment-result-icon success'></i>
                        <h1 class="payment-result-title">Thanh toán thành công!</h1>
                        <p class="payment-result-message">
                            Giao dịch của bạn đã được xử lý thành công. Cảm ơn bạn đã sử dụng dịch vụ!
                        </p>
                    </c:when>
                    <c:when test="<%= isFailed %>">
                        <i class='bx bx-x-circle payment-result-icon failed'></i>
                        <h1 class="payment-result-title">Thanh toán thất bại</h1>
                        <p class="payment-result-message">
                            <%= message != null ? message : "Giao dịch của bạn không thể được xử lý. Vui lòng thử lại hoặc liên hệ hỗ trợ." %>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <i class='bx bx-time payment-result-icon pending'></i>
                        <h1 class="payment-result-title">Đang xử lý</h1>
                        <p class="payment-result-message">
                            Giao dịch của bạn đang được xử lý. Vui lòng đợi trong giây lát...
                        </p>
                    </c:otherwise>
                </c:choose>
                
                <c:if test="<%= (transactionId != null && !transactionId.isEmpty()) || (responseCode != null && !responseCode.isEmpty()) || isFailed %>">
                    <div class="payment-details">
                        <div class="payment-details-title">
                            <i class='bx bx-info-circle'></i> Thông tin giao dịch
                        </div>
                        <c:if test="<%= transactionId != null && !transactionId.isEmpty() %>">
                            <div class="payment-detail-row">
                                <span class="payment-detail-label">Mã giao dịch:</span>
                                <span class="payment-detail-value"><%= transactionId %></span>
                            </div>
                        </c:if>
                        <c:if test="<%= responseCode != null && !responseCode.isEmpty() %>">
                            <div class="payment-detail-row">
                                <span class="payment-detail-label">Mã phản hồi:</span>
                                <span class="payment-detail-value"><%= responseCode %></span>
                            </div>
                        </c:if>
                        <div class="payment-detail-row">
                            <span class="payment-detail-label">Trạng thái:</span>
                            <span class="payment-detail-value">
                                <c:choose>
                                    <c:when test="<%= isSuccess %>">Thành công</c:when>
                                    <c:when test="<%= isFailed %>">Thất bại</c:when>
                                    <c:otherwise>Đang xử lý</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                </c:if>
                
                <div class="payment-actions">
                    <a href="${pageContext.request.contextPath}/cashier" class="btn btn-primary">
                        <i class='bx bx-home'></i> Quay lại Cashier
                    </a>
                    <c:if test="<%= isFailed %>">
                        <a href="javascript:history.back()" class="btn btn-secondary">
                            <i class='bx bx-arrow-back'></i> Thử lại
                        </a>
                    </c:if>
                </div>
                
                <c:if test="<%= isSuccess %>">
                    <div class="countdown">
                        Tự động chuyển về Cashier sau <span id="countdown">5</span> giây...
                    </div>
                </c:if>
            </div>
            
            <c:if test="<%= isSuccess && transactionId != null && !transactionId.isEmpty() %>">
                <div class="order-info-section" id="orderInfoSection">
                    <div class="loading">
                        <i class='bx bx-loader-alt'></i>
                        <p>Đang tải thông tin đơn hàng...</p>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
    
    <script>
        <c:if test="<%= isSuccess %>">
        // Auto redirect after 5 seconds
        let countdown = 5;
        const countdownElement = document.getElementById('countdown');
        
        const timer = setInterval(function() {
            countdown--;
            if (countdownElement) {
                countdownElement.textContent = countdown;
            }
            
            if (countdown <= 0) {
                clearInterval(timer);
                window.location.href = '${pageContext.request.contextPath}/cashier';
            }
        }, 1000);
        </c:if>
        
        <c:if test="<%= isSuccess && transactionId != null && !transactionId.isEmpty() %>">
        // Load order information
        (async function() {
            try {
                const transactionId = '<%= transactionId %>';
                const response = await fetch('${pageContext.request.contextPath}/api/payment/result?transactionId=' + transactionId);
                const data = await response.json();
                
                const orderInfoSection = document.getElementById('orderInfoSection');
                
                if (data.success && data.session) {
                    // Format currency
                    const formatCurrency = (amount) => {
                        if (!amount) return '0đ';
                        return new Intl.NumberFormat('vi-VN', {
                            style: 'currency',
                            currency: 'VND'
                        }).format(amount).replace('₫', 'đ');
                    };
                    
                    // Format date
                    const formatDate = (dateStr) => {
                        if (!dateStr) return '';
                        const date = new Date(dateStr);
                        return date.toLocaleString('vi-VN', {
                            year: 'numeric',
                            month: '2-digit',
                            day: '2-digit',
                            hour: '2-digit',
                            minute: '2-digit'
                        });
                    };
                    
                    let html = '<div class="order-info-title"><i class=\'bx bx-receipt\'></i> Thông tin đơn hàng</div>';
                    
                    // Session/Table info
                    if (data.session.table) {
                        html += '<div class="order-info-item">';
                        html += '<div class="order-info-label">Bàn</div>';
                        html += '<div class="order-info-value">' + (data.session.table.roomName ? data.session.table.roomName + ' - ' : '') + data.session.table.tableName + '</div>';
                        html += '</div>';
                    } else {
                        html += '<div class="order-info-item">';
                        html += '<div class="order-info-label">Loại đơn</div>';
                        html += '<div class="order-info-value">Mang về / Giao hàng</div>';
                        html += '</div>';
                    }
                    
                    if (data.session.invoiceName) {
                        html += '<div class="order-info-item">';
                        html += '<div class="order-info-label">Mã hóa đơn</div>';
                        html += '<div class="order-info-value">' + data.session.invoiceName + '</div>';
                        html += '</div>';
                    }
                    
                    if (data.session.checkOutTime) {
                        html += '<div class="order-info-item">';
                        html += '<div class="order-info-label">Thời gian thanh toán</div>';
                        html += '<div class="order-info-value">' + formatDate(data.session.checkOutTime) + '</div>';
                        html += '</div>';
                    }
                    
                    // Payment method
                    if (data.transaction.paymentMethod) {
                        const paymentMethods = {
                            'Cash': 'Tiền mặt',
                            'Card': 'Thẻ',
                            'Transfer': 'Chuyển khoản',
                            'VNPay': 'VNPay',
                            'Wallet': 'Ví điện tử'
                        };
                        html += '<div class="order-info-item">';
                        html += '<div class="order-info-label">Phương thức thanh toán</div>';
                        html += '<div class="order-info-value">' + (paymentMethods[data.transaction.paymentMethod] || data.transaction.paymentMethod) + '</div>';
                        html += '</div>';
                    }
                    
                    // Order items
                    if (data.items && data.items.length > 0) {
                        html += '<div class="order-items-list">';
                        html += '<div style="font-weight: 600; color: #333; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 2px solid #e0e0e0;">Danh sách món</div>';
                        
                        data.items.forEach(item => {
                            html += '<div class="order-item-row">';
                            html += '<div class="order-item-name">' + (item.productName || 'Món ăn') + (item.size ? ' (' + item.size + ')' : '') + '</div>';
                            html += '<div class="order-item-quantity">x' + (item.quantity || 1) + '</div>';
                            html += '<div class="order-item-price">' + formatCurrency(item.totalPrice || item.unitPrice || 0) + '</div>';
                            html += '</div>';
                        });
                        
                        html += '</div>';
                    }
                    
                    // Order summary
                    if (data.totalAmount) {
                        html += '<div class="order-summary">';
                        html += '<div class="order-summary-row total">';
                        html += '<span class="order-summary-label">Tổng cộng:</span>';
                        html += '<span class="order-summary-value">' + formatCurrency(data.totalAmount) + '</span>';
                        html += '</div>';
                        html += '</div>';
                    }
                    
                    orderInfoSection.innerHTML = html;
                } else {
                    orderInfoSection.innerHTML = '<div class="empty-state"><i class=\'bx bx-info-circle\'></i><p>Không tìm thấy thông tin đơn hàng</p></div>';
                }
            } catch (error) {
                console.error('Error loading order info:', error);
                const orderInfoSection = document.getElementById('orderInfoSection');
                orderInfoSection.innerHTML = '<div class="empty-state"><i class=\'bx bx-error-circle\'></i><p>Không thể tải thông tin đơn hàng</p></div>';
            }
        })();
        </c:if>
    </script>
</body>
</html>
