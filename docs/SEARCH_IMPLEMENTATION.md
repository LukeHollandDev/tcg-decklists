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

- **Text search** across card names (case-insensitive, partial matching)
- **Multiple filter types** (supertype, types, subtypes, set, rarity, HP range)
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

## Future Enhancements

### Phase 2: Extended Filters

- **Attack Filters**
    - `attackName` - Attack name search
    - `attackDamageMin` / `attackDamageMax` - Damage range
    - `attackCost` - Energy cost requirements

- **Ability Filters**
    - `hasAbility` - Boolean, cards with abilities
    - `abilityName` - Ability name search

- **Additional Filters**
    - `artist` - Artist name
    - `regulationMark` - Regulation mark
    - `retreatCostMin` / `retreatCostMax` - Retreat cost range
    - `format` - Format legality (Standard, Expanded, Unlimited)

### Phase 3: Advanced Features

- **Boolean Filters**
    - `hasRuleBox` - Cards with rule boxes
    - `hasWeakness` - Cards with weaknesses
    - `hasResistance` - Cards with resistances

- **Full-Text Search**
    - PostgreSQL `tsvector` for text search across multiple fields
    - Search attack text, ability text, trainer text simultaneously
    - Relevance ranking

- **Weakness/Resistance Filters**
    - `weaknessType` - Weakness type
    - `resistanceType` - Resistance type

- **Evolution Filters**
    - `evolvesFrom` - Evolution source
    - `evolvesTo` - Evolution target

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
