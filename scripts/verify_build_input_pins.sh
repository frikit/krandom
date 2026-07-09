#!/bin/bash
# Reject mutable or unverified inputs used by local and CI builds.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORKFLOW_DIR="${REPO_ROOT}/.github/workflows"
WRAPPER_PROPERTIES="${REPO_ROOT}/gradle/wrapper/gradle-wrapper.properties"
CI_WORKFLOW="${WORKFLOW_DIR}/continuous-integration-workflow.yml"
RELEASE_WORKFLOW="${WORKFLOW_DIR}/release-maven-central.yml"

fail() {
    echo "Build input pin verification failed: $*" >&2
    exit 1
}

wrapper_checksum="$(sed -En 's/^distributionSha256Sum=([0-9a-fA-F]+)$/\1/p' "${WRAPPER_PROPERTIES}")"
if [[ ! "${wrapper_checksum}" =~ ^[0-9a-fA-F]{64}$ ]]; then
    fail "Gradle wrapper distributionSha256Sum must be one SHA-256 value"
fi

action_count=0
while IFS= read -r action; do
    ((action_count += 1))
    case "${action}" in
        ./*)
            # Repository-local actions are immutable within the checked-out revision.
            ;;
        docker://*)
            [[ "${action}" =~ @sha256:[0-9a-fA-F]{64}$ ]] ||
                fail "container action must use an immutable digest: ${action}"
            ;;
        *@*)
            ref="${action##*@}"
            [[ "${ref}" =~ ^[0-9a-fA-F]{40}$ ]] ||
                fail "GitHub Action must use a full commit SHA: ${action}"
            ;;
        *)
            fail "action has no immutable ref: ${action}"
            ;;
    esac
done < <(sed -En 's/^[[:space:]-]*uses:[[:space:]]*([^#[:space:]]+).*/\1/p' "${WORKFLOW_DIR}"/*.yml)

if [[ "${action_count}" -eq 0 ]]; then
    fail "no GitHub Actions were found"
fi

mill_checksum="$(sed -En "s/^[[:space:]]*MILL_SHA256:[[:space:]]*['\"]?([0-9a-fA-F]{64})['\"]?[[:space:]]*$/\1/p" "${CI_WORKFLOW}")"
if [[ ! "${mill_checksum}" =~ ^[0-9a-fA-F]{64}$ ]]; then
    fail "MILL_SHA256 must be one SHA-256 value"
fi

if ! grep -Fq '"${MILL_SHA256}  ${HOME}/.local/bin/mill" | sha256sum --check --strict' "${CI_WORKFLOW}"; then
    fail "downloaded Mill launcher must be verified before execution"
fi

downloads="$(grep -R -n -E '^[[:space:]]+curl[[:space:]].*(-o|--output)[[:space:]]' "${WORKFLOW_DIR}" || true)"
unexpected_downloads="$(printf '%s\n' "${downloads}" | grep -v 'mill-dist-${MILL_VERSION}-mill\.sh' || true)"
if [[ -n "${unexpected_downloads}" ]]; then
    fail "downloaded workflow files need an explicit verification rule:\n${unexpected_downloads}"
fi

for permission in "id-token: write" "attestations: write" "artifact-metadata: write"; do
    if ! grep -Fq "${permission}" "${RELEASE_WORKFLOW}"; then
        fail "release provenance requires workflow permission: ${permission}"
    fi
done

if ! grep -Fq "build/nmcp/zip/aggregation.zip" "${RELEASE_WORKFLOW}"; then
    fail "release provenance must cover the exact Maven Central bundle"
fi

echo "Build input pins verified (${action_count} actions; Gradle wrapper and Mill checksums present)."
