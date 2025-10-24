import json
import os
import sys
import glob
import time
from typing import Any, Dict, List, Optional, Tuple
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor, as_completed
from dataclasses import dataclass, field
import psycopg2
from psycopg2 import sql, pool
from psycopg2.extras import execute_batch

# Configuration
DATA_DIR = "data/pokemon"
MAX_WORKERS = os.cpu_count() or 4
USE_MULTIPROCESSING = True  # Set to False to use threading instead

# Database configuration from environment
DB_CONFIG = {
    'host': os.getenv('PGHOST', 'localhost'),
    'port': int(os.getenv('PGPORT', '5432')),
    'database': os.getenv('PGDATABASE', 'tcg_decklists'),
    'user': os.getenv('PGUSER', 'postgres'),
    'password': os.getenv('PGPASSWORD', 'testing1234'),
}


@dataclass
class LookupCache:
    """In-memory cache for lookup tables to minimize database queries."""
    types: Dict[str, int] = field(default_factory=dict)
    subtypes: Dict[str, int] = field(default_factory=dict)
    artists: Dict[str, int] = field(default_factory=dict)
    rarities: Dict[str, int] = field(default_factory=dict)
    formats: Dict[str, int] = field(default_factory=dict)
    sets: Dict[str, int] = field(default_factory=dict)
    pokedex: Dict[int, int] = field(default_factory=dict)
    names: Dict[str, int] = field(default_factory=dict)
    ancient_traits: Dict[Tuple[str, str], int] = field(default_factory=dict)
    abilities: Dict[Tuple[str, str, Optional[str]], int] = field(default_factory=dict)
    attacks: Dict[Tuple[str, int, Optional[str], Optional[str], Tuple[str, ...]], int] = field(default_factory=dict)
    resistances: Dict[Tuple[Optional[int], str], int] = field(default_factory=dict)
    weaknesses: Dict[Tuple[Optional[int], str], int] = field(default_factory=dict)
    rules: Dict[str, int] = field(default_factory=dict)


def load_json_file(file_path: str) -> Tuple[str, List[Dict[str, Any]]]:
    """Load a single JSON file and return its contents."""
    try:
        with open(file_path, 'r') as f:
            data = json.load(f)

        # Handle both array and single object formats
        if isinstance(data, list):
            return file_path, data
        else:
            return file_path, [data]
    except Exception as e:
        print(f"Error loading {file_path}: {e}", file=sys.stderr)
        return file_path, []


def consolidate_and_load_cards() -> List[Dict[str, Any]]:
    """
    Find all JSON files in the data directory and load them in parallel.
    """
    if not os.path.exists(DATA_DIR):
        print(f"Error: {DATA_DIR} directory not found.", file=sys.stderr)
        sys.exit(1)

    json_files = glob.glob(os.path.join(DATA_DIR, "*.json"))

    if not json_files:
        print(f"Error: No JSON files found in {DATA_DIR}", file=sys.stderr)
        sys.exit(1)

    print(f"Found {len(json_files)} JSON file(s) in {DATA_DIR}")
    print("Loading files in parallel...")

    all_cards = []
    start_time = time.time()

    # Load files in parallel using ThreadPoolExecutor (I/O-bound operation)
    with ThreadPoolExecutor(max_workers=min(MAX_WORKERS * 2, len(json_files))) as executor:
        futures = [executor.submit(load_json_file, file_path) for file_path in json_files]

        for future in as_completed(futures):
            file_path, cards = future.result()
            if cards:
                all_cards.extend(cards)
                print(f"  ✓ Loaded {os.path.basename(file_path)} ({len(cards)} cards)")

    load_time = time.time() - start_time
    print(f"Consolidated {len(all_cards)} total card records in {load_time:.2f}s")
    return all_cards


