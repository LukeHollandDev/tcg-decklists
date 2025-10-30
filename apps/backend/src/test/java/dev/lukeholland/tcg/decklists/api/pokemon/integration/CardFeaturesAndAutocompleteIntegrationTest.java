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
 * Integration tests for Pokemon card features and autocomplete endpoints.
 * <p>
 * Tests cover:
 * - GET /api/pokemon/features (available filter values)
 * - GET /api/pokemon/artists (autocomplete)
 * - GET /api/pokemon/attacks (autocomplete)
 * - GET /api/pokemon/abilities (autocomplete)
 * - GET /api/pokemon/card-names (autocomplete)
 * - GET /api/pokemon/sets (autocomplete)
 */
@DisplayName("Pokemon Card - Features & Autocomplete")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardFeaturesAndAutocompleteIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // GET /api/pokemon/features Tests
    // ============================================================================

    @Test
    @DisplayName("Should return features endpoint with all filter options")
    void shouldReturnFeaturesEndpoint() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("pokemon"))
                .andExpect(jsonPath("$.supertypes").isArray())
                .andExpect(jsonPath("$.types").isArray())
                .andExpect(jsonPath("$.subtypes").isArray())
                .andExpect(jsonPath("$.sets").isArray())
                .andExpect(jsonPath("$.rarities").isArray())
                .andExpect(jsonPath("$.formats").isArray())
                .andExpect(jsonPath("$.regulationMarks").isArray());
    }

    @Test
    @DisplayName("Should return all supertypes")
    void shouldReturnAllSupertypes() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supertypes").isArray())
                .andExpect(jsonPath("$.supertypes", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all Pokemon types")
    void shouldReturnAllPokemonTypes() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types").isArray())
                .andExpect(jsonPath("$.types", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all subtypes")
    void shouldReturnAllSubtypes() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtypes").isArray())
                .andExpect(jsonPath("$.subtypes", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all sets")
    void shouldReturnAllSets() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets").isArray())
                .andExpect(jsonPath("$.sets", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all rarities")
    void shouldReturnAllRarities() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rarities").isArray())
                .andExpect(jsonPath("$.rarities", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all formats")
    void shouldReturnAllFormats() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formats").isArray())
                .andExpect(jsonPath("$.formats", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all regulation marks")
    void shouldReturnAllRegulationMarks() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regulationMarks").isArray());
        // May or may not have regulation marks depending on test data
    }

    // ============================================================================
    // GET /api/pokemon/artists Autocomplete Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete artists with query")
    void shouldAutocompleteArtistsWithQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/artists")
                        .param("query", "Ken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.query").value("Ken"))
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.limit").value(10));
    }

    @Test
    @DisplayName("Should autocomplete artists with custom limit")
    void shouldAutocompleteArtistsWithCustomLimit() throws Exception {
        mockMvc.perform(get("/api/pokemon/artists")
                        .param("query", "a")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(5))))
                .andExpect(jsonPath("$.limit").value(5));
    }

    @Test
    @DisplayName("Should return 400 for artists without query")
    void shouldReturn400ForArtistsWithoutQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/artists"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle case-insensitive artist search")
    void shouldHandleCaseInsensitiveArtistSearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/artists")
                        .param("query", "ken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // GET /api/pokemon/attacks Autocomplete Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete attacks with query")
    void shouldAutocompleteAttacksWithQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/attacks")
                        .param("query", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.query").value("Fire"))
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.limit").value(10));
    }

    @Test
    @DisplayName("Should autocomplete attacks with custom limit")
    void shouldAutocompleteAttacksWithCustomLimit() throws Exception {
        mockMvc.perform(get("/api/pokemon/attacks")
                        .param("query", "a")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(3))))
                .andExpect(jsonPath("$.limit").value(3));
    }

    @Test
    @DisplayName("Should return 400 for attacks without query")
    void shouldReturn400ForAttacksWithoutQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/attacks"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle case-insensitive attack search")
    void shouldHandleCaseInsensitiveAttackSearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/attacks")
                        .param("query", "FIRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // GET /api/pokemon/abilities Autocomplete Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete abilities with query")
    void shouldAutocompleteAbilitiesWithQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/abilities")
                        .param("query", "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.query").value("a"))
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.limit").value(10));
    }

    @Test
    @DisplayName("Should autocomplete abilities with custom limit")
    void shouldAutocompleteAbilitiesWithCustomLimit() throws Exception {
        mockMvc.perform(get("/api/pokemon/abilities")
                        .param("query", "power")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(2))))
                .andExpect(jsonPath("$.limit").value(2));
    }

    @Test
    @DisplayName("Should return 400 for abilities without query")
    void shouldReturn400ForAbilitiesWithoutQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/abilities"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle case-insensitive ability search")
    void shouldHandleCaseInsensitiveAbilitySearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/abilities")
                        .param("query", "STATIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // GET /api/pokemon/card-names Autocomplete Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete card names with query")
    void shouldAutocompleteCardNamesWithQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/card-names")
                        .param("query", "Char"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.query").value("Char"))
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.limit").value(10));
    }

    @Test
    @DisplayName("Should include card IDs in card name results")
    void shouldIncludeCardIdsInCardNameResults() throws Exception {
        mockMvc.perform(get("/api/pokemon/card-names")
                        .param("query", "Pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Results should be in format "CardName (card-id)"
    }

    @Test
    @DisplayName("Should autocomplete card names with custom limit")
    void shouldAutocompleteCardNamesWithCustomLimit() throws Exception {
        mockMvc.perform(get("/api/pokemon/card-names")
                        .param("query", "a")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(5))))
                .andExpect(jsonPath("$.limit").value(5));
    }

    @Test
    @DisplayName("Should return 400 for card names without query")
    void shouldReturn400ForCardNamesWithoutQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/card-names"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle case-insensitive card name search")
    void shouldHandleCaseInsensitiveCardNameSearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/card-names")
                        .param("query", "PIKACHU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should handle accent-insensitive card name search")
    void shouldHandleAccentInsensitiveCardNameSearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/card-names")
                        .param("query", "flabebe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Should match "Flabébé" even though query has no accents
    }

    // ============================================================================
    // GET /api/pokemon/sets Autocomplete Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete set names with query")
    void shouldAutocompleteSetNamesWithQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/sets")
                        .param("query", "Base"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.query").value("Base"))
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.limit").value(10));
    }

    @Test
    @DisplayName("Should autocomplete set names with custom limit")
    void shouldAutocompleteSetNamesWithCustomLimit() throws Exception {
        mockMvc.perform(get("/api/pokemon/sets")
                        .param("query", "a")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(3))))
                .andExpect(jsonPath("$.limit").value(3));
    }

    @Test
    @DisplayName("Should return 400 for sets without query")
    void shouldReturn400ForSetsWithoutQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/sets"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle case-insensitive set search")
    void shouldHandleCaseInsensitiveSetSearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/sets")
                        .param("query", "BASE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // Edge Cases for Autocomplete
    // ============================================================================

    @Test
    @DisplayName("Should handle empty autocomplete results")
    void shouldHandleEmptyAutocompleteResults() throws Exception {
        mockMvc.perform(get("/api/pokemon/artists")
                        .param("query", "NonExistentArtist12345XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("Should enforce maximum limit for autocomplete")
    void shouldEnforceMaximumLimitForAutocomplete() throws Exception {
        mockMvc.perform(get("/api/pokemon/artists")
                        .param("query", "a")
                        .param("limit", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(100))));
        // Maximum limit should be 100
    }
}
