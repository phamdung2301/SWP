/**
 * Kitchen System - LiteFlow
 * Main JavaScript file for kitchen functionality
 */

// Global variables - read from window object (set by JSP)
let orders = window.orders || [];
let currentFilter = 'all';
let autoRefreshInterval = null;
let soundEnabled = localStorage.getItem('kitchen_sound_enabled') === 'true' || true;
let notificationHistory = [];

// Context path - read from window object (set by JSP)
let contextPath = window.contextPath || '';

// Audio context for notification sounds
let audioContext = null;

/**
 * Notification Manager - Matching Cashier Style
 */
class NotificationManager {
    constructor() {
        this.stack = null;
        this.notifications = new Map();
        this.maxNotifications = 5;
        this.init();
    }
    
    init() {
        // Create notification stack
        this.stack = document.createElement('div');
        this.stack.id = 'notification-stack';
        this.stack.className = 'notification-stack';
        document.body.appendChild(this.stack);
        
        // Listen for page visibility changes
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.pauseAllAnimations();
            } else {
                this.resumeAllAnimations();
            }
        });
    }
    
    show(message, type = 'info', title = null, duration = 3000) {
        // Remove oldest notification if at max capacity
        if (this.notifications.size >= this.maxNotifications) {
            const oldest = this.notifications.values().next().value;
            this.remove(oldest);
        }
        
        const id = Date.now() + Math.random();
        const notification = this.createNotification(id, message, type, title, duration);
        
        this.notifications.set(id, notification);
        this.stack.appendChild(notification);
        
        // Trigger animation
        requestAnimationFrame(() => {
            notification.classList.add('show');
        });
        
        return id;
    }
    
    createNotification(id, message, type, title, duration) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.dataset.id = id;
        
        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };
        
        const icon = icons[type] || icons.info;
        
        notification.innerHTML = `
            <div class="notification-icon">${icon}</div>
            <div class="notification-content">
                ${title ? `<div class="notification-title">${title}</div>` : ''}
                <div class="notification-message">${message}</div>
            </div>
            <div class="notification-close" onclick="window.notificationManager.remove(${id})">×</div>
            ${duration > 0 ? '<div class="notification-progress"></div>' : ''}
        `;
        
        // Auto remove
        if (duration > 0) {
            setTimeout(() => this.remove(id), duration);
        }
        
        return notification;
    }
    
    remove(id) {
        const notification = this.notifications.get(id);
        if (!notification || !document.body.contains(notification)) return;
        
        notification.classList.remove('show');
        notification.style.animation = 'slideOutRight 0.3s ease-in';
        
        setTimeout(() => {
            if (document.body.contains(notification)) {
                notification.remove();
                this.notifications.delete(id);
            }
        }, 300);
    }
    
    clear() {
        this.notifications.forEach((notification, id) => {
            this.remove(id);
        });
    }
    
    pauseAllAnimations() {
        this.notifications.forEach(notification => {
            notification.style.animationPlayState = 'paused';
        });
    }
    
    resumeAllAnimations() {
        this.notifications.forEach(notification => {
            notification.style.animationPlayState = 'running';
        });
    }
}

// Initialize global notification manager
window.notificationManager = new NotificationManager();

/**
 * Initialize the page
 */
document.addEventListener('DOMContentLoaded', function() {
  console.log('🍳 Kitchen page loaded');
  console.log('📊 Orders data loaded:', orders?.length || 0);
  
  // Initialize
  renderOrders();
  updateStats();
  updateTime();
  initSoundState();
  loadNotificationHistory();
  
  // Update time every second
  setInterval(updateTime, 1000);
  
  // Auto refresh every 30 seconds
  autoRefreshInterval = setInterval(refreshOrders, 30000);
  
  // Close dropdowns when clicking outside
  document.addEventListener('click', function(e) {
    if (!e.target.closest('.user-menu-wrapper')) {
      closeUserMenu();
    }
    if (!e.target.closest('.notification-btn') && !e.target.closest('.notification-panel')) {
      closeNotifications();
    }
  });
  
  // Keyboard shortcuts
  document.addEventListener('keydown', function(e) {
    // ESC to close panels
    if (e.key === 'Escape') {
      closeUserMenu();
      closeNotifications();
    }
    // F5 to refresh
    if (e.key === 'F5') {
      e.preventDefault();
      refreshOrders();
    }
  });
});

/**
 * Update current time display
 */
function updateTime() {
  const now = new Date();
  const timeString = now.toLocaleTimeString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit' 
  });
  const timeElement = document.getElementById('currentTime');
  if (timeElement) {
    timeElement.textContent = timeString;
  }
}

