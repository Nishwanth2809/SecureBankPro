package com.securebank.pro.validation;

import java.math.BigDecimal;

/**
 * Provides static validation methods for financial transaction amounts.
 *
 * Ensures amounts are non-null, positive, have at most 2 decimal places,
 * and do not exceed the per-transaction limit.
 *
 * Throws IllegalArgumentException (unchecked) on failure.
 *
 * Phase 2 — Exception Handling & Validation
 */
public class TransactionValidator {

    /**
     * Maximum amount allowed per single transaction.
     * This is a business rule — adjust as needed.
     */
    private static final BigDecimal MAX_TRANSACTION_LIMIT = new BigDecimal("100000.00");

    private TransactionValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Validates a transfer/deposit/withdrawal amount.
     *
     * Rules enforced:
     * <ul>
     *   <li>Amount must not be null</li>
     *   <li>Amount must be greater than zero</li>
     *   <li>Amount must not have more than 2 decimal places (cents)</li>
     *   <li>Amount must not exceed the per-transaction limit</li>
     * </ul>
     *
     * @param amount the BigDecimal amount to validate
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public static void validateTransferAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Transaction amount cannot be null.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Transaction amount must be greater than zero. Provided: " + amount + "."
            );
        }
        // stripTrailingZeros() handles cases like 100.000 correctly
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                "Transaction amount cannot have more than 2 decimal places. Provided: " + amount + "."
            );
        }
        if (amount.compareTo(MAX_TRANSACTION_LIMIT) > 0) {
            throw new IllegalArgumentException(
                "Transaction amount of " + amount + " exceeds the per-transaction limit of "
                + MAX_TRANSACTION_LIMIT + "."
            );
        }
    }
}
