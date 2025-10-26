package dev.lukeholland.tcg.decklists.api.pokemon.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_card_weakness")
@IdClass(CardWeakness.PokemonCardWeaknessId.class)
public class CardWeakness {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weakness_id")
    private Weakness weakness;

    public CardWeakness() {
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Weakness getWeakness() {
        return weakness;
    }

    public void setWeakness(Weakness weakness) {
        this.weakness = weakness;
    }

    public static class PokemonCardWeaknessId implements Serializable {
        private String card;
        private Integer weakness;

        public PokemonCardWeaknessId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PokemonCardWeaknessId that = (PokemonCardWeaknessId) o;
            return Objects.equals(card, that.card) && Objects.equals(weakness, that.weakness);
        }

        @Override
        public int hashCode() {
            return Objects.hash(card, weakness);
        }
    }
}