<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
    <head>
        <title>LiteFlow - Verify Login</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/otp.css">
    </head>
    <body>
        <div class="auth-container">
            <div class="welcome-section">
                <h1 class="typing-text">Two-factor </br> Verification</h1>
                <a class="skip-btn" href="#">Where small shops run smarter</a>
            </div>

            <div class="auth-card otp-card">
                <h2>OTP Verification (Login)</h2>
                <p class="subtitle">Enter the 6-digit code to complete login</p>

                <div id="ajax-otp-message">
                    <c:if test="${not empty error}">
                        <div class="error">${error}</div>
                    </c:if>
                    <c:if test="${not empty msg}">
                        <div class="msg">${msg}</div>
                    </c:if>
                </div>

                <!-- keep resend email for AJAX resend (use session otpEmail set by login flow) -->
                <input type="hidden" id="resendEmail" value="${sessionScope.otpEmail}">

                <form action="${pageContext.request.contextPath}/verify-otp" method="post" id="otp-form">
                    <div class="otp-inputs">
                        <input type="text" name="otp1" maxlength="1">
                        <input type="text" name="otp2" maxlength="1">
                        <input type="text" name="otp3" maxlength="1">
                        <input type="text" name="otp4" maxlength="1">
                        <input type="text" name="otp5" maxlength="1">
                        <input type="text" name="otp6" maxlength="1">
                    </div>
                    <button type="submit" class="btn-verify">Verify OTP</button>
                </form>

                <div class="resend-section" style="margin-top: 12px;">
                    Didn't receive a code?
                    <a href="javascript:void(0)" id="openResendModal">Resend OTP</a>
                </div>

                <div style="margin-top:16px;text-align:center;">
                    <a href="${pageContext.request.contextPath}/login" class="btn-plain">Quay lại Login</a>
                </div>
            </div>
        </div>
        <script>
            // Get form element first so it's available in event handlers
            const otpForm = document.getElementById('otp-form');
            
            // Auto-advance & digit-only handling for OTP inputs (same as verify-signup)
            const otpInputs = document.querySelectorAll('.otp-inputs input');
            otpInputs.forEach((input, idx, arr) => {
                input.addEventListener('input', (e) => {
                    // enforce digits only
                    input.value = input.value.replace(/\D/g, '');
                    const val = input.value;
                    // If user pasted multiple chars into this box, distribute digits to next boxes
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
                    // Handle Enter key - submit form if all fields are filled
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        const allFilled = Array.from(arr).every(inp => inp.value && inp.value.trim() !== '');
                        if (allFilled) {
                            otpForm.submit();
                        } else {
                            const firstEmpty = Array.from(arr).find(inp => !inp.value || inp.value.trim() === '');
                            if (firstEmpty) firstEmpty.focus();
                        }
                        return;
                    }
                    
                    // Handle Backspace - improved logic: always move to previous field after deleting
                    if (e.key === 'Backspace') {
                        if (input.value) {
                            // If current field has value, clear it and move to previous field
                            input.value = '';
                            if (idx > 0) {
                                arr[idx - 1].focus();
                            }
                        } else if (idx > 0) {
                            // If current field is empty, move to previous field and clear it
                            arr[idx - 1].value = '';
                            arr[idx - 1].focus();
                        }
                        e.preventDefault(); // Prevent default backspace behavior
                        return;
                    }
                    
                    // Allow navigation and edit keys
                    const allowed = ['Delete','ArrowLeft','ArrowRight','Tab'];
                    if (allowed.includes(e.key)) return;
                    
                    // Only single digit keys
                    if (!/^[0-9]$/.test(e.key)) e.preventDefault();
                });
                input.addEventListener('focus', () => input.select());
                // Handle paste of multi-digit codes, digits only
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

            // Inline AJAX resend (no popup) + cooldown
            const resendLink = document.getElementById('openResendModal');
            const RESEND_KEY = 'liteflow_resend_until';
            const ajaxMsgWrap = document.getElementById('ajax-otp-message');

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
                    resendLink.textContent = `Resend OTP (${rem}s)`;
                    resendLink.style.pointerEvents = 'none';
                    setTimeout(updateResendState, 1000);
                } else {
                    localStorage.removeItem(RESEND_KEY);
                    resendLink.textContent = 'Resend OTP';
                    resendLink.style.pointerEvents = '';
                }
            }

            if (resendLink) {
                resendLink.addEventListener('click', function(e) {
                    e.preventDefault();
                    if (resendLink.style.pointerEvents === 'none') return;
                    const email = (document.getElementById('resendEmail') || {}).value || '';
                    if (!email) {
                        ajaxMsgWrap.innerHTML = '';
                        const d = document.createElement('div'); d.className = 'error'; d.textContent = 'Email không khả dụng. Vui lòng quay lại Login và thử lại.';
                        ajaxMsgWrap.appendChild(d);
                        return;
                    }
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
                            setResendDisabled(60);
                        } else {
                            const d = document.createElement('div'); d.className = 'error'; d.textContent = (data && data.msg) ? data.msg : 'Failed to resend OTP';
                            ajaxMsgWrap.appendChild(d);
                        }
                    }).catch(err => {
                        ajaxMsgWrap.innerHTML = '';
                        const d = document.createElement('div'); d.className = 'error'; d.textContent = err.message || 'Lỗi kết nối. Vui lòng thử lại.';
                        ajaxMsgWrap.appendChild(d);
                    });
                });
            }

            // Custom form validation: require all 6 digits, show inline error instead of native bubble
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

            updateResendState();
        </script>
    </body>
</html>
