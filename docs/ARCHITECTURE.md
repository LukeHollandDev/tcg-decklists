# Architecture

## System Overview

TCG Decklists is a full-stack web application designed with multi-TCG extensibility in mind. The system consists of
three main parts:

1. **Backend** - Java Spring Boot REST API
2. **Frontend** - React 19 application
3. **Data Pipeline** - Python scripts for card data ingestion

## Backend Structure

The Spring Boot backend follows a standard layered architecture:

```
apps/backend/src/main/java/dev/lukeholland/tcg/decklists/api/
├── Application.java              # Spring Boot entry point
├── controllers/                  # REST endpoints
├── services/                     # Business logic layer
├── repositories/                 # JPA repositories for data access
├── entities/                     # JPA entities (database models)
│   └── pokemon/                  # Pokemon-specific entities
└── dto/                          # Data Transfer Objects
    └── pokemon/                  # Pokemon-specific DTOs
```

### API Design Philosophy

The backend API is designed with multi-TCG extensibility in mind. Endpoints use a `<type>` parameter (currently
`pokemon`) to allow future support for other card games like Yu-Gi-Oh.

**Planned Endpoints:**

- `GET /api/features/<type>` - Feature flags and available filters and site website features for a card game type
- `GET /api/search/<type>?<params>` - Search cards with filters
- `GET /api/card/<type>/<id>` - Get specific card details
- `POST /api/decklist` - Create a new decklist
- `GET /api/decklist/<id>` - Retrieve decklist with all card data
- `GET /api/templates/<type>` - Get available PDF template options

### Important Architectural Notes

- Entities use Jackson `@JsonBackReference` and `@JsonManagedReference` to prevent infinite recursion during JSON
  serialization of bidirectional relationships
- Database schema changes are managed through Liquibase (see `apps/backend/src/main/resources/db/changelog/`)
- Application properties support environment variable overrides for DB connection settings
- Code should avoid Pokémon-specific hardcoding where possible to support future TCG additions

## Frontend Structure

The frontend is a React 19 application using:

- **Vite** with Rolldown for build tooling
- **React Compiler** for automatic memoization
- **TailwindCSS v4** for styling
- **Biome** for linting and formatting (replaces ESLint)

### Planned UI Features

**Search Functionality**: Default text search across card name, attack names/descriptions, ability descriptions, and
trainer text

**Comprehensive Filters** organized into two categories:

- *Gameplay Filters*: Card Type (Pokémon/Trainer/Energy), Mechanic (ex/V/GX/EX), Label (Tera/Ancient/Future), Type (
  elemental types), Stage (Basic/Stage 1/Stage 2/VSTAR), Attributes (Ability/Rule Box/Weakness/Resistance)
- *Detail Filters*: Card Name, HP, Attack Cost/Text/Damage, Retreat Cost, Weakness, Resistance, Rarity, Artist, Set,
  Series, Appearance (Full Art/Alt Art/Shiny), Format, Regulation Mark

**Deck Export**: Generate PDFs using official Pokémon templates (processing may be client-side) and exports to clipboard
to support games like Pokémon TCG Live

### Filter-to-API Mapping

When implementing search filters, note that the source data JSON uses specific field names:

- Card Type filters → `supertype` and `subtype` fields
- Mechanic/Stage filters → `subtype` field (e.g., "ex", "V", "Stage 1")
- Type filters → `types` array field
- Most other filters map directly to their corresponding database tables

## Data Management

Card data is stored in `tools/data-pipeline/pokemon/` with many JSON files representing different card sets.

### Data Pipeline Flow

The data pipeline:

1. Is designed to run as a scheduled GitHub CI/CD job
2. Tracks upstream data version via commit hash in `metadata.json`
3. Only updates when new data is available
4. Uses Python scripts (`tools/data-pipeline/scripts/pokemon-migrate.py`) to transform and load data into PostgreSQL

**Schema generation**: If `genson` is installed, the pipeline generates JSON schemas in `tools/data-pipeline/schema/`

## Database Schema

The database is organized into two main areas:

### 1. User-Generated Content (Planned, Not Yet Implemented)

- `decklist` - Will store user-created decklists with metadata (id, type, dates, views)
- `decklist_card` - Junction table linking decklists to cards
- `template` - Custom decklist templates (future feature)

