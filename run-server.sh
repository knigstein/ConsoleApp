#!/bin/sh
# Server startup script for Lab 5
# Usage: ./run-server.sh [data.xml] [port]

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DATA_FILE="${1:-data.xml}"
PORT="${2:-5555}"

# Check if JAR exists, build if not
if [ ! -f "$SCRIPT_DIR/dist/server.jar" ]; then
    echo "JAR not found. Building first..."
    if [ -f "$SCRIPT_DIR/build.sh" ]; then
        sh "$SCRIPT_DIR/build.sh"
    else
        echo "Error: build.sh not found."
        exit 1
    fi
fi

echo "Starting Lab 5 Server..."
echo "Data file: $DATA_FILE"
echo "Port: $PORT"
echo ""

java -jar "$SCRIPT_DIR/dist/server.jar" "$DATA_FILE" "$PORT"
