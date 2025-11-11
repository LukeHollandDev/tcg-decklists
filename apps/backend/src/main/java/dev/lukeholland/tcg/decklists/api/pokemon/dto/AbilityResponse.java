package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AbilityResponse(
        String name,
        String text,
        String type
) {
    public AbilityResponse(dev.lukeholland.tcg.decklists.api.pokemon.entities.Ability ability) {
        this(ability.getName(), ability.getText(), ability.getType());
    }
}
