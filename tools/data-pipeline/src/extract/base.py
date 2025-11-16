"""
Base extractor class for data extraction.

Defines the interface for all data extractors.
"""

from abc import ABC, abstractmethod
from typing import Any, Dict, Optional
from pathlib import Path


class BaseExtractor(ABC):
    """Abstract base class for data extractors."""

    def __init__(self, output_dir: str = "data"):
        """Initialize the extractor."""
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)

    @abstractmethod
    def extract(
        self,
        source: str,
        destination: str,
        context: Optional[str] = None
    ) -> bool:
        """
        Extract data from source to destination.

        Args:
            source: Source location (URL, path, etc.)
            destination: Destination directory
            context: Optional context/subdirectory within source

        Returns:
            bool: True if extraction successful, False otherwise
        """
        pass

    @abstractmethod
    def get_latest_version(self, source: str) -> Optional[str]:
        """
        Get the latest version identifier for a source.

        Args:
            source: Source location

        Returns:
            Optional[str]: Version identifier or None if unavailable
        """
        pass

    @abstractmethod
    def needs_update(self, source: str, current_version: Optional[str]) -> bool:
        """
        Check if source has updates since current_version.

        Args:
            source: Source location
            current_version: Currently tracked version

        Returns:
            bool: True if updates available, False otherwise
        """
        pass

    def cleanup(self, path: Path) -> None:
        """Clean up temporary files."""
        import shutil
        if path.exists():
            shutil.rmtree(path)
