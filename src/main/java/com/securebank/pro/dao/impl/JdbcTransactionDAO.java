package com.securebank.pro.dao.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.securebank.pro.dao.TransactionDAO;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Admin;
import com.securebank.pro.entity.CurrentAccount;
import com.securebank.pro.entity.SavingsAccount;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.entity.User;
import com.securebank.pro.enums.AccountType;
import com.securebank.pro.enums.Role;
import com.securebank.pro.enums.TransactionType;

@Repository
public class JdbcTransactionDAO implements TransactionDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransactionDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }

        String sql = "INSERT INTO transactions (transactionId, referenceNumber, transactionType, sourceAccountId, destinationAccountId, amount, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            transaction.getTransactionId(),
            transaction.getReferenceNumber(),
            transaction.getTransactionType().name(),
            transaction.getSourceAccount() != null ? transaction.getSourceAccount().getAccountId() : null,
            transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getAccountId() : null,
            transaction.getAmount(),
            Timestamp.valueOf(transaction.getCreatedAt())
        );
    }

    @Override
    public List<Transaction> getTransactionsByUser(int userId) {
        String sql = "SELECT DISTINCT t.*, " +
                     "sa.accountNumber AS saNumber, sa.balance AS saBalance, sa.active AS saActive, sa.accountType AS saType, sa.ownerId AS saOwner, " +
                     "da.accountNumber AS daNumber, da.balance AS daBalance, da.active AS daActive, da.accountType AS daType, da.ownerId AS daOwner " +
                     "FROM transactions t " +
                     "LEFT JOIN accounts sa ON t.sourceAccountId = sa.accountId " +
                     "LEFT JOIN accounts da ON t.destinationAccountId = da.accountId " +
                     "WHERE sa.ownerId = ? OR da.ownerId = ?";
        return jdbcTemplate.query(sql, this::mapRowToTransaction, userId, userId);
    }

    private Transaction mapRowToTransaction(ResultSet rs, int rowNum) throws SQLException {
        int transactionId = rs.getInt("transactionId");
        String referenceNumber = rs.getString("referenceNumber");
        TransactionType type = TransactionType.valueOf(rs.getString("transactionType"));
        BigDecimal amount = rs.getBigDecimal("amount");
        LocalDateTime createdAt = rs.getTimestamp("createdAt").toLocalDateTime();

        int saId = rs.getInt("sourceAccountId");
        Account source = null;
        if (saId > 0) {
            source = constructAccount(
                saId,
                rs.getString("saNumber"),
                rs.getBigDecimal("saBalance"),
                rs.getBoolean("saActive"),
                rs.getString("saType"),
                rs.getInt("saOwner")
            );
        }

        int daId = rs.getInt("destinationAccountId");
        Account dest = null;
        if (daId > 0) {
            dest = constructAccount(
                daId,
                rs.getString("daNumber"),
                rs.getBigDecimal("daBalance"),
                rs.getBoolean("daActive"),
                rs.getString("daType"),
                rs.getInt("daOwner")
            );
        }

        return new Transaction(transactionId, referenceNumber, type, source, dest, amount, createdAt);
    }

    private Account constructAccount(int id, String number, BigDecimal balance, boolean active, String typeStr, int ownerId) {
        if (number == null) return null;
        User owner = findUserById(ownerId);
        AccountType type = AccountType.valueOf(typeStr);
        if (type == AccountType.SAVINGS) {
            return new SavingsAccount(id, number, owner, balance, active);
        } else {
            return new CurrentAccount(id, number, owner, balance, active);
        }
    }

    private User findUserById(int userId) {
        if (userId == 0) return null;
        String sql = "SELECT * FROM users WHERE userId = ?";
        List<User> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Role role = Role.valueOf(rs.getString("role"));
            String fullName = rs.getString("fullName");
            String email = rs.getString("email");
            String password = rs.getString("password");
            boolean registered = rs.getBoolean("registered");
            boolean loggedIn = rs.getBoolean("loggedIn");
            if (role == Role.ADMIN) {
                return new Admin(userId, fullName, email, password, role, registered, loggedIn, rs.getString("department"));
            } else {
                return new User(userId, fullName, email, password, role, registered, loggedIn);
            }
        }, userId);
        return list.isEmpty() ? null : list.get(0);
    }
}
