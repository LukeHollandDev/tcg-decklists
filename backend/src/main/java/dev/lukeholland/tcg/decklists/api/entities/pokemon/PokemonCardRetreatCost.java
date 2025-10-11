package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "pokemon_card_retreat_cost")
public class PokemonCardRetreatCost {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pokemon_card_retreat_cost_id_gen")
    @SequenceGenerator(name = "pokemon_card_retreat_cost_id_gen", sequenceName = "pokemon_card_retreat_cost_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "card_id", nullable = false)
    @JsonBackReference
    private PokemonCard card;

    @Column(name = "energy_type", nullable = false, length = 50)
    private String energyType;

    @Column(name = "position", nullable = false)
    private Integer position;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PokemonCard getCard() {
        return card;
    }

    public void setCard(PokemonCard card) {
        this.card = card;
    }

    public String getEnergyType() {
        return energyType;
    }

    public void setEnergyType(String energyType) {
        this.energyType = energyType;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

}