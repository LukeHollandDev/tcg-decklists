package dev.lukeholland.tcg.decklists.api.pokemon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Pokemon Card Controller search functionality.
 * These tests require the database to be running with test data loaded.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test: Search without any filters should return paginated results
     */
    @Test
    void testSearchWithoutFilters() throws Exception {
        mockMvc.perform(get("/api/pokemon/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.totalResults").isNumber())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.hasNext").isBoolean())
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    /**
     * Test: Search by card name (partial match)
     */
    @Test
    void testSearchByName() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("name", "Pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[*].name", everyItem(containsStringIgnoringCase("pikachu"))));
    }

    /**
     * Test: Search by supertype
     */
    @Test
    void testSearchBySupertype() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("supertype", "Pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[*].supertype", everyItem(equalToIgnoringCase("Pokemon"))));
    }

    /**
     * Test: Search by type (should match cards with the specified type)
     */
    @Test
    void testSearchByType() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Results should contain Fire type cards
    }

    /**
     * Test: Search by multiple types
     */
    @Test
    void testSearchByMultipleTypes() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Fire")
                        .param("types", "Water"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Results should contain cards with Fire OR Water types
    }

    /**
     * Test: Search by subtype
     */
    @Test
    void testSearchBySubtype() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("subtypes", "Basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    /**
     * Test: Search by set identifier
     */
    @Test
    void testSearchBySetId() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("setId", "base1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[*].setId", everyItem(equalToIgnoringCase("base1"))));
    }

    /**
     * Test: Search by rarity
     */
    @Test
    void testSearchByRarity() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("rarity", "Rare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[*].rarityName", everyItem(containsStringIgnoringCase("rare"))));
    }

    /**
     * Test: Search by HP range (minimum only)
     */
    @Test
    void testSearchByHpMin() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hpMin", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[*].hpNumeric", everyItem(greaterThanOrEqualTo(100))));
    }

    /**
     * Test: Search by HP range (maximum only)
     */
    @Test
    void testSearchByHpMax() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hpMax", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[*].hpNumeric", everyItem(lessThanOrEqualTo(50))));
    }

    /**
     * Test: Search by HP range (both min and max)
     */
    @Test
    void testSearchByHpRange() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("hpMin", "50")
                        .param("hpMax", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[*].hpNumeric", everyItem(allOf(
                        greaterThanOrEqualTo(50),
                        lessThanOrEqualTo(100)
                ))));
    }

    /**
     * Test: Combined filters (name + type + HP range)
     */
    @Test
    void testSearchWithCombinedFilters() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("name", "Charizard")
                        .param("types", "Fire")
                        .param("hpMin", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[*].name", everyItem(containsStringIgnoringCase("charizard"))))
                .andExpect(jsonPath("$.results[*].hpNumeric", everyItem(greaterThanOrEqualTo(100))));
    }

    /**
     * Test: Pagination - first page
     */
    @Test
    void testPaginationFirstPage() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(10))))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    /**
     * Test: Pagination - second page
     */
    @Test
    void testPaginationSecondPage() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    /**
     * Test: Page size limit enforcement (max 100)
     */
    @Test
    void testPageSizeLimitEnforcement() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("pageSize", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(100)); // Should be capped at 100
    }

    /**
     * Test: Sorting by name ascending (default)
     */
    @Test
    void testSortByNameAsc() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Results should be sorted alphabetically
    }

    /**
     * Test: Sorting by HP descending
     */
    @Test
    void testSortByHpDesc() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("sortBy", "hpNumeric")
                        .param("sortOrder", "desc")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Results should be sorted by HP descending
    }

    /**
     * Test: Get filter options (features endpoint)
     */
    @Test
    void testGetFeatures() throws Exception {
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

    /**
     * Test: Features endpoint returns non-empty lists (assuming data is loaded)
     */
    @Test
    void testFeaturesContainData() throws Exception {
        mockMvc.perform(get("/api/pokemon/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types", not(empty())))
                .andExpect(jsonPath("$.subtypes", not(empty())));
    }
}
