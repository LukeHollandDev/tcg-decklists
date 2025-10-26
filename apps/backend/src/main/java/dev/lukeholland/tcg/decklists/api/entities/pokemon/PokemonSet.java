package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon_set")
public class PokemonSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "set_id", nullable = false, unique = true)
    private String setId;

    @Column(name = "name")
    private String name;

    public PokemonSet() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}