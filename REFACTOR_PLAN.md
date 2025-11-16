# Data Pipeline ETL Refactor - Comprehensive Implementation Plan

**Author:** Claude (Plan Agent)
**Date:** 2025-11-16
**Branch:** claude/plan-etl-refactor-01SPMcZ3GdLXvab5DVpCZzQw
**Estimated Timeline:** 5-6 weeks

---

## Executive Summary

This plan details a complete refactor of the Pokemon-specific data pipeline into a generic, config-driven ETL system that can support multiple card games without code changes. The refactor will replace ~750 lines of hardcoded Pokemon logic with a flexible configuration-based approach.

**Key Transformation:**
- **Before:** Hardcoded Python scripts specific to Pokemon
- **After:** Generic ETL engine driven by YAML configuration files

---

## 1. Current State Analysis

### 1.1 Existing Architecture

**Components:**
- `run.sh` (169 lines): Bash orchestrator managing git clones, version tracking, and Python execution
- `metadata.json`: Tracks data sources, versions, and migration scripts
- `_migrate_lib.py` (758 lines): Contains ALL Pokemon-specific migration logic
- `migrate-sets.py` & `migrate-cards.py`: Thin wrappers calling functions from `_migrate_lib.py`

**Hardcoded Pokemon-Specific Logic:**

1. **Database Connection** (lines 14-21): Direct PostgreSQL config
2. **File Path References** (lines 11-12): Hardcoded paths to Pokemon data
3. **Field Mappings** (throughout):
   - Simple: `hp`, `name`, `supertype`, `number`, `artist`, `rarity`
   - Nested: `images.small`, `images.large`, `images.symbol`, `images.logo`
   - Arrays: `types[]`, `subtypes[]`, `retreatCost[]`, `rules[]`
   - Complex nested: `attacks[].cost[]`, `abilities[].name`, `weaknesses[].type`
4. **Lookup Tables** (10+ functions): artists, rarities, types, subtypes, formats, pokedex, etc.
5. **Junction Tables** (15+ tables): card_type, card_subtype, card_attack, attack_cost, etc.
6. **Custom Transformations**:
   - `parse_hp()`: String to integer
   - `parse_damage()`: Extract numeric and modifier parts
   - Array flattening with quantity tracking (retreat costs, attack costs)
7. **Foreign Key Resolution**: Sets must load before cards
8. **Complex Deduplication**: Attacks deduplicated by name + cost + damage + text + cost_types

### 1.2 Pain Points

1. **No Extensibility**: Adding Yu-Gi-Oh would require duplicating all 750 lines
2. **Mixed Concerns**: ETL logic mixed with Pokemon-specific mappings
3. **No Reusability**: Each card game needs custom Python code
4. **Brittle**: Schema changes require code updates
5. **Testing Difficulty**: Hard to test generic ETL behavior
6. **Bash Dependency**: Python would be cleaner for orchestration

---

## 2. Target Architecture

### 2.1 Design Principles

1. **Separation of Concerns**: Extract, Transform, Load as distinct phases
2. **Configuration-Driven**: All game-specific logic in JSON/YAML config
3. **Generic Python**: Code works for ANY card game
4. **Declarative**: Configs describe WHAT to do, not HOW
5. **Composable**: Small, reusable transformation functions
6. **Testable**: Easy to unit test with mock configs

### 2.2 Directory Structure

