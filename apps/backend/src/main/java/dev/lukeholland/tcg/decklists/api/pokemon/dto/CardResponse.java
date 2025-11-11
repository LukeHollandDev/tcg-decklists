package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.lukeholland.tcg.decklists.api.pokemon.entities.*;
import dev.lukeholland.tcg.decklists.api.pokemon.enums.EvolutionDirection;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CardResponse(
        String id,
        String name,
        String supertype,
        String hp,
        Integer hpNumeric,
        Integer convertedRetreatCost,
        List<String> retreatCost,
        String number,
        String setId,
        String setName,
        String artistName,
        String rarityName,
        String flavorText,
        String imageLow,
        String imageHigh,
        String regulationMark,
        String level,
        Set<String> subtypes,
        Set<String> types,
        AncientTraitResponse ancientTrait,
        Set<AbilityResponse> abilities,
        Set<AttackResponse> attacks,
        Set<String> rules,
        Set<Integer> pokedexNumbers,
        Map<String, String> legalities,
        List<String> evolvesFrom,
        List<String> evolvesTo,
        Set<ResistanceResponse> resistances,
        Set<WeaknessResponse> weaknesses
) {
    public CardResponse(Card card) {
        this(
                card.getId(),
                card.getName(),
                card.getSupertype(),
                card.getHp(),
                card.getHpNumeric(),
                card.getConvertedRetreatCost(),
                card.getRetreatCosts().stream()
                        .flatMap(rc -> {
                            String typeName = rc.getType().getName();
                            Integer quantity = rc.getQuantity();
                            return java.util.stream.Stream.generate(() -> typeName)
                                    .limit(quantity);
                        })
                        .collect(Collectors.toList()),
                card.getNumber(),
                card.getSetId(),
                card.getPokemonSet() != null ? card.getPokemonSet().getName() : null,
                card.getArtist() != null ? card.getArtist().getName() : null,
                card.getRarity() != null ? card.getRarity().getName() : null,
                card.getFlavorText(),
                card.getImageLow(),
                card.getImageHigh(),
                card.getRegulationMark(),
                card.getLevel(),
                card.getSubtypes().stream()
                        .map(Subtype::getName)
                        .collect(Collectors.toSet()),
                card.getTypes().stream()
                        .map(Type::getName)
                        .collect(Collectors.toSet()),
                card.getAncientTrait() != null ?
                        new AncientTraitResponse(card.getAncientTrait()) : null,
                card.getAbilities().stream()
                        .map(AbilityResponse::new)
                        .collect(Collectors.toSet()),
                card.getAttacks().stream()
                        .map(AttackResponse::new)
                        .collect(Collectors.toSet()),
                card.getRules().stream()
                        .map(Rule::getText)
                        .collect(Collectors.toSet()),
                card.getPokedexNumbers().stream()
                        .map(Pokedex::getNumber)
                        .collect(Collectors.toSet()),
                card.getLegalities().stream()
                        .collect(Collectors.toMap(
                                legality -> legality.getFormat().getName(),
                                legality -> legality.getStatus().name()
                        )),
                card.getEvolutions().stream()
                        .filter(evo -> evo.getDirection() == EvolutionDirection.from)
                        .map(evo -> evo.getName().getName())
                        .collect(Collectors.toList()),
                card.getEvolutions().stream()
                        .filter(evo -> evo.getDirection() == EvolutionDirection.to)
                        .map(evo -> evo.getName().getName())
                        .collect(Collectors.toList()),
                card.getResistances().stream()
                        .map(cr -> new ResistanceResponse(cr.getResistance()))
                        .collect(Collectors.toSet()),
                card.getWeaknesses().stream()
                        .map(cw -> new WeaknessResponse(cw.getWeakness()))
                        .collect(Collectors.toSet())
        );
    }
}
