"""
Database connection and loading utilities.

Handles database connections, transactions, and basic CRUD operations.
"""

import psycopg2
from psycopg2 import sql
from psycopg2.extras import execute_batch
from typing import Any, Dict, List, Optional, Tuple
from contextlib import contextmanager

from ..models.config import DatabaseConfig, LoadStrategy
from ..utils.logger import get_logger


class DatabaseConnection:
    """Manages database connections and operations."""

    def __init__(self, config: DatabaseConfig):
        """Initialize database connection."""
        self.config = config
        self.logger = get_logger()
        self.conn: Optional[psycopg2.extensions.connection] = None
        self.cursor: Optional[psycopg2.extensions.cursor] = None

    def connect(self) -> None:
        """Establish database connection."""
        if self.conn is not None:
            return  # Already connected

        try:
            self.conn = psycopg2.connect(
                host=self.config.host,
                port=self.config.port,
                database=self.config.database,
                user=self.config.user,
                password=self.config.password
            )
            self.conn.set_session(autocommit=False)
            self.cursor = self.conn.cursor()
            self.logger.info(
                f"Connected to database: {self.config.database}@{self.config.host}"
            )
        except Exception as e:
            self.logger.error("Failed to connect to database", e)
            raise

    def disconnect(self) -> None:
        """Close database connection."""
        if self.cursor:
            self.cursor.close()
            self.cursor = None

        if self.conn:
            self.conn.close()
            self.conn = None
            self.logger.info("Disconnected from database")

    def commit(self) -> None:
        """Commit current transaction."""
        if self.conn:
            self.conn.commit()

    def rollback(self) -> None:
        """Rollback current transaction."""
        if self.conn:
            self.conn.rollback()
            self.logger.warning("Transaction rolled back")

    @contextmanager
    def transaction(self):
        """Context manager for database transactions."""
        self.connect()
        try:
            yield self
            self.commit()
        except Exception as e:
            self.rollback()
            raise e

    def execute(self, query: str, params: Optional[Tuple] = None) -> None:
        """Execute a query."""
        if not self.cursor:
            raise RuntimeError("Not connected to database")

        self.cursor.execute(query, params)

    def execute_batch(
        self,
        query: str,
        params_list: List[Tuple],
        batch_size: int = 500
    ) -> None:
        """Execute a query in batches."""
        if not self.cursor:
            raise RuntimeError("Not connected to database")

        execute_batch(self.cursor, query, params_list, page_size=batch_size)

    def fetchone(self) -> Optional[Tuple]:
        """Fetch one row from last query."""
        if not self.cursor:
            raise RuntimeError("Not connected to database")

        return self.cursor.fetchone()

    def fetchall(self) -> List[Tuple]:
        """Fetch all rows from last query."""
        if not self.cursor:
            raise RuntimeError("Not connected to database")

        return self.cursor.fetchall()

    def __enter__(self):
        """Context manager entry."""
        self.connect()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit."""
        if exc_type is not None:
            self.rollback()
        self.disconnect()


class EntityLoader:
    """Loads entities into database tables."""

    def __init__(self, db: DatabaseConnection):
        """Initialize entity loader."""
        self.db = db
        self.logger = get_logger()

    def upsert_entity(
        self,
        table: str,
        data: Dict[str, Any],
        conflict_target: str,
        primary_key_column: Optional[str] = None
    ) -> Optional[int]:
        """
        Upsert a single entity into a table.

        Args:
            table: Table name
            data: Dictionary of column -> value
            conflict_target: Column to check for conflicts (usually primary key)
            primary_key_column: Name of primary key column (for RETURNING)

        Returns:
            Optional[int]: Primary key ID if available
        """
        if not data:
            return None

        columns = list(data.keys())
        values = [data[col] for col in columns]

        # Build INSERT statement
        insert_query = sql.SQL("INSERT INTO {} ({}) VALUES ({})").format(
            sql.Identifier(table),
            sql.SQL(', ').join(map(sql.Identifier, columns)),
            sql.SQL(', ').join(sql.Placeholder() * len(columns))
        )

        # Build ON CONFLICT DO UPDATE SET clause
        update_clauses = [
            sql.SQL("{} = EXCLUDED.{}").format(
                sql.Identifier(col),
                sql.Identifier(col)
            )
            for col in columns if col != conflict_target
        ]

        upsert_query = sql.SQL("{} ON CONFLICT ({}) DO UPDATE SET {}").format(
            insert_query,
            sql.Identifier(conflict_target),
            sql.SQL(', ').join(update_clauses) if update_clauses else sql.SQL("id = EXCLUDED.id")
        )

        # Add RETURNING clause if primary key specified
        if primary_key_column:
            upsert_query = sql.SQL("{} RETURNING {}").format(
                upsert_query,
                sql.Identifier(primary_key_column)
            )

        # Execute query
        self.db.execute(upsert_query.as_string(self.db.conn), tuple(values))

        # Get returned ID if applicable
        if primary_key_column:
            result = self.db.fetchone()
            return result[0] if result else None

        return None

    def insert_entity(
        self,
        table: str,
        data: Dict[str, Any],
        primary_key_column: Optional[str] = None
    ) -> Optional[int]:
        """
        Insert a single entity into a table.

        Args:
            table: Table name
            data: Dictionary of column -> value
            primary_key_column: Name of primary key column (for RETURNING)

        Returns:
            Optional[int]: Primary key ID if available
        """
        if not data:
            return None

        columns = list(data.keys())
        values = [data[col] for col in columns]

        # Build INSERT statement
        insert_query = sql.SQL("INSERT INTO {} ({}) VALUES ({})").format(
            sql.Identifier(table),
            sql.SQL(', ').join(map(sql.Identifier, columns)),
            sql.SQL(', ').join(sql.Placeholder() * len(columns))
        )

        # Add RETURNING clause if primary key specified
        if primary_key_column:
            insert_query = sql.SQL("{} RETURNING {}").format(
                insert_query,
                sql.Identifier(primary_key_column)
            )

        # Execute query
        self.db.execute(insert_query.as_string(self.db.conn), tuple(values))

        # Get returned ID if applicable
        if primary_key_column:
            result = self.db.fetchone()
            return result[0] if result else None

        return None

    def delete_related_records(self, table: str, column: str, value: Any) -> None:
        """
        Delete records from a table matching a condition.

        Args:
            table: Table name
            column: Column name
            value: Value to match
        """
        delete_query = sql.SQL("DELETE FROM {} WHERE {} = %s").format(
            sql.Identifier(table),
            sql.Identifier(column)
        )

        self.db.execute(delete_query.as_string(self.db.conn), (value,))

    def batch_upsert(
        self,
        table: str,
        data_list: List[Dict[str, Any]],
        conflict_target: str,
        batch_size: int = 500
    ) -> None:
        """
        Batch upsert multiple entities.

        Args:
            table: Table name
            data_list: List of dictionaries (column -> value)
            conflict_target: Column to check for conflicts
            batch_size: Number of records per batch
        """
        if not data_list:
            return

        total = len(data_list)
        for i in range(0, total, batch_size):
            batch = data_list[i:i + batch_size]

            for data in batch:
                self.upsert_entity(table, data, conflict_target)

            # Commit batch
            self.db.commit()

            if i + batch_size < total:
                self.logger.progress(
                    min(i + batch_size, total),
                    total,
                    "records"
                )
