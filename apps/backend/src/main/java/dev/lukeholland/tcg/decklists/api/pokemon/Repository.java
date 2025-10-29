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

    // ========== Autocomplete Methods ==========

    /**
     * Find artist names starting with the given prefix (accent-insensitive, case-insensitive).
     *
     * @param prefix The prefix to search for
     * @param limit  Maximum number of results to return
     * @return List of artist names matching the prefix
     */
    @Query(value = """
            SELECT DISTINCT a.name
            FROM Artist a
            WHERE LOWER(TRANSLATE(a.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                LIKE LOWER(CONCAT(:prefix, '%'))
            ORDER BY a.name
            LIMIT :limit
            """)
    List<String> findArtistNamesByPrefix(@org.springframework.data.repository.query.Param("prefix") String prefix,
                                         @org.springframework.data.repository.query.Param("limit") int limit);

    /**
     * Find artist names containing the given substring (accent-insensitive, case-insensitive).
     * Excludes results that start with the substring to avoid duplicates with prefix search.
     *
     * @param substring The substring to search for
     * @param limit     Maximum number of results to return
     * @return List of artist names containing the substring
     */
    @Query(value = """
            SELECT DISTINCT a.name
            FROM Artist a
            WHERE LOWER(TRANSLATE(a.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                LIKE LOWER(CONCAT('%', :substring, '%'))
            AND LOWER(TRANSLATE(a.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                NOT LIKE LOWER(CONCAT(:substring, '%'))
            ORDER BY a.name
            LIMIT :limit
            """)
    List<String> findArtistNamesBySubstring(@org.springframework.data.repository.query.Param("substring") String substring,
                                            @org.springframework.data.repository.query.Param("limit") int limit);

    /**
     * Find attack names starting with the given prefix (accent-insensitive, case-insensitive).
     *
     * @param prefix The prefix to search for
     * @param limit  Maximum number of results to return
     * @return List of distinct attack names matching the prefix
     */
    @Query(value = """
            SELECT DISTINCT a.name
            FROM Attack a
            WHERE LOWER(TRANSLATE(a.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                LIKE LOWER(CONCAT(:prefix, '%'))
            ORDER BY a.name
            LIMIT :limit
            """)
    List<String> findAttackNamesByPrefix(@org.springframework.data.repository.query.Param("prefix") String prefix,
                                         @org.springframework.data.repository.query.Param("limit") int limit);

    /**
     * Find attack names containing the given substring (accent-insensitive, case-insensitive).
     * Excludes results that start with the substring to avoid duplicates with prefix search.
     *
     * @param substring The substring to search for
     * @param limit     Maximum number of results to return
     * @return List of distinct attack names containing the substring
     */
    @Query(value = """
            SELECT DISTINCT a.name
            FROM Attack a
            WHERE LOWER(TRANSLATE(a.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                LIKE LOWER(CONCAT('%', :substring, '%'))
            AND LOWER(TRANSLATE(a.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                NOT LIKE LOWER(CONCAT(:substring, '%'))
            ORDER BY a.name
            LIMIT :limit
            """)
    List<String> findAttackNamesBySubstring(@org.springframework.data.repository.query.Param("substring") String substring,
                                            @org.springframework.data.repository.query.Param("limit") int limit);

    /**
     * Find ability names starting with the given prefix (accent-insensitive, case-insensitive).
     *
     * @param prefix The prefix to search for
     * @param limit  Maximum number of results to return
     * @return List of distinct ability names matching the prefix
     */
    @Query(value = """
            SELECT DISTINCT a.name
            FROM Ability a
            WHERE LOWER(TRANSLATE(a.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                LIKE LOWER(CONCAT(:prefix, '%'))
            ORDER BY a.name
            LIMIT :limit
            """)
    List<String> findAbilityNamesByPrefix(@org.springframework.data.repository.query.Param("prefix") String prefix,
                                          @org.springframework.data.repository.query.Param("limit") int limit);

    /**
     * Find ability names containing the given substring (accent-insensitive, case-insensitive).
     * Excludes results that start with the substring to avoid duplicates with prefix search.
     *
     * @param substring The substring to search for
     * @param limit     Maximum number of results to return
     * @return List of distinct ability names containing the substring
     */
    @Query(value = """
            SELECT DISTINCT a.name
            FROM Ability a
            WHERE LOWER(TRANSLATE(a.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                LIKE LOWER(CONCAT('%', :substring, '%'))
            AND LOWER(TRANSLATE(a.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                NOT LIKE LOWER(CONCAT(:substring, '%'))
            ORDER BY a.name
            LIMIT :limit
            """)
    List<String> findAbilityNamesBySubstring(@org.springframework.data.repository.query.Param("substring") String substring,
                                             @org.springframework.data.repository.query.Param("limit") int limit);

    /**
     * Find card names starting with the given prefix (accent-insensitive, case-insensitive).
     *
     * @param prefix The prefix to search for
     * @param limit  Maximum number of results to return
     * @return List of distinct card names matching the prefix
     */
    @Query(value = """
            SELECT DISTINCT c.name
            FROM Card c
            WHERE LOWER(TRANSLATE(c.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                LIKE LOWER(CONCAT(:prefix, '%'))
            ORDER BY c.name
            LIMIT :limit
            """)
    List<String> findCardNamesByPrefix(@org.springframework.data.repository.query.Param("prefix") String prefix,
                                       @org.springframework.data.repository.query.Param("limit") int limit);

    /**
     * Find card names containing the given substring (accent-insensitive, case-insensitive).
     * Excludes results that start with the substring to avoid duplicates with prefix search.
     *
     * @param substring The substring to search for
     * @param limit     Maximum number of results to return
     * @return List of distinct card names containing the substring
     */
    @Query(value = """
            SELECT DISTINCT c.name
            FROM Card c
            WHERE LOWER(TRANSLATE(c.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                LIKE LOWER(CONCAT('%', :substring, '%'))
            AND LOWER(TRANSLATE(c.name,
                'áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżžÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ',
                'aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzAAAAAAAAAEEEEEEEIIIIIIOOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ'))
                NOT LIKE LOWER(CONCAT(:substring, '%'))
            ORDER BY c.name
            LIMIT :limit
            """)
    List<String> findCardNamesBySubstring(@org.springframework.data.repository.query.Param("substring") String substring,
                                          @org.springframework.data.repository.query.Param("limit") int limit);
}