package com.securebank.pro.exception;

import java.math.BigDecimal;

/**
 * Thrown when a withdrawal or transfer is attempted but the account
 * does not have sufficient funds to cover the requested amount.
 *
 * This is an UNCHECKED (runtime) exception — it extends RuntimeException.
 * Callers are not forced to handle it at compile time, but they may
 * choose to catch it for better user feedback.
 *
 * Phase 2 — Exception Handling & Validation
 */
public class InsufficientBalanceException extends RuntimeException {

    private final String accountNumber;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;

    /**
     * Simple message constructor.
     * Use when account details are not available.
     */
    public InsufficientBalanceException(String message) {
        super(message);
        this.accountNumber = null;
        this.currentBalance = null;
        this.requestedAmount = null;
    }

    /**
     * Detailed constructor with account context.
     * Automatically builds a descriptive message.
     * Use this when you have full account details — gives much richer error info.
     */
    public InsufficientBalanceException(String accountNumber, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format(
            "Insufficient balance in account '%s'. Available: %.2f, Requested: %.2f.",
            accountNumber, currentBalance, requestedAmount
        ));
        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    /**
     * Cause-chaining constructor.
     * Use when wrapping a lower-level exception — preserves the original cause
     * in the stack trace for easier debugging.
     */
    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
        this.accountNumber = null;
        this.currentBalance = null;
        this.requestedAmount = null;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
