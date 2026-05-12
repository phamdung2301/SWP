/**
 * Enhanced Product List Management
 * Following roomtable pattern with pagination, search, filtering
 */

// ============================================================================
// GLOBAL STATE & CONFIGURATION
// ============================================================================

const productsPagination = {
    currentPage: 1,
    pageSize: 10,
    totalItems: 0,
    totalPages: 0
};

let currentSortColumn = -1;
let currentSortDirection = 'asc';

// ============================================================================
// INITIALIZATION
// ============================================================================

document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Product List Enhanced initialized');
    
    // Initialize pagination
    initializePagination();
    
    // Initialize formatted dates
    formatAllDates();
    
    // Auto search on input
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', searchProducts);
    }
    
    // Add event listeners for filters
    document.querySelectorAll('input[name="categoryFilter"], input[name="productType"], input[name="unitFilter"], input[name="stockFilter"], input[name="statusFilter"]').forEach(checkbox => {
        checkbox.addEventListener('change', applyAllFilters);
    });
    
    // Add event listeners for category and unit dropdowns to show/hide delete buttons
    const categorySelect = document.getElementById('category');
    const unitSelect = document.getElementById('unit');
    const deleteCategoryBtn = document.getElementById('deleteCategoryBtn');
    const deleteUnitBtn = document.getElementById('deleteUnitBtn');
    
    if (categorySelect && deleteCategoryBtn) {
        categorySelect.addEventListener('change', function() {
            if (this.value && this.value !== '') {
                deleteCategoryBtn.style.display = 'flex';
            } else {
                deleteCategoryBtn.style.display = 'none';
            }
        });
    }
    
    if (unitSelect && deleteUnitBtn) {
        unitSelect.addEventListener('change', function() {
            if (this.value && this.value !== '') {
                deleteUnitBtn.style.display = 'flex';
            } else {
                deleteUnitBtn.style.display = 'none';
            }
        });
    }
    
    // Update product count
    updateProductCount();
    
    console.log('✅ Initialization complete');
});

// ============================================================================
// DATE FORMATTING
// ============================================================================

window.formatDate = function(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return 'N/A';
        
        // Format as dd/MM/yyyy HH:mm
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        
        return `${day}/${month}/${year} ${hours}:${minutes}`;
    } catch (error) {
        console.warn('Error formatting date:', error);
        return 'N/A';
    }
};

window.formatNumber = function(number) {
    if (!number || isNaN(number)) return '0';
    return Number(number).toLocaleString('vi-VN');
};

function formatAllDates() {
    const dateElements = document.querySelectorAll('.formatted-date[data-date]');
    dateElements.forEach(el => {
        const dateStr = el.getAttribute('data-date');
        if (dateStr) {
            const formattedDate = window.formatDate(dateStr);
            el.textContent = formattedDate;
        }
    });
}

// ============================================================================
// PAGINATION
// ============================================================================

function initializePagination() {
    console.log('🔢 Initializing pagination...');
    
    const productsContainer = document.querySelector('.product-table');
    if (!productsContainer) {
        console.log('⚠️ No product table container found');
        return;
    }
    
    const emptyState = productsContainer.querySelector('.empty-state');
    const isEmptyStateVisible = emptyState && emptyState.style.display !== 'none';
    
    if (isEmptyStateVisible) {
        console.log('🔢 Products in empty state, skipping pagination initialization');
        productsPagination.totalItems = 0;
        productsPagination.totalPages = 0;
        productsPagination.currentPage = 1;
        return;
    }
    
    const tbody = productsContainer.querySelector('.table tbody');
    if (!tbody) {
        console.log('⚠️ No tbody found');
        return;
    }
    
    const rows = tbody.querySelectorAll('tr[data-product-id]');
    productsPagination.totalItems = rows.length;
    productsPagination.totalPages = Math.ceil(productsPagination.totalItems / productsPagination.pageSize);
    
    console.log(`📊 Total products: ${productsPagination.totalItems}, Pages: ${productsPagination.totalPages}`);
    
    updatePaginationUI();
    showProductsPage(1);
}

