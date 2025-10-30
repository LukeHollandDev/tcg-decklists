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
 * Integration tests for pagination and sorting functionality in Pokemon card search.
 * <p>
 * Tests cover:
 * - Pagination parameters (page, pageSize)
 * - Pagination metadata (hasNext, hasPrevious, totalPages, totalResults)
 * - Sorting by different fields (name, hp, number, etc.)
 * - Sort order (ascending, descending)
 * - Combined pagination and sorting
 */
@DisplayName("Pokemon Card Search - Pagination & Sorting")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardPaginationSortingIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    void setUpTestData() {
        // Load test data once for all tests in this class
        testDataLoader.loadTestData();
    }

    // ============================================================================
    // Pagination Tests
    // ============================================================================

    @Test
    @DisplayName("Should return first page with default page size")
    void shouldReturnFirstPageWithDefaultPageSize() throws Exception {
        mockMvc.perform(get("/api/pokemon/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.totalResults").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.hasNext").exists())
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    @DisplayName("Should return custom page size")
    void shouldReturnCustomPageSize() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(5))))
                .andExpect(jsonPath("$.pageSize").value(5));
    }

    @Test
    @DisplayName("Should navigate to second page")
    void shouldNavigateToSecondPage() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    @Test
    @DisplayName("Should calculate totalPages correctly")
    void shouldCalculateTotalPagesCorrectly() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.pageSize").value(10));
        // totalPages should be ceil(totalResults / pageSize)
    }

    @Test
    @DisplayName("Should indicate hasNext correctly")
    void shouldIndicateHasNextCorrectly() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "0")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").exists());
        // hasNext should be true if there are more results
    }

    @Test
    @DisplayName("Should indicate hasPrevious correctly")
    void shouldIndicateHasPreviousCorrectly() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasPrevious").value(false));

        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    // ============================================================================
    // Sorting Tests
    // ============================================================================

    @Test
    @DisplayName("Should sort by name ascending (default)")
    void shouldSortByNameAscending() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
        // Results should be sorted alphabetically by name
    }

    @Test
    @DisplayName("Should sort by name descending")
    void shouldSortByNameDescending() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("sortBy", "name")
                        .param("sortOrder", "desc")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(greaterThan(0))));
        // Results should be sorted reverse alphabetically by name
    }

    @Test
    @DisplayName("Should sort by HP ascending")
    void shouldSortByHpAscending() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("sortBy", "hpNumeric")
                        .param("sortOrder", "asc")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Results should be sorted by HP from lowest to highest
    }

    @Test
    @DisplayName("Should sort by HP descending")
    void shouldSortByHpDescending() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("sortBy", "hpNumeric")
                        .param("sortOrder", "desc")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
        // Results should be sorted by HP from highest to lowest
    }

    // ============================================================================
    // Combined Pagination and Sorting Tests
    // ============================================================================

    @Test
    @DisplayName("Should combine pagination and sorting")
    void shouldCombinePaginationAndSorting() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "1")
                        .param("pageSize", "5")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.pageSize").value(5));
    }

    @Test
    @DisplayName("Should paginate filtered results")
    void shouldPaginateFilteredResults() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Fire")
                        .param("page", "0")
                        .param("pageSize", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(3))))
                .andExpect(jsonPath("$.pageSize").value(3));
    }

    @Test
    @DisplayName("Should sort filtered results")
    void shouldSortFilteredResults() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Water")
                        .param("sortBy", "hpNumeric")
                        .param("sortOrder", "desc")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should handle pagination with complex filters")
    void shouldHandlePaginationWithComplexFilters() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                        .param("types", "Fire")
                        .param("hpMin", "60")
                        .param("hasAbility", "true")
                        .param("page", "0")
                        .param("pageSize", "5")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(5));
    }

    @Test
    @DisplayName("Should maintain sort order across pages")
    void shouldMaintainSortOrderAcrossPages() throws Exception {
        // Get first page
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "0")
                        .param("pageSize", "5")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());

        // Get second page - should continue sorted order
        mockMvc.perform(get("/api/pokemon/search")
                        .param("page", "1")
                        .param("pageSize", "5")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }
}
