# TCG Decklists

A Pokémon TCG deck builder and viewer web application designed for extensibility to other trading card games.

## Quick Links

- **[Full Documentation](docs/README.md)** - Complete project overview and quick start
- **[Development Guide](docs/DEVELOPMENT.md)** - Setup, commands, and workflows
- **[Architecture](docs/ARCHITECTURE.md)** - System design and database schema
- **[API Documentation](docs/API.md)** - Endpoints and usage

## Quick Start

```bash
# Start database
docker compose -f config/docker-compose.yml up -d

# Run backend (Java 21 required)
cd apps/backend && ./gradlew bootRun

# Run frontend (Node.js 18+ required)
cd apps/frontend && npm install && npm run dev

# Load card data (Python 3.13+ required)
cd tools/data-pipeline && ./run.sh
```

## Project Structure

```
tcg-decklists/
├── docs/         # 📚 All documentation
├── apps/         # 🚀 Production applications (backend, frontend)
├── tools/        # 🛠️ Dev tools (data pipeline, API testing)
└── config/       # ⚙️ Configuration files
```

## Tech Stack

- **Backend**: Java 21 Spring Boot + PostgreSQL
- **Frontend**: React 19 + TypeScript + Vite + TailwindCSS v4
- **Data**: Self-hosted from [pokemon-tcg-data](https://github.com/PokemonTCG/pokemon-tcg-data)

For detailed information, see the **[full documentation](docs/README.md)**.
