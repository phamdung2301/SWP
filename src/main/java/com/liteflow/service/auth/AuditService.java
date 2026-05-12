package com.liteflow.service.auth;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.auth.AuditLog;
import com.liteflow.model.auth.User;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuditService: - Ghi nhận các hành động quan trọng (login, logout, signup,
 * OTP, role...). - Lưu vào bảng AuditLogs để phục vụ auditing & bảo mật.
 */
public class AuditService {

    private static final Logger LOGGER = Logger.getLogger(AuditService.class.getName());

    /**
     * Các loại hành động cần audit.
     */
    public enum AuditAction {
        LOGIN_SUCCESS, LOGIN_FAIL, LOGOUT,
        SIGNUP,
        OTP_ISSUED, OTP_USED,
        PASSWORD_CHANGED,
        ACCESS_DENIED,
        LOCK_ACCOUNT,
        UNLOCK_ACCOUNT,
        CHANGE_PASSWORD,
        ROLE_ASSIGNED, ROLE_REMOVED,
        TOKEN_REFRESH,
        CREATE
    }

    /**
     * Loại đối tượng liên quan đến audit.
     */
    public enum ObjectType {
        USER, OTP, OTHER
    }

    private final GenericDAO<AuditLog, UUID> dao = new GenericDAO<>(AuditLog.class, UUID.class);

    /**
     * API chung để ghi log audit.
     *
     * @param user user thực hiện hành động (có thể null)
     * @param action loại hành động
     * @param type loại đối tượng liên quan
     * @param objectId id của đối tượng (có thể null)
     * @param details chi tiết hành động
     * @param ip địa chỉ IP client
     */
    public void log(User user, AuditAction action, ObjectType type,
            String objectId, String details, String ip) {
        try {
            AuditLog log = new AuditLog();
            log.setUserID(user != null ? user.getUserID() : null);
            log.setAction(action != null ? action.name() : "UNKNOWN");
            log.setObjectType(type != null ? type.name() : "OTHER");
            log.setObjectID(objectId);
            log.setDetails(details);
            log.setIpAddress(ip);

            dao.insert(log);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Failed to write audit log", e);
        }
    }

    // ==============================
    // Convenience methods
    // ==============================
//    /**
//     * Ghi log khi bị từ chối truy cập.
//     */
    public void logDenied(User user, String path, String ip) {
        log(user, AuditAction.ACCESS_DENIED, ObjectType.OTHER,
                path, "Access denied: " + path, ip);
    }

//    /**
//     * Ghi log khi đăng nhập thành công.
//     */
    public void logLoginSuccess(User user, String ip) {
        log(user, AuditAction.LOGIN_SUCCESS, ObjectType.USER,
                user != null ? user.getUserID().toString() : null,
                "Login successful", ip);
    }

//    /**
//     * Ghi log khi đăng nhập thất bại.
//     */
    public void logLoginFail(String email, String ip) {
        log(null, AuditAction.LOGIN_FAIL, ObjectType.USER,
                email, "Login failed for email: " + email, ip);
    }

//    /**
//     * Ghi log khi logout.
//     */
    public void logLogout(User user, String ip) {
        log(user, AuditAction.LOGOUT, ObjectType.USER,
                user != null ? user.getUserID().toString() : null,
                "User logged out", ip);
    }

//    /**
//     * Ghi log khi signup thành công.
//     */
    public void logSignup(User user, String ip) {
        log(user, AuditAction.SIGNUP, ObjectType.USER,
                user != null ? user.getUserID().toString() : null,
                "User signed up", ip);
    }
//
//    /**
//     * Ghi log khi phát hành OTP.
//     */
    public void logOtpIssued(User user, String ip) {
        log(user, AuditAction.OTP_ISSUED, ObjectType.OTP,
                user != null ? user.getUserID().toString() : null,
                "OTP issued", ip);
    }
//
//    /**
//     * Ghi log khi OTP được sử dụng.
//     */
    public void logOtpUsed(User user, String ip) {
        log(user, AuditAction.OTP_USED, ObjectType.OTP,
                user != null ? user.getUserID().toString() : null,
                "OTP used", ip);
    }

//    /**
//     * Ghi log khi đổi mật khẩu.
//     */
    public void logPasswordChanged(User user, String ip) {
        log(user, AuditAction.PASSWORD_CHANGED, ObjectType.USER,
                user != null ? user.getUserID().toString() : null,
                "Password changed", ip);
    }

//    /**
//     * Ghi log khi gán role cho user.
//     */
    public void logRoleAssigned(User user, String roleName, String ip) {
        log(user, AuditAction.ROLE_ASSIGNED, ObjectType.USER,
                user != null ? user.getUserID().toString() : null,
                "Role assigned: " + roleName, ip);
    }

//    /**
//     * Ghi log khi gỡ role khỏi user.
//     */
    public void logRoleRemoved(User user, String roleName, String ip) {
        log(user, AuditAction.ROLE_REMOVED, ObjectType.USER,
                user != null ? user.getUserID().toString() : null,
                "Role removed: " + roleName, ip);
    }
}