```
tools/data-pipeline/
├── config/
│   ├── games/
│   │   ├── pokemon/
│   │   │   ├── sources.yaml          # Data source definitions
│   │   │   ├── entities.yaml         # Entity definitions & dependencies
│   │   │   ├── sets.yaml             # Set entity mapping
│   │   │   └── cards.yaml            # Card entity mapping
│   │   └── yugioh/                   # Future: Yu-Gi-Oh config
│   │       ├── sources.yaml
│   │       └── ...
│   └── database.yaml                 # DB connection config
├── src/
│   ├── __init__.py
│   ├── main.py                       # Entry point (replaces run.sh)
│   ├── extract/
│   │   ├── __init__.py
│   │   ├── base.py                   # Abstract extractor
│   │   ├── git.py                    # Git source extractor
│   │   ├── url.py                    # HTTP/API extractor
│   │   └── local.py                  # Local file extractor
│   ├── transform/
│   │   ├── __init__.py
│   │   ├── engine.py                 # Transform orchestration
│   │   ├── mappers.py                # Field mapping logic
│   │   ├── transforms.py             # Transform functions library
│   │   └── validators.py             # Data validation
│   ├── load/
│   │   ├── __init__.py
│   │   ├── database.py               # Database loader
│   │   ├── lookup_manager.py         # Lookup table handler
│   │   └── junction_manager.py       # Junction table handler
│   ├── models/
│   │   ├── __init__.py
│   │   ├── config.py                 # Config model classes
│   │   └── entity.py                 # Entity model classes
│   └── utils/
│       ├── __init__.py
│       ├── logger.py                 # Logging utilities
│       └── version_tracker.py        # Version tracking
├── data/                             # Downloaded data (gitignored)
├── metadata.json                     # Version tracking (auto-updated)
├── requirements.txt
└── README.md
```

### 2.3 Data Flow

```
1. ORCHESTRATION (main.py)
   ↓
2. EXTRACT (extract/*.py)
   - Read sources.yaml for each game
   - Clone git repos / download files
   - Track versions
   ↓
3. TRANSFORM (transform/*.py)
   - Read entities.yaml to understand dependencies
   - For each entity (sets, cards, etc.):
     - Read entity config (sets.yaml, cards.yaml)
     - Apply field mappings
     - Run transformations
     - Validate data
   ↓
4. LOAD (load/*.py)
   - Respect entity dependencies
   - Load in correct order (sets before cards)
   - Handle lookup tables automatically
   - Handle junction tables automatically
   - Track success/failure
```

---

## 3. Configuration Schema Design

### 3.1 sources.yaml

Defines where to get data for a game.

```yaml
game: pokemon
version: 1.0

sources:
  - name: pokemon-tcg-data
    type: git
    url: https://github.com/PokemonTCG/pokemon-tcg-data
    update_check: github_commits  # Check GitHub API for updates
    outputs:
      - name: sets
        path: sets/en.json
        destination: data/pokemon/sets
      - name: cards
        path: cards/en
        destination: data/pokemon/cards
```

### 3.2 entities.yaml

Defines entities and their dependencies.

```yaml
game: pokemon
version: 1.0

# Entity loading order (respects dependencies)
entities:
  - name: sets
    config: sets.yaml
    dependencies: []

  - name: cards
    config: cards.yaml
    dependencies:
      - sets  # Cards must load after sets
```

### 3.3 Entity Mapping Config (sets.yaml)

Defines how to map JSON → Database for a specific entity.

```yaml
entity: pokemon_sets
version: 1.0

# Data source
source:
  type: json
  location: data/pokemon/sets/en.json
  is_array: true

# Primary table
table: pokemon_set

# Primary key config
primary_key:
  field: id
  column: set_id
  type: text

# Upsert strategy
strategy: upsert
conflict_target: set_id

# Simple field mappings (JSON field → DB column)
fields:
  - json_path: id
    column: set_id
    type: text
    required: true

  - json_path: name
    column: name
    type: text

  - json_path: series
    column: series
    type: text

  - json_path: printedTotal
    column: printed_total
    type: integer

  - json_path: total
    column: total
    type: integer

  - json_path: ptcgoCode
    column: ptcgo_code
    type: text

  - json_path: releaseDate
    column: release_date
    type: text

  - json_path: updatedAt
    column: updated_at
    type: text

# Nested object mappings
nested_fields:
  - json_path: images.symbol
    column: image_symbol
    type: text

  - json_path: images.logo
    column: image_logo
    type: text
```

### 3.4 Complex Entity Mapping (cards.yaml)

Shows the full complexity of Pokemon cards.

