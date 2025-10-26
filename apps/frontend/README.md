# TCG Decklists Frontend

This directory contains the source code for the React static site for TCG
Decklists. The initial template for this codebase is based on the Vite create
app using the React Compiler and Rolldown Vite.

```sh
npm create vite@latest
```

Selecting the following options in order:

1. `tcg-decklists` as project name
2. `React` for the framework
3. `TypeScript + React Compiler` for the variant
4. `Yes` to use rolldown-vite

The only other difference out of the box from this was replacing
[ESLint](https://eslint.org/) with [Biome](https://biomejs.dev/).
