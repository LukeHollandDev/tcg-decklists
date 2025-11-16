"""
Git-based data extractor.

Extracts data from Git repositories.
"""

import subprocess
import tempfile
import shutil
from pathlib import Path
from typing import Optional
import requests

from .base import BaseExtractor
from ..utils.logger import get_logger


class GitExtractor(BaseExtractor):
    """Extracts data from Git repositories."""

    def __init__(self, output_dir: str = "data"):
        """Initialize the Git extractor."""
        super().__init__(output_dir)
        self.logger = get_logger()

    def extract(
        self,
        source: str,
        destination: str,
        context: Optional[str] = None
    ) -> bool:
        """
        Extract data from a Git repository.

        Args:
            source: Git repository URL
            destination: Destination directory
            context: Optional subdirectory within repo to extract

        Returns:
            bool: True if extraction successful
        """
        dest_path = self.output_dir / destination

        # Remove old destination if it exists
        if dest_path.exists():
            shutil.rmtree(dest_path)

        # Create parent directory
        dest_path.parent.mkdir(parents=True, exist_ok=True)

        # Clone to temporary directory
        with tempfile.TemporaryDirectory() as tmp_dir:
            tmp_path = Path(tmp_dir)

            try:
                # Clone repository (shallow clone for speed)
                self.logger.info(f"Cloning repository: {source}")
                subprocess.run(
                    ["git", "clone", "--depth", "1", source, str(tmp_path)],
                    check=True,
                    capture_output=True,
                    text=True
                )

                # Extract specific context or entire repo
                if context and context != ".":
                    source_path = tmp_path / context
                    if not source_path.exists():
                        self.logger.error(f"Context path not found: {context}")
                        return False

                    # Copy context to destination
                    dest_path.mkdir(parents=True, exist_ok=True)
                    for item in source_path.iterdir():
                        if item.is_dir():
                            shutil.copytree(item, dest_path / item.name)
                        else:
                            shutil.copy2(item, dest_path / item.name)
                else:
                    # Move entire repo to destination
                    shutil.move(str(tmp_path), str(dest_path))

                self.logger.success(f"Extracted to: {destination}")
                return True

            except subprocess.CalledProcessError as e:
                self.logger.error(f"Git clone failed: {e.stderr}", e)
                return False
            except Exception as e:
                self.logger.error(f"Extraction failed", e)
                return False

    def get_latest_version(self, source: str) -> Optional[str]:
        """
        Get the latest commit hash from GitHub repository.

        Args:
            source: Git repository URL (GitHub)

        Returns:
            Optional[str]: Latest commit SHA or None
        """
        # Extract repo path from URL
        # https://github.com/owner/repo -> owner/repo
        if "github.com/" in source:
            repo_path = source.split("github.com/")[-1].rstrip("/").replace(".git", "")
            api_url = f"https://api.github.com/repos/{repo_path}/commits?per_page=1"

            try:
                response = requests.get(api_url, timeout=10)
                response.raise_for_status()
                commits = response.json()

                if commits and len(commits) > 0:
                    return commits[0]["sha"]
            except Exception as e:
                self.logger.error(f"Failed to get latest version: {e}")

        return None

    def needs_update(self, source: str, current_version: Optional[str]) -> bool:
        """
        Check if repository has new commits.

        Args:
            source: Git repository URL
            current_version: Current commit SHA

        Returns:
            bool: True if updates available
        """
        latest_version = self.get_latest_version(source)
        if not latest_version:
            return True  # Assume needs update if can't determine

        return latest_version != current_version
