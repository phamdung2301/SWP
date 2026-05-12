<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="../includes/header.jsp">
  <jsp:param name="page" value="rooms" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/roomtable.css">
<script src="${pageContext.request.contextPath}/js/roomtable-enhanced.js"></script>

<div class="content">
    <!-- Statistics -->
    <div class="stats">
        <div class="stat-card">
            <div class="stat-number">${rooms.size()}</div>
            <div class="stat-label">Tổng số phòng</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">${tables.size()}</div>
            <div class="stat-label">Tổng số bàn</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">
                <c:set var="availableTables" value="0" />
                <c:forEach var="table" items="${tables}">
                    <c:if test="${table.status == 'Available'}">
                        <c:set var="availableTables" value="${availableTables + 1}" />
                    </c:if>
                </c:forEach>
                ${availableTables}
            </div>
            <div class="stat-label">Bàn trống</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">
                <c:set var="occupiedTables" value="0" />
                <c:forEach var="table" items="${tables}">
                    <c:if test="${table.status == 'Occupied'}">
                        <c:set var="occupiedTables" value="${occupiedTables + 1}" />
                    </c:if>
                </c:forEach>
                ${occupiedTables}
            </div>
            <div class="stat-label">Bàn đang sử dụng</div>
        </div>
    </div>

    <!-- Success/Error Messages -->
    <c:if test="${not empty success}">
        <div style="background: #d4edda; color: #155724; padding: 1rem; border-radius: 6px; margin-bottom: 1rem; border: 1px solid #c3e6cb;">
            ✅ ${success}
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div style="background: #f8d7da; color: #721c24; padding: 1rem; border-radius: 6px; margin-bottom: 1rem; border: 1px solid #f5c6cb;">
            ❌ ${error}
        </div>
    </c:if>

    <!-- Toolbar -->
    <div class="toolbar">
        <div class="search-box">
            <input type="text" class="search-input" placeholder="Tìm kiếm phòng, bàn..." id="searchInput">
            <button class="btn btn-primary" onclick="searchItems()">Tìm kiếm</button>
        </div>
        <div>
            <a href="#" class="btn btn-success" onclick="addRoom()">Thêm phòng</a>
            <a href="#" class="btn btn-primary" onclick="addTable()">Thêm bàn</a>
            <button class="btn btn-success" onclick="showImportModal()">
                Nhập Excel
            </button>
            <button class="btn btn-primary" onclick="exportToExcel()">
                Xuất Excel
            </button>
        </div>
    </div>

    <!-- Rooms Section -->
    <div class="room-table-container">
        <div class="section-title">Danh sách phòng</div>
        
        <!-- Empty state (always present, hidden when there are rooms) -->
        <div class="empty-state" <c:if test="${not empty rooms}">style="display: none;"</c:if>>
            <h3>Chưa có phòng nào</h3>
            <p>Hãy thêm phòng đầu tiên để bắt đầu quản lý</p>
            <a href="#" class="btn btn-success" onclick="addRoom()" style="margin-top: 1rem;">Thêm phòng</a>
        </div>
        
        <!-- Rooms table (hidden when there are no rooms) -->
        <c:if test="${not empty rooms}">
            <table class="table">
                    <thead>
                        <tr>
                            <th class="sortable" onclick="sortTable(0, 'string', 'rooms')">
                                Tên phòng
                                <span class="sort-icon"></span>
                            </th>
                            <th>
                                Mô tả
                            </th>
                            <th class="sortable" onclick="sortTable(2, 'date', 'rooms')">
                                Ngày tạo
                                <span class="sort-icon"></span>
                            </th>
                            <th class="sortable" onclick="sortTable(3, 'number', 'rooms')">
                                Số lượng bàn
                                <span class="sort-icon"></span>
                            </th>
                            <th class="sortable" onclick="sortTable(4, 'number', 'rooms')">
                                Tổng sức chứa
                                <span class="sort-icon"></span>
                            </th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="room" items="${rooms}">
                            <tr data-room-id="${room.roomId}">
                                <td>
                                    <div class="room-name">${room.name}</div>
                                </td>
                                <td>${room.description != null ? room.description : 'Không có mô tả'}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${room.createdAt != null}">
                                            <span class="formatted-date" data-date="${room.createdAt}">Loading...</span>
                                        </c:when>
                                        <c:otherwise>
                                            N/A
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${room.tableCount != null && room.tableCount > 0}">
                                            <span class="table-count-badge">Tối đa ${room.tableCount} bàn</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="table-count-badge">Chưa thiết lập</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${room.totalCapacity != null && room.totalCapacity > 0}">
                                            <span class="capacity-badge">Tối đa ${room.totalCapacity} người</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="capacity-badge">Chưa thiết lập</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="actions">
                                        <button class="btn btn-warning btn-sm" onclick="editRoom('${room.roomId}')">
                                            Sửa
                                        </button>
                                        <button class="btn btn-danger btn-sm" onclick="deleteRoom('${room.roomId}', event); return false;">
                                            Xóa
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                
                <!-- Rooms Pagination -->
                <div class="pagination-container" id="roomsPagination">
                    <div class="pagination-info" id="roomsPageInfo">
                        Trang 1 / 1
                    </div>
                    <div class="pagination-controls">
                        <button class="pagination-btn" id="roomsPrevBtn" onclick="changeRoomsPage(-1)" disabled>
                            ← Trước
                        </button>
                        <div class="pagination-numbers" id="roomsPageNumbers">
                            <span class="pagination-number active">1</span>
                        </div>
                        <button class="pagination-btn" id="roomsNextBtn" onclick="changeRoomsPage(1)" disabled>
                            Sau →
                        </button>
                    </div>
                    <div class="pagination-size">
                        <label for="roomsPageSize">Hiển thị:</label>
                        <select id="roomsPageSize" onchange="changeRoomsPageSize(this.value)">
                            <option value="5" selected>5</option>
                            <option value="10">10</option>
                            <option value="20">20</option>
                            <option value="50">50</option>
                        </select>
                    </div>
                </div>
        </c:if>
    </div>

    <!-- Tables Section -->
    <div class="room-table-container">
        <div class="section-title">Danh sách bàn</div>
        
        <!-- Empty state (always present, hidden when there are tables) -->
        <div class="empty-state" <c:if test="${not empty tables}">style="display: none;"</c:if>>
            <h3>Chưa có bàn nào</h3>
            <p>Hãy thêm bàn đầu tiên để bắt đầu quản lý</p>
            <a href="#" class="btn btn-success" onclick="addTable()" style="margin-top: 1rem;">Thêm bàn</a>
        </div>
        
        <!-- Tables table (hidden when there are no tables) -->
        <c:if test="${not empty tables}">
                <table class="table">
                    <thead>
                        <tr>
                            <th class="sortable" onclick="sortTable(0, 'string', 'tables')">
                                Số bàn
                                <span class="sort-icon"></span>
                            </th>
                            <th class="sortable" onclick="sortTable(1, 'string', 'tables')">
                                Tên bàn
                                <span class="sort-icon"></span>
                            </th>
                            <th class="sortable" onclick="sortTable(2, 'string', 'tables')">
                                Phòng
                                <span class="sort-icon"></span>
                            </th>
                            <th class="sortable" onclick="sortTable(3, 'number', 'tables')">
                                Sức chứa
                                <span class="sort-icon"></span>
                            </th>
                            <th class="sortable" onclick="sortTable(4, 'string', 'tables')">
                                Trạng thái
                                <span class="sort-icon"></span>
                            </th>
                            <th class="sortable" onclick="sortTable(5, 'date', 'tables')">
                                Ngày tạo
                                <span class="sort-icon"></span>
                            </th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="table" items="${tables}">
                            <tr data-table-id="${table.tableId}" <c:if test="${table.room != null}">data-room-id="${table.room.roomId}"</c:if>>
                                <td>
                                    <span class="table-number">${table.tableNumber}</span>
                                </td>
                                <td>
                                    <span class="table-name">${table.tableName != null ? table.tableName : 'Không có tên'}</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${table.room != null}">
                                            <span class="room-badge">${table.room.name}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="no-room">Chưa phân phòng</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="capacity-badge">${table.capacity != null ? table.capacity : 4} người</span>
                                </td>
                                <td>
                                    <span class="status ${table.status.toLowerCase()}">
                                        <c:choose>
                                            <c:when test="${table.status == 'Available'}">Trống</c:when>
                                            <c:when test="${table.status == 'Occupied'}">Đang sử dụng</c:when>
                                            <c:when test="${table.status == 'Reserved'}">Đã đặt</c:when>
                                            <c:when test="${table.status == 'Maintenance'}">Bảo trì</c:when>
                                            <c:otherwise>${table.status}</c:otherwise>
                                        </c:choose>
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${table.createdAt != null}">
                                            <span class="formatted-date" data-date="${table.createdAt}">Loading...</span>
                                        </c:when>
                                        <c:otherwise>
                                            N/A
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="actions">
                                        <button class="btn btn-warning btn-sm" onclick="editTable('${table.tableId}')">
                                            Sửa
                                        </button>
                                        <button class="btn btn-success btn-sm" onclick="viewTableHistory('${table.tableId}')">
                                            Lịch sử
                                        </button>
                                        <button class="btn btn-danger btn-sm" onclick="deleteTable('${table.tableId}', event); return false;">
                                            Xóa
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                
                <!-- Tables Pagination -->
                <div class="pagination-container" id="tablesPagination">
                    <div class="pagination-info" id="tablesPageInfo">
                        Trang 1 / 1
                    </div>
                    <div class="pagination-controls">
                        <button class="pagination-btn" id="tablesPrevBtn" onclick="changeTablesPage(-1)" disabled>
                            ← Trước
                        </button>
                        <div class="pagination-numbers" id="tablesPageNumbers">
                            <span class="pagination-number active">1</span>
                        </div>
                        <button class="pagination-btn" id="tablesNextBtn" onclick="changeTablesPage(1)" disabled>
                            Sau →
                        </button>
                    </div>
                    <div class="pagination-size">
                        <label for="tablesPageSize">Hiển thị:</label>
                        <select id="tablesPageSize" onchange="changeTablesPageSize(this.value)">
                            <option value="5" selected>5</option>
                            <option value="10">10</option>
                            <option value="20">20</option>
                            <option value="50">50</option>
                        </select>
                    </div>
                </div>
        </c:if>
    </div>
