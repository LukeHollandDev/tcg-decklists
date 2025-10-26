-- Weaknesses
CREATE TABLE pokemon_weakness
(
    id      SERIAL PRIMARY KEY,
    type_id INT REFERENCES pokemon_type (id) ON DELETE CASCADE,
    value   TEXT NOT NULL
);

-- Linking table
CREATE TABLE pokemon_card_weakness
(
    card_id     TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    weakness_id INT REFERENCES pokemon_weakness (id) ON DELETE CASCADE
);
