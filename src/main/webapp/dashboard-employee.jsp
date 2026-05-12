<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.liteflow.model.timesheet.EmployeeAttendance" %>

<jsp:include page="includes/header.jsp">
  <jsp:param name="page" value="dashboard-employee" />
</jsp:include>

<%
// Lấy thông tin tháng hiện tại
Integer currentYear = (Integer) request.getAttribute("currentYear");
Integer currentMonth = (Integer) request.getAttribute("currentMonth");
Map<Integer, EmployeeAttendance> attendanceMap = (Map<Integer, EmployeeAttendance>) request.getAttribute("attendanceMap");

if (currentYear == null) currentYear = LocalDate.now().getYear();
if (currentMonth == null) currentMonth = LocalDate.now().getMonthValue();
if (attendanceMap == null) attendanceMap = new java.util.HashMap<>();

LocalDate firstDay = LocalDate.of(currentYear, currentMonth, 1);
int daysInMonth = firstDay.lengthOfMonth();
DayOfWeek firstDayOfWeek = firstDay.getDayOfWeek();
// Java DayOfWeek: Monday=1, Tuesday=2, ..., Sunday=7
// Calendar header: Sunday=0, Monday=1, ..., Saturday=6
// Nếu getValue()=7 (Sunday) → offset=0, nếu getValue()=1 (Monday) → offset=1
int offset = firstDayOfWeek.getValue() % 7;

// Tên tháng tiếng Việt
String[] monthNames = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                      "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard-employee.css">

<div class="dashboard-employee-container">
  <!-- Header Section -->


  <!-- Main Content Area -->
  <div class="main-content-grid">
    <!-- Top Row -->
    <div class="widget personal-schedule">
      <div class="widget-title-row">
        <h2 class="widget-title">LỊCH CÁ NHÂN</h2>
        <div class="widget-title-actions">
          <a href="${pageContext.request.contextPath}/schedule" class="btn-view-full" title="Xem lịch làm việc đầy đủ">
            <i class='bx bx-calendar'></i>
            <span>Xem đầy đủ</span>
          </a>
          <button class="btn-add-schedule" id="btnAddSchedule" type="button">
            <i class='bx bx-plus'></i> Thêm mới
          </button>
        </div>
      </div>
      <div class="schedule-list" id="personalScheduleList">
        <!-- Dữ liệu sẽ được load từ API -->
      </div>
    </div>

    <div class="widget salary-summary">
      <div class="widget-title-row">
        <h2 class="widget-title" id="salaryMonthTitle">TIỀN CÔNG THÁNG <span id="currentMonthName"></span></h2>
        <div class="widget-title-actions">
          <div class="month-year-selector">
            <select id="salaryMonthSelect" class="month-select" onchange="changeSalaryMonth()">
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
            <input type="number" id="salaryYearSelect" class="year-select" min="2020" max="2030" onchange="changeSalaryMonth()" />
          </div>
          <a href="${pageContext.request.contextPath}/employee/paysheet.jsp" class="btn-view-details" title="Xem chi tiết bảng lương">
            <i class='bx bx-detail'></i>
            <span>Xem chi tiết</span>
          </a>
        </div>
      </div>
      <div class="salary-stats">
        <div class="salary-item">
          <div class="salary-label">Tổng lương</div>
          <div class="salary-value" id="totalSalary">0</div>
        </div>
        <div class="salary-item">
          <div class="salary-label">Tổng đã ứng</div>
          <div class="salary-value advance" id="totalAdvance">0</div>
        </div>
        <div class="salary-item">
          <div class="salary-label">Tổng tiền trừ</div>
          <div class="salary-value deduction" id="totalDeduction">0</div>
        </div>
        <div class="salary-item">
          <div class="salary-label">Tổng đã thanh toán</div>
          <div class="salary-value paid" id="totalPaid">0</div>
        </div>
        <div class="salary-item highlight">
          <div class="salary-label">Tổng chưa nhận</div>
          <div class="salary-value remaining" id="totalRemaining">0</div>
        </div>
        </div>
      </div>

    <div class="widget attendance-clock">
      <h2 class="widget-title">CHẤM CÔNG HÔM NAY</h2>
      <div class="attendance-status">
        <div class="time-display">
          <div class="current-time" id="currentTime">--:--:--</div>
          <div class="current-date" id="currentDate">-- / -- / ----</div>
        </div>
        <div class="attendance-info">
          <div class="info-row" id="checkInInfo">
            <span class="info-label">Giờ vào:</span>
            <span class="info-value" id="checkInTime">--:--:--</span>
          </div>
          <div class="info-row" id="checkOutInfo">
            <span class="info-label">Giờ ra:</span>
            <span class="info-value" id="checkOutTime">--:--:--</span>
          </div>
      </div>
      </div>
      <div class="attendance-actions">
        <button class="btn-clock-toggle" id="btnClockToggle" onclick="toggleClock()">
          <i class='bx bx-time-five' id="clockIcon"></i>
          <span id="clockText">Chấm công vào</span>
        </button>
      </div>
      <div class="attendance-message" id="attendanceMessage"></div>

      <!-- Additional Actions -->
      <div class="attendance-secondary-actions">
        <button class="btn-secondary-action" onclick="openForgotClockModal()">
          <i class='bx bx-error-circle'></i>
          <span>Quên chấm công</span>
        </button>
        <button class="btn-secondary-action" onclick="openLeaveRequestModal()">
          <i class='bx bx-calendar-check'></i>
          <span>Xin nghỉ phép</span>
        </button>
      </div>
      <div class="attendance-view-history">
        <a href="${pageContext.request.contextPath}/attendance" class="btn-view-history">
          <i class='bx bx-history'></i>
          <span>Xem lịch sử chấm công</span>
        </a>
      </div>
    </div>

    <div class="widget notice-board">
      <h2 class="widget-title">
        <span><i class='bx bx-notification'></i> BẢNG THÔNG BÁO</span>
      </h2>

      <div class="notice-list" id="noticeList">
        <!-- Sample notices - these should be loaded from backend -->
        <div class="notice-item important">
          <div class="notice-header">
            <span class="notice-badge important">Quan trọng</span>
            <span class="notice-date">30/10/2025</span>
          </div>
          <div class="notice-title">Thông báo nghỉ lễ Quốc Khánh</div>
          <div class="notice-content">
            Công ty thông báo lịch nghỉ lễ Quốc Khánh 2/9 từ ngày 31/8 đến 3/9. Toàn thể nhân viên nghỉ theo quy định.
          </div>
        </div>

        <div class="notice-item general">
          <div class="notice-header">
            <span class="notice-badge general">Chung</span>
            <span class="notice-date">29/10/2025</span>
          </div>
          <div class="notice-title">Cập nhật quy trình chấm công mới</div>
          <div class="notice-content">
            Từ ngày 1/11, quy trình chấm công sẽ được cập nhật. Vui lòng chấm công đúng giờ và báo cáo khi quên chấm công.
          </div>
        </div>

        <div class="notice-item info">
          <div class="notice-header">
            <span class="notice-badge info">Thông tin</span>
            <span class="notice-date">28/10/2025</span>
          </div>
          <div class="notice-title">Lịch họp phòng ban tháng 11</div>
          <div class="notice-content">
            Lịch họp phòng ban đã được cập nhật. Vui lòng kiểm tra lịch cá nhân của bạn.
          </div>
        </div>
      </div>

      
    </div>

    <!-- Bottom Row -->
    <div class="widget timesheet-calendar large">
      <div class="widget-title-row">
        <h2 class="widget-title">LỊCH CHẤM CÔNG - KỲ CÔNG <%= monthNames[currentMonth - 1] %> <%= currentYear %></h2>
        <div class="widget-title-actions">
          <div class="calendar-nav">
            <button class="btn-nav-month" onclick="changeCalendarMonth(-1)" title="Tháng trước">
              <i class='bx bx-chevron-left'></i>
            </button>
            <button class="btn-nav-month" onclick="changeCalendarMonth(1)" title="Tháng sau">
              <i class='bx bx-chevron-right'></i>
            </button>
            <button class="btn-nav-month" onclick="resetCalendarMonth()" title="Tháng hiện tại">
              <i class='bx bx-calendar'></i>
            </button>
          </div>
          <a href="${pageContext.request.contextPath}/attendance" class="btn-view-full" title="Xem bảng chấm công đầy đủ">
            <i class='bx bx-time'></i>
            <span>Xem đầy đủ</span>
          </a>
        </div>
      </div>
      <div class="timesheet-legend">
        <div class="legend-dot green"></div><span>Đúng giờ</span>
        <div class="legend-dot purple"></div><span>Vi phạm (muộn/sớm)</span>
        <div class="legend-dot red"></div><span>Tăng ca</span>
        <div class="legend-dot orange"></div><span>Quên chấm công</span>
        <div class="legend-dot gray"></div><span>Vắng mặt</span>
      </div>
      <div class="calendar-grid">
        <div class="calendar-header">
          <div>Chủ nhật</div>
          <div>Thứ 2</div>
          <div>Thứ 3</div>
          <div>Thứ 4</div>
          <div>Thứ 5</div>
          <div>Thứ 6</div>
          <div>Thứ 7</div>
        </div>
        <div class="calendar-body">
