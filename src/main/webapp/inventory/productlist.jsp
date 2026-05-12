<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.liteflow.model.inventory.ProductDisplayDTO" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:include page="../includes/header.jsp">
  <jsp:param name="page" value="products" />
</jsp:include>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/productlist.css">
<script src="${pageContext.request.contextPath}/js/productlist-enhanced.js"></script>

<div class="content">
    <!-- Statistics -->
    <div class="stats">
        <div class="stat-card">
            <div class="stat-number">${products.size()}</div>
            <div class="stat-label">Tổng sản phẩm</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">
                <c:set var="totalStock" value="0" />
                <c:forEach var="p" items="${products}">
                    <c:set var="totalStock" value="${totalStock + p.stockAmount}" />
                </c:forEach>
                <fmt:formatNumber value="${totalStock}" pattern="#,###" />
            </div>
            <div class="stat-label">Tổng tồn kho</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">
                <c:set var="activeProducts" value="0" />
                <c:forEach var="p" items="${products}">
                    <c:if test="${!p.isDeleted}">
                        <c:set var="activeProducts" value="${activeProducts + 1}" />
                    </c:if>
                </c:forEach>
                ${activeProducts}
            </div>
            <div class="stat-label">Đang bán</div>
        </div>
    </div>

    <!-- Success/Error Messages - Will use notification system -->
    <c:if test="${not empty success}">
        <script>
            window.addEventListener('DOMContentLoaded', () => {
                showNotification('${success}', 'success');
            });
        </script>
    </c:if>
    <c:if test="${not empty error}">
        <script>
            window.addEventListener('DOMContentLoaded', () => {
                showNotification('${error}', 'error');
            });
        </script>
    </c:if>

    <!-- Main Content Layout -->
    <div class="main-layout">
        <!-- Left Sidebar - Product Filters -->
        <div class="sidebar">
            <div class="filter-section">
                <h3 class="filter-title">Tìm kiếm</h3>
                <div class="search-box">
                    <input type="text" class="search-input" placeholder="Theo mã, tên hàng" id="searchInput" onkeyup="searchProducts()">
                </div>
            </div>

            <div class="filter-section">
                <h3 class="filter-title collapsible" onclick="toggleFilterSection(this)">
                    Danh Mục
                    <span class="collapse-icon">▼</span>
                </h3>
                <div class="filter-options collapsed">
                    <c:choose>
                        <c:when test="${not empty categories}">
                            <c:forEach var="category" items="${categories}">
                                <label class="filter-option">
                                    <input type="checkbox" name="categoryFilter" value="${category}" onchange="filterProducts()">
                                    <span class="checkmark"></span>
                                    ${category}
                                </label>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p style="color: #666; font-style: italic;">Chưa có danh mục nào từ sản phẩm</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="filter-section">
                <h3 class="filter-title collapsible" onclick="toggleFilterSection(this)">
                    Loại hàng
                    <span class="collapse-icon">▼</span>
                </h3>
                <div class="filter-options collapsed">
                    <label class="filter-option">
                        <input type="checkbox" name="productType" value="regular" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Hàng hóa thường
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="productType" value="processed" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Chế biến
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="productType" value="service" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Dịch vụ
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="productType" value="combo" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Combo
                    </label>
                </div>
            </div>

            <div class="filter-section">
                <h3 class="filter-title collapsible" onclick="toggleFilterSection(this)">
                    Đơn vị tính
                    <span class="collapse-icon">▼</span>
                </h3>
                <div class="filter-options collapsed">
                    <c:choose>
                        <c:when test="${not empty units}">
                            <c:forEach var="unit" items="${units}">
                                <label class="filter-option">
                                    <input type="checkbox" name="unitFilter" value="${unit}" onchange="filterProducts()">
                                    <span class="checkmark"></span>
                                    ${unit}
                                </label>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p style="color: #666; font-style: italic;">Chưa có đơn vị tính nào từ sản phẩm</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="filter-section">
                <h3 class="filter-title collapsible" onclick="toggleFilterSection(this)">
                    Tồn kho
                    <span class="collapse-icon">▼</span>
                </h3>
                <div class="filter-options collapsed">
                    <label class="filter-option">
                        <input type="checkbox" name="stockFilter" value="0" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Bằng 0
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="stockFilter" value="above0" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Trên 0
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="stockFilter" value="above100" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Trên 100
                    </label>
                </div>
            </div>

            <div class="filter-section">
                <h3 class="filter-title collapsible" onclick="toggleFilterSection(this)">
                    Trạng thái
                    <span class="collapse-icon">▼</span>
                </h3>
                <div class="filter-options collapsed">
                    <label class="filter-option">
                        <input type="checkbox" name="statusFilter" value="Đang bán" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Đang bán
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="statusFilter" value="Hết hàng" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Hết hàng
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="statusFilter" value="Dừng bán" onchange="filterProducts()">
                        <span class="checkmark"></span>
                        Dừng bán
                    </label>
                </div>
            </div>
        </div>

        <!-- Right Content - Product List -->
        <div class="main-content">
            <!-- Toolbar -->
            <div class="toolbar">
                <div>
                    <a href="#" class="btn btn-success" onclick="addProduct()">Thêm mới</a>
                    <button class="btn btn-primary" onclick="importProducts()">Nhập Excel</button>
                    <button class="btn btn-primary" onclick="exportProducts()">Xuất Excel</button>
                </div>
            </div>

            <!-- Product Table -->
            <div class="product-table">
                <c:choose>
                    <c:when test="${empty products}">
                        <div class="empty-state">
                            <h3>📦 Chưa có sản phẩm nào</h3>
                            <p>Hãy thêm sản phẩm đầu tiên để bắt đầu quản lý kho hàng</p>
                            <a href="#" class="btn btn-success" onclick="addProduct()" style="margin-top: 1rem;">➕ Thêm sản phẩm</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table class="table">
                            <thead>
                                <tr>
                                    <th class="sortable" onclick="sortTable(0, 'string')">
                                        Mã hàng
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th class="sortable" onclick="sortTable(1, 'string')">
                                        Tên hàng
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th class="sortable" onclick="sortTable(2, 'string')">
                                        Kích thước
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th class="sortable" onclick="sortTable(3, 'string')">
                                        Danh mục
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th class="sortable" onclick="sortTable(4, 'number')">
                                        Giá bán
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th class="sortable" onclick="sortTable(5, 'number')">
                                        Tồn kho
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th class="sortable" onclick="sortTable(6, 'string')">
                                        Trạng thái
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${products}">
                                    <tr class="product-row"
                                        data-product-id="${p.productId}"
                                        data-product-code="${p.productCode}"
                                        data-product-name="${p.productName}"
                                        data-size="${p.size}"
                                        data-category="${p.categoryName}"
                                        data-price="${p.price}"
                                        data-stock="${p.stockAmount}"
                                        data-image-url="${p.imageUrl}"
                                        data-product-type="${p.productType}"
                                        data-description="${p.description}"
                                        data-status="${p.status}"
                                        data-unit="${p.unit}">
                                        <td>
                                            <span class="product-code">${p.productCode}</span>
                                        </td>
                                        <td>
                                            <div class="product-name">${p.productName}</div>
                                        </td>
                                        <td>${p.size}</td>
                                        <td>
                                            <span class="category">
                                                ${p.categoryName != null ? p.categoryName : 'Chưa phân loại'}
                                            </span>
                                        </td>
                                        <td>
                                            <span class="price">
                                                <c:set var="priceInt" value="${Math.round(p.price)}" />
                                                <fmt:formatNumber value="${priceInt}" pattern="#,###" var="priceFormatted"/>
                                                ${fn:replace(priceFormatted, ',', '.')} ₫
                                            </span>
                                        </td>
                                        <td>
                                            <span class="stock 
                                                  <c:choose>
                                                      <c:when test="${p.stockAmount <= 10}">low</c:when>
                                                      <c:when test="${p.stockAmount <= 50}">medium</c:when>
                                                      <c:otherwise>high</c:otherwise>
                                                  </c:choose>">
                                                <fmt:formatNumber value="${p.stockAmount}" pattern="#,###" />
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.isDeleted}">
                                                    <span class="status inactive">Đã ẩn</span>
                                                </c:when>
                                                <c:when test="${p.stockAmount == 0}">
                                                    <span class="status warning">Hết hàng</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status ${p.status == 'Đang bán' ? 'active' : (p.status == 'Hết hàng' ? 'warning' : 'danger')}">
                                                        ${p.status != null ? p.status : 'Đang bán'}
                                            </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="actions">
                                                <button class="btn btn-warning btn-sm" onclick="editProduct('${p.productId}', event)">
                                                    Sửa
                                                </button>
                                                <button class="btn btn-danger btn-sm" onclick="deleteProduct('${p.productId}', '${p.size}', event); return false;">
                                                    Xóa
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        
                        <!-- Products Pagination -->
                        <div class="pagination-container" id="productsPagination">
                            <div class="pagination-info" id="productsPageInfo">
                                Trang 1 / 1
                            </div>
                            <div class="pagination-controls">
                                <button class="pagination-btn" id="productsPrevBtn" onclick="changeProductsPage(-1)" disabled>
                                    ← Trước
                                </button>
                                <div class="pagination-numbers" id="productsPageNumbers">
                                    <span class="pagination-number active">1</span>
                                </div>
                                <button class="pagination-btn" id="productsNextBtn" onclick="changeProductsPage(1)" disabled>
                                    Sau →
                                </button>
                            </div>
                            <div class="pagination-size">
                                <label for="productsPageSize">Hiển thị:</label>
                                <select id="productsPageSize" onchange="changeProductsPageSize(this.value)">
                                    <option value="10" selected>10</option>
                                    <option value="20">20</option>
                                    <option value="50">50</option>
                                    <option value="100">100</option>
                                </select>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

        <!-- JavaScript has been moved to productlist-enhanced.js -->

        <!-- Add Product Modal -->
        <div id="addProductModal" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>➕ Thêm sản phẩm mới</h2>
                    <span class="close" onclick="closeAddProductModal()">&times;</span>
                </div>
                <form id="addProductForm" action="products" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="create">
                    <div class="modal-body">
                        <!-- Row 1: Tên sản phẩm và Giá bán -->
                        <div class="form-row">
                            <div class="form-group">
                                <label for="name">Tên sản phẩm *</label>
                                <input type="text" id="name" name="name" required 
                                       placeholder="Nhập tên sản phẩm">
                            </div>
                            <div class="form-group">
                                <label for="price">Giá bán *</label>
                                <div style="position: relative;">
                                    <input type="text" id="price" name="price" required 
                                           placeholder="Nhập giá bán (>= 0 VND)"
                                           style="padding-right: 30px;">
                                    <span style="position: absolute; right: 10px; top: 50%; transform: translateY(-50%); color: #666;">đ</span>
                                </div>
                            </div>
                        </div>

                        <!-- Row 2: Loại hàng và Danh mục -->
                        <div class="form-row">
                            <div class="form-group">
                                <label for="productType">Loại hàng *</label>
                                <select id="productType" name="productType" required>
                                    <option value="">-- Chọn loại hàng --</option>
                                    <option value="Hàng hóa thường">Hàng hóa thường</option>
                                    <option value="Chế biến">Chế biến</option>
                                    <option value="Dịch vụ">Dịch vụ</option>
                                    <option value="Combo">Combo</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="category">Danh mục *</label>
                                <div style="display: flex; gap: 0.5rem; align-items: stretch;">
                                    <select id="category" name="category" required style="flex: 1;">
                                        <option value="">-- Chọn danh mục --</option>
                                        <c:forEach var="category" items="${categories}">
                                            <option value="${category}" data-category="${category}">${category}</option>
                                        </c:forEach>
                                    </select>
                                    <button type="button" id="deleteCategoryBtn" onclick="deleteCurrentCategory()" 
                                            style="padding: 0; min-width: 45px; max-width: 45px; width: 45px; height: 45px; flex-shrink: 0; display: none; align-items: center; justify-content: center; overflow: hidden; background: #dc3545; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 24px; font-weight: bold; line-height: 1;" 
                                            title="Xóa danh mục này">
                                        ×
                                    </button>
                                    <button type="button" onclick="showAddCategoryModal()" 
                                            style="padding: 0; min-width: 45px; max-width: 45px; width: 45px; height: 45px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; overflow: hidden; background: linear-gradient(135deg, #667eea, #00c6ff); color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 28px; font-weight: bold; line-height: 1;" 
                                            title="Thêm danh mục mới">
                                        +
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- Row 3: Size + Image (left) và Stock/Unit/Status + Description (right) -->
                        <div class="form-row">
                            <div class="form-group">
                                <label>Size *</label>
                                <div class="size-options">
                                    <div class="size-checkboxes">
                                        <label class="size-checkbox">
                                            <input type="radio" name="size" value="S" onchange="toggleCustomSize()">
                                            <span>S</span>
                                        </label>
                                        <label class="size-checkbox">
                                            <input type="radio" name="size" value="M" onchange="toggleCustomSize()">
                                            <span>M</span>
                                        </label>
                                        <label class="size-checkbox">
                                            <input type="radio" name="size" value="L" onchange="toggleCustomSize()">
                                            <span>L</span>
                                        </label>
                                    </div>
                                    <div class="custom-size">
                                        <label class="size-checkbox">
                                            <input type="radio" name="size" id="customSizeCheck" value="custom" onchange="toggleCustomSize()">
                                            <span>Tùy chọn nhập chữ</span>
                                        </label>
                                        <input type="text" id="customSizeInput" name="customSize" 
                                               placeholder="Nhập size tùy chỉnh" disabled
                                               style="margin-top: 0.5rem;">
                                    </div>
                                </div>
                                
                                <label for="imageInput" style="margin-top: 1rem;">Hình ảnh sản phẩm</label>
                                <div class="image-upload-container">
                                    <!-- Drag & Drop Area -->
                                    <div id="imagePlaceholder" class="image-placeholder">
                                        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="2">
                                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                            <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                            <polyline points="21 15 16 10 5 21"></polyline>
                                        </svg>
                                        <p>Kéo thả ảnh vào đây hoặc nhấn để chọn</p>
                                    </div>
                                    
                                    <!-- Image Preview Container (with close button overlay) -->
                                    <div id="imagePreviewContainer" class="image-preview-container" style="display: none; position: relative; width: fit-content; margin: 0 auto;">
                                        <button type="button" id="removeImageBtn" onclick="removeImage()" class="image-close-btn">×</button>
                                        <img id="imagePreview" src="" alt="Preview" class="image-preview">
                                    </div>
                                    
                                    <!-- URL Input -->
                                    <div class="url-input-container" style="margin-top: 10px;">
                                        <input type="text" id="imageUrl" name="imageUrl" 
                                               placeholder="Hoặc nhập URL hình ảnh (https://...)" 
                                               style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                            </div>
                                    
                                    <!-- File Input (Hidden) -->
                                    <input type="file" id="imageInput" name="imageFile" accept="image/*" style="display: none;">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="stock">Số lượng tồn kho *</label>
                                <input type="number" id="stock" name="stock" required 
                                       min="0" max="10000" placeholder="Nhập số lượng tồn kho (0-10000)">
                                
                                <label for="unit" style="margin-top: 1rem;">Đơn vị tính *</label>
                                <div style="display: flex; gap: 0.5rem; align-items: stretch;">
                                    <select id="unit" name="unit" required style="flex: 1;">
                                        <option value="">-- Chọn đơn vị tính --</option>
                                        <c:forEach var="u" items="${units}">
                                            <option value="${u}" data-unit="${u}">${u}</option>
                                        </c:forEach>
                                    </select>
                                    <button type="button" id="deleteUnitBtn" onclick="deleteCurrentUnit()" 
                                            style="padding: 0; min-width: 45px; max-width: 45px; width: 45px; height: 45px; flex-shrink: 0; display: none; align-items: center; justify-content: center; overflow: hidden; background: #dc3545; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 24px; font-weight: bold; line-height: 1;" 
                                            title="Xóa đơn vị tính này">
                                        ×
                                    </button>
                                    <button type="button" onclick="showAddUnitModal()" 
                                            style="padding: 0; min-width: 45px; max-width: 45px; width: 45px; height: 45px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; overflow: hidden; background: linear-gradient(135deg, #667eea, #00c6ff); color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 24px; font-weight: bold; line-height: 1;" 
                                            title="Thêm đơn vị tính mới">
                                        +
                                    </button>
                            </div>
                                
                                <label for="status" style="margin-top: 1rem;">Trạng thái *</label>
                                <select id="status" name="status" required>
                                    <option value="Đang bán">Đang bán</option>
                                    <option value="Hết hàng">Hết hàng</option>
                                    <option value="Dừng bán">Dừng bán</option>
                                </select>
                                
                                <label for="description" style="margin-top: 1rem;">Mô tả sản phẩm</label>
                                <textarea id="description" name="description"
                                      placeholder="Nhập mô tả chi tiết về sản phẩm (tùy chọn)" rows="8"></textarea>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-warning" onclick="closeAddProductModal()">
                            ❌ Hủy
                        </button>
                        <button type="button" class="btn btn-success" onclick="submitAddProduct(event)">
                            ✅ Thêm sản phẩm
                        </button>
                    </div>
                </form>
            </div>
        </div>

