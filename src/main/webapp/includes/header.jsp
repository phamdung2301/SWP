<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="contextPath" content="${pageContext.request.contextPath}">
  <title>LiteFlow - Hệ thống quản lý</title>
  
  <!-- Icons + Fonts -->
  <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  
  <!-- Custom CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/animations.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/ui-components.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/chatbot.css">
  
  <!-- Dropdown Fix Script -->
  <script src="${pageContext.request.contextPath}/js/dropdown-fix.js"></script>
  
  <!-- Notification Bell Script -->
  <script src="${pageContext.request.contextPath}/js/notification-bell.js"></script>

  <!-- Send Notification Script -->
  <script src="${pageContext.request.contextPath}/js/send-notification.js"></script>
  
  <!-- ChatBot Script -->
  <script src="${pageContext.request.contextPath}/js/chatbot.js"></script>
  
  <!-- Inline Dropdown Fix -->
  <script>
    // Simple inline dropdown fix
    document.addEventListener('DOMContentLoaded', function() {
      console.log('🔧 Inline dropdown fix loaded');
      
      const dropdowns = document.querySelectorAll('.nav-item.dropdown');
      console.log('Found dropdowns:', dropdowns.length);
      
      // CRITICAL: Close all dropdowns on page load
      dropdowns.forEach(dropdown => {
        dropdown.classList.remove('show', 'active');
        console.log('✅ Closed dropdown on page load');
      });
      
      dropdowns.forEach((dropdown, index) => {
        const toggle = dropdown.querySelector('.nav-link.dropdown-toggle');
        const menu = dropdown.querySelector('.dropdown-menu');
        
        console.log(`Dropdown ${index}:`, {
          element: dropdown,
          toggle: toggle,
          menu: menu
        });
        
        if (toggle && menu) {
          let hoverTimeout;
          
          // Click handler
          toggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            console.log('🖱️ Click on dropdown:', dropdown);
            
            // Close all other dropdowns
            dropdowns.forEach(otherDropdown => {
              if (otherDropdown !== dropdown) {
                otherDropdown.classList.remove('show', 'active');
              }
            });
            
            // Toggle current dropdown
            const isOpen = dropdown.classList.contains('show');
            
            if (isOpen) {
              dropdown.classList.remove('show', 'active');
              console.log('❌ Closed dropdown');
            } else {
              dropdown.classList.add('show', 'active');
              console.log('✅ Opened dropdown');
            }
          });
          
          // Hover effects with delay for better UX
          let headerHoverTimeout;
          
          dropdown.addEventListener('mouseenter', function() {
            if (window.innerWidth > 768) {
              console.log('🖱️ Header hover enter:', dropdown);
              // Clear any pending close timeout
              if (headerHoverTimeout) {
                clearTimeout(headerHoverTimeout);
                headerHoverTimeout = null;
              }
              dropdown.classList.add('show', 'active');
            }
          });
          
          dropdown.addEventListener('mouseleave', function() {
            if (window.innerWidth > 768) {
              console.log('🖱️ Header hover leave:', dropdown);
              // Add delay before closing
              headerHoverTimeout = setTimeout(function() {
                dropdown.classList.remove('show', 'active');
                console.log('⏰ Header delayed close dropdown');
              }, 300); // 300ms delay
            }
          });
        }
      });
      
      // Close dropdowns when clicking outside or on dropdown items
      document.addEventListener('click', function(e) {
        if (!e.target.closest('.nav-item.dropdown')) {
          console.log('🖱️ Clicked outside, closing dropdowns');
          dropdowns.forEach(dropdown => {
            dropdown.classList.remove('show', 'active');
          });
        }
      });
      
      // Close dropdown when clicking on dropdown items
      document.querySelectorAll('.dropdown-item').forEach(item => {
        item.addEventListener('click', function() {
          console.log('🖱️ Dropdown item clicked:', this.textContent.trim());
          const dropdown = this.closest('.nav-item.dropdown');
          if (dropdown) {
            dropdown.classList.remove('show', 'active');
          }
        });
      });
      
      // Initialize Notification Bell (only for non-Employee roles)
      const notificationBellContainer = document.getElementById('notification-bell-container');
      if (notificationBellContainer) {
        initNotificationBell({
          contextPath: '${pageContext.request.contextPath}',
          refreshInterval: 60000  // 1 minute
        });
      }
    });
  </script>
</head>
<body>

<!-- Check user roles for fine-grained access control -->
<c:set var="isAdmin" value="false" />
<c:set var="isOwner" value="false" />
<c:set var="isHR" value="false" />
<c:set var="isProcurement" value="false" />
<c:set var="isInventory" value="false" />
<c:set var="isCashier" value="false" />
<c:set var="isKitchen" value="false" />
<c:set var="isEmployee" value="false" />

