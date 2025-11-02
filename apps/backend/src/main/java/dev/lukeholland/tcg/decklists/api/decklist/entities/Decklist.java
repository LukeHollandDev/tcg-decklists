package dev.lukeholland.tcg.decklists.api.decklist.entities;

import dev.lukeholland.tcg.decklists.api.enums.CardGame;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "decklist")
public class Decklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardGame type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "decklist", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DecklistCard> cards = new HashSet<>();

    public Decklist() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CardGame getType() {
        return type;
    }

    public void setType(CardGame type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<DecklistCard> getCards() {
        return cards;
    }

    public void setCards(Set<DecklistCard> cards) {
        this.cards = cards;
    }

    public void addCard(DecklistCard card) {
        cards.add(card);
        card.setDecklist(this);
    }

    public void removeCard(DecklistCard card) {
        cards.remove(card);
        card.setDecklist(null);
    }
}