function showProductsPage(page) {
    const productsContainer = document.querySelector('.product-table');
    if (!productsContainer) return;
    
    const emptyState = productsContainer.querySelector('.empty-state');
    const isEmptyStateVisible = emptyState && emptyState.style.display !== 'none';
    
    if (isEmptyStateVisible) {
        console.log('📄 Products in empty state, skipping page show');
        return;
    }
    
    const tbody = productsContainer.querySelector('.table tbody');
    if (!tbody) return;
    
    const rows = tbody.querySelectorAll('tr[data-product-id]');
    
    // Validate page number
    if (page < 1) page = 1;
    if (page > productsPagination.totalPages) page = productsPagination.totalPages;
    
    productsPagination.currentPage = page;
    
    // Calculate range
    const startIndex = (page - 1) * productsPagination.pageSize;
    const endIndex = startIndex + productsPagination.pageSize;
    
    // Show/hide rows
    rows.forEach((row, index) => {
        if (index >= startIndex && index < endIndex) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
    
    updatePaginationUI();
}

function changeProductsPage(delta) {
    const newPage = productsPagination.currentPage + delta;
    if (newPage >= 1 && newPage <= productsPagination.totalPages) {
        showProductsPage(newPage);
    }
}

function changeProductsPageSize(newSize) {
    productsPagination.pageSize = parseInt(newSize);
    productsPagination.totalPages = Math.ceil(productsPagination.totalItems / productsPagination.pageSize);
    showProductsPage(1);
}

function updatePaginationUI() {
    const paginationContainer = document.getElementById('productsPagination');
    if (!paginationContainer) return;
    
    // Update page info
    const pageInfo = document.getElementById('productsPageInfo');
    if (pageInfo) {
        pageInfo.textContent = `Trang ${productsPagination.currentPage} / ${productsPagination.totalPages || 1}`;
    }
    
    // Update buttons
    const prevBtn = document.getElementById('productsPrevBtn');
    const nextBtn = document.getElementById('productsNextBtn');
    
    if (prevBtn) {
        prevBtn.disabled = productsPagination.currentPage <= 1;
    }
    
    if (nextBtn) {
        nextBtn.disabled = productsPagination.currentPage >= productsPagination.totalPages;
    }
    
    // Update page numbers
    updatePageNumbers();
}

function updatePageNumbers() {
    const pageNumbersContainer = document.getElementById('productsPageNumbers');
    if (!pageNumbersContainer) return;
    
    pageNumbersContainer.innerHTML = '';
    
    const maxVisible = 5;
    let startPage = Math.max(1, productsPagination.currentPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(productsPagination.totalPages, startPage + maxVisible - 1);
    
    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(1, endPage - maxVisible + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        const span = document.createElement('span');
        span.className = 'pagination-number' + (i === productsPagination.currentPage ? ' active' : '');
        span.textContent = i;
        span.onclick = () => showProductsPage(i);
        pageNumbersContainer.appendChild(span);
    }
}

// ============================================================================
// SEARCH & FILTER
// ============================================================================

function searchProducts() {
    applyAllFilters();
}

function filterProducts() {
    applyAllFilters();
}

function applyAllFilters() {
    const searchTerm = document.getElementById('searchInput')?.value.toLowerCase().trim() || '';
    const categoryFilters = Array.from(document.querySelectorAll('input[name="categoryFilter"]:checked')).map(cb => cb.value);
    const productTypeFilters = Array.from(document.querySelectorAll('input[name="productType"]:checked')).map(cb => cb.value);
    const unitFilters = Array.from(document.querySelectorAll('input[name="unitFilter"]:checked')).map(cb => cb.value);
    const stockFilters = Array.from(document.querySelectorAll('input[name="stockFilter"]:checked')).map(cb => cb.value);
    const statusFilters = Array.from(document.querySelectorAll('input[name="statusFilter"]:checked')).map(cb => cb.value);
    
    const tbody = document.querySelector('.table tbody');
    if (!tbody) return;
    
    const rows = tbody.querySelectorAll('tr[data-product-id]');
    
    console.log('🔍 Applying filters...', {
        searchTerm,
        categoryFilters,
        productTypeFilters,
        unitFilters,
        stockFilters,
        statusFilters,
        totalRows: rows.length
    });
    
    let visibleCount = 0;
    
    rows.forEach((row, index) => {
        let showRow = true;
        
        // Apply search filter
        if (searchTerm !== '') {
            const productCode = (row.dataset.productCode || '').toLowerCase();
            const productName = (row.dataset.productName || '').toLowerCase();
            const size = (row.dataset.size || '').toLowerCase();
            const category = (row.dataset.category || '').toLowerCase();
            
            if (!productCode.includes(searchTerm) && 
                !productName.includes(searchTerm) && 
                !size.includes(searchTerm) &&
                !category.includes(searchTerm)) {
                showRow = false;
            }
        }
        
        // Apply category filter
        if (showRow && categoryFilters.length > 0) {
            const category = (row.dataset.category || '').trim();
            if (!categoryFilters.includes(category)) {
                showRow = false;
            }
        }
        
        // Apply product type filter
        if (showRow && productTypeFilters.length > 0) {
            const productType = (row.dataset.productType || '').trim();
            
            // Map checkbox values to actual product types from database
            const typeMapping = {
                'regular': 'Hàng hóa thường',
                'processed': 'Chế biến',
                'service': 'Dịch vụ',
                'combo': 'Combo',
                'custom-combo': 'Combo'
            };
            
            // Check if any selected filter matches the product type
            const matches = productTypeFilters.some(filterValue => {
                const mappedType = typeMapping[filterValue] || filterValue;
                return productType === mappedType;
            });
            
            if (!matches) {
                showRow = false;
            }
        }
        
        // Apply unit filter
        if (showRow && unitFilters.length > 0) {
            const unit = (row.dataset.unit || '').trim();
            if (!unitFilters.includes(unit)) {
                showRow = false;
            }
        }
        
        // Apply stock filter
        if (showRow && stockFilters.length > 0) {
            const stock = parseInt(row.dataset.stock || '0');
            const matches = stockFilters.some(filterValue => {
                if (filterValue === '0') return stock === 0;
                if (filterValue === 'above0') return stock > 0;
                if (filterValue === 'above100') return stock > 100;
                return false;
            });
            if (!matches) {
                showRow = false;
            }
        }
        
        // Apply status filter
        if (showRow && statusFilters.length > 0) {
            const status = (row.dataset.status || '').trim();
            if (!statusFilters.includes(status)) {
                showRow = false;
            }
        }
        
        row.style.display = showRow ? '' : 'none';
        if (showRow) visibleCount++;
    });
    
    console.log(`✅ Filtering completed. Visible: ${visibleCount}/${rows.length}`);
    
    // Re-calculate pagination for visible items
    recalculatePaginationAfterFilter();
}

function recalculatePaginationAfterFilter() {
    const tbody = document.querySelector('.table tbody');
    if (!tbody) return;
    
    const visibleRows = Array.from(tbody.querySelectorAll('tr[data-product-id]')).filter(row => row.style.display !== 'none');
    
    productsPagination.totalItems = visibleRows.length;
    productsPagination.totalPages = Math.ceil(productsPagination.totalItems / productsPagination.pageSize);
    productsPagination.currentPage = 1;
    
    // Show first page of filtered results
    const startIndex = 0;
    const endIndex = productsPagination.pageSize;
    
    visibleRows.forEach((row, index) => {
        if (index >= startIndex && index < endIndex) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
    
    updatePaginationUI();
}

// ============================================================================
// SORTING
// ============================================================================

function sortTable(columnIndex, dataType) {
    const table = document.querySelector('.table');
    if (!table) return;
    
    const tbody = table.querySelector('tbody');
    if (!tbody) return;
    
    const rows = Array.from(tbody.querySelectorAll('tr[data-product-id]'));
    
    // Clear old sort classes
    document.querySelectorAll('.table th').forEach(th => {
        th.classList.remove('sort-asc', 'sort-desc');
    });
    
    // Determine sort direction
    if (currentSortColumn === columnIndex) {
        currentSortDirection = currentSortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        currentSortDirection = 'asc';
    }
    currentSortColumn = columnIndex;
    
    // Add sort class to current header
    const currentHeader = document.querySelectorAll('.table th')[columnIndex];
    if (currentHeader) {
        currentHeader.classList.add(currentSortDirection === 'asc' ? 'sort-asc' : 'sort-desc');
    }
    
    // Sort rows
    rows.sort((a, b) => {
        let aValue, bValue;
        
        switch(columnIndex) {
            case 0: // Mã hàng
                aValue = a.dataset.productCode || '';
                bValue = b.dataset.productCode || '';
                break;
            case 1: // Tên hàng
                aValue = a.dataset.productName || '';
                bValue = b.dataset.productName || '';
                break;
            case 2: // Kích thước
                aValue = a.dataset.size || '';
                bValue = b.dataset.size || '';
                break;
            case 3: // Danh mục
                aValue = a.dataset.category || '';
                bValue = b.dataset.category || '';
                break;
            case 4: // Giá bán
                aValue = parseFloat(a.dataset.price || 0);
                bValue = parseFloat(b.dataset.price || 0);
                break;
            case 5: // Tồn kho
                aValue = parseFloat(a.dataset.stock || 0);
                bValue = parseFloat(b.dataset.stock || 0);
                break;
            case 6: // Trạng thái
                aValue = a.querySelector('.status')?.textContent.trim() || '';
                bValue = b.querySelector('.status')?.textContent.trim() || '';
                break;
            default:
                return 0;
        }
        
        // Compare based on data type
        let comparison = 0;
        if (dataType === 'number') {
            comparison = aValue - bValue;
        } else {
            comparison = String(aValue).localeCompare(String(bValue), 'vi', { numeric: true });
        }
        
        return currentSortDirection === 'asc' ? comparison : -comparison;
    });
    
    // Update DOM
    rows.forEach(row => tbody.appendChild(row));
    
    // Re-apply pagination
    showProductsPage(productsPagination.currentPage);
}

// ============================================================================
// PRODUCT COUNT
// ============================================================================

function updateProductCount() {
    const productsContainer = document.querySelector('.product-table');
    if (!productsContainer) return;
    
    const emptyState = productsContainer.querySelector('.empty-state');
    const isEmptyStateVisible = emptyState && emptyState.style.display !== 'none';
    
    if (isEmptyStateVisible) {
        const productCountElement = document.querySelector('.stat-card .stat-number');
        if (productCountElement) {
            productCountElement.textContent = '0';
        }
        return;
    }
    
    const tbody = productsContainer.querySelector('.table tbody');
    if (!tbody) return;
    
    const rows = tbody.querySelectorAll('tr[data-product-id]');
    const productCountElement = document.querySelector('.stat-card .stat-number');
    
    if (productCountElement) {
        productCountElement.textContent = rows.length;
    }
}

// ============================================================================
// FILTER SECTION TOGGLE
// ============================================================================

function toggleFilterSection(element) {
    const options = element.nextElementSibling;
    const icon = element.querySelector('.collapse-icon');
    
    if (options.classList.contains('collapsed')) {
        options.classList.remove('collapsed');
        icon.textContent = '▲';
    } else {
        options.classList.add('collapsed');
        icon.textContent = '▼';
    }
}

// ============================================================================
// MODAL MANAGEMENT
// ============================================================================

function addProduct() {
    const modal = document.getElementById('addProductModal');
    if (!modal) return;
    
    modal.style.display = 'block';
    document.querySelector('#addProductModal h2').textContent = '➕ Thêm sản phẩm mới';
    document.querySelector('#addProductForm input[name="action"]').value = 'create';
    document.querySelector('#addProductForm .modal-footer button.btn.btn-success').textContent = '✅ Thêm sản phẩm';
    
    // Reset hidden productId if exists
    const idInput = document.querySelector('#addProductForm input[name="productId"]');
    if (idInput) idInput.remove();
    
    // Reset form
    document.getElementById('addProductForm').reset();
    
    // Reset image preview
    const imagePreview = document.getElementById('imagePreview');
    const imagePreviewContainer = document.getElementById('imagePreviewContainer');
    const imagePlaceholder = document.getElementById('imagePlaceholder');
    const imageInput = document.getElementById('imageInput');
    const imageUrl = document.getElementById('imageUrl');
    
    if (imagePreview) imagePreview.src = '';
    if (imagePreviewContainer) imagePreviewContainer.style.display = 'none';
    if (imagePlaceholder) imagePlaceholder.style.display = 'block';
    if (imageInput) imageInput.value = '';
    if (imageUrl) imageUrl.value = '';
    
    // Reset submit button state
    const submitBtn = document.querySelector('#addProductForm .btn-success');
    if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = '✅ Thêm sản phẩm';
    }
    
    // Reset size selection (radio buttons)
    const sizeRadios = document.querySelectorAll('input[name="size"]');
    const customSizeCheck = document.getElementById('customSizeCheck');
    const customSizeInput = document.getElementById('customSizeInput');
    
    // Uncheck all size radio buttons
    sizeRadios.forEach(radio => {
        radio.checked = false;
    });
    
    // Reset custom size input
    if (customSizeInput) {
        customSizeInput.disabled = true;
        customSizeInput.value = '';
    }
    
    // Focus on first input
    setTimeout(() => {
        const firstInput = document.getElementById('name');
        if (firstInput) firstInput.focus();
    }, 100);
    
    console.log('✅ Add product modal opened');
}

function closeAddProductModal() {
    const modal = document.getElementById('addProductModal');
    if (modal) {
        modal.style.display = 'none';
        document.getElementById('addProductForm').reset();
        
        // Reset image preview
        const imagePreview = document.getElementById('imagePreview');
        const imagePreviewContainer = document.getElementById('imagePreviewContainer');
        const imagePlaceholder = document.getElementById('imagePlaceholder');
        const imageInput = document.getElementById('imageInput');
        const imageUrl = document.getElementById('imageUrl');
        
        if (imagePreview) imagePreview.src = '';
        if (imagePreviewContainer) imagePreviewContainer.style.display = 'none';
        if (imagePlaceholder) imagePlaceholder.style.display = 'block';
        if (imageInput) imageInput.value = '';
        if (imageUrl) imageUrl.value = '';
    }
}

function toggleCustomSize() {
    const customSizeCheck = document.getElementById('customSizeCheck');
    const customSizeInput = document.getElementById('customSizeInput');
    
    // Check if custom size radio is selected
    const customSizeSelected = customSizeCheck.checked;
    
    if (customSizeSelected) {
        // If custom size is selected, enable input
        customSizeInput.disabled = false;
        customSizeInput.focus();
    } else {
        // If custom size is not selected, disable input
        customSizeInput.disabled = true;
        customSizeInput.value = '';
    }
}

function submitAddProduct(event) {
    // Prevent default form submission
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    
    const form = document.getElementById('addProductForm');
    if (!form) {
        showNotification('Không tìm thấy form', 'error');
        return;
    }
    
    const formData = new FormData(form);
    const action = form.querySelector('input[name="action"]').value;
    
    // Validate required fields
    const name = formData.get('name');
    const description = formData.get('description');
    const price = formData.get('price');
    const stock = formData.get('stock');
    const productType = formData.get('productType');
    const category = formData.get('category');
    
    // Validation 1: Tên sản phẩm
    if (!name || name.trim() === '') {
        showNotification('Vui lòng nhập tên sản phẩm', 'error');
        document.getElementById('name').focus();
        return false;
    }
    
    if (name.trim().length > 100) {
        showNotification('Tên sản phẩm không được vượt quá 100 ký tự', 'error');
        document.getElementById('name').focus();
        return false;
    }
    
    // Duplicate name check removed - allow duplicate product names
    
    // Validation 2: Mô tả - KHÔNG yêu cầu nhập (optional)
    // (Removed required check for description)
    
    // Validation 3: Giá bán (cho phép >= 0)
    if (!price || price.trim() === '') {
        showNotification('Vui lòng nhập giá bán', 'error');
        document.getElementById('price').focus();
        return false;
    }
    
    // Remove formatting (dots, commas and spaces) before parsing
    const priceWithoutFormatting = price.replace(/\./g, '').replace(/,/g, '').replace(/\s/g, '');
    
    // Check if price is a valid number
    if (!/^\d+$/.test(priceWithoutFormatting)) {
        showNotification('Giá bán chỉ được chứa các chữ số', 'error');
        document.getElementById('price').focus();
        return false;
    }
    
    const priceValue = parseFloat(priceWithoutFormatting);
    
    // Check if price is 0 or less
    if (isNaN(priceValue)) {
        showNotification('Giá bán không hợp lệ', 'error');
        document.getElementById('price').focus();
        return false;
    }
    
    if (priceValue < 0) {
        showNotification('Giá bán phải lớn hơn hoặc bằng 0', 'error');
        document.getElementById('price').focus();
        return false;
    }
    
    if (priceValue === 0) {
        showNotification('Giá bán phải lớn hơn 0', 'error');
        document.getElementById('price').focus();
        return false;
    }
    
    // Check if price is too large
    if (priceValue > 100000000) {
        showNotification('Giá bán không được vượt quá 100,000,000 VND', 'error');
        document.getElementById('price').focus();
        return false;
    }
    
    // Check minimum price (should be at least 1000 VND)
    if (priceValue < 1000) {
        showNotification('Giá bán tối thiểu là 1,000 VND', 'error');
        document.getElementById('price').focus();
        return false;
    }
    
    // Validation 4: Số lượng tồn kho (0 - 10000)
    if (!stock || stock.trim() === '') {
        showNotification('Vui lòng nhập số lượng tồn kho', 'error');
        document.getElementById('stock').focus();
        return false;
    }
    
    const stockValue = parseInt(stock);
    if (isNaN(stockValue) || stockValue < 0 || stockValue > 10000) {
        showNotification('Số lượng tồn kho phải từ 0 đến 10,000', 'error');
        document.getElementById('stock').focus();
        return false;
    }
    
    // Validation 4.5: Loại hàng
    if (!productType || productType.trim() === '' || productType === '-- Chọn loại hàng --') {
        showNotification('Vui lòng chọn loại hàng', 'error');
        document.getElementById('productType').focus();
        return false;
    }
    
    // Validation 4.6: Danh mục
    if (!category || category.trim() === '' || category === '-- Chọn danh mục --') {
        showNotification('Vui lòng chọn danh mục', 'error');
        document.getElementById('category').focus();
        return false;
    }
    
    // Validation 4.7: Đơn vị tính
    const unit = document.getElementById('unit').value;
    if (!unit || unit.trim() === '' || unit === '-- Chọn đơn vị tính --') {
        showNotification('Vui lòng chọn đơn vị tính', 'error');
        document.getElementById('unit').focus();
        return false;
    }
    
    // Validation 4.8: Trạng thái
    const statusSelect = document.getElementById('status');
    if (statusSelect && (!statusSelect.value || statusSelect.value.trim() === '')) {
        showNotification('Vui lòng chọn trạng thái', 'error');
        statusSelect.focus();
        return false;
    }
    
    // Validation 4.9: Kiểm tra mâu thuẫn giữa số lượng tồn kho và trạng thái
    if (stockValue > 0 && statusSelect.value === 'Hết hàng') {
        showNotification('Không thể đặt trạng thái "Hết hàng" khi số lượng tồn kho lớn hơn 0', 'error');
        statusSelect.focus();
        return false;
    }
    
    // Validation 4.10: Tự động cập nhật trạng thái khi số lượng tồn kho = 0
    if (stockValue === 0 && statusSelect.value !== 'Hết hàng') {
        showNotification('Số lượng tồn kho = 0, trạng thái sẽ tự động chuyển thành "Hết hàng"', 'warning');
        statusSelect.value = 'Hết hàng';
    }
    
    // Validation 5: Size selection (radio buttons - only one can be selected)
    const sizeRadios = document.querySelectorAll('input[name="size"]');
    const selectedSize = document.querySelector('input[name="size"]:checked');
    const customSizeCheck = document.getElementById('customSizeCheck');
    const customSizeInput = document.getElementById('customSizeInput');
    
    // Check if no size is selected
    if (!selectedSize) {
        showNotification('Vui lòng chọn một size (S, M, L) hoặc tùy chọn nhập chữ', 'error');
        return false;
    }
    
    // If custom size is selected, validate the custom input
    if (customSizeCheck && customSizeCheck.checked) {
        if (!customSizeInput || !customSizeInput.value || customSizeInput.value.trim() === '') {
            showNotification('Vui lòng nhập size tùy chỉnh', 'error');
            if (customSizeInput) customSizeInput.focus();
            return false;
        }
        if (customSizeInput.value.trim().length > 50) {
            showNotification('Size tùy chỉnh không được vượt quá 50 ký tự', 'error');
            customSizeInput.focus();
            return false;
        }
    }
    
    // Validation 6: URL format validation removed - allow any path/URL for images
    
    // Show loading state
    const submitBtn = document.querySelector('#addProductForm .btn-success');
    if (submitBtn) {
        const originalText = submitBtn.textContent;
        submitBtn.textContent = '⏳ Đang xử lý...';
        submitBtn.disabled = true;
        
        // Reset after 5 seconds if form doesn't submit
        setTimeout(() => {
            submitBtn.textContent = originalText;
            submitBtn.disabled = false;
        }, 5000);
    }
    
    // Handle price formatting: remove dots and commas before submission
    const priceInput = document.getElementById('price');
    if (priceInput && priceInput.value) {
        priceInput.value = priceInput.value.replace(/\./g, '').replace(/,/g, '');
    }
    
    // Handle size for create action
    if (action === 'create') {
        const customSizeCheck = document.getElementById('customSizeCheck');
        const customSizeInput = document.getElementById('customSizeInput');
        const sizeChecked = document.querySelector('input[name="size"]:checked');
        
        // Remove any existing hidden size inputs
        const existingHiddenSize = form.querySelector('input[name="size"][type="hidden"]');
        if (existingHiddenSize) {
            existingHiddenSize.remove();
        }
        
        // If custom size is selected, set the custom value
        if (customSizeCheck && customSizeCheck.checked && customSizeInput && customSizeInput.value.trim() !== '') {
            // Create a hidden input with the custom size value
            const hiddenSize = document.createElement('input');
            hiddenSize.type = 'hidden';
            hiddenSize.name = 'customSize';
            hiddenSize.value = customSizeInput.value.trim();
            form.appendChild(hiddenSize);
            
            // Remove the "custom" value from the radio button by temporarily changing it
            const customRadio = document.getElementById('customSizeCheck');
            if (customRadio) {
                customRadio.disabled = true; // Disable to prevent submission of "custom"
            }
        }
    }
    
    // Submit form
    try {
        form.submit();
        
        // Close modal after successful validation (before actual submit)
        console.log('✅ Form submitted successfully');
        
    } catch (error) {
        console.error('Error submitting form:', error);
        if (submitBtn) {
            submitBtn.textContent = '✅ ' + (action === 'create' ? 'Thêm sản phẩm' : 'Cập nhật');
            submitBtn.disabled = false;
        }
        showNotification('Có lỗi xảy ra khi gửi form. Vui lòng thử lại.', 'error');
        return false;
    }
    
    return true;
}

// Open edit modal when clicking on row
document.addEventListener('click', function(e) {
    const row = e.target.closest('tr.product-row');
    if (!row) return;
    openEditModalFromRow(row);
});

function openEditModalFromRow(row) {
    const id = row.dataset.productId;
    const name = row.dataset.productName || '';
    const price = row.dataset.price || '';
    const stock = row.dataset.stock || '';
    const size = row.dataset.size || '';
    const imageUrl = row.dataset.imageUrl || '';
    const productType = row.dataset.productType || '';
    const category = row.dataset.category || '';
    const description = row.dataset.description || '';
    const status = row.dataset.status || 'Đang bán';
    const unit = row.dataset.unit || '';
    
    // Reuse addProduct modal as edit modal
    const modal = document.getElementById('addProductModal');
    if (!modal) return;
    
    modal.style.display = 'block';
    document.querySelector('#addProductModal h2').textContent = '✏️ Sửa sản phẩm';
    document.querySelector('#addProductForm input[name="action"]').value = 'update';
    
    // Add hidden productId input if not exists
    let idInput = document.querySelector('#addProductForm input[name="productId"]');
    if (!idInput) {
        idInput = document.createElement('input');
        idInput.type = 'hidden';
        idInput.name = 'productId';
        document.getElementById('addProductForm').appendChild(idInput);
    }
    idInput.value = id;
    
    // Prefill fields
    document.getElementById('name').value = name;
    // Format price with thousand separators using dots
    const priceFormatted = parseFloat(price).toLocaleString('vi-VN').replace(/,/g, '.');
    document.getElementById('price').value = priceFormatted;
    document.getElementById('stock').value = stock;
    document.getElementById('imageUrl').value = imageUrl;
    
    // Show image preview if imageUrl exists
    const imagePreview = document.getElementById('imagePreview');
    const imagePreviewContainer = document.getElementById('imagePreviewContainer');
    const imagePlaceholder = document.getElementById('imagePlaceholder');
    
    if (imageUrl && imageUrl.trim() !== '') {
        // Get full URL for preview
        const fullImageUrl = imageUrl.startsWith('http://') || imageUrl.startsWith('https://') 
            ? imageUrl 
            : `${window.location.origin}${window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/'))}${imageUrl}`;
        
        imagePreview.src = fullImageUrl;
        imagePreviewContainer.style.display = 'block';
        imagePlaceholder.style.display = 'none';
    } else {
        imagePreview.src = '';
        imagePreviewContainer.style.display = 'none';
        imagePlaceholder.style.display = 'block';
    }
    
    // Prefill productType
    const productTypeSelect = document.getElementById('productType');
    if (productTypeSelect) {
        productTypeSelect.value = productType;
    }
    
    // Prefill category
    const categorySelect = document.getElementById('category');
    if (categorySelect) {
        categorySelect.value = category;
    }
    
    // Prefill description
    const descriptionTextarea = document.getElementById('description');
    if (descriptionTextarea) {
        descriptionTextarea.value = description;
    }
    
    // Prefill unit
    const unitSelect = document.getElementById('unit');
    if (unitSelect) {
        unitSelect.value = unit;
    }
    
    // Prefill status
    const statusSelect = document.getElementById('status');
    if (statusSelect) {
        statusSelect.value = status;
    }
    
    // Size prefill
    const customSizeCheck = document.getElementById('customSizeCheck');
    const customSizeInput = document.getElementById('customSizeInput');
    const sizeCheckboxes = document.querySelectorAll('input[name="size"]');
    
    sizeCheckboxes.forEach(cb => cb.checked = false);
    
    if (size === 'S' || size === 'M' || size === 'L') {
        const target = Array.from(sizeCheckboxes).find(cb => cb.value === size);
        if (target) target.checked = true;
        customSizeCheck.checked = false;
        customSizeInput.disabled = true;
        customSizeInput.value = '';
    } else if (size) {
        customSizeCheck.checked = true;
        customSizeInput.disabled = false;
        customSizeInput.value = size;
    }
    
    // Change submit button to Update
    document.querySelector('#addProductForm .modal-footer button.btn.btn-success').textContent = '💾 Cập nhật';
}

// Hook form submit for update action
(function hookSubmit() {
    const form = document.getElementById('addProductForm');
    if (!form) return;
    
    const originalSubmit = form.submit.bind(form);
    form.submit = function() {
        const action = form.querySelector('input[name="action"]').value;
        if (action === 'update') {
            // Ensure one size value is sent
            const customSizeCheck = document.getElementById('customSizeCheck');
            const customSizeInput = document.getElementById('customSizeInput');
            const sizeChecked = document.querySelector('input[name="size"]:checked');
            let sizeValue = '';
            
            if (customSizeCheck.checked && customSizeInput.value.trim() !== '') {
                sizeValue = customSizeInput.value.trim();
            } else if (sizeChecked) {
                sizeValue = sizeChecked.value;
            }
            
            // Create/overwrite size input
            let hiddenSize = form.querySelector('input[name="size"][type="hidden"]');
            if (!hiddenSize) {
                hiddenSize = document.createElement('input');
                hiddenSize.type = 'hidden';
                hiddenSize.name = 'size';
                form.appendChild(hiddenSize);
            }
            hiddenSize.value = sizeValue;
        }
        originalSubmit();
    };
})();

// ============================================================================
// EXPORT & IMPORT
// ============================================================================

// ============================================================================
// IMPORT/EXPORT EXCEL FUNCTIONALITY
// ============================================================================

let selectedFile = null;

// Export Products to Excel
window.exportProducts = function() {
    // First check if there's data to export
    const tbody = document.querySelector('.table tbody');
    const rows = tbody ? tbody.querySelectorAll('tr[data-product-id]') : [];
    
    if (rows.length === 0) {
        showNotification('Không có dữ liệu để xuất Excel', 'warning');
        return;
    }
    
    showNotification('Đang xuất dữ liệu...', 'info');
    
    // Use fetch to check response
    fetch('products', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            action: 'exportExcel'
        })
    })
    .then(response => {
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return response.json().then(data => {
                if (!data.success) {
                    showNotification(data.message || 'Không có dữ liệu để xuất', 'error');
                }
            });
        } else {
            // It's a file download
            return response.blob().then(blob => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'danh_sach_san_pham_' + new Date().toISOString().split('T')[0] + '.xlsx';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
                showNotification('✅ Xuất Excel thành công', 'success');
            });
        }
    })
    .catch(error => {
        console.error('Export error:', error);
        showNotification('❌ Lỗi khi xuất Excel', 'error');
    });
};

