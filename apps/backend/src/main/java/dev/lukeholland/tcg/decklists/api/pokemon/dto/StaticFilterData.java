package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Static filter data containing all available values for enum/multiselect filters.
 * This data is used by the frontend to populate dropdown options.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record StaticFilterData(
        List<String> supertypes,
        List<String> types,
        List<String> subtypes,
        List<String> sets,
        List<String> rarities,
        List<String> formats,
        List<String> regulationMarks
) {
}
