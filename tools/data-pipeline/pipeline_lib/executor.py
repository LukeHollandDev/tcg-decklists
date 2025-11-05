"""
Script executor for the data pipeline.

Handles running migration scripts in a virtual environment with proper
error handling and logging.
"""

import subprocess
import sys
from pathlib import Path
from typing import Optional, Tuple


class ExecutionError(Exception):
    """Raised when script execution fails."""
    pass


class Executor:
    """Executes migration scripts."""

    def __init__(self, python_path: Optional[Path] = None):
        """
        Initialize the executor.

        Args:
            python_path: Path to Python interpreter (default: current Python)
        """
        self.python_path = python_path or Path(sys.executable)

    @staticmethod
    def ensure_venv(venv_path: Path, requirements_path: Path) -> Path:
        """
        Ensure a virtual environment exists and has dependencies installed.

        Args:
            venv_path: Path where virtual environment should be created
            requirements_path: Path to requirements.txt file

        Returns:
            Path to the Python interpreter in the virtual environment

        Raises:
            ExecutionError: If virtual environment creation or installation fails
        """
        python_exe = venv_path / "bin" / "python"

        # Create virtual environment if it doesn't exist
        if not venv_path.exists():
            print(f"Creating virtual environment at {venv_path}...")
            try:
                subprocess.run(
                    [sys.executable, "-m", "venv", str(venv_path)],
                    check=True,
                    capture_output=True,
                    text=True
                )
            except subprocess.CalledProcessError as e:
                raise ExecutionError(
                    f"Failed to create virtual environment: {e.stderr}"
                )

        # Upgrade pip
        try:
            subprocess.run(
                [str(python_exe), "-m", "pip", "install", "--upgrade", "pip"],
                check=True,
                capture_output=True,
                text=True
            )
        except subprocess.CalledProcessError as e:
            raise ExecutionError(f"Failed to upgrade pip: {e.stderr}")

        # Install requirements
        if requirements_path.exists():
            print(f"Installing dependencies from {requirements_path}...")
            try:
                subprocess.run(
                    [str(python_exe), "-m", "pip", "install", "-r", str(requirements_path)],
                    check=True,
                    capture_output=True,
                    text=True
                )
            except subprocess.CalledProcessError as e:
                raise ExecutionError(
                    f"Failed to install dependencies: {e.stderr}"
                )

        return python_exe

    def execute_script(self, script_path: Path, python_exe: Optional[Path] = None,
                      env: Optional[dict] = None) -> Tuple[bool, Optional[str]]:
        """
        Execute a migration script.

        Args:
            script_path: Path to the Python script to execute
            python_exe: Python interpreter to use (default: self.python_path)
            env: Environment variables to pass to the script

        Returns:
            Tuple of (success, error_message)
        """
        if not script_path.exists():
            return False, f"Script not found: {script_path}"

        python = python_exe or self.python_path

        print(f"Executing {script_path.name}...")

        try:
            result = subprocess.run(
                [str(python), str(script_path)],
                capture_output=True,
                text=True,
                env=env,
                cwd=script_path.parent
            )

            if result.returncode != 0:
                error_msg = result.stderr or result.stdout or "Unknown error"
                return False, error_msg.strip()

            # Print script output for visibility
            if result.stdout:
                print(result.stdout)

            return True, None

        except subprocess.SubprocessError as e:
            return False, f"Subprocess error: {str(e)}"
        except Exception as e:
            return False, f"Unexpected error: {str(e)}"

    @staticmethod
    def verify_python_available() -> bool:
        """
        Verify that Python is available on the system.

        Returns:
            True if Python is available, False otherwise
        """
        try:
            result = subprocess.run(
                [sys.executable, "--version"],
                capture_output=True,
                timeout=5
            )
            return result.returncode == 0
        except (subprocess.SubprocessError, FileNotFoundError):
            return False
