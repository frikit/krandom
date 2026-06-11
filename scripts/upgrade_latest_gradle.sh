#!/bin/bash
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

echo "Upgrading Gradle wrapper to [${latest_version}]"
./gradlew wrapper --gradle-version "${latest_version}" --distribution-type=ALL --warning-mode=ALL

./gradlew --version
