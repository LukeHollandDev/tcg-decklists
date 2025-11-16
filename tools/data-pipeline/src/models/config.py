"""
Configuration data models for the ETL pipeline.

These models define the structure of YAML configuration files and provide
validation and type safety throughout the pipeline.
"""

from dataclasses import dataclass, field
from typing import Any, Dict, List, Optional
from enum import Enum


class SourceType(Enum):
    """Type of data source."""
    GIT = "git"
    URL = "url"
    LOCAL = "local"
    JSON = "json"
    JSON_MULTI = "json_multi"


class DataType(Enum):
    """Database column data types."""
    TEXT = "text"
    INTEGER = "integer"
    FLOAT = "float"
    BOOLEAN = "boolean"
    DATE = "date"
    DATETIME = "datetime"
    JSON = "json"


class LoadStrategy(Enum):
    """Strategy for loading data into database."""
    INSERT = "insert"
    UPSERT = "upsert"
    UPDATE = "update"


@dataclass
class SourceOutput:
    """Output configuration for a data source."""
    name: str
    path: str
    destination: str


@dataclass
class SourceConfig:
    """Configuration for a data source."""
    name: str
    type: SourceType
    url: Optional[str] = None
    update_check: Optional[str] = None
    outputs: List[SourceOutput] = field(default_factory=list)

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "SourceConfig":
        """Create SourceConfig from dictionary."""
        return cls(
            name=data["name"],
            type=SourceType(data["type"]),
            url=data.get("url"),
            update_check=data.get("update_check"),
            outputs=[
                SourceOutput(**output) for output in data.get("outputs", [])
            ]
        )


@dataclass
class EntityDependency:
    """Entity dependency configuration."""
    name: str
    config: str
    dependencies: List[str] = field(default_factory=list)


@dataclass
class SourceLocation:
    """Source location configuration for entity data."""
    type: SourceType
    location: str
    is_array: bool = True
    consolidate: bool = False


@dataclass
class PrimaryKey:
    """Primary key configuration."""
    field: str
    column: str
    type: DataType


@dataclass
class FieldMapping:
    """Simple field mapping configuration."""
    json_path: str
    column: str
    type: DataType
    required: bool = False
    nullable: bool = True


@dataclass
class NestedFieldMapping:
    """Nested field mapping (e.g., images.small)."""
    json_path: str
    column: str
    type: DataType
    nullable: bool = True


@dataclass
class TransformedField:
    """Field with transformation applied."""
    json_path: str
    column: str
    type: DataType
    transform: str
    nullable: bool = True


@dataclass
class LookupMapping:
    """Lookup table mapping configuration."""
    json_path: str
    column: str
    lookup_table: str
    lookup_column: str
    lookup_match_field: Optional[str] = None
    transform: Optional[str] = None
    create_if_missing: bool = False


@dataclass
class MatchField:
    """Field matching configuration for nested entities."""
    json_field: str
    column: str
    lookup_table: Optional[str] = None
    lookup_column: Optional[str] = None
    create_if_missing: bool = False


@dataclass
class NestedEntityMapping:
    """Nested entity mapping (e.g., ancientTrait)."""
    json_path: str
    column: str
    entity_table: str
    create_if_missing: bool = True
    match_fields: List[MatchField] = field(default_factory=list)
    required_sub_fields: List[str] = field(default_factory=list)


@dataclass
class ArrayJunction:
    """Array to junction table mapping."""
    json_path: str
    junction_table: str
    lookup_table: str
    lookup_column: str
    create_if_missing: bool = True


@dataclass
class ArrayWithQuantity:
    """Array mapping with quantity tracking."""
    json_path: str
    junction_table: str
    lookup_table: str
    lookup_column: str
    create_if_missing: bool = True
    count_occurrences: bool = True


@dataclass
class SubArrayMapping:
    """Sub-array mapping within nested arrays."""
    json_field: str
    junction_table: str
    lookup_table: str
    lookup_column: str
    create_if_missing: bool = True
    count_occurrences: bool = False


@dataclass
class NestedArrayEntity:
    """Complex nested array entity mapping (e.g., attacks, abilities)."""
    json_path: str
    junction_table: str
    entity_table: str
    create_if_missing: bool = True
    match_fields: List[MatchField] = field(default_factory=list)
    transformed_fields: List[TransformedField] = field(default_factory=list)
    sub_array_with_quantity: List[SubArrayMapping] = field(default_factory=list)
    uniqueness_includes_sub_array: Optional[str] = None


@dataclass
class EvolutionMapping:
    """Evolution chain mapping."""
    json_path: str
    junction_table: str
    lookup_table: str
    lookup_column: str
    create_if_missing: bool = True
    is_array: bool = False
    extra_columns: Dict[str, Any] = field(default_factory=dict)


