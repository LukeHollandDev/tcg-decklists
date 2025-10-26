-- Attacks
CREATE TABLE pokemon_attack
(
    id              SERIAL PRIMARY KEY,
    name            TEXT NOT NULL,
    converted_cost  INT  NOT NULL,
    damage          TEXT,
    damage_numeric  INT,
    damage_modifier TEXT,
    text            TEXT
);

-- Attack ↔ Type cost
-- Note: Uses surrogate key to allow duplicate (attack_id, type_id) pairs (e.g., 2x Colorless)
CREATE TABLE pokemon_attack_cost
(
    id        SERIAL PRIMARY KEY,
    attack_id INT REFERENCES pokemon_attack (id) ON DELETE CASCADE,
    type_id   INT REFERENCES pokemon_type (id) ON DELETE CASCADE
);

-- Card ↔ Attack
CREATE TABLE pokemon_card_attack
(
    card_id   TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    attack_id INT REFERENCES pokemon_attack (id) ON DELETE CASCADE,
    PRIMARY KEY (card_id, attack_id)
);
