package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response DTO for autocomplete endpoints.
 * Returns a list of matching strings for use in search-enabled dropdowns.
 * <p>
 * Example usage: /api/pokemon/artists?query=ken&limit=10
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AutocompleteResponse {

    /**
     * List of matching results (e.g., artist names, attack names, etc.)
     */
    private List<String> results;

    /**
     * Number of results returned
     */
    private int count;

    /**
     * Query string that was used for the search
     */
    private String query;

    /**
     * Maximum limit that was applied
     */
    private Integer limit;

    // ===== Constructors =====

    public AutocompleteResponse() {
    }

    public AutocompleteResponse(List<String> results, String query, Integer limit) {
        this.results = results;
        this.count = results != null ? results.size() : 0;
        this.query = query;
        this.limit = limit;
    }

    // ===== Getters and Setters =====

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
        this.count = results != null ? results.size() : 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
