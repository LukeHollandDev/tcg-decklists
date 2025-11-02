package dev.lukeholland.tcg.decklists.api.decklist.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "decklist_card")
public class DecklistCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decklist_id", nullable = false)
    private Decklist decklist;

    @Column(name = "card_id", nullable = false)
    private String cardId;

    @Column(nullable = false)
    private Integer quantity = 1;

    public DecklistCard() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Decklist getDecklist() {
        return decklist;
    }

    public void setDecklist(Decklist decklist) {
        this.decklist = decklist;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
