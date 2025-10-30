package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Ability;

/**
 * Fluent builder for creating {@link Ability} test entities.
 * <p>
 * Provides convenient factory methods for common Pokémon abilities
 * and supports full customization for test scenarios.
 * </p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a Blaze ability
 * Ability blaze = AbilityBuilder.blaze().build();
 *
 * // Create a custom ability for testing
 * Ability custom = AbilityBuilder.anAbility()
 *     .withName("Custom Ability")
 *     .withType("Ability")
 *     .withText("Custom ability effect.")
 *     .build();
 * }</pre>
 */
public class AbilityBuilder {

    private String name;
    private String text;
    private String type;

    private AbilityBuilder() {
    }

    /**
     * Creates a new AbilityBuilder instance.
     *
     * @return a new AbilityBuilder
     */
    public static AbilityBuilder anAbility() {
        return new AbilityBuilder();
    }

    /**
     * Creates a Blaze ability.
     *
     * @return an AbilityBuilder configured for Blaze
     */
    public static AbilityBuilder blaze() {
        return anAbility()
                .withName("Blaze")
                .withType("Ability")
                .withText("Once during your turn, you may attach a Fire Energy card from your discard pile to 1 of your Fire Pokémon.");
    }

    /**
     * Creates an Intimidate ability.
     *
     * @return an AbilityBuilder configured for Intimidate
     */
    public static AbilityBuilder intimidate() {
        return anAbility()
                .withName("Intimidate")
                .withType("Ability")
                .withText("When you play this Pokémon from your hand onto your Bench during your turn, you may switch 1 of your opponent's Benched Pokémon with their Active Pokémon.");
    }

    /**
     * Creates a Power Draw ability.
     *
     * @return an AbilityBuilder configured for Power Draw
     */
    public static AbilityBuilder powerDraw() {
        return anAbility()
                .withName("Power Draw")
                .withType("Poké-Power")
                .withText("Once during your turn (before your attack), you may draw 2 cards. This power can't be used if this Pokémon is affected by a Special Condition.");
    }

    /**
     * Creates a Solar Evolution ability.
     *
     * @return an AbilityBuilder configured for Solar Evolution
     */
    public static AbilityBuilder solarEvolution() {
        return anAbility()
                .withName("Solar Evolution")
                .withType("Ability")
                .withText("Once during your turn (before your attack), you may search your deck for a card that evolves from this Pokémon and put it onto this Pokémon to evolve it. Then, shuffle your deck.");
    }

    /**
     * Creates a Triple Draw ability.
     *
     * @return an AbilityBuilder configured for Triple Draw
     */
    public static AbilityBuilder tripleDraw() {
        return anAbility()
                .withName("Triple Draw")
                .withType("Ability")
                .withText("When you play this Pokémon from your hand to evolve 1 of your Pokémon during your turn, you may draw 3 cards.");
    }

    /**
     * Creates a Shining Horn ability.
     *
     * @return an AbilityBuilder configured for Shining Horn
     */
    public static AbilityBuilder shiningHorn() {
        return anAbility()
                .withName("Shining Horn")
                .withType("Ability")
                .withText("Once during your turn, you may discard a Pokémon Tool card from your hand. If you do, draw 3 cards.");
    }

    /**
     * Sets the ability name.
     *
     * @param name the ability name
     * @return this builder for method chaining
     */
    public AbilityBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the ability text/description.
     *
     * @param text the ability text
     * @return this builder for method chaining
     */
    public AbilityBuilder withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Sets the ability type.
     *
     * @param type the ability type (e.g., "Ability", "Poké-Power", "Poké-Body")
     * @return this builder for method chaining
     */
    public AbilityBuilder withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Builds the Ability entity with configured values.
     *
     * @return a new Ability instance
     */
    public Ability build() {
        Ability ability = new Ability();
        ability.setName(name);
        ability.setText(text);
        ability.setType(type);
        return ability;
    }
}
