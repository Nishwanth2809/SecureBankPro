package com.securebank.pro.validation;

import com.securebank.pro.entity.Account;
import com.securebank.pro.exception.InvalidAccountException;

/**
 * Provides static validation methods for bank account state.
 *
 * Validates that an account is not null and is in an active state
 * before any banking operation is attempted on it.
 *
 * Throws InvalidAccountException (unchecked) on failure —
 * callers are not forced to catch it, but should.
 *
 * Phase 2 — Exception Handling & Validation
 */
public class AccountValidator {

    private AccountValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Validates that an account reference is not null and that the account is active.
     *
     * An account is considered active only after createAccount() has been called on it.
     * Any operation on an inactive account (deposit, withdraw, transfer) should be
     * rejected before it reaches the balance logic.
     *
     * @param account the account to validate
     * @throws InvalidAccountException if account is null or not active
     */
    public static void validateAccountStatus(Account account) {
        if (account == null) {
            throw new InvalidAccountException("Account reference cannot be null.");
        }
        if (!account.isActive()) {
            throw new InvalidAccountException(
                account.getAccountNumber(),
                "Account is not active. Call createAccount() before performing transactions."
            );
        }
    }
}
