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
public record AutocompleteResponse(
        List<String> results,
        int count,
        String query,
        Integer limit
) {
    public AutocompleteResponse(List<String> results, String query, Integer limit) {
        this(results, results != null ? results.size() : 0, query, limit);
    }
}