```yaml
entity: pokemon_cards
version: 1.0

source:
  type: json_multi
  location: data/pokemon/cards/*.json
  is_array: true
  consolidate: true  # Merge all files into one array

table: pokemon_card

primary_key:
  field: id
  column: id
  type: text

strategy: upsert
conflict_target: id

# Simple fields
fields:
  - json_path: id
    column: id
    type: text
    required: true

  - json_path: name
    column: name
    type: text
    required: true

  - json_path: supertype
    column: supertype
    type: text
    required: true

  - json_path: number
    column: number
    type: text
    required: true

  - json_path: flavorText
    column: flavor_text
    type: text

  - json_path: level
    column: level
    type: text

  - json_path: regulationMark
    column: regulation_mark
    type: text

  - json_path: convertedRetreatCost
    column: converted_retreat_cost
    type: integer

# Nested simple fields
nested_fields:
  - json_path: images.small
    column: image_low
    type: text

  - json_path: images.large
    column: image_high
    type: text

# Fields requiring transformation
transformed_fields:
  - json_path: hp
    column: hp
    type: text
    transform: none

  - json_path: hp
    column: hp_numeric
    type: integer
    transform: parse_integer
    nullable: true

# Foreign key lookups (creates/finds in lookup tables)
lookups:
  - json_path: id
    column: set_id
    lookup_table: pokemon_set
    lookup_column: set_id
    lookup_match_field: id
    transform: extract_set_id  # Custom: "base1-1" → "base1"

  - json_path: artist
    column: artist_id
    lookup_table: pokemon_artist
    lookup_column: name
    create_if_missing: true

  - json_path: rarity
    column: rarity_id
    lookup_table: pokemon_rarity
    lookup_column: name
    create_if_missing: true

# Complex nested object (creates entity and references it)
nested_entities:
  - json_path: ancientTrait
    column: ancient_trait_id
    entity_table: pokemon_ancient_trait
    create_if_missing: true
    match_fields:
      - json_field: name
        column: name
      - json_field: text
        column: text

# Array fields → junction tables
array_junctions:
  # Simple array: types → card_type junction
  - json_path: types
    junction_table: pokemon_card_type
    lookup_table: pokemon_type
    lookup_column: name
    create_if_missing: true

  # Simple array: subtypes → card_subtype junction
  - json_path: subtypes
    junction_table: pokemon_card_subtype
    lookup_table: pokemon_subtype
    lookup_column: name
    create_if_missing: true

  # Integer array: nationalPokedexNumbers → card_pokedex junction
  - json_path: nationalPokedexNumbers
    junction_table: pokemon_card_pokedex
    lookup_table: pokemon_pokedex
    lookup_column: number
    create_if_missing: true

  # String array: rules → card_rule junction
  - json_path: rules
    junction_table: pokemon_card_rule
    lookup_table: pokemon_rule
    lookup_column: text
    create_if_missing: true

# Array with quantity tracking
array_with_quantity:
  # retreatCost: ["Colorless", "Colorless"] → quantities per type
  - json_path: retreatCost
    junction_table: pokemon_card_retreat_cost
    lookup_table: pokemon_type
    lookup_column: name
    create_if_missing: true
    count_occurrences: true  # Groups and counts

# Complex nested arrays (objects within arrays)
nested_array_entities:
  # abilities[{name, text, type}]
  - json_path: abilities
    junction_table: pokemon_card_ability
    entity_table: pokemon_ability
    create_if_missing: true
    match_fields:
      - json_field: name
        column: name
      - json_field: text
        column: text
      - json_field: type
        column: type

  # attacks[{name, cost[], convertedEnergyCost, damage, text}]
  - json_path: attacks
    junction_table: pokemon_card_attack
    entity_table: pokemon_attack
    create_if_missing: true
    match_fields:
      - json_field: name
        column: name
      - json_field: convertedEnergyCost
        column: converted_cost
      - json_field: damage
        column: damage
      - json_field: text
        column: text
    transformed_fields:
      - json_field: damage
        column: damage_numeric
        transform: parse_damage_numeric
        nullable: true
      - json_field: damage
        column: damage_modifier
        transform: parse_damage_modifier
        nullable: true
    # Nested within attacks: cost array
    sub_array_with_quantity:
      - json_field: cost
        junction_table: pokemon_attack_cost
        lookup_table: pokemon_type
        lookup_column: name
        create_if_missing: true
        count_occurrences: true
    # Attack uniqueness includes cost types
    uniqueness_includes_sub_array: cost

  # weaknesses[{type, value}]
  - json_path: weaknesses
    junction_table: pokemon_card_weakness
    entity_table: pokemon_weakness
    create_if_missing: true
    match_fields:
      - json_field: type
        column: type_id
        lookup_table: pokemon_type
        lookup_column: name
        create_if_missing: true
      - json_field: value
        column: value

  # resistances[{type, value}]
  - json_path: resistances
    junction_table: pokemon_card_resistance
    entity_table: pokemon_resistance
    create_if_missing: true
    match_fields:
      - json_field: type
        column: type_id
        lookup_table: pokemon_type
        lookup_column: name
        create_if_missing: true
      - json_field: value
        column: value

# Evolution chains
evolution_mappings:
  - json_path: evolvesFrom
    junction_table: pokemon_card_evolution
    lookup_table: pokemon_name
    lookup_column: name
    create_if_missing: true
    extra_columns:
      direction: 'from'

  - json_path: evolvesTo
    is_array: true
    junction_table: pokemon_card_evolution
    lookup_table: pokemon_name
    lookup_column: name
    create_if_missing: true
    extra_columns:
      direction: 'to'

# Dynamic legality fields (object with unknown keys)
dynamic_object_mapping:
  - json_path: legalities
    junction_table: pokemon_card_legality
    key_lookup_table: pokemon_format
    key_lookup_column: name
    value_column: status
    value_transform: lowercase
    create_keys_if_missing: true
```