/**
 * Render orders
 */
function renderOrders() {
  const ordersGrid = document.getElementById('ordersGrid');
  
  if (!ordersGrid) {
    console.warn('Orders grid element not found');
    return;
  }
  
  // Filter orders
  let filteredOrders = orders;
  if (currentFilter !== 'all') {
    filteredOrders = orders.filter(order => order.status === currentFilter);
  }
  
  if (filteredOrders.length === 0) {
    ordersGrid.innerHTML = `
      <div class="empty-state">
        <i class='bx bx-bowl-hot'></i>
        <p>Không có đơn hàng nào</p>
      </div>
    `;
    return;
  }
  
  ordersGrid.innerHTML = filteredOrders.map(order => {
    const statusClass = order.status.toLowerCase();
    const statusText = getStatusText(order.status);
    const statusIcon = getStatusIcon(order.status);
    const timeAgo = getTimeAgo(order.orderDate);
    
    return `
      <div class="order-card ${statusClass}" data-order-id="${order.orderId}">
        <div class="order-header">
          <div class="order-info">
            <h3>${order.orderNumber}</h3>
            <span class="table-badge">
              <i class='bx bx-table'></i> ${order.tableName}
            </span>
          </div>
          <div class="order-time">
            <i class='bx bx-time'></i>
            <span>${timeAgo}</span>
          </div>
        </div>
        
        <div class="order-status-badge ${statusClass}">
          <i class='bx ${statusIcon}'></i>
          <span>${statusText}</span>
        </div>
        
        <div class="order-items">
          ${order.items.map(item => `
            <div class="order-item">
              <div class="item-quantity">${item.quantity}x</div>
              <div class="item-details">
                <div class="item-name">${item.productName}</div>
                ${item.note ? '<div class="item-note"><i class="bx bx-note"></i> ' + item.note + '</div>' : ''}
              </div>
              <div class="item-status ${item.status.toLowerCase()}">
                ${getStatusText(item.status)}
              </div>
            </div>
          `).join('')}
        </div>
        
        <div class="order-actions">
          ${renderActionButtons(order.status, order.orderId)}
        </div>
      </div>
    `;
  }).join('');
}

/**
 * Render action buttons based on status
 */
function renderActionButtons(status, orderId) {
  switch (status) {
    case 'Pending':
      return `
        <button class="btn btn-primary" onclick="updateStatus('${orderId}', 'Preparing')">
          <i class='bx bx-play'></i> Bắt đầu làm
        </button>
      `;
    case 'Preparing':
      return `
        <button class="btn btn-success" onclick="updateStatus('${orderId}', 'Ready')">
          <i class='bx bx-check'></i> Hoàn thành
        </button>
      `;
    case 'Ready':
      return `
        <button class="btn btn-info" onclick="updateStatus('${orderId}', 'Served')">
          <i class='bx bx-dish'></i> Đã phục vụ
        </button>
      `;
    default:
      return '';
  }
}

/**
 * Get status text in Vietnamese
 */
function getStatusText(status) {
  const statusMap = {
    'Pending': 'Chờ làm',
    'Preparing': 'Đang làm',
    'Ready': 'Sẵn sàng',
    'Served': 'Đã phục vụ',
    'Cancelled': 'Đã hủy'
  };
  return statusMap[status] || status;
}

/**
 * Get status icon
 */
function getStatusIcon(status) {
  const iconMap = {
    'Pending': 'bx-time-five',
    'Preparing': 'bx-food-menu',
    'Ready': 'bx-check-circle',
    'Served': 'bx-dish',
    'Cancelled': 'bx-x-circle'
  };
  return iconMap[status] || 'bx-help-circle';
}

/**
 * Get time ago string
 */
function getTimeAgo(dateString) {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);
  
  if (diffMins < 1) return 'Vừa xong';
  if (diffMins < 60) return diffMins + ' phút trước';
  
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return diffHours + ' giờ trước';
  
  return date.toLocaleDateString('vi-VN');
}

/**
 * Update statistics
 */
