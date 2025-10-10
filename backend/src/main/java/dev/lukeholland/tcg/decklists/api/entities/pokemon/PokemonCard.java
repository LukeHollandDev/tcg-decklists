package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "pokemon_card")
public class PokemonCard {
    @Id
    @SequenceGenerator(name = "pokemon_card_id_gen", sequenceName = "pokemon_card_evolves_to_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "supertype", nullable = false, length = 50)
    private String supertype;

    @Column(name = "hp", length = 10)
    private String hp;

    @Column(name = "number", nullable = false, length = 50)
    private String number;

    @Column(name = "artist")
    private String artist;

    @Column(name = "rarity", length = 50)
    private String rarity;

    @Column(name = "flavor_text", length = Integer.MAX_VALUE)
    private String flavorText;

    @Column(name = "evolves_from")
    private String evolvesFrom;

    @Column(name = "level", length = 10)
    private String level;

    @Column(name = "regulation_mark", length = 10)
    private String regulationMark;

    @Column(name = "converted_retreat_cost")
    private Integer convertedRetreatCost;

    @Column(name = "image_small", length = 500)
    private String imageSmall;

    @Column(name = "image_large", length = 500)
    private String imageLarge;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "card")
    private Set<PokemonAbility> pokemonAbilities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<PokemonAncientTrait> pokemonAncientTraits = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<PokemonAttack> pokemonAttacks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardEvolvesTo> pokemonCardEvolvesTos = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardNationalPokedexNumber> pokemonCardNationalPokedexNumbers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardRetreatCost> pokemonCardRetreatCosts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardSubtype> pokemonCardSubtypes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardType> pokemonCardTypes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonLegality> pokemonLegalities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonResistance> pokemonResistances = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonRule> pokemonRules = new LinkedHashSet<>();

    @OneToMany(mappedBy = "card")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonWeakness> pokemonWeaknesses = new LinkedHashSet<>();

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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getFlavorText() {
        return flavorText;
    }

    public void setFlavorText(String flavorText) {
        this.flavorText = flavorText;
    }

    public String getEvolvesFrom() {
        return evolvesFrom;
    }

    public void setEvolvesFrom(String evolvesFrom) {
        this.evolvesFrom = evolvesFrom;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getRegulationMark() {
        return regulationMark;
    }

    public void setRegulationMark(String regulationMark) {
        this.regulationMark = regulationMark;
    }

    public Integer getConvertedRetreatCost() {
        return convertedRetreatCost;
    }

    public void setConvertedRetreatCost(Integer convertedRetreatCost) {
        this.convertedRetreatCost = convertedRetreatCost;
    }

    public String getImageSmall() {
        return imageSmall;
    }

    public void setImageSmall(String imageSmall) {
        this.imageSmall = imageSmall;
    }

    public String getImageLarge() {
        return imageLarge;
    }

    public void setImageLarge(String imageLarge) {
        this.imageLarge = imageLarge;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<PokemonAbility> getPokemonAbilities() {
        return pokemonAbilities;
    }

    public void setPokemonAbilities(Set<PokemonAbility> pokemonAbilities) {
        this.pokemonAbilities = pokemonAbilities;
    }

    public Set<PokemonAncientTrait> getPokemonAncientTraits() {
        return pokemonAncientTraits;
    }

    public void setPokemonAncientTraits(Set<PokemonAncientTrait> pokemonAncientTraits) {
        this.pokemonAncientTraits = pokemonAncientTraits;
    }

    public Set<PokemonAttack> getPokemonAttacks() {
        return pokemonAttacks;
    }

    public void setPokemonAttacks(Set<PokemonAttack> pokemonAttacks) {
        this.pokemonAttacks = pokemonAttacks;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardEvolvesTo> getPokemonCardEvolvesTos() {
        return pokemonCardEvolvesTos;
    }

    public void setPokemonCardEvolvesTos(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardEvolvesTo> pokemonCardEvolvesTos) {
        this.pokemonCardEvolvesTos = pokemonCardEvolvesTos;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardNationalPokedexNumber> getPokemonCardNationalPokedexNumbers() {
        return pokemonCardNationalPokedexNumbers;
    }

    public void setPokemonCardNationalPokedexNumbers(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardNationalPokedexNumber> pokemonCardNationalPokedexNumbers) {
        this.pokemonCardNationalPokedexNumbers = pokemonCardNationalPokedexNumbers;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardRetreatCost> getPokemonCardRetreatCosts() {
        return pokemonCardRetreatCosts;
    }

    public void setPokemonCardRetreatCosts(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardRetreatCost> pokemonCardRetreatCosts) {
        this.pokemonCardRetreatCosts = pokemonCardRetreatCosts;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardSubtype> getPokemonCardSubtypes() {
        return pokemonCardSubtypes;
    }

    public void setPokemonCardSubtypes(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardSubtype> pokemonCardSubtypes) {
        this.pokemonCardSubtypes = pokemonCardSubtypes;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardType> getPokemonCardTypes() {
        return pokemonCardTypes;
    }

    public void setPokemonCardTypes(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCardType> pokemonCardTypes) {
        this.pokemonCardTypes = pokemonCardTypes;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonLegality> getPokemonLegalities() {
        return pokemonLegalities;
    }

    public void setPokemonLegalities(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonLegality> pokemonLegalities) {
        this.pokemonLegalities = pokemonLegalities;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonResistance> getPokemonResistances() {
        return pokemonResistances;
    }

    public void setPokemonResistances(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonResistance> pokemonResistances) {
        this.pokemonResistances = pokemonResistances;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonRule> getPokemonRules() {
        return pokemonRules;
    }

    public void setPokemonRules(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonRule> pokemonRules) {
        this.pokemonRules = pokemonRules;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonWeakness> getPokemonWeaknesses() {
        return pokemonWeaknesses;
    }

    public void setPokemonWeaknesses(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonWeakness> pokemonWeaknesses) {
        this.pokemonWeaknesses = pokemonWeaknesses;
    }

}