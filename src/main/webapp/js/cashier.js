/**
 * Cashier System - LiteFlow
 * Main JavaScript file for cashier functionality
 */

// Global variables
let selectedTable = null;
let orderItems = [];
let currentFilter = 'all'; // Status filter: all, available, occupied
let currentCategory = 'all';
let currentRoomFilter = 'all'; // Room filter
let currentCapacityFilter = 'all'; // Capacity filter
let currentDiscount = null;
let discountType = 'percent';

// ✅ Setting: Tự động chuyển sang tab Thực đơn khi chọn bàn
let autoSwitchToMenu = localStorage.getItem('cashier_auto_switch_menu') === 'true' || false;

// Context path - will be set from JSP
let contextPath = '';

// Data from server - will be set from JSP
let tables = [];
let rooms = [];
let menuItems = [];
let categories = [];
let reservations = []; // Today's reservations
let reservationSyncInterval = null; // Interval for auto-refresh

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
  console.log('🍽️ Cashier page loaded');
  console.log('📊 Data loaded:', {
    tables: tables?.length || 0,
    rooms: rooms?.length || 0,
    menuItems: menuItems?.length || 0,
    categories: categories?.length || 0
  });
  
  // Warn if no data
  if (!tables || tables.length === 0) console.warn('⚠️ No tables loaded');
  if (!rooms || rooms.length === 0) console.warn('⚠️ No rooms loaded');
  if (!menuItems || menuItems.length === 0) console.warn('⚠️ No menu items loaded');
  if (!categories || categories.length === 0) console.warn('⚠️ No categories loaded');
  
  // ✅ Đảm bảo trạng thái sạch khi load trang
  selectedTable = null;
  orderItems = [];
  invoices = [{id: 1, orders: [], table: null, name: 'Hóa đơn 1'}];
  currentInvoiceId = 1;
  invoiceIdCounter = 1;
  
  // Update UI - Reset table info
  const tableInfoElement = document.getElementById('selectedTableInfo');
  if (tableInfoElement) {
    tableInfoElement.textContent = 'Chưa chọn bàn';
  }
  
  // ✅ Render invoice tabs
  renderInvoiceTabs();
  
  console.log('🔄 Initialized clean state - selectedTable:', selectedTable, '- invoices:', invoices);
  
  populateMenuCategories();
  populateRoomFilters(); // Populate room filter chips
  renderTables();
  renderMenu();
  renderOrderItems(); // Reset order display
  updateBill(); // Reset bill
  updateFilterCounts();
  setupEventListeners();
  setupTabSystem();
  
  // ✅ Khởi tạo trạng thái button auto-switch
  initAutoSwitchButton();
  
  // ✅ Khởi tạo trạng thái âm thanh
  initSoundState();
  
  // ✅ Event delegation cho invoice tabs
  const invoiceTabs = document.getElementById('invoiceTabs');
  if (invoiceTabs) {
    invoiceTabs.addEventListener('click', function(e) {
      const tab = e.target.closest('.invoice-tab');
      if (tab && !e.target.closest('.bx-x')) {
        const invoiceId = parseInt(tab.dataset.invoice);
        if (invoiceId && invoiceId !== currentInvoiceId) {
          switchInvoice(invoiceId);
          console.log('✅ Switched to invoice:', invoiceId);
        }
      }
    });
  }
  
  // Setup discount input listener
  const discountInput = document.getElementById('discountInput');
  if (discountInput) {
    discountInput.addEventListener('input', updateDiscountPreview);
  }
  
  // Discount tab switching
  document.querySelectorAll('.discount-tabs .tab').forEach(tab => {
    tab.addEventListener('click', function() {
      discountType = this.dataset.type;
      document.querySelectorAll('.discount-tabs .tab').forEach(t => t.classList.remove('active'));
      this.classList.add('active');
      
      const suffix = document.getElementById('discountSuffix');
      suffix.textContent = discountType === 'percent' ? '%' : 'đ';
      
      updateDiscountPreview();
    });
  });
  
  // ✅ Load notification history từ database
  loadNotificationHistory();
  
  // ✅ Initialize reservations
  if (window.reservationsData) {
    reservations = window.reservationsData || [];
    console.log('📅 Reservations loaded:', reservations.length);
  }
  
  // ✅ Load today's reservations and start auto-refresh
  loadTodayReservations();
  startReservationSync();
  
  // ✅ Re-render tables to show reservation status
  renderTables();
});

// Render tables
function renderTables() {
  const tablesGrid = document.getElementById('tablesGrid');
  let filteredTables = window.tables || tables || [];
  
  console.log('Rendering tables:', filteredTables);
  console.log('Current filter:', currentFilter);
  console.log('Current room filter:', currentRoomFilter);
  console.log('Current capacity filter:', currentCapacityFilter);
  
  // ✅ Filter by status - dựa trên database status HOẶC invoices
  if (currentFilter !== 'all') {
    console.log('🔍 Filtering by status:', currentFilter);
    filteredTables = filteredTables.filter(table => {
      // Kiểm tra database status
      const dbStatus = (table.status || '').toLowerCase();
      const isOccupiedInDB = dbStatus === 'occupied';
      
      // Kiểm tra bàn có trong invoice nào không
      const isTableInUse = invoices.some(inv => inv.table && inv.table.id === table.id);
      
      // Bàn có khách nếu có trong database HOẶC trong invoice
      const isOccupied = isOccupiedInDB || isTableInUse;
      
      console.log('Table:', table.name, 'DB Status:', dbStatus, 'In invoice:', isTableInUse, 'Occupied:', isOccupied, 'Filter:', currentFilter);
      
      if (currentFilter === 'available') {
        // Trống: bàn KHÔNG có trong database (status != Occupied) VÀ KHÔNG có trong invoice nào
        return !isOccupied;
      } else if (currentFilter === 'occupied') {
        // Có khách: bàn CÓ trong database (status = Occupied) HOẶC CÓ trong invoice
        return isOccupied;
      }
      return false;
    });
    console.log('✅ After status filter:', filteredTables.length, 'tables');
  }
  
  // Filter by room
  if (currentRoomFilter !== 'all') {
    console.log('Filtering by room:', currentRoomFilter);
    filteredTables = filteredTables.filter(table => {
      // Get room name
      let tableRoomName = '';
      if (typeof table.room === 'string') {
        tableRoomName = table.room;
      } else if (table.room && table.room.name) {
        tableRoomName = table.room.name;
      } else if (table.roomId) {
        // Find room by ID
        const room = (window.rooms || rooms || []).find(r => r.id === table.roomId);
        tableRoomName = room ? room.name : '';
      }
      
      // Find selected room by ID
      const selectedRoom = (window.rooms || rooms || []).find(room => room.id == currentRoomFilter);
      if (selectedRoom) {
        console.log('Comparing:', tableRoomName, 'with', selectedRoom.name);
        return tableRoomName === selectedRoom.name;
      }
      
      return false;
    });
    console.log('After room filter:', filteredTables.length, 'tables');
  }
  
  // Filter by capacity
  if (currentCapacityFilter !== 'all') {
    console.log('Filtering by capacity:', currentCapacityFilter);
    filteredTables = filteredTables.filter(table => {
      const capacity = parseInt(table.capacity) || 4;
      if (currentCapacityFilter === '2-4') {
        return capacity >= 2 && capacity <= 4;
      } else if (currentCapacityFilter === '5-6') {
        return capacity >= 5 && capacity <= 6;
      } else if (currentCapacityFilter === '7+') {
        return capacity >= 7;
      }
      return true;
    });
    console.log('After capacity filter:', filteredTables.length, 'tables');
  }
  
  // ✅ Chỉ hiển thị ô "Mang về" và "Giao hàng" khi KHÔNG filter theo status
  // (tức là khi filter = 'all' hoặc chỉ filter theo phòng/sức chứa)
  const shouldShowSpecialTables = currentFilter === 'all';
  
  if (filteredTables.length === 0 && !shouldShowSpecialTables) {
    // Không có bàn nào và đang filter theo status
    tablesGrid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 20px; color: #6c757d;">
        <i class='bx bx-table' style="font-size: 48px; margin-bottom: 16px; color: #dee2e6;"></i>
        <p>Không có bàn nào</p>
      </div>
    `;
  } else {
    let allTablesHTML = [];
    
    // ✅ Thêm ô đặc biệt chỉ khi filter = 'all'
    if (shouldShowSpecialTables) {
      const takeawayTable = {
        id: 'takeaway',
        name: 'Mang về',
        room: 'Đặc biệt',
        capacity: 0,
        status: 'available',
        isTakeaway: true
      };
      
      const deliveryTable = {
        id: 'delivery',
        name: 'Giao hàng',
        room: 'Đặc biệt',
        capacity: 0,
        status: 'available',
        isDelivery: true
      };
      
      allTablesHTML.push(renderTableItem(takeawayTable));
      allTablesHTML.push(renderTableItem(deliveryTable));
    }
    
    // Thêm các bàn từ database
    allTablesHTML.push(...filteredTables.map(table => renderTableItem(table)));
    
    if (allTablesHTML.length === 0) {
      tablesGrid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 20px; color: #6c757d;">
          <i class='bx bx-table' style="font-size: 48px; margin-bottom: 16px; color: #dee2e6;"></i>
          <p>Không có bàn nào</p>
        </div>
      `;
    } else {
      tablesGrid.innerHTML = allTablesHTML.join('');
    }
  }
}

// Hàm render một table item
function renderTableItem(table) {
  // Kiểm tra bàn hiện tại có đang được chọn không (trong hóa đơn hiện tại)
  const isSelectedCurrent = selectedTable && selectedTable.id === table.id;
  
  // Kiểm tra bàn có đang được sử dụng trong hóa đơn nào không
  const tableInUse = invoices.find(inv => inv.table && inv.table.id === table.id);
  const isSelected = tableInUse !== undefined;
  
  // Debug log
  if (isSelected && !table.isTakeaway && !table.isDelivery) {
    console.log('🔴 Table in use:', table.name, 'in invoice:', tableInUse.id, tableInUse.name);
  }
  
  const tableStatus = (table.status || '').toLowerCase();
  
  // ✅ Xử lý đặc biệt cho ô "Mang về"
  if (table.isTakeaway) {
    console.log('Rendering takeaway table - isSelected:', isSelected);
    
    return `
      <div class="table-item takeaway ${isSelected ? 'selected' : ''}" 
           data-table-id="${table.id}" data-status="takeaway">
        <div class="table-icon">
          <i class='bx bx-shopping-bag'></i>
        </div>
        <span class="table-name">${table.name}</span>
        <span class="table-room">Mang về</span>
        <span class="table-capacity">Không giới hạn</span>
      </div>
    `;
  }
  
  // ✅ Xử lý đặc biệt cho ô "Giao hàng"
  if (table.isDelivery) {
    console.log('Rendering delivery table - isSelected:', isSelected);
    
    return `
      <div class="table-item delivery ${isSelected ? 'selected' : ''}" 
           data-table-id="${table.id}" data-status="delivery">
        <div class="table-icon">
          <i class='bx bx-car'></i>
        </div>
        <span class="table-name">${table.name}</span>
        <span class="table-room">Giao tận nơi</span>
        <span class="table-capacity">Không giới hạn</span>
      </div>
    `;
  }
  
  // ✅ Quyết định trạng thái dựa trên database status HOẶC invoices
  // - Nếu bàn có status "Occupied" trong database → "Đang có khách" (occupied)
  // - Nếu bàn có trong invoice → "Đang sử dụng" (occupied)
  // - Nếu không → "Trống" (available)
  let statusText, statusClass;
  const dbStatus = (table.status || '').toLowerCase();
  const isOccupiedInDB = dbStatus === 'occupied';
  
  if (isOccupiedInDB || isSelected) {
    statusText = isOccupiedInDB ? 'Đang có khách' : 'Đang sử dụng';
    statusClass = 'occupied';
  } else {
    statusText = 'Trống';
    statusClass = 'available';
  }
  
  // Handle room name - could be string or object
  let roomName = 'Chưa phân loại';
  if (typeof table.room === 'string') {
    roomName = table.room;
  } else if (table.room && table.room.name) {
    roomName = table.room.name;
  }
  
  // Parse capacity - ensure it's a number
  let capacity = table.capacity;
  if (typeof capacity === 'string') {
    capacity = parseInt(capacity);
  }
  if (!capacity || isNaN(capacity)) {
    capacity = 4; // Default fallback
  }
  
  // ✅ Check for reservation for this table today
  const tableReservation = getReservationForTable(table.id);
  const reservationStatusHtml = tableReservation ? renderReservationStatus(tableReservation) : '';
  
  // ✅ Sử dụng database status cho data-status attribute
  const dataStatus = isOccupiedInDB ? 'occupied' : statusClass;
  
  return `
    <div class="table-item ${statusClass} ${isSelected ? 'selected' : ''}" 
         data-table-id="${table.id}" data-status="${dataStatus}"
         title="${isSelected && !isSelectedCurrent ? 'Bàn đang được sử dụng: ' + tableInUse.name : ''}">
      <div class="table-icon">
        <i class='bx bx-table'></i>
      </div>
      <span class="table-name">${table.name}</span>
      <span class="table-room">${roomName}</span>
      <span class="table-capacity">${capacity} người</span>
      <span class="status-badge ${statusClass}">
        ${statusText}
      </span>
      ${reservationStatusHtml}
    </div>
  `;
}

