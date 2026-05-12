/**
 * LiteFlow Advanced Performance System
 * Professional performance optimizations and utilities
 * Inspired by modern POS systems and enterprise applications
 */

class LiteFlowPerformance {
  constructor() {
    this.metrics = new Map();
    this.observers = new Map();
    this.cache = new Map();
    this.debounceTimers = new Map();
    this.throttleTimers = new Map();
    this.animationFrameId = null;
    this.isInitialized = false;
    
    this.init();
  }

  init() {
    if (this.isInitialized) return;
    
    this.setupPerformanceMonitoring();
    this.setupIntersectionObserver();
    this.setupResizeObserver();
    this.setupScrollOptimization();
    this.setupMemoryManagement();
    
    this.isInitialized = true;
    console.log('🚀 LiteFlow Performance System initialized');
  }

  // ===== PERFORMANCE MONITORING =====
  setupPerformanceMonitoring() {
    // Monitor Core Web Vitals
    if ('PerformanceObserver' in window) {
      // Largest Contentful Paint
      const lcpObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        const lastEntry = entries[entries.length - 1];
        this.metrics.set('LCP', lastEntry.startTime);
        this.logMetric('LCP', lastEntry.startTime);
      });
      lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });

      // First Input Delay
      const fidObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach(entry => {
          this.metrics.set('FID', entry.processingStart - entry.startTime);
          this.logMetric('FID', entry.processingStart - entry.startTime);
        });
      });
      fidObserver.observe({ entryTypes: ['first-input'] });

      // Cumulative Layout Shift
      let clsValue = 0;
      const clsObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach(entry => {
          if (!entry.hadRecentInput) {
            clsValue += entry.value;
          }
        });
        this.metrics.set('CLS', clsValue);
        this.logMetric('CLS', clsValue);
      });
      clsObserver.observe({ entryTypes: ['layout-shift'] });
    }

    // Monitor Memory Usage
    if ('memory' in performance) {
      setInterval(() => {
        const memory = performance.memory;
        this.metrics.set('memory', {
          used: memory.usedJSHeapSize,
          total: memory.totalJSHeapSize,
          limit: memory.jsHeapSizeLimit
        });
      }, 5000);
    }
  }

  logMetric(name, value) {
    const threshold = this.getMetricThreshold(name);
    const status = value <= threshold ? '✅' : '⚠️';
    console.log(`${status} ${name}: ${value.toFixed(2)}ms`);
  }

  getMetricThreshold(metric) {
    const thresholds = {
      'LCP': 2500,
      'FID': 100,
      'CLS': 0.1
    };
    return thresholds[metric] || 1000;
  }

  // ===== INTERSECTION OBSERVER =====
  setupIntersectionObserver() {
    this.intersectionObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.handleElementVisible(entry.target);
          } else {
            this.handleElementHidden(entry.target);
          }
        });
      },
      {
        rootMargin: '50px',
        threshold: 0.1
      }
    );
  }

  observeElement(element, callback) {
    if (!this.intersectionObserver) return;
    
    element.dataset.observeCallback = callback.name || 'default';
    this.intersectionObserver.observe(element);
  }

  handleElementVisible(element) {
    // Lazy load images
    if (element.tagName === 'IMG' && element.dataset.src) {
      element.src = element.dataset.src;
      element.removeAttribute('data-src');
    }

    // Trigger animations
    if (element.classList.contains('animate-on-scroll')) {
      element.classList.add('animate-fade-in-up');
    }

    // Load content
    if (element.dataset.loadContent) {
      this.loadContent(element);
    }
  }

  handleElementHidden(element) {
    // Pause animations or videos
    if (element.tagName === 'VIDEO') {
      element.pause();
    }
  }

  // ===== RESIZE OBSERVER =====
  setupResizeObserver() {
    this.resizeObserver = new ResizeObserver((entries) => {
      entries.forEach(entry => {
        this.handleResize(entry.target, entry.contentRect);
      });
    });
  }

  observeResize(element, callback) {
    if (!this.resizeObserver) return;
    
    element.dataset.resizeCallback = callback.name || 'default';
    this.resizeObserver.observe(element);
  }

  handleResize(element, rect) {
    // Update responsive layouts
    if (element.classList.contains('responsive-grid')) {
      this.updateResponsiveGrid(element, rect);
    }

    // Update charts or visualizations
    if (element.dataset.chart) {
      this.updateChart(element, rect);
    }
  }

  // ===== SCROLL OPTIMIZATION =====
  setupScrollOptimization() {
    let ticking = false;
    
    const optimizedScrollHandler = () => {
      if (!ticking) {
        requestAnimationFrame(() => {
          this.handleScroll();
          ticking = false;
        });
        ticking = true;
      }
    };

    window.addEventListener('scroll', optimizedScrollHandler, { passive: true });
  }

  handleScroll() {
    const scrollY = window.scrollY;
    
    // Update header visibility
    const header = document.querySelector('.top-header');
    if (header) {
      if (scrollY > 100) {
        header.classList.add('scrolled');
      } else {
        header.classList.remove('scrolled');
      }
    }

    // Update progress indicators
    const progressBars = document.querySelectorAll('.scroll-progress');
    progressBars.forEach(bar => {
      const rect = bar.getBoundingClientRect();
      const progress = Math.max(0, Math.min(1, (window.innerHeight - rect.top) / window.innerHeight));
      bar.style.width = `${progress * 100}%`;
    });
  }

  // ===== MEMORY MANAGEMENT =====
  setupMemoryManagement() {
    // Clean up unused observers
    setInterval(() => {
      this.cleanupObservers();
    }, 30000);

    // Clean up cache
    setInterval(() => {
      this.cleanupCache();
    }, 60000);
  }

  cleanupObservers() {
    // Remove observers for elements that no longer exist
    this.observers.forEach((observer, element) => {
      if (!document.contains(element)) {
        observer.disconnect();
        this.observers.delete(element);
      }
    });
  }

  cleanupCache() {
    // Remove old cache entries
    const now = Date.now();
    this.cache.forEach((value, key) => {
      if (now - value.timestamp > 300000) { // 5 minutes
        this.cache.delete(key);
      }
    });
  }

  // ===== CACHING SYSTEM =====
  setCache(key, value, ttl = 300000) {
    this.cache.set(key, {
      value,
      timestamp: Date.now(),
      ttl
    });
  }

  getCache(key) {
    const cached = this.cache.get(key);
    if (!cached) return null;
    
    if (Date.now() - cached.timestamp > cached.ttl) {
      this.cache.delete(key);
      return null;
    }
    
    return cached.value;
  }

  // ===== DEBOUNCE & THROTTLE =====
  debounce(key, func, delay = 300) {
    if (this.debounceTimers.has(key)) {
      clearTimeout(this.debounceTimers.get(key));
    }
    
    const timer = setTimeout(() => {
      func();
      this.debounceTimers.delete(key);
    }, delay);
    
    this.debounceTimers.set(key, timer);
  }

  throttle(key, func, delay = 100) {
    if (this.throttleTimers.has(key)) {
      return;
    }
    
    func();
    this.throttleTimers.set(key, true);
    
    setTimeout(() => {
      this.throttleTimers.delete(key);
    }, delay);
  }

  // ===== UTILITY METHODS =====
  measurePerformance(name, func) {
    const start = performance.now();
    const result = func();
    const end = performance.now();
    
    this.metrics.set(name, end - start);
    console.log(`⏱️ ${name}: ${(end - start).toFixed(2)}ms`);
    
    return result;
  }

  async measureAsyncPerformance(name, asyncFunc) {
    const start = performance.now();
    const result = await asyncFunc();
    const end = performance.now();
    
    this.metrics.set(name, end - start);
    console.log(`⏱️ ${name}: ${(end - start).toFixed(2)}ms`);
    
    return result;
  }

  loadContent(element) {
    const url = element.dataset.loadContent;
    if (!url) return;
    
    // Check cache first
    const cached = this.getCache(url);
    if (cached) {
      element.innerHTML = cached;
      return;
    }
    
    // Load content
    fetch(url)
      .then(response => response.text())
      .then(html => {
        element.innerHTML = html;
        this.setCache(url, html);
      })
      .catch(error => {
        console.error('Failed to load content:', error);
      });
  }

  updateResponsiveGrid(element, rect) {
    const columns = Math.max(1, Math.floor(rect.width / 300));
    element.style.gridTemplateColumns = `repeat(${columns}, 1fr)`;
  }

  updateChart(element, rect) {
    // Update chart dimensions
    const chart = element.querySelector('canvas');
    if (chart) {
      chart.width = rect.width;
      chart.height = rect.height;
    }
  }

  // ===== PUBLIC API =====
  getMetrics() {
    return Object.fromEntries(this.metrics);
  }

  getPerformanceReport() {
    const metrics = this.getMetrics();
    const report = {
      timestamp: new Date().toISOString(),
      metrics,
      cacheSize: this.cache.size,
      observersCount: this.observers.size
    };
    
    console.table(report);
    return report;
  }

  destroy() {
    // Clean up observers
    this.intersectionObserver?.disconnect();
    this.resizeObserver?.disconnect();
    
    // Clear timers
    this.debounceTimers.forEach(timer => clearTimeout(timer));
    this.throttleTimers.forEach(timer => clearTimeout(timer));
    
    // Clear cache
    this.cache.clear();
    
    this.isInitialized = false;
  }
}

