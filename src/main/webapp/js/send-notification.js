/**
 * Send Notification Script
 * Handles sending notifications from admin to employees
 */

(function() {
    'use strict';

    // Initialize when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        initSendNotification();
    });

    function initSendNotification() {
        const sendIcon = document.getElementById('send-notification-icon');
        if (!sendIcon) {
            console.warn('Send notification icon not found');
            return;
        }

        // Add click event to open modal
        sendIcon.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            openNotificationModal();
        });
    }

    function openNotificationModal() {
        // Create modal HTML
        const modalHTML = `
            <div id="notification-modal" class="notification-modal-overlay">
                <div class="notification-modal">
                    <div class="notification-modal-header">
                        <h2>Gửi thông báo đến nhân viên</h2>
                        <button class="notification-modal-close" onclick="closeNotificationModal()">
                            <i class='bx bx-x'></i>
                        </button>
                    </div>
                    <div class="notification-modal-body">
                        <form id="notification-form">
                            <div class="form-group">
                                <label for="notification-title">Tiêu đề <span class="required">*</span></label>
                                <input type="text" id="notification-title" class="form-control"
                                       placeholder="Nhập tiêu đề thông báo" required maxlength="200">
                            </div>

                            <div class="form-group">
                                <label for="notification-message">Nội dung <span class="required">*</span></label>
                                <textarea id="notification-message" class="form-control" rows="6"
                                          placeholder="Nhập nội dung thông báo" required maxlength="1000"></textarea>
                                <small class="form-text">Tối đa 1000 ký tự</small>
                            </div>

                            <div class="form-group">
                                <label for="notification-priority">Mức độ ưu tiên</label>
                                <select id="notification-priority" class="form-control">
                                    <option value="LOW">Thấp</option>
                                    <option value="MEDIUM" selected>Trung bình</option>
                                    <option value="HIGH">Cao</option>
                                    <option value="CRITICAL">Khẩn cấp</option>
                                </select>
                            </div>

                            <div class="notification-modal-actions">
                                <button type="button" class="btn btn-secondary" onclick="closeNotificationModal()">
                                    Hủy
                                </button>
                                <button type="submit" class="btn btn-primary">
                                    <i class='bx bx-send'></i> Gửi thông báo
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        `;

        // Add modal to body
        document.body.insertAdjacentHTML('beforeend', modalHTML);

        // Add form submit handler
        const form = document.getElementById('notification-form');
        form.addEventListener('submit', handleNotificationSubmit);

        // Add escape key handler
        document.addEventListener('keydown', handleEscapeKey);

        // Focus on title input
        setTimeout(() => {
            document.getElementById('notification-title').focus();
        }, 100);
    }

    function handleNotificationSubmit(e) {
        e.preventDefault();

        const title = document.getElementById('notification-title').value.trim();
        const message = document.getElementById('notification-message').value.trim();
        const priority = document.getElementById('notification-priority').value;

        if (!title || !message) {
            showNotificationError('Vui lòng điền đầy đủ tiêu đề và nội dung');
            return;
        }

        // Show loading state
        const submitBtn = e.target.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="bx bx-loader-alt bx-spin"></i> Đang gửi...';

        // Send notification
        sendNotification(title, message, priority)
            .then(response => {
                if (response.success) {
                    showNotificationSuccess('Thông báo đã được gửi thành công!');
                    setTimeout(() => {
                        closeNotificationModal();
                    }, 1500);
                } else {
                    showNotificationError(response.error || 'Không thể gửi thông báo');
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                }
            })
            .catch(error => {
                console.error('Error sending notification:', error);
                showNotificationError('Lỗi kết nối. Vui lòng thử lại.');
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            });
    }

    async function sendNotification(title, message, priority) {
        const contextPath = document.querySelector('meta[name="contextPath"]')?.content || '';
        const url = contextPath + '/api/send-notification';

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                title: title,
                message: message,
                priority: priority
            })
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        return await response.json();
    }

    function showNotificationSuccess(message) {
        showNotificationToast(message, 'success');
    }

    function showNotificationError(message) {
        showNotificationToast(message, 'error');
    }

    function showNotificationToast(message, type) {
        // Remove existing toast
        const existingToast = document.querySelector('.notification-toast');
        if (existingToast) {
            existingToast.remove();
        }

        const icon = type === 'success' ? 'bx-check-circle' : 'bx-error-circle';
        const bgColor = type === 'success' ? '#10b981' : '#ef4444';

        const toast = document.createElement('div');
        toast.className = 'notification-toast';
        toast.innerHTML = `
            <i class='bx ${icon}'></i>
            <span>${message}</span>
        `;
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${bgColor};
            color: white;
            padding: 12px 24px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: flex;
            align-items: center;
            gap: 8px;
            z-index: 10001;
            animation: slideInRight 0.3s ease;
            font-size: 14px;
            font-weight: 500;
        `;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    function handleEscapeKey(e) {
        if (e.key === 'Escape') {
            closeNotificationModal();
        }
    }

    // Export to global scope
    window.closeNotificationModal = function() {
        const modal = document.getElementById('notification-modal');
        if (modal) {
            modal.style.animation = 'fadeOut 0.2s ease';
            setTimeout(() => {
                modal.remove();
                document.removeEventListener('keydown', handleEscapeKey);
            }, 200);
        }
    };

    // Add CSS animations
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeIn {
            from {
                opacity: 0;
            }
            to {
                opacity: 1;
            }
        }

        @keyframes fadeOut {
            from {
                opacity: 1;
            }
            to {
                opacity: 0;
            }
        }

        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }

        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }

        @keyframes slideInDown {
            from {
                transform: translateY(-20px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }

        .notification-modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10000;
            animation: fadeIn 0.2s ease;
        }

        .notification-modal {
            background: white;
            border-radius: 12px;
            width: 90%;
            max-width: 600px;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            animation: slideInDown 0.3s ease;
        }

        .notification-modal-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 24px;
            border-bottom: 1px solid #e5e7eb;
        }

        .notification-modal-header h2 {
            margin: 0;
            font-size: 20px;
            font-weight: 600;
            color: #1f2937;
        }

        .notification-modal-close {
            background: none;
            border: none;
            font-size: 24px;
            color: #6b7280;
            cursor: pointer;
            padding: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 6px;
            transition: all 0.2s;
        }

        .notification-modal-close:hover {
            background: #f3f4f6;
            color: #1f2937;
        }

        .notification-modal-body {
            padding: 24px;
        }

        .notification-modal .form-group {
            margin-bottom: 20px;
        }

        .notification-modal .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #374151;
            font-size: 14px;
        }

        .notification-modal .form-group .required {
            color: #ef4444;
        }

        .notification-modal .form-control {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 14px;
            font-family: inherit;
            transition: all 0.2s;
        }

        .notification-modal .form-control:focus {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .notification-modal textarea.form-control {
            resize: vertical;
            min-height: 120px;
        }

        .notification-modal .form-text {
            display: block;
            margin-top: 4px;
            font-size: 12px;
            color: #6b7280;
        }

        .notification-modal-actions {
            display: flex;
            gap: 12px;
            justify-content: flex-end;
            margin-top: 24px;
            padding-top: 20px;
            border-top: 1px solid #e5e7eb;
        }

        .notification-modal .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }

        .notification-modal .btn-secondary {
            background: #f3f4f6;
            color: #374151;
        }

        .notification-modal .btn-secondary:hover {
            background: #e5e7eb;
        }

        .notification-modal .btn-primary {
            background: #3b82f6;
            color: white;
        }

        .notification-modal .btn-primary:hover {
            background: #2563eb;
        }

        .notification-modal .btn:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        .notification-modal .bx-spin {
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            from {
                transform: rotate(0deg);
            }
            to {
                transform: rotate(360deg);
            }
        }
    `;
    document.head.appendChild(style);

})();
