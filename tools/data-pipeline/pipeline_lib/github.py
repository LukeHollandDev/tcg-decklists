"""
GitHub API integration for the data pipeline.

Handles fetching commit information from GitHub repositories.
"""

import re
from typing import Optional, Tuple
import urllib.request
import json


class GitHubAPI:
    """Interface to GitHub API for fetching commit information."""

    API_BASE = "https://api.github.com"

    @staticmethod
    def parse_repo_url(url: str) -> Optional[Tuple[str, str]]:
        """
        Parse a GitHub repository URL to extract owner and repo name.

        Args:
            url: GitHub repository URL (e.g., https://github.com/owner/repo)

        Returns:
            Tuple of (owner, repo) or None if URL is invalid
        """
        pattern = r'github\.com[:/]([^/]+)/([^/\.]+)'
        match = re.search(pattern, url)
        if match:
            return match.group(1), match.group(2)
        return None

    @classmethod
    def get_latest_commit(cls, repo_url: str, branch: str = "master") -> Tuple[str, str]:
        """
        Get the latest commit SHA and message from a GitHub repository.

        Args:
            repo_url: GitHub repository URL
            branch: Branch name to fetch from (default: master)

        Returns:
            Tuple of (commit_sha, commit_message)

        Raises:
            ValueError: If the repository URL is invalid
            urllib.error.URLError: If the API request fails
        """
        parsed = cls.parse_repo_url(repo_url)
        if not parsed:
            raise ValueError(f"Invalid GitHub repository URL: {repo_url}")

        owner, repo = parsed

        # Try master branch first, fall back to main if it fails
        branches_to_try = [branch]
        if branch == "master":
            branches_to_try.append("main")

        last_error = None
        for branch_name in branches_to_try:
            try:
                api_url = f"{cls.API_BASE}/repos/{owner}/{repo}/commits/{branch_name}"
                req = urllib.request.Request(api_url)
                req.add_header('Accept', 'application/vnd.github.v3+json')

                with urllib.request.urlopen(req, timeout=10) as response:
                    data = json.loads(response.read().decode('utf-8'))
                    commit_sha = data['sha']
                    commit_message = data['commit']['message'].split('\n')[0]
                    return commit_sha, commit_message

            except urllib.error.HTTPError as e:
                if e.code == 404 and branch_name != branches_to_try[-1]:
                    # Branch not found, try the next one
                    last_error = e
                    continue
                raise
            except Exception as e:
                last_error = e
                if branch_name != branches_to_try[-1]:
                    continue
                raise

        # If we get here, all branches failed
        if last_error:
            raise last_error
        raise ValueError(f"Unable to fetch commit from {repo_url}")

    @classmethod
    def has_new_commits(cls, repo_url: str, current_version: Optional[str],
                        branch: str = "master") -> Tuple[bool, str, str]:
        """
        Check if a repository has new commits since the current version.

        Args:
            repo_url: GitHub repository URL
            current_version: Current commit SHA (None if never fetched)
            branch: Branch name to check (default: master)

        Returns:
            Tuple of (has_changes, latest_sha, latest_commit_message)
        """
        latest_sha, latest_message = cls.get_latest_commit(repo_url, branch)

        if current_version is None:
            # Never fetched before, so there are "new" commits
            return True, latest_sha, latest_message

        has_changes = latest_sha != current_version
        return has_changes, latest_sha, latest_message
