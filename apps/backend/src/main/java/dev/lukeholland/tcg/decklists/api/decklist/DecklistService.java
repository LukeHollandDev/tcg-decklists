package dev.lukeholland.tcg.decklists.api.decklist;

import dev.lukeholland.tcg.decklists.api.decklist.dto.DecklistRequest;
import dev.lukeholland.tcg.decklists.api.decklist.entities.Decklist;
import dev.lukeholland.tcg.decklists.api.decklist.entities.DecklistCard;
import dev.lukeholland.tcg.decklists.api.pokemon.PokemonCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DecklistService {

    private final DecklistRepository decklistRepository;
    private final PokemonCardRepository pokemonCardRepository;

    public DecklistService(DecklistRepository decklistRepository, PokemonCardRepository pokemonCardRepository) {
        this.decklistRepository = decklistRepository;
        this.pokemonCardRepository = pokemonCardRepository;
    }

    public Optional<Decklist> findById(Integer id) {
        return decklistRepository.findById(id);
    }

    @Transactional
    public Decklist createDecklist(DecklistRequest request) {
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Decklist name is required");
        }
        if (request.type() == null) {
            throw new IllegalArgumentException("Decklist type is required");
        }
        if (request.cards() == null || request.cards().isEmpty()) {
            throw new IllegalArgumentException("Decklist must contain at least one card");
        }

        Set<String> uniqueCardIds = new HashSet<>(request.cards());

        List<String> invalidCardIds = uniqueCardIds.stream()
                .filter(cardId -> !pokemonCardRepository.existsById(cardId))
                .collect(Collectors.toList());

        if (!invalidCardIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid card IDs: " + String.join(", ", invalidCardIds)
            );
        }

        Map<String, Long> cardQuantities = request.cards().stream()
                .collect(Collectors.groupingBy(
                        cardId -> cardId,
                        Collectors.counting()
                ));

        Decklist decklist = new Decklist();
        decklist.setName(request.name());
        decklist.setType(request.type());

        cardQuantities.forEach((cardId, quantity) -> {
            DecklistCard decklistCard = new DecklistCard();
            decklistCard.setCardId(cardId);
            decklistCard.setQuantity(quantity.intValue());
            decklist.addCard(decklistCard);
        });

        return decklistRepository.save(decklist);
    }
}
