<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="contextPath" content="${pageContext.request.contextPath}">
  <title>Cài đặt - LiteFlow</title>
  
  <!-- Icons + Fonts -->
  <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  
  <!-- CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/settings.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/ai-agent-config.css">
</head>
<body class="settings-page-body">

<%
    // Get user roles from session
    java.util.List<String> userRoles = (java.util.List<String>) session.getAttribute("UserRoles");
    if (userRoles == null) {
        userRoles = new java.util.ArrayList<>();
    }
    
    // Check which settings cards user can access
    boolean canAccessAI = false;
    for (String role : userRoles) {
        if ("ADMIN".equalsIgnoreCase(role) || 
            "MANAGER".equalsIgnoreCase(role) || 
            "Owner".equalsIgnoreCase(role)) {
            canAccessAI = true;
            break;
        }
    }
    
    // Store in page context for JSTL
    pageContext.setAttribute("canAccessAI", canAccessAI);
    pageContext.setAttribute("userRoles", userRoles);
%>

<!-- Top Bar with Back Button -->
<div class="settings-top-bar">
    <a href="${pageContext.request.contextPath}/dashboard" class="back-button">
        <i class='bx bx-arrow-back'></i>
        <span>Trở về trang chủ</span>
    </a>
</div>

<!-- Main Layout -->
<div class="settings-layout">
    <!-- Sidebar -->
    <aside class="settings-sidebar" id="settingsSidebar">
        <div class="sidebar-header">
            <div class="sidebar-logo">
                <i class='bx bx-cog'></i>
                <span class="sidebar-title">Cài đặt</span>
            </div>
            <button class="sidebar-toggle" id="sidebarToggle" aria-label="Toggle sidebar">
                <i class='bx bx-menu'></i>
            </button>
        </div>
        
        <nav class="sidebar-nav">
            <!-- User Info - visible to all logged-in users -->
            <a href="#user-info" class="sidebar-item active" data-section="user-info">
                <i class='bx bx-user'></i>
                <span class="sidebar-item-text">Thông tin người dùng</span>
            </a>
            
            <c:if test="${canAccessAI}">
                <a href="#ai-agent" class="sidebar-item" data-section="ai-agent">
                    <i class='bx bx-brain'></i>
                    <span class="sidebar-item-text">AI Agent</span>
                </a>
            </c:if>
            
            <c:if test="${canAccessAI}">
                <a href="#company-info" class="sidebar-item" data-section="company-info">
                    <i class='bx bx-building'></i>
                    <span class="sidebar-item-text">Thông tin công ty</span>
                </a>
            </c:if>
            
            <!-- Placeholder for future menu items -->
            <!--
            <a href="#system" class="sidebar-item" data-section="system">
                <i class='bx bx-cog'></i>
                <span class="sidebar-item-text">Hệ thống</span>
            </a>
            <a href="#notifications" class="sidebar-item" data-section="notifications">
                <i class='bx bx-bell'></i>
                <span class="sidebar-item-text">Thông báo</span>
            </a>
            -->
        </nav>
    </aside>

    <!-- Main Content -->
    <main class="settings-main-content">
        <div class="settings-content-wrapper">
            <!-- User Info Section - visible to all logged-in users -->
            <section id="user-info-section" class="settings-section active">
                <div class="section-header">
                    <h1>
                        <i class='bx bx-user'></i>
                        Thông tin người dùng
                    </h1>
                    <p class="section-description">Xem thông tin tài khoản của bạn</p>
                </div>

                <c:choose>
                    <c:when test="${not empty currentUser}">
                        <div class="user-info-container">
                            <div class="user-info-card">
                                <div class="user-info-avatar">
                                    <i class='bx bx-user-circle'></i>
                                </div>
                                
                                <div class="user-info-details">
                                    <div class="info-row">
                                        <div class="info-label">
                                            <i class='bx bx-user'></i>
                                            <span>Họ và tên</span>
                                        </div>
                                        <div class="info-value">
                                            ${not empty currentUser.displayName ? currentUser.displayName : 'Chưa cập nhật'}
                                        </div>
                                    </div>
                                    
                                    <div class="info-row">
                                        <div class="info-label">
                                            <i class='bx bx-envelope'></i>
                                            <span>Email</span>
                                        </div>
                                        <div class="info-value">
                                            ${currentUser.email}
                                        </div>
                                    </div>
                                    
                                    <c:if test="${not empty currentUser.phone}">
                                        <div class="info-row">
                                            <div class="info-label">
                                                <i class='bx bx-phone'></i>
                                                <span>Số điện thoại</span>
                                            </div>
                                            <div class="info-value">
                                                ${currentUser.phone}
                                            </div>
                                        </div>
                                    </c:if>
                                    
                                    <div class="info-row">
                                        <div class="info-label">
                                            <i class='bx bx-shield-quarter'></i>
                                            <span>Vai trò</span>
                                        </div>
                                        <div class="info-value">
                                            <c:choose>
                                                <c:when test="${not empty userRoles}">
                                                    <c:forEach var="role" items="${userRoles}" varStatus="status">
                                                        <span class="role-badge">${role}</span>
                                                        <c:if test="${!status.last}">, </c:if>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">Chưa có vai trò</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    
                                    <c:if test="${not empty currentUser.createdAt}">
                                        <div class="info-row">
                                            <div class="info-label">
                                                <i class='bx bx-calendar'></i>
                                                <span>Ngày tạo tài khoản</span>
                                            </div>
                                            <div class="info-value">
                                                <fmt:formatDate value="${currentUser.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                            </div>
                                        </div>
                                    </c:if>
                                    
                                    <c:if test="${not empty currentUser.updatedAt}">
                                        <div class="info-row">
                                            <div class="info-label">
                                                <i class='bx bx-time-five'></i>
                                                <span>Cập nhật lần cuối</span>
                                            </div>
                                            <div class="info-value">
                                                <fmt:formatDate value="${currentUser.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                                            </div>
                                        </div>
                                    </c:if>
                                    
                                    <div class="info-row">
                                        <div class="info-label">
                                            <i class='bx bx-check-circle'></i>
                                            <span>Trạng thái</span>
                                        </div>
                                        <div class="info-value">
                                            <c:choose>
                                                <c:when test="${currentUser.isActive}">
                                                    <span class="status-badge status-active">Đang hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-badge status-inactive">Đã khóa</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="settings-empty">
                            <i class='bx bx-info-circle'></i>
                            <h3>Không thể tải thông tin người dùng</h3>
                            <p>Vui lòng thử lại sau hoặc liên hệ quản trị viên nếu vấn đề vẫn tiếp tục.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
            
            <!-- AI Agent Section -->
            <c:if test="${canAccessAI}">
                <section id="ai-agent-section" class="settings-section">
                    <div class="ai-config-container">
                        <div class="config-header">
                            <h1><i class='bx bx-cog'></i> LiteFlow Agent Configure</h1>
                            <p class="subtitle">Điều chỉnh các thông số của AI Agent để tối ưu hoạt động</p>
                        </div>

                        <!-- Tabs Navigation -->
                        <div class="config-tabs">
                            <button class="tab-btn active" data-category="STOCK_ALERT">
                                <i class='bx bx-package'></i> Cảnh báo Tồn kho
                            </button>
                            <button class="tab-btn" data-category="DEMAND_FORECAST">
                                <i class='bx bx-trending-up'></i> Dự báo Nhu cầu
                            </button>
                            <button class="tab-btn" data-category="PO_AUTO">
                                <i class='bx bx-cart'></i> Tự động Đặt hàng
                            </button>
                            <button class="tab-btn" data-category="SUPPLIER_MAPPING">
                                <i class='bx bx-link-alt'></i> Ánh xạ Nhà cung cấp
                            </button>
                            <button class="tab-btn" data-category="GPT_SERVICE">
                                <i class='bx bx-brain'></i> GPT Service
                            </button>
                            <button class="tab-btn" data-category="NOTIFICATION">
                                <i class='bx bx-bell'></i> Thông báo
                            </button>
                        </div>

                        <!-- Loading Indicator -->
                        <div id="loadingIndicator" class="loading-indicator">
                            <div class="spinner"></div>
                            <p>Đang tải cấu hình...</p>
                        </div>

                        <!-- Error Message -->
                        <div id="errorMessage" class="error-message" style="display: none;"></div>

                        <!-- Success Message -->
                        <div id="successMessage" class="success-message" style="display: none;"></div>

                        <!-- Config Content -->
                        <div id="configContent" class="config-content" style="display: none;">
                            <!-- Content will be dynamically loaded by JavaScript -->
                        </div>

                        <!-- Action Buttons -->
                        <div class="config-actions" style="display: none;">
                            <button id="saveBtn" class="btn btn-primary">
                                <i class='bx bx-save'></i> Lưu thay đổi
                            </button>
                            <button id="resetBtn" class="btn btn-secondary">
                                <i class='bx bx-reset'></i> Khôi phục mặc định
                            </button>
                            <button id="cancelBtn" class="btn btn-outline">
                                <i class='bx bx-x'></i> Hủy
                            </button>
                        </div>
                    </div>
                </section>
            </c:if>
            
            <!-- Company Info Section -->
            <c:if test="${canAccessAI}">
                <section id="company-info-section" class="settings-section">
                    <div class="section-header">
                        <h1><i class='bx bx-building'></i> Thông tin công ty</h1>
                        <p class="section-description">Quản lý thông tin công ty LiteFlow</p>
                    </div>
                    
                    <div class="company-info-container">
                        <div class="company-info-card">
                            <form id="companyInfoForm" class="company-info-form">
                                <div class="form-section">
                                    <h3 class="form-section-title">
                                        <i class='bx bx-info-circle'></i> Thông tin cơ bản
                                    </h3>
                                    
                                    <div class="form-group">
                                        <label for="companyName">
                                            Tên công ty 
                                            <span class="required">*</span>
                                        </label>
                                        <input type="text" id="companyName" name="name" required 
                                               placeholder="Nhập tên công ty" class="form-input">
                                    </div>
                                    
                                    <div class="form-group">
                                        <label for="companyAddress">Địa chỉ</label>
                                        <textarea id="companyAddress" name="address" rows="3" 
                                                  placeholder="Nhập địa chỉ công ty" class="form-input"></textarea>
                                    </div>
                                    
                                    <div class="form-row">
                                        <div class="form-group">
                                            <label for="companyPhone">Số điện thoại</label>
                                            <input type="text" id="companyPhone" name="phone" 
                                                   placeholder="Nhập số điện thoại" class="form-input">
                                        </div>
                                        
                                        <div class="form-group">
                                            <label for="companyEmail">Email</label>
                                            <input type="email" id="companyEmail" name="email" 
                                                   placeholder="Nhập email" class="form-input">
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="form-section">
                                    <h3 class="form-section-title">
                                        <i class='bx bx-file-blank'></i> Thông tin thuế
                                    </h3>
                                    
                                    <div class="form-group readonly-field">
                                        <label for="companyTaxCode">
                                            Mã số thuế
                                            <span class="readonly-badge">
                                                <i class='bx bx-lock-alt'></i> Chỉ đọc
                                            </span>
                                        </label>
                                        <input type="text" id="companyTaxCode" name="taxCode" readonly 
                                               placeholder="Mã số thuế sẽ được lấy từ file .env" 
                                               class="form-input readonly-input">
                                    </div>
                                </div>
                                
                                <div class="form-actions">
                                    <button type="button" id="cancelCompanyInfoBtn" class="btn btn-outline">
                                        <i class='bx bx-x'></i> Hủy
                                    </button>
                                    <button type="submit" id="saveCompanyInfoBtn" class="btn btn-primary">
                                        <i class='bx bx-save'></i> Lưu thông tin
                                    </button>
                                </div>
                            </form>
                            
                            <div id="companyInfoMessage" class="message-box" style="display: none;"></div>
                        </div>
                    </div>
                </section>
            </c:if>
            
            <!-- Empty State Section (if no accessible settings) -->
            <c:if test="${!canAccessAI}">
                <section id="empty-section" class="settings-section active">
                    <div class="settings-empty">
                        <i class='bx bx-info-circle'></i>
                        <h3>Không có cài đặt khả dụng</h3>
                        <p>Tài khoản của bạn hiện không có quyền truy cập vào bất kỳ cài đặt nào.</p>
                        <p class="empty-hint">Vui lòng liên hệ quản trị viên nếu bạn cần quyền truy cập.</p>
                    </div>
                </section>
            </c:if>

            <!-- Placeholder for future sections -->
            <!--
            <section id="system-section" class="settings-section">
                <div class="section-header">
                    <h1><i class='bx bx-cog'></i> Hệ thống</h1>
                    <p class="section-description">Cấu hình hệ thống và thông số chung</p>
                </div>
                <div class="settings-grid">
                    Settings cards here
                </div>
            </section>
            -->
        </div>
    </main>
</div>

<!-- JavaScript -->
<script src="${pageContext.request.contextPath}/js/settings.js"></script>
<script src="${pageContext.request.contextPath}/js/ai-agent-config.js"></script>
<script src="${pageContext.request.contextPath}/js/company-info.js"></script>

</body>
</html>
