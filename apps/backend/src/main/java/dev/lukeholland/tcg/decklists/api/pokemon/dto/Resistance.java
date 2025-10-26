package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Resistance {
    private String type;
    private String value;

    public Resistance(dev.lukeholland.tcg.decklists.api.pokemon.entities.Resistance resistance) {
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
