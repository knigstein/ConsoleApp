#!/usr/bin/env bash
# Выбор JDK для Gradle (portable: Arch/Linux + Helios/FreeBSD).
#
# Использование: source scripts/java-build-home.sh
set -euo pipefail

extract_major() {
  local java_bin="$1"
  local version_line version
  version_line="$("$java_bin" -version 2>&1 | sed -n '1p')"
  version="$(echo "$version_line" | sed -E 's/.*version "([^"]+)".*/\1/')"
  if [[ "$version" == 1.* ]]; then
    echo "${version#1.}" | cut -d. -f1
  else
    echo "$version" | cut -d. -f1
  fi
}

resolve_java_home_from_bin() {
  local java_bin="$1"
  local real_java
  # readlink -f есть не везде (например, FreeBSD), поэтому используем -f если доступно.
  if real_java="$(readlink -f "$java_bin" 2>/dev/null)"; then
    :
  else
    real_java="$java_bin"
  fi
  dirname "$(dirname "$real_java")"
}

pick_java_home() {
  local candidates=(
    "${JAVA_HOME:-}"
    /usr/lib/jvm/java-17-openjdk
    /usr/lib/jvm/java-17
    /usr/lib/jvm/java-21-openjdk
    /usr/lib/jvm/default
    /usr/local/openjdk17
    /usr/local/openjdk21
    /usr/local/openjdk-17
    /usr/local/openjdk-21
  )

  for dir in "${candidates[@]}"; do
    [[ -z "$dir" || ! -x "$dir/bin/java" ]] && continue
    local major
    major="$(extract_major "$dir/bin/java" 2>/dev/null || true)"
    if [[ "$major" == "17" || "$major" == "21" ]]; then
      echo "$dir"
      return 0
    fi
  done

  local java_cmd
  java_cmd="$(command -v java 2>/dev/null || true)"
  if [[ -n "$java_cmd" ]]; then
    local major inferred_home
    major="$(extract_major "$java_cmd" 2>/dev/null || true)"
    if [[ "$major" == "17" || "$major" == "21" ]]; then
      inferred_home="$(resolve_java_home_from_bin "$java_cmd")"
      if [[ -x "$inferred_home/bin/java" ]]; then
        echo "$inferred_home"
        return 0
      fi
    fi
  fi

  return 1
}

if ! JAVA_HOME="$(pick_java_home)"; then
  echo "ERROR: не найден JDK 17 или 21."
  echo "Текущий java: $(command -v java || echo 'не найден')"
  return 1 2>/dev/null || exit 1
fi

export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

ver="$("$JAVA_HOME/bin/java" -version 2>&1 | sed -n '1p')"
major="$(extract_major "$JAVA_HOME/bin/java")"
if [[ "$major" != "17" && "$major" != "21" ]]; then
  echo "ERROR: для Gradle нужен JDK 17 или 21, не Java $major ($JAVA_HOME)"
  return 1 2>/dev/null || exit 1
fi

echo "Сборка: JAVA_HOME=$JAVA_HOME ($ver)"
echo "Артефакты: bytecode Java 17 → на Helios достаточно java 17."
