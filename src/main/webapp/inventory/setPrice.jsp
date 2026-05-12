<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:include page="../includes/header.jsp">
  <jsp:param name="page" value="setprice" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/productlist.css">

<script>
    // Define pageContext for JS
    window.pageContext = {
        request: {
            contextPath: '${pageContext.request.contextPath}'
        }
    };
</script>
<script src="${pageContext.request.contextPath}/js/setprice-enhanced.js"></script>

<div class="content">
    <!-- Statistics -->
    <div class="stats">
        <div class="stat-card">
            <div class="stat-number">${productPrices.size()}</div>
            <div class="stat-label">Tổng sản phẩm</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">
                <c:choose>
                    <c:when test="${not empty productPrices and productPrices.size() > 0}">
                        <c:set var="totalOriginalPrice" value="0" />
                        <c:forEach var="p" items="${productPrices}">
                            <c:set var="totalOriginalPrice" value="${totalOriginalPrice + (p.originalPrice != null ? p.originalPrice : 0)}" />
                        </c:forEach>
                        <c:set var="avgOriginalPrice" value="${totalOriginalPrice / productPrices.size()}" />
                        <fmt:formatNumber value="${avgOriginalPrice}" pattern="#,###" var="avgOriginalFormatted"/>
                        ${fn:replace(avgOriginalFormatted, ',', '.')} ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                </c:choose>
            </div>
            <div class="stat-label">Giá vốn trung bình</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">
                <c:choose>
                    <c:when test="${not empty productPrices and productPrices.size() > 0}">
                        <c:set var="totalSellingPrice" value="0" />
                    <c:forEach var="p" items="${productPrices}">
                            <c:set var="totalSellingPrice" value="${totalSellingPrice + (p.sellingPrice != null ? p.sellingPrice : 0)}" />
                    </c:forEach>
                        <c:set var="avgSellingPrice" value="${totalSellingPrice / productPrices.size()}" />
                        <fmt:formatNumber value="${avgSellingPrice}" pattern="#,###" var="avgSellingFormatted"/>
                        ${fn:replace(avgSellingFormatted, ',', '.')} ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                </c:choose>
            </div>
            <div class="stat-label">Giá bán trung bình</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">
                <c:choose>
                    <c:when test="${not empty productPrices and productPrices.size() > 0}">
                        <c:set var="totalProfit" value="0" />
                <c:forEach var="p" items="${productPrices}">
                            <c:set var="profit" value="${(p.sellingPrice != null ? p.sellingPrice : 0) - (p.originalPrice != null ? p.originalPrice : 0)}" />
                            <c:set var="totalProfit" value="${totalProfit + profit}" />
                </c:forEach>
                        <c:set var="avgProfit" value="${totalProfit / productPrices.size()}" />
                        <fmt:formatNumber value="${avgProfit}" pattern="#,###" var="avgProfitFormatted"/>
                        ${fn:replace(avgProfitFormatted, ',', '.')} ₫
                    </c:when>
                    <c:otherwise>0 ₫</c:otherwise>
                </c:choose>
            </div>
            <div class="stat-label">Lợi nhuận trung bình</div>
        </div>
    </div>

    <!-- Success/Error Messages -->
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
        <!-- Left Sidebar - Price Filters -->
        <div class="sidebar">
            <div class="filter-section">
                <h3 class="filter-title">Tìm kiếm</h3>
                <div class="search-box">
                    <input type="text" class="search-input" placeholder="Theo mã, tên hàng" id="searchInput" onkeyup="searchPrices()">
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
                                    <input type="checkbox" name="categoryFilter" value="${category}" onchange="filterPrices()">
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
                    Khoảng giá bán
                    <span class="collapse-icon">▼</span>
                </h3>
                <div class="filter-options collapsed">
                    <label class="filter-option">
                        <input type="checkbox" name="priceFilter" value="below10000" onchange="filterPrices()">
                        <span class="checkmark"></span>
                        Dưới 10,000 ₫
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="priceFilter" value="10000-50000" onchange="filterPrices()">
                        <span class="checkmark"></span>
                        10,000 - 50,000 ₫
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="priceFilter" value="above50000" onchange="filterPrices()">
                        <span class="checkmark"></span>
                        Trên 50,000 ₫
                    </label>
                </div>
            </div>

            <div class="filter-section">
                <h3 class="filter-title collapsible" onclick="toggleFilterSection(this)">
                    Mức lợi nhuận
                    <span class="collapse-icon">▼</span>
                </h3>
                <div class="filter-options collapsed">
                    <label class="filter-option">
                        <input type="checkbox" name="profitFilter" value="negative" onchange="filterPrices()">
                        <span class="checkmark"></span>
                        Lỗ (âm)
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="profitFilter" value="low" onchange="filterPrices()">
                        <span class="checkmark"></span>
                        Thấp (< 20%)
                    </label>
                    <label class="filter-option">
                        <input type="checkbox" name="profitFilter" value="high" onchange="filterPrices()">
                        <span class="checkmark"></span>
                        Cao (>= 20%)
                    </label>
                </div>
            </div>
        </div>

        <!-- Right Content - Price List -->
        <div class="main-content">
            <!-- Toolbar -->
            <div class="toolbar">
                <div>
                    <button class="btn btn-success" onclick="saveAllPrices()">Lưu hàng loạt</button>
                    <button class="btn btn-success" onclick="exportPriceReport()">Xuất báo cáo giá</button>
                    <button class="btn btn-success" onclick="importPrices()">Cập nhật hàng loạt</button>
                </div>
            </div>

            <!-- Price Table -->
            <div class="price-table">
                <c:choose>
                    <c:when test="${empty productPrices}">
                        <div class="empty-state">
                            <h3>💰 Chưa có sản phẩm nào để thiết lập giá</h3>
                            <p>Hãy thêm sản phẩm trước để có thể thiết lập giá</p>
                            <a href="${pageContext.request.contextPath}/products" class="btn btn-success" style="margin-top: 1rem;">📦 Quản lý sản phẩm</a>
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
                                        Giá vốn
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th class="sortable" onclick="sortTable(5, 'number')">
                                        Giá bán
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th class="sortable" onclick="sortTable(6, 'number')">
                                        Lợi nhuận
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th class="sortable" onclick="sortTable(7, 'number')">
                                        % LN
                                        <span class="sort-icon"></span>
                                    </th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${productPrices}">
                                    <tr class="price-row"
                                        data-product-id="${p.productId}"
                                        data-product-code="${p.productCode}"
                                        data-product-name="${p.productName}"
                                        data-size="${p.size}"
                                        data-category="${p.categoryName}"
                                        data-original-price="${p.originalPrice}"
                                        data-selling-price="${p.sellingPrice}">
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
                                            <div style="display: flex; align-items: center;">
                                                <c:set var="originalInt" value="${Math.round(p.originalPrice != null ? p.originalPrice : 0)}" />
                                                <fmt:formatNumber value="${originalInt}" pattern="#,###" var="originalFormatted"/>
                                                <input type="text" 
                                                       class="price-input original-price price-formatted" 
                                                       data-product-id="${p.productId}" 
                                                       data-size="${p.size}"
                                                       data-original-value="${originalInt}"
                                                       value="${fn:replace(originalFormatted, ',', '.')}"
                                                       placeholder="0"
                                                       onfocus="this.select()"
                                                       onblur="formatPriceInput(this, 'original-price')"
                                                       onkeypress="return allowOnlyNumbers(event)">
                                                <span style="margin-left: 4px; color: #666;">₫</span>
                                            </div>
                                        </td>
                                        <td>
                                            <div style="display: flex; align-items: center;">
                                                <c:set var="sellingInt" value="${Math.round(p.sellingPrice != null ? p.sellingPrice : 0)}" />
                                                <fmt:formatNumber value="${sellingInt}" pattern="#,###" var="sellingFormatted"/>
                                                <input type="text" 
                                                       class="price-input selling-price price-formatted" 
                                                       data-product-id="${p.productId}" 
                                                       data-size="${p.size}"
                                                       data-original-value="${sellingInt}"
                                                       value="${fn:replace(sellingFormatted, ',', '.')}"
                                                       placeholder="0"
                                                       onfocus="this.select()"
                                                       onblur="formatPriceInput(this, 'selling-price')"
                                                       onkeypress="return allowOnlyNumbers(event)">
                                                <span style="margin-left: 4px; color: #666;">₫</span>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="profit-amount">
                                                <fmt:formatNumber 
                                                    value="${(p.sellingPrice != null ? p.sellingPrice : 0) - (p.originalPrice != null ? p.originalPrice : 0)}" 
                                                    pattern="#,###" /> ₫
                                            </span>
                                        </td>
                                        <td>
                                            <span class="profit-percent">
                                                <c:set var="originalP" value="${p.originalPrice != null ? p.originalPrice : 0}" />
                                                <c:set var="sellingP" value="${p.sellingPrice != null ? p.sellingPrice : 0}" />
                                                <c:choose>
                                                    <c:when test="${originalP > 0}">
                                                        <fmt:formatNumber 
                                                            value="${((sellingP - originalP) / originalP) * 100}" 
                                                            pattern="#,###.#" />%
                                                    </c:when>
                                                    <c:otherwise>0%</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td>
                                            <button class="btn btn-success btn-sm" 
                                                    onclick="updatePrice('${p.productId}', '${p.size}')">
                                                💾 Lưu
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        
                        <!-- Pagination -->
                        <div class="pagination-container" id="pricesPagination">
                            <div class="pagination-info" id="pricesPageInfo">
                                Trang 1 / 1
                            </div>
                            <div class="pagination-controls">
                                <button class="pagination-btn" id="pricesPrevBtn" onclick="changePricesPage(-1)" disabled>
                                    ← Trước
                                </button>
                                <div class="pagination-numbers" id="pricesPageNumbers">
                                    <span class="pagination-number active">1</span>
                                </div>
                                <button class="pagination-btn" id="pricesNextBtn" onclick="changePricesPage(1)" disabled>
                                    Sau →
                                </button>
                            </div>
                            <div class="pagination-size">
                                <label for="pricesPageSize">Hiển thị:</label>
                                <select id="pricesPageSize" onchange="changePricesPageSize(this.value)">
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

