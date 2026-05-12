<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- Include Header -->
<jsp:include page="/includes/header.jsp">
    <jsp:param name="page" value="report" />
</jsp:include>

<!-- Chart.js -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
        /* Design System */
        :root {
            --primary-500: #0080FF;
            --primary-600: #0066cc;
            --primary-50: #f2f7ff;
            --secondary-500: #00c6ff;
            --secondary-50: #f0f9ff;
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
            
            --primary-gradient: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
            --success-gradient: linear-gradient(135deg, var(--success-500) 0%, #388e3c 100%);
            --warning-gradient: linear-gradient(135deg, var(--warning-500) 0%, #f57c00 100%);
            --danger-gradient: linear-gradient(135deg, var(--danger-500) 0%, #c82333 100%);
            --info-gradient: linear-gradient(135deg, var(--info-500) 0%, #1976d2 100%);
            
            --card-shadow: 0 10px 30px rgba(0,0,0,0.1);
            --card-hover-shadow: 0 15px 40px rgba(0,0,0,0.15);
            
            --text-primary: #1f2937;
            --text-secondary: #6b7280;
            --border-color: #e5e7eb;
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
        }
        
        /* Override body styling for report page */
        .content {
            background: white;
            min-height: calc(100vh - 120px);
            padding: 20px;
            width: 100%;
            box-sizing: border-box;
        }
        
        .container {
            max-width: 1600px;
            margin: 0 auto;
            width: 100%;
            box-sizing: border-box;
            padding: 20px;
        }
        
        /* Header */
        .page-header {
            background: white;
            padding: 30px;
            border-radius: 20px;
            margin-bottom: 30px;
            box-shadow: var(--card-shadow);
            border: 2px solid var(--color-primary);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 20px;
            width: 100%;
            box-sizing: border-box;
        }
        
        .page-header h1 {
            font-size: 2.5em;
            background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            display: flex;
            align-items: center;
            gap: 15px;
        }
        
        .header-actions {
            display: flex;
            gap: 15px;
            align-items: center;
        }
        
        .date-range-picker {
            display: flex;
            gap: 10px;
            align-items: center;
            background: white;
            padding: 10px 15px;
            border-radius: 12px;
            border: 2px solid var(--border-color);
        }
        
        .date-range-picker input {
            padding: 8px 12px;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            font-size: 0.95em;
        }
        
        .date-range-picker input:focus {
            outline: none;
            border-color: var(--color-primary);
            box-shadow: 0 0 0 3px rgba(0, 128, 255, 0.1);
        }
        
        /* Statistics Cards */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: var(--card-shadow);
            border: 2px solid var(--color-primary);
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: var(--card-hover-shadow);
        }
        
        .stat-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 5px;
        }
        
        .stat-card.revenue::before {
            background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
        }
        
        .stat-card.orders::before {
            background: linear-gradient(135deg, var(--success-500) 0%, #388e3c 100%);
        }
        
        .stat-card.avg::before {
            background: linear-gradient(135deg, var(--info-500) 0%, #1976d2 100%);
        }
        
        .stat-card.growth::before {
            background: linear-gradient(135deg, var(--warning-500) 0%, #f57c00 100%);
        }
        
        .stat-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        
        .stat-icon {
            font-size: 2.5em;
            opacity: 0.2;
        }
        
        .stat-label {
            color: var(--text-secondary);
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-weight: 600;
            margin-bottom: 8px;
        }
        
        .stat-value {
            font-size: 2.2em;
            font-weight: bold;
            background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 10px;
        }
        
        .stat-change {
            font-size: 0.85em;
            display: flex;
            align-items: center;
            gap: 5px;
        }
        
        .stat-change.positive {
            color: var(--success-500);
        }
        
        .stat-change.negative {
            color: var(--danger-500);
        }
        
        /* Charts */
        .charts-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .chart-card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: var(--card-shadow);
            border: 2px solid var(--color-primary);
        }
        
        .chart-card.full-width {
            grid-column: 1 / -1;
        }
        
        .chart-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 2px solid var(--border-color);
        }
        
        .chart-title {
            font-size: 1.3em;
            font-weight: 600;
            color: var(--text-primary);
        }
        
        .chart-container {
            position: relative;
            height: 350px;
        }
        
        /* Table */
        .table-card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: var(--card-shadow);
            border: 2px solid var(--color-primary);
            margin-bottom: 30px;
            width: 100%;
            box-sizing: border-box;
        }
        
        .table-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 2px solid var(--color-primary);
        }
        
        .table-title {
            font-size: 1.3em;
            font-weight: 600;
            color: var(--text-primary);
        }
        
        .table-wrapper {
            overflow-x: auto;
            width: 100%;
        }
        
        table {
            width: 100%;
            min-width: 1000px;
            border-collapse: collapse;
            table-layout: fixed;
            background: white;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            border: 1px solid var(--color-primary);
        }
        
        thead {
            background: linear-gradient(135deg, var(--primary-50, #f2f7ff) 0%, var(--secondary-50, #f0f9ff) 100%);
        }
        
        th {
            padding: 18px 20px;
            text-align: left;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.85em;
            letter-spacing: 0.5px;
            color: var(--gray-800, #1f2937);
            border-bottom: 2px solid var(--color-primary);
            white-space: nowrap;
        }
        
        th:nth-child(1) { width: 10%; } /* Xếp hạng */
        th:nth-child(2) { width: 30%; } /* Sản phẩm */
        th:nth-child(3) { width: 15%; } /* Số lượng */
        th:nth-child(4) { width: 15%; } /* Đơn giá */
        th:nth-child(5) { width: 15%; } /* Doanh thu */
        th:nth-child(6) { width: 15%; } /* % Tổng DT */
        
        tbody tr {
            border-bottom: 1px solid var(--gray-200, #e5e7eb);
            transition: all 0.3s ease;
        }
        
        tbody tr:hover {
            background: rgba(0, 128, 255, 0.05);
            transform: scale(1.01);
        }
        
        td {
            padding: 18px 20px;
            color: var(--text-primary);
            word-wrap: break-word;
            overflow-wrap: break-word;
        }
        
        td:nth-child(2) {
            white-space: normal;
            max-width: 0;
        }
        
        .rank-badge {
            display: inline-block;
            width: 30px;
            height: 30px;
            border-radius: 50%;
            text-align: center;
            line-height: 30px;
            font-weight: bold;
            color: white;
        }
        
        .rank-1 { background: linear-gradient(135deg, #ffd700, #ffed4e); color: #333; }
        .rank-2 { background: linear-gradient(135deg, #c0c0c0, #e8e8e8); color: #333; }
        .rank-3 { background: linear-gradient(135deg, #cd7f32, #e0a878); color: white; }
        .rank-other { background: linear-gradient(135deg, #9e9e9e, #bdbdbd); color: white; }
        
        /* Buttons */
        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 10px;
            font-size: 0.95em;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            text-decoration: none;
            color: white;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
            box-shadow: 0 4px 15px rgba(0, 128, 255, 0.4);
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(0, 128, 255, 0.6);
            background: linear-gradient(135deg, var(--primary-600) 0%, var(--secondary-500) 100%);
        }
        
        .btn-success {
            background: linear-gradient(135deg, var(--success-500) 0%, #388e3c 100%);
            box-shadow: 0 4px 15px rgba(76, 175, 80, 0.4);
        }
        
        .btn-success:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(76, 175, 80, 0.6);
        }
        
        /* Loading */
        .loading {
            text-align: center;
            padding: 40px;
            color: white;
        }
        
        .loading-spinner {
            display: inline-block;
            width: 40px;
            height: 40px;
            border: 4px solid rgba(255,255,255,0.3);
            border-top-color: white;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
        
        /* 🆕 TODAY'S DASHBOARD STYLES */
        .today-dashboard {
            background: white;
            border-radius: 20px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: var(--card-shadow);
            border: 3px solid var(--color-primary);
            position: relative;
            overflow: hidden;
            width: 100%;
            box-sizing: border-box;
        }
        
        .today-dashboard::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
            animation: pulse-bg 3s ease-in-out infinite;
        }
        
        @keyframes pulse-bg {
            0%, 100% { transform: scale(1); opacity: 0.5; }
            50% { transform: scale(1.1); opacity: 0.3; }
        }
        
        .today-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
            position: relative;
            z-index: 1;
        }
        
        .today-title {
            display: flex;
            align-items: center;
            gap: 15px;
        }
        
        .today-title h2 {
            color: var(--color-primary);
            font-size: 2em;
            margin: 0;
            font-weight: 700;
        }
        
        .pulse-indicator {
            width: 12px;
            height: 12px;
            background: #4caf50;
            border-radius: 50%;
            animation: pulse 2s infinite;
            box-shadow: 0 0 15px rgba(76, 175, 80, 0.8);
        }
        
        @keyframes pulse {
            0%, 100% { transform: scale(1); opacity: 1; }
            50% { transform: scale(1.3); opacity: 0.7; }
        }
        
        .live-badge {
            background: rgba(76, 175, 80, 0.9);
            color: white;
            padding: 5px 12px;
            border-radius: 20px;
            font-size: 0.75em;
            font-weight: 700;
            letter-spacing: 1px;
            box-shadow: 0 4px 15px rgba(76, 175, 80, 0.4);
        }
        
        .today-date {
            color: var(--text-secondary);
            font-size: 1.1em;
            font-weight: 500;
        }
        
        .today-content {
            display: grid;
            grid-template-columns: 1.5fr 1fr;
            gap: 25px;
            position: relative;
            z-index: 1;
        }
        
        .today-main-card {
            background: white;
            border-radius: 15px;
            padding: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.15);
            border: 2px solid var(--color-primary);
            display: flex;
            flex-direction: column;
            gap: 20px;
        }
        
        .today-revenue-display {
            text-align: center;
        }
        
        .today-label {
            color: var(--text-secondary);
            font-size: 0.9em;
            margin-bottom: 10px;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-weight: 600;
        }
        
        .today-value {
            font-size: 3.5em;
            font-weight: 800;
            background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 15px;
            line-height: 1.2;
        }
        
        .today-comparison {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            font-size: 0.95em;
        }
        
        .comparison-badge {
            padding: 6px 12px;
            border-radius: 20px;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            gap: 5px;
        }
        
        .comparison-badge.positive {
            background: rgba(76, 175, 80, 0.15);
            color: #2e7d32;
        }
        
        .comparison-badge.negative {
            background: rgba(244, 67, 54, 0.15);
            color: #c62828;
        }
        
        .comparison-badge.neutral {
            background: rgba(158, 158, 158, 0.15);
            color: #616161;
        }
        
        .comparison-text {
            color: var(--text-secondary);
        }
        
        .today-sparkline {
            position: relative;
            height: 100px;
            margin-top: 10px;
        }
        
        .sparkline-label {
            text-align: center;
            color: var(--text-secondary);
            font-size: 0.85em;
            margin-top: 8px;
        }
        
        .today-stats-grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: 15px;
        }
        
        .today-stat-item {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            border: 2px solid var(--color-primary);
            display: flex;
            align-items: center;
            gap: 15px;
            transition: all 0.3s ease;
        }
        
        .today-stat-item:hover {
            transform: translateY(-3px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.15);
        }
        
        .stat-icon-mini {
            font-size: 2em;
            width: 50px;
            height: 50px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, rgba(0, 128, 255, 0.1) 0%, rgba(0, 198, 255, 0.1) 100%);
            border-radius: 12px;
        }
        
        .stat-info-mini {
            flex: 1;
        }
        
        .stat-value-mini {
            font-size: 1.5em;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 3px;
        }
        
        .stat-label-mini {
            color: var(--text-secondary);
            font-size: 0.85em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .stat-change-mini {
            color: var(--text-secondary);
            font-size: 0.8em;
            margin-top: 3px;
        }
        
        /* Responsive */
        @media (max-width: 1200px) {
            .charts-grid {
                grid-template-columns: 1fr;
            }
        }
        
        @media (max-width: 768px) {
            .page-header {
                flex-direction: column;
                align-items: flex-start;
            }
            
            .stats-grid {
                grid-template-columns: 1fr;
            }
            
            .header-actions {
                width: 100%;
                flex-direction: column;
            }
            
            .date-range-picker {
                width: 100%;
                flex-direction: column;
            }
            
            /* Today's Dashboard Responsive */
            .today-content {
                grid-template-columns: 1fr;
            }
            
            .today-title h2 {
                font-size: 1.5em;
            }
            
            .today-value {
                font-size: 2.5em;
            }
            
            table {
                min-width: 800px;
            }
            
            th, td {
                padding: 12px 15px;
                font-size: 0.85em;
            }
        }
        
        @media (max-width: 480px) {
            table {
                min-width: 700px;
            }
            
            th, td {
                padding: 10px 12px;
                font-size: 0.8em;
            }
            
            .page-header h1 {
                font-size: 1.8em;
            }
        }
        
        /* Export Report Modal */
        .export-modal-overlay {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            z-index: 10000;
            justify-content: center;
            align-items: center;
        }
        
        .export-modal-overlay.show {
            display: flex;
        }
        
        .export-modal-content {
            background: white;
            border-radius: 12px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            width: 90%;
            max-width: 500px;
            max-height: 90vh;
            overflow-y: auto;
            animation: modalSlideIn 0.3s ease-out;
        }
        
        @keyframes modalSlideIn {
            from {
                opacity: 0;
                transform: translateY(-20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .export-modal-header {
            padding: 20px 24px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .export-modal-header h3 {
            margin: 0;
            font-size: 1.25em;
            font-weight: 600;
            color: var(--text-primary);
        }
        
        .export-modal-close {
            background: none;
            border: none;
            font-size: 24px;
            color: var(--text-secondary);
            cursor: pointer;
            padding: 0;
            width: 32px;
            height: 32px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 6px;
            transition: all 0.2s;
        }
        
        .export-modal-close:hover {
            background: var(--gray-50);
            color: var(--text-primary);
        }
        
        .export-modal-body {
            padding: 24px;
        }
        
        .export-modal-form-group {
            margin-bottom: 20px;
        }
        
        .export-modal-form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: var(--text-primary);
            font-size: 0.95em;
        }
        
        .export-modal-form-group input[type="date"] {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            font-size: 1em;
            transition: all 0.2s;
        }
        
        .export-modal-form-group input[type="date"]:focus {
            outline: none;
            border-color: var(--primary-500);
            box-shadow: 0 0 0 3px rgba(0, 128, 255, 0.1);
        }
        
        .export-modal-error {
            color: var(--danger-500);
            font-size: 0.875em;
            margin-top: 5px;
            display: none;
        }
        
        .export-modal-error.show {
            display: block;
        }
        
        .export-modal-footer {
            padding: 20px 24px;
            border-top: 1px solid var(--border-color);
            display: flex;
            justify-content: flex-end;
            gap: 12px;
        }
        
        .export-modal-btn {
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            font-size: 0.95em;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
        }
        
        .export-modal-btn-cancel {
            background: var(--gray-50);
            color: var(--text-primary);
        }
        
        .export-modal-btn-cancel:hover {
            background: var(--gray-200);
        }
        
        .export-modal-btn-export {
            background: var(--success-500);
            color: white;
        }
        
        .export-modal-btn-export:hover {
            background: #388e3c;
        }
</style>

<div class="container">
        <!-- Header -->
        <div class="page-header">
            <h1>
                📊 Báo cáo Doanh thu
            </h1>
            <div class="header-actions">
                <div class="date-range-picker">
                    <label>Từ:</label>
                    <input type="date" id="startDate" value="${startDate}">
                    <label>Đến:</label>
                    <input type="date" id="endDate" value="${endDate}">
                    <button class="btn btn-primary" onclick="applyDateRange()">
                        🔍 Xem
                    </button>
                </div>
                <button class="btn btn-success" onclick="exportReport()">
                    📥 Xuất báo cáo
                </button>
            </div>
        </div>
        
        <!-- 🆕 TODAY'S DASHBOARD -->
        <div class="today-dashboard" id="todayDashboard" style="display: none;">
            <div class="today-header">
                <div class="today-title">
                    <span class="pulse-indicator"></span>
                    <h2>🌟 Doanh Thu Hôm Nay</h2>
                    <span class="live-badge">LIVE</span>
                </div>
                <div class="today-date" id="todayDate"></div>
            </div>
            
            <div class="today-content">
                <!-- Main Revenue Card -->
                <div class="today-main-card">
                    <div class="today-revenue-display">
                        <div class="today-label">Tổng Doanh Thu</div>
                        <div class="today-value" id="todayRevenueValue">0 ₫</div>
                        <div class="today-comparison">
                            <span class="comparison-badge" id="todayComparisonBadge">
                                <span id="todayGrowthIcon">→</span>
                                <span id="todayGrowthValue">0%</span>
                            </span>
                            <span class="comparison-text">so với hôm qua</span>
                        </div>
                    </div>
                    
                    <!-- Mini Sparkline Chart -->
                    <div class="today-sparkline">
                        <canvas id="todaySparklineChart"></canvas>
                        <div class="sparkline-label">Xu hướng theo giờ</div>
                    </div>
                </div>
                
                <!-- Quick Stats Grid -->
                <div class="today-stats-grid">
                    <div class="today-stat-item">
                        <div class="stat-icon-mini">🛒</div>
                        <div class="stat-info-mini">
                            <div class="stat-value-mini" id="todayOrders">0</div>
                            <div class="stat-label-mini">Đơn hàng</div>
                            <div class="stat-change-mini" id="todayOrdersChange">--</div>
                        </div>
                    </div>
                    
                    <div class="today-stat-item">
                        <div class="stat-icon-mini">📊</div>
                        <div class="stat-info-mini">
                            <div class="stat-value-mini" id="todayAvgOrder">0 ₫</div>
                            <div class="stat-label-mini">TB/Đơn</div>
                        </div>
                    </div>
                    
                    <div class="today-stat-item">
                        <div class="stat-icon-mini">⏰</div>
                        <div class="stat-info-mini">
                            <div class="stat-value-mini" id="todayPeakHour">--:--</div>
                            <div class="stat-label-mini">Giờ cao điểm</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Statistics Cards -->
        <div class="stats-grid">
            <div class="stat-card revenue">
                <div class="stat-header">
                    <div>
                        <div class="stat-label">Doanh thu tháng</div>
                        <div class="stat-value" id="stat-revenue">--</div>
                        <div class="stat-change" id="stat-revenue-change-container">
                            <span id="stat-revenue-change">Đang tải...</span> so với kỳ trước
                        </div>
                    </div>
                    <div class="stat-icon">💰</div>
                </div>
            </div>
            
            <div class="stat-card orders">
                <div class="stat-header">
                    <div>
                        <div class="stat-label">Số Đơn Hàng</div>
                        <div class="stat-value" id="stat-orders">--</div>
                        <div class="stat-change" id="stat-orders-change-container">
                            <span id="stat-orders-change">Đang tải...</span> so với kỳ trước
                        </div>
                    </div>
                    <div class="stat-icon">🛒</div>
                </div>
            </div>
            
            <div class="stat-card avg">
                <div class="stat-header">
                    <div>
                        <div class="stat-label">Giá Trị TB/Đơn</div>
                        <div class="stat-value" id="stat-avg">--</div>
                        <div class="stat-change" id="stat-avg-change-container">
                            <span id="stat-avg-change">Đang tải...</span> so với kỳ trước
                        </div>
                    </div>
                    <div class="stat-icon">📈</div>
                </div>
            </div>
            
            <div class="stat-card growth">
                <div class="stat-header">
                    <div>
                        <div class="stat-label">Tổng Lợi Nhuận</div>
                        <div class="stat-value" id="stat-profit">--</div>
                        <div class="stat-change" id="stat-profit-change-container">
                            <span id="stat-profit-change">Đang tải...</span> so với kỳ trước
                        </div>
                    </div>
                    <div class="stat-icon">💰</div>
                </div>
            </div>
        </div>
        
        <!-- Charts -->
        <div class="charts-grid">
            <!-- Revenue Trend Chart -->
            <div class="chart-card full-width">
                <div class="chart-header">
                    <div class="chart-title">📈 Xu hướng Doanh thu theo Ngày</div>
                </div>
                <div class="chart-container">
                    <canvas id="revenueTrendChart"></canvas>
                </div>
            </div>
            
            <!-- Product Category Chart -->
            <div class="chart-card">
                <div class="chart-header">
                    <div class="chart-title">🍰 Doanh thu theo Danh mục</div>
                </div>
                <div class="chart-container">
                    <canvas id="productCategoryChart"></canvas>
                </div>
            </div>
            
            <!-- Hourly Revenue Chart -->
            <div class="chart-card">
                <div class="chart-header">
                    <div class="chart-title">⏰ Doanh thu theo Giờ</div>
                </div>
                <div class="chart-container">
                    <canvas id="hourlyRevenueChart"></canvas>
                </div>
            </div>
            
            <!-- 🆕 Monthly Revenue Chart -->
            <div class="chart-card">
                <div class="chart-header">
                    <div class="chart-title">📅 Doanh thu theo Tháng</div>
                </div>
                <div class="chart-container">
                    <canvas id="monthlyRevenueChart"></canvas>
                </div>
            </div>
        </div>
        
        <!-- Top Products Table -->
        <div class="table-card">
            <div class="table-header">
                <div class="table-title">🏆 Top 10 Sản phẩm Bán chạy</div>
            </div>
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>Xếp hạng</th>
                            <th>Sản phẩm</th>
                            <th>Số lượng</th>
                            <th>Đơn giá</th>
                            <th>Doanh thu</th>
                            <th>% Tổng DT</th>
                        </tr>
                    </thead>
                    <tbody id="topProductsTable">
                        <tr>
                            <td colspan="6" style="text-align: center; padding: 40px;">
                                <div class="loading-spinner"></div>
                                <p>Đang tải dữ liệu...</p>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
</div>

<script>
    let reportData = null;
        let charts = {};
        
        // Load report data on page load
        window.onload = function() {
            loadTodayDashboard();  // 🆕 Load today's data first
            loadReportData();
        };
        
        // 🆕 Load Today's Dashboard
        function loadTodayDashboard() {
            console.log('🌟 Loading TODAY\'s dashboard data...');
            
            fetch('${pageContext.request.contextPath}/report/revenue?action=today')
                .then(response => response.json())
                .then(data => {
                    console.log('📦 Today\'s data received:', data);
                    if (data.success) {
                        renderTodayDashboard(data);
                    } else {
                        console.error('❌ Today API error:', data.error);
                    }
                })
                .catch(error => {
                    console.error('❌ Today dashboard error:', error);
                });
        }
        
        // 🆕 Render Today's Dashboard
        function renderTodayDashboard(data) {
            console.log('🎨 Rendering today\'s dashboard...');
            
            // Show dashboard
            document.getElementById('todayDashboard').style.display = 'block';
            
            // Set today's date
            const today = new Date();
            const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
            document.getElementById('todayDate').textContent = today.toLocaleDateString('vi-VN', options);
            
            // Update main revenue value
            document.getElementById('todayRevenueValue').textContent = formatCurrency(data.todayRevenue);
            
            // Update growth indicator
            const growth = data.revenueGrowth;
            const badge = document.getElementById('todayComparisonBadge');
            const icon = document.getElementById('todayGrowthIcon');
            const value = document.getElementById('todayGrowthValue');
            
            badge.className = 'comparison-badge ' + (growth > 0 ? 'positive' : growth < 0 ? 'negative' : 'neutral');
            icon.textContent = growth > 0 ? '↑' : growth < 0 ? '↓' : '→';
            value.textContent = Math.abs(growth).toFixed(1) + '%';
            
            // Update quick stats
            document.getElementById('todayOrders').textContent = formatNumber(data.todayOrders);
            document.getElementById('todayAvgOrder').textContent = formatCurrency(data.avgOrderValue);
            document.getElementById('todayPeakHour').textContent = data.peakHour;
            
            // Order growth
            const orderGrowth = data.orderGrowth;
            const orderChangeText = orderGrowth > 0 ? '↑ +' : orderGrowth < 0 ? '↓ ' : '→ ';
            document.getElementById('todayOrdersChange').textContent = orderChangeText + Math.abs(orderGrowth).toFixed(1) + '% vs hôm qua';
            
            // Render sparkline chart
            renderSparklineChart(data.hourlyTrend);
            
            console.log('✅ Today\'s dashboard rendered!');
        }
        
        // 🆕 Render Sparkline Chart
        let sparklineChart = null;
        function renderSparklineChart(hourlyTrend) {
            const ctx = document.getElementById('todaySparklineChart');
            
            if (sparklineChart) {
                sparklineChart.destroy();
            }
            
            sparklineChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: hourlyTrend.hours,
                    datasets: [{
                        data: hourlyTrend.revenues,
                        borderColor: 'rgba(0, 128, 255, 1)',
                        backgroundColor: 'rgba(0, 128, 255, 0.1)',
                        borderWidth: 2,
                        fill: true,
                        tension: 0.4,
                        pointRadius: 0,
                        pointHoverRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            enabled: true,
                            callbacks: {
                                label: function(context) {
                                    return formatCurrency(context.parsed.y);
                                }
                            }
                        }
                    },
                    scales: {
                        x: {
                            display: false
                        },
                        y: {
                            display: false
                        }
                    }
                }
            });
        }
        
        // Load report data from servlet
        function loadReportData() {
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;
            
            console.log('🔄 Loading report data from', startDate, 'to', endDate);
            
            fetch('${pageContext.request.contextPath}/report/revenue?action=api&startDate=' + startDate + '&endDate=' + endDate)
                .then(response => {
                    console.log('📡 Response status:', response.status);
                    return response.json();
                })
                .then(data => {
                    console.log('📦 Data received:', data);
                    if (data.success) {
                        reportData = data;
                        renderDashboard(data);
                    } else {
                        console.error('❌ API returned success=false:', data.error);
                        alert('Lỗi tải dữ liệu: ' + data.error);
                    }
                })
                .catch(error => {
                    console.error('❌ Fetch error:', error);
                    alert('Lỗi kết nối: ' + error.message);
                });
        }
        
        // Render dashboard with data
        function renderDashboard(data) {
            console.log('📊 renderDashboard called with data:', data);
            console.log('🏆 topProducts in data:', data.topProducts);
            
            // Update statistics
            updateStatistics(data);
            
            // Render charts
            renderRevenueTrendChart(data.trendData);
            renderProductCategoryChart(data.productData);
            renderHourlyRevenueChart(data.hourlyData);
            renderMonthlyRevenueChart(data.monthlyData);  // 🆕 Monthly chart
            
            // Render top products table
            renderTopProductsTable(data.topProducts);
        }
        
        // Update statistics cards
        function updateStatistics(data) {
            // Update revenue
            document.getElementById('stat-revenue').textContent = 
                formatCurrency(data.totalRevenue);
            const revenueGrowth = data.growth || 0;
            const revenueChangeEl = document.getElementById('stat-revenue-change');
            const revenueChangeContainer = document.getElementById('stat-revenue-change-container');
            revenueChangeEl.textContent = (revenueGrowth > 0 ? '↑ +' : revenueGrowth < 0 ? '↓ ' : '→ ') + 
                Math.abs(revenueGrowth).toFixed(1) + '%';
            revenueChangeContainer.className = 'stat-change ' + 
                (revenueGrowth > 0 ? 'positive' : revenueGrowth < 0 ? 'negative' : '');
            
            // Update orders
            document.getElementById('stat-orders').textContent = 
                formatNumber(data.totalOrders);
            // For orders growth, we can calculate if previous orders data is available
            // For now, just show neutral if not available
            const ordersGrowth = data.ordersGrowth || 0;
            const ordersChangeEl = document.getElementById('stat-orders-change');
            const ordersChangeContainer = document.getElementById('stat-orders-change-container');
            if (data.ordersGrowth !== undefined) {
                ordersChangeEl.textContent = (ordersGrowth > 0 ? '↑ +' : ordersGrowth < 0 ? '↓ ' : '→ ') + 
                    Math.abs(ordersGrowth).toFixed(1) + '%';
                ordersChangeContainer.className = 'stat-change ' + 
                    (ordersGrowth > 0 ? 'positive' : ordersGrowth < 0 ? 'negative' : '');
            } else {
                ordersChangeEl.textContent = '--';
                ordersChangeContainer.className = 'stat-change';
            }
            
            // Update average order value
            document.getElementById('stat-avg').textContent = 
                formatCurrency(data.avgOrderValue);
            const avgGrowth = data.avgOrderGrowth || 0;
            const avgChangeEl = document.getElementById('stat-avg-change');
            const avgChangeContainer = document.getElementById('stat-avg-change-container');
            if (data.avgOrderGrowth !== undefined) {
                avgChangeEl.textContent = (avgGrowth > 0 ? '↑ +' : avgGrowth < 0 ? '↓ ' : '→ ') + 
                    Math.abs(avgGrowth).toFixed(1) + '%';
                avgChangeContainer.className = 'stat-change ' + 
                    (avgGrowth > 0 ? 'positive' : avgGrowth < 0 ? 'negative' : '');
            } else {
                avgChangeEl.textContent = '--';
                avgChangeContainer.className = 'stat-change';
            }
            
            // Update profit
            const profitValue = data.totalProfit || 0;
            document.getElementById('stat-profit').textContent = formatCurrency(profitValue);
            
            const profitGrowth = data.profitGrowth || 0;
            const profitChangeEl = document.getElementById('stat-profit-change');
            const profitChangeContainer = document.getElementById('stat-profit-change-container');
            if (data.profitGrowth !== undefined && data.profitGrowth !== null) {
                profitChangeEl.textContent = (profitGrowth > 0 ? '↑ +' : profitGrowth < 0 ? '↓ ' : '→ ') + 
                    Math.abs(profitGrowth).toFixed(1) + '%';
                profitChangeContainer.className = 'stat-change ' + 
                    (profitGrowth > 0 ? 'positive' : profitGrowth < 0 ? 'negative' : '');
            } else {
                profitChangeEl.textContent = '--';
                profitChangeContainer.className = 'stat-change';
            }
        }
        
        // Render revenue trend chart
        function renderRevenueTrendChart(trendData) {
            const ctx = document.getElementById('revenueTrendChart');
            
            if (charts.trend) {
                charts.trend.destroy();
            }
            
            charts.trend = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: trendData.dates,
                    datasets: [{
                        label: 'Doanh thu (VNĐ)',
                        data: trendData.revenues,
                        borderColor: 'rgba(0, 128, 255, 1)',
                        backgroundColor: 'rgba(0, 128, 255, 0.1)',
                        borderWidth: 3,
                        fill: true,
                        tension: 0.4,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: true,
                            position: 'top'
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return formatCurrency(context.parsed.y);
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                callback: function(value) {
                                    return formatCurrency(value, true);
                                }
                            }
                        }
                    }
                }
            });
        }
        
        // Render product category chart
        function renderProductCategoryChart(productData) {
            const ctx = document.getElementById('productCategoryChart');
            
            if (charts.category) {
                charts.category.destroy();
            }
            
            charts.category = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: productData.categories,
                    datasets: [{
                        data: productData.revenues,
                        backgroundColor: productData.colors,
                        borderWidth: 2,
                        borderColor: '#fff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'right'
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                    const percentage = ((context.parsed / total) * 100).toFixed(1);
                                    return context.label + ': ' + formatCurrency(context.parsed) + ' (' + percentage + '%)';
                                }
                            }
                        }
                    }
                }
            });
        }
        
        // Render hourly revenue chart
        function renderHourlyRevenueChart(hourlyData) {
            const ctx = document.getElementById('hourlyRevenueChart');
            
            if (charts.hourly) {
                charts.hourly.destroy();
            }
            
            charts.hourly = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: hourlyData.hours,
                    datasets: [{
                        label: 'Doanh thu (VNĐ)',
                        data: hourlyData.revenues,
                        backgroundColor: 'rgba(0, 198, 255, 0.8)',
                        borderColor: 'rgba(0, 198, 255, 1)',
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return formatCurrency(context.parsed.y);
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                callback: function(value) {
                                    return formatCurrency(value, true);
                                }
                            }
                        }
                    }
                }
            });
        }
        
        // 🆕 Render monthly revenue chart
        function renderMonthlyRevenueChart(monthlyData) {
            const ctx = document.getElementById('monthlyRevenueChart');
            
            if (charts.monthly) {
                charts.monthly.destroy();
            }
            
            charts.monthly = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: monthlyData.months,
                    datasets: [{
                        label: 'Doanh thu (VNĐ)',
                        data: monthlyData.revenues,
                        backgroundColor: 'rgba(255, 152, 0, 0.8)',
                        borderColor: 'rgba(255, 152, 0, 1)',
                        borderWidth: 2,
                        borderRadius: 8,
                        borderSkipped: false
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return 'Doanh thu: ' + formatCurrency(context.parsed.y);
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                callback: function(value) {
                                    return formatCurrency(value, true);
                                }
                            },
                            grid: {
                                color: 'rgba(0, 0, 0, 0.05)'
                            }
                        },
                        x: {
                            grid: {
                                display: false
                            }
                        }
                    }
                }
            });
            
            console.log('✅ Monthly revenue chart rendered with ' + monthlyData.months.length + ' months');
        }
        
        // Render top products table
        function renderTopProductsTable(products) {
            console.log('🏆 renderTopProductsTable called with:', products);
            
            const tbody = document.getElementById('topProductsTable');
            if (!tbody) {
                console.error('❌ topProductsTable tbody not found!');
                return;
            }
            
            tbody.innerHTML = '';
            
            // Check if products is valid
            if (!products || !Array.isArray(products)) {
                console.error('❌ products is not an array:', products);
                tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 20px;">Không có dữ liệu sản phẩm</td></tr>';
                return;
            }
            
            if (products.length === 0) {
                console.warn('⚠️ products array is empty');
                tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 20px;">Chưa có sản phẩm bán trong khoảng thời gian này</td></tr>';
                return;
            }
            
            console.log('✅ Rendering ' + products.length + ' products');
            
            products.forEach((product, index) => {
                const row = document.createElement('tr');
                
                let rankClass = 'rank-other';
                if (index === 0) rankClass = 'rank-1';
                else if (index === 1) rankClass = 'rank-2';
                else if (index === 2) rankClass = 'rank-3';
                
                row.innerHTML = '<td><span class="rank-badge ' + rankClass + '">' + (index + 1) + '</span></td>' +
                    '<td><strong>' + product.name + '</strong></td>' +
                    '<td>' + formatNumber(product.quantity) + '</td>' +
                    '<td>' + formatCurrency(product.price) + '</td>' +
                    '<td><strong>' + formatCurrency(product.revenue) + '</strong></td>' +
                    '<td>' + product.share + '</td>';
                
                tbody.appendChild(row);
            });
            
            console.log('✅ Table rendered successfully');
        }
        
        // Apply date range filter
        function applyDateRange() {
            loadReportData();
        }
        
        // Export report to print - Open modal
        function exportReport() {
            openExportModal();
        }
        
        // Format currency
        function formatCurrency(value, short = false) {
            if (short && value >= 1000000) {
                return (value / 1000000).toFixed(1) + 'M ₫';
            }
            return new Intl.NumberFormat('vi-VN').format(value) + ' ₫';
        }
        
        // Format number
        function formatNumber(value) {
            return new Intl.NumberFormat('vi-VN').format(value);
        }
        
        // Export Report Modal Functions
        function openExportModal() {
            const modal = document.getElementById('exportReportModal');
            const startDateInput = document.getElementById('exportStartDate');
            const endDateInput = document.getElementById('exportEndDate');
            
            // Get current date values from main form
            const currentStartDate = document.getElementById('startDate').value;
            const currentEndDate = document.getElementById('endDate').value;
            
            // Set default values
            startDateInput.value = currentStartDate || '';
            endDateInput.value = currentEndDate || '';
            
            // Clear any previous errors
            const errorMsg = document.getElementById('exportModalError');
            if (errorMsg) {
                errorMsg.classList.remove('show');
                errorMsg.textContent = '';
            }
            
            // Show modal
            modal.classList.add('show');
        }
        
        function closeExportModal() {
            const modal = document.getElementById('exportReportModal');
            modal.classList.remove('show');
            
            // Clear error message
            const errorMsg = document.getElementById('exportModalError');
            if (errorMsg) {
                errorMsg.classList.remove('show');
                errorMsg.textContent = '';
            }
        }
        
        function confirmExportReport() {
            const startDate = document.getElementById('exportStartDate').value;
            const endDate = document.getElementById('exportEndDate').value;
            const errorMsg = document.getElementById('exportModalError');
            
            // Validation
            if (!startDate || !endDate) {
                errorMsg.textContent = 'Vui lòng chọn đầy đủ từ ngày và đến ngày';
                errorMsg.classList.add('show');
                return;
            }
            
            if (new Date(endDate) < new Date(startDate)) {
                errorMsg.textContent = 'Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu';
                errorMsg.classList.add('show');
                return;
            }
            
            // Get context path
            const contextPath = '${pageContext.request.contextPath}';
            
            // Open print window
            const url = contextPath + '/report/revenue/print?startDate=' + startDate + '&endDate=' + endDate;
            window.open(url, '_blank');
            
            // Close modal
            closeExportModal();
        }
        
        // Close modal when clicking outside
        document.addEventListener('DOMContentLoaded', function() {
            const modal = document.getElementById('exportReportModal');
            if (modal) {
                modal.addEventListener('click', function(e) {
                    if (e.target === modal) {
                        closeExportModal();
                    }
                });
            }
        });
