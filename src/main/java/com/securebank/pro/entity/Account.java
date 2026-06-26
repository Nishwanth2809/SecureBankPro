package com.securebank.pro.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import jakarta.persistence.*;

import com.securebank.pro.enums.AccountType;
import com.securebank.pro.exception.InsufficientBalanceException;

@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "accountType", discriminatorType = DiscriminatorType.STRING)
public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final BigDecimal MIN_OPENING_BALANCE = BigDecimal.ZERO;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int accountId;

    private String accountNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ownerId", nullable = false)
    private User owner;

    private BigDecimal balance;
    private boolean active;

    protected Account() {
        this.accountId = 0;
        this.accountNumber = "";
        this.owner = null;
        this.balance = BigDecimal.ZERO;
        this.active = false;
    }

    protected Account(String accountNumber, User owner, BigDecimal openingBalance) {
        if (openingBalance.compareTo(MIN_OPENING_BALANCE) < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative.");
        }
        this.accountId = 0;
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber is required");
        this.owner = Objects.requireNonNull(owner, "owner is required");
        this.balance = openingBalance;
        this.active = false;
    }

    // Database mapping constructor
    protected Account(int accountId, String accountNumber, User owner, BigDecimal balance, boolean active) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance;
        this.active = active;
    }

    public static Account createAccount(AccountType accountType, String accountNumber, User owner) {
        return createAccount(accountType, accountNumber, owner, MIN_OPENING_BALANCE);
    }

    public static Account createAccount(
            AccountType accountType,
            String accountNumber,
            User owner,
            BigDecimal openingBalance
    ) {
        if (accountType == AccountType.SAVINGS) {
            return new SavingsAccount(accountNumber, owner, openingBalance);
        }
        return new CurrentAccount(accountNumber, owner, openingBalance);
    }

    public void createAccount() {
        active = true;
    }

    public void freeze() {
        active = false;
    }

    public void unblock() {
        active = true;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Updates the account balance by adding the given amount (positive or negative).
     *
     * Phase 2 change: now throws InsufficientBalanceException (our custom unchecked
     * exception) instead of a generic IllegalArgumentException.
     * This gives callers a specific, meaningful exception type to catch.
     *
     * @param amount positive for credit, negative for debit
     * @throws InsufficientBalanceException if the resulting balance would be negative
     */
    public void updateBalance(BigDecimal amount) {
        BigDecimal newBalance = balance.add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException(accountNumber, balance, amount.abs());
        }
        balance = newBalance;
    }

    public abstract AccountType getAccountType();

    public abstract BigDecimal calculateMaintenanceFee();

    public int getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public User getOwner() {
        return owner;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber != null ? accountNumber.toLowerCase() : null,
                              account.accountNumber != null ? account.accountNumber.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber != null ? accountNumber.toLowerCase() : null);
    }
}
