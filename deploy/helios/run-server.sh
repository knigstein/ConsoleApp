#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

DB_LOGIN="${1:-$USER}"
PORT="${2:-5555}"

shopt -s nullglob
SERVER_JARS=(dist/server*.jar)
shopt -u nullglob

if [[ ${#SERVER_JARS[@]} -eq 0 ]]; then
  echo "dist/server*.jar не найден. Сначала выполните: ./build.sh"
  exit 1
fi
SERVER_JAR="${SERVER_JARS[0]}"

shopt -s nullglob
SERVER_CP="$SERVER_JAR"
for f in lib/*.jar; do
  SERVER_CP="$SERVER_CP:$f"
done
shopt -u nullglob

echo "Starting Lab 5 Server (java, без Gradle)..."
echo "Jar: $SERVER_JAR"
echo "DB login: $DB_LOGIN"
echo "Port: $PORT"
echo ""

exec env -u _JAVA_OPTIONS java \
  ${LAB5_SERVER_JAVA_OPTS:--Xms32m -Xmx512m -XX:MaxMetaspaceSize=192m} \
  -cp "$SERVER_CP" \
  server.ServerMain "$DB_LOGIN" "$PORT"
