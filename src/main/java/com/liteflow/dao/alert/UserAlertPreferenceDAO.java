package com.liteflow.dao.alert;

import com.liteflow.model.alert.UserAlertPreference;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DAO for User Alert Preference operations
 */
public class UserAlertPreferenceDAO {
    
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");
    
    /**
     * Get all user preferences
     */
    public List<UserAlertPreference> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserAlertPreference> query = em.createQuery(
                "SELECT uap FROM UserAlertPreference uap ORDER BY uap.createdAt DESC",
                UserAlertPreference.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get preference by ID
     */
    public UserAlertPreference getById(UUID preferenceID) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(UserAlertPreference.class, preferenceID);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get preference by user ID
     */
    public UserAlertPreference getByUserId(UUID userID) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserAlertPreference> query = em.createQuery(
                "SELECT uap FROM UserAlertPreference uap WHERE uap.userID = :userID",
                UserAlertPreference.class
            );
            query.setParameter("userID", userID);
            List<UserAlertPreference> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get or create preference for user
     */
    public UserAlertPreference getOrCreateForUser(UUID userID) {
        UserAlertPreference preference = getByUserId(userID);
        if (preference == null) {
            preference = new UserAlertPreference(userID);
            preference.enableDefaultNotifications();
            insert(preference);
        }
        return preference;
    }
    
    /**
     * Get users with notifications enabled
     */
    public List<UserAlertPreference> getUsersWithNotificationsEnabled() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserAlertPreference> query = em.createQuery(
                "SELECT uap FROM UserAlertPreference uap WHERE uap.enableNotifications = true",
                UserAlertPreference.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get users with Slack enabled
     */
    public List<UserAlertPreference> getUsersWithSlackEnabled() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserAlertPreference> query = em.createQuery(
                "SELECT uap FROM UserAlertPreference uap " +
                "WHERE uap.enableNotifications = true AND uap.enableSlack = true",
                UserAlertPreference.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get users with Telegram enabled
     */
    public List<UserAlertPreference> getUsersWithTelegramEnabled() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserAlertPreference> query = em.createQuery(
                "SELECT uap FROM UserAlertPreference uap " +
                "WHERE uap.enableNotifications = true AND uap.enableTelegram = true",
                UserAlertPreference.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get users with Email enabled
     */
    public List<UserAlertPreference> getUsersWithEmailEnabled() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserAlertPreference> query = em.createQuery(
                "SELECT uap FROM UserAlertPreference uap " +
                "WHERE uap.enableNotifications = true AND uap.enableEmail = true",
                UserAlertPreference.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get users with quiet hours enabled
     */
    public List<UserAlertPreference> getUsersWithQuietHoursEnabled() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserAlertPreference> query = em.createQuery(
                "SELECT uap FROM UserAlertPreference uap WHERE uap.quietHoursEnabled = true",
                UserAlertPreference.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Insert new preference
     */
    public boolean insert(UserAlertPreference preference) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(preference);
            em.getTransaction().commit();
            System.out.println("✅ UserAlertPreference inserted for user: " + preference.getUserID());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to insert UserAlertPreference: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Update preference
     */
    public boolean update(UserAlertPreference preference) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            preference.setUpdatedAt(LocalDateTime.now());
            em.merge(preference);
            em.getTransaction().commit();
            System.out.println("✅ UserAlertPreference updated for user: " + preference.getUserID());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to update UserAlertPreference: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Enable/disable notifications for user
     */
    public boolean setNotificationsEnabled(UUID userID, boolean enabled) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            UserAlertPreference preference = getByUserId(userID);
            if (preference == null) {
                preference = new UserAlertPreference(userID);
                em.persist(preference);
            }
            preference.setEnableNotifications(enabled);
            preference.setUpdatedAt(LocalDateTime.now());
            em.merge(preference);
            em.getTransaction().commit();
            System.out.println("✅ Notifications " + (enabled ? "enabled" : "disabled") + " for user: " + userID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to set notifications enabled: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Update channel settings
     */
    public boolean updateChannelSettings(UUID userID, boolean slack, boolean telegram, boolean email, boolean inApp) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            UserAlertPreference preference = getByUserId(userID);
            if (preference == null) {
                preference = new UserAlertPreference(userID);
                em.persist(preference);
            }
            preference.setEnableSlack(slack);
            preference.setEnableTelegram(telegram);
            preference.setEnableEmail(email);
            preference.setEnableInApp(inApp);
            preference.setUpdatedAt(LocalDateTime.now());
            em.merge(preference);
            em.getTransaction().commit();
            System.out.println("✅ Channel settings updated for user: " + userID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to update channel settings: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Link Telegram account
     */
    public boolean linkTelegramAccount(UUID userID, String telegramUserID) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            UserAlertPreference preference = getOrCreateForUser(userID);
            preference.setTelegramUserID(telegramUserID);
            preference.setEnableTelegram(true);
            preference.setUpdatedAt(LocalDateTime.now());
            em.merge(preference);
            em.getTransaction().commit();
            System.out.println("✅ Telegram linked for user: " + userID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to link Telegram: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Link Slack account
     */
    public boolean linkSlackAccount(UUID userID, String slackUserID) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            UserAlertPreference preference = getOrCreateForUser(userID);
            preference.setSlackUserID(slackUserID);
            preference.setEnableSlack(true);
            preference.setUpdatedAt(LocalDateTime.now());
            em.merge(preference);
            em.getTransaction().commit();
            System.out.println("✅ Slack linked for user: " + userID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to link Slack: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Delete preference
     */
    public boolean delete(UUID preferenceID) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            UserAlertPreference preference = em.find(UserAlertPreference.class, preferenceID);
            if (preference != null) {
                em.remove(preference);
            }
            em.getTransaction().commit();
            System.out.println("✅ UserAlertPreference deleted: " + preferenceID);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Failed to delete UserAlertPreference: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get count of users with notifications enabled
     */
    public long getCountWithNotificationsEnabled() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(uap) FROM UserAlertPreference uap WHERE uap.enableNotifications = true",
                Long.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}


