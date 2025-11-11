package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.lukeholland.tcg.decklists.api.pokemon.entities.*;
import dev.lukeholland.tcg.decklists.api.pokemon.enums.EvolutionDirection;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CardResponse {
    private String id;
    private String name;
    private String supertype;
    private String hp;
    private Integer hpNumeric;
    private Integer convertedRetreatCost;
    private List<String> retreatCost;
    private String number;
    private String setId;
    private String setName;
    private String artistName;
    private String rarityName;
    private String flavorText;
    private String imageLow;
    private String imageHigh;
    private String regulationMark;
    private String level;
    private Set<String> subtypes;
    private Set<String> types;
    private AncientTraitResponse ancientTrait;
    private Set<AbilityResponse> abilities;
    private Set<AttackResponse> attacks;
    private Set<String> rules;
    private Set<Integer> pokedexNumbers;
    private Map<String, String> legalities;
    private List<String> evolvesFrom;
    private List<String> evolvesTo;
    private Set<ResistanceResponse> resistances;
    private Set<WeaknessResponse> weaknesses;

    public CardResponse(Card card) {
        this.id = card.getId();
        this.name = card.getName();
        this.supertype = card.getSupertype();
        this.hp = card.getHp();
        this.hpNumeric = card.getHpNumeric();
        this.convertedRetreatCost = card.getConvertedRetreatCost();
        this.retreatCost = card.getRetreatCosts().stream()
                .flatMap(rc -> {
                    String typeName = rc.getType().getName();
                    Integer quantity = rc.getQuantity();
                    return java.util.stream.Stream.generate(() -> typeName)
                            .limit(quantity);
                })
                .collect(Collectors.toList());
        this.number = card.getNumber();
        this.setId = card.getSetId();
        this.setName = card.getPokemonSet() != null ? card.getPokemonSet().getName() : null;
        this.artistName = card.getArtist() != null ? card.getArtist().getName() : null;
        this.rarityName = card.getRarity() != null ? card.getRarity().getName() : null;
        this.flavorText = card.getFlavorText();
        this.imageLow = card.getImageLow();
        this.imageHigh = card.getImageHigh();
        this.regulationMark = card.getRegulationMark();
        this.level = card.getLevel();
        this.subtypes = card.getSubtypes().stream()
                .map(Subtype::getName)
                .collect(Collectors.toSet());
        this.types = card.getTypes().stream()
                .map(Type::getName)
                .collect(Collectors.toSet());
        this.ancientTrait = card.getAncientTrait() != null ?
                new AncientTraitResponse(card.getAncientTrait()) : null;
        this.abilities = card.getAbilities().stream()
                .map(AbilityResponse::new)
                .collect(Collectors.toSet());
        this.attacks = card.getAttacks().stream()
                .map(AttackResponse::new)
                .collect(Collectors.toSet());
        this.rules = card.getRules().stream()
                .map(Rule::getText)
                .collect(Collectors.toSet());
        this.pokedexNumbers = card.getPokedexNumbers().stream()
                .map(Pokedex::getNumber)
                .collect(Collectors.toSet());
        this.legalities = card.getLegalities().stream()
                .collect(Collectors.toMap(
                        legality -> legality.getFormat().getName(),
                        legality -> legality.getStatus().name()
                ));
        this.evolvesFrom = card.getEvolutions().stream()
                .filter(evo -> evo.getDirection() == EvolutionDirection.from)
                .map(evo -> evo.getName().getName())
                .collect(Collectors.toList());
        this.evolvesTo = card.getEvolutions().stream()
                .filter(evo -> evo.getDirection() == EvolutionDirection.to)
                .map(evo -> evo.getName().getName())
                .collect(Collectors.toList());
        this.resistances = card.getResistances().stream()
                .map(cr -> new ResistanceResponse(cr.getResistance()))
                .collect(Collectors.toSet());
        this.weaknesses = card.getWeaknesses().stream()
                .map(cw -> new WeaknessResponse(cw.getWeakness()))
                .collect(Collectors.toSet());
    }

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

    public List<String> getRetreatCost() {
        return retreatCost;
    }

    public void setRetreatCost(List<String> retreatCost) {
        this.retreatCost = retreatCost;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getRarityName() {
        return rarityName;
    }

    public void setRarityName(String rarityName) {
        this.rarityName = rarityName;
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

    public Set<String> getSubtypes() {
        return subtypes;
    }

    public void setSubtypes(Set<String> subtypes) {
        this.subtypes = subtypes;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public AncientTraitResponse getAncientTrait() {
        return ancientTrait;
    }

    public void setAncientTrait(AncientTraitResponse ancientTrait) {
        this.ancientTrait = ancientTrait;
    }

    public Set<AbilityResponse> getAbilities() {
        return abilities;
    }

    public void setAbilities(Set<AbilityResponse> abilities) {
        this.abilities = abilities;
    }

    public Set<AttackResponse> getAttacks() {
        return attacks;
    }

    public void setAttacks(Set<AttackResponse> attacks) {
        this.attacks = attacks;
    }

    public Set<String> getRules() {
        return rules;
    }

    public void setRules(Set<String> rules) {
        this.rules = rules;
    }

    public Set<Integer> getPokedexNumbers() {
        return pokedexNumbers;
    }

    public void setPokedexNumbers(Set<Integer> pokedexNumbers) {
        this.pokedexNumbers = pokedexNumbers;
    }

    public Map<String, String> getLegalities() {
        return legalities;
    }

    public void setLegalities(Map<String, String> legalities) {
        this.legalities = legalities;
    }

    public List<String> getEvolvesFrom() {
        return evolvesFrom;
    }

    public void setEvolvesFrom(List<String> evolvesFrom) {
        this.evolvesFrom = evolvesFrom;
    }

    public List<String> getEvolvesTo() {
        return evolvesTo;
    }

    public void setEvolvesTo(List<String> evolvesTo) {
        this.evolvesTo = evolvesTo;
    }

    public Set<ResistanceResponse> getResistances() {
        return resistances;
    }

    public void setResistances(Set<ResistanceResponse> resistances) {
        this.resistances = resistances;
    }

    public Set<WeaknessResponse> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(Set<WeaknessResponse> weaknesses) {
        this.weaknesses = weaknesses;
    }
}