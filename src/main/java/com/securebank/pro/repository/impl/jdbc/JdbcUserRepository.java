package com.securebank.pro.repository.impl.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.securebank.pro.entity.Admin;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.Role;
import com.securebank.pro.exception.UserNotFoundException;
import com.securebank.pro.repository.UserRepository;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(User user) {
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
    public void deleteById(int userId) {
        String sql = "DELETE FROM users WHERE userId = ?";
        int rows = jdbcTemplate.update(sql, userId);
        if (rows == 0) {
            throw new UserNotFoundException("userId", String.valueOf(userId));
        }
    }

    @Override
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE userId = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, userId);
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public User findByEmail(String email) {
        if (email == null) return null;
        String sql = "SELECT * FROM users WHERE LOWER(email) = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, email.toLowerCase().trim());
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public List<User> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String sql = "SELECT * FROM users WHERE LOWER(fullName) LIKE ? OR LOWER(email) LIKE ?";
        String wildCard = "%" + query.toLowerCase().trim() + "%";
        return jdbcTemplate.query(sql, this::mapRowToUser, wildCard, wildCard);
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM users";
        jdbcTemplate.update(sql);
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