@dataclass
class DynamicObjectMapping:
    """Dynamic object mapping (e.g., legalities with unknown keys)."""
    json_path: str
    junction_table: str
    key_lookup_table: str
    key_lookup_column: str
    value_column: str
    value_transform: Optional[str] = None
    create_keys_if_missing: bool = True


@dataclass
class EntityConfig:
    """Complete entity configuration."""
    entity: str
    version: str
    source: SourceLocation
    table: str
    primary_key: PrimaryKey
    strategy: LoadStrategy
    conflict_target: Optional[str] = None

    # Field mappings
    fields: List[FieldMapping] = field(default_factory=list)
    nested_fields: List[NestedFieldMapping] = field(default_factory=list)
    transformed_fields: List[TransformedField] = field(default_factory=list)

    # Lookups and relationships
    lookups: List[LookupMapping] = field(default_factory=list)
    nested_entities: List[NestedEntityMapping] = field(default_factory=list)

    # Array mappings
    array_junctions: List[ArrayJunction] = field(default_factory=list)
    array_with_quantity: List[ArrayWithQuantity] = field(default_factory=list)
    nested_array_entities: List[NestedArrayEntity] = field(default_factory=list)

    # Special mappings
    evolution_mappings: List[EvolutionMapping] = field(default_factory=list)
    dynamic_object_mapping: List[DynamicObjectMapping] = field(default_factory=list)

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "EntityConfig":
        """Create EntityConfig from dictionary."""
        # Parse source
        source_data = data["source"]
        source = SourceLocation(
            type=SourceType(source_data["type"]),
            location=source_data["location"],
            is_array=source_data.get("is_array", True),
            consolidate=source_data.get("consolidate", False)
        )

        # Parse primary key
        pk_data = data["primary_key"]
        primary_key = PrimaryKey(
            field=pk_data["field"],
            column=pk_data["column"],
            type=DataType(pk_data["type"])
        )

        # Parse simple fields
        fields = [
            FieldMapping(
                json_path=f["json_path"],
                column=f["column"],
                type=DataType(f["type"]),
                required=f.get("required", False),
                nullable=f.get("nullable", True)
            )
            for f in data.get("fields", [])
        ]

        # Parse nested fields
        nested_fields = [
            NestedFieldMapping(
                json_path=nf["json_path"],
                column=nf["column"],
                type=DataType(nf["type"]),
                nullable=nf.get("nullable", True)
            )
            for nf in data.get("nested_fields", [])
        ]

        # Parse transformed fields
        transformed_fields = [
            TransformedField(
                json_path=tf["json_path"],
                column=tf["column"],
                type=DataType(tf["type"]),
                transform=tf["transform"],
                nullable=tf.get("nullable", True)
            )
            for tf in data.get("transformed_fields", [])
        ]

        # Parse lookups
        lookups = [
            LookupMapping(
                json_path=lk["json_path"],
                column=lk["column"],
                lookup_table=lk["lookup_table"],
                lookup_column=lk["lookup_column"],
                lookup_match_field=lk.get("lookup_match_field"),
                transform=lk.get("transform"),
                create_if_missing=lk.get("create_if_missing", False)
            )
            for lk in data.get("lookups", [])
        ]

        # Parse nested entities
        nested_entities = [
            NestedEntityMapping(
                json_path=ne["json_path"],
                column=ne["column"],
                entity_table=ne["entity_table"],
                create_if_missing=ne.get("create_if_missing", True),
                match_fields=[
                    MatchField(
                        json_field=mf["json_field"],
                        column=mf["column"],
                        lookup_table=mf.get("lookup_table"),
                        lookup_column=mf.get("lookup_column"),
                        create_if_missing=mf.get("create_if_missing", False)
                    )
                    for mf in ne.get("match_fields", [])
                ],
                required_sub_fields=ne.get("required_sub_fields", [])
            )
            for ne in data.get("nested_entities", [])
        ]

        # Parse array junctions
        array_junctions = [
            ArrayJunction(
                json_path=aj["json_path"],
                junction_table=aj["junction_table"],
                lookup_table=aj["lookup_table"],
                lookup_column=aj["lookup_column"],
                create_if_missing=aj.get("create_if_missing", True)
            )
            for aj in data.get("array_junctions", [])
        ]

        # Parse array with quantity
        array_with_quantity = [
            ArrayWithQuantity(
                json_path=aq["json_path"],
                junction_table=aq["junction_table"],
                lookup_table=aq["lookup_table"],
                lookup_column=aq["lookup_column"],
                create_if_missing=aq.get("create_if_missing", True),
                count_occurrences=aq.get("count_occurrences", True)
            )
            for aq in data.get("array_with_quantity", [])
        ]

        # Parse nested array entities
        nested_array_entities = [
            NestedArrayEntity(
                json_path=nae["json_path"],
                junction_table=nae["junction_table"],
                entity_table=nae["entity_table"],
                create_if_missing=nae.get("create_if_missing", True),
                match_fields=[
                    MatchField(
                        json_field=mf["json_field"],
                        column=mf["column"],
                        lookup_table=mf.get("lookup_table"),
                        lookup_column=mf.get("lookup_column"),
                        create_if_missing=mf.get("create_if_missing", False)
                    )
                    for mf in nae.get("match_fields", [])
                ],
                transformed_fields=[
                    TransformedField(
                        json_path=tf["json_field"],
                        column=tf["column"],
                        type=DataType(tf.get("type", "text")),
                        transform=tf["transform"],
                        nullable=tf.get("nullable", True)
                    )
                    for tf in nae.get("transformed_fields", [])
                ],
                sub_array_with_quantity=[
                    SubArrayMapping(
                        json_field=sa["json_field"],
                        junction_table=sa["junction_table"],
                        lookup_table=sa["lookup_table"],
                        lookup_column=sa["lookup_column"],
                        create_if_missing=sa.get("create_if_missing", True),
                        count_occurrences=sa.get("count_occurrences", False)
                    )
                    for sa in nae.get("sub_array_with_quantity", [])
                ],
                uniqueness_includes_sub_array=nae.get("uniqueness_includes_sub_array")
            )
            for nae in data.get("nested_array_entities", [])
        ]

        # Parse evolution mappings
        evolution_mappings = [
            EvolutionMapping(
                json_path=em["json_path"],
                junction_table=em["junction_table"],
                lookup_table=em["lookup_table"],
                lookup_column=em["lookup_column"],
                create_if_missing=em.get("create_if_missing", True),
                is_array=em.get("is_array", False),
                extra_columns=em.get("extra_columns", {})
            )
            for em in data.get("evolution_mappings", [])
        ]

        # Parse dynamic object mapping
        dynamic_object_mapping = [
            DynamicObjectMapping(
                json_path=dom["json_path"],
                junction_table=dom["junction_table"],
                key_lookup_table=dom["key_lookup_table"],
                key_lookup_column=dom["key_lookup_column"],
                value_column=dom["value_column"],
                value_transform=dom.get("value_transform"),
                create_keys_if_missing=dom.get("create_keys_if_missing", True)
            )
            for dom in data.get("dynamic_object_mapping", [])
        ]

        return cls(
            entity=data["entity"],
            version=data["version"],
            source=source,
            table=data["table"],
            primary_key=primary_key,
            strategy=LoadStrategy(data["strategy"]),
            conflict_target=data.get("conflict_target"),
            fields=fields,
            nested_fields=nested_fields,
            transformed_fields=transformed_fields,
            lookups=lookups,
            nested_entities=nested_entities,
            array_junctions=array_junctions,
            array_with_quantity=array_with_quantity,
            nested_array_entities=nested_array_entities,
            evolution_mappings=evolution_mappings,
            dynamic_object_mapping=dynamic_object_mapping
        )


