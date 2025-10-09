# TCG Decklists Data

This directory contains the data used for the card list. The data is sourced
from https://github.com/PokemonTCG/pokemon-tcg-data.

## Metadata

`metadata.json` keeps track of the most recent ingestion of the data and is
updated whenever the pipeline is run to update the database.

```json
{
    "url": "https://github.com/PokemonTCG/pokemon-tcg-data",
    "commit_hash": "37d13b7cd2ff04c41a319ecf9b5d854328a8a390",
    "last_updated": "2025-10-09 23:59:59.123456+00:00",
    "successful": "true"
}
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
    G --> H[Run data/migrate.sh to update database]

    H --> I{Migration successful?}
    I -->|No| J[Fail pipeline - rollback or abort]
    I -->|Yes| K[Update data/metadata.json with new commit_hash, success=true, and completion timestamp]

    K --> L[Commit and push updated metadata]
    L --> M[End pipeline]
```