// Render menu
function renderMenu() {
  const menuGrid = document.getElementById('menuGrid');
  const searchTerm = document.getElementById('headerSearch').value.toLowerCase();
  
  let filteredItems = menuItems || [];
  
  // Filter by category
  if (currentCategory !== 'all') {
    filteredItems = filteredItems.filter(item => {
      // So sánh categoryId (number) với currentCategory
      // currentCategory có thể là string hoặc number từ button data-category
      return String(item.categoryId) === String(currentCategory);
    });
  }
  
  // Filter by search term
  if (searchTerm) {
    filteredItems = filteredItems.filter(item => 
      item.name.toLowerCase().includes(searchTerm)
    );
  }
  
  if (filteredItems.length === 0) {
    menuGrid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #6c757d;">
        <i class='bx bx-food-menu' style="font-size: 48px; margin-bottom: 16px; color: #dee2e6;"></i>
        <p>Không có món ăn nào</p>
      </div>
    `;
  } else {
    menuGrid.innerHTML = filteredItems.map(item => {
      // Kiểm tra và hiển thị hình ảnh
      let imageHTML = '';
      if (item.imageUrl && item.imageUrl !== 'null' && item.imageUrl.trim() !== '') {
        // Có hình ảnh - hiển thị ảnh thực
        imageHTML = `<img src="${item.imageUrl}" alt="${item.name}" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                     <i class='bx bx-restaurant' style="display: none;"></i>`;
      } else {
        // Không có hình ảnh - hiển thị icon mặc định
        imageHTML = `<i class='bx bx-restaurant'></i>`;
      }
      
      return `
        <div class="menu-item" data-item-id="${item.variantId}" onclick="addToCart('${item.variantId}')">
          <div class="menu-item-image">
            ${imageHTML}
          </div>
          <span class="menu-item-name">${item.name}</span>
          ${item.size ? `<span class="menu-item-size">${item.size}</span>` : ''}
          <span class="price">${parseFloat(item.price || 0).toLocaleString('vi-VN')}đ</span>
        </div>
      `;
    }).join('');
  }
}

// ========================================
// RESERVATION FUNCTIONS
// ========================================

/**
 * Get reservation for a specific table today
 */
function getReservationForTable(tableId) {
  if (!reservations || reservations.length === 0) return null;
  if (!tableId) return null;
  
  // Convert tableId to string for comparison
  const tableIdStr = String(tableId);
  
  // Find reservation with matching tableId and status not CANCELLED or CLOSED
  const reservation = reservations.find(r => {
    if (!r.tableId) return false;
    const reservationTableId = String(r.tableId);
    return reservationTableId === tableIdStr && 
           r.status !== 'CANCELLED' && 
           r.status !== 'CLOSED';
  });
  
  return reservation || null;
}

/**
 * Render reservation status HTML for table item
 */
function renderReservationStatus(reservation) {
  if (!reservation) return '';
  
  const arrivalTime = new Date(reservation.arrivalTime);
  const timeStr = arrivalTime.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  });
  
  return `
    <div class="table-reservation-status">
      <div class="reservation-info-item">
        <i class='bx bx-time'></i>
        <span>${timeStr}</span>
      </div>
      <div class="reservation-info-item">
        <i class='bx bx-user'></i>
        <span>${reservation.numberOfGuests} khách</span>
      </div>
    </div>
  `;
}

/**
 * Load today's reservations from API
 */
function loadTodayReservations() {
  const today = new Date();
  const dateStr = formatDateForFilter(today);
  
  fetch(`${contextPath}/reception/api/reservations?date=${dateStr}`)
    .then(response => response.json())
    .then(data => {
      if (data.success && data.reservations) {
        reservations = data.reservations || [];
        console.log('📅 Reservations loaded:', reservations.length);
        
        // Re-render tables to update reservation status
        renderTables();
        
        // Re-render sidebar if open
        if (document.getElementById('reservationSidebar')?.classList.contains('active')) {
          renderReservationList();
        }
      }
    })
    .catch(error => {
      console.error('❌ Error loading reservations:', error);
    });
}

/**
 * Format date for API (YYYY-MM-DD)
 */
function formatDateForFilter(date) {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Start auto-refresh for reservations (every 30 seconds)
 */
function startReservationSync() {
  // Clear existing interval if any
  if (reservationSyncInterval) {
    clearInterval(reservationSyncInterval);
  }
  
  // Start new interval
  reservationSyncInterval = setInterval(() => {
    loadTodayReservations();
  }, 30000); // 30 seconds
  
  console.log('✅ Reservation sync started (30s interval)');
}

/**
 * Stop auto-refresh for reservations
 */
function stopReservationSync() {
  if (reservationSyncInterval) {
    clearInterval(reservationSyncInterval);
    reservationSyncInterval = null;
    console.log('⏸️ Reservation sync stopped');
  }
}

/**
 * Open reservation sidebar
 */
function openReservationSidebar() {
  const overlay = document.getElementById('reservationSidebarOverlay');
  const sidebar = document.getElementById('reservationSidebar');
  
  if (overlay && sidebar) {
    overlay.classList.add('active');
    sidebar.classList.add('active');
    renderReservationList();
  }
}

/**
 * Close reservation sidebar
 */
function closeReservationSidebar() {
  const overlay = document.getElementById('reservationSidebarOverlay');
  const sidebar = document.getElementById('reservationSidebar');
  
  if (overlay && sidebar) {
    overlay.classList.remove('active');
    sidebar.classList.remove('active');
  }
}

/**
 * Render reservation list in sidebar
 */
function renderReservationList() {
  const container = document.getElementById('reservationListSidebar');
  if (!container) return;
  
  // Get filter values
  const searchInput = document.getElementById('reservationSearchInput');
  const statusFilter = document.getElementById('reservationStatusFilter');
  
  const searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
  const statusValue = statusFilter ? statusFilter.value : '';
  
  // ✅ Filter reservations - chỉ hiển thị đặt bàn trong ngày hôm nay
  const today = new Date();
  const todayStr = formatDateForFilter(today);
  
  let filtered = (reservations || []).filter(r => {
    if (!r.arrivalTime) return false;
    
    // So sánh ngày của arrivalTime với ngày hôm nay
    const arrivalDate = new Date(r.arrivalTime);
    const arrivalDateStr = formatDateForFilter(arrivalDate);
    
    return arrivalDateStr === todayStr;
  });
  
  if (statusValue) {
    filtered = filtered.filter(r => r.status === statusValue);
  }
  
  if (searchTerm) {
    filtered = filtered.filter(r => {
      const name = (r.customerName || '').toLowerCase();
      const phone = (r.customerPhone || '').toLowerCase();
      const code = (r.reservationCode || '').toLowerCase();
      return name.includes(searchTerm) || 
             phone.includes(searchTerm) || 
             code.includes(searchTerm);
    });
  }
  
  // Sort: CLOSED at bottom, others by arrival time
  filtered.sort((a, b) => {
    const aClosed = a.status === 'CLOSED' ? 1 : 0;
    const bClosed = b.status === 'CLOSED' ? 1 : 0;
    if (aClosed !== bClosed) return aClosed - bClosed;
    const aTime = new Date(a.arrivalTime);
    const bTime = new Date(b.arrivalTime);
    return aTime - bTime;
  });
  
  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <i class='bx bx-calendar-x'></i>
        <p>Không có đặt bàn nào</p>
      </div>
    `;
    return;
  }
  
  const html = filtered.map(r => {
    const arrivalTime = new Date(r.arrivalTime);
    const timeStr = arrivalTime.toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit'
    });
    const dateStr = arrivalTime.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit'
    });
    
    const statusLabel = getReservationStatusLabel(r.status);
    const statusClass = r.status ? r.status.toLowerCase() : '';
    
    return `
      <div class="reservation-card" onclick="openEditReservationModal('${r.reservationId}')" style="cursor: pointer;">
        <div class="reservation-card-header">
          <div class="reservation-card-info">
            <h3>${escapeHtml(r.customerName || 'N/A')}</h3>
            <p>Mã: ${r.reservationCode || 'N/A'}</p>
          </div>
          <span class="status-badge ${statusClass}">${statusLabel}</span>
        </div>
        
        <div class="reservation-card-details">
          <div class="detail-item">
            <i class='bx bx-time'></i>
            <span>${timeStr} (${dateStr})</span>
          </div>
          <div class="detail-item">
            <i class='bx bx-user'></i>
            <span>${r.numberOfGuests || 0} khách</span>
          </div>
          <div class="detail-item">
            <i class='bx bx-phone'></i>
            <span>${r.customerPhone || 'N/A'}</span>
          </div>
          ${r.tableName ? `
            <div class="detail-item">
              <i class='bx bx-chair'></i>
              <span>${r.tableName}</span>
            </div>
          ` : ''}
        </div>
        
        ${r.notes ? `
          <div class="detail-item" style="margin: 6px 0 0 0; color:#4b5563;">
            <i class='bx bx-note'></i>
            <span>${escapeHtml(r.notes)}</span>
          </div>
        ` : ''}
      </div>
    `;
  }).join('');
  
  container.innerHTML = html;
}

/**
 * Filter reservation list in sidebar
 */
function filterReservationList() {
  renderReservationList();
}

/**
 * Get reservation status label in Vietnamese
 */
function getReservationStatusLabel(status) {
  const labels = {
    'PENDING': 'Chờ xác nhận',
    'CONFIRMED': 'Đã xác nhận',
    'SEATED': 'Đang phục vụ',
    'CANCELLED': 'Đã hủy',
    'NO_SHOW': 'Không đến',
    'CLOSED': 'Đã đóng'
  };
  return labels[status] || status || 'N/A';
}

/**
 * Escape HTML to prevent XSS
 */
function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

// Variable to store current editing reservation ID
let currentEditingReservationId = null;

/**
 * Open edit reservation modal
 */
function openEditReservationModal(reservationId) {
  const reservation = reservations.find(r => r.reservationId === reservationId || String(r.reservationId) === String(reservationId));
  
  if (!reservation) {
    console.error('❌ Reservation not found:', reservationId);
    return;
  }
  
  currentEditingReservationId = reservationId;
  
  // Format arrival time
  const arrivalTime = new Date(reservation.arrivalTime);
  const timeStr = arrivalTime.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  });
  const dateStr = arrivalTime.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
  
  // Format datetime-local for input
  const dateTimeLocal = formatDateTimeLocal(arrivalTime);
  
  const statusLabel = getReservationStatusLabel(reservation.status);
  const statusClass = reservation.status ? reservation.status.toLowerCase() : '';
  
  // Build form content
  const content = `
    <form id="editReservationForm">
      <input type="hidden" id="editReservationId" value="${reservation.reservationId}">
      
      <!-- Customer Information -->
      <div class="form-section">
        <h3 class="section-title">
          <i class='bx bx-user'></i>
          Thông tin khách hàng
        </h3>
        
        <div class="form-group">
          <label>Tên khách hàng</label>
          <input type="text" id="editCustomerName" class="form-control" value="${escapeHtml(reservation.customerName || '')}">
        </div>
        
        <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
          <div class="form-group">
            <label>Số điện thoại</label>
            <input type="tel" id="editCustomerPhone" class="form-control" value="${escapeHtml(reservation.customerPhone || '')}">
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" id="editCustomerEmail" class="form-control" value="${escapeHtml(reservation.customerEmail || '')}">
          </div>
        </div>
      </div>
      
      <!-- Reservation Details -->
      <div class="form-section">
        <h3 class="section-title">
          <i class='bx bx-calendar-event'></i>
          Thông tin đặt bàn
        </h3>
        
        <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
          <div class="form-group">
            <label>Ngày & Giờ đến</label>
            <input type="datetime-local" id="editArrivalTime" class="form-control" value="${dateTimeLocal}">
          </div>
          <div class="form-group">
            <label>Số khách</label>
            <input type="number" id="editNumberOfGuests" class="form-control" value="${reservation.numberOfGuests || 2}" min="1">
          </div>
        </div>
        
        <div class="form-group">
          <label>Trạng thái</label>
          <select id="editStatus" class="form-control">
            <option value="PENDING" ${reservation.status === 'PENDING' ? 'selected' : ''}>Chờ xác nhận</option>
            <option value="CONFIRMED" ${reservation.status === 'CONFIRMED' ? 'selected' : ''}>Đã xác nhận</option>
            <option value="SEATED" ${reservation.status === 'SEATED' ? 'selected' : ''}>Đang phục vụ</option>
            <option value="CLOSED" ${reservation.status === 'CLOSED' ? 'selected' : ''}>Đã đóng</option>
            <option value="CANCELLED" ${reservation.status === 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
          </select>
        </div>
        
        <div class="form-group">
          <label>Bàn</label>
          <input type="text" class="form-control" value="${escapeHtml(reservation.tableName || 'Chưa chọn bàn')}" readonly>
        </div>
        
        <div class="form-group">
          <label>Ghi chú</label>
          <textarea id="editNotes" class="form-control" rows="3">${escapeHtml(reservation.notes || '')}</textarea>
        </div>
      </div>
      
      <!-- Reservation Info Display -->
      <div class="form-section">
        <h3 class="section-title">
          <i class='bx bx-info-circle'></i>
          Thông tin bổ sung
        </h3>
        
        <div class="info-display">
          <div class="info-item">
            <span class="info-label">Mã đặt bàn:</span>
            <span class="info-value">${escapeHtml(reservation.reservationCode || 'N/A')}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Thời gian tạo:</span>
            <span class="info-value">${reservation.createdAt ? new Date(reservation.createdAt).toLocaleString('vi-VN') : 'N/A'}</span>
          </div>
        </div>
      </div>
    </form>
  `;
  
  document.getElementById('editReservationContent').innerHTML = content;
  
  // Show modal
  const modal = document.getElementById('editReservationModal');
  if (modal) {
    modal.style.display = 'flex';
  }
}

/**
 * Close edit reservation modal
 */
function closeEditReservationModal() {
  const modal = document.getElementById('editReservationModal');
  if (modal) {
    modal.style.display = 'none';
  }
  currentEditingReservationId = null;
}

/**
 * Format datetime for datetime-local input
 */
function formatDateTimeLocal(date) {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

/**
 * Delete reservation from modal
 */
function deleteReservationFromModal() {
  if (!currentEditingReservationId) {
    console.error('❌ No reservation ID to delete');
    return;
  }
  
  const reservation = reservations.find(r => 
    r.reservationId === currentEditingReservationId || 
    String(r.reservationId) === String(currentEditingReservationId)
  );
  
  if (!reservation) {
    console.error('❌ Reservation not found');
    return;
  }
  
  const customerName = reservation.customerName || 'N/A';
  const reservationCode = reservation.reservationCode || 'N/A';
  
  // Show confirmation
  if (!confirm(`Bạn có chắc chắn muốn xóa đặt bàn này?\n\nKhách hàng: ${customerName}\nMã đặt bàn: ${reservationCode}\n\nHành động này không thể hoàn tác!`)) {
    return;
  }
  
  // Call API to cancel/delete reservation
  fetch(`${contextPath}/reception/api/reservation/cancel`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      reservationId: currentEditingReservationId,
      reason: 'Xóa từ trang thu ngân'
    })
  })
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      // Show success message
      alert('✅ Đã xóa đặt bàn thành công!');
      
      // Close modal
      closeEditReservationModal();
      
      // Reload reservations
      loadTodayReservations();
      
      // Re-render tables
      renderTables();
      
      // Re-render sidebar if open
      if (document.getElementById('reservationSidebar')?.classList.contains('active')) {
        renderReservationList();
      }
    } else {
      alert('❌ Lỗi: ' + (data.message || 'Không thể xóa đặt bàn'));
    }
  })
  .catch(error => {
    console.error('❌ Error deleting reservation:', error);
    alert('❌ Lỗi kết nối: Không thể xóa đặt bàn');
  });
}

/**
 * Save reservation changes
 */
function saveReservationChanges() {
  if (!currentEditingReservationId) {
    console.error('❌ No reservation ID to update');
    return;
  }
  
  // Get form values
  const customerName = document.getElementById('editCustomerName')?.value || '';
  const customerPhone = document.getElementById('editCustomerPhone')?.value || '';
  const customerEmail = document.getElementById('editCustomerEmail')?.value || '';
  const arrivalTime = document.getElementById('editArrivalTime')?.value || '';
  const numberOfGuests = parseInt(document.getElementById('editNumberOfGuests')?.value || '2');
  const status = document.getElementById('editStatus')?.value || 'PENDING';
  const notes = document.getElementById('editNotes')?.value || '';
  
  // Validate
  if (!customerName.trim()) {
    alert('⚠️ Vui lòng nhập tên khách hàng');
    return;
  }
  
  if (!customerPhone.trim()) {
    alert('⚠️ Vui lòng nhập số điện thoại');
    return;
  }
  
  // Prepare data
  const data = {
    reservationId: currentEditingReservationId,
    customerName: customerName.trim(),
    customerPhone: customerPhone.trim(),
    customerEmail: customerEmail.trim(),
    arrivalTime: arrivalTime,
    numberOfGuests: numberOfGuests,
    status: status,
    notes: notes.trim()
  };
  
  // Call API to update
  fetch(`${contextPath}/reception/api/reservation/update`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  })
  .then(response => response.json())
  .then(result => {
    if (result.success) {
      alert('✅ Cập nhật đặt bàn thành công!');
      
      // Reload reservations
      loadTodayReservations();
      
      // Re-render tables
      renderTables();
      
      // Re-render sidebar if open
      if (document.getElementById('reservationSidebar')?.classList.contains('active')) {
        renderReservationList();
      }
      
      // Close modal
      closeEditReservationModal();
    } else {
      alert('❌ Lỗi: ' + (result.message || 'Không thể cập nhật đặt bàn'));
    }
  })
  .catch(error => {
    console.error('❌ Error updating reservation:', error);
    alert('❌ Lỗi kết nối: Không thể cập nhật đặt bàn');
  });
}

// Populate menu categories - Dropdown version
function populateMenuCategories() {
  const categoryFilters = document.querySelector('.category-filters');
  if (!categoryFilters) return;
  
  console.log('🔧 Populating menu categories...');
  console.log('Total menuItems:', menuItems ? menuItems.length : 0);
  console.log('Total categories:', categories ? categories.length : 0);
  
  if (categories && categories.length > 0) {
    // Get existing "Tất cả" button
    const allButton = categoryFilters.querySelector('.category-btn[data-category="all"]');
    
    // Update count for "Tất cả"
    const totalCount = menuItems.length;
    console.log('Total count for "Tất cả":', totalCount);
    
    if (allButton) {
      allButton.innerHTML = `
        <i class='bx bx-category'></i>
        <span>Tất cả</span>
        <span class="category-count">${totalCount}</span>
      `;
      
      // Add click event for "Tất cả" button
      allButton.addEventListener('click', function() {
        document.querySelectorAll('.category-btn').forEach(btn => btn.classList.remove('active'));
        this.classList.add('active');
        currentCategory = 'all';
        renderMenu();
      });
    }
    
    // Add category buttons
    categories.forEach(category => {
      // Đếm số món theo categoryId
      const itemCount = menuItems.filter(item => String(item.categoryId) === String(category.id)).length;
      console.log(`📊 Category "${category.name}" (ID: ${category.id}): ${itemCount} items`);
      
      const button = document.createElement('button');
      button.className = 'category-btn';
      button.dataset.category = category.id;
      
      // Get icon based on category name
      const icon = getCategoryIcon(category.name);
      
      button.innerHTML = `
        <i class='bx ${icon}'></i>
        <span>${category.name}</span>
        <span class="category-count">${itemCount}</span>
      `;
      
      button.addEventListener('click', function() {
        // Remove active from all buttons
        document.querySelectorAll('.category-btn').forEach(btn => btn.classList.remove('active'));
        // Add active to clicked button
        this.classList.add('active');
        
        // Update current category
        currentCategory = this.dataset.category;
        renderMenu();
      });
      
      categoryFilters.appendChild(button);
    });
  }
}

// Get icon for category
function getCategoryIcon(categoryName) {
  const icons = {
    'Khai vị': 'bx-dish',
    'Món chính': 'bxs-bowl-rice',
    'Món phụ': 'bx-food-menu',
    'Đồ uống': 'bx-drink',
    'Tráng miệng': 'bx-cake',
    'Salad': 'bx-leaf',
    'Soup': 'bx-bowl-hot',
    'Nước': 'bx-water',
    'Bia': 'bx-beer',
    'Rượu': 'bx-wine',
    'Cà phê': 'bx-coffee',
    'Trà': 'bx-coffee-togo'
  };
  
  // Find matching icon
  for (const [key, icon] of Object.entries(icons)) {
    if (categoryName.toLowerCase().includes(key.toLowerCase())) {
      return icon;
    }
  }
  
  // Default icon
  return 'bx-food-tag';
}

// Populate room filters - Dropdown version
function populateRoomFilters() {
  const roomFilter = document.getElementById('roomFilter');
  if (!roomFilter) return;
  
  if (rooms && rooms.length > 0) {
    rooms.forEach(room => {
      // Count tables in this room
      const roomTableCount = tables.filter(table => {
        if (typeof table.room === 'string') {
          return table.room === room.name;
        } else if (table.room && table.room.name) {
          return table.room.name === room.name;
        } else if (table.roomId) {
          return table.roomId === room.id;
        }
        return false;
      }).length;
      
      const option = document.createElement('option');
      option.value = room.id;
      option.textContent = room.name + ' (' + roomTableCount + ')';
      
      roomFilter.appendChild(option);
    });
  }
}

