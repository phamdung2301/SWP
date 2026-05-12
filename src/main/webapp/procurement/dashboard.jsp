<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.temporal.ChronoUnit" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard Mua sắm - LiteFlow</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
    <style>
        :root {
            --primary-500: #0080FF;
            --primary-600: #0066cc;
            --secondary-500: #00c6ff;
            --color-primary: #0080FF;
            --color-secondary: #00c6ff;
            --color-accent: #7d2ae8;
            --success-500: #4caf50;
            --warning-500: #ff9800;
            --danger-500: #dc3545;
            --info-500: #2196f3;
            --gray-50: #f9fafb;
            --gray-200: #e5e7eb;
            --gray-800: #1f2937;
        }
        
        * {
            box-sizing: border-box;
        }
        
        html, body {
            overflow-x: hidden;
            width: 100%;
            max-width: 100vw;
        }
        
        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            margin: 0;
            padding: 0;
            background: var(--gray-50, #f9fafb);
        }
        
        .container {
            max-width: 1800px;
            margin: 0 auto;
            padding: 20px 26px;
            width: 100%;
            box-sizing: border-box;
        }
        .header {
            margin-bottom: 30px;
            background: white;
            padding: 30px;
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            border: 2px solid var(--color-primary);
        }
        .header h1 {
            color: var(--color-primary);
            margin: 0 0 10px 0;
            font-size: 2.5em;
            font-weight: 700;
            background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        .header p {
            color: var(--gray-800, #6b7280);
            margin: 0;
            font-size: 1.1em;
        }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: white;
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            border: 2px solid var(--color-primary);
            border-left: 5px solid var(--primary-500);
            transition: all 0.3s ease;
        }
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 40px rgba(0,0,0,0.15);
        }
        .stat-card.success {
            border-left-color: var(--success-500, #4caf50);
            border-color: var(--success-500, #4caf50);
        }
        .stat-card.warning {
            border-left-color: var(--warning-500, #ff9800);
            border-color: var(--warning-500, #ff9800);
        }
        .stat-card.danger {
            border-left-color: var(--danger-500, #dc3545);
            border-color: var(--danger-500, #dc3545);
        }
        .stat-card.info {
            border-left-color: var(--info-500, #2196f3);
            border-color: var(--info-500, #2196f3);
        }
        .stat-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        .stat-title {
            font-size: 14px;
            color: var(--gray-800, #6b7280);
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .stat-icon {
            font-size: 32px;
            opacity: 0.8;
        }
        .stat-value {
            font-size: 2.5em;
            font-weight: 700;
            background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 10px;
        }
        .stat-card.success .stat-value {
            background: linear-gradient(135deg, var(--success-500) 0%, #388e3c 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .stat-card.warning .stat-value {
            background: linear-gradient(135deg, var(--warning-500) 0%, #f57c00 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .stat-card.danger .stat-value {
            background: linear-gradient(135deg, var(--danger-500) 0%, #c82333 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .stat-card.info .stat-value {
            background: linear-gradient(135deg, var(--info-500) 0%, #1976d2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .stat-change {
            font-size: 13px;
            color: var(--success-500, #4caf50);
            font-weight: 500;
        }
        .stat-change.negative {
            color: var(--danger-500, #dc3545);
        }
        .quick-actions {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .action-card {
            background: white;
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            border: 2px solid var(--color-primary);
            text-align: center;
            transition: all 0.3s ease;
            cursor: pointer;
        }
        .action-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 40px rgba(0,0,0,0.15);
            border-color: var(--secondary-500);
        }
        .action-icon {
            font-size: 48px;
            margin-bottom: 15px;
        }
        .action-title {
            font-size: 18px;
            font-weight: 700;
            color: var(--gray-800, #1f2937);
            margin-bottom: 8px;
        }
        .action-desc {
            font-size: 13px;
            color: var(--gray-800, #6b7280);
        }
        .recent-activities {
            background: white;
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            border: 2px solid var(--color-primary);
        }
        .section-title {
            font-size: 1.5em;
            font-weight: 700;
            color: var(--gray-800, #1f2937);
            margin-bottom: 25px;
            padding-bottom: 15px;
            border-bottom: 2px solid var(--color-primary);
        }
        .activity-item {
            display: flex;
            align-items: center;
            padding: 10px 0;
            border-bottom: 1px solid #f8f9fa;
        }
        .activity-item:last-child {
            border-bottom: none;
        }
        .activity-icon {
            width: 50px;
            height: 50px;
            border-radius: 12px;
            background: linear-gradient(135deg, rgba(0, 128, 255, 0.1) 0%, rgba(0, 198, 255, 0.1) 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 15px;
            font-size: 24px;
            flex-shrink: 0;
        }
        .activity-content {
            flex: 1;
        }
        .activity-text {
            font-size: 15px;
            color: var(--gray-800, #1f2937);
            margin-bottom: 5px;
            font-weight: 500;
        }
        .activity-time {
            font-size: 13px;
            color: var(--gray-800, #6b7280);
        }
        .alert {
            padding: 18px 22px;
            border-radius: 12px;
            margin-bottom: 25px;
            border: 2px solid;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        }
        .alert-warning {
            background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
            border-color: var(--warning-500, #ff9800);
            color: #856404;
        }
        .alert-info {
            background: linear-gradient(135deg, #d1ecf1 0%, #bee5eb 100%);
            border-color: var(--info-500, #2196f3);
            color: #0c5460;
        }
        
        /* Responsive */
        @media (max-width: 768px) {
            .container {
                padding: 15px;
            }
            
            .header {
                padding: 20px;
            }
            
            .header h1 {
                font-size: 1.8em;
            }
            
            .stats-grid {
                grid-template-columns: 1fr;
            }
            
            .quick-actions {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <jsp:include page="../includes/header.jsp">
        <jsp:param name="page" value="procurement" />
    </jsp:include>
    
    <div class="container">
        <div class="header">
            <h1>📦 Dashboard Mua sắm</h1>
            <p>Tổng quan về hoạt động mua sắm và quản lý nhà cung cấp</p>
        </div>

        <!-- Alerts -->
        <c:if test="${showWarningAlert}">
            <div class="alert alert-warning">
                <strong>⚠️ Cảnh báo:</strong> 
                <c:choose>
                    <c:when test="${nearDeadlineCount > 0 && overdueCount > 0}">
                        Có ${nearDeadlineCount} đơn hàng sắp đến hạn giao và ${overdueCount} đơn hàng đã trễ hạn.
                    </c:when>
                    <c:when test="${nearDeadlineCount > 0}">
                        Có ${nearDeadlineCount} đơn hàng sắp đến hạn giao.
                    </c:when>
                    <c:when test="${overdueCount > 0}">
                        Có ${overdueCount} đơn hàng đã trễ hạn.
                    </c:when>
                </c:choose>
            </div>
        </c:if>

        <c:if test="${showInfoAlert}">
            <div class="alert alert-info">
                <strong>ℹ️ Thông tin:</strong> Có ${unmatchedInvoices} hóa đơn chờ đối chiếu từ nhà cung cấp.
            </div>
        </c:if>

        <!-- Statistics -->
        <div class="stats-grid">
            <div class="stat-card success">
                <div class="stat-header">
                    <div class="stat-title">Tổng Nhà cung cấp</div>
                    <div class="stat-icon">🏢</div>
                </div>
                <div class="stat-value">${stats.totalSuppliers != null ? stats.totalSuppliers : 0}</div>
                <div class="stat-change">Nhà cung cấp đang hoạt động</div>
            </div>
            
            <div class="stat-card warning">
                <div class="stat-header">
                    <div class="stat-title">Đơn hàng chờ duyệt</div>
                    <div class="stat-icon">⏳</div>
                </div>
                <div class="stat-value">${stats.pendingPOs != null ? stats.pendingPOs : 0}</div>
                <div class="stat-change">Cần xử lý ngay</div>
            </div>
            
            <div class="stat-card info">
                <div class="stat-header">
                    <div class="stat-title">Đơn hàng đang giao</div>
                    <div class="stat-icon">🚚</div>
                </div>
                <div class="stat-value">${stats.inDeliveryPOs != null ? stats.inDeliveryPOs : 0}</div>
                <div class="stat-change">Theo dõi tiến độ</div>
            </div>
            
            <div class="stat-card danger">
                <div class="stat-header">
                    <div class="stat-title">Đơn hàng trễ hạn</div>
                    <div class="stat-icon">⚠️</div>
                </div>
                <div class="stat-value">${stats.overduePOs != null ? stats.overduePOs : 0}</div>
                <div class="stat-change negative">Cần liên hệ NCC</div>
            </div>
        </div>

        <!-- Quick Actions -->
        <div class="quick-actions">
            <div class="action-card" onclick="window.location.href='${pageContext.request.contextPath}/procurement/supplier'">
                <div class="action-icon">🏢</div>
                <div class="action-title">Quản lý Nhà cung cấp</div>
                <div class="action-desc">Thêm, sửa, xem thông tin NCC</div>
            </div>
            
            <div class="action-card" onclick="window.location.href='${pageContext.request.contextPath}/procurement/po'">
                <div class="action-icon">📋</div>
                <div class="action-title">Tạo Đơn đặt hàng</div>
                <div class="action-desc">Lập đơn hàng mới từ NCC</div>
            </div>
            
            <div class="action-card" onclick="window.location.href='${pageContext.request.contextPath}/procurement/gr'">
                <div class="action-icon">📦</div>
                <div class="action-title">Nhận hàng</div>
                <div class="action-desc">Xác nhận nhận hàng từ NCC</div>
            </div>
            
            <div class="action-card" onclick="window.location.href='${pageContext.request.contextPath}/sales/invoice'">
                <div class="action-icon">🧾</div>
                <div class="action-title">Hoá đơn Bán hàng</div>
                <div class="action-desc">Quản lý hóa đơn bán cho khách</div>
            </div>
        </div>

        <!-- Recent Activities -->
        <div class="recent-activities">
            <div class="section-title">Hoạt động gần đây</div>
            
            <c:choose>
                <c:when test="${not empty recentActivities && recentActivities.size() > 0}">
                    <c:forEach var="activity" items="${recentActivities}">
                        <div class="activity-item">
                            <div class="activity-icon">
                                <c:choose>
                                    <c:when test="${activity.type == 'PO_CREATED' || activity.type == 'PO_APPROVED' || activity.type == 'PO_REJECTED' || activity.type == 'PO_UPDATED'}">📋</c:when>
                                    <c:when test="${activity.type == 'GR_RECEIVED'}">📦</c:when>
                                    <c:when test="${activity.type == 'INVOICE_MATCHED'}">🧾</c:when>
                                    <c:when test="${activity.type == 'SUPPLIER_ADDED'}">🏢</c:when>
                                    <c:otherwise>📌</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="activity-content">
                                <div class="activity-text">${activity.description}</div>
                                <div class="activity-time">${activity.formattedTime}</div>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div style="padding: 40px 20px; text-align: center; color: #6c757d;">
                        <p>Chưa có hoạt động nào gần đây.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <script>
        // Auto refresh stats every 5 minutes
        setInterval(function() {
            // TODO: Implement AJAX call to refresh stats
            console.log('Refreshing procurement stats...');
        }, 300000);

        // Add click handlers for quick actions
        document.querySelectorAll('.action-card').forEach(card => {
            card.addEventListener('click', function() {
                // Add visual feedback
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 150);
            });
        });
    </script>
</body>
</html>






