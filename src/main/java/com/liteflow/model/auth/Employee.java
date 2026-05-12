package com.liteflow.model.auth;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Employee: Thông tin nhân viên (liên kết 1-1 với User).
 */
@Entity
@Table(name = "Employees")
public class Employee implements Serializable {

    @Id
    @Column(name = "EmployeeID", columnDefinition = "uniqueidentifier")
    private UUID employeeID;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false, unique = true)
    private User user;

    @Column(name = "EmployeeCode", length = 50, unique = true, nullable = false)
    private String employeeCode;

    @Column(name = "FullName", length = 200, nullable = false)
    private String fullName;

    @Column(name = "Gender", length = 10)
    private String gender;

    @Column(name = "BirthDate")
    private LocalDate birthDate;

    @Column(name = "NationalID", length = 20)
    private String nationalID;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Email", length = 320)
    private String email;

    @Column(name = "Address", length = 500)
    private String address;

    @Lob
    @Column(name = "AvatarURL")
    private String avatarURL;

    @Column(name = "HireDate")
    private LocalDateTime hireDate;

    @Column(name = "TerminationDate")
    private LocalDateTime terminationDate;

    @Column(name = "EmploymentStatus", length = 50)
    private String employmentStatus;

    @Column(name = "Position", length = 100)
    private String position;

    @Column(name = "Salary", precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(name = "BankAccount", length = 100)
    private String bankAccount;

    @Column(name = "BankName", length = 200)
    private String bankName;

    @Lob
    @Column(name = "Notes")
    private String notes;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    // ==============================
    // Lifecycle hooks
    // ==============================
    @PrePersist
    protected void onCreate() {
        if (employeeID == null) {
            employeeID = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        if (hireDate == null) {
            hireDate = LocalDateTime.now();
        }
        
        if (employmentStatus == null) {
            employmentStatus = "Đang làm";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==============================
    // Helper methods
    // ==============================
    
    /**
     * Kiểm tra nhân viên có đang làm việc không
     */
    public boolean isActive() {
        return "Đang làm".equals(employmentStatus);
    }

    /**
     * Kiểm tra nhân viên đã nghỉ việc chưa
     */
    public boolean isTerminated() {
        return terminationDate != null || "Đã nghỉ".equals(employmentStatus);
    }

    // ==============================
    // Getters & Setters
    // ==============================
    
    public UUID getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(UUID employeeID) {
        this.employeeID = employeeID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getNationalID() {
        return nationalID;
    }

    public void setNationalID(String nationalID) {
        this.nationalID = nationalID;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDateTime getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDateTime terminationDate) {
        this.terminationDate = terminationDate;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ==============================
    // equals, hashCode, toString
    // ==============================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employee)) {
            return false;
        }
        Employee employee = (Employee) o;
        return Objects.equals(employeeID, employee.employeeID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeID);
    }

    @Override
    public String toString() {
        return "Employee{"
                + "employeeID=" + employeeID
                + ", employeeCode='" + employeeCode + '\''
                + ", fullName='" + fullName + '\''
                + ", position='" + position + '\''
                + ", employmentStatus='" + employmentStatus + '\''
                + '}';
    }
}

