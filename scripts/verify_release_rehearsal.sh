#!/bin/bash
# Rehearse local release checks without tagging or publishing artifacts.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${1:-}"
RUNBOOK="${REPO_ROOT}/docs/release-runbook.md"

if [[ ! "${VERSION}" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-[0-9A-Za-z.-]+)?(\+[0-9A-Za-z.-]+)?$ ]]; then
    echo "Usage: $0 <SemVer release version>; got: ${VERSION:-<missing>}" >&2
    exit 1
fi

# Exit status 1 means the tag is absent; anything else (git missing, not a repository) is an error
# rather than a silent "no tag".
tag_status=0
git -C "${REPO_ROOT}" rev-parse -q --verify "refs/tags/v${VERSION}" >/dev/null || tag_status=$?
case "${tag_status}" in
    0)
        echo "Release rehearsal requires a new version; tag already exists: v${VERSION}" >&2
        exit 1
        ;;
    1) ;;
    *)
        echo "Release rehearsal could not check tag v${VERSION} (git exit status ${tag_status})." >&2
        exit 1
        ;;
esac

for marker in "Release rehearsal and recovery" "resumeGithubRelease=true" "Never rerun the Central upload"; do
    if ! grep -Fq "${marker}" "${RUNBOOK}"; then
        echo "Release runbook is missing recovery marker: ${marker}" >&2
        exit 1
    fi
done

"${REPO_ROOT}/scripts/require_java21.sh"
"${REPO_ROOT}/scripts/verify_release_facts.sh" "${VERSION}"
"${REPO_ROOT}/scripts/verify_documentation_facts.sh"
"${REPO_ROOT}/gradlew" clean build check checkApiContract \
    -PreleaseVersion="${VERSION}" \
    --stacktrace --console=plain --max-workers=1 --no-daemon \
    -x :benchmarks:test -x :benchmarks:check
"${REPO_ROOT}/gradlew" verifyReleaseSboms \
    -PreleaseVersion="${VERSION}" \
    --stacktrace --console=plain --max-workers=1 --no-daemon

echo "Release rehearsal passed for ${VERSION}; no tag or remote publication was attempted."
