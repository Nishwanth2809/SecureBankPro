package com.securebank.pro.repository.impl.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.securebank.pro.entity.Admin;
import com.securebank.pro.enums.Role;
import com.securebank.pro.repository.AdminRepository;

@Repository
public class JdbcAdminRepository implements AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Admin admin) {
        if (admin == null) {
            throw new IllegalArgumentException("Admin cannot be null.");
        }

        String sql = "MERGE INTO users (userId, fullName, email, password, role, registered, loggedIn, department) KEY(userId) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            admin.getUserId(),
            admin.getFullName(),
            admin.getEmail().toLowerCase().trim(),
            admin.getPassword(),
            admin.getRole().name(),
            admin.isRegistered(),
            admin.isLoggedIn(),
            admin.getDepartment()
        );
    }

    @Override
    public Admin findByEmail(String email) {
        if (email == null) return null;

        String sql = "SELECT * FROM users WHERE LOWER(email) = ? AND role = 'ADMIN'";
        List<Admin> admins = jdbcTemplate.query(sql, this::mapRowToAdmin, email.toLowerCase().trim());
        return admins.isEmpty() ? null : admins.get(0);
    }

    @Override
    public List<Admin> findAll() {
        String sql = "SELECT * FROM users WHERE role = 'ADMIN'";
        return jdbcTemplate.query(sql, this::mapRowToAdmin);
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM users WHERE role = 'ADMIN'";
        jdbcTemplate.update(sql);
    }

    private Admin mapRowToAdmin(ResultSet rs, int rowNum) throws SQLException {
        return new Admin(
            rs.getInt("userId"),
            rs.getString("fullName"),
            rs.getString("email"),
            rs.getString("password"),
            Role.ADMIN,
            rs.getBoolean("registered"),
            rs.getBoolean("loggedIn"),
            rs.getString("department")
        );
    }
}