// Import Products from Excel
window.importProducts = function() {
    const modal = document.getElementById('importExcelModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        resetImportModal();
        setupDragAndDrop();
        
        modal.onclick = function(event) {
            if (event.target === modal) {
                closeImportModal();
            }
        };
    }
};

// Close Import Modal
window.closeImportModal = function() {
    const modal = document.getElementById('importExcelModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        modal.onclick = null;
        resetImportModal();
    }
};

// Reset Import Modal
function resetImportModal() {
    selectedFile = null;
    const fileInput = document.getElementById('excelFile');
    const filePreview = document.getElementById('filePreview');
    const fileUploadArea = document.getElementById('fileUploadArea');
    const progress = document.getElementById('importProgress');
    const results = document.getElementById('importResults');
    const checkBtn = document.getElementById('checkBtn');
    const importBtn = document.getElementById('importBtn');
    
    if (fileInput) fileInput.value = '';
    if (filePreview) filePreview.style.display = 'none';
    if (fileUploadArea) fileUploadArea.style.display = 'block';
    if (progress) progress.style.display = 'none';
    if (results) results.style.display = 'none';
    if (checkBtn) checkBtn.disabled = true;
    if (importBtn) {
        importBtn.disabled = true;
        importBtn.style.display = 'none';
    }
}

