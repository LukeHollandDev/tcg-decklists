package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Ability {
    private String name;
    private String text;
    private String type;

    public Ability(dev.lukeholland.tcg.decklists.api.pokemon.entities.Ability ability) {
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
