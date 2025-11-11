package dev.lukeholland.tcg.decklists.api.decklist.dto;

import dev.lukeholland.tcg.decklists.api.enums.CardGame;

import java.util.List;

/**
 * Request DTO for creating a decklist.
 * Contains the deck name, card game type, and list of card IDs (allowing duplicates).
 */
public record DecklistRequest(
        String name,
        CardGame type,
        List<String> cards
) {
}
