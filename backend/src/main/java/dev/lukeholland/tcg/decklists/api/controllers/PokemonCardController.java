package dev.lukeholland.tcg.decklists.api.controllers;

import dev.lukeholland.tcg.decklists.api.dto.pokemon.PokemonCardDTO;
import dev.lukeholland.tcg.decklists.api.services.PokemonCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card/pokemon")
public class PokemonCardController {

    private final PokemonCardService pokemonCardService;

    public PokemonCardController(PokemonCardService pokemonCardService) {
        this.pokemonCardService = pokemonCardService;
    }

    /**
     * Get a Pokemon card by its ID
     * GET /api/card/pokemon/{id}
     *
     * @param id the card ID
     * @return 200 OK with the card DTO if found, 404 Not Found otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<PokemonCardDTO> getCardById(@PathVariable String id) {
        return pokemonCardService.findById(id)
                .map(PokemonCardDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Check if a card exists by ID
     * HEAD /api/card/pokemon/{id}
     *
     * @param id the card ID
     * @return 200 OK if exists, 404 Not Found otherwise
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkCardExists(@PathVariable String id) {
        if (pokemonCardService.existsById(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}