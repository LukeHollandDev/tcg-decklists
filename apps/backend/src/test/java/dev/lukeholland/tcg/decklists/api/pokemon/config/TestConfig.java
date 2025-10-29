package dev.lukeholland.tcg.decklists.api.pokemon.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test-specific Spring configuration.
 * <p>
 * This configuration is loaded during test execution and can override
 * or supplement the main application configuration with test-specific beans.
 * <p>
 * Use this to:
 * - Define test-specific beans (e.g., mocks, test utilities)
 * - Override production beans with test doubles
 * - Configure test-specific behavior
 */
@TestConfiguration
public class TestConfig {

    /**
     * Provides TestDataLoader bean for loading SQL fixtures into test database.
     *
     * @return TestDataLoader instance
     */
    @Bean
    public TestDataLoader testDataLoader() {
        return new TestDataLoader();
    }

    // Add additional test-specific beans here as needed
    // Example:
    // @Bean
    // public SomeTestUtility someTestUtility() {
    //     return new SomeTestUtility();
    // }
}
