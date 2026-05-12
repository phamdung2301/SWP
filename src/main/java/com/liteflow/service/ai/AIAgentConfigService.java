package com.liteflow.service.ai;

import com.liteflow.dao.ai.AIAgentConfigurationDAO;
import com.liteflow.model.ai.AIAgentConfiguration;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing AI Agent configurations
 */
public class AIAgentConfigService {
    
    private final AIAgentConfigurationDAO configDAO;
    
    public AIAgentConfigService() {
        this.configDAO = new AIAgentConfigurationDAO();
    }
    
    /**
     * Get all configurations grouped by category
     */
    public Map<String, List<AIAgentConfiguration>> getAllConfigs() {
        List<AIAgentConfiguration> allConfigs = configDAO.findAll();
        return allConfigs.stream()
            .collect(Collectors.groupingBy(AIAgentConfiguration::getCategory));
    }
    
    /**
     * Get configurations by category
     */
    public List<AIAgentConfiguration> getConfigsByCategory(String category) {
        return configDAO.findByCategory(category);
    }
    
    /**
     * Get configuration by key
     */
    public AIAgentConfiguration getConfigByKey(String key) {
        return configDAO.findByKey(key);
    }
    
    /**
     * Update a single configuration
     */
    public boolean updateConfig(String key, String value, UUID updatedBy) {
        // Validate value
        AIAgentConfiguration config = configDAO.findByKey(key);
        if (config == null) {
            System.err.println("❌ Config not found: " + key);
            return false;
        }
        
        if (!validateConfig(key, value)) {
            System.err.println("❌ Invalid value for config: " + key + " = " + value);
            return false;
        }
        
        return configDAO.updateValue(key, value, updatedBy);
    }
    
    /**
     * Update multiple configurations
     */
    public Map<String, Boolean> updateConfigs(Map<String, String> updates, UUID updatedBy) {
        Map<String, Boolean> results = new HashMap<>();
        
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (validateConfig(key, value)) {
                boolean success = configDAO.updateValue(key, value, updatedBy);
                results.put(key, success);
            } else {
                System.err.println("❌ Invalid value for config: " + key + " = " + value);
                results.put(key, false);
            }
        }
        
