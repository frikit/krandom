#!/usr/bin/env bash
# Record verification-metadata checksums for a Dependabot pull request.
#
# Dependabot cannot run scripts/update_verification_metadata.sh, so its Gradle updates fail
# dependency verification until a maintainer records the new checksums. This script does the
# mechanical part: it checks the pull request's branch out into a temporary git worktree, runs the
# metadata update under every JDK in KRANDOM_METADATA_JDKS (CI on Linux/Java 25 resolves POM and
# Gradle-module entries that a Java 21 run can miss, so run both), commits the metadata change on
# that branch, and prints what was added. Reviewing the diff against the checklist in
# docs/development/dependency-reproducibility.md stays with the maintainer: push with --push once
# the diff is reviewed, or push from the kept worktree by hand.
#
# Usage: ./scripts/fix_dependabot_pr.sh <pull-request-number> [--push] [--keep-worktree]
#   --push            push the metadata commit to the pull request branch after the update
#   --keep-worktree   keep the temporary worktree (its path is printed) instead of removing it
#   KRANDOM_METADATA_JDKS   colon-separated JAVA_HOME directories to run in order, for example
#                           "/path/to/jdk-21:/path/to/jdk-25"; defaults to JAVA_HOME alone.
#
# Exit code: 0 = metadata recorded (or already complete), non-zero = a step failed; the worktree is
# kept on failure so the partial result can be inspected.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
METADATA="gradle/verification-metadata.xml"

usage() {
    sed -n '2,20p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
}

PR_NUMBER=""
PUSH=false
KEEP_WORKTREE=false
for arg in "$@"; do
    case "${arg}" in
        --push) PUSH=true ;;
        --keep-worktree) KEEP_WORKTREE=true ;;
        -h|--help) usage; exit 0 ;;
        -*) echo "Unknown option: ${arg}" >&2; usage >&2; exit 2 ;;
        *)
            if [[ -n "${PR_NUMBER}" ]]; then
                echo "Only one pull request number is accepted, got '${PR_NUMBER}' and '${arg}'" >&2
                exit 2
            fi
            PR_NUMBER="${arg}"
            ;;
    esac
done
if [[ ! "${PR_NUMBER}" =~ ^[0-9]+$ ]]; then
    echo "A pull request number is required." >&2
    usage >&2
    exit 2
fi
command -v gh >/dev/null 2>&1 || { echo "gh (GitHub CLI) is required." >&2; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "jq is required." >&2; exit 1; }

