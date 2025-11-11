package dev.lukeholland.tcg.decklists.api.pokemon.integration;

import dev.lukeholland.tcg.decklists.api.pokemon.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for decklist operations.
 * <p>
 * Tests cover:
 * - Creating a decklist with valid cards
 * - Retrieving a decklist by ID
 * - Card quantity aggregation (duplicates)
 * - Validation of card IDs
 * - Error handling (404, 400)
 * - createdAt timestamp
 */
@DisplayName("Decklist Operations")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DecklistIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Success Cases
    // ============================================================================

    @Test
    @DisplayName("Should successfully create a decklist with valid cards")
    void shouldCreateDecklistWithValidCards() throws Exception {
        String requestBody = """
                {
                    "name": "My Fire Deck",
                    "type": "pokemon",
                    "cards": ["base1-4", "base1-46"]
                }
                """;

        mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Should create decklist and retrieve it by ID")
    void shouldCreateAndRetrieveDecklistById() throws Exception {
        // Create decklist
        String requestBody = """
                {
                    "name": "Test Deck",
                    "type": "pokemon",
                    "cards": ["base1-4", "base1-46", "base1-24"]
                }
                """;

        String createResponse = mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID from response
        int decklistId = objectMapper.readTree(createResponse).get("id").asInt();

        // Retrieve the decklist
        mockMvc.perform(get("/api/decklist/" + decklistId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(decklistId))
                .andExpect(jsonPath("$.name").value("Test Deck"))
                .andExpect(jsonPath("$.type").value("pokemon"))
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards", hasSize(3)))
                .andExpect(jsonPath("$.cards", containsInAnyOrder("base1-4", "base1-46", "base1-24")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.createdAt").isString());
    }

    @Test
    @DisplayName("Should aggregate duplicate cards into quantities")
    void shouldAggregateDuplicateCardsIntoQuantities() throws Exception {
        // Create decklist with duplicate cards
        String requestBody = """
                {
                    "name": "Deck with Duplicates",
                    "type": "pokemon",
                    "cards": ["base1-4", "base1-4", "base1-4", "base1-46", "base1-46"]
                }
                """;

        String createResponse = mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int decklistId = objectMapper.readTree(createResponse).get("id").asInt();

        // Retrieve the decklist and verify cards are expanded with quantities
        mockMvc.perform(get("/api/decklist/" + decklistId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards", hasSize(5)))  // 3 + 2 = 5 total cards
                .andExpect(jsonPath("$.cards[?(@ == 'base1-4')]", hasSize(3)))  // 3 copies of Charizard
                .andExpect(jsonPath("$.cards[?(@ == 'base1-46')]", hasSize(2)));  // 2 copies of Charmander
    }

    @Test
    @DisplayName("Should handle decklist with single card")
    void shouldHandleDecklistWithSingleCard() throws Exception {
        String requestBody = """
                {
                    "name": "Single Card Deck",
                    "type": "pokemon",
                    "cards": ["base1-4"]
                }
                """;

        String createResponse = mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int decklistId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(get("/api/decklist/" + decklistId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards", hasSize(1)))
                .andExpect(jsonPath("$.cards[0]").value("base1-4"));
    }

    @Test
    @DisplayName("Should handle decklist with many cards")
    void shouldHandleDecklistWithManyCards() throws Exception {
        // Create a deck with 60 cards (typical deck size)
        StringBuilder cardsJson = new StringBuilder("[");
        for (int i = 0; i < 60; i++) {
            if (i > 0) cardsJson.append(",");
            cardsJson.append("\"base1-4\"");  // 60 copies of the same card for simplicity
        }
        cardsJson.append("]");

        String requestBody = String.format("""
                {
                    "name": "Large Deck",
                    "type": "pokemon",
                    "cards": %s
                }
                """, cardsJson.toString());

        String createResponse = mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int decklistId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(get("/api/decklist/" + decklistId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards", hasSize(60)));
    }

    // ============================================================================
    // Validation and Error Cases
    // ============================================================================

    @Test
    @DisplayName("Should return 400 for invalid card ID")
    void shouldReturn400ForInvalidCardId() throws Exception {
        String requestBody = """
                {
                    "name": "Invalid Deck",
                    "type": "pokemon",
                    "cards": ["invalid-card-id"]
                }
                """;

        mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value(containsString("Invalid card IDs")))
                .andExpect(jsonPath("$.error").value(containsString("invalid-card-id")));
    }

    @Test
    @DisplayName("Should return 400 for multiple invalid card IDs")
    void shouldReturn400ForMultipleInvalidCardIds() throws Exception {
        String requestBody = """
                {
                    "name": "Invalid Deck",
                    "type": "pokemon",
                    "cards": ["invalid-1", "base1-4", "invalid-2"]
                }
                """;

        mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value(containsString("Invalid card IDs")));
    }

    @Test
    @DisplayName("Should return 400 for missing name")
    void shouldReturn400ForMissingName() throws Exception {
        String requestBody = """
                {
                    "type": "pokemon",
                    "cards": ["base1-4"]
                }
                """;

        mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value(containsString("name is required")));
    }

    @Test
    @DisplayName("Should return 400 for empty name")
    void shouldReturn400ForEmptyName() throws Exception {
        String requestBody = """
                {
                    "name": "",
                    "type": "pokemon",
                    "cards": ["base1-4"]
                }
                """;

        mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value(containsString("name is required")));
    }

    @Test
    @DisplayName("Should return 400 for missing type")
    void shouldReturn400ForMissingType() throws Exception {
        String requestBody = """
                {
                    "name": "Test Deck",
                    "cards": ["base1-4"]
                }
                """;

        mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for invalid card game type")
    void shouldReturn400ForInvalidCardGameType() throws Exception {
        String requestBody = """
                {
                    "name": "Test Deck",
                    "type": "invalid-game",
                    "cards": ["base1-4"]
                }
                """;

        mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing cards")
    void shouldReturn400ForMissingCards() throws Exception {
        String requestBody = """
                {
                    "name": "Test Deck",
                    "type": "pokemon"
                }
                """;

        mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value(containsString("at least one card")));
    }

    @Test
    @DisplayName("Should return 400 for empty cards array")
    void shouldReturn400ForEmptyCardsArray() throws Exception {
        String requestBody = """
                {
                    "name": "Test Deck",
                    "type": "pokemon",
                    "cards": []
                }
                """;

        mockMvc.perform(post("/api/decklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value(containsString("at least one card")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent decklist ID")
    void shouldReturn404ForNonExistentDecklistId() throws Exception {
        mockMvc.perform(get("/api/decklist/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 for negative decklist ID")
    void shouldReturn404ForNegativeDecklistId() throws Exception {
        mockMvc.perform(get("/api/decklist/-1"))
                .andExpect(status().isNotFound());
    }
}
