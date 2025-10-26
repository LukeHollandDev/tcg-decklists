package dev.lukeholland.tcg.decklists.api.repositories;

import dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PokemonCardRepository extends JpaRepository<PokemonCard, String> {
}