// Add item to cart
function addToCart(variantId) {
  if (!selectedTable) {
    window.notificationManager?.show('Vui lòng chọn bàn trước khi thêm món!', 'warning', 'Chưa chọn bàn');
    return;
  }
  
  const item = menuItems.find(i => i.variantId === variantId);
  if (!item) {
    window.notificationManager?.show('Không tìm thấy món ăn!', 'error', 'Lỗi');
    return;
  }
  
  // ✅ Kiểm tra tồn kho
  const stock = item.stock || 0;
  const existingItem = orderItems.find(i => i.variantId === variantId);
  const currentQty = existingItem ? existingItem.quantity : 0;
  const newQty = currentQty + 1;
  
  console.log(`🔍 Adding ${item.name}:`, {
    stock: stock,
    currentQty: currentQty,
    newQty: newQty,
    willExceedStock: newQty > stock
  });
  
  // ⚠️ Cảnh báo nếu hết hàng - NHƯNG VẪN CHO THÊM
  if (stock <= 0) {
    console.warn(`❌ ${item.name} HẾT HÀNG!`);
    window.notificationManager?.show(
      `${item.name} hiện đang HẾT HÀNG. Bạn vẫn có thể thêm nhưng không thể thanh toán!`,
      'warning',
      'Cảnh báo tồn kho'
    );
  } else if (newQty > stock) {
    console.warn(`⚠️ ${item.name} vượt quá tồn: ${newQty} > ${stock}`);
    window.notificationManager?.show(
      `${item.name} chỉ còn ${stock} sản phẩm. Bạn vẫn có thể thêm nhưng không thể thanh toán!`,
      'warning',
      'Vượt quá tồn kho'
    );
  }
  
  if (existingItem) {
    // ✅ Di chuyển item lên đầu danh sách khi thêm lại
    const index = orderItems.indexOf(existingItem);
    if (index > -1) {
      orderItems.splice(index, 1); // Xóa khỏi vị trí cũ
    }
    
    existingItem.quantity += 1;
    existingItem.stock = stock; // ✅ Cập nhật stock
    existingItem.outOfStock = newQty > stock; // ✅ Đánh dấu hết hàng
    
    // Track số lượng đã notify để chỉ gửi phần mới
    if (existingItem.notified && existingItem.notifiedQuantity) {
      // Nếu đã notify trước đó, giữ số lượng đã notify
      // Phần tăng thêm sẽ được gửi ở lần notify tiếp theo
    } else if (existingItem.notified) {
      // Nếu đã notify nhưng chưa có notifiedQuantity, set lại
      existingItem.notifiedQuantity = existingItem.quantity - 1; // số lượng cũ đã gửi
    }
    
    // ✅ Thêm lại vào đầu danh sách
    orderItems.unshift(existingItem);
  } else {
    // Display name with size if available
    const itemName = item.name || 'Món ăn';
    const itemSize = item.size;
    const displayName = itemSize ? itemName + ' (' + itemSize + ')' : itemName;
    
    // ✅ Thêm item mới vào đầu danh sách
    orderItems.unshift({
      id: item.id,
      variantId: item.variantId,
      name: displayName,
      price: parseFloat(item.price || 0),
      quantity: 1,
      stock: stock, // ✅ Lưu thông tin stock
      outOfStock: newQty > stock, // ✅ Đánh dấu hết hàng
      notifiedQuantity: 0, // Chưa notify món nào
      note: '' // Ghi chú cho món
    });
  }
  
  renderOrderItems();
  updateBill();
  syncCurrentInvoice(); // ✅ Lưu vào invoice
}

// Remove item from cart
function removeFromCart(variantId) {
  orderItems = orderItems.filter(item => item.variantId !== variantId);
  renderOrderItems();
  updateBill();
  syncCurrentInvoice(); // ✅ Lưu vào invoice
}

// Update item quantity
function updateQuantity(variantId, newQuantity) {
  if (newQuantity <= 0) {
    removeFromCart(variantId);
    return;
  }
  
  const item = orderItems.find(i => i.variantId === variantId);
  if (item) {
    // ✅ Kiểm tra tồn kho khi tăng số lượng
    const stock = item.stock || 0;
    
    if (newQuantity > item.quantity) { // Đang tăng số lượng
      if (newQuantity > stock) {
        window.notificationManager?.show(
          `${item.name} chỉ còn ${stock} sản phẩm. Bạn vẫn có thể thêm nhưng không thể thanh toán!`,
          'warning',
          'Vượt quá tồn kho'
        );
      }
    }
    
    item.quantity = newQuantity;
    item.outOfStock = newQuantity > stock; // ✅ Cập nhật trạng thái hết hàng
    renderOrderItems();
    updateBill();
    syncCurrentInvoice(); // ✅ Lưu vào invoice
  }
}

// Update item note
function updateNote(variantId, note) {
  const item = orderItems.find(i => i.variantId === variantId);
  if (item) {
    item.note = note;
    console.log('Updated note for', item.name, ':', note);
    syncCurrentInvoice(); // ✅ Lưu vào invoice
  }
}

// Render order items
function renderOrderItems() {
  const orderItemsContainer = document.getElementById('orderItems');
  
  if (orderItems.length === 0) {
    orderItemsContainer.innerHTML = `
      <div class="empty-order">
        <i class='bx bx-shopping-cart'></i>
        <p>Chưa có món nào được chọn</p>
      </div>
    `;
    return;
  }
  
  orderItemsContainer.innerHTML = orderItems.map((item, index) => {
    const itemName = item.name || 'Món ăn';
    const itemPrice = item.price || 0;
    const itemQuantity = item.quantity || 1;
    const itemVariantId = item.variantId || '';
    const itemNote = item.note || '';
    const notifiedQty = item.notifiedQuantity || 0;
    const newQty = itemQuantity - notifiedQty;
    const itemIndex = index + 1;
    
    // ✅ Tính giá với discount per item
    const itemDiscount = item.discount || null;
    let itemSubtotal = itemPrice * itemQuantity;
    let itemDiscountAmount = 0;
    let itemTotal = itemSubtotal;
    
    if (itemDiscount) {
      if (itemDiscount.type === 'percent') {
        itemDiscountAmount = Math.round(itemSubtotal * itemDiscount.value / 100);
      } else {
        itemDiscountAmount = itemDiscount.value;
      }
      itemTotal = Math.max(0, itemSubtotal - itemDiscountAmount);
    }
    
    // ✅ Kiểm tra tồn kho
    const stock = item.stock || 0;
    const outOfStock = item.outOfStock || itemQuantity > stock;
    const stockWarning = outOfStock ? 
      `<span class="stock-warning">⚠️ Hết hàng</span>` : '';
    
    // Xác định trạng thái: toàn bộ đã gửi, một phần, hoặc chưa gửi
    let isNotified, notifiedBadge;
    if (newQty === 0) {
      isNotified = 'notified';
      notifiedBadge = '<span class="notified-badge">✓ Bếp</span>';
    } else if (notifiedQty > 0) {
      isNotified = 'partial';
      notifiedBadge = '<span class="partial-badge">' + notifiedQty + '/' + itemQuantity + '</span>';
    } else {
      isNotified = 'new';
      notifiedBadge = '<span class="new-badge">Mới</span>';
    }
    
    // ✅ Note button
    const noteButton = itemNote 
      ? `<i class='bx bx-note' style="color: #28a745; cursor: pointer;" title="${itemNote}"></i>`
      : `<i class='bx bx-note' style="color: #dee2e6; cursor: pointer;" onclick="document.getElementById('note-${itemVariantId}').focus()" title="Thêm ghi chú"></i>`;
    
    // ✅ Discount display
    let discountBadge = '';
    if (itemDiscount) {
      const discountText = itemDiscount.type === 'percent' 
        ? `Giảm ${itemDiscount.value}%` 
        : `Giảm ${itemDiscount.value.toLocaleString('vi-VN')}đ`;
      discountBadge = `<span class="discount-badge">${discountText}</span>`;
    }
    
    // ✅ Total price display with discount info
    let totalPriceDisplay = '';
    if (itemDiscount) {
      totalPriceDisplay = `
        <div class="price-with-discount" onclick="openItemDiscountModal('${itemVariantId}')" title="Click để chỉnh sửa giảm giá">
          <span class="original-total">${itemSubtotal.toLocaleString('vi-VN')}đ</span>
          ${discountBadge}
          <span class="final-total">${itemTotal.toLocaleString('vi-VN')}đ</span>
        </div>
      `;
    } else {
      totalPriceDisplay = `
        <span class="item-price-total" onclick="openItemDiscountModal('${itemVariantId}')" title="Click để áp dụng giảm giá">
          ${itemTotal.toLocaleString('vi-VN')}đ
        </span>
      `;
    }
    
    return `
      <div class="order-item ${isNotified} ${outOfStock ? 'out-of-stock' : ''}">
        <span style="font-weight: 700; color: #6c757d; min-width: 30px;">${itemIndex}.</span>
        <div class="item-info">
          <div class="item-name">
            ${itemName}
            ${notifiedBadge}
            ${stockWarning}
            ${noteButton}
          </div>
          <input type="text" 
                 id="note-${itemVariantId}"
                 class="item-note-input" 
                 placeholder="Ghi chú món (nhấn icon note để thêm)"
                 value="${itemNote}"
                 onchange="updateNote('${itemVariantId}', this.value)">
        </div>
        <span class="item-price" style="text-align: right; white-space: nowrap;">${itemPrice.toLocaleString('vi-VN')}đ</span>
        <div class="quantity-controls">
          <button onclick="updateQuantity('${itemVariantId}', ${itemQuantity - 1})">
            <i class='bx bx-minus'></i>
          </button>
          <span class="quantity">${itemQuantity}</span>
          <button onclick="updateQuantity('${itemVariantId}', ${itemQuantity + 1})">
            <i class='bx bx-plus'></i>
          </button>
        </div>
        ${totalPriceDisplay}
        <button class="remove-btn" onclick="removeFromCart('${itemVariantId}')">
          <i class='bx bx-trash'></i>
        </button>
      </div>
    `;
  }).join('');
}

// Update bill
function updateBill() {
  // ✅ Tính subtotal sau khi trừ discount per item
  let subtotal = 0;
  let totalItemDiscounts = 0;
  
  orderItems.forEach(item => {
    const itemSubtotal = item.price * item.quantity;
    let itemDiscountAmount = 0;
    
    if (item.discount) {
      if (item.discount.type === 'percent') {
        itemDiscountAmount = Math.round(itemSubtotal * item.discount.value / 100);
      } else {
        itemDiscountAmount = item.discount.value;
      }
      totalItemDiscounts += itemDiscountAmount;
    }
    
    subtotal += Math.max(0, itemSubtotal - itemDiscountAmount);
  });
  
  // Calculate discount toàn đơn (áp dụng sau khi đã trừ discount per item)
  let orderDiscountAmount = 0;
  if (currentDiscount) {
    if (currentDiscount.type === 'percent') {
      orderDiscountAmount = Math.round(subtotal * currentDiscount.value / 100);
    } else {
      orderDiscountAmount = currentDiscount.value;
    }
  }
  
  // ✅ Lấy VAT rate từ input
  const vatRateInput = document.getElementById('vatRate');
  const vatRate = vatRateInput ? parseFloat(vatRateInput.value) || 0 : 10;
  
  const afterDiscount = Math.max(0, subtotal - orderDiscountAmount);
  const vat = Math.round(afterDiscount * (vatRate / 100));
  const total = afterDiscount + vat;
  
  // ✅ Tổng discount = discount per item + discount toàn đơn
  const totalDiscount = totalItemDiscounts + orderDiscountAmount;
  
  document.getElementById('subtotal').textContent = subtotal.toLocaleString('vi-VN') + 'đ';
  document.getElementById('discount').textContent = '-' + orderDiscountAmount.toLocaleString('vi-VN') + 'đ';
  document.getElementById('vat').textContent = vat.toLocaleString('vi-VN') + 'đ';
  document.getElementById('total').textContent = total.toLocaleString('vi-VN') + 'đ';
  
  // ✅ Kiểm tra có món hết hàng không
  const hasOutOfStockItems = orderItems.some(item => {
    const stock = item.stock || 0;
    return item.quantity > stock;
  });
  
  // Enable/disable buttons
  const hasItemsAndTable = orderItems.length > 0 && selectedTable;
  
  // ⚠️ Disable checkout nếu có món hết hàng
  const checkoutBtn = document.getElementById('checkoutBtn');
  checkoutBtn.disabled = !hasItemsAndTable || hasOutOfStockItems;
  
  // Update button title để thông báo lý do
  if (hasOutOfStockItems) {
    checkoutBtn.title = 'Không thể thanh toán vì có món hết hàng. Vui lòng điều chỉnh số lượng.';
    checkoutBtn.style.cursor = 'not-allowed';
  } else if (!hasItemsAndTable) {
    checkoutBtn.title = 'Vui lòng chọn bàn và thêm món';
    checkoutBtn.style.cursor = 'not-allowed';
  } else {
    checkoutBtn.title = 'Thanh toán đơn hàng';
    checkoutBtn.style.cursor = 'pointer';
  }
  
  document.getElementById('notifyKitchenBtn').disabled = !hasItemsAndTable;
  
  // Enable/disable print bill button
  const printBillBtn = document.getElementById('printBillBtn');
  if (printBillBtn) {
    printBillBtn.disabled = !hasItemsAndTable;
  }
}

// Load orders của bàn đang có khách
async function loadTableOrders(tableId) {
  console.log('Loading orders for table:', tableId);
  
  try {
    const response = await fetch(contextPath + '/api/order/table/' + tableId);
    const result = await response.json();
    
    if (result.success) {
      console.log('Loaded orders from database:', result.orders);
      
      // Convert orders từ database sang format orderItems
      const dbOrderItems = result.orders.map(item => {
        // Display name with size
        const itemName = item.name || 'Món ăn';
        const itemSize = item.size;
        const displayName = itemSize ? itemName + ' (' + itemSize + ')' : itemName;
        const qty = parseInt(item.quantity || 1);
        
        return {
          id: item.productId,
          variantId: item.variantId,
          name: displayName,
          price: parseFloat(item.price || 0),
          quantity: qty,
          notified: true, // Đã được gửi bếp (từ database)
          notifiedQuantity: qty, // Số lượng đã gửi = số lượng hiện tại (từ DB)
          status: item.status, // Pending, Preparing, Ready, etc.
          note: item.note || '' // Ghi chú từ DB
        };
      });
      
      // Merge items có cùng variantId (group by variant)
      const mergedDbItems = [];
      dbOrderItems.forEach(item => {
        const existing = mergedDbItems.find(i => i.variantId === item.variantId);
        if (existing) {
          existing.quantity += item.quantity;
          existing.notifiedQuantity += item.notifiedQuantity;
          // Merge notes: nếu có note mới, append; nếu không giữ note cũ
          if (item.note && item.note !== existing.note) {
            existing.note = existing.note ? existing.note + '; ' + item.note : item.note;
          }
        } else {
          mergedDbItems.push(item);
        }
      });
      
      // ✅ Merge với orders chưa notify trong invoice memory (nếu có)
      const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
      const memoryOrders = currentInvoice?.orders || [];
      
      // Tìm các món chưa notify trong memory
      const unnotifiedItems = memoryOrders.filter(item => {
        const notifiedQty = item.notifiedQuantity || 0;
        const currentQty = item.quantity || 0;
        return currentQty > notifiedQty; // Có món chưa notify
      });
      
      // Merge: Start với DB orders, sau đó merge với memory orders (chưa notify)
      orderItems = [...mergedDbItems];
      
      unnotifiedItems.forEach(memoryItem => {
        const notifiedQty = memoryItem.notifiedQuantity || 0;
        const currentQty = memoryItem.quantity || 0;
        const newQty = currentQty - notifiedQty; // Số lượng chưa notify
        
        if (newQty > 0) {
          // Tìm xem đã có variant này trong DB orders chưa
          const existing = orderItems.find(i => i.variantId === memoryItem.variantId);
          if (existing) {
            // Đã có trong DB - chỉ cập nhật số lượng và notifiedQuantity
            // Giữ số lượng từ DB, nhưng có thể thêm số lượng mới chưa notify
            // (Thực ra không cần merge vì DB là source of truth)
          } else {
            // Chưa có trong DB - thêm món chưa notify vào
            orderItems.push({
              ...memoryItem,
              quantity: newQty, // Chỉ giữ số lượng chưa notify
              notified: false,
              notifiedQuantity: 0
            });
          }
        }
      });
      
      // ✅ Reload stock từ database để đảm bảo có thông tin stock đầy đủ
      // (Quan trọng để tránh hiển thị "hết hàng" sai và không thể thanh toán)
      try {
        await reloadMenuStock();
      } catch (error) {
        console.warn('⚠️ Could not reload stock after loading orders:', error);
        // Không block nếu reload stock thất bại
      }
      
      renderOrderItems();
      updateBill();
      syncCurrentInvoice(); // ✅ Lưu vào invoice
      
      console.log('Orders loaded successfully:', orderItems.length, 'items (DB:', mergedDbItems.length, ', Memory:', unnotifiedItems.length, ')');
      return true;
    } else {
      console.error('Error loading orders:', result.message);
      // Không hiện alert nếu bàn chưa có orders
      orderItems = [];
      renderOrderItems();
      updateBill();
      return false;
    }
  } catch (error) {
    console.error('Error loading table orders:', error);
    orderItems = [];
    renderOrderItems();
    updateBill();
    return false;
  }
}

