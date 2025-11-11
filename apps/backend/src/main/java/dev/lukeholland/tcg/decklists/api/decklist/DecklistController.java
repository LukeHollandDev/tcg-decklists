package dev.lukeholland.tcg.decklists.api.decklist;

import dev.lukeholland.tcg.decklists.api.decklist.dto.DecklistRequest;
import dev.lukeholland.tcg.decklists.api.decklist.dto.DecklistResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/decklist")
public class DecklistController {

    private final DecklistService decklistService;

    public DecklistController(DecklistService decklistService) {
        this.decklistService = decklistService;
    }

    /**
     * Create a new decklist
     * POST /api/decklist
     * <p>
     * Request body should contain:
     * - name: Decklist name (required)
     * - type: Card game type (required)
     * - cards: List of card IDs, allowing duplicates (required, must not be empty)
     * <p>
     * Example:
     * {
     * "name": "My Fire Deck",
     * "type": "pokemon",
     * "cards": ["base1-4", "base1-4", "base1-46"]
     * }
     *
     * @param request The decklist creation request
     * @return 201 Created with the decklist ID, or 400 Bad Request if validation fails
     */
    @PostMapping
    public ResponseEntity<?> createDecklist(@RequestBody DecklistRequest request) {
        try {
            var decklist = decklistService.createDecklist(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of("id", decklist.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get a decklist by its ID
     * GET /api/decklist/{id}
     * <p>
     * Returns the complete decklist with all card IDs expanded by quantity.
     *
     * @param id The decklist ID
     * @return 200 OK with the decklist DTO if found, 404 Not Found otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<DecklistResponse> getDecklistById(@PathVariable Integer id) {
        return decklistService.findById(id)
                .map(DecklistResponse::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
