package dev.lukeholland.tcg.decklists.api.controllers;

import dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard;
import dev.lukeholland.tcg.decklists.api.repositories.PokemonCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/card/pokemon")
public class PokemonController {
    private final PokemonCardRepository pokemonCardRepository;

    @Autowired
    public PokemonController(PokemonCardRepository pokemonCardRepository) {
        this.pokemonCardRepository = pokemonCardRepository;
    }

    @GetMapping
    public ResponseEntity<PokemonCard> getCardByName(@RequestParam("id") String id) {
        Optional<PokemonCard> card = pokemonCardRepository.findById(id);
        return card.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
