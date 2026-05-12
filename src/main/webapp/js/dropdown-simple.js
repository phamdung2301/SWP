/**
 * Simple and Reliable Dropdown Fix
 * Ensures dropdown menus work without complex timing issues
 */

console.log('🔧 Simple Dropdown Fix Loading...');

// Wait for DOM to be fully ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 DOM Ready - Simple dropdown init...');
    
    // Try multiple times to find dropdowns
    let attempts = 0;
    const maxAttempts = 5;
    
    function initializeDropdowns() {
        attempts++;
        console.log(`🔄 Attempt ${attempts} to find dropdowns...`);
        
        const dropdowns = document.querySelectorAll('.nav-item.dropdown');
        console.log(`Found ${dropdowns.length} dropdowns`);
        
        if (dropdowns.length === 0 && attempts < maxAttempts) {
            console.log('⏳ No dropdowns found, retrying in 200ms...');
            setTimeout(initializeDropdowns, 200);
            return;
        }
        
        if (dropdowns.length === 0) {
            console.warn('⚠️ No dropdowns found after maximum attempts');
            return;
        }
        
        console.log('✅ Dropdowns found, initializing...');
        
        dropdowns.forEach((dropdown, index) => {
            const toggle = dropdown.querySelector('.nav-link.dropdown-toggle, .dropdown-toggle');
            const menu = dropdown.querySelector('.dropdown-menu');
            
            if (!toggle || !menu) {
                console.warn(`⚠️ Dropdown ${index} missing toggle or menu`);
                return;
            }
            
            console.log(`🔧 Initializing dropdown ${index}`);
            
            // Remove any existing event listeners to prevent duplicates
            const newToggle = toggle.cloneNode(true);
            toggle.parentNode.replaceChild(newToggle, toggle);
            
            let hoverTimeout;
            
            // Click handler
            newToggle.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                console.log(`🖱️ Click on dropdown ${index}`);
                
                // Close all other dropdowns
                dropdowns.forEach((otherDropdown, otherIndex) => {
                    if (otherIndex !== index) {
                        otherDropdown.classList.remove('show', 'active');
                    }
                });
                
                // Toggle current dropdown
                const isOpen = dropdown.classList.contains('show');
                
                if (isOpen) {
                    dropdown.classList.remove('show', 'active');
                    console.log(`❌ Closed dropdown ${index}`);
                } else {
                    dropdown.classList.add('show', 'active');
                    console.log(`✅ Opened dropdown ${index}`);
                }
            });
            
            // Hover handlers with delay
            dropdown.addEventListener('mouseenter', function() {
                if (window.innerWidth > 768) {
                    console.log(`🖱️ Mouse enter dropdown ${index}`);
                    if (hoverTimeout) {
                        clearTimeout(hoverTimeout);
                        hoverTimeout = null;
                    }
                    dropdown.classList.add('show', 'active');
                }
            });
            
            dropdown.addEventListener('mouseleave', function() {
                if (window.innerWidth > 768) {
                    console.log(`🖱️ Mouse leave dropdown ${index}`);
                    hoverTimeout = setTimeout(function() {
                        dropdown.classList.remove('show', 'active');
                        console.log(`⏰ Delayed close dropdown ${index}`);
                    }, 300);
                }
            });
        });
        
        // Global click outside handler
        document.addEventListener('click', function(e) {
            if (!e.target.closest('.nav-item.dropdown')) {
                console.log('🖱️ Clicked outside, closing all dropdowns');
                dropdowns.forEach(dropdown => {
                    dropdown.classList.remove('show', 'active');
                });
            }
        });
        
        // Escape key handler
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                console.log('⌨️ Escape pressed, closing all dropdowns');
                dropdowns.forEach(dropdown => {
                    dropdown.classList.remove('show', 'active');
                });
            }
        });
        
        console.log('✅ Simple dropdown initialization complete');
    }
    
    // Start initialization
    initializeDropdowns();
});
