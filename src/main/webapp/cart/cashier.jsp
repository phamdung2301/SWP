<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cashier - LiteFlow</title>
    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.0.7/css/boxicons.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cashier.css">
    
    <!-- Initialize data for JavaScript -->
    <script>
      // Set context path for JavaScript
      window.contextPath = '${pageContext.request.contextPath}';
      
      // Database data from server
      window.tables = <c:choose><c:when test="${tablesJson != null}"><c:out value="${tablesJson}" escapeXml="false"/></c:when><c:otherwise>[]</c:otherwise></c:choose>;
      window.rooms = <c:choose><c:when test="${roomsJson != null}"><c:out value="${roomsJson}" escapeXml="false"/></c:when><c:otherwise>[]</c:otherwise></c:choose>;
      window.menuItems = <c:choose><c:when test="${menuItemsJson != null}"><c:out value="${menuItemsJson}" escapeXml="false"/></c:when><c:otherwise>[]</c:otherwise></c:choose>;
      window.categories = <c:choose><c:when test="${categoriesJson != null}"><c:out value="${categoriesJson}" escapeXml="false"/></c:when><c:otherwise>[]</c:otherwise></c:choose>;
      window.reservationsData = <c:choose><c:when test="${reservationsJson != null}"><c:out value="${reservationsJson}" escapeXml="false"/></c:when><c:otherwise>[]</c:otherwise></c:choose>;
    </script>
</head>

