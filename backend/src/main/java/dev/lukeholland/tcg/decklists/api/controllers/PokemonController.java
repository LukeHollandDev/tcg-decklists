package dev.lukeholland.tcg.decklists.api.controllers;

import dev.lukeholland.tcg.decklists.api.services.PokemonCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card/pokemon")
public class PokemonController {
    private final PokemonCardService pokemonCardService;

    @Autowired
    public PokemonController(PokemonCardService pokemonCardService) {
        this.pokemonCardService = pokemonCardService;
    }

    @PostMapping("/search")
    public ResponseEntity<String> searchCards() {
        return ResponseEntity.ok(pokemonCardService.search("hello"));
    }
}
