#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
SQL_FILE="$ROOT_DIR/server/src/main/resources/init.sql"
DB_NAME="${LAB5_DB_NAME:-studs}"
DB_HOST="${LAB5_DB_HOST:-pg}"
DB_PORT="${LAB5_DB_PORT:-5432}"
DB_LOGIN="${1:-$USER}"

if ! command -v psql >/dev/null 2>&1; then
  echo "psql not found. Install PostgreSQL client tools first."
  exit 1
fi

if [[ ! -f "$SQL_FILE" ]]; then
  echo "SQL file not found: $SQL_FILE"
  exit 1
fi

echo "WARNING: $SQL_FILE contains DROP TABLE statements."
echo "This script will recreate schema in $DB_NAME on $DB_HOST:$DB_PORT."
read -r -p "Type YES to continue: " ANSWER
if [[ "$ANSWER" != "YES" ]]; then
  echo "Cancelled."
  exit 1
fi

echo "Applying schema as '$DB_LOGIN'..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_LOGIN" -d "$DB_NAME" -v ON_ERROR_STOP=1 -f "$SQL_FILE"
echo "Schema applied successfully."
