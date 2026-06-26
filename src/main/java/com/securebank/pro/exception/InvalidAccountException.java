package com.securebank.pro.exception;

/**
 * Thrown when an operation is attempted on a bank account that is invalid,
 * inactive, null, or does not belong to the current user.
 *
 * This is an UNCHECKED (runtime) exception — it extends RuntimeException.
 * Used to guard operations like deposit/withdraw/transfer from bad account state.
 *
 * Phase 2 — Exception Handling & Validation
 */
public class InvalidAccountException extends RuntimeException {

    private final String accountNumber;

    /**
     * Simple message constructor.
     * Use when the account number is not available or not applicable.
     */
    public InvalidAccountException(String message) {
        super(message);
        this.accountNumber = null;
    }

    /**
     * Account-specific constructor.
     * Automatically builds a descriptive message that includes the account number
     * and the reason for invalidity.
     */
    public InvalidAccountException(String accountNumber, String reason) {
        super("Account '" + accountNumber + "' is invalid: " + reason);
        this.accountNumber = accountNumber;
    }

    /**
     * Cause-chaining constructor.
     * Use when wrapping a lower-level exception — preserves the original cause.
     */
    public InvalidAccountException(String message, Throwable cause) {
        super(message, cause);
        this.accountNumber = null;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
