/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.liteflow.dao;

import com.liteflow.model.auth.User;
import com.liteflow.model.auth.Employee;
import com.liteflow.model.auth.Role;
import com.liteflow.model.auth.AuditLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class DBTest {
    
    /**
     * Hàm test đơn giản để lấy dữ liệu từ database
     */
    public static void testDatabaseConnection() {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        
        try {
            System.out.println("🔄 Đang kết nối đến database...");
            emf = Persistence.createEntityManagerFactory("LiteFlowPU");
            em = emf.createEntityManager();
            
            System.out.println("✅ Kết nối database thành công!");
            
            // Test các hàm lấy dữ liệu
            testGetUsers(em);
            testGetEmployees(em);
            testGetRoles(em);
            testGetAuditLogs(em);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi kết nối DB: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) {
                emf.close();
            }
        }
    }
    
    /**
     * Test lấy danh sách User
     */
    private static void testGetUsers(EntityManager em) {
        try {
            System.out.println("\n📋 Test lấy danh sách Users:");
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            List<User> users = query.setMaxResults(5).getResultList();
            
            if (users.isEmpty()) {
                System.out.println("   ⚠️  Không có dữ liệu User trong database");
            } else {
                System.out.println("   ✅ Tìm thấy " + users.size() + " User(s):");
                for (User user : users) {
                    System.out.println("      - " + user.getEmail() + " (" + user.getDisplayName() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("   ❌ Lỗi khi lấy Users: " + e.getMessage());
        }
    }
    
    /**
     * Test lấy danh sách Employee
     */
    private static void testGetEmployees(EntityManager em) {
        try {
            System.out.println("\n👥 Test lấy danh sách Employees:");
            TypedQuery<Employee> query = em.createQuery("SELECT e FROM Employee e", Employee.class);
            List<Employee> employees = query.setMaxResults(5).getResultList();
            
            if (employees.isEmpty()) {
                System.out.println("   ⚠️  Không có dữ liệu Employee trong database");
            } else {
                System.out.println("   ✅ Tìm thấy " + employees.size() + " Employee(s):");
                for (Employee emp : employees) {
                    System.out.println("      - " + emp.getFullName() + " (" + emp.getPosition() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("   ❌ Lỗi khi lấy Employees: " + e.getMessage());
        }
    }
    
    /**
     * Test lấy danh sách Role
     */
    private static void testGetRoles(EntityManager em) {
        try {
            System.out.println("\n🔐 Test lấy danh sách Roles:");
            TypedQuery<Role> query = em.createQuery("SELECT r FROM Role r", Role.class);
            List<Role> roles = query.getResultList();
            
            if (roles.isEmpty()) {
                System.out.println("   ⚠️  Không có dữ liệu Role trong database");
            } else {
                System.out.println("   ✅ Tìm thấy " + roles.size() + " Role(s):");
                for (Role role : roles) {
                    System.out.println("      - " + role.getName() + ": " + role.getDescription());
                }
            }
        } catch (Exception e) {
            System.err.println("   ❌ Lỗi khi lấy Roles: " + e.getMessage());
        }
    }
    
    /**
     * Test lấy danh sách AuditLog
     */
    private static void testGetAuditLogs(EntityManager em) {
        try {
            System.out.println("\n📊 Test lấy danh sách AuditLogs:");
            TypedQuery<AuditLog> query = em.createQuery("SELECT a FROM AuditLog a ORDER BY a.createdAt DESC", AuditLog.class);
            List<AuditLog> logs = query.setMaxResults(3).getResultList();
            
            if (logs.isEmpty()) {
                System.out.println("   ⚠️  Không có dữ liệu AuditLog trong database");
            } else {
                System.out.println("   ✅ Tìm thấy " + logs.size() + " AuditLog(s) gần nhất:");
                for (AuditLog log : logs) {
                    System.out.println("      - " + log.getAction() + " (" + log.getCreatedAt() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("   ❌ Lỗi khi lấy AuditLogs: " + e.getMessage());
        }
    }
    
    /**
     * Hàm đơn giản test lấy 1 record từ bảng User
     */
    public static void testSimpleUserQuery() {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        
        try {
            emf = Persistence.createEntityManagerFactory("LiteFlowPU");
            em = emf.createEntityManager();

            em.getTransaction().begin();
            // Thử query dữ liệu từ bảng User
            Object result = em.createQuery("SELECT u FROM User u").setMaxResults(1).getSingleResult();
            em.getTransaction().commit();

            System.out.println("✅ Kết nối thành công, có dữ liệu: " + result);

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi kết nối DB: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) {
                emf.close();
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Bắt đầu test database connection...\n");
        
        // Test đơn giản
        System.out.println("=== TEST ĐƠN GIẢN ===");
        testSimpleUserQuery();
        
        System.out.println("\n" + "=".repeat(50));
        
        // Test chi tiết
        System.out.println("=== TEST CHI TIẾT ===");
        testDatabaseConnection();
        
        System.out.println("\n🏁 Hoàn thành test database!");
    }
}