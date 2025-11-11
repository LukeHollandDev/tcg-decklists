package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AttackResponse(
        String name,
        Integer convertedCost,
        String damage,
        Integer damageNumeric,
        String damageModifier,
        String text,
        List<String> cost
) {
    public AttackResponse(dev.lukeholland.tcg.decklists.api.pokemon.entities.Attack attack) {
        this(
                attack.getName(),
                attack.getConvertedCost(),
                attack.getDamage(),
                attack.getDamageNumeric(),
                attack.getDamageModifier(),
                attack.getText(),
                attack.getCosts().stream()
                        .flatMap(c -> {
                            String typeName = c.getType().getName();
                            Integer quantity = c.getQuantity();
                            return java.util.stream.Stream.generate(() -> typeName)
                                    .limit(quantity);
                        })
                        .collect(Collectors.toList())
        );
    }
}
