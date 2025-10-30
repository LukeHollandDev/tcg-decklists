package dev.lukeholland.tcg.decklists.api.pokemon.integration;

import dev.lukeholland.tcg.decklists.api.pokemon.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for error handling and edge cases in Pokemon card endpoints.
 * <p>
 * Tests cover:
 * - Invalid card IDs (404 errors)
 * - Invalid query parameters
 * - Empty search results
 * - Boundary conditions
 * - Malformed requests
 */
@DisplayName("Pokemon Card - Error Handling")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardErrorHandlingIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // GET /api/pokemon/{id} Error Cases
    // ============================================================================

    @Test
    @DisplayName("Should return 404 for non-existent card ID")
    void shouldReturn404ForNonExistentCardId() throws Exception {
        mockMvc.perform(get("/api/pokemon/nonexistent-card-123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 for malformed card ID")
    void shouldReturn404ForMalformedCardId() throws Exception {
        mockMvc.perform(get("/api/pokemon/!!!invalid!!!"))
                .andExpect(status().isNotFound());
    }

    // ============================================================================
    // GET /api/pokemon/search Error Cases
    // ============================================================================

    @Test
    @DisplayName("Should handle empty search results gracefully")
    void shouldHandleEmptySearchResultsGracefully() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("name", "ThisCardDefinitelyDoesNotExist12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.totalResults").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    @DisplayName("Should handle search with no parameters (return all cards paginated)")
    void shouldHandleSearchWithNoParameters() throws Exception {
        mockMvc.perform(get("/api/pokemon/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalResults").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    @DisplayName("Should handle invalid page number gracefully (negative)")
    void shouldHandleNegativePageNumber() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // System should handle negative page gracefully (may default to 0)
    }

    @Test
    @DisplayName("Should handle invalid pageSize gracefully (negative)")
    void shouldHandleNegativePageSize() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("pageSize", "-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // System should handle negative pageSize gracefully (may use default)
    }

    @Test
    @DisplayName("Should handle page beyond available results")
    void shouldHandlePageBeyondAvailableResults() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.currentPage").value(9999))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("Should handle invalid HP range (min > max)")
    void shouldHandleInvalidHpRange() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hpMin", "200")
                        .param("hpMax", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
        // No cards can have HP >= 200 AND HP <= 50
    }

    @Test
    @DisplayName("Should handle invalid retreat cost range (min > max)")
    void shouldHandleInvalidRetreatCostRange() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("retreatCostMin", "5")
                        .param("retreatCostMax", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
        // No cards can have retreat cost >= 5 AND <= 1
    }

    @Test
    @DisplayName("Should handle search with all filters resulting in no matches")
    void shouldHandleSearchWithNoMatches() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Dragon")
                        .param("subtypes", "Basic")
                        .param("hpMin", "500")
                        .param("attackDamageMin", "1000")
                        .param("rarity", "NonExistentRarity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.totalResults").value(0));
    }
}
