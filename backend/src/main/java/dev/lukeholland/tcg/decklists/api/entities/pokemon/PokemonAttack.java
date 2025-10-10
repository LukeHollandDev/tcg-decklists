package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "pokemon_attack")
public class PokemonAttack {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pokemon_attack_id_gen")
    @SequenceGenerator(name = "pokemon_attack_id_gen", sequenceName = "pokemon_attack_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "card_id", nullable = false)
    private dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard card;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "converted_energy_cost", nullable = false)
    private Integer convertedEnergyCost;

    @Column(name = "damage", length = 20)
    private String damage;

    @Column(name = "text", nullable = false, length = Integer.MAX_VALUE)
    private String text;

    @Column(name = "position", nullable = false)
    private Integer position;

    @OneToMany(mappedBy = "attack")
    private Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonAttackCost> pokemonAttackCosts = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard getCard() {
        return card;
    }

    public void setCard(dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard card) {
        this.card = card;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getConvertedEnergyCost() {
        return convertedEnergyCost;
    }

    public void setConvertedEnergyCost(Integer convertedEnergyCost) {
        this.convertedEnergyCost = convertedEnergyCost;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonAttackCost> getPokemonAttackCosts() {
        return pokemonAttackCosts;
    }

    public void setPokemonAttackCosts(Set<dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonAttackCost> pokemonAttackCosts) {
        this.pokemonAttackCosts = pokemonAttackCosts;
    }

}