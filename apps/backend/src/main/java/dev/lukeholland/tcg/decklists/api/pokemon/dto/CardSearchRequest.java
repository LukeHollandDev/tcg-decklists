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
     * Case-insensitive name search (partial match)
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

    // ===== Phase 2 Filters =====

    /**
     * Filter by attack name (partial match)
     */
    private String attackName;

    /**
     * Minimum attack damage value (inclusive) - uses damage_numeric field
     */
    private Integer attackDamageMin;

    /**
     * Maximum attack damage value (inclusive) - uses damage_numeric field
     */
    private Integer attackDamageMax;

    /**
     * Filter by one or more attack cost types (Fire, Water, Colorless, etc.)
     * By default uses OR logic (ANY match). Set attackCostMatchAll=true for AND logic.
     */
    private List<String> attackCost;

    /**
     * If true, attacks must require ALL specified cost types (AND logic).
     * If false/null, attacks must require ANY specified cost type (OR logic) - default behavior.
     */
    private Boolean attackCostMatchAll;

    /**
     * Filter to only cards with abilities (true) or without abilities (false).
     * If null, no filtering by ability presence.
     */
    private Boolean hasAbility;

    /**
     * Filter by ability name (partial match)
     */
    private String abilityName;

    /**
     * Filter by artist name (exact match)
     */
    private String artist;

    /**
     * Filter by regulation mark (A, B, C, D, E, F, G, H)
     */
    private String regulationMark;

    /**
     * Minimum retreat cost value (inclusive) - uses converted_retreat_cost field
     */
    private Integer retreatCostMin;

    /**
     * Maximum retreat cost value (inclusive) - uses converted_retreat_cost field
     */
    private Integer retreatCostMax;

    /**
     * Filter by one or more formats (Standard, Expanded, Unlimited)
     * By default uses OR logic (ANY match). Set formatsMatchAll=true for AND logic.
     */
    private List<String> formats;

    /**
     * If true, cards must be legal in ALL specified formats (AND logic).
     * If false/null, cards must be legal in ANY specified format (OR logic) - default behavior.
     */
    private Boolean formatsMatchAll;

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

    public String getAttackName() {
        return attackName;
    }

    public void setAttackName(String attackName) {
        this.attackName = attackName;
    }

    public Integer getAttackDamageMin() {
        return attackDamageMin;
    }

    public void setAttackDamageMin(Integer attackDamageMin) {
        this.attackDamageMin = attackDamageMin;
    }

    public Integer getAttackDamageMax() {
        return attackDamageMax;
    }

    public void setAttackDamageMax(Integer attackDamageMax) {
        this.attackDamageMax = attackDamageMax;
    }

    public List<String> getAttackCost() {
        return attackCost;
    }

    public void setAttackCost(List<String> attackCost) {
        this.attackCost = attackCost;
    }

    public Boolean getAttackCostMatchAll() {
        return attackCostMatchAll;
    }

    public void setAttackCostMatchAll(Boolean attackCostMatchAll) {
        this.attackCostMatchAll = attackCostMatchAll;
    }

    public Boolean getHasAbility() {
        return hasAbility;
    }

    public void setHasAbility(Boolean hasAbility) {
        this.hasAbility = hasAbility;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public void setAbilityName(String abilityName) {
        this.abilityName = abilityName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getRegulationMark() {
        return regulationMark;
    }

    public void setRegulationMark(String regulationMark) {
        this.regulationMark = regulationMark;
    }

    public Integer getRetreatCostMin() {
        return retreatCostMin;
    }

    public void setRetreatCostMin(Integer retreatCostMin) {
        this.retreatCostMin = retreatCostMin;
    }

    public Integer getRetreatCostMax() {
        return retreatCostMax;
    }

    public void setRetreatCostMax(Integer retreatCostMax) {
        this.retreatCostMax = retreatCostMax;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    public Boolean getFormatsMatchAll() {
        return formatsMatchAll;
    }

    public void setFormatsMatchAll(Boolean formatsMatchAll) {
        this.formatsMatchAll = formatsMatchAll;
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
                || hpMax != null
                || attackName != null
                || attackDamageMin != null
                || attackDamageMax != null
                || (attackCost != null && !attackCost.isEmpty())
                || hasAbility != null
                || abilityName != null
                || artist != null
                || regulationMark != null
                || retreatCostMin != null
                || retreatCostMax != null
                || (formats != null && !formats.isEmpty());
    }
}
