#!/bin/bash
# Verify that a proposed release version matches the checked-in release facts.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${1:-}"
MODE="${2:-prepare}"
FACTS_FILE="${REPO_ROOT}/gradle.properties"
CHANGELOG="${REPO_ROOT}/CHANGELOG.md"

fail() {
    echo "Release facts check failed: $*" >&2
    exit 1
}

fact() {
    local key="$1"
    awk -F= -v key="${key}" '$1 == key { print substr($0, index($0, "=") + 1) }' "${FACTS_FILE}"
}

if [[ ! "${VERSION}" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-[0-9A-Za-z.-]+)?(\+[0-9A-Za-z.-]+)?$ ]]; then
    fail "expected a SemVer release version, got ${VERSION:-<missing>}"
fi

if [[ "${MODE}" != "prepare" && "${MODE}" != "resume" ]]; then
    fail "mode must be prepare or resume, got ${MODE}"
fi

LATEST_GA_VERSION="$(fact latestGaVersion)"
DEVELOPMENT_VERSION="$(fact developmentVersion)"
API_BASELINE_VERSION="$(fact apiBaselineVersion)"

[[ "${LATEST_GA_VERSION}" == "${VERSION}" ]] ||
    fail "latestGaVersion is ${LATEST_GA_VERSION}, expected ${VERSION}"

if [[ "${MODE}" == "prepare" ]]; then
    [[ "${DEVELOPMENT_VERSION}" == "${VERSION}-SNAPSHOT" ]] ||
        fail "developmentVersion is ${DEVELOPMENT_VERSION}, expected ${VERSION}-SNAPSHOT"
fi

grep -Eq "^## \[${VERSION//./\\.}\] - [0-9]{4}-[0-9]{2}-[0-9]{2}$" "${CHANGELOG}" ||
    fail "CHANGELOG.md has no dated ${VERSION} release heading"
grep -Fq "[Unreleased]: https://github.com/frikit/krandom/compare/v${VERSION}...HEAD" "${CHANGELOG}" ||
    fail "CHANGELOG.md Unreleased link does not start at v${VERSION}"
grep -Fq "[${VERSION}]: https://github.com/frikit/krandom/compare/v${API_BASELINE_VERSION}...v${VERSION}" "${CHANGELOG}" ||
    fail "CHANGELOG.md ${VERSION} link does not start at API baseline v${API_BASELINE_VERSION}"

echo "Release facts verified: ${VERSION} from baseline ${API_BASELINE_VERSION} (${MODE} mode)."
