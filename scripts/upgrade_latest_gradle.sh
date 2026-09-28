#!/bin/bash
# Upgrade the Gradle wrapper to the latest release, keeping the pinned distribution checksum.
# Manual tool: Dependabot normally proposes wrapper upgrades. Review the diff, then run
# ./scripts/update_verification_metadata.sh and the full ./scripts/pre_commit_check.sh.
set -euo pipefail

REPO_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "${REPO_ROOT}"

command -v jq >/dev/null 2>&1 || {
    echo "ERROR: jq is required to parse the Gradle version feed. Install jq and retry." >&2
    exit 1
}

echo "Make gradlew executable"
chmod +x gradlew

echo "Fetching latest Gradle version..."
version_json=$(curl -fsSL https://services.gradle.org/versions/current) || {
    echo "ERROR: failed to fetch https://services.gradle.org/versions/current" >&2
    exit 1
}

latest_version=$(printf '%s' "${version_json}" | jq -r '.version // empty')

if [ -z "${latest_version}" ]; then
    echo "ERROR: could not parse Gradle version from response:" >&2
    printf '%s\n' "${version_json}" >&2
    exit 1
fi

# gradle-wrapper.properties pins distributionSha256Sum, and the wrapper task refuses to drop it.
checksum_url="https://services.gradle.org/distributions/gradle-${latest_version}-all.zip.sha256"
distribution_checksum=$(curl -fsSL "${checksum_url}") || {
    echo "ERROR: failed to fetch ${checksum_url}" >&2
    exit 1
}
if [[ ! "${distribution_checksum}" =~ ^[0-9a-f]{64}$ ]]; then
    echo "ERROR: ${checksum_url} did not return one SHA-256 value" >&2
    exit 1
fi

echo "Upgrading Gradle wrapper to [${latest_version}] (sha256 ${distribution_checksum})"
wrapper_args=(
    wrapper
    --gradle-version "${latest_version}"
    --distribution-type=ALL
    --gradle-distribution-sha256-sum "${distribution_checksum}"
    --warning-mode=ALL
)
./gradlew "${wrapper_args[@]}"
# The second run uses the new distribution (verified against the checksum) to regenerate the
# wrapper jar and scripts from that version.
./gradlew "${wrapper_args[@]}"

./gradlew --version
