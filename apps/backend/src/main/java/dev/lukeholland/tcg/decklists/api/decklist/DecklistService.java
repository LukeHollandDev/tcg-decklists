package dev.lukeholland.tcg.decklists.api.decklist;

import dev.lukeholland.tcg.decklists.api.decklist.dto.DecklistRequest;
import dev.lukeholland.tcg.decklists.api.decklist.entities.Decklist;
import dev.lukeholland.tcg.decklists.api.decklist.entities.DecklistCard;
import dev.lukeholland.tcg.decklists.api.pokemon.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for decklist operations.
 * Handles business logic including card validation and quantity aggregation.
 */
@Service
@Transactional(readOnly = true)
public class DecklistService {

    private final DecklistRepository decklistRepository;
    private final Repository pokemonCardRepository;

    public DecklistService(DecklistRepository decklistRepository, Repository pokemonCardRepository) {
        this.decklistRepository = decklistRepository;
        this.pokemonCardRepository = pokemonCardRepository;
    }

    /**
     * Find a decklist by its ID.
     *
     * @param id the decklist ID
     * @return Optional containing the decklist if found, empty otherwise
     */
    public Optional<Decklist> findById(Integer id) {
        return decklistRepository.findById(id);
    }

    /**
     * Create a new decklist from a request.
     * Validates all card IDs exist, aggregates duplicate cards into quantities,
     * and saves the decklist to the database.
     *
     * @param request the decklist creation request
     * @return the created decklist with generated ID
     * @throws IllegalArgumentException if any card IDs are invalid
     */
    @Transactional
    public Decklist createDecklist(DecklistRequest request) {
        // Validate request
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Decklist name is required");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Decklist type is required");
        }
        if (request.getCards() == null || request.getCards().isEmpty()) {
            throw new IllegalArgumentException("Decklist must contain at least one card");
        }

        // Get unique card IDs
        Set<String> uniqueCardIds = new HashSet<>(request.getCards());

        // Validate all card IDs exist
        List<String> invalidCardIds = uniqueCardIds.stream()
                .filter(cardId -> !pokemonCardRepository.existsById(cardId))
                .collect(Collectors.toList());

        if (!invalidCardIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid card IDs: " + String.join(", ", invalidCardIds)
            );
        }

        // Count occurrences of each card ID
        Map<String, Long> cardQuantities = request.getCards().stream()
                .collect(Collectors.groupingBy(
                        cardId -> cardId,
                        Collectors.counting()
                ));

        // Create decklist entity
        Decklist decklist = new Decklist();
        decklist.setName(request.getName());
        decklist.setType(request.getType());

        // Create DecklistCard entities with quantities
        cardQuantities.forEach((cardId, quantity) -> {
            DecklistCard decklistCard = new DecklistCard();
            decklistCard.setCardId(cardId);
            decklistCard.setQuantity(quantity.intValue());
            decklist.addCard(decklistCard);
        });

        // Save and return
        return decklistRepository.save(decklist);
    }
}
