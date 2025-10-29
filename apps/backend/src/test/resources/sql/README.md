# SQL Test Fixtures

This directory contains SQL scripts with test data for integration tests.

## Files

### test-data.sql (To be created)

Main test data file containing comprehensive Pokemon card test data including:

- Cards with various types (Fire, Water, Grass, etc.)
- Cards with various subtypes (Basic, Stage 1, Stage 2, ex, V, VMAX, etc.)
- Cards with accent characters (e.g., Flabébé, Pokémon)
- Evolution chains (e.g., Charmander → Charmeleon → Charizard)
- Cards with attacks (various costs, damage, descriptions)
- Cards with abilities
- Cards with weaknesses and resistances
- Cards across multiple sets and regulation marks
- Cards in different formats (Standard, Expanded, Unlimited)
- Cards by various artists
- Edge cases (free retreat, no HP, etc.)

## Usage

Test data can be loaded using the `TestDataLoader` utility:

```java
@Autowired
protected TestDataLoader testDataLoader;

@BeforeEach
void setUp() {
    // Load main test data
    testDataLoader.loadTestData();

    // Or load custom SQL file
    testDataLoader.loadCustomSqlFile("custom-test-data.sql");
}
```

## Creating Test Data

When creating test data SQL files:

1. Use realistic card data from actual Pokemon TCG sets when possible
2. Include edge cases that might not exist in production data
3. Use descriptive card names to make tests easier to understand
4. Include comments explaining what each section tests
5. Ensure data covers all test scenarios in the test plan

## Notes

- Test data is automatically rolled back after each test due to `@Transactional`
- TestContainers provides a fresh database for each test run
- Reference data (types, subtypes, sets, etc.) should be included in fixtures
