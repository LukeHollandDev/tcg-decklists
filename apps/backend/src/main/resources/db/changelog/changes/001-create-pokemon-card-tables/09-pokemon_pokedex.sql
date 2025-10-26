-- Pokedex entries
CREATE TABLE pokemon_pokedex
(
    id     SERIAL PRIMARY KEY,
    number INT NOT NULL
);

-- Linking table: card ↔ pokedex
CREATE TABLE pokemon_card_pokedex
(
    card_id    TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    pokedex_id INT REFERENCES pokemon_pokedex (id) ON DELETE CASCADE,
    PRIMARY KEY (card_id, pokedex_id)
);
