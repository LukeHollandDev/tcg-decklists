# TCG Decklists Frontend

This directory contains the source code for the React static site for TCG
Decklists. The initial template for this codebase is based on the Vite create
app using the React Compiler and Rolldown Vite.

```shell
npm create vite@latest
```

Selecting the following options in order:

| Option            | Selection                   |
|-------------------|-----------------------------|
| Project Name      | tcg-decklists               |
| Framework         | React                       |
| Variant           | TypeScript + React Compiler |
| Use rolldown-vite | Yes                         |

The only other difference out of the box from this was replacing
[ESLint](https://eslint.org/) with [Biome](https://biomejs.dev/).

## Commands

| Command           | Description                                                   |
|-------------------|---------------------------------------------------------------|
| `npm run dev`     | Start the Vite development server with hot module replacement |
| `npm run build`   | Type-check with TypeScript compiler and build for production  |
| `npm run preview` | Preview the production build locally                          |
| `npm run lint`    | Run Biome linter and auto-fix issues                          |
| `npm run format`  | Format code with Biome and apply changes                      |
| `npm run check`   | Run Biome's combined lint + format check and auto-fix         |

## Dependencies

To run the frontend, the following needs to be available:

- API needs to be running, see [Backend README](../backend/README.md)