// Reload tables from server to get latest status
async function reloadTablesFromServer() {
  try {
    console.log('🔄 Reloading tables from server...');
    const response = await fetch(contextPath + '/cashier?action=getTables');
    const result = await response.json();
    
    if (result.success && result.tables) {
      // Update tables array
      window.tables = result.tables;
      tables = result.tables;
      
      // Update selectedTable status if exists
      if (selectedTable && selectedTable.id && !selectedTable.isTakeaway && !selectedTable.isDelivery) {
        const updatedTable = tables.find(t => t.id === selectedTable.id);
        if (updatedTable) {
          selectedTable.status = updatedTable.status;
          console.log('✅ Updated selectedTable status:', selectedTable.status);
        }
      }
      
      // Re-render tables
      renderTables();
      updateFilterCounts();
      
      console.log('✅ Tables reloaded successfully:', tables.length, 'tables');
      return true;
    } else {
      console.error('❌ Error reloading tables:', result.message);
      return false;
    }
  } catch (error) {
    console.error('❌ Error reloading tables from server:', error);
    return false;
  }
}

// Notify kitchen - send order to kitchen
async function notifyKitchen() {
  if (orderItems.length === 0) {
    window.notificationManager?.show('Vui lòng chọn ít nhất một món!', 'warning', 'Chưa có món');
    return false;
  }
  
  if (!selectedTable) {
    window.notificationManager?.show('Vui lòng chọn bàn!', 'warning', 'Chưa chọn bàn');
    return false;
  }
  
  // Tạo danh sách món cần gửi bếp
  const itemsToNotify = [];
  
  orderItems.forEach(item => {
    const notifiedQty = item.notifiedQuantity || 0;
    const currentQty = item.quantity || 0;
    const newQty = currentQty - notifiedQty;
    
    if (newQty > 0) {
      // Có món mới chưa gửi
      itemsToNotify.push({
        variantId: item.variantId,
        quantity: newQty, // CHỈ GỬI số lượng mới
        unitPrice: item.price,
        note: item.note || '', // Ghi chú
        originalItem: item // Reference để update sau
      });
    }
  });
  
  if (itemsToNotify.length === 0) {
    // ✅ Nếu không có món mới, vẫn return true (đã được thông báo hết rồi)
    return true;
  }
  
  // ✅ Get current invoice name and note
  const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
  const invoiceName = currentInvoice?.name || '';
  const orderNote = currentInvoice?.note || ''; // ✅ Lấy ghi chú hóa đơn
  
  // Prepare order data
  const orderData = {
    tableId: selectedTable.id,
    invoiceName: invoiceName, // ✅ Gửi invoice name lên backend
    orderNote: orderNote, // ✅ Gửi ghi chú hóa đơn lên backend
    items: itemsToNotify.map(item => ({
      variantId: item.variantId,
      quantity: item.quantity,
      unitPrice: item.unitPrice,
      note: item.note || ''
    }))
  };
  
  console.log('Sending NEW items to kitchen:', orderData);
  console.log('Invoice name:', invoiceName);
  console.log('Total items in order:', orderItems.length);
  console.log('Items to notify:', itemsToNotify.length);
  
  try {
    const response = await fetch(contextPath + '/api/cashier/order/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(orderData)
    });
    
    const result = await response.json();
    
    if (result.success) {
      // Tính tổng số món đã gửi
      const totalNewQty = itemsToNotify.reduce((sum, item) => sum + item.quantity, 0);
      
      // Lấy tên hóa đơn hiện tại
      const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
      const invoiceName = currentInvoice?.name || 'Hóa đơn';
      const invoiceNote = currentInvoice?.note || '';
      
      // Hiển thị thông báo với ghi chú (nếu có)
      let notificationMsg = `${invoiceName} | Số món: ${totalNewQty} | Đơn hàng: ${result.orderId}`;
      if (invoiceNote) {
        notificationMsg += `\n📝 Ghi chú: ${invoiceNote}`;
      }
      
      window.notificationManager?.show(
        notificationMsg,
        'success',
        'Đã gửi thông báo đến bếp!'
      );
      
      // ✅ Phát âm âm thanh thông báo
      playNotificationSound('notify');
      
      // ✅ Làm mới lịch sử thông báo từ database
      refreshNotificationHistory();
      
      // ✅ Cập nhật notifiedQuantity cho các món vừa gửi (giữ nguyên orders trong memory)
      itemsToNotify.forEach(item => {
        item.originalItem.notified = true;
        item.originalItem.notifiedQuantity = item.originalItem.quantity;
      });
      
      // ✅ Render lại để hiển thị TẤT CẢ món (đã notify)
      renderOrderItems();
      updateBill();
      syncCurrentInvoice(); // ✅ Lưu vào invoice
      
      // ✅ RELOAD TABLES từ database để cập nhật trạng thái bàn (KHÔNG reload orders)
      await reloadTablesFromServer();
      
      // ✅ KHÔNG reload orders từ database ngay sau khi notify vì:
      // 1. Orders đã được lưu vào DB, nhưng có thể chưa có đầy đủ thông tin (như stock)
      // 2. Reload sẽ làm mất thông tin stock, dẫn đến hiển thị "hết hàng" và không thể thanh toán
      // 3. Orders sẽ được reload tự động khi:
      //    - Chọn lại bàn (nếu bàn có status Occupied)
      //    - Switch invoice (nếu invoice có bàn với status Occupied)
      //    - F5 và chọn lại bàn
      
      console.log('Order created successfully:', result.orderId);
      console.log('All items after notify:', orderItems);
      
      return true; // ✅ Thành công
    } else {
      window.notificationManager?.show(result.message, 'error', 'Lỗi');
      return false; // ✅ Thất bại
    }
  } catch (error) {
    console.error('Error notifying kitchen:', error);
    window.notificationManager?.show('Không thể gửi thông báo đến bếp. Vui lòng thử lại.', 'error', 'Lỗi kết nối');
    return false; // ✅ Thất bại
  }
}

// Setup tab system
function setupTabSystem() {
  const tabButtons = document.querySelectorAll('.tab-btn');
  const tabPanels = document.querySelectorAll('.tab-panel');
  
  tabButtons.forEach(button => {
    button.addEventListener('click', function() {
      const targetTab = this.dataset.tab;
      
      // Remove active class from all buttons and panels
      tabButtons.forEach(btn => btn.classList.remove('active'));
      tabPanels.forEach(panel => panel.classList.remove('active'));
      
      // Add active class to clicked button and corresponding panel
      this.classList.add('active');
      document.getElementById(targetTab + '-tab').classList.add('active');
      
      console.log('Switched to tab:', targetTab);
    });
  });
}

// Update filter counts
function updateFilterCounts() {
  const allTables = window.tables || tables || [];
  
  // ✅ Đếm dựa trên database status HOẶC invoices
  const availableTables = allTables.filter(t => {
    // Kiểm tra database status
    const dbStatus = (t.status || '').toLowerCase();
    const isOccupiedInDB = dbStatus === 'occupied';
    
    // Kiểm tra bàn có trong invoice nào không
    const isTableInUse = invoices.some(inv => inv.table && inv.table.id === t.id);
    
    // Bàn trống = bàn KHÔNG có status Occupied trong database VÀ KHÔNG có trong invoice nào
    return !isOccupiedInDB && !isTableInUse;
  });
  
  const occupiedTables = allTables.filter(t => {
    // Kiểm tra database status
    const dbStatus = (t.status || '').toLowerCase();
    const isOccupiedInDB = dbStatus === 'occupied';
    
    // Kiểm tra bàn có trong invoice nào không
    const isTableInUse = invoices.some(inv => inv.table && inv.table.id === t.id);
    
    // Bàn có khách = bàn CÓ status Occupied trong database HOẶC CÓ trong invoice
    return isOccupiedInDB || isTableInUse;
  });
  
  // Update status dropdown options
  const statusFilter = document.getElementById('statusFilter');
  if (statusFilter) {
    statusFilter.options[0].text = 'Tất cả (' + allTables.length + ')';
    statusFilter.options[1].text = 'Trống (' + availableTables.length + ')';
    statusFilter.options[2].text = 'Có khách (' + occupiedTables.length + ')';
  }
  
  // Update capacity dropdown options
  const capacity24 = allTables.filter(t => {
    const cap = parseInt(t.capacity) || 4;
    return cap >= 2 && cap <= 4;
  }).length;
  const capacity56 = allTables.filter(t => {
    const cap = parseInt(t.capacity) || 4;
    return cap >= 5 && cap <= 6;
  }).length;
  const capacity7 = allTables.filter(t => {
    const cap = parseInt(t.capacity) || 4;
    return cap >= 7;
  }).length;
  
  const capacityFilter = document.getElementById('capacityFilter');
  if (capacityFilter) {
    capacityFilter.options[0].text = 'Tất cả (' + allTables.length + ')';
    capacityFilter.options[1].text = '2-4 chỗ (' + capacity24 + ')';
    capacityFilter.options[2].text = '5-6 chỗ (' + capacity56 + ')';
    capacityFilter.options[3].text = '7+ chỗ (' + capacity7 + ')';
  }
  
  // Update room dropdown options
  const roomFilter = document.getElementById('roomFilter');
  if (roomFilter && roomFilter.options[0]) {
    roomFilter.options[0].text = 'Tất cả (' + allTables.length + ')';
    
    // Update counts for each room option
    if (rooms && rooms.length > 0) {
      rooms.forEach((room, index) => {
        const roomTableCount = allTables.filter(table => {
          if (typeof table.room === 'string') {
            return table.room === room.name;
          } else if (table.room && table.room.name) {
            return table.room.name === room.name;
          } else if (table.roomId) {
            return table.roomId === room.id;
          }
          return false;
        }).length;
        
        // Index + 1 vì option 0 là "Tất cả"
        const optionIndex = index + 1;
        if (roomFilter.options[optionIndex]) {
          roomFilter.options[optionIndex].text = room.name + ' (' + roomTableCount + ')';
        }
      });
    }
  }
  
  // Update category dropdown options
  const categoryFilter = document.getElementById('categoryFilter');
  if (categoryFilter && categoryFilter.options[0]) {
    categoryFilter.options[0].text = 'Tất cả (' + menuItems.length + ')';
    
    // Update counts for each category option
    if (categories && categories.length > 0) {
      categories.forEach((category, index) => {
        const itemCount = menuItems.filter(item => item.categoryId === category.id).length;
        
        // Index + 1 vì option 0 là "Tất cả"
        const optionIndex = index + 1;
        if (categoryFilter.options[optionIndex]) {
          categoryFilter.options[optionIndex].text = category.name + ' (' + itemCount + ')';
        }
      });
    }
  }
}

// Show table guide
// ============================================================
// GUIDE MODAL FUNCTIONS
// ============================================================

function showTableGuide() {
  openGuideModal('tables');
}

function showMenuGuide() {
  openGuideModal('menu');
}

function openGuideModal(tab = 'tables') {
  const modal = document.getElementById('guideModal');
  if (modal) {
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
    
    // Switch to specified tab
    if (tab) {
      switchGuideTab(tab);
    }
  }
}

function closeGuideModal() {
  const modal = document.getElementById('guideModal');
  if (modal) {
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
  }
}

function switchGuideTab(tab) {
  // Update tab buttons
  document.querySelectorAll('.guide-tab').forEach(btn => {
    btn.classList.remove('active');
  });
  event?.target?.closest('.guide-tab')?.classList.add('active') || 
    document.querySelector(`.guide-tab[onclick*="${tab}"]`)?.classList.add('active');
  
  // Update tab panels
  document.querySelectorAll('.guide-panel').forEach(panel => {
    panel.classList.remove('active');
  });
  
  const panelId = tab + 'Guide';
  const panel = document.getElementById(panelId);
  if (panel) {
    panel.classList.add('active');
  }
}

// Close guide modal when clicking outside
window.addEventListener('click', function(event) {
  const modal = document.getElementById('guideModal');
  if (event.target === modal) {
    closeGuideModal();
  }
});

// Close guide modal with ESC key
document.addEventListener('keydown', function(event) {
  if (event.key === 'Escape') {
    const modal = document.getElementById('guideModal');
    if (modal && modal.style.display === 'block') {
      closeGuideModal();
    }
  }
});

