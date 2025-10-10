import json
import os
import sys
import glob
from typing import Any, Dict, List, Optional
import psycopg2
from psycopg2 import sql
from psycopg2.extras import execute_batch

# Configuration
DATA_DIR = "data/pokemon"

# Database configuration from environment
DB_CONFIG = {
    'host': os.getenv('PGHOST', 'localhost'),
    'port': int(os.getenv('PGPORT', '5432')),
    'database': os.getenv('PGDATABASE', 'tcg_decklists'),
    'user': os.getenv('PGUSER', 'postgres'),
    'password': os.getenv('PGPASSWORD', 'testing1234'),
}


def consolidate_and_load_cards() -> List[Dict[str, Any]]:
    """
    Find all JSON files in the data directory, validate them,
    and consolidate into a single list of cards.
    """
    if not os.path.exists(DATA_DIR):
        print(f"Error: {DATA_DIR} directory not found.", file=sys.stderr)
        sys.exit(1)

    json_files = glob.glob(os.path.join(DATA_DIR, "*.json"))

    if not json_files:
        print(f"Error: No JSON files found in {DATA_DIR}", file=sys.stderr)
        sys.exit(1)

    print(f"Found {len(json_files)} JSON file(s) in {DATA_DIR}")

    all_cards = []

    for json_file in json_files:
        try:
            with open(json_file, 'r') as f:
                data = json.load(f)

            # Handle both array and single object formats
            if isinstance(data, list):
                all_cards.extend(data)
            else:
                all_cards.append(data)

            print(f"  ✓ Loaded {json_file}")

        except json.JSONDecodeError as e:
            print(f"Error: Invalid JSON in {json_file}: {e}", file=sys.stderr)
            sys.exit(1)
        except Exception as e:
            print(f"Error reading {json_file}: {e}", file=sys.stderr)
            sys.exit(1)

    print(f"Consolidated {len(all_cards)} total card records")
    return all_cards


