package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AncientTrait {
    private String name;
    private String text;

    public AncientTrait(dev.lukeholland.tcg.decklists.api.pokemon.entities.AncientTrait trait) {
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