<c:forEach var="role" items="${sessionScope.UserRoles}">
  <c:if test="${role == 'Admin'}"><c:set var="isAdmin" value="true" /></c:if>
  <c:if test="${role == 'Owner'}"><c:set var="isOwner" value="true" /></c:if>
  <c:if test="${role == 'HR Officer'}"><c:set var="isHR" value="true" /></c:if>
  <c:if test="${role == 'Procurement Officer'}"><c:set var="isProcurement" value="true" /></c:if>
  <c:if test="${role == 'Inventory Manager'}"><c:set var="isInventory" value="true" /></c:if>
  <c:if test="${role == 'Cashier'}"><c:set var="isCashier" value="true" /></c:if>
  <c:if test="${role == 'Kitchen'}"><c:set var="isKitchen" value="true" /></c:if>
  <c:if test="${role == 'Employee'}"><c:set var="isEmployee" value="true" /></c:if>
</c:forEach>

<c:set var="isSuper" value="${isAdmin || isOwner}" />

<div class="app">
  <!-- Top Header Bar -->
  <header class="top-header">
    <div class="top-header-content">
      <a href="${pageContext.request.contextPath}/dashboard" class="nav-brand">
        <img src="${pageContext.request.contextPath}/img/logo.png" alt="LiteFlow Logo" class="nav-logo">
        <span class="nav-brand-name">LiteFlow</span>
      </a>
      <nav class="top-header-nav">
        <div class="top-header-banner">Where small shops run smarter</div>
      </nav>
      <div class="top-header-right">
        
        <div class="header-icons">
          <!-- Notification Bell - Only for relevant roles -->
          <c:if test="${isSuper || isHR || isProcurement || isInventory || isCashier || isKitchen}">
            <div id="notification-bell-container"></div>
          </c:if>
          <div class="nav-item dropdown" style="margin: 0;">
            <a href="#" class="nav-link dropdown-toggle" aria-expanded="false" style="display: flex; align-items: center; gap: 8px; padding: 8px 12px; color: #374151 !important;">
              <i class='bx bx-user' style="color: #374151;"></i>
              <div style="display: flex; flex-direction: column; align-items: flex-start; line-height: 1.2;">
                <span style="font-size: 14px; font-weight: 600; color: #1f2937;">
                  <c:choose>
                    <c:when test="${not empty sessionScope.UserDisplayName}">
                      ${sessionScope.UserDisplayName}
                    </c:when>
                    <c:otherwise>
                      Tài khoản
                    </c:otherwise>
                  </c:choose>
                </span>
                <span style="font-size: 11px; color: #6b7280;">
                  <c:if test="${not empty sessionScope.UserRoles}">
                    <c:forEach var="role" items="${sessionScope.UserRoles}" varStatus="status">
                      ${role}<c:if test="${!status.last}">, </c:if>
                    </c:forEach>
                  </c:if>
                </span>
              </div>
            </a>
            <div class="dropdown-menu" style="right: 0; left: auto;">
              <a href="${pageContext.request.contextPath}/settings" class="dropdown-item" target="_blank">
                <i class='bx bx-cog'></i> Cài đặt
              </a>
              <a href="${pageContext.request.contextPath}/logout" class="dropdown-item">
                <i class='bx bx-log-out'></i> Đăng xuất
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>

  <!-- Main Navigation Bar -->
  <nav class="main-nav">
    <div class="nav-content">
      <div class="nav-menu">
        <!-- Tổng quan -->
        <c:choose>
          <c:when test="${isSuper || isHR || isProcurement || isInventory}">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-item ${param.page == 'dashboard' ? 'active' : ''}">
              <i class='bx bxs-dashboard'></i> Tổng quan
            </a>
          </c:when>
          <c:otherwise>
            <a href="${pageContext.request.contextPath}/dashboard-employee" class="nav-item ${param.page == 'dashboard-employee' ? 'active' : ''}">
              <i class='bx bxs-dashboard'></i> Tổng quan
            </a>
          </c:otherwise>
        </c:choose>
        
        <!-- Hàng hóa -->
        <c:if test="${isSuper || isInventory || isProcurement}">
          <div class="nav-item dropdown ${param.page == 'products' || param.page == 'setprice' ? 'active' : ''}">
            <a href="#" class="nav-link dropdown-toggle">
              <i class='bx bxs-package'></i> Hàng hóa
              <i class='bx bx-chevron-down' style="margin-left: 4px; font-size: 14px;"></i>
            </a>
            <div class="dropdown-menu">
              <a href="${pageContext.request.contextPath}/products" class="dropdown-item">
                <i class='bx bxs-category'></i> Danh mục
              </a>
              <a href="${pageContext.request.contextPath}/setprice" class="dropdown-item">
                <i class='bx bx-dollar'></i> Thiết lập giá
              </a>
            </div>
          </div>
        </c:if>
          
        <!-- Phòng/Bàn -->
        <c:if test="${isSuper || isCashier}">
          <a href="${pageContext.request.contextPath}/roomtable" class="nav-item ${param.page == 'rooms' ? 'active' : ''}">
            <i class='bx bx-store'></i> Phòng/Bàn
          </a>
        </c:if>
          
        <!-- Giao dịch -->
        <c:if test="${isSuper || isCashier}">
          <div class="nav-item dropdown">
            <a href="#" class="nav-link dropdown-toggle">
              <i class='bx bx-receipt'></i> Giao dịch
              <i class='bx bx-chevron-down' style="margin-left: 4px; font-size: 14px;"></i>
            </a>
            <div class="dropdown-menu">
              <a href="${pageContext.request.contextPath}/sales/invoice" class="dropdown-item">
                <i class='bx bx-receipt'></i> Hoá đơn bán hàng
              </a>
            </div>
          </div>
        </c:if>
          
        <!-- Đối tác -->
        <c:if test="${isSuper || isProcurement || isInventory}">
          <div class="nav-item dropdown">
            <a href="#" class="nav-link dropdown-toggle">
              <i class='bx bx-group'></i> Đối tác
              <i class='bx bx-chevron-down' style="margin-left: 4px; font-size: 14px;"></i>
            </a>
            <div class="dropdown-menu">
              <a href="${pageContext.request.contextPath}/procurement/supplier" class="dropdown-item">
                <i class='bx bx-store'></i> Nhà cung cấp
              </a>
            </div>
          </div>
        </c:if>
          
        <!-- Mua sắm -->
        <c:if test="${isSuper || isProcurement || isInventory}">
          <div class="nav-item dropdown">
            <a href="#" class="nav-link dropdown-toggle">
              <i class='bx bx-shopping-bag'></i> Mua sắm
              <i class='bx bx-chevron-down' style="margin-left: 4px; font-size: 14px;"></i>
            </a>
            <div class="dropdown-menu">
              <a href="${pageContext.request.contextPath}/procurement/dashboard" class="dropdown-item">
                <i class='bx bxs-dashboard'></i> Tổng quan
              </a>
              <a href="${pageContext.request.contextPath}/procurement/po" class="dropdown-item">
                <i class='bx bx-receipt'></i> Đơn đặt hàng
              </a>
            </div>
          </div>
        </c:if>
          
        <!-- Báo cáo -->
        <c:if test="${isSuper}">
          <div class="nav-item dropdown">
            <a href="#" class="nav-link dropdown-toggle">
              <i class='bx bx-bar-chart'></i> Báo cáo
              <i class='bx bx-chevron-down' style="margin-left: 4px; font-size: 14px;"></i>
            </a>
            <div class="dropdown-menu">
              <a href="${pageContext.request.contextPath}/report/revenue" class="dropdown-item">
                <i class='bx bx-line-chart'></i> Báo cáo doanh thu
              </a>
            </div>
          </div>
        </c:if>
        
        <!-- Nhân viên - hiển thị lịch/chấm công cho mọi người, nhưng thiết lập chỉ cho HR/Super -->
        <div class="nav-item dropdown">
          <a href="#" class="nav-link dropdown-toggle">
            <i class='bx bx-user'></i> Nhân sự
            <i class='bx bx-chevron-down' style="margin-left: 4px; font-size: 14px;"></i>
          </a>
          <div class="dropdown-menu">
            <c:if test="${isSuper || isHR}">
              <a href="${pageContext.request.contextPath}/employees" class="dropdown-item">
                <i class='bx bx-group'></i> Danh sách nhân viên
              </a>
              <a href="${pageContext.request.contextPath}/employee/setup" class="dropdown-item">
                <i class='bx bx-cog'></i> Thiết lập nhân viên
              </a>
            </c:if>
            <a href="${pageContext.request.contextPath}/schedule" class="dropdown-item">
              <i class='bx bx-calendar'></i> Lịch làm việc
            </a>
            <a href="${pageContext.request.contextPath}/attendance" class="dropdown-item">
              <i class='bx bx-time'></i> Bảng chấm công
            </a>
            <a href="${pageContext.request.contextPath}/employee/paysheet" class="dropdown-item">
              <i class='bx bx-money'></i> Bảng lương
            </a>
          </div>
        </div>
      </div>
      
      <!-- Right Side Icons -->
      <div class="nav-right">
        <c:if test="${isSuper || isKitchen}">
          <a href="${pageContext.request.contextPath}/kitchen" class="nav-icon" title="Nhà bếp" target="_blank">
            <i class='bx bxs-bowl-hot'></i>
          </a>
        </c:if>
        <c:if test="${isSuper || isCashier}">
          <a href="${pageContext.request.contextPath}/reception" class="nav-icon" title="Lễ tân" target="_blank">
            <i class='bx bx-calendar'></i>
          </a>
        </c:if>
        <c:if test="${isSuper || isHR || isProcurement}">
          <div class="nav-icon" id="send-notification-icon" title="Gửi thông báo" style="cursor: pointer;">
            <i class='bx bx-mail-send'></i>
          </div>
        </c:if>
        <c:if test="${isSuper || isCashier}">
          <a href="${pageContext.request.contextPath}/cashier" class="nav-icon" title="Thu ngân (POS)" target="_blank">
            <i class='bx bx-file'></i>
          </a>
        </c:if>
      </div>
    </div>
  </nav>

  <!-- Main Content -->
  <main class="main">
    <div class="content">
