#!/usr/bin/env bash
# UDP-туннель к серверу Lab 5 на Helios через SSH.
# ssh -L пробрасывает только TCP; клиент Lab 5 — UDP (DatagramChannel).
#
# Схема (как при тестировании):
#   1) Helios: Java-мост server.UdpTcpBridge (tcp-lsn) в фоне
#   2) Arch:   ssh -N -L 15555:127.0.0.1:15555
#   3) Arch:   Java-мост udp-lsn (127.0.0.1:5555 ↔ 127.0.0.1:15555)
#
# Использование:
#   ./scripts/udp-tunnel-helios.sh start <user>@helios [local_udp] [remote_udp] [tcp_bridge]
#   ./scripts/udp-tunnel-helios.sh stop
#   ./scripts/udp-tunnel-helios.sh status
set -euo pipefail

SSH_PORT="${SSH_PORT:-2222}"
STATE_DIR="${TMPDIR:-/tmp}/lab5-udp-tunnel"
PID_FILE="$STATE_DIR/pids"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOCAL_SERVER_JAR="$(ls "$ROOT"/deploy/helios/dist/server*.jar 2>/dev/null | head -1 || true)"
LOCAL_BRIDGE_LOG="$ROOT/logs/bridge-local.log"

detect_remote_deploy() {
  local remote="$1"
  if [[ -n "${LAB5_REMOTE_DEPLOY:-}" ]]; then
    echo "$LAB5_REMOTE_DEPLOY"
    return 0
  fi
  local candidate
  for candidate in '~/labuba8/LAB_5/deploy/helios' '~/LAB_5/deploy/helios'; do
    if ssh -p "$SSH_PORT" -o BatchMode=yes -o ConnectTimeout=8 "$remote" \
      "test -f ${candidate}/dist/server.jar && test -x ${candidate}/run-udp-bridge-bg.sh" 2>/dev/null; then
      echo "$candidate"
      return 0
    fi
  done
  echo '~/LAB_5/deploy/helios'
}

preflight_remote_server() {
  local remote="$1"
  local remote_udp="$2"
  local deploy="$3"

  echo "Проверка на Helios (UDP $remote_udp, $deploy)..."
  if ! ssh -p "$SSH_PORT" -o BatchMode=yes -o ConnectTimeout=8 "$remote" \
    "sockstat -4 -u -l 2>/dev/null | grep -q ':${remote_udp}[[:space:]]'"; then
    echo "ERROR: на Helios не слушается UDP $remote_udp."
    echo "  cd ~/labuba8/LAB_5 && ./lab5 server-bg <db_login> $remote_udp"
    return 1
  fi
  if ! ssh -p "$SSH_PORT" -o BatchMode=yes -o ConnectTimeout=8 "$remote" \
    "grep -q 'Server started on port ${remote_udp}' ${deploy}/logs/server.log 2>/dev/null"; then
    echo "WARN: в ${deploy}/logs/server.log нет 'Server started on port ${remote_udp}'."
    echo "  Запустите сервер: ./lab5 server-bg <db_login> $remote_udp"
    return 1
  fi
  echo "Helios: UDP-сервер на порту $remote_udp активен."
  return 0
}

usage() {
  sed -n '2,16p' "$0" | sed 's/^# \{0,1\}//'
  exit "${1:-0}"
}

remote_has_java_bridge() {
  local remote="$1"
  ssh -p "$SSH_PORT" -o BatchMode=yes -o ConnectTimeout=8 "$remote" \
    "test -x ${REMOTE_DEPLOY}/run-udp-bridge-bg.sh && ls ${REMOTE_DEPLOY}/dist/server*.jar" >/dev/null 2>&1
}

remote_kill_bridge() {
  local remote="${1:-}"
  local deploy="${REMOTE_DEPLOY:-${LAB5_REMOTE_DEPLOY:-~/labuba8/LAB_5/deploy/helios}}"
  [[ -z "$remote" ]] && return 0
  ssh -p "$SSH_PORT" -o BatchMode=yes -o ConnectTimeout=8 "$remote" \
    "if [ -f ${deploy}/logs/bridge.pid ]; then kill \$(cat ${deploy}/logs/bridge.pid) 2>/dev/null || true; fi; \
     pkill -f 'server.UdpTcpBridge' 2>/dev/null || true; \
     pkill -f 'run-udp-bridge' 2>/dev/null || true; \
     rm -f ${deploy}/logs/bridge.pid" \
    2>/dev/null || true
}

