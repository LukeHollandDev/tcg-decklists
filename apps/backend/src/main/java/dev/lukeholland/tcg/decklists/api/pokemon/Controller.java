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
     * Query parameters:
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
     * - page: Page number (0-indexed, default: 0)
     * - pageSize: Results per page (default: 20, max: 100)
     * - sortBy: Field to sort by (default: "name")
     * - sortOrder: Sort order "asc" or "desc" (default: "asc")
     * <p>
     * Examples:
     * - Fire OR Water: /search?types=Fire&types=Water
     * - Fire AND Grass: /search?types=Fire&types=Grass&typesMatchAll=true
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