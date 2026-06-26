package com.securebank.pro.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import com.securebank.pro.enums.Role;
import com.securebank.pro.exception.UnauthorizedAccessException;
import com.securebank.pro.validation.PasswordValidator;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("CUSTOMER")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    private String fullName;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", insertable = false, updatable = false)
    private Role role;

    private boolean registered;
    private boolean loggedIn;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    protected User() {
        this.userId = 0;
        this.role = Role.CUSTOMER;
    }

    public User(String fullName, String email, String password) {
        this(fullName, email, password, Role.CUSTOMER);
    }

    protected User(String fullName, String email, String password, Role role) {
        this.userId = 0;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.registered = false;
        this.loggedIn = false;
    }

    // Database mapping constructor
    public User(int userId, String fullName, String email, String password, Role role, boolean registered, boolean loggedIn) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.registered = registered;
        this.loggedIn = loggedIn;
    }

    /**
     * Attempts to register the user.
     *
     * Phase 2 change: now uses PasswordValidator to validate email and password
     * before allowing registration. Validation errors are caught internally and
     * cause the method to return false with a printed message, keeping the same
     * return-type contract as before.
     *
     * @return true if registration succeeded, false otherwise
     */
    public boolean registerUser() {
        if (isBlank(fullName)) {
            System.out.println("[Registration] Full name cannot be blank.");
            return false;
        }
        try {
            PasswordValidator.validateEmail(email);
            PasswordValidator.validatePassword(password);
        } catch (IllegalArgumentException e) {
            System.out.println("[Registration] Validation failed: " + e.getMessage());
            return false;
        }
        registered = true;
        return true;
    }

    public boolean registerUser(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        return registerUser();
    }

    public boolean loginUser(String email, String password) {
        if (!registered) {
            return false;
        }
        loggedIn = this.email.equals(email) && this.password.equals(password);
        return loggedIn;
    }

    public boolean loginUser(String email, String password, org.springframework.security.crypto.password.PasswordEncoder encoder) {
        if (!registered) {
            return false;
        }
        loggedIn = this.email.equals(email) && (encoder.matches(password, this.password) || this.password.equals(password));
        return loggedIn;
    }

    /**
     * Authenticates the user and throws if credentials are wrong.
     *
     * Phase 2 addition: demonstrates the "throw" keyword with an unchecked exception.
     *
     * Instead of returning false on failure, this method throws
     * UnauthorizedAccessException — which forces the caller to decide what to do
     * (catch and handle, or let it propagate).
     *
     * Use this version when a failed login should be a hard stop, not a silent false.
     *
     * @throws UnauthorizedAccessException if the credentials do not match
     */
    public void authenticateOrThrow(String email, String password) {
        if (!loginUser(email, password)) {
            throw new UnauthorizedAccessException(email, "login");
        }
    }

    public void logoutUser() {
        loggedIn = false;
    }

    public String getDashboardMessage() {
        return "Welcome, " + fullName;
    }

    public final boolean hasRole(Role expectedRole) {
        return role == expectedRole;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public boolean isRegistered() {
        return registered;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    protected void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