// Setup Drag and Drop
function setupDragAndDrop() {
    const uploadArea = document.getElementById('fileUploadArea');
    if (!uploadArea) return;
    
    uploadArea.ondragover = function(e) {
        e.preventDefault();
        e.stopPropagation();
        uploadArea.classList.add('dragover');
    };
    
    uploadArea.ondragleave = function(e) {
        e.preventDefault();
        e.stopPropagation();
        uploadArea.classList.remove('dragover');
    };
    
    uploadArea.ondrop = function(e) {
        e.preventDefault();
        e.stopPropagation();
        uploadArea.classList.remove('dragover');
        
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFileSelect({ target: { files: files } });
        }
    };
}

// Handle File Selection
window.handleFileSelect = function(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    // Validate file type
    const allowedTypes = [
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
        'application/vnd.ms-excel' // .xls
    ];
    
    if (!allowedTypes.includes(file.type) && !file.name.match(/\.(xlsx|xls)$/i)) {
        showNotification('Định dạng file không hợp lệ', 'error');
        return;
    }
    
    // Store file
    selectedFile = file;
    
    // Show file preview
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');
    const filePreview = document.getElementById('filePreview');
    const fileUploadArea = document.getElementById('fileUploadArea');
    const checkBtn = document.getElementById('checkBtn');
    
    if (fileName) fileName.textContent = file.name;
    if (fileSize) {
        const size = (file.size / 1024).toFixed(2);
        fileSize.textContent = size + ' KB';
    }
    if (filePreview) filePreview.style.display = 'block';
    if (fileUploadArea) fileUploadArea.style.display = 'none';
    if (checkBtn) checkBtn.disabled = false;
};

// Remove File
window.removeFile = function() {
    selectedFile = null;
    const fileInput = document.getElementById('excelFile');
    const filePreview = document.getElementById('filePreview');
    const fileUploadArea = document.getElementById('fileUploadArea');
    const checkBtn = document.getElementById('checkBtn');
    const importBtn = document.getElementById('importBtn');
    
    if (fileInput) fileInput.value = '';
    if (filePreview) filePreview.style.display = 'none';
    if (fileUploadArea) fileUploadArea.style.display = 'block';
    if (checkBtn) checkBtn.disabled = true;
    if (importBtn) {
        importBtn.disabled = true;
        importBtn.style.display = 'none';
    }
};

