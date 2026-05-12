package com.liteflow.service.auth;

import com.liteflow.dao.BaseDAO;
import com.liteflow.dao.GenericDAO;
import com.liteflow.model.auth.Role;
import com.liteflow.model.auth.User;
import com.liteflow.model.auth.UserRole;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * RoleService: - Quản lý CRUD Role - Quản lý mapping User ↔ Role - Tích hợp
 * AuditService để log thay đổi
 */
public class RoleService {

    private final GenericDAO<Role, UUID> roleDao = new GenericDAO<>(Role.class, UUID.class);
    private final AuditService audit = new AuditService();

    // ==========================
    // CRUD Role
    // ==========================
    public List<Role> getAllRoles() {
        return roleDao.getAll();
    }

    public Role getRoleById(UUID id) {
        return roleDao.findById(id);
    }

    public boolean createRole(Role role) {
        if (role == null || role.getName() == null || role.getName().isBlank()) {
            return false;
        }
        return roleDao.insert(role);
    }

    public boolean updateRole(Role role) {
        if (role == null || role.getRoleID() == null) {
            return false;
        }
        return roleDao.update(role);
    }

    public boolean deleteRole(UUID roleId) {
        Role r = roleDao.findById(roleId);
        if (r != null) {
            return roleDao.delete(roleId);
        }
        return false;
    }

    // ==========================
    // User ↔ Role mapping
    // ==========================
    /**
     * Gán role cho user với metadata. Nếu user đã có role active thì bỏ qua.
     */
    public boolean assignRole(UUID userId, UUID roleId, UUID assignedBy, String ip) {
        EntityManager em = BaseDAO.emf.createEntityManager();
        try {
            long count = em.createQuery(
                    "SELECT COUNT(ur) FROM UserRole ur WHERE ur.userId = :u AND ur.roleId = :r AND ur.isActive = true",
                    Long.class
            )
                    .setParameter("u", userId)
                    .setParameter("r", roleId)
                    .getSingleResult();

            if (count == 0) {
                em.getTransaction().begin();
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                ur.setAssignedAt(LocalDateTime.now());
                ur.setAssignedBy(assignedBy);
                ur.setIsActive(true);
                em.persist(ur);
                em.getTransaction().commit();

                User u = em.find(User.class, userId);
                Role r = em.find(Role.class, roleId);
                if (u != null && r != null) {
                    audit.logRoleAssigned(u, r.getName(), ip);
                }
                return true;
            }
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Overload: gán role cho user mà không cần assignedBy (dùng cho
     * signup/SSO).
     */
    public boolean assignRole(UUID userId, UUID roleId, String ip) {
        return assignRole(userId, roleId, null, ip);
    }

    public boolean deactivateRole(UUID userId, UUID roleId, String ip) {
        EntityManager em = BaseDAO.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            int updated = em.createQuery(
                    "UPDATE UserRole ur SET ur.isActive = false WHERE ur.userId = :u AND ur.roleId = :r"
            )
                    .setParameter("u", userId)
                    .setParameter("r", roleId)
                    .executeUpdate();
            em.getTransaction().commit();

            if (updated > 0) {
                User u = em.find(User.class, userId);
                Role r = em.find(Role.class, roleId);
                if (u != null && r != null) {
                    audit.logRoleRemoved(u, r.getName(), ip);
                }
                return true;
            }
            return false;
        } finally {
            em.close();
        }
    }

    public List<Role> getUserRoles(UUID userId) {
        EntityManager em = BaseDAO.emf.createEntityManager();
        try {
            TypedQuery<Role> q = em.createQuery(
                    "SELECT r FROM Role r, UserRole ur WHERE r.roleID = ur.roleId AND ur.userId = :uid AND ur.isActive = true",
                    Role.class
            );
            q.setParameter("uid", userId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public UUID getRoleIdByName(String name) {
        EntityManager em = BaseDAO.emf.createEntityManager();
        try {
            TypedQuery<Role> q = em.createQuery(
                    "SELECT r FROM Role r WHERE LOWER(r.name) = LOWER(:n)", Role.class
            );
            q.setParameter("n", name);
            List<Role> list = q.getResultList();
            return list.isEmpty() ? null : list.get(0).getRoleID();
        } finally {
            em.close();
        }
    }
}
