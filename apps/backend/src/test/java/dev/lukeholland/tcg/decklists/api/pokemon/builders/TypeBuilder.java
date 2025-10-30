package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Type;

/**
 * Fluent builder for creating {@link Type} test entities.
 * <p>
 * Provides convenient factory methods for all standard Pokémon types
 * and supports custom type creation for test scenarios.
 * </p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a standard Fire type
 * Type fire = TypeBuilder.fire().build();
 *
 * // Create a custom type for testing
 * Type custom = TypeBuilder.aType()
 *     .withName("CustomType")
 *     .build();
 * }</pre>
 */
public class TypeBuilder {

    private String name;

    private TypeBuilder() {
    }

    /**
     * Creates a new TypeBuilder instance.
     *
     * @return a new TypeBuilder
     */
    public static TypeBuilder aType() {
        return new TypeBuilder();
    }

    /**
     * Creates a Fire type.
     *
     * @return a TypeBuilder configured for Fire type
     */
    public static TypeBuilder fire() {
        return aType().withName("Fire");
    }

    /**
     * Creates a Water type.
     *
     * @return a TypeBuilder configured for Water type
     */
    public static TypeBuilder water() {
        return aType().withName("Water");
    }

    /**
     * Creates a Grass type.
     *
     * @return a TypeBuilder configured for Grass type
     */
    public static TypeBuilder grass() {
        return aType().withName("Grass");
    }

    /**
     * Creates a Lightning type.
     *
     * @return a TypeBuilder configured for Lightning type
     */
    public static TypeBuilder lightning() {
        return aType().withName("Lightning");
    }

    /**
     * Creates a Fighting type.
     *
     * @return a TypeBuilder configured for Fighting type
     */
    public static TypeBuilder fighting() {
        return aType().withName("Fighting");
    }

    /**
     * Creates a Psychic type.
     *
     * @return a TypeBuilder configured for Psychic type
     */
    public static TypeBuilder psychic() {
        return aType().withName("Psychic");
    }

    /**
     * Creates a Colorless type.
     *
     * @return a TypeBuilder configured for Colorless type
     */
    public static TypeBuilder colorless() {
        return aType().withName("Colorless");
    }

    /**
     * Creates a Darkness type.
     *
     * @return a TypeBuilder configured for Darkness type
     */
    public static TypeBuilder darkness() {
        return aType().withName("Darkness");
    }

    /**
     * Creates a Metal type.
     *
     * @return a TypeBuilder configured for Metal type
     */
    public static TypeBuilder metal() {
        return aType().withName("Metal");
    }

    /**
     * Creates a Dragon type.
     *
     * @return a TypeBuilder configured for Dragon type
     */
    public static TypeBuilder dragon() {
        return aType().withName("Dragon");
    }

    /**
     * Creates a Fairy type.
     *
     * @return a TypeBuilder configured for Fairy type
     */
    public static TypeBuilder fairy() {
        return aType().withName("Fairy");
    }

    /**
     * Sets the type name.
     *
     * @param name the type name
     * @return this builder for method chaining
     */
    public TypeBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Builds the Type entity with configured values.
     *
     * @return a new Type instance
     */
    public Type build() {
        Type type = new Type();
        type.setName(name);
        return type;
    }
}
