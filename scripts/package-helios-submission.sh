#!/usr/bin/env bash
# Собирает tar.gz для отправки на Helios (корень архива: LAB_5/).
# Запускать из корня репозитория: ./scripts/package-helios-submission.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STAMP="$(date +%Y%m%d-%H%M)"
OUT_PARENT="$(cd "$ROOT/.." && pwd)"
OUT="$OUT_PARENT/LAB_5-helios-${STAMP}.tar.gz"

cd "$OUT_PARENT"
echo "Создание: $OUT"
rm -f "$OUT"

echo "Проверка артефактов в deploy/helios..."
missing=0
for f in dist/server*.jar dist/gui-client*.jar dist/client*.jar lib/javafx-*.jar Corpus/834200.jpg Corpus/834204.jpg; do
  if ! compgen -G "$ROOT/deploy/helios/$f" >/dev/null; then
    echo "  WARN: нет $f"
    missing=1
  fi
done
if [[ $missing -eq 1 ]]; then
  echo "Сборка перед упаковкой..."
  "$ROOT/deploy/helios/build.sh"
fi

tar -czf "$OUT" \
  --exclude='LAB_5/.git' \
  --exclude='LAB_5/.gradle' \
  --exclude='LAB_5/.gradle-local' \
  --exclude='LAB_5/.gradle-local-agent' \
  --exclude='LAB_5/build' \
  --exclude='LAB_5/common/build' \
  --exclude='LAB_5/client/build' \
  --exclude='LAB_5/server/build' \
  --exclude='LAB_5/.cursor' \
  --exclude='LAB_5/target' \
  --exclude='LAB_5/.tmp' \
  --exclude='LAB_5/deploy/helios/build' \
  --exclude='LAB_5/deploy/helios/LAB_5-helios*.tar.gz' \
  --exclude='LAB_5/LAB_5-helios*.tar.gz' \
  LAB_5

cp -f "$OUT" "$ROOT/deploy/helios/LAB_5-helios-submission.tar.gz"
ls -lh "$OUT" "$ROOT/deploy/helios/LAB_5-helios-submission.tar.gz"
echo ""
echo "Готово. Отправка:"
echo "  scp -P 2222 \"$OUT\" <login>@helios.se.ifmo.ru:~/"
echo ""
echo "На Helios (Java 17, только сервер):"
echo "  tar -xzf LAB_5-helios-*.tar.gz && cd ~/LAB_5/deploy/helios"
echo "  chmod +x build.sh run-server*.sh run-udp-bridge*.sh helios-stack.sh init-db.sh server-runtime.sh"
echo "  ./build.sh verify && cd .. && chmod +x lab5 && ./lab5 check server"
echo "  ./lab5 server-bg <db_login> 5555"
echo ""
echo "На Arch (GUI + UDP-туннель):"
echo "  export LAB5_REMOTE_DEPLOY='~/LAB_5/deploy/helios'"
echo "  ./lab5 remote-gui helios <db_login> 5555 5555"