# JDKs to run, in order. Each must contain bin/java.
IFS=':' read -r -a JDK_HOMES <<< "${KRANDOM_METADATA_JDKS:-${JAVA_HOME:-}}"
if (( ${#JDK_HOMES[@]} == 0 )) || [[ -z "${JDK_HOMES[0]}" ]]; then
    echo "Set KRANDOM_METADATA_JDKS (colon-separated JAVA_HOME directories) or JAVA_HOME." >&2
    exit 1
fi
for jdk in "${JDK_HOMES[@]}"; do
    if [[ ! -x "${jdk}/bin/java" ]]; then
        echo "Not a JDK directory: ${jdk}" >&2
        exit 1
    fi
done
if (( ${#JDK_HOMES[@]} == 1 )); then
    echo "WARNING: running under one JDK only; CI on Java 25 can resolve artifacts this run does not record." >&2
    echo "         Set KRANDOM_METADATA_JDKS to a JDK 21 and a JDK 25 directory to cover both." >&2
fi

cd "${REPO_ROOT}"

echo "==> Pull request #${PR_NUMBER}"
PR_JSON="$(gh pr view "${PR_NUMBER}" --json headRefName,author,title,state)"
BRANCH="$(jq -r '.headRefName' <<< "${PR_JSON}")"
AUTHOR="$(jq -r '.author.login' <<< "${PR_JSON}")"
STATE="$(jq -r '.state' <<< "${PR_JSON}")"
echo "    $(jq -r '.title' <<< "${PR_JSON}") (${BRANCH}, by ${AUTHOR}, ${STATE})"
if [[ "${STATE}" != "OPEN" ]]; then
    echo "The pull request is not open." >&2
    exit 1
fi
if [[ "${AUTHOR}" != "app/dependabot" && "${AUTHOR}" != "dependabot[bot]" ]]; then
    echo "WARNING: #${PR_NUMBER} was not opened by Dependabot; continuing on ${BRANCH} anyway." >&2
fi

echo "==> Fetch ${BRANCH}"
git fetch --quiet origin "${BRANCH}"

# A previous run keeps its worktree until the branch is pushed; reuse that checkout instead of
# creating a second one for the same branch.
# The porcelain path is everything after "worktree " (it may contain spaces).
EXISTING_WORKTREE="$(git worktree list --porcelain | awk -v ref="branch refs/heads/${BRANCH}" '
    /^worktree / { path = substr($0, 10) }
    $0 == ref { print path; exit }')"
if [[ -n "${EXISTING_WORKTREE}" ]]; then
    QUOTED_WORKTREE="$(printf '%q' "${EXISTING_WORKTREE}")"
    echo "${BRANCH} is already checked out at ${EXISTING_WORKTREE}." >&2
    echo "Push from there (git -C ${QUOTED_WORKTREE} push origin ${BRANCH}) or remove it first" >&2
    echo "(git worktree remove ${QUOTED_WORKTREE}) and rerun." >&2
    exit 1
fi
# Never discard local work: a local branch that is ahead of the fetched tip is left alone.
if git show-ref --verify --quiet "refs/heads/${BRANCH}" \
   && ! git merge-base --is-ancestor "${BRANCH}" "origin/${BRANCH}"; then
    echo "Local branch ${BRANCH} has commits that origin/${BRANCH} does not have." >&2
    echo "Push or delete the local branch before rerunning." >&2
    exit 1
fi

WORKTREE="$(mktemp -d "${TMPDIR:-/tmp}/krandom-pr-${PR_NUMBER}.XXXXXX")"
echo "==> Worktree ${WORKTREE}"
# -B creates the local branch at the fetched tip or fast-forwards it there (the guard above
# rejected any branch that would lose commits).
git worktree add --quiet -B "${BRANCH}" "${WORKTREE}" "origin/${BRANCH}"

cleanup() {
    if [[ "${KEEP_WORKTREE}" == true ]]; then
        echo "Worktree kept at ${WORKTREE} (branch ${BRANCH}); remove it with: git worktree remove $(printf '%q' "${WORKTREE}")"
    else
        git worktree remove --force "${WORKTREE}"
    fi
}

BEFORE="$(git -C "${WORKTREE}" rev-parse HEAD)"
for jdk in "${JDK_HOMES[@]}"; do
    echo
    echo "==> Record checksums with $("${jdk}/bin/java" -version 2>&1 | head -n 1) (${jdk})"
    if ! JAVA_HOME="${jdk}" PATH="${jdk}/bin:${PATH}" "${WORKTREE}/scripts/update_verification_metadata.sh"; then
        echo "Metadata update failed under ${jdk}; worktree kept at ${WORKTREE}" >&2
        exit 1
    fi
done

if git -C "${WORKTREE}" diff --quiet -- "${METADATA}"; then
    echo
    echo "==> No metadata change: every resolved artifact already has a reviewed checksum."
    cleanup
    exit 0
fi

echo
echo "==> Added entries (review each one against docs/development/dependency-reproducibility.md)"
git -C "${WORKTREE}" diff -- "${METADATA}" | grep -E '^\+\s*<(component|artifact)' | sed 's/^+//' || true
git -C "${WORKTREE}" diff --stat -- "${METADATA}"

git -C "${WORKTREE}" add -- "${METADATA}"
git -C "${WORKTREE}" commit --quiet -m "build(deps): record verification metadata for #${PR_NUMBER}"
echo "==> Committed $(git -C "${WORKTREE}" rev-parse --short HEAD) on ${BRANCH} (was ${BEFORE:0:8})"

if [[ "${PUSH}" == true ]]; then
    echo "==> Push ${BRANCH}"
    git -C "${WORKTREE}" push origin "${BRANCH}"
    echo "Pushed; CI now verifies the branch in strict mode. Dependabot stops rebasing a branch with"
    echo "manual commits, so comment '@dependabot rebase' or recreate the update if the base moves."
else
    KEEP_WORKTREE=true
    echo "Not pushed. Review the diff, then push with: git -C $(printf '%q' "${WORKTREE}") push origin ${BRANCH}"
fi
cleanup
