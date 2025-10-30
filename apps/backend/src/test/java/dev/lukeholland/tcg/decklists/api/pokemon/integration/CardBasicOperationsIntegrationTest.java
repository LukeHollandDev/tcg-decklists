package dev.lukeholland.tcg.decklists.api.pokemon.integration;

import dev.lukeholland.tcg.decklists.api.pokemon.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for basic Pokemon card operations (GET /api/pokemon/{id}).
 * <p>
 * Tests cover:
 * - Successful card retrieval
 * - Error handling (404, 405)
 * - Response format validation
 * - Cards with various relationships (attacks, abilities, weaknesses, etc.)
 * - Accent-insensitive handling
 * - Different card types (Pokemon, Trainer, Energy)
 */
@DisplayName("Pokemon Card - Basic Operations")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardBasicOperationsIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Success Cases
    // ============================================================================

    @Test
    @DisplayName("Should successfully retrieve card by valid ID")
    void shouldRetrieveCardByValidId() throws Exception {
        mockMvc.perform(get("/api/pokemon/base1-46"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value("base1-46"))
                .andExpect(jsonPath("$.name").value("Charmander"))
                .andExpect(jsonPath("$.supertype").value("Pokémon"))
                .andExpect(jsonPath("$.hp").value("50"))
                .andExpect(jsonPath("$.hpNumeric").value(50));
    }

    @Test
    @DisplayName("Should retrieve card with all relationships (attacks, abilities, weaknesses, resistances)")
    void shouldRetrieveCardWithAllRelationships() throws Exception {
        mockMvc.perform(get("/api/pokemon/base1-4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("base1-4"))
                .andExpect(jsonPath("$.name").value("Charizard"))

                // Types
                .andExpect(jsonPath("$.types").isArray())
                .andExpect(jsonPath("$.types[0]").value("Fire"))

                // Subtypes
                .andExpect(jsonPath("$.subtypes").isArray())
                .andExpect(jsonPath("$.subtypes[0]").value("Stage 2"))

                // Attacks
                .andExpect(jsonPath("$.attacks").isArray())
                .andExpect(jsonPath("$.attacks", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.attacks[0].name").exists())
                .andExpect(jsonPath("$.attacks[0].damage").exists())

                // Ability
                .andExpect(jsonPath("$.abilities").isArray())
                .andExpect(jsonPath("$.abilities", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.abilities[0].name").value("Energy Burn"))

                // Weaknesses
                .andExpect(jsonPath("$.weaknesses").isArray())
                .andExpect(jsonPath("$.weaknesses[0].type").value("Water"))
                .andExpect(jsonPath("$.weaknesses[0].value").value("×2"))

                // Resistances
                .andExpect(jsonPath("$.resistances").isArray())
                .andExpect(jsonPath("$.resistances[0].type").value("Fighting"))
                .andExpect(jsonPath("$.resistances[0].value").value("-30"))

                // Retreat cost
                .andExpect(jsonPath("$.retreatCost").isArray())
                .andExpect(jsonPath("$.convertedRetreatCost").value(3))

                // Set information
                .andExpect(jsonPath("$.setId").value("base1"))
                .andExpect(jsonPath("$.setName").value("Base Set"))

                // Artist and rarity
                .andExpect(jsonPath("$.artistName").value("Mitsuhiro Arita"))
                .andExpect(jsonPath("$.rarityName").value("Rare Holo"))

                // Flavor text
                .andExpect(jsonPath("$.flavorText").exists());
    }

    @Test
    @DisplayName("Should retrieve card with accent characters (Flabébé)")
    void shouldRetrieveCardWithAccentCharacters() throws Exception {
        // Find the Flabébé card ID from test data
        // This tests that the API correctly handles and returns accent characters
        MvcResult result = mockMvc.perform(get("/api/pokemon/search")
                        .param("name", "Flabebe"))  // Search without accent
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andReturn();

        // Extract the ID from search results and fetch the card
        String content = result.getResponse().getContentAsString();
        String cardId = objectMapper.readTree(content)
                .get("results")
                .get(0)
                .get("id")
                .asText();

        mockMvc.perform(get("/api/pokemon/" + cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Flabébé"));  // Should return with accent
    }

    @Test
    @DisplayName("Should retrieve card with evolution data")
    void shouldRetrieveCardWithEvolutionData() throws Exception {
        // Charmeleon evolves from Charmander and to Charizard
        mockMvc.perform(get("/api/pokemon/base1-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Charmeleon"))
                .andExpect(jsonPath("$.evolvesFrom").exists())
                .andExpect(jsonPath("$.evolvesTo").exists());
    }

    @Test
    @DisplayName("Should retrieve Trainer card (Professor Oak)")
    void shouldRetrieveTrainerCard() throws Exception {
        // Find Professor Oak in test data
        MvcResult result = mockMvc.perform(get("/api/pokemon/search")
                        .param("supertype", "Trainer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        String cardId = objectMapper.readTree(content)
                .get("results")
                .get(0)
                .get("id")
                .asText();

        mockMvc.perform(get("/api/pokemon/" + cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supertype").value("Trainer"))
                .andExpect(jsonPath("$.subtypes").isArray())
                .andExpect(jsonPath("$.hp").doesNotExist())  // Trainer cards don't have HP
                .andExpect(jsonPath("$.attacks").doesNotExist());  // Trainer cards don't have attacks
    }

    @Test
    @DisplayName("Should retrieve Energy card (Fire Energy)")
    void shouldRetrieveEnergyCard() throws Exception {
        // Find Fire Energy in test data
        MvcResult result = mockMvc.perform(get("/api/pokemon/search")
                        .param("supertype", "Energy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        String cardId = objectMapper.readTree(content)
                .get("results")
                .get(0)
                .get("id")
                .asText();

        mockMvc.perform(get("/api/pokemon/" + cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supertype").value("Energy"))
                .andExpect(jsonPath("$.hp").doesNotExist())  // Energy cards don't have HP
                .andExpect(jsonPath("$.attacks").doesNotExist());  // Energy cards don't have attacks
    }

    @Test
    @DisplayName("Should retrieve card with rule box (Charizard ex)")
    void shouldRetrieveCardWithRuleBox() throws Exception {
        // Find Charizard ex in test data
        MvcResult result = mockMvc.perform(get("/api/pokemon/search")
                        .param("subtypes", "ex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        String cardId = objectMapper.readTree(content)
                .get("results")
                .get(0)
                .get("id")
                .asText();

        mockMvc.perform(get("/api/pokemon/" + cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rules").isArray())
                .andExpect(jsonPath("$.rules", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.rules[0]").isString());
    }

    @Test
    @DisplayName("Should retrieve card with format legality information")
    void shouldRetrieveCardWithFormatLegality() throws Exception {
        mockMvc.perform(get("/api/pokemon/base1-46"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legalities").exists())
                .andExpect(jsonPath("$.legalities").isMap())
                // Charmander should have legality entries (unlimited at minimum per test data)
                .andExpect(jsonPath("$.legalities").isNotEmpty());
    }

    // ============================================================================
    // Error Cases
    // ============================================================================

    @Test
    @DisplayName("Should return 404 for non-existent card ID")
    void shouldReturn404ForNonExistentCard() throws Exception {
        mockMvc.perform(get("/api/pokemon/invalid-card-id-12345"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 405 for unsupported HTTP method (POST)")
    void shouldReturn405ForUnsupportedMethod() throws Exception {
        mockMvc.perform(post("/api/pokemon/base1-46"))
                .andExpect(status().isMethodNotAllowed());
    }
}
