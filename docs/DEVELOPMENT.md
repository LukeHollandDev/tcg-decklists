# Development Guide

This guide covers everything you need to develop, test, and deploy TCG Decklists.

## Prerequisites

- **Java 21** - Backend development
- **Node.js 18+** - Frontend development
- **Docker & Docker Compose** - Database and containerization
- **Python 3.13+** - Data pipeline scripts
- **Git** - Version control

## Project Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd tcg-decklists
```

### 2. Start the Database

```bash
docker compose -f config/docker-compose.yml up -d
```

This starts PostgreSQL on `localhost:5432` with:

- Database: `tcg_decklists`
- Username: `postgres` (override with `DB_USER` env var)
- Password: `testing1234` (override with `DB_PASSWORD` env var)

### 3. Load Card Data

```bash
cd tools/data-pipeline
./run.sh
```

This will:

1. Check for updates from the upstream Pokémon TCG data repository
2. Download new data if available
3. Run the migration script to populate the database

## Development Commands

### Backend (Spring Boot)

```bash
cd apps/backend

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

The API will be available at `http://localhost:8080`

### Testing (Backend)

We use **TestContainers** with PostgreSQL for integration testing. See [TESTING.md](TESTING.md) for complete details.

#### Quick Test Commands

```bash
cd apps/backend

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "InfrastructureSmokeTest"

# Run with coverage report
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

#### Using Test Data Builders

**Phase 2 (Test Data Builders) and Phase 3 (Test Utilities) are complete!** Use the builders to create test data:

```java
// Use preset cards
Card charizard = CardBuilder.charizard().build();
Card pikachu = CardBuilder.pikachu().build();

// Create custom cards
Card customCard = CardBuilder.aCard()
    .withId("test-1")
    .withName("Test Card")
    .withSupertype("Pokémon")
    .withHp("100")
    .addType(TypeBuilder.fire().build())
    .addSubtype(SubtypeBuilder.basic().build())
    .build();

// Use custom matchers for assertions
assertThat(attackCosts, hasAtLeast(2, "Fire"));  // Multiset matching
assertThat("Flabébé", normalizedEquals("flabebe"));  // Accent-insensitive
```

**Available Builders**:
- `CardBuilder` - Full card entities with all relationships
- `AttackBuilder` - Attacks with multiset cost support
- `AbilityBuilder` - Abilities with type and text
- `TypeBuilder` - All 11 Pokémon types
- `WeaknessBuilder` / `ResistanceBuilder` - Weakness/resistance with values
- `SetBuilder`, `ArtistBuilder`, `RarityBuilder`, `SubtypeBuilder` - Supporting entities

**Test Utilities**:
- `CustomMatchers` - Hamcrest matchers for multiset matching and accent-insensitive comparison
- `TestUtils` - JSON parsing, pagination validation, and common test helpers

See **[TESTING.md](TESTING.md)** for complete usage examples and testing strategy.

### Frontend (React + Vite)

```bash
cd apps/frontend

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

The development server will be available at `http://localhost:5173`

### Database Management

#### Start/Stop Database

```bash
# Start database only
docker compose -f config/docker-compose.yml up -d

# Stop database
docker compose -f config/docker-compose.yml down

# Stop database and remove volumes (fresh start - RECOMMENDED for full cleanup)
docker compose -f config/docker-compose.yml down -v

# View database logs
docker compose -f config/docker-compose.yml logs -f database
```

#### Database Connection

You can connect to the database using any PostgreSQL client:

```bash
# Using psql in the Docker container
docker exec -it tcg-decklists-new-database-1 psql -U postgres -d tcg_decklists

# Using psql locally
psql -h localhost -p 5432 -U postgres -d tcg_decklists
```

#### Reset Database

If you need to completely reset the database (e.g., after modifying Liquibase migrations):

```bash
# 1. Stop and remove database volumes
docker compose -f config/docker-compose.yml down -v

# 2. Start fresh database
docker compose -f config/docker-compose.yml up -d

# 3. Run Spring Boot (migrations will run automatically)
cd apps/backend
./gradlew bootRun

# 4. Load card data
cd tools/data-pipeline
python scripts/pokemon-migrate.py
```

### Data Pipeline

#### Run Data Pipeline

```bash
cd tools/data-pipeline

# Run the full pipeline (checks for updates, downloads, migrates)
./run.sh
```

_The script creates the Python virtual environment within a temporary directory._

#### Force Re-migration

If you want to re-migrate data without waiting for upstream changes:

```bash
cd tools/data-pipeline

# Create and start virtual environment
python -m venv .venv && source .venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run the migrate script
python scripts/pokemon-migrate.py
```

## Testing

### Backend Tests

```bash
cd apps/backend

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "dev.lukeholland.tcg.decklists.api.controllers.PokemonCardControllerTest"

# Run with coverage
./gradlew test jacocoTestReport
```

### Frontend Tests

```bash
cd apps/frontend

# Tests are not yet implemented
# TODO: Add Vitest configuration
```

### API Testing with Bruno

1. Open Bruno and load the collection from `tools/api-testing/`
2. Ensure the backend is running
3. Run requests to test endpoints
4. Create new requests for new endpoints

## Troubleshooting

### Liquibase Checksum Errors

If you get a checksum validation error:

```
Validation Failed:
     1 change sets check sum
```

**Cause**: You modified a Liquibase migration that was already applied.

**Solution**: Reset the database (see [Reset Database](#reset-database))

### Port Already in Use

If port 8080 is already in use:

```bash
# Find the process using port 8080
lsof -ti:8080

# Kill the process
kill -9 <PID>
```

### Database Connection Failed

If Spring Boot can't connect to the database:

1. Check if the database is running: `docker ps`
2. Check the connection details in `apps/backend/src/main/resources/application.properties`
3. Verify environment variables if using custom credentials

## Git Workflow

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Make changes and commit regularly
3. Run tests before pushing
4. Push to remote and create a pull request
5. Merge after review

## Environment Variables

### Backend

- `DB_USER` - Database username (default: `postgres`)
- `DB_PASSWORD` - Database password (default: `testing1234`)
- `DB_HOST` - Database host (default: `localhost`)
- `DB_PORT` - Database port (default: `5432`)

### Data Pipeline

Environment variables can be passed directly:

```bash
DB_HOST=localhost DB_PORT=5432 ./run.sh
```

## CI/CD

TODO: add CI/CD GitHub Workflows to automatically run and test the code and the scheduled jobs to automatically pull
card data and update the database.

## Additional Resources

- [Architecture Documentation](ARCHITECTURE.md)
- [API Documentation](API.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [TailwindCSS Documentation](https://tailwindcss.com/)
- [Vite Documentation](https://vite.dev/)
