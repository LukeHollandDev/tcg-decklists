# Backend Refactoring Plan - TCG Decklists API

**Version:** 1.0
**Date:** 2025-11-16
**Current Stack:** Java 21, Spring Boot 3.5.6, PostgreSQL 18
**Status:** Ready for Implementation

---

## Executive Summary

This document outlines comprehensive improvements to enhance the readability, maintainability, and modernization of the
TCG Decklists backend API. The codebase currently demonstrates solid architecture and modern Java practices, but several
enhancements will bring it to production-ready, enterprise-grade quality.

### Current Assessment

- **Architecture:** Strong foundation with feature-based packaging (B+/A-)
- **Readability:** 9/10 - Clean, well-organized code
- **Maintainability:** 8/10 - Good structure, some technical debt areas
- **Modernization:** 7.5/10 - Uses Java 21+ but missing key Spring Boot 3.5 patterns

### Goals

1. Implement industry-standard error handling (RFC 7807)
2. Adopt declarative validation patterns
3. Enable production monitoring and observability
4. Optimize performance with caching and virtual threads
5. Future-proof with API versioning
6. Maximize Java 21 and Spring Boot 3.5 features

---

## Progress Tracking

**Last Updated:** 2025-11-16
**Overall Status:** 4/14 improvements completed (29%)

| #  | Improvement                   | Priority | Status        | Completed  | Notes             |
|----|-------------------------------|----------|---------------|------------|-------------------|
| 1  | Exception Handling (RFC 7807) | HIGH     | ✅ Done        | 2025-11-16 | All tests passing |
| 2  | Bean Validation               | HIGH     | ✅ Done        | 2025-11-16 | All tests passing |
| 3  | API Versioning                | HIGH     | ✅ Done        | 2025-11-16 | All tests passing |
| 4  | CardSearchRequest to Record   | HIGH     | ✅ Done        | 2025-11-16 | All tests passing |
| 5  | Spring Boot Actuator          | HIGH     | ⏳ Not Started | -          | -                 |
| 6  | Application Properties        | HIGH     | ⏳ Not Started | -          | -                 |
| 7  | Caching Strategy              | MEDIUM   | ⏳ Not Started | -          | -                 |
| 8  | Virtual Threads               | MEDIUM   | ⏳ Not Started | -          | -                 |
| 9  | Clean Up Common Package       | MEDIUM   | ⏳ Not Started | -          | -                 |
| 10 | Standardize Validation        | MEDIUM   | ⏳ Not Started | -          | -                 |
| 11 | Enhanced OpenAPI Config       | LOW      | ⏳ Not Started | -          | -                 |
| 12 | Query Optimization            | LOW      | ⏳ Not Started | -          | -                 |
| 13 | Modern Java 21 Patterns       | LOW      | ⏳ Not Started | -          | -                 |
| 14 | Structured Logging            | LOW      | ⏳ Not Started | -          | -                 |

**Status Legend:**

- ✅ **Done** - Completed and tested
- 🚧 **In Progress** - Currently being implemented
- ⏸️ **Blocked** - Waiting on dependencies or decisions
- ⏳ **Not Started** - Ready to begin
- ❌ **Cancelled** - Decided not to implement
- 🔄 **Needs Revision** - Completed but needs changes

**Sprint Progress:**

- **Sprint 1 (High Priority):** 4/6 completed
- **Sprint 2 (Medium Priority):** 0/4 completed
- **Additional Improvements:** 0/4 completed

---

## How to Use This Document

Each improvement is structured as follows:

- **Priority & Effort:** High/Medium/Low priority, Effort estimate
- **Rationale:** Why this improvement matters
- **Current State:** What exists today
- **Proposed Solution:** What we're implementing
- **Pros:** Benefits of this change
- **Implementation Steps:** Detailed step-by-step guide
- **Code Examples:** Working examples you can adapt
- **Impact Assessment:** Expected benefits and risks

**Recommended Approach:** Implement improvements in order, completing all High Priority items first. Each improvement is
self-contained and can be implemented independently.

---

## Table of Contents

### High Priority (Sprint 1)

