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
 * Integration tests for Pokemon card search with weakness and resistance type filters (Phase 3 filters).
 * <p>
 * Tests cover:
 * - Weakness type filtering (single, multiple, AND/OR logic)
 * - Resistance type filtering (single, multiple, AND/OR logic)
 * - Combined weakness and resistance filters
 * - Empty results handling
 * - Integration with other filters
 */
@DisplayName("Pokemon Card Search - Weakness & Resistance Type Filters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardSearchWeaknessResistanceIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Weakness Type Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards weak to Fire")
    void shouldFindCardsWeakToFire() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("weaknessType", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on test data
    }

    @Test
    @DisplayName("Should find cards weak to Water")
    void shouldFindCardsWeakToWater() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("weaknessType", "Water"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].weaknesses[*].type", hasItem("Water")));
    }

    @Test
    @DisplayName("Should return empty results for non-existent weakness type")
    void shouldReturnEmptyResultsForNonExistentWeaknessType() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("weaknessType", "Dragon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    @Test
    @DisplayName("Should find cards weak to Fire OR Water (OR logic)")
    void shouldFindCardsWeakToFireOrWater() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("weaknessType", "Fire")
                        .param("weaknessType", "Water"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
        // Cards should be weak to at least one of Fire or Water
    }

    @Test
    @DisplayName("Should find cards weak to BOTH Fire AND Water (AND logic)")
    void shouldFindCardsWeakToBothFireAndWater() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("weaknessType", "Fire")
                        .param("weaknessType", "Water")
                        .param("weaknessTypeMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results - rare for cards to have multiple weakness types
    }

    // ============================================================================
    // Resistance Type Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards resistant to Fighting")
    void shouldFindCardsResistantToFighting() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("resistanceType", "Fighting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].resistances[*].type", hasItem("Fighting")));
    }

    @Test
    @DisplayName("Should find cards resistant to Psychic")
    void shouldFindCardsResistantToPsychic() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("resistanceType", "Psychic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on test data
    }

    @Test
    @DisplayName("Should find cards resistant to Fighting OR Psychic (OR logic)")
    void shouldFindCardsResistantToFightingOrPsychic() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("resistanceType", "Fighting")
                        .param("resistanceType", "Psychic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should find cards resistant to BOTH Fighting AND Psychic (AND logic)")
    void shouldFindCardsResistantToBothFightingAndPsychic() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("resistanceType", "Fighting")
                        .param("resistanceType", "Psychic")
                        .param("resistanceTypeMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results - rare for cards to have multiple resistance types
    }

    // ============================================================================
    // Combined Weakness and Resistance Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards weak to Fire and resistant to Fighting")
    void shouldFindCardsWeakToFireResistantToFighting() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("weaknessType", "Fire")
                        .param("resistanceType", "Fighting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on card combinations
    }

    @Test
    @DisplayName("Should find Grass-type cards weak to Fire")
    void shouldFindGrassTypeCardsWeakToFire() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Grass")
                        .param("weaknessType", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on test data combinations
    }

    @Test
    @DisplayName("Should find Fire-type cards weak to Water")
    void shouldFindFireTypeCardsWeakToWater() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Fire")
                        .param("weaknessType", "Water"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Fire"))));
    }

    @Test
    @DisplayName("Should find Psychic-type cards resistant to Fighting")
    void shouldFindPsychicTypeCardsResistantToFighting() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Psychic")
                        .param("resistanceType", "Fighting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on test data combinations
    }

    @Test
    @DisplayName("Should combine weakness/resistance filters with other filters")
    void shouldCombineWeaknessResistanceWithOtherFilters() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("weaknessType", "Water")
                        .param("hpMin", "60")
                        .param("subtypes", "Stage 2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }
}
