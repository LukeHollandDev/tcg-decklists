-- Sets
CREATE TABLE pokemon_set
(
    id     SERIAL PRIMARY KEY,
    set_id TEXT NOT NULL UNIQUE,
    name   TEXT
);

-- Artists
CREATE TABLE pokemon_artist
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

-- Rarities
CREATE TABLE pokemon_rarity
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

-- Ancient Traits
CREATE TABLE pokemon_ancient_trait
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    text TEXT NOT NULL
);

-- Main Pokémon card table
CREATE TABLE pokemon_card
(
    id                     TEXT PRIMARY KEY,
    name                   TEXT NOT NULL,
    supertype              TEXT NOT NULL,
    hp                     TEXT,
    hp_numeric             INT,
    converted_retreat_cost INT,
    number                 TEXT NOT NULL,
    set_id                 INT  REFERENCES pokemon_set (id) ON DELETE SET NULL,
    artist_id              INT  REFERENCES pokemon_artist (id) ON DELETE SET NULL,
    rarity_id              INT  REFERENCES pokemon_rarity (id) ON DELETE SET NULL,
    flavor_text            TEXT,
    image_low              TEXT,
    image_high             TEXT,
    regulation_mark        TEXT,
    level                  TEXT,
    ancient_trait_id       INT  REFERENCES pokemon_ancient_trait (id) ON DELETE SET NULL
);
