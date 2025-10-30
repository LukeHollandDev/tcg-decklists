package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Rarity;

/**
 * Fluent builder for creating {@link Rarity} test entities.
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create Rare Holo rarity
 * Rarity rarity = RarityBuilder.rareHolo().build();
 *
 * // Create a custom rarity
 * Rarity custom = RarityBuilder.aRarity()
 *     .withName("Custom Rarity")
 *     .build();
 * }</pre>
 */
public class RarityBuilder {

    private String name;

    private RarityBuilder() {
    }

    public static RarityBuilder aRarity() {
        return new RarityBuilder();
    }

    public static RarityBuilder common() {
        return aRarity().withName("Common");
    }

    public static RarityBuilder uncommon() {
        return aRarity().withName("Uncommon");
    }

    public static RarityBuilder rare() {
        return aRarity().withName("Rare");
    }

    public static RarityBuilder rareHolo() {
        return aRarity().withName("Rare Holo");
    }

    public static RarityBuilder rareHoloEX() {
        return aRarity().withName("Rare Holo EX");
    }

    public static RarityBuilder rareHoloGX() {
        return aRarity().withName("Rare Holo GX");
    }

    public static RarityBuilder rareHoloV() {
        return aRarity().withName("Rare Holo V");
    }

    public static RarityBuilder rareHoloVMAX() {
        return aRarity().withName("Rare Holo VMAX");
    }

    public static RarityBuilder rareUltra() {
        return aRarity().withName("Rare Ultra");
    }

    public static RarityBuilder rareRainbow() {
        return aRarity().withName("Rare Rainbow");
    }

    public static RarityBuilder rareSecret() {
        return aRarity().withName("Rare Secret");
    }

    public RarityBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public Rarity build() {
        Rarity rarity = new Rarity();
        rarity.setName(name);
        return rarity;
    }
}
