package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_card_evolution")
@IdClass(PokemonCardEvolution.PokemonCardEvolutionId.class)
public class PokemonCardEvolution {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private PokemonCard card;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_id", nullable = false)
    private PokemonName name;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvolutionDirection direction;

    public PokemonCardEvolution() {
    }

    public PokemonCard getCard() {
        return card;
    }

    public void setCard(PokemonCard card) {
        this.card = card;
    }

    public PokemonName getName() {
        return name;
    }

    public void setName(PokemonName name) {
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