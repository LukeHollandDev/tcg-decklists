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
     * Helper method for OR logic (ANY match) in many-to-many relationships.
     * Generic implementation that handles joining to a collection and checking if any value matches.
     *
     * @param root             The root entity
     * @param query            The criteria query
     * @param criteriaBuilder  The criteria builder
     * @param normalizedValues List of normalized values to match
     * @param joinAttribute    Name of the join attribute on the root entity
     * @param fieldName        Name of the field to match in the joined entity
     * @param <T>              Type of the joined entity
     * @return Predicate for OR logic matching
     */
    private static <T> jakarta.persistence.criteria.Predicate buildMatchAnyPredicate(
            jakarta.persistence.criteria.Root<Card> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            List<String> normalizedValues,
            String joinAttribute,
            String fieldName) {

        // Join to the collection (many-to-many or one-to-many)
        Join<Card, T> join = root.join(joinAttribute, JoinType.INNER);

        // Add DISTINCT to avoid duplicate cards when multiple matches occur
        if (query != null) {
            query.distinct(true);
        }

        // Normalize database field and check if it's in the list
        Expression<String> normalizedField = normalizeField(join.get(fieldName), criteriaBuilder);
        return normalizedField.in(normalizedValues);
    }

    /**
     * Helper method for AND logic (ALL match) in many-to-many relationships.
     * Generic implementation that creates subqueries to ensure the card has all specified values.
     *
     * @param root             The root entity
     * @param query            The criteria query
     * @param criteriaBuilder  The criteria builder
     * @param normalizedValues List of normalized values to match
     * @param joinAttribute    Name of the join attribute on the root entity
     * @param fieldName        Name of the field to match in the joined entity
     * @param additionalWhere  Optional additional where clause for subquery (can be null)
     * @param <T>              Type of the joined entity
     * @return Predicate for AND logic matching
     */
    private static <T> jakarta.persistence.criteria.Predicate buildMatchAllPredicate(
            jakarta.persistence.criteria.Root<Card> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            List<String> normalizedValues,
            String joinAttribute,
            String fieldName,
            jakarta.persistence.criteria.Predicate additionalWhere) {

        // Create a predicate for each value that must be matched
        jakarta.persistence.criteria.Predicate[] predicates = new jakarta.persistence.criteria.Predicate[normalizedValues.size()];

        for (int i = 0; i < normalizedValues.size(); i++) {
            String normalizedValue = normalizedValues.get(i);

            // Create a subquery that checks if this card has this specific value
            jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
            jakarta.persistence.criteria.Root<Card> subRoot = subquery.from(Card.class);
            Join<Card, T> subJoin = subRoot.join(joinAttribute, JoinType.INNER);

            Expression<String> subNormalizedField = normalizeField(subJoin.get(fieldName), criteriaBuilder);

            // Build the where clause
            jakarta.persistence.criteria.Predicate whereClause = criteriaBuilder.and(
                    criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                    criteriaBuilder.equal(subNormalizedField, normalizedValue)
            );

            // Add additional where conditions if provided
            if (additionalWhere != null) {
                whereClause = criteriaBuilder.and(whereClause, additionalWhere);
            }

            // Count how many times this specific value appears for this card
            subquery.select(criteriaBuilder.count(subJoin.get("id"))).where(whereClause);

            // This card must have at least one of this value
            predicates[i] = criteriaBuilder.greaterThan(subquery, 0L);
        }

        // All predicates must be true (AND them together)
        return criteriaBuilder.and(predicates);
    }

    /**
     * Helper method for format legality filtering with OR/AND logic.
     * Handles the nested join path: Card -> CardLegality -> Format
     *
     * @param root              The root entity
     * @param query             The criteria query
     * @param criteriaBuilder   The criteria builder
     * @param normalizedFormats List of normalized format names
     * @param matchAll          If true, use AND logic; if false/null, use OR logic
     * @param legalityStatus    The legality status to match (legal or banned)
     * @return Predicate for format matching
     */
    private static jakarta.persistence.criteria.Predicate buildFormatPredicate(
            jakarta.persistence.criteria.Root<Card> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            List<String> normalizedFormats,
            Boolean matchAll,
            LegalityStatus legalityStatus) {

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

            // Match format name AND legality status
            return criteriaBuilder.and(
                    normalizedField.in(normalizedFormats),
                    criteriaBuilder.equal(legalityJoin.get("status"), criteriaBuilder.literal(legalityStatus))
            );
        }

        // AND logic (ALL match) - card must match in ALL specified formats
        jakarta.persistence.criteria.Predicate[] predicates = new jakarta.persistence.criteria.Predicate[normalizedFormats.size()];

        for (int i = 0; i < normalizedFormats.size(); i++) {
            String normalizedFormat = normalizedFormats.get(i);

            // Create a subquery that checks if this card matches this specific format
            jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
            jakarta.persistence.criteria.Root<Card> subRoot = subquery.from(Card.class);
            Join<Card, CardLegality> subLegalityJoin = subRoot.join("legalities", JoinType.INNER);
            Join<CardLegality, Format> subFormatJoin = subLegalityJoin.join("format", JoinType.INNER);

            Expression<String> subNormalizedField = normalizeField(subFormatJoin.get("name"), criteriaBuilder);

            // Count cards with this format and legality status
            subquery.select(criteriaBuilder.count(subFormatJoin.get("id")))
                    .where(
                            criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                            criteriaBuilder.equal(subNormalizedField, normalizedFormat),
                            criteriaBuilder.equal(subLegalityJoin.get("status"), criteriaBuilder.literal(legalityStatus))
                    );

            // This card must match this format
            predicates[i] = criteriaBuilder.greaterThan(subquery, 0L);
        }

        // All format predicates must be true (AND them together)
        return criteriaBuilder.and(predicates);
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
                return buildMatchAnyPredicate(root, query, criteriaBuilder, normalizedTypes, "types", "name");
            }

            // AND logic (ALL match) - card must have ALL specified types
            return buildMatchAllPredicate(root, query, criteriaBuilder, normalizedTypes, "types", "name", null);
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
                return buildMatchAnyPredicate(root, query, criteriaBuilder, normalizedSubtypes, "subtypes", "name");
            }

            // AND logic (ALL match) - card must have ALL specified subtypes
            return buildMatchAllPredicate(root, query, criteriaBuilder, normalizedSubtypes, "subtypes", "name", null);
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
     * Supports accent-insensitive matching and multiset subset matching.
     * <p>
     * When matchAll=true, supports searching for attacks with multiple of the same type.
     * For example, searching for ["Fire", "Fire", "Water"] will match attacks with at least
     * 2 Fire and 1 Water energy, like ["Fire", "Fire", "Water"] or ["Fire", "Fire", "Water", "Lightning"].
     * <p>
     * This implements multiset subset matching: the attack's cost multiset must contain
     * the search multiset as a subset.
     *
     * @param costTypes List of cost type names to match (can contain duplicates)
     * @param matchAll  If true, attacks must have ALL cost types with sufficient quantities (multiset subset matching).
     *                  If false/null, attacks must have ANY of the specified cost types (OR logic).
     * @return Specification that matches cards with attacks having the specified cost types
     */
    public static Specification<Card> hasAttackCost(List<String> costTypes, Boolean matchAll) {
        return (root, query, criteriaBuilder) -> {
            if (costTypes == null || costTypes.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Normalize search terms and count occurrences (create multiset)
            List<String> normalizedCostTypes = costTypes.stream()
                    .map(StringNormalizer::normalize)
                    .toList();

            // OR logic (ANY match) - default behavior
            // For OR logic, we just check if any of the types appear (ignoring quantities)
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
                // Use a set to avoid checking duplicates multiple times
                java.util.Set<String> uniqueTypes = new java.util.HashSet<>(normalizedCostTypes);
                return normalizedField.in(uniqueTypes);
            }

            // AND logic (ALL match) with multiset support - card must have at least one attack
            // with sufficient quantities of each type in the SAME attack

            // Count occurrences of each type (multiset/bag)
            java.util.Map<String, Long> typeCounts = normalizedCostTypes.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            java.util.function.Function.identity(),
                            java.util.stream.Collectors.counting()
                    ));

            // Create a main subquery that checks if there exists an attack with all required types/quantities
            // Key insight: All type requirements must be checked against the SAME attack
            jakarta.persistence.criteria.Subquery<Integer> mainSubquery = query.subquery(Integer.class);
            jakarta.persistence.criteria.Root<Card> subCardRoot = mainSubquery.from(Card.class);
            Join<Card, Attack> subAttackJoin = subCardRoot.join("attacks", JoinType.INNER);

            // For each type/quantity requirement, create a predicate that checks if THIS attack has it
            java.util.List<jakarta.persistence.criteria.Predicate> attackRequirements = new java.util.ArrayList<>();

            for (java.util.Map.Entry<String, Long> entry : typeCounts.entrySet()) {
                String normalizedCostType = entry.getKey();
                Long requiredQuantity = entry.getValue();

                // Create a subquery that checks if this specific attack has an AttackCost
                // for this type with sufficient quantity
                jakarta.persistence.criteria.Subquery<Integer> costCheckSubquery = query.subquery(Integer.class);
                jakarta.persistence.criteria.Root<AttackCost> costRoot = costCheckSubquery.from(AttackCost.class);
                Join<AttackCost, Type> costTypeJoin = costRoot.join("type", JoinType.INNER);

                Expression<String> costNormalizedField = normalizeField(costTypeJoin.get("name"), criteriaBuilder);

                costCheckSubquery.select(criteriaBuilder.literal(1))
                        .where(
                                criteriaBuilder.equal(costRoot.get("attack"), subAttackJoin),
                                criteriaBuilder.equal(costNormalizedField, normalizedCostType),
                                criteriaBuilder.greaterThanOrEqualTo(
                                        costRoot.get("quantity"),
                                        requiredQuantity.intValue()
                                )
                        );

                // This attack must have this type/quantity (all requirements reference the same subAttackJoin)
                attackRequirements.add(criteriaBuilder.exists(costCheckSubquery));
            }

            // The main subquery checks if there exists an attack that satisfies ALL requirements
            mainSubquery.select(criteriaBuilder.literal(1))
                    .where(
                            criteriaBuilder.equal(subCardRoot.get("id"), root.get("id")),
                            criteriaBuilder.and(attackRequirements.toArray(new jakarta.persistence.criteria.Predicate[0]))
                    );

            return criteriaBuilder.exists(mainSubquery);
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
     * Filter by presence of rule boxes.
     *
     * @param hasRuleBox If true, only return cards with rule boxes. If false, only return cards without rule boxes. If null, no filtering.
     * @return Specification that matches cards based on rule box presence
     */
    public static Specification<Card> hasRuleBox(Boolean hasRuleBox) {
        return (root, query, criteriaBuilder) -> {
            if (hasRuleBox == null) {
                return criteriaBuilder.conjunction();
            }

            // Join to rules collection (many-to-many)
            Join<Card, Rule> rulesJoin = root.join("rules", JoinType.LEFT);

            if (hasRuleBox) {
                // Cards WITH rule boxes: join must find at least one rule
                return criteriaBuilder.isNotNull(rulesJoin.get("id"));
            } else {
                // Cards WITHOUT rule boxes: join must find no rules
                return criteriaBuilder.isNull(rulesJoin.get("id"));
            }
        };
    }

    /**
     * Filter by presence of weaknesses.
     *
     * @param hasWeakness If true, only return cards with weaknesses. If false, only return cards without weaknesses. If null, no filtering.
     * @return Specification that matches cards based on weakness presence
     */
    public static Specification<Card> hasWeakness(Boolean hasWeakness) {
        return (root, query, criteriaBuilder) -> {
            if (hasWeakness == null) {
                return criteriaBuilder.conjunction();
            }

            // Join to weaknesses collection (one-to-many)
            Join<Card, CardWeakness> weaknessesJoin = root.join("weaknesses", JoinType.LEFT);

            if (hasWeakness) {
                // Cards WITH weaknesses: join must find at least one weakness
                return criteriaBuilder.isNotNull(weaknessesJoin.get("id"));
            } else {
                // Cards WITHOUT weaknesses: join must find no weaknesses
                return criteriaBuilder.isNull(weaknessesJoin.get("id"));
            }
        };
    }

    /**
     * Filter by presence of resistances.
     *
     * @param hasResistance If true, only return cards with resistances. If false, only return cards without resistances. If null, no filtering.
     * @return Specification that matches cards based on resistance presence
     */
    public static Specification<Card> hasResistance(Boolean hasResistance) {
        return (root, query, criteriaBuilder) -> {
            if (hasResistance == null) {
                return criteriaBuilder.conjunction();
            }

            // Join to resistances collection (one-to-many)
            Join<Card, CardResistance> resistancesJoin = root.join("resistances", JoinType.LEFT);

            if (hasResistance) {
                // Cards WITH resistances: join must find at least one resistance
                return criteriaBuilder.isNotNull(resistancesJoin.get("id"));
            } else {
                // Cards WITHOUT resistances: join must find no resistances
                return criteriaBuilder.isNull(resistancesJoin.get("id"));
            }
        };
    }

    /**
     * Filter by one or more weakness types (Fire, Water, etc.).
     * Supports both OR logic (ANY match) and AND logic (ALL match).
     * Uses accent-insensitive matching.
     *
     * @param typeNames List of weakness type names to match
     * @param matchAll  If true, cards must have weaknesses of ALL specified types (AND logic). If false/null, ANY type (OR logic).
     * @return Specification that matches cards with the specified weakness types
     */
    public static Specification<Card> hasWeaknessTypes(List<String> typeNames, Boolean matchAll) {
        return (root, query, criteriaBuilder) -> {
            if (typeNames == null || typeNames.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Normalize search terms
            List<String> normalizedTypes = typeNames.stream()
                    .filter(t -> t != null && !t.trim().isEmpty())
                    .map(t -> StringNormalizer.normalize(t.trim()))
                    .toList();

            if (normalizedTypes.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // OR logic (ANY match) - default behavior
            if (matchAll == null || !matchAll) {
                // Join: Card -> CardWeakness -> Weakness -> Type
                Join<Card, CardWeakness> weaknessJoin = root.join("weaknesses", JoinType.INNER);
                Join<CardWeakness, Weakness> weaknessValueJoin = weaknessJoin.join("weakness", JoinType.INNER);
                Join<Weakness, Type> typeJoin = weaknessValueJoin.join("type", JoinType.INNER);

                // Add DISTINCT to avoid duplicate cards
                if (query != null) {
                    query.distinct(true);
                }

                // Match any of the normalized types
                Expression<String> normalizedField = normalizeField(typeJoin.get("name"), criteriaBuilder);
                return normalizedField.in(normalizedTypes);
            }

            // AND logic (ALL match) - card must have weaknesses of ALL specified types
            jakarta.persistence.criteria.Predicate[] typePredicates = new jakarta.persistence.criteria.Predicate[normalizedTypes.size()];

            for (int i = 0; i < normalizedTypes.size(); i++) {
                String normalizedType = normalizedTypes.get(i);

                // Create a subquery that checks if this card has a weakness of this specific type
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<Card> subRoot = subquery.from(Card.class);
                Join<Card, CardWeakness> subWeaknessJoin = subRoot.join("weaknesses", JoinType.INNER);
                Join<CardWeakness, Weakness> subWeaknessValueJoin = subWeaknessJoin.join("weakness", JoinType.INNER);
                Join<Weakness, Type> subTypeJoin = subWeaknessValueJoin.join("type", JoinType.INNER);

                Expression<String> subNormalizedField = normalizeField(subTypeJoin.get("name"), criteriaBuilder);

                subquery.select(criteriaBuilder.count(subWeaknessJoin.get("id")))
                        .where(
                                criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                criteriaBuilder.equal(subNormalizedField, normalizedType)
                        );

                // This card must have at least one weakness of this type
                typePredicates[i] = criteriaBuilder.greaterThan(subquery, 0L);
            }

            // All type predicates must be true (AND them together)
            return criteriaBuilder.and(typePredicates);
        };
    }

    /**
     * Filter by one or more resistance types (Fire, Water, etc.).
     * Supports both OR logic (ANY match) and AND logic (ALL match).
     * Uses accent-insensitive matching.
     *
     * @param typeNames List of resistance type names to match
     * @param matchAll  If true, cards must have resistances of ALL specified types (AND logic). If false/null, ANY type (OR logic).
     * @return Specification that matches cards with the specified resistance types
     */
    public static Specification<Card> hasResistanceTypes(List<String> typeNames, Boolean matchAll) {
        return (root, query, criteriaBuilder) -> {
            if (typeNames == null || typeNames.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Normalize search terms
            List<String> normalizedTypes = typeNames.stream()
                    .filter(t -> t != null && !t.trim().isEmpty())
                    .map(t -> StringNormalizer.normalize(t.trim()))
                    .toList();

            if (normalizedTypes.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // OR logic (ANY match) - default behavior
            if (matchAll == null || !matchAll) {
                // Join: Card -> CardResistance -> Resistance -> Type
                Join<Card, CardResistance> resistanceJoin = root.join("resistances", JoinType.INNER);
                Join<CardResistance, Resistance> resistanceValueJoin = resistanceJoin.join("resistance", JoinType.INNER);
                Join<Resistance, Type> typeJoin = resistanceValueJoin.join("type", JoinType.INNER);

                // Add DISTINCT to avoid duplicate cards
                if (query != null) {
                    query.distinct(true);
                }

                // Match any of the normalized types
                Expression<String> normalizedField = normalizeField(typeJoin.get("name"), criteriaBuilder);
                return normalizedField.in(normalizedTypes);
            }

            // AND logic (ALL match) - card must have resistances of ALL specified types
            jakarta.persistence.criteria.Predicate[] typePredicates = new jakarta.persistence.criteria.Predicate[normalizedTypes.size()];

            for (int i = 0; i < normalizedTypes.size(); i++) {
                String normalizedType = normalizedTypes.get(i);

                // Create a subquery that checks if this card has a resistance of this specific type
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<Card> subRoot = subquery.from(Card.class);
                Join<Card, CardResistance> subResistanceJoin = subRoot.join("resistances", JoinType.INNER);
                Join<CardResistance, Resistance> subResistanceValueJoin = subResistanceJoin.join("resistance", JoinType.INNER);
                Join<Resistance, Type> subTypeJoin = subResistanceValueJoin.join("type", JoinType.INNER);

                Expression<String> subNormalizedField = normalizeField(subTypeJoin.get("name"), criteriaBuilder);

                subquery.select(criteriaBuilder.count(subResistanceJoin.get("id")))
                        .where(
                                criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                criteriaBuilder.equal(subNormalizedField, normalizedType)
                        );

                // This card must have at least one resistance of this type
                typePredicates[i] = criteriaBuilder.greaterThan(subquery, 0L);
            }

            // All type predicates must be true (AND them together)
            return criteriaBuilder.and(typePredicates);
        };
    }

    /**
     * Filter by evolution source (what this card evolves from).
     * Uses case-insensitive, accent-insensitive partial matching.
     *
     * @param name The name of the Pokemon this card evolves from
     * @return Specification that matches cards that evolve from the specified Pokemon
     */
    public static Specification<Card> evolvesFrom(String name) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(name)) {
                return criteriaBuilder.conjunction();
            }

            // Join: Card -> CardEvolution -> Name
            Join<Card, CardEvolution> evolutionJoin = root.join("evolutions", JoinType.INNER);
            Join<CardEvolution, Name> nameJoin = evolutionJoin.join("name", JoinType.INNER);

            // Add DISTINCT to avoid duplicate cards
            if (query != null) {
                query.distinct(true);
            }

            // Filter by direction = FROM and match the name
            String normalizedSearch = StringNormalizer.normalize(name.trim());
            Expression<String> normalizedField = normalizeField(nameJoin.get("name"), criteriaBuilder);

            // Use criteriaBuilder.literal() for enum comparison (same pattern as hasFormats)
            return criteriaBuilder.and(
                    criteriaBuilder.equal(
                            evolutionJoin.get("direction"),
                            criteriaBuilder.literal(dev.lukeholland.tcg.decklists.api.pokemon.enums.EvolutionDirection.from)
                    ),
                    criteriaBuilder.like(normalizedField, "%" + normalizedSearch + "%")
            );
        };
    }

    /**
     * Filter by evolution target (what this card evolves to).
     * Uses case-insensitive, accent-insensitive partial matching.
     *
     * @param name The name of the Pokemon this card evolves to
     * @return Specification that matches cards that evolve to the specified Pokemon
     */
    public static Specification<Card> evolvesTo(String name) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(name)) {
                return criteriaBuilder.conjunction();
            }

            // Join: Card -> CardEvolution -> Name
            Join<Card, CardEvolution> evolutionJoin = root.join("evolutions", JoinType.INNER);
            Join<CardEvolution, Name> nameJoin = evolutionJoin.join("name", JoinType.INNER);

            // Add DISTINCT to avoid duplicate cards
            if (query != null) {
                query.distinct(true);
            }

            // Filter by direction = TO and match the name
            String normalizedSearch = StringNormalizer.normalize(name.trim());
            Expression<String> normalizedField = normalizeField(nameJoin.get("name"), criteriaBuilder);

            // Use criteriaBuilder.literal() for enum comparison (same pattern as hasFormats)
            return criteriaBuilder.and(
                    criteriaBuilder.equal(
                            evolutionJoin.get("direction"),
                            criteriaBuilder.literal(dev.lukeholland.tcg.decklists.api.pokemon.enums.EvolutionDirection.to)
                    ),
                    criteriaBuilder.like(normalizedField, "%" + normalizedSearch + "%")
            );
        };
    }

    /**
     * Filter by rule text/description (case-insensitive, accent-insensitive partial match).
     *
     * @param ruleText The rule text to search for
     * @return Specification that matches cards with rules containing the search term
     */
    public static Specification<Card> hasRuleText(String ruleText) {
        return (root, query, criteriaBuilder) -> {
            if (StringNormalizer.isNullOrEmpty(ruleText)) {
                return criteriaBuilder.conjunction();
            }

            // Join to rules collection (many-to-many)
            Join<Card, Rule> rulesJoin = root.join("rules", JoinType.INNER);

            // Add DISTINCT to avoid duplicate cards
            if (query != null) {
                query.distinct(true);
            }

            // Normalize search term
            String normalizedSearch = StringNormalizer.normalize(ruleText.trim());

            // Normalize database field and compare
            Expression<String> normalizedField = normalizeField(rulesJoin.get("text"), criteriaBuilder);

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

            // Use helper for both OR and AND logic
            return buildFormatPredicate(root, query, criteriaBuilder, normalizedFormats, matchAll, LegalityStatus.legal);
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

            // Use helper for both OR and AND logic
            return buildFormatPredicate(root, query, criteriaBuilder, normalizedFormats, matchAll, LegalityStatus.banned);
        };
    }

    /**
     * Combine all specifications based on search request parameters.
     * This is a convenience method that applies all applicable filters.
     *
     * @param name                   Card name search
     * @param supertype              Supertype filter
     * @param types                  List of types to match
     * @param typesMatchAll          If true, match ALL types (AND logic); if false/null, match ANY type (OR logic)
     * @param subtypes               List of subtypes to match
     * @param subtypesMatchAll       If true, match ALL subtypes (AND logic); if false/null, match ANY subtype (OR logic)
     * @param setId                  Set identifier
     * @param rarity                 Rarity name
     * @param hpMin                  Minimum HP
     * @param hpMax                  Maximum HP
     * @param attackName             Attack name search
     * @param attackText             Attack text/description search
     * @param attackDamageMin        Minimum attack damage
     * @param attackDamageMax        Maximum attack damage
     * @param attackCost             List of attack cost types to match
     * @param attackCostMatchAll     If true, match ALL cost types (AND logic); if false/null, match ANY cost type (OR logic)
     * @param hasAbility             If true, only cards with abilities; if false, only cards without abilities; if null, no filtering
     * @param abilityName            Ability name search
     * @param abilityText            Ability text/description search
     * @param artist                 Artist name
     * @param regulationMark         Regulation mark
     * @param retreatCostMin         Minimum retreat cost
     * @param retreatCostMax         Maximum retreat cost
     * @param formats                List of formats to match (legal status)
     * @param formatsMatchAll        If true, cards must be legal in ALL formats (AND logic); if false/null, ANY format (OR logic)
     * @param formatsBanned          List of formats where cards are banned
     * @param formatsBannedMatchAll  If true, cards must be banned in ALL formats (AND logic); if false/null, ANY format (OR logic)
     * @param hasRuleBox             If true, only cards with rule boxes; if false, only cards without rule boxes; if null, no filtering
     * @param hasWeakness            If true, only cards with weaknesses; if false, only cards without weaknesses; if null, no filtering
     * @param hasResistance          If true, only cards with resistances; if false, only cards without resistances; if null, no filtering
     * @param weaknessType           List of weakness types to match
     * @param weaknessTypeMatchAll   If true, match ALL weakness types (AND logic); if false/null, match ANY weakness type (OR logic)
     * @param resistanceType         List of resistance types to match
     * @param resistanceTypeMatchAll If true, match ALL resistance types (AND logic); if false/null, match ANY resistance type (OR logic)
     * @param evolvesFrom            Evolution source name (what this card evolves from)
     * @param evolvesTo              Evolution target name (what this card evolves to)
     * @param ruleText               Rule text/description search
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
            Boolean formatsBannedMatchAll,
            Boolean hasRuleBox,
            Boolean hasWeakness,
            Boolean hasResistance,
            List<String> weaknessType,
            Boolean weaknessTypeMatchAll,
            List<String> resistanceType,
            Boolean resistanceTypeMatchAll,
            String evolvesFrom,
            String evolvesTo,
            String ruleText
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
                hasFormatsBanned(formatsBanned, formatsBannedMatchAll),
                hasRuleBox(hasRuleBox),
                hasWeakness(hasWeakness),
                hasResistance(hasResistance),
                hasWeaknessTypes(weaknessType, weaknessTypeMatchAll),
                hasResistanceTypes(resistanceType, resistanceTypeMatchAll),
                evolvesFrom(evolvesFrom),
                evolvesTo(evolvesTo),
                hasRuleText(ruleText)
        );
    }
}