function updateStats() {
  const pendingCount = orders.filter(o => o.status === 'Pending').length;
  const preparingCount = orders.filter(o => o.status === 'Preparing').length;
  const readyCount = orders.filter(o => o.status === 'Ready').length;
  const allCount = orders.length;
  
  const elements = {
    pendingCount: document.getElementById('pendingCount'),
    preparingCount: document.getElementById('preparingCount'),
    readyCount: document.getElementById('readyCount'),
    allCount: document.getElementById('allCount'),
    pendingBadge: document.getElementById('pendingBadge'),
    preparingBadge: document.getElementById('preparingBadge'),
    readyBadge: document.getElementById('readyBadge')
  };
  
  if (elements.pendingCount) elements.pendingCount.textContent = pendingCount;
  if (elements.preparingCount) elements.preparingCount.textContent = preparingCount;
  if (elements.readyCount) elements.readyCount.textContent = readyCount;
  if (elements.allCount) elements.allCount.textContent = allCount;
  if (elements.pendingBadge) elements.pendingBadge.textContent = pendingCount;
  if (elements.preparingBadge) elements.preparingBadge.textContent = preparingCount;
  if (elements.readyBadge) elements.readyBadge.textContent = readyCount;
}

/**
 * Filter orders
 */
function filterOrders(filter) {
  currentFilter = filter;
  
  // Update active filter button
  document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.classList.remove('active');
    if (btn.dataset.filter === filter) {
      btn.classList.add('active');
    }
  });
  
  renderOrders();
}

/**
 * Update order status
 */
async function updateStatus(orderId, newStatus) {
  console.log('Updating order', orderId, 'to', newStatus);
  
  // Find the order
  const order = orders.find(o => o.orderId === orderId);
  if (!order) {
    console.error('Order not found:', orderId);
    return;
  }
  
  const oldStatus = order.status;
  
  try {
    const response = await fetch(contextPath + '/api/order/status', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        orderId: orderId,
        status: newStatus
      })
    });
    
    const result = await response.json();
    
    if (result.success) {
      // Update local data
      order.status = newStatus;
      // Update all items status
      order.items.forEach(item => {
        item.status = newStatus;
      });
      
      // Add notification to history
      addKitchenNotification(order, oldStatus, newStatus);
      
      // If status is Served, remove from list after a delay
      if (newStatus === 'Served') {
        setTimeout(() => {
          orders = orders.filter(o => o.orderId !== orderId);
          renderOrders();
          updateStats();
        }, 1000);
      }
      
      renderOrders();
      updateStats();
      
      // Play sound if enabled
      if (soundEnabled) {
        playNotificationSound('success');
      }
      
      // Show success message
      showNotification('Đã cập nhật trạng thái thành công!', 'success');
    } else {
      showNotification('Lỗi: ' + result.message, 'error');
    }
  } catch (error) {
    console.error('Error updating status:', error);
    showNotification('Không thể cập nhật trạng thái', 'error');
  }
}

/**
 * Refresh orders from API
 */
async function refreshOrders() {
  console.log('Refreshing orders...');
  
  try {
    const response = await fetch(contextPath + '/api/kitchen/orders');
    
    if (!response.ok) {
      throw new Error('HTTP error! status: ' + response.status);
    }
    
    const result = await response.json();
    
    if (result.success && result.orders) {
      orders = result.orders;
      renderOrders();
      updateStats();
      showNotification('Đã làm mới danh sách', 'success', null, 2000);
      console.log('✅ Refreshed ' + result.count + ' orders');
    } else {
      console.error('Failed to refresh orders:', result.message);
      showNotification('Không thể làm mới danh sách', 'error');
    }
  } catch (error) {
    console.error('Error refreshing orders:', error);
    showNotification('Lỗi kết nối. Đang tải lại trang...', 'error');
    // Reload page as fallback after 2 seconds
    setTimeout(() => {
      window.location.reload();
    }, 2000);
  }
}

/**
 * Show notification toast (wrapper for NotificationManager)
 */
function showNotification(message, type = 'info', title = null, duration = 3000) {
  window.notificationManager?.show(message, type, title, duration);
}

/**
 * Toggle sound
 */
function toggleSound() {
  soundEnabled = !soundEnabled;
  localStorage.setItem('kitchen_sound_enabled', soundEnabled);
  
  const soundBtn = document.getElementById('soundToggle');
  if (soundBtn) {
    const icon = soundBtn.querySelector('i');
    if (soundEnabled) {
      icon.className = 'bx bx-volume-full';
      soundBtn.classList.remove('muted');
      // Phát âm thanh test khi bật
      playNotificationSound('success');
      showNotification('🔊 Đã bật âm thanh', 'success');
    } else {
      icon.className = 'bx bx-volume-mute';
      soundBtn.classList.add('muted');
      showNotification('🔇 Đã tắt âm thanh', 'success');
    }
  }
}

/**
 * Initialize sound state
 */
function initSoundState() {
  const soundBtn = document.getElementById('soundToggle');
  if (soundBtn) {
    const icon = soundBtn.querySelector('i');
    if (soundEnabled) {
      icon.className = 'bx bx-volume-full';
      soundBtn.classList.remove('muted');
    } else {
      icon.className = 'bx bx-volume-mute';
      soundBtn.classList.add('muted');
    }
  }
}