<%
// Tạo calendar
// Offset đầu tháng (fill empty cells)
for (int i = 0; i < offset; i++) {
%>
          <div class="calendar-day empty"></div>
<%
}

// Tạo các ngày trong tháng
for (int day = 1; day <= daysInMonth; day++) {
    EmployeeAttendance attendance = attendanceMap.get(day);
    String statusDot = "gray"; // Mặc định là vắng mặt (gray)
    
    if (attendance != null) {
        Boolean isOvertime = attendance.getIsOvertime();
        Boolean isLate = attendance.getIsLate();
        Boolean isEarlyLeave = attendance.getIsEarlyLeave();
        
        // Logic mới: Ưu tiên vi phạm trước, sau đó mới đến tăng ca
        boolean hasViolation = (isLate != null && isLate) || (isEarlyLeave != null && isEarlyLeave);
        
        if (hasViolation) {
            // Có vi phạm (đi muộn hoặc về sớm) -> màu tím
            statusDot = "purple";
        } else if (isOvertime != null && isOvertime) {
            // Chỉ có tăng ca, không vi phạm -> màu đỏ
            statusDot = "red";
        } else if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            // Chấm công đầy đủ, đúng giờ -> màu xanh lá
            statusDot = "green";
        } else if (attendance.getCheckInTime() != null || attendance.getCheckOutTime() != null) {
            // Chỉ chấm 1 lần (quên chấm công) -> màu cam
            statusDot = "orange";
        }
    }
    
    LocalDate today = LocalDate.now();
    boolean isToday = (day == today.getDayOfMonth() && 
                        currentMonth == today.getMonthValue() && 
                        currentYear == today.getYear());
    
    String todayClass = isToday ? " today" : "";
%>
          <div class="calendar-day<%= todayClass %> clickable-day" 
               data-day="<%= day %>" 
               data-month="<%= currentMonth %>" 
               data-year="<%= currentYear %>"
               onclick="viewDayDetails(<%= day %>, <%= currentMonth %>, <%= currentYear %>)"
               title="Click để xem chi tiết ngày <%= day %>/<%= currentMonth %>/<%= currentYear %>">
            <span><%= day %></span>
            <div class="status-dot <%= statusDot %>"></div>
          </div>
<%
}
%>
        </div>
      </div>
    </div>

  </div>
</div>

<script>
// Context path for API calls
const CONTEXT_PATH = '<c:out value="${pageContext.request.contextPath}" />';

// Global variable
let currentEditingSchedule = null;

// Helper function to escape HTML
function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

