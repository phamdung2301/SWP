package com.liteflow.dao.ai;

import com.liteflow.model.ai.AIAgentConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DAO for AI Agent Configuration operations
 */
public class AIAgentConfigurationDAO {
    
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");
    
    /**
     * Get all configurations
     */
    public List<AIAgentConfiguration> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AIAgentConfiguration> query = em.createQuery(
                "SELECT c FROM AIAgentConfiguration c WHERE c.isActive = true ORDER BY c.category, c.configKey",
                AIAgentConfiguration.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get configurations by category
     */
    public List<AIAgentConfiguration> findByCategory(String category) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AIAgentConfiguration> query = em.createQuery(
                "SELECT c FROM AIAgentConfiguration c WHERE c.category = :category AND c.isActive = true ORDER BY c.configKey",
                AIAgentConfiguration.class
            );
            query.setParameter("category", category);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get configuration by key
     */
    public AIAgentConfiguration findByKey(String configKey) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AIAgentConfiguration> query = em.createQuery(
                "SELECT c FROM AIAgentConfiguration c WHERE c.configKey = :configKey",
                AIAgentConfiguration.class
            );
            query.setParameter("configKey", configKey);
            List<AIAgentConfiguration> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    /**
     * Save or update configuration
     */
    public boolean save(AIAgentConfiguration config) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            config.setUpdatedAt(LocalDateTime.now());
            if (config.getConfigID() == null) {
                em.persist(config);
            } else {
                em.merge(config);
            }
            em.getTransaction().commit();
            System.out.println("✅ AIAgentConfiguration saved: " + config.getConfigKey());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to save AIAgentConfiguration: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Save multiple configurations
     */
    public boolean saveAll(List<AIAgentConfiguration> configs) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            for (AIAgentConfiguration config : configs) {
                config.setUpdatedAt(LocalDateTime.now());
                if (config.getConfigID() == null) {
                    em.persist(config);
                } else {
                    em.merge(config);
                }
            }
            em.getTransaction().commit();
            System.out.println("✅ Saved " + configs.size() + " AIAgentConfigurations");
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to save AIAgentConfigurations: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Update configuration value
     */
    public boolean updateValue(String configKey, String configValue, UUID updatedBy) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            AIAgentConfiguration config = findByKey(configKey);
            if (config != null) {
                config.setConfigValue(configValue);
                config.setUpdatedBy(updatedBy);
                config.setUpdatedAt(LocalDateTime.now());
                em.merge(config);
            } else {
                em.getTransaction().rollback();
                return false;
            }
            em.getTransaction().commit();
            System.out.println("✅ Updated AIAgentConfiguration: " + configKey + " = " + configValue);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to update AIAgentConfiguration: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Reset configuration to default value
     */
    public boolean resetToDefault(String configKey) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            AIAgentConfiguration config = em.createQuery(
                "SELECT c FROM AIAgentConfiguration c WHERE c.configKey = :configKey",
                AIAgentConfiguration.class
            ).setParameter("configKey", configKey).getSingleResult();
            
            if (config != null) {
                config.setConfigValue(config.getDefaultValue());
                config.setUpdatedAt(LocalDateTime.now());
                em.merge(config);
            }
            em.getTransaction().commit();
            System.out.println("✅ Reset AIAgentConfiguration to default: " + configKey);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to reset AIAgentConfiguration: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Reset all configurations in a category to default
     */
    public boolean resetCategoryToDefault(String category) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<AIAgentConfiguration> configs = em.createQuery(
                "SELECT c FROM AIAgentConfiguration c WHERE c.category = :category",
                AIAgentConfiguration.class
            ).setParameter("category", category).getResultList();
            
            for (AIAgentConfiguration config : configs) {
                config.setConfigValue(config.getDefaultValue());
                config.setUpdatedAt(LocalDateTime.now());
                em.merge(config);
            }
            em.getTransaction().commit();
            System.out.println("✅ Reset " + configs.size() + " AIAgentConfigurations in category: " + category);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to reset category: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Helper: Get integer value
     */
    public int getIntValue(String key, int defaultValue) {
        AIAgentConfiguration config = findByKey(key);
        if (config != null && "INTEGER".equals(config.getConfigType())) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Helper: Get string value
     */
    public String getStringValue(String key, String defaultValue) {
        AIAgentConfiguration config = findByKey(key);
        if (config != null) {
            return config.getConfigValue();
        }
        return defaultValue;
    }
    
    /**
     * Helper: Get boolean value
     */
    public boolean getBooleanValue(String key, boolean defaultValue) {
        AIAgentConfiguration config = findByKey(key);
        if (config != null && "BOOLEAN".equals(config.getConfigType())) {
            return "true".equalsIgnoreCase(config.getConfigValue());
        }
        return defaultValue;
    }
    
    /**
     * Helper: Get decimal value
     */
    public double getDecimalValue(String key, double defaultValue) {
        AIAgentConfiguration config = findByKey(key);
        if (config != null && "DECIMAL".equals(config.getConfigType())) {
            try {
                return Double.parseDouble(config.getConfigValue());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Get count of configurations
     */
    public long getCount() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM AIAgentConfiguration c",
                Long.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get count by category
     */
    public long getCountByCategory(String category) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM AIAgentConfiguration c WHERE c.category = :category",
                Long.class
            );
            query.setParameter("category", category);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}

