/**
 * LiteFlow ChatBot - Floating AI Assistant
 * Handles chat UI interactions and GPT API communication
 */

class LiteFlowChatBot {
    constructor() {
        this.isOpen = false;
        this.isTyping = false;
        this.messages = [];
        this.apiEndpoint = this.getContextPath() + '/api/chatbot';
        this.sessionKey = 'liteflow_chatbot_session';
        this.historyKey = 'liteflow_chat_history';
        
        this.init();
    }
    
    getContextPath() {
        // Get context path from current URL
        const path = window.location.pathname;
        const contextPath = path.substring(0, path.indexOf('/', 1));
        return contextPath || '';
    }
    
    init() {
        // Check and clear history if new session
        this.checkAndClearSession();
        
        // Create chat UI
        this.createChatUI();
        
        // Bind events
        this.bindEvents();
        
        // Load chat history from localStorage (empty on new session)
        this.loadChatHistory();
        
        console.log('🤖 LiteFlow ChatBot initialized');
    }
    
    checkAndClearSession() {
        try {
            // Get current session ID from sessionStorage
            // sessionStorage is automatically cleared when browser tab/window is closed
            // So if it exists, we're in the same session (same tab/window)
            const currentSessionId = sessionStorage.getItem(this.sessionKey);
            
            if (!currentSessionId) {
                // No session ID = new session (new tab/window or page reload after close)
                // Clear chat history from localStorage
                localStorage.removeItem(this.historyKey);
                this.messages = [];
                
                // Generate and set new session ID
                const newSessionId = Date.now().toString() + '-' + Math.random().toString(36).substr(2, 9);
                sessionStorage.setItem(this.sessionKey, newSessionId);
                
                console.log('🧹 Chat history cleared for new session');
            } else {
                // Session exists = same tab/window, keep history
                console.log('📜 Continuing existing chat session');
            }
            
        } catch (e) {
            console.warn('Failed to check session:', e);
            // Fallback: clear history if sessionStorage is not available
            try {
                localStorage.removeItem(this.historyKey);
                this.messages = [];
            } catch (err) {
                console.warn('Failed to clear history:', err);
            }
        }
    }
    
    createChatUI() {
        const contextPath = this.getContextPath();
        const chatHTML = `
            <!-- Floating Chat Button -->
            <button class="chatbot-button" id="chatbot-button" aria-label="Open Chat">
                <img src="${contextPath}/img/trans_logo.png" alt="LiteFlow" class="chatbot-button-icon">
            </button>
            
            <!-- Chat Window -->
            <div class="chatbot-window" id="chatbot-window">
                <!-- Header -->
                <div class="chatbot-header">
                    <div class="chatbot-header-info">
                        <div class="chatbot-avatar">
                            <img src="${contextPath}/img/trans_logo.png" alt="LiteFlow AI">
                        </div>
                        <div class="chatbot-header-text">
                            <h3>LiteFlow AI</h3>
                            <p>Trợ lý thông minh của bạn ✨</p>
                        </div>
                    </div>
                    <button class="chatbot-close" id="chatbot-close" aria-label="Close Chat">
                        ×
                    </button>
                </div>
                
                <!-- Messages -->
                <div class="chatbot-messages" id="chatbot-messages">
                    <div class="chatbot-welcome">
                        <div class="chatbot-welcome-icon">
                            <img src="${contextPath}/img/trans_logo.png" alt="LiteFlow" style="width: 64px; height: 64px; object-fit: contain;">
                        </div>
                        <h4>Xin chào! 👋</h4>
                        <p style="margin: 8px 0 0 0;">
                            Tôi là trợ lý AI của bạn. Hỏi tôi bất cứ điều gì về hệ thống LiteFlow nhé!
                        </p>
                    </div>
                </div>
                
                <!-- Input -->
                <div class="chatbot-input-container">
                    <input 
                        type="text" 
                        class="chatbot-input" 
                        id="chatbot-input" 
                        placeholder="Nhập câu hỏi của bạn..."
                        autocomplete="off"
                    />
                    <button class="chatbot-send-btn" id="chatbot-send" aria-label="Send Message">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="22" y1="2" x2="11" y2="13"></line>
                            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                        </svg>
                    </button>
                </div>
            </div>
        `;
        
        // Append to body
        document.body.insertAdjacentHTML('beforeend', chatHTML);
    }
    
