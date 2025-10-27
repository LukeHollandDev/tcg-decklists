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
 * Deep equality comparison using JSON serialization
 * @param {*} a - First value
 * @param {*} b - Second value
 * @returns {boolean} True if equal
 */
function deepEqual(a, b) {
    return JSON.stringify(a) === JSON.stringify(b);
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
    normalizeAttack,
    deepEqual,
    arraysHaveSameMembers
};
