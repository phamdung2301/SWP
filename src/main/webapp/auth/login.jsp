<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    // Đảm bảo CSRF token được tạo nếu chưa có (khi JSP được load trực tiếp không qua servlet)
    // Sử dụng biến implicit 'session' của JSP, không cần khai báo lại
    if (request.getAttribute("csrfToken") == null) {
        // Đảm bảo session tồn tại
        if (session == null) {
            session = request.getSession(true);
        }
        String csrfToken = (String) session.getAttribute("csrfToken");
        if (csrfToken == null || csrfToken.isEmpty()) {
            csrfToken = java.util.UUID.randomUUID().toString();
            session.setAttribute("csrfToken", csrfToken);
        }
        request.setAttribute("csrfToken", csrfToken);
    }
%>
<html>
    <head>
        <title>LiteFlow - Login</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <!-- CSS -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

        <style>
            .sr-only {
                position: absolute;
                width: 1px;
                height: 1px;
                padding: 0;
                margin: -1px;
                overflow: hidden;
                clip: rect(0,0,0,0);
                white-space: nowrap;
                border: 0;
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
        </style>
    </head>
    <body>
        <div class="auth-container">

            <!-- Welcome -->
            <div class="welcome-section">
                <h1 class="typing-text">Welcome Back <br/> LiteFlow!</h1>
                <a class="skip-btn" href="#">Where small shops run smarter</a>
            </div>

            <!-- Card -->
            <div class="auth-card login-card">
                <h2>Login</h2>
                <p class="subtitle">Glad you're back!</p>

                <!-- Thông báo -->
                <c:if test="${not empty error}">
                    <div class="error">${error}</div>
                </c:if>
                <c:if test="${not empty msg}">
                    <div class="msg">${msg}</div>
                </c:if>

                <!-- Form -->
                <form id="login-form" action="${pageContext.request.contextPath}/login" method="post">
                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>

                    <label for="email" class="sr-only">Email</label>
                    <input type="email" id="email" name="id" placeholder="Email (.com only)" required autofocus/>

                    <label for="password" class="sr-only">Password</label>
                    <div class="password-container">
                        <input type="password" id="password" name="password" placeholder="Password" required autocomplete="off"/>
                        <button type="button" class="password-toggle" id="passwordToggle">
                            <i class="fas fa-eye" id="passwordIcon"></i>
                        </button>
                    </div>

                    <div class="options">
                        <label><input type="checkbox" id="rememberChk" name="remember"/> Remember me</label>
                        <a href="${pageContext.request.contextPath}/auth/forgot">Forgot password?</a>
                    </div>

                    <button type="submit" class="btn-login">Login</button>
                </form>

                <script>
                    // LƯU Ý: Lưu email + password vào localStorage chỉ khi user bật "Remember me".
                    // Đây là hành vi client-side (chỉ lưu trên máy user). Cân nhắc rủi ro bảo mật.
                    (function() {
                        const KEY = 'LITEFLOW_REMEMBER_LOCAL';
                        const MAX_AGE_MS = 24 * 3600 * 1000; // 24 giờ

                        const emailInput = document.getElementById('email');
                        const passInput = document.getElementById('password');
                        const rememberChk = document.getElementById('rememberChk');
                        const form = document.getElementById('login-form');

                        // Hàm đọc cookie
                        function getCookie(name) {
                            const value = `; ${document.cookie}`;
                            const parts = value.split(`; ${name}=`);
                            if (parts.length === 2) return parts.pop().split(';').shift();
                            return null;
                        }

                        // Hàm xóa cookie
                        function clearCookie(name) {
                            document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
                        }

                        // Khi trang load, kiểm tra localStorage và cookie để autofill
                        try {
                            // Ưu tiên localStorage trước
                            const raw = localStorage.getItem(KEY);
                            if (raw) {
                                const obj = JSON.parse(raw);
                                if (obj && obj.ts && (Date.now() - obj.ts) < MAX_AGE_MS) {
                                    if (obj.email) emailInput.value = obj.email;
                                    if (obj.password) passInput.value = obj.password;
                                    rememberChk.checked = true;
                                } else {
                                    localStorage.removeItem(KEY);
                                }
                            } else {
                                // Nếu không có localStorage, kiểm tra cookie LITEFLOW_REMEMBER
                                const rememberedEmail = getCookie('LITEFLOW_REMEMBER');
                                if (rememberedEmail) {
                                    emailInput.value = decodeURIComponent(rememberedEmail);
                                    // Tìm password trong localStorage nếu có
                                    const storedData = localStorage.getItem(KEY);
                                    if (storedData) {
                                        try {
                                            const obj = JSON.parse(storedData);
                                            if (obj && obj.password && obj.email === emailInput.value) {
                                                passInput.value = obj.password;
                                            }
                                        } catch (e) {}
                                    }
                                    rememberChk.checked = true;
                                }
                            }
                        } catch (e) {
                            console.error('Error loading remembered credentials:', e);
                        }

                        // Khi submit form: nếu checked -> lưu; nếu không -> xóa
                        form.addEventListener('submit', function() {
                            try {
                                if (rememberChk.checked) {
                                    const v = { email: emailInput.value, password: passInput.value, ts: Date.now() };
                                    localStorage.setItem(KEY, JSON.stringify(v));
                                } else {
                                    localStorage.removeItem(KEY);
                                    // Xóa cookie remember me nếu user uncheck
                                    clearCookie('LITEFLOW_REMEMBER');
                                }
                            } catch (e) {
                                console.error('Error saving credentials:', e);
                            }
                        });

                        // Nếu user uncheck, xóa ngay
                        rememberChk.addEventListener('change', function() {
                            if (!this.checked) {
                                try { 
                                    localStorage.removeItem(KEY); 
                                    clearCookie('LITEFLOW_REMEMBER');
                                } catch(e) {
                                    console.error('Error clearing credentials:', e);
                                }
                            }
                        });

                        // Password toggle functionality
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
                    })();
                </script>

                <div class="divider">Or</div>
                <div class="social-login">
                    <a href="${pageContext.request.contextPath}/auth/google" class="google-btn">
                        <i class="fab fa-google"></i> Login with Google
                    </a>
                </div>

                <div class="alt-links">
                    Don’t have an account? <a href="${pageContext.request.contextPath}/register">Sign up</a>
                </div>

                <div class="footer-links">
                    <a href="${pageContext.request.contextPath}/auth/terms.jsp" target="_blank">Terms & Conditions</a>
                    <a href="${pageContext.request.contextPath}/auth/support.jsp" target="_blank">Support</a>
                    <a href="${pageContext.request.contextPath}/auth/customer-care.jsp" target="_blank">Customer Care</a>
                </div>
            </div>
        </div>
    </body>
</html>
