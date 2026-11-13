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
REQUIRE_ANDROID_SDK="${KRANDOM_REQUIRE_ANDROID_SDK:-false}"

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

# Whether Gradle's toolchain detection, which the example builds use, finds a JDK of the given version, whose
# directory it then sets in FOUND_JDK_HOME. Without one the run fails when KRANDOM_REQUIRE_JAVA17=true, as CI
# sets, and skips the examples on that Java otherwise.
FOUND_JDK_HOME=""
require_jdk() {
    local version="$1" toolchains
    if ! toolchains="$("${GRADLEW}" -p "${REPO_ROOT}/examples/java-gradle" -q javaToolchains --no-daemon --console=plain)"; then
        echo "Listing the JDKs that Gradle detects failed."
        exit 1
    fi
    # Each detected Java is an entry that starts with " + "; the report also lists JREs, which cannot compile.
    # Prints the location of the first JDK of that version.
    FOUND_JDK_HOME="$(awk -v version="${version}" '
        function check() { if (found == "" && language == version && jdk == "true") found = home }
        /^ \+ / { check(); language = ""; jdk = ""; home = "" }
        /\| Location:/ { home = $0; sub(/^.*\| Location:[[:space:]]*/, "", home) }
        /\| Language Version:/ { language = $NF }
        /\| Is JDK:/ { jdk = $NF }
        END { check(); if (found != "") print found }' <<< "${toolchains}")"
    if [[ -n "${FOUND_JDK_HOME}" ]]; then
        return 0
    fi
    if [[ "${REQUIRE_JAVA17}" == "true" ]]; then
        echo "Gradle detects no JDK ${version} (./gradlew javaToolchains), which KRANDOM_REQUIRE_JAVA17=true requires."
        exit 1
    fi
    return 1
}

# Succeeds when ANDROID_HOME, or else ANDROID_SDK_ROOT, names a directory, which is then exported as
# ANDROID_HOME for the Android Gradle plugin. Without one, the Android example is skipped unless
# KRANDOM_REQUIRE_ANDROID_SDK is true, which makes a missing SDK an error.
require_android_sdk() {
    local sdk
    for sdk in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}"; do
        if [[ -n "${sdk}" && -d "${sdk}" ]]; then
            export ANDROID_HOME="${sdk}"
            return 0
        fi
    done
    if [[ "${REQUIRE_ANDROID_SDK}" == "true" ]]; then
        echo "An Android SDK is required: set ANDROID_HOME or ANDROID_SDK_ROOT to its directory."
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

# The oldest Java that kRandom runs on: a consumer on it resolves the Gradle metadata and runs the modules.
MINIMUM_JDK_HOME=""
if require_jdk "${JAVA_MINIMUM_VERSION}"; then
    MINIMUM_JDK_HOME="${FOUND_JDK_HOME}"
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

if require_android_sdk; then
    step "Verify Kotlin + Android unit-test example (Android SDK at ${ANDROID_HOME})"
    (
        cd "${REPO_ROOT}/examples/kotlin-android"
        ./gradlew -PkrandomVersion="${VERSION}" test --no-daemon --console=plain
    )
    # The example sets no toolchain, so its unit tests run on the Java that runs Gradle.
    if [[ -n "${MINIMUM_JDK_HOME}" ]]; then
        step "Verify Kotlin + Android unit-test example with Gradle on Java ${JAVA_MINIMUM_VERSION}"
        (
            cd "${REPO_ROOT}/examples/kotlin-android"
            export JAVA_HOME="${MINIMUM_JDK_HOME}"
            ./gradlew --version --no-daemon | grep -E '^(Launcher|Daemon) JVM'
            ./gradlew -PkrandomVersion="${VERSION}" test --rerun-tasks --no-daemon --console=plain
        )
    else
        step "Skip Kotlin + Android unit-test example on Java ${JAVA_MINIMUM_VERSION}"
        echo "Gradle detects no JDK ${JAVA_MINIMUM_VERSION}; install one, or set KRANDOM_REQUIRE_JAVA17=true to fail instead."
    fi
else
    step "Skip Kotlin + Android unit-test example"
    echo "No Android SDK: set ANDROID_HOME or ANDROID_SDK_ROOT to run it."
fi

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
