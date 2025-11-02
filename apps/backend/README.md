# TCG Decklists Backend

This project was initialised using [Spring Initializr](https://start.spring.io/).

The following options were selected:

| Option              | Selection                                            |
|---------------------|------------------------------------------------------|
| Project             | Gradle - Kotlin                                      |
| Language            | Java                                                 |
| Spring Boot Version | 3.5.6                                                |
| Packaging           | Jar                                                  |
| Java Version        | 21                                                   |
| Dependencies        | Spring Web, Spring Data, JPA and Liquibase Migration |

## Commands

| Command                                         | Description                                           |
|-------------------------------------------------|-------------------------------------------------------|
| `./gradlew bootRun`                             | Start the Spring Boot application                     |
| `./gradlew build`                               | Compile, test, and package the application into a JAR |
| `./gradlew clean`                               | Remove build artifacts and output directories         |
| `./gradlew test`                                | Run all unit and integration tests                    |
| `./gradlew test --tests "ClassName"`            | Run a specific test class                             |
| `./gradlew test --tests "ClassName.methodName"` | Run a specific test method                            |
| `./gradlew tasks`                               | List all available Gradle tasks                       |
| `./gradlew dependencies`                        | Display project dependency tree                       |
| `./gradlew bootJar`                             | Build executable JAR file for production deployment   |

## Dependencies

The backend needs the following to be available:

- Database needs to be running, see [README](../../README.md)
- Optionally, data can be loaded into the database see [Data Pipeline README](../../tools/data-pipeline/README.md)

## Implementation

The backend is a Spring Boot application following a modular, domain-driven architecture. Each domain (trading card
game) defines its own models, specifications, and routes while sharing a common foundation for persistence,
configuration, and utilities.

### Architecture

The system is organised into layers—controllers expose REST endpoints, services handle domain logic, and repositories
manage persistence. This clear separation ensures extensibility, testability, and isolation of domain-specific logic
from shared infrastructure.

### Dynamic Querying

Search functionality is powered by Spring Data JPA Specifications, allowing type-safe, composable query definitions.
Each filter is represented as a small, reusable specification that can be combined dynamically to form complex queries
at runtime. This enables consistent and flexible search capabilities across all supported domains without modifying
repository code.

### Data Access & DTO Mapping

Entities are designed for persistence and use lazy relationships by default to reduce query overhead. Data Transfer
Objects (DTOs) control API responses and prevent direct entity serialization, ensuring a stable public contract even as
internal models evolve. Mapping between entities and DTOs follows a consistent pattern for all domains.

### Extensibility

New domains can be added by registering a module with its own entities, specifications, and API endpoints. Shared
components handle pagination, error responses, and validation, minimising duplication. This modularity allows the system
to scale to multiple datasets with minimal configuration effort.

## Testing

The application uses PostgreSQL as its primary data store, with schema management handled through Liquibase.

### Schema Evolution

Liquibase changelogs are maintained per domain, enabling isolated schema changes without impacting other modules. Shared
changelogs define core tables and constraints, while domain-specific ones extend these with custom attributes and
relationships. Migrations are applied automatically on startup.

### Data Modeling

Schemas follow a normalised design to balance flexibility and query performance. Many-to-many relationships are
represented through join tables, and numeric or comparable attributes are stored in optimised formats to support
efficient filtering and sorting.

## Testing

Testing focuses primarily on integration validation—ensuring that API behaviour, data contracts, and system
interactions remain consistent across changes. The goal is to guarantee that updates to backend logic, schemas, or
filters do not break integrations or frontend functionality.

### Integration Tests

Most tests are integration tests that exercise the full stack—from REST controllers through service logic and
persistence—using realistic datasets and environments. These tests verify that endpoints respond as expected, filters
and specifications behave correctly, and serialization formats remain stable.

### Environment

Integration tests run against a real PostgreSQL instance provisioned through TestContainers. Each test suite applies
Liquibase migrations automatically, ensuring schema compatibility before execution. This setup closely mirrors
production behaviour and reduces false positives compared to mocked tests.

## API

The API is following a by-feature pattern for the base URL is `/api/<feature>`.

### Available Endpoints

Each feature has the following endpoints available to it. They're largely consistent but responses and available
parameters may vary.

#### `GET /:id`

This endpoint allows for getting card details for a specific card given its id.

It returns a JSON response representing the card; a 404 error is returned if the card is not found.

#### `GET /search`

This endpoint allows for searching for cards, it takes URL parameters as the input for searching. These parameters will
differ per card game.

All features should share the same pagination parameters:

| Parameter   | Type    | Default | Description                                          |
|-------------|---------|---------|------------------------------------------------------|
| `page`      | integer | `0`     | Page number (0-indexed)                              |
| `pageSize`  | integer | `20`    | Number of results per page (maximum: 100)            |
| `sortBy`    | string  | `name`  | Field to sort results by                             |
| `sortOrder` | string  | `asc`   | Sort order: `asc` (ascending) or `desc` (descending) |

It returns a JSON response containing a list of the cards as well as pagination details, so additional results can be
obtained using the page parameter.

#### `GET /autocomplete/:field`

This endpoint enables an autocomplete feature which can be used for a more interactive search. The fields supported
depend on the specific card game.

These endpoints take a `query` string and a `limit` parameter which defaults to 10 with a max of 50.

It returns a JSON response containing a list of strings for the results, the count (number of results returned) the
query provided, and the limit.

#### `GET /features`

This endpoint provides metadata about available search filters and static data for the card game. These will vary for
each card game and will be used to build the frontend filters.

For the filters, the response might contain the following:

| Field               | Type    | Description                                                            |
|---------------------|---------|------------------------------------------------------------------------|
| `type`              | string  | Data type of the filter (e.g., "string", "number", "boolean", "array") |
| `operator`          | string  | Comparison operator (e.g., "equals", "contains", "between", "in")      |
| `description`       | string  | Human-readable description of the filter                               |
| `parameterName`     | string  | URL parameter name to use in search requests                           |
| `accentInsensitive` | boolean | (Optional) Whether the filter ignores accents in search                |
| `valuesRef`         | string  | (Optional) Reference to static data array (e.g., "types", "rarities")  |
| `matchAllParameter` | string  | (Optional) Parameter name for AND/OR logic (e.g., "typesMatchAll")     |
| `minParameter`      | string  | (Optional) Parameter name for minimum value in range filters           |
| `maxParameter`      | string  | (Optional) Parameter name for maximum value in range filters           |

The response is a JSON object which provides filters and static data as objects.

#### `POST /decklist`

This endpoint allows the creation of a decklist. The request body should contain a JSON object with the decklist
details:

| Field   | Type         | Required | Description                                                     |
|---------|--------------|----------|-----------------------------------------------------------------|
| `name`  | string       | Yes      | Name of the decklist                                            |
| `type`  | string       | Yes      | Card game type. Supported values: "pokemon" (case-insensitive)  |
| `cards` | string array | Yes      | List of card IDs (duplicates allowed for quantity)              |

It returns a JSON response with the created decklist ID. A 400 error is returned if validation fails (invalid card IDs,
missing fields, etc.).

#### `GET /decklist/:id`

This endpoint retrieves a decklist by its ID. It returns a JSON response containing the decklist details with card IDs
expanded by quantity. A 404 error is returned if the decklist is not found.
