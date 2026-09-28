#!/usr/bin/env bash
# Record SHA-256 checksums in gradle/verification-metadata.xml for every Gradle task graph that CI,
# the release workflow, and scripts/pre_commit_check.sh run. Dependabot cannot regenerate the file,
# so its dependency updates fail verification until a maintainer checks out the update branch, runs
# this script, reviews the diff (docs/development/dependency-reproducibility.md), and pushes it.
#
# Usage: ./scripts/update_verification_metadata.sh
#
# Gradle records only artifacts resolved during an invocation and keeps existing entries, so each
# distinct graph runs below. The script fails if an existing artifact gains a different checksum
# (<also-trust>) or a trust exception appears; investigate such a change instead of accepting it.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLEW="${REPO_ROOT}/gradlew"
METADATA="${REPO_ROOT}/gradle/verification-metadata.xml"

cd "${REPO_ROOT}"
"${REPO_ROOT}/scripts/require_java21.sh"

if [[ ! -f "${METADATA}" ]]; then
    # Pruning stale entries: the previous file was moved aside, so this run bootstraps a new one.
    echo "No ${METADATA}: bootstrapping a new file; compare it with the previous one before committing."
fi

# Counts lines with trust-widening elements; grep -c prints 0 (exit 1) when nothing matches.
trust_exceptions() {
    if [[ -f "${METADATA}" ]]; then
        grep -cE '<also-trust|<trusted-artifacts|<trust |<trusted-key|<ignored-key' "${METADATA}" || true
    else
        echo 0
    fi
}

write_metadata() {
    echo
    echo "==> Record checksums: $*"
    "${GRADLEW}" --write-verification-metadata sha256 --console=plain "$@"
}

TRUST_EXCEPTIONS_BEFORE="$(trust_exceptions)"

# CI build job, release build, and pre-commit gate: Spotless, compilation, all tests (benchmarks and
# examples-e2e included), core coverage, and Javadoc. Module-boundary and native-image checks reuse
# these jars and runtime classpaths.
write_metadata spotlessCheck build

# Release SBOMs: CycloneDX runs in its own invocation, as in CI and the release workflow.
write_metadata verifyReleaseSboms

# Critical-path mutation testing (CI mutation job and the full pre-commit gate).
write_metadata :core:pitest

# Signed-bundle assembly in the release workflow; covers the publications that
# scripts/verify_examples_local.sh installs with publishToMavenLocal.
write_metadata nmcpZipAggregation

TRUST_EXCEPTIONS_AFTER="$(trust_exceptions)"
if (( TRUST_EXCEPTIONS_AFTER > TRUST_EXCEPTIONS_BEFORE )); then
    echo >&2
    echo "ERROR: the update added ${TRUST_EXCEPTIONS_AFTER} - ${TRUST_EXCEPTIONS_BEFORE} trust exception(s) to ${METADATA}." >&2
    echo "An existing artifact resolved to different bytes or verification was widened; do not commit this" >&2
    echo "file until the change is explained (git diff -- gradle/verification-metadata.xml)." >&2
    exit 1
fi

echo
echo "==> gradle/verification-metadata.xml changes"
if git -C "${REPO_ROOT}" diff --quiet -- gradle/verification-metadata.xml; then
    echo "No changes: every resolved artifact already has a reviewed checksum."
else
    git -C "${REPO_ROOT}" diff --stat -- gradle/verification-metadata.xml
    echo "Review each added component and checksum, then run ./scripts/pre_commit_check.sh (strict verification)."
fi