// Priority sort order: High -> Medium -> Low
function getPriorityOrder(priority) {
  switch(priority) {
    case 'High': return 1;
    case 'Medium': return 2;
    case 'Low': return 3;
    default: return 4;
  }
}

// Format date to Vietnamese format
function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString + 'T00:00:00');
  const days = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
  const dayName = days[date.getDay()];
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return dayName + ', ' + day + '/' + month + '/' + date.getFullYear();
}

// Format date to Vietnamese short format (DD/MM/YYYY)
function formatDateVN(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString + 'T00:00:00');
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return day + '/' + month + '/' + year;
}

// Send notification to admin/manager
async function sendNotificationToAdmin(notificationData) {
  try {
    const formData = new URLSearchParams();
    formData.append('type', notificationData.type);
    formData.append('title', notificationData.title);
    formData.append('message', notificationData.message);
    formData.append('priority', notificationData.priority || 'MEDIUM');
    if (notificationData.targetUrl) {
      formData.append('targetUrl', notificationData.targetUrl);
    }
    
    const response = await fetch(CONTEXT_PATH + '/api/notification/send-to-admin', {
      method: 'POST',
      body: formData
    });
    
    if (response.ok) {
      console.log('✅ Notification sent to admin successfully');
    } else {
      console.warn('⚠️ Failed to send notification to admin');
    }
  } catch (error) {
    console.error('❌ Error sending notification to admin:', error);
  }
}

// Open add schedule modal - MUST be available immediately
function openAddScheduleModal() {
  console.log('=== openAddScheduleModal called ===');
  try {
    currentEditingSchedule = null;
    
    // Reset form fields
    const modalScheduleId = document.getElementById('modalScheduleId');
    const modalTitle = document.getElementById('modalTitle');
    const modalDescription = document.getElementById('modalDescription');
    const modalStartDate = document.getElementById('modalStartDate');
    const modalStartTime = document.getElementById('modalStartTime');
    const modalEndTime = document.getElementById('modalEndTime');
    const modalPriority = document.getElementById('modalPriority');
    const scheduleModalTitle = document.getElementById('scheduleModalTitle');
    const scheduleModal = document.getElementById('scheduleModal');
    
    console.log('Modal element:', scheduleModal);
    console.log('Title element:', modalTitle);
    
    if (!scheduleModal) {
      console.error('❌ Modal element not found!');
      alert('Không thể mở form. Modal element không tồn tại. Vui lòng tải lại trang.');
      return;
    }
    
    // Reset form
    if (modalScheduleId) modalScheduleId.value = '';
    if (modalTitle) modalTitle.value = '';
    if (modalDescription) modalDescription.value = '';
    if (modalStartDate) {
      modalStartDate.value = '';
      // Set default to today
      const today = new Date().toISOString().split('T')[0];
      modalStartDate.value = today;
    }
    if (modalStartTime) modalStartTime.value = '';
    if (modalEndTime) modalEndTime.value = '';
    if (modalPriority) modalPriority.value = 'Medium';
    if (scheduleModalTitle) scheduleModalTitle.textContent = 'Thêm lịch cá nhân';
    
    // Show modal
    scheduleModal.style.display = 'flex';
    scheduleModal.style.zIndex = '10000';
    scheduleModal.style.visibility = 'visible';
    scheduleModal.style.opacity = '1';
    
    console.log('✅ Modal display:', scheduleModal.style.display);
    console.log('✅ Modal opened successfully');
    
    // Focus on title input after a short delay
    if (modalTitle) {
      setTimeout(() => {
        modalTitle.focus();
        console.log('✅ Focused on title input');
      }, 100);
    }
  } catch (error) {
    console.error('❌ Error opening modal:', error);
    console.error('Error stack:', error.stack);
    alert('Có lỗi xảy ra khi mở form: ' + error.message);
  }
}

// Make functions available globally immediately
window.openAddScheduleModal = openAddScheduleModal;
window.saveSchedule = saveSchedule;
window.editSchedule = editSchedule;
window.deleteSchedule = deleteSchedule;
window.closeScheduleModal = closeScheduleModal;

// Old chart code removed - replaced with salary summary

// ==============================
// Salary Summary Functions
// ==============================

// Format currency to Vietnamese format
function formatCurrency(amount) {
  if (amount == null || amount === undefined || isNaN(amount)) {
    return '0 ₫';
  }
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount);
}

// Load salary summary for current month
async function loadSalarySummary(month, year) {
  try {
    const now = new Date();
    if (!month) month = now.getMonth() + 1;
    if (!year) year = now.getFullYear();
    
    // Set month name in title
    const monthNames = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
                       'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
    const monthNameEl = document.getElementById('currentMonthName');
    if (monthNameEl) {
      monthNameEl.textContent = monthNames[month - 1];
    }
    
    // Update selectors
    const monthSelect = document.getElementById('salaryMonthSelect');
    const yearSelect = document.getElementById('salaryYearSelect');
    if (monthSelect) monthSelect.value = month;
    if (yearSelect) yearSelect.value = year;
    
    // Call API to get salary summary
    const response = await fetch(CONTEXT_PATH + '/api/employee/salary-summary?month=' + month + '&year=' + year);
    if (!response.ok) {
      console.error('Failed to load salary summary');
      return;
    }
    
    const data = await response.json();
    
    // Update UI
    document.getElementById('totalSalary').textContent = formatCurrency(data.totalSalary || 0);
    document.getElementById('totalAdvance').textContent = formatCurrency(data.totalAdvance || 0);
    document.getElementById('totalDeduction').textContent = formatCurrency(data.totalDeduction || 0);
    document.getElementById('totalPaid').textContent = formatCurrency(data.totalPaid || 0);
    document.getElementById('totalRemaining').textContent = formatCurrency(data.totalRemaining || 0);
    
  } catch (error) {
    console.error('Error loading salary summary:', error);
    // Set default values
    document.getElementById('totalSalary').textContent = '0 ₫';
    document.getElementById('totalAdvance').textContent = '0 ₫';
    document.getElementById('totalDeduction').textContent = '0 ₫';
    document.getElementById('totalPaid').textContent = '0 ₫';
    document.getElementById('totalRemaining').textContent = '0 ₫';
  }
}

