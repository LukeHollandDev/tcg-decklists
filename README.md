# TCG Decklists

Pokémon TCG deck builder and viewer with comprehensive card search, designed for multi-TCG extensibility.

## Project Structure

TODO: add the tree project structure in here explaining each directory's use.

## Features

- Provides comprehensive search for cards
- Build decks by selecting cards you need
- Import and export card games using supported formats
- Share decklists built on the website
- Download a printable decklist of your deck

## Technology Stack

- Frontend is a TypeScript, React 19 website using Vite
- Backend is a Java 21, Spring Boot 3.5 API
- Database is using PostgreSQL 18
- Data pipeline uses Python 3.12

## Quick Start

Start PostgreSQL database hosted at `localhost:5432`:

```shell
# Start PostgreSQL database
docker compose -f config/docker-compose.yml up -d
```

Download and load the card data into the database:

```shell
# Load card data (Python 3.12+ required)
cd tools/data-pipeline && ./run.sh
```

Start the API hosted at `http://localhost:8080`:

```shell
# Run backend (Java 21 required)
cd apps/backend && ./gradlew bootRun
```

Start the website hosted at `http://localhost:5173`

```shell
# Run frontend (Node.js 18+ required)
cd apps/frontend && npm install && npm run dev
```

## Card Data Source

Where possible, it's preferable to host the data by ingesting it into our database as it'll reduce costs if APIs are
paid and improve the performance of our own API. Additionally, it allows us to build many features we might not have
been able to do using a third party.

### Pokémon

The Pokémon card data is sourced from https://github.com/PokemonTCG/pokemon-tcg-data/ which is from the creator of
the [Pokémon TCG API](https://pokemontcg.io/). All credit for the actual card data and images goes to them.

## Additional Card Games

To enable adding new card games easily without major refactoring, care is being taken to ensure nothing is highly
specific to a particular card game.

- Feature-based API routing: `/api/pokemon`, `/api/yu-gi-oh`
- Database tables prefixed with card game: `pokemon_card`, `yugioh_card`
- The `/api/.../features` endpoint exposes the available filters and static data for the card game
