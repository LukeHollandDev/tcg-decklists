package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Card;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class Service {

    private final Repository repository;

    public Service(Repository pokemonCardRepository) {
        this.repository = pokemonCardRepository;
    }

    /**
     * Find a Pokémon card by its ID
     *
     * @param id the card ID
     * @return Optional containing the card if found, empty otherwise
     */
    public Optional<Card> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Check if a card exists by ID
     *
     * @param id the card ID
     * @return true if the card exists, false otherwise
     */
    public boolean existsById(String id) {
        return repository.existsById(id);
    }
}