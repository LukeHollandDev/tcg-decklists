package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Fluent builder for creating {@link Card} test entities with all relationships.
 * <p>
 * This is the most comprehensive builder, supporting all card properties and relationships.
 * Properly handles bidirectional JPA relationships including OneToMany junction tables.
 * </p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a Charizard card with all relationships
 * Card charizard = CardBuilder.charizard().build();
 *
 * // Create a custom card with specific attributes
 * Card custom = CardBuilder.aCard()
 *     .withId("test-1")
 *     .withName("Test Card")
 *     .withSupertype("Pokémon")
 *     .withHp("100")
 *     .withHpNumeric(100)
 *     .addType(TypeBuilder.fire().build())
 *     .addSubtype(SubtypeBuilder.basic().build())
 *     .withSet(SetBuilder.baseSet().build())
 *     .withArtist(ArtistBuilder.kenSugimori().build())
 *     .withRarity(RarityBuilder.rare().build())
 *     .addAttack(AttackBuilder.tackle().build())
 *     .addAbility(AbilityBuilder.blaze().build())
 *     .addWeakness(WeaknessBuilder.times2(TypeBuilder.water().build()))
 *     .addRetreatCost(TypeBuilder.colorless().build(), 2)
 *     .build();
 * }</pre>
 */
public class CardBuilder {

    private final java.util.Set<Subtype> subtypes = new HashSet<>();
    private final java.util.Set<Type> types = new HashSet<>();
    private final java.util.Set<Ability> abilities = new HashSet<>();
    private final java.util.Set<Attack> attacks = new HashSet<>();
    private final java.util.Set<Rule> rules = new HashSet<>();
    private final List<WeaknessEntry> weaknesses = new ArrayList<>();
    private final List<ResistanceEntry> resistances = new ArrayList<>();
    private final List<RetreatCostEntry> retreatCosts = new ArrayList<>();
    private String id;
    private String name;
    private String supertype;
    private String hp;
    private Integer hpNumeric;
    private Integer convertedRetreatCost;
    private String number;
    private Set set;
    private Artist artist;
    private Rarity rarity;
    private String flavorText;
    private String imageLow;
    private String imageHigh;
    private String regulationMark;
    private String level;
    private AncientTrait ancientTrait;

    private CardBuilder() {
    }

    public static CardBuilder aCard() {
        return new CardBuilder();
    }

    /**
     * Creates a Base Set Charizard card (one of the most iconic Pokémon cards).
     * Fully configured with types, attacks, weaknesses, etc.
     *
     * @return a CardBuilder configured for Base Set Charizard
     */
    public static CardBuilder charizard() {
        Type fire = TypeBuilder.fire().build();
        Type water = TypeBuilder.water().build();
        Type colorless = TypeBuilder.colorless().build();

        Attack energyBurn = AttackBuilder.anAttack()
                .withName("Energy Burn")
                .withText("As often as you like during your turn (before your attack), you may turn all Energy attached to Charizard into Fire Energy for the rest of the turn. This power can't be used if Charizard is Asleep, Confused, or Paralyzed.")
                .build();

        Attack fireBlast = AttackBuilder.fireBlast()
                .addCost(fire, 4)
                .build();

        return aCard()
                .withId("base1-4")
                .withName("Charizard")
                .withSupertype("Pokémon")
                .withHp("120")
                .withHpNumeric(120)
                .withNumber("4")
                .addType(fire)
                .addSubtype(SubtypeBuilder.stage2().build())
                .withSet(SetBuilder.baseSet().build())
                .withArtist(ArtistBuilder.mitsuhiroArita().build())
                .withRarity(RarityBuilder.rareHolo().build())
                .addAttack(fireBlast)
                .addWeakness(WeaknessBuilder.times2(water).build())
                .addResistance(ResistanceBuilder.minus30(TypeBuilder.fighting().build()).build())
                .addRetreatCost(colorless, 3)
                .withConvertedRetreatCost(3);
    }

