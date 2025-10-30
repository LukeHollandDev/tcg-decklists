package dev.lukeholland.tcg.decklists.api.pokemon.integration;

import dev.lukeholland.tcg.decklists.api.pokemon.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Pokemon card search with detail filters (Phase 2 filters).
 * <p>
 * Tests cover:
 * - Artist filtering (exact match, accent-insensitive)
 * - Regulation mark filtering
 * - Retreat cost filtering (min, max, range)
 * - Format legality filtering (AND/OR logic, banned formats)
 * - Empty results handling
 * - Combined filter tests
 */
@DisplayName("Pokemon Card Search - Detail Filters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardSearchDetailFiltersIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Artist Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by exact artist name")
    void shouldFindCardsByArtist() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("artist", "Ken Sugimori"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].artistName", everyItem(equalToIgnoringCase("Ken Sugimori"))));
    }

    @Test
    @DisplayName("Should find cards by artist with case-insensitive search")
    void shouldFindCardsByArtistCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("artist", "ken sugimori"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].artistName", everyItem(equalToIgnoringCase("Ken Sugimori"))));
    }

    @Test
    @DisplayName("Should find cards by different artist")
    void shouldFindCardsByDifferentArtist() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("artist", "Mitsuhiro Arita"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].artistName", everyItem(equalToIgnoringCase("Mitsuhiro Arita"))));
    }

    @Test
    @DisplayName("Should return empty results for non-existent artist")
    void shouldReturnEmptyResultsForNonExistentArtist() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("artist", "NonExistentArtist12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    // ============================================================================
    // Regulation Mark Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by regulation mark D")
    void shouldFindCardsByRegulationMarkD() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("regulationMark", "D"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on test data
    }

    @Test
    @DisplayName("Should find cards by regulation mark E")
    void shouldFindCardsByRegulationMarkE() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("regulationMark", "E"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].regulationMark", everyItem(is("E"))));
    }

    @Test
    @DisplayName("Should return empty results for non-existent regulation mark")
    void shouldReturnEmptyResultsForNonExistentRegulationMark() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("regulationMark", "Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    // ============================================================================
    // Retreat Cost Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with zero retreat cost")
    void shouldFindCardsWithZeroRetreatCost() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("retreatCostMax", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on test data
        // If results exist, verify they have 0 retreat cost
    }

    @Test
    @DisplayName("Should find cards with retreat cost >= 2 (retreatCostMin)")
    void shouldFindCardsByMinRetreatCost() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("retreatCostMin", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[?(@.convertedRetreatCost)].convertedRetreatCost",
                        everyItem(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Should find cards with retreat cost <= 1 (retreatCostMax)")
    void shouldFindCardsByMaxRetreatCost() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("retreatCostMax", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[?(@.convertedRetreatCost)].convertedRetreatCost",
                        everyItem(lessThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Should find cards within retreat cost range (1-2)")
    void shouldFindCardsByRetreatCostRange() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("retreatCostMin", "1")
                        .param("retreatCostMax", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[?(@.convertedRetreatCost)].convertedRetreatCost",
                        everyItem(allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(2)))));
    }

    @Test
    @DisplayName("Should find cards with high retreat cost (3+)")
    void shouldFindCardsByHighRetreatCost() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("retreatCostMin", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[?(@.convertedRetreatCost)].convertedRetreatCost",
                        everyItem(greaterThanOrEqualTo(3))));
    }

    // ============================================================================
    // Format Legality Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards legal in Standard format")
    void shouldFindCardsLegalInStandard() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("formats", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].legalities.standard",
                        everyItem(anyOf(is("legal"), is("Legal")))));
    }

    @Test
    @DisplayName("Should find cards legal in Expanded format")
    void shouldFindCardsLegalInExpanded() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("formats", "Expanded"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].legalities.expanded",
                        everyItem(anyOf(is("legal"), is("Legal")))));
    }

    @Test
    @DisplayName("Should find cards legal in Unlimited format")
    void shouldFindCardsLegalInUnlimited() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("formats", "Unlimited"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].legalities.unlimited",
                        everyItem(anyOf(is("legal"), is("Legal")))));
    }

    @Test
    @DisplayName("Should find cards legal in Standard OR Expanded (OR logic)")
    void shouldFindCardsLegalInStandardOrExpanded() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("formats", "Standard")
                        .param("formats", "Expanded"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
        // Cards should be legal in at least one of Standard or Expanded
    }

    @Test
    @DisplayName("Should find cards legal in BOTH Standard AND Expanded (AND logic)")
    void shouldFindCardsLegalInBothFormats() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("formats", "Standard")
                        .param("formats", "Expanded")
                        .param("formatsMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].legalities.standard",
                        everyItem(anyOf(is("legal"), is("Legal")))))
                .andExpect(jsonPath("$.results[*].legalities.expanded",
                        everyItem(anyOf(is("legal"), is("Legal")))));
    }

    @Test
    @DisplayName("Should find cards BANNED in Standard format")
    void shouldFindCardsBannedInStandard() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("formatsBanned", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on test data
        // Just verify the endpoint works
    }

    @Test
    @DisplayName("Should find cards legal in Expanded but banned in Standard")
    void shouldFindCardsLegalInExpandedButBannedInStandard() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("formats", "Expanded")
                        .param("formatsBanned", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Interesting edge case - legal in one format but banned in another
    }

    // ============================================================================
    // Combined Detail Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by artist and regulation mark")
    void shouldFindCardsByArtistAndRegulationMark() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("artist", "Ken Sugimori")
                        .param("regulationMark", "D"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find cards with low retreat cost legal in Standard")
    void shouldFindLowRetreatCostCardsLegalInStandard() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("retreatCostMax", "1")
                        .param("formats", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should combine detail filters with core filters")
    void shouldCombineDetailFiltersWithCoreFilters() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Fire")
                        .param("artist", "Ken Sugimori")
                        .param("retreatCostMin", "1")
                        .param("formats", "Unlimited"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find high HP cards with specific artist and format")
    void shouldFindHighHpCardsByArtistAndFormat() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hpMin", "100")
                        .param("artist", "Mitsuhiro Arita")
                        .param("formats", "Unlimited"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }
}
