package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ResistanceResponse(
        String type,
        String value
) {
    public ResistanceResponse(dev.lukeholland.tcg.decklists.api.pokemon.entities.Resistance resistance) {
        this(
                resistance.getType() != null ? resistance.getType().getName() : null,
                resistance.getValue()
        );
    }
}
