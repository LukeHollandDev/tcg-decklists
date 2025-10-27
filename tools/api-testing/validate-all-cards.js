#!/usr/bin/env node

/**
 * Card Validation Script
 *
 * This script validates all Pokemon cards by:
 * 1. Loading source data from tools/data-pipeline/pokemon/*.json
 * 2. Making API calls to GET /api/card/pokemon/:id for each card
 * 3. Comparing the API response against source data
 * 4. Reporting any failures or errors
 *
 * Usage:
 *   node validate-all-cards.js [options]
 *
 * Options:
 *   --base-url=URL    Base API URL (default: http://localhost:8080/api)
 *   --limit=N         Only validate first N cards
 *   --verbose         Show details for passing tests too
 *   --card-id=ID      Validate only a specific card ID
 *   --concurrency=N   Number of concurrent requests (default: 10)
 */

const path = require('path');
const { loadPokemonSourceDataAsArray } = require('./lib/load-data');
const { validateCard } = require('./lib/validators');

// Parse command line arguments
const args = process.argv.slice(2);
const config = {
    baseUrl: 'http://localhost:8080/api',
    limit: null,
    verbose: false,
    cardId: null,
    concurrency: 10
};

for (const arg of args) {
    if (arg.startsWith('--base-url=')) {
        config.baseUrl = arg.split('=')[1];
    } else if (arg.startsWith('--limit=')) {
        config.limit = parseInt(arg.split('=')[1], 10);
    } else if (arg === '--verbose') {
        config.verbose = true;
    } else if (arg.startsWith('--card-id=')) {
        config.cardId = arg.split('=')[1];
    } else if (arg.startsWith('--concurrency=')) {
        config.concurrency = parseInt(arg.split('=')[1], 10);
    } else if (arg === '--help') {
        console.log(`
Card Validation Script

Usage:
  node validate-all-cards.js [options]

Options:
  --base-url=URL      Base API URL (default: http://localhost:8080/api)
  --limit=N           Only validate first N cards
  --verbose           Show details for passing tests too
  --card-id=ID        Validate only a specific card ID
  --concurrency=N     Number of concurrent requests (default: 10)
  --help              Show this help message
`);
        process.exit(0);
    }
}

/**
 * Validates a single card by fetching from API and comparing to source data
 * @param {string} cardId - Card ID to validate
 * @param {Object} sourceData - Source card data
 * @returns {Promise<Object>} Validation result
 */
async function validateCardFromApi(cardId, sourceData) {
    const url = `${config.baseUrl}/card/pokemon/${cardId}`;

    try {
        const response = await fetch(url);

        if (!response.ok) {
            return {
                cardId,
                success: false,
                errors: [`HTTP ${response.status}: ${response.statusText}`]
            };
        }

        const apiCard = await response.json();

        // Use shared validation logic
        const errors = validateCard(apiCard, sourceData);

        return {
            cardId,
            success: errors.length === 0,
            errors
        };

    } catch (error) {
        return {
            cardId,
            success: false,
            errors: [`Exception: ${error.message}`]
        };
    }
}

async function runValidation() {
    console.log('Loading source data...');
    const allCards = loadPokemonSourceDataAsArray();
    console.log(`Loaded ${allCards.length} cards from source data\n`);

    let cardsToValidate;
    if (config.cardId) {
        const card = allCards.find(c => c.id === config.cardId);
        if (!card) {
            console.error(`Error: Card ID "${config.cardId}" not found in source data`);
            process.exit(1);
        }
        cardsToValidate = [card];
        console.log(`Validating single card: ${config.cardId}\n`);
    } else {
        cardsToValidate = config.limit ? allCards.slice(0, config.limit) : allCards;
        console.log(`Validating ${cardsToValidate.length} cards...`);
        console.log(`Base URL: ${config.baseUrl}`);
        console.log(`Concurrency: ${config.concurrency}\n`);
    }

    const results = {
        total: cardsToValidate.length,
        passed: 0,
        failed: 0,
        failures: []
    };

    // Process cards in batches for concurrency control
    const batches = [];
    for (let i = 0; i < cardsToValidate.length; i += config.concurrency) {
        batches.push(cardsToValidate.slice(i, i + config.concurrency));
    }

    let processed = 0;
    for (const batch of batches) {
        const batchResults = await Promise.all(
            batch.map(card => validateCardFromApi(card.id, card))
        );

        for (const result of batchResults) {
            processed++;

            if (result.success) {
                results.passed++;
                if (config.verbose) {
                    console.log(`✓ ${result.cardId} - PASSED`);
                }
            } else {
                results.failed++;
                results.failures.push(result);
                console.log(`✗ ${result.cardId} - FAILED`);
                for (const error of result.errors) {
                    console.log(`  - ${error}`);
                }
            }

            // Progress indicator (every 100 cards)
            if (!config.verbose && processed % 100 === 0) {
                const percent = ((processed / results.total) * 100).toFixed(1);
                console.log(`Progress: ${processed}/${results.total} (${percent}%)`);
            }
        }
    }

    // Summary
    console.log('\n' + '='.repeat(60));
    console.log('VALIDATION SUMMARY');
    console.log('='.repeat(60));
    console.log(`Total cards validated: ${results.total}`);
    console.log(`Passed: ${results.passed} (${((results.passed / results.total) * 100).toFixed(2)}%)`);
    console.log(`Failed: ${results.failed} (${((results.failed / results.total) * 100).toFixed(2)}%)`);

    if (results.failed > 0) {
        console.log('\nFailed cards:');
        for (const failure of results.failures) {
            console.log(`  - ${failure.cardId}`);
        }
        process.exit(1);
    } else {
        console.log('\n✓ All cards validated successfully!');
        process.exit(0);
    }
}

// Run the validation
runValidation().catch(error => {
    console.error('Fatal error:', error);
    process.exit(1);
});
