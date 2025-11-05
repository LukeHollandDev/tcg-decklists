"""
Dependency resolution for data sources.

Implements topological sorting to determine the correct execution order
for data sources based on their dependencies.
"""

from typing import List, Set, Dict
from .metadata import DataSource


class CircularDependencyError(Exception):
    """Raised when a circular dependency is detected."""
    pass


class DependencyResolver:
    """Resolves dependencies between data sources."""

    @staticmethod
    def resolve(sources: List[DataSource]) -> List[DataSource]:
        """
        Resolve dependencies and return sources in execution order.

        Uses topological sort (Kahn's algorithm) to determine the correct
        order to execute data sources based on their dependencies.

        Args:
            sources: List of data sources to order

        Returns:
            List of data sources in execution order (dependencies first)

        Raises:
            CircularDependencyError: If a circular dependency is detected
            ValueError: If a dependency references a non-existent source
        """
        # Build a map of source names to sources
        source_map: Dict[str, DataSource] = {s.name: s for s in sources}

        # Validate that all dependencies exist
        for source in sources:
            for dep in source.dependencies:
                if dep not in source_map:
                    raise ValueError(
                        f"Source '{source.name}' depends on non-existent source '{dep}'"
                    )

        # Build the dependency graph
        # in_degree: number of dependencies for each source
        in_degree: Dict[str, int] = {s.name: 0 for s in sources}
        # adjacency list: sources that depend on each source
        graph: Dict[str, List[str]] = {s.name: [] for s in sources}

        for source in sources:
            for dep in source.dependencies:
                graph[dep].append(source.name)
                in_degree[source.name] += 1

        # Kahn's algorithm for topological sort
        queue: List[str] = []
        for name, degree in in_degree.items():
            if degree == 0:
                queue.append(name)

        result: List[DataSource] = []
        while queue:
            # Process sources with no remaining dependencies
            current = queue.pop(0)
            result.append(source_map[current])

            # Decrease in-degree for dependent sources
            for neighbor in graph[current]:
                in_degree[neighbor] -= 1
                if in_degree[neighbor] == 0:
                    queue.append(neighbor)

        # If we haven't processed all sources, there's a cycle
        if len(result) != len(sources):
            unprocessed = [name for name, degree in in_degree.items() if degree > 0]
            raise CircularDependencyError(
                f"Circular dependency detected involving: {', '.join(unprocessed)}"
            )

        return result

    @staticmethod
    def get_dependencies(source: DataSource, sources: List[DataSource],
                        recursive: bool = True) -> Set[str]:
        """
        Get all dependencies for a source.

        Args:
            source: The source to get dependencies for
            sources: All available sources
            recursive: If True, get transitive dependencies

        Returns:
            Set of dependency names
        """
        source_map = {s.name: s for s in sources}
        dependencies: Set[str] = set()

        def collect_deps(src: DataSource):
            for dep in src.dependencies:
                if dep not in dependencies:
                    dependencies.add(dep)
                    if recursive and dep in source_map:
                        collect_deps(source_map[dep])

        collect_deps(source)
        return dependencies
