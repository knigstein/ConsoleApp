#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

HOST="${1:-localhost}"
PORT="${2:-5555}"

shopt -s nullglob
CLIENT_JARS=(dist/client*.jar)
shopt -u nullglob

if [[ ${#CLIENT_JARS[@]} -eq 0 ]]; then
  echo "dist/client*.jar не найден. Сначала выполните: ./build.sh"
  exit 1
fi
CLIENT_JAR="${CLIENT_JARS[0]}"

shopt -s nullglob
CLIENT_CP="$CLIENT_JAR"
for f in lib/*.jar; do
  CLIENT_CP="$CLIENT_CP:$f"
done
shopt -u nullglob

echo "Starting Lab 5 Client (java, без Gradle)..."
echo "Jar: $CLIENT_JAR"
echo "Host: $HOST"
echo "Port: $PORT"
echo ""

# Gradle для runClient поднимает вторую тяжёлую JVM и на стенде часто упирается в лимит памяти.
# Сбрасываем _JAVA_OPTIONS (часто -Xmx1G), задаём скромный heap для одного процесса.
exec env -u _JAVA_OPTIONS java \
  ${LAB5_CLIENT_JAVA_OPTS:--Xms16m -Xmx256m -XX:MaxMetaspaceSize=128m} \
  -cp "$CLIENT_CP" \
  client.ClientMain "$HOST" "$PORT"