// Change salary month/year
function changeSalaryMonth() {
  const month = parseInt(document.getElementById('salaryMonthSelect').value);
  const year = parseInt(document.getElementById('salaryYearSelect').value);
  loadSalarySummary(month, year);
}

// ==============================
// Personal Schedule Functions
// ==============================

// Load personal schedules
async function loadPersonalSchedules() {
  try {
    const response = await fetch(CONTEXT_PATH + '/api/personal-schedule/');
    if (!response.ok) throw new Error('Failed to load schedules');
    
    const schedules = await response.json();
    const scheduleList = document.getElementById('personalScheduleList');
    
    if (schedules.length === 0) {
      scheduleList.innerHTML = '<div class="empty-state">Chưa có lịch cá nhân nào. Nhấn "Thêm mới" để tạo lịch.</div>';
      return;
    }
    
    // Sort schedules: by priority first (High -> Medium -> Low), then by date
    schedules.sort((a, b) => {
      const priorityDiff = getPriorityOrder(a.priority) - getPriorityOrder(b.priority);
      if (priorityDiff !== 0) return priorityDiff;
      // If same priority, sort by date (earlier dates first)
      return new Date(a.startDate) - new Date(b.startDate);
    });
    
    scheduleList.innerHTML = schedules.map(schedule => {
      const priorityClass = schedule.priority.toLowerCase();
      const priorityLabel = schedule.priority === 'High' ? 'Cao' : schedule.priority === 'Medium' ? 'Trung bình' : 'Thấp';
      
      // Format date
      const dateDisplay = formatDate(schedule.startDate);
      
      // Format time
      let timeDisplay = '';
      if (schedule.startTime || schedule.endTime) {
        const startTime = schedule.startTime ? schedule.startTime.substring(0, 5) : '';
        const endTime = schedule.endTime ? schedule.endTime.substring(0, 5) : '';
        timeDisplay = startTime + (endTime ? ' - ' + endTime : '');
      }
      
      let html = '<div class="schedule-item priority-' + priorityClass + '" data-id="' + schedule.scheduleId + '" data-priority="' + schedule.priority + '">';
      html += '<div class="schedule-header">';
      html += '<div class="schedule-title">' + escapeHtml(schedule.title) + '</div>';
      html += '<div class="schedule-actions">';
      html += '<button class="btn-edit-schedule" onclick="editSchedule(\'' + schedule.scheduleId + '\')" title="Sửa">';
      html += '<i class=\'bx bx-edit\'></i>';
      html += '</button>';
      html += '<button class="btn-delete-schedule" onclick="deleteSchedule(\'' + schedule.scheduleId + '\')" title="Xóa">';
      html += '<i class=\'bx bx-trash\'></i>';
      html += '</button>';
      html += '</div>';
      html += '</div>';
      
      if (schedule.description) {
        html += '<div class="schedule-meta">' + escapeHtml(schedule.description) + '</div>';
      }
      
      html += '<div class="schedule-footer">';
      html += '<span class="schedule-priority priority-' + priorityClass + '">' + priorityLabel + '</span>';
      if (dateDisplay) {
        html += '<span class="schedule-date">' + escapeHtml(dateDisplay) + '</span>';
      }
      if (timeDisplay) {
        html += '<span class="schedule-time">' + escapeHtml(timeDisplay) + '</span>';
      }
      html += '</div>';
      html += '</div>';
      
      return html;
    }).join('');
  } catch (error) {
    console.error('Error loading schedules:', error);
    document.getElementById('personalScheduleList').innerHTML = '<div class="error-state">Lỗi khi tải lịch cá nhân</div>';
  }
}

// Edit schedule
function editSchedule(scheduleId) {
  fetch(CONTEXT_PATH + '/api/personal-schedule/' + scheduleId)
    .then(res => res.json())
    .then(schedule => {
      currentEditingSchedule = schedule;
      document.getElementById('modalScheduleId').value = schedule.scheduleId;
      document.getElementById('modalTitle').value = schedule.title;
      document.getElementById('modalDescription').value = schedule.description || '';
      document.getElementById('modalStartDate').value = schedule.startDate;
      document.getElementById('modalStartTime').value = schedule.startTime || '';
      document.getElementById('modalEndTime').value = schedule.endTime || '';
      document.getElementById('modalPriority').value = schedule.priority;
      document.getElementById('scheduleModalTitle').textContent = 'Sửa lịch cá nhân';
      document.getElementById('scheduleModal').style.display = 'flex';
    })
    .catch(err => {
      console.error('Error loading schedule:', err);
      alert('Không thể tải thông tin lịch');
    });
}

// Delete schedule
async function deleteSchedule(scheduleId) {
  if (!confirm('Bạn có chắc chắn muốn xóa lịch này?')) return;
  
  try {
    const response = await fetch(CONTEXT_PATH + '/api/personal-schedule/' + scheduleId, {
      method: 'DELETE'
    });
    
    if (!response.ok) throw new Error('Failed to delete schedule');
    
    alert('Xóa lịch thành công');
    loadPersonalSchedules();
  } catch (error) {
    console.error('Error deleting schedule:', error);
    alert('Không thể xóa lịch');
  }
}