</script>

<!-- Export Report Modal -->
<div id="exportReportModal" class="export-modal-overlay">
    <div class="export-modal-content" onclick="event.stopPropagation();">
        <div class="export-modal-header">
            <h3>Chọn khoảng thời gian báo cáo</h3>
            <button class="export-modal-close" onclick="closeExportModal()" aria-label="Đóng">
                ×
            </button>
        </div>
        <div class="export-modal-body">
            <div class="export-modal-form-group">
                <label for="exportStartDate">
                    Từ ngày <span style="color: var(--danger-500);">*</span>
                </label>
                <input type="date" id="exportStartDate" required>
            </div>
            <div class="export-modal-form-group">
                <label for="exportEndDate">
                    Đến ngày <span style="color: var(--danger-500);">*</span>
                </label>
                <input type="date" id="exportEndDate" required>
            </div>
            <div class="export-modal-error" id="exportModalError"></div>
        </div>
        <div class="export-modal-footer">
            <button class="export-modal-btn export-modal-btn-cancel" onclick="closeExportModal()">
                Hủy
            </button>
            <button class="export-modal-btn export-modal-btn-export" onclick="confirmExportReport()">
                📥 Xuất báo cáo
            </button>
        </div>
    </div>
</div>

<!-- Include Footer -->
<jsp:include page="/includes/footer.jsp" />