// Setup event listeners
function setupEventListeners() {
  // Dropdown for status filter
  const statusFilter = document.getElementById('statusFilter');
  if (statusFilter) {
    statusFilter.addEventListener('change', function() {
      currentFilter = this.value;
      renderTables();
      updateFilterCounts();
    });
  }
  
  // Dropdown for room filter
  const roomFilterDropdown = document.getElementById('roomFilter');
  if (roomFilterDropdown) {
    roomFilterDropdown.addEventListener('change', function() {
      currentRoomFilter = this.value;
      renderTables();
      updateFilterCounts();
    });
  }
  
  // Dropdown for capacity filter
  const capacityFilter = document.getElementById('capacityFilter');
  if (capacityFilter) {
    capacityFilter.addEventListener('change', function() {
      currentCapacityFilter = this.value;
      renderTables();
      updateFilterCounts();
    });
  }
  
  // Category filter buttons - Handled in populateMenuCategories()
  // Event listeners are added when buttons are created
  
  // Table selection
  document.addEventListener('click', async function(e) {
    if (e.target.closest('.table-item')) {
      const tableItem = e.target.closest('.table-item');
      const tableId = tableItem.dataset.tableId;
      const tableStatus = tableItem.dataset.status;
      
      // ✅ Kiểm tra xem bàn này đã được chọn trong hóa đơn khác chưa
      // NHƯNG cho phép chọn nếu bàn có status Occupied trong database (đã được thông báo)
      const tableInOtherInvoice = invoices.find(inv => 
        inv.id !== currentInvoiceId && inv.table && inv.table.id === tableId
      );
      
      // Reload tables để kiểm tra database status
      await reloadTablesFromServer();
      const tableFromDB = tables.find(t => t.id === tableId);
      const isOccupiedInDB = tableFromDB && tableFromDB.status === 'Occupied';
      
      if (tableInOtherInvoice && !isOccupiedInDB) {
        // Bàn đang được sử dụng trong invoice khác VÀ chưa được thông báo (chưa có trong database)
        // → Không cho chọn
        window.notificationManager?.show(
          `Bàn này đang được sử dụng trong "${tableInOtherInvoice.name}"`,
          'warning',
          'Bàn đang sử dụng!'
        );
        return; // Ngăn chặn chọn bàn
      }
      
      // ✅ Nếu bàn có status Occupied trong database, cho phép chọn (có thể xem orders đã được thông báo)
      
      // ✅ Tìm bàn trong database HOẶC tạo object cho ô đặc biệt
      selectedTable = (window.tables || tables || []).find(t => t.id === tableId);
      
      // ✅ Nếu không tìm thấy (ô đặc biệt), tạo object tương ứng
      if (!selectedTable) {
        if (tableId === 'takeaway') {
          selectedTable = {
            id: 'takeaway',
            name: 'Mang về',
            room: 'Đặc biệt',
            capacity: 0,
            status: 'available',
            isTakeaway: true
          };
        } else if (tableId === 'delivery') {
          selectedTable = {
            id: 'delivery',
            name: 'Giao hàng',
            room: 'Đặc biệt',
            capacity: 0,
            status: 'available',
            isDelivery: true
          };
        }
      }
      
      if (selectedTable) {
        // ✅ Lưu bàn cũ TRƯỚC KHI cập nhật để kiểm tra orders
        const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
        const previousTable = currentInvoice?.table;
        const previousTableId = previousTable?.id;
        
        // Display table info with room name
        let tableInfo = selectedTable.name;
        if (selectedTable.room) {
          if (typeof selectedTable.room === 'string') {
            tableInfo += ` - ${selectedTable.room}`;
          } else if (selectedTable.room.name) {
            tableInfo += ` - ${selectedTable.room.name}`;
          }
        }
        document.getElementById('selectedTableInfo').textContent = tableInfo;
        
        // Lưu bàn vào invoice hiện tại
        if (currentInvoice) {
          // ✅ Gán bàn vào invoice
          currentInvoice.table = selectedTable;
          
          // ✅ Tạo tên tự động (fetch từ database)
          const newInvoiceName = await generateInvoiceName(selectedTable);
          currentInvoice.name = newInvoiceName;
          updateInvoiceTabName(currentInvoiceId, newInvoiceName);
          
          console.log('📝 Updated invoice name:', newInvoiceName);
        }
        
        // ✅ Kiểm tra database status và load orders nếu bàn có session active
        // (Tables đã được reload ở trên khi kiểm tra tableInOtherInvoice)
        // Lấy lại selectedTable với status mới nhất từ database
        const updatedTable = tables.find(t => t.id === tableId);
        if (updatedTable) {
          selectedTable.status = updatedTable.status;
        }
        
        // Load orders nếu bàn đang có khách (chỉ với bàn thường)
        // Kiểm tra database status
        const isOccupied = (updatedTable && updatedTable.status === 'Occupied') && 
                           !selectedTable.isTakeaway && !selectedTable.isDelivery;
        
        if (isOccupied) {
          // Bàn đang có khách - load orders từ database
          console.log('🔄 Table is occupied, loading orders from database...');
          await loadTableOrders(tableId);
        } else {
          // Bàn trống hoặc ô đặc biệt - xóa orders cũ (nếu có)
          // NHƯNG giữ lại orders nếu đang trong invoice memory VÀ chưa notify (chưa gửi bếp)
          // VÀ orders phải thuộc về bàn hiện tại (không phải từ bàn Occupied khác)
          
          // ✅ Kiểm tra xem orders trong invoice có phải là orders chưa notify không
          // Orders chưa notify = orders có quantity > notifiedQuantity (chưa gửi bếp)
          // Orders đã notify = orders có quantity === notifiedQuantity (đã gửi bếp, từ database)
          let hasUnnotifiedOrders = false;
          if (currentInvoice && currentInvoice.orders && currentInvoice.orders.length > 0) {
            // Kiểm tra xem có món nào chưa notify không
            hasUnnotifiedOrders = currentInvoice.orders.some(item => {
              const notifiedQty = item.notifiedQuantity || 0;
              const currentQty = item.quantity || 0;
              return currentQty > notifiedQty; // Có món chưa notify
            });
            
            // ✅ Kiểm tra: orders phải thuộc về bàn hiện tại (hoặc bàn trống/đặc biệt)
            // Nếu bàn cũ là Occupied và khác bàn mới, thì orders đó là từ database của bàn cũ → cần xóa
            const isOrdersFromPreviousOccupiedTable = previousTableId && 
              previousTableId !== tableId && 
              previousTableId !== 'takeaway' && 
              previousTableId !== 'delivery' &&
              tables.find(t => t.id === previousTableId)?.status === 'Occupied';
            
            if (hasUnnotifiedOrders && !isOrdersFromPreviousOccupiedTable) {
              // Có orders chưa notify VÀ không phải từ bàn Occupied khác - giữ lại
              console.log('✅ Keeping unnotified orders from invoice memory');
              orderItems = [...currentInvoice.orders];
              renderOrderItems();
              updateBill();
            } else {
              // Không có orders chưa notify HOẶC orders từ bàn Occupied khác - xóa
              console.log('🗑️ Clearing orders (unnotified:', hasUnnotifiedOrders, ', from previous occupied table:', isOrdersFromPreviousOccupiedTable, ')');
              orderItems = [];
              renderOrderItems();
              updateBill();
            }
          } else {
            // Không có orders - xóa
            orderItems = [];
            renderOrderItems();
            updateBill();
          }
          syncCurrentInvoice();
        }
        
        renderTables();
        updateFilterCounts(); // ✅ Cập nhật số lượng filter sau khi chọn bàn
        
        // ✅ Tự động chuyển sang tab Thực đơn nếu setting bật
        if (autoSwitchToMenu) {
          switchMainTab('menu');
        }
      }
    }
  });
  
  // Table filters
  document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.addEventListener('click', function() {
      document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
      this.classList.add('active');
      currentFilter = this.dataset.filter;
      renderTables();
    });
  });

  // Clear order
  document.getElementById('clearOrder').addEventListener('click', function() {
    if (confirm('Bạn có chắc muốn xóa toàn bộ đơn hàng?')) {
      orderItems = [];
      renderOrderItems();
      updateBill();
    }
  });
  
  // Order note button
  document.getElementById('orderNoteBtn').addEventListener('click', function() {
    openOrderNoteModal();
  });
  
  // VAT rate input - auto update bill
  const vatRateInput = document.getElementById('vatRate');
  if (vatRateInput) {
    vatRateInput.addEventListener('input', function() {
      updateBill();
      syncCurrentInvoice(); // Lưu VAT rate vào invoice
    });
  }
  
  // Notify kitchen button
  document.getElementById('notifyKitchenBtn').addEventListener('click', function() {
    notifyKitchen();
  });
  
  // Payment methods
  document.querySelectorAll('.payment-btn').forEach(btn => {
    btn.addEventListener('click', function() {
      document.querySelectorAll('.payment-btn').forEach(b => b.classList.remove('active'));
      this.classList.add('active');
    });
  });

  // Checkout
  document.getElementById('checkoutBtn').addEventListener('click', async function() {
    if (orderItems.length === 0) {
      window.notificationManager?.show('Vui lòng chọn ít nhất một món!', 'warning', 'Chưa có món');
      return;
    }

    if (!selectedTable) {
      window.notificationManager?.show('Vui lòng chọn bàn!', 'warning', 'Chưa chọn bàn');
      return;
    }

    // ✅ Lấy VAT rate từ input
    const vatRateInput = document.getElementById('vatRate');
    const vatRate = vatRateInput ? parseFloat(vatRateInput.value) || 0 : 10;
    
    const total = orderItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const vat = Math.round(total * (vatRate / 100));
    const finalTotal = total + vat;
    
    // Lấy payment method được chọn
    const selectedPaymentBtn = document.querySelector('.payment-btn.active');
    const paymentMethod = selectedPaymentBtn ? selectedPaymentBtn.dataset.method : 'cash';
    
    // ✅ Reload stock từ database trước khi thanh toán
    console.log('🔄 Reloading stock from database before checkout...');
    try {
      await reloadMenuStock();
    } catch (error) {
      console.error('❌ Error reloading stock:', error);
      window.notificationManager?.show(
        'Không thể kiểm tra tồn kho. Vui lòng thử lại!',
        'error',
        'Lỗi'
      );
      return;
    }
    
    // ✅ Kiểm tra lại stock sau khi reload
    const hasOutOfStock = orderItems.some(item => {
      const stock = item.stock || 0;
      return item.quantity > stock;
    });
    
    if (hasOutOfStock) {
      window.notificationManager?.show(
        'Một số món đã hết hàng hoặc không đủ số lượng. Vui lòng kiểm tra lại!',
        'error',
        'Không thể thanh toán'
      );
      renderOrderItems(); // Re-render để hiển thị stock mới
      updateBill(); // Cập nhật lại button state
      return;
    }
    
    // ✅ Xử lý thanh toán VNPay (cả VNPay và Transfer đều sử dụng VNPay)
    // VNPay hỗ trợ chuyển khoản ngân hàng, nên cả hai phương thức đều dùng VNPay
    if (paymentMethod === 'vnpay' || paymentMethod === 'transfer') {
      console.log('🔄 Processing VNPay payment for method:', paymentMethod);
      try {
        const paymentMethodName = paymentMethod === 'transfer' ? 'Chuyển khoản qua VNPay' : 'VNPay';
        window.notificationManager?.show(
          `Đang tạo đơn hàng và thanh toán ${paymentMethodName}...`,
          'info',
          paymentMethodName
        );
        
        // Tạo orders trước để có session và orders
        const orderItemsToSend = orderItems.map(item => ({
          variantId: item.variantId,
          quantity: item.quantity,
          unitPrice: item.price,
          note: item.note || ''
        }));
        
        // Lấy invoice name
        const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
        const invoiceName = currentInvoice?.name || 'Hóa đơn';
        
        // Tạo orders trước
        const createOrderResponse = await fetch(contextPath + '/api/cashier/order/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            tableId: selectedTable.id,
            items: orderItemsToSend,
            invoiceName: invoiceName
          })
        });
        
        const createOrderResult = await createOrderResponse.json();
        
        if (!createOrderResult.success) {
          console.error('❌ Create order failed:', createOrderResult);
          window.notificationManager?.show(
            createOrderResult.message || 'Không thể tạo đơn hàng',
            'error',
            'Lỗi'
          );
          return;
        }
        
        console.log('✅ Order created successfully:', createOrderResult);
        
        // Sau khi tạo orders thành công, tạo payment URL VNPay
        // Sử dụng sessionId từ create order response nếu có, nếu không thì dùng tableId
        const vnpayRequest = {
          amount: finalTotal,
          orderInfo: `Thanh toan don hang - ${invoiceName} - Ban ${selectedTable.name || selectedTable.id}`
        };
        
        // Ưu tiên sử dụng sessionId từ create order response
        if (createOrderResult.sessionId) {
          vnpayRequest.sessionId = createOrderResult.sessionId;
          console.log('✅ Using sessionId from create order:', createOrderResult.sessionId);
        } else {
          // Fallback: sử dụng tableId (VNPayService sẽ tìm hoặc tạo session)
          vnpayRequest.tableId = selectedTable.id;
          console.log('⚠️ No sessionId from create order, using tableId:', selectedTable.id);
        }
        
        console.log('📤 Sending VNPay request:', vnpayRequest);
        
        const vnpayResponse = await fetch(contextPath + '/api/payment/vnpay/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(vnpayRequest)
        });
        
        console.log('📥 VNPay response status:', vnpayResponse.status);
        
        if (!vnpayResponse.ok) {
          const errorText = await vnpayResponse.text();
          console.error('❌ VNPay API error:', errorText);
          window.notificationManager?.show(
            'Lỗi kết nối đến VNPay. Vui lòng thử lại.',
            'error',
            'Lỗi VNPay'
          );
          return;
        }
        
        const vnpayResult = await vnpayResponse.json();
        console.log('📥 VNPay result:', vnpayResult);
        
        if (vnpayResult.success && vnpayResult.paymentUrl) {
          // Lưu transactionId vào sessionStorage (optional, for tracking)
          if (vnpayResult.transactionId) {
            sessionStorage.setItem('vnpayTransactionId', vnpayResult.transactionId);
          }
          
          console.log('✅ Redirecting to VNPay:', vnpayResult.paymentUrl);
          
          // Redirect đến VNPay
          window.location.href = vnpayResult.paymentUrl;
        } else {
          console.error('❌ VNPay payment creation failed:', vnpayResult);
          window.notificationManager?.show(
            vnpayResult.message || 'Không thể tạo thanh toán VNPay',
            'error',
            'Lỗi VNPay'
          );
        }
      } catch (error) {
        console.error('❌ Error creating VNPay payment:', error);
        console.error('Error stack:', error.stack);
        window.notificationManager?.show(
          'Không thể tạo thanh toán VNPay. Vui lòng thử lại. Lỗi: ' + error.message,
          'error',
          'Lỗi kết nối'
        );
      }
      return; // Exit early for VNPay
    }
    
    // ✅ Gửi orderItems trực tiếp để backend trừ stock (giống cơ chế kitchen)
    // Không cần tạo orders trước, chỉ cần trừ stock
    const orderItemsToSend = orderItems.map(item => ({
      variantId: item.variantId,
      quantity: item.quantity
    }));
    
    // ✅ Bỏ confirm modal - thanh toán trực tiếp
    try {
      // Gọi API checkout với orderItems để trừ stock trực tiếp
      const response = await fetch(contextPath + '/api/cashier/checkout', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          tableId: selectedTable.id,
          paymentMethod: paymentMethod,
          totalAmount: finalTotal, // Gửi tổng tiền để backend lưu vào session
          orderItems: orderItemsToSend // ✅ Gửi orderItems để trừ stock trực tiếp
        })
      });
      
      const result = await response.json();
      
      if (result.success) {
        // Lấy tên hóa đơn hiện tại trước khi xóa
        const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
        const invoiceName = currentInvoice?.name || 'Hóa đơn';
        
        window.notificationManager?.show(
          `${invoiceName} | Tổng tiền: ${finalTotal.toLocaleString('vi-VN')}đ`,
          'success',
          'Thanh toán thành công!'
        );
        
        // ✅ Phát âm thanh thanh toán thành công
        playNotificationSound('success');
        
        // ✅ Làm mới lịch sử thông báo từ database
        refreshNotificationHistory();
        
        // ✅ Lưu thông tin trước khi xóa
        const paidInvoiceId = currentInvoiceId;
        const paidTableId = selectedTable?.id;
        
        console.log('💰 Payment successful for invoice:', paidInvoiceId, 'table:', paidTableId);
        
        // ✅ Xóa hóa đơn đã thanh toán khỏi danh sách
        invoices = invoices.filter(inv => inv.id !== paidInvoiceId);
        
        // ✅ Xóa tab hóa đơn đã thanh toán
        const paidTab = document.querySelector(`[data-invoice="${paidInvoiceId}"]`);
        if (paidTab) {
          paidTab.remove();
        }
        
        // ✅ Nếu không còn hóa đơn nào, tạo hóa đơn mới
        if (invoices.length === 0) {
          invoiceIdCounter++;
          const newInvoice = {
            id: invoiceIdCounter,
            orders: [],
            table: null,
            name: 'Hóa đơn 1'
          };
          invoices.push(newInvoice);
          
          const tabsContainer = document.getElementById('invoiceTabs');
          const newTab = document.createElement('button');
          newTab.className = 'invoice-tab active';
          newTab.dataset.invoice = newInvoice.id;
          newTab.innerHTML = '<span>' + newInvoice.name + '</span><i class="bx bx-x" onclick="closeInvoice(' + newInvoice.id + ', event)"></i>';
          tabsContainer.appendChild(newTab);
          
          currentInvoiceId = newInvoice.id;
        } else {
          // ✅ Chuyển sang hóa đơn đầu tiên
          switchInvoice(invoices[0].id);
        }
        
        // ✅ NOTE: Không cập nhật lại tên các hóa đơn còn lại vì counter không reset
        // Số thứ tự hóa đơn sẽ tăng dần (HD 1, HD 2, HD 3...) ngay cả sau khi thanh toán
        
        // Reset UI
        orderItems = [];
        selectedTable = null;
        document.getElementById('selectedTableInfo').textContent = 'Chưa chọn bàn';
        
        // Reset payment method selection
        document.querySelectorAll('.payment-btn').forEach(btn => btn.classList.remove('active'));
        
        renderOrderItems();
        updateBill();
        
        // ✅ Fetch lại data tables để cập nhật trạng thái
        try {
          const tablesResponse = await fetch(contextPath + '/cashier?action=getTables');
          if (tablesResponse.ok) {
            const tablesData = await tablesResponse.json();
            if (tablesData.success && tablesData.tables) {
              window.tables = tablesData.tables;
              tables = tablesData.tables;
              renderTables();
              console.log('✅ Tables data refreshed after payment');
            }
          }
        } catch (e) {
          console.warn('Could not refresh tables data:', e);
          // Fallback: render with current data
          renderTables();
        }
        
        console.log('✅ Invoice closed and counter updated. Remaining invoices:', invoices.length);
      } else {
        window.notificationManager?.show(result.message, 'error', 'Lỗi thanh toán');
      }
    } catch (error) {
      console.error('Error during checkout:', error);
      window.notificationManager?.show('Không thể thanh toán. Vui lòng thử lại.', 'error', 'Lỗi kết nối');
    }
  });
}

// ===== DISCOUNT FUNCTIONS =====
function applyPercentDiscount() {
  discountType = 'percent';
  openDiscountModal();
}

function applyAmountDiscount() {
  discountType = 'amount';
  openDiscountModal();
}

function openDiscountModal() {
  const modal = document.getElementById('discountModal');
  modal.style.display = 'block';
  
  // Set active tab
  document.querySelectorAll('.discount-tabs .tab').forEach(tab => {
    tab.classList.remove('active');
    if (tab.dataset.type === discountType) {
      tab.classList.add('active');
    }
  });
  
  // Update suffix
  const suffix = document.getElementById('discountSuffix');
  suffix.textContent = discountType === 'percent' ? '%' : 'đ';
  
  // Clear input
  document.getElementById('discountInput').value = '';
  updateDiscountPreview();
}

function closeDiscountModal() {
  document.getElementById('discountModal').style.display = 'none';
}

function updateDiscountPreview() {
  const input = document.getElementById('discountInput');
  const value = parseFloat(input.value) || 0;
  
  const subtotal = orderItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  
  let discountAmount = 0;
  if (discountType === 'percent') {
    discountAmount = Math.round(subtotal * value / 100);
  } else {
    discountAmount = value;
  }
  
  // ✅ Lấy VAT rate từ input
  const vatRateInput = document.getElementById('vatRate');
  const vatRate = vatRateInput ? parseFloat(vatRateInput.value) || 0 : 10;
  
  const afterDiscount = Math.max(0, subtotal - discountAmount);
  const vat = Math.round(afterDiscount * (vatRate / 100));
  const total = afterDiscount + vat;
  
  document.getElementById('previewSubtotal').textContent = subtotal.toLocaleString('vi-VN') + 'đ';
  document.getElementById('previewDiscount').textContent = '-' + discountAmount.toLocaleString('vi-VN') + 'đ';
  document.getElementById('previewTotal').textContent = total.toLocaleString('vi-VN') + 'đ';
}

