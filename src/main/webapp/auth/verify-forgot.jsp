<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
    <head>
        <title>LiteFlow - Verify Forgot</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/otp.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
        <style>
            .modal {
                display: none;
                position: fixed;
                z-index: 1000;
                left: 0;
                top: 0;
                width: 100%;
                height: 100%;
                background: rgba(0,0,0,0.5);
            }
            .modal-content {
                background: #fff;
                margin: 10% auto;
                padding: 20px;
                width: 90%;
                max-width: 400px;
                border-radius: 8px;
                text-align: center;
            }
            .close-btn {
                float: right;
                font-size: 20px;
                cursor: pointer;
            }
        </style>
    </head>
    <body>
        <div class="auth-container">
            <!-- Left -->
            <div class="welcome-section">
                <h1 class="typing-text">Verify <br/> Your OTP</h1>
                <a class="skip-btn" href="#">Where small shops run smarter</a>
            </div>

            <!-- Right -->
            <div class="auth-card otp-card">
                <h2>OTP Verification (Forgot)</h2>
                <p class="subtitle">Enter the 6-digit code we sent to your email</p>
                
                <!-- Messages -->
                <div id="ajax-otp-message">
                    <c:if test="${not empty error}">
                        <!-- Hiển thị lỗi ngay trên dòng OTP -->
                        <div class="error">${error}</div>
                    </c:if>
                    <c:if test="${not empty msg}">
                        <div class="msg">${msg}</div>
                    </c:if>
                </div>
                <!-- keep resend email available for AJAX; prefer session, fallback to query param 'email' -->
                <input type="hidden" id="resendEmail" value="${sessionScope.otpEmail != null ? sessionScope.otpEmail : param.email}">

                <!-- Thông báo đặc biệt cho admin (chỉ hiện khi email trong session là admin@liteflow.com hoặc pendingUser là admin) -->
                <%
                    // Only read otpEmail from session for admin DEV visibility.
                    String adminEmail = (String) session.getAttribute("otpEmail");
                %>
                <% if (adminEmail != null && "admin@liteflow.com".equalsIgnoreCase(adminEmail)) { %>
                    <div class="admin-info">
                        <strong><i class="fas fa-info-circle"></i> Admin DEV Mode:</strong><br/>
                        Use OTP: <strong>000000</strong>
                    </div>
                <% } %>

                <!-- OTP form -->
                <form action="${pageContext.request.contextPath}/verify-otp" method="post" id="otp-form">
                    <div class="otp-inputs">
                        <input type="text" name="otp1" maxlength="1">
                        <input type="text" name="otp2" maxlength="1">
                        <input type="text" name="otp3" maxlength="1">
                        <input type="text" name="otp4" maxlength="1">
                        <input type="text" name="otp5" maxlength="1">
                        <input type="text" name="otp6" maxlength="1">
                    </div>
                    <button type="submit" class="btn-verify" id="btnVerify">Verify OTP</button>
                </form>

                <div class="resend-section" style="margin-top:12px;">
                    Didn't receive a code?
                    <a href="javascript:void(0)" id="openResendModal">Resend OTP</a>
                    <span id="resendCountdown" style="margin-left:8px;color:#bbb;"></span>
                </div>
                <div style="margin-top:16px;text-align:center;display:flex;flex-direction:column;gap:8px;align-items:center;">
                    <a href="${pageContext.request.contextPath}/auth/login.jsp" class="btn-plain">Quay lại Login</a>
                </div>
            </div>
        </div>

        <script>
            // Auto-advance & digit-only handling for OTP inputs (align with verify-signup)
            const otpInputs = document.querySelectorAll('.otp-inputs input');
            otpInputs.forEach((input, idx, arr) => {
                input.addEventListener('input', (e) => {
                    // Enforce digits only
                    input.value = input.value.replace(/\D/g, '');
                    const val = input.value;
                    // If user pasted full OTP into a single box, distribute
                    if (val.length > 1) {
                        const chars = val.replace(/\D/g,'').split('').slice(0, arr.length - idx);
                        for (let i = 0; i < chars.length; i++) {
                            if (arr[idx + i]) arr[idx + i].value = chars[i];
                        }
                        const nextIndex = Math.min(idx + chars.length, arr.length - 1);
                        arr[nextIndex].focus();
                        return;
                    }
                    if (val.length === 1 && idx < arr.length - 1) arr[idx + 1].focus();
                });
                input.addEventListener('keydown', (e) => {
                    // Allow control keys
                    const allowed = ['Backspace','Delete','ArrowLeft','ArrowRight','Tab'];
                    if (allowed.includes(e.key)) return;
                    // Only digits
                    if (!/^[0-9]$/.test(e.key)) e.preventDefault();
                    if (e.key === 'Backspace' && !input.value && idx > 0) {
                        arr[idx - 1].focus();
                    }
                });
                // optional: select on focus for quick overwrite
                input.addEventListener('focus', () => input.select());
                // Prevent non-digit paste, but still allow distributing digits
                input.addEventListener('paste', (ev) => {
                    ev.preventDefault();
                    const text = (ev.clipboardData || window.clipboardData).getData('text');
                    const digits = (text || '').replace(/\D/g,'');
                    if (!digits) return;
                    const chars = digits.split('');
                    for (let i = 0; i < chars.length && (idx + i) < arr.length; i++) {
                        arr[idx + i].value = chars[i];
                    }
                    const nextIndex = Math.min(idx + chars.length, arr.length - 1);
                    arr[nextIndex].focus();
                });
            });

            // Inline AJAX resend (no popup) + cooldown (align with verify-signup)
            const resendLink = document.getElementById('openResendModal');
            const RESEND_KEY = 'liteflow_resend_until_forgot';
            const ajaxMsgWrap = document.getElementById('ajax-otp-message');
            const countdownLabel = document.getElementById('resendCountdown');

            function setResendDisabled(seconds) {
                const until = Date.now() + seconds * 1000;
                localStorage.setItem(RESEND_KEY, String(until));
                updateResendState();
            }

            function updateResendState() {
                const txt = localStorage.getItem(RESEND_KEY);
                if (!txt) return;
                const until = Number(txt);
                const now = Date.now();
                const rem = Math.ceil((until - now) / 1000);
                if (rem > 0) {
                    resendLink.textContent = 'Resend OTP';
                    // During cooldown, visually disable; keep pointer events for click handler guard
                    resendLink.style.opacity = '0.5';
                    if (countdownLabel) countdownLabel.textContent = '(' + rem + 's)';
                    setTimeout(updateResendState, 1000);
                } else {
                    localStorage.removeItem(RESEND_KEY);
                    resendLink.textContent = 'Resend OTP';
                    resendLink.style.opacity = '1';
                    if (countdownLabel) countdownLabel.textContent = '';
                }
            }

            if (resendLink) {
                resendLink.addEventListener('click', function(e) {
                    e.preventDefault();
                    // prevent resend during cooldown
                    const txt = localStorage.getItem(RESEND_KEY);
                    if (txt && Number(txt) > Date.now()) return;
                    const email = (document.getElementById('resendEmail') || {}).value || '';
                    if (!email) {
                        ajaxMsgWrap.innerHTML = '';
                        const d = document.createElement('div'); d.className = 'error'; d.textContent = 'Email không khả dụng. Vui lòng quay lại và thử lại.';
                        ajaxMsgWrap.appendChild(d);
                        return;
                    }
                    // UI feedback & immediate cooldown (optimistic)
                    resendLink.style.opacity = '0.5';
                    if (countdownLabel) countdownLabel.textContent = '(60s)';
                    setResendDisabled(60);

                    const fd = new FormData();
                    fd.append('email', email);
                    fd.append('ajax', '1');
                    fetch('${pageContext.request.contextPath}/send-otp', {
                        method: 'POST',
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        credentials: 'same-origin',
                        body: fd
                    }).then(r => {
                        const ct = r.headers.get('content-type') || '';
                        if (!r.ok) return r.text().then(t => { throw new Error('Network error: ' + r.status + ' - ' + (t ? t.slice(0,200) : '')); });
                        if (ct.includes('application/json')) return r.json();
                        return r.text().then(t => { throw new Error('Unexpected server response: ' + (t ? t.slice(0,200) : '')); });
                    }).then(data => {
                        ajaxMsgWrap.innerHTML = '';
                        if (data && data.success) {
                            const d = document.createElement('div'); d.className = 'msg'; d.textContent = data.msg || 'OTP sent';
                            ajaxMsgWrap.appendChild(d);
                            // cooldown already set; continue counting
                        } else {
                            const d = document.createElement('div'); d.className = 'error'; d.textContent = (data && data.msg) ? data.msg : 'Failed to resend OTP';
                            ajaxMsgWrap.appendChild(d);
                            // rollback cooldown on error
                            localStorage.removeItem(RESEND_KEY);
                            resendLink.style.opacity = '1';
                            if (countdownLabel) countdownLabel.textContent = '';
                        }
                    }).catch(err => {
                        ajaxMsgWrap.innerHTML = '';
                        const d = document.createElement('div'); d.className = 'error'; d.textContent = err.message || 'Lỗi kết nối. Vui lòng thử lại.';
                        ajaxMsgWrap.appendChild(d);
                        // rollback cooldown on error
                        localStorage.removeItem(RESEND_KEY);
                        resendLink.style.opacity = '1';
                        if (countdownLabel) countdownLabel.textContent = '';
                    });
                });
            }

            // If this page is loaded right after sending OTP (sent=1) and no cooldown exists yet, start it.
            (function initFirstCooldown(){
                try {
                    const params = new URLSearchParams(window.location.search);
                    if (params.get('sent') === '1' && !localStorage.getItem(RESEND_KEY)) {
                        setResendDisabled(60);
                    }
                } catch (e) {}
            })();
            updateResendState();

            // Custom form validation: require all 6 digits, show inline error instead of native bubble
            const otpForm = document.getElementById('otp-form');
            if (otpForm) {
                otpForm.addEventListener('submit', function(e) {
                    const inputs = Array.from(document.querySelectorAll('.otp-inputs input'));
                    const firstEmpty = inputs.find(i => !i.value || i.value.trim() === '');
                    if (firstEmpty) {
                        e.preventDefault();
                        ajaxMsgWrap.innerHTML = '';
                        const d = document.createElement('div');
                        d.className = 'error';
                        d.textContent = 'Vui lòng nhập đủ 6 số OTP.';
                        ajaxMsgWrap.appendChild(d);
                        firstEmpty.focus();
                    }
                });
            }
        </script>
    </body>
    </html>
