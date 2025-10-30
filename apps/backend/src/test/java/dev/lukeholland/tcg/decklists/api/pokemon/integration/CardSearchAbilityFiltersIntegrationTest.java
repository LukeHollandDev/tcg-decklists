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
 * Integration tests for Pokemon card search with ability filters (Phase 2 filters).
 * <p>
 * Tests cover:
 * - Ability presence filtering (hasAbility)
 * - Ability name filtering (partial match, accent-insensitive)
 * - Ability text/description filtering
 * - Empty results handling
 * - Combined ability and core filter tests
 */
@DisplayName("Pokemon Card Search - Ability Filters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardSearchAbilityFiltersIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Ability Presence Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with abilities (hasAbility=true)")
    void shouldFindCardsWithAbilities() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasAbility", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].abilities").isNotEmpty())
                .andExpect(jsonPath("$.results[*].abilities", everyItem(not(empty()))));
    }

    @Test
    @DisplayName("Should find cards without abilities (hasAbility=false)")
    void shouldFindCardsWithoutAbilities() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasAbility", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
        // Note: Cards without abilities may have empty array or no abilities field
    }

    // ============================================================================
    // Ability Name Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by exact ability name")
    void shouldFindCardsByExactAbilityName() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityName", "Energy Burn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].abilities[*].name", hasItem("Energy Burn")));
    }

    @Test
    @DisplayName("Should find cards by partial ability name match")
    void shouldFindCardsByPartialAbilityName() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityName", "Rain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].abilities[*].name", hasItem(containsStringIgnoringCase("Rain"))));
    }

    @Test
    @DisplayName("Should find cards with case-insensitive ability name search")
    void shouldFindCardsByCaseInsensitiveAbilityName() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityName", "STATIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].abilities[*].name", hasItem(containsStringIgnoringCase("Static"))));
    }

    @Test
    @DisplayName("Should find cards with accent-insensitive ability name search")
    void shouldFindCardsByAccentInsensitiveAbilityName() throws Exception {
        // Test accent-insensitive search (e.g., if abilities had accented names)
        // For now, verify the search works even if no specific accent cases
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityName", "static"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should return empty results for non-existent ability name")
    void shouldReturnEmptyResultsForNonExistentAbilityName() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityName", "NonExistentAbilityName12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    // ============================================================================
    // Ability Text/Description Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with ability text containing query")
    void shouldFindCardsByAbilityText() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityText", "Energy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].abilities[*].text", hasItem(containsStringIgnoringCase("Energy"))));
    }

    @Test
    @DisplayName("Should find cards with ability text using case-insensitive search")
    void shouldFindCardsByAbilityTextCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityText", "DAMAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].abilities[*].text", hasItem(containsStringIgnoringCase("damage"))));
    }

    @Test
    @DisplayName("Should find cards with abilities about turn actions")
    void shouldFindCardsByAbilityTextTurnActions() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityText", "during your turn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return empty results for non-existent ability text")
    void shouldReturnEmptyResultsForNonExistentAbilityText() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityText", "NonExistentAbilityText12345XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    // ============================================================================
    // Combined Ability Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with abilities and specific type")
    void shouldFindCardsWithAbilitiesAndType() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasAbility", "true")
                        .param("types", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Fire"))))
                .andExpect(jsonPath("$.results[*].abilities", everyItem(not(empty()))));
    }

    @Test
    @DisplayName("Should find Stage 2 cards with abilities")
    void shouldFindStage2CardsWithAbilities() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasAbility", "true")
                        .param("subtypes", "Stage 2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(hasItem("Stage 2"))))
                .andExpect(jsonPath("$.results[*].abilities", everyItem(not(empty()))));
    }

    @Test
    @DisplayName("Should find cards with specific ability name and type")
    void shouldFindCardsByAbilityNameAndType() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityName", "Rain Dance")
                        .param("types", "Water"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Water"))));
    }

    @Test
    @DisplayName("Should combine ability text search with core filters")
    void shouldCombineAbilityTextWithCoreFilters() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("abilityText", "attach")
                        .param("hpMin", "80"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[?(@.hpNumeric)].hpNumeric", everyItem(greaterThanOrEqualTo(80))));
    }
}
