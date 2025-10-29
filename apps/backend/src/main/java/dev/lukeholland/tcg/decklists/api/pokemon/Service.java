package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardResponse;
import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardSearchRequest;
import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardSearchResponse;
import dev.lukeholland.tcg.decklists.api.pokemon.dto.FilterOptionsResponse;
import dev.lukeholland.tcg.decklists.api.pokemon.entities.Card;
import dev.lukeholland.tcg.decklists.api.pokemon.specifications.CardSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class Service {

    private final Repository repository;

    public Service(Repository pokemonCardRepository) {
        this.repository = pokemonCardRepository;
    }

    /**
     * Find a Pokémon card by its ID
     *
     * @param id the card ID
     * @return Optional containing the card if found, empty otherwise
     */
    public Optional<Card> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Check if a card exists by ID
     *
     * @param id the card ID
     * @return true if the card exists, false otherwise
     */
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    /**
     * Search for cards based on filter criteria with pagination and sorting.
     *
     * @param request Search request containing filter parameters, pagination, and sorting
     * @return CardSearchResponse containing paginated results and metadata
     */
    public CardSearchResponse search(CardSearchRequest request) {
        // Build the Specification from request parameters
        Specification<Card> spec = CardSpecification.buildSpecification(
                request.getName(),
                request.getSupertype(),
                request.getTypes(),
                request.getTypesMatchAll(),
                request.getSubtypes(),
                request.getSubtypesMatchAll(),
                request.getSetId(),
                request.getRarity(),
                request.getHpMin(),
                request.getHpMax(),
                request.getAttackName(),
                request.getAttackText(),
                request.getAttackDamageMin(),
                request.getAttackDamageMax(),
                request.getAttackCost(),
                request.getAttackCostMatchAll(),
                request.getHasAbility(),
                request.getAbilityName(),
                request.getAbilityText(),
                request.getArtist(),
                request.getRegulationMark(),
                request.getRetreatCostMin(),
                request.getRetreatCostMax(),
                request.getFormats(),
                request.getFormatsMatchAll(),
                request.getFormatsBanned(),
                request.getFormatsBannedMatchAll(),
                request.getHasRuleBox(),
                request.getHasWeakness(),
                request.getHasResistance(),
                request.getWeaknessType(),
                request.getWeaknessTypeMatchAll(),
                request.getResistanceType(),
                request.getResistanceTypeMatchAll(),
                request.getEvolvesFrom(),
                request.getEvolvesTo(),
                request.getRuleText()
        );

        // Build Sort object from request
        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());

        // Create Pageable for pagination
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                sort
        );

        // Execute query with specification and pagination
        Page<Card> cardPage = repository.findAll(spec, pageable);

        // Transform Card entities to CardResponse DTOs
        Page<CardResponse> responsePage = cardPage.map(CardResponse::new);

        // Wrap in CardSearchResponse with pagination metadata
        return new CardSearchResponse(responsePage);
    }

    /**
     * Get available filter options for building search UI.
     * Returns distinct values for types, subtypes, sets, rarities, etc.
     *
     * @return FilterOptionsResponse containing all available filter values
     */
    public FilterOptionsResponse getFilterOptions() {
        FilterOptionsResponse response = new FilterOptionsResponse();

        // Query for distinct values from database
        response.setSupertypes(repository.findDistinctSupertypes());
        response.setTypes(repository.findDistinctTypes());
        response.setSubtypes(repository.findDistinctSubtypes());
        response.setSets(repository.findDistinctSetIds());
        response.setRarities(repository.findDistinctRarities());
        response.setFormats(repository.findDistinctFormats());
        response.setRegulationMarks(repository.findDistinctRegulationMarks());

        return response;
    }

    // ========== Autocomplete Methods ==========

    /**
     * Autocomplete artist names with prefix-first matching.
     * Returns prefix matches first, then substring matches to fill remaining slots.
     *
     * @param query The search query
     * @param limit Maximum number of results (default: 10, max: 100)
     * @return List of matching artist names
     */
    public List<String> autocompleteArtists(String query, int limit) {
        return autocomplete(
                query,
                limit,
                repository::findArtistNamesByPrefix,
                repository::findArtistNamesBySubstring
        );
    }

    /**
     * Autocomplete attack names with prefix-first matching.
     * Returns prefix matches first, then substring matches to fill remaining slots.
     *
     * @param query The search query
     * @param limit Maximum number of results (default: 10, max: 100)
     * @return List of matching attack names
     */
    public List<String> autocompleteAttacks(String query, int limit) {
        return autocomplete(
                query,
                limit,
                repository::findAttackNamesByPrefix,
                repository::findAttackNamesBySubstring
        );
    }

    /**
     * Autocomplete ability names with prefix-first matching.
     * Returns prefix matches first, then substring matches to fill remaining slots.
     *
     * @param query The search query
     * @param limit Maximum number of results (default: 10, max: 100)
     * @return List of matching ability names
     */
    public List<String> autocompleteAbilities(String query, int limit) {
        return autocomplete(
                query,
                limit,
                repository::findAbilityNamesByPrefix,
                repository::findAbilityNamesBySubstring
        );
    }

    /**
     * Autocomplete card names with prefix-first matching.
     * Returns prefix matches first, then substring matches to fill remaining slots.
     *
     * @param query The search query
     * @param limit Maximum number of results (default: 10, max: 100)
     * @return List of matching card names
     */
    public List<String> autocompleteCardNames(String query, int limit) {
        return autocomplete(
                query,
                limit,
                repository::findCardNamesByPrefix,
                repository::findCardNamesBySubstring
        );
    }

    /**
     * Generic autocomplete implementation with prefix-first matching strategy.
     * <p>
     * Algorithm:
     * 1. Query for prefix matches up to the limit
     * 2. If result count < limit, query for substring matches to fill remaining slots
     * 3. Deduplicate using LinkedHashSet to maintain order (prefix matches first)
     * 4. Return list limited to requested size
     *
     * @param query           The search query
     * @param limit           Maximum number of results
     * @param prefixFinder    Function to find prefix matches
     * @param substringFinder Function to find substring matches
     * @return List of matching names with prefix matches prioritized
     */
    private List<String> autocomplete(
            String query,
            int limit,
            AutocompleteFinder prefixFinder,
            AutocompleteFinder substringFinder) {

        // Validate query
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String normalizedQuery = query.trim();

        // Step 1: Get prefix matches first (these are most relevant)
        List<String> prefixMatches = prefixFinder.find(normalizedQuery, limit);

        // If we have enough results from prefix matching, return early
        if (prefixMatches.size() >= limit) {
            return prefixMatches;
        }

        // Step 2: Get substring matches to fill remaining slots
        int remaining = limit - prefixMatches.size();
        List<String> substringMatches = substringFinder.find(normalizedQuery, remaining);

        // Step 3: Combine results with deduplication (LinkedHashSet maintains insertion order)
        Set<String> results = new LinkedHashSet<>(prefixMatches);
        results.addAll(substringMatches);

        // Step 4: Convert to list and ensure we don't exceed limit
        return new ArrayList<>(results).subList(0, Math.min(results.size(), limit));
    }

    /**
     * Build a Sort object from sort field and order.
     *
     * @param sortBy    Field to sort by
     * @param sortOrder Sort order ("asc" or "desc")
     * @return Sort object for query
     */
    private Sort buildSort(String sortBy, String sortOrder) {
        // Default to sorting by name ascending
        String field = sortBy != null && !sortBy.trim().isEmpty() ? sortBy : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Map sort field names (some may need special handling)
        // For now, use the field directly - could add mapping logic here if needed
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