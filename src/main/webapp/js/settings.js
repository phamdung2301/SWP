/**
 * Settings Page JavaScript
 * Handles sidebar toggle, animations, and interactions
 */

// Global AI Agent Config instance (initialized when section becomes active)
let aiAgentConfigInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('settingsSidebar');
    const toggleButton = document.getElementById('sidebarToggle');
    const sidebarItems = document.querySelectorAll('.sidebar-item');
    const sections = document.querySelectorAll('.settings-section');
    
    // Load sidebar state from localStorage
    const savedState = localStorage.getItem('settingsSidebarCollapsed');
    if (savedState === 'true') {
        sidebar.classList.add('collapsed');
    }
    
    // Sidebar Toggle
    if (toggleButton && sidebar) {
        toggleButton.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
            const isCollapsed = sidebar.classList.contains('collapsed');
            localStorage.setItem('settingsSidebarCollapsed', isCollapsed.toString());
        });
    }
    
    // Initialize AI Agent Config if section is active on load
    // Use setTimeout to ensure AIAgentConfig class is loaded
    const aiAgentSection = document.getElementById('ai-agent-section');
    if (aiAgentSection && aiAgentSection.classList.contains('active')) {
        setTimeout(() => {
            initializeAIAgentConfig();
        }, 100);
    }
    
    // Sidebar Item Click - Switch Sections
    sidebarItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            
            // Remove active class from all items
            sidebarItems.forEach(i => i.classList.remove('active'));
            
            // Add active class to clicked item
            item.classList.add('active');
            
            // Get section to show
            const sectionId = item.getAttribute('data-section');
            if (sectionId) {
                // Hide all sections
                sections.forEach(section => {
                    section.classList.remove('active');
                });
                
                // Show selected section
                const targetSection = document.getElementById(sectionId + '-section');
                if (targetSection) {
                    targetSection.classList.add('active');
                    
                    // Initialize AI Agent Config if AI Agent section is shown
                    if (sectionId === 'ai-agent' && !aiAgentConfigInstance) {
                        setTimeout(() => {
                            initializeAIAgentConfig();
                        }, 100);
                    }
                    
                    // Initialize Company Info if company-info section is shown
                    if (sectionId === 'company-info') {
                        // Company info will auto-initialize via MutationObserver in company-info.js
                        // But we can trigger it here if needed
                        if (typeof initializeCompanyInfo === 'function') {
                            setTimeout(() => {
                                initializeCompanyInfo();
                            }, 100);
                        }
                    }
                }
            }
        });
    });
    
    // Animate cards on page load
    const cards = document.querySelectorAll('.settings-card');
    
    cards.forEach((card, index) => {
        // Set initial state
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        
        // Animate in with delay
        setTimeout(() => {
            card.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
    
    // Add click ripple effect to cards
    cards.forEach(card => {
        card.addEventListener('click', function(e) {
            // Create ripple element
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');
            
            this.appendChild(ripple);
            
            // Remove ripple after animation
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
    
    // Mobile: Close sidebar when clicking outside
    if (window.innerWidth <= 768) {
        document.addEventListener('click', (e) => {
            if (sidebar && !sidebar.contains(e.target) && !toggleButton.contains(e.target)) {
                sidebar.classList.remove('active');
            }
        });
    }
    
    // Mobile: Add toggle button for sidebar (if needed)
    if (window.innerWidth <= 768 && !document.querySelector('.mobile-sidebar-toggle')) {
        const mobileToggle = document.createElement('button');
        mobileToggle.className = 'mobile-sidebar-toggle';
        mobileToggle.innerHTML = '<i class="bx bx-menu"></i>';
        mobileToggle.style.cssText = `
            position: fixed;
            top: 70px;
            left: 16px;
            z-index: 1001;
            width: 40px;
            height: 40px;
            border: none;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--gray-700);
        `;
        mobileToggle.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
        document.body.appendChild(mobileToggle);
    }
    
    console.log('✅ Settings page initialized');
});

/**
 * Initialize AI Agent Config when section becomes active
 */
function initializeAIAgentConfig() {
    if (aiAgentConfigInstance) {
        return; // Already initialized
    }
    
    const aiAgentSection = document.getElementById('ai-agent-section');
    if (!aiAgentSection) {
        return; // Section doesn't exist
    }
    
    // Check if AIAgentConfig class is available
    if (typeof AIAgentConfig === 'undefined') {
        console.warn('⚠️ AIAgentConfig class not loaded yet');
        return;
    }
    
    // Initialize AI Agent Config
    try {
        aiAgentConfigInstance = new AIAgentConfig();
        console.log('✅ AI Agent Config initialized in settings page');
    } catch (error) {
        console.error('❌ Failed to initialize AI Agent Config:', error);
    }
}