wait_remote_tcp_port() {
  local remote="$1"
  local port="$2"
  local max="${3:-30}"
  echo "Ожидание Java-моста на Helios (TCP $port)..."
  for ((i = 0; i < max; i++)); do
    if ssh -p "$SSH_PORT" -o BatchMode=yes -o ConnectTimeout=8 "$remote" \
      "sockstat -4 -l 2>/dev/null | grep -q ':${port}[[:space:]]'"; then
      echo "Helios: TCP $port слушается."
      return 0
    fi
    sleep 1
  done
  echo "Таймаут: на Helios не поднялся мост. См. ${REMOTE_DEPLOY}/logs/bridge.log"
  return 1
}

wait_local_tcp_port() {
  local port="$1"
  local max="${2:-15}"
  echo "Ожидание SSH -L на 127.0.0.1:$port ..."
  for ((i = 0; i < max; i++)); do
    if (echo >/dev/tcp/127.0.0.1/"$port") 2>/dev/null; then
      echo "Локальный порт $port доступен."
      return 0
    fi
    sleep 1
  done
  echo "Таймаут: SSH не открыл локальный порт $port."
  return 1
}

remote_start_java_bridge() {
  local remote="$1"
  local tcp_bridge="$2"
  local remote_udp="$3"
  local verbose="${LAB5_BRIDGE_VERBOSE:-0}"
  echo "Мост на Helios: Java (server.UdpTcpBridge tcp-lsn)"
  ssh -p "$SSH_PORT" -o BatchMode=yes -o ConnectTimeout=15 "$remote" \
    "cd ${REMOTE_DEPLOY} && chmod +x run-udp-bridge.sh run-udp-bridge-bg.sh 2>/dev/null; \
     LAB5_BRIDGE_VERBOSE=${verbose} ./run-udp-bridge-bg.sh ${tcp_bridge} ${remote_udp}"
}

stop_tunnel() {
  if [[ -f "$PID_FILE" ]]; then
    # shellcheck disable=SC1090
    source "$PID_FILE" 2>/dev/null || true
    [[ -n "${LOCAL_BRIDGE_PID:-}" ]] && kill "$LOCAL_BRIDGE_PID" 2>/dev/null || true
    [[ -n "${SSH_PID:-}" ]] && kill "$SSH_PID" 2>/dev/null || true
    remote_kill_bridge "${REMOTE:-}"
    rm -f "$PID_FILE"
  fi
  pkill -f 'server.UdpTcpBridge udp-lsn' 2>/dev/null || true
  echo "UDP-туннель остановлен."
}

status_tunnel() {
  if [[ ! -f "$PID_FILE" ]]; then
    echo "UDP-туннель не запущен."
    return 1
  fi
  # shellcheck disable=SC1090
  source "$PID_FILE"
  local ok=0
  if kill -0 "$LOCAL_BRIDGE_PID" 2>/dev/null; then
    echo "local bridge (PID $LOCAL_BRIDGE_PID): UDP 127.0.0.1:${LOCAL_UDP:-?}"
  else
    echo "local bridge: не работает"
    ok=1
  fi
  if kill -0 "$SSH_PID" 2>/dev/null; then
    echo "ssh -L (PID $SSH_PID): 127.0.0.1:${TCP_BRIDGE:-?} -> Helios"
  else
    echo "ssh -L: не работает"
    ok=1
  fi
  return "$ok"
}