// Check File
window.checkFile = function() {
    if (!selectedFile) {
        showNotification('Chưa chọn file', 'error');
        return;
    }
    
    const formData = new FormData();
    formData.append('excelFile', selectedFile);
    formData.append('action', 'checkExcel');
    formData.append('skipDuplicates', document.getElementById('skipDuplicates').checked);
    formData.append('validateData', document.getElementById('validateData').checked);
    
    const checkBtn = document.getElementById('checkBtn');
    if (checkBtn) {
        checkBtn.disabled = true;
        checkBtn.textContent = '⏳ Đang kiểm tra...';
    }
    
    fetch('products', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        showCheckResults(data);
        if (checkBtn) {
            checkBtn.disabled = false;
            checkBtn.textContent = 'Kiểm tra file';
        }
        
        const importBtn = document.getElementById('importBtn');
        if (data.success && importBtn) {
            importBtn.disabled = false;
            importBtn.style.display = 'inline-block';
        }
    })
    .catch(error => {
        console.error('Check file error:', error);
        showNotification('Lỗi khi kiểm tra file', 'error');
        if (checkBtn) {
            checkBtn.disabled = false;
            checkBtn.textContent = 'Kiểm tra file';
        }
    });
};

// Show Check Results
function showCheckResults(data) {
    const resultSummary = document.getElementById('resultSummary');
    const resultDetails = document.getElementById('resultDetails');
    const results = document.getElementById('importResults');
    
    if (!results || !resultSummary) return;
    
    // Show results section
    results.style.display = 'block';
    
    // Summary - similar to roomtable format
    let summaryHtml = `
        <div class="result-item">
            <div class="result-number">${data.totalProducts || 0}</div>
            <div class="result-label">Sản phẩm</div>
        </div>
        <div class="result-item">
            <div class="result-number">${data.totalRows || 0}</div>
            <div class="result-label">Dòng dữ liệu</div>
        </div>
        <div class="result-item">
            <div class="result-number">${data.errors ? data.errors.length : 0}</div>
            <div class="result-label">Lỗi</div>
        </div>
    `;
    resultSummary.innerHTML = summaryHtml;
    
    // Details
    if (resultDetails) {
        if (data.errors && data.errors.length > 0) {
            let detailsHtml = '<h5>Danh sách lỗi:</h5><ul>';
            data.errors.forEach(error => {
                detailsHtml += `<li class="error-item">${error}</li>`;
            });
            detailsHtml += '</ul>';
            resultDetails.innerHTML = detailsHtml;
        } else {
            resultDetails.innerHTML = '<div class="success-message">✅ File hợp lệ, có thể bắt đầu nhập dữ liệu!</div>';
        }
    }
}

// Start Import Process
window.startImport = function() {
    if (!selectedFile) {
        showNotification('Vui lòng chọn file Excel', 'error');
        return;
    }
    
    const progress = document.getElementById('importProgress');
    const results = document.getElementById('importResults');
    const importBtn = document.getElementById('importBtn');
    
    if (progress) progress.style.display = 'block';
    if (results) results.style.display = 'none';
    if (importBtn) {
        importBtn.disabled = true;
        importBtn.textContent = '⏳ Đang xử lý...';
    }
    
    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('action', 'importExcel');
    formData.append('skipDuplicates', document.getElementById('skipDuplicates').checked);
    formData.append('validateData', document.getElementById('validateData').checked);
    
    simulateProgress();
    
    fetch('products', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        hideProgress();
        showImportResults(data);
        
        if (importBtn) {
            importBtn.disabled = false;
            importBtn.textContent = '✅ Hoàn thành';
        }
    })
    .catch(error => {
        console.error('Import error:', error);
        hideProgress();
        showNotification('Lỗi khi nhập dữ liệu', 'error');
        
        if (importBtn) {
            importBtn.disabled = false;
            importBtn.textContent = 'Bắt đầu nhập';
        }
    });
};

// Simulate Progress
function simulateProgress() {
    const progressFill = document.getElementById('progressFill');
    if (!progressFill) return;
    
    let progress = 0;
    const interval = setInterval(() => {
        progress += 5;
        if (progress > 90) {
            clearInterval(interval);
        } else {
            progressFill.style.width = progress + '%';
        }
    }, 100);
}

// Hide Progress
function hideProgress() {
    const progressFill = document.getElementById('progressFill');
    if (progressFill) progressFill.style.width = '100%';
    
    setTimeout(() => {
        const progress = document.getElementById('importProgress');
        if (progress) progress.style.display = 'none';
    }, 500);
}

// Show Import Results
function showImportResults(data) {
    const results = document.getElementById('importResults');
    const resultSummary = document.getElementById('resultSummary');
    const resultDetails = document.getElementById('resultDetails');
    
    if (!results || !resultSummary) return;
    
    results.style.display = 'block';
    
    if (data.success) {
        // Summary - show import statistics
        let summaryHtml = `
            <div class="result-item">
                <div class="result-number">${data.successCount || 0}</div>
                <div class="result-label">Thành công</div>
            </div>
            <div class="result-item">
                <div class="result-number">${data.errorCount || 0}</div>
                <div class="result-label">Lỗi</div>
            </div>
            <div class="result-item">
                <div class="result-number">${data.totalProducts || data.successCount || 0}</div>
                <div class="result-label">Tổng nhập</div>
            </div>
        `;
        resultSummary.innerHTML = summaryHtml;
        
        // Details
        if (resultDetails) {
            if (data.errors && data.errors.length > 0) {
                let detailsHtml = '<h5>Danh sách lỗi:</h5><ul>';
                data.errors.forEach(error => {
                    detailsHtml += `<li class="error-item">${error}</li>`;
                });
                detailsHtml += '</ul>';
                resultDetails.innerHTML = detailsHtml;
            } else {
                resultDetails.innerHTML = '<div class="success-message">✅ Nhập dữ liệu thành công! Tất cả sản phẩm đã được thêm vào hệ thống.</div>';
            }
        }
        
        showNotification('Nhập dữ liệu thành công', 'success');
        
        // Reload page to show new data
        setTimeout(() => {
            window.location.reload();
        }, 2000);
    } else {
        // Error summary
        resultSummary.innerHTML = `
            <div class="result-item">
                <div class="result-number">0</div>
                <div class="result-label">Thành công</div>
            </div>
            <div class="result-item">
                <div class="result-number">${data.errorCount || 0}</div>
                <div class="result-label">Lỗi</div>
            </div>
            <div class="result-item">
                <div class="result-number">0</div>
                <div class="result-label">Tổng nhập</div>
            </div>
        `;
        
        // Error details
        if (resultDetails) {
            let detailsHtml = '<div class="error-message"><h4>❌ Có lỗi xảy ra</h4><p>' + (data.message || 'Vui lòng thử lại') + '</p></div>';
            if (data.errors && data.errors.length > 0) {
                detailsHtml += '<h5>Danh sách lỗi:</h5><ul>';
                data.errors.forEach(error => {
                    detailsHtml += `<li class="error-item">${error}</li>`;
                });
                detailsHtml += '</ul>';
            }
            resultDetails.innerHTML = detailsHtml;
        }
        
        showNotification('Nhập dữ liệu hoàn thành với lỗi', 'warning');
    }
}

// Download Template
window.downloadTemplate = function(type) {
    showNotification('Đang tải template...', 'info', 'Vui lòng chờ trong giây lát');
    
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = 'products';
    form.style.display = 'none';
    
    const actionInput = document.createElement('input');
    actionInput.type = 'hidden';
    actionInput.name = 'action';
    actionInput.value = 'downloadTemplate';
    
    const typeInput = document.createElement('input');
    typeInput.type = 'hidden';
    typeInput.name = 'templateType';
    typeInput.value = type;
    
    form.appendChild(actionInput);
    form.appendChild(typeInput);
    document.body.appendChild(form);
    form.submit();
    document.body.removeChild(form);
};

// ============================================================================
// MODAL CLOSE ON OUTSIDE CLICK
// ============================================================================

window.onclick = function(event) {
    const addProductModal = document.getElementById('addProductModal');
    const importModal = document.getElementById('importExcelModal');
    
    if (event.target === addProductModal) {
        closeAddProductModal();
    }
    
    if (event.target === importModal) {
        closeImportModal();
    }
};

// ============================================================================
// NOTIFICATION SYSTEM
// ============================================================================

function showNotification(message, type = 'info', title = null, duration = 3000) {
    // Create notification stack if it doesn't exist
    let stack = document.getElementById('notification-stack');
    if (!stack) {
        stack = document.createElement('div');
        stack.id = 'notification-stack';
        stack.className = 'notification-stack';
        document.body.appendChild(stack);
    }

    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    
    // Get icon based on type
    const icons = {
        success: '✅',
        error: '❌',
        warning: '⚠️',
        info: 'ℹ️'
    };
    
    const icon = icons[type] || icons.info;
    
    // Create notification content
    notification.innerHTML = `
        <div class="notification-icon">${icon}</div>
        <div class="notification-content">
            ${title ? `<div class="notification-title">${title}</div>` : ''}
            <div class="notification-message">${message}</div>
        </div>
        <div class="notification-close" onclick="removeNotification(this.parentElement)">×</div>
        <div class="notification-progress"></div>
    `;
    
    // Add to stack
    stack.appendChild(notification);
    
    // Trigger animation
    requestAnimationFrame(() => {
        notification.classList.add('show');
    });
    
    // Auto remove after duration
    setTimeout(() => {
        removeNotification(notification);
    }, duration);
    
    return notification;
}

