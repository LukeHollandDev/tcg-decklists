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
 * Integration tests for Pokemon card search with attack filters (Phase 2 filters).
 * <p>
 * Tests cover:
 * - Attack name filtering (partial match, accent-insensitive)
 * - Attack text/description filtering
 * - Attack damage filtering (min, max, range)
 * - Attack cost filtering with multiset matching
 * - AND/OR logic for attack costs
 * - Empty results handling
 * - Complex attack filter combinations
 */
@DisplayName("Pokemon Card Search - Attack Filters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardSearchAttackFiltersIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Attack Name Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards by exact attack name")
    void shouldFindCardsByExactAttackName() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackName", "Fire Spin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].attacks[*].name", hasItem(containsStringIgnoringCase("Fire Spin"))));
    }

    @Test
    @DisplayName("Should find cards by partial attack name match")
    void shouldFindCardsByPartialAttackName() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackName", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].attacks[*].name", hasItem(containsStringIgnoringCase("Fire"))));
    }

    @Test
    @DisplayName("Should find cards with case-insensitive attack name search")
    void shouldFindCardsByCaseInsensitiveAttackName() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackName", "THUNDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].attacks[*].name", hasItem(containsStringIgnoringCase("Thunder"))));
    }

    @Test
    @DisplayName("Should find cards with accent-insensitive attack name search")
    void shouldFindCardsByAccentInsensitiveAttackName() throws Exception {
        // If there are attacks with accents in test data, test them here
        // For now, just verify the search works even if no results
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackName", "petal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should return empty results for non-existent attack name")
    void shouldReturnEmptyResultsForNonExistentAttackName() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackName", "NonExistentAttackName12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    // ============================================================================
    // Attack Text/Description Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with attack text containing query")
    void shouldFindCardsByAttackText() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackText", "discard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].attacks[*].text", hasItem(containsStringIgnoringCase("discard"))));
    }

    @Test
    @DisplayName("Should find cards with attack text using case-insensitive search")
    void shouldFindCardsByAttackTextCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackText", "ENERGY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].attacks[*].text", hasItem(containsStringIgnoringCase("Energy"))));
    }

    @Test
    @DisplayName("Should find cards with attack text about damage")
    void shouldFindCardsByAttackTextDamage() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackText", "damage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return empty results for non-existent attack text")
    void shouldReturnEmptyResultsForNonExistentAttackText() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackText", "NonExistentAttackText12345XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    // ============================================================================
    // Attack Damage Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find cards with attack damage >= 50 (attackDamageMin)")
    void shouldFindCardsByMinAttackDamage() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackDamageMin", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                // Filter out attacks without damage (null)
                .andExpect(jsonPath("$.results[*].attacks[?(@.damageNumeric)].damageNumeric",
                        everyItem(greaterThanOrEqualTo(50))));
    }

    @Test
    @DisplayName("Should find cards with attack damage <= 30 (attackDamageMax)")
    void shouldFindCardsByMaxAttackDamage() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackDamageMax", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                // Filter out attacks without damage (null)
                .andExpect(jsonPath("$.results[*].attacks[?(@.damageNumeric)].damageNumeric",
                        everyItem(lessThanOrEqualTo(30))));
    }

    @Test
    @DisplayName("Should find cards within attack damage range (attackDamageMin and attackDamageMax)")
    void shouldFindCardsByAttackDamageRange() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackDamageMin", "40")
                        .param("attackDamageMax", "90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].attacks[?(@.damageNumeric)].damageNumeric",
                        everyItem(allOf(greaterThanOrEqualTo(40), lessThanOrEqualTo(90)))));
    }

    @Test
    @DisplayName("Should find cards with high attack damage (100+)")
    void shouldFindCardsByHighAttackDamage() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackDamageMin", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].attacks[?(@.damageNumeric)].damageNumeric",
                        everyItem(greaterThanOrEqualTo(100))));
    }

    @Test
    @DisplayName("Should return empty results for impossible attack damage range")
    void shouldReturnEmptyResultsForImpossibleAttackDamageRange() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackDamageMin", "500")
                        .param("attackDamageMax", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    // ============================================================================
    // Attack Cost Filter Tests (with multiset matching)
    // ============================================================================

    @Test
    @DisplayName("Should find cards with single attack cost type: Fire")
    void shouldFindCardsBySingleAttackCost() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackCost", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].attacks[*].cost", hasItem(hasItem("Fire"))));
    }

    @Test
    @DisplayName("Should find cards with attack costs containing Fire OR Water (OR logic)")
    void shouldFindCardsByMultipleAttackCostsOrLogic() throws Exception {
        // Default OR logic - attacks with Fire OR Water
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackCost", "Fire")
                        .param("attackCost", "Water"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].attacks[*].cost",
                        hasItem(anyOf(hasItem("Fire"), hasItem("Water")))));
    }

    @Test
    @DisplayName("Should find cards with at least 2x Fire energy (multiset matching)")
    void shouldFindCardsByMultisetAttackCost() throws Exception {
        // Searching for 2x Fire should match attacks with at least 2 Fire energy
        // This tests multiset subset matching (e.g., [Fire, Fire, Fire, Fire] matches)
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackCost", "Fire")
                        .param("attackCost", "Fire")
                        .param("attackCostMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
                // TODO: Add custom matcher for multiset matching
                // .andExpect(jsonPath("$.results[*].attacks[*].cost", hasAtLeastMultiset(Map.of("Fire", 2))));
    }

    @Test
    @DisplayName("Should find cards with at least 4x Fire energy (Charizard)")
    void shouldFindCardsByFourFireEnergy() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackCost", "Fire")
                        .param("attackCost", "Fire")
                        .param("attackCost", "Fire")
                        .param("attackCost", "Fire")
                        .param("attackCostMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", hasItem("Charizard")));
    }

    @Test
    @DisplayName("Should find cards with at least 2x Water energy")
    void shouldFindCardsByTwoWaterEnergy() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackCost", "Water")
                        .param("attackCost", "Water")
                        .param("attackCostMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should find cards with at least 2x Lightning + 1x Colorless (Raichu)")
    void shouldFindCardsByMixedMultisetAttackCost() throws Exception {
        // This tests complex multiset matching with different types
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackCost", "Lightning")
                        .param("attackCost", "Lightning")
                        .param("attackCost", "Colorless")
                        .param("attackCostMatchAll", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", hasItem("Raichu")));
    }

    @Test
    @DisplayName("Should find cards with Colorless attack costs")
    void shouldFindCardsByColorlessAttackCost() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackCost", "Colorless"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should find cards with Fairy attack costs")
    void shouldFindCardsByFairyAttackCost() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackCost", "Fairy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].name", hasItem("Flabébé")));
    }

    // ============================================================================
    // Combined Attack Filter Tests
    // ============================================================================

    @Test
    @DisplayName("Should find Fire-type cards with Fire attacks")
    void shouldFindFireTypeCardsWithFireAttacks() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Fire")
                        .param("attackCost", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].types", everyItem(hasItem("Fire"))));
    }

    @Test
    @DisplayName("Should find cards with high damage attacks (100+) that discard energy")
    void shouldFindHighDamageAttacksWithDiscardEffect() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackDamageMin", "100")
                        .param("attackText", "discard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should find cards with attack name and specific damage range")
    void shouldFindCardsByAttackNameAndDamageRange() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("attackName", "Slash")
                        .param("attackDamageMin", "40")
                        .param("attackDamageMax", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should combine attack filters with core filters (Stage 2 + high damage)")
    void shouldCombineAttackFiltersWithCoreFilters() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("subtypes", "Stage 2")
                        .param("attackDamageMin", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.results[*].subtypes", everyItem(hasItem("Stage 2"))));
    }
}
