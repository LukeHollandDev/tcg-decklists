-- Decklist table - stores user-created decklists
CREATE TABLE decklist
(
    id         SERIAL PRIMARY KEY,
    name       TEXT      NOT NULL,
    type       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Decklist card junction table - stores cards in each decklist with quantities
CREATE TABLE decklist_card
(
    id          SERIAL PRIMARY KEY,
    decklist_id INT  NOT NULL REFERENCES decklist (id) ON DELETE CASCADE,
    card_id     TEXT NOT NULL,
    quantity    INT  NOT NULL DEFAULT 1,
    CONSTRAINT decklist_card_unique UNIQUE (decklist_id, card_id)
);

-- Index for faster lookups by decklist_id
CREATE INDEX idx_decklist_card_decklist_id ON decklist_card (decklist_id);

-- Index for faster lookups by card_id (useful for finding which decklists contain a card)
CREATE INDEX idx_decklist_card_card_id ON decklist_card (card_id);
