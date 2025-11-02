package dev.lukeholland.tcg.decklists.api.decklist;

import dev.lukeholland.tcg.decklists.api.decklist.entities.Decklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Decklist entities.
 * Provides standard CRUD operations for decklists.
 */
@Repository
public interface DecklistRepository extends JpaRepository<Decklist, Integer> {
}