<!-- Add Category Modal -->
<div id="addCategoryModal" class="modal">
    <div class="modal-content" style="max-width: 400px;">
        <div class="modal-header">
            <h2>➕ Thêm danh mục mới</h2>
            <span class="close" onclick="closeAddCategoryModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="form-group">
                <label for="newCategoryName">Tên danh mục *</label>
                <input type="text" id="newCategoryName" name="newCategoryName" 
                       placeholder="Nhập tên danh mục">
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-warning" onclick="closeAddCategoryModal()">
                ❌ Hủy
            </button>
            <button type="button" class="btn btn-success" onclick="submitAddCategory()">
                ✅ Thêm danh mục
            </button>
        </div>
    </div>
</div>

<!-- Add Unit Modal -->
<div id="addUnitModal" class="modal">
    <div class="modal-content" style="max-width: 400px;">
        <div class="modal-header">
            <h2>➕ Thêm đơn vị tính mới</h2>
            <span class="close" onclick="closeAddUnitModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="form-group">
                <label for="newUnitName">Tên đơn vị tính *</label>
                <input type="text" id="newUnitName" name="newUnitName" 
                       placeholder="Nhập tên đơn vị tính">
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-warning" onclick="closeAddUnitModal()">
                ❌ Hủy
            </button>
            <button type="button" class="btn btn-success" onclick="submitAddUnit()">
                ✅ Thêm đơn vị tính
            </button>
        </div>
    </div>
