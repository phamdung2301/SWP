/**
 * Notice Board Script
 * Handles loading and displaying notices on employee dashboard
 */

(function() {
    'use strict';

    let notices = [];

    // Initialize when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        initNoticeBoard();
    });

    function initNoticeBoard() {
        const noticeList = document.getElementById('noticeList');
        if (!noticeList) {
            console.log('Notice list container not found');
            return;
        }

        // Load notices
        loadNotices();

        // Refresh every 5 minutes
        setInterval(loadNotices, 5 * 60 * 1000);
    }

    async function loadNotices() {
        try {
            const contextPath = document.querySelector('meta[name="contextPath"]')?.content || '';
            const response = await fetch(contextPath + '/api/notices/list?limit=10');

            if (!response.ok) {
                throw new Error('Failed to fetch notices');
            }

            const data = await response.json();

            if (data.success) {
                notices = data.notices || [];
                renderNotices();
            } else {
                console.error('Error loading notices:', data.error);
                showErrorMessage('Không thể tải thông báo');
            }

        } catch (error) {
            console.error('Error loading notices:', error);
            showErrorMessage('Lỗi kết nối. Vui lòng thử lại.');
        }
    }

    function renderNotices() {
        const noticeList = document.getElementById('noticeList');
        if (!noticeList) return;

        if (notices.length === 0) {
            noticeList.innerHTML = `
                <div class="notice-empty">
                    <i class='bx bx-info-circle'></i>
                    <p>Chưa có thông báo mới</p>
                </div>
            `;
            return;
        }

        noticeList.innerHTML = '';

        notices.forEach(notice => {
            const noticeElement = createNoticeElement(notice);
            noticeList.appendChild(noticeElement);
        });
    }

    function createNoticeElement(notice) {
        const div = document.createElement('div');
        div.className = `notice-item ${notice.noticeType} ${notice.isRead ? 'read' : 'unread'}`;
        div.dataset.noticeId = notice.noticeID;

        div.innerHTML = `
            <div class="notice-header">
                <span class="notice-badge ${notice.noticeType}">${notice.noticeTypeLabel}</span>
                <span class="notice-date">${notice.publishedDate}</span>
            </div>
            <div class="notice-title">${escapeHtml(notice.title)}</div>
            <div class="notice-content">${escapeHtml(notice.content)}</div>
            ${notice.isPinned ? '<i class="bx bx-pin notice-pin-icon"></i>' : ''}
        `;

        // Add click handler to mark as read
        div.addEventListener('click', function() {
            if (!notice.isRead) {
                markNoticeAsRead(notice.noticeID);
            }
        });

        return div;
    }

    async function markNoticeAsRead(noticeID) {
        try {
            const contextPath = document.querySelector('meta[name="contextPath"]')?.content || '';
            const formData = new URLSearchParams();
            formData.append('noticeId', noticeID);

            const response = await fetch(contextPath + '/api/notices/mark-read', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData.toString()
            });

            if (!response.ok) {
                throw new Error('Failed to mark as read');
            }

            const data = await response.json();

            if (data.success) {
                // Update local state
                const notice = notices.find(n => n.noticeID === noticeID);
                if (notice) {
                    notice.isRead = true;
                }

                // Update UI
                const noticeElement = document.querySelector(`[data-notice-id="${noticeID}"]`);
                if (noticeElement) {
                    noticeElement.classList.remove('unread');
                    noticeElement.classList.add('read');
                }
            }

        } catch (error) {
            console.error('Error marking notice as read:', error);
        }
    }

    function showErrorMessage(message) {
        const noticeList = document.getElementById('noticeList');
        if (!noticeList) return;

        noticeList.innerHTML = `
            <div class="notice-error">
                <i class='bx bx-error-circle'></i>
                <p>${message}</p>
            </div>
        `;
    }

    function escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, m => map[m]);
    }

    // Export to global scope
    window.noticeBoard = {
        reload: loadNotices,
        markAsRead: markNoticeAsRead
    };

})();
