package dev.lukeholland.tcg.decklists.api.pokemon.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_card_resistance")
@IdClass(CardResistance.PokemonCardResistanceId.class)
public class CardResistance {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resistance_id")
    private Resistance resistance;

    public CardResistance() {
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Resistance getResistance() {
        return resistance;
    }

    public void setResistance(Resistance resistance) {
        this.resistance = resistance;
    }

    public static class PokemonCardResistanceId implements Serializable {
        private String card;
        private Integer resistance;

        public PokemonCardResistanceId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PokemonCardResistanceId that = (PokemonCardResistanceId) o;
            return Objects.equals(card, that.card) && Objects.equals(resistance, that.resistance);
        }

        @Override
        public int hashCode() {
            return Objects.hash(card, resistance);
        }
    }
}