package dev.lukeholland.tcg.decklists.api.common.exception;

/**
 * Exception thrown when business validation fails.
 * Results in a 400 Bad Request HTTP response with RFC 7807 Problem Details format.
 * <p>
 * This exception is used for business rule validation that cannot be expressed
 * through Jakarta Bean Validation annotations (e.g., checking database state,
 * complex business logic).
 */
public class ValidationException extends RuntimeException {

    /**
     * Creates a new ValidationException with a detail message.
     *
     * @param message the detail message explaining what validation failed
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Creates a new ValidationException with a detail message and cause.
     *
     * @param message the detail message explaining what validation failed
     * @param cause   the underlying cause of the validation failure
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
