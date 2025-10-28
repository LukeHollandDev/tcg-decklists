package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for Pokemon Card entities.
 * Extends JpaSpecificationExecutor to enable dynamic query building with Specifications.
 */
@org.springframework.stereotype.Repository
public interface Repository extends JpaRepository<Card, String>, JpaSpecificationExecutor<Card> {

    /**
     * Find all distinct supertypes (Pokemon, Trainer, Energy)
     *
     * @return List of distinct supertype names
     */
    @Query("SELECT DISTINCT c.supertype FROM Card c WHERE c.supertype IS NOT NULL ORDER BY c.supertype")
    List<String> findDistinctSupertypes();

    /**
     * Find all distinct Pokemon types (Fire, Water, Grass, etc.)
     *
     * @return List of distinct type names
     */
    @Query("SELECT DISTINCT t.name FROM Type t ORDER BY t.name")
    List<String> findDistinctTypes();

    /**
     * Find all distinct subtypes (Basic, Stage 1, ex, V, Item, etc.)
     *
     * @return List of distinct subtype names
     */
    @Query("SELECT DISTINCT s.name FROM Subtype s ORDER BY s.name")
    List<String> findDistinctSubtypes();

    /**
     * Find all distinct set identifiers (base1, swsh8, etc.)
     *
     * @return List of distinct set IDs
     */
    @Query("SELECT DISTINCT s.setId FROM Set s WHERE s.setId IS NOT NULL ORDER BY s.setId")
    List<String> findDistinctSetIds();

    /**
     * Find all distinct rarities (Common, Uncommon, Rare, etc.)
     *
     * @return List of distinct rarity names
     */
    @Query("SELECT DISTINCT r.name FROM Rarity r ORDER BY r.name")
    List<String> findDistinctRarities();

    /**
     * Find all distinct format names (Standard, Expanded, Unlimited)
     *
     * @return List of distinct format names
     */
    @Query("SELECT DISTINCT f.name FROM Format f ORDER BY f.name")
    List<String> findDistinctFormats();

    /**
     * Find all distinct regulation marks (A, B, C, D, E, F, G, H, etc.)
     *
     * @return List of distinct regulation marks
     */
    @Query("SELECT DISTINCT c.regulationMark FROM Card c WHERE c.regulationMark IS NOT NULL ORDER BY c.regulationMark")
    List<String> findDistinctRegulationMarks();
}