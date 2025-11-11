package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Search request with filters for Pokemon cards, including pagination and sorting options")
public class CardSearchRequest {

    private String name;
    private String supertype;
    private List<String> types;
    private Boolean typesMatchAll;
    private List<String> subtypes;
    private Boolean subtypesMatchAll;
    private String setId;
    private String rarity;
    private Integer hpMin;
    private Integer hpMax;

    private String attackName;
    private String attackText;
    private Integer attackDamageMin;
    private Integer attackDamageMax;
    private List<String> attackCost;
    private Boolean attackCostMatchAll;

    private Boolean hasAbility;
    private String abilityName;
    private String abilityText;

    private String artist;
    private String regulationMark;
    private Integer retreatCostMin;
    private Integer retreatCostMax;

    private List<String> formats;
    private Boolean formatsMatchAll;
    private List<String> formatsBanned;
    private Boolean formatsBannedMatchAll;

    private Boolean hasRuleBox;
    private Boolean hasWeakness;
    private Boolean hasResistance;
    private List<String> weaknessType;
    private Boolean weaknessTypeMatchAll;
    private List<String> resistanceType;
    private Boolean resistanceTypeMatchAll;

    private String evolvesFrom;
    private String evolvesTo;
    private String ruleText;

    @Schema(description = "Generic search across name, attacks, abilities, rules, and artist")
    private String q;
    private Boolean excludeName = false;
    private Boolean excludeAttacks = false;
    private Boolean excludeAbilities = false;
    private Boolean excludeRules = false;
    private Boolean excludeArtist = false;

    private Integer page = 0;
    private Integer pageSize = 20;
    private String sortBy = "name";
    private String sortOrder = "asc";

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

    public String getAttackText() {
        return attackText;
    }

    public void setAttackText(String attackText) {
        this.attackText = attackText;
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

    public String getAbilityText() {
        return abilityText;
    }

    public void setAbilityText(String abilityText) {
        this.abilityText = abilityText;
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

    public List<String> getFormatsBanned() {
        return formatsBanned;
    }

    public void setFormatsBanned(List<String> formatsBanned) {
        this.formatsBanned = formatsBanned;
    }

    public Boolean getFormatsBannedMatchAll() {
        return formatsBannedMatchAll;
    }

    public void setFormatsBannedMatchAll(Boolean formatsBannedMatchAll) {
        this.formatsBannedMatchAll = formatsBannedMatchAll;
    }

    public Boolean getHasRuleBox() {
        return hasRuleBox;
    }

    public void setHasRuleBox(Boolean hasRuleBox) {
        this.hasRuleBox = hasRuleBox;
    }

    public Boolean getHasWeakness() {
        return hasWeakness;
    }

    public void setHasWeakness(Boolean hasWeakness) {
        this.hasWeakness = hasWeakness;
    }

    public Boolean getHasResistance() {
        return hasResistance;
    }

    public void setHasResistance(Boolean hasResistance) {
        this.hasResistance = hasResistance;
    }

    public List<String> getWeaknessType() {
        return weaknessType;
    }

    public void setWeaknessType(List<String> weaknessType) {
        this.weaknessType = weaknessType;
    }

    public Boolean getWeaknessTypeMatchAll() {
        return weaknessTypeMatchAll;
    }

    public void setWeaknessTypeMatchAll(Boolean weaknessTypeMatchAll) {
        this.weaknessTypeMatchAll = weaknessTypeMatchAll;
    }

    public List<String> getResistanceType() {
        return resistanceType;
    }

    public void setResistanceType(List<String> resistanceType) {
        this.resistanceType = resistanceType;
    }

    public Boolean getResistanceTypeMatchAll() {
        return resistanceTypeMatchAll;
    }

    public void setResistanceTypeMatchAll(Boolean resistanceTypeMatchAll) {
        this.resistanceTypeMatchAll = resistanceTypeMatchAll;
    }

    public String getEvolvesFrom() {
        return evolvesFrom;
    }

    public void setEvolvesFrom(String evolvesFrom) {
        this.evolvesFrom = evolvesFrom;
    }

    public String getEvolvesTo() {
        return evolvesTo;
    }

    public void setEvolvesTo(String evolvesTo) {
        this.evolvesTo = evolvesTo;
    }

    public String getRuleText() {
        return ruleText;
    }

    public void setRuleText(String ruleText) {
        this.ruleText = ruleText;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public Boolean getExcludeName() {
        return excludeName != null ? excludeName : false;
    }

    public void setExcludeName(Boolean excludeName) {
        this.excludeName = excludeName;
    }

    public Boolean getExcludeAttacks() {
        return excludeAttacks != null ? excludeAttacks : false;
    }

    public void setExcludeAttacks(Boolean excludeAttacks) {
        this.excludeAttacks = excludeAttacks;
    }

    public Boolean getExcludeAbilities() {
        return excludeAbilities != null ? excludeAbilities : false;
    }

    public void setExcludeAbilities(Boolean excludeAbilities) {
        this.excludeAbilities = excludeAbilities;
    }

    public Boolean getExcludeRules() {
        return excludeRules != null ? excludeRules : false;
    }

    public void setExcludeRules(Boolean excludeRules) {
        this.excludeRules = excludeRules;
    }

    public Boolean getExcludeArtist() {
        return excludeArtist != null ? excludeArtist : false;
    }

    public void setExcludeArtist(Boolean excludeArtist) {
        this.excludeArtist = excludeArtist;
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
                || attackText != null
                || attackDamageMin != null
                || attackDamageMax != null
                || (attackCost != null && !attackCost.isEmpty())
                || hasAbility != null
                || abilityName != null
                || abilityText != null
                || artist != null
                || regulationMark != null
                || retreatCostMin != null
                || retreatCostMax != null
                || (formats != null && !formats.isEmpty())
                || (formatsBanned != null && !formatsBanned.isEmpty())
                || hasRuleBox != null
                || hasWeakness != null
                || hasResistance != null
                || (weaknessType != null && !weaknessType.isEmpty())
                || (resistanceType != null && !resistanceType.isEmpty())
                || evolvesFrom != null
                || evolvesTo != null
                || ruleText != null
                || q != null;
    }
}
