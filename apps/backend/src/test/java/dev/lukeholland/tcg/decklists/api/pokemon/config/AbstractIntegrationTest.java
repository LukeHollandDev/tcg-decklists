package dev.lukeholland.tcg.decklists.api.pokemon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Abstract base class for all integration tests.
 * <p>
 * Provides:
 * - TestContainers PostgreSQL setup for isolated database testing
 * - MockMvc for HTTP request testing
 * - ObjectMapper for JSON serialization/deserialization
 * - Transactional rollback for test isolation
 * <p>
 * All integration tests should extend this class to benefit from the shared infrastructure.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL TestContainer instance.
     * Shared across all tests in the same JVM using singleton pattern.
     * Uses PostgreSQL 16 Alpine for faster startup.
     */
    protected static final PostgreSQLContainer<?> postgres = PostgresTestContainer.getInstance();
    /**
     * MockMvc for making HTTP requests in tests.
     */
    @Autowired
    protected MockMvc mockMvc;
    /**
     * ObjectMapper for JSON serialization/deserialization in tests.
     */
    @Autowired
    protected ObjectMapper objectMapper;
    /**
     * TestDataLoader for loading SQL fixtures into the test database.
     */
    @Autowired
    protected TestDataLoader testDataLoader;

    /**
     * Configure Spring properties to use the TestContainers database.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Liquibase configuration for test database
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");

        // Problem Details (RFC 7807) Configuration
        registry.add("spring.mvc.problemdetails.enabled", () -> true);
        registry.add("server.error.include-message", () -> "always");
        registry.add("server.error.include-binding-errors", () -> "always");

        // HikariCP connection pool configuration for tests
        // Shorter lifetimes prevent stale connections when container is reused
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> 5);
        registry.add("spring.datasource.hikari.minimum-idle", () -> 2);
        registry.add("spring.datasource.hikari.max-lifetime", () -> 300000); // 5 minutes
        registry.add("spring.datasource.hikari.connection-timeout", () -> 10000); // 10 seconds
        registry.add("spring.datasource.hikari.idle-timeout", () -> 60000); // 1 minute
        registry.add("spring.datasource.hikari.validation-timeout", () -> 5000); // 5 seconds
    }

    /**
     * Setup method run before each test.
     * Subclasses can override this to add custom setup logic.
     */
    @BeforeEach
    void setUp() {
        // Optional: Load test data here if needed
        // testDataLoader.loadTestData();
        // Currently commented out - tests should load their own data as needed
    }
}
