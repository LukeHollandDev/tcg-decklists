package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Resistance;
import dev.lukeholland.tcg.decklists.api.pokemon.entities.Type;

/**
 * Fluent builder for creating {@link Resistance} test entities.
 * <p>
 * Provides convenient factory methods for common resistance patterns
 * and supports full customization for test scenarios.
 * </p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a Psychic resistance with -30 modifier
 * Type psychicType = TypeBuilder.psychic().build();
 * Resistance resistance = ResistanceBuilder.aResistance()
 *     .withType(psychicType)
 *     .withValue("-30")
 *     .build();
 *
 * // Create a Fighting resistance with -20 modifier
 * Resistance resistance = ResistanceBuilder.minus20(
 *     TypeBuilder.fighting().build()
 * );
 * }</pre>
 */
public class ResistanceBuilder {

    private Type type;
    private String value;

    private ResistanceBuilder() {
    }

    /**
     * Creates a new ResistanceBuilder instance.
     *
     * @return a new ResistanceBuilder
     */
    public static ResistanceBuilder aResistance() {
        return new ResistanceBuilder();
    }

    /**
     * Creates a resistance with -10 modifier.
     *
     * @param type the resistance type
     * @return a ResistanceBuilder configured with -10 modifier
     */
    public static ResistanceBuilder minus10(Type type) {
        return aResistance()
                .withType(type)
                .withValue("-10");
    }

    /**
     * Creates a resistance with -20 modifier (most common).
     *
     * @param type the resistance type
     * @return a ResistanceBuilder configured with -20 modifier
     */
    public static ResistanceBuilder minus20(Type type) {
        return aResistance()
                .withType(type)
                .withValue("-20");
    }

    /**
     * Creates a resistance with -30 modifier (common in older sets).
     *
     * @param type the resistance type
     * @return a ResistanceBuilder configured with -30 modifier
     */
    public static ResistanceBuilder minus30(Type type) {
        return aResistance()
                .withType(type)
                .withValue("-30");
    }

    /**
     * Sets the resistance type.
     *
     * @param type the Type entity representing the resistance
     * @return this builder for method chaining
     */
    public ResistanceBuilder withType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the resistance value.
     *
     * @param value the resistance value (e.g., "-20", "-30")
     * @return this builder for method chaining
     */
    public ResistanceBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Builds the Resistance entity with configured values.
     *
     * @return a new Resistance instance
     */
    public Resistance build() {
        Resistance resistance = new Resistance();
        resistance.setType(type);
        resistance.setValue(value);
        return resistance;
    }
}
