package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Type;
import dev.lukeholland.tcg.decklists.api.pokemon.entities.Weakness;

/**
 * Fluent builder for creating {@link Weakness} test entities.
 * <p>
 * Provides convenient factory methods for common weakness patterns
 * and supports full customization for test scenarios.
 * </p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a Fire weakness with ×2 multiplier
 * Type fireType = TypeBuilder.fire().build();
 * Weakness weakness = WeaknessBuilder.aWeakness()
 *     .withType(fireType)
 *     .withValue("×2")
 *     .build();
 *
 * // Create a Water weakness with +20 modifier
 * Weakness weakness = WeaknessBuilder.aWeakness()
 *     .withType(TypeBuilder.water().build())
 *     .withValue("+20")
 *     .build();
 * }</pre>
 */
public class WeaknessBuilder {

    private Type type;
    private String value;

    private WeaknessBuilder() {
    }

    /**
     * Creates a new WeaknessBuilder instance.
     *
     * @return a new WeaknessBuilder
     */
    public static WeaknessBuilder aWeakness() {
        return new WeaknessBuilder();
    }

    /**
     * Creates a weakness with ×2 multiplier (common in modern sets).
     *
     * @param type the weakness type
     * @return a WeaknessBuilder configured with ×2 multiplier
     */
    public static WeaknessBuilder times2(Type type) {
        return aWeakness()
                .withType(type)
                .withValue("×2");
    }

    /**
     * Creates a weakness with +10 modifier (common in older sets).
     *
     * @param type the weakness type
     * @return a WeaknessBuilder configured with +10 modifier
     */
    public static WeaknessBuilder plus10(Type type) {
        return aWeakness()
                .withType(type)
                .withValue("+10");
    }

    /**
     * Creates a weakness with +20 modifier (common in older sets).
     *
     * @param type the weakness type
     * @return a WeaknessBuilder configured with +20 modifier
     */
    public static WeaknessBuilder plus20(Type type) {
        return aWeakness()
                .withType(type)
                .withValue("+20");
    }

    /**
     * Creates a weakness with +30 modifier (common in older sets).
     *
     * @param type the weakness type
     * @return a WeaknessBuilder configured with +30 modifier
     */
    public static WeaknessBuilder plus30(Type type) {
        return aWeakness()
                .withType(type)
                .withValue("+30");
    }

    /**
     * Sets the weakness type.
     *
     * @param type the Type entity representing the weakness
     * @return this builder for method chaining
     */
    public WeaknessBuilder withType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the weakness value.
     *
     * @param value the weakness value (e.g., "×2", "+20")
     * @return this builder for method chaining
     */
    public WeaknessBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Builds the Weakness entity with configured values.
     *
     * @return a new Weakness instance
     */
    public Weakness build() {
        Weakness weakness = new Weakness();
        weakness.setType(type);
        weakness.setValue(value);
        return weakness;
    }
}