### 3.5 Transform Functions Library

Built-in transforms available in `transform/transforms.py`:

```yaml
# Transform function registry
transforms:
  # String/Text
  - name: lowercase
    type: string → string

  - name: uppercase
    type: string → string

  - name: trim
    type: string → string

  # Numeric
  - name: parse_integer
    type: string → integer
    nullable: true

  - name: parse_float
    type: string → float
    nullable: true

  # Custom Pokemon
  - name: parse_damage_numeric
    type: string → integer
    example: "50+" → 50

  - name: parse_damage_modifier
    type: string → string
    example: "50+" → "+"

  - name: extract_set_id
    type: string → string
    example: "base1-1" → "base1"

  # Date/Time
  - name: parse_date
    type: string → date

  - name: parse_datetime
    type: string → datetime
```

---

## 4. Implementation Roadmap

### Phase 1: Foundation (Week 1)

**Goal:** Set up project structure and core models

**Tasks:**
1. Create new directory structure
2. Set up virtual environment and requirements.txt:
   ```
   psycopg2-binary>=2.9.0
   pyyaml>=6.0
   jsonschema>=4.0
   python-dotenv>=1.0
   ```
3. Create config model classes (`models/config.py`):
   - `SourceConfig`
   - `EntityConfig`
   - `FieldMapping`
   - `LookupMapping`
   - `JunctionMapping`
4. Create entity model classes (`models/entity.py`):
   - `Entity`
   - `Field`
   - `Relationship`
5. Add config validation using jsonschema
6. Create unit tests for models

**Deliverables:**
- `/home/user/tcg-decklists/tools/data-pipeline/src/models/` with all model classes
- Tests passing
- Type hints throughout

### Phase 2: Extract Layer (Week 1-2)

**Goal:** Generic data extraction from multiple sources

**Tasks:**
1. Create abstract `BaseExtractor` class
2. Implement `GitExtractor`:
   - Clone repos
   - Check for updates via GitHub API
   - Extract specific paths
3. Implement `UrlExtractor`:
   - Download from HTTP/HTTPS
   - Support authentication
4. Implement `LocalExtractor`:
   - Read local files
5. Create version tracking system (`utils/version_tracker.py`):
   - Read/write metadata.json
   - Track source versions
   - Determine if updates needed
6. Add extraction tests

**Deliverables:**
- `/home/user/tcg-decklists/tools/data-pipeline/src/extract/` with all extractors
- Version tracking working
- Tests for each extractor

### Phase 3: Transform Library (Week 2)

**Goal:** Build reusable transformation functions

