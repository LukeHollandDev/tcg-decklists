"""
Entity data models for the ETL pipeline.

These models represent data entities during the ETL process.
"""

from dataclasses import dataclass, field
from typing import Any, Dict, List, Optional


@dataclass
class Field:
    """Represents a single field in an entity."""
    name: str
    value: Any
    data_type: str
    nullable: bool = True


@dataclass
class Relationship:
    """Represents a relationship between entities."""
    name: str
    entity_type: str
    foreign_key: str
    related_id: Optional[int] = None


@dataclass
class Entity:
    """Represents a data entity during ETL."""
    entity_type: str
    primary_key_value: Any
    fields: Dict[str, Any] = field(default_factory=dict)
    relationships: List[Relationship] = field(default_factory=list)
    metadata: Dict[str, Any] = field(default_factory=dict)

    def add_field(self, name: str, value: Any) -> None:
        """Add a field to the entity."""
        self.fields[name] = value

    def add_relationship(self, relationship: Relationship) -> None:
        """Add a relationship to the entity."""
        self.relationships.append(relationship)

    def get_field(self, name: str, default: Any = None) -> Any:
        """Get a field value by name."""
        return self.fields.get(name, default)


@dataclass
class EntityBatch:
    """Represents a batch of entities for processing."""
    entity_type: str
    entities: List[Entity] = field(default_factory=list)
    total_count: int = 0
    processed_count: int = 0

    def add_entity(self, entity: Entity) -> None:
        """Add an entity to the batch."""
        self.entities.append(entity)
        self.total_count += 1

    def mark_processed(self, count: int = 1) -> None:
        """Mark entities as processed."""
        self.processed_count += count

    @property
    def is_complete(self) -> bool:
        """Check if all entities in batch are processed."""
        return self.processed_count >= self.total_count

    @property
    def progress_percentage(self) -> float:
        """Get processing progress as percentage."""
        if self.total_count == 0:
            return 0.0
        return (self.processed_count / self.total_count) * 100
