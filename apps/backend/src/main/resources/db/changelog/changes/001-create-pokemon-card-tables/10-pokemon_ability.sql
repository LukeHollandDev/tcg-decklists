-- Abilities
CREATE TABLE pokemon_ability
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    text TEXT NOT NULL,
    type TEXT
);

-- Linking table
CREATE TABLE pokemon_card_ability
(
    card_id    TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    ability_id INT REFERENCES pokemon_ability (id) ON DELETE CASCADE,
    PRIMARY KEY (card_id, ability_id)
);