**Tasks:**
1. Create transform function registry (`transform/transforms.py`):
   - String transforms (lowercase, uppercase, trim)
   - Numeric parsing (parse_integer, parse_float)
   - Date parsing
   - Custom Pokemon transforms (parse_damage, extract_set_id)
2. Create transform engine (`transform/engine.py`):
   - Apply transforms based on config
   - Handle nullable values
   - Error handling
3. Create field mapper (`transform/mappers.py`):
   - Simple field mapping
   - Nested field extraction (images.small)
   - Array flattening
4. Create validators (`transform/validators.py`):
   - Required field validation
   - Type validation
   - Custom validation rules
5. Add transform tests

**Deliverables:**
- Transform function library with 10+ functions
- Transform engine that applies configs
- Comprehensive tests

### Phase 4: Load Layer - Part 1 (Week 3)

**Goal:** Basic database loading

**Tasks:**
1. Create database connection manager (`load/database.py`):
   - Connection pooling
   - Transaction management
   - Retry logic
2. Create basic entity loader:
   - Insert/upsert records
   - Handle conflicts
   - Batch processing
3. Create lookup table manager (`load/lookup_manager.py`):
   - Get or create pattern
   - Caching for performance
   - Track created vs existing
4. Add database tests (use test database)

**Deliverables:**
- Database loader with upsert capability
- Lookup manager working
- Integration tests

### Phase 5: Load Layer - Part 2 (Week 3-4)

**Goal:** Complex relationship handling

**Tasks:**
1. Create junction table manager (`load/junction_manager.py`):
   - Simple junctions (card → types)
   - Quantity tracking (retreat costs)
   - Clean old records before insert
2. Create nested entity handler:
   - Handle abilities, attacks, weaknesses
   - Deduplication logic
   - Sub-array handling (attack costs)
3. Create dependency resolver:
   - Ensure entities load in correct order
   - Handle circular dependencies (fail gracefully)
4. Add complex loading tests

**Deliverables:**
- Junction manager handling all patterns
- Nested entity support
- Dependency resolution working

### Phase 6: Orchestration (Week 4)

**Goal:** Main pipeline orchestration

**Tasks:**
1. Create main entry point (`main.py`):
   - CLI argument parsing
   - Game selection
   - Dry-run mode
2. Create pipeline orchestrator:
   - Read configs
   - Run extract phase
   - Run transform phase
   - Run load phase
   - Track progress
3. Add logging (`utils/logger.py`):
   - Structured logging
   - Progress reporting
   - Error tracking
4. Add error handling:
   - Rollback on failure
   - Partial success handling
   - Retry logic
5. Create end-to-end tests

**Deliverables:**
- Working `python main.py --game pokemon` command
- Comprehensive logging
- Full E2E test

### Phase 7: Pokemon Migration (Week 5)

**Goal:** Migrate Pokemon to new system

**Tasks:**
1. Create Pokemon config files:
   - `config/games/pokemon/sources.yaml`
   - `config/games/pokemon/entities.yaml`
   - `config/games/pokemon/sets.yaml`
   - `config/games/pokemon/cards.yaml`
2. Implement Pokemon-specific transforms:
   - `parse_damage_numeric`
   - `parse_damage_modifier`
   - `extract_set_id`
3. Test against real Pokemon data:
   - Run full migration
   - Compare results with old system
   - Fix discrepancies
4. Performance optimization:
   - Batch sizes
   - Connection pooling
   - Caching strategies
5. Documentation

**Deliverables:**
- Complete Pokemon configs
- Successful full migration
- Performance at least as good as current system
- Migration guide

### Phase 8: Cleanup & Documentation (Week 5-6)

**Goal:** Production-ready system

**Tasks:**
1. Remove old scripts:
   - Archive `run.sh`
   - Archive `_migrate_lib.py`
   - Archive `migrate-sets.py` and `migrate-cards.py`
2. Update main README.md
3. Create comprehensive docs:
   - Architecture overview
   - Configuration guide
   - Adding new games guide
   - Troubleshooting guide
4. Add CLI improvements:
   - `--dry-run` mode
   - `--game <name>` selection
   - `--entity <name>` to load specific entity
   - `--force` to ignore versions
5. Final testing:
   - Fresh database test
   - Update scenario test
   - Error scenario tests

