package dev.lukeholland.tcg.decklists.api.pokemon.entities;

import jakarta.persistence.*;

import java.util.HashSet;

@Entity
@Table(name = "pokemon_card")
public class Card {

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
    private Set set;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rarity_id")
    private Rarity rarity;

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
    private AncientTrait ancientTrait;

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_subtype",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "subtype_id")
    )
    private java.util.Set<Subtype> subtypes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_type",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private java.util.Set<Type> types = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_pokedex",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "pokedex_id")
    )
    private java.util.Set<Pokedex> pokedexNumbers = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_ability",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "ability_id")
    )
    private java.util.Set<Ability> abilities = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_attack",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "attack_id")
    )
    private java.util.Set<Attack> attacks = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "pokemon_card_rule",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id")
    )
    private java.util.Set<Rule> rules = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<CardLegality> legalities = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<CardEvolution> evolutions = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<CardRetreatCost> retreatCosts = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<CardResistance> resistances = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<CardWeakness> weaknesses = new HashSet<>();

    // Constructors
    public Card() {
    }

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

    public Set getPokemonSet() {
        return set;
    }

    public void setPokemonSet(Set pokemonSet) {
        this.set = pokemonSet;
    }

    public String getSetId() {
        return set != null ? set.getSetId() : null;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
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

    public AncientTrait getAncientTrait() {
        return ancientTrait;
    }

    public void setAncientTrait(AncientTrait ancientTrait) {
        this.ancientTrait = ancientTrait;
    }

    public java.util.Set<Subtype> getSubtypes() {
        return subtypes;
    }

    public void setSubtypes(java.util.Set<Subtype> subtypes) {
        this.subtypes = subtypes;
    }

    public java.util.Set<Type> getTypes() {
        return types;
    }

    public void setTypes(java.util.Set<Type> types) {
        this.types = types;
    }

    public java.util.Set<Pokedex> getPokedexNumbers() {
        return pokedexNumbers;
    }

    public void setPokedexNumbers(java.util.Set<Pokedex> pokedexNumbers) {
        this.pokedexNumbers = pokedexNumbers;
    }

    public java.util.Set<Ability> getAbilities() {
        return abilities;
    }

    public void setAbilities(java.util.Set<Ability> abilities) {
        this.abilities = abilities;
    }

    public java.util.Set<Attack> getAttacks() {
        return attacks;
    }

    public void setAttacks(java.util.Set<Attack> attacks) {
        this.attacks = attacks;
    }

    public java.util.Set<Rule> getRules() {
        return rules;
    }

    public void setRules(java.util.Set<Rule> rules) {
        this.rules = rules;
    }

    public java.util.Set<CardLegality> getLegalities() {
        return legalities;
    }

    public void setLegalities(java.util.Set<CardLegality> legalities) {
        this.legalities = legalities;
    }

    public java.util.Set<CardEvolution> getEvolutions() {
        return evolutions;
    }

    public void setEvolutions(java.util.Set<CardEvolution> evolutions) {
        this.evolutions = evolutions;
    }

    public java.util.Set<CardRetreatCost> getRetreatCosts() {
        return retreatCosts;
    }

    public void setRetreatCosts(java.util.Set<CardRetreatCost> retreatCosts) {
        this.retreatCosts = retreatCosts;
    }

    public java.util.Set<CardResistance> getResistances() {
        return resistances;
    }

    public void setResistances(java.util.Set<CardResistance> resistances) {
        this.resistances = resistances;
    }

    public java.util.Set<CardWeakness> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(java.util.Set<CardWeakness> weaknesses) {
        this.weaknesses = weaknesses;
    }
}