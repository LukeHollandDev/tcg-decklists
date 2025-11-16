# New Config-Driven ETL Pipeline

This is the refactored, config-driven ETL pipeline for TCG card data. It replaces the old bash/Python hybrid system with a pure Python, configuration-driven approach.

## Quick Start

### Prerequisites

```bash
# Install dependencies
pip install -r requirements.txt
```

### Running the Pipeline

```bash
# Run the complete pipeline for Pokemon
python -m src.main --game pokemon

# Dry run (extract only, no database operations)
python -m src.main --game pokemon --dry-run

# Verbose logging
python -m src.main --game pokemon --verbose
```

## Architecture

### Directory Structure

```
tools/data-pipeline/
├── config/
│   └── games/
│       └── pokemon/
│           ├── sources.yaml       # Data source definitions
│           ├── entities.yaml      # Entity dependencies
│           ├── sets.yaml          # Set entity mapping
│           └── cards.yaml         # Card entity mapping
├── src/
│   ├── main.py                    # Entry point & orchestration
│   ├── extract/                   # Data extraction layer
│   │   ├── base.py
│   │   ├── git.py
│   │   └── local.py
│   ├── transform/                 # Data transformation layer
│   │   ├── transforms.py
│   │   └── mappers.py
│   ├── load/                      # Data loading layer
│   │   ├── database.py
│   │   ├── lookup_manager.py
│   │   └── junction_manager.py
│   ├── models/                    # Data models
│   │   ├── config.py
│   │   └── entity.py
│   └── utils/                     # Utilities
│       ├── logger.py
│       ├── version_tracker.py
│       └── config_loader.py
└── metadata.json                  # Version tracking (auto-updated)
```

### ETL Flow

1. **Extract**: Download data from Git repositories
   - Checks for updates via GitHub API
   - Only downloads if version changed
   - Tracks versions in metadata.json

2. **Transform**: Map JSON to database schema
   - Applies field mappings from YAML config
   - Runs transformation functions (parse_integer, extract_set_id, etc.)
   - Handles nested objects and arrays

3. **Load**: Insert/upsert into database
   - Respects entity dependencies (sets before cards)
   - Manages lookup tables with caching
   - Handles junction tables for many-to-many relationships

## Configuration Format

### sources.yaml

Defines where to get data:

```yaml
game: pokemon
version: 1.0

sources:
  - name: pokemon-tcg-data
    type: git
    url: https://github.com/PokemonTCG/pokemon-tcg-data
    update_check: github_commits
    outputs:
      - name: sets
        path: sets
        destination: data/pokemon/sets
```

### entities.yaml

Defines loading order and dependencies:

```yaml
game: pokemon
version: 1.0

entities:
  - name: sets
    config: sets.yaml
    dependencies: []

  - name: cards
    config: cards.yaml
    dependencies:
      - sets  # Cards load after sets
```

### Entity Config (sets.yaml, cards.yaml)

Defines JSON → Database mapping:

```yaml
entity: pokemon_sets
version: 1.0

source:
  type: json
  location: data/pokemon/sets/en.json
  is_array: true

table: pokemon_set

primary_key:
  field: id
  column: set_id
  type: text

strategy: upsert
conflict_target: set_id

fields:
  - json_path: name
    column: name
    type: text

nested_fields:
  - json_path: images.small
    column: image_small
    type: text

transformed_fields:
  - json_path: hp
    column: hp_numeric
    type: integer
    transform: parse_integer
```

## Transform Functions

Available transforms:

- **String**: `lowercase`, `uppercase`, `trim`
- **Numeric**: `parse_integer`, `parse_float`
- **Pokemon-specific**: `parse_damage_numeric`, `extract_set_id`
- **Date**: `parse_date`, `parse_datetime`

## Adding a New Card Game

1. Create config directory: `config/games/yourgame/`
2. Create `sources.yaml` defining data sources
3. Create `entities.yaml` defining entities and dependencies
4. Create entity config files (e.g., `sets.yaml`, `cards.yaml`)
5. Add game-specific transforms if needed
6. Run: `python -m src.main --game yourgame`

**No Python code changes required!**

## Current Status

### ✅ Implemented

- Complete Extract layer with Git and local file support
- Transform library with 15+ functions
- Load layer with database, lookup, and junction management
- Main orchestration with CLI
- Pokemon configuration for basic fields
- Version tracking and incremental updates
- Lookup caching for performance
- Transaction management with rollback
- Progress tracking and logging

### 🚧 TODO

- Nested array entities (attacks, abilities, weaknesses, resistances)
- Evolution mappings (evolvesFrom/evolvesTo)
- Ancient trait handling
- Complex entity deduplication (attacks by name+cost+damage+types)
- Unit tests
- Integration tests
- Performance optimization
- Complete documentation

## Environment Variables

```bash
PGHOST=localhost          # Database host
PGPORT=5432              # Database port
PGDATABASE=tcg_decklists # Database name
PGUSER=postgres          # Database user
PGPASSWORD=testing1234   # Database password
```

## Performance

Target performance (to be benchmarked):
- 100 sets: < 5 seconds
- 20,000 cards: < 2 minutes
- Full Pokemon migration: < 3 minutes

Optimizations:
- Lookup caching (90%+ hit rate expected)
- Batch processing (500 records per transaction)
- Connection pooling
- Incremental updates (version tracking)

## Troubleshooting

### "ModuleNotFoundError: No module named 'src'"

Run from the data-pipeline directory:
```bash
cd tools/data-pipeline
python -m src.main --game pokemon
```

### "Config file not found"

Ensure config files exist at:
- `config/games/pokemon/sources.yaml`
- `config/games/pokemon/entities.yaml`
- `config/games/pokemon/sets.yaml`
- `config/games/pokemon/cards.yaml`

### Database connection failed

Check environment variables and ensure PostgreSQL is running:
```bash
docker compose -f config/docker-compose.yml up -d
```

## Migration from Old System

The old system (`run.sh`, `scripts/pokemon/`) is still in place and can run in parallel.

Once the new system is validated:
1. Archive old scripts to `scripts/archived/`
2. Update CLAUDE.md with new commands
3. Remove old code

## Contributing

See REFACTOR_PLAN.md for the full implementation plan and architecture details.