**Deliverables:**
- Production-ready system
- Complete documentation
- Old code removed

---

## 5. Edge Cases & Solutions

### 5.1 Attack Deduplication with Cost Types

**Problem:** Attacks are unique by name + cost + damage + text + cost_types. Current code checks existing cost types from DB.

**Solution:**
```yaml
# In cards.yaml
nested_array_entities:
  - json_path: attacks
    entity_table: pokemon_attack
    uniqueness_includes_sub_array: cost
    # This tells the system to include the cost array in uniqueness check
```

**Implementation:**
- When checking if attack exists, also query `pokemon_attack_cost`
- Compare cost type arrays (sorted for order-independence)
- Only create new if no match

### 5.2 Dynamic Object Keys (Legalities)

**Problem:** `legalities: {unlimited: "Legal", standard: "Legal"}` has unknown keys.

**Solution:**
```yaml
dynamic_object_mapping:
  - json_path: legalities
    junction_table: pokemon_card_legality
    key_lookup_table: pokemon_format
    key_lookup_column: name
    value_column: status
```

**Implementation:**
- Iterate over object keys
- For each key, get/create in `pokemon_format`
- Insert junction record with key_id and value

### 5.3 Array Quantity Tracking

**Problem:** `retreatCost: ["Colorless", "Colorless", "Fire"]` needs to become quantities.

**Solution:**
```yaml
array_with_quantity:
  - json_path: retreatCost
    count_occurrences: true
```

**Implementation:**
- Use `Counter` to count occurrences
- Insert one junction record per unique type with quantity column

### 5.4 Nested Arrays within Nested Arrays

**Problem:** `attacks[].cost[]` - array within array of objects.

**Solution:**
```yaml
nested_array_entities:
  - json_path: attacks
    sub_array_with_quantity:
      - json_field: cost
        junction_table: pokemon_attack_cost
```

**Implementation:**
- First create/find attack entity
- Then process sub_array for that attack
- Use attack's ID as foreign key

### 5.5 Conditional Processing

**Problem:** Only process `ancientTrait` if it exists and has both name and text.

**Solution:**
```yaml
nested_entities:
  - json_path: ancientTrait
    required_sub_fields: [name, text]
```

**Implementation:**
- Check if path exists
- Check if all required_sub_fields present and non-null
- Skip if conditions not met

### 5.6 Multiple Source Files

**Problem:** Pokemon cards come from 100+ JSON files.

**Solution:**
```yaml
source:
  type: json_multi
  location: data/pokemon/cards/*.json
  consolidate: true
```

**Implementation:**
- Use glob to find files
- Load each JSON file
- Merge into single array
- Process as one entity

---

## 6. Configuration Examples

### 6.1 Simple Game: Yu-Gi-Oh Sets (Hypothetical)

```yaml
# config/games/yugioh/sets.yaml
entity: yugioh_sets
version: 1.0

source:
  type: json
  location: data/yugioh/sets.json
  is_array: true

table: yugioh_set

primary_key:
  field: set_code
  column: set_code
  type: text

strategy: upsert
conflict_target: set_code

fields:
  - json_path: set_code
    column: set_code
    type: text
    required: true

  - json_path: set_name
    column: name
    type: text

  - json_path: num_cards
    column: card_count
    type: integer

  - json_path: tcg_date
    column: release_date
    type: text
    transform: parse_date
```

### 6.2 Medium Complexity: Magic: The Gathering Cards (Hypothetical)

```yaml
# config/games/mtg/cards.yaml (simplified)
entity: mtg_cards
version: 1.0

source:
  type: json
  location: data/mtg/cards.json
  is_array: true

table: mtg_card

primary_key:
  field: id
  column: id
  type: text

fields:
  - json_path: name
    column: name
    type: text

  - json_path: mana_cost
    column: mana_cost
    type: text

  - json_path: cmc
    column: converted_mana_cost
    type: integer

lookups:
  - json_path: set
    column: set_id
    lookup_table: mtg_set
    lookup_column: code

array_junctions:
  - json_path: colors
    junction_table: mtg_card_color
    lookup_table: mtg_color
    lookup_column: name
    create_if_missing: true

  - json_path: types
    junction_table: mtg_card_type
    lookup_table: mtg_type
    lookup_column: name
    create_if_missing: true
```

