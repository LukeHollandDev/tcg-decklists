package dev.lukeholland.tcg.decklists.api.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Global exception handler for all REST controllers.
 * Converts exceptions into RFC 7807 Problem Details format.
 * <p>
 * This handler provides consistent error responses across the entire API,
 * following the RFC 7807 standard for HTTP API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TIMESTAMP_PROPERTY = "timestamp";

    /**
     * Handle entity not found exceptions (404).
     * <p>
     * Triggered when a requested resource does not exist in the database.
     *
     * @param ex the EntityNotFoundException
     * @return RFC 7807 Problem Detail with 404 status
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Entity Not Found");
        problemDetail.setType(URI.create("/errors/not-found"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        problemDetail.setProperty("entityType", ex.getEntityType());
        problemDetail.setProperty("entityId", ex.getEntityId());

        return problemDetail;
    }

    /**
     * Handle validation exceptions (400).
     * <p>
     * Triggered when business validation fails (e.g., invalid card IDs,
     * business rule violations).
     *
     * @param ex the ValidationException
     * @return RFC 7807 Problem Detail with 400 status
     */
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("/errors/validation"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        return problemDetail;
    }

    /**
     * Handle illegal argument exceptions (400).
     * <p>
     * Provides backward compatibility with existing code that throws
     * IllegalArgumentException for validation failures.
     *
     * @param ex the IllegalArgumentException
     * @return RFC 7807 Problem Detail with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Argument");
        problemDetail.setType(URI.create("/errors/invalid-argument"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        return problemDetail;
    }

    /**
     * Generic handler for unexpected exceptions (500).
     * <p>
     * Catches all unhandled exceptions to prevent leaking internal details.
     * In production, the full exception should be logged but not exposed
     * to the client.
     *
     * @param ex the unexpected exception
     * @return RFC 7807 Problem Detail with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        LOG.error("Unexpected error in REST API", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("/errors/internal"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        return problemDetail;
    }
}