// Save schedule
async function saveSchedule() {
  const title = document.getElementById('modalTitle').value.trim();
  const description = document.getElementById('modalDescription').value.trim();
  const startDate = document.getElementById('modalStartDate').value;
  const startTime = document.getElementById('modalStartTime').value;
  const endTime = document.getElementById('modalEndTime').value;
  const priority = document.getElementById('modalPriority').value;
  
  if (!title || !startDate) {
    alert('Vui lòng điền đầy đủ thông tin bắt buộc');
    return;
  }
  
  const formData = new URLSearchParams();
  formData.append('title', title);
  formData.append('description', description);
  formData.append('startDate', startDate);
  if (startTime) formData.append('startTime', startTime);
  if (endTime) formData.append('endTime', endTime);
  formData.append('priority', priority);
  
  try {
    let response;
    if (currentEditingSchedule) {
      // Update existing schedule
      response = await fetch(CONTEXT_PATH + '/api/personal-schedule/' + currentEditingSchedule.scheduleId, {
        method: 'PUT',
        body: formData
      });
    } else {
      // Create new schedule
      response = await fetch(CONTEXT_PATH + '/api/personal-schedule/', {
        method: 'POST',
        body: formData
      });
    }
    
    if (!response.ok) throw new Error('Failed to save schedule');
    
    alert(currentEditingSchedule ? 'Cập nhật lịch thành công' : 'Thêm lịch thành công');
    document.getElementById('scheduleModal').style.display = 'none';
    loadPersonalSchedules();
  } catch (error) {
    console.error('Error saving schedule:', error);
    alert('Không thể lưu lịch');
  }
}

// Close modal
function closeScheduleModal() {
  const scheduleModal = document.getElementById('scheduleModal');
  if (scheduleModal) {
    scheduleModal.style.display = 'none';
  }
}

// Close modal when clicking outside
function setupModalCloseOnOutsideClick() {
  const modal = document.getElementById('scheduleModal');
  if (modal) {
    modal.addEventListener('click', function(e) {
      if (e.target === modal) {
        closeScheduleModal();
      }
    });
  }
}

// View day details in calendar
function viewDayDetails(day, month, year) {
  // Calculate week start for the selected day
  const selectedDate = new Date(year, month - 1, day);
  const dayOfWeek = selectedDate.getDay();
  const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek; // Monday = 0
  const weekStart = new Date(selectedDate);
  weekStart.setDate(selectedDate.getDate() + mondayOffset);
  
  // Format date as YYYY-MM-DD
  const weekStartStr = weekStart.getFullYear() + '-' + 
    String(weekStart.getMonth() + 1).padStart(2, '0') + '-' + 
    String(weekStart.getDate()).padStart(2, '0');
  
  // Redirect to attendance page with the week
  window.location.href = CONTEXT_PATH + '/attendance?weekStart=' + weekStartStr;
}

// Change calendar month
function changeCalendarMonth(direction) {
  const currentMonth = <%= currentMonth %>;
  const currentYear = <%= currentYear %>;
  
  let newMonth = currentMonth + direction;
  let newYear = currentYear;
  
  if (newMonth < 1) {
    newMonth = 12;
    newYear--;
  } else if (newMonth > 12) {
    newMonth = 1;
    newYear++;
  }
  
  // Reload page with new month/year
  window.location.href = CONTEXT_PATH + '/dashboard-employee?month=' + newMonth + '&year=' + newYear;
}

// Reset calendar to current month
function resetCalendarMonth() {
  window.location.href = CONTEXT_PATH + '/dashboard-employee';
}

// Load schedules when page loads
document.addEventListener('DOMContentLoaded', function() {
  console.log('DOMContentLoaded - Setting up schedule functions');
  
  // Setup button click event
  const btnAddSchedule = document.getElementById('btnAddSchedule');
  if (btnAddSchedule) {
    btnAddSchedule.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();
      console.log('Add button clicked');
      openAddScheduleModal();
    });
    console.log('Add button event listener attached');
  } else {
    console.error('btnAddSchedule button not found!');
  }
  
  // Initialize salary year selector
  const yearSelect = document.getElementById('salaryYearSelect');
  if (yearSelect) {
    const now = new Date();
    yearSelect.value = now.getFullYear();
  }
  
  loadPersonalSchedules();
  loadSalarySummary();
  loadAttendanceStatus();
  updateCurrentTime();
  setupModalCloseOnOutsideClick();
  
  // Update current time every second
  setInterval(updateCurrentTime, 1000);
  
  // Refresh attendance status every 30 seconds
  setInterval(loadAttendanceStatus, 30000);
  
  console.log('All schedule functions initialized');
});

// ==============================
// Attendance Clock Functions
// ==============================

// Update current time display
function updateCurrentTime() {
  const now = new Date();
  const timeStr = now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  const dateStr = now.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  
  document.getElementById('currentTime').textContent = timeStr;
  document.getElementById('currentDate').textContent = dateStr;
}

// Load attendance status
async function loadAttendanceStatus() {
  try {
    const response = await fetch(CONTEXT_PATH + '/api/timesheet/status');
    if (!response.ok) {
      console.error('Failed to load attendance status');
      return;
    }

    const data = await response.json();

    // Update UI based on status
    const toggleBtn = document.getElementById('btnClockToggle');
    const clockIcon = document.getElementById('clockIcon');
    const clockText = document.getElementById('clockText');
    const checkInTimeEl = document.getElementById('checkInTime');
    const checkOutTimeEl = document.getElementById('checkOutTime');

    if (data.hasClockedIn) {
      checkInTimeEl.textContent = data.checkInTime || '--:--:--';
    } else {
      checkInTimeEl.textContent = '--:--:--';
    }

    if (data.hasClockedOut) {
      checkOutTimeEl.textContent = data.checkOutTime || '--:--:--';
      toggleBtn.disabled = true;
      toggleBtn.classList.add('disabled');
      clockText.textContent = 'Đã chấm công';
      clockIcon.className = 'bx bx-check-circle';
    } else if (data.hasClockedIn) {
      checkOutTimeEl.textContent = '--:--:--';
      toggleBtn.disabled = false;
      toggleBtn.classList.remove('disabled');
      toggleBtn.classList.add('clock-out-mode');
      clockText.textContent = 'Chấm công ra';
      clockIcon.className = 'bx bx-time';
    } else {
      checkInTimeEl.textContent = '--:--:--';
      checkOutTimeEl.textContent = '--:--:--';
      toggleBtn.disabled = false;
      toggleBtn.classList.remove('disabled', 'clock-out-mode');
      clockText.textContent = 'Chấm công vào';
      clockIcon.className = 'bx bx-time-five';
    }

  } catch (error) {
    console.error('Error loading attendance status:', error);
  }
}

