<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
    <head>
        <title>LiteFlow - Forgot Password</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- CSS chung cho auth layout -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
        <!-- CSS riêng cho forgot -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/forgot.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
        <style>
            .error {
                color: #f44336;
                margin: 10px 0;
            }
            .msg {
                color: #4caf50;
                margin: 10px 0;
            }
        </style>
    </head>
    <body>
        <div class="auth-container">
            <!-- Bên trái -->
            <div class="welcome-section">
                <h1 class="typing-text">Forgot <br/> Password</h1>
                <a class="skip-btn" href="#">Where small shops run smarter</a>
            </div>

            <!-- Bên phải -->
            <div class="auth-card forgot-card">
                <h2>Forgot Password?</h2>
                <p class="subtitle">Enter your email to receive an OTP</p>

                <!-- Hiển thị thông báo từ server -->
                <c:if test="${not empty error}">
                    <div class="error">${error}</div>
                </c:if>
                <c:if test="${not empty msg}">
                    <div class="msg">${msg}</div>
                </c:if>

                <!-- Form Forgot -->
                <form action="${pageContext.request.contextPath}/auth/forgot" method="post" novalidate>
                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                    <div class="form-group">
                        <input type="email" id="email" name="email" placeholder="example@mail.com" required>
                    </div>
                    <button type="submit" class="btn-forgot">Send OTP</button>
                </form>

                <div class="alt-links">
                    Remembered your password? 
                    <a href="${pageContext.request.contextPath}/login">Login</a>
                </div>
            </div>
        </div>
    </body>
</html>
