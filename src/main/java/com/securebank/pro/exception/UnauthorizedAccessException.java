package com.securebank.pro.exception;

/**
 * Thrown when a user attempts an action they do not have permission to perform.
 * Common cases: wrong credentials on login, a CUSTOMER trying to perform
 * an ADMIN-only action, or accessing another user's account.
 *
 * This is an UNCHECKED (runtime) exception — it extends RuntimeException.
 *
 * Phase 2 — Exception Handling & Validation
 */
public class UnauthorizedAccessException extends RuntimeException {

    private final String userEmail;
    private final String action;

    /**
     * Simple message constructor.
     */
    public UnauthorizedAccessException(String message) {
        super(message);
        this.userEmail = null;
        this.action = null;
    }

    /**
     * User-and-action constructor.
     * Automatically builds a message that tells WHO tried to do WHAT.
     * Useful for audit logs or detailed error responses.
     */
    public UnauthorizedAccessException(String userEmail, String action) {
        super("User '" + userEmail + "' is not authorized to perform action: '" + action + "'.");
        this.userEmail = userEmail;
        this.action = action;
    }

    /**
     * Cause-chaining constructor.
     * Preserves the original cause when re-throwing as this exception type.
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
        this.userEmail = null;
        this.action = null;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getAction() {
        return action;
    }
}
