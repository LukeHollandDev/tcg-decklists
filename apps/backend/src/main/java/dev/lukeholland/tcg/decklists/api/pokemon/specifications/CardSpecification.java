package dev.lukeholland.tcg.decklists.api.pokemon.specifications;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.*;
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
     * Combine all specifications based on search request parameters.
     * This is a convenience method that applies all applicable filters.
     *
     * @param name             Card name search
     * @param supertype        Supertype filter
     * @param types            List of types to match
     * @param typesMatchAll    If true, match ALL types (AND logic); if false/null, match ANY type (OR logic)
     * @param subtypes         List of subtypes to match
     * @param subtypesMatchAll If true, match ALL subtypes (AND logic); if false/null, match ANY subtype (OR logic)
     * @param setId            Set identifier
     * @param rarity           Rarity name
     * @param hpMin            Minimum HP
     * @param hpMax            Maximum HP
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
            Integer hpMax
    ) {
        // Build list of all specifications and combine them with allOf()
        return Specification.allOf(
                hasNameContaining(name),
                hasSupertype(supertype),
                hasTypes(types, typesMatchAll),
                hasSubtypes(subtypes, subtypesMatchAll),
                hasSetId(setId),
                hasRarity(rarity),
                hpBetween(hpMin, hpMax)
        );
    }
}