def upsert_card(cursor, card: Dict[str, Any]) -> None:
    """Upsert a single card and all its related data."""
    card_id = card['id']

    # Upsert main card
    cursor.execute("""
        INSERT INTO pokemon_card (
            id, name, supertype, hp, number, artist, rarity, flavor_text,
            evolves_from, level, regulation_mark, converted_retreat_cost,
            image_small, image_large, created_at, updated_at
        ) VALUES (
            %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        )
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            supertype = EXCLUDED.supertype,
            hp = EXCLUDED.hp,
            number = EXCLUDED.number,
            artist = EXCLUDED.artist,
            rarity = EXCLUDED.rarity,
            flavor_text = EXCLUDED.flavor_text,
            evolves_from = EXCLUDED.evolves_from,
            level = EXCLUDED.level,
            regulation_mark = EXCLUDED.regulation_mark,
            converted_retreat_cost = EXCLUDED.converted_retreat_cost,
            image_small = EXCLUDED.image_small,
            image_large = EXCLUDED.image_large,
            updated_at = CURRENT_TIMESTAMP
    """, (
        card_id,
        card.get('name'),
        card.get('supertype'),
        card.get('hp'),
        card.get('number'),
        card.get('artist'),
        card.get('rarity'),
        card.get('flavorText'),
        card.get('evolvesFrom'),
        card.get('level'),
        card.get('regulationMark'),
        card.get('convertedRetreatCost'),
        card.get('images', {}).get('small'),
        card.get('images', {}).get('large')
    ))

    # Delete existing related records (we'll re-insert them)
    # This is safe because we're not deleting the main card
    cursor.execute("DELETE FROM pokemon_card_type WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_subtype WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_evolves_to WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_national_pokedex_number WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_retreat_cost WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_weakness WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_resistance WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_legality WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_rule WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_ancient_trait WHERE card_id = %s", (card_id,))

    # Delete attacks (cascade will delete attack_costs)
    cursor.execute("DELETE FROM pokemon_attack WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_ability WHERE card_id = %s", (card_id,))

    # Insert types
    if 'types' in card and card['types']:
        types_data = [(card_id, t, i) for i, t in enumerate(card['types'])]
        execute_batch(cursor, """
            INSERT INTO pokemon_card_type (card_id, type, position)
            VALUES (%s, %s, %s)
        """, types_data)

    # Insert subtypes
    if 'subtypes' in card and card['subtypes']:
        subtypes_data = [(card_id, st, i) for i, st in enumerate(card['subtypes'])]
        execute_batch(cursor, """
            INSERT INTO pokemon_card_subtype (card_id, subtype, position)
            VALUES (%s, %s, %s)
        """, subtypes_data)

    # Insert evolvesTo
    if 'evolvesTo' in card and card['evolvesTo']:
        evolves_data = [(card_id, evo, i) for i, evo in enumerate(card['evolvesTo'])]
        execute_batch(cursor, """
            INSERT INTO pokemon_card_evolves_to (card_id, evolves_to_name, position)
            VALUES (%s, %s, %s)
        """, evolves_data)

    # Insert national pokedex numbers
    if 'nationalPokedexNumbers' in card and card['nationalPokedexNumbers']:
        pokedex_data = [(card_id, num, i) for i, num in enumerate(card['nationalPokedexNumbers'])]
        execute_batch(cursor, """
            INSERT INTO pokemon_card_national_pokedex_number (card_id, pokedex_number, position)
            VALUES (%s, %s, %s)
        """, pokedex_data)

    # Insert retreat costs
    if 'retreatCost' in card and card['retreatCost']:
        retreat_data = [(card_id, cost, i) for i, cost in enumerate(card['retreatCost'])]
        execute_batch(cursor, """
            INSERT INTO pokemon_card_retreat_cost (card_id, energy_type, position)
            VALUES (%s, %s, %s)
        """, retreat_data)

    # Insert attacks with their costs
    if 'attacks' in card and card['attacks']:
        for i, attack in enumerate(card['attacks']):
            cursor.execute("""
                INSERT INTO pokemon_attack (
                    card_id, name, converted_energy_cost, damage, text, position
                ) VALUES (%s, %s, %s, %s, %s, %s)
                RETURNING id
            """, (
                card_id,
                attack.get('name'),
                attack.get('convertedEnergyCost'),
                attack.get('damage'),
                attack.get('text'),
                i
            ))
            attack_id = cursor.fetchone()[0]

            # Insert attack costs
            if 'cost' in attack and attack['cost']:
                cost_data = [(attack_id, cost, j) for j, cost in enumerate(attack['cost'])]
                execute_batch(cursor, """
                    INSERT INTO pokemon_attack_cost (attack_id, energy_type, position)
                    VALUES (%s, %s, %s)
                """, cost_data)

    # Insert abilities
    if 'abilities' in card and card['abilities']:
        abilities_data = [
            (card_id, ability.get('name'), ability.get('text'), ability.get('type'), i)
            for i, ability in enumerate(card['abilities'])
        ]
        execute_batch(cursor, """
            INSERT INTO pokemon_ability (card_id, name, text, type, position)
            VALUES (%s, %s, %s, %s, %s)
        """, abilities_data)

    # Insert weaknesses
    if 'weaknesses' in card and card['weaknesses']:
        weakness_data = [
            (card_id, w.get('type'), w.get('value'))
            for w in card['weaknesses']
        ]
        execute_batch(cursor, """
            INSERT INTO pokemon_weakness (card_id, type, value)
            VALUES (%s, %s, %s)
        """, weakness_data)

    # Insert resistances
    if 'resistances' in card and card['resistances']:
        resistance_data = [
            (card_id, r.get('type'), r.get('value'))
            for r in card['resistances']
        ]
        execute_batch(cursor, """
            INSERT INTO pokemon_resistance (card_id, type, value)
            VALUES (%s, %s, %s)
        """, resistance_data)

    # Insert legalities
    if 'legalities' in card:
        legalities = card['legalities']
        legality_data = []
        for format_name in ['unlimited', 'expanded', 'standard']:
            if format_name in legalities:
                legality_data.append((card_id, format_name, legalities[format_name]))

        if legality_data:
            execute_batch(cursor, """
                INSERT INTO pokemon_legality (card_id, format, status)
                VALUES (%s, %s, %s)
            """, legality_data)

    # Insert rules
    if 'rules' in card and card['rules']:
        rules_data = [(card_id, rule, i) for i, rule in enumerate(card['rules'])]
        execute_batch(cursor, """
            INSERT INTO pokemon_rule (card_id, rule_text, position)
            VALUES (%s, %s, %s)
        """, rules_data)

    # Insert ancient trait
    if 'ancientTrait' in card and card['ancientTrait']:
        trait = card['ancientTrait']
        cursor.execute("""
            INSERT INTO pokemon_ancient_trait (card_id, name, text)
            VALUES (%s, %s, %s)
        """, (card_id, trait.get('name'), trait.get('text')))


def main():
    """Main migration function."""
    print("Starting Pokemon card migration...")

    # Load cards
    cards = consolidate_and_load_cards()

    # Connect to database
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        conn.set_session(autocommit=False)
        print(f"Connected to database: {DB_CONFIG['database']}@{DB_CONFIG['host']}")
    except Exception as e:
        print(f"Error connecting to database: {e}", file=sys.stderr)
        sys.exit(1)

    try:
        cursor = conn.cursor()

        # Process cards in batches with progress reporting
        batch_size = 100
        total = len(cards)

        for i, card in enumerate(cards, 1):
            try:
                upsert_card(cursor, card)

                # Commit in batches for better performance
                if i % batch_size == 0:
                    conn.commit()
                    print(f"Progress: {i}/{total} cards processed ({i*100//total}%)")

            except Exception as e:
                print(f"Error processing card {card.get('id')}: {e}", file=sys.stderr)
                conn.rollback()
                raise

        # Final commit
        conn.commit()
        print(f"✓ Migration completed successfully!")
        print(f"✓ Total cards processed: {total}")

    except Exception as e:
        conn.rollback()
        print(f"Migration failed: {e}", file=sys.stderr)
        sys.exit(1)
    finally:
        cursor.close()
        conn.close()


if __name__ == '__main__':
    main()