/**
 * Play notification sound (matching cashier style)
 */
function playNotificationSound(type = 'success') {
  if (!soundEnabled) return;
  
  try {
    // Lazy init AudioContext
    if (!audioContext) {
      audioContext = new (window.AudioContext || window.webkitAudioContext)();
    }
    
    // Resume context nếu bị suspended (do autoplay policy)
    if (audioContext.state === 'suspended') {
      audioContext.resume();
    }
    
    const now = audioContext.currentTime;
    
    // Tạo oscillator (tạo âm thanh)
    const oscillator = audioContext.createOscillator();
    const gainNode = audioContext.createGain();
    
    oscillator.connect(gainNode);
    gainNode.connect(audioContext.destination);
    
    // Cấu hình âm thanh dựa trên type
    if (type === 'success') {
      // Âm thanh thành công: 2 nốt cao vút (C5 -> E5)
      oscillator.frequency.setValueAtTime(523.25, now); // C5
      oscillator.frequency.setValueAtTime(659.25, now + 0.1); // E5
      
      gainNode.gain.setValueAtTime(0.3, now);
      gainNode.gain.exponentialRampToValueAtTime(0.01, now + 0.3);
      
      oscillator.start(now);
      oscillator.stop(now + 0.3);
    } else if (type === 'notify') {
      // Âm thanh thông báo: 3 nốt ngắn (D5 -> D5 -> D5)
      oscillator.frequency.setValueAtTime(587.33, now); // D5
      oscillator.frequency.setValueAtTime(587.33, now + 0.08);
      oscillator.frequency.setValueAtTime(587.33, now + 0.16);
      
      gainNode.gain.setValueAtTime(0.3, now);
      gainNode.gain.setValueAtTime(0, now + 0.05);
      gainNode.gain.setValueAtTime(0.3, now + 0.08);
      gainNode.gain.setValueAtTime(0, now + 0.13);
      gainNode.gain.setValueAtTime(0.3, now + 0.16);
      gainNode.gain.exponentialRampToValueAtTime(0.01, now + 0.25);
      
      oscillator.start(now);
      oscillator.stop(now + 0.25);
    }
    
    console.log('🔊 Played notification sound:', type);
  } catch (error) {
    console.warn('Could not play sound:', error);
  }
}

/**
 * Toggle notifications panel
 */
function toggleNotifications() {
  const panel = document.getElementById('notificationPanel');
  if (panel) {
    const isShowing = panel.classList.contains('show');
    if (isShowing) {
      closeNotifications();
    } else {
      panel.classList.add('show');
      // Close user menu if open
      closeUserMenu();
    }
  }
}

/**
 * Close notifications panel
 */
function closeNotifications() {
  const panel = document.getElementById('notificationPanel');
  if (panel) {
    panel.classList.remove('show');
  }
}

/**
 * Toggle user menu
 */
function toggleUserMenu() {
  const dropdown = document.getElementById('userDropdown');
  if (dropdown) {
    const isShowing = dropdown.classList.contains('show');
    if (isShowing) {
      closeUserMenu();
    } else {
      dropdown.classList.add('show');
      // Close notifications if open
      closeNotifications();
    }
  }
}

/**
 * Close user menu
 */
function closeUserMenu() {
  const dropdown = document.getElementById('userDropdown');
  if (dropdown) {
    dropdown.classList.remove('show');
  }
}

/**
 * Navigate to different pages
 */
function navigate(destination) {
  const routes = {
    'management': contextPath + '/dashboard',
    'kitchen': contextPath + '/kitchen',
    'reception': contextPath + '/reception',
    'cashier': contextPath + '/cart/cashier',
    'end-of-day-report': contextPath + '/report/revenue',
    'dashboard': contextPath + '/dashboard'
  };
  
  const url = routes[destination];
  if (url) {
    window.location.href = url;
  } else {
    console.warn('Unknown destination:', destination);
  }
}

/**
 * Logout functions (matching cashier style)
 */
function logout() {
  openLogoutModal();
}

function openLogoutModal() {
  const modal = document.getElementById('logoutModal');
  if (modal) {
    modal.style.display = 'flex';
    setTimeout(() => modal.classList.add('show'), 10);
  }
}

function closeLogoutModal() {
  const modal = document.getElementById('logoutModal');
  if (modal) {
    modal.classList.remove('show');
    setTimeout(() => {
      modal.style.display = 'none';
    }, 300);
  }
}

function confirmLogout() {
  window.location.href = contextPath + '/logout';
}

/**
 * Load notification history from API
 */
