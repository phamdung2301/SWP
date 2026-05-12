<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.liteflow.service.inventory.RoomTableService" %>
<jsp:include page="includes/header.jsp">
  <jsp:param name="page" value="dashboard" />
  <jsp:param name="currentDate" value="Thứ Hai, 06/10/2025" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">

<%
  RoomTableService roomTableService = new RoomTableService();
  int occupiedTables = roomTableService.getOccupiedTables();
  java.math.BigDecimal totalActiveAmount = roomTableService.getTotalActiveSessionsAmount();
  long completedOrdersToday = roomTableService.getCompletedOrdersToday();
  long completedOrdersYesterday = roomTableService.getCompletedOrdersYesterday();
  
  // Calculate growth percentage
  double growthPercentage = 0.0;
  if (completedOrdersYesterday > 0) {
    growthPercentage = ((completedOrdersToday - completedOrdersYesterday) * 100.0) / completedOrdersYesterday;
  } else if (completedOrdersToday > 0) {
    growthPercentage = 100.0;
  }
%>

<div class="dashboard-content">
  <!-- Left Section -->
  <div class="left-section">
    <!-- Sales Results -->
    <div class="sales-results">
      <div class="section-title">KẾT QUẢ BÁN HÀNG HÔM NAY</div>
      <div class="sales-cards">
        <div class="sales-card">
          <div class="icon blue">
            <i class='bx bx-dollar'></i>
          </div>
          <div class="value"><%= completedOrdersToday %></div>
          <div class="label">Đơn đã xong</div>
          <div class="change <%= growthPercentage >= 0 ? "up" : "down" %>">
            <%= growthPercentage >= 0 ? "↑" : "↓" %> <%= String.format("%.1f", Math.abs(growthPercentage)) %>%
          </div>
          <div style="font-size: 12px; color: #6a7a92; margin-top: 4px;">Hôm qua <%= completedOrdersYesterday %></div>
        </div>
        <div class="sales-card">
          <div class="icon green">
            <i class='bx bx-edit'></i>
          </div>
          <div class="value"><%= occupiedTables %></div>
          <div class="label">Đơn đang phục vụ</div>
          <div class="change up" style="font-size:12px;color:#6a7a92;margin-top:4px;">
            Tổng giá trị: <%= new java.text.DecimalFormat("#,###").format(totalActiveAmount) %> ₫
          </div>
        </div>
        <div class="sales-card">
          <div class="icon teal">
            <i class='bx bx-user'></i>
          </div>
          <div class="value">0</div>
          <div class="label">Khách hàng</div>
          <div class="change up">↑ 0%</div>
          <div style="font-size: 12px; color: #6a7a92; margin-top: 4px;">Hôm qua 0</div>
        </div>
      </div>
    </div>

    <!-- Revenue Section -->
    <div class="revenue-section">
      <div class="revenue-header">
        <div class="revenue-title">DOANH SỐ</div>
        <div class="revenue-tabs">
          <div class="tab" data-tab="day">Theo ngày</div>
          <div class="tab active" data-tab="hour">Theo giờ</div>
          <div class="tab" data-tab="weekday">Theo thứ</div>
        </div>
      </div>
      <div class="revenue-content">
        <div class="revenue-chart-container">
          <canvas id="revenueChart"></canvas>
        </div>
      </div>
    </div>
  </div>

  <!-- Right Section -->
  <div class="right-section">
    <!-- Advertisement Banner -->
    <div class="ad-banner" onclick="window.open('https://fpt.edu.vn', '_blank')" style="cursor: pointer;">
      <img src="${pageContext.request.contextPath}/img/fpt.jpg" alt="FPT Logo" class="logo">
      <div class="title">FPT University</div>
      <div class="subtitle">Top University in Vietnam - Innovation & Excellence</div>
    </div>

    <!-- Recent Activities -->
    <div class="activities-section">
      <div class="activities-header">
        <div class="activities-title">CÁC HOẠT ĐỘNG GẦN ĐÂY</div>
        <select class="activities-dropdown">
          <option>Hôm nay</option>
          <option>Tuần này</option>
          <option>Tháng này</option>
        </select>
      </div>
      <div class="activity-item">
        <div class="activity-icon">
          <i class='bx bx-receipt'></i>
        </div>
        <div class="activity-content">
          <div class="activity-text">Glang vừa bán đơn hàng với giá trị 195,000</div>
          <div class="activity-time">5 days ago</div>
        </div>
      </div>
      <div class="activity-item">
        <div class="activity-icon">
          <i class='bx bx-receipt'></i>
        </div>
        <div class="activity-content">
          <div class="activity-text">Glang vừa bán đơn hàng với giá trị 45,000</div>
          <div class="activity-time">5 days ago</div>
        </div>
      </div>
      <div class="activity-item">
        <div class="activity-icon">
          <i class='bx bx-receipt'></i>
        </div>
        <div class="activity-content">
          <div class="activity-text">Glang vừa bán đơn giao hàng với giá trị 1,321,000</div>
          <div class="activity-time">5 days ago</div>
        </div>
      </div>
      <div class="activity-item">
        <div class="activity-icon">
          <i class='bx bx-receipt'></i>
        </div>
        <div class="activity-content">
          <div class="activity-text">Hoàng - Kinh Doanh vừa bán đơn hàng với giá trị 460,000</div>
          <div class="activity-time">5 days ago</div>
        </div>
      </div>
      <div class="activity-item">
        <div class="activity-icon">
          <i class='bx bx-receipt'></i>
        </div>
        <div class="activity-content">
          <div class="activity-text">Hương - Kế Toán vừa bán đơn hàng với giá trị 959,000</div>
          <div class="activity-time">5 days ago</div>
        </div>
      </div>
    </div>
  </div>
</div>


<!-- Chart.js -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>

<!-- Dashboard Enhancements -->
<script>
  // Set context path for JavaScript
  window.CONTEXT_PATH = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/dashboard-enhancements.js"></script>
<!-- Revenue Data Loader -->
<script src="${pageContext.request.contextPath}/js/dashboard-revenue.js"></script>

<jsp:include page="includes/footer.jsp" />