#!/bin/bash
# Verify the core artifact can become a GraalVM native executable when native-image is available.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SMOKE_SOURCE="${REPO_ROOT}/scripts/native-image-smoke/NativeImageSmoke.java"
WORK_DIRECTORY="$(mktemp -d "${TMPDIR:-/tmp}/krandom-native-image.XXXXXX")"

cleanup() {
    rm -rf "${WORK_DIRECTORY}"
}
trap cleanup EXIT

if ! command -v native-image >/dev/null 2>&1; then
    echo "Native-image smoke check skipped: GraalVM native-image is not installed."
    exit 0
fi

"${REPO_ROOT}/gradlew" :core:jar --quiet

CORE_JAR="$(find "${REPO_ROOT}/core/build/libs" -maxdepth 1 -name 'core-*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' -print -quit)"
if [[ -z "${CORE_JAR}" ]]; then
    echo "Native-image smoke check failed: core jar was not produced." >&2
    exit 1
fi

RUNTIME_CLASSPATH="$("${REPO_ROOT}/gradlew" :core:printRuntimeClasspath --quiet)"
SMOKE_CLASSPATH="${CORE_JAR}:${RUNTIME_CLASSPATH}:${WORK_DIRECTORY}"

javac --release 21 -cp "${CORE_JAR}:${RUNTIME_CLASSPATH}" -d "${WORK_DIRECTORY}" "${SMOKE_SOURCE}"

native-image \
    --no-fallback \
    --class-path "${SMOKE_CLASSPATH}" \
    -H:Class=io.github.frikit.krandom.smoke.NativeImageSmoke \
    -H:Name=krandom-native-image-smoke \
    -H:Path="${WORK_DIRECTORY}"

RESULT="$(${WORK_DIRECTORY}/krandom-native-image-smoke)"
[[ "${RESULT}" == "native-image-smoke-passed" ]] || {
    echo "Native-image smoke check failed: got '${RESULT}'." >&2
    exit 1
}

echo "Native-image smoke check passed."
