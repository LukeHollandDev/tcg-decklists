package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.dto.*;
import dev.lukeholland.tcg.decklists.api.pokemon.enums.AutocompleteField;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
@Tag(name = "Pokemon Cards", description = "Endpoints for searching and retrieving Pokemon TCG card data")
public class PokemonCardController {

    private final PokemonCardService service;

    public PokemonCardController(PokemonCardService pokemonCardService) {
        this.service = pokemonCardService;
    }

    @Operation(summary = "Get a Pokémon card by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(
            @Parameter(description = "Card ID") @PathVariable String id) {
        return service.findById(id)
                .map(CardResponse::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search Pokemon cards",
            description = "Search for cards with comprehensive filters including name, type, HP, attacks, abilities, format legality, and more. Supports pagination and sorting.")
    @GetMapping("/search")
    public ResponseEntity<CardSearchResponse> searchCards(
            @Parameter(description = "Search filters, pagination, and sorting parameters") @ModelAttribute CardSearchRequest request) {
        CardSearchResponse response = service.search(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get available filter options",
            description = "Returns filter metadata and available values for building search UI")
    @GetMapping("/features")
    public ResponseEntity<FilterOptionsResponse> getFeatures() {
        FilterOptionsResponse response = service.getFilterOptions();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Autocomplete for search fields",
            description = "Provides autocomplete suggestions for artist, attack, ability, and set fields. Uses prefix-first matching strategy.")
    @GetMapping("/autocomplete/{field}")
    public ResponseEntity<AutocompleteResponse> autocomplete(
            @Parameter(description = "Field to autocomplete (artist, attack, ability, set)") @PathVariable String field,
            @Parameter(description = "Search query") @RequestParam(required = true) String query,
            @Parameter(description = "Maximum results (max: 50)") @RequestParam(required = false, defaultValue = "10") int limit) {

        AutocompleteField autocompleteField;
        try {
            autocompleteField = AutocompleteField.fromString(field);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        int effectiveLimit = Math.min(limit, 50);
        List<String> results = service.autocomplete(autocompleteField, query, effectiveLimit);
        AutocompleteResponse response = new AutocompleteResponse(results, query, effectiveLimit);
        return ResponseEntity.ok(response);
    }
}