-- Formats for legality
CREATE TABLE pokemon_format
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

-- Legality linking table
CREATE TYPE legality_status AS ENUM ('legal', 'illegal', 'banned', 'unlimited');

CREATE TABLE pokemon_card_legality
(
    card_id   TEXT REFERENCES pokemon_card (id) ON DELETE CASCADE,
    format_id INT REFERENCES pokemon_format (id) ON DELETE CASCADE,
    status    legality_status NOT NULL,
    PRIMARY KEY (card_id, format_id)
);
