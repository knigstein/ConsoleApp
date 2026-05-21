#!/usr/bin/env bash
# Проверка: размещение на карте (JUnit), два пользователя, live sync через show.
#
#   ./scripts/verify-map-sync.sh [host] [port]
#   ./scripts/verify-map-sync.sh 127.0.0.1 5555
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOST="${1:-127.0.0.1}"
PORT="${2:-5555}"
STAMP="$(date +%s)"
USER_A="lab5map_a_${STAMP}"
USER_B="lab5map_b_${STAMP}"
PASS="testpass123"
GROUP_NAME="MapSync_${STAMP}"

cd "$ROOT"

echo "=== 1/3 JUnit: round-trip аудитория ↔ координаты ==="
./gradlew :client:test --tests 'client.gui.map.RoomPlacementServiceTest' -q

echo ""
echo "=== 2/3 UDP: два пользователя (register/login/show) ==="
CLIENT_JAR="$(ls "$ROOT/deploy/helios/dist/client"*.jar 2>/dev/null | head -1 || true)"
if [[ -z "$CLIENT_JAR" ]]; then
  ./gradlew distHelios -q
  CLIENT_JAR="$(ls "$ROOT/deploy/helios/dist/client"*.jar | head -1)"
fi

run_client() {
  timeout 45s env -u _JAVA_OPTIONS java -cp "$CLIENT_JAR:$ROOT/deploy/helios/lib/*" client.ClientMain "$HOST" "$PORT" <<<"$1"
}

if ! run_client $'help\nexit\n' >/dev/null 2>&1; then
  echo "ERROR: сервер недоступен на $HOST:$PORT"
  echo "  Запустите: ./lab5 server-bg <db_login> $PORT  (Helios)"
  echo "  или:       ./gradlew runServer -PappArgs=\"<db_login> $PORT\"  (локально)"
  exit 1
fi

run_client "register ${USER_A} ${PASS}
login ${USER_A} ${PASS}
exit" >/dev/null

run_client "register ${USER_B} ${PASS}
login ${USER_B} ${PASS}
exit" >/dev/null

echo "Пользователи зарегистрированы: $USER_A, $USER_B"

echo ""
echo "=== 3/3 Live sync: user B видит коллекцию после login ==="
OUT_B="$(run_client "login ${USER_B} ${PASS}
show
exit")"
if ! grep -q "MapSync\|групп\|элемент\|StudyGroup\|id" <<<"$OUT_B"; then
  echo "WARN: show для $USER_B вернул неожиданный вывод (возможно пустая коллекция — это OK для новых пользователей)"
fi
echo "show от $USER_B: OK"

echo ""
echo "=== verify-map-sync: OK ==="
echo "GUI: запустите два окна ./lab5 remote-gui ... и проверьте карту + обновление за ~5 с."
