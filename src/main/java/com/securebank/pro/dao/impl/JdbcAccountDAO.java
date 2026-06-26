package com.securebank.pro.dao.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.securebank.pro.dao.AccountDAO;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Admin;
import com.securebank.pro.entity.CurrentAccount;
import com.securebank.pro.entity.SavingsAccount;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.AccountType;
import com.securebank.pro.enums.Role;

@Repository
public class JdbcAccountDAO implements AccountDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }

        String sql = "MERGE INTO accounts (accountId, accountNumber, ownerId, balance, active, accountType) KEY(accountId) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            account.getAccountId(),
            account.getAccountNumber().toUpperCase().trim(),
            account.getOwner() != null ? account.getOwner().getUserId() : 0,
            account.getBalance(),
            account.isActive(),
            account.getAccountType().name()
        );
    }

    @Override
    public void updateBalance(String accountNumber, BigDecimal newBalance) {
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number cannot be null.");
        }

        String sql = "UPDATE accounts SET balance = ? WHERE LOWER(accountNumber) = ?";
        int updated = jdbcTemplate.update(sql, newBalance, accountNumber.toLowerCase().trim());
        if (updated == 0) {
            throw new IllegalArgumentException("Account not found for balance update: " + accountNumber);
        }
    }

    @Override
    public Account fetchAccount(String accountNumber) {
        if (accountNumber == null) return null;

        String sql = "SELECT a.*, u.fullName, u.email, u.password, u.role, u.registered, u.loggedIn, u.department " +
                     "FROM accounts a " +
                     "JOIN users u ON a.ownerId = u.userId " +
                     "WHERE LOWER(a.accountNumber) = ?";
        List<Account> accounts = jdbcTemplate.query(sql, this::mapRowToAccount, accountNumber.toLowerCase().trim());
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    private Account mapRowToAccount(ResultSet rs, int rowNum) throws SQLException {
        int userId = rs.getInt("ownerId");
        String fullName = rs.getString("fullName");
        String email = rs.getString("email");
        String password = rs.getString("password");
        Role role = Role.valueOf(rs.getString("role"));
        boolean registered = rs.getBoolean("registered");
        boolean loggedIn = rs.getBoolean("loggedIn");

        User owner;
        if (role == Role.ADMIN) {
            String dept = rs.getString("department");
            owner = new Admin(userId, fullName, email, password, role, registered, loggedIn, dept);
        } else {
            owner = new User(userId, fullName, email, password, role, registered, loggedIn);
        }

        int accountId = rs.getInt("accountId");
        String accountNumber = rs.getString("accountNumber");
        BigDecimal balance = rs.getBigDecimal("balance");
        boolean active = rs.getBoolean("active");
        AccountType type = AccountType.valueOf(rs.getString("accountType"));

        if (type == AccountType.SAVINGS) {
            return new SavingsAccount(accountId, accountNumber, owner, balance, active);
        } else {
            return new CurrentAccount(accountId, accountNumber, owner, balance, active);
        }
    }
}