@dataclass
class GameConfig:
    """Complete game configuration."""
    game: str
    version: str
    sources: List[SourceConfig]
    entities: List[EntityDependency]

    @classmethod
    def from_dict(cls, sources_data: Dict[str, Any], entities_data: Dict[str, Any]) -> "GameConfig":
        """Create GameConfig from source and entity dictionaries."""
        return cls(
            game=sources_data["game"],
            version=sources_data["version"],
            sources=[
                SourceConfig.from_dict(s) for s in sources_data.get("sources", [])
            ],
            entities=[
                EntityDependency(
                    name=e["name"],
                    config=e["config"],
                    dependencies=e.get("dependencies", [])
                )
                for e in entities_data.get("entities", [])
            ]
        )


@dataclass
class DatabaseConfig:
    """Database connection configuration."""
    host: str
    port: int
    database: str
    user: str
    password: str

    @classmethod
    def from_env(cls) -> "DatabaseConfig":
        """Create DatabaseConfig from environment variables."""
        import os
        return cls(
            host=os.getenv("PGHOST", "localhost"),
            port=int(os.getenv("PGPORT", "5432")),
            database=os.getenv("PGDATABASE", "tcg_decklists"),
            user=os.getenv("PGUSER", "postgres"),
            password=os.getenv("PGPASSWORD", "testing1234")
        )
