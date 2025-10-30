package dev.lukeholland.tcg.decklists.api.pokemon.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for loading SQL test data fixtures into the test database.
 * <p>
 * This class provides methods to load predefined SQL scripts containing
 * test card data, which can be used to set up known test scenarios.
 * <p>
 * Test data scripts are located in: src/test/resources/sql/
 */
@Component
public class TestDataLoader {

    private static final Logger log = LoggerFactory.getLogger(TestDataLoader.class);

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * Load the main test data SQL file (test-data.sql).
     * <p>
     * This method loads comprehensive test card data including:
     * - Cards with various types, subtypes, and rarities
     * - Cards with accent characters (e.g., Flabébé, Pokémon)
     * - Evolution chains
     * - Cards with attacks, abilities, weaknesses, resistances
     * - Cards across multiple sets and regulation marks
     * <p>
     * Note: This method will fail silently if the SQL file doesn't exist yet.
     * This is intentional to allow tests to run before fixtures are created.
     * <p>
     * This method is idempotent - it will silently skip loading if data already exists
     * (handles duplicate key exceptions gracefully when running multiple test classes).
     *
     * @throws RuntimeException if there's an error loading the SQL file (except duplicate key errors)
     */
    public void loadTestData() {
        if (dataSource == null) {
            log.warn("DataSource not available. Skipping test data loading.");
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource("sql/test-data.sql");

            if (!resource.exists()) {
                log.warn("Test data SQL file not found: sql/test-data.sql. Skipping test data loading.");
                return;
            }

            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, resource);
                log.info("Successfully loaded test data from sql/test-data.sql");
            }

        } catch (Exception e) {
            // Check if this is a duplicate key error (data already loaded)
            // Check both the exception and its cause chain for duplicate key errors
            Throwable cause = e;
            while (cause != null) {
                if (cause.getMessage() != null && cause.getMessage().contains("duplicate key value violates unique constraint")) {
                    log.info("Test data already loaded, skipping...");
                    return;
                }
                cause = cause.getCause();
            }

            log.error("Failed to load test data", e);
            throw new RuntimeException("Failed to load test data from SQL file", e);
        }
    }

    /**
     * Load a custom SQL file by name.
     * <p>
     * Useful for loading specific test data for particular test scenarios.
     *
     * @param sqlFileName Name of the SQL file (relative to src/test/resources/sql/)
     * @throws RuntimeException if there's an error loading the SQL file
     */
    public void loadCustomSqlFile(String sqlFileName) {
        if (dataSource == null) {
            log.warn("DataSource not available. Skipping SQL file loading: {}", sqlFileName);
            return;
        }

        try {
            String resourcePath = "sql/" + sqlFileName;
            ClassPathResource resource = new ClassPathResource(resourcePath);

            if (!resource.exists()) {
                log.warn("SQL file not found: {}. Skipping loading.", resourcePath);
                return;
            }

            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, resource);
                log.info("Successfully loaded SQL file: {}", resourcePath);
            }

        } catch (SQLException e) {
            log.error("Failed to load SQL file: {}", sqlFileName, e);
            throw new RuntimeException("Failed to load SQL file: " + sqlFileName, e);
        }
    }

    /**
     * Clear all test data from the database.
     * <p>
     * Warning: This will delete ALL data from pokemon card tables.
     * Only use in tests with proper transaction rollback.
     */
    public void clearTestData() {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate not available. Skipping test data clearing.");
            return;
        }

        log.info("Clearing all test data from database");

        // Delete in reverse dependency order to avoid foreign key constraint violations
        jdbcTemplate.execute("DELETE FROM pokemon_card_retreat_cost");
        jdbcTemplate.execute("DELETE FROM pokemon_card_weakness");
        jdbcTemplate.execute("DELETE FROM pokemon_card_rule");
        jdbcTemplate.execute("DELETE FROM pokemon_card_resistance");
        jdbcTemplate.execute("DELETE FROM pokemon_card_evolution");
        jdbcTemplate.execute("DELETE FROM pokemon_attack_cost");
        jdbcTemplate.execute("DELETE FROM pokemon_card_attack");
        jdbcTemplate.execute("DELETE FROM pokemon_card_legality");
        jdbcTemplate.execute("DELETE FROM pokemon_card_ability");
        jdbcTemplate.execute("DELETE FROM pokemon_card_pokedex");
        jdbcTemplate.execute("DELETE FROM pokemon_card_type");
        jdbcTemplate.execute("DELETE FROM pokemon_card_subtype");
        jdbcTemplate.execute("DELETE FROM pokemon_card");

        // Note: We don't clear reference tables (types, subtypes, sets, etc.)
        // as they may be needed across multiple tests

        log.info("Test data cleared successfully");
    }
}
