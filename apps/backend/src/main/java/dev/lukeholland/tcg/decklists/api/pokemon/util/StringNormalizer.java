package dev.lukeholland.tcg.decklists.api.pokemon.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class for normalizing strings to support accent-insensitive searching.
 * Converts accented characters to their base forms (e.g., é → e, ñ → n).
 */
public class StringNormalizer {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Normalize a string by removing accents and converting to lowercase.
     * This enables accent-insensitive and case-insensitive searching.
     * <p>
     * Examples:
     * - "Pokémon" → "pokemon"
     * - "Flabébé" → "flabebe"
     * - "Piñata" → "pinata"
     * - "Müller" → "muller"
     *
     * @param input The string to normalize
     * @return Normalized string (lowercase, without accents), or null if input is null
     */
    public static String normalize(String input) {
        if (input == null) {
            return null;
        }

        // Step 1: Decompose accented characters into base + combining diacritical marks
        // Example: "é" becomes "e" + "´" (combining acute accent)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Step 2: Remove all combining diacritical marks
        // This removes the accent marks, leaving only the base characters
        normalized = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");

        // Step 3: Convert to lowercase for case-insensitive comparison
        return normalized.toLowerCase();
    }

    /**
     * Check if the input is null or empty after normalization.
     *
     * @param input The string to check
     * @return true if input is null or empty/blank after normalization
     */
    public static boolean isNullOrEmpty(String input) {
        return input == null || normalize(input).trim().isEmpty();
    }
}
