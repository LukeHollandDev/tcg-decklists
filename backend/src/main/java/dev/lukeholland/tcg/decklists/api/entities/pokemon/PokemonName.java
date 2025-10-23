package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon_name")
public class PokemonName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    public PokemonName() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}