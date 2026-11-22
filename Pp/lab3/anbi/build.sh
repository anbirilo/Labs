#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

is_wsl() {
  grep -qi microsoft /proc/version 2>/dev/null
}

find_binary() {
  local tool="$1"
  local roots=()
  if is_wsl; then
    roots+=("/mnt/c")
  fi
  if [[ -d "/c/Program Files" ]]; then
    roots+=("/c")
  fi
  shopt -s nullglob
  local root candidates=()
  for root in "${roots[@]}"; do
    candidates+=(
      "$root/Program Files/Android/Android Studio/jbr/bin/${tool}.exe"
      "$root/Program Files/Java/"*/bin/"${tool}.exe"
      "$root/Program Files/Eclipse Adoptium/"*/bin/"${tool}.exe"
      "$root/Program Files/Microsoft/jdk-"*/bin/"${tool}.exe"
      "$root/Program Files/Amazon Corretto/"*/bin/"${tool}.exe"
    )
  done
  shopt -u nullglob
  local candidate
  for candidate in "${candidates[@]}"; do
    if [[ -f "$candidate" ]]; then
      printf '%s\n' "$candidate"
      return
    fi
  done
  if command -v "$tool" >/dev/null 2>&1; then
    command -v "$tool"
    return
  fi
  echo "Не найден $tool. Добавьте JDK в PATH." >&2
  exit 1
}

# Git Bash держит консоль в CP866, а Java пишет UTF-8. Без этого русский выглядит как «╨д╨░╨╣╨╗».
use_utf8_console() {
  if is_wsl; then
    return
  fi
  if command -v chcp.com >/dev/null 2>&1; then
    chcp.com 65001 >/dev/null || true
  fi
}

JAVAC="$(find_binary javac)"
JAVA="$(find_binary java)"
JAVA_OPTS=(-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8)

if [[ "$JAVAC" == *.exe || "$JAVA" == *.exe ]]; then
  CP_SEP=';'
else
  CP_SEP=':'
fi

VERSION="$("$JAVAC" -version 2>&1 || true)"
if [[ "$VERSION" == *"1.8"* ]]; then
  RELEASE_ARGS=(-source 1.8 -target 1.8)
else
  RELEASE_ARGS=(--release 8)
fi

mkdir -p out/main
mapfile -d '' MAIN_SOURCES < <(find src/main/java -name '*.java' -print0)
"$JAVAC" -encoding UTF-8 -Xlint:-options "${RELEASE_ARGS[@]}" -d out/main "${MAIN_SOURCES[@]}"

if [[ "${1:-}" == "--run" ]]; then
  use_utf8_console
  "$JAVA" "${JAVA_OPTS[@]}" -cp "out/main" coffeefactory.Main
  exit 0
fi

JUNIT="lib/junit-platform-console-standalone-1.9.3.jar"
if [[ ! -f "$JUNIT" ]]; then
  mkdir -p lib
  curl -fsSL -o "$JUNIT" \
    "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.9.3/junit-platform-console-standalone-1.9.3.jar"
fi

mkdir -p out/test
mapfile -d '' TEST_SOURCES < <(find src/test/java -name '*.java' -print0)
"$JAVAC" -encoding UTF-8 -Xlint:-options "${RELEASE_ARGS[@]}" -cp "out/main${CP_SEP}${JUNIT}" -d out/test "${TEST_SOURCES[@]}"

use_utf8_console
"$JAVA" "${JAVA_OPTS[@]}" -jar "$JUNIT" -cp "out/main${CP_SEP}out/test" --scan-classpath --disable-banner --details=summary
