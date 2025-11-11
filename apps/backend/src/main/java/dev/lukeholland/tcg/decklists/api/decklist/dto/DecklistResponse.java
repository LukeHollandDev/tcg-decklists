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
public record DecklistResponse(
        Integer id,
        String name,
        CardGame type,
        List<String> cards,
        LocalDateTime createdAt
) {
    /**
     * Constructs a DecklistResponse from a Decklist entity.
     * Expands cards with quantities into a flat list (e.g., quantity 3 becomes 3 entries).
     *
     * @param decklist the decklist entity to transform
     */
    public DecklistResponse(Decklist decklist) {
        this(
                decklist.getId(),
                decklist.getName(),
                decklist.getType(),
                decklist.getCards().stream()
                        .flatMap(decklistCard -> {
                            String cardId = decklistCard.getCardId();
                            Integer quantity = decklistCard.getQuantity();
                            return Stream.generate(() -> cardId)
                                    .limit(quantity);
                        })
                        .collect(Collectors.toList()),
                decklist.getCreatedAt()
        );
    }
}
