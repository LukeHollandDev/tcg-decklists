package dev.lukeholland.tcg.decklists.api.decklist.dto;

import dev.lukeholland.tcg.decklists.api.enums.CardGame;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request for creating a decklist with name, card game type, and list of card IDs (duplicates allowed)")
public record DecklistRequest(
        @NotBlank(message = "Decklist name is required and cannot be blank")
        String name,

        @NotNull(message = "Card game type is required")
        CardGame type,

        @NotEmpty(message = "Decklist must contain at least one card")
        List<@NotBlank(message = "Card ID cannot be blank") String> cards
) {
}
