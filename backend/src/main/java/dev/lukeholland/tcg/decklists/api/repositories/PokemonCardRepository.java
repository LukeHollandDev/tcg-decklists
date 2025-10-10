package dev.lukeholland.tcg.decklists.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard;

@Repository
public interface PokemonCardRepository extends JpaRepository<PokemonCard, String> {
    PokemonCard findByName(String name);
}
