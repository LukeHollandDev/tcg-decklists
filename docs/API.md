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

| Parameter            | Type     | Description                                                              | Example                                            |
|----------------------|----------|--------------------------------------------------------------------------|----------------------------------------------------|
| `attackName`         | String   | Attack name (partial match, accent-insensitive)                          | `attackName=Thunderbolt`                           |
| `attackText`         | String   | Attack text/description search (partial match)                           | `attackText=draw`                                  |
| `attackDamageMin`    | Integer  | Minimum attack damage (inclusive)                                        | `attackDamageMin=100`                              |
| `attackDamageMax`    | Integer  | Maximum attack damage (inclusive)                                        | `attackDamageMax=200`                              |
| `attackCost`         | String[] | Attack cost types (supports duplicates for multiset matching)            | `attackCost=Fire&attackCost=Fire&attackCost=Water` |
| `attackCostMatchAll` | Boolean  | If true, match ALL cost types with quantities (multiset subset matching) | `attackCostMatchAll=true`                          |

**Query Parameters (Phase 2 - Ability Filters):**

| Parameter     | Type    | Description                                      | Example                  |
|---------------|---------|--------------------------------------------------|--------------------------|
| `hasAbility`  | Boolean | Filter by ability presence (true/false)          | `hasAbility=true`        |
| `abilityName` | String  | Ability name (partial match, accent-insensitive) | `abilityName=Intimidate` |
| `abilityText` | String  | Ability text/description search (partial match)  | `abilityText=damage`     |

**Query Parameters (Phase 2 - Detail Filters):**

| Parameter               | Type     | Description                                   | Example                             |
|-------------------------|----------|-----------------------------------------------|-------------------------------------|
| `artist`                | String   | Artist name (exact match, accent-insensitive) | `artist=Ken Sugimori`               |
| `regulationMark`        | String   | Regulation mark (A, B, C, D, E, F, G, H)      | `regulationMark=E`                  |
| `retreatCostMin`        | Integer  | Minimum retreat cost (inclusive)              | `retreatCostMin=1`                  |
| `retreatCostMax`        | Integer  | Maximum retreat cost (inclusive)              | `retreatCostMax=3`                  |
| `formats`               | String[] | Format legality - ANY match                   | `formats=Standard&formats=Expanded` |
| `formatsMatchAll`       | Boolean  | If true, legal in ALL formats (AND logic)     | `formatsMatchAll=true`              |
| `formatsBanned`         | String[] | Formats where cards are BANNED - ANY match    | `formatsBanned=Standard`            |
| `formatsBannedMatchAll` | Boolean  | If true, banned in ALL formats (AND logic)    | `formatsBannedMatchAll=true`        |

**Query Parameters (Phase 3 - Boolean Filters):**

| Parameter       | Type    | Description                                | Example              |
|-----------------|---------|--------------------------------------------|----------------------|
| `hasRuleBox`    | Boolean | Filter by rule box presence (true/false)   | `hasRuleBox=true`    |
| `hasWeakness`   | Boolean | Filter by weakness presence (true/false)   | `hasWeakness=true`   |
| `hasResistance` | Boolean | Filter by resistance presence (true/false) | `hasResistance=true` |

**Query Parameters (Phase 3 - Weakness/Resistance Filters):**

| Parameter                | Type     | Description                                      | Example                                    |
|--------------------------|----------|--------------------------------------------------|--------------------------------------------|
| `weaknessType`           | String[] | Weakness types (Fire, Water, etc.) - ANY match   | `weaknessType=Fire&weaknessType=Water`     |
| `weaknessTypeMatchAll`   | Boolean  | If true, match ALL weakness types (AND logic)    | `weaknessTypeMatchAll=true`                |
| `resistanceType`         | String[] | Resistance types (Fire, Water, etc.) - ANY match | `resistanceType=Fire&resistanceType=Water` |
| `resistanceTypeMatchAll` | Boolean  | If true, match ALL resistance types (AND logic)  | `resistanceTypeMatchAll=true`              |

**Query Parameters (Phase 3 - Evolution & Rule Filters):**

