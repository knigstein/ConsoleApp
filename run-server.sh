#!/bin/sh
# Server startup script for Lab 5 - PostgreSQL version
# Usage:
#   ./run-server.sh <db_login> [port]
#   ./run-server.sh <db_login> <db_password> [port]

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DB_LOGIN="${1}"
DB_PASSWORD=""
PORT="5555"

if [ -z "$DB_LOGIN" ]; then
    echo "Usage: $0 <db_login> [port]"
    echo "   or: $0 <db_login> <db_password> [port]"
    echo "  db_login    - PostgreSQL login"
    echo "  db_password - PostgreSQL password (optional)"
    echo "  port       - server port (default: 5555)"
    exit 1
fi

# If the second argument is numeric, treat it as port and use empty DB password.
if [ -n "$2" ]; then
    case "$2" in
        ''|*[!0-9]*)
            DB_PASSWORD="$2"
            PORT="${3:-5555}"
            ;;
        *)
            DB_PASSWORD=""
            PORT="$2"
            ;;
    esac
fi

if [ ! -f "$SCRIPT_DIR/dist/server.jar" ]; then
    echo "JAR not found. Building first..."
    sh "$SCRIPT_DIR/build.sh"
fi

echo "Starting Lab 5 Server..."
echo "Database: pg:5432/studs"
echo "Login: $DB_LOGIN"
echo "Port: $PORT"
echo ""

CLASSPATH="$SCRIPT_DIR/dist/server.jar:$SCRIPT_DIR/lib/postgresql-42.7.0.jar"
java -cp "$CLASSPATH" server.ServerMain "$DB_LOGIN" "$DB_PASSWORD" "$PORT"