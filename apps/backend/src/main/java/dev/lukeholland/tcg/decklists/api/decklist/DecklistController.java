package dev.lukeholland.tcg.decklists.api.decklist;

import dev.lukeholland.tcg.decklists.api.common.exception.EntityNotFoundException;
import dev.lukeholland.tcg.decklists.api.decklist.dto.DecklistRequest;
import dev.lukeholland.tcg.decklists.api.decklist.dto.DecklistResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/decklist")
@Tag(name = "Decklists", description = "Endpoints for creating and retrieving decklists")
public class DecklistController {

    private final DecklistService decklistService;

    public DecklistController(DecklistService decklistService) {
        this.decklistService = decklistService;
    }

    @Operation(summary = "Create a new decklist",
            description = "Creates a decklist with a name, card game type, and list of card IDs (duplicates allowed)")
    @PostMapping
    public ResponseEntity<Map<String, Integer>> createDecklist(
            @Parameter(description = "Decklist creation request") @Valid @RequestBody DecklistRequest request) {
        var decklist = decklistService.createDecklist(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", decklist.getId()));
    }

    @Operation(summary = "Get decklist by ID")
    @GetMapping("/{id}")
    public DecklistResponse getDecklistById(
            @Parameter(description = "Decklist ID") @PathVariable Integer id) {
        return decklistService.findById(id)
                .map(DecklistResponse::new)
                .orElseThrow(() -> new EntityNotFoundException("Decklist", id.toString()));
    }
}