<body>
<div class="cashier-container">
  <!-- Cashier Header -->
  <div class="cashier-header">
    <!-- Header Left: Tabs & Search -->
    <div class="header-left">
      <!-- Main Tabs - Large KiotViet Style -->
      <div class="main-tabs">
        <button class="main-tab-btn active" data-tab="tables" onclick="switchMainTab('tables')">
            <i class='bx bx-table'></i>
          <span>Phòng bàn</span>
          </button>
        <button class="main-tab-btn" data-tab="menu" onclick="switchMainTab('menu')">
            <i class='bx bx-food-menu'></i>
          <span>Thực đơn</span>
          </button>
        </div>
        
      <!-- Search Box -->
      <div class="header-search-box">
        <i class='bx bx-search'></i>
        <input type="text" id="headerSearch" placeholder="Tìm món (F3)" class="search-input-header">
      </div>
    </div>
    
    <!-- Header Right: Invoice Tabs & Actions -->
    <div class="header-right">
      <!-- Invoice Tabs -->
      <div class="invoice-tabs" id="invoiceTabs">
        <button class="invoice-tab active" data-invoice="1">
          <span>Hóa đơn 1</span>
          <i class='bx bx-x' onclick="closeInvoice(1, event)"></i>
        </button>
      </div>
      
      <!-- Current Invoice Display -->
      <div class="current-invoice-display">
        <span class="invoice-label">Hóa đơn hiện tại:</span>
        <span class="invoice-name" id="currentInvoiceName">Hóa đơn 1</span>
      </div>
      
      <!-- Add Invoice Button -->
      <button class="add-invoice-btn" onclick="addNewInvoice()" title="Thêm hóa đơn">
        <i class='bx bx-plus'></i>
        <span>Thêm hóa đơn</span>
      </button>
      
      <!-- Header Actions Group -->
      <div class="header-actions-group">
        <!-- Sound Toggle -->
        <button class="sound-toggle-btn" id="soundToggle" onclick="toggleSound()" title="Bật/tắt âm thanh">
          <i class='bx bx-volume-full'></i>
        </button>
        
        <!-- Notifications -->
        <button class="notification-btn" onclick="toggleNotifications()" title="Thông báo">
          <i class='bx bx-bell'></i>
          <span class="notification-badge" id="notificationCount" style="display: none;">0</span>
        </button>
        
        <!-- User Menu -->
        <div class="user-menu-wrapper">
        <button class="user-menu-btn" onclick="toggleUserMenu()">
            <i class='bx bx-user-circle' style="font-size: 20px;"></i>
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
        
        <!-- User Dropdown -->
        <div class="user-dropdown" id="userDropdown" style="display: none;">
          <button class="user-dropdown-item" onclick="navigate('management')">
            <i class='bx bx-category'></i>
            <span>Quản lý</span>
          </button>
          <button class="user-dropdown-item" onclick="navigate('kitchen')">
            <i class='bx bx-restaurant'></i>
            <span>Nhà bếp</span>
          </button>
          <button class="user-dropdown-item" onclick="navigate('reception')">
            <i class='bx bx-book'></i>
            <span>Lễ tân</span>
          </button>
          <div class="dropdown-divider"></div>
          <button class="user-dropdown-item" onclick="navigate('end-of-day-report')">
            <i class='bx bx-line-chart'></i>
            <span>Báo cáo cuối ngày</span>
          </button>
          <div class="dropdown-divider"></div>
          <button class="user-dropdown-item danger" onclick="logout()">
            <i class='bx bx-log-out'></i>
            <span>Đăng xuất</span>
          </button>
        </div>
      </div>
      </div> <!-- Close header-actions-group -->
    </div>
  </div>
  
  <!-- Notification Panel -->
  <div class="notification-panel" id="notificationPanel" style="display: none;">
    <div class="notification-header">
      <h3>Thông báo</h3>
      <button onclick="toggleNotifications()"><i class='bx bx-x'></i></button>
    </div>
    <div class="notification-list" id="notificationList">
      <div class="notification-empty">
        <i class='bx bx-bell-off'></i>
        <p>Không có thông báo mới</p>
      </div>
    </div>
  </div>
  <!-- Main Content -->
  <div class="main-content">
    <!-- Left Panel: Tables & Menu -->
    <div class="left-panel">
      <!-- Tab Container -->
      <div class="tab-container">
        <!-- Tab Content -->
        <div class="tab-content">
          <!-- Tables Tab -->
          <div class="tab-panel active" id="tables-tab">
            <div class="table-section">
              <!-- Top Filters Dropdown Style -->
              <div class="top-filters-dropdown">
                <!-- Filter by Status -->
                <div class="filter-dropdown-group">
                  <label for="statusFilter">Trạng thái:</label>
                  <select id="statusFilter" class="filter-select">
                    <option value="all">Tất cả</option>
                    <option value="available">Trống</option>
                    <option value="occupied">Có khách</option>
                  </select>
                </div>
                
                <!-- Filter by Room -->
                <div class="filter-dropdown-group">
                  <label for="roomFilter">Phòng:</label>
                  <select id="roomFilter" class="filter-select">
                    <option value="all">Tất cả</option>
                    <!-- Room filters will be populated by JavaScript -->
                  </select>
                </div>
                
                <!-- Filter by Capacity -->
                <div class="filter-dropdown-group">
                  <label for="capacityFilter">Sức chứa:</label>
                  <select id="capacityFilter" class="filter-select">
                    <option value="all">Tất cả</option>
                    <option value="2-4">2-4 chỗ</option>
                    <option value="5-6">5-6 chỗ</option>
                    <option value="7+">7+ chỗ</option>
                  </select>
              </div>
                
                <!-- Button Xem đặt bàn hôm nay -->
                <div class="filter-dropdown-group">
                  <button class="btn-view-reservations" onclick="openReservationSidebar()" title="Xem danh sách đặt bàn hôm nay">
                    <i class='bx bx-calendar-check'></i>
                    <span>Xem đặt bàn hôm nay</span>
                  </button>
                </div>
                
                <!-- ✅ Auto Switch to Menu Toggle -->
                <div class="filter-dropdown-group">
                  <label>Tự động mở menu:</label>
                  <button id="autoSwitchMenuBtn" class="auto-switch-toggle-btn" onclick="toggleAutoSwitchMenu()" title="Tự động chuyển sang tab Thực đơn khi chọn bàn">
                    <i class='bx bx-circle'></i>
                    <span class="btn-text">Tắt</span>
                  </button>
                </div>
              </div>
              
              <!-- Tables Grid -->
              <div class="tables-grid" id="tablesGrid">
                <!-- Tables will be populated by JavaScript -->
              </div>
              
              <!-- Guide Button -->
              <div class="guide-section">
                <button class="guide-btn" onclick="showTableGuide()">
                  <i class='bx bx-help-circle'></i>
                  <span>Hướng dẫn</span>
                </button>
              </div>
            </div>
          </div>
          
          <!-- Menu Tab -->
          <div class="tab-panel" id="menu-tab">
            <div class="menu-section">
              <!-- Category Buttons -->
              <div class="category-filters">
                <button class="category-btn active" data-category="all">
                  <i class='bx bx-category'></i>
                  <span>Tất cả</span>
                </button>
                <!-- Categories will be populated by JavaScript -->
              </div>

              <!-- Menu Grid -->
              <div class="menu-grid" id="menuGrid">
                <!-- Menu items will be populated by JavaScript -->
              </div>
              
              <!-- Guide Button -->
              <div class="guide-section">
                <button class="guide-btn" onclick="showMenuGuide()">
                  <i class='bx bx-help-circle'></i>
                  <span>Hướng dẫn</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Panel: Order & Bill -->
    <div class="right-panel">
      <!-- Combined Order & Bill Section -->
      <div class="section order-bill-section">
        <div class="section-header">
          <h2><i class='bx bx-receipt'></i> Đơn hàng</h2>
          <span class="table-info" id="selectedTableInfo">Chưa chọn bàn</span>
        </div>
        
        <!-- Order Items -->
        <div class="order-items" id="orderItems">
          <div class="empty-order">
            <i class='bx bx-shopping-cart'></i>
            <p>Chưa có món nào được chọn</p>
          </div>
        </div>

        <!-- Bill Summary -->
        <div class="bill-summary">
          <div class="bill-row">
            <span>Tạm tính:</span>
            <span id="subtotal">0đ</span>
          </div>
          <div class="bill-row discount" id="discountRow" style="display: none;">
            <span>Giảm giá:</span>
            <span id="discount">0đ</span>
          </div>
          <div class="bill-row vat-row">
            <span class="vat-label">
              VAT
              <input 
                type="number" 
                id="vatRate" 
                class="vat-input" 
                value="10" 
                min="0" 
                max="100" 
                step="1"
              />%:
            </span>
            <span id="vat">0đ</span>
          </div>
          <div class="bill-row total">
            <span>Tổng cộng:</span>
            <span id="total">0đ</span>
          </div>
        </div>

        <!-- Payment Methods -->
        <div class="payment-methods">
          <button class="payment-btn active" data-method="cash">
            <i class='bx bx-money'></i>
            <span>Tiền mặt</span>
          </button>
          <!-- ✅ Ẩn nút Thẻ - để sau -->
          <!-- <button class="payment-btn" data-method="card">
            <i class='bx bx-credit-card'></i>
            <span>Thẻ</span>
          </button> -->
          <button class="payment-btn" data-method="transfer">
            <i class='bx bx-transfer'></i>
            <span>Chuyển khoản</span>
          </button>
          <button class="payment-btn" data-method="vnpay">
            <i class='bx bx-credit-card'></i>
            <span>VNPay</span>
          </button>
        </div>

        <!-- Action Buttons -->
        <div class="action-buttons">
          <button class="btn btn-secondary btn-icon-only" id="clearOrder" title="Xóa đơn">
            <i class='bx bx-trash'></i>
          </button>
          <button class="btn btn-info btn-icon-only" id="orderNoteBtn" title="Ghi chú">
            <i class='bx bx-note'></i>
          </button>
          <button class="btn btn-success btn-icon-only" id="discountBtn" title="Giảm giá" onclick="openDiscountModal()">
            <i class='bx bx-gift'></i>
          </button>
          <button class="btn btn-info btn-icon-only" id="printBillBtn" onclick="printTemporaryBill()" disabled title="In bill tạm tính">
            <i class='bx bx-printer'></i>
          </button>
          <button class="btn btn-warning" id="notifyKitchenBtn" disabled>
            <i class='bx bx-bell'></i> Thông báo bếp
          </button>
          <button class="btn btn-primary" id="checkoutBtn" disabled>
            <i class='bx bx-check'></i> Thanh toán
          </button>
        </div>
      </div>
    </div>
  </div>
