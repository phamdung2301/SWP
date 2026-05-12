/**
 * Enhanced Set Price Page Management
 * Based on productlist-enhanced.js with price-specific features
 */

// ============================================================================
// GLOBAL STATE & CONFIGURATION
// ============================================================================

const pricesPagination = {
    currentPage: 1,
    pageSize: 10,
    totalItems: 0,
    totalPages: 0
};

let currentSortColumn = -1;
let currentSortDirection = 'asc';
let selectedFile = null;

// ============================================================================
// INITIALIZATION
// ============================================================================

document.addEventListener('DOMContentLoaded', function() {
    // Format all price inputs on page load
    initializePriceFormatting();
    
    // Initialize pagination
    initializePagination();
    
    // Auto search on input
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', searchPrices);
    }
    
    // Add event listeners for filters
    document.querySelectorAll('input[name="categoryFilter"], input[name="priceFilter"], input[name="profitFilter"]').forEach(checkbox => {
        checkbox.addEventListener('change', applyAllFilters);
    });
    
    // Setup price input listeners for real-time profit calculation
    setupPriceInputListeners();
});

// ============================================================================
// PRICE FORMATTING
// ============================================================================

function initializePriceFormatting() {
    // Format all price inputs on page load
    document.querySelectorAll('.price-input.price-formatted').forEach(input => {
        const originalValue = input.dataset.originalValue || '0';
        formatPriceValue(input, originalValue);
    });
}

function formatPriceInput(input, type) {
    const value = input.value;
    formatPriceValue(input, value);
}

function formatPriceValue(input, value) {
    // Remove all non-digit characters
    const numValue = parseFloat(value.toString().replace(/\D/g, '')) || 0;
    
    // Format with dots as thousands separator (Vietnamese style)
    // Use JavaScript number formatting without decimals
    let formatted = numValue.toString();
    formatted = formatted.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    
    input.value = formatted;
    input.setAttribute('data-numeric-value', numValue.toString());
}

function getNumericValue(input) {
    const numValue = input.getAttribute('data-numeric-value');
    return parseFloat(numValue || '0');
}

// ============================================================================
// PAGINATION
// ============================================================================

function initializePagination() {
    console.log('🔢 Initializing pagination...');
    
    const pricesContainer = document.querySelector('.price-table');
    if (!pricesContainer) {
        console.log('⚠️ No price table container found');
        return;
    }
    
    const emptyState = pricesContainer.querySelector('.empty-state');
    const isEmptyStateVisible = emptyState && emptyState.style.display !== 'none';
    
    if (isEmptyStateVisible) {
        console.log('🔢 Prices in empty state, skipping pagination initialization');
        pricesPagination.totalItems = 0;
        pricesPagination.totalPages = 0;
        pricesPagination.currentPage = 1;
        return;
    }
    
    const tbody = pricesContainer.querySelector('.table tbody');
    if (!tbody) {
        console.log('⚠️ No tbody found');
        return;
    }
    
    const rows = tbody.querySelectorAll('tr[data-product-id]');
    pricesPagination.totalItems = rows.length;
    pricesPagination.totalPages = Math.ceil(pricesPagination.totalItems / pricesPagination.pageSize);
    
    console.log(`📊 Total prices: ${pricesPagination.totalItems}, Pages: ${pricesPagination.totalPages}`);
    
    updatePaginationUI();
    showPricesPage(1);
}

