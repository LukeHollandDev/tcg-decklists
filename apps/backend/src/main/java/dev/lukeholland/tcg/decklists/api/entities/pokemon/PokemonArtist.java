package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon_artist")
public class PokemonArtist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    public PokemonArtist() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}