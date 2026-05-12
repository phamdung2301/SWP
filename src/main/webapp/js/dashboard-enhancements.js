/**
 * LiteFlow Dashboard Enhancements
 * Advanced dashboard functionality with animations and performance optimizations
 */

class DashboardEnhancements {
  constructor() {
    this.isInitialized = false;
    this.animationQueue = [];
    this.metrics = {
      salesCards: [],
      charts: [],
      activities: []
    };
    
    this.init();
  }

  init() {
    if (this.isInitialized) return;
    
    this.setupAnimations();
    this.setupInteractiveElements();
    this.setupDataVisualization();
    this.setupPerformanceOptimizations();
    this.setupKeyboardShortcuts();
    
    this.isInitialized = true;
    console.log('📊 Dashboard Enhancements initialized');
  }

  // ===== ANIMATIONS =====
  setupAnimations() {
    // Staggered animation for sales cards
    this.animateSalesCards();
    
    // Animate revenue section
    this.animateRevenueSection();
    
    // Animate activities
    this.animateActivities();
    
    // Setup scroll-triggered animations
    this.setupScrollAnimations();
  }

  animateSalesCards() {
    const salesCards = document.querySelectorAll('.sales-card');
    
    salesCards.forEach((card, index) => {
      // Add staggered animation delay
      card.style.animationDelay = `${index * 0.1}s`;
      card.classList.add('animate-fade-in-up');
      
      // Add hover effects
      card.classList.add('interactive');
      
      // Removed: animateNumbers - display actual values from database
    });
  }

  animateRevenueSection() {
    const revenueSection = document.querySelector('.revenue-section');
    if (!revenueSection) return;
    
    revenueSection.classList.add('animate-fade-in-up', 'animate-stagger-2');
    
    // Animate tabs
    const tabs = revenueSection.querySelectorAll('.tab');
    tabs.forEach((tab, index) => {
      tab.style.animationDelay = `${0.5 + index * 0.1}s`;
      tab.classList.add('animate-fade-in-down');
    });
  }

  animateActivities() {
    const activities = document.querySelectorAll('.activity-item');
    
    activities.forEach((activity, index) => {
      activity.style.animationDelay = `${index * 0.1}s`;
      activity.classList.add('animate-fade-in-right');
      
      // Add hover effects
      activity.classList.add('interactive');
    });
  }

