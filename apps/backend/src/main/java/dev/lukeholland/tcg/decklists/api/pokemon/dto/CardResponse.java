package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.*;
import dev.lukeholland.tcg.decklists.api.pokemon.enums.EvolutionDirection;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private AncientTraitDTO ancientTrait;
    private Set<AbilityDTO> abilities;
    private Set<AttackDTO> attacks;
    private Set<String> rules;
    private Set<Integer> pokedexNumbers;
    private Map<String, String> legalities;
    private List<String> evolvesFrom;
    private List<String> evolvesTo;
    private Set<ResistanceDTO> resistances;
    private Set<WeaknessDTO> weaknesses;

    // Constructor that converts from entity
    public CardResponse(Card card) {
        this.id = card.getId();
        this.name = card.getName();
        this.supertype = card.getSupertype();
        this.hp = card.getHp();
        this.hpNumeric = card.getHpNumeric();
        this.convertedRetreatCost = card.getConvertedRetreatCost();
        // Expand retreat cost quantities: if Colorless has quantity 3, output ["Colorless", "Colorless", "Colorless"]
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
                new AncientTraitDTO(card.getAncientTrait()) : null;
        this.abilities = card.getAbilities().stream()
                .map(AbilityDTO::new)
                .collect(Collectors.toSet());
        this.attacks = card.getAttacks().stream()
                .map(AttackDTO::new)
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
                .map(cr -> new ResistanceDTO(cr.getResistance()))
                .collect(Collectors.toSet());
        this.weaknesses = card.getWeaknesses().stream()
                .map(cw -> new WeaknessDTO(cw.getWeakness()))
                .collect(Collectors.toSet());
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

    public AncientTraitDTO getAncientTrait() {
        return ancientTrait;
    }

    public void setAncientTrait(AncientTraitDTO ancientTrait) {
        this.ancientTrait = ancientTrait;
    }

    public Set<AbilityDTO> getAbilities() {
        return abilities;
    }

    public void setAbilities(Set<AbilityDTO> abilities) {
        this.abilities = abilities;
    }

    public Set<AttackDTO> getAttacks() {
        return attacks;
    }

    public void setAttacks(Set<AttackDTO> attacks) {
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

    public Set<ResistanceDTO> getResistances() {
        return resistances;
    }

    public void setResistances(Set<ResistanceDTO> resistances) {
        this.resistances = resistances;
    }

    public Set<WeaknessDTO> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(Set<WeaknessDTO> weaknesses) {
        this.weaknesses = weaknesses;
    }

    // Nested DTOs for complex objects
    public static class AncientTraitDTO {
        private String name;
        private String text;

        public AncientTraitDTO(AncientTrait trait) {
            this.name = trait.getName();
            this.text = trait.getText();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class AbilityDTO {
        private String name;
        private String text;
        private String type;

        public AbilityDTO(Ability ability) {
            this.name = ability.getName();
            this.text = ability.getText();
            this.type = ability.getType();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class AttackDTO {
        private String name;
        private Integer convertedCost;
        private String damage;
        private Integer damageNumeric;
        private String damageModifier;
        private String text;
        private List<String> cost;

        public AttackDTO(Attack attack) {
            this.name = attack.getName();
            this.convertedCost = attack.getConvertedCost();
            this.damage = attack.getDamage();
            this.damageNumeric = attack.getDamageNumeric();
            this.damageModifier = attack.getDamageModifier();
            this.text = attack.getText();
            // Expand quantities: if Grass has quantity 3, output ["Grass", "Grass", "Grass"]
            this.cost = attack.getCosts().stream()
                    .flatMap(c -> {
                        String typeName = c.getType().getName();
                        Integer quantity = c.getQuantity();
                        return java.util.stream.Stream.generate(() -> typeName)
                                .limit(quantity);
                    })
                    .collect(Collectors.toList());
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getConvertedCost() {
            return convertedCost;
        }

        public void setConvertedCost(Integer convertedCost) {
            this.convertedCost = convertedCost;
        }

        public String getDamage() {
            return damage;
        }

        public void setDamage(String damage) {
            this.damage = damage;
        }

        public Integer getDamageNumeric() {
            return damageNumeric;
        }

        public void setDamageNumeric(Integer damageNumeric) {
            this.damageNumeric = damageNumeric;
        }

        public String getDamageModifier() {
            return damageModifier;
        }

        public void setDamageModifier(String damageModifier) {
            this.damageModifier = damageModifier;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<String> getCost() {
            return cost;
        }

        public void setCost(List<String> cost) {
            this.cost = cost;
        }
    }

    public static class ResistanceDTO {
        private String type;
        private String value;

        public ResistanceDTO(Resistance resistance) {
            this.type = resistance.getType() != null ? resistance.getType().getName() : null;
            this.value = resistance.getValue();
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class WeaknessDTO {
        private String type;
        private String value;

        public WeaknessDTO(Weakness weakness) {
            this.type = weakness.getType() != null ? weakness.getType().getName() : null;
            this.value = weakness.getValue();
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}