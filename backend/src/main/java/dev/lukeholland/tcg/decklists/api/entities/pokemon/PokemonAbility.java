package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "pokemon_ability")
public class PokemonAbility {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pokemon_ability_id_gen")
    @SequenceGenerator(name = "pokemon_ability_id_gen", sequenceName = "pokemon_ability_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "card_id", nullable = false)
    private dev.lukeholland.tcg.decklists.api.entities.pokemon.PokemonCard card;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "text", nullable = false, length = Integer.MAX_VALUE)
    private String text;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "position", nullable = false)
    private Integer position;

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

}