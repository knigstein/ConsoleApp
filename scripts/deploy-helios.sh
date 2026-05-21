#!/usr/bin/env bash
# Ручная выкладка исправленных файлов Lab 5 на Helios
#
#   export LAB5_HELIOS_HOST=helios
#   export LAB5_REMOTE_DIR='~/labuba8/LAB_5'
#   ./scripts/deploy-helios.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOST="${LAB5_HELIOS_HOST:-helios}"
SSH_PORT="${SSH_PORT:-2222}"
REMOTE_DIR="${LAB5_REMOTE_DIR:-~/labuba8/LAB_5}"
REMOTE_DEPLOY="${REMOTE_DIR}/deploy/helios"

# shellcheck source=/dev/null
source "$ROOT/scripts/java-build-home.sh"

echo "=== Сборка ==="
"$ROOT/deploy/helios/build.sh"

JAR="$(ls "$ROOT/deploy/helios/dist/server"*.jar 2>/dev/null | head -1)"
[[ -n "$JAR" ]] || { echo "нет server.jar"; exit 1; }

echo ""
echo "=== Копирование на $HOST ==="
echo "  $REMOTE_DIR"
echo "  $REMOTE_DEPLOY"
echo ""

# JAR и runtime libs
scp -P "$SSH_PORT" "$ROOT/deploy/helios/dist/"*.jar "$HOST:${REMOTE_DEPLOY}/dist/"
scp -P "$SSH_PORT" "$ROOT/deploy/helios/lib/"*.jar "$HOST:${REMOTE_DEPLOY}/lib/" 2>/dev/null || true

# Скрипты deploy/helios
scp -P "$SSH_PORT" \
  "$ROOT/deploy/helios/build.sh" \
  "$ROOT/deploy/helios/check-env.sh" \
  "$ROOT/deploy/helios/helios-stack.sh" \
  "$ROOT/deploy/helios/server-runtime.sh" \
  "$ROOT/deploy/helios/run-server.sh" \
  "$ROOT/deploy/helios/run-server-bg.sh" \
  "$ROOT/deploy/helios/run-client.sh" \
  "$ROOT/deploy/helios/run-udp-bridge.sh" \
  "$ROOT/deploy/helios/run-udp-bridge-bg.sh" \
  "$ROOT/deploy/helios/init-db.sh" \
  "$HOST:${REMOTE_DEPLOY}/"

# lab5 в корне проекта на Helios
scp -P "$SSH_PORT" "$ROOT/lab5" "$HOST:${REMOTE_DIR}/"

ssh -p "$SSH_PORT" "$HOST" "chmod +x ${REMOTE_DIR}/lab5 ${REMOTE_DEPLOY}/*.sh 2>/dev/null" || true

echo ""
echo "=== Готово ==="
echo "Helios:"
echo "  ssh -p $SSH_PORT $HOST"
echo "  cd $REMOTE_DIR"
echo "  ./lab5 stop 5555"
echo "  pkill -f UdpTcpBridge 2>/dev/null || true"
echo "  ./lab5 server-bg s456129 5555"
echo "  ./lab5 status"
echo ""
echo "Arch:"
echo "  export LAB5_REMOTE_DEPLOY='${REMOTE_DEPLOY}'"
echo "  ./lab5 remote-gui $HOST s456129 5555 5555"