</div>

<!-- Delete Product Confirmation Modal -->
<div id="deleteProductConfirmModal" class="modal">
    <div class="modal-content delete-confirm-modal">
        <div class="modal-header">
            <h2>Xác nhận xóa sản phẩm</h2>
            <span class="close" onclick="closeDeleteProductConfirmModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="delete-warning">
                <div class="warning-icon">⚠️</div>
                <div class="warning-content">
                    <h3>Bạn có chắc chắn muốn xóa sản phẩm này?</h3>
                    <p class="product-name-to-delete" id="productNameToDelete"></p>
                    <div class="warning-details">
                        <p><strong>Lưu ý:</strong></p>
                        <ul>
                            <li>Tất cả variant và stock của sản phẩm sẽ bị xóa</li>
                            <li>Hình ảnh sản phẩm sẽ bị xóa khỏi server</li>
                            <li>Hành động này không thể hoàn tác</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" onclick="closeDeleteProductConfirmModal()">
                Hủy
            </button>
            <button type="button" class="btn btn-danger" id="confirmDeleteProductBtn" onclick="confirmDeleteProduct()">
                Xóa sản phẩm
            </button>
        </div>
            </div>
        </div>

<!-- Delete Category Confirmation Modal -->
<div id="deleteCategoryConfirmModal" class="modal">
    <div class="modal-content delete-confirm-modal">
        <div class="modal-header">
            <h2>Xác nhận xóa danh mục</h2>
            <span class="close" onclick="closeDeleteCategoryConfirmModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="delete-warning">
                <div class="warning-icon">⚠️</div>
                <div class="warning-content">
                    <h3>Bạn có chắc chắn muốn xóa danh mục này?</h3>
                    <p class="category-name-to-delete" id="categoryNameToDelete"></p>
                    <div class="warning-details">
                        <p><strong>Cảnh báo:</strong></p>
                        <ul>
                            <li>Danh mục sẽ bị xóa khỏi hệ thống</li>
                            <li>Các sản phẩm thuộc danh mục này sẽ không còn danh mục</li>
                            <li>Hành động này không thể hoàn tác</li>
                        </ul>
                        <p id="affectedProductsCount" class="affected-count"></p>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" onclick="closeDeleteCategoryConfirmModal()">
                Hủy
            </button>
            <button type="button" class="btn btn-danger" id="confirmDeleteCategoryBtn" onclick="confirmDeleteCategory()">
                Xóa danh mục
            </button>
        </div>
    </div>
