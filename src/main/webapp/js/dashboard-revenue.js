/**
 * Dashboard Revenue Data Loader
 * Fetches and displays revenue data from API using Chart.js
 */

class DashboardRevenue {
  constructor() {
    this.currentTab = 'hour'; // Default to hourly view
    this.revenueData = null;
    this.chart = null;
    this.init();
  }

  init() {
    this.setupTabListeners();
    this.loadRevenueData();
  }

  setupTabListeners() {
    const tabs = document.querySelectorAll('.revenue-tabs .tab');
    tabs.forEach(tab => {
      tab.addEventListener('click', () => {
        // Remove active from all tabs
        tabs.forEach(t => t.classList.remove('active'));
        
        // Add active to clicked tab
        tab.classList.add('active');
        
        // Update current tab
        this.currentTab = tab.dataset.tab || 'hour';
        
        // Render table for selected tab
        this.renderTable();
      });
    });
  }

  async loadRevenueData() {
    try {
      // Get date range (last 30 days)
      const endDate = new Date();
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 30);
      
      const startDateStr = this.formatDate(startDate);
      const endDateStr = this.formatDate(endDate);
      
      // Get context path from page (set by JSP)
      const contextPath = window.CONTEXT_PATH || '';
      const baseUrl = contextPath.endsWith('/') ? contextPath.slice(0, -1) : contextPath;
      
      // Fetch data from API
      const url = `${baseUrl}/report/revenue?action=api&startDate=${startDateStr}&endDate=${endDateStr}`;
      
      console.log('📊 Loading revenue data from:', url);
      
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      console.log('✅ Revenue data loaded:', data);
      
      this.revenueData = data;
      this.renderTable();
      
    } catch (error) {
      console.error('❌ Error loading revenue data:', error);
      const container = document.querySelector('.revenue-chart-container');
      if (container) {
        container.innerHTML = '<div class="error-text">Không thể tải dữ liệu doanh thu</div>';
      }
    }
  }

  formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  renderTable() {
    const container = document.querySelector('.revenue-chart-container');
    if (!container) return;

    // Ensure canvas exists
    let canvas = document.getElementById('revenueChart');
    if (!canvas) {
      container.innerHTML = '<canvas id="revenueChart"></canvas>';
      canvas = document.getElementById('revenueChart');
    }

    if (!this.revenueData) {
      container.innerHTML = '<div class="loading-text">Đang tải dữ liệu...</div>';
      return;
    }

    // Destroy existing chart if exists
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }

    // Ensure canvas is back in container
    if (!container.querySelector('#revenueChart')) {
      container.innerHTML = '<canvas id="revenueChart"></canvas>';
      canvas = document.getElementById('revenueChart');
    }

    switch (this.currentTab) {
      case 'day':
        this.renderDailyChart(canvas);
        break;
      case 'hour':
        this.renderHourlyChart(canvas);
        break;
      case 'weekday':
        this.renderWeekdayChart(canvas);
        break;
      default:
        this.renderHourlyChart(canvas);
    }

    // Show error if chart was not created
    if (!this.chart) {
      container.innerHTML = '<div class="error-text">Không có dữ liệu để hiển thị</div>';
    }
  }

  renderDailyChart(canvas) {
    if (!this.revenueData.trendData) {
      return;
    }

    const dates = this.revenueData.trendData.dates || [];
    const revenues = this.revenueData.trendData.revenues || [];

    if (dates.length === 0) {
      return;
    }

    // Show last 7 days
    const startIndex = Math.max(0, dates.length - 7);
    const chartDates = dates.slice(startIndex);
    const chartRevenues = revenues.slice(startIndex);

    this.chart = new Chart(canvas, {
      type: 'line',
      data: {
        labels: chartDates,
        datasets: [{
          label: 'Doanh thu (VNĐ)',
          data: chartRevenues,
          borderColor: 'rgba(102, 126, 234, 1)',
          backgroundColor: 'rgba(102, 126, 234, 0.1)',
          borderWidth: 3,
          fill: true,
          tension: 0.4,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: 'rgba(102, 126, 234, 1)',
          pointBorderColor: '#fff',
          pointBorderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: 'top'
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                return this.formatCurrency(context.parsed.y);
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value) => {
                return this.formatCurrency(value, true);
              }
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.05)'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  }

  renderHourlyChart(canvas) {
    if (!this.revenueData.hourlyData) {
      return;
    }

    const hours = this.revenueData.hourlyData.hours || [];
    const revenues = this.revenueData.hourlyData.revenues || [];

    if (hours.length === 0) {
      return;
    }

    this.chart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: hours,
        datasets: [{
          label: 'Doanh thu (VNĐ)',
          data: revenues,
          backgroundColor: 'rgba(118, 75, 162, 0.8)',
          borderColor: 'rgba(118, 75, 162, 1)',
          borderWidth: 2,
          borderRadius: 8,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                return this.formatCurrency(context.parsed.y);
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value) => {
                return this.formatCurrency(value, true);
              }
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.05)'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  }

  renderWeekdayChart(canvas) {
    if (!this.revenueData.weekdayData) {
      return;
    }

    const weekdayNames = this.revenueData.weekdayData.weekdayNames || [];
    const revenues = this.revenueData.weekdayData.revenues || [];

    if (weekdayNames.length === 0) {
      return;
    }

    this.chart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: weekdayNames,
        datasets: [{
          label: 'Doanh thu (VNĐ)',
          data: revenues,
          backgroundColor: 'rgba(255, 152, 0, 0.8)',
          borderColor: 'rgba(255, 152, 0, 1)',
          borderWidth: 2,
          borderRadius: 8,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                return this.formatCurrency(context.parsed.y);
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value) => {
                return this.formatCurrency(value, true);
              }
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.05)'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  }

  formatCurrency(amount, short = false) {
    if (amount === 0 || !amount) return '0 ₫';
    
    if (short) {
      // Format ngắn gọn cho trục Y: 1.000.000 -> 1M, 500.000 -> 500K
      if (amount >= 1000000) {
        return (amount / 1000000).toFixed(1).replace('.0', '') + 'M ₫';
      } else if (amount >= 1000) {
        return (amount / 1000).toFixed(0) + 'K ₫';
      }
    }
    
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  }

  formatNumber(num) {
    if (num === 0 || !num) return '0';
    return new Intl.NumberFormat('vi-VN').format(num);
  }


  refresh() {
    this.loadRevenueData();
  }
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    window.dashboardRevenue = new DashboardRevenue();
  });
} else {
  window.dashboardRevenue = new DashboardRevenue();
}

// Export for global access
window.DashboardRevenue = DashboardRevenue;