1. [Centralized Exception Handling with RFC 7807](#1-centralized-exception-handling-with-rfc-7807)
2. [Jakarta Bean Validation Implementation](#2-jakarta-bean-validation-implementation)
3. [API Versioning Strategy](#3-api-versioning-strategy)
4. [Convert CardSearchRequest to Record](#4-convert-cardsearchrequest-to-record)
5. [Spring Boot Actuator Integration](#5-spring-boot-actuator-integration)
6. [Application Properties Configuration](#6-application-properties-configuration)

### Medium Priority (Sprint 2)

7. [Caching Strategy Implementation](#7-caching-strategy-implementation)
8. [Enable Virtual Threads](#8-enable-virtual-threads)
9. [Clean Up Common Package Structure](#9-clean-up-common-package-structure)
10. [Standardize Validation Layer](#10-standardize-validation-layer)

### Additional Improvements

11. [Enhanced OpenAPI Configuration](#11-enhanced-openapi-configuration)
12. [Query Optimization Recommendations](#12-query-optimization-recommendations)
13. [Modern Java 21 Patterns](#13-modern-java-21-patterns)
14. [Structured Logging Improvements](#14-structured-logging-improvements)

---

# High Priority Improvements (Sprint 1)

---

## 1. Centralized Exception Handling with RFC 7807

**Priority:** HIGH
**Effort:** Medium (4-6 hours)
**Files Affected:** New GlobalExceptionHandler, all controllers
**Status:** ✅ Done (2025-11-16)

---

### Implementation Checklist

- [x] Create custom exception classes (EntityNotFoundException, ValidationException)
- [x] Create GlobalExceptionHandler with @RestControllerAdvice
- [x] Update PokemonCardController to use exceptions
- [x] Update DecklistController to use exceptions
- [x] Update application.properties with Problem Details config
- [x] Test error responses with curl/Postman
- [ ] Update frontend error handling to parse ProblemDetail format (Deferred - backend only)
- [x] Update integration tests for new error format

### Implementation Notes

**Completed:** 2025-11-16

**Actual time taken:** ~3 hours

**What was implemented:**

- Created 2 custom exception classes (EntityNotFoundException, ValidationException)
- Note: Did NOT implement DuplicateEntityException as duplicate decklist names are acceptable
- Created GlobalExceptionHandler with @RestControllerAdvice handling 4 exception types:
    - EntityNotFoundException → 404
    - ValidationException → 400
    - IllegalArgumentException → 400 (backward compatibility)
    - Generic Exception → 500
- Updated PokemonCardController: Removed ResponseEntity.notFound() and .badRequest() patterns
- Updated DecklistController: Removed all try-catch blocks, simplified return types
- Updated application.properties with RFC 7807 Problem Details configuration
- Updated 2 integration test files with comprehensive RFC 7807 validation:
    - CardErrorHandlingIntegrationTest.java
    - DecklistIntegrationTest.java
    - CardFeaturesAndAutocompleteIntegrationTest.java (1 test)
- All integration tests passing ✅

**Deviations from plan:**

- Frontend error handling deferred to separate task (backend-only focus)
- Did not create DuplicateEntityException (not needed per user requirements)

**Lessons learned:**

- Breaking change approach simplified implementation significantly
- Spring Boot 3.5's built-in RFC 7807 support made implementation straightforward
- Controllers are now much cleaner without ResponseEntity error handling
- Comprehensive test updates were critical to verify RFC 7807 compliance

---

### Rationale

Currently, error handling is inconsistent across controllers:

- `PokemonCardController` uses Optional/ResponseEntity pattern
- `DecklistController` uses try-catch with `Map.of("error", message)`
- Exceptions leak implementation details to API consumers

**RFC 7807 (Problem Details)** is the industry standard for REST API error responses, providing:

- Consistent error format across all endpoints
- Machine-readable error types
- Human-readable error details
- Additional context when needed

### Current State

**PokemonCardController:**

```java

@GetMapping("/autocomplete")
public ResponseEntity<List<String>> autocomplete(...) {
    if (query == null || query.trim().isEmpty()) {
        return ResponseEntity.badRequest().build();  // No error message!
    }
    // ...
}
```

**DecklistController:**

```java

@PostMapping
public ResponseEntity<?> createDecklist(@RequestBody DecklistRequest request) {
    try {
        // ...
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));  // Inconsistent format
    }
}
```

### Proposed Solution

Implement `@RestControllerAdvice` with Spring Boot 3's built-in `ProblemDetail` support (RFC 7807).

### Pros

✅ **Consistency:** All endpoints return errors in the same format
✅ **Standards-based:** RFC 7807 is the industry standard
✅ **Client-friendly:** Structured errors are easier to parse
✅ **Centralized:** One place to manage all error handling logic
✅ **Extensible:** Easy to add custom error properties
✅ **Spring Boot Native:** Built-in support in Spring Boot 3.x
✅ **Reduces Boilerplate:** No try-catch in every controller method

### Implementation Steps

#### Step 1: Create Custom Exception Classes

Create domain-specific exceptions for business logic errors.

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/common/exception/EntityNotFoundException.java`

```java
package dev.lukeholland.tcg.decklists.api.common.exception;

public class EntityNotFoundException extends RuntimeException {
    private final String entityType;
    private final String entityId;

    public EntityNotFoundException(String entityType, String entityId) {
        super(String.format("%s not found with id: %s", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }
}
```

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/common/exception/ValidationException.java`

```java
package dev.lukeholland.tcg.decklists.api.common.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### Step 2: Create Global Exception Handler

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/common/exception/GlobalExceptionHandler.java`

```java
package dev.lukeholland.tcg.decklists.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP_PROPERTY = "timestamp";
    private static final String ERRORS_PROPERTY = "errors";

    /**
     * Handle entity not found exceptions (404)
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
     * Handle validation exceptions (400)
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
     * Handle Bean Validation errors (400)
     * Triggered when @Valid fails on request bodies
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields"
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("/errors/validation"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        problemDetail.setProperty(ERRORS_PROPERTY, errors);

        return problemDetail;
    }

    /**
     * Handle illegal argument exceptions (400)
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
     * Generic handler for unexpected exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("/errors/internal"));
        problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        // In production, you might want to log the full exception
        // but not expose internal details to the client
        // log.error("Unexpected error", ex);

        return problemDetail;
    }
}
```

#### Step 3: Update Controllers to Use Custom Exceptions

**Before (PokemonCardController):**

```java

@GetMapping("/{id}")
public ResponseEntity<CardResponse> getCardById(@PathVariable String id) {
    return service.findById(id)
            .map(CardResponse::new)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

**After:**

```java

@GetMapping("/{id}")
public CardResponse getCardById(@PathVariable String id) {
    return service.findById(id)
            .map(CardResponse::new)
            .orElseThrow(() -> new EntityNotFoundException("Card", id));
}
```

**Before (DecklistController):**

```java

@PostMapping
public ResponseEntity<?> createDecklist(@RequestBody DecklistRequest request) {
    try {
        Decklist decklist = decklistService.createDecklist(request);
        return ResponseEntity.ok(new DecklistResponse(decklist));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
```

**After:**

```java

@PostMapping
public DecklistResponse createDecklist(@Valid @RequestBody DecklistRequest request) {
    Decklist decklist = decklistService.createDecklist(request);
    return new DecklistResponse(decklist);
}
```

#### Step 4: Update Service Layer

Remove try-catch blocks and let exceptions propagate to the global handler.

**Before (DecklistService):**

```java
public Decklist createDecklist(DecklistRequest request) {
    if (request.name() == null || request.name().trim().isEmpty()) {
        throw new IllegalArgumentException("Decklist name is required");
    }
    // ...
}
```

**After:**

```java
public Decklist createDecklist(DecklistRequest request) {
    // Validation now handled by @Valid in controller
    // Business logic only
    Decklist decklist = new Decklist();
    decklist.setName(request.name());
    decklist.setType(request.type());
    // ...
    return repository.save(decklist);
}
```

#### Step 5: Configure Application Properties

Add to `application.properties`:

```properties
# Problem Details configuration
spring.mvc.problemdetails.enabled=true
server.error.include-message=always
server.error.include-binding-errors=always
server.error.include-exception=false
server.error.include-stacktrace=never
```

#### Step 6: Test Error Responses

Example error response for validation failure:

```json
{
  "type": "/errors/validation",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed for one or more fields",
  "instance": "/api/v1/decklist",
  "timestamp": "2025-11-16T10:30:00Z",
  "errors": {
    "name": "must not be blank",
    "cards": "must not be empty"
  }
}
```

Example error response for not found:

```json
{
  "type": "/errors/not-found",
  "title": "Entity Not Found",
  "status": 404,
  "detail": "Card not found with id: sv3-999",
  "instance": "/api/v1/pokemon/sv3-999",
  "timestamp": "2025-11-16T10:30:00Z",
  "entityType": "Card",
  "entityId": "sv3-999"
}
```

### Impact Assessment

**Benefits:**

- All errors now have consistent structure
- Better API consumer experience
- Easier to debug with structured error information
- Cleaner controller code (no ResponseEntity.badRequest() everywhere)
- Follows REST best practices

**Risks:**

- Frontend may need updates to handle new error format
- Existing API consumers need to adapt (breaking change)

**Migration Notes:**

- Update frontend error handling to parse ProblemDetail format
- Document new error format in OpenAPI spec
- Consider running old and new format in parallel during migration

---

## 2. Jakarta Bean Validation Implementation

**Priority:** HIGH
**Effort:** Medium (3-5 hours)
**Files Affected:** All DTOs, Controllers, build.gradle.kts
**Status:** ✅ Done (2025-11-16)

---

### Implementation Checklist

- [x] Add spring-boot-starter-validation dependency to build.gradle.kts
- [x] Update DecklistRequest with validation annotations
- [x] Update DecklistController to use @Valid
- [x] Remove manual validation from DecklistService
- [ ] Create validated CardSearchRequest (preview for Improvement #4 - deferred)
- [x] Test validation error responses
- [x] Update integration tests

### Implementation Notes

**Completed:** 2025-11-16

**Actual time taken:** ~2 hours

**What was implemented:**

- Added spring-boot-starter-validation dependency to build.gradle.kts
- Updated DecklistRequest with @NotBlank, @NotNull, and @NotEmpty validation annotations
- Added @Valid annotation to DecklistController.createDecklist method
- Removed manual input validation from DecklistService (kept business logic validation for card IDs)
- Extended GlobalExceptionHandler from ResponseEntityExceptionHandler
- Overrode handleMethodArgumentNotValid to provide custom RFC 7807 error format with field-specific errors
- Updated 4 integration tests to expect new Bean Validation error format
- Added Problem Details configuration to test setup (AbstractIntegrationTest)
- All 274 tests passing ✅

**Challenges encountered:**

- Initial implementation using @ExceptionHandler didn't work because Spring Boot's built-in handlers took precedence
- Solution: Extended ResponseEntityExceptionHandler and overrode handleMethodArgumentNotValid instead
- Tests were failing with type="about:blank" until proper override was implemented

**Deviations from plan:**

- CardSearchRequest validation deferred to Improvement #4 as planned
- Had to extend ResponseEntityExceptionHandler instead of just using @RestControllerAdvice

**Lessons learned:**

- When spring.mvc.problemdetails.enabled=true, must extend ResponseEntityExceptionHandler and override methods rather
  than using @ExceptionHandler for Spring-handled exceptions
- Bean Validation integrates seamlessly with RFC 7807 Problem Details
- Service layer is much cleaner with only business logic validation remaining
- Field-specific validation errors provide excellent API consumer experience

---

### Rationale

Currently, validation is scattered across layers:

- DTOs have validation logic in getters (e.g., `CardSearchRequest.getPageSize()`)
- Services validate manually with if-statements
- Controllers have ad-hoc validation checks

**Jakarta Bean Validation** provides declarative, annotation-based validation that's:

- Standardized and well-documented
- Automatically integrated with Spring
- Testable and reusable
- Generates consistent error messages

### Current State

**CardSearchRequest (current class implementation):**

```java
public class CardSearchRequest {
    private Integer pageSize;

    public Integer getPageSize() {
        if (pageSize == null) return 20;
        if (pageSize < 1) return 1;
        if (pageSize > 100) return 100;
        return pageSize;
    }
}
```

**DecklistService:**

```java
public Decklist createDecklist(DecklistRequest request) {
    if (request.name() == null || request.name().trim().isEmpty()) {
        throw new IllegalArgumentException("Decklist name is required");
    }
    if (request.cards() == null || request.cards().isEmpty()) {
        throw new IllegalArgumentException("Decklist must contain at least one card");
    }
    // ...
}
```

### Proposed Solution

Use Jakarta Bean Validation annotations on DTOs and `@Valid` in controllers.

### Pros

✅ **Declarative:** Validation rules visible at a glance in DTO definitions
✅ **Consistent:** Same validation framework across all DTOs
✅ **Automatic:** Spring handles validation before method execution
✅ **Better Errors:** Generates field-specific error messages
✅ **Testable:** Easy to unit test validation rules
✅ **Less Code:** No manual if-statements in services
✅ **Standards-based:** Jakarta EE standard, widely supported

### Implementation Steps

#### Step 1: Add Dependency

**File:** `apps/backend/build.gradle.kts`

```kotlin
dependencies {
    // Add Bean Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // ... existing dependencies
}
```

Then run: `./gradlew build --refresh-dependencies`

#### Step 2: Update DecklistRequest

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/decklist/dto/DecklistRequest.java`

**Before:**

```java
public record DecklistRequest(
        String name,
        CardGame type,
        List<String> cards
) {
}
```

**After:**

```java
package dev.lukeholland.tcg.decklists.api.decklist.dto;

import dev.lukeholland.tcg.decklists.api.enums.CardGame;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DecklistRequest(
        @NotBlank(message = "Decklist name is required and cannot be blank")
        String name,

        @NotNull(message = "Card game type is required")
        CardGame type,

        @NotEmpty(message = "Decklist must contain at least one card")
        List<@NotBlank(message = "Card ID cannot be blank") String> cards
) {
}
```

#### Step 3: Update Controllers with @Valid

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/decklist/DecklistController.java`

**Before:**

```java

@PostMapping
public ResponseEntity<?> createDecklist(@RequestBody DecklistRequest request) {
    try {
        Decklist decklist = decklistService.createDecklist(request);
        return ResponseEntity.ok(new DecklistResponse(decklist));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
```

**After:**

```java

@PostMapping
public DecklistResponse createDecklist(@Valid @RequestBody DecklistRequest request) {
    Decklist decklist = decklistService.createDecklist(request);
    return new DecklistResponse(decklist);
}
```

#### Step 4: Remove Manual Validation from Service

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/decklist/DecklistService.java`

**Before:**

```java
public Decklist createDecklist(DecklistRequest request) {
    if (request.name() == null || request.name().trim().isEmpty()) {
        throw new IllegalArgumentException("Decklist name is required");
    }
    if (request.cards() == null || request.cards().isEmpty()) {
        throw new IllegalArgumentException("Decklist must contain at least one card");
    }
    // ... business logic
}
```

**After:**

```java
public Decklist createDecklist(DecklistRequest request) {
    // Validation already done by @Valid in controller
    // Only business logic here
    Decklist decklist = new Decklist();
    decklist.setName(request.name());
    decklist.setType(request.type());
    // ... rest of business logic
    return repository.save(decklist);
}
```

#### Step 5: Create Validated CardSearchRequest Record

This will be covered in detail in [Improvement #4](#4-convert-cardsearchrequest-to-record), but here's a preview:

```java
package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record CardSearchRequest(
        String name,

        @Min(value = 0, message = "Page number must be 0 or greater")
        Integer page,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer pageSize,

        @Pattern(regexp = "asc|desc", message = "Sort order must be 'asc' or 'desc'")
        String sortOrder,

        // ... other fields
) {
    // Compact constructor for defaults
    public CardSearchRequest {
        page = (page == null) ? 0 : page;
        pageSize = (pageSize == null) ? 20 : pageSize;
        sortOrder = (sortOrder == null) ? "asc" : sortOrder;
    }
}
```

#### Step 6: Common Validation Annotations Reference

```java
// String validations
@NotNull        // Must not be null
@NotBlank       // Must not be null, empty, or whitespace-only
@NotEmpty       // Must not be null or empty (but can be whitespace)
@Size(min = 1, max = 100)  // String length or collection size
@Pattern(regexp = "...")  // Regex pattern match
@Email          // Must be valid email format

// Number validations
@Min(0)         // Minimum value (inclusive)
@Max(100)       // Maximum value (inclusive)
@Positive       // Must be positive (> 0)
@PositiveOrZero // Must be >= 0
@Negative       // Must be negative (< 0)

// Collection validations
@NotEmpty       // Collection must not be empty
@Size(min = 1, max = 10)  // Collection size constraints

// Custom messages
@NotNull(message = "Custom error message")
```

### Impact Assessment

**Benefits:**

- Cleaner service layer (only business logic)
- Consistent validation across all endpoints
- Better error messages for API consumers
- Easier to understand validation rules (declarative in DTO)
- Reduced boilerplate code

**Risks:**

- Need to update all DTOs that currently have manual validation
- May require updating existing tests
- Custom validation messages need to be consistent

**Migration Notes:**

- Review all service methods for manual validation and remove
- Update DTOs one at a time to minimize risk
- Ensure all controllers use @Valid on validated request bodies

---

## 3. API Versioning Strategy

**Priority:** HIGH
**Effort:** Medium (3-4 hours)
**Files Affected:** All controllers, potentially frontend
**Status:** ✅ Done (2025-11-16)

---

### Implementation Checklist

- [x] Update PokemonCardController RequestMapping to /api/v1/pokemon
- [x] Update DecklistController RequestMapping to /api/v1/decklist
- [x] Update all integration test URLs (14 test files updated)
- [ ] Create ApiConstants class with version constants (skipped - opted for direct updates)
- [ ] Update frontend API base URL to /api/v1 (deferred - backend only implementation)
- [ ] Update OpenAPI configuration with version info (not needed - SpringDoc auto-discovers)
- [ ] Update README and documentation (to be done separately)

### Implementation Notes

> **Completed on:** 2025-11-16
> - **Actual time taken:** ~15 minutes
> - **Approach:** Simple find-and-replace strategy for test URLs
> - **Files changed:** 2 controllers + 14 integration test files = 16 files total
> - **Test results:** All integration tests passing ✅
> - **Deviations from plan:** Skipped ApiConstants class as per preference for simplicity
> - **Frontend:** Deferred for later - backend only implementation
> - **OpenAPI:** No configuration changes needed - SpringDoc auto-discovers from @RequestMapping annotations

---

### Rationale

Currently, API endpoints use `/api/pokemon` and `/api/decklist` without versioning. Adding versioning now:

- Allows breaking changes in the future without disrupting existing consumers
- Follows REST best practices
- Makes API evolution easier
- Signals API maturity to consumers

### Current State

```java

@RestController
@RequestMapping("/api/pokemon")
public class PokemonCardController {
}

@RestController
@RequestMapping("/api/decklist")
public class DecklistController {
}
```

### Proposed Solution

Add `/v1/` prefix to all API endpoints: `/api/v1/pokemon`, `/api/v1/decklist`

### Pros

✅ **Future-proof:** Can introduce /v2 for breaking changes
✅ **Clear API Contract:** Version number signals stability expectations
✅ **Parallel Versions:** Can run v1 and v2 simultaneously during migration
✅ **Industry Standard:** Common practice in REST APIs
✅ **Documentation:** Makes it clear which version consumers are using

### Implementation Steps

#### Step 1: Update Controller RequestMappings

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/pokemon/PokemonCardController.java`

**Before:**

```java

@RestController
@RequestMapping("/api/pokemon")
@Tag(name = "Pokemon Cards", description = "Endpoints for searching and retrieving Pokemon TCG cards")
public class PokemonCardController {
    // ...
}
```

**After:**

```java

@RestController
@RequestMapping("/api/v1/pokemon")
@Tag(name = "Pokemon Cards", description = "Endpoints for searching and retrieving Pokemon TCG cards")
public class PokemonCardController {
    // ...
}
```

#### Step 2: Update DecklistController

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/decklist/DecklistController.java`

**Before:**

```java

@RestController
@RequestMapping("/api/decklist")
@Tag(name = "Decklists", description = "Endpoints for managing decklists")
public class DecklistController {
    // ...
}
```

**After:**

```java

@RestController
@RequestMapping("/api/v1/decklist")
@Tag(name = "Decklists", description = "Endpoints for managing decklists")
public class DecklistController {
    // ...
}
```

#### Step 3: Update OpenAPI Configuration (if created)

If you create an OpenAPI config class (see [Improvement #11](#11-enhanced-openapi-configuration)):

```java

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TCG Decklists API")
                        .version("1.0")
                        .description("Version 1 of the TCG Decklists API"));
    }
}
```

#### Step 4: Optional - Create Version Constants

For maintainability, consider creating a constants class:

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/common/ApiConstants.java`

```java
package dev.lukeholland.tcg.decklists.api.common;

public final class ApiConstants {
    private ApiConstants() {
        // Prevent instantiation
    }

    public static final String API_VERSION_1 = "/api/v1";

    // Future versions
    // public static final String API_VERSION_2 = "/api/v2";
}
```

Then use in controllers:

```java
import static dev.lukeholland.tcg.decklists.api.common.ApiConstants.API_VERSION_1;

@RestController
@RequestMapping(API_VERSION_1 + "/pokemon")
public class PokemonCardController {
    // ...
}
```

#### Step 5: Update Frontend API Calls

Update the frontend base URL from `/api/` to `/api/v1/`:

**File:** `apps/frontend/src/config/api.ts` (or wherever API base URL is defined)

**Before:**

```typescript
export const API_BASE_URL = 'http://localhost:8080/api';
```

**After:**

```typescript
export const API_BASE_URL = 'http://localhost:8080/api/v1';
```

#### Step 6: Update Integration Tests

Update all test URLs to include `/v1`:

**Before:**

```java
mockMvc.perform(get("/api/pokemon/{id}", cardId))
```

**After:**

```java
mockMvc.perform(get("/api/v1/pokemon/{id}", cardId))
```

Consider creating a test constant:

```java
private static final String BASE_URL = "/api/v1/pokemon";

mockMvc.

perform(get(BASE_URL+"/{id}", cardId))
```

### Impact Assessment

**Benefits:**

- Can introduce breaking changes in v2 without disrupting v1 users
- Clear API version in URLs makes debugging easier
- Follows industry best practices
- Prepares for future growth

**Risks:**

- Requires frontend updates
- All existing API consumers need to update URLs
- Need to update all tests

**Migration Notes:**

- This is a **breaking change** - existing API consumers will need to update
- Consider supporting both `/api/pokemon` and `/api/v1/pokemon` temporarily
- Update all documentation, Swagger UI, and README files
- Communicate change to any external API consumers

### Alternative Versioning Strategies (Not Recommended for Now)

For reference, other versioning approaches exist but are not recommended for this project:

**Header-based versioning:**

```http
Accept: application/vnd.tcg-decklists.v1+json
```

- Pros: Cleaner URLs
- Cons: Less discoverable, harder to test with curl/Postman

**Query parameter versioning:**

```
/api/pokemon?version=1
```

- Pros: Easy to change version
- Cons: Not RESTful, easy to forget

**Recommendation:** Stick with URL path versioning (`/api/v1/`) as it's the most common and discoverable approach.

---

## 4. Convert CardSearchRequest to Record

**Priority:** HIGH
**Effort:** Small (1-2 hours)
**Files Affected:** CardSearchRequest, PokemonCardController, PokemonCardService
**Status:** ✅ Done (2025-11-16)

---

### Implementation Checklist

- [x] Analyze current CardSearchRequest fields and validation logic
- [x] Create new record with all fields
- [x] Add Bean Validation annotations
- [x] Implement compact constructor for defaults
- [x] Update PokemonCardController to use @Valid
- [x] Update PokemonCardService to use record accessors (page() instead of getPage())
- [x] Create builder for tests (optional) - Not needed; tests use HTTP request parameters
- [x] Update all tests

### Implementation Notes

> **Implementation completed successfully:**
> - **Actual time taken:** ~30 minutes
> - **Challenges encountered:** None - conversion was straightforward
> - **Deviations from plan:**
    >
- No builder pattern needed for tests as integration tests use HTTP request parameters directly
>   - No test file changes required
> - **Lessons learned:**
    >
- Record conversion significantly reduced code from 498 lines to 156 lines (-69%)
>   - Compact constructor cleanly handles default values and bounds enforcement
>   - Bean Validation annotations work seamlessly with @ModelAttribute binding
>   - All 45 fields successfully converted with validation preserved
>   - Tests passed without modification, confirming backward compatibility

---

### Rationale

`CardSearchRequest` is currently the only DTO still using a class instead of a record. It has validation logic embedded
in getters, which is an anti-pattern. Converting it to a record with Bean Validation:

- Makes it consistent with other DTOs
- Separates validation from data access
- Uses modern Java 21 features
- Simplifies the code

### Current State

**File:** `CardSearchRequest.java` (currently a class)

```java
public class CardSearchRequest {
    private String name;
    private Integer page;
    private Integer pageSize;
    private String sortOrder;
    // ... many more fields

    // Validation in getters - anti-pattern!
    public Integer getPage() {
        if (page == null || page < 0) return 0;
        return page;
    }

    public Integer getPageSize() {
        if (pageSize == null) return 20;
        if (pageSize < 1) return 1;
        if (pageSize > 100) return 100;
        return pageSize;
    }

    public String getSortOrder() {
        if (sortOrder == null) return "asc";
        return sortOrder;
    }

    // ... many more getters/setters
}
```

### Proposed Solution

Convert to a record with:

- Compact constructor for default values
- Bean Validation annotations for constraints
- No validation logic in accessors

### Pros

✅ **Consistency:** Matches all other DTOs (CardResponse, DecklistRequest, etc.)
✅ **Modern Java:** Uses Java 21 records feature
✅ **Immutability:** Records are immutable by default
✅ **Less Code:** No need for explicit getters/setters
✅ **Clearer Intent:** Validation rules visible in field declarations
✅ **Thread-safe:** Immutability provides thread safety

### Implementation Steps

#### Step 1: Analyze Current Fields

Current `CardSearchRequest` has these fields:

- Pagination: page, pageSize, sortField, sortOrder
- Search: name
- Filters: types, subtypes, supertypes, rarities, sets, marks, artists, hp, retreatCost, etc.

#### Step 2: Create New Record with Validation

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/pokemon/dto/CardSearchRequest.java`

```java
package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CardSearchRequest(
        // Search criteria
        String name,

        // Pagination
        @Min(value = 0, message = "Page number must be 0 or greater")
        Integer page,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer pageSize,

        String sortField,

        @Pattern(regexp = "asc|desc", message = "Sort order must be 'asc' or 'desc'")
        String sortOrder,

        // Type filters
        List<String> types,
        List<String> subtypes,
        List<String> supertypes,

        // Card attributes
        List<String> rarities,
        List<String> sets,
        List<String> marks,
        List<String> artists,

        // Numeric filters
        @Min(value = 0, message = "HP minimum must be 0 or greater")
        Integer hpMin,

        @Min(value = 0, message = "HP maximum must be 0 or greater")
        Integer hpMax,

        @Min(value = 0, message = "Retreat cost minimum must be 0 or greater")
        Integer retreatCostMin,

        @Min(value = 0, message = "Retreat cost maximum must be 0 or greater")
        Integer retreatCostMax,

        @Min(value = 0, message = "Attack cost minimum must be 0 or greater")
        Integer attackCostMin,

        @Min(value = 0, message = "Attack cost maximum must be 0 or greater")
        Integer attackCostMax,

        @Min(value = 0, message = "Attack damage minimum must be 0 or greater")
        Integer attackDamageMin,

        @Min(value = 0, message = "Attack damage maximum must be 0 or greater")
        Integer attackDamageMax,

        // Boolean filters
        Boolean hasAbility,
        Boolean hasAttack,
        Boolean hasRule
) {
    /**
     * Compact constructor to apply default values.
     * This is called automatically when creating the record.
     */
    public CardSearchRequest {
        // Apply defaults for null values
        page = (page == null) ? 0 : page;
        pageSize = (pageSize == null) ? 20 : pageSize;
        sortField = (sortField == null || sortField.isBlank()) ? "name" : sortField;
        sortOrder = (sortOrder == null || sortOrder.isBlank()) ? "asc" : sortOrder.toLowerCase();

        // Ensure page/pageSize are within bounds (in case @Min/@Max aren't applied yet)
        page = Math.max(0, page);
        pageSize = Math.min(Math.max(1, pageSize), 100);
    }

    /**
     * Alternative constructor for creating with partial data.
     * Useful in tests.
     */
    public CardSearchRequest(String name) {
        this(name, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);
    }
}
```

#### Step 3: Update Controller to Use @Valid

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/pokemon/PokemonCardController.java`

**Before:**

```java

@GetMapping("/search")
public ResponseEntity<CardSearchResponse> searchCards(@ModelAttribute CardSearchRequest request) {
    return ResponseEntity.ok(service.searchCards(request));
}
```

**After:**

```java

@GetMapping("/search")
public ResponseEntity<CardSearchResponse> searchCards(
        @Valid @ModelAttribute CardSearchRequest request
) {
    return ResponseEntity.ok(service.searchCards(request));
}
```

#### Step 4: Update Service to Trust Validated Input

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/pokemon/PokemonCardService.java`

The service can now trust that:

- `page` is >= 0
- `pageSize` is between 1 and 100
- `sortOrder` is "asc" or "desc"
- All constraints have been validated

**Before:**

```java
public CardSearchResponse searchCards(CardSearchRequest request) {
    int page = request.getPage();  // This applied validation/defaults
    int size = request.getPageSize();  // This applied validation/defaults
    // ...
}
```

**After:**

```java
public CardSearchResponse searchCards(CardSearchRequest request) {
    // No need to call getters - direct field access on records
    // Validation and defaults already applied in compact constructor
    int page = request.page();
    int size = request.pageSize();
    // ...
}
```

#### Step 5: Update Tests

Test builders may need updating:

**Before:**

```java
CardSearchRequest request = new CardSearchRequest();
request.

setName("Pikachu");
request.

setPage(0);
```

**After:**

```java
CardSearchRequest request = new CardSearchRequest(
        "Pikachu",  // name
        0,          // page
        20,         // pageSize
        null,       // sortField (will default to "name")
        null,       // sortOrder (will default to "asc")
        // ... rest of fields
);

// Or use the convenience constructor if only name is needed
CardSearchRequest request = new CardSearchRequest("Pikachu");
```

#### Step 6: Consider Creating a Builder for Tests

For complex test scenarios, consider a builder pattern:

**File:** `apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/builders/CardSearchRequestBuilder.java`

```java
package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardSearchRequest;

import java.util.List;

public class CardSearchRequestBuilder {
    private String name;
    private Integer page = 0;
    private Integer pageSize = 20;
    private String sortField = "name";
    private String sortOrder = "asc";
    private List<String> types;
    // ... other fields with defaults

    public static CardSearchRequestBuilder builder() {
        return new CardSearchRequestBuilder();
    }

    public CardSearchRequestBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CardSearchRequestBuilder page(Integer page) {
        this.page = page;
        return this;
    }

    public CardSearchRequestBuilder pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public CardSearchRequestBuilder types(List<String> types) {
        this.types = types;
        return this;
    }

    // ... other builder methods

    public CardSearchRequest build() {
        return new CardSearchRequest(
                name, page, pageSize, sortField, sortOrder,
                types, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null
        );
    }
}
```

Usage in tests:

```java
CardSearchRequest request = CardSearchRequestBuilder.builder()
        .name("Pikachu")
        .types(List.of("Electric"))
        .pageSize(50)
        .build();
```

### Impact Assessment

**Benefits:**

- Consistent with all other DTOs
- Uses modern Java 21 records
- Cleaner, more maintainable code
- Immutability by default
- Better separation of concerns

**Risks:**

- Need to update all places that create CardSearchRequest
- Tests may need updating
- Getters change from `getPage()` to `page()`

**Migration Notes:**

- Search for all usages of `CardSearchRequest` in the codebase
- Update from setter-based construction to constructor-based
- Update from `request.getPage()` to `request.page()`
- Run all tests to ensure nothing breaks

---

## 5. Spring Boot Actuator Integration

**Priority:** HIGH
**Effort:** Small (1-2 hours)
**Files Affected:** build.gradle.kts, application.properties
**Status:** ✅ Done (2025-11-16)

---

### Implementation Checklist

- [x] Add spring-boot-starter-actuator dependency to build.gradle.kts
- [x] Add micrometer-registry-prometheus dependency (optional)
- [x] Configure actuator endpoints in application.properties
- [x] Add application info properties
- [x] Test /actuator/health endpoint
- [x] Test /actuator/info endpoint
- [x] Test /actuator/metrics endpoint
- [x] Test /actuator/prometheus endpoint (if added)

### Implementation Notes

**Completed:** 2025-11-16

**Actual time taken:** ~30 minutes

**What was implemented:**

- Added `spring-boot-starter-actuator` dependency to build.gradle.kts
- Added `micrometer-registry-prometheus` dependency for Prometheus metrics
- Configured actuator endpoints in application.properties:
    - Exposed endpoints: health, info, metrics, prometheus
    - Enabled liveness and readiness probes
    - Enabled database and disk space health checks
    - Configured info endpoint with app details, Java version, and OS info
- Added application info properties (name, description, version)
- Tested all endpoints successfully:
    - `/actuator/health` - Returns UP status with liveness/readiness groups
    - `/actuator/health/liveness` - Returns UP
    - `/actuator/health/readiness` - Returns UP
    - `/actuator/info` - Shows app info, Java 21.0.9, and OS details
    - `/actuator/metrics` - Lists available metrics
    - `/actuator/prometheus` - Returns Prometheus-formatted metrics

**Challenges encountered:**

- Backend needed to be restarted for new dependencies to load

**Deviations from plan:**

- None - followed the plan exactly as specified

**Lessons learned:**

- Spring Boot Actuator integrates seamlessly with minimal configuration
- Prometheus metrics are available immediately after adding the dependency
- Liveness and readiness probes are essential for Kubernetes/Docker deployments

---

### Rationale

Spring Boot Actuator provides production-ready features for monitoring and managing your application:

- Health checks for readiness/liveness probes
- Metrics for performance monitoring
- Info endpoint for build/version information
- Thread dumps, heap dumps for debugging

This is **essential for production deployments**, especially with Kubernetes/Docker.

### Current State

No actuator dependency or configuration exists.

### Proposed Solution

Add Spring Boot Actuator with carefully exposed endpoints.

### Pros

✅ **Production Readiness:** Essential for running in production
✅ **Health Monitoring:** /actuator/health for load balancer health checks
✅ **Metrics:** Built-in JVM, HTTP, and database metrics
✅ **Observability:** Integration with Prometheus, Grafana
✅ **Low Effort:** Just add dependency and configure
✅ **Debugging:** Access to runtime information when issues occur
✅ **Standards-based:** Industry standard monitoring approach

### Implementation Steps

#### Step 1: Add Dependency

**File:** `apps/backend/build.gradle.kts`

```kotlin
dependencies {
    // Spring Boot Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Optional: Prometheus metrics (recommended for production)
    implementation("io.micrometer:micrometer-registry-prometheus")

    // ... existing dependencies
}
```

Run: `./gradlew build --refresh-dependencies`

#### Step 2: Configure Actuator Endpoints

**File:** `apps/backend/src/main/resources/application.properties`

```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoints.web.base-path=/actuator
management.endpoint.health.show-details=when-authorized
management.endpoint.health.probes.enabled=true
# Health check configuration
management.health.defaults.enabled=true
management.health.db.enabled=true
management.health.diskspace.enabled=true
# Info endpoint
management.info.env.enabled=true
management.info.java.enabled=true
management.info.os.enabled=true
```

#### Step 3: Add Application Info

Add build and version information to the info endpoint:

**File:** `apps/backend/src/main/resources/application.properties`

```properties
# Application Info
info.app.name=TCG Decklists API
info.app.description=Pokemon TCG deck builder and viewer API
info.app.version=1.0.0
info.app.encoding=${file.encoding}
info.app.java.version=${java.version}
```

#### Step 4: Secure Actuator Endpoints (Optional but Recommended)

For production, you'll want to secure sensitive endpoints. For now, we're only exposing safe endpoints.

**Future consideration** - when adding Spring Security:

```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never
# Require authentication for actuator
management.security.enabled=true
```

#### Step 5: Test Actuator Endpoints

Start the application and test:

**Health Check:**

```bash
curl http://localhost:8080/actuator/health
```

Response:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Application Info:**

```bash
curl http://localhost:8080/actuator/info
```

Response:

```json
{
  "app": {
    "name": "TCG Decklists API",
    "description": "Pokemon TCG deck builder and viewer API",
    "version": "1.0.0",
    "encoding": "UTF-8",
    "java": {
      "version": "21.0.1"
    }
  }
}
```

**Metrics:**

```bash
curl http://localhost:8080/actuator/metrics
```

Response:

```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "http.server.requests",
    "hikaricp.connections.active",
    "system.cpu.usage"
    // ... many more
  ]
}
```

**Specific Metric:**

```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

**Prometheus Format (if micrometer-registry-prometheus added):**

```bash
curl http://localhost:8080/actuator/prometheus
```

#### Step 6: Kubernetes Integration (Future)

When deploying to Kubernetes, use actuator for probes:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: tcg-decklists-api
spec:
  containers:
    - name: api
      image: tcg-decklists-api:1.0
      livenessProbe:
        httpGet:
          path: /actuator/health/liveness
          port: 8080
        initialDelaySeconds: 30
        periodSeconds: 10
      readinessProbe:
        httpGet:
          path: /actuator/health/readiness
          port: 8080
        initialDelaySeconds: 10
        periodSeconds: 5
```

### Available Actuator Endpoints

| Endpoint               | Purpose                      | Expose?               |
|------------------------|------------------------------|-----------------------|
| `/actuator/health`     | Application health status    | ✅ Yes                 |
| `/actuator/info`       | Application information      | ✅ Yes                 |
| `/actuator/metrics`    | Application metrics          | ✅ Yes                 |
| `/actuator/prometheus` | Prometheus-formatted metrics | ✅ Yes                 |
| `/actuator/env`        | Environment properties       | ❌ No (sensitive)      |
| `/actuator/loggers`    | Logging configuration        | ❌ No (unless secured) |
| `/actuator/heapdump`   | JVM heap dump                | ❌ No (unless secured) |
| `/actuator/threaddump` | JVM thread dump              | ❌ No (unless secured) |

### Impact Assessment

**Benefits:**

- Production-ready monitoring out of the box
- Easy integration with monitoring tools (Prometheus, Grafana, Datadog)
- Health checks for load balancers
- Performance metrics for optimization
- Debugging capabilities

**Risks:**

- Exposing too many endpoints can leak sensitive information
- Actuator endpoints add minor overhead (negligible)

**Migration Notes:**

- Start with basic endpoints (health, info, metrics)
- Add Prometheus support when ready to set up monitoring
- Secure sensitive endpoints in production

---

## 6. Application Properties Configuration

**Priority:** HIGH
**Effort:** Small (30 minutes)
**Files Affected:** application.properties
**Status:** ⏳ Not Started

---

### Implementation Checklist

- [ ] Add comprehensive database/HikariCP configuration
- [ ] Add JPA/Hibernate configuration (including open-in-view=false)
- [ ] Add Liquibase configuration
- [ ] Add Jackson (JSON) configuration
- [ ] Add server configuration
- [ ] Add logging configuration
- [ ] Add actuator configuration (from Improvement #5)
- [ ] Add cache configuration (preview for Improvement #7)
- [ ] Test application startup with new properties
- [ ] Create profile-specific properties (dev, prod) if needed

### Implementation Notes

> **Add notes here as you implement:**
> - Actual time taken:
> - Challenges encountered:
> - Deviations from plan:
> - Lessons learned:

---

### Rationale

The current `application.properties` file is minimal. Adding explicit configuration:

- Makes behavior predictable and documented
- Prevents unexpected defaults
- Improves performance with specific settings
- Makes development/production differences clear

### Current State

**File:** `apps/backend/src/main/resources/application.properties`

```properties
spring.application.name=tcg.decklists.api
spring.liquibase.enabled=true
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:testing1234}
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:tcg_decklists}
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Proposed Solution

Add comprehensive configuration for JPA, Jackson, logging, and error handling.

### Pros

✅ **Explicit Behavior:** No surprises from Spring Boot defaults
✅ **Performance:** Disable features that hurt performance (open-in-view)
✅ **Better Errors:** More informative error responses
✅ **Development Experience:** Better logging during development
✅ **Production Ready:** Configurations suitable for production

### Implementation Steps

#### Step 1: Create Comprehensive Application Properties

**File:** `apps/backend/src/main/resources/application.properties`

```properties
# ===================================================================
# TCG Decklists API - Application Configuration
# ===================================================================
# Application
spring.application.name=tcg.decklists.api
# ===================================================================
# Database Configuration
# ===================================================================
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:tcg_decklists}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:testing1234}
spring.datasource.driver-class-name=org.postgresql.Driver
# HikariCP Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
# ===================================================================
# JPA / Hibernate Configuration
# ===================================================================
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
# CRITICAL: Disable open-in-view to prevent lazy loading issues
spring.jpa.open-in-view=false
# Hibernate performance
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.query.in_clause_parameter_padding=true
# ===================================================================
# Liquibase Configuration
# ===================================================================
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
# ===================================================================
# Jackson (JSON) Configuration
# ===================================================================
spring.jackson.default-property-inclusion=non_null
spring.jackson.deserialization.fail-on-unknown-properties=false
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=UTC
# ===================================================================
# Server Configuration
# ===================================================================
server.port=8080
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
# Error handling
server.error.include-message=always
server.error.include-binding-errors=always
server.error.include-exception=false
server.error.include-stacktrace=never
# Problem Details (RFC 7807)
spring.mvc.problemdetails.enabled=true
# ===================================================================
# Logging Configuration
# ===================================================================
logging.level.root=INFO
logging.level.dev.lukeholland.tcg.decklists=INFO
logging.level.org.springframework.web=INFO
logging.level.org.springframework.security=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
# ===================================================================
# Actuator Configuration
# ===================================================================
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoints.web.base-path=/actuator
management.endpoint.health.show-details=when-authorized
management.endpoint.health.probes.enabled=true
management.health.defaults.enabled=true
management.health.db.enabled=true
# Application info for /actuator/info
info.app.name=TCG Decklists API
info.app.description=Pokemon TCG deck builder and viewer API
info.app.version=1.0.0
info.app.encoding=${file.encoding}
info.app.java.version=${java.version}
# ===================================================================
# Cache Configuration (if implementing caching)
# ===================================================================
# spring.cache.type=caffeine
# spring.cache.cache-names=filterOptions
# spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=1h
```

#### Step 2: Create Profile-Specific Properties (Optional)

For different environments, create profile-specific files:

**File:** `apps/backend/src/main/resources/application-dev.properties`

```properties
# Development-specific configuration
spring.jpa.show-sql=true
logging.level.dev.lukeholland.tcg.decklists=DEBUG
management.endpoint.health.show-details=always
```

**File:** `apps/backend/src/main/resources/application-prod.properties`

```properties
# Production-specific configuration
spring.jpa.show-sql=false
logging.level.dev.lukeholland.tcg.decklists=WARN
server.error.include-binding-errors=never
management.endpoints.web.exposure.include=health,info,prometheus
```

Activate profiles via environment variable:

```bash
# Development
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun

# Production
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

#### Step 3: Important Property Explanations

**`spring.jpa.open-in-view=false`**
**CRITICAL:** This prevents the infamous "LazyInitializationException" and improves performance. With this disabled, all
database fetching must happen within transactional boundaries (in the service layer).

**`spring.jackson.default-property-inclusion=non_null`**
Prevents null fields from appearing in JSON responses, reducing response size.

**`spring.datasource.hikari.*`**
Configures connection pooling for better database performance.

**`spring.jpa.properties.hibernate.jdbc.batch_size=20`**
Enables batch insert/update operations for better performance.

**`server.error.include-stacktrace=never`**
Never expose stack traces to API consumers (security).

**`logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE`**
In development, shows actual parameter values in SQL queries (helpful for debugging).

### Impact Assessment

**Benefits:**

- Better performance (open-in-view=false, connection pooling, batching)
- Clearer behavior (explicit configuration)
- Better debugging (proper logging levels)
- Production-ready error handling

**Risks:**

- Disabling open-in-view may expose lazy loading issues
- Need to ensure all database access happens in @Transactional methods

**Migration Notes:**

- Test thoroughly after disabling open-in-view
- May need to add @Transactional or eager fetching in some places
- Review logging levels in production

---

# Medium Priority Improvements (Sprint 2)

---

## 7. Caching Strategy Implementation

**Priority:** MEDIUM
**Effort:** Medium (3-4 hours)
**Files Affected:** build.gradle.kts, application.properties, PokemonCardService
**Status:** ⏳ Not Started

---

### Implementation Checklist

- [ ] Add spring-boot-starter-cache and caffeine dependencies
- [ ] Add @EnableCaching to Application.java
- [ ] Configure cache in application.properties
- [ ] Create CacheConfig class (optional but recommended)
- [ ] Add @Cacheable to getFilterOptions()
- [ ] Add @Cacheable to findById()
- [ ] Add @CacheEvict method for clearing caches
- [ ] Test cache behavior
- [ ] Monitor cache metrics via Actuator
- [ ] Create cache warming logic (optional)

### Implementation Notes

> **Add notes here as you implement:**
> - Actual time taken:
> - Challenges encountered:
> - Deviations from plan:
> - Lessons learned:

---

### Rationale

The `getFilterOptions()` method performs expensive database queries to fetch all possible filter values (types,
subtypes, rarities, sets, etc.). This data:

- Changes infrequently (only when new sets are added)
- Is requested on every page load
- Is identical for all users
- Is expensive to compute (multiple queries)

Caching this data can **dramatically improve performance** and **reduce database load**.

### Current State

**File:** `PokemonCardService.java`

```java
public FilterOptionsResponse getFilterOptions() {
    return new FilterOptionsResponse(
            repository.findAllTypeNames(),      // Database query
            repository.findAllSubtypeNames(),   // Database query
            repository.findAllSupertypeNames(), // Database query
            repository.findAllRarityNames(),    // Database query
            repository.findAllSetNames(),       // Database query
            repository.findAllMarkNames(),      // Database query
            repository.findAllArtistNames()     // Database query
    );
}
```

This executes **7 database queries** on every request!

### Proposed Solution

Use Spring Cache abstraction with Caffeine as the cache provider.

### Pros

✅ **Performance:** Reduce database queries from 7 to 0 (after cache warm-up)
✅ **Scalability:** Handle more concurrent users
✅ **Flexibility:** Easy to change cache provider (Redis, Hazelcast, etc.)
✅ **Declarative:** Single annotation to enable caching
✅ **Configurable:** TTL, size limits, eviction policies
✅ **Testable:** Can disable caching in tests

### Implementation Steps

#### Step 1: Add Dependencies

**File:** `apps/backend/build.gradle.kts`

```kotlin
dependencies {
    // Spring Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Caffeine cache implementation (fastest in-memory cache)
    implementation("com.github.ben-manes.caffeine:caffeine")

    // ... existing dependencies
}
```

#### Step 2: Enable Caching

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/Application.java`

```java
package dev.lukeholland.tcg.decklists.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching  // Add this annotation
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### Step 3: Configure Cache

**File:** `apps/backend/src/main/resources/application.properties`

```properties
# Cache Configuration
spring.cache.type=caffeine
spring.cache.cache-names=filterOptions,cardDetails
spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=1h
```

Explanation:

- `maximumSize=100`: Cache up to 100 entries
- `expireAfterWrite=1h`: Entries expire 1 hour after being written

#### Step 4: Add @Cacheable to Service Methods

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/pokemon/PokemonCardService.java`

```java
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
@Transactional(readOnly = true)
public class PokemonCardService {

    /**
     * Get all available filter options.
     * Results are cached for 1 hour to improve performance.
     */
    @Cacheable(value = "filterOptions", key = "'all'")
    public FilterOptionsResponse getFilterOptions() {
        return new FilterOptionsResponse(
                repository.findAllTypeNames(),
                repository.findAllSubtypeNames(),
                repository.findAllSupertypeNames(),
                repository.findAllRarityNames(),
                repository.findAllSetNames(),
                repository.findAllMarkNames(),
                repository.findAllArtistNames()
        );
    }

    /**
     * Cache individual card lookups by ID.
     */
    @Cacheable(value = "cardDetails", key = "#id")
    public Optional<Card> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Evict caches when data changes (e.g., after importing new sets).
     * Call this method after bulk data imports.
     */
    @CacheEvict(value = {"filterOptions", "cardDetails"}, allEntries = true)
    public void clearCaches() {
        // Method body can be empty - annotation does the work
    }
}
```

#### Step 5: Create Cache Configuration Class (Optional but Recommended)

For more control over caching behavior:

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/common/config/CacheConfig.java`

```java
package dev.lukeholland.tcg.decklists.api.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "filterOptions",
                "cardDetails"
        );
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats();  // Enable cache statistics
    }
}
```

#### Step 6: Add Cache Metrics to Actuator

With Caffeine's `recordStats()` enabled, cache metrics are automatically available:

```bash
curl http://localhost:8080/actuator/metrics/cache.gets
```

Response:

```json
{
  "name": "cache.gets",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1000.0
    }
  ],
  "availableTags": [
    {
      "tag": "result",
      "values": [
        "hit",
        "miss"
      ]
    },
    {
      "tag": "name",
      "values": [
        "filterOptions",
        "cardDetails"
      ]
    }
  ]
}
```

#### Step 7: Testing Cache Behavior

**Test that caching works:**

```java

@SpringBootTest
@AutoConfigureMockMvc
class CachingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PokemonCardService service;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void filterOptions_shouldBeCached() {
        // First call - should hit database
        FilterOptionsResponse response1 = service.getFilterOptions();

        // Second call - should use cache
        FilterOptionsResponse response2 = service.getFilterOptions();

        // Verify same instance returned from cache
        assertSame(response1, response2);

        // Verify cache contains the value
        Cache cache = cacheManager.getCache("filterOptions");
        assertNotNull(cache);
        assertNotNull(cache.get("all"));
    }

    @Test
    void clearCaches_shouldEvictAllCaches() {
        // Populate cache
        service.getFilterOptions();

        // Clear caches
        service.clearCaches();

        // Verify cache is empty
        Cache cache = cacheManager.getCache("filterOptions");
        assertNull(cache.get("all"));
    }
}
```

#### Step 8: Cache Key Strategies

Different caching strategies for different scenarios:

```java
// Simple caching - same for all users
@Cacheable(value = "filterOptions", key = "'all'")
public FilterOptionsResponse getFilterOptions() {
}

// Cache by ID - different cache entry per card
@Cacheable(value = "cardDetails", key = "#id")
public Optional<Card> findById(String id) {
}

// Cache by multiple parameters
@Cacheable(value = "searchResults", key = "#setId + '-' + #rarity")
public List<Card> findBySetAndRarity(String setId, String rarity) {
}

// Conditional caching - only cache if result is not empty
@Cacheable(value = "searchResults", unless = "#result.isEmpty()")
public List<Card> searchCards(String query) {
}
```

#### Step 9: Cache Eviction Strategies

**Time-based eviction:**

```properties
spring.cache.caffeine.spec=expireAfterWrite=1h
```

**Manual eviction:**

```java

@CacheEvict(value = "filterOptions", key = "'all'")
public void updateFilterOptions() {
}
```

**Evict all entries:**

```java

@CacheEvict(value = "filterOptions", allEntries = true)
public void clearAllFilters() {
}
```

**Evict multiple caches:**

```java

@CacheEvict(value = {"filterOptions", "cardDetails"}, allEntries = true)
public void clearAllCaches() {
}
```

### Advanced: When to Evict Caches

If you have data import processes, evict caches after imports:

```java

@Service
public class DataImportService {

    private final PokemonCardService cardService;

    public void importNewSet(String setData) {
        // Import new set data
        // ...

        // Clear caches so new data is visible
        cardService.clearCaches();
    }
}
```

### Considerations for Production

**1. Distributed Caching**

For multiple server instances, consider Redis:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}
```

```properties
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
```

**2. Cache Warming**

Warm up caches at application startup:

```java

@Component
public class CacheWarmer implements ApplicationListener<ContextRefreshedEvent> {

    private final PokemonCardService cardService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Warm up filter options cache
        cardService.getFilterOptions();
    }
}
```

### Impact Assessment

**Benefits:**

- **Huge performance improvement** - 7 queries → 0 queries
- **Better user experience** - Faster page loads
- **Reduced database load** - Better scalability
- **Easy to implement** - Just annotations

**Risks:**

- Stale data if cache isn't evicted when data changes
- Memory usage increases (but Caffeine has limits)
- Need to test cache eviction scenarios

**Migration Notes:**

- Start with conservative TTL (1 hour)
- Monitor cache hit rates with Actuator metrics
- Implement cache eviction when importing new card data
- Consider distributed cache (Redis) for multi-instance deployments

---

## 8. Enable Virtual Threads

**Priority:** MEDIUM
**Effort:** Small (30 minutes)
**Files Affected:** application.properties
**Status:** ⏳ Not Started

---

### Implementation Checklist

- [ ] Add spring.threads.virtual.enabled=true to application.properties
- [ ] Create debug endpoint to verify virtual threads (optional)
- [ ] Test application startup
- [ ] Run load tests to measure performance improvement
- [ ] Monitor thread usage with JVM metrics
- [ ] Document virtual thread enablement

### Implementation Notes

> **Add notes here as you implement:**
> - Actual time taken:
> - Challenges encountered:
> - Deviations from plan:
> - Lessons learned:
> - Performance improvements observed:

---

### Rationale

Java 21 introduced **Virtual Threads** (Project Loom), which are lightweight threads that improve concurrency and
scalability. Spring Boot 3.2+ has built-in support.

Virtual Threads are particularly beneficial for:

- I/O-bound operations (database queries, API calls)
- Applications with many concurrent requests
- Reducing thread pool contention

### Current State

Using traditional platform threads (the default).

### Proposed Solution

Enable virtual threads with a single configuration property.

### Pros

✅ **Better Scalability:** Handle more concurrent requests with same resources
✅ **Simpler Code:** No need for reactive programming
✅ **Better Resource Usage:** Virtual threads are extremely lightweight
✅ **Easy to Enable:** One line of configuration
✅ **Java 21 Native:** Built into the JVM
✅ **Spring Boot 3.5 Ready:** Automatic integration

### Implementation Steps

#### Step 1: Enable Virtual Threads

**File:** `apps/backend/src/main/resources/application.properties`

```properties
# Enable Java 21 Virtual Threads
spring.threads.virtual.enabled=true
```

That's it! Spring Boot will now use virtual threads for all web requests.

#### Step 2: Verify Virtual Threads Are Used

Create a test endpoint to verify:

```java

@RestController
@RequestMapping("/api/v1/debug")
public class DebugController {

    @GetMapping("/thread-info")
    public Map<String, String> getThreadInfo() {
        Thread currentThread = Thread.currentThread();
        return Map.of(
                "threadName", currentThread.getName(),
                "isVirtual", String.valueOf(currentThread.isVirtual()),
                "threadClass", currentThread.getClass().getName()
        );
    }
}
```

Response with virtual threads enabled:

```json
{
  "threadName": "virtual-1234",
  "isVirtual": "true",
  "threadClass": "java.lang.VirtualThread"
}
```

#### Step 3: Understand Virtual Thread Benefits

**Before (Platform Threads):**

- Limited by thread pool size (typically 200 threads)
- Each thread consumes ~1MB of memory
- Thread creation is expensive
- Thread parking/unparking has overhead

**After (Virtual Threads):**

- Can create millions of virtual threads
- Virtual threads use very little memory
- Cheap to create and destroy
- Automatically managed by JVM

**Example scenario:**

```java
// With platform threads: Limited by pool size
// With virtual threads: Can handle 10,000+ concurrent requests easily

@GetMapping("/cards/{id}")
public ResponseEntity<CardResponse> getCard(@PathVariable String id) {
    // This blocks on database I/O
    // With virtual threads, the platform thread is freed while waiting
    Card card = repository.findById(id).orElseThrow();
    return ResponseEntity.ok(new CardResponse(card));
}
```

#### Step 4: Customize Virtual Thread Executor (Optional)

For more control:

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/common/config/AsyncConfig.java`

```java
package dev.lukeholland.tcg.decklists.api.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
```

#### Step 5: Test Performance Impact

**Load test with platform threads:**

```bash
ab -n 10000 -c 100 http://localhost:8080/api/v1/pokemon/search?name=Pikachu
```

**Load test with virtual threads:**

```bash
# Same test after enabling virtual threads
ab -n 10000 -c 100 http://localhost:8080/api/v1/pokemon/search?name=Pikachu
```

Expected improvements:

- Higher throughput (requests/second)
- Lower latency under high load
- Better CPU utilization

### Things to Note

**Virtual threads work best with:**

- Blocking I/O (database, HTTP calls)
- High concurrency scenarios
- Spring MVC (not WebFlux - WebFlux is already non-blocking)

**Virtual threads are NOT beneficial for:**

- CPU-intensive tasks
- Applications with very few concurrent requests
- Code already using reactive programming (WebFlux)

**Potential Issues:**

- Thread-local variables still work but may have performance implications
- Synchronized blocks can pin virtual threads to platform threads
- Some libraries may not be fully compatible

### Impact Assessment

**Benefits:**

- Significantly better scalability with no code changes
- Better resource utilization
- Handle more concurrent users
- Simplified concurrency model

**Risks:**

- Relatively new feature (Java 21)
- Some edge cases with thread-locals and synchronized blocks
- Need to test thoroughly under load

**Migration Notes:**

- Enable in development first
- Run load tests to verify improvement
- Monitor thread usage with JVM metrics
- Can easily disable if issues arise

---

## 9. Clean Up Common Package Structure

**Priority:** MEDIUM
**Effort:** Minimal (15 minutes)
**Files Affected:** Common package folders
**Status:** ⏳ Not Started

---

### Implementation Checklist

- [ ] Move GlobalExceptionHandler to common/exception/ (from Improvement #1)
- [ ] Move custom exception classes to common/exception/
- [ ] Move CacheConfig to common/config/ (from Improvement #7)
- [ ] Move OpenApiConfig to common/config/ (from Improvement #11)
- [ ] Move ApiConstants to common/ (from Improvement #3)
- [ ] Remove empty folders if any remain
- [ ] Verify imports and package structure

### Implementation Notes

> **Add notes here as you implement:**
> - Actual time taken:
> - Challenges encountered:
> - Deviations from plan:
> - Lessons learned:

---

### Rationale

The codebase has empty folders in the common package:

- `common/config/` - empty
- `common/dto/` - empty
- `common/exception/` - empty (though we'll populate this with GlobalExceptionHandler)

Empty folders create confusion about their purpose and clutter the project structure.

### Current State

```
dev.lukeholland.tcg.decklists.api/
├── common/
│   ├── config/         # Empty
│   ├── dto/            # Empty
│   └── exception/      # Empty (will be populated)
```

### Proposed Solution

**Option 1:** Populate folders with actual common components
**Option 2:** Remove empty folders

Recommendation: **Option 1** - Populate with common components created in other improvements.

### Pros

✅ **Clarity:** Clear purpose for each package
✅ **Organization:** Logical home for cross-cutting concerns
✅ **Consistency:** Matches feature-based organization

### Implementation Steps

#### After Implementing Other Improvements

The common package will be populated with:

**`common/exception/`**

- `GlobalExceptionHandler.java` (from Improvement #1)
- `EntityNotFoundException.java` (from Improvement #1)
- `ValidationException.java` (from Improvement #1)

**`common/config/`**

- `OpenApiConfig.java` (from Improvement #11)
- `CacheConfig.java` (from Improvement #7)
- `WebConfig.java` (if CORS needed)

**`common/dto/`**

- Currently no common DTOs needed
- Could add: `PageResponse.java`, `ErrorResponse.java` if standardizing pagination

**`common/`** (root level)

- `ApiConstants.java` (API version constants from Improvement #3)

#### Final Structure

```
dev.lukeholland.tcg.decklists.api/
├── common/
│   ├── config/
│   │   ├── CacheConfig.java
│   │   └── OpenApiConfig.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── EntityNotFoundException.java
│   │   └── ValidationException.java
│   └── ApiConstants.java
```

### Impact Assessment

**Benefits:**

- Cleaner project structure
- Clear home for cross-cutting concerns
- Better organization

**Risks:**

- None - just organizational

---

## 10. Standardize Validation Layer

**Priority:** MEDIUM
**Effort:** Small (1-2 hours)
**Files Affected:** Service classes
**Status:** ⏳ Not Started

---

### Implementation Checklist

- [ ] Review all service methods for input validation
- [ ] Remove input validation from DecklistService
- [ ] Remove input validation from PokemonCardService
- [ ] Keep only business validation in services
- [ ] Ensure DTOs have complete validation coverage
- [ ] Update service layer documentation
- [ ] Test that validation still works correctly

### Implementation Notes

> **Add notes here as you implement:**
> - Actual time taken:
> - Challenges encountered:
> - Deviations from plan:
> - Lessons learned:

---

### Rationale

After implementing Bean Validation (Improvement #2), validation logic should be removed from the service layer. Services
should focus on business logic only.

### Current State

**DecklistService has validation:**

```java
public Decklist createDecklist(DecklistRequest request) {
    if (request.name() == null || request.name().trim().isEmpty()) {
        throw new IllegalArgumentException("Decklist name is required");
    }
    if (request.cards() == null || request.cards().isEmpty()) {
        throw new IllegalArgumentException("Decklist must contain at least one card");
    }
    // ... business logic
}
```

### Proposed Solution

Move all input validation to DTOs with Bean Validation. Services only handle business validation.

### Pros

✅ **Separation of Concerns:** Services focus on business logic
✅ **Testability:** Easier to test business logic separately
✅ **Consistency:** All validation in one place

### Implementation Steps

#### Step 1: Identify Validation Types

**Input Validation:** (belongs in DTOs)

- Required fields
- Format validation
- Range validation
- Pattern validation

**Business Validation:** (belongs in services)

- "Card ID doesn't exist in database"
- "User already has a decklist with this name"
- "Cannot exceed 60 cards in deck"

#### Step 2: Move Input Validation to DTOs

Already covered in Improvement #2.

#### Step 3: Keep Business Validation in Services

```java

@Service
public class DecklistService {

    public Decklist createDecklist(DecklistRequest request) {
        // Input validation already done by @Valid in controller

        // Business validation still in service
        if (request.cards().size() > 60) {
            throw new ValidationException("Decklist cannot exceed 60 cards");
        }

        // Check that all card IDs exist
        List<String> invalidCardIds = request.cards().stream()
                .filter(cardId -> !cardRepository.existsById(cardId))
                .toList();

        if (!invalidCardIds.isEmpty()) {
            throw new ValidationException(
                    "Invalid card IDs: " + String.join(", ", invalidCardIds)
            );
        }

        // Business logic
        Decklist decklist = new Decklist();
        // ...
        return repository.save(decklist);
    }
}
```

### Impact Assessment

**Benefits:**

- Clearer separation of validation types
- Easier to test and maintain
- Better error messages

---

# Additional Improvements

---

## 11. Enhanced OpenAPI Configuration

**Priority:** LOW
**Effort:** Small (1 hour)
**Files Affected:** New OpenApiConfig class
**Status:** ⏳ Not Started

---

### Implementation Checklist

- [ ] Create OpenApiConfig class in common/config/
- [ ] Add API info (title, version, description)
- [ ] Add contact information
- [ ] Add license information
- [ ] Add server URLs (local, production)
- [ ] Test Swagger UI with new configuration
- [ ] Verify documentation looks professional

### Implementation Notes

> **Add notes here as you implement:**
> - Actual time taken:
> - Challenges encountered:
> - Deviations from plan:
> - Lessons learned:

---

### Rationale

Currently using default OpenAPI/Swagger configuration. Custom configuration provides:

- Better API documentation
- Contact information
- License details
- Server URLs
- Security schemes (for future)

### Implementation

**File:** `apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/common/config/OpenApiConfig.java`

```java
package dev.lukeholland.tcg.decklists.api.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers());
    }

    private Info apiInfo() {
        return new Info()
                .title("TCG Decklists API")
                .version("1.0")
                .description("""
                        REST API for Pokemon TCG deck builder and viewer.
                        
                        Features:
                        - Comprehensive card search with multiple filters
                        - Decklist creation and management
                        - Autocomplete for card attributes
                        - Filter options for building search UIs
                        
                        This API is designed to support multiple trading card games,
                        with Pokemon TCG as the first implementation.
                        """)
                .contact(new Contact()
                        .name("Luke Holland")
                        .url("https://github.com/lukeholland/tcg-decklists")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }

    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local development server"),
                new Server()
                        .url("https://api.tcg-decklists.example.com")
                        .description("Production server")
        );
    }
}
```

### Pros

✅ **Better Documentation:** Clear API description
✅ **Professional:** Contact and license information
✅ **Multi-Environment:** Different server URLs
✅ **Discoverable:** Easier for API consumers to understand

---

## 12. Query Optimization Recommendations

**Priority:** LOW
**Effort:** Variable
**Files Affected:** Repository classes
**Status:** ⏳ Not Started

---

### Implementation Checklist

- [ ] Analyze N+1 query problems with @EntityGraph
- [ ] Consider database views for complex aggregations
- [ ] Review and add database indexes for common queries
- [ ] Profile query performance with SQL logging
- [ ] Test query optimizations
- [ ] Document optimization decisions

### Implementation Notes

> **Add notes here as you implement:**
> - Actual time taken:
> - Challenges encountered:
> - Deviations from plan:
> - Lessons learned:
> - Performance improvements measured:

---

### Rationale

Some queries can be optimized for better performance.

### Recommendations

#### 1. Use @EntityGraph for Eager Fetching

Prevent N+1 query problems:

```java
public interface PokemonCardRepository extends JpaRepository<Card, String> {

    @EntityGraph(attributePaths = {"types", "subtypes", "attacks"})
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdWithDetails(@Param("id") String id);
}
```

#### 2. Consider Database Views for Complex Aggregations

For filter options, create a database view:

```sql
CREATE VIEW filter_options AS
SELECT (SELECT json_agg(DISTINCT name) FROM type)    as types,
       (SELECT json_agg(DISTINCT name) FROM subtype) as subtypes,
       (SELECT json_agg(DISTINCT name) FROM rarity)  as rarities,
       (SELECT json_agg(DISTINCT name) FROM set)     as sets;
```

#### 3. Add Database Indexes

Ensure common filter fields are indexed:

```sql
CREATE INDEX idx_card_name ON card (name);
CREATE INDEX idx_card_set_id ON card (set_id);
CREATE INDEX idx_card_hp ON card (hp);
```

---

## 13. Modern Java 21 Patterns

**Priority:** LOW
**Effort:** Variable
**Files Affected:** Various
**Status:** ⏳ Not Started

---

### Implementation Checklist

- [ ] Identify candidates for sealed classes
- [ ] Implement sealed interfaces for response hierarchies
- [ ] Use pattern matching for switch where applicable
- [ ] Use sequenced collections methods (getFirst/getLast)
- [ ] Document modern Java 21 pattern usage
- [ ] Update code style guide

### Implementation Notes

> **Add notes here as you implement:**
> - Actual time taken:
> - Challenges encountered:
> - Deviations from plan:
> - Lessons learned:

---

### Sealed Classes for Response Hierarchies

```java
public sealed interface SearchResult permits SuccessResult, ErrorResult {
}

public record SuccessResult(List<Card> cards, int total) implements SearchResult {
}

public record ErrorResult(String message) implements SearchResult {
}
```

### Pattern Matching for Switch

```java
public String formatCard(Object card) {
    return switch (card) {
        case PokemonCard p -> "Pokemon: " + p.name();
        case YuGiOhCard y -> "Yu-Gi-Oh: " + y.name();
        case null -> "Unknown";
        default -> throw new IllegalArgumentException();
    };
}
```

### Sequenced Collections

```java
List<Card> cards = repository.findAll();
Card first = cards.getFirst();  // Instead of cards.get(0)
Card last = cards.getLast();    // Instead of cards.get(cards.size() - 1)
```

---

## 14. Structured Logging Improvements

**Priority:** LOW
**Effort:** Small (1-2 hours)
**Files Affected:** Various services
**Status:** ⏳ Not Started

---

### Implementation Checklist

- [ ] Review all log statements in service classes
- [ ] Convert String concatenation to parameterized logging
- [ ] Add contextual logging to key service methods
- [ ] Create RequestLoggingFilter for HTTP logging
- [ ] Add request/response duration logging
- [ ] Configure appropriate log levels
- [ ] Test logging output

### Implementation Notes

> **Add notes here as you implement:**
> - Actual time taken:
> - Challenges encountered:
> - Deviations from plan:
> - Lessons learned:

---

### Use SLF4J Parameterized Logging

**Before:**

```java
log.info("Searching cards with name: "+name);
```

**After:**

```java
log.info("Searching cards with name: {}",name);
```

### Add Contextual Logging

```java

@Service
@Slf4j  // Lombok annotation
public class PokemonCardService {

    public CardSearchResponse searchCards(CardSearchRequest request) {
        log.debug("Search request received: page={}, size={}, filters={}",
                request.page(), request.pageSize(), request.name());

        Specification<Card> spec = buildSpecification(request);
        Page<Card> results = repository.findAll(spec, pageable);

        log.info("Search completed: found {} results in {}ms",
                results.getTotalElements(), duration);

        return new CardSearchResponse(results);
    }
}
```

### Add Request Logging Filter

```java

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("{} {} - {} - {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);
        }
    }
}
```

---

# Implementation Roadmap

## Sprint 1 (Week 1)

1. ✅ Centralized Exception Handling with RFC 7807
2. ✅ Jakarta Bean Validation Implementation
3. ✅ API Versioning Strategy
4. ✅ Convert CardSearchRequest to Record
5. ✅ Spring Boot Actuator Integration
6. ✅ Application Properties Configuration

**Outcome:** Production-ready error handling, validation, and monitoring

## Sprint 2 (Week 2)

7. ✅ Caching Strategy Implementation
8. ✅ Enable Virtual Threads
9. ✅ Clean Up Common Package Structure
10. ✅ Standardize Validation Layer

**Outcome:** Optimized performance and cleaner architecture

## Future Enhancements

11. Enhanced OpenAPI Configuration
12. Query Optimization
13. Modern Java 21 Patterns
14. Structured Logging

---

# Testing Strategy

After each improvement:

1. **Unit Tests:** Test validation, caching, exception handling
2. **Integration Tests:** Update existing tests for new endpoints/behavior
3. **Manual Testing:** Verify via Swagger UI and curl
4. **Load Testing:** Verify performance improvements (especially caching and virtual threads)

---

# Migration Checklist

- [ ] Review each improvement section
- [ ] Implement improvements in order (High → Medium → Low)
- [ ] Update tests after each change
- [ ] Update frontend to use `/v1/` endpoints
- [ ] Update README with new features
- [ ] Document API changes (especially RFC 7807 error format)
- [ ] Load test after performance improvements
- [ ] Deploy to staging environment
- [ ] Monitor metrics via Actuator
- [ ] Deploy to production

---

# Additional Resources

## Documentation

- [RFC 7807 Problem Details](https://datatracker.ietf.org/doc/html/rfc7807)
- [Jakarta Bean Validation](https://beanvalidation.org/3.0/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Caffeine Cache](https://github.com/ben-manes/caffeine)
- [Java 21 Virtual Threads](https://openjdk.org/jeps/444)
- [Spring Boot 3.5 Reference](https://docs.spring.io/spring-boot/docs/3.5.x/reference/html/)

## Tools

- [JMeter](https://jmeter.apache.org/) - Load testing
- [Prometheus](https://prometheus.io/) - Metrics collection
- [Grafana](https://grafana.com/) - Metrics visualization

---

# Conclusion

This refactoring plan provides a comprehensive path to transform your backend from a solid foundation into a
production-ready, enterprise-grade API. The improvements focus on:

1. **Standards Compliance:** RFC 7807, Jakarta EE, REST best practices
2. **Performance:** Caching, virtual threads, query optimization
3. **Maintainability:** Clean validation, centralized error handling, modern Java patterns
4. **Observability:** Actuator, metrics, structured logging
5. **Future-Proofing:** API versioning, extensible architecture

Each improvement is self-contained and can be implemented independently, allowing you to prioritize based on your
immediate needs.

**Estimated Total Effort:** 3-4 days for all high and medium priority improvements.

**Questions or need clarification on any section?** Each improvement includes detailed implementation steps and code
examples to guide you through the process.

Happy coding! 🚀
