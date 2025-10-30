-- ============================================================================
-- TCG Decklists - Comprehensive Test Data
-- ============================================================================
-- This SQL fixture provides test data for integration tests covering:
-- - Evolution chains (Charmander → Charmeleon → Charizard)
-- - Accent characters (Flabébé) for accent-insensitive search testing
-- - Multiset attack costs (2x Fire + 1x Water, etc.)
-- - All types, subtypes, rarities, regulation marks
-- - Format legality variations (Standard, Expanded, banned cards)
-- - Cards with abilities, weaknesses, resistances
-- - Trainer and Energy cards
-- - Various HP ranges for filtering
-- ============================================================================
-- Note: TestContainers provides a fresh database for each test class,
-- so DELETE statements are not needed.

-- ============================================================================
-- Reference Data: Types (All 11 Pokémon types)
-- ============================================================================
INSERT INTO pokemon_type (id, name)
VALUES (1, 'Fire'),
       (2, 'Water'),
       (3, 'Grass'),
       (4, 'Lightning'),
       (5, 'Fighting'),
       (6, 'Psychic'),
       (7, 'Colorless'),
       (8, 'Darkness'),
       (9, 'Metal'),
       (10, 'Dragon'),
       (11, 'Fairy');

-- ============================================================================
-- Reference Data: Sets
-- ============================================================================
INSERT INTO pokemon_set (id, set_id, name, series, printed_total, total, ptcgo_code, release_date)
VALUES (1, 'base1', 'Base Set', 'Base', 102, 102, 'BS', '1999-01-09'),
       (2, 'base2', 'Jungle', 'Base', 64, 64, 'JU', '1999-06-16'),
       (3, 'swsh1', 'Sword & Shield', 'Sword & Shield', 202, 216, 'SSH', '2020-02-07'),
       (4, 'sv1', 'Scarlet & Violet', 'Scarlet & Violet', 198, 258, 'SVI', '2023-03-31');

-- ============================================================================
-- Reference Data: Artists
-- ============================================================================
INSERT INTO pokemon_artist (id, name)
VALUES (1, 'Ken Sugimori'),
       (2, 'Mitsuhiro Arita'),
       (3, 'Kouki Saitou'),
       (4, 'Atsuko Nishida'),
       (5, '5ban Graphics');

-- ============================================================================
-- Reference Data: Rarities
-- ============================================================================
INSERT INTO pokemon_rarity (id, name)
VALUES (1, 'Common'),
       (2, 'Uncommon'),
       (3, 'Rare'),
       (4, 'Rare Holo'),
       (5, 'Rare Holo EX'),
       (6, 'Rare Holo GX'),
       (7, 'Rare Holo V'),
       (8, 'Rare Holo VMAX'),
       (9, 'Rare Ultra'),
       (10, 'Rare Rainbow'),
       (11, 'Rare Secret');

-- ============================================================================
-- Reference Data: Subtypes
-- ============================================================================
INSERT INTO pokemon_subtype (id, name)
VALUES
-- Pokémon Stages
(1, 'Basic'),
(2, 'Stage 1'),
(3, 'Stage 2'),
-- Pokémon Mechanics
(4, 'ex'),
(5, 'EX'),
(6, 'GX'),
(7, 'V'),
(8, 'VMAX'),
(9, 'VSTAR'),
(10, 'MEGA'),
-- Trainer Types
(11, 'Item'),
(12, 'Supporter'),
(13, 'Stadium'),
(14, 'Tool'),
-- Energy Types
(15, 'Special');

-- ============================================================================
-- Reference Data: Formats
-- ============================================================================
INSERT INTO pokemon_format (id, name)
VALUES (1, 'Standard'),
       (2, 'Expanded'),
       (3, 'Unlimited');

-- ============================================================================
-- Reference Data: Pokémon Names (for evolution chains)
-- ============================================================================
INSERT INTO pokemon_name (id, name)
VALUES (1, 'Charmander'),
       (2, 'Charmeleon'),
       (3, 'Charizard'),
       (4, 'Pikachu'),
       (5, 'Raichu'),
       (6, 'Squirtle'),
       (7, 'Wartortle'),
       (8, 'Blastoise'),
       (9, 'Flabébé');

