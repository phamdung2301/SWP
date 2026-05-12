/**
 * AI Agent Configuration Page JavaScript
 * Handles loading, displaying, saving, and resetting configurations
 */

class AIAgentConfig {
    constructor() {
        this.currentCategory = 'STOCK_ALERT';
        this.configs = {};
        this.originalConfigs = {};
        this.hasChanges = false;
        this.init();
    }
    
    init() {
        this.setupEventListeners();
        this.loadAllConfigs();
    }
    
    setupEventListeners() {
        // Tab buttons
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const category = e.currentTarget.getAttribute('data-category');
                this.switchTab(category);
            });
        });
        
        // Save button
        document.getElementById('saveBtn')?.addEventListener('click', () => {
            this.saveConfigs();
        });
        
        // Reset button
        document.getElementById('resetBtn')?.addEventListener('click', () => {
            this.resetCategory();
        });
        
        // Cancel button
        document.getElementById('cancelBtn')?.addEventListener('click', () => {
            this.cancelChanges();
        });
    }
    
    async loadAllConfigs() {
        try {
            this.showLoading();
            
            const response = await fetch(`${this.getContextPath()}/api/ai-agent-config`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            if (data.success) {
                this.configs = data;
                this.originalConfigs = JSON.parse(JSON.stringify(data));
                this.renderCurrentCategory();
                this.hideLoading();
                this.showContent();
            } else {
                throw new Error(data.error || 'Failed to load configurations');
            }
        } catch (error) {
            console.error('Error loading configs:', error);
            this.showError('Không thể tải cấu hình: ' + error.message);
            this.hideLoading();
        }
    }
    
    switchTab(category) {
        // Update active tab
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelector(`[data-category="${category}"]`)?.classList.add('active');
        
        this.currentCategory = category;
        this.renderCurrentCategory();
    }
    
    renderCurrentCategory() {
        const contentDiv = document.getElementById('configContent');
        if (!contentDiv) return;
        
        const categoryConfigs = this.configs[this.currentCategory];
        if (!categoryConfigs) {
            contentDiv.innerHTML = '<div class="empty-state"><i class="bx bx-info-circle"></i><p>Không có cấu hình nào</p></div>';
            return;
        }
        
        let html = '<div class="config-section">';
        html += `<div class="config-section-title">`;
        html += `<i class='bx ${this.getCategoryIcon(this.currentCategory)}'></i>`;
        html += `${this.getCategoryName(this.currentCategory)}</div>`;
        
        for (const [key, config] of Object.entries(categoryConfigs)) {
            html += this.renderConfigItem(key, config);
        }
        
        html += '</div>';
        contentDiv.innerHTML = html;
        
        // Attach event listeners to inputs
        this.attachInputListeners();
    }
    
    renderConfigItem(key, config) {
        const isDefault = config.isDefaultValue;
        const value = config.configValue;
        const type = config.configType;
        
        // Special handling for supplier mapping (async, will be loaded separately)
        if (key === 'po.supplier_mapping' && type === 'JSON') {
            // Return placeholder, will be replaced by async renderSupplierMapping
            const placeholderId = `supplier-mapping-${key.replace(/\./g, '-')}`;
            setTimeout(() => this.loadSupplierMapping(key, config, placeholderId), 0);
            return `<div id="${placeholderId}" style="padding: 20px; text-align: center; color: #999;">Đang tải...</div>`;
        }
        
        let inputHtml = '';
        
        if (type === 'BOOLEAN') {
            inputHtml = `
                <label class="toggle-switch">
                    <input type="checkbox" data-key="${key}" ${value === 'true' ? 'checked' : ''}>
                    <span class="toggle-slider"></span>
                </label>
            `;
        } else if (type === 'TIME') {
            inputHtml = `<input type="time" class="config-input time" data-key="${key}" value="${value}">`;
        } else if (type === 'INTEGER' || type === 'DECIMAL') {
            const inputType = type === 'DECIMAL' ? 'number' : 'number';
            const step = type === 'DECIMAL' ? '0.1' : '1';
            inputHtml = `
                <input type="${inputType}" 
                       class="config-input number" 
                       data-key="${key}" 
                       value="${value}"
                       step="${step}"
                       min="${config.minValue || ''}"
                       max="${config.maxValue || ''}">
            `;
        } else if (type === 'JSON') {
            // For other JSON types, show as textarea
            inputHtml = `<textarea class="config-input json" data-key="${key}" rows="4" style="width: 100%; font-family: monospace;">${this.escapeHtml(value)}</textarea>`;
        } else {
            inputHtml = `<input type="text" class="config-input string" data-key="${key}" value="${this.escapeHtml(value)}">`;
        }
        
        return `
            <div class="config-item" data-key="${key}">
                <div class="config-item-header">
                    <div class="config-item-label">
                        <span class="label">${this.escapeHtml(config.displayName)}</span>
                        ${config.description ? `<span class="description">${this.escapeHtml(config.description)}</span>` : ''}
                    </div>
                    <div class="config-item-value">
                        ${isDefault ? '<span class="default-badge is-default">Mặc định</span>' : '<span class="default-badge">Đã thay đổi</span>'}
                    </div>
                </div>
                <div class="config-input-wrapper">
                    ${inputHtml}
                    <div class="validation-error"></div>
                </div>
            </div>
        `;
    }
    
    /**
     * Load and render supplier mapping UI (async)
     */
    async loadSupplierMapping(key, config, containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        try {
            const html = await this.renderSupplierMapping(key, config);
            container.outerHTML = html;
            
            // Attach event listeners after rendering
            this.attachSupplierMappingListeners(key);
        } catch (error) {
            console.error('Error loading supplier mapping:', error);
            container.innerHTML = '<div style="padding: 20px; color: #ef4444;">Lỗi khi tải dữ liệu. Vui lòng thử lại.</div>';
        }
    }
    
    /**
     * Render supplier mapping UI (special case for po.supplier_mapping)
     */
    async renderSupplierMapping(key, config) {
        const isDefault = config.isDefaultValue;
        const value = config.configValue;
        
        // Parse JSON value
        let mapping = {};
        try {
            if (value && value !== '{}' && value.trim() !== '') {
                mapping = JSON.parse(value);
            }
        } catch (e) {
            console.error('Error parsing supplier mapping:', e);
            mapping = {};
        }
        
        // Fetch suppliers and categories
        const [suppliers, categories] = await Promise.all([
            this.fetchSuppliers(),
            this.fetchCategories()
        ]);
        
        // Build table HTML
        // Format mới: {"ProductName": "SupplierID"}
        // Cần lấy category của từng product để hiển thị
        let tableRows = '';
        const mappingEntries = Object.entries(mapping);
        
        if (mappingEntries.length === 0) {
            tableRows = '<tr><td colspan="4" style="text-align: center; color: #999; padding: 20px;">Chưa có ánh xạ nào. Nhấn "Thêm ánh xạ" để bắt đầu.</td></tr>';
        } else {
            // Load products để lấy category của từng product
            const productPromises = mappingEntries.map(([productName, supplierId]) => 
                this.getProductCategory(productName)
            );
            const productCategories = await Promise.all(productPromises);
            
            // Load products for each category found
            const categoryProductsMap = new Map();
            for (let i = 0; i < mappingEntries.length; i++) {
                const categoryName = productCategories[i];
                if (categoryName && !categoryProductsMap.has(categoryName)) {
                    const products = await this.fetchProductsByCategory(categoryName);
                    categoryProductsMap.set(categoryName, products);
                }
            }
            
            mappingEntries.forEach(([productName, supplierId], index) => {
                const categoryName = productCategories[index] || '';
                const supplier = suppliers.find(s => s.supplierID === supplierId);
                const products = categoryName ? (categoryProductsMap.get(categoryName) || []) : [];
                
                tableRows += `
                    <tr data-index="${index}" data-product-name="${this.escapeHtml(productName)}">
                        <td>
                            <select class="supplier-mapping-category" data-index="${index}" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                                ${this.buildCategoryOptions(categories, categoryName)}
                            </select>
                        </td>
                        <td>
                            <select class="supplier-mapping-product" data-index="${index}" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;" ${categoryName ? '' : 'disabled'}>
                                ${this.buildProductOptions(products, productName)}
                            </select>
                        </td>
                        <td>
                            <select class="supplier-mapping-supplier" data-index="${index}" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                                ${this.buildSupplierOptions(suppliers, supplierId)}
                            </select>
                        </td>
                        <td style="text-align: center;">
                            <button type="button" class="btn-remove-mapping" data-index="${index}" style="background: #ef4444; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer;">Xóa</button>
                        </td>
                    </tr>
                `;
            });
        }
        
        return `
            <div class="config-item supplier-mapping-item" data-key="${key}">
                <div class="config-item-header">
                    <div class="config-item-label">
                        <span class="label">${this.escapeHtml(config.displayName)}</span>
                        ${config.description ? `<span class="description">${this.escapeHtml(config.description)}</span>` : ''}
                    </div>
                    <div class="config-item-value">
                        ${isDefault ? '<span class="default-badge is-default">Mặc định</span>' : '<span class="default-badge">Đã thay đổi</span>'}
                    </div>
                </div>
                <div class="config-input-wrapper">
                    <div class="supplier-mapping-container" style="margin-top: 12px;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                            <h4 style="margin: 0; font-size: 14px; font-weight: 600;">Ánh xạ sản phẩm → Nhà cung cấp</h4>
                            <button type="button" class="btn-add-mapping" style="background: #3b82f6; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 500;">➕ Thêm ánh xạ</button>
                        </div>
                        <table class="supplier-mapping-table" style="width: 100%; border-collapse: collapse; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;">
                            <thead>
                                <tr style="background: #f9fafb;">
                                    <th style="padding: 12px; text-align: left; font-weight: 600; font-size: 13px; border-bottom: 1px solid #e5e7eb;">Danh mục</th>
                                    <th style="padding: 12px; text-align: left; font-weight: 600; font-size: 13px; border-bottom: 1px solid #e5e7eb;">Sản phẩm</th>
                                    <th style="padding: 12px; text-align: left; font-weight: 600; font-size: 13px; border-bottom: 1px solid #e5e7eb;">Nhà cung cấp</th>
                                    <th style="padding: 12px; text-align: center; font-weight: 600; font-size: 13px; border-bottom: 1px solid #e5e7eb; width: 100px;">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody id="supplierMappingTableBody">
                                ${tableRows}
                            </tbody>
                        </table>
                        <input type="hidden" class="config-input json" data-key="${key}" value="${this.escapeHtml(value)}">
                    </div>
                    <div class="validation-error"></div>
                </div>
            </div>
        `;
    }
    
    buildCategoryOptions(categories, selectedCategory) {
        let options = '<option value="">-- Chọn danh mục --</option>';
        categories.forEach(cat => {
            const selected = cat.name === selectedCategory ? 'selected' : '';
            options += `<option value="${this.escapeHtml(cat.name)}" ${selected}>${this.escapeHtml(cat.name)}</option>`;
        });
        return options;
    }
    
    buildSupplierOptions(suppliers, selectedSupplierId) {
        let options = '<option value="">-- Chọn nhà cung cấp --</option>';
        suppliers.forEach(supplier => {
            const selected = supplier.supplierID === selectedSupplierId ? 'selected' : '';
            options += `<option value="${this.escapeHtml(supplier.supplierID)}" ${selected}>${this.escapeHtml(supplier.name)}</option>`;
        });
        return options;
    }
    
    buildProductOptions(products, selectedProductName) {
        if (!products || products.length === 0) {
            return `<option value="${this.escapeHtml(selectedProductName || '')}">${this.escapeHtml(selectedProductName || '-- Chọn danh mục trước --')}</option>`;
        }
        let options = '<option value="">-- Chọn sản phẩm --</option>';
        products.forEach(product => {
            const selected = product.productName === selectedProductName ? 'selected' : '';
            options += `<option value="${this.escapeHtml(product.productName)}" ${selected}>${this.escapeHtml(product.productName)}</option>`;
        });
        return options;
    }
    
    async fetchSuppliers() {
        try {
            const response = await fetch(`${this.getContextPath()}/api/ai-agent-config?resource=suppliers`);
            const data = await response.json();
            if (data.success && data.suppliers) {
                return data.suppliers;
            }
            return [];
        } catch (error) {
            console.error('Error fetching suppliers:', error);
            return [];
        }
    }
    
    async fetchCategories() {
        try {
            const response = await fetch(`${this.getContextPath()}/api/ai-agent-config?resource=categories`);
            const data = await response.json();
            if (data.success && data.categories) {
                return data.categories;
            }
            return [];
        } catch (error) {
            console.error('Error fetching categories:', error);
            return [];
        }
    }
    
    async fetchProductsByCategory(categoryName) {
        try {
            if (!categoryName || categoryName.trim() === '') {
                return [];
            }
            const response = await fetch(`${this.getContextPath()}/api/ai-agent-config?resource=products&category=${encodeURIComponent(categoryName)}`);
            const data = await response.json();
            if (data.success && data.products) {
                return data.products;
            }
            return [];
        } catch (error) {
            console.error('Error fetching products by category:', error);
            return [];
        }
    }
    
    async getProductCategory(productName) {
        try {
            // Load tất cả categories và tìm category chứa product này
            const categories = await this.fetchCategories();
            
            // Thử tìm category của product bằng cách query từng category
            for (const category of categories) {
                const products = await this.fetchProductsByCategory(category.name);
                const found = products.find(p => p.productName === productName);
                if (found) {
                    return category.name;
                }
            }
            
            return '';
        } catch (error) {
            console.error('Error getting product category:', error);
            return '';
        }
    }
    
    attachSupplierMappingListeners(key) {
        const container = document.querySelector(`[data-key="${key}"]`);
        if (!container) return;
        
        // Add mapping button
        const addBtn = container.querySelector('.btn-add-mapping');
        if (addBtn) {
            addBtn.addEventListener('click', () => this.addSupplierMappingRow(key));
        }
        
        // Remove mapping buttons
        container.querySelectorAll('.btn-remove-mapping').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const index = parseInt(e.target.getAttribute('data-index'));
                this.removeSupplierMappingRow(key, index);
            });
        });
        
        // Category dropdown - load products when changed
        container.querySelectorAll('.supplier-mapping-category').forEach(select => {
            select.addEventListener('change', async (e) => {
                const index = parseInt(e.target.getAttribute('data-index'));
                const categoryName = e.target.value;
                await this.loadProductsForRow(key, index, categoryName);
                this.updateSupplierMappingValue(key);
            });
        });
        
        // Product and supplier dropdowns
        container.querySelectorAll('.supplier-mapping-product, .supplier-mapping-supplier').forEach(select => {
            select.addEventListener('change', () => this.updateSupplierMappingValue(key));
        });
    }
    
    async addSupplierMappingRow(key) {
        const [suppliers, categories] = await Promise.all([
            this.fetchSuppliers(),
            this.fetchCategories()
        ]);
        
        const tbody = document.querySelector(`[data-key="${key}"] #supplierMappingTableBody`);
        if (!tbody) return;
        
        // Clear empty message if exists
        if (tbody.querySelector('td[colspan="4"]')) {
            tbody.innerHTML = '';
        }
        
        const index = tbody.children.length;
        const row = document.createElement('tr');
        row.setAttribute('data-index', index);
        row.innerHTML = `
            <td>
                <select class="supplier-mapping-category" data-index="${index}" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                    ${this.buildCategoryOptions(categories, '')}
                </select>
            </td>
            <td>
                <select class="supplier-mapping-product" data-index="${index}" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;" disabled>
                    <option value="">-- Chọn danh mục trước --</option>
                </select>
            </td>
            <td>
                <select class="supplier-mapping-supplier" data-index="${index}" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                    ${this.buildSupplierOptions(suppliers, '')}
                </select>
            </td>
            <td style="text-align: center;">
                <button type="button" class="btn-remove-mapping" data-index="${index}" style="background: #ef4444; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer;">Xóa</button>
            </td>
        `;
        tbody.appendChild(row);
        
        // Re-attach listeners
        this.attachSupplierMappingListeners(key);
    }
    
    async loadProductsForRow(key, index, categoryName) {
        const row = document.querySelector(`[data-key="${key}"] tr[data-index="${index}"]`);
        if (!row) return;
        
        const productSelect = row.querySelector('.supplier-mapping-product');
        if (!productSelect) return;
        
        if (!categoryName || categoryName.trim() === '') {
            productSelect.innerHTML = '<option value="">-- Chọn danh mục trước --</option>';
            productSelect.disabled = true;
            return;
        }
        
        // Load products for this category
        const products = await this.fetchProductsByCategory(categoryName);
        productSelect.innerHTML = '<option value="">-- Chọn sản phẩm --</option>';
        
        products.forEach(product => {
            const option = document.createElement('option');
            option.value = product.productName;
            option.textContent = product.productName;
            productSelect.appendChild(option);
        });
        
        productSelect.disabled = false;
    }
    
    removeSupplierMappingRow(key, index) {
        const tbody = document.querySelector(`[data-key="${key}"] #supplierMappingTableBody`);
        if (!tbody) return;
        
        const row = tbody.querySelector(`tr[data-index="${index}"]`);
        if (row) {
            row.remove();
        }
        
        // Re-index remaining rows
        Array.from(tbody.children).forEach((row, newIndex) => {
            row.setAttribute('data-index', newIndex);
            row.querySelectorAll('[data-index]').forEach(el => {
                el.setAttribute('data-index', newIndex);
            });
        });
        
        // Show empty message if no rows
        if (tbody.children.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #999; padding: 20px;">Chưa có ánh xạ nào. Nhấn "Thêm ánh xạ" để bắt đầu.</td></tr>';
        }
        
        // Update value
        this.updateSupplierMappingValue(key);
    }
    
    updateSupplierMappingValue(key) {
        const tbody = document.querySelector(`[data-key="${key}"] #supplierMappingTableBody`);
        const hiddenInput = document.querySelector(`[data-key="${key}"] input.config-input.json`);
        
        if (!tbody || !hiddenInput) return;
        
        // Format mới: {"ProductName": "SupplierID"}
        const mapping = {};
        const rows = Array.from(tbody.querySelectorAll('tr'));
        
        rows.forEach(row => {
            // Skip empty message row
            if (row.querySelector('td[colspan]')) return;
            
            const productSelect = row.querySelector('.supplier-mapping-product');
            const supplierSelect = row.querySelector('.supplier-mapping-supplier');
            
            if (productSelect && supplierSelect && 
                productSelect.value && supplierSelect.value) {
                // Format mới: ProductName -> SupplierID
                mapping[productSelect.value] = supplierSelect.value;
            }
        });
        
        const jsonValue = JSON.stringify(mapping);
        hiddenInput.value = jsonValue;
        
        // Update config value
        this.updateConfigValue(key, jsonValue);
        
        // Update default badge
        const category = this.currentCategory;
        if (this.configs[category] && this.configs[category][key]) {
            const isDefault = jsonValue === this.configs[category][key].defaultValue;
            this.configs[category][key].isDefaultValue = isDefault;
            this.updateDefaultBadge(key);
        }
    }
    
    attachInputListeners() {
        document.querySelectorAll('.config-input, .toggle-switch input').forEach(input => {
            // Skip supplier mapping hidden input (handled separately)
            if (input.classList.contains('supplier-mapping') || 
                (input.classList.contains('json') && input.closest('.supplier-mapping-item'))) {
                return;
            }
            
            input.addEventListener('change', (e) => {
                const key = e.target.getAttribute('data-key');
                let value = e.target.value;
                
                if (e.target.type === 'checkbox') {
                    value = e.target.checked ? 'true' : 'false';
                }
                
                this.updateConfigValue(key, value);
                this.validateInput(e.target);
            });
            
            input.addEventListener('input', (e) => {
                this.validateInput(e.target);
            });
        });
    }
    
    updateConfigValue(key, value) {
        const category = this.currentCategory;
        if (this.configs[category] && this.configs[category][key]) {
            this.configs[category][key].configValue = value;
            this.configs[category][key].isDefaultValue = 
                value === this.configs[category][key].defaultValue;
            this.hasChanges = true;
            this.updateDefaultBadge(key);
        }
    }
    
    updateDefaultBadge(key) {
        const item = document.querySelector(`[data-key="${key}"]`);
        if (!item) return;
        
        const badge = item.querySelector('.default-badge');
        const config = this.configs[this.currentCategory][key];
        
        if (badge && config) {
            if (config.isDefaultValue) {
                badge.textContent = 'Mặc định';
                badge.classList.add('is-default');
            } else {
                badge.textContent = 'Đã thay đổi';
                badge.classList.remove('is-default');
            }
        }
    }
    
    validateInput(input) {
        const key = input.getAttribute('data-key');
        const value = input.value;
        const config = this.configs[this.currentCategory][key];
        if (!config) return;
        
        const item = input.closest('.config-item');
        const errorDiv = item.querySelector('.validation-error');
        
        // Remove previous validation
        item.classList.remove('has-error');
        errorDiv.textContent = '';
        
        // Validate based on type
        let isValid = true;
        let errorMessage = '';
        
        if (config.configType === 'INTEGER') {
            const intValue = parseInt(value);
            if (isNaN(intValue)) {
                isValid = false;
                errorMessage = 'Giá trị phải là số nguyên';
            } else {
                if (config.minValue && intValue < parseInt(config.minValue)) {
                    isValid = false;
                    errorMessage = `Giá trị tối thiểu: ${config.minValue}`;
                }
                if (config.maxValue && intValue > parseInt(config.maxValue)) {
                    isValid = false;
                    errorMessage = `Giá trị tối đa: ${config.maxValue}`;
                }
            }
        } else if (config.configType === 'DECIMAL') {
            const decimalValue = parseFloat(value);
            if (isNaN(decimalValue)) {
                isValid = false;
                errorMessage = 'Giá trị phải là số';
            } else {
                if (config.minValue && decimalValue < parseFloat(config.minValue)) {
                    isValid = false;
                    errorMessage = `Giá trị tối thiểu: ${config.minValue}`;
                }
                if (config.maxValue && decimalValue > parseFloat(config.maxValue)) {
                    isValid = false;
                    errorMessage = `Giá trị tối đa: ${config.maxValue}`;
                }
            }
        } else if (config.configType === 'TIME') {
            if (!value.match(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/)) {
                isValid = false;
                errorMessage = 'Định dạng thời gian không hợp lệ (HH:mm)';
            }
        }
        
        if (!isValid) {
            item.classList.add('has-error');
            errorDiv.textContent = errorMessage;
        }
        
        return isValid;
    }
    
    async saveConfigs() {
        const category = this.currentCategory;
        const categoryConfigs = this.configs[category];
        if (!categoryConfigs) return;
        
        // Validate all inputs
        let allValid = true;
        document.querySelectorAll(`.config-item[data-key]`).forEach(item => {
            const input = item.querySelector('.config-input, .toggle-switch input');
            if (input && !this.validateInput(input)) {
                allValid = false;
            }
        });
        
        if (!allValid) {
            this.showError('Vui lòng sửa các lỗi trước khi lưu');
            return;
        }
        
        // Collect changes
        const updates = {};
        for (const [key, config] of Object.entries(categoryConfigs)) {
            const original = this.originalConfigs[category]?.[key];
            if (original && config.configValue !== original.configValue) {
                updates[key] = config.configValue;
            }
        }
        
        if (Object.keys(updates).length === 0) {
            this.showSuccess('Không có thay đổi nào để lưu');
            return;
        }
        
        try {
            this.showLoading();
            
            const response = await fetch(`${this.getContextPath()}/api/ai-agent-config`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    configs: updates
                })
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to save configurations');
            }
            
            const result = await response.json();
            if (result.success) {
                // Update original configs
                for (const key in updates) {
                    if (this.originalConfigs[category] && this.originalConfigs[category][key]) {
                        this.originalConfigs[category][key].configValue = updates[key];
                        this.originalConfigs[category][key].isDefaultValue = 
                            updates[key] === this.originalConfigs[category][key].defaultValue;
                    }
                }
                
                this.hasChanges = false;
                this.showSuccess('Đã lưu cấu hình thành công!');
                this.renderCurrentCategory(); // Refresh to update badges
            } else {
                throw new Error(result.error || 'Failed to save');
            }
        } catch (error) {
            console.error('Error saving configs:', error);
            this.showError('Không thể lưu cấu hình: ' + error.message);
        } finally {
            this.hideLoading();
        }
    }
    
    async resetCategory() {
        if (!confirm('Bạn có chắc muốn khôi phục tất cả cấu hình trong mục này về giá trị mặc định?')) {
            return;
        }
        
        try {
            this.showLoading();
            
            const response = await fetch(`${this.getContextPath()}/api/ai-agent-config`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    action: 'reset',
                    category: this.currentCategory
                })
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to reset configurations');
            }
            
            const result = await response.json();
            if (result.success) {
                // Reload configs
                await this.loadAllConfigs();
                this.showSuccess('Đã khôi phục về giá trị mặc định!');
            } else {
                throw new Error(result.error || 'Failed to reset');
            }
        } catch (error) {
            console.error('Error resetting configs:', error);
            this.showError('Không thể khôi phục: ' + error.message);
        } finally {
            this.hideLoading();
        }
    }
    
    cancelChanges() {
        if (this.hasChanges && !confirm('Bạn có chắc muốn hủy các thay đổi chưa lưu?')) {
            return;
        }
        
        // Restore from original
        this.configs = JSON.parse(JSON.stringify(this.originalConfigs));
        this.hasChanges = false;
        this.renderCurrentCategory();
        this.showSuccess('Đã hủy các thay đổi');
    }
    
    // UI Helper Methods
    showLoading() {
        document.getElementById('loadingIndicator')?.style.setProperty('display', 'block');
        document.getElementById('configContent')?.style.setProperty('display', 'none');
        document.querySelector('.config-actions')?.style.setProperty('display', 'none');
    }
    
    hideLoading() {
        document.getElementById('loadingIndicator')?.style.setProperty('display', 'none');
    }
    
    showContent() {
        document.getElementById('configContent')?.style.setProperty('display', 'block');
        document.querySelector('.config-actions')?.style.setProperty('display', 'flex');
    }
    
    showError(message) {
        const errorDiv = document.getElementById('errorMessage');
        if (errorDiv) {
            errorDiv.innerHTML = `<i class='bx bx-error-circle'></i> ${this.escapeHtml(message)}`;
            errorDiv.style.display = 'flex';
            setTimeout(() => {
                errorDiv.style.display = 'none';
            }, 5000);
        }
    }
    
    showSuccess(message) {
        const successDiv = document.getElementById('successMessage');
        if (successDiv) {
            successDiv.innerHTML = `<i class='bx bx-check-circle'></i> ${this.escapeHtml(message)}`;
            successDiv.style.display = 'flex';
            setTimeout(() => {
                successDiv.style.display = 'none';
            }, 3000);
        }
    }
    
    getCategoryIcon(category) {
        const icons = {
            'STOCK_ALERT': 'bx-package',
            'DEMAND_FORECAST': 'bx-trending-up',
            'PO_AUTO': 'bx-cart',
            'SUPPLIER_MAPPING': 'bx-link-alt',
            'GPT_SERVICE': 'bx-brain',
            'NOTIFICATION': 'bx-bell'
        };
        return icons[category] || 'bx-cog';
    }
    
    getCategoryName(category) {
        const names = {
            'STOCK_ALERT': 'Cảnh báo Tồn kho',
            'DEMAND_FORECAST': 'Dự báo Nhu cầu',
            'PO_AUTO': 'Tự động Đặt hàng',
            'SUPPLIER_MAPPING': 'Ánh xạ Nhà cung cấp',
            'GPT_SERVICE': 'GPT Service',
            'NOTIFICATION': 'Thông báo'
        };
        return names[category] || category;
    }
    
    getContextPath() {
        const meta = document.querySelector('meta[name="contextPath"]');
        return meta ? meta.getAttribute('content') : '';
    }
    
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize when DOM is ready (only if not in settings page)
// In settings page, initialization is handled by settings.js
document.addEventListener('DOMContentLoaded', () => {
    // Check if we're in the standalone ai-agent-config page
    const isStandalonePage = document.querySelector('.ai-config-container') && 
                              !document.querySelector('.settings-layout');
    
    if (isStandalonePage) {
        new AIAgentConfig();
    }
    // Otherwise, initialization will be handled by settings.js when section becomes active
});

