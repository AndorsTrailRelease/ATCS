#!/usr/bin/env sh
set -eu

# Build and profile ATCS startup latency.
# Default mode: rebuild + run with startup markers + JFR + quick summary.

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT_DIR=$(CDPATH= cd -- "${SCRIPT_DIR}/.." && pwd)
JAR_PATH="${ROOT_DIR}/packaging/ATCS.jar"
JFR_PATH="${ROOT_DIR}/atcs.jfr"

JAVA_BIN="${JAVA_BIN:-/run/host/bin/java}"
JFR_BIN="${JFR_BIN:-/run/host/bin/jfr}"

if [ ! -x "${JAVA_BIN}" ]; then
  JAVA_BIN="java"
fi
if [ ! -x "${JFR_BIN}" ]; then
  JFR_BIN="jfr"
fi

MODE="jfr"
DO_BUILD=1

usage() {
  cat <<'EOF'
Usage: profile-startup.sh [options]

Options:
  --timing-only   Build + startup timing markers only (no JFR file)
  --jfr           Build + timing markers + JFR (default)
  --no-build      Skip rebuild and run current jar
  --help          Show this help

Notes:
  - The app is GUI-based. Close the app window to finish recording.
  - Timing markers print lines starting with: [ATCS] startup ...
EOF
}

while [ "$#" -gt 0 ]; do
  case "$1" in
    --timing-only)
      MODE="timing"
      ;;
    --jfr)
      MODE="jfr"
      ;;
    --no-build)
      DO_BUILD=0
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 2
      ;;
  esac
  shift
done

cd "${ROOT_DIR}"

if [ "${DO_BUILD}" -eq 1 ]; then
  echo "[ATCS] Building jar..."
  sh packaging/package.sh
fi

if [ ! -f "${JAR_PATH}" ]; then
  echo "[ATCS] Missing jar: ${JAR_PATH}" >&2
  exit 1
fi

if [ "${MODE}" = "timing" ]; then
  echo "[ATCS] Running startup timing only. Close the app when finished."
  "${JAVA_BIN}" -Datcs.profileStartup=true -jar "${JAR_PATH}"
  exit 0
fi

rm -f "${JFR_PATH}"
echo "[ATCS] Running startup timing + JFR. Close the app when finished."
"${JAVA_BIN}" -Datcs.profileStartup=true -XX:StartFlightRecording=filename="${JFR_PATH}",dumponexit=true,settings=profile -jar "${JAR_PATH}"

if [ ! -f "${JFR_PATH}" ]; then
  echo "[ATCS] JFR file was not created: ${JFR_PATH}" >&2
  exit 1
fi

echo ""
echo "[ATCS] JFR summary (header):"
"${JFR_BIN}" summary "${JFR_PATH}" | sed -n '1,28p'

echo ""
echo "[ATCS] Top ATCS execution-sample methods:"
"${JFR_BIN}" print --events jdk.ExecutionSample "${JFR_PATH}" > exec.txt
grep -oE 'com\.gpl\.rpg\.atcontentstudio\.[A-Za-z0-9_.$]+' exec.txt | sort | uniq -c | sort -nr | head -n 20

