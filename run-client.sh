#!/bin/sh
# Client startup script for Lab 5
# Usage: ./run-client.sh [host] [port]

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
HOST="${1:-localhost}"
PORT="${2:-5555}"

# Check if JAR exists, build if not
if [ ! -f "$SCRIPT_DIR/dist/client.jar" ]; then
    echo "JAR not found. Building first..."
    if [ -f "$SCRIPT_DIR/build.sh" ]; then
        sh "$SCRIPT_DIR/build.sh"
    else
        echo "Error: build.sh not found."
        exit 1
    fi
fi

echo "Starting Lab 5 Client..."
echo "Host: $HOST"
echo "Port: $PORT"
echo ""

java -jar "$SCRIPT_DIR/dist/client.jar" "$HOST" "$PORT"