-- ============================================================================
-- Reference Data: Pokédex Numbers
-- ============================================================================
INSERT INTO pokemon_pokedex (id, number)
VALUES (1, 4),  -- Charmander
       (2, 5),  -- Charmeleon
       (3, 6),  -- Charizard
       (4, 25), -- Pikachu
       (5, 26), -- Raichu
       (6, 7),  -- Squirtle
       (7, 8),  -- Wartortle
       (8, 9),  -- Blastoise
       (9, 669);
-- Flabébé

-- ============================================================================
-- Abilities
-- ============================================================================
INSERT INTO pokemon_ability (id, name, text, type)
VALUES (1, 'Blaze',
        'Once during your turn, you may attach a Fire Energy card from your discard pile to 1 of your Fire Pokémon.',
        'Ability'),
       (2, 'Energy Burn',
        'As often as you like during your turn (before your attack), you may turn all Energy attached to Charizard into Fire Energy for the rest of the turn.',
        'Poké-Power'),
       (3, 'Static',
        'If this Pokémon is your Active Pokémon and is damaged by an opponent''s attack, flip a coin. If heads, the Attacking Pokémon is now Paralyzed.',
        'Ability'),
       (4, 'Rain Dance',
        'As often as you like during your turn (before your attack), you may attach a Water Energy card from your hand to 1 of your Water Pokémon.',
        'Poké-Power'),
       (5, 'Flower Veil',
        'Prevent all effects of attacks, including damage, done to this Pokémon by your opponent''s Pokémon that have 100 HP or more.',
        'Ability');

-- ============================================================================
-- Attacks
-- ============================================================================
INSERT INTO pokemon_attack (id, name, converted_cost, damage, damage_numeric, damage_modifier, text)
VALUES
-- Charmander attacks
(1, 'Scratch', 1, '10', 10, NULL, NULL),
(2, 'Ember', 2, '30', 30, NULL, 'Discard 1 Fire Energy card attached to Charmander in order to use this attack.'),
-- Charmeleon attacks
(3, 'Slash', 3, '50', 50, NULL, NULL),
-- Charizard attacks (multiset: 4x Fire)
(4, 'Fire Spin', 4, '100', 100, NULL, 'Discard 2 Energy cards attached to Charizard in order to use this attack.'),
(5, 'Fire Blast', 4, '120', 120, NULL, 'Discard 1 Fire Energy card attached to Charizard.'),
-- Pikachu attacks
(6, 'Thundershock', 1, '10', 10, NULL, 'Flip a coin. If heads, the Defending Pokémon is now Paralyzed.'),
(7, 'Gnaw', 2, '20', 20, NULL, NULL),
-- Raichu attacks (multiset: 2x Lightning + 1x Colorless)
(8, 'Thunder', 3, '90', 90, NULL, 'Flip a coin. If tails, Raichu does 30 damage to itself.'),
-- Squirtle attacks
(9, 'Bubble', 1, '10', 10, NULL, 'Flip a coin. If heads, the Defending Pokémon is now Paralyzed.'),
(10, 'Withdraw', 1, NULL, NULL, NULL,
 'Flip a coin. If heads, prevent all damage done to Squirtle during your opponent''s next turn.'),
-- Wartortle attacks (multiset: 2x Water)
(11, 'Bite', 2, '40', 40, NULL, NULL),
-- Blastoise attacks (multiset: 3x Water)
(12, 'Hydro Pump', 3, '40+', 40, '+',
 'Does 40 damage plus 10 more damage for each Water Energy attached to Blastoise but not used to pay for this attack''s cost.'),
-- Flabébé attacks (testing accent-insensitive)
(13, 'Fairy Wind', 1, '10', 10, NULL, NULL),
(14, 'Petal Dance', 2, '30', 30, NULL, NULL);