// Toggle Clock (In/Out)
async function toggleClock() {
  const btn = document.getElementById('btnClockToggle');
  const messageEl = document.getElementById('attendanceMessage');
  const clockText = document.getElementById('clockText');

  if (btn.disabled) {
    return;
  }

  // Check current mode based on button class
  const isClockOutMode = btn.classList.contains('clock-out-mode');
  const endpoint = isClockOutMode ? '/api/timesheet/clock-out' : '/api/timesheet/clock-in';
  const actionText = isClockOutMode ? 'chấm công ra' : 'chấm công vào';

  btn.disabled = true;
  messageEl.textContent = 'Đang xử lý...';
  messageEl.className = 'attendance-message info';

  try {
    const response = await fetch(CONTEXT_PATH + endpoint, {
      method: 'POST'
    });

    const data = await response.json();

    if (response.ok && data.success) {
      messageEl.textContent = data.message || (actionText.charAt(0).toUpperCase() + actionText.slice(1) + ' thành công!');
      messageEl.className = 'attendance-message success';

      // Update time display
      if (isClockOutMode) {
        document.getElementById('checkOutTime').textContent = data.checkOutTime || '--:--:--';
      } else {
        document.getElementById('checkInTime').textContent = data.checkInTime || '--:--:--';
      }

      // Clear message after 3 seconds
      setTimeout(function() {
        messageEl.textContent = '';
        messageEl.className = 'attendance-message';
      }, 3000);

      // Reload status to update button
      await loadAttendanceStatus();
    } else {
      messageEl.textContent = data.error || ('Không thể ' + actionText);
      messageEl.className = 'attendance-message error';
      btn.disabled = false;

      setTimeout(function() {
        messageEl.textContent = '';
        messageEl.className = 'attendance-message';
      }, 5000);
    }
  } catch (error) {
    console.error('Error ' + actionText + ':', error);
    messageEl.textContent = 'Lỗi kết nối. Vui lòng thử lại.';
    messageEl.className = 'attendance-message error';
    btn.disabled = false;

    setTimeout(function() {
      messageEl.textContent = '';
      messageEl.className = 'attendance-message';
    }, 5000);
  }
}

// Make functions globally accessible
window.toggleClock = toggleClock;

// ==============================
// Leave Request Functions
// ==============================

// Open leave request modal
function openLeaveRequestModal() {
  console.log('=== openLeaveRequestModal called ===');
  try {
    const modal = document.getElementById('leaveRequestModal');
    if (!modal) {
      console.error('❌ Leave request modal element not found!');
      alert('Không thể mở form. Modal element không tồn tại. Vui lòng tải lại trang.');
      return;
    }

    // Reset form
    document.getElementById('leaveRequestId').value = '';
    document.getElementById('leaveType').value = 'Nghỉ phép';
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    document.getElementById('reason').value = '';

    // Set default start date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    document.getElementById('startDate').value = tomorrow.toISOString().split('T')[0];
    document.getElementById('endDate').value = tomorrow.toISOString().split('T')[0];

    // Show modal
    modal.style.display = 'flex';
    modal.style.zIndex = '10000';
    modal.style.visibility = 'visible';
    modal.style.opacity = '1';

    console.log('✅ Leave request modal opened successfully');
  } catch (error) {
    console.error('❌ Error opening leave request modal:', error);
    alert('Có lỗi xảy ra khi mở form: ' + error.message);
  }
}

// Close leave request modal
function closeLeaveRequestModal() {
  const modal = document.getElementById('leaveRequestModal');
  if (modal) {
    modal.style.display = 'none';
  }
}

// Save leave request
async function saveLeaveRequest() {
  const leaveType = document.getElementById('leaveType').value;
  const startDate = document.getElementById('startDate').value;
  const endDate = document.getElementById('endDate').value;
  const reason = document.getElementById('reason').value.trim();

  if (!leaveType || !startDate || !endDate) {
    alert('Vui lòng điền đầy đủ thông tin bắt buộc');
    return;
  }

  // Validate dates
  if (new Date(endDate) < new Date(startDate)) {
    alert('Ngày kết thúc phải sau hoặc bằng ngày bắt đầu');
    return;
  }

  const formData = new URLSearchParams();
  formData.append('leaveType', leaveType);
  formData.append('startDate', startDate);
  formData.append('endDate', endDate);
  formData.append('reason', reason);

  try {
    const response = await fetch(CONTEXT_PATH + '/api/leave-request/', {
      method: 'POST',
      body: formData
    });

    const data = await response.json();

    if (response.ok) {
      alert('Đơn xin nghỉ đã được gửi thành công! Vui lòng chờ phê duyệt.');
      closeLeaveRequestModal();
      
      // Send notification to admin/manager
      await sendNotificationToAdmin({
        type: 'LEAVE_REQUEST',
        title: 'Đơn xin nghỉ phép mới',
        message: 'Nhân viên đã gửi đơn xin ' + leaveType + ' từ ' + formatDateVN(startDate) + ' đến ' + formatDateVN(endDate),
        priority: 'MEDIUM',
        targetUrl: CONTEXT_PATH + '/employee/leave-requests'
      });
      
      // Refresh notification bell if exists
      if (typeof notificationBell !== 'undefined' && notificationBell) {
        notificationBell.refresh();
      }
    } else {
      alert('Không thể gửi đơn xin nghỉ: ' + (data.error || 'Unknown error'));
    }
  } catch (error) {
    console.error('Error saving leave request:', error);
    alert('Có lỗi xảy ra khi gửi đơn xin nghỉ');
  }
}

// Make leave request functions globally accessible
window.openLeaveRequestModal = openLeaveRequestModal;
window.closeLeaveRequestModal = closeLeaveRequestModal;
window.saveLeaveRequest = saveLeaveRequest;

