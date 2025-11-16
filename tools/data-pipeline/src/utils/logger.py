"""
Logging utilities for the ETL pipeline.

Provides structured logging with progress tracking and error reporting.
"""

import logging
import sys
from typing import Optional
from datetime import datetime


class ETLLogger:
    """Custom logger for ETL pipeline with structured output."""

    def __init__(self, name: str = "etl-pipeline", level: int = logging.INFO):
        """Initialize the ETL logger."""
        self.logger = logging.getLogger(name)
        self.logger.setLevel(level)

        # Remove existing handlers
        self.logger.handlers = []

        # Create console handler with formatting
        handler = logging.StreamHandler(sys.stdout)
        handler.setLevel(level)

        # Format: timestamp - level - message
        formatter = logging.Formatter(
            '%(asctime)s - %(levelname)s - %(message)s',
            datefmt='%Y-%m-%d %H:%M:%S'
        )
        handler.setFormatter(formatter)

        self.logger.addHandler(handler)

    def info(self, message: str) -> None:
        """Log info message."""
        self.logger.info(message)

    def error(self, message: str, exc: Optional[Exception] = None) -> None:
        """Log error message."""
        if exc:
            self.logger.error(f"{message}: {str(exc)}")
        else:
            self.logger.error(message)

    def warning(self, message: str) -> None:
        """Log warning message."""
        self.logger.warning(message)

    def debug(self, message: str) -> None:
        """Log debug message."""
        self.logger.debug(message)

    def phase(self, phase_name: str, message: str = "") -> None:
        """Log a phase header."""
        separator = "=" * 60
        self.logger.info("")
        self.logger.info(separator)
        self.logger.info(f"  {phase_name}")
        if message:
            self.logger.info(f"  {message}")
        self.logger.info(separator)
        self.logger.info("")

    def progress(self, current: int, total: int, item_name: str = "items") -> None:
        """Log progress information."""
        percentage = (current / total * 100) if total > 0 else 0
        self.logger.info(f"Progress: {current}/{total} {item_name} ({percentage:.1f}%)")

    def success(self, message: str) -> None:
        """Log success message."""
        self.logger.info(f"✓ {message}")

    def failure(self, message: str) -> None:
        """Log failure message."""
        self.logger.error(f"✗ {message}")

    def timer_start(self, operation: str) -> datetime:
        """Start timing an operation."""
        self.logger.info(f"Starting: {operation}")
        return datetime.now()

    def timer_end(self, operation: str, start_time: datetime) -> None:
        """End timing an operation and log duration."""
        duration = (datetime.now() - start_time).total_seconds()
        self.logger.info(f"Completed: {operation} (took {duration:.2f}s)")


# Global logger instance
_logger: Optional[ETLLogger] = None


def get_logger() -> ETLLogger:
    """Get or create the global logger instance."""
    global _logger
    if _logger is None:
        _logger = ETLLogger()
    return _logger


def set_log_level(level: int) -> None:
    """Set the log level for the global logger."""
    logger = get_logger()
    logger.logger.setLevel(level)
    for handler in logger.logger.handlers:
        handler.setLevel(level)
