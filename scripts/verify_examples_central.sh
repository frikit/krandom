#!/bin/bash
# Verify published artifacts from Maven Central without Maven-local resolution.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${KRANDOM_VERSION:-}"
MAVEN_REPOSITORY="$(mktemp -d "${TMPDIR:-/tmp}/krandom-central-m2.XXXXXX")"

cleanup() {
    rm -rf "${MAVEN_REPOSITORY}"
}
trap cleanup EXIT

if [[ -z "${VERSION}" ]]; then
    echo "KRANDOM_VERSION must name the published version to verify (for example 2.2.0)." >&2
    exit 1
fi
if [[ "${VERSION}" == *SNAPSHOT* ]]; then
    echo "KRANDOM_VERSION must be a published non-SNAPSHOT version, got: ${VERSION}" >&2
    exit 1
fi

"${REPO_ROOT}/scripts/require_java21.sh"

step() { echo; echo "==> $*"; }

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

echo "Maven Central consumer verification passed for krandom ${VERSION}."
