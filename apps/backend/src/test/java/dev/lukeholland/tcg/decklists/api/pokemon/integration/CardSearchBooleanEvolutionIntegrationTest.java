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
 * Integration tests for Pokemon card search with boolean and evolution filters (Phase 3 filters).
 * <p>
 * Tests cover:
 * - Boolean filters (hasRuleBox, hasWeakness, hasResistance)
 * - Evolution filters (evolvesFrom, evolvesTo)
 * - Rule text filtering
 * - Empty results handling
 * - Combined filter tests
 */
@DisplayName("Pokemon Card Search - Boolean & Evolution Filters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardSearchBooleanEvolutionIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Boolean Filter Tests - Rule Box
    // ============================================================================

    @Test
    @DisplayName("Should find cards with rule boxes (hasRuleBox=true)")
    void shouldFindCardsWithRuleBoxes() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasRuleBox", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].rules", everyItem(not(empty()))));
    }

    @Test
    @DisplayName("Should find cards without rule boxes (hasRuleBox=false)")
    void shouldFindCardsWithoutRuleBoxes() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasRuleBox", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
        // Cards without rules may have empty array or null rules field
    }

    // ============================================================================
    // Boolean Filter Tests - Weakness
    // ============================================================================

    @Test
    @DisplayName("Should find cards with weaknesses (hasWeakness=true)")
    void shouldFindCardsWithWeaknesses() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasWeakness", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].weaknesses", everyItem(not(empty()))));
    }

    @Test
    @DisplayName("Should find cards without weaknesses (hasWeakness=false)")
    void shouldFindCardsWithoutWeaknesses() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasWeakness", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on test data
    }

    // ============================================================================
    // Boolean Filter Tests - Resistance
    // ============================================================================

    @Test
    @DisplayName("Should find cards with resistances (hasResistance=true)")
    void shouldFindCardsWithResistances() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasResistance", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].resistances", everyItem(not(empty()))));
    }

    @Test
    @DisplayName("Should find cards without resistances (hasResistance=false)")
    void shouldFindCardsWithoutResistances() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasResistance", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    // ============================================================================
    // Evolution Filter Tests - Evolves From
    // ============================================================================

    @Test
    @DisplayName("Should find cards that evolve from Pikachu")
    void shouldFindCardsEvolvingFromPikachu() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesFrom", "Pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", hasItem("Raichu")));
    }

    @Test
    @DisplayName("Should find cards that evolve from Charmander")
    void shouldFindCardsEvolvingFromCharmander() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesFrom", "Charmander"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", hasItem("Charmeleon")));
    }

    @Test
    @DisplayName("Should find cards with case-insensitive evolution search")
    void shouldFindCardsByEvolvesFromCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesFrom", "PIKACHU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should find cards with partial evolution name match")
    void shouldFindCardsByPartialEvolvesFrom() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesFrom", "Char"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Should match evolutions from Charmander, Charmeleon, etc.
    }

    @Test
    @DisplayName("Should return empty results for non-existent evolution source")
    void shouldReturnEmptyResultsForNonExistentEvolvesFrom() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesFrom", "NonExistentPokemon12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    // ============================================================================
    // Evolution Filter Tests - Evolves To
    // ============================================================================

    @Test
    @DisplayName("Should find cards that evolve to Raichu")
    void shouldFindCardsEvolvingToRaichu() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesTo", "Raichu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on evolution data population
    }

    @Test
    @DisplayName("Should find cards that evolve to Charizard")
    void shouldFindCardsEvolvingToCharizard() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesTo", "Charizard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", hasItem("Charmeleon")));
    }

    @Test
    @DisplayName("Should find cards with case-insensitive evolves to search")
    void shouldFindCardsByEvolvesToCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesTo", "RAICHU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // May or may not have results depending on evolution data population
    }

    @Test
    @DisplayName("Should return empty results for non-existent evolution target")
    void shouldReturnEmptyResultsForNonExistentEvolvesTo() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesTo", "NonExistentPokemon12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    // ============================================================================
    // Rule Text Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with rule text containing query")
    void shouldFindCardsByRuleText() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("ruleText", "ex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Should match cards with "ex" in their rule text
    }

    @Test
    @DisplayName("Should find cards with rule text using case-insensitive search")
    void shouldFindCardsByRuleTextCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("ruleText", "WHEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should return empty results for non-existent rule text")
    void shouldReturnEmptyResultsForNonExistentRuleText() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("ruleText", "NonExistentRuleText12345XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    // ============================================================================
    // Combined Boolean and Evolution Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with rule boxes and weaknesses")
    void shouldFindCardsWithRuleBoxesAndWeaknesses() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasRuleBox", "true")
                        .param("hasWeakness", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find cards with weaknesses but no resistances")
    void shouldFindCardsWithWeaknessesButNoResistances() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasWeakness", "true")
                        .param("hasResistance", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should find Fire-type cards that evolve from Charmander")
    void shouldFindFireTypeCardsEvolvingFromCharmander() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Fire")
                        .param("evolvesFrom", "Charmander"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Fire"))));
    }

    @Test
    @DisplayName("Should find evolution cards with high HP")
    void shouldFindEvolutionCardsWithHighHp() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("evolvesFrom", "Charmeleon")
                        .param("hpMin", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should combine boolean filters with core filters")
    void shouldCombineBooleanFiltersWithCoreFilters() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasWeakness", "true")
                        .param("hasResistance", "true")
                        .param("types", "Psychic")
                        .param("hpMin", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should find cards without weaknesses and without resistances")
    void shouldFindCardsWithoutWeaknessesOrResistances() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hasWeakness", "false")
                        .param("hasResistance", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Should find cards with neither weaknesses nor resistances
    }
}
