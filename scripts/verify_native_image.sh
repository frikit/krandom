#!/bin/bash
# Verify the core artifact can become a GraalVM native executable when native-image is available.
# Locally a missing native-image skips the check; set KRANDOM_REQUIRE_NATIVE_IMAGE=true (as CI does)
# to fail instead, so a job that promises this gate cannot pass without running it.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SMOKE_SOURCE="${REPO_ROOT}/scripts/native-image-smoke/NativeImageSmoke.java"
REFLECTION_CONFIG="${REPO_ROOT}/scripts/native-image-smoke/reflect-config.json"
REQUIRE_NATIVE_IMAGE="${KRANDOM_REQUIRE_NATIVE_IMAGE:-false}"
WORK_DIRECTORY="$(mktemp -d "${TMPDIR:-/tmp}/krandom-native-image.XXXXXX")"

cleanup() {
    rm -rf "${WORK_DIRECTORY}"
}
trap cleanup EXIT

if ! command -v native-image >/dev/null 2>&1; then
    if [[ "${REQUIRE_NATIVE_IMAGE}" == "true" ]]; then
        echo "Native-image smoke check failed: GraalVM native-image is not on PATH and KRANDOM_REQUIRE_NATIVE_IMAGE=true." >&2
        exit 1
    fi
    echo "Native-image smoke check skipped: GraalVM native-image is not installed."
    exit 0
fi

DEVELOPMENT_VERSION="$(awk -F= '$1 == "developmentVersion" { print substr($0, index($0, "=") + 1) }' "${REPO_ROOT}/gradle.properties")"

"${REPO_ROOT}/gradlew" :core:jar --quiet

# Select the jar for the current development version; build/libs can hold stale release jars.
CORE_JAR="${REPO_ROOT}/core/build/libs/core-${DEVELOPMENT_VERSION}.jar"
if [[ -z "${DEVELOPMENT_VERSION}" || ! -f "${CORE_JAR}" ]]; then
    echo "Native-image smoke check failed: core jar was not produced at ${CORE_JAR}." >&2
    exit 1
fi

RUNTIME_CLASSPATH="$("${REPO_ROOT}/gradlew" :core:printRuntimeClasspath --quiet)"
SMOKE_CLASSPATH="${CORE_JAR}:${RUNTIME_CLASSPATH}:${WORK_DIRECTORY}"

javac --release 21 -cp "${CORE_JAR}:${RUNTIME_CLASSPATH}" -d "${WORK_DIRECTORY}" "${SMOKE_SOURCE}"

native-image \
    --no-fallback \
    --class-path "${SMOKE_CLASSPATH}" \
    -H:IncludeResources='krandom/.*' \
    -H:ReflectionConfigurationFiles="${REFLECTION_CONFIG}" \
    -H:Class=io.github.frikit.krandom.smoke.NativeImageSmoke \
    -H:Name=krandom-native-image-smoke \
    -H:Path="${WORK_DIRECTORY}"

RESULT="$("${WORK_DIRECTORY}/krandom-native-image-smoke")"
[[ "${RESULT}" == "native-image-smoke-passed" ]] || {
    echo "Native-image smoke check failed: got '${RESULT}'." >&2
    exit 1
}

echo "Native-image smoke check passed."
