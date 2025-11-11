package dev.lukeholland.tcg.decklists.api.decklist;

import dev.lukeholland.tcg.decklists.api.decklist.dto.DecklistRequest;
import dev.lukeholland.tcg.decklists.api.decklist.dto.DecklistResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/decklist")
@Tag(name = "Decklists", description = "Endpoints for creating and retrieving decklists")
public class DecklistController {

    private final DecklistService decklistService;

    public DecklistController(DecklistService decklistService) {
        this.decklistService = decklistService;
    }

    @Operation(summary = "Create a new decklist",
            description = "Creates a decklist with a name, card game type, and list of card IDs (duplicates allowed)")
    @PostMapping
    public ResponseEntity<?> createDecklist(
            @Parameter(description = "Decklist creation request") @RequestBody DecklistRequest request) {
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

    @Operation(summary = "Get decklist by ID")
    @GetMapping("/{id}")
    public ResponseEntity<DecklistResponse> getDecklistById(
            @Parameter(description = "Decklist ID") @PathVariable Integer id) {
        return decklistService.findById(id)
                .map(DecklistResponse::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
