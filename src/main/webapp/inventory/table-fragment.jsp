<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!-- Rooms Table -->
<div class="room-table-container">
    <h3>Danh sách phòng</h3>
    <div class="table-responsive">
        <table class="table">
            <thead>
                <tr>
                    <th class="sortable" onclick="sortTable(0, 'text', 'rooms')">
                        Tên phòng
                        <span class="sort-icon"></span>
                    </th>
                    <th class="sortable" onclick="sortTable(1, 'text', 'rooms')">
                        Mô tả
                        <span class="sort-icon"></span>
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
                    <tr>
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
                                    ✏️ Sửa
                                </button>
                                <button class="btn btn-danger btn-sm" onclick="deleteRoom('${room.roomId}')">
                                    🗑️ Xóa
                                </button>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<!-- Tables Table -->
<div class="room-table-container">
    <h3>Danh sách bàn</h3>
    <div class="table-responsive">
        <table class="table">
            <thead>
                <tr>
                    <th class="sortable" onclick="sortTable(0, 'text', 'tables')">
                        Số bàn
                        <span class="sort-icon"></span>
                    </th>
                    <th class="sortable" onclick="sortTable(1, 'text', 'tables')">
                        Tên bàn
                        <span class="sort-icon"></span>
                    </th>
                    <th class="sortable" onclick="sortTable(2, 'text', 'tables')">
                        Phòng
                        <span class="sort-icon"></span>
                    </th>
                    <th class="sortable" onclick="sortTable(3, 'number', 'tables')">
                        Sức chứa
                        <span class="sort-icon"></span>
                    </th>
                    <th class="sortable" onclick="sortTable(4, 'text', 'tables')">
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
                    <tr>
                        <td>${table.tableNumber}</td>
                        <td>${table.tableName}</td>
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
                            <span class="status-badge status-${table.status != null ? table.status.toLowerCase() : 'available'}">
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
                                <button class="btn btn-info btn-sm" onclick="viewTableDetails('${table.tableId}')">
                                    👁️ Chi tiết
                                </button>
                                <button class="btn btn-warning btn-sm" onclick="editTable('${table.tableId}')">
                                    ✏️ Sửa
                                </button>
                                <button class="btn btn-danger btn-sm" onclick="deleteTable('${table.tableId}')">
                                    🗑️ Xóa
                                </button>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>
