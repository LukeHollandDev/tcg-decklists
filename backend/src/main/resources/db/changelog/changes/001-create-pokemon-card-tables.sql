-- Main card table
CREATE TABLE pokemon_card
(
    id                     VARCHAR(255) PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL,
    supertype              VARCHAR(50)  NOT NULL,
    hp                     VARCHAR(10),
    number                 VARCHAR(50)  NOT NULL,
    artist                 VARCHAR(255),
    rarity                 VARCHAR(50),
    flavor_text            TEXT,
    evolves_from           VARCHAR(255),
    level                  VARCHAR(10),
    regulation_mark        VARCHAR(10),
    converted_retreat_cost INTEGER,
    image_small            VARCHAR(500),
    image_large            VARCHAR(500),
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Card types (Fire, Water, Psychic, etc.)
CREATE TABLE pokemon_card_type
(
    id       BIGSERIAL PRIMARY KEY,
    card_id  VARCHAR(255) NOT NULL,
    type     VARCHAR(50)  NOT NULL,
    position INTEGER      NOT NULL,
    CONSTRAINT fk_card_type_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Card subtypes (Basic, Stage 1, EX, V, VMAX, etc.)
CREATE TABLE pokemon_card_subtype
(
    id       BIGSERIAL PRIMARY KEY,
    card_id  VARCHAR(255) NOT NULL,
    subtype  VARCHAR(50)  NOT NULL,
    position INTEGER      NOT NULL,
    CONSTRAINT fk_card_subtype_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Evolution targets
CREATE TABLE pokemon_card_evolves_to
(
    id              BIGSERIAL PRIMARY KEY,
    card_id         VARCHAR(255) NOT NULL,
    evolves_to_name VARCHAR(255) NOT NULL,
    position        INTEGER      NOT NULL,
    CONSTRAINT fk_card_evolves_to_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- National Pokedex numbers
CREATE TABLE pokemon_card_national_pokedex_number
(
    id             BIGSERIAL PRIMARY KEY,
    card_id        VARCHAR(255) NOT NULL,
    pokedex_number INTEGER      NOT NULL,
    position       INTEGER      NOT NULL,
    CONSTRAINT fk_card_pokedex_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Retreat costs
CREATE TABLE pokemon_card_retreat_cost
(
    id          BIGSERIAL PRIMARY KEY,
    card_id     VARCHAR(255) NOT NULL,
    energy_type VARCHAR(50)  NOT NULL,
    position    INTEGER      NOT NULL,
    CONSTRAINT fk_card_retreat_cost_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Attacks
CREATE TABLE pokemon_attack
(
    id                    BIGSERIAL PRIMARY KEY,
    card_id               VARCHAR(255) NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    converted_energy_cost INTEGER      NOT NULL,
    damage                VARCHAR(20),
    text                  TEXT         NOT NULL,
    position              INTEGER      NOT NULL,
    CONSTRAINT fk_attack_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Attack energy costs
CREATE TABLE pokemon_attack_cost
(
    id          BIGSERIAL PRIMARY KEY,
    attack_id   BIGINT      NOT NULL,
    energy_type VARCHAR(50) NOT NULL,
    position    INTEGER     NOT NULL,
    CONSTRAINT fk_attack_cost_attack FOREIGN KEY (attack_id) REFERENCES pokemon_attack (id) ON DELETE CASCADE
);

-- Abilities (Abilities, Poké-Powers, Poké-Bodies, etc.)
CREATE TABLE pokemon_ability
(
    id       BIGSERIAL PRIMARY KEY,
    card_id  VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    text     TEXT         NOT NULL,
    type     VARCHAR(50)  NOT NULL,
    position INTEGER      NOT NULL,
    CONSTRAINT fk_ability_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Weaknesses
CREATE TABLE pokemon_weakness
(
    id      BIGSERIAL PRIMARY KEY,
    card_id VARCHAR(255) NOT NULL,
    type    VARCHAR(50)  NOT NULL,
    value   VARCHAR(10)  NOT NULL,
    CONSTRAINT fk_weakness_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Resistances
CREATE TABLE pokemon_resistance
(
    id      BIGSERIAL PRIMARY KEY,
    card_id VARCHAR(255) NOT NULL,
    type    VARCHAR(50)  NOT NULL,
    value   VARCHAR(10)  NOT NULL,
    CONSTRAINT fk_resistance_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Legalities (unlimited, expanded, standard)
CREATE TABLE pokemon_legality
(
    id      BIGSERIAL PRIMARY KEY,
    card_id VARCHAR(255) NOT NULL,
    format  VARCHAR(50)  NOT NULL,
    status  VARCHAR(50)  NOT NULL,
    CONSTRAINT fk_legality_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE,
    CONSTRAINT unique_card_format UNIQUE (card_id, format)
);

-- Rules text (for special cards like EX, GX, V, etc.)
CREATE TABLE pokemon_rule
(
    id        BIGSERIAL PRIMARY KEY,
    card_id   VARCHAR(255) NOT NULL,
    rule_text TEXT         NOT NULL,
    position  INTEGER      NOT NULL,
    CONSTRAINT fk_rule_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Ancient Traits (older mechanic)
CREATE TABLE pokemon_ancient_trait
(
    id      BIGSERIAL PRIMARY KEY,
    card_id VARCHAR(255) NOT NULL,
    name    VARCHAR(255) NOT NULL,
    text    TEXT         NOT NULL,
    CONSTRAINT fk_ancient_trait_card FOREIGN KEY (card_id) REFERENCES pokemon_card (id) ON DELETE CASCADE
);

-- Indexes for common queries
CREATE INDEX idx_pokemon_card_supertype ON pokemon_card (supertype);
CREATE INDEX idx_pokemon_card_rarity ON pokemon_card (rarity);
CREATE INDEX idx_pokemon_card_name ON pokemon_card (name);
CREATE INDEX idx_pokemon_card_evolves_from ON pokemon_card (evolves_from);
CREATE INDEX idx_pokemon_card_regulation_mark ON pokemon_card (regulation_mark);

CREATE INDEX idx_pokemon_card_type_card_id ON pokemon_card_type (card_id);
CREATE INDEX idx_pokemon_card_type_type ON pokemon_card_type (type);

CREATE INDEX idx_pokemon_card_subtype_card_id ON pokemon_card_subtype (card_id);
CREATE INDEX idx_pokemon_card_subtype_subtype ON pokemon_card_subtype (subtype);

CREATE INDEX idx_pokemon_evolves_to_card_id ON pokemon_card_evolves_to (card_id);
CREATE INDEX idx_pokemon_evolves_to_name ON pokemon_card_evolves_to (evolves_to_name);

CREATE INDEX idx_pokemon_pokedex_card_id ON pokemon_card_national_pokedex_number (card_id);
CREATE INDEX idx_pokemon_pokedex_number ON pokemon_card_national_pokedex_number (pokedex_number);

CREATE INDEX idx_pokemon_retreat_cost_card_id ON pokemon_card_retreat_cost (card_id);

CREATE INDEX idx_pokemon_attack_card_id ON pokemon_attack (card_id);
CREATE INDEX idx_pokemon_attack_name ON pokemon_attack (name);
CREATE INDEX idx_pokemon_attack_energy_cost ON pokemon_attack (converted_energy_cost);

CREATE INDEX idx_pokemon_attack_cost_attack_id ON pokemon_attack_cost (attack_id);

CREATE INDEX idx_pokemon_ability_card_id ON pokemon_ability (card_id);
CREATE INDEX idx_pokemon_ability_type ON pokemon_ability (type);

CREATE INDEX idx_pokemon_weakness_card_id ON pokemon_weakness (card_id);
CREATE INDEX idx_pokemon_weakness_type ON pokemon_weakness (type);

CREATE INDEX idx_pokemon_resistance_card_id ON pokemon_resistance (card_id);
CREATE INDEX idx_pokemon_resistance_type ON pokemon_resistance (type);

CREATE INDEX idx_pokemon_legality_card_id ON pokemon_legality (card_id);
CREATE INDEX idx_pokemon_legality_format_status ON pokemon_legality (format, status);

CREATE INDEX idx_pokemon_rule_card_id ON pokemon_rule (card_id);

CREATE INDEX idx_pokemon_ancient_trait_card_id ON pokemon_ancient_trait (card_id);
