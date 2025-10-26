-- Retreat cost table - cost allows duplicates by having its own ID key
CREATE TABLE pokemon_card_retreat_cost
(
    id      SERIAL PRIMARY KEY,
    card_id TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    type_id INT REFERENCES pokemon_type (id) ON DELETE CASCADE
);