        return results;
    }
    
    /**
     * Reset configuration to default value
     */
    public boolean resetToDefault(String key) {
        return configDAO.resetToDefault(key);
    }
    
    /**
     * Reset all configurations in a category to default
     */
    public boolean resetCategoryToDefault(String category) {
        return configDAO.resetCategoryToDefault(category);
    }
    
    /**
     * Validate configuration value
     */
    public boolean validateConfig(String key, String value) {
        AIAgentConfiguration config = configDAO.findByKey(key);
        if (config == null) {
            return false;
        }
        
        String configType = config.getConfigType();
        
        // Validate based on type
        switch (configType) {
            case "INTEGER":
                try {
                    int intValue = Integer.parseInt(value);
                    if (config.getMinValue() != null) {
                        int min = Integer.parseInt(config.getMinValue());
                        if (intValue < min) return false;
                    }
                    if (config.getMaxValue() != null) {
                        int max = Integer.parseInt(config.getMaxValue());
                        if (intValue > max) return false;
                    }
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
                
            case "DECIMAL":
                try {
                    double decimalValue = Double.parseDouble(value);
                    if (config.getMinValue() != null) {
                        double min = Double.parseDouble(config.getMinValue());
                        if (decimalValue < min) return false;
                    }
                    if (config.getMaxValue() != null) {
                        double max = Double.parseDouble(config.getMaxValue());
                        if (decimalValue > max) return false;
                    }
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
                
            case "BOOLEAN":
                return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
                
            case "TIME":
                // Format: HH:mm
                return value.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
                
            case "STRING":
            case "JSON":
            case "CRON":
                // Basic validation - not empty
                return value != null && !value.trim().isEmpty();
                
            default:
                return false;
        }
    }
    
    /**
     * Get integer configuration value
     */
    public int getIntConfig(String key, int defaultValue) {
        return configDAO.getIntValue(key, defaultValue);
    }
    
    /**
     * Get string configuration value
     */
    public String getStringConfig(String key, String defaultValue) {
        return configDAO.getStringValue(key, defaultValue);
    }
    
    /**
     * Get boolean configuration value
     */
    public boolean getBooleanConfig(String key, boolean defaultValue) {
        return configDAO.getBooleanValue(key, defaultValue);
    }
    
    /**
     * Get decimal configuration value
     */
    public double getDecimalConfig(String key, double defaultValue) {
        return configDAO.getDecimalValue(key, defaultValue);
    }
    
    /**
     * Get JSON configuration value
     */
    public JSONObject getJSONConfig(String key, JSONObject defaultValue) {
        try {
            AIAgentConfiguration config = configDAO.findByKey(key);
            if (config == null || config.getConfigValue() == null || config.getConfigValue().trim().isEmpty()) {
                return defaultValue;
            }
            
            String jsonString = config.getConfigValue().trim();
            if (jsonString.equals("{}") || jsonString.isEmpty()) {
                return defaultValue;
            }
            
            return new JSONObject(jsonString);
        } catch (Exception e) {
            System.err.println("❌ Error parsing JSON config for key '" + key + "': " + e.getMessage());
            e.printStackTrace();
            return defaultValue;
        }
    }
    
    /**
     * Get JSON configuration value as Map
     */
    public Map<String, String> getJSONConfigAsMap(String key, Map<String, String> defaultValue) {
        try {
            JSONObject json = getJSONConfig(key, null);
            if (json == null) {
                return defaultValue;
            }
            
            Map<String, String> result = new HashMap<>();
            for (String jsonKey : json.keySet()) {
                result.put(jsonKey, json.getString(jsonKey));
            }
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error converting JSON config to Map for key '" + key + "': " + e.getMessage());
            e.printStackTrace();
            return defaultValue;
        }
    }
    
    /**
     * Get all configurations as JSON (for API response)
     */
    public JSONObject getAllConfigsAsJSON() {
        JSONObject result = new JSONObject();
        Map<String, List<AIAgentConfiguration>> configsByCategory = getAllConfigs();
        
        for (Map.Entry<String, List<AIAgentConfiguration>> entry : configsByCategory.entrySet()) {
            String category = entry.getKey();
            List<AIAgentConfiguration> configs = entry.getValue();
            
            JSONObject categoryObj = new JSONObject();
            for (AIAgentConfiguration config : configs) {
                JSONObject configObj = new JSONObject();
                configObj.put("configID", config.getConfigID().toString());
                configObj.put("configKey", config.getConfigKey());
                configObj.put("configValue", config.getConfigValue());
                configObj.put("configType", config.getConfigType());
                configObj.put("displayName", config.getDisplayName());
                configObj.put("description", config.getDescription());
                configObj.put("minValue", config.getMinValue());
                configObj.put("maxValue", config.getMaxValue());
                configObj.put("defaultValue", config.getDefaultValue());
                configObj.put("isActive", config.getIsActive());
                configObj.put("isDefaultValue", config.isDefaultValue());
                
                categoryObj.put(config.getConfigKey(), configObj);
            }
            
            result.put(category, categoryObj);
        }
        
        return result;
    }
    
    /**
     * Get configurations by category as JSON
     */
    public JSONObject getConfigsByCategoryAsJSON(String category) {
        JSONObject result = new JSONObject();
        List<AIAgentConfiguration> configs = getConfigsByCategory(category);
        
        for (AIAgentConfiguration config : configs) {
            JSONObject configObj = new JSONObject();
            configObj.put("configID", config.getConfigID().toString());
            configObj.put("configKey", config.getConfigKey());
            configObj.put("configValue", config.getConfigValue());
            configObj.put("configType", config.getConfigType());
            configObj.put("displayName", config.getDisplayName());
            configObj.put("description", config.getDescription());
            configObj.put("minValue", config.getMinValue());
            configObj.put("maxValue", config.getMaxValue());
            configObj.put("defaultValue", config.getDefaultValue());
            configObj.put("isActive", config.getIsActive());
            configObj.put("isDefaultValue", config.isDefaultValue());
            
            result.put(config.getConfigKey(), configObj);
        }
        
        return result;
    }
}

