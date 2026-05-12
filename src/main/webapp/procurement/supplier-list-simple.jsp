<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Nhà cung cấp</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <script src="${pageContext.request.contextPath}/js/dropdown-simple.js"></script>
    
    <style>
        :root {
            --primary-500: #0080FF;
            --primary-600: #0066cc;
            --secondary-500: #00c6ff;
            --color-primary: #0080FF;
            --color-secondary: #00c6ff;
            --color-accent: #7d2ae8;
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
            background: var(--gray-50, #f9fafb);
            margin: 0;
            padding: 0;
        }
        
        .container {
            max-width: 1800px;
            margin: 0 auto;
            padding: 20px;
            width: 100%;
            box-sizing: border-box;
        }
        
        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
            background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%);
            padding: 30px;
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            border: 2px solid rgba(255,255,255,0.3);
            width: 100%;
            box-sizing: border-box;
            flex-wrap: wrap;
            gap: 15px;
        }
        
        .page-title {
            font-size: 32px;
            font-weight: 700;
            background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            display: flex;
            align-items: center;
            gap: 15px;
        }
        
        .page-title .icon {
            background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
            color: white;
            width: 50px;
            height: 50px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            box-shadow: 0 4px 12px rgba(0, 128, 255, 0.3);
            -webkit-text-fill-color: initial;
            -webkit-background-clip: initial;
            background-clip: initial;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            box-shadow: 0 2px 6px rgba(0, 128, 255, 0.3);
            transition: all 0.2s;
        }
        
        .btn-primary:hover {
            background: linear-gradient(135deg, var(--primary-600), var(--secondary-500));
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 128, 255, 0.4);
        }
        
        .table-wrapper {
            width: 100%;
            overflow-x: auto;
            overflow-y: visible;
            -webkit-overflow-scrolling: touch;
            box-sizing: border-box;
        }
        
        .supplier-table {
            width: 100%;
            min-width: 1200px;
            border-collapse: collapse;
            background: white;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            border: 1px solid var(--color-primary);
            position: relative;
            table-layout: fixed;
        }
        
        .supplier-table::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--primary-500), var(--secondary-500));
        }
        
        .supplier-table th {
            background: linear-gradient(135deg, var(--primary-50, #f2f7ff) 0%, var(--secondary-50, #f0f9ff) 100%);
            padding: 16px 20px;
            text-align: left;
            font-weight: 600;
            color: var(--gray-800, #1f2937);
            border-bottom: 2px solid var(--color-primary);
            white-space: nowrap;
        }
        
        .supplier-table th:nth-child(1) { width: 18%; } /* Tên nhà cung cấp */
        .supplier-table th:nth-child(2) { width: 12%; } /* Người liên hệ */
        .supplier-table th:nth-child(3) { width: 18%; } /* Email */
        .supplier-table th:nth-child(4) { width: 12%; } /* Số điện thoại */
        .supplier-table th:nth-child(5) { width: 10%; } /* Đánh giá */
        .supplier-table th:nth-child(6) { width: 12%; } /* Tỷ lệ đúng hẹn */
        .supplier-table th:nth-child(7) { width: 10%; } /* Trạng thái */
        .supplier-table th:nth-child(8) { width: 8%; }  /* Thao tác */
        
        .supplier-table td {
            padding: 16px 20px;
            border-bottom: 1px solid var(--gray-200, #e5e7eb);
            word-wrap: break-word;
            overflow-wrap: break-word;
        }
        
        .supplier-table td:nth-child(1),
        .supplier-table td:nth-child(3) {
            white-space: normal;
            max-width: 0;
        }
        
        .supplier-table tr:hover {
            background: rgba(0, 128, 255, 0.05);
            transition: all 0.2s ease;
        }
        
        @media (min-width: 1024px) {
            .supplier-table tr:hover {
                transform: translateX(2px);
            }
        }
        
        .btn-edit, .btn-details {
            padding: 8px 16px;
            border: none;
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
            margin-right: 8px;
            transition: all 0.2s;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
            white-space: nowrap;
        }
        
        .btn-edit {
            background: linear-gradient(135deg, var(--success-500, #22c55e), var(--success-600, #16a34a));
            color: white;
        }
        
        .btn-edit:hover {
            background: linear-gradient(135deg, var(--success-600, #16a34a), var(--success-700, #15803d));
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
        }
        
        .btn-details {
            background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
            color: white;
        }
        
        .btn-details:hover {
            background: linear-gradient(135deg, var(--primary-600), var(--secondary-500));
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 128, 255, 0.3);
        }
        
        .status-badge {
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            white-space: nowrap;
        }
        
        .status-active {
            background: linear-gradient(135deg, var(--success-500, #22c55e), var(--success-600, #16a34a));
            color: white;
        }
        
        .status-inactive {
            background: linear-gradient(135deg, var(--error-500, #ef4444), var(--error-600, #dc2626));
            color: white;
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: var(--gray-600, #6b7280);
            background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%);
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            border: 1px solid var(--color-primary);
        }
        
        .empty-state i {
            font-size: 48px;
            margin-bottom: 16px;
            background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
    </style>
</head>
<body>
    <jsp:include page="/includes/header.jsp">
        <jsp:param name="page" value="procurement"/>
    </jsp:include>

    <div class="container">
        <div class="page-header">
            <h1 class="page-title">
                <span class="icon">🏢</span>
                Quản lý Nhà cung cấp
            </h1>
            <button class="btn-primary" onclick="openAddModal()">
                <i class='bx bx-plus'></i>
                Thêm nhà cung cấp
            </button>
        </div>

        <c:choose>
            <c:when test="${suppliers != null && suppliers.size() > 0}">
                <div class="table-wrapper">
                <table class="supplier-table">
                    <thead>
                        <tr>
                            <th>Tên nhà cung cấp</th>
                            <th>Người liên hệ</th>
                            <th>Email</th>
                            <th>Số điện thoại</th>
                            <th>Đánh giá</th>
                            <th>Tỷ lệ đúng hẹn</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="supplier" items="${suppliers}">
                            <tr>
                                <td>
                                    <strong>${supplier.name != null ? supplier.name : 'Chưa cập nhật'}</strong>
                                </td>
                                <td>${supplier.contact != null ? supplier.contact : 'Chưa cập nhật'}</td>
                                <td>${supplier.email != null ? supplier.email : 'Chưa cập nhật'}</td>
                                <td>${supplier.phone != null ? supplier.phone : 'Chưa cập nhật'}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${supplier.rating != null && supplier.rating > 0}">
                                            ${supplier.rating}/5
                                        </c:when>
                                        <c:otherwise>
                                            Chưa đánh giá
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${supplier.onTimeRate != null && supplier.onTimeRate > 0}">
                                            ${supplier.onTimeRate}%
                                        </c:when>
                                        <c:otherwise>
                                            Chưa có dữ liệu
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="status-badge ${supplier.isActive ? 'status-active' : 'status-inactive'}">
                                        ${supplier.isActive ? 'Hoạt động' : 'Không hoạt động'}
                                    </span>
                                </td>
                                <td>
                                    <button class="btn-edit" onclick="editSupplier('${supplier.supplierID}')">
                                        <i class='bx bx-edit'></i>
                                        Sửa
                                    </button>
                                    <button class="btn-details" onclick="viewDetails('${supplier.supplierID}')">
                                        <i class='bx bx-show'></i>
                                        Chi tiết
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <i class='bx bx-store'></i>
                    <h3>Chưa có nhà cung cấp nào</h3>
                    <p>Hãy thêm nhà cung cấp đầu tiên để bắt đầu quản lý</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Edit Modal -->
    <div id="editModal" class="modal" style="display: none;">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Sửa nhà cung cấp</h2>
                <span class="close" onclick="closeEditModal()">&times;</span>
            </div>
            <div class="modal-body">
                <form id="editForm">
                    <div class="form-group">
                        <label for="editName">Tên nhà cung cấp *</label>
                        <input type="text" id="editName" name="name" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="editContact">Người liên hệ</label>
                        <input type="text" id="editContact" name="contact">
                    </div>
                    
                    <div class="form-group">
                        <label for="editEmail">Email</label>
                        <input type="email" id="editEmail" name="email">
                    </div>
                    
                    <div class="form-group">
                        <label for="editPhone">Số điện thoại</label>
                        <input type="tel" id="editPhone" name="phone">
                    </div>
                    
                    <div class="form-group">
                        <label for="editAddress">Địa chỉ</label>
                        <textarea id="editAddress" name="address" rows="3"></textarea>
                    </div>
                    
                    <div class="form-group">
                        <label for="editTaxCode">Mã số thuế</label>
                        <input type="text" id="editTaxCode" name="taxCode" placeholder="Nhập mã số thuế">
                    </div>
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="editRating">Đánh giá (1-5)</label>
                            <input type="number" id="editRating" name="rating" min="0" max="5" step="0.1">
                        </div>
                        
                        <div class="form-group">
                            <label for="editOnTimeRate">Tỷ lệ đúng hẹn (%)</label>
                            <input type="number" id="editOnTimeRate" name="onTimeRate" min="0" max="100" step="0.1">
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label>
                            <input type="checkbox" id="editIsActive" name="isActive">
                            Hoạt động
                        </label>
                    </div>
                    
                    <input type="hidden" id="editSupplierId" name="supplierId">
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeEditModal()">Hủy</button>
                <button type="button" class="btn btn-primary" onclick="saveSupplier()">Lưu thay đổi</button>
            </div>
        </div>
    </div>

    <!-- Details Modal -->
    <div id="detailsModal" class="modal" style="display: none;">
        <div class="modal-content details-modal">
            <div class="modal-header">
                <h2>Chi tiết nhà cung cấp</h2>
                <span class="close" onclick="closeDetailsModal()">&times;</span>
            </div>
            <div class="modal-body">
                <div class="supplier-details">
                    <div class="detail-section">
                        <h3>Thông tin cơ bản</h3>
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label>Tên nhà cung cấp:</label>
                                <span id="detailName">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Người liên hệ:</label>
                                <span id="detailContact">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Email:</label>
                                <span id="detailEmail">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Số điện thoại:</label>
                                <span id="detailPhone">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Mã số thuế:</label>
                                <span id="detailTaxCode">-</span>
                            </div>
                            <div class="detail-item full-width">
                                <label>Địa chỉ:</label>
                                <span id="detailAddress">-</span>
                            </div>
                        </div>
                    </div>

                    <div class="detail-section">
                        <h3>Đánh giá & Hiệu suất</h3>
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label>Đánh giá:</label>
                                <span id="detailRating">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Tỷ lệ đúng hẹn:</label>
                                <span id="detailOnTimeRate">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Trạng thái:</label>
                                <span id="detailStatus">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Ngày tạo:</label>
                                <span id="detailCreatedAt">-</span>
                            </div>
                        </div>
                    </div>

                    <div class="detail-section">
                        <h3>Thống kê đơn hàng</h3>
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label>Tổng đơn hàng:</label>
                                <span id="detailTotalOrders">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Đơn hàng thành công:</label>
                                <span id="detailSuccessfulOrders">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Đơn hàng trễ:</label>
                                <span id="detailLateOrders">-</span>
                            </div>
                            <div class="detail-item">
                                <label>Giá trị trung bình:</label>
                                <span id="detailAvgOrderValue">-</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeDetailsModal()">Đóng</button>
                <button type="button" class="btn btn-primary" onclick="editFromDetails()">Chỉnh sửa</button>
            </div>
        </div>
    </div>

    <!-- Add Supplier Modal -->
    <div id="addModal" class="modal" style="display: none;">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Thêm nhà cung cấp mới</h2>
                <span class="close" onclick="closeAddModal()">&times;</span>
            </div>
            <div class="modal-body">
                <form id="addForm">
                    <div class="form-group">
                        <label for="addName">Tên nhà cung cấp *</label>
                        <input type="text" id="addName" name="name" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="addContact">Người liên hệ</label>
                        <input type="text" id="addContact" name="contact">
                    </div>
                    
                    <div class="form-group">
                        <label for="addEmail">Email *</label>
                        <input type="email" id="addEmail" name="email" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="addPhone">Số điện thoại</label>
                        <input type="tel" id="addPhone" name="phone">
                    </div>
                    
                    <div class="form-group">
                        <label for="addAddress">Địa chỉ</label>
                        <textarea id="addAddress" name="address" rows="3"></textarea>
                    </div>
                    
                    <div class="form-group">
                        <label for="addTaxCode">Mã số thuế</label>
                        <input type="text" id="addTaxCode" name="taxCode" placeholder="Nhập mã số thuế">
                    </div>
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="addRating">Đánh giá ban đầu (1-5)</label>
                            <input type="number" id="addRating" name="rating" min="0" max="5" step="0.1" value="0">
                        </div>
                        
                        <div class="form-group">
                            <label for="addOnTimeRate">Tỷ lệ đúng hẹn (%)</label>
                            <input type="number" id="addOnTimeRate" name="onTimeRate" min="0" max="100" step="0.1" value="0">
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label>
                            <input type="checkbox" id="addIsActive" name="isActive" checked>
                            Hoạt động
                        </label>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeAddModal()">Hủy</button>
                <button type="button" class="btn btn-primary" onclick="saveNewSupplier()">Thêm nhà cung cấp</button>
            </div>
        </div>
    </div>

    <style>
        .modal {
            position: fixed !important;
            z-index: 999999 !important;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .modal-content {
            background: white;
            border-radius: 16px;
            width: 90%;
            max-width: 600px;
            max-height: 80vh;
            z-index: 1000000 !important;
            overflow-y: auto;
            overflow-x: hidden;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
            margin-top: 12vh;
            border: 1px solid var(--color-primary);
            box-sizing: border-box;
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 24px;
            border-bottom: 1px solid var(--gray-200, #e5e7eb);
            background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
            color: white;
            position: relative;
            flex-wrap: wrap;
            gap: 10px;
        }
        
        .modal-header::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--primary-500), var(--secondary-500));
        }

        .modal-header h2 {
            margin: 0;
            font-size: 20px;
            font-weight: 800;
            color: white;
            margin-top: 4px;
        }
        
        .modal-header .close {
            color: white;
            margin-top: 4px;
        }
        
        .modal-header .close:hover {
            color: rgba(255, 255, 255, 0.8);
        }

        .close {
            font-size: 24px;
            font-weight: bold;
            color: var(--gray-600, #6b7280);
            cursor: pointer;
            line-height: 1;
        }

        .close:hover {
            color: var(--gray-800, #374151);
        }

        .modal-body {
            padding: 24px;
        }

        .modal-footer {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            padding: 20px 24px;
            border-top: 1px solid #e5e7eb;
            background: #f9fafb;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 6px;
            font-weight: 500;
            color: #374151;
        }

        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            font-size: 14px;
            transition: border-color 0.2s;
        }

        .form-group input,
        .form-group textarea {
            border-color: var(--color-primary);
        }

        .form-group input:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: var(--color-primary);
            box-shadow: 0 0 0 3px rgba(0, 128, 255, 0.1);
        }

        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
        }

        .modal .btn-primary {
            background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
            color: white;
            box-shadow: 0 2px 6px rgba(0, 128, 255, 0.3);
        }

        .modal .btn-primary:hover {
            background: linear-gradient(135deg, var(--primary-600), var(--secondary-500));
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 128, 255, 0.4);
        }

        .modal .btn-secondary {
            background: var(--gray-500, #6b7280);
            color: white;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        }

        .modal .btn-secondary:hover {
            background: var(--gray-600, #4b5563);
            transform: translateY(-2px);
        }

        .loading {
            opacity: 0.7;
            pointer-events: none;
        }

        /* Details Modal Specific Styles */
        .details-modal {
            max-width: 800px;
            width: 95%;
            max-height: 75vh;
            margin-top: 10vh;
        }

        .supplier-details {
            padding: 0;
        }

        .detail-section {
            margin-bottom: 32px;
            padding-bottom: 24px;
            border-bottom: 1px solid #e5e7eb;
        }

        .detail-section:last-child {
            border-bottom: none;
            margin-bottom: 0;
        }

        .detail-section h3 {
            margin: 0 0 16px 0;
            font-size: 18px;
            font-weight: 600;
            color: var(--gray-800, #1f2937);
            padding-bottom: 8px;
            border-bottom: 2px solid var(--color-primary);
        }

        .detail-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }

        .detail-item {
            display: flex;
            flex-direction: column;
            gap: 4px;
        }

        .detail-item.full-width {
            grid-column: 1 / -1;
        }

        .detail-item label {
            font-weight: 600;
            color: #374151;
            font-size: 14px;
        }

        .detail-item span {
            color: #6b7280;
            font-size: 14px;
            padding: 8px 12px;
            background: #f9fafb;
            border-radius: 6px;
            border: 1px solid #e5e7eb;
            min-height: 20px;
        }

        .detail-item span:empty::before {
            content: "Chưa cập nhật";
            color: #9ca3af;
            font-style: italic;
        }


        /* Rating stars */
        .rating-stars {
            display: flex;
            gap: 2px;
        }

        .star {
            color: #fbbf24;
            font-size: 16px;
        }

        .star.empty {
            color: #d1d5db;
        }

        /* Force modal to be above everything */
        #editModal, #detailsModal, #addModal {
            z-index: 999999 !important;
            position: fixed !important;
        }
        
        #editModal .modal-content, #detailsModal .modal-content, #addModal .modal-content {
            z-index: 1000000 !important;
            position: relative !important;
        }

        /* Responsive */
        @media (max-width: 768px) {
            .container {
                padding: 10px;
            }
            
            .page-header {
                padding: 20px;
                flex-direction: column;
                align-items: flex-start;
            }
            
            .page-title {
                font-size: 24px;
            }
            
            .supplier-table {
                min-width: 900px;
            }
            
            .supplier-table th,
            .supplier-table td {
                padding: 12px 12px;
                font-size: 13px;
            }
            
            .detail-grid {
                grid-template-columns: 1fr;
            }
            
            .details-modal {
                width: 98%;
                margin: 10px;
                margin-top: 15vh;
            }
            
            .modal-content {
                margin-top: 15vh;
                max-height: 75vh;
                width: 95%;
            }
        }
        
        @media (max-width: 480px) {
            .page-title {
                font-size: 20px;
            }
            
            .supplier-table {
                min-width: 800px;
            }
            
            .supplier-table th,
            .supplier-table td {
                padding: 10px 10px;
                font-size: 12px;
            }
            
            .btn-edit, .btn-details {
                padding: 6px 12px;
                font-size: 12px;
            }
        }
    </style>

    <script>
        let currentSupplierData = null;

        function editSupplier(supplierId) {
            console.log('Edit supplier:', supplierId);
            
            // Fetch full supplier data from server (to get taxCode)
            fetchSupplierById(supplierId)
                .then(supplierData => {
                    if (supplierData) {
                        currentSupplierData = supplierData;
                        showEditModal(supplierData);
                    } else {
                        alert('Không tìm thấy thông tin nhà cung cấp');
                    }
                })
                .catch(error => {
                    console.error('Error fetching supplier:', error);
                    // Fallback to table data if fetch fails
                    const supplierData = findSupplierInTable(supplierId);
                    if (supplierData) {
                        currentSupplierData = supplierData;
                        showEditModal(supplierData);
                    } else {
                        alert('Không tìm thấy thông tin nhà cung cấp');
                    }
                });
        }
        
        function viewDetails(supplierId) {
            console.log('View details for supplier:', supplierId);
            
            // Fetch full supplier data from server (to get taxCode)
            fetchSupplierById(supplierId)
                .then(supplierData => {
                    if (supplierData) {
                        currentSupplierData = supplierData;
                        showDetailsModal(supplierData);
                    } else {
                        alert('Không tìm thấy thông tin nhà cung cấp');
                    }
                })
                .catch(error => {
                    console.error('Error fetching supplier:', error);
                    // Fallback to table data if fetch fails
                    const supplierData = findSupplierInTable(supplierId);
                    if (supplierData) {
                        currentSupplierData = supplierData;
                        showDetailsModal(supplierData);
                    } else {
                        alert('Không tìm thấy thông tin nhà cung cấp');
                    }
                });
        }
        
        /**
         * Fetch supplier data from server by ID
         * Returns Promise with supplier data object
         */
        function fetchSupplierById(supplierId) {
            const contextPath = '${pageContext.request.contextPath}';
            const url = contextPath + '/procurement/supplier?id=' + encodeURIComponent(supplierId) + '&format=json';
            
            return fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(result => {
                if (result.success && result.data) {
                    return {
                        id: result.data.id,
                        name: result.data.name || '',
                        contact: result.data.contact || '',
                        email: result.data.email || '',
                        phone: result.data.phone || '',
                        address: result.data.address || '',
                        taxCode: result.data.taxCode || '',
                        rating: result.data.rating || 0,
                        onTimeRate: result.data.onTimeRate || 0,
                        isActive: result.data.isActive || false
                    };
                }
                return null;
            });
        }

        function findSupplierInTable(supplierId) {
            const table = document.querySelector('.supplier-table');
            if (!table) return null;
            
            const rows = table.getElementsByTagName('tr');
            
            for (let i = 1; i < rows.length; i++) {
                const cells = rows[i].getElementsByTagName('td');
                if (cells.length >= 7) {
                    const actionButtons = cells[cells.length - 1].querySelectorAll('button');
                    for (let btn of actionButtons) {
                        if (btn.onclick && btn.onclick.toString().includes(supplierId)) {
                            return {
                                id: supplierId,
                                name: cells[0] ? cells[0].textContent.trim() : '',
                                contact: cells[1] ? cells[1].textContent.trim() : '',
                                email: cells[2] ? cells[2].textContent.trim() : '',
                                phone: cells[3] ? cells[3].textContent.trim() : '',
                                rating: extractNumber(cells[4].textContent),
                                onTimeRate: extractNumber(cells[5].textContent),
                                isActive: cells[6].textContent.includes('Hoạt động')
                            };
                        }
                    }
                }
            }
            return null;
        }

        function extractNumber(text) {
            const match = text.match(/(\d+\.?\d*)/);
            return match ? parseFloat(match[1]) : 0;
        }

        function showEditModal(supplierData) {
            // Populate form with current data
            document.getElementById('editSupplierId').value = supplierData.id;
            document.getElementById('editName').value = supplierData.name || '';
            document.getElementById('editContact').value = supplierData.contact || '';
            document.getElementById('editEmail').value = supplierData.email || '';
            document.getElementById('editPhone').value = supplierData.phone || '';
            document.getElementById('editAddress').value = supplierData.address || '';
            document.getElementById('editTaxCode').value = supplierData.taxCode || '';
            document.getElementById('editRating').value = supplierData.rating || 0;
            document.getElementById('editOnTimeRate').value = supplierData.onTimeRate || 0;
            document.getElementById('editIsActive').checked = supplierData.isActive;
            
            // Show modal
            document.getElementById('editModal').style.display = 'flex';
        }

        function closeEditModal() {
            document.getElementById('editModal').style.display = 'none';
            currentSupplierData = null;
        }

        function showDetailsModal(supplierData) {
            // Populate detail fields
            document.getElementById('detailName').textContent = supplierData.name || 'Chưa cập nhật';
            document.getElementById('detailContact').textContent = supplierData.contact || 'Chưa cập nhật';
            document.getElementById('detailEmail').textContent = supplierData.email || 'Chưa cập nhật';
            document.getElementById('detailPhone').textContent = supplierData.phone || 'Chưa cập nhật';
            document.getElementById('detailTaxCode').textContent = supplierData.taxCode || 'Chưa cập nhật';
            document.getElementById('detailAddress').textContent = supplierData.address || 'Chưa cập nhật';
            
            // Rating with stars
            const ratingElement = document.getElementById('detailRating');
            if (supplierData.rating && supplierData.rating > 0) {
                ratingElement.innerHTML = generateStarRating(supplierData.rating) + ' (' + supplierData.rating + '/5)';
            } else {
                ratingElement.textContent = 'Chưa đánh giá';
            }
            
            // On-time rate
            const onTimeRateElement = document.getElementById('detailOnTimeRate');
            if (supplierData.onTimeRate && supplierData.onTimeRate > 0) {
                onTimeRateElement.textContent = supplierData.onTimeRate + '%';
            } else {
                onTimeRateElement.textContent = 'Chưa có dữ liệu';
            }
            
            // Status
            const statusElement = document.getElementById('detailStatus');
            if (supplierData.isActive) {
                statusElement.innerHTML = '<span class="status-badge status-active">Hoạt động</span>';
            } else {
                statusElement.innerHTML = '<span class="status-badge status-inactive">Không hoạt động</span>';
            }
            
            // Created date (placeholder)
            document.getElementById('detailCreatedAt').textContent = 'Chưa có thông tin';
            
            // Statistics (placeholder data)
            document.getElementById('detailTotalOrders').textContent = '0';
            document.getElementById('detailSuccessfulOrders').textContent = '0';
            document.getElementById('detailLateOrders').textContent = '0';
            document.getElementById('detailAvgOrderValue').textContent = '0 VND';
            
            // Show modal
            document.getElementById('detailsModal').style.display = 'flex';
        }

        function closeDetailsModal() {
            document.getElementById('detailsModal').style.display = 'none';
            currentSupplierData = null;
        }

        function editFromDetails() {
            closeDetailsModal();
            if (currentSupplierData) {
                showEditModal(currentSupplierData);
            }
        }

        function openAddModal() {
            console.log('Opening add supplier modal');
            
            // Clear form
            document.getElementById('addForm').reset();
            
            // Set default values
            document.getElementById('addIsActive').checked = true;
            document.getElementById('addRating').value = 0;
            document.getElementById('addOnTimeRate').value = 0;
            
            // Show modal
            document.getElementById('addModal').style.display = 'flex';
        }

        function closeAddModal() {
            document.getElementById('addModal').style.display = 'none';
        }

        function saveNewSupplier() {
            const form = document.getElementById('addForm');
            
            // Get form values
            const data = {
                action: 'create',
                name: document.getElementById('addName').value,
                contact: document.getElementById('addContact').value,
                email: document.getElementById('addEmail').value,
                phone: document.getElementById('addPhone').value,
                address: document.getElementById('addAddress').value,
                taxCode: document.getElementById('addTaxCode').value,
                rating: parseFloat(document.getElementById('addRating').value) || 0,
                onTimeRate: parseFloat(document.getElementById('addOnTimeRate').value) || 0,
                isActive: document.getElementById('addIsActive').checked
            };
            
            console.log('Creating new supplier:', data);
            
            // Validate required fields
            if (!data.name || data.name.trim() === '') {
                alert('Tên nhà cung cấp không được để trống');
                return;
            }
            
            if (!data.email || data.email.trim() === '') {
                alert('Email không được để trống');
                return;
            }
            
            // Basic email validation
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(data.email)) {
                alert('Email không đúng định dạng');
                return;
            }
            
            // Show loading state
            const saveBtn = document.querySelector('#addModal .btn-primary');
            saveBtn.textContent = 'Đang thêm...';
            saveBtn.classList.add('loading');
            
            // Send create request
            fetch('${pageContext.request.contextPath}/procurement/supplier', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8',
                },
                body: JSON.stringify(data)
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alert('Thêm nhà cung cấp thành công!');
                    closeAddModal();
                    location.reload(); // Refresh page to show new supplier
                } else {
                    alert('Lỗi: ' + (result.message || 'Không thể thêm nhà cung cấp'));
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Lỗi kết nối: ' + error.message);
            })
            .finally(() => {
                // Reset button state
                saveBtn.textContent = 'Thêm nhà cung cấp';
                saveBtn.classList.remove('loading');
            });
        }

        function generateStarRating(rating) {
            let stars = '';
            const fullStars = Math.floor(rating);
            const hasHalfStar = rating % 1 !== 0;
            
            for (let i = 0; i < fullStars; i++) {
                stars += '<span class="star">★</span>';
            }
            
            if (hasHalfStar) {
                stars += '<span class="star">☆</span>';
            }
            
            const emptyStars = 5 - Math.ceil(rating);
            for (let i = 0; i < emptyStars; i++) {
                stars += '<span class="star empty">★</span>';
            }
            
            return '<span class="rating-stars">' + stars + '</span>';
        }

        function saveSupplier() {
            const form = document.getElementById('editForm');
            
            // Get form values directly to avoid FormData issues
            const data = {
                action: 'update',
                supplierId: document.getElementById('editSupplierId').value,
                name: document.getElementById('editName').value,
                contact: document.getElementById('editContact').value,
                email: document.getElementById('editEmail').value,
                phone: document.getElementById('editPhone').value,
                address: document.getElementById('editAddress').value,
                taxCode: document.getElementById('editTaxCode').value,
                rating: parseFloat(document.getElementById('editRating').value) || 0,
                onTimeRate: parseFloat(document.getElementById('editOnTimeRate').value) || 0,
                isActive: document.getElementById('editIsActive').checked
            };
            
            console.log('Sending data:', data);
            
            // Show loading state
            const saveBtn = document.querySelector('.btn-primary');
            saveBtn.textContent = 'Đang lưu...';
            saveBtn.classList.add('loading');
            
            // Send update request
            fetch('${pageContext.request.contextPath}/procurement/supplier', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8',
                },
                body: JSON.stringify(data)
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alert('Cập nhật nhà cung cấp thành công!');
                    closeEditModal();
                    location.reload(); // Refresh page to show updated data
                } else {
                    alert('Lỗi: ' + (result.message || 'Không thể cập nhật nhà cung cấp'));
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Lỗi kết nối: ' + error.message);
            })
            .finally(() => {
                // Reset button state
                saveBtn.textContent = 'Lưu thay đổi';
                saveBtn.classList.remove('loading');
            });
        }

        // Close modal when clicking outside
        window.onclick = function(event) {
            const editModal = document.getElementById('editModal');
            const detailsModal = document.getElementById('detailsModal');
            const addModal = document.getElementById('addModal');
            
            if (event.target === editModal) {
                closeEditModal();
            } else if (event.target === detailsModal) {
                closeDetailsModal();
            } else if (event.target === addModal) {
                closeAddModal();
            }
        }
        
        // Initialize page
        document.addEventListener('DOMContentLoaded', function() {
            console.log('✅ Supplier list page loaded successfully');
        });
    </script>
</body>
</html>
</html>