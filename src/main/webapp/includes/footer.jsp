<%@ page contentType="text/html; charset=UTF-8" %>
    </div>
  </main>

  <!-- UTF-8 Standard Footer -->
  <footer class="utf8-footer">
    <div class="footer-container">
      <div class="footer-main">
        <div class="footer-brand">
          <div class="footer-logo">
     <!--       <img src="${pageContext.request.contextPath}/img/logo.png" alt="LiteFlow Logo" class="logo-image"> -->
            <span class="brand-name">LiteFlow</span>
          </div>
          <p class="brand-description">
            Hệ thống quản lý bán hàng toàn diện, giúp doanh nghiệp tối ưu hóa quy trình kinh doanh và tăng trưởng bền vững.
          </p>
          <div class="social-links">
            <a href="#" class="social-link facebook" data-tooltip="Facebook">
              <i class='bx bxl-facebook'></i>
            </a>
            <a href="#" class="social-link twitter" data-tooltip="Twitter">
              <i class='bx bxl-twitter'></i>
            </a>
            <a href="#" class="social-link linkedin" data-tooltip="LinkedIn">
              <i class='bx bxl-linkedin'></i>
            </a>
            <a href="#" class="social-link youtube" data-tooltip="YouTube">
              <i class='bx bxl-youtube'></i>
            </a>
          </div>
        </div>
        
        <div class="footer-sections">
          <div class="footer-column">
            <h4 class="column-title">Sản phẩm</h4>
            <ul class="footer-menu">
              <li><a href="${pageContext.request.contextPath}/products" class="footer-link">Hàng hóa</a></li>
              <li><a href="${pageContext.request.contextPath}/cashier" class="footer-link">Thu ngân</a></li>
              <li><a href="${pageContext.request.contextPath}/report/revenue" class="footer-link">Báo cáo</a></li>
              <li><a href="${pageContext.request.contextPath}/procurement/dashboard" class="footer-link">Mua sắm</a></li>
              <li><a href="${pageContext.request.contextPath}/ai-agent-config" class="footer-link">AI Agent</a></li>
            </ul>
          </div>
          
          <div class="footer-column">
            <h4 class="column-title">Hỗ trợ</h4>
            <ul class="footer-menu">
              <li><a href="${pageContext.request.contextPath}/auth/support.jsp" class="footer-link">Trung tâm hỗ trợ</a></li>
              <li><a href="${pageContext.request.contextPath}/auth/customer-care.jsp" class="footer-link">Chăm sóc khách hàng</a></li>
              <li><a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=email" class="footer-link">Liên hệ</a></li>
              <li><a href="https://github.com/VuxDucGiang/LiteFlow" target="_blank" class="footer-link">GitHub Repository</a></li>
              <li><a href="https://github.com/VuxDucGiang/LiteFlow/wiki" target="_blank" class="footer-link">Tài liệu hướng dẫn</a></li>
            </ul>
          </div>
          
          <div class="footer-column">
            <h4 class="column-title">Công ty</h4>
            <ul class="footer-menu">
              <li><a href="https://fpt.edu.vn" target="_blank" class="footer-link">Trường Đại học FPT</a></li>
              <li><a href="${pageContext.request.contextPath}/auth/terms.jsp" class="footer-link">Điều khoản sử dụng</a></li>
              <li><a href="https://github.com/VuxDucGiang/LiteFlow/issues" target="_blank" class="footer-link">Báo cáo lỗi</a></li>
              <li><a href="${pageContext.request.contextPath}/auth/customer-care.jsp#team" class="footer-link">Đội ngũ phát triển</a></li>
            </ul>
          </div>
          
          <div class="footer-column">
            <h4 class="column-title">Liên hệ</h4>
            <div class="contact-info">
              <div class="contact-row">
                <i class='bx bx-envelope'></i>
                <span>liteflow.team@fpt.edu.vn</span>
              </div>
              <div class="contact-row">
                <i class='bx bxl-github'></i>
                <span><a href="https://github.com/VuxDucGiang/LiteFlow" target="_blank" style="color: inherit; text-decoration: none;">GitHub Project</a></span>
              </div>
              <div class="contact-row">
                <i class='bx bx-map'></i>
                <span>Trường Đại học FPT, Việt Nam</span>
              </div>
              <div class="contact-row">
                <i class='bx bx-time'></i>
                <span>Thứ 2 - Thứ 6: 8:00 - 17:00 (GMT+7)</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
     
    </div>
       <div class="footer-bottom">
        <div class="footer-bottom-content">
          <div class="copyright">
            <p>&copy; 2025 LiteFlow Development Team - Trường Đại học FPT. Tất cả quyền được bảo lưu.</p>
          </div>
          <div class="legal-links">
            <a href="${pageContext.request.contextPath}/auth/terms.jsp" class="legal-link">Điều khoản sử dụng</a>
            <a href="${pageContext.request.contextPath}/auth/customer-care.jsp" class="legal-link">Chăm sóc khách hàng</a>
            <a href="https://github.com/VuxDucGiang/LiteFlow" target="_blank" class="legal-link">Chính sách bảo mật</a>
          </div>
        </div>
      </div>
  </footer>

