package com.securebank.pro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import com.securebank.pro.enums.Role;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    private static final long serialVersionUID = 1L;
    private String department;

    protected Admin() {
        super();
        this.department = "";
    }

    public Admin(String fullName, String email, String password, String department) {
        super(fullName, email, password, Role.ADMIN);
        this.department = department;
    }

    @Override
    public String getDashboardMessage() {
        return "Admin dashboard for " + getFullName() + " (" + department + ")";
    }

    public boolean canManageUsers() {
        return hasRole(Role.ADMIN);
    }

    public String getDepartment() {
        return department;
    }

    // Database mapping constructor
    public Admin(int userId, String fullName, String email, String password, Role role, boolean registered, boolean loggedIn, String department) {
        super(userId, fullName, email, password, role, registered, loggedIn);
        this.department = department;
    }
}