</div>

  <!-- Import Cashier JavaScript -->
  <script src="${pageContext.request.contextPath}/js/cashier.js"></script>
  <script>
// Initialize data from window object
contextPath = window.contextPath || '';
tables = window.tables || [];
rooms = window.rooms || [];
menuItems = window.menuItems || [];
categories = window.categories || [];
  </script>

<!-- Note: All JavaScript logic has been moved to /js/cashier.js -->

<!-- Discount Modal -->
<!-- ✅ Modal Giảm giá toàn đơn -->
<div id="discountModal" class="modal">
  <div class="modal-content">
    <div class="modal-header">
      <h2>Áp dụng giảm giá toàn đơn</h2>
      <span class="close" onclick="closeDiscountModal()">&times;</span>
    </div>
    
    <div class="modal-body">
      <div class="discount-tabs">
        <button class="tab active" data-type="percent">Giảm %</button>
        <button class="tab" data-type="amount">Giảm tiền</button>
      </div>
      
      <div class="tab-content">
        <div class="discount-input-group">
          <label>Giá trị giảm</label>
          <input type="number" id="discountInput" placeholder="Nhập số tiền hoặc %" min="0">
          <span class="input-suffix" id="discountSuffix">%</span>
        </div>
        
        <div class="discount-preview">
          <div class="preview-row">
            <span>Tạm tính:</span>
            <span id="previewSubtotal">0đ</span>
          </div>
          <div class="preview-row discount">
            <span>Giảm giá:</span>
            <span id="previewDiscount">0đ</span>
          </div>
          <div class="preview-row total">
            <span>Tổng tiền:</span>
            <span id="previewTotal">0đ</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="modal-footer">
      <button class="btn btn-danger" onclick="removeDiscount()" id="removeDiscountBtn" style="margin-right: auto;">
        <i class='bx bx-trash'></i> Xóa giảm giá
      </button>
      <button class="btn btn-secondary" onclick="closeDiscountModal()">Hủy</button>
      <button class="btn btn-success" onclick="confirmDiscount()">Áp dụng</button>
    </div>
  </div>
