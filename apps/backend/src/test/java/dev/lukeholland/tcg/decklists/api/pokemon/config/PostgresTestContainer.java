package dev.lukeholland.tcg.decklists.api.pokemon.config;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton PostgreSQL TestContainer to ensure the same container instance
 * is shared across all test classes.
 * <p>
 * This prevents connection issues and improves test performance by
 * ensuring the container starts only once and stays alive for all tests.
 */
public class PostgresTestContainer {

    private static PostgreSQLContainer<?> container;

    private PostgresTestContainer() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get the shared PostgreSQL container instance.
     * Creates the container on first access and reuses it for all subsequent calls.
     *
     * @return the shared PostgreSQL container
     */
    public static PostgreSQLContainer<?> getInstance() {
        if (container == null) {
            container = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("tcg_decklists_test")
                    .withUsername("test")
                    .withPassword("test");
            container.start();
        }
        return container;
    }
}
