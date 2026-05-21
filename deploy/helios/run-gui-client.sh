#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

if [[ "${1:-}" != "mock" && -z "${DISPLAY:-}" ]]; then
  echo "DISPLAY не задан — JavaFX GUI не запустится."
  echo "На Helios: ssh -Y <login>@helios.se.ifmo.ru"
  exit 1
fi

if [[ "$(uname -s)" == "FreeBSD" ]]; then
  echo "JavaFX (OpenJFX) не поддерживает запуск GUI на FreeBSD на Helios."
  echo ""
  echo "Рабочий вариант для вашего сценария:"
  echo "  1) Сервер на Helios:  ./lab5 server-bg <db_login> 5555"
  echo "  2) На Arch: ./lab5 remote-gui <login>@helios.se.ifmo.ru <db_login>"
  echo "     (Java-мост на Helios, socat там не нужен)"
  echo ""
  echo "Или: ./lab5 remote-gui <login>@helios.cs.ifmo.ru <db_login>"
  exit 1
fi

MODE="${1:-real}"
HOST="${2:-localhost}"
PORT="${3:-5555}"

shopt -s nullglob
GUI_JARS=(dist/gui-client*.jar)
FX_JARS=(lib/javafx-*.jar)
shopt -u nullglob

if [[ ${#GUI_JARS[@]} -eq 0 ]]; then
  echo "dist/gui-client*.jar не найден. Сначала выполните: ./build.sh"
  exit 1
fi

if [[ ${#FX_JARS[@]} -eq 0 ]]; then
  echo "JavaFX jars в lib/ не найдены. Сначала выполните: ./build.sh"
  exit 1
fi

GUI_JAR="${GUI_JARS[0]}"
APP_ARGS=()

if [[ "$MODE" == "mock" ]]; then
  APP_ARGS+=(--mock)
  echo "Starting GUI client in MOCK mode"
elif [[ "$MODE" == "real" ]]; then
  APP_ARGS+=("$HOST" "$PORT")
  echo "Starting GUI client in REAL mode ($HOST:$PORT)"
elif [[ "$MODE" =~ ^[0-9]+$ ]]; then
  APP_ARGS+=("localhost" "$MODE")
  echo "Starting GUI client in REAL mode (localhost:$MODE)"
else
  APP_ARGS+=("$MODE" "5555")
  echo "Starting GUI client in REAL mode ($MODE:5555)"
  echo "Hint: use explicit mode './run-gui-client.sh real <host> <port>' for clarity."
fi

MODULE_PATH="$(IFS=:; echo "${FX_JARS[*]}")"
CLIENT_CP="$GUI_JAR"
shopt -s nullglob
for f in lib/log4j-api-*.jar lib/log4j-core-*.jar; do
  CLIENT_CP="$CLIENT_CP:$f"
done
shopt -u nullglob

exec env -u _JAVA_OPTIONS java \
  ${LAB5_GUI_CLIENT_JAVA_OPTS:--Xms64m -Xmx512m -XX:MaxMetaspaceSize=256m --enable-native-access=javafx.graphics} \
  -Dlab5.deploy.root="$SCRIPT_DIR" \
  --module-path "$MODULE_PATH" \
  --add-modules javafx.base,javafx.graphics,javafx.controls \
  -cp "$CLIENT_CP" \
  client.gui.GuiClientApp "${APP_ARGS[@]}"
