"""
Transform functions library for the ETL pipeline.

Provides reusable transformation functions that can be applied to data fields.
"""

import re
from typing import Any, Callable, Dict, Optional
from datetime import datetime


# Transform function registry
_TRANSFORMS: Dict[str, Callable] = {}


def register_transform(name: str):
    """Decorator to register a transform function."""
    def decorator(func: Callable):
        _TRANSFORMS[name] = func
        return func
    return decorator


def get_transform(name: str) -> Optional[Callable]:
    """Get a transform function by name."""
    return _TRANSFORMS.get(name)


def list_transforms() -> list[str]:
    """List all available transform names."""
    return list(_TRANSFORMS.keys())


# ===== String Transforms =====

@register_transform("lowercase")
def lowercase(value: Any) -> Optional[str]:
    """Convert string to lowercase."""
    if value is None:
        return None
    return str(value).lower()


@register_transform("uppercase")
def uppercase(value: Any) -> Optional[str]:
    """Convert string to uppercase."""
    if value is None:
        return None
    return str(value).upper()


@register_transform("trim")
def trim(value: Any) -> Optional[str]:
    """Trim whitespace from string."""
    if value is None:
        return None
    return str(value).strip()


@register_transform("none")
def none_transform(value: Any) -> Any:
    """No transformation (pass-through)."""
    return value


# ===== Numeric Transforms =====

@register_transform("parse_integer")
def parse_integer(value: Any) -> Optional[int]:
    """Parse string to integer."""
    if value is None or value == "":
        return None
    try:
        return int(value)
    except (ValueError, TypeError):
        return None


@register_transform("parse_float")
def parse_float(value: Any) -> Optional[float]:
    """Parse string to float."""
    if value is None or value == "":
        return None
    try:
        return float(value)
    except (ValueError, TypeError):
        return None


# ===== Pokemon-Specific Transforms =====

@register_transform("parse_damage_numeric")
def parse_damage_numeric(value: Any) -> Optional[int]:
    """
    Parse Pokemon damage string to numeric value.

    Examples:
        "50" -> 50
        "50+" -> 50
        "30×" -> 30
        "" -> None
    """
    if not value:
        return None

    damage_str = str(value).strip()

    # Extract numeric part using regex
    match = re.match(r'^(\d+)', damage_str)
    if match:
        return int(match.group(1))

    return None


@register_transform("parse_damage_modifier")
def parse_damage_modifier(value: Any) -> Optional[str]:
    """
    Parse Pokemon damage modifier from damage string.

    Examples:
        "50+" -> "+"
        "30×" -> "×"
        "50" -> None
    """
    if not value:
        return None

    damage_str = str(value).strip()

    # Check for modifiers
    if '+' in damage_str:
        return '+'
    elif '×' in damage_str or 'x' in damage_str.lower():
        return '×'
    elif '-' in damage_str:
        return '-'

    return None


@register_transform("extract_set_id")
def extract_set_id(value: Any) -> Optional[str]:
    """
    Extract set ID from Pokemon card ID.

    Examples:
        "base1-1" -> "base1"
        "xy1-25" -> "xy1"
    """
    if not value:
        return None

    card_id = str(value)

    if '-' in card_id:
        return card_id.rsplit('-', 1)[0]

    return card_id


# ===== Date/Time Transforms =====

@register_transform("parse_date")
def parse_date(value: Any) -> Optional[str]:
    """
    Parse date string to ISO format.

    Handles various date formats and converts to YYYY-MM-DD.
    """
    if not value:
        return None

    date_str = str(value).strip()

    # Try common date formats
    formats = [
        "%Y-%m-%d",
        "%m/%d/%Y",
        "%d/%m/%Y",
        "%Y/%m/%d",
        "%B %d, %Y",
        "%d %B %Y"
    ]

    for fmt in formats:
        try:
            dt = datetime.strptime(date_str, fmt)
            return dt.strftime("%Y-%m-%d")
        except ValueError:
            continue

    # If no format matched, return original
    return date_str


@register_transform("parse_datetime")
def parse_datetime(value: Any) -> Optional[str]:
    """
    Parse datetime string to ISO format.

    Returns:
        ISO 8601 datetime string or None
    """
    if not value:
        return None

    datetime_str = str(value).strip()

    # Try ISO format first
    try:
        dt = datetime.fromisoformat(datetime_str.replace('Z', '+00:00'))
        return dt.isoformat()
    except ValueError:
        pass

    # Try common datetime formats
    formats = [
        "%Y-%m-%d %H:%M:%S",
        "%Y-%m-%dT%H:%M:%S",
        "%m/%d/%Y %H:%M:%S",
        "%d/%m/%Y %H:%M:%S"
    ]

    for fmt in formats:
        try:
            dt = datetime.strptime(datetime_str, fmt)
            return dt.isoformat()
        except ValueError:
            continue

    # If no format matched, return original
    return datetime_str


# ===== Utility Functions =====

def apply_transform(transform_name: str, value: Any) -> Any:
    """
    Apply a transform function by name to a value.

    Args:
        transform_name: Name of the transform function
        value: Value to transform

    Returns:
        Transformed value

    Raises:
        ValueError: If transform name not found
    """
    transform_func = get_transform(transform_name)

    if transform_func is None:
        raise ValueError(f"Unknown transform: {transform_name}")

    return transform_func(value)
