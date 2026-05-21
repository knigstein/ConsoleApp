#!/usr/bin/env bash
# Local GUI: ./run-gui-client.sh mock
# With server: ./run-gui-client.sh real localhost 5555
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
MODE="${1:-mock}"
HOST="${2:-localhost}"
PORT="${3:-5555}"
GRADLE_CMD=""

if command -v gradle >/dev/null 2>&1; then
  GRADLE_CMD="gradle"
else
  chmod +x "$BASE_DIR/gradlew"
  GRADLE_CMD="$BASE_DIR/gradlew"
fi

if [[ "$MODE" == "mock" ]]; then
  ARGS="--mock"
  echo "Starting GUI client in MOCK mode"
elif [[ "$MODE" == "real" ]]; then
  ARGS="$HOST $PORT"
  echo "Starting GUI client in REAL mode ($HOST:$PORT)"
else
  if [[ "$MODE" =~ ^[0-9]+$ ]]; then
    ARGS="localhost $MODE"
    echo "Starting GUI client in REAL mode (localhost:$MODE)"
  else
    ARGS="$MODE 5555"
    echo "Starting GUI client in REAL mode ($MODE:5555)"
    echo "Hint: use explicit mode './run-gui-client.sh real <host> <port>' for clarity."
  fi
fi

"$GRADLE_CMD" -p "$BASE_DIR" :client:runGuiClient -PappArgs="$ARGS"
