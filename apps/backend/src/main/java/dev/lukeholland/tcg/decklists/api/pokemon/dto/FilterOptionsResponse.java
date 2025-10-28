package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response DTO for the /features endpoint.
 * Returns all available filter values that can be used in search queries.
 * This helps frontend applications build dynamic filter UI components.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FilterOptionsResponse {

    /**
     * Card game type (e.g., "pokemon")
     */
    private String type = "pokemon";

    /**
     * Available supertypes (Pokemon, Trainer, Energy)
     */
    private List<String> supertypes;

    /**
     * Available Pokemon types (Grass, Fire, Water, Psychic, etc.)
     */
    private List<String> types;

    /**
     * Available subtypes (Basic, Stage 1, Stage 2, ex, V, GX, Item, Supporter, etc.)
     */
    private List<String> subtypes;

    /**
     * Available set identifiers (base1, swsh8, etc.)
     */
    private List<String> sets;

    /**
     * Available rarity values (Common, Uncommon, Rare, Rare Holo, etc.)
     */
    private List<String> rarities;

    /**
     * Available format names for legality filtering (Standard, Expanded, Unlimited)
     */
    private List<String> formats;

    /**
     * Available regulation marks (A, B, C, D, E, F, G, H, etc.)
     */
    private List<String> regulationMarks;

    // ===== Getters and Setters =====

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getSupertypes() {
        return supertypes;
    }

    public void setSupertypes(List<String> supertypes) {
        this.supertypes = supertypes;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getSubtypes() {
        return subtypes;
    }

    public void setSubtypes(List<String> subtypes) {
        this.subtypes = subtypes;
    }

    public List<String> getSets() {
        return sets;
    }

    public void setSets(List<String> sets) {
        this.sets = sets;
    }

    public List<String> getRarities() {
        return rarities;
    }

    public void setRarities(List<String> rarities) {
        this.rarities = rarities;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    public List<String> getRegulationMarks() {
        return regulationMarks;
    }

    public void setRegulationMarks(List<String> regulationMarks) {
        this.regulationMarks = regulationMarks;
    }
}
