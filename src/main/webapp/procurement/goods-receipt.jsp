<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhận hàng - LiteFlow</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            padding: 20px;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #e0e0e0;
        }
        .header h1 {
            color: #2c3e50;
            margin: 0;
        }
        .btn {
            background: #3498db;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            font-size: 14px;
        }
        .btn:hover {
            background: #2980b9;
        }
        .btn-success {
            background: #27ae60;
        }
        .btn-success:hover {
            background: #229954;
        }
        .btn-warning {
            background: #f39c12;
        }
        .btn-warning:hover {
            background: #e67e22;
        }
        .table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        .table th, .table td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        .table th {
            background-color: #34495e;
            color: white;
            font-weight: 600;
        }
        .table tr:hover {
            background-color: #f8f9fa;
        }
        .status-partial {
            background: #f39c12;
            color: white;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
        }
        .status-full {
            background: #27ae60;
            color: white;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
        }
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
        }
        .modal-content {
            background-color: white;
            margin: 5% auto;
            padding: 20px;
            border-radius: 8px;
            width: 80%;
            max-width: 600px;
        }
        .close {
            color: #aaa;
            float: right;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
        }
        .close:hover {
            color: black;
        }
        .form-group {
            margin-bottom: 15px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        .form-group input, .form-group select, .form-group textarea {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        .po-details {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .po-details h3 {
            margin-top: 0;
            color: #2c3e50;
        }
        .item-list {
            margin-top: 15px;
        }
        .item-list table {
            width: 100%;
            border-collapse: collapse;
        }
        .item-list th, .item-list td {
            padding: 8px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        .item-list th {
            background-color: #e9ecef;
        }
    </style>
</head>
<body>
    <jsp:include page="../includes/header.jsp">
        <jsp:param name="page" value="procurement" />
    </jsp:include>
    
    <div class="container">
        <div class="header">
            <h1>📦 Nhận hàng</h1>
            <button class="btn btn-success" onclick="openReceiveModal()">+ Nhận hàng mới</button>
        </div>

        <table class="table">
            <thead>
                <tr>
                    <th>Mã phiếu nhận</th>
                    <th>Mã PO</th>
                    <th>Nhà cung cấp</th>
                    <th>Ngày nhận</th>
                    <th>Người nhận</th>
                    <th>Trạng thái</th>
                    <th>Ghi chú</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="receipt" items="${goodsReceipts}">
                    <tr>
                        <td><strong>GR-${receipt.receiptID.toString().substring(0,8)}</strong></td>
                        <td>PO-${receipt.poid.toString().substring(0,8)}</td>
                        <td>
                            <!-- TODO: Get supplier name from PO -->
                            Nhà cung cấp
                        </td>
                        <td><fmt:formatDate value="${receipt.receiveDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td>
                            <!-- TODO: Get user name -->
                            Người nhận
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${receipt.status == 'PARTIAL'}">
                                    <span class="status-partial">Nhận một phần</span>
                                </c:when>
                                <c:when test="${receipt.status == 'FULL'}">
                                    <span class="status-full">Nhận đầy đủ</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-partial">${receipt.status}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>${receipt.notes}</td>
                        <td>
                            <button class="btn btn-warning" onclick="viewDetails('${receipt.receiptID}')">Chi tiết</button>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>

    <!-- Modal nhận hàng -->
    <div id="receiveModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal()">&times;</span>
            <h2>Nhận hàng</h2>
            
            <div class="form-group">
                <label for="poSelect">Chọn đơn hàng *</label>
                <select id="poSelect" onchange="loadPODetails()" required>
                    <option value="">Chọn đơn hàng</option>
                    <c:forEach var="po" items="${approvedPOs}">
                        <option value="${po.poid}">PO-${po.poid.toString().substring(0,8)} - 
                            <fmt:formatNumber value="${po.totalAmount}" type="currency" currencyCode="VND"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div id="poDetails" class="po-details" style="display: none;">
                <h3>Chi tiết đơn hàng</h3>
                <div id="poInfo"></div>
                <div class="item-list">
                    <h4>Danh sách sản phẩm</h4>
                    <table id="itemsTable">
                        <thead>
                            <tr>
                                <th>Sản phẩm</th>
                                <th>Số lượng đặt</th>
                                <th>Đơn giá</th>
                                <th>Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody id="itemsBody">
                        </tbody>
                    </table>
                </div>
            </div>

            <form id="receiveForm" action="${pageContext.request.contextPath}/procurement/gr" method="post">
                <input type="hidden" id="poid" name="poid">
                
                <div class="form-group">
                    <label for="status">Trạng thái nhận hàng *</label>
                    <select id="status" name="status" required>
                        <option value="PARTIAL">Nhận một phần</option>
                        <option value="FULL">Nhận đầy đủ</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="notes">Ghi chú</label>
                    <textarea id="notes" name="notes" rows="3" placeholder="Ghi chú về tình trạng hàng hóa, chất lượng..."></textarea>
                </div>
                
                <div style="text-align: right; margin-top: 20px;">
                    <button type="button" class="btn" onclick="closeModal()">Hủy</button>
                    <button type="submit" class="btn btn-success">Xác nhận nhận hàng</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openReceiveModal() {
            document.getElementById('receiveModal').style.display = 'block';
        }

        function closeModal() {
            document.getElementById('receiveModal').style.display = 'none';
            document.getElementById('poDetails').style.display = 'none';
            document.getElementById('receiveForm').reset();
        }

        function loadPODetails() {
            const poId = document.getElementById('poSelect').value;
            if (poId) {
                document.getElementById('poid').value = poId;
                document.getElementById('poDetails').style.display = 'block';
                
                // TODO: Load PO details via AJAX
                // For now, show placeholder data
                document.getElementById('poInfo').innerHTML = `
                    <p><strong>Mã PO:</strong> PO-${poId.substring(0,8)}</p>
                    <p><strong>Nhà cung cấp:</strong> Nhà cung cấp ABC</p>
                    <p><strong>Ngày đặt:</strong> <%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()) %></p>
                    <p><strong>Tổng tiền:</strong> 2,500,000 VND</p>
                `;
                
                // TODO: Load PO items
                document.getElementById('itemsBody').innerHTML = `
                    <tr>
                        <td>Cà phê Arabica Premium</td>
                        <td>50</td>
                        <td>25,000 VND</td>
                        <td>1,250,000 VND</td>
                    </tr>
                    <tr>
                        <td>Cà phê Robusta Đặc biệt</td>
                        <td>30</td>
                        <td>20,000 VND</td>
                        <td>600,000 VND</td>
                    </tr>
                `;
            } else {
                document.getElementById('poDetails').style.display = 'none';
            }
        }

        function viewDetails(receiptId) {
            // TODO: Show receipt details in modal or redirect to details page
            alert('Xem chi tiết phiếu nhận: ' + receiptId);
        }

        // Close modal when clicking outside
        window.onclick = function(event) {
            const modal = document.getElementById('receiveModal');
            if (event.target === modal) {
                closeModal();
            }
        }
    </script>
</body>
</html>
