package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record WeaknessResponse(
        String type,
        String value
) {
    public WeaknessResponse(dev.lukeholland.tcg.decklists.api.pokemon.entities.Weakness weakness) {
        this(
                weakness.getType() != null ? weakness.getType().getName() : null,
                weakness.getValue()
        );
    }
}
