# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TCG Decklists is a Pokemon TCG deck builder and viewer web application. Users can import decklists (e.g., from PTCGL format), view them interactively with filters, search for cards, build custom decklists, and export them as PDFs or shareable images using official Pokemon templates. The project is designed to be extensible to other TCG games in the future.

**Key Features:**
- Import decklists in standard format (PTCGL)
- Interactive deck viewer with card filters
- Card search with comprehensive filtering options
- Build and export custom decklists
- Share decklists via unique URLs (e.g., `lukeholland.dev/tcg-decklists/<id>[.jpeg]`)
- Export PDFs using official Pokemon templates

**Technology Stack:**
- **Backend**: Java 21 Spring Boot REST API with PostgreSQL database
- **Frontend**: React 19 + TypeScript with Vite and TailwindCSS v4
- **Data Pipeline**: Self-hosted card data (originally planned to use pokemontcg.io API, but switched to self-hosting due to API becoming paid)

**Why Self-Hosted Data:**
The project initially planned to use the pokemontcg.io API with caching, but when that API switched to a paid model, the architecture pivoted to cloning and self-hosting card data from https://github.com/PokemonTCG/pokemon-tcg-data. This provides complete control over the data without rate limits or API costs.

## Development Commands

### Backend (Spring Boot)

```bash
cd backend

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run a single test
./gradlew test --tests "fully.qualified.TestClassName"

# Run the application (requires PostgreSQL running)
./gradlew bootRun

# Clean build artifacts
./gradlew clean
```

### Frontend (React + Vite)

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run Biome linter
npm run lint

# Format code with Biome
npm run format

# Run Biome check (lint + format)
npm run check
```

### Database

```bash
# Start PostgreSQL database only
docker compose up database -d

# Stop database
docker compose down

# View database logs
docker compose logs -f database
```

Database connection details:
- Host: localhost:5432
- Database: tcg_decklists
- Username: postgres (default, override with DB_USER env var)
- Password: testing1234 (default, override with DB_PASSWORD env var)

### Data Pipeline

```bash
cd data

# Run the data ingestion pipeline (fetches latest card data and migrates to database)
./run.sh
```

The pipeline:
1. Checks metadata.json for current data version
2. Fetches latest commit from upstream Pokemon TCG data repository
3. Clones new data if version differs
4. Runs Python migration script to populate database
5. Updates metadata.json with new version and timestamp

## Architecture

### Backend Structure

The Spring Boot backend follows a standard layered architecture:

```
backend/src/main/java/dev/lukeholland/tcg/decklists/api/
├── Application.java              # Spring Boot entry point
├── controllers/                  # REST endpoints
├── services/                     # Business logic layer
├── repositories/                 # JPA repositories for data access
├── entities/                     # JPA entities (database models)
│   └── pokemon/                  # Pokemon-specific entities
└── dto/                          # Data Transfer Objects
    └── pokemon/                  # Pokemon-specific DTOs
