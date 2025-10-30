package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Subtype;

/**
 * Fluent builder for creating {@link Subtype} test entities.
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create Stage 2 subtype
 * Subtype stage2 = SubtypeBuilder.stage2().build();
 *
 * // Create a custom subtype
 * Subtype custom = SubtypeBuilder.aSubtype()
 *     .withName("Custom Subtype")
 *     .build();
 * }</pre>
 */
public class SubtypeBuilder {

    private String name;

    private SubtypeBuilder() {
    }

    public static SubtypeBuilder aSubtype() {
        return new SubtypeBuilder();
    }

    // Pokemon Stages
    public static SubtypeBuilder basic() {
        return aSubtype().withName("Basic");
    }

    public static SubtypeBuilder stage1() {
        return aSubtype().withName("Stage 1");
    }

    public static SubtypeBuilder stage2() {
        return aSubtype().withName("Stage 2");
    }

    // Pokemon Mechanics
    public static SubtypeBuilder ex() {
        return aSubtype().withName("ex");
    }

    public static SubtypeBuilder EX() {
        return aSubtype().withName("EX");
    }

    public static SubtypeBuilder GX() {
        return aSubtype().withName("GX");
    }

    public static SubtypeBuilder V() {
        return aSubtype().withName("V");
    }

    public static SubtypeBuilder VMAX() {
        return aSubtype().withName("VMAX");
    }

    public static SubtypeBuilder VSTAR() {
        return aSubtype().withName("VSTAR");
    }

    public static SubtypeBuilder mega() {
        return aSubtype().withName("MEGA");
    }

    public static SubtypeBuilder legend() {
        return aSubtype().withName("LEGEND");
    }

    // Trainer Types
    public static SubtypeBuilder item() {
        return aSubtype().withName("Item");
    }

    public static SubtypeBuilder supporter() {
        return aSubtype().withName("Supporter");
    }

    public static SubtypeBuilder stadium() {
        return aSubtype().withName("Stadium");
    }

    public static SubtypeBuilder tool() {
        return aSubtype().withName("Tool");
    }

    public static SubtypeBuilder aceSpec() {
        return aSubtype().withName("ACE SPEC");
    }

    // Energy Types
    public static SubtypeBuilder basicEnergy() {
        return aSubtype().withName("Basic");
    }

    public static SubtypeBuilder specialEnergy() {
        return aSubtype().withName("Special");
    }

    public SubtypeBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public Subtype build() {
        Subtype subtype = new Subtype();
        subtype.setName(name);
        return subtype;
    }
}
