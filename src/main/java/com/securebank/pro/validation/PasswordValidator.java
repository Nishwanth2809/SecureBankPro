package com.securebank.pro.validation;

/**
 * Provides static validation methods for user credentials.
 *
 * Validates email format and password strength rules.
 * Throws IllegalArgumentException (unchecked) on failure.
 *
 * Phase 2 — Exception Handling & Validation
 */
public class PasswordValidator {

    /**
     * Regex: standard email pattern.
     * Allows local part with letters, digits, dots, underscores, percent, plus, hyphen.
     * Requires @ symbol, domain, and a TLD of 2+ letters.
     */
    private static final String EMAIL_REGEX = "^[\\w.%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$";

    private static final int MIN_PASSWORD_LENGTH = 8;

    private PasswordValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Validates that an email address is non-blank and matches the standard format.
     *
     * @param email the email string to validate
     * @throws IllegalArgumentException if email is null, blank, or malformed
     */
    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank.");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException(
                "Invalid email format: '" + email + "'. Expected format: user@domain.com"
            );
        }
    }

    /**
     * Validates password strength using the following rules:
     * <ul>
     *   <li>At least 8 characters long</li>
     *   <li>Contains at least one uppercase letter (A–Z)</li>
     *   <li>Contains at least one digit (0–9)</li>
     *   <li>Contains at least one special character</li>
     * </ul>
     *
     * Each rule is checked separately so the error message is specific
     * about exactly which rule was violated.
     *
     * @param password the password string to validate
     * @throws IllegalArgumentException if any password rule is violated
     */
    public static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long. "
                + "Provided length: " + password.length() + "."
            );
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException(
                "Password must contain at least one uppercase letter (A-Z)."
            );
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException(
                "Password must contain at least one digit (0-9)."
            );
        }
        if (!password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:',.<>?/`~].*")) {
            throw new IllegalArgumentException(
                "Password must contain at least one special character (e.g. @, #, !, %)."
            );
        }
    }
}
