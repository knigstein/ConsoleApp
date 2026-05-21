#!/bin/sh
# Запуск Java-моста TCP→UDP на Helios в фоне (для udp-tunnel-helios.sh).
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

TCP_PORT="${1:-15555}"
UDP_PORT="${2:-5555}"

mkdir -p logs
if [ -f logs/bridge.pid ] && kill -0 "$(cat logs/bridge.pid)" 2>/dev/null; then
  echo "Мост уже запущен (pid $(cat logs/bridge.pid))"
  exit 0
fi

nohup ./run-udp-bridge.sh "$TCP_PORT" "$UDP_PORT" >> logs/bridge.log 2>&1 &
echo $! > logs/bridge.pid
echo "Мост запущен, pid $(cat logs/bridge.pid), лог: logs/bridge.log"