</div>

<!-- ✅ Modal Giảm giá cho từng món -->
<div id="itemDiscountModal" class="modal">
  <div class="modal-content">
    <div class="modal-header">
      <h2>Giảm giá cho món: <span id="itemDiscountName"></span></h2>
      <span class="close" onclick="closeItemDiscountModal()">&times;</span>
    </div>
    
    <div class="modal-body">
      <div class="discount-tabs">
        <button class="tab item-discount-tab active" data-type="percent">Giảm %</button>
        <button class="tab item-discount-tab" data-type="amount">Giảm tiền</button>
      </div>
      
      <div class="tab-content">
        <div class="discount-input-group">
          <label>Giá trị giảm</label>
          <input type="number" id="itemDiscountInput" placeholder="Nhập số tiền hoặc %" min="0">
          <span class="input-suffix" id="itemDiscountSuffix">%</span>
        </div>
        
        <div class="discount-preview">
          <div class="preview-row">
            <span>Đơn giá:</span>
            <span id="itemPreviewUnitPrice">0đ</span>
          </div>
          <div class="preview-row">
            <span>Số lượng:</span>
            <span id="itemPreviewQuantity">1</span>
          </div>
          <div class="preview-row">
            <span>Thành tiền:</span>
            <span id="itemPreviewSubtotal">0đ</span>
          </div>
          <div class="preview-row discount">
            <span>Giảm giá:</span>
            <span id="itemPreviewDiscount">0đ</span>
          </div>
          <div class="preview-row total">
            <span>Tổng tiền:</span>
            <span id="itemPreviewTotal">0đ</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="modal-footer">
      <button class="btn btn-danger" onclick="removeItemDiscount()" id="removeItemDiscountBtn" style="margin-right: auto;">
        <i class='bx bx-trash'></i> Xóa giảm giá
      </button>
      <button class="btn btn-secondary" onclick="closeItemDiscountModal()">Hủy</button>
      <button class="btn btn-success" onclick="confirmItemDiscount()">Áp dụng</button>
    </div>
  </div>
</div>