function confirmDiscount() {
  const input = document.getElementById('discountInput');
  const value = parseFloat(input.value) || 0;
  
  if (value <= 0) {
    window.notificationManager?.show('Vui lòng nhập giá trị giảm giá hợp lệ!', 'error');
    return;
  }
  
  const subtotal = orderItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  
  if (discountType === 'percent' && value > 100) {
    window.notificationManager?.show('Giảm giá không được vượt quá 100%!', 'error');
    return;
  }
  
  if (discountType === 'amount' && value > subtotal) {
    window.notificationManager?.show('Giảm giá không được lớn hơn tổng tiền!', 'error');
    return;
  }
  
  currentDiscount = {
    type: discountType,
    value: value
  };
  
  // Calculate discount amount
  let discountName = '';
  
  if (discountType === 'percent') {
    discountName = 'Giảm ' + value + '%';
  } else {
    discountName = 'Giảm ' + value.toLocaleString('vi-VN') + 'đ';
  }
  
  // Update UI - show discount row in bill summary
  document.getElementById('discountRow').style.display = 'flex';
  
  updateBill();
  closeDiscountModal();
  
  window.notificationManager?.show(`Áp dụng ${discountName} thành công!`, 'success');
}

function removeDiscount() {
  currentDiscount = null;
  document.getElementById('discountRow').style.display = 'none';
  updateBill();
  closeDiscountModal();
  window.notificationManager?.show('Đã xóa giảm giá', 'info');
}

// ===== DISCOUNT PER ITEM =====
let currentItemDiscountVariantId = null;
let itemDiscountType = 'percent';

function openItemDiscountModal(variantId) {
  const item = orderItems.find(i => i.variantId === variantId);
  if (!item) return;
  
  currentItemDiscountVariantId = variantId;
  const modal = document.getElementById('itemDiscountModal');
  const input = document.getElementById('itemDiscountInput');
  
  // Set item name
  document.getElementById('itemDiscountName').textContent = item.name;
  
  // Load existing discount if any
  if (item.discount) {
    itemDiscountType = item.discount.type;
    input.value = item.discount.value;
    
    // Update active tab
    document.querySelectorAll('.item-discount-tab').forEach(tab => {
      if (tab.dataset.type === itemDiscountType) {
        tab.classList.add('active');
      } else {
        tab.classList.remove('active');
      }
    });
  } else {
    input.value = '';
    itemDiscountType = 'percent';
  }
  
  // Update suffix
  document.getElementById('itemDiscountSuffix').textContent = itemDiscountType === 'percent' ? '%' : 'đ';
  
  // Show modal
  modal.style.display = 'flex';
  
  // Add event listeners for tabs
  document.querySelectorAll('.item-discount-tab').forEach(tab => {
    tab.addEventListener('click', function() {
      document.querySelectorAll('.item-discount-tab').forEach(t => t.classList.remove('active'));
      this.classList.add('active');
      itemDiscountType = this.dataset.type;
      document.getElementById('itemDiscountSuffix').textContent = itemDiscountType === 'percent' ? '%' : 'đ';
      updateItemDiscountPreview();
    });
  });
  
  // Add input listener
  input.addEventListener('input', updateItemDiscountPreview);
  
  updateItemDiscountPreview();
}

function closeItemDiscountModal() {
  document.getElementById('itemDiscountModal').style.display = 'none';
  currentItemDiscountVariantId = null;
}

function updateItemDiscountPreview() {
  if (!currentItemDiscountVariantId) return;
  
  const item = orderItems.find(i => i.variantId === currentItemDiscountVariantId);
  if (!item) return;
  
  const input = document.getElementById('itemDiscountInput');
  const value = parseFloat(input.value) || 0;
  
  const unitPrice = item.price;
  const quantity = item.quantity;
  const itemSubtotal = unitPrice * quantity;
  
  let discountAmount = 0;
  if (itemDiscountType === 'percent') {
    discountAmount = Math.round(itemSubtotal * value / 100);
  } else {
    discountAmount = value;
  }
  
  const itemTotal = Math.max(0, itemSubtotal - discountAmount);
  
  document.getElementById('itemPreviewUnitPrice').textContent = unitPrice.toLocaleString('vi-VN') + 'đ';
  document.getElementById('itemPreviewQuantity').textContent = quantity;
  document.getElementById('itemPreviewSubtotal').textContent = itemSubtotal.toLocaleString('vi-VN') + 'đ';
  document.getElementById('itemPreviewDiscount').textContent = discountAmount.toLocaleString('vi-VN') + 'đ';
  document.getElementById('itemPreviewTotal').textContent = itemTotal.toLocaleString('vi-VN') + 'đ';
}

function confirmItemDiscount() {
  if (!currentItemDiscountVariantId) return;
  
  const item = orderItems.find(i => i.variantId === currentItemDiscountVariantId);
  if (!item) return;
  
  const input = document.getElementById('itemDiscountInput');
  const value = parseFloat(input.value) || 0;
  
  if (value <= 0) {
    window.notificationManager?.show('Vui lòng nhập giá trị giảm giá hợp lệ!', 'error');
    return;
  }
  
  const itemSubtotal = item.price * item.quantity;
  
  if (itemDiscountType === 'percent' && value > 100) {
    window.notificationManager?.show('Giảm giá không được vượt quá 100%!', 'error');
    return;
  }
  
  if (itemDiscountType === 'amount' && value > itemSubtotal) {
    window.notificationManager?.show('Giảm giá không được lớn hơn giá món!', 'error');
    return;
  }
  
  // Apply discount to item
  item.discount = {
    type: itemDiscountType,
    value: value
  };
  
  renderOrderItems();
  updateBill();
  syncCurrentInvoice();
  closeItemDiscountModal();
  
  window.notificationManager?.show(`Đã áp dụng giảm giá cho "${item.name}"`, 'success');
}

function removeItemDiscount() {
  if (!currentItemDiscountVariantId) return;
  
  const item = orderItems.find(i => i.variantId === currentItemDiscountVariantId);
  if (!item) return;
  
  delete item.discount;
  
  renderOrderItems();
  updateBill();
  syncCurrentInvoice();
  closeItemDiscountModal();
  
  window.notificationManager?.show(`Đã xóa giảm giá cho "${item.name}"`, 'info');
}

// ===== DISCOUNT TOGGLE =====

// ===== ORDER NOTE MANAGEMENT =====
let currentOrderNote = '';

function openOrderNoteModal() {
  const modal = document.getElementById('orderNoteModal');
  const input = document.getElementById('orderNoteInput');
  const preview = document.getElementById('orderNotePreview');
  const previewContent = document.getElementById('orderNotePreviewContent');
  const clearBtn = document.getElementById('clearNoteBtn');
  
  // Load existing note for current invoice
  const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
  if (currentInvoice && currentInvoice.note) {
    currentOrderNote = currentInvoice.note;
    input.value = currentOrderNote;
    preview.style.display = 'block';
    previewContent.textContent = currentOrderNote;
    clearBtn.style.display = 'inline-flex';
  } else {
    currentOrderNote = '';
    input.value = '';
    preview.style.display = 'none';
    clearBtn.style.display = 'none';
  }
  
  modal.style.display = 'block';
  setTimeout(() => input.focus(), 100);
}

function closeOrderNoteModal() {
  const modal = document.getElementById('orderNoteModal');
  modal.style.display = 'none';
}

function saveOrderNote() {
  const input = document.getElementById('orderNoteInput');
  const noteText = input.value.trim();
  
  if (!noteText) {
    window.notificationManager?.show('Vui lòng nhập nội dung ghi chú', 'warning', 'Thiếu nội dung');
    return;
  }
  
  // Save note to current invoice
  const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
  if (currentInvoice) {
    currentInvoice.note = noteText;
    currentOrderNote = noteText;
    
    window.notificationManager?.show(`Đã lưu ghi chú cho ${currentInvoice.name}`, 'success', 'Thành công');
    closeOrderNoteModal();
    
    // Update UI to show note indicator (optional - you could add a badge on invoice tab)
    console.log('Order note saved:', noteText);
  } else {
    window.notificationManager?.show('Vui lòng tạo hóa đơn trước khi thêm ghi chú', 'error', 'Lỗi');
  }
}

function clearOrderNote() {
  const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
  if (currentInvoice) {
    currentInvoice.note = '';
    currentOrderNote = '';
    
    const input = document.getElementById('orderNoteInput');
    const preview = document.getElementById('orderNotePreview');
    const clearBtn = document.getElementById('clearNoteBtn');
    
    input.value = '';
    preview.style.display = 'none';
    clearBtn.style.display = 'none';
    
    window.notificationManager?.show('Đã xóa ghi chú', 'info', 'Thông tin');
  }
}

// ===== SHIFT MANAGEMENT =====
let shiftData = {
  name: 'Ca Sáng',
  startTime: '08:00',
  endTime: '14:00',
  cashier: 'Admin',
  orders: [],
  totalOrders: 0,
  totalRevenue: 0,
  cashAmount: 0,
  cardAmount: 0,
  transferAmount: 0
};

function showShiftInfo() {
  alert('Ca làm việc: ' + shiftData.name + '\nThời gian: ' + shiftData.startTime + ' - ' + shiftData.endTime + '\nThu ngân: ' + shiftData.cashier);
}

function openEndShiftModal() {
  // Calculate shift summary
  calculateShiftSummary();
  
  const modal = document.getElementById('endShiftModal');
  modal.style.display = 'block';
  
  // Update modal content
  document.getElementById('shiftName').textContent = shiftData.name;
  document.getElementById('shiftTime').textContent = shiftData.startTime + ' - ' + shiftData.endTime;
  document.getElementById('cashierName').textContent = shiftData.cashier;
  
  document.getElementById('totalOrders').textContent = shiftData.totalOrders;
  document.getElementById('totalRevenue').textContent = shiftData.totalRevenue.toLocaleString('vi-VN') + 'đ';
  document.getElementById('cashAmount').textContent = shiftData.cashAmount.toLocaleString('vi-VN') + 'đ';
  document.getElementById('transferAmount').textContent = shiftData.transferAmount.toLocaleString('vi-VN') + 'đ';
  
  // Update payment details table
  updatePaymentDetailsTable();
}

function closeEndShiftModal() {
  document.getElementById('endShiftModal').style.display = 'none';
}

function calculateShiftSummary() {
  // Get shift data from localStorage or session
  const savedShiftData = localStorage.getItem('shiftData');
  if (savedShiftData) {
    const data = JSON.parse(savedShiftData);
    shiftData.orders = data.orders || [];
    shiftData.totalOrders = data.orders ? data.orders.length : 0;
    shiftData.totalRevenue = data.totalRevenue || 0;
    shiftData.cashAmount = data.cashAmount || 0;
    shiftData.cardAmount = data.cardAmount || 0;
    shiftData.transferAmount = data.transferAmount || 0;
  }
}

function updatePaymentDetailsTable() {
  const tbody = document.getElementById('paymentDetails');
  
  const cashOrders = shiftData.orders.filter(o => o.paymentMethod === 'cash').length;
  const cardOrders = shiftData.orders.filter(o => o.paymentMethod === 'card').length;
  const transferOrders = shiftData.orders.filter(o => o.paymentMethod === 'transfer').length;
  
  tbody.innerHTML = 
    '<tr>' +
      '<td>Tiền mặt</td>' +
      '<td>' + cashOrders + '</td>' +
      '<td>' + shiftData.cashAmount.toLocaleString('vi-VN') + 'đ</td>' +
    '</tr>' +
    '<tr>' +
      '<td>Thẻ</td>' +
      '<td>' + cardOrders + '</td>' +
      '<td>' + shiftData.cardAmount.toLocaleString('vi-VN') + 'đ</td>' +
    '</tr>' +
    '<tr>' +
      '<td>Chuyển khoản</td>' +
      '<td>' + transferOrders + '</td>' +
      '<td>' + shiftData.transferAmount.toLocaleString('vi-VN') + 'đ</td>' +
    '</tr>';
}

function printShiftReport() {
  alert('Chức năng in báo cáo đang được phát triển...');
  // TODO: Implement print functionality
}

function confirmEndShift() {
  if (confirm('Bạn có chắc muốn đóng ca? Dữ liệu ca sẽ được lưu lại.')) {
    // Save shift data
    localStorage.setItem('shiftDataHistory', JSON.stringify({
      ...shiftData,
      endedAt: new Date().toISOString()
    }));
    
    // Reset shift data
    localStorage.removeItem('shiftData');
    shiftData = {
      name: 'Ca Sáng',
      startTime: '08:00',
      endTime: '14:00',
      cashier: 'Admin',
      orders: [],
      totalOrders: 0,
      totalRevenue: 0,
      cashAmount: 0,
      cardAmount: 0,
      transferAmount: 0
    };
    
    alert('Đã đóng ca thành công!');
    closeEndShiftModal();
  }
}

// ===== NAVIGATION =====
function goBack() {
  const referrer = sessionStorage.getItem('cashier_referrer');
  
  if (referrer === 'roomtable') {
    window.location.href = contextPath + '/inventory/roomtable';
  } else if (referrer === 'products') {
    window.location.href = contextPath + '/inventory/productlist';
  } else {
    window.location.href = contextPath + '/dashboard';
  }
}

// ===== HEADER FUNCTIONS =====

// Switch main tab (Phòng bàn / Thực đơn)
function switchMainTab(tabName) {
  // Update header tab buttons
  document.querySelectorAll('.main-tab-btn').forEach(btn => {
    btn.classList.remove('active');
    if (btn.dataset.tab === tabName) {
      btn.classList.add('active');
    }
  });
  
  // Update tab panels
  document.querySelectorAll('.tab-panel').forEach(panel => {
    panel.classList.remove('active');
  });
  
  const targetPanel = document.getElementById(tabName + '-tab');
  if (targetPanel) {
    targetPanel.classList.add('active');
  }
}

// Invoice Management
let invoices = [{id: 1, orders: [], table: null, name: 'Hóa đơn 1'}];
let currentInvoiceId = 1;
let invoiceIdCounter = 1; // Bộ đếm ID tăng dần

// ✅ Sync orderItems với invoice hiện tại
function syncCurrentInvoice() {
  const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
  if (currentInvoice) {
    currentInvoice.orders = [...orderItems]; // Deep copy
    currentInvoice.discount = currentDiscount; // Lưu discount
    
    // ✅ Lưu VAT rate
    const vatRateInput = document.getElementById('vatRate');
    if (vatRateInput) {
      currentInvoice.vatRate = parseFloat(vatRateInput.value) || 10;
    }
    
    console.log('💾 Synced invoice', currentInvoiceId, '- Orders:', orderItems.length);
  }
}

// Đếm số hóa đơn đang mở của mỗi bàn (chưa thanh toán)
function getTableInvoiceCount(tableId) {
  if (!tableId) return 0;
  return invoices.filter(inv => inv.table && inv.table.id === tableId).length;
}

// Lấy danh sách hóa đơn của bàn (đã sắp xếp theo ID)
function getTableInvoices(tableId) {
  if (!tableId) return [];
  return invoices
    .filter(inv => inv.table && inv.table.id === tableId)
    .sort((a, b) => a.id - b.id);
}

// ✅ Fetch số thứ tự hóa đơn tiếp theo từ database
async function getNextInvoiceNumber(tableId) {
  try {
    const response = await fetch(contextPath + '/api/cashier/invoice/next-number?tableId=' + tableId);
    const result = await response.json();
    
    if (result.success) {
      return result.nextNumber;
    } else {
      console.error('Failed to get next invoice number:', result.message);
      return 1; // Fallback
    }
  } catch (error) {
    console.error('Error fetching next invoice number:', error);
    return 1; // Fallback
  }
}

// Tạo tên hóa đơn tự động (async để fetch từ database)
async function generateInvoiceName(table, invoiceIndex = null) {
  if (!table) {
    return 'Hóa đơn ' + invoices.length;
  }
  
  // Nếu có invoiceIndex (khi cập nhật lại), dùng nó
  if (invoiceIndex !== null) {
    return table.name + ' - HD ' + invoiceIndex;
  }
  
  // ✅ Với bàn đặc biệt (Mang về, Giao hàng), đếm local
  if (table.isTakeaway || table.isDelivery) {
    const existingInvoices = invoices.filter(inv => 
      inv.table && inv.table.id === table.id
    ).length;
    return table.name + ' - HD ' + (existingInvoices + 1);
  }
  
  // ✅ Lấy số thứ tự tiếp theo từ DATABASE (chỉ với bàn thường)
  const invoiceNumber = await getNextInvoiceNumber(table.id);
  
  return table.name + ' - HD ' + invoiceNumber;
}

// Update tên invoice tab
function updateInvoiceTabName(invoiceId, name) {
  // ✅ Re-render tabs để cập nhật tên
  renderInvoiceTabs();
}

// Cập nhật lại tên tất cả hóa đơn của cùng bàn (sau khi xóa)
function updateTableInvoiceNames(tableId) {
  if (!tableId) return;
  
  const tableInvoices = getTableInvoices(tableId);
  tableInvoices.forEach((invoice, index) => {
    const newName = generateInvoiceName(invoice.table, index + 1);
    invoice.name = newName;
    updateInvoiceTabName(invoice.id, newName);
  });
  
  console.log(`✅ Updated ${tableInvoices.length} invoice names for table ${tableId}`);
}

