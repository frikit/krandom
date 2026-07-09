#!/bin/bash
# Publish local artifacts and run the consumer examples against them.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLEW="${REPO_ROOT}/gradlew"
DEFAULT_VERSION="$(awk -F= '$1 == "developmentVersion" { print substr($0, index($0, "=") + 1) }' "${REPO_ROOT}/gradle.properties")"
VERSION="${KRANDOM_VERSION:-${DEFAULT_VERSION}}"
REQUIRE_SCALA_TOOLS="${KRANDOM_REQUIRE_SCALA_TOOLS:-false}"

step() { echo; echo "==> $*"; }

require_tool() {
    local tool="$1"
    if command -v "${tool}" >/dev/null 2>&1; then
        return 0
    fi
    if [[ "${REQUIRE_SCALA_TOOLS}" == "true" ]]; then
        echo "${tool} is required on PATH."
        exit 1
    fi
    return 1
}

cd "${REPO_ROOT}"

"${REPO_ROOT}/scripts/require_java21.sh"

step "Publish krandom modules ${VERSION} to Maven local"
"${GRADLEW}" \
    :bom:publishToMavenLocal \
    :core:publishToMavenLocal \
    :jackson:publishToMavenLocal \
    :junit:publishToMavenLocal \
    :spring-boot-starter:publishToMavenLocal \
    :kotest-extensions:publishToMavenLocal \
    :kotlin-dsl:publishToMavenLocal \
    -PreleaseVersion="${VERSION}" \
    --no-daemon \
    --console=plain

step "Verify Java + Gradle example"
(
    cd "${REPO_ROOT}/examples/java-gradle"
    ./gradlew -PkrandomVersion="${VERSION}" test --no-daemon --console=plain
)

step "Verify Java + Maven example"
(
    cd "${REPO_ROOT}/examples/java-maven"
    mvn -q -Dkrandom.version="${VERSION}" test
)

step "Verify Java + Gradle integration modules example"
"${GRADLEW}" -p "${REPO_ROOT}/examples/java-gradle-integrations" -PkrandomVersion="${VERSION}" test --no-daemon --console=plain

step "Verify Java + Maven integration modules example"
(
    cd "${REPO_ROOT}/examples/java-maven-integrations"
    mvn -q -Dkrandom.version="${VERSION}" test
)

step "Verify Kotlin + Gradle example"
(
    cd "${REPO_ROOT}/examples/kotlin-gradle"
    ./gradlew -PkrandomVersion="${VERSION}" test --no-daemon --console=plain
)

step "Verify Kotlin + Maven example"
(
    cd "${REPO_ROOT}/examples/kotlin-maven"
    mvn -q -Dkrandom.version="${VERSION}" test
)

step "Verify Kotlin + Maven integration modules example"
(
    cd "${REPO_ROOT}/examples/kotlin-maven-integrations"
    mvn -q -Dkrandom.version="${VERSION}" test
)

if require_tool sbt; then
    step "Verify Scala + sbt example"
    (
        cd "${REPO_ROOT}/examples/scala-sbt"
        sbt -Dkrandom.version="${VERSION}" test
    )
else
    step "Skip Scala + sbt example"
    echo "sbt is not installed on PATH."
fi

if require_tool mill; then
    step "Verify Scala + Mill example"
    (
        cd "${REPO_ROOT}/examples/scala-mill"
        # Mill requires -i (interactive / no-server) to be the first argument.
        mill -i -Dkrandom.version="${VERSION}" app.test
    )
else
    step "Skip Scala + Mill example"
    echo "mill is not installed on PATH."
fi