| Parameter     | Type   | Description                                          | Example               |
|---------------|--------|------------------------------------------------------|-----------------------|
| `evolvesFrom` | String | Evolution source (partial match, accent-insensitive) | `evolvesFrom=Pikachu` |
| `evolvesTo`   | String | Evolution target (partial match, accent-insensitive) | `evolvesTo=Raichu`    |
| `ruleText`    | String | Rule text/description search (partial match)         | `ruleText=GX`         |

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

# Find attacks that mention "draw" (card draw effects)
GET /api/pokemon/search?attackText=draw

# High-damage attacks (100+ damage)
GET /api/pokemon/search?attackDamageMin=100

# Attacks requiring Fire OR Colorless energy (OR logic)
GET /api/pokemon/search?attackCost=Fire&attackCost=Colorless

# Attacks requiring at least 2x Fire energy (multiset matching)
GET /api/pokemon/search?attackCost=Fire&attackCost=Fire&attackCostMatchAll=true

# Attacks requiring at least 2x Fire AND 1x Water (multiset subset matching)
# This will match attacks like [Fire, Fire, Water] or [Fire, Fire, Water, Colorless]
GET /api/pokemon/search?attackCost=Fire&attackCost=Fire&attackCost=Water&attackCostMatchAll=true

# Attacks requiring Fire AND Grass energy (ignores quantity for mixed types)
GET /api/pokemon/search?attackCost=Fire&attackCost=Grass&attackCostMatchAll=true

# Fire-type cards with "draw" in attack text
GET /api/pokemon/search?types=Fire&attackText=draw
```

**Example Requests (Phase 2 - Ability & Detail Filters):**

```http
# Only cards with abilities
GET /api/pokemon/search?hasAbility=true

# Cards with "Intimidate" ability
GET /api/pokemon/search?abilityName=Intimidate

# Find abilities that mention "damage"
GET /api/pokemon/search?abilityText=damage

# Abilities with "once during your turn" text
GET /api/pokemon/search?abilityText=once%20during%20your%20turn

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

# Cards BANNED in Standard format
GET /api/pokemon/search?formatsBanned=Standard

# Cards legal in Expanded but banned in Standard (interesting use case!)
GET /api/pokemon/search?formats=Expanded&formatsBanned=Standard

# Cards banned in BOTH Standard AND Expanded
GET /api/pokemon/search?formatsBanned=Standard&formatsBanned=Expanded&formatsBannedMatchAll=true
```

**Example Requests (Phase 3 - Boolean & Evolution Filters):**

```http
# Cards with rule boxes (like GX, V, VMAX, ex cards)
GET /api/pokemon/search?hasRuleBox=true

# Cards without weaknesses
GET /api/pokemon/search?hasWeakness=false

# Cards with resistances
GET /api/pokemon/search?hasResistance=true

# Cards that evolve from Pikachu
GET /api/pokemon/search?evolvesFrom=Pikachu

# Cards that evolve to Charizard
GET /api/pokemon/search?evolvesTo=Charizard

# Cards with "GX" in rule text
GET /api/pokemon/search?ruleText=GX
```

**Example Requests (Phase 3 - Weakness/Resistance Type Filters):**

```http
# Cards weak to Fire OR Water
GET /api/pokemon/search?weaknessType=Fire&weaknessType=Water

# Cards weak to BOTH Fire AND Water (rare, but possible)
GET /api/pokemon/search?weaknessType=Fire&weaknessType=Water&weaknessTypeMatchAll=true

# Cards resistant to Psychic
GET /api/pokemon/search?resistanceType=Psychic

# Cards resistant to BOTH Fire AND Water
GET /api/pokemon/search?resistanceType=Fire&resistanceType=Water&resistanceTypeMatchAll=true

# Grass-type cards weak to Fire
GET /api/pokemon/search?types=Grass&weaknessType=Fire

# Cards with weaknesses but no resistances
GET /api/pokemon/search?hasWeakness=true&hasResistance=false
```

**Complex Examples (Multiple Filters Combined):**

```http
# Fire-type ex cards with high HP, an ability, legal in Standard format
GET /api/pokemon/search?types=Fire&subtypes=ex&hpMin=200&hasAbility=true&formats=Standard&sortBy=hpNumeric&sortOrder=desc

