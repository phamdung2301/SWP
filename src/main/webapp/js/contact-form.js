// Contact Form Handler
(function() {
    'use strict';

    // Google Apps Script Web App URL
    const SCRIPT_URL = 'https://script.google.com/macros/s/AKfycbxW4C0svj2o15Mdg11ZpgQDbnmYK54ZASaf58Eph9lPYJ3vq7EngUgJ_rysPOdr8amyXw/exec';

    // Get URL parameter
    function getUrlParameter(name) {
        name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
        const regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
        const results = regex.exec(location.search);
        return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
    }

    // Show form based on type
    function showForm(type) {
        // Hide all forms
        document.querySelectorAll('.form-container').forEach(form => {
            form.style.display = 'none';
        });

        // Remove active class from all nav items
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active-form');
        });

        // Show selected form and activate nav
        const formId = type + '-form';
        const navId = 'nav-' + type;
        
        const formElement = document.getElementById(formId);
        const navElement = document.getElementById(navId);

        if (formElement) {
            formElement.style.display = 'block';
        }
        if (navElement) {
            navElement.classList.add('active-form');
        }
    }

    // Initialize form display
    const formType = getUrlParameter('type') || 'email';
    showForm(formType);

    // Handle navigation clicks
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            const type = getUrlParameter('type', href) || href.split('type=')[1];
            if (type) {
                showForm(type);
                // Update URL without reload
                const newUrl = window.location.pathname + '?type=' + type;
                window.history.pushState({path: newUrl}, '', newUrl);
            }
        });
    });

    // Helper function to get URL parameter from a URL string
    function getUrlParameter(name, url) {
        if (!url) url = window.location.href;
        name = name.replace(/[\[\]]/g, '\\$&');
        const regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
        const results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, ' '));
    }

    // Form submission handler
    document.querySelectorAll('.contact-form').forEach(form => {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            const submitBtn = this.querySelector('.btn-submit');
            const originalBtnText = submitBtn.innerHTML;

            // Disable submit button and show loading
            submitBtn.disabled = true;
            submitBtn.classList.add('loading');
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';

            // Get form data
            const formData = new FormData(this);
            const data = {};
            
            // Convert FormData to object
            for (let [key, value] of formData.entries()) {
                data[key] = value;
            }

            // Add timestamp
            data.timestamp = new Date().toISOString();

            try {
                // Send to Google Apps Script
                const response = await fetch(SCRIPT_URL, {
                    method: 'POST',
                    mode: 'no-cors', // Important for Google Apps Script
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(data)
                });

                // Since we're using no-cors mode, we can't read the response
                // Assume success if no error is thrown
                console.log('Form submitted successfully');
                
                // Hide form and show success message
                document.querySelectorAll('.form-container').forEach(container => {
                    container.style.display = 'none';
                });
                document.getElementById('success-message').style.display = 'block';

                // Reset form
                this.reset();

            } catch (error) {
                console.error('Error submitting form:', error);
                
                // Show error message
                alert('Sorry, there was an error submitting your message. Please try again or contact us directly at liteflow.team@fpt.edu.vn');
                
                // Re-enable submit button
                submitBtn.disabled = false;
                submitBtn.classList.remove('loading');
                submitBtn.innerHTML = originalBtnText;
            }
        });
    });

    // Form validation
    document.querySelectorAll('.contact-form input, .contact-form select, .contact-form textarea').forEach(field => {
        field.addEventListener('blur', function() {
            validateField(this);
        });
    });

    function validateField(field) {
        const formGroup = field.closest('.form-group');
        
        // Remove existing error
        formGroup.classList.remove('error');
        const existingError = formGroup.querySelector('.error-message');
        if (existingError) {
            existingError.remove();
        }

        // Check if field is required and empty
        if (field.hasAttribute('required') && !field.value.trim()) {
            showError(formGroup, 'This field is required');
            return false;
        }

        // Validate email
        if (field.type === 'email' && field.value.trim()) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(field.value)) {
                showError(formGroup, 'Please enter a valid email address');
                return false;
            }
        }

        return true;
    }

    function showError(formGroup, message) {
        formGroup.classList.add('error');
        const errorDiv = document.createElement('span');
        errorDiv.className = 'error-message';
        errorDiv.textContent = message;
        formGroup.appendChild(errorDiv);
    }

    // Handle browser back/forward buttons
    window.addEventListener('popstate', function() {
        const formType = getUrlParameter('type') || 'email';
        showForm(formType);
    });

})();
