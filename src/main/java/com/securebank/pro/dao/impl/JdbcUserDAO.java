package com.securebank.pro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.securebank.pro.dao.UserDAO;
import com.securebank.pro.entity.Admin;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.Role;

@Repository
public class JdbcUserDAO implements UserDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        String sql = "MERGE INTO users (userId, fullName, email, password, role, registered, loggedIn, department) KEY(userId) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            String department = (user instanceof Admin) ? ((Admin) user).getDepartment() : null;
            jdbcTemplate.update(sql,
                user.getUserId(),
                user.getFullName(),
                user.getEmail().toLowerCase().trim(),
                user.getPassword(),
                user.getRole().name(),
                user.isRegistered(),
                user.isLoggedIn(),
                department
            );
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered.", e);
        }
    }

    @Override
    public User findUserByEmail(String email) {
        if (email == null) return null;

        String sql = "SELECT * FROM users WHERE LOWER(email) = ?";
        List<User> list = jdbcTemplate.query(sql, this::mapRowToUser, email.toLowerCase().trim());
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        String sql = "UPDATE users SET fullName = ?, password = ?, role = ?, registered = ?, loggedIn = ?, department = ? WHERE userId = ?";
        String department = (user instanceof Admin) ? ((Admin) user).getDepartment() : null;
        int updated = jdbcTemplate.update(sql,
            user.getFullName(),
            user.getPassword(),
            user.getRole().name(),
            user.isRegistered(),
            user.isLoggedIn(),
            department,
            user.getUserId()
        );
        if (updated == 0) {
            throw new IllegalArgumentException("User not found for update: ID " + user.getUserId());
        }
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        int userId = rs.getInt("userId");
        String fullName = rs.getString("fullName");
        String email = rs.getString("email");
        String password = rs.getString("password");
        boolean registered = rs.getBoolean("registered");
        boolean loggedIn = rs.getBoolean("loggedIn");

        if (role == Role.ADMIN) {
            String department = rs.getString("department");
            return new Admin(userId, fullName, email, password, role, registered, loggedIn, department);
        } else {
            return new User(userId, fullName, email, password, role, registered, loggedIn);
        }
    }
}
