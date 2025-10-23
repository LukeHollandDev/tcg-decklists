package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pokemon_card")
public class PokemonCard {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "supertype", nullable = false)
    private String supertype;

    @Column(name = "hp")
    private String hp;

    @Column(name = "hp_numeric")
    private Integer hpNumeric;

    @Column(name = "converted_retreat_cost")
    private Integer convertedRetreatCost;

    @Column(name = "number", nullable = false)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id")
    private PokemonSet pokemonSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private PokemonArtist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rarity_id")
    private PokemonRarity rarity;

    @Column(name = "flavor_text")
    private String flavorText;

    @Column(name = "image_low")
    private String imageLow;

    @Column(name = "image_high")
    private String imageHigh;

    @Column(name = "regulation_mark")
    private String regulationMark;

    @Column(name = "level")
    private String level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancient_trait_id")
    private PokemonAncientTrait ancientTrait;

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_subtype",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "subtype_id")
    )
    private Set<PokemonSubtype> subtypes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_type",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private Set<PokemonType> types = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_pokedex",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "pokedex_id")
    )
    private Set<PokemonPokedex> pokedexNumbers = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_ability",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "ability_id")
    )
    private Set<PokemonAbility> abilities = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_attack",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "attack_id")
    )
    private Set<PokemonAttack> attacks = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_rule",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id")
    )
    private Set<PokemonRule> rules = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PokemonCardLegality> legalities = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PokemonCardEvolution> evolutions = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PokemonCardRetreatCost> retreatCosts = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PokemonCardResistance> resistances = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PokemonCardWeakness> weaknesses = new HashSet<>();

    // Constructors
    public PokemonCard() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupertype() {
        return supertype;
    }

    public void setSupertype(String supertype) {
        this.supertype = supertype;
    }

    public String getHp() {
        return hp;
    }

    public void setHp(String hp) {
        this.hp = hp;
    }

    public Integer getHpNumeric() {
        return hpNumeric;
    }

    public void setHpNumeric(Integer hpNumeric) {
        this.hpNumeric = hpNumeric;
    }

    public Integer getConvertedRetreatCost() {
        return convertedRetreatCost;
    }

    public void setConvertedRetreatCost(Integer convertedRetreatCost) {
        this.convertedRetreatCost = convertedRetreatCost;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public PokemonSet getPokemonSet() {
        return pokemonSet;
    }

    public void setPokemonSet(PokemonSet pokemonSet) {
        this.pokemonSet = pokemonSet;
    }

    public PokemonArtist getArtist() {
        return artist;
    }

    public void setArtist(PokemonArtist artist) {
        this.artist = artist;
    }

    public PokemonRarity getRarity() {
        return rarity;
    }

    public void setRarity(PokemonRarity rarity) {
        this.rarity = rarity;
    }

    public String getFlavorText() {
        return flavorText;
    }

    public void setFlavorText(String flavorText) {
        this.flavorText = flavorText;
    }

    public String getImageLow() {
        return imageLow;
    }

    public void setImageLow(String imageLow) {
        this.imageLow = imageLow;
    }

    public String getImageHigh() {
        return imageHigh;
    }

    public void setImageHigh(String imageHigh) {
        this.imageHigh = imageHigh;
    }

    public String getRegulationMark() {
        return regulationMark;
    }

    public void setRegulationMark(String regulationMark) {
        this.regulationMark = regulationMark;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public PokemonAncientTrait getAncientTrait() {
        return ancientTrait;
    }

    public void setAncientTrait(PokemonAncientTrait ancientTrait) {
        this.ancientTrait = ancientTrait;
    }

    public Set<PokemonSubtype> getSubtypes() {
        return subtypes;
    }

    public void setSubtypes(Set<PokemonSubtype> subtypes) {
        this.subtypes = subtypes;
    }

    public Set<PokemonType> getTypes() {
        return types;
    }

    public void setTypes(Set<PokemonType> types) {
        this.types = types;
    }

    public Set<PokemonPokedex> getPokedexNumbers() {
        return pokedexNumbers;
    }

    public void setPokedexNumbers(Set<PokemonPokedex> pokedexNumbers) {
        this.pokedexNumbers = pokedexNumbers;
    }

    public Set<PokemonAbility> getAbilities() {
        return abilities;
    }

    public void setAbilities(Set<PokemonAbility> abilities) {
        this.abilities = abilities;
    }

    public Set<PokemonAttack> getAttacks() {
        return attacks;
    }

    public void setAttacks(Set<PokemonAttack> attacks) {
        this.attacks = attacks;
    }

    public Set<PokemonRule> getRules() {
        return rules;
    }

    public void setRules(Set<PokemonRule> rules) {
        this.rules = rules;
    }

    public Set<PokemonCardLegality> getLegalities() {
        return legalities;
    }

    public void setLegalities(Set<PokemonCardLegality> legalities) {
        this.legalities = legalities;
    }

    public Set<PokemonCardEvolution> getEvolutions() {
        return evolutions;
    }

    public void setEvolutions(Set<PokemonCardEvolution> evolutions) {
        this.evolutions = evolutions;
    }

    public Set<PokemonCardRetreatCost> getRetreatCosts() {
        return retreatCosts;
    }

    public void setRetreatCosts(Set<PokemonCardRetreatCost> retreatCosts) {
        this.retreatCosts = retreatCosts;
    }

    public Set<PokemonCardResistance> getResistances() {
        return resistances;
    }

    public void setResistances(Set<PokemonCardResistance> resistances) {
        this.resistances = resistances;
    }

    public Set<PokemonCardWeakness> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(Set<PokemonCardWeakness> weaknesses) {
        this.weaknesses = weaknesses;
    }
}