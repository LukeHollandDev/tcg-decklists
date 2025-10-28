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

### Planned Endpoints

#### Search Cards

```http
GET /api/pokemon/search?name=alakazam&hp=80&types=psychic
```

**Parameters:**

- Query parameters for filtering (see Filter Parameters below)

**Response:**

```json
{
  "results": [
    ...
  ],
  "total": 42,
  "page": 1,
  "pageSize": 20
}
```

#### Get Available Features

```http
GET /api/pokemon/features
```

**Response:**

```json
{
  "type": "pokemon",
  "filters": {
    "types": [
      "Grass",
      "Fire",
      "Water",
      ...
    ],
    "subtypes": [
      "Basic",
      "Stage 1",
      "Stage 2",
      ...
    ],
    "rarities": [
      "Common",
      "Uncommon",
      "Rare",
      ...
    ],
    "formats": [
      "Standard",
      "Expanded",
      "Unlimited"
    ]
  }
}
```

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

#### Gameplay Filters

- `supertype` - Card type (Pokémon, Trainer, Energy)
- `subtype` - Mechanic/Stage (ex, V, GX, Basic, Stage 1, Stage 2, etc.)
- `types` - Elemental types (Grass, Fire, Water, etc.)
- `hasAbility` - Boolean, cards with abilities
- `hasRuleBox` - Boolean, cards with rule boxes
- `hasWeakness` - Boolean, cards with weaknesses
- `hasResistance` - Boolean, cards with resistances

#### Detail Filters

- `name` - Card name (partial match)
- `hp` - HP value (exact or range: `hp=80` or `hpMin=80&hpMax=120`)
- `attackName` - Attack name (partial match)
- `attackDamage` - Attack damage (exact or range)
- `retreatCost` - Retreat cost (exact or range)
- `weakness` - Weakness type
- `resistance` - Resistance type
- `rarity` - Card rarity
- `artist` - Artist name
- `set` - Set identifier
- `format` - Format legality (Standard, Expanded, Unlimited)
- `regulationMark` - Regulation mark

#### Pagination

- `page` - Page number (default: 1)
- `pageSize` - Results per page (default: 20, max: 100)
- `sort` - Sort field (name, hp, number, etc.)
- `order` - Sort order (asc, desc)

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