async function loadNotificationHistory() {
  try {
    const response = await fetch(contextPath + '/api/kitchen/notifications');
    
    if (!response.ok) {
      console.warn('Notification API not available yet');
      // Use mock data for now
      displayNotificationHistory([]);
      return;
    }
    
    const data = await response.json();
    notificationHistory = data.notifications || [];
    displayNotificationHistory(notificationHistory);
    
    // Update badge count
    updateNotificationBadge(notificationHistory.length);
  } catch (error) {
    console.warn('Error loading notification history:', error);
    // Display empty state
    displayNotificationHistory([]);
  }
}

/**
 * Display notification history
 */
function displayNotificationHistory(notifications) {
  const notificationList = document.getElementById('notificationList');
  if (!notificationList) return;
  
  if (notifications.length === 0) {
    notificationList.innerHTML = `
      <div class="notification-empty">
        <i class='bx bx-bell-off'></i>
        <p>Không có thông báo nào</p>
      </div>
    `;
    return;
  }
  
  // Group notifications by date
  const grouped = groupNotificationsByDate(notifications);
  
  let html = '';
  for (const [dateLabel, items] of Object.entries(grouped)) {
    html += `
      <div class="notification-date-group">
        <div class="notification-date-header">${dateLabel}</div>
        ${items.map(notif => createNotificationHTML(notif)).join('')}
      </div>
    `;
  }
  
  notificationList.innerHTML = html;
}

/**
 * Group notifications by date
 */
function groupNotificationsByDate(notifications) {
  const grouped = {};
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  
  notifications.forEach(notif => {
    const notifDate = new Date(notif.timestamp);
    notifDate.setHours(0, 0, 0, 0);
    
    let label;
    if (notifDate.getTime() === today.getTime()) {
      label = 'Hôm nay';
    } else if (notifDate.getTime() === yesterday.getTime()) {
      label = 'Hôm qua';
    } else {
      label = notifDate.toLocaleDateString('vi-VN');
    }
    
    if (!grouped[label]) {
      grouped[label] = [];
    }
    grouped[label].push(notif);
  });
  
  return grouped;
}

/**
 * Create notification HTML
 */
function createNotificationHTML(notif) {
  return `
    <div class="notification-item" data-type="${notif.type || 'status-change'}">
      <div class="notif-icon">
        <i class='bx bx-receipt'></i>
      </div>
      <div class="notif-content">
        <div class="notif-title">${notif.title}</div>
        <div class="notif-detail">
          <strong>Đơn hàng:</strong> ${notif.orderNumber}
        </div>
        <div class="notif-detail">
          <strong>Bàn:</strong> ${notif.tableName}
        </div>
        <div class="notif-detail">
          <strong>Trạng thái:</strong>
          <span class="status-change">
            ${getStatusText(notif.oldStatus)}
            <span class="status-arrow">→</span>
            ${getStatusText(notif.newStatus)}
          </span>
        </div>
        <div class="notif-time">${formatNotificationTime(notif.timestamp)}</div>
      </div>
    </div>
  `;
}

/**
 * Format notification time
 */
function formatNotificationTime(timestamp) {
  const date = new Date(timestamp);
  // Always return HH:mm format
  return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

/**
 * Add kitchen notification when status changes
 */
function addKitchenNotification(order, oldStatus, newStatus) {
  const notification = {
    type: 'status-change',
    title: 'Cập nhật trạng thái đơn hàng',
    orderNumber: order.orderNumber,
    tableName: order.tableName,
    orderId: order.orderId,
    oldStatus: oldStatus,
    newStatus: newStatus,
    timestamp: new Date().toISOString()
  };
  
  // Add to beginning of array
  notificationHistory.unshift(notification);
  
  // Keep only last 50 notifications
  if (notificationHistory.length > 50) {
    notificationHistory = notificationHistory.slice(0, 50);
  }
  
  // Update display
  displayNotificationHistory(notificationHistory);
  updateNotificationBadge(notificationHistory.length);
  
  // Save to server (if API is available)
  saveNotificationToServer(notification);
}

/**
 * Save notification to server
 */
async function saveNotificationToServer(notification) {
  try {
    await fetch(contextPath + '/api/kitchen/notifications', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(notification)
    });
  } catch (error) {
    console.warn('Error saving notification:', error);
  }
}

/**
 * Update notification badge
 */
function updateNotificationBadge(count) {
  const badge = document.getElementById('notificationCount');
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
 * Cleanup on page unload
 */
window.addEventListener('beforeunload', function() {
  if (autoRefreshInterval) {
    clearInterval(autoRefreshInterval);
  }
});

