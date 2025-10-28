# API Documentation

The TCG Decklists API is a RESTful API built with Spring Boot. All endpoints return JSON.

## Base URL

```
http://localhost:8080/api
```

## API Structure

The API uses feature-based routing where each card game type (e.g., `pokemon`) has its own base path under
`/api/{type}/`. This allows for clean organization and easy extensibility to support additional card games in the
future.

### Current Endpoints

#### Get Card by ID

```http
GET /api/pokemon/{id}
```

**Parameters:**

- `id` (path) - Card identifier (e.g., `base1-1`)

**Response:**

```json
{
  "id": "base1-1",
  "name": "Alakazam",
  "supertype": "Pokémon",
  "hp": "80",
  "types": [
    "Psychic"
  ],
  "attacks": [
    "..."
  ],
  "weaknesses": [
    "..."
  ],
  "resistances": [
    "..."
  ],
  "retreatCost": [
    "Colorless",
    "Colorless",
    "Colorless"
  ],
  "set": "base1",
  "number": "1",
  "artist": "Ken Sugimori",
  "rarity": "Rare Holo",
  "images": {
    "small": "https://...",
    "large": "https://..."
  }
}
```

#### Check Card Exists

```http
HEAD /api/pokemon/{id}
```

**Parameters:**

- `id` (path) - Card identifier (e.g., `base1-1`)

**Response:**

- `200 OK` - Card exists
- `404 Not Found` - Card does not exist

#### Search Cards

```http
GET /api/pokemon/search?name=alakazam&hpMin=80&types=psychic
```

**Query Parameters (Phase 1 - Core Filters):**

| Parameter          | Type     | Description                                    | Example                  |
|--------------------|----------|------------------------------------------------|--------------------------|
| `name`             | String   | Card name (partial match, accent-insensitive)  | `name=Pikachu`           |
| `supertype`        | String   | Card supertype (Pokémon, Trainer, Energy)      | `supertype=Pokemon`      |
| `types`            | String[] | Pokémon types (Fire, Water, etc.) - ANY match  | `types=Fire&types=Water` |
| `typesMatchAll`    | Boolean  | If true, match ALL types (AND logic)           | `typesMatchAll=true`     |
| `subtypes`         | String[] | Card subtypes (ex, V, Basic, etc.) - ANY match | `subtypes=ex&subtypes=V` |
| `subtypesMatchAll` | Boolean  | If true, match ALL subtypes (AND logic)        | `subtypesMatchAll=true`  |
| `setId`            | String   | Set identifier                                 | `setId=base1`            |
| `rarity`           | String   | Rarity name                                    | `rarity=Rare`            |
| `hpMin`            | Integer  | Minimum HP (inclusive)                         | `hpMin=80`               |
| `hpMax`            | Integer  | Maximum HP (inclusive)                         | `hpMax=120`              |

**Query Parameters (Phase 2 - Attack Filters):**

| Parameter            | Type     | Description                                     | Example                              |
|----------------------|----------|-------------------------------------------------|--------------------------------------|
| `attackName`         | String   | Attack name (partial match, accent-insensitive) | `attackName=Thunderbolt`             |
| `attackDamageMin`    | Integer  | Minimum attack damage (inclusive)               | `attackDamageMin=100`                |
| `attackDamageMax`    | Integer  | Maximum attack damage (inclusive)               | `attackDamageMax=200`                |
| `attackCost`         | String[] | Attack cost types - ANY match                   | `attackCost=Fire&attackCost=Colorle` |
| `attackCostMatchAll` | Boolean  | If true, match ALL cost types (AND logic)       | `attackCostMatchAll=true`            |

**Query Parameters (Phase 2 - Ability Filters):**

| Parameter     | Type    | Description                                      | Example                  |
|---------------|---------|--------------------------------------------------|--------------------------|
| `hasAbility`  | Boolean | Filter by ability presence (true/false)          | `hasAbility=true`        |
| `abilityName` | String  | Ability name (partial match, accent-insensitive) | `abilityName=Intimidate` |

**Query Parameters (Phase 2 - Detail Filters):**

| Parameter         | Type     | Description                                   | Example                             |
|-------------------|----------|-----------------------------------------------|-------------------------------------|
| `artist`          | String   | Artist name (exact match, accent-insensitive) | `artist=Ken Sugimori`               |
| `regulationMark`  | String   | Regulation mark (A, B, C, D, E, F, G, H)      | `regulationMark=E`                  |
| `retreatCostMin`  | Integer  | Minimum retreat cost (inclusive)              | `retreatCostMin=1`                  |
| `retreatCostMax`  | Integer  | Maximum retreat cost (inclusive)              | `retreatCostMax=3`                  |
| `formats`         | String[] | Format legality - ANY match                   | `formats=Standard&formats=Expanded` |
| `formatsMatchAll` | Boolean  | If true, legal in ALL formats (AND logic)     | `formatsMatchAll=true`              |

**Pagination & Sorting:**