// ==============================
// Forgot Clock In Functions
// ==============================

// Open forgot clock in modal
function openForgotClockModal() {
  console.log('=== openForgotClockModal called ===');
  try {
    const modal = document.getElementById('forgotClockModal');
    if (!modal) {
      console.error('❌ Forgot clock modal element not found!');
      alert('Không thể mở form. Modal element không tồn tại. Vui lòng tải lại trang.');
      return;
    }

    // Reset form
    document.getElementById('forgotClockDate').value = '';
    document.getElementById('forgotClockType').value = 'CHECK_IN';
    document.getElementById('forgotClockTime').value = '';
    document.getElementById('forgotClockReason').value = '';

    // Set default date to yesterday
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    document.getElementById('forgotClockDate').value = yesterday.toISOString().split('T')[0];

    // Show/hide time field based on type
    updateForgotClockTimeField();

    // Show modal
    modal.style.display = 'flex';
    modal.style.zIndex = '10000';
    modal.style.visibility = 'visible';
    modal.style.opacity = '1';

    console.log('✅ Forgot clock modal opened successfully');
  } catch (error) {
    console.error('❌ Error opening forgot clock modal:', error);
    alert('Có lỗi xảy ra khi mở form: ' + error.message);
  }
}

// Close forgot clock modal
function closeForgotClockModal() {
  const modal = document.getElementById('forgotClockModal');
  if (modal) {
    modal.style.display = 'none';
  }
}

// Update time field based on forgot clock type
function updateForgotClockTimeField() {
  const type = document.getElementById('forgotClockType').value;
  const timeGroup = document.getElementById('forgotClockTimeGroup');
  const timeLabel = document.getElementById('forgotClockTimeLabel');
  
  if (type === 'CHECK_IN') {
    timeLabel.innerHTML = 'Giờ vào (nếu nhớ)';
    timeGroup.style.display = 'block';
  } else if (type === 'CHECK_OUT') {
    timeLabel.innerHTML = 'Giờ ra (nếu nhớ)';
    timeGroup.style.display = 'block';
  } else {
    timeGroup.style.display = 'none';
  }
}

// Save forgot clock request
async function saveForgotClockRequest() {
  const forgotDate = document.getElementById('forgotClockDate').value;
  const forgotType = document.getElementById('forgotClockType').value;
  const forgotTime = document.getElementById('forgotClockTime').value;
  const reason = document.getElementById('forgotClockReason').value.trim();

  if (!forgotDate || !forgotType || !reason) {
    alert('Vui lòng điền đầy đủ thông tin bắt buộc');
    return;
  }

  // Validate date is not in future
  const selectedDate = new Date(forgotDate);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  if (selectedDate >= today) {
    alert('Ngày quên chấm công phải là ngày trong quá khứ');
    return;
  }

  // Validate date is not too old (e.g., max 7 days ago)
  const maxDaysAgo = new Date();
  maxDaysAgo.setDate(maxDaysAgo.getDate() - 7);
  maxDaysAgo.setHours(0, 0, 0, 0);
  
  if (selectedDate < maxDaysAgo) {
    alert('Chỉ có thể báo quên chấm công trong vòng 7 ngày gần đây');
    return;
  }

  const formData = new URLSearchParams();
  formData.append('forgotDate', forgotDate);
  formData.append('forgotType', forgotType);
  if (forgotTime) {
    formData.append('forgotTime', forgotTime);
  }
  formData.append('reason', reason);

  try {
    const response = await fetch(CONTEXT_PATH + '/api/forgot-clock/', {
      method: 'POST',
      body: formData
    });

    const data = await response.json();

    if (response.ok) {
      alert('Yêu cầu quên chấm công đã được gửi thành công! Vui lòng chờ quản lý xét duyệt.');
      closeForgotClockModal();
      
      // Get forgot type text
      let forgotTypeText = '';
      switch(forgotType) {
        case 'CHECK_IN': forgotTypeText = 'chấm công vào'; break;
        case 'CHECK_OUT': forgotTypeText = 'chấm công ra'; break;
        case 'BOTH': forgotTypeText = 'chấm công vào và ra'; break;
      }
      
      // Send notification to admin/manager
      await sendNotificationToAdmin({
        type: 'FORGOT_CLOCK',
        title: 'Yêu cầu quên chấm công',
        message: 'Nhân viên báo quên ' + forgotTypeText + ' vào ngày ' + formatDateVN(forgotDate),
        priority: 'HIGH',
        targetUrl: CONTEXT_PATH + '/attendance/forgot-clock-requests'
      });
      
      // Refresh notification bell if exists
      if (typeof notificationBell !== 'undefined' && notificationBell) {
        notificationBell.refresh();
      }
    } else {
      alert('Không thể gửi yêu cầu: ' + (data.error || 'Unknown error'));
    }
  } catch (error) {
    console.error('Error saving forgot clock request:', error);
    alert('Có lỗi xảy ra khi gửi yêu cầu');
  }
}

// Make forgot clock functions globally accessible
window.openForgotClockModal = openForgotClockModal;
window.closeForgotClockModal = closeForgotClockModal;
window.saveForgotClockRequest = saveForgotClockRequest;
window.updateForgotClockTimeField = updateForgotClockTimeField;

// Setup modal close on outside click for leave request modal
function setupLeaveRequestModalCloseOnOutsideClick() {
  const modal = document.getElementById('leaveRequestModal');
  if (modal) {
    modal.addEventListener('click', function(e) {
      if (e.target === modal) {
        closeLeaveRequestModal();
      }
    });
  }
}

// Setup modal close on outside click for forgot clock modal
function setupForgotClockModalCloseOnOutsideClick() {
  const modal = document.getElementById('forgotClockModal');
  if (modal) {
    modal.addEventListener('click', function(e) {
      if (e.target === modal) {
        closeForgotClockModal();
      }
    });
  }
}