function showPricesPage(page) {
    const pricesContainer = document.querySelector('.price-table');
    if (!pricesContainer) return;
    
    const emptyState = pricesContainer.querySelector('.empty-state');
    const isEmptyStateVisible = emptyState && emptyState.style.display !== 'none';
    
    if (isEmptyStateVisible) {
        console.log('📄 Prices in empty state, skipping page show');
        return;
    }
    
    const tbody = pricesContainer.querySelector('.table tbody');
    if (!tbody) return;
    
    const rows = tbody.querySelectorAll('tr[data-product-id]');
    
    // Validate page number
    if (page < 1) page = 1;
    if (page > pricesPagination.totalPages) page = pricesPagination.totalPages;
    
    pricesPagination.currentPage = page;
    
    // Calculate range
    const startIndex = (page - 1) * pricesPagination.pageSize;
    const endIndex = startIndex + pricesPagination.pageSize;
    
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

function changePricesPage(delta) {
    const newPage = pricesPagination.currentPage + delta;
    if (newPage >= 1 && newPage <= pricesPagination.totalPages) {
        showPricesPage(newPage);
    }
}

function changePricesPageSize(newSize) {
    pricesPagination.pageSize = parseInt(newSize);
    pricesPagination.totalPages = Math.ceil(pricesPagination.totalItems / pricesPagination.pageSize);
    showPricesPage(1);
}

function updatePaginationUI() {
    const paginationContainer = document.getElementById('pricesPagination');
    if (!paginationContainer) return;
    
    // Update page info
    const pageInfo = document.getElementById('pricesPageInfo');
    if (pageInfo) {
        pageInfo.textContent = `Trang ${pricesPagination.currentPage} / ${pricesPagination.totalPages || 1}`;
    }
    
    // Update buttons
    const prevBtn = document.getElementById('pricesPrevBtn');
    const nextBtn = document.getElementById('pricesNextBtn');
    
    if (prevBtn) {
        prevBtn.disabled = pricesPagination.currentPage <= 1;
    }
    
    if (nextBtn) {
        nextBtn.disabled = pricesPagination.currentPage >= pricesPagination.totalPages;
    }
    
    // Update page numbers
    updatePageNumbers();
}

function updatePageNumbers() {
    const pageNumbersContainer = document.getElementById('pricesPageNumbers');
    if (!pageNumbersContainer) return;
    
    pageNumbersContainer.innerHTML = '';
    
    const maxVisible = 5;
    let startPage = Math.max(1, pricesPagination.currentPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(pricesPagination.totalPages, startPage + maxVisible - 1);
    
    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(1, endPage - maxVisible + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        const span = document.createElement('span');
        span.className = 'pagination-number' + (i === pricesPagination.currentPage ? ' active' : '');
        span.textContent = i;
        span.onclick = () => showPricesPage(i);
        pageNumbersContainer.appendChild(span);
    }
}

// ============================================================================
// SEARCH & FILTER
// ============================================================================

function searchPrices() {
    applyAllFilters();
}

function filterPrices() {
    applyAllFilters();
}

function applyAllFilters() {
    const searchTerm = document.getElementById('searchInput')?.value.toLowerCase().trim() || '';
    const categoryFilters = Array.from(document.querySelectorAll('input[name="categoryFilter"]:checked')).map(cb => cb.value);
    const priceFilters = Array.from(document.querySelectorAll('input[name="priceFilter"]:checked')).map(cb => cb.value);
    const profitFilters = Array.from(document.querySelectorAll('input[name="profitFilter"]:checked')).map(cb => cb.value);
    
    const tbody = document.querySelector('.table tbody');
    if (!tbody) return;
    
    const rows = tbody.querySelectorAll('tr[data-product-id]');
    
    console.log('🔍 Applying filters...', {
        searchTerm,
        categoryFilters,
        priceFilters,
        profitFilters,
        totalRows: rows.length
    });
    
    let visibleCount = 0;
    
    rows.forEach((row, index) => {
        let showRow = true;
        
        // Apply search filter
        if (searchTerm !== '') {
            const productCode = (row.dataset.productCode || '').toLowerCase();
            const productName = (row.dataset.productName || '').toLowerCase();
            
            if (!productCode.includes(searchTerm) && !productName.includes(searchTerm)) {
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
        
        // Apply price filter
        if (showRow && priceFilters.length > 0) {
            const sellingPrice = parseFloat(row.dataset.sellingPrice || 0);
            const matches = priceFilters.some(filterValue => {
                if (filterValue === 'below10000') return sellingPrice > 0 && sellingPrice < 10000;
                if (filterValue === '10000-50000') return sellingPrice >= 10000 && sellingPrice <= 50000;
                if (filterValue === 'above50000') return sellingPrice > 50000;
                return false;
            });
            if (!matches) {
                showRow = false;
            }
        }
        
        // Apply profit filter
        if (showRow && profitFilters.length > 0) {
            const sellingPrice = parseFloat(row.dataset.sellingPrice || 0);
            const originalPrice = parseFloat(row.dataset.originalPrice || 0);
            const profit = sellingPrice - originalPrice;
            const profitPercent = originalPrice > 0 ? (profit / originalPrice * 100) : 0;
            
            const matches = profitFilters.some(filterValue => {
                if (filterValue === 'negative') return profit < 0;
                if (filterValue === 'low') return profit >= 0 && profitPercent > 0 && profitPercent < 20;
                if (filterValue === 'high') return profitPercent >= 20;
                return false;
            });
            if (!matches) {
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
    
    pricesPagination.totalItems = visibleRows.length;
    pricesPagination.totalPages = Math.ceil(pricesPagination.totalItems / pricesPagination.pageSize);
    pricesPagination.currentPage = 1;
    
    // Show first page of filtered results
    const startIndex = 0;
    const endIndex = pricesPagination.pageSize;
    
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
            case 4: // Giá vốn
                aValue = parseFloat(a.dataset.originalPrice || 0);
                bValue = parseFloat(b.dataset.originalPrice || 0);
                break;
            case 5: // Giá bán
                aValue = parseFloat(a.dataset.sellingPrice || 0);
                bValue = parseFloat(b.dataset.sellingPrice || 0);
                break;
            case 6: // Lợi nhuận
                const aSelling = parseFloat(a.dataset.sellingPrice || 0);
                const aOriginal = parseFloat(a.dataset.originalPrice || 0);
                const bSelling = parseFloat(b.dataset.sellingPrice || 0);
                const bOriginal = parseFloat(b.dataset.originalPrice || 0);
                aValue = aSelling - aOriginal;
                bValue = bSelling - bOriginal;
                break;
            case 7: // % Lợi nhuận
                const aSelling2 = parseFloat(a.dataset.sellingPrice || 0);
                const aOriginal2 = parseFloat(a.dataset.originalPrice || 0);
                const bSelling2 = parseFloat(b.dataset.sellingPrice || 0);
                const bOriginal2 = parseFloat(b.dataset.originalPrice || 0);
                const aProfit = aSelling2 - aOriginal2;
                const bProfit = bSelling2 - bOriginal2;
                aValue = aOriginal2 > 0 ? (aProfit / aOriginal2 * 100) : 0;
                bValue = bOriginal2 > 0 ? (bProfit / bOriginal2 * 100) : 0;
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
    showPricesPage(pricesPagination.currentPage);
}

// ============================================================================
// PRICE INPUT & PROFIT CALCULATION
// ============================================================================

function setupPriceInputListeners() {
    document.addEventListener('input', function(e) {
        if (e.target.classList.contains('price-input')) {
            const row = e.target.closest('tr');
            const originalPriceInput = row.querySelector('.original-price');
            const sellingPriceInput = row.querySelector('.selling-price');
            
            if (!originalPriceInput || !sellingPriceInput) return;
            
            // Get values directly from input and parse (remove dots and non-numeric chars)
            const originalPriceStr = originalPriceInput.value || '0';
            const sellingPriceStr = sellingPriceInput.value || '0';
            
            const originalPrice = parseFloat(originalPriceStr.replace(/\./g, '').replace(/[^\d]/g, '')) || 0;
            const sellingPrice = parseFloat(sellingPriceStr.replace(/\./g, '').replace(/[^\d]/g, '')) || 0;
            
            const profit = sellingPrice - originalPrice;
            const profitPercent = originalPrice > 0 ? (profit / originalPrice * 100) : 0;
            
            // Update profit display
            const profitAmountEl = row.querySelector('.profit-amount');
            const profitPercentEl = row.querySelector('.profit-percent');
            
            if (profitAmountEl) {
                profitAmountEl.textContent = formatCurrency(profit) + ' ₫';
            }
            if (profitPercentEl) {
                profitPercentEl.textContent = profitPercent.toFixed(1) + '%';
            }
            
            // Color coding
            if (profitAmountEl) {
                if (profit < 0) {
                    profitAmountEl.style.color = '#dc3545';
                } else if (profit > 0) {
                    profitAmountEl.style.color = '#28a745';
                } else {
                    profitAmountEl.style.color = '#6c757d';
                }
            }
            if (profitPercentEl) {
                if (profit < 0) {
                    profitPercentEl.style.color = '#dc3545';
                } else if (profit > 0) {
                    profitPercentEl.style.color = '#28a745';
                } else {
                    profitPercentEl.style.color = '#6c757d';
                }
            }
        }
    });
    
    // Auto-format on input for price-formatted inputs
    document.addEventListener('input', function(e) {
        if (e.target.classList.contains('price-formatted')) {
            const value = e.target.value;
            const numValue = parseFloat(value.replace(/\D/g, '')) || 0;
            
            // Format with dots as thousands separator (Vietnamese style)
            let formatted = numValue.toString();
            formatted = formatted.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
            
            e.target.value = formatted;
            e.target.setAttribute('data-numeric-value', numValue.toString());
        }
    });
}

function validatePriceInput(input) {
    const value = getNumericValue(input);
    
    if (input.classList.contains('selling-price') && value < 1000 && value !== 0) {
        showNotification('Giá bán tối thiểu 1,000 VND', 'error');
        input.focus();
        return false;
    }
    
    if (value < 0) {
        showNotification('Giá không thể âm', 'error');
        input.focus();
        return false;
    }
    
    return true;
}

function formatCurrency(value) {
    if (!value || isNaN(value)) return '0';
    return Number(value).toLocaleString('vi-VN');
}

// ============================================================================
// UPDATE PRICE
// ============================================================================

async function updatePrice(productId, size) {
    const row = findRowByProductAndSize(productId, size);
    
    if (!row) {
        showNotification('Không tìm thấy dòng sản phẩm', 'error');
        return;
    }
    
    const originalPriceInput = row.querySelector('.original-price');
    const sellingPriceInput = row.querySelector('.selling-price');
    
    const originalPrice = getNumericValue(originalPriceInput);
    const sellingPrice = getNumericValue(sellingPriceInput);
    
    // Validation
    if (!validatePrices(originalPrice.toString(), sellingPrice.toString())) {
        return;
    }
    
    // Show loading state
    const btn = event.target;
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = '⏳ Đang lưu...';
    
    try {
        const resp = await fetch(`${pageContext.request.contextPath}/setprice`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: new URLSearchParams({
                action: 'update',
                productId: productId,
                size: size,
                originalPrice: originalPrice.toString(),
                sellingPrice: sellingPrice.toString()
            })
        });
        
        const data = await resp.json().catch(() => ({}));
        
        if (resp.ok && data.success) {
            showNotification('✅ Cập nhật giá thành công', 'success');
            
            // Update dataset
            row.dataset.originalPrice = originalPrice.toString();
            row.dataset.sellingPrice = sellingPrice.toString();
        } else {
            showNotification('❌ Cập nhật thất bại: ' + (data.message || resp.status), 'error');
        }
    } catch (err) {
        showNotification('❌ Lỗi khi gọi API: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}

function findRowByProductAndSize(productId, size) {
    const rows = document.querySelectorAll('tr[data-product-id]');
    for (let row of rows) {
        if (row.dataset.productId === productId && row.dataset.size === size) {
            return row;
        }
    }
    return null;
}

function validatePrices(originalPrice, sellingPrice) {
    const original = parseFloat(originalPrice);
    const selling = parseFloat(sellingPrice);
    
    if (isNaN(original) || original < 0) {
        showNotification('Giá vốn phải >= 0', 'error');
        return false;
    }
    
    if (isNaN(selling)) {
        showNotification('Giá bán không hợp lệ', 'error');
        return false;
    }
    
    if (selling < 1000) {
        showNotification('Giá bán tối thiểu 1,000 VND', 'error');
        return false;
    }
    
    // Max value check
    const MAX_VALUE = 1000000000; // 1 tỷ
    if (original > MAX_VALUE || selling > MAX_VALUE) {
        showNotification('Giá không được vượt quá 1.000.000.000 VND', 'error');
        return false;
    }
    
    // Optional: Check selling price >= original price for positive profit
    // Not enforced as business logic allows discount/loss
    
    return true;
}

// ============================================================================
// EXPORT & IMPORT
// ============================================================================

// Global variable to store pending updates
let pendingUpdates = [];

function saveAllPrices() {
    // Lấy tất cả các dòng có input giá đã thay đổi
    const visibleRows = Array.from(document.querySelectorAll('tr[data-product-id]')).filter(row => row.style.display !== 'none');
    pendingUpdates = [];
    
    visibleRows.forEach(row => {
        const originalPriceInput = row.querySelector('.original-price');
        const sellingPriceInput = row.querySelector('.selling-price');
        
        if (originalPriceInput && sellingPriceInput) {
            const originalPrice = getNumericValue(originalPriceInput);
            const sellingPrice = getNumericValue(sellingPriceInput);
            
            // Kiểm tra xem giá có được nhập không
            if (originalPrice >= 0 && sellingPrice >= 0) {
                pendingUpdates.push({
                    productId: row.dataset.productId,
                    size: row.dataset.size,
                    originalPrice: originalPrice.toString(),
                    sellingPrice: sellingPrice.toString()
                });
            }
        }
    });
    
    if (pendingUpdates.length === 0) {
        showNotification('Không có thay đổi nào để lưu', 'warning');
        return;
    }
    
    // Hiển thị modal xác nhận
    showSaveAllModal();
}

function showSaveAllModal() {
    const modal = document.getElementById('saveAllModal');
    const saveCount = document.getElementById('saveCount');
    
    if (modal && saveCount) {
        saveCount.textContent = pendingUpdates.length;
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Close modal on outside click
        modal.onclick = function(event) {
            if (event.target === modal) {
                closeSaveAllModal();
            }
        };
    }
}

function closeSaveAllModal() {
    const modal = document.getElementById('saveAllModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        modal.onclick = null;
    }
}

async function confirmSaveAll() {
    if (pendingUpdates.length === 0) {
        showNotification('Không có dữ liệu để lưu', 'error');
        closeSaveAllModal();
        return;
    }
    
    // Disable button and show loading
    const confirmBtn = document.getElementById('confirmSaveBtn');
    if (confirmBtn) {
        confirmBtn.disabled = true;
        confirmBtn.textContent = '⏳ Đang lưu...';
    }
    
    showNotification(`Đang lưu ${pendingUpdates.length} bản ghi...`, 'info');
    
    // Gửi tất cả các update
    Promise.all(pendingUpdates.map(update => 
        fetch(`${pageContext.request.contextPath}/setprice`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: new URLSearchParams({
                action: 'update',
                productId: update.productId,
                size: update.size,
                originalPrice: update.originalPrice,
                sellingPrice: update.sellingPrice
            })
        })
        .then(resp => resp.json())
    ))
    .then(results => {
        const successCount = results.filter(r => r.success).length;
        const failCount = results.length - successCount;
        
        if (failCount === 0) {
            showNotification(`✅ Đã lưu thành công ${successCount} bản ghi`, 'success');
            closeSaveAllModal();
            // Reload page sau 1 giây
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showNotification(`⚠️ Đã lưu ${successCount} bản ghi, ${failCount} bản ghi lỗi`, 'warning');
            closeSaveAllModal();
            if (confirmBtn) {
                confirmBtn.disabled = false;
                confirmBtn.textContent = '✅ Xác nhận lưu';
            }
        }
    })
    .catch(error => {
        console.error('Save all error:', error);
        showNotification('❌ Lỗi khi lưu hàng loạt', 'error');
        closeSaveAllModal();
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.textContent = '✅ Xác nhận lưu';
        }
    });
}

function exportPriceReport() {
    // First check if there's data to export
    const tbody = document.querySelector('.table tbody');
    const rows = tbody ? tbody.querySelectorAll('tr[data-product-id]') : [];
    
    if (rows.length === 0) {
        showNotification('Không có dữ liệu để xuất báo cáo', 'warning');
        return;
    }
    
    // Use fetch to check response
    fetch('setprice', {
        method: 'POST',
        body: new URLSearchParams({
            action: 'exportExcel'
        })
    })
    .then(response => {
        // Check if it's JSON (error) or file (success)
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
                a.download = 'bao_cao_gia_' + new Date().toISOString().split('T')[0] + '.xlsx';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
                showNotification('✅ Xuất báo cáo thành công', 'success');
            });
        }
    })
    .catch(error => {
        console.error('Export error:', error);
        showNotification('❌ Lỗi khi xuất báo cáo', 'error');
    });
}

function downloadTemplate(type) {
    showNotification('Đang tải mẫu...', 'info');
    
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = 'setprice';
    
    const actionInput = document.createElement('input');
    actionInput.type = 'hidden';
    actionInput.name = 'action';
    actionInput.value = 'downloadTemplate';
    
    form.appendChild(actionInput);
    document.body.appendChild(form);
    form.submit();
    document.body.removeChild(form);
}

function importPrices() {
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
}

function closeImportModal() {
    const modal = document.getElementById('importExcelModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        modal.onclick = null;
        resetImportModal();
    }
}

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

function handleFileSelect(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    // Validate file type
    const allowedTypes = [
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-excel'
    ];
    
    if (!allowedTypes.includes(file.type) && !file.name.match(/\.(xlsx|xls)$/i)) {
        showNotification('Định dạng file không hợp lệ', 'error');
        return;
    }
    
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
}

function removeFile() {
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
}

function checkFile() {
    if (!selectedFile) {
        showNotification('Chưa chọn file', 'error');
        return;
    }
    
    const formData = new FormData();
    formData.append('excelFile', selectedFile);
    formData.append('action', 'checkExcel');
    
    const checkBtn = document.getElementById('checkBtn');
    if (checkBtn) {
        checkBtn.disabled = true;
        checkBtn.textContent = '⏳ Đang kiểm tra...';
    }
    
    fetch('setprice', {
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
}

function showCheckResults(data) {
    const resultSummary = document.getElementById('resultSummary');
    const resultDetails = document.getElementById('resultDetails');
    const results = document.getElementById('importResults');
    
    if (!results || !resultSummary) return;
    
    results.style.display = 'block';
    
    // Summary
    let summaryHtml = `
        <div class="result-item">
            <div class="result-number">${data.totalProducts || 0}</div>
            <div class="result-label">Bản ghi</div>
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

function startImport() {
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
    
    simulateProgress();
    
    fetch('setprice', {
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
}

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

function hideProgress() {
    const progressFill = document.getElementById('progressFill');
    if (progressFill) progressFill.style.width = '100%';
    
    setTimeout(() => {
        const progress = document.getElementById('importProgress');
        if (progress) progress.style.display = 'none';
    }, 500);
}

function showImportResults(data) {
    const results = document.getElementById('importResults');
    const resultSummary = document.getElementById('resultSummary');
    const resultDetails = document.getElementById('resultDetails');
    
    if (!results || !resultSummary) return;
    
    results.style.display = 'block';
    
    if (data.success) {
        // Summary
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
                <div class="result-number">${data.successCount || 0}</div>
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
                resultDetails.innerHTML = '<div class="success-message">✅ Nhập dữ liệu thành công! Tất cả giá đã được cập nhật vào hệ thống.</div>';
            }
        }
        
        // Check if there were errors
        if (data.errorCount && data.errorCount > 0) {
            showNotification('⚠️ Có một số lỗi khi nhập dữ liệu', 'warning');
        } else {
            showNotification('✅ Nhập dữ liệu thành công', 'success');
            
            // Reload page to show updated data only if no errors
            setTimeout(() => {
                window.location.reload();
            }, 2000);
        }
    }
}

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
// EXPOSE FUNCTIONS TO GLOBAL SCOPE
// ============================================================================

// ============================================================================
// INPUT VALIDATION - Allow only numbers
// ============================================================================

function allowOnlyNumbers(event) {
    // Allow: backspace, delete, tab, escape, enter
    if ([46, 8, 9, 27, 13].indexOf(event.keyCode) !== -1 ||
        // Allow: Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X
        (event.keyCode === 65 && event.ctrlKey === true) ||
        (event.keyCode === 67 && event.ctrlKey === true) ||
        (event.keyCode === 86 && event.ctrlKey === true) ||
        (event.keyCode === 88 && event.ctrlKey === true) ||
        // Allow: home, end, left, right, down, up
        (event.keyCode >= 35 && event.keyCode <= 40) ||
        // Allow numbers
        (event.keyCode >= 48 && event.keyCode <= 57) ||
        (event.keyCode >= 96 && event.keyCode <= 105)) {
        return true;
    }
    // Ensure that it is a number and stop the keypress
    if ((event.shiftKey || (event.keyCode < 48 || event.keyCode > 57)) && (event.keyCode < 96 || event.keyCode > 105)) {
        event.preventDefault();
        return false;
    }
    return true;
}

window.searchPrices = searchPrices;
window.filterPrices = filterPrices;
window.toggleFilterSection = toggleFilterSection;
window.updatePrice = updatePrice;
window.saveAllPrices = saveAllPrices;
window.changePricesPage = changePricesPage;
window.changePricesPageSize = changePricesPageSize;
window.sortTable = sortTable;
window.formatPriceInput = formatPriceInput;
window.getNumericValue = getNumericValue;
window.formatPriceValue = formatPriceValue;
window.allowOnlyNumbers = allowOnlyNumbers;
window.exportPriceReport = exportPriceReport;
window.importPrices = importPrices;
window.closeImportModal = closeImportModal;
window.closeSaveAllModal = closeSaveAllModal;
window.confirmSaveAll = confirmSaveAll;
window.handleFileSelect = handleFileSelect;
window.removeFile = removeFile;
window.checkFile = checkFile;
window.startImport = startImport;
window.downloadTemplate = downloadTemplate;
window.showNotification = showNotification;
window.removeNotification = removeNotification;
