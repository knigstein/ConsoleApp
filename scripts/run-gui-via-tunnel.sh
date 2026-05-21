#!/usr/bin/env bash
# GUI на Linux с UDP-туннелем к серверу на Helios (ssh -L для UDP не работает).
#
# Использование:
#   ./scripts/run-gui-via-tunnel.sh <user>@helios.cs.ifmo.ru <db_login> [local_udp] [remote_udp]
set -euo pipefail

REMOTE_USER_HOST="${1:?usage: $0 <user>@helios.cs.ifmo.ru <db_login> [local_udp] [remote_udp]}"
DB_LOGIN="${2:?}"
LOCAL_UDP="${3:-5555}"
REMOTE_UDP="${4:-5555}"
SSH_PORT="${SSH_PORT:-2222}"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TUNNEL="$ROOT/scripts/udp-tunnel-helios.sh"
LAB5="$ROOT/lab5"

export SSH_PORT
if [[ -z "${LAB5_REMOTE_DEPLOY:-}" ]]; then
  export LAB5_REMOTE_DEPLOY='~/labuba8/LAB_5/deploy/helios'
fi

cleanup() {
  "$TUNNEL" stop 2>/dev/null || true
}
trap cleanup EXIT INT TERM

chmod +x "$TUNNEL" "$LAB5" 2>/dev/null || true

echo "=== Lab 5: GUI + UDP-туннель (Java-мост) ==="
echo ""
echo "На Helios (отдельный терминал), один раз:"
echo "  cd ~/labuba8/LAB_5 && ./lab5 server-bg $DB_LOGIN $REMOTE_UDP"
echo "  (или: export LAB5_REMOTE_DEPLOY='~/labuba8/LAB_5/deploy/helios')"
echo "  (SSH: ssh -p $SSH_PORT $REMOTE_USER_HOST)"
echo ""
echo "Проверка сервера на Helios:"
echo "  ./lab5 status   # или: tail logs/server.log"
echo ""

"$TUNNEL" start "$REMOTE_USER_HOST" "$LOCAL_UDP" "$REMOTE_UDP"
echo ""
exec "$LAB5" mock 127.0.0.1 "$LOCAL_UDP"
