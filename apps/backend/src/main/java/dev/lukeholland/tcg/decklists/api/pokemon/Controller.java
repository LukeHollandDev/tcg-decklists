package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardResponse;
import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardSearchRequest;
import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardSearchResponse;
import dev.lukeholland.tcg.decklists.api.pokemon.dto.FilterOptionsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pokemon")
public class Controller {

    private final Service service;

    public Controller(Service pokemonCardService) {
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
     * Check if a card exists by ID
     * HEAD /api/pokemon/{id}
     *
     * @param id the card ID
     * @return 200 OK if exists, 404 Not Found otherwise
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkCardExists(@PathVariable String id) {
        if (service.existsById(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Search for Pokemon cards with filters, pagination, and sorting
     * GET /api/pokemon/search
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
}