package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_card_weakness")
@IdClass(PokemonCardWeakness.PokemonCardWeaknessId.class)
public class PokemonCardWeakness {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private PokemonCard card;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weakness_id")
    private PokemonWeakness weakness;

    public PokemonCardWeakness() {
    }

    public PokemonCard getCard() {
        return card;
    }

    public void setCard(PokemonCard card) {
        this.card = card;
    }

    public PokemonWeakness getWeakness() {
        return weakness;
    }

    public void setWeakness(PokemonWeakness weakness) {
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