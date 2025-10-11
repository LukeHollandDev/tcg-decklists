package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "pokemon_legality")
public class PokemonLegality {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pokemon_legality_id_gen")
    @SequenceGenerator(name = "pokemon_legality_id_gen", sequenceName = "pokemon_legality_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "card_id", nullable = false)
    @JsonBackReference
    private PokemonCard card;

    @Column(name = "format", nullable = false, length = 50)
    private String format;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}