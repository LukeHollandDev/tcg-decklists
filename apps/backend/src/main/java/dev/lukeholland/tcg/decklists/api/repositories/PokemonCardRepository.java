package dev.lukeholland.tcg.decklists.api.repositories;

import dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PokemonCardRepository extends JpaRepository<PokemonCard, String> {
    // The basic findById is inherited from JpaRepository
    // You can add custom query methods here if needed, for example:

    // List<PokemonCard> findByName(String name);
    // List<PokemonCard> findBySupertypeIgnoreCase(String supertype);
    // List<PokemonCard> findByPokemonSetId(Integer setId);
}