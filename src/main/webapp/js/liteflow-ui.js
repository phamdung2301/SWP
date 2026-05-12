/**
 * LiteFlow UI Enhancement Library
 * Core UI utilities and components for LiteFlow system
 */

class LiteFlowUI {
    constructor() {
        this.init();
    }

    init() {
        this.setupGlobalStyles();
        this.setupEventListeners();
        this.setupAnimations();
    }

    setupGlobalStyles() {
        // Add global CSS variables if needed
        const style = document.createElement('style');
        style.textContent = `
            .liteflow-ui-loading {
                opacity: 0;
                transition: opacity 0.3s ease;
            }
            .liteflow-ui-loaded {
                opacity: 1;
            }
        `;
        document.head.appendChild(style);
    }

    setupEventListeners() {
        // Global click handlers
        document.addEventListener('click', (e) => {
            // Handle ripple effects
            if (e.target.classList.contains('ripple-effect')) {
                this.createRipple(e);
            }
        });
    }

    setupAnimations() {
        // Intersection Observer for scroll animations
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-in');
                }
            });
        }, { threshold: 0.1 });

        // Observe elements with animation classes
        document.querySelectorAll('.animate-on-scroll').forEach(el => {
            observer.observe(el);
        });
    }

    createRipple(event) {
        const button = event.currentTarget;
        const circle = document.createElement('span');
        const diameter = Math.max(button.clientWidth, button.clientHeight);
        const radius = diameter / 2;

        circle.style.width = circle.style.height = `${diameter}px`;
        circle.style.left = `${event.clientX - button.offsetLeft - radius}px`;
        circle.style.top = `${event.clientY - button.offsetTop - radius}px`;
        circle.classList.add('ripple');

        const ripple = button.getElementsByClassName('ripple')[0];
        if (ripple) {
            ripple.remove();
        }

        button.appendChild(circle);
    }

    // Utility methods
    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.classList.add('show');
        }, 100);
        
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    showLoading(element) {
        element.classList.add('liteflow-ui-loading');
    }

    hideLoading(element) {
        element.classList.remove('liteflow-ui-loading');
        element.classList.add('liteflow-ui-loaded');
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.liteflowUI = new LiteFlowUI();
});

// Export for module systems
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LiteFlowUI;
}