    bindEvents() {
        const button = document.getElementById('chatbot-button');
        const closeBtn = document.getElementById('chatbot-close');
        const sendBtn = document.getElementById('chatbot-send');
        const input = document.getElementById('chatbot-input');
        
        // Toggle chat window
        button.addEventListener('click', () => this.toggleChat());
        closeBtn.addEventListener('click', () => this.closeChat());
        
        // Send message
        sendBtn.addEventListener('click', () => this.sendMessage());
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });
    }
    
    toggleChat() {
        this.isOpen = !this.isOpen;
        const window = document.getElementById('chatbot-window');
        const button = document.getElementById('chatbot-button');
        
        if (this.isOpen) {
            window.classList.add('active');
            button.classList.add('active');
            // Smooth focus with slight delay for better UX
            setTimeout(() => {
                document.getElementById('chatbot-input').focus();
            }, 100);
        } else {
            window.classList.remove('active');
            button.classList.remove('active');
        }
    }
    
    closeChat() {
        this.isOpen = false;
        document.getElementById('chatbot-window').classList.remove('active');
        document.getElementById('chatbot-button').classList.remove('active');
    }
    
    async sendMessage() {
        const input = document.getElementById('chatbot-input');
        const message = input.value.trim();
        
        if (!message || this.isTyping) return;
        
        // Add user message
        this.addMessage('user', message);
        input.value = '';
        
        // Show typing indicator
        this.showTyping();
        
        try {
            // Call API
            const response = await fetch(this.apiEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ message: message })
            });
            
            const data = await response.json();
            
            // Hide typing indicator
            this.hideTyping();
            
            if (data.success) {
                // Add bot response
                this.addMessage('bot', data.response);
            } else {
                // Show error
                this.addMessage('bot', 'Xin lỗi, có vấn đề xảy ra. ' + (data.error || 'Vui lòng thử lại sau nhé! 🙏'));
            }
            
        } catch (error) {
            console.error('ChatBot API Error:', error);
            this.hideTyping();
            this.addMessage('bot', 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng của bạn. 🔌');
        }
    }
    
    addMessage(role, content) {
        const messagesContainer = document.getElementById('chatbot-messages');
        
        // Remove welcome message if exists
        const welcome = messagesContainer.querySelector('.chatbot-welcome');
        if (welcome) {
            welcome.remove();
        }
        
        const time = new Date().toLocaleTimeString('vi-VN', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
        
        const contextPath = this.getContextPath();
        const avatarContent = role === 'user' 
            ? `<img src="${contextPath}/img/trans_logo.png" alt="User">` 
            : `<img src="${contextPath}/img/trans_logo.png" alt="AI">`;
        
        const messageHTML = `
            <div class="chatbot-message ${role}">
                <div class="chatbot-message-avatar">
                    ${avatarContent}
                </div>
                <div class="chatbot-message-content">
                    ${this.formatMessage(content)}
                    <div class="chatbot-message-time">${time}</div>
                </div>
            </div>
        `;
        
        messagesContainer.insertAdjacentHTML('beforeend', messageHTML);
        
        // Smooth scroll to bottom
        setTimeout(() => {
            messagesContainer.scrollTo({
                top: messagesContainer.scrollHeight,
                behavior: 'smooth'
            });
        }, 50);
        
        // Save to history
        this.messages.push({ role, content, time });
        this.saveChatHistory();
    }
    
    formatMessage(text) {
        // Simple formatting: line breaks, bold, etc.
        return text
            .replace(/\n/g, '<br>')
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>');
    }
    
    showTyping() {
        this.isTyping = true;
        const messagesContainer = document.getElementById('chatbot-messages');
        const contextPath = this.getContextPath();
        
        const typingHTML = `
            <div class="chatbot-message bot" id="chatbot-typing-indicator">
                <div class="chatbot-message-avatar">
                    <img src="${contextPath}/img/trans_logo.png" alt="AI">
                </div>
                <div class="chatbot-typing">
                    <div class="chatbot-typing-dots">
                        <div class="chatbot-typing-dot"></div>
                        <div class="chatbot-typing-dot"></div>
                        <div class="chatbot-typing-dot"></div>
                    </div>
                </div>
            </div>
        `;
        
        messagesContainer.insertAdjacentHTML('beforeend', typingHTML);
        
        // Smooth scroll to bottom
        setTimeout(() => {
            messagesContainer.scrollTo({
                top: messagesContainer.scrollHeight,
                behavior: 'smooth'
            });
        }, 50);
        
        // Disable send button
        document.getElementById('chatbot-send').disabled = true;
    }
    
    hideTyping() {
        this.isTyping = false;
        const indicator = document.getElementById('chatbot-typing-indicator');
        if (indicator) {
            indicator.remove();
        }
        
        // Enable send button
        document.getElementById('chatbot-send').disabled = false;
    }
    
    saveChatHistory() {
        try {
            localStorage.setItem(this.historyKey, JSON.stringify(this.messages));
        } catch (e) {
            console.warn('Failed to save chat history:', e);
        }
    }
    
    loadChatHistory() {
        try {
            // Only load history if we're in the same session
            const currentSessionId = sessionStorage.getItem(this.sessionKey);
            if (!currentSessionId) {
                // No session means this is a new session, don't load old history
                this.messages = [];
                return;
            }
            
            const history = localStorage.getItem(this.historyKey);
            if (history) {
                this.messages = JSON.parse(history);
                
                // Restore last 10 messages
                const recentMessages = this.messages.slice(-10);
                const messagesContainer = document.getElementById('chatbot-messages');
                
                // Remove welcome
                const welcome = messagesContainer.querySelector('.chatbot-welcome');
                if (welcome && recentMessages.length > 0) {
                    welcome.remove();
                }
                
                // Render messages
                const contextPath = this.getContextPath();
                recentMessages.forEach(msg => {
                    const avatarContent = msg.role === 'user' 
                        ? `<img src="${contextPath}/img/trans_logo.png" alt="User">` 
                        : `<img src="${contextPath}/img/trans_logo.png" alt="AI">`;
                    
                    const messageHTML = `
                        <div class="chatbot-message ${msg.role}">
                            <div class="chatbot-message-avatar">
                                ${avatarContent}
                            </div>
                            <div class="chatbot-message-content">
                                ${this.formatMessage(msg.content)}
                                <div class="chatbot-message-time">${msg.time}</div>
                            </div>
                        </div>
                    `;
                    messagesContainer.insertAdjacentHTML('beforeend', messageHTML);
                });
            }
        } catch (e) {
            console.warn('Failed to load chat history:', e);
        }
    }
}

// Initialize chatbot when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    window.liteflowChatBot = new LiteFlowChatBot();
});