  setupScrollAnimations() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('animate-fade-in-up');
        }
      });
    }, {
      threshold: 0.1,
      rootMargin: '50px'
    });

    // Observe elements for scroll animations
    document.querySelectorAll('.animate-on-scroll').forEach(el => {
      observer.observe(el);
    });
  }

  // ===== INTERACTIVE ELEMENTS =====
  setupInteractiveElements() {
    this.setupSalesCardInteractions();
    this.setupTabInteractions();
    this.setupDropdownInteractions();
    this.setupAdBannerInteractions();
  }

  setupSalesCardInteractions() {
    const salesCards = document.querySelectorAll('.sales-card');
    
    salesCards.forEach(card => {
      card.addEventListener('mouseenter', () => {
        this.highlightCard(card);
      });
      
      card.addEventListener('mouseleave', () => {
        this.unhighlightCard(card);
      });
      
      card.addEventListener('click', () => {
        this.showCardDetails(card);
      });
    });
  }

  highlightCard(card) {
    card.style.transform = 'translateY(-8px) scale(1.02)';
    card.style.boxShadow = '0 20px 40px rgba(0, 0, 0, 0.15)';
    
    // Add glow effect
    const icon = card.querySelector('.icon');
    if (icon) {
      icon.classList.add('animate-glow');
    }
  }

  unhighlightCard(card) {
    card.style.transform = '';
    card.style.boxShadow = '';
    
    const icon = card.querySelector('.icon');
    if (icon) {
      icon.classList.remove('animate-glow');
    }
  }

  showCardDetails(card) {
    const value = card.querySelector('.value').textContent;
    const label = card.querySelector('.label').textContent;
    
    // Show detailed modal or tooltip
    this.showTooltip(card, `${label}: ${value}`);
  }

  setupTabInteractions() {
    // Only setup tab interactions for non-revenue tabs
    // Revenue tabs are handled by DashboardRevenue class
    const revenueSection = document.querySelector('.revenue-section');
    if (revenueSection) {
      // Revenue tabs are handled by dashboard-revenue.js
      return;
    }
    
    const tabs = document.querySelectorAll('.tab');
    
    tabs.forEach(tab => {
      tab.addEventListener('click', () => {
        // Remove active from all tabs
        tabs.forEach(t => t.classList.remove('active'));
        
        // Add active to clicked tab
        tab.classList.add('active');
        
        // Add click animation
        tab.classList.add('animate-pulse');
        setTimeout(() => {
          tab.classList.remove('animate-pulse');
        }, 200);
        
        // Update content based on tab
        this.updateTabContent(tab.dataset.tab || tab.textContent);
      });
    });
  }

  updateTabContent(tabName) {
    const revenueContent = document.querySelector('.revenue-content');
    if (!revenueContent) return;
    
    // Add loading state
    revenueContent.innerHTML = '<div class="loading-skeleton" style="height: 200px;"></div>';
    
    // Simulate data loading
    setTimeout(() => {
      revenueContent.innerHTML = `
        <div class="empty-icon animate-bounce">📊</div>
        <div class="empty-text">Dữ liệu ${tabName.toLowerCase()}</div>
      `;
    }, 500);
  }

  setupDropdownInteractions() {
    const dropdowns = document.querySelectorAll('.activities-dropdown');
    
    dropdowns.forEach(dropdown => {
      dropdown.addEventListener('change', () => {
        this.updateActivities(dropdown.value);
      });
    });
  }

  updateActivities(period) {
    const activities = document.querySelectorAll('.activity-item');
    
    // Add loading animation
    activities.forEach(activity => {
      activity.classList.add('loading-skeleton');
    });
    
    // Simulate data update
    setTimeout(() => {
      activities.forEach(activity => {
        activity.classList.remove('loading-skeleton');
        activity.classList.add('animate-fade-in-up');
      });
    }, 300);
  }

  setupAdBannerInteractions() {
    const adBanner = document.querySelector('.ad-banner');
    if (!adBanner) return;
    
    adBanner.addEventListener('click', () => {
      this.handleAdClick();
    });
    
    // Add floating animation
    adBanner.classList.add('animate-float');
  }

  handleAdClick() {
    // Add click animation
    const adBanner = document.querySelector('.ad-banner');
    adBanner.classList.add('animate-bounce');
    
    setTimeout(() => {
      adBanner.classList.remove('animate-bounce');
    }, 600);
    
    // Show promotion details
    this.showPromotionModal();
  }

  // ===== DATA VISUALIZATION =====
  setupDataVisualization() {
    this.setupCharts();
    this.setupProgressBars();
    this.setupMetrics();
  }

  setupCharts() {
    // Initialize empty charts with placeholders
    const chartContainers = document.querySelectorAll('.chart-container');
    
    chartContainers.forEach(container => {
      this.createChartPlaceholder(container);
    });
  }

  createChartPlaceholder(container) {
    container.innerHTML = `
      <div class="chart-placeholder">
        <div class="chart-skeleton loading-skeleton"></div>
        <div class="chart-label">Biểu đồ doanh thu</div>
      </div>
    `;
  }

  setupProgressBars() {
    const progressBars = document.querySelectorAll('.progress-bar');
    
    progressBars.forEach(bar => {
      const progress = bar.dataset.progress || 0;
      this.animateProgressBar(bar, progress);
    });
  }

  animateProgressBar(bar, targetProgress) {
    bar.style.width = '0%';
    
    setTimeout(() => {
      bar.style.width = `${targetProgress}%`;
    }, 500);
  }

  setupMetrics() {
    // Removed: Auto-update metrics - display actual values from database only
    // No automatic value increases
  }

  // ===== PERFORMANCE OPTIMIZATIONS =====
  setupPerformanceOptimizations() {
    this.setupLazyLoading();
    this.setupVirtualScrolling();
    this.setupDebouncedUpdates();
  }

  setupLazyLoading() {
    // Lazy load images
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
  }

  setupVirtualScrolling() {
    // Setup virtual scrolling for large lists
    const longLists = document.querySelectorAll('.virtual-scroll');
    
    longLists.forEach(list => {
      this.setupVirtualScroll(list);
    });
  }

  setupVirtualScroll(container) {
    // Implementation for virtual scrolling
    // This would be used for large activity lists
  }

  setupDebouncedUpdates() {
    // Debounce search and filter updates
    const searchInputs = document.querySelectorAll('input[type="search"]');
    
    searchInputs.forEach(input => {
      input.addEventListener('input', LiteFlowPerformance.debounce(
        `search-${input.id}`,
        () => this.handleSearch(input.value),
        300
      ));
    });
  }

  handleSearch(query) {
    // Handle search with debouncing
    console.log('Searching for:', query);
  }

  // ===== KEYBOARD SHORTCUTS =====
  setupKeyboardShortcuts() {
    const shortcuts = {
      'ctrl+1': () => this.switchToTab('sales'),
      'ctrl+2': () => this.switchToTab('revenue'),
      'ctrl+3': () => this.switchToTab('activities'),
      'ctrl+r': () => this.refreshData(),
      'ctrl+f': () => this.focusSearch(),
      'escape': () => this.closeModals()
    };
    
    LiteFlowUtils.createKeyboardShortcuts(shortcuts);
  }

  switchToTab(tabName) {
    const tab = document.querySelector(`[data-tab="${tabName}"]`);
    if (tab) {
      tab.click();
    }
  }

  refreshData() {
    // Add refresh animation
    document.body.classList.add('animate-pulse');
    
    setTimeout(() => {
      document.body.classList.remove('animate-pulse');
      // Removed: updateMetrics - no automatic value increases
      // Values should be refreshed from server if needed
    }, 1000);
  }

  focusSearch() {
    const searchInput = document.querySelector('input[type="search"]');
    if (searchInput) {
      searchInput.focus();
    }
  }

  closeModals() {
    // Close any open modals
    document.querySelectorAll('.modal.show').forEach(modal => {
      modal.classList.remove('show');
    });
  }

  // ===== UTILITY METHODS =====
  showTooltip(element, text) {
    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip';
    tooltip.textContent = text;
    tooltip.style.cssText = `
      position: absolute;
      background: rgba(0, 0, 0, 0.8);
      color: white;
      padding: 8px 12px;
      border-radius: 6px;
      font-size: 12px;
      z-index: 1000;
      pointer-events: none;
      opacity: 0;
      transition: opacity 0.3s ease;
    `;
    
    document.body.appendChild(tooltip);
    
    const rect = element.getBoundingClientRect();
    tooltip.style.left = `${rect.left + rect.width / 2 - tooltip.offsetWidth / 2}px`;
    tooltip.style.top = `${rect.top - tooltip.offsetHeight - 8}px`;
    
    // Show tooltip
    setTimeout(() => {
      tooltip.style.opacity = '1';
    }, 10);
    
    // Hide tooltip
    setTimeout(() => {
      tooltip.style.opacity = '0';
      setTimeout(() => {
        document.body.removeChild(tooltip);
      }, 300);
    }, 2000);
  }

  showPromotionModal() {
    // Show promotion details modal
    const modal = document.createElement('div');
    modal.className = 'modal-overlay show';
    modal.innerHTML = `
      <div class="modal-dialog modal-medium">
        <div class="modal-header">
          <h3 class="modal-title">🎉 Khuyến mãi đặc biệt</h3>
          <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">×</button>
        </div>
        <div class="modal-body">
          <p>Rinh ưu đãi tới 1,8 triệu đồng!</p>
          <p>Áp dụng cho tất cả sản phẩm trong tháng này.</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" onclick="this.closest('.modal-overlay').remove()">Đóng</button>
        </div>
      </div>
    `;
    
    document.body.appendChild(modal);
  }

  // ===== PUBLIC API =====
  refresh() {
    // Removed: updateMetrics - no automatic value increases
    // Only refresh visual animations if needed
    this.animateSalesCards();
  }

  destroy() {
    this.isInitialized = false;
    this.animationQueue = [];
  }
}

// Initialize dashboard enhancements
const dashboardEnhancements = new DashboardEnhancements();

// Export for global access
window.DashboardEnhancements = dashboardEnhancements;
