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


def get_or_create_id(cursor, table: str, name_col: str, value: str) -> Optional[int]:
    """Get or create a record in a lookup table and return its ID."""
    if not value:
        return None

    cursor.execute(
        sql.SQL("SELECT id FROM {} WHERE {} = %s").format(
            sql.Identifier(table),
            sql.Identifier(name_col)
        ),
        (value,)
    )
    result = cursor.fetchone()

    if result:
        return result[0]

    cursor.execute(
        sql.SQL("INSERT INTO {} ({}) VALUES (%s) RETURNING id").format(
            sql.Identifier(table),
            sql.Identifier(name_col)
        ),
        (value,)
    )
    return cursor.fetchone()[0]


def get_or_create_set_id(cursor, set_id: str) -> Optional[int]:
    """Get or create a set by set_id and return its ID."""
    if not set_id:
        return None

    cursor.execute("SELECT id FROM pokemon_set WHERE set_id = %s", (set_id,))
    result = cursor.fetchone()

    if result:
        return result[0]

    cursor.execute("INSERT INTO pokemon_set (set_id, name) VALUES (%s, %s) RETURNING id", (set_id, None))
    return cursor.fetchone()[0]


def get_or_create_pokedex_id(cursor, number: int) -> int:
    """Get or create a pokedex entry and return its ID."""
    cursor.execute("SELECT id FROM pokemon_pokedex WHERE number = %s", (number,))
    result = cursor.fetchone()

    if result:
        return result[0]

    cursor.execute("INSERT INTO pokemon_pokedex (number) VALUES (%s) RETURNING id", (number,))
    return cursor.fetchone()[0]


def get_or_create_ancient_trait_id(cursor, name: str, text: str) -> Optional[int]:
    """Get or create an ancient trait and return its ID."""
    if not name or not text:
        return None

    cursor.execute(
        "SELECT id FROM pokemon_ancient_trait WHERE name = %s AND text = %s",
        (name, text)
    )
    result = cursor.fetchone()

    if result:
        return result[0]

    cursor.execute(
        "INSERT INTO pokemon_ancient_trait (name, text) VALUES (%s, %s) RETURNING id",
        (name, text)
    )
    return cursor.fetchone()[0]


def get_or_create_ability_id(cursor, name: str, text: str, ability_type: Optional[str]) -> int:
    """Get or create an ability and return its ID."""
    cursor.execute(
        "SELECT id FROM pokemon_ability WHERE name = %s AND text = %s AND type IS NOT DISTINCT FROM %s",
        (name, text, ability_type)
    )
    result = cursor.fetchone()

    if result:
        return result[0]

    cursor.execute(
        "INSERT INTO pokemon_ability (name, text, type) VALUES (%s, %s, %s) RETURNING id",
        (name, text, ability_type)
    )
    return cursor.fetchone()[0]


def parse_damage(damage_str: Optional[str]) -> tuple[Optional[int], Optional[str]]:
    """Parse damage string into numeric and modifier parts."""
    if not damage_str:
        return None, None

    # Remove any whitespace
    damage_str = damage_str.strip()

    # Check for modifiers
    if '+' in damage_str or '-' in damage_str or '×' in damage_str:
        # Extract numeric part and modifier
        import re
        match = re.match(r'^(\d+)(.*)$', damage_str)
        if match:
            return int(match.group(1)), match.group(2).strip()
        return None, damage_str

    # Pure numeric
    try:
        return int(damage_str), None
    except ValueError:
        return None, damage_str


def get_or_create_attack_id(cursor, name: str, converted_cost: int, damage: Optional[str], text: Optional[str]) -> int:
    """Get or create an attack and return its ID."""
    damage_numeric, damage_modifier = parse_damage(damage)

    cursor.execute(
        """SELECT id FROM pokemon_attack 
           WHERE name = %s 
           AND converted_cost = %s 
           AND damage IS NOT DISTINCT FROM %s 
           AND text IS NOT DISTINCT FROM %s""",
        (name, converted_cost, damage, text)
    )
    result = cursor.fetchone()

    if result:
        return result[0]

    cursor.execute(
        """INSERT INTO pokemon_attack (name, converted_cost, damage, damage_numeric, damage_modifier, text) 
           VALUES (%s, %s, %s, %s, %s, %s) RETURNING id""",
        (name, converted_cost, damage, damage_numeric, damage_modifier, text)
    )
    return cursor.fetchone()[0]


def get_or_create_resistance_id(cursor, type_id: Optional[int], value: str) -> int:
    """Get or create a resistance and return its ID."""
    cursor.execute(
        "SELECT id FROM pokemon_resistance WHERE type_id IS NOT DISTINCT FROM %s AND value = %s",
        (type_id, value)
    )
    result = cursor.fetchone()

    if result:
        return result[0]

    cursor.execute(
        "INSERT INTO pokemon_resistance (type_id, value) VALUES (%s, %s) RETURNING id",
        (type_id, value)
    )
    return cursor.fetchone()[0]


