package com.securebank.pro.exception;

/**
 * Thrown when a lookup for a user fails — the user does not exist in the system.
 * Common cases: looking up by email or ID that doesn't match any registered user.
 *
 * This is an UNCHECKED (runtime) exception — it extends RuntimeException.
 *
 * Phase 2 — Exception Handling & Validation
 */
public class UserNotFoundException extends RuntimeException {

    private final String identifier;

    /**
     * Simple message constructor.
     */
    public UserNotFoundException(String message) {
        super(message);
        this.identifier = null;
    }

    /**
     * Field-value constructor.
     * Clearly states which field was searched and what value was not found.
     * Example: new UserNotFoundException("email", "ghost@nowhere.com")
     *       -> "No user found with email = 'ghost@nowhere.com'."
     */
    public UserNotFoundException(String fieldName, String value) {
        super("No user found with " + fieldName + " = '" + value + "'.");
        this.identifier = value;
    }

    /**
     * Cause-chaining constructor.
     * Preserves the original exception when wrapping database or lookup failures.
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.identifier = null;
    }

    /**
     * Returns the identifier (value) that was searched for, or null if not applicable.
     */
    public String getIdentifier() {
        return identifier;
    }
}
