/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import enums.AdminRole;
import java.time.LocalDateTime;

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class Admins {
    private int adminID;
    private String fullName;
    private String account;
    private String passwordHash;
    private String phone;
    private String email;
    private AdminRole roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Admins() {
    }

    public Admins(int adminID, String fullName, String account, String passwordHash, String phone, String email, AdminRole roles, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.adminID = adminID;
        this.fullName = fullName;
        this.account = account;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.email = email;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Admins(String fullName, String account, String passwordHash, String phone, String email) {
        this.fullName = fullName;
        this.account = account;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.email = email;
        this.roles = AdminRole.MOVIES_MANAGER;
        this.createdAt = LocalDateTime.now();
    }

    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public AdminRole getRoles() {
        return roles;
    }

    public void setRoles(AdminRole roles) {
        this.roles = roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Admins{" + "adminID=" + adminID + ", fullName=" + fullName + ", account=" + account + ", passwordHash=" + passwordHash + ", phone=" + phone + ", email=" + email + ", roles=" + roles + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }
}
