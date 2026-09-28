#!/usr/bin/env bash
# Compile a real consumer/extension against 2.2.0 and run identical bytecode on the candidate.
# CI runs it through `./gradlew verifyV2ConsumerCompatibility`, which resolves all three arguments.
set -euo pipefail
if [[ $# != 3 ]]; then
    echo "Usage: $0 <released-2.2.0-core.jar> <candidate-core.jar> <runtime-dependency-classpath>" >&2
    exit 1
fi
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPAT_WORK="$(mktemp -d "${TMPDIR:-/tmp}/krandom-v2-compatibility.XXXXXX")"
trap 'rm -rf "${COMPAT_WORK}"' EXIT

# The default object.unique-fields recipe setting is built from Set.of(...), whose iteration order is
# randomized per JVM run in every released 2.x version. Compare that setting as a set by sorting its
# URL-encoded, comma-separated members; every other line must match exactly.
normalize_output() {
    awk '
        index($0, "setting.object.unique-fields=") == 1 {
            prefix = "setting.object.unique-fields="
            count = split(substr($0, length(prefix) + 1), members, "%2C")
            for (i = 2; i <= count; i++) {
                value = members[i]
                for (j = i - 1; j >= 1 && members[j] > value; j--) {
                    members[j + 1] = members[j]
                }
                members[j + 1] = value
            }
            line = prefix
            for (i = 1; i <= count; i++) {
                line = line (i > 1 ? "%2C" : "") members[i]
            }
            print line
            next
        }
        { print }
    ' "$1"
}

"${REPO_ROOT}/scripts/require_java21.sh"
javac --release 21 -cp "$1:$3" -d "${COMPAT_WORK}" "${REPO_ROOT}/scripts/compatibility/V2Consumer.java"
java -cp "${COMPAT_WORK}:$1:$3" V2Consumer > "${COMPAT_WORK}/baseline.raw"
java -cp "${COMPAT_WORK}:$2:$3" V2Consumer > "${COMPAT_WORK}/candidate.raw"
normalize_output "${COMPAT_WORK}/baseline.raw" > "${COMPAT_WORK}/baseline.txt"
normalize_output "${COMPAT_WORK}/candidate.raw" > "${COMPAT_WORK}/candidate.txt"
diff -u "${COMPAT_WORK}/baseline.txt" "${COMPAT_WORK}/candidate.txt"
echo "V2 bytecode, extension, recipe and default-output compatibility verified."
