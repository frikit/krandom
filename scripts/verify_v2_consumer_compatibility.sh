#!/usr/bin/env bash
# Compile a real consumer/extension against 2.2.0 and run identical bytecode on the candidate.
set -euo pipefail
if [[ $# != 3 ]]; then
    echo "Usage: $0 <released-2.2.0-core.jar> <candidate-core.jar> <runtime-dependency-classpath>" >&2
    exit 1
fi
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPAT_WORK="$(mktemp -d "${TMPDIR:-/tmp}/krandom-v2-compatibility.XXXXXX")"
trap 'rm -rf "${COMPAT_WORK}"' EXIT
"${REPO_ROOT}/scripts/require_java21.sh"
javac --release 21 -cp "$1:$3" -d "${COMPAT_WORK}" "${REPO_ROOT}/scripts/compatibility/V2Consumer.java"
java -cp "${COMPAT_WORK}:$1:$3" V2Consumer > "${COMPAT_WORK}/baseline.txt"
java -cp "${COMPAT_WORK}:$2:$3" V2Consumer > "${COMPAT_WORK}/candidate.txt"
diff -u "${COMPAT_WORK}/baseline.txt" "${COMPAT_WORK}/candidate.txt"
echo "V2 bytecode, extension, recipe and default-output compatibility verified."
