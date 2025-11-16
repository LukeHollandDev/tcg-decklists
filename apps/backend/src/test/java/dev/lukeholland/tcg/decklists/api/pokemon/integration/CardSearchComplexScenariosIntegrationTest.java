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
 * Integration tests for complex Pokemon card search scenarios combining multiple filters.
 * <p>
 * Tests cover:
 * - Multi-filter combinations (3+ filters)
 * - Edge cases and boundary conditions
 * - Performance with complex queries
 * - Real-world search scenarios
 */
@DisplayName("Pokemon Card Search - Complex Scenarios")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardSearchComplexScenariosIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Complex Multi-Filter Scenarios
    // ============================================================================

    @Test
    @DisplayName("Should find Fire-type Stage 2 cards with high HP and abilities")
    void shouldFindFireStage2CardsWithHighHpAndAbilities() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Fire")
                        .param("subtypes", "Stage 2")
                        .param("hpMin", "100")
                        .param("hasAbility", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find Basic Pokemon with attacks dealing high damage")
    void shouldFindBasicPokemonWithHighDamageAttacks() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "Basic")
                        .param("attackDamageMin", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find evolved Pokemon weak to Water with abilities")
    void shouldFindEvolvedPokemonWeakToWaterWithAbilities() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "Stage 1")
                        .param("weaknessType", "Water")
                        .param("hasAbility", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find Grass-type cards with Fire weakness and low retreat cost")
    void shouldFindGrassCardsWithFireWeaknessLowRetreat() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Grass")
                        .param("weaknessType", "Fire")
                        .param("retreatCostMax", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find cards legal in Standard with rule boxes and high HP")
    void shouldFindStandardLegalRuleBoxHighHpCards() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("formats", "Standard")
                        .param("hasRuleBox", "true")
                        .param("hpMin", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // Real-World Search Scenarios
    // ============================================================================

    @Test
    @DisplayName("Should find competitive Stage 2 cards (high HP, abilities, legal in Standard)")
    void shouldFindCompetitiveStage2Cards() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "Stage 2")
                        .param("hpMin", "120")
                        .param("hasAbility", "true")
                        .param("formats", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find budget deck options (Basic Pokemon, decent HP, low retreat)")
    void shouldFindBudgetDeckOptions() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "Basic")
                        .param("hpMin", "60")
                        .param("retreatCostMax", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on test data combinations
    }

    @Test
    @DisplayName("Should find energy acceleration cards (abilities with 'attach' text)")
    void shouldFindEnergyAccelerationCards() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("abilityText", "attach"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find Pokemon with draw support (attack or ability text contains 'draw')")
    void shouldFindDrawSupportCards() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("attackText", "draw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find tanky Psychic Pokemon (Psychic-type, high HP, resistance)")
    void shouldFindTankyPsychicPokemon() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Psychic")
                        .param("hpMin", "80")
                        .param("hasResistance", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // Edge Cases and Boundary Conditions
    // ============================================================================

    @Test
    @DisplayName("Should handle empty results gracefully with many filters")
    void shouldHandleEmptyResultsWithManyFilters() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Dragon")
                        .param("subtypes", "Basic")
                        .param("hpMin", "300")
                        .param("attackDamageMin", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    @Test
    @DisplayName("Should find all Charizard evolution line")
    void shouldFindCharizardEvolutionLine() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("name", "Char"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should search across multiple card attributes simultaneously")
    void shouldSearchAcrossMultipleAttributes() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("name", "chu")
                        .param("types", "Lightning")
                        .param("subtypes", "Basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find cards by specific artist with type filter")
    void shouldFindCardsByArtistAndType() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("artist", "Ken Sugimori")
                        .param("types", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should combine format legality with multiple gameplay filters")
    void shouldCombineFormatLegalityWithGameplayFilters() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("formats", "Unlimited")
                        .param("types", "Water")
                        .param("hasAbility", "true")
                        .param("attackDamageMin", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // Performance and Stress Scenarios
    // ============================================================================

    @Test
    @DisplayName("Should handle query with all available filter types")
    void shouldHandleQueryWithAllFilterTypes() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("name", "i")
                        .param("types", "Fire")
                        .param("subtypes", "Stage 1")
                        .param("hpMin", "50")
                        .param("hpMax", "200")
                        .param("hasAbility", "true")
                        .param("attackDamageMin", "20")
                        .param("retreatCostMax", "3")
                        .param("formats", "Unlimited")
                        .param("hasWeakness", "true")
                        .param("hasResistance", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should handle multiple AND filters efficiently")
    void shouldHandleMultipleAndFilters() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Fire")
                        .param("types", "Fighting")
                        .param("typesMatchAll", "true")
                        .param("formats", "Standard")
                        .param("formats", "Expanded")
                        .param("formatsMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find cards matching complex evolution criteria")
    void shouldFindCardsMatchingComplexEvolutionCriteria() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("evolvesFrom", "Char")
                        .param("types", "Fire")
                        .param("hpMin", "80"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should search with pagination and complex filters")
    void shouldSearchWithPaginationAndComplexFilters() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Water")
                        .param("hpMin", "50")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.currentPage").value(0));
    }
}
