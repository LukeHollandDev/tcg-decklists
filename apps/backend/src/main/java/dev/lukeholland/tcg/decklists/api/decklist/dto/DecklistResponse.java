package dev.lukeholland.tcg.decklists.api.decklist.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.lukeholland.tcg.decklists.api.decklist.entities.Decklist;
import dev.lukeholland.tcg.decklists.api.enums.CardGame;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Response DTO for decklist data.
 * Transforms the entity to API response format, expanding card quantities into a flat list.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DecklistResponse {

    private Integer id;
    private String name;
    private CardGame type;
    private List<String> cards;
    private LocalDateTime createdAt;

    public DecklistResponse() {
    }

    /**
     * Constructs a DecklistResponse from a Decklist entity.
     * Expands cards with quantities into a flat list (e.g., quantity 3 becomes 3 entries).
     *
     * @param decklist the decklist entity to transform
     */
    public DecklistResponse(Decklist decklist) {
        this.id = decklist.getId();
        this.name = decklist.getName();
        this.type = decklist.getType();
        this.createdAt = decklist.getCreatedAt();

        // Expand cards with quantities into a flat list
        this.cards = decklist.getCards().stream()
                .flatMap(decklistCard -> {
                    String cardId = decklistCard.getCardId();
                    Integer quantity = decklistCard.getQuantity();
                    return Stream.generate(() -> cardId)
                            .limit(quantity);
                })
                .collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
