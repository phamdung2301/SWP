package com.liteflow.service.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.liteflow.dao.BaseDAO.emf;
import com.liteflow.dao.GenericDAO;
import com.liteflow.model.auth.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.*;

/**
 * UserService: - Quản lý CRUD User - Quản lý Role/UserRole - Quản lý Session -
 * Đổi mật khẩu - Truy xuất meta JSON
 */
public class UserService {

    private final GenericDAO<User, UUID> userDao = new GenericDAO<>(User.class, UUID.class);
    private final GenericDAO<UserSession, UUID> sessionDao = new GenericDAO<>(UserSession.class, UUID.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final AuditService audit = new AuditService();

    // ==============================
    // CRUD User
    // ==============================
    /**
     * Lấy tất cả users.
     *
     * @return danh sách User
     */
    public List<User> getAllUsers() {
        return userDao.getAll();
    }

    /**
     * Tìm user theo ID.
     *
     * @param id UUID
     * @return Optional<User>
     */
    public Optional<User> getUserById(UUID id) {
        return Optional.ofNullable(userDao.findById(id));
    }

    /**
     * Tạo user mới.
     *
     * @param user entity User
     * @return true nếu insert thành công
     */
    public boolean createUser(User user) {
        return userDao.insert(user);
    }

    /**
     * Cập nhật user.
     *
     * @param user entity User
     * @return true nếu update thành công
     */
    public boolean updateUser(User user) {
        return userDao.update(user);
    }

    /**
     * Khóa tài khoản user.
     *
     * @param id UUID user
     * @param ip địa chỉ IP client
     * @return true nếu thành công
     */
    public boolean deactivateUser(UUID id, String ip) {
        User user = userDao.findById(id);
        if (user != null) {
            user.setIsActive(false);
            boolean ok = userDao.update(user);
            if (ok) {
                audit.log(user, AuditService.AuditAction.LOCK_ACCOUNT,
                        AuditService.ObjectType.USER, user.getUserID().toString(),
                        "User account locked", ip);
            }
            return ok;
        }
        return false;
    }

    /**
     * Mở khóa tài khoản user.
     *
     * @param id UUID user
     * @param ip địa chỉ IP client
     * @return true nếu thành công
     */
    public boolean activateUser(UUID id, String ip) {
        User user = userDao.findById(id);
        if (user != null) {
            user.setIsActive(true);
            boolean ok = userDao.update(user);
            if (ok) {
                audit.log(user, AuditService.AuditAction.UNLOCK_ACCOUNT,
                        AuditService.ObjectType.USER, user.getUserID().toString(),
                        "User account unlocked", ip);
            }
            return ok;
        }
        return false;
    }

    // ==============================
    // Search
    // ==============================
    /**
     * Tìm user theo email.
     *
     * @param email email
     * @return User hoặc null
     */
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        try {
        TypedQuery<User> q = em.createQuery(
            "SELECT u FROM User u WHERE LOWER(u.email) = :email", User.class);
        q.setParameter("email", email.trim().toLowerCase());
            List<User> result = q.getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Tìm user theo số điện thoại.
     *
     * @param phone số điện thoại
     * @return User hoặc null
     */
    public User findByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> q = em.createQuery(
                    "SELECT u FROM User u WHERE u.phone = :p", User.class);
            q.setParameter("p", phone);
            List<User> result = q.getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    // ==============================
    // Role Management
    // ==============================
    /**
     * /**
     * Gán role cho user.
     *
     * @param userId UUID user
     * @param roleName tên role
     * @param ip địa chỉ IP
     */
    public void assignRole(UUID userId, String roleName, String ip) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Role r = em.createQuery("SELECT r FROM Role r WHERE r.name = :n", Role.class)
                    .setParameter("n", roleName)
                    .getSingleResult();

            UserRole ur = new UserRole();
            ur.setUserId(userId);            // set từng field
            ur.setRoleId(r.getRoleID());     // set từng field
            ur.setAssignedAt(LocalDateTime.now());
            ur.setIsActive(true);
            em.persist(ur);

            em.getTransaction().commit();

            User u = userDao.findById(userId);
            audit.logRoleAssigned(u, roleName, ip);
        } finally {
            em.close();
        }
    }

    /**
     * Xóa role khỏi user.
     *
     * @param userId UUID user
     * @param roleName tên role
     * @param ip địa chỉ IP
     */
    public void removeRole(UUID userId, String roleName, String ip) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Role r = em.createQuery("SELECT r FROM Role r WHERE r.name = :n", Role.class)
                    .setParameter("n", roleName)
                    .getSingleResult();

