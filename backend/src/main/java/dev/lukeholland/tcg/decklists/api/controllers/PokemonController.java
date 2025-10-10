package dev.lukeholland.tcg.decklists.api.controllers;

import dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard;
import dev.lukeholland.tcg.decklists.api.repositories.PokemonCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/card/pokemon")
public class PokemonController {
    private final PokemonCardRepository pokemonCardRepository;

    @Autowired
    public PokemonController(PokemonCardRepository pokemonCardRepository) {
        this.pokemonCardRepository = pokemonCardRepository;
    }

    @GetMapping
    public ResponseEntity<PokemonCard> getCardByName(@RequestParam("name") String name) {
        PokemonCard card = pokemonCardRepository.findByName(name);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(card);
    }
}

