"""
Metadata management for the data pipeline.

Handles loading, saving, and updating the metadata.json file that tracks
data source versions, dependencies, and execution status.
"""

import json
from pathlib import Path
from typing import List, Dict, Optional, Any
from datetime import datetime


class DataSource:
    """Represents a single data source configuration."""

    def __init__(self, data: Dict[str, Any]):
        self.name: str = data["name"]
        self.source: str = data["source"]
        self.context: str = data["context"]
        self.output: str = data["output"]
        self.script: str = data["script"]
        self.dependencies: List[str] = data.get("dependencies", [])

        # Version tracking
        self.version: Optional[str] = data.get("version")
        self.commit_message: Optional[str] = data.get("commit_message")

        # Execution status
        self.successful: Optional[bool] = data.get("successful")
        self.error_message: Optional[str] = data.get("error_message")
        self.timestamp: Optional[str] = data.get("timestamp")

    def to_dict(self) -> Dict[str, Any]:
        """Convert the data source to a dictionary for JSON serialization."""
        return {
            "name": self.name,
            "source": self.source,
            "context": self.context,
            "output": self.output,
            "script": self.script,
            "dependencies": self.dependencies,
            "version": self.version,
            "commit_message": self.commit_message,
            "successful": self.successful,
            "error_message": self.error_message,
            "timestamp": self.timestamp
        }

    def update_success(self, version: str, commit_message: str):
        """Update the data source after a successful migration."""
        self.version = version
        self.commit_message = commit_message
        self.successful = True
        self.error_message = None
        self.timestamp = datetime.utcnow().isoformat() + "Z"

    def update_failure(self, error_message: str):
        """Update the data source after a failed migration."""
        self.successful = False
        self.error_message = error_message
        self.timestamp = datetime.utcnow().isoformat() + "Z"


class Metadata:
    """Manages the metadata.json file."""

    def __init__(self, metadata_path: Path):
        self.metadata_path = metadata_path
        self.sources: List[DataSource] = []
        self.load()

    def load(self):
        """Load metadata from the JSON file."""
        if not self.metadata_path.exists():
            raise FileNotFoundError(f"Metadata file not found: {self.metadata_path}")

        with open(self.metadata_path, 'r') as f:
            data = json.load(f)

        self.sources = [DataSource(item) for item in data]

    def save(self):
        """Save metadata to the JSON file."""
        data = [source.to_dict() for source in self.sources]

        with open(self.metadata_path, 'w') as f:
            json.dump(data, f, indent=2)
            f.write('\n')  # Add trailing newline

    def get_source(self, name: str) -> Optional[DataSource]:
        """Get a data source by name."""
        for source in self.sources:
            if source.name == name:
                return source
        return None

    def update_source(self, name: str, version: str, commit_message: str,
                     successful: bool, error_message: Optional[str] = None):
        """Update a data source's execution status."""
        source = self.get_source(name)
        if source is None:
            raise ValueError(f"Data source not found: {name}")

        if successful:
            source.update_success(version, commit_message)
        else:
            source.update_failure(error_message or "Unknown error")

        self.save()
