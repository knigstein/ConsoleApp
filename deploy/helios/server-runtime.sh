#!/bin/sh
# Общие функции: UDP-порт, остановка ServerMain (POSIX sh — Helios/FreeBSD).
# Подключается через: . "$SCRIPT_DIR/server-runtime.sh"
# Перед source задайте: LAB5_DEPLOY_DIR="$SCRIPT_DIR"

lab5_deploy_dir() {
  if [ -n "${LAB5_DEPLOY_DIR:-}" ]; then
    printf '%s\n' "$LAB5_DEPLOY_DIR"
    return 0
  fi
  cd "$(dirname "$0")" && pwd
}

lab5_udp_port_in_use() {
  port="$1"
  if command -v sockstat >/dev/null 2>&1; then
    sockstat -4 -u -l 2>/dev/null | grep -q ":${port} "
    return $?
  fi
  if command -v ss >/dev/null 2>&1; then
    ss -u -l -n 2>/dev/null | grep -q ":${port} "
    return $?
  fi
  if command -v netstat >/dev/null 2>&1; then
    netstat -an -f inet 2>/dev/null | grep -q "\.${port} " \
      || netstat -uln 2>/dev/null | grep -q ":${port} "
    return $?
  fi
  return 1
}

lab5_show_udp_listeners() {
  port="$1"
  echo "Процессы на UDP $port:"
  if command -v sockstat >/dev/null 2>&1; then
    sockstat -4 -u -l 2>/dev/null | grep ":${port} " || echo "  (sockstat: не найдено)"
    return 0
  fi
  if command -v ss >/dev/null 2>&1; then
    ss -u -l -np 2>/dev/null | grep ":${port} " || true
    return 0
  fi
  netstat -an -f inet 2>/dev/null | grep "\.${port} " || true
}

lab5_server_main_pids() {
  port="${1:-}"
  user_name="$(id -un 2>/dev/null || echo "$USER")"
  if command -v pgrep >/dev/null 2>&1; then
    if [ -n "$port" ]; then
      pgrep -u "$user_name" -f "server.ServerMain" 2>/dev/null | while read -r pid; do
        if ps -p "$pid" -o command= 2>/dev/null | grep -q " ${port}\$"; then
          printf '%s\n' "$pid"
        fi
      done
    else
      pgrep -u "$user_name" -f "server.ServerMain" 2>/dev/null || true
    fi
    return 0
  fi
  ps -u "$user_name" -o pid= -o command= 2>/dev/null \
    | grep 'server\.ServerMain' \
    | awk -v p="$port" '{
        if (p == "") { print $1; next }
        for (i = 2; i <= NF; i++) s = s " " $i
        if (s ~ (" " p "$")) print $1
      }' || true
}

lab5_stop_server() {
  port="${1:-5555}"
  script_dir="${2:-$(lab5_deploy_dir)}"
  pid_file="$script_dir/logs/server.pid"
  killed=0

  if [ -f "$pid_file" ]; then
    pid="$(cat "$pid_file" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      sleep 1
      kill -9 "$pid" 2>/dev/null || true
      echo "Остановлен pid $pid (из logs/server.pid)"
      killed=1
    fi
    rm -f "$pid_file"
  fi

  for p in $(lab5_server_main_pids "$port"); do
    [ -z "$p" ] && continue
    kill "$p" 2>/dev/null || true
    sleep 1
    kill -9 "$p" 2>/dev/null || true
    echo "Остановлен ServerMain pid $p"
    killed=1
  done

  if lab5_udp_port_in_use "$port"; then
    echo "WARN: UDP $port всё ещё занят после stop:"
    lab5_show_udp_listeners "$port"
    return 1
  fi

  if [ "$killed" -eq 1 ]; then
    echo "Порт UDP $port свободен."
  else
    echo "Фоновый ServerMain не найден (порт $port)."
  fi
  return 0
}

lab5_ensure_udp_port_free() {
  port="$1"
  script_dir="${2:-$(lab5_deploy_dir)}"

  if lab5_udp_port_in_use "$port"; then
    echo "UDP $port занят — останавливаем старый Lab 5 сервер..."
    lab5_stop_server "$port" "$script_dir" || true
    sleep 1
  fi

  if lab5_udp_port_in_use "$port"; then
    echo "ERROR: UDP $port всё ещё занят (другой процесс или чужой экземпляр)."
    lab5_show_udp_listeners "$port"
    echo ""
    echo "Вручную на Helios:"
    echo "  sockstat -4 -u -l | grep $port"
    echo "  kill <PID>"
    echo "  ./lab5 stop"
    return 1
  fi
  return 0
}

lab5_wait_server_ready() {
  port="$1"
  script_dir="${2:-$(lab5_deploy_dir)}"
  pid_file="$script_dir/logs/server.pid"
  log_file="$script_dir/logs/server.log"
  max="${LAB5_SERVER_WAIT_SEC:-20}"
  i=1

  while [ "$i" -le "$max" ]; do
    if [ -f "$log_file" ] && grep -q "Server started on port $port" "$log_file" 2>/dev/null; then
      echo "Сервер готов (UDP $port)."
      return 0
    fi
    if [ -f "$pid_file" ]; then
      pid="$(cat "$pid_file" 2>/dev/null || true)"
      if [ -n "$pid" ] && ! kill -0 "$pid" 2>/dev/null; then
        echo "ERROR: сервер завершился с ошибкой. Последние строки logs/server.log:"
        tail -n 20 "$log_file" 2>/dev/null || true
        rm -f "$pid_file"
        return 1
      fi
    fi
    sleep 1
    i=$((i + 1))
  done

  echo "ERROR: таймаут ожидания 'Server started on port $port'."
  tail -n 15 "$log_file" 2>/dev/null || true
  return 1
}

lab5_server_status() {
  script_dir="${1:-$(lab5_deploy_dir)}"
  pid_file="$script_dir/logs/server.pid"
  port="${2:-5555}"

  if [ -f "$pid_file" ]; then
    pid="$(cat "$pid_file" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      echo "Сервер работает, pid $pid (UDP $port)."
      return 0
    fi
  fi

  p="$(lab5_server_main_pids "$port" | head -1)"
  if [ -n "$p" ] && kill -0 "$p" 2>/dev/null; then
    echo "Сервер работает, pid $p (UDP $port, без актуального logs/server.pid)."
    return 0
  fi

  if lab5_udp_port_in_use "$port"; then
    echo "UDP $port занят, но ServerMain Lab 5 не найден:"
    lab5_show_udp_listeners "$port"
    return 1
  fi

  echo "Фоновый сервер не запущен."
  return 1
}
