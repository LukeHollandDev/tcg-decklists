package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Attack {
    private String name;
    private Integer convertedCost;
    private String damage;
    private Integer damageNumeric;
    private String damageModifier;
    private String text;
    private List<String> cost;

    public Attack(dev.lukeholland.tcg.decklists.api.pokemon.entities.Attack attack) {
        this.name = attack.getName();
        this.convertedCost = attack.getConvertedCost();
        this.damage = attack.getDamage();
        this.damageNumeric = attack.getDamageNumeric();
        this.damageModifier = attack.getDamageModifier();
        this.text = attack.getText();
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
