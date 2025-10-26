# TCG Decklists

A Pokémon TCG deck builder and viewer web application with plans to support multiple trading card games.

## Overview

TCG Decklists allows users to:

- Import decklists in standard format (PTCGL)
- Search for cards with comprehensive filtering
- Build and export custom decklists
- Share decklists via unique URLs (e.g., `lukeholland.dev/tcg-decklists/<id>[.jpeg]`)
- Export PDFs using official and custom decklist templates

## Technology Stack

- **Backend**: Java 21 Spring Boot REST API with PostgreSQL database
- **Frontend**: React 19 + TypeScript with Vite and TailwindCSS v4
- **Data Pipeline**: Self-hosted card data from [pokemon-tcg-data](https://github.com/PokemonTCG/pokemon-tcg-data)
- **Containerization**: Docker & Docker Compose
- **API Testing**: Bruno

## Project Structure

```
tcg-decklists/
├── docs/              # All project documentation
├── apps/              # Production applications
│   ├── backend/       # Spring Boot API
│   └── frontend/      # React application
├── tools/             # Development & operational tools
│   ├── data-pipeline/ # Card data ingestion
│   └── api-testing/   # Bruno API collection
└── config/            # Configuration files
```

## Quick Start

### Prerequisites

- Java 21
- Node.js 18+
- Docker & Docker Compose
- Python 3.13+ (for data pipeline)

### 1. Start the Database

```bash
docker compose -f config/docker-compose.yml up -d
```

### 2. Run the Backend

```bash
cd apps/backend
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

### 3. Run the Frontend

```bash
cd apps/frontend
npm install
npm run dev
```

The UI will be available at `http://localhost:5173`

### 4. Load Card Data

```bash
cd tools/data-pipeline
./run.sh
```

## Documentation

- **[Architecture](ARCHITECTURE.md)** - System design, database schema, and technical decisions
- **[Development](DEVELOPMENT.md)** - Development commands, workflows, and setup
- **[API](API.md)** - API endpoints and usage

## Why Self-Hosted Data?

The project initially planned to use the `pokemontcg.io` API with caching, but when that API switched to a paid model,
the architecture pivoted to cloning and self-hosting card data from https://github.com/PokemonTCG/pokemon-tcg-data. This
provides complete control over the data without rate limits or API costs.

## Multi-TCG Vision

While currently focused on Pokémon TCG, the architecture is designed to support other trading card games (Yu-Gi-Oh,
Magic: The Gathering, etc.) in the future. The API uses a `<type>` parameter pattern to enable this extensibility.