| Parameter   | Type    | Description                                  | Example            |
|-------------|---------|----------------------------------------------|--------------------|
| `page`      | Integer | Page number (0-indexed, default: 0)          | `page=2`           |
| `pageSize`  | Integer | Results per page (default: 20, max: 100)     | `pageSize=50`      |
| `sortBy`    | String  | Sort field (default: "name")                 | `sortBy=hpNumeric` |
| `sortOrder` | String  | Sort order: "asc" or "desc" (default: "asc") | `sortOrder=desc`   |

**Example Requests (Phase 1):**

```http
# Simple name search
GET /api/pokemon/search?name=Charizard

# Fire-type cards with HP >= 100
GET /api/pokemon/search?types=Fire&hpMin=100

# ex cards from base set, sorted by HP descending
GET /api/pokemon/search?subtypes=ex&setId=base1&sortBy=hpNumeric&sortOrder=desc

# Multiple types (Fire OR Water)
GET /api/pokemon/search?types=Fire&types=Water

# Cards with BOTH Fire AND Grass types
GET /api/pokemon/search?types=Fire&types=Grass&typesMatchAll=true

# All cards (paginated)
GET /api/pokemon/search?page=0&pageSize=20
```

**Example Requests (Phase 2 - Attack Filters):**

```http
# Cards with "Thunderbolt" attack
GET /api/pokemon/search?attackName=Thunderbolt

# High-damage attacks (100+ damage)
GET /api/pokemon/search?attackDamageMin=100

# Attacks requiring Fire OR Colorless energy
GET /api/pokemon/search?attackCost=Fire&attackCost=Colorless

# Attacks requiring BOTH Fire AND Grass energy
GET /api/pokemon/search?attackCost=Fire&attackCost=Grass&attackCostMatchAll=true
```

**Example Requests (Phase 2 - Ability & Detail Filters):**

```http
# Only cards with abilities
GET /api/pokemon/search?hasAbility=true

# Cards with "Intimidate" ability
GET /api/pokemon/search?abilityName=Intimidate

# Cards by Ken Sugimori
GET /api/pokemon/search?artist=Ken Sugimori

# Cards with regulation mark E
GET /api/pokemon/search?regulationMark=E

# Low retreat cost cards (0-1 energy)
GET /api/pokemon/search?retreatCostMax=1

# Legal in Standard OR Expanded
GET /api/pokemon/search?formats=Standard&formats=Expanded

# Legal in BOTH Standard AND Expanded
GET /api/pokemon/search?formats=Standard&formats=Expanded&formatsMatchAll=true
```

**Complex Example (Multiple Filters Combined):**

```http
# Fire-type ex cards with high HP, an ability, legal in Standard format
GET /api/pokemon/search?types=Fire&subtypes=ex&hpMin=200&hasAbility=true&formats=Standard&sortBy=hpNumeric&sortOrder=desc
```

**Response:**

```json
{
  "results": [
    {
      "id": "base1-4",
      "name": "Charizard",
      "supertype": "Pokémon",
      "hp": "120",
      "hpNumeric": 120,
      "types": [
        "Fire"
      ],
      "subtypes": [
        "Stage 2"
      ],
      "setId": "base1",
      "setName": "Base Set",
      "rarityName": "Rare Holo",
      "artistName": "Mitsuhiro Arita",
      "attacks": [
        ...
      ],
      "abilities": [
        ...
      ],
      "weaknesses": [
        ...
      ],
      "resistances": [
        ...
      ],
      "retreatCost": [
        "Colorless",
        "Colorless",
        "Colorless"
      ],
      "convertedRetreatCost": 3,
      ...
    }
  ],
  "totalResults": 42,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": true,
  "hasPrevious": false
}
```

**Response Fields:**

- `results` - Array of card objects matching the search criteria
- `totalResults` - Total number of cards across all pages
- `totalPages` - Total number of pages available
- `currentPage` - Current page number (0-indexed)
- `pageSize` - Number of results per page
- `hasNext` - Boolean indicating if there's a next page
- `hasPrevious` - Boolean indicating if there's a previous page

#### Get Available Features

```http
GET /api/pokemon/features
```

Returns all available filter values to help build a dynamic search UI.

**Response:**

```json
{
  "type": "pokemon",
  "supertypes": [
    "Energy",
    "Pokémon",
    "Trainer"
  ],
  "types": [
    "Colorless",
    "Darkness",
    "Dragon",
    "Fairy",
    "Fighting",
    "Fire",
    "Grass",
    "Lightning",
    "Metal",
    "Psychic",
    "Water"
  ],
  "subtypes": [
    "ACE SPEC",
    "Ancient",
    "Basic",
    "EX",
    "ex",
    "LEGEND",
    "Level-Up",
    "MEGA",
    "Rapid Strike",
    "Single Strike",
    "Stage 1",
    "Stage 2",
    "Supporter",
    "Item",
    "Stadium",
    "Tool",
    "V",
    "VMAX",
    "VSTAR",
    ...
  ],
  "sets": [
    "base1",
    "base2",
    "base3",
    "swsh1",
    "swsh2",
    ...
  ],
  "rarities": [
    "Common",
    "Uncommon",
    "Rare",
    "Rare Holo",
    "Rare Ultra",
    "Rare Rainbow",
    ...
  ],
  "formats": [
    "Expanded",
    "Standard",
    "Unlimited"
  ],
  "regulationMarks": [
    "A",
    "B",
    "C",
    "D",
    "E",
    "F",
    "G",
    "H"
  ]
}
```