-- ============================================================================
-- Attack Costs (multiset support with quantity column)
-- ============================================================================
INSERT INTO pokemon_attack_cost (attack_id, type_id, quantity)
VALUES
-- Charmander
(1, 7, 1),   -- Scratch: 1x Colorless
(2, 1, 1),   -- Ember: 1x Fire
(2, 7, 1),   -- Ember: 1x Colorless
-- Charmeleon
(3, 1, 2),   -- Slash: 2x Fire
(3, 7, 1),   -- Slash: 1x Colorless
-- Charizard (4x Fire - important for multiset testing)
(4, 1, 4),   -- Fire Spin: 4x Fire
(5, 1, 4),   -- Fire Blast: 4x Fire
-- Pikachu
(6, 4, 1),   -- Thundershock: 1x Lightning
(7, 7, 2),   -- Gnaw: 2x Colorless
-- Raichu (2x Lightning + 1x Colorless - multiset testing)
(8, 4, 2),   -- Thunder: 2x Lightning
(8, 7, 1),   -- Thunder: 1x Colorless
-- Squirtle
(9, 2, 1),   -- Bubble: 1x Water
(10, 2, 1),  -- Withdraw: 1x Water
-- Wartortle (2x Water - multiset testing)
(11, 2, 2),  -- Bite: 2x Water
-- Blastoise (3x Water - multiset testing)
(12, 2, 3),  -- Hydro Pump: 3x Water
-- Flabébé
(13, 11, 1), -- Fairy Wind: 1x Fairy
(14, 11, 2);
-- Petal Dance: 2x Fairy

-- ============================================================================
-- Weaknesses
-- ============================================================================
INSERT INTO pokemon_weakness (id, type_id, value)
VALUES (1, 2, '×2'),  -- Water ×2
       (2, 1, '×2'),  -- Fire ×2
       (3, 5, '×2'),  -- Fighting ×2
       (4, 3, '×2'),  -- Grass ×2
       (5, 4, '+20'), -- Lightning +20 (old format)
       (6, 9, '×2');
-- Metal ×2

-- ============================================================================
-- Resistances
-- ============================================================================
INSERT INTO pokemon_resistance (id, type_id, value)
VALUES (1, 5, '-30'), -- Fighting -30
       (2, 6, '-20'), -- Psychic -20
       (3, 3, '-20');
-- Grass -20

-- ============================================================================
-- Rules (for rule box cards)
-- ============================================================================
INSERT INTO pokemon_rule (id, text)
VALUES (1, 'When your Pokémon-ex is Knocked Out, your opponent takes 2 Prize cards.'),
       (2, 'When your Pokémon V is Knocked Out, your opponent takes 2 Prize cards.'),
       (3, 'When your Pokémon VMAX is Knocked Out, your opponent takes 3 Prize cards.');

-- ============================================================================
-- CARDS: Evolution Chain - Charmander line
-- ============================================================================

-- Charmander (Basic)
INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark, image_low, image_high)
VALUES ('base1-46', 'Charmander', 'Pokémon', '50', 50, 1, '46', 1, 2, 1, NULL,
        'https://images.pokemontcg.io/base1/46.png', 'https://images.pokemontcg.io/base1/46_hires.png');

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-46', 1);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-46', 1);
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('base1-46', 1),
       ('base1-46', 2);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('base1-46', 1);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('base1-46', 7, 1);
INSERT INTO pokemon_card_pokedex (card_id, pokedex_id)
VALUES ('base1-46', 1);
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES
-- base1-46 (Charmander) is NOT legal in Standard, so no entry
('base1-46', 2, 'legal'), -- Legal in Expanded
('base1-46', 3, 'legal');
-- Legal in Unlimited

