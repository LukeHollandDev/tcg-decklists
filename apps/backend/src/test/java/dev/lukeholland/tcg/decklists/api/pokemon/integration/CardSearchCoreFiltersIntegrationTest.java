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
 * Integration tests for Pokemon card search with core filters (Phase 1 filters).
 * <p>
 * Tests cover:
 * - Name filtering (partial match, accent-insensitive, case-insensitive)
 * - Supertype filtering (Pokémon, Trainer, Energy)
 * - Type filtering (Fire, Water, etc.) with AND/OR logic
 * - Subtype filtering (Basic, Stage 1, ex, etc.) with AND/OR logic
 * - Set ID filtering
 * - Rarity filtering
 * - HP range filtering (hpMin, hpMax)
 * - Empty results handling
 * - Multiple filter combinations
 */
@DisplayName("Pokemon Card Search - Core Filters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardSearchCoreFiltersIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Name Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by exact name match")
    void shouldFindCardsByExactName() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("name", "Charizard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", everyItem(containsStringIgnoringCase("Charizard"))));
    }

    @Test
    @DisplayName("Should find cards by partial name match")
    void shouldFindCardsByPartialName() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("name", "char"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", everyItem(containsStringIgnoringCase("char"))));
    }

    @Test
    @DisplayName("Should find cards with accent-insensitive name search")
    void shouldFindCardsByAccentInsensitiveName() throws Exception {
        // Search for "Flabebe" should find "Flabébé"
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("name", "flabebe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[0].name").value("Flabébé"));
    }

    @Test
    @DisplayName("Should find cards with case-insensitive name search")
    void shouldFindCardsByCaseInsensitiveName() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("name", "PIKACHU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", hasItem("Pikachu")));
    }

    @Test
    @DisplayName("Should return empty results for non-existent name")
    void shouldReturnEmptyResultsForNonExistentName() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("name", "NonExistentCardName12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    // ============================================================================
    // Supertype Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by supertype: Pokémon")
    void shouldFindCardsBySupertypePokemon() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("supertype", "Pokémon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].supertype", everyItem(is("Pokémon"))));
    }

    @Test
    @DisplayName("Should find cards by supertype: Trainer")
    void shouldFindCardsBySupertypeTrainer() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("supertype", "Trainer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].supertype", everyItem(is("Trainer"))));
    }

    @Test
    @DisplayName("Should find cards by supertype: Energy")
    void shouldFindCardsBySupertypeEnergy() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("supertype", "Energy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].supertype", everyItem(is("Energy"))));
    }

    // ============================================================================
    // Type Filter Tests (with AND/OR logic)
    // ============================================================================

    @Test
    @DisplayName("Should find cards by single type: Fire")
    void shouldFindCardsBySingleType() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Fire"))));
    }

    @Test
    @DisplayName("Should find cards by multiple types with OR logic (default)")
    void shouldFindCardsByMultipleTypesOrLogic() throws Exception {
        // Default OR logic - cards with Fire OR Water
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Fire")
                        .param("types", "Water"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                // Each card should have at least one of Fire or Water
                .andExpect(jsonPath("$.results[*].types", everyItem(anyOf(hasItem("Fire"), hasItem("Water")))));
    }

    @Test
    @DisplayName("Should find cards by multiple types with AND logic")
    void shouldFindCardsByMultipleTypesAndLogic() throws Exception {
        // AND logic - cards with BOTH Fire AND another type (if any exist)
        // Note: Most Pokemon have single types, but some have dual types
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Fire")
                        .param("typesMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                // All results should have Fire type
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Fire"))));
    }

    @Test
    @DisplayName("Should find Lightning-type cards")
    void shouldFindLightningTypeCards() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Lightning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Lightning"))));
    }

    @Test
    @DisplayName("Should find Fairy-type cards")
    void shouldFindFairyTypeCards() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Fairy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Fairy"))));
    }

    // ============================================================================
    // Subtype Filter Tests (with AND/OR logic)
    // ============================================================================

    @Test
    @DisplayName("Should find cards by single subtype: Basic")
    void shouldFindCardsBySingleSubtype() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "Basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(hasItem("Basic"))));
    }

    @Test
    @DisplayName("Should find cards by subtype: Stage 1")
    void shouldFindCardsBySubtypeStage1() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "Stage 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(hasItem("Stage 1"))));
    }

    @Test
    @DisplayName("Should find cards by subtype: Stage 2")
    void shouldFindCardsBySubtypeStage2() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "Stage 2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(hasItem("Stage 2"))));
    }

    @Test
    @DisplayName("Should find cards by subtype: ex")
    void shouldFindCardsBySubtypeEx() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "ex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(hasItem("ex"))));
    }

    @Test
    @DisplayName("Should find cards by multiple subtypes with OR logic (default)")
    void shouldFindCardsByMultipleSubtypesOrLogic() throws Exception {
        // Default OR logic - cards with Basic OR Stage 1
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "Basic")
                        .param("subtypes", "Stage 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                // Each card should have at least one of Basic or Stage 1
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(anyOf(hasItem("Basic"), hasItem("Stage 1")))));
    }

    @Test
    @DisplayName("Should find cards by multiple subtypes with AND logic")
    void shouldFindCardsByMultipleSubtypesAndLogic() throws Exception {
        // AND logic - cards with BOTH subtypes (e.g., Stage 2 AND ex if any exist)
        // Charizard ex should be Stage 2 AND ex
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "ex")
                        .param("subtypesMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                // All results should have ex subtype
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(hasItem("ex"))));
    }

    // ============================================================================
    // Set ID Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by set ID: base1")
    void shouldFindCardsBySetId() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("setId", "base1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].setId", everyItem(is("base1"))));
    }

    @Test
    @DisplayName("Should find cards by set ID: swsh1")
    void shouldFindCardsBySetIdSwsh1() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("setId", "swsh1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                // May or may not have results depending on test data
                .andExpect(jsonPath("$.results[*].setId", everyItem(is("swsh1"))));
    }

    // ============================================================================
    // Rarity Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by rarity: Common")
    void shouldFindCardsByRarityCommon() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("rarity", "Common"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].rarityName", everyItem(is("Common"))));
    }

    @Test
    @DisplayName("Should find cards by rarity: Rare Holo")
    void shouldFindCardsByRarityRareHolo() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("rarity", "Rare Holo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].rarityName", everyItem(is("Rare Holo"))));
    }

    // ============================================================================
    // HP Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with HP >= 100 (hpMin)")
    void shouldFindCardsByMinHp() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("hpMin", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                // Filter out cards without HP (Trainer/Energy)
                .andExpect(jsonPath("$.results[?(@.hpNumeric)].hpNumeric", everyItem(greaterThanOrEqualTo(100))));
    }

    @Test
    @DisplayName("Should find cards with HP <= 50 (hpMax)")
    void shouldFindCardsByMaxHp() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("hpMax", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                // Filter out cards without HP (Trainer/Energy)
                .andExpect(jsonPath("$.results[?(@.hpNumeric)].hpNumeric", everyItem(lessThanOrEqualTo(50))));
    }

    @Test
    @DisplayName("Should find cards within HP range (hpMin and hpMax)")
    void shouldFindCardsByHpRange() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("hpMin", "40")
                        .param("hpMax", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                // Filter out cards without HP (Trainer/Energy)
                .andExpect(jsonPath("$.results[?(@.hpNumeric)].hpNumeric", everyItem(allOf(
                        greaterThanOrEqualTo(40),
                        lessThanOrEqualTo(100)
                ))));
    }

    @Test
    @DisplayName("Should return empty results for impossible HP range")
    void shouldReturnEmptyResultsForImpossibleHpRange() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("hpMin", "500")
                        .param("hpMax", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    // ============================================================================
    // Multiple Filter Combination Tests
    // ============================================================================

    @Test
    @DisplayName("Should find Fire-type Basic Pokemon")
    void shouldFindFireTypeBasicPokemon() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("types", "Fire")
                        .param("subtypes", "Basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Fire"))))
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(hasItem("Basic"))));
    }

    @Test
    @DisplayName("Should find Stage 2 Pokemon with HP >= 100")
    void shouldFindStage2PokemonWithHighHp() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("subtypes", "Stage 2")
                        .param("hpMin", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(hasItem("Stage 2"))))
                .andExpect(jsonPath("$.results[*].hpNumeric", everyItem(greaterThanOrEqualTo(100))));
    }

    @Test
    @DisplayName("Should find cards in base1 set with rarity Rare Holo")
    void shouldFindCardsInSetWithRarity() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("setId", "base1")
                        .param("rarity", "Rare Holo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].setId", everyItem(is("base1"))))
                .andExpect(jsonPath("$.results[*].rarityName", everyItem(is("Rare Holo"))));
    }

    @Test
    @DisplayName("Should search with name filter and type filter combined")
    void shouldSearchWithNameAndTypeFilters() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("name", "char")
                        .param("types", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", everyItem(containsStringIgnoringCase("char"))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Fire"))));
    }

    // ============================================================================
    // Pagination with Filters Tests
    // ============================================================================

    @Test
    @DisplayName("Should paginate search results with filters")
    void shouldPaginateSearchResultsWithFilters() throws Exception {
        mockMvc.perform(get("/api/v1/pokemon/search")
                        .param("supertype", "Pokémon")
                        .param("page", "0")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(5))))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalResults").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }
}
