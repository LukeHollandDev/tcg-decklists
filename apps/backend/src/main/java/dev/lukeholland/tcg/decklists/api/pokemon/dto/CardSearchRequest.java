package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import java.util.List;

/**
 * Request DTO for searching Pokemon cards.
 * Captures filter parameters, pagination, and sorting options.
 * <p>
 * Phase 1 includes: name search, supertype, types, subtypes, set, rarity, HP range
 * Future phases will add: attack filters, ability filters, format legality, etc.
 */
public class CardSearchRequest {

    // ===== Phase 1 Filters =====

    /**
     * Case-insensitive name search (partial match using ILIKE)
     */
    private String name;

    /**
     * Exact match on supertype (Pokemon, Trainer, Energy)
     */
    private String supertype;

    /**
     * Filter by one or more Pokemon types (Fire, Water, etc.)
     * By default uses OR logic (ANY match). Set typesMatchAll=true for AND logic.
     */
    private List<String> types;

    /**
     * If true, cards must have ALL specified types (AND logic).
     * If false/null, cards must have ANY specified type (OR logic) - default behavior.
     */
    private Boolean typesMatchAll;

    /**
     * Filter by one or more subtypes (ex, V, Basic, Stage 1, etc.)
     * By default uses OR logic (ANY match). Set subtypesMatchAll=true for AND logic.
     */
    private List<String> subtypes;

    /**
     * If true, cards must have ALL specified subtypes (AND logic).
     * If false/null, cards must have ANY specified subtype (OR logic) - default behavior.
     */
    private Boolean subtypesMatchAll;

    /**
     * Filter by set identifier (e.g., "base1", "swsh8")
     */
    private String setId;

    /**
     * Filter by rarity name
     */
    private String rarity;

    /**
     * Minimum HP value (inclusive) - uses hp_numeric field
     */
    private Integer hpMin;

    /**
     * Maximum HP value (inclusive) - uses hp_numeric field
     */
    private Integer hpMax;

    // ===== Pagination & Sorting =====

    /**
     * Page number (0-indexed, default: 0)
     */
    private Integer page = 0;

    /**
     * Number of results per page (default: 20, max: 100)
     */
    private Integer pageSize = 20;

    /**
     * Field to sort by (e.g., "name", "hp", "number") - default: "name"
     */
    private String sortBy = "name";

    /**
     * Sort order: "asc" or "desc" (default: "asc")
     */
    private String sortOrder = "asc";

    // ===== Getters and Setters =====

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupertype() {
        return supertype;
    }

    public void setSupertype(String supertype) {
        this.supertype = supertype;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public Boolean getTypesMatchAll() {
        return typesMatchAll;
    }

    public void setTypesMatchAll(Boolean typesMatchAll) {
        this.typesMatchAll = typesMatchAll;
    }

    public List<String> getSubtypes() {
        return subtypes;
    }

    public void setSubtypes(List<String> subtypes) {
        this.subtypes = subtypes;
    }

    public Boolean getSubtypesMatchAll() {
        return subtypesMatchAll;
    }

    public void setSubtypesMatchAll(Boolean subtypesMatchAll) {
        this.subtypesMatchAll = subtypesMatchAll;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public Integer getHpMin() {
        return hpMin;
    }

    public void setHpMin(Integer hpMin) {
        this.hpMin = hpMin;
    }

    public Integer getHpMax() {
        return hpMax;
    }

    public void setHpMax(Integer hpMax) {
        this.hpMax = hpMax;
    }

    public Integer getPage() {
        // Enforce minimum page of 0 (0-indexed pagination)
        if (page == null) {
            return 0; // Default to first page
        }
        if (page < 0) {
            return 0; // Minimum (can't have negative pages)
        }
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        // Enforce min page size of 1 and max page size of 100
        if (pageSize == null) {
            return 20; // Default
        }
        if (pageSize < 1) {
            return 1; // Minimum
        }
        if (pageSize > 100) {
            return 100; // Maximum
        }
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "name";
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder != null ? sortOrder : "asc";
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Check if any filters are applied
     *
     * @return true if at least one filter parameter is set
     */
    public boolean hasFilters() {
        return name != null
                || supertype != null
                || (types != null && !types.isEmpty())
                || (subtypes != null && !subtypes.isEmpty())
                || setId != null
                || rarity != null
                || hpMin != null
                || hpMax != null;
    }
}
