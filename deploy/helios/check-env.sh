#!/bin/sh
# Проверка окружения (deploy/helios).
#   ./check-env.sh         — полная (сервер + артефакты GUI/Corpus для архива)
#   ./check-env.sh server  — только сервер на Helios (схема UDP-туннель)
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"
MODE="${1:-all}"
ERR=0

warn() { echo "WARN: $*"; }
fail() { echo "ERROR: $*"; ERR=1; }

echo "=== Lab 5 environment check ($MODE) ==="
echo "Directory: $SCRIPT_DIR"

if ! command -v java >/dev/null 2>&1; then
  fail "java не найден (нужен Java 17+)"
else
  echo "Java: $(env -u _JAVA_OPTIONS java -Xms16m -Xmx64m -version 2>&1 | head -1)"
fi

SERVER_JAR="$(ls dist/server*.jar 2>/dev/null | head -1 || true)"
GUI_JAR="$(ls dist/gui-client*.jar 2>/dev/null | head -1 || true)"
FX_JAR="$(ls lib/javafx-*.jar 2>/dev/null | head -1 || true)"
CORPUS_COUNT="$(ls Corpus/83420*.jpg 2>/dev/null | wc -l | tr -d ' ')"

[ -n "$SERVER_JAR" ] || fail "нет dist/server*.jar — на Arch: ./scripts/deploy-helios.sh или ./lab5 build"

if [ "$MODE" = "server" ]; then
  if [ -x "$SCRIPT_DIR/run-udp-bridge.sh" ]; then
    echo "UDP-мост: ./run-udp-bridge.sh (Java 17, класс в server.jar)"
  else
    warn "нет run-udp-bridge.sh — обновите архив LAB_5"
  fi
else
  [ -n "$GUI_JAR" ] || fail "нет dist/gui-client*.jar — выполните ./build.sh"
  [ -n "$FX_JAR" ] || fail "нет lib/javafx-*.jar — выполните ./build.sh"
  [ "${CORPUS_COUNT:-0}" -ge 5 ] || fail "нет Corpus/834200.jpg … 834204.jpg — выполните ./build.sh (distHelios копирует Corpus)"

  if [ -z "${DISPLAY:-}" ]; then
    warn "DISPLAY пуст — GUI на Helios (FreeBSD) недоступен; используйте ./lab5 remote-gui с Arch"
  else
    echo "DISPLAY=$DISPLAY"
  fi
fi

if [ -f logs/server.pid ] && kill -0 "$(cat logs/server.pid)" 2>/dev/null; then
  echo "Фоновый сервер уже запущен (pid $(cat logs/server.pid))"
fi

if [ "$ERR" -ne 0 ]; then
  echo "=== Проверка не пройдена ==="
  exit 1
fi
if [ "$MODE" = "server" ]; then
  echo "=== OK: ./helios-stack.sh server-bg <db_login> [port] (GUI — ./lab5 remote-gui с Arch) ==="
else
  echo "=== OK: сервер ./helios-stack.sh server-bg … | GUI на Arch: ./lab5 remote-gui … ==="
fi