---

## 7. Testing Strategy

### 7.1 Unit Tests

**Coverage:**
- Config parsing and validation
- Transform functions
- Field mapping logic
- Lookup table operations
- Junction table operations

**Example:**
```python
def test_parse_damage_numeric():
    assert transforms.parse_damage_numeric("50") == 50
    assert transforms.parse_damage_numeric("50+") == 50
    assert transforms.parse_damage_numeric("30×") == 30
    assert transforms.parse_damage_numeric("") is None
    assert transforms.parse_damage_numeric(None) is None
```

### 7.2 Integration Tests

**Coverage:**
- Database operations with test DB
- Full ETL for minimal dataset
- Error handling and rollback

**Example:**
```python
def test_load_simple_entity(test_db):
    config = {
        "table": "test_table",
        "fields": [{"json_path": "name", "column": "name"}]
    }
    data = [{"name": "Test"}]
    loader = EntityLoader(test_db, config)
    loader.load(data)

    result = test_db.query("SELECT name FROM test_table")
    assert result[0]['name'] == "Test"
```

### 7.3 End-to-End Tests

**Coverage:**
- Full Pokemon migration
- Version update scenario
- Multiple games scenario

**Example:**
```bash
# E2E test script
python main.py --game pokemon --dry-run
python main.py --game pokemon --entity sets
python main.py --game pokemon --entity cards
# Verify database state matches expected
```

---

## 8. Performance Considerations

### 8.1 Optimizations

1. **Batch Processing:**
   - Insert 500 records per transaction
   - Commit frequency configurable

2. **Lookup Caching:**
   - Cache lookup table results in memory
   - Reduces DB queries by ~90%

3. **Connection Pooling:**
   - Reuse DB connections
   - Configurable pool size

4. **Parallel Processing:**
   - Extract phase can run in parallel for multiple sources
   - Transform phase parallelizable per entity

5. **Incremental Updates:**
   - Version tracking prevents re-downloading
   - Smart upsert reduces duplicate work

### 8.2 Benchmarks

**Target Performance:**
- 100 sets: < 5 seconds
- 20,000 cards: < 2 minutes
- Full Pokemon migration: < 3 minutes

**Monitoring:**
- Log timing for each phase
- Track records processed per second
- Report slowest operations

---

## 9. Migration Path

### 9.1 Parallel Running

During development, both systems can coexist:

```
tools/data-pipeline/
├── run.sh                  # Old system
├── scripts/pokemon/        # Old scripts
├── main.py                 # New system
└── src/                    # New code
```

### 9.2 Validation

Before switching:

1. Run old system, capture database state
2. Clear database
3. Run new system
4. Compare database states
5. Investigate discrepancies
6. Repeat until identical

### 9.3 Cutover

Once validated:

1. Update CLAUDE.md with new commands
2. Archive old scripts to `scripts/archived/`
3. Update README.md
4. Announce change

---

## 10. Future Extensibility

### 10.1 Adding New Games

To add Yu-Gi-Oh:

1. Create `config/games/yugioh/` directory
2. Add `sources.yaml`, `entities.yaml`
3. Add entity configs (`sets.yaml`, `cards.yaml`, etc.)
4. Add game-specific transforms if needed
5. Run: `python main.py --game yugioh`

**No Python code changes required!**

### 10.2 New Data Sources

Current: Git, URL, Local
Future:
- REST APIs with pagination
- GraphQL APIs
- Database sources
- Cloud storage (S3, GCS)

Simply implement new extractor class, register it, use in config.

### 10.3 New Transform Types

Add to `transform/transforms.py`:

```python
@register_transform("parse_edition")
def parse_edition(value: str) -> Optional[str]:
    """Extract edition from string like '1st Edition'"""
    if not value:
        return None
    return value.replace(" Edition", "")
```

Use in config:

```yaml
transformed_fields:
  - json_path: edition
    column: edition_normalized
    transform: parse_edition
```

---

## 11. Risk Mitigation

### 11.1 Risks