-- Charmeleon (Stage 1)
INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark)
VALUES ('base1-24', 'Charmeleon', 'Pokémon', '80', 80, 1, '24', 1, 2, 2, NULL);

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-24', 1);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-24', 2);
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('base1-24', 3);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('base1-24', 1);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('base1-24', 7, 1);
INSERT INTO pokemon_card_pokedex (card_id, pokedex_id)
VALUES ('base1-24', 2);
INSERT INTO pokemon_card_evolution (card_id, name_id, direction)
VALUES ('base1-24', 1, 'from');
INSERT INTO pokemon_card_evolution (card_id, name_id, direction)
VALUES ('base1-24', 3, 'to');
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES
-- base1-24 (Charmeleon) is NOT legal in Standard
('base1-24', 2, 'legal'), -- Legal in Expanded
('base1-24', 3, 'legal');
-- Legal in Unlimited

-- Charizard (Stage 2) - With ability and 4x Fire attack cost
INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark, flavor_text)
VALUES ('base1-4', 'Charizard', 'Pokémon', '120', 120, 3, '4', 1, 2, 4, NULL,
        'Spits fire that is hot enough to melt boulders. Known to cause forest fires unintentionally.');

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-4', 1);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-4', 3);
INSERT INTO pokemon_card_ability (card_id, ability_id)
VALUES ('base1-4', 2);
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('base1-4', 4),
       ('base1-4', 5);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('base1-4', 1);
INSERT INTO pokemon_card_resistance (card_id, resistance_id)
VALUES ('base1-4', 1);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('base1-4', 7, 3);
INSERT INTO pokemon_card_pokedex (card_id, pokedex_id)
VALUES ('base1-4', 3);
INSERT INTO pokemon_card_evolution (card_id, name_id, direction)
VALUES ('base1-4', 2, 'from');
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES
-- base1-4 (Charizard) is NOT legal in Standard
('base1-4', 2, 'legal'), -- Legal in Expanded
('base1-4', 3, 'legal');
-- Legal in Unlimited

-- ============================================================================
-- CARDS: Pikachu & Raichu (Testing 2x Lightning + 1x Colorless multiset)
-- ============================================================================

-- Pikachu (Basic)
INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark)
VALUES ('base1-58', 'Pikachu', 'Pokémon', '40', 40, 1, '58', 1, 2, 1, NULL);

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-58', 4);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-58', 1);
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('base1-58', 6),
       ('base1-58', 7);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('base1-58', 3);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('base1-58', 7, 1);
INSERT INTO pokemon_card_pokedex (card_id, pokedex_id)
VALUES ('base1-58', 4);
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES
-- base1-58 (Pikachu) is NOT legal in Standard
('base1-58', 2, 'legal'), -- Legal in Expanded
('base1-58', 3, 'legal');
-- Legal in Unlimited

-- Raichu (Stage 1) - With ability and 2x Lightning + 1x Colorless attack
INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark)
VALUES ('base1-14', 'Raichu', 'Pokémon', '90', 90, 1, '14', 1, 2, 4, NULL);

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-14', 4);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-14', 2);
INSERT INTO pokemon_card_ability (card_id, ability_id)
VALUES ('base1-14', 3);
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('base1-14', 8);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('base1-14', 3);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('base1-14', 7, 1);
INSERT INTO pokemon_card_pokedex (card_id, pokedex_id)
VALUES ('base1-14', 5);
INSERT INTO pokemon_card_evolution (card_id, name_id, direction)
VALUES ('base1-14', 4, 'from');
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES
-- base1-14 (Raichu) is NOT legal in Standard
('base1-14', 2, 'legal'), -- Legal in Expanded
('base1-14', 3, 'legal');
-- Legal in Unlimited

-- ============================================================================
-- CARDS: Squirtle Line (Testing 2x Water and 3x Water multiset)
-- ============================================================================

-- Squirtle (Basic)
INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark)
VALUES ('base1-63', 'Squirtle', 'Pokémon', '40', 40, 1, '63', 1, 2, 1, NULL);

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-63', 2);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-63', 1);
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('base1-63', 9),
       ('base1-63', 10);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('base1-63', 4);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('base1-63', 7, 1);