function removeNotification(notification) {
    if (!notification || !document.body.contains(notification)) return;
    
    notification.classList.remove('show');
    notification.style.animation = 'slideOutRight 0.3s ease-in';
    
    setTimeout(() => {
        if (document.body.contains(notification)) {
            notification.remove();
        }
    }, 300);
}

// ============================================================================
// EXPOSE FUNCTIONS TO GLOBAL SCOPE
// ============================================================================

window.searchProducts = searchProducts;
window.filterProducts = filterProducts;
window.toggleFilterSection = toggleFilterSection;
window.addProduct = addProduct;
window.closeAddProductModal = closeAddProductModal;
window.toggleCustomSize = toggleCustomSize;
window.submitAddProduct = submitAddProduct;
window.exportProducts = exportProducts;
window.importProducts = importProducts;
window.sortTable = sortTable;
window.changeProductsPage = changeProductsPage;
window.changeProductsPageSize = changeProductsPageSize;
window.showNotification = showNotification;
window.removeNotification = removeNotification;

// ============================================================================
// CATEGORY MODAL MANAGEMENT
// ============================================================================

function showAddCategoryModal() {
    const modal = document.getElementById('addCategoryModal');
    if (modal) {
        modal.style.display = 'block';
        console.log('✅ Add category modal opened');
    }
}

function closeAddCategoryModal() {
    const modal = document.getElementById('addCategoryModal');
    if (modal) {
        modal.style.display = 'none';
        // Reset form
        const nameInput = document.getElementById('newCategoryName');
        if (nameInput) nameInput.value = '';
    }
}

function submitAddCategory() {
    const nameInput = document.getElementById('newCategoryName');
    
    if (!nameInput || !nameInput.value || nameInput.value.trim() === '') {
        showNotification('Vui lòng nhập tên danh mục', 'error');
        if (nameInput) nameInput.focus();
        return;
    }
    
    const categoryName = nameInput.value.trim();
    
    if (categoryName.length > 100) {
        showNotification('Tên danh mục không được vượt quá 100 ký tự', 'error');
        if (nameInput) nameInput.focus();
        return;
    }
    
    // Check for duplicate category names (case-insensitive)
    const categorySelect = document.getElementById('category');
    if (categorySelect) {
        const existingCategories = categorySelect.querySelectorAll('option');
        let duplicateFound = false;
        
        for (let existingCategory of existingCategories) {
            const existingName = existingCategory.textContent.trim();
            // Skip the default "-- Chọn danh mục --" option
            if (existingName === '-- Chọn danh mục --') continue;
            
            if (existingName.toLowerCase() === categoryName.toLowerCase()) {
                duplicateFound = true;
                break;
            }
        }
        
        if (duplicateFound) {
            showNotification('Tên danh mục "' + categoryName + '" đã tồn tại! Vui lòng chọn tên khác.', 'error');
            if (nameInput) nameInput.focus();
            return;
        }
        
        // Call API to add category to database
        fetch('products', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `action=addCategory&categoryName=${encodeURIComponent(categoryName)}`
        })
        .then(response => {
            if (response.ok) {
                return response.json ? response.json() : { status: 'success' };
            } else {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
        })
        .then(data => {
            // Add to dropdown
            const option = document.createElement('option');
            option.value = categoryName;
            option.textContent = categoryName;
            categorySelect.appendChild(option);
            categorySelect.value = categoryName; // Set the value to trigger any change listeners
            
            showNotification('Thêm danh mục "' + categoryName + '" thành công!', 'success');
            closeAddCategoryModal();
            
            // Trigger change event to update form state
            categorySelect.dispatchEvent(new Event('change', { bubbles: true }));
        })
        .catch(error => {
            console.error('Error adding category:', error);
            if (error.message.includes('already exists')) {
                showNotification('Danh mục "' + categoryName + '" đã tồn tại! Vui lòng chọn tên khác.', 'error');
            } else {
                showNotification('Lỗi khi thêm danh mục: ' + error.message, 'error');
            }
            if (nameInput) nameInput.focus();
        });
    }
}

window.showAddCategoryModal = showAddCategoryModal;
window.closeAddCategoryModal = closeAddCategoryModal;
window.submitAddCategory = submitAddCategory;

// ============================================================================
// ADD UNIT MODAL FUNCTIONS
// ============================================================================
function showAddUnitModal() {
    const modal = document.getElementById('addUnitModal');
    if (modal) {
        modal.style.display = 'block';
        console.log('✅ Add unit modal opened');
    }
}

function closeAddUnitModal() {
    const modal = document.getElementById('addUnitModal');
    if (modal) {
        modal.style.display = 'none';
        // Reset form
        const nameInput = document.getElementById('newUnitName');
        if (nameInput) nameInput.value = '';
    }
}

function submitAddUnit() {
    const nameInput = document.getElementById('newUnitName');
    
    if (!nameInput || !nameInput.value || nameInput.value.trim() === '') {
        showNotification('Vui lòng nhập tên đơn vị tính', 'error');
        if (nameInput) nameInput.focus();
        return;
    }
    
    const unitName = nameInput.value.trim();
    
    if (unitName.length > 50) {
        showNotification('Tên đơn vị tính không được vượt quá 50 ký tự', 'error');
        if (nameInput) nameInput.focus();
        return;
    }
    
    // Check for duplicate unit names (case-insensitive)
    const unitSelect = document.getElementById('unit');
    if (unitSelect) {
        const existingUnits = unitSelect.querySelectorAll('option');
        let duplicateFound = false;
        
        for (let existingUnit of existingUnits) {
            const existingName = existingUnit.textContent.trim();
            // Skip the default "-- Chọn đơn vị tính --" option
            if (existingName === '-- Chọn đơn vị tính --') continue;
            
            if (existingName.toLowerCase() === unitName.toLowerCase()) {
                duplicateFound = true;
                break;
            }
        }
        
        if (duplicateFound) {
            showNotification('Đơn vị tính "' + unitName + '" đã tồn tại! Vui lòng chọn tên khác.', 'error');
            if (nameInput) nameInput.focus();
            return;
        }
        
        // Note: Units are just text values, not entities in database
        // So we just add to the dropdown UI. When a product is created/updated with this unit,
        // it will be saved in the product's unit field
        const option = document.createElement('option');
        option.value = unitName;
        option.textContent = unitName;
        unitSelect.appendChild(option);
        unitSelect.value = unitName; // Set the value to trigger any change listeners
        
        showNotification('Thêm đơn vị tính "' + unitName + '" thành công!', 'success');
        closeAddUnitModal();
        
        // Trigger change event to update form state
        unitSelect.dispatchEvent(new Event('change', { bubbles: true }));
    }
}

window.showAddUnitModal = showAddUnitModal;
window.closeAddUnitModal = closeAddUnitModal;
window.submitAddUnit = submitAddUnit;

// ============================================================================
// IMAGE UPLOAD HANDLING
// ============================================================================

document.addEventListener('DOMContentLoaded', function() {
    initializeImageUpload();
    initializePriceFormatting();
});

function initializePriceFormatting() {
    const priceInput = document.getElementById('price');
    if (!priceInput) return;
    
    // Format number with thousand separators on input
    priceInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, ''); // Remove all non-digit characters
        
        if (value) {
            // Format with thousand separators (using dots as per Vietnamese format)
            value = parseInt(value).toLocaleString('vi-VN');
            // Use dots instead of commas
            value = value.replace(/,/g, '.');
            e.target.value = value;
        }
    });
    
    // Format number with thousand separators on blur
    priceInput.addEventListener('blur', function(e) {
        if (e.target.value) {
            e.target.value = e.target.value.replace(/\D/g, '');
            if (e.target.value) {
                e.target.value = parseInt(e.target.value).toLocaleString('vi-VN');
                // Use dots instead of commas
                e.target.value = e.target.value.replace(/,/g, '.');
            }
        }
    });
}

