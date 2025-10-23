package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_attack_cost")
@IdClass(PokemonAttackCost.PokemonAttackCostId.class)
public class PokemonAttackCost {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attack_id")
    private PokemonAttack attack;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private PokemonType type;

    public PokemonAttackCost() {}

    public PokemonAttack getAttack() { return attack; }
    public void setAttack(PokemonAttack attack) { this.attack = attack; }
    public PokemonType getType() { return type; }
    public void setType(PokemonType type) { this.type = type; }

    public static class PokemonAttackCostId implements Serializable {
        private Integer attack;
        private Integer type;

        public PokemonAttackCostId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PokemonAttackCostId that = (PokemonAttackCostId) o;
            return Objects.equals(attack, that.attack) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attack, type);
        }
    }
}