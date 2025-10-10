# TCG Decklists

Monorepo for TCG Decklists projects.

- [frontend](/frontend)
    - React frontend
- [backend](/backend)
    - SpringBoot backend with PostgreSQL db
- [data](/data)
    - Card data for the different TCGs
- [api-requests](/api-requests)
    - Bruno collection for testing API requests

The outstanding and completed tasks are stored in the [TODO.md](/TODO.md) file. It organises the tasks into various
categories.

## Docker

The whole stack can be run using the `docker-compose.yml`.

```sh
docker compose up -d
```

This will start up the following services:

- Database
    - This is pulled using the official PostgreSQL alpine image
- Backend
    - Uses the `Dockerfile` within the backend directory
    - Java SpringBoot API
- Frontend
    - Uses the `Dockerfile` within the frontend directory
    - Static React site using NGINX Proxy

## Data

The TCG card data is stored inside the [data](/data) directory. It is pulled and populated into the database via a
scheduled GitLab workflow.

The `metadata.json` keeps track of the _version_ of the data based on its source.
