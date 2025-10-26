package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card/pokemon")
public class Controller {

    private final Service service;

    public Controller(Service pokemonCardService) {
        this.service = pokemonCardService;
    }

    /**
     * Get a Pokémon card by its ID
     * GET /api/card/pokemon/{id}
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
     * HEAD /api/card/pokemon/{id}
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
}