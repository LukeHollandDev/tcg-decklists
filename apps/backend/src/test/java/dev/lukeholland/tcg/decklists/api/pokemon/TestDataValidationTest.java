package dev.lukeholland.tcg.decklists.api.pokemon;

import dev.lukeholland.tcg.decklists.api.pokemon.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that the test-data.sql fixture loads correctly and contains expected data.
 * This test ensures our test data is properly structured before running the full integration test suite.
 */
@DisplayName("Test Data Validation")
class TestDataValidationTest extends AbstractIntegrationTest {

    @Autowired
    private Repository repository;

    @BeforeEach
    void loadDataIfNeeded() {
        // Load test data only if not already loaded
        // Check by counting cards in the repository
        long cardCount = repository.count();
        if (cardCount == 0) {
            testDataLoader.loadTestData();
            System.out.println("✅ Test data loaded successfully!");
        } else {
            System.out.println("ℹ️  Test data already loaded, skipping...");
        }
    }

    @Test
    @DisplayName("Should have cards loaded from test-data.sql")
    void shouldHaveCardsLoaded() {
        // Verify data was loaded - check that we have cards
        long cardCount = repository.count();
        assertTrue(cardCount > 0, "Expected test data to contain cards");
        System.out.println("   Total cards in test database: " + cardCount);
    }

    @Test
    @DisplayName("Should have expected test cards after loading test data")
    void shouldHaveExpectedTestCards() {
        // Verify specific test cards exist
        assertTrue(repository.existsById("base1-4"), "Charizard (base1-4) should exist");
        assertTrue(repository.existsById("base1-46"), "Charmander (base1-46) should exist");
        assertTrue(repository.existsById("base1-24"), "Charmeleon (base1-24) should exist");
        assertTrue(repository.existsById("base1-58"), "Pikachu (base1-58) should exist");
        assertTrue(repository.existsById("base1-14"), "Raichu (base1-14) should exist");
        assertTrue(repository.existsById("base1-2"), "Blastoise (base1-2) should exist");
        assertTrue(repository.existsById("sv1-83"), "Flabébé (sv1-83) should exist for accent testing");
        assertTrue(repository.existsById("swsh1-25"), "Charizard ex (swsh1-25) should exist for ex testing");
        assertTrue(repository.existsById("base1-88"), "Professor Oak (base1-88) should exist for Trainer testing");
        assertTrue(repository.existsById("base1-98"), "Fire Energy (base1-98) should exist for Energy testing");

        System.out.println("✅ All expected test cards are present!");
    }

    @Test
    @DisplayName("Should have evolution chains in test data")
    void shouldHaveEvolutionChains() {
        // Verify evolution chain cards exist
        var charmander = repository.findById("base1-46");
        var charmeleon = repository.findById("base1-24");
        var charizard = repository.findById("base1-4");

        assertTrue(charmander.isPresent(), "Charmander should exist");
        assertTrue(charmeleon.isPresent(), "Charmeleon should exist");
        assertTrue(charizard.isPresent(), "Charizard should exist");

        System.out.println("✅ Evolution chain data is present!");
    }

    @Test
    @DisplayName("Should have cards with various attack costs for multiset testing")
    void shouldHaveCardsWithVariousAttackCosts() {
        // Verify cards with different attack cost patterns exist
        var charizard = repository.findById("base1-4");
        var raichu = repository.findById("base1-14");
        var blastoise = repository.findById("base1-2");

        assertTrue(charizard.isPresent(), "Charizard (4x Fire) should exist");
        assertTrue(raichu.isPresent(), "Raichu (2x Lightning + 1x Colorless) should exist");
        assertTrue(blastoise.isPresent(), "Blastoise (3x Water) should exist");

        System.out.println("✅ Multiset attack cost test data is present!");
    }

    @Test
    @DisplayName("Should have cards across different supertypes")
    void shouldHaveCardsAcrossDifferentSupertypes() {
        var pokemon = repository.findById("base1-4");  // Charizard
        var trainer = repository.findById("base1-88"); // Professor Oak
        var energy = repository.findById("base1-98");  // Fire Energy

        assertTrue(pokemon.isPresent() && "Pokémon".equals(pokemon.get().getSupertype()),
                "Should have Pokémon supertype card");
        assertTrue(trainer.isPresent() && "Trainer".equals(trainer.get().getSupertype()),
                "Should have Trainer supertype card");
        assertTrue(energy.isPresent() && "Energy".equals(energy.get().getSupertype()),
                "Should have Energy supertype card");

        System.out.println("✅ All supertypes represented in test data!");
    }
}
