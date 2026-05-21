#!/usr/bin/env bash
# Сборка артефактов deploy/helios (dist/, lib/, Corpus/).
#
# Рекомендуется на Arch/Linux с полным деревом LAB_5 + Gradle.
# На Helios (FreeBSD, Java 17): обычно не собирать — только проверить готовые JAR
# или получить их с Arch: ./scripts/deploy-helios.sh
#
#   ./build.sh          — полная сборка (Gradle) или проверка на Helios
#   ./build.sh verify   — только проверить dist/lib/Corpus
#   LAB5_SKIP_BUILD=1   — не вызывать Gradle, если JAR уже есть
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$BASE_DIR/../.." && pwd)"
cd "$BASE_DIR"

is_helios_host() {
  [[ "$(uname -s 2>/dev/null || true)" == "FreeBSD" ]]
}

has_gradle_project() {
  [[ -f "$ROOT_DIR/settings.gradle" ]] \
    && { [[ -x "$ROOT_DIR/gradlew" ]] || command -v gradle >/dev/null 2>&1; }
}

source_java_build_home() {
  local helper="$ROOT_DIR/scripts/java-build-home.sh"
  if [[ -f "$helper" ]]; then
    # shellcheck source=/dev/null
    source "$helper"
    return 0
  fi
  if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: java не найден (нужен JDK 17+ для сборки)"
    exit 1
  fi
  local ver
  ver="$(java -version 2>&1 | sed -n '1p')"
  echo "Сборка: java из PATH ($ver)"
  echo "WARN: scripts/java-build-home.sh не найден — задайте JAVA_HOME=JDK17 вручную"
}

verify_artifacts() {
  local mode="${1:-all}"
  local err=0

  shopt -s nullglob
  local server_jars=(dist/server*.jar)
  local client_jars=(dist/client*.jar)
  local gui_jars=(dist/gui-client*.jar)
  local fx_jars=(lib/javafx-*.jar)
  local corpus_imgs=(Corpus/83420*.jpg)
  shopt -u nullglob

  echo "=== Lab 5 artifact check ($mode) ==="
  echo "Directory: $BASE_DIR"

  if [[ ${#server_jars[@]} -eq 0 ]]; then
    echo "ERROR: нет dist/server*.jar"
    err=1
  else
    echo "OK: ${server_jars[0]}"
  fi

  if [[ "$mode" != "server" ]]; then
    [[ ${#client_jars[@]} -gt 0 ]] || { echo "ERROR: нет dist/client*.jar"; err=1; }
    [[ ${#gui_jars[@]} -gt 0 ]] || { echo "ERROR: нет dist/gui-client*.jar"; err=1; }
    [[ ${#fx_jars[@]} -gt 0 ]] || { echo "ERROR: нет lib/javafx-*.jar"; err=1; }
    if [[ ${#corpus_imgs[@]} -lt 5 ]]; then
      echo "WARN: Corpus/ неполный (нужны 834200.jpg … 834204.jpg)"
    fi
  fi

  if [[ $err -ne 0 ]]; then
    echo ""
    echo "На Helios: соберите на Arch и скопируйте JAR:"
    echo "  ./scripts/deploy-helios.sh"
    echo "Или загрузите полный архив с уже собранным deploy/helios/dist/"
    exit 1
  fi

  echo "=== Артефакты в порядке ==="
}

run_gradle_build() {
  if ! has_gradle_project; then
    echo "ERROR: не найден Gradle-проект в $ROOT_DIR"
    echo "  Нужны settings.gradle и gradlew (или системный gradle)."
    echo "  Загрузите на Helios весь каталог LAB_5, не только deploy/helios."
    exit 1
  fi

  source_java_build_home

  local gradle_cmd=""
  if [[ -x "$ROOT_DIR/gradlew" ]]; then
    gradle_cmd="$ROOT_DIR/gradlew"
  elif command -v gradle >/dev/null 2>&1; then
    gradle_cmd="gradle"
  else
    echo "ERROR: gradlew и gradle не найдены"
    exit 1
  fi

  echo "=== Lab 5 Gradle Build ==="
  echo "Project root: $ROOT_DIR"
  echo "Gradle: $gradle_cmd"

  rm -rf "$BASE_DIR/dist" "$BASE_DIR/lib"

  # distHelios не требует JavaFX на Helios, но :client:build тянет OpenJFX — на FreeBSD часто падает
  if is_helios_host && [[ "${LAB5_FORCE_HELIOS_BUILD:-}" != "1" ]]; then
    echo "ERROR: полная сборка на Helios (FreeBSD) не поддерживается (OpenJFX / сеть)."
    echo "  Соберите на Arch:  cd $ROOT_DIR && ./scripts/deploy-helios.sh"
    echo "  Или только проверка: ./build.sh verify"
    echo "  Принудительно на Helios: LAB5_FORCE_HELIOS_BUILD=1 ./build.sh"
    exit 1
  fi

  "$gradle_cmd" -p "$ROOT_DIR" clean build distHelios

  verify_artifacts all
}

# --- main ---

CMD="${1:-build}"

case "$CMD" in
  verify|check)
    verify_artifacts "${2:-all}"
    exit 0
    ;;
  server)
    verify_artifacts server
    exit 0
    ;;
esac

if [[ "${LAB5_SKIP_BUILD:-}" == "1" ]]; then
  echo "LAB5_SKIP_BUILD=1 — пропуск Gradle"
  verify_artifacts all
  exit 0
fi

# Helios: если JAR уже лежат — не гонять Gradle
if is_helios_host; then
  shopt -s nullglob
  local_has_server=(dist/server*.jar)
  shopt -u nullglob
  if [[ ${#local_has_server[@]} -gt 0 ]]; then
    echo "Helios: dist/server*.jar найден — сборка Gradle не требуется."
    echo "Обновление с Arch: ./scripts/deploy-helios.sh (из каталога LAB_5 на Linux)"
    verify_artifacts server
    exit 0
  fi
fi

run_gradle_build

echo ""
echo "Run server: ./run-server.sh <db_login> [port]"
echo "Run server+GUI on Helios: ./helios-stack.sh all <db_login> [port]  (нужен DISPLAY / лучше remote-gui с Arch)"
echo "Run client: ./run-client.sh 127.0.0.1 5555"
echo "Run GUI: ./run-gui-client.sh real 127.0.0.1 5555"
