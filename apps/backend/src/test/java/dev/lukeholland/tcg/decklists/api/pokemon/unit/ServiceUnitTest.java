package dev.lukeholland.tcg.decklists.api.pokemon.unit;

import dev.lukeholland.tcg.decklists.api.pokemon.Repository;
import dev.lukeholland.tcg.decklists.api.pokemon.Service;
import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardSearchRequest;
import dev.lukeholland.tcg.decklists.api.pokemon.dto.CardSearchResponse;
import dev.lukeholland.tcg.decklists.api.pokemon.dto.FilterOptionsResponse;
import dev.lukeholland.tcg.decklists.api.pokemon.entities.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Pokemon Card Service.
 * Tests service layer logic in isolation with mocked repository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pokemon Card Service - Unit Tests")
class ServiceUnitTest {

    @Mock
    private Repository repository;

    private Service service;

    @BeforeEach
    void setUp() {
        service = new Service(repository);
    }

    // ============================================================================
    // findById Tests
    // ============================================================================

    @Test
    @DisplayName("Should find card by ID when it exists")
    void shouldFindCardByIdWhenExists() {
        // Given
        String cardId = "base1-1";
        Card mockCard = new Card();
        mockCard.setId(cardId);
        when(repository.findById(cardId)).thenReturn(Optional.of(mockCard));

        // When
        Optional<Card> result = service.findById(cardId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(cardId);
        verify(repository).findById(cardId);
    }

    @Test
    @DisplayName("Should return empty Optional when card not found")
    void shouldReturnEmptyOptionalWhenCardNotFound() {
        // Given
        String cardId = "nonexistent-1";
        when(repository.findById(cardId)).thenReturn(Optional.empty());

        // When
        Optional<Card> result = service.findById(cardId);

        // Then
        assertThat(result).isEmpty();
        verify(repository).findById(cardId);
    }

    // ============================================================================
    // existsById Tests
    // ============================================================================

    @Test
    @DisplayName("Should return true when card exists")
    void shouldReturnTrueWhenCardExists() {
        // Given
        String cardId = "base1-1";
        when(repository.existsById(cardId)).thenReturn(true);

        // When
        boolean result = service.existsById(cardId);

        // Then
        assertThat(result).isTrue();
        verify(repository).existsById(cardId);
    }

    @Test
    @DisplayName("Should return false when card does not exist")
    void shouldReturnFalseWhenCardDoesNotExist() {
        // Given
        String cardId = "nonexistent-1";
        when(repository.existsById(cardId)).thenReturn(false);

        // When
        boolean result = service.existsById(cardId);

        // Then
        assertThat(result).isFalse();
        verify(repository).existsById(cardId);
    }

    // ============================================================================
    // search Tests
    // ============================================================================

    @Test
    @DisplayName("Should execute search with pagination")
    void shouldExecuteSearchWithPagination() {
        // Given
        CardSearchRequest request = new CardSearchRequest();
        request.setPage(0);
        request.setPageSize(20);

        Card mockCard = new Card();
        mockCard.setId("base1-1");
        Page<Card> mockPage = new PageImpl<>(List.of(mockCard));

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        // When
        CardSearchResponse response = service.search(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalResults()).isEqualTo(1);
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle empty search results")
    void shouldHandleEmptySearchResults() {
        // Given
        CardSearchRequest request = new CardSearchRequest();
        request.setName("NonExistentCard");

        Page<Card> emptyPage = new PageImpl<>(List.of());
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        CardSearchResponse response = service.search(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalResults()).isZero();
        assertThat(response.getResults()).isEmpty();
    }

    // ============================================================================
    // getFilterOptions Tests
    // ============================================================================

    @Test
    @DisplayName("Should retrieve static filter data and filter metadata")
    void shouldRetrieveAllFilterOptions() {
        // Given
        when(repository.findDistinctSupertypes()).thenReturn(List.of("Pokémon", "Trainer", "Energy"));
        when(repository.findDistinctTypes()).thenReturn(List.of("Fire", "Water", "Grass"));
        when(repository.findDistinctSubtypes()).thenReturn(List.of("Basic", "Stage 1", "Stage 2"));
        when(repository.findDistinctSetIds()).thenReturn(List.of("base1", "base2"));
        when(repository.findDistinctRarities()).thenReturn(List.of("Common", "Rare"));
        when(repository.findDistinctFormats()).thenReturn(List.of("Standard", "Expanded"));
        when(repository.findDistinctRegulationMarks()).thenReturn(List.of("E", "F"));

        // When
        FilterOptionsResponse response = service.getFilterOptions();

        // Then - Verify response structure
        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo("pokemon");

        // Verify static data
        assertThat(response.getStaticData()).isNotNull();
        assertThat(response.getStaticData().getSupertypes()).containsExactly("Pokémon", "Trainer", "Energy");
        assertThat(response.getStaticData().getTypes()).containsExactly("Fire", "Water", "Grass");
        assertThat(response.getStaticData().getSubtypes()).containsExactly("Basic", "Stage 1", "Stage 2");
        assertThat(response.getStaticData().getSets()).containsExactly("base1", "base2");
        assertThat(response.getStaticData().getRarities()).containsExactly("Common", "Rare");
        assertThat(response.getStaticData().getFormats()).containsExactly("Standard", "Expanded");
        assertThat(response.getStaticData().getRegulationMarks()).containsExactly("E", "F");

        // Verify filter metadata exists
        assertThat(response.getFilters()).isNotNull();
        assertThat(response.getFilters()).isNotEmpty();

        // Verify some key filters are present
        assertThat(response.getFilters()).containsKeys("name", "supertype", "types", "hp", "attackName", "hasAbility");

        // Verify a string filter metadata
        assertThat(response.getFilters().get("name").getType()).isEqualTo("string");
        assertThat(response.getFilters().get("name").getParameterName()).isEqualTo("name");

        // Verify an enum filter metadata
        assertThat(response.getFilters().get("supertype").getType()).isEqualTo("enum");
        assertThat(response.getFilters().get("supertype").getValuesRef()).isEqualTo("static.supertypes");

        // Verify a multiselect filter metadata
        assertThat(response.getFilters().get("types").getType()).isEqualTo("multiselect");
        assertThat(response.getFilters().get("types").getMatchAllParameter()).isEqualTo("typesMatchAll");

        // Verify a range filter metadata
        assertThat(response.getFilters().get("hp").getType()).isEqualTo("range");
        assertThat(response.getFilters().get("hp").getMinParameter()).isEqualTo("hpMin");
        assertThat(response.getFilters().get("hp").getMaxParameter()).isEqualTo("hpMax");

        // Verify a boolean filter metadata
        assertThat(response.getFilters().get("hasAbility").getType()).isEqualTo("boolean");

        verify(repository).findDistinctSupertypes();
        verify(repository).findDistinctTypes();
        verify(repository).findDistinctSubtypes();
        verify(repository).findDistinctSetIds();
        verify(repository).findDistinctRarities();
        verify(repository).findDistinctFormats();
        verify(repository).findDistinctRegulationMarks();
    }

    // ============================================================================
    // autocompleteArtists Tests
    // ============================================================================

    @Test
    @DisplayName("Should return prefix matches first for artist autocomplete")
    void shouldReturnPrefixMatchesFirstForArtists() {
        // Given
        String query = "Ken";
        int limit = 5;

        // Return exactly limit results so substring is never called
        when(repository.findArtistNamesByPrefix(query, limit))
                .thenReturn(List.of("Ken Sugimori", "Ken Ikuji", "Kent Kanetsuna", "Kenkichi Toyama", "Kenny Doe"));

        // When
        List<String> results = service.autocompleteArtists(query, limit);

        // Then
        assertThat(results).containsExactly("Ken Sugimori", "Ken Ikuji", "Kent Kanetsuna", "Kenkichi Toyama", "Kenny Doe");
        assertThat(results).hasSize(5);
        verify(repository).findArtistNamesByPrefix(query, limit);
        verify(repository, never()).findArtistNamesBySubstring(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should fill with substring matches when prefix matches insufficient")
    void shouldFillWithSubstringMatchesForArtists() {
        // Given
        String query = "mori";
        int limit = 5;

        when(repository.findArtistNamesByPrefix(query, limit))
                .thenReturn(List.of("Mori Takeda"));
        when(repository.findArtistNamesBySubstring(query, 4))
                .thenReturn(List.of("Ken Sugimori", "Arita Mori", "Takeshi Morimoto"));

        // When
        List<String> results = service.autocompleteArtists(query, limit);

        // Then
        assertThat(results).hasSize(4);
        assertThat(results.get(0)).isEqualTo("Mori Takeda"); // Prefix match comes first
        assertThat(results).contains("Ken Sugimori", "Arita Mori", "Takeshi Morimoto");
        verify(repository).findArtistNamesByPrefix(query, limit);
        verify(repository).findArtistNamesBySubstring(query, 4);
    }

    @Test
    @DisplayName("Should handle empty query for autocomplete")
    void shouldHandleEmptyQueryForAutocomplete() {
        // Given
        String query = "";
        int limit = 10;

        // When
        List<String> results = service.autocompleteArtists(query, limit);

        // Then
        assertThat(results).isEmpty();
        verify(repository, never()).findArtistNamesByPrefix(anyString(), anyInt());
        verify(repository, never()).findArtistNamesBySubstring(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should handle null query for autocomplete")
    void shouldHandleNullQueryForAutocomplete() {
        // Given
        String query = null;
        int limit = 10;

        // When
        List<String> results = service.autocompleteArtists(query, limit);

        // Then
        assertThat(results).isEmpty();
        verify(repository, never()).findArtistNamesByPrefix(anyString(), anyInt());
        verify(repository, never()).findArtistNamesBySubstring(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should deduplicate results in autocomplete")
    void shouldDeduplicateResultsInAutocomplete() {
        // Given
        String query = "Fire";
        int limit = 10;

        when(repository.findAttackNamesByPrefix(query, limit))
                .thenReturn(List.of("Fire Blast", "Fire Spin"));
        when(repository.findAttackNamesBySubstring(query, 8))
                .thenReturn(List.of("Fire Blast", "Flamethrower", "Wildfire")); // "Fire Blast" is duplicate

        // When
        List<String> results = service.autocompleteAttacks(query, limit);

        // Then
        assertThat(results).containsExactly("Fire Blast", "Fire Spin", "Flamethrower", "Wildfire");
        assertThat(results).hasSize(4); // Deduplicated
    }

    // ============================================================================
    // autocompleteAttacks Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete attack names")
    void shouldAutocompleteAttackNames() {
        // Given
        String query = "Thund";
        int limit = 3;

        when(repository.findAttackNamesByPrefix(query, limit))
                .thenReturn(List.of("Thunder", "Thunderbolt", "Thunder Punch"));

        // When
        List<String> results = service.autocompleteAttacks(query, limit);

        // Then
        assertThat(results).containsExactly("Thunder", "Thunderbolt", "Thunder Punch");
        verify(repository).findAttackNamesByPrefix(query, limit);
    }

    // ============================================================================
    // autocompleteAbilities Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete ability names")
    void shouldAutocompleteAbilityNames() {
        // Given
        String query = "Static";
        int limit = 5;

        when(repository.findAbilityNamesByPrefix(query, limit))
                .thenReturn(List.of("Static"));

        // When
        List<String> results = service.autocompleteAbilities(query, limit);

        // Then
        assertThat(results).containsExactly("Static");
        verify(repository).findAbilityNamesByPrefix(query, limit);
    }

    // ============================================================================
    // autocompleteCardNames Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete card names with IDs")
    void shouldAutocompleteCardNamesWithIds() {
        // Given
        String query = "Char";
        int limit = 5;

        when(repository.findCardNamesByPrefix(query, limit))
                .thenReturn(List.of("Charizard (base1-4)", "Charmander (base1-46)", "Charmeleon (base1-24)"));

        // When
        List<String> results = service.autocompleteCardNames(query, limit);

        // Then
        assertThat(results).containsExactly("Charizard (base1-4)", "Charmander (base1-46)", "Charmeleon (base1-24)");
        verify(repository).findCardNamesByPrefix(query, limit);
    }

    // ============================================================================
    // autocompleteSetNames Tests
    // ============================================================================

    @Test
    @DisplayName("Should autocomplete set names")
    void shouldAutocompleteSetNames() {
        // Given
        String query = "Base";
        int limit = 3;

        when(repository.findSetNamesByPrefix(query, limit))
                .thenReturn(List.of("Base Set", "Base Set 2"));

        // When
        List<String> results = service.autocompleteSetNames(query, limit);

        // Then
        assertThat(results).containsExactly("Base Set", "Base Set 2");
        verify(repository).findSetNamesByPrefix(query, limit);
    }

    // ============================================================================
    // Edge Cases
    // ============================================================================

    @Test
    @DisplayName("Should trim whitespace from query in autocomplete")
    void shouldTrimWhitespaceFromQueryInAutocomplete() {
        // Given
        String query = "  Ken  ";
        int limit = 5;

        when(repository.findArtistNamesByPrefix("Ken", limit))
                .thenReturn(List.of("Ken Sugimori"));

        // When
        List<String> results = service.autocompleteArtists(query, limit);

        // Then
        assertThat(results).containsExactly("Ken Sugimori");
        verify(repository).findArtistNamesByPrefix("Ken", limit);
    }

    @Test
    @DisplayName("Should respect limit in autocomplete results")
    void shouldRespectLimitInAutocompleteResults() {
        // Given
        String query = "a";
        int limit = 3;

        when(repository.findArtistNamesByPrefix(query, limit))
                .thenReturn(List.of("Artist A", "Artist B"));
        when(repository.findArtistNamesBySubstring(query, 1))
                .thenReturn(List.of("John A", "Mike A", "Extra")); // More than needed

        // When
        List<String> results = service.autocompleteArtists(query, limit);

        // Then
        assertThat(results).hasSize(3); // Should not exceed limit
    }
}
