#!/usr/bin/env python
"""
Pokemon sets migration script.
Imports set metadata from data/pokemon/sets/en.json into the database.
"""
import sys
import os

# Add parent directory to path to import the library
sys.path.insert(0, os.path.dirname(__file__))

from _migrate_lib import main_sets_only

if __name__ == '__main__':
    main_sets_only()
