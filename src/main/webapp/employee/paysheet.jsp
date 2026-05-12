<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="../includes/header.jsp">
  <jsp:param name="page" value="paysheet" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/employee.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
<style>
    /* Design System Colors */
    :root {
        --primary-500: #0080FF;
        --primary-600: #0066cc;
        --primary-50: #f2f7ff;
        --secondary-500: #00c6ff;
        --secondary-50: #f0f9ff;
        --color-primary: #0080FF;
        --color-secondary: #00c6ff;
        --color-accent: #7d2ae8;
        --success-500: #4caf50;
        --warning-500: #ff9800;
        --danger-500: #dc3545;
        --info-500: #2196f3;
        --gray-50: #f9fafb;
        --gray-200: #e5e7eb;
        --gray-800: #1f2937;
    }
    
    * {
        box-sizing: border-box;
    }
    
    html, body {
        overflow-x: hidden;
        width: 100%;
        max-width: 100vw;
    }
    
    body {
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }
    
    .content {
        width: 100%;
        box-sizing: border-box;
    }
    
    .paysheet-controls {
        display: flex;
        align-items: center;
        gap: 16px;
        margin-bottom: 24px;
        background: white;
        padding: 16px;
        border-radius: 12px;
        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        border: 2px solid var(--color-primary);
        width: 100%;
        box-sizing: border-box;
    }
    
    .month-year-selector {
        display: flex;
        align-items: center;
        gap: 12px;
    }
    
    .month-year-selector select,
    .month-year-selector input {
        padding: 8px 12px;
        border: 1px solid var(--gray-200);
        border-radius: 6px;
        font-size: 14px;
    }
    
    .month-year-selector select:focus,
    .month-year-selector input:focus {
        outline: none;
        border-color: var(--color-primary);
        box-shadow: 0 0 0 3px rgba(0, 128, 255, 0.1);
    }
    
    .paysheet-table {
        background: white;
        border-radius: 12px;
        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        overflow: hidden;
        border: 2px solid var(--color-primary);
        width: 100%;
        box-sizing: border-box;
    }
    
    .table-wrapper {
        overflow-x: auto;
        width: 100%;
    }
    
    .paysheet-table table {
        width: 100%;
        min-width: 1200px;
        border-collapse: collapse;
        table-layout: fixed;
    }
    
    .paysheet-table thead {
        background: linear-gradient(135deg, var(--primary-50, #f2f7ff) 0%, var(--secondary-50, #f0f9ff) 100%);
    }
    
    .paysheet-table th {
        padding: 18px 20px;
        text-align: left;
        font-weight: 600;
        color: var(--gray-800, #1f2937);
        border-bottom: 2px solid var(--color-primary);
        white-space: nowrap;
    }
    
    .paysheet-table th:nth-child(1) { width: 8%; }  /* Mã NV */
    .paysheet-table th:nth-child(2) { width: 12%; } /* Họ tên */
    .paysheet-table th:nth-child(3) { width: 10%; } /* Loại lương */
    .paysheet-table th:nth-child(4) { width: 12%; } /* Tổng lương */
    .paysheet-table th:nth-child(5) { width: 10%; } /* Phụ cấp */
    .paysheet-table th:nth-child(6) { width: 10%; } /* Thưởng */
    .paysheet-table th:nth-child(7) { width: 10%; } /* Giảm trừ */
    .paysheet-table th:nth-child(8) { width: 12%; } /* Đã thanh toán */
    .paysheet-table th:nth-child(9) { width: 12%; } /* Chưa nhận */
    .paysheet-table th:nth-child(10) { width: 10%; } /* Trạng thái */
    .paysheet-table th:nth-child(11) { width: 14%; } /* Thao tác */
    
    .paysheet-table td {
        padding: 18px 20px;
        border-bottom: 1px solid var(--gray-200);
        word-wrap: break-word;
        overflow-wrap: break-word;
    }
    
    .paysheet-table td:nth-child(2) {
        white-space: normal;
        max-width: 0;
    }
    
    .paysheet-table tbody tr:hover {
        background: rgba(0, 128, 255, 0.05);
    }
    
    /* Statistics Cards - Updated Colors */
    .stat-card {
        border-left: 4px solid var(--color-primary) !important;
        border: 2px solid var(--color-primary) !important;
    }
    
    .stat-number {
        color: var(--color-primary) !important;
    }
    
    /* Buttons - Updated Colors */
    .btn-primary {
        background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%) !important;
        border: none !important;
        box-shadow: 0 4px 15px rgba(0, 128, 255, 0.4) !important;
    }
    
    .btn-primary:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 128, 255, 0.6) !important;
        background: linear-gradient(135deg, var(--primary-600) 0%, var(--secondary-500) 100%) !important;
    }
    
    .btn {
        transition: all 0.3s ease;
    }
    
    .btn:hover {
        transform: translateY(-2px);
    }
    
    .btn:not(.btn-primary):hover {
        background: rgba(0, 128, 255, 0.1) !important;
        border-color: var(--primary-600) !important;
    }
    
    .compensation-type-badge {
        display: inline-block;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 600;
    }
    
    .compensation-type-badge.fixed {
        background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
        color: white;
    }
    
    .compensation-type-badge.hybrid {
        background: linear-gradient(135deg, var(--success-500) 0%, #388e3c 100%);
        color: white;
    }
    
    .compensation-type-badge.pershift {
        background: linear-gradient(135deg, var(--warning-500) 0%, #f57c00 100%);
        color: white;
    }
    
    .paid-badge {
        display: inline-block;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 600;
        background: linear-gradient(135deg, var(--success-500) 0%, #388e3c 100%);
        color: white;
    }
    
    .unpaid-badge {
        display: inline-block;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 600;
        background: linear-gradient(135deg, var(--danger-500) 0%, #c82333 100%);
        color: white;
    }
    
    .btn-mark-paid {
        padding: 8px 16px;
        background: linear-gradient(135deg, var(--success-500) 0%, #388e3c 100%);
        color: white;
        border: none;
        border-radius: 8px;
        cursor: pointer;
        font-size: 13px;
        font-weight: 600;
        transition: all 0.3s ease;
        box-shadow: 0 4px 15px rgba(76, 175, 80, 0.4);
    }
    
    .btn-mark-paid:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(76, 175, 80, 0.6);
    }
    
    .btn-mark-paid:disabled {
        background: #9ca3af;
        cursor: not-allowed;
        box-shadow: none;
    }
    
    .currency {
        font-weight: 600;
        color: var(--gray-800);
    }
    
    /* Page Header */
    h1 {
        background: linear-gradient(135deg, var(--primary-500) 0%, var(--secondary-500) 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
    }
    
    /* Responsive */
    @media (max-width: 768px) {
        .paysheet-table table {
            min-width: 1000px;
        }
        
        .paysheet-table th,
        .paysheet-table td {
            padding: 12px 15px;
            font-size: 0.85em;
        }
    }
    
    @media (max-width: 480px) {
        .paysheet-table table {
            min-width: 900px;
        }
        
        .paysheet-table th,
        .paysheet-table td {
            padding: 10px 12px;
            font-size: 0.8em;
        }
    }
</style>

<div class="content">
    <!-- Page Header -->
    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px;">
        <div>
            <h1 style="font-size: 28px; font-weight: 700; margin: 0; margin-bottom: 8px;">
                <i class='bx bx-money' style="color: #0080FF; margin-right: 8px;"></i>
                Bảng lương
            </h1>
            <p style="color: #6b7280; margin: 0;">
                Quản lý bảng lương và thanh toán cho nhân viên
            </p>
        </div>
            <div style="display: flex; gap: 12px;">
                <button id="btnRecalculatePayroll" class="btn" onclick="recalculatePayroll()" 
                        style="display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; font-size: 14px; font-weight: 600; background: white; color: var(--color-primary); border: 2px solid var(--color-primary); border-radius: 8px; transition: all 0.3s ease;">
                    <i class='bx bx-refresh'></i>
                    Cập nhật lại bảng lương
                </button>
                <button id="btnGeneratePayroll" class="btn btn-primary" onclick="generatePayroll()" 
                        style="display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; font-size: 14px; font-weight: 600;">
                    <i class='bx bx-plus-circle'></i>
                    Tạo bảng lương tháng này
                </button>
            </div>
    </div>

    <!-- Month/Year Selector -->
    <div class="paysheet-controls">
        <div class="month-year-selector">
            <label style="font-weight: 600;">Tháng/Năm:</label>
            <select id="monthSelect" onchange="loadPayroll()">
                <option value="1">Tháng 1</option>
                <option value="2">Tháng 2</option>
                <option value="3">Tháng 3</option>
                <option value="4">Tháng 4</option>
                <option value="5">Tháng 5</option>
                <option value="6">Tháng 6</option>
                <option value="7">Tháng 7</option>
                <option value="8">Tháng 8</option>
                <option value="9">Tháng 9</option>
                <option value="10">Tháng 10</option>
                <option value="11">Tháng 11</option>
                <option value="12">Tháng 12</option>
            </select>
            <input type="number" id="yearSelect" min="2020" max="2030" onchange="loadPayroll()" />
            <button class="btn btn-primary" onclick="loadPayroll()">
                <i class='bx bx-refresh'></i> Tải lại
            </button>
        </div>
    </div>

    <!-- Statistics -->
    <div class="stats">
        <div class="stat-card">
            <div class="stat-number" id="totalPayrollCount">0</div>
            <div class="stat-label">Tổng nhân viên</div>
        </div>
        <div class="stat-card">
            <div class="stat-number currency" id="totalSalaryAmount">0 ₫</div>
            <div class="stat-label">Tổng tiền lương tháng</div>
        </div>
        <div class="stat-card">
            <div class="stat-number" id="paidCount">0</div>
            <div class="stat-label">Đã thanh toán</div>
        </div>
        <div class="stat-card">
            <div class="stat-number currency" id="totalRemainingAmount">0 ₫</div>
            <div class="stat-label">Tổng chưa thanh toán</div>
        </div>
    </div>

    <!-- Payroll Table -->
    <div class="paysheet-table" id="payrollTableContainer" style="display: none;">
        <div class="table-wrapper">
            <table>
                <thead>
                    <tr>
                        <th>Mã NV</th>
                        <th>Họ tên</th>
                        <th>Loại lương</th>
                        <th>Tổng lương</th>
                        <th>Phụ cấp</th>
                        <th>Thưởng</th>
                        <th>Giảm trừ</th>
                        <th>Đã thanh toán</th>
                        <th>Chưa nhận</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody id="payrollTableBody">
                    <!-- Data will be loaded here -->
                </tbody>
            </table>
        </div>
    </div>

    <!-- Empty State -->
    <div id="emptyState" style="background: white; border-radius: 12px; padding: 48px; text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,0.1); border: 2px solid var(--color-primary);">
        <i class='bx bx-money' style="font-size: 64px; color: var(--color-primary); margin-bottom: 16px;"></i>
        <h3 style="font-size: 20px; font-weight: 600; margin: 0 0 8px 0; color: var(--gray-800);">
            Đang tải dữ liệu...
        </h3>
    </div>
</div>

<script>
    const CONTEXT_PATH = '<c:out value="${pageContext.request.contextPath}" />';

    // Initialize current month/year
    document.addEventListener('DOMContentLoaded', function() {
        const now = new Date();
        document.getElementById('monthSelect').value = now.getMonth() + 1;
        document.getElementById('yearSelect').value = now.getFullYear();
        loadPayroll();
    });

    function formatCurrency(amount) {
        if (!amount || amount === '0' || amount === 0) return '0 ₫';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(parseFloat(amount));
    }

    function getCompensationTypeLabel(type) {
        switch(type) {
            case 'Fixed': return 'Lương cứng';
            case 'Hybrid': return 'Theo giờ';
            case 'PerShift': return 'Theo ca';
            default: return type;
        }
    }

    function getCompensationTypeClass(type) {
        switch(type) {
            case 'Fixed': return 'fixed';
            case 'Hybrid': return 'hybrid';
            case 'PerShift': return 'pershift';
            default: return '';
        }
    }

    async function loadPayroll() {
        const month = document.getElementById('monthSelect').value;
        const year = document.getElementById('yearSelect').value;

        try {
            const response = await fetch(CONTEXT_PATH + '/api/payroll/list?month=' + month + '&year=' + year);
            if (!response.ok) {
                throw new Error('Failed to load payroll');
            }

            const data = await response.json();
            
            if (data.success) {
                updateStatistics(data);
                updatePayrollTable(data.entries);
            } else {
                console.error('Error loading payroll:', data.error);
                showError('Không thể tải dữ liệu bảng lương');
            }
        } catch (error) {
            console.error('Error loading payroll:', error);
            showError('Có lỗi xảy ra khi tải dữ liệu');
        }
    }

    function updateStatistics(data) {
        document.getElementById('totalPayrollCount').textContent = data.entries ? data.entries.length : 0;
        document.getElementById('totalSalaryAmount').textContent = formatCurrency(data.totalSalary || 0);
        document.getElementById('paidCount').textContent = data.paidCount || 0;
        document.getElementById('totalRemainingAmount').textContent = formatCurrency(data.totalRemaining || 0);
    }

    function updatePayrollTable(entries) {
        const tbody = document.getElementById('payrollTableBody');
        const tableContainer = document.getElementById('payrollTableContainer');
        const emptyState = document.getElementById('emptyState');

        if (!entries || entries.length === 0) {
            tableContainer.style.display = 'none';
            emptyState.style.display = 'block';
            emptyState.querySelector('h3').textContent = 'Chưa có dữ liệu bảng lương';
            return;
        }

        tableContainer.style.display = 'block';
        emptyState.style.display = 'none';

        tbody.innerHTML = entries.map(entry => {
            const isPaid = entry.isPaid === true;
            const employeeCode = escapeHtml(entry.employeeCode || '');
            const employeeName = escapeHtml(entry.employeeName || '');
            const compensationTypeClass = getCompensationTypeClass(entry.compensationType);
            const compensationTypeLabel = getCompensationTypeLabel(entry.compensationType);
            const payrollEntryId = entry.payrollEntryId || '';
            return '<tr>' +
                '<td>' + employeeCode + '</td>' +
                '<td>' + employeeName + '</td>' +
                '<td>' +
                    '<span class="compensation-type-badge ' + compensationTypeClass + '">' +
                        compensationTypeLabel +
                    '</span>' +
                '</td>' +
                '<td class="currency">' + formatCurrency(entry.totalSalary) + '</td>' +
                '<td class="currency">' + formatCurrency(entry.allowances) + '</td>' +
                '<td class="currency">' + formatCurrency(entry.bonuses) + '</td>' +
                '<td class="currency">' + formatCurrency(entry.deductions) + '</td>' +
                '<td class="currency">' + formatCurrency(entry.totalPaid) + '</td>' +
                '<td class="currency">' + formatCurrency(entry.totalRemaining) + '</td>' +
                '<td>' +
                    (isPaid 
                        ? '<span class="paid-badge">Đã thanh toán</span>' 
                        : '<span class="unpaid-badge">Chưa thanh toán</span>') +
                '</td>' +
                '<td>' +
                    '<button class="btn-mark-paid" ' +
                            'onclick="markAsPaid(\'' + payrollEntryId + '\')" ' +
                            (isPaid ? 'disabled' : '') + '>' +
                        (isPaid ? 'Đã thanh toán' : 'Đánh dấu đã thanh toán') +
                    '</button>' +
                '</td>' +
            '</tr>';
        }).join('');
    }

    async function generatePayroll() {
        const month = document.getElementById('monthSelect').value;
        const year = document.getElementById('yearSelect').value;
        
        if (!confirm('Bạn có chắc chắn muốn tạo bảng lương cho tháng ' + month + '/' + year + '?\n\nHệ thống sẽ tạo bảng lương cho tất cả nhân viên đang hoạt động trong tháng này.')) {
            return;
        }

        const btn = document.getElementById('btnGeneratePayroll');
        const originalText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<i class="bx bx-loader-alt bx-spin"></i> Đang tạo...';

        try {
            const response = await fetch(CONTEXT_PATH + '/api/payroll/generate?month=' + month + '&year=' + year, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            const result = await response.json();
            
            if (result.success) {
                if (result.createdCount > 0) {
                    alert('Đã tạo bảng lương thành công!\n\nĐã tạo bảng lương cho ' + result.createdCount + ' nhân viên.');
                } else {
                    alert('Thông báo:\n\n' + (result.message || 'Không có nhân viên nào cần tạo bảng lương mới. Có thể tất cả nhân viên đã có bảng lương cho tháng này.'));
                }
                loadPayroll(); // Reload data
            } else {
                alert('Có lỗi xảy ra: ' + (result.error || 'Không thể tạo bảng lương'));
            }
        } catch (error) {
            console.error('Error generating payroll:', error);
            alert('Có lỗi xảy ra khi tạo bảng lương');
            } finally {
                btn.disabled = false;
                btn.innerHTML = originalText;
            }
        }

        async function recalculatePayroll() {
            const month = document.getElementById('monthSelect').value;
            const year = document.getElementById('yearSelect').value;
            
            if (!confirm('Bạn có chắc chắn muốn cập nhật lại bảng lương cho tháng ' + month + '/' + year + '?\n\nHệ thống sẽ tính lại lương dựa trên dữ liệu chấm công mới nhất.')) {
                return;
            }

            const btn = document.getElementById('btnRecalculatePayroll');
            const originalText = btn.innerHTML;
            btn.disabled = true;
            btn.innerHTML = '<i class="bx bx-loader-alt bx-spin"></i> Đang cập nhật...';

            try {
                const url = CONTEXT_PATH + '/api/payroll/recalculate?month=' + month + '&year=' + year;
                console.log('Recalculating payroll - URL:', url);
                
                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                });

                console.log('Recalculate response status:', response.status);
                
                if (!response.ok) {
                    const errorText = await response.text();
                    console.error('Recalculate error response:', errorText);
                    try {
                        const errorJson = JSON.parse(errorText);
                        alert('Có lỗi xảy ra: ' + (errorJson.error || 'Không thể cập nhật bảng lương'));
                    } catch (e) {
                        alert('Có lỗi xảy ra: ' + errorText);
                    }
                    return;
                }

                const result = await response.json();
                console.log('Recalculate result:', result);
                
                if (result.success) {
                    if (result.recalculatedCount !== undefined) {
                        alert('Đã cập nhật lại bảng lương thành công!\n\nĐã cập nhật cho ' + result.recalculatedCount + ' nhân viên.');
                    } else {
                        alert('Đã cập nhật lại bảng lương thành công!');
                    }
                    loadPayroll(); // Reload data
                } else {
                    alert('Có lỗi xảy ra: ' + (result.error || 'Không thể cập nhật bảng lương'));
                }
            } catch (error) {
                console.error('Error recalculating payroll:', error);
                alert('Có lỗi xảy ra khi cập nhật bảng lương');
            } finally {
                btn.disabled = false;
                btn.innerHTML = originalText;
            }
        }

    async function markAsPaid(payrollEntryId) {
        if (!confirm('Bạn có chắc chắn muốn đánh dấu đã thanh toán lương cho nhân viên này?')) {
            return;
        }

        try {
            const formData = new URLSearchParams();
            formData.append('payrollEntryId', payrollEntryId);

            const response = await fetch(CONTEXT_PATH + '/api/payroll/mark-paid', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData
            });

            const data = await response.json();

            if (data.success) {
                alert('Đánh dấu đã thanh toán thành công!');
                loadPayroll(); // Reload to update the table
            } else {
                alert('Lỗi: ' + (data.error || 'Không thể đánh dấu đã thanh toán'));
            }
        } catch (error) {
            console.error('Error marking as paid:', error);
            alert('Có lỗi xảy ra khi đánh dấu đã thanh toán');
        }
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function showError(message) {
        const emptyState = document.getElementById('emptyState');
        emptyState.style.display = 'block';
        emptyState.querySelector('h3').textContent = message;
        document.getElementById('payrollTableContainer').style.display = 'none';
    }
</script>

<jsp:include page="../includes/footer.jsp" />
