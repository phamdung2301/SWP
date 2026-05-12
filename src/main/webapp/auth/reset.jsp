<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
    <head>
        <title>LiteFlow - Reset Password</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- CSS chung -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
        <!-- CSS riêng cho reset (nhưng giữ chung theme với auth.css) -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/reset.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
        <style>
            /* Thông báo đồng nhất với otp.css */
            .error {
                background: #ff4d4f;
                color: #fff;
                padding: 10px 14px;
                border-radius: 8px;
                margin-bottom: 16px;
                font-size: 0.9rem;
                width: 100%;
                box-sizing: border-box;
                max-width: 370px;
            }
            .success, .msg {
                background: #28a745;
                color: #fff;
                padding: 10px 14px;
                border-radius: 8px;
                margin-bottom: 16px;
                font-size: 0.95rem;
                width: 100%;
                box-sizing: border-box;
                max-width: 370px;
            }
            .error-text { color:#ffb3b3; font-size:0.9em; display:none; margin-top:6px; }
            .reset-card form {
                width: 100%;
                max-width: 370px;
                margin: 0 auto;
            }
            .reset-card .password-container,
            .reset-card input[type="password"],
            .reset-card input[type="text"],
            .reset-card .btn-login {
                width: 100%;
                max-width: 370px;
                box-sizing: border-box;
            }
            .reset-card .btn-login {
                display: block;
                margin: 0 auto;
            }
        </style>
    </head>
    <body>
        <div class="auth-container">

            <!-- Bên trái -->
            <div class="welcome-section">
                <h1 class="typing-text">Reset <br/> Password</h1>
                <a class="skip-btn" href="#">Enter OTP & set your new password</a>
            </div>

            <!-- Bên phải -->
            <div class="auth-card reset-card">
                <h2>Reset Password</h2>
                <p class="subtitle">Đặt mật khẩu mới sau khi xác thực OTP</p>

                <!-- Hiển thị lỗi/thành công -->
                <c:if test="${not empty error}">
                    <div class="error">${error}</div>
                </c:if>
                <c:if test="${not empty msg}">
                    <div class="success">${msg}</div>
                </c:if>
                <!-- Hiển thị thông báo OTP đã verify → yêu cầu nhập mật khẩu mới (1 lần) -->
                <c:if test="${not empty sessionScope.resetVerifiedMsg}">
                    <div class="success">${sessionScope.resetVerifiedMsg}</div>
                    <c:remove var="resetVerifiedMsg" scope="session"/>
                </c:if>

                <!-- Form Reset -->
                <form id="reset-form" action="${pageContext.request.contextPath}/auth/reset" method="post" novalidate>
                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                    <div class="password-container">
                        <input type="password" id="newPassword" name="newPassword" placeholder="New Password" required autocomplete="off">
                        <button type="button" class="password-toggle" id="newPasswordToggle">
                            <i class="fas fa-eye" id="newPasswordIcon"></i>
                        </button>
                    </div>
                    <span class="error-text" id="newPassword-error"></span>

                    <div class="password-container">
                        <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm Password" required autocomplete="off">
                        <button type="button" class="password-toggle" id="confirmPasswordToggle">
                            <i class="fas fa-eye" id="confirmPasswordIcon"></i>
                        </button>
                    </div>
                    <span class="error-text" id="confirmPassword-error"></span>

                    <div style="color:#bbb;font-size:12px;margin:8px 0 16px;">
                        Mật khẩu tối thiểu 8 ký tự, có ít nhất 1 chữ hoa và 1 số.
                    </div>
                    <button type="submit" class="btn-login">Reset Password</button>
                </form>

                <div style="margin-top:16px;text-align:center;display:flex;flex-direction:column;gap:8px;align-items:center;">
                    <a href="${pageContext.request.contextPath}/auth/login.jsp" class="btn-plain">Quay lại Login</a>
                </div>
            </div>
        </div>

        <script>
            // Client-side validation giống Signup
            const form = document.getElementById('reset-form');
            const newEl = document.getElementById('newPassword');
            const confirmEl = document.getElementById('confirmPassword');
            const newErr = document.getElementById('newPassword-error');
            const confirmErr = document.getElementById('confirmPassword-error');

            function showErr(el, msg) {
                el.textContent = msg || '';
                el.style.display = msg ? 'block' : 'none';
            }

            const passRegex = /^(?=.*[A-Z])(?=.*\d).{8,}$/;

            form.addEventListener('submit', function(e){
                let ok = true;
                showErr(newErr, '');
                showErr(confirmErr, '');
                const p1 = newEl.value;
                const p2 = confirmEl.value;
                if (!passRegex.test(p1)) {
                    showErr(newErr, 'Mật khẩu tối thiểu 8 ký tự, 1 chữ hoa và 1 số.');
                    ok = false;
                }
                if (p1 !== p2) {
                    showErr(confirmErr, 'Mật khẩu xác nhận không khớp.');
                    ok = false;
                }
                if (!ok) e.preventDefault();
            });

            newEl.addEventListener('input', function(){
                const v = newEl.value;
                if (!v) { showErr(newErr, ''); return; }
                if (!passRegex.test(v)) showErr(newErr, 'Mật khẩu tối thiểu 8 ký tự, 1 chữ hoa và 1 số.');
                else showErr(newErr, '');
            });
            confirmEl.addEventListener('input', function(){
                const v = confirmEl.value;
                if (!v) { showErr(confirmErr, ''); return; }
                if (v !== newEl.value) showErr(confirmErr, 'Mật khẩu xác nhận không khớp.');
                else showErr(confirmErr, '');
            });
            // Password toggle functionality for new password field
            const newPasswordInput = document.getElementById('newPassword');
            const newPasswordToggle = document.getElementById('newPasswordToggle');
            const newPasswordIcon = document.getElementById('newPasswordIcon');

            newPasswordToggle.addEventListener('click', function() {
                if (newPasswordInput.type === 'password') {
                    newPasswordInput.type = 'text';
                    newPasswordIcon.classList.remove('fa-eye');
                    newPasswordIcon.classList.add('fa-eye-slash');
                } else {
                    newPasswordInput.type = 'password';
                    newPasswordIcon.classList.remove('fa-eye-slash');
                    newPasswordIcon.classList.add('fa-eye');
                }
            });

            // Password toggle functionality for confirm password field
            const confirmPasswordInput = document.getElementById('confirmPassword');
            const confirmPasswordToggle = document.getElementById('confirmPasswordToggle');
            const confirmPasswordIcon = document.getElementById('confirmPasswordIcon');

            confirmPasswordToggle.addEventListener('click', function() {
                if (confirmPasswordInput.type === 'password') {
                    confirmPasswordInput.type = 'text';
                    confirmPasswordIcon.classList.remove('fa-eye');
                    confirmPasswordIcon.classList.add('fa-eye-slash');
                } else {
                    confirmPasswordInput.type = 'password';
                    confirmPasswordIcon.classList.remove('fa-eye-slash');
                    confirmPasswordIcon.classList.add('fa-eye');
                }
            });
        </script>
    </body>
</html>