function initializeImageUpload() {
    const imageInput = document.getElementById('imageInput');
    const imagePlaceholder = document.getElementById('imagePlaceholder');
    const imageUrl = document.getElementById('imageUrl');
    
    if (!imageInput || !imagePlaceholder || !imageUrl) {
        console.warn('Image upload elements not found');
        return;
    }
    
    // Click on placeholder to open file picker
    imagePlaceholder.addEventListener('click', () => {
        imageInput.click();
    });
    
    // File input change
    imageInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            handleImageFileSelect(file);
        }
    });
    
    // Drag and drop
    imagePlaceholder.addEventListener('dragover', (e) => {
        e.preventDefault();
        imagePlaceholder.style.borderColor = '#0080FF';
        imagePlaceholder.style.background = '#f0f4ff';
    });
    
    imagePlaceholder.addEventListener('dragleave', (e) => {
        e.preventDefault();
        imagePlaceholder.style.borderColor = '#ddd';
        imagePlaceholder.style.background = '#f8f9fa';
    });
    
    imagePlaceholder.addEventListener('drop', (e) => {
        e.preventDefault();
        imagePlaceholder.style.borderColor = '#ddd';
        imagePlaceholder.style.background = '#f8f9fa';
        
        const file = e.dataTransfer.files[0];
        if (file && file.type.startsWith('image/')) {
            handleImageFileSelect(file);
        } else {
            showNotification('Vui lòng chọn file ảnh hợp lệ', 'error');
        }
    });
    
    // URL input change
    imageUrl.addEventListener('input', () => {
        const url = imageUrl.value.trim();
        const imagePreviewContainer = document.getElementById('imagePreviewContainer');
        
        if (url && isValidUrl(url)) {
            const imagePreview = document.getElementById('imagePreview');
            const imagePlaceholder = document.getElementById('imagePlaceholder');
            
            imagePreview.src = url;
            imagePreviewContainer.style.display = 'block';
            imagePlaceholder.style.display = 'none';
            imageInput.value = ''; // Clear file input
        }
    });
}

function handleImageFileSelect(file) {
    if (!file.type.startsWith('image/')) {
        showNotification('Vui lòng chọn file ảnh hợp lệ', 'error');
        return;
    }
    
    // Check file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
        showNotification('Kích thước file quá lớn. Vui lòng chọn file nhỏ hơn 5MB', 'error');
        return;
    }
    
    const reader = new FileReader();
    reader.onload = (e) => {
        const imagePreview = document.getElementById('imagePreview');
        const imagePreviewContainer = document.getElementById('imagePreviewContainer');
        const imagePlaceholder = document.getElementById('imagePlaceholder');
        const imageUrl = document.getElementById('imageUrl');
        
        imagePreview.src = e.target.result;
        imagePreviewContainer.style.display = 'block';
        imagePlaceholder.style.display = 'none';
        imageUrl.value = ''; // Clear URL input
    };
    reader.readAsDataURL(file);
}

function removeImage() {
    const imagePreview = document.getElementById('imagePreview');
    const imagePreviewContainer = document.getElementById('imagePreviewContainer');
    const imagePlaceholder = document.getElementById('imagePlaceholder');
    const imageInput = document.getElementById('imageInput');
    const imageUrl = document.getElementById('imageUrl');
    
    if (imagePreview) imagePreview.src = '';
    if (imagePreviewContainer) imagePreviewContainer.style.display = 'none';
    if (imagePlaceholder) imagePlaceholder.style.display = 'block';
    if (imageInput) imageInput.value = '';
    if (imageUrl) imageUrl.value = '';
}

function isValidUrl(string) {
    try {
        new URL(string);
        return true;
    } catch (_) {
        return false;
    }
}

window.removeImage = removeImage;

// ============================================================================
// EDIT AND DELETE PRODUCT
// ============================================================================

// Function to edit product
window.editProduct = function(productId, event) {
    if (event) {
        event.stopPropagation(); // Prevent row click event
    }
    
    console.log('✏️ Edit product:', productId);
    
    // Find the row with matching product ID
    const row = document.querySelector(`tr[data-product-id="${productId}"]`);
    if (row) {
        openEditModalFromRow(row);
    } else {
        showNotification('Không tìm thấy sản phẩm', 'error');
    }
};

// Global variables to store product ID and size for deletion
let productToDelete = null;
let productToDeleteSize = null;

// Function to show delete confirmation modal
window.deleteProduct = function(productId, size, event) {
    if (event) {
        event.stopPropagation(); // Prevent row click event
        event.preventDefault();
    }
    
    console.log('🗑️ Delete product requested:', productId, 'Size:', size);
    
    // Find the row to get product name and store size
    const row = document.querySelector(`tr[data-product-id="${productId}"][data-size="${size}"]`);
    const productName = row ? row.dataset.productName : 'sản phẩm';
    
    // Store product ID and size for deletion
    productToDelete = productId;
    productToDeleteSize = size;
    
    // Show modal
    const modal = document.getElementById('deleteProductConfirmModal');
    const productNameElement = document.getElementById('productNameToDelete');
    
    if (modal && productNameElement) {
        productNameElement.textContent = `Sản phẩm: ${productName}`;
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    } else {
        console.error('Delete modal not found');
    }
};

// Function to close delete confirmation modal
window.closeDeleteProductConfirmModal = function() {
    const modal = document.getElementById('deleteProductConfirmModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        productToDelete = null;
        
        // Reset button state
        const confirmBtn = document.getElementById('confirmDeleteProductBtn');
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = 'Xóa sản phẩm';
        }
        
        // Clear product name display
        const productNameElement = document.getElementById('productNameToDelete');
        if (productNameElement) {
            productNameElement.textContent = 'Sản phẩm';
        }
    }
};

// Function to confirm and execute product deletion
window.confirmDeleteProduct = async function() {
    if (!productToDelete) {
        console.error('No product ID to delete');
        return;
    }
    
    console.log('🗑️ Confirming deletion of product:', productToDelete);
    
    const modal = document.getElementById('deleteProductConfirmModal');
    const confirmBtn = document.getElementById('confirmDeleteProductBtn');
    
    if (!confirmBtn) {
        console.error('Confirm delete button not found');
        return;
    }
    
    // Disable button and show loading state
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '⏳ Đang xóa...';
    
    // Show loading notification
    showNotification('Đang xóa sản phẩm...', 'info');
    
    try {
        // Send delete request
        const response = await fetch('products', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `action=delete&productId=${productToDelete}&size=${encodeURIComponent(productToDeleteSize)}`
        });
        
        console.log('Delete response status:', response.status);
        
        if (response.ok) {
            const productName = document.getElementById('productNameToDelete').textContent;
            
            // Store productId before closing modal
            const productIdToDelete = productToDelete;
            
            // Close modal
            closeDeleteProductConfirmModal();
            
            // Remove product from DOM
            removeProductFromList(productIdToDelete);
            
            showNotification(`Đã xóa sản phẩm "${productName}" thành công`, 'success');
            
            console.log('✅ Product deleted successfully');
        } else {
            showNotification('Có lỗi xảy ra khi xóa sản phẩm', 'error');
            
            // Reset button
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = 'Xóa sản phẩm';
        }
    } catch (error) {
        console.error('Error deleting product:', error);
        showNotification('Có lỗi xảy ra khi xóa sản phẩm', 'error');
        
        // Reset button
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = 'Xóa sản phẩm';
    }
};

// Function to remove product from DOM after successful deletion
function removeProductFromList(productId) {
    const productsTableBody = document.querySelector('.product-table tbody');
    
    if (!productsTableBody) {
        console.error('Product table body not found');
        return;
    }
    
    // Find and remove ONLY the row with matching product ID AND size
    const rowsToRemove = productsTableBody.querySelectorAll(`tr[data-product-id="${productId}"][data-size="${productToDeleteSize}"]`);
    
    if (rowsToRemove && rowsToRemove.length > 0) {
        console.log(`✅ Found ${rowsToRemove.length} row(s) with data-product-id:`, productId);
        
        // Store current page before deletion
        const currentPageBeforeDelete = productsPagination.currentPage;
        
        // Animate and remove all rows
        rowsToRemove.forEach((rowToRemove, index) => {
            rowToRemove.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            rowToRemove.style.opacity = '0';
            rowToRemove.style.transform = 'translateX(-20px)';
            
            setTimeout(() => {
                if (rowToRemove.parentNode) {
                    rowToRemove.parentNode.removeChild(rowToRemove);
                    console.log(`✅ Product row ${index + 1} removed from DOM`);
                }
                
                // After all removals, re-initialize pagination and show correct page
                if (index === rowsToRemove.length - 1) {
                    // Update stat cards after a small delay to ensure DOM is updated
                    setTimeout(() => {
                        updateProductCount();
                        
                        // Re-initialize pagination with new row count
                        const remainingRows = productsTableBody.querySelectorAll('tr[data-product-id]');
                        productsPagination.totalItems = remainingRows.length;
                        productsPagination.totalPages = Math.ceil(productsPagination.totalItems / productsPagination.pageSize);
                        
                        // If current page is empty or beyond total pages, go to last available page
                        if (productsPagination.totalPages > 0) {
                            if (productsPagination.currentPage > productsPagination.totalPages) {
                                productsPagination.currentPage = productsPagination.totalPages;
                            }
                        } else {
                            productsPagination.currentPage = 1;
                        }
                        
                        // Update pagination UI
                        updatePaginationUI();
                        
                        // Re-show current page with remaining products
                        showProductsPage(productsPagination.currentPage);
                    }, 100);
                }
            }, 300 + (index * 50)); // Stagger animations slightly
        });
        
        // Check if this was the last product and show empty state
        const remainingRows = productsTableBody.querySelectorAll('tr[data-product-id]');
        if (remainingRows.length === 0) {
            console.log('📦 No products left, might need to show empty state');
            // Note: Empty state logic would go here if needed
        }
    } else {
        console.error('Could not find row to remove:', productId);
    }
}

