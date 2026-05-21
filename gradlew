#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
GRADLE_VERSION="8.10.2"
DIST_DIR="$APP_HOME/.gradle-dist"
ZIP_FILE="$DIST_DIR/gradle-${GRADLE_VERSION}-bin.zip"
UNPACK_DIR="$DIST_DIR/gradle-${GRADLE_VERSION}"
GRADLE_BIN="$UNPACK_DIR/bin/gradle"

resolve_java_major() {
  local java_cmd="$1"
  local version_line
  version_line="$("$java_cmd" -version 2>&1 | sed -n '1p')"
  local version
  version="$(echo "$version_line" | sed -E 's/.*version "([^"]+)".*/\1/')"
  if [[ "$version" == 1.* ]]; then
    echo "${version#1.}" | cut -d. -f1
  else
    echo "$version" | cut -d. -f1
  fi
}

select_compatible_java() {
  local java_cmd
  java_cmd="$(command -v java || true)"
  if [[ -z "$java_cmd" ]]; then
    return
  fi
  local major
  major="$(resolve_java_major "$java_cmd")"
  if [[ -z "$major" ]] || ! [[ "$major" =~ ^[0-9]+$ ]]; then
    return
  fi
  if (( major < 26 )); then
    return
  fi

  local fallback17="${JAVA17_HOME:-/usr/lib/jvm/java-17-openjdk}"
  local fallback21="${JAVA21_HOME:-/usr/lib/jvm/java-21-openjdk}"
  local fallback=""
  if [[ -x "$fallback17/bin/java" ]]; then
    fallback="$fallback17"
  elif [[ -x "$fallback21/bin/java" ]]; then
    fallback="$fallback21"
  fi
  if [[ -n "$fallback" ]]; then
    export JAVA_HOME="$fallback"
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "Detected Java ${major}; using JAVA_HOME=$JAVA_HOME for Gradle ${GRADLE_VERSION} compatibility."
  fi
}

select_compatible_java

if [[ ! -x "$GRADLE_BIN" ]]; then
  mkdir -p "$DIST_DIR"
  if [[ ! -f "$ZIP_FILE" ]]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    if command -v curl >/dev/null 2>&1; then
      curl -fL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$ZIP_FILE"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -O "$ZIP_FILE"
    else
      echo "Neither curl nor wget is available."
      exit 1
    fi
  fi
  echo "Extracting Gradle ${GRADLE_VERSION}..."
  unzip -q -o "$ZIP_FILE" -d "$DIST_DIR"
fi

exec "$GRADLE_BIN" "$@"
