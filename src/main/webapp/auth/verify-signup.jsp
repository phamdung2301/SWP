<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
    <head>
        <title>LiteFlow - Verify Signup</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/otp.css">
    </head>
    <body>
        <div class="auth-container">
            <div class="welcome-section">
                <h1 class="typing-text">Verify <br/> Your OTP</h1>
                <a class="skip-btn" href="#">Where small shops run smarter</a>
            </div>

            <div class="auth-card otp-card">
                <h2>OTP Verification (Signup)</h2>
                <p class="subtitle">Enter the 6-digit code we sent to your email</p>

                <div id="ajax-otp-message">
                    <c:if test="${not empty error}">
                        <div class="error">${error}</div>
                    </c:if>
                    <c:if test="${not empty msg}">
                        <div class="msg">${msg}</div>
                    </c:if>
                    <c:if test="${not empty success}">
                        <div class="success">${success}</div>
                    </c:if>
                </div>
                <!-- keep resend email available for AJAX; prefer session, fallback to query param 'email' -->
                <input type="hidden" id="resendEmail" value="${sessionScope.otpEmail != null ? sessionScope.otpEmail : param.email}">

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
                    <c:if test="${empty success}">
                        <a href="${pageContext.request.contextPath}/auth/signup.jsp" class="btn-plain">Quay lại trang Signup</a>
                    </c:if>
                    <c:if test="${not empty success}">
                        <a href="${pageContext.request.contextPath}/login" class="btn-plain">Quay lại Login</a>
                    </c:if>
                </div>
            </div>
        </div>

        <script>
            // Inline AJAX resend (no popup) + cooldown
            const resendLink = document.getElementById('openResendModal');
            const RESEND_KEY = 'liteflow_resend_until_signup';
            const ajaxMsgWrap = document.getElementById('ajax-otp-message');
            const countdownLabel = document.getElementById('resendCountdown');
            const verifyBtn = document.getElementById('btnVerify');
            const successBox = document.querySelector('#ajax-otp-message .success');
            const isSuccess = !!successBox; // lock UI when signup already succeeded

            function showLockedError(message) {
                // Ensure error appears under the success banner
                const existing = document.getElementById('success-lock-error');
                const text = message || 'Đăng ký đã thành công. Không thể nhập mã hay gửi lại OTP.';
                if (existing) {
                    existing.textContent = text;
                    return;
                }
                const d = document.createElement('div');
                d.className = 'error';
                d.id = 'success-lock-error';
                d.textContent = text;
                // Insert after success if present; otherwise at top
                if (successBox && successBox.parentNode) {
                    successBox.parentNode.insertBefore(d, successBox.nextSibling);
                } else {
                    ajaxMsgWrap.appendChild(d);
                }
            }

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
                    // If account already signed up successfully, block and notify
                    if (isSuccess) { showLockedError('Đăng ký đã thành công. Không thể gửi lại OTP.'); return; }
                    // prevent resend during cooldown
                    const txt = localStorage.getItem(RESEND_KEY);
                    if (txt && Number(txt) > Date.now()) return;
                    const email = (document.getElementById('resendEmail') || {}).value || '';
                    if (!email) {
                        ajaxMsgWrap.innerHTML = '';
                        const d = document.createElement('div'); d.className = 'error'; d.textContent = 'Email không khả dụng. Vui lòng quay lại trang signup và thử lại.';
                        ajaxMsgWrap.appendChild(d);
                        return;
                    }
                        // UI feedback & immediate cooldown (optimistic)
                        resendLink.style.opacity = '0.5';
                        if (countdownLabel) countdownLabel.textContent = '(60s)';
                        setResendDisabled(60);

                        // build form data
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
                        console.log('send-otp response', r.status, ct);
                        if (!r.ok) return r.text().then(t => { throw new Error('Network error: ' + r.status + ' - ' + (t ? t.slice(0,200) : '')); });
                        if (ct.includes('application/json')) return r.json();
                        return r.text().then(t => { throw new Error('Unexpected server response: ' + (t ? t.slice(0,200) : '')); });
                    }).then(data => {
                        ajaxMsgWrap.innerHTML = '';
                        if (data && data.success) {
                            const d = document.createElement('div'); d.className = 'msg'; d.textContent = data.msg || 'OTP sent';
                            ajaxMsgWrap.appendChild(d);
                                // cooldown đã bắt đầu; chỉ cần tiếp tục đếm
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

            // Auto-advance & digit-only handling for OTP inputs
            const otpInputs = document.querySelectorAll('.otp-inputs input');
            otpInputs.forEach((input, idx, arr) => {
                input.addEventListener('input', (e) => {
                    if (isSuccess) { input.value = ''; showLockedError('Đăng ký đã thành công. Không cần nhập OTP nữa.'); return; }
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
                    if (isSuccess) { e.preventDefault(); showLockedError('Đăng ký đã thành công. Không cần nhập OTP nữa.'); return; }
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
                // Prevent non-digit paste
                input.addEventListener('paste', (ev) => {
                    if (isSuccess) { ev.preventDefault(); showLockedError('Đăng ký đã thành công. Không thể nhập OTP.'); return; }
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

            // Lock UI if success: disable verify button and mark inputs read-only
            if (isSuccess) {
                otpInputs.forEach(i => { i.readOnly = true; i.classList.add('readonly'); });
                if (verifyBtn) { verifyBtn.disabled = true; verifyBtn.style.opacity = '0.6'; verifyBtn.style.cursor = 'not-allowed'; }
                // Also visually dim resend
                if (resendLink) { resendLink.style.opacity = '0.5'; if (countdownLabel) countdownLabel.textContent = ''; }
            }

            // Custom form validation: require all 6 digits, show inline error instead of native bubble
            const otpForm = document.getElementById('otp-form');
            if (otpForm) {
                otpForm.addEventListener('submit', function(e) {
                    if (isSuccess) { e.preventDefault(); return; }
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
