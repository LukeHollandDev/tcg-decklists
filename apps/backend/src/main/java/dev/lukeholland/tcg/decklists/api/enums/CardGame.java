package dev.lukeholland.tcg.decklists.api.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of supported trading card games.
 * Used to ensure type safety when creating and managing decklists.
 */
public enum CardGame {
    POKEMON("pokemon");

    private final String value;

    CardGame(String value) {
        this.value = value;
    }

    /**
     * Parse a string value to a CardGame enum.
     * Case-insensitive matching for better API usability.
     *
     * @param value the string value to parse
     * @return the matching CardGame enum
     * @throws IllegalArgumentException if the value doesn't match any card game
     */
    public static CardGame fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Card game type cannot be null");
        }

        for (CardGame game : CardGame.values()) {
            if (game.value.equalsIgnoreCase(value)) {
                return game;
            }
        }

        throw new IllegalArgumentException(
                "Invalid card game type: '" + value + "'. Supported values: " + getSupportedValues()
        );
    }

    /**
     * Get a comma-separated list of supported card game values.
     *
     * @return supported values as a string
     */
    public static String getSupportedValues() {
        StringBuilder sb = new StringBuilder();
        for (CardGame game : CardGame.values()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(game.value);
        }
        return sb.toString();
    }

    /**
     * Get the string value of the card game (used for JSON serialization and database storage).
     *
     * @return the lowercase string representation
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
