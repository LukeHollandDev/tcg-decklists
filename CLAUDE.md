# TCG Decklist Agentic Documentation

This document is to provide important information for any agentic AI working with this codebase.

## Project Overview

@README.md

## Useful Command

Here are the commands used for running the codebase as well as creating a fresh environment.

### Database

```shell
# create a postgresql container
docker compose -f config/docker-compose.yml up -d
```

```shell
# remove the postgresql container -- including the volume (removes data)
docker compose -f config/docker-compose.yml down -v
```

```shell
# can use psql cli directly on the container to execute sql
docker exec -it tcg-decklists-database-1 psql -U postgres -d tcg_decklists
```

### Backend

```shell
# start backend application, hosted on localhost:8080
cd apps/backend && ./gradlew bootRun
```

```shell
# stop backend application, get pid and then kill process
lsof -i:8080
kill <pid>
```

### Data Pipeline

```shell
# using the shell script, it decides if the migrate should be run based on the tools/data-pipeline/metadata.json
cd tools/data-pipeline && ./run.sh
```

```shell
# directly run the migration script to populate the database, assumes the data exists at tools/data-pipeline/data
cd tools/data-pipeline
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python scripts/pokemon/migrate-sets.py
python scripts/pokemon/migrate-cards.py
```

## Code Preferences

- Backend development package by feature is preferred
- For database tables using singular names is preferred
- Comprehensive integration tests are more valuable than unit tests
- Preference to using Records instead of classes where it is possible in Java

## Developer Environment

- The default Java installation on the system is Java 23 but also has Java 21 installed, which the backend is using
- For managing the Python version, the system has `pyenv` installed and is currently using Python 3.14 as the default
- For managing the Node version, the system has `nvm` installed and is currently using Node 22 as the default

## Documentation

Each component has its own documentation contain in it's `README.md` file, it should be referenced for up-to-date
information about the component. Each component can be seen in the [Project Overview](#project-overview) section.
