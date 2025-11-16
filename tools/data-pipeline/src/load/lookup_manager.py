"""
Lookup table management for the ETL pipeline.

Handles the "get or create" pattern for lookup tables with caching.
"""

from typing import Any, Dict, Optional, Tuple
from psycopg2 import sql

from .database import DatabaseConnection
from ..utils.logger import get_logger


class LookupManager:
    """Manages lookup tables with caching for performance."""

    def __init__(self, db: DatabaseConnection):
        """Initialize lookup manager."""
        self.db = db
        self.logger = get_logger()
        # Cache: (table, column, value) -> id
        self.cache: Dict[Tuple[str, str, Any], int] = {}
        self.stats = {
            "cache_hits": 0,
            "cache_misses": 0,
            "created": 0,
            "found": 0
        }

    def get_or_create(
        self,
        table: str,
        column: str,
        value: Any,
        create_if_missing: bool = True,
        additional_data: Optional[Dict[str, Any]] = None
    ) -> Optional[int]:
        """
        Get or create a record in a lookup table.

        Args:
            table: Lookup table name
            column: Column to match on
            value: Value to match
            create_if_missing: If True, create record if not found
            additional_data: Additional columns to set when creating

        Returns:
            Optional[int]: ID of the record, or None if not found and create_if_missing=False
        """
        if value is None:
            return None

        # Check cache first
        cache_key = (table, column, value)
        if cache_key in self.cache:
            self.stats["cache_hits"] += 1
            return self.cache[cache_key]

        self.stats["cache_misses"] += 1

        # Try to find existing record
        record_id = self._find_record(table, column, value)

        if record_id is not None:
            self.stats["found"] += 1
            self.cache[cache_key] = record_id
            return record_id

        # Record not found
        if not create_if_missing:
            return None

        # Create new record
        record_id = self._create_record(table, column, value, additional_data)

        if record_id is not None:
            self.stats["created"] += 1
            self.cache[cache_key] = record_id

        return record_id

    def _find_record(self, table: str, column: str, value: Any) -> Optional[int]:
        """Find existing record in lookup table."""
        query = sql.SQL("SELECT id FROM {} WHERE {} = %s").format(
            sql.Identifier(table),
            sql.Identifier(column)
        )

        self.db.execute(query.as_string(self.db.conn), (value,))
        result = self.db.fetchone()

        return result[0] if result else None

    def _create_record(
        self,
        table: str,
        column: str,
        value: Any,
        additional_data: Optional[Dict[str, Any]] = None
    ) -> Optional[int]:
        """Create new record in lookup table."""
        # Build data dictionary
        data = {column: value}
        if additional_data:
            data.update(additional_data)

        columns = list(data.keys())
        values = [data[col] for col in columns]

        # Build INSERT statement
        query = sql.SQL("INSERT INTO {} ({}) VALUES ({}) RETURNING id").format(
            sql.Identifier(table),
            sql.SQL(', ').join(map(sql.Identifier, columns)),
            sql.SQL(', ').join(sql.Placeholder() * len(columns))
        )

        try:
            self.db.execute(query.as_string(self.db.conn), tuple(values))
            result = self.db.fetchone()
            return result[0] if result else None
        except Exception as e:
            self.logger.error(f"Failed to create record in {table}", e)
            return None

    def get_or_create_complex(
        self,
        table: str,
        match_fields: Dict[str, Any],
        create_if_missing: bool = True
    ) -> Optional[int]:
        """
        Get or create a record matching multiple fields.

        Args:
            table: Lookup table name
            match_fields: Dictionary of column -> value to match on
            create_if_missing: If True, create record if not found

        Returns:
            Optional[int]: ID of the record
        """
        if not match_fields:
            return None

        # Try to find existing record
        record_id = self._find_record_complex(table, match_fields)

        if record_id is not None:
            self.stats["found"] += 1
            return record_id

        # Record not found
        if not create_if_missing:
            return None

        # Create new record
        record_id = self._create_record_complex(table, match_fields)

        if record_id is not None:
            self.stats["created"] += 1

        return record_id

    def _find_record_complex(
        self,
        table: str,
        match_fields: Dict[str, Any]
    ) -> Optional[int]:
        """Find existing record matching multiple fields."""
        # Build WHERE clause
        conditions = []
        values = []

        for column, value in match_fields.items():
            if value is None:
                conditions.append(
                    sql.SQL("{} IS NULL").format(sql.Identifier(column))
                )
            else:
                conditions.append(
                    sql.SQL("{} = %s").format(sql.Identifier(column))
                )
                values.append(value)

        where_clause = sql.SQL(" AND ").join(conditions)

        query = sql.SQL("SELECT id FROM {} WHERE {}").format(
            sql.Identifier(table),
            where_clause
        )

        self.db.execute(query.as_string(self.db.conn), tuple(values) if values else None)
        result = self.db.fetchone()

        return result[0] if result else None

    def _create_record_complex(
        self,
        table: str,
        fields: Dict[str, Any]
    ) -> Optional[int]:
        """Create new record with multiple fields."""
        columns = list(fields.keys())
        values = [fields[col] for col in columns]

        query = sql.SQL("INSERT INTO {} ({}) VALUES ({}) RETURNING id").format(
            sql.Identifier(table),
            sql.SQL(', ').join(map(sql.Identifier, columns)),
            sql.SQL(', ').join(sql.Placeholder() * len(columns))
        )

        try:
            self.db.execute(query.as_string(self.db.conn), tuple(values))
            result = self.db.fetchone()
            return result[0] if result else None
        except Exception as e:
            self.logger.error(f"Failed to create complex record in {table}", e)
            return None

    def clear_cache(self) -> None:
        """Clear the lookup cache."""
        self.cache.clear()
        self.logger.debug("Lookup cache cleared")

    def get_stats(self) -> Dict[str, int]:
        """Get lookup statistics."""
        total_lookups = self.stats["cache_hits"] + self.stats["cache_misses"]
        if total_lookups > 0:
            hit_rate = (self.stats["cache_hits"] / total_lookups) * 100
        else:
            hit_rate = 0.0

        return {
            **self.stats,
            "total_lookups": total_lookups,
            "cache_hit_rate": hit_rate
        }

    def log_stats(self) -> None:
        """Log lookup statistics."""
        stats = self.get_stats()
        self.logger.info(f"Lookup Statistics:")
        self.logger.info(f"  Total lookups: {stats['total_lookups']}")
        self.logger.info(f"  Cache hit rate: {stats['cache_hit_rate']:.1f}%")
        self.logger.info(f"  Records found: {stats['found']}")
        self.logger.info(f"  Records created: {stats['created']}")
