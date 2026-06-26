package com.securebank.pro.repository.impl.jdbc;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.AccountType;
import com.securebank.pro.exception.InvalidAccountException;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.repository.UserRepository;

@Repository
public class JdbcAccountRepository implements AccountRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    public JdbcAccountRepository(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    @Override
    public void save(Account account) {
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
    public Account findByAccountNumber(String accountNumber) {
        if (accountNumber == null) return null;

        String sql = "SELECT * FROM accounts WHERE LOWER(accountNumber) = ?";
        List<Account> accounts = jdbcTemplate.query(sql, this::mapRowToAccount, accountNumber.toLowerCase().trim());
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    @Override
    public List<Account> findByOwnerId(int ownerId) {
        String sql = "SELECT * FROM accounts WHERE ownerId = ?";
        return jdbcTemplate.query(sql, this::mapRowToAccount, ownerId);
    }

    @Override
    public List<Account> findAll() {
        String sql = "SELECT * FROM accounts";
        return jdbcTemplate.query(sql, this::mapRowToAccount);
    }

    @Override
    public void deleteByAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number cannot be null.");
        }

        String sql = "DELETE FROM accounts WHERE LOWER(accountNumber) = ?";
        int rows = jdbcTemplate.update(sql, accountNumber.toLowerCase().trim());
        if (rows == 0) {
            throw new InvalidAccountException(accountNumber, "Account not found.");
        }
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM accounts";
        jdbcTemplate.update(sql);
    }

    private Account mapRowToAccount(ResultSet rs, int rowNum) throws SQLException {
        int ownerId = rs.getInt("ownerId");
        User owner = userRepository.findById(ownerId);

        int accountId = rs.getInt("accountId");
        String accountNumber = rs.getString("accountNumber");
        BigDecimal balance = rs.getBigDecimal("balance");
        boolean active = rs.getBoolean("active");
        AccountType type = AccountType.valueOf(rs.getString("accountType"));

        if (type == AccountType.SAVINGS) {
            return new com.securebank.pro.entity.SavingsAccount(accountId, accountNumber, owner, balance, active);
        } else {
            return new com.securebank.pro.entity.CurrentAccount(accountId, accountNumber, owner, balance, active);
        }
    }
}