// Function to update product count and all stat cards
function updateProductCount() {
    const tbody = document.querySelector('.product-table tbody');
    const productRows = tbody ? tbody.querySelectorAll('tr[data-product-id]') : [];
    const currentCount = productRows.length;
    
    console.log('📊 Updating stat cards...');
    console.log('Current product count:', currentCount);
    
    // Update "Tổng sản phẩm" (stat card 1 - index 0)
    const statNumbers = document.querySelectorAll('.stat-number');
    if (statNumbers.length > 0) {
        statNumbers[0].textContent = currentCount;
        console.log('✅ Updated "Tổng sản phẩm":', currentCount);
    }
    
    // Calculate and update "Tổng tồn kho" (stat card 2 - index 1)
    let totalStock = 0;
    productRows.forEach(row => {
        const stockCell = row.querySelector('td:nth-child(6)'); // Stock is in 6th column (after ProductCode, Name, Size, Category, Price)
        if (stockCell) {
            const stockText = stockCell.textContent.trim().replace(/\./g, '').replace(/,/g, ''); // Remove thousand separators
            const stock = parseInt(stockText) || 0;
            totalStock += stock;
        }
    });
    
    if (statNumbers.length > 1) {
        // Format with thousand separators (using dots as per Vietnamese format)
        const formattedStock = totalStock.toLocaleString('vi-VN').replace(/,/g, '.');
        statNumbers[1].textContent = formattedStock;
        console.log('✅ Updated "Tổng tồn kho":', formattedStock);
    }
    
    // Calculate and update "Đang bán" (stat card 3 - index 2)
    let activeProducts = 0;
    productRows.forEach(row => {
        // Check if product is active (Đang bán status)
        const statusCell = row.querySelector('td:nth-child(7)'); // Status is in 7th column
        if (statusCell) {
            const statusText = statusCell.textContent.trim();
            // Check if status contains "Đang bán" (active)
            if (statusText.includes('Đang bán')) {
                activeProducts++;
            }
        }
    });
    
    if (statNumbers.length > 2) {
        statNumbers[2].textContent = activeProducts;
        console.log('✅ Updated "Đang bán":', activeProducts);
    }
    
    console.log('📊 Stat cards updated successfully');
}

// ============================================================================
// DELETE CATEGORY AND UNIT
// ============================================================================

window.deleteCurrentCategory = function() {
    const categorySelect = document.getElementById('category');
    if (!categorySelect || !categorySelect.value) {
        showNotification('Vui lòng chọn danh mục để xóa', 'error');
        return;
    }
    
    const categoryName = categorySelect.value;
    
    // Count products in this category
    let productCount = 0;
    const productRows = document.querySelectorAll('.product-table tbody tr[data-product-id]');
    productRows.forEach(row => {
        const categoryCell = row.querySelector('td:nth-child(4)'); // Category is in 4th column
        if (categoryCell && categoryCell.textContent.trim() === categoryName) {
            productCount++;
        }
    });
    
    // Show modal
    const modal = document.getElementById('deleteCategoryConfirmModal');
    const categoryNameElement = document.getElementById('categoryNameToDelete');
    const affectedCountElement = document.getElementById('affectedProductsCount');
    
    if (modal && categoryNameElement && affectedCountElement) {
        categoryNameElement.textContent = `Danh mục: ${categoryName}`;
        if (productCount > 0) {
            affectedCountElement.innerHTML = `<strong>Số sản phẩm bị ảnh hưởng: ${productCount}</strong>`;
        } else {
            affectedCountElement.innerHTML = `<strong>Không có sản phẩm nào sử dụng danh mục này</strong>`;
        }
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Store category name globally for deletion
        window.categoryToDelete = categoryName;
    }
};

window.deleteCurrentUnit = function() {
    const unitSelect = document.getElementById('unit');
    if (!unitSelect || !unitSelect.value) {
        showNotification('Vui lòng chọn đơn vị tính để xóa', 'error');
        return;
    }
    
    const unitName = unitSelect.value;
    
    // Count products using this unit
    let productCount = 0;
    const productRows = document.querySelectorAll('.product-table tbody tr[data-product-id]');
    productRows.forEach(row => {
        const unitCell = row.querySelector('td:nth-child(9)'); // Unit is in 9th column
        if (unitCell && unitCell.textContent.trim() === unitName) {
            productCount++;
        }
    });
    
    // Show modal
    const modal = document.getElementById('deleteUnitConfirmModal');
    const unitNameElement = document.getElementById('unitNameToDelete');
    const affectedCountElement = document.getElementById('affectedProductsByUnit');
    
    if (modal && unitNameElement && affectedCountElement) {
        unitNameElement.textContent = `Đơn vị tính: ${unitName}`;
        if (productCount > 0) {
            affectedCountElement.innerHTML = `<strong>Số sản phẩm bị ảnh hưởng: ${productCount}</strong>`;
        } else {
            affectedCountElement.innerHTML = `<strong>Không có sản phẩm nào sử dụng đơn vị tính này</strong>`;
        }
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Store unit name globally for deletion
        window.unitToDelete = unitName;
    }
};

// Close Delete Category Modal
window.closeDeleteCategoryConfirmModal = function() {
    const modal = document.getElementById('deleteCategoryConfirmModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        window.categoryToDelete = null;
        
        // Reset modal content for next deletion
        const categoryNameElement = document.getElementById('categoryNameToDelete');
        const affectedCountElement = document.getElementById('affectedProductsCount');
        if (categoryNameElement) {
            categoryNameElement.textContent = '';
        }
        if (affectedCountElement) {
            affectedCountElement.innerHTML = '';
        }
        
        // Reset button state
        const confirmBtn = document.getElementById('confirmDeleteCategoryBtn');
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = 'Xóa danh mục';
        }
    }
};

// Confirm Delete Category
window.confirmDeleteCategory = function() {
    const categoryName = window.categoryToDelete;
    if (!categoryName) {
        showNotification('Không tìm thấy danh mục để xóa', 'error');
        return;
    }
    
    const categorySelect = document.getElementById('category');
    const modal = document.getElementById('deleteCategoryConfirmModal');
    const confirmBtn = document.getElementById('confirmDeleteCategoryBtn');
    
    if (confirmBtn) {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '⏳ Đang xóa...';
    }
    
    // Send delete request to server
    fetch('products', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'action=deleteCategory&categoryName=' + encodeURIComponent(categoryName)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Remove from dropdown
            const option = categorySelect.querySelector(`option[value="${categoryName}"]`);
            if (option) {
                option.remove();
            }
            
            // Reset to default
            categorySelect.value = '';
            const deleteBtn = document.getElementById('deleteCategoryBtn');
            if (deleteBtn) {
                deleteBtn.style.display = 'none';
            }
            
            closeDeleteCategoryConfirmModal();
            showNotification('Đã xóa danh mục "' + categoryName + '"', 'success');
        } else {
            showNotification('Không thể xóa danh mục. ' + (data.message || ''), 'error');
            if (confirmBtn) {
                confirmBtn.disabled = false;
                confirmBtn.innerHTML = 'Xóa danh mục';
            }
        }
    })
    .catch(error => {
        console.error('Error deleting category:', error);
        showNotification('Có lỗi xảy ra khi xóa danh mục', 'error');
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = 'Xóa danh mục';
        }
    });
};

// Close Delete Unit Modal
window.closeDeleteUnitConfirmModal = function() {
    const modal = document.getElementById('deleteUnitConfirmModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        window.unitToDelete = null;
        
        // Reset modal content for next deletion
        const unitNameElement = document.getElementById('unitNameToDelete');
        const affectedCountElement = document.getElementById('affectedProductsByUnit');
        if (unitNameElement) {
            unitNameElement.textContent = '';
        }
        if (affectedCountElement) {
            affectedCountElement.innerHTML = '';
        }
        
        // Reset button state
        const confirmBtn = document.getElementById('confirmDeleteUnitBtn');
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = 'Xóa đơn vị tính';
        }
    }
};

// Confirm Delete Unit
window.confirmDeleteUnit = function() {
    const unitName = window.unitToDelete;
    if (!unitName) {
        showNotification('Không tìm thấy đơn vị tính để xóa', 'error');
        return;
    }
    
    const unitSelect = document.getElementById('unit');
    const modal = document.getElementById('deleteUnitConfirmModal');
    const confirmBtn = document.getElementById('confirmDeleteUnitBtn');
    
    if (confirmBtn) {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '⏳ Đang xóa...';
    }
    
    // Send delete request to server
    fetch('products', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'action=deleteUnit&unitName=' + encodeURIComponent(unitName)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Remove from dropdown
            const option = unitSelect.querySelector(`option[value="${unitName}"]`);
            if (option) {
                option.remove();
            }
            
            // Reset to default
            unitSelect.value = '';
            const deleteBtn = document.getElementById('deleteUnitBtn');
            if (deleteBtn) {
                deleteBtn.style.display = 'none';
            }
            
            closeDeleteUnitConfirmModal();
            showNotification('Đã xóa đơn vị tính "' + unitName + '"', 'success');
        } else {
            showNotification('Không thể xóa đơn vị tính. ' + (data.message || ''), 'error');
            if (confirmBtn) {
                confirmBtn.disabled = false;
                confirmBtn.innerHTML = 'Xóa đơn vị tính';
            }
        }
    })
    .catch(error => {
        console.error('Error deleting unit:', error);
        showNotification('Có lỗi xảy ra khi xóa đơn vị tính', 'error');
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = 'Xóa đơn vị tính';
        }
    });
};