// ✅ Render invoice tabs (hiển thị tối đa 3, còn lại vào dropdown)
function renderInvoiceTabs() {
  const tabsContainer = document.getElementById('invoiceTabs');
  if (!tabsContainer) return;
  
  let html = '';
  const MAX_VISIBLE_TABS = 3;
  
  if (invoices.length <= MAX_VISIBLE_TABS) {
    // Hiển thị tất cả tabs
    invoices.forEach(invoice => {
      const isActive = invoice.id === currentInvoiceId ? 'active' : '';
      html += `
        <button class="invoice-tab ${isActive}" data-invoice="${invoice.id}">
          <span>${invoice.name}</span>
          <i class="bx bx-x" onclick="closeInvoice(${invoice.id}, event)"></i>
        </button>
      `;
    });
  } else {
    // Hiển thị 3 tabs đầu
    for (let i = 0; i < MAX_VISIBLE_TABS; i++) {
      const invoice = invoices[i];
      const isActive = invoice.id === currentInvoiceId ? 'active' : '';
      html += `
        <button class="invoice-tab ${isActive}" data-invoice="${invoice.id}">
          <span>${invoice.name}</span>
          <i class="bx bx-x" onclick="closeInvoice(${invoice.id}, event)"></i>
        </button>
      `;
    }
    
    // Nút "Thêm" với dropdown
    const remainingCount = invoices.length - MAX_VISIBLE_TABS;
    html += `
      <button class="more-invoices-btn" onclick="toggleInvoiceDropdown(event)">
        <span>+${remainingCount}</span>
        <i class='bx bx-chevron-down'></i>
      </button>
      <div class="invoice-dropdown" id="invoiceDropdown">
    `;
    
    // Các invoice còn lại trong dropdown
    for (let i = MAX_VISIBLE_TABS; i < invoices.length; i++) {
      const invoice = invoices[i];
      const isActive = invoice.id === currentInvoiceId ? 'active' : '';
      html += `
        <button class="invoice-dropdown-item ${isActive}" onclick="switchInvoice(${invoice.id})">
          <span>${invoice.name}</span>
          <i class="bx bx-x" onclick="closeInvoice(${invoice.id}, event)"></i>
        </button>
      `;
    }
    
    html += '</div>';
  }
  
  tabsContainer.innerHTML = html;
}

// ✅ Toggle dropdown
function toggleInvoiceDropdown(event) {
  event.stopPropagation();
  const dropdown = document.getElementById('invoiceDropdown');
  if (dropdown) {
    dropdown.classList.toggle('show');
  }
}

// ✅ Close dropdown khi click outside
document.addEventListener('click', function() {
  const dropdown = document.getElementById('invoiceDropdown');
  if (dropdown) {
    dropdown.classList.remove('show');
  }
});

function addNewInvoice() {
  invoiceIdCounter++; // Tăng bộ đếm
  const newId = invoiceIdCounter;
  const newInvoice = {
    id: newId,
    orders: [],
    table: null,
    name: 'Hóa đơn ' + (invoices.length + 1) // Tên tạm, sẽ đổi khi chọn bàn
  };
  
  invoices.push(newInvoice);
  renderInvoiceTabs(); // Re-render tabs
  switchInvoice(newId);
  renderTables(); // ✅ Re-render tables để cập nhật trạng thái
  updateFilterCounts(); // ✅ Cập nhật số lượng filter
  
  console.log('➕ Created new invoice:', newId);
}

async function switchInvoice(invoiceId) {
  currentInvoiceId = invoiceId;
  
  // ✅ Re-render tabs to update active state
  renderInvoiceTabs();
  
  // Load invoice data
  const invoice = invoices.find(inv => inv.id === invoiceId);
  if (invoice) {
    selectedTable = invoice.table;
    
    // ✅ Nếu invoice có table và table có status Occupied trong database, load orders từ database
    if (selectedTable && selectedTable.id && !selectedTable.isTakeaway && !selectedTable.isDelivery) {
      // Reload tables để có status mới nhất
      await reloadTablesFromServer();
      
      // Kiểm tra database status
      const updatedTable = tables.find(t => t.id === selectedTable.id);
      if (updatedTable && updatedTable.status === 'Occupied') {
        // Bàn có session active trong database - load orders từ database
        console.log('🔄 Invoice has occupied table, loading orders from database...');
        await loadTableOrders(selectedTable.id);
      } else {
        // Bàn không có session active - load từ invoice memory
        orderItems = invoice.orders || [];
        renderOrderItems();
        updateBill();
      }
    } else {
      // Không có table hoặc là bàn đặc biệt - load từ invoice memory
      orderItems = invoice.orders || [];
      renderOrderItems();
      updateBill();
    }
    
    // ✅ Restore discount
    currentDiscount = invoice.discount || null;
    if (currentDiscount) {
      document.getElementById('discountRow').style.display = 'flex';
    } else {
      document.getElementById('discountRow').style.display = 'none';
    }
    
    // ✅ Restore VAT rate
    const vatRateInput = document.getElementById('vatRate');
    if (vatRateInput && invoice.vatRate !== undefined) {
      vatRateInput.value = invoice.vatRate;
    } else if (vatRateInput) {
      vatRateInput.value = 10; // Default
    }
    
    // ✅ Update table info display
    const tableInfoElement = document.getElementById('selectedTableInfo');
    if (tableInfoElement) {
      if (selectedTable) {
        let tableInfo = selectedTable.name;
        if (selectedTable.room) {
          if (typeof selectedTable.room === 'string') {
            tableInfo += ` - ${selectedTable.room}`;
          } else if (selectedTable.room.name) {
            tableInfo += ` - ${selectedTable.room.name}`;
          }
        }
        tableInfoElement.textContent = tableInfo;
      } else {
        tableInfoElement.textContent = 'Chưa chọn bàn';
      }
    }
    
    // ✅ Re-render tables để cập nhật trạng thái selected
    renderTables();
    
    renderOrderItems();
    updateBill();
    
    console.log('📋 Switched to invoice:', invoiceId, '- Table:', selectedTable?.name, '- Orders:', orderItems.length);
  }
}

async function closeInvoice(invoiceId, event) {
  if (event) {
    event.stopPropagation();
  }
  
  if (invoices.length === 1) {
    window.notificationManager?.show('Phải có ít nhất 1 hóa đơn!', 'warning', 'Không thể đóng');
    return;
  }
  
  // Lấy thông tin hóa đơn trước khi xóa
  const invoiceToClose = invoices.find(inv => inv.id === invoiceId);
  const invoiceName = invoiceToClose?.name || 'hóa đơn này';
  const tableId = invoiceToClose?.table?.id;
  
  // ✅ Show custom confirm modal
  const confirmed = await showConfirmModal(
    `Bạn có chắc muốn đóng "${invoiceName}"?`,
    'Đóng hóa đơn'
  );
  
  if (confirmed) {
    // Xóa hóa đơn
    invoices = invoices.filter(inv => inv.id !== invoiceId);
    
    // Chuyển sang hóa đơn khác nếu đang ở hóa đơn bị xóa
    if (currentInvoiceId === invoiceId) {
      switchInvoice(invoices[0].id);
    }
    
    // ✅ Re-render tabs
    renderInvoiceTabs();
    renderTables(); // ✅ Re-render tables để cập nhật trạng thái
    updateFilterCounts(); // ✅ Cập nhật số lượng filter
    
    // ✅ NOTE: Không cập nhật lại tên vì counter không reset sau khi đóng hóa đơn
    
    // ✅ Show success notification
    window.notificationManager?.show(
      `Đã đóng "${invoiceName}"`,
      'success',
      'Hoàn tất'
    );
    
    console.log('🗑️ Closed invoice:', invoiceId, '- Remaining:', invoices.length);
  }
}

// Sound Toggle
let soundEnabled = true;

// Khởi tạo AudioContext cho âm thanh
let audioContext = null;

// Hàm phát âm thanh thông báo
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

function toggleSound() {
  soundEnabled = !soundEnabled;
  const icon = document.querySelector('#soundToggle i');
  
  if (soundEnabled) {
    icon.className = 'bx bx-volume-full';
    // Phát âm thanh test khi bật
    playNotificationSound('success');
  } else {
    icon.className = 'bx bx-volume-mute';
  }
  
  localStorage.setItem('cashier_sound_enabled', soundEnabled);
}

// ✅ Khởi tạo sound state từ localStorage
function initSoundState() {
  const savedState = localStorage.getItem('cashier_sound_enabled');
  if (savedState !== null) {
    soundEnabled = savedState === 'true';
    const icon = document.querySelector('#soundToggle i');
    if (icon) {
      icon.className = soundEnabled ? 'bx bx-volume-full' : 'bx bx-volume-mute';
    }
  }
}

// ✅ Khởi tạo button auto-switch
function initAutoSwitchButton() {
  const btn = document.getElementById('autoSwitchMenuBtn');
  if (!btn) return;
  
  const icon = btn.querySelector('i');
  const text = btn.querySelector('.btn-text');
  
  // Đọc từ localStorage
  const savedState = localStorage.getItem('cashier_auto_switch_menu');
  autoSwitchToMenu = savedState === 'true';
  
  // Update UI
  if (autoSwitchToMenu) {
    btn.classList.add('active');
    icon.className = 'bx bx-check-circle';
    if (text) text.textContent = 'Bật';
  } else {
    btn.classList.remove('active');
    icon.className = 'bx bx-circle';
    if (text) text.textContent = 'Tắt';
  }
}

// ✅ Toggle auto switch to menu
function toggleAutoSwitchMenu() {
  autoSwitchToMenu = !autoSwitchToMenu;
  const btn = document.getElementById('autoSwitchMenuBtn');
  const icon = btn.querySelector('i');
  const text = btn.querySelector('.btn-text');
  
  if (autoSwitchToMenu) {
    btn.classList.add('active');
    icon.className = 'bx bx-check-circle';
    if (text) text.textContent = 'Bật';
  } else {
    btn.classList.remove('active');
    icon.className = 'bx bx-circle';
    if (text) text.textContent = 'Tắt';
  }
  
  localStorage.setItem('cashier_auto_switch_menu', autoSwitchToMenu);
  
  // Hiển thị thông báo
  if (autoSwitchToMenu) {
    window.notificationManager.show(
      'Khi chọn bàn, hệ thống sẽ tự động chuyển sang tab Thực đơn',
      'success',
      'Đã bật tự động mở menu'
    );
  } else {
    window.notificationManager.show(
      'Khi chọn bàn, hệ thống sẽ giữ nguyên tab hiện tại',
      'info',
      'Đã tắt tự động mở menu'
    );
  }
}

// ============================================================
// NOTIFICATION SYSTEM (from roomtable)
// ============================================================

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

// Notifications
// ✅ Lịch sử thông báo bếp và thanh toán (từ database)
let notificationHistory = [];

// Load lịch sử từ database
async function loadNotificationHistory() {
  try {
    const response = await fetch(contextPath + '/api/cashier/notification/history?days=7');
    const result = await response.json();
    
    if (result.success) {
      notificationHistory = result.notifications;
      updateNotificationBadge();
      console.log('✅ Loaded notification history from DB:', result.total);
    } else {
      console.warn('Failed to load notification history:', result.message);
      notificationHistory = [];
    }
  } catch (e) {
    console.warn('Failed to load notification history:', e);
    notificationHistory = [];
  }
}

// Update badge (hiển thị số thông báo)
function updateNotificationBadge() {
  const badge = document.getElementById('notificationCount');
  if (badge) {
    const count = notificationHistory.length;
    if (count > 0) {
      badge.textContent = count > 99 ? '99+' : count;
      badge.style.display = 'block';
    } else {
      badge.style.display = 'none';
    }
  }
}

function toggleNotifications() {
  const panel = document.getElementById('notificationPanel');
  const isVisible = panel.style.display === 'block';
  
  if (isVisible) {
    panel.style.display = 'none';
  } else {
    panel.style.display = 'block';
    loadNotifications();
  }
}

function loadNotifications() {
  const list = document.getElementById('notificationList');
  
  if (notificationHistory.length === 0) {
    list.innerHTML = '<div class="notification-empty"><i class="bx bx-bell-off"></i><p>Chưa có lịch sử (7 ngày gần nhất)</p></div>';
    return;
  }
  
  // Group by date
  const grouped = {};
  notificationHistory.forEach(notif => {
    // Parse timestamp từ database (format: yyyy-MM-dd HH:mm:ss)
    const timestamp = new Date(notif.timestamp.replace(' ', 'T'));
    const date = timestamp.toLocaleDateString('vi-VN');
    const time = timestamp.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    
    if (!grouped[date]) {
      grouped[date] = [];
    }
    grouped[date].push({ ...notif, displayTime: time });
  });
  
  let html = '';
  
  Object.keys(grouped).forEach(date => {
    html += `<div class="notification-date-group">`;
    html += `<div class="notification-date-header">${date}</div>`;
    
    grouped[date].forEach(notif => {
      const icon = notif.type === 'kitchen' ? 'bx-restaurant' : 'bx-receipt';
      const color = notif.type === 'kitchen' ? '#ff9800' : '#4caf50';
      const title = notif.type === 'kitchen' ? 'Thông báo bếp' : 'Thanh toán';
      
      let content = '';
      if (notif.type === 'kitchen') {
        // ✅ Hiển thị chi tiết thông báo bếp
        const orderNumber = notif.orderId || 'N/A'; // orderId đã là orderNumber từ backend
        const invoiceName = notif.invoiceName || notif.tableName; // Ưu tiên invoice name
        
        let itemsHtml = '';
        if (notif.items && notif.items.length > 0) {
          itemsHtml = '<div class="notif-items-list">';
          notif.items.forEach(item => {
            const itemName = item.name + (item.size ? ' (' + item.size + ')' : '');
            itemsHtml += `<div class="notif-item-row">• ${itemName} x${item.quantity}</div>`;
          });
          itemsHtml += '</div>';
        }
        
        // Hiển thị ghi chú nếu có
        const noteHtml = notif.orderNote ? `<div class="notif-detail notif-note"><strong>📝 Ghi chú:</strong> ${notif.orderNote}</div>` : '';
        
        content = `<div class="notif-detail"><strong>Hóa đơn:</strong> ${invoiceName}</div>` +
                 `<div class="notif-detail"><strong>Số món:</strong> ${notif.itemCount}</div>` +
                 itemsHtml +
                 noteHtml +
                 `<div class="notif-detail notif-order-id"><strong>Đơn hàng:</strong> #${orderNumber}</div>`;
      } else {
        // ✅ Hiển thị chi tiết thanh toán
        const invoiceName = notif.invoiceName || notif.tableName; // Ưu tiên invoice name
        const paymentMethodText = notif.paymentMethod === 'cash' ? 'Tiền mặt' : 
                                 notif.paymentMethod === 'card' ? 'Thẻ' : 'Chuyển khoản';
        const hasVoucher = notif.hasVoucher || false;
        const voucherText = hasVoucher ? `<div class="notif-detail notif-voucher">✨ Có áp dụng Voucher (-${notif.discount.toLocaleString('vi-VN')}đ)</div>` : '';
        
        content = `<div class="notif-detail"><strong>Hóa đơn:</strong> ${invoiceName}</div>` +
                 `<div class="notif-detail"><strong>Số món:</strong> ${notif.itemCount}</div>` +
                 `<div class="notif-detail"><strong>Tổng tiền:</strong> ${notif.amount.toLocaleString('vi-VN')}đ</div>` +
                 voucherText +
                 `<div class="notif-detail notif-final-amount"><strong>Thanh toán:</strong> ${notif.finalAmount.toLocaleString('vi-VN')}đ (${paymentMethodText})</div>`;
      }
      
      html += `
        <div class="notification-item" data-type="${notif.type}">
          <div class="notif-icon" style="background: ${color}20; color: ${color};">
            <i class='bx ${icon}'></i>
          </div>
          <div class="notif-content">
            <div class="notif-title">${title}</div>
            ${content}
            <div class="notif-time">${notif.displayTime}</div>
          </div>
        </div>
      `;
    });
    
    html += `</div>`;
  });
  
  // Thêm nút làm mới
  html += `
    <div class="notification-footer">
      <button class="refresh-history-btn" onclick="refreshNotificationHistory()">
        <i class='bx bx-refresh'></i> Làm mới
      </button>
    </div>
  `;
  
  list.innerHTML = html;
}

// Làm mới lịch sử từ database
async function refreshNotificationHistory() {
  await loadNotificationHistory();
  loadNotifications();
  window.notificationManager?.show('Đã làm mới lịch sử thông báo', 'success', 'Thành công');
}

// ✅ Reload stock từ database
async function reloadMenuStock() {
  try {
    console.log('🔄 Fetching latest stock from database...');
    
    const response = await fetch(contextPath + '/cashier');
    if (!response.ok) {
      throw new Error('Failed to reload menu');
    }
    
    const html = await response.text();
    
    // Parse HTML để lấy menuItems mới
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    const scriptTag = doc.querySelector('script:not([src])');
    
    if (scriptTag) {
      const scriptContent = scriptTag.textContent;
      
      // Extract menuItems từ script
      const menuMatch = scriptContent.match(/menuItems\s*=\s*(\[[\s\S]*?\]);/);
      if (menuMatch) {
        const newMenuItems = JSON.parse(menuMatch[1]);
        
        // ✅ Cập nhật menuItems global
        menuItems = newMenuItems;
        
        // ✅ Cập nhật stock cho các món đang có trong giỏ hàng
        orderItems.forEach(orderItem => {
          const menuItem = menuItems.find(m => m.variantId === orderItem.variantId);
          if (menuItem) {
            orderItem.stock = menuItem.stock || 0;
            orderItem.outOfStock = orderItem.quantity > (menuItem.stock || 0);
            console.log(`📦 Updated ${orderItem.name}: stock=${menuItem.stock}, quantity=${orderItem.quantity}, outOfStock=${orderItem.outOfStock}`);
          }
        });
        
        // ✅ Re-render menu với stock mới
        renderMenu();
        
        console.log('✅ Stock reloaded successfully');
        console.log('📦 Updated stock summary:');
        menuItems.forEach(item => {
          const stock = item.stock || 0;
          const status = stock > 0 ? '✅' : '❌';
          console.log(`  ${status} ${item.name} (${item.size || 'N/A'}): ${stock}`);
        });
      }
    }
  } catch (error) {
    console.error('❌ Error reloading stock:', error);
    throw error; // Re-throw để checkout handler có thể xử lý
  }
}

