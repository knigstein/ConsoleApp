#!/bin/sh
# Единая точка входа на Helios: сервер + GUI на одной машине (UDP localhost).
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"
export LAB5_DEPLOY_DIR="$SCRIPT_DIR"

# shellcheck source=/dev/null
. "$SCRIPT_DIR/server-runtime.sh"

usage() {
  cat <<'EOF'
Использование (все команды — из каталога deploy/helios после ./build.sh):

  ./helios-stack.sh server-fg <db_login> [port]   # сервер в переднем плане (как run-server.sh)
  ./helios-stack.sh server-bg <db_login> [port]   # сервер в фоне + logs/server.log
  ./helios-stack.sh gui [port]                    # GUI → localhost (реальный сервер на этой же машине)
  ./helios-stack.sh all <db_login> [port]         # server-bg, затем gui (типичный сценарий сдачи)
  ./helios-stack.sh stop [port]                     # остановить сервер + освободить UDP-порт
  ./helios-stack.sh status [port]                   # статус ServerMain / порт

Переменные БД (опционально, см. DatabaseManager):
  LAB5_DB_HOST  LAB5_DB_PORT  LAB5_DB_NAME

GUI и карта этажей ожидают каталог Corpus рядом со скриптами (кладёт distHelios).
Для отображения окна с Helios: ssh -Y <login>@helios... затем ./helios-stack.sh all ...
EOF
}

require_gui_display() {
  if [ -n "${DISPLAY:-}" ]; then
    return 0
  fi
  echo "Ошибка: DISPLAY не задан — JavaFX GUI на этой SSH-сессии не откроется."
  echo ""
  echo "Если при входе было «X11 forwarding request failed» — сервер Helios не принял X11."
  echo "Вручную export DISPLAY=... не поможет: канал форвардинга не создан."
  echo ""
  echo "Рабочий сценарий без GUI на Helios:"
  echo "  ./lab5 server-bg <db_login> 5555    # только сервер"
  echo "  ./lab5 client localhost 5555        # консольный клиент на Helios"
  echo ""
  echo "GUI с картой этажей — на Arch (UDP-туннель, рекомендуется):"
  echo "  ./lab5 remote-gui <login>@helios.se.ifmo.ru <db_login> 5555 5555"
  echo "  (сначала на Helios: ./lab5 server-bg <db_login> 5555)"
  echo ""
  echo "Попытка починить X11 на Arch перед ssh:"
  echo "  sudo pacman -S xorg-xauth"
  echo "  ssh -p 2222 -Y s456129@helios.cs.ifmo.ru   # не ssh -Y 2222 ..."
  exit 1
}

require_deploy_artifacts() {
  mode="${1:-all}"
  if [ ! -f check-env.sh ]; then
    return 0
  fi
  ./check-env.sh "$mode" || exit 1
}

wait_for_server_log() {
  lab5_wait_server_ready "$1" "$SCRIPT_DIR"
}

cmd="${1:-help}"
if [ "$cmd" = "help" ] || [ "$cmd" = "-h" ] || [ "$cmd" = "--help" ]; then
  usage
  exit 0
fi

case "$cmd" in
  server-fg)
    shift
    exec ./run-server.sh "$@"
    ;;
  server-bg)
    shift
    require_deploy_artifacts server
    exec ./run-server-bg.sh "$@"
    ;;
  gui)
    PORT="${2:-5555}"
    require_gui_display
    require_deploy_artifacts
    exec ./run-gui-client.sh real localhost "$PORT"
    ;;
  all)
    DB_LOGIN="${2:-$USER}"
    PORT="${3:-5555}"
    require_gui_display
    require_deploy_artifacts
    ./run-server-bg.sh "$DB_LOGIN" "$PORT"
    wait_for_server_log "$PORT"
    exec ./run-gui-client.sh real localhost "$PORT"
    ;;
  stop)
    PORT="${2:-5555}"
    lab5_stop_server "$PORT" "$SCRIPT_DIR" || exit 1
    ;;
  status)
    PORT="${2:-5555}"
    lab5_server_status "$SCRIPT_DIR" "$PORT" || exit 1
    ;;
  *)
    usage
    exit 1
    ;;
esac