# Cards with "draw" in attack text, legal in Standard, sorted by name
GET /api/pokemon/search?attackText=draw&formats=Standard&sortBy=name&sortOrder=asc

# High-HP cards (200+) with abilities that mention "damage", not banned in Standard
GET /api/pokemon/search?hpMin=200&abilityText=damage&formats=Standard

# Cards legal in Expanded but banned in Standard (format comparison)
GET /api/pokemon/search?formats=Expanded&formatsBanned=Standard&pageSize=50

# Evolution cards with rule boxes that evolve from Eevee
GET /api/pokemon/search?evolvesFrom=Eevee&hasRuleBox=true

# Fire-type Stage 2 cards weak to Water, with abilities
GET /api/pokemon/search?types=Fire&subtypes=Stage%202&weaknessType=Water&hasAbility=true&sortBy=hpNumeric&sortOrder=desc
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

#### Autocomplete Endpoints

These endpoints provide autocomplete/typeahead functionality for search-enabled dropdowns in the frontend. They use *
*prefix-first matching** for better relevance: results starting with the query are prioritized, with substring matches
used as fallback if needed.

All autocomplete endpoints support:

- **Accent-insensitive matching** (e.g., "flabebe" matches "Flabébé")
- **Case-insensitive matching**
- **Configurable result limits** (default: 10, max: 100)

##### Autocomplete Artists

```http
GET /api/pokemon/artists?query={text}&limit={n}
```

Returns artist names matching the query for use in search dropdowns.

**Query Parameters:**

| Parameter | Type    | Required | Description                       | Default |
|-----------|---------|----------|-----------------------------------|---------|
| `query`   | String  | Yes      | Search text (minimum 1 character) | -       |
| `limit`   | Integer | No       | Maximum results (max: 100)        | 10      |

**Example Requests:**

```http
# Basic usage
GET /api/pokemon/artists?query=ken

# With custom limit
GET /api/pokemon/artists?query=sugimori&limit=20
```

**Response:**

```json
{
  "results": [
    "Ken Ikuji",
    "Ken Sugimori",
    "Ken Sugimori/Yusuke Ohmura",
    "Kenkichi Toyama",
    "Kent Kanetsuna"
  ],
  "count": 5,
  "query": "ken",
  "limit": 10
}
```

**Response Fields:**

- `results` - Array of matching artist names
- `count` - Number of results returned
- `query` - The search query used
- `limit` - The limit that was applied

**Error Responses:**

- `400 Bad Request` - Query parameter is missing or empty

##### Autocomplete Attacks

```http
GET /api/pokemon/attacks?query={text}&limit={n}
```

Returns attack names matching the query for use in search dropdowns.

**Query Parameters:**

| Parameter | Type    | Required | Description                       | Default |
|-----------|---------|----------|-----------------------------------|---------|
| `query`   | String  | Yes      | Search text (minimum 1 character) | -       |
| `limit`   | Integer | No       | Maximum results (max: 100)        | 10      |

**Example Requests:**

```http
# Find fire-related attacks
GET /api/pokemon/attacks?query=fire

# Find blast attacks
GET /api/pokemon/attacks?query=blast&limit=20
```

**Response:**

```json
{
  "results": [
    "Fire Arrow",
    "Fire Blast",
    "Fire Blaster",
    "Fire Blow",
    "Fire Claws",
    "Fire Counterattack",
    "Fire Dance",
    "Fire Fang",
    "Fire Fling",
    "Fire Force"
  ],
  "count": 10,
  "query": "fire",
  "limit": 10
}
```

##### Autocomplete Abilities

```http
GET /api/pokemon/abilities?query={text}&limit={n}
```

Returns ability names matching the query for use in search dropdowns.

**Query Parameters:**

| Parameter | Type    | Required | Description                       | Default |
|-----------|---------|----------|-----------------------------------|---------|
| `query`   | String  | Yes      | Search text (minimum 1 character) | -       |
| `limit`   | Integer | No       | Maximum results (max: 100)        | 10      |

**Example Requests:**

```http
# Find power-related abilities
GET /api/pokemon/abilities?query=power

# Find intimidate abilities
GET /api/pokemon/abilities?query=intimidate&limit=15
```

**Response:**

