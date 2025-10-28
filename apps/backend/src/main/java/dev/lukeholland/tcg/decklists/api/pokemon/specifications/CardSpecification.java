package dev.lukeholland.tcg.decklists.api.pokemon.specifications;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.*;
import dev.lukeholland.tcg.decklists.api.pokemon.enums.LegalityStatus;
import dev.lukeholland.tcg.decklists.api.pokemon.util.StringNormalizer;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * JPA Specifications for building dynamic Card queries.
 * Each static method returns a Specification that can be composed with others using and(), or(), etc.
 * <p>
 * Supports accent-insensitive searching using StringNormalizer utility.
 * <p>
 * Example usage:
 * <pre>
 * Specification<Card> spec = Specification.where(CardSpecification.hasNameContaining("Pikachu"))
 *     .and(CardSpecification.hasTypes(List.of("Electric")))
 *     .and(CardSpecification.hpBetween(50, 100));
 * </pre>
 */
public class CardSpecification {

    /**
     * Normalize a database text field to remove accents and convert to lowercase.
     * Uses PostgreSQL's translate() function with explicit character mappings.
     *
     * @param expression      The field expression to normalize
     * @param criteriaBuilder The criteria builder
     * @return Expression representing the normalized field
     */
    private static Expression<String> normalizeField(Expression<String> expression, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        // Common accented characters found in Pokémon card names
        String from = "áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ";
        String to = "aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ";

        // Apply translate() to map accented characters to their base forms
        Expression<String> translated = criteriaBuilder.function(
                "translate",
                String.class,
                expression,
                criteriaBuilder.literal(from),
                criteriaBuilder.literal(to)
        );

        // Then convert to lowercase
        return criteriaBuilder.lower(translated);
    }