</div>

<!-- LiteFlow UI Enhancement Scripts -->
<script src="${pageContext.request.contextPath}/js/performance-system.js"></script>
<script src="${pageContext.request.contextPath}/js/ui-enhancements.js"></script>

<script>
  // Simple and effective dropdown functionality
  document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Dropdown script loaded');
    
    // Handle dropdown menus
    const dropdowns = document.querySelectorAll('.nav-item.dropdown');
    console.log('Found dropdowns:', dropdowns.length);
    
    dropdowns.forEach((dropdown, index) => {
      const dropdownToggle = dropdown.querySelector('.nav-link.dropdown-toggle');
      const dropdownMenu = dropdown.querySelector('.dropdown-menu');
      
      console.log(`Dropdown ${index}:`, {
        dropdown: dropdown,
        toggle: dropdownToggle,
        menu: dropdownMenu
      });
      
      if (dropdownToggle && dropdownMenu) {
        // Click event handler
        dropdownToggle.addEventListener('click', function(e) {
          e.preventDefault();
          e.stopPropagation();
          
          console.log('🖱️ Dropdown clicked:', dropdown);
          
          // Close all other dropdowns first
          document.querySelectorAll('.nav-item.dropdown').forEach(otherDropdown => {
            if (otherDropdown !== dropdown) {
              otherDropdown.classList.remove('show', 'active');
            }
          });
          
          // Toggle current dropdown
          const isOpen = dropdown.classList.contains('show');
          
          if (isOpen) {
            dropdown.classList.remove('show', 'active');
            console.log('❌ Closing dropdown');
          } else {
            dropdown.classList.add('show', 'active');
            console.log('✅ Opening dropdown');
          }
        });
        
        // Hover events for desktop with delay
        let footerHoverTimeout;
        
        dropdown.addEventListener('mouseenter', function() {
          if (window.innerWidth > 768) {
            console.log('🖱️ Hover enter:', dropdown);
            // Clear any pending close timeout
            if (footerHoverTimeout) {
              clearTimeout(footerHoverTimeout);
              footerHoverTimeout = null;
            }
            dropdown.classList.add('show', 'active');
          }
        });
        
        dropdown.addEventListener('mouseleave', function() {
          if (window.innerWidth > 768) {
            console.log('🖱️ Hover leave:', dropdown);
            // Add delay before closing
            footerHoverTimeout = setTimeout(function() {
              dropdown.classList.remove('show', 'active');
              console.log('⏰ Footer delayed close dropdown');
            }, 300); // 300ms delay
          }
        });
      }
    });
    
    // Close dropdowns when clicking outside
    document.addEventListener('click', function(e) {
      if (!e.target.closest('.nav-item.dropdown')) {
        console.log('🖱️ Clicked outside, closing all dropdowns');
        document.querySelectorAll('.nav-item.dropdown').forEach(dropdown => {
          dropdown.classList.remove('show', 'active');
        });
      }
    });
    
    // Handle dropdown item clicks
    document.querySelectorAll('.dropdown-item').forEach(item => {
      item.addEventListener('click', function() {
        console.log('🖱️ Dropdown item clicked:', this.textContent);
        // Close parent dropdown
        const dropdown = this.closest('.nav-item.dropdown');
        if (dropdown) {
          dropdown.classList.remove('show', 'active');
        }
      });
    });
    
    // Escape key handler
    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape') {
        console.log('⌨️ Escape pressed, closing dropdowns');
        document.querySelectorAll('.nav-item.dropdown').forEach(dropdown => {
          dropdown.classList.remove('show', 'active');
        });
      }
    });
  });

  // Navigation item interactions
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', function() {
      document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
      this.classList.add('active');
    });
  });

  // Language selector dropdown
  document.querySelector('.language-selector')?.addEventListener('click', function() {
    // Add dropdown functionality here
    console.log('Language selector clicked');
  });


  // Footer Enhancements
  document.addEventListener('DOMContentLoaded', function() {
    // Animate footer elements on scroll
    const footerElements = document.querySelectorAll('.footer-column, .footer-brand');
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('animate-fade-in-up');
        }
      });
    }, { threshold: 0.1 });

    footerElements.forEach(el => observer.observe(el));

    // Social links hover effects
    document.querySelectorAll('.social-link').forEach(link => {
      link.addEventListener('mouseenter', function() {
        this.style.transform = 'translateY(-3px) scale(1.1)';
        this.style.boxShadow = '0 8px 25px rgba(0, 128, 255, 0.3)';
      });
      
      link.addEventListener('mouseleave', function() {
        this.style.transform = 'translateY(0) scale(1)';
        this.style.boxShadow = '0 4px 15px rgba(0, 128, 255, 0.2)';
      });
    });

    // Footer links hover effects
    document.querySelectorAll('.footer-link').forEach(link => {
      link.addEventListener('mouseenter', function() {
        this.style.color = '#00c6ff';
        this.style.transform = 'translateX(5px)';
      });
      
      link.addEventListener('mouseleave', function() {
        this.style.color = '#ccc';
        this.style.transform = 'translateX(0)';
      });
    });

    // Contact info hover effects
    document.querySelectorAll('.contact-row').forEach(row => {
      row.addEventListener('mouseenter', function() {
        this.style.backgroundColor = 'rgba(0, 128, 255, 0.1)';
        this.style.transform = 'translateX(10px)';
      });
      
      row.addEventListener('mouseleave', function() {
        this.style.backgroundColor = 'transparent';
        this.style.transform = 'translateX(0)';
      });
    });

    // Tooltip functionality
    document.querySelectorAll('[data-tooltip]').forEach(element => {
      element.addEventListener('mouseenter', function() {
        const tooltip = document.createElement('div');
        tooltip.className = 'tooltip';
        tooltip.textContent = this.dataset.tooltip;
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
          bottom: 100%;
          left: 50%;
          transform: translateX(-50%);
          margin-bottom: 8px;
        `;
        
        this.style.position = 'relative';
        this.appendChild(tooltip);
        
        setTimeout(() => {
          tooltip.style.opacity = '1';
        }, 10);
      });
      
      element.addEventListener('mouseleave', function() {
        const tooltip = this.querySelector('.tooltip');
        if (tooltip) {
          tooltip.style.opacity = '0';
          setTimeout(() => {
            tooltip.remove();
          }, 300);
        }
      });
    });

    // Newsletter subscription (if added)
    const newsletterForm = document.querySelector('.newsletter-form');
    if (newsletterForm) {
      newsletterForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const email = this.querySelector('input[type="email"]').value;
        
        // Show success animation
        const button = this.querySelector('button');
        const originalText = button.textContent;
        button.textContent = 'Đang gửi...';
        button.style.background = 'linear-gradient(135deg, #00c6ff, #0080FF)';
        
        setTimeout(() => {
          button.textContent = 'Đã đăng ký!';
          button.style.background = 'linear-gradient(135deg, #7d2ae8, #00c6ff)';
          
          setTimeout(() => {
            button.textContent = originalText;
            button.style.background = 'linear-gradient(135deg, #0080FF, #00c6ff)';
            this.reset();
          }, 2000);
        }, 1000);
      });
    }
  });
</script>
</body>
</html>
