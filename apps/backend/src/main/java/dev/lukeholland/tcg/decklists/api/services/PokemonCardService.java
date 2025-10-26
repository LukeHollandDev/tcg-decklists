package dev.lukeholland.tcg.decklists.api.services;

import dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard;
import dev.lukeholland.tcg.decklists.api.repositories.PokemonCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PokemonCardService {

    private final PokemonCardRepository pokemonCardRepository;

    public PokemonCardService(PokemonCardRepository pokemonCardRepository) {
        this.pokemonCardRepository = pokemonCardRepository;
    }

    /**
     * Find a Pokemon card by its ID
     * @param id the card ID
     * @return Optional containing the card if found, empty otherwise
     */
    public Optional<PokemonCard> findById(String id) {
        return pokemonCardRepository.findById(id);
    }

    /**
     * Check if a card exists by ID
     * @param id the card ID
     * @return true if the card exists, false otherwise
     */
    public boolean existsById(String id) {
        return pokemonCardRepository.existsById(id);
    }
}