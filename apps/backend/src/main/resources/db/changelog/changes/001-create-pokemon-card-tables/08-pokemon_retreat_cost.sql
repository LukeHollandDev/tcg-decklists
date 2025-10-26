-- Retreat cost table - stores quantity for each type per card
CREATE TABLE pokemon_card_retreat_cost
(
    card_id  TEXT NOT NULL REFERENCES pokemon_card (id) ON DELETE CASCADE,
    type_id  INT NOT NULL REFERENCES pokemon_type (id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (card_id, type_id)
);
