package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AncientTraitResponse(
        String name,
        String text
) {
    public AncientTraitResponse(dev.lukeholland.tcg.decklists.api.pokemon.entities.AncientTrait trait) {
        this(trait.getName(), trait.getText());
    }
}