```

**API Design Philosophy:**
The backend API is designed with multi-TCG extensibility in mind. Endpoints use a `<type>` parameter (currently `pokemon`) to allow future support for other card games like Yu-Gi-Oh. Key planned endpoints:
- `GET /api/features/<type>` - Feature flags and available filters for a card game type
- `GET /api/search/<type>?<params>` - Search cards with filters
- `GET /api/card/<type>/<id>` - Get specific card details
- `POST /api/decklist` - Create a new decklist
- `GET /api/decklist/<id>` - Retrieve decklist with all card data
- `GET /api/templates/<type>` - Get available PDF template options

**Important architectural notes:**
- Entities use Jackson `@JsonBackReference` and `@JsonManagedReference` to prevent infinite recursion during JSON serialization of bidirectional relationships
- Database schema changes are managed through Liquibase (see `backend/src/main/resources/db/changelog/`)
- Application properties support environment variable overrides for DB connection settings
- Code should avoid Pokemon-specific hardcoding where possible to support future TCG additions

### Frontend Structure

The frontend is a React 19 application using:
- **Vite** with Rolldown for build tooling
- **React Compiler** for automatic memoization
- **TailwindCSS v4** for styling
- **Biome** for linting and formatting (replaces ESLint)

**Planned UI Features:**
- **Search Functionality**: Default text search across card name, attack names/descriptions, ability descriptions, and trainer text
- **Comprehensive Filters** organized into two categories:
  - *Gameplay Filters*: Card Type (Pokemon/Trainer/Energy), Mechanic (ex/V/GX/EX), Label (Tera/Ancient/Future), Type (elemental types), Stage (Basic/Stage 1/Stage 2/VSTAR), Attributes (Ability/Rule Box/Weakness/Resistance)
  - *Detail Filters*: Card Name, HP, Attack Cost/Text/Damage, Retreat Cost, Weakness, Resistance, Rarity, Artist, Set, Series, Appearance (Full Art/Alt Art/Shiny), Format, Regulation Mark
- **Deck Export**: Generate PDFs using official Pokemon templates (processing may be client-side)

**Filter-to-API Mapping:**
When implementing search filters, note that the source data JSON uses specific field names:
- Card Type filters → `supertype` and `subtype` fields
- Mechanic/Stage filters → `subtype` field (e.g., "ex", "V", "Stage 1")
- Type filters → `types` array field
- Most other filters map directly to their corresponding database tables

### Data Management

Card data is stored in `data/pokemon/` with 169 JSON files representing different card sets. The data pipeline:
1. Is designed to run as a scheduled GitLab CI/CD job
2. Tracks upstream data version via commit hash in `metadata.json`
3. Only updates when new data is available
4. Uses Python scripts (`data/scripts/pokemon-migrate.py`) to transform and load data into PostgreSQL

**Schema generation**: If `genson` is installed, the pipeline generates JSON schemas in `data/schema/`

### Database Schema

The database is organized into two main areas:

**1. User-Generated Content (planned, not yet implemented):**
- `decklist` - Will store user-created decklists with metadata (id, type, dates, views)
- `decklist_card` - Junction table linking decklists to cards
- `template` - Custom decklist templates (future feature)

**2. Pokemon Card Data (implemented):**
The schema is highly normalized to enable efficient querying and filtering across all card attributes.

**Core Tables:**
- `pokemon_card` - Main card data with TEXT id, name, supertype, hp (TEXT + hp_numeric INT), converted_retreat_cost, number, flavor_text, image_low, image_high, regulation_mark, level
- `pokemon_set` - Card sets (many-to-one with cards)
- `pokemon_artist` - Card artists (many-to-one with cards)
- `pokemon_rarity` - Rarity values (many-to-one with cards)
- `pokemon_ancient_trait` - Ancient trait definitions with name and text (many-to-one with cards)

**Many-to-Many Relationships:**
- `pokemon_subtype` + `pokemon_card_subtype` - Card subtypes (Stage 1, ex, V, etc.)
- `pokemon_type` + `pokemon_card_type` - Pokemon types (Fire, Water, etc.)
- `pokemon_pokedex` + `pokemon_card_pokedex` - National Pokedex numbers
- `pokemon_ability` + `pokemon_card_ability` - Abilities with name, text, and type
- `pokemon_format` + `pokemon_card_legality` - Format legality (uses `legality_status` enum: legal, illegal, banned, unlimited)
- `pokemon_attack` + `pokemon_card_attack` - Attacks with converted_cost, damage (TEXT + damage_numeric INT), damage_modifier, and text
- `pokemon_attack_cost` - Links attacks to required energy types (no primary key, allows duplicates)
- `pokemon_name` + `pokemon_card_evolution` - Evolution chains with `evolution_direction` enum (from, to)
- `pokemon_resistance` + `pokemon_card_resistance` - Resistances with type and value
- `pokemon_rule` + `pokemon_card_rule` - Card rules text
- `pokemon_weakness` + `pokemon_card_weakness` - Weaknesses with type and value
- `pokemon_card_retreat_cost` - Links cards to retreat cost types (no primary key)

**Key Schema Decisions:**
- Card IDs are TEXT (matches source data format like "xy1-1")
- `pokemon_name` is separate from `pokemon_pokedex` to handle special names like "Brock's Vulpix"
- `pokemon_card_evolution` uses direction enum to track both "evolves from" and "evolves to" in one table
- Numeric fields (hp_numeric, damage_numeric) are pre-converted from strings for faster filtering/sorting
- Attack costs stored as many-to-many through `pokemon_attack_cost` (e.g., one Fire + two Colorless)
- Two PostgreSQL enums: `legality_status` and `evolution_direction`
- ON DELETE CASCADE used for junction tables to maintain referential integrity
- ON DELETE SET NULL used for nullable foreign keys (set_id, artist_id, etc.)

Liquibase migrations are in `backend/src/main/resources/db/changelog/changes/001-create-pokemon-card-tables/` (11 numbered SQL files)

## Testing

The project uses JUnit for backend tests located in `backend/src/test/java/`.

## API Testing

Bruno collection for API testing is available in `api-requests/` directory.

## Task Tracking

Outstanding and completed development tasks are tracked in `TODO.md` at the repository root, organized by category (Features, Bugs, Refactoring, Documentation, Testing).
