# TCG Decklists API Testing

Bruno API testing collection for the TCG Decklists API. These tests validate that the API correctly transforms and
serves card data from the source JSON files.

## Overview

This collection uses [Bruno](https://www.usebruno.com/) to test API endpoints. The tests compare API responses against
source data from `tools/data-pipeline/data/pokemon/cards/` to ensure data integrity and correct transformation.

## Prerequisites

- [Bruno](https://www.usebruno.com/) installed
- Backend server running on `http://localhost:8080`
- Card data loaded into the database (see `tools/data-pipeline/README.md`)

## Getting Started

1. Open Bruno
2. Load this collection: `File > Open Collection` and select `tools/api-testing`
3. Select the "Local" environment (should be active by default)
4. Run individual requests or the entire collection

## Collection Structure

### Shared Validation Library

The `lib/` directory contains shared validation logic used by both Bruno tests and the validation script:

```
lib/
├── index.js         # Main entry point, exports all functions
├── load-data.js     # Pokemon card data loading functions
├── validators.js    # Card validation logic (required/optional fields)
└── utils.js         # Utility functions (sorting, comparison, normalization)
```

**Benefits of the shared library:**

- Single source of truth for validation logic
- Consistent behavior between Bruno tests and CLI validation
- Easier to maintain and update
- Well-documented and testable

**Usage example:**

```javascript
// In Bruno tests or scripts
const {loadPokemonSourceData, validateCard} = require('./lib');

const sourceData = loadPokemonSourceData();
const errors = validateCard(apiCard, sourceData['base1-1']);
```

### Environments

- **Local** - Points to `http://localhost:8080/api` for local development

### Folders

#### Pokémon Card

Contains requests for Pokemon card endpoints.

**Folder-level Setup (`folder.bru`):**

- Imports shared validation library (`lib/load-data.js`)
- Provides `getPokemonSourceData()` function to child requests

**Requests:**

##### By ID (`By ID.bru`)

- **Endpoint:** `GET /api/pokemon/:id`
- **Description:** Retrieves a single Pokémon card by its ID
- **Current Test ID:** `svp-78` (can be changed in path params)
- **Testing:** Uses `getValidationTests()` from `lib/validators.js` to generate individual test cases, showing each
  field validation as a separate passing/failing test in Bruno

**Test Coverage:**

The test validates both required and optional fields:

**Required Fields:**

- `id` - Card identifier
- `name` - Card name
- `number` - Card number in set
- `supertype` - Pokemon/Trainer/Energy
- `images` - Small and large image URLs
- `legalities` - Format legality (case-insensitive comparison)

**Optional Fields (tested if present in source data):**

- `subtypes` - Card subtypes (Basic, Stage 1, ex, etc.)
- `hp` - Hit points
- `types` - Pokemon types (Fire, Water, etc.)
- `evolvesFrom` - Evolution source
- `evolvesTo` - Evolution targets
- `attacks` - Attack details (name, cost, damage, text)
- `weaknesses` - Weakness types and values
- `resistances` - Resistance types and values
- `retreatCost` - Retreat cost types
- `convertedRetreatCost` - Numeric retreat cost
- `artist` - Card artist name
- `rarity` - Card rarity
- `flavorText` - Flavor text
- `pokedexNumbers` - National Pokedex numbers
- `regulationMark` - Regulation mark
- `abilities` - Abilities (name, text, type)
- `rules` - Card rules text
- `level` - Pokémon level
- `ancientTrait` - Ancient trait (name and text)

##### Search (`Search.bru`)

- **Endpoint:** `GET /api/pokemon/search`
- **Description:** Search for Pokémon cards using various filters
- **Testing:** Provides a comprehensive set of filter parameters for testing search functionality

**Available Search Filters:**

The search endpoint supports numerous filters that can be combined:

**Core Filters:**
- `name` - Card name (partial match)
- `supertype` - Pokemon/Trainer/Energy
- `types` - Pokemon types (supports multiple values)
- `typesMatchAll` - Require ALL specified types (AND logic)
- `subtypes` - Card subtypes
- `subtypesMatchAll` - Require ALL specified subtypes (AND logic)
- `setId` - Set identifier
- `rarity` - Rarity name
- `hpMin`, `hpMax` - HP range

**Generic Search (NEW):**
- `q` - Generic search term that searches across multiple fields simultaneously
- `excludeName` - Exclude name from generic search
- `excludeAttacks` - Exclude attack names/text from generic search
- `excludeAbilities` - Exclude ability names/text from generic search
- `excludeRules` - Exclude rule text from generic search
- `excludeArtist` - Exclude artist name from generic search

**Pagination & Sorting:**
- `page` - Page number (0-indexed)
- `pageSize` - Results per page (max 100)
- `sortBy` - Field to sort by
- `sortOrder` - Sort order (asc/desc)

##### Generic Search (`Generic Search.bru`)

- **Endpoint:** `GET /api/pokemon/search`
- **Description:** Dedicated request for testing the generic search feature
- **Features:**
  - Search across multiple fields at once (name, attacks, abilities, rules, artist)
  - Case-insensitive and accent-insensitive matching
  - Field exclusion toggles for focused searches
  - Combines with all other filters

**Generic Search Examples:**
```
# Basic search
q=pikachu

# Search excluding artist field
q=thunderbolt&excludeArtist=true

# Search only in attacks (exclude all other fields)
q=damage&excludeName=true&excludeAbilities=true&excludeRules=true&excludeArtist=true

# Combined with other filters
q=charizard&types=Fire&rarity=Rare
```

## Running Tests

### Run a Single Request

1. Select the request in Bruno
2. Click "Send" or press `Ctrl+Enter`
3. View test results in the "Tests" tab

### Run All Tests in a Folder

1. Right-click on the folder (e.g., "Pokémon Card")
2. Select "Run All Requests"

### View Test Results

Test results appear in the "Tests" tab of each request:

- Green checkmarks indicate passing tests
- Red X marks indicate failures with detailed error messages

## Test Data Source

Tests compare API responses against source JSON files in:

```
tools/data-pipeline/data/pokemon/cards/*.json
```

The `loadPokemonSourceData()` function:

1. Reads all JSON files from the Pokémon directory
2. Parses each file as an array of card objects
3. Creates a map indexed by card ID for fast lookup

## Validation Script

The `validate-all-cards.js` script allows you to validate all cards (or a subset) in batch mode. This is useful for:

- Regression testing after API changes
- Verifying data migrations
- Continuous integration testing
- Quickly identifying cards with data issues

### Usage

```bash
# Validate all cards
node validate-all-cards.js

# Validate with options
node validate-all-cards.js --limit=100

# Validate a specific card
node validate-all-cards.js --card-id=base1-1

# Use custom API URL
node validate-all-cards.js --base-url=http://localhost:3000/api

# Control concurrency (default: 10)
node validate-all-cards.js --concurrency=20
```

### Options

- `--base-url=URL` - Base API URL (default: `http://localhost:8080/api`)
- `--limit=N` - Only validate first N cards (useful for quick testing)
- `--card-id=ID` - Validate only a specific card ID
- `--concurrency=N` - Number of concurrent requests (default: 10)
- `--help` - Show help message

### Output

The script provides:

- Real-time progress updates
- Detailed error messages for failures
- Summary statistics at the end
- Exit code 0 for success, 1 for failures

## Maintaining the Validation Library

### Adding New Validations

When adding support for new card fields or validation logic:

1. **Update validators** (`lib/validators.js`):
    - Add validation logic to `validateRequiredFields()` or `validateOptionalFields()`
    - Ensure consistent error message format

2. **Add utilities if needed** (`lib/utils.js`):
    - Add normalization or comparison functions
    - Export new functions in `module.exports`

3. **Test changes**:
    - Changes automatically apply to both Bruno tests and the validation script
    - Run a Bruno test to verify
    - Run `node validate-all-cards.js --limit=10` to verify CLI

### Library Module Reference

**`lib/load-data.js`:**

- `loadPokemonSourceData(basePath?)` - Returns map of card ID → card data
- `loadPokemonSourceDataAsArray(basePath?)` - Returns array of card data

**`lib/validators.js`:**

- `validateRequiredFields(apiCard, sourceCard)` - Returns array of errors for required fields
- `validateOptionalFields(apiCard, sourceCard)` - Returns array of errors for optional fields
- `validateCard(apiCard, sourceCard)` - Returns combined array of all errors (for CLI use)
- `getValidationTests(apiCard, sourceCard, expect)` - Returns array of test objects for Bruno (each test shows
  individually)

**`lib/utils.js`:**

- `sortByName(arr)` - Sorts array by name property
- `sortByType(arr)` - Sorts array by type property (for weaknesses/resistances)
- `normalizeAttack(attack)` - Normalizes attack object for comparison
- `normalizeForComparison(value)` - Recursively normalizes values by sorting object keys
- `deepEqual(a, b)` - Deep equality check that ignores object key order
- `arraysHaveSameMembers(arr1, arr2)` - Order-independent array comparison

## Notes

- Tests use Bruno's built-in Chai assertions (`expect`)
- The collection allows filesystem access to read source data files
- All validation logic is centralized in the `lib/` directory for consistency
- Tests normalize data structures to avoid false failures due to ordering differences:
    - Object keys are sorted recursively before comparison
    - Arrays of objects (weaknesses, resistances) are sorted by type
    - Arrays of primitives are sorted before comparison
    - Attack costs and other order-independent arrays use member comparison
