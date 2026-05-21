#!/bin/sh
# Java-мост TCP→UDP на Helios (без socat). Обычно запускается через SSH из udp-tunnel-helios.sh.
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

TCP_PORT="${1:-15555}"
UDP_PORT="${2:-5555}"
UDP_HOST="${3:-127.0.0.1}"

SERVER_JAR="$(ls dist/server*.jar 2>/dev/null | head -1 || true)"
if [ -z "$SERVER_JAR" ]; then
  echo "dist/server*.jar не найден. Выполните ./build.sh"
  exit 1
fi

echo "Запуск Java UDP/TCP bridge: TCP 127.0.0.1:$TCP_PORT -> UDP $UDP_HOST:$UDP_PORT"
exec env -u _JAVA_OPTIONS java \
  ${LAB5_BRIDGE_JAVA_OPTS:--Xms16m -Xmx64m} \
  -cp "$SERVER_JAR" \
  server.UdpTcpBridge tcp-lsn "$TCP_PORT" "$UDP_HOST" "$UDP_PORT"
