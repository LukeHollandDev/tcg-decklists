-- Pokemon names
CREATE TABLE pokemon_name
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

-- Evolutions (merged from evolves_from / evolves_to)
CREATE TYPE evolution_direction AS ENUM ('from', 'to');

CREATE TABLE pokemon_card_evolution
(
    card_id   TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    name_id   INT REFERENCES pokemon_name (id) ON DELETE CASCADE,
    direction evolution_direction NOT NULL,
    PRIMARY KEY (card_id, name_id, direction)
);