</div>

<script>
    // Functions moved to roomtable-enhanced.js

    // Auto search when typing
    document.getElementById('searchInput').addEventListener('input', searchItems);

    // Debug function to check table data
    function debugTables() {
        console.log('=== DEBUG TABLES ===');
        
        // Check rooms table
        const roomContainer = document.querySelector('.room-table-container');
        if (roomContainer) {
            const roomTable = roomContainer.querySelector('.table');
            const roomRows = roomTable ? roomTable.querySelectorAll('tbody tr') : [];
            console.log('Rooms table found:', roomTable !== null);
            console.log('Rooms rows count:', roomRows.length);
            
            // Debug headers
            const headers = roomTable ? roomTable.querySelectorAll('th') : [];
            console.log('Rooms headers count:', headers.length);
            headers.forEach((header, index) => {
                console.log(`  Header ${index}:`, header.textContent.trim(), 'sortable:', header.classList.contains('sortable'));
            });
            
            // Debug first row
            if (roomRows.length > 0) {
                const firstRow = roomRows[0];
                console.log('First room row cells:', firstRow.cells.length);
                for (let i = 0; i < firstRow.cells.length; i++) {
                    console.log(`  Cell ${i}:`, firstRow.cells[i].textContent.trim());
                }
            }
        } else {
            console.log('Rooms container not found');
        }
        
        // Check tables table
        const tableContainers = document.querySelectorAll('.room-table-container');
        if (tableContainers.length > 1) {
            const tableTable = tableContainers[1].querySelector('.table');
            const tableRows = tableTable ? tableTable.querySelectorAll('tbody tr') : [];
            console.log('Tables table found:', tableTable !== null);
            console.log('Tables rows count:', tableRows.length);
            
            // Debug headers
            const headers = tableTable ? tableTable.querySelectorAll('th') : [];
            console.log('Tables headers count:', headers.length);
            headers.forEach((header, index) => {
                console.log(`  Header ${index}:`, header.textContent.trim(), 'sortable:', header.classList.contains('sortable'));
            });
            
            // Debug first row
            if (tableRows.length > 0) {
                const firstRow = tableRows[0];
                console.log('First table row cells:', firstRow.cells.length);
                for (let i = 0; i < firstRow.cells.length; i++) {
                    console.log(`  Cell ${i}:`, firstRow.cells[i].textContent.trim());
                }
            }
        } else {
            console.log('Tables container not found');
        }
    }

    // Run debug when page loads
    window.addEventListener('load', function() {
        setTimeout(debugTables, 1000); // Wait 1 second for data to load
    });
    
    // Global test function
    window.testSort = function() {
        console.log('🧪 Testing sort function...');
        testSorting();
    };

    // Test function for sorting
    function testSorting() {
        console.log('🧪 Testing sorting...');
        console.log('Available containers:', document.querySelectorAll('.room-table-container').length);
        
        // Test bảng phòng
        const roomTable = document.querySelector('.room-table-container .table');
        if (roomTable) {
            const roomRows = roomTable.querySelectorAll('tbody tr');
            console.log('Room table found with', roomRows.length, 'rows');
            
            // Test sort tên phòng
            console.log('Testing room name sort...');
            sortTable(0, 'string', 'rooms');
        }
        
        // Test bảng bàn
        const tableContainers = document.querySelectorAll('.room-table-container');
        if (tableContainers.length > 1) {
            const tableTable = tableContainers[1].querySelector('.table');
            if (tableTable) {
                const tableRows = tableTable.querySelectorAll('tbody tr');
                console.log('Table table found with', tableRows.length, 'rows');
                
                // Test sort số bàn
                console.log('Testing table number sort...');
                sortTable(0, 'string', 'tables');
            }
        }
    }

    // Close modal when clicking outside
    window.onclick = function (event) {
        const roomModal = document.getElementById('addRoomModal');
        const tableModal = document.getElementById('addTableModal');
        if (event.target === roomModal) {
            closeAddRoomModal();
        }
        if (event.target === tableModal) {
            closeAddTableModal();
        }
    }
