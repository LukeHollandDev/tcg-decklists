/**
 * TCG Decklists Card Validation Library
 *
 * This module provides shared validation logic for Pokemon cards,
 * used by both Bruno tests and the validation script.
 *
 * @example
 * // For CLI validation (returns error array)
 * const { loadPokemonSourceData, validateCard } = require('./lib');
 * const errors = validateCard(apiCard, sourceCard);
 *
 * // For Bruno tests (generates individual test() calls)
 * const { getValidationTests } = require('./lib/validators');
 * const tests = getValidationTests(apiCard, sourceCard, expect);
 * for (const { name, fn } of tests) {
 *   test(name, fn);
 * }
 */

const loadData = require('./load-data');
const validators = require('./validators');
const utils = require('./utils');

module.exports = {
    // Data loading
    ...loadData,

    // Validators
    ...validators,

    // Utilities
    ...utils
};
