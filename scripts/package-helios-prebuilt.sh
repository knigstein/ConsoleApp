#!/usr/bin/env bash
# Сборка на машине с сетью + архив с уже собранными dist/lib/Corpus в deploy/helios.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "=== Сборка (bytecode Java 17) + distHelios ==="
"$ROOT/deploy/helios/build.sh"

"$ROOT/scripts/package-helios-submission.sh"

echo ""
echo "Архив содержит deploy/helios/dist, lib, Corpus — на Helios:"
echo "  tar -xzf ... && cd ~/LAB_5 && ./lab5 check server && ./lab5 server-bg <db_login> 5555"
echo "На Arch: ./lab5 remote-gui <login>@helios.se.ifmo.ru <db_login> 5555 5555"