    /**
     * Creates a Base Set Pikachu card.
     *
     * @return a CardBuilder configured for Base Set Pikachu
     */
    public static CardBuilder pikachu() {
        Type lightning = TypeBuilder.lightning().build();
        Type colorless = TypeBuilder.colorless().build();

        Attack thundershock = AttackBuilder.anAttack()
                .withName("Thundershock")
                .withDamage("10")
                .withDamageNumeric(10)
                .withText("Flip a coin. If heads, the Defending Pokémon is now Paralyzed.")
                .addCost(lightning, 1)
                .build();

        Attack gnaw = AttackBuilder.anAttack()
                .withName("Gnaw")
                .withDamage("20")
                .withDamageNumeric(20)
                .addCost(colorless, 2)
                .build();

        return aCard()
                .withId("base1-58")
                .withName("Pikachu")
                .withSupertype("Pokémon")
                .withHp("40")
                .withHpNumeric(40)
                .withNumber("58")
                .addType(lightning)
                .addSubtype(SubtypeBuilder.basic().build())
                .withSet(SetBuilder.baseSet().build())
                .withArtist(ArtistBuilder.mitsuhiroArita().build())
                .withRarity(RarityBuilder.common().build())
                .addAttack(thundershock)
                .addAttack(gnaw)
                .addRetreatCost(colorless, 1)
                .withConvertedRetreatCost(1);
    }

    /**
     * Creates a Professor Oak Trainer card.
     *
     * @return a CardBuilder configured for Professor Oak
     */
    public static CardBuilder professorOak() {
        return aCard()
                .withId("base1-88")
                .withName("Professor Oak")
                .withSupertype("Trainer")
                .withNumber("88")
                .addSubtype(SubtypeBuilder.supporter().build())
                .withSet(SetBuilder.baseSet().build())
                .withArtist(ArtistBuilder.kenSugimori().build())
                .withRarity(RarityBuilder.uncommon().build())
                .withFlavorText("Discard your hand, then draw 7 cards.");
    }

    /**
     * Creates a Fire Energy card.
     *
     * @return a CardBuilder configured for Fire Energy
     */
    public static CardBuilder fireEnergy() {
        return aCard()
                .withId("base1-98")
                .withName("Fire Energy")
                .withSupertype("Energy")
                .withNumber("98")
                .addType(TypeBuilder.fire().build())
                .addSubtype(SubtypeBuilder.basicEnergy().build())
                .withSet(SetBuilder.baseSet().build())
                .withRarity(RarityBuilder.common().build());
    }

    public CardBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public CardBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CardBuilder withSupertype(String supertype) {
        this.supertype = supertype;
        return this;
    }

    // Fluent API methods

    public CardBuilder withHp(String hp) {
        this.hp = hp;
        return this;
    }

    public CardBuilder withHpNumeric(Integer hpNumeric) {
        this.hpNumeric = hpNumeric;
        return this;
    }

    public CardBuilder withConvertedRetreatCost(Integer convertedRetreatCost) {
        this.convertedRetreatCost = convertedRetreatCost;
        return this;
    }

    public CardBuilder withNumber(String number) {
        this.number = number;
        return this;
    }

    public CardBuilder withSet(Set set) {
        this.set = set;
        return this;
    }

    public CardBuilder withArtist(Artist artist) {
        this.artist = artist;
        return this;
    }

