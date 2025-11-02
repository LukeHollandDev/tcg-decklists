package dev.lukeholland.tcg.decklists.api.pokemon.enums;

public enum AutocompleteField {
    ARTIST,
    ATTACK,
    ABILITY,
    SET;

    /**
     * Parse a field name from a path parameter.
     * Accepts singular lowercase strings: "artist", "attack", "ability", "set"
     *
     * @param field the field name from the path parameter
     * @return the corresponding AutocompleteField
     * @throws IllegalArgumentException if the field is not recognized
     */
    public static AutocompleteField fromString(String field) {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("Field cannot be null or blank");
        }

        return switch (field.toLowerCase().trim()) {
            case "artist" -> ARTIST;
            case "attack" -> ATTACK;
            case "ability" -> ABILITY;
            case "set" -> SET;
            default -> throw new IllegalArgumentException("Invalid autocomplete field: " + field);
        };
    }
}