<!-- End Shift Modal -->
<div id="endShiftModal" class="modal">
  <div class="modal-content shift-summary">
    <div class="modal-header">
      <h2>Đối soát cuối ca</h2>
      <span class="close" onclick="closeEndShiftModal()">&times;</span>
    </div>
    
    <div class="modal-body">
      <div class="shift-info">
        <div class="info-row">
          <span>Ca làm việc:</span>
          <strong id="shiftName">Ca Sáng</strong>
        </div>
        <div class="info-row">
          <span>Thời gian:</span>
          <strong id="shiftTime">08:00 - 14:00</strong>
        </div>
        <div class="info-row">
          <span>Thu ngân:</span>
          <strong id="cashierName">Nguyễn Văn A</strong>
        </div>
      </div>
      
      <div class="revenue-summary">
        <div class="summary-card">
          <div class="label">Tổng đơn</div>
          <div class="value" id="totalOrders">0</div>
        </div>
        <div class="summary-card">
          <div class="label">Doanh thu</div>
          <div class="value primary" id="totalRevenue">0đ</div>
        </div>
        <div class="summary-card">
          <div class="label">Tiền mặt</div>
          <div class="value" id="cashAmount">0đ</div>
        </div>
        <div class="summary-card">
          <div class="label">Chuyển khoản</div>
          <div class="value" id="transferAmount">0đ</div>
        </div>
      </div>
      
      <div class="payment-methods-detail">
        <h3>Chi tiết thanh toán</h3>
        <table class="detail-table">
          <thead>
            <tr>
              <th>Phương thức</th>
              <th>Số đơn</th>
              <th>Tổng tiền</th>
            </tr>
          </thead>
          <tbody id="paymentDetails">
            <tr>
              <td>Tiền mặt</td>
              <td>0</td>
              <td>0đ</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <div class="modal-footer">
      <button class="btn btn-secondary" onclick="closeEndShiftModal()">Hủy</button>
      <button class="btn btn-primary" onclick="printShiftReport()">
        <i class='bx bx-printer'></i> In báo cáo
      </button>
      <button class="btn btn-success" onclick="confirmEndShift()">
        <i class='bx bx-check'></i> Xác nhận đóng ca
      </button>
      </div>
  </div>
</div>

