/**
 * Utility functions for card validation
 */

/**
 * Sorts an array of objects by their name property
 * @param {Array} arr - Array of objects with name property
 * @returns {Array} Sorted array
 */
function sortByName(arr) {
    return arr.slice().sort((a, b) => a.name.localeCompare(b.name));
}

/**
 * Sorts an array of objects by their type property
 * Used for weaknesses and resistances which can be in different orders
 * @param {Array} arr - Array of objects with type property
 * @returns {Array} Sorted array
 */
function sortByType(arr) {
    if (!arr || !Array.isArray(arr)) return arr;
    return arr.slice().sort((a, b) => {
        if (!a.type || !b.type) return 0;
        return a.type.localeCompare(b.type);
    });
}

/**
 * Normalizes an attack object for comparison
 * @param {Object} attack - Attack object from source or API
 * @returns {Object} Normalized attack
 */
function normalizeAttack(attack) {
    return {
        name: attack.name,
        cost: (attack.cost || []).slice().sort(),
        convertedEnergyCost: attack.convertedEnergyCost ?? attack.convertedCost ?? 0,
        damage: attack.damage || "",
        text: attack.text || ""
    };
}

/**
 * Normalizes a value by recursively sorting object keys
 * This ensures consistent comparison regardless of key order
 * @param {*} value - Value to normalize
 * @returns {*} Normalized value
 */
function normalizeForComparison(value) {
    if (value === null || value === undefined) {
        return value;
    }

    if (Array.isArray(value)) {
        return value.map(normalizeForComparison);
    }

    if (typeof value === 'object') {
        const sortedObj = {};
        Object.keys(value).sort().forEach(key => {
            sortedObj[key] = normalizeForComparison(value[key]);
        });
        return sortedObj;
    }

    return value;
}

/**
 * Deep equality comparison that ignores object key order
 * @param {*} a - First value
 * @param {*} b - Second value
 * @returns {boolean} True if equal
 */
function deepEqual(a, b) {
    return JSON.stringify(normalizeForComparison(a)) === JSON.stringify(normalizeForComparison(b));
}

/**
 * Checks if two arrays have the same members (order-independent)
 * @param {Array} arr1 - First array
 * @param {Array} arr2 - Second array
 * @returns {boolean} True if arrays have same members
 */
function arraysHaveSameMembers(arr1, arr2) {
    if (arr1.length !== arr2.length) return false;
    const sorted1 = [...arr1].sort();
    const sorted2 = [...arr2].sort();
    return deepEqual(sorted1, sorted2);
}

module.exports = {
    sortByName,
    sortByType,
    normalizeAttack,
    normalizeForComparison,
    deepEqual,
    arraysHaveSameMembers
};
