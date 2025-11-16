"""
Configuration file loading utilities.

Handles loading and parsing of YAML configuration files.
"""

import yaml
from pathlib import Path
from typing import Any, Dict

from ..models.config import EntityConfig, GameConfig
from ..utils.logger import get_logger


class ConfigLoader:
    """Loads and parses configuration files."""

    def __init__(self, config_dir: str = "config"):
        """Initialize config loader."""
        self.config_dir = Path(config_dir)
        self.logger = get_logger()

    def load_yaml(self, file_path: Path) -> Dict[str, Any]:
        """Load a YAML file."""
        try:
            with open(file_path, 'r') as f:
                data = yaml.safe_load(f)
            self.logger.debug(f"Loaded config: {file_path}")
            return data if data else {}
        except yaml.YAMLError as e:
            self.logger.error(f"Invalid YAML in {file_path}", e)
            raise
        except Exception as e:
            self.logger.error(f"Failed to read {file_path}", e)
            raise

    def load_game_config(self, game_name: str) -> GameConfig:
        """
        Load complete game configuration.

        Args:
            game_name: Name of the game (e.g., "pokemon")

        Returns:
            GameConfig: Complete game configuration
        """
        game_dir = self.config_dir / "games" / game_name

        if not game_dir.exists():
            raise FileNotFoundError(f"Game config directory not found: {game_dir}")

        # Load sources.yaml
        sources_file = game_dir / "sources.yaml"
        if not sources_file.exists():
            raise FileNotFoundError(f"sources.yaml not found: {sources_file}")

        sources_data = self.load_yaml(sources_file)

        # Load entities.yaml
        entities_file = game_dir / "entities.yaml"
        if not entities_file.exists():
            raise FileNotFoundError(f"entities.yaml not found: {entities_file}")

        entities_data = self.load_yaml(entities_file)

        # Create GameConfig
        game_config = GameConfig.from_dict(sources_data, entities_data)

        self.logger.success(f"Loaded game config: {game_name}")
        return game_config

    def load_entity_config(self, game_name: str, config_file: str) -> EntityConfig:
        """
        Load entity configuration.

        Args:
            game_name: Name of the game
            config_file: Name of the entity config file (e.g., "sets.yaml")

        Returns:
            EntityConfig: Entity configuration
        """
        game_dir = self.config_dir / "games" / game_name
        entity_file = game_dir / config_file

        if not entity_file.exists():
            raise FileNotFoundError(f"Entity config not found: {entity_file}")

        entity_data = self.load_yaml(entity_file)

        # Create EntityConfig
        entity_config = EntityConfig.from_dict(entity_data)

        self.logger.debug(f"Loaded entity config: {config_file}")
        return entity_config
