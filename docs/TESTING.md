# Testing Guide

This guide provides a comprehensive overview of the testing strategy, infrastructure, and implementation plan for the TCG Decklists backend.

## Table of Contents

- [Implementation Progress](#implementation-progress)
- [Testing Philosophy](#testing-philosophy)
- [Test Organization](#test-organization)
- [Running Tests](#running-tests)
- [Test Infrastructure](#test-infrastructure)
- [Test Data Builders](#test-data-builders)
- [Integration Tests](#integration-tests)
- [Unit Tests](#unit-tests)
- [Test Coverage Goals](#test-coverage-goals)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

## Implementation Progress

Track the progress of implementing the comprehensive test suite:

### Phase 1: Infrastructure Setup
- [x] Add TestContainers dependencies to `build.gradle`
- [x] Create `AbstractIntegrationTest.java` (base class with TestContainers)
- [x] Create `TestConfig.java` (test-specific Spring configuration)
- [x] Create `TestDataLoader.java` (utility to load SQL fixtures)

### Phase 2: Test Data Builders ✅ COMPLETED
- [x] Create `CardBuilder.java` with fluent API and common presets
- [x] Create `AttackBuilder.java` for attack data
- [x] Create `AbilityBuilder.java` for ability data
- [x] Create `TypeBuilder.java` for type data
- [x] Create `WeaknessBuilder.java` and `ResistanceBuilder.java` for weakness/resistance data
- [x] Create `SetBuilder.java` for set data
- [x] Create `ArtistBuilder.java`, `RarityBuilder.java`, `SubtypeBuilder.java` for supporting entities

### Phase 3: Test Utilities ✅ COMPLETED
- [x] Create `TestUtils.java` with common assertions and helpers
- [x] Create `CustomMatchers.java` with Hamcrest matchers for complex validations
- [x] Add accent normalization test helpers
- [x] Add multiset matching validators

### Phase 4: SQL Test Fixtures ✅ COMPLETED
- [x] Create `test-data.sql` with comprehensive test card data
- [x] Include cards with accent characters (Flabébé)
- [x] Include evolution chains (Charmander → Charmeleon → Charizard, Squirtle → Wartortle → Blastoise)
- [x] Include cards across all types, subtypes, and rarities
- [x] Include cards with various attack costs (including multiset scenarios: 4x Fire, 2x Lightning + 1x Colorless, 2x Water, 3x Water)
- [x] Include regulation marks (E, G)
- [x] Include format legality variations (Standard, Expanded, Unlimited)
- [x] Include banned cards (Professor Oak)
- [x] Include rule box cards (Charizard ex)
- [x] Include trainer and energy cards
- [x] Create TestDataValidationTest with 5 validation tests
- [x] Fix schema alignment issues (evolution_direction → direction)
- [x] Fix test isolation issues

### Phase 5: Integration Tests (11 test classes) - IN PROGRESS (8/11 completed - 163 tests passing ✅)
- [x] `CardBasicOperationsIntegrationTest.java` (10 tests) ✅ **COMPLETED**
- [x] `CardSearchCoreFiltersIntegrationTest.java` (32 tests) ✅ **COMPLETED**
- [x] `CardSearchAttackFiltersIntegrationTest.java` (26 tests) ✅ **COMPLETED**
- [x] `CardSearchAbilityFiltersIntegrationTest.java` (15 tests) ✅ **COMPLETED**
- [x] `CardSearchDetailFiltersIntegrationTest.java` (23 tests) ✅ **COMPLETED**
- [x] `CardSearchBooleanEvolutionIntegrationTest.java` (24 tests) ✅ **COMPLETED**
- [x] `CardSearchWeaknessResistanceIntegrationTest.java` (14 tests) ✅ **COMPLETED**
- [x] `CardSearchComplexScenariosIntegrationTest.java` (19 tests) ✅ **COMPLETED**
- [ ] `CardFeaturesAndAutocompleteIntegrationTest.java` (38 tests)
- [ ] `CardErrorHandlingIntegrationTest.java` (10 tests)
- [ ] `CardPaginationSortingIntegrationTest.java` (15 tests)

### Phase 6: Unit Tests (3 test classes)
- [ ] `CardServiceUnitTest.java` (service layer logic)
- [ ] `CardSpecificationUnitTest.java` (JPA Criteria queries, multiset matching)
- [ ] `CardRepositoryUnitTest.java` (custom JPQL queries)

### Phase 7: Final Steps
- [ ] Delete existing `ControllerIntegrationTest.java`
- [ ] Run all tests and verify they pass
- [ ] Generate coverage report with `./gradlew test jacocoTestReport`
- [ ] Optimize test execution time (parallel execution, TestContainers reuse)

## Testing Philosophy

Our testing strategy follows these principles:

1. **Comprehensive Coverage** - Test all endpoints, all filter parameters, and edge cases
2. **Isolation** - Tests run in isolated environments using TestContainers
3. **Clarity** - Test names clearly describe what is being tested
4. **Maintainability** - Use builders and utilities to keep tests DRY and readable
5. **Speed** - Balance thorough testing with reasonable execution time (target: < 5 minutes)
6. **Real Behavior** - Integration tests exercise the full stack; unit tests validate complex logic

## Test Organization

### Test Structure

Tests are organized into **14 test classes** across two categories:

#### Integration Tests (11 classes, ~225 tests)

Located in `apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/integration/`

1. **`CardBasicOperationsIntegrationTest.java`** (10 tests)
   - GET /api/pokemon/{id}

2. **`CardSearchCoreFiltersIntegrationTest.java`** (35 tests)
   - Core filters: name, supertype, types, subtypes, setId, rarity, HP

3. **`CardSearchAttackFiltersIntegrationTest.java`** (30 tests)
   - Attack filters: name, text, damage, cost (including multiset matching)

4. **`CardSearchAbilityFiltersIntegrationTest.java`** (15 tests)
   - Ability filters: hasAbility, name, text

5. **`CardSearchDetailFiltersIntegrationTest.java`** (25 tests)
   - Detail filters: artist, regulation mark, retreat cost, formats

6. **`CardSearchBooleanEvolutionIntegrationTest.java`** (27 tests)
   - Boolean filters: hasRuleBox, hasWeakness, hasResistance
   - Evolution filters: evolvesFrom, evolvesTo, ruleText

7. **`CardSearchWeaknessResistanceIntegrationTest.java`** (15 tests)
   - Weakness/resistance type filters with AND/OR logic

8. **`CardSearchComplexScenariosIntegrationTest.java`** (20 tests)
   - Complex multi-filter combinations
   - Real-world search scenarios

9. **`CardFeaturesAndAutocompleteIntegrationTest.java`** (38 tests)
   - Features endpoint validation
   - All 5 autocomplete endpoints

10. **`CardErrorHandlingIntegrationTest.java`** (10 tests)
    - Error responses (400, 404, 405)
    - Edge cases and validation

11. **`CardPaginationSortingIntegrationTest.java`** (15 tests)
    - Pagination edge cases
    - Sorting with various fields
    - Metadata validation

#### Unit Tests (3 classes)

Located in `apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/unit/`

1. **`CardServiceUnitTest.java`**
   - Service layer business logic
   - Request validation and transformation

2. **`CardSpecificationUnitTest.java`**
   - JPA Criteria query generation
   - Complex logic: multiset matching, accent normalization
   - AND/OR logic for multi-value filters

3. **`CardRepositoryUnitTest.java`**
   - Custom JPQL queries
   - Autocomplete prefix/substring matching

### File Structure

```
apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/
├── integration/ (NOT YET CREATED)
│   ├── CardBasicOperationsIntegrationTest.java
│   ├── CardSearchCoreFiltersIntegrationTest.java
│   ├── CardSearchAttackFiltersIntegrationTest.java
│   ├── CardSearchAbilityFiltersIntegrationTest.java
│   ├── CardSearchDetailFiltersIntegrationTest.java
│   ├── CardSearchBooleanEvolutionIntegrationTest.java
│   ├── CardSearchWeaknessResistanceIntegrationTest.java
│   ├── CardSearchComplexScenariosIntegrationTest.java
│   ├── CardFeaturesAndAutocompleteIntegrationTest.java
│   ├── CardErrorHandlingIntegrationTest.java
│   └── CardPaginationSortingIntegrationTest.java
├── unit/ (NOT YET CREATED)
│   ├── CardServiceUnitTest.java
│   ├── CardSpecificationUnitTest.java
│   └── CardRepositoryUnitTest.java
├── builders/ ✅ COMPLETED
│   ├── CardBuilder.java
│   ├── AttackBuilder.java
│   ├── AbilityBuilder.java
│   ├── TypeBuilder.java
│   ├── WeaknessBuilder.java
│   ├── ResistanceBuilder.java
│   ├── SetBuilder.java
│   ├── ArtistBuilder.java
│   ├── RarityBuilder.java
│   └── SubtypeBuilder.java
├── config/ ✅ COMPLETED
│   ├── AbstractIntegrationTest.java
│   ├── TestConfig.java
│   └── TestDataLoader.java
└── testutils/ ✅ COMPLETED
    ├── TestUtils.java
    └── CustomMatchers.java
```

## Running Tests

### Prerequisites

- **Java 21** - Required for running tests
- **Docker** - Required for TestContainers (PostgreSQL)

### Run All Tests

```bash
cd apps/backend

# Run all tests (integration + unit)
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport
```

### Run Specific Test Suites

```bash
# Run only integration tests
./gradlew test --tests "*.integration.*"

# Run only unit tests
./gradlew test --tests "*.unit.*"

# Run specific test class
./gradlew test --tests "CardSearchAttackFiltersIntegrationTest"

# Run specific test method
./gradlew test --tests "CardSearchAttackFiltersIntegrationTest.shouldFindCardsWithMultisetAttackCostMatching"
```

### Run Tests in Parallel

```bash
# Enable parallel test execution (faster but uses more resources)
./gradlew test --parallel --max-workers=4
```

### Test Categories

Tests can be categorized using JUnit tags:

```bash
# Run smoke tests only (critical paths)
./gradlew test --tests "*" -Dtags="smoke"

# Run slow tests separately
./gradlew test --tests "*" -Dtags="slow"
```

## Test Infrastructure

### TestContainers Setup

We use **TestContainers** to spin up an isolated PostgreSQL instance for each test run. This ensures:

- Tests run in complete isolation
- No interference with production database
- Consistent test data across environments
- Parallel test execution support

#### Configuration

**`AbstractIntegrationTest.java`** - Base class for all integration tests:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("tcg_decklists_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
```

#### Dependencies

Add to `apps/backend/build.gradle`:

```gradle
dependencies {
    // Existing dependencies...

    // TestContainers
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:postgresql:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
}
```

### Test Data Loading

**`TestDataLoader.java`** - Utility to load SQL fixtures:

```java
@Component
public class TestDataLoader {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void loadTestData() {
        // Load SQL fixtures from test-data.sql
        Resource resource = new ClassPathResource("sql/test-data.sql");
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), resource);
    }
}
```

**Test Data SQL** - Located at `apps/backend/src/test/resources/sql/test-data.sql`:

Contains known test cards covering:
- Various types (Fire, Water, Grass, etc.)
- Various subtypes (Basic, Stage 1, Stage 2, ex, V, VMAX, Trainer, Energy)
- Accent characters (Flabébé, Pokémon)
- Attack cost variations (free attacks, single type, multi-type, duplicates)
- Evolution chains (Charmander → Charmeleon → Charizard)
- All regulation marks (A-H)
- Multiple formats (Standard, Expanded, Unlimited)
- Various rarities
- Multiple artists

## Test Data Builders

We use the **Builder pattern** to create test data fluently and readably. All builders support method chaining and properly handle JPA entity relationships.

### CardBuilder

**Location**: `apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/builders/CardBuilder.java`

**Features**:
- Fluent API with method chaining
- Common presets for popular cards (Charizard, Pikachu, Prof. Oak, Fire Energy)
- Properly handles bidirectional JPA relationships
- Supports all card properties and relationships

**Usage**:

```java
// Use presets for common cards (fully configured)
Card charizard = CardBuilder.charizard().build();
Card pikachu = CardBuilder.pikachu().build();
Card professorOak = CardBuilder.professorOak().build();
Card fireEnergy = CardBuilder.fireEnergy().build();

// Create a custom card with all relationships
Type fire = TypeBuilder.fire().build();
Type water = TypeBuilder.water().build();
Subtype stage2 = SubtypeBuilder.stage2().build();
Set baseSet = SetBuilder.baseSet().build();
Artist artist = ArtistBuilder.kenSugimori().build();
Rarity rarity = RarityBuilder.rareHolo().build();

Attack attack = AttackBuilder.fireBlast()
    .addCost(fire, 3)
    .addCost(TypeBuilder.colorless().build(), 1)
    .build();

Card customCard = CardBuilder.aCard()
    .withId("test-1")
    .withName("Custom Charizard")
    .withSupertype("Pokémon")
    .withHp("150")
    .withHpNumeric(150)
    .withNumber("1")
    .addType(fire)
    .addSubtype(stage2)
    .withSet(baseSet)
    .withArtist(artist)
    .withRarity(rarity)
    .addAttack(attack)
    .addWeakness(WeaknessBuilder.times2(water).build())
    .addResistance(ResistanceBuilder.minus30(TypeBuilder.fighting().build()).build())
    .addRetreatCost(TypeBuilder.colorless().build(), 3)
    .withConvertedRetreatCost(3)
    .build();
```

### AttackBuilder

**Location**: `apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/builders/AttackBuilder.java`

**Features**:
- Supports attack costs with multiset matching (e.g., 2x Fire + 1x Water)
- Properly creates AttackCost junction table entities
- Preset attacks (Fire Blast, Thunderbolt, Tackle, etc.)

**Usage**:

```java
// Use preset attacks
Attack fireBlast = AttackBuilder.fireBlast().build();
Attack thunderbolt = AttackBuilder.thunderbolt().build();

// Create custom attack with specific costs
Type fire = TypeBuilder.fire().build();
Type colorless = TypeBuilder.colorless().build();

Attack customAttack = AttackBuilder.anAttack()
    .withName("Inferno")
    .withDamage("150")
    .withDamageNumeric(150)
    .withText("Discard 2 Fire Energy from this Pokémon.")
    .addCost(fire, 2)        // 2x Fire
    .addCost(colorless, 1)   // 1x Colorless
    .build();
```

### AbilityBuilder

**Location**: `apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/builders/AbilityBuilder.java`

**Features**:
- Preset abilities (Blaze, Intimidate, Power Draw, etc.)
- Supports all ability types (Ability, Poké-Power, Poké-Body)

**Usage**:

```java
// Use preset abilities
Ability blaze = AbilityBuilder.blaze().build();
Ability intimidate = AbilityBuilder.intimidate().build();

// Create custom ability
Ability customAbility = AbilityBuilder.anAbility()
    .withName("Energy Boost")
    .withType("Ability")
    .withText("Once during your turn, you may attach an Energy card from your hand to this Pokémon.")
    .build();
```

### TypeBuilder

**Location**: `apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/builders/TypeBuilder.java`

**Features**:
- Factory methods for all 11 Pokémon types
- Custom type creation for test scenarios

**Usage**:

```java
// Use preset types
Type fire = TypeBuilder.fire().build();
Type water = TypeBuilder.water().build();
Type grass = TypeBuilder.grass().build();

// All 11 types available:
// fire(), water(), grass(), lightning(), fighting(), psychic(),
// colorless(), darkness(), metal(), dragon(), fairy()
```

### WeaknessBuilder & ResistanceBuilder

**Locations**:
- `apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/builders/WeaknessBuilder.java`
- `apps/backend/src/test/java/dev/lukeholland/tcg/decklists/api/pokemon/builders/ResistanceBuilder.java`

**Features**:
- Factory methods for common values (×2, +10, +20, -20, -30)
- Custom value configuration

**Usage**:

```java
// Weakness
Type water = TypeBuilder.water().build();
Weakness weakness = WeaknessBuilder.times2(water).build();  // ×2 multiplier
Weakness oldWeakness = WeaknessBuilder.plus20(water).build();  // +20 modifier

// Resistance
Type fighting = TypeBuilder.fighting().build();
Resistance resistance = ResistanceBuilder.minus30(fighting).build();  // -30 modifier
Resistance resistance2 = ResistanceBuilder.minus20(fighting).build();  // -20 modifier
```

### SetBuilder, ArtistBuilder, RarityBuilder, SubtypeBuilder

**Features**:
- Factory methods for common values
- Preset configurations for popular sets, artists, rarities, subtypes

**Usage**:

```java
// Sets
Set baseSet = SetBuilder.baseSet().build();
Set jungle = SetBuilder.jungle().build();
Set swordAndShield = SetBuilder.swordAndShield().build();

// Artists
Artist kenSugimori = ArtistBuilder.kenSugimori().build();
Artist mitsuhiroArita = ArtistBuilder.mitsuhiroArita().build();

// Rarities
Rarity common = RarityBuilder.common().build();
Rarity rareHolo = RarityBuilder.rareHolo().build();
Rarity rareUltra = RarityBuilder.rareUltra().build();

// Subtypes
Subtype basic = SubtypeBuilder.basic().build();
Subtype stage1 = SubtypeBuilder.stage1().build();
Subtype ex = SubtypeBuilder.ex().build();
Subtype item = SubtypeBuilder.item().build();
```

### Complete Example

Here's a complete example creating a fully configured card:

```java
// Create all required entities
Type fire = TypeBuilder.fire().build();
Type colorless = TypeBuilder.colorless().build();
Subtype stage2 = SubtypeBuilder.stage2().build();
Set baseSet = SetBuilder.baseSet().build();
Artist artist = ArtistBuilder.mitsuhiroArita().build();
Rarity rarity = RarityBuilder.rareHolo().build();

// Create attack with costs
Attack fireBlast = AttackBuilder.anAttack()
    .withName("Fire Blast")
    .withDamage("120")
    .withDamageNumeric(120)
    .withText("Discard 1 Fire Energy from this Pokémon.")
    .addCost(fire, 4)  // 4x Fire energy
    .build();

// Create the card with all relationships
Card charizard = CardBuilder.aCard()
    .withId("base1-4")
    .withName("Charizard")
    .withSupertype("Pokémon")
    .withHp("120")
    .withHpNumeric(120)
    .withNumber("4")
    .addType(fire)
    .addSubtype(stage2)
    .withSet(baseSet)
    .withArtist(artist)
    .withRarity(rarity)
    .addAttack(fireBlast)
    .addWeakness(WeaknessBuilder.times2(TypeBuilder.water().build()).build())
    .addResistance(ResistanceBuilder.minus30(TypeBuilder.fighting().build()).build())
    .addRetreatCost(colorless, 3)
    .withConvertedRetreatCost(3)
    .build();
```

## Integration Tests

### Test Anatomy

All integration tests extend `AbstractIntegrationTest` and use MockMvc:

```java
@DisplayName("Pokemon Card Search - Attack Filters")
class CardSearchAttackFiltersIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should find cards with attack name containing query (partial match)")
    void shouldFindCardsByAttackNamePartialMatch() throws Exception {
        mockMvc.perform(get("/api/pokemon/search")
                .param("attackName", "Thunder"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results").isArray())
            .andExpect(jsonPath("$.results[*].attacks[*].name", hasItem(containsStringIgnoringCase("Thunder"))));
    }
}
```

### Key Testing Patterns

#### 1. Basic Filter Tests

```java
@Test
void shouldFilterByType() throws Exception {
    mockMvc.perform(get("/api/pokemon/search")
            .param("types", "Fire"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[*].types[*]", everyItem(hasItem("Fire"))));
}
```

#### 2. AND/OR Logic Tests

```java
@Test
void shouldFilterByMultipleTypesWithOrLogic() throws Exception {
    // Default OR logic - cards with Fire OR Water
    mockMvc.perform(get("/api/pokemon/search")
            .param("types", "Fire")
            .param("types", "Water"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[*].types[*]",
            everyItem(anyOf(hasItem("Fire"), hasItem("Water")))));
}

@Test
void shouldFilterByMultipleTypesWithAndLogic() throws Exception {
    // AND logic - cards with BOTH Fire AND Water
    mockMvc.perform(get("/api/pokemon/search")
            .param("types", "Fire")
            .param("types", "Water")
            .param("typesMatchAll", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[*].types[*]",
            everyItem(allOf(hasItem("Fire"), hasItem("Water")))));
}
```

#### 3. Multiset Attack Cost Matching

**Critical Test** - Validates complex attack cost filtering:

```java
@Test
@DisplayName("Should find cards with at least 2x Fire energy in attack cost (multiset matching)")
void shouldFindCardsWithMultisetAttackCostMatching() throws Exception {
    // Searching for 2x Fire should match:
    // - [Fire, Fire]
    // - [Fire, Fire, Water]
    // - [Fire, Fire, Colorless, Colorless]
    // But NOT:
    // - [Fire]
    // - [Fire, Water]

    mockMvc.perform(get("/api/pokemon/search")
            .param("attackCost", "Fire")
            .param("attackCost", "Fire")
            .param("attackCostMatchAll", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[*].attacks[*].cost",
            everyItem(hasAtLeast(2, "Fire"))));
}

@Test
@DisplayName("Should find cards with at least 2x Fire + 1x Water (multiset subset matching)")
void shouldFindCardsWithMultisetSubsetMatching() throws Exception {
    // Searching for 2x Fire + 1x Water should match:
    // - [Fire, Fire, Water]
    // - [Fire, Fire, Water, Colorless]
    // But NOT:
    // - [Fire, Fire]
    // - [Fire, Water, Water]

    mockMvc.perform(get("/api/pokemon/search")
            .param("attackCost", "Fire")
            .param("attackCost", "Fire")
            .param("attackCost", "Water")
            .param("attackCostMatchAll", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[*].attacks[*].cost",
            everyItem(hasAtLeastMultiset(Map.of("Fire", 2, "Water", 1)))));
}
```

#### 4. Accent-Insensitive Matching

```java
@Test
@DisplayName("Should find cards with accented names (accent-insensitive)")
void shouldFindCardsWithAccentInsensitiveMatch() throws Exception {
    // Searching for "flabebe" should find "Flabébé"
    mockMvc.perform(get("/api/pokemon/search")
            .param("name", "flabebe"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[*].name", hasItem("Flabébé")));
}
```

#### 5. Pagination Tests

```java
@Test
@DisplayName("Should paginate results correctly")
void shouldPaginateResults() throws Exception {
    // First page
    MvcResult page1 = mockMvc.perform(get("/api/pokemon/search")
            .param("page", "0")
            .param("pageSize", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentPage").value(0))
        .andExpect(jsonPath("$.pageSize").value(20))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.hasPrevious").value(false))
        .andReturn();

    // Second page
    mockMvc.perform(get("/api/pokemon/search")
            .param("page", "1")
            .param("pageSize", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentPage").value(1))
        .andExpect(jsonPath("$.hasPrevious").value(true));
}
```

#### 6. Error Handling Tests

```java
@Test
@DisplayName("Should return 404 for non-existent card")
void shouldReturn404ForNonExistentCard() throws Exception {
    mockMvc.perform(get("/api/pokemon/invalid-id"))
        .andExpect(status().isNotFound());
}

@Test
@DisplayName("Should return 400 for autocomplete with missing query parameter")
void shouldReturn400ForMissingQueryParameter() throws Exception {
    mockMvc.perform(get("/api/pokemon/artists"))
        .andExpect(status().isBadRequest());
}
```

## Unit Tests

### Service Layer Tests

**`CardServiceUnitTest.java`** - Tests service layer logic in isolation using mocks:

```java
@ExtendWith(MockitoExtension.class)
class CardServiceUnitTest {

    @Mock
    private Repository repository;

    @InjectMocks
    private Service service;

    @Test
    void shouldBuildSpecificationForSearchRequest() {
        // Test that service correctly builds JPA Specifications
        // from search request parameters
    }

    @Test
    void shouldValidatePageSizeDoesNotExceedMaximum() {
        // Test that pageSize > 100 is capped at 100
    }
}
```

### Specification Tests

**`CardSpecificationUnitTest.java`** - Tests complex JPA Criteria query logic:

```java
@ExtendWith(MockitoExtension.class)
class CardSpecificationUnitTest {

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<?> criteriaQuery;

    @Mock
    private Root<Card> root;

    @Test
    void shouldCreateMultisetAttackCostPredicate() {
        // Test that multiset matching generates correct subquery
        // for attack cost filtering
    }

    @Test
    void shouldNormalizeAccents() {
        // Test accent normalization logic
        assertEquals("flabebe", CardSpecification.normalizeAccents("Flabébé"));
    }
}
```

### Repository Tests

**`CardRepositoryUnitTest.java`** - Tests custom JPQL queries:

```java
@DataJpaTest
class CardRepositoryUnitTest {

    @Autowired
    private Repository repository;

    @Test
    void shouldFindArtistsByPrefixFirst() {
        // Test that prefix matches are prioritized
        List<String> results = repository.findArtistsByQuery("ken", 10);

        // "Ken Sugimori" should come before "Masakazu Fukuda"
        assertTrue(results.get(0).startsWith("Ken"));
    }
}
```

## Test Coverage Goals

### Overall Coverage Targets

- **Line Coverage**: 85%+
- **Branch Coverage**: 80%+
- **Method Coverage**: 90%+

### Endpoint Coverage

- ✅ **100% endpoint coverage** - All 9 endpoints tested
- ✅ **100% parameter coverage** - All 31 search parameters tested
- ✅ **Complex scenarios** - Multi-filter combinations validated

### Critical Logic Coverage

Must have 100% coverage for:
- Multiset attack cost matching logic
- Accent normalization
- AND/OR logic for multi-value filters
- Pagination metadata calculation
- Autocomplete prefix/substring matching

## Best Practices

### 1. Test Naming Conventions

Use descriptive test names following this pattern:

```java
@Test
@DisplayName("Should [expected behavior] when [condition]")
void should[ExpectedBehavior]When[Condition]() {
    // Test implementation
}
```

Examples:
- `shouldReturnCardWhenValidIdProvided()`
- `shouldReturn404WhenCardDoesNotExist()`
- `shouldFindCardsWithMultisetAttackCostMatching()`

### 2. Arrange-Act-Assert Pattern

Structure tests clearly:

```java
@Test
void shouldFilterByHpRange() throws Exception {
    // Arrange - setup test data (if needed)
    int minHp = 80;
    int maxHp = 120;

    // Act - perform the action
    MvcResult result = mockMvc.perform(get("/api/pokemon/search")
            .param("hpMin", String.valueOf(minHp))
            .param("hpMax", String.valueOf(maxHp)))
        .andExpect(status().isOk())
        .andReturn();

    // Assert - verify expectations
    CardSearchResponse response = objectMapper.readValue(
        result.getResponse().getContentAsString(),
        CardSearchResponse.class
    );

    assertThat(response.getResults())
        .allMatch(card -> card.getHpNumeric() >= minHp && card.getHpNumeric() <= maxHp);
}
```

### 3. Use Custom Matchers

Create reusable Hamcrest matchers for complex assertions:

```java
// CustomMatchers.java
public static Matcher<List<String>> hasAtLeast(int count, String value) {
    return new TypeSafeMatcher<List<String>>() {
        @Override
        protected boolean matchesSafely(List<String> items) {
            return Collections.frequency(items, value) >= count;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("list containing at least ")
                .appendValue(count)
                .appendText(" occurrences of ")
                .appendValue(value);
        }
    };
}
```

### 4. Test Data Isolation

Each test should be independent:

```java
@BeforeEach
void setUp() {
    // Load fresh test data for each test
    testDataLoader.loadTestData();
}

@AfterEach
void tearDown() {
    // Cleanup is handled automatically by @Transactional rollback
}
```

### 5. Avoid Test Interdependence

Tests should not depend on execution order:

```java
// ❌ BAD - depends on previous test
@Test
void testA() {
    // Creates card with id "test-1"
}

@Test
void testB() {
    // Assumes card "test-1" exists from testA
}

// ✅ GOOD - each test is independent
@Test
void testA() {
    Card card = cardBuilder.id("test-a").build();
    repository.save(card);
    // Test logic
}

@Test
void testB() {
    Card card = cardBuilder.id("test-b").build();
    repository.save(card);
    // Test logic
}
```

### 6. Test Edge Cases

Always test boundary conditions:

```java
@Test
void shouldHandleZeroHp() { /* ... */ }

@Test
void shouldHandleNullRetreatCost() { /* ... */ }

@Test
void shouldHandleEmptyAttackCost() { /* ... */ }

@Test
void shouldHandlePageBeyondTotalPages() { /* ... */ }
```

### 7. Use Meaningful Assertions

Prefer specific assertions over generic ones:

```java
// ❌ BAD
assertTrue(response.getResults().size() > 0);

// ✅ GOOD
assertThat(response.getResults())
    .isNotEmpty()
    .hasSize(expectedCount);

// ✅ EVEN BETTER
assertThat(response.getResults())
    .extracting(Card::getName)
    .containsExactlyInAnyOrder("Charizard", "Pikachu", "Mewtwo");
```

## Troubleshooting

### Tests Fail to Start

**Symptom**: Tests fail with "Could not start container"

**Cause**: Docker is not running or TestContainers cannot connect

**Solution**:
```bash
# Ensure Docker is running
docker ps

# Check Docker daemon is accessible
docker info
```

### Slow Test Execution

**Symptom**: Tests take > 5 minutes to run

**Solutions**:
1. Enable parallel test execution:
   ```bash
   ./gradlew test --parallel --max-workers=4
   ```

2. Use test categories to run subsets:
   ```bash
   ./gradlew test --tests "*.integration.*" -Dtags="smoke"
   ```

3. Consider increasing TestContainers startup performance:
   ```java
   @Container
   static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
       .withReuse(true); // Reuse container across test runs
   ```

### Flaky Tests

**Symptom**: Tests pass sometimes, fail other times

**Common Causes**:
1. Test interdependence (test order matters)
2. Insufficient test data cleanup
3. Race conditions in parallel execution
4. Time-sensitive assertions

**Solutions**:
- Ensure each test is independent
- Use `@Transactional` for automatic rollback
- Avoid parallel execution for tests that share state
- Use `awaitility` for async operations

### TestContainers Port Conflicts

**Symptom**: "Port already in use" errors

**Solution**:
```bash
# Find and kill processes using the port
lsof -ti:5432 | xargs kill -9

# Or let TestContainers use dynamic ports (default behavior)
```

### Database Migration Errors

**Symptom**: Liquibase errors during test startup

**Cause**: Liquibase migrations have issues

**Solution**:
```bash
# Ensure migrations are valid
cd apps/backend
./gradlew bootRun

# If migrations work in production but not tests,
# check TestContainers PostgreSQL version matches production
```

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [TestContainers Documentation](https://testcontainers.com/guides/getting-started-with-testcontainers-for-java/)
- [Hamcrest Matchers](https://hamcrest.org/JavaHamcrest/tutorial)
- [MockMvc Documentation](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)

## Next Steps

1. **Implement Test Infrastructure** - Create base classes, configuration, and TestContainers setup
2. **Create Test Data Builders** - Implement Builder pattern for Card, Attack, Ability, etc.
3. **Write SQL Test Fixtures** - Create comprehensive test data SQL file
4. **Implement Integration Tests** - Start with basic operations, then move to complex filters
5. **Implement Unit Tests** - Test complex logic in isolation
6. **Verify Coverage** - Run coverage reports and fill gaps
7. **Optimize Performance** - Tune parallel execution and TestContainers configuration

For implementation details, see the [Development Guide](DEVELOPMENT.md).