    public CardBuilder withRarity(Rarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public CardBuilder withFlavorText(String flavorText) {
        this.flavorText = flavorText;
        return this;
    }

    public CardBuilder withImageLow(String imageLow) {
        this.imageLow = imageLow;
        return this;
    }

    public CardBuilder withImageHigh(String imageHigh) {
        this.imageHigh = imageHigh;
        return this;
    }

    public CardBuilder withRegulationMark(String regulationMark) {
        this.regulationMark = regulationMark;
        return this;
    }

    public CardBuilder withLevel(String level) {
        this.level = level;
        return this;
    }

    public CardBuilder withAncientTrait(AncientTrait ancientTrait) {
        this.ancientTrait = ancientTrait;
        return this;
    }

    public CardBuilder addSubtype(Subtype subtype) {
        this.subtypes.add(subtype);
        return this;
    }

    public CardBuilder addType(Type type) {
        this.types.add(type);
        return this;
    }

    public CardBuilder addAbility(Ability ability) {
        this.abilities.add(ability);
        return this;
    }

    public CardBuilder addAttack(Attack attack) {
        this.attacks.add(attack);
        return this;
    }

    public CardBuilder addRule(Rule rule) {
        this.rules.add(rule);
        return this;
    }

    /**
     * Adds a weakness to the card.
     * Creates the proper {@link CardWeakness} junction table entity.
     *
     * @param weakness the Weakness entity
     * @return this builder for method chaining
     */
    public CardBuilder addWeakness(Weakness weakness) {
        this.weaknesses.add(new WeaknessEntry(weakness));
        return this;
    }

    /**
     * Adds a resistance to the card.
     * Creates the proper {@link CardResistance} junction table entity.
     *
     * @param resistance the Resistance entity
     * @return this builder for method chaining
     */
    public CardBuilder addResistance(Resistance resistance) {
        this.resistances.add(new ResistanceEntry(resistance));
        return this;
    }

    /**
     * Adds a retreat cost to the card.
     * Creates the proper {@link CardRetreatCost} junction table entity.
     *
     * @param type     the energy type required for retreat
     * @param quantity the number of that energy type required
     * @return this builder for method chaining
     */
    public CardBuilder addRetreatCost(Type type, Integer quantity) {
        this.retreatCosts.add(new RetreatCostEntry(type, quantity));
        return this;
    }

    /**
     * Adds a retreat cost with quantity of 1.
     *
     * @param type the energy type required for retreat
     * @return this builder for method chaining
     */
    public CardBuilder addRetreatCost(Type type) {
        return addRetreatCost(type, 1);
    }

    /**
     * Builds the Card entity with all configured values and relationships.
     * Properly creates junction table entities ({@link CardWeakness}, {@link CardResistance},
     * {@link CardRetreatCost}) with bidirectional relationships.
     *
     * @return a new Card instance with all relationships properly configured
     */
    public Card build() {
        Card card = new Card();
        card.setId(id);
        card.setName(name);
        card.setSupertype(supertype);
        card.setHp(hp);
        card.setHpNumeric(hpNumeric);
        card.setConvertedRetreatCost(convertedRetreatCost);
        card.setNumber(number);
        card.setPokemonSet(set);
        card.setArtist(artist);
        card.setRarity(rarity);
        card.setFlavorText(flavorText);
        card.setImageLow(imageLow);
        card.setImageHigh(imageHigh);
        card.setRegulationMark(regulationMark);
        card.setLevel(level);
        card.setAncientTrait(ancientTrait);

        // Set ManyToMany relationships
        card.setSubtypes(subtypes);
        card.setTypes(types);
        card.setAbilities(abilities);
        card.setAttacks(attacks);
        card.setRules(rules);

        // Create CardWeakness junction table entities
        java.util.Set<CardWeakness> cardWeaknesses = new HashSet<>();
        for (WeaknessEntry entry : weaknesses) {
            CardWeakness cardWeakness = new CardWeakness();
            cardWeakness.setCard(card);
            cardWeakness.setWeakness(entry.weakness);
            cardWeaknesses.add(cardWeakness);
        }
        card.setWeaknesses(cardWeaknesses);

        // Create CardResistance junction table entities
        java.util.Set<CardResistance> cardResistances = new HashSet<>();
        for (ResistanceEntry entry : resistances) {
            CardResistance cardResistance = new CardResistance();
            cardResistance.setCard(card);
            cardResistance.setResistance(entry.resistance);
            cardResistances.add(cardResistance);
        }
        card.setResistances(cardResistances);

        // Create CardRetreatCost junction table entities
        java.util.Set<CardRetreatCost> cardRetreatCosts = new HashSet<>();
        for (RetreatCostEntry entry : retreatCosts) {
            CardRetreatCost cardRetreatCost = new CardRetreatCost();
            cardRetreatCost.setCard(card);
            cardRetreatCost.setType(entry.type);
            cardRetreatCost.setQuantity(entry.quantity);
            cardRetreatCosts.add(cardRetreatCost);
        }
        card.setRetreatCosts(cardRetreatCosts);

        return card;
    }

    /**
     * Helper classes to track junction table entries before building.
     */
    private record WeaknessEntry(Weakness weakness) {
    }

    private record ResistanceEntry(Resistance resistance) {
    }

    private record RetreatCostEntry(Type type, Integer quantity) {
    }
}
