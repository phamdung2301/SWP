/**
 * Simple Dropdown Fix for LiteFlow
 * Ensures dropdown menus work properly
 */

console.log('🔧 Dropdown Fix Script Loading...');

// Wait for DOM to be ready
document.addEventListener('DOMContentLoaded', function() {
  console.log('🚀 DOM Ready - Initializing dropdowns...');
  
  // Find all dropdowns
  const dropdowns = document.querySelectorAll('.nav-item.dropdown');
  console.log('Found dropdowns:', dropdowns.length);
  
  if (dropdowns.length === 0) {
    console.warn('⚠️ No dropdowns found!');
    return;
  }
  
  // Process each dropdown
  dropdowns.forEach((dropdown, index) => {
    const toggle = dropdown.querySelector('.nav-link.dropdown-toggle');
    const menu = dropdown.querySelector('.dropdown-menu');
    
    console.log(`Dropdown ${index}:`, {
      element: dropdown,
      toggle: toggle,
      menu: menu,
      hasToggle: !!toggle,
      hasMenu: !!menu
    });
    
    if (!toggle || !menu) {
      console.warn(`⚠️ Dropdown ${index} missing toggle or menu`);
      return;
    }
    
    // Click handler
    toggle.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();
      
      console.log('🖱️ Click on dropdown:', dropdown);
      
      // Close all other dropdowns
      dropdowns.forEach(otherDropdown => {
        if (otherDropdown !== dropdown) {
          otherDropdown.classList.remove('show', 'active');
        }
      });
      
      // Toggle current dropdown
      const isOpen = dropdown.classList.contains('show');
      
      if (isOpen) {
        dropdown.classList.remove('show', 'active');
        console.log('❌ Closed dropdown');
      } else {
        dropdown.classList.add('show', 'active');
        console.log('✅ Opened dropdown');
      }
    });
    
    // Hover handlers for desktop with delay
    let hoverTimeout;
    
    dropdown.addEventListener('mouseenter', function() {
      if (window.innerWidth > 768) {
        console.log('🖱️ Mouse enter:', dropdown);
        // Clear any pending close timeout
        if (hoverTimeout) {
          clearTimeout(hoverTimeout);
          hoverTimeout = null;
        }
        dropdown.classList.add('show', 'active');
      }
    });
    
    dropdown.addEventListener('mouseleave', function() {
      if (window.innerWidth > 768) {
        console.log('🖱️ Mouse leave:', dropdown);
        // Add delay before closing
        hoverTimeout = setTimeout(function() {
          dropdown.classList.remove('show', 'active');
          console.log('⏰ Delayed close dropdown');
        }, 300); // 300ms delay
      }
    });
  });
  
  // Close dropdowns when clicking outside
  document.addEventListener('click', function(e) {
    if (!e.target.closest('.nav-item.dropdown')) {
      console.log('🖱️ Clicked outside, closing dropdowns');
      dropdowns.forEach(dropdown => {
        dropdown.classList.remove('show', 'active');
      });
    }
  });
  
  // Escape key handler
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      console.log('⌨️ Escape pressed');
      dropdowns.forEach(dropdown => {
        dropdown.classList.remove('show', 'active');
      });
    }
  });
  
  // Handle dropdown item clicks
  document.querySelectorAll('.dropdown-item').forEach(item => {
    item.addEventListener('click', function() {
      console.log('🖱️ Dropdown item clicked:', this.textContent.trim());
      const dropdown = this.closest('.nav-item.dropdown');
      if (dropdown) {
        dropdown.classList.remove('show', 'active');
      }
    });
  });
  
  console.log('✅ Dropdown initialization complete');
});

// Also try to initialize after a short delay
setTimeout(function() {
  console.log('🔄 Re-initializing dropdowns after delay...');
  
  const dropdowns = document.querySelectorAll('.nav-item.dropdown');
  dropdowns.forEach((dropdown, index) => {
    const toggle = dropdown.querySelector('.nav-link.dropdown-toggle');
    const menu = dropdown.querySelector('.dropdown-menu');
    
    if (toggle && menu) {
      // Force add event listeners again
      toggle.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        
        console.log('🖱️ Delayed click on dropdown:', dropdown);
        
        // Close all other dropdowns
        dropdowns.forEach(otherDropdown => {
          if (otherDropdown !== dropdown) {
            otherDropdown.classList.remove('show', 'active');
          }
        });
        
        // Toggle current dropdown
        const isOpen = dropdown.classList.contains('show');
        
        if (isOpen) {
          dropdown.classList.remove('show', 'active');
          console.log('❌ Delayed closed dropdown');
        } else {
          dropdown.classList.add('show', 'active');
          console.log('✅ Delayed opened dropdown');
        }
      });
      
      // Add hover handlers for delayed initialization too
      let delayedHoverTimeout;
      
      dropdown.addEventListener('mouseenter', function() {
        if (window.innerWidth > 768) {
          console.log('🖱️ Delayed mouse enter:', dropdown);
          // Clear any pending close timeout
          if (delayedHoverTimeout) {
            clearTimeout(delayedHoverTimeout);
            delayedHoverTimeout = null;
          }
          dropdown.classList.add('show', 'active');
        }
      });
      
      dropdown.addEventListener('mouseleave', function() {
        if (window.innerWidth > 768) {
          console.log('🖱️ Delayed mouse leave:', dropdown);
          // Add delay before closing
          delayedHoverTimeout = setTimeout(function() {
            dropdown.classList.remove('show', 'active');
            console.log('⏰ Delayed close dropdown (delayed init)');
          }, 300); // 300ms delay
        }
      });
    }
  });
}, 500);