</div>

<!-- Delete Unit Confirmation Modal -->
<div id="deleteUnitConfirmModal" class="modal">
    <div class="modal-content delete-confirm-modal">
        <div class="modal-header">
            <h2>Xác nhận xóa đơn vị tính</h2>
            <span class="close" onclick="closeDeleteUnitConfirmModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="delete-warning">
                <div class="warning-icon">⚠️</div>
                <div class="warning-content">
                    <h3>Bạn có chắc chắn muốn xóa đơn vị tính này?</h3>
                    <p class="unit-name-to-delete" id="unitNameToDelete"></p>
                    <div class="warning-details">
                        <p><strong>Cảnh báo:</strong></p>
                        <ul>
                            <li>Đơn vị tính sẽ bị xóa khỏi danh sách</li>
                            <li>Các sản phẩm sử dụng đơn vị tính này sẽ không có đơn vị tính</li>
                            <li>Hành động này không thể hoàn tác</li>
                        </ul>
                        <p id="affectedProductsByUnit" class="affected-count"></p>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" onclick="closeDeleteUnitConfirmModal()">
                Hủy
            </button>
            <button type="button" class="btn btn-danger" id="confirmDeleteUnitBtn" onclick="confirmDeleteUnit()">
                Xóa đơn vị tính
            </button>
        </div>
    </div>
