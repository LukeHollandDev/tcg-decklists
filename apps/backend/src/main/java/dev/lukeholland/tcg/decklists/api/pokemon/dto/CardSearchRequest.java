package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@Schema(description = "Search request with filters for Pokemon cards, including pagination and sorting options")
public record CardSearchRequest(
        // Basic search filters
        String name,
        String supertype,
        List<String> types,
        Boolean typesMatchAll,
        List<String> subtypes,
        Boolean subtypesMatchAll,
        String setId,
        String rarity,

        @Min(value = 0, message = "HP minimum must be 0 or greater")
        Integer hpMin,

        @Min(value = 0, message = "HP maximum must be 0 or greater")
        Integer hpMax,

        // Attack filters
        String attackName,
        String attackText,

        @Min(value = 0, message = "Attack damage minimum must be 0 or greater")
        Integer attackDamageMin,

        @Min(value = 0, message = "Attack damage maximum must be 0 or greater")
        Integer attackDamageMax,

        List<String> attackCost,
        Boolean attackCostMatchAll,

        // Ability filters
        Boolean hasAbility,
        String abilityName,
        String abilityText,

        // Card details
        String artist,
        String regulationMark,

        @Min(value = 0, message = "Retreat cost minimum must be 0 or greater")
        Integer retreatCostMin,

        @Min(value = 0, message = "Retreat cost maximum must be 0 or greater")
        Integer retreatCostMax,

        // Format legality
        List<String> formats,
        Boolean formatsMatchAll,
        List<String> formatsBanned,
        Boolean formatsBannedMatchAll,

        // Boolean filters
        Boolean hasRuleBox,
        Boolean hasWeakness,
        Boolean hasResistance,

        List<String> weaknessType,
        Boolean weaknessTypeMatchAll,
        List<String> resistanceType,
        Boolean resistanceTypeMatchAll,

        // Evolution and rules
        String evolvesFrom,
        String evolvesTo,
        String ruleText,

        // Generic search
        @Schema(description = "Generic search across name, attacks, abilities, rules, and artist")
        String q,
        Boolean excludeName,
        Boolean excludeAttacks,
        Boolean excludeAbilities,
        Boolean excludeRules,
        Boolean excludeArtist,

        // Pagination and sorting
        @Min(value = 0, message = "Page number must be 0 or greater")
        Integer page,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer pageSize,

        String sortBy,

        @Pattern(regexp = "asc|desc", message = "Sort order must be 'asc' or 'desc'")
        String sortOrder
) {
    /**
     * Compact constructor to apply default values.
     * This is called automatically when creating the record.
     */
    public CardSearchRequest {
        // Apply defaults for pagination and sorting
        page = (page == null || page < 0) ? 0 : page;
        pageSize = (pageSize == null) ? 20 : Math.min(Math.max(1, pageSize), 100);
        sortBy = (sortBy == null || sortBy.isBlank()) ? "name" : sortBy;
        sortOrder = (sortOrder == null || sortOrder.isBlank()) ? "asc" : sortOrder.toLowerCase();

        // Apply defaults for boolean exclude flags
        excludeName = (excludeName != null) ? excludeName : false;
        excludeAttacks = (excludeAttacks != null) ? excludeAttacks : false;
        excludeAbilities = (excludeAbilities != null) ? excludeAbilities : false;
        excludeRules = (excludeRules != null) ? excludeRules : false;
        excludeArtist = (excludeArtist != null) ? excludeArtist : false;
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