<!-- Guide Modal -->
<div id="guideModal" class="modal">
  <div class="modal-content guide-modal">
    <div class="modal-header">
      <h2><i class='bx bx-book-open'></i> Hướng dẫn sử dụng</h2>
      <span class="close" onclick="closeGuideModal()">&times;</span>
    </div>
    
    <div class="modal-body">
      <!-- Tab Selection -->
      <div class="guide-tabs">
        <button class="guide-tab active" onclick="switchGuideTab('tables')">
            <i class='bx bx-table'></i>
          <span>Phòng bàn</span>
        </button>
        <button class="guide-tab" onclick="switchGuideTab('menu')">
          <i class='bx bx-restaurant'></i>
          <span>Thực đơn</span>
        </button>
        <button class="guide-tab" onclick="switchGuideTab('order')">
          <i class='bx bx-receipt'></i>
          <span>Đơn hàng</span>
        </button>
        <button class="guide-tab" onclick="switchGuideTab('payment')">
          <i class='bx bx-credit-card'></i>
          <span>Thanh toán</span>
        </button>
          </div>
      
      <!-- Guide Content -->
      <div class="guide-content">
        <!-- Tables Guide -->
        <div id="tablesGuide" class="guide-panel active">
          <h3><i class='bx bx-table'></i> Hướng dẫn quản lý Phòng bàn</h3>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">1</div>
              <div class="step-content">
                <h4>Chọn bàn</h4>
                <p>Click vào bàn <strong>TRỐNG</strong> (màu xanh lá) để chọn bàn cho khách.</p>
                <div class="guide-tip info">
                  <i class='bx bx-info-circle'></i>
                  <span>Bàn <strong>ĐÃ CHỌN</strong> sẽ chuyển sang màu đỏ và không thể chọn cho hóa đơn khác.</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">2</div>
              <div class="step-content">
                <h4>Tự động mở menu</h4>
                <p>Bật nút <strong>"Tự động mở menu"</strong> để hệ thống tự động chuyển sang tab Thực đơn khi chọn bàn.</p>
                <div class="guide-tip success">
                  <i class='bx bx-check-circle'></i>
                  <span>Tiết kiệm thời gian khi order nhanh!</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">3</div>
              <div class="step-content">
                <h4>Các loại bàn đặc biệt</h4>
                <ul class="guide-list">
                  <li><strong>Mang về</strong> (xanh lá): Dành cho khách mua mang đi</li>
                  <li><strong>Giao hàng</strong> (xanh dương): Dành cho đơn giao tận nơi</li>
                </ul>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">4</div>
              <div class="step-content">
                <h4>Lọc và tìm kiếm bàn</h4>
                <ul class="guide-list">
                  <li><strong>Trạng thái</strong>: Lọc bàn trống/có khách</li>
                  <li><strong>Phòng</strong>: Lọc theo khu vực (Tầng 1, Tầng 2, ...)</li>
                  <li><strong>Sức chứa</strong>: Lọc theo số chỗ ngồi</li>
                </ul>
      </div>
            </div>
          </div>
        </div>
        
        <!-- Menu Guide -->
        <div id="menuGuide" class="guide-panel">
          <h3><i class='bx bx-restaurant'></i> Hướng dẫn order món ăn</h3>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">1</div>
              <div class="step-content">
                <h4>Chọn món ăn</h4>
                <p>Click vào món trong danh sách để thêm vào đơn hàng.</p>
                <div class="guide-tip warning">
                  <i class='bx bx-error'></i>
                  <span>Lưu ý: Phải chọn bàn trước khi thêm món!</span>
          </div>
        </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">2</div>
              <div class="step-content">
                <h4>Tìm kiếm món</h4>
                <p>Sử dụng ô tìm kiếm hoặc nhấn phím tắt <kbd>F3</kbd> để focus vào ô tìm kiếm.</p>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">3</div>
              <div class="step-content">
                <h4>Lọc theo danh mục</h4>
                <p>Chọn danh mục trong dropdown để lọc món theo loại (Khai vị, Món chính, Đồ uống, ...).</p>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">4</div>
              <div class="step-content">
                <h4>Thông tin món ăn</h4>
                <p>Mỗi món hiển thị:</p>
                <ul class="guide-list">
                  <li>Tên món và kích cỡ (nếu có)</li>
                  <li>Giá tiền</li>
                  <li>Trạng thái còn hàng</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Order Guide -->
        <div id="orderGuide" class="guide-panel">
          <h3><i class='bx bx-receipt'></i> Hướng dẫn quản lý Đơn hàng</h3>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">1</div>
              <div class="step-content">
                <h4>Điều chỉnh số lượng</h4>
                <p>Trong panel bên phải, sử dụng nút <strong>+</strong> / <strong>-</strong> để điều chỉnh số lượng món.</p>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">2</div>
              <div class="step-content">
                <h4>Xóa món</h4>
                <p>Click vào icon <i class='bx bx-trash'></i> để xóa món khỏi đơn hàng.</p>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">3</div>
              <div class="step-content">
                <h4>Thông báo bếp</h4>
                <p>Click nút <strong>"Thông báo bếp"</strong> để gửi đơn hàng đến nhà bếp.</p>
                <div class="guide-tip info">
                  <i class='bx bx-info-circle'></i>
                  <span>Chỉ gửi những món <strong>MỚI</strong> chưa thông báo. Món đã gửi sẽ có dấu hiệu riêng.</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">4</div>
              <div class="step-content">
                <h4>Xóa đơn</h4>
                <p>Click nút <strong>"Xóa đơn"</strong> để xóa toàn bộ đơn hàng và bỏ chọn bàn.</p>
                <div class="guide-tip warning">
                  <i class='bx bx-error'></i>
                  <span>Cẩn thận! Hành động này không thể hoàn tác.</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Payment Guide -->
        <div id="paymentGuide" class="guide-panel">
          <h3><i class='bx bx-credit-card'></i> Hướng dẫn Thanh toán</h3>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">1</div>
              <div class="step-content">
                <h4>Áp dụng giảm giá</h4>
                <p>Click vào <strong>"Giảm %"</strong> hoặc <strong>"Giảm tiền"</strong> để áp dụng khuyến mãi.</p>
                <ul class="guide-list">
                  <li><strong>Giảm %</strong>: Giảm theo phần trăm (0-100%)</li>
                  <li><strong>Giảm tiền</strong>: Giảm số tiền cố định</li>
                </ul>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">2</div>
              <div class="step-content">
                <h4>Chọn phương thức thanh toán</h4>
                <p>Chọn một trong các phương thức:</p>
                <ul class="guide-list">
                  <li><i class='bx bx-money'></i> <strong>Tiền mặt</strong></li>
                  <li><i class='bx bx-credit-card'></i> <strong>Thẻ</strong></li>
                  <li><i class='bx bx-mobile'></i> <strong>Chuyển khoản</strong></li>
                </ul>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">3</div>
              <div class="step-content">
                <h4>Xác nhận thanh toán</h4>
                <p>Click nút <strong>"Thanh toán"</strong> để hoàn tất.</p>
                <div class="guide-tip success">
                  <i class='bx bx-check-circle'></i>
                  <span>Hệ thống sẽ tự động cập nhật trạng thái bàn và đóng đơn hàng.</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="guide-section-item">
            <div class="guide-step">
              <div class="step-number">4</div>
              <div class="step-content">
                <h4>Chuyển khoản</h4>
                <p>Click nút <strong>"Chuyển khoản"</strong> để chuyển đơn sang bàn khác (nếu khách đổi bàn).</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="modal-footer">
      <div class="guide-footer-note">
        <i class='bx bx-info-circle'></i>
        <span>Nhấn phím <kbd>ESC</kbd> để đóng hướng dẫn</span>
      </div>
      <button class="btn btn-primary" onclick="closeGuideModal()">
        <i class='bx bx-check'></i> Đã hiểu
      </button>
    </div>
  </div>