INSERT INTO pokemon_card_pokedex (card_id, pokedex_id)
VALUES ('base1-63', 6);
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES
-- base1-63 (Squirtle) is NOT legal in Standard
('base1-63', 2, 'legal'), -- Legal in Expanded
('base1-63', 3, 'legal');
-- Legal in Unlimited

-- Wartortle (Stage 1) - 2x Water attack
INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark)
VALUES ('base1-42', 'Wartortle', 'Pokémon', '70', 70, 1, '42', 1, 2, 2, NULL);

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-42', 2);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-42', 2);
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('base1-42', 11);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('base1-42', 4);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('base1-42', 7, 1);
INSERT INTO pokemon_card_pokedex (card_id, pokedex_id)
VALUES ('base1-42', 7);
INSERT INTO pokemon_card_evolution (card_id, name_id, direction)
VALUES ('base1-42', 6, 'from');
INSERT INTO pokemon_card_evolution (card_id, name_id, direction)
VALUES ('base1-42', 8, 'to');
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES
-- base1-42 (Wartortle) is NOT legal in Standard
('base1-42', 2, 'legal'), -- Legal in Expanded
('base1-42', 3, 'legal');
-- Legal in Unlimited

-- Blastoise (Stage 2) - 3x Water attack with ability
INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark)
VALUES ('base1-2', 'Blastoise', 'Pokémon', '100', 100, 3, '2', 1, 2, 4, NULL);

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-2', 2);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-2', 3);
INSERT INTO pokemon_card_ability (card_id, ability_id)
VALUES ('base1-2', 4);
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('base1-2', 12);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('base1-2', 4);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('base1-2', 7, 3);
INSERT INTO pokemon_card_pokedex (card_id, pokedex_id)
VALUES ('base1-2', 8);
INSERT INTO pokemon_card_evolution (card_id, name_id, direction)
VALUES ('base1-2', 7, 'from');
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES
-- base1-2 (Blastoise) is NOT legal in Standard
('base1-2', 2, 'legal'), -- Legal in Expanded
('base1-2', 3, 'legal');
-- Legal in Unlimited

-- ============================================================================
-- CARDS: Flabébé (Testing accent-insensitive search)
-- ============================================================================

INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark)
VALUES ('sv1-83', 'Flabébé', 'Pokémon', '50', 50, 1, '83', 4, 5, 1, 'G');

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('sv1-83', 11);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('sv1-83', 1);
INSERT INTO pokemon_card_ability (card_id, ability_id)
VALUES ('sv1-83', 5);
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('sv1-83', 13),
       ('sv1-83', 14);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('sv1-83', 6);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('sv1-83', 7, 1);
INSERT INTO pokemon_card_pokedex (card_id, pokedex_id)
VALUES ('sv1-83', 9);
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES ('sv1-83', 1, 'legal'),
       ('sv1-83', 2, 'legal'),
       ('sv1-83', 3, 'legal');

-- ============================================================================
-- CARDS: Modern ex card (Testing rule box, regulation mark, banned status)
-- ============================================================================

INSERT INTO pokemon_card (id, name, supertype, hp, hp_numeric, converted_retreat_cost, number, set_id, artist_id,
                          rarity_id, regulation_mark)
VALUES ('swsh1-25', 'Charizard ex', 'Pokémon', '280', 280, 2, '25', 3, 5, 9, 'E');

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('swsh1-25', 1);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('swsh1-25', 4); -- ex
INSERT INTO pokemon_card_attack (card_id, attack_id)
VALUES ('swsh1-25', 5);
INSERT INTO pokemon_card_rule (card_id, rule_id)
VALUES ('swsh1-25', 1);
INSERT INTO pokemon_card_weakness (card_id, weakness_id)
VALUES ('swsh1-25', 1);
INSERT INTO pokemon_card_retreat_cost (card_id, type_id, quantity)
VALUES ('swsh1-25', 7, 2);
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES ('swsh1-25', 1, 'legal'),
       ('swsh1-25', 2, 'legal'),
       ('swsh1-25', 3, 'legal');