</script>

<!-- Add Room Modal -->
<div id="addRoomModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2>Thêm phòng mới</h2>
            <span class="close" onclick="closeAddRoomModal()">&times;</span>
        </div>
        <form id="addRoomForm" action="roomtable" method="post">
            <input type="hidden" name="action" value="addRoom">
            <div class="modal-body">
                <div class="form-group">
                    <label for="roomName">Tên phòng *</label>
                    <input type="text" id="roomName" name="roomName" required 
                           placeholder="Nhập tên phòng">
                </div>
                <div class="form-group">
                    <label for="roomDescription">Mô tả phòng</label>
                    <textarea id="roomDescription" name="roomDescription" 
                              placeholder="Nhập mô tả phòng (tùy chọn)" rows="4"></textarea>
                </div>
                <div class="form-group">
                    <label for="roomTableCount">Số lượng bàn *</label>
                    <input type="number" id="roomTableCount" name="roomTableCount" required 
                           min="0" max="50" placeholder="Nhập số lượng bàn">
                </div>
                <div class="form-group">
                    <label for="roomTotalCapacity">Tổng sức chứa *</label>
                    <input type="number" id="roomTotalCapacity" name="roomTotalCapacity" required 
                           min="1" max="1000" placeholder="Nhập tổng sức chứa">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-warning" onclick="closeAddRoomModal()">
                    ❌ Hủy
                </button>
                <button type="button" class="btn btn-success" onclick="submitAddRoom(event)">
                    ✅ Thêm phòng
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Add Table Modal -->
<div id="addTableModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2>Thêm bàn mới</h2>
            <span class="close" onclick="closeAddTableModal()">&times;</span>
        </div>
        <form id="addTableForm" action="roomtable" method="post">
            <input type="hidden" name="action" value="addTable">
            <div class="modal-body">
                <div class="form-group">
                    <label for="tableNumber">Số bàn *</label>
                    <input type="text" id="tableNumber" name="tableNumber" required 
                           placeholder="Nhập số bàn">
                </div>
                <div class="form-group">
                    <label for="tableName">Tên bàn *</label>
                    <input type="text" id="tableName" name="tableName" required 
                           placeholder="Nhập tên bàn">
                </div>
                <div class="form-group">
                    <label for="roomId">Phòng</label>
                    <select id="roomId" name="roomId" onchange="updateRoomLimits()">
                        <option value="">Chọn phòng (tùy chọn)</option>
                        <c:forEach var="room" items="${rooms}">
                            <option value="${room.roomId}" 
                                    data-table-count="${room.tableCount}" 
                                    data-total-capacity="${room.totalCapacity}"
                                    data-room-name="${room.name}">
                                ${room.name} (Tối đa ${room.tableCount} bàn, ${room.totalCapacity} người)
                            </option>
                        </c:forEach>
                    </select>
                    <div id="roomLimitsInfo" class="room-limits-info" style="display: none;">
                        <small class="text-info">
                            <strong>Giới hạn phòng:</strong><br>
                            <span id="currentTableCount">0</span> / <span id="maxTableCount">0</span> bàn<br>
                            <span id="currentTotalCapacity">0</span> / <span id="maxTotalCapacity">0</span> người
                        </small>
                    </div>
                </div>
                <div class="form-group">
                    <label for="capacity">Sức chứa *</label>
                    <input type="number" id="capacity" name="capacity" required 
                           min="1" max="20" value="4" placeholder="Nhập sức chứa">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-warning" onclick="closeAddTableModal()">
                    ❌ Hủy
                </button>
                <button type="button" class="btn btn-success" onclick="submitAddTable(event)">
                    ✅ Thêm bàn
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Delete Confirmation Modal -->
<div id="deleteConfirmModal" class="modal">
    <div class="modal-content delete-confirm-modal">
        <div class="modal-header">
            <h2>Xác nhận xóa phòng</h2>
            <span class="close" onclick="closeDeleteConfirmModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="delete-warning">
                <div class="warning-icon">!</div>
                <div class="warning-content">
                    <h3>Bạn có chắc chắn muốn xóa phòng này?</h3>
                    <p class="room-name-to-delete" id="roomNameToDelete"></p>
                    <div class="warning-details">
                        <p><strong>Lưu ý:</strong></p>
                        <ul>
                            <li>Tất cả bàn trong phòng này sẽ bị xóa</li>
                            <li>Dữ liệu lịch sử giao dịch sẽ được giữ lại</li>
                            <li>Hành động này không thể hoàn tác</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" onclick="closeDeleteConfirmModal()">
                Hủy
            </button>
            <button type="button" class="btn btn-danger" id="confirmDeleteBtn" onclick="confirmDeleteRoom()">
                Xóa phòng
            </button>
        </div>
    </div>
