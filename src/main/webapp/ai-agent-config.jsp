<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="includes/header.jsp">
  <jsp:param name="page" value="ai-agent-config" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/ai-agent-config.css">
<script src="${pageContext.request.contextPath}/js/ai-agent-config.js"></script>

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

<jsp:include page="includes/footer.jsp" />

