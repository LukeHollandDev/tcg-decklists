/**
 * Card validation functions
 *
 * These functions validate API responses against source data.
 * They return arrays of error messages (empty array = validation passed).
 */

const { sortByName, normalizeAttack, deepEqual, arraysHaveSameMembers } = require('./utils');

/**
 * Validates required fields for a Pokemon card
 * @param {Object} apiCard - Card data from API
 * @param {Object} sourceCard - Card data from source JSON
 * @returns {Array<string>} Array of error messages
 */
function validateRequiredFields(apiCard, sourceCard) {
    const errors = [];

    // ID
    if (apiCard.id !== sourceCard.id) {
        errors.push(`ID mismatch: expected "${sourceCard.id}", got "${apiCard.id}"`);
    }

    // Images
    if (apiCard.imageLow !== sourceCard.images.small) {
        errors.push(`Image low mismatch: expected "${sourceCard.images.small}", got "${apiCard.imageLow}"`);
    }

    if (apiCard.imageHigh !== sourceCard.images.large) {
        errors.push(`Image high mismatch: expected "${sourceCard.images.large}", got "${apiCard.imageHigh}"`);
    }

    // Legalities (case-insensitive)
    const apiKeys = Object.keys(apiCard.legalities || {}).sort();
    const sourceKeys = Object.keys(sourceCard.legalities || {}).sort();
    if (!arraysHaveSameMembers(apiKeys, sourceKeys)) {
        errors.push(`Legalities keys mismatch: expected [${sourceKeys}], got [${apiKeys}]`);
    } else {
        for (const key of apiKeys) {
            const apiValue = apiCard.legalities[key]?.toLowerCase();
            const sourceValue = sourceCard.legalities[key]?.toLowerCase();
            if (apiValue !== sourceValue) {
                errors.push(`Legality "${key}" mismatch: expected "${sourceValue}", got "${apiValue}"`);
            }
        }
    }

    // Name, Number, Supertype
    if (apiCard.name !== sourceCard.name) {
        errors.push(`Name mismatch: expected "${sourceCard.name}", got "${apiCard.name}"`);
    }

    if (apiCard.number !== sourceCard.number) {
        errors.push(`Number mismatch: expected "${sourceCard.number}", got "${apiCard.number}"`);
    }

    if (apiCard.supertype !== sourceCard.supertype) {
        errors.push(`Supertype mismatch: expected "${sourceCard.supertype}", got "${apiCard.supertype}"`);
    }

    return errors;
}

/**
 * Validates optional fields for a Pokemon card
 * @param {Object} apiCard - Card data from API
 * @param {Object} sourceCard - Card data from source JSON
 * @returns {Array<string>} Array of error messages
 */
function validateOptionalFields(apiCard, sourceCard) {
    const errors = [];

    // Subtypes
    if (sourceCard.subtypes && !arraysHaveSameMembers(apiCard.subtypes || [], sourceCard.subtypes)) {
        errors.push(`Subtypes mismatch: expected [${sourceCard.subtypes}], got [${apiCard.subtypes}]`);
    }

    // HP
    if (sourceCard.hp && apiCard.hp !== sourceCard.hp) {
        errors.push(`HP mismatch: expected "${sourceCard.hp}", got "${apiCard.hp}"`);
    }

    // Types
    if (sourceCard.types && !arraysHaveSameMembers(apiCard.types || [], sourceCard.types)) {
        errors.push(`Types mismatch: expected [${sourceCard.types}], got [${apiCard.types}]`);
    }

    // Evolution
    if (sourceCard.evolvesTo && !arraysHaveSameMembers(apiCard.evolvesTo || [], sourceCard.evolvesTo)) {
        errors.push(`EvolvesTo mismatch: expected [${sourceCard.evolvesTo}], got [${apiCard.evolvesTo}]`);
    }

    if (sourceCard.evolvesFrom && !arraysHaveSameMembers(apiCard.evolvesFrom || [], [sourceCard.evolvesFrom])) {
        errors.push(`EvolvesFrom mismatch: expected ["${sourceCard.evolvesFrom}"], got [${apiCard.evolvesFrom}]`);
    }

    // Attacks
    if (sourceCard.attacks) {
        const responseAttacks = sortByName((apiCard.attacks || []).map(normalizeAttack));
        const sourceAttacks = sortByName((sourceCard.attacks || []).map(normalizeAttack));

        if (responseAttacks.length !== sourceAttacks.length) {
            errors.push(`Attacks length mismatch: expected ${sourceAttacks.length}, got ${responseAttacks.length}`);
        } else {
            for (let i = 0; i < sourceAttacks.length; i++) {
                const apiAttack = responseAttacks[i];
                const srcAttack = sourceAttacks[i];

                if (apiAttack.name !== srcAttack.name) {
                    errors.push(`Attack ${i} name mismatch: expected "${srcAttack.name}", got "${apiAttack.name}"`);
                }
                if (!deepEqual(apiAttack.cost, srcAttack.cost)) {
                    errors.push(`Attack "${srcAttack.name}" cost mismatch: expected [${srcAttack.cost}], got [${apiAttack.cost}]`);
                }
                if (apiAttack.convertedEnergyCost !== srcAttack.convertedEnergyCost) {
                    errors.push(`Attack "${srcAttack.name}" convertedEnergyCost mismatch: expected ${srcAttack.convertedEnergyCost}, got ${apiAttack.convertedEnergyCost}`);
                }
                if (apiAttack.damage !== srcAttack.damage) {
                    errors.push(`Attack "${srcAttack.name}" damage mismatch: expected "${srcAttack.damage}", got "${apiAttack.damage}"`);
                }
                if (apiAttack.text !== srcAttack.text) {
                    errors.push(`Attack "${srcAttack.name}" text mismatch`);
                }
            }
        }
    }

    // Weaknesses
    if (sourceCard.weaknesses && !deepEqual(apiCard.weaknesses, sourceCard.weaknesses)) {
        errors.push(`Weaknesses mismatch`);
    }

    // Resistances
    if (sourceCard.resistances && !deepEqual(apiCard.resistances, sourceCard.resistances)) {
        errors.push(`Resistances mismatch`);
    }

    // Retreat Cost
    if (sourceCard.retreatCost && !arraysHaveSameMembers(apiCard.retreatCost || [], sourceCard.retreatCost)) {
        errors.push(`RetreatCost mismatch: expected [${sourceCard.retreatCost}], got [${apiCard.retreatCost}]`);
    }

    if (sourceCard.convertedRetreatCost !== undefined && apiCard.convertedRetreatCost !== sourceCard.convertedRetreatCost) {
        errors.push(`ConvertedRetreatCost mismatch: expected ${sourceCard.convertedRetreatCost}, got ${apiCard.convertedRetreatCost}`);
    }

    // Artist
    if (sourceCard.artist && apiCard.artistName !== sourceCard.artist) {
        errors.push(`Artist mismatch: expected "${sourceCard.artist}", got "${apiCard.artistName}"`);
    }

    // Rarity
    if (sourceCard.rarity && apiCard.rarityName !== sourceCard.rarity) {
        errors.push(`Rarity mismatch: expected "${sourceCard.rarity}", got "${apiCard.rarityName}"`);
    }

    // Flavor Text
    if (sourceCard.flavorText && apiCard.flavorText !== sourceCard.flavorText) {
        errors.push(`FlavorText mismatch`);
    }

    // Pokedex Numbers
    if (sourceCard.nationalPokedexNumbers && !arraysHaveSameMembers(apiCard.pokedexNumbers || [], sourceCard.nationalPokedexNumbers)) {
        errors.push(`PokedexNumbers mismatch: expected [${sourceCard.nationalPokedexNumbers}], got [${apiCard.pokedexNumbers}]`);
    }

    // Regulation Mark
    if (sourceCard.regulationMark && apiCard.regulationMark !== sourceCard.regulationMark) {
        errors.push(`RegulationMark mismatch: expected "${sourceCard.regulationMark}", got "${apiCard.regulationMark}"`);
    }

    // Abilities
    if (sourceCard.abilities) {
        const apiAbilities = sortByName(apiCard.abilities || []);
        const srcAbilities = sortByName(sourceCard.abilities);
        if (!deepEqual(apiAbilities, srcAbilities)) {
            errors.push(`Abilities mismatch`);
        }
    }

    // Rules
    if (sourceCard.rules && !arraysHaveSameMembers(apiCard.rules || [], sourceCard.rules)) {
        errors.push(`Rules mismatch`);
    }

    // Level
    if (sourceCard.level && apiCard.level !== sourceCard.level) {
        errors.push(`Level mismatch: expected "${sourceCard.level}", got "${apiCard.level}"`);
    }

    // Ancient Trait
    if (sourceCard.ancientTrait) {
        if (apiCard.ancientTrait?.name !== sourceCard.ancientTrait.name) {
            errors.push(`AncientTrait name mismatch: expected "${sourceCard.ancientTrait.name}", got "${apiCard.ancientTrait?.name}"`);
        }
        if (apiCard.ancientTrait?.text !== sourceCard.ancientTrait.text) {
            errors.push(`AncientTrait text mismatch`);
        }
    }

    return errors;
}

/**
 * Validates a complete Pokemon card
 * @param {Object} apiCard - Card data from API
 * @param {Object} sourceCard - Card data from source JSON
 * @returns {Array<string>} Array of error messages
 */
function validateCard(apiCard, sourceCard) {
    return [
        ...validateRequiredFields(apiCard, sourceCard),
        ...validateOptionalFields(apiCard, sourceCard)
    ];
}

/**
 * Gets validation test cases for Bruno/Chai testing
 * Returns an array of test objects that can be used to create individual test() calls
 * @param {Object} apiCard - Card data from API
 * @param {Object} sourceCard - Card data from source JSON
 * @param {Object} expect - Chai expect function (from Bruno)
 * @returns {Array<{name: string, category: string, fn: function}>} Array of test cases
 */
function getValidationTests(apiCard, sourceCard, expect) {
    const tests = [];

    // Helper to add a test
    const addTest = (category, name, fn) => {
        tests.push({ category, name, fn });
    };

    // ------------------ REQUIRED FIELDS ------------------

    addTest('required', 'id should match', () => {
        expect(apiCard.id).to.equal(sourceCard.id);
    });

    addTest('required', 'images should match', () => {
        expect(apiCard.imageLow).to.equal(sourceCard.images.small);
        expect(apiCard.imageHigh).to.equal(sourceCard.images.large);
    });

    addTest('required', 'legalities keys and values should match case-insensitively', () => {
        const apiKeys = Object.keys(apiCard.legalities || {}).sort();
        const sourceKeys = Object.keys(sourceCard.legalities || {}).sort();
        expect(apiKeys).to.have.all.members(sourceKeys);

        for (const key of apiKeys) {
            const apiValue = apiCard.legalities[key]?.toLowerCase();
            const sourceValue = sourceCard.legalities[key]?.toLowerCase();
            expect(apiValue).to.equal(sourceValue);
        }
    });

    addTest('required', 'name, number, supertype should match', () => {
        expect(apiCard.name).to.equal(sourceCard.name);
        expect(apiCard.number).to.equal(sourceCard.number);
        expect(apiCard.supertype).to.equal(sourceCard.supertype);
    });

    // ------------------ OPTIONAL FIELDS ------------------

    if (sourceCard.subtypes) {
        addTest('optional', 'subtypes should match', () => {
            expect(apiCard.subtypes).to.have.all.members(sourceCard.subtypes);
        });
    }

    if (sourceCard.hp) {
        addTest('optional', 'hp should match', () => {
            expect(apiCard.hp).to.equal(sourceCard.hp);
        });
    }

    if (sourceCard.types) {
        addTest('optional', 'types should match', () => {
            expect(apiCard.types).to.have.all.members(sourceCard.types);
        });
    }

    if (sourceCard.evolvesTo) {
        addTest('optional', 'evolvesTo should match', () => {
            expect(apiCard.evolvesTo).to.have.all.members(sourceCard.evolvesTo);
        });
    }

    if (sourceCard.evolvesFrom) {
        addTest('optional', 'evolvesFrom should match', () => {
            expect(apiCard.evolvesFrom).to.have.all.members([sourceCard.evolvesFrom]);
        });
    }

    if (sourceCard.attacks) {
        addTest('optional', 'attacks should match', () => {
            const responseAttacks = sortByName((apiCard.attacks || []).map(normalizeAttack));
            const sourceAttacks = sortByName((sourceCard.attacks || []).map(normalizeAttack));

            expect(responseAttacks.length).to.equal(sourceAttacks.length);

            for (let i = 0; i < sourceAttacks.length; i++) {
                const apiAttack = responseAttacks[i];
                const srcAttack = sourceAttacks[i];
                expect(apiAttack.name).to.equal(srcAttack.name);
                expect(apiAttack.cost).to.deep.equal(srcAttack.cost);
                expect(apiAttack.convertedEnergyCost).to.equal(srcAttack.convertedEnergyCost);
                expect(apiAttack.damage).to.equal(srcAttack.damage);
                expect(apiAttack.text).to.equal(srcAttack.text);
            }
        });
    }

    if (sourceCard.weaknesses) {
        addTest('optional', 'weaknesses should match', () => {
            expect(apiCard.weaknesses).to.deep.equal(sourceCard.weaknesses);
        });
    }

    if (sourceCard.resistances) {
        addTest('optional', 'resistances should match', () => {
            expect(apiCard.resistances).to.deep.equal(sourceCard.resistances);
        });
    }

    if (sourceCard.retreatCost) {
        addTest('optional', 'retreatCost should match', () => {
            expect(apiCard.retreatCost).to.have.all.members(sourceCard.retreatCost);
        });
    }

    if (sourceCard.convertedRetreatCost !== undefined) {
        addTest('optional', 'convertedRetreatCost should match', () => {
            expect(apiCard.convertedRetreatCost).to.equal(sourceCard.convertedRetreatCost);
        });
    }

    if (sourceCard.artist) {
        addTest('optional', 'artist should match', () => {
            expect(apiCard.artistName).to.equal(sourceCard.artist);
        });
    }

    if (sourceCard.rarity) {
        addTest('optional', 'rarity should match', () => {
            expect(apiCard.rarityName).to.equal(sourceCard.rarity);
        });
    }

    if (sourceCard.flavorText) {
        addTest('optional', 'flavorText should match', () => {
            expect(apiCard.flavorText).to.equal(sourceCard.flavorText);
        });
    }

    if (sourceCard.nationalPokedexNumbers) {
        addTest('optional', 'pokedexNumbers should match', () => {
            expect(apiCard.pokedexNumbers).to.have.all.members(sourceCard.nationalPokedexNumbers);
        });
    }

    if (sourceCard.regulationMark) {
        addTest('optional', 'regulationMark should match', () => {
            expect(apiCard.regulationMark).to.equal(sourceCard.regulationMark);
        });
    }

    if (sourceCard.abilities) {
        addTest('optional', 'abilities should match', () => {
            expect(sortByName(apiCard.abilities || [])).to.deep.equal(sortByName(sourceCard.abilities));
        });
    }

    if (sourceCard.rules) {
        addTest('optional', 'rules should match', () => {
            expect(apiCard.rules).to.have.all.members(sourceCard.rules);
        });
    }

    if (sourceCard.level) {
        addTest('optional', 'level should match', () => {
            expect(apiCard.level).to.equal(sourceCard.level);
        });
    }

    if (sourceCard.ancientTrait) {
        addTest('optional', 'ancientTrait should match', () => {
            expect(apiCard.ancientTrait.name).to.equal(sourceCard.ancientTrait.name);
            expect(apiCard.ancientTrait.text).to.equal(sourceCard.ancientTrait.text);
        });
    }

    return tests;
}

module.exports = {
    validateCard,
    getValidationTests
};
