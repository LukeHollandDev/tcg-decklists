#!/bin/bash
set -euo pipefail

# --- CONFIGURATION ---
DATA_DIR="data"
SCRIPTS_DIR="$DATA_DIR/scripts"
METADATA_FILE="$DATA_DIR/metadata.json"

# --- PRECHECKS ---
if ! command -v jq &>/dev/null; then
  echo "Error: jq is required but not installed." >&2
  exit 1
fi

if ! command -v curl &>/dev/null; then
  echo "Error: curl is required but not installed." >&2
  exit 1
fi

if ! [ -f "$METADATA_FILE" ]; then
  echo "Error: $METADATA_FILE not found." >&2
  exit 1
fi

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

  data_path="$DATA_DIR/$name"

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

  # Run prepare and migrate scripts if they exist
  prepare_script="$SCRIPTS_DIR/${name}-prepare.sh"
  migrate_script="$SCRIPTS_DIR/${name}-migrate.sh"
  success=false

  if [[ -x "$prepare_script" ]]; then
    echo "Running prepare script for $name..."
    if "$prepare_script"; then
      echo "Prepare succeeded."
    else
      echo "Prepare failed."
      success=false
      # Update metadata immediately and skip migration
      update_metadata
      updates_made=true
      continue
    fi
  else
    echo "No prepare script found for $name."
  fi

  if [[ -x "$migrate_script" ]]; then
    echo "Running migrate script for $name..."
    if "$migrate_script"; then
      echo "Migration succeeded."
      success=true
    else
      echo "Migration failed."
      success=false
    fi
  else
    echo "No migrate script found for $name."
  fi

  # --- Update metadata for this item ---
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
  update_metadata
  updates_made=true
done

# --- COMMIT AND PUSH CHANGES IF ANY ---
# if git diff --quiet "$DATA_DIR" "$METADATA_FILE"; then
#   echo "No updates to commit."
# else
#   echo "Committing and pushing changes..."
#   git add "$DATA_DIR" "$METADATA_FILE"
#   git commit -m "Automated data update: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
#   git push
#   updates_made=true
# fi

# if [[ "$updates_made" == true ]]; then
#   echo "All updates processed successfully."
# else
#   echo "No updates were required."
# fi

exit 0
