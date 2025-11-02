package dev.lukeholland.tcg.decklists.api.decklist.dto;

import dev.lukeholland.tcg.decklists.api.enums.CardGame;

import java.util.List;

/**
 * Request DTO for creating a decklist.
 * Contains the deck name, card game type, and list of card IDs (allowing duplicates).
 */
public class DecklistRequest {

    /**
     * Name of the decklist
     */
    private String name;

    /**
     * Card game type (e.g., "pokemon")
     * Accepts string values that map to CardGame enum (case-insensitive)
     */
    private CardGame type;

    /**
     * List of card IDs, allowing duplicates for multiple copies of the same card
     */
    private List<String> cards;

    public DecklistRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CardGame getType() {
        return type;
    }

    public void setType(CardGame type) {
        this.type = type;
    }

    public List<String> getCards() {
        return cards;
    }

    public void setCards(List<String> cards) {
        this.cards = cards;
    }
}
