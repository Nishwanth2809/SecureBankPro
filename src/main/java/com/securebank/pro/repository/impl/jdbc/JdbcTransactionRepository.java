package com.securebank.pro.repository.impl.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.enums.TransactionType;
import com.securebank.pro.repository.AccountRepository;
import com.securebank.pro.repository.TransactionRepository;

@Repository
public class JdbcTransactionRepository implements TransactionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AccountRepository accountRepository;
    private final Queue<Transaction> pendingQueue = new LinkedList<>();

    public JdbcTransactionRepository(JdbcTemplate jdbcTemplate, AccountRepository accountRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountRepository = accountRepository;
    }

    @Override
    public void save(Transaction transaction) {
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
    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions";
        return jdbcTemplate.query(sql, this::mapRowToTransaction);
    }

    @Override
    public List<Transaction> findByAccount(Account account) {
        if (account == null) {
            return List.of();
        }

        String sql = "SELECT * FROM transactions WHERE sourceAccountId = ? OR destinationAccountId = ?";
        return jdbcTemplate.query(sql, this::mapRowToTransaction, account.getAccountId(), account.getAccountId());
    }

    @Override
    public void queuePending(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Cannot queue a null transaction.");
        }
        pendingQueue.offer(transaction);
    }

    @Override
    public Transaction pollPending() {
        return pendingQueue.poll();
    }

    @Override
    public Transaction peekPending() {
        return pendingQueue.peek();
    }

    @Override
    public Queue<Transaction> getPendingQueue() {
        return pendingQueue;
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM transactions";
        jdbcTemplate.update(sql);
        pendingQueue.clear();
    }

    private Transaction mapRowToTransaction(ResultSet rs, int rowNum) throws SQLException {
        int transactionId = rs.getInt("transactionId");
        String referenceNumber = rs.getString("referenceNumber");
        TransactionType type = TransactionType.valueOf(rs.getString("transactionType"));

        int sourceId = rs.getInt("sourceAccountId");
        Account source = rs.wasNull() ? null : getAccountById(sourceId);

        int destId = rs.getInt("destinationAccountId");
        Account dest = rs.wasNull() ? null : getAccountById(destId);

        java.math.BigDecimal amount = rs.getBigDecimal("amount");
        LocalDateTime createdAt = rs.getTimestamp("createdAt").toLocalDateTime();

        return new Transaction(transactionId, referenceNumber, type, source, dest, amount, createdAt);
    }

    private Account getAccountById(int accountId) {
        if (accountId == 0) return null;
        String sql = "SELECT accountNumber FROM accounts WHERE accountId = ?";
        List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("accountNumber"), accountId);
        if (!list.isEmpty()) {
            return accountRepository.findByAccountNumber(list.get(0));
        }
        return null;
    }
}
