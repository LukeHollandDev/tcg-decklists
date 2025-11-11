package dev.lukeholland.tcg.decklists.api.pokemon.util;

/**
 * Constants for SQL query fragments used across repository methods.
 * Centralizes accent-normalization character mappings to reduce duplication.
 * <p>
 * Use these constants in @Query annotations by referencing them in TRANSLATE expressions.
 * Since @Query requires compile-time constants, use these in text blocks like:
 * <pre>
 * {@code @Query("""
 *     SELECT a.name FROM Artist a
 *     WHERE LOWER(TRANSLATE(a.name, '%s', '%s')) LIKE ...
 *     """.formatted(QueryConstants.ACCENT_SOURCE, QueryConstants.ACCENT_TARGET))
 * }</pre>
 */
public class QueryConstants {

    /**
     * Source characters for TRANSLATE function - accented characters to be normalized.
     * Contains lowercase and uppercase accented characters that should be converted to base forms.
     * <p>
     * Use in TRANSLATE(field, ACCENT_SOURCE, ACCENT_TARGET)
     */
    public static final String ACCENT_SOURCE =
            "áàâäãåāăąéèêëēėęíìîïīįóòôöõøōőúùûüūűųýÿŷñńňçćčßśšźżž" +
                    "ÁÀÂÄÃÅĀĂĄÉÈÊËĒĖĘÍÌÎÏĪĮÓÒÔÖÕØŌŐÚÙÛÜŪŰŲÝŸŶÑŃŇÇĆČßŚŠŹŻŽ";

    /**
     * Target characters for TRANSLATE function - base characters without accents.
     * Corresponds to ACCENT_SOURCE in the same order (each accented char maps to its base form).
     * <p>
     * Use in TRANSLATE(field, ACCENT_SOURCE, ACCENT_TARGET)
     */
    public static final String ACCENT_TARGET =
            "aaaaaaaaaeeeeeeeiiiiiiooooooooouuuuuuuyyynnncccssszzzs" +
                    "AAAAAAAAAEEEEEEEIIIIIIOOOOOOOOUUUUUUUYYYNNNCCCSSSZZZ";

    // Private constructor to prevent instantiation
    private QueryConstants() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    /**
     * Builds a PostgreSQL expression that normalizes a field for accent-insensitive comparison.
     * Utility method for programmatic query building (not for @Query annotations).
     *
     * @param fieldExpression The field or column expression to normalize (e.g., "a.name", "c.name")
     * @return SQL expression for normalized field comparison
     * <p>
     * Example:
     * <pre>
     * normalizeField("a.name")
     * // Returns: "LOWER(TRANSLATE(a.name, 'áàâ...', 'aaa...'))"
     * </pre>
     */
    public static String normalizeField(String fieldExpression) {
        return String.format(
                "LOWER(TRANSLATE(%s, '%s', '%s'))",
                fieldExpression,
                ACCENT_SOURCE,
                ACCENT_TARGET
        );
    }
}

