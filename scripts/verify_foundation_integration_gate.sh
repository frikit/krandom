#!/usr/bin/env bash
# Run the reproducible Stage 2 correctness and published-consumer verification gate.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

step() {
    echo
    echo "==> $*"
}

step "Run the Stage 2 correctness gate"
"${REPO_ROOT}/scripts/pre_commit_check.sh"

step "Verify locally published Java, Kotlin, JPMS, and Scala consumers"
"${REPO_ROOT}/scripts/verify_examples_local.sh"
