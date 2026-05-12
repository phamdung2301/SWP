package com.liteflow.service;

import com.liteflow.dao.CompanyInfoDAO;
import com.liteflow.model.CompanyInfo;
import com.liteflow.util.EnvConfigUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing company information
 * Singleton pattern: Only one CompanyInfo record exists
 */
public class CompanyInfoService {
    
    private final CompanyInfoDAO companyInfoDAO;
    
    public CompanyInfoService() {
        this.companyInfoDAO = new CompanyInfoDAO();
    }
    
    /**
     * Get company info with TaxCode from .env
     * @return Map containing all company info including TaxCode
     */
    public Map<String, Object> getCompanyInfoWithTaxCode() {
        Map<String, Object> result = new HashMap<>();
        
        CompanyInfo companyInfo = companyInfoDAO.getCompanyInfo();
        
        if (companyInfo != null) {
            result.put("companyID", companyInfo.getCompanyID() != null ? companyInfo.getCompanyID().toString() : null);
            result.put("name", companyInfo.getName());
            result.put("address", companyInfo.getAddress());
            result.put("phone", companyInfo.getPhone());
            result.put("email", companyInfo.getEmail());
            result.put("createdAt", companyInfo.getCreatedAt());
            result.put("updatedAt", companyInfo.getUpdatedAt());
        } else {
            // Return default values if not found
            result.put("name", "LiteFlow Restaurant");
            result.put("address", "123 Nguyễn Huệ, Quận 1, TP.HCM");
            result.put("phone", "1900-1234");
            result.put("email", "procurement@liteflow.com");
        }
        
        // Add TaxCode from .env (read-only)
        String taxCode = EnvConfigUtil.getMaSoThue();
        result.put("taxCode", taxCode);
        
        return result;
    }
    
    /**
     * Get company info entity (without TaxCode)
     * @return CompanyInfo or null
     */
    public CompanyInfo getCompanyInfo() {
        return companyInfoDAO.getCompanyInfo();
    }
    
    /**
     * Update company info
     * @param data Map containing name, address, phone, email
     * @return true if successful
     */
    public boolean updateCompanyInfo(Map<String, String> data) {
        CompanyInfo companyInfo = companyInfoDAO.getCompanyInfo();
        
        if (companyInfo == null) {
            // Create new if doesn't exist
            companyInfo = new CompanyInfo();
        }
        
        if (data.containsKey("name")) {
            companyInfo.setName(data.get("name"));
        }
        if (data.containsKey("address")) {
            companyInfo.setAddress(data.get("address"));
        }
        if (data.containsKey("phone")) {
            companyInfo.setPhone(data.get("phone"));
        }
        if (data.containsKey("email")) {
            companyInfo.setEmail(data.get("email"));
        }
        
        if (companyInfo.getCompanyID() == null) {
            return companyInfoDAO.insert(companyInfo);
        } else {
            return companyInfoDAO.update(companyInfo);
        }
    }
    
    /**
     * Create default company info if not exists
     * @return true if created successfully
     */
    public boolean createDefaultCompanyInfo() {
        CompanyInfo existing = companyInfoDAO.getCompanyInfo();
        if (existing != null) {
            return false; // Already exists
        }
        
        CompanyInfo companyInfo = new CompanyInfo();
        companyInfo.setName("LiteFlow Restaurant");
        companyInfo.setAddress("123 Nguyễn Huệ, Quận 1, TP.HCM");
        companyInfo.setPhone("1900-1234");
        companyInfo.setEmail("procurement@liteflow.com");
        
        return companyInfoDAO.insert(companyInfo);
    }
}

