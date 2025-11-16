package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.dto.*;
import dev.lukeholland.tcg.decklists.api.pokemon.entities.Card;
import dev.lukeholland.tcg.decklists.api.pokemon.enums.AutocompleteField;
import dev.lukeholland.tcg.decklists.api.pokemon.specifications.CardSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class PokemonCardService {

    private final PokemonCardRepository repository;

    public PokemonCardService(PokemonCardRepository pokemonCardRepository) {
        this.repository = pokemonCardRepository;
    }

    public Optional<Card> findById(String id) {
        return repository.findById(id);
    }

    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    public CardSearchResponse search(CardSearchRequest request) {
        Specification<Card> spec = CardSpecification.buildSpecification(
                request.name(),
                request.supertype(),
                request.types(),
                request.typesMatchAll(),
                request.subtypes(),
                request.subtypesMatchAll(),
                request.setId(),
                request.rarity(),
                request.hpMin(),
                request.hpMax(),
                request.attackName(),
                request.attackText(),
                request.attackDamageMin(),
                request.attackDamageMax(),
                request.attackCost(),
                request.attackCostMatchAll(),
                request.hasAbility(),
                request.abilityName(),
                request.abilityText(),
                request.artist(),
                request.regulationMark(),
                request.retreatCostMin(),
                request.retreatCostMax(),
                request.formats(),
                request.formatsMatchAll(),
                request.formatsBanned(),
                request.formatsBannedMatchAll(),
                request.hasRuleBox(),
                request.hasWeakness(),
                request.hasResistance(),
                request.weaknessType(),
                request.weaknessTypeMatchAll(),
                request.resistanceType(),
                request.resistanceTypeMatchAll(),
                request.evolvesFrom(),
                request.evolvesTo(),
                request.ruleText(),
                request.q(),
                request.excludeName(),
                request.excludeAttacks(),
                request.excludeAbilities(),
                request.excludeRules(),
                request.excludeArtist()
        );

        Sort sort = buildSort(request.sortBy(), request.sortOrder());
        Pageable pageable = PageRequest.of(request.page(), request.pageSize(), sort);
        Page<Card> cardPage = repository.findAll(spec, pageable);
        Page<CardResponse> responsePage = cardPage.map(CardResponse::new);

        return new CardSearchResponse(responsePage);
    }

    public FilterOptionsResponse getFilterOptions() {
        FilterOptionsResponse response = new FilterOptionsResponse();

        StaticFilterData staticData = new StaticFilterData(
                repository.findDistinctSupertypes(),
                repository.findDistinctTypes(),
                repository.findDistinctSubtypes(),
                repository.findDistinctSetIds(),
                repository.findDistinctRarities(),
                repository.findDistinctFormats(),
                repository.findDistinctRegulationMarks()
        );
        response.setStaticData(staticData);

        // Build filter metadata map
        Map<String, FilterMetadata> filters = new LinkedHashMap<>();

        // Phase 1 - Core Filters
        filters.put("name", FilterMetadata.string("name", "Card name search", true));
        filters.put("supertype", FilterMetadata.enumFilter("supertype", "Card supertype", "static.supertypes"));
        filters.put("types", FilterMetadata.multiselect("types", "Pokémon types", "static.types", "typesMatchAll"));
        filters.put("subtypes", FilterMetadata.multiselect("subtypes", "Card subtypes", "static.subtypes", "subtypesMatchAll"));
        filters.put("setId", FilterMetadata.enumFilter("setId", "Set identifier", "static.sets"));
        filters.put("rarity", FilterMetadata.enumFilter("rarity", "Rarity", "static.rarities"));
        filters.put("hp", FilterMetadata.range("Hit points", "hpMin", "hpMax"));

        // Phase 2 - Attack Filters
        filters.put("attackName", FilterMetadata.string("attackName", "Attack name search", true));
        filters.put("attackText", FilterMetadata.string("attackText", "Attack text/description search", false));
        filters.put("attackDamage", FilterMetadata.range("Attack damage", "attackDamageMin", "attackDamageMax"));
        filters.put("attackCost", FilterMetadata.multiselect("attackCost", "Attack cost types", "static.types", "attackCostMatchAll"));

        // Phase 2 - Ability Filters
        filters.put("hasAbility", FilterMetadata.booleanFilter("hasAbility", "Has ability"));
        filters.put("abilityName", FilterMetadata.string("abilityName", "Ability name search", true));
        filters.put("abilityText", FilterMetadata.string("abilityText", "Ability text/description search", false));

        // Phase 2 - Detail Filters
        filters.put("artist", FilterMetadata.string("artist", "Artist name", true));
        filters.put("regulationMark", FilterMetadata.enumFilter("regulationMark", "Regulation mark", "static.regulationMarks"));
        filters.put("retreatCost", FilterMetadata.range("Retreat cost", "retreatCostMin", "retreatCostMax"));
        filters.put("formats", FilterMetadata.multiselect("formats", "Format legality", "static.formats", "formatsMatchAll"));
        filters.put("formatsBanned", FilterMetadata.multiselect("formatsBanned", "Formats where cards are banned", "static.formats", "formatsBannedMatchAll"));

        // Phase 3 - Boolean Filters
        filters.put("hasRuleBox", FilterMetadata.booleanFilter("hasRuleBox", "Has rule box"));
        filters.put("hasWeakness", FilterMetadata.booleanFilter("hasWeakness", "Has weakness"));
        filters.put("hasResistance", FilterMetadata.booleanFilter("hasResistance", "Has resistance"));

        // Phase 3 - Weakness/Resistance Type Filters
        filters.put("weaknessType", FilterMetadata.multiselect("weaknessType", "Weakness types", "static.types", "weaknessTypeMatchAll"));
        filters.put("resistanceType", FilterMetadata.multiselect("resistanceType", "Resistance types", "static.types", "resistanceTypeMatchAll"));

        // Phase 3 - Evolution & Rule Filters
        filters.put("evolvesFrom", FilterMetadata.string("evolvesFrom", "Evolution source", true));
        filters.put("evolvesTo", FilterMetadata.string("evolvesTo", "Evolution target", true));
        filters.put("ruleText", FilterMetadata.string("ruleText", "Rule text/description search", false));

        response.setFilters(filters);

        return response;
    }

    public List<String> autocomplete(AutocompleteField field, String query, int limit) {
        return switch (field) {
            case ARTIST -> autocomplete(
                    query,
                    limit,
                    repository::findArtistNamesByPrefix,
                    repository::findArtistNamesBySubstring
            );
            case ATTACK -> autocomplete(
                    query,
                    limit,
                    repository::findAttackNamesByPrefix,
                    repository::findAttackNamesBySubstring
            );
            case ABILITY -> autocomplete(
                    query,
                    limit,
                    repository::findAbilityNamesByPrefix,
                    repository::findAbilityNamesBySubstring
            );
            case SET -> autocomplete(
                    query,
                    limit,
                    repository::findSetNamesByPrefix,
                    repository::findSetNamesBySubstring
            );
        };
    }

    /**
     * Autocomplete implementation with prefix-first matching strategy.
     * Algorithm:
     * 1. Query for prefix matches up to the limit
     * 2. If result count < limit, query for substring matches to fill remaining slots
     * 3. Deduplicate using LinkedHashSet to maintain order (prefix matches first)
     * 4. Return list limited to requested size
     */
    private List<String> autocomplete(
            String query,
            int limit,
            AutocompleteFinder prefixFinder,
            AutocompleteFinder substringFinder) {

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String normalizedQuery = query.trim();
        List<String> prefixMatches = prefixFinder.find(normalizedQuery, limit);

        if (prefixMatches.size() >= limit) {
            return prefixMatches;
        }

        int remaining = limit - prefixMatches.size();
        List<String> substringMatches = substringFinder.find(normalizedQuery, remaining);

        Set<String> results = new LinkedHashSet<>(prefixMatches);
        results.addAll(substringMatches);

        return new ArrayList<>(results).subList(0, Math.min(results.size(), limit));
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null && !sortBy.trim().isEmpty() ? sortBy : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, field);
    }

    /**
     * Functional interface for autocomplete finder methods.
     * Allows method reference usage for cleaner code.
     */
    @FunctionalInterface
    private interface AutocompleteFinder {
        List<String> find(String query, int limit);
    }
}