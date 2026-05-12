<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="includes/header.jsp">
  <jsp:param name="page" value="schedule" />
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

/* Quick add shift styles */
.clickable-cell {
  cursor: pointer !important;
  transition: background-color 0.2s ease;
  position: relative;
}

.clickable-cell:hover {
  background-color: #f0f9ff !important;
  color: #0369a1 !important;
}

.clickable-cell:hover::after {
  content: '+';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 16px;
  font-weight: bold;
  color: #0369a1;
}

.clickable-cell:not(:hover) {
  background-color: transparent !important;
  color: #9ca3af !important;
}

/* Modal animation */
#quickAddShiftOverlay {
  animation: fadeIn 0.2s ease;
}

#quickAddShiftOverlay > div {
  animation: slideIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from { 
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to { 
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Small gap below each shift for easier clicking */
.schedule-cell .shift-block {
  margin-bottom: 6px;
}
</style>

<c:if test="${param.embed == '1'}">
  <style>
    /* Hide global chrome when embedded */
    .top-header, .main-nav, .utf8-footer, .floating-support { display: none !important; }
    .main .content { padding: 0 !important; }
    .schedule-container { padding: 8px !important; }
    body { background: transparent; }
    /* Hide page title and search bar in embed mode */
    .schedule-header h1 { display: none !important; }
    .schedule-header form { display: none !important; }
  </style>
</c:if>

<div class="schedule-container">
  <!-- Header Section -->
  <div class="schedule-header">
    <h1>Lịch làm việc</h1>
    <div class="header-actions" style="display:flex; align-items:center; gap:12px; flex-wrap:nowrap;">
      <!-- Search/Filter Bar - chỉ hiển thị nếu không phải Employee -->
      <c:if test="${!isEmployee}">
        <form method="get" action="${pageContext.request.contextPath}/schedule" style="display:flex; align-items:center; gap:8px; flex-wrap:nowrap; background:#fff; padding:8px 12px; border:1px solid #e5e7eb; border-radius:10px;">
          <input type="hidden" name="weekStart" value="${currentWeekStart}" />
          <c:if test="${param.embed == '1'}">
            <input type="hidden" name="embed" value="1" />
          </c:if>
          <select name="employeeCode" style="width:220px; padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px;">
            <option value="">Chọn nhân viên</option>
            <c:forEach var="e" items="${employees}">
              <option value="${e.employeeCode}" <c:if test='${selectedEmployeeCode == e.employeeCode}'>selected</c:if>>${e.employeeCode} - ${e.fullName}</option>
            </c:forEach>
          </select>
          <select name="templateName" style="width:220px; padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px;">
            <option value="">Chọn ca làm việc</option>
            <c:forEach var="t" items="${templates}">
              <option value="${t.name}" <c:if test='${selectedTemplateName == t.name}'>selected</c:if>>${t.name}</option>
            </c:forEach>
          </select>
          <button type="submit" class="btn btn-primary" title="Tìm kiếm" style="display:flex; align-items:center; justify-content:center; width:40px; height:36px; padding:0;">
            <i class='bx bx-search'></i>
          </button>
          <a href="${pageContext.request.contextPath}/schedule?weekStart=${currentWeekStart}<c:if test='${param.embed == "1"}'> &amp;embed=1</c:if>" class="btn btn-light" title="Hủy lọc" style="display:flex; align-items:center; justify-content:center; width:36px; height:36px; padding:0;">
            <i class='bx bx-filter-alt-off'></i>
          </a>
        </form>
      </c:if>
      <div class="schedule-toolbar" style="margin: 0 12px 0 0;">
        <div class="week-chip" id="weekChip">
          <a class="chip-btn prev" href="${pageContext.request.contextPath}/schedule?weekStart=${prevWeekStart}${filterQuery}"><i class='bx bx-chevron-left'></i></a>
          <button type="button" class="chip-label" id="openCalendar">${controlLabel}</button>
          <a class="chip-btn next" href="${pageContext.request.contextPath}/schedule?weekStart=${nextWeekStart}${filterQuery}"><i class='bx bx-chevron-right'></i></a>
        </div>
        <a class="btn btn-light" href="${pageContext.request.contextPath}/schedule?weekStart=${currentWeekStart}${filterQuery}<c:if test='${param.embed == "1"}'> &amp;embed=1</c:if>">Tuần này</a>
      </div>
      <!-- Button thêm lịch - chỉ hiển thị nếu không phải Employee -->
      <c:if test="${!isEmployee}">
        <button class="btn btn-primary" id="openAddShift" type="button">
          <i class='bx bx-plus'></i> Thêm lịch làm việc
        </button>
      </c:if>
    </div>
  </div>

  <!-- Main Content Area -->
  <div class="main-content">
    

    <div class="calendar-popover" id="calendarPopover" hidden>
      <div class="calendar-header">
        <button class="cal-nav" id="calPrev"><i class='bx bx-chevron-left'></i></button>
        <div class="cal-title" id="calTitle"></div>
        <button class="cal-nav" id="calNext"><i class='bx bx-chevron-right'></i></button>
      </div>
      <div class="calendar-grid" id="calendarGrid"></div>
    </div>

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
          <!-- Render each base shift row (templates) -->
          <c:forEach var="t" items="${templates}">
            <tr>
              <td class="time-col">
                <div class="slot-title">${t.name}</div>
                <div class="slot-time">${t.startTime} - ${t.endTime}</div>
              </td>
              <c:forEach var="d" items="${weekDays}">
                <td class="schedule-cell"
                     data-date="${d.dateStr}"
                     data-day-label="${d.label}"
                     data-template-name="${t.name}"
                     data-start-time="${t.startTime.toString().substring(0,5)}"
                     data-end-time="${t.endTime.toString().substring(0,5)}">
                  <c:set var="rowFound" value="false" />
                  <c:forEach var="row" items="${d.rows}">
                    <c:if test="${row.templateName == t.name}">
                      <c:choose>
                        <c:when test="${empty row.items}">
                          <div class="empty-day<c:if test='${!isEmployee}'> clickable-cell</c:if>" 
                               data-date="${d.dateStr}"
                               data-day-label="${d.label}"
                               data-template-name="${t.name}"
                               data-start-time="${t.startTime.toString().substring(0,5)}"
                               data-end-time="${t.endTime.toString().substring(0,5)}"
                               <c:if test='${!isEmployee}'>title="Click để thêm lịch làm việc"</c:if>>—</div>
                        </c:when>
                        <c:otherwise>
                          <c:forEach var="s" items="${row.items}">
                            <div class="shift-block"
                                 data-shift-id="${s.shiftId}"
                                 data-title="${s.title}"
                                 data-employee="${s.employee}"
                                 data-notes="${s.notes}"
                                 data-location="${s.location}"
                                 data-status="${s.status}"
                                 data-start-at="${s.startAt}"
                                 data-end-at="${s.endAt}"
                                 data-is-recurring="${s.isRecurring}">
                              <div class="shift-emp">${s.employee}</div>
                              <c:if test="${not empty s.notes}">
                                <div class="shift-notes">${s.notes}</div>
                              </c:if>
                              <c:if test="${not empty s.location}">
                                <div class="shift-location"><i class='bx bx-map'></i> ${s.location}</div>
                              </c:if>
                            </div>
                          </c:forEach>
                        </c:otherwise>
                      </c:choose>
                      <c:set var="rowFound" value="true" />
                    </c:if>
                  </c:forEach>
                  <c:if test="${!rowFound}">
                    <div class="empty-day<c:if test='${!isEmployee}'> clickable-cell</c:if>" 
                         data-date="${d.dateStr}"
                         data-day-label="${d.label}"
                         data-template-name="${t.name}"
                         data-start-time="${t.startTime.toString().substring(0,5)}"
                         data-end-time="${t.endTime.toString().substring(0,5)}"
                         <c:if test='${!isEmployee}'>title="Click để thêm lịch làm việc"</c:if>>—</div>
                  </c:if>
                </td>
              </c:forEach>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>

<!-- Add Shift Modal - chỉ hiển thị nếu không phải Employee -->
<c:if test="${!isEmployee}">
<div id="addShiftOverlay" style="position:fixed; inset:0; background:rgba(0,0,0,.45); display:none; align-items:center; justify-content:center; z-index:1000;">
  <div style="background:#fff; width:95%; max-width:640px; border-radius:10px; box-shadow:0 10px 30px rgba(0,0,0,0.2); overflow:hidden;">
    <div style="display:flex; align-items:center; justify-content:space-between; padding:16px 20px; border-bottom:1px solid #eee;">
      <h3 style="margin:0; font-size:18px; font-weight:600;">Thêm lịch làm việc</h3>
      <button type="button" id="closeAddShift" style="background:transparent; border:none; font-size:20px; cursor:pointer; padding:6px 10px; border-radius:6px;">✕</button>
    </div>
    <form id="addShiftForm" method="post" action="${pageContext.request.contextPath}/schedule" style="padding:20px; display:grid; grid-template-columns:1fr 1fr; gap:12px 16px;">
      <input type="hidden" name="action" value="create" />
      <input type="hidden" name="weekStart" value="${currentWeekStart}" />
      <c:if test="${param.embed == '1'}">
        <input type="hidden" name="embed" value="1" />
        <input type="hidden" name="redirectEmployeeCode" value="${selectedEmployeeCode}" />
      </c:if>

      <div style="grid-column:1 / -1;">
        <label style="font-size:12px; color:#6b7280; display:block;">Nhân viên</label>
        <div id="employeeChecklist" style="display:grid; grid-template-columns:repeat(2, minmax(0,1fr)); gap:8px; max-height:220px; overflow:auto; padding:8px; border:1px solid #e5e7eb; border-radius:8px;">
          <c:forEach var="e" items="${employees}">
            <label style="display:flex; align-items:center; gap:8px;">
              <input type="checkbox" name="employeeCode" value="${e.employeeCode}" />
              <span>${e.employeeCode} - ${e.fullName}</span>
            </label>
          </c:forEach>
        </div>
        <div style="font-size:12px; color:#6b7280; margin-top:4px;">Chọn 1 hoặc nhiều nhân viên</div>
      </div>

      <div>
        <label for="date" style="font-size:12px; color:#6b7280;">Ngày</label>
        <input id="date" name="date" type="date" required style="width:100%; padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px;" />
      </div>
      <div>
        <label for="location" style="font-size:12px; color:#6b7280;">Địa điểm</label>
        <input id="location" name="location" type="text" placeholder="VD: Quầy, Bếp..." style="width:100%; padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px;" />
      </div>

      <div style="grid-column:1 / -1;">
        <label style="font-size:12px; color:#6b7280; display:block; margin-bottom:6px;">Chọn ca</label>
        <div id="templateOptions" style="display:flex; flex-wrap:wrap; gap:8px;">
          <c:forEach var="t" items="${templates}">
            <button type="button"
                    class="tmpl-option"
                    data-start="${t.startTime.toString().substring(0,5)}"
                    data-end="${t.endTime.toString().substring(0,5)}"
                    style="padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px; background:#fff; cursor:pointer;">
              <span style="font-weight:600;">${t.name}</span>
              <span style="color:#6b7280; margin-left:6px;">${t.startTime.toString().substring(0,5)} - ${t.endTime.toString().substring(0,5)}</span>
            </button>
          </c:forEach>
        </div>
        <div style="margin-top:6px; font-size:12px; color:#374151;">Đã chọn: <span id="selectedTemplateLabel" style="font-weight:600;">Chưa chọn</span></div>
        <div id="hiddenTimesContainer"></div>
      </div>

      <div>
        <label for="title" style="font-size:12px; color:#6b7280;">Tiêu đề</label>
        <input id="title" name="title" type="text" placeholder="Ca sáng, Ca tối..." style="width:100%; padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px;" />
      </div>
      <div>
        <label for="notes" style="font-size:12px; color:#6b7280;">Ghi chú</label>
        <input id="notes" name="notes" type="text" placeholder="Ghi chú thêm" style="width:100%; padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px;" />
      </div>

      <!-- Toggle lặp lại hằng tuần cho ca mới -->
      <div style="grid-column:1 / -1; margin-top:16px; padding-top:16px; border-top:1px solid #eee;">
        <div style="display:flex; align-items:center; justify-content:space-between;">
          <div>
            <label style="font-size:14px; color:#374151; font-weight:500;">Lặp lại hằng tuần</label>
            <div style="font-size:12px; color:#6b7280; margin-top:2px;">Ca làm việc này sẽ tự động lặp lại vào cùng thời điểm mỗi tuần</div>
          </div>
          <div style="position:relative;">
            <input type="checkbox" id="addRecurringToggle" name="isRecurring" value="true" style="display:none;">
            <label for="addRecurringToggle" id="addRecurringToggleLabel" style="
              display:inline-block; 
              width:48px; 
              height:24px; 
              background:#e5e7eb; 
              border-radius:12px; 
              position:relative; 
              cursor:pointer; 
              transition:background 0.3s ease;
            ">
              <span style="
                position:absolute; 
                top:2px; 
                left:2px; 
                width:20px; 
                height:20px; 
                background:#fff; 
                border-radius:50%; 
                transition:transform 0.3s ease; 
                box-shadow:0 2px 4px rgba(0,0,0,0.1);
              "></span>
            </label>
          </div>
        </div>
      </div>

      <div style="grid-column:1 / -1; display:flex; gap:10px; justify-content:flex-end; margin-top:8px;">
        <button type="button" id="cancelAddShift" class="btn btn-light">Hủy</button>
        <button type="submit" class="btn btn-primary">Lưu</button>
      </div>
    </form>
  </div>
  
</div>
</c:if>

<!-- Quick Add Shift Modal - chỉ hiển thị nếu không phải Employee -->
<c:if test="${!isEmployee}">
<div id="quickAddShiftOverlay" style="position:fixed; inset:0; background:rgba(0,0,0,.45); display:none; align-items:center; justify-content:center; z-index:1000;">
  <div style="background:#fff; width:95%; max-width:480px; border-radius:10px; box-shadow:0 10px 30px rgba(0,0,0,0.2); overflow:hidden;">
    <div style="display:flex; align-items:center; justify-content:space-between; padding:16px 20px; border-bottom:1px solid #eee;">
      <h3 style="margin:0; font-size:18px; font-weight:600;">Thêm lịch làm việc nhanh</h3>
      <button type="button" id="closeQuickAddShift" style="background:transparent; border:none; font-size:20px; cursor:pointer; padding:6px 10px; border-radius:6px;">✕</button>
    </div>
    <form id="quickAddShiftForm" method="post" action="${pageContext.request.contextPath}/schedule" style="padding:20px; display:grid; grid-template-columns:1fr 1fr; gap:12px 16px;">
      <input type="hidden" name="action" value="create" />
      <input type="hidden" name="weekStart" value="${currentWeekStart}" />
      <c:if test="${param.embed == '1'}">
        <input type="hidden" name="embed" value="1" />
        <input type="hidden" name="redirectEmployeeCode" value="${selectedEmployeeCode}" />
      </c:if>
      <input type="hidden" name="date" id="quickDate" />
      <input type="hidden" name="startTime" id="quickStartTime" />
      <input type="hidden" name="endTime" id="quickEndTime" />

      <!-- Thông tin ca làm việc được điền sẵn -->
      <div style="background:#f8fafc; padding:12px; border-radius:8px; border-left:4px solid #3b82f6; grid-column:1 / -1;">
        <div style="font-size:14px; color:#374151; font-weight:500;">Ca làm việc</div>
        <div id="quickShiftInfo" style="font-size:13px; color:#6b7280; margin-top:4px;"></div>
      </div>

      <div style="grid-column:1 / -1;">
        <label style="font-size:12px; color:#6b7280;">Nhân viên *</label>
        <div id="quickEmployeeChecklist" style="display:grid; grid-template-columns:repeat(1, minmax(0,1fr)); gap:8px; max-height:200px; overflow:auto; padding:8px; border:1px solid #e5e7eb; border-radius:8px;">
          <c:forEach var="e" items="${employees}">
            <label style="display:flex; align-items:center; gap:8px;">
              <input type="checkbox" name="employeeCode" value="${e.employeeCode}" />
              <span>${e.employeeCode} - ${e.fullName}</span>
            </label>
          </c:forEach>
        </div>
      </div>

      <div>
        <label for="quickTitle" style="font-size:12px; color:#6b7280;">Tiêu đề</label>
        <input id="quickTitle" name="title" type="text" placeholder="Ca sáng, Ca tối..." style="width:100%; padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px;" />
      </div>

      <div>
        <label for="quickLocation" style="font-size:12px; color:#6b7280;">Địa điểm</label>
        <input id="quickLocation" name="location" type="text" placeholder="VD: Quầy, Bếp..." style="width:100%; padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px;" />
      </div>

      <div style="grid-column:1 / -1;">
        <label for="quickNotes" style="font-size:12px; color:#6b7280;">Ghi chú</label>
        <input id="quickNotes" name="notes" type="text" placeholder="Ghi chú thêm" style="width:100%; padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px;" />
      </div>

      <!-- Toggle lặp lại hằng tuần -->
      <div style="grid-column:1 / -1; margin-top:8px; padding-top:16px; border-top:1px solid #eee;">
        <div style="display:flex; align-items:center; justify-content:space-between;">
          <div>
            <label style="font-size:14px; color:#374151; font-weight:500;">Lặp lại hằng tuần</label>
            <div style="font-size:12px; color:#6b7280; margin-top:2px;">Ca làm việc này sẽ tự động lặp lại vào cùng thời điểm mỗi tuần</div>
          </div>
          <div style="position:relative;">
            <input type="checkbox" id="quickRecurringToggle" name="isRecurring" value="true" style="display:none;">
            <label for="quickRecurringToggle" id="quickRecurringToggleLabel" style="
              display:inline-block; 
              width:48px; 
              height:24px; 
              background:#e5e7eb; 
              border-radius:12px; 
              position:relative; 
              cursor:pointer; 
              transition:background 0.3s ease;
            ">
              <span style="
                position:absolute; 
                top:2px; 
                left:2px; 
                width:20px; 
                height:20px; 
                background:#fff; 
                border-radius:50%; 
                transition:transform 0.3s ease; 
                box-shadow:0 2px 4px rgba(0,0,0,0.1);
              "></span>
            </label>
          </div>
        </div>
      </div>

      <div style="grid-column:1 / -1; display:flex; gap:10px; justify-content:flex-end; margin-top:8px;">
        <button type="button" id="cancelQuickAddShift" class="btn btn-light">Hủy</button>
        <button type="submit" class="btn btn-primary">Thêm ca</button>
      </div>
    </form>
  </div>
</div>
</c:if>

<!-- Shift Detail Modal - chỉ hiển thị nếu không phải Employee -->
<c:if test="${!isEmployee}">
<div id="shiftDetailOverlay" style="position:fixed; inset:0; background:rgba(0,0,0,.45); display:none; align-items:center; justify-content:center; z-index:1000;">
  <div style="background:#fff; width:95%; max-width:560px; border-radius:10px; box-shadow:0 10px 30px rgba(0,0,0,0.2); overflow:hidden;">
    <div style="display:flex; align-items:center; justify-content:space-between; padding:16px 20px; border-bottom:1px solid #eee;">
      <h3 style="margin:0; font-size:18px; font-weight:600;">Chi tiết ca làm việc</h3>
      <div>
        <form id="deleteShiftForm" method="post" action="${pageContext.request.contextPath}/schedule" style="display:inline; margin-right:8px;">
          <input type="hidden" name="action" value="delete" />
          <input type="hidden" name="weekStart" value="${currentWeekStart}" />
          <input type="hidden" name="shiftId" id="deleteShiftId" />
          <button type="submit" class="btn btn-danger" id="deleteShiftBtn">Xóa</button>
        </form>
        <button type="button" id="closeShiftDetail" class="btn btn-light">Đóng</button>
      </div>
    </div>
    <div style="padding:20px; display:grid; grid-template-columns:1fr 1fr; gap:12px 16px;">
      <div><label style="font-size:12px; color:#6b7280;">Nhân viên</label><div id="sdEmployee" style="margin-top:6px; font-weight:600;"></div></div>
      <div><label style="font-size:12px; color:#6b7280;">Tiêu đề</label><div id="sdTitle" style="margin-top:6px;"></div></div>
      <div><label style="font-size:12px; color:#6b7280;">Trạng thái</label><div id="sdStatus" style="margin-top:6px;"></div></div>
      <div><label style="font-size:12px; color:#6b7280;">Địa điểm</label><div id="sdLocation" style="margin-top:6px;"></div></div>
      <div><label style="font-size:12px; color:#6b7280;">Bắt đầu</label><div id="sdStartAt" style="margin-top:6px;"></div></div>
      <div><label style="font-size:12px; color:#6b7280;">Kết thúc</label><div id="sdEndAt" style="margin-top:6px;"></div></div>
      <div style="grid-column:1 / -1;"><label style="font-size:12px; color:#6b7280;">Ghi chú</label><div id="sdNotes" style="margin-top:6px;"></div></div>
      
      <!-- Toggle lặp lại hằng tuần -->
      <div style="grid-column:1 / -1; margin-top:16px; padding-top:16px; border-top:1px solid #eee;">
        <div style="display:flex; align-items:center; justify-content:space-between;">
          <div>
            <label style="font-size:14px; color:#374151; font-weight:500;">Lặp lại hằng tuần</label>
            <div style="font-size:12px; color:#6b7280; margin-top:2px;">Ca làm việc này sẽ tự động lặp lại vào cùng thời điểm mỗi tuần</div>
          </div>
          <div style="position:relative;">
            <input type="checkbox" id="recurringToggle" style="display:none;">
            <label for="recurringToggle" id="recurringToggleLabel" style="
              display:inline-block; 
              width:48px; 
              height:24px; 
              background:#e5e7eb; 
              border-radius:12px; 
              position:relative; 
              cursor:pointer; 
              transition:background 0.3s ease;
            ">
              <span style="
                position:absolute; 
                top:2px; 
                left:2px; 
                width:20px; 
                height:20px; 
                background:#fff; 
                border-radius:50%; 
                transition:transform 0.3s ease; 
                box-shadow:0 2px 4px rgba(0,0,0,0.1);
              "></span>
            </label>
          </div>
        </div>
        <form id="toggleRecurringForm" method="post" action="${pageContext.request.contextPath}/schedule" style="display:none;">
          <input type="hidden" name="action" value="toggleRecurring" />
          <input type="hidden" name="weekStart" value="${currentWeekStart}" />
          <input type="hidden" name="shiftId" id="toggleShiftId" />
          <input type="hidden" name="isRecurring" id="toggleIsRecurring" />
        </form>
      </div>
    </div>
  </div>
</div>
</c:if>
<script>
(function() {
  const gridBody = document.getElementById('scheduleBody');
  const weekLabel = document.getElementById('weekLabel');
  const headerCells = Array.prototype.slice.call(document.querySelectorAll('.schedule-grid thead th[data-day]'));
  // If legacy dynamic grid anchors are not present (server-rendered table), skip this block
  if (!gridBody || !weekLabel) {
    return;
  }

  function startOfWeek(date) {
    const d = new Date(date);
    const day = (d.getDay() + 6) % 7; // Monday = 0
    d.setDate(d.getDate() - day);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  function formatDate(d) {
    const dd = ('0' + d.getDate()).slice(-2);
    const mm = ('0' + (d.getMonth() + 1)).slice(-2);
    return dd + '/' + mm;
  }

  function formatFullDate(d) {
    const dd = ('0' + d.getDate()).slice(-2);
    const mm = ('0' + (d.getMonth() + 1)).slice(-2);
    return dd + '/' + mm + '/' + d.getFullYear();
  }

  let currentWeekStart = startOfWeek(new Date());

  function renderHeader() {
    headerCells.forEach(function(th, idx) {
      const date = new Date(currentWeekStart);
      date.setDate(currentWeekStart.getDate() + idx);
      const small = th.querySelector('.date');
      if (small) small.textContent = formatDate(date);
    });
    const end = new Date(currentWeekStart);
    end.setDate(end.getDate() + 6);
    weekLabel.textContent = 'Tuần ' + formatDate(currentWeekStart) + ' - ' + formatFullDate(end);
  }

  function renderBody() {
    gridBody.innerHTML = '';
    var startHour = 7, endHour = 22;
    for (var h = startHour; h <= endHour; h++) {
      var tr = document.createElement('tr');
      var timeTd = document.createElement('td');
      timeTd.className = 'time-col';
      var label = (h < 10 ? '0' : '') + h + ':00';
      timeTd.textContent = label;
      tr.appendChild(timeTd);
      for (var day = 0; day < 7; day++) {
        var td = document.createElement('td');
        td.className = 'schedule-cell';
        td.setAttribute('data-day-index', String(day));
        td.setAttribute('data-hour', String(h));
        tr.appendChild(td);
      }
      gridBody.appendChild(tr);
    }
  }

  function addShift(td) {
    if (td.querySelector('.shift-block')) return;
    var hour = parseInt(td.getAttribute('data-hour'), 10);
    var block = document.createElement('div');
    block.className = 'shift-block';
    block.innerHTML = "<div class='shift-time'>" + (hour < 10 ? '0' : '') + hour + ":00 - " + (hour + 1 < 10 ? '0' : '') + (hour + 1) + ":00</div>" +
                      "<div class='shift-title'>Ca mới</div>";
    td.appendChild(block);
  }

  var prevBtn = document.getElementById('prevWeek');
  var nextBtn = document.getElementById('nextWeek');
  var todayBtn = document.getElementById('todayWeek');
  if (prevBtn) prevBtn.addEventListener('click', function() { currentWeekStart.setDate(currentWeekStart.getDate() - 7); renderHeader(); });
  if (nextBtn) nextBtn.addEventListener('click', function() { currentWeekStart.setDate(currentWeekStart.getDate() + 7); renderHeader(); });
  if (todayBtn) todayBtn.addEventListener('click', function() { currentWeekStart = startOfWeek(new Date()); renderHeader(); });

  document.querySelector('.schedule-grid').addEventListener('click', function(e) {
    var td = e.target.closest('td.schedule-cell');
    if (!td) return;
    addShift(td);
  });

  renderBody();
  renderHeader();
  // Lightweight calendar popover for choosing a date -> navigate to its week
  var openBtn = document.getElementById('openCalendar');
  var pop = document.getElementById('calendarPopover');
  var calTitle = document.getElementById('calTitle');
  var calGrid = document.getElementById('calendarGrid');
  var calPrev = document.getElementById('calPrev');
  var calNext = document.getElementById('calNext');

  var viewYear, viewMonth; // 0-based month

  function setViewToToday() {
    var today = new Date();
    viewYear = today.getFullYear();
    viewMonth = today.getMonth();
  }

  function renderCalendar() {
    calTitle.textContent = 'Thg ' + (viewMonth + 1) + ' ' + viewYear;
    calGrid.innerHTML = '';

    var header = ['T2','T3','T4','T5','T6','T7','CN'];
    var headRow = document.createElement('div');
    headRow.className = 'cal-row cal-head';
    header.forEach(function(h){
      var c = document.createElement('div'); c.textContent = h; headRow.appendChild(c);
    });
    calGrid.appendChild(headRow);

    var first = new Date(viewYear, viewMonth, 1);
    var startIdx = (first.getDay() + 6) % 7; // Monday=0
    var daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();
    var day = 1 - startIdx;
    for (var r = 0; r < 6; r++) {
      var row = document.createElement('div');
      row.className = 'cal-row';
      for (var c = 0; c < 7; c++, day++) {
        var cell = document.createElement('button');
        cell.className = 'cal-cell';
        var thisDate = new Date(viewYear, viewMonth, day);
        if (day < 1 || day > daysInMonth) {
          cell.classList.add('muted');
          cell.textContent = thisDate.getDate();
          cell.disabled = true;
        } else {
          cell.textContent = day;
          cell.addEventListener('click', function(ev){
            var picked = new Date(viewYear, viewMonth, parseInt(ev.target.textContent, 10));
            var ws = startOfWeek(picked);
            var y = ws.getFullYear();
            var m = ('0' + (ws.getMonth()+1)).slice(-2);
            var d = ('0' + ws.getDate()).slice(-2);
            window.location.href = '${pageContext.request.contextPath}/schedule?weekStart=' + y + '-' + m + '-' + d;
          });
        }
        row.appendChild(cell);
      }
      calGrid.appendChild(row);
    }
  }

  openBtn.addEventListener('click', function(){
    if (pop.hasAttribute('hidden')) {
      setViewToToday();
      renderCalendar();
      pop.removeAttribute('hidden');
    } else {
      pop.setAttribute('hidden', 'hidden');
    }
  });
  calPrev.addEventListener('click', function(){ viewMonth -= 1; if (viewMonth<0){viewMonth=11; viewYear-=1;} renderCalendar(); });
  calNext.addEventListener('click', function(){ viewMonth += 1; if (viewMonth>11){viewMonth=0; viewYear+=1;} renderCalendar(); });

})();

// Add Shift Modal wiring
(function(){
  var overlay = document.getElementById('addShiftOverlay');
  var openBtn = document.getElementById('openAddShift');
  var closeBtn = document.getElementById('closeAddShift');
  var cancelBtn = document.getElementById('cancelAddShift');
  function open(){
    overlay.style.display = 'flex';
    // In embed mode, pre-select current employee and hide checklist
    var isEmbed = ('<c:out value="${param.embed}"/>' === '1');
    var selectedEmp = '<c:out value="${selectedEmployeeCode}"/>';
    if (isEmbed && selectedEmp) {
      var checklist = document.getElementById('employeeChecklist');
      if (checklist) {
        // Uncheck all first
        Array.prototype.slice.call(checklist.querySelectorAll('input[type="checkbox"][name="employeeCode"]')).forEach(function(cb){
          cb.checked = (cb.value === selectedEmp);
        });
        // Hide the entire employee selector section
        if (checklist.parentElement) {
          checklist.parentElement.style.display = 'none';
        } else {
          checklist.style.display = 'none';
        }
      }
    }
  }
  function close(){ overlay.style.display = 'none'; }
  if (openBtn) openBtn.addEventListener('click', open);
  if (closeBtn) closeBtn.addEventListener('click', close);
  if (cancelBtn) cancelBtn.addEventListener('click', close);
  if (overlay) overlay.addEventListener('click', function(e){ if (e.target === overlay) close(); });
  document.addEventListener('keydown', function(e){ if (e.key === 'Escape') close(); });

  // Template selection wiring
  var selectedLabel = document.getElementById('selectedTemplateLabel');
  var hiddenContainer = document.getElementById('hiddenTimesContainer');
  var options = Array.prototype.slice.call(document.querySelectorAll('.tmpl-option'));
  function fmt(s, e){ return s + ' - ' + e; }
  function updateSelectedLabel(){
    var chips = Array.prototype.slice.call(hiddenContainer.querySelectorAll('input[name="startTime"]'));
    if (!chips.length) { selectedLabel.textContent = 'Chưa chọn'; return; }
    var labels = [];
    chips.forEach(function(inp){
      var s = inp.value;
      var e = inp.nextSibling && inp.nextSibling.name === 'endTime' ? inp.nextSibling.value : '';
      // end input is not reliably nextSibling if nodes differ; query accordingly
    });
    // Build labels from pairs
    var starts = Array.prototype.slice.call(hiddenContainer.querySelectorAll('input[name="startTime"]'));
    var ends = Array.prototype.slice.call(hiddenContainer.querySelectorAll('input[name="endTime"]'));
    for (var i = 0; i < starts.length; i++) {
      labels.push(fmt(starts[i].value, ends[i] ? ends[i].value : ''));
    }
    selectedLabel.textContent = labels.join(', ');
  }
  function isSelected(s, e){
    var starts = hiddenContainer.querySelectorAll('input[name="startTime"][value="' + s + '"]');
    var ends = hiddenContainer.querySelectorAll('input[name="endTime"][value="' + e + '"]');
    return starts.length > 0 && ends.length > 0;
  }
  function addHidden(s, e){
    var si = document.createElement('input'); si.type = 'hidden'; si.name = 'startTime'; si.value = s;
    var ei = document.createElement('input'); ei.type = 'hidden'; ei.name = 'endTime'; ei.value = e;
    hiddenContainer.appendChild(si);
    hiddenContainer.appendChild(ei);
  }
  function removeHidden(s, e){
    var starts = Array.prototype.slice.call(hiddenContainer.querySelectorAll('input[name="startTime"]'));
    var ends = Array.prototype.slice.call(hiddenContainer.querySelectorAll('input[name="endTime"]'));
    for (var i = 0; i < starts.length; i++) {
      if (starts[i].value === s && ends[i] && ends[i].value === e) {
        hiddenContainer.removeChild(starts[i]);
        hiddenContainer.removeChild(ends[i]);
        break;
      }
    }
  }
  function setActive(btn, active){
    btn.style.background = active ? '#eef2ff' : '#fff';
    btn.style.borderColor = active ? '#6366f1' : '#e5e7eb';
  }
  options.forEach(function(btn){
    btn.addEventListener('click', function(){
      var s = btn.getAttribute('data-start');
      var e = btn.getAttribute('data-end');
      if (isSelected(s, e)) {
        removeHidden(s, e);
        setActive(btn, false);
      } else {
        addHidden(s, e);
        setActive(btn, true);
      }
      updateSelectedLabel();
    });
  });

  // Xử lý toggle switch cho modal thêm lịch
  var addToggle = document.getElementById('addRecurringToggle');
  var addToggleLabel = document.getElementById('addRecurringToggleLabel');
  
  if (addToggle && addToggleLabel) {
    addToggleLabel.addEventListener('click', function(e) {
      e.preventDefault();
      
      // Toggle trạng thái
      addToggle.checked = !addToggle.checked;
      
      // Cập nhật giao diện
      if (addToggle.checked) {
        addToggleLabel.style.background = '#10b981';
        addToggleLabel.querySelector('span').style.transform = 'translateX(24px)';
      } else {
        addToggleLabel.style.background = '#e5e7eb';
        addToggleLabel.querySelector('span').style.transform = 'translateX(0px)';
      }
    });
  }

  // Validate before submit
  var form = document.getElementById('addShiftForm');
  if (form) form.addEventListener('submit', function(e){
    if (!hiddenContainer.querySelector('input[name="startTime"]')) {
      e.preventDefault();
      alert('Vui lòng chọn ít nhất một ca làm việc.');
      return;
    }
    var empChecked = form.querySelectorAll('#employeeChecklist input[name="employeeCode"]:checked');
    if (!empChecked.length) {
      e.preventDefault();
      alert('Vui lòng chọn ít nhất một nhân viên.');
      return;
    }
  });
})();

// Shift Detail Modal wiring
(function(){
  var overlay = document.getElementById('shiftDetailOverlay');
  var closeBtn = document.getElementById('closeShiftDetail');
  function open(){ if (overlay) overlay.style.display = 'flex'; }
  function close(){ if (overlay) overlay.style.display = 'none'; }
  if (closeBtn) closeBtn.addEventListener('click', close);
  if (overlay) overlay.addEventListener('click', function(e){ if (e.target === overlay) close(); });
  document.addEventListener('keydown', function(e){ if (e.key === 'Escape') close(); });

  // Xử lý toggle switch
  var toggle = document.getElementById('recurringToggle');
  var toggleLabel = document.getElementById('recurringToggleLabel');
  var toggleForm = document.getElementById('toggleRecurringForm');
  
  if (toggle && toggleLabel && toggleForm) {
    toggleLabel.addEventListener('click', function(e) {
      e.preventDefault();
      
      // Toggle trạng thái
      toggle.checked = !toggle.checked;
      
      // Cập nhật giao diện
      if (toggle.checked) {
        toggleLabel.style.background = '#10b981';
        toggleLabel.querySelector('span').style.transform = 'translateX(24px)';
      } else {
        toggleLabel.style.background = '#e5e7eb';
        toggleLabel.querySelector('span').style.transform = 'translateX(0px)';
      }
      
      // Cập nhật giá trị trong form và submit
      var toggleIsRecurring = document.getElementById('toggleIsRecurring');
      if (toggleIsRecurring) {
        toggleIsRecurring.value = toggle.checked ? 'true' : 'false';
        toggleForm.submit();
      }
    });
  }

  function setText(id, v){ var el = document.getElementById(id); if (el) el.textContent = v || ''; }
  function formatDT(dt){
    if (!dt) return '';
    try { var d = new Date(dt); return d.toLocaleString('vi-VN'); } catch(e) { return dt; }
  }

  document.addEventListener('click', function(e){
    var block = e.target.closest('.shift-block');
    if (!block) return;
    var emp = block.getAttribute('data-employee') || '';
    var title = block.getAttribute('data-title') || '';
    var status = block.getAttribute('data-status') || '';
    var loc = block.getAttribute('data-location') || '';
    var startAt = block.getAttribute('data-start-at') || '';
    var endAt = block.getAttribute('data-end-at') || '';
    var notes = block.getAttribute('data-notes') || '';
    var sid = block.getAttribute('data-shift-id') || '';
    var isRecurring = block.getAttribute('data-is-recurring') || 'false';

    setText('sdEmployee', emp);
    setText('sdTitle', title);
    setText('sdStatus', status);
    setText('sdLocation', loc);
    setText('sdStartAt', formatDT(startAt));
    setText('sdEndAt', formatDT(endAt));
    setText('sdNotes', notes);
    
    var delId = document.getElementById('deleteShiftId');
    if (delId) delId.value = sid;
    
    // Cập nhật toggle switch
    var toggle = document.getElementById('recurringToggle');
    var toggleLabel = document.getElementById('recurringToggleLabel');
    var toggleShiftId = document.getElementById('toggleShiftId');
    var toggleIsRecurring = document.getElementById('toggleIsRecurring');
    
    if (toggle && toggleLabel && toggleShiftId && toggleIsRecurring) {
      toggleShiftId.value = sid;
      toggleIsRecurring.value = isRecurring;
      toggle.checked = isRecurring === 'true';
      
      // Cập nhật giao diện toggle
      if (toggle.checked) {
        toggleLabel.style.background = '#10b981';
        toggleLabel.querySelector('span').style.transform = 'translateX(24px)';
      } else {
        toggleLabel.style.background = '#e5e7eb';
        toggleLabel.querySelector('span').style.transform = 'translateX(0px)';
      }
    }
    
    open();
  });
})();

// Quick Add Shift Modal wiring
(function(){
  var overlay = document.getElementById('quickAddShiftOverlay');
  var closeBtn = document.getElementById('closeQuickAddShift');
  var cancelBtn = document.getElementById('cancelQuickAddShift');
  var form = document.getElementById('quickAddShiftForm');
  
  function open(){
    if (overlay) overlay.style.display = 'flex';
    // In embed mode, pre-select current employee and hide quick checklist
    var isEmbed = ('<c:out value="${param.embed}"/>' === '1');
    var selectedEmp = '<c:out value="${selectedEmployeeCode}"/>';
    if (isEmbed && selectedEmp) {
      var quickChecklist = document.getElementById('quickEmployeeChecklist');
      if (quickChecklist) {
        Array.prototype.slice.call(quickChecklist.querySelectorAll('input[name="employeeCode"]')).forEach(function(cb){
          cb.checked = (cb.value === selectedEmp);
        });
        if (quickChecklist.parentElement) quickChecklist.parentElement.style.display = 'none';
        else quickChecklist.style.display = 'none';
      }
    }
  }
  function close(){ if (overlay) overlay.style.display = 'none'; }
  
  if (closeBtn) closeBtn.addEventListener('click', close);
  if (cancelBtn) cancelBtn.addEventListener('click', close);
  if (overlay) overlay.addEventListener('click', function(e){ if (e.target === overlay) close(); });
  document.addEventListener('keydown', function(e){ if (e.key === 'Escape') close(); });

  // Xử lý toggle switch cho modal thêm nhanh
  var quickToggle = document.getElementById('quickRecurringToggle');
  var quickToggleLabel = document.getElementById('quickRecurringToggleLabel');
  
  if (quickToggle && quickToggleLabel) {
    quickToggleLabel.addEventListener('click', function(e) {
      e.preventDefault();
      
      // Toggle trạng thái
      quickToggle.checked = !quickToggle.checked;
      
      // Cập nhật giao diện
      if (quickToggle.checked) {
        quickToggleLabel.style.background = '#10b981';
        quickToggleLabel.querySelector('span').style.transform = 'translateX(24px)';
      } else {
        quickToggleLabel.style.background = '#e5e7eb';
        quickToggleLabel.querySelector('span').style.transform = 'translateX(0px)';
      }
    });
  }

  // Xử lý click vào ô lịch để thêm nhanh khi ô trống
  document.addEventListener('click', function(e){
    // Bỏ qua nếu click vào một ca đã tồn tại
    var existing = e.target.closest('.shift-block');
    if (existing) return;

    var td = e.target.closest('td.schedule-cell');
    if (!td) return;

    // Ưu tiên lấy từ chính ô td (đã gắn data-*)
    var dateStr = td.getAttribute('data-date');
    var dayLabel = td.getAttribute('data-day-label');
    var templateName = td.getAttribute('data-template-name');
    var startTime = td.getAttribute('data-start-time');
    var endTime = td.getAttribute('data-end-time');

    // Fallback: nếu click vào .clickable-cell cũ
    if ((!dateStr || !templateName || !startTime || !endTime)) {
      var legacy = e.target.closest('.clickable-cell');
      if (legacy) {
        dateStr = legacy.getAttribute('data-date');
        dayLabel = legacy.getAttribute('data-day-label');
        templateName = legacy.getAttribute('data-template-name');
        startTime = legacy.getAttribute('data-start-time');
        endTime = legacy.getAttribute('data-end-time');
      }
    }

    if (!dateStr || !templateName || !startTime || !endTime) return;

    // Chuyển đổi ngày từ dd/mm sang yyyy-mm-dd
    var parts = dateStr.split('/');
    var dd = parts[0];
    var mm = parts[1];

    var urlParams = new URLSearchParams(window.location.search);
    var weekStart = urlParams.get('weekStart');
    var year = new Date().getFullYear();
    if (weekStart) {
      var wsDate = new Date(weekStart);
      if (!isNaN(wsDate.getTime())) {
        year = wsDate.getFullYear();
      }
    }
    var date = year + '-' + mm + '-' + dd;

    // Điền thông tin vào form
    var qDate = document.getElementById('quickDate');
    var qStart = document.getElementById('quickStartTime');
    var qEnd = document.getElementById('quickEndTime');
    if (qDate) qDate.value = date;
    if (qStart) qStart.value = startTime;
    if (qEnd) qEnd.value = endTime;

    // Hiển thị thông tin ca làm việc
    var info = dayLabel + ' ' + dateStr + ' • ' + templateName + ' (' + startTime + ' - ' + endTime + ')';
    var infoEl = document.getElementById('quickShiftInfo');
    if (infoEl) infoEl.textContent = info;

    // Reset form các trường còn lại
    var title = document.getElementById('quickTitle');
    var loc = document.getElementById('quickLocation');
    var notes = document.getElementById('quickNotes');
    if (title) title.value = '';
    if (loc) loc.value = '';
    if (notes) notes.value = '';
    // Reset checklist nhân viên nhanh
    var quickChecklist = document.getElementById('quickEmployeeChecklist');
    if (quickChecklist) {
      Array.prototype.slice.call(quickChecklist.querySelectorAll('input[name="employeeCode"]')).forEach(function(cb){ cb.checked = false; });
    }

    // Reset toggle
    if (quickToggle && quickToggleLabel) {
      quickToggle.checked = false;
      quickToggleLabel.style.background = '#e5e7eb';
      quickToggleLabel.querySelector('span').style.transform = 'translateX(0px)';
    }

    open();
  });
  // Validate checklist trước khi submit Quick Add
  if (form) form.addEventListener('submit', function(e){
    var isEmbed = ('<c:out value="${param.embed}"/>' === '1');
    var checked = document.querySelectorAll('#quickEmployeeChecklist input[name="employeeCode"]:checked');
    if (!checked.length && !isEmbed) {
      e.preventDefault();
      alert('Vui lòng chọn ít nhất một nhân viên.');
      return;
    }
    if (isEmbed && !checked.length) {
      var selectedEmp = '<c:out value="${selectedEmployeeCode}"/>';
      if (selectedEmp) {
        var hidden = document.createElement('input');
        hidden.type = 'hidden';
        hidden.name = 'employeeCode';
        hidden.value = selectedEmp;
        form.appendChild(hidden);
      }
    }
  });
})();
</script>

<jsp:include page="includes/footer.jsp" />