**Response Fields:**

- `type` - Card game type (always "pokemon")
- `supertypes` - Available supertypes for filtering
- `types` - Available Pokémon types for filtering
- `subtypes` - Available subtypes (stages, mechanics, trainer types)
- `sets` - Available set identifiers
- `rarities` - Available rarity values
- `formats` - Available tournament formats
- `regulationMarks` - Available regulation marks

### Planned Future Endpoints

### Decklist Management

#### Create Decklist

```http
POST /api/decklist
```

**Request Body:**

```json
{
  "type": "pokemon",
  "name": "My Deck",
  "cards": [
    {
      "id": "base1-1",
      "quantity": 2
    },
    {
      "id": "base1-2",
      "quantity": 4
    }
  ]
}
```

**Response:**

```json
{
  "id": "abc123",
  "type": "pokemon",
  "name": "My Deck",
  "createdAt": "2024-10-24T12:00:00Z",
  "cards": [
    ...
  ]
}
```

#### Get Decklist

```http
GET /api/decklist/{id}
```

**Parameters:**

- `id` (path) - Decklist identifier

**Response:**

```json
{
  "id": "abc123",
  "type": "pokemon",
  "name": "My Deck",
  "createdAt": "2024-10-24T12:00:00Z",
  "views": 42,
  "cards": [
    {
      "id": "base1-1",
      "quantity": 2,
      "card": {
        /* full card data */
      }
    }
  ]
}
```

### PDF Templates

#### Get Available Templates

```http
GET /api/pokemon/templates
```

**Response:**

```json
{
  "templates": [
    {
      "id": "official-pokemon",
      "name": "Official Pokemon Template",
      "description": "Standard Pokemon TCG decklist format"
    }
  ]
}
```

## Filter Parameters

### Pokemon Card Filters

##### Core Filters (Phase 1 - IMPLEMENTED)

- `name` - Card name (partial match, accent-insensitive)
- `supertype` - Card supertype (Pokémon, Trainer, Energy)
- `types` + `typesMatchAll` - Pokémon types with AND/OR logic
- `subtypes` + `subtypesMatchAll` - Card subtypes with AND/OR logic
- `setId` - Set identifier (e.g., "base1", "swsh8")
- `rarity` - Rarity name
- `hpMin` / `hpMax` - HP range filtering

##### Pagination & Sorting (IMPLEMENTED)

- `page` - Page number (0-indexed, default: 0)
- `pageSize` - Results per page (default: 20, max: 100)
- `sortBy` - Sort field (name, hpNumeric, number, etc.)
- `sortOrder` - Sort order ("asc" or "desc", default: "asc")

##### Attack & Ability Filters (Phase 2 - IMPLEMENTED)

- `attackName` - Attack name (partial match, accent-insensitive)
- `attackDamageMin` / `attackDamageMax` - Attack damage range
- `attackCost` + `attackCostMatchAll` - Attack cost types with AND/OR logic
- `hasAbility` - Boolean filter for ability presence
- `abilityName` - Ability name (partial match, accent-insensitive)

##### Additional Detail Filters (Phase 2 - IMPLEMENTED)

- `artist` - Artist name (exact match, accent-insensitive)
- `regulationMark` - Regulation mark (A, B, C, D, E, F, G, H)
- `retreatCostMin` / `retreatCostMax` - Retreat cost range
- `formats` + `formatsMatchAll` - Format legality with AND/OR logic

##### Boolean Filters (Phase 3 - PLANNED)

- `hasRuleBox` - Boolean, cards with rule boxes
- `hasWeakness` - Boolean, cards with weaknesses
- `hasResistance` - Boolean, cards with resistances

##### Weakness/Resistance Filters (Phase 3 - PLANNED)

- `weaknessType` - Weakness type
- `resistanceType` - Resistance type

##### Full-Text Search (Phase 3 - PLANNED)

- Multi-field text search across name, attack text, ability text, and trainer text
- PostgreSQL full-text search with relevance ranking

##### Evolution Filters (Phase 3 - PLANNED)

- `evolvesFrom` - Evolution source
- `evolvesTo` - Evolution target

## Response Codes

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request parameters
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Error Response Format

```json
{
  "error": {
    "code": "CARD_NOT_FOUND",
    "message": "Card with ID 'invalid-id' not found",
    "timestamp": "2024-10-24T12:00:00Z"
  }
}
```

## Rate Limiting

*Not yet implemented*

Rate limiting will be added in the future to prevent abuse:

- 100 requests per minute per IP
- 1000 requests per hour per IP

## Authentication

*Not yet implemented*

Future versions will support user authentication:

- OAuth2 for third-party login
- JWT tokens for API access
- API keys for programmatic access

## CORS

The API supports CORS for frontend access. In production, CORS will be restricted to specific domains.

## Notes

- All timestamps are in ISO 8601 format (UTC)
- Card IDs follow the format: `{set}-{number}` (e.g., `base1-1`)
