--liquibase formatted sql

--changeset lukeholland:002-add-autocomplete-indexes-01
--comment: Add B-tree indexes on name columns used by autocomplete queries to improve search performance

-- Index for Artist name autocomplete
-- Supports LOWER(name) queries used in findArtistNamesByPrefix and findArtistNamesBySubstring
CREATE INDEX IF NOT EXISTS idx_artist_name_lower ON pokemon_artist (LOWER(name));

-- Index for Attack name autocomplete
-- Supports LOWER(name) queries used in findAttackNamesByPrefix and findAttackNamesBySubstring
CREATE INDEX IF NOT EXISTS idx_attack_name_lower ON pokemon_attack (LOWER(name));

-- Index for Ability name autocomplete
-- Supports LOWER(name) queries used in findAbilityNamesByPrefix and findAbilityNamesBySubstring
CREATE INDEX IF NOT EXISTS idx_ability_name_lower ON pokemon_ability (LOWER(name));

-- Index for Card name autocomplete
-- Supports LOWER(name) queries used in findCardNamesByPrefix and findCardNamesBySubstring
CREATE INDEX IF NOT EXISTS idx_card_name_lower ON pokemon_card (LOWER(name));

-- Index for Set name autocomplete
-- Supports LOWER(name) queries used in findSetNamesByPrefix and findSetNamesBySubstring
CREATE INDEX IF NOT EXISTS idx_set_name_lower ON pokemon_set (LOWER(name));

--rollback DROP INDEX IF EXISTS idx_artist_name_lower;
--rollback DROP INDEX IF EXISTS idx_attack_name_lower;
--rollback DROP INDEX IF EXISTS idx_ability_name_lower;
--rollback DROP INDEX IF EXISTS idx_card_name_lower;
--rollback DROP INDEX IF EXISTS idx_set_name_lower;
