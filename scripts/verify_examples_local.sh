#!/bin/bash
# Publish local artifacts and run the consumer examples against them.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLEW="${REPO_ROOT}/gradlew"
DEFAULT_VERSION="$(awk -F= '$1 == "developmentVersion" { print substr($0, index($0, "=") + 1) }' "${REPO_ROOT}/gradle.properties")"
PUBLISHED_MODULES="$(awk -F= '$1 == "publishedModules" { print substr($0, index($0, "=") + 1) }' "${REPO_ROOT}/gradle.properties")"
JAVA_MINIMUM_VERSION="$(awk -F= '$1 == "javaMinimumVersion" { print substr($0, index($0, "=") + 1) }' "${REPO_ROOT}/gradle.properties")"
VERSION="${KRANDOM_VERSION:-${DEFAULT_VERSION}}"
REQUIRE_SCALA_TOOLS="${KRANDOM_REQUIRE_SCALA_TOOLS:-false}"
REQUIRE_JAVA17="${KRANDOM_REQUIRE_JAVA17:-false}"

if [[ -z "${PUBLISHED_MODULES}" ]]; then
    echo "gradle.properties must define publishedModules." >&2
    exit 1
fi
if [[ -z "${JAVA_MINIMUM_VERSION}" ]]; then
    echo "gradle.properties must define javaMinimumVersion." >&2
    exit 1
fi
PUBLISH_TASKS=()
IFS=',' read -r -a published_modules <<< "${PUBLISHED_MODULES}"
for module in "${published_modules[@]}"; do
    PUBLISH_TASKS+=(":${module}:publishToMavenLocal")
done

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

# Whether Gradle's toolchain detection, which the example builds use, finds a JDK of the given version.
# Without one the run fails when KRANDOM_REQUIRE_JAVA17=true, as CI sets, and skips that example otherwise.
require_jdk() {
    local version="$1" toolchains
    if ! toolchains="$("${GRADLEW}" -p "${REPO_ROOT}/examples/java-gradle" -q javaToolchains --no-daemon --console=plain)"; then
        echo "Listing the JDKs that Gradle detects failed."
        exit 1
    fi
    # Each detected Java is an entry that starts with " + "; the report also lists JREs, which cannot compile.
    if awk -v version="${version}" '
        /^ \+ / { if (language == version && jdk == "true") found = 1; language = ""; jdk = "" }
        /\| Language Version:/ { language = $NF }
        /\| Is JDK:/ { jdk = $NF }
        END { if (language == version && jdk == "true") found = 1; exit(found ? 0 : 1) }' <<< "${toolchains}"; then
        return 0
    fi
    if [[ "${REQUIRE_JAVA17}" == "true" ]]; then
        echo "Gradle detects no JDK ${version} (./gradlew javaToolchains), which KRANDOM_REQUIRE_JAVA17=true requires."
        exit 1
    fi
    return 1
}

cd "${REPO_ROOT}"

"${REPO_ROOT}/scripts/require_java21.sh"

step "Publish krandom modules ${VERSION} to Maven local"
"${GRADLEW}" \
    "${PUBLISH_TASKS[@]}" \
    -PreleaseVersion="${VERSION}" \
    --no-daemon \
    --console=plain

step "Verify Java + Gradle example"
(
    cd "${REPO_ROOT}/examples/java-gradle"
    ./gradlew -PkrandomVersion="${VERSION}" test --no-daemon --console=plain
)

# The oldest Java that the Java modules run on: a consumer on it resolves their Gradle metadata and runs them.
if require_jdk "${JAVA_MINIMUM_VERSION}"; then
    step "Verify Java + Gradle example on Java ${JAVA_MINIMUM_VERSION}"
    (
        cd "${REPO_ROOT}/examples/java-gradle"
        ./gradlew -PkrandomVersion="${VERSION}" -PjavaVersion="${JAVA_MINIMUM_VERSION}" test --no-daemon --console=plain
    )
else
    step "Skip Java + Gradle example on Java ${JAVA_MINIMUM_VERSION}"
    echo "Gradle detects no JDK ${JAVA_MINIMUM_VERSION} (./gradlew javaToolchains); install one, or set KRANDOM_REQUIRE_JAVA17=true to fail instead."
fi

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

step "Verify Java 21 named-module example"
"${GRADLEW}" -p "${REPO_ROOT}/examples/java-jpms" -PkrandomVersion="${VERSION}" verifyJpms --no-daemon --console=plain

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
