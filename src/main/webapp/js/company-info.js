/**
 * Company Info Management JavaScript
 * Handles loading and saving company information
 */

let companyInfoInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    // Initialize company info when section is shown
    const companyInfoSection = document.getElementById('company-info-section');
    if (companyInfoSection) {
        // Check if section is active
        if (companyInfoSection.classList.contains('active')) {
            initializeCompanyInfo();
        }
        
        // Watch for section activation
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    if (companyInfoSection.classList.contains('active') && !companyInfoInstance) {
                        initializeCompanyInfo();
                    }
                }
            });
        });
        
        observer.observe(companyInfoSection, { attributes: true });
    }
});

function initializeCompanyInfo() {
    if (companyInfoInstance) {
        return; // Already initialized
    }
    
    companyInfoInstance = true;
    console.log('✅ Company Info initialized');
    
    loadCompanyInfo();
    setupFormHandlers();
}

function loadCompanyInfo() {
    const contextPath = document.querySelector('meta[name="contextPath"]')?.content || '';
    const apiUrl = contextPath + '/api/company-info';
    
    // Show loading state
    const form = document.getElementById('companyInfoForm');
    if (form) {
        form.style.opacity = '0.6';
        form.style.pointerEvents = 'none';
    }
    
    fetch(apiUrl, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        if (data.success && data.data) {
            displayCompanyInfo(data.data);
        } else {
            showMessage('Không thể tải thông tin công ty', 'error');
        }
    })
    .catch(error => {
        console.error('Error loading company info:', error);
        showMessage('Lỗi khi tải thông tin công ty: ' + error.message, 'error');
    })
    .finally(() => {
        if (form) {
            form.style.opacity = '1';
            form.style.pointerEvents = 'auto';
        }
    });
}

function displayCompanyInfo(data) {
    // Populate form fields
    const nameInput = document.getElementById('companyName');
    const addressInput = document.getElementById('companyAddress');
    const phoneInput = document.getElementById('companyPhone');
    const emailInput = document.getElementById('companyEmail');
    const taxCodeInput = document.getElementById('companyTaxCode');
    
    if (nameInput && data.name) {
        nameInput.value = data.name;
    }
    if (addressInput && data.address) {
        addressInput.value = data.address;
    }
    if (phoneInput && data.phone) {
        phoneInput.value = data.phone;
    }
    if (emailInput && data.email) {
        emailInput.value = data.email;
    }
    if (taxCodeInput) {
        taxCodeInput.value = data.taxCode || 'Chưa cấu hình trong file .env';
        if (!data.taxCode) {
            taxCodeInput.style.color = '#999';
        }
    }
}

function setupFormHandlers() {
    const form = document.getElementById('companyInfoForm');
    const cancelBtn = document.getElementById('cancelCompanyInfoBtn');
    
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            saveCompanyInfo();
        });
    }
    
    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            loadCompanyInfo(); // Reload to discard changes
        });
    }
}

function saveCompanyInfo() {
    const form = document.getElementById('companyInfoForm');
    if (!form) {
        return;
    }
    
    const formData = new FormData(form);
    const data = {
        name: formData.get('name'),
        address: formData.get('address'),
        phone: formData.get('phone'),
        email: formData.get('email')
    };
    
    // Validate required fields
    if (!data.name || data.name.trim() === '') {
        showMessage('Vui lòng nhập tên công ty', 'error');
        return;
    }
    
    const contextPath = document.querySelector('meta[name="contextPath"]')?.content || '';
    const apiUrl = contextPath + '/api/company-info';
    
    // Show loading state
    const saveBtn = document.getElementById('saveCompanyInfoBtn');
    if (saveBtn) {
        saveBtn.disabled = true;
        saveBtn.innerHTML = '<i class="bx bx-loader-alt bx-spin"></i> Đang lưu...';
    }
    
    fetch(apiUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(result => {
        if (result.success) {
            showMessage('Lưu thông tin công ty thành công!', 'success');
            // Reload to get updated data
            setTimeout(() => {
                loadCompanyInfo();
            }, 1000);
        } else {
            showMessage('Lỗi khi lưu thông tin: ' + (result.message || 'Unknown error'), 'error');
        }
    })
    .catch(error => {
        console.error('Error saving company info:', error);
        showMessage('Lỗi khi lưu thông tin công ty: ' + error.message, 'error');
    })
    .finally(() => {
        if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.innerHTML = '<i class="bx bx-save"></i> Lưu thông tin';
        }
    });
}

function showMessage(message, type) {
    const messageBox = document.getElementById('companyInfoMessage');
    if (!messageBox) {
        return;
    }
    
    messageBox.textContent = message;
    messageBox.className = 'message-box';
    messageBox.style.display = 'block';
    
    if (type === 'success') {
        messageBox.style.backgroundColor = '#d4edda';
        messageBox.style.color = '#155724';
        messageBox.style.border = '1px solid #c3e6cb';
        messageBox.style.padding = '12px 16px';
        messageBox.style.borderRadius = '4px';
    } else if (type === 'error') {
        messageBox.style.backgroundColor = '#f8d7da';
        messageBox.style.color = '#721c24';
        messageBox.style.border = '1px solid #f5c6cb';
        messageBox.style.padding = '12px 16px';
        messageBox.style.borderRadius = '4px';
    }
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
        messageBox.style.display = 'none';
    }, 5000);
}