start_local_java_bridge() {
  local local_udp="$1"
  local tcp_bridge="$2"
  local verbose="${LAB5_BRIDGE_VERBOSE:-0}"

  if [[ -z "$LOCAL_SERVER_JAR" ]]; then
    echo "Нет deploy/helios/dist/server*.jar — выполните: ./lab5 build"
    exit 1
  fi

  mkdir -p "$ROOT/logs"
  : >"$LOCAL_BRIDGE_LOG"
  nohup env -u _JAVA_OPTIONS LAB5_BRIDGE_VERBOSE="$verbose" java \
    -cp "$LOCAL_SERVER_JAR" \
    server.UdpTcpBridge udp-lsn 127.0.0.1 "$local_udp" 127.0.0.1 "$tcp_bridge" \
    >>"$LOCAL_BRIDGE_LOG" 2>&1 &
  LOCAL_BRIDGE_PID=$!
  echo "Локальный мост: pid $LOCAL_BRIDGE_PID, лог: $LOCAL_BRIDGE_LOG"
}

start_tunnel() {
  local remote="${1:?}"
  local local_udp="${2:-5555}"
  local remote_udp="${3:-5555}"
  local tcp_bridge="${4:-15555}"

  REMOTE_DEPLOY="$(detect_remote_deploy "$remote")"
  export LAB5_REMOTE_DEPLOY="$REMOTE_DEPLOY"
  echo "REMOTE_DEPLOY=$REMOTE_DEPLOY"

  if ! preflight_remote_server "$remote" "$remote_udp" "$REMOTE_DEPLOY"; then
    exit 1
  fi

  if ! remote_has_java_bridge "$remote"; then
    echo "На Helios не найден Java-мост:"
    echo "  ${REMOTE_DEPLOY}/run-udp-bridge-bg.sh"
    echo "  ${REMOTE_DEPLOY}/dist/server*.jar"
    echo "Выложите: ./scripts/deploy-helios.sh"
    exit 1
  fi

  stop_tunnel 2>/dev/null || true
  remote_kill_bridge "$remote"
  sleep 1
  mkdir -p "$STATE_DIR"

  echo "Запуск UDP-туннеля: GUI → UDP 127.0.0.1:$local_udp → TCP:$tcp_bridge → SSH -L → Helios → UDP 127.0.0.1:$remote_udp"
  echo "(на Helios: ./lab5 server-bg <db_login> $remote_udp)"
  echo ""

  if ! remote_start_java_bridge "$remote" "$tcp_bridge" "$remote_udp"; then
    exit 1
  fi
  if ! wait_remote_tcp_port "$remote" "$tcp_bridge" 30; then
    remote_kill_bridge "$remote"
    exit 1
  fi

  ssh -p "$SSH_PORT" -N \
    -o ExitOnForwardFailure=yes \
    -o ServerAliveInterval=30 \
    -L "${tcp_bridge}:127.0.0.1:${tcp_bridge}" \
    "$remote" &
  SSH_PID=$!

  if ! wait_local_tcp_port "$tcp_bridge" 20; then
    kill "$SSH_PID" 2>/dev/null || true
    remote_kill_bridge "$remote"
    exit 1
  fi

  start_local_java_bridge "$local_udp" "$tcp_bridge"
  sleep 2

  if ! kill -0 "$SSH_PID" 2>/dev/null || ! kill -0 "$LOCAL_BRIDGE_PID" 2>/dev/null; then
    echo "Не удалось поднять туннель. Проверьте SSH и сервер на Helios."
    stop_tunnel
    remote_kill_bridge "$remote"
    exit 1
  fi

  cat >"$PID_FILE" <<EOF
LOCAL_BRIDGE_PID=$LOCAL_BRIDGE_PID
SSH_PID=$SSH_PID
LOCAL_UDP=$local_udp
TCP_BRIDGE=$tcp_bridge
REMOTE=$remote
EOF

  echo "Туннель запущен (Java-мост на Helios и локально)."
  echo "GUI: ./lab5 mock 127.0.0.1 $local_udp"
  echo "Остановка: ./scripts/udp-tunnel-helios.sh stop"
}

CMD="${1:-help}"
shift || true

case "$CMD" in
  start) start_tunnel "$@" ;;
  stop) stop_tunnel ;;
  status) status_tunnel ;;
  help|-h|--help) usage 0 ;;
  *) usage 1 ;;
esac
