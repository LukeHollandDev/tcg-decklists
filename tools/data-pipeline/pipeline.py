#!/usr/bin/env python3
"""
TCG Decklists Data Pipeline

Main CLI for downloading card data from external sources and migrating
it to the database.
"""

import argparse
import sys
import os
from pathlib import Path
from typing import List

# Add the pipeline_lib to the path
sys.path.insert(0, str(Path(__file__).parent))

from pipeline_lib.metadata import Metadata, DataSource
from pipeline_lib.github import GitHubAPI
from pipeline_lib.downloader import Downloader, DownloadError
from pipeline_lib.executor import Executor, ExecutionError
from pipeline_lib.dependencies import DependencyResolver, CircularDependencyError


class Pipeline:
    """Main pipeline orchestrator."""

    def __init__(self, base_path: Path, force: bool = False):
        """
        Initialize the pipeline.

        Args:
            base_path: Base path of the data pipeline directory
            force: If True, ignore version checks and force re-download
        """
        self.base_path = base_path
        self.force = force
        self.metadata_path = base_path / "metadata.json"
        self.requirements_path = base_path / "requirements.txt"
        self.venv_path = base_path / "venv"

        # Load metadata
        self.metadata = Metadata(self.metadata_path)

        # Initialize components
        self.github = GitHubAPI()
        self.downloader = Downloader()
        self.executor = Executor()

    def run(self):
        """Run the pipeline."""
        print("=" * 60)
        print("TCG Decklists Data Pipeline")
        print("=" * 60)

        # Verify prerequisites
        if not self._verify_prerequisites():
            return 1

        # Resolve dependencies
        try:
            ordered_sources = DependencyResolver.resolve(self.metadata.sources)
        except (CircularDependencyError, ValueError) as e:
            print(f"ERROR: {e}")
            return 1

        print(f"\nFound {len(ordered_sources)} data source(s)")

        # Set up virtual environment
        try:
            python_exe = Executor.ensure_venv(self.venv_path, self.requirements_path)
        except ExecutionError as e:
            print(f"ERROR: {e}")
            return 1

        # Process each source
        failed_sources = []
        executed_scripts = set()

        for source in ordered_sources:
            print(f"\n{'=' * 60}")
            print(f"Processing: {source.name}")
            print(f"{'=' * 60}")

            # Check if dependencies failed
            if not self._check_dependencies(source, failed_sources):
                print(f"SKIPPED: Dependencies failed for {source.name}")
                failed_sources.append(source.name)
                continue

            # Check for updates
            try:
                has_changes, latest_sha, commit_msg = self._check_for_updates(source)
            except Exception as e:
                error_msg = f"Failed to check for updates: {e}"
                print(f"ERROR: {error_msg}")
                self.metadata.update_source(
                    source.name,
                    source.version or "",
                    source.commit_message or "",
                    successful=False,
                    error_message=error_msg
                )
                failed_sources.append(source.name)
                continue

            if not has_changes and not self.force:
                print(f"SKIPPED: No changes detected (version: {source.version[:8]})")
                continue

            # Download data
            try:
                print(f"Downloading from {source.source}...")
                output_path = self.base_path / source.output
                self.downloader.download(
                    source.source,
                    source.context,
                    output_path,
                    commit_sha=latest_sha
                )
                print(f"Downloaded to {output_path}")
            except DownloadError as e:
                error_msg = f"Failed to download: {e}"
                print(f"ERROR: {error_msg}")
                self.metadata.update_source(
                    source.name,
                    latest_sha,
                    commit_msg,
                    successful=False,
                    error_message=error_msg
                )
                failed_sources.append(source.name)
                continue

            # Execute migration script (only once per script)
            if source.script not in executed_scripts:
                script_path = self.base_path / source.script

                success, error_msg = self.executor.execute_script(
                    script_path,
                    python_exe=python_exe,
                    env=os.environ.copy()
                )

                executed_scripts.add(source.script)

                if success:
                    print(f"SUCCESS: Migration completed")
                    self.metadata.update_source(
                        source.name,
                        latest_sha,
                        commit_msg,
                        successful=True
                    )
                else:
                    print(f"ERROR: Migration failed")
                    print(f"  {error_msg}")
                    self.metadata.update_source(
                        source.name,
                        latest_sha,
                        commit_msg,
                        successful=False,
                        error_message=error_msg
                    )
                    failed_sources.append(source.name)
            else:
                print(f"SKIPPED: Script already executed in this run")
                self.metadata.update_source(
                    source.name,
                    latest_sha,
                    commit_msg,
                    successful=True
                )

        # Summary
        print(f"\n{'=' * 60}")
        print("Pipeline Summary")
        print(f"{'=' * 60}")
        print(f"Total sources: {len(ordered_sources)}")
        print(f"Failed sources: {len(failed_sources)}")

        if failed_sources:
            print("\nFailed sources:")
            for name in failed_sources:
                print(f"  - {name}")
            return 1

        print("\nAll sources processed successfully!")
        return 0

    def status(self):
        """Display pipeline status."""
        print("=" * 60)
        print("TCG Decklists Data Pipeline Status")
        print("=" * 60)

        for source in self.metadata.sources:
            print(f"\n{source.name}:")
            print(f"  Source: {source.source}")
            print(f"  Script: {source.script}")
            print(f"  Dependencies: {', '.join(source.dependencies) if source.dependencies else 'None'}")

            if source.version:
                print(f"  Version: {source.version[:8]}")
                print(f"  Commit: {source.commit_message or 'N/A'}")
                print(f"  Status: {'✓ Success' if source.successful else '✗ Failed'}")
                if not source.successful and source.error_message:
                    print(f"  Error: {source.error_message[:100]}...")
                print(f"  Last run: {source.timestamp or 'Never'}")
            else:
                print(f"  Status: Never run")

        return 0

    def _verify_prerequisites(self) -> bool:
        """Verify that required tools are available."""
        if not Downloader.verify_git_available():
            print("ERROR: git is not available. Please install git.")
            return False

        if not Executor.verify_python_available():
            print("ERROR: Python is not available.")
            return False

        return True

    def _check_for_updates(self, source: DataSource) -> tuple:
        """Check if a source has updates available."""
        if self.force:
            # In force mode, always get latest commit info
            latest_sha, commit_msg = self.github.get_latest_commit(source.source)
            return True, latest_sha, commit_msg

        has_changes, latest_sha, commit_msg = self.github.has_new_commits(
            source.source,
            source.version
        )
        return has_changes, latest_sha, commit_msg

    def _check_dependencies(self, source: DataSource,
                           failed_sources: List[str]) -> bool:
        """Check if all dependencies for a source are satisfied."""
        for dep in source.dependencies:
            if dep in failed_sources:
                return False
        return True


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description="TCG Decklists Data Pipeline",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )

    subparsers = parser.add_subparsers(dest="command", help="Command to run")

    # Run command
    run_parser = subparsers.add_parser("run", help="Run the data pipeline")
    run_parser.add_argument(
        "--force",
        action="store_true",
        help="Force re-download and re-migrate all data, ignoring version checks"
    )

    # Status command
    subparsers.add_parser("status", help="Show pipeline status")

    args = parser.parse_args()

    # Default to 'run' if no command specified
    if not args.command:
        args.command = "run"
        args.force = False

    # Initialize pipeline
    base_path = Path(__file__).parent
    pipeline = Pipeline(base_path, force=getattr(args, "force", False))

    # Execute command
    if args.command == "run":
        return pipeline.run()
    elif args.command == "status":
        return pipeline.status()
    else:
        parser.print_help()
        return 1


if __name__ == "__main__":
    sys.exit(main())
