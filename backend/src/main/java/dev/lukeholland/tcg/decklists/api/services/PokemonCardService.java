package dev.lukeholland.tcg.decklists.api.services;

import org.springframework.stereotype.Service;

@Service
public class PokemonCardService {
//    private final PokemonCardRepository repository;

    public PokemonCardService() {
//        this.repository = repository;
    }

    public String search(String filter) {
        return "Hello, " + filter + "!";
    }
}