-- ============================================================================
-- CARDS: Trainer and Energy cards
-- ============================================================================

-- Professor Oak (Trainer - Supporter)
INSERT INTO pokemon_card (id, name, supertype, number, set_id, artist_id, rarity_id, flavor_text)
VALUES ('base1-88', 'Professor Oak', 'Trainer', '88', 1, 1, 2, 'Discard your hand, then draw 7 cards.');

INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-88', 12); -- Supporter
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES ('base1-88', 1, 'banned'),
       ('base1-88', 2, 'banned'),
       ('base1-88', 3, 'legal');

-- Potion (Trainer - Item)
INSERT INTO pokemon_card (id, name, supertype, number, set_id, artist_id, rarity_id, flavor_text)
VALUES ('base1-94', 'Potion', 'Trainer', '94', 1, 1, 1, 'Heal 20 damage from 1 of your Pokémon.');

INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-94', 11); -- Item
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES ('base1-94', 1, 'legal'),
       ('base1-94', 2, 'legal'),
       ('base1-94', 3, 'legal');

-- Fire Energy (Energy - Basic)
INSERT INTO pokemon_card (id, name, supertype, number, set_id, rarity_id)
VALUES ('base1-98', 'Fire Energy', 'Energy', '98', 1, 1);

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-98', 1);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-98', 1); -- Basic
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES ('base1-98', 1, 'legal'),
       ('base1-98', 2, 'legal'),
       ('base1-98', 3, 'legal');

-- Double Colorless Energy (Energy - Special)
INSERT INTO pokemon_card (id, name, supertype, number, set_id, rarity_id, flavor_text)
VALUES ('base1-96', 'Double Colorless Energy', 'Energy', '96', 1, 2, 'Provides 2 Colorless Energy.');

INSERT INTO pokemon_card_type (card_id, type_id)
VALUES ('base1-96', 7);
INSERT INTO pokemon_card_subtype (card_id, subtype_id)
VALUES ('base1-96', 15); -- Special
INSERT INTO pokemon_card_legality (card_id, format_id, status)
VALUES ('base1-96', 1, 'legal'),
       ('base1-96', 2, 'legal'),
       ('base1-96', 3, 'legal');

-- ============================================================================
-- Summary of Test Data Coverage
-- ============================================================================
-- ✅ Evolution chains: Charmander → Charmeleon → Charizard, Squirtle → Wartortle → Blastoise, Pikachu → Raichu
-- ✅ Accent characters: Flabébé
-- ✅ Multiset attack costs:
--    - 4x Fire (Charizard)
--    - 2x Lightning + 1x Colorless (Raichu)
--    - 2x Water (Wartortle)
--    - 3x Water (Blastoise)
--    - 2x Fairy (Flabébé)
-- ✅ All supertypes: Pokémon, Trainer, Energy
-- ✅ Various HP ranges: 40, 50, 70, 80, 90, 100, 120, 280
-- ✅ Multiple types: Fire, Water, Lightning, Fairy
-- ✅ Multiple subtypes: Basic, Stage 1, Stage 2, ex, Item, Supporter, Special
-- ✅ Abilities: 5 different abilities
-- ✅ Weaknesses: Various types and values
-- ✅ Resistances: Fighting -30, Psychic -20, Grass -20
-- ✅ Regulation marks: None (old cards), E, G
-- ✅ Format legality: Standard, Expanded, Unlimited
-- ✅ Banned cards: Professor Oak (banned in Standard/Expanded, legal in Unlimited)
-- ✅ Rule boxes: Charizard ex with ex rule
-- ✅ Multiple artists: Ken Sugimori, Mitsuhiro Arita, 5ban Graphics
-- ✅ Multiple rarities: Common, Uncommon, Rare, Rare Holo, Rare Ultra
-- ✅ Multiple sets: Base Set, Jungle, Sword & Shield, Scarlet & Violet
