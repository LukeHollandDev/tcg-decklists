#!/bin/bash
set -euo pipefail

# --- CONFIGURATION ---
DATA_DIR="data"
SCRIPTS_DIR="scripts"
METADATA_FILE="metadata.json"
SCHEMA_DIR="schema"
mkdir -p "$SCHEMA_DIR"

# --- PRECHECKS ---
if ! command -v jq &>/dev/null; then
  echo "Error: jq is required but not installed." >&2
  exit 1
fi

if ! command -v curl &>/dev/null; then
  echo "Error: curl is required but not installed." >&2
  exit 1
fi

if ! command -v python &>/dev/null; then
  echo "Error: python is required but not installed." >&2
  exit 1
fi

if ! [ -f "$METADATA_FILE" ]; then
  echo "Error: $METADATA_FILE not found." >&2
  exit 1
fi

# --- FUNCTIONS ---
update_metadata() {
  jq --arg name "$name" \
      --arg sha "$latest_sha" \
      --arg date "$latest_date" \
      --argjson success "$success" \
      'map(if .name == $name then
            .version = $sha |
            .timestamp = $date |
            .successful = $success
          else . end)' \
      "$METADATA_FILE" > "${METADATA_FILE}.tmp" && mv "${METADATA_FILE}.tmp" "$METADATA_FILE"
}

# --- PYTHON ENV SETUP ---
REQUIREMENTS_FILE="requirements.txt"
venv_dir=$(mktemp -d)
python -m venv "$venv_dir"
source "$venv_dir/bin/activate"
pip install --upgrade pip >/dev/null 2>&1
pip install -r "$REQUIREMENTS_FILE" >/dev/null 2>&1

# --- MAIN LOOP ---
updates_made=false

items=()
while IFS= read -r line; do
  items+=("$line")
done < <(jq -c '.[]' "$METADATA_FILE")

for item in "${items[@]}"; do
  name=$(jq -r '.name' <<<"$item")
  source=$(jq -r '.source' <<<"$item")
  context=$(jq -r '.context' <<<"$item")
  version=$(jq -r '.version' <<<"$item")

  repo_path=${source#https://github.com/}
  api_url="https://api.github.com/repos/${repo_path}/commits?per_page=1"

  echo "Checking $name ($repo_path)..."

  # Get latest commit info
  latest_commit_json=$(curl -s "$api_url" | jq '.[0]')
  latest_sha=$(jq -r '.sha' <<<"$latest_commit_json")
  latest_message=$(jq -r '.commit.message' <<<"$latest_commit_json")
  latest_date=$(jq -r '.commit.author.date' <<<"$latest_commit_json")

  if [[ "$latest_sha" == "$version" ]]; then
    echo "$name is up to date ($version)"
    continue
  fi

  echo "Updating $name to $latest_sha"

  data_path="$name"

  # Remove old data directory if exists
  if [[ -d "$data_path" ]]; then
    rm -rf "$data_path"
  fi

  # Clone the specific context path
  git clone --depth 1 "$source" "$data_path" >/dev/null 2>&1

  if [[ -n "$context" && "$context" != "." ]]; then
    # Move context folder content to the root of data_path
    tmp_dir=$(mktemp -d)
    mv "$data_path/$context"/* "$tmp_dir"/
    rm -rf "$data_path"
    mkdir -p "$data_path"
    mv "$tmp_dir"/* "$data_path"/
    rm -rf "$tmp_dir"
  fi

  success=false

  # --- Run Python migration script in shared virtual environment ---
  migrate_py="$SCRIPTS_DIR/${name}-migrate.py"
  if [[ -f "$migrate_py" ]]; then
    echo "Running Python migrate script for $name..."
    if python "$migrate_py"; then
      echo "Migration succeeded."
      success=true
    else
      echo "Migration failed."
      success=false
    fi
  else
    echo "No Python migrate script found for $name."
  fi

  # --- Update metadata for this item ---
  update_metadata
  updates_made=true

  # --- Generate JSON Schema if genson is available ---
  if command -v genson &>/dev/null; then
    echo "Generating/updating schema for $name..."
    SCHEMA_FILE="$SCHEMA_DIR/$name.json"
    JSON_FILES=$(find "$data_path" -name '*.json')

    if [[ -n "$JSON_FILES" ]]; then
      genson -i 2 $JSON_FILES > "$SCHEMA_FILE"
      echo "Schema written to $SCHEMA_FILE"
    else
      echo "No JSON files found in $data_path for schema generation."
    fi
  else
    echo "Genson not installed, skipping schema generation."
  fi

done

rm -rf "$venv_dir"

exit 0