</div>

<!-- Edit Table Modal -->
<div id="editTableModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2>Sửa thông tin bàn</h2>
            <span class="close" onclick="closeEditTableModal()">&times;</span>
        </div>
        <form id="editTableForm" action="roomtable" method="post">
            <input type="hidden" name="action" value="editTable">
            <input type="hidden" id="editTableId" name="tableId">
            <div class="modal-body">
                <div class="form-group">
                    <label for="editTableNumber">Số bàn *</label>
                    <input type="text" id="editTableNumber" name="tableNumber" required 
                           placeholder="Nhập số bàn">
                </div>
                <div class="form-group">
                    <label for="editTableName">Tên bàn *</label>
                    <input type="text" id="editTableName" name="tableName" required 
                           placeholder="Nhập tên bàn">
                </div>
                <div class="form-group">
                    <label for="editRoomId">Phòng</label>
                    <select id="editRoomId" name="roomId">
                        <option value="">Chọn phòng (tùy chọn)</option>
                        <c:forEach var="room" items="${rooms}">
                            <option value="${room.roomId}">
                                ${room.name} (Tối đa ${room.tableCount} bàn, ${room.totalCapacity} người)
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label for="editCapacity">Sức chứa *</label>
                    <input type="number" id="editCapacity" name="capacity" required 
                           min="1" max="20" placeholder="Nhập sức chứa">
                </div>
                <div class="form-group">
                    <label for="editStatus">Trạng thái *</label>
                    <select id="editStatus" name="status" required>
                        <option value="Available">Trống</option>
                        <option value="Occupied">Đang sử dụng</option>
                        <option value="Reserved">Đã đặt</option>
                        <option value="Maintenance">Bảo trì</option>
                    </select>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-warning" onclick="closeEditTableModal()">
                    ❌ Hủy
                </button>
                <button type="button" class="btn btn-success" onclick="submitEditTable(event)">
                    ✅ Cập nhật
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Delete Table Confirmation Modal -->
<div id="deleteTableConfirmModal" class="modal">
    <div class="modal-content delete-confirm-modal">
        <div class="modal-header">
            <h2>Xác nhận xóa bàn</h2>
            <span class="close" onclick="closeDeleteTableConfirmModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="delete-warning">
                <div class="warning-icon">⚠️</div>
                <div class="warning-content">
                    <h3>Bạn có chắc chắn muốn xóa bàn này?</h3>
                    <p class="table-name-to-delete" id="tableNameToDelete"></p>
                    <div class="warning-details">
                        <p><strong>Lưu ý:</strong></p>
                        <ul>
                            <li>Dữ liệu lịch sử giao dịch sẽ được giữ lại</li>
                            <li>Hành động này không thể hoàn tác</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" onclick="closeDeleteTableConfirmModal()">
                Hủy
            </button>
            <button type="button" class="btn btn-danger" id="confirmDeleteTableBtn" onclick="confirmDeleteTable()">
                Xóa bàn
            </button>
        </div>
    </div>
