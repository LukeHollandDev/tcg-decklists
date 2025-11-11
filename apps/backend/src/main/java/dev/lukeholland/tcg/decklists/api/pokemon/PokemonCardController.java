package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.dto.*;
import dev.lukeholland.tcg.decklists.api.pokemon.enums.AutocompleteField;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonCardController {

    private final PokemonCardService service;

    public PokemonCardController(PokemonCardService pokemonCardService) {
        this.service = pokemonCardService;
    }

    /**
     * Get a Pokémon card by its ID
     * GET /api/pokemon/{id}
     *
     * @param id the card ID
     * @return 200 OK with the card DTO if found, 404 Not Found otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable String id) {
        return service.findById(id)
                .map(CardResponse::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search for Pokemon cards with filters, pagination, and sorting
     * GET /api/pokemon/search
     * <p>
     * Query parameters (Generic Search):
     * - q: Generic search term across multiple fields (name, attacks, abilities, rules, artist)
     * - excludeName: Exclude name from generic search (default: false)
     * - excludeAttacks: Exclude attack names/text from generic search (default: false)
     * - excludeAbilities: Exclude ability names/text from generic search (default: false)
     * - excludeRules: Exclude rule text from generic search (default: false)
     * - excludeArtist: Exclude artist name from generic search (default: false)
     * <p>
     * Query parameters (Phase 1):
     * - name: Card name search (partial match)
     * - supertype: Card supertype (Pokemon, Trainer, Energy)
     * - types: Pokemon types (can specify multiple, e.g., types=Fire&types=Water)
     * - typesMatchAll: If true, cards must have ALL specified types (AND logic). Default: false (OR logic)
     * - subtypes: Card subtypes (can specify multiple, e.g., subtypes=ex&subtypes=V)
     * - subtypesMatchAll: If true, cards must have ALL specified subtypes (AND logic). Default: false (OR logic)
     * - setId: Set identifier (e.g., "base1")
     * - rarity: Rarity name
     * - hpMin: Minimum HP value
     * - hpMax: Maximum HP value
     * <p>
     * Query parameters (Phase 2 - Attack Filters):
     * - attackName: Attack name search (partial match)
     * - attackText: Attack text/description search (partial match)
     * - attackDamageMin: Minimum attack damage value
     * - attackDamageMax: Maximum attack damage value
     * - attackCost: Attack cost types (can specify multiple, e.g., attackCost=Fire&attackCost=Colorless)
     * - attackCostMatchAll: If true, attacks must have ALL specified cost types (AND logic). Default: false (OR logic)
     * <p>
     * Query parameters (Phase 2 - Ability Filters):
     * - hasAbility: Filter by ability presence (true=cards with abilities, false=cards without abilities)
     * - abilityName: Ability name search (partial match)
     * - abilityText: Ability text/description search (partial match)
     * <p>
     * Query parameters (Phase 2 - Detail Filters):
     * - artist: Artist name (exact match)
     * - regulationMark: Regulation mark (A, B, C, D, E, F, G, H)
     * - retreatCostMin: Minimum retreat cost value
     * - retreatCostMax: Maximum retreat cost value
     * - formats: Format legality (can specify multiple, e.g., formats=Standard&formats=Expanded)
     * - formatsMatchAll: If true, cards must be legal in ALL specified formats (AND logic). Default: false (OR logic)
     * - formatsBanned: Formats where cards are BANNED (can specify multiple, e.g., formatsBanned=Standard)
     * - formatsBannedMatchAll: If true, cards must be banned in ALL specified formats (AND logic). Default: false (OR logic)
     * <p>
     * Pagination & Sorting:
     * - page: Page number (0-indexed, default: 0)
     * - pageSize: Results per page (default: 20, max: 100)
     * - sortBy: Field to sort by (default: "name")
     * - sortOrder: Sort order "asc" or "desc" (default: "asc")
     * <p>
     * Examples:
     * - Generic search for "pikachu": /search?q=pikachu
     * - Generic search excluding artist: /search?q=thunderbolt&excludeArtist=true
     * - Generic search only in name and attacks: /search?q=fire&excludeAbilities=true&excludeRules=true&excludeArtist=true
     * - Fire OR Water types: /search?types=Fire&types=Water
     * - Fire AND Grass types: /search?types=Fire&types=Grass&typesMatchAll=true
     * - Attacks with Fire OR Colorless cost: /search?attackCost=Fire&attackCost=Colorless
     * - Search attack text for "draw": /search?attackText=draw
     * - Search ability text for "damage": /search?abilityText=damage
     * - Legal in Standard AND Expanded: /search?formats=Standard&formats=Expanded&formatsMatchAll=true
     * - Banned in Standard: /search?formatsBanned=Standard
     * - Legal in Expanded but banned in Standard: /search?formats=Expanded&formatsBanned=Standard
     * - Cards by specific artist: /search?artist=Ken Sugimori
     * - High damage attacks: /search?attackDamageMin=100
     * - Generic search with filters: /search?q=charizard&types=Fire&rarity=Rare
     *
     * @param request Search request parameters
     * @return 200 OK with paginated search results
     */
    @GetMapping("/search")
    public ResponseEntity<CardSearchResponse> searchCards(@ModelAttribute CardSearchRequest request) {
        CardSearchResponse response = service.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get available filter options for search
     * GET /api/pokemon/features
     * <p>
     * Returns distinct values for all filterable fields to help build search UI
     *
     * @return 200 OK with available filter options
     */
    @GetMapping("/features")
    public ResponseEntity<FilterOptionsResponse> getFeatures() {
        FilterOptionsResponse response = service.getFilterOptions();
        return ResponseEntity.ok(response);
    }

    // ========== Autocomplete Endpoint ==========

    /**
     * Autocomplete endpoint for search-enabled dropdowns
     * GET /api/pokemon/autocomplete/{field}?query={text}&limit={n}
     * <p>
     * Supports prefix-first matching for better relevance:
     * - Prioritizes results starting with the query
     * - Falls back to substring matching if needed
     * <p>
     * Path parameters:
     * - field: The field to autocomplete (artist, attack, ability, set)
     * <p>
     * Query parameters:
     * - query: Search text (required, min length: 1)
     * - limit: Maximum results (optional, default: 10, max: 50)
     * <p>
     * Examples:
     * - /api/pokemon/autocomplete/artist?query=ken
     * - /api/pokemon/autocomplete/attack?query=fire&limit=20
     * - /api/pokemon/autocomplete/ability?query=intimidate
     * - /api/pokemon/autocomplete/set?query=base
     *
     * @param field The autocomplete field (artist, attack, ability, set)
     * @param query Search query text
     * @param limit Maximum number of results (default: 10, max: 50)
     * @return 200 OK with autocomplete results, or 400 Bad Request if query is missing/empty or field is invalid
     */
    @GetMapping("/autocomplete/{field}")
    public ResponseEntity<AutocompleteResponse> autocomplete(
            @PathVariable String field,
            @RequestParam(required = true) String query,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        // Validate and parse field
        AutocompleteField autocompleteField;
        try {
            autocompleteField = AutocompleteField.fromString(field);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        // Validate query
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Enforce max limit of 50
        int effectiveLimit = Math.min(limit, 50);

        // Call service
        List<String> results = service.autocomplete(autocompleteField, query, effectiveLimit);

        // Build response
        AutocompleteResponse response = new AutocompleteResponse(results, query, effectiveLimit);
        return ResponseEntity.ok(response);
    }
}