</div>

<!-- Confirm Modal -->
<div id="confirmModal" class="modal" style="display: none;">
  <div class="modal-content confirm-modal">
    <div class="modal-header">
      <h3 id="confirmModalTitle">Xác nhận</h3>
      </div>
    
    <div class="modal-body">
      <p id="confirmModalMessage"></p>
        </div>
    
    <div class="modal-footer">
      <button class="btn btn-secondary" id="confirmCancelBtn">
        <i class='bx bx-x'></i> Hủy
          </button>
      <button class="btn btn-primary" id="confirmOkBtn">
        <i class='bx bx-check'></i> Đồng ý
          </button>
        </div>
  </div>
</div>

<!-- Order Note Modal -->
<div id="orderNoteModal" class="modal" style="display: none;">
  <div class="modal-content">
    <div class="modal-header">
      <h3><i class='bx bx-note'></i> Ghi chú đơn hàng</h3>
      <button class="close-modal-btn" onclick="closeOrderNoteModal()">
        <i class='bx bx-x'></i>
        </button>
      </div>
    
    <div class="modal-body">
      <div class="form-group">
        <label for="orderNoteInput">Nội dung ghi chú:</label>
        <textarea 
          id="orderNoteInput" 
          class="form-control" 
          rows="5" 
          placeholder="Nhập ghi chú cho đơn hàng (vd: khách yêu cầu ít đường, không đá...)"
        ></textarea>
      </div>
      
      <div class="order-note-preview" id="orderNotePreview" style="display: none;">
        <div class="note-preview-header">
          <i class='bx bx-info-circle'></i>
          <span>Ghi chú hiện tại:</span>
        </div>
        <div class="note-preview-content" id="orderNotePreviewContent"></div>
      </div>
    </div>
    
    <div class="modal-footer">
      <button class="btn btn-secondary" onclick="closeOrderNoteModal()">
        <i class='bx bx-x'></i> Hủy
      </button>
      <button class="btn btn-danger" onclick="clearOrderNote()" id="clearNoteBtn" style="display: none;">
        <i class='bx bx-trash'></i> Xóa ghi chú
      </button>
      <button class="btn btn-primary" onclick="saveOrderNote()">
        <i class='bx bx-save'></i> Lưu ghi chú
      </button>
    </div>
  </div>
</div>

