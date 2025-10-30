package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Attack;
import dev.lukeholland.tcg.decklists.api.pokemon.entities.AttackCost;
import dev.lukeholland.tcg.decklists.api.pokemon.entities.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fluent builder for creating {@link Attack} test entities.
 * <p>
 * Provides convenient factory methods for common Pokémon attacks
 * and supports full customization including attack costs for test scenarios.
 * Properly handles the bidirectional relationship with {@link AttackCost} entities.
 * </p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a Fire Blast attack
 * Attack fireBlast = AttackBuilder.fireBlast().build();
 *
 * // Create a custom attack with specific costs
 * Type fire = TypeBuilder.fire().build();
 * Type colorless = TypeBuilder.colorless().build();
 * Attack custom = AttackBuilder.anAttack()
 *     .withName("Custom Attack")
 *     .withDamage("150")
 *     .withDamageNumeric(150)
 *     .addCost(fire, 2)  // 2x Fire
 *     .addCost(colorless, 1)  // 1x Colorless
 *     .withText("Does 150 damage.")
 *     .build();
 * }</pre>
 */
public class AttackBuilder {

    private String name;
    private Integer convertedCost = 0;
    private String damage;
    private Integer damageNumeric;
    private String damageModifier;
    private String text;
    private final List<CostEntry> costs = new ArrayList<>();

    private AttackBuilder() {
    }

    /**
     * Helper class to track attack costs before building.
     */
    private static class CostEntry {
        final Type type;
        final Integer quantity;

        CostEntry(Type type, Integer quantity) {
            this.type = type;
            this.quantity = quantity;
        }
    }

    /**
     * Creates a new AttackBuilder instance.
     *
     * @return a new AttackBuilder
     */
    public static AttackBuilder anAttack() {
        return new AttackBuilder();
    }

    /**
     * Creates a Fire Blast attack (common Charizard attack).
     *
     * @return an AttackBuilder configured for Fire Blast
     */
    public static AttackBuilder fireBlast() {
        return anAttack()
                .withName("Fire Blast")
                .withDamage("120")
                .withDamageNumeric(120)
                .withText("Discard 1 Fire Energy card attached to this Pokémon.");
    }

    /**
     * Creates a Thunderbolt attack (common Pikachu evolution attack).
     *
     * @return an AttackBuilder configured for Thunderbolt
     */
    public static AttackBuilder thunderbolt() {
        return anAttack()
                .withName("Thunderbolt")
                .withDamage("100")
                .withDamageNumeric(100)
                .withText("Discard all Energy attached to this Pokémon.");
    }

    /**
     * Creates a Tackle attack (basic attack).
     *
     * @return an AttackBuilder configured for Tackle
     */
    public static AttackBuilder tackle() {
        return anAttack()
                .withName("Tackle")
                .withDamage("30")
                .withDamageNumeric(30);
    }

    /**
     * Creates a Flamethrower attack.
     *
     * @return an AttackBuilder configured for Flamethrower
     */
    public static AttackBuilder flamethrower() {
        return anAttack()
                .withName("Flamethrower")
                .withDamage("90")
                .withDamageNumeric(90)
                .withText("Discard 1 Fire Energy attached to this Pokémon.");
    }

    /**
     * Creates a Hydro Pump attack (damage scales with Energy).
     *
     * @return an AttackBuilder configured for Hydro Pump
     */
    public static AttackBuilder hydroPump() {
        return anAttack()
                .withName("Hydro Pump")
                .withDamage("50+")
                .withDamageModifier("+")
                .withText("Does 50 damage plus 10 more damage for each Water Energy attached to this Pokémon but not used to pay for this attack's Energy cost.");
    }

    /**
     * Creates a Solar Beam attack.
     *
     * @return an AttackBuilder configured for Solar Beam
     */
    public static AttackBuilder solarBeam() {
        return anAttack()
                .withName("Solar Beam")
                .withDamage("120")
                .withDamageNumeric(120);
    }

    /**
     * Creates a Quick Attack with damage modifier.
     *
     * @return an AttackBuilder configured for Quick Attack
     */
    public static AttackBuilder quickAttack() {
        return anAttack()
                .withName("Quick Attack")
                .withDamage("10+")
                .withDamageModifier("+")
                .withText("Flip a coin. If heads, this attack does 30 more damage.");
    }

    /**
     * Sets the attack name.
     *
     * @param name the attack name
     * @return this builder for method chaining
     */
    public AttackBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the converted cost (total energy cost).
     * This will be automatically calculated from added costs if not explicitly set.
     *
     * @param convertedCost the total energy cost
     * @return this builder for method chaining
     */
    public AttackBuilder withConvertedCost(Integer convertedCost) {
        this.convertedCost = convertedCost;
        return this;
    }

    /**
     * Sets the damage string.
     *
     * @param damage the damage value (e.g., "120", "50+", "×30")
     * @return this builder for method chaining
     */
    public AttackBuilder withDamage(String damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Sets the numeric damage value (for filtering/sorting).
     *
     * @param damageNumeric the numeric damage value
     * @return this builder for method chaining
     */
    public AttackBuilder withDamageNumeric(Integer damageNumeric) {
        this.damageNumeric = damageNumeric;
        return this;
    }

    /**
     * Sets the damage modifier.
     *
     * @param damageModifier the damage modifier (e.g., "+", "×", "-")
     * @return this builder for method chaining
     */
    public AttackBuilder withDamageModifier(String damageModifier) {
        this.damageModifier = damageModifier;
        return this;
    }

    /**
     * Sets the attack text/description.
     *
     * @param text the attack text
     * @return this builder for method chaining
     */
    public AttackBuilder withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Adds an attack cost with the specified type and quantity.
     * Creates an {@link AttackCost} entity and properly sets up the bidirectional relationship.
     *
     * @param type     the energy type required
     * @param quantity the number of that energy type required (multiset support)
     * @return this builder for method chaining
     */
    public AttackBuilder addCost(Type type, Integer quantity) {
        costs.add(new CostEntry(type, quantity));
        return this;
    }

    /**
     * Adds an attack cost with quantity of 1.
     *
     * @param type the energy type required
     * @return this builder for method chaining
     */
    public AttackBuilder addCost(Type type) {
        return addCost(type, 1);
    }

    /**
     * Builds the Attack entity with configured values.
     * Automatically calculates convertedCost from costs if not explicitly set.
     * Properly creates {@link AttackCost} entities with bidirectional relationships.
     *
     * @return a new Attack instance with associated AttackCost entities
     */
    public Attack build() {
        Attack attack = new Attack();
        attack.setName(name);
        attack.setDamage(damage);
        attack.setDamageNumeric(damageNumeric);
        attack.setDamageModifier(damageModifier);
        attack.setText(text);

        // Calculate convertedCost from costs if not explicitly set
        int totalCost = convertedCost != null ? convertedCost : 0;
        if (convertedCost == null || convertedCost == 0) {
            for (CostEntry entry : costs) {
                totalCost += entry.quantity;
            }
        }
        attack.setConvertedCost(totalCost);

        // Create AttackCost entities and set up bidirectional relationship
        Set<AttackCost> attackCosts = new HashSet<>();
        for (CostEntry entry : costs) {
            AttackCost attackCost = new AttackCost();
            attackCost.setAttack(attack);
            attackCost.setType(entry.type);
            attackCost.setQuantity(entry.quantity);
            attackCosts.add(attackCost);
        }
        attack.setCosts(attackCosts);

        return attack;
    }
}