def get_or_create_weakness_id(cursor, type_id: Optional[int], value: str) -> int:
    """Get or create a weakness and return its ID."""
    cursor.execute(
        "SELECT id FROM pokemon_weakness WHERE type_id IS NOT DISTINCT FROM %s AND value = %s",
        (type_id, value)
    )
    result = cursor.fetchone()

    if result:
        return result[0]

    cursor.execute(
        "INSERT INTO pokemon_weakness (type_id, value) VALUES (%s, %s) RETURNING id",
        (type_id, value)
    )
    return cursor.fetchone()[0]


def get_or_create_rule_id(cursor, text: str) -> int:
    """Get or create a rule and return its ID."""
    cursor.execute("SELECT id FROM pokemon_rule WHERE text = %s", (text,))
    result = cursor.fetchone()

    if result:
        return result[0]

    cursor.execute("INSERT INTO pokemon_rule (text) VALUES (%s) RETURNING id", (text,))
    return cursor.fetchone()[0]


def parse_hp(hp_str: Optional[str]) -> Optional[int]:
    """Parse HP string to numeric value."""
    if not hp_str:
        return None
    try:
        return int(hp_str)
    except ValueError:
        return None


def upsert_card(cursor, card: Dict[str, Any]) -> None:
    """Upsert a single card and all its related data."""
    card_id = card['id']

    # Extract set_id from card ID (e.g., "base1-1" -> "base1")
    set_id_str = card_id.rsplit('-', 1)[0] if '-' in card_id else None

    # Get or create foreign key IDs
    set_id = get_or_create_set_id(cursor, set_id_str)
    artist_id = get_or_create_id(cursor, 'pokemon_artist', 'name', card.get('artist'))
    rarity_id = get_or_create_id(cursor, 'pokemon_rarity', 'name', card.get('rarity'))

    # Handle ancient trait
    ancient_trait_id = None
    if 'ancientTrait' in card and card['ancientTrait']:
        trait = card['ancientTrait']
        ancient_trait_id = get_or_create_ancient_trait_id(
            cursor,
            trait.get('name'),
            trait.get('text')
        )

    # Parse HP
    hp_str = card.get('hp')
    hp_numeric = parse_hp(hp_str)

    # Upsert main card
    cursor.execute("""
        INSERT INTO pokemon_card (
            id, name, supertype, hp, hp_numeric, number, set_id, artist_id, rarity_id,
            flavor_text, level, regulation_mark, converted_retreat_cost,
            image_low, image_high, ancient_trait_id
        ) VALUES (
            %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
        )
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            supertype = EXCLUDED.supertype,
            hp = EXCLUDED.hp,
            hp_numeric = EXCLUDED.hp_numeric,
            number = EXCLUDED.number,
            set_id = EXCLUDED.set_id,
            artist_id = EXCLUDED.artist_id,
            rarity_id = EXCLUDED.rarity_id,
            flavor_text = EXCLUDED.flavor_text,
            level = EXCLUDED.level,
            regulation_mark = EXCLUDED.regulation_mark,
            converted_retreat_cost = EXCLUDED.converted_retreat_cost,
            image_low = EXCLUDED.image_low,
            image_high = EXCLUDED.image_high,
            ancient_trait_id = EXCLUDED.ancient_trait_id
    """, (
        card_id,
        card.get('name'),
        card.get('supertype'),
        hp_str,
        hp_numeric,
        card.get('number'),
        set_id,
        artist_id,
        rarity_id,
        card.get('flavorText'),
        card.get('level'),
        card.get('regulationMark'),
        card.get('convertedRetreatCost'),
        card.get('images', {}).get('small'),
        card.get('images', {}).get('large'),
        ancient_trait_id
    ))

    # Delete existing junction table records
    cursor.execute("DELETE FROM pokemon_card_type WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_subtype WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_pokedex WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_retreat_cost WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_ability WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_attack WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_weakness WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_resistance WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_legality WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_rule WHERE card_id = %s", (card_id,))
    cursor.execute("DELETE FROM pokemon_card_evolution WHERE card_id = %s", (card_id,))

    # Insert types
    if 'types' in card and card['types']:
        for card_type in card['types']:
            type_id = get_or_create_id(cursor, 'pokemon_type', 'name', card_type)
            cursor.execute(
                "INSERT INTO pokemon_card_type (card_id, type_id) VALUES (%s, %s)",
                (card_id, type_id)
            )

    # Insert subtypes
    if 'subtypes' in card and card['subtypes']:
        for subtype in card['subtypes']:
            subtype_id = get_or_create_id(cursor, 'pokemon_subtype', 'name', subtype)
            cursor.execute(
                "INSERT INTO pokemon_card_subtype (card_id, subtype_id) VALUES (%s, %s)",
                (card_id, subtype_id)
            )

    # Insert national pokedex numbers
    if 'nationalPokedexNumbers' in card and card['nationalPokedexNumbers']:
        for num in card['nationalPokedexNumbers']:
            pokedex_id = get_or_create_pokedex_id(cursor, num)
            cursor.execute(
                "INSERT INTO pokemon_card_pokedex (card_id, pokedex_id) VALUES (%s, %s)",
                (card_id, pokedex_id)
            )

    # Insert retreat costs
    if 'retreatCost' in card and card['retreatCost']:
        for energy_type in card['retreatCost']:
            type_id = get_or_create_id(cursor, 'pokemon_type', 'name', energy_type)
            cursor.execute(
                "INSERT INTO pokemon_card_retreat_cost (card_id, type_id) VALUES (%s, %s)",
                (card_id, type_id)
            )

    # Insert abilities
    if 'abilities' in card and card['abilities']:
        for ability in card['abilities']:
            ability_id = get_or_create_ability_id(
                cursor,
                ability.get('name'),
                ability.get('text'),
                ability.get('type')
            )
            cursor.execute(
                "INSERT INTO pokemon_card_ability (card_id, ability_id) VALUES (%s, %s)",
                (card_id, ability_id)
            )

    # Insert attacks
    if 'attacks' in card and card['attacks']:
        for attack in card['attacks']:
            attack_id = get_or_create_attack_id(
                cursor,
                attack.get('name'),
                attack.get('convertedEnergyCost', 0),
                attack.get('damage'),
                attack.get('text')
            )
            cursor.execute(
                "INSERT INTO pokemon_card_attack (card_id, attack_id) VALUES (%s, %s)",
                (card_id, attack_id)
            )

            # Insert attack costs
            if 'cost' in attack and attack['cost']:
                for cost_type in attack['cost']:
                    type_id = get_or_create_id(cursor, 'pokemon_type', 'name', cost_type)
                    cursor.execute(
                        "INSERT INTO pokemon_attack_cost (attack_id, type_id) VALUES (%s, %s)",
                        (attack_id, type_id)
                    )

    # Insert weaknesses
    if 'weaknesses' in card and card['weaknesses']:
        for weakness in card['weaknesses']:
            type_id = get_or_create_id(cursor, 'pokemon_type', 'name', weakness.get('type'))
            weakness_id = get_or_create_weakness_id(cursor, type_id, weakness.get('value'))
            cursor.execute(
                "INSERT INTO pokemon_card_weakness (card_id, weakness_id) VALUES (%s, %s)",
                (card_id, weakness_id)
            )

    # Insert resistances
    if 'resistances' in card and card['resistances']:
        for resistance in card['resistances']:
            type_id = get_or_create_id(cursor, 'pokemon_type', 'name', resistance.get('type'))
            resistance_id = get_or_create_resistance_id(cursor, type_id, resistance.get('value'))
            cursor.execute(
                "INSERT INTO pokemon_card_resistance (card_id, resistance_id) VALUES (%s, %s)",
                (card_id, resistance_id)
            )

    # Insert legalities
    if 'legalities' in card:
        legalities = card['legalities']
        for format_name, status in legalities.items():
            format_id = get_or_create_id(cursor, 'pokemon_format', 'name', format_name)
            cursor.execute(
                "INSERT INTO pokemon_card_legality (card_id, format_id, status) VALUES (%s, %s, %s)",
                (card_id, format_id, status.lower())
            )

    # Insert rules
    if 'rules' in card and card['rules']:
        for rule_text in card['rules']:
            rule_id = get_or_create_rule_id(cursor, rule_text)
            cursor.execute(
                "INSERT INTO pokemon_card_rule (card_id, rule_id) VALUES (%s, %s)",
                (card_id, rule_id)
            )

    # Insert evolvesFrom
    if 'evolvesFrom' in card and card['evolvesFrom']:
        name_id = get_or_create_id(cursor, 'pokemon_name', 'name', card['evolvesFrom'])
        cursor.execute(
            "INSERT INTO pokemon_card_evolution (card_id, name_id, direction) VALUES (%s, %s, 'from')",
            (card_id, name_id)
        )

    # Insert evolvesTo
    if 'evolvesTo' in card and card['evolvesTo']:
        for evolves_to_name in card['evolvesTo']:
            name_id = get_or_create_id(cursor, 'pokemon_name', 'name', evolves_to_name)
            cursor.execute(
                "INSERT INTO pokemon_card_evolution (card_id, name_id, direction) VALUES (%s, %s, 'to')",
                (card_id, name_id)
            )


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