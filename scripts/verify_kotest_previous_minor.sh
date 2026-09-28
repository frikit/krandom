#!/usr/bin/env bash
# Compile and test krandom-kotest-extensions against the previous Kotest minor line; the current line
# is the `kotest` version in gradle/libs.versions.toml. Extra arguments are passed to Gradle.
# When the catalog moves to a new Kotest minor, set KOTEST_PREVIOUS_MINOR_VERSION to the newest patch
# of the minor it leaves and run ./scripts/update_verification_metadata.sh.
set -euo pipefail

KOTEST_PREVIOUS_MINOR_VERSION="6.1.11"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CURRENT_KOTEST_VERSION="$(awk -F'"' '$1 ~ /^kotest[[:space:]]*=[[:space:]]*$/ { print $2 }' "${REPO_ROOT}/gradle/libs.versions.toml")"

current_major="${CURRENT_KOTEST_VERSION%%.*}"
current_minor="$(printf '%s' "${CURRENT_KOTEST_VERSION}" | cut -d. -f2)"
previous_major="${KOTEST_PREVIOUS_MINOR_VERSION%%.*}"
previous_minor="$(printf '%s' "${KOTEST_PREVIOUS_MINOR_VERSION}" | cut -d. -f2)"
if ! [[ "${current_major}.${current_minor}" =~ ^[0-9]+\.[0-9]+$ ]] ||
    [[ "${previous_major}" != "${current_major}" || "${previous_minor}" != "$((current_minor - 1))" ]]; then
    echo "KOTEST_PREVIOUS_MINOR_VERSION=${KOTEST_PREVIOUS_MINOR_VERSION} is not on the minor line before the" >&2
    echo "catalog's Kotest ${CURRENT_KOTEST_VERSION:-<missing>}; update it in $0." >&2
    exit 1
fi

echo "Testing krandom-kotest-extensions against Kotest ${KOTEST_PREVIOUS_MINOR_VERSION} (current: ${CURRENT_KOTEST_VERSION})."
exec "${REPO_ROOT}/gradlew" :kotest-extensions:test -PkotestVersion="${KOTEST_PREVIOUS_MINOR_VERSION}" "$@"
