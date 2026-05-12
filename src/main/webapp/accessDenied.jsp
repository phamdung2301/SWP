<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Access Denied - LiteFlow</title>
  
  <!-- Icons + Fonts -->
  <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  
  <!-- Custom CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
  
  <style>
    body {
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }
    
    .error-container {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
    }
    
    .error-box {
      background: white;
      border-radius: 20px;
      padding: 3rem 4rem;
      max-width: 600px;
      width: 100%;
      text-align: center;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
      animation: slideUp 0.5s ease-out;
    }
    
    @keyframes slideUp {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
    
    .error-icon {
      font-size: 80px;
      color: #dc3545;
      margin-bottom: 1.5rem;
      animation: shake 0.5s ease-in-out;
    }
    
    @keyframes shake {
      0%, 100% { transform: translateX(0); }
      25% { transform: translateX(-10px) rotate(-5deg); }
      75% { transform: translateX(10px) rotate(5deg); }
    }
    
    .error-title {
      font-size: 2rem;
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 1rem;
    }
    
    .error-message {
      font-size: 1.1rem;
      color: #6c757d;
      margin-bottom: 2rem;
      line-height: 1.6;
    }
    
    .error-details {
      background: #f8f9fa;
      border-radius: 10px;
      padding: 1.5rem;
      margin-bottom: 2rem;
      border-left: 4px solid #dc3545;
    }
    
    .error-details p {
      margin: 0.5rem 0;
      color: #495057;
      font-size: 0.95rem;
    }
    
    .error-details strong {
      color: #2c3e50;
    }
    
    .action-buttons {
      display: flex;
      gap: 1rem;
      justify-content: center;
      flex-wrap: wrap;
    }
    
    .btn {
      padding: 12px 30px;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s ease;
      border: none;
      cursor: pointer;
    }
    
    .btn-primary {
      background: linear-gradient(135deg, #0080FF 0%, #00c6ff 100%);
      color: white;
    }
    
    .btn-primary:hover {
      background: linear-gradient(135deg, #0066cc 0%, #00a8cc 100%);
      transform: translateY(-2px);
      box-shadow: 0 10px 20px rgba(0, 128, 255, 0.3);
    }
    
    .btn-secondary {
      background: #6c757d;
      color: white;
    }
    
    .btn-secondary:hover {
      background: #5a6268;
      transform: translateY(-2px);
      box-shadow: 0 10px 20px rgba(108, 117, 125, 0.3);
    }
    
    .footer-minimal {
      padding: 2rem;
      text-align: center;
      color: rgba(255, 255, 255, 0.8);
      background: rgba(0, 0, 0, 0.2);
    }
    
    .footer-minimal a {
      color: white;
      text-decoration: none;
      font-weight: 500;
    }
    
    .footer-minimal a:hover {
      text-decoration: underline;
    }
  </style>
</head>
<body>
  <div class="error-container">
    <div class="error-box">
      <div class="error-icon">
        <i class='bx bx-shield-x'></i>
      </div>
      
      <h1 class="error-title">Quyền Truy Cập Bị Từ Chối</h1>
      
      <p class="error-message">
        Xin lỗi, bạn không có quyền truy cập vào trang này. Vui lòng liên hệ quản trị viên nếu bạn cần quyền truy cập.
      </p>
      
      <div class="error-details">
        <p><strong>Lỗi:</strong> Access Denied (403)</p>
        <p><strong>Thông báo:</strong> Tài khoản của bạn không có quyền sử dụng chức năng này</p>
        <p><strong>Giải pháp:</strong> Liên hệ quản trị viên để được cấp quyền truy cập phù hợp</p>
      </div>
      
      <div class="action-buttons">
        <a href="${pageContext.request.contextPath}/dashboard.jsp" class="btn btn-primary">
          <i class='bx bxs-dashboard'></i>
          <span>Trở về Trang Chủ</span>
        </a>
        <a href="javascript:history.back()" class="btn btn-secondary">
          <i class='bx bx-arrow-back'></i>
          <span>Quay Lại</span>
        </a>
      </div>
    </div>
  </div>
  
  <div class="footer-minimal">
    <p>&copy; 2024 LiteFlow. Tất cả quyền được bảo lưu.</p>
    <p>
      <a href="${pageContext.request.contextPath}">Trang chủ</a> | 
      <a href="${pageContext.request.contextPath}/login">Đăng nhập</a> | 
      <a href="#">Liên hệ hỗ trợ</a>
    </p>
  </div>
</body>
</html>

