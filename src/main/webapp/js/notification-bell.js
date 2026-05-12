/**
 * Notification Bell Component for LiteFlow Alert System
 * Displays unread alert count and dropdown with recent alerts
 */

class NotificationBell {
    constructor(options = {}) {
        this.containerId = options.containerId || 'notification-bell-container';
        this.contextPath = options.contextPath || '';
        this.refreshInterval = options.refreshInterval || 60000; // 1 minute
        this.maxDisplayAlerts = options.maxDisplayAlerts || 5;
        
        this.unreadCount = 0;
        this.alerts = [];
        this.intervalId = null;
        
        this.init();
    }
    
    /**
     * Initialize the notification bell
     */
    init() {
        this.render();
        this.loadUnreadCount();
        this.startAutoRefresh();
        
        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            const bell = document.getElementById(this.containerId);
            if (bell && !bell.contains(e.target)) {
                this.closeDropdown();
            }
        });
    }
    
    /**
     * Render the notification bell HTML
     */
    render() {
        const container = document.getElementById(this.containerId);
        if (!container) {
            console.error('Notification bell container not found:', this.containerId);
            return;
        }
        
        container.innerHTML = `
            <div class="notification-bell" id="notification-bell">
                <button class="bell-button" onclick="notificationBell.toggleDropdown()">
                    <span class="bell-icon">🔔</span>
                    <span class="badge" id="notification-badge" style="display: none;">0</span>
                </button>
                <div class="notification-dropdown" id="notification-dropdown" style="display: none;">
                    <div class="dropdown-header">
                        <h3>Notifications</h3>
                        <button class="mark-all-read-btn" onclick="notificationBell.markAllAsRead()">
                            ✓ Mark all read
                        </button>
                    </div>
                    <div class="dropdown-content" id="dropdown-content">
                        <div class="loading-state">
                            <div class="spinner"></div>
                            <p>Loading...</p>
                        </div>
                    </div>
                    <div class="dropdown-footer">
                        <a href="${this.contextPath}/alert/" class="view-all-link">
                            View All Alerts →
                        </a>
                    </div>
                </div>
            </div>
        `;
        
        this.injectStyles();
    }
    
    /**
     * Inject CSS styles
     */
    injectStyles() {
        if (document.getElementById('notification-bell-styles')) return;
        
        const style = document.createElement('style');
        style.id = 'notification-bell-styles';
        style.textContent = `
            .notification-bell {
                position: relative;
                display: inline-block;
            }
            
            .bell-button {
                position: relative;
                background: linear-gradient(135deg, #0080FF 0%, #0066cc 100%);
                border: none;
                border-radius: 50%;
                width: 45px;
                height: 45px;
                cursor: pointer;
                transition: all 0.3s ease;
                box-shadow: 0 4px 15px rgba(0, 128, 255, 0.3);
            }
            
            .bell-button:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(0, 128, 255, 0.5);
            }
            
            .bell-icon {
                font-size: 1.5em;
            }
            
            .badge {
                position: absolute;
                top: -5px;
                right: -5px;
                background: linear-gradient(135deg, #ff4444, #cc0000);
                color: white;
                border-radius: 12px;
                padding: 2px 7px;
                font-size: 0.75em;
                font-weight: bold;
                min-width: 20px;
                text-align: center;
                box-shadow: 0 2px 8px rgba(255, 68, 68, 0.4);
                animation: pulse 2s infinite;
            }
            
            @keyframes pulse {
                0%, 100% { transform: scale(1); }
                50% { transform: scale(1.1); }
            }
            
            .notification-dropdown {
                position: absolute;
                top: calc(100% + 10px);
                right: 0;
                width: 380px;
                max-height: 500px;
                background: white;
                border-radius: 12px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.15);
                z-index: 10000;
                overflow: hidden;
                display: flex;
                flex-direction: column;
            }
            
            .dropdown-header {
                padding: 15px 20px;
                border-bottom: 1px solid #e0e0e0;
                display: flex;
                justify-content: space-between;
                align-items: center;
                background: linear-gradient(135deg, #0080FF 0%, #0066cc 100%);
                color: white;
            }
            
            .dropdown-header h3 {
                margin: 0;
                font-size: 1.1em;
            }
            
            .mark-all-read-btn {
                background: rgba(255,255,255,0.2);
                border: 1px solid rgba(255,255,255,0.3);
                color: white;
                padding: 5px 12px;
                border-radius: 6px;
                font-size: 0.85em;
                cursor: pointer;
                transition: all 0.2s ease;
            }
            
            .mark-all-read-btn:hover {
                background: rgba(255,255,255,0.3);
            }
            
            .dropdown-content {
                flex: 1;
                overflow-y: auto;
                max-height: 400px;
            }
            
            .notification-item {
                padding: 15px 20px;
                border-bottom: 1px solid #f0f0f0;
                cursor: pointer;
                transition: all 0.2s ease;
                position: relative;
            }
            
            .notification-item:hover {
                background: #f8f9fa;
            }
            
            .notification-item.unread {
                background: linear-gradient(90deg, rgba(0, 128, 255, 0.05) 0%, rgba(255,255,255,1) 100%);
                border-left: 4px solid #0080FF;
            }
            
            .notification-item .priority-badge {
                display: inline-block;
                padding: 2px 8px;
                border-radius: 12px;
                font-size: 0.7em;
                font-weight: bold;
                text-transform: uppercase;
                margin-bottom: 5px;
            }
            
            .priority-badge.critical { background: #dc3545; color: white; }
            .priority-badge.high { background: #ff9800; color: white; }
            .priority-badge.medium { background: #2196f3; color: white; }
            .priority-badge.low { background: #4caf50; color: white; }
            
            .notification-item .title {
                font-weight: 600;
                color: #333;
                margin-bottom: 5px;
                font-size: 0.95em;
            }
            
            .notification-item .message {
                font-size: 0.85em;
                color: #666;
                margin-bottom: 5px;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
            }
            
            .notification-item .time {
                font-size: 0.75em;
                color: #999;
            }
            
            .dropdown-footer {
                padding: 12px 20px;
                border-top: 1px solid #e0e0e0;
                text-align: center;
                background: #f8f9fa;
            }
            
            .view-all-link {
                color: #0080FF;
                text-decoration: none;
                font-weight: 600;
                font-size: 0.9em;
            }
            
            .view-all-link:hover {
                text-decoration: underline;
            }
            
            .loading-state,
            .empty-state {
                padding: 40px 20px;
                text-align: center;
                color: #999;
            }
            
            .spinner {
                display: inline-block;
                width: 30px;
                height: 30px;
                border: 3px solid #f0f0f0;
                border-top-color: #0080FF;
                border-radius: 50%;
                animation: spin 1s linear infinite;
                margin-bottom: 10px;
            }
            
            @keyframes spin {
                to { transform: rotate(360deg); }
            }
            
            /* Mark as read button hover effect */
            .notification-item button:hover {
                background: #0066cc !important;
                transform: scale(1.1);
                box-shadow: 0 2px 8px rgba(0, 128, 255, 0.4);
            }
        `;
        
        document.head.appendChild(style);
    }
    
    /**
     * Load unread count from API
     */
    loadUnreadCount() {
        fetch(`${this.contextPath}/alert/api/unread-count`, {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    this.updateBadge(data.unreadCount);
                }
            })
            .catch(error => {
                console.error('Error loading unread count:', error);
            });
    }
    
    /**
     * Load recent alerts from API
     */
    loadRecentAlerts() {
        const content = document.getElementById('dropdown-content');
        if (!content) return;
        
        content.innerHTML = `
            <div class="loading-state">
                <div class="spinner"></div>
                <p>Loading...</p>
            </div>
        `;
        
        fetch(`${this.contextPath}/alert/api/recent?limit=${this.maxDisplayAlerts}`, {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    this.alerts = data.alerts;
                    this.renderAlerts();
                } else {
                    content.innerHTML = `<div class="empty-state">Error loading alerts</div>`;
                }
            })
            .catch(error => {
                console.error('Error loading alerts:', error);
                content.innerHTML = `<div class="empty-state">Network error</div>`;
            });
    }
    
    /**
     * Render alerts in dropdown
     */
    renderAlerts() {
        const content = document.getElementById('dropdown-content');
        if (!content) return;
        
        if (this.alerts.length === 0) {
            content.innerHTML = `
                <div class="empty-state">
                    <p>✅ No new notifications</p>
                </div>
            `;
            return;
        }
        
        content.innerHTML = '';
        
        this.alerts.forEach(alert => {
            const item = document.createElement('div');
            item.className = 'notification-item' + (alert.isRead ? '' : ' unread');
            item.onclick = () => this.onAlertClick(alert);
            
            const timeAgo = this.formatTimeAgo(alert.minutesAgo);
            
            item.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                    <div style="flex: 1;">
                        <span class="priority-badge ${alert.priority.toLowerCase()}">${alert.priority}</span>
                        <div class="title">${this.escapeHtml(alert.title)}</div>
                        <div class="message">${this.escapeHtml(alert.message)}</div>
                        <div class="time">${timeAgo}</div>
                    </div>
                    ${!alert.isRead && alert.alertType !== 'PO_PENDING' && alert.alertType !== 'PO_OVERDUE' ? `<button onclick="event.stopPropagation(); notificationBell.markAsRead('${alert.historyID}')" title="Đánh dấu đã đọc" style="background: #0080FF; color: white; border: none; padding: 6px; border-radius: 6px; cursor: pointer; font-size: 0.9em; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; transition: all 0.2s ease;">✓</button>` : ''}
                </div>
            `;
            
            content.appendChild(item);
        });
    }
    
    /**
     * Update badge count
     */
    updateBadge(count) {
        this.unreadCount = count;
        const badge = document.getElementById('notification-badge');
        
        if (badge) {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.style.display = 'block';
            } else {
                badge.style.display = 'none';
            }
        }
    }
    
    /**
     * Toggle dropdown visibility
     */
    toggleDropdown() {
        const dropdown = document.getElementById('notification-dropdown');
        if (!dropdown) return;
        
        if (dropdown.style.display === 'none') {
            dropdown.style.display = 'block';
            this.loadRecentAlerts();
        } else {
            dropdown.style.display = 'none';
        }
    }
    
    /**
     * Close dropdown
     */
    closeDropdown() {
        const dropdown = document.getElementById('notification-dropdown');
        if (dropdown) {
            dropdown.style.display = 'none';
        }
    }
    
    /**
     * Handle alert click
     */
    onAlertClick(alert) {
        // Don't auto-mark as read when clicking
        // Only mark as read when clicking the "Đã đọc" button
        
        // Get target URL based on alert type
        const targetUrl = this.getAlertTargetUrl(alert);
        window.location.href = targetUrl;
    }
    
    /**
     * Get target URL based on alert type
     */
    getAlertTargetUrl(alert) {
        // Mapping alert types to their corresponding pages
        const alertTypeMapping = {
            'DAILY_SUMMARY': '/report/revenue',
            'REVENUE_ANOMALY': '/report/revenue',
            'PO_PENDING': '/procurement/po',
            'PO_OVERDUE': '/procurement/po',
            'LOW_INVENTORY': '/products',
            'OUT_OF_STOCK': '/inventory/productlist'
        };
        
        // Get the target path from mapping
        const targetPath = alertTypeMapping[alert.alertType];
        
        // Return full URL with context path
        if (targetPath) {
            return `${this.contextPath}${targetPath}`;
        }
        
        // Default: navigate to alert dashboard
        return `${this.contextPath}/alert/`;
    }
    
    /**
     * Mark single alert as read
     */
    markAsRead(historyId) {
        fetch(`${this.contextPath}/alert/api/mark-read`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `historyId=${historyId}`,
            credentials: 'include'
        })
        .then(response => {
            if (!response.ok) {
                console.error('HTTP error:', response.status, response.statusText);
                return response.text().then(text => {
                    console.error('Response body:', text);
                    throw new Error(`HTTP ${response.status}: ${text}`);
                });
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.loadUnreadCount();
                this.loadRecentAlerts();
            } else {
                console.error('Mark as read failed:', data.error);
            }
        })
        .catch(error => {
            console.error('Error marking as read:', error);
        });
    }
    
    /**
     * Mark all alerts as read
     */
    markAllAsRead() {
        fetch(`${this.contextPath}/alert/api/mark-all-read`, {
            method: 'POST',
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                this.loadUnreadCount();
                this.loadRecentAlerts();
            }
        })
        .catch(error => {
            console.error('Error marking all as read:', error);
        });
    }
    
    /**
     * Start auto-refresh
     */
    startAutoRefresh() {
        this.intervalId = setInterval(() => {
            this.loadUnreadCount();
        }, this.refreshInterval);
    }
    
    /**
     * Stop auto-refresh
     */
    stopAutoRefresh() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
    }
    
    /**
     * Refresh manually
     */
    refresh() {
        this.loadUnreadCount();
        const dropdown = document.getElementById('notification-dropdown');
        if (dropdown && dropdown.style.display !== 'none') {
            this.loadRecentAlerts();
        }
    }
    
    /**
     * Format time ago
     */
    formatTimeAgo(minutes) {
        if (minutes < 1) return 'just now';
        if (minutes < 60) return `${minutes}m ago`;
        
        const hours = Math.floor(minutes / 60);
        if (hours < 24) return `${hours}h ago`;
        
        const days = Math.floor(hours / 24);
        return `${days}d ago`;
    }
    
    /**
     * Escape HTML
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    /**
     * Destroy the notification bell
     */
    destroy() {
        this.stopAutoRefresh();
        const container = document.getElementById(this.containerId);
        if (container) {
            container.innerHTML = '';
        }
    }
}

// Global instance
let notificationBell = null;

/**
 * Initialize notification bell
 * Call this in your page after DOM is loaded
 */
function initNotificationBell(options = {}) {
    notificationBell = new NotificationBell(options);
    return notificationBell;
}

