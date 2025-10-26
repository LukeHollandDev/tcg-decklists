# TCG Decklists Data

This directory contains the data used for the card list. The data is sourced
from https://github.com/PokemonTCG/pokemon-tcg-data.

## Metadata

`metadata.json` records the latest data ingestion and is updated each time the pipeline runs to refresh the database.

```json
[
  {
    "name": "pokemon",
    "source": "https://github.com/PokemonTCG/pokemon-tcg-data",
    "context": "cards/en",
    "version": "37d13b7cd2ff04c41a319ecf9b5d854328a8a390",
    "successful": true,
    "timestamp": "2025-09-26T15:37:58Z"
  }
]
```

## Pipeline

```mermaid
flowchart TD
    A[Scheduled GitLab CI/CD Pipeline Run] --> B[Fetch current commit hash from PokemonTCG/pokemon-tcg-data repo]
    B --> C[Read stored commit_hash from data/metadata.json]
    C --> D{Hashes match?}
    D -->|Yes| E[No changes detected - end pipeline]
    D -->|No| F[Remove old data in data/pokemon]
    F --> G[Clone new data from PokemonTCG/pokemon-tcg-data repo]
    G --> H[Run scripts/pokemon-migrate.py to update database]
    H --> I{Migration successful?}
    I -->|No| J[Fail pipeline - rollback or abort]
    I -->|Yes| K[Update data/metadata.json with new commit_hash, success=true, and completion timestamp]
    K --> L[Commit and push updated metadata]
    L --> M[End pipeline]
```
