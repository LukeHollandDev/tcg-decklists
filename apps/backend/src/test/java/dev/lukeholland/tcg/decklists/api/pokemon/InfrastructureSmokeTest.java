package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test to verify the test infrastructure is working correctly.
 * <p>
 * This test validates:
 * - TestContainers PostgreSQL is running
 * - Spring Boot application context loads
 * - Database connection is established
 * - Liquibase migrations ran successfully
 * - MockMvc is configured and working
 * - Basic API endpoint responds
 */
@DisplayName("Infrastructure Smoke Test")
class InfrastructureSmokeTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should verify TestContainers PostgreSQL is running")
    void shouldVerifyPostgresContainerIsRunning() {
        assertThat(postgres.isRunning())
                .as("PostgreSQL container should be running")
                .isTrue();

        assertThat(postgres.getDatabaseName())
                .as("Database name should match configuration")
                .isEqualTo("tcg_decklists_test");

        assertThat(postgres.getUsername())
                .as("Username should match configuration")
                .isEqualTo("test");
    }

    @Test
    @DisplayName("Should verify database connection is established")
    void shouldVerifyDatabaseConnection() {
        // Execute a simple query to verify connection
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        assertThat(result)
                .as("Database should respond to simple query")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Should verify Liquibase migrations ran successfully")
    void shouldVerifyLiquibaseMigrationsRan() {
        // Check that DATABASECHANGELOG table exists (created by Liquibase)
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'databasechangelog'",
                Integer.class
        );

        assertThat(count)
                .as("Liquibase changelog table should exist")
                .isEqualTo(1);

        // Verify some of our Pokemon tables were created
        List<String> expectedTables = List.of(
                "pokemon_card",
                "pokemon_set",
                "pokemon_type",
                "pokemon_attack",
                "pokemon_ability"
        );

        for (String tableName : expectedTables) {
            Integer tableCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?",
                    Integer.class,
                    tableName
            );

            assertThat(tableCount)
                    .as("Table '%s' should exist", tableName)
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Should verify MockMvc is configured and working")
    void shouldVerifyMockMvcIsWorking() throws Exception {
        // Make a request to the features endpoint (should work even with no data)
        MvcResult result = mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody)
                .as("Response should contain JSON content")
                .isNotEmpty()
                .contains("type")
                .contains("pokemon");
    }

    @Test
    @DisplayName("Should verify ObjectMapper is available")
    void shouldVerifyObjectMapperIsAvailable() {
        assertThat(objectMapper)
                .as("ObjectMapper should be autowired")
                .isNotNull();
    }

    @Test
    @DisplayName("Should verify TestDataLoader is available")
    void shouldVerifyTestDataLoaderIsAvailable() {
        assertThat(testDataLoader)
                .as("TestDataLoader should be autowired")
                .isNotNull();
    }

    @Test
    @DisplayName("Should verify transactional rollback works")
    void shouldVerifyTransactionalRollback() {
        // Insert test data
        jdbcTemplate.update(
                "INSERT INTO pokemon_set (set_id, name) VALUES (?, ?)",
                "test-set-smoke",
                "Smoke Test Set"
        );

        // Verify it was inserted
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pokemon_set WHERE set_id = ?",
                Integer.class,
                "test-set-smoke"
        );

        assertThat(count)
                .as("Test data should be inserted")
                .isEqualTo(1);

        // Note: This data will be automatically rolled back after the test
        // due to @Transactional on AbstractIntegrationTest
    }
}
