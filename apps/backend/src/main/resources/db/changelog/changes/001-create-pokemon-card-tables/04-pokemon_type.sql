-- Types
CREATE TABLE pokemon_type
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

-- Linking table: card ↔ type
CREATE TABLE pokemon_card_type
(
    card_id TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    type_id INT REFERENCES pokemon_type (id) ON DELETE CASCADE,
    PRIMARY KEY (card_id, type_id)
);
