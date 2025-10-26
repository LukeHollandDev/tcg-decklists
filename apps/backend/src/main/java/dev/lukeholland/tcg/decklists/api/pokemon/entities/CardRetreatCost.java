package dev.lukeholland.tcg.decklists.api.pokemon.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_card_retreat_cost")
@IdClass(CardRetreatCost.PokemonCardRetreatCostId.class)
public class CardRetreatCost {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private Type type;

    public CardRetreatCost() {
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static class PokemonCardRetreatCostId implements Serializable {
        private String card;
        private Integer type;

        public PokemonCardRetreatCostId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PokemonCardRetreatCostId that = (PokemonCardRetreatCostId) o;
            return Objects.equals(card, that.card) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(card, type);
        }
    }
}