// ===== ADVANCED UTILITIES =====
class LiteFlowUtils {
  static createVirtualScroll(container, items, itemHeight, renderItem) {
    const viewport = container.clientHeight;
    const totalHeight = items.length * itemHeight;
    const visibleItems = Math.ceil(viewport / itemHeight) + 2;
    
    let scrollTop = 0;
    let startIndex = 0;
    let endIndex = Math.min(startIndex + visibleItems, items.length);
    
    const updateScroll = () => {
      startIndex = Math.floor(scrollTop / itemHeight);
      endIndex = Math.min(startIndex + visibleItems, items.length);
      
      const visibleItemsData = items.slice(startIndex, endIndex);
      const offsetY = startIndex * itemHeight;
      
      container.innerHTML = visibleItemsData.map(renderItem).join('');
      container.style.transform = `translateY(${offsetY}px)`;
    };
    
    container.addEventListener('scroll', () => {
      scrollTop = container.scrollTop;
      updateScroll();
    });
    
    updateScroll();
  }

  static createInfiniteScroll(container, loadMore, threshold = 100) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          loadMore();
        }
      });
    }, {
      root: container,
      rootMargin: `${threshold}px`
    });
    
    const sentinel = document.createElement('div');
    sentinel.className = 'infinite-scroll-sentinel';
    container.appendChild(sentinel);
    observer.observe(sentinel);
    
    return observer;
  }

  static createImageLazyLoader() {
    const images = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target;
          img.src = img.dataset.src;
          img.removeAttribute('data-src');
          imageObserver.unobserve(img);
        }
      });
    });
    
    images.forEach(img => imageObserver.observe(img));
    
    return imageObserver;
  }

  static createFormValidator(form, rules) {
    const validator = {
      errors: new Map(),
      
      validate() {
        this.errors.clear();
        
        Object.keys(rules).forEach(fieldName => {
          const field = form.querySelector(`[name="${fieldName}"]`);
          if (!field) return;
          
          const value = field.value.trim();
          const fieldRules = rules[fieldName];
          
          fieldRules.forEach(rule => {
            if (!rule.validate(value)) {
              this.errors.set(fieldName, rule.message);
            }
          });
        });
        
        this.displayErrors();
        return this.errors.size === 0;
      },
      
      displayErrors() {
        // Clear previous errors
        form.querySelectorAll('.error-message').forEach(el => el.remove());
        
        this.errors.forEach((message, fieldName) => {
          const field = form.querySelector(`[name="${fieldName}"]`);
          if (field) {
            field.classList.add('input-error');
            
            const errorEl = document.createElement('div');
            errorEl.className = 'error-message';
            errorEl.textContent = message;
            field.parentNode.appendChild(errorEl);
          }
        });
      }
    };
    
    form.addEventListener('submit', (e) => {
      if (!validator.validate()) {
        e.preventDefault();
      }
    });
    
    return validator;
  }

  static createKeyboardShortcuts(shortcuts) {
    const handler = (e) => {
      const key = e.key.toLowerCase();
      const ctrl = e.ctrlKey || e.metaKey;
      const shift = e.shiftKey;
      const alt = e.altKey;
      
      const shortcut = `${ctrl ? 'ctrl+' : ''}${shift ? 'shift+' : ''}${alt ? 'alt+' : ''}${key}`;
      
      if (shortcuts[shortcut]) {
        e.preventDefault();
        shortcuts[shortcut]();
      }
    };
    
    document.addEventListener('keydown', handler);
    
    return () => document.removeEventListener('keydown', handler);
  }
}

// ===== INITIALIZATION =====
const liteFlowPerformance = new LiteFlowPerformance();

// Export for global access
window.LiteFlowPerformance = liteFlowPerformance;
window.LiteFlowUtils = LiteFlowUtils;

// Auto-initialize on DOM ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    liteFlowPerformance.init();
  });
} else {
  liteFlowPerformance.init();
}
