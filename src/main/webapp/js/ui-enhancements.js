/**
 * LiteFlow UI Enhancements Library
 * Modern, professional UI components and utilities
 * Inspired by KiotViet, IPOS, and modern POS systems
 */

// ==================== TOAST NOTIFICATIONS ====================
class ToastNotification {
  constructor() {
    this.container = null;
    this.init();
  }

  init() {
    if (!document.getElementById('toast-container')) {
      this.container = document.createElement('div');
      this.container.id = 'toast-container';
      this.container.className = 'toast-container';
      document.body.appendChild(this.container);
    } else {
      this.container = document.getElementById('toast-container');
    }
  }

  show(message, type = 'info', duration = 3000) {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type} toast-enter`;
    
    const icons = {
      success: '✓',
      error: '✕',
      warning: '⚠',
      info: 'ℹ'
    };

    toast.innerHTML = `
      <div class="toast-icon">${icons[type] || icons.info}</div>
      <div class="toast-content">
        <div class="toast-message">${message}</div>
      </div>
      <button class="toast-close" onclick="this.parentElement.remove()">×</button>
    `;

    this.container.appendChild(toast);

    // Trigger animation
    setTimeout(() => toast.classList.add('toast-show'), 10);

    // Auto remove
    if (duration > 0) {
      setTimeout(() => {
        toast.classList.remove('toast-show');
        toast.classList.add('toast-exit');
        setTimeout(() => toast.remove(), 300);
      }, duration);
    }

    return toast;
  }

  success(message, duration) {
    return this.show(message, 'success', duration);
  }

  error(message, duration) {
    return this.show(message, 'error', duration);
  }

  warning(message, duration) {
    return this.show(message, 'warning', duration);
  }

  info(message, duration) {
    return this.show(message, 'info', duration);
  }
}

// Global toast instance
window.toast = new ToastNotification();

// ==================== LOADING OVERLAY ====================
class LoadingOverlay {
  constructor() {
    this.overlay = null;
  }

  show(message = 'Đang tải...') {
    if (this.overlay) return;

    this.overlay = document.createElement('div');
    this.overlay.className = 'loading-overlay';
    this.overlay.innerHTML = `
      <div class="loading-spinner">
        <div class="spinner"></div>
        <div class="loading-text">${message}</div>
      </div>
    `;
    document.body.appendChild(this.overlay);
    setTimeout(() => this.overlay.classList.add('show'), 10);
  }

  hide() {
    if (!this.overlay) return;
    
    this.overlay.classList.remove('show');
    setTimeout(() => {
      if (this.overlay && this.overlay.parentNode) {
        this.overlay.parentNode.removeChild(this.overlay);
      }
      this.overlay = null;
    }, 300);
  }

  static showFor(promise, message) {
    const loader = new LoadingOverlay();
    loader.show(message);
    return promise.finally(() => loader.hide());
  }
}

window.loading = new LoadingOverlay();

// ==================== MODAL SYSTEM ====================
class Modal {
  constructor(options = {}) {
    this.options = {
      title: '',
      content: '',
      size: 'medium', // small, medium, large
      showClose: true,
      backdrop: true,
      ...options
    };
    this.modal = null;
  }

  show() {
    this.modal = document.createElement('div');
    this.modal.className = 'modal-overlay';
    
    const sizeClass = `modal-${this.options.size}`;
    
    this.modal.innerHTML = `
      <div class="modal-dialog ${sizeClass}">
        <div class="modal-header">
          <h3 class="modal-title">${this.options.title}</h3>
          ${this.options.showClose ? '<button class="modal-close" onclick="this.closest(\'.modal-overlay\').remove()">×</button>' : ''}
        </div>
        <div class="modal-body">
          ${this.options.content}
        </div>
        ${this.options.footer ? `<div class="modal-footer">${this.options.footer}</div>` : ''}
      </div>
    `;

    if (this.options.backdrop) {
      this.modal.addEventListener('click', (e) => {
        if (e.target === this.modal) {
          this.close();
        }
      });
    }

    document.body.appendChild(this.modal);
    setTimeout(() => this.modal.classList.add('show'), 10);

    return this;
  }

  close() {
    if (!this.modal) return;
    
    this.modal.classList.remove('show');
    setTimeout(() => {
      if (this.modal && this.modal.parentNode) {
        this.modal.parentNode.removeChild(this.modal);
      }
      this.modal = null;
    }, 300);
  }

  static confirm(options) {
    return new Promise((resolve) => {
      const modal = new Modal({
        title: options.title || 'Xác nhận',
        content: options.message || '',
        size: 'small',
        footer: `
          <button class="btn btn-secondary" onclick="this.closest('.modal-overlay').dispatchEvent(new CustomEvent('cancel'))">
            ${options.cancelText || 'Hủy'}
          </button>
          <button class="btn btn-primary" onclick="this.closest('.modal-overlay').dispatchEvent(new CustomEvent('confirm'))">
            ${options.confirmText || 'Xác nhận'}
          </button>
        `
      });

      const modalElement = modal.show().modal;
      
      modalElement.addEventListener('confirm', () => {
        modal.close();
        resolve(true);
      });
      
      modalElement.addEventListener('cancel', () => {
        modal.close();
        resolve(false);
      });
    });
  }
}

window.Modal = Modal;

// ==================== SKELETON LOADING ====================
function createSkeleton(type = 'card', count = 1) {
  const skeletons = {
    card: '<div class="skeleton skeleton-card"></div>',
    text: '<div class="skeleton skeleton-text"></div>',
    circle: '<div class="skeleton skeleton-circle"></div>',
    table: `
      <div class="skeleton-table">
        <div class="skeleton skeleton-text" style="width: 100%; margin-bottom: 8px;"></div>
        <div class="skeleton skeleton-text" style="width: 100%; margin-bottom: 8px;"></div>
        <div class="skeleton skeleton-text" style="width: 80%;"></div>
      </div>
    `
  };

  return Array(count).fill(skeletons[type] || skeletons.card).join('');
}

// ==================== DEBOUNCE & THROTTLE ====================
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

function throttle(func, limit) {
  let inThrottle;
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}

// ==================== ANIMATION UTILITIES ====================
function animateValue(element, start, end, duration, suffix = '') {
  const range = end - start;
  const startTime = performance.now();
  
  function update(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    
    // Easing function (easeOutCubic)
    const easeProgress = 1 - Math.pow(1 - progress, 3);
    
    const current = start + (range * easeProgress);
    element.textContent = Math.round(current).toLocaleString('vi-VN') + suffix;
    
    if (progress < 1) {
      requestAnimationFrame(update);
    }
  }
  
  requestAnimationFrame(update);
}

// ==================== SMOOTH SCROLL ====================
function smoothScrollTo(element, duration = 500) {
  const target = typeof element === 'string' ? document.querySelector(element) : element;
  if (!target) return;

  const start = window.pageYOffset;
  const targetPosition = target.getBoundingClientRect().top + start;
  const distance = targetPosition - start;
  const startTime = performance.now();

  function animation(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    
    // Easing function
    const easeProgress = progress < 0.5
      ? 4 * progress * progress * progress
      : 1 - Math.pow(-2 * progress + 2, 3) / 2;

    window.scrollTo(0, start + distance * easeProgress);

    if (progress < 1) {
      requestAnimationFrame(animation);
    }
  }

  requestAnimationFrame(animation);
}

// ==================== RIPPLE EFFECT ====================
function addRippleEffect(element) {
  element.addEventListener('click', function(e) {
    const ripple = document.createElement('span');
    ripple.className = 'ripple';
    
    const rect = this.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = e.clientX - rect.left - size / 2;
    const y = e.clientY - rect.top - size / 2;
    
    ripple.style.width = ripple.style.height = size + 'px';
    ripple.style.left = x + 'px';
    ripple.style.top = y + 'px';
    
    this.appendChild(ripple);
    
    setTimeout(() => ripple.remove(), 600);
  });
}

// ==================== COPY TO CLIPBOARD ====================
async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text);
    toast.success('Đã sao chép vào clipboard');
    return true;
  } catch (err) {
    toast.error('Không thể sao chép');
    return false;
  }
}

// ==================== FORMAT UTILITIES ====================
const formatters = {
  currency: (value) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value);
  },
  
  number: (value) => {
    return new Intl.NumberFormat('vi-VN').format(value);
  },
  
  date: (date, format = 'short') => {
    const options = format === 'long' 
      ? { year: 'numeric', month: 'long', day: 'numeric' }
      : { year: 'numeric', month: '2-digit', day: '2-digit' };
    
    return new Intl.DateTimeFormat('vi-VN', options).format(new Date(date));
  },
  
  time: (date) => {
    return new Intl.DateTimeFormat('vi-VN', {
      hour: '2-digit',
      minute: '2-digit'
    }).format(new Date(date));
  }
};

// ==================== INTERSECTION OBSERVER (Lazy Loading) ====================
function lazyLoadImages() {
  const images = document.querySelectorAll('img[data-src]');
  
  const imageObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        img.src = img.dataset.src;
        img.classList.add('fade-in');
        img.removeAttribute('data-src');
        observer.unobserve(img);
      }
    });
  });

  images.forEach(img => imageObserver.observe(img));
}

// ==================== FORM VALIDATION ====================
class FormValidator {
  constructor(form) {
    this.form = form;
    this.errors = {};
  }

  validate(rules) {
    this.errors = {};
    
    for (const [field, fieldRules] of Object.entries(rules)) {
      const input = this.form.querySelector(`[name="${field}"]`);
      if (!input) continue;
      
      const value = input.value.trim();
      
      for (const rule of fieldRules) {
        if (rule.required && !value) {
          this.errors[field] = rule.message || 'Trường này là bắt buộc';
          break;
        }
        
        if (rule.pattern && !rule.pattern.test(value)) {
          this.errors[field] = rule.message || 'Định dạng không hợp lệ';
          break;
        }
        
        if (rule.minLength && value.length < rule.minLength) {
          this.errors[field] = rule.message || `Tối thiểu ${rule.minLength} ký tự`;
          break;
        }
        
        if (rule.custom && !rule.custom(value)) {
          this.errors[field] = rule.message || 'Giá trị không hợp lệ';
          break;
        }
      }
    }
    
    this.displayErrors();
    return Object.keys(this.errors).length === 0;
  }

  displayErrors() {
    // Clear previous errors
    this.form.querySelectorAll('.error-message').forEach(el => el.remove());
    this.form.querySelectorAll('.input-error').forEach(el => el.classList.remove('input-error'));
    
    // Display new errors
    for (const [field, message] of Object.entries(this.errors)) {
      const input = this.form.querySelector(`[name="${field}"]`);
      if (!input) continue;
      
      input.classList.add('input-error');
      
      const errorDiv = document.createElement('div');
      errorDiv.className = 'error-message';
      errorDiv.textContent = message;
      
      input.parentNode.insertBefore(errorDiv, input.nextSibling);
    }
  }
}

// ==================== PERFORMANCE MONITORING ====================
function measurePerformance(name, fn) {
  const start = performance.now();
  const result = fn();
  const end = performance.now();
  console.log(`⏱️ ${name}: ${(end - start).toFixed(2)}ms`);
  return result;
}

// ==================== KEYBOARD SHORTCUTS ====================
class KeyboardShortcuts {
  constructor() {
    this.shortcuts = new Map();
    this.init();
  }

  init() {
    document.addEventListener('keydown', (e) => {
      const key = this.getKeyString(e);
      const handler = this.shortcuts.get(key);
      
      if (handler) {
        e.preventDefault();
        handler(e);
      }
    });
  }

  getKeyString(e) {
    const parts = [];
    if (e.ctrlKey) parts.push('Ctrl');
    if (e.altKey) parts.push('Alt');
    if (e.shiftKey) parts.push('Shift');
    parts.push(e.key.toLowerCase());
    return parts.join('+');
  }

  register(keyCombo, handler) {
    this.shortcuts.set(keyCombo.toLowerCase(), handler);
  }

  unregister(keyCombo) {
    this.shortcuts.delete(keyCombo.toLowerCase());
  }
}

window.keyboard = new KeyboardShortcuts();

// ==================== DROPDOWN FUNCTIONALITY ====================
function setupDropdownMenus() {
  const dropdowns = document.querySelectorAll('.nav-item.dropdown');
  
  dropdowns.forEach(dropdown => {
    const toggle = dropdown.querySelector('.dropdown-toggle');
    const menu = dropdown.querySelector('.dropdown-menu');
    
    if (!toggle || !menu) return;
    
    // Click handler for mobile/touch devices
    toggle.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();
      
      // Close other dropdowns
      dropdowns.forEach(otherDropdown => {
        if (otherDropdown !== dropdown) {
          otherDropdown.classList.remove('show');
        }
      });
      
      // Toggle current dropdown
      dropdown.classList.toggle('show');
    });
    
    // Hover handlers for desktop
    dropdown.addEventListener('mouseenter', function() {
      if (window.innerWidth > 768) {
        dropdown.classList.add('show');
      }
    });
    
    dropdown.addEventListener('mouseleave', function() {
      if (window.innerWidth > 768) {
        dropdown.classList.remove('show');
      }
    });
  });
  
  // Close dropdowns when clicking outside
  document.addEventListener('click', function(e) {
    if (!e.target.closest('.nav-item.dropdown')) {
      dropdowns.forEach(dropdown => {
        dropdown.classList.remove('show');
      });
    }
  });
  
  // Close dropdowns on escape key
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      dropdowns.forEach(dropdown => {
        dropdown.classList.remove('show');
      });
    }
  });
}

// ==================== AUTO-INIT ON DOM READY ====================
document.addEventListener('DOMContentLoaded', function() {
  // Setup dropdown menus
  setupDropdownMenus();
  
  // Add ripple effect to buttons
  document.querySelectorAll('.btn, button').forEach(btn => {
    if (!btn.classList.contains('no-ripple')) {
      addRippleEffect(btn);
    }
  });

  // Initialize lazy loading
  lazyLoadImages();

  // Add smooth scroll to anchor links
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
      e.preventDefault();
      const href = this.getAttribute('href');
      // Only process if href has more than just '#'
      if (href && href.length > 1) {
        const target = document.querySelector(href);
        if (target) {
          smoothScrollTo(target);
        }
      }
    });
  });

  // Auto-hide alerts after 5 seconds
  document.querySelectorAll('.alert, .msg, .error, .success').forEach(alert => {
    setTimeout(() => {
      alert.style.transition = 'opacity 0.3s ease';
      alert.style.opacity = '0';
      setTimeout(() => alert.remove(), 300);
    }, 5000);
  });

  console.log('✨ LiteFlow UI Enhancements loaded successfully!');
});

// Export utilities
window.LiteFlowUI = {
  toast,
  loading,
  Modal,
  createSkeleton,
  debounce,
  throttle,
  animateValue,
  smoothScrollTo,
  copyToClipboard,
  formatters,
  FormValidator,
  keyboard,
  measurePerformance
};
