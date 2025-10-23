package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon_weakness")
public class PokemonWeakness {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private PokemonType type;

    @Column(nullable = false)
    private String value;

    public PokemonWeakness() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public PokemonType getType() { return type; }
    public void setType(PokemonType type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}