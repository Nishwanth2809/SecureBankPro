package com.securebank.pro.exception;

/**
 * Thrown when a financial transaction fails for a recoverable business reason,
 * such as invalid accounts, insufficient funds, or validation failures
 * that are caught and wrapped during a transfer.
 *
 * THIS IS A CHECKED EXCEPTION — it extends Exception (not RuntimeException).
 *
 * What does "checked" mean?
 *   The Java compiler FORCES every caller of a method that throws this exception
 *   to either:
 *     (a) Catch it in a try-catch block, OR
 *     (b) Declare it in their own method signature using "throws TransactionFailedException"
 *
 * Why use a checked exception here?
 *   A failed transfer is a scenario the calling code must explicitly acknowledge.
 *   It is not a programming error (like NullPointerException) — it is a business
 *   failure that callers should plan for and handle gracefully.
 *
 * Phase 2 — Exception Handling & Validation
 */
public class TransactionFailedException extends Exception {

    private final String referenceId;

    /**
     * Simple message constructor.
     * Use when no transaction reference is available yet.
     */
    public TransactionFailedException(String message) {
        super(message);
        this.referenceId = null;
    }

    /**
     * Cause-chaining constructor.
     * Use to wrap an underlying unchecked exception (e.g. InsufficientBalanceException)
     * into this checked exception. The original cause is preserved in the stack trace.
     *
     * Example flow:
     *   InsufficientBalanceException (unchecked, thrown deep inside)
     *       -> caught by transferMoney()
     *       -> re-thrown as TransactionFailedException (checked)
     *       -> caller is forced to handle it
     */
    public TransactionFailedException(String message, Throwable cause) {
        super(message, cause);
        this.referenceId = null;
    }

    /**
     * Full constructor with reference ID.
     * Once a transaction reference number is known, include it for traceability.
     */
    public TransactionFailedException(String referenceId, String message, Throwable cause) {
        super("[" + referenceId + "] " + message, cause);
        this.referenceId = referenceId;
    }

    /**
     * Returns the transaction reference ID, or null if not assigned.
     */
    public String getReferenceId() {
        return referenceId;
    }
}
