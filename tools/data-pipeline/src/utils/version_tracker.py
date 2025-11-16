"""
Version tracking utilities for the ETL pipeline.

Manages metadata.json to track data source versions and migration status.
"""

import json
import os
from datetime import datetime
from typing import Any, Dict, List, Optional
from pathlib import Path


class VersionTracker:
    """Tracks versions and status of data sources."""

    def __init__(self, metadata_file: str = "metadata.json"):
        """Initialize the version tracker."""
        self.metadata_file = Path(metadata_file)
        self.metadata: List[Dict[str, Any]] = []
        self._load_metadata()

    def _load_metadata(self) -> None:
        """Load metadata from file."""
        if self.metadata_file.exists():
            try:
                with open(self.metadata_file, 'r') as f:
                    self.metadata = json.load(f)
            except json.JSONDecodeError:
                self.metadata = []
        else:
            self.metadata = []

    def _save_metadata(self) -> None:
        """Save metadata to file."""
        with open(self.metadata_file, 'w') as f:
            json.dump(self.metadata, f, indent=2)

    def get_entry(self, name: str) -> Optional[Dict[str, Any]]:
        """Get metadata entry by name."""
        for entry in self.metadata:
            if entry.get("name") == name:
                return entry
        return None

    def get_version(self, name: str) -> Optional[str]:
        """Get current version for a data source."""
        entry = self.get_entry(name)
        return entry.get("version") if entry else None

    def has_updates(self, name: str, latest_version: str) -> bool:
        """Check if there are updates available."""
        current_version = self.get_version(name)
        return current_version != latest_version

    def update_entry(
        self,
        name: str,
        version: str,
        successful: bool,
        **kwargs
    ) -> None:
        """Update or create a metadata entry."""
        timestamp = datetime.utcnow().isoformat() + "Z"

        # Find existing entry
        entry = self.get_entry(name)

        if entry:
            # Update existing
            entry["version"] = version
            entry["successful"] = successful
            entry["timestamp"] = timestamp
            entry.update(kwargs)
        else:
            # Create new
            new_entry = {
                "name": name,
                "version": version,
                "successful": successful,
                "timestamp": timestamp,
                **kwargs
            }
            self.metadata.append(new_entry)

        self._save_metadata()

    def mark_successful(self, name: str, version: str) -> None:
        """Mark a migration as successful."""
        self.update_entry(name, version, successful=True)

    def mark_failed(self, name: str, version: str, error: Optional[str] = None) -> None:
        """Mark a migration as failed."""
        kwargs = {"error": error} if error else {}
        self.update_entry(name, version, successful=False, **kwargs)

    def get_all_entries(self) -> List[Dict[str, Any]]:
        """Get all metadata entries."""
        return self.metadata

    def create_entry_if_missing(
        self,
        name: str,
        source: str,
        context: str,
        output: str,
        script: str
    ) -> None:
        """Create an entry if it doesn't exist."""
        if not self.get_entry(name):
            entry = {
                "name": name,
                "source": source,
                "context": context,
                "output": output,
                "script": script,
                "version": "",
                "successful": False,
                "timestamp": ""
            }
            self.metadata.append(entry)
            self._save_metadata()
