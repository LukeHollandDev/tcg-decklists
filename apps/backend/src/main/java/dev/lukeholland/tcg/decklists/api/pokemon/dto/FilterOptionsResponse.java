package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Response DTO for the /features endpoint.
 * Returns filter metadata and available static values for building dynamic filter UI.
 * <p>
 * Structure:
 * - type: Card game type (e.g., "pokemon")
 * - static: Static data containing all available values for enum/multiselect filters
 * - filters: Metadata describing each filter (type, operator, parameters, etc.)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FilterOptionsResponse {

    /**
     * Card game type (e.g., "pokemon")
     */
    private String type = "pokemon";

    /**
     * Static filter data (supertypes, types, subtypes, sets, rarities, formats, regulation marks)
     */
    @JsonProperty("static")
    private StaticFilterData staticData;

    /**
     * Filter metadata describing each available filter
     * Key: filter name (e.g., "name", "supertype", "types", "hp")
     * Value: FilterMetadata describing the filter
     */
    private Map<String, FilterMetadata> filters;

    // ===== Getters and Setters =====

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public StaticFilterData getStaticData() {
        return staticData;
    }

    public void setStaticData(StaticFilterData staticData) {
        this.staticData = staticData;
    }

    public Map<String, FilterMetadata> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, FilterMetadata> filters) {
        this.filters = filters;
    }
}

