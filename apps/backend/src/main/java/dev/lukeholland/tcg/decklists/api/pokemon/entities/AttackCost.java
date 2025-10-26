package dev.lukeholland.tcg.decklists.api.pokemon.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_attack_cost")
@IdClass(AttackCost.PokemonAttackCostId.class)
public class AttackCost {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attack_id")
    private Attack attack;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private Type type;

    @Column(nullable = false)
    private Integer quantity = 1;

    public AttackCost() {
    }

    public Attack getAttack() {
        return attack;
    }

    public void setAttack(Attack attack) {
        this.attack = attack;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public static class PokemonAttackCostId implements Serializable {
        private Integer attack;
        private Integer type;

        public PokemonAttackCostId() {
        }

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