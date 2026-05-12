<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Alert Dashboard - LiteFlow</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
    <style>
        /* Design System */
        :root {
            --primary-color: #0066ff;
            --primary-dark: #0052cc;
            --secondary-color: #00d4ff;
            --success-color: #00c851;
            --warning-color: #ffbb33;
            --danger-color: #ff4444;
            --info-color: #33b5e5;
            
            --bg-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            --card-shadow: 0 10px 30px rgba(0,0,0,0.1);
            --card-hover-shadow: 0 15px 40px rgba(0,0,0,0.15);
            
            --priority-critical: #dc3545;
            --priority-high: #ff9800;
            --priority-medium: #2196f3;
            --priority-low: #4caf50;
        }
        
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
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
        }
        
        /* Header */
        .page-header {
            background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%);
            padding: 30px;
            border-radius: 20px;
            margin-bottom: 30px;
            box-shadow: var(--card-shadow);
            border: 2px solid rgba(255,255,255,0.3);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .page-header h1 {
            font-size: 2.5em;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
        }
        
        /* Statistics Cards */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%);
            padding: 25px;
            border-radius: 15px;
            box-shadow: var(--card-shadow);
            border: 2px solid rgba(255,255,255,0.3);
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
            background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
        }
        
        .stat-card h3 {
            color: #666;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 10px;
        }
        
        .stat-card .number {
            font-size: 2.5em;
            font-weight: bold;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        
        /* Filters */
        .filters {
            background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%);
            padding: 20px;
            border-radius: 15px;
            margin-bottom: 20px;
            box-shadow: var(--card-shadow);
            border: 2px solid rgba(255,255,255,0.3);
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
        }
        
        .filter-group {
            display: flex;
            flex-direction: column;
            gap: 5px;
        }
        
        .filter-group label {
            font-size: 0.85em;
            color: #666;
            font-weight: 600;
        }
        
        .filter-group select,
        .filter-group input {
            padding: 10px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 0.95em;
            transition: all 0.3s ease;
            background: white;
        }
        
        .filter-group select:focus,
        .filter-group input:focus {
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(0,102,255,0.1);
        }
        
        /* Alert Cards */
        .alerts-container {
            display: flex;
            flex-direction: column;
            gap: 15px;
        }
        
        .alert-card {
            background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%);
            padding: 25px;
            border-radius: 15px;
            box-shadow: var(--card-shadow);
            border: 2px solid rgba(255,255,255,0.3);
            transition: all 0.3s ease;
            position: relative;
            border-left: 5px solid var(--primary-color);
        }
        
        .alert-card:hover {
            transform: translateX(5px);
            box-shadow: var(--card-hover-shadow);
        }
        
        .alert-card.unread {
            background: linear-gradient(135deg, rgba(0,102,255,0.05) 0%, rgba(0,102,255,0.02) 100%);
            border-left-width: 7px;
        }
        
        .alert-card.priority-critical {
            border-left-color: var(--priority-critical);
        }
        
        .alert-card.priority-high {
            border-left-color: var(--priority-high);
        }
        
        .alert-card.priority-medium {
            border-left-color: var(--priority-medium);
        }
        
        .alert-card.priority-low {
            border-left-color: var(--priority-low);
        }
        
        .alert-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 15px;
        }
        
        .alert-title {
            font-size: 1.2em;
            font-weight: 600;
            color: #333;
            margin-bottom: 5px;
        }
        
        .alert-meta {
            display: flex;
            gap: 10px;
            align-items: center;
            font-size: 0.85em;
            color: #666;
        }
        
        .alert-priority {
            padding: 5px 12px;
            border-radius: 20px;
            font-size: 0.75em;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .alert-priority.critical {
            background: linear-gradient(135deg, #dc3545, #c82333);
            color: white;
        }
        
        .alert-priority.high {
            background: linear-gradient(135deg, #ff9800, #f57c00);
            color: white;
        }
        
        .alert-priority.medium {
            background: linear-gradient(135deg, #2196f3, #1976d2);
            color: white;
        }
        
        .alert-priority.low {
            background: linear-gradient(135deg, #4caf50, #388e3c);
            color: white;
        }
        
        .alert-message {
            color: #555;
            line-height: 1.6;
            margin-bottom: 15px;
        }
        
        .alert-actions {
            display: flex;
            gap: 10px;
        }
        
        /* Buttons */
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            font-size: 0.95em;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 8px;
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
            background: linear-gradient(135deg, #e0e0e0, #bdbdbd);
            color: #333;
        }
        
        .btn-secondary:hover {
            background: linear-gradient(135deg, #bdbdbd, #9e9e9e);
        }
        
        .btn-success {
            background: linear-gradient(135deg, #4caf50, #388e3c);
            color: white;
        }
        
        .btn-danger {
            background: linear-gradient(135deg, #dc3545, #c82333);
            color: white;
        }
        
        .btn-small {
            padding: 6px 12px;
            font-size: 0.85em;
        }
        
        /* Empty State */
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%);
            border-radius: 15px;
            box-shadow: var(--card-shadow);
        }
        
        .empty-state-icon {
            font-size: 5em;
            margin-bottom: 20px;
        }
        
        .empty-state h3 {
            color: #333;
            margin-bottom: 10px;
        }
        
        .empty-state p {
            color: #666;
        }
        
        /* Loading */
        .loading {
            text-align: center;
            padding: 40px;
            font-size: 1.2em;
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
        
        /* Responsive */
        @media (max-width: 768px) {
            .page-header {
                flex-direction: column;
                gap: 20px;
            }
            
            .stats-grid {
                grid-template-columns: 1fr;
            }
            
            .filters {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="page-header">
            <h1>
                🔔 Alert Dashboard
            </h1>
            <div class="header-actions">
                <button class="btn btn-primary" onclick="markAllAsRead()">
                    ✓ Mark All Read
                </button>
                <button class="btn btn-secondary" onclick="refreshAlerts()">
                    🔄 Refresh
                </button>
            </div>
        </div>
        
        <!-- Statistics -->
        <div class="stats-grid">
            <div class="stat-card">
                <h3>Total Alerts</h3>
                <div class="number" id="stat-total">0</div>
            </div>
            <div class="stat-card">
                <h3>Unread</h3>
                <div class="number" id="stat-unread">0</div>
            </div>
            <div class="stat-card">
                <h3>Critical</h3>
                <div class="number" id="stat-critical">0</div>
            </div>
            <div class="stat-card">
                <h3>Today</h3>
                <div class="number" id="stat-today">0</div>
            </div>
        </div>
        
        <!-- Filters -->
        <div class="filters">
            <div class="filter-group">
                <label>Priority</label>
                <select id="filter-priority" onchange="applyFilters()">
                    <option value="">All</option>
                    <option value="CRITICAL">Critical</option>
                    <option value="HIGH">High</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                </select>
            </div>
            <div class="filter-group">
                <label>Type</label>
                <select id="filter-type" onchange="applyFilters()">
                    <option value="">All</option>
                    <option value="DAILY_SUMMARY">Daily Summary</option>
                    <option value="PO_PENDING">PO Pending</option>
                    <option value="PO_OVERDUE">PO Overdue</option>
                    <option value="LOW_INVENTORY">Low Inventory</option>
                    <option value="OUT_OF_STOCK">Out of Stock</option>
                    <option value="REVENUE_ANOMALY">Revenue Anomaly</option>
                </select>
            </div>
            <div class="filter-group">
                <label>Status</label>
                <select id="filter-status" onchange="applyFilters()">
                    <option value="">All</option>
                    <option value="unread">Unread Only</option>
                    <option value="read">Read Only</option>
                </select>
            </div>
        </div>
        
        <!-- Alerts Container -->
        <div class="alerts-container" id="alerts-container">
            <div class="loading">
                <div class="loading-spinner"></div>
                <p>Loading alerts...</p>
            </div>
        </div>
    </div>
    
    <script>
        let allAlerts = [];
        
        // Load alerts on page load
        window.onload = function() {
            loadAlerts();
        };
        
        // Load alerts from API
        function loadAlerts() {
            fetch('${pageContext.request.contextPath}/alert/api/active?limit=100')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        allAlerts = data.alerts;
                        renderAlerts(allAlerts);
                        updateStatistics(allAlerts);
                    } else {
                        showError('Failed to load alerts: ' + data.error);
                    }
                })
                .catch(error => {
                    console.error('Error loading alerts:', error);
                    showError('Network error: ' + error.message);
                });
        }
        
        // Render alerts
        function renderAlerts(alerts) {
            const container = document.getElementById('alerts-container');
            
            if (alerts.length === 0) {
                container.innerHTML = '<div class="empty-state">' +
                    '<div class="empty-state-icon">✅</div>' +
                    '<h3>No Active Alerts</h3>' +
                    '<p>All clear! You have no active alerts at the moment.</p>' +
                    '</div>';
                return;
            }
            
            container.innerHTML = '';
            
            alerts.forEach(alert => {
                const card = createAlertCard(alert);
                container.appendChild(card);
            });
        }
        
        // Create alert card element
        function createAlertCard(alert) {
            const card = document.createElement('div');
            card.className = 'alert-card priority-' + alert.priority.toLowerCase();
            if (!alert.isRead) {
                card.className += ' unread';
            }
            card.setAttribute('data-priority', alert.priority);
            card.setAttribute('data-type', alert.alertType);
            card.setAttribute('data-read', alert.isRead ? 'read' : 'unread');
            
            // Make card clickable to navigate to relevant page
            card.style.cursor = 'pointer';
            card.onclick = function(e) {
                // Don't navigate if clicking on buttons
                if (!e.target.closest('button')) {
                    const targetUrl = getAlertTargetUrl(alert);
                    window.location.href = targetUrl;
                }
            };
            
            const priorityBadge = '<span class="alert-priority ' + alert.priority.toLowerCase() + '">' + 
                alert.priority + '</span>';
            
            const timeAgo = formatTimeAgo(alert.minutesAgo);
            
            card.innerHTML = '<div class="alert-header">' +
                '<div>' +
                '<div class="alert-title">' + escapeHtml(alert.title) + '</div>' +
                '<div class="alert-meta">' +
                priorityBadge +
                '<span>' + alert.triggeredAt + '</span>' +
                '<span>(' + timeAgo + ')</span>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '<div class="alert-message">' + escapeHtml(alert.message).replace(/\n/g, '<br>') + '</div>' +
                '<div class="alert-actions">' +
                (alert.isRead ? '' : '<button class="btn btn-success btn-small" onclick="markAsRead(\'' + alert.historyID + '\')">✓ Mark Read</button>') +
                '<button class="btn btn-danger btn-small" onclick="dismissAlert(\'' + alert.historyID + '\')">✕ Dismiss</button>' +
                '<button class="btn btn-primary btn-small" onclick="navigateToAlert(\'' + alert.alertType + '\', event)">➜ View</button>' +
                '</div>';
            
            return card;
        }
        
        // Get target URL based on alert type
        function getAlertTargetUrl(alert) {
            const alertTypeMapping = {
                'DAILY_SUMMARY': '${pageContext.request.contextPath}/report/revenue',
                'REVENUE_ANOMALY': '${pageContext.request.contextPath}/report/revenue',
                'PO_PENDING': '${pageContext.request.contextPath}/procurement/po',
                'PO_OVERDUE': '${pageContext.request.contextPath}/procurement/po',
                'LOW_INVENTORY': '${pageContext.request.contextPath}/products',
                'OUT_OF_STOCK': '${pageContext.request.contextPath}/inventory/productlist'
            };
            
            return alertTypeMapping[alert.alertType] || '${pageContext.request.contextPath}/alert/';
        }
        
        // Navigate to alert page
        function navigateToAlert(alertType, event) {
            if (event) {
                event.stopPropagation();
            }
            const alert = { alertType: alertType };
            window.location.href = getAlertTargetUrl(alert);
        }
        
        // Update statistics
        function updateStatistics(alerts) {
            document.getElementById('stat-total').textContent = alerts.length;
            
            const unreadCount = alerts.filter(a => !a.isRead).length;
            document.getElementById('stat-unread').textContent = unreadCount;
            
            const criticalCount = alerts.filter(a => a.priority === 'CRITICAL').length;
            document.getElementById('stat-critical').textContent = criticalCount;
            
            const today = new Date().toISOString().split('T')[0];
            const todayCount = alerts.filter(a => {
                if (!a.triggeredAt) return false;
                const parts = a.triggeredAt.split(' ')[0].split('/');
                const alertDate = parts[2] + '-' + parts[1] + '-' + parts[0];
                return alertDate === today;
            }).length;
            document.getElementById('stat-today').textContent = todayCount;
        }
        
        // Apply filters
        function applyFilters() {
            const priority = document.getElementById('filter-priority').value;
            const type = document.getElementById('filter-type').value;
            const status = document.getElementById('filter-status').value;
            
            let filtered = allAlerts;
            
            if (priority) {
                filtered = filtered.filter(a => a.priority === priority);
            }
            
            if (type) {
                filtered = filtered.filter(a => a.alertType === type);
            }
            
            if (status === 'unread') {
                filtered = filtered.filter(a => !a.isRead);
            } else if (status === 'read') {
                filtered = filtered.filter(a => a.isRead);
            }
            
            renderAlerts(filtered);
        }
        
        // Mark alert as read
        function markAsRead(historyId) {
            fetch('${pageContext.request.contextPath}/alert/api/mark-read', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'historyId=' + historyId
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    loadAlerts();
                } else {
                    alert('Failed to mark as read: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Network error');
            });
        }
        
        // Mark all as read
        function markAllAsRead() {
            if (!confirm('Mark all alerts as read?')) return;
            
            fetch('${pageContext.request.contextPath}/alert/api/mark-all-read', {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    loadAlerts();
                    alert('Marked ' + data.count + ' alerts as read');
                } else {
                    alert('Failed: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Network error');
            });
        }
        
        // Dismiss alert
        function dismissAlert(historyId) {
            if (!confirm('Dismiss this alert?')) return;
            
            fetch('${pageContext.request.contextPath}/alert/api/dismiss', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'historyId=' + historyId
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    loadAlerts();
                } else {
                    alert('Failed to dismiss: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Network error');
            });
        }
        
        // Refresh alerts
        function refreshAlerts() {
            loadAlerts();
        }
        
        // Format time ago
        function formatTimeAgo(minutes) {
            if (minutes < 1) return 'just now';
            if (minutes < 60) return minutes + 'm ago';
            
            const hours = Math.floor(minutes / 60);
            if (hours < 24) return hours + 'h ago';
            
            const days = Math.floor(hours / 24);
            return days + 'd ago';
        }
        
        // Escape HTML
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
        
        // Show error
        function showError(message) {
            const container = document.getElementById('alerts-container');
            container.innerHTML = '<div class="empty-state">' +
                '<div class="empty-state-icon">❌</div>' +
                '<h3>Error</h3>' +
                '<p>' + escapeHtml(message) + '</p>' +
                '</div>';
        }
    </script>
</body>
</html>

