"""
Junction table management for the ETL pipeline.

Handles many-to-many relationships through junction tables.
"""

from typing import Any, Dict, List, Optional
from psycopg2 import sql

from .database import DatabaseConnection
from .lookup_manager import LookupManager
from ..utils.logger import get_logger


class JunctionManager:
    """Manages junction tables for many-to-many relationships."""

    def __init__(self, db: DatabaseConnection, lookup_manager: LookupManager):
        """Initialize junction manager."""
        self.db = db
        self.lookup = lookup_manager
        self.logger = get_logger()

    def insert_simple_junction(
        self,
        junction_table: str,
        parent_column: str,
        parent_id: Any,
        child_column: str,
        child_id: Any,
        extra_columns: Optional[Dict[str, Any]] = None
    ) -> None:
        """
        Insert a record into a junction table.

        Args:
            junction_table: Name of junction table
            parent_column: Parent foreign key column name
            parent_id: Parent ID value
            child_column: Child foreign key column name
            child_id: Child ID value
            extra_columns: Additional columns to set (e.g., {"direction": "from"})
        """
        # Build data dictionary
        data = {
            parent_column: parent_id,
            child_column: child_id
        }

        if extra_columns:
            data.update(extra_columns)

        columns = list(data.keys())
        values = [data[col] for col in columns]

        # Build INSERT statement
        query = sql.SQL("INSERT INTO {} ({}) VALUES ({})").format(
            sql.Identifier(junction_table),
            sql.SQL(', ').join(map(sql.Identifier, columns)),
            sql.SQL(', ').join(sql.Placeholder() * len(columns))
        )

        self.db.execute(query.as_string(self.db.conn), tuple(values))

    def insert_array_junction(
        self,
        junction_table: str,
        parent_column: str,
        parent_id: Any,
        child_column: str,
        lookup_table: str,
        lookup_column: str,
        values: List[Any],
        create_if_missing: bool = True
    ) -> None:
        """
        Insert multiple junction records for an array of values.

        Args:
            junction_table: Name of junction table
            parent_column: Parent foreign key column (e.g., "card_id")
            parent_id: Parent ID value
            child_column: Child foreign key column (e.g., "type_id")
            lookup_table: Table to lookup child values (e.g., "pokemon_type")
            lookup_column: Column to match in lookup table (e.g., "name")
            values: List of values to insert
            create_if_missing: Create lookup records if not found
        """
        if not values:
            return

        for value in values:
            # Get or create child ID
            child_id = self.lookup.get_or_create(
                lookup_table,
                lookup_column,
                value,
                create_if_missing
            )

            if child_id is not None:
                self.insert_simple_junction(
                    junction_table,
                    parent_column,
                    parent_id,
                    child_column,
                    child_id
                )

    def insert_array_junction_with_quantity(
        self,
        junction_table: str,
        parent_column: str,
        parent_id: Any,
        child_column: str,
        lookup_table: str,
        lookup_column: str,
        value_counts: Dict[Any, int],
        create_if_missing: bool = True
    ) -> None:
        """
        Insert junction records with quantity tracking.

        Used for cases like retreat costs where we need to track how many
        of each type (e.g., {"Colorless": 2, "Fire": 1}).

        Args:
            junction_table: Name of junction table
            parent_column: Parent foreign key column
            parent_id: Parent ID value
            child_column: Child foreign key column
            lookup_table: Table to lookup child values
            lookup_column: Column to match in lookup table
            value_counts: Dictionary of value -> count
            create_if_missing: Create lookup records if not found
        """
        if not value_counts:
            return

        for value, quantity in value_counts.items():
            # Get or create child ID
            child_id = self.lookup.get_or_create(
                lookup_table,
                lookup_column,
                value,
                create_if_missing
            )

            if child_id is not None:
                self.insert_simple_junction(
                    junction_table,
                    parent_column,
                    parent_id,
                    child_column,
                    child_id,
                    extra_columns={"quantity": quantity}
                )

    def insert_dynamic_object_junction(
        self,
        junction_table: str,
        parent_column: str,
        parent_id: Any,
        key_column: str,
        key_lookup_table: str,
        key_lookup_column: str,
        value_column: str,
        dynamic_object: Dict[str, Any],
        value_transform: Optional[str] = None,
        create_keys_if_missing: bool = True
    ) -> None:
        """
        Insert junction records for dynamic objects (unknown keys).

        Used for cases like legalities where keys are format names:
        {"unlimited": "Legal", "standard": "Banned"}

        Args:
            junction_table: Name of junction table
            parent_column: Parent foreign key column (e.g., "card_id")
            parent_id: Parent ID value
            key_column: Column for key foreign key (e.g., "format_id")
            key_lookup_table: Table to lookup keys (e.g., "pokemon_format")
            key_lookup_column: Column to match keys (e.g., "name")
            value_column: Column for value (e.g., "status")
            dynamic_object: Dictionary with dynamic keys
            value_transform: Optional transform to apply to values
            create_keys_if_missing: Create lookup records for keys if not found
        """
        if not dynamic_object:
            return

        from ..transform.transforms import apply_transform

        for key, value in dynamic_object.items():
            # Get or create key ID
            key_id = self.lookup.get_or_create(
                key_lookup_table,
                key_lookup_column,
                key,
                create_keys_if_missing
            )

            if key_id is not None:
                # Apply transform to value if specified
                if value_transform:
                    try:
                        value = apply_transform(value_transform, value)
                    except ValueError:
                        self.logger.warning(f"Unknown transform: {value_transform}")

                self.insert_simple_junction(
                    junction_table,
                    parent_column,
                    parent_id,
                    key_column,
                    key_id,
                    extra_columns={value_column: value}
                )

    def clear_junction_records(
        self,
        junction_table: str,
        parent_column: str,
        parent_id: Any
    ) -> None:
        """
        Delete all junction records for a parent entity.

        Used to clear old relationships before inserting new ones.

        Args:
            junction_table: Name of junction table
            parent_column: Parent foreign key column
            parent_id: Parent ID value
        """
        query = sql.SQL("DELETE FROM {} WHERE {} = %s").format(
            sql.Identifier(junction_table),
            sql.Identifier(parent_column)
        )

        self.db.execute(query.as_string(self.db.conn), (parent_id,))
