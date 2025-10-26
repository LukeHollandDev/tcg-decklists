package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pokemon_attack")
public class PokemonAttack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "converted_cost", nullable = false)
    private Integer convertedCost;

    @Column
    private String damage;

    @Column(name = "damage_numeric")
    private Integer damageNumeric;

    @Column(name = "damage_modifier")
    private String damageModifier;

    @Column
    private String text;

    @OneToMany(mappedBy = "attack", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PokemonAttackCost> costs = new HashSet<>();

    public PokemonAttack() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getConvertedCost() { return convertedCost; }
    public void setConvertedCost(Integer convertedCost) { this.convertedCost = convertedCost; }
    public String getDamage() { return damage; }
    public void setDamage(String damage) { this.damage = damage; }
    public Integer getDamageNumeric() { return damageNumeric; }
    public void setDamageNumeric(Integer damageNumeric) { this.damageNumeric = damageNumeric; }
    public String getDamageModifier() { return damageModifier; }
    public void setDamageModifier(String damageModifier) { this.damageModifier = damageModifier; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Set<PokemonAttackCost> getCosts() { return costs; }
    public void setCosts(Set<PokemonAttackCost> costs) { this.costs = costs; }
}