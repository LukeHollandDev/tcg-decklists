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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
@Testcontainers
@Transactional
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL TestContainer instance.
     * Shared across all tests in the same JVM for performance.
     * Uses PostgreSQL 16 Alpine for faster startup.
     */
    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("tcg_decklists_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Reuse container across test runs for faster execution

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
    }

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
