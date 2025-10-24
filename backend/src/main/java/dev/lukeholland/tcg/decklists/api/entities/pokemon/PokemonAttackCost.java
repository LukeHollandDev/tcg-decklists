package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_attack_cost")
public class PokemonAttackCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attack_id")
    private PokemonAttack attack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private PokemonType type;

    public PokemonAttackCost() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public PokemonAttack getAttack() { return attack; }
    public void setAttack(PokemonAttack attack) { this.attack = attack; }
    public PokemonType getType() { return type; }
    public void setType(PokemonType type) { this.type = type; }
}