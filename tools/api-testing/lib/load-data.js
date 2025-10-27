/**
 * Data loading functions for Pokemon card data
 */

const fs = require('fs');
const path = require('path');

/**
 * Loads all Pokemon card data from JSON files
 * @param {string} [basePath] - Optional base path (defaults to relative path from this file)
 * @returns {Object} Map of card ID to card data
 */
function loadPokemonSourceData(basePath) {
    // Default to the data-pipeline/pokemon directory relative to this file
    const dataDir = basePath || path.join(__dirname, '../../data-pipeline/pokemon');

    if (!fs.existsSync(dataDir)) {
        throw new Error(`Data directory not found at ${dataDir}. Please ensure card data is loaded using the data pipeline.`);
    }

    const pokemonJSONFiles = fs.readdirSync(dataDir);

    const allPokemon = pokemonJSONFiles
        .filter(file => file.endsWith('.json'))
        .flatMap(file => {
            const filePath = path.join(dataDir, file);
            const content = fs.readFileSync(filePath, 'utf-8');
            try {
                const json = JSON.parse(content);
                if (Array.isArray(json)) return json;
                return [];
            } catch (e) {
                console.error(`Failed to parse ${file}:`, e);
                return [];
            }
        });

    // Convert array to map for fast lookup by ID
    const cardMap = allPokemon.reduce((map, pokemon) => {
        map[pokemon.id] = pokemon;
        return map;
    }, {});

    return cardMap;
}

/**
 * Loads Pokemon card data as an array (for batch processing)
 * @param {string} [basePath] - Optional base path
 * @returns {Array} Array of card data
 */
function loadPokemonSourceDataAsArray(basePath) {
    const cardMap = loadPokemonSourceData(basePath);
    return Object.values(cardMap);
}

module.exports = {
    loadPokemonSourceData,
    loadPokemonSourceDataAsArray
};