<style>
/* Price Input Styles */
.price-input {
    width: 100%;
    padding: 0.625rem 0.75rem;
    border: 1.5px solid #e0e7ff;
    border-radius: 8px;
    font-size: 0.95rem;
    font-weight: 600;
    text-align: right;
    color: #1e293b;
    background: #ffffff;
    transition: all 0.2s ease;
    font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.price-input:focus {
    outline: none;
    border-color: #0080FF;
    box-shadow: 0 0 0 3px rgba(0, 128, 255, 0.1);
    background: #ffffff;
}

.price-input:hover {
    border-color: #bfdbfe;
}

/* Giá vốn - Warm tone */
.original-price {
    background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
    border-color: #ffc895;
    color: #9a3412;
}

.original-price:focus {
    border-color: #ea580c;
    box-shadow: 0 0 0 3px rgba(234, 88, 12, 0.1);
    background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
}

.original-price:hover {
    border-color: #fdba74;
}

/* Giá bán - Cool tone */
.selling-price {
    background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
    border-color: #7dd3fc;
    color: #0c4a6e;
}

.selling-price:focus {
    border-color: #0284c7;
    box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.1);
    background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
}

.selling-price:hover {
    border-color: #38bdf8;
}

/* Profit Display */
.profit-amount {
    font-weight: 700;
    font-size: 0.95rem;
    letter-spacing: -0.01em;
    font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.profit-percent {
    font-weight: 700;
    font-size: 0.9rem;
    letter-spacing: -0.01em;
    font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

/* Table Row Styles */
.row-selected {
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.price-table {
    background: white;
    padding: 0;
    overflow: hidden;
}

/* Enhanced Table Typography */
.price-table .table {
    font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.price-table .table th {
    font-weight: 600;
    letter-spacing: -0.01em;
    font-size: 0.875rem;
    color: #334155;
}

.price-table .table td {
    font-size: 0.9rem;
    color: #475569;
    letter-spacing: -0.005em;
}

.price-table .product-name {
    font-weight: 500;
    color: #1e293b;
}

.price-table .product-code {
    font-weight: 600;
    color: #0080FF;
    font-family: 'SF Mono', 'Monaco', 'Courier New', monospace;
    font-size: 0.85rem;
}

.price-table .category {
    padding: 0.25rem 0.5rem;
    background: #f1f5f9;
    border-radius: 6px;
    font-size: 0.85rem;
    font-weight: 500;
    color: #64748b;
}

/* Cột giá vốn và giá bán co giãn theo nội dung */
.price-table .table thead th:nth-child(5),
.price-table .table tbody td:nth-child(5) {
    white-space: nowrap;
}

.price-table .table thead th:nth-child(6),
.price-table .table tbody td:nth-child(6) {
    white-space: nowrap;
}

.price-input {
    font-size: 0.875rem;
    padding: 0.5rem 0.625rem;
    min-width: 70px;
    max-width: 180px;
}
</style>

<!-- Import Excel Modal -->
<div id="importExcelModal" class="modal">
    <div class="modal-content" style="max-width: 600px; max-height: 90vh; overflow-y: auto;">
        <div class="modal-header">
            <h2>Nhập dữ liệu giá từ Excel</h2>
            <span class="close" onclick="closeImportModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="import-instructions">
                <h3>Hướng dẫn nhập dữ liệu</h3>
                <div class="instruction-content">
                    <div class="instruction-section">
                        <h4>Cấu trúc file Excel (giống file xuất báo cáo):</h4>
                        <ul>
                            <li><strong>Cột A:</strong> ProductID (bắt buộc, UUID)</li>
                            <li><strong>Cột B:</strong> Mã sản phẩm (không sử dụng khi import)</li>
                            <li><strong>Cột C:</strong> Tên sản phẩm (không sử dụng khi import)</li>
                            <li><strong>Cột D:</strong> Size (bắt buộc, ví dụ: S, M, L)</li>
                            <li><strong>Cột E:</strong> Giá vốn (bắt buộc, >= 0)</li>
                            <li><strong>Cột F:</strong> Giá bán (bắt buộc, >= 1,000)</li>
                            <li><strong>Cột G:</strong> Lợi nhuận (không sử dụng khi import)</li>
                            <li><strong>Cột H:</strong> Danh mục (không sử dụng khi import)</li>
                        </ul>
                        <p style="color: #666; font-size: 0.9rem; margin-top: 0.5rem;">
                            ✅ Bạn có thể sử dụng trực tiếp file Excel đã xuất báo cáo để cập nhật lại giá.
                        </p>
                        <div class="template-download">
                            <button type="button" class="btn btn-outline-primary btn-sm" onclick="downloadTemplate('price')">
                                📥 Tải về mẫu giá
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
                        Bỏ qua bản ghi trùng lặp
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

<!-- Save All Confirmation Modal -->
<div id="saveAllModal" class="modal">
    <div class="modal-content" style="max-width: 500px;">
        <div class="modal-header">
            <h2>💾 Xác nhận lưu hàng loạt</h2>
            <span class="close" onclick="closeSaveAllModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="confirmation-content">
                <div class="icon-large">💾</div>
                <h3>Bạn có chắc muốn lưu tất cả giá đã thay đổi?</h3>
                <div class="save-stats" id="saveStats">
                    <p>📊 Số lượng bản ghi sẽ được cập nhật: <strong id="saveCount">0</strong></p>
                </div>
                <p style="color: #64748b; margin-top: 1rem;">Hành động này sẽ cập nhật giá vốn và giá bán cho tất cả sản phẩm được chọn.</p>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" onclick="closeSaveAllModal()">
                Hủy
            </button>
            <button type="button" class="btn btn-success" onclick="confirmSaveAll()" id="confirmSaveBtn">
                ✅ Xác nhận lưu
            </button>
        </div>
    </div>
</div>

<style>
.confirmation-content {
    text-align: center;
    padding: 1.5rem 0;
}

.icon-large {
    font-size: 4rem;
    margin-bottom: 1rem;
}

.save-stats {
    background: #f0f9ff;
    border: 2px solid #0080FF;
    border-radius: 8px;
    padding: 1rem;
    margin: 1rem 0;
}

.save-stats strong {
    color: #0080FF;
    font-size: 1.2rem;
}
</style>

<jsp:include page="../includes/footer.jsp" />