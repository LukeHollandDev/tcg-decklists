package dev.lukeholland.tcg.decklists.api.entities.pokemon;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pokemon_card_legality")
@IdClass(PokemonCardLegality.PokemonCardLegalityId.class)
public class PokemonCardLegality {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private PokemonCard card;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "format_id", nullable = false)
    private PokemonFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LegalityStatus status;

    public PokemonCardLegality() {}

    public PokemonCard getCard() { return card; }
    public void setCard(PokemonCard card) { this.card = card; }
    public PokemonFormat getFormat() { return format; }
    public void setFormat(PokemonFormat format) { this.format = format; }
    public LegalityStatus getStatus() { return status; }
    public void setStatus(LegalityStatus status) { this.status = status; }

    public static class PokemonCardLegalityId implements Serializable {
        private String card;
        private Integer format;

        public PokemonCardLegalityId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PokemonCardLegalityId that = (PokemonCardLegalityId) o;
            return Objects.equals(card, that.card) && Objects.equals(format, that.format);
        }

        @Override
        public int hashCode() {
            return Objects.hash(card, format);
        }
    }
}