### 2. Pokemon Card Data (Implemented)

The schema is highly normalized to enable efficient querying and filtering across all card attributes.

#### Core Tables

- `pokemon_card` - Main card data with TEXT id, name, supertype, hp (TEXT + hp_numeric INT), converted_retreat_cost,
  number, flavor_text, image_low, image_high, regulation_mark, level
- `pokemon_set` - Card sets with set_id (extracted from card ID, e.g., "base1" from "base1-1") and optional name field
  for future metadata (many-to-one with cards)
- `pokemon_artist` - Card artists (many-to-one with cards)
- `pokemon_rarity` - Rarity values (many-to-one with cards)
- `pokemon_ancient_trait` - Ancient trait definitions with name and text (many-to-one with cards)

#### Many-to-Many Relationships

- `pokemon_subtype` + `pokemon_card_subtype` - Card subtypes (Stage 1, ex, V, etc.)
- `pokemon_type` + `pokemon_card_type` - Pokémon types (Fire, Water, etc.)
- `pokemon_pokedex` + `pokemon_card_pokedex` - National Pokedex numbers
- `pokemon_ability` + `pokemon_card_ability` - Abilities with name, text, and type
- `pokemon_format` + `pokemon_card_legality` - Format legality (uses `legality_status` enum: legal, illegal, banned,
  unlimited)
- `pokemon_attack` + `pokemon_card_attack` - Attacks with converted_cost, damage (TEXT + damage_numeric INT),
  damage_modifier, and text
- `pokemon_attack_cost` - Links attacks to required energy types (uses surrogate PK to allow duplicate type entries
  like "2x Colorless")
- `pokemon_name` + `pokemon_card_evolution` - Evolution chains with `evolution_direction` enum (from, to)
- `pokemon_resistance` + `pokemon_card_resistance` - Resistances with type and value
- `pokemon_rule` + `pokemon_card_rule` - Card rules text
- `pokemon_weakness` + `pokemon_card_weakness` - Weaknesses with type and value
- `pokemon_card_retreat_cost` - Links cards to retreat cost types (no primary key)

#### Key Schema Decisions

- Card IDs are TEXT (matches a source data format like "xy1-1"), with set_id extracted as the portion before the final
  hyphen
- `pokemon_name` is separate from `pokemon_pokedex` to handle special names like "Brock's Vulpix"
- `pokemon_set.set_id` stores the extracted set identifier (e.g., base1, swsh8) and is populated automatically by
  the migration script
- `pokemon_card_evolution` uses direction enum to track both "evolves from" and "evolves to" in one table
- Numeric fields (hp_numeric, damage_numeric) are pre-converted from strings for faster filtering/sorting
- Attack costs stored through `pokemon_attack_cost` with surrogate PK (e.g., one Fire + two Colorless requires 3 rows)
- **Attack Uniqueness**: Attacks are considered unique based on name, converted_cost, damage, text, AND cost types. This
  ensures "Tackle" with [Grass] is different from "Tackle" with [Fire] or [Colorless, Colorless]
- Two PostgreSQL enums: `legality_status` and `evolution_direction`
- ON DELETE CASCADE used for junction tables to maintain referential integrity
- ON DELETE SET NULL used for nullable foreign keys (set_id, artist_id, etc.)

Liquibase migrations are in `apps/backend/src/main/resources/db/changelog/changes/001-create-pokemon-card-tables/`.

## Technology Choices

### Why Java 21 + Spring Boot?

- Mature ecosystem for REST APIs
- Excellent database integration with JPA/Hibernate
- Strong typing and compile-time checks
- Familiar to many enterprise developers

### Why React 19?

- Latest React features including the React Compiler
- Automatic memoization reduces manual optimization
- Strong TypeScript integration
- Large ecosystem of libraries

### Why PostgreSQL?

- Robust support for complex relationships
- JSONB for potential future flexibility
- Excellent indexing capabilities
- Strong ACID guarantees for user data

### Why Self-Hosted Data?

Originally planned to use `pokemontcg.io` API, but it switched to a paid model. Self-hosting provides:

- No rate limits
- No API costs
- Complete control over data freshness
- Ability to add custom metadata
- Offline development capability

## Security Considerations

- Database credentials via environment variables
- Prepared statements (via JPA) prevent SQL injection
- CORS configuration for API access