// Call setup function when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
  setupLeaveRequestModalCloseOnOutsideClick();
  setupForgotClockModalCloseOnOutsideClick();
});
</script>

<!-- Schedule Modal -->
<div id="scheduleModal" class="modal-overlay" style="display: none; z-index: 10000;">
  <div class="modal-content" onclick="event.stopPropagation();">
    <div class="modal-header">
      <h3 id="scheduleModalTitle">Thêm lịch cá nhân</h3>
      <button class="modal-close" onclick="closeScheduleModal()">
        <i class='bx bx-x'></i>
      </button>
    </div>
    <div class="modal-body">
      <input type="hidden" id="modalScheduleId" />
      
      <div class="form-group">
        <label>Tên công việc <span class="required">*</span></label>
        <input type="text" id="modalTitle" class="form-control" placeholder="Nhập tên công việc" required />
      </div>
      
      <div class="form-group">
        <label>Mô tả</label>
        <textarea id="modalDescription" class="form-control" rows="3" placeholder="Nhập mô tả chi tiết"></textarea>
      </div>
      
      <div class="form-group">
        <label>Ngày <span class="required">*</span></label>
        <input type="date" id="modalStartDate" class="form-control" required />
      </div>
      
      <div class="form-row">
        <div class="form-group">
          <label>Giờ bắt đầu</label>
          <input type="time" id="modalStartTime" class="form-control" />
        </div>
        <div class="form-group">
          <label>Giờ kết thúc</label>
          <input type="time" id="modalEndTime" class="form-control" />
        </div>
      </div>
      
      <div class="form-group">
        <label>Mức độ ưu tiên</label>
        <select id="modalPriority" class="form-control">
          <option value="Low">Thấp</option>
          <option value="Medium" selected>Trung bình</option>
          <option value="High">Cao</option>
        </select>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn-cancel" onclick="closeScheduleModal()">Hủy</button>
      <button class="btn-save" onclick="saveSchedule()">Lưu</button>
    </div>
  </div>
</div>

<!-- Leave Request Modal -->
<div id="leaveRequestModal" class="modal-overlay" style="display: none; z-index: 10000;">
  <div class="modal-content" onclick="event.stopPropagation();">
    <div class="modal-header">
      <h3>Đơn xin nghỉ phép</h3>
      <button class="modal-close" onclick="closeLeaveRequestModal()">
        <i class='bx bx-x'></i>
      </button>
    </div>
    <div class="modal-body">
      <input type="hidden" id="leaveRequestId" />

      <div class="form-group">
        <label>Loại nghỉ phép <span class="required">*</span></label>
        <select id="leaveType" class="form-control" required>
          <option value="Nghỉ phép">Nghỉ phép</option>
          <option value="Nghỉ bệnh">Nghỉ bệnh</option>
          <option value="Nghỉ không lương">Nghỉ không lương</option>
          <option value="Nghỉ khác">Nghỉ khác</option>
        </select>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Ngày bắt đầu <span class="required">*</span></label>
          <input type="date" id="startDate" class="form-control" required />
        </div>
        <div class="form-group">
          <label>Ngày kết thúc <span class="required">*</span></label>
          <input type="date" id="endDate" class="form-control" required />
        </div>
      </div>

      <div class="form-group">
        <label>Lý do</label>
        <textarea id="reason" class="form-control" rows="4" placeholder="Nhập lý do xin nghỉ phép (tùy chọn)"></textarea>
      </div>

      <div class="form-note">
        <i class='bx bx-info-circle'></i>
        <span>Đơn xin nghỉ sẽ được gửi đến quản lý để phê duyệt. Bạn sẽ nhận được thông báo khi đơn được xử lý.</span>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn-cancel" onclick="closeLeaveRequestModal()">Hủy</button>
      <button class="btn-save" onclick="saveLeaveRequest()">Gửi đơn</button>
    </div>
  </div>
</div>

<!-- Forgot Clock Modal -->
<div id="forgotClockModal" class="modal-overlay" style="display: none; z-index: 10000;">
  <div class="modal-content" onclick="event.stopPropagation();">
    <div class="modal-header">
      <h3>Báo quên chấm công</h3>
      <button class="modal-close" onclick="closeForgotClockModal()">
        <i class='bx bx-x'></i>
      </button>
    </div>
    <div class="modal-body">
      <div class="form-group">
        <label>Ngày quên chấm công <span class="required">*</span></label>
        <input type="date" id="forgotClockDate" class="form-control" required />
      </div>

      <div class="form-group">
        <label>Loại quên chấm công <span class="required">*</span></label>
        <select id="forgotClockType" class="form-control" onchange="updateForgotClockTimeField()" required>
          <option value="CHECK_IN">Quên chấm công vào</option>
          <option value="CHECK_OUT">Quên chấm công ra</option>
          <option value="BOTH">Quên chấm cả vào và ra</option>
        </select>
      </div>

      <div class="form-group" id="forgotClockTimeGroup">
        <label id="forgotClockTimeLabel">Giờ vào (nếu nhớ)</label>
        <input type="time" id="forgotClockTime" class="form-control" />
      </div>

      <div class="form-group">
        <label>Lý do <span class="required">*</span></label>
        <textarea id="forgotClockReason" class="form-control" rows="4" placeholder="Nhập lý do quên chấm công (bắt buộc)" required></textarea>
      </div>

      <div class="form-note">
        <i class='bx bx-info-circle'></i>
        <span>Yêu cầu sẽ được gửi đến quản lý để xét duyệt. Chỉ có thể báo quên chấm công trong vòng 7 ngày gần đây.</span>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn-cancel" onclick="closeForgotClockModal()">Hủy</button>
      <button class="btn-save" onclick="saveForgotClockRequest()">Gửi yêu cầu</button>
    </div>
  </div>
</div>

<!-- Notice Board Script -->
<script src="${pageContext.request.contextPath}/js/notice-board.js"></script>

<jsp:include page="includes/footer.jsp" />
