<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lễ tân - Quản lý đặt bàn - LiteFlow</title>
    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.0.7/css/boxicons.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/reception.css">
</head>

<body>
    <!-- ========== TOP NAVIGATION ========== -->
    <nav class="top-nav">
        <div class="nav-left">
            <div class="brand">
                <i class='bx bxs-calendar-check'></i>
                <div class="brand-text">
                    <h1>Lễ tân</h1>
                    <span>Quản lý đặt bàn</span>
                </div>
            </div>
        </div>

        <div class="nav-center">
            <!-- Date Navigation -->
            <div class="date-nav">
                <button class="date-nav-btn" onclick="previousDay()">
                    <i class='bx bx-chevron-left'></i>
                </button>
                <div class="current-date">
                    <i class='bx bx-calendar'></i>
                    <span id="currentDateDisplay">Hôm nay</span>
                </div>
                <button class="date-nav-btn" onclick="nextDay()">
                    <i class='bx bx-chevron-right'></i>
                </button>
                <button class="today-btn" onclick="goToToday()">
                    Hôm nay
                </button>
                <button class="calendar-picker-btn" onclick="showDatePicker()" title="Chọn ngày">
                    <i class='bx bx-calendar-event'></i>
                    <input type="date" id="hiddenDatePicker" class="hidden-date-input" onchange="selectDateFromPicker()">
                </button>
            </div>
        </div>

        <div class="nav-right">
            <!-- Current Time -->
            <div class="time-display" id="currentTime"></div>

            <!-- Quick Actions -->
            <button class="nav-btn nav-btn-primary" onclick="openCreateReservation()">
                <i class='bx bx-plus'></i>
                <span>Đặt bàn mới</span>
            </button>

            <!-- Quick icons: Export and Refresh -->
            <button class="nav-btn" title="Xuất Excel" onclick="exportToExcel()">
                <i class='bx bx-download'></i>
            </button>
            <button class="nav-btn" title="Làm mới" onclick="triggerRefresh()">
                <i class='bx bx-refresh'></i>
            </button>

            <!-- Notifications removed per requirement -->

            <!-- User Menu -->
            <div class="dropdown">
                <button class="user-btn" onclick="toggleDropdown('userDropdown')">
                    <i class='bx bx-user-circle'></i>
                    <span class="user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.UserDisplayName}">
                                ${sessionScope.UserDisplayName}
                            </c:when>
                            <c:otherwise>
                                Tài khoản
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <i class='bx bx-chevron-down'></i>
                </button>
                <div class="dropdown-menu dropdown-menu-right" id="userDropdown">
                    <button class="dropdown-item" onclick="navigate('management')">
                        <i class='bx bx-category'></i>
                        <span>Quản lý</span>
                    </button>
                    <button class="dropdown-item" onclick="navigate('cashier')">
                        <i class='bx bx-cart'></i>
                        <span>Thu ngân</span>
                    </button>
                    <button class="dropdown-item" onclick="navigate('kitchen')">
                        <i class='bx bx-restaurant'></i>
                        <span>Bếp</span>
                    </button>
                    <div class="dropdown-divider"></div>
                    <button class="dropdown-item" onclick="logout()">
                        <i class='bx bx-log-out'></i>
                        <span>Đăng xuất</span>
                    </button>
                </div>
            </div>
        </div>
    </nav>

    <!-- ========== MAIN CONTENT ========== -->
    <div class="main-container">
        <!-- Stats Dashboard -->
        <div class="stats-grid">
            <div class="stat-card stat-card-total">
                <div class="stat-icon">
                    <i class='bx bx-calendar-check'></i>
                </div>
                <div class="stat-info">
                    <div class="stat-label">Tổng đặt bàn</div>
                    <div class="stat-value" id="totalReservations">0</div>
                </div>
            </div>

            <div class="stat-card stat-card-pending">
                <div class="stat-icon">
                    <i class='bx bx-time'></i>
                </div>
                <div class="stat-info">
                    <div class="stat-label">Chờ xác nhận</div>
                    <div class="stat-value" id="pendingReservations">0</div>
                </div>
            </div>


            <div class="stat-card stat-card-closed">
                <div class="stat-icon">
                    <i class='bx bx-check-shield'></i>
                </div>
                <div class="stat-info">
                    <div class="stat-label">Đã đóng</div>
                    <div class="stat-value" id="closedReservations">0</div>
                </div>
            </div>
        </div>

        <!-- Main Content Grid -->
        <div class="content-grid">
            <!-- Left Column: Timeline View -->
            <div class="timeline-panel">
                <div class="panel-header">
                    <h2>
                        <i class='bx bx-time-five'></i>
                        Lịch đặt bàn
                    </h2>
                    <div class="view-switcher">
                        <button class="view-btn active" data-view="timeline" onclick="switchTimelineView('timeline')">
                            <i class='bx bx-time'></i>
                            Timeline
                        </button>
                        <button class="view-btn" data-view="calendar" onclick="switchTimelineView('calendar')">
                            <i class='bx bx-calendar'></i>
                            Lịch
                        </button>
                    </div>
                </div>

                <div class="panel-content">
                    <!-- Timeline View -->
                    <div id="timelineView" class="timeline-view">
                        <div class="timeline-container" id="timelineContainer">
                            <!-- Timeline will be generated by JS -->
                        </div>
                    </div>

                    <!-- Calendar View (hidden by default) -->
                    <div id="calendarView" class="calendar-view" style="display: none;">
                        <div class="calendar-header">
                            <button onclick="previousWeek()"><i class='bx bx-chevron-left'></i></button>
                            <span id="weekDisplay">Tuần này</span>
                            <button onclick="nextWeek()"><i class='bx bx-chevron-right'></i></button>
                        </div>
                        <div class="calendar-grid" id="calendarGrid">
                            <!-- Calendar will be generated by JS -->
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right Column: Reservation List -->
            <div class="list-panel">
                <div class="panel-header">
                    <h2>
                        <i class='bx bx-list-ul'></i>
                        Danh sách đặt bàn
                    </h2>
                    
                    <!-- Filter & Search -->
                    <div class="filter-group">
                        <div class="search-box">
                            <i class='bx bx-search'></i>
                            <input type="text" id="searchInput" placeholder="Tìm theo tên, SĐT, mã đặt bàn..." 
                                   onkeyup="searchReservations()">
                        </div>
                        <select id="statusFilter" onchange="filterReservations()" class="filter-select">
                            <option value="">Tất cả trạng thái</option>
                            <option value="PENDING">Chờ xác nhận</option>
                            <option value="CANCELLED">Đã hủy</option>
                        </select>
                    </div>
                </div>

                <div class="panel-content">
                    <div class="reservation-list" id="reservationList">
                        <!-- Reservation items will be generated by JS -->
                        <div class="empty-state">
                            <i class='bx bx-calendar-x'></i>
                            <p>Không có đặt bàn nào</p>
                            <button class="btn-create" onclick="openCreateReservation()">
                                <i class='bx bx-plus'></i>
                                Tạo đặt bàn mới
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ========== SIDEBAR PANEL (Create/Edit Reservation) ========== -->
    <div class="sidebar-overlay" id="sidebarOverlay" onclick="closeSidebar()"></div>
    <div class="sidebar-panel" id="reservationSidebar">
        <div class="sidebar-header">
            <h2 id="sidebarTitle">
                <i class='bx bx-calendar-plus'></i>
                Đặt bàn mới
            </h2>
            <div class="sidebar-header-actions">
                <button class="btn-delete-reservation" id="deleteReservationBtn" onclick="deleteReservationFromSidebar()" style="display: none;" title="Xóa đặt bàn">
                    <i class='bx bx-trash'></i>
                </button>
                <button class="close-btn" onclick="closeSidebar()">
                    <i class='bx bx-x'></i>
                </button>
            </div>
        </div>

        <div class="sidebar-content">
            <form id="reservationForm">
                <input type="hidden" id="reservationId" name="reservationId">

                <!-- Customer Information -->
                <div class="form-section">
                    <h3 class="section-title">
                        <i class='bx bx-user'></i>
                        Thông tin khách hàng
                    </h3>
                    
                    <div class="form-group">
                        <label class="required">Tên khách hàng</label>
                        <input type="text" id="customerName" class="form-input" 
                               placeholder="Nhập tên khách hàng" required>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="required">Số điện thoại</label>
                            <input type="tel" id="customerPhone" class="form-input" 
                                   placeholder="0901234567" required>
                        </div>
                        <div class="form-group">
                            <label>Email</label>
                            <input type="email" id="customerEmail" class="form-input" 
                                   placeholder="email@example.com">
                        </div>
                    </div>
                </div>

                <!-- Reservation Details -->
                <div class="form-section">
                    <h3 class="section-title">
                        <i class='bx bx-calendar-event'></i>
                        Thông tin đặt bàn
                    </h3>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="required">Ngày & Giờ đến</label>
                            <input type="datetime-local" id="arrivalTime" class="form-input" required>
                        </div>
                        <div class="form-group">
                            <label class="required">Số khách</label>
                            <input type="number" id="numberOfGuests" class="form-input" 
                                   min="1" value="2" required onchange="filterTablesByRoom()">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Phòng</label>
                            <select id="roomId" class="form-input" onchange="filterTablesByRoom()">
                                <option value="">-- Chọn phòng --</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Bàn</label>
                            <select id="tableId" class="form-input">
                                <option value="">-- Chọn bàn (nếu có) --</option>
                            </select>
                        </div>
                    </div>

                    

                    <div class="form-group">
                        <label>Ghi chú</label>
                        <textarea id="notes" class="form-input" rows="3" 
                                  placeholder="Ghi chú đặc biệt (nếu có)..."></textarea>
                    </div>
                </div>

                <!-- Pre-order Items -->
                <div class="form-section">
                    <h3 class="section-title">
                        <i class='bx bx-food-menu'></i>
                        Món đặt trước
                        <button type="button" class="btn-add-item" onclick="openProductSelector()">
                            <i class='bx bx-plus'></i>
                            Thêm món
                        </button>
                    </h3>

                    <div id="preOrderList" class="pre-order-list">
                        <div class="empty-preorder">
                            <i class='bx bx-dish'></i>
                            <p>Chưa có món đặt trước</p>
                        </div>
                    </div>
                </div>
            </form>
        </div>

        <div class="sidebar-footer">
            <button type="button" class="btn btn-secondary" onclick="closeSidebar()">
                <i class='bx bx-x'></i>
                Hủy
            </button>
            <button type="button" class="btn btn-primary" onclick="submitReservation()">
                <i class='bx bx-check'></i>
                Lưu đặt bàn
            </button>
        </div>
    </div>

    <!-- ========== PRODUCT SELECTOR MODAL ========== -->
    <div class="modal-overlay" id="productSelectorOverlay" onclick="closeProductSelector()"></div>
    <div class="modal product-selector-modal" id="productSelectorModal" onclick="event.stopPropagation()">
        <div class="modal-header">
            <h3>
                <i class='bx bx-search'></i>
                Chọn món đặt trước
            </h3>
            <button class="close-btn" onclick="closeProductSelector()">
                <i class='bx bx-x'></i>
            </button>
        </div>
        <div class="modal-body">
            <div class="product-search">
                <i class='bx bx-search'></i>
                <input type="text" id="productSearchInput" placeholder="Tìm món..." onkeyup="searchProducts()">
            </div>
            <div class="product-grid" id="productGrid">
                <!-- Products will be loaded here -->
            </div>
        </div>
    </div>

    <!-- ========== IMPORT EXCEL MODAL ========== -->
    <div class="modal-overlay" id="importModalOverlay"></div>
    <div class="modal" id="importModal">
        <div class="modal-header">
            <h3>
                <i class='bx bx-upload'></i>
                Nhập dữ liệu từ Excel
            </h3>
            <button class="close-btn" onclick="closeImportModal()">
                <i class='bx bx-x'></i>
            </button>
        </div>
        <div class="modal-body">
            <div class="upload-zone" id="uploadZone">
                <i class='bx bx-cloud-upload'></i>
                <p>Kéo thả file Excel vào đây hoặc click để chọn file</p>
                <input type="file" id="excelFileInput" accept=".xlsx,.xls" onchange="handleFileSelect(event)" hidden>
                <button class="btn btn-primary" onclick="document.getElementById('excelFileInput').click()">
                    <i class='bx bx-folder-open'></i>
                    Chọn file
                </button>
            </div>
            <div class="upload-info">
                <a href="#" onclick="downloadTemplate()">
                    <i class='bx bx-download'></i>
                    Tải file mẫu
                </a>
            </div>
        </div>
    </div>

    <!-- Action Confirm Modal -->
    <div class="modal-overlay" id="actionModalOverlay" onclick="closeActionModal()"></div>
    <div class="modal" id="actionModal" onclick="event.stopPropagation()">
        <div class="modal-header">
            <h3 id="actionModalTitle"><i class='bx bx-question-mark'></i> Xác nhận thao tác</h3>
            <button class="close-btn" onclick="closeActionModal()"><i class='bx bx-x'></i></button>
        </div>
        <div class="modal-body" id="actionModalBody"></div>
        <div class="sidebar-footer" style="border-top:none; padding: 16px; gap: 10px;">
            <button class="btn btn-secondary" onclick="closeActionModal()"><i class='bx bx-x'></i> Hủy</button>
            <button class="btn btn-primary" id="actionModalConfirmBtn"><i class='bx bx-check'></i> Xác nhận</button>
        </div>
    </div>

    <!-- Logout Modal -->
    <div class="modal-overlay" id="logoutModalOverlay" onclick="closeLogoutModal()" style="display: none;"></div>
    <div id="logoutModal" class="modal" onclick="event.stopPropagation()" style="display: none;">
        <div class="modal-content">
            <div class="modal-header">
                <h3><i class='bx bx-log-out'></i> Xác nhận đăng xuất</h3>
                <button class="close-modal-btn" onclick="closeLogoutModal()">
                    <i class='bx bx-x'></i>
                </button>
            </div>
            
            <div class="modal-body">
                <p>Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?</p>
            </div>
            
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="closeLogoutModal()">
                    <i class='bx bx-x'></i> Hủy
                </button>
                <button class="btn btn-danger" onclick="confirmLogout()">
                    <i class='bx bx-log-out'></i> Đăng xuất
                </button>
            </div>
        </div>
    </div>

    <!-- Hidden data from server -->
    <script>
        // Data from server (already JSON serialized)
        window.reservationsData = ${not empty reservationsJson ? reservationsJson : '[]'};
        window.roomsData = ${not empty roomsJson ? roomsJson : '[]'};
        window.tablesData = ${not empty tablesJson ? tablesJson : '[]'};
        window.productsData = ${not empty productsJson ? productsJson : '[]'};
        window.contextPath = '${pageContext.request.contextPath}';
        
        console.log('📊 Server data loaded:', {
            reservations: window.reservationsData.length,
            rooms: window.roomsData.length,
            tables: window.tablesData.length,
            products: window.productsData.length,
            contextPath: window.contextPath
        });
    </script>

    <!-- Main JS -->
    <script src="${pageContext.request.contextPath}/js/reception.js"></script>
</body>
</html>
