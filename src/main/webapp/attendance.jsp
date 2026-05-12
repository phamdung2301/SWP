<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="includes/header.jsp">
  <jsp:param name="page" value="attendance" />
  <jsp:param name="pageTitle" value="Bảng chấm công" />
  <jsp:param name="pageDescription" value="Theo dõi trạng thái chấm công theo ca" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/schedule.css">

<style>
/* Prevent horizontal scroll */
body, html {
  overflow-x: hidden;
  max-width: 100vw;
}

.app {
  overflow-x: hidden;
  max-width: 100vw;
}
</style>

<div class="schedule-container">
  <div class="schedule-header">
    <h1>Bảng chấm công</h1>
    <div class="header-actions">
      <form method="get" action="${pageContext.request.contextPath}/attendance" class="employee-filter-form">
        <input type="hidden" name="weekStart" value="${currentWeekStart}" />
        <select name="employeeCode" class="employee-select">
          <option value="">Chọn nhân viên</option>
          <c:forEach var="e" items="${employees}">
            <option value="${e.employeeCode}" <c:if test='${selectedEmployeeCode == e.employeeCode}'>selected</c:if>>${e.employeeCode} - ${e.fullName}</option>
          </c:forEach>
        </select>
        <button type="submit" class="btn btn-primary btn-icon" title="Tìm kiếm">
          <i class='bx bx-search'></i>
        </button>
        <a href="${pageContext.request.contextPath}/attendance?weekStart=${currentWeekStart}" class="btn btn-light btn-icon" title="Hủy lọc">
          <i class='bx bx-filter-alt-off'></i>
        </a>
      </form>
      <div class="schedule-toolbar">
        <div class="week-chip" id="weekChip">
          <a class="chip-btn prev" href="${pageContext.request.contextPath}/attendance?weekStart=${prevWeekStart}${filterQuery}"><i class='bx bx-chevron-left'></i></a>
          <button type="button" class="chip-label">${controlLabel}</button>
          <a class="chip-btn next" href="${pageContext.request.contextPath}/attendance?weekStart=${nextWeekStart}${filterQuery}"><i class='bx bx-chevron-right'></i></a>
        </div>
        <a class="btn btn-light" href="${pageContext.request.contextPath}/attendance?weekStart=${currentWeekStart}${filterQuery}">Tuần này</a>
      </div>
    </div>
  </div>

  <div class="main-content">
    <div class="schedule-table">
      <table class="schedule-grid">
        <thead>
          <tr>
            <th class="time-col">Ca làm việc</th>
            <c:forEach var="d" items="${weekDays}">
              <th><span>${d.label}</span><small class="date">${d.dateStr}</small></th>
            </c:forEach>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="t" items="${templates}">
            <tr>
              <td class="time-col">
                <div class="slot-title">${t.name}</div>
                <div class="slot-time">${t.startTime.toString().substring(0,5)} - ${t.endTime.toString().substring(0,5)}</div>
              </td>
              <c:forEach var="d" items="${weekDays}">
                <td class="schedule-cell">
                  <c:forEach var="row" items="${d.rows}">
                    <c:if test="${row.templateName == t.name}">
                      <c:choose>
                        <c:when test="${empty row.items}">
                          <div class="empty-day">—</div>
                        </c:when>
                        <c:otherwise>
                          <c:forEach var="item" items="${row.items}">
                            <c:set var="attStatus" value="${empty item.attendanceStatus ? item.status : item.attendanceStatus}" />
                            <c:set var="hasCheckIn" value="${not empty item.checkInAt}" />
                            <c:set var="hasCheckOut" value="${not empty item.checkOutAt}" />
                            <c:set var="isLeave" value="${attStatus == 'LeavePaid' || attStatus == 'LeaveUnpaid'}" />
                            
                            <%-- Determine block background color based on attendance status --%>
                            <%-- 
                              Màu sắc chấm công:
                              - Cam (#fed7aa): Chưa chấm công (không có checkIn/Out)
                              - Xanh dương (#e0f2fe): Nghỉ làm (LeavePaid/LeaveUnpaid)
                              - Xanh lá (#dcfce7): Đã chấm công đầy đủ (có cả checkIn và checkOut)
                              - Đỏ (#fecaca): Chấm công thiếu (chỉ có 1 trong 2)
                              - Tím (#ddd6fe): Có vi phạm (muộn/về sớm) - sẽ được set bởi JavaScript
                            --%>
                            <c:set var="blockBg" value="#fed7aa" />
                            <c:set var="blockBorder" value="#fdba74" />
                            <c:choose>
                              <c:when test="${isLeave}">
                                <c:set var="blockBg" value="#e0f2fe" />
                                <c:set var="blockBorder" value="#bae6fd" />
                              </c:when>
                              <c:when test="${hasCheckIn && hasCheckOut}">
                                <c:set var="blockBg" value="#dcfce7" />
                                <c:set var="blockBorder" value="#86efac" />
                              </c:when>
                              <c:when test="${hasCheckIn || hasCheckOut}">
                                <c:set var="blockBg" value="#fecaca" />
                                <c:set var="blockBorder" value="#f87171" />
                              </c:when>
                            </c:choose>
                            
                            <div class="shift-block" title="${item.employee}"
                              style="background:${blockBg}; border:1px solid ${blockBorder}; padding:8px; width:100%; max-width:100%; box-sizing:border-box; overflow:hidden;"
                              data-employee="${item.employee}"
                              data-employee-code="${item.employeeCode}"
                              data-status="${item.status}"
                              data-att-status="${attStatus}"
                              data-date="${d.dateStr}"
                              data-template-name="${t.name}"
                              data-start-time="${t.startTime.toString().substring(0,5)}"
                              data-end-time="${t.endTime.toString().substring(0,5)}"
                              data-check-in-at="${item.checkInAt}"
                              data-check-out-at="${item.checkOutAt}"
                              data-source="${item.source}"
                              data-is-late="${item.isLate}"
                              data-is-overtime="${item.isOvertime}"
                              data-is-early-leave="${item.isEarlyLeave}">
                              
                              <!-- 1. Tên nhân viên -->
                              <div class="shift-emp" style="font-weight:600; font-size:13px; margin-bottom:4px; word-wrap:break-word; overflow-wrap:break-word; max-width:100%;">${item.employee}</div>
                              
                              <!-- 2. Trạng thái (di chuyển lên dưới tên) -->
                              <div class="shift-status" style="display:flex; flex-wrap:wrap; gap:4px; align-items:center; max-width:100%; overflow:hidden; box-sizing:border-box; margin-bottom:4px;">
                                <c:choose>
                                  <c:when test="${attStatus == 'Work' || attStatus == 'Approved'}">
                                    <span class="badge" data-status="work" style="background:#ffffff;color:#166534;border:1px solid #86efac;font-size:10px;padding:2px 6px;border-radius:4px;font-weight:500;box-shadow:0 1px 2px rgba(0,0,0,0.1);">Đi làm</span>
                                  </c:when>
                                  <c:when test="${attStatus == 'LeavePaid'}">
                                    <span class="badge" data-status="leave-paid" style="background:#ffffff;color:#075985;border:1px solid #bae6fd;font-size:10px;padding:2px 6px;border-radius:4px;font-weight:500;box-shadow:0 1px 2px rgba(0,0,0,0.1);">Nghỉ có phép</span>
                                  </c:when>
                                  <c:when test="${attStatus == 'LeaveUnpaid'}">
                                    <span class="badge" data-status="leave-unpaid" style="background:#ffffff;color:#991b1b;border:1px solid #fecaca;font-size:10px;padding:2px 6px;border-radius:4px;font-weight:500;box-shadow:0 1px 2px rgba(0,0,0,0.1);">Nghỉ không phép</span>
                                  </c:when>
                                  <c:otherwise>
                                    <span class="badge" data-status="unknown" style="background:#ffffff;color:#854d0e;border:1px solid #fde68a;font-size:10px;padding:2px 6px;border-radius:4px;font-weight:500;box-shadow:0 1px 2px rgba(0,0,0,0.1);">Chưa xác định</span>
                                  </c:otherwise>
                                </c:choose>
                              </div>
                              
                              <!-- 3. Thời gian check-in/out -->
                              <c:if test="${not empty item.checkInAt}">
                                <div class="shift-time" style="color:#374151; font-size:11px; font-weight:500; margin-bottom:4px; word-wrap:break-word; overflow-wrap:break-word; max-width:100%;">
                                  <i class='bx bx-time' style="font-size:12px;"></i> ${item.checkInAt} - ${item.checkOutAt}
                                </div>
                              </c:if>
                              
                              <!-- 4. Ghi chú -->
                              <c:if test="${not empty item.notes}">
                                <div class="shift-notes" style="color:#6b7280; font-size:10px; font-style:italic; margin-bottom:4px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; max-width:100%;">
                                  <i class='bx bx-note' style="font-size:11px;"></i> ${item.notes}
                                </div>
                              </c:if>
                              
                              <!-- Status flags with calculated time diff (will be filled by JS) -->
                              <div class="shift-flags" style="display:flex; flex-wrap:wrap; gap:3px; margin-top:4px; font-size:9px; max-width:100%; overflow:hidden; box-sizing:border-box;"></div>
                            </div>
                          </c:forEach>
                        </c:otherwise>
                      </c:choose>
                    </c:if>
                  </c:forEach>
                </td>
              </c:forEach>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>

    <!-- Status Legend -->
    <div style="margin-top:24px; padding:16px; background:#f9fafb; border:1px solid #e5e7eb; border-radius:12px;display:flex;justify-content: center;">
      <div style="display:flex; flex-wrap:wrap; align-items:center; gap:16px;">
        <div style="display:flex; align-items:center; gap:6px;">
          <span style="display:inline-block; width:16px; height:16px; border-radius:4px; background:#dcfce7; border:2px solid #86efac;"></span>
          <span style="color:#166534; font-size:13px; font-weight:500;">Chấm công đầy đủ</span>
        </div>
        <div style="display:flex; align-items:center; gap:6px;">
          <span style="display:inline-block; width:16px; height:16px; border-radius:4px; background:#ddd6fe; border:2px solid #c4b5fd;"></span>
          <span style="color:#5b21b6; font-size:13px; font-weight:500;">Vi phạm (muộn/sớm)</span>
        </div>
        <div style="display:flex; align-items:center; gap:6px;">
          <span style="display:inline-block; width:16px; height:16px; border-radius:4px; background:#fecaca; border:2px solid #f87171;"></span>
          <span style="color:#991b1b; font-size:13px; font-weight:500;">Chấm công thiếu</span>
        </div>
        <div style="display:flex; align-items:center; gap:6px;">
          <span style="display:inline-block; width:16px; height:16px; border-radius:4px; background:#fed7aa; border:2px solid #fdba74;"></span>
          <span style="color:#9a3412; font-size:13px; font-weight:500;">Chưa chấm công</span>
        </div>
        <div style="display:flex; align-items:center; gap:6px;">
          <span style="display:inline-block; width:16px; height:16px; border-radius:4px; background:#e0f2fe; border:2px solid #bae6fd;"></span>
          <span style="color:#075985; font-size:13px; font-weight:500;">Nghỉ làm</span>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="includes/footer.jsp" />


<!-- Shift Detail Modal for Attendance -->
<div id="attShiftDetailOverlay" style="position:fixed; inset:0; background:rgba(0,0,0,.45); display:none; align-items:center; justify-content:center; z-index:1000;">
  <div style="background:#fff; width:95%; max-width:720px; border-radius:10px; box-shadow:0 10px 30px rgba(0,0,0,0.2); overflow:hidden;">
    <div style="display:flex; align-items:center; justify-content:space-between; padding:16px 20px; border-bottom:1px solid #eee;">
      <h3 style="margin:0; font-size:18px; font-weight:600;">Chi tiết ca làm</h3>
      <button type="button" id="attCloseShiftDetail" class="btn btn-light">Đóng</button>
    </div>
    <div style="padding:16px 20px;">
      <!-- Tab headers -->
      <div id="attTabs" style="display:flex; gap:8px; border-bottom:1px solid #e5e7eb;">
        <button class="att-tab att-tab-active" data-tab="overview" style="padding:8px 12px; border:none; background:transparent; border-bottom:2px solid #3b82f6; color:#1f2937; cursor:pointer;">Tổng quan</button>
        <button class="att-tab" data-tab="history" style="padding:8px 12px; border:none; background:transparent; color:#6b7280; cursor:pointer;">Lịch sử chấm công</button>
        <button class="att-tab" data-tab="penalty" style="padding:8px 12px; border:none; background:transparent; color:#6b7280; cursor:pointer;">Phạt vi phạm</button>
        <button class="att-tab" data-tab="bonus" style="padding:8px 12px; border:none; background:transparent; color:#6b7280; cursor:pointer;">Thưởng</button>
      </div>

      <!-- Tab panels -->
      <div id="attTabPanels" style="padding-top:12px;">
        <!-- Overview -->
        <div class="att-panel" data-panel="overview" style="display:block;">
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px 16px;">
            <div><label style="font-size:12px; color:#6b7280;">Nhân viên</label><div id="attSdEmployee" style="margin-top:6px; font-weight:600;"></div></div>
            <div>
              <label style="font-size:12px; color:#6b7280;">Trạng thái</label>
              <select id="attStatusSelect" name="status" form="attSaveForm" style="margin-top:6px; width:100%; padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px;">
                <option value="">Chọn trạng thái</option>
                <option value="work">Đi làm</option>
                <option value="leave_paid">Nghỉ có phép</option>
                <option value="leave_unpaid">Nghỉ không phép</option>
              </select>
            </div>
            <div><label style="font-size:12px; color:#6b7280;">Ngày</label><div id="attSdDate" style="margin-top:6px;"></div></div>
            <div><label style="font-size:12px; color:#6b7280;">Ca</label><div id="attSdTemplate" style="margin-top:6px;"></div></div>
            <div><label style="font-size:12px; color:#6b7280;">Giờ ca</label><div id="attSdShiftTime" style="margin-top:6px;"></div></div>
            <div style="grid-column:1 / -1;">
              <label style="font-size:12px; color:#6b7280;">Ghi chú</label>
              <input id="attNotes" name="notes" form="attSaveForm" type="text" placeholder="Ghi chú chấm công" style="margin-top:6px; width:100%; padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px;" />
            </div>
            <div id="attWorkTimeRow" style="grid-column:1 / -1; display:none;">
              <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px 16px;">
                <!-- Left column: times -->
                <div>
                  <label style="font-size:12px; color:#6b7280;">Giờ vào / ra</label>
                  <div style="display:flex; flex-direction:column; gap:10px; margin-top:6px;">
                    <div style="display:flex; align-items:center; gap:8px;">
                      <span style="width:40px; color:#6b7280;">Vào</span>
                      <input id="attCheckInInput" name="checkIn" form="attSaveForm" type="time" style="width:140px; padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px;" />
                    </div>
                    <div style="display:flex; align-items:center; gap:8px;">
                      <span style="width:40px; color:#6b7280;">Ra</span>
                      <input id="attCheckOutInput" name="checkOut" form="attSaveForm" type="time" style="width:140px; padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px;" />
                    </div>
                  </div>
                </div>
                <!-- Right column: symmetric checkboxes -->
                <div>
                  <label style="font-size:12px; color:#6b7280;">Trạng thái</label>
                  <div style="display:flex; flex-direction:column; gap:12px; margin-top:6px;">
                    <div id="checkInStatusRow" style="display:flex; align-items:center; gap:12px; min-height:34px;">
                      <span style="width:40px; color:#6b7280;">Vào</span>
                      <label id="inLateLabel" style="display:none; align-items:center; gap:6px; padding:4px 10px; background:#fef2f2; border:1px solid #fecaca; border-radius:6px; cursor:pointer;">
                        <input id="inLateToggle" type="checkbox" style="cursor:pointer;" />
                        <span id="inLateText" style="color:#dc2626; font-size:13px;">Đi muộn</span>
                      </label>
                      <label id="inOverLabel" style="display:none; align-items:center; gap:6px; padding:4px 10px; background:#f0fdf4; border:1px solid #bbf7d0; border-radius:6px; cursor:pointer;">
                        <input id="inOverToggle" type="checkbox" style="cursor:pointer;" />
                        <span id="inOverText" style="color:#16a34a; font-size:13px;">Làm thêm</span>
                      </label>
                    </div>
                    <div id="checkOutStatusRow" style="display:flex; align-items:center; gap:12px; min-height:34px;">
                      <span style="width:40px; color:#6b7280;">Ra</span>
                      <label id="outEarlyLabel" style="display:none; align-items:center; gap:6px; padding:4px 10px; background:#fef2f2; border:1px solid #fecaca; border-radius:6px; cursor:pointer;">
                        <input id="outEarlyToggle" type="checkbox" style="cursor:pointer;" />
                        <span id="outEarlyText" style="color:#dc2626; font-size:13px;">Về sớm</span>
                      </label>
                      <label id="outOverLabel" style="display:none; align-items:center; gap:6px; padding:4px 10px; background:#f0fdf4; border:1px solid #bbf7d0; border-radius:6px; cursor:pointer;">
                        <input id="outOverToggle" type="checkbox" style="cursor:pointer;" />
                        <span id="outOverText" style="color:#16a34a; font-size:13px;">Làm thêm</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div style="grid-column:1 / -1; margin-top:12px; display:flex; gap:8px; justify-content:flex-end;">
              <form id="attSaveForm" method="post" action="${pageContext.request.contextPath}/attendance" style="margin:0;">
                <input type="hidden" name="action" value="save" />
                <input type="hidden" name="weekStart" value="${currentWeekStart}" />
                <input type="hidden" name="employeeCode" id="attFormEmployeeCode" />
                <input type="hidden" name="date" id="attFormDate" />
                <input type="hidden" name="redirectEmployeeCode" value="${selectedEmployeeCode}" />
                <input type="hidden" name="inLate" id="attFormInLate" value="false" />
                <input type="hidden" name="inOver" id="attFormInOver" value="false" />
                <input type="hidden" name="outEarly" id="attFormOutEarly" value="false" />
                <input type="hidden" name="outOver" id="attFormOutOver" value="false" />
                <button type="submit" class="btn btn-primary">Lưu trạng thái</button>
              </form>
            </div>
          </div>
        </div>

        <!-- Attendance history -->
        <div class="att-panel" data-panel="history" style="display:none;">
          <div id="attHistoryEmpty" style="font-size:13px; color:#6b7280;">Chưa có dữ liệu lịch sử.</div>
          <div id="attHistoryList" style="display:none; max-height:260px; overflow:auto; border:1px solid #e5e7eb; border-radius:8px;">
            <table style="width:100%; border-collapse:collapse; font-size:13px;">
              <thead>
                <tr style="background:#f9fafb; text-align:left;">
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Ngày</th>
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Check-in</th>
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Check-out</th>
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Ghi chú</th>
                </tr>
              </thead>
              <tbody id="attHistoryTbody"></tbody>
            </table>
          </div>
        </div>

        <!-- Penalties -->
        <div class="att-panel" data-panel="penalty" style="display:none;">
          <div id="attPenaltyEmpty" style="font-size:13px; color:#6b7280;">Không có vi phạm nào.</div>
          <div id="attPenaltyList" style="display:none; max-height:260px; overflow:auto; border:1px solid #e5e7eb; border-radius:8px;">
            <table style="width:100%; border-collapse:collapse; font-size:13px;">
              <thead>
                <tr style="background:#f9fafb; text-align:left;">
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Ngày</th>
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Lý do</th>
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Số tiền</th>
                </tr>
              </thead>
              <tbody id="attPenaltyTbody"></tbody>
            </table>
          </div>
        </div>

        <!-- Bonuses -->
        <div class="att-panel" data-panel="bonus" style="display:none;">
          <div id="attBonusEmpty" style="font-size:13px; color:#6b7280;">Chưa có thưởng nào.</div>
          <div id="attBonusList" style="display:none; max-height:260px; overflow:auto; border:1px solid #e5e7eb; border-radius:8px;">
            <table style="width:100%; border-collapse:collapse; font-size:13px;">
              <thead>
                <tr style="background:#f9fafb; text-align:left;">
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Ngày</th>
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Lý do</th>
                  <th style="padding:8px 10px; border-bottom:1px solid #e5e7eb;">Số tiền</th>
                </tr>
              </thead>
              <tbody id="attBonusTbody"></tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
  
</div>

<script>
(function(){
  function setText(id, v){ var el = document.getElementById(id); if (el) el.textContent = v || ''; }
  function open(){ var ov = document.getElementById('attShiftDetailOverlay'); if (ov) ov.style.display = 'flex'; }
  function close(){ var ov = document.getElementById('attShiftDetailOverlay'); if (ov) ov.style.display = 'none'; }
  var closeBtn = document.getElementById('attCloseShiftDetail');
  if (closeBtn) closeBtn.addEventListener('click', close);
  var overlay = document.getElementById('attShiftDetailOverlay');
  if (overlay) overlay.addEventListener('click', function(e){ if (e.target === overlay) close(); });
  document.addEventListener('keydown', function(e){ if (e.key === 'Escape') close(); });

  // Tabs wiring
  function activateTab(name){
    var tabs = Array.prototype.slice.call(document.querySelectorAll('#attTabs .att-tab'));
    var panels = Array.prototype.slice.call(document.querySelectorAll('#attTabPanels .att-panel'));
    tabs.forEach(function(t){
      var isActive = t.getAttribute('data-tab') === name;
      t.className = 'att-tab' + (isActive ? ' att-tab-active' : '');
      // inline style updates for bottom border and colors
      if (isActive) {
        t.style.borderBottom = '2px solid #3b82f6';
        t.style.color = '#1f2937';
      } else {
        t.style.borderBottom = '2px solid transparent';
        t.style.color = '#6b7280';
      }
    });
    panels.forEach(function(p){ p.style.display = (p.getAttribute('data-panel') === name) ? 'block' : 'none'; });
  }
  document.addEventListener('click', function(e){
    var tabBtn = e.target.closest('#attTabs .att-tab');
    if (tabBtn) {
      var name = tabBtn.getAttribute('data-tab');
      if (name) activateTab(name);
    }
  });

  // Global variables to store current shift info
  var currentShiftStartTime = '';
  var currentShiftEndTime = '';

  document.addEventListener('click', function(e){
    var block = e.target.closest('.shift-block');
    if (!block) return;
    // Extract attributes populated on the block
    var emp = block.getAttribute('data-employee') || '';
    var status = block.getAttribute('data-status') || '';
    var att = block.getAttribute('data-att-status') || '';
    var date = block.getAttribute('data-date') || '';
    var tmpl = block.getAttribute('data-template-name') || '';
    var s = block.getAttribute('data-start-time') || '';
    var en = block.getAttribute('data-end-time') || '';
    var ci = block.getAttribute('data-check-in-at') || '';
    var co = block.getAttribute('data-check-out-at') || '';
    var src = block.getAttribute('data-source') || '';
    var empCode = block.getAttribute('data-employee-code') || block.getAttribute('data-emp-code') || '';
    
    // Lưu giờ ca mặc định vào biến toàn cục
    currentShiftStartTime = s;
    currentShiftEndTime = en;

    // Populate overview
    setText('attSdEmployee', emp);
    setText('attSdDate', date);
    setText('attSdTemplate', tmpl);
    setText('attSdShiftTime', (s && en) ? (s + ' - ' + en) : '');
    setText('attSdCheckTime', (ci && co) ? (ci + ' - ' + co) : (ci ? (ci + ' - —') : ''));

    // Initialize status dropdown
    var sel = document.getElementById('attStatusSelect');
    var workRow = document.getElementById('attWorkTimeRow');
    var inInput = document.getElementById('attCheckInInput');
    var outInput = document.getElementById('attCheckOutInput');
    if (sel) {
      if (att) {
        // Prefer new DB-driven attendance status
        if (att === 'Work') sel.value = 'work';
        else if (att === 'LeavePaid') sel.value = 'leave_paid';
        else if (att === 'LeaveUnpaid') sel.value = 'leave_unpaid';
        else sel.value = '';
      } else {
        // Fallback to legacy mapping
        var normalized = (status || '').toLowerCase();
        if (normalized.indexOf('approved') !== -1 || normalized.indexOf('đã duyệt') !== -1 || normalized.indexOf('chờ') !== -1 || ci) {
          sel.value = 'work';
        } else if (normalized.indexOf('paid') !== -1 || normalized.indexOf('có phép') !== -1) {
          sel.value = 'leave_paid';
        } else if (normalized.indexOf('unpaid') !== -1 || normalized.indexOf('không phép') !== -1) {
          sel.value = 'leave_unpaid';
        } else {
          sel.value = '';
        }
      }
    }
    // Điền giờ vào/ra: nếu đã có check-in/out thì dùng, không thì dùng giờ ca mặc định
    if (inInput) {
      if (ci && ci.length >= 5) {
        inInput.value = ci.substring(0,5);
      } else if (s && s.length >= 5) {
        inInput.value = s.substring(0,5); // Mặc định là giờ bắt đầu ca
      } else {
        inInput.value = '';
      }
    }
    if (outInput) {
      if (co && co.length >= 5) {
        outInput.value = co.substring(0,5);
      } else if (en && en.length >= 5) {
        outInput.value = en.substring(0,5); // Mặc định là giờ kết thúc ca
      } else {
        outInput.value = '';
      }
    }
    if (workRow) workRow.style.display = (sel && sel.value === 'work') ? 'block' : 'none';

    // Auto-calc toggles for in/out based on template vs actual
    function toMinutes(hhmm){
      if (!hhmm) return null;
      if (/^\d{2}:\d{2}$/.test(hhmm)) { var p = hhmm.split(':'); return parseInt(p[0],10)*60+parseInt(p[1],10); }
      var d = new Date(hhmm); if (!isNaN(d.getTime())) return d.getHours()*60 + d.getMinutes();
      if (hhmm.length>=5 && hhmm.indexOf(':')===2){ var p2 = hhmm.substring(0,5).split(':'); return parseInt(p2[0],10)*60+parseInt(p2[1],10); }
      return null;
    }
    function formatDiff(minutes){
      var h = Math.floor(minutes/60);
      var m = minutes%60;
      if (h>0 && m>0) return h+' giờ '+m+' phút';
      if (h>0) return h+' giờ';
      return m+' phút';
    }
    function showStatusLabel(labelId, toggleId, textId, diff){
      var label = document.getElementById(labelId);
      var toggle = document.getElementById(toggleId);
      var text = document.getElementById(textId);
      if (label && toggle && text) {
        if (diff && diff>0) {
          label.style.display = 'flex';
          label.style.opacity = '1';
          label.style.textDecoration = 'none';
          toggle.checked = true;
          var baseText = text.textContent.split(' (')[0]; // Remove old time diff
          text.textContent = baseText + ' (' + formatDiff(diff) + ')';
          
          // Sync with hidden form input
          var formInputId = '';
          if (toggleId === 'inLateToggle') formInputId = 'attFormInLate';
          else if (toggleId === 'inOverToggle') formInputId = 'attFormInOver';
          else if (toggleId === 'outEarlyToggle') formInputId = 'attFormOutEarly';
          else if (toggleId === 'outOverToggle') formInputId = 'attFormOutOver';
          var formInput = document.getElementById(formInputId);
          if (formInput) formInput.value = 'true';
        } else {
          label.style.display = 'none';
          label.style.opacity = '1';
          label.style.textDecoration = 'none';
          toggle.checked = false;
          
          // Sync with hidden form input
          var formInputId2 = '';
          if (toggleId === 'inLateToggle') formInputId2 = 'attFormInLate';
          else if (toggleId === 'inOverToggle') formInputId2 = 'attFormInOver';
          else if (toggleId === 'outEarlyToggle') formInputId2 = 'attFormOutEarly';
          else if (toggleId === 'outOverToggle') formInputId2 = 'attFormOutOver';
          var formInput2 = document.getElementById(formInputId2);
          if (formInput2) formInput2.value = 'false';
        }
      }
    }

    var startM = toMinutes(s);
    var endM = toMinutes(en);
    var inM = toMinutes(ci);
    var outM = toMinutes(co);

    // In-side checkboxes: late vs overtime (arrive early counts as overtime)
    if (inM!=null && startM!=null){
      if (inM>startM){ 
        showStatusLabel('inLateLabel','inLateToggle','inLateText', inM-startM); 
        showStatusLabel('inOverLabel','inOverToggle','inOverText', 0); 
      } else if (inM<startM){ 
        showStatusLabel('inOverLabel','inOverToggle','inOverText', startM-inM); 
        showStatusLabel('inLateLabel','inLateToggle','inLateText', 0); 
      } else { 
        showStatusLabel('inLateLabel','inLateToggle','inLateText', 0); 
        showStatusLabel('inOverLabel','inOverToggle','inOverText', 0); 
      }
    } else {
      showStatusLabel('inLateLabel','inLateToggle','inLateText', 0); 
      showStatusLabel('inOverLabel','inOverToggle','inOverText', 0); 
    }

    // Out-side checkboxes: leave early vs overtime (checkout late counts as overtime)
    if (outM!=null && endM!=null){
      if (outM<endM){ 
        showStatusLabel('outEarlyLabel','outEarlyToggle','outEarlyText', endM-outM); 
        showStatusLabel('outOverLabel','outOverToggle','outOverText', 0); 
      } else if (outM>endM){ 
        showStatusLabel('outOverLabel','outOverToggle','outOverText', outM-endM); 
        showStatusLabel('outEarlyLabel','outEarlyToggle','outEarlyText', 0); 
      } else { 
        showStatusLabel('outEarlyLabel','outEarlyToggle','outEarlyText', 0); 
        showStatusLabel('outOverLabel','outOverToggle','outOverText', 0); 
      }
    } else {
      showStatusLabel('outEarlyLabel','outEarlyToggle','outEarlyText', 0); 
      showStatusLabel('outOverLabel','outOverToggle','outOverText', 0); 
    }

    // Fill form hidden fields
    var formEmp = document.getElementById('attFormEmployeeCode');
    var formDate = document.getElementById('attFormDate');
    if (formEmp) formEmp.value = (block.getAttribute('data-employee-code') || '');
    // Convert dd/MM to yyyy-MM-dd using current/selected week year from URL if present
    if (formDate) {
      var parts = (date || '').split('/');
      if (parts.length === 2) {
        var urlParams = new URLSearchParams(window.location.search);
        var weekStart = urlParams.get('weekStart');
        var year = new Date().getFullYear();
        if (weekStart) { var wsDate = new Date(weekStart); if (!isNaN(wsDate.getTime())) { year = wsDate.getFullYear(); } }
        formDate.value = year + '-' + parts[1] + '-' + parts[0];
      } else {
        formDate.value = '';
      }
    }

    // Reset secondary tabs to empty state (placeholder demo)
    var histEmpty = document.getElementById('attHistoryEmpty');
    var histList = document.getElementById('attHistoryList');
    var histBody = document.getElementById('attHistoryTbody');
    if (histEmpty && histList && histBody) { histEmpty.style.display = 'block'; histList.style.display = 'none'; histBody.innerHTML = ''; }

    var penEmpty = document.getElementById('attPenaltyEmpty');
    var penList = document.getElementById('attPenaltyList');
    var penBody = document.getElementById('attPenaltyTbody');
    if (penEmpty && penList && penBody) { penEmpty.style.display = 'block'; penList.style.display = 'none'; penBody.innerHTML = ''; }

    var bonEmpty = document.getElementById('attBonusEmpty');
    var bonList = document.getElementById('attBonusList');
    var bonBody = document.getElementById('attBonusTbody');
    if (bonEmpty && bonList && bonBody) { bonEmpty.style.display = 'block'; bonList.style.display = 'none'; bonBody.innerHTML = ''; }

    // Default to Overview tab each time open
    activateTab('overview');

    // Build history table rows with status evaluation (đúng giờ / muộn / về sớm / tăng ca)
    var histEmpty = document.getElementById('attHistoryEmpty');
    var histList = document.getElementById('attHistoryList');
    var histBody = document.getElementById('attHistoryTbody');
    if (histEmpty && histList && histBody) {
      histBody.innerHTML = '';
      var haveAny = false;
      function addRow(timeStr, statusLabel, methodLabel, content) {
        var tr = document.createElement('tr');
        var td1 = document.createElement('td'); td1.style.padding = '8px 10px'; td1.textContent = timeStr || '';
        var td2 = document.createElement('td'); td2.style.padding = '8px 10px'; td2.textContent = statusLabel || '';
        var td3 = document.createElement('td'); td3.style.padding = '8px 10px'; td3.textContent = methodLabel || '';
        var td4 = document.createElement('td'); td4.style.padding = '8px 10px'; td4.textContent = content || '';
        tr.appendChild(td1); tr.appendChild(td2); tr.appendChild(td3); tr.appendChild(td4);
        histBody.appendChild(tr);
        haveAny = true;
      }

      var method = '—';
      if (src) {
        if (src === 'Auto') method = 'Chấm công tự động';
        else if (src === 'Manual') method = 'Chấm công thủ công';
        else if (src === 'Import') method = 'Chấm công bằng máy chấm công';
        else method = src;
      }
      var contentBase = 'Giờ ca: ' + (s && en ? (s + ' - ' + en) : '—');

      function toMinutes(hhmm){
        if (!hhmm) return null;
        // Try HH:mm first
        if (/^\d{2}:\d{2}$/.test(hhmm)) {
          var p = hhmm.split(':');
          return parseInt(p[0],10)*60 + parseInt(p[1],10);
        }
        // Try Date string
        var d = new Date(hhmm);
        if (!isNaN(d.getTime())) return d.getHours()*60 + d.getMinutes();
        // Try substring
        if (hhmm.length >= 5 && hhmm.indexOf(':') === 2) {
          var p2 = hhmm.substring(0,5).split(':');
          return parseInt(p2[0],10)*60 + parseInt(p2[1],10);
        }
        return null;
      }
      function fmtDuration(mins){
        var m = Math.abs(mins|0); var h = Math.floor(m/60); var r = m%60;
        return h + ' giờ ' + r + ' phút';
      }

      var startM = toMinutes(s);
      var endM = toMinutes(en);
      var inM = toMinutes(ci);
      var outM = toMinutes(co);

      // Check-in row with status
      if (inM != null && startM != null) {
        var statusIn;
        if (inM === startM) statusIn = 'Đúng giờ';
        else if (inM > startM) statusIn = 'Muộn ' + fmtDuration(inM - startM);
        else statusIn = 'Tăng ca ' + fmtDuration(startM - inM); // đến sớm
        addRow((ci && ci.substring ? ci.substring(0,5) : ci), statusIn, method, contentBase);
      } else if (ci) {
        addRow(ci, 'Đã chấm công vào', method, contentBase);
      }

      // Check-out row with status
      if (outM != null && endM != null) {
        var statusOut;
        if (outM === endM) statusOut = 'Đúng giờ';
        else if (outM < endM) statusOut = 'Về sớm ' + fmtDuration(endM - outM);
        else statusOut = 'Tăng ca ' + fmtDuration(outM - endM);
        addRow((co && co.substring ? co.substring(0,5) : co), statusOut, method, contentBase);
      } else if (co) {
        addRow(co, 'Đã chấm công ra', method, contentBase);
      }

      histEmpty.style.display = haveAny ? 'none' : 'block';
      histList.style.display = haveAny ? 'block' : 'none';
    }

    open();
  });

  // Handle checkbox toggle for status labels
  document.addEventListener('change', function(e){
    if (!e.target) return;
    var id = e.target.id;
    if (id === 'inLateToggle' || id === 'inOverToggle' || id === 'outEarlyToggle' || id === 'outOverToggle') {
      var labelId = id.replace('Toggle', 'Label');
      var label = document.getElementById(labelId);
      if (label) {
        if (e.target.checked) {
          // Keep original colors
          label.style.opacity = '1';
          label.style.textDecoration = 'none';
        } else {
          // Dim when unchecked
          label.style.opacity = '0.5';
          label.style.textDecoration = 'line-through';
        }
      }
      
      // Sync with hidden form inputs
      var formInputId = '';
      if (id === 'inLateToggle') formInputId = 'attFormInLate';
      else if (id === 'inOverToggle') formInputId = 'attFormInOver';
      else if (id === 'outEarlyToggle') formInputId = 'attFormOutEarly';
      else if (id === 'outOverToggle') formInputId = 'attFormOutOver';
      
      var formInput = document.getElementById(formInputId);
      if (formInput) {
        formInput.value = e.target.checked ? 'true' : 'false';
      }
    }
  });

  // Color shift blocks based on late/early status (on page load)
  document.addEventListener('DOMContentLoaded', function(){
    function toMinutes(hhmm){
      if (!hhmm) return null;
      if (/^\d{2}:\d{2}$/.test(hhmm)) { var p = hhmm.split(':'); return parseInt(p[0],10)*60+parseInt(p[1],10); }
      var d = new Date(hhmm); if (!isNaN(d.getTime())) return d.getHours()*60 + d.getMinutes();
      if (hhmm.length>=5 && hhmm.indexOf(':')===2){ var p2 = hhmm.substring(0,5).split(':'); return parseInt(p2[0],10)*60+parseInt(p2[1],10); }
      return null;
    }
    
    function formatTimeDiff(minutes){
      var h = Math.floor(minutes/60);
      var m = minutes%60;
      if (h>0 && m>0) return h+'g'+m+'p';
      if (h>0) return h+'g';
      return m+'p';
    }
    
    var blocks = document.querySelectorAll('.shift-block');
    blocks.forEach(function(block){
      var s = block.getAttribute('data-start-time') || '';
      var en = block.getAttribute('data-end-time') || '';
      var ci = block.getAttribute('data-check-in-at') || '';
      var co = block.getAttribute('data-check-out-at') || '';
      var attStatus = block.getAttribute('data-att-status') || '';
      var isLate = block.getAttribute('data-is-late') === 'true';
      var isOvertime = block.getAttribute('data-is-overtime') === 'true';
      var isEarlyLeave = block.getAttribute('data-is-early-leave') === 'true';
      
      // Skip if leave status
      if (attStatus === 'LeavePaid' || attStatus === 'LeaveUnpaid') return;
      
      // Only process if both check-in and check-out exist
      if (!ci || !co) return;
      
      var startM = toMinutes(s);
      var endM = toMinutes(en);
      var inM = toMinutes(ci);
      var outM = toMinutes(co);
      
      var hasViolation = false; // Vi phạm (đi muộn hoặc về sớm)
      var hasOnlyOvertime = false; // Chỉ có tăng ca, không có vi phạm
      var flagsContainer = block.querySelector('.shift-flags');
      if (!flagsContainer) return;
      
      // Check-in late (vi phạm)
      if (inM !== null && startM !== null && inM > startM) {
        hasViolation = true;
        var diff = inM - startM;
        var badge = document.createElement('span');
        badge.className = 'badge';
        badge.style.cssText = 'background:#fef2f2;color:#dc2626;border:1px solid #fca5a5;padding:2px 5px;font-size:9px;';
        badge.innerHTML = '<i class="bx bx-time-five" style="font-size:10px;"></i> Muộn ' + formatTimeDiff(diff);
        flagsContainer.appendChild(badge);
      }
      
      // Check-out early (vi phạm)
      if (outM !== null && endM !== null && outM < endM) {
        hasViolation = true;
        var diff3 = endM - outM;
        var badge3 = document.createElement('span');
        badge3.className = 'badge';
        badge3.style.cssText = 'background:#fef2f2;color:#dc2626;border:1px solid #fca5a5;padding:2px 5px;font-size:9px;';
        badge3.innerHTML = '<i class="bx bx-log-out" style="font-size:10px;"></i> Về sớm ' + formatTimeDiff(diff3);
        flagsContainer.appendChild(badge3);
      }
      
      // Check-in early (overtime - chỉ hiển thị nếu không có vi phạm)
      if (inM !== null && startM !== null && inM < startM) {
        if (!hasViolation) hasOnlyOvertime = true;
        var diff2 = startM - inM;
        var badge2 = document.createElement('span');
        badge2.className = 'badge';
        badge2.style.cssText = 'background:#f0fdf4;color:#16a34a;border:1px solid #86efac;padding:2px 5px;font-size:9px;';
        badge2.innerHTML = '<i class="bx bx-time" style="font-size:10px;"></i> Vào sớm ' + formatTimeDiff(diff2);
        flagsContainer.appendChild(badge2);
      }
      
      // Check-out late (overtime - chỉ hiển thị nếu không có vi phạm)
      if (outM !== null && endM !== null && outM > endM) {
        if (!hasViolation) hasOnlyOvertime = true;
        var diff4 = outM - endM;
        var badge4 = document.createElement('span');
        badge4.className = 'badge';
        badge4.style.cssText = 'background:#f0fdf4;color:#16a34a;border:1px solid #86efac;padding:2px 5px;font-size:9px;';
        badge4.innerHTML = '<i class="bx bx-time" style="font-size:10px;"></i> Tăng ca ' + formatTimeDiff(diff4);
        flagsContainer.appendChild(badge4);
      }
      
      // Xét màu theo ưu tiên:
      // 1. Nếu có vi phạm (muộn/sớm) -> màu tím (violet)
      // 2. Nếu chỉ có tăng ca (không có vi phạm) -> màu xanh lá (green)
      // 3. Mặc định (đúng giờ, không vi phạm, không tăng ca) -> màu xanh lá (green)
      if (hasViolation) {
        block.style.background = '#ddd6fe'; // Violet
        block.style.borderColor = '#c4b5fd';
      } else if (hasOnlyOvertime) {
        block.style.background = '#dcfce7'; // Green (overtime nhưng không vi phạm)
        block.style.borderColor = '#86efac';
      }
      // Nếu không có gì đặc biệt thì giữ màu xanh mặc định
    });
  });

  // React to status change to show/hide time fields
  document.addEventListener('change', function(e){
    if (e.target && e.target.id === 'attStatusSelect') {
      var workRow = document.getElementById('attWorkTimeRow');
      if (workRow) workRow.style.display = (e.target.value === 'work') ? 'block' : 'none';
      
      var inInput = document.getElementById('attCheckInInput');
      var outInput = document.getElementById('attCheckOutInput');
      
      if (e.target.value === 'work') {
        // Khi chọn "Đi làm", nếu ô giờ đang trống thì điền giờ ca mặc định
        if (inInput && !inInput.value && currentShiftStartTime) {
          inInput.value = currentShiftStartTime.substring(0,5);
        }
        if (outInput && !outInput.value && currentShiftEndTime) {
          outInput.value = currentShiftEndTime.substring(0,5);
        }
      } else {
        // Clear time inputs when not work to avoid stale values
        if (inInput) inInput.value = '';
        if (outInput) outInput.value = '';
      }
    }
  });
})();
</script>
