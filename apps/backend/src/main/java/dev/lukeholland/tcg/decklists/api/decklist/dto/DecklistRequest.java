package dev.lukeholland.tcg.decklists.api.decklist.dto;

import dev.lukeholland.tcg.decklists.api.enums.CardGame;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request for creating a decklist with name, card game type, and list of card IDs (duplicates allowed)")
public record DecklistRequest(
        String name,
        CardGame type,
        List<String> cards
) {
}
