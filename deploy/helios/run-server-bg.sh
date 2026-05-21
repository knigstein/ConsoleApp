#!/bin/sh
# Запуск UDP-сервера в фоне (канон для Helios: потом GUI к localhost).
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"
export LAB5_DEPLOY_DIR="$SCRIPT_DIR"

# shellcheck source=/dev/null
. "$SCRIPT_DIR/server-runtime.sh"

DB_LOGIN="${1:-$USER}"
PORT="${2:-5555}"

SERVER_JAR="$(ls dist/server*.jar 2>/dev/null | head -1 || true)"
if [ -z "$SERVER_JAR" ]; then
  echo "dist/server*.jar не найден. На Arch: ./scripts/deploy-helios.sh  или  ./lab5 build"
  exit 1
fi
SERVER_CP="$SERVER_JAR"
for f in lib/*.jar; do
  [ -f "$f" ] || continue
  SERVER_CP="$SERVER_CP:$f"
done

mkdir -p logs

lab5_ensure_udp_port_free "$PORT" "$SCRIPT_DIR"

if [ -f logs/server.pid ]; then
  old_pid="$(cat logs/server.pid 2>/dev/null || true)"
  if [ -n "$old_pid" ] && kill -0 "$old_pid" 2>/dev/null; then
    echo "Сервер уже запущен (pid $old_pid). Остановка: ./lab5 stop"
    exit 1
  fi
  rm -f logs/server.pid
fi

echo "Запуск сервера в фоне: $SERVER_JAR"
echo "DB login: $DB_LOGIN  UDP порт: $PORT"
echo "Лог: $SCRIPT_DIR/logs/server.log"

: > logs/server.log

nohup env -u _JAVA_OPTIONS java \
  ${LAB5_SERVER_JAVA_OPTS:--Xms32m -Xmx512m -XX:MaxMetaspaceSize=192m} \
  -cp "$SERVER_CP" \
  server.ServerMain "$DB_LOGIN" "$PORT" >> logs/server.log 2>&1 &

echo $! > logs/server.pid
echo "PID записан в logs/server.pid ($(cat logs/server.pid))"

if ! lab5_wait_server_ready "$PORT" "$SCRIPT_DIR"; then
  rm -f logs/server.pid
  exit 1
fi
