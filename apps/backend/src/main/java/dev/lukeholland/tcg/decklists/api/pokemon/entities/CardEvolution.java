package dev.lukeholland.tcg.decklists.api.pokemon.entities;

import dev.lukeholland.tcg.decklists.api.pokemon.enums.EvolutionDirection;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_card_evolution")
@IdClass(CardEvolution.PokemonCardEvolutionId.class)
public class CardEvolution {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_id", nullable = false)
    private Name name;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvolutionDirection direction;

    public CardEvolution() {
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public EvolutionDirection getDirection() {
        return direction;
    }

    public void setDirection(EvolutionDirection direction) {
        this.direction = direction;
    }

    public static class PokemonCardEvolutionId implements Serializable {
        private String card;
        private Integer name;
        private EvolutionDirection direction;

        public PokemonCardEvolutionId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PokemonCardEvolutionId that = (PokemonCardEvolutionId) o;
            return Objects.equals(card, that.card) &&
                    Objects.equals(name, that.name) &&
                    direction == that.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(card, name, direction);
        }
    }
}