1. **Scope Creep:** Config schema becomes too complex
   - *Mitigation:* Start simple, add features incrementally

2. **Performance Regression:** New system slower than old
   - *Mitigation:* Benchmark early, optimize as needed

3. **Data Loss:** Migration fails, corrupts data
   - *Mitigation:* Always run in transaction, test rollback

4. **Config Errors:** Invalid configs break pipeline
   - *Mitigation:* JSON schema validation, helpful error messages

5. **Incomplete Migration:** Missing edge cases from old code
   - *Mitigation:* Thorough testing, database comparison

### 11.2 Rollback Plan

If new system fails in production:

1. Keep old scripts until thoroughly validated
2. Database backups before migration
3. Quick revert: `git checkout old-branch`
4. Document rollback procedure

---

## 12. Success Criteria

The refactor is successful when:

1. **Functional:**
   - Pokemon migrates identically to old system
   - All 20,000+ cards load correctly
   - All relationships preserved

2. **Configurable:**
   - No Pokemon-specific code in `src/`
   - New game added via config only

3. **Performant:**
   - Migration time ≤ current system
   - Memory usage reasonable (< 2GB)

4. **Maintainable:**
   - Code coverage > 80%
   - Clear documentation
   - Type hints throughout

5. **Extensible:**
   - Easy to add new games
   - Easy to add new transforms
   - Easy to add new sources

---

## 13. Implementation Checklist

### Week 1
- [ ] Create directory structure
- [ ] Set up requirements.txt
- [ ] Create model classes
- [ ] Implement extractors
- [ ] Implement version tracking

### Week 2
- [ ] Create transform library
- [ ] Create transform engine
- [ ] Create field mappers
- [ ] Add validators

### Week 3
- [ ] Implement database loader
- [ ] Implement lookup manager
- [ ] Implement junction manager
- [ ] Add nested entity support

### Week 4
- [ ] Create main orchestrator
- [ ] Add logging
- [ ] Add error handling
- [ ] Create E2E tests

### Week 5
- [ ] Create Pokemon configs
- [ ] Test full Pokemon migration
- [ ] Performance tuning
- [ ] Documentation

### Week 6
- [ ] Remove old code
- [ ] Final testing
- [ ] Production deployment
- [ ] Monitor first run

---

## 14. Conclusion

This refactor transforms a Pokemon-specific ETL pipeline into a generic, config-driven system that can support any card game. The key innovations are:

1. **Separation of concerns** through distinct Extract, Transform, Load phases
2. **Configuration-driven** approach replacing hardcoded logic
3. **Reusable components** that work for any card game
4. **Declarative configs** that describe data mappings clearly
5. **Extensibility** for new games, sources, and transforms

The implementation will take approximately 5-6 weeks and result in a maintainable, testable, and extensible system that significantly reduces the effort to add new card games from "weeks of coding" to "hours of configuration."

---

## Appendix: Quick Start Guide for Implementation

### Getting Started

1. **Read this entire document** to understand the architecture
2. **Start with Phase 1** (Foundation) - don't skip ahead
3. **Write tests** as you go - test-driven development recommended
4. **Keep old system** until new system is validated
5. **Ask questions** when design decisions arise

### Key Files to Create First

1. `src/models/config.py` - Config data models
2. `src/extract/base.py` - Base extractor class
3. `src/transform/transforms.py` - Transform function registry
4. `src/load/database.py` - Database connection manager
5. `src/main.py` - Entry point

### Development Tips

- Use type hints everywhere
- Write docstrings for all public functions
- Use dataclasses for models
- Validate configs early
- Log liberally
- Test with small datasets first
- Benchmark frequently

### Questions to Resolve During Implementation

1. Should we use JSON or YAML for configs? (Recommendation: YAML for readability)
2. How to handle schema validation? (Recommendation: jsonschema library)
3. What Python version? (Recommendation: 3.12+ for best type hints)
4. Testing framework? (Recommendation: pytest)
5. Logging format? (Recommendation: structured JSON logs)

---

**Document Version:** 1.0
**Last Updated:** 2025-11-16
**Status:** Planning Phase
**Next Steps:** Begin Phase 1 - Foundation
