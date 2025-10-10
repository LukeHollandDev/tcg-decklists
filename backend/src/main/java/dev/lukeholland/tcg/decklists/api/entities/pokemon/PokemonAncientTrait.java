package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "pokemon_ancient_trait")
public class PokemonAncientTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pokemon_ancient_trait_id_gen")
    @SequenceGenerator(name = "pokemon_ancient_trait_id_gen", sequenceName = "pokemon_ancient_trait_id_seq", allocationSize = 1)
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

}