</div>

<!-- Table History Modal -->
<div id="tableHistoryModal" class="modal">
    <div class="modal-content table-history-modal-content">
        <div class="modal-header">
            <h2><i class='bx bx-receipt'></i> Lịch sử hóa đơn</h2>
            <span class="close" onclick="closeTableHistoryModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="history-stats">
                <div class="stat-item">
                    <span class="stat-label">Tổng hóa đơn</span>
                    <span class="stat-value" id="historyTotalInvoices">0</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Tổng doanh thu</span>
                    <span class="stat-value" id="historyTotalRevenue">0đ</span>
                </div>
            </div>
            <div id="tableHistoryContent" class="invoice-history-list">
                <div class="loading-spinner"></div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" onclick="closeTableHistoryModal()">
                <i class='bx bx-x'></i> Đóng
            </button>
            <button type="button" class="btn btn-primary" onclick="refreshTableHistory()">
                <i class='bx bx-refresh'></i> Làm mới
            </button>
        </div>
    </div>
</div>

<!-- Import Excel Modal -->
<div id="importExcelModal" class="modal">
    <div class="modal-content" style="max-width: 600px; max-height: 90vh; overflow-y: auto;">
        <div class="modal-header">
            <h2>Nhập dữ liệu từ Excel</h2>
            <span class="close" onclick="closeImportModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div class="import-instructions">
                <h3>Hướng dẫn nhập dữ liệu</h3>
                <div class="instruction-content">
                    <div class="instruction-section">
                        <h4>Sheet "Rooms" (Phòng):</h4>
                        <ul>
                            <li><strong>Cột A:</strong> Tên phòng (bắt buộc)</li>
                            <li><strong>Cột B:</strong> Mô tả phòng (tùy chọn)</li>
                            <li><strong>Cột C:</strong> Số lượng bàn tối đa (bắt buộc)</li>
                            <li><strong>Cột D:</strong> Tổng sức chứa (bắt buộc)</li>
                        </ul>
                        <div class="template-download">
                            <button type="button" class="btn btn-outline-primary btn-sm" onclick="downloadTemplate('rooms')">
                                📥 Tải về mẫu phòng
                            </button>
                        </div>
                    </div>
                            <div class="instruction-section">
                                <h4>Sheet "Tables" (Bàn):</h4>
                                <ul>
                                    <li><strong>Cột A:</strong> Số bàn (bắt buộc)</li>
                                    <li><strong>Cột B:</strong> Tên bàn (bắt buộc)</li>
                                    <li><strong>Cột C:</strong> Tên phòng (tùy chọn)</li>
                                    <li><strong>Cột D:</strong> Sức chứa (bắt buộc)</li>
                                </ul>
                                <div class="template-download">
                                    <button type="button" class="btn btn-outline-primary btn-sm" onclick="downloadTemplate('tables')">
                                        📥 Tải về mẫu bàn
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
                        <span class="checkmark"></span>
                        Bỏ qua dữ liệu trùng lặp
                    </label>
                    <label class="checkbox-label">
                        <input type="checkbox" id="validateData" checked>
                        <span class="checkmark"></span>
                        Kiểm tra tính hợp lệ của dữ liệu
                    </label>
                    <label class="checkbox-label">
                        <input type="checkbox" id="createMissingRooms">
                        <span class="checkmark"></span>
                        Tự động tạo phòng nếu chưa tồn tại
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
