# Search Implementation Guide

This document explains the search functionality implementation for TCG Decklists, covering architecture, design
decisions, usage, and how to extend it.

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Phase 1 Implementation](#phase-1-implementation)
4. [How It Works](#how-it-works)
5. [Adding New Filters](#adding-new-filters)
6. [Query Performance](#query-performance)
7. [Testing Strategy](#testing-strategy)
8. [Usage Examples](#usage-examples)
9. [Future Enhancements](#future-enhancements)

---

## Overview

The search functionality enables comprehensive filtering of Pokemon cards with:

- **Text search** across card names, attack text, ability text, and rule text (case-insensitive, accent-insensitive,
  partial matching)
- **Multiple filter types** (supertype, types, subtypes, set, rarity, HP range, attacks, abilities, weaknesses,
  resistances, evolutions)
- **Boolean filters** (hasAbility, hasRuleBox, hasWeakness, hasResistance)
- **AND/OR logic support** for multi-value filters (types, subtypes, attack costs, weaknesses, resistances, formats)
- **Pagination** (configurable page size, max 100 per page)
- **Sorting** (by any field, ascending or descending)
- **Filter discovery** (features endpoint returns available filter values)

### Technology Stack

- **Spring Data JPA Specifications** - Dynamic query building
- **JPQL (Java Persistence Query Language)** - Database queries
- **PostgreSQL** - Database with optimized schema
- **Spring Boot** - REST API framework

---

## Architecture

### Component Structure

```
Controller (REST endpoints)
    ↓
Service (Business logic)
    ↓
Specifications (Query building)
    ↓
Repository (Data access)
    ↓
Database (PostgreSQL)
```

### Key Components

#### 1. DTOs (`dto/` package)

- **CardSearchRequest** - Captures all search parameters from query string
- **CardSearchResponse** - Wraps paginated results with metadata
- **FilterOptionsResponse** - Returns available filter values for UI

#### 2. Specifications (`specifications/` package)

- **CardSpecification** - Contains static methods that build JPA Specification objects
- Each method returns a `Specification<Card>` that can be composed with others
- Specifications are combined using `Specification.allOf()` for AND logic

#### 3. Repository

- Extends `JpaSpecificationExecutor<Card>` to enable dynamic queries
- Provides custom JPQL queries for filter options (distinct values)

#### 4. Service

- Orchestrates search: builds specification, creates pageable, executes query
- Transforms entity results to DTOs
- Provides filter options for features endpoint

#### 5. Controller

- Maps HTTP query parameters to DTOs using `@ModelAttribute`
- Returns standardized JSON responses

---

## Phase 1 Implementation

### Implemented Filters

| Filter      | Type         | Description                   | Implementation                   |
|-------------|--------------|-------------------------------|----------------------------------|
| `name`      | String       | Card name (partial match)     | ILIKE query on `name` field      |
| `supertype` | String       | Pokémon/Trainer/Energy        | Exact match on `supertype` field |
| `types`     | List<String> | Fire, Water, etc.             | Join to `types` many-to-many     |
| `subtypes`  | List<String> | ex, V, Basic, etc.            | Join to `subtypes` many-to-many  |
| `setId`     | String       | Set identifier (base1, swsh8) | Join to `set` many-to-one        |
| `rarity`    | String       | Common, Rare, etc.            | Join to `rarity` many-to-one     |
| `hpMin`     | Integer      | Minimum HP (inclusive)        | `>= hpNumeric`                   |
| `hpMax`     | Integer      | Maximum HP (inclusive)        | `<= hpNumeric`                   |

### Pagination & Sorting

- **Page** - 0-indexed (default: 0)
- **Page Size** - Results per page (default: 20, max: 100)
- **Sort By** - Field name (default: "name")
- **Sort Order** - "asc" or "desc" (default: "asc")

---

## How It Works

### 1. Request Flow

```
GET /api/pokemon/search?name=Pikachu&types=Electric&hpMin=50
                    ↓
Controller receives CardSearchRequest with populated fields
                    ↓
Service.search(request) called
                    ↓
CardSpecification.buildSpecification() creates composed Specification
                    ↓
Repository.findAll(spec, pageable) executes query
                    ↓
Results transformed to CardResponse DTOs
                    ↓
Wrapped in CardSearchResponse with pagination metadata
                    ↓
JSON returned to client
```

### 2. Specification Composition

Specifications are functions that build query predicates. They follow this pattern:

```java
public static Specification<Card> hasNameContaining(String name) {
    return (root, query, criteriaBuilder) -> {
        if (name == null || name.trim().isEmpty()) {
            return criteriaBuilder.conjunction(); // No filter (always true)
        }
        return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + name.trim().toLowerCase() + "%"
        );
    };
}
```

Multiple specifications are combined with `Specification.allOf()`:

```java
return Specification.allOf(
        hasNameContaining(name),

hasSupertype(supertype),

hasTypes(types),
// ... more specifications
);
```

This generates SQL with `WHERE` clauses combined with `AND`.

### 3. Database Query Example

For a request like `?name=Pikachu&types=Electric&hpMin=50`:

```sql
SELECT DISTINCT c.*
FROM pokemon_card c
         LEFT JOIN pokemon_card_type ct ON c.id = ct.card_id
         LEFT JOIN pokemon_type t ON ct.type_id = t.id
WHERE LOWER(c.name) LIKE '%pikachu%'
  AND LOWER(t.name) IN ('electric')
  AND c.hp_numeric >= 50
ORDER BY c.name ASC
LIMIT 20 OFFSET 0;
```

### 4. Features Endpoint

The `/api/pokemon/features` endpoint queries distinct values:

```sql
-- Types
SELECT DISTINCT name
FROM pokemon_type
ORDER BY name;

-- Sets
SELECT DISTINCT set_id
FROM pokemon_set
WHERE set_id IS NOT NULL
ORDER BY set_id;

-- Etc. for each filter type
```

These results help frontends build dynamic filter UI components.

---

## Adding New Filters

### Step-by-Step Guide

Let's add a filter for attack names as an example.

#### 1. Add Parameter to CardSearchRequest

```java
/** Filter by attack name (partial match) */
private String attackName;

public String getAttackName() {
    return attackName;
}

public void setAttackName(String attackName) {
    this.attackName = attackName;
}
```

#### 2. Create Specification Method

```java
/**
 * Filter by attack name (case-insensitive partial match).
 *
 * @param attackName The attack name to search for
 * @return Specification that matches cards with attacks containing the search term
 */
public static Specification<Card> hasAttackName(String attackName) {
    return (root, query, criteriaBuilder) -> {
        if (attackName == null || attackName.trim().isEmpty()) {
            return criteriaBuilder.conjunction();
        }

        // Join to attacks collection (many-to-many)
        Join<Card, Attack> attacksJoin = root.join("attacks", JoinType.INNER);

        // Add DISTINCT to avoid duplicate cards
        if (query != null) {
            query.distinct(true);
        }

        // Match attack name (case-insensitive)
        return criteriaBuilder.like(
                criteriaBuilder.lower(attacksJoin.get("name")),
                "%" + attackName.trim().toLowerCase() + "%"
        );
    };
}
```

#### 3. Update buildSpecification()

```java
public static Specification<Card> buildSpecification(
        String name,
        String supertype,
        List<String> types,
        List<String> subtypes,
        String setId,
        String rarity,
        Integer hpMin,
        Integer hpMax,
        String attackName  // NEW PARAMETER
) {
    return Specification.allOf(
            hasNameContaining(name),
            hasSupertype(supertype),
            hasTypes(types),
            hasSubtypes(subtypes),
            hasSetId(setId),
            hasRarity(rarity),
            hpBetween(hpMin, hpMax),
            hasAttackName(attackName)  // NEW SPECIFICATION
    );
}
```

#### 4. Update Service.search()

```java
Specification<Card> spec = CardSpecification.buildSpecification(
        request.getName(),
        request.getSupertype(),
        request.getTypes(),
        request.getSubtypes(),
        request.getSetId(),
        request.getRarity(),
        request.getHpMin(),
        request.getHpMax(),
        request.getAttackName()  // NEW PARAMETER
);
```

#### 5. Add Tests

```java

@Test
void testSearchByAttackName() throws Exception {
    mockMvc.perform(get("/api/pokemon/search")
                    .param("attackName", "Thunderbolt"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results").isArray());
}
```

#### 6. Update Documentation

Add the new filter to API.md query parameters documentation.

---

## Query Performance

### Optimization Strategies

#### 1. Pre-computed Numeric Fields

The schema includes `hp_numeric`, `damage_numeric`, and `converted_retreat_cost` fields to avoid string-to-number
conversions during queries.

```java
// Efficient: Uses indexed numeric field
.and(hpBetween(50, 100))

// Would be slow: String parsing in query
// WHERE CAST(hp AS INTEGER) BETWEEN 50 AND 100
```

#### 2. DISTINCT for Many-to-Many Joins

When joining to collections (types, subtypes, attacks), we add `DISTINCT` to prevent duplicate cards:

```java
if(query !=null){
        query.

distinct(true);
}
```

Without this, a card with 2 Fire energy and 1 Water energy would appear 3 times in results.

#### 3. Database Indexes

Consider adding indexes for frequently queried fields:

```sql
CREATE INDEX idx_card_name ON pokemon_card (LOWER(name));
CREATE INDEX idx_card_hp_numeric ON pokemon_card (hp_numeric);
CREATE INDEX idx_card_supertype ON pokemon_card (LOWER(supertype));
```

#### 4. Lazy Loading

Entity relationships use `FetchType.LAZY` to avoid loading unnecessary data:

```java

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "set_id")
private Set set;
```

Only when `CardResponse` DTOs are created are related entities loaded (via getters).

#### 5. Pagination

Always use pagination to limit result set size. The max page size of 100 prevents overwhelming queries.

### Performance Monitoring

Monitor query performance using:

1. **Spring Boot Actuator** - Track query execution times
2. **PostgreSQL EXPLAIN ANALYZE** - Analyze query plans
3. **Application logs** - Enable Hibernate SQL logging:

```properties
# application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## Testing Strategy

### Three Testing Levels

#### 1. Unit Tests (Not Yet Implemented)

Test individual Specification methods in isolation:

```java

@Test
void testHasNameContaining() {
    Specification<Card> spec = CardSpecification.hasNameContaining("Pikachu");
    // Use JPA Metamodel to verify predicate structure
}
```

#### 2. Integration Tests (Implemented)

Test full request-to-response flow with MockMvc:

```java

@Test
void testSearchByName() throws Exception {
    mockMvc.perform(get("/api/pokemon/search")
                    .param("name", "Pikachu"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results[*].name",
                    everyItem(containsStringIgnoringCase("pikachu"))));
}
```

Located in: `ControllerIntegrationTest.java`

#### 3. Manual Testing

Use tools like Bruno, Postman, or curl:

```bash
# Search for Electric Pikachu cards with HP >= 50
curl "http://localhost:8080/api/pokemon/search?name=Pikachu&types=Electric&hpMin=50"

# Get available filter options
curl "http://localhost:8080/api/pokemon/features"
```

### Test Data Requirements

Integration tests require:

- Database running (see DEVELOPMENT.md)
- Card data loaded via data pipeline
- Spring Boot application context

---

## Usage Examples

### Example 1: Simple Name Search

**Request:**

```http
GET /api/pokemon/search?name=Charizard
```

**Response:**

```json
{
  "results": [
    {
      "id": "base1-4",
      "name": "Charizard",
      "supertype": "Pokémon",
      "types": [
        "Fire"
      ],
      "hp": "120",
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

### Example 2: Multi-Filter Search

**Request:**

```http
GET /api/pokemon/search?types=Fire&subtypes=ex&hpMin=100&sortBy=hpNumeric&sortOrder=desc
```

Finds: Fire-type ex cards with HP >= 100, sorted by HP descending.

### Example 3: Pagination

**Request:**

```http
GET /api/pokemon/search?supertype=Trainer&page=2&pageSize=50
```

Returns: Page 3 (0-indexed) of Trainer cards, 50 per page.

### Example 4: Filter Options Discovery

**Request:**

```http
GET /api/pokemon/features
```

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
    ...
  ],
  "subtypes": [
    "ACE SPEC",
    "Ancient",
    "Basic",
    "EX",
    "ex",
    ...
  ],
  "sets": [
    "base1",
    "base2",
    "base3",
    ...
  ],
  "rarities": [
    "Common",
    "Uncommon",
    "Rare",
    "Rare Holo",
    ...
  ],
  "formats": [
    "Standard",
    "Expanded",
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

### Example 5: Empty Search (All Cards)

**Request:**

```http
GET /api/pokemon/search
```

Returns: First 20 cards (default pagination), sorted by name.

---

## Completed Phases

### Phase 2: Extended Filters (IMPLEMENTED)

Phase 2 has been fully implemented with the following filters:

- **Attack Filters**
    - `attackName` - Attack name search (accent-insensitive partial match)
    - `attackText` - Attack text/description search (accent-insensitive partial match)
    - `attackDamageMin` / `attackDamageMax` - Attack damage range filtering
    - `attackCost` + `attackCostMatchAll` - Attack cost types with AND/OR logic and **multiset subset matching**
        - Supports specifying multiple of the same type (e.g., `Fire,Fire,Water`)
        - With `attackCostMatchAll=true`, finds cards with attacks containing AT LEAST the specified quantities
        - Example: searching for `Fire,Fire,Water` matches attacks with `[Fire, Fire, Water]` or `[Fire, Fire, Water, Colorless]`

- **Ability Filters**
    - `hasAbility` - Boolean filter for ability presence/absence
    - `abilityName` - Ability name search (accent-insensitive partial match)
    - `abilityText` - Ability text/description search (accent-insensitive partial match)

- **Additional Detail Filters**
    - `artist` - Artist name (exact match, accent-insensitive)
    - `regulationMark` - Regulation mark filtering (A, B, C, D, E, F, G, H)
    - `retreatCostMin` / `retreatCostMax` - Retreat cost range
    - `formats` + `formatsMatchAll` - Format legality with AND/OR logic
    - `formatsBanned` + `formatsBannedMatchAll` - Format ban status with AND/OR logic

All Phase 2 filters support the same accent-insensitive searching as Phase 1!

#### Phase 2 Enhancements (Latest Update)

**Text Search Capabilities:**

- Search within attack descriptions to find cards by what they do (e.g., "draw cards", "damage counters")
- Search within ability descriptions to find specific mechanics (e.g., "once during your turn")

**Format Ban Analysis:**

- Find cards banned in specific formats
- Compare format legality (e.g., legal in Expanded but banned in Standard)
- Support for both OR logic (banned in ANY format) and AND logic (banned in ALL formats)

#### Multiset Subset Matching for Attack Costs

The `attackCost` filter now supports **multiset subset matching** when `attackCostMatchAll=true`. This allows you to search for attacks requiring specific quantities of energy types.

**How It Works:**

1. **Specify quantities by repeating types**: Pass the same type multiple times to specify quantity requirements
2. **Subset matching**: Attacks with MORE energies than specified will still match (as long as they have at least the required quantities)
3. **Single attack requirement**: All type/quantity requirements must be satisfied by the SAME attack

**Examples:**

```http
# Find attacks requiring at least 2 Fire energy
GET /api/pokemon/search?attackCost=Fire&attackCost=Fire&attackCostMatchAll=true

# Find attacks requiring at least 2 Fire AND 1 Water
# Matches: [Fire, Fire, Water], [Fire, Fire, Water, Colorless], [Fire, Fire, Fire, Water], etc.
# Does NOT match: [Fire, Water] (insufficient Fire)
GET /api/pokemon/search?attackCost=Fire&attackCost=Fire&attackCost=Water&attackCostMatchAll=true

# Find attacks requiring at least 3 Colorless
GET /api/pokemon/search?attackCost=Colorless&attackCost=Colorless&attackCost=Colorless&attackCostMatchAll=true
```

**Implementation Details:**

- Uses JPA Criteria API with nested subqueries
- Counts occurrences of each type in the search criteria (creates a multiset)
- For each unique type, checks if there exists an attack with an `AttackCost` entry where `quantity >= required_quantity`
- All type requirements are checked against the SAME attack (not different attacks on the same card)
- The `AttackCost` table stores quantities in a single row per type (e.g., `Fire(quantity=2)` instead of two separate rows)

#### Example Use Cases for Phase 2 Enhancements

**Content-Based Search:**

```http
# Find cards with card draw mechanics
GET /api/pokemon/search?attackText=draw

# Find cards with damage counter manipulation
GET /api/pokemon/search?abilityText=damage%20counter

# Find abilities that activate "once during your turn"
GET /api/pokemon/search?abilityText=once%20during%20your%20turn
```

**Ban List Analysis:**

```http
# All cards banned in Standard
GET /api/pokemon/search?formatsBanned=Standard

# Cards legal in Expanded but banned in Standard (format-specific bans)
GET /api/pokemon/search?formats=Expanded&formatsBanned=Standard

# Cards banned in both Standard AND Expanded (most restrictive)
GET /api/pokemon/search?formatsBanned=Standard&formatsBanned=Expanded&formatsBannedMatchAll=true
```

**Deck Building Scenarios:**

```http
# Fire-type cards with "draw" effects, legal in Standard
GET /api/pokemon/search?types=Fire&attackText=draw&formats=Standard

# High-HP cards (200+) with damaging abilities
GET /api/pokemon/search?hpMin=200&abilityText=damage

# Low retreat cost cards with search abilities
GET /api/pokemon/search?retreatCostMax=1&abilityText=search
```

### Phase 3: Advanced Filters (IMPLEMENTED)

Phase 3 has been fully implemented with the following filters:

- **Boolean Filters**
    - `hasRuleBox` - Filter cards with/without rule boxes
    - `hasWeakness` - Filter cards with/without weaknesses
    - `hasResistance` - Filter cards with/without resistances

- **Weakness/Resistance Type Filters**
    - `weaknessType` + `weaknessTypeMatchAll` - Weakness types with AND/OR logic
    - `resistanceType` + `resistanceTypeMatchAll` - Resistance types with AND/OR logic

- **Evolution Filters**
    - `evolvesFrom` - Evolution source (partial match, accent-insensitive)
    - `evolvesTo` - Evolution target (partial match, accent-insensitive)

- **Rule Text Search**
    - `ruleText` - Rule text/description search (partial match, accent-insensitive)

All Phase 3 filters follow the same accent-insensitive and composable patterns as Phase 1 and Phase 2!

#### Example Use Cases for Phase 3 Filters

**Boolean Filters:**

```http
# Find all cards with rule boxes (GX, V, VMAX, ex, etc.)
GET /api/pokemon/search?hasRuleBox=true

# Find cards without weaknesses
GET /api/pokemon/search?hasWeakness=false

# Find cards with resistances
GET /api/pokemon/search?hasResistance=true
```

**Weakness/Resistance Analysis:**

```http
# Find cards weak to Fire OR Water
GET /api/pokemon/search?weaknessType=Fire&weaknessType=Water

# Find Grass-type cards weak to Fire
GET /api/pokemon/search?types=Grass&weaknessType=Fire

# Find cards resistant to Psychic
GET /api/pokemon/search?resistanceType=Psychic

# Find cards with weaknesses but no resistances
GET /api/pokemon/search?hasWeakness=true&hasResistance=false
```

**Evolution Chain Exploration:**

```http
# Find all cards that evolve from Pikachu
GET /api/pokemon/search?evolvesFrom=Pikachu

# Find all cards that evolve to Charizard
GET /api/pokemon/search?evolvesTo=Charizard

# Find evolution cards with rule boxes that evolve from Eevee
GET /api/pokemon/search?evolvesFrom=Eevee&hasRuleBox=true
```

**Rule Text Search:**

```http
# Find all GX cards by searching rule text
GET /api/pokemon/search?ruleText=GX

# Find VMAX cards
GET /api/pokemon/search?ruleText=VMAX

# Combined: Fire-type Stage 2 cards weak to Water with abilities
GET /api/pokemon/search?types=Fire&subtypes=Stage%202&weaknessType=Water&hasAbility=true
```

## Future Enhancements

### Phase 4: Full-Text Search (PLANNED)

- **Full-Text Search**
    - PostgreSQL `tsvector` for text search across multiple fields
    - Search attack text, ability text, trainer text, rule text simultaneously
    - Relevance ranking

### Potential Optimizations

1. **Query Result Caching** - Cache frequent searches
2. **Database Views** - Pre-joined views for common queries
3. **Elasticsearch Integration** - For advanced full-text search
4. **GraphQL API** - Allow clients to request exactly what they need
5. **Materialized Views** - For filter options (updated periodically)

---

## Troubleshooting

### Common Issues

#### Issue: Duplicate Results

**Cause:** Many-to-many joins without DISTINCT

**Solution:** Ensure specifications that join collections call `query.distinct(true)`:

```java
if(query !=null){
        query.

distinct(true);
}
```

#### Issue: Case-Sensitive Searches

**Cause:** Forgot to use `LOWER()` in specification

**Solution:** Always use case-insensitive comparisons:

```java
return criteriaBuilder.like(
        criteriaBuilder.lower(root.get("name")),
        "%"+searchTerm.

toLowerCase() +"%"
        );
```

#### Issue: Null Pointer Exceptions

**Cause:** Filter parameter is null but not handled

**Solution:** Always check for null at the start of specifications:

```java
if(filterValue ==null||filterValue.

trim().

isEmpty()){
        return criteriaBuilder.

conjunction(); // Always true
}
```

#### Issue: Slow Queries

**Cause:** Missing indexes or inefficient joins

**Solution:**

1. Use PostgreSQL `EXPLAIN ANALYZE` to identify bottlenecks
2. Add indexes on frequently filtered columns
3. Consider using `hp_numeric` instead of parsing string `hp`

---

## Additional Resources

- [Spring Data JPA Specifications](https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/)
- [JPA Criteria API Guide](https://docs.oracle.com/javaee/7/tutorial/persistence-criteria.htm)
- [PostgreSQL Query Optimization](https://www.postgresql.org/docs/current/performance-tips.html)
- [Project API Documentation](API.md)
- [Project Architecture](ARCHITECTURE.md)

---

## Conclusion

The search implementation provides a solid foundation for comprehensive card filtering. The JPA Specifications pattern
enables:

- **Composability** - Filters can be combined flexibly
- **Type Safety** - Compile-time checks prevent errors
- **Maintainability** - Each filter is isolated and testable
- **Extensibility** - New filters are straightforward to add

Follow the patterns established in Phase 1 to add new filters in future phases. Always include tests and update
documentation when extending functionality.
