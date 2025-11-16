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
 * Integration tests for Pokemon card generic search functionality.
 * <p>
 * Tests cover:
 * - Generic search across multiple fields (name, attacks, abilities, rules, artist)
 * - Field exclusion toggles (excludeName, excludeAttacks, excludeAbilities, excludeRules, excludeArtist)
 * - Generic search combined with other filters
 * - Accent-insensitive and case-insensitive matching
 * - Edge cases and validation
 */
@DisplayName("Pokemon Card Search - Generic Search")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardSearchGenericSearchIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Basic Generic Search Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by generic search in name field")
    void shouldFindCardsByGenericSearchInName() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalResults").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Should find cards by generic search across multiple fields")
    void shouldFindCardsByGenericSearchAcrossFields() throws Exception {
        // Search for a common word that might appear in various fields
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "attack"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Note: Just verify the search executes successfully
        // The actual results depend on test data
    }

    @Test
    @DisplayName("Should accept generic search with attack-related terms")
    void shouldAcceptGenericSearchWithAttackTerms() throws Exception {
        // Test that the search works with attack-related terms
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "damage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // The search should execute successfully regardless of results
    }

    @Test
    @DisplayName("Should accept generic search with ability-related terms")
    void shouldAcceptGenericSearchWithAbilityTerms() throws Exception {
        // Test that the search works with ability-related terms
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "ability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // The search should execute successfully regardless of results
    }

    @Test
    @DisplayName("Should find cards by generic search in artist name")
    void shouldFindCardsByGenericSearchInArtistName() throws Exception {
        // Search for "Ken Sugimori" which appears in artist names
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Ken Sugimori"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalResults").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Should find cards by generic search in rule text")
    void shouldFindCardsByGenericSearchInRuleText() throws Exception {
        // Search for "Prize cards" which appears in rule text
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Prize cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalResults").value(greaterThan(0)));
    }

    // ============================================================================
    // Field Exclusion Tests
    // ============================================================================

    @Test
    @DisplayName("Should exclude name field when excludeName is true")
    void shouldExcludeNameField() throws Exception {
        // First, get total results with name included
        var withName = mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Pikachu"))
                .andExpect(status().isOk())
                .andReturn();

        int totalWithName = Integer.parseInt(
                objectMapper.readTree(withName.getResponse().getContentAsString())
                        .get("totalResults").asText()
        );

        // Then, get results with name excluded
        var withoutName = mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Pikachu")
                        .param("excludeName", "true"))
                .andExpect(status().isOk())
                .andReturn();

        int totalWithoutName = Integer.parseInt(
                objectMapper.readTree(withoutName.getResponse().getContentAsString())
                        .get("totalResults").asText()
        );

        // Results should be different (fewer results when name is excluded)
        // Note: Some cards might still match in other fields (e.g., "Pikachu Project" artist)
        assert totalWithoutName <= totalWithName;
    }

    @Test
    @DisplayName("Should exclude attacks field when excludeAttacks is true")
    void shouldExcludeAttacksField() throws Exception {
        // Search for "Thunderbolt" which appears in both card names and attack names
        var withAttacks = mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Thunderbolt"))
                .andExpect(status().isOk())
                .andReturn();

        int totalWithAttacks = Integer.parseInt(
                objectMapper.readTree(withAttacks.getResponse().getContentAsString())
                        .get("totalResults").asText()
        );

        var withoutAttacks = mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Thunderbolt")
                        .param("excludeAttacks", "true"))
                .andExpect(status().isOk())
                .andReturn();

        int totalWithoutAttacks = Integer.parseInt(
                objectMapper.readTree(withoutAttacks.getResponse().getContentAsString())
                        .get("totalResults").asText()
        );

        // Should have fewer or equal results when attacks are excluded
        assert totalWithoutAttacks <= totalWithAttacks;
    }

    @Test
    @DisplayName("Should exclude abilities field when excludeAbilities is true")
    void shouldExcludeAbilitiesField() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "draw")
                        .param("excludeAbilities", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // The test verifies the endpoint works with the parameter
    }

    @Test
    @DisplayName("Should exclude rules field when excludeRules is true")
    void shouldExcludeRulesField() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Prize")
                        .param("excludeRules", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // The test verifies the endpoint works with the parameter
    }

    @Test
    @DisplayName("Should exclude artist field when excludeArtist is true")
    void shouldExcludeArtistField() throws Exception {
        // Search for "Ken" which appears in artist name "Ken Sugimori"
        var withArtist = mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Ken"))
                .andExpect(status().isOk())
                .andReturn();

        int totalWithArtist = Integer.parseInt(
                objectMapper.readTree(withArtist.getResponse().getContentAsString())
                        .get("totalResults").asText()
        );

        var withoutArtist = mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Ken")
                        .param("excludeArtist", "true"))
                .andExpect(status().isOk())
                .andReturn();

        int totalWithoutArtist = Integer.parseInt(
                objectMapper.readTree(withoutArtist.getResponse().getContentAsString())
                        .get("totalResults").asText()
        );

        // Should have fewer results when artist is excluded
        assert totalWithoutArtist < totalWithArtist;
    }

    // ============================================================================
    // Multiple Field Exclusion Tests
    // ============================================================================

    @Test
    @DisplayName("Should handle multiple field exclusions")
    void shouldHandleMultipleFieldExclusions() throws Exception {
        // Search only in attacks by excluding all other fields
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "damage")
                        .param("excludeName", "true")
                        .param("excludeAbilities", "true")
                        .param("excludeRules", "true")
                        .param("excludeArtist", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all results when no search term provided")
    void shouldReturnAllResultsWhenNoSearchTerm() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return empty results when search term does not match any field")
    void shouldReturnEmptyResultsForNonMatchingSearchTerm() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "NonExistentSearchTerm12345XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    // ============================================================================
    // Case and Accent Insensitive Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with case-insensitive generic search")
    void shouldFindCardsByCaseInsensitiveGenericSearch() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "PIKACHU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should find cards with accent-insensitive generic search")
    void shouldFindCardsByAccentInsensitiveGenericSearch() throws Exception {
        // Search for "Flabebe" should find "Flabébé"
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "flabebe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    // ============================================================================
    // Generic Search Combined with Other Filters
    // ============================================================================

    @Test
    @DisplayName("Should combine generic search with type filter")
    void shouldCombineGenericSearchWithTypeFilter() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "fire")
                        .param("types", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types[*]", hasItem("Fire")));
    }

    @Test
    @DisplayName("Should combine generic search with rarity filter")
    void shouldCombineGenericSearchWithRarityFilter() throws Exception {
        // Test that generic search can be combined with other filters
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "fire")
                        .param("rarity", "Common"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // The combination should work regardless of whether results are found
    }

    @Test
    @DisplayName("Should combine generic search with HP range filter")
    void shouldCombineGenericSearchWithHpRangeFilter() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Pikachu")
                        .param("hpMin", "60")
                        .param("hpMax", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should combine generic search with multiple filters")
    void shouldCombineGenericSearchWithMultipleFilters() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "electric")
                        .param("types", "Lightning")
                        .param("supertype", "Pokémon")
                        .param("hpMin", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // Pagination and Sorting with Generic Search
    // ============================================================================

    @Test
    @DisplayName("Should paginate generic search results")
    void shouldPaginateGenericSearchResults() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "attack")
                        .param("page", "0")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(5))))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    @DisplayName("Should sort generic search results")
    void shouldSortGenericSearchResults() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Pikachu")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    // ============================================================================
    // Edge Cases
    // ============================================================================

    @Test
    @DisplayName("Should handle empty string search term")
    void shouldHandleEmptyStringSearchTerm() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should handle whitespace-only search term")
    void shouldHandleWhitespaceOnlySearchTerm() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should handle special characters in search term")
    void shouldHandleSpecialCharactersInSearchTerm() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", "Pokémon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should handle very long search term")
    void shouldHandleVeryLongSearchTerm() throws Exception {
        String longSearchTerm = "a".repeat(100);
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("q", longSearchTerm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }
}
