#!/bin/bash
# Verify published artifacts from Maven Central without Maven-local resolution.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${KRANDOM_VERSION:-}"
REQUIRE_ANDROID_SDK="${KRANDOM_REQUIRE_ANDROID_SDK:-false}"
MAVEN_REPOSITORY="$(mktemp -d "${TMPDIR:-/tmp}/krandom-central-m2.XXXXXX")"

cleanup() {
    rm -rf "${MAVEN_REPOSITORY}"
}
trap cleanup EXIT

if [[ -z "${VERSION}" ]]; then
    echo "KRANDOM_VERSION must name the published version to verify (for example 2.3.0)." >&2
    exit 1
fi
if [[ "${VERSION}" == *SNAPSHOT* ]]; then
    echo "KRANDOM_VERSION must be a published non-SNAPSHOT version, got: ${VERSION}" >&2
    exit 1
fi

"${REPO_ROOT}/scripts/require_java21.sh"

step() { echo; echo "==> $*"; }

# Succeeds when ANDROID_HOME or ANDROID_SDK_ROOT names a directory, which is then exported as ANDROID_HOME
# for the Android Gradle plugin. Without one, the Android consumer is skipped unless KRANDOM_REQUIRE_ANDROID_SDK
# is true, which makes a missing SDK an error.
require_android_sdk() {
    local sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
    if [[ -n "${sdk}" && -d "${sdk}" ]]; then
        export ANDROID_HOME="${sdk}"
        return 0
    fi
    if [[ "${REQUIRE_ANDROID_SDK}" == "true" ]]; then
        echo "An Android SDK is required: set ANDROID_HOME or ANDROID_SDK_ROOT to its directory."
        exit 1
    fi
    return 1
}

step "Verify plain Java Maven consumer from Maven Central"
(
    cd "${REPO_ROOT}/examples/java-maven"
    mvn -q -Dmaven.repo.local="${MAVEN_REPOSITORY}" -Dkrandom.version="${VERSION}" test
)

step "Verify Kotlin Spring Maven consumer from Maven Central"
(
    cd "${REPO_ROOT}/examples/kotlin-maven-integrations"
    mvn -q -Dmaven.repo.local="${MAVEN_REPOSITORY}" -Dkrandom.version="${VERSION}" test
)

step "Verify plain Java Gradle consumer from Maven Central"
(
    cd "${REPO_ROOT}/examples/java-gradle"
    ./gradlew -PkrandomVersion="${VERSION}" -PkrandomRepository=central test --no-daemon --console=plain
)

if require_android_sdk; then
    step "Verify Kotlin Android unit-test consumer from Maven Central (Android SDK at ${ANDROID_HOME})"
    (
        cd "${REPO_ROOT}/examples/kotlin-android"
        ./gradlew -PkrandomVersion="${VERSION}" -PkrandomRepository=central test --no-daemon --console=plain
    )
else
    step "Skip Kotlin Android unit-test consumer"
    echo "No Android SDK: set ANDROID_HOME or ANDROID_SDK_ROOT to run it."
fi

echo "Maven Central consumer verification passed for krandom ${VERSION}."