</div>

<!-- Import Excel Modal -->
<div id="importExcelModal" class="modal">
    <div class="modal-content" style="max-width: 600px; max-height: 90vh; overflow-y: auto;">
        <div class="modal-header">
            <h2>Nhập dữ liệu sản phẩm từ Excel</h2>
            <span class="close" onclick="closeImportModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="import-instructions">
                <h3>Hướng dẫn nhập dữ liệu</h3>
                <div class="instruction-content">
                    <div class="instruction-section">
                        <h4>Cấu trúc file Excel:</h4>
                        <ul>
                            <li><strong>Cột A:</strong> Tên sản phẩm (bắt buộc)</li>
                            <li><strong>Cột B:</strong> Mô tả sản phẩm (tùy chọn)</li>
                            <li><strong>Cột C:</strong> Loại hàng <span style="color: red;">CHỈ</span>: Hàng hóa thường / Chế biến / Dịch vụ / Combo (bắt buộc, chính xác)</li>
                            <li><strong>Cột D:</strong> Size (S/M/L hoặc tùy chỉnh)</li>
                            <li><strong>Cột E:</strong> Giá bán (bắt buộc, >= 1000)</li>
                            <li><strong>Cột F:</strong> Số lượng tồn kho (bắt buộc, 0-10000)</li>
                            <li><strong>Cột G:</strong> Trạng thái <span style="color: red;">CHỈ</span>: Đang bán / Hết hàng / Dừng bán (bắt buộc, chính xác)</li>
                            <li><strong>Cột H:</strong> Đơn vị tính (Ly/Cái/Miếng/Phần - hoặc đơn vị mới)</li>
                            <li><strong>Cột I:</strong> Danh mục (tùy chọn)</li>
                        </ul>
                        <div class="template-download">
                            <button type="button" class="btn btn-outline-primary btn-sm" onclick="downloadTemplate('products')">
                                📥 Tải về mẫu sản phẩm
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="file-upload-section">
                <div class="file-upload-area" id="fileUploadArea">
                    <div class="upload-icon">📁</div>
                    <div class="upload-text">
                        <h4>Kéo thả file Excel vào đây hoặc</h4>
                        <button type="button" class="btn btn-primary" onclick="document.getElementById('excelFile').click()">
                            Chọn file Excel
                        </button>
                        <p class="file-info">Hỗ trợ định dạng: .xlsx, .xls</p>
                    </div>
                </div>
                <input type="file" id="excelFile" accept=".xlsx,.xls" style="display: none;" onchange="handleFileSelect(event)">
                
                <div class="file-preview" id="filePreview" style="display: none;">
                    <div class="preview-content">
                        <div class="file-icon">📊</div>
                        <div class="file-details">
                            <div class="file-name" id="fileName"></div>
                            <div class="file-size" id="fileSize"></div>
                        </div>
                        <button type="button" class="btn btn-danger btn-sm" onclick="removeFile()">Xóa</button>
                    </div>
                </div>
            </div>
            
            <div class="import-options">
                <h4>Tùy chọn nhập:</h4>
                <div class="option-group">
                    <label class="checkbox-label">
                        <input type="checkbox" id="skipDuplicates" checked>
                        Bỏ qua sản phẩm trùng lặp
                    </label>
                    <label class="checkbox-label">
                        <input type="checkbox" id="validateData" checked>
                        Kiểm tra tính hợp lệ của dữ liệu
                    </label>
                </div>
            </div>
            
            <div class="import-progress" id="importProgress" style="display: none;">
                <div class="progress-bar">
                    <div class="progress-fill" id="progressFill"></div>
                </div>
                <div class="progress-text" id="progressText">Đang xử lý...</div>
            </div>
            
            <div class="import-results" id="importResults" style="display: none;">
                <h4>Kết quả nhập dữ liệu:</h4>
                <div class="result-summary" id="resultSummary"></div>
                <div class="result-details" id="resultDetails"></div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-warning" onclick="closeImportModal()">
                Hủy
            </button>
            <button type="button" class="btn btn-primary" id="checkBtn" onclick="checkFile()" disabled>
                Kiểm tra file
            </button>
            <button type="button" class="btn btn-success" id="importBtn" onclick="startImport()" disabled style="display: none;">
                Bắt đầu nhập
            </button>
        </div>
            </div>
        </div>

<jsp:include page="../includes/footer.jsp" />