def preload_lookup_caches(cursor) -> LookupCache:
    """Preload all lookup tables into memory to minimize database queries."""
    print("Preloading lookup caches...")
    cache = LookupCache()
    start_time = time.time()

    # Load all lookup tables
    cursor.execute("SELECT id, name FROM pokemon_type")
    cache.types = {row[1]: row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, name FROM pokemon_subtype")
    cache.subtypes = {row[1]: row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, name FROM pokemon_artist")
    cache.artists = {row[1]: row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, name FROM pokemon_rarity")
    cache.rarities = {row[1]: row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, name FROM pokemon_format")
    cache.formats = {row[1]: row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, set_id FROM pokemon_set")
    cache.sets = {row[1]: row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, number FROM pokemon_pokedex")
    cache.pokedex = {row[1]: row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, name FROM pokemon_name")
    cache.names = {row[1]: row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, name, text FROM pokemon_ancient_trait")
    cache.ancient_traits = {(row[1], row[2]): row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, name, text, type FROM pokemon_ability")
    cache.abilities = {(row[1], row[2], row[3]): row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, text FROM pokemon_rule")
    cache.rules = {row[1]: row[0] for row in cursor.fetchall()}

    # Load attacks with their cost types
    cursor.execute("""
        SELECT a.id, a.name, a.converted_cost, a.damage, a.text,
               STRING_AGG(t.name, '||' ORDER BY t.name) as cost_types
        FROM pokemon_attack a
        LEFT JOIN pokemon_attack_cost ac ON a.id = ac.attack_id
        LEFT JOIN pokemon_type t ON ac.type_id = t.id
        GROUP BY a.id, a.name, a.converted_cost, a.damage, a.text
    """)
    for row in cursor.fetchall():
        attack_id, name, cost, damage, text, cost_types_str = row
        cost_types = tuple(sorted(cost_types_str.split('||'))) if cost_types_str else ()
        cache.attacks[(name, cost, damage, text, cost_types)] = attack_id

    cursor.execute("SELECT id, type_id, value FROM pokemon_resistance")
    cache.resistances = {(row[1], row[2]): row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, type_id, value FROM pokemon_weakness")
    cache.weaknesses = {(row[1], row[2]): row[0] for row in cursor.fetchall()}

    load_time = time.time() - start_time
    total_cached = (len(cache.types) + len(cache.subtypes) + len(cache.artists) +
                   len(cache.rarities) + len(cache.formats) + len(cache.sets) +
                   len(cache.pokedex) + len(cache.names) + len(cache.ancient_traits) +
                   len(cache.abilities) + len(cache.attacks) + len(cache.resistances) +
                   len(cache.weaknesses) + len(cache.rules))
    print(f"  ✓ Cached {total_cached} lookup records in {load_time:.2f}s")
    return cache


def get_or_create_id_cached(cursor, cache_dict: Dict, table: str, name_col: str, value: str) -> Optional[int]:
    """Get or create a record using cache, falling back to database if needed."""
    if not value:
        return None

    # Check cache first
    if value in cache_dict:
        return cache_dict[value]

    # Not in cache, check database
    cursor.execute(
        sql.SQL("SELECT id FROM {} WHERE {} = %s").format(
            sql.Identifier(table),
            sql.Identifier(name_col)
        ),
        (value,)
    )
    result = cursor.fetchone()

    if result:
        cache_dict[value] = result[0]
        return result[0]

    # Create new record
    cursor.execute(
        sql.SQL("INSERT INTO {} ({}) VALUES (%s) RETURNING id").format(
            sql.Identifier(table),
            sql.Identifier(name_col)
        ),
        (value,)
    )
    new_id = cursor.fetchone()[0]
    cache_dict[value] = new_id
    return new_id


def parse_damage(damage_str: Optional[str]) -> Tuple[Optional[int], Optional[str]]:
    """Parse damage string into numeric and modifier parts."""
    if not damage_str:
        return None, None

    damage_str = damage_str.strip()

    if '+' in damage_str or '-' in damage_str or '×' in damage_str:
        import re
        match = re.match(r'^(\d+)(.*)$', damage_str)
        if match:
            return int(match.group(1)), match.group(2).strip()
        return None, damage_str

    try:
        return int(damage_str), None
    except ValueError:
        return None, damage_str


def parse_hp(hp_str: Optional[str]) -> Optional[int]:
    """Parse HP string to numeric value."""
    if not hp_str:
        return None
    try:
        return int(hp_str)
    except ValueError:
        return None


def get_or_create_attack_id_cached(cursor, cache: LookupCache, name: str, converted_cost: int,
                                   damage: Optional[str], text: Optional[str], cost_types: List[str]) -> int:
    """Get or create an attack using cache."""
    cost_types_tuple = tuple(sorted(cost_types))
    cache_key = (name, converted_cost, damage, text, cost_types_tuple)

    # Check cache
    if cache_key in cache.attacks:
        return cache.attacks[cache_key]

    damage_numeric, damage_modifier = parse_damage(damage)

    # Not in cache, check database
    cursor.execute(
        """SELECT a.id FROM pokemon_attack a
           WHERE a.name = %s
           AND a.converted_cost = %s
           AND a.damage IS NOT DISTINCT FROM %s
           AND a.text IS NOT DISTINCT FROM %s""",
        (name, converted_cost, damage, text)
    )
    candidates = cursor.fetchall()

    # Check if cost types match
    for (candidate_id,) in candidates:
        cursor.execute(
            """SELECT t.name FROM pokemon_attack_cost ac
               JOIN pokemon_type t ON ac.type_id = t.id
               WHERE ac.attack_id = %s
               ORDER BY t.name""",
            (candidate_id,)
        )
        existing_costs = tuple(sorted([row[0] for row in cursor.fetchall()]))

        if existing_costs == cost_types_tuple:
            cache.attacks[cache_key] = candidate_id
            return candidate_id

    # Create new attack
    cursor.execute(
        """INSERT INTO pokemon_attack (name, converted_cost, damage, damage_numeric, damage_modifier, text)
           VALUES (%s, %s, %s, %s, %s, %s) RETURNING id""",
        (name, converted_cost, damage, damage_numeric, damage_modifier, text)
    )
    new_id = cursor.fetchone()[0]
    cache.attacks[cache_key] = new_id
    return new_id


def upsert_card(cursor, card: Dict[str, Any], cache: LookupCache) -> None:
    """Upsert a single card using cached lookups."""
    card_id = card['id']

    # Extract set_id from card ID
    set_id_str = card_id.rsplit('-', 1)[0] if '-' in card_id else None

    # Get or create foreign key IDs using cache
    set_id = None
    if set_id_str:
        if set_id_str in cache.sets:
            set_id = cache.sets[set_id_str]
        else:
            cursor.execute("SELECT id FROM pokemon_set WHERE set_id = %s", (set_id_str,))
            result = cursor.fetchone()
            if result:
                set_id = result[0]
            else:
                cursor.execute("INSERT INTO pokemon_set (set_id, name) VALUES (%s, %s) RETURNING id", (set_id_str, None))
                set_id = cursor.fetchone()[0]
            cache.sets[set_id_str] = set_id

    artist_id = get_or_create_id_cached(cursor, cache.artists, 'pokemon_artist', 'name', card.get('artist'))
    rarity_id = get_or_create_id_cached(cursor, cache.rarities, 'pokemon_rarity', 'name', card.get('rarity'))

    # Handle ancient trait
    ancient_trait_id = None
    if 'ancientTrait' in card and card['ancientTrait']:
        trait = card['ancientTrait']
        trait_key = (trait.get('name'), trait.get('text'))
        if trait_key in cache.ancient_traits:
            ancient_trait_id = cache.ancient_traits[trait_key]
        elif trait.get('name') and trait.get('text'):
            cursor.execute(
                "INSERT INTO pokemon_ancient_trait (name, text) VALUES (%s, %s) RETURNING id",
                trait_key
            )
            ancient_trait_id = cursor.fetchone()[0]
            cache.ancient_traits[trait_key] = ancient_trait_id

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
    junction_tables = [
        'pokemon_card_type', 'pokemon_card_subtype', 'pokemon_card_pokedex',
        'pokemon_card_retreat_cost', 'pokemon_card_ability', 'pokemon_card_attack',
        'pokemon_card_weakness', 'pokemon_card_resistance', 'pokemon_card_legality',
        'pokemon_card_rule', 'pokemon_card_evolution'
    ]
    for table in junction_tables:
        cursor.execute(f"DELETE FROM {table} WHERE card_id = %s", (card_id,))

    # Collect batch inserts for junction tables
    type_inserts = []
    subtype_inserts = []
    pokedex_inserts = []
    retreat_inserts = []
    ability_inserts = []
    attack_inserts = []
    attack_cost_inserts = []
    weakness_inserts = []
    resistance_inserts = []
    legality_inserts = []
    rule_inserts = []
    evolution_inserts = []

    # Types
    if 'types' in card and card['types']:
        for card_type in card['types']:
            type_id = get_or_create_id_cached(cursor, cache.types, 'pokemon_type', 'name', card_type)
            type_inserts.append((card_id, type_id))

    # Subtypes
    if 'subtypes' in card and card['subtypes']:
        for subtype in card['subtypes']:
            subtype_id = get_or_create_id_cached(cursor, cache.subtypes, 'pokemon_subtype', 'name', subtype)
            subtype_inserts.append((card_id, subtype_id))

    # Pokedex numbers
    if 'nationalPokedexNumbers' in card and card['nationalPokedexNumbers']:
        for num in card['nationalPokedexNumbers']:
            if num in cache.pokedex:
                pokedex_id = cache.pokedex[num]
            else:
                cursor.execute("INSERT INTO pokemon_pokedex (number) VALUES (%s) RETURNING id", (num,))
                pokedex_id = cursor.fetchone()[0]
                cache.pokedex[num] = pokedex_id
            pokedex_inserts.append((card_id, pokedex_id))

    # Retreat costs
    if 'retreatCost' in card and card['retreatCost']:
        for energy_type in card['retreatCost']:
            type_id = get_or_create_id_cached(cursor, cache.types, 'pokemon_type', 'name', energy_type)
            retreat_inserts.append((card_id, type_id))

    # Abilities
    if 'abilities' in card and card['abilities']:
        for ability in card['abilities']:
            ability_key = (ability.get('name'), ability.get('text'), ability.get('type'))
            if ability_key in cache.abilities:
                ability_id = cache.abilities[ability_key]
            else:
                cursor.execute(
                    "INSERT INTO pokemon_ability (name, text, type) VALUES (%s, %s, %s) RETURNING id",
                    ability_key
                )
                ability_id = cursor.fetchone()[0]
                cache.abilities[ability_key] = ability_id
            ability_inserts.append((card_id, ability_id))

    # Attacks
    if 'attacks' in card and card['attacks']:
        for attack in card['attacks']:
            cost_types = attack.get('cost', [])
            attack_id = get_or_create_attack_id_cached(
                cursor, cache,
                attack.get('name'),
                attack.get('convertedEnergyCost', 0),
                attack.get('damage'),
                attack.get('text'),
                cost_types
            )
            attack_inserts.append((card_id, attack_id))

            # Check if attack costs need to be added
            if cost_types:
                cursor.execute(
                    "SELECT COUNT(*) FROM pokemon_attack_cost WHERE attack_id = %s",
                    (attack_id,)
                )
                if cursor.fetchone()[0] == 0:
                    for cost_type in cost_types:
                        type_id = get_or_create_id_cached(cursor, cache.types, 'pokemon_type', 'name', cost_type)
                        attack_cost_inserts.append((attack_id, type_id))

    # Weaknesses
    if 'weaknesses' in card and card['weaknesses']:
        for weakness in card['weaknesses']:
            type_id = get_or_create_id_cached(cursor, cache.types, 'pokemon_type', 'name', weakness.get('type'))
            weakness_key = (type_id, weakness.get('value'))
            if weakness_key in cache.weaknesses:
                weakness_id = cache.weaknesses[weakness_key]
            else:
                cursor.execute(
                    "INSERT INTO pokemon_weakness (type_id, value) VALUES (%s, %s) RETURNING id",
                    weakness_key
                )
                weakness_id = cursor.fetchone()[0]
                cache.weaknesses[weakness_key] = weakness_id
            weakness_inserts.append((card_id, weakness_id))

    # Resistances
    if 'resistances' in card and card['resistances']:
        for resistance in card['resistances']:
            type_id = get_or_create_id_cached(cursor, cache.types, 'pokemon_type', 'name', resistance.get('type'))
            resistance_key = (type_id, resistance.get('value'))
            if resistance_key in cache.resistances:
                resistance_id = cache.resistances[resistance_key]
            else:
                cursor.execute(
                    "INSERT INTO pokemon_resistance (type_id, value) VALUES (%s, %s) RETURNING id",
                    resistance_key
                )
                resistance_id = cursor.fetchone()[0]
                cache.resistances[resistance_key] = resistance_id
            resistance_inserts.append((card_id, resistance_id))

    # Legalities
    if 'legalities' in card:
        for format_name, status in card['legalities'].items():
            format_id = get_or_create_id_cached(cursor, cache.formats, 'pokemon_format', 'name', format_name)
            legality_inserts.append((card_id, format_id, status.lower()))

    # Rules
    if 'rules' in card and card['rules']:
        for rule_text in card['rules']:
            if rule_text in cache.rules:
                rule_id = cache.rules[rule_text]
            else:
                cursor.execute("INSERT INTO pokemon_rule (text) VALUES (%s) RETURNING id", (rule_text,))
                rule_id = cursor.fetchone()[0]
                cache.rules[rule_text] = rule_id
            rule_inserts.append((card_id, rule_id))

    # Evolution
    if 'evolvesFrom' in card and card['evolvesFrom']:
        name_id = get_or_create_id_cached(cursor, cache.names, 'pokemon_name', 'name', card['evolvesFrom'])
        evolution_inserts.append((card_id, name_id, 'from'))

    if 'evolvesTo' in card and card['evolvesTo']:
        for evolves_to_name in card['evolvesTo']:
            name_id = get_or_create_id_cached(cursor, cache.names, 'pokemon_name', 'name', evolves_to_name)
            evolution_inserts.append((card_id, name_id, 'to'))

    # Execute batch inserts
    if type_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_type (card_id, type_id) VALUES (%s, %s)", type_inserts)
    if subtype_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_subtype (card_id, subtype_id) VALUES (%s, %s)", subtype_inserts)
    if pokedex_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_pokedex (card_id, pokedex_id) VALUES (%s, %s)", pokedex_inserts)
    if retreat_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_retreat_cost (card_id, type_id) VALUES (%s, %s)", retreat_inserts)
    if ability_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_ability (card_id, ability_id) VALUES (%s, %s)", ability_inserts)
    if attack_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_attack (card_id, attack_id) VALUES (%s, %s)", attack_inserts)
    if attack_cost_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_attack_cost (attack_id, type_id) VALUES (%s, %s)", attack_cost_inserts)
    if weakness_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_weakness (card_id, weakness_id) VALUES (%s, %s)", weakness_inserts)
    if resistance_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_resistance (card_id, resistance_id) VALUES (%s, %s)", resistance_inserts)
    if legality_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_legality (card_id, format_id, status) VALUES (%s, %s, %s)", legality_inserts)
    if rule_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_rule (card_id, rule_id) VALUES (%s, %s)", rule_inserts)
    if evolution_inserts:
        execute_batch(cursor, "INSERT INTO pokemon_card_evolution (card_id, name_id, direction) VALUES (%s, %s, %s)", evolution_inserts)


def process_card_batch(cards_batch: List[Dict[str, Any]], cache: LookupCache, db_config: Dict) -> Tuple[int, int]:
    """Process a batch of cards. Used for multiprocessing."""
    try:
        conn = psycopg2.connect(**db_config)
        conn.set_session(autocommit=False)
        cursor = conn.cursor()

        success_count = 0
        for card in cards_batch:
            try:
                upsert_card(cursor, card, cache)
                success_count += 1
            except Exception as e:
                print(f"Error processing card {card.get('id')}: {e}", file=sys.stderr)
                conn.rollback()
                continue

        conn.commit()
        cursor.close()
        conn.close()
        return success_count, len(cards_batch)
    except Exception as e:
        print(f"Batch processing error: {e}", file=sys.stderr)
        return 0, len(cards_batch)


def main():
    """Main migration function with optimizations."""
    print("=" * 60)
    print("Pokemon Card Migration (optimised)")
    print("=" * 60)
    print(f"Max workers: {MAX_WORKERS}")
    print(f"Processing mode: {'Multiprocessing' if USE_MULTIPROCESSING else 'Sequential'}")
    print()

    overall_start = time.time()

    # Load cards in parallel
    cards = consolidate_and_load_cards()
    print()

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

        # Preload lookup caches
        cache = preload_lookup_caches(cursor)
        print()

        # Process cards
        print(f"Processing {len(cards)} cards...")
        process_start = time.time()

        if USE_MULTIPROCESSING and MAX_WORKERS > 1:
            # Split cards into batches
            batch_size = max(10, len(cards) // (MAX_WORKERS * 4))
            batches = [cards[i:i + batch_size] for i in range(0, len(cards), batch_size)]
            print(f"Split into {len(batches)} batches ({batch_size} cards per batch)")

            # Process batches in parallel
            with ProcessPoolExecutor(max_workers=MAX_WORKERS) as executor:
                futures = [executor.submit(process_card_batch, batch, cache, DB_CONFIG) for batch in batches]

                total_processed = 0
                total_success = 0
                for future in as_completed(futures):
                    success, total = future.result()
                    total_success += success
                    total_processed += total
                    progress = (total_processed * 100) // len(cards)
                    print(f"  Progress: {total_processed}/{len(cards)} cards ({progress}%)")

            # Refresh connection after multiprocessing
            cursor.close()
            conn.close()
            conn = psycopg2.connect(**DB_CONFIG)
            cursor = conn.cursor()
        else:
            # Sequential processing with batch commits
            batch_size = 100
            for i, card in enumerate(cards, 1):
                try:
                    upsert_card(cursor, card, cache)

                    if i % batch_size == 0:
                        conn.commit()
                        progress = (i * 100) // len(cards)
                        print(f"  Progress: {i}/{len(cards)} cards ({progress}%)")

                except Exception as e:
                    print(f"Error processing card {card.get('id')}: {e}", file=sys.stderr)
                    conn.rollback()
                    raise

        # Final commit
        conn.commit()
        process_time = time.time() - process_start
        overall_time = time.time() - overall_start

        print()
        print("=" * 60)
        print(f"✓ Migration completed successfully!")
        print(f"✓ Total cards processed: {len(cards)}")
        print(f"✓ Processing time: {process_time:.2f}s")
        print(f"✓ Overall time: {overall_time:.2f}s")
        print(f"✓ Throughput: {len(cards) / process_time:.1f} cards/second")
        print("=" * 60)

    except Exception as e:
        conn.rollback()
        print(f"Migration failed: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)
    finally:
        cursor.close()
        conn.close()


if __name__ == '__main__':
    main()