    /**
     * Filter by card name (case-insensitive, accent-insensitive partial match).
     * Supports searches like "pokemon" matching "Pokémon", "flabebe" matching "Flabébé", etc.
     *
     * @param name The name to search for (will match cards containing this string)
     * @return Specification that matches cards with names containing the search term
     */
    public static Specification<Card> hasNameContaining(String name) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(name)) {
                return criteriaBuilder.conjunction(); // No filter (always true)
            }

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(name.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(root.get("name"), criteriaBuilder);

            return criteriaBuilder.like(normalizedField, "%" + normalizedSearch + "%");
        };
    }

    /**
     * Filter by supertype (exact match, case-insensitive, accent-insensitive).
     *
     * @param supertype The supertype to match (Pokemon, Trainer, Energy)
     * @return Specification that matches cards with the specified supertype
     */
    public static Specification<Card> hasSupertype(String supertype) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(supertype)) {
                return criteriaBuilder.conjunction();
            }

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(supertype.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(root.get("supertype"), criteriaBuilder);

            return criteriaBuilder.equal(normalizedField, normalizedSearch);
        };
    }

    /**
     * Filter by one or more Pokemon types (Fire, Water, etc.).
     * Supports both OR logic (ANY match) and AND logic (ALL match).
     * Supports accent-insensitive matching.
     *
     * @param typeNames List of type names to match
     * @param matchAll  If true, cards must have ALL types (AND logic). If false/null, ANY type (OR logic).
     * @return Specification that matches cards with the specified types
     */
    public static Specification<Card> hasTypes(List<String> typeNames, Boolean matchAll) {
        return (root, query, criteriaBuilder) -> {
            if (typeNames == null || typeNames.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Normalize search terms
            List<String> normalizedTypes = typeNames.stream()
                    .map(StringNormalizer::normalize)
                    .toList();

            // OR logic (ANY match) - default behavior
            if (matchAll == null || !matchAll) {
                // Join to the types collection (many-to-many)
                Join<Card, Type> typesJoin = root.join("types", JoinType.INNER);

                // Add DISTINCT to avoid duplicate cards when multiple types match
                if (query != null) {
                    query.distinct(true);
                }

                // Normalize database field and compare
                Expression<String> normalizedField = normalizeField(typesJoin.get("name"), criteriaBuilder);
                return normalizedField.in(normalizedTypes);
            }

            // AND logic (ALL match) - card must have ALL specified types
            // Strategy: For each type, check if the card has it, then combine with AND

            // Create a predicate for each type that must be matched
            jakarta.persistence.criteria.Predicate[] typePredicates = new jakarta.persistence.criteria.Predicate[normalizedTypes.size()];

            for (int i = 0; i < normalizedTypes.size(); i++) {
                String normalizedType = normalizedTypes.get(i);

                // Create a subquery that checks if this card has this specific type
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<Card> subRoot = subquery.from(Card.class);
                Join<Card, Type> subTypesJoin = subRoot.join("types", JoinType.INNER);

                Expression<String> subNormalizedField = normalizeField(subTypesJoin.get("name"), criteriaBuilder);

                // Count how many times this specific type appears for this card
                subquery.select(criteriaBuilder.count(subTypesJoin.get("id")))
                        .where(
                                criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                criteriaBuilder.equal(subNormalizedField, normalizedType)
                        );

                // This card must have at least one of this type
                typePredicates[i] = criteriaBuilder.greaterThan(subquery, 0L);
            }

            // All type predicates must be true (AND them together)
            return criteriaBuilder.and(typePredicates);
        };
    }

    /**
     * Filter by one or more subtypes (ex, V, Basic, Stage 1, etc.).
     * Supports both OR logic (ANY match) and AND logic (ALL match).
     * Supports accent-insensitive matching.
     *
     * @param subtypeNames List of subtype names to match
     * @param matchAll     If true, cards must have ALL subtypes (AND logic). If false/null, ANY subtype (OR logic).
     * @return Specification that matches cards with the specified subtypes
     */
    public static Specification<Card> hasSubtypes(List<String> subtypeNames, Boolean matchAll) {
        return (root, query, criteriaBuilder) -> {
            if (subtypeNames == null || subtypeNames.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Normalize search terms
            List<String> normalizedSubtypes = subtypeNames.stream()
                    .map(StringNormalizer::normalize)
                    .toList();

            // OR logic (ANY match) - default behavior
            if (matchAll == null || !matchAll) {
                // Join to the subtypes collection (many-to-many)
                Join<Card, Subtype> subtypesJoin = root.join("subtypes", JoinType.INNER);

                // Add DISTINCT to avoid duplicate cards
                if (query != null) {
                    query.distinct(true);
                }

                // Normalize database field and compare
                Expression<String> normalizedField = normalizeField(subtypesJoin.get("name"), criteriaBuilder);
                return normalizedField.in(normalizedSubtypes);
            }

            // AND logic (ALL match) - card must have ALL specified subtypes
            // Strategy: For each subtype, check if the card has it, then combine with AND

            // Create a predicate for each subtype that must be matched
            jakarta.persistence.criteria.Predicate[] subtypePredicates = new jakarta.persistence.criteria.Predicate[normalizedSubtypes.size()];

            for (int i = 0; i < normalizedSubtypes.size(); i++) {
                String normalizedSubtype = normalizedSubtypes.get(i);

                // Create a subquery that checks if this card has this specific subtype
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<Card> subRoot = subquery.from(Card.class);
                Join<Card, Subtype> subSubtypesJoin = subRoot.join("subtypes", JoinType.INNER);

                Expression<String> subNormalizedField = normalizeField(subSubtypesJoin.get("name"), criteriaBuilder);

                // Count how many times this specific subtype appears for this card
                subquery.select(criteriaBuilder.count(subSubtypesJoin.get("id")))
                        .where(
                                criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                criteriaBuilder.equal(subNormalizedField, normalizedSubtype)
                        );

                // This card must have at least one of this subtype
                subtypePredicates[i] = criteriaBuilder.greaterThan(subquery, 0L);
            }

            // All subtype predicates must be true (AND them together)
            return criteriaBuilder.and(subtypePredicates);
        };
    }

    /**
     * Filter by set identifier (e.g., "base1", "swsh8").
     * Supports accent-insensitive matching.
     *
     * @param setId The set identifier to match
     * @return Specification that matches cards from the specified set
     */
    public static Specification<Card> hasSetId(String setId) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(setId)) {
                return criteriaBuilder.conjunction();
            }

            // Join to the set entity (many-to-one)
            Join<Card, Set> setJoin = root.join("set", JoinType.INNER);

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(setId.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(setJoin.get("setId"), criteriaBuilder);
            return criteriaBuilder.equal(normalizedField, normalizedSearch);
        };
    }

    /**
     * Filter by rarity name.
     * Supports accent-insensitive matching.
     *
     * @param rarityName The rarity to match (Common, Uncommon, Rare, etc.)
     * @return Specification that matches cards with the specified rarity
     */
    public static Specification<Card> hasRarity(String rarityName) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(rarityName)) {
                return criteriaBuilder.conjunction();
            }

            // Join to the rarity entity (many-to-one)
            Join<Card, Rarity> rarityJoin = root.join("rarity", JoinType.INNER);

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(rarityName.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(rarityJoin.get("name"), criteriaBuilder);
            return criteriaBuilder.equal(normalizedField, normalizedSearch);
        };
    }

    /**
     * Filter by HP range (inclusive).
     * Uses the hp_numeric field for efficient numeric comparison.
     *
     * @param minHp Minimum HP (inclusive), null for no minimum
     * @param maxHp Maximum HP (inclusive), null for no maximum
     * @return Specification that matches cards with HP in the specified range
     */
    public static Specification<Card> hpBetween(Integer minHp, Integer maxHp) {
        return (root, query, criteriaBuilder) -> {
            if (minHp == null && maxHp == null) {
                return criteriaBuilder.conjunction();
            }

            if (minHp != null && maxHp != null) {
                // Both min and max specified: use BETWEEN
                return criteriaBuilder.between(root.get("hpNumeric"), minHp, maxHp);
            } else if (minHp != null) {
                // Only min specified: >= minHp
                return criteriaBuilder.greaterThanOrEqualTo(root.get("hpNumeric"), minHp);
            } else {
                // Only max specified: <= maxHp
                return criteriaBuilder.lessThanOrEqualTo(root.get("hpNumeric"), maxHp);
            }
        };
    }

    /**
     * Filter by attack name (case-insensitive, accent-insensitive partial match).
     *
     * @param attackName The attack name to search for
     * @return Specification that matches cards with attacks containing the search term
     */
    public static Specification<Card> hasAttackName(String attackName) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(attackName)) {
                return criteriaBuilder.conjunction();
            }

            // Join to attacks collection (many-to-many)
            Join<Card, Attack> attacksJoin = root.join("attacks", JoinType.INNER);

            // Add DISTINCT to avoid duplicate cards
            if (query != null) {
                query.distinct(true);
            }

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(attackName.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(attacksJoin.get("name"), criteriaBuilder);

            return criteriaBuilder.like(normalizedField, "%" + normalizedSearch + "%");
        };
    }

    /**
     * Filter by attack damage range (inclusive).
     * Uses the damage_numeric field for efficient numeric comparison.
     *
     * @param minDamage Minimum damage (inclusive), null for no minimum
     * @param maxDamage Maximum damage (inclusive), null for no maximum
     * @return Specification that matches cards with attacks in the specified damage range
     */
    public static Specification<Card> attackDamageBetween(Integer minDamage, Integer maxDamage) {
        return (root, query, criteriaBuilder) -> {
            if (minDamage == null && maxDamage == null) {
                return criteriaBuilder.conjunction();
            }

            // Join to attacks collection (many-to-many)
            Join<Card, Attack> attacksJoin = root.join("attacks", JoinType.INNER);

            // Add DISTINCT to avoid duplicate cards
            if (query != null) {
                query.distinct(true);
            }

            if (minDamage != null && maxDamage != null) {
                // Both min and max specified: use BETWEEN
                return criteriaBuilder.between(attacksJoin.get("damageNumeric"), minDamage, maxDamage);
            } else if (minDamage != null) {
                // Only min specified: >= minDamage
                return criteriaBuilder.greaterThanOrEqualTo(attacksJoin.get("damageNumeric"), minDamage);
            } else {
                // Only max specified: <= maxDamage
                return criteriaBuilder.lessThanOrEqualTo(attacksJoin.get("damageNumeric"), maxDamage);
            }
        };
    }

    /**
     * Filter by one or more attack cost types (Fire, Water, Colorless, etc.).
     * Supports both OR logic (ANY match) and AND logic (ALL match).
     * Supports accent-insensitive matching.
     *
     * @param costTypes List of cost type names to match
     * @param matchAll  If true, attacks must have ALL cost types (AND logic). If false/null, ANY cost type (OR logic).
     * @return Specification that matches cards with attacks having the specified cost types
     */
    public static Specification<Card> hasAttackCost(List<String> costTypes, Boolean matchAll) {
        return (root, query, criteriaBuilder) -> {
            if (costTypes == null || costTypes.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Normalize search terms
            List<String> normalizedCostTypes = costTypes.stream()
                    .map(StringNormalizer::normalize)
                    .toList();

            // OR logic (ANY match) - default behavior
            if (matchAll == null || !matchAll) {
                // Join through: Card -> Attack -> AttackCost -> Type
                Join<Card, Attack> attacksJoin = root.join("attacks", JoinType.INNER);
                Join<Attack, AttackCost> costsJoin = attacksJoin.join("costs", JoinType.INNER);
                Join<AttackCost, Type> typeJoin = costsJoin.join("type", JoinType.INNER);

                // Add DISTINCT to avoid duplicate cards
                if (query != null) {
                    query.distinct(true);
                }

                // Normalize database field and compare
                Expression<String> normalizedField = normalizeField(typeJoin.get("name"), criteriaBuilder);
                return normalizedField.in(normalizedCostTypes);
            }

            // AND logic (ALL match) - card's attacks must have ALL specified cost types
            // Strategy: For each cost type, check if the card has an attack with it, then combine with AND

            // Create a predicate for each cost type that must be matched
            jakarta.persistence.criteria.Predicate[] costPredicates = new jakarta.persistence.criteria.Predicate[normalizedCostTypes.size()];

            for (int i = 0; i < normalizedCostTypes.size(); i++) {
                String normalizedCostType = normalizedCostTypes.get(i);

                // Create a subquery that checks if this card has an attack with this specific cost type
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<Card> subRoot = subquery.from(Card.class);
                Join<Card, Attack> subAttacksJoin = subRoot.join("attacks", JoinType.INNER);
                Join<Attack, AttackCost> subCostsJoin = subAttacksJoin.join("costs", JoinType.INNER);
                Join<AttackCost, Type> subTypeJoin = subCostsJoin.join("type", JoinType.INNER);

                Expression<String> subNormalizedField = normalizeField(subTypeJoin.get("name"), criteriaBuilder);

                // Count how many times this specific cost type appears in the card's attacks
                subquery.select(criteriaBuilder.count(subTypeJoin.get("id")))
                        .where(
                                criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                criteriaBuilder.equal(subNormalizedField, normalizedCostType)
                        );

                // This card must have at least one attack with this cost type
                costPredicates[i] = criteriaBuilder.greaterThan(subquery, 0L);
            }

            // All cost predicates must be true (AND them together)
            return criteriaBuilder.and(costPredicates);
        };
    }

    /**
     * Filter by attack text/description (case-insensitive, accent-insensitive partial match).
     *
     * @param attackText The attack text to search for
     * @return Specification that matches cards with attacks containing the search term in their description
     */
    public static Specification<Card> hasAttackText(String attackText) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(attackText)) {
                return criteriaBuilder.conjunction();
            }

            // Join to attacks collection (many-to-many)
            Join<Card, Attack> attacksJoin = root.join("attacks", JoinType.INNER);

            // Add DISTINCT to avoid duplicate cards
            if (query != null) {
                query.distinct(true);
            }

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(attackText.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(attacksJoin.get("text"), criteriaBuilder);

            return criteriaBuilder.like(normalizedField, "%" + normalizedSearch + "%");
        };
    }

    /**
     * Filter by presence of abilities.
     *
     * @param hasAbility If true, only return cards with abilities. If false, only return cards without abilities. If null, no filtering.
     * @return Specification that matches cards based on ability presence
     */
    public static Specification<Card> hasAbility(Boolean hasAbility) {
        return (root, query, criteriaBuilder) -> {
            if (hasAbility == null) {
                return criteriaBuilder.conjunction();
            }

            // Join to abilities collection (many-to-many)
            Join<Card, Ability> abilitiesJoin = root.join("abilities", JoinType.LEFT);

            if (hasAbility) {
                // Cards WITH abilities: join must find at least one ability
                return criteriaBuilder.isNotNull(abilitiesJoin.get("id"));
            } else {
                // Cards WITHOUT abilities: join must find no abilities
                return criteriaBuilder.isNull(abilitiesJoin.get("id"));
            }
        };
    }

    /**
     * Filter by ability name (case-insensitive, accent-insensitive partial match).
     *
     * @param abilityName The ability name to search for
     * @return Specification that matches cards with abilities containing the search term
     */
    public static Specification<Card> hasAbilityName(String abilityName) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(abilityName)) {
                return criteriaBuilder.conjunction();
            }

            // Join to abilities collection (many-to-many)
            Join<Card, Ability> abilitiesJoin = root.join("abilities", JoinType.INNER);

            // Add DISTINCT to avoid duplicate cards
            if (query != null) {
                query.distinct(true);
            }

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(abilityName.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(abilitiesJoin.get("name"), criteriaBuilder);

            return criteriaBuilder.like(normalizedField, "%" + normalizedSearch + "%");
        };
    }

    /**
     * Filter by ability text/description (case-insensitive, accent-insensitive partial match).
     *
     * @param abilityText The ability text to search for
     * @return Specification that matches cards with abilities containing the search term in their description
     */
    public static Specification<Card> hasAbilityText(String abilityText) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(abilityText)) {
                return criteriaBuilder.conjunction();
            }

            // Join to abilities collection (many-to-many)
            Join<Card, Ability> abilitiesJoin = root.join("abilities", JoinType.INNER);

            // Add DISTINCT to avoid duplicate cards
            if (query != null) {
                query.distinct(true);
            }

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(abilityText.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(abilitiesJoin.get("text"), criteriaBuilder);

            return criteriaBuilder.like(normalizedField, "%" + normalizedSearch + "%");
        };
    }

    /**
     * Filter by artist name (case-insensitive, accent-insensitive exact match).
     *
     * @param artistName The artist name to match
     * @return Specification that matches cards by the specified artist
     */
    public static Specification<Card> hasArtist(String artistName) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(artistName)) {
                return criteriaBuilder.conjunction();
            }

            // Join to the artist entity (many-to-one)
            Join<Card, Artist> artistJoin = root.join("artist", JoinType.INNER);

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(artistName.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(artistJoin.get("name"), criteriaBuilder);
            return criteriaBuilder.equal(normalizedField, normalizedSearch);
        };
    }

    /**
     * Filter by regulation mark (exact match, case-insensitive).
     *
     * @param regulationMark The regulation mark to match (A, B, C, D, E, F, G, H)
     * @return Specification that matches cards with the specified regulation mark
     */
    public static Specification<Card> hasRegulationMark(String regulationMark) {
        return (root, query, criteriaBuilder) -> {
            if (regulationMark == null || regulationMark.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Direct comparison on card's regulationMark field
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("regulationMark")),
                    regulationMark.trim().toLowerCase()
            );
        };
    }

    /**
     * Filter by retreat cost range (inclusive).
     * Uses the converted_retreat_cost field for efficient numeric comparison.
     *
     * @param minCost Minimum retreat cost (inclusive), null for no minimum
     * @param maxCost Maximum retreat cost (inclusive), null for no maximum
     * @return Specification that matches cards with retreat cost in the specified range
     */
    public static Specification<Card> retreatCostBetween(Integer minCost, Integer maxCost) {
        return (root, query, criteriaBuilder) -> {
            if (minCost == null && maxCost == null) {
                return criteriaBuilder.conjunction();
            }

            if (minCost != null && maxCost != null) {
                // Both min and max specified: use BETWEEN
                return criteriaBuilder.between(root.get("convertedRetreatCost"), minCost, maxCost);
            } else if (minCost != null) {
                // Only min specified: >= minCost
                return criteriaBuilder.greaterThanOrEqualTo(root.get("convertedRetreatCost"), minCost);
            } else {
                // Only max specified: <= maxCost
                return criteriaBuilder.lessThanOrEqualTo(root.get("convertedRetreatCost"), maxCost);
            }
        };
    }

    /**
     * Filter by one or more formats (Standard, Expanded, Unlimited).
     * Supports both OR logic (ANY match) and AND logic (ALL match).
     * Only matches cards that are legal in the specified format(s).
     * Supports accent-insensitive matching.
     *
     * @param formatNames List of format names to match
     * @param matchAll    If true, cards must be legal in ALL formats (AND logic). If false/null, ANY format (OR logic).
     * @return Specification that matches cards legal in the specified formats
     */
    public static Specification<Card> hasFormats(List<String> formatNames, Boolean matchAll) {
        return (root, query, criteriaBuilder) -> {
            if (formatNames == null || formatNames.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Normalize search terms
            List<String> normalizedFormats = formatNames.stream()
                    .map(StringNormalizer::normalize)
                    .toList();

            // OR logic (ANY match) - default behavior
            if (matchAll == null || !matchAll) {
                // Join to the legalities collection (many-to-many through pokemon_card_legality)
                Join<Card, CardLegality> legalityJoin = root.join("legalities", JoinType.INNER);
                Join<CardLegality, Format> formatJoin = legalityJoin.join("format", JoinType.INNER);

                // Add DISTINCT to avoid duplicate cards
                if (query != null) {
                    query.distinct(true);
                }

                // Normalize database field and compare
                Expression<String> normalizedField = normalizeField(formatJoin.get("name"), criteriaBuilder);

                // Only match cards that are "legal" in the format (not banned)
                return criteriaBuilder.and(
                        normalizedField.in(normalizedFormats),
                        criteriaBuilder.equal(legalityJoin.get("status"), criteriaBuilder.literal(LegalityStatus.legal))
                );
            }

            // AND logic (ALL match) - card must be legal in ALL specified formats
            // Strategy: For each format, check if the card is legal in it, then combine with AND

            // Create a predicate for each format that must be matched
            jakarta.persistence.criteria.Predicate[] formatPredicates = new jakarta.persistence.criteria.Predicate[normalizedFormats.size()];

            for (int i = 0; i < normalizedFormats.size(); i++) {
                String normalizedFormat = normalizedFormats.get(i);

                // Create a subquery that checks if this card is legal in this specific format
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<Card> subRoot = subquery.from(Card.class);
                Join<Card, CardLegality> subLegalityJoin = subRoot.join("legalities", JoinType.INNER);
                Join<CardLegality, Format> subFormatJoin = subLegalityJoin.join("format", JoinType.INNER);

                Expression<String> subNormalizedField = normalizeField(subFormatJoin.get("name"), criteriaBuilder);

                // Count how many times this card is legal in this specific format
                subquery.select(criteriaBuilder.count(subFormatJoin.get("id")))
                        .where(
                                criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                criteriaBuilder.equal(subNormalizedField, normalizedFormat),
                                criteriaBuilder.equal(subLegalityJoin.get("status"), criteriaBuilder.literal(LegalityStatus.legal))
                        );

                // This card must be legal in this format (count > 0)
                formatPredicates[i] = criteriaBuilder.greaterThan(subquery, 0L);
            }

            // All format predicates must be true (AND them together)
            return criteriaBuilder.and(formatPredicates);
        };
    }

    /**
     * Filter by one or more formats where cards are BANNED (Standard, Expanded, Unlimited).
     * Supports both OR logic (ANY match) and AND logic (ALL match).
     * Only matches cards that are banned in the specified format(s).
     * Supports accent-insensitive matching.
     *
     * @param formatNames List of format names to match
     * @param matchAll    If true, cards must be banned in ALL formats (AND logic). If false/null, ANY format (OR logic).
     * @return Specification that matches cards banned in the specified formats
     */
    public static Specification<Card> hasFormatsBanned(List<String> formatNames, Boolean matchAll) {
        return (root, query, criteriaBuilder) -> {
            if (formatNames == null || formatNames.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Normalize search terms
            List<String> normalizedFormats = formatNames.stream()
                    .map(StringNormalizer::normalize)
                    .toList();

            // OR logic (ANY match) - default behavior
            if (matchAll == null || !matchAll) {
                // Join to the legalities collection (many-to-many through pokemon_card_legality)
                Join<Card, CardLegality> legalityJoin = root.join("legalities", JoinType.INNER);
                Join<CardLegality, Format> formatJoin = legalityJoin.join("format", JoinType.INNER);

                // Add DISTINCT to avoid duplicate cards
                if (query != null) {
                    query.distinct(true);
                }

                // Normalize database field and compare
                Expression<String> normalizedField = normalizeField(formatJoin.get("name"), criteriaBuilder);

                // Only match cards that are "banned" in the format
                return criteriaBuilder.and(
                        normalizedField.in(normalizedFormats),
                        criteriaBuilder.equal(legalityJoin.get("status"), criteriaBuilder.literal(LegalityStatus.banned))
                );
            }

            // AND logic (ALL match) - card must be banned in ALL specified formats
            // Strategy: For each format, check if the card is banned in it, then combine with AND

            // Create a predicate for each format that must be matched
            jakarta.persistence.criteria.Predicate[] formatPredicates = new jakarta.persistence.criteria.Predicate[normalizedFormats.size()];

            for (int i = 0; i < normalizedFormats.size(); i++) {
                String normalizedFormat = normalizedFormats.get(i);

                // Create a subquery that checks if this card is banned in this specific format
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<Card> subRoot = subquery.from(Card.class);
                Join<Card, CardLegality> subLegalityJoin = subRoot.join("legalities", JoinType.INNER);
                Join<CardLegality, Format> subFormatJoin = subLegalityJoin.join("format", JoinType.INNER);

                Expression<String> subNormalizedField = normalizeField(subFormatJoin.get("name"), criteriaBuilder);

                // Count how many times this card is banned in this specific format
                subquery.select(criteriaBuilder.count(subFormatJoin.get("id")))
                        .where(
                                criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                criteriaBuilder.equal(subNormalizedField, normalizedFormat),
                                criteriaBuilder.equal(subLegalityJoin.get("status"), criteriaBuilder.literal(LegalityStatus.banned))
                        );

                // This card must be banned in this format (count > 0)
                formatPredicates[i] = criteriaBuilder.greaterThan(subquery, 0L);
            }

            // All format predicates must be true (AND them together)
            return criteriaBuilder.and(formatPredicates);
        };
    }

    /**
     * Combine all specifications based on search request parameters.
     * This is a convenience method that applies all applicable filters.
     *
     * @param name               Card name search
     * @param supertype          Supertype filter
     * @param types              List of types to match
     * @param typesMatchAll      If true, match ALL types (AND logic); if false/null, match ANY type (OR logic)
     * @param subtypes           List of subtypes to match
     * @param subtypesMatchAll   If true, match ALL subtypes (AND logic); if false/null, match ANY subtype (OR logic)
     * @param setId              Set identifier
     * @param rarity             Rarity name
     * @param hpMin              Minimum HP
     * @param hpMax              Maximum HP
     * @param attackName              Attack name search
     * @param attackText              Attack text/description search
     * @param attackDamageMin         Minimum attack damage
     * @param attackDamageMax         Maximum attack damage
     * @param attackCost              List of attack cost types to match
     * @param attackCostMatchAll      If true, match ALL cost types (AND logic); if false/null, match ANY cost type (OR logic)
     * @param hasAbility              If true, only cards with abilities; if false, only cards without abilities; if null, no filtering
     * @param abilityName             Ability name search
     * @param abilityText             Ability text/description search
     * @param artist                  Artist name
     * @param regulationMark          Regulation mark
     * @param retreatCostMin          Minimum retreat cost
     * @param retreatCostMax          Maximum retreat cost
     * @param formats                 List of formats to match (legal status)
     * @param formatsMatchAll         If true, cards must be legal in ALL formats (AND logic); if false/null, ANY format (OR logic)
     * @param formatsBanned           List of formats where cards are banned
     * @param formatsBannedMatchAll   If true, cards must be banned in ALL formats (AND logic); if false/null, ANY format (OR logic)
     * @return Combined Specification with all applicable filters
     */
    public static Specification<Card> buildSpecification(
            String name,
            String supertype,
            List<String> types,
            Boolean typesMatchAll,
            List<String> subtypes,
            Boolean subtypesMatchAll,
            String setId,
            String rarity,
            Integer hpMin,
            Integer hpMax,
            String attackName,
            String attackText,
            Integer attackDamageMin,
            Integer attackDamageMax,
            List<String> attackCost,
            Boolean attackCostMatchAll,
            Boolean hasAbility,
            String abilityName,
            String abilityText,
            String artist,
            String regulationMark,
            Integer retreatCostMin,
            Integer retreatCostMax,
            List<String> formats,
            Boolean formatsMatchAll,
            List<String> formatsBanned,
            Boolean formatsBannedMatchAll
    ) {
        // Build list of all specifications and combine them with allOf()
        return Specification.allOf(
                hasNameContaining(name),
                hasSupertype(supertype),
                hasTypes(types, typesMatchAll),
                hasSubtypes(subtypes, subtypesMatchAll),
                hasSetId(setId),
                hasRarity(rarity),
                hpBetween(hpMin, hpMax),
                hasAttackName(attackName),
                hasAttackText(attackText),
                attackDamageBetween(attackDamageMin, attackDamageMax),
                hasAttackCost(attackCost, attackCostMatchAll),
                hasAbility(hasAbility),
                hasAbilityName(abilityName),
                hasAbilityText(abilityText),
                hasArtist(artist),
                hasRegulationMark(regulationMark),
                retreatCostBetween(retreatCostMin, retreatCostMax),
                hasFormats(formats, formatsMatchAll),
                hasFormatsBanned(formatsBanned, formatsBannedMatchAll)
        );
    }
}
