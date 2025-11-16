"""
Main entry point for the ETL pipeline.

Orchestrates the Extract, Transform, Load process based on configuration files.
"""

import argparse
import sys
from pathlib import Path
from datetime import datetime

from models.config import DatabaseConfig
from models.entity import EntityBatch
from extract.git import GitExtractor
from extract.local import LocalExtractor
from load.database import DatabaseConnection, EntityLoader
from load.lookup_manager import LookupManager
from load.junction_manager import JunctionManager
from transform.mappers import FieldMapper
from transform.transforms import apply_transform
from utils.config_loader import ConfigLoader
from utils.logger import get_logger, set_log_level
from utils.version_tracker import VersionTracker

import logging


class ETLPipeline:
    """Main ETL pipeline orchestrator."""

    def __init__(self, game: str, dry_run: bool = False):
        """Initialize the pipeline."""
        self.game = game
        self.dry_run = dry_run
        self.logger = get_logger()

        # Initialize components
        self.config_loader = ConfigLoader()
        self.version_tracker = VersionTracker()
        self.db_config = DatabaseConfig.from_env()

        # Will be initialized later
        self.db: DatabaseConnection = None
        self.entity_loader: EntityLoader = None
        self.lookup_manager: LookupManager = None
        self.junction_manager: JunctionManager = None

    def run(self) -> bool:
        """
        Run the complete ETL pipeline.

        Returns:
            bool: True if successful
        """
        try:
            self.logger.phase(f"ETL Pipeline: {self.game.upper()}", f"Dry run: {self.dry_run}")

            # Load game configuration
            game_config = self.config_loader.load_game_config(self.game)

            # Phase 1: Extract
            if not self._run_extract_phase(game_config):
                return False

            if self.dry_run:
                self.logger.info("Dry run mode - skipping transform and load phases")
                return True

            # Initialize database connection
            self.db = DatabaseConnection(self.db_config)
            self.db.connect()

            self.entity_loader = EntityLoader(self.db)
            self.lookup_manager = LookupManager(self.db)
            self.junction_manager = JunctionManager(self.db, self.lookup_manager)

            # Phase 2 & 3: Transform and Load (combined)
            with self.db.transaction():
                for entity_dep in game_config.entities:
                    if not self._process_entity(entity_dep.name, entity_dep.config):
                        return False

            # Log lookup statistics
            self.lookup_manager.log_stats()

            self.logger.phase("Pipeline Complete", "All entities processed successfully")
            return True

        except Exception as e:
            self.logger.error("Pipeline failed", e)
            return False
        finally:
            if self.db:
                self.db.disconnect()

    def _run_extract_phase(self, game_config) -> bool:
        """Run the extract phase."""
        self.logger.phase("Phase 1: Extract", "Downloading data from sources")

        git_extractor = GitExtractor()

        for source_config in game_config.sources:
            self.logger.info(f"Processing source: {source_config.name}")

            # Check if updates are needed
            current_version = self.version_tracker.get_version(source_config.name)
            latest_version = git_extractor.get_latest_version(source_config.url)

            if not latest_version:
                self.logger.warning(f"Could not determine latest version for {source_config.name}")
                continue

            if current_version == latest_version:
                self.logger.info(f"{source_config.name} is up to date ({current_version})")
                continue

            self.logger.info(f"Updating {source_config.name} to {latest_version}")

            # Extract each output
            all_success = True
            for output in source_config.outputs:
                success = git_extractor.extract(
                    source_config.url,
                    output.destination,
                    output.path
                )

                if not success:
                    self.logger.error(f"Failed to extract {output.name}")
                    all_success = False
                    break

            # Update version tracker
            if all_success:
                self.version_tracker.mark_successful(source_config.name, latest_version)
            else:
                self.version_tracker.mark_failed(source_config.name, latest_version)
                return False

        self.logger.success("Extract phase completed")
        return True

    def _process_entity(self, entity_name: str, config_file: str) -> bool:
        """Process a single entity (transform + load)."""
        self.logger.phase(f"Processing Entity: {entity_name}", f"Config: {config_file}")

        start_time = datetime.now()

        try:
            # Load entity configuration
            entity_config = self.config_loader.load_entity_config(self.game, config_file)

            # Load data from source
            data = self._load_entity_data(entity_config)

            if not data:
                self.logger.warning(f"No data found for {entity_name}")
                return True

            self.logger.info(f"Loaded {len(data)} records")

            # Process each record
            batch_size = 500
            total = len(data)

            for i, record in enumerate(data, 1):
                # Transform and load
                self._transform_and_load_record(entity_config, record)

                # Commit in batches
                if i % batch_size == 0:
                    self.db.commit()
                    self.logger.progress(i, total, "records")

            # Final commit
            self.db.commit()

            duration = (datetime.now() - start_time).total_seconds()
            self.logger.success(f"Completed {entity_name}: {total} records in {duration:.2f}s")

            return True

        except Exception as e:
            self.logger.error(f"Failed to process {entity_name}", e)
            return False

    def _load_entity_data(self, entity_config):
        """Load data from source based on entity config."""
        local_extractor = LocalExtractor()

        source = entity_config.source

        if source.type.value == "json":
            return local_extractor.load_json(source.location)
        elif source.type.value == "json_multi":
            return local_extractor.load_json_multi(
                source.location,
                consolidate=source.consolidate
            )
        else:
            raise ValueError(f"Unsupported source type: {source.type}")

    def _transform_and_load_record(self, entity_config, record):
        """Transform and load a single record."""
        mapper = FieldMapper()

        # Build main entity data
        entity_data = {}

        # Extract primary key
        pk_value = mapper.get_value(record, entity_config.primary_key.field)
        entity_data[entity_config.primary_key.column] = pk_value

        # Extract simple fields
        for field in entity_config.fields:
            value = mapper.get_value(record, field.json_path)
            entity_data[field.column] = value

        # Extract nested fields
        for nested_field in entity_config.nested_fields:
            value = mapper.get_value(record, nested_field.json_path)
            entity_data[nested_field.column] = value

        # Extract transformed fields
        for tf in entity_config.transformed_fields:
            value = mapper.get_value(record, tf.json_path)
            if value is not None and tf.transform != "none":
                value = apply_transform(tf.transform, value)
            entity_data[tf.column] = value

        # Handle lookups
        for lookup in entity_config.lookups:
            value = mapper.get_value(record, lookup.json_path)
            if value is not None and lookup.transform:
                value = apply_transform(lookup.transform, value)

            # Get lookup match field if specified
            if lookup.lookup_match_field:
                # For special cases like set_id extraction
                match_value = value
            else:
                match_value = value

            lookup_id = self.lookup_manager.get_or_create(
                lookup.lookup_table,
                lookup.lookup_column,
                match_value,
                lookup.create_if_missing
            )
            entity_data[lookup.column] = lookup_id

        # Upsert main entity
        self.entity_loader.upsert_entity(
            entity_config.table,
            entity_data,
            entity_config.conflict_target
        )

        # Handle junction tables
        self._handle_junctions(entity_config, record, pk_value, mapper)

    def _handle_junctions(self, entity_config, record, parent_id, mapper):
        """Handle all junction table insertions for a record."""
        # Simple array junctions
        for array_junction in entity_config.array_junctions:
            values = mapper.get_array_value(record, array_junction.json_path)
            if values:
                self.junction_manager.insert_array_junction(
                    array_junction.junction_table,
                    "card_id",  # TODO: Make this configurable
                    parent_id,
                    "type_id" if "type" in array_junction.junction_table else f"{array_junction.lookup_table.split('_')[-1]}_id",
                    array_junction.lookup_table,
                    array_junction.lookup_column,
                    values,
                    array_junction.create_if_missing
                )

        # Array with quantity
        for array_qty in entity_config.array_with_quantity:
            value_counts = mapper.count_array_occurrences(record, array_qty.json_path)
            if value_counts:
                self.junction_manager.insert_array_junction_with_quantity(
                    array_qty.junction_table,
                    "card_id",
                    parent_id,
                    "type_id",
                    array_qty.lookup_table,
                    array_qty.lookup_column,
                    value_counts,
                    array_qty.create_if_missing
                )

        # Dynamic object mapping (e.g., legalities)
        for dynamic_map in entity_config.dynamic_object_mapping:
            dynamic_obj = mapper.extract_dynamic_object_keys(record, dynamic_map.json_path)
            if dynamic_obj:
                self.junction_manager.insert_dynamic_object_junction(
                    dynamic_map.junction_table,
                    "card_id",
                    parent_id,
                    "format_id",
                    dynamic_map.key_lookup_table,
                    dynamic_map.key_lookup_column,
                    dynamic_map.value_column,
                    dynamic_obj,
                    dynamic_map.value_transform,
                    dynamic_map.create_keys_if_missing
                )


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description="TCG Decklists ETL Pipeline - Config-driven data extraction and loading"
    )
    parser.add_argument(
        "--game",
        required=True,
        help="Game to process (e.g., pokemon, yugioh)"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Run extract phase only without loading to database"
    )
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Enable verbose logging"
    )

    args = parser.parse_args()

    # Set log level
    if args.verbose:
        set_log_level(logging.DEBUG)

    # Run pipeline
    pipeline = ETLPipeline(args.game, args.dry_run)
    success = pipeline.run()

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
