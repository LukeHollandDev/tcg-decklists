package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_card_resistance")
@IdClass(PokemonCardResistance.PokemonCardResistanceId.class)
public class PokemonCardResistance {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private PokemonCard card;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resistance_id")
    private PokemonResistance resistance;

    public PokemonCardResistance() {}

    public PokemonCard getCard() { return card; }
    public void setCard(PokemonCard card) { this.card = card; }
    public PokemonResistance getResistance() { return resistance; }
    public void setResistance(PokemonResistance resistance) { this.resistance = resistance; }

    public static class PokemonCardResistanceId implements Serializable {
        private String card;
        private Integer resistance;

        public PokemonCardResistanceId() {}

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