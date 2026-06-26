package com.securebank.pro.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.*;

import com.securebank.pro.enums.TransactionType;

@Entity
@Table(name = "transactions")
public class Transaction implements Comparable<Transaction>, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;

    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sourceAccountId")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destinationAccountId")
    private Account destinationAccount;

    private BigDecimal amount;
    private LocalDateTime createdAt;

    protected Transaction() {
        this.transactionId = 0;
        this.referenceNumber = "";
        this.transactionType = null;
        this.sourceAccount = null;
        this.destinationAccount = null;
        this.amount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    private Transaction(
            TransactionType transactionType,
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount
    ) {
        this.transactionId = 0;
        this.referenceNumber = "TXN-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.transactionType = transactionType;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    // Database mapping constructor
    public Transaction(
            int transactionId,
            String referenceNumber,
            TransactionType transactionType,
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount,
            LocalDateTime createdAt
    ) {
        this.transactionId = transactionId;
        this.referenceNumber = referenceNumber;
        this.transactionType = transactionType;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public static Transaction createTransaction(
            TransactionType transactionType,
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount
    ) {
        return new Transaction(transactionType, sourceAccount, destinationAccount, amount);
    }

    public static List<Transaction> getTransactionHistory(List<Transaction> transactions, Account account) {
        return transactions.stream()
                .filter(transaction -> transaction.belongsTo(account))
                .collect(Collectors.toList());
    }

    public boolean belongsTo(Account account) {
        if (account == null) return false;
        boolean matchesSource = sourceAccount != null && sourceAccount.getAccountNumber().equalsIgnoreCase(account.getAccountNumber());
        boolean matchesDest = destinationAccount != null && destinationAccount.getAccountNumber().equalsIgnoreCase(account.getAccountNumber());
        return matchesSource || matchesDest;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static final Comparator<Transaction> BY_AMOUNT_ASC = Comparator.comparing(Transaction::getAmount);
    public static final Comparator<Transaction> BY_AMOUNT_DESC = BY_AMOUNT_ASC.reversed();

    public static final Comparator<Transaction> BY_DATE_ASC = Comparator.comparing(Transaction::getCreatedAt);
    public static final Comparator<Transaction> BY_DATE_DESC = BY_DATE_ASC.reversed();

    @Override
    public int compareTo(Transaction other) {
        return Integer.compare(this.transactionId, other.transactionId);
    }
}
