"""
Data downloader for the data pipeline.

Handles cloning GitHub repositories and extracting specific paths to
the output directory.
"""

import subprocess
import shutil
from pathlib import Path
from typing import Optional
import tempfile


class DownloadError(Exception):
    """Raised when data download fails."""
    pass


class Downloader:
    """Downloads data from GitHub repositories."""

    @staticmethod
    def download(repo_url: str, context_path: str, output_path: Path,
                 commit_sha: Optional[str] = None) -> None:
        """
        Download data from a GitHub repository.

        Clones the repository to a temporary directory, extracts the specified
        context path, and copies it to the output directory.

        Args:
            repo_url: GitHub repository URL to clone
            context_path: Path within the repository to extract
            output_path: Local path where data should be saved
            commit_sha: Specific commit SHA to checkout (optional)

        Raises:
            DownloadError: If the download or extraction fails
        """
        temp_dir = None
        try:
            # Create a temporary directory for cloning
            temp_dir = Path(tempfile.mkdtemp(prefix="tcg-pipeline-"))

            # Clone the repository
            clone_cmd = [
                "git", "clone",
                "--depth", "1",  # Shallow clone for speed
                "--quiet",       # Suppress output
                repo_url,
                str(temp_dir / "repo")
            ]

            result = subprocess.run(
                clone_cmd,
                capture_output=True,
                text=True
            )

            if result.returncode != 0:
                raise DownloadError(
                    f"Failed to clone repository: {result.stderr}"
                )

            repo_dir = temp_dir / "repo"

            # Checkout specific commit if provided
            if commit_sha:
                checkout_cmd = ["git", "-C", str(repo_dir), "checkout", commit_sha]
                result = subprocess.run(
                    checkout_cmd,
                    capture_output=True,
                    text=True
                )

                if result.returncode != 0:
                    raise DownloadError(
                        f"Failed to checkout commit {commit_sha}: {result.stderr}"
                    )

            # Verify the context path exists
            source_path = repo_dir / context_path
            if not source_path.exists():
                raise DownloadError(
                    f"Context path '{context_path}' not found in repository"
                )

            # Create output directory if it doesn't exist
            output_path.parent.mkdir(parents=True, exist_ok=True)

            # Remove existing output directory if it exists
            if output_path.exists():
                shutil.rmtree(output_path)

            # Copy the context path to the output directory
            if source_path.is_dir():
                shutil.copytree(source_path, output_path)
            else:
                output_path.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(source_path, output_path)

        except subprocess.SubprocessError as e:
            raise DownloadError(f"Subprocess error during download: {e}")
        except OSError as e:
            raise DownloadError(f"File system error during download: {e}")
        finally:
            # Clean up temporary directory
            if temp_dir and temp_dir.exists():
                shutil.rmtree(temp_dir, ignore_errors=True)

    @staticmethod
    def verify_git_available() -> bool:
        """
        Verify that git is available on the system.

        Returns:
            True if git is available, False otherwise
        """
        try:
            result = subprocess.run(
                ["git", "--version"],
                capture_output=True,
                timeout=5
            )
            return result.returncode == 0
        except (subprocess.SubprocessError, FileNotFoundError):
            return False