            // Tìm UserRole theo IdClass
            UserRole ur = em.find(UserRole.class, new UserRoleId(userId, r.getRoleID()));
            if (ur != null) {
                em.remove(ur);

                User u = userDao.findById(userId);
                audit.logRoleRemoved(u, roleName, ip);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách tên roles của user.
     *
     * @param userId UUID user
     * @return danh sách role name
     */
    public List<String> getRoleNames(UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT r.name FROM Role r JOIN UserRole ur ON r.roleID = ur.id.roleId WHERE ur.id.userId = :u",
                    String.class)
                    .setParameter("u", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // ==============================
    // Password
    // ==============================
    /**
     * Đổi mật khẩu user.
     *
     * @param user User
     * @param newPassword mật khẩu mới
     * @param ip địa chỉ IP
     * @return true nếu đổi thành công
     */
    public boolean changePassword(User user, String newPassword, String ip) {
        if (user == null || newPassword == null || newPassword.isBlank()) {
            return false;
        }

    user.setPasswordHash(com.liteflow.util.PasswordUtil.hash(newPassword, 12));
        boolean ok = updateUser(user);
        if (ok) {
            audit.logPasswordChanged(user, ip);
        }
        return ok;
    }

    // ==============================
    // Sessions
    // ==============================
    /**
     * Tạo session mới cho user.
     *
     * @param user User
     * @param jwt token
     * @param device thông tin thiết bị
     * @param ip địa chỉ IP
     * @param expiresAt thời điểm hết hạn
     * @return UserSession
     */
    public UserSession createSession(User user, String jwt, String device, String ip, LocalDateTime expiresAt) {
        UserSession s = new UserSession();
        s.setUserId(user.getUserID());
        s.setJwt(jwt);
        s.setDeviceInfo(device);
        s.setIpAddress(ip);
        s.setExpiresAt(expiresAt);
        s.setRevoked(false);
        sessionDao.insert(s);

        audit.logLoginSuccess(user, ip);
        return s;
    }

    /**
     * Revoke session theo ID.
     *
     * @param sessionId UUID session
     * @return true nếu thành công
     */
    public boolean revokeSession(UUID sessionId) {
        UserSession s = sessionDao.findById(sessionId);
        if (s != null) {
            s.setRevoked(true);
            return sessionDao.update(s);
        }
        return false;
    }

    /**
     * Tìm session còn active theo JWT.
     *
     * @param jwt token
     * @return UserSession hoặc null
     */
    public UserSession findActiveSessionByJwt(String jwt) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserSession> q = em.createQuery(
                    "SELECT s FROM UserSession s WHERE s.jwt = :jwt "
                    + "AND s.revoked = false "
                    + "AND s.expiresAt > CURRENT_TIMESTAMP", UserSession.class);
            q.setParameter("jwt", jwt);
            List<UserSession> sessions = q.getResultList();
            return sessions.isEmpty() ? null : sessions.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Đánh dấu session (tìm theo jwt) là đã xác thực 2FA vào thời điểm hiện tại.
     * Trả về true nếu cập nhật thành công.
     */
    public boolean markSession2faVerifiedByJwt(String jwt) {
        UserSession s = findActiveSessionByJwt(jwt);
        if (s == null) return false;
        s.setLast2faVerifiedAt(LocalDateTime.now());
        return sessionDao.update(s);
    }

    /**
     * Đánh dấu người dùng là đã xác thực 2FA (cập nhật trường Last2FAVerifiedAt trên User).
     */
    public boolean markUser2faVerified(java.util.UUID userId) {
        User u = userDao.findById(userId);
        if (u == null) return false;
        u.setLast2faVerifiedAt(LocalDateTime.now());
        return userDao.update(u);
    }

    /**
     * Revoke session theo JWT.
     *
     * @param jwt token
     * @return true nếu thành công
     */
    public boolean revokeByToken(String jwt) {
        UserSession s = findActiveSessionByJwt(jwt);
        if (s != null) {
            s.setRevoked(true);
            return sessionDao.update(s);
        }
        return false;
    }

    /**
     * Lấy các session active của user.
     *
     * @param userId UUID user
     * @return danh sách session
     */
    public List<UserSession> getUserSessions(UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserSession> q = em.createQuery(
                    "SELECT s FROM UserSession s WHERE s.userId = :uid AND s.revoked = false", UserSession.class);
            q.setParameter("uid", userId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    // ==============================
    // Helpers
    // ==============================
    /**
     * Tìm user theo email (case-insensitive).
     *
     * @param email email
     * @return User hoặc null
     */
    public User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        try {
        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u WHERE LOWER(u.email) = :email", User.class);
        query.setParameter("email", email.trim().toLowerCase());
            List<User> users = query.getResultList();
            return users.isEmpty() ? null : users.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy giá trị meta JSON.
     *
     * @param user User
     * @param key key
     * @return value hoặc null
     */
    public String getMetaValue(User user, String key) {
        if (user.getMeta() == null) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(user.getMeta());
            JsonNode v = node.get(key);
            return v != null ? v.asText() : null;
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
