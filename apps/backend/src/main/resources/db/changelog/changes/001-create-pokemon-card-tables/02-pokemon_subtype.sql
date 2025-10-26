-- Subtypes
CREATE TABLE pokemon_subtype
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

-- Linking table
CREATE TABLE pokemon_card_subtype
(
    card_id    TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    subtype_id INT REFERENCES pokemon_subtype (id) ON DELETE CASCADE,
    PRIMARY KEY (card_id, subtype_id)
);
