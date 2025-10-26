package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Weakness {
    private String type;
    private String value;

    public Weakness(dev.lukeholland.tcg.decklists.api.pokemon.entities.Weakness weakness) {
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
