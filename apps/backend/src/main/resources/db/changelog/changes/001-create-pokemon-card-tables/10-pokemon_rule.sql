-- Rules
CREATE TABLE pokemon_rule
(
    id   SERIAL PRIMARY KEY,
    text TEXT NOT NULL
);

-- Linking table
CREATE TABLE pokemon_card_rule
(
    card_id TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    rule_id INT REFERENCES pokemon_rule (id) ON DELETE CASCADE,
    PRIMARY KEY (card_id, rule_id)
);
