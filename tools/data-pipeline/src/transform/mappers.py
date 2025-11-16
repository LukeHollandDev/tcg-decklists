"""
Field mapping utilities for the ETL pipeline.

Handles extraction of values from JSON data using various path notations.
"""

from typing import Any, Dict, List, Optional
from collections import Counter


class FieldMapper:
    """Handles mapping and extraction of fields from JSON data."""

    @staticmethod
    def get_value(data: Dict[str, Any], json_path: str) -> Any:
        """
        Extract value from JSON data using dot notation path.

        Args:
            data: Source JSON data
            json_path: Dot-separated path (e.g., "images.small")

        Returns:
            Any: Extracted value or None if path not found

        Examples:
            get_value({"name": "Pikachu"}, "name") -> "Pikachu"
            get_value({"images": {"small": "url"}}, "images.small") -> "url"
        """
        if not json_path:
            return None

        # Split path by dots
        keys = json_path.split('.')

        # Navigate through nested structure
        current = data
        for key in keys:
            if isinstance(current, dict) and key in current:
                current = current[key]
            else:
                return None

        return current

    @staticmethod
    def has_path(data: Dict[str, Any], json_path: str) -> bool:
        """
        Check if a path exists in JSON data.

        Args:
            data: Source JSON data
            json_path: Dot-separated path

        Returns:
            bool: True if path exists
        """
        value = FieldMapper.get_value(data, json_path)
        return value is not None

    @staticmethod
    def get_array_value(data: Dict[str, Any], json_path: str) -> List[Any]:
        """
        Extract array value from JSON data.

        Args:
            data: Source JSON data
            json_path: Dot-separated path to array

        Returns:
            List: Array value or empty list if not found
        """
        value = FieldMapper.get_value(data, json_path)

        if value is None:
            return []

        if isinstance(value, list):
            return value

        # Single value, wrap in list
        return [value]

    @staticmethod
    def count_array_occurrences(
        data: Dict[str, Any],
        json_path: str
    ) -> Dict[Any, int]:
        """
        Count occurrences of each unique value in an array.

        Args:
            data: Source JSON data
            json_path: Dot-separated path to array

        Returns:
            Dict: Mapping of value to count

        Example:
            count_array_occurrences(
                {"retreatCost": ["Colorless", "Colorless", "Fire"]},
                "retreatCost"
            )
            -> {"Colorless": 2, "Fire": 1}
        """
        array_value = FieldMapper.get_array_value(data, json_path)

        if not array_value:
            return {}

        return dict(Counter(array_value))

    @staticmethod
    def get_nested_object(data: Dict[str, Any], json_path: str) -> Optional[Dict[str, Any]]:
        """
        Extract nested object from JSON data.

        Args:
            data: Source JSON data
            json_path: Dot-separated path to object

        Returns:
            Optional[Dict]: Nested object or None
        """
        value = FieldMapper.get_value(data, json_path)

        if isinstance(value, dict):
            return value

        return None

    @staticmethod
    def get_nested_array_objects(
        data: Dict[str, Any],
        json_path: str
    ) -> List[Dict[str, Any]]:
        """
        Extract array of objects from JSON data.

        Args:
            data: Source JSON data
            json_path: Dot-separated path to array of objects

        Returns:
            List[Dict]: Array of objects or empty list

        Example:
            get_nested_array_objects(
                {"attacks": [{"name": "Tackle"}, {"name": "Thunder"}]},
                "attacks"
            )
            -> [{"name": "Tackle"}, {"name": "Thunder"}]
        """
        array_value = FieldMapper.get_array_value(data, json_path)

        # Filter only dict objects
        return [item for item in array_value if isinstance(item, dict)]

    @staticmethod
    def check_required_fields(
        data: Dict[str, Any],
        required_fields: List[str]
    ) -> bool:
        """
        Check if all required fields are present and non-null.

        Args:
            data: Source JSON data (can be nested object)
            required_fields: List of required field names (simple keys, not paths)

        Returns:
            bool: True if all required fields present and non-null
        """
        if not isinstance(data, dict):
            return False

        for field in required_fields:
            if field not in data or data[field] is None:
                return False

        return True

    @staticmethod
    def extract_dynamic_object_keys(
        data: Dict[str, Any],
        json_path: str
    ) -> Dict[str, Any]:
        """
        Extract object with dynamic keys.

        Args:
            data: Source JSON data
            json_path: Dot-separated path to object with dynamic keys

        Returns:
            Dict: Object with dynamic keys or empty dict

        Example:
            extract_dynamic_object_keys(
                {"legalities": {"unlimited": "Legal", "standard": "Banned"}},
                "legalities"
            )
            -> {"unlimited": "Legal", "standard": "Banned"}
        """
        obj = FieldMapper.get_nested_object(data, json_path)
        return obj if obj else {}
