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
 * - GET /api/pokemon/autocomplete/{field} (autocomplete for artist, attack, ability, set)
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
    @DisplayName("Should return features endpoint with static data and filters")
    void shouldReturnFeaturesEndpoint() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("pokemon"))
                .andExpect(jsonPath("$.static").exists())
                .andExpect(jsonPath("$.static.supertypes").isArray())
                .andExpect(jsonPath("$.static.types").isArray())
                .andExpect(jsonPath("$.static.subtypes").isArray())
                .andExpect(jsonPath("$.static.sets").isArray())
                .andExpect(jsonPath("$.static.rarities").isArray())
                .andExpect(jsonPath("$.static.formats").isArray())
                .andExpect(jsonPath("$.static.regulationMarks").isArray())
                .andExpect(jsonPath("$.filters").exists())
                .andExpect(jsonPath("$.filters").isMap());
    }

    @Test
    @DisplayName("Should return all supertypes in static data")
    void shouldReturnAllSupertypes() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.static.supertypes").isArray())
                .andExpect(jsonPath("$.static.supertypes", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all Pokemon types in static data")
    void shouldReturnAllPokemonTypes() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.static.types").isArray())
                .andExpect(jsonPath("$.static.types", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all subtypes in static data")
    void shouldReturnAllSubtypes() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.static.subtypes").isArray())
                .andExpect(jsonPath("$.static.subtypes", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all sets in static data")
    void shouldReturnAllSets() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.static.sets").isArray())
                .andExpect(jsonPath("$.static.sets", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all rarities in static data")
    void shouldReturnAllRarities() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.static.rarities").isArray())
                .andExpect(jsonPath("$.static.rarities", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all formats in static data")
    void shouldReturnAllFormats() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.static.formats").isArray())
                .andExpect(jsonPath("$.static.formats", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return all regulation marks in static data")
    void shouldReturnAllRegulationMarks() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.static.regulationMarks").isArray());
        // May or may not have regulation marks depending on test data
    }

    @Test
    @DisplayName("Should return filter metadata for all filters")
    void shouldReturnFilterMetadata() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters").isMap())
                .andExpect(jsonPath("$.filters.name").exists())
                .andExpect(jsonPath("$.filters.supertype").exists())
                .andExpect(jsonPath("$.filters.types").exists())
                .andExpect(jsonPath("$.filters.hp").exists())
                .andExpect(jsonPath("$.filters.attackName").exists())
                .andExpect(jsonPath("$.filters.hasAbility").exists());
    }

    @Test
    @DisplayName("Should return correct metadata for string filter")
    void shouldReturnCorrectStringFilterMetadata() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.name.type").value("string"))
                .andExpect(jsonPath("$.filters.name.operator").value("contains"))
                .andExpect(jsonPath("$.filters.name.parameterName").value("name"))
                .andExpect(jsonPath("$.filters.name.description").exists())
                .andExpect(jsonPath("$.filters.name.accentInsensitive").value(true));
    }

    @Test
    @DisplayName("Should return correct metadata for enum filter")
    void shouldReturnCorrectEnumFilterMetadata() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.supertype.type").value("enum"))
                .andExpect(jsonPath("$.filters.supertype.operator").value("equals"))
                .andExpect(jsonPath("$.filters.supertype.parameterName").value("supertype"))
                .andExpect(jsonPath("$.filters.supertype.valuesRef").value("static.supertypes"));
    }

    @Test
    @DisplayName("Should return correct metadata for multiselect filter")
    void shouldReturnCorrectMultiselectFilterMetadata() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.types.type").value("multiselect"))
                .andExpect(jsonPath("$.filters.types.operator").value("anyOf"))
                .andExpect(jsonPath("$.filters.types.parameterName").value("types"))
                .andExpect(jsonPath("$.filters.types.valuesRef").value("static.types"))
                .andExpect(jsonPath("$.filters.types.matchAllParameter").value("typesMatchAll"));
    }

    @Test
    @DisplayName("Should return correct metadata for range filter")
    void shouldReturnCorrectRangeFilterMetadata() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.hp.type").value("range"))
                .andExpect(jsonPath("$.filters.hp.operator").value("range"))
                .andExpect(jsonPath("$.filters.hp.minParameter").value("hpMin"))
                .andExpect(jsonPath("$.filters.hp.maxParameter").value("hpMax"));
    }

    @Test
    @DisplayName("Should return correct metadata for boolean filter")
    void shouldReturnCorrectBooleanFilterMetadata() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.hasAbility.type").value("boolean"))
                .andExpect(jsonPath("$.filters.hasAbility.operator").value("equals"))
                .andExpect(jsonPath("$.filters.hasAbility.parameterName").value("hasAbility"));
    }

    // ============================================================================
    // GET /api/pokemon/autocomplete/artist Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete artists with query")
    void shouldAutocompleteArtistsWithQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/artist")
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
        mockMvc.perform(get("/api/pokemon/autocomplete/artist")
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
        mockMvc.perform(get("/api/pokemon/autocomplete/artist"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle case-insensitive artist search")
    void shouldHandleCaseInsensitiveArtistSearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/artist")
                        .param("query", "ken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // GET /api/pokemon/autocomplete/attack Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete attacks with query")
    void shouldAutocompleteAttacksWithQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/attack")
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
        mockMvc.perform(get("/api/pokemon/autocomplete/attack")
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
        mockMvc.perform(get("/api/pokemon/autocomplete/attack"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle case-insensitive attack search")
    void shouldHandleCaseInsensitiveAttackSearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/attack")
                        .param("query", "FIRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // GET /api/pokemon/autocomplete/ability Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete abilities with query")
    void shouldAutocompleteAbilitiesWithQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/ability")
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
        mockMvc.perform(get("/api/pokemon/autocomplete/ability")
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
        mockMvc.perform(get("/api/pokemon/autocomplete/ability"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle case-insensitive ability search")
    void shouldHandleCaseInsensitiveAbilitySearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/ability")
                        .param("query", "STATIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ============================================================================
    // GET /api/pokemon/autocomplete/set Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete set names with query")
    void shouldAutocompleteSetNamesWithQuery() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/set")
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
        mockMvc.perform(get("/api/pokemon/autocomplete/set")
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
        mockMvc.perform(get("/api/pokemon/autocomplete/set"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle case-insensitive set search")
    void shouldHandleCaseInsensitiveSetSearch() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/set")
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
        mockMvc.perform(get("/api/pokemon/autocomplete/artist")
                        .param("query", "NonExistentArtist12345XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("Should enforce maximum limit for autocomplete")
    void shouldEnforceMaximumLimitForAutocomplete() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/artist")
                        .param("query", "a")
                        .param("limit", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(50))));
        // The maximum limit should be 50
    }

    @Test
    @DisplayName("Should return 400 with RFC 7807 Problem Details for invalid autocomplete field")
    void shouldReturn400ForInvalidField() throws Exception {
        mockMvc.perform(get("/api/pokemon/autocomplete/invalid")
                        .param("query", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/validation"))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid autocomplete field: invalid. Valid fields are: artist, attack, ability, set"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
