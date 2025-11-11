package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Card;
import dev.lukeholland.tcg.decklists.api.pokemon.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PokemonCardRepository extends JpaRepository<Card, String>, JpaSpecificationExecutor<Card> {

    @Query("SELECT DISTINCT c.supertype FROM Card c WHERE c.supertype IS NOT NULL ORDER BY c.supertype")
    List<String> findDistinctSupertypes();

    @Query("SELECT DISTINCT t.name FROM Type t ORDER BY t.name")
    List<String> findDistinctTypes();

    @Query("SELECT DISTINCT s.name FROM Subtype s ORDER BY s.name")
    List<String> findDistinctSubtypes();

    @Query("SELECT DISTINCT s.setId FROM Set s WHERE s.setId IS NOT NULL ORDER BY s.setId")
    List<String> findDistinctSetIds();

    @Query("SELECT DISTINCT r.name FROM Rarity r ORDER BY r.name")
    List<String> findDistinctRarities();

    @Query("SELECT DISTINCT f.name FROM Format f ORDER BY f.name")
    List<String> findDistinctFormats();

    @Query("SELECT DISTINCT c.regulationMark FROM Card c WHERE c.regulationMark IS NOT NULL ORDER BY c.regulationMark")
    List<String> findDistinctRegulationMarks();

    @Query(value = """
            SELECT DISTINCT a.name
            FROM Artist a
            WHERE LOWER(TRANSLATE(a.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            LIKE LOWER(CONCAT(:prefix, '%'))
                        ORDER BY a.name
                        LIMIT :limit
            """)
    List<String> findArtistNamesByPrefix(@Param("prefix") String prefix,
                                         @Param("limit") int limit);

    @Query(value = """
            SELECT DISTINCT a.name
            FROM Artist a
            WHERE LOWER(TRANSLATE(a.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            LIKE LOWER(CONCAT('%', :substring, '%'))
                        AND LOWER(TRANSLATE(a.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            NOT LIKE LOWER(CONCAT(:substring, '%'))
                        ORDER BY a.name
                        LIMIT :limit
            """)
    List<String> findArtistNamesBySubstring(@Param("substring") String substring,
                                            @Param("limit") int limit);

    @Query(value = """
            SELECT DISTINCT a.name
            FROM Attack a
            WHERE LOWER(TRANSLATE(a.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            LIKE LOWER(CONCAT(:prefix, '%'))
                        ORDER BY a.name
                        LIMIT :limit
            """)
    List<String> findAttackNamesByPrefix(@Param("prefix") String prefix,
                                         @Param("limit") int limit);

    @Query(value = """
            SELECT DISTINCT a.name
            FROM Attack a
            WHERE LOWER(TRANSLATE(a.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            LIKE LOWER(CONCAT('%', :substring, '%'))
                        AND LOWER(TRANSLATE(a.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            NOT LIKE LOWER(CONCAT(:substring, '%'))
                        ORDER BY a.name
                        LIMIT :limit
            """)
    List<String> findAttackNamesBySubstring(@Param("substring") String substring,
                                            @Param("limit") int limit);

    @Query(value = """
            SELECT DISTINCT a.name
            FROM Ability a
            WHERE LOWER(TRANSLATE(a.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            LIKE LOWER(CONCAT(:prefix, '%'))
                        ORDER BY a.name
                        LIMIT :limit
            """)
    List<String> findAbilityNamesByPrefix(@Param("prefix") String prefix,
                                          @Param("limit") int limit);

    @Query(value = """
            SELECT DISTINCT a.name
            FROM Ability a
            WHERE LOWER(TRANSLATE(a.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            LIKE LOWER(CONCAT('%', :substring, '%'))
                        AND LOWER(TRANSLATE(a.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            NOT LIKE LOWER(CONCAT(:substring, '%'))
                        ORDER BY a.name
                        LIMIT :limit
            """)
    List<String> findAbilityNamesBySubstring(@Param("substring") String substring,
                                             @Param("limit") int limit);

    @Query(value = """
            SELECT DISTINCT s.name
            FROM Set s
            WHERE s.name IS NOT NULL
            AND LOWER(TRANSLATE(s.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            LIKE LOWER(CONCAT(:prefix, '%'))
                        ORDER BY s.name
                        LIMIT :limit
            """)
    List<String> findSetNamesByPrefix(@Param("prefix") String prefix,
                                      @Param("limit") int limit);

    @Query(value = """
            SELECT DISTINCT s.name
            FROM Set s
            WHERE s.name IS NOT NULL
            AND LOWER(TRANSLATE(s.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            LIKE LOWER(CONCAT('%', :substring, '%'))
                        AND LOWER(TRANSLATE(s.name, '""" + QueryConstants.ACCENT_SOURCE + "', '" + QueryConstants.ACCENT_TARGET + """
            '))
                            NOT LIKE LOWER(CONCAT(:substring, '%'))
                        ORDER BY s.name
                        LIMIT :limit
            """)
    List<String> findSetNamesBySubstring(@Param("substring") String substring,
                                         @Param("limit") int limit);
}