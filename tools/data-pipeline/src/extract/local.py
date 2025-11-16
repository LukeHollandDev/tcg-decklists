"""
Local file data extractor.

Extracts data from local files.
"""

import json
import glob
from pathlib import Path
from typing import Any, Dict, List, Optional

from .base import BaseExtractor
from ..utils.logger import get_logger


class LocalExtractor(BaseExtractor):
    """Extracts data from local files."""

    def __init__(self, output_dir: str = "data"):
        """Initialize the local extractor."""
        super().__init__(output_dir)
        self.logger = get_logger()

    def extract(
        self,
        source: str,
        destination: str,
        context: Optional[str] = None
    ) -> bool:
        """
        Extract data from local files (no-op, just validates existence).

        Args:
            source: Local file path
            destination: Not used for local files
            context: Not used for local files

        Returns:
            bool: True if file exists and is readable
        """
        path = Path(source)
        return path.exists() and path.is_file()

    def get_latest_version(self, source: str) -> Optional[str]:
        """
        Get file modification time as version.

        Args:
            source: Local file path

        Returns:
            Optional[str]: File modification timestamp
        """
        path = Path(source)
        if path.exists():
            return str(int(path.stat().st_mtime))
        return None

    def needs_update(self, source: str, current_version: Optional[str]) -> bool:
        """
        Check if file has been modified.

        Args:
            source: Local file path
            current_version: Previous modification timestamp

        Returns:
            bool: True if file modified
        """
        latest_version = self.get_latest_version(source)
        if not latest_version or not current_version:
            return True

        return latest_version != current_version

    def load_json(self, file_path: str) -> Any:
        """
        Load JSON data from a file.

        Args:
            file_path: Path to JSON file

        Returns:
            Any: Parsed JSON data
        """
        path = Path(file_path)
        self.logger.debug(f"Loading JSON from: {file_path}")

        try:
            with open(path, 'r') as f:
                data = json.load(f)
            self.logger.success(f"Loaded JSON: {file_path}")
            return data
        except json.JSONDecodeError as e:
            self.logger.error(f"Invalid JSON in {file_path}", e)
            raise
        except Exception as e:
            self.logger.error(f"Failed to read {file_path}", e)
            raise

    def load_json_multi(self, pattern: str, consolidate: bool = False) -> Any:
        """
        Load multiple JSON files matching a pattern.

        Args:
            pattern: Glob pattern for JSON files
            consolidate: If True, merge all files into single array

        Returns:
            Any: List of parsed JSON data or consolidated array
        """
        file_paths = glob.glob(pattern)

        if not file_paths:
            self.logger.warning(f"No files found matching pattern: {pattern}")
            return [] if consolidate else {}

        self.logger.info(f"Found {len(file_paths)} files matching: {pattern}")

        all_data = []

        for file_path in sorted(file_paths):
            try:
                data = self.load_json(file_path)

                if consolidate:
                    # Consolidate into single array
                    if isinstance(data, list):
                        all_data.extend(data)
                    else:
                        all_data.append(data)
                else:
                    all_data.append(data)

            except Exception as e:
                self.logger.error(f"Skipping file due to error: {file_path}", e)
                continue

        if consolidate:
            self.logger.info(f"Consolidated {len(all_data)} records from {len(file_paths)} files")
            return all_data
        else:
            return all_data