// User Menu
function toggleUserMenu() {
  const dropdown = document.getElementById('userDropdown');
  const isVisible = dropdown.style.display === 'block';
  
  if (isVisible) {
    dropdown.style.display = 'none';
    dropdown.classList.remove('show');
  } else {
    dropdown.style.display = 'block';
    setTimeout(() => dropdown.classList.add('show'), 10);
  }
}

function navigate(page) {
  // ✅ Đặc biệt: Mở modal báo cáo cuối ngày thay vì chuyển trang
  if (page === 'end-of-day-report') {
    openEndOfDayModal();
    toggleUserMenu();
    return;
  }
  
  const routes = {
    'management': '/dashboard',
    'kitchen': '/kitchen',
    'reception': '/reception'
  };
  
  const path = routes[page] || '/dashboard';
  window.location.href = contextPath + path;
  toggleUserMenu();
}

function logout() {
  openLogoutModal();
}

function openLogoutModal() {
  const modal = document.getElementById('logoutModal');
  modal.style.display = 'flex';
  setTimeout(() => modal.classList.add('show'), 10);
}

function closeLogoutModal() {
  const modal = document.getElementById('logoutModal');
  modal.classList.remove('show');
  setTimeout(() => {
    modal.style.display = 'none';
  }, 300);
}

function confirmLogout() {
  window.location.href = contextPath + '/logout';
}

// Close dropdowns when clicking outside
document.addEventListener('click', function(event) {
  const userDropdown = document.getElementById('userDropdown');
  const userMenuBtn = document.querySelector('.user-menu-btn');
  const notificationPanel = document.getElementById('notificationPanel');
  const notificationBtn = document.querySelector('.notification-btn');
  
  if (userDropdown && !userMenuBtn.contains(event.target) && !userDropdown.contains(event.target)) {
    userDropdown.style.display = 'none';
    userDropdown.classList.remove('show');
  }
  
  if (notificationPanel && !notificationBtn.contains(event.target) && !notificationPanel.contains(event.target)) {
    notificationPanel.style.display = 'none';
  }
});

// Header search
document.getElementById('headerSearch').addEventListener('input', function(e) {
  const searchTerm = e.target.value.toLowerCase();
  renderMenu();
  
  // Auto switch to menu tab if searching
  if (searchTerm.length > 0) {
    switchMainTab('menu');
  }
});

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
  // F3 - Focus search
  if (e.key === 'F3') {
    e.preventDefault();
    document.getElementById('headerSearch').focus();
  }
});

// Close modal when clicking outside
window.onclick = function(event) {
  const discountModal = document.getElementById('discountModal');
  const shiftModal = document.getElementById('endShiftModal');
  
  if (event.target === discountModal) {
    closeDiscountModal();
  }
  if (event.target === shiftModal) {
    closeEndShiftModal();
  }
}

// ===== CUSTOM CONFIRM MODAL =====
function showConfirmModal(message, title = 'Xác nhận') {
  return new Promise((resolve) => {
    const modal = document.getElementById('confirmModal');
    const titleElement = document.getElementById('confirmModalTitle');
    const messageElement = document.getElementById('confirmModalMessage');
    const okBtn = document.getElementById('confirmOkBtn');
    const cancelBtn = document.getElementById('confirmCancelBtn');
    
    // Set content
    titleElement.textContent = title;
    messageElement.textContent = message;
    
    // Show modal
    modal.style.display = 'block';
    
    // Handle OK
    const handleOk = () => {
      cleanup();
      resolve(true);
    };
    
    // Handle Cancel
    const handleCancel = () => {
      cleanup();
      resolve(false);
    };
    
    // Cleanup function
    const cleanup = () => {
      modal.style.display = 'none';
      okBtn.removeEventListener('click', handleOk);
      cancelBtn.removeEventListener('click', handleCancel);
      modal.removeEventListener('click', handleModalClick);
    };
    
    // Close when click outside
    const handleModalClick = (e) => {
      if (e.target === modal) {
        handleCancel();
      }
    };
    
    // Add event listeners
    okBtn.addEventListener('click', handleOk);
    cancelBtn.addEventListener('click', handleCancel);
    modal.addEventListener('click', handleModalClick);
  });
}

// =============================================
// END OF DAY REPORT
// =============================================

function openEndOfDayModal() {
  const modal = document.getElementById('endOfDayModal');
  modal.style.display = 'flex';
  setTimeout(() => modal.classList.add('show'), 10);
  
  // Auto preview when modal opens
  setTimeout(() => previewReport(), 300);
}

function closeEndOfDayModal() {
  const modal = document.getElementById('endOfDayModal');
  modal.classList.remove('show');
  setTimeout(() => {
    modal.style.display = 'none';
    // Reset form
    document.getElementById('reportSummary').style.display = 'none';
  }, 300);
}

async function previewReport() {
  const reportDate = document.getElementById('reportDate').value;
  const reportType = document.getElementById('reportType').value;
  
  if (!reportDate) {
    window.notificationManager?.show('Vui lòng chọn ngày báo cáo', 'error');
    return;
  }
  
  try {
    console.log('📊 Fetching report data for:', reportDate, reportType);
    
    const response = await fetch(`${contextPath}/api/reports/daily-summary?date=${reportDate}&type=${reportType}`);
    
    if (!response.ok) {
      throw new Error('Không thể tải dữ liệu báo cáo');
    }
    
    const data = await response.json();
    console.log('📊 Report data:', data);
    
    // Display summary - Format currency properly
    document.getElementById('totalInvoices').textContent = data.totalInvoices || 0;
    document.getElementById('totalRevenue').textContent = (data.totalRevenue || 0).toLocaleString('vi-VN') + 'đ';
    document.getElementById('totalItems').textContent = data.totalItems || 0;
    
    document.getElementById('reportSummary').style.display = 'block';
    
  } catch (error) {
    console.error('❌ Error loading report:', error);
    window.notificationManager?.show('Lỗi khi tải dữ liệu báo cáo', 'error');
  }
}

async function exportReport() {
  const reportDate = document.getElementById('reportDate').value;
  const reportType = document.getElementById('reportType').value;
  
  if (!reportDate) {
    window.notificationManager?.show('Vui lòng chọn ngày báo cáo', 'error');
    return;
  }
  
  try {
    window.notificationManager?.show('Đang xuất báo cáo PDF...', 'info');
    
    // Open in new tab to trigger PDF download/print
    const url = `${contextPath}/api/reports/daily-export?date=${reportDate}&format=pdf&type=${reportType}`;
    window.open(url, '_blank');
    
    // Close modal and show success
    setTimeout(() => {
      closeEndOfDayModal();
      window.notificationManager?.show('Đã mở báo cáo PDF!', 'success');
    }, 500);
    
  } catch (error) {
    console.error('❌ Error exporting report:', error);
    window.notificationManager?.show('Lỗi khi xuất báo cáo', 'error');
  }
}

// Listen for changes to auto-update preview
document.addEventListener('DOMContentLoaded', function() {
  const reportDateInput = document.getElementById('reportDate');
  const reportTypeSelect = document.getElementById('reportType');
  
  if (reportDateInput) {
    reportDateInput.addEventListener('change', previewReport);
  }
  
  if (reportTypeSelect) {
    reportTypeSelect.addEventListener('change', previewReport);
  }
});

// ========================================
// PRINT TEMPORARY BILL FUNCTION
// ========================================

/**
 * Print temporary bill for current table order
 */
function printTemporaryBill() {
  // Check if there are items and a selected table
  if (!selectedTable || orderItems.length === 0) {
    alert('Vui lòng chọn bàn và thêm món vào đơn hàng trước khi in bill!');
    return;
  }

  // Calculate bill totals (same logic as updateBill)
  let subtotal = 0;
  let totalItemDiscounts = 0;
  
  orderItems.forEach(item => {
    const itemSubtotal = item.price * item.quantity;
    let itemDiscountAmount = 0;
    
    if (item.discount) {
      if (item.discount.type === 'percent') {
        itemDiscountAmount = Math.round(itemSubtotal * item.discount.value / 100);
      } else {
        itemDiscountAmount = item.discount.value;
      }
      totalItemDiscounts += itemDiscountAmount;
    }
    
    subtotal += Math.max(0, itemSubtotal - itemDiscountAmount);
  });
  
  // Calculate order discount
  let orderDiscountAmount = 0;
  if (currentDiscount) {
    if (currentDiscount.type === 'percent') {
      orderDiscountAmount = Math.round(subtotal * currentDiscount.value / 100);
    } else {
      orderDiscountAmount = currentDiscount.value;
    }
  }
  
  // Get VAT rate
  const vatRateInput = document.getElementById('vatRate');
  const vatRate = vatRateInput ? parseFloat(vatRateInput.value) || 0 : 10;
  
  const afterDiscount = Math.max(0, subtotal - orderDiscountAmount);
  const vat = Math.round(afterDiscount * (vatRate / 100));
  const total = afterDiscount + vat;
  
  // Get table name
  const tableName = selectedTable.name || 'Chưa xác định';
  const tableRoom = selectedTable.room ? (typeof selectedTable.room === 'string' ? selectedTable.room : selectedTable.room.name) : '';
  
  // Get current invoice name
  const currentInvoice = invoices.find(inv => inv.id === currentInvoiceId);
  const invoiceName = currentInvoice?.name || 'Hóa đơn';
  
  // Get order note
  const orderNote = currentInvoice?.note || '';
  
  // Get current date and time
  const now = new Date();
  const dateStr = now.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
  const timeStr = now.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  });
  
  // Create print window
  const printWindow = window.open('', '_blank');
  if (!printWindow) {
    alert('Không thể mở cửa sổ in. Vui lòng cho phép popup và thử lại.');
    return;
  }
  
  // Build bill HTML
  const billHTML = `
    <!DOCTYPE html>
    <html lang="vi">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>Bill Tạm Tính - ${tableName}</title>
      <style>
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }
        
        body {
          font-family: 'Arial', 'Helvetica', sans-serif;
          padding: 20px;
          background: white;
          color: #333;
        }
        
        .bill-container {
          max-width: 400px;
          margin: 0 auto;
          background: white;
        }
        
        .bill-header {
          text-align: center;
          border-bottom: 2px dashed #333;
          padding-bottom: 15px;
          margin-bottom: 15px;
        }
        
        .bill-header h1 {
          font-size: 24px;
          font-weight: bold;
          margin-bottom: 5px;
          color: #333;
        }
        
        .bill-header h2 {
          font-size: 18px;
          font-weight: normal;
          color: #666;
          margin-bottom: 10px;
        }
        
        .bill-info {
          margin-bottom: 15px;
          padding-bottom: 15px;
          border-bottom: 1px solid #ddd;
        }
        
        .bill-info-row {
          display: flex;
          justify-content: space-between;
          margin-bottom: 5px;
          font-size: 14px;
        }
        
        .bill-info-label {
          font-weight: bold;
        }
        
        .bill-items {
          margin-bottom: 15px;
        }
        
        .bill-item {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          padding-bottom: 8px;
          border-bottom: 1px dotted #ddd;
          font-size: 14px;
        }
        
        .bill-item-name {
          flex: 1;
          margin-right: 10px;
        }
        
        .bill-item-details {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          min-width: 120px;
        }
        
        .bill-item-quantity {
          color: #666;
          font-size: 12px;
        }
        
        .bill-item-price {
          font-weight: bold;
        }
        
        .bill-item-discount {
          color: #dc3545;
          font-size: 12px;
        }
        
        .bill-summary {
          border-top: 2px solid #333;
          padding-top: 15px;
          margin-top: 15px;
        }
        
        .bill-summary-row {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          font-size: 14px;
        }
        
        .bill-summary-label {
          color: #666;
        }
        
        .bill-summary-value {
          font-weight: bold;
        }
        
        .bill-total {
          border-top: 2px dashed #333;
          padding-top: 10px;
          margin-top: 10px;
        }
        
        .bill-total-label {
          font-size: 18px;
          font-weight: bold;
        }
        
        .bill-total-value {
          font-size: 20px;
          font-weight: bold;
          color: #dc3545;
        }
        
        .bill-note {
          margin-top: 15px;
          padding-top: 15px;
          border-top: 1px solid #ddd;
          font-size: 12px;
          color: #666;
          font-style: italic;
        }
        
        .bill-footer {
          text-align: center;
          margin-top: 20px;
          padding-top: 15px;
          border-top: 1px dashed #ddd;
          font-size: 12px;
          color: #999;
        }
        
        @media print {
          body {
            padding: 0;
          }
          
          .bill-container {
            max-width: 100%;
          }
          
          @page {
            size: 80mm auto;
            margin: 0;
          }
        }
      </style>
    </head>
    <body>
      <div class="bill-container">
        <div class="bill-header">
          <h1>LITEFLOW RESTAURANT</h1>
          <h2>BILL TẠM TÍNH</h2>
        </div>
        
        <div class="bill-info">
          <div class="bill-info-row">
            <span class="bill-info-label">Bàn:</span>
            <span>${tableName}${tableRoom ? ' - ' + tableRoom : ''}</span>
          </div>
          <div class="bill-info-row">
            <span class="bill-info-label">Hóa đơn:</span>
            <span>${invoiceName}</span>
          </div>
          <div class="bill-info-row">
            <span class="bill-info-label">Ngày:</span>
            <span>${dateStr} ${timeStr}</span>
          </div>
        </div>
        
        <div class="bill-items">
          ${orderItems.map(item => {
            const itemSubtotal = item.price * item.quantity;
            let itemDiscountAmount = 0;
            let discountText = '';
            
            if (item.discount) {
              if (item.discount.type === 'percent') {
                itemDiscountAmount = Math.round(itemSubtotal * item.discount.value / 100);
                discountText = `-${item.discount.value}%`;
              } else {
                itemDiscountAmount = item.discount.value;
                discountText = `-${itemDiscountAmount.toLocaleString('vi-VN')}đ`;
              }
            }
            
            const itemTotal = Math.max(0, itemSubtotal - itemDiscountAmount);
            
            return `
              <div class="bill-item">
                <div class="bill-item-name">
                  ${item.name}
                </div>
                <div class="bill-item-details">
                  <div class="bill-item-quantity">${item.quantity} x ${item.price.toLocaleString('vi-VN')}đ</div>
                  ${discountText ? `<div class="bill-item-discount">${discountText}</div>` : ''}
                  <div class="bill-item-price">${itemTotal.toLocaleString('vi-VN')}đ</div>
                </div>
              </div>
            `;
          }).join('')}
        </div>
        
        <div class="bill-summary">
          <div class="bill-summary-row">
            <span class="bill-summary-label">Tạm tính:</span>
            <span class="bill-summary-value">${subtotal.toLocaleString('vi-VN')}đ</span>
          </div>
          ${orderDiscountAmount > 0 ? `
            <div class="bill-summary-row">
              <span class="bill-summary-label">Giảm giá:</span>
              <span class="bill-summary-value" style="color: #dc3545;">-${orderDiscountAmount.toLocaleString('vi-VN')}đ</span>
            </div>
          ` : ''}
          <div class="bill-summary-row">
            <span class="bill-summary-label">VAT (${vatRate}%):</span>
            <span class="bill-summary-value">${vat.toLocaleString('vi-VN')}đ</span>
          </div>
          <div class="bill-summary-row bill-total">
            <span class="bill-total-label">TỔNG CỘNG:</span>
            <span class="bill-total-value">${total.toLocaleString('vi-VN')}đ</span>
          </div>
        </div>
        
        ${orderNote ? `
          <div class="bill-note">
            <strong>Ghi chú:</strong> ${orderNote}
          </div>
        ` : ''}
        
        <div class="bill-footer">
          <p>Cảm ơn quý khách!</p>
          <p>Bill tạm tính - Chưa thanh toán</p>
        </div>
      </div>
    </body>
    </html>
  `;
  
  // Write HTML to print window
  printWindow.document.open();
  printWindow.document.write(billHTML);
  printWindow.document.close();
  
  // Wait for content to load, then print
  const printAfterLoad = () => {
    try {
      printWindow.focus();
      // Small delay to ensure content is rendered
      setTimeout(() => {
        printWindow.print();
      }, 100);
    } catch (error) {
      console.error('Error printing:', error);
      alert('Có lỗi xảy ra khi in. Vui lòng thử lại.');
    }
  };
  
  // Check if document is already loaded
  if (printWindow.document.readyState === 'complete') {
    printAfterLoad();
  } else {
    printWindow.addEventListener('load', printAfterLoad, { once: true });
    // Fallback timeout
    setTimeout(printAfterLoad, 1000);
  }
}

