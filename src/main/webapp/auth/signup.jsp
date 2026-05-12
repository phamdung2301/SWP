<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
    <head>
        <title>LiteFlow - Signup</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- CSS -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/signup.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

        <style>
            /* Thông báo lỗi chung (nhẹ, không chói) */
            .error {
                background: rgba(255, 0, 0, 0.08);
                color: #ffdede;
                margin: 8px 0;
                padding: 6px 10px;
                border-radius: 6px;
            }
            .msg {
                color: #33cc33;
                margin: 8px 0;
            }
            /* Văn bản lỗi cho từng ô: mặc định ẩn, chỉ show khi JS set nội dung */
            .error-text {
                color: #ffb3b3;
                font-size: 0.9em;
                display: none;
                margin-top: 6px;
            }
        </style>
    </head>
    <body>
        <div class="auth-container">

            <!-- Welcome section -->
            <div class="welcome-section">
                <h1 class="typing-text">Sign Up With <br/> LiteFlow!</h1>
                <a class="skip-btn" href="#">Where small shops run smarter</a>
            </div>

            <!-- Signup card -->
            <div class="auth-card signup-card">
                <h2>Signup</h2>
                <p class="subtitle">Just some details to get you in!</p>

                <!-- Thông báo -->
                <c:if test="${not empty error}">
                    <div class="error">${error}</div>
                </c:if>
                <c:if test="${not empty msg}">
                    <div class="msg">${msg}</div>
                </c:if>

                <!-- Form -->
                <form id="signup-form" action="${pageContext.request.contextPath}/register" method="post" novalidate>
                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                    <div class="form-group">
                        <input type="text" id="username" name="username" placeholder="Username" required
                               value="${fn:escapeXml(param.username != null ? param.username : (requestScope.usernameValue != null ? requestScope.usernameValue : ''))}">
                        <!-- Thông báo lỗi cho username (nếu có) -->
                        <span class="error-text" id="username-error">${requestScope.usernameError}</span>
                    </div>

                    <div class="form-group">
                        <input type="email" id="email" name="email" placeholder="Email (.com only)" required
                               value="${fn:escapeXml(param.email != null ? param.email : (requestScope.emailValue != null ? requestScope.emailValue : ''))}">
                        <!-- Thông báo lỗi cho email -->
                        <span class="error-text" id="email-error">${requestScope.emailError}</span>
                    </div>

                    <div class="form-group">
                        <div class="password-container">
                            <input type="password" id="password" name="password" placeholder="Password" required>
                            <button type="button" class="password-toggle" id="passwordToggle">
                                <i class="fas fa-eye" id="passwordIcon"></i>
                            </button>
                        </div>
                        <!-- Thông báo lỗi cho password -->
                        <span class="error-text" id="password-error">${requestScope.passwordError}</span>
                    </div>

                    <div class="form-group">
                        <div class="password-container">
                            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm Password" required>
                            <button type="button" class="password-toggle" id="confirmPasswordToggle">
                                <i class="fas fa-eye" id="confirmPasswordIcon"></i>
                            </button>
                        </div>
                        <!-- Thông báo lỗi cho confirm password -->
                        <span class="error-text" id="confirm-error">${requestScope.confirmError}</span>
                    </div>

                    <button type="submit" class="btn-verify">Create Account</button>
                </form>

                <div class="divider">Or</div>
                <div class="social-login">
                    <a href="${pageContext.request.contextPath}/auth/google" class="google-btn">
                        <i class="fab fa-google"></i> Sign up with Google
                    </a>
                </div>

                <div class="alt-links">
                    Already registered? <a href="${pageContext.request.contextPath}/login">Login</a>
                </div>
            </div>
        </div>

        <script>
            // Frontend validation: hiện lỗi trước từng ô và focus vào ô lỗi đầu tiên
            document.getElementById("signup-form").addEventListener("submit", function (e) {
                let valid = true;
                let firstInvalid = null;

                const usernameEl = document.getElementById("username");
                const emailEl = document.getElementById("email");
                const passEl = document.getElementById("password");
                const confirmEl = document.getElementById("confirmPassword");

                const usernameError = document.getElementById("username-error");
                const emailError = document.getElementById("email-error");
                const passError = document.getElementById("password-error");
                const confirmError = document.getElementById("confirm-error");

                // Reset messages (ẩn span nếu rỗng)
                usernameError.innerHTML = ""; usernameError.style.display = 'none';
                emailError.innerHTML = ""; emailError.style.display = 'none';
                passError.innerHTML = ""; passError.style.display = 'none';
                confirmError.innerHTML = ""; confirmError.style.display = 'none';

                // Trim values
                const username = usernameEl.value.trim();
                const email = emailEl.value.trim();
                const pass = passEl.value;
                const confirm = confirmEl.value;

                // Username required
                if (!username) {
                    usernameError.innerHTML = '<i class="err-icon fas fa-exclamation-circle"></i>' + 'Vui lòng nhập tên đăng nhập.';
                    usernameError.style.display = 'block';
                    valid = false;
                    if (!firstInvalid) firstInvalid = usernameEl;
                }

                // Email phải có '@' và kết thúc bằng .com
                if (!email.includes('@') || !email.toLowerCase().endsWith('.com')) {
                    emailError.innerHTML = '<i class="err-icon fas fa-exclamation-circle"></i>' + 'Email phải có ký tự @ và kết thúc bằng .com';
                    emailError.style.display = 'block';
                    valid = false;
                    if (!firstInvalid) firstInvalid = emailEl;
                } else {
                    // Nếu muốn regex chặt hơn, vẫn giữ kiểm tra regex
                    const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.com$/;
                    if (!emailRegex.test(email)) {
                        emailError.innerHTML = '<i class="err-icon fas fa-exclamation-circle"></i>' + 'Email không hợp lệ';
                        emailError.style.display = 'block';
                        valid = false;
                        if (!firstInvalid) firstInvalid = emailEl;
                    }
                }

                // Password quy tắc: tối thiểu 8 ký tự, 1 chữ hoa, 1 số
                const passRegex = /^(?=.*[A-Z])(?=.*\d).{8,}$/;
                if (!passRegex.test(pass)) {
                    passError.innerHTML = '<i class="err-icon fas fa-exclamation-circle"></i>' + 'Mật khẩu tối thiểu 8 ký tự, 1 chữ hoa và 1 số.';
                    passError.style.display = 'block';
                    valid = false;
                    if (!firstInvalid) firstInvalid = passEl;
                }

                // Confirm phải khớp
                if (pass !== confirm) {
                    confirmError.innerHTML = '<i class="err-icon fas fa-exclamation-circle"></i>' + 'Mật khẩu xác nhận không khớp.';
                    confirmError.style.display = 'block';
                    valid = false;
                    if (!firstInvalid) firstInvalid = confirmEl;
                }

                if (!valid) {
                    e.preventDefault();
                    // Thêm class để CSS hiển thị viền đỏ nhẹ cho các input invalid
                    this.classList.add('was-validated');
                    if (firstInvalid) {
                        firstInvalid.focus();
                    }
                } else {
                    // nếu valid, remove was-validated nếu có
                    this.classList.remove('was-validated');
                }
            });

            // Password toggle functionality for password field
            const passwordInput = document.getElementById('password');
            const passwordToggle = document.getElementById('passwordToggle');
            const passwordIcon = document.getElementById('passwordIcon');

            passwordToggle.addEventListener('click', function() {
                if (passwordInput.type === 'password') {
                    passwordInput.type = 'text';
                    passwordIcon.classList.remove('fa-eye');
                    passwordIcon.classList.add('fa-eye-slash');
                } else {
                    passwordInput.type = 'password';
                    passwordIcon.classList.remove('fa-eye-slash');
                    passwordIcon.classList.add('fa-eye');
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

            // Realtime validation: oninput / onblur handlers
            function showError(spanEl, message) {
                if (!message) {
                    spanEl.innerHTML = '';
                    spanEl.style.display = 'none';
                    return;
                }
                spanEl.innerHTML = '<i class="err-icon fas fa-exclamation-circle"></i>' + message;
                spanEl.style.display = 'block';
            }

            // Email realtime
            emailEl.addEventListener('input', function() {
                const v = emailEl.value.trim();
                if (!v) { showError(emailError, ''); return; }
                if (!v.includes('@') || !v.toLowerCase().endsWith('.com')) {
                    showError(emailError, 'Email phải có ký tự @ và kết thúc bằng .com');
                } else {
                    const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.com$/;
                    if (!emailRegex.test(v)) showError(emailError, 'Email không hợp lệ');
                    else showError(emailError, '');
                }
            });

            // Password realtime (min rule)
            passEl.addEventListener('input', function() {
                const v = passEl.value;
                if (!v) { showError(passError, ''); return; }
                const passRegex = /^(?=.*[A-Z])(?=.*\d).{8,}$/;
                if (!passRegex.test(v)) showError(passError, 'Mật khẩu tối thiểu 8 ký tự, 1 chữ hoa và 1 số.');
                else showError(passError, '');
            });

            // Confirm realtime: check equality with password
            confirmEl.addEventListener('input', function() {
                const v = confirmEl.value;
                if (!v) { showError(confirmError, ''); return; }
                if (v !== passEl.value) showError(confirmError, 'Mật khẩu xác nhận không khớp.');
                else showError(confirmError, '');
            });
        </script>
    </body>
</html>
