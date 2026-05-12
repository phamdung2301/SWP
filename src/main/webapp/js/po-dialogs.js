/**
 * PO Dialogs - Replacement for browser confirm() and prompt()
 * Uses Modal class from ui-enhancements.js
 */

(function() {
    'use strict';

    // Helper function to escape HTML
    function escapeHtml(text) {
        if (typeof text !== 'string') return text;
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Show confirm dialog - replacement for confirm()
     * @param {string} message - Message to display
     * @param {string} title - Dialog title (default: 'Xác nhận')
     * @returns {Promise<boolean>} - true if confirmed, false if cancelled
     */
    async function showConfirm(message, title = 'Xác nhận') {
        console.log('showConfirm called:', message, title);
        console.log('window.Modal:', window.Modal);
        console.log('window.Modal.confirm:', window.Modal && window.Modal.confirm);
        
        if (window.Modal && typeof window.Modal.confirm === 'function') {
            console.log('Using Modal.confirm');
            return window.Modal.confirm({
                title: title,
                message: message,
                confirmText: 'Xác nhận',
                cancelText: 'Hủy'
            });
        }

        console.warn('Modal.confirm not available, falling back to browser confirm');
        // Fallback to browser confirm
        return Promise.resolve(confirm(message));
    }

    /**
     * Show prompt dialog - replacement for prompt()
     * @param {string} message - Message to display
     * @param {string} title - Dialog title (default: 'Nhập thông tin')
     * @param {string} defaultValue - Default input value (default: '')
     * @returns {Promise<string|null>} - Input value if OK, null if cancelled
     */
    async function showPrompt(message, title = 'Nhập thông tin', defaultValue = '') {
        console.log('showPrompt called:', message, title, defaultValue);
        console.log('window.Modal:', window.Modal);
        
        if (window.Modal && typeof window.Modal === 'function') {
            console.log('Using Modal for prompt');
            return new Promise((resolve) => {
                const inputId = 'prompt-input-' + Date.now();
                const modal = new window.Modal({
                    title: title,
                    content: `
                        <div class="prompt-dialog-content">
                            <label for="${inputId}" class="prompt-label">
                                ${escapeHtml(message)}
                            </label>
                            <input 
                                type="text" 
                                id="${inputId}" 
                                class="prompt-input" 
                                value="${escapeHtml(defaultValue)}"
                                autofocus
                                placeholder="Nhập thông tin..."
                            />
                        </div>
                    `,
                    size: 'small',
                    showClose: false,
                    footer: `
                        <button class="btn btn-secondary" onclick="this.closest('.modal-overlay').dispatchEvent(new CustomEvent('cancel'))">
                            Hủy
                        </button>
                        <button class="btn btn-primary" onclick="this.closest('.modal-overlay').dispatchEvent(new CustomEvent('ok'))">
                            OK
                        </button>
                    `
                });

                const modalElement = modal.show().modal;
                const input = modalElement.querySelector('#' + inputId);
                
                // Focus input after modal is shown
                setTimeout(() => {
                    if (input) {
                        input.focus();
                        if (defaultValue) {
                            input.select();
                        }
                    }
                }, 100);

                // Handle Enter key
                const handleKeyPress = (e) => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        modalElement.dispatchEvent(new CustomEvent('ok'));
                    } else if (e.key === 'Escape') {
                        e.preventDefault();
                        modalElement.dispatchEvent(new CustomEvent('cancel'));
                    }
                };
                
                if (input) {
                    input.addEventListener('keydown', handleKeyPress);
                }

                // Handle OK button
                modalElement.addEventListener('ok', () => {
                    const value = input ? input.value : defaultValue;
                    modal.close();
                    resolve(value);
                });
                
                // Handle Cancel button
                modalElement.addEventListener('cancel', () => {
                    modal.close();
                    resolve(null);
                });
            });
        }

        // Fallback to browser prompt
        return Promise.resolve(prompt(message, defaultValue));
    }

    // Export to global scope
    window.showConfirm = showConfirm;
    window.showPrompt = showPrompt;

})();