```json
{
  "results": [
    "Power Bind",
    "Power Cancel",
    "Power Cheer",
    "Power Circulation",
    "Power Connect",
    "Power Diffusion",
    "Power Draw",
    "Power Gene",
    "Power Huddle",
    "Power Pinchers"
  ],
  "count": 10,
  "query": "power",
  "limit": 10
}
```

##### Autocomplete Card Names

```http
GET /api/pokemon/card-names?query={text}&limit={n}
```

Returns card names with their IDs matching the query for use in search dropdowns.
**Format:** `"CardName (card-id)"` to distinguish between multiple versions across different sets.

**Query Parameters:**

| Parameter | Type    | Required | Description                       | Default |
|-----------|---------|----------|-----------------------------------|---------|
| `query`   | String  | Yes      | Search text (minimum 1 character) | -       |
| `limit`   | Integer | No       | Maximum results (max: 100)        | 10      |

**Example Requests:**

```http
# Find Charizard cards
GET /api/pokemon/card-names?query=char

# Find Pikachu cards
GET /api/pokemon/card-names?query=pikachu&limit=20

# Accent-insensitive search
GET /api/pokemon/card-names?query=flabebe
```

**Response:**

```json
{
  "results": [
    "Charcadet (sv1-40)",
    "Charizard (base1-4)",
    "Charizard (basep-4)",
    "Charizard (dp-37)",
    "Charizard (xy121)",
    "Charizard & Braixen-GX (sm11-22)",
    "Charizard G (pl3-20)",
    "Charizard G LV.X (pl3-143)",
    "Charizard V (swsh9-17)",
    "Charizard VMAX (swsh9-18)"
  ],
  "count": 10,
  "query": "char",
  "limit": 10
}
```

**Key Features:**

- Shows card ID in parentheses (e.g., "Charizard (base1-4)")
- Displays all versions of a card across different sets
- Useful for precise card selection when building decklists

##### Autocomplete Set Names

```http
GET /api/pokemon/sets?query={text}&limit={n}
```

Returns user-friendly set names matching the query for use in set selection dropdowns.
Returns human-readable names (e.g., "Base Set") rather than set IDs (e.g., "base1").

**Query Parameters:**

| Parameter | Type    | Required | Description                       | Default |
|-----------|---------|----------|-----------------------------------|---------|
| `query`   | String  | Yes      | Search text (minimum 1 character) | -       |
| `limit`   | Integer | No       | Maximum results (max: 100)        | 10      |

**Example Requests:**

```http
# Find Base sets
GET /api/pokemon/sets?query=base

# Find Sword & Shield sets
GET /api/pokemon/sets?query=sword&limit=20
```

**Response:**

```json
{
  "results": [
    "Base",
    "Base Set",
    "Base Set 2",
    "EX FireRed & LeafGreen",
    "Gym Heroes",
    "Gym Challenge"
  ],
  "count": 6,
  "query": "base",
  "limit": 10
}
```

**Key Features:**

- Returns display-friendly set names for better UX
- Useful for set-based filtering in search interfaces
- Complements the existing `/features` endpoint which returns set IDs

**General Notes for All Autocomplete Endpoints:**

- All endpoints use the same response format
- Results are ordered with prefix matches first, then substring matches for better relevance
- Empty or missing `query` parameter returns `400 Bad Request`
- Accent-insensitive and case-insensitive matching supported
- Duplicates are automatically deduplicated

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
- `attackText` - Attack text/description search (partial match, accent-insensitive)
- `attackDamageMin` / `attackDamageMax` - Attack damage range
- `attackCost` + `attackCostMatchAll` - Attack cost types with AND/OR logic
- `hasAbility` - Boolean filter for ability presence
- `abilityName` - Ability name (partial match, accent-insensitive)
- `abilityText` - Ability text/description search (partial match, accent-insensitive)

##### Additional Detail Filters (Phase 2 - IMPLEMENTED)

- `artist` - Artist name (exact match, accent-insensitive)
- `regulationMark` - Regulation mark (A, B, C, D, E, F, G, H)
- `retreatCostMin` / `retreatCostMax` - Retreat cost range
- `formats` + `formatsMatchAll` - Format legality with AND/OR logic
- `formatsBanned` + `formatsBannedMatchAll` - Format ban status with AND/OR logic

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
