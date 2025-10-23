package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_card_retreat_cost")
@IdClass(PokemonCardRetreatCost.PokemonCardRetreatCostId.class)
public class PokemonCardRetreatCost {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private PokemonCard card;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private PokemonType type;

    public PokemonCardRetreatCost() {}

    public PokemonCard getCard() { return card; }
    public void setCard(PokemonCard card) { this.card = card; }
    public PokemonType getType() { return type; }
    public void setType(PokemonType type) { this.type = type; }

    public static class PokemonCardRetreatCostId implements Serializable {
        private String card;
        private Integer type;

        public PokemonCardRetreatCostId() {}

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