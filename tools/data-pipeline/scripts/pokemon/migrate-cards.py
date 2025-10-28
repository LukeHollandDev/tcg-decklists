#!/usr/bin/env python
"""
Pokemon cards migration script.
Imports card data from data/pokemon/cards/*.json into the database.
Note: Sets must be migrated first (run migrate-sets.py) for FK integrity.
"""
import sys
import os

# Add parent directory to path to import the library
sys.path.insert(0, os.path.dirname(__file__))

from _migrate_lib import main_cards_only

if __name__ == '__main__':
    main_cards_only()