<!-- End of Day Report Modal -->
<div id="endOfDayModal" class="modal" style="display: none;">
  <div class="modal-content" style="max-width: 550px;">
    <div class="modal-header">
      <h3><i class='bx bx-line-chart'></i> Báo cáo cuối ngày</h3>
      <button class="close-modal-btn" onclick="closeEndOfDayModal()">
        <i class='bx bx-x'></i>
      </button>
    </div>
    
    <div class="modal-body">
      <div class="form-group">
        <label for="reportDate">Ngày báo cáo:</label>
        <input 
          type="date" 
          id="reportDate" 
          class="form-control"
          value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>"
        />
      </div>
      
      <div class="form-group">
        <label for="reportType">Loại báo cáo:</label>
        <select id="reportType" class="form-control">
          <option value="completed">Đã thanh toán</option>
          <option value="all">Tất cả hóa đơn</option>
          <option value="cancelled">Đã hủy</option>
        </select>
      </div>
      
      <div class="report-summary" id="reportSummary" style="display: none;">
        <h4><i class='bx bx-bar-chart-alt'></i> Thống kê</h4>
        <div class="summary-grid">
          <div class="summary-item">
            <div class="summary-icon">
              <i class='bx bx-receipt'></i>
            </div>
            <div class="summary-info">
              <span class="summary-label">Tổng hóa đơn</span>
              <span class="summary-value" id="totalInvoices">0</span>
            </div>
          </div>
          <div class="summary-item">
            <div class="summary-icon">
              <i class='bx bx-money'></i>
            </div>
            <div class="summary-info">
              <span class="summary-label">Doanh thu</span>
              <span class="summary-value" id="totalRevenue">0đ</span>
            </div>
          </div>
          <div class="summary-item">
            <div class="summary-icon">
              <i class='bx bx-cart'></i>
            </div>
            <div class="summary-info">
              <span class="summary-label">Tổng món bán</span>
              <span class="summary-value" id="totalItems">0</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="modal-footer">
      <button class="btn btn-secondary" onclick="closeEndOfDayModal()">
        <i class='bx bx-x'></i> Đóng
      </button>
      <button class="btn btn-success" onclick="exportReport()">
        <i class='bx bx-download'></i> Xuất PDF
      </button>
    </div>
  </div>
</div>

<!-- Logout Confirmation Modal -->
<div id="logoutModal" class="modal" style="display: none;">
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

<!-- ========== RESERVATION SIDEBAR PANEL ========== -->
<div class="sidebar-overlay" id="reservationSidebarOverlay" onclick="closeReservationSidebar()"></div>
<div class="reservation-sidebar" id="reservationSidebar">
  <div class="sidebar-header">
    <h2>
      <i class='bx bx-calendar-check'></i>
      Đặt bàn hôm nay
    </h2>
    <button class="close-btn" onclick="closeReservationSidebar()">
      <i class='bx bx-x'></i>
    </button>
  </div>

  <div class="sidebar-content">
    <!-- Filter & Search -->
    <div class="filter-group">
      <div class="search-box">
        <i class='bx bx-search'></i>
        <input type="text" id="reservationSearchInput" placeholder="Tìm theo tên, SĐT, mã đặt bàn..." 
               onkeyup="filterReservationList()">
      </div>
      <select id="reservationStatusFilter" onchange="filterReservationList()" class="filter-select">
        <option value="">Tất cả trạng thái</option>
        <option value="PENDING">Chờ xác nhận</option>
        <option value="CONFIRMED">Đã xác nhận</option>
        <option value="SEATED">Đang phục vụ</option>
        <option value="CLOSED">Đã đóng</option>
        <option value="CANCELLED">Đã hủy</option>
      </select>
    </div>

    <!-- Reservation List -->
    <div class="reservation-list" id="reservationListSidebar">
      <!-- Reservation items will be generated by JS -->
      <div class="empty-state">
        <i class='bx bx-calendar-x'></i>
        <p>Không có đặt bàn nào hôm nay</p>
      </div>
    </div>
  </div>
</div>

<!-- ========== EDIT RESERVATION MODAL ========== -->
<div id="editReservationModal" class="modal" style="display: none;">
  <div class="modal-content">
    <div class="modal-header">
      <h2>
        <i class='bx bx-edit'></i>
        Chỉnh sửa đặt bàn
      </h2>
      <div class="modal-header-actions">
        <button class="btn-delete-reservation" onclick="deleteReservationFromModal()" title="Xóa đặt bàn">
          <i class='bx bx-trash'></i>
        </button>
        <button class="close-modal-btn" onclick="closeEditReservationModal()">
          <i class='bx bx-x'></i>
        </button>
      </div>
    </div>
    
    <div class="modal-body">
      <div id="editReservationContent">
        <!-- Content will be populated by JavaScript -->
      </div>
    </div>
    
    <div class="modal-footer">
      <button class="btn btn-secondary" onclick="closeEditReservationModal()">
        <i class='bx bx-x'></i> Đóng
      </button>
      <button class="btn btn-primary" onclick="saveReservationChanges()">
        <i class='bx bx-save'></i> Lưu thay đổi
      </button>
    </div>
  </div>
</div>

</body>
</html>