-- Resistances
CREATE TABLE pokemon_resistance
(
    id      SERIAL PRIMARY KEY,
    type_id INT REFERENCES pokemon_type (id) ON DELETE CASCADE,
    value   TEXT NOT NULL
);

-- Linking table
CREATE TABLE pokemon_card_resistance
(
    card_id       TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    resistance_id INT REFERENCES pokemon_resistance (id) ON DELETE